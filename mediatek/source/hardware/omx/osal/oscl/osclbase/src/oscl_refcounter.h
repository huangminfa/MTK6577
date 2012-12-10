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

//                   O S C L _ R E F C O U N T E R

// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

/*! \addtogroup osclbase OSCL Base
 *
 * @{
 */



/**
 *  @file oscl_refcounter.h
 *  @brief A general purpose reference counter to object lifetimes.
 *
 */

#ifndef OSCL_REFCOUNTER_H_INCLUDED
#define OSCL_REFCOUNTER_H_INCLUDED

#ifndef OSCL_ASSERT_H_INCLUDED
#include "oscl_assert.h"
#endif

#ifndef OSCL_DEFALLOC_H_INCLUDED
#include "oscl_defalloc.h"
#endif

/**
 * Interface class for OsclRefCounter implementations
 */
class OsclRefCounter
{
    public:
        /**
         * Add to the reference count
         */
        virtual void addRef() = 0;

        /**
         * Delete from reference count
         */
        virtual void removeRef() = 0;

        /**
         * Gets the current number of references
         */
        virtual uint32 getCount() = 0;

        virtual ~OsclRefCounter() {}
};


/**
 * Implementation of an OsclRefCounter that uses a dynamically
 * created deallocator.
 */
class OsclRefCounterDA : public OsclRefCounter
{
    public:

        /**
         * Constructor
         * Takes a pointer to the buffer to track, and a pointer
         * to the deallocator object which will be used to delete
         * the buffer.
         *
         * When the reference count reaches zero, the buffer will
         * be deleted by the deallocator.  Also, the OsclRefCounter
         * object (this) will self-destruct when the reference count
         * is zero.  In some cases the OsclRefCounter object will
         * be part of the buffer being deleted.  For such cases,
         * the object pointer must be equal to the buffer pointer
         * given at construction.  If the object is not part of the
         * buffer being deleted, it will self-destruct with a call
         * to delete().
         *
         * @param p       pointer to the buffer to track
         * @param dealloc pointer to the deallocator to use when
         *                deleting the buffer
         */
        OsclRefCounterDA(OsclAny *p, OsclDestructDealloc *dealloc):
                ptr(p), deallocator(dealloc), refcnt(1)
        {
            OSCL_ASSERT(ptr != NULL && deallocator != NULL);
        }

        /**
         * Destructor
         * empty
         */
        virtual ~OsclRefCounterDA() {}

        /**
         * Add to the reference count
         */
        void addRef()
        {
            ++refcnt;
        }

        /**
         * Remove from the reference count
         */
        void removeRef()
        {
            if (--refcnt == 0)
            {
                if (ptr == this)
                {
                    // buffer is part of the refcounter
                    deallocator->destruct_and_dealloc(this);
                }
                else
                {
                    // delete the buffer and the refcounter object
                    deallocator->destruct_and_dealloc(ptr);
                    delete(this);
                }
            }
        }

        /**
         * Gets the current number of references
         */
        uint32 getCount()
        {
            return refcnt;
        }

    private:
        OsclRefCounterDA();
        OsclRefCounterDA(const OsclRefCounterDA& x);
        OsclRefCounterDA& operator=(const OsclRefCounterDA& x);

        OsclAny *ptr;
        OsclDestructDealloc *deallocator;
        uint32 refcnt;
};


/**
 * Implementation of an OsclRefCounter that uses a statically
 * created deallocator.
 */
template<class DeallocType>
class OsclRefCounterSA : public OsclRefCounter
{
    public:
        /**
         * Constructor
         * Takes a pointer to the buffer to track.
         *
         * When the reference count reaches zero, the buffer will
         * be deleted by the deallocator.  Also, the OsclRefCounter
         * object (this) will self-destruct when the reference count
         * is zero.  In some cases the OsclRefCounter object will
         * be part of the buffer being deleted.  For such cases,
         * the object pointer must be equal to the buffer pointer
         * given at construction.  If the object is not part of the
         * buffer being deleted, it will self-destruct with a call
         * to delete().
         *
         * @param p       pointer to the buffer to track
         */
        OsclRefCounterSA(OsclAny *p) :
                ptr(p), refcnt(1)
        {
            OSCL_ASSERT(ptr != NULL);
        }

        /**
         * Destructor
         * empty
         */
        virtual ~OsclRefCounterSA() {}

        /**
         * Add to the reference count
         */
        void addRef()
        {
            ++refcnt;
        }

        /**
         * Remove from the reference count
         */
        void removeRef()
        {
            if (--refcnt == 0)
            {
                if (ptr == this)
                {

                    // buffer is part of the refcounter
                    DeallocType deallocator;
                    deallocator.destruct_and_dealloc(this);
                }
                else
                {

                    // delete the buffer and the recounter object
                    DeallocType deallocator;
                    deallocator.destruct_and_dealloc(ptr);
                    delete(this);
                }
            }
        }

        /**
         * Gets the current number of references
         */
        uint32 getCount()
        {
            return refcnt;
        }

    private:
        OsclRefCounterSA();
        OsclRefCounterSA(const OsclRefCounterSA<DeallocType>& x);
        OsclRefCounterSA<DeallocType>& operator=(const OsclRefCounterSA<DeallocType>& x);

        OsclAny *ptr;
        uint32 refcnt;
};

/**
 * Implementation of OsclRefCounterDA for multi-threaded use.
 * A templated lock class must be specified.
 */
