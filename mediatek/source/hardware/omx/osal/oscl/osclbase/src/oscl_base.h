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

/* ------------------------------------------------------------------
 * Copyright (C) 1998-2009 PacketVideo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 * -------------------------------------------------------------------
 */
// -*- c++ -*-
// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

//                           O S C L _ B A S E

// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

/*! \addtogroup osclbase OSCL Base
 *
 * @{
 */


/*! \file oscl_base.h
    \brief The file oscl_base.h is the public header that should be included to pick up the platform configuration, basic type definitions, and common macros.*/


#ifndef OSCL_BASE_H_INCLUDED
#define OSCL_BASE_H_INCLUDED

#include "osclconfig.h"
#include "oscl_base_macros.h"
#include "oscl_types.h"
#include "osclconfig_check.h"

#ifdef USE_CML2_CONFIG
#include "pv_config.h"
#endif

//singleton support derives from global var support.
#define OSCL_HAS_SINGLETON_SUPPORT 1

#ifdef __cplusplus

class OsclLockBase;

class OsclBase
{
    public:
        /**
         * Initializes OsclBase functionality.
         * OsclBase must be initialized before any OsclBase
         * functionality can be used.
         *
         * Note: The first call to OsclBase::Init will initialize
         *  the thread lock that is used to avoid thread contention
         *  for process scope singleton access.  The last call to
         *  OsclBase::Cleanup will cleanup the thread lock.
         *  Case should be taken to avoid possible thread contention
         *  on the first Init and the last Cleanup call.
         *
         * @return 0 on success
         */
        OSCL_IMPORT_REF static int32 Init();

        /**
         * Cleanup OsclBase functionality
         * OsclBase should be cleaned once OsclBase
         * functions are no longer needed
         * @return 0 on success
         */
        OSCL_IMPORT_REF static int32 Cleanup();
};

/**
//OsclBase error codes.  These values are used as return codes for OsclBase, OsclTLSRegistry,
//and OsclSingletonRegistry.
//Maintenance note: _OsclBaseToErrorMap in oscl_error must match this list
*/
enum TPVBaseErrorEnum
{
    EPVErrorBaseNotInstalled = 1
    , EPVErrorBaseAlreadyInstalled = 2
    , EPVErrorBaseOutOfMemory = 3
    , EPVErrorBaseSystemCallFailed = 4
    , EPVErrorBaseTooManyThreads = 5
};

#include "oscl_lock_base.h"

/**
 * _OsclBasicLock is a simple thread lock class for internal use by
 * OsclTLSRegistry and OsclSingleton.
 * Higher-level code should use OsclMutex instead.
 */
#if (OSCL_HAS_BASIC_LOCK)
class _OsclBasicLock : public OsclLockBase
{
    public:

        /**
         * Class constructor.
         */
        OSCL_IMPORT_REF _OsclBasicLock();

        /**
         * Class destructor
         */
        OSCL_IMPORT_REF ~_OsclBasicLock();

        /**
         * Takes the lock
         *
         */
        OSCL_IMPORT_REF void Lock();

        /**
         * Releases the lock
         *
         */
        OSCL_IMPORT_REF void Unlock();


        /**
        * Set to non-zero on error
        */
        int32 iError;

    private:
        TOsclBasicLockObject    ObjLock;

};
#else
typedef OsclNullLock _OsclBasicLock;
#endif

#else

/**
 * Initializes OsclBase functionality.
 * OsclBase must be initialized before any OsclBase
 * functionality can be used.
 *
 * @exception leaves if out-of-memory
 */
void PVOsclBase_Init();

/**
 * Cleanup OsclBase functionality
 * OsclBase should be cleaned once OsclBase
 * functions are no longer needed
 */
void PVOsclBase_Cleanup();

#endif

/*! @} */

#endif  // OSCL_BASE_H_INCLUDED
