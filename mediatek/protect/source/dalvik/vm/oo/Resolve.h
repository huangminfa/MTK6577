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
 * Resolve "constant pool" references into pointers to VM structs.
 */
#ifndef DALVIK_OO_RESOLVE_H_
#define DALVIK_OO_RESOLVE_H_

/*
 * "Direct" and "virtual" methods are stored independently.  The type of call
 * used to invoke the method determines which list we search, and whether
 * we travel up into superclasses.
 *
 * (<clinit>, <init>, and methods declared "private" or "static" are stored
 * in the "direct" list.  All others are stored in the "virtual" list.)
 */
enum MethodType {
    METHOD_UNKNOWN  = 0,
    METHOD_DIRECT,      // <init>, private
    METHOD_STATIC,      // static
    METHOD_VIRTUAL,     // virtual, super
    METHOD_INTERFACE    // interface
};

/*
 * Resolve a class, given the referring class and a constant pool index
 * for the DexTypeId.
 *
 * Does not initialize the class.
 *
 * Throws an exception and returns NULL on failure.
 */
extern "C" ClassObject* dvmResolveClass(const ClassObject* referrer,
                                        u4 classIdx,
                                        bool fromUnverifiedConstant);

/*
 * Resolve a direct, static, or virtual method.
 *
 * Can cause the method's class to be initialized if methodType is
 * METHOD_STATIC.
 *
 * Throws an exception and returns NULL on failure.
 */
extern "C" Method* dvmResolveMethod(const ClassObject* referrer, u4 methodIdx,
                                    MethodType methodType);

/*
 * Resolve an interface method.
 *
 * Throws an exception and returns NULL on failure.
 */
Method* dvmResolveInterfaceMethod(const ClassObject* referrer, u4 methodIdx);

/*
 * Resolve an instance field.
 *
 * Throws an exception and returns NULL on failure.
 */
extern "C" InstField* dvmResolveInstField(const ClassObject* referrer,
                                          u4 ifieldIdx);

/*
 * Resolve a static field.
 *
 * Causes the field's class to be initialized.
 *
 * Throws an exception and returns NULL on failure.
 */
extern "C" StaticField* dvmResolveStaticField(const ClassObject* referrer,
                                              u4 sfieldIdx);

/*
 * Resolve a "const-string" reference.
 *
 * Throws an exception and returns NULL on failure.
 */
extern "C" StringObject* dvmResolveString(const ClassObject* referrer, u4 stringIdx);

/*
 * Return debug string constant for enum.
 */
const char* dvmMethodTypeStr(MethodType methodType);

#endif  // DALVIK_OO_RESOLVE_H_
