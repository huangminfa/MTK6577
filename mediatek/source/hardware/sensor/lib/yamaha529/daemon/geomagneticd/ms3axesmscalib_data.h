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
  Copyright(c) 2009 Yamaha Corporation
*/
/*!
  @file ms3axesmscalib_data.h
  @brief sub-module of auto calibration library.
  @date 2009/11/04
  @note revision: 1
*/

#ifndef __MS3AXESMSCALIB_DATA_H_INCLUDED__
#define __MS3AXESMSCALIB_DATA_H_INCLUDED__

#include "ms3axestypes.h"

#if (MS3AXESMSCALIB_NUM_LEVEL == 1)
#define MS3AXESMSCALIBDATA_DISTANCE_THRESH      (20)
#elif (MS3AXESMSCALIB_NUM_LEVEL == 2)
#define MS3AXESMSCALIBDATA_DISTANCE_THRESH      (20)
#elif (MS3AXESMSCALIB_NUM_LEVEL == 3)
#define MS3AXESMSCALIBDATA_DISTANCE_THRESH      (20)
#endif

typedef struct Ms3AxesMsCalibData {
    Ms3AxesData data[MS3AXESMSCALIB_MAX_SAMPLE_POINTS*MS3AXESMSCALIB_NUM_LEVEL];
    MSSINT8     num;
    MSSINT8     index;
    MSSINT8     level;
} Ms3AxesMsCalibData;

#ifdef __cplusplus
extern "C" {
#endif

MSINT Ms3AxesMsCalibData_Init(Ms3AxesMsCalibData *pThis);
MSINT Ms3AxesMsCalibData_AddToBuffer(Ms3AxesMsCalibData *pCalibData,
                                     const Ms3AxesData *pData);
void Ms3AxesMsCalibData_NotifyHighDistortion(Ms3AxesMsCalibData *pCalibData);

#ifdef __cplusplus
}
#endif

#endif /* #define __MS3AXESMSCALIB_DATA_H_INCLUDED__ */
