

/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2008
*
*  BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
*  THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
*  RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON
*  AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
*  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
*  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
*  NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
*  SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
*  SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH
*  THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
*  NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S
*  SPECIFICATION OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
*
*  BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
*  LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
*  AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
*  OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY BUYER TO
*  MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
*
*  THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE
*  WITH THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF
*  LAWS PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING THEREOF AND
*  RELATED THERETO SHALL BE SETTLED BY ARBITRATION IN SAN FRANCISCO, CA, UNDER
*  THE RULES OF THE INTERNATIONAL CHAMBER OF COMMERCE (ICC).
*
*****************************************************************************/
/*****************************************************************************
 *
 * Filename:
 * ---------
 *   sensor.c
 *
 * Project:
 * --------
 *   DUMA
 *
 * Description:
 * ------------
 *   Source code of Sensor driver
 *
 *
 * Author:
 * -------
 *   PC Huang (MTK02204)
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by CC/CQ. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision:$
 * $Modtime:$
 * $Log:$
 *
 * 06 21 2011 yan.xu
 * [ALPS00051346] MT6573 Image sensor driver check in
 * .
 *
 * 10 12 2010 sean.cheng
 * [ALPS00021722] [Need Patch] [Volunteer Patch][Camera]MT6573 Camera related function
 * .rollback the lib3a for mt6573 camera related files
 *
 * 09 10 2010 jackie.su
 * [ALPS00002279] [Need Patch] [Volunteer Patch] ALPS.Wxx.xx Volunteer patch for
 * .alps dual sensor
 *
 * 09 02 2010 jackie.su
 * [ALPS00002279] [Need Patch] [Volunteer Patch] ALPS.Wxx.xx Volunteer patch for
 * .roll back dual sensor
 *
 * 07 27 2010 sean.cheng
 * [ALPS00003112] [Need Patch] [Volunteer Patch] ISP/Sensor driver modification for Customer support
 * .1. add master clock switcher 
 *  2. add master enable/disable 
 *  3. add dummy line/pixel for sensor 
 *  4. add sensor driving current setting
 *
 * 07 01 2010 sean.cheng
 * [ALPS00121215][Camera] Change color when switch low and high 
 * .Add video delay frame.
 *
 * 07 01 2010 sean.cheng
 * [ALPS00002805][Need Patch] [Volunteer Patch]10X Patch for DS269 Video Frame Rate 
 * .Change the sensor clock to let frame rate to be 30fps in vidoe mode
 *
 * 06 13 2010 sean.cheng
 * [ALPS00002514][Need Patch] [Volunteer Patch] ALPS.10X.W10.11 Volunteer patch for E1k Camera 
 * .
 * 1. Add set zoom factor and capdelay frame for YUV sensor 
 * 2. Modify e1k sensor setting
 *
 * 05 25 2010 sean.cheng
 * [ALPS00002250][Need Patch] [Volunteer Patch] ALPS.10X.W10.11 Volunteer patch for YUV video frame rate 
 * .
 * Add 15fps option for video mode
 *
 * 05 03 2010 sean.cheng
 * [ALPS00001357][Meta]CameraTool 
 * .
 * Fix SP2518 YUV sensor frame rate to 30fps in vidoe mode
 *
 * Mar 4 2010 mtk70508
 * [DUMA00154792] Sensor driver
 * 
 *
 * Mar 4 2010 mtk70508
 * [DUMA00154792] Sensor driver
 * 
 *
 * Mar 1 2010 mtk01118
 * [DUMA00025869] [Camera][YUV I/F & Query feature] check in camera code
 * 
 *
 * Feb 24 2010 mtk01118
 * [DUMA00025869] [Camera][YUV I/F & Query feature] check in camera code
 * 
 *
 * Nov 24 2009 mtk02204
 * [DUMA00015869] [Camera Driver] Modifiy camera related drivers for dual/backup sensor/lens drivers.
 * 
 *
 * Oct 29 2009 mtk02204
 * [DUMA00015869] [Camera Driver] Modifiy camera related drivers for dual/backup sensor/lens drivers.
 * 
 *
 * Oct 27 2009 mtk02204
 * [DUMA00015869] [Camera Driver] Modifiy camera related drivers for dual/backup sensor/lens drivers.
 * 
 *
 * Aug 13 2009 mtk01051
 * [DUMA00009217] [Camera Driver] CCAP First Check In
 * 
 *
 * Aug 5 2009 mtk01051
 * [DUMA00009217] [Camera Driver] CCAP First Check In
 * 
 *
 * Jul 17 2009 mtk01051
 * [DUMA00009217] [Camera Driver] CCAP First Check In
 * 
 *
 * Jul 7 2009 mtk01051
 * [DUMA00008051] [Camera Driver] Add drivers for camera high ISO binning mode.
 * Add ISO query info for Sensor
 *
 * May 18 2009 mtk01051
 * [DUMA00005921] [Camera] LED Flashlight first check in
 * 
 *
 * May 16 2009 mtk01051
 * [DUMA00005921] [Camera] LED Flashlight first check in
 * 
 *
 * May 16 2009 mtk01051
 * [DUMA00005921] [Camera] LED Flashlight first check in
 * 
 *
 * Apr 7 2009 mtk02204
 * [DUMA00004012] [Camera] Restructure and rename camera related custom folders and folder name of came
 * 
 *
 * Mar 27 2009 mtk02204
 * [DUMA00002977] [CCT] First check in of MT6516 CCT drivers
 *
 *
 * Mar 25 2009 mtk02204
 * [DUMA00111570] [Camera] The system crash after some operations
 *
 *
 * Mar 20 2009 mtk02204
 * [DUMA00002977] [CCT] First check in of MT6516 CCT drivers
 *
 *
 * Mar 2 2009 mtk02204
 * [DUMA00001084] First Check in of MT6516 multimedia drivers
 *
 *
 * Feb 24 2009 mtk02204
 * [DUMA00001084] First Check in of MT6516 multimedia drivers
 *
 *
 * Dec 27 2008 MTK01813
 * DUMA_MBJ CheckIn Files
 * created by clearfsimport
 *
 * Dec 10 2008 mtk02204
 * [DUMA00001084] First Check in of MT6516 multimedia drivers
 *
 *
 * Oct 27 2008 mtk01051
 * [DUMA00000851] Camera related drivers check in
 * Modify Copyright Header
 *
 * Oct 24 2008 mtk02204
 * [DUMA00000851] Camera related drivers check in
 *
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by CC/CQ. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
//#include <windows.h>
//#include <memory.h>
//#include <nkintr.h>
//#include <ceddk.h>
//#include <ceddk_exp.h>

//#include "kal_release.h"
//#include "i2c_exp.h"
//#include "gpio_exp.h"
//#include "msdk_exp.h"
//#include "msdk_sensor_exp.h"
//#include "msdk_isp_exp.h"
//#include "base_regs.h"
//#include "Sensor.h"
//#include "camera_sensor_para.h"
//#include "CameraCustomized.h"

#include <linux/videodev2.h>
#include <linux/i2c.h>
#include <linux/platform_device.h>
#include <linux/delay.h>
#include <linux/cdev.h>
#include <linux/uaccess.h>
#include <linux/fs.h>
#include <asm/atomic.h>
//#include <mach/mt6516_pll.h>

#include "kd_camera_hw.h"
#include "kd_imgsensor.h"
#include "kd_imgsensor_define.h"
#include "kd_imgsensor_errcode.h"
#include "kd_camera_feature.h"

#include "sp2518yuv_Sensor.h"
#include "sp2518yuv_Camera_Sensor_para.h"
#include "sp2518yuv_CameraCustomized.h"

#define SP2518YUV_DEBUG
#ifdef SP2518YUV_DEBUG
#define SENSORDB printk
#else
#define SENSORDB(x,...)
#endif

struct
{
  kal_bool    NightMode;
  kal_uint8   ZoomFactor; /* Zoom Index */
  kal_uint16  Banding;
  kal_uint32  PvShutter;
  kal_uint32  PvDummyPixels;
  kal_uint32  PvDummyLines;
  kal_uint32  CapDummyPixels;
  kal_uint32  CapDummyLines;
  kal_uint32  PvOpClk;
  kal_uint32  CapOpClk;
  
  /* Video frame rate 300 means 30.0fps. Unit Multiple 10. */
  kal_uint32  MaxFrameRate; 
  kal_uint32  MiniFrameRate; 
  /* Sensor Register backup. */
  kal_uint8   VDOCTL2; /* P0.0x11. */
  kal_uint8   ISPCTL3; /* P10.0x12. */
  kal_uint8   AECTL1;  /* P20.0x10. */
  kal_uint8   AWBCTL1; /* P22.0x10. */
  kal_uint8   Videomode;
} SP2518Status;


void SP2518_night_mode(kal_bool enable);


#if 0
#define DELAY_TIME 700
extern int iReadReg(u16 a_u2Addr , u8 * a_puBuff , u16 i2cId);
extern int iWriteReg(u16 a_u2Addr , u32 a_u4Data , u32 a_u4Bytes , u16 i2cId);
#define SP2518_write_cmos_sensor(addr, para) iWriteReg((u16) addr , (u32) para ,1, SP2518_WRITE_ID)
#define SP2518_write_cmos_sensor_2(addr, para, bytes) iWriteReg((u16) addr , (u32) para ,bytes, SP2518_WRITE_ID)
kal_uint16 SP2518_read_cmos_sensor(kal_uint32 addr)
{
kal_uint16 get_byte=0;
    iReadReg((u16) addr ,(u8*)&get_byte,SP2518_WRITE_ID);
    return get_byte;
}
#else
extern int iReadRegI2C(u8 *a_pSendData , u16 a_sizeSendData, u8 * a_pRecvData, u16 a_sizeRecvData, u16 i2cId);
extern int iWriteRegI2C(u8 *a_pSendData , u16 a_sizeSendData, u16 i2cId);

static void SP2518_write_cmos_sensor(kal_uint8 addr, kal_uint8 para)
{
kal_uint8 out_buff[2];

    out_buff[0] = addr;
    out_buff[1] = para;

    iWriteRegI2C((u8*)out_buff , (u16)sizeof(out_buff), SP2518_WRITE_ID); 

#if (defined(__SP2518_DEBUG_TRACE__))
  if (sizeof(out_buff) != rt) printk("I2C write %x, %x error\n", addr, para);
#endif
}

static kal_uint8 SP2518_read_cmos_sensor(kal_uint8 addr)
{
  kal_uint8 in_buff[1] = {0xFF};
  kal_uint8 out_buff[1];
  
  out_buff[0] = addr;
   
    if (0 != iReadRegI2C((u8*)out_buff , (u16) sizeof(out_buff), (u8*)in_buff, (u16) sizeof(in_buff), SP2518_WRITE_ID)) {
        SENSORDB("ERROR: SP2518_read_cmos_sensor \n");
    }

#if (defined(__SP2518_DEBUG_TRACE__))
  if (size != rt) printk("I2C read %x error\n", addr);
#endif

  return in_buff[0];
}
#endif
/*******************************************************************************
* // Adapter for Winmo typedef 
********************************************************************************/
#define WINMO_USE 0

#define mDELAY(ms)  mdelay(ms)
#define uDELAY(ms)  udelay(ms)
#define RETAILMSG(x,...)
#define TEXT


/*******************************************************************************
* // End Adapter for Winmo typedef 
********************************************************************************/


#define	SP2518_LIMIT_EXPOSURE_LINES				(1253)
#define	SP2518_VIDEO_NORMALMODE_30FRAME_RATE       (30)
#define	SP2518_VIDEO_NORMALMODE_FRAME_RATE         (15)
#define	SP2518_VIDEO_NIGHTMODE_FRAME_RATE          (7.5)
#define BANDING50_30HZ
/* Global Valuable */

static kal_uint32 zoom_factor = 0; 
static kal_uint8 SP2518_exposure_line_h = 0, SP2518_exposure_line_l = 0,
SP2518_extra_exposure_line_h = 0, SP2518_extra_exposure_line_l = 0;

static kal_bool SP2518_gPVmode = KAL_TRUE; //PV size or Full size
static kal_bool SP2518_VEDIO_encode_mode = KAL_FALSE; //Picture(Jpeg) or Video(Mpeg4)
static kal_bool SP2518_sensor_cap_state = KAL_FALSE; //Preview or Capture

static kal_uint16 SP2518_dummy_pixels=0, SP2518_dummy_lines=0;

static kal_uint16 SP2518_exposure_lines=0, SP2518_extra_exposure_lines = 0;


static kal_int8 SP2518_DELAY_AFTER_PREVIEW = -1;

static kal_uint8 SP2518_Banding_setting = AE_FLICKER_MODE_50HZ;  //Wonder add

/****** OVT 6-18******/
static kal_uint16 SP2518_Capture_Max_Gain16= 6*16;
static kal_uint16 SP2518_Capture_Gain16=0 ;    
static kal_uint16 SP2518_Capture_Shutter=0;
static kal_uint16 SP2518_Capture_Extra_Lines=0;

static kal_uint16  SP2518_PV_Dummy_Pixels =0, SP2518_Capture_Dummy_Pixels =0, SP2518_Capture_Dummy_Lines =0;
static kal_uint16  SP2518_PV_Gain16 = 0;
static kal_uint16  SP2518_PV_Shutter = 0;
static kal_uint16  SP2518_PV_Extra_Lines = 0;

kal_uint16 SP2518_sensor_gain_base=0,SP2518_FAC_SENSOR_REG=0,SP2518_iSP2518_Mode=0,SP2518_max_exposure_lines=0;
kal_uint32 SP2518_capture_pclk_in_M=520,SP2518_preview_pclk_in_M=390,SP2518_PV_dummy_pixels=0,SP2518_PV_dummy_lines=0,SP2518_isp_master_clock=0;


