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
 * Copyright (C) 2010 The Android Open Source Project
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

#ifndef DALVIK_ALLOC_VISITINLINES_H_
#define DALVIK_ALLOC_VISITINLINES_H_

/*
 * Visits the instance fields of a class or data object.
 */
static void visitFields(Visitor *visitor, Object *obj, void *arg)
{
    assert(visitor != NULL);
    assert(obj != NULL);
    assert(obj->clazz != NULL);
    if (obj->clazz->refOffsets != CLASS_WALK_SUPER) {
        size_t refOffsets = obj->clazz->refOffsets;
        while (refOffsets != 0) {
            size_t rshift = CLZ(refOffsets);
            size_t offset = CLASS_OFFSET_FROM_CLZ(rshift);
            Object **ref = (Object **)BYTE_OFFSET(obj, offset);
            (*visitor)(ref, arg);
            refOffsets &= ~(CLASS_HIGH_BIT >> rshift);
        }
    } else {
        for (ClassObject *clazz = obj->clazz;
             clazz != NULL;
             clazz = clazz->super) {
            InstField *field = clazz->ifields;
            for (int i = 0; i < clazz->ifieldRefCount; ++i, ++field) {
                size_t offset = field->byteOffset;
                Object **ref = (Object **)BYTE_OFFSET(obj, offset);
                (*visitor)(ref, arg);
            }
        }
    }
}

/*
 * Visits the static fields of a class object.
 */
static void visitStaticFields(Visitor *visitor, ClassObject *clazz,
                              void *arg)
{
    assert(visitor != NULL);
    assert(clazz != NULL);
    for (int i = 0; i < clazz->sfieldCount; ++i) {
        char ch = clazz->sfields[i].signature[0];
        if (ch == '[' || ch == 'L') {
            (*visitor)(&clazz->sfields[i].value.l, arg);
        }
    }
}

/*
 * Visit the interfaces of a class object.
 */
static void visitInterfaces(Visitor *visitor, ClassObject *clazz,
                            void *arg)
{
    assert(visitor != NULL);
    assert(clazz != NULL);
    for (int i = 0; i < clazz->interfaceCount; ++i) {
        (*visitor)(&clazz->interfaces[i], arg);
    }
}

/*
 * Visits all the references stored in a class object instance.
 */
static void visitClassObject(Visitor *visitor, Object *obj, void *arg)
{
    ClassObject *asClass;

    assert(visitor != NULL);
    assert(obj != NULL);
    assert(obj->clazz != NULL);
    assert(!strcmp(obj->clazz->descriptor, "Ljava/lang/Class;"));
    (*visitor)(&obj->clazz, arg);
    asClass = (ClassObject *)obj;
    if (IS_CLASS_FLAG_SET(asClass, CLASS_ISARRAY)) {
        (*visitor)(&asClass->elementClass, arg);
    }
    if (asClass->status > CLASS_IDX) {
        (*visitor)(&asClass->super, arg);
    }
    (*visitor)(&asClass->classLoader, arg);
    visitFields(visitor, obj, arg);
    visitStaticFields(visitor, asClass, arg);
    if (asClass->status > CLASS_IDX) {
      visitInterfaces(visitor, asClass, arg);
    }
}

/*
 * Visits the class object and, if the array is typed as an object
 * array, all of the array elements.
 */
static void visitArrayObject(Visitor *visitor, Object *obj, void *arg)
{
    assert(visitor != NULL);
    assert(obj != NULL);
    assert(obj->clazz != NULL);
    (*visitor)(&obj->clazz, arg);
    if (IS_CLASS_FLAG_SET(obj->clazz, CLASS_ISOBJECTARRAY)) {
        ArrayObject *array = (ArrayObject *)obj;
        Object **contents = (Object **)(void *)array->contents;
        for (size_t i = 0; i < array->length; ++i) {
            (*visitor)(&contents[i], arg);
        }
    }
}

/*
 * Visits the class object and reference typed instance fields of a
 * data object.
 */
static void visitDataObject(Visitor *visitor, Object *obj, void *arg)
{
    assert(visitor != NULL);
    assert(obj != NULL);
    assert(obj->clazz != NULL);
    (*visitor)(&obj->clazz, arg);
    visitFields(visitor, obj, arg);
}

/*
 * Like visitDataObject, but visits the hidden referent field that
 * belongings to the subclasses of java.lang.Reference.
 */
static void visitReferenceObject(Visitor *visitor, Object *obj, void *arg)
{
    assert(visitor != NULL);
    assert(obj != NULL);
    assert(obj->clazz != NULL);
    visitDataObject(visitor, obj, arg);
    size_t offset = gDvm.offJavaLangRefReference_referent;
    Object **ref = (Object **)BYTE_OFFSET(obj, offset);
    (*visitor)(ref, arg);
}

/*
 * Visits all of the reference stored in an object.
 */
static void visitObject(Visitor *visitor, Object *obj, void *arg)
{
    assert(visitor != NULL);
    assert(obj != NULL);
    assert(obj->clazz != NULL);
    if (dvmIsClassObject(obj)) {
        visitClassObject(visitor, obj, arg);
    } else if (IS_CLASS_FLAG_SET(obj->clazz, CLASS_ISARRAY)) {
        visitArrayObject(visitor, obj, arg);
    } else if (IS_CLASS_FLAG_SET(obj->clazz, CLASS_ISREFERENCE)) {
        visitReferenceObject(visitor, obj, arg);
    } else {
        visitDataObject(visitor, obj, arg);
    }
}

#endif  // DALVIK_ALLOC_VISITINLINES_H_
