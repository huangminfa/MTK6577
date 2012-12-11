/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
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
*  permission of MediaTek Inc. (C) 2005
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
 *   Sensor.c
 *
 * Project:
 * --------
 *   DUMA
 *
 * Description:
 * ------------
 *   Image sensor driver function
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/



/*#####################################################


superpix    sensor   30m  SP0828 .   sensorID = 0X0A       SLAVE ADDR= 0X42 



#####################################################*/

 
#include <linux/videodev2.h>
#include <linux/i2c.h>
#include <linux/platform_device.h>
#include <linux/delay.h>
#include <linux/cdev.h>
#include <linux/uaccess.h>
#include <linux/fs.h>
#include <asm/atomic.h>
#include <asm/io.h>

#include "kd_camera_hw.h"
#include "kd_imgsensor.h"
#include "kd_imgsensor_define.h"
#include "kd_imgsensor_errcode.h"
#include "kd_camera_feature.h"

#include "sp0828yuv_Sensor.h"
#include "sp0828yuv_Camera_Sensor_para.h"
#include "sp0828yuv_CameraCustomized.h"

static MSDK_SENSOR_CONFIG_STRUCT SP0828SensorConfigData;
static struct SP0828_Sensor_Struct SP0828_Sensor_Driver;




#define SP0828YUV_DEBUG
#ifdef SP0828YUV_DEBUG
#define SENSORDB printk
#else
#define SENSORDB(x,...)
#endif


#define __SENSOR_CONTROL__
#ifdef __SENSOR_CONTROL__
#define CAMERA_CONTROL_FLOW(para1,para2) printk("[%s:%d]::para1=0x%x,para1=0x%x\n\n",__FUNCTION__,__LINE__,para1,para2)
#else
#define CAMERA_CONTROL_FLOW(para1, para2)
#endif

//kal_uint8 res=0,closed=0,info=0;

kal_uint8 is_SP0828_Banding = 0; // 0: 50hz  1:60hz


#if 0
extern int iReadReg(u16 a_u2Addr , u8 * a_puBuff , u16 i2cId);
extern int iWriteReg(u16 a_u2Addr , u32 a_u4Data , u32 a_u4Bytes , u16 i2cId);
static int sensor_id_fail = 0; 
#define SP0828_write_cmos_sensor(addr,para) iWriteReg((u16) addr , (u32) para ,1,SP0828_WRITE_ID)
//#define SP0828_write_cmos_sensor_2(addr, para, bytes) iWriteReg((u16) addr , (u32) para ,bytes,SP0828_WRITE_ID)
kal_uint16 SP0828_read_cmos_sensor(kal_uint32 addr)
{
kal_uint16 get_byte=0;
    iReadReg((u16) addr ,(u8*)&get_byte,SP0828_WRITE_ID);
    return get_byte;
}
#endif

extern int iReadRegI2C(u8 *a_pSendData , u16 a_sizeSendData, u8 * a_pRecvData, u16 a_sizeRecvData, u16 i2cId);
extern int iWriteRegI2C(u8 *a_pSendData , u16 a_sizeSendData, u16 i2cId);
/*************************************************************************
* FUNCTION
*    SP0828_write_cmos_sensor
*
* DESCRIPTION
*    This function wirte data to CMOS sensor through I2C
*
* PARAMETERS
*    addr: the 16bit address of register
*    para: the 8bit value of register
*
* RETURNS
*    None
*
* LOCAL AFFECTED
*
*************************************************************************/
static void SP0828_write_cmos_sensor(kal_uint8 addr, kal_uint8 para)
{
kal_uint8 out_buff[2];

    out_buff[0] = addr;
    out_buff[1] = para;

    iWriteRegI2C((u8*)out_buff , (u16)sizeof(out_buff), SP0828_WRITE_ID); 

#if (defined(__SP0828_DEBUG_TRACE__))
  if (sizeof(out_buff) != rt) printk("I2C write %x, %x error\n", addr, para);
#endif
}

/*************************************************************************
* FUNCTION
*    SP0828_read_cmos_sensor
*
* DESCRIPTION
*    This function read data from CMOS sensor through I2C.
*
* PARAMETERS
*    addr: the 16bit address of register
*
* RETURNS
*    8bit data read through I2C
*
* LOCAL AFFECTED
*
*************************************************************************/
static kal_uint8 SP0828_read_cmos_sensor(kal_uint8 addr)
{
  kal_uint8 in_buff[1] = {0xFF};
  kal_uint8 out_buff[1];
  
  out_buff[0] = addr;
   
    if (0 != iReadRegI2C((u8*)out_buff , (u16) sizeof(out_buff), (u8*)in_buff, (u16) sizeof(in_buff), SP0828_WRITE_ID)) {
        SENSORDB("ERROR: SP0828_read_cmos_sensor \n");
    }

#if (defined(__SP0828_DEBUG_TRACE__))
  if (size != rt) printk("I2C read %x error\n", addr);
#endif

  return in_buff[0];
}


#if 0
static void SP0828_Write_Shutter(kal_uint16 shutter)
{  
  kal_uint8 temp_reg;
   
	if(shutter<=VGA_EXPOSURE_LIMITATION)
	{		
		 SP0828_Sensor_Driver.extra_exposure_lines=0;
		}
	else
	{			
	SP0828_Sensor_Driver.extra_exposure_lines=shutter-VGA_EXPOSURE_LIMITATION;
	}
		
	if(shutter>VGA_EXPOSURE_LIMITATION)
	shutter=VGA_EXPOSURE_LIMITATION;
		 
	temp_reg=SP0828_read_cmos_sensor(0x04);
	SP0828_write_cmos_sensor(0x04,( (temp_reg&0xFC) | (shutter&0x3) ));			// AEC[b1~b0]
	SP0828_write_cmos_sensor(0x10,((shutter&0x3FC)>>2));								// AEC[b9~b2]
	SP0828_write_cmos_sensor(0x07,((shutter&0x400)>>10));

   SP0828_write_cmos_sensor(0x2D,SP0828_Sensor_Driver.extra_exposure_lines&0xFF);                   // ADVFL(LSB of extra exposure lines)
   SP0828_write_cmos_sensor(0x2E,(SP0828_Sensor_Driver.extra_exposure_lines&0xFF00)>>8);            // ADVFH(MSB of extra exposure lines)  
	
}   /*  SP0828_Write_Shutter    */
#endif

 static void SP0828_Set_Dummy(const kal_uint16 iPixels, const kal_uint16 iLines)
{
#if 0
    SP0828_write_cmos_sensor(0x2A,((iPixels&0x700)>>4));
	SP0828_write_cmos_sensor(0x2B,(iPixels&0xFF));
	SP0828_write_cmos_sensor(0x92,(iLines&0xFF));
	SP0828_write_cmos_sensor(0x93,((iLines&0xFF00)>>8));
#endif
}   /*  SP0828_Set_Dummy    */


/*************************************************************************
* FUNCTION
*	SP0828_write_reg
*
* DESCRIPTION
*	This function set the register of SP0828.
*
* PARAMETERS
*	addr : the register index of OV76X0
*  para : setting parameter of the specified register of OV76X0
*
* RETURNS
*	None
*
* GLOBALS AFFECTED
*
*************************************************************************/

//static void SP0828_write_reg(kal_uint32 addr, kal_uint32 para)
//{
//	SP0828_write_cmos_sensor(addr,para);
//}	/* SP0828_write_reg() */

/*************************************************************************
* FUNCTION
*	ov7670_read_cmos_sensor
*
* DESCRIPTION
*	This function read parameter of specified register from OV76X0.
*
* PARAMETERS
*	addr : the register index of OV76X0
*
* RETURNS
*	the data that read from OV76X0
*
* GLOBALS AFFECTED
*
*************************************************************************/
//static kal_uint32 SP0828_read_reg(kal_uint32 addr)
//{
//	return (SP0828_read_cmos_sensor(addr));
//}	/* OV7670_read_reg() */


