#include <linux/moduleparam.h>
#include <linux/firmware.h>
#include <linux/netdevice.h>
#include <linux/delay.h>
#include <linux/mmc/card.h>
#include <linux/mmc/sdio_func.h>
#include <linux/mmc/sdio_ids.h>

#include "rda5890_defs.h"
#include "rda5890_dev.h"
#include "rda5890_if_sdio.h"
#include "rda5890_wid.h"
#include "rda5890_debugfs.h"

int rda5890_dbg_level = RDA5890_DL_CRIT;
int rda5890_dbg_area = RDA5890_DA_MAIN
	| RDA5890_DA_SDIO
	//| RDA5890_DA_ETHER
	| RDA5890_DA_WID
	| RDA5890_DA_WEXT
	//| RDA5890_DA_TXRX
	//| RDA5890_DA_PM
	;
bool rda5890_assoc_in_progress = false;

extern void export_msdc_clk_always_on();
extern void export_msdc_clk_always_on_off();

/* Module parameters */
module_param_named(debug_level, rda5890_dbg_level, int, 0644); 
module_param_named(debug_area, rda5890_dbg_area, int, 0644); 
int sdio_test_flag = 0;
module_param_named(sdio_test, sdio_test_flag, int, 0644); 

#define SDIO_VENDOR_ID_RDA5890        0x5449
#define SDIO_DEVICE_ID_RDA5890        0x0145

static const struct sdio_device_id if_sdio_ids[] = {
	{ SDIO_DEVICE(SDIO_VENDOR_ID_RDA5890, SDIO_DEVICE_ID_RDA5890) },
	{ /* end: all zeroes */						},
};

struct if_sdio_card *gCard = NULL;

MODULE_DEVICE_TABLE(sdio, if_sdio_ids);

struct if_sdio_packet {
	struct if_sdio_packet	*next;
    unsigned char packet_type;
	u16			nb;
	u8			buffer[0] __attribute__((aligned(4)));
};


static  u8 mask_irq_flag = 0;
void mask_all_sdio_irq(MTK_WCN_HIF_SDIO_CLTCTX cltCtx)
{
	if(!mask_irq_flag)
	{
		mtk_wcn_hif_sdio_writeb(cltCtx, IF_SDIO_FUN1_INT_MASK, 0);
		mask_irq_flag = 1;
	}	
}

void unmask_all_sdio_irq(MTK_WCN_HIF_SDIO_CLTCTX cltCtx)
{
	if(mask_irq_flag)
	{
		mtk_wcn_hif_sdio_writeb(cltCtx, IF_SDIO_FUN1_INT_MASK, 0x7);
		mask_irq_flag = 0;
	}
}

int if_sdio_card_to_host(struct if_sdio_card *card)
{
	int ret = 0;
	struct rda5890_private *priv = card->priv;
	u8 size_l = 0, size_h = 0;
	u16 size, chunk;

	RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_DEBUG,
		"%s <<< \n", __func__);

	ret = mtk_wcn_hif_sdio_readb(priv->cltCtx, IF_SDIO_AHB2SDIO_PKTLEN_L, &size_l);
	if (ret) {
		RDA5890_ERRP("read PKTLEN_L reg fail\n");
		goto out;
	}
	else
		RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_NORM,"read PKTLEN_L reg size_l:%d \n", size_l);

	ret = mtk_wcn_hif_sdio_readb(priv->cltCtx, IF_SDIO_AHB2SDIO_PKTLEN_H, &size_h);
	if (ret) {
		RDA5890_ERRP("read PKTLEN_H reg fail\n");
		goto out;
	}
	else
		RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_NORM,"read PKTLEN_H reg size_h:%d\n",size_h);	

	size = (size_l | ((size_h & 0x7f) << 8)) * 4;
	if (size < 4) {
		RDA5890_ERRP("invalid packet size (%d bytes) from firmware\n", size);
		ret = -EINVAL;
		goto out;
	}

	/* alignment is handled on firmside */
	//chunk = sdio_align_size(card->func, size);
	chunk = size;

	RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_NORM,
		"if_sdio_card_to_host, size = %d, aligned size = %d\n", size, chunk);

	/* TODO: handle multiple packets here */
	ret = mtk_wcn_hif_sdio_read_buf(priv->cltCtx, IF_SDIO_FUN1_FIFO_RD, (u32)card->buffer, chunk);
	if (ret) {
		RDA5890_ERRP("sdio_readsb fail, ret = %d, size = %d, aligned size = %d\n", ret, size, chunk);
		goto out;
	}