static kal_uint32  SP2518_sensor_pclk=390;
static kal_bool SP2518_AWB_ENABLE = KAL_TRUE; 
static kal_bool SP2518_AE_ENABLE = KAL_TRUE; 

static kal_bool sensor_night_mode = KAL_FALSE;
static kal_bool VEDIO_encode_mode = KAL_FALSE;
static kal_uint8 SP2518_AG_H = 0, SP2518_AG_L = 0;

static kal_uint32 Capture_Shutter = 0; 
static kal_uint32 Capture_Gain = 0; 

static kal_uint16 sensor_gain = 0;
static kal_uint16 PVExposureLine = 0x0180;

#if WINMO_USE
kal_uint8 SP2518_sensor_write_I2C_address = SP2518_WRITE_ID;
kal_uint8 SP2518_sensor_read_I2C_address = SP2518_READ_ID;

UINT32 SP2518GPIOBaseAddr;
HANDLE SP2518hGPIO;
HANDLE SP2518hDrvI2C;
I2C_TRANSACTION SP2518I2CConfig;
#endif 
UINT8 SP2518_PixelClockDivider=0;


//SENSOR_REG_STRUCT SP2518SensorCCT[FACTORY_END_ADDR]=CAMERA_SENSOR_CCT_DEFAULT_VALUE;
//SENSOR_REG_STRUCT SP2518SensorReg[ENGINEER_END]=CAMERA_SENSOR_REG_DEFAULT_VALUE;
//	camera_para.SENSOR.cct	SensorCCT	=> SensorCCT
//	camera_para.SENSOR.reg	SensorReg
MSDK_SENSOR_CONFIG_STRUCT SP2518SensorConfigData;

#if WINMO_USE
void SP2518_write_cmos_sensor(kal_uint32 addr, kal_uint32 para)
{
	SP2518I2CConfig.operation=I2C_OP_WRITE;
	SP2518I2CConfig.slaveAddr=SP2518_sensor_write_I2C_address>>1;
	SP2518I2CConfig.transfer_num=1;	/* TRANSAC_LEN */
	SP2518I2CConfig.transfer_len=3;
	SP2518I2CConfig.buffer[0]=(UINT8)(addr>>8);
	SP2518I2CConfig.buffer[1]=(UINT8)(addr&0xFF);
	SP2518I2CConfig.buffer[2]=(UINT8)para;
	DRV_I2CTransaction(SP2518hDrvI2C, &SP2518I2CConfig);

}	/* SP2518_write_cmos_sensor() */

kal_uint32 SP2518_read_cmos_sensor(kal_uint32 addr)
{
	kal_uint8 get_byte=0xFF;

	SP2518I2CConfig.operation=I2C_OP_WRITE;
	SP2518I2CConfig.slaveAddr=SP2518_sensor_write_I2C_address>>1;
	SP2518I2CConfig.transfer_num=1;	/* TRANSAC_LEN */
	SP2518I2CConfig.transfer_len=2;
	SP2518I2CConfig.buffer[0]=(UINT8)(addr>>8);
	SP2518I2CConfig.buffer[1]=(UINT8)(addr&0xFF);
	DRV_I2CTransaction(SP2518hDrvI2C, &SP2518I2CConfig);

	SP2518I2CConfig.operation=I2C_OP_READ;
	SP2518I2CConfig.slaveAddr=SP2518_sensor_read_I2C_address>>1;
	SP2518I2CConfig.transfer_num=1;	/* TRANSAC_LEN */
	SP2518I2CConfig.transfer_len=1;
	DRV_I2CTransaction(SP2518hDrvI2C, &SP2518I2CConfig);
	get_byte=SP2518I2CConfig.buffer[0];

	return get_byte;
}	/* SP2518_read_cmos_sensor() */
#endif 

void SP2518_set_dummy(kal_uint16 pixels, kal_uint16 lines)
{
   	kal_uint16 regTemp;

	regTemp = SP2518_FULL_PERIOD_PIXEL_NUMS + pixels;
	SP2518_write_cmos_sensor(0x300a,(regTemp>>8) & 0xFF);
	SP2518_write_cmos_sensor(0x300B,(regTemp & 0xFF));

	regTemp = SP2518_FULL_PERIOD_LINE_NUMS + lines;
	SP2518_write_cmos_sensor(0x300c,(regTemp>>8) & 0xFF);
	SP2518_write_cmos_sensor(0x300d,(regTemp & 0xFF));
	SP2518_write_cmos_sensor(0x3060,0x01);	
}    /* SP2518_set_dummy */

kal_uint16 SP2518_read_gain(void)
{
   //kal_uint16 sensor_gain;
   kal_uint16 temp_reg;

   temp_reg=(SP2518_read_cmos_sensor(0x301c) << 8) + SP2518_read_cmos_sensor(0x301d);

      
    return temp_reg;
}  /* SP2518_read_SP2518_gain */


kal_uint16 SP2518_read_shutter(void)
{
    kal_uint8 temp_reg1, temp_reg2;
    kal_uint16 temp_reg, extra_exp_lines;

    temp_reg1 = SP2518_read_cmos_sensor(0x3013);	
    temp_reg2 = SP2518_read_cmos_sensor(0x3012);	
    temp_reg = (temp_reg1 & 0xFF) | (temp_reg2 << 8);
    return temp_reg ;

}    /* SP2518_read_shutter */
    
void SP2518_write_gain(kal_uint16 gain)
    {
    SP2518_write_cmos_sensor(0x301c,(gain >>8) & 0xff);
    SP2518_write_cmos_sensor(0x301d,(gain & 0xff));
    //SP2518_write_cmos_sensor(0x3060,0x01);

}  /* SP2518_write_SP2518_gain */

static void SP2518_write_shutter(kal_uint16 shutter)
{
    // set extra exposure line
    SP2518_write_cmos_sensor(0x3012, (shutter >> 8) & 0xff);          
    SP2518_write_cmos_sensor(0x3013, shutter & 0xFF); // EXVTS[b15~b8]
    //SP2518_write_cmos_sensor(0x3060,0x01);

}    /* SP2518_write_shutter */

void SP2518_ExposureTrans()
{
  kal_uint16 temp_gain,maxExposureLine;

  SP2518_write_cmos_sensor(0x3201,0x0f);
  PVExposureLine = SP2518_read_shutter();
  sensor_gain = SP2518_read_gain();
  maxExposureLine = (SP2518_read_cmos_sensor(0x32c1) << 8) + SP2518_read_cmos_sensor(0x32c2);
  temp_gain = 1;
 
    if(sensor_gain&0x10)
        temp_gain<<=1;
      
    if(sensor_gain&0x20)
        temp_gain<<=1;
      
    if(sensor_gain&0x40)
        temp_gain<<=1;
  
   if(sensor_gain<0x02)
      {
        SP2518_write_cmos_sensor(0x3320,0x10);
        SP2518_write_cmos_sensor(0x3331,0x04);
        SP2518_write_cmos_sensor(0x3301,0x70);
		SP2518_write_cmos_sensor(0x3338,0x00);
		SP2518_write_cmos_sensor(0x32d8,0x30);
		SP2518_write_cmos_sensor(0x32d9,0x28);
      }
   else if((maxExposureLine ==PVExposureLine)&&(sensor_gain>=0x18))
      {
        SP2518_write_cmos_sensor(0x3320,0x20);
        SP2518_write_cmos_sensor(0x3331,0x08);
        SP2518_write_cmos_sensor(0x3301,0x60); 
      }

   

    SP2518_write_gain(sensor_gain&0xff0f);
    SP2518_write_shutter(PVExposureLine*temp_gain);
}

/*
void SP2518_Computer_AEC(kal_uint16 preview_clk_in_M, kal_uint16 capture_clk_in_M)
{
    kal_uint16 PV_Line_Width;
    kal_uint16 Capture_Line_Width;
    kal_uint16 Capture_Mensor_gainaximum_Shutter;
    kal_uint16 Capture_Exposure;
    kal_uint16 Capture_Gain16;
    kal_uint16 Capture_Banding_Filter;
    kal_uint32 Gain_Exposure=0;

    PV_Line_Width = SP2518_PV_PERIOD_PIXEL_NUMS + SP2518_PV_Dummy_Pixels;   

    Capture_Line_Width = SP2518_FULL_PERIOD_PIXEL_NUMS + SP2518_Capture_Dummy_Pixels;
    Capture_Maximum_Shutter = SP2518_FULL_EXPOSURE_LIMITATION + SP2518_Capture_Dummy_Lines;
    Gain_Exposure = 1;
    ///////////////////////
    Gain_Exposure *=(SP2518_PV_Shutter+SP2518_PV_Extra_Lines);
    Gain_Exposure *=PV_Line_Width;  //970
    //   Gain_Exposure /=g_Preview_PCLK_Frequency;
    Gain_Exposure /=Capture_Line_Width;//1940
    Gain_Exposure = Gain_Exposure*capture_clk_in_M/preview_clk_in_M;// for clock   

    //SP2518_Capture_Gain16 = Capture_Gain16;
    SP2518_Capture_Extra_Lines = (Gain_Exposure > Capture_Maximum_Shutter)?
            (Gain_Exposure - Capture_Maximum_Shutter):0;     
    
    SP2518_Capture_Shutter = Gain_Exposure - SP2518_Capture_Extra_Lines;
}
*/

void SP2518_Computer_AECAGC(kal_uint16 preview_clk_in_M, kal_uint16 capture_clk_in_M)
{
   
}

void SP2518_set_isp_driving_current(kal_uint8 current)
{
}


static void SP2518_set_AE_mode(kal_bool AE_enable)
{
    
}


static void SP2518_set_AWB_mode(kal_bool AWB_enable)
{
    
}
static void SP2518SetAeMode(kal_bool AeEnable)
{
  kal_uint8 temp;
  SENSORDB("[SP2518]SP2518SetAeMode AeEnable:%d;\n",AeEnable);
  SP2518_write_cmos_sensor(0xfd,0x00);
  temp = SP2518_read_cmos_sensor(0x32);
  if (AeEnable == KAL_TRUE)
  {
    temp |= 0x05;
  }
  else
  {
    temp &= ~0x05;
  }
  SP2518_write_cmos_sensor(0x32,temp);  
}

void SP2518_Video_mode_Bandingset_lowlight(void)
{
	if(AE_FLICKER_MODE_50HZ==SP2518Status.Banding)
	{
		//ae setting
		// 2.5x fix 6 fps 50hz
		SP2518_write_cmos_sensor(0xfd,0x00);
		SP2518_write_cmos_sensor(0x03,0x01);
		SP2518_write_cmos_sensor(0x04,0xd4);
		SP2518_write_cmos_sensor(0x05,0x00);
		SP2518_write_cmos_sensor(0x06,0x00);
		SP2518_write_cmos_sensor(0x07,0x00);
		SP2518_write_cmos_sensor(0x08,0x00);
		SP2518_write_cmos_sensor(0x09,0x03);
		SP2518_write_cmos_sensor(0x0a,0xe5);
		SP2518_write_cmos_sensor(0x2f,0x00);
		//SP2518_write_cmos_sensor(0x30,0x11);
		SP2518_write_cmos_sensor(0xf0,0x4e);
		SP2518_write_cmos_sensor(0xf1,0x00);
		SP2518_write_cmos_sensor(0xfd,0x01);
		SP2518_write_cmos_sensor(0x90,0x10);
		SP2518_write_cmos_sensor(0x92,0x01);
		SP2518_write_cmos_sensor(0x98,0x4e);
		SP2518_write_cmos_sensor(0x99,0x00);
		SP2518_write_cmos_sensor(0x9a,0x01);
		SP2518_write_cmos_sensor(0x9b,0x00);
		//Status 
		SP2518_write_cmos_sensor(0xfd,0x01);
		SP2518_write_cmos_sensor(0xce,0xe0);
		SP2518_write_cmos_sensor(0xcf,0x04);
		SP2518_write_cmos_sensor(0xd0,0xe0);
		SP2518_write_cmos_sensor(0xd1,0x04);
		SP2518_write_cmos_sensor(0xd7,0x4e);
		SP2518_write_cmos_sensor(0xd8,0x00);
		SP2518_write_cmos_sensor(0xd9,0x52);
		SP2518_write_cmos_sensor(0xda,0x00);
		SP2518_write_cmos_sensor(0xfd,0x00);
	}
	else if(AE_FLICKER_MODE_60HZ==SP2518Status.Banding)
	{
		//ae setting
		// 2.5x fix 6 fps 60hz
		SP2518_write_cmos_sensor(0xfd,0x00);
		SP2518_write_cmos_sensor(0x03,0x01);
		SP2518_write_cmos_sensor(0x04,0x86);
		SP2518_write_cmos_sensor(0x05,0x00);
		SP2518_write_cmos_sensor(0x06,0x00);
		SP2518_write_cmos_sensor(0x07,0x00);
		SP2518_write_cmos_sensor(0x08,0x00);
		SP2518_write_cmos_sensor(0x09,0x03);
		SP2518_write_cmos_sensor(0x0a,0xe5);
		SP2518_write_cmos_sensor(0x2f,0x00);
		//SP2518_write_cmos_sensor(0x30,0x11);
		SP2518_write_cmos_sensor(0xf0,0x41);
		SP2518_write_cmos_sensor(0xf1,0x00);
		SP2518_write_cmos_sensor(0xfd,0x01);
		SP2518_write_cmos_sensor(0x90,0x14);
		SP2518_write_cmos_sensor(0x92,0x01);
		SP2518_write_cmos_sensor(0x98,0x41);
		SP2518_write_cmos_sensor(0x99,0x00);
		SP2518_write_cmos_sensor(0x9a,0x01);
		SP2518_write_cmos_sensor(0x9b,0x00);
		//Status  
		SP2518_write_cmos_sensor(0xfd,0x01);
		SP2518_write_cmos_sensor(0xce,0x14);
		SP2518_write_cmos_sensor(0xcf,0x05);
		SP2518_write_cmos_sensor(0xd0,0x14);
		SP2518_write_cmos_sensor(0xd1,0x05);
		SP2518_write_cmos_sensor(0xd7,0x41);
		SP2518_write_cmos_sensor(0xd8,0x00);
		SP2518_write_cmos_sensor(0xd9,0x45);
		SP2518_write_cmos_sensor(0xda,0x00);
		SP2518_write_cmos_sensor(0xfd,0x00);
	}
}

