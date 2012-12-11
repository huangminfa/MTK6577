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
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by CC/CQ. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#include <linux/videodev2.h>
#include <linux/i2c.h>
#include <linux/platform_device.h>
#include <linux/delay.h>
#include <linux/cdev.h>
#include <linux/uaccess.h>
#include <linux/fs.h>
#include <asm/atomic.h>
//#include <mach/mt6573_pll.h>
//#include <asm/io.h>

#include "kd_camera_hw.h"
#include "kd_imgsensor.h"
#include "kd_imgsensor_define.h"
#include "kd_imgsensor_errcode.h"
#include "kd_camera_feature.h"

#include "gc0309yuv_Sensor.h"
#include "gc0309yuv_Camera_Sensor_para.h"
#include "gc0309yuv_CameraCustomized.h"


#define GC0309YUV_DEBUG
#ifdef GC0309YUV_DEBUG
#define SENSORDB printk
#else
#define SENSORDB(x,...)
#endif

#define GC0309_SET_PAGE0 GC0309_write_cmos_sensor(0xfe , 0x00)
#define GC0309_SET_PAGE1 GC0309_write_cmos_sensor(0xfe , 0x01)

#if 0
extern int iReadReg(u16 a_u2Addr , u8 * a_puBuff , u16 i2cId);
extern int iWriteReg(u16 a_u2Addr , u32 a_u4Data , u32 a_u4Bytes , u16 i2cId);
static int sensor_id_fail = 0; 
#define GC0309_write_cmos_sensor(addr, para) iWriteReg((u16) addr , (u32) para ,1,GC0309_WRITE_ID)
#define GC0309_write_cmos_sensor_2(addr, para, bytes) iWriteReg((u16) addr , (u32) para ,bytes,GC0309_WRITE_ID)
kal_uint16 GC0309_read_cmos_sensor(kal_uint32 addr)
{
kal_uint16 get_byte=0;
    iReadReg((u16) addr ,(u8*)&get_byte,GC0309_WRITE_ID);
    return get_byte;
}
#endif

extern CAMERA_DUAL_CAMERA_SENSOR_ENUM g_currDualSensorIdx;

extern int iReadRegI2C(u8 *a_pSendData , u16 a_sizeSendData, u8 * a_pRecvData, u16 a_sizeRecvData, u16 i2cId);
extern int iWriteRegI2C(u8 *a_pSendData , u16 a_sizeSendData, u16 i2cId);
kal_uint16 GC0309_write_cmos_sensor(kal_uint8 addr, kal_uint8 para)
{
    char puSendCmd[2] = {(char)(addr & 0xFF) , (char)(para & 0xFF)};
	
	iWriteRegI2C(puSendCmd , 2,GC0309_WRITE_ID);

}
kal_uint16 GC0309_read_cmos_sensor(kal_uint8 addr)
{
	kal_uint16 get_byte=0;
    char puSendCmd = { (char)(addr & 0xFF) };
	iReadRegI2C(&puSendCmd , 1, (u8*)&get_byte,1,GC0309_READ_ID);
	
    return get_byte;
}


/*******************************************************************************
* // Adapter for Winmo typedef 
********************************************************************************/
#define WINMO_USE 0

#define Sleep(ms) mdelay(ms)
#define RETAILMSG(x,...)
#define TEXT


/*******************************************************************************
* follow is define by jun
********************************************************************************/
MSDK_SENSOR_CONFIG_STRUCT GC0309SensorConfigData;

static struct GC0309_sensor_STRUCT GC0309_sensor;
static kal_uint32 GC0309_zoom_factor = 0; 
static int sensor_id_fail = 0;	

static void GC0309GammaSelect(kal_uint32 GammaLvl)
{
	return;
	switch(GammaLvl)
	{
		case 1:                                             //smallest gamma curve
			GC0309_write_cmos_sensor(0x9F, 0x0B); 
			GC0309_write_cmos_sensor(0xA0, 0x16); 
			GC0309_write_cmos_sensor(0xA1, 0x29); 
			GC0309_write_cmos_sensor(0xA2, 0x3C); 
			GC0309_write_cmos_sensor(0xA3, 0x4F); 
			GC0309_write_cmos_sensor(0xA4, 0x5F); 
			GC0309_write_cmos_sensor(0xA5, 0x6F); 
			GC0309_write_cmos_sensor(0xA6, 0x8A); 
			GC0309_write_cmos_sensor(0xA7, 0x9F); 
			GC0309_write_cmos_sensor(0xA8, 0xB4); 
			GC0309_write_cmos_sensor(0xA9, 0xC6); 
			GC0309_write_cmos_sensor(0xAA, 0xD3); 
			GC0309_write_cmos_sensor(0xAB, 0xDD);  
			GC0309_write_cmos_sensor(0xAC, 0xE5);  
			GC0309_write_cmos_sensor(0xAD, 0xF1); 
			GC0309_write_cmos_sensor(0xAE, 0xFA); 
			GC0309_write_cmos_sensor(0xAF, 0xFF); 	
			break;
		case 2:			
			GC0309_write_cmos_sensor(0x9F, 0x0E); 
			GC0309_write_cmos_sensor(0xA0, 0x1C); 
			GC0309_write_cmos_sensor(0xA1, 0x34); 
			GC0309_write_cmos_sensor(0xA2, 0x48); 
			GC0309_write_cmos_sensor(0xA3, 0x5A); 
			GC0309_write_cmos_sensor(0xA4, 0x6B); 
			GC0309_write_cmos_sensor(0xA5, 0x7B); 
			GC0309_write_cmos_sensor(0xA6, 0x95); 
			GC0309_write_cmos_sensor(0xA7, 0xAB); 
			GC0309_write_cmos_sensor(0xA8, 0xBF);
			GC0309_write_cmos_sensor(0xA9, 0xCE); 
			GC0309_write_cmos_sensor(0xAA, 0xD9); 
			GC0309_write_cmos_sensor(0xAB, 0xE4);  
			GC0309_write_cmos_sensor(0xAC, 0xEC); 
			GC0309_write_cmos_sensor(0xAD, 0xF7); 
			GC0309_write_cmos_sensor(0xAE, 0xFD); 
			GC0309_write_cmos_sensor(0xAF, 0xFF); 
		break;
		case 3:
			GC0309_write_cmos_sensor(0x9F, 0x10); 
			GC0309_write_cmos_sensor(0xA0, 0x20); 
			GC0309_write_cmos_sensor(0xA1, 0x38); 
			GC0309_write_cmos_sensor(0xA2, 0x4E); 
			GC0309_write_cmos_sensor(0xA3, 0x63); 
			GC0309_write_cmos_sensor(0xA4, 0x76); 
			GC0309_write_cmos_sensor(0xA5, 0x87); 
			GC0309_write_cmos_sensor(0xA6, 0xA2); 
			GC0309_write_cmos_sensor(0xA7, 0xB8); 
			GC0309_write_cmos_sensor(0xA8, 0xCA); 
			GC0309_write_cmos_sensor(0xA9, 0xD8); 
			GC0309_write_cmos_sensor(0xAA, 0xE3); 
			GC0309_write_cmos_sensor(0xAB, 0xEB); 
			GC0309_write_cmos_sensor(0xAC, 0xF0); 
			GC0309_write_cmos_sensor(0xAD, 0xF8); 
			GC0309_write_cmos_sensor(0xAE, 0xFD); 
			GC0309_write_cmos_sensor(0xAF, 0xFF); 

			break;
		case 4:
			GC0309_write_cmos_sensor(0x9F, 0x14); 
			GC0309_write_cmos_sensor(0xA0, 0x28); 
			GC0309_write_cmos_sensor(0xA1, 0x44); 
			GC0309_write_cmos_sensor(0xA2, 0x5D); 
			GC0309_write_cmos_sensor(0xA3, 0x72); 
			GC0309_write_cmos_sensor(0xA4, 0x86); 
			GC0309_write_cmos_sensor(0xA5, 0x95); 
			GC0309_write_cmos_sensor(0xA6, 0xB1); 
			GC0309_write_cmos_sensor(0xA7, 0xC6); 
			GC0309_write_cmos_sensor(0xA8, 0xD5); 
			GC0309_write_cmos_sensor(0xA9, 0xE1); 
			GC0309_write_cmos_sensor(0xAA, 0xEA); 
			GC0309_write_cmos_sensor(0xAB, 0xF1); 
			GC0309_write_cmos_sensor(0xAC, 0xF5); 
			GC0309_write_cmos_sensor(0xAD, 0xFB); 
			GC0309_write_cmos_sensor(0xAE, 0xFE); 
			GC0309_write_cmos_sensor(0xAF, 0xFF);
		break;
		case 5:								// largest gamma curve
			GC0309_write_cmos_sensor(0x9F, 0x15); 
			GC0309_write_cmos_sensor(0xA0, 0x2A); 
			GC0309_write_cmos_sensor(0xA1, 0x4A); 
			GC0309_write_cmos_sensor(0xA2, 0x67); 
			GC0309_write_cmos_sensor(0xA3, 0x79); 
			GC0309_write_cmos_sensor(0xA4, 0x8C); 
			GC0309_write_cmos_sensor(0xA5, 0x9A); 
			GC0309_write_cmos_sensor(0xA6, 0xB3); 
			GC0309_write_cmos_sensor(0xA7, 0xC5); 
			GC0309_write_cmos_sensor(0xA8, 0xD5); 
			GC0309_write_cmos_sensor(0xA9, 0xDF); 
			GC0309_write_cmos_sensor(0xAA, 0xE8); 
			GC0309_write_cmos_sensor(0xAB, 0xEE); 
			GC0309_write_cmos_sensor(0xAC, 0xF3); 
			GC0309_write_cmos_sensor(0xAD, 0xFA); 
			GC0309_write_cmos_sensor(0xAE, 0xFD); 
			GC0309_write_cmos_sensor(0xAF, 0xFF);
			break;
		default:
		break;
	}
}

static void GC0309_Initial_Setting(void)
{
	GC0309_write_cmos_sensor(0xfe,0x80);	 // soft reset	
		
	GC0309_SET_PAGE0;		// set page0
	
	GC0309_write_cmos_sensor(0x1a,0x16);		
	GC0309_write_cmos_sensor(0xd2,0x10);	 // close AEC
	GC0309_write_cmos_sensor(0x22,0x55);	 // close AWB

	GC0309_write_cmos_sensor(0x5a,0x56); 
	GC0309_write_cmos_sensor(0x5b,0x40);
	GC0309_write_cmos_sensor(0x5c,0x4a);			

	GC0309_write_cmos_sensor(0x22,0x57); 
		
	GC0309_write_cmos_sensor(0x01,0xfa); 
	GC0309_write_cmos_sensor(0x02,0x70); 
	GC0309_write_cmos_sensor(0x0f,0x01); 

	GC0309_write_cmos_sensor(0xe2,0x00); 
	GC0309_write_cmos_sensor(0xe3,0x64); 

	GC0309_write_cmos_sensor(0x03,0x01); 
	GC0309_write_cmos_sensor(0x04,0x2c); 

	/*
	GC0309_write_cmos_sensor(0x01,0x6a); 
	GC0309_write_cmos_sensor(0x02,0x25); 
	GC0309_write_cmos_sensor(0x0f,0x00);

	GC0309_write_cmos_sensor(0xe2,0x00); 
	GC0309_write_cmos_sensor(0xe3,0x4b); 
		
	GC0309_write_cmos_sensor(0xe4,0x02); 
	GC0309_write_cmos_sensor(0xe5,0x0d); 
	GC0309_write_cmos_sensor(0xe6,0x02); 
	GC0309_write_cmos_sensor(0xe7,0x0d); 
	GC0309_write_cmos_sensor(0xe8,0x02); 
	GC0309_write_cmos_sensor(0xe9,0x0d); 
	GC0309_write_cmos_sensor(0xea,0x05); 
	GC0309_write_cmos_sensor(0xeb,0xdc); 
	*/

	GC0309_write_cmos_sensor(0x05,0x00);
	GC0309_write_cmos_sensor(0x06,0x00);
	GC0309_write_cmos_sensor(0x07,0x00); 
	GC0309_write_cmos_sensor(0x08,0x00); 
	GC0309_write_cmos_sensor(0x09,0x01); 
	GC0309_write_cmos_sensor(0x0a,0xe8); 
	GC0309_write_cmos_sensor(0x0b,0x02); 
	GC0309_write_cmos_sensor(0x0c,0x88); 
	GC0309_write_cmos_sensor(0x0d,0x02); 
	GC0309_write_cmos_sensor(0x0e,0x02); 
	GC0309_write_cmos_sensor(0x10,0x22); 
	GC0309_write_cmos_sensor(0x11,0x0d); 
	GC0309_write_cmos_sensor(0x12,0x2a); 
	GC0309_write_cmos_sensor(0x13,0x00); 

	if (DUAL_CAMERA_MAIN_SENSOR == g_currDualSensorIdx || DUAL_CAMERA_MAIN_SECOND_SENSOR == g_currDualSensorIdx)
	{
		GC0309_write_cmos_sensor(0x14,0x10);	//  0x10
	}
	else
	{
		GC0309_write_cmos_sensor(0x14,0x13);	//  0x10
	}
	GC0309_write_cmos_sensor(0x15,0x0a); 
	GC0309_write_cmos_sensor(0x16,0x05); 
	GC0309_write_cmos_sensor(0x17,0x01); 

	GC0309_write_cmos_sensor(0x1b,0x03); 
	GC0309_write_cmos_sensor(0x1c,0xc1); 
	GC0309_write_cmos_sensor(0x1d,0x08); 
	GC0309_write_cmos_sensor(0x1e,0x20);
	GC0309_write_cmos_sensor(0x1f,0x16); 

	GC0309_write_cmos_sensor(0x20,0xff); 
	GC0309_write_cmos_sensor(0x21,0xf8); 
	GC0309_write_cmos_sensor(0x24,0xa2); 
	GC0309_write_cmos_sensor(0x25,0x0f);
	//output sync_mode
	GC0309_write_cmos_sensor(0x26,0x02); 
	GC0309_write_cmos_sensor(0x2f,0x01); 
	/////////////////////////////////////////////////////////////////////
	/////////////////////////// grab_t ////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	GC0309_write_cmos_sensor(0x30,0xf7); 
	GC0309_write_cmos_sensor(0x31,0x40);
	GC0309_write_cmos_sensor(0x32,0x00); 
	GC0309_write_cmos_sensor(0x39,0x04); 
	GC0309_write_cmos_sensor(0x3a,0x20); 
	GC0309_write_cmos_sensor(0x3b,0x20); 
	GC0309_write_cmos_sensor(0x3c,0x02); 
	GC0309_write_cmos_sensor(0x3d,0x02); 
	GC0309_write_cmos_sensor(0x3e,0x02);
	GC0309_write_cmos_sensor(0x3f,0x02); 
	
	//gain
	GC0309_write_cmos_sensor(0x50,0x24); 
	
	GC0309_write_cmos_sensor(0x53,0x82); 
	GC0309_write_cmos_sensor(0x54,0x80); 
	GC0309_write_cmos_sensor(0x55,0x80); 
	GC0309_write_cmos_sensor(0x56,0x82); 
	
	/////////////////////////////////////////////////////////////////////
	/////////////////////////// LSC_t  ////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	GC0309_write_cmos_sensor(0x8b,0x20); 
	GC0309_write_cmos_sensor(0x8c,0x20); 
	GC0309_write_cmos_sensor(0x8d,0x20); 
	GC0309_write_cmos_sensor(0x8e,0x10); 
	GC0309_write_cmos_sensor(0x8f,0x10); 
	GC0309_write_cmos_sensor(0x90,0x10); 
	GC0309_write_cmos_sensor(0x91,0x3c); 
	GC0309_write_cmos_sensor(0x92,0x50); 
	GC0309_write_cmos_sensor(0x5d,0x12); 
	GC0309_write_cmos_sensor(0x5e,0x1a); 
	GC0309_write_cmos_sensor(0x5f,0x24); 
	/////////////////////////////////////////////////////////////////////
	/////////////////////////// DNDD_t	///////////////////////////////
	/////////////////////////////////////////////////////////////////////
	GC0309_write_cmos_sensor(0x60,0x07); 
	GC0309_write_cmos_sensor(0x61,0x0e); 
	GC0309_write_cmos_sensor(0x62,0x0c); 
	GC0309_write_cmos_sensor(0x64,0x03); 
	GC0309_write_cmos_sensor(0x66,0xe8); 
	GC0309_write_cmos_sensor(0x67,0x86); 
	GC0309_write_cmos_sensor(0x68,0xa2); 
	
	/////////////////////////////////////////////////////////////////////
	/////////////////////////// asde_t ///////////////////////////////
	/////////////////////////////////////////////////////////////////////
	GC0309_write_cmos_sensor(0x69,0x20); 
	GC0309_write_cmos_sensor(0x6a,0x0f); 
	GC0309_write_cmos_sensor(0x6b,0x00); 
	GC0309_write_cmos_sensor(0x6c,0x53); 
	GC0309_write_cmos_sensor(0x6d,0x83); 
	GC0309_write_cmos_sensor(0x6e,0xac); 
	GC0309_write_cmos_sensor(0x6f,0xac); 
	GC0309_write_cmos_sensor(0x70,0x15); 
	GC0309_write_cmos_sensor(0x71,0x33); 
	/////////////////////////////////////////////////////////////////////
	/////////////////////////// eeintp_t///////////////////////////////
	/////////////////////////////////////////////////////////////////////
	GC0309_write_cmos_sensor(0x72,0xdc);	
	GC0309_write_cmos_sensor(0x73,0x80);	
	//for high resolution in light scene
	GC0309_write_cmos_sensor(0x74,0x02); 
	GC0309_write_cmos_sensor(0x75,0x3f); 
	GC0309_write_cmos_sensor(0x76,0x02); 
	GC0309_write_cmos_sensor(0x77,0x54); 
	GC0309_write_cmos_sensor(0x78,0x88); 
	GC0309_write_cmos_sensor(0x79,0x81); 
	GC0309_write_cmos_sensor(0x7a,0x81); 
	GC0309_write_cmos_sensor(0x7b,0x22); 
	GC0309_write_cmos_sensor(0x7c,0xff);
	
	
	/////////////////////////////////////////////////////////////////////
	///////////////////////////CC_t///////////////////////////////
	/////////////////////////////////////////////////////////////////////
	GC0309_write_cmos_sensor(0x93,0x45); 
	GC0309_write_cmos_sensor(0x94,0x00); 
	GC0309_write_cmos_sensor(0x95,0x00); 
	GC0309_write_cmos_sensor(0x96,0x00); 
	GC0309_write_cmos_sensor(0x97,0x45); 
	GC0309_write_cmos_sensor(0x98,0xf0); 
	GC0309_write_cmos_sensor(0x9c,0x00); 
	GC0309_write_cmos_sensor(0x9d,0x03); 
	GC0309_write_cmos_sensor(0x9e,0x00); 

	
	
	/////////////////////////////////////////////////////////////////////
	///////////////////////////YCP_t///////////////////////////////
	/////////////////////////////////////////////////////////////////////
	GC0309_write_cmos_sensor(0xb1,0x40); 
	GC0309_write_cmos_sensor(0xb2,0x40); 
	GC0309_write_cmos_sensor(0xb8,0x20); 
	GC0309_write_cmos_sensor(0xbe,0x36); 
	GC0309_write_cmos_sensor(0xbf,0x00); 
	/////////////////////////////////////////////////////////////////////
	///////////////////////////AEC_t///////////////////////////////
	/////////////////////////////////////////////////////////////////////
	GC0309_write_cmos_sensor(0xd0,0xc9);	
	GC0309_write_cmos_sensor(0xd1,0x10);	
//	GC0309_write_cmos_sensor(0xd2,0x90);	
	GC0309_write_cmos_sensor(0xd3,0x80);	
	GC0309_write_cmos_sensor(0xd5,0xf2); 
	GC0309_write_cmos_sensor(0xd6,0x16);	
	GC0309_write_cmos_sensor(0xdb,0x92); 
	GC0309_write_cmos_sensor(0xdc,0xa5);	
	GC0309_write_cmos_sensor(0xdf,0x23);	 
	GC0309_write_cmos_sensor(0xd9,0x00);	
	GC0309_write_cmos_sensor(0xda,0x00);	
	GC0309_write_cmos_sensor(0xe0,0x09);

	GC0309_write_cmos_sensor(0xec,0x00);	
	GC0309_write_cmos_sensor(0xed,0x04);	
	GC0309_write_cmos_sensor(0xee,0xa0);	
	GC0309_write_cmos_sensor(0xef,0x40);	
	///////////////////////////////////////////////////////////////////
	///////////////////////////GAMMA//////////////////////////////////
	///////////////////////////////////////////////////////////////////
#if 0	
	GC0309_write_cmos_sensor(0x9F,0x0F);			 
	GC0309_write_cmos_sensor(0xA0,0x1D);	
	GC0309_write_cmos_sensor(0xA1,0x2D);
	GC0309_write_cmos_sensor(0xA2,0x3B);
	GC0309_write_cmos_sensor(0xA3,0x46);
	GC0309_write_cmos_sensor(0xA4,0x50);
	GC0309_write_cmos_sensor(0xA5,0x5A);
	GC0309_write_cmos_sensor(0xA6,0x6B);
	GC0309_write_cmos_sensor(0xA7,0x7B);
	GC0309_write_cmos_sensor(0xA8,0x8A);
	GC0309_write_cmos_sensor(0xA9,0x98);
	GC0309_write_cmos_sensor(0xAA,0xA5);
	GC0309_write_cmos_sensor(0xAB,0xB2);
	GC0309_write_cmos_sensor(0xAC,0xBE);
	GC0309_write_cmos_sensor(0xAD,0xD5);
	GC0309_write_cmos_sensor(0xAE,0xEB);
	GC0309_write_cmos_sensor(0xAF,0xFE);
#endif
	//Y_gamma
	GC0309_write_cmos_sensor(0xc0,0x00);
	GC0309_write_cmos_sensor(0xc1,0x0B);
	GC0309_write_cmos_sensor(0xc2,0x15);
	GC0309_write_cmos_sensor(0xc3,0x27);
	GC0309_write_cmos_sensor(0xc4,0x39);
	GC0309_write_cmos_sensor(0xc5,0x49);
	GC0309_write_cmos_sensor(0xc6,0x5A);
	GC0309_write_cmos_sensor(0xc7,0x6A);
	GC0309_write_cmos_sensor(0xc8,0x89);
	GC0309_write_cmos_sensor(0xc9,0xA8);
	GC0309_write_cmos_sensor(0xca,0xC6);
	GC0309_write_cmos_sensor(0xcb,0xE3);
	GC0309_write_cmos_sensor(0xcc,0xFF);

	/////////////////////////////////////////////////////////////////
	/////////////////////////// ABS_t ///////////////////////////////
	/////////////////////////////////////////////////////////////////
	GC0309_write_cmos_sensor(0xf0,0x02);
	GC0309_write_cmos_sensor(0xf1,0x01);
	GC0309_write_cmos_sensor(0xf2,0x00); 
	GC0309_write_cmos_sensor(0xf3,0x30); 
	
	/////////////////////////////////////////////////////////////////
	/////////////////////////// Measure Window ///////////////////////
	/////////////////////////////////////////////////////////////////
	GC0309_write_cmos_sensor(0xf7,0x04); 
	GC0309_write_cmos_sensor(0xf8,0x02); 
	GC0309_write_cmos_sensor(0xf9,0x9f);
	GC0309_write_cmos_sensor(0xfa,0x78);

	//---------------------------------------------------------------
	GC0309_SET_PAGE1;

	/////////////////////////////////////////////////////////////////
	///////////////////////////AWB_p/////////////////////////////////
	/////////////////////////////////////////////////////////////////
	GC0309_write_cmos_sensor(0x00,0xf5); 
	//GC0309_write_cmos_sensor(0x01,0x0a);  
	GC0309_write_cmos_sensor(0x02,0x1a); 
	GC0309_write_cmos_sensor(0x0a,0xa0); 
	GC0309_write_cmos_sensor(0x0b,0x60); 
	GC0309_write_cmos_sensor(0x0c,0x08);
	GC0309_write_cmos_sensor(0x0e,0x4c); 
	GC0309_write_cmos_sensor(0x0f,0x39); 
	GC0309_write_cmos_sensor(0x11,0x3f); 
	GC0309_write_cmos_sensor(0x12,0x72); 
	GC0309_write_cmos_sensor(0x13,0x13); 
	GC0309_write_cmos_sensor(0x14,0x42);	
	GC0309_write_cmos_sensor(0x15,0x43); 
	GC0309_write_cmos_sensor(0x16,0xc2); 
	GC0309_write_cmos_sensor(0x17,0xa8); 
	GC0309_write_cmos_sensor(0x18,0x18);	
	GC0309_write_cmos_sensor(0x19,0x40);	
	GC0309_write_cmos_sensor(0x1a,0xd0); 
	GC0309_write_cmos_sensor(0x1b,0xf5);	

	GC0309_write_cmos_sensor(0x70,0x40); 
	GC0309_write_cmos_sensor(0x71,0x58);	
	GC0309_write_cmos_sensor(0x72,0x30);	
	GC0309_write_cmos_sensor(0x73,0x48);	
	GC0309_write_cmos_sensor(0x74,0x20);	
	GC0309_write_cmos_sensor(0x75,0x60);	
	
	GC0309_SET_PAGE0;

	GC0309_write_cmos_sensor(0xd2,0x90);  // Open AEC at last.  
}

