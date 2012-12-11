/******************************************************************************
Mcube Inc. (C) 2010
*****************************************************************************/

/*****************************************************************************
 *
 * Filename:
 * ---------
 *   MMA7660.h
 *
 * Project:
 * --------
 *   Mcube acceleration sensor
 *
 * Description:
 * ------------
 *   This file implements basic dirver for MTK android
 *
 * Author:
 * -------
 * Tan Liang
 ****************************************************************************/


#include <linux/interrupt.h>
#include <linux/i2c.h>
#include <linux/slab.h>
#include <linux/irq.h>
#include <linux/miscdevice.h>
#include <asm/uaccess.h>
#include <linux/delay.h>
#include <linux/input.h>
#include <linux/workqueue.h>
#include <linux/kobject.h>
#include <linux/earlysuspend.h>
#include <linux/platform_device.h>
#include <asm/atomic.h>

#include <cust_acc.h>
#include <linux/hwmsensor.h>
#include <linux/hwmsen_dev.h>
#include <linux/sensors_io.h>
#include "mma7660_mc32x0.h"
#include <linux/hwmsen_helper.h>

#ifdef MT6516
#include <mach/mt6516_devs.h>
#include <mach/mt6516_typedefs.h>
#include <mach/mt6516_gpio.h>
#include <mach/mt6516_pll.h>
#endif

#ifdef MT6573
#include <mach/mt6573_devs.h>
#include <mach/mt6573_typedefs.h>
#include <mach/mt6573_gpio.h>
#include <mach/mt6573_pll.h>
#endif

#ifdef MT6575
#include <mach/mt6575_devs.h>
#include <mach/mt6575_typedefs.h>
#include <mach/mt6575_gpio.h>
#include <mach/mt6575_pm_ldo.h>
#endif

#ifdef MT6577
#include <mach/mt6577_devs.h>
#include <mach/mt6577_typedefs.h>
#include <mach/mt6577_gpio.h>
#include <mach/mt6577_pm_ldo.h>
#endif

#define	ANDROID_VERSION_40

/*----------------------------------------------------------------------------*/
#define DEBUG 0

#define COMBO_GSENSOR

/*----------------------------------------------------------------------------*/
#define CONFIG_MMA7660_LOWPASS   /*apply low pass filter on output*/       
/*----------------------------------------------------------------------------*/
#define MMA7660_AXIS_X          0
#define MMA7660_AXIS_Y          1
#define MMA7660_AXIS_Z          2
#define MMA7660_AXES_NUM        3
#define MMA7660_DATA_LEN        6
#define MMA7660_DEV_NAME        "MMA7660"
/*----------------------------------------------------------------------------*/
#define MMA7660_2G //MMA7660_8G_14BIT

#ifdef MMA7660_8G_14BIT
#define MMA7660_Sensitivity 1024
#endif

#ifdef MMA7660_2G
#define MMA7660_Sensitivity 256
#endif

#ifdef MMA7660_4G
#define MMA7660_Sensitivity 128
#endif

#ifdef MMA7660_8G
#define MMA7660_Sensitivity 64
#endif

static const struct i2c_device_id MMA7660_i2c_id[] = {{MMA7660_DEV_NAME,0},{}};
/*the adapter id will be available in customization*/
#ifdef ANDROID_VERSION_40
#define	I2C_NUMBER	0
static struct i2c_board_info __initdata i2c_MMA7660 ={ I2C_BOARD_INFO("MMA7660", (MMA7660_I2C_SLAVE_ADDR>>1))};
#else
static unsigned short MMA7660_force[] = {0x00, MMA7660_I2C_SLAVE_ADDR, I2C_CLIENT_END, I2C_CLIENT_END};
static const unsigned short *const MMA7660_forces[] = { MMA7660_force, NULL };
static struct i2c_client_address_data MMA7660_addr_data = { .forces = MMA7660_forces,};
#endif

/*----------------------------------------------------------------------------*/
static int MMA7660_i2c_probe(struct i2c_client *client, const struct i2c_device_id *id); 
static int MMA7660_i2c_remove(struct i2c_client *client);
static int MMA7660_i2c_detect(struct i2c_client *client, int kind, struct i2c_board_info *info);

/*----------------------------------------------------------------------------*/
static int MMA7660_SetPowerMode(struct i2c_client *client, bool enable);

#define IS_MC3230 1
#define IS_MC3210 2

static unsigned char MMA7660_type;
/*------------------------------------------------------------------------------*/
typedef enum {
    MCUBE_TRC_FILTER  = 0x01,
    MCUBE_TRC_RAWDATA = 0x02,
    MCUBE_TRC_IOCTL   = 0x04,
    MCUBE_TRC_CALI	= 0X08,
    MCUBE_TRC_INFO	= 0X10,
    MCUBE_TRC_REGXYZ	= 0X20,
} MCUBE_TRC;

/*----------------------------------------------------------------------------*/
struct scale_factor{
    u8  whole;
    u8  fraction;
};
/*----------------------------------------------------------------------------*/
struct data_resolution {
    struct scale_factor scalefactor;
    int                 sensitivity;
};
/*----------------------------------------------------------------------------*/
#define C_MAX_FIR_LENGTH (32)
/*----------------------------------------------------------------------------*/
struct data_filter {
    s16 raw[C_MAX_FIR_LENGTH][MMA7660_AXES_NUM];
    int sum[MMA7660_AXES_NUM];
    int num;
    int idx;
};
/*----------------------------------------------------------------------------*/
struct MMA7660_i2c_data {
    struct i2c_client *client;
    struct acc_hw *hw;
    struct hwmsen_convert   cvt;
    
    /*misc*/
    struct data_resolution *reso;
    atomic_t                trace;
    atomic_t                suspend;
    atomic_t                selftest;
	atomic_t				filter;
    s16                     cali_sw[MMA7660_AXES_NUM+1];

    /*data*/
    s16                     offset[MMA7660_AXES_NUM+1];  /*+1: for 4-byte alignment*/
    s16                     data[MMA7660_AXES_NUM+1];

#if defined(CONFIG_MMA7660_LOWPASS)
    atomic_t                firlen;
    atomic_t                fir_en;
    struct data_filter      fir;
#endif 
    /*early suspend*/
#if defined(CONFIG_HAS_EARLYSUSPEND)
    struct early_suspend    early_drv;
#endif     
};


/*----------------------------------------------------------------------------*/
static struct i2c_driver MMA7660_i2c_driver = {
    .driver = {
        .owner          = THIS_MODULE,
        .name           = MMA7660_DEV_NAME,
    },
	.probe      		= MMA7660_i2c_probe,
	.remove    			= MMA7660_i2c_remove,
	.detect				= MMA7660_i2c_detect,
#if !defined(CONFIG_HAS_EARLYSUSPEND)    
    .suspend            = MMA7660_suspend,
    .resume             = MMA7660_resume,
#endif
	.id_table = MMA7660_i2c_id,
	#ifndef ANDROID_VERSION_40
	.address_data = &MMA7660_addr_data,
	#endif
};

/*----------------------------------------------------------------------------*/
static struct i2c_client *MMA7660_i2c_client = NULL;
static struct platform_driver MMA7660_gsensor_driver;
static struct MMA7660_i2c_data *obj_i2c_data = NULL;
static bool sensor_power = false;
static GSENSOR_VECTOR3D MMA7660_gsensor_gain, MMA7660_gsensor_offset;
static char selftestRes[10] = {0};

static GSENSOR_VECTOR3D MC32X0_gsensor_gain, MC32X0_gsensor_offset;

/*----------------------------------------------------------------------------*/
#if DEBUG
#define GSE_TAG                  "[Gsensor MMA7660] "
#define GSE_FUN(f)               printk(KERN_INFO GSE_TAG"%s\n", __FUNCTION__)
#define GSE_ERR(fmt, args...)    printk(KERN_ERR GSE_TAG"%s %d : "fmt, __FUNCTION__, __LINE__, ##args)
#define GSE_LOG(fmt, args...)    printk(KERN_INFO GSE_TAG fmt, ##args)
#else
#define GSE_TAG
#define GSE_FUN(f)               do {} while (0)
#define GSE_ERR(fmt, args...)    do {} while (0)
#define GSE_LOG(fmt, args...)    do {} while (0)
#endif

/*----------------------------------------------------------------------------*/
static struct data_resolution MMA7660_data_resolution[] = {
 /*8 combination by {FULL_RES,RANGE}*/
    {{15, 6},  256},   /*+/-2g  in 10-bit resolution: 15.6 mg/LSB*/
    {{ 15, 6}, 64},   /*+/-2g  in 8-bit resolution:  /LSB*/         
};
/*----------------------------------------------------------------------------*/
static struct data_resolution MMA7660_offset_resolution = {{7, 8}, 256};

#if defined(COMBO_GSENSOR)
typedef enum
{
	E_UNKNOWN = 0,
	E_MMA7660 = 1,
	E_MC32X0,	
} GSENSOR_TYPE;

GSENSOR_TYPE gSensorType = E_UNKNOWN;

#define CONFIG_MC32X0_LOWPASS   /*apply low pass filter on output*/       

/*----------------------------------------------------------------------------*/
#define MC32X0_AXIS_X          0
#define MC32X0_AXIS_Y          1
#define MC32X0_AXIS_Z          2
#define MC32X0_AXES_NUM        3
#define MC32X0_DATA_LEN        6
#define MC32X0_DEV_NAME        "MC32X0"
/*----------------------------------------------------------------------------*/
#define MC32X0_2G //MC32X0_8G_14BIT

#ifdef MC32X0_8G_14BIT
#define MC32X0_Sensitivity 1024
#endif

#ifdef MC32X0_2G
#define MC32X0_Sensitivity 256
#endif

#ifdef MC32X0_4G
#define MC32X0_Sensitivity 128
#endif

#ifdef MC32X0_8G
#define MC32X0_Sensitivity 64
#endif

#if 0
static const struct i2c_device_id mc32x0_i2c_id[] = {{MC32X0_DEV_NAME,0},{}};
/*the adapter id will be available in customization*/
static unsigned short mc32x0_force[] = {0x00, MC32X0_I2C_SLAVE_ADDR, I2C_CLIENT_END, I2C_CLIENT_END};
static const unsigned short *const mc32x0_forces[] = { mc32x0_force, NULL };
static struct i2c_client_address_data mc32x0_addr_data = { .forces = mc32x0_forces,};


/*----------------------------------------------------------------------------*/
static int mc32x0_i2c_probe(struct i2c_client *client, const struct i2c_device_id *id); 
static int mc32x0_i2c_remove(struct i2c_client *client);
static int mc32x0_i2c_detect(struct i2c_client *client, int kind, struct i2c_board_info *info);
#endif

/*----------------------------------------------------------------------------*/
static int hwmsen_read_byte_sr(struct i2c_client *client, u8 addr, u8 *data);
static int MC32X0_SetPowerMode(struct i2c_client *client, bool enable);

static unsigned char mc32x0_type;
/*------------------------------------------------------------------------------*/
#if 0
#define IS_MC3230 1
#define IS_MC3210 2

typedef enum {
    MCUBE_TRC_FILTER  = 0x01,
    MCUBE_TRC_RAWDATA = 0x02,
    MCUBE_TRC_IOCTL   = 0x04,
    MCUBE_TRC_CALI	= 0X08,
    MCUBE_TRC_INFO	= 0X10,
    MCUBE_TRC_REGXYZ	= 0X20,
} MCUBE_TRC;



