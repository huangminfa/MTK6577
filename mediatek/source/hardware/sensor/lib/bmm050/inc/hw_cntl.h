/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/*
 * =====================================================================================
 * Copyright (C) 2011 Bosch Sensortec GmbH
 *
 *       Filename:  hw_cntl.h
 *
 *    Description:  
 *
 *        Version:  1.0
 *        Created:  03/23/2011 05:02:19 PM
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  Zhengguang.Guo@bosch-sensortec.com
 *
 *        Company:  
 *
 * =====================================================================================
 */

/*************************************************************************************************/
/*  Disclaimer
*
* Common:
* Bosch Sensortec products are developed for the consumer goods industry. They may only be used
* within the parameters of the respective valid product data sheet.  Bosch Sensortec products are
* provided with the express understanding that there is no warranty of fitness for a particular purpose.
* They are not fit for use in life-sustaining, safety or security sensitive systems or any system or device
* that may lead to bodily harm or property damage if the system or device malfunctions. In addition,
* Bosch Sensortec products are not fit for use in products which interact with motor vehicle systems.
* The resale and/or use of products are at the purchasers own risk and his own responsibility. The
* examination of fitness for the intended use is the sole responsibility of the Purchaser.
*
* The purchaser shall indemnify Bosch Sensortec from all third party claims, including any claims for
* incidental, or consequential damages, arising from any product use not covered by the parameters of
* the respective valid product data sheet or not approved by Bosch Sensortec and reimburse Bosch
* Sensortec for all costs in connection with such claims.
*
* The purchaser must monitor the market for the purchased products, particularly with regard to
* product safety and inform Bosch Sensortec without delay of all security relevant incidents.
*
* Engineering Samples are marked with an asterisk (*) or (e). Samples may vary from the valid
* technical specifications of the product series. They are therefore not intended or fit for resale to third
* parties or for use in end products. Their sole purpose is internal client testing. The testing of an
* engineering sample may in no way replace the testing of a product series. Bosch Sensortec
* assumes no liability for the use of engineering samples. By accepting the engineering samples, the
* Purchaser agrees to indemnify Bosch Sensortec from all claims arising from the use of engineering
* samples.
*
* Special:
* This software module (hereinafter called "Software") and any information on application-sheets
* (hereinafter called "Information") is provided free of charge for the sole purpose to support your
* application work. The Software and Information is subject to the following terms and conditions:
*
* The Software is specifically designed for the exclusive use for Bosch Sensortec products by
* personnel who have special experience and training. Do not use this Software if you do not have the
* proper experience or training.
*
* This Software package is provided `` as is `` and without any expressed or implied warranties,
* including without limitation, the implied warranties of merchantability and fitness for a particular
* purpose.
*
* Bosch Sensortec and their representatives and agents deny any liability for the functional impairment
* of this Software in terms of fitness, performance and safety. Bosch Sensortec and their
* representatives and agents shall not be liable for any direct or indirect damages or injury, except as
* otherwise stipulated in mandatory applicable law.
*
* The Information provided is believed to be accurate and reliable. Bosch Sensortec assumes no
* responsibility for the consequences of use of such Information nor for any infringement of patents or
* other rights of third parties which may result from its use. No license is granted by implication or
* otherwise under any patent or patent rights of Bosch. Specifications mentioned in the Information are
* subject to change without notice.
*
* It is not allowed to deliver the source code of the Software to any third party without permission of
* Bosch Sensortec.
*/

#ifndef __HW_CNTL_H
#define __HW_CNTL_H

#include "options.h"
#include "sensor_priv.h"

#define DELAY_TIME_UI_PROXIMITY (60000)	/* max delay response time of user interaction in ms */

struct axis_remap {
	unsigned int rx: 2;
	unsigned int ry: 2;
	unsigned int rz: 2;

	unsigned int sx: 2;
	unsigned int sy: 2;
	unsigned int sz: 2;
};


#if SPT_SENSOR_A
int hc_init_a();
void hc_destroy_a();