void SP2518_Camera_mode_Bandingset_lowlight(void)
{
if(AE_FLICKER_MODE_50HZ==SP2518Status.Banding)
  {
	 //ae setting
	 // 2.5x fix 6 fps 50hz
	  SP2518_write_cmos_sensor(0xfd,0x00);
	  SP2518_write_cmos_sensor(0x03,0x01);
	  SP2518_write_cmos_sensor(0x04,0xd4);
	  SP2518_write_cmos_sensor(0x05,0x00);
	  SP2518_write_cmos_sensor(0x06,0x00);
	  SP2518_write_cmos_sensor(0x07,0x00);
	  SP2518_write_cmos_sensor(0x08,0x00);
	  SP2518_write_cmos_sensor(0x09,0x03);
	  SP2518_write_cmos_sensor(0x0a,0xe5);
	  SP2518_write_cmos_sensor(0x2f,0x00);
	  //SP2518_write_cmos_sensor(0x30,0x11);
	  SP2518_write_cmos_sensor(0xf0,0x4e);
	  SP2518_write_cmos_sensor(0xf1,0x00);
	  SP2518_write_cmos_sensor(0xfd,0x01);
	  SP2518_write_cmos_sensor(0x90,0x10);
	  SP2518_write_cmos_sensor(0x92,0x01);
	  SP2518_write_cmos_sensor(0x98,0x4e);
	  SP2518_write_cmos_sensor(0x99,0x00);
	  SP2518_write_cmos_sensor(0x9a,0x01);
	  SP2518_write_cmos_sensor(0x9b,0x00);
	  //Status 
	  SP2518_write_cmos_sensor(0xfd,0x01);
	  SP2518_write_cmos_sensor(0xce,0xe0);
	  SP2518_write_cmos_sensor(0xcf,0x04);
	  SP2518_write_cmos_sensor(0xd0,0xe0);
	  SP2518_write_cmos_sensor(0xd1,0x04);
	  SP2518_write_cmos_sensor(0xd7,0x4e);
	  SP2518_write_cmos_sensor(0xd8,0x00);
	  SP2518_write_cmos_sensor(0xd9,0x52);
	  SP2518_write_cmos_sensor(0xda,0x00);
	  SP2518_write_cmos_sensor(0xfd,0x00);

  }
else if(AE_FLICKER_MODE_60HZ==SP2518Status.Banding)
	
{
	//ae setting
	// 2.5x fix 6 fps 60hz
	SP2518_write_cmos_sensor(0xfd,0x00);
	SP2518_write_cmos_sensor(0x03,0x01);
	SP2518_write_cmos_sensor(0x04,0x86);
	SP2518_write_cmos_sensor(0x05,0x00);
	SP2518_write_cmos_sensor(0x06,0x00);
	SP2518_write_cmos_sensor(0x07,0x00);
	SP2518_write_cmos_sensor(0x08,0x00);
	SP2518_write_cmos_sensor(0x09,0x03);
	SP2518_write_cmos_sensor(0x0a,0xe5);
	SP2518_write_cmos_sensor(0x2f,0x00);
	//SP2518_write_cmos_sensor(0x30,0x11);
	SP2518_write_cmos_sensor(0xf0,0x41);
	SP2518_write_cmos_sensor(0xf1,0x00);
	SP2518_write_cmos_sensor(0xfd,0x01);
	SP2518_write_cmos_sensor(0x90,0x14);
	SP2518_write_cmos_sensor(0x92,0x01);
	SP2518_write_cmos_sensor(0x98,0x41);
	SP2518_write_cmos_sensor(0x99,0x00);
	SP2518_write_cmos_sensor(0x9a,0x01);
	SP2518_write_cmos_sensor(0x9b,0x00);
	//Status  
	SP2518_write_cmos_sensor(0xfd,0x01);
	SP2518_write_cmos_sensor(0xce,0x14);
	SP2518_write_cmos_sensor(0xcf,0x05);
	SP2518_write_cmos_sensor(0xd0,0x14);
	SP2518_write_cmos_sensor(0xd1,0x05);
	SP2518_write_cmos_sensor(0xd7,0x41);
	SP2518_write_cmos_sensor(0xd8,0x00);
	SP2518_write_cmos_sensor(0xd9,0x45);
	SP2518_write_cmos_sensor(0xda,0x00);
	SP2518_write_cmos_sensor(0xfd,0x00);
  }
}

void SP2518_Video_mode_Bandingset_normal(void)
{
	if(AE_FLICKER_MODE_50HZ==SP2518Status.Banding)
	{
		//ae setting
		// 2.5x fix 10.5 fps 50hz
		SP2518_write_cmos_sensor(0xfd,0x00);
		SP2518_write_cmos_sensor(0x03,0x03);
		SP2518_write_cmos_sensor(0x04,0x36);
		SP2518_write_cmos_sensor(0x05,0x00);
		SP2518_write_cmos_sensor(0x06,0x00);
		SP2518_write_cmos_sensor(0x07,0x00);
		SP2518_write_cmos_sensor(0x08,0x00);
		SP2518_write_cmos_sensor(0x09,0x00);
		SP2518_write_cmos_sensor(0x0a,0xa9);
		SP2518_write_cmos_sensor(0x2f,0x00);
		SP2518_write_cmos_sensor(0x30,0x11);
		SP2518_write_cmos_sensor(0xf0,0x89);
		SP2518_write_cmos_sensor(0xf1,0x00);
		SP2518_write_cmos_sensor(0xfd,0x01);
		SP2518_write_cmos_sensor(0x90,0x09);
		SP2518_write_cmos_sensor(0x92,0x01);
		SP2518_write_cmos_sensor(0x98,0x89);
		SP2518_write_cmos_sensor(0x99,0x00);
		SP2518_write_cmos_sensor(0x9a,0x01);
		SP2518_write_cmos_sensor(0x9b,0x00);
		//Status
		SP2518_write_cmos_sensor(0xfd,0x01);
		SP2518_write_cmos_sensor(0xce,0xd1);
		SP2518_write_cmos_sensor(0xcf,0x04);
		SP2518_write_cmos_sensor(0xd0,0xd1);
		SP2518_write_cmos_sensor(0xd1,0x04);
		SP2518_write_cmos_sensor(0xd7,0x85);
		SP2518_write_cmos_sensor(0xd8,0x00);
		SP2518_write_cmos_sensor(0xd9,0x89);
		SP2518_write_cmos_sensor(0xda,0x00);
		SP2518_write_cmos_sensor(0xfd,0x00);
	}
	else if(AE_FLICKER_MODE_60HZ==SP2518Status.Banding)
	{
		//ae setting
		// 2.5X pll   10.5~10.5fps  60hz     
		SP2518_write_cmos_sensor(0xfd,0x00);
		SP2518_write_cmos_sensor(0x03,0x02);
		SP2518_write_cmos_sensor(0x04,0xb2);
		SP2518_write_cmos_sensor(0x05,0x00);
		SP2518_write_cmos_sensor(0x06,0x00);
		SP2518_write_cmos_sensor(0x07,0x00);
		SP2518_write_cmos_sensor(0x08,0x00);
		SP2518_write_cmos_sensor(0x09,0x00);
		SP2518_write_cmos_sensor(0x0a,0xa1);
		SP2518_write_cmos_sensor(0x2f,0x00);
		SP2518_write_cmos_sensor(0x30,0x11);
		SP2518_write_cmos_sensor(0xf0,0x73);
		SP2518_write_cmos_sensor(0xf1,0x00);
		SP2518_write_cmos_sensor(0xfd,0x01);
		SP2518_write_cmos_sensor(0x90,0x0b);
		SP2518_write_cmos_sensor(0x92,0x01);
		SP2518_write_cmos_sensor(0x98,0x73);
		SP2518_write_cmos_sensor(0x99,0x00);
		SP2518_write_cmos_sensor(0x9a,0x01);
		SP2518_write_cmos_sensor(0x9b,0x00);
		//Status
		SP2518_write_cmos_sensor(0xfd,0x01);
		SP2518_write_cmos_sensor(0xce,0xf1);
		SP2518_write_cmos_sensor(0xcf,0x04);
		SP2518_write_cmos_sensor(0xd0,0xf1);
		SP2518_write_cmos_sensor(0xd1,0x04);
		SP2518_write_cmos_sensor(0xd7,0x6f);
		SP2518_write_cmos_sensor(0xd8,0x00);
		SP2518_write_cmos_sensor(0xd9,0x73);
		SP2518_write_cmos_sensor(0xda,0x00);
		SP2518_write_cmos_sensor(0xfd,0x00);
	}
}



static void SP2518_Camera_mode_Bandingset_normal(void)
{
if(AE_FLICKER_MODE_50HZ==SP2518Status.Banding)
  {   
	//ae setting
	// 2.5x fix 10.5 fps 50hz
	SP2518_write_cmos_sensor(0xfd,0x00);
	SP2518_write_cmos_sensor(0x03,0x03);
	SP2518_write_cmos_sensor(0x04,0x30);
	SP2518_write_cmos_sensor(0x05,0x00);
	SP2518_write_cmos_sensor(0x06,0x00);
	SP2518_write_cmos_sensor(0x07,0x00);
	SP2518_write_cmos_sensor(0x08,0x00);
	SP2518_write_cmos_sensor(0x09,0x00);
	SP2518_write_cmos_sensor(0x0a,0xaf);
	SP2518_write_cmos_sensor(0x2f,0x00);
	SP2518_write_cmos_sensor(0x30,0x11);
	SP2518_write_cmos_sensor(0xf0,0x88);
	SP2518_write_cmos_sensor(0xf1,0x00);
	SP2518_write_cmos_sensor(0xfd,0x01);
	SP2518_write_cmos_sensor(0x90,0x09);
	SP2518_write_cmos_sensor(0x92,0x01);
	SP2518_write_cmos_sensor(0x98,0x88);
	SP2518_write_cmos_sensor(0x99,0x00);
	SP2518_write_cmos_sensor(0x9a,0x01);
	SP2518_write_cmos_sensor(0x9b,0x00);
	//Status
	SP2518_write_cmos_sensor(0xfd,0x01);
	SP2518_write_cmos_sensor(0xce,0xc8);
	SP2518_write_cmos_sensor(0xcf,0x04);
	SP2518_write_cmos_sensor(0xd0,0xc8);
	SP2518_write_cmos_sensor(0xd1,0x04);
	SP2518_write_cmos_sensor(0xd7,0x88);
	SP2518_write_cmos_sensor(0xd8,0x00);
	SP2518_write_cmos_sensor(0xd9,0x8c);
	SP2518_write_cmos_sensor(0xda,0x00);
	SP2518_write_cmos_sensor(0xfd,0x00);
  }
else if(AE_FLICKER_MODE_60HZ==SP2518Status.Banding)
{	                 
	//ae setting
	// 2.5X pll   10.5~10.5fps  60hz     
	  SP2518_write_cmos_sensor(0xfd,0x00);
	  SP2518_write_cmos_sensor(0x03,0x02);
	  SP2518_write_cmos_sensor(0x04,0xa6);
	  SP2518_write_cmos_sensor(0x05,0x00);
	  SP2518_write_cmos_sensor(0x06,0x00);
	  SP2518_write_cmos_sensor(0x07,0x00);
	  SP2518_write_cmos_sensor(0x08,0x00);
	  SP2518_write_cmos_sensor(0x09,0x00);
	  SP2518_write_cmos_sensor(0x0a,0xaf);
	  SP2518_write_cmos_sensor(0x2f,0x00);
	  SP2518_write_cmos_sensor(0x30,0x11);
	  SP2518_write_cmos_sensor(0xf0,0x71);
	  SP2518_write_cmos_sensor(0xf1,0x00);
	  SP2518_write_cmos_sensor(0xfd,0x01);
	  SP2518_write_cmos_sensor(0x90,0x0b);
	  SP2518_write_cmos_sensor(0x92,0x01);
	  SP2518_write_cmos_sensor(0x98,0x71);
	  SP2518_write_cmos_sensor(0x99,0x00);
	  SP2518_write_cmos_sensor(0x9a,0x01);
	  SP2518_write_cmos_sensor(0x9b,0x00);
	  //Status
	  SP2518_write_cmos_sensor(0xfd,0x01);
	  SP2518_write_cmos_sensor(0xce,0xdb);
	  SP2518_write_cmos_sensor(0xcf,0x04);
	  SP2518_write_cmos_sensor(0xd0,0xdb);
	  SP2518_write_cmos_sensor(0xd1,0x04);
	  SP2518_write_cmos_sensor(0xd7,0x71);
	  SP2518_write_cmos_sensor(0xd8,0x00);
	  SP2518_write_cmos_sensor(0xd9,0x75);
	  SP2518_write_cmos_sensor(0xda,0x00);
	  SP2518_write_cmos_sensor(0xfd,0x00);
         }
}

/*************************************************************************
* FUNCTION
*	SP2518_night_mode
*
* DESCRIPTION
*	This function night mode of SP2518.
*
* PARAMETERS
*	nonePVExposureLine
*
* RETURNS
*	None
*ensor_gain
* GLOBALS AFFECTED
*
*************************************************************************/
BOOL SP2518_set_param_banding(UINT16 iPara); 

