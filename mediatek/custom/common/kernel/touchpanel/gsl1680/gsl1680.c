
/******************************************************************************

                  Copyright (C), 2010-2012, Silead, Inc.

 ******************************************************************************
  Filename      : gsl1680-d0.c
  Version       : R2.0
  Aurthor       : mark_huang
  Creattime     : 2012.6.20
  Description   : Driver for Silead I2C touchscreen.

******************************************************************************/

#include "tpd.h"
#include <linux/interrupt.h>
#include <cust_eint.h>
#include <linux/i2c.h>
#include <linux/sched.h>
#include <linux/kthread.h>
#include <linux/rtpm_prio.h>
#include <linux/wait.h>
#include <linux/delay.h>
#include <linux/time.h>

#include <mach/mt6577_pm_ldo.h>
#include <mach/mt6577_typedefs.h>
#include <mach/mt6577_boot.h>

#include <gsl1680.h>
#include "tpd_custom_gsl1680.h" 

#define ANDROID_VERSION_40

#define GSL_DEV_NAME "gsl1680"

extern struct tpd_device *tpd;
extern void mt65xx_eint_unmask(unsigned int line);
extern void mt65xx_eint_mask(unsigned int line);
extern void mt65xx_eint_set_hw_debounce(kal_uint8 eintno, kal_uint32 ms);
extern kal_uint32 mt65xx_eint_set_sens(kal_uint8 eintno, kal_bool sens);
extern void mt65xx_eint_registration(kal_uint8 eintno, kal_bool Dbounce_En, kal_bool ACT_Polarity, void (EINT_FUNC_PTR)(void),kal_bool auto_umask);

static struct gsl_ts_data *ddata = NULL;

static int boot_mode = NORMAL_BOOT;
static int tpd_flag = 0;


#define I2C_TRANS_SPEED 400	//100 khz or 400 khz
#define TPD_REG_BASE 	0x00

#define GSL1680_DEBUG		0

#if GSL1680_DEBUG
#define GSL_FUN(f) 				printk("[tp-gsl]%s => %s[%d]\n", __FILE__, __FUNCTION__, __LINE__)
#define GSL_DEBUG(fmt, arg...) 	printk(KERN_INFO "[tp-gsl]" fmt "\n", ##arg)
#else
#define GSL_FUN(f) 
#define GSL_DEBUG(fmt, arg...)
#endif

#ifdef CUSTOM_CTP_HAVE_BUTTON
#define TPD_HAVE_BUTTON
#endif

#ifdef TPD_HAVE_BUTTON

#define TPD_KEY_COUNT           4
#define TPD_KEYS                {KEY_HOME, KEY_MENU, KEY_BACK,KEY_SEARCH}

#if defined(__DRV_LCD_RESOLUTION_480X800__)
#define TPD_KEYS_DIM 		{{50,845,100,70},{160,845,120,70},{310,845,120,70},{430,845,100,70}}
#else
// Default resolution: 320x480
#define TPD_KEYS_DIM      {{40,535,60,32},{120,535,60,32},{200,535,60,32},{280,535,60,32}}
#endif

extern void tpd_button(unsigned int x, unsigned int y, unsigned int down);
static int tpd_keys_local[TPD_KEY_COUNT] = TPD_KEYS;
static int tpd_keys_dim_local[TPD_KEY_COUNT][4] = TPD_KEYS_DIM;
#endif

static struct tpd_bootloader_data_t g_bootloader_data;

static const struct i2c_device_id gsl_device_id[] = {{TPD_DEVICE,0},{}};

#ifdef ANDROID_VERSION_40
#define	I2C_NUMBER		0
static struct i2c_board_info __initdata i2c_tpd = { I2C_BOARD_INFO("mtk-tpd", (0x80>>1))};
#else
static unsigned short force[] = {0,0x80,I2C_CLIENT_END,I2C_CLIENT_END};
static const unsigned short * const forces[] = { force, NULL };
static struct i2c_client_address_data gsl_addr_data = { .forces = forces, };
#endif