/*************************************************************************
* FUNCTION
*	SP0828_NightMode
*
* DESCRIPTION
*	This function night mode of SP0828.
*
* PARAMETERS
*	bEnable: KAL_TRUE -> enable night mode, otherwise, disable night mode
*
* RETURNS
*	None
*
* GLOBALS AFFECTED
*
*************************************************************************/
static void SP0828_night_mode(kal_bool bEnable)
{
 //kal_uint8 temp = SP0828_read_cmos_sensor(0x3B);


  if (!SP0828_Sensor_Driver.MODE_CAPTURE) { 
	if(bEnable)   //night mode  
	{ 
		   SP0828_Sensor_Driver.bNight_mode = KAL_TRUE;
	   if(SP0828_Sensor_Driver.MPEG4_encode_mode == KAL_TRUE)
	   	{
				//dbg_print(" SP0828_banding=%x\r\n",SP0828_banding);
				if(is_SP0828_Banding== 0)
				{
				//Video record night 24M 50hz 20-10FPS maxgain:0x78                 
				SP0828_write_cmos_sensor(0xfd,0x00);
				SP0828_write_cmos_sensor(0x05,0x0 );
				SP0828_write_cmos_sensor(0x06,0x0 );
			SP0828_write_cmos_sensor(0x09,0x2	);
			SP0828_write_cmos_sensor(0x0a,0x8d);
			SP0828_write_cmos_sensor(0xf0,0x42);
			SP0828_write_cmos_sensor(0xf1,0x0	);
			SP0828_write_cmos_sensor(0xf2,0x57);
			SP0828_write_cmos_sensor(0xf5,0x70);
			SP0828_write_cmos_sensor(0xfd,0x01);
			SP0828_write_cmos_sensor(0x00,0xae);
			SP0828_write_cmos_sensor(0x0f,0x58);
			SP0828_write_cmos_sensor(0x16,0x58);
			SP0828_write_cmos_sensor(0x17,0x96);
			SP0828_write_cmos_sensor(0x18,0x9e);
			SP0828_write_cmos_sensor(0x1b,0x58);
			SP0828_write_cmos_sensor(0x1c,0x9e);
			SP0828_write_cmos_sensor(0xb4,0x20);
			SP0828_write_cmos_sensor(0xb5,0x3a);
			SP0828_write_cmos_sensor(0xb6,0x3e);
				SP0828_write_cmos_sensor(0xb9,0x40);
				SP0828_write_cmos_sensor(0xba,0x4f);
				SP0828_write_cmos_sensor(0xbb,0x47);
				SP0828_write_cmos_sensor(0xbc,0x45);
				SP0828_write_cmos_sensor(0xbd,0x43);
				SP0828_write_cmos_sensor(0xbe,0x42);
				SP0828_write_cmos_sensor(0xbf,0x42);
				SP0828_write_cmos_sensor(0xc0,0x42);
				SP0828_write_cmos_sensor(0xc1,0x41);
				SP0828_write_cmos_sensor(0xc2,0x41);
				SP0828_write_cmos_sensor(0xc3,0x78);
				SP0828_write_cmos_sensor(0xc4,0x41);
				SP0828_write_cmos_sensor(0xc5,0x41);
				SP0828_write_cmos_sensor(0xc6,0x41);
				SP0828_write_cmos_sensor(0xca,0x78);
				SP0828_write_cmos_sensor(0xcb,0xa );
				SP0828_write_cmos_sensor(0x14,0x20);
				SP0828_write_cmos_sensor(0x15,0x1f);
				SP0828_write_cmos_sensor(0xfd,0x00);			
				//dbg_print(" video 50Hz night\r\n");				
				}
				else if(is_SP0828_Banding == 1)
				{
				           
				//Video record night 24M 60hz 20-10FPS maxgain:0x78               
				SP0828_write_cmos_sensor(0xfd,0x00);
				SP0828_write_cmos_sensor(0x05,0x0 );
				SP0828_write_cmos_sensor(0x06,0x0 );
			SP0828_write_cmos_sensor(0x09,0x2	);
			SP0828_write_cmos_sensor(0x0a,0x8d);
			SP0828_write_cmos_sensor(0xf0,0x37);
			SP0828_write_cmos_sensor(0xf1,0x0	);
			SP0828_write_cmos_sensor(0xf2,0x54);
			SP0828_write_cmos_sensor(0xf5,0x6d);
			SP0828_write_cmos_sensor(0xfd,0x01);
			SP0828_write_cmos_sensor(0x00,0xaf);
			SP0828_write_cmos_sensor(0x0f,0x55);
			SP0828_write_cmos_sensor(0x16,0x55);
			SP0828_write_cmos_sensor(0x17,0x97);
			SP0828_write_cmos_sensor(0x18,0x9f);
			SP0828_write_cmos_sensor(0x1b,0x55);
			SP0828_write_cmos_sensor(0x1c,0x9f);
			SP0828_write_cmos_sensor(0xb4,0x21);
			SP0828_write_cmos_sensor(0xb5,0x35);
			SP0828_write_cmos_sensor(0xb6,0x35);
				SP0828_write_cmos_sensor(0xb9,0x40);
				SP0828_write_cmos_sensor(0xba,0x4f);
				SP0828_write_cmos_sensor(0xbb,0x47);
				SP0828_write_cmos_sensor(0xbc,0x45);
				SP0828_write_cmos_sensor(0xbd,0x43);
				SP0828_write_cmos_sensor(0xbe,0x42);
				SP0828_write_cmos_sensor(0xbf,0x42);
				SP0828_write_cmos_sensor(0xc0,0x42);
				SP0828_write_cmos_sensor(0xc1,0x41);
				SP0828_write_cmos_sensor(0xc2,0x41);
				SP0828_write_cmos_sensor(0xc3,0x41);
				SP0828_write_cmos_sensor(0xc4,0x41);
				SP0828_write_cmos_sensor(0xc5,0x78);
				SP0828_write_cmos_sensor(0xc6,0x41);
				SP0828_write_cmos_sensor(0xca,0x78);
				SP0828_write_cmos_sensor(0xcb,0xc );
				SP0828_write_cmos_sensor(0x14,0x20);
				SP0828_write_cmos_sensor(0x15,0x1f);
				SP0828_write_cmos_sensor(0xfd,0x00);
				//dbg_print(" video 60Hz night\r\n");				
				}
			   }
	    else  
	   {
			//	dbg_print(" SP0828_banding=%x\r\n",SP0828_banding);
			       if(is_SP0828_Banding== 0)
				{
				//caprure preview night 12M 50hz 20-6FPS maxgain:0x78 
			SP0828_write_cmos_sensor(0xfd,0x00); 
			SP0828_write_cmos_sensor(0x05,0x0	); 
			SP0828_write_cmos_sensor(0x06,0x0	); 
			SP0828_write_cmos_sensor(0x09,0x2	); 
			SP0828_write_cmos_sensor(0x0a,0x8d); 
			SP0828_write_cmos_sensor(0xf0,0x42); 
			SP0828_write_cmos_sensor(0xf1,0x0	); 
			SP0828_write_cmos_sensor(0xf2,0x57); 
			SP0828_write_cmos_sensor(0xf5,0x70); 
			SP0828_write_cmos_sensor(0xfd,0x01); 
			SP0828_write_cmos_sensor(0x00,0xb8); 
			SP0828_write_cmos_sensor(0x0f,0x58); 
			SP0828_write_cmos_sensor(0x16,0x58); 
			SP0828_write_cmos_sensor(0x17,0xa0); 
			SP0828_write_cmos_sensor(0x18,0xa8); 
			SP0828_write_cmos_sensor(0x1b,0x58); 
			SP0828_write_cmos_sensor(0x1c,0xa8); 
			SP0828_write_cmos_sensor(0xb4,0x20); 
			SP0828_write_cmos_sensor(0xb5,0x3a); 
			SP0828_write_cmos_sensor(0xb6,0x3e); 
				SP0828_write_cmos_sensor(0xb9,0x40);
				SP0828_write_cmos_sensor(0xba,0x4f);
				SP0828_write_cmos_sensor(0xbb,0x47);
				SP0828_write_cmos_sensor(0xbc,0x45);
				SP0828_write_cmos_sensor(0xbd,0x43);
				SP0828_write_cmos_sensor(0xbe,0x42);
				SP0828_write_cmos_sensor(0xbf,0x42);
				SP0828_write_cmos_sensor(0xc0,0x42);
				SP0828_write_cmos_sensor(0xc1,0x41);
				SP0828_write_cmos_sensor(0xc2,0x41);
				SP0828_write_cmos_sensor(0xc3,0x41);
				SP0828_write_cmos_sensor(0xc4,0x41);
				SP0828_write_cmos_sensor(0xc5,0x41);
				SP0828_write_cmos_sensor(0xc6,0x41);
				SP0828_write_cmos_sensor(0xca,0x78);
				SP0828_write_cmos_sensor(0xcb,0x10);
				SP0828_write_cmos_sensor(0x14,0x20);
				SP0828_write_cmos_sensor(0x15,0x1f);
				SP0828_write_cmos_sensor(0xfd,0x00);			
				//dbg_print(" priview 50Hz night\r\n");	
				}  
				else if(is_SP0828_Banding== 1)
				{
				//caprure preview night 12M 60hz 20-6FPS maxgain:0x78		
			SP0828_write_cmos_sensor(0xfd,0x00);
			SP0828_write_cmos_sensor(0x05,0x0	);
			SP0828_write_cmos_sensor(0x06,0x0	);
			SP0828_write_cmos_sensor(0x09,0x2	);
			SP0828_write_cmos_sensor(0x0a,0x8d);
			SP0828_write_cmos_sensor(0xf0,0x37);
			SP0828_write_cmos_sensor(0xf1,0x0	);
			SP0828_write_cmos_sensor(0xf2,0x54);
			SP0828_write_cmos_sensor(0xf5,0x6d);
			SP0828_write_cmos_sensor(0xfd,0x01);
			SP0828_write_cmos_sensor(0x00,0xb9);
			SP0828_write_cmos_sensor(0x0f,0x55);
			SP0828_write_cmos_sensor(0x16,0x55);
			SP0828_write_cmos_sensor(0x17,0xa1);
			SP0828_write_cmos_sensor(0x18,0xa9);
			SP0828_write_cmos_sensor(0x1b,0x55);
			SP0828_write_cmos_sensor(0x1c,0xa9);
			SP0828_write_cmos_sensor(0xb4,0x21);
			SP0828_write_cmos_sensor(0xb5,0x35);
			SP0828_write_cmos_sensor(0xb6,0x35);
			SP0828_write_cmos_sensor(0xb9,0x40);
				SP0828_write_cmos_sensor(0xba,0x4f);
				SP0828_write_cmos_sensor(0xbb,0x47);
				SP0828_write_cmos_sensor(0xbc,0x45);
				SP0828_write_cmos_sensor(0xbd,0x43);
				SP0828_write_cmos_sensor(0xbe,0x42);   
				SP0828_write_cmos_sensor(0xbf,0x42);
				SP0828_write_cmos_sensor(0xc0,0x42);
				SP0828_write_cmos_sensor(0xc1,0x41);
				SP0828_write_cmos_sensor(0xc2,0x41);
				SP0828_write_cmos_sensor(0xc3,0x41);
				SP0828_write_cmos_sensor(0xc4,0x41);
				SP0828_write_cmos_sensor(0xc5,0x41);
				SP0828_write_cmos_sensor(0xc6,0x41);
				SP0828_write_cmos_sensor(0xca,0x78);
				SP0828_write_cmos_sensor(0xcb,0x14);
				SP0828_write_cmos_sensor(0x14,0x20);
				SP0828_write_cmos_sensor(0x15,0x1f);
				SP0828_write_cmos_sensor(0xfd,0x00);
			//	dbg_print(" priview 60Hz night\r\n");	
				}
			       } 		
	}
	else//normal mode
	{
		SP0828_Sensor_Driver.bNight_mode = KAL_FALSE;
	    if(SP0828_Sensor_Driver.MPEG4_encode_mode == KAL_TRUE)
			{
				if(is_SP0828_Banding== 0)
				{
			//Video record daylight 12M 50hz fix 20FPS maxgain:0x78
			SP0828_write_cmos_sensor(0xfd,0x00);
			SP0828_write_cmos_sensor(0x05,0x0	);
			SP0828_write_cmos_sensor(0x06,0x0	);
			SP0828_write_cmos_sensor(0x09,0x2	);
			SP0828_write_cmos_sensor(0x0a,0x8d);
			SP0828_write_cmos_sensor(0xf0,0x42);
			SP0828_write_cmos_sensor(0xf1,0x0	);
			SP0828_write_cmos_sensor(0xf2,0x57);
			SP0828_write_cmos_sensor(0xf5,0x70);
			SP0828_write_cmos_sensor(0xfd,0x01);
			SP0828_write_cmos_sensor(0x00,0xa1);
			SP0828_write_cmos_sensor(0x0f,0x58);
			SP0828_write_cmos_sensor(0x16,0x58);
			SP0828_write_cmos_sensor(0x17,0x89);
			SP0828_write_cmos_sensor(0x18,0x91);
			SP0828_write_cmos_sensor(0x1b,0x58);
			SP0828_write_cmos_sensor(0x1c,0x91);
			SP0828_write_cmos_sensor(0xb4,0x20);
			SP0828_write_cmos_sensor(0xb5,0x3a);
			SP0828_write_cmos_sensor(0xb6,0x3e);
			SP0828_write_cmos_sensor(0xb9,0x40);
				SP0828_write_cmos_sensor(0xba,0x4f);
				SP0828_write_cmos_sensor(0xbb,0x47);
				SP0828_write_cmos_sensor(0xbc,0x45);
				SP0828_write_cmos_sensor(0xbd,0x43);
				SP0828_write_cmos_sensor(0xbe,0x78);
				SP0828_write_cmos_sensor(0xbf,0x42);
				SP0828_write_cmos_sensor(0xc0,0x42);
				SP0828_write_cmos_sensor(0xc1,0x41);
				SP0828_write_cmos_sensor(0xc2,0x41);
				SP0828_write_cmos_sensor(0xc3,0x41);
				SP0828_write_cmos_sensor(0xc4,0x41);
				SP0828_write_cmos_sensor(0xc5,0x41);
				SP0828_write_cmos_sensor(0xc6,0x41);
				SP0828_write_cmos_sensor(0xca,0x78);
				SP0828_write_cmos_sensor(0xcb,0x5 );
				SP0828_write_cmos_sensor(0x14,0x20);
				SP0828_write_cmos_sensor(0x15,0x0f);		
				SP0828_write_cmos_sensor(0xfd,0x00);
				//dbg_print(" video 50Hz normal\r\n");
				}
				else if(is_SP0828_Banding == 1)
				{
				//Video record daylight 12M 60hz fix 20FPS maxgain:0x78
			SP0828_write_cmos_sensor(0xfd,0x00);
			SP0828_write_cmos_sensor(0x05,0x0	);
			SP0828_write_cmos_sensor(0x06,0x0	);
			SP0828_write_cmos_sensor(0x09,0x2	);
			SP0828_write_cmos_sensor(0x0a,0x8d);
			SP0828_write_cmos_sensor(0xf0,0x37);
			SP0828_write_cmos_sensor(0xf1,0x0	);
			SP0828_write_cmos_sensor(0xf2,0x54);
			SP0828_write_cmos_sensor(0xf5,0x6d);
			SP0828_write_cmos_sensor(0xfd,0x01);
			SP0828_write_cmos_sensor(0x00,0xa1);
			SP0828_write_cmos_sensor(0x0f,0x55);
			SP0828_write_cmos_sensor(0x16,0x55);
			SP0828_write_cmos_sensor(0x17,0x89);
			SP0828_write_cmos_sensor(0x18,0x91);
			SP0828_write_cmos_sensor(0x1b,0x55);
			SP0828_write_cmos_sensor(0x1c,0x91);
			SP0828_write_cmos_sensor(0xb4,0x21);
			SP0828_write_cmos_sensor(0xb5,0x35);
			SP0828_write_cmos_sensor(0xb6,0x35);
			SP0828_write_cmos_sensor(0xb9,0x40);
				SP0828_write_cmos_sensor(0xba,0x4f);
				SP0828_write_cmos_sensor(0xbb,0x47);
				SP0828_write_cmos_sensor(0xbc,0x45);
				SP0828_write_cmos_sensor(0xbd,0x43);
				SP0828_write_cmos_sensor(0xbe,0x42);
				SP0828_write_cmos_sensor(0xbf,0x78);
				SP0828_write_cmos_sensor(0xc0,0x42);
				SP0828_write_cmos_sensor(0xc1,0x41);
				SP0828_write_cmos_sensor(0xc2,0x41);
				SP0828_write_cmos_sensor(0xc3,0x41);
				SP0828_write_cmos_sensor(0xc4,0x41);
				SP0828_write_cmos_sensor(0xc5,0x41);
				SP0828_write_cmos_sensor(0xc6,0x41);
				SP0828_write_cmos_sensor(0xca,0x78);
				SP0828_write_cmos_sensor(0xcb,0x6 );
				SP0828_write_cmos_sensor(0x14,0x20);
				SP0828_write_cmos_sensor(0x15,0x0f);
				SP0828_write_cmos_sensor(0xfd,0x00);
				//dbg_print(" video 60Hz normal\r\n");
				}
   		  	}
	    
		else 
			{
			//	dbg_print(" SP0828_banding=%x\r\n",SP0828_banding);
			       if(is_SP0828_Banding== 0)
				{
				//caprure preview daylight 12M 50hz 20-8FPS maxgain:0x70	
			SP0828_write_cmos_sensor(0xfd,0x00);
			SP0828_write_cmos_sensor(0x05,0x0	);
			SP0828_write_cmos_sensor(0x06,0x0	);
			SP0828_write_cmos_sensor(0x09,0x2	);
			SP0828_write_cmos_sensor(0x0a,0x8d);
			SP0828_write_cmos_sensor(0xf0,0x42);
			SP0828_write_cmos_sensor(0xf1,0x0	);
			SP0828_write_cmos_sensor(0xf2,0x57);
			SP0828_write_cmos_sensor(0xf5,0x70);
			SP0828_write_cmos_sensor(0xfd,0x01);
			SP0828_write_cmos_sensor(0x00,0xaa);
			SP0828_write_cmos_sensor(0x0f,0x58);
			SP0828_write_cmos_sensor(0x16,0x58);
			SP0828_write_cmos_sensor(0x17,0x9a);
			SP0828_write_cmos_sensor(0x18,0xa2);
			SP0828_write_cmos_sensor(0x1b,0x58);
			SP0828_write_cmos_sensor(0x1c,0xa2);
			SP0828_write_cmos_sensor(0xb4,0x20);
			SP0828_write_cmos_sensor(0xb5,0x3a);
			SP0828_write_cmos_sensor(0xb6,0x3e);
				SP0828_write_cmos_sensor(0xb9,0x40);
				SP0828_write_cmos_sensor(0xba,0x4f);
				SP0828_write_cmos_sensor(0xbb,0x47);
				SP0828_write_cmos_sensor(0xbc,0x45);
				SP0828_write_cmos_sensor(0xbd,0x43);
				SP0828_write_cmos_sensor(0xbe,0x42);
				SP0828_write_cmos_sensor(0xbf,0x42);
				SP0828_write_cmos_sensor(0xc0,0x42);
				SP0828_write_cmos_sensor(0xc1,0x41);
				SP0828_write_cmos_sensor(0xc2,0x41);
				SP0828_write_cmos_sensor(0xc3,0x41);
				SP0828_write_cmos_sensor(0xc4,0x41);
				SP0828_write_cmos_sensor(0xc5,0x70);
				SP0828_write_cmos_sensor(0xc6,0x41);
				SP0828_write_cmos_sensor(0xca,0x70);
				SP0828_write_cmos_sensor(0xcb,0xc );
				SP0828_write_cmos_sensor(0x14,0x20);
				SP0828_write_cmos_sensor(0x15,0x0f);
				SP0828_write_cmos_sensor(0xfd,0x00);
			//	dbg_print(" priview 50Hz normal\r\n");
				}

          else if(is_SP0828_Banding == 1)
			   {
				//caprure preview daylight 12M 60hz 20-8FPS maxgain:0x70	  	
			SP0828_write_cmos_sensor(0xfd,0x00);
			SP0828_write_cmos_sensor(0x05,0x0	);
			SP0828_write_cmos_sensor(0x06,0x0	);
			SP0828_write_cmos_sensor(0x09,0x2	);
			SP0828_write_cmos_sensor(0x0a,0x8d);
			SP0828_write_cmos_sensor(0xf0,0x37);
			SP0828_write_cmos_sensor(0xf1,0x0	);
			SP0828_write_cmos_sensor(0xf2,0x54);
			SP0828_write_cmos_sensor(0xf5,0x6d);
			SP0828_write_cmos_sensor(0xfd,0x01);
			SP0828_write_cmos_sensor(0x00,0xac);
			SP0828_write_cmos_sensor(0x0f,0x55);
			SP0828_write_cmos_sensor(0x16,0x55);
			SP0828_write_cmos_sensor(0x17,0x9c);
			SP0828_write_cmos_sensor(0x18,0xa4);
			SP0828_write_cmos_sensor(0x1b,0x55);
			SP0828_write_cmos_sensor(0x1c,0xa4);
			SP0828_write_cmos_sensor(0xb4,0x21);
			SP0828_write_cmos_sensor(0xb5,0x35);
			SP0828_write_cmos_sensor(0xb6,0x35);
				SP0828_write_cmos_sensor(0xb9,0x40);
				SP0828_write_cmos_sensor(0xba,0x4f);
				SP0828_write_cmos_sensor(0xbb,0x47);
				SP0828_write_cmos_sensor(0xbc,0x45);
				SP0828_write_cmos_sensor(0xbd,0x43);
				SP0828_write_cmos_sensor(0xbe,0x42);
				SP0828_write_cmos_sensor(0xbf,0x42);
				SP0828_write_cmos_sensor(0xc0,0x42);
				SP0828_write_cmos_sensor(0xc1,0x41);
				SP0828_write_cmos_sensor(0xc2,0x41);
				SP0828_write_cmos_sensor(0xc3,0x41);
				SP0828_write_cmos_sensor(0xc4,0x41);
				SP0828_write_cmos_sensor(0xc5,0x41);
				SP0828_write_cmos_sensor(0xc6,0x41);
				SP0828_write_cmos_sensor(0xca,0x70);
				SP0828_write_cmos_sensor(0xcb,0xf );
				SP0828_write_cmos_sensor(0x14,0x20);
				SP0828_write_cmos_sensor(0x15,0x0f);
				SP0828_write_cmos_sensor(0xfd,0x00);
			   //dbg_print(" priview 60Hz normal\r\n");   
			   }				   
			       }	   
	}  
	}
}	/*	SP0828_NightMode	*/

