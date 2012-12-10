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

#ifndef _PANO_WARP_SW_CORE_H
#define _PANO_WARP_SW_CORE_H

#include "MTKWarp.h"

#define OVLP_RATIO          (32)                // overlap ratio (base = 64, OVLP_RATIO = 16=>16/64=1/4, OVLP_RATIO = 21=>21/64~=1/3, OVLP_RATIO = 26=>26/64~=0.4)

typedef struct{
    MUINT32                 ProcBufAddr;        // working buffer address
    MUINT8                  *ImgAddr;           // input image address
    MUINT8                  *ImgBufferAddr;     // image buffer address
    MUINT32                 ImgNum;             // input image number
    WARP_IMAGE_FORMAT       ImgFmt;             // input image format
    MUINT32                 ImgWidth;           // input image width
    MUINT32                 ImgHeight;          // input image height
    MUINT32                 Flength;            // input focal length
    MTKWARP_DIRECTION_ENUM  Direction;          // input capture direction
} projection_sw_cal_struct;

typedef struct sColor{
    MUINT8 r;
    MUINT8 g;
    MUINT8 b;
    MUINT8 luma;
} tColor;

void CalculateProjectionImgSize(MINT32 y_min, MINT32 y_max, MINT32 x_max, MINT32 x_min, 
                                MUINT32* new_width, MUINT32* new_height, MUINT32 flength, 
                                MINT32* v_min_o, MINT32* u_min_o, MUINT32* inv_flength);
void CylindricalProjectionYUV420(MUINT8* image_data_y, MUINT8* image_data_u, MUINT8* image_data_v, 
                                 MUINT32 radius, MUINT32 flength, MINT32 x_min, MINT32 x_max, MINT32 y_min, MINT32 y_max,
                                 MUINT32 ori_width, MUINT32 ori_height, MUINT32 new_height, MUINT32 new_width, 
                                 MINT32 v_min, MINT32 u_min, MUINT32 inv_flength);
MUINT32 BilinearInterpolation_Y(MUINT32 y, MUINT32 x, MUINT8 mode_x, MUINT8 mode_y, MUINT8* refImg_y, MINT32 width);
tColor BilinearInterpolation_UV420(MUINT32 y, MUINT32 x,  MUINT8 mode_x,  MUINT8 mode_y, MUINT8* refImg_u, MUINT8* refImg_v, MINT32 width);
void SwProjectionInit(WarpImageInfo *);
void SwProjection(MINT32, WarpResultInfo *);

#endif