static int gsl_local_init(void);
static int gsl1680_suspend(struct i2c_client *client);
static int gsl1680_resume(struct i2c_client *client);

/*****************************************************************************
Prototype    	: gsl_read_interface
Description  	: gsl chip tranfer function
Input        	: struct i2c_client *client
		  u8 reg
		  u8 *buf
		  u32 num
Output		: None
Return Value 	: static

 *****************************************************************************/
static struct tpd_driver_t gsl_driver = {
	.tpd_device_name = GSL_DEV_NAME,
	.tpd_local_init = gsl_local_init,
	.suspend = gsl1680_suspend,
	.resume = gsl1680_resume,
#ifdef TPD_HAVE_BUTTON
	.tpd_have_button = 1,
#else
	.tpd_have_button = 0,
#endif
};

static u32 gsl_read_interface(struct i2c_client *client,
		u8 reg, u8 *buf, u32 num)
{
	struct i2c_msg xfer_msg[2] = {0};
	u32 ret = 0;
	u8 i;

	xfer_msg[0].addr = client->addr;
	xfer_msg[0].flags = 0;
	xfer_msg[0].len = 1;
	xfer_msg[0].buf = (char *)&reg;
	xfer_msg[0].timing = I2C_TRANS_SPEED;

	xfer_msg[1].addr = client->addr;
	xfer_msg[1].flags = I2C_M_RD;
	xfer_msg[1].len = num;
	xfer_msg[1].buf = buf;
	xfer_msg[1].timing = I2C_TRANS_SPEED;
	if(num > 8) {
		for(i = 0; i < num; i = i + 8)
		{
			if(num - i >= 8) {
				xfer_msg[1].len = 8;
			}
			else {
				xfer_msg[1].len = num - i;
			}
			xfer_msg[1].buf = buf + i;
			xfer_msg[0].buf = (char *)&reg;
			ret = i2c_transfer(client->adapter, xfer_msg, 2);
			reg += 8;
		}
		return ret; 
	}
	if(reg < 0x80)
		i2c_transfer(client->adapter, xfer_msg, 2);
	return i2c_transfer(client->adapter, xfer_msg, 2);
		
}

/*****************************************************************************
Prototype    : gsl_write_interface
Description  : gsl chip tranfer function
Input        : struct i2c_client *client
const u8 reg
u8 *buf
u32 num
Output       : None
Return Value : static

 *****************************************************************************/
static u32 gsl_write_interface(struct i2c_client *client,
		const u8 reg, u8 *buf, u32 num)
{
	struct i2c_msg xfer_msg[1] = {0};
	u8 tmp_buf[num + 1];

	tmp_buf[0] = reg;
	memcpy(tmp_buf + 1, buf, num);

	xfer_msg[0].addr = client->addr;
	xfer_msg[0].len = num + 1;
	xfer_msg[0].flags = client->flags & I2C_M_TEN;
	xfer_msg[0].buf = tmp_buf;
	xfer_msg[0].timing = I2C_TRANS_SPEED;

	return i2c_transfer(client->adapter, xfer_msg, 1);
}


/*****************************************************************************
Prototype    : gsl_start_core
Description  : touchscreen chip work
Input        : struct i2c_client *client
Output       : None
Return Value : static

 *****************************************************************************/
static void gsl_start_core(struct i2c_client *client)
{
	u8 buf[4] = {0};
	gsl_write_interface(client, GSL_START_REG, buf, 4);
}

/*****************************************************************************
Prototype    : gsl_reset_core
Description  : touchscreen chip soft reset
Input        : struct i2c_client *client
Output       : None
Return Value : static

 *****************************************************************************/
static void gsl_reset_core(struct i2c_client *client)
{
	u8 buf[4] = {0};
	buf[0] = 0x88;
	gsl_write_interface(client, GSL_START_REG, buf, 4);
	buf[0] = 0x04;
	gsl_write_interface(client, GSL_CLOCK_REG, buf, 4);
	buf[0] = 0x00;
	gsl_write_interface(client, POWE_FAIL_REG, buf, 4);
}

