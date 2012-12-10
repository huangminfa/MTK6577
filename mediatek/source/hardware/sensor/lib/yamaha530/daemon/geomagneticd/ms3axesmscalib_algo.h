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
  @file ms3axesmscalib_algo.h
  @brief sub-module of auto calibration library.
  @date 2010/06/23
  @note revision: 20
*/

#ifndef __MS3AXESMSCALIB_ALGO_H_INCLUDED__
#define __MS3AXESMSCALIB_ALGO_H_INCLUDED__

#include "ms3axestypes.h"
#include "msfloat.h"

#define MS3AXESMSCALIBALGO_NUM_THRESHOLDS           (3)
#define MS3AXESMSCALIBALGO_2D_THRESHOLD             (1000)

typedef struct {
    MSSINT32 val3d[9];
    MSSINT32 distortion[3];
} Ms3AxesMsCalibThreshold;

typedef struct {
    Ms3AxesData m_Center;
    Ms3AxesMsCalibThreshold m_Threshold;
    MSINT m_Accuracy;
} Ms3AxesMsCalibAlgo;

typedef struct {
    MSSINT32 val3d;
    MSSINT32 val2d;
    MSSINT32 distortion;
    MSSINT8 accuracy;
    MSSINT32 radius;
    MSSINT8 axis;
} Ms3AxesMsCalibAlgo_Result;

#ifdef __cplusplus
extern "C" {
#endif

MSINT Ms3AxesMsCalibAlgo_Init(Ms3AxesMsCalibAlgo *pThis);
MSINT Ms3AxesMsCalibAlgo_Update(Ms3AxesMsCalibAlgo *pThis,
                                Ms3AxesData *pSamplePoints,
                                MSSINT32 num,
                                Ms3AxesMsCalibAlgo_Result *pResult);
MSINT Ms3AxesMsCalibAlgo_SetOffset(Ms3AxesMsCalibAlgo *pThis,
                                 const Ms3AxesData *pFineOffset);
MSINT Ms3AxesMsCalibAlgo_GetOffset(const Ms3AxesMsCalibAlgo *pThis,
                                 Ms3AxesData *pFineOffset);
MSINT Ms3AxesMsCalibAlgo_GetThreshold(const Ms3AxesMsCalibAlgo *pThis,
                                      Ms3AxesMsCalibThreshold *pThreshold);
MSINT Ms3AxesMsCalibAlgo_SetThreshold(Ms3AxesMsCalibAlgo *pThis,
                                      const Ms3AxesMsCalibThreshold *pThreshold);
MSINT Ms3AxesMsCalibAlgo_GetAccuracy(const Ms3AxesMsCalibAlgo *pThis);
MSINT Ms3AxesMsCalibAlgo_SetAccuracy(Ms3AxesMsCalibAlgo *pThis, MSINT accuracy);

#ifdef __cplusplus
}
#endif

#endif /* #define __MS3AXESMSCALIB_ALGO_H_INCLUDED__ */
