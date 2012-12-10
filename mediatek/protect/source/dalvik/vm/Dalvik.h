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

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * All-inclusive internal header file.  Include this to get everything useful.
 */
#ifndef DALVIK_DALVIK_H_
#define DALVIK_DALVIK_H_

#include "Common.h"
#include "Inlines.h"
#include "Misc.h"
#include "Bits.h"
#include "BitVector.h"
#include "libdex/SysUtil.h"
#include "libdex/DexDebugInfo.h"
#include "libdex/DexFile.h"
#include "libdex/DexProto.h"
#include "libdex/DexUtf.h"
#include "libdex/ZipArchive.h"
#include "DvmDex.h"
#include "RawDexFile.h"
#include "Sync.h"
#include "oo/Object.h"
#include "Native.h"
#include "native/InternalNative.h"

#include "DalvikVersion.h"
#include "Debugger.h"
#include "Profile.h"
#include "UtfString.h"
#include "Intern.h"
#include "ReferenceTable.h"
#include "IndirectRefTable.h"
#include "AtomicCache.h"
#include "Thread.h"
#include "Ddm.h"
#include "Hash.h"
#include "interp/Stack.h"
#include "oo/Class.h"
#include "oo/Resolve.h"
#include "oo/Array.h"
#include "Exception.h"
#include "alloc/Alloc.h"
#include "alloc/CardTable.h"
#include "alloc/HeapDebug.h"
#include "alloc/WriteBarrier.h"
#include "oo/AccessCheck.h"
#include "JarFile.h"
#include "jdwp/Jdwp.h"
#include "SignalCatcher.h"
#include "StdioConverter.h"
#include "JniInternal.h"
#include "LinearAlloc.h"
#include "analysis/DexVerify.h"
#include "analysis/DexPrepare.h"
#include "analysis/RegisterMap.h"
#include "Init.h"
#include "libdex/DexOpcodes.h"
#include "libdex/InstrUtils.h"
#include "AllocTracker.h"
#include "PointerSet.h"
#if defined(WITH_JIT)
#include "compiler/Compiler.h"
#endif
#include "Globals.h"
#include "reflect/Reflect.h"
#include "oo/TypeCheck.h"
#include "Atomic.h"
#include "interp/Interp.h"
#include "InlineNative.h"
#include "oo/ObjectInlines.h"

#endif  // DALVIK_DALVIK_H_
