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

#ifndef _RECTIFYIMAGE_H_
#define _RECTIFYIMAGE_H_

#include "MTKMavCommon.h"
#include "MTKMavType.h"
#include <float.h>
#include <math.h>

/* common define macro */
#ifdef SIM_MAIN
#define inline __inline // MSVC
#ifdef __ARMCC_VERSION
#define LM_FINITE isfinite // RVDS
#else
#define LM_FINITE _finite // MSVC
#endif
#else
#define inline __inline // RVDS
#define LM_FINITE isfinite // RVDS
#endif

#define _POW_ LM_CNST(2.1)
#define ALPHA LM_CNST(1e-4)
#define BETA LM_CNST(0.9)
#define GAMMA_SQ LM_CNST(0.99995)*LM_CNST(0.99995)  //gamma=LM_CNST(0.99995),
#define THO LM_CNST(1e-8)
#define TMING LM_CNST(1e-18) /* minimum step length for LS and PG steps */

#define __LSITMAX   150 // max #iterations for line search
#define LM_OPTS_SZ    	 5 /* max(4, 5) */
#define LM_ERROR         -1
#define LM_INIT_MU    	 1E-03
#define LM_STOP_THRESH	 1E-17
#define LM_DIFF_DELTA    1E-06
#define LM_REAL_MAX 3.402823466e+38F 
#define LM_REAL_MIN 1.175494351e-38F 
#define LM_REAL_EPSILON 1.192092896e-07F 
#define EPSILON       1E-12
#define ONE_THIRD     0.3333333334 /* 1.0/3.0 */
#define M_PI_R 0.017453

#define FABS(x) (((x)>=0.0)? (x) : -(x))
//#define LM_BC_DER_WORKSZ(npar, nmeas) (2*(nmeas) + 4*(npar) + (nmeas)*(npar) + (npar)*(npar))
//#define LM_BC_DIF_WORKSZ(npar, nmeas) LM_BC_DER_WORKSZ((npar), (nmeas)) /* LEVMAR_BC_DIF currently implemented using LEVMAR_BC_DER()! */

#define __SUBCNST(x) x##F
#define LM_CNST(x) __SUBCNST(x) // force substitution
/* find the median of 3 numbers */
#define __MEDIAN3(a, b, c) ( ((a) >= (b))?\
        ( ((c) >= (a))? (a) : ( ((c) <= (b))? (b) : (c) ) ) : \
        ( ((c) >= (b))? (b) : ( ((c) <= (a))? (a) : (c) ) ) )

/* structures */
typedef struct
{
    MINT32 n, *nfev;
    MFLOAT *hx, *x;
  void *adata;
}FUNC_STATE_STRUCT;

typedef struct 
{
    MINT32 ffdif; // nonzero if forward differencing is used
    MFLOAT *hx, *hxx;
	void *adata;
    MFLOAT delta;
}LMBC_DIF_DATA_STRUCT;

typedef struct 
{
    MFLOAT *x;	 // old iterate:	x[k-1]
    MFLOAT f;   	//function value at old iterate, f(x)
    MFLOAT *g;	//gradient at old iterate, g(x), or approximate
    MFLOAT *p;	//non-zero newton step
    MFLOAT alpha;//fixed constant < 0.5 for line search (see above)
    MFLOAT stepmx; //maximum allowable step size
}LNSRCH_INPUT_STRUCT;

typedef struct 
{
    MFLOAT* xpls;	 //new iterate x[k]
    MFLOAT* ffpls;	//function value at new iterate, f(xpls)
    MINT32 iretcd;
}LNSRCH_OUTPUT_STRUCT;

MINT32 RectifyImage(MINT32 m, MavInitInfo *InitInfo, mav_rec_par_struct *pRecData, MavResultInfo *pMavResultInfo);
MINT32 AlignImage(MINT32 m, MavInitInfo *InitInfo, mav_rec_par_struct *pRecData, MavResultInfo *pMavResultInfo);

#endif /* _RECTIFYIMAGE_H_ */
