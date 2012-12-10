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
 
#ifndef _GIA_CORE_H_
#define _GIA_CORE_H_
#include "MTKGia.h"
#include "MTKType.h"

typedef struct
{
	MINT32*  projY ;					// 1 x mem_len_y
	MINT32** projX ;					// n_block_row x mem_len_x
}struct_n3d_data;


typedef struct
{
    MUINT32 descriptor_type_x;			// 0: Gradient Response 1 Gradient Vector (for Directions)
    MUINT32 data_projection_nd_x;		// from 1~4 // if DESCRIPTOR_TYPE_X = 1, set 2 here, otherwise 4 for image
    MUINT32 match_sample_type_x;		// 0: Regular,	1: Content-aware
    MUINT32 match_sample_rate_x;		// Sample rate, no larger than 8, [1-8]

    // Could be changed by tester
    MUINT32 match_search_type_y;		// 0: Sequential, 1: Jumpping
    MUINT32 match_search_step_y;		// from 1~4, no larger than 4 
    MFLOAT match_search_range_y;		// percentage, will multiplied by image height, 0.01-0.05

    MUINT32 match_search_type_x;		// 0: Sequential, 1: Jumpping
    MUINT32 match_search_step_x;		// from 1~4, no larger than 4 , [1-4]
    MFLOAT match_search_range_x_l;		// percentage, will multiplied by image width, 0.01-0.3

	// 2012-0518 --- 
	MFLOAT match_search_range_x_r_vdo;		// percentage, will multiplied by image width, 0.01-0.1
	MFLOAT match_search_range_x_r_img;		// percentage, will multiplied by image width, 0.01-0.3
	//
    MFLOAT thr_smoothness_grad;
    MFLOAT block_size_percentage_x;// percentage, will multiplied by image height,bound [0.1~0.5]

	// 2012-0518 --- 
	MFLOAT block_size_percentage_y_vdo;		// percentage, will multiplied by image height,bound [0.1~0.5]
	MFLOAT block_size_percentage_y_img;		// percentage, will multiplied by image height,bound [0.1~0.5]
	//

    MUINT32 n_sample_rows;			// number of sample rows, bound 1~4
    MUINT32 n_sample_cols;			// number of sample cols, bound 1~6

    // threshold hold is set for width = 1280
    MUINT32 thr_defined_width;	// threshold is defined for width = 1280
    MUINT32 thr_matched_range;			// hot-zone: define matched region for cross-verification
    MUINT32 thr_rescuing_range;		// rescuring-zone: test +/- 1 pixel
    MUINT32 thr_support_range_bg;		// used for outlier estimation, bound >=0
    MUINT32 thr_support_range_fg;		// used for outlier estimation, bound >=0
    MINT32 convergence_min;	// minimal convergency for foreground
    MINT32 convergence_max;		// maximal convergency for background

	//------------------- 2012-0510 -------------------------------------------
    MBOOL enable_x_shift;
    MBOOL enable_y_shift;
	MUINT32 thr_defined_height ;		// matching parameters are defined for 720p
    MUINT32 the_reference_height ;		// refence height for y-shift store in NVRAM
	MUINT32 total_calculation_bound ;	// defined for 720p (a 1280x720 side-by-side image), each match compare 72 elements

	//------------------- 2012-0402 -------------------------------------------
	MINT32 convergence_def;
	MINT32 moving_ratio;
	// 2012-0605
	MUINT32 convergence_speed ;
	MUINT32 convergence_sensing ;
	MUINT32 convergence_effect ;
	MINT32 convergence_min_inc ;
	MINT32 convergence_max_inc ;
	MINT32 convergence_def_inc ;
	MUINT32 convergence_min_deg ;
	MUINT32 convergence_max_deg ;
	MUINT32 convergence_def_deg ;
	//

	MUINT32 working_buf_size ; //2012-0514

}GIA_PARAMETER_STRUCT;
//MRESULT GiaCoreSetTuningPara2(GIA_SET_ENV_INFO_STRUCT *pInitInfo , GIA_TUNING_PARA2_STRUCT*);
void GiaCoreInit(GIA_SET_ENV_INFO_STRUCT *pInitInfo ,  GIA_PARAMETER_STRUCT *pPara);
int GiaCoreMain();
void GiaCoreSetWorkingBufInfo(P_GIA_SET_WORK_BUF_INFO_STRUCT pWorkBufInfo);
void GiaCoreSetProcInfo(P_GIA_SET_PROC_INFO_STRUCT pProcInfo);
void GiaCoreGetResultInfo(P_GIA_RESULT_STRUCT pGiaResultInfo);
void SaveGiaProcLog();
void GiaCoreSetLogBuffer(MUINT32 LogBufAddr);
#endif