/*****************************************************************************
Prototype    : gsl_load_fw
Description  : gsl chip load the firmware throught I2C
Input        : struct i2c_client *client
Output       : None
Return Value : static

 *****************************************************************************/
static void gsl_load_fw(struct i2c_client *client)
{
	u8 buf[4] = {0};
	u8 addr = 0;
	u8 send_flag = 1;
	u32 source_line = 0;
	u32 source_len = ARRAY_SIZE(GSL1680_D0_FW);

	for (; source_line < source_len; source_line++) {
		/* init page trans, set the page val */
		addr = (u8)GSL1680_D0_FW[source_line].offset;

		memcpy(buf, &GSL1680_D0_FW[source_line].val, 4);
		gsl_write_interface(client, addr, buf, 4);
	}

#if 1
	buf[0] = 0x0;
	buf[1] = 0x0;
	buf[2] = 0x0;
	buf[3] = 0x0;
	gsl_write_interface(client, 0xf0, buf, 4);
	gsl_read_interface(client, 0x0, buf, 4);
//	gsl_read_interface(client, 0x0, buf, 4);

	GSL_DEBUG("[i2c_test]{0x0,0x%02x%02x%02x%02x}" , buf[3], buf[2], buf[1], buf[0]);
#endif
}

#define JITTER_RANGE	10
/*****************************************************************************
Prototype    : gsl_avoid_jit
Description  : avoid coor jitter while touching
Input        : union gsl_touch_info *ti
Output       : None
Return Value : static

 *****************************************************************************/
static void gsl_avoid_jit(union gsl_touch_info *ti)
{
    	int i, j;
    	if (ddata->base.finger_num) {
        	ddata->base.finger_num = ti->finger_num;
        	for (i = 0; i < ti->finger_num; i++) {
            		j = ti->point[i].id - 1;
            		if (abs(ddata->base.point[j].x - ti->point[i].x) < JITTER_RANGE && abs(ddata->base.point[j].y - ti->point[i].y) < JITTER_RANGE) {
            			/* avoid jitter */
            			ti->point[i].x = ddata->base.point[j].x;
            			ti->point[i].y = ddata->base.point[j].y;
#ifndef JUST_ONCE_AVOID
            		} else {
                		/* touch valid */
                		ddata->base.point[j] = ti->point[i];
#endif
            		}
        	}
    	} else {
		for(i = 0;i < ti->finger_num; i++)
		{
			ddata->base.point[ti->point[i].id - 1].x = ti->point[i].x;
			ddata->base.point[ti->point[i].id - 1].y = ti->point[i].y;
			ddata->base.point[ti->point[i].id - 1].id = ti->point[i].id;
		}
		ddata->base.finger_num = ti->finger_num;
//        	ddata->base = *ti;
    	}
}


/*****************************************************************************
Prototype    : check_mem_data
Description  : check mem data second to deal with power off
Input        : struct i2c_client *client
Output       : None
Return Value : static

 *****************************************************************************/
static void check_mem_data(struct i2c_client *client)
{
	char read_buf[4] = {0};
	gsl_read_interface(client, 0xb0, read_buf, 4);

	GSL_DEBUG("[gsl1680][%s] addr = 0xb0; read_buf = %02x%02x%02x%02x\n",
			__func__, read_buf[3], read_buf[2], read_buf[1], read_buf[0]);
	if (read_buf[3] == 0x5a && read_buf[2] == 0x5a && read_buf[1] == 0x5a && read_buf[0] == 0x5a)
	{
		return;
	}
	else
	{
		gsl_reset_core(client);
		gsl_load_fw(client);
		gsl_start_core(client);
		gsl_reset_core(client);
		gsl_start_core(client);	
	}
}

/*****************************************************************************
Prototype    : gsl_report_point
Description  : gsl1680 report touch event
Input        : union gsl_touch_info *ti
Output       : None
Return Value : static

 *****************************************************************************/
