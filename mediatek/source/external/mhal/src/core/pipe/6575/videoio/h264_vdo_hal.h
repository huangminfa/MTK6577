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
 *   mpeg4_vdo_hal.h
 *
 * @par Project:
 *   MT6575 
 *
 * @par Description:
 *   mpeg4 enc driver and mhal integration wrapper
 *
 * @par Author:  
 *   Jackal Chen (mtk02532)
 *
 * @par $Revision: #1 $
 * @par $Modtime:$
 * @par $Log:$
 *
 */
 
#ifndef __H264_VDO_HAL_H__
#define __H264_VDO_HAL_H__

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
INT32 mHalVdoH264Init(mhalCamVdoParam_t *pmhalCamVdoParam, mhalCamFrame_t *pfrmBS, MUINT32 bsIdx, mhalVdoDrv_t *pmhalVdoDrv);
INT32 mHalVdoH264EncodeFrame(mhalCamFrame_t *pfrmYuv, MUINT32 yuvIdx, mhalCamFrame_t *pfrmBS, MUINT32 bsIdx,  mhalVdoDrv_t *pmhalVdoDrv);
INT32 mHalVdoH264Uninit(mhalVdoDrv_t *pmhalVdoDrv);
INT32 mHalVdoH264GetParam(MHAL_VDO_PARAM_T eCtrlCode, MVOID *pBuffer, MUINT32 u4BufSize);
INT32 mHalVdoH264SetParam(MHAL_VDO_PARAM_T eCtrlCode, MVOID *pBuffer, MUINT32 u4BufSize);

#ifdef __cplusplus
}
#endif

#endif //#ifndef __H264_VDO_HAL_H__
