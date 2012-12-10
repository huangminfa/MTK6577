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

#if (0)
******************************************************************************
Filename    :
Description :
******************************************************************************
                Copyright (c) 1999 RADVision Inc.
************************************************************************
NOTICE:
This document contains information that is proprietary to RADVision LTD.
No part of this publication may be reproduced in any form whatsoever 
without written prior approval by RADVision LTD..

RADVision LTD. reserves the right to revise this publication and make 
changes without obligation to notify any person of such revisions or 
changes.
******************************************************************************
$Revision:$
$Date:$
$Author: S. Cipolli$
******************************************************************************
#endif
#ifndef RV_PTRVECTOR_H
#define RV_PTRVECTOR_H

#include "rvccore.h"
#include "rvvector.h"

#if defined(__cplusplus)
extern "C" {
#endif

rvDeclareVector(RvVoidPtr)

/* PtrVector iterators */
#define RvPtrVectorIter				RvVectorIter(RvVoidPtr)
#define rvPtrVectorIterData(i)		(i)
#define rvPtrVectorIterPrev(i)		((i) - 1)
#define rvPtrVectorIterNext(i)		((i) + 1)

#define RvPtrVectorIterData(i)		(i)
#define RvPtrVectorIterPrev(i)		((i) - 1)
#define RvPtrVectorIterNext(i)		((i) + 1)

#define RvPtrVectorRevIter			RvVectorRevIter(RvVoidPtr)
#define rvPtrVectorRevIterData(i)	(i)
#define rvPtrVectorRevIterPrev(i)	((i) + 1)
#define rvPtrVectorRevIterNext(i)	((i) - 1)

#define RvPtrVectorRevIterData(i)	(i)
#define RvPtrVectorRevIterPrev(i)	((i) + 1)
#define RvPtrVectorRevIterNext(i)	((i) - 1)

/* Public PtrVector interface */
typedef RvVector(RvVoidPtr) RvPtrVector;
#define rvPtrVectorConstruct		rvVectorConstruct(RvVoidPtr)
#define rvPtrVectorConstructN		rvVectorConstructN(RvVoidPtr)
#define rvPtrVectorConstructCopy	rvVectorConstructCopy(RvVoidPtr)
#define rvPtrVectorCopy				rvVectorCopy(RvVoidPtr)
#define rvPtrVectorDestruct			rvVectorDestruct(RvVoidPtr)
#define rvPtrVectorGetAllocator		rvVectorGetAllocator
#define rvPtrVectorSize				rvVectorSize
#define rvPtrVectorBegin			rvVectorBegin
#define rvPtrVectorEnd				rvVectorEnd
#define rvPtrVectorRevBegin			rvVectorRevBegin
#define rvPtrVectorRevEnd			rvVectorRevEnd
#define rvPtrVectorAt(v, i)			(rvVectorAt(v, i))
#define rvPtrVectorPopFront			rvVectorPopFront(RvVoidPtr)
#define rvPtrVectorPopBack			rvVectorPopBack(RvVoidPtr)
#define rvPtrVectorErase			rvVectorErase(RvVoidPtr)
#define rvPtrVectorClear			rvVectorClear(RvVoidPtr)
#define rvPtrVectorFill				rvVectorFill(RvVoidPtr)
#define rvPtrVectorSwap				rvVectorSwap(RvVoidPtr)
#define rvPtrVectorEqual			rvVectorEqual(RvVoidPtr)
#define rvPtrVectorRemoveIf			rvVectorRemoveIf(RvVoidPtr)

RVCOREAPI
RvVoidPtr rvPtrVectorBack(RvPtrVector* l);

RVCOREAPI
RvPtrVectorIter rvPtrVectorPushBack(RvPtrVector* l, RvVoidPtr x);

RVCOREAPI
RvPtrVectorIter rvPtrVectorInsert(RvPtrVector* l, RvPtrVectorIter i, RvVoidPtr x);

#if defined(__cplusplus)
}
#endif

#endif