/*
static void SP0828_set_isp_driving_current(kal_uint8 current)
{
    //#define CONFIG_BASE      	(0xF0001000)     
//  iowrite32((0xE << 12)|(0 << 28)|0x8880888, 0xF0001500);
}
*/

static void SP0828_Sensor_Driver_Init(void)
	{
SP0828_write_cmos_sensor(0xfd,0x00);
SP0828_write_cmos_sensor(0x1c,0x00);//08
SP0828_write_cmos_sensor(0x30,0x00);
SP0828_write_cmos_sensor(0x0f,0x2f);//;analog
SP0828_write_cmos_sensor(0x10,0x2f);
SP0828_write_cmos_sensor(0x12,0x7f);
SP0828_write_cmos_sensor(0x13,0x2f);
SP0828_write_cmos_sensor(0x15,0x7f);
SP0828_write_cmos_sensor(0x16,0x0f);
SP0828_write_cmos_sensor(0x22,0xe0);
SP0828_write_cmos_sensor(0x26,0x08);
SP0828_write_cmos_sensor(0x27,0xe8);
SP0828_write_cmos_sensor(0x28,0x0b);
SP0828_write_cmos_sensor(0x32,0x00);
SP0828_write_cmos_sensor(0x31,0x10);   //Upside/mirr/Pclk inv/sub
SP0828_write_cmos_sensor(0x5f,0x11);
SP0828_write_cmos_sensor(0xe0,0x00);//;resize
SP0828_write_cmos_sensor(0xe1,0xdc);
SP0828_write_cmos_sensor(0xe2,0xb0);
SP0828_write_cmos_sensor(0xe3,0x00);
SP0828_write_cmos_sensor(0xe4,0x2e);
SP0828_write_cmos_sensor(0xe5,0x00);
SP0828_write_cmos_sensor(0xe6,0x2b);
SP0828_write_cmos_sensor(0xb7,0x3c);//;LSC
SP0828_write_cmos_sensor(0xb8,0x50);
SP0828_write_cmos_sensor(0xfd,0x01);
SP0828_write_cmos_sensor(0x25,0x1a);//AWB
SP0828_write_cmos_sensor(0x26,0xfb);
SP0828_write_cmos_sensor(0x28,0x61);
SP0828_write_cmos_sensor(0x29,0x49);
SP0828_write_cmos_sensor(0x31,0x64);
SP0828_write_cmos_sensor(0x32,0x18);
SP0828_write_cmos_sensor(0x4d,0xdc);
SP0828_write_cmos_sensor(0x4e,0x53);//6b
SP0828_write_cmos_sensor(0x41,0x8c);
SP0828_write_cmos_sensor(0x42,0x57);//66
SP0828_write_cmos_sensor(0x55,0xff);
SP0828_write_cmos_sensor(0x56,0x00);
SP0828_write_cmos_sensor(0x59,0x82);
SP0828_write_cmos_sensor(0x5a,0x00);
SP0828_write_cmos_sensor(0x5d,0xff);
SP0828_write_cmos_sensor(0x5e,0x6f);
SP0828_write_cmos_sensor(0x57,0xff);
SP0828_write_cmos_sensor(0x58,0x00);
SP0828_write_cmos_sensor(0x5b,0xff);
SP0828_write_cmos_sensor(0x5c,0xa8);
SP0828_write_cmos_sensor(0x5f,0x75);
SP0828_write_cmos_sensor(0x60,0x00);
SP0828_write_cmos_sensor(0x2d,0x00);
SP0828_write_cmos_sensor(0x2e,0x00);
SP0828_write_cmos_sensor(0x2f,0x00);
SP0828_write_cmos_sensor(0x30,0x00);
SP0828_write_cmos_sensor(0x33,0x00);
SP0828_write_cmos_sensor(0x34,0x00);
SP0828_write_cmos_sensor(0x37,0x00);
SP0828_write_cmos_sensor(0x38,0x00);
SP0828_write_cmos_sensor(0x39,0x01);
SP0828_write_cmos_sensor(0x3a,0x07);
SP0828_write_cmos_sensor(0xfd,0x00);//;BPC
SP0828_write_cmos_sensor(0x33,0x0f);
SP0828_write_cmos_sensor(0x51,0x3f);
SP0828_write_cmos_sensor(0x52,0x09);
SP0828_write_cmos_sensor(0x53,0x00);
SP0828_write_cmos_sensor(0x54,0x00);// 4
SP0828_write_cmos_sensor(0x55,0x10);
SP0828_write_cmos_sensor(0x4f,0xff);//blueedge
SP0828_write_cmos_sensor(0x50,0xff);
SP0828_write_cmos_sensor(0x56,0x70);//smooth   
SP0828_write_cmos_sensor(0x57,0x10);//0x10
SP0828_write_cmos_sensor(0x58,0x10);//0x10
SP0828_write_cmos_sensor(0x59,0x10);//0x10
SP0828_write_cmos_sensor(0x5a,0x05);//0x02
SP0828_write_cmos_sensor(0x5b,0x15);//0x02
SP0828_write_cmos_sensor(0x5c,0x20);

SP0828_write_cmos_sensor(0x65,0x05);//sharpness 0x03
SP0828_write_cmos_sensor(0x66,0x01);
SP0828_write_cmos_sensor(0x67,0x02);//0x03
SP0828_write_cmos_sensor(0x68,0x42);//0x46
SP0828_write_cmos_sensor(0x69,0x7f);
SP0828_write_cmos_sensor(0x6a,0x01);
SP0828_write_cmos_sensor(0x6b,0x07);//0x04
SP0828_write_cmos_sensor(0x6c,0x01);
SP0828_write_cmos_sensor(0x6d,0x02);//0x03
SP0828_write_cmos_sensor(0x6e,0x42);//0x46

SP0828_write_cmos_sensor(0x6f,0x7f);
SP0828_write_cmos_sensor(0x70,0x01);
SP0828_write_cmos_sensor(0x71,0x09);//0x05
SP0828_write_cmos_sensor(0x72,0x01);//0x01
SP0828_write_cmos_sensor(0x73,0x02);//3
SP0828_write_cmos_sensor(0x74,0x42);
SP0828_write_cmos_sensor(0x75,0x7f);
SP0828_write_cmos_sensor(0x76,0x01);
SP0828_write_cmos_sensor(0x7f,0xa0);//;colorcorrection
SP0828_write_cmos_sensor(0x80,0x00);
SP0828_write_cmos_sensor(0x81,0xe0); 
SP0828_write_cmos_sensor(0x82,0xed);
SP0828_write_cmos_sensor(0x83,0xa6);
SP0828_write_cmos_sensor(0x84,0xed);
SP0828_write_cmos_sensor(0x85,0xfa);
SP0828_write_cmos_sensor(0x86,0xba);
SP0828_write_cmos_sensor(0x87,0xcc);
SP0828_write_cmos_sensor(0x88,0x30); 
SP0828_write_cmos_sensor(0x89,0x33);
SP0828_write_cmos_sensor(0x8a,0x0f); 
SP0828_write_cmos_sensor(0x8b,0x00);// ;gamma
SP0828_write_cmos_sensor(0x8c,0x1a);
SP0828_write_cmos_sensor(0x8d,0x29);
SP0828_write_cmos_sensor(0x8e,0x41);
SP0828_write_cmos_sensor(0x8f,0x62);
SP0828_write_cmos_sensor(0x90,0x7c);
SP0828_write_cmos_sensor(0x91,0x90);
SP0828_write_cmos_sensor(0x92,0xa2);
SP0828_write_cmos_sensor(0x93,0xaf);
SP0828_write_cmos_sensor(0x94,0xba);
SP0828_write_cmos_sensor(0x95,0xc4);
SP0828_write_cmos_sensor(0x96,0xce);
SP0828_write_cmos_sensor(0x97,0xd6);
SP0828_write_cmos_sensor(0x98,0xdd);
SP0828_write_cmos_sensor(0x99,0xe4);
SP0828_write_cmos_sensor(0x9a,0xea);
SP0828_write_cmos_sensor(0x9b,0xf1);
SP0828_write_cmos_sensor(0xfd,0x01);
SP0828_write_cmos_sensor(0x8d,0xf8);
SP0828_write_cmos_sensor(0x8e,0xff);
SP0828_write_cmos_sensor(0xfd,0x00);
SP0828_write_cmos_sensor(0xca,0xcf);//;saturation
SP0828_write_cmos_sensor(0xd8,0x50); //0x58
SP0828_write_cmos_sensor(0xd9,0x50);
SP0828_write_cmos_sensor(0xda,0x50);
SP0828_write_cmos_sensor(0xdb,0x50);
SP0828_write_cmos_sensor(0xcb,0x07);//;hist-expand
SP0828_write_cmos_sensor(0xcc,0x04);
SP0828_write_cmos_sensor(0xce,0xff);
SP0828_write_cmos_sensor(0xcf,0x10);
SP0828_write_cmos_sensor(0xd0,0x20);
SP0828_write_cmos_sensor(0xd1,0x00);
SP0828_write_cmos_sensor(0xd2,0x1c);
SP0828_write_cmos_sensor(0xd3,0x16);
SP0828_write_cmos_sensor(0xd4,0x00);
SP0828_write_cmos_sensor(0xd6,0x1c);
SP0828_write_cmos_sensor(0xd7,0x16);
SP0828_write_cmos_sensor(0xdd,0x70);// ;heq
SP0828_write_cmos_sensor(0xde,0xa4);
SP0828_write_cmos_sensor(0xb9,0x00);//;Ygamma 
SP0828_write_cmos_sensor(0xba,0x04); 
SP0828_write_cmos_sensor(0xbb,0x08); 
SP0828_write_cmos_sensor(0xbc,0x10);
SP0828_write_cmos_sensor(0xbd,0x20);
SP0828_write_cmos_sensor(0xbe,0x30);
SP0828_write_cmos_sensor(0xbf,0x40);
SP0828_write_cmos_sensor(0xc0,0x50);
SP0828_write_cmos_sensor(0xc1,0x60);
SP0828_write_cmos_sensor(0xc2,0x70);
SP0828_write_cmos_sensor(0xc3,0x80);
SP0828_write_cmos_sensor(0xc4,0x90);
SP0828_write_cmos_sensor(0xc5,0xA0);
SP0828_write_cmos_sensor(0xc6,0xB0);
SP0828_write_cmos_sensor(0xc7,0xC0);
SP0828_write_cmos_sensor(0xc8,0xD0);
SP0828_write_cmos_sensor(0xc9,0xE0);
SP0828_write_cmos_sensor(0xfd,0x01);
SP0828_write_cmos_sensor(0x89,0xf0);
SP0828_write_cmos_sensor(0x8a,0xff);
SP0828_write_cmos_sensor(0xfd,0x00);
SP0828_write_cmos_sensor(0xe8,0x30);//;AE
SP0828_write_cmos_sensor(0xe9,0x30);
SP0828_write_cmos_sensor(0xea,0x40);
SP0828_write_cmos_sensor(0xf4,0x1b);
SP0828_write_cmos_sensor(0xf5,0x97);
SP0828_write_cmos_sensor(0xec,0x53);
SP0828_write_cmos_sensor(0xed,0x78);
SP0828_write_cmos_sensor(0xee,0x47);
SP0828_write_cmos_sensor(0xef,0x6c);
SP0828_write_cmos_sensor(0xf7,0x70);//AEtarget
SP0828_write_cmos_sensor(0xf8,0x5b);//AEtarget
SP0828_write_cmos_sensor(0xf9,0x64);//AEtarget
SP0828_write_cmos_sensor(0xfa,0x4f);//AEtarget
SP0828_write_cmos_sensor(0xfd,0x01);
SP0828_write_cmos_sensor(0x09,0x31);	
SP0828_write_cmos_sensor(0x0a,0x85);
SP0828_write_cmos_sensor(0x0b,0x0b);	
SP0828_write_cmos_sensor(0x14,0x20);
SP0828_write_cmos_sensor(0x15,0x0f); 

#if 1 // MCLK 12M 20-8fps maxgain:0x70
SP0828_write_cmos_sensor(0xfd,0x00);
SP0828_write_cmos_sensor(0x05,0x0	);
SP0828_write_cmos_sensor(0x06,0x0	);
SP0828_write_cmos_sensor(0x09,0x2	);
SP0828_write_cmos_sensor(0x0a,0x8d);
SP0828_write_cmos_sensor(0xf0,0x42);
SP0828_write_cmos_sensor(0xf1,0x0	);
SP0828_write_cmos_sensor(0xf2,0x57);
SP0828_write_cmos_sensor(0xf5,0x70);
SP0828_write_cmos_sensor(0xfd,0x01);
SP0828_write_cmos_sensor(0x00,0xaa);
SP0828_write_cmos_sensor(0x0f,0x58);
SP0828_write_cmos_sensor(0x16,0x58);
SP0828_write_cmos_sensor(0x17,0x9a);
SP0828_write_cmos_sensor(0x18,0xa2);
SP0828_write_cmos_sensor(0x1b,0x58);
SP0828_write_cmos_sensor(0x1c,0xa2);
SP0828_write_cmos_sensor(0xb4,0x20);
SP0828_write_cmos_sensor(0xb5,0x3a);
SP0828_write_cmos_sensor(0xb6,0x3e);
SP0828_write_cmos_sensor(0xb9,0x40);
SP0828_write_cmos_sensor(0xba,0x4f);
SP0828_write_cmos_sensor(0xbb,0x47);
SP0828_write_cmos_sensor(0xbc,0x45);
SP0828_write_cmos_sensor(0xbd,0x43);
SP0828_write_cmos_sensor(0xbe,0x42);
SP0828_write_cmos_sensor(0xbf,0x42);
SP0828_write_cmos_sensor(0xc0,0x42);
SP0828_write_cmos_sensor(0xc1,0x41);
SP0828_write_cmos_sensor(0xc2,0x41);
SP0828_write_cmos_sensor(0xc3,0x41);
SP0828_write_cmos_sensor(0xc4,0x41);
SP0828_write_cmos_sensor(0xc5,0x70);
SP0828_write_cmos_sensor(0xc6,0x41);
SP0828_write_cmos_sensor(0xca,0x70);
SP0828_write_cmos_sensor(0xcb,0xc	);
SP0828_write_cmos_sensor(0xfd,0x00);
#endif

SP0828_write_cmos_sensor(0xfd,0x00);
SP0828_write_cmos_sensor(0x32,0x15);
SP0828_write_cmos_sensor(0x34,0x66);
SP0828_write_cmos_sensor(0x35,0x00);//out format
SP0828_write_cmos_sensor(0x1b,0x07);//drv ability
}

	
/*************************************************************************
* FUNCTION
*	SP0828Open
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
static kal_uint32 SP0828Open(void)

{
	/*kal_uint16 sensor_id=0; 
	int retry = 10; 

    SENSORDB("SP0828Open_start \n");

//	SP0828_Sensor_Driver.i2c_clit.addr=SP0828_WRITE_ID;
//	SP0828_Sensor_Driver.i2c_clit = i2c_clit;
//    SP0828_Sensor_Driver.i2c_clit->addr = SP0828_WRITE_ID;

#if 0 
	SP0828_write_cmos_sensor(0x12, 0x80);
	mDELAY(10);
#endif 

	// check if sensor ID correct
	do {
		
		SP0828_write_cmos_sensor(0xfd,0x00);
	    sensor_id=SP0828_read_cmos_sensor(0x02);
    	    if (sensor_id == SP0828_SENSOR_ID) {
                 break; 
    	    }
         SENSORDB("Read Sensor ID Fail = 0x%x\n", sensor_id); 
    	    
    	    retry--; 
	}while (retry > 0); 
	
	if (sensor_id != SP0828_SENSOR_ID) {
	    return ERROR_SENSOR_CONNECT_FAIL;
	}*/

	volatile signed char i;
	kal_uint16 sensor_id=0;

	printk("%s, start\n", __func__);
	
       //zoom_factor = 0; 

       mDELAY(10);

	//  Read sensor ID to adjust I2C is OK?
	for(i=0;i<3;i++)
	{
	       SP0828_write_cmos_sensor(0xfd, 0x00); 
		sensor_id = SP0828_read_cmos_sensor(0x02);
		SENSORDB("%s sensor_id=%d\n", __func__, sensor_id);
		if(sensor_id != SP0828_SENSOR_ID)
		{
			return ERROR_SENSOR_CONNECT_FAIL;
		}
	}



  memset(&SP0828_Sensor_Driver, 0, sizeof(struct SP0828_Sensor_Struct)); 
	SP0828_Sensor_Driver.MPEG4_encode_mode=KAL_FALSE;
	SP0828_Sensor_Driver.dummy_pixels=0;
	SP0828_Sensor_Driver.dummy_lines=0;
	SP0828_Sensor_Driver.extra_exposure_lines=0;
	SP0828_Sensor_Driver.exposure_lines=0;
	SP0828_Sensor_Driver.MODE_CAPTURE=KAL_FALSE;
		
	SP0828_Sensor_Driver.bNight_mode =KAL_FALSE; // to distinguish night mode or auto mode, default: auto mode setting
	SP0828_Sensor_Driver.bBanding_value = AE_FLICKER_MODE_50HZ; // to distinguish between 50HZ and 60HZ.
		
	SP0828_Sensor_Driver.fPV_PCLK = 26; //26000000;
	SP0828_Sensor_Driver.iPV_Pixels_Per_Line = 0;

