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

/* rvadlock.h - rvadlock header file */
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

#ifndef RV_ADLOCK_H
#define RV_ADLOCK_H


#include "rvccore.h"


#if (RV_LOCK_TYPE != RV_LOCK_NONE)

#include "rvadlock_t.h"
#include "rvlog.h"


#ifdef RV_LOCK_INCLUDE_ADLOCK

#ifdef __cplusplus
extern "C" {
#endif

/********************************************************************************************
 * RvAdLockConstruct
 *
 * Called by RvLockConstruct.
 * Create and initialize a non-recursive mutex (lock).
 *
 * INPUT   : None.
 * OUTPUT  : lock - address of the lock object to be constructed
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus RvAdLockConstruct(
    OUT RvLock*         lock,
    IN  RvLogMgr*       logMgr);


/********************************************************************************************
 * RvAdLockDestruct
 *
 * Called by RvLockDestruct.
 * Destruct a lock.
 *
 * INPUT   : lock - address of the lock object to be destructed
 * OUTPUT  : None.
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus RvAdLockDestruct(
    IN  RvLock*         lock,
    IN  RvLogMgr*       logMgr);


/********************************************************************************************
 * RvAdLockGet
 *
 * Called by RvLockGet.
 * Lock a lock.
 *
 * INPUT   : lock - address of the lock object to be locked
 * OUTPUT  : None.
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus RvAdLockGet(
    IN  RvLock*         lock,
    IN  RvLogMgr*       logMgr);


/********************************************************************************************
 * RvAdLockRelease
 *
 * Called by RvLockRelease.
 * Release a lock.
 *
 * INPUT   : lock - address of the lock object to be released
 * OUTPUT  : None.
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus RvAdLockRelease(
    IN  RvLock*         lock,
    IN  RvLogMgr*       logMgr);


/********************************************************************************************
 * RvAdLockSetAttr
 *
 * Called by RvLockSetAttr.
 * Set default options to be used from now on by RvAdLockConstruct.
 *
 * INPUT   : attr - set of options to be saved for future locks construction
 * OUTPUT  : None.
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus RvAdLockSetAttr(
    IN  RvLockAttr*     attr,
    IN  RvLogMgr*       logMgr);

/********************************************************************************************
 * vt_RvLckPoolInit
 *
 * Called by vt_init.
 * add by mtk01567
 * because our KAL do not support delete lock, so we allocate a lock pool. If we want to allocate
 * or delete lock, we get/release from/to the pool
 *
 * INPUT   : None
 * OUTPUT  : None.
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus vt_RvLckPoolInit(void);

#ifdef __cplusplus
}
#endif

#endif  /* RV_LOCK_INCLUDE_ADLOCK */


#else

typedef RvInt RvLock;     /* Dummy types, used to prevent warnings. */
typedef RvInt RvLockAttr; /* not used */

#endif


#endif
