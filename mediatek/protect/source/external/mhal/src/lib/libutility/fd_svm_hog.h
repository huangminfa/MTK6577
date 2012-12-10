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

#ifndef _FD_HOG_H_
#define _FD_HOG_H_

#include "kal_release.h"
#include "fd_svm_parameter.h"
#include "fd_svm_type.h"

void fd_set_hog_param(fd_hog_parameter *hog_param, kal_uint8 sample_x[], kal_uint8 sample_y[], kal_uint8 fp_num, kal_uint8 block_size, kal_uint8 block_num, kal_uint8 hist_bin_num);

//cell=6, block=2

static kal_uint32 fd_hog_kernel_int[] = {
229, 267, 302, 333, 357, 373, 378, 373, 357, 333, 302, 267, 
267, 311, 352, 388, 416, 434, 440, 434, 416, 388, 352, 311, 
302, 352, 399, 440, 472, 492, 499, 492, 472, 440, 399, 352, 
333, 388, 440, 485, 520, 542, 550, 542, 520, 485, 440, 388, 
357, 416, 472, 520, 558, 581, 589, 581, 558, 520, 472, 416, 
373, 434, 492, 542, 581, 606, 615, 606, 581, 542, 492, 434, 
378, 440, 499, 550, 589, 615, 623, 615, 589, 550, 499, 440, 
373, 434, 492, 542, 581, 606, 615, 606, 581, 542, 492, 434, 
357, 416, 472, 520, 558, 581, 589, 581, 558, 520, 472, 416, 
333, 388, 440, 485, 520, 542, 550, 542, 520, 485, 440, 388, 
302, 352, 399, 440, 472, 492, 499, 492, 472, 440, 399, 352, 
267, 311, 352, 388, 416, 434, 440, 434, 416, 388, 352, 311, 
};

 //hog kernel 10x10
/*
static kal_uint32 hog_kernel_int[] = {
331,396,456,504,535,546,535,504,456,396,
396,474,546,603,640,653,640,603,546,474,
456,546,628,694,736,751,736,694,628,546,
504,603,694,766,814,830,814,766,694,603,
535,640,736,814,864,882,864,814,736,640,
546,653,751,830,882,899,882,830,751,653,
535,640,736,814,864,882,864,814,736,640,
504,603,694,766,814,830,814,766,694,603,
456,546,628,694,736,751,736,694,628,546,
396,474,546,603,640,653,640,603,546,474 }  ;
*/
/*----------------------------------------------------------------------------*/
/*	Fixed-point implementation                                                */
/*----------------------------------------------------------------------------*/
#define FD_HOG_MAG_SFHIT	12
#define FD_HOG_MAG_PREC		16
#define FD_HOG_DESC_SHIFT	8

#define ULONG_MAX     0xffffffffUL  /* maximum unsigned long value */
#define FD_HOG_MARGIN		ULONG_MAX*0.95f
#define FD_HOG_NORM_VALUE	( ( 1 << 16 ) - 1 )
#define FD_HOG_NORM_TH		( FD_HOG_NORM_VALUE * 0.2 )

#define ABS(a)		((a < 0) ? -a : a )
#define FIXED16_PI	205887						// fixed-point 15.16 representation of pi: 3.14159265358979 * 2^16
static kal_int16 fd_hog_image_gx[ FD_MAX_IMAGE_W * FD_MAX_IMAGE_H ]  ;
static kal_int16 fd_hog_image_gy[ FD_MAX_IMAGE_W * FD_MAX_IMAGE_H ]  ;
static kal_int32 fd_hog_mag_buffer[ FD_MAX_IMAGE_W * FD_MAX_IMAGE_H ]  ;
static kal_int32 fd_hog_ori_buffer[ FD_MAX_IMAGE_W * FD_MAX_IMAGE_H ]  ;
static kal_uint32 fd_hog_feature[ FD_HOG_VECTOR_LENGTH ]  ;		// feature descriptor buffer, for classification
void fd_hog_extract(const FD_SGradImg img, const fd_hog_parameter param, kal_uint32 feature[]);
void fd_hog_calc_grad_x(const FD_SGrayImg image, kal_int16 *ig);
void fd_hog_calc_grad_y(const FD_SGrayImg image, kal_int16 *ig);
static void fd_hog_normalize(kal_uint32* v, const kal_uint32 n, const kal_uint32 THRESHOLD, const FD_SNormalType type);
static kal_uint32 fd_hog_L2_norm_shift(const kal_uint32* v, const kal_uint32 len);
static kal_uint32 fd_hog_L2_norm(const kal_uint32* v, const kal_uint32 len);
kal_int32 fd_xatan(const kal_int32 x, const kal_uint32 _FRAC_BITS);
kal_int32 fd_xatan2(const kal_int32 y, const kal_int32 x, const kal_uint32 _FRAC_BITS);
kal_int32 fd_xatan2_poly3(const kal_int32 y, const kal_int32 x);
kal_int32 fd_xmul_64(kal_int32 x, kal_int32 y, kal_uint32 BITS);
kal_int32 fd_xdiv(kal_int32 numerator, kal_int32 denominator, kal_uint32 BITS);
kal_uint32 fd_xsqrt(const kal_uint32 x, const kal_uint32 _FRAC_BITS);
/*----------------------------------------------------------------------------*/

#endif