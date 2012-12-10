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

//                     O S C L _ S E M A P H O R E

// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

/**
 *  @file oscl_semaphore.h
 *  @brief This file provides implementation of mutex
 *
 */

// Definition file for OSCL Semaphore
#ifndef OSCL_SEMAPHORE_H_INCLUDED
#define OSCL_SEMAPHORE_H_INCLUDED

#ifndef OSCLCONFIG_PROC_H_INCLUDED
#include "osclconfig_proc.h"
#endif

#ifndef OSCL_THREAD_H_INCLUDED
#include "oscl_thread.h"
#endif


/**
 * Class Semaphore
 */
class OsclSemaphore
{
    public:

        /**
         * Class constructor
         */
        OSCL_IMPORT_REF OsclSemaphore();

        /**
         * Class destructor
         */
        OSCL_IMPORT_REF ~OsclSemaphore();

        /**
         * Creates the Semaphore
         *
         * @param Intialcount
         *
         * @return Returns the Error whether it is success or failure
         *incase of failure it will return what is the specific error
         */
        OSCL_IMPORT_REF OsclProcStatus::eOsclProcError Create(uint32 initVal = 0);

        /**
         * Creates the Semaphore
         *
         * @param Intialcount
         *
         * @return Returns the Error whether it is success or failure
         *incase of failure it will return what is the specific error
         */
        OSCL_IMPORT_REF OsclProcStatus::eOsclProcError Create(uint32 initVal ,int MaxCount);

        /**
         * Closes the Semaphore
         *
         * @param It wont take any parameters
         *
         * @return Returns the Error whether it is success or failure
         *incase of failure it will return what is the specific error
         */
        OSCL_IMPORT_REF OsclProcStatus::eOsclProcError Close();

        /**
         * Makes the thread to wait on the Semaphore
         *
         * @param It wont take any parameters
         *
         * @return Returns the Error whether it is success or failure
         *incase of failure it will return what is the specific error
         */
        OSCL_IMPORT_REF OsclProcStatus::eOsclProcError Wait();

        /**
         * Makes the thread to wait on the Semaphore, with a timeout.
         *
         * @param timeout in milliseconds.
         *
         * @return Returns SUCCESS_ERROR if the semaphore was aquired,
         * WAIT_TIMEOUT_ERROR if the timeout expired without acquiring the
         * semaphore, or an error code if the operation failed.
         * Note: this function may not be supported on all platforms, and
         * may return NOT_IMPLEMENTED.
         */
        OSCL_IMPORT_REF OsclProcStatus::eOsclProcError Wait(uint32 timeout_msec);

        /**
         * Try to acquire semaphore ,if the semaphore is already acquired by another thread,
         *  calling thread immediately returns with out blocking
         *
         * @param It wont take any parameters
         *
         * @return Returns SUCCESS_ERROR if the semaphore was acquired,
         * SEM_LOCKED_ERROR if the semaphore cannot be acquired without waiting,
         * or an error code if the operation failed.
         * Note: this function may not be supported on all platforms, and
         * may return NOT_IMPLEMENTED.
         */
        OSCL_IMPORT_REF OsclProcStatus::eOsclProcError TryWait();

        /**
         * Signals that the thread is finished with the Semaphore
         *
         * @param It wont take any parameters
         *
         * @return Returns the Error whether it is success or failure
         *incase of failure it will return what is the specific error
         */
        OSCL_IMPORT_REF OsclProcStatus::eOsclProcError Signal();

    private:

        bool bCreated;
        //for pthreads implementations without sem timedwait support.
        TOsclMutexObject ObjMutex;
        TOsclConditionObject ObjCondition;
        uint32 iCount;
        int32 MaximunCount;


};





#endif  //  END OF File