void GC0309MoreSetting(void)
{
	//  TODO: FAE Modify the Init Regs here!!! 
/******* 5/11 daemon update for image performance *****************/
	GC0309_write_cmos_sensor(0x8b,0x22);   // lsc r 
	GC0309_write_cmos_sensor(0x71,0x43);   // auto sat limit
	
	//cc
	GC0309_write_cmos_sensor(0x93,0x48); 
	GC0309_write_cmos_sensor(0x94,0x00); 
	GC0309_write_cmos_sensor(0x95,0x05); 
	GC0309_write_cmos_sensor(0x96,0xe8); 
	GC0309_write_cmos_sensor(0x97,0x40); 
	GC0309_write_cmos_sensor(0x98,0xf8); 
	GC0309_write_cmos_sensor(0x9c,0x00); 
	GC0309_write_cmos_sensor(0x9d,0x00); 
	GC0309_write_cmos_sensor(0x9e,0x00); 
	
	GC0309_write_cmos_sensor(0xd0,0xcb);  // aec before gamma
	GC0309_write_cmos_sensor(0xd3,0x50);  // ae target
	
//	GC0309_SET_PAGE1;
	// awb update
//	GC0309_write_cmos_sensor(0x02,0x20); 
//	GC0309_write_cmos_sensor(0x04,0x06);
//	GC0309_write_cmos_sensor(0x05,0x20);
//	GC0309_write_cmos_sensor(0x06,0x20);
//	GC0309_write_cmos_sensor(0x10,0x41); 
//	GC0309_write_cmos_sensor(0x13,0x19); 
//	GC0309_write_cmos_sensor(0x1b,0xe0); 
	
//	GC0309_SET_PAGE0;
/******* end update for image performance *****************/


/******* 2010/07/19 Mormo Update ********** *****************/
	GC0309_write_cmos_sensor(0x31,0x60); 

	GC0309_write_cmos_sensor(0x1c,0x49); 
	GC0309_write_cmos_sensor(0x1d,0x98); 
	GC0309_write_cmos_sensor(0x10,0x26); 
	GC0309_write_cmos_sensor(0x1a,0x26);  
/**************** Mormo Update End *************************/	
	
    /*Customer can adjust GAMMA, MIRROR & UPSIDEDOWN here!*/
    GC0309GammaSelect(2);
}

