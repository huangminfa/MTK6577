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

//               O S C L _ L O C K _ B A S E

// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

/*! \addtogroup osclbase OSCL Base
 *
 * @{
 */


/*! \file oscl_lock_base.h
 * \brief This file defines an abstract lock class, OsclLockBase,
 * that is used for APIs potentially requiring multi-thread safety.
 * A null-lock implementation, OsclNullLock, is also provided for
 * single-thread configurations (basically a noop for lock/unlock).
 * Also provides the OsclScopedLock class which is template class
 * takes care of freeing the lock when the class goes out of scope.
*/

#ifndef OSCL_LOCK_BASE_H_INCLUDED
#define OSCL_LOCK_BASE_H_INCLUDED


class OsclLockBase
{

    public:
        virtual void Lock() = 0;
        virtual void Unlock() = 0;
        virtual ~OsclLockBase() {}

};

class OsclNullLock: public OsclLockBase
{

    public:
        virtual void Lock() {};
        virtual void Unlock() {};
        virtual ~OsclNullLock() {}

};



/**
 * @brief The OsclScopedLock class is a template class that handles unlocking
 *   an abstract class on destruction.  This is very useful for ensuring that
 *   the lock is released when the OsclScopedLock goes out of scope.
 *
 * The purpose of this class is to provide a way to prevent accidental resource
 * leaks in a class or a method, due to "not remembering to unlock" variables
 * which might lead to deadlock conditions.
 *
 */

template<class LockClass> class OsclScopedLock
{
    private:
        LockClass* _Ptr;

        // make copy constructor private so no default is created.
        /**
        * @brief Copy constructor
        *
        * Initializes the pointer and takes ownership from another oscl_auto_ptr.
        * Note that the other class does NOT own the pointer any longer, and
        * hence it is NOT its responsibility to free it.
        */
        OsclScopedLock(const OsclScopedLock<LockClass>&) {}


        /**
        * @brief release() method releases ownership of the pointer, currently owned
        * by the class. It returns the pointer as well.
        *
        */
        void release()
        {
            if (_Ptr)
            {
                _Ptr->Unlock();
                _Ptr = NULL;
            }
        }


        /**
        * @brief acquire() method acquires ownership of the lock.
        *
        */
        void acquire()
        {
            if (_Ptr)
            {
                _Ptr->Lock();
            }
        }


    public:

        /**
        * @brief Default constructor
        * Initializes the pointer and takes ownership.
        */
        explicit OsclScopedLock(LockClass& inLock) : _Ptr(&inLock)
        {
            acquire();
        };

        /**
        * @brief Destructor
        *
        * The pointer is deleted in case this class still has ownership
        */
        ~OsclScopedLock()
        {
            release();
        }



};


/*! @} */


#endif
