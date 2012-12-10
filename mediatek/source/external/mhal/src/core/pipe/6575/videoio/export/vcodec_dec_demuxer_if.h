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

#ifndef VCODEC_DEC_DEMUXER_IF_H
#define VCODEC_DEC_DEMUXER_IF_H

#include "vcodec_if.h"

typedef struct 
{
    unsigned int u4Address;
    unsigned int u4Length;
    int fgValid;
} RM_DECODER_PAYLOAD_INFO_T;

typedef struct
{
    unsigned int u4PayloadNumber;
    RM_DECODER_PAYLOAD_INFO_T* pu1PayloadAddress;
} RM_DECODER_INPUT_PARAM_T;

typedef enum
{
	RV8 = 0,
	RV9,
	RV10
}RM_CODEC_VERSION_T;

#define MAX_NUM_RPR_SIZES 8
typedef struct
{
    RM_CODEC_VERSION_T eDecoderVersion;
    unsigned int u4MaxDimWidth;
    unsigned int u4MaxDimHeight;
    unsigned int u4NumRPRSizes;
    unsigned int au4RPRSizes[2*MAX_NUM_RPR_SIZES];
} RM_DECODER_INIT_PARAM_T;

// The H264 uses the private data to transfer NAL units
// The related data structure informations are defined as below
//

typedef struct 
{
    unsigned int u4Address;
    unsigned int u4Length;
} H264_DECODER_PAYLOAD_INFO_T;

typedef struct
{
    unsigned int u4PayloadNumber;
    H264_DECODER_PAYLOAD_INFO_T* pu1PayloadAddress;
} H264_DECODER_INPUT_PARAM_T;

typedef struct 
{
    unsigned int u4Address;
    unsigned int u4Length;
} MPEG4_DECODER_PAYLOAD_INFO_T;

typedef struct
{
    unsigned int u4PayloadNumber;
    H264_DECODER_PAYLOAD_INFO_T* pu1PayloadAddress;
} MPEG4_DECODER_INPUT_PARAM_T;

typedef struct 
{
    VCODEC_BUFFER_T 	rPayload;
    unsigned int 		u4Length;
} VP8_DECODER_INPUT_UNIT_T;

#endif /* VCODEC_DEC_DEMUXER_IF_H */ 