void SP2518_night_mode(kal_bool enable)
{
      SENSORDB("%s\n", __func__);
	
	SP2518SetAeMode(KAL_FALSE);//add  0514

  /* Night mode only for camera preview */
  if(enable)
  {
  	SP2518Status.NightMode = KAL_TRUE;
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0xb2 , 0x20);
	
	if(KAL_TRUE == SP2518Status.Videomode)
	{
		//SENSORDB("[SP2518]SP2518NightMode Video_night SP2518Status.Banding:%d;\n",SP2518Status.Banding);
		//SENSORDB("[SP2518]SP2518NightMode Video_night SP2518Status.NightMode:%d;\n",SP2518Status.NightMode);
        SP2518_Video_mode_Bandingset_lowlight();
	}
	 else
	{
		//SENSORDB("[SP2518]SP2518NightMode Cam_night SP2518Status.Banding:%d;\n",SP2518Status.Banding);
		//SENSORDB("[SP2518]SP2518NightMode Cam_nigth SP2518Status.NightMode:%d;\n",SP2518Status.NightMode);
		SP2518_Camera_mode_Bandingset_lowlight();
	}
  }
  else
  {
      SP2518Status.NightMode = KAL_FALSE;
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0xb2 , 0x10);
	if(KAL_TRUE == SP2518Status.Videomode)
	{
		//SENSORDB("[SP2518]SP2518NightMode Video_normal SP2518Status.Banding:%d;\n",SP2518Status.Banding);
		//SENSORDB("[SP2518]SP2518NightMode Video_normal SP2518Status.NightMode:%d;\n",SP2518Status.NightMode);
	    SP2518_Video_mode_Bandingset_normal();
	}
	 else
	{
		//SENSORDB("[SP2518]SP2518NightMode Cam_normal SP2518Status.Banding:%d;\n",SP2518Status.Banding);
		//SENSORDB("[SP2518]SP2518NightMode Cam_normal SP2518Status.NightMode:%d;\n",SP2518Status.NightMode);
		SP2518_Camera_mode_Bandingset_normal();
	}
  }

  	SP2518SetAeMode(KAL_TRUE);//add  0514
}	/* SP2518_night_mode */

/*************************************************************************
* FUNCTIONSP2518_write_shutter(kal_uint16 shutter)
*	SP2518_GetSensorID
*
* DESCRIPTION
*	This function get the sensor ID
*SP2518_write_shutter(kal_uint16 shutter)
* PARAMETERS
*	None
*
* RETURNS
*	None
*
* GLOBALS AFFECTED
*
*************************************************************************/
static kal_uint32 SP2518_GetSensorID(kal_uint32 *sensorID)
{
       volatile signed char i;
	kal_uint16 sensor_id=0;
	  SENSORDB("xieyang SP2518GetSensorID ");
	  SENSORDB("xieyang in GPIO_CAMERA_CMPDN_PIN=%d,GPIO_CAMERA_CMPDN1_PIN=%d"
	  	    , mt_get_gpio_out(GPIO_CAMERA_CMPDN_PIN),mt_get_gpio_out(GPIO_CAMERA_CMPDN1_PIN));

      for(i=0;i<3;i++)
	{
	       SP2518_write_cmos_sensor(0xfd, 0x00); 
		sensor_id = SP2518_read_cmos_sensor(0x02);
		SENSORDB("%s sensor_id=%d\n", __func__, sensor_id);
		if(sensor_id == SP2518_SENSOR_ID)
		{
			break;
		}
	}
	 
	if(sensor_id != SP2518_SENSOR_ID)
	{
		*sensorID = 0xFFFFFFFF;
		return ERROR_SENSOR_CONNECT_FAIL;
	}
	else
		*sensorID = sensor_id;
	
	return ERROR_NONE;
}     

void SP2518InitPara(void)
{
  SENSORDB("[SP2518]SP2518InitPara \n");
  SP2518Status.NightMode = KAL_FALSE;
  SP2518Status.ZoomFactor = 0;
  SP2518Status.Banding = AE_FLICKER_MODE_50HZ;
  //SP2518Status.PvShutter = 0x17c40;
  //SP2518Status.MaxFrameRate = SP2518_MAX_FPS;
  //SP2518Status.MiniFrameRate = SP2518_FPS(10);
  SP2518Status.PvDummyPixels = 424;
  SP2518Status.PvDummyLines = 62;
  SP2518Status.CapDummyPixels = 360;
  SP2518Status.CapDummyLines = 52; /* 10 FPS, 104 for 9.6 FPS*/
  SP2518Status.PvOpClk = 24;
  SP2518Status.CapOpClk = 24;  
  SP2518Status.VDOCTL2 = 0x90;
  //SP2518Status.ISPCTL3 = 0x30;
  //SP2518Status.AECTL1 = 0x9c;
  //SP2518Status.AWBCTL1 = 0xe9;
  SP2518Status.Videomode = KAL_FALSE;
}