/*----------------------------------------------------------------------------*/
struct scale_factor{
    u8  whole;
    u8  fraction;
};
/*----------------------------------------------------------------------------*/
struct data_resolution {
    struct scale_factor scalefactor;
    int                 sensitivity;
};
/*----------------------------------------------------------------------------*/
#define C_MAX_FIR_LENGTH (32)
/*----------------------------------------------------------------------------*/
struct data_filter {
    s16 raw[C_MAX_FIR_LENGTH][MC32X0_AXES_NUM];
    int sum[MC32X0_AXES_NUM];
    int num;
    int idx;
};
/*----------------------------------------------------------------------------*/
struct mc32x0_i2c_data {
    struct i2c_client *client;
    struct acc_hw *hw;
    struct hwmsen_convert   cvt;
    
    /*misc*/
    struct data_resolution *reso;
    atomic_t                trace;
    atomic_t                suspend;
    atomic_t                selftest;
	atomic_t				filter;
    s16                     cali_sw[MC32X0_AXES_NUM+1];

    /*data*/
    s16                     offset[MC32X0_AXES_NUM+1];  /*+1: for 4-byte alignment*/
    s16                     data[MC32X0_AXES_NUM+1];

#if defined(CONFIG_MC32X0_LOWPASS)
    atomic_t                firlen;
    atomic_t                fir_en;
    struct data_filter      fir;
#endif 
    /*early suspend*/
#if defined(CONFIG_HAS_EARLYSUSPEND)
    struct early_suspend    early_drv;
#endif     
};
/*----------------------------------------------------------------------------*/
static struct i2c_driver mc32x0_i2c_driver = {
    .driver = {
        .owner          = THIS_MODULE,
        .name           = MC32X0_DEV_NAME,
    },
	.probe      		= mc32x0_i2c_probe,
	.remove    			= mc32x0_i2c_remove,
	.detect				= mc32x0_i2c_detect,
#if !defined(CONFIG_HAS_EARLYSUSPEND)    
    .suspend            = mc32x0_suspend,
    .resume             = mc32x0_resume,
#endif
	.id_table = mc32x0_i2c_id,
	.address_data = &mc32x0_addr_data,
};

/*----------------------------------------------------------------------------*/
static struct i2c_client *mc32x0_i2c_client = NULL;
static struct platform_driver mc32x0_gsensor_driver;
static struct mc32x0_i2c_data *obj_i2c_data = NULL;
static bool sensor_power = false;
static GSENSOR_VECTOR3D gsensor_gain, gsensor_offset;
static char selftestRes[10] = {0};



/*----------------------------------------------------------------------------*/
#if 1
#define GSE_TAG                  "[Gsensor MC32X0] "
#define GSE_FUN(f)               printk(KERN_INFO GSE_TAG"%s\n", __FUNCTION__)
#define GSE_ERR(fmt, args...)    printk(KERN_ERR GSE_TAG"%s %d : "fmt, __FUNCTION__, __LINE__, ##args)
#define GSE_LOG(fmt, args...)    printk(KERN_INFO GSE_TAG fmt, ##args)
#else
#define GSE_TAG
#define GSE_FUN(f)               do {} while (0)
#define GSE_ERR(fmt, args...)    do {} while (0)
#define GSE_LOG(fmt, args...)    do {} while (0)
#endif
/*----------------------------------------------------------------------------*/
static struct data_resolution mc32x0_data_resolution[] = {
 /*8 combination by {FULL_RES,RANGE}*/
    {{15, 6},  256},   /*+/-2g  in 10-bit resolution: 15.6 mg/LSB*/
    {{ 15, 6}, 64},   /*+/-2g  in 8-bit resolution:  /LSB*/         
};
/*----------------------------------------------------------------------------*/
static struct data_resolution mc32x0_offset_resolution = {{7, 8}, 256};



static int hwmsen_read_byte_sr(struct i2c_client *client, u8 addr, u8 *data)
{
   u8 buf;
    int ret = 0;
	
    client->addr = client->addr& I2C_MASK_FLAG | I2C_WR_FLAG |I2C_RS_FLAG;
    buf = addr;
	ret = i2c_master_send(client, (const char*)&buf, 1<<8 | 1);
    //ret = i2c_master_send(client, (const char*)&buf, 1);
    if (ret < 0) {
        HWM_ERR("send command error!!\n");
        return -EFAULT;
    }

    *data = buf;
	client->addr = client->addr& I2C_MASK_FLAG;
    return 0;
}

static void dumpReg(struct i2c_client *client)
{
  int i=0;
  u8 addr = 0x00;
  u8 regdata=0;
  for(i=0; i<49 ; i++)
  {
    //dump all
    hwmsen_read_byte_sr(client,addr,&regdata);
	HWM_LOG("Reg addr=%x regdata=%x\n",addr,regdata);
	addr++;
	if(addr ==01)
		addr=addr+0x06;
	if(addr==0x09)
		addr++;
	if(addr==0x0A)
		addr++;
  }
}

static int hwmsen_read_block_sr(struct i2c_client *client, u8 addr, u8 *data)
{
   u8 buf[10];
    int ret = 0;
	memset(buf, 0, sizeof(u8)*10); 
	
    client->addr = client->addr& I2C_MASK_FLAG | I2C_WR_FLAG |I2C_RS_FLAG;
    buf[0] = addr;
	ret = i2c_master_send(client, (const char*)&buf, 6<<8 | 1);
    //ret = i2c_master_send(client, (const char*)&buf, 1);
    if (ret < 0) {
        HWM_ERR("send command error!!\n");
        return -EFAULT;
    }

    *data = buf;
	client->addr = client->addr& I2C_MASK_FLAG;
    return 0;
}
#endif

static void MC32X0_power(struct acc_hw *hw, unsigned int on) 
{
	static unsigned int power_on = 0;

	if(hw->power_id != MT65XX_POWER_NONE)		// have externel LDO
	{        
		GSE_LOG("power %s\n", on ? "on" : "off");
		if(power_on == on)	// power status not change
		{
			GSE_LOG("ignore power control: %d\n", on);
		}
		else if(on)	// power on
		{
			if(!hwPowerOn(hw->power_id, hw->power_vol, "MC32X0"))
			{
				GSE_ERR("power on fails!!\n");
			}
		}
		else	// power off
		{
			if (!hwPowerDown(hw->power_id, "MC32X0"))
			{
				GSE_ERR("power off fail!!\n");
			}			  
		}
	}
	power_on = on;    
}
/*----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------*/
static int MC32X0_ReadData(struct i2c_client *client, s16 data[MC32X0_AXES_NUM])
{
	//struct mc32x0_i2c_data *priv = i2c_get_clientdata(client);  
	struct MMA7660_i2c_data *priv = i2c_get_clientdata(client);   
	//u8 uData;
	u8 buf[MC32X0_DATA_LEN] = {0};
	u8 databuf[0x40];
	int err = 0;
	int i;

	if(NULL == client)
	{
		err = -EINVAL;
	}
	else		
	{
		
        if ((err = hwmsen_read_block(client, MC32X0_XOUT_EX_L_REG, buf, MC32X0_DATA_LEN))) 
        {
    	   GSE_ERR("error: %d\n", err);
    	   return err;
        }
	


		if ( mc32x0_type == IS_MC3230 )
		{
			if ((err = hwmsen_read_block(client, 0, buf, 3))) 
			{
				GSE_ERR("error: %d\n", err);
				return err;
			}


			data[MC32X0_AXIS_X] = (s8)buf[0];
			data[MC32X0_AXIS_Y] = (s8)buf[1];
			data[MC32X0_AXIS_Z] = (s8)buf[2];	   


			GSE_LOG("fwq read MC32X0_data =%d %d %d in %s \n",data[MC32X0_AXIS_X],data[MC32X0_AXIS_Y],data[MC32X0_AXIS_Z],__FUNCTION__);

			if(atomic_read(&priv->trace) & MCUBE_TRC_REGXYZ)
			{
				GSE_LOG("raw from reg(SR) [%08X %08X %08X] => [%5d %5d %5d]\n", data[MC32X0_AXIS_X], data[MC32X0_AXIS_Y], data[MC32X0_AXIS_Z],
						data[MC32X0_AXIS_X], data[MC32X0_AXIS_Y], data[MC32X0_AXIS_Z]);
			}
		}
		else if ( mc32x0_type == IS_MC3210 )
		{
			if ((err = hwmsen_read_block(client, MC32X0_XOUT_EX_L_REG, buf, MC32X0_DATA_LEN))) 
			{
				GSE_ERR("error: %d\n", err);
				return err;
			}


			data[MC32X0_AXIS_X] = ((s16)(buf[0]))|((s16)(buf[1])<<8);
			data[MC32X0_AXIS_Y] = ((s16)(buf[2]))|((s16)(buf[3])<<8);
			data[MC32X0_AXIS_Z] = ((s16)(buf[4]))|((s16)(buf[5])<<8);	   


			GSE_LOG("fwq read MC32X0_data =%d %d %d in %s \n",data[MC32X0_AXIS_X],data[MC32X0_AXIS_Y],data[MC32X0_AXIS_Z],__FUNCTION__);

			if(atomic_read(&priv->trace) & MCUBE_TRC_REGXYZ)
			{
				GSE_LOG("raw from reg(SR) [%08X %08X %08X] => [%5d %5d %5d]\n", data[MC32X0_AXIS_X], data[MC32X0_AXIS_Y], data[MC32X0_AXIS_Z],
						data[MC32X0_AXIS_X], data[MC32X0_AXIS_Y], data[MC32X0_AXIS_Z]);
			}
		}

     
	}
	return err;
}
/*----------------------------------------------------------------------------*/
static int MC32X0_ReadOffset(struct i2c_client *client, s16 ofs[MC32X0_AXES_NUM])
{    
	int err;
	u8 off_data[6];
	

	if ( mc32x0_type == IS_MC3210 )
	{
		if ((err = hwmsen_read_block(client, MC32X0_XOUT_EX_L_REG, off_data, MC32X0_DATA_LEN))) 
    		{
    			GSE_ERR("error: %d\n", err);
    			return err;
    		}
		ofs[MC32X0_AXIS_X] = ((s16)(off_data[0]))|((s16)(off_data[1])<<8);
		ofs[MC32X0_AXIS_Y] = ((s16)(off_data[2]))|((s16)(off_data[3])<<8);
		ofs[MC32X0_AXIS_Z] = ((s16)(off_data[4]))|((s16)(off_data[5])<<8);
	}
	else if (mc32x0_type == IS_MC3230) 
	{
		if ((err = hwmsen_read_block(client, 0, off_data, 3))) 
    		{
    			GSE_ERR("error: %d\n", err);
    			return err;
    		}
		ofs[MC32X0_AXIS_X] = (s8)off_data[0];
		ofs[MC32X0_AXIS_Y] = (s8)off_data[1];
		ofs[MC32X0_AXIS_Z] = (s8)off_data[2];			
	}

	GSE_LOG("MC32X0_ReadOffset %d %d %d \n",ofs[MC32X0_AXIS_X] ,ofs[MC32X0_AXIS_Y],ofs[MC32X0_AXIS_Z]);

    return 0;  
}
/*----------------------------------------------------------------------------*/
static int MC32X0_ResetCalibration(struct i2c_client *client)
{
	//struct mc32x0_i2c_data *obj = i2c_get_clientdata(client);	
	struct MMA7660_i2c_data *obj = i2c_get_clientdata(client);	  

	memset(obj->cali_sw, 0x00, sizeof(obj->cali_sw));
	return 0;  

}
/*----------------------------------------------------------------------------*/
static int MC32X0_ReadCalibration(struct i2c_client *client, int dat[MC32X0_AXES_NUM])
{
	
    //struct mc32x0_i2c_data *obj = i2c_get_clientdata(client);
    struct MMA7660_i2c_data *obj = i2c_get_clientdata(client);	
    int err;
    int mul;
	
    if ((err = MC32X0_ReadOffset(client, obj->offset))) {
        GSE_ERR("read offset fail, %d\n", err);
        return err;
    }    
    
    dat[MC32X0_AXIS_X] = obj->offset[MC32X0_AXIS_X];
    dat[MC32X0_AXIS_Y] = obj->offset[MC32X0_AXIS_Y];
    dat[MC32X0_AXIS_Z] = obj->offset[MC32X0_AXIS_Z];  
	GSE_LOG("MC32X0_ReadCalibration %d %d %d \n",dat[obj->cvt.map[MC32X0_AXIS_X]] ,dat[obj->cvt.map[MC32X0_AXIS_Y]],dat[obj->cvt.map[MC32X0_AXIS_Z]]);
                                      
    return 0;
}
/*----------------------------------------------------------------------------*/
static int MC32X0_ReadCalibrationEx(struct i2c_client *client, int act[MC32X0_AXES_NUM], int raw[MC32X0_AXES_NUM])
{      
    GSE_LOG("Sensor MC32X0_ReadCalibration!\n");  
    return 0;
}
/*----------------------------------------------------------------------------*/
static int MC32X0_WriteCalibration(struct i2c_client *client, int dat[MC32X0_AXES_NUM])
{
	GSE_LOG("Sensor MC32X0_WriteCalibration!\n");
    return 0;
}
/*----------------------------------------------------------------------------*/
static int MC32X0_CheckDeviceID(struct i2c_client *client)
{
	u8 databuf[2];    
	int res = 0;

	memset(databuf, 0, sizeof(u8)*2);    
	databuf[0] = MC32X0_REG_DEVID;    

	res = i2c_master_send(client, databuf, 0x1);
	if(res <= 0)
	{
		goto exit_MC32X0_CheckDeviceID;
	}
	
	udelay(500);

	databuf[0] = 0x0;        
	res = i2c_master_recv(client, databuf, 0x01);
	if(res <= 0)
	{
		goto exit_MC32X0_CheckDeviceID;
	}
	
	/*
	if(databuf[0]!=MC32X0_FIXED_DEVID)
	{
		GSE_ERR("MC32X0_CheckDeviceID %x fail!\n ", databuf[0]);
		return MC32X0_ERR_IDENTIFICATION;
	}
	else
	{
		GSE_LOG("MC32X0_CheckDeviceID %x pass!\n ", databuf[0]);
	}
	*/

exit_MC32X0_CheckDeviceID:
	if (res <= 0)
	{
		return MC32X0_ERR_I2C;
	}
	GSE_LOG("MC32X0_CheckDeviceID: chip_id = 0x%x \n ", databuf[0]);
	
	return databuf[0];//MC32X0_SUCCESS;
}