template<class LockType>
class OsclRefCounterMTDA : public OsclRefCounter
{
    public:

        /**
         * Constructor
         * Takes a pointer to the buffer to track, and a pointer
         * to the deallocator object which will be used to delete
         * the buffer.
         *
         * When the reference count reaches zero, the buffer will
         * be deleted by the deallocator.  Also, the OsclRefCounter
         * object (this) will self-destruct when the reference count
         * is zero.  In some cases the OsclRefCounter object will
         * be part of the buffer being deleted.  For such cases,
         * the object pointer must be equal to the buffer pointer
         * given at construction.  If the object is not part of the
         * buffer being deleted, it will self-destruct with a call
         * to delete().
         *
         * @param p       pointer to the buffer to track
         * @param dealloc pointer to the deallocator to use when
         *                deleting the buffer
         */
        OsclRefCounterMTDA(OsclAny *p, OsclDestructDealloc *dealloc) :
                ptr(p), deallocator(dealloc), refcnt(1)
        {
            OSCL_ASSERT(ptr != NULL && deallocator != NULL);
        }

        /**
         * Destructor
         * empty
         */
        virtual ~OsclRefCounterMTDA() {}

        /**
         * Add to the reference count
         */
        void addRef()
        {
            lock.Lock();
            ++refcnt;
            lock.Unlock();
        }

        /**
         * Remove from the reference count
         */
        void removeRef()
        {
            lock.Lock();
            if (--refcnt == 0)
            {
                if (ptr == this)
                {

                    // buffer is part of the refcounter
                    deallocator->destruct_and_dealloc(this);
                }
                else
                {
                    // delete the buffer and the refcounter object
                    deallocator->destruct_and_dealloc(ptr);
                    delete(this);
                }
            }
            else
            {
                lock.Unlock();
            }
        }

        /**
         * Gets the current number of references
         */
        uint32 getCount()
        {
            return refcnt;
        }

    private:
        OsclRefCounterMTDA();
        OsclRefCounterMTDA(const OsclRefCounterMTDA<LockType>& x);
        OsclRefCounterMTDA<LockType>& operator=(const OsclRefCounterMTDA<LockType>& x);

        OsclAny *ptr;
        OsclDestructDealloc *deallocator;
        LockType lock;
        uint32 refcnt;
};


/**
 * Implementation of OsclRefCounterSA for multi-threaded use.
 * A templated lock class must be specified.
 */
template<class DeallocType, class LockType>
class OsclRefCounterMTSA : public OsclRefCounter
{
    public:
        /**
         * Constructor
         * Takes a pointer to the buffer to track.
         *
         * When the reference count reaches zero, the buffer will
         * be deleted by the deallocator.  Also, the OsclRefCounter
         * object (this) will self-destruct when the reference count
         * is zero.  In some cases the OsclRefCounter object will
         * be part of the buffer being deleted.  For such cases,
         * the object pointer must be equal to the buffer pointer
         * given at construction.  If the object is not part of the
         * buffer being deleted, it will self-destruct with a call
         * to delete().
         *
         * @param p       pointer to the buffer to track
         */
        OsclRefCounterMTSA(OsclAny *p) :
                ptr(p), refcnt(1)
        {
            OSCL_ASSERT(ptr != NULL);
        }

        /**
         * Destructor
         * empty
         */
        virtual ~OsclRefCounterMTSA() {}

        /**
         * Add to the reference count
         */
        void addRef()
        {
            lock.Lock();
            ++refcnt;
            lock.Unlock();
        }

        /**
         * Remove from the reference count
         */
        void removeRef()
        {
            lock.Lock();
            if (--refcnt == 0)
            {
                if (ptr == this)
                {
                    // buffer is part of the refcounter
                    DeallocType deallocator;
                    deallocator.destruct_and_dealloc(this);
                }
                else
                {

                    // delete the buffer and the recounter object
                    DeallocType deallocator;
                    deallocator.destruct_and_dealloc(ptr);
                    delete(this);
                }
            }
            else
            {
                lock.Unlock();
            }
        }

        /**
         * Gets the current number of references
         */
        uint32 getCount()
        {
            return refcnt;
        }

    private:
        OsclRefCounterMTSA();
        OsclRefCounterMTSA(const OsclRefCounterMTSA<DeallocType, LockType>& x);
        OsclRefCounterMTSA<DeallocType, LockType>& operator=(const OsclRefCounterMTSA<DeallocType, LockType>& x);

        OsclAny *ptr;
        LockType lock;
        uint32 refcnt;
};

/**
    Implementation of an Oscl_DefAlloc class with a
    built-in ref counter.
 */
template<class DefAlloc>
class Oscl_DefAllocWithRefCounter: public OsclRefCounter, public DefAlloc
{
    public:
        /** Create object
        */
        static Oscl_DefAllocWithRefCounter* New()
        {
            DefAlloc alloc;
            OsclAny* p = alloc.ALLOCATE(sizeof(Oscl_DefAllocWithRefCounter));
            return new(p) Oscl_DefAllocWithRefCounter();
        }

        /** Delete object
        */
        void Delete()
        {
            removeRef();
        }

        void addRef()
        {
            refcount++;
        }

        void removeRef()
        {
            --refcount;
            if (refcount == 0)
                DefAlloc::deallocate(this);
        }

        uint32 getCount()
        {
            return refcount;
        }

    private:
        Oscl_DefAllocWithRefCounter(): refcount(1)
        {}
        uint32 refcount;
};

/*! @} */


#endif
