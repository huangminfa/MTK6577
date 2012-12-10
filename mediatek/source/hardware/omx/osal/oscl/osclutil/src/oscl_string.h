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

//               O S C L_ S T R I N G   C L A S S

//    This file contains a standardized set of string containers that
//    can be used in place of character arrays.

// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

/*! \addtogroup osclutil OSCL Util
 *
 * @{
 */


/*!
 * \file oscl_string.h
 * \brief Provides a standardized set of string containers that
 *    can be used in place of character arrays.
 *
 */


#ifndef OSCL_STRING_H_INCLUDED
#define OSCL_STRING_H_INCLUDED


#ifndef OSCL_BASE_H_INCLUDED
#include "oscl_base.h"
#endif

#ifndef OSCL_MEM_H_INCLUDED
#include "oscl_mem.h"
#endif

/**
    A common base class for string classes with
    "char" character format
*/
class OSCL_EXPORT_REF OSCL_String : public HeapBase

{
    public:
        typedef char chartype;

        /**
            This function returns the string size not including
            the null-terminator.
        */
        virtual uint32 get_size() const = 0;

        /**
            This function returns the maximum available storage size,
            not including null terminator.  The maximum size may be
            larger than the current string size.
        */
        virtual uint32 get_maxsize() const = 0;

        /**
            This function returns the C-style string for read access.
        */
        virtual const chartype* get_cstr() const = 0;

        /**
            This function returns true if the string is writable.
        */
        OSCL_IMPORT_REF virtual bool is_writable() const;

        /**
            This function returns the C-style string for write access.
            If the string is not writable it returns NULL.
        */
        virtual chartype* get_str() const = 0;

        /** Assignment operator
        */
        OSCL_IMPORT_REF OSCL_String& operator=(const OSCL_String& src);

        /** Assignment operator
            @param: null-terminated string
        */
        OSCL_IMPORT_REF OSCL_String& operator=(const chartype* cstr);

        /** Append operator.
            This operator appends the input string to this object.
            The string may be truncated to fit available storage.
        */
        OSCL_IMPORT_REF OSCL_String& operator+=(const OSCL_String& src);

        /** Append operator.
            This operator appends the input string to this object.
            The string may be truncated to fit available storage.
            @param: null-terminated string
        */
        OSCL_IMPORT_REF OSCL_String& operator+=(const chartype* cstr);

        /** Append operator.
            This operator appends the input character to this object.
            The string may be truncated to fit available storage.
        */
        OSCL_IMPORT_REF OSCL_String& operator+=(const chartype c);

        /** Comparison operators
        */
        OSCL_IMPORT_REF bool operator== (const OSCL_String& src) const;
        OSCL_IMPORT_REF bool operator!= (const OSCL_String& src) const;
        OSCL_IMPORT_REF bool operator< (const OSCL_String& src) const;
        OSCL_IMPORT_REF bool operator<= (const OSCL_String& src) const;
        OSCL_IMPORT_REF bool operator> (const OSCL_String& src) const;
        OSCL_IMPORT_REF bool operator>= (const OSCL_String& src) const;

        /** Comparison operator
            @param: null-terminated string
        */
        OSCL_IMPORT_REF bool operator== (const chartype* cstr) const;

        /**
            This is subscript notation to access a character
            at the given position.
            If the index is outside the current size range then the
            function leaves.
        */
        OSCL_IMPORT_REF chartype operator[](uint32 index) const;

        /**
            This function returns the character at the given position.
            If the index is outside the current size range then the
            function leaves.
        */
        OSCL_IMPORT_REF virtual chartype read(uint32 index)const;

        /**
            This function performs a hash operation on the string.
            If the string is not writable, the function leaves.
        */
        OSCL_IMPORT_REF virtual int8 hash() const;

        /**
            This function stores a character at the specified position.
            If the string is not writable, the function leaves.
            If the index is outside the current size range then the
            function leaves.
        */
        OSCL_IMPORT_REF virtual void write(uint32 index, chartype c);