/*----------------------------------------------------------------------------*/
//normal
//High resolution
//low noise low power
//low power

/*---------------------------------------------------------------------------*/
static int MC32X0_SetPowerMode(struct i2c_client *client, bool enable)
{
	u8 databuf[2];    
	int res = 0;
	u8 addr = MC32X0_Mode_Feature_REG;
	//struct mc32x0_i2c_data *obj = i2c_get_clientdata(client);
	struct MMA7660_i2c_data *obj = i2c_get_clientdata(client);	
	
	if(enable == sensor_power)
	{
		GSE_LOG("Sensor power status need not to be set again!!!\n");
		return MC32X0_SUCCESS;
	}

	if(hwmsen_read_byte_sr(client, addr, databuf))
	{
		GSE_ERR("read power ctl register err!\n");
		return MC32X0_ERR_I2C;
	}
	GSE_LOG("set power read MC32X0_Mode_Feature_REG =%x\n",databuf[0]);

	
	if(enable){
		databuf[1] = 0x01; 		// 0x41;
		databuf[0] = MC32X0_Mode_Feature_REG;
		res = i2c_master_send(client, databuf, 0x2);
	}
	else{
		databuf[1] = 0x03;		// 0x43: No external pull-up resistor;  0x03:  Interrupt pin is open drain and requires an external pull-up to AVDD.
		databuf[0] = MC32X0_Mode_Feature_REG;
		res = i2c_master_send(client, databuf, 0x2);
	}

	
	if(res <= 0)
	{
		GSE_LOG("fwq set power mode failed!\n");
		return MC32X0_ERR_I2C;
	}
	else if(atomic_read(&obj->trace) & MCUBE_TRC_INFO)
	{
		GSE_LOG("fwq set power mode ok %d!\n", databuf[1]);
	}

	sensor_power = enable;
	return MC32X0_SUCCESS;    
}
/*----------------------------------------------------------------------------*/


static int MC32X0_SetBWRate(struct i2c_client *client, u8 bwrate)
{
	u8 databuf[10];    
	int res = 0;

	if(hwmsen_read_byte_sr(client, MC32X0_RANGE_Control_REG, databuf))
	{
		GSE_ERR("read power ctl register err!\n");
		return MC32X0_ERR_I2C;
	}

	databuf[0] &= ~0x0c;//clear original  data rate 

	if(bwrate == MC32X0_2G_LSB_G)	
	databuf[0] = databuf[0]; //set data rate
	else if(bwrate == MC32X0_4G_LSB_G)
	databuf[0] |= 0x04;
	else if(bwrate == MC32X0_8G_LSB_G)
	databuf[0] |= 0x08;	
	databuf[1]= databuf[0];
	databuf[0]= MC32X0_RANGE_Control_REG;
	
	res = i2c_master_send(client, databuf, 0x2);

	if(res <= 0)
	{
		return MC32X0_ERR_I2C;
	}	
	return MC32X0_SUCCESS;    
}


static int MC32X0_Init(struct i2c_client *client, int reset_cali)
{
	//struct mc32x0_i2c_data *obj = i2c_get_clientdata(client);
	struct MMA7660_i2c_data *obj = i2c_get_clientdata(client);	
	int res = 0;
	u8 uData = 0;
	u8 databuf[2];
	
	GSE_FUN(f);
    GSE_LOG("mc32x0 addr %x!\n",client->addr);

	res = MC32X0_CheckDeviceID(client); 
	/*
	if(res != MC32X0_SUCCESS)
	{
		//return res;
	}	
	*/
	if (MC32X0_FIXED_DEVID == res)
	{
		// MC3230
		
	}
	else
	{
		// MMA7660
		/*
		obj_i2c_data->hw->direction = 5;
	
		if(res = hwmsen_get_convert(obj_i2c_data->hw->direction, &obj_i2c_data->cvt))
		{
			GSE_ERR("invalid direction: %d\n", obj_i2c_data->hw->direction);
		}
		*/
	}
	GSE_LOG("MC32X0_CheckDeviceID ok \n");
	
	databuf[1] = 0x43;
	databuf[0] = MC32X0_Mode_Feature_REG;
	res = i2c_master_send(client, databuf, 0x2);
	GSE_LOG("MC32X0: id = (%x, %x)\n", databuf[0], databuf[1]);
	
	if( databuf[0]=0x19 )
	{
		mc32x0_type = IS_MC3230;
	}
	else if ( databuf[0]=0x90 )
	{
		mc32x0_type = IS_MC3210;
	}	
	/*
	else
	{
		return MC32X0_ERR_IDENTIFICATION;
	}
	*/

	databuf[1] = 0x00;
	databuf[0] = MC32X0_Sample_Rate_REG;
	res = i2c_master_send(client, databuf, 0x2);

	databuf[1] = 0x00;
	databuf[0] = MC32X0_Tap_Detection_Enable_REG;
	res = i2c_master_send(client, databuf, 0x2);

	databuf[1] = 0x00;
	databuf[0] = MC32X0_Interrupt_Enable_REG;
	res = i2c_master_send(client, databuf, 0x2);

	if ( mc32x0_type == IS_MC3230 )
	{
		databuf[1] = 0x32;
	}
	else if ( mc32x0_type == IS_MC3210 )
	{
		databuf[1] = 0x33;
	}
	/*
	else
	{
		return MC32X0_ERR_IDENTIFICATION;
	}
	*/

	databuf[0] = MC32X0_RANGE_Control_REG;
	res = i2c_master_send(client, databuf, 0x2);

	if ( mc32x0_type == IS_MC3230 )
	{
		MC32X0_gsensor_gain.x = MC32X0_gsensor_gain.y = MC32X0_gsensor_gain.z = 86;
	}
	else if ( mc32x0_type == IS_MC3210 )
	{
		MC32X0_gsensor_gain.x = MC32X0_gsensor_gain.y = MC32X0_gsensor_gain.z = 256;
	}

	
	databuf[1] = 0x41;
	databuf[0] = MC32X0_Mode_Feature_REG;
	res = i2c_master_send(client, databuf, 0x2);	
	
	
    GSE_LOG("fwq mc32x0 Init OK\n");
	return MC32X0_SUCCESS;
}
/*----------------------------------------------------------------------------*/
static int MC32X0_ReadChipInfo(struct i2c_client *client, char *buf, int bufsize)
{
	u8 databuf[10];    

	memset(databuf, 0, sizeof(u8)*10);

	if((NULL == buf)||(bufsize<=30))
	{
		return -1;
	}
	
	if(NULL == client)
	{
		*buf = 0;
		return -2;
	}

	sprintf(buf, "MC32X0 Chip");
	return 0;
}
/*----------------------------------------------------------------------------*/
static int MC32X0_ReadSensorData(struct i2c_client *client, char *buf, int bufsize)
{
	//struct mc32x0_i2c_data *obj = (struct mc32x0_i2c_data*)i2c_get_clientdata(client);
	struct MMA7660_i2c_data *obj = i2c_get_clientdata(client);	
	u8 databuf[20];
	int acc[MC32X0_AXES_NUM];
	int temp[MC32X0_AXES_NUM];
	int res = 0;
	memset(databuf, 0, sizeof(u8)*10);

	if(NULL == buf)
	{
		return -1;
	}
	if(NULL == client)
	{
		*buf = 0;
		return -2;
	}

	if(sensor_power == false)
	{
		res = MC32X0_SetPowerMode(client, true);
		if(res)
		{
			GSE_ERR("Power on mc32x0 error %d!\n", res);
		}
	}

	if(res = MC32X0_ReadData(client, obj->data))
	{        
		GSE_ERR("I2C error: ret value=%d", res);
		return -3;
	}
	else
	{
		GSE_LOG("Mapped gsensor data: %d, %d, %d!\n", obj->data[MC32X0_AXIS_X], obj->data[MC32X0_AXIS_Y], obj->data[MC32X0_AXIS_Z]);

		//Out put the mg
		GSE_LOG("MC32X0_ReadSensorData rawdata: %d, %d, %d!\n", obj->data[MC32X0_AXIS_X], obj->data[MC32X0_AXIS_Y], obj->data[MC32X0_AXIS_Z]);
	
		acc[(obj->cvt.map[MC32X0_AXIS_X])] = obj->cvt.sign[MC32X0_AXIS_X] * obj->data[MC32X0_AXIS_X];
		acc[(obj->cvt.map[MC32X0_AXIS_Y])] = obj->cvt.sign[MC32X0_AXIS_Y] * obj->data[MC32X0_AXIS_Y];
		acc[(obj->cvt.map[MC32X0_AXIS_Z])] = obj->cvt.sign[MC32X0_AXIS_Z] * obj->data[MC32X0_AXIS_Z];

		GSE_LOG("MC32X0_ReadSensorData mapdata: %d, %d, %d!\n", acc[MC32X0_AXIS_X], acc[MC32X0_AXIS_Y], acc[MC32X0_AXIS_Z]);
		
		acc[MC32X0_AXIS_X] = (acc[MC32X0_AXIS_X]*GRAVITY_EARTH_1000/MC32X0_gsensor_gain.x);
		acc[MC32X0_AXIS_Y] = (acc[MC32X0_AXIS_Y]*GRAVITY_EARTH_1000/MC32X0_gsensor_gain.y);
		acc[MC32X0_AXIS_Z] = (acc[MC32X0_AXIS_Z]*GRAVITY_EARTH_1000/MC32X0_gsensor_gain.z);	
		
		GSE_LOG("MC32X0_ReadSensorData mapdata1: %d, %d, %d!\n", acc[MC32X0_AXIS_X], acc[MC32X0_AXIS_Y], acc[MC32X0_AXIS_Z]);
		
		acc[MC32X0_AXIS_X] += obj->cali_sw[MC32X0_AXIS_X];
		acc[MC32X0_AXIS_Y] += obj->cali_sw[MC32X0_AXIS_Y];
		acc[MC32X0_AXIS_Z] += obj->cali_sw[MC32X0_AXIS_Z];

		GSE_LOG("MC32X0_ReadSensorData mapdata2: %d, %d, %d!\n", acc[MC32X0_AXIS_X], acc[MC32X0_AXIS_Y], acc[MC32X0_AXIS_Z]);

		sprintf(buf, "%04x %04x %04x", acc[MC32X0_AXIS_X], acc[MC32X0_AXIS_Y], acc[MC32X0_AXIS_Z]);
		//GSE_LOG("gsensor data: %s!\n", buf);
		if(atomic_read(&obj->trace) & MCUBE_TRC_IOCTL)
		{
			GSE_LOG("gsensor data: %s!\n", buf);
			GSE_LOG("gsensor raw data: %d %d %d!\n", acc[obj->cvt.map[MC32X0_AXIS_X]],acc[obj->cvt.map[MC32X0_AXIS_Y]],acc[obj->cvt.map[MC32X0_AXIS_Z]]);
			GSE_LOG("gsensor data:  sensitivity x=%d \n",MC32X0_gsensor_gain.z);
			 
		}
	}
	
	return 0;
}
/*----------------------------------------------------------------------------*/
static int MC32X0_ReadRawData(struct i2c_client *client, char *buf)
{
	//struct mc32x0_i2c_data *obj = (struct mc32x0_i2c_data*)i2c_get_clientdata(client);
	struct MMA7660_i2c_data *obj = i2c_get_clientdata(client);	
	int res = 0;

	if (!buf || !client)
	{
		return EINVAL;
	}

	if(sensor_power == false)
	{
		res = MC32X0_SetPowerMode(client, true);
		if(res)
		{
			GSE_ERR("Power on mc32x0 error %d!\n", res);
		}
	}
	
	if(res = MC32X0_ReadData(client, buf))
	{        
		GSE_ERR("I2C error: ret value=%d", res);
		return EIO;
	}
	else
	{
		sprintf(buf, "%04x %04x %04x", buf[MC32X0_AXIS_X], 
			buf[MC32X0_AXIS_Y], buf[MC32X0_AXIS_Z]);
	
	}
	
	return 0;
}
/*----------------------------------------------------------------------------*/
static int MC32X0_InitSelfTest(struct i2c_client *client)
{
	return MC32X0_SUCCESS;
}
/*----------------------------------------------------------------------------*/
static int MC32X0_JudgeTestResult(struct i2c_client *client, s32 prv[MC32X0_AXES_NUM], s32 nxt[MC32X0_AXES_NUM])
{
    return 0;
}
#endif


