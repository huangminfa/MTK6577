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

/* rvadthread_t.h - rvadthread header file */
/************************************************************************
      Copyright (c) 2001,2002 RADVISION Inc. and RADVISION Ltd.
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

#ifndef RV_ADTHREAD_T_H
#define RV_ADTHREAD_T_H


#include "rvtypes.h"
//#include <nucleus.h>

#ifndef __VT_SWIP__
#if defined(RV_DEBUG)
typedef kal_task_type RvThreadBlock;
#else
//#include "tc_defs.h"
typedef kal_task_type RvThreadBlock;
#endif

#else

typedef RvInt32*   RvThreadBlock;
#endif 

/********************************************************************************************
 * RvThreadId
 * An OS specific thread ID. Used to identify threads regardless
 * of whether or not a thread handle has been constructed for it.
 ********************************************************************************************/

/* Patch: 2008/10/22 
Note that RvThreadsHashFind() requires RvThreadId to be an integer.
However, this value will be a pointer returned by kal_get_current_thread_ID().
Since address can be 0xF~, so we shall cast it to a unsigned value*/
typedef kal_uint32 RvThreadId;


/********************************************************************************************
 * RvThreadAttr
 * OS specific attributes and options used for threads. See definitions in rvthread.h
 * along with the default values in rvccoreconfig.h for more information.
 ********************************************************************************************/
typedef struct { /* parameters for NU_Create_task */
    kal_uint32 time_slice;
    unsigned char preempt;
} RvThreadAttr;

#define RV_THREAD_PRIORITY_MAX 4
#define RV_THREAD_PRIORITY_MIN 255
#define RV_THREAD_PRIORITY_INCREMENT (-1)
#define RV_THREAD_WRAPPER_STACK 48

/* OS specific options */
#define RV_THREAD_AUTO_DELETE       RV_NO   /* whether thread auto-delete is supported or not */
#define RV_THREAD_SETUP_STACK       RV_YES  /* whether to allocate stack if stackaddr == NULL */
#define RV_THREAD_STACK_ADDR_USER   RV_YES  /* whether user allocated stack is supported or not */
#define RV_THREAD_STACK_SIZE_OS     RV_NO   /* whether OS can determine stack size or not */


#endif