static void gsl_report_point(union gsl_touch_info *ti)
{
    	int tmp = 0;

		GSL_DEBUG("[gsl1680] gsl_report_point %d ", ti->finger_num);
    
    	if (unlikely(ti->finger_num == 0)) {
#ifdef TPD_HAVE_BUTTON
        	if (boot_mode != NORMAL_BOOT && tpd->btn_state)
            		tpd_button(ti->point[0].x, ti->point[0].y, 0);
#endif
        	input_report_abs(tpd->dev, ABS_MT_TOUCH_MAJOR, 0);
			input_report_key(tpd->dev, BTN_TOUCH, 0);
        	input_mt_sync(tpd->dev);
    	} 
		else {
        	for (tmp = 0; ti->finger_num > tmp; tmp++) 
			{
#ifdef TPD_HAVE_BUTTON 	
				if(MTK_LCM_PHYSICAL_ROTATION == 270 || MTK_LCM_PHYSICAL_ROTATION == 90 )
				{
					if(boot_mode!=NORMAL_BOOT && ti->point[tmp].x >=TPD_RES_Y ) 
					{ 
						tpd_button(ti->point[tmp].x, ti->point[tmp].y, 1);
						return;
					}
				}
				else
				{
					GSL_DEBUG("----Button----MTK_LCM_PHYSICAL_ROTATION=%d \n",MTK_LCM_PHYSICAL_ROTATION);
					if(boot_mode!=NORMAL_BOOT && ti->point[tmp].y >=TPD_RES_Y ) 
					{ 
						printk("Enter Button Mode!!!!! \n");			
						tpd_button(ti->point[tmp].x, ti->point[tmp].y, 1);
						return;
					}
				}
#endif

				
#if 0//#ifdef TPD_HAVE_BUTTON
        		if (ti->finger_num == 1) {
            		if (boot_mode != NORMAL_BOOT && tpd->btn_state)
                			tpd_button(ti->point[0].x, ti->point[0].y, 1);
       		 	}
#endif
				GSL_DEBUG("(x[%d],y[%d]) = (%d,%d);", ti->point[tmp].id, ti->point[tmp].id, ti->point[tmp].x, ti->point[tmp].y);
				input_report_key(tpd->dev, BTN_TOUCH, 1);
				input_report_abs(tpd->dev, ABS_MT_TOUCH_MAJOR, 1);
				
				if(boot_mode!=NORMAL_BOOT && (MTK_LCM_PHYSICAL_ROTATION == 270 || MTK_LCM_PHYSICAL_ROTATION == 90) )
				{
					int temp;
					temp = ti->point[tmp].y;
					ti->point[tmp].y = ti->point[tmp].x;
					ti->point[tmp].x = TPD_RES_X-temp;
				}

					
        		input_report_abs(tpd->dev, ABS_MT_TRACKING_ID, ti->point[tmp].id - 1);
        		input_report_abs(tpd->dev, ABS_MT_POSITION_X, ti->point[tmp].x);
        		input_report_abs(tpd->dev, ABS_MT_POSITION_Y, ti->point[tmp].y);
        		
        		input_mt_sync(tpd->dev);
        	}
    	}
    	input_sync(tpd->dev);
}

/*****************************************************************************
Prototype    : gsl_report_work
Description  : to deal interrupt throught workqueue
Input        : struct work_struct *work
Output       : None
Return Value : static

 *****************************************************************************/