void SP2518InitSetting(void)
{
	SP2518_write_cmos_sensor(0xfd , 0x00);
       SP2518_write_cmos_sensor(0x1b , 0x1a);//0X02
	SP2518_write_cmos_sensor(0x0e , 0x01);
	SP2518_write_cmos_sensor(0x0f , 0x2f);
	SP2518_write_cmos_sensor(0x10 , 0x2e);
	SP2518_write_cmos_sensor(0x11 , 0x00);
	SP2518_write_cmos_sensor(0x12 , 0x4f);//2f
	SP2518_write_cmos_sensor(0x14 , 0x20);//xg 0x20
	SP2518_write_cmos_sensor(0x16 , 0x02);
	SP2518_write_cmos_sensor(0x17 , 0x10);
	SP2518_write_cmos_sensor(0x1a , 0x1f);//xg 0x1e
	SP2518_write_cmos_sensor(0x1e , 0x81);
	SP2518_write_cmos_sensor(0x21 , 0x00);//xg 0x0f --->0x00
	SP2518_write_cmos_sensor(0x22 , 0x1B);//19 修正竖线问题
	SP2518_write_cmos_sensor(0x25 , 0x10);
	SP2518_write_cmos_sensor(0x26 , 0x25);
	SP2518_write_cmos_sensor(0x27 , 0x6d);//0x6d	//xg 0x67
	SP2518_write_cmos_sensor(0x2c , 0x31);//xg 0x4a
	SP2518_write_cmos_sensor(0x2d , 0x75);
	SP2518_write_cmos_sensor(0x2e , 0x38);//18
	SP2518_write_cmos_sensor(0x31 , 0x10);  //mirror upside down
	SP2518_write_cmos_sensor(0x44 , 0x03);
	SP2518_write_cmos_sensor(0x6f , 0x00);
	SP2518_write_cmos_sensor(0xa0 , 0x04);
	SP2518_write_cmos_sensor(0x5f , 0x01);
	SP2518_write_cmos_sensor(0x32 , 0x00);
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0x2c , 0x00);
	SP2518_write_cmos_sensor(0x2d , 0x00);
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0xfb , 0x83);
	SP2518_write_cmos_sensor(0xf4 , 0x09);

	//Pregain
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0xc6 , 0xa0);//0x90
	SP2518_write_cmos_sensor(0xc7 , 0xa0); 
	SP2518_write_cmos_sensor(0xc8 , 0xa0);
	SP2518_write_cmos_sensor(0xc9 , 0xa0);
	//black level
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0x65 , 0x08);
	SP2518_write_cmos_sensor(0x66 , 0x08);
	SP2518_write_cmos_sensor(0x67 , 0x08);
	SP2518_write_cmos_sensor(0x68 , 0x08);
	//rpc
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0xe0 , 0x6c); 
	SP2518_write_cmos_sensor(0xe1 , 0x54); 
	SP2518_write_cmos_sensor(0xe2 , 0x48); 
	SP2518_write_cmos_sensor(0xe3 , 0x40);
	SP2518_write_cmos_sensor(0xe4 , 0x40);
	SP2518_write_cmos_sensor(0xe5 , 0x3e);
	SP2518_write_cmos_sensor(0xe6 , 0x3e);
	SP2518_write_cmos_sensor(0xe8 , 0x3a);
	SP2518_write_cmos_sensor(0xe9 , 0x3a);
	SP2518_write_cmos_sensor(0xea , 0x3a);
	SP2518_write_cmos_sensor(0xeb , 0x38);
	SP2518_write_cmos_sensor(0xf5 , 0x38);
	SP2518_write_cmos_sensor(0xf6 , 0x38);
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0x94 , 0xc0);
	SP2518_write_cmos_sensor(0x95 , 0x38);
	SP2518_write_cmos_sensor(0x9c , 0x6c);
	SP2518_write_cmos_sensor(0x9d , 0x38);  

	#if 1
	//ae setting
	 // 24Mhz 2.5x fix 10.5 fps  50hz
	  SP2518_write_cmos_sensor(0xfd,0x00);
	  SP2518_write_cmos_sensor(0x03,0x03);
	  SP2518_write_cmos_sensor(0x04,0x30);
	  SP2518_write_cmos_sensor(0x05,0x00);
	  SP2518_write_cmos_sensor(0x06,0x00);
	  SP2518_write_cmos_sensor(0x07,0x00);
	  SP2518_write_cmos_sensor(0x08,0x00);
	  SP2518_write_cmos_sensor(0x09,0x00);
	  SP2518_write_cmos_sensor(0x0a,0xaf);
	  SP2518_write_cmos_sensor(0x2f,0x00);
	  SP2518_write_cmos_sensor(0x30,0x11);
	  SP2518_write_cmos_sensor(0xf0,0x88);
	  SP2518_write_cmos_sensor(0xf1,0x00);
	  SP2518_write_cmos_sensor(0xfd,0x01);
	  SP2518_write_cmos_sensor(0x90,0x09);
	  SP2518_write_cmos_sensor(0x92,0x01);
	  SP2518_write_cmos_sensor(0x98,0x88);
	  SP2518_write_cmos_sensor(0x99,0x00);
	  SP2518_write_cmos_sensor(0x9a,0x01);
	  SP2518_write_cmos_sensor(0x9b,0x00);
	  //Status
	  SP2518_write_cmos_sensor(0xfd,0x01);
	  SP2518_write_cmos_sensor(0xce,0xc8);
	  SP2518_write_cmos_sensor(0xcf,0x04);
	  SP2518_write_cmos_sensor(0xd0,0xc8);
	  SP2518_write_cmos_sensor(0xd1,0x04);
	  SP2518_write_cmos_sensor(0xd7,0x88);
	  SP2518_write_cmos_sensor(0xd8,0x00);
	  SP2518_write_cmos_sensor(0xd9,0x8c);
	  SP2518_write_cmos_sensor(0xda,0x00);
	  SP2518_write_cmos_sensor(0xfd,0x00);
	#else
	 //3X pll   13~8fps                                         
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0x03 , 0x03);
	SP2518_write_cmos_sensor(0x04 , 0xf6);
	SP2518_write_cmos_sensor(0x05 , 0x00);
	SP2518_write_cmos_sensor(0x06 , 0x00);
	SP2518_write_cmos_sensor(0x07 , 0x00);
	SP2518_write_cmos_sensor(0x08 , 0x00);
	SP2518_write_cmos_sensor(0x09 , 0x00);
	SP2518_write_cmos_sensor(0x0a , 0x8b);
	SP2518_write_cmos_sensor(0x2f , 0x00);
	SP2518_write_cmos_sensor(0x30 , 0x08);
	SP2518_write_cmos_sensor(0xf0 , 0xa9);
	SP2518_write_cmos_sensor(0xf1 , 0x00);
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0x90 , 0x0c);
	SP2518_write_cmos_sensor(0x92 , 0x01);
	SP2518_write_cmos_sensor(0x98 , 0xa9);
	SP2518_write_cmos_sensor(0x99 , 0x00);
	SP2518_write_cmos_sensor(0x9a , 0x01);
	SP2518_write_cmos_sensor(0x9b , 0x00);
	//Status                              
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0xce , 0xec);
	SP2518_write_cmos_sensor(0xcf , 0x07);
	SP2518_write_cmos_sensor(0xd0 , 0xec);
	SP2518_write_cmos_sensor(0xd1 , 0x07);
	SP2518_write_cmos_sensor(0xd7 , 0xa5);
	SP2518_write_cmos_sensor(0xd8 , 0x00);
	SP2518_write_cmos_sensor(0xd9 , 0xa9);
	SP2518_write_cmos_sensor(0xda , 0x00);
	SP2518_write_cmos_sensor(0xfd , 0x00);
       #endif
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0xca , 0x30);//mean dummy2low
	SP2518_write_cmos_sensor(0xcb , 0x50);//mean low2dummy
	SP2518_write_cmos_sensor(0xcc , 0xc0);//rpc low
	SP2518_write_cmos_sensor(0xcd , 0xc0);//rpc dummy
	SP2518_write_cmos_sensor(0xd5 , 0x80);//mean normal2dummy
	SP2518_write_cmos_sensor(0xd6 , 0x90);//mean dummy2normal
	SP2518_write_cmos_sensor(0xfd , 0x00);
	//lens shading for 舜泰979C-171A\181A
	SP2518_write_cmos_sensor(0xfd , 0x00); 
	SP2518_write_cmos_sensor(0xa1 , 0x20);
	SP2518_write_cmos_sensor(0xa2 , 0x20);
	SP2518_write_cmos_sensor(0xa3 , 0x20);
	SP2518_write_cmos_sensor(0xa4 , 0xff);
	SP2518_write_cmos_sensor(0xa5 , 0x80);
	SP2518_write_cmos_sensor(0xa6 , 0x80);
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0x64 , 0x1e);//28
	SP2518_write_cmos_sensor(0x65 , 0x1c);//25
	SP2518_write_cmos_sensor(0x66 , 0x1c);//2a
	SP2518_write_cmos_sensor(0x67 , 0x16);//25
	SP2518_write_cmos_sensor(0x68 , 0x1c);//25
	SP2518_write_cmos_sensor(0x69 , 0x1c);//29
	SP2518_write_cmos_sensor(0x6a , 0x1a);//28
	SP2518_write_cmos_sensor(0x6b , 0x16);//20
	SP2518_write_cmos_sensor(0x6c , 0x1a);//22
	SP2518_write_cmos_sensor(0x6d , 0x1a);//22
	SP2518_write_cmos_sensor(0x6e , 0x1a);//22
	SP2518_write_cmos_sensor(0x6f , 0x16);//1c
	SP2518_write_cmos_sensor(0xb8 , 0x04);//0a
	SP2518_write_cmos_sensor(0xb9 , 0x13);//0a
	SP2518_write_cmos_sensor(0xba , 0x00);//23
	SP2518_write_cmos_sensor(0xbb , 0x03);//14
	SP2518_write_cmos_sensor(0xbc , 0x03);//08
	SP2518_write_cmos_sensor(0xbd , 0x11);//08
	SP2518_write_cmos_sensor(0xbe , 0x00);//12
	SP2518_write_cmos_sensor(0xbf , 0x02);//00
	SP2518_write_cmos_sensor(0xc0 , 0x04);//05
	SP2518_write_cmos_sensor(0xc1 , 0x0e);//05
	SP2518_write_cmos_sensor(0xc2 , 0x00);//18
	SP2518_write_cmos_sensor(0xc3 , 0x05);//08   
	//raw filter
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0xde , 0x0f);
	SP2518_write_cmos_sensor(0xfd , 0x00);

	SP2518_write_cmos_sensor(0x57 , 0x08);//raw_dif_thr
	SP2518_write_cmos_sensor(0x58 , 0x08);//a
	SP2518_write_cmos_sensor(0x56 , 0x08);//10
	SP2518_write_cmos_sensor(0x59 , 0x10);
	//R\B通道间平滑
	SP2518_write_cmos_sensor(0x5a , 0xa0);//raw_rb_fac_outdoor
	SP2518_write_cmos_sensor(0xc4 , 0xa0);//60raw_rb_fac_indoor
	SP2518_write_cmos_sensor(0x43 , 0xa0);//40raw_rb_fac_dummy  
	SP2518_write_cmos_sensor(0xad , 0x40);//raw_rb_fac_low  
	  
	//Gr、Gb 通道内部平滑
	SP2518_write_cmos_sensor(0x4f , 0xa0);//raw_gf_fac_outdoor
	SP2518_write_cmos_sensor(0xc3 , 0xa0);//60raw_gf_fac_indoor
	SP2518_write_cmos_sensor(0x3f , 0xa0);//40raw_gf_fac_dummy
	SP2518_write_cmos_sensor(0x42 , 0x40);//raw_gf_fac_low
	SP2518_write_cmos_sensor(0xc2 , 0x15);

	//Gr、Gb通道间平滑
	SP2518_write_cmos_sensor(0xb6 , 0x80);//raw_gflt_fac_outdoor
	SP2518_write_cmos_sensor(0xb7 , 0x80);//60raw_gflt_fac_normal
	SP2518_write_cmos_sensor(0xb8 , 0x40);//40raw_gflt_fac_dummy
	SP2518_write_cmos_sensor(0xb9 , 0x20);//raw_gflt_fac_low
	//Gr、Gb通道阈值
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0x50 , 0x0c);//raw_grgb_thr
	SP2518_write_cmos_sensor(0x51 , 0x0c);
	SP2518_write_cmos_sensor(0x52 , 0x10);
	SP2518_write_cmos_sensor(0x53 , 0x10);
	SP2518_write_cmos_sensor(0xfd , 0x00);	
	   
	// awb1
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0x11 , 0x10);
	SP2518_write_cmos_sensor(0x12 , 0x1f);
	SP2518_write_cmos_sensor(0x16 , 0x1c);
	SP2518_write_cmos_sensor(0x18 , 0x00);
	SP2518_write_cmos_sensor(0x19 , 0x00);
	SP2518_write_cmos_sensor(0x1b , 0x96);
	SP2518_write_cmos_sensor(0x1a , 0x9a);//95
	SP2518_write_cmos_sensor(0x1e , 0x2f);
	SP2518_write_cmos_sensor(0x1f , 0x29);
	SP2518_write_cmos_sensor(0x20 , 0xff);
	SP2518_write_cmos_sensor(0x22 , 0xff);  
	SP2518_write_cmos_sensor(0x28 , 0xce);
	SP2518_write_cmos_sensor(0x29 , 0x8a);
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0xe7 , 0x03);
	SP2518_write_cmos_sensor(0xe7 , 0x00);
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0x2a , 0xf0);
	SP2518_write_cmos_sensor(0x2b , 0x10);
	SP2518_write_cmos_sensor(0x2e , 0x04);
	SP2518_write_cmos_sensor(0x2f , 0x18);
	SP2518_write_cmos_sensor(0x21 , 0x60);
	SP2518_write_cmos_sensor(0x23 , 0x60);
	SP2518_write_cmos_sensor(0x8b , 0xab);
	SP2518_write_cmos_sensor(0x8f , 0x12);
	  
	//awb2
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0x1a , 0x80);
	SP2518_write_cmos_sensor(0x1b , 0x80); 
	SP2518_write_cmos_sensor(0x43 , 0x80);
	//d65
	SP2518_write_cmos_sensor(0x35 , 0xd6);//b0
	SP2518_write_cmos_sensor(0x36 , 0xf0);//d1//e9
	SP2518_write_cmos_sensor(0x37 , 0x7a);//70
	SP2518_write_cmos_sensor(0x38 , 0x9a);//9a//af
	//indoor
	SP2518_write_cmos_sensor(0x39 , 0xab);
	SP2518_write_cmos_sensor(0x3a , 0xca);
	SP2518_write_cmos_sensor(0x3b , 0xa3);
	SP2518_write_cmos_sensor(0x3c , 0xc1);
	//f
	SP2518_write_cmos_sensor(0x31 , 0x82);
	SP2518_write_cmos_sensor(0x32 , 0xa5);//74
	SP2518_write_cmos_sensor(0x33 , 0xd6);
	SP2518_write_cmos_sensor(0x34 , 0xec);
	//cwf
	SP2518_write_cmos_sensor(0x3d , 0xa5);//88
	SP2518_write_cmos_sensor(0x3e , 0xc2);//bb
	SP2518_write_cmos_sensor(0x3f , 0xa7);//ad
	SP2518_write_cmos_sensor(0x40 , 0xc5);//d0

	//Color Correction
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0x1c , 0xc0);
	SP2518_write_cmos_sensor(0x1d , 0x95);

	SP2518_write_cmos_sensor(0xa0 , 0xa6);//b8 
	SP2518_write_cmos_sensor(0xa1 , 0xda);//d5
	SP2518_write_cmos_sensor(0xa2 , 0x00);//f2
	SP2518_write_cmos_sensor(0xa3 , 0x06);//e8
	SP2518_write_cmos_sensor(0xa4 , 0xb2);//95
	SP2518_write_cmos_sensor(0xa5 , 0xc7);//03
	SP2518_write_cmos_sensor(0xa6 , 0x00);//f2
	SP2518_write_cmos_sensor(0xa7 , 0xce);//c4
	SP2518_write_cmos_sensor(0xa8 , 0xb2);//ca
	SP2518_write_cmos_sensor(0xa9 , 0x0c);//3c
	SP2518_write_cmos_sensor(0xaa , 0x30);//03
	SP2518_write_cmos_sensor(0xab , 0x0c);//0f

	SP2518_write_cmos_sensor(0xac , 0xc0);//b8 
	SP2518_write_cmos_sensor(0xad , 0xc0);//d5
	SP2518_write_cmos_sensor(0xae , 0x00);//f2
	SP2518_write_cmos_sensor(0xaf , 0xf2);//e8
	SP2518_write_cmos_sensor(0xb0 , 0xa6);//95
	SP2518_write_cmos_sensor(0xb1 , 0xe8);//03
	SP2518_write_cmos_sensor(0xb2 , 0x00);//f2
	SP2518_write_cmos_sensor(0xb3 , 0xe7);//c4
	SP2518_write_cmos_sensor(0xb4 , 0x99);//ca
	SP2518_write_cmos_sensor(0xb5 , 0x0c);//3c
	SP2518_write_cmos_sensor(0xb6 , 0x33);//03
	SP2518_write_cmos_sensor(0xb7 , 0x0c);//0f
	 
	//Saturation
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0xbf , 0x01);
	SP2518_write_cmos_sensor(0xbe , 0xbb);
	SP2518_write_cmos_sensor(0xc0 , 0xb0);
	SP2518_write_cmos_sensor(0xc1 , 0xf0);
	SP2518_write_cmos_sensor(0xd3 , 0x77);
	SP2518_write_cmos_sensor(0xd4 , 0x77);
	SP2518_write_cmos_sensor(0xd6 , 0x77);
	SP2518_write_cmos_sensor(0xd7 , 0x77);
	SP2518_write_cmos_sensor(0xd8 , 0x77);
	SP2518_write_cmos_sensor(0xd9 , 0x77);
	SP2518_write_cmos_sensor(0xda , 0x77);
	SP2518_write_cmos_sensor(0xdb , 0x77);
	//uv_dif
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0xf3 , 0x03);
	SP2518_write_cmos_sensor(0xb0 , 0x00);
	SP2518_write_cmos_sensor(0xb1 , 0x23); 
	    
	//gamma1
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0x8b , 0x00);  
	SP2518_write_cmos_sensor(0x8c , 0xa);
	SP2518_write_cmos_sensor(0x8d , 0x13);
	SP2518_write_cmos_sensor(0x8e , 0x25);
	SP2518_write_cmos_sensor(0x8f , 0x43);
	SP2518_write_cmos_sensor(0x90 , 0x5d);
	SP2518_write_cmos_sensor(0x91 , 0x74);
	SP2518_write_cmos_sensor(0x92 , 0x88);
	SP2518_write_cmos_sensor(0x93 , 0x9a);
	SP2518_write_cmos_sensor(0x94 , 0xa9);
	SP2518_write_cmos_sensor(0x95 , 0xb5);
	SP2518_write_cmos_sensor(0x96 , 0xc0);
	SP2518_write_cmos_sensor(0x97 , 0xca);
	SP2518_write_cmos_sensor(0x98 , 0xd4);
	SP2518_write_cmos_sensor(0x99 , 0xdd);
	SP2518_write_cmos_sensor(0x9a , 0xe6);
	SP2518_write_cmos_sensor(0x9b , 0xef);
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0x8d , 0xf7);
	SP2518_write_cmos_sensor(0x8e , 0xff);
	//gamma2   
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0x78 , 0x00);  
	SP2518_write_cmos_sensor(0x79 , 0xa);
	SP2518_write_cmos_sensor(0x7a , 0x13);
	SP2518_write_cmos_sensor(0x7b , 0x25);
	SP2518_write_cmos_sensor(0x7c , 0x43);
	SP2518_write_cmos_sensor(0x7d , 0x5d);
	SP2518_write_cmos_sensor(0x7e , 0x74);
	SP2518_write_cmos_sensor(0x7f , 0x88);
	SP2518_write_cmos_sensor(0x80 , 0x9a);
	SP2518_write_cmos_sensor(0x81 , 0xa9);
	SP2518_write_cmos_sensor(0x82 , 0xb5);
	SP2518_write_cmos_sensor(0x83 , 0xc0);
	SP2518_write_cmos_sensor(0x84 , 0xca);
	SP2518_write_cmos_sensor(0x85 , 0xd4);
	SP2518_write_cmos_sensor(0x86 , 0xdd);
	SP2518_write_cmos_sensor(0x87 , 0xe6);
	SP2518_write_cmos_sensor(0x88 , 0xef);
	SP2518_write_cmos_sensor(0x89 , 0xf7);
	SP2518_write_cmos_sensor(0x8a , 0xff);
	//gamma_ae  
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0x96 , 0x46);
	SP2518_write_cmos_sensor(0x97 , 0x14);
	SP2518_write_cmos_sensor(0x9f , 0x06);

	//HEQ
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0xdd , 0x80);
	SP2518_write_cmos_sensor(0xde , 0x98);
	SP2518_write_cmos_sensor(0xdf , 0x80);
	  
	//Ytarget 
	SP2518_write_cmos_sensor(0xfd , 0x00);  
	SP2518_write_cmos_sensor(0xec , 0x70);
	SP2518_write_cmos_sensor(0xed , 0x86);
	SP2518_write_cmos_sensor(0xee , 0x70);
	SP2518_write_cmos_sensor(0xef , 0x86);
	SP2518_write_cmos_sensor(0xf7 , 0x80);
	SP2518_write_cmos_sensor(0xf8 , 0x74);
	SP2518_write_cmos_sensor(0xf9 , 0x80);
	SP2518_write_cmos_sensor(0xfa , 0x74); 

	//sharpen
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0xdf , 0x0f);
	SP2518_write_cmos_sensor(0xe5 , 0x10);
	SP2518_write_cmos_sensor(0xe7 , 0x10);
	SP2518_write_cmos_sensor(0xe8 , 0x28);
	SP2518_write_cmos_sensor(0xec , 0x28);  
	SP2518_write_cmos_sensor(0xe9 , 0x28);
	SP2518_write_cmos_sensor(0xed , 0x28);  
	SP2518_write_cmos_sensor(0xea , 0x18);
	SP2518_write_cmos_sensor(0xef , 0x18);  
	SP2518_write_cmos_sensor(0xeb , 0x10);
	SP2518_write_cmos_sensor(0xf0 , 0x10);    
	            
	//gw
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0x70 , 0x76);
	SP2518_write_cmos_sensor(0x7b , 0x40);
	SP2518_write_cmos_sensor(0x81 , 0x30);
	  
	//Y_offset
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0xb2 , 0x1f);
	SP2518_write_cmos_sensor(0xb3 , 0x0f);
	SP2518_write_cmos_sensor(0xb4 , 0x30);
	SP2518_write_cmos_sensor(0xb5 , 0x50);
	  
	//CNR
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0x5b , 0x20);
	SP2518_write_cmos_sensor(0x61 , 0x80);
	SP2518_write_cmos_sensor(0x77 , 0x80);//xg 0x20
	SP2518_write_cmos_sensor(0xca , 0x80);
	SP2518_write_cmos_sensor(0xab , 0x00);
	SP2518_write_cmos_sensor(0xac , 0x02);
	SP2518_write_cmos_sensor(0xae , 0x08);
	SP2518_write_cmos_sensor(0xaf , 0x20);

	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0x32 , 0x0d);
	SP2518_write_cmos_sensor(0x33 , 0xcf);//xg 0xef
	SP2518_write_cmos_sensor(0x34 , 0x3f);
	SP2518_write_cmos_sensor(0x35 , 0x00);

	 #if 0
	//resize   800x600                           
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0x06 , 0x00);
	SP2518_write_cmos_sensor(0x07 , 0x40);
	SP2518_write_cmos_sensor(0x08 , 0x00);
	SP2518_write_cmos_sensor(0x09 , 0x40);
	SP2518_write_cmos_sensor(0x0a , 0x02);
	SP2518_write_cmos_sensor(0x0b , 0x58);
	SP2518_write_cmos_sensor(0x0c , 0x03);
	SP2518_write_cmos_sensor(0x0d , 0x20);
	SP2518_write_cmos_sensor(0x0e , 0x01);
	SP2518_write_cmos_sensor(0x0f , 0x01);
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0x2f , 0x08);
       #else
	//1600x1200
	SP2518_write_cmos_sensor(0xfd , 0x01);
	SP2518_write_cmos_sensor(0x06 , 0x00);
	SP2518_write_cmos_sensor(0x07 , 0x40);
	SP2518_write_cmos_sensor(0x08 , 0x00);
	SP2518_write_cmos_sensor(0x09 , 0x40);
	SP2518_write_cmos_sensor(0x0a , 0x02);
	SP2518_write_cmos_sensor(0x0b , 0x58);
	SP2518_write_cmos_sensor(0x0c , 0x03);
	SP2518_write_cmos_sensor(0x0d , 0x20);
	SP2518_write_cmos_sensor(0x0e , 0x00);
	SP2518_write_cmos_sensor(0x0f , 0x00);
	SP2518_write_cmos_sensor(0xfd , 0x00);
	SP2518_write_cmos_sensor(0x2f , 0x00);
       #endif
  /*[END]*/
}