BS_U8 hc_acc_read_xyzdata(BS_S16 *px, BS_S16 *py, BS_S16 *pz);
BS_U8 hc_acc_get_grange(BS_U8 *pvalue);
BS_U8 hc_acc_set_grange(BS_U8 value);
BS_U8 hc_acc_get_opmode(BS_U8 *pvalue);
BS_U8 hc_acc_set_opmode(BS_U8 value);
#if !(DAEMON_FOR_MTK)
BS_U8 hc_acc_get_enable(BS_U8 *pvalue);
BS_U8 hc_acc_set_enable(BS_U8 value);
#endif //DAEMON_FOR_MTK
BS_U8 hc_acc_get_bandwidth(BS_U8 *pvalue);
BS_U8 hc_acc_set_bandwidth(BS_U8 value);
#endif

#if SPT_SENSOR_M
int hc_init_m();
void hc_destroy_m();

BS_U8 hc_mag_get_opmode(BS_U8 *pvalue);
BS_U8 hc_mag_set_opmode(BS_U8 value);
BS_U8 hc_mag_read_xyzdata(BS_S16 *px, BS_S16 *py, BS_S16 *pz);
#endif

#if SPT_SENSOR_G && !CFG_SENSOR_G_EMU
int hc_init_g();
void hc_destroy_g();

BS_S8 hc_gyro_read_xyzdata(BS_S16 *px, BS_S16 *py, BS_S16 *pz);
BS_S8 hc_gyro_get_opmode(BS_U8 *value);
BS_S8 hc_gyro_set_opmode(BS_U8 value);
BS_S8 hc_gyro_get_power_mode(BS_U8 *value);
BS_S8 hc_gyro_set_power_mode(BS_U8 value);
BS_S8 hc_gyro_get_bw(BS_U8 *value);
BS_S8 hc_gyro_set_bw(BS_U8 value);
BS_S8 hc_gyro_get_range(BS_U8 *value);
BS_S8 hc_gyro_set_range(BS_U8 value);
BS_S8 hc_gyro_get_srd(BS_U8 *value);
BS_S8 hc_gyro_set_srd(BS_U8 value);
#endif

#if SPT_SENSOR_D
int hc_init_d();
void hc_destroy_d();

int hc_prx_enable(int enable);
int hc_prx_read_value(int *pvalue);
int hc_prx_read_value_nb(int *pvalue);
int hc_prx_set_delay(int delay);
#endif


#if SPT_SENSOR_L
int hc_init_l();
void hc_destroy_l();

int hc_light_read_data(float *data);
int hc_light_read_value_nb(float *pvalue);

int hc_light_set_enable(int enable);
int hc_light_set_delay(int delay);
#endif


#if 0
#define DFT_AXIS_REMAP_CFG_ACC_X 0
#define DFT_AXIS_REMAP_CFG_ACC_Y 0
#define DFT_AXIS_REMAP_CFG_ACC_Z 0

#define AXIS_REMAP_CFG_X2X 0
#define AXIS_REMAP_CFG_Y2X 1
#define AXIS_REMAP_CFG_Z2X 2

#define AXIS_REMAP_CFG_Y2Y 0
#define AXIS_REMAP_CFG_X2Y 1
#define AXIS_REMAP_CFG_Z2Y 2

#define AXIS_REMAP_CFG_Z2Z 0
#define AXIS_REMAP_CFG_X2Z 1
#define AXIS_REMAP_CFG_Y2Z 2


#define DFT_AXIS_REMAP_SIGN_CFG_ACC_X 0
#define DFT_AXIS_REMAP_SIGN_CFG_ACC_Y 0
#define DFT_AXIS_REMAP_SIGN_CFG_ACC_Z 0

#define DFT_AXIS_REMAP_CFG_MAG_X 0
#define DFT_AXIS_REMAP_CFG_MAG_Y 0
#define DFT_AXIS_REMAP_CFG_MAG_Z 0

#define DFT_AXIS_REMAP_SIGN_CFG_MAG_X 0
#define DFT_AXIS_REMAP_SIGN_CFG_MAG_Y 0
#define DFT_AXIS_REMAP_SIGN_CFG_MAG_Z 0
#endif

#endif
