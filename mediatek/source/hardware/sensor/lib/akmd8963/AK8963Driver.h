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

/******************************************************************************
 *
 * $Id: AK8963Driver.h 303 2011-08-12 04:22:45Z kihara.gb $
 *
 * -- Copyright Notice --
 *
 * Copyright (c) 2004 Asahi Kasei Microdevices Corporation, Japan
 * All Rights Reserved.
 *
 * This software program is proprietary program of Asahi Kasei Microdevices
 * Corporation("AKM") licensed to authorized Licensee under Software License
 * Agreement (SLA) executed between the Licensee and AKM.
 *
 * Use of the software by unauthorized third party, or use of the software
 * beyond the scope of the SLA is strictly prohibited.
 *
 * -- End Asahi Kasei Microdevices Copyright Notice --
 *
 ******************************************************************************/
#ifndef AKMD_INC_AK8963DRIVER_H
#define AKMD_INC_AK8963DRIVER_H

#include <linux/sensors_io.h>	/* Device driver */
#include <stdint.h>			/* int8_t, int16_t etc. */

#define SENSOR_DATA_SIZE	8
#define YPR_DATA_SIZE		12
#define RWBUF_SIZE		16

#define ACC_DATA_FLAG		0
#define MAG_DATA_FLAG		1
#define ORI_DATA_FLAG		2
#define AKM_NUM_SENSORS		3

#define ACC_DATA_READY		(1<<(ACC_DATA_FLAG))
#define MAG_DATA_READY		(1<<(MAG_DATA_FLAG))
#define ORI_DATA_READY		(1<<(ORI_DATA_FLAG))

/*! \name AK8963 constant definition
 \anchor AK8963_Def
 Constant definitions of the AK8963.*/
#define AK8963_MEASUREMENT_TIME_US	10000

/*! \name AK8963 operation mode
 \anchor AK8963_Mode
 Defines an operation mode of the AK8963.*/
/*! @{*/
#define AK8963_MODE_SNG_MEASURE	0x01
#define	AK8963_MODE_SELF_TEST	0x08
#define	AK8963_MODE_FUSE_ACCESS	0x0F
#define	AK8963_MODE_POWERDOWN	0x00

/*! @}*/

/*! \name AK8963 register address
\anchor AK8963_REG
Defines a register address of the AK8963.*/
/*! @{*/
#define AK8963_REG_WIA		0x00
#define AK8963_REG_INFO		0x01
#define AK8963_REG_ST1		0x02
#define AK8963_REG_HXL		0x03
#define AK8963_REG_HXH		0x04
#define AK8963_REG_HYL		0x05
#define AK8963_REG_HYH		0x06
#define AK8963_REG_HZL		0x07
#define AK8963_REG_HZH		0x08
#define AK8963_REG_ST2		0x09
#define AK8963_REG_CNTL1	0x0A
#define AK8963_REG_CNTL2	0x0B
#define AK8963_REG_ASTC		0x0C
#define AK8963_REG_TS1		0x0D
#define AK8963_REG_TS2		0x0E
#define AK8963_REG_I2CDIS	0x0F
/*! @}*/

/*! \name AK8963 fuse-rom address
\anchor AK8963_FUSE
Defines a read-only address of the fuse ROM of the AK8963.*/
/*! @{*/
#define AK8963_FUSE_ASAX	0x10
#define AK8963_FUSE_ASAY	0x11
#define AK8963_FUSE_ASAZ	0x12
/*! @}*/


/*** Constant definition ******************************************************/
#define AKD_TRUE	1		/*!< Represents true */
#define AKD_FALSE	0		/*!< Represents false */
#define AKD_SUCCESS	1		/*!< Represents success.*/
#define AKD_FAIL	0		/*!< Represents fail. */
#define AKD_ERROR	-1		/*!< Represents error. */

/*! 0:Don't Output data, 1:Output data */
#define AKD_DBG_DATA	0
/*! Typical interval in ns */
#define AK8963_MEASUREMENT_TIME_NS	((AK8963_MEASUREMENT_TIME_US) * 1000)
/*! 720 LSG = 1G = 9.8 m/s2 */
#define LSG			720


/*** Type declaration *********************************************************/
typedef unsigned char BYTE;

/*!
 Open device driver.
 This function opens device driver of acceleration sensor.
 @return If this function succeeds, the return value is #AKD_SUCCESS. Otherwise
 the return value is #AKD_FAIL.
 */
typedef int16_t(*ACCFNC_INITDEVICE)(void);

/*!
 Close device driver.
 This function closes device drivers of acceleration sensor.
 */
typedef void(*ACCFNC_DEINITDEVICE)(void);

/*!
 Acquire acceleration data from acceleration sensor and convert it to Android
 coordinate system.
 @return If this function succeeds, the return value is #AKD_SUCCESS. Otherwise
 the return value is #AKD_FAIL.
 @param[out] data A acceleration data array. The coordinate system of the
 acquired data follows the definition of Android. Unit is SmartCompass.
 */
typedef int16_t(*ACCFNC_GETACCDATA)(short data[3]);


/*** Global variables *********************************************************/
extern int g_file;

/*** Prototype of Function  ***************************************************/

int16_t AKD_InitDevice(void);

void AKD_DeinitDevice(void);

int16_t AKD_TxData(
	const BYTE address,
	const BYTE* data,
	const uint16_t numberOfBytesToWrite);

int16_t AKD_RxData(
	const BYTE address,
	BYTE* data,
	const uint16_t numberOfBytesToRead);

int16_t AKD_ResetAK8963(void);

int16_t AKD_GetMagneticData(BYTE data[SENSOR_DATA_SIZE]);

void AKD_SetYPR(const int buf[YPR_DATA_SIZE]);

int AKD_GetOpenStatus(int* status);

int AKD_GetCloseStatus(int* status);

int16_t AKD_SetMode(const BYTE mode);

//int16_t AKD_GetDelay(int64_t delay[AKM_NUM_SENSORS]);
int16_t AKD_GetDelay(int32_t *delay);

int16_t AKD_GetLayout(int16_t* layout);

int16_t AKD_GetOutbit(int16_t* outbit);

int16_t AKD_GetAccelerationData(int16_t data[3]);

#endif //AKMD_INC_AK8963DRIVER_H

