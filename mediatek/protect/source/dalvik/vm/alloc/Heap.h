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

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Internal heap functions
 */
#ifndef DALVIK_ALLOC_HEAP_H_
#define DALVIK_ALLOC_HEAP_H_

struct GcSpec {
  /* If true, only the application heap is threatened. */
  bool isPartial;
  /* If true, the trace is run concurrently with the mutator. */
  bool isConcurrent;
  /* Toggles for the soft reference clearing policy. */
  bool doPreserve;
  /* A name for this garbage collection mode. */
  const char *reason;
};

/* Not enough space for an "ordinary" Object to be allocated. */
extern const GcSpec *GC_FOR_MALLOC;

/* Automatic GC triggered by exceeding a heap occupancy threshold. */
extern const GcSpec *GC_CONCURRENT;

/* Explicit GC via Runtime.gc(), VMRuntime.gc(), or SIGUSR1. */
extern const GcSpec *GC_EXPLICIT;

/* Final attempt to reclaim memory before throwing an OOM. */
extern const GcSpec *GC_BEFORE_OOM;

/*
 * Initialize the GC heap.
 *
 * Returns true if successful, false otherwise.
 */
bool dvmHeapStartup(void);

/*
 * Initialization that needs to wait until after leaving zygote mode.
 * This needs to be called before the first allocation or GC that
 * happens after forking.
 */
bool dvmHeapStartupAfterZygote(void);

/*
 * Tear down the GC heap.
 *
 * Frees all memory allocated via dvmMalloc() as
 * a side-effect.
 */
void dvmHeapShutdown(void);

/*
 * Stops any threads internal to the garbage collector.  Called before
 * the heap itself is shutdown.
 */
void dvmHeapThreadShutdown(void);

#if 0       // needs to be in Alloc.h so debug code can find it.
/*
 * Returns a number of bytes greater than or
 * equal to the size of the named object in the heap.
 *
 * Specifically, it returns the size of the heap
 * chunk which contains the object.
 */
size_t dvmObjectSizeInHeap(const Object *obj);
#endif

/*
 * Run the garbage collector without doing any locking.
 */
void dvmCollectGarbageInternal(const GcSpec *spec);

/*
 * Blocks the calling thread until the garbage collector is inactive.
 * The caller must hold the heap lock as this call releases and
 * re-acquires the heap lock.  After returning, no garbage collection
 * will be in progress and the heap lock will be held by the caller.
 */
void dvmWaitForConcurrentGcToComplete(void);

/*
 * Returns true iff <obj> points to a valid allocated object.
 */
bool dvmIsValidObject(const Object* obj);

#endif  // DALVIK_ALLOC_HEAP_H_