static int hwmsen_read_byte_sr(struct i2c_client *client, u8 addr, u8 *data)
{
   u8 buf;
    int ret = 0;
	
    client->addr = client->addr& I2C_MASK_FLAG | I2C_WR_FLAG |I2C_RS_FLAG;
    buf = addr;
	ret = i2c_master_send(client, (const char*)&buf, 1<<8 | 1);
    //ret = i2c_master_send(client, (const char*)&buf, 1);
    if (ret < 0) {
        HWM_ERR("send command error!!\n");
        return -EFAULT;
    }

    *data = buf;
	client->addr = client->addr& I2C_MASK_FLAG;
    return 0;
}

static void dumpReg(struct i2c_client *client)
{
  int i=0;
  u8 addr = 0x00;
  u8 regdata=0;
  for(i=0; i<49 ; i++)
  {
    //dump all
    hwmsen_read_byte_sr(client,addr,&regdata);
	HWM_LOG("Reg addr=%x regdata=%x\n",addr,regdata);
	addr++;
	if(addr ==01)
		addr=addr+0x06;
	if(addr==0x09)
		addr++;
	if(addr==0x0A)
		addr++;
  }
}

static int hwmsen_read_block_sr(struct i2c_client *client, u8 addr, u8 *data)
{
   u8 buf[10];
    int ret = 0;
	memset(buf, 0, sizeof(u8)*10); 
	
    client->addr = client->addr& I2C_MASK_FLAG | I2C_WR_FLAG |I2C_RS_FLAG;
    buf[0] = addr;
	ret = i2c_master_send(client, (const char*)&buf, 6<<8 | 1);
    //ret = i2c_master_send(client, (const char*)&buf, 1);
    if (ret < 0) {
        HWM_ERR("send command error!!\n");
        return -EFAULT;
    }

    *data = buf;
	client->addr = client->addr& I2C_MASK_FLAG;
    return 0;
}

static void MMA7660_power(struct acc_hw *hw, unsigned int on) 
{
	static unsigned int power_on = 0;

	if(hw->power_id != MT65XX_POWER_NONE)		// have externel LDO
	{        
		GSE_LOG("power %s\n", on ? "on" : "off");
		if(power_on == on)	// power status not change
		{
			GSE_LOG("ignore power control: %d\n", on);
		}
		else if(on)	// power on
		{
			if(!hwPowerOn(hw->power_id, hw->power_vol, "MMA7660"))
			{
				GSE_ERR("power on fails!!\n");
			}
		}
		else	// power off
		{
			if (!hwPowerDown(hw->power_id, "MMA7660"))
			{
				GSE_ERR("power off fail!!\n");
			}			  
		}
	}
	power_on = on;    
}
/*----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------*/
static int MMA7660_ReadData(struct i2c_client *client, s16 data[MMA7660_AXES_NUM])
{
	struct MMA7660_i2c_data *priv = i2c_get_clientdata(client);        
	//u8 uData;
	u8 buf[MMA7660_DATA_LEN] = {0};
	u8 databuf[0x40];
	int err = 0;
	int i;

	if(NULL == client)
	{
		err = -EINVAL;
	}
	else
	{

		if ((err = hwmsen_read_block(client, MMA7660_XOUT_EX_L_REG, buf, MMA7660_DATA_LEN))) 
		{
			GSE_ERR("error: %d\n", err);
			return err;
		}

		data[MMA7660_AXIS_X] = 0;
		data[MMA7660_AXIS_Y] = 0;
		data[MMA7660_AXIS_Z] = 0;
		for(i=0;i<10;i++)
		{
			do
			{
				if ((err = hwmsen_read_block(client, 0, buf, 3))) 
				{
					GSE_ERR("error: %d\n", err);
					return err;
				}
			}while(buf[0]&0x40==1 || buf[1]&0x40==1 || buf[2]&0x40==1);
			data[MMA7660_AXIS_X] += ((s8)(buf[0]<<2))>>2;
			data[MMA7660_AXIS_Y] += ((s8)(buf[1]<<2))>>2;
			data[MMA7660_AXIS_Z] += ((s8)(buf[2]<<2))>>2;
			mdelay(10);
		}
		data[MMA7660_AXIS_X] /= i;
		data[MMA7660_AXIS_Y] /= i;
		data[MMA7660_AXIS_Z] /= i;

			GSE_LOG("fwq read MMA7660_data =%d %d %d in %s \n",data[MMA7660_AXIS_X],data[MMA7660_AXIS_Y],data[MMA7660_AXIS_Z],__FUNCTION__);

		if(atomic_read(&priv->trace) & MCUBE_TRC_REGXYZ)
		{
			GSE_LOG("raw from reg(SR) [%08X %08X %08X] => [%5d %5d %5d]\n", data[MMA7660_AXIS_X], data[MMA7660_AXIS_Y], data[MMA7660_AXIS_Z],
					data[MMA7660_AXIS_X], data[MMA7660_AXIS_Y], data[MMA7660_AXIS_Z]);
		}
	}
	return err;
}
/*----------------------------------------------------------------------------*/
static int MMA7660_ReadOffset(struct i2c_client *client, s16 ofs[MMA7660_AXES_NUM])
{    
	int err;
	u8 off_data[6];
	
		if ((err = hwmsen_read_block(client, 0, off_data, 3))) 
    		{
    			GSE_ERR("error: %d\n", err);
    			return err;
    		}
		ofs[MMA7660_AXIS_X] = (s8)off_data[0];
		ofs[MMA7660_AXIS_Y] = (s8)off_data[1];
		ofs[MMA7660_AXIS_Z] = (s8)off_data[2];			

	GSE_LOG("MMA7660_ReadOffset %d %d %d \n",ofs[MMA7660_AXIS_X] ,ofs[MMA7660_AXIS_Y],ofs[MMA7660_AXIS_Z]);

    return 0;  
}
/*----------------------------------------------------------------------------*/
static int MMA7660_ResetCalibration(struct i2c_client *client)
{
	struct MMA7660_i2c_data *obj = i2c_get_clientdata(client);	

	memset(obj->cali_sw, 0x00, sizeof(obj->cali_sw));
	return 0;  

}
/*----------------------------------------------------------------------------*/
static int MMA7660_ReadCalibration(struct i2c_client *client, int dat[MMA7660_AXES_NUM])
{
	
    struct MMA7660_i2c_data *obj = i2c_get_clientdata(client);
    int err;
    int mul;
	
    if ((err = MMA7660_ReadOffset(client, obj->offset))) {
        GSE_ERR("read offset fail, %d\n", err);
        return err;
    }    
    
    dat[MMA7660_AXIS_X] = obj->offset[MMA7660_AXIS_X];
    dat[MMA7660_AXIS_Y] = obj->offset[MMA7660_AXIS_Y];
    dat[MMA7660_AXIS_Z] = obj->offset[MMA7660_AXIS_Z];  
	GSE_LOG("MMA7660_ReadCalibration %d %d %d \n",dat[obj->cvt.map[MMA7660_AXIS_X]] ,dat[obj->cvt.map[MMA7660_AXIS_Y]],dat[obj->cvt.map[MMA7660_AXIS_Z]]);
                                      
    return 0;
}
/*----------------------------------------------------------------------------*/
static int MMA7660_ReadCalibrationEx(struct i2c_client *client, int act[MMA7660_AXES_NUM], int raw[MMA7660_AXES_NUM])
{      
    GSE_LOG("Sensor MMA7660_ReadCalibration!\n");  
    return 0;
}
/*----------------------------------------------------------------------------*/
static int MMA7660_WriteCalibration(struct i2c_client *client, int dat[MMA7660_AXES_NUM])
{
	GSE_LOG("Sensor MMA7660_WriteCalibration!\n");
    return 0;
}
/*----------------------------------------------------------------------------*/

