#include <linux/kernel.h>
#include <linux/module.h>
#include <linux/init.h>
#include <linux/platform_device.h>
#include "musbfsh_core.h"
#include "musbfsh_mt65xx.h"

// unsigned char __iomem *usb11_phy_addr = (unsigned char __iomem*)USB11_PHY_ADDR;

void mt65xx_usb11_phy_poweron(void)
{
	INFO("mt65xx_usb11_phy_poweron++\r\n");
	
	enable_pll(MT65XX_UPLL, "USB11");
	udelay(100); // PHY power stable time	

	/* reverse preloader's sin @mt6575_usbphy.c */
	USB11PHY_CLR8(U1PHTCR2+3, force_usb11_avalid | force_usb11_bvalid | force_usb11_sessend | force_usb11_vbusvalid);
	USB11PHY_CLR8(U1PHTCR2+2, RG_USB11_AVALID | RG_USB11_BVALID | RG_USB11_SESSEND | RG_USB11_VBUSVALID);
	USB11PHY_CLR8(U1PHYCR1+2, force_usb11_en_fs_ls_rcv | force_usb11_en_fs_ls_tx);
	/**************************************/
     
	USB11PHY_SET8(U1PHYCR0+1, RG_USB11_FSLS_ENBGRI);
	
	USB11PHY_SET8(U1PHTCR2+3, force_usb11_avalid | force_usb11_sessend | force_usb11_vbusvalid);
	USB11PHY_SET8(U1PHTCR2+2, RG_USB11_AVALID | RG_USB11_VBUSVALID);
	USB11PHY_CLR8(U1PHTCR2+2, RG_USB11_SESSEND);
    
	udelay(100);
}

void mt65xx_usb11_phy_savecurrent(void)
{
	INFO("mt65xx_usb11_phy_savecurrent++\r\n");

	USB11PHY_SET8(U1PHTCR2+3, force_usb11_avalid | force_usb11_sessend | force_usb11_vbusvalid);
	USB11PHY_CLR8(U1PHTCR2+2, RG_USB11_AVALID | RG_USB11_VBUSVALID);
	USB11PHY_SET8(U1PHTCR2+2, RG_USB11_SESSEND);

	USB11PHY_CLR8(U1PHYCR0+1, RG_USB11_FSLS_ENBGRI);
	
	USB11PHY_SET8(U1PHYCR1+2, force_usb11_en_fs_ls_rcv | force_usb11_en_fs_ls_tx);
	USB11PHY_CLR8(U1PHYCR1+3, RG_USB11_EN_FS_LS_RCV | RG_USB11_EN_FS_LS_TX);
	
	disable_pll(MT65XX_UPLL, "USB11");
}

void mt65xx_usb11_phy_recover(void)
{
	INFO("mt65xx_usb11_phy_recover++\r\n");
	
	enable_pll(MT65XX_UPLL, "USB11");

	USB11PHY_SET8(U1PHTCR2+3, force_usb11_avalid | force_usb11_sessend | force_usb11_vbusvalid);
	USB11PHY_SET8(U1PHTCR2+2, RG_USB11_AVALID | RG_USB11_VBUSVALID);
	USB11PHY_CLR8(U1PHTCR2+2, RG_USB11_SESSEND);
	
	USB11PHY_CLR8(U1PHYCR1+2, force_usb11_en_fs_ls_rcv | force_usb11_en_fs_ls_tx);
	USB11PHY_CLR8(U1PHYCR1+3, RG_USB11_EN_FS_LS_RCV | RG_USB11_EN_FS_LS_TX);
	
	USB11PHY_SET8(U1PHYCR0+1, RG_USB11_FSLS_ENBGRI);

	udelay(100);
}

static bool clock_enabled = false;
void mt65xx_usb11_clock_enable(bool enable)
{
	WARNING("mt65xx_usb11_clock_enable action=%d enabled=%d\n", enable, clock_enabled);
	if(enable){
		if(clock_enabled)//already enable
			return;
		else{
			enable_clock (MT65XX_PDN_PERI_USB2, "USB11");
			clock_enabled = true;
		}
	} else {
		if(!clock_enabled)//already disabled.
			return;
		else{
			disable_clock (MT65XX_PDN_PERI_USB2, "USB11");
			clock_enabled = false;
		}
	}
	return;
}

bool mt65xx_usb11_get_clock_state()
{
	return clock_enabled;
}