static void GC0309_Init_Parameter(void)
{
    GC0309_sensor.first_init = KAL_TRUE;
    GC0309_sensor.pv_mode = KAL_TRUE;
    GC0309_sensor.night_mode = KAL_FALSE;
    GC0309_sensor.MPEG4_Video_mode = KAL_FALSE;
    
    GC0309_sensor.cp_pclk = GC0309_sensor.pv_pclk;
    
    GC0309_sensor.pv_dummy_pixels = 0;
    GC0309_sensor.pv_dummy_lines = 0;
	GC0309_sensor.cp_dummy_pixels = 0;
	GC0309_sensor.cp_dummy_lines = 0;

    GC0309_sensor.wb = 0;
    GC0309_sensor.exposure = 0;
    GC0309_sensor.effect = 0;
    GC0309_sensor.banding = AE_FLICKER_MODE_50HZ;

    GC0309_sensor.pv_line_length = 640;
    GC0309_sensor.pv_frame_height = 480;
    GC0309_sensor.cp_line_length = 640;
    GC0309_sensor.cp_frame_height = 480;
    
}

#if 0
static kal_uint8 GC0309_power_on(void)
{
    kal_uint8 GC0309_sensor_id = 0;

	GC0309_sensor.pv_pclk = 26000000;
    //Software Reset
    GC0309_write_cmos_sensor(0xfe,0x80);
    GC0309_write_cmos_sensor(0xfe,0x00);
    
    /* Read Sensor ID  */
	GC0309_sensor_id = GC0309_read_cmos_sensor(0x00);
    SENSORDB("[GC0309YUV]:read Sensor ID:%x\n",GC0309_sensor_id);
	
	return GC0309_sensor_id;
}
#endif

/*************************************************************************
* FUNCTION
*	GC0309_GetSensorID
*
* DESCRIPTION
*	This function get the sensor ID
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
static kal_uint32 GC0309_GetSensorID(kal_uint32 * sensorID)

{
	volatile signed char i;
	kal_uint32 sensor_id=0;
	
	GC0309_sensor.pv_pclk = 26000000;
    //Software Reset
    GC0309_write_cmos_sensor(0xfe,0x80);
    GC0309_write_cmos_sensor(0xfe,0x00);
   
	//mDELAY(50);
	msleep(50);

	//	Read sensor ID to adjust I2C is OK?
	for(i=0;i<3;i++)
	{
		sensor_id =GC0309_read_cmos_sensor(0x00);
		if(sensor_id != GC0309_SENSOR_ID)
		{
		    *sensorID = 0xFFFFFFFF;
			return ERROR_SENSOR_CONNECT_FAIL;
		}
	}

	RETAILMSG(1, (TEXT("GC2005 Sensor Read ID OK \r\n")));
	*sensorID = sensor_id;
		
    return ERROR_NONE;    
}  

/*************************************************************************
* FUNCTION
*	GC0309Open
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
UINT32 GC0309Open(void)
{
  	kal_uint8 i, GC0309_sensor_id = 0;
	
	sensor_id_fail = 0; 

	SENSORDB("[Enter]:GC0309 Open func:");

	GC0309_sensor.pv_pclk = 26000000;
	
    //Software Reset
    GC0309_write_cmos_sensor(0xfe,0x80);
	Sleep(20);
    GC0309_write_cmos_sensor(0xfe,0x00);
	Sleep(20);
	
	/* Read Sensor ID  */
	do
    {
        // check if sensor ID correct
		for(i = 0; i < 3; i++)
		{
			GC0309_sensor_id = GC0309_read_cmos_sensor(0x00);
			SENSORDB("[GC0309YUV]:read Sensor ID:%x\n",GC0309_sensor_id);
			if (GC0309_sensor_id == GC0309_SENSOR_ID)
			{
				break;
			}
			Sleep(50);
		}
		
    }while(0);
    
    if (GC0309_sensor_id != GC0309_SENSOR_ID) 
	{
		SENSORDB("[GC0309]Error:read sensor ID fail\n");
		sensor_id_fail = 1;
		
		return ERROR_SENSOR_CONNECT_FAIL;
	}

	/* Apply sensor initail setting*/
	GC0309_Initial_Setting();
	GC0309MoreSetting();
	GC0309_Init_Parameter(); 

	SENSORDB("[Exit]:GC0309 Open func\n");

	return ERROR_NONE;
}	/* GC0309Open() */

/*************************************************************************
* FUNCTION
*	GC0309Close
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
UINT32 GC0309Close(void)
{

	return ERROR_NONE;
}	/* GC0309Close() */


static void GC0309_Set_Mirror_Flip(kal_uint8 image_mirror)
{
  /********************************************************
    * Page Mode 0: Reg 0x0011 bit[1:0] = [Y Flip : X Flip]
    * 0: Off; 1: On.
    *********************************************************/    
    SENSORDB("[Enter]:GC0309 set Mirror_flip func:image_mirror=%d\n",image_mirror);
	
    switch (image_mirror) 
    {
        case IMAGE_NORMAL:
		    GC0309_sensor.mirror = 0x10;
	        break;
	    case IMAGE_H_MIRROR:
		    GC0309_sensor.mirror = 0x11;
	        break;
	    case IMAGE_V_MIRROR:
		    GC0309_sensor.mirror = 0x12;
	        break;
    	case IMAGE_HV_MIRROR:
		    GC0309_sensor.mirror = 0x13;
	        break;
    	default:
	        GC0309_sensor.mirror = 0x10;
    }

    GC0309_write_cmos_sensor(0x14, GC0309_sensor.mirror);

	SENSORDB("[Exit]:GC0309 set Mirror_flip func\n");

}

