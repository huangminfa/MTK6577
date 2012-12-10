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
 *   local_main_mp4_enc.h 
 *
 * @par Project:
 *   MT6575 
 *
 * @par Description:
 *   local main test code header file
 *
 * @par Author:
 *   Jackal Chen (mtk02532)
 *
 * @par $Revision: #1 $
 * @par $Modtime:$
 * @par $Log:$
 *
 */

#ifndef _LOCAL_MAIN_MP4_DEC_H_
#define _LOCAL_MAIN_MP4_DEC_H_

#include "val_types.h"
#include "vcodec_if.h"

#ifdef __cplusplus
 extern "C" {
#endif

/*=============================================================================
 *                              Type definition
 *===========================================================================*/
#define MAX_FILE_NAME_SIZE  (100)

typedef struct {
    VAL_DRIVER_TYPE_T   eCodecType;
    VAL_CHAR_T			cInFile[MAX_FILE_NAME_SIZE];
    VAL_CHAR_T			cOutFile[MAX_FILE_NAME_SIZE];
    VAL_CHAR_T			cGoldenPath[MAX_FILE_NAME_SIZE];
    VAL_BOOL_T          bUseGolden;
    VAL_VOID_T          *pvExtraParam;
} INPUT_CONFIG_T;

typedef struct {
    VAL_UINT32_T            u4FrameWidth;
    VAL_UINT32_T            u4FrameHeight;
    VAL_UINT32_T            u4BitRate;
    VAL_UINT32_T            u4FrameRate;    //15fps => u4FrameRate = 150, 15.2fps => u4FrameRate = 152
    VAL_BOOL_T              fgShortHeaderMode;
    VAL_UINT32_T            u4IntraVOPRate;
    VCODEC_ENC_QUALITY_T    eQualityLevel;
    VCODEC_ENC_CODEC_T      eCodecType;
    VAL_UINT32_T            u4RotateAngle;
} INPUT_CONFIG_MP4ENC_T;

/*=============================================================================
 *                             Function Declaration
 *===========================================================================*/
VAL_BOOL_T LocalMainMp4Enc(INPUT_CONFIG_T *a_prConfig);

#ifdef __cplusplus
}
#endif

#endif // #ifndef _LOCAL_MAIN_MP4_DEC_H_