//	SP0828_set_isp_driving_current(1);
	// initail sequence write in
//    SP0828_write_cmos_sensor(0x12, 0x80);
    mDELAY(10);
    SP0828_Sensor_Driver_Init();		
    SENSORDB("SP0828Open_end \n");
    
    return ERROR_NONE;
}   /* SP0828Open  */



/*************************************************************************
* FUNCTION
*	SP0828_GetSensorID
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
static kal_uint32 SP0828_GetSensorID(kal_uint32 *sensorID)

{
	
	/*SENSORDB("SP0828GetSensorID\n");	
	//	Read sensor ID to adjust I2C is OK?
    //	SP0828_write_cmos_sensor(0xfd,0x00);
	*sensorID = SP0828_read_cmos_sensor(0x02);
	SENSORDB("SP0828 Sensor Read ID %x\n",*sensorID);
	if (*sensorID != SP0828_SENSOR_ID) 
	{
	  return ERROR_SENSOR_CONNECT_FAIL;
	}
	
	return ERROR_NONE;*/
	volatile signed char i;
	kal_uint16 sensor_id=0;
	SENSORDB("xieyang SP0828GetSensorID ");
	//SENSORDB("xieyang in GPIO_CAMERA_CMPDN_PIN=%d,GPIO_CAMERA_CMPDN1_PIN=%d", mt_get_gpio_out(GPIO_CAMERA_CMPDN_PIN),mt_get_gpio_out(GPIO_CAMERA_CMPDN1_PIN));

    for(i=0;i<3;i++)
	{
	    SP0828_write_cmos_sensor(0xfd, 0x00); 
		sensor_id = SP0828_read_cmos_sensor(0x02);
		SENSORDB("%s sensor_id=%d\n", __func__, sensor_id);
		if(sensor_id != SP0828_SENSOR_ID)
		{
			*sensorID = 0xFFFFFFFF;
			return ERROR_SENSOR_CONNECT_FAIL;
		}
		else
		{
			*sensorID = sensor_id;
			break;
		}
	}
  
	return ERROR_NONE;
}


