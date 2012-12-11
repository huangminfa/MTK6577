//For mt6573_evb
///#include <mach/mt6575_pll.h>
#include <linux/init.h>
#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/types.h>
#include <linux/device.h>
#include <linux/workqueue.h>
///#include <linux/delay.h>


#include <linux/hrtimer.h>
#include <linux/err.h>
#include <linux/platform_device.h>
#include <linux/spinlock.h>

#include <linux/jiffies.h>
#include <linux/timer.h>




#if defined(MT6575)
#include <mach/mt6575_typedefs.h>
#include <mach/mt6575_pm_ldo.h>
#include <mach/mt6575_reg_base.h>

#elif defined(MT6577)

#include <mach/mt6577_typedefs.h>
#include <mach/mt6577_pm_ldo.h>
#include <mach/mt6577_reg_base.h>

#endif

#include "cust_matv.h"
#include "cust_matv_comm.h"


int cust_matv_power_on(void)
{  
    //set GPIO94 for power
    ///int pinSetIdx = 0;//default main sensor
    u32 pinSetIdx = 0;//default main sensor	
    u32 pinSet[2][4] = 
	{
		//for main sensor 
		{	GPIO_CAMERA_CMRST_PIN,
			GPIO_CAMERA_CMRST_PIN_M_GPIO,
			GPIO_CAMERA_CMPDN_PIN,
			GPIO_CAMERA_CMPDN_PIN_M_GPIO
		},
		//for sub sensor 
		{	
			#ifdef DUAL_CAMERA_RESET_SUPPORT
			GPIO_CAMERA_CMRST1_PIN,
			GPIO_CAMERA_CMRST1_PIN_M_GPIO,
			#else
			GPIO_CAMERA_CMRST_PIN,
			GPIO_CAMERA_CMRST_PIN_M_GPIO,
			#endif
			GPIO_CAMERA_CMPDN1_PIN,
			GPIO_CAMERA_CMPDN1_PIN_M_GPIO
		}
    		   };
#endif
	MATV_LOGE("[MATV] cust_matv_power_on Start\n");

    if(TRUE != hwPowerOn(CAMERA_POWER_VCAM_D2, VOL_2800, "MT5193"))
    {
    MATV_LOGE("[CAMERA SENSOR] Fail to enable digital power\n");
    //return -EIO;
    return 0;
    }                    

    if(TRUE != hwPowerOn(CAMERA_POWER_VCAM_A, VOL_2800, "MT5193"))
    {
    MATV_LOGE("[CAMERA SENSOR] Fail to enable analog power\n");
    //return -EIO;
    return 0;
    }


    if(TRUE != hwPowerOn(CAMERA_POWER_VCAM_D, VOL_1800, "MT5193"))
    {
    MATV_LOGE("[CAMERA SENSOR] Fail to enable digital power\n");
    //return -EIO;
    return 0;
    }

	#if 0
    if(TRUE != hwPowerOn(CAMERA_POWER_VCAM_A2, VOL_2800,"MT5192"))
    {
    MATV_LOGE("[CAMERA SENSOR] Fail to enable analog power\n");
    //return -EIO;
    return 0;
    }    
#endif
    mt_set_gpio_mode(pinSet[0][0],pinSet[0][1]);
    mt_set_gpio_dir(pinSet[0][0],GPIO_DIR_OUT);
    mt_set_gpio_out(pinSet[0][0],GPIO_OUT_ZERO);
    
    mt_set_gpio_mode(pinSet[1][0],pinSet[1][1]);
    mt_set_gpio_dir(pinSet[1][0],GPIO_DIR_OUT);
    mt_set_gpio_out(pinSet[1][0],GPIO_OUT_ZERO);
    
    mt_set_gpio_mode(pinSet[0][2],pinSet[0][3]);
    mt_set_gpio_dir(pinSet[0][2],GPIO_DIR_OUT);
    mt_set_gpio_out(pinSet[0][2],GPIO_OUT_ZERO);
    
    mt_set_gpio_mode(pinSet[1][2],pinSet[1][3]);
    mt_set_gpio_dir(pinSet[1][2],GPIO_DIR_OUT);
    mt_set_gpio_out(pinSet[1][2],GPIO_OUT_ZERO);
    
#if 0
    mdelay(10);
    mt_set_gpio_out(pinSet[0][0],GPIO_OUT_ONE);
    
    mt_set_gpio_out(pinSet[1][0],GPIO_OUT_ONE);
    
    mdelay(1);
    //PDN pin
    mt_set_gpio_mode(pinSet[0][2],pinSet[0][3]);
    mt_set_gpio_dir(pinSet[0][2],GPIO_DIR_OUT);
    mt_set_gpio_out(pinSet[0][2],GPIO_OUT_ONE);
    
    mt_set_gpio_mode(pinSet[1][2],pinSet[1][3]);
    mt_set_gpio_dir(pinSet[1][2],GPIO_DIR_OUT);
    mt_set_gpio_out(pinSet[1][2],GPIO_OUT_ONE);

    mdelay(5);
    mt_set_gpio_out(pinSet[0][0],GPIO_OUT_ZERO);
    mt_set_gpio_out(pinSet[0][2],GPIO_OUT_ZERO);
    
    mt_set_gpio_out(pinSet[1][0],GPIO_OUT_ZERO);
    mt_set_gpio_out(pinSet[1][2],GPIO_OUT_ZERO);
        
    }