// return value: chip id
static int MMA7660_CheckDeviceID(struct i2c_client *client)
{
	u8 databuf[2];    
	int res = 0;

	memset(databuf, 0, sizeof(u8)*2);    
	databuf[0] = MC32X0_REG_DEVID;	// MMA7660_REG_DEVID;    

	res = i2c_master_send(client, databuf, 0x1);
	if(res <= 0)
	{
		goto exit_MMA7660_CheckDeviceID;
	}
	
	udelay(500);

	databuf[0] = 0x0;        
	res = i2c_master_recv(client, databuf, 0x01);
	if(res <= 0)
	{
		goto exit_MMA7660_CheckDeviceID;
	}
	
	/*
	//if(databuf[0]!=MMA7660_FIXED_DEVID)
	if(databuf[0]!=MMA7660_FIXED_DEVID)
	{
		GSE_ERR("MMA7660_CheckDeviceID %x fail!\n ", databuf[0]);
		return MMA7660_ERR_IDENTIFICATION;
	}
	else
	{
		GSE_LOG("MMA7660_CheckDeviceID %x pass!\n ", databuf[0]);
	}
	*/

exit_MMA7660_CheckDeviceID:
	if (res <= 0)
	{
		return MMA7660_ERR_I2C;
	}

	GSE_LOG("MMA7660_CheckDeviceID: chip_id = 0x%x \n ", databuf[0]);
	
	return databuf[0]; // MMA7660_SUCCESS;
}
/*----------------------------------------------------------------------------*/
//normal
//High resolution
//low noise low power
//low power

/*---------------------------------------------------------------------------*/
static int MMA7660_SetPowerMode(struct i2c_client *client, bool enable)
{
	u8 databuf[2];    
	int res = 0;
	u8 addr = MMA7660_Mode_Feature_REG;
	struct MMA7660_i2c_data *obj = i2c_get_clientdata(client);
	
	
	if(enable == sensor_power)
	{
		GSE_LOG("Sensor power status need not to be set again!!!\n");
		return MMA7660_SUCCESS;
	}

	if(hwmsen_read_byte_sr(client, addr, databuf))
	{
		GSE_ERR("read power ctl register err!\n");
		return MMA7660_ERR_I2C;
	}
	GSE_LOG("set power read MMA7660_Mode_Feature_REG =%x\n",databuf[0]);

	
	if(enable){
		databuf[1] = 0x01;		// 0x41;
		databuf[0] = MMA7660_Mode_Feature_REG;
		res = i2c_master_send(client, databuf, 0x2);
	}
	else{
		databuf[1] = 0x00;		// 0x43;
		databuf[0] = MMA7660_Mode_Feature_REG;
		res = i2c_master_send(client, databuf, 0x2);
	}

	
	if(res <= 0)
	{
		GSE_LOG("fwq set power mode failed!\n");
		return MMA7660_ERR_I2C;
	}
	else if(atomic_read(&obj->trace) & MCUBE_TRC_INFO)
	{
		GSE_LOG("fwq set power mode ok %d!\n", databuf[1]);
	}

	sensor_power = enable;
	return MMA7660_SUCCESS;    
}
/*----------------------------------------------------------------------------*/


static int MMA7660_SetBWRate(struct i2c_client *client, u8 bwrate)
{
	u8 databuf[10];    
	int res = 0;

	if(hwmsen_read_byte_sr(client, MMA7660_RANGE_Control_REG, databuf))
	{
		GSE_ERR("read power ctl register err!\n");
		return MMA7660_ERR_I2C;
	}

	databuf[0] &= ~0x0c;//clear original  data rate 

	if(bwrate == MMA7660_2G_LSB_G)	
	databuf[0] = databuf[0]; //set data rate
	else if(bwrate == MMA7660_4G_LSB_G)
	databuf[0] |= 0x04;
	else if(bwrate == MMA7660_8G_LSB_G)
	databuf[0] |= 0x08;	
	databuf[1]= databuf[0];
	databuf[0]= MMA7660_RANGE_Control_REG;
	
	res = i2c_master_send(client, databuf, 0x2);

	if(res <= 0)
	{
		return MMA7660_ERR_I2C;
	}	
	return MMA7660_SUCCESS;    
}