/*************************************************************************
* FUNCTION
*	SP0828Close
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
static kal_uint32 SP0828Close(void)
{
	kal_uint8 tmp1;
   // tmp1 = closed;
	//CAMERA_CONTROL_FLOW(tmp1,closed++);
   SENSORDB("SP0828Close\n");
	return ERROR_NONE;
}   /* SP0828Close */




static void SP0828_HVMirror(ACDK_SENSOR_IMAGE_MIRROR_ENUM SensorImageMirror)
{
	//volatile kal_uint32 temp_reg2=SP0828_read_cmos_sensor(0x1E), temp_reg1=(temp_reg2&0x0F);
	kal_uint32 iTemp;
	
	kal_uint32 iTemp2;
        int retry = 3;
		
		SP0828_write_cmos_sensor(0xfd,0x00);
	iTemp =  SP0828_read_cmos_sensor(0x31);
	#if 0
#if defined(AGOLD_SP0828_YUV_HV_MIRROR) //[Agold][xxd]
	SensorImageMirror  = IMAGE_HV_MIRROR;
#elif defined(AGOLD_SP0828_YUV_H_MIRROR)
	SensorImageMirror   = IMAGE_H_MIRROR;
#elif defined(AGOLD_SP0828_YUV_V_MIRROR)
	SensorImageMirror   = IMAGE_V_MIRROR;
#endif 

        SensorImageMirror ^= IMAGE_HV_MIRROR; //[Agold][xxd][add for qq video rotate 180]   
      #endif
	iTemp2= iTemp;
	switch (SensorImageMirror)
	{
		case IMAGE_NORMAL:
			SP0828_write_cmos_sensor(0xfd,0x00);
			SP0828_write_cmos_sensor(0x31,iTemp2);

			
			break;
		case IMAGE_H_MIRROR:			 

			if((iTemp2 & 0x20)==0x20)
				iTemp2 &= (~0x20);
			else
				iTemp2 |= 0x20;
			
			SP0828_write_cmos_sensor(0xfd,0x00);
			SP0828_write_cmos_sensor(0x31,iTemp2);
			break;
		case IMAGE_V_MIRROR:			 
			if((iTemp2 & 0x40)==0x40)
				iTemp2 &= (~0x40);
			else
				iTemp2 |= 0x40;
			
			SP0828_write_cmos_sensor(0xfd,0x00);
			SP0828_write_cmos_sensor(0x31,iTemp2);
			break;
		case IMAGE_HV_MIRROR:						 

			if((iTemp2 & 0x20)==0x20)
				iTemp2 &= (~0x20);
			else
				iTemp2 |= 0x20;

			if((iTemp2 & 0x40)==0x40)
				iTemp2 &= (~0x40);
			else
				iTemp2 |= 0x40;

			SP0828_write_cmos_sensor(0xfd,0x00);
			SP0828_write_cmos_sensor(0x31,iTemp2);
			break;
	}


}
/*************************************************************************
* FUNCTION
* SP0828_Preview
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
static kal_uint32 SP0828_Preview(MSDK_SENSOR_EXPOSURE_WINDOW_STRUCT *image_window,
					  MSDK_SENSOR_CONFIG_STRUCT *sensor_config_data)

{
	SP0828_Sensor_Driver.fPV_PCLK=26000000;
	SP0828_Sensor_Driver.MODE_CAPTURE=KAL_FALSE;

	if(sensor_config_data->SensorOperationMode==MSDK_SENSOR_OPERATION_MODE_VIDEO){
		SP0828_Sensor_Driver.MPEG4_encode_mode = KAL_TRUE;  // MPEG4 Encode Mode
	}else{
		SP0828_Sensor_Driver.MPEG4_encode_mode = KAL_FALSE;  
	}


//   SP0828_HVMirror(sensor_config_data->SensorImageMirror);

//	SP0828_Sensor_Driver.dummy_pixels = 0;
//	SP0828_Sensor_Driver.dummy_lines = 42;
//	SP0828_Sensor_Driver.iPV_Pixels_Per_Line =VGA_PERIOD_PIXEL_NUMS+SP0828_Sensor_Driver.dummy_pixels;  
//	SP0828_Set_Dummy(SP0828_Sensor_Driver.dummy_pixels, SP0828_Sensor_Driver.dummy_lines);

	
	image_window->GrabStartX= IMAGE_SENSOR_VGA_INSERTED_PIXELS;
	image_window->GrabStartY= IMAGE_SENSOR_VGA_INSERTED_LINES;
	image_window->ExposureWindowWidth = IMAGE_SENSOR_PV_WIDTH;
	image_window->ExposureWindowHeight =IMAGE_SENSOR_PV_HEIGHT;

	if(KAL_TRUE == SP0828_Sensor_Driver.bNight_mode) // for nd 128 noise,decrease color matrix
	{
	}

	// copy sensor_config_data
	memcpy(&SP0828SensorConfigData, sensor_config_data, sizeof(MSDK_SENSOR_CONFIG_STRUCT));
	return ERROR_NONE;

}   /*  SP0828_Preview   */

