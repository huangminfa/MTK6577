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
 * System utilities.
 */
#ifndef LIBDEX_SYSUTIL_H_
#define LIBDEX_SYSUTIL_H_

#include <sys/types.h>

/*
 * System page size.  Normally you're expected to get this from
 * sysconf(_SC_PAGESIZE) or some system-specific define (usually PAGESIZE
 * or PAGE_SIZE).  If we use a simple #define the compiler can generate
 * appropriate masks directly, so we define it here and verify it as the
 * VM is starting up.
 *
 * Must be a power of 2.
 */
#define SYSTEM_PAGE_SIZE        4096

/*
 * Use this to keep track of mapped segments.
 */
struct MemMapping {
    void*   addr;           /* start of data */
    size_t  length;         /* length of data */

    void*   baseAddr;       /* page-aligned base address */
    size_t  baseLength;     /* length of mapping */
};

/*
 * Copy a map.
 */
void sysCopyMap(MemMapping* dst, const MemMapping* src);

/*
 * Load a file into a new shared memory segment.  All data from the current
 * offset to the end of the file is pulled in.
 *
 * The segment is read-write, allowing VM fixups.  (It should be modified
 * to support .gz/.zip compressed data.)
 *
 * On success, "pMap" is filled in, and zero is returned.
 */
int sysLoadFileInShmem(int fd, MemMapping* pMap);

/*
 * Map a file (from fd's current offset) into a shared,
 * read-only memory segment.
 *
 * On success, "pMap" is filled in, and zero is returned.
 */
int sysMapFileInShmemReadOnly(int fd, MemMapping* pMap);

/*
 * Map a file (from fd's current offset) into a shared, read-only memory
 * segment that can be made writable.  (In some cases, such as when
 * mapping a file on a FAT filesystem, the result may be fully writable.)
 *
 * On success, "pMap" is filled in, and zero is returned.
 */
int sysMapFileInShmemWritableReadOnly(int fd, MemMapping* pMap);

/*
 * Like sysMapFileInShmemReadOnly, but on only part of a file.
 */
int sysMapFileSegmentInShmem(int fd, off_t start, size_t length,
    MemMapping* pMap);

/*
 * Create a private anonymous mapping, useful for large allocations.
 *
 * On success, "pMap" is filled in, and zero is returned.
 */
int sysCreatePrivateMap(size_t length, MemMapping* pMap);

/*
 * Change the access rights on one or more pages.  If "wantReadWrite" is
 * zero, the pages will be made read-only; otherwise they will be read-write.
 *
 * Returns 0 on success.
 */
int sysChangeMapAccess(void* addr, size_t length, int wantReadWrite,
    MemMapping* pmap);

/*
 * Release the pages associated with a shared memory segment.
 *
 * This does not free "pMap"; it just releases the memory.
 */
void sysReleaseShmem(MemMapping* pMap);

/*
 * Write until all bytes have been written.
 *
 * Returns 0 on success, or an errno value on failure.
 */
int sysWriteFully(int fd, const void* buf, size_t count, const char* logMsg);

/*
 * Copy the given number of bytes from one fd to another. Returns
 * 0 on success, -1 on failure.
 */
int sysCopyFileToFile(int outFd, int inFd, size_t count);

#endif  // LIBDEX_SYSUTIL_H_
