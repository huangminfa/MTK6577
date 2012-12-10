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

/***********************************************************************
Filename   : rvlinux.h
Description: config file for Linux
************************************************************************
        Copyright (c) 2001 RADVISION Inc. and RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Inc. and RADVISION Ltd.. No part of this document may be
reproduced in any form whatsoever without written prior approval by
RADVISION Inc. or RADVISION Ltd..

RADVISION Inc. and RADVISION Ltd. reserve the right to revise this
publication and make changes without obligation to notify any person of
such revisions or changes.
***********************************************************************/
#ifndef RV_LINUX_H
#define RV_LINUX_H

#define RVAPI
#define RVINTAPI
#define RVCOREAPI
#define RVCALLCONV
//heshun
#define IN

/* rvtime: Select timestamp interface to use */
#if ((RV_OS_VERSION & RV_OS_LINUX_REDHAT) != 0) || ((RV_OS_VERSION & RV_OS_LINUX_SUSE) != 0)
#define RV_TIMESTAMP_TYPE RV_TIMESTAMP_LINUX
#else
#define RV_TIMESTAMP_TYPE RV_TIMESTAMP_POSIX
#endif

/* rvtime: Select clock interface to use */
#define RV_CLOCK_TYPE RV_CLOCK_LINUX

/* rvtm: Select tm (calendar time) interface to use */
#define RV_TM_TYPE RV_TM_POSIX

/* rv64ascii: Select 64 bit conversions to use */
#define RV_64TOASCII_TYPE RV_64TOASCII_STANDARD

/* rvsemaphore: Select semaphore interface to use */
#define RV_SEMAPHORE_TYPE RV_SEMAPHORE_POSIX
#define RV_SEMAPHORE_ATTRIBUTE_DEFAULT 0 /* 0 = not shared, otherwise shared */

/* rvmutex: Select mutex interface to use */
#define RV_MUTEX_TYPE RV_MUTEX_POSIX
#define RV_MUTEX_ATTRIBUTE_DEFAULT  0 /* not used */
//#define RV_MUTEX_ATTRIBUTE_DEFAULT { 0, 0 } /* not used */

/* rvthread: Select thread interface to use and set parameters */
#define RV_THREAD_TYPE RV_THREAD_POSIX

/* If scheduling policy is set to SCHED_OTHER (as it happens in the RV_THREAD_ATTRIBUTE_DEFAULT),
 *  thread priority SHOULD be 0 (priority is irrelevant for this policy). Some kernels will
 *  be upset by other values.
 */
#define RV_THREAD_PRIORITY_DEFAULT 0

#if ((RV_OS_VERSION & RV_OS_LINUX_UCLINUX) == 0)
#define RV_THREAD_STACKSIZE_DEFAULT 0 /* Allow OS to allocate */
#else
#define RV_THREAD_STACKSIZE_DEFAULT 20000
#endif
#define RV_THREAD_STACKSIZE_USEDEFAULT 0x100000 /* Under this stack size use default stack size */

#include "rv_customize.h"
/* PTHREAD_EXPLICIT_SCHED causes pthread_create to return error under Linux kernel 2.6 */
//#define RV_THREAD_ATTRIBUTE_DEFAULT { PTHREAD_SCOPE_SYSTEM, SCHED_OTHER, 0 } /* scope, schedpolicy, inheritsched */
#define RV_THREAD_ATTRIBUTE_DEFAULT { 0, 0 }

/* rvlock: Select lock interface to use */
#define RV_LOCK_TYPE RV_LOCK_POSIX

#define RV_LOCK_ATTRIBUTE_DEFAULT { PTHREAD_MUTEX_FAST_NP } /* set to FAST or ERRORCHECK only */


/* rvmemory: Select memory interface to use */
#define RV_MEMORY_TYPE RV_MEMORY_STANDARD

/* rvosmem: Select OS dynamic memory driver to use */
#define RV_OSMEM_TYPE RV_OSMEM_MALLOC

/* rvhost: Select network host interface to use */
#define RV_HOST_TYPE RV_HOST_POSIX

/* rvfdevent: File-descriptor events interface to use */
#define RV_SELECT_TYPE RV_SELECT_SELECT

/* rvsockets: Type of Sockets used in the system */
#define RV_SOCKET_TYPE RV_SOCKET_BSD

/* rvportrange: Type of Port-range used in the system */
#define RV_PORTRANGE_TYPE RV_PORTRANGE_FAST

/* rvloglistener: Type of log listeners used in the system */
#define RV_LOGLISTENER_TYPE RV_LOGLISTENER_FILE_AND_TERMINAL

/* rvstdio: Select stdio interface to use */
#define RV_STDIO_TYPE RV_STDIO_ANSI

/* rvassert: Select stdio interface to use */
#define RV_ASSERT_TYPE RV_ASSERT_ANSI

#define RV_TARGET_CPU_NOT_SET       0
#define RV_TARGET_CPU_I386          1
#define RV_TARGET_CPU_X86_64        2
#define RV_TARGET_CPU_IA64          3
#define RV_TARGET_CPU_I686          4

#ifndef	RV_TARGET_CPU
#define	RV_TARGET_CPU_TYPE          RV_TARGET_CPU_NOT_SET
#endif


#endif /* RV_LINUX_H */
