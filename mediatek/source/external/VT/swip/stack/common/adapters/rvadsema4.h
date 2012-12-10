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

/* rvadsema4.h - rvadsema4 header file */
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

#ifndef RV_ADSEMA4_H
#define RV_ADSEMA4_H


#include "rvccore.h"


#if (RV_SEMAPHORE_TYPE != RV_SEMAPHORE_NONE)

#include "rvadsema4_t.h"
#include "rvlog.h"


#ifdef __cplusplus
extern "C" {
#endif

/********************************************************************************************
 * RvAdSema4Construct
 *
 * Called by RvSemaphoreConstruct.
 * Create and initialize a semaphore.
 *
 * INPUT   : None.
 * OUTPUT  : sema - address of the semaphore object to be constructed
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus RvAdSema4Construct(
    OUT RvSemaphore*    sema,
    IN  RvUint32        startcount,
    IN  RvLogMgr*       logMgr);


/********************************************************************************************
 * RvAdSema4Destruct
 *
 * Called by RvSemaphoreDestruct.
 * Destruct a semaphore.
 *
 * INPUT   : sema - address of the semaphore object to be destructed
 * OUTPUT  : None.
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus RvAdSema4Destruct(
    IN  RvSemaphore*    sema,
    IN  RvLogMgr*       logMgr);


/********************************************************************************************
 * RvAdSema4Post
 *
 * Called by RvSemaphorePost.
 * Release a semaphore token.
 *
 * INPUT   : sema - address of the semaphore object to be released
 * OUTPUT  : None.
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus RvAdSema4Post(
    IN  RvSemaphore*    sema,
    IN  RvLogMgr*       logMgr);


/********************************************************************************************
 * RvAdSema4Wait
 *
 * Called by RvSemaphoreWait.
 * Acquire a semaphore token.
 *
 * INPUT   : sema - address of the semaphore object to be acquired
 * OUTPUT  : None.
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus RvAdSema4Wait(
    IN  RvSemaphore*    sema,
    IN  RvLogMgr*       logMgr);


/********************************************************************************************
 * RvAdSema4TryWait
 *
 * Called by RvSemaphoreTryWait.
 * Try to acquire a semaphore token.
 * If the semaphore is not available - returns immediately with RV_ERROR_TRY_AGAIN status.
 *
 * INPUT   : sema - address of the semaphore object to be acquired
 * OUTPUT  : None.
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus RvAdSema4TryWait(
    IN  RvSemaphore*    sema,
    IN  RvLogMgr*       logMgr);


/********************************************************************************************
 * RvAdSema4SetAttr
 *
 * Called by RvSemaphoreSetAttr.
 * Set default options to be used from now on by RvAdSema4Construct.
 *
 * INPUT   : attr - set of options to be saved for future semaphores construction
 * OUTPUT  : None.
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus RvAdSema4SetAttr(
    IN  RvSemaphoreAttr*attr,
    IN  RvLogMgr*       logMgr);

#ifdef __cplusplus
}
#endif


#else

#endif


#endif
