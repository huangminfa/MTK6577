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

#ifndef __PANO_CORE_H__
#define __PANO_CORE_H__

#include "kal_release.h"
#include "fb_comm_def.h"


typedef enum
{
    FB_IDLE = 0,
    FB_ALPHA_MAP,
    FB_BLEND_TEXTURE_IMG,
    FB_BLEND_COLOR_IMG,
    FB_STATE_MAX
} FB_CTRL_ENUM;

typedef enum
{
    FB_PROCESS_OK,
    FB_PROCESS_ERROR_STATE

} FB_ERROR_ENUM;

typedef struct
{
    kal_int32 WorkMode;
    kal_int32 SkinToneEn;
    kal_int32 BlurLevel;
    kal_int32 AlphaBackground;
    kal_int32 ZoomRatio;
    kal_int32 TargetColor;
    kal_int32 InputDsWidth;
    kal_int32 InputDsHeight;
    kal_int32 InputWidth;
    kal_int32 InputHeight;
    kal_int32 FDWidth;
    kal_int32 FDHeight;
    MM_IMAGE_FORMAT_ENUM SrcImgFormat;


} FB_ENV_INFO_STRUCT, *P_FB_ENV_INFO_STRUCT;
    
typedef struct
{
    FB_CTRL_ENUM CtrlEnum;
    kal_uint32 WorkingBuffer;               // the address of working memory
    kal_uint32 WorkingBufferSize;           // working memory size
    kal_uint8* ImgSrcDSAddr;    // source image buffer address 
    kal_uint8* ImgSrcAddr;    // source image buffer address 
    kal_uint8* ImgSrcBlurAddr;    // source image buffer address 
    kal_int32* FDLeftTopPointX1; //start x position of face in FD image (320x240)
    kal_int32* FDLeftTopPointY1; //start y position of face in FD image (320x240)
    kal_int32* FDBoxSize;		 //size of face in FD image (320x240)
    kal_int32* FDPose;             //Direction of face (0: 0 degree, 1: 15 degrees, 2: 30 degrees, and the like
    kal_int32  FaceCount;              //Number of face in current image
    kal_uint8*  AlphaMap;
    kal_uint8*  TexBlendResultAddr;
    kal_uint8*  TexBlendAndYSResultAddr;
    kal_uint8*  AlphaMapColor;
    kal_uint8*  PCAImgAddr;
    kal_uint8*  ColorBlendResultAddr;

    
  
} FB_PROC_INFO_STRUCT, *P_FB_PROC_INFO_STRUCT;

typedef struct
{
    kal_uint32 AlphaMapDsAddr;
    kal_uint32 AlphaMapColorDsAddr;
    kal_uint32 AlphaMapDsCrzWidth;
    kal_uint32 AlphaMapDsCrzHeight;
    kal_uint32 AlphaMapColorDsCrzWidth;
    kal_uint32 AlphaMapColorDsCrzHeight;
    kal_int32  AngleRange[2];
    kal_uint32 PCAYTable;
    kal_uint32 PCASTable;
    kal_uint32 PCAHTable;
    kal_uint8* BlendTextureImgAddr;
    kal_uint8* BlendTextureAndYSImgAddr;
    kal_uint8* BlendColorImgAddr;
  
} FB_RESULT_STRUCT, *P_FB_RESULT_STRUCT;


FB_ERROR_ENUM FBCoreSetEnvInfo(P_FB_ENV_INFO_STRUCT pFBEnvInfo);
FB_ERROR_ENUM FBCoreSetProcInfo(P_FB_PROC_INFO_STRUCT pFBProcInfo);
FB_ERROR_ENUM FBCoreGetResult(FB_RESULT_STRUCT* pFBResultInfo);
FB_ERROR_ENUM FBCoreProcess();
FB_ERROR_ENUM FBCoreExit();

#endif