#if 1
    if(priv->version == 7)
    {
        mtk_wcn_hif_sdio_writeb(priv->cltCtx, IF_SDIO_FUN1_INT_PEND, 0x20);
        if(ret) {
           RDA5890_ERRP("clear SDIO Tx Complete flag failed\n");
           goto out;
        }
    }
#endif

	RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_VERB,
		"if_sdio_card_to_host, read done\n");

    if(ret) 
    {
        RDA5890_ERRP("clear SDIO Tx Complete flag failed\n");
        goto out;
    }

	if (sdio_test_flag) {
		rda5890_sdio_test_card_to_host(card->buffer, chunk);
		goto out;
	}

	/* TODO: this chunk size need to be handled here */
	rda5890_card_to_host(priv, card->buffer, chunk);

out:
	RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_DEBUG,
		"if_sdio_card_to_host >>>\n");
	return ret;
}

#ifdef WIFI_POWER_MANAGER
void if_sdio_sleep_worker(struct work_struct *work)
{
	struct if_sdio_card *card = NULL;
	int ret = 0;
    struct rda5890_private *priv = NULL;

	card = container_of(work, struct if_sdio_card,
                sleep_work.work);
	
	if(card)
		priv = card->priv;
	else
		goto out;

	if(!priv)
		goto out;

	RDA5890_DBGLAP(RDA5890_DA_PM, RDA5890_DL_CRIT, "Sleep\n");

	/* clear IF_SDIO_FUN1_INT_PEND, this allow device to sleep */
	ret = mtk_wcn_hif_sdio_writeb(priv->cltCtx, IF_SDIO_FUN1_INT_PEND, IF_SDIO_HOST_TX_FLAG);
	if (ret)
	{
		RDA5890_ERRP("clear IF_SDIO_HOST_TX_FLAG failed\n");
	}
	atomic_inc(&card->priv->sleep_flag); 

out:

    atomic_set(&card->sleep_work_is_active, 0);
#ifdef WIFI_UNLOCK_SYSTEM
    rda5990_wakeUnlock();
#endif
	return;
}

static int if_sdio_wakeup_card(struct if_sdio_card *card)
{
	struct rda5890_private *priv = card->priv;
	int ret = 0;

#ifdef WIFI_TEST_MODE
    if(rda_5990_wifi_in_test_mode())
        return 0;
#endif
    
#ifdef WIFI_CLOCK_ALWAYS_ON
	export_msdc_clk_always_on();
#endif	

	ret = mtk_wcn_hif_sdio_writeb(priv->cltCtx, IF_SDIO_FUN1_INT_TO_DEV,1);
	if (ret) {
		RDA5890_ERRP("if_sdio_wakeup_card write FUN1_INT_TO_DEV reg fail\n");
		goto out;
	}
    atomic_set(&priv->sleep_flag, 0);
      
	RDA5890_DBGLAP(RDA5890_DA_PM, RDA5890_DL_CRIT, "wake up \n");
	// wait 15ms, hardware need 13ms to wakeup
    rda5890_shedule_timeout(8);
out:
	return ret;
}

#endif //WIFI_POWER_MANAGER

static void if_sdio_host_to_card_worker(struct work_struct *work)
{
	struct if_sdio_card *card = NULL;
	struct rda5890_private *priv = NULL;
	struct if_sdio_packet *packet = NULL;
	int ret;
	unsigned long flags;
	u16 size;
	u32 retries = 500;
	u8 size_l, size_h, write_status = 0;
#define SDIO_HOST_WRITE_BATCH_SIZE   512
	u16 bytes_left, offset, batch;

	card = container_of(work, struct if_sdio_card, packet_worker);
	priv = card->priv;

	//mask_all_sdio_irq(priv->cltCtx);
#ifdef WIFI_POWER_MANAGER    
	if(is_sdio_init_complete())
	{   
		if(atomic_read(&card->sleep_work_is_active))
		{
		    cancel_delayed_work(&card->sleep_work);
#ifdef WIFI_UNLOCK_SYSTEM    
		    rda5990_wakeUnlock();
#endif 
		    atomic_set(&card->sleep_work_is_active, 0);
		}
	}
#endif

	while (1) 
	{  
		retries = 500;
		  
		spin_lock_irqsave(&card->lock, flags);
		packet = card->packets;
		if (packet)
			card->packets = packet->next;
		spin_unlock_irqrestore(&card->lock, flags);

		if (!packet)
			break;
		
#ifdef WIFI_POWER_MANAGER		
		if (atomic_read(&priv->sleep_flag)) 
		{
			/* Deivce maybe sleep, wakeup it. */
			RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_NORM, "Wakeup\n");
			ret = if_sdio_wakeup_card(card);
			if (ret) 
			{
				RDA5890_ERRP("wakeup card fail\n");
				goto out;
			}
		}
#endif
		while(retries && is_sdio_patch_complete()) //check write flow ctrl
		{
			ret = mtk_wcn_hif_sdio_readb(priv->cltCtx, IF_SDIO_FUN1_INT_PEND, &write_status);
			if(ret)
			{
				RDA5890_ERRP("read IF_SDIO_FUN1_INT_PEND failed\n");
				goto release;
			}
			if((write_status & IF_SDIO_INT_RXCMPL) == 0)
			{
				//RDA5890_ERRP("**** sdio is busy retry next time \n ");
				retries --;
				schedule();
			}
		    	else
			{
				ret = mtk_wcn_hif_sdio_writeb(priv->cltCtx, IF_SDIO_FUN1_INT_PEND, IF_SDIO_INT_RXCMPL);
				if(ret)
				{
					RDA5890_ERRP("write IF_SDIO_FUN1_INT_PEND failed\n");
					goto release;
				}
				break;
			}
		}
	      
		RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_DEBUG,
		"if_sdio_host_to_card_worker, send one packet, size = %d\n", packet->nb);
		/* write length */
		size = packet->nb/4;
		size_l = (u8)(size & 0xff);
		size_h = (u8)((size >> 8) & 0x7f);
		size_h |= 0x80;

		ret = mtk_wcn_hif_sdio_writeb(priv->cltCtx, IF_SDIO_SDIO2AHB_PKTLEN_L, size_l);
		if (ret) {
			RDA5890_ERRP("write PKTLEN_L reg fail\n");
			goto release;
		}
		ret = mtk_wcn_hif_sdio_writeb(priv->cltCtx, IF_SDIO_SDIO2AHB_PKTLEN_H, size_h);
		if (ret) {
			RDA5890_ERRP("write PKTLEN_H reg fail\n");
			goto release;
		}

		/* write data */
		bytes_left = packet->nb;
		offset = 0;
		while(bytes_left) 
		{
			batch = (bytes_left < SDIO_HOST_WRITE_BATCH_SIZE)?
			bytes_left:SDIO_HOST_WRITE_BATCH_SIZE;
			ret = mtk_wcn_hif_sdio_write_buf(priv->cltCtx, IF_SDIO_FUN1_FIFO_WR,
			packet->buffer + offset, batch);
			if (ret) {
				RDA5890_ERRP("sdio_writesb fail, ret = %d\n", ret);
				goto release;
			}
			RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_DEBUG,
			"write batch %d, offset = %d\n", batch, offset);
			offset += batch;
			bytes_left -= batch;
		}

		RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_DEBUG,
		"if_sdio_host_to_card_worker, send one packet done\n");

		release:

		kfree(packet);
		packet = NULL;
	}

	out:

	if(is_sdio_init_complete()) //init complete should start sleep_work
	{
#ifdef WIFI_UNLOCK_SYSTEM    
		rda5990_wakeLock();
#endif   

#ifdef WIFI_POWER_MANAGER
#ifdef WIFI_TEST_MODE
		if(rda_5990_wifi_in_test_mode())
		    return;
#endif  //end WIFI_TEST_MODE
		atomic_set(&card->sleep_work_is_active, 1);
	    queue_delayed_work(priv->work_thread, &card->sleep_work, rda5890_assoc_in_progress ? 30 * HZ : HZ/5);  // 100ms
		//queue_delayed_work(priv->work_thread, &card->sleep_work, HZ/5);  // 100ms
#endif	    
	}
}

/*******************************************************************/
/* RDA5890 callbacks                                              */
/*******************************************************************/