/*****************************************************************************/
/* Windows Mobile Sensor Interface */
/*****************************************************************************/
/*************************************************************************
* FUNCTION
*	SP2518Open
*
* DESCRIPTION
*	This function initialize the registers of CMOS sensor
*
* PARAMETERS
*	None
*
* RETURNS
*	None
*
* GLOBALS AFFECTED
*
*************************************************************************/
UINT32 SP2518Open(void)
{
	volatile signed char i;
	kal_uint16 sensor_id=0;

	printk("%s, start\n", __func__);
	
       zoom_factor = 0; 

       mDELAY(10);

	//  Read sensor ID to adjust I2C is OK?
	for(i=0;i<3;i++)
	{
	       SP2518_write_cmos_sensor(0xfd, 0x00); 
		sensor_id = SP2518_read_cmos_sensor(0x02);
		SENSORDB("%s sensor_id=%d\n", __func__, sensor_id);
		if(sensor_id == SP2518_SENSOR_ID)
		{
			break;
			//return ERROR_SENSOR_CONNECT_FAIL;
		}
	}
	if(sensor_id != SP2518_SENSOR_ID)
	{
       return ERROR_SENSOR_CONNECT_FAIL;
	}
	RETAILMSG(1, (TEXT("SP2518 Sensor Read ID OK \r\n")));
       mDELAY(10);
       SP2518InitSetting();
       SP2518InitPara();
	SENSORDB("%s, open end\n", __func__);
	return ERROR_NONE;
}	/* SP2518Open() */

/*************************************************************************
* FUNCTION
*	SP2518Close
*
* DESCRIPTION
*	This function is to turn off sensor module power.
*
* PARAMETERS
*	None
*
* RETURNS
*	None
*
* GLOBALS AFFECTED
*
*************************************************************************/
UINT32 SP2518Close(void)
{
//	CISModulePowerOn(FALSE);

#if WINMO_USE
	#ifndef SOFTWARE_I2C_INTERFACE	/* software I2c MODE */
	DRV_I2CClose(SP2518hDrvI2C);
	#endif
#endif 	
	return ERROR_NONE;
}	/* SP2518Close() */

/*************************************************************************
* FUNCTION
*	SP2518Preview
*
* DESCRIPTION
*	This function start the sensor preview.
*
* PARAMETERS
*	*image_window : address pointer of pixel numbers in one period of HSYNC
*  *sensor_config_data : address pointer of line numbers in one period of VSYNC
*
* RETURNS
*	None
*
* GLOBALS AFFECTED
*
*************************************************************************/
UINT32 SP2518Preview(MSDK_SENSOR_EXPOSURE_WINDOW_STRUCT *image_window,
					  MSDK_SENSOR_CONFIG_STRUCT *sensor_config_data)
{
    kal_uint8 iTemp, temp_AE_reg, temp_AWB_reg, iMirrorTemp;
    kal_uint16 iDummyPixels = 0, iDummyLines = 0, iStartX = 0, iStartY = 0;
    kal_uint16 prview_delay=0;
    kal_uint8 banding;
     
    SP2518_sensor_cap_state = KAL_FALSE;
    
    printk("........cam SP2518_Preview start.......\r\n");
    
    SP2518_gPVmode = KAL_TRUE;
   
    if(sensor_config_data->SensorOperationMode==MSDK_SENSOR_OPERATION_MODE_VIDEO)		// MPEG4 Encode Mode
    {
        VEDIO_encode_mode = KAL_TRUE;	
    }
    else
    {
        SP2518_VEDIO_encode_mode = KAL_FALSE;
    }
    
    //4 <3> set mirror and flip
    //4 <6> set dummy
    //4 <7> set shutter
    image_window->GrabStartX = 1;
    image_window->GrabStartY = 1;
    image_window->ExposureWindowWidth = SP2518_IMAGE_SENSOR_FULL_WIDTH;
    image_window->ExposureWindowHeight = SP2518_IMAGE_SENSOR_FULL_HEIGHT;
    SP2518_DELAY_AFTER_PREVIEW = 1;
     // copy sensor_config_data
    memcpy(&SP2518SensorConfigData, sensor_config_data, sizeof(MSDK_SENSOR_CONFIG_STRUCT));
    printk("%s, end\n", __func__);
    return ERROR_NONE;

}/* SP2518Preview() */

UINT32 SP2518Capture(MSDK_SENSOR_EXPOSURE_WINDOW_STRUCT *image_window,
					  MSDK_SENSOR_CONFIG_STRUCT *sensor_config_data)
{
  	SENSORDB("%s\n", __func__);
 
    SP2518_sensor_cap_state = KAL_TRUE;
    SP2518_DELAY_AFTER_PREVIEW = 2;//4;  // 2

	// copy sensor_config_data
	//memcpy(&SP2518SensorConfigData, sensor_config_data, sizeof(MSDK_SENSOR_CONFIG_STRUCT));
       
	//mdelay(1000);
	return ERROR_NONE;

}    /* SP2518_Capture */

UINT32 SP2518GetResolution(MSDK_SENSOR_RESOLUTION_INFO_STRUCT *pSensorResolution)
{
	printk("%s, start\n", __func__);
	pSensorResolution->SensorFullWidth=SP2518_IMAGE_SENSOR_FULL_WIDTH - 2 * IMAGE_SENSOR_START_GRAB_X;  //modify by yanxu
	pSensorResolution->SensorFullHeight=SP2518_IMAGE_SENSOR_FULL_HEIGHT - 2 * IMAGE_SENSOR_START_GRAB_Y;
	pSensorResolution->SensorPreviewWidth=SP2518_IMAGE_SENSOR_FULL_WIDTH - 2 * IMAGE_SENSOR_START_GRAB_X;
	pSensorResolution->SensorPreviewHeight=SP2518_IMAGE_SENSOR_FULL_HEIGHT - 2 * IMAGE_SENSOR_START_GRAB_Y;
	printk("%s, end\n", __func__);

	return ERROR_NONE;
}	/* SP2518GetResolution() */

UINT32 SP2518GetInfo(MSDK_SCENARIO_ID_ENUM ScenarioId,
					  MSDK_SENSOR_INFO_STRUCT *pSensorInfo,
					  MSDK_SENSOR_CONFIG_STRUCT *pSensorConfigData)
{
	printk("%s, start\n", __func__);
	pSensorInfo->SensorPreviewResolutionX=SP2518_IMAGE_SENSOR_FULL_WIDTH;
	pSensorInfo->SensorPreviewResolutionY=SP2518_IMAGE_SENSOR_FULL_HEIGHT;
	pSensorInfo->SensorFullResolutionX=SP2518_IMAGE_SENSOR_FULL_WIDTH;
	pSensorInfo->SensorFullResolutionY=SP2518_IMAGE_SENSOR_FULL_HEIGHT;

	pSensorInfo->SensorCameraPreviewFrameRate=30;
	pSensorInfo->SensorVideoFrameRate=30;
	pSensorInfo->SensorStillCaptureFrameRate=10;
	pSensorInfo->SensorWebCamCaptureFrameRate=15;
	pSensorInfo->SensorResetActiveHigh=FALSE;
	pSensorInfo->SensorResetDelayCount=1;
	pSensorInfo->SensorOutputDataFormat=SENSOR_OUTPUT_FORMAT_VYUY;//SENSOR_OUTPUT_FORMAT_UYVY;
	pSensorInfo->SensorClockPolarity=SENSOR_CLOCK_POLARITY_LOW;	/*??? */
	pSensorInfo->SensorClockFallingPolarity=SENSOR_CLOCK_POLARITY_LOW;  // SET_CMOS_CLOCK_POLARITY_LOW
	pSensorInfo->SensorHsyncPolarity = SENSOR_CLOCK_POLARITY_LOW;       // SET_HSYNC_POLARITY_LOW
	pSensorInfo->SensorVsyncPolarity = SENSOR_CLOCK_POLARITY_HIGH;       // SET_VSYNC_POLARITY_HIGH
	pSensorInfo->SensorInterruptDelayLines = 1;
	pSensorInfo->SensroInterfaceType=SENSOR_INTERFACE_TYPE_PARALLEL;

	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_100_MODE].MaxWidth=CAM_SIZE_5M_WIDTH;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_100_MODE].MaxHeight=CAM_SIZE_5M_HEIGHT;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_100_MODE].ISOSupported=TRUE;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_100_MODE].BinningEnable=FALSE;

	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_200_MODE].MaxWidth=CAM_SIZE_5M_WIDTH;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_200_MODE].MaxHeight=CAM_SIZE_5M_HEIGHT;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_200_MODE].ISOSupported=TRUE;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_200_MODE].BinningEnable=FALSE;

	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_400_MODE].MaxWidth=CAM_SIZE_5M_WIDTH;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_400_MODE].MaxHeight=CAM_SIZE_5M_HEIGHT;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_400_MODE].ISOSupported=TRUE;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_400_MODE].BinningEnable=FALSE;

	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_800_MODE].MaxWidth=CAM_SIZE_1M_WIDTH;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_800_MODE].MaxHeight=CAM_SIZE_1M_HEIGHT;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_800_MODE].ISOSupported=TRUE;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_800_MODE].BinningEnable=TRUE;

	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_1600_MODE].MaxWidth=CAM_SIZE_1M_WIDTH;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_1600_MODE].MaxHeight=CAM_SIZE_1M_HEIGHT;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_1600_MODE].ISOSupported=TRUE;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_1600_MODE].BinningEnable=TRUE;
	pSensorInfo->CaptureDelayFrame = 3; 
	pSensorInfo->PreviewDelayFrame = 3; 
	pSensorInfo->VideoDelayFrame = 4; 		
	pSensorInfo->SensorMasterClockSwitch = 0; 
       pSensorInfo->SensorDrivingCurrent = ISP_DRIVING_8MA;   		

	switch (ScenarioId)
	{
		case MSDK_SCENARIO_ID_CAMERA_PREVIEW:
		case MSDK_SCENARIO_ID_VIDEO_PREVIEW:
		case MSDK_SCENARIO_ID_VIDEO_CAPTURE_MPEG4:
			pSensorInfo->SensorClockFreq=24;
			pSensorInfo->SensorClockDividCount=	3;
			pSensorInfo->SensorClockRisingCount= 0;
			pSensorInfo->SensorClockFallingCount= 2;
			pSensorInfo->SensorPixelClockCount= 3;
			pSensorInfo->SensorDataLatchCount= 2;
                     pSensorInfo->SensorGrabStartX = 1;  
                     pSensorInfo->SensorGrabStartY = 1;             
			
		break;
		case MSDK_SCENARIO_ID_CAMERA_CAPTURE_JPEG:
		case MSDK_SCENARIO_ID_CAMERA_CAPTURE_MEM:
			pSensorInfo->SensorClockFreq=24;
			pSensorInfo->SensorClockDividCount=	3;
			pSensorInfo->SensorClockRisingCount= 0;
			pSensorInfo->SensorClockFallingCount= 2;
			pSensorInfo->SensorPixelClockCount= 3;
			pSensorInfo->SensorDataLatchCount= 2;
                     pSensorInfo->SensorGrabStartX = 1; 
                     pSensorInfo->SensorGrabStartY = 1;             
			
		break;
		default:
			pSensorInfo->SensorClockFreq=24;
			pSensorInfo->SensorClockDividCount=3;
			pSensorInfo->SensorClockRisingCount=0;
			pSensorInfo->SensorClockFallingCount=2;
			pSensorInfo->SensorPixelClockCount=3;
			pSensorInfo->SensorDataLatchCount=2;
                     pSensorInfo->SensorGrabStartX = 1; 
                     pSensorInfo->SensorGrabStartY = 1;             
			
		break;
	}
	SP2518_PixelClockDivider=pSensorInfo->SensorPixelClockCount;
	memcpy(pSensorConfigData, &SP2518SensorConfigData, sizeof(MSDK_SENSOR_CONFIG_STRUCT));
	printk("%s, end\n", __func__);
	return ERROR_NONE;
}	/* SP2518GetInfo() */


