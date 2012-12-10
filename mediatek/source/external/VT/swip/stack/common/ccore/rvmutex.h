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

/* rvmutex.h - rvmutex header file */
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
/**********************************************************************
 *
 *	DESCRIPTION:	
 *		This module provides recursive locking functions to use specifically
 *		for locking code sections.
 *
 ***********************************************************************/

#ifndef RV_MUTEX_H
#define RV_MUTEX_H

#include "rvccore.h"
#include "rvadmutex.h"
#include "rvlog.h"

#if !defined(RV_MUTEX_TYPE) || \
    ((RV_MUTEX_TYPE != RV_MUTEX_SOLARIS)     && (RV_MUTEX_TYPE != RV_MUTEX_POSIX)          && \
     (RV_MUTEX_TYPE != RV_MUTEX_VXWORKS)     && (RV_MUTEX_TYPE != RV_MUTEX_PSOS)           && \
     (RV_MUTEX_TYPE != RV_MUTEX_WIN32_MUTEX) && (RV_MUTEX_TYPE != RV_MUTEX_WIN32_CRITICAL) && \
     (RV_MUTEX_TYPE != RV_MUTEX_MOPI)        && (RV_MUTEX_TYPE != RV_MUTEX_MANUAL)         && \
(RV_MUTEX_TYPE != RV_MUTEX_NONE))
#error RV_MUTEX_TYPE not set properly
#endif

#if !defined(RV_MUTEX_ATTRIBUTE_DEFAULT)
#error RV_MUTEX_ATTRIBUTE_DEFAULT not set properly
#endif