static void GC0309_set_dummy(kal_uint16 dummy_pixels,kal_uint16 dummy_lines)
{	
	//GC0309_write_cmos_sensor(0xFE, 0x00);                        //Page 0
	//GC0309_write_cmos_sensor(0x40,((dummy_pixels & 0x0F00))>>8);       //HBLANK
	//GC0309_write_cmos_sensor(0x41,(dummy_pixels & 0xFF));
	//GC0309_write_cmos_sensor(0x42,((dummy_lines & 0xFF00)>>8));       //VBLANK ( Vsync Type 1)
	//GC0309_write_cmos_sensor(0x43,(dummy_lines & 0xFF));
}  

void GC0309_night_mode(kal_bool enable)
{	
	SENSORDB("[Enter]GC0309 night mode func:enable = %d\n",enable);
	SENSORDB("GC0309_sensor.video_mode = %d\n",GC0309_sensor.MPEG4_Video_mode); 
	SENSORDB("GC0309_sensor.night_mode = %d\n",GC0309_sensor.night_mode);

	GC0309_sensor.night_mode = enable;

    if(GC0309_sensor.MPEG4_Video_mode == KAL_TRUE)
	    return;

	if(enable)
	{
        GC0309_write_cmos_sensor(0xec ,0x30);	 //exp level 3                      
	}
	else
    {
	    GC0309_write_cmos_sensor(0xec ,0x20);	 //exp level 2
	}
}

/*************************************************************************
* FUNCTION
*	GC0309Preview
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
static UINT32 GC0309Preview(MSDK_SENSOR_EXPOSURE_WINDOW_STRUCT *image_window, MSDK_SENSOR_CONFIG_STRUCT *sensor_config_data)
{
	
	if(GC0309_sensor.first_init == KAL_TRUE)
		GC0309_sensor.MPEG4_Video_mode = GC0309_sensor.MPEG4_Video_mode;
	else
		GC0309_sensor.MPEG4_Video_mode = !GC0309_sensor.MPEG4_Video_mode;

	
	SENSORDB("[Enter]:GC0309 preview func:");		
	SENSORDB("GC0309_sensor.video_mode = %d\n",GC0309_sensor.MPEG4_Video_mode); 

    GC0309_sensor.first_init = KAL_FALSE;
	GC0309_sensor.pv_mode = KAL_TRUE;		
 
	{   
	    SENSORDB("[GC0309]preview set_VGA_mode\n");
	//	GC0309_Set_VGA_mode();
   	}
   
	GC0309_Set_Mirror_Flip(sensor_config_data->SensorImageMirror);

	SENSORDB("[Exit]:GC0309 preview func\n");
    return TRUE; 
}	/* GC0309_Preview */


UINT32 GC0309Capture(MSDK_SENSOR_EXPOSURE_WINDOW_STRUCT *image_window, MSDK_SENSOR_CONFIG_STRUCT *sensor_config_data)
{	

	SENSORDB("[GC0309][Enter]GC0309_capture_func\n");
	
	GC0309_sensor.pv_mode = KAL_FALSE;	
		
	return ERROR_NONE;
}	/* GC0309Capture() */


UINT32 GC0309GetResolution(MSDK_SENSOR_RESOLUTION_INFO_STRUCT *pSensorResolution)
{
	
    SENSORDB("[Enter]:GC0309 get Resolution func\n");

	pSensorResolution->SensorFullWidth=GC0309_IMAGE_SENSOR_FULL_WIDTH - 8;  
	pSensorResolution->SensorFullHeight=GC0309_IMAGE_SENSOR_FULL_HEIGHT - 6;
	pSensorResolution->SensorPreviewWidth=GC0309_IMAGE_SENSOR_PV_WIDTH - 8;
	pSensorResolution->SensorPreviewHeight=GC0309_IMAGE_SENSOR_PV_HEIGHT - 6;

    SENSORDB("[Exit]:GC0309 get Resolution func\n");
	
	return ERROR_NONE;
}	/* GC0309GetResolution() */



UINT32 GC0309GetInfo(MSDK_SCENARIO_ID_ENUM ScenarioId,
					  MSDK_SENSOR_INFO_STRUCT *pSensorInfo,
					  MSDK_SENSOR_CONFIG_STRUCT *pSensorConfigData)
{
	
    SENSORDB("[Enter]:GC0309 getInfo func:ScenarioId = %d\n",ScenarioId);

	pSensorInfo->SensorPreviewResolutionX=GC0309_IMAGE_SENSOR_PV_WIDTH;
	pSensorInfo->SensorPreviewResolutionY=GC0309_IMAGE_SENSOR_PV_HEIGHT;
	pSensorInfo->SensorFullResolutionX=GC0309_IMAGE_SENSOR_FULL_WIDTH;
	pSensorInfo->SensorFullResolutionY=GC0309_IMAGE_SENSOR_FULL_HEIGHT;

	pSensorInfo->SensorCameraPreviewFrameRate=30;
	pSensorInfo->SensorVideoFrameRate=30;
	pSensorInfo->SensorStillCaptureFrameRate=10; //30;
	pSensorInfo->SensorWebCamCaptureFrameRate=15;
	pSensorInfo->SensorResetActiveHigh=FALSE;//low is to reset 
	pSensorInfo->SensorResetDelayCount=4;  //4ms 
	pSensorInfo->SensorOutputDataFormat=SENSOR_OUTPUT_FORMAT_YUYV;
	pSensorInfo->SensorClockPolarity=SENSOR_CLOCK_POLARITY_LOW;	
	pSensorInfo->SensorClockFallingPolarity=SENSOR_CLOCK_POLARITY_LOW;
	pSensorInfo->SensorHsyncPolarity = SENSOR_CLOCK_POLARITY_LOW;
	pSensorInfo->SensorVsyncPolarity = SENSOR_CLOCK_POLARITY_LOW; // SENSOR_CLOCK_POLARITY_HIGH;
	pSensorInfo->SensorInterruptDelayLines = 1; 
	pSensorInfo->SensroInterfaceType=SENSOR_INTERFACE_TYPE_PARALLEL;

	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_100_MODE].MaxWidth=CAM_SIZE_VGA_WIDTH; //???
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_100_MODE].MaxHeight=CAM_SIZE_VGA_HEIGHT;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_100_MODE].ISOSupported=TRUE;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_100_MODE].BinningEnable=FALSE;

	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_200_MODE].MaxWidth=CAM_SIZE_VGA_WIDTH;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_200_MODE].MaxHeight=CAM_SIZE_VGA_HEIGHT;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_200_MODE].ISOSupported=TRUE;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_200_MODE].BinningEnable=FALSE;

	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_400_MODE].MaxWidth=CAM_SIZE_VGA_WIDTH;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_400_MODE].MaxHeight=CAM_SIZE_VGA_HEIGHT;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_400_MODE].ISOSupported=TRUE;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_400_MODE].BinningEnable=FALSE;

	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_800_MODE].MaxWidth=CAM_SIZE_VGA_WIDTH;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_800_MODE].MaxHeight=CAM_SIZE_VGA_HEIGHT;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_800_MODE].ISOSupported=TRUE;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_800_MODE].BinningEnable=TRUE;

	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_1600_MODE].MaxWidth=CAM_SIZE_VGA_WIDTH;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_1600_MODE].MaxHeight=CAM_SIZE_VGA_HEIGHT;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_1600_MODE].ISOSupported=TRUE;
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_1600_MODE].BinningEnable=TRUE;

	pSensorInfo->CaptureDelayFrame = 4; 
	pSensorInfo->PreviewDelayFrame = 10; 
	pSensorInfo->VideoDelayFrame = 0; 
	pSensorInfo->SensorMasterClockSwitch = 0; 
       pSensorInfo->SensorDrivingCurrent = ISP_DRIVING_8MA;   		
	switch (ScenarioId)
	{
		case MSDK_SCENARIO_ID_CAMERA_PREVIEW:
		case MSDK_SCENARIO_ID_VIDEO_PREVIEW:
		case MSDK_SCENARIO_ID_VIDEO_CAPTURE_MPEG4:
			pSensorInfo->SensorClockFreq=26;
			pSensorInfo->SensorClockDividCount=	3;
			pSensorInfo->SensorClockRisingCount= 0;
			pSensorInfo->SensorClockFallingCount= 2;
			pSensorInfo->SensorPixelClockCount= 3;
			pSensorInfo->SensorDataLatchCount= 2;
	        pSensorInfo->SensorGrabStartX = 1; 
	        pSensorInfo->SensorGrabStartY = 10;  			
			
		break;
		case MSDK_SCENARIO_ID_CAMERA_CAPTURE_JPEG:
		case MSDK_SCENARIO_ID_CAMERA_CAPTURE_MEM:
			pSensorInfo->SensorClockFreq=26;
			pSensorInfo->SensorClockDividCount=	3;
			pSensorInfo->SensorClockRisingCount= 0;
			pSensorInfo->SensorClockFallingCount= 2;
			pSensorInfo->SensorPixelClockCount= 3;
			pSensorInfo->SensorDataLatchCount= 2;
            pSensorInfo->SensorGrabStartX = 1; 
            pSensorInfo->SensorGrabStartY = 10;//1;     			
		break;
		default:
			pSensorInfo->SensorClockFreq=26;
			pSensorInfo->SensorClockDividCount=3;
			pSensorInfo->SensorClockRisingCount=0;
			pSensorInfo->SensorClockFallingCount=2;
			pSensorInfo->SensorPixelClockCount=3;
			pSensorInfo->SensorDataLatchCount=2;
            pSensorInfo->SensorGrabStartX = 0; 
            pSensorInfo->SensorGrabStartY = 10;//1;     			
		break;
	}