static void gsl_report_work(void)
{

	u8 buf[4] = {0xff, 0xff, 0xff, 0xff};
	u8 i = 0;
	u16 ret = 0;

	GSL_DEBUG("[gsl1680] enter gsl_report_work");

	memset((u8 *)(ddata->ti), 0, sizeof(union gsl_touch_info));

    	
	gsl_read_interface(ddata->client, POWE_FAIL_REG, buf, 4);

	if((buf[3] != 0) || (buf[2] != 0) || (buf[1] != 0) || (buf[0] != 0)) 
	{
		GSL_DEBUG("0xbc = {0x%02x%02x%02x%02x},",buf[3],buf[2],buf[1],buf[0]);
		gsl_reset_core(ddata->client);
		gsl_start_core(ddata->client);
		msleep(20);
		check_mem_data(ddata->client);	
	}

    	gsl_read_interface(ddata->client, TOUCH_INFO_REG, (u8 *)(ddata->ti), 24);
		if(MTK_LCM_PHYSICAL_ROTATION == 270 || MTK_LCM_PHYSICAL_ROTATION == 90)
		{
			
			GSL_DEBUG("MTK_LCM_PHYSICAL_ROTATION = %d\n",MTK_LCM_PHYSICAL_ROTATION);
			for(i = 0;i < 5 ;i++){
				ret = ddata->ti->point[i].x;
				ddata->ti->point[i].x = ddata->ti->point[i].y;
				ddata->ti->point[i].y = ret;
			}
		}
		gsl_avoid_jit(ddata->ti);
    	gsl_report_point(ddata->ti);

}

/*****************************************************************************
Prototype    : tpd_eint_interrupt_handler
Description  : gsl1680 ISR
Input        : None
Output       : None
Return Value : static

 *****************************************************************************/
static int tpd_eint_interrupt_handler(void)
{

	GSL_DEBUG("[gsl1680] TPD interrupt has been triggered\n");

	mt65xx_eint_mask(CUST_EINT_TOUCH_PANEL_NUM);
	if (!work_pending(&ddata->work)) {
		queue_work(ddata->wq, &ddata->work);
	}
}

/*****************************************************************************
Prototype    : gsl1680_hw_init
Description  : gsl1680 set gpio
Input        : None
Output       : None
Return Value : static

 *****************************************************************************/
static void gsl1680_hw_init(void)
{
	//power on
	/*
	mt_set_gpio_mode(GPIO_CTP_EN_PIN, GPIO_CTP_EN_PIN_M_GPIO);
	mt_set_gpio_dir(GPIO_CTP_EN_PIN, GPIO_DIR_OUT);
	mt_set_gpio_out(GPIO_CTP_EN_PIN, GPIO_OUT_ONE);
	*/
//	 hwPowerOn(MT65XX_POWER_LDO_VGP2, VOL_2800, "TP"); 
	/* reset ctp gsl1680 */
    #ifdef MT6577
    //power on, need confirm with SA
    hwPowerOn(MT65XX_POWER_LDO_VGP2, VOL_2800, "TP");
    //hwPowerOn(MT65XX_POWER_LDO_VGP, VOL_1800, "TP");      
    #endif


	mt_set_gpio_mode(GPIO_CTP_RST_PIN, GPIO_MODE_00);
	mt_set_gpio_dir(GPIO_CTP_RST_PIN, GPIO_DIR_OUT);
	mt_set_gpio_out(GPIO_CTP_RST_PIN, GPIO_OUT_ZERO);
	msleep(20);
	mt_set_gpio_out(GPIO_CTP_RST_PIN, GPIO_OUT_ONE);
	/* set interrupt work mode */
	mt_set_gpio_mode(GPIO_CTP_EINT_PIN, GPIO_CTP_EINT_PIN_M_EINT);
	mt_set_gpio_dir(GPIO_CTP_EINT_PIN, GPIO_DIR_IN);
	mt_set_gpio_pull_enable(GPIO_CTP_EINT_PIN, GPIO_PULL_ENABLE);
	mt_set_gpio_pull_select(GPIO_CTP_EINT_PIN, GPIO_PULL_UP);
	msleep(100);
}

/*****************************************************************************
Prototype    : gsl1680_sw_init
Description  : gsl1680 load firmware
Input        : struct i2c_client *client
Output       : int
Return Value : static

 *****************************************************************************/
