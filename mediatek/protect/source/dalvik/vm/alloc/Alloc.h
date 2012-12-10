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
 * Garbage-collecting allocator.
 */
#ifndef DALVIK_ALLOC_ALLOC_H_
#define DALVIK_ALLOC_ALLOC_H_

#include <stddef.h>

/* flags for dvmMalloc */
enum {
    ALLOC_DEFAULT = 0x00,
    ALLOC_DONT_TRACK = 0x01,  /* don't add to internal tracking list */
    ALLOC_NON_MOVING = 0x02,
};

/*
 * Initialization.
 */
bool dvmGcStartup(void);
bool dvmCreateStockExceptions(void);
bool dvmGcStartupAfterZygote(void);
void dvmGcShutdown(void);
void dvmGcThreadShutdown(void);
bool dvmGcStartupClasses(void);

/*
 * Do any last-minute preparation before we call fork() for the first time.
 */
bool dvmGcPreZygoteFork(void);

/*
 * Basic allocation function.
 *
 * The new object will be added to the "tracked alloc" table unless
 * flags is ALLOC_DONT_TRACK.
 *
 * Returns NULL and throws an exception on failure.
 */
void* dvmMalloc(size_t size, int flags);

/*
 * Allocate a new object.
 *
 * The new object will be added to the "tracked alloc" table unless
 * flags is ALLOC_DONT_TRACK.
 *
 * Returns NULL and throws an exception on failure.
 */
extern "C" Object* dvmAllocObject(ClassObject* clazz, int flags);

/*
 * Track an object reference that is currently only visible internally.
 * This is called automatically by dvmMalloc() unless ALLOC_DONT_TRACK
 * is set.
 *
 * The "self" argument is allowed as an optimization; it may be NULL.
 */
extern "C" void dvmAddTrackedAlloc(Object* obj, Thread* self);

/*
 * Remove an object from the internal tracking list.
 *
 * Does nothing if "obj" is NULL.
 *
 * The "self" argument is allowed as an optimization; it may be NULL.
 */
extern "C" void dvmReleaseTrackedAlloc(Object* obj, Thread* self);

/*
 * Returns true iff <obj> points to a zygote allocated object.
 */
bool dvmIsZygoteObject(const Object* obj);

/*
 * Create a copy of an object.
 *
 * Returns NULL and throws an exception on failure.
 */
Object* dvmCloneObject(Object* obj, int flags);

/*
 * Make the object finalizable.
 */
extern "C" void dvmSetFinalizable(Object* obj);

/*
 * Determine the exact number of GC heap bytes used by an object.  (Internal
 * to heap code except for debugging.)
 */
size_t dvmObjectSizeInHeap(const Object* obj);

/*
 * Gets the current ideal heap utilization, represented as a number
 * between zero and one.
 */
float dvmGetTargetHeapUtilization(void);

/*
 * Sets the new ideal heap utilization, represented as a number
 * between zero and one.
 */
void dvmSetTargetHeapUtilization(float newTarget);

/*
 * Initiate garbage collection.
 *
 * This usually happens automatically, but can also be caused by
 * Runtime.gc().
 */
void dvmCollectGarbage(void);

/*
 * Returns a count of the direct instances of a class.
 */
size_t dvmCountInstancesOfClass(const ClassObject *clazz);

/*
 * Returns a count of the instances of a class and its subclasses.
 */
size_t dvmCountAssignableInstancesOfClass(const ClassObject *clazz);

/*
 * Removes any growth limits from the heap.
 */
void dvmClearGrowthLimit(void);

/*
 * Returns true if the address is aligned appropriately for a heap object.
 * Does not require the caller to hold the heap lock, and does not take the
 * heap lock internally.
 */
bool dvmIsHeapAddress(void *address);

bool dvmIsNonMovingObject(const Object* object);

#endif  // DALVIK_ALLOC_ALLOC_H_