//	GC0309_PixelClockDivider=pSensorInfo->SensorPixelClockCount;
	memcpy(pSensorConfigData, &GC0309SensorConfigData, sizeof(MSDK_SENSOR_CONFIG_STRUCT));

	SENSORDB("[Exit]:GC0309 getInfo func\n");
	
	return ERROR_NONE;
}	/* GC0309GetInfo() */


UINT32 GC0309Control(MSDK_SCENARIO_ID_ENUM ScenarioId, MSDK_SENSOR_EXPOSURE_WINDOW_STRUCT *pImageWindow,
					  MSDK_SENSOR_CONFIG_STRUCT *pSensorConfigData)
{
	
   SENSORDB("[Enter]:GC0309 Control func:ScenarioId = %d\n",ScenarioId);

	switch (ScenarioId)
	{
		case MSDK_SCENARIO_ID_CAMERA_PREVIEW:
		case MSDK_SCENARIO_ID_VIDEO_PREVIEW:
		case MSDK_SCENARIO_ID_VIDEO_CAPTURE_MPEG4:
		 	 GC0309Preview(pImageWindow, pSensorConfigData); 
			 break;
		case MSDK_SCENARIO_ID_CAMERA_CAPTURE_JPEG:
		case MSDK_SCENARIO_ID_CAMERA_CAPTURE_MEM:
			 GC0309Capture(pImageWindow, pSensorConfigData); 
			 break;
		default:
		     break; 
	}

   SENSORDB("[Exit]:GC0309 Control func\n");
	
	return TRUE;
}	/* GC0309Control() */


/*************************************************************************
* FUNCTION
*	GC0309_set_param_wb
*
* DESCRIPTION
*	wb setting.
*
* PARAMETERS
*	none
*
* RETURNS
*	None
*
* GLOBALS AFFECTED
*
*************************************************************************/
BOOL GC0309_set_param_wb(UINT16 para)
{
	UINT16 temp_reg;
	
    //This sensor need more time to balance AWB, 
    //we suggest higher fps or drop some frame to avoid garbage color when preview initial
   	SENSORDB("[Enter]GC0309 set_param_wb func:para = %d\n",para);

	temp_reg=GC0309_read_cmos_sensor(0x22);	
   if(GC0309_sensor.wb == para) return KAL_TRUE;	

	GC0309_sensor.wb = para;
	
	switch (para)
	{            
		case AWB_MODE_AUTO:
		{
			GC0309_write_cmos_sensor(0x5a,0x56); //for AWB can adjust back
			GC0309_write_cmos_sensor(0x5b,0x40);
			GC0309_write_cmos_sensor(0x5c,0x4a);			
			GC0309_write_cmos_sensor(0x22,temp_reg|0x02);	 // Enable AWB
		        			
            	}                
		    break;
		case AWB_MODE_CLOUDY_DAYLIGHT:
			{
		     	GC0309_write_cmos_sensor(0x22,temp_reg&~0x02);   // Disable AWB 
			GC0309_write_cmos_sensor(0x5a,0x8c); //WB_manual_gain 
			GC0309_write_cmos_sensor(0x5b,0x50);
			GC0309_write_cmos_sensor(0x5c,0x40);
	                }			   
		    break;
		case AWB_MODE_DAYLIGHT:
		    {		    	
	          	GC0309_write_cmos_sensor(0x22,temp_reg&~0x02);   
			GC0309_write_cmos_sensor(0x5a,0x74); 
			GC0309_write_cmos_sensor(0x5b,0x52);
			GC0309_write_cmos_sensor(0x5c,0x40);
            	    }      
		    break;
		case AWB_MODE_INCANDESCENT:	
		    {
		        GC0309_write_cmos_sensor(0x22,temp_reg&~0x02); 
			GC0309_write_cmos_sensor(0x5a,0x48);
			GC0309_write_cmos_sensor(0x5b,0x40);
			GC0309_write_cmos_sensor(0x5c,0x5c);
            }		
		    break;  
		case AWB_MODE_FLUORESCENT:
		    {
	          	GC0309_write_cmos_sensor(0x22,temp_reg&~0x02);   
			GC0309_write_cmos_sensor(0x5a,0x40);
			GC0309_write_cmos_sensor(0x5b,0x42);
			GC0309_write_cmos_sensor(0x5c,0x50);
            	    }	
		    break;  
		case AWB_MODE_TUNGSTEN:
		   {
	           	GC0309_write_cmos_sensor(0x22,temp_reg&~0x02); 
			GC0309_write_cmos_sensor(0x5a,0x40);
			GC0309_write_cmos_sensor(0x5b,0x54);
			GC0309_write_cmos_sensor(0x5c,0x70);
          	   }
		    break;

		default:
			return FALSE;
	}

	return TRUE;
	
} /* GC0309_set_param_wb */

/*************************************************************************
* FUNCTION
*	GC0309_set_param_effect
*
* DESCRIPTION
*	effect setting.
*
* PARAMETERS
*	none
*
* RETURNS
*	None
*
* GLOBALS AFFECTED
*
*************************************************************************/
BOOL GC0309_set_param_effect(UINT16 para)
{
	
   SENSORDB("[Enter]GC0309 set_param_effect func:para = %d\n",para);

   if(GC0309_sensor.effect == para) return KAL_TRUE;
    GC0309_sensor.effect = para;
	
	switch (para)
	{
		case MEFFECT_OFF:
		{
			GC0309_write_cmos_sensor(0x23,0x00);
			GC0309_write_cmos_sensor(0x2d,0x0a); // 0x08
			GC0309_write_cmos_sensor(0x20,0xff);
			GC0309_write_cmos_sensor(0xd2,0x90);
			GC0309_write_cmos_sensor(0x73,0x00);
			GC0309_write_cmos_sensor(0x77,0x54);
			
			GC0309_write_cmos_sensor(0xb3,0x40);
			GC0309_write_cmos_sensor(0xb4,0x80);
			GC0309_write_cmos_sensor(0xba,0x00);
			GC0309_write_cmos_sensor(0xbb,0x00);
            	}
	        break;
		case MEFFECT_SEPIA:
		{
	            	GC0309_write_cmos_sensor(0x23,0x02);		
			GC0309_write_cmos_sensor(0x2d,0x0a);
			GC0309_write_cmos_sensor(0x20,0xff);
			GC0309_write_cmos_sensor(0xd2,0x90);
			GC0309_write_cmos_sensor(0x73,0x00);

			GC0309_write_cmos_sensor(0xb3,0x40);
			GC0309_write_cmos_sensor(0xb4,0x80);
			GC0309_write_cmos_sensor(0xba,0xd0);
			GC0309_write_cmos_sensor(0xbb,0x28);
            	}	
			break;  
		case MEFFECT_NEGATIVE:
		{
	            	GC0309_write_cmos_sensor(0x23,0x03);	
			GC0309_write_cmos_sensor(0x2d,0x0a);
			GC0309_write_cmos_sensor(0x20,0xff);
			GC0309_write_cmos_sensor(0xd2,0x90);
			GC0309_write_cmos_sensor(0x73,0x00);

			GC0309_write_cmos_sensor(0xb3,0x40);
			GC0309_write_cmos_sensor(0xb4,0x80);
			GC0309_write_cmos_sensor(0xba,0x00);
			GC0309_write_cmos_sensor(0xbb,0x00);
            	}
			break; 
		case MEFFECT_SEPIAGREEN:		
			{
	            	GC0309_write_cmos_sensor(0x23,0x02);	
			GC0309_write_cmos_sensor(0x2d,0x0a);
			GC0309_write_cmos_sensor(0x20,0xff);
			GC0309_write_cmos_sensor(0xd2,0x90);
			GC0309_write_cmos_sensor(0x77,0x88);

			GC0309_write_cmos_sensor(0xb3,0x40);
			GC0309_write_cmos_sensor(0xb4,0x80);
			GC0309_write_cmos_sensor(0xba,0xc0);
			GC0309_write_cmos_sensor(0xbb,0xc0);
            		}	
			break;
		case MEFFECT_SEPIABLUE:
			{
			GC0309_write_cmos_sensor(0x23,0x02);	
			GC0309_write_cmos_sensor(0x2d,0x0a);
			GC0309_write_cmos_sensor(0x20,0xff);
			GC0309_write_cmos_sensor(0xd2,0x90);
			GC0309_write_cmos_sensor(0x73,0x00);

			GC0309_write_cmos_sensor(0xb3,0x40);
			GC0309_write_cmos_sensor(0xb4,0x80);
			GC0309_write_cmos_sensor(0xba,0x50);
			GC0309_write_cmos_sensor(0xbb,0xe0);
		    	}     
			break;        
		case MEFFECT_MONO:	//BLACKBOARD		
			{
			GC0309_write_cmos_sensor(0x23,0x02);	
			GC0309_write_cmos_sensor(0x2d,0x0a);
			GC0309_write_cmos_sensor(0x20,0xbf);
			GC0309_write_cmos_sensor(0xd2,0x10);
			GC0309_write_cmos_sensor(0x73,0x01);

			GC0309_write_cmos_sensor(0x51,0x40);
			GC0309_write_cmos_sensor(0x52,0x40);

			GC0309_write_cmos_sensor(0xb3,0x98);
			GC0309_write_cmos_sensor(0xb4,0xb0);
			GC0309_write_cmos_sensor(0xba,0x00);
			GC0309_write_cmos_sensor(0xbb,0x00);
            		}
			break;

		default:
			return KAL_FALSE;
	}

	return KAL_TRUE;

} /* GC0309_set_param_effect */

