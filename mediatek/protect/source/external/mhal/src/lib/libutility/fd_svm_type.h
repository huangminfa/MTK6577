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

#ifndef _FD_SVM_TYPE_H_
#define _FD_SVM_TYPE_H_

#include "kal_release.h"
#include "fd_svm_parameter.h"

/*
 * structure for a 2D point
 */
typedef struct FD_SPt2D_Int_s {
    kal_int32 x, y;                                                                         // (x, y) coordinate of a 2D point
} FD_SPt2D;

/*
 * structure for image size
 */
typedef struct FD_SImgSize_s {
    kal_uint32 w, h;                                                                        // width & height of an image
} FD_SImgSize;

/*
 * structure for gray image
 */
typedef struct FD_SGrayImg_s {
    //kal_uint16 *yy;                                                                         // intensity image
    kal_uint8 *yy;                                                                         // intensity image
    FD_SImgSize size;                                                                          // image size
} FD_SGrayImg;

/*
 * structure for gradient image
 */
typedef struct FD_SGradImg_s {
    kal_int16 *grad_x;                                                                         // intensity image
    kal_int16 *grad_y;                                                                         // intensity image
    FD_SImgSize size;                                                                          // image size
} FD_SGradImg;

/*
 * structure of HOG parameters
 */
typedef struct fd_hog_parameter_s {
    FD_SPt2D fp[FD_HOG_POINT_NUM];                                                                // each retina sampling points' position
    kal_uint8 fp_num;                                                                       // number of fp
    kal_uint8 block_num;                                                                    // block number
    kal_uint8 block_size;                                                                   // block size
    kal_uint8 hist_bin_num;                                                                 // bin number of the histogram
} fd_hog_parameter;

typedef enum {FD_OFF, FD_L2NORM, FD_L2HYS} FD_SNormalType;

/*
 * window rotation type
 */
typedef enum {FD_NO_FLIP, FD_H_FLIP, FD_V_FLIP} FD_FlipType;

//
//	SVM model structure
// 
struct fd_svm_model
{
	float	gamma		;	//	Gamma
	float	rho			;	//	Constants in decision functions
	float	*sv			;	//	Array of support vector
	float	*coef		;	//	Array of coefficients for SVs in decision functions
	float	kconst		;	//	Kernel constant
};
struct fd_svm_model_int
{
	kal_int32	gamma	;	//	Gamma
	kal_int32	rho		;	//	Constants in decision functions
	kal_int32	*sv		;	//	Array of support vector
	kal_int32	*coef	;	//	Array of coefficients for SVs in decision functions
	kal_int32	kconst	;	//	Kernel constant
};

//
// Ensemble SVM model structure
//
struct fd_ensemble_svm_model
{
	float	*beta;
	float	*omega;
	FD_SPt2D	*fp;
	int		*parities;
	float	*ths;
	float	*alphas;
	int		n;
	int		d;
	float	pTh;
	float	mTh;
};

struct fd_ensemble_svm_model_int{
	kal_int32 *beta;
	kal_int32 *omega;
	kal_uint8 *fpx;
	kal_uint8 *fpy;
	kal_int8 *parities;
	kal_int32 *ths;
	kal_uint32 *alphas;
	kal_uint8 n;
	kal_uint16 d;
	kal_uint8 hog_cell_size;
};

#endif