static int gsl1680_sw_init(struct i2c_client *client)
{
    	
	int ret;
    u8 buf[4] = { 0x0, 0x0, 0x0, 0x12 };

	GSL_FUN();

	GSL_DEBUG("i2c_client addr = 0x%02x", client->addr);

	if (!i2c_check_functionality(client->adapter, I2C_FUNC_I2C)) {
		GSL_DEBUG("I2C BUS function no support");
		return -1;
	}

	ret = gsl_write_interface(client, 0, &buf, 4);

	GSL_DEBUG("I2C transfer %s, line: %d",ret < 0 ? "error" : "success", __LINE__);
	if(ret < 0)
        return ret;

	gsl_reset_core(client);
	gsl_load_fw(client);
	gsl_start_core(client);
	gsl_reset_core(client);
	gsl_start_core(client);
	msleep(20);
	check_mem_data(client);
    	return 0;
}

/*****************************************************************************
Prototype    : gsl1680_probe
Description  : setup gsl1680 driver
Input        : struct i2c_client *client
               const struct i2c_device_id *id
Output       : None
Return Value : static

 *****************************************************************************/
static int __devinit gsl1680_probe(struct i2c_client *client, const struct i2c_device_id *id)
{
	int i,ret;
	unsigned char tp_data[4];

   	GSL_FUN();
	ddata = kzalloc(sizeof(struct gsl_ts_data), GFP_KERNEL);
	if (!ddata) {
    	GSL_DEBUG("alloc ddata memory error");
    	return -ENOMEM;
	}
	ddata->ti = kzalloc(sizeof(union gsl_touch_info), GFP_KERNEL);
	if (!ddata->ti) {
    	GSL_DEBUG("alloc ddata->ti memory error");
    	ret = -ENOMEM;
    	goto err_malloc;
	}

	ddata->client = client;
	gsl1680_hw_init();

	mt65xx_eint_mask(CUST_EINT_TOUCH_PANEL_NUM);

	tpd_load_status = 1;

	i2c_set_clientdata(ddata->client, ddata);

	ret = gsl1680_sw_init(ddata->client);
	if (ret < 0) {
		GSL_DEBUG("gsl1680_sw_init failed");
    	goto err_xfer;
	}

	INIT_WORK(&ddata->work, gsl_report_work);
	ddata->wq = create_singlethread_workqueue(GSL_DEV_NAME);
	if (!(ddata->wq)) {
		GSL_DEBUG("can't create workqueue");
	}

	mt65xx_eint_set_sens(CUST_EINT_TOUCH_PANEL_NUM, CUST_EINT_TOUCH_PANEL_SENSITIVE);
	mt65xx_eint_set_hw_debounce(CUST_EINT_TOUCH_PANEL_NUM, CUST_EINT_TOUCH_PANEL_DEBOUNCE_CN);

	mt65xx_eint_registration(CUST_EINT_TOUCH_PANEL_NUM, CUST_EINT_TOUCH_PANEL_DEBOUNCE_EN, CUST_EINT_TOUCH_PANEL_POLARITY, tpd_eint_interrupt_handler, 1);

	mt65xx_eint_unmask(CUST_EINT_TOUCH_PANEL_NUM);

	return 0;

err_xfer:
    kfree(ddata->ti);
err_malloc:
	tpd_driver_remove(&gsl_driver);
	if (ddata)
		kfree(ddata);
	return ret;
}

/*****************************************************************************
Prototype    : gsl1680_remove
Description  : remove gsl1680 driver
Input        : struct i2c_client *client
Output       : int
Return Value : static

 *****************************************************************************/
static int __devexit gsl1680_remove(struct i2c_client *client)
{
	GSL_DEBUG("[gsl1680] TPD removed\n");
	return 0;
}

/*****************************************************************************
Prototype    : gsl1680_detect
Description  : gsl1680 driver local setup without board file
Input        : struct i2c_client *client
               int kind
               struct i2c_board_info *info
Output       : int
Return Value : static

 *****************************************************************************/
static int gsl1680_detect (struct i2c_client *client, int kind, struct i2c_board_info *info)
{
	int error;

	GSL_DEBUG("%s, %d\n", __FUNCTION__, __LINE__);
	strcpy(info->type, TPD_DEVICE);
	return 0;
}

