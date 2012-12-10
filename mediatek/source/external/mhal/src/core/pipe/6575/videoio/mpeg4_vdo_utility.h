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
 *   mpeg4_vdo_utility.h
 *
 * @par Project:
 *   MFlexVideo 
 *
 * @par Description:
 *   mpeg4 enc utility function implementation
 *
 * @par Author:
 *   Jackal Chen (mtk02532)
 *
 * @par $Revision: #1 $
 * @par $Modtime:$
 * @par $Log:$
 *
 */

#ifndef __MPEG4_VDO_UTILITY_H__
#define __MPEG4_VDO_UTILITY_H__

/*=============================================================================
 *                              Include Files
 *===========================================================================*/

#include "mpeg4_vdo_hal.h"
#include "venc_drv_if.h"

#ifdef __cplusplus
extern "C" {
#endif

/*=============================================================================
 *                              Type definition
 *===========================================================================*/
typedef struct INPUT_CONFIG_MP4ENC_s{
    VAL_UINT32_T    u4FrameNum;     
    VAL_UINT32_T    u4IntraPeriod;                  // DMEM[231]: iPeriodOfIntraFrames
    VAL_UINT32_T    u4FrameRate;                    // DMEM[24]:  frame_rate
    VAL_BOOL_T      fgpmv;                          // DMEM[113]: pmv
    VAL_UINT32_T    u4FrameWidth;                   // MFV_ENC_CONFIG, MFV_CONFIG
    VAL_UINT32_T    u4FrameHeight;                  // MFV_ENC_CONFIG, MFV_CONFIG
    VAL_UINT32_T    u4BitRate;                      // DMEM[23]:  bitrate
    VAL_BOOL_T      fgnewMD;                        // DMEM[80]:  newmd
    VAL_BOOL_T      fgHdrCbr;                       // DMEM[230]: hdr_cbr
    VAL_BOOL_T      fgShortHeaderMode;              // DMEM[1]:   short_video_header
    VAL_UINT32_T    u4VOPTimeIncrementResolution;   // DMEM[74]:  vop_time_increment_resolution
    VAL_UINT32_T    u4fcode;                        // DMEM[2]:   vop_fcode
    VAL_BOOL_T      fgResyncMarkerEn;               // DMEM[70]:  resync_en
    VAL_BOOL_T      fgResyncMarkerInsertMode;       // DMEM[71]:  resync_marker_insert_mode
    VAL_UINT32_T    u4ResyncMarkerPeriod;           // DMEM[72]:  resync_marker_period
    VAL_BOOL_T      fgHeaderExtensionCode;          // DMEM[73]:  header_extension_code  
    VAL_BOOL_T      fgVOPRoundingType;              // DMEM[3]:   vop_rounding_type
    VAL_UINT32_T    u4StepLimit;                    // DMEM[114]: step_limit
    VAL_BOOL_T      fgInsertIntra;                  // DMEM[100]: insert_intra
    VAL_UINT32_T    u4InsertPeriod;                 // DMEM[101]: insert_period
    /*EIS Support*/
    VAL_BOOL_T      fgEISEnable;                    // Enable EIS feature
    VAL_UINT32_T    u4EISFrameWidth;                // EIS Frame Width
    VAL_UINT32_T    u4EISFrameHeight;               // EIS Frame Height
    VAL_UINT32_T    u4EIS_GMV_X;                    // MFV_EIS_CONFIG, GMV_X
    VAL_UINT32_T    u4EIS_GMV_Y;                    // MFV_EIS_CONFIG, GMV_Y
} INPUT_CONFIG_MP4ENC_T;

/*=============================================================================
 *                             Function Declaration
 *===========================================================================*/

VAL_BOOL_T QueryDriverMp4Enc(VENC_DRV_QUERY_VIDEO_FORMAT_T *a_prQueryInfo, VENC_DRV_PROPERTY_T *a_prPropertyInfo);
VAL_BOOL_T RCSettingMp4Enc(VAL_HANDLE_T a_Handle, INPUT_CONFIG_MP4ENC_T *a_pMP4EncConfig);
VAL_BOOL_T MESettingMp4Enc(VAL_HANDLE_T a_Handle, INPUT_CONFIG_MP4ENC_T *a_pMP4EncConfig);
VAL_BOOL_T EncSettingMp4Enc(VAL_HANDLE_T a_Handle, INPUT_CONFIG_MP4ENC_T *a_pMP4EncConfig);
VAL_BOOL_T EISSettingMp4Enc(VAL_HANDLE_T a_Handle, INPUT_CONFIG_MP4ENC_T *a_pMP4EncConfig, VAL_UINT32_T a_u4FrameCount);

#ifdef __cplusplus
}
#endif

#endif //#ifndef __MPEG4_VDO_UTILITY_H__
