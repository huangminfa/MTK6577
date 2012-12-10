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
  CONFIDENTIAL
  Copyright(c) 2006-2009 Yamaha Corporation
*/
/*!
  @file ms3cdrv.h
  @brief A header file of ms3cdrv.c
  @date 2009/09/07
  @note revision: 19
*/

#ifndef __MS3CDRV_H_INCLUDED__
#define __MS3CDRV_H_INCLUDED__

#include "ms3axesdrv.h"

#define MS3AXESDRV_ROUGHOFFSET_MEASURE_OF_VALUE 33
#define MS3AXESDRV_ROUGHOFFSET_MEASURE_UF_VALUE  0
#define MS3AXESDRV_NORMAL_MEASURE_OF_VALUE 1024
#define MS3AXESDRV_NORMAL_MEASURE_UF_VALUE    1

/*
  Values below are defined to make the absolute values of
  'pData->x' and 'pData->y' small as possible.
  If they are defined as the middle of the measurable range,
  the statistical expectations of them become zero.
*/
# define MS3AXESDRV_CENTER_X  512
# define MS3AXESDRV_CENTER_Y1 512
# define MS3AXESDRV_CENTER_Y2 512
# define MS3AXESDRV_CENTER_T  256
# define MS3AXESDRV_CENTER_I1 512
# define MS3AXESDRV_CENTER_I2 512
# define MS3AXESDRV_CENTER_I3 512

typedef struct {
    MSSINT16 val[3][3];
} Ms3cMatrix;

/*! MS-3C device handle */
typedef struct {
    MSUINT8 raw_calreg[9]; /*!< raw data of CAL register */
    Ms3AxesRoughOffset roughoffset; /*!< roughoffset written to the Rough Offset Register (OFFSETR) */
    MSSINT8 roughoffset_is_set; /*!< a flag for whether roughoffset is set or not */
    Ms3cMatrix correction_m; /*!< a matrix for sensitivity correction and mount correction of MS magnetic sensor */
    MSSINT8 temp_coeff[3];
    MSSINT8 IsInitialized; /*!< Flag whether the driver is initialized or not. */
} Ms3cDrv;

#ifdef __cplusplus
extern "C" {
#endif

/*!
  @brief
  Measures magnetic field normally and corrects the value
  to a temperature-compensated, orthogonalized, normalized and
  coordinate-transformed one.
  @param[out] pMsensData
  A pointer to the measured magnetic field value which is
  temperature-compensated, orthogonalized, normalized and
  coordinate-transformed.
  @param[out] pRawData
  "NULL" should be assigned to this argument in normal use.
  @return
  A non-negative number for success, or a negative number for failure.
  If the number is 1, overflow or underflow of measured value of X occurred.
  If 2, that of Y did.
  If 3, those of both X and Y1 did.
  If 4, that of  Y2 did.
  If 5, those of both X and Y2 did.
  If 6, those of both Y1 and Y2 did.
  If 7, those of all did.
  @note
  The member of pRawData related to acceleration value would not be changed
  by this function.
*/
MSINT Ms3cDrvMsMeasure(Ms3AxesData *pMsensData, Ms3cRawData *pRawData);

/*!
  @brief
  Measures acceleration value and corrects the value to a normalized
  and coordinate-transformed one.
  @param[out] pGsensData
  A pointer to the measured acceleration value which is
  normalized and coordinate-transformed.
  @param[out] pRawData
  "NULL" should be assigned to this argument in normal use.
  @return
  A non-negative number for success, or a negative number for failure.
  @note
  The size of the vector "pGsensData" is normalized as 0x1000 for 1G.
  The members of pRawData related to magnetic field value and temperature
  would not be changed by this function.
*/
MSINT Ms3cDrvGsMeasure(Ms3AxesData *pGsensData, Ms3cRawData *pRawData);

/*!
  @brief
  Returns a pointer to the MS-3C driver.
  @return
  A pointer to the MS-3c driver.
*/
const Ms3cDrv *Ms3cDrvHandle(void);

/*!
  @brief
  Sets a value to the GPO of MS-3C.
  @param[in] val
  If 0, the GPO of MS-3C is set to L, otherwise H.
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT Ms3cDrvSetGPO(MSUINT8 val);

MSINT Ms3AxesDrvMsensCorrection(const Ms3cRawDataElement *pRawData,
                                       MSUINT16 temperature,
                                       Ms3AxesData *pData);

#ifdef __cplusplus
}
#endif

#endif /* __MS3CDRV_H_INCLUDED__ */
