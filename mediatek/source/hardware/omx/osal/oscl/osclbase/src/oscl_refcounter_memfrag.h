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

//           O S C L _ R E F C O U N T E R _ M E M F R A G

// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

/*! \addtogroup osclbase OSCL Base
 *
 * @{
 */



/**
 *  @file oscl_refcounter_memfrag.h
 *  @brief This file provides the definition of reference counted
 *  memory fragment, which provides access to a buffer and helps manage
 *  its manage its lifetime through the refcount.
 *
 */

#ifndef OSCL_REFCOUNTER_MEMFRAG_H_INCLUDED
#define OSCL_REFCOUNTER_MEMFRAG_H_INCLUDED

#ifndef OSCL_BASE_H_INCLUDED
#include "oscl_base.h"
#endif

#ifndef OSCL_REFCOUNTER_H_INCLUDED
#include "oscl_refcounter.h"
#endif


/**
 * Class to contain a memory fragment with it's associated
 * reference counter.
 */
class OsclRefCounterMemFrag
{
    public:

        /**
         * Constructor.
         * A valid memory fragment and reference counter are
         * required as input.  The memory fragment structure
         * will be copied locally.
         *
         * @param m      reference to memory fragment
         * @param r      pointer to the reference counter associated with the
         *               memory fragment.
         */
        OsclRefCounterMemFrag(OsclMemoryFragment &m, OsclRefCounter *r,
                              uint32 in_capacity) :
                memfrag(m), refcnt(r), capacity(in_capacity)
                // no need to increment refcnt--it should already be done.
        {}

        /**
         * Copy constructor.
         */
        OsclRefCounterMemFrag(const OsclRefCounterMemFrag &x) :
                memfrag(x.memfrag), refcnt(x.refcnt), capacity(x.capacity)
        {
            if (refcnt)
            {
                refcnt->addRef();
            }
        }

        /**
         * Default constructor.
         */
        OsclRefCounterMemFrag()
        {
            memfrag.ptr = 0;
            memfrag.len = 0;
            refcnt = 0;
            capacity = 0;
        }


        /**
         * Assignment Operator
         */
        OsclRefCounterMemFrag& operator= (const OsclRefCounterMemFrag &x)
        {
            if (this == &x)
            {
                // protect against self-assignment
                return *this;
            }

            // remove ref for current memfrag
            if (refcnt)
            {
                refcnt->removeRef();
            }

            // copy assigned object
            memfrag = x.memfrag;
            refcnt = x.refcnt;
            capacity = x.capacity;

            // add ref for new memfrag
            if (refcnt)
            {
                refcnt->addRef();
            }

            return *this;
        }

        /**
         * Destructor.
         * Removes this object's reference from the reference counter.
         * The reference counter will not be deleted.  The reference
         * counter is designed to self-delete when it's reference
         * count reaches 0.
         */
        ~OsclRefCounterMemFrag()
        {
            if (refcnt)
            {
                refcnt->removeRef();
            }
        }

        /**
         * Returns a pointer to the contained reference counter
         * object
         */
        OsclRefCounter* getRefCounter()
        {
            return refcnt;
        }

        /**
         * Returns a reference to the contained memory fragment
         * structure.
         */
        OsclMemoryFragment& getMemFrag()
        {
            return memfrag;
        }

        /**
         * Returns a pointer to the memory fragment data.
         */
        OsclAny* getMemFragPtr()
        {
            return memfrag.ptr;
        }

        /**
         * Returns the size of the memory fragment data which
         * equals its filled size.
         *
         * @return
         */
        uint32 getMemFragSize()
        {
            return memfrag.len;
        }

        /**
         * Returns the capacity of the memory fragment
         *
         * @return
         */
        uint32 getCapacity()
        {
            return capacity;
        }

        /**
         * Returns the reference counter's current count.
         */
        uint32 getCount()
        {
            return (refcnt) ? refcnt->getCount() : 0;
        }



    private:

        OsclMemoryFragment memfrag;
        OsclRefCounter *refcnt;
        uint32 capacity;
};


/*! @} */


#endif // OSCL_REFCOUNTER_MEMFRAG_H