static struct i2c_driver gsl1680_driver = {
	.driver = {
		.name = TPD_DEVICE,
		.owner = THIS_MODULE,
	},
	.probe = gsl1680_probe,
	.remove = __devexit_p(gsl1680_remove),
	.id_table = gsl_device_id,
	.detect = gsl1680_detect,
	#ifndef ANDROID_VERSION_40
	.address_data = &gsl_addr_data,
	#endif
//	.address_list = forces,
};

/*****************************************************************************
Prototype    : gsl_local_init
Description  : setup gsl1680 driver
Input        : None
Output       : None
Return Value : static

 *****************************************************************************/
static int gsl_local_init(void)
{
	int ret;
	GSL_FUN();
	boot_mode = get_boot_mode();
	printk("boot_mode == %d \n", boot_mode);

	if (boot_mode == SW_REBOOT)
		boot_mode = NORMAL_BOOT;

#ifdef TPD_HAVE_BUTTON
	GSL_DEBUG("TPD_HAVE_BUTTON");
	tpd_button_setting(TPD_KEY_COUNT, tpd_keys_local, tpd_keys_dim_local);
#endif

	ret = i2c_add_driver(&gsl1680_driver);

	if (ret < 0) {
		GSL_DEBUG("unable to i2c_add_driver");
		return -ENODEV;
	}

	if (tpd_load_status == 0) {
		GSL_DEBUG("tpd_load_status == 0, gsl1680_probe failed");
		i2c_del_driver(&gsl1680_driver);
		return -ENODEV;
	}

    	/* define in tpd_debug.h */
	tpd_type_cap = 1;
	GSL_DEBUG("end %s, %d", __FUNCTION__, __LINE__);

	return 0;
}

/*****************************************************************************
Prototype    : gsl1680_suspend
Description  : gsl chip power manage, device goto sleep
Input        : struct i2c_client *client
Output       : int
Return Value : static

 *****************************************************************************/
static  int gsl1680_suspend(struct i2c_client *client)
{
    	GSL_FUN();

	mt65xx_eint_mask(CUST_EINT_TOUCH_PANEL_NUM);
	gsl_reset_core(ddata->client);
	msleep(20);
	mt_set_gpio_out(GPIO_CTP_RST_PIN, GPIO_OUT_ZERO);
	return 0;
}

/*****************************************************************************
Prototype    : gsl1680_resume
Description  : gsl chip power manage, wake up device
Input        : struct i2c_client *client
Output       : int
Return Value : static

 *****************************************************************************/
static  int gsl1680_resume(struct i2c_client *client)
{
    	GSL_FUN();

	mt_set_gpio_out(GPIO_CTP_RST_PIN, GPIO_OUT_ONE);
	msleep(20);
	gsl_reset_core(ddata->client);
	gsl_start_core(ddata->client);
	msleep(20);
	check_mem_data(ddata->client);
	mt65xx_eint_unmask(CUST_EINT_TOUCH_PANEL_NUM);

    	return 0;
}


/*****************************************************************************
Prototype    : gsl_driver_init
Description  : driver module entry
Input        : None
Output       : int
Return Value : static

 *****************************************************************************/
static int __init gsl_driver_init(void)
{
    int ret;

	GSL_FUN();
	printk("I2C Touchscreen Driver (Built %s @ %s)\n", __DATE__, __TIME__);
	
	#ifdef ANDROID_VERSION_40
	i2c_register_board_info(I2C_NUMBER, &i2c_tpd, 1);
	#endif
	
	if(ret = tpd_driver_add(&gsl_driver) < 0)
        printk("gsl_driver init error, return num is %d", ret);

	return ret;
}


/*****************************************************************************
Prototype    : gsl_driver_exit
Description  : driver module exit
Input        : None
Output       : None
Return Value : static

 *****************************************************************************/
static void __exit gsl_driver_exit(void)
{
    	GSL_FUN();
	tpd_driver_remove(&gsl_driver);
}

module_init(gsl_driver_init);
module_exit(gsl_driver_exit);