/*************************************************************************
* FUNCTION
*	GC0309_set_param_banding
*
* DESCRIPTION
*	banding setting.
*
* PARAMETERS
*	none
*
* RETURNS
*	None
*
* GLOBALS AFFECTED
*
*************************************************************************/
BOOL GC0309_set_param_banding(UINT16 para)
{
	
	SENSORDB("[Enter]GC0309 set_param_banding func:para = %d\n",para);
	
	if(GC0309_sensor.banding == para) return KAL_TRUE;

	  GC0309_sensor.banding = para;

	switch (para)
	{
		case AE_FLICKER_MODE_50HZ:
		    {
			GC0309_write_cmos_sensor(0x01  ,0x26); 	
			GC0309_write_cmos_sensor(0x02  ,0x98); 
			GC0309_write_cmos_sensor(0x0f  ,0x03);

			GC0309_write_cmos_sensor(0xe2  ,0x00); 	//anti-flicker step [11:8]
			GC0309_write_cmos_sensor(0xe3  ,0x50);   //anti-flicker step [7:0]
			
			GC0309_write_cmos_sensor(0xe4  ,0x02);   //exp level 0  12.5fps
			GC0309_write_cmos_sensor(0xe5  ,0x80); 
			GC0309_write_cmos_sensor(0xe6  ,0x03);   //exp level 1  10fps
			GC0309_write_cmos_sensor(0xe7  ,0x20); 
			GC0309_write_cmos_sensor(0xe8  ,0x04);   //exp level 2  7.69fps
			GC0309_write_cmos_sensor(0xe9  ,0x10); 
			GC0309_write_cmos_sensor(0xea  ,0x06);   //exp level 3  5.00fps
			GC0309_write_cmos_sensor(0xeb  ,0x40); 
		    }
			break;

		case AE_FLICKER_MODE_60HZ:
		    {
			GC0309_write_cmos_sensor(0x01  ,0x97); 	
			GC0309_write_cmos_sensor(0x02  ,0x84); 
			GC0309_write_cmos_sensor(0x0f  ,0x03);

			GC0309_write_cmos_sensor(0xe2  ,0x00); 	//anti-flicker step [11:8]
			GC0309_write_cmos_sensor(0xe3  ,0x3e);   //anti-flicker step [7:0]
			
			GC0309_write_cmos_sensor(0xe4  ,0x02);   //exp level 0  12.00fps
			GC0309_write_cmos_sensor(0xe5  ,0x6c); 
			GC0309_write_cmos_sensor(0xe6  ,0x02);   //exp level 1  10.00fps
			GC0309_write_cmos_sensor(0xe7  ,0xe8); 
			GC0309_write_cmos_sensor(0xe8  ,0x03);   //exp level 2  7.50fps
			GC0309_write_cmos_sensor(0xe9  ,0xe0); 
			GC0309_write_cmos_sensor(0xea  ,0x05);   //exp level 3  5.00fps
			GC0309_write_cmos_sensor(0xeb  ,0xd0); 
		    }
			break;

	     default:
	          return KAL_FALSE;
	}


	return KAL_TRUE;
} /* GC0309_set_param_banding */




/*************************************************************************
* FUNCTION
*	GC0309_set_param_exposure
*
* DESCRIPTION
*	exposure setting.
*
* PARAMETERS
*	none
*
* RETURNS
*	None
*
* GLOBALS AFFECTED
*
*************************************************************************/
BOOL GC0309_set_param_exposure(UINT16 para)
{
	

	SENSORDB("[Enter]GC0309 set_param_exposure func:para = %d\n",para);

	
	if(GC0309_sensor.exposure == para) return KAL_TRUE;

	  GC0309_sensor.exposure = para;

	switch (para)
	{	
		case AE_EV_COMP_13:  //+4 EV
			GC0309_write_cmos_sensor(0xb5, 0x40);
			GC0309_write_cmos_sensor(0xd3, 0x90);
			break;  
		case AE_EV_COMP_10:  //+3 EV
			GC0309_write_cmos_sensor(0xb5, 0x30);
			GC0309_write_cmos_sensor(0xd3, 0x80);
			break;    
		case AE_EV_COMP_07:  //+2 EV
			GC0309_write_cmos_sensor(0xb5, 0x20);
			GC0309_write_cmos_sensor(0xd3, 0x70);
			break;    
		case AE_EV_COMP_03:	 //	+1 EV	
			GC0309_write_cmos_sensor(0xb5, 0x10);
			GC0309_write_cmos_sensor(0xd3, 0x60);	
			break;    
		case AE_EV_COMP_00:  // +0 EV
		    	GC0309_write_cmos_sensor(0xb5, 0x00);
			GC0309_write_cmos_sensor(0xd3, 0x50);
			break;    
		case AE_EV_COMP_n03:  // -1 EV
			GC0309_write_cmos_sensor(0xb5, 0xf0);
			GC0309_write_cmos_sensor(0xd3, 0x48);
			break;    
		case AE_EV_COMP_n07:	// -2 EV		
			GC0309_write_cmos_sensor(0xb5, 0xe0);
			GC0309_write_cmos_sensor(0xd3, 0x40);	
			break;    
		case AE_EV_COMP_n10:   //-3 EV
			GC0309_write_cmos_sensor(0xb5, 0xd0);
			GC0309_write_cmos_sensor(0xd3, 0x38);
			break;
		case AE_EV_COMP_n13:  // -4 EV
			GC0309_write_cmos_sensor(0xb5, 0xc0);
			GC0309_write_cmos_sensor(0xd3, 0x30);
			break;
		default:
			return KAL_FALSE;
	}

	return TRUE;
	
} /* GC0309_set_param_exposure */


