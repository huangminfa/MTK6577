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

#ifndef __MEDIATEK_ENHANCEMENT_H__
#define __MEDIATEK_ENHANCEMENT_H__

#define _USE_X86_DEBUG_ 0
#if _USE_X86_DEBUG_
typedef long long	off64_t;
#define lseek64 lseek

//Set Parameter
#define NUMARG 5
#define PARAM1 "-p"
#define PARAM2 "-f"
#define PARAM3 "-d"
//#define PARAM4 "-s 1048576"
#define PARAM4 "/proj/mtk04301/Perforce/fsck_msdos/img/fat8g.img"

#endif

// ------------------------------------------
// Time Info
// ------------------------------------------
#include <sys/time.h>
extern struct timeval fsck_total_time, fsck_p1_time, fsck_p2_time, fsck_p3_time, fsck_p4_time ;
unsigned long print_time(struct timeval *time) ;
void start_count(struct timeval *) ;
void end_count(const char *, struct timeval *) ;

// ------------------------------------------
// Memory Info
// ------------------------------------------
extern unsigned long memory_usage ;
extern unsigned long max_memory_usage ;
void fsck_alloc(const char *funcname, int linenum, unsigned long size) ;
void fsck_free(const char *funcname, int linenum, unsigned long size) ;

// ------------------------------------------
// xlog Info
// ------------------------------------------
#include <cutils/xlog.h>
#define FSCK_XLOG_TAG "FSCK_MTK"

// ---------------------------------------
// Enhancement 
//  - Common Part
//  - Next Part
//  - Flags Part
//  - Length Part

// ---------------------------------------
//  1. Common Part
// ---------------------------------------
extern int active_fat_num ;

// ---------------------------------------
//  2. "next" Part
// ---------------------------------------
void set_storage_dosfs(int) ;
void set_nextbuf_size(unsigned long size) ;
cl_t get_nextvalue(struct bootblock *boot, struct fatEntry *fat, cl_t cluster, int fat_no) ;
void set_nextvalue(struct bootblock * boot, struct fatEntry * fat, cl_t cluster, int value, int fat_no) ;

u_char *allocate_nextvalue_buffer(void) ;
u_char *allocate_nextvalue_buffer_tmp(void) ;
void free_nextvalue_buffer(void) ;
void free_nextvalue_buffer_tmp(void) ;
void store_nextvalue_buffer(struct bootblock *boot, struct fatEntry *fat, int fat_no) ;

// ---------------------------------------
//  3. "flags" Part
// ---------------------------------------
void flags_bitmap_setval(struct fatEntry *fat, struct bootblock* boot, cl_t fatentry_index, int value) ;
int flags_bitmap_getval(struct fatEntry *fat, cl_t fatentry_index) ;

// ---------------------------------------
//  4. "length" Part
// ---------------------------------------
int get_chain_length(struct bootblock *boot, struct fatEntry *fat, cl_t head_cluster, int fat_no) ;
void assign_chain_length(struct fatEntry *fat, cl_t head_cluster, int value) ;

#endif