static int if_sdio_host_to_card(struct rda5890_private *priv,
		u8 *buf, u16 nb, unsigned char packet_type)
{
	int ret = 0;
	struct if_sdio_card *card;
	struct if_sdio_packet *packet, *cur;
	u16 size;
	unsigned long flags;

	RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_DEBUG,
		"%s >>>\n", __func__);

	card = priv->card;
    nb += 2; //for chip packet head
	if (nb > (65536 - sizeof(struct if_sdio_packet))) {
		ret = -EINVAL;
		goto out;
	}

	//size = sdio_align_size(card->func, nb);
	size = nb;
	size = ((size + 3)/4)*4;

	RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_NORM,
		"if_sdio_host_to_card, size = %d, aligned size = %d\n", nb, size);

	packet = kzalloc(sizeof(struct if_sdio_packet) + size,
			GFP_ATOMIC);
	if (!packet) {
		ret = -ENOMEM;
		goto out;
	}

	packet->next = NULL;
	packet->nb = size;
    packet->packet_type = packet_type;
    packet->buffer[0] = (char)((nb)&0xFF);
	packet->buffer[1] = (char)(((nb)>>8)&0x0F);
    if(packet_type == DATA_REQUEST_PACKET)
	    packet->buffer[1] |= 0x10;  // for DataOut 0x1
	else 
        packet->buffer[1] |= 0x40;  //for request
	memcpy(packet->buffer + 2, buf, nb - 2);

	spin_lock_irqsave(&card->lock, flags);

	if (!card->packets)
		card->packets = packet;
	else {
		cur = card->packets;
		while (cur->next)
			cur = cur->next;
		cur->next = packet;
	}

	spin_unlock_irqrestore(&card->lock, flags);

    queue_work(card->work_thread, &card->packet_worker);

	RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_DEBUG,
		"%s <<<\n", __func__);

	ret = 0;

out:
	return ret;
}

extern void export_wifi_eirq_enable(void);
extern int  rda_wlan_version(void);

u32 rda_hif_sdio_probe(MTK_WCN_HIF_SDIO_CLTCTX cltCtx, MTK_WCN_HIF_SDIO_FUNCINFO* funcInfo)
{   
    struct if_sdio_card *card = NULL;
    struct rda5890_private *priv = NULL;
    struct if_sdio_packet *packet = NULL; 
    int ret = -1;
    unsigned long flags;

    RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_DEBUG,
    	"%s >>>\n", __func__);

    printk(KERN_INFO "20120905 RDA5890: SDIO card attached func num %d block size %d \n", funcInfo->func_num, funcInfo->blk_sz);

    if(gCard)
    {
	    RDA5890_ERRP("rda5890 sdio has probed \n");
	    return 0;
    }
    card = kzalloc(sizeof(struct if_sdio_card), GFP_KERNEL);
    if (!card) {
    	RDA5890_ERRP("kzalloc fail\n");
    	return -ENOMEM;
    }
    

    card->func = NULL;
    spin_lock_init(&card->lock);  
    atomic_set(&card->wid_complete_flag, 0);
    INIT_WORK(&card->packet_worker, if_sdio_host_to_card_worker);
    card->work_thread = create_singlethread_workqueue("rda5890_if_sdio_worker");
    
#ifdef WIFI_POWER_MANAGER   
    atomic_set(&card->sleep_work_is_active, 0);
    INIT_DELAYED_WORK(&card->sleep_work, if_sdio_sleep_worker); 
#endif

#ifdef WIFI_UNLOCK_SYSTEM 
    atomic_set(&wake_lock_counter, 0);
    wake_lock_init(&sleep_worker_wake_lock, WAKE_LOCK_SUSPEND, "RDA_sleep_worker_wake_lock");
#endif
    
    priv = rda5890_add_card(card);
	if (!priv) {
		RDA5890_ERRP("rda5890_add_card fail\n");
		ret = -ENOMEM;
		goto remove_card;
	}
    rda5890_debugfs_init_one(priv);

    card->priv = priv;
    priv->card = card;
    priv->hw_host_to_card = if_sdio_host_to_card;
    priv->cltCtx = cltCtx;
    
    priv->version = rda_wlan_version();
    priv->version &= 0x1f;
    if(priv->version  != 4 && priv->version  != 7
    	&& priv->version != 5)
    {
        RDA5890_ERRP("no correct version exit wlan initiate \n");
        goto remove_card;
    }

    /*
     * Enable interrupts now that everything is set up
     */
    printk(KERN_INFO " mtk_wcn_hif_sdio_writeb IF_SDIO_FUN1_INT_MASK \n");
    ret = mtk_wcn_hif_sdio_writeb(cltCtx, IF_SDIO_FUN1_INT_MASK, 0x07); 
    if (ret) {
    	RDA5890_ERRP("enable func interrupt fail\n");
    	goto remove_card;
    }

