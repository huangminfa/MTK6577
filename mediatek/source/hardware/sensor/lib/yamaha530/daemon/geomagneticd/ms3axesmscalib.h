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
  Copyright(c) 2006-2010 Yamaha Corporation
*/
/*!
  @file ms3axesmscalib.h
  @brief auto-calibration library
  @date 2010/06/23
  @note revision: 22
*/

#ifndef __MS3AXESMSCALIB_H_INCLUDED__
#define __MS3AXESMSCALIB_H_INCLUDED__

#define MS3AXESMSCALIB_MAX_SAMPLE_POINTS    (16)
#define MS3AXESMSCALIB_NUM_LEVEL            (3)

#include "ms3axestypes.h"
#include "ms3axesmscalib_features.h"
#include "ms3axesmscalib_algo.h"
#include "ms3axesmscalib_data.h"

/* This value must be even and greater than or equal to 4. */

typedef struct {
    MSSINT32 val3d;
    MSSINT32 val2d;
    MSSINT32 distortion;
    MSSINT8 level;
    MSSINT8 accuracy;
    MSSINT32 radius;
    MSSINT8 axis;
} Ms3AxesMsCalibResult;

typedef struct {
    Ms3AxesMsCalibAlgo algo;
    Ms3AxesMsCalibData data;
} Ms3AxesMsCalib;

#ifdef __cplusplus
extern "C" {
#endif

/*!
  @brief
  Initializes the calibration module of magnetic sensor.
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT Ms3AxesMsCalibInit(Ms3AxesMsCalib *pCalib);

/*!
  @brief Sets threshold values of calibration.
  @param[in] pThreshold
  A pointer to the threshold values.
  @return
  A non-negative number for success, or a negative number for failure.
  @note
  This function is designed for temporary use.
*/
MSINT Ms3AxesMsCalibGetThreshold(Ms3AxesMsCalib *pCalib,
                                 Ms3AxesMsCalibThreshold *pThreshold);
MSINT Ms3AxesMsCalibSetThreshold(Ms3AxesMsCalib *pCalib,
                                 const Ms3AxesMsCalibThreshold *pThreshold);

/*!
  @brief
  Updates the state of the calibration module. By calling this
  at every measurement, the fine offset of the magnetic sensor
  will be obtained.
  @param[in] pCalib
  A pointer to the calibration module.
  @param[in] pData
  A pointer to the measured value of the magnetic sensor.
  @param[out] pResult
  A pointer to the calibration result.
  @return
  A non-negative number for success, or a negative number for failure.
  Zero means that the fine offset has not been changed.
  Positive number means the fine offset has been changed.
  @note
  The function "Ms3AxesMsCalibInit()" must be called at least once before
  calling this function.
*/
MSINT Ms3AxesMsCalibUpdate(Ms3AxesMsCalib *pCalib,
                                 const Ms3AxesData *pData,
                                 Ms3AxesMsCalibResult *pResult);

/*!
  @brief
  Gets the fine offset value which is calculated by this module.
  @param[out] pFineOffset
  A pointer to an argument to which you want to assign the fine offset value.
  @return
  A non-negative number for success, or a negative number for failure.
  @note
  The function "Ms3AxesMsCalibInit()" must be called at least once before
  calling this function.
 */
MSINT Ms3AxesMsCalibGetOffset(Ms3AxesMsCalib *pCalib, Ms3AxesData *pFineOffset);

/*!
  @brief
  Sets the fine offset to this module.
  @param[out] pFineOffset
  A pointer to the fine offset value.
  @return
  A non-negative number for success, or a negative number for failure.
  @note
  The function "Ms3AxesMsCalibInit()" must be called at least once before
  calling this function.
 */
MSINT Ms3AxesMsCalibSetOffset(Ms3AxesMsCalib *pCalib,
                              const Ms3AxesData *pFineOffset);

/*!
  @brief
  Gets the offset status. The offset status is a value equivalent to the
  three LSBs of the return value of "Ms3AxesMsCalibUpdate()".
  This function does not involve any change to the calibration module.
  @return
  A non-negative number for success, or a negative number for failure.
  If the value is not negative, it is equivalent to the three LSBs of
  "Ms3AxesMsCalibUpdate()".
  @note
  The function "Ms3AxesMsCalibInit()" must be called at least once before
  calling this function.
  This function is designed for a special use.
 */
MSINT Ms3AxesMsCalibGetAccuracy(Ms3AxesMsCalib *pCalib);

/*!
  @brief
  Sets the offset status. The offset status is a value equivalent to the
  three LSBs of the return value of "Ms3AxesMsCalibUpdate()".
  This function does not involve any change to the calibration module
  except for the value of the offset status.
  @param[in] status
  A value to be set as the offset status.
  @return
  A non-negative number for success, or a negative number for failure.
  @note
  The function "Ms3AxesMsCalibInit()" must be called at least once before
  calling this function.
  If "Ms3AxesMsCalibInit()" is called, the offset status is reset to zero.
  This function is designed for a special use.
 */
MSINT Ms3AxesMsCalibSetAccuracy(Ms3AxesMsCalib *pCalib, MSINT status);

#ifdef __cplusplus
}
#endif

#endif /* #define __MS3AXESMSCALIB_H_INCLUDED__ */