static int MMA7660_Init(struct i2c_client *client, int reset_cali)
{
	struct MMA7660_i2c_data *obj = i2c_get_clientdata(client);
	int res = 0;
	u8 uData = 0;
	u8 databuf[2];

	GSE_FUN(f);
    GSE_LOG("MMA7660 addr %x!\n",client->addr);

	
	
	
	databuf[1] = 0x43;
	databuf[0] = MMA7660_Mode_Feature_REG;
	res = i2c_master_send(client, databuf, 0x2);

	//GSE_LOG("MMA7660: id = (%x, %x)\n", databuf[0], databuf[1]);
	/*
	if( databuf[0]=0x19 )
	{
		MMA7660_type = IS_MC3230;
	}
	else if ( databuf[0]=0x90 )
	{
		MMA7660_type = IS_MC3210;
	}
	else
	{
		return MMA7660_ERR_IDENTIFICATION;
	}
	*/

	databuf[1] = 0x00;
	databuf[0] = MMA7660_Sample_Rate_REG;
	res = i2c_master_send(client, databuf, 0x2);

	databuf[1] = 0x00;
	databuf[0] = MMA7660_Tap_Detection_Enable_REG;
	res = i2c_master_send(client, databuf, 0x2);

	databuf[1] = 0x00;
	databuf[0] = MMA7660_Interrupt_Enable_REG;
	res = i2c_master_send(client, databuf, 0x2);

	databuf[1] = 0x32;
	databuf[0] = MMA7660_RANGE_Control_REG;
	res = i2c_master_send(client, databuf, 0x2);

	MMA7660_gsensor_gain.x = MMA7660_gsensor_gain.y = MMA7660_gsensor_gain.z = 21;

	
	databuf[1] = 0x41;
	databuf[0] = MMA7660_Mode_Feature_REG;
	res = i2c_master_send(client, databuf, 0x2);	
	
	
    GSE_LOG("fwq MMA7660 Init OK\n");
	return MMA7660_SUCCESS;
}
/*----------------------------------------------------------------------------*/
static int MMA7660_ReadChipInfo(struct i2c_client *client, char *buf, int bufsize)
{
	u8 databuf[10];    

	memset(databuf, 0, sizeof(u8)*10);

	if((NULL == buf)||(bufsize<=30))
	{
		return -1;
	}
	
	if(NULL == client)
	{
		*buf = 0;
		return -2;
	}

	sprintf(buf, "MMA7660 Chip");
	return 0;
}
/*----------------------------------------------------------------------------*/
static int MMA7660_ReadSensorData(struct i2c_client *client, char *buf, int bufsize)
{
	struct MMA7660_i2c_data *obj = (struct MMA7660_i2c_data*)i2c_get_clientdata(client);
	u8 databuf[20];
	int acc[MMA7660_AXES_NUM];
	int temp[MMA7660_AXES_NUM];
	int res = 0;
	memset(databuf, 0, sizeof(u8)*10);

	if(NULL == buf)
	{
		return -1;
	}
	if(NULL == client)
	{
		*buf = 0;
		return -2;
	}

	if(sensor_power == false)
	{
		res = MMA7660_SetPowerMode(client, true);
		if(res)
		{
			GSE_ERR("Power on MMA7660 error %d!\n", res);
		}
	}

	if(res = MMA7660_ReadData(client, obj->data))
	{        
		GSE_ERR("I2C error: ret value=%d", res);
		return -3;
	}
	else
	{
		GSE_LOG("Mapped gsensor data: %d, %d, %d!\n", obj->data[MMA7660_AXIS_X], obj->data[MMA7660_AXIS_Y], obj->data[MMA7660_AXIS_Z]);

		//Out put the mg
		GSE_LOG("MMA7660_ReadSensorData rawdata: %d, %d, %d!\n", obj->data[MMA7660_AXIS_X], obj->data[MMA7660_AXIS_Y], obj->data[MMA7660_AXIS_Z]);
	
		acc[(obj->cvt.map[MMA7660_AXIS_X])] = obj->cvt.sign[MMA7660_AXIS_X] * obj->data[MMA7660_AXIS_X];
		acc[(obj->cvt.map[MMA7660_AXIS_Y])] = obj->cvt.sign[MMA7660_AXIS_Y] * obj->data[MMA7660_AXIS_Y];
		acc[(obj->cvt.map[MMA7660_AXIS_Z])] = obj->cvt.sign[MMA7660_AXIS_Z] * obj->data[MMA7660_AXIS_Z];

		GSE_LOG("MMA7660_ReadSensorData mapdata: %d, %d, %d!\n", acc[MMA7660_AXIS_X], acc[MMA7660_AXIS_Y], acc[MMA7660_AXIS_Z]);
		
		acc[MMA7660_AXIS_X] = (acc[MMA7660_AXIS_X]*GRAVITY_EARTH_1000/MMA7660_gsensor_gain.x);
		acc[MMA7660_AXIS_Y] = (acc[MMA7660_AXIS_Y]*GRAVITY_EARTH_1000/MMA7660_gsensor_gain.y);
		acc[MMA7660_AXIS_Z] = (acc[MMA7660_AXIS_Z]*GRAVITY_EARTH_1000/MMA7660_gsensor_gain.z);	
		
		GSE_LOG("MMA7660_ReadSensorData mapdata1: %d, %d, %d!\n", acc[MMA7660_AXIS_X], acc[MMA7660_AXIS_Y], acc[MMA7660_AXIS_Z]);
		
		acc[MMA7660_AXIS_X] += obj->cali_sw[MMA7660_AXIS_X];
		acc[MMA7660_AXIS_Y] += obj->cali_sw[MMA7660_AXIS_Y];
		acc[MMA7660_AXIS_Z] += obj->cali_sw[MMA7660_AXIS_Z];

		GSE_LOG("MMA7660_ReadSensorData mapdata2: %d, %d, %d!\n", acc[MMA7660_AXIS_X], acc[MMA7660_AXIS_Y], acc[MMA7660_AXIS_Z]);

		sprintf(buf, "%04x %04x %04x", acc[MMA7660_AXIS_X], acc[MMA7660_AXIS_Y], acc[MMA7660_AXIS_Z]);
		//GSE_LOG("gsensor data: %s!\n", buf);
		if(atomic_read(&obj->trace) & MCUBE_TRC_IOCTL)
		{
			GSE_LOG("gsensor data: %s!\n", buf);
			GSE_LOG("gsensor raw data: %d %d %d!\n", acc[obj->cvt.map[MMA7660_AXIS_X]],acc[obj->cvt.map[MMA7660_AXIS_Y]],acc[obj->cvt.map[MMA7660_AXIS_Z]]);
			GSE_LOG("gsensor data:  sensitivity x=%d \n",MMA7660_gsensor_gain.z);
			 
		}
	}
	
	return 0;
}
/*----------------------------------------------------------------------------*/
static int MMA7660_ReadRawData(struct i2c_client *client, char *buf)
{
	struct MMA7660_i2c_data *obj = (struct MMA7660_i2c_data*)i2c_get_clientdata(client);
	int res = 0;

	if (!buf || !client)
	{
		return EINVAL;
	}

	if(sensor_power == false)
	{
		res = MMA7660_SetPowerMode(client, true);
		if(res)
		{
			GSE_ERR("Power on MMA7660 error %d!\n", res);
		}
	}
	
	if(res = MMA7660_ReadData(client, buf))
	{        
		GSE_ERR("I2C error: ret value=%d", res);
		return EIO;
	}
	else
	{
		sprintf(buf, "%04x %04x %04x", buf[MMA7660_AXIS_X], 
			buf[MMA7660_AXIS_Y], buf[MMA7660_AXIS_Z]);
	
	}
	
	return 0;
}
/*----------------------------------------------------------------------------*/
static int MMA7660_InitSelfTest(struct i2c_client *client)
{
	return MMA7660_SUCCESS;
}
/*----------------------------------------------------------------------------*/
static int MMA7660_JudgeTestResult(struct i2c_client *client, s32 prv[MMA7660_AXES_NUM], s32 nxt[MMA7660_AXES_NUM])
{
    return 0;
}
/*----------------------------------------------------------------------------*/
static ssize_t show_chipinfo_value(struct device_driver *ddri, char *buf)
{
    GSE_LOG("fwq show_chipinfo_value \n");
	struct i2c_client *client = MMA7660_i2c_client;
	char strbuf[MMA7660_BUFSIZE];
	if(NULL == client)
	{
		GSE_ERR("i2c client is null!!\n");
		return 0;
	}
	
	MMA7660_ReadChipInfo(client, strbuf, MMA7660_BUFSIZE);
	return snprintf(buf, PAGE_SIZE, "%s\n", strbuf);        
}
/*----------------------------------------------------------------------------*/
static ssize_t show_sensordata_value(struct device_driver *ddri, char *buf)
{
	struct i2c_client *client = MMA7660_i2c_client;
	char strbuf[MMA7660_BUFSIZE];
	
	if(NULL == client)
	{
		GSE_ERR("i2c client is null!!\n");
		return 0;
	}
	MMA7660_ReadSensorData(client, strbuf, MMA7660_BUFSIZE);
	return snprintf(buf, PAGE_SIZE, "%s\n", strbuf);            
}
/*----------------------------------------------------------------------------*/
static ssize_t show_cali_value(struct device_driver *ddri, char *buf)
{
    GSE_LOG("fwq show_cali_value \n");
	struct i2c_client *client = MMA7660_i2c_client;
	struct MMA7660_i2c_data *obj;

	if(NULL == client)
	{
		GSE_ERR("i2c client is null!!\n");
		return 0;
	}

	obj = i2c_get_clientdata(client);

	int err, len = 0, mul;
	int tmp[MMA7660_AXES_NUM];

	if(err = MMA7660_ReadOffset(client, obj->offset))
	{
		return -EINVAL;
	}
	else if(err = MMA7660_ReadCalibration(client, tmp))
	{
		return -EINVAL;
	}
	else
	{    
		mul = obj->reso->sensitivity/MMA7660_offset_resolution.sensitivity;
		len += snprintf(buf+len, PAGE_SIZE-len, "[HW ][%d] (%+3d, %+3d, %+3d) : (0x%02X, 0x%02X, 0x%02X)\n", mul,                        
			obj->offset[MMA7660_AXIS_X], obj->offset[MMA7660_AXIS_Y], obj->offset[MMA7660_AXIS_Z],
			obj->offset[MMA7660_AXIS_X], obj->offset[MMA7660_AXIS_Y], obj->offset[MMA7660_AXIS_Z]);
		len += snprintf(buf+len, PAGE_SIZE-len, "[SW ][%d] (%+3d, %+3d, %+3d)\n", 1, 
			obj->cali_sw[MMA7660_AXIS_X], obj->cali_sw[MMA7660_AXIS_Y], obj->cali_sw[MMA7660_AXIS_Z]);

		len += snprintf(buf+len, PAGE_SIZE-len, "[ALL]    (%+3d, %+3d, %+3d) : (%+3d, %+3d, %+3d)\n", 
			obj->offset[MMA7660_AXIS_X]*mul + obj->cali_sw[MMA7660_AXIS_X],
			obj->offset[MMA7660_AXIS_Y]*mul + obj->cali_sw[MMA7660_AXIS_Y],
			obj->offset[MMA7660_AXIS_Z]*mul + obj->cali_sw[MMA7660_AXIS_Z],
			tmp[MMA7660_AXIS_X], tmp[MMA7660_AXIS_Y], tmp[MMA7660_AXIS_Z]);
		
		return len;
    }
}
/*----------------------------------------------------------------------------*/
static ssize_t store_cali_value(struct device_driver *ddri, char *buf, size_t count)
{
	struct i2c_client *client = MMA7660_i2c_client;  
	int err, x, y, z;
	int dat[MMA7660_AXES_NUM];

	if(!strncmp(buf, "rst", 3))
	{
		if(err = MMA7660_ResetCalibration(client))
		{
			GSE_ERR("reset offset err = %d\n", err);
		}	
	}
	else if(3 == sscanf(buf, "0x%02X 0x%02X 0x%02X", &x, &y, &z))
	{
		dat[MMA7660_AXIS_X] = x;
		dat[MMA7660_AXIS_Y] = y;
		dat[MMA7660_AXIS_Z] = z;
		if(err = MMA7660_WriteCalibration(client, dat))
		{
			GSE_ERR("write calibration err = %d\n", err);
		}		
	}
	else
	{
		GSE_ERR("invalid format\n");
	}
	
	return count;
}
/*----------------------------------------------------------------------------*/
static ssize_t show_selftest_value(struct device_driver *ddri, char *buf)
{

	return 0;
}
/*----------------------------------------------------------------------------*/
static ssize_t store_selftest_value(struct device_driver *ddri, char *buf, size_t count)
{   /*write anything to this register will trigger the process*/
	return 0;
}
/*----------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------*/
static ssize_t show_firlen_value(struct device_driver *ddri, char *buf)
{
    GSE_LOG("fwq show_firlen_value \n");

	return snprintf(buf, PAGE_SIZE, "not support\n");
}
/*----------------------------------------------------------------------------*/
static ssize_t store_firlen_value(struct device_driver *ddri, char *buf, size_t count)
{
    GSE_LOG("fwq store_firlen_value \n");
	return count;
}
/*----------------------------------------------------------------------------*/
static ssize_t show_trace_value(struct device_driver *ddri, char *buf)
{
    GSE_LOG("fwq show_trace_value \n");
	ssize_t res;
	struct MMA7660_i2c_data *obj = obj_i2c_data;
	if (obj == NULL)
	{
		GSE_ERR("i2c_data obj is null!!\n");
		return 0;
	}
	
	res = snprintf(buf, PAGE_SIZE, "0x%04X\n", atomic_read(&obj->trace));     
	return res;    
}
/*----------------------------------------------------------------------------*/
static ssize_t store_trace_value(struct device_driver *ddri, char *buf, size_t count)
{
    GSE_LOG("fwq store_trace_value \n");
	struct MMA7660_i2c_data *obj = obj_i2c_data;
	int trace;
	if (obj == NULL)
	{
		GSE_ERR("i2c_data obj is null!!\n");
		return 0;
	}
	
	if(1 == sscanf(buf, "0x%x", &trace))
	{
		atomic_set(&obj->trace, trace);
	}	
	else
	{
		GSE_ERR("invalid content: '%s', length = %d\n", buf, count);
	}
	
	return count;    
}
/*----------------------------------------------------------------------------*/
static ssize_t show_status_value(struct device_driver *ddri, char *buf)
{
    GSE_LOG("fwq show_status_value \n");
	ssize_t len = 0;    
	struct MMA7660_i2c_data *obj = obj_i2c_data;
	if (obj == NULL)
	{
		GSE_ERR("i2c_data obj is null!!\n");
		return 0;
	}	
	
	if(obj->hw)
	{
		len += snprintf(buf+len, PAGE_SIZE-len, "CUST: %d %d (%d %d)\n", 
	            obj->hw->i2c_num, obj->hw->direction, obj->hw->power_id, obj->hw->power_vol);   
	}
	else
	{
		len += snprintf(buf+len, PAGE_SIZE-len, "CUST: NULL\n");
	}
	return len;    
}

static ssize_t show_power_status(struct device_driver *ddri, char *buf)
{
	
	ssize_t res;
	u8 uData;
	struct MMA7660_i2c_data *obj = obj_i2c_data;
	if (obj == NULL)
	{
		GSE_ERR("i2c_data obj is null!!\n");
		return 0;
	}
	hwmsen_read_byte(obj->client, MMA7660_Mode_Feature_REG, &uData);
	
	res = snprintf(buf, PAGE_SIZE, "0x%04X\n", uData);     
	return res;   
}


/*----------------------------------------------------------------------------*/
static DRIVER_ATTR(chipinfo,             S_IRUGO, show_chipinfo_value,      NULL);
static DRIVER_ATTR(sensordata,           S_IRUGO, show_sensordata_value,    NULL);
static DRIVER_ATTR(cali,       S_IWUSR | S_IRUGO, show_cali_value,          store_cali_value);
static DRIVER_ATTR(selftest,       S_IWUSR | S_IRUGO, show_selftest_value,          store_selftest_value);
static DRIVER_ATTR(firlen,     S_IWUSR | S_IRUGO, show_firlen_value,        store_firlen_value);
static DRIVER_ATTR(trace,      S_IWUSR | S_IRUGO, show_trace_value,         store_trace_value);
static DRIVER_ATTR(status,               S_IRUGO, show_status_value,        NULL);
static DRIVER_ATTR(power,               S_IRUGO, show_power_status,        NULL);

/*----------------------------------------------------------------------------*/
static struct driver_attribute *MMA7660_attr_list[] = {
	&driver_attr_chipinfo,     /*chip information*/
	&driver_attr_sensordata,   /*dump sensor data*/
	&driver_attr_cali,         /*show calibration data*/
	&driver_attr_selftest,         /*self test demo*/
	&driver_attr_firlen,       /*filter length: 0: disable, others: enable*/
	&driver_attr_trace,        /*trace log*/
	&driver_attr_status,
	&driver_attr_power, 
};
/*----------------------------------------------------------------------------*/
static int MMA7660_create_attr(struct device_driver *driver) 
{
	int idx, err = 0;
	int num = (int)(sizeof(MMA7660_attr_list)/sizeof(MMA7660_attr_list[0]));
	if (driver == NULL)
	{
		return -EINVAL;
	}

	for(idx = 0; idx < num; idx++)
	{
		if(err = driver_create_file(driver, MMA7660_attr_list[idx]))
		{            
			GSE_ERR("driver_create_file (%s) = %d\n", MMA7660_attr_list[idx]->attr.name, err);
			break;
		}
	}    
	return err;
}
/*----------------------------------------------------------------------------*/
static int MMA7660_delete_attr(struct device_driver *driver)
{
	int idx ,err = 0;
	int num = (int)(sizeof(MMA7660_attr_list)/sizeof(MMA7660_attr_list[0]));

	if(driver == NULL)
	{
		return -EINVAL;
	}
	

	for(idx = 0; idx < num; idx++)
	{
		driver_remove_file(driver, MMA7660_attr_list[idx]);
	}
	

	return err;
}

/*----------------------------------------------------------------------------*/