#ifdef WIFI_TEST_MODE
    if(!rda_5990_wifi_in_test_mode())
        {
#endif        
            ret = rda5890_sdio_init(priv);
            if(ret < 0)
                goto remove_card;
			
            gCard = card;
        	ret=rda5890_disable_self_cts(priv);   
            if(ret < 0)
                goto remove_card;

        	ret=rda5890_disable_block_bt(priv);
            if(ret < 0)
                goto remove_card;

            ret = rda5890_set_scan_timeout(priv);
            if (ret) {
                	RDA5890_ERRP("rda5890_set_scan_timeout fail, ret = %d\n", ret);
                	goto remove_card;
            }
        		
            ret= rda5890_set_listen_interval(priv, WIFI_LISTEN_INTERVAL);
            if(ret < 0)
                goto remove_card;

            ret = rda5890_set_link_loss_threshold(priv, WIFI_LINK_LOSS_THRESHOLD);
            if(ret < 0)
                goto remove_card;
            
            ret = rda5890_init_pm(priv);
            if(ret < 0)
                goto remove_card;
            
#ifdef WIFI_TEST_MODE            
        }
    else
        {
            ret = rda5890_set_test_mode(priv);
            if(ret < 0)
                goto remove_card;

			gCard = card;
		}
#endif
#ifdef WIFI_SELECT_CHANNEL    
    // Add for choose operation band, 3rd parameter indicate which band will be used.
    // 0/1: channel1-11, 2:channel1-13, 3:channel1-14, other values: channel1-13
#ifdef  CHANNEL_11
    if (rda5890_generic_set_uchar(priv, 0x0047, 1) < 0)
#elif defined CHANNEL_14
    if (rda5890_generic_set_uchar(priv, 0x0047, 3) < 0)    	
#else /* CHANNEL_13 && others */
    if (rda5890_generic_set_uchar(priv, 0x0047, 2) < 0)    
#endif   		
    {
        RDA5890_ERRP("set op_band failed!\n");
        goto remove_card;
    }
    else
    {
#ifdef  CHANNEL_11    	
		channel_nums = 11;
#elif defined CHANNEL_12
		channel_nums = 12;
#elif defined CHANNEL_14
		channel_nums = 14;
#else /* CHANNEL_13 && others */
		channel_nums = 13;
#endif  		
    }
#endif  
    if (sdio_test_flag) {
        unsigned char mac_addr[6];
        RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_TRACE,
        "SDIO TESTING MODE\n");


        ret = rda5890_get_mac_addr(priv, mac_addr);
        printk(KERN_INFO "Test rda5890_get_mac_addr %x:%x:%x:%x:%x:%x", mac_addr[0],mac_addr[1],mac_addr[2],mac_addr[3],mac_addr[4],mac_addr[5]);
        goto done;
    }

    ret = rda5890_start_card(priv);
    if (ret) {
    	RDA5890_ERRP("rda5890_start_card fail, ret = %d\n", ret);
    	goto remove_card;
    }
done:
    printk(KERN_INFO "RDA5890: SDIO card started\n");

out:
    RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_DEBUG,
    	"%s ret:%d <<<\n", __func__, ret);
    return ret;

remove_card:   
    
    if (atomic_read(&card->wid_complete_flag) && priv)
    {
        complete(&priv->wid_done);
        printk(KERN_INFO "*****RDA5890: probe send wid complete\n");
    }
    flush_work(&card->packet_worker);
    cancel_work_sync(&card->packet_worker);
    
#ifdef WIFI_POWER_MANAGER
    cancel_delayed_work(&card->sleep_work);
#endif
    destroy_workqueue(card->work_thread);
    
    if(priv)
        rda5890_remove_card(priv);

    while (card->packets) {
    	packet = card->packets;
    	card->packets = card->packets->next;
    	kfree(packet);	
    }
    
    kfree(card);
    gCard = NULL;
    goto out;
}

u32 rda_hif_sdio_remove(MTK_WCN_HIF_SDIO_CLTCTX cltCtx)
{
    struct if_sdio_card *card;
    struct if_sdio_packet *packet;
    unsigned char count = 20; 

    RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_DEBUG,
    	"%s >>>\n", __func__);

    if(!gCard)
        return -EPERM;
    
    printk(KERN_INFO "RDA5890: SDIO card detached\n");

    card = gCard;  

    while(card->priv->scan_running && count--)
    {
        rda5890_shedule_timeout(100);
        printk("remove card wait scan complete \n");
    }