#endif

    return 0;
}


int cust_matv_power_off(void)
{  
	u32 pinSet[2][4] = 
	{
		//for main sensor 
		{	GPIO_CAMERA_CMRST_PIN,
			GPIO_CAMERA_CMRST_PIN_M_GPIO,
			GPIO_CAMERA_CMPDN_PIN,
			GPIO_CAMERA_CMPDN_PIN_M_GPIO
		},
		//for sub sensor 
		{	
			#ifdef DUAL_CAMERA_RESET_SUPPORT
			GPIO_CAMERA_CMRST1_PIN,
			GPIO_CAMERA_CMRST1_PIN_M_GPIO,
			#else
			GPIO_CAMERA_CMRST_PIN,
			GPIO_CAMERA_CMRST_PIN_M_GPIO,
			#endif
			GPIO_CAMERA_CMPDN1_PIN,
			GPIO_CAMERA_CMPDN1_PIN_M_GPIO
		}
	};
	
    MATV_LOGE("[MATV] cust_matv_power_off Start\n");

    if(TRUE != hwPowerDown(CAMERA_POWER_VCAM_A,"MT5193")) 
	{
        MATV_LOGE("[MATV] Fail to OFF analog power\n");
        //return -EIO;
        return 0;
    }
	#if 0
    if(TRUE != hwPowerDown(CAMERA_POWER_VCAM_A2,"MT5193"))
    {
        MATV_LOGE("[CAMERA SENSOR] Fail to enable analog power\n");
        //return -EIO;
        return 0;
    }
	#endif
    if(TRUE != hwPowerDown(CAMERA_POWER_VCAM_D, "MT5193")) 
	{
        MATV_LOGE("[MATV] Fail to OFF digital power\n");
        //return -EIO;
        return 0;
    }
    if(TRUE != hwPowerDown(CAMERA_POWER_VCAM_D2,"MT5193"))
    {
        MATV_LOGE("[MATV] Fail to enable digital power\n");
        //return -EIO;
        return 0;
    }

    mt_set_gpio_mode(pinSet[0][0],pinSet[0][1]);
    mt_set_gpio_dir(pinSet[0][0],GPIO_DIR_OUT);
    mt_set_gpio_out(pinSet[0][0],GPIO_OUT_ZERO);
    
    mt_set_gpio_mode(pinSet[1][0],pinSet[1][1]);
    mt_set_gpio_dir(pinSet[1][0],GPIO_DIR_OUT);
    mt_set_gpio_out(pinSet[1][0],GPIO_OUT_ZERO);
    
    mt_set_gpio_mode(pinSet[0][2],pinSet[0][3]);
    mt_set_gpio_dir(pinSet[0][2],GPIO_DIR_OUT);
    mt_set_gpio_out(pinSet[0][2],GPIO_OUT_ZERO);
    
    mt_set_gpio_mode(pinSet[1][2],pinSet[1][3]);
    mt_set_gpio_dir(pinSet[1][2],GPIO_DIR_OUT);
    mt_set_gpio_out(pinSet[1][2],GPIO_OUT_ZERO);

	#ifdef GPIO_MATV_N_RST
	mt_set_gpio_mode(GPIO_MATV_N_RST, GPIO_MODE_00);
    mt_set_gpio_dir(GPIO_MATV_N_RST, GPIO_DIR_OUT);
    mt_set_gpio_out(GPIO_MATV_N_RST, GPIO_OUT_ZERO);
	#endif
	
    return 0;
}