UINT32 GC0309YUVSensorSetting(FEATURE_ID iCmd, UINT32 iPara)
{
	
    SENSORDB("[Enter]GC0309YUVSensorSetting func:cmd = %d\n",iCmd);

	switch (iCmd) {
	case FID_SCENE_MODE:	    //auto mode or night mode
	    if (iPara == SCENE_MODE_OFF)//auto mode
	    {
	        GC0309_night_mode(FALSE); 
	    }
	    else if (iPara == SCENE_MODE_NIGHTSCENE)//night mode
	    {
	        GC0309_night_mode(TRUE); 
	    }	
			
	     break; 	    
	case FID_AWB_MODE:
           GC0309_set_param_wb(iPara);
	     break;
	case FID_COLOR_EFFECT:
           GC0309_set_param_effect(iPara);
	     break;
	case FID_AE_EV:	    	    
           GC0309_set_param_exposure(iPara);
	     break;
	case FID_AE_FLICKER:	    	    	    
           GC0309_set_param_banding(iPara);
	     break;
	case FID_ZOOM_FACTOR:
	     GC0309_zoom_factor = iPara; 		
	     break; 
	default:
	     break;
	}
	return TRUE;
}   /* GC0309YUVSensorSetting */

UINT32 GC0309YUVSetVideoMode(UINT16 u2FrameRate)
{
	
   GC0309_sensor.MPEG4_Video_mode = KAL_TRUE;
    SENSORDB("[Enter]GC0309 Set Video Mode:FrameRate= %d\n",u2FrameRate);
	SENSORDB("GC0309_sensor.video_mode = %d\n",GC0309_sensor.MPEG4_Video_mode);

    if(u2FrameRate == 30) u2FrameRate = 25;
   	
	GC0309_sensor.fix_framerate = u2FrameRate * 10;
    
    if(GC0309_sensor.fix_framerate <= 300 )
    {
    	 if(GC0309_sensor.banding == AE_FLICKER_MODE_50HZ)
    	 {
    	 		GC0309_write_cmos_sensor(0x01  ,0x5e); 	
			GC0309_write_cmos_sensor(0x02  ,0x38); 
			GC0309_write_cmos_sensor(0x0f  ,0x12);

			GC0309_write_cmos_sensor(0xe2  ,0x00); 	//anti-flicker step [11:8]
			GC0309_write_cmos_sensor(0xe3  ,0x64);   //anti-flicker step [7:0]
			
			GC0309_write_cmos_sensor(0xe4  ,0x03);   //exp level 0  12.5fps
			GC0309_write_cmos_sensor(0xe5  ,0x20); 
			GC0309_write_cmos_sensor(0xe6  ,0x03);   //exp level 1  10fps
			GC0309_write_cmos_sensor(0xe7  ,0x20); 
			GC0309_write_cmos_sensor(0xe8  ,0x03);   //exp level 2  7.69fps
			GC0309_write_cmos_sensor(0xe9  ,0x20); 
			GC0309_write_cmos_sensor(0xea  ,0x06);   //exp level 3  5.00fps
			GC0309_write_cmos_sensor(0xeb  ,0x40); 
    	 }
    	 else
    	 {
    	 	GC0309_write_cmos_sensor(0x01  ,0x83); 	
			GC0309_write_cmos_sensor(0x02  ,0xc0); 
			GC0309_write_cmos_sensor(0x0f  ,0x03);

			GC0309_write_cmos_sensor(0xe2  ,0x00); 	//anti-flicker step [11:8]
			GC0309_write_cmos_sensor(0xe3  ,0x44);   //anti-flicker step [7:0]
			
			GC0309_write_cmos_sensor(0xe4  ,0x02);   //exp level 0  12.5fps
			GC0309_write_cmos_sensor(0xe5  ,0xa8); 
			GC0309_write_cmos_sensor(0xe6  ,0x02);   //exp level 1  10fps
			GC0309_write_cmos_sensor(0xe7  ,0xa8); 
			GC0309_write_cmos_sensor(0xe8  ,0x02);   //exp level 2  7.69fps
			GC0309_write_cmos_sensor(0xe9  ,0xa8); 
			GC0309_write_cmos_sensor(0xea  ,0x05);   //exp level 3  5.00fps
			GC0309_write_cmos_sensor(0xeb  ,0x50); 
    	 }
    }
    else 
    {
        SENSORDB("Wrong Frame Rate"); 
    } 
    return TRUE;
}

UINT32 GC0309FeatureControl(MSDK_SENSOR_FEATURE_ENUM FeatureId,
							 UINT8 *pFeaturePara,UINT32 *pFeatureParaLen)
{
    UINT16 u2Temp = 0; 
	UINT16 *pFeatureReturnPara16=(UINT16 *) pFeaturePara;
	UINT16 *pFeatureData16=(UINT16 *) pFeaturePara;
	UINT32 *pFeatureReturnPara32=(UINT32 *) pFeaturePara;
	UINT32 *pFeatureData32=(UINT32 *) pFeaturePara;
	MSDK_SENSOR_CONFIG_STRUCT *pSensorConfigData=(MSDK_SENSOR_CONFIG_STRUCT *) pFeaturePara;
	MSDK_SENSOR_REG_INFO_STRUCT *pSensorRegData=(MSDK_SENSOR_REG_INFO_STRUCT *) pFeaturePara;

	SENSORDB("[Enter]GC0309FeatureControl: FeatureId = %d\n", FeatureId);

	switch (FeatureId)
	{
		case SENSOR_FEATURE_GET_RESOLUTION:
			*pFeatureReturnPara16++=GC0309_IMAGE_SENSOR_FULL_WIDTH;
			*pFeatureReturnPara16=GC0309_IMAGE_SENSOR_FULL_HEIGHT;
			*pFeatureParaLen=4;
		     break;
		case SENSOR_FEATURE_GET_PERIOD:
			*pFeatureReturnPara16++=GC0309_IMAGE_SENSOR_PV_WIDTH;//+GC0309_sensor.pv_dummy_pixels;
			*pFeatureReturnPara16=GC0309_IMAGE_SENSOR_PV_HEIGHT;//+GC0309_sensor.pv_dummy_lines;
			*pFeatureParaLen=4;
		     break;
		case SENSOR_FEATURE_GET_PIXEL_CLOCK_FREQ:
			//*pFeatureReturnPara32 = GC0309_sensor_pclk/10;
			*pFeatureParaLen=4;
		     break;
		case SENSOR_FEATURE_SET_ESHUTTER:
	
		     break;
		case SENSOR_FEATURE_SET_NIGHTMODE:
			 GC0309_night_mode((BOOL) *pFeatureData16);
		     break;
		case SENSOR_FEATURE_SET_GAIN:
			 break; 
		case SENSOR_FEATURE_SET_FLASHLIGHT:
		     break;
		case SENSOR_FEATURE_SET_ISP_MASTER_CLOCK_FREQ:
		     break;
		case SENSOR_FEATURE_SET_REGISTER:
			 GC0309_write_cmos_sensor(pSensorRegData->RegAddr, pSensorRegData->RegData);
		     break;
		case SENSOR_FEATURE_GET_REGISTER:
			 pSensorRegData->RegData = GC0309_read_cmos_sensor(pSensorRegData->RegAddr);
		     break;
	        case SENSOR_FEATURE_CHECK_SENSOR_ID:
			 GC0309_GetSensorID(pFeatureData32);
			 break;
		case SENSOR_FEATURE_GET_CONFIG_PARA:
			 memcpy(pSensorConfigData, &GC0309SensorConfigData, sizeof(MSDK_SENSOR_CONFIG_STRUCT));
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
	               // *pFeatureReturnPara32++=0;
			//*pFeatureParaLen=4;
		     break; 
		
		case SENSOR_FEATURE_GET_LENS_DRIVER_ID:
			// get the lens driver ID from EEPROM or just return LENS_DRIVER_ID_DO_NOT_CARE
			// if EEPROM does not exist in camera module.
			*pFeatureReturnPara32=LENS_DRIVER_ID_DO_NOT_CARE;
			*pFeatureParaLen=4;
		     break;
		case SENSOR_FEATURE_SET_YUV_CMD:
			 GC0309YUVSensorSetting((FEATURE_ID)*pFeatureData32, *(pFeatureData32+1));
		     break;	
		case SENSOR_FEATURE_SET_VIDEO_MODE:
		     GC0309YUVSetVideoMode(*pFeatureData16);
		     break; 
		default:
			 break;			
	}
	return ERROR_NONE;
}	/* GC0309FeatureControl() */


SENSOR_FUNCTION_STRUCT	SensorFuncGC0309=
{
	GC0309Open,
	GC0309GetInfo,
	GC0309GetResolution,
	GC0309FeatureControl,
	GC0309Control,
	GC0309Close
};

UINT32 GC0309_YUV_SensorInit(PSENSOR_FUNCTION_STRUCT *pfFunc)
{
	/* To Do : Check Sensor status here */
	if (pfFunc!=NULL)
		*pfFunc=&SensorFuncGC0309;

	return ERROR_NONE;
}	/* SensorInit() */


