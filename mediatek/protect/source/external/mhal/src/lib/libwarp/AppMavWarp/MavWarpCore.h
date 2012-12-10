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

#ifndef _MAV_WARP_GPU_CORE_H
#define _MAV_WARP_GPU_CORE_H

#include "MTKWarp.h"
#include "imagpu.h"

#define V_WIDTH     1
#define V_HEIGHT    1

#define GPU_WARP_MATRIX_SIZE    (9*WARP_MAX_IMG_NUM)
#define VTX_DATA_SIZE           ((V_WIDTH+1)*2*V_HEIGHT)
#define ELE_DATA_SIZE           (6*V_WIDTH*V_HEIGHT)


typedef struct{
    // image parameters
    MUINT32             ProcBufAddr;            // working buffer address
    MUINT8              *ImgAddr;               // input image address
    MUINT32             ImgNum;                 // input image number
    WARP_IMAGE_FORMAT   ImgFmt;                 // input image format
    MUINT32             ImgWidth;               // input image width
    MUINT32             ImgHeight;              // input image height
    MFLOAT              *WarpMatrix;            // warping matrix pointer
    MINT32              roi_x[WARP_MAX_IMG_NUM];// ROI horizontal coordinate
    MINT32              roi_y[WARP_MAX_IMG_NUM];// ROI vertical coordiante
    MUINT32             roi_width;              // ROI width
    MUINT32             roi_height;             // ROI height
    MUINT8              *ImgBufferAddr;         // buffer image address

    // opengl parameters
    vtx_fmt             *VtxData;               // vertex data pointer
    MUINT16             *EleData;               // element data pointer
    GLuint              uiProgramObject;        // program object handle
    GLuint              uiVertShader;           // vertex shader handle
    GLuint              uiFragShader;           // fragment shader handle
    GLuint              uiVBO[2];               // VPE object handle
    GLuint              fbo;                    // framebuffer object
    GLuint              rbo;                    // renderbuffer object
} warp_cal_struct;

void WarpingInit(WarpImageInfo *, WarpResultInfo *);
void Warping(MINT32, WarpResultInfo *);
void WarpingFinish(void);

#endif
