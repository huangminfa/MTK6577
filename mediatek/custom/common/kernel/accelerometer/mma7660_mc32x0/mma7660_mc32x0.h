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

#ifndef MMA7660_MC32X0_H
#define MMA7660_MC32X0_H 
	 
#include <linux/ioctl.h>


/* MMA7660 register address */
#define MMA7660_XOUT_REG						0x00
#define MMA7660_YOUT_REG						0x01
#define MMA7660_ZOUT_REG						0x02
#define MMA7660_Tilt_Status_REG				0x03
#define MMA7660_Sampling_Rate_Status_REG		0x04
#define MMA7660_Sleep_Count_REG				0x05
#define MMA7660_Interrupt_Enable_REG			0x06
#define MMA7660_Mode_Feature_REG				0x07
#define MMA7660_Sample_Rate_REG				0x08
#define MMA7660_Tap_Detection_Enable_REG		0x09
#define MMA7660_TAP_Dwell_Reject_REG			0x0a
#define MMA7660_DROP_Control_Register_REG	0x0b
#define MMA7660_SHAKE_Debounce_REG			0x0c
#define MMA7660_XOUT_EX_L_REG				0x0d
#define MMA7660_XOUT_EX_H_REG				0x0e
#define MMA7660_YOUT_EX_L_REG				0x0f
#define MMA7660_YOUT_EX_H_REG				0x10
#define MMA7660_ZOUT_EX_L_REG				0x11
#define MMA7660_ZOUT_EX_H_REG				0x12
#define MMA7660_RANGE_Control_REG			0x20
#define MMA7660_SHAKE_Threshold_REG			0x2B
#define MMA7660_UD_Z_TH_REG					0x2C
#define MMA7660_UD_X_TH_REG					0x2D
#define MMA7660_RL_Z_TH_REG					0x2E
#define MMA7660_RL_Y_TH_REG					0x2F
#define MMA7660_FB_Z_TH_REG					0x30
#define MMA7660_DROP_Threshold_REG			0x31
#define MMA7660_TAP_Threshold_REG			0x32



#define MMA7660_I2C_SLAVE_ADDR 		0x98


#define MMA7660_REG_DEVID			0x0F //  Must read this chip's datasheet
#define MMA7660_FIXED_DEVID		0x00 //  Must read this chip's datasheet

//#define MC32X0_REG_DEVID			0x3B 
//#define MC32X0_FIXED_DEVID		0x19	

#define MMA7660_BW_256HZ			0x80
#define MMA7660_BW_128HZ			0x00

#define MMA7660_MEASURE_MODE		0x08	
#define MMA7660_DATA_READY			0x80

#define MMA7660_FULL_RES			0x08
#define MMA7660_RANGE_2G			0x00
#define MMA7660_RANGE_4G			0x01
#define MMA7660_RANGE_8G			0x02
#define MMA7660_RANGE_16G			0x03
#define MMA7660_SELF_TEST           0x80

#define MMA7660_STREAM_MODE			0x80
#define MMA7660_SAMPLES_15			0x0F

#define MMA7660_8G_LSB_G			2
#define MMA7660_4G_LSB_G			1
#define MMA7660_2G_LSB_G			0

#define MMA7660_LEFT_JUSTIFY		0x04
#define MMA7660_RIGHT_JUSTIFY		0x00


#define MMA7660_SUCCESS						0
#define MMA7660_ERR_I2C						-1
#define MMA7660_ERR_STATUS					-3
#define MMA7660_ERR_SETUP_FAILURE			-4
#define MMA7660_ERR_GETGSENSORDATA			-5
#define MMA7660_ERR_IDENTIFICATION			-6

#define MMA7660IO						   	0x85
#define MMA7660_IOCTL_INIT                  _IO(MMA7660IO,  0x01)
#define MMA7660_IOCTL_READ_CHIPINFO         _IOR(MMA7660IO, 0x02, int)
#define MMA7660_IOCTL_READ_SENSORDATA       _IOR(MMA7660IO, 0x03, int)