/*************************************************************************
* FUNCTION
*	SP0828_Capture
*
* DESCRIPTION
*	This function setup the CMOS sensor in capture MY_OUTPUT mode
*
* PARAMETERS
*
* RETURNS
*	None
*
* GLOBALS AFFECTED
*
*************************************************************************/
#if 0
static kal_uint32 SP0828_Capture(MSDK_SENSOR_EXPOSURE_WINDOW_STRUCT *image_window,
						  MSDK_SENSOR_CONFIG_STRUCT *sensor_config_data)

{
	volatile kal_uint32 shutter=SP0828_Sensor_Driver.exposure_lines;
	//tatic float fCP_PCLK = 0;
	kal_uint32 fCP_PCLK = 0;
	kal_uint16 iCP_Pixels_Per_Line = 0;

	SP0828_Sensor_Driver.MODE_CAPTURE=KAL_TRUE;




	SP0828_Sensor_Driver.dummy_pixels=0;
	SP0828_Sensor_Driver.dummy_lines=0;  
	fCP_PCLK= SP0828_Sensor_Driver.fPV_PCLK;
	iCP_Pixels_Per_Line =VGA_PERIOD_PIXEL_NUMS+SP0828_Sensor_Driver.dummy_pixels;
	//shutter = shutter * (fCP_PCLK / SP0828_Sensor_Driver.fPV_PCLK) * SP0828_Sensor_Driver.iPV_Pixels_Per_Line / iCP_Pixels_Per_Line;
	shutter = shutter * (fCP_PCLK / SP0828_Sensor_Driver.fPV_PCLK) * SP0828_Sensor_Driver.iPV_Pixels_Per_Line / iCP_Pixels_Per_Line;
	if (shutter < 1) {
	  shutter = 1;
	}	  
	SP0828_Set_Dummy(SP0828_Sensor_Driver.dummy_pixels, SP0828_Sensor_Driver.dummy_lines);
	SP0828_Write_Shutter(shutter);	

/*	
	sensor_config_data->DefaultPclk = fCP_PCLK; 
	sensor_config_data->Pixels = iCP_Pixels_Per_Line;
	sensor_config_data->FrameLines =VGA_PERIOD_PIXEL_NUMS ;
	sensor_config_data->Lines = image_window->ExposureWindowHeight;    
	sensor_config_data->Shutter =shutter;		
*/	

	
	if(KAL_TRUE == SP0828_Sensor_Driver.bNight_mode)  // for nd128 noise
	{
		kal_uint8 gain = SP0828_read_cmos_sensor(0x00);
		if(gain == 0x7f){
			SP0828_write_cmos_sensor(0x4f,0x63);//matrix
			SP0828_write_cmos_sensor(0x50,0x68);
			SP0828_write_cmos_sensor(0x51,0x05);
			SP0828_write_cmos_sensor(0x52,0x16);
			SP0828_write_cmos_sensor(0x53,0x42);
			SP0828_write_cmos_sensor(0x54,0x57);
			SP0828_write_cmos_sensor(0x58,0x1a);
		}
	}
	
	// copy sensor_config_data
	memcpy(&SP0828SensorConfigData, sensor_config_data, sizeof(MSDK_SENSOR_CONFIG_STRUCT));

	image_window->GrabStartX = IMAGE_SENSOR_VGA_INSERTED_PIXELS;
	image_window->GrabStartY = IMAGE_SENSOR_VGA_INSERTED_LINES;
	image_window->ExposureWindowWidth= IMAGE_SENSOR_FULL_WIDTH;
	image_window->ExposureWindowHeight = IMAGE_SENSOR_FULL_HEIGHT;  


	return ERROR_NONE;
}   /* OV7576_Capture() */
#endif

static kal_uint32 SP0828GetResolution(MSDK_SENSOR_RESOLUTION_INFO_STRUCT *pSensorResolution)
{
kal_uint8 tmp1;
//    tmp1 = res;
//	CAMERA_CONTROL_FLOW(tmp1,res++);

	pSensorResolution->SensorFullWidth=IMAGE_SENSOR_FULL_WIDTH;
	pSensorResolution->SensorFullHeight=IMAGE_SENSOR_FULL_HEIGHT;
    pSensorResolution->SensorPreviewWidth=IMAGE_SENSOR_PV_WIDTH;
	pSensorResolution->SensorPreviewHeight=IMAGE_SENSOR_PV_HEIGHT;
	return ERROR_NONE;
}	/* SP0828GetResolution() */

static kal_uint32 SP0828GetInfo(MSDK_SCENARIO_ID_ENUM ScenarioId,
					  MSDK_SENSOR_INFO_STRUCT *pSensorInfo,
					  MSDK_SENSOR_CONFIG_STRUCT *pSensorConfigData)
{
#if 0
#if 0
	pSensorInfo->SensorPreviewResolutionX=IMAGE_SENSOR_PV_WIDTH;
	pSensorInfo->SensorPreviewResolutionY=IMAGE_SENSOR_PV_HEIGHT;
	pSensorInfo->SensorFullResolutionX=IMAGE_SENSOR_FULL_WIDTH;
    pSensorInfo->SensorFullResolutionY=IMAGE_SENSOR_FULL_HEIGHT;
	pSensorInfo->SensorCameraPreviewFrameRate=30;
	pSensorInfo->SensorVideoFrameRate=30;
	pSensorInfo->SensorStillCaptureFrameRate=10;
	pSensorInfo->SensorWebCamCaptureFrameRate=15;
	pSensorInfo->SensorResetActiveHigh=FALSE; //low active
	pSensorInfo->SensorResetDelayCount=5; 
#endif

	pSensorInfo->SensorOutputDataFormat=SENSOR_OUTPUT_FORMAT_YUYV;
	pSensorInfo->SensorClockPolarity=SENSOR_CLOCK_POLARITY_LOW;
	pSensorInfo->SensorClockFallingPolarity=SENSOR_CLOCK_POLARITY_LOW;
	pSensorInfo->SensorHsyncPolarity = SENSOR_CLOCK_POLARITY_LOW;
 	pSensorInfo->SensorVsyncPolarity = SENSOR_CLOCK_POLARITY_HIGH;
       pSensorInfo->SensorDriver3D = 0;   // the sensor driver is 2D

	
	pSensorInfo->SensorMasterClockSwitch = 0; 
      pSensorInfo->SensorDrivingCurrent = ISP_DRIVING_2MA;   		

#if 0
	pSensorInfo->SensorInterruptDelayLines = 1;
	pSensorInfo->SensroInterfaceType=SENSOR_INTERFACE_TYPE_PARALLEL;
	
	pSensorInfo->SensorISOBinningInfo.ISOBinningInfo[ISO_100_MODE].MaxWidth=CAM_SIZE_VGA_WIDTH;
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
#endif

	//CAMERA_CONTROL_FLOW(ScenarioId,ScenarioId);

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
			
			pSensorInfo->SensorGrabStartX = IMAGE_SENSOR_VGA_INSERTED_PIXELS; 
			pSensorInfo->SensorGrabStartY = IMAGE_SENSOR_VGA_INSERTED_LINES;			   
		break;
		case MSDK_SCENARIO_ID_CAMERA_CAPTURE_JPEG:
		case MSDK_SCENARIO_ID_CAMERA_CAPTURE_MEM:		
			pSensorInfo->SensorClockFreq=26;
			pSensorInfo->SensorClockDividCount= 3;
			pSensorInfo->SensorClockRisingCount=0;
			pSensorInfo->SensorClockFallingCount=2;
			pSensorInfo->SensorPixelClockCount=3;
			pSensorInfo->SensorDataLatchCount=2;	
			
			pSensorInfo->SensorGrabStartX = IMAGE_SENSOR_VGA_INSERTED_PIXELS; 
			pSensorInfo->SensorGrabStartY = IMAGE_SENSOR_VGA_INSERTED_LINES;			   
		break;
		default:
			pSensorInfo->SensorClockFreq=26;
			pSensorInfo->SensorClockDividCount= 3;
			pSensorInfo->SensorClockRisingCount=0;
			pSensorInfo->SensorClockFallingCount=2;
			pSensorInfo->SensorPixelClockCount=3;
			pSensorInfo->SensorDataLatchCount=2;
			
			pSensorInfo->SensorGrabStartX = IMAGE_SENSOR_VGA_INSERTED_PIXELS; 
			pSensorInfo->SensorGrabStartY = IMAGE_SENSOR_VGA_INSERTED_LINES;			   
		break;
	}
#endif
	
	SENSORDB("SP0828GetInfo \n");
#if 1
		pSensorInfo->SensorPreviewResolutionX = IMAGE_SENSOR_PV_WIDTH;
		pSensorInfo->SensorPreviewResolutionY = IMAGE_SENSOR_PV_HEIGHT;
		pSensorInfo->SensorFullResolutionX = IMAGE_SENSOR_PV_WIDTH;
		pSensorInfo->SensorFullResolutionY = IMAGE_SENSOR_PV_HEIGHT;
	
		pSensorInfo->SensorCameraPreviewFrameRate=30;
		pSensorInfo->SensorVideoFrameRate=30;
		pSensorInfo->SensorStillCaptureFrameRate=30;
		pSensorInfo->SensorWebCamCaptureFrameRate=30;
		pSensorInfo->SensorResetActiveHigh=FALSE;
		pSensorInfo->SensorResetDelayCount=1;
#endif
#if 0
		pSensorInfo->SensorOutputDataFormat=SENSOR_OUTPUT_FORMAT_YVYU;
		pSensorInfo->SensorClockPolarity=SENSOR_CLOCK_POLARITY_LOW;
		pSensorInfo->SensorClockFallingPolarity=SENSOR_CLOCK_POLARITY_LOW;
		pSensorInfo->SensorHsyncPolarity = SENSOR_CLOCK_POLARITY_LOW;
		pSensorInfo->SensorVsyncPolarity = SENSOR_CLOCK_POLARITY_LOW;
		pSensorInfo->SensorInterruptDelayLines = 1;
		pSensorInfo->SensroInterfaceType=SENSOR_INTERFACE_TYPE_PARALLEL;
		pSensorInfo->SensorMasterClockSwitch = 0;
