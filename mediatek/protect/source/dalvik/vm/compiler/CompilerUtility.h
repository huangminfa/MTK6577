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
 * Copyright (C) 2009 The Android Open Source Project
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

#ifndef DALVIK_VM_COMPILER_UTILITY_H_
#define DALVIK_VM_COMPILER_UTILITY_H_

#include "Dalvik.h"

/* Each arena page has some overhead, so take a few bytes off 8k */
#define ARENA_DEFAULT_SIZE 8100

/* Allocate the initial memory block for arena-based allocation */
bool dvmCompilerHeapInit(void);

typedef struct ArenaMemBlock {
    size_t blockSize;
    size_t bytesAllocated;
    struct ArenaMemBlock *next;
    char ptr[0];
} ArenaMemBlock;

void *dvmCompilerNew(size_t size, bool zero);

void dvmCompilerArenaReset(void);

typedef struct GrowableList {
    size_t numAllocated;
    size_t numUsed;
    intptr_t *elemList;
} GrowableList;

typedef struct GrowableListIterator {
    GrowableList *list;
    size_t idx;
    size_t size;
} GrowableListIterator;

#define GET_ELEM_N(LIST, TYPE, N) (((TYPE*) LIST->elemList)[N])

#define BLOCK_NAME_LEN 80

/* Forward declarations */
struct LIR;
struct BasicBlock;

void dvmInitGrowableList(GrowableList *gList, size_t initLength);
void dvmInsertGrowableList(GrowableList *gList, intptr_t elem);
void dvmGrowableListIteratorInit(GrowableList *gList,
                                 GrowableListIterator *iterator);
intptr_t dvmGrowableListIteratorNext(GrowableListIterator *iterator);
intptr_t dvmGrowableListGetElement(const GrowableList *gList, size_t idx);

BitVector* dvmCompilerAllocBitVector(unsigned int startBits, bool expandable);
bool dvmCompilerSetBit(BitVector* pBits, unsigned int num);
bool dvmCompilerClearBit(BitVector* pBits, unsigned int num);
void dvmCompilerMarkAllBits(BitVector *pBits, bool set);
void dvmDebugBitVector(char *msg, const BitVector *bv, int length);
void dvmDumpLIRInsn(struct LIR *lir, unsigned char *baseAddr);
void dvmDumpResourceMask(struct LIR *lir, u8 mask, const char *prefix);
void dvmDumpBlockBitVector(const GrowableList *blocks, char *msg,
                           const BitVector *bv, int length);
void dvmGetBlockName(struct BasicBlock *bb, char *name);
int dvmCompilerCacheFlush(long start, long end, long flags);


#endif  // DALVIK_COMPILER_UTILITY_H_