UINT32 SP2518Control(MSDK_SCENARIO_ID_ENUM ScenarioId, MSDK_SENSOR_EXPOSURE_WINDOW_STRUCT *pImageWindow,
					  MSDK_SENSOR_CONFIG_STRUCT *pSensorConfigData)
{
	printk("%s, start\n", __func__);
	switch (ScenarioId)
	{
		case MSDK_SCENARIO_ID_CAMERA_PREVIEW:
		case MSDK_SCENARIO_ID_VIDEO_PREVIEW:
		case MSDK_SCENARIO_ID_VIDEO_CAPTURE_MPEG4:
			SP2518Preview(pImageWindow, pSensorConfigData);
		break;
		case MSDK_SCENARIO_ID_CAMERA_CAPTURE_JPEG:
		case MSDK_SCENARIO_ID_CAMERA_CAPTURE_MEM:
			SP2518Capture(pImageWindow, pSensorConfigData);
		break;
		default:
		    break; 
	}
	printk("%s, end\n", __func__);
	return TRUE;
}	/* SP2518Control() */

/* [TC] YUV sensor */	
#if WINMO_USE
void SP2518Query(PMSDK_FEATURE_INFO_STRUCT pSensorFeatureInfo)
{
	MSDK_FEATURE_TYPE_RANGE_STRUCT *pFeatureRange;
	MSDK_FEATURE_TYPE_MULTI_SELECTION_STRUCT *pFeatureMultiSelection;
	switch (pSensorFeatureInfo->FeatureId)
	{
		case ISP_FEATURE_DSC_MODE:
			pSensorFeatureInfo->FeatureType = MSDK_FEATURE_TYPE_MULTI_SELECTION;
			pSensorFeatureInfo->FeatureSupported = (UINT8)(MSDK_SET_GET_FEATURE_SUPPORTED|MSDK_QUERY_CAMERA_SUPPORTED);
			pFeatureMultiSelection = (PMSDK_FEATURE_TYPE_MULTI_SELECTION_STRUCT)(&pSensorFeatureInfo->FeatureInformation.FeatureMultiSelection);
			pFeatureMultiSelection->TotalSelection = CAM_NO_OF_SCENE_MODE_MAX;
			pFeatureMultiSelection->DefaultSelection = CAM_AUTO_DSC_MODE;
			pFeatureMultiSelection->SupportedSelection = 
				(CAMERA_FEATURE_SUPPORT(CAM_AUTO_DSC_MODE)|
				CAMERA_FEATURE_SUPPORT(CAM_NIGHTSCENE_MODE));			
		break;
		case ISP_FEATURE_WHITEBALANCE:
			pSensorFeatureInfo->FeatureType = MSDK_FEATURE_TYPE_MULTI_SELECTION;
			pSensorFeatureInfo->FeatureSupported = (UINT8)(MSDK_SET_GET_FEATURE_SUPPORTED|MSDK_QUERY_CAMERA_VIDEO_SUPPORTED);
			pFeatureMultiSelection = (PMSDK_FEATURE_TYPE_MULTI_SELECTION_STRUCT)(&pSensorFeatureInfo->FeatureInformation.FeatureMultiSelection);
			pFeatureMultiSelection->TotalSelection = CAM_NO_OF_WB;
			pFeatureMultiSelection->DefaultSelection = CAM_WB_AUTO;
			pFeatureMultiSelection->SupportedSelection = 
				(CAMERA_FEATURE_SUPPORT(CAM_WB_AUTO)|
				CAMERA_FEATURE_SUPPORT(CAM_WB_CLOUD)|
				CAMERA_FEATURE_SUPPORT(CAM_WB_DAYLIGHT)|
				CAMERA_FEATURE_SUPPORT(CAM_WB_INCANDESCENCE)|
				CAMERA_FEATURE_SUPPORT(CAM_WB_TUNGSTEN)|
				CAMERA_FEATURE_SUPPORT(CAM_WB_FLUORESCENT));
		break;
		case ISP_FEATURE_IMAGE_EFFECT:
			pSensorFeatureInfo->FeatureType = MSDK_FEATURE_TYPE_MULTI_SELECTION;
			pSensorFeatureInfo->FeatureSupported = (UINT8)(MSDK_SET_GET_FEATURE_SUPPORTED|MSDK_QUERY_CAMERA_VIDEO_SUPPORTED);
			pFeatureMultiSelection = (PMSDK_FEATURE_TYPE_MULTI_SELECTION_STRUCT)(&pSensorFeatureInfo->FeatureInformation.FeatureMultiSelection);
			pFeatureMultiSelection->TotalSelection = CAM_NO_OF_EFFECT_ENC;
			pFeatureMultiSelection->DefaultSelection = CAM_EFFECT_ENC_NORMAL;
			pFeatureMultiSelection->SupportedSelection = 
				(CAMERA_FEATURE_SUPPORT(CAM_EFFECT_ENC_NORMAL)|
				CAMERA_FEATURE_SUPPORT(CAM_EFFECT_ENC_GRAYSCALE)|
				CAMERA_FEATURE_SUPPORT(CAM_EFFECT_ENC_COLORINV)|
				CAMERA_FEATURE_SUPPORT(CAM_EFFECT_ENC_SEPIAGREEN)|
				CAMERA_FEATURE_SUPPORT(CAM_EFFECT_ENC_SEPIABLUE)|
				CAMERA_FEATURE_SUPPORT(CAM_EFFECT_ENC_SEPIA));	
		break;
		case ISP_FEATURE_AE_METERING_MODE:
			pSensorFeatureInfo->FeatureSupported = MSDK_FEATURE_NOT_SUPPORTED;
		break;
		case ISP_FEATURE_BRIGHTNESS:
			pSensorFeatureInfo->FeatureType = MSDK_FEATURE_TYPE_RANGE;
			pSensorFeatureInfo->FeatureSupported = (UINT8)(MSDK_SET_GET_FEATURE_SUPPORTED|MSDK_QUERY_CAMERA_VIDEO_SUPPORTED);
			pFeatureRange = (PMSDK_FEATURE_TYPE_RANGE_STRUCT)(&pSensorFeatureInfo->FeatureInformation.FeatureRange);
			pFeatureRange->MinValue = CAM_EV_NEG_4_3;
			pFeatureRange->MaxValue = CAM_EV_POS_4_3;
			pFeatureRange->StepValue = CAMERA_FEATURE_ID_EV_STEP;
			pFeatureRange->DefaultValue = CAM_EV_ZERO;
		break;
		case ISP_FEATURE_BANDING_FREQ:
			pSensorFeatureInfo->FeatureType = MSDK_FEATURE_TYPE_MULTI_SELECTION;
			pSensorFeatureInfo->FeatureSupported = (UINT8)(MSDK_SET_GET_FEATURE_SUPPORTED|MSDK_QUERY_CAMERA_VIDEO_SUPPORTED);
			pFeatureMultiSelection = (PMSDK_FEATURE_TYPE_MULTI_SELECTION_STRUCT)(&pSensorFeatureInfo->FeatureInformation.FeatureMultiSelection);
			pFeatureMultiSelection->TotalSelection = CAM_NO_OF_BANDING;
			pFeatureMultiSelection->DefaultSelection = CAM_BANDING_50HZ;
			pFeatureMultiSelection->SupportedSelection = 
				(CAMERA_FEATURE_SUPPORT(CAM_BANDING_50HZ)|
				CAMERA_FEATURE_SUPPORT(CAM_BANDING_60HZ));
		break;
		case ISP_FEATURE_AF_OPERATION:
			pSensorFeatureInfo->FeatureSupported = MSDK_FEATURE_NOT_SUPPORTED;
		break;
		case ISP_FEATURE_AF_RANGE_CONTROL:
			pSensorFeatureInfo->FeatureSupported = MSDK_FEATURE_NOT_SUPPORTED;
		break;
		case ISP_FEATURE_FLASH:
			pSensorFeatureInfo->FeatureSupported = MSDK_FEATURE_NOT_SUPPORTED;			
		break;
		case ISP_FEATURE_VIDEO_SCENE_MODE:
			pSensorFeatureInfo->FeatureType = MSDK_FEATURE_TYPE_MULTI_SELECTION;
			pSensorFeatureInfo->FeatureSupported = (UINT8)(MSDK_SET_GET_FEATURE_SUPPORTED|MSDK_QUERY_VIDEO_SUPPORTED);
			pFeatureMultiSelection = (PMSDK_FEATURE_TYPE_MULTI_SELECTION_STRUCT)(&pSensorFeatureInfo->FeatureInformation.FeatureMultiSelection);
			pFeatureMultiSelection->TotalSelection = CAM_NO_OF_SCENE_MODE_MAX;
			pFeatureMultiSelection->DefaultSelection = CAM_VIDEO_AUTO_MODE;
			pFeatureMultiSelection->SupportedSelection = 
				(CAMERA_FEATURE_SUPPORT(CAM_VIDEO_AUTO_MODE)|
				CAMERA_FEATURE_SUPPORT(CAM_VIDEO_NIGHT_MODE));
		break;
		case ISP_FEATURE_ISO:
			pSensorFeatureInfo->FeatureSupported = MSDK_FEATURE_NOT_SUPPORTED;			
		break;
		default:
			pSensorFeatureInfo->FeatureSupported = MSDK_FEATURE_NOT_SUPPORTED;			
		break;
	}
}
#endif 

BOOL SP2518_set_param_wb(UINT16 iPara)
{
	SENSORDB("%s\n", __func__);
    kal_uint8  temp_reg;

    //temp_reg=SP2518_read_cmos_sensor(0x3306);

    switch (iPara)
    {
        case AWB_MODE_OFF:
             //   SP2518_write_cmos_sensor(0xfd,0x00);
		//  SP2518_write_cmos_sensor(0x32,0x05);
            break;                     
        case AWB_MODE_AUTO:
                SP2518_write_cmos_sensor(0xfd,0x01);
		  SP2518_write_cmos_sensor(0x28,0xce);
		  SP2518_write_cmos_sensor(0x29,0x8a);		
		  SP2518_write_cmos_sensor(0xfd,0x00);
		  SP2518_write_cmos_sensor(0x32,0x0d);
            break;

        case AWB_MODE_CLOUDY_DAYLIGHT: //cloudy
	         SP2518_write_cmos_sensor(0xfd,0x00);
		  SP2518_write_cmos_sensor(0x32,0x05);
		  SP2518_write_cmos_sensor(0xfd,0x01);
		  SP2518_write_cmos_sensor(0x28,0xe2);
		  SP2518_write_cmos_sensor(0x29,0x82);
		  SP2518_write_cmos_sensor(0xfd,0x00);
            break;

        case AWB_MODE_DAYLIGHT: //sunny
		  SP2518_write_cmos_sensor(0xfd,0x00);
		  SP2518_write_cmos_sensor(0x32,0x05);
		  SP2518_write_cmos_sensor(0xfd,0x01);
		  SP2518_write_cmos_sensor(0x28,0xc1);
		  SP2518_write_cmos_sensor(0x29,0x88);
		  SP2518_write_cmos_sensor(0xfd,0x00);
            break;

        case AWB_MODE_INCANDESCENT: //office
                SP2518_write_cmos_sensor(0xfd,0x00);
		  SP2518_write_cmos_sensor(0x32,0x05);
		  SP2518_write_cmos_sensor(0xfd,0x01);
		  SP2518_write_cmos_sensor(0x28,0x7b);
		  SP2518_write_cmos_sensor(0x29,0xd3);
		  SP2518_write_cmos_sensor(0xfd,0x00);
            break;

        case AWB_MODE_TUNGSTEN: //home
		  SP2518_write_cmos_sensor(0xfd,0x00);
		  SP2518_write_cmos_sensor(0x32,0x05);
		  SP2518_write_cmos_sensor(0xfd,0x01);
		  SP2518_write_cmos_sensor(0x28,0xae);
		  SP2518_write_cmos_sensor(0x29,0xcc);
		  SP2518_write_cmos_sensor(0xfd,0x00);
            break;

        case AWB_MODE_FLUORESCENT:
      		  SP2518_write_cmos_sensor(0xfd,0x00);
		  SP2518_write_cmos_sensor(0x32,0x05);
		  SP2518_write_cmos_sensor(0xfd,0x01);
		  SP2518_write_cmos_sensor(0x28,0xb4);
		  SP2518_write_cmos_sensor(0x29,0xc4);
		  SP2518_write_cmos_sensor(0xfd,0x00);
            break;


        default:
            return FALSE;
    }

    return TRUE;
} /* SP2518_set_param_wb */