int cust_matv_gpio_on(void)
{
	MATV_LOGE("[MATV] mt5193 cust_matv_gpio_on Start\n");
#if 0//def GPIO_MATV_I2S_DAT_PIN    
    mt_set_gpio_mode(GPIO_MATV_I2S_DAT_PIN, GPIO_MATV_I2S_DAT_PIN_M_I2S0_DAT);
    mt_set_gpio_mode(GPIO_MATV_I2S_WS_PIN, GPIO_MATV_I2S_WS_PIN_M_I2S0_WS);
    mt_set_gpio_mode(GPIO_MATV_I2S_CK_PIN, GPIO_MATV_I2S_CK_PIN_M_I2S0_CK);
#endif

#if 0
    mt_set_gpio_mode(GPIO_I2S1_DAT_PIN, GPIO_MODE_00);
    mt_set_gpio_dir(GPIO_I2S1_DAT_PIN,GPIO_DIR_OUT);
    mt_set_gpio_out(GPIO_I2S1_DAT_PIN,GPIO_OUT_ZERO);
    mt_set_gpio_mode(GPIO_I2S1_WS_PIN, GPIO_MODE_00);
    mt_set_gpio_dir(GPIO_I2S1_WS_PIN,GPIO_DIR_OUT);
    mt_set_gpio_out(GPIO_I2S1_WS_PIN,GPIO_OUT_ZERO);
    mt_set_gpio_mode(GPIO_I2S1_CK_PIN, GPIO_MODE_00);
    mt_set_gpio_dir(GPIO_I2S1_CK_PIN,GPIO_DIR_OUT);
    mt_set_gpio_out(GPIO_I2S1_CK_PIN,GPIO_OUT_ZERO);
#endif
#if 1//def GPIO_I2S1_DAT_PIN
    mt_set_gpio_mode(GPIO_I2S1_DAT_PIN, GPIO_MODE_00);
    mt_set_gpio_dir(GPIO_I2S1_DAT_PIN, GPIO_DIR_IN);
    mt_set_gpio_out(GPIO_I2S1_DAT_PIN, GPIO_OUT_ZERO);
	
    mt_set_gpio_mode(GPIO_I2S1_WS_PIN, GPIO_MODE_00);
    mt_set_gpio_dir(GPIO_I2S1_WS_PIN, GPIO_DIR_IN);
    mt_set_gpio_out(GPIO_I2S1_WS_PIN, GPIO_OUT_ZERO);
	
    mt_set_gpio_mode(GPIO_I2S1_CK_PIN, GPIO_MODE_00);
    mt_set_gpio_dir(GPIO_I2S1_CK_PIN, GPIO_DIR_IN);
    mt_set_gpio_out(GPIO_I2S1_CK_PIN, GPIO_OUT_ZERO);
	
	mt_set_gpio_mode(GPIO_I2S1_CK_PIN, GPIO_MODE_04);
	mt_set_gpio_mode(GPIO_I2S1_DAT_PIN, GPIO_MODE_04);
	mt_set_gpio_mode(GPIO_I2S1_WS_PIN, GPIO_MODE_04);
#endif   

    return 1;

}

int cust_matv_gpio_off(void)
{
	MATV_LOGE("[MATV] mt5193 cust_matv_gpio_off Start\n");

#if 0//def GPIO_MATV_I2S_CK_PIN
    mt_set_gpio_mode(GPIO_MATV_I2S_CK_PIN, GPIO_MODE_00);
    mt_set_gpio_dir(GPIO_MATV_I2S_CK_PIN,GPIO_DIR_OUT);
    mt_set_gpio_out(GPIO_MATV_I2S_CK_PIN,GPIO_OUT_ZERO);
    mt_set_gpio_mode(GPIO_MATV_I2S_WS_PIN, GPIO_MODE_00);
    mt_set_gpio_dir(GPIO_MATV_I2S_WS_PIN,GPIO_DIR_OUT);
    mt_set_gpio_out(GPIO_MATV_I2S_WS_PIN,GPIO_OUT_ZERO);
    mt_set_gpio_mode(GPIO_MATV_I2S_DAT_PIN, GPIO_MODE_00);
    mt_set_gpio_dir(GPIO_MATV_I2S_DAT_PIN,GPIO_DIR_OUT);
    mt_set_gpio_out(GPIO_MATV_I2S_DAT_PIN,GPIO_OUT_ZERO);
#endif

#if 1
	mt_set_gpio_mode(GPIO_I2S1_CK_PIN, GPIO_MODE_00);
    mt_set_gpio_mode(GPIO_I2S1_WS_PIN, GPIO_MODE_00);
    mt_set_gpio_mode(GPIO_I2S1_DAT_PIN, GPIO_MODE_00);
	
    mt_set_gpio_dir(GPIO_I2S1_CK_PIN, GPIO_DIR_OUT);
    mt_set_gpio_out(GPIO_I2S1_CK_PIN, GPIO_OUT_ZERO);
	
	mt_set_gpio_dir(GPIO_I2S1_WS_PIN, GPIO_DIR_OUT);
    mt_set_gpio_out(GPIO_I2S1_WS_PIN, GPIO_OUT_ZERO);
	
	mt_set_gpio_dir(GPIO_I2S1_DAT_PIN, GPIO_DIR_OUT);
    mt_set_gpio_out(GPIO_I2S1_DAT_PIN, GPIO_OUT_ZERO);
#endif
    return 1;

}