static int gsensor_operate(void* self, uint32_t command, void* buff_in, int size_in,
		void* buff_out, int size_out, int* actualout)
{
	int err = 0;
	int value, sample_delay;	
	struct MMA7660_i2c_data *priv = (struct MMA7660_i2c_data*)self;
	hwm_sensor_data* gsensor_data;
	char buff[MMA7660_BUFSIZE];
	
	GSE_FUN(f);
	switch (command)
	{
		case SENSOR_DELAY:
			GSE_LOG("STEP 1: fwq set delay. gSensorType = %d\n", gSensorType);
			MMA7660_CheckDeviceID(priv->client); 
			break;

		case SENSOR_ENABLE:
			GSE_LOG("STEP 2: fwq sensor enable gsensor. gSensorType = %d\n", gSensorType);
			MMA7660_CheckDeviceID(priv->client); 
			if((buff_in == NULL) || (size_in < sizeof(int)))
			{
				GSE_ERR("Enable sensor parameter error!\n");
				err = -EINVAL;
			}
			else
			{
				value = *(int *)buff_in;
				if(((value == 0) && (sensor_power == false)) ||((value == 1) && (sensor_power == true)))
				{
					GSE_LOG("Gsensor device have updated!\n");
				}
				else
				{
					if (E_MMA7660 == gSensorType)
					{
						err = MMA7660_SetPowerMode( priv->client, !sensor_power);
					}
					else if (E_MC32X0 == gSensorType)
					{
						err = MC32X0_SetPowerMode( priv->client, !sensor_power);
					}
				}
			}
			break;

		case SENSOR_GET_DATA:
			GSE_LOG("STEP 3: fwq sensor operate get data. gSensorType = %d\n", gSensorType);
			MMA7660_CheckDeviceID(priv->client); 
			if((buff_out == NULL) || (size_out< sizeof(hwm_sensor_data)))
			{
				GSE_ERR("get sensor data parameter error!\n");
				err = -EINVAL;
			}
			else if (E_MMA7660 == gSensorType)
			{
				gsensor_data = (hwm_sensor_data *)buff_out;
				MMA7660_ReadSensorData(priv->client, buff, MMA7660_BUFSIZE);
				sscanf(buff, "%x %x %x", &gsensor_data->values[0], 
					&gsensor_data->values[1], &gsensor_data->values[2]);				
				gsensor_data->status = SENSOR_STATUS_ACCURACY_MEDIUM;				
				gsensor_data->value_divide = 1000;
				GSE_LOG("X :%d,Y: %d, Z: %d\n",gsensor_data->values[0],gsensor_data->values[1],gsensor_data->values[2]);
			}
			else if (E_MC32X0 == gSensorType)
			{
				gsensor_data = (hwm_sensor_data *)buff_out;
				MC32X0_ReadSensorData(priv->client, buff, MC32X0_BUFSIZE);
				sscanf(buff, "%x %x %x", &gsensor_data->values[0], 
					&gsensor_data->values[1], &gsensor_data->values[2]);				
				gsensor_data->status = SENSOR_STATUS_ACCURACY_MEDIUM;				
				gsensor_data->value_divide = 1000;
				GSE_LOG("MC32X0: X :%d,Y: %d, Z: %d\n",gsensor_data->values[0],gsensor_data->values[1],gsensor_data->values[2]);
			}
			break;
		default:
			GSE_ERR("gsensor operate function no this parameter %d!\n", command);
			err = -1;
			break;
	}
	
	return err;
}


/****************************************************************************** 
 * Function Configuration
******************************************************************************/
static int MMA7660_open(struct inode *inode, struct file *file)
{
	file->private_data = MMA7660_i2c_client;

	if(file->private_data == NULL)
	{
		GSE_ERR("null pointer!!\n");
		return -EINVAL;
	}
	return nonseekable_open(inode, file);
}
/*----------------------------------------------------------------------------*/
static int MMA7660_release(struct inode *inode, struct file *file)
{
	file->private_data = NULL;
	return 0;
}
/*----------------------------------------------------------------------------*/
#ifdef ANDROID_VERSION_40
static int MMA7660_unlocked_ioctl(struct file *file, unsigned int cmd, unsigned long arg)
#else
static int MMA7660_ioctl(struct inode *inode, struct file *file, unsigned int cmd, unsigned long arg)
#endif
{
	struct i2c_client *client = (struct i2c_client*)file->private_data;
	struct MMA7660_i2c_data *obj = (struct MMA7660_i2c_data*)i2c_get_clientdata(client);	
	char strbuf[MMA7660_BUFSIZE];
	void __user *data;
	SENSOR_DATA sensor_data;
	int err = 0;
	int cali[3];

	//GSE_FUN(f);
	if(_IOC_DIR(cmd) & _IOC_READ)
	{
		err = !access_ok(VERIFY_WRITE, (void __user *)arg, _IOC_SIZE(cmd));
	}
	else if(_IOC_DIR(cmd) & _IOC_WRITE)
	{
		err = !access_ok(VERIFY_READ, (void __user *)arg, _IOC_SIZE(cmd));
	}

	if(err)
	{
		GSE_ERR("access error: %08X, (%2d, %2d)\n", cmd, _IOC_DIR(cmd), _IOC_SIZE(cmd));
		return -EFAULT;
	}

	switch(cmd)
	{
		case GSENSOR_IOCTL_INIT:
			GSE_LOG("fwq GSENSOR_IOCTL_INIT\n");
			if (E_MMA7660 == gSensorType)
			{
				err = MMA7660_Init(client, 0);			
			}
			else if (E_MC32X0 == gSensorType)
			{
				err = MC32X0_Init(client, 0);		
			}

			data = (void __user *) arg;
			if(data == NULL)
			{
				err = -EINVAL;
				break;	  
			}			
			
			if(copy_to_user(data, &err, sizeof(err)))
			{
				err = -EFAULT;
				break;
			}	
			break;

		case GSENSOR_IOCTL_READ_CHIPINFO:

			GSE_LOG("fwq GSENSOR_IOCTL_READ_CHIPINFO\n");
			MMA7660_CheckDeviceID(client);
			data = (void __user *) arg;
			if(data == NULL)
			{
				err = -EINVAL;
				break;	  
			}
			
			MMA7660_ReadChipInfo(client, strbuf, MMA7660_BUFSIZE);
			if(copy_to_user(data, strbuf, strlen(strbuf)+1))
			{
				err = -EFAULT;
				break;
			}				 
			break;	  

		case GSENSOR_IOCTL_READ_SENSORDATA:
			MMA7660_CheckDeviceID(client);
			data = (void __user *) arg;
			if(data == NULL)
			{
				err = -EINVAL;
				break;	  
			}
		//***************%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	
			if (E_MMA7660 == gSensorType)
			{
				MMA7660_ReadSensorData(client, strbuf, MMA7660_BUFSIZE);
			}
			else if (E_MC32X0 == gSensorType)
			{
				MC32X0_ReadSensorData(client, strbuf, MMA7660_BUFSIZE);
			}
			
			if(copy_to_user(data, strbuf, strlen(strbuf)+1))
			{
				err = -EFAULT;
				break;	  
			}				 
			break;

		case GSENSOR_IOCTL_READ_GAIN:			
			GSE_LOG("fwq GSENSOR_IOCTL_READ_GAIN\n");
			MMA7660_CheckDeviceID(client);
			data = (void __user *) arg;
			if(data == NULL)
			{
				err = -EINVAL;
				break;	  
			}			

			if (E_MMA7660 == gSensorType)
			{
				if(copy_to_user(data, &MMA7660_gsensor_gain, sizeof(GSENSOR_VECTOR3D)))
				{
					err = -EFAULT;
					break;
				}		
			}
			else if (E_MC32X0 == gSensorType)
			{
				if(copy_to_user(data, &MC32X0_gsensor_gain, sizeof(GSENSOR_VECTOR3D)))
				{
					err = -EFAULT;
					break;
				}		

			}
			break;

		case GSENSOR_IOCTL_READ_OFFSET:
			GSE_LOG("fwq GSENSOR_IOCTL_READ_OFFSET\n");
			MMA7660_CheckDeviceID(client);
			data = (void __user *) arg;
			if(data == NULL)
			{
				err = -EINVAL;
				break;	  
			}

			if (E_MMA7660 == gSensorType)
			{
				if(copy_to_user(data, &MMA7660_gsensor_offset, sizeof(GSENSOR_VECTOR3D)))
				{
					err = -EFAULT;
					break;
				}		
			}
			else if (E_MC32X0 == gSensorType)
			{
				if(copy_to_user(data, &MC32X0_gsensor_offset, sizeof(GSENSOR_VECTOR3D)))
				{
					err = -EFAULT;
					break;
				}		

			}
			break;

		case GSENSOR_IOCTL_READ_RAW_DATA:
			GSE_LOG("fwq GSENSOR_IOCTL_READ_RAW_DATA\n");
			MMA7660_CheckDeviceID(client);
			data = (void __user *) arg;
			if(data == NULL)
			{
				err = -EINVAL;
				break;	  
			}
			if (E_MMA7660 == gSensorType)
				MMA7660_ReadRawData(client, &strbuf);
			else if (E_MC32X0 == gSensorType)
				MC32X0_ReadRawData(client, &strbuf);
			
			if(copy_to_user(data, &strbuf, strlen(strbuf)+1))
			{
				err = -EFAULT;
				break;	  
			}
			break;	  

		case GSENSOR_IOCTL_SET_CALI:
			GSE_LOG("fwq GSENSOR_IOCTL_SET_CALI!!\n");
			MMA7660_CheckDeviceID(client);
			data = (void __user*)arg;
			if(data == NULL)
			{
				err = -EINVAL;
				break;	  
			}
			if(copy_from_user(&sensor_data, data, sizeof(sensor_data)))
			{
				err = -EFAULT;
				break;	  
			}
			if(atomic_read(&obj->suspend))
			{
				GSE_ERR("Perform calibration in suspend state!!\n");
				err = -EINVAL;
			}
			else
			{
				cali[MMA7660_AXIS_X] = sensor_data.x;
				cali[MMA7660_AXIS_Y] = sensor_data.y;
				cali[MMA7660_AXIS_Z] = sensor_data.z;		


				obj->cali_sw[MMA7660_AXIS_X] = cali[MMA7660_AXIS_X];
				obj->cali_sw[MMA7660_AXIS_Y] = cali[MMA7660_AXIS_Y];
				obj->cali_sw[MMA7660_AXIS_Z] = cali[MMA7660_AXIS_Z];	

				GSE_LOG("GSENSOR_IOCTL_SET_CALI %d %d %d \n",obj->cali_sw[MMA7660_AXIS_X],obj->cali_sw[MMA7660_AXIS_Y],obj->cali_sw[MMA7660_AXIS_Z]);
			}
			break;

		case GSENSOR_IOCTL_CLR_CALI:
			GSE_LOG("fwq GSENSOR_IOCTL_CLR_CALI!!\n");
			MMA7660_CheckDeviceID(client);
			err = MMA7660_ResetCalibration(client);
			break;

		case GSENSOR_IOCTL_GET_CALI:
			GSE_LOG("fwq MMA7660 GSENSOR_IOCTL_GET_CALI\n");
			MMA7660_CheckDeviceID(client);
			data = (void __user*)arg;
			if(data == NULL)
			{
				err = -EINVAL;
				break;	  
			}

			sensor_data.x = obj->cali_sw[MMA7660_AXIS_X];
			sensor_data.y = obj->cali_sw[MMA7660_AXIS_Y];
			sensor_data.z = obj->cali_sw[MMA7660_AXIS_Z];

			GSE_LOG("GSENSOR_IOCTL_GET_CALI %d %d %d \n",sensor_data.x ,sensor_data.y ,sensor_data.z );
			if(copy_to_user(data, &sensor_data, sizeof(sensor_data)))
			{
				err = -EFAULT;
				break;
			}		
			break;
			

		default:
			GSE_ERR("unknown IOCTL: 0x%08x\n", cmd);
			err = -ENOIOCTLCMD;
			break;
			
	}

	return err;
}


