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

//             O S C L C O N F I G _ M E M O R Y

// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =




#ifndef OSCLCONFIG_MEMORY_H_INCLUDED
#define OSCLCONFIG_MEMORY_H_INCLUDED


#ifndef OSCLCONFIG_H_INCLUDED
#include "osclconfig.h"
#endif

#ifndef OSCLCONFIG_ANSI_MEMORY_H_INCLUDED
#include "osclconfig_ansi_memory.h"
#endif

#ifdef NDEBUG
#define OSCL_BYPASS_MEMMGT 1
#else
#define OSCL_BYPASS_MEMMGT 1  //disabling memory managerment
#endif


/* PVMEM_INST_LEVEL - Memory leak instrumentation level enables the compilation
 * of detailed memory leak info (filename + line number).
 * PVMEM_INST_LEVEL 0: Release mode.
 * PVMEM_INST_LEVEL 1: Debug mode.
 */

/* OSCL_HAS_GLOBAL_NEW_DELETE - Enables or disables the definition of overloaded
 * global memory operators in oscl_mem.h
 *
 * Release Mode: OSCL_HAS_GLOBAL_NEW_DELETE 0
 * Debug Mode: OSCL_HAS_GLOBAL_NEW_DELETE 1
 */

#if(OSCL_RELEASE_BUILD)
#define OSCL_HAS_GLOBAL_NEW_DELETE 0
#define PVMEM_INST_LEVEL 0
#else
#define OSCL_HAS_GLOBAL_NEW_DELETE 0
#define PVMEM_INST_LEVEL 1
#endif

#if(OSCL_HAS_GLOBAL_NEW_DELETE)
//Detect if <new> or <new.h> is included anyplace to avoid a compile error.
#if defined(_INC_NEW)
#error Duplicate New Definition!
#endif //_INC_NEW
#if defined(_NEW_)
#error Duplicate New Definition!
#endif //_NEW_
#endif //OSCL_HAS_GLOBAL_NEW_DELETE

#ifdef __cplusplus
#include <new> //for placement new
#endif //__cplusplus

//OSCL_HAS_HEAP_BASE_SUPPORT - Enables or disables overloaded memory operators in HeapBase class
#define OSCL_HAS_HEAP_BASE_SUPPORT 1

#define OSCL_HAS_SYMBIAN_MEMORY_FUNCS 0


#include "osclconfig_memory_check.h"


#endif