int mt65xx_usb11_poweron(int action){
#if 0
    static bool recover = false;
    static bool oned = false;
    INFO("mt65xx_usb11_poweron++\r\n");
    if(on){
        if(oned) {
            return 1; //already powered on
        } else{
            mt65xx_usb11_clock_enable (true);	            	
            if(!recover){
                mt65xx_usb11_phy_poweron();
                recover = true;
            } else {
                mt65xx_usb11_phy_recover();
            }
            oned = true;
        }
    } else{
        if(!oned) {
            return 1; //already power off
        } else{
            mt65xx_usb11_phy_savecurrent();
            mt65xx_usb11_clock_enable(false);
            oned = false;
        }
    }
#endif

	static bool phy_poweron = false;
	static bool phy_recovered = false; // to prevent running savecurrent/recover flow repeatedly
	WARNING("mt65xx_usb11_poweron action=%d phy_poweron=%d phy_recoverd=%d\n", action, phy_poweron, phy_recovered);
	switch (action) {
		case PLATFORM_INIT:
    		hwPowerOn(MT65XX_POWER_LDO_VUSB, VOL_3300, "USB11"); 
			mt65xx_usb11_clock_enable(true);
			mt65xx_usb11_phy_poweron();
			phy_poweron = true;
			phy_recovered = true;
			break;
			
		case PLATFORM_EXIT:
			mt65xx_usb11_phy_savecurrent();
			mt65xx_usb11_clock_enable(false);
			hwPowerDown(MT65XX_POWER_LDO_VUSB, "USB11");
			phy_poweron = false;
			phy_recovered = false;
			break;
			
		case RUNTIME_SUSPEND:
			mt65xx_usb11_clock_enable(false);
			break;
			
		case RUNTIME_RESUME:
		case PORT_RESET:
			mt65xx_usb11_clock_enable(true);
#if defined(EVDO_DT_SUPPORT)
			/* workaround for VIA, due to its remote wakeup is not under our control (call usb_autopm_get_interafce after EINT), 
			   so if remote wakeup after system suspend, resume will fail.
			   (runtime resume runs before system resume, hub port/device resume runs before platform device resume)
			 */
			if(phy_poweron && !phy_recovered){
				mt65xx_usb11_phy_recover();
				phy_recovered = true;
			}
#endif
			break;
			
		case SYSTEM_SUSPEND:
		case DRIVER_SHUTDOWN:
			if(phy_poweron && phy_recovered){
				mt65xx_usb11_phy_savecurrent();
				phy_recovered = false;
			}
			mt65xx_usb11_clock_enable(false);
			break;
			
		case SYSTEM_RESUME:
		case REMOTE_WAKEUP:
			mt65xx_usb11_clock_enable(true);
			if(phy_poweron && !phy_recovered){
				mt65xx_usb11_phy_recover();
				phy_recovered = true;
			}
			break;
			
		default:
			ERR("unkown poweron action\n");
			break;
	}
    return 0;
}

void mt65xx_usb11_vbus(struct musbfsh *musbfsh, int is_on)
{
    INFO("mt65xx_usb11_vbus++,is_on=%d\r\n",is_on);
#if 0
    static int oned = 0;  
    mt_set_gpio_mode(GPIO67,0);//should set GPIO_OTG_DRVVBUS_PIN as gpio mode. 
    mt_set_gpio_dir(GPIO67,GPIO_DIR_OUT);
    if(is_on){
        if(oned)
            return;
        else{
            mt_set_gpio_out(GPIO67,GPIO_OUT_ONE);
            oned = 1;
            }
    } else {
        if(!oned)
            return;
        else{
            mt_set_gpio_out(GPIO67,GPIO_OUT_ZERO);
            oned = 0;
            }
        }
#endif
    return;
}

int musbfsh_platform_init(struct musbfsh *musbfsh)
{
    INFO("musbfsh_platform_init++\n");
    if(!musbfsh){
        ERR("musbfsh_platform_init,error,musbfsh is NULL");
        return -1;
    }
    musbfsh->board_set_vbus = mt65xx_usb11_vbus;
    musbfsh->board_set_power = mt65xx_usb11_poweron;
    mt65xx_usb11_poweron(PLATFORM_INIT);
    return 0;
}

int musbfsh_platform_exit(struct musbfsh *musbfsh)
{
    INFO("musbfsh_platform_exit++\r\n");
	mt65xx_usb11_poweron(PLATFORM_EXIT);
	return 0;
}

void musbfsh_platform_enable(struct musbfsh *musbfsh)
{
    INFO("musbfsh_platform_enable++\r\n");
}

void musbfsh_platform_disable(struct musbfsh *musbfsh)
{
    INFO("musbfsh_platform_disable++\r\n");
}
    
void musbfsh_hcd_release (struct device *dev) 
{
    INFO("musbfsh_hcd_release++,dev = 0x%08X.\n", (uint32_t)dev);
}  
//-------------------------------------------------------------------------




