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

#ifndef DEBUG_H
#define DEBUG_H

#include <stdlib.h>
#include <stdio.h>
#include <common.h>

#ifdef DEBUG

    #define FAILIF(cond, msg...) do {                        \
	if (unlikely(cond)) {                                \
        fprintf(stderr, "%s(%d): ", __FILE__, __LINE__); \
		fprintf(stderr, ##msg);                          \
		exit(1);                                         \
	}                                                    \
} while(0)

/* Debug enabled */
    #define ASSERT(x) do {                                \
	if (unlikely(!(x))) {                             \
		fprintf(stderr,                               \
				"ASSERTION FAILURE %s:%d: [%s]\n",    \
				__FILE__, __LINE__, #x);              \
		exit(1);                                      \
	}                                                 \
} while(0)

#else

    #define FAILIF(cond, msg...) do { \
	if (unlikely(cond)) {         \
		fprintf(stderr, ##msg);   \
		exit(1);                  \
	}                             \
} while(0)

/* No debug */
    #define ASSERT(x)   do { } while(0)

#endif/* DEBUG */

#define FAILIF_LIBELF(cond, function) \
    FAILIF(cond, "%s(): %s\n", #function, elf_errmsg(elf_errno()));

static inline void FREE(void *ptr) {
    free(ptr);
}

static inline void FREEIF(void *ptr) {
    if (ptr) FREE(ptr);
}

#define PRINT(x...)  do {                             \
    extern int quiet_flag;                            \
    if(likely(!quiet_flag))                           \
        fprintf(stdout, ##x);                         \
} while(0)

#define ERROR(x...) fprintf(stderr, ##x)

#define INFO(x...)  do {                              \
    extern int verbose_flag;                          \
    if(unlikely(verbose_flag))                        \
        fprintf(stdout, ##x);                         \
} while(0)

/* Prints a hex and ASCII dump of the selected buffer to the selected stream. */
int dump_hex_buffer(FILE *s, void *b, size_t l, size_t elsize);

#endif/*DEBUG_H*/
