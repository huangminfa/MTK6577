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

#ifndef PANO3D_CORE_H
#define PANO3D_CORE_H

#include "MTKPano3D.h"

#define SEAM_COST_THRES             (5)         // clamp cost to 0 if cost below threshold
#define PANO_MAX_IMG_NUM            PANO3D_MAX_IMG_NUM
#define MAX_SEAM_SIZE               PANO3D_IM_HEIGHT      // maximum seam array size (2M)
#define max_blend_width             (4)        //Max blending width
#define overflow_strength           (4)
#define blend_distance_threshold    (3)         //>0 (1, 2, 3...etc). the higher then value, the more the blending strength
#define max_enable_gain             (4096)      //Max compensation gain (4x = 4096, 3x = 3072, 2x=2048) 
#define BLEND_AVG_HEIGHT            (60)        // Number of points to decide blending width of each pixel 
#define BLEND_WIDTH_GAIN            (255)
#define g_seam_thr                  PANO3D_IM_WIDTH
#define g_disparity                 (1)
#define MAX_PYRAMID_SIZE            ((PANO3D_MAX_IMG_NUM-3)*511500)    // maximum pyramid size for WVGA
#define PANO_WORKING_BUFFER_SIZE    (PANO3D_IM_WIDTH*PANO3D_IM_HEIGHT*PANO3D_MAX_IMG_NUM)
#define PHOTO_GAIN_BIT              (20)                                // number of bit for gain
#define SMALL_SHIFT					(PHOTO_GAIN_BIT-16)					// rest number of bit for gain


typedef struct {
    // input
    MUINT32     ProcAddr;
    MUINT32     ImgAddr;
    MUINT32     ImgNum;
    MUINT32     ImgWidth;
    MUINT32     ImgHeight;
    PANO3D_IMAGE_FORMAT     ImgFmt;
    MUINT32                 ImgBufferAddr;

    // Added by CM Cheng, 2011-06-09
    // for 3D Panorama Application
    MINT16      mMinX[PANO3D_MAX_IMG_NUM];
    MINT16      mMaxX[PANO3D_MAX_IMG_NUM];
    MINT16      mGridX[PANO3D_MAX_IMG_NUM];
    MINT16      mClipY;
    MINT16      mClipHeight;
    MFLOAT      mGain[PANO3D_MAX_IMG_NUM];

    MFLOAT      Hmtx[PANO3D_MAX_IMG_NUM][9];
    MINT16      pano_bound[4];                                  // (start_x, start_y, end_x, end_y)
    MINT32      xbound_in_pano[2];                              // horizontal boundary in pano coordinate
    MUINT8      *image_mask[2];
    MUINT8      *left_its_mask, *right_its_mask;                // intersection mask of left/right panoramas
    MUINT8      *left_union_mask, *right_union_mask;            // union mask of left/right panoramas
    MUINT8      *left_cost, *right_cost, *down_cost, *tmp_cost; // seam cost buffer
    MUINT32     left_selection[PANO3D_MAX_IMG_NUM];             // image selection for left panorama
    MUINT32     num_selection;
    MINT32      seam_left[PANO_MAX_IMG_NUM][MAX_SEAM_SIZE];     // best seam for the left view
    MINT32      LocalWidth;                                     // overlap width
    MINT32      LocalHeight;                                    // overlap height
    MINT32      LocalPara[7];                                   // local parameters
    MINT32      ImageIdx[4];                                    // image indices
    MINT32      ShiftX;                                         // local shift x
    MINT32      final_cost;

    // simple seam blending
    MINT32      *seam_pos;
    MINT32      *seam_cost;
    MINT8       *seam_dir;

    // multi-band blending
    MUINT8      *mb_src;
    MUINT8      *mb_dst;
    MUINT8      *mb_mask;
    MFLOAT      *gpyr;
    MFLOAT      *lpyr;
    MFLOAT      *mpyr;
    MFLOAT      v1y[PANO3D_IM_WIDTH];

    // output
    MUINT32     PanoWidth;
    MUINT32     PanoHeight;
    MUINT32     LeftPanoImageAddr;
    MUINT32     RightPanoImageAddr;
    MINT32      ROI[4];

    // debug info
    MINT32      middle_line[PANO3D_MAX_IMG_NUM-1];              // middle point of overlaping regions

} pano3d_cal_struct;

void PanoInit(Pano3DImageInfo *ImageInfo, Pano3DResultInfo *ResultInfo);
MRESULT PhotoGain(MINT32 flag);
void PhotoAdjust(MINT32 ImgIdx, MINT32 flag);
MRESULT ImageSelection();
MRESULT SeamFinding(MINT32 ImgIdx, Pano3DResultInfo *ResultInfo);
MRESULT PanoCore(MINT32 ImgIdx, Pano3DResultInfo *ResultInfo);
void Pano3DErrControl(Pano3DResultInfo *ResultInfo);

// multi-band blending related functions
void cmImageBlend( MUINT8 *dst, MUINT8 *src, MUINT8 *mask, MINT32 width, MINT32 height, MINT32 img_num );
void BuildGPyr( float *img, int *addr, int *height, int *width, int level, int img_idx );
void BuildLPyr( float *lpyr, float *gpyr, int *addr, int *width, int *height, int level, int img_idx );
void ReconstSPyr( MUINT8 *dst, float *spyr, int *addr, int *width, int *height, int level );

#endif /* PANO3D_CORE_H */