#ifdef WIFI_POWER_MANAGER
    cancel_delayed_work_sync(&card->sleep_work);
#endif
    cancel_work_sync(&card->packet_worker);
    
    if (atomic_read(&card->wid_complete_flag) && card->priv)
    {
        complete(&card->priv->wid_done);
        printk(KERN_INFO "*****RDA5890: send wid complete\n");
    }

    netif_stop_queue(card->priv->dev);
    netif_carrier_off(card->priv->dev);
    rda5890_stop_card(card->priv);
    rda5890_debugfs_remove_one(card->priv);
    destroy_workqueue(card->work_thread);
    rda5890_remove_card(card->priv);
    
    while (card->packets) {
    	packet = card->packets;
    	card->packets = card->packets->next;
    	kfree(packet);
    } 
    
    kfree(card);
    gCard = NULL;

#ifdef WIFI_UNLOCK_SYSTEM 
    rda5990_wakeLock_destroy();
#endif  

    printk(KERN_INFO "RDA5890: SDIO card removed\n");

    RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_DEBUG,
    	"%s <<<\n", __func__); 
    return 0;   
}

u32 rda_hif_sdio_irq(MTK_WCN_HIF_SDIO_CLTCTX cltCtx)
{ 
    int ret = 0;
    struct if_sdio_card *card;
    struct rda5890_private *priv;
    u8 status;

    RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_DEBUG,
    	"%s >>>\n", __func__); 

    if(!gCard)
    {
	    return -EIO;
    }  
   
    card = gCard;
    priv = card->priv;
	
    ret = mtk_wcn_hif_sdio_readb(priv->cltCtx, IF_SDIO_FUN1_INT_STAT, &status);
    if (ret)
    	goto out;
    
    RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_VERB,
    	"if_sdio_interrupt, status = 0x%02x\n", status);

    if (status & IF_SDIO_INT_AHB2SDIO)
    	if_sdio_card_to_host(card);

    if (status & IF_SDIO_INT_ERROR) {
    	ret = mtk_wcn_hif_sdio_writeb(priv->cltCtx, IF_SDIO_FUN1_INT_PEND ,IF_SDIO_INT_ERROR);
    	if (ret) {
    		RDA5890_ERRP("write FUN1_INT_STAT reg fail\n");
    		goto out;
    	}

    	RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_TRACE,
    		"%s, INT_ERROR\n", __func__);
    }

#ifdef WIFI_CLOCK_ALWAYS_ON
	export_msdc_clk_always_on_off();
#endif

    out:
    RDA5890_DBGLAP(RDA5890_DA_SDIO, RDA5890_DL_DEBUG,
    	"%s <<< ret=%d \n", __func__, ret);   
    return ret;
}

MTK_WCN_HIF_SDIO_FUNCINFO rda_sdio_func_info = 
    {
        0x5449,  //vendor id
        0x0145,  //card id
        0x01,    //func id
        512      //block size
    };


MTK_WCN_HIF_SDIO_CLTINFO rda_sdio_client = 
    {
        &rda_sdio_func_info,
        sizeof(rda_sdio_func_info)/sizeof(MTK_WCN_HIF_SDIO_FUNCINFO),
        rda_hif_sdio_probe,
        rda_hif_sdio_remove,
        rda_hif_sdio_irq
    };


/*******************************************************************/
/* Module functions                                                */
/*******************************************************************/

static int __init if_sdio_init_module(void)
{
    int ret = 0;

    printk(KERN_INFO "\nRDA5890 SDIO WIFI Driver 20120220\n");
    printk(KERN_INFO "Ver: %d.%d.%d\n\n", 
    	RDA5890_SDIOWIFI_VER_MAJ, 
    	RDA5890_SDIOWIFI_VER_MIN, 
    	RDA5890_SDIOWIFI_VER_BLD);


	rda5890_debugfs_init();
    mtk_wcn_hif_sdio_client_reg(&rda_sdio_client);
	return ret;
}

static void __exit if_sdio_exit_module(void)
{
	mtk_wcn_hif_sdio_client_unreg(&rda_sdio_client);
	rda5890_debugfs_remove();
}


module_init(if_sdio_init_module);
module_exit(if_sdio_exit_module);

MODULE_DESCRIPTION("RDA5890 SDIO WLAN Driver");
MODULE_AUTHOR("lwang");
MODULE_LICENSE("GPL");
