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

/***********************************************************************
Filename   : rvrandomgenerator.h
Description: Random number generator
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
#ifndef RV_RANDOMGENERATOR_H
#define RV_RANDOMGENERATOR_H

#include "rvtypes.h"
#include "rverror.h"

typedef RvUint32 RvRandom;

#define RV_RAND_MAX ((RvRandom)~0)


typedef struct
{
	RvRandom state; /* State of the random generator's seed value */
} RvRandomGenerator;


#if defined(__cplusplus)
extern "C" {
#endif 

/********************************************************************************************
 * RvRandomGeneratorConstruct
 * construct a random generator object.
 * INPUT   : r - a random generator object.
 *           seed - random generator seed.
 * OUTPUT  : None
 * RETURN  : RV_OK.
 */
RVCOREAPI RvStatus RVCALLCONV
RvRandomGeneratorConstruct(IN RvRandomGenerator* r, IN RvRandom seed);

#define RvRandomGeneratorDestruct(_r) ((_r)->state = RV_OK)

#define RvRandomGeneratorGetMax(_r) (RV_RAND_MAX)

/********************************************************************************************
 * RvRandomGeneratorGetValue
 * returns a random value.
 * INPUT   : r - a random generator object.
 * OUTPUT  : value - the random value.
 * RETURN  : RV_OK.
 */
RVCOREAPI RvStatus RVCALLCONV
RvRandomGeneratorGetValue(IN RvRandomGenerator* r, OUT RvRandom* value);

/********************************************************************************************
 * RvRandomGeneratorGetInRange
 * returns a random value.
 * INPUT   : r - a random generator object.
 * INPUT   : n - maximal random value.
 * OUTPUT  : value - the random value in range [0;n-1].
 * RETURN  : RV_OK/RV_ERROR_BADPARAM - if value==NULL
 */
RVCOREAPI RvStatus RVCALLCONV
RvRandomGeneratorGetInRange(RvRandomGenerator* r, RvRandom n, OUT RvRandom* value);

#if defined(__cplusplus)
}
#endif 

#endif /* RV_RANDOMGENERATOR_H */

