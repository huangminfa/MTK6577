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

//     O S C L C O N F I G   ( P L A T F O R M   C O N F I G   I N F O )

// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =


/*! \file osclconfig.h
 *  \brief This file contains configuration information for the linux platform
 *
 */

#ifndef OSCLCONFIG_H_INCLUDED
#define OSCLCONFIG_H_INCLUDED

// system includes for dynamic registry
#include <dirent.h>
#include <dlfcn.h>

// include common include for determining sizes from limits.h
#include "osclconfig_limits_typedefs.h"

//This switch turns off some profiling and debug settings
#ifdef NDEBUG
#define OSCL_RELEASE_BUILD 1
#else
#define OSCL_RELEASE_BUILD 0
#endif

// include common unix definitions
#include "osclconfig_unix_common.h"

// define the suffix for unsigned constants
#define OSCL_UNSIGNED_CONST(x) x##u

// override the common definition for
#undef OSCL_NATIVE_UINT64_TYPE
#define OSCL_NATIVE_UINT64_TYPE    u_int64_t

// include the definitions for the processor
#include "osclconfig_ix86.h"

/* The __TFS__ macro is used to optionally expand to "<>" depending on the
 * compiler.  Some compilers require it to indicate that the friend function
 * is a template function as specified in the standard, but others don't
 * like it so it will handled with a macro expansion that depends on the
 * compiler.
 */
#define __TFS__ <>

#ifdef __GNUC__
#if (__GNUC__ > 3) || ((__GNUC__ == 3) && (__GNUC_MINOR__ >= 4))
#define OSCL_TEMPLATED_DESTRUCTOR_CALL(type,simple_type) ~type ()
#endif
#endif

#define OSCL_BEGIN_PACKED
#define OSCL_PACKED_VAR(x)  x __attribute__((packed))
#define OSCL_PACKED_STRUCT_BEGIN
#define OSCL_PACKED_STRUCT_END __attribute__((packed))
#define OSCL_END_PACKED

#if(OSCL_RELEASE_BUILD)
//no logging
#define PVLOGGER_INST_LEVEL 0
#elif defined(NDEBUG)
//profiling only
#define PVLOGGER_INST_LEVEL 2
#else
//full logging
#define PVLOGGER_INST_LEVEL 5
#endif

//set this to 1 to enable OSCL_ASSERT in release builds.
#define OSCL_ASSERT_ALWAYS 0


// check all osclconfig required macros are defined
#include "osclconfig_check.h"

#endif
