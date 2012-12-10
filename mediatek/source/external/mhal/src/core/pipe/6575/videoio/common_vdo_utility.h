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

/** 
 * @file 
 *   common_vdo_utility.h
 *
 * @par Project:
 *   MT6575 
 *
 * @par Description:
 *   vidoe enc driver and mhal integration wrapper
 *
 * @par Author:
 *   Jackal Chen (mtk02532)
 *
 * @par $Revision: #1 $
 * @par $Modtime:$
 * @par $Log:$
 *
 */
 
#ifndef __COMMON_VDO_UTILITY_H__
#define __COMMON_VDO_UTILITY_H__

/*=============================================================================
 *                              Include Files
 *===========================================================================*/

#include "MediaHal.h"
#include "common_vdo_hal.h"

#ifdef __cplusplus
extern "C" {
#endif

/*=============================================================================
 *                              Type definition
 *===========================================================================*/


/*=============================================================================
 *                             Function Declaration
 *===========================================================================*/
VAL_VOID_T mHalVdoMemAllocAligned(VAL_VOID_T *handle, VAL_UINT32_T size, VCODEC_MEMORY_TYPE_T cachable, VCODEC_BUFFER_T *pBuf);
VAL_VOID_T mHalVdoMemFree(VAL_VOID_T *handle, VCODEC_BUFFER_T *pBuf);
VAL_VOID_T mHalVdoReleaseYUV(VAL_VOID_T *handle, VCODEC_BUFFER_T *pBuf);
VAL_VOID_T mHalVdoPaused(VAL_VOID_T *handle, VCODEC_BUFFER_T *pBuf);
VAL_VOID_T mHalVdoAllocateBitstreamBuffer(VAL_VOID_T *handle, VCODEC_ENC_BUFFER_INFO_T *pBuffer);
VAL_VOID_T mHalVdoUpdateBitstreamWP(VAL_VOID_T *handle, VCODEC_ENC_UPDATE_WP_INTO_T *pInfo);

#ifdef __cplusplus
}
#endif

#endif //#ifndef __COMMON_VDO_UTILITY_H__
