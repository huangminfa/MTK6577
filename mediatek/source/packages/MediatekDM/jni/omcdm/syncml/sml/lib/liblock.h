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

/*[
 *      Project:    	    OMC
 *
 *      Name:				liblock.h
 *
 *      Derived From:		Original
 *
 *      Created On:			May 2004
 *
 *      Version:			$Id: //depot/main/base/syncml/sml/lib/liblock.h#3 $
 *
 *      Coding Standards:	3.0
 *
 *      Purpose:            SyncML core code
 *
 *      (c) Copyright Insignia Solutions plc, 2004
 *
]*/

/**
 * @file
 * Library for Thread Locking Functions
 *
 * @target_system   ALL
 * @target_os       ALL
 * @description thread-locking library, RTK addition by luz@synthesis.ch
 */

#ifndef LIBLOCK_H
#define LIBLOCK_H

#include <syncml/define.h>

#ifdef __DEBUG_LOCKS
// needs debug global and DebugPrintf()
extern void ThreadDebugPrintf(const char *text,...);
#define DEBUGPRINTF(m) { ThreadDebugPrintf m; }
#endif

#if defined(__MAKE_THREADSAFE) && !defined(NOWSM)
  // thread safety measures are required only when working with WSM
  #ifdef _WIN32
    #ifdef __DEBUG_LOCKS
      // we need TryEnterCriticalSection for debug
      #define _WIN32_WINNT 0x0400
    #endif
    #define WIN32_LEAN_AND_MEAN
    #include <windows.h>
    #undef WIN32_LEAN_AND_MEAN
    // remove problematic windows defines (OPAQUE in wingdi.h, OPTIONAL in ????)
    #undef OPAQUE
    #undef OPTIONAL

    #include <stdio.h>

    #ifdef __DEBUG_LOCKS
      /* - functions that also document lock usage */
      void _ToolKitLockInit(const char *msg);
      void _ToolKitLockFree(const char *msg);
      void _LockToolKit(const char *msg);
      void _ReleaseToolKit(const char *msg);
      #define TOOLKITLOCK_INIT(m) _ToolKitLockInit(m);
      #define TOOLKITLOCK_FREE(m) _ToolKitLockFree(m);
      #define LOCKTOOLKIT(m) _LockToolKit(m);
      #define RELEASETOOLKIT(m) _ReleaseToolKit(m);
    #else
      /* - simple macros to use the lock */
      #define TOOLKITLOCK_INIT(m) InitializeCriticalSection(&gSmlLock);
      #define TOOLKITLOCK_FREE(m) DeleteCriticalSection(&gSmlLock);
      #define LOCKTOOLKIT(m) EnterCriticalSection(&gSmlLock);
      #define RELEASETOOLKIT(m) LeaveCriticalSection(&gSmlLock);
    #endif
  #else
    #error "<syncml/sml/lib/liblock.h>: unsupported platform"
  #endif
#else
  /* just NOP */
  #define TOOLKITLOCK_INIT(m)
  #define TOOLKITLOCK_FREE(m)
  #define LOCKTOOLKIT(m)
  #define RELEASETOOLKIT(m)
#endif

#endif // LIBLOCK_H

// globals declarations may not be omitted on second include!
#ifdef __MAKE_THREADSAFE
  #ifdef _WIN32
    /* define the required things */
    #ifndef _IMPLEMENTS_LOCK_GLOBALS
    /* - the lock itself, as global variable */
    extern CRITICAL_SECTION gSmlLock;
    #else
    CRITICAL_SECTION gSmlLock;
    #endif
  #endif
#endif


/* eof */
