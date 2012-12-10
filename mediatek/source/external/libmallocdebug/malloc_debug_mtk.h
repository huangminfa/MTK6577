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
 * Contains declarations of types and constants used by malloc leak
 * detection code in both, libc and libc_malloc_debug libraries.
 */
#ifndef MALLOC_DEBUG_MTK_H
#define MALLOC_DEBUG_MTK_H

#ifdef __cplusplus
extern "C" {
#endif

#define BT_TABLE_SIZE      1543

#define CHUNK_HASH_BITS 15
#define CHUNK_TABLE_SIZE (1 << CHUNK_HASH_BITS)

#define HISTORICAL_ALLOC_SIZE (2046)

/* flag definitions, currently sharing storage with "size" */
#define SIZE_FLAG_ZYGOTE_CHILD  (1<<31)
#define SIZE_FLAG_MASK          (SIZE_FLAG_ZYGOTE_CHILD)

#define MAX_SIZE_T           (~(size_t)0)

#define MALLOC_ALIGNMENT    8
#define GUARD               0x48151642

// =================================================================
// Macro
// =================================================================
#define DEBUG_MSPACE_SIZE (32*1024*1024)
#define BACKTRACE_SIZE 5

// =================================================================
// Alignment
// =================================================================
#define SYSTEM_PAGE_SIZE        4096
#define ALIGN_UP_TO_PAGE_SIZE(p) \
    (((size_t)(p) + (SYSTEM_PAGE_SIZE - 1)) & ~(SYSTEM_PAGE_SIZE - 1))
    
// for BT
typedef struct BtEntry{
    size_t slot;
    struct BtEntry* prev;
    struct BtEntry* next;
    size_t numEntries;
    // fields above "size" are NOT sent to the host
    size_t size;
    size_t allocations;
    intptr_t backtrace[0];
}BtEntry, *PBtEntry;

typedef struct {
    size_t count;
    BtEntry* slots[BT_TABLE_SIZE];
}BtTable;

typedef struct BT{
    size_t numEntries;
    intptr_t backtrace[0];
}BT, *PBT;


// for chunk
typedef struct ChunkHashEntry{
    void* chunk_start;
    size_t bytes;

    struct ChunkHashEntry *prev;
    struct ChunkHashEntry *next;

    PBtEntry bt_entry;
	PBT free_bt;

    unsigned int flag; // 0: free, 1: used
}ChunkHashEntry, *PChunkHashEntry;


typedef struct {
    size_t count;
    PChunkHashEntry *chunk_hash_table;
} ChunkHashTable;

// for historical allocations
typedef struct {
    int head;
    PChunkHashEntry *historical_alloc_table;
} HistoricalAllocTable;

#ifdef __cplusplus
};  /* end of extern "C" */
#endif

#endif  // MALLOC_DEBUG_MTK_H
