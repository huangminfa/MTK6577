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
  @file ms3cdrv_common.h
  @brief declaration of the common functions of 'ms3cdrv.c'
  and a acceleration-sensor-dependent file.
  @date 2009/07/31
  @note revision: 4
*/

#ifndef __MS3CDRV_COMMON_H_INCLUDED__
#define __MS3CDRV_COMMON_H_INCLUDED__

#include "mstypes.h"

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

/*
  The functions below are defined in "ms3cdrv.c".
*/
MSINT Ms3cDrvMeasurePreparation(void);
MSINT Ms3cDrvTransform(const Ms3cMatrix *pMatrix,
                       const Ms3cRawDataElement *pRawData,
                       Ms3AxesData *pData);
MSINT Ms3cDrvMakeCorrectionMatrix(const Ms3AxesTransformation *pTransformation,
                                  const Ms3cMatrix *pMatrix,
                                  Ms3cMatrix *pAns);

/*
  The functions below are defined in a file other than "ms3cdrv.c"
*/

MSINT Ms3cDrvGsInit(const Ms3AxesTransformation *pGsensTransformation,
                    MSUINT16 gsens_resolution);

MSINT Ms3cDrvGsTerm(void);

#ifdef __cplusplus
}
#endif /* __cplusplus */

#endif /* __MS3CDRV_COMMON_H_INCLUDED__ */