#endif 
		pSensorInfo->SensorDrivingCurrent = ISP_DRIVING_8MA;
	
		pSensorInfo->SensorOutputDataFormat=SENSOR_OUTPUT_FORMAT_VYUY;
		pSensorInfo->SensorClockPolarity=SENSOR_CLOCK_POLARITY_LOW;
		pSensorInfo->SensorClockFallingPolarity=SENSOR_CLOCK_POLARITY_LOW;
		pSensorInfo->SensorHsyncPolarity = SENSOR_CLOCK_POLARITY_LOW;
		pSensorInfo->SensorVsyncPolarity = SENSOR_CLOCK_POLARITY_HIGH;
		   pSensorInfo->SensorDriver3D = 0;   // the sensor driver is 2D
		pSensorInfo->SensorInterruptDelayLines = 1;
		pSensorInfo->SensroInterfaceType=SENSOR_INTERFACE_TYPE_PARALLEL;
		
		pSensorInfo->SensorMasterClockSwitch = 0; 
	
	
		switch (ScenarioId)
		{
			case MSDK_SCENARIO_ID_CAMERA_PREVIEW:
			case MSDK_SCENARIO_ID_VIDEO_PREVIEW:
			case MSDK_SCENARIO_ID_VIDEO_CAPTURE_MPEG4:
				 
			case MSDK_SCENARIO_ID_CAMERA_CAPTURE_JPEG:
				
			case MSDK_SCENARIO_ID_CAMERA_CAPTURE_MEM:
			default:			
				pSensorInfo->SensorClockFreq=26;//26;
				pSensorInfo->SensorClockDividCount= 3;
				pSensorInfo->SensorClockRisingCount= 0;
				pSensorInfo->SensorClockFallingCount= 2;
				pSensorInfo->SensorPixelClockCount= 3;
				pSensorInfo->SensorDataLatchCount= 2;
				pSensorInfo->SensorGrabStartX = 1; 
				pSensorInfo->SensorGrabStartY = 1;		   
				break;
		}

	memcpy(pSensorConfigData, &SP0828SensorConfigData, sizeof(MSDK_SENSOR_CONFIG_STRUCT));
	
	return ERROR_NONE;
}	/* SP0828GetInfo() */


static kal_uint32 SP0828Control(MSDK_SCENARIO_ID_ENUM ScenarioId, MSDK_SENSOR_EXPOSURE_WINDOW_STRUCT *pImageWindow,
					  MSDK_SENSOR_CONFIG_STRUCT *pSensorConfigData)
{
	CAMERA_CONTROL_FLOW(ScenarioId,ScenarioId);

	switch (ScenarioId)
	{
		case MSDK_SCENARIO_ID_CAMERA_PREVIEW:
		case MSDK_SCENARIO_ID_VIDEO_PREVIEW:
		case MSDK_SCENARIO_ID_VIDEO_CAPTURE_MPEG4:
			SP0828_Preview(pImageWindow, pSensorConfigData);
		break;
		case MSDK_SCENARIO_ID_CAMERA_CAPTURE_JPEG:
		case MSDK_SCENARIO_ID_CAMERA_CAPTURE_MEM:
			//SP0828_Capture(pImageWindow, pSensorConfigData);
			SP0828_Preview(pImageWindow, pSensorConfigData);
		break;
		default:
			return ERROR_INVALID_SCENARIO_ID;
	}
	return TRUE;
}	/* MT9P012Control() */



static BOOL SP0828_set_param_wb(UINT16 para)
{
	kal_uint8  temp_reg;

	if(SP0828_Sensor_Driver.u8Wb_value==para)
		return FALSE;

	
	SP0828_Sensor_Driver.u8Wb_value = para;

	switch (para)
	 {
		 case AWB_MODE_OFF:
		 SP0828_write_cmos_sensor(0xfd,0x00);				   
		 SP0828_write_cmos_sensor(0x32,0x05);	   
		 break;
			 
		 case AWB_MODE_AUTO:
		 		//sp0828_reg_WB_auto        
		   SP0828_write_cmos_sensor(0xfd,0x01);                                                          
			SP0828_write_cmos_sensor(0x28,0x61);		                                                       
			SP0828_write_cmos_sensor(0x29,0x49);
			SP0828_write_cmos_sensor(0xfd,0x00);  // AUTO 3000K~7000K                                     
			SP0828_write_cmos_sensor(0x32,0x15); 	
			 break;
	
		 case AWB_MODE_CLOUDY_DAYLIGHT: //cloudy
		 	// sp0828_reg_WB_auto   
			SP0828_write_cmos_sensor(0xfd,0x00);   //7000K                                     
			SP0828_write_cmos_sensor(0x32,0x05);                                                          
			SP0828_write_cmos_sensor(0xfd,0x01);                                                          
			SP0828_write_cmos_sensor(0x28,0x71);		                                                       
			SP0828_write_cmos_sensor(0x29,0x41);		                                                       
            SP0828_write_cmos_sensor(0xfd,0x00);                                             
		break;
	
		 case AWB_MODE_DAYLIGHT: //sunny
		// sp0828_reg_WB_auto 
			SP0828_write_cmos_sensor(0xfd,0x00);  //6500K                                     
			SP0828_write_cmos_sensor(0x32,0x05);                                                          
			SP0828_write_cmos_sensor(0xfd,0x01);                                                          
			SP0828_write_cmos_sensor(0x28,0x6b);		                                                       
			SP0828_write_cmos_sensor(0x29,0x48);		                                                       
            SP0828_write_cmos_sensor(0xfd,0x00);                                                         
		break;
	
		 case AWB_MODE_INCANDESCENT: //office
		
		// sp0828_reg_WB_auto 
					SP0828_write_cmos_sensor(0xfd,0x00);  //2800K~3000K 									
					SP0828_write_cmos_sensor(0x32,0x05);														  
					SP0828_write_cmos_sensor(0xfd,0x01);														  
					SP0828_write_cmos_sensor(0x28,0x41);															   
					SP0828_write_cmos_sensor(0x29,0x71);															   
					SP0828_write_cmos_sensor(0xfd,0x00);														  
				break;
		
	
		 case AWB_MODE_TUNGSTEN: //home
			// sp0828_reg_WB_auto 
			SP0828_write_cmos_sensor(0xfd,0x00);  //4000K                                   
			SP0828_write_cmos_sensor(0x32,0x05);                                                          
			SP0828_write_cmos_sensor(0xfd,0x01);                                                          
			SP0828_write_cmos_sensor(0x28,0x5a);		                                                       
			SP0828_write_cmos_sensor(0x29,0x62);		                                                                                                             
			SP0828_write_cmos_sensor(0xfd,0x00);                                                         
		 break;
	   case AWB_MODE_FLUORESCENT:
			// SP0828_reg_WB_auto 
			SP0828_write_cmos_sensor(0xfd,0x00);  //4000K                                   
			SP0828_write_cmos_sensor(0x32,0x05);                                                          
			SP0828_write_cmos_sensor(0xfd,0x01);                                                          
			SP0828_write_cmos_sensor(0x28,0x57);		                                                       
			SP0828_write_cmos_sensor(0x29,0x66);		                                                                                                            
            SP0828_write_cmos_sensor(0xfd,0x00);                                                         
		break;
	
		 default:
			 return FALSE;
	 }


	return TRUE;
} /* SP0828_set_param_wb */


static BOOL SP0828_set_param_effect(UINT16 para)
{
	kal_uint32 ret = KAL_TRUE;

	if(para==SP0828_Sensor_Driver.u8Effect_value)
		return FALSE;

	
	SP0828_Sensor_Driver.u8Effect_value = para;
    switch (para)
    {
        case MEFFECT_OFF:
		SP0828_write_cmos_sensor(0xfd, 0x00);
		SP0828_write_cmos_sensor(0x62, 0x00);
		SP0828_write_cmos_sensor(0x63, 0x80);
		SP0828_write_cmos_sensor(0x64, 0x80);
            break;

        case MEFFECT_SEPIA:
		SP0828_write_cmos_sensor(0xfd, 0x00);
		SP0828_write_cmos_sensor(0x62, 0x20);
		SP0828_write_cmos_sensor(0x63, 0xc0);
		SP0828_write_cmos_sensor(0x64, 0x20);

            break;

        case MEFFECT_NEGATIVE:
		SP0828_write_cmos_sensor(0xfd, 0x00);
		SP0828_write_cmos_sensor(0x62, 0x10);//08
		SP0828_write_cmos_sensor(0x63, 0x80);
		SP0828_write_cmos_sensor(0x64, 0x80);
            break;

        case MEFFECT_SEPIAGREEN:
		SP0828_write_cmos_sensor(0xfd, 0x00);
		SP0828_write_cmos_sensor(0x62, 0x20);
		SP0828_write_cmos_sensor(0x63, 0x20);
		SP0828_write_cmos_sensor(0x64, 0x20);
            break;

        case MEFFECT_SEPIABLUE:
			SP0828_write_cmos_sensor(0xfd, 0x00);
			SP0828_write_cmos_sensor(0x62, 0x20);
			SP0828_write_cmos_sensor(0x63, 0x20);
			SP0828_write_cmos_sensor(0x64, 0xf0);


            break;
			
		case MEFFECT_MONO: //B&W
			SP0828_write_cmos_sensor(0xfd, 0x00);
			SP0828_write_cmos_sensor(0x62, 0x40);//10
			SP0828_write_cmos_sensor(0x63, 0x80);
			SP0828_write_cmos_sensor(0x64, 0x80);


			break;
        default:
            return FALSE;
    }

	return ret;

} /* SP0828_set_param_effect */

static void SP0828_set_banding_for_50Hz(void)
{
}


static void SP0828_set_banding_for_60Hz(void)
{
}

static BOOL SP0828_set_param_banding(UINT16 para)
{
	//if(SP0828_Sensor_Driver.bBanding_value == para)
	//	return TRUE;
	
	SP0828_Sensor_Driver.bBanding_value = para;
	
	switch (para)
	{
		case AE_FLICKER_MODE_50HZ:
			is_SP0828_Banding = 0;
			
			//SP0828_set_banding_for_50Hz();
			break;
		case AE_FLICKER_MODE_60HZ:
			is_SP0828_Banding = 1;
		//	SP0828_set_banding_for_60Hz();
			break;
		default:
			return FALSE;
	}

	return TRUE;
} /* SP0828_set_param_banding */

static BOOL SP0828_set_param_exposure(UINT16 para)
{
	if(para == SP0828_Sensor_Driver.u8Ev_value)
		return FALSE;

	SP0828_Sensor_Driver.u8Ev_value = para;

    switch (para)
    {
        case AE_EV_COMP_n13:
		SP0828_write_cmos_sensor(0xfd, 0x00);
		SP0828_write_cmos_sensor(0xdc, 0xc0);
            break;

        case AE_EV_COMP_n10:
		SP0828_write_cmos_sensor(0xfd, 0x00);
		SP0828_write_cmos_sensor(0xdc, 0xd0);
            break;

        case AE_EV_COMP_n07:
		SP0828_write_cmos_sensor(0xfd, 0x00);
		SP0828_write_cmos_sensor(0xdc, 0xe0);
            break;

        case AE_EV_COMP_n03:
		SP0828_write_cmos_sensor(0xfd, 0x00);
		SP0828_write_cmos_sensor(0xdc, 0xf0);
            break;

        case AE_EV_COMP_00:
		SP0828_write_cmos_sensor(0xfd, 0x00);
		SP0828_write_cmos_sensor(0xdc, 0x00);//0xfa before
            break;

        case AE_EV_COMP_03:
		SP0828_write_cmos_sensor(0xfd, 0x00);
		SP0828_write_cmos_sensor(0xdc, 0x10);
            break;

        case AE_EV_COMP_07:
		SP0828_write_cmos_sensor(0xfd, 0x00);
		SP0828_write_cmos_sensor(0xdc, 0x20);
            break;

        case AE_EV_COMP_10:
		SP0828_write_cmos_sensor(0xfd, 0x00);
		SP0828_write_cmos_sensor(0xdc, 0x30);
            break;

        case AE_EV_COMP_13:
		SP0828_write_cmos_sensor(0xfd, 0x00);
		SP0828_write_cmos_sensor(0xdc, 0x40);
            break;

        default:
            return FALSE;
    }


	return TRUE;
} /* SP0828_set_param_exposure */