#if defined(__cplusplus)
extern "C" {
#endif 

/* Prototypes: See documentation blocks below for details. */
/********************************************************************************************
 * RvMutexInit - Initializes the Mutex module.
 *
 * Must be called once (and only once) before any other functions in the module are called.
 *
 * INPUT   : none
 * OUTPUT  : none
 * RETURN  : Always RV_OK
 */
RvStatus RvMutexInit(void);

/********************************************************************************************
 * RvMutexEnd - Shuts down the Mutex module. 
 *
 * Must be called once (and only once) when no further calls to this module will be made.
 *
 * INPUT   : none
 * OUTPUT  : none
 * RETURN  : Always RV_OK
 */
RvStatus RvMutexEnd(void);
/********************************************************************************************
 * RvLockSourceConstruct - Constructs lock module log source.
 *
 * Constructs log source to be used by common core when printing log from the 
 * lock module. This function is applied per instance of log manager.
 * 
 * INPUT   : logMgr - log manager instance
 * OUTPUT  : none
 * RETURN  : RV_OK if successful otherwise an error code. 
 */
RvStatus RvMutexSourceConstruct(
	IN RvLogMgr	*logMgr);

#if (RV_MUTEX_TYPE != RV_MUTEX_NONE)
/********************************************************************************************
 * RvMutexConstruct - Creates a recursive mutex object. 
 *
 * INPUT   : logMgr	- log manager instance
 * OUTPUT  : mu		- Pointer to mutex object to be constructed.
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RVCOREAPI
RvStatus RVCALLCONV RvMutexConstruct(
	IN  RvLogMgr*  logMgr,
	OUT RvMutex*   mu);

/********************************************************************************************
 * RvMutexDestruct - Destroys a recursive mutex object.
 *
 * note: Never destroy a mutex object which has a thread suspended on it.
 *
 * INPUT   : mu		- Pointer to recursive mutex object to be destructed.
 *			 logMgr	- log manager instance
 * OUTPUT  : 
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RVCOREAPI
RvStatus RVCALLCONV RvMutexDestruct(
	IN  RvMutex*   mu,
	IN  RvLogMgr*  logMgr);

/********************************************************************************************
 * RvMutexLock - Aquires a recursive mutex.
 *
 * Will suspend the calling task until the mutex is available.
 *
 * INPUT   : mu		- Pointer to mutex object to be aquired.
 *			 logMgr	- log manager instance
 * OUTPUT  : 
 * RETURN  : RV_OK if successful otherwise an error code.
 */

//#define RvMutexLock(_mu, _logMgr) RV_OK

#if defined(RV_MUTEX_DEBUG)
RVCOREAPI
RvStatus RVCALLCONV RvMutexLockDbg(
	IN  RvMutex*        mu,
	IN  RvLogMgr*       logMgr,
    IN  const RvChar*   filename,
    IN  RvInt           lineno);
#else
RVCOREAPI
RvStatus RVCALLCONV RvMutexLock(
    IN  RvMutex*   mu,
    IN  RvLogMgr*  logMgr);
#endif

/********************************************************************************************
 * RvMutexUnlock - Unlocks a recursive mutex.
 *
 * INPUT   : mu     - Pointer to mutex object to be unlocked.
 *           logMgr - log manager instance
 * OUTPUT  :
 * RETURN  : RV_OK if successful otherwise an error code.
 */

#if defined(RV_MUTEX_DEBUG)
RVCOREAPI
RvStatus RVCALLCONV RvMutexUnlockDbg(
	IN  RvMutex*        mu,
	IN  RvLogMgr*       logMgr,
    IN  const RvChar*   filename,
    IN  RvInt           lineno);
#else
RVCOREAPI
RvStatus RVCALLCONV RvMutexUnlock(
    IN  RvMutex*   mu,
    IN  RvLogMgr*  logMgr);
#endif
//#define RvMutexUnlock(_mu, _logMgr) RV_OK


/********************************************************************************************
 * RvMutexSetAttr
 *
 * Sets the options and attributes to be used when creating and using mutex objects.
 * note: Non-reentrant function. Do not call when other threads may be calling rvmutex functions.
 * note: These attributes are global and will effect all mutex functions called thereafter.
 * note: The default values for these attributes are set in rvccoreconfig.h.
 *
 * INPUT   : mu		- Pointer to mutex object to be unlocked.
 *			 logMgr	- log manager instance
 * OUTPUT  : 
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RVCOREAPI
RvStatus RvMutexSetAttr(
	IN RvMutexAttr *attr,
	IN RvLogMgr    *logMgr);

/********************************************************************************************
 * RvMutexGetLockCounter - Returns the number of times the mutex has been locked by
 *                         this thread.
 *
 * INPUT   : mu     - Pointer to mutex object
 *           logMgr - log manager instance
 * OUTPUT  : lockCnt - lock counter to be returned
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RVCOREAPI
RvStatus RvMutexGetLockCounter(
	IN  RvMutex		*mu,
	IN  RvLogMgr	*logMgr,
	OUT RvInt32		*lockCnt);

/********************************************************************************************
 * RvMutexRelease - Unlocks a mutex recursively until the mutex is released completely.
 *
 * INPUT   : mu     - Pointer to mutex object
 *           logMgr - log manager instance
 * OUTPUT  : lockCnt - lock counter to be returned
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RVCOREAPI
RvStatus RvMutexRelease(
    IN  RvMutex     *mu,
    IN  RvLogMgr    *logMgr,
    OUT RvInt32     *lockCnt);

/********************************************************************************************
 * RvMutexRestore - Locks a mutex recursively lockCnt times (restores a mutex to its
 *                  previously saved state).
 *
 * INPUT   : mu     - Pointer to mutex object
 *           logMgr - log manager instance
 * OUTPUT  : lockCnt - the original lock counter
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RVCOREAPI
RvStatus RvMutexRestore(
    IN  RvMutex     *mu,
    IN  RvLogMgr    *logMgr,
    IN  RvInt32     lockCnt);

#else

/* If none is set then none of these functions do anything */
#define RvMutexConstruct(_lg,_m) (*(_m) = RV_OK)
#define RvMutexDestruct(_m,_lg) (*(_m) = RV_OK)
#define RvMutexLock(_m,_lg) (*(_m) = RV_OK)
#define RvMutexUnlock(_m,_lg) (*(_m) = RV_OK)
#define RvMutexSetAttr(_m,_lg) (*(_m) = RV_OK)
#define RvMutexGetLockCounter(_m,_lg,_c) (*(_m) = RV_OK)
#define RvMutexRelease(_m,_lg,_c) (*(_m) = RV_OK)
#define RvMutexRestore(_m,_lg,_c) (*(_m) = RV_OK)
#endif

#if defined(__cplusplus)
}
#endif 

#endif
