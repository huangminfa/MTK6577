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
 *
 *       Filename:  options.h
 *
 *    Description:  
 *
 *        Version:  1.0
 *        Created:  12/27/2011 02:39:07 PM
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  Zhengguang.Guo@bosch-sensortec.com
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

#ifndef __OPTIONS_H
#define __OPTIONS_H
#include "sensor_def.h"
#include "configure.h"

#define CFG_HANDLE_FAULT_SIG
#define CFG_DISCARD_OLD_CMD
#define CFG_ALGO_USE_ADV_PRE_FILTER
#define CFG_SET_AXIS_FROM_FILE


#define CFG_HW_DEP_A (1 << SENSOR_HANDLE_ACCELERATION)

#define CFG_HW_DEP_G ((1 << SENSOR_HANDLE_ACCELERATION)\
		| (1 << SENSOR_HANDLE_MAGNETIC_FIELD))

#define CFG_HW_DEP_M (1 << SENSOR_HANDLE_MAGNETIC_FIELD)

#define CFG_HW_DEP_O ((1 << SENSOR_HANDLE_ACCELERATION)\
		| (1 << SENSOR_HANDLE_MAGNETIC_FIELD))

#define CFG_HW_DEP_RO CFG_HW_DEP_O

#define CFG_HW_DEP_VG (1 << SENSOR_HANDLE_ACCELERATION)
#define CFG_HW_DEP_VLA (1 << SENSOR_HANDLE_ACCELERATION)
#define CFG_HW_DEP_VRV ((1 << SENSOR_HANDLE_ACCELERATION)\
		| (1 << SENSOR_HANDLE_MAGNETIC_FIELD))

#define CFG_HW_DEP_D (1 << SENSOR_HANDLE_PROXIMITY)
#define CFG_HW_DEP_L (1 << SENSOR_HANDLE_LIGHT)

#define CFG_HW_DEP_P (1 << SENSOR_HANDLE_PRESSURE)
#define CFG_HW_DEP_T (1 << SENSOR_HANDLE_TEMPERATURE)


#define CFG_DELAY_A_MIN 10
#define CFG_DELAY_A_MAX 100

#define CFG_DELAY_M_MIN 20
#define CFG_DELAY_M_MAX 100

#if CFG_SENSOR_G_EMU
#define CFG_DELAY_G_MIN 20
#define CFG_DELAY_G_MAX 20
#else
#define CFG_DELAY_G_MIN 10
#define CFG_DELAY_G_MAX 100
#endif

#define CFG_DELAY_O_MIN 20
#define CFG_DELAY_O_MAX 100

#define CFG_DELAY_RO_MIN CFG_DELAY_O_MIN
#define CFG_DELAY_RO_MAX CFG_DELAY_O_MAX

#define CFG_DELAY_VG_MIN CFG_DELAY_A_MIN
#define CFG_DELAY_VG_MAX CFG_DELAY_A_MAX

#define CFG_DELAY_VLA_MIN CFG_DELAY_A_MIN
#define CFG_DELAY_VLA_MAX CFG_DELAY_A_MAX

#define CFG_DELAY_VRV_MIN CFG_DELAY_M_MIN
#define CFG_DELAY_VRV_MAX CFG_DELAY_M_MAX


#define CFG_DELAY_D_MIN 20
#define CFG_DELAY_D_MAX 200

#define CFG_DELAY_L_MIN 20
#define CFG_DELAY_L_MAX 200

#define CFG_DELAY_P_MIN 20
#define CFG_DELAY_P_MAX 200

#define CFG_DELAY_T_MIN 20
#define CFG_DELAY_T_MAX 200

#endif
