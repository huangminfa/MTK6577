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

/************************************************************************
 File Name     : rvansi.h
 Description   :
************************************************************************
        Copyright (c) 2001 RADVISION Inc. and RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Inc. and RADVISION Ltd.. No part of this document may be
reproduced in any form whatsoever without written prior approval by
RADVISION Inc. or RADVISION Ltd..

RADVISION Inc. and RADVISION Ltd. reserve the right to revise this
publication and make changes without obligation to notify any person of
such revisions or changes.
***********************************************************************/

#ifndef RV_SQRT_H
#define RV_SQRT_H

#ifdef __cplusplus
extern "C" {
#endif

#include "rvtypes.h"

#if RV_SQRT_TYPE != RV_SQRT_NONE

/* Define RvSqrt and export appropriate library function macro depending on chosen algorithm */

/* RV_SQRT_TYPE is a bit mask of  RV_SQRT_STANDARD, RV_SQRT_MBORG, RV_SQRT_ALGR, etc */
#if RV_SQRT_TYPE & RV_SQRT_STANDARD 
/* standard sqrt is available, use it */	

#include <math.h>

#ifndef RvSqrt
#define RvSqrt(n) (RvUint32)sqrt((double)(n))
#endif

#endif /*  RV_SQRT_TYPE & RV_SQRT_STANDARD */

#if RV_SQRT_TYPE & RV_SQRT_MBORG  

#ifndef RvSqrt
#define RvSqrt RvSqrtMborg   /* rename RvSqrt to be RvSqrtMborg */
#endif

RVCOREAPI
RvUint32 RVCALLCONV RvSqrtMborg(RvUint32 n);

#endif /* RV_SQRT_TYPE & RV_SQRT_MBORG */


#if RV_SQRT_TYPE & RV_SQRT_FRED

#ifndef RvSqrt
#define RvSqrt RvSqrtFred   /* rename RvSqrt to be RvSqrtFred */
#endif

RVCOREAPI
RvUint32 RVCALLCONV RvSqrtFred(RvUint32 n);

#endif  /* RV_SQRT_TYPE & RV_SQRT_FRED */

#if RV_SQRT_TYPE & RV_SQRT_ALGR

#ifndef RvSqrt
#define RvSqrt RvSqrtAlgr  /* rename RvSqrt to be RvSqrtAlgr */
#endif

RVCOREAPI
RvUint32 RVCALLCONV RvSqrtAlgr(RvUint32 n);

#endif /* RV_SQRT_TYPE & RV_SQRT_ALGR */

#if RV_SQRT_TYPE & RV_SQRT_FAST

#ifndef RvSqrt
#define RvSqrt RvSqrtFast
#endif

RVCOREAPI
RvUint32 RVCALLCONV RvSqrtFast(RvUint32 n);

#endif

#endif  /* RV_SQRT_TYPE != RV_SQRT_NONE */

#ifdef __cplusplus
}
#endif

#endif