static kal_uint32 SP0828_YUVSensorSetting(FEATURE_ID iCmd, UINT16 iPara)
{
/*
CMD: AE Strob(4)->AE Flick(0) -> AF Mode(6) -> AF Metring(7) -> AE metring(1)
-> EV(3) -> AWB(5) -> ISO(2) -> AE Scene Mode(13) ->Brightness(1) -> Hue(9)
-> Saturation(10) -> Edge(8) -> Contrast(12) -> Scene Mode(14) -> Effect(15)

For Current: Banding->EV->WB->Effect
*/
    //printk("[SP0828_YUVSensorSetting], Cmd = 0x%x, Para = 0x%x\n", iCmd, iPara); 
	//CAMERA_CONTROL_FLOW(iCmd,iPara);

	switch (iCmd) {
		case FID_SCENE_MODE:
                //printk("\n\nSP0828YUVSensorSetting:para=%d\n\n",iPara);
			
		    if (iPara == SCENE_MODE_OFF){
		        SP0828_night_mode(FALSE); 
		    }else if (iPara == SCENE_MODE_NIGHTSCENE){
               		SP0828_night_mode(TRUE);   
		    }	    
		    
		    break; 
		case FID_AWB_MODE:
			SP0828_set_param_wb(iPara);
		break;
		case FID_COLOR_EFFECT:
			SP0828_set_param_effect(iPara);
		break;
		case FID_AE_EV:	
			SP0828_set_param_exposure(iPara);
		break;
		case FID_AE_FLICKER:
			SP0828_set_param_banding(iPara);
		break;
		default:
		break;
	}
	
	return TRUE;
}   /* SP0828_YUVSensorSetting */

static kal_uint32 SP0828_YUVSetVideoMode(UINT16 u2FrameRate)
{
    kal_uint8 temp ;//= SP0828_read_cmos_sensor(0x3B);
    SP0828_Sensor_Driver.MPEG4_encode_mode = KAL_TRUE; 

    if (u2FrameRate == 30)
    {
    }
    else if (u2FrameRate == 15)       
    {
    }
    else 
    {
        printk("Wrong frame rate setting \n");
    }   
    
	printk("\n SP0828_YUVSetVideoMode:u2FrameRate=%d\n\n",u2FrameRate);
    return TRUE;
}

UINT32 SP0828SetSoftwarePWDNMode(kal_bool bEnable)
{
#if 0
    SENSORDB("[SP0828SetSoftwarePWDNMode] Software Power down enable:%d\n", bEnable);
    
    if(bEnable) {   // enable software sleep mode   
	 SP0828_write_cmos_sensor(0x09, 0x10);
    } else {
        SP0828_write_cmos_sensor(0x09, 0x03);  
    }
#endif
    return TRUE;
}

/*************************************************************************
* FUNCTION
*    SP0828_get_size
*
* DESCRIPTION
*    This function return the image width and height of image sensor.
*
* PARAMETERS
*    *sensor_width: address pointer of horizontal effect pixels of image sensor
*    *sensor_height: address pointer of vertical effect pixels of image sensor
*
* RETURNS
*    None
*
* LOCAL AFFECTED
*
*************************************************************************/
static void SP0828_get_size(kal_uint16 *sensor_width, kal_uint16 *sensor_height)
{
  *sensor_width = IMAGE_SENSOR_FULL_WIDTH; /* must be 4:3 */
  *sensor_height = IMAGE_SENSOR_FULL_HEIGHT;
}

/*************************************************************************
* FUNCTION
*    SP0828_get_period
*
* DESCRIPTION
*    This function return the image width and height of image sensor.
*
* PARAMETERS
*    *pixel_number: address pointer of pixel numbers in one period of HSYNC
*    *line_number: address pointer of line numbers in one period of VSYNC
*
* RETURNS
*    None
*
* LOCAL AFFECTED
*
*************************************************************************/
static void SP0828_get_period(kal_uint16 *pixel_number, kal_uint16 *line_number)
{
  *pixel_number = VGA_PERIOD_PIXEL_NUMS+SP0828_Sensor_Driver.dummy_pixels;
  *line_number = VGA_PERIOD_LINE_NUMS+SP0828_Sensor_Driver.dummy_lines;
}

/*************************************************************************
* FUNCTION
*    SP0828_feature_control
*
* DESCRIPTION
*    This function control sensor mode
*
* PARAMETERS
*    id: scenario id
*    image_window: image grab window
*    cfg_data: config data
*
* RETURNS
*    error code
*
* LOCAL AFFECTED
*
*************************************************************************/
static kal_uint32 SP0828FeatureControl(MSDK_SENSOR_FEATURE_ENUM id, kal_uint8 *para, kal_uint32 *len)
{
	UINT32 *pFeatureData32=(UINT32 *) para;

	switch (id)
	{
		case SENSOR_FEATURE_GET_RESOLUTION: /* no use */
			SP0828_get_size((kal_uint16 *)para, (kal_uint16 *)(para + sizeof(kal_uint16)));
			*len = sizeof(kal_uint32);
			break;
		case SENSOR_FEATURE_GET_PERIOD:
			SP0828_get_period((kal_uint16 *)para, (kal_uint16 *)(para + sizeof(kal_uint16)));
			*len = sizeof(kal_uint32);
			break;
		case SENSOR_FEATURE_GET_PIXEL_CLOCK_FREQ:
			*(kal_uint32 *)para = SP0828_Sensor_Driver.fPV_PCLK;
			*len = sizeof(kal_uint32);
			break;
		case SENSOR_FEATURE_SET_ESHUTTER:
			break;
		case SENSOR_FEATURE_SET_NIGHTMODE: 
			SP0828_night_mode((kal_bool)*(kal_uint16 *)para);
			break;
		case SENSOR_FEATURE_SET_GAIN:
		case SENSOR_FEATURE_SET_FLASHLIGHT:
		case SENSOR_FEATURE_SET_ISP_MASTER_CLOCK_FREQ:
			break;
		case SENSOR_FEATURE_SET_REGISTER:
			SP0828_write_cmos_sensor(((MSDK_SENSOR_REG_INFO_STRUCT *)para)->RegAddr, ((MSDK_SENSOR_REG_INFO_STRUCT *)para)->RegData);
			break;
		case SENSOR_FEATURE_GET_REGISTER: /* 10 */
			((MSDK_SENSOR_REG_INFO_STRUCT *)para)->RegData = SP0828_read_cmos_sensor(((MSDK_SENSOR_REG_INFO_STRUCT *)para)->RegAddr);
			break;
		case SENSOR_FEATURE_SET_CCT_REGISTER:
			memcpy(&SP0828_Sensor_Driver.eng.CCT, para, sizeof(SP0828_Sensor_Driver.eng.CCT));
			break;
		case SENSOR_FEATURE_GET_CCT_REGISTER:
		case SENSOR_FEATURE_SET_ENG_REGISTER:
		case SENSOR_FEATURE_GET_ENG_REGISTER:
		case SENSOR_FEATURE_GET_REGISTER_DEFAULT:
		case SENSOR_FEATURE_GET_CONFIG_PARA: /* no use */
			break;
		case SENSOR_FEATURE_CAMERA_PARA_TO_SENSOR:
			break;
		case SENSOR_FEATURE_SENSOR_TO_CAMERA_PARA:
			break;
		case SENSOR_FEATURE_GET_GROUP_COUNT:
		case SENSOR_FEATURE_GET_GROUP_INFO: /* 20 */
		case SENSOR_FEATURE_GET_ITEM_INFO:
		case SENSOR_FEATURE_SET_ITEM_INFO:
		case SENSOR_FEATURE_GET_ENG_INFO:
			break;
		case SENSOR_FEATURE_GET_LENS_DRIVER_ID:
		/*
		* get the lens driver ID from EEPROM or just return LENS_DRIVER_ID_DO_NOT_CARE
		* if EEPROM does not exist in camera module.
		*/
			*(kal_uint32 *)para = LENS_DRIVER_ID_DO_NOT_CARE;
			*len = sizeof(kal_uint32);
			break;
		case SENSOR_FEATURE_SET_YUV_CMD:
	//		SP0828_YUVSensorSetting((FEATURE_ID)(UINT32 *)para, (UINT32 *)(para+1));
			
			SP0828_YUVSensorSetting((FEATURE_ID)*pFeatureData32, *(pFeatureData32+1));
			break;
#if 0		    		
		case SENSOR_FEATURE_QUERY:
			SP0828_Query(pSensorFeatureInfo);
			*pFeatureParaLen = sizeof(MSDK_FEATURE_INFO_STRUCT);
			break;		
		case SENSOR_FEATURE_SET_YUV_CAPTURE_RAW_SUPPORT:
			/* update yuv capture raw support flag by *pFeatureData16 */
			break;		
#endif 			
		case SENSOR_FEATURE_SET_VIDEO_MODE:
			SP0828_YUVSetVideoMode(*para);
			break;
              case SENSOR_FEATURE_CHECK_SENSOR_ID:
                     SP0828_GetSensorID(pFeatureData32); 
                     break; 	
              case SENSOR_FEATURE_SET_SOFTWARE_PWDN:
                     SP0828SetSoftwarePWDNMode((BOOL)*pFeatureData32);        	        	
                     break;
		default:
			break;
	}
	return ERROR_NONE;
}




#if 0
image_sensor_func_struct image_sensor_driver_SP0828=
{
	SP0828Open,
	SP0828Close,
	SP0828GetResolution,
	SP0828GetInfo,
	SP0828Control,
	SP0828FeatureControl
};
void image_sensor_func_config(void)
{
	extern image_sensor_func_struct *image_sensor_driver;

	image_sensor_driver = &image_sensor_driver_SP0828;
}

#endif

SENSOR_FUNCTION_STRUCT	SensorFuncSP0828=
{
	SP0828Open,
	SP0828GetInfo, 
	SP0828GetResolution,
	SP0828FeatureControl,
	SP0828Control,
	SP0828Close
};

UINT32 SP0828_YUV_SensorInit(PSENSOR_FUNCTION_STRUCT *pfFunc)
{
	/* To Do : Check Sensor status here */
	if (pfFunc!=NULL)
		*pfFunc=&SensorFuncSP0828;

	return ERROR_NONE;
}	/* SensorInit() */




