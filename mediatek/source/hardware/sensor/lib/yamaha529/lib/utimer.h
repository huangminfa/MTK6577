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
 * CONFIDENTIAL
 * Copyright (C) 2010 Yamaha Corporation
 */

#ifndef __UTIMER_H__
#define __UTIMER_H__

#ifndef STATIC
#define STATIC 
#endif
#ifndef NULL
#define NULL ((void*)0)
#endif

#ifdef __LINUX_KERNEL_DRIVER__
#include <linux/types.h>
#else
#include <stdint.h>
#endif

struct utimeval {
    int32_t tv_sec;
    int32_t tv_msec;
};

struct utimer {
    struct utimeval prev_time;
    struct utimeval total_time;
    struct utimeval delay_ms;
};

STATIC int utimeval_init(struct utimeval *val);
STATIC int utimeval_is_initial(struct utimeval *val);
STATIC int utimeval_is_overflow(struct utimeval *val);
STATIC struct utimeval utimeval_plus(struct utimeval *first, struct utimeval *second);
STATIC struct utimeval utimeval_minus(struct utimeval *first, struct utimeval *second);
STATIC int utimeval_greater_than(struct utimeval *first, struct utimeval *second);
STATIC int utimeval_greater_or_equal(struct utimeval *first, struct utimeval *second);
STATIC int utimeval_greater_than_zero(struct utimeval *val);
STATIC int utimeval_less_than_zero(struct utimeval *val);
STATIC struct utimeval *msec_to_utimeval(struct utimeval *result, uint32_t msec);
STATIC uint32_t utimeval_to_msec(struct utimeval *val);

STATIC struct utimeval utimer_calc_next_time(struct utimer *ut,
                                             struct utimeval *cur);
STATIC struct utimeval utimer_current_time(void);
STATIC int utimer_is_timeout(struct utimer *ut);
STATIC int utimer_clear_timeout(struct utimer *ut);
STATIC uint32_t utimer_get_total_time(struct utimer *ut);
STATIC uint32_t utimer_get_delay(struct utimer *ut);
STATIC int utimer_set_delay(struct utimer *ut, uint32_t delay_ms);
STATIC int utimer_update(struct utimer *ut);
STATIC int utimer_update_with_curtime(struct utimer *ut, struct utimeval *cur);
STATIC uint32_t utimer_sleep_time(struct utimer *ut);
STATIC uint32_t utimer_sleep_time_with_curtime(struct utimer *ut,
                                               struct utimeval *cur);
STATIC int utimer_init(struct utimer *ut, uint32_t delay_ms);
STATIC int utimer_clear(struct utimer *ut);
STATIC void utimer_lib_init(void (*func)(int *sec, int *msec));

#endif
