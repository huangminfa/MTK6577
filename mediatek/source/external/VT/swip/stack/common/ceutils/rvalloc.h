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
Filename:
Description: C/Macro-based allocator
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

#ifndef RV_ALLOC_H
#define RV_ALLOC_H
#include "rvtypes.h"
#include "rverror.h"


/*$
{type:
	{name: RvAlloc}
	{superpackage: Util}	
	{include: rvalloc.h}
	{description:	
		{p: Describes a generic interface to a dynamic memory allocator.}
		{p: Allocators are used to parameterize memory management function
			for a given algorithm.  By parameterizing the allocator the 
			algorithm is no longer bound to a single memory management 
			methodology.
		}
	}
	{methods:
		{method: RvAlloc* rvAllocConstruct(RvAlloc* a, void* pool, RvSize_t maxSize, void* (*alloc)(void*, RvSize_t), void (*dealloc)(void*, RvSize_t, void*));}
		{method: void rvAllocDestruct(RvAlloc* a);}
		{method: void* rvAllocAllocate(RvAlloc* a, RvSize_t s);}
		{method: void rvAllocDeallocate(RvAlloc* a, RvSize_t s, void* ptr);}
		{method: void* rvAllocGetPool(RvAlloc* a);}
		{method: RvSize_t rvAllocGetMaxSize(RvAlloc* a);}
	}
}
$*/
#if defined(__cplusplus)
extern "C" {
#endif

typedef struct RvAlloc_ {
    void* pool;
    RvSize_t maxSize;
    void* (*alloc)(void* pool, RvSize_t s);
    void (*dealloc)(void* pool, RvSize_t s, void* x);
} RvAlloc;


RVCOREAPI 
RvAlloc* rvAllocConstruct(RvAlloc* a, void* pool, RvSize_t maxSize,
  void* (*alloc)(void*, RvSize_t), void (*dealloc)(void*, RvSize_t, void*));
/*$
{function:
    {name: rvAllocDestruct}
    {class: RvAlloc}
    {include: rvalloc.h}
    {description:
        {p: Destruct an allocator object.}
    }
    {proto: void rvAllocDestruct(RvAlloc* a);}
    {params:
        {param: {n: a} {d: A pointer to the allocator.}}
    }
}
$*/
#define rvAllocDestruct(a)

#define rvAllocConstructCopy rvDefaultConstructCopy

RVCOREAPI
void* rvAllocAllocate(RvAlloc* a, RvSize_t s);

RVCOREAPI
void rvAllocDeallocate(RvAlloc* a, RvSize_t s, void* ptr);

RVCOREAPI
RvAlloc *rvAllocGetDefaultAllocator(void);

#define rvAllocAllocateObject(a,t)    (t *)rvAllocAllocate(a, sizeof(t))
#define rvAllocDeallocateObject(a,t,o)  if((o) != NULL){ rvAllocDeallocate(a, sizeof(t), (o));(o) = NULL; }

/*$
{function:
    {name: rvAllocGetPool}
    {class: RvAlloc}
    {include: rvalloc.h}
    {description:
        {p: Get the allocator's pool.}
    }
    {proto: void* rvAllocGetPool(RvAlloc* a);}
    {params:
        {param: {n: a} {d: A pointer to the allocator.}}
    }
    {returns: A pointer to the pool.}
}
$*/
#define rvAllocGetPool(a)                       ((a)->pool)
/*$
{function:
    {name: rvAllocGetMaxSize}
    {class: RvAlloc}
    {include: rvalloc.h}
    {description:
        {p: Get the maximum size (in bytes) the allocator can allocate.}
    }
    {proto: RvSize_t rvAllocGetMaxSize(RvAlloc* a);}
    {params:
        {param: {n: a} {d: A pointer to the allocator.}}
    }
    {returns: The maximum allocation size.}
}
$*/
#define rvAllocGetMaxSize(a)                    ((a)->maxSize)

void rvAssert(int expression);

/* Some general definitions 
 * They has nothing to do with allocations actually,
 *  but there is no better place for them
 */

#define rvDefaultConstructCopy(d, s, a) ((void)(a), *(d) = *(s), (d))
#define rvDefaultDestruct(x)			
#define rvDefaultEqual(a, b)			(*(a) == *(b))

typedef void* RvVoidPtr;

#define RvVoidPtrConstructCopy(d, s, a) rvDefaultConstructCopy(d, s, a)
#define RvVoidPtrDestruct(x)			rvDefaultDestruct(x)
#define RvVoidPtrEqual(a, b)			rvDefaultEqual(a, b)

extern const RvAlloc rvConstDefaultAlloc;

#if defined(__cplusplus)
}
#endif

#endif

