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
 * Array handling.
 */
#ifndef DALVIK_OO_ARRAY_H_
#define DALVIK_OO_ARRAY_H_

/*
 * Find a matching array class.  If it doesn't exist, create it.
 *
 * "descriptor" looks like "[I".
 *
 * "loader" should be the defining class loader for the elements held
 * in the array.
 */
ClassObject* dvmFindArrayClass(const char* descriptor, Object* loader);

/*
 * Find the array class for the specified class.  If "elemClassObj" is the
 * class "Foo", this returns the class object for "[Foo".
 */
ClassObject* dvmFindArrayClassForElement(ClassObject* elemClassObj);

/*
 * Create a new array, given an array class.  The class may represent an
 * array of references or primitives.
 *
 * Returns NULL with an exception raised if allocation fails.
 */
extern "C" ArrayObject* dvmAllocArrayByClass(ClassObject* arrayClass,
    size_t length, int allocFlags);

/*
 * Allocate an array whose members are primitives (bools, ints, etc.).
 *
 * "type" should be 'I', 'J', 'Z', etc.
 *
 * The new object will be added to the "tracked alloc" table.
 *
 * Returns NULL with an exception raised if allocation fails.
 */
ArrayObject* dvmAllocPrimitiveArray(char type, size_t length, int allocFlags);

/*
 * Allocate an array with multiple dimensions.  Elements may be Objects or
 * primitive types.
 *
 * The base object will be added to the "tracked alloc" table.
 *
 * Returns NULL with an exception raised if allocation fails.
 */
ArrayObject* dvmAllocMultiArray(ClassObject* arrayClass, int curDim,
    const int* dimensions);

/*
 * Verify that the object is actually an array.
 *
 * Does not verify that the object is actually a non-NULL object.
 */
INLINE bool dvmIsArray(const ArrayObject* arrayObj)
{
    return ( ((Object*)arrayObj)->clazz->descriptor[0] == '[' );
}

/*
 * Verify that the array is an object array and not a primitive array.
 *
 * Does not verify that the object is actually a non-NULL object.
 */
INLINE bool dvmIsObjectArrayClass(const ClassObject* clazz)
{
    const char* descriptor = clazz->descriptor;
    return descriptor[0] == '[' && (descriptor[1] == 'L' ||
                                    descriptor[1] == '[');
}

/*
 * Verify that the array is an object array and not a primitive array.
 *
 * Does not verify that the object is actually a non-NULL object.
 */
INLINE bool dvmIsObjectArray(const ArrayObject* arrayObj)
{
    return dvmIsObjectArrayClass(arrayObj->clazz);
}

/*
 * Verify that the class is an array class.
 *
 * TODO: there may be some performance advantage to setting a flag in
 * the accessFlags field instead of chasing into the name string.
 */
INLINE bool dvmIsArrayClass(const ClassObject* clazz)
{
    return (clazz->descriptor[0] == '[');
}

/*
 * Copy the entire contents of one array of objects to another.  If the copy
 * is impossible because of a type clash, we fail and return "false".
 *
 * "dstElemClass" is the type of element that "dstArray" holds.
 */
bool dvmCopyObjectArray(ArrayObject* dstArray, const ArrayObject* srcArray,
    ClassObject* dstElemClass);

/*
 * Copy the entire contents of an array of boxed primitives into an
 * array of primitives.  The boxed value must fit in the primitive (i.e.
 * narrowing conversions are not allowed).
 */
bool dvmUnboxObjectArray(ArrayObject* dstArray, const ArrayObject* srcArray,
    ClassObject* dstElemClass);

/*
 * Returns the size of the given array object in bytes.
 */
size_t dvmArrayObjectSize(const ArrayObject *array);

/*
 * Returns the width, in bytes, required by elements in instances of
 * the array class.
 */
size_t dvmArrayClassElementWidth(const ClassObject* clazz);

#endif  // DALVIK_OO_ARRAY_H_
