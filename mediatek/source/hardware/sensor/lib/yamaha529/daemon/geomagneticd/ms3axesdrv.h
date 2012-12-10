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
   @file ms3axesdrv.h
   @brief Interface of the MS three axes driver.
   @date 2009/10/21
   @note revision: 15
*/

#ifndef __MS3AXESDRV_H_INCLUDED__
#define __MS3AXESDRV_H_INCLUDED__

#include "ms3axestypes.h"
#include "ms3axesdrv_features.h"

#define MS3AXESDRV_NO_ERROR 0
#define MS3AXESDRV_ERR_ARG (-1)
/* #define MS3AXESDRV_ERR_ALREADY_INITIALIZED (-2) */
#define MS3AXESDRV_ERR_NOT_INITIALIZED (-3)
#define MS3AXESDRV_ERR_BUSY (-4)
#define MS3AXESDRV_ERR_I2CCTRL (-5)
#define MS3AXESDRV_ERR_ROUGHOFFSET_NOT_WRITTEN (-126)
#define MS3AXESDRV_ERROR (-127)

#define MS3AXESDRV_MEASURE_X_OFUF  0x1
#define MS3AXESDRV_MEASURE_Y1_OFUF 0x2
#define MS3AXESDRV_MEASURE_Y2_OFUF 0x4

/*! mount information */
typedef struct {
    MSSINT8 matrix[3][3]; /*!< coordinate transformation matrix */
} Ms3AxesTransformation;

/*! rough offset value written to the 'Rough Offset Register (OFFSETR)' */
typedef struct {
    MSUINT8 x[3]; /*!< rough offset value of X/Y1/Y2 axes */
} Ms3AxesRoughOffset;

#ifdef __cplusplus
extern "C" {
#endif

/*!
 Initializes the GS three axes driver.
  @param[in] pGsensTransformation
  A pointer to a coordinate transformation matrix for the G-sensor.
  @param[in] gsens_resolution
  Sensitivity of G-sensor in [mV/gravity].
  @return
  A non-negative number for success, or a negative number for failure.
  @note
*/
MSINT Ms3AxesDrvGsInit(const Ms3AxesTransformation *pGsensTransformation,
                       MSUINT16 gsens_resolution);

/*!
  Initializes the MS three axes driver.
  @param[in] pMsensTransformation
  A pointer to a coordinate transformation matrix for the MS magnetic sensor.
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT Ms3AxesDrvMsInit(const Ms3AxesTransformation *pMsensTransformation);

/*!
  Terminates the GS three axes driver.
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT Ms3AxesDrvGsTerm(void);

/*!
  Terminates the MS three axes driver.
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT Ms3AxesDrvMsTerm(void);

/*!
  Operates the initialization coils.
  @return
  A non-negative number for success, or a negative number for failure.
 */
MSINT Ms3AxesDrvMsInitCoil(void);

/*!
  Measures rough offset value.
  @param[out] pRoughOffset
  A pointer to an argument to which you want to assign the measured rough
  offset value.
  @return
  A non-negative number for success, or a negative number for failure.
  If the number is 1, overflow or underflow of measured value of X occurred.
  If 2, that of Y1 did.
  If 3, those of both X and Y1 did.
  If 4, that of  Y2 did.
  If 5, those of both X and Y2 did.
  If 6, those of both Y1 and Y2 did.
  If 7, those of all did.
*/
MSINT Ms3AxesDrvMsMeasureRoughOffset(Ms3AxesRoughOffset *pRoughOffset);

/*!
  Writes a rough offset value which you specified to the
  "Rough Offset Register (OFFSETR)",
  or after calculating a value based on the measured rough offset
  writes it to the register if pRoughOffset is assigned as "NULL".
  @param[in] pRoughOffset
  A pointer to rough offset value which you want to write to the
  "Rough Offset Register (OFFSETR)".
  If NULL, a value which is calculated based on the measured rough offset
  is written to the register.
  @return
  A non-negative number for success, or a negative number for failure.
  If the number is 1, overflow or underflow of measured value of X occurred.
  If 2, that of Y1 did.
  If 3, those of both X and Y1 did.
  If 4, that of  Y2 did.
  If 5, those of both X and Y2 did.
  If 6, those of both Y1 and Y2 did.
  If 7, those of all did.
  Positive number appears only when 'pRoughOffset' is assigned as 'NULL'.
*/
MSINT Ms3AxesDrvMsSetRoughOffset(const Ms3AxesRoughOffset *pRoughOffset);

/*!
  Gets a rough offset value which is written to the
  "Rough Offset Register (OFFSETR)".
  @param[out] pRoughOffset
  A pointer to an argument to which you want to assign the written rough
  offset value of the "Rough Offset Register (OFFSETR)".
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT Ms3AxesDrvMsGetWrittenRoughOffset(Ms3AxesRoughOffset *pRoughOffset);

/*!
  Measures acceleration value and corrects the value to a normalized
  and coordinate-transformed one.
  @param[out] pGsensData
  A pointer to the measured acceleration value which is
  normalized and coordinate-transformed.
  @param[out] pRawData
  A pointer to the measured raw acceleration value
  @return
  A non-negative number for success, or a negative number for failure.
  @note
  The size of the vector "pGsensData" is normalized as 0x1000 for 1G.
*/
MSINT Ms3AxesDrvGsMeasure(Ms3AxesData *pGsensData, Ms3cRawData *pRawData);

/*!
  Measures magnetic field normally and corrects the value
  to a coordinate-transformed one.
  @param[out] pMsensData
  A pointer to the measured magnetic field value which is coordinate-transformed.
  @param[out] pRawData
  A pointer to the measured raw magnetic field value
  @return
  A non-negative number for success, or a negative number for failure.
  If the number is 1, overflow or underflow of measured value of X occurred.
  If 2, that of Y1 did.
  If 3, those of both X and Y1 did.
  If 4, that of  Y2 did.
  If 5, those of both X and Y2 did.
  If 6, those of both Y1 and Y2 did.
  If 7, those of all did.
*/
MSINT Ms3AxesDrvMsMeasure(Ms3AxesData *pMsensData, Ms3cRawData *pRawData);

/*!
  Re-calculate MS fine offset values when the rough offset changes
  @param[in] pPrevOffset
  @param[out] pNewOffset
  @param[in] pPrevRoughOffset
  @param[in] pNewRoughOffset
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT Ms3AxesDrvMsRecalcOffset(Ms3AxesData *pPrevOffset,
        Ms3AxesData *pNewOffset,
        Ms3AxesRoughOffset *pPrevRoughOffset,
        Ms3AxesRoughOffset *pNewRoughOffset);

#ifdef __cplusplus
}
#endif

#endif /* __MS3AXESDRV_H_INCLUDED__ */