#define MMA7660_BUFSIZE				256

// mc32x0.h
#define MC32X0_XOUT_REG						0x00
#define MC32X0_YOUT_REG						0x01
#define MC32X0_ZOUT_REG						0x02
#define MC32X0_Tilt_Status_REG				0x03
#define MC32X0_Sampling_Rate_Status_REG		0x04
#define MC32X0_Sleep_Count_REG				0x05
#define MC32X0_Interrupt_Enable_REG			0x06
#define MC32X0_Mode_Feature_REG				0x07
#define MC32X0_Sample_Rate_REG				0x08
#define MC32X0_Tap_Detection_Enable_REG		0x09
#define MC32X0_TAP_Dwell_Reject_REG			0x0a
#define MC32X0_DROP_Control_Register_REG	0x0b
#define MC32X0_SHAKE_Debounce_REG			0x0c
#define MC32X0_XOUT_EX_L_REG				0x0d
#define MC32X0_XOUT_EX_H_REG				0x0e
#define MC32X0_YOUT_EX_L_REG				0x0f
#define MC32X0_YOUT_EX_H_REG				0x10
#define MC32X0_ZOUT_EX_L_REG				0x11
#define MC32X0_ZOUT_EX_H_REG				0x12
#define MC32X0_RANGE_Control_REG			0x20
#define MC32X0_SHAKE_Threshold_REG			0x2B
#define MC32X0_UD_Z_TH_REG					0x2C
#define MC32X0_UD_X_TH_REG					0x2D
#define MC32X0_RL_Z_TH_REG					0x2E
#define MC32X0_RL_Y_TH_REG					0x2F
#define MC32X0_FB_Z_TH_REG					0x30
#define MC32X0_DROP_Threshold_REG			0x31
#define MC32X0_TAP_Threshold_REG			0x32

#define MC32X0_I2C_SLAVE_ADDR 		0x98

#define MC32X0_REG_DEVID			0x3B  // 0x0F //changed
#define MC32X0_FIXED_DEVID		0x19	//  The chip MC3230's ID is 0x19 when read the register 0x3b

#define MC32X0_BW_256HZ			0x80
#define MC32X0_BW_128HZ			0x00

#define MC32X0_MEASURE_MODE		0x08	
#define MC32X0_DATA_READY			0x80

#define MC32X0_FULL_RES			0x08
#define MC32X0_RANGE_2G			0x00
#define MC32X0_RANGE_4G			0x01
#define MC32X0_RANGE_8G			0x02
#define MC32X0_RANGE_16G			0x03
#define MC32X0_SELF_TEST           0x80

#define MC32X0_STREAM_MODE			0x80
#define MC32X0_SAMPLES_15			0x0F

#define MC32X0_8G_LSB_G			2
#define MC32X0_4G_LSB_G			1
#define MC32X0_2G_LSB_G			0

#define MC32X0_LEFT_JUSTIFY		0x04
#define MC32X0_RIGHT_JUSTIFY		0x00

#define MC32X0_SUCCESS						0
#define MC32X0_ERR_I2C						-1
#define MC32X0_ERR_STATUS					-3
#define MC32X0_ERR_SETUP_FAILURE			-4
#define MC32X0_ERR_GETGSENSORDATA			-5
#define MC32X0_ERR_IDENTIFICATION			-6

#define MC32X0IO						   	0x85
#define MC32X0_IOCTL_INIT                  _IO(MC32X0IO,  0x01)
#define MC32X0_IOCTL_READ_CHIPINFO         _IOR(MC32X0IO, 0x02, int)
#define MC32X0_IOCTL_READ_SENSORDATA       _IOR(MC32X0IO, 0x03, int)


#define MC32X0_BUFSIZE				256
// mc32x0.hs above

#endif