BOOL SP2518_set_param_effect(UINT16 iPara)
{
	SENSORDB("%s\n", __func__);
      BOOL  ret = TRUE;

    switch (iPara)
    {
        case MEFFECT_OFF:

		SP2518_write_cmos_sensor(0xfd, 0x00);
		SP2518_write_cmos_sensor(0x62, 0x00);
		SP2518_write_cmos_sensor(0x63, 0x80);
		SP2518_write_cmos_sensor(0x64, 0x80);
	break;

        case MEFFECT_SEPIA:
	     	SP2518_write_cmos_sensor(0xfd, 0x00);
		SP2518_write_cmos_sensor(0x62, 0x10);
		SP2518_write_cmos_sensor(0x63, 0xb0);
		SP2518_write_cmos_sensor(0x64, 0x40);
            break;

        case MEFFECT_NEGATIVE:
		SP2518_write_cmos_sensor(0xfd, 0x00);
		SP2518_write_cmos_sensor(0x62, 0x04);
		SP2518_write_cmos_sensor(0x63, 0x80);
		SP2518_write_cmos_sensor(0x64, 0x80);
            break;

        case MEFFECT_SEPIAGREEN:
		SP2518_write_cmos_sensor(0xfd, 0x00);
		SP2518_write_cmos_sensor(0x62, 0x10);
		SP2518_write_cmos_sensor(0x63, 0x50);
		SP2518_write_cmos_sensor(0x64, 0x50);
            break;

        case MEFFECT_SEPIABLUE:
		SP2518_write_cmos_sensor(0xfd, 0x00);
		SP2518_write_cmos_sensor(0x62, 0x10);
		SP2518_write_cmos_sensor(0x63, 0x80);
		SP2518_write_cmos_sensor(0x64, 0xb0);
            break;
	case MEFFECT_MONO: //B&W
		SP2518_write_cmos_sensor(0xfd, 0x00);
		SP2518_write_cmos_sensor(0x62, 0x20);
		SP2518_write_cmos_sensor(0x63, 0x80);
		SP2518_write_cmos_sensor(0x64, 0x80);
		break;

        default:
            ret = FALSE;
    }

    return ret;

} /* SP2518_set_param_effect */

BOOL SP2518_set_param_banding(UINT16 iPara)
{
  SENSORDB("[SP2518]SP2518SetBanding Para:%d;\n",iPara);// 1==50hz ,0=60hz

	 switch (iPara)
	    {
	        case AE_FLICKER_MODE_50HZ:
	                 SP2518Status.Banding = AE_FLICKER_MODE_50HZ;//AE_FLICKER_MODE_50HZ ==1

	            break;

	        case AE_FLICKER_MODE_60HZ:			
	                 SP2518Status.Banding = AE_FLICKER_MODE_60HZ;//AE_FLICKER_MODE_60HZ==0		                 

	            break;

	          default:
	                 break;
	    }


	SP2518_night_mode(SP2518Status.NightMode);
  return TRUE;
} /* SP2518_set_param_banding */

BOOL SP2518_set_param_exposure(UINT16 iPara)
{
	SENSORDB("%s\n", __func__);
    kal_uint8  temp_reg;
	
    switch (iPara)
    {
        case AE_EV_COMP_n13:
            	       SP2518_write_cmos_sensor(0xfd,0x00);
		       SP2518_write_cmos_sensor(0xdc,0xc0);
            break;

        case AE_EV_COMP_n10:    
            		SP2518_write_cmos_sensor(0xfd,0x00);
		       SP2518_write_cmos_sensor(0xdc,0xd0);
            break;

        case AE_EV_COMP_n07:

            		SP2518_write_cmos_sensor(0xfd,0x00);
		       SP2518_write_cmos_sensor(0xdc,0xe0);
		break;
        case AE_EV_COMP_n03:
            		SP2518_write_cmos_sensor(0xfd,0x00);
		       SP2518_write_cmos_sensor(0xdc,0xf0);
            break;

        case AE_EV_COMP_00:
           		SP2518_write_cmos_sensor(0xfd,0x00);
		       SP2518_write_cmos_sensor(0xdc,0x00);
            break;

        case AE_EV_COMP_03:
            		SP2518_write_cmos_sensor(0xfd,0x00);
		       SP2518_write_cmos_sensor(0xdc,0x10);
            break;

        case AE_EV_COMP_07:
            		SP2518_write_cmos_sensor(0xfd,0x00);
		       SP2518_write_cmos_sensor(0xdc,0x20);
            break;

        case AE_EV_COMP_10:
            		SP2518_write_cmos_sensor(0xfd,0x00);
		       SP2518_write_cmos_sensor(0xdc,0x30);
            break;

        case AE_EV_COMP_13:
            		SP2518_write_cmos_sensor(0xfd,0x00);
		       SP2518_write_cmos_sensor(0xdc,0x40);
            break;

        default:
            return FALSE;
    }

    return TRUE;
} /* SP2518_set_param_exposure */



UINT32 SP2518YUVSensorSetting(FEATURE_ID iCmd, UINT32 iPara)
{

  //if( SP2518_sensor_cap_state == KAL_TRUE)
	//   return TRUE;

	switch (iCmd) {
	case FID_SCENE_MODE:	    
//	    printk("Set Scene Mode:%d\n", iPara); 
	    if (iPara == SCENE_MODE_OFF)
	    {
	        SP2518_night_mode(0); 
	    }
	    else if (iPara == SCENE_MODE_NIGHTSCENE)
	    {
               SP2518_night_mode(1); 
	    }	    
	    break; 	    
	case FID_AWB_MODE:
//	    printk("Set AWB Mode:%d\n", iPara); 	    
           SP2518_set_param_wb(iPara);
	break;
	case FID_COLOR_EFFECT:
//	    printk("Set Color Effect:%d\n", iPara); 	    	    
           SP2518_set_param_effect(iPara);
	break;
	case FID_AE_EV:
#if WINMO_USE	    
	case ISP_FEATURE_EXPOSURE:
#endif 	    
//           printk("Set EV:%d\n", iPara); 	    	    
           SP2518_set_param_exposure(iPara);
	break;
	case FID_AE_FLICKER:
//           printk("Set Flicker:%d\n", iPara); 	    	    	    
           SP2518_set_param_banding(iPara);
	break;
        case FID_AE_SCENE_MODE: 

           if (iPara == AE_MODE_OFF) 
		{
                   SP2518_AE_ENABLE = KAL_FALSE; 
             }
            else 
		{
                   SP2518_AE_ENABLE = KAL_TRUE; 
	      }
            SP2518SetAeMode(SP2518_AE_ENABLE);
			
            break; 
	case FID_ZOOM_FACTOR:
	    zoom_factor = iPara; 
	    SP2518Status.ZoomFactor = iPara;
        break; 
	default:
	break;
	}
	return TRUE;
}   /* SP2518YUVSensorSetting */

UINT32 SP2518YUVSetVideoMode(UINT16 u2FrameRate)
{
     /*  kal_uint8 iTemp;

    iTemp = SP2518_read_cmos_sensor(0x3014);
    SP2518_write_cmos_sensor(0x3014, iTemp & 0xf7); //Disable night mode

    if (u2FrameRate == 30)
    {
        SP2518_write_cmos_sensor(0x302d, 0x00);
        SP2518_write_cmos_sensor(0x302e, 0x00);
    }
    else if (u2FrameRate == 15)       
    {
        SP2518_write_cmos_sensor(0x300e, 0x34);
        SP2518_write_cmos_sensor(0x302A, SP2518_VIDEO_15FPS_FRAME_LENGTH>>8);  //  15fps
        SP2518_write_cmos_sensor(0x302B, SP2518_VIDEO_15FPS_FRAME_LENGTH&0xFF);
                
        // clear extra exposure line
        SP2518_write_cmos_sensor(0x302d, 0x00);
        SP2518_write_cmos_sensor(0x302e, 0x00);   
    }
    else 
    {
        printk("Wrong frame rate setting \n");
    }
    SP2518_VEDIO_encode_mode = KAL_TRUE; 
    */    
     SP2518_VEDIO_encode_mode = KAL_TRUE; 
    SP2518Status.Videomode = KAL_TRUE; 
    return TRUE;
}

UINT32 SP2518FeatureControl(MSDK_SENSOR_FEATURE_ENUM FeatureId,
							 UINT8 *pFeaturePara,UINT32 *pFeatureParaLen)
{
	UINT16 *pFeatureReturnPara16=(UINT16 *) pFeaturePara;
	UINT16 *pFeatureData16=(UINT16 *) pFeaturePara;
	UINT32 *pFeatureReturnPara32=(UINT32 *) pFeaturePara;
	UINT32 *pFeatureData32=(UINT32 *) pFeaturePara;
	MSDK_SENSOR_CONFIG_STRUCT *pSensorConfigData=(MSDK_SENSOR_CONFIG_STRUCT *) pFeaturePara;
	MSDK_SENSOR_REG_INFO_STRUCT *pSensorRegData=(MSDK_SENSOR_REG_INFO_STRUCT *) pFeaturePara;

#if WINMO_USE	
	PMSDK_FEATURE_INFO_STRUCT pSensorFeatureInfo=(PMSDK_FEATURE_INFO_STRUCT) pFeaturePara;
#endif 

	switch (FeatureId)
	{
		case SENSOR_FEATURE_GET_RESOLUTION:
			*pFeatureReturnPara16++=SP2518_IMAGE_SENSOR_FULL_WIDTH;
			*pFeatureReturnPara16=SP2518_IMAGE_SENSOR_FULL_HEIGHT;
			*pFeatureParaLen=4;
		break;
		case SENSOR_FEATURE_GET_PERIOD:
			*pFeatureReturnPara16++=SP2518_FULL_PERIOD_PIXEL_NUMS+SP2518Status.PvDummyPixels;
			*pFeatureReturnPara16=SP2518_PV_PERIOD_LINE_NUMS+SP2518Status.PvDummyLines;
			*pFeatureParaLen=4;
		break;
		case SENSOR_FEATURE_GET_PIXEL_CLOCK_FREQ:
			*pFeatureReturnPara32 =  SP2518Status.PvOpClk*2;
			*pFeatureParaLen=4;
		break;
		case SENSOR_FEATURE_SET_ESHUTTER:
		break;
		case SENSOR_FEATURE_SET_NIGHTMODE:
			SP2518_night_mode((BOOL) *pFeatureData16);
		break;
		case SENSOR_FEATURE_SET_GAIN:
		case SENSOR_FEATURE_SET_FLASHLIGHT:
		break;
		case SENSOR_FEATURE_SET_ISP_MASTER_CLOCK_FREQ:
			SP2518_isp_master_clock=*pFeatureData32;
		break;
		case SENSOR_FEATURE_SET_REGISTER:
			SP2518_write_cmos_sensor(pSensorRegData->RegAddr, pSensorRegData->RegData);
		break;
		case SENSOR_FEATURE_GET_REGISTER:
			pSensorRegData->RegData = SP2518_read_cmos_sensor(pSensorRegData->RegAddr);
		break;
		case SENSOR_FEATURE_GET_CONFIG_PARA:
			memcpy(pSensorConfigData, &SP2518SensorConfigData, sizeof(MSDK_SENSOR_CONFIG_STRUCT));
			*pFeatureParaLen=sizeof(MSDK_SENSOR_CONFIG_STRUCT);
		break;
		case SENSOR_FEATURE_SET_CCT_REGISTER:
		case SENSOR_FEATURE_GET_CCT_REGISTER:
		case SENSOR_FEATURE_SET_ENG_REGISTER:
		case SENSOR_FEATURE_GET_ENG_REGISTER:
		case SENSOR_FEATURE_GET_REGISTER_DEFAULT:

		case SENSOR_FEATURE_CAMERA_PARA_TO_SENSOR:
		case SENSOR_FEATURE_SENSOR_TO_CAMERA_PARA:
		case SENSOR_FEATURE_GET_GROUP_INFO:
		case SENSOR_FEATURE_GET_ITEM_INFO:
		case SENSOR_FEATURE_SET_ITEM_INFO:
		case SENSOR_FEATURE_GET_ENG_INFO:
		break;
		case SENSOR_FEATURE_GET_GROUP_COUNT:
                        *pFeatureReturnPara32++=0;
                        *pFeatureParaLen=4;	    
		    break; 
		case SENSOR_FEATURE_GET_LENS_DRIVER_ID:
			// get the lens driver ID from EEPROM or just return LENS_DRIVER_ID_DO_NOT_CARE
			// if EEPROM does not exist in camera module.
			*pFeatureReturnPara32=LENS_DRIVER_ID_DO_NOT_CARE;
			*pFeatureParaLen=4;
		break;
		case SENSOR_FEATURE_CHECK_SENSOR_ID:
			 SP2518_GetSensorID(pFeatureData32);
		 break;
		case SENSOR_FEATURE_SET_YUV_CMD:
//		       printk("SP2518 YUV sensor Setting:%d, %d \n", *pFeatureData32,  *(pFeatureData32+1));
			SP2518YUVSensorSetting((FEATURE_ID)*pFeatureData32, *(pFeatureData32+1));
		break;
#if WINMO_USE		    		
		case SENSOR_FEATURE_QUERY:
			SP2518Query(pSensorFeatureInfo);
			*pFeatureParaLen = sizeof(MSDK_FEATURE_INFO_STRUCT);
		break;		
		case SENSOR_FEATURE_SET_YUV_CAPTURE_RAW_SUPPORT:
			/* update yuv capture raw support flag by *pFeatureData16 */
		break;		
#endif 			
		case SENSOR_FEATURE_SET_VIDEO_MODE:
		       SP2518YUVSetVideoMode(*pFeatureData16);
		       break; 
		default:
			break;			
	}
	return ERROR_NONE;
}	/* SP2518FeatureControl() */


SENSOR_FUNCTION_STRUCT	SensorFuncSP2518=
{
	SP2518Open,
	SP2518GetInfo,
	SP2518GetResolution,
	SP2518FeatureControl,
	SP2518Control,
	SP2518Close
};

UINT32 SP2518_YUV_SensorInit(PSENSOR_FUNCTION_STRUCT *pfFunc)
{
	/* To Do : Check Sensor status here */
	printk("SP2518 22222222222222222Sensor id =\n");
	if (pfFunc!=NULL)
		*pfFunc=&SensorFuncSP2518;

	return ERROR_NONE;
}	/* SensorInit() */


