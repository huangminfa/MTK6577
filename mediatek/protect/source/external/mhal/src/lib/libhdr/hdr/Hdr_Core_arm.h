/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/********************************************************************************************
 *     LEGAL DISCLAIMER
 *
 *     (Header of MediaTek Software/Firmware Release or Documentation)
 *
 *     BY OPENING OR USING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *     THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE") RECEIVED
 *     FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON AN "AS-IS" BASIS
 *     ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES, EXPRESS OR IMPLIED,
 *     INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 *     A PARTICULAR PURPOSE OR NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY
 *     WHATSOEVER WITH RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK
 *     ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
 *     NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S SPECIFICATION
 *     OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
 *
 *     BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE LIABILITY WITH
 *     RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION,
TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE
 *     FEES OR SERVICE CHARGE PAID BY BUYER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *     THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE WITH THE LAWS
 *     OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF LAWS PRINCIPLES.
 ************************************************************************************************/
 
#ifndef _HDR_CORE_ARM_H_
#define _HDR_CORE_ARM_H_

#include "MTKHdr.h"
#include "MTKHdrType.h"

typedef struct Matrix
{
	MINT32 cols;
	MINT32 rows;
	MUINT8* data;
} Matrix, *pMatrix;

typedef struct YuvImage
{
	Matrix* Y;
	Matrix* U;
	Matrix* V;
} YuvImage, *pYuvImage;

typedef struct YuvMatrix
{
    Matrix Y;
    Matrix U;
    Matrix V;
}YuvMatrix, *pYuvMatrix;

typedef struct CropInfo
{
	int startX;
	int startY;
	int endX;
	int endY;
} CropInfo;

typedef struct HDR_UPSAMPLE_THREAD_PARA
{
	MINT32 start_row;
	MINT32 end_row;
	MINT32 upsample_start_row;
	MINT32 upsample_end_row;
	Matrix* I; 
	int odd_rows;
	int odd_cols;
	Matrix* upsampled;
}HDR_UPSAMPLE_THREAD_PARA, *P_HDR_UPSAMPLE_THREAD_PARA;

typedef struct HDR_DOWNSAMPLE_THREAD_PARA
{
	MINT32 start_row;
	MINT32 end_row;
	Matrix* I; 
	Matrix* downsampled;
	Matrix* WorkingMartrix;
}HDR_DOWNSAMPLE_THREAD_PARA, *P_HDR_DOWNSAMPLE_THREAD_PARA;

typedef struct HDR_SUBSTRACT_THREAD_PARA
{
    Matrix SrcMatrixA;
    Matrix SrcMatrixB;
    int residue;
    Matrix DstMatrix;
}HDR_SUBSTRACT_THREAD_PARA, *P_HDR_SUBSTRACT_THREAD_PARA;

// multi-core parameter structure for CopyYuvImage and AddEqualsYuvImage
typedef struct HDR_PROC_YUV_THREAD_PARA
{
    YuvMatrix YuvMatrixLaplasPyramid;
    YuvMatrix YuvMatrixFusedPyramid;
    YuvImage YuvImageLaplasPyramid;
    YuvImage YuvImageFusedPyramid;
    Matrix GaussPyramid;

    MINT32 gain;
    bool y_only;
    bool copy_only;     // 0: CopyYuv; 1:AddEqualsYuv
}HDR_PROC_YUV_THREAD_PARA, *P_HDR_PROC_YUV_THREAD_PARA;

typedef struct HDR_RECONSTRUCT_THREAD_PARA
{
    pYuvImage pFusedPyramid;
    pMatrix pBufferMatrix;
    pYuvImage pResultImage;
    int level_no;
    int level_end;
    int plane_idx;    // 0:Y, 1:U, 2:V
}HDR_RECONSTRUCT_THREAD_PARA, *P_HDR_RECONSTRUCT_THREAD_PARA;

#define ELM(A,i,j) (A).data[ (i)*(A).cols + (j) ]

/* debug functions */
void print_matrix_info(Matrix *in, char *string);
void print_yuvimage_info(YuvImage *in, char *string);

/* multicore functions */
void* MulticoreUpsample(void* thread_para);
void* MulticoreDownsample(void* thread_para);
void* MulticoreSubstract(void *thread_para);
void* MulticoreWeightYuvImage(void *thread_para);
void* MulticoreProcYuvImage(void *thread_para);
void UpsampleNeon(const Matrix* I, const int odd_rows, int odd_cols, Matrix* upsampled);

/* original functions */
Matrix* AddEqualsMatrix(Matrix* A, const Matrix* B, int residue);
void Substract(const Matrix* A, const Matrix* B, int residue, Matrix* C);
void AddMatrix(const Matrix* A, const Matrix* B, int residue, Matrix* C);
void CopyMatrix(const Matrix* A, Matrix* C);
void AddMatrixFlare(const Matrix* A, const Matrix* B, int residue, int TopFlare, int BottomFlare, Matrix* C);
void WeightYuvImage(const YuvImage* yuv_image, const Matrix* weights, bool Yonly, const int gain);
void CopyYuvImage(const YuvImage* image,bool Yonly, YuvImage* copy);
void AddEqualsYuvImage(YuvImage* A, const YuvImage* B, bool Yonly);
void Upsample(const Matrix* I, const int odd_rows, int odd_cols, Matrix* upsampled);
void Downsample(const Matrix* I, Matrix* downsampled, Matrix*WorkingMartrix);
void QuickSort(MFLOAT arr[], MINT32 beg, MINT32 end);
void Weight2(const YuvImage* I, CropInfo crop_info, Matrix* J, MUINT32 TargetTone);
void Convolve(const Matrix* A, const Matrix* kernel, Matrix* C);



#endif