        /**
            This function replaces characters at the specified offset
            within the current string.
            If the string is not writable, the function leaves.
            The characters may be truncted to fit the current storage.
            @param offset: the offset into the existing string buffer
            @param length: number of characters to copy.
            @param ptr: character buffer, not necessarily null-terminated.
        */
        OSCL_IMPORT_REF virtual void write(uint32 offset, uint32 length, const chartype* buf);

#ifdef T_ARM
//ADS 1.2 compiler doesn't interpret "protected" correctly for templates.
//so the constructor/destructor need to be public.
    public:
#else
    protected:
#endif
        OSCL_IMPORT_REF OSCL_String();
        OSCL_IMPORT_REF virtual ~OSCL_String();

    protected:
        /** Each representation class must implement these pure virtuals.
        */

        /** Set string representation to input
            null-terminated string.
        */
        virtual void set_rep(const chartype* cstr) = 0;

        /** Append the input null-terminated string to the current
            string.  The string may be truncated to fit the available
            storage.
        */
        virtual void append_rep(const chartype* cstr) = 0;

        /** Set string representation to input string.
        */
        virtual void set_rep(const OSCL_String& src) = 0;

        /** Append the input string to the current string.
            The string may be truncated to fit the available storage.
        */
        virtual void append_rep(const OSCL_String& src) = 0;

        /** Update the length of the string.  This function
            will only be called when the string is writable.
        */
        virtual void set_len(uint32 len) = 0;
};

/**
    A common base class for string classes with
    wide character (oscl_wchar) format.
    OSCL_wString and OSCL_String are identical except
    for the character format.
    For descriptions, see OSCL_String.
*/
class OSCL_EXPORT_REF OSCL_wString
{
    public:
        typedef oscl_wchar chartype;

        virtual uint32 get_size() const = 0;

        virtual uint32 get_maxsize() const = 0;

        virtual const chartype* get_cstr() const = 0;

        OSCL_IMPORT_REF virtual bool is_writable() const;

        virtual chartype* get_str() const = 0;

        OSCL_IMPORT_REF OSCL_wString& operator=(const OSCL_wString& src);
        OSCL_IMPORT_REF OSCL_wString& operator=(const chartype* cstr);

        OSCL_IMPORT_REF OSCL_wString& operator+=(const OSCL_wString& src);
        OSCL_IMPORT_REF OSCL_wString& operator+=(const chartype* cstr);
        OSCL_IMPORT_REF OSCL_wString& operator+=(const chartype c);

        OSCL_IMPORT_REF bool operator== (const OSCL_wString& src) const;
        OSCL_IMPORT_REF bool operator!= (const OSCL_wString& src) const;
        OSCL_IMPORT_REF bool operator< (const OSCL_wString& src) const;
        OSCL_IMPORT_REF bool operator<= (const OSCL_wString& src) const;
        OSCL_IMPORT_REF bool operator> (const OSCL_wString& src) const;
        OSCL_IMPORT_REF bool operator>= (const OSCL_wString& src) const;
        OSCL_IMPORT_REF bool operator== (const chartype* cstr) const;

        OSCL_IMPORT_REF chartype operator[](uint32 index) const;

        OSCL_IMPORT_REF virtual chartype read(uint32 index)const;

        OSCL_IMPORT_REF virtual int8 hash() const;

        OSCL_IMPORT_REF virtual void write(uint32 index, chartype c);
        OSCL_IMPORT_REF virtual void write(uint32 offset, uint32 length, const chartype* buf);

#ifdef T_ARM
//ADS 1.2 compiler doesn't interpret "protected" correctly for templates.
//so the constructor/destructor need to be public.
    public:
#else
    protected:
#endif
        OSCL_IMPORT_REF OSCL_wString();
        OSCL_IMPORT_REF virtual ~OSCL_wString();

    protected:
        virtual void set_rep(const chartype* cstr) = 0;
        virtual void append_rep(const chartype* cstr) = 0;

        virtual void set_rep(const OSCL_wString& src) = 0;
        virtual void append_rep(const OSCL_wString& src) = 0;

        virtual void set_len(uint32 len) = 0;
};

#endif   // OSCL_STRING_H_INCLUDED

/*! @} */