/*----------------------------------------------------------------------------*/
static struct file_operations MMA7660_fops = {
	.owner = THIS_MODULE,
	.open = MMA7660_open,
	.release = MMA7660_release,
	#ifdef ANDROID_VERSION_40
	.unlocked_ioctl = MMA7660_unlocked_ioctl,
	#else
	.ioctl = MMA7660_ioctl,
	#endif
};
/*----------------------------------------------------------------------------*/
static struct miscdevice MMA7660_device = {
	.minor = MISC_DYNAMIC_MINOR,
	.name = "gsensor",
	.fops = &MMA7660_fops,
};
/*----------------------------------------------------------------------------*/
#ifndef CONFIG_HAS_EARLYSUSPEND
/*----------------------------------------------------------------------------*/
static int MMA7660_suspend(struct i2c_client *client, pm_message_t msg) 
{
	struct MMA7660_i2c_data *obj = i2c_get_clientdata(client);    
	int err = 0;
	u8  dat=0;
	GSE_FUN();    

	if(msg.event == PM_EVENT_SUSPEND)
	{   
		if(obj == NULL)
		{
			GSE_ERR("null pointer!!\n");
			return -EINVAL;
		}
		
		atomic_set(&obj->suspend, 1);

		if (E_MMA7660 == gSensorType)
		{
			if ((err = MMA7660_SetPowerMode(client,false)
			{
	            GSE_ERR("write power control fail!!\n");
	            return err;
	        }     
		}
		else if (E_MC32X0 == gSensorType)
		{
			if ((err = MC32X0_SetPowerMode(client,false)
			{
	            GSE_ERR("write power control fail!!\n");
	            return err;
	        }     			
		}
		MMA7660_power(obj->hw, 0);
	}
	return err;
}
/*----------------------------------------------------------------------------*/
static int MMA7660_resume(struct i2c_client *client)
{
	struct MMA7660_i2c_data *obj = i2c_get_clientdata(client);        
	int err;
	GSE_FUN();

	if(obj == NULL)
	{
		GSE_ERR("null pointer!!\n");
		return -EINVAL;
	}

	MMA7660_power(obj->hw, 1);

	if (E_MMA7660 == gSensorType)
	{
		if(err = MMA7660_Init(client, 0))
		{
			GSE_ERR("initialize client fail!!\n");
			return err;        
		}
	}
	else if (E_MC32X0 == gSensorType)
	{
		if(err = MC32X0_Init(client, 0))
		{
			GSE_ERR("initialize client fail!!\n");
			return err;        
		}
		
	}
	atomic_set(&obj->suspend, 0);

	return 0;
}
/*----------------------------------------------------------------------------*/
#else /*CONFIG_HAS_EARLY_SUSPEND is defined*/
/*----------------------------------------------------------------------------*/
static void MMA7660_early_suspend(struct early_suspend *h) 
{
	struct MMA7660_i2c_data *obj = container_of(h, struct MMA7660_i2c_data, early_drv);   
	int err;
	GSE_FUN();    

	if(obj == NULL)
	{
		GSE_ERR("null pointer!!\n");
		return;
	}
	atomic_set(&obj->suspend, 1); 
	/*
	if(err = hwmsen_write_byte(obj->client, MMA7660_REG_POWER_CTL, 0x00))
	{
		GSE_ERR("write power control fail!!\n");
		return;
	}  
	*/
	if (E_MMA7660 == gSensorType)
	{
		if(err = MMA7660_SetPowerMode(obj->client, false))
		{
			GSE_ERR("write power control fail!!\n");
			return;
		}
	}
	else if (E_MC32X0 == gSensorType)
	{
		if(err = MC32X0_SetPowerMode(obj->client, false))
		{
			GSE_ERR("write power control fail!!\n");
			return;
		}

	}

	sensor_power = false;
	
	MMA7660_power(obj->hw, 0);
}
/*----------------------------------------------------------------------------*/
static void MMA7660_late_resume(struct early_suspend *h)
{
	struct MMA7660_i2c_data *obj = container_of(h, struct MMA7660_i2c_data, early_drv);         
	int err;
	GSE_FUN();

	if(obj == NULL)
	{
		GSE_ERR("null pointer!!\n");
		return;
	}

	MMA7660_power(obj->hw, 1);
	if (E_MMA7660 == gSensorType)
	{
		if(err = MMA7660_Init(obj->client, 0))
		{
			GSE_ERR("initialize client fail!!\n");
			return;        
		}
	}
	else if (E_MC32X0 == gSensorType)
	{
		if(err = MC32X0_Init(obj->client, 0))
		{
			GSE_ERR("initialize client fail!!\n");
			return;        
		}
	}
	atomic_set(&obj->suspend, 0);    
}
/*----------------------------------------------------------------------------*/
#endif /*CONFIG_HAS_EARLYSUSPEND*/
/*----------------------------------------------------------------------------*/
static int MMA7660_i2c_detect(struct i2c_client *client, int kind, struct i2c_board_info *info) 
{    
	strcpy(info->type, MMA7660_DEV_NAME);
	return 0;
}

/*----------------------------------------------------------------------------*/
static int MMA7660_i2c_probe(struct i2c_client *client, const struct i2c_device_id *id)
{
	struct i2c_client *new_client;
	struct MMA7660_i2c_data *obj;
	struct hwmsen_object sobj;
	int err = 0;
	GSE_FUN();

	if(!(obj = kzalloc(sizeof(*obj), GFP_KERNEL)))
	{
		err = -ENOMEM;
		goto exit;
	}
	
	memset(obj, 0, sizeof(struct MMA7660_i2c_data));

	obj->hw = get_cust_acc_hw();

	#ifdef COMBO_GSENSOR
	err = MMA7660_CheckDeviceID(client); 
	if (MC32X0_FIXED_DEVID == err)
	{
		gSensorType = E_MC32X0;
		//obj->hw->direction = 5;
	}
	else
	{
		gSensorType = E_MMA7660;
		//obj->hw->direction = 5;
	}
	#endif
	
	if(err = hwmsen_get_convert(obj->hw->direction, &obj->cvt))
	{
		GSE_ERR("invalid direction: %d\n", obj->hw->direction);
		goto exit;
	}

	obj_i2c_data = obj;
	obj->client = client;
	new_client = obj->client;
	i2c_set_clientdata(new_client,obj);
	
	atomic_set(&obj->trace, 0);
	atomic_set(&obj->suspend, 0);
	
#ifdef CONFIG_MMA7660_LOWPASS
	if(obj->hw->firlen > C_MAX_FIR_LENGTH)
	{
		atomic_set(&obj->firlen, C_MAX_FIR_LENGTH);
	}	
	else
	{
		atomic_set(&obj->firlen, obj->hw->firlen);
	}
	
	if(atomic_read(&obj->firlen) > 0)
	{
		atomic_set(&obj->fir_en, 1);
	}
	
#endif

	MMA7660_i2c_client = new_client;	

	if (E_MMA7660 == gSensorType)
	{
		if(err = MMA7660_Init(new_client, 1))
		{
			goto exit_init_failed;
		}
	}
	else if (E_MC32X0 == gSensorType)
	{
		if(err = MC32X0_Init(new_client, 1))
		{
			goto exit_init_failed;
		}
	}
	

	if(err = misc_register(&MMA7660_device))
	{
		GSE_ERR("MMA7660_device register failed\n");
		goto exit_misc_device_register_failed;
	}

	if(err = MMA7660_create_attr(&MMA7660_gsensor_driver.driver))
	{
		GSE_ERR("create attribute err = %d\n", err);
		goto exit_create_attr_failed;
	}

	sobj.self = obj;
    sobj.polling = 1;
    sobj.sensor_operate = gsensor_operate;
	if(err = hwmsen_attach(ID_ACCELEROMETER, &sobj))
	{
		GSE_ERR("attach fail = %d\n", err);
		goto exit_kfree;
	}

#ifdef CONFIG_HAS_EARLYSUSPEND
	obj->early_drv.level    = EARLY_SUSPEND_LEVEL_DISABLE_FB - 1,
	obj->early_drv.suspend  = MMA7660_early_suspend,
	obj->early_drv.resume   = MMA7660_late_resume,    
	register_early_suspend(&obj->early_drv);
#endif 

	GSE_LOG("%s: OK\n", __func__);    
	return 0;

exit_create_attr_failed:
	misc_deregister(&MMA7660_device);
exit_misc_device_register_failed:
exit_init_failed:
	//i2c_detach_client(new_client);
exit_kfree:
	kfree(obj);
exit:
	GSE_ERR("%s: err = %d\n", __func__, err);        
	return err;
}

/*----------------------------------------------------------------------------*/
static int MMA7660_i2c_remove(struct i2c_client *client)
{
	int err = 0;	
	
	if(err = MMA7660_delete_attr(&MMA7660_gsensor_driver.driver))
	{
		GSE_ERR("MMA7660_delete_attr fail: %d\n", err);
	}
	
	if(err = misc_deregister(&MMA7660_device))
	{
		GSE_ERR("misc_deregister fail: %d\n", err);
	}

	if(err = hwmsen_detach(ID_ACCELEROMETER))
	    

	MMA7660_i2c_client = NULL;
	i2c_unregister_device(client);
	kfree(i2c_get_clientdata(client));
	return 0;
}
/*----------------------------------------------------------------------------*/
static int MMA7660_probe(struct platform_device *pdev) 
{
	struct acc_hw *hw = get_cust_acc_hw();
	GSE_FUN();

	MMA7660_power(hw, 1);

	#ifndef ANDROID_VERSION_40
	MMA7660_force[0] = hw->i2c_num;
	#endif
	if(i2c_add_driver(&MMA7660_i2c_driver))
	{
		GSE_ERR("add driver error\n");
		return -1;
	}
	return 0;
}
/*----------------------------------------------------------------------------*/
static int MMA7660_remove(struct platform_device *pdev)
{
    struct acc_hw *hw = get_cust_acc_hw();

    GSE_FUN();    
    MMA7660_power(hw, 0);    
    i2c_del_driver(&MMA7660_i2c_driver);
    return 0;
}
/*----------------------------------------------------------------------------*/
static struct platform_driver MMA7660_gsensor_driver = {
	.probe      = MMA7660_probe,
	.remove     = MMA7660_remove,    
	.driver     = {
		.name  = "gsensor",
		.owner = THIS_MODULE,
	}
};

/*----------------------------------------------------------------------------*/
static int __init MMA7660_init(void)
{
	GSE_FUN();

	#ifdef ANDROID_VERSION_40
	i2c_register_board_info(I2C_NUMBER, &i2c_MMA7660, 1);
	#endif
	if(platform_driver_register(&MMA7660_gsensor_driver))
	{
		GSE_ERR("failed to register driver");
		return -ENODEV;
	}
	return 0;    
}
/*----------------------------------------------------------------------------*/
static void __exit MMA7660_exit(void)
{
	GSE_FUN();
	platform_driver_unregister(&MMA7660_gsensor_driver);
}
/*----------------------------------------------------------------------------*/
module_init(MMA7660_init);
module_exit(MMA7660_exit);
/*----------------------------------------------------------------------------*/
MODULE_DESCRIPTION("MMA7660 I2C driver");
