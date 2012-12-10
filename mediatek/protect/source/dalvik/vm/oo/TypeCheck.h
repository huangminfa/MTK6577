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
 * instanceof, checkcast, etc.
 */
#ifndef DALVIK_OO_TYPECHECK_H_
#define DALVIK_OO_TYPECHECK_H_

/* VM startup/shutdown */
bool dvmInstanceofStartup(void);
void dvmInstanceofShutdown(void);


/* used by dvmInstanceof; don't call */
extern "C" int dvmInstanceofNonTrivial(const ClassObject* instance,
                                       const ClassObject* clazz);

/*
 * Determine whether "instance" is an instance of "clazz".
 *
 * Returns 0 (false) if not, 1 (true) if so.
 */
INLINE int dvmInstanceof(const ClassObject* instance, const ClassObject* clazz)
{
    if (instance == clazz) {
        if (CALC_CACHE_STATS)
            gDvm.instanceofCache->trivial++;
        return 1;
    } else
        return dvmInstanceofNonTrivial(instance, clazz);
}

/*
 * Determine whether a class implements an interface.
 *
 * Returns 0 (false) if not, 1 (true) if so.
 */
int dvmImplements(const ClassObject* clazz, const ClassObject* interface);

/*
 * Determine whether "sub" is a sub-class of "clazz".
 *
 * Returns 0 (false) if not, 1 (true) if so.
 */
INLINE int dvmIsSubClass(const ClassObject* sub, const ClassObject* clazz) {
    do {
        /*printf("###### sub='%s' clazz='%s'\n", sub->name, clazz->name);*/
        if (sub == clazz)
            return 1;
        sub = sub->super;
    } while (sub != NULL);

    return 0;
}

/*
 * Determine whether or not we can store an object into an array, based
 * on the classes of the two.
 *
 * Returns 0 (false) if not, 1 (true) if so.
 */
extern "C" bool dvmCanPutArrayElement(const ClassObject* elemClass,
    const ClassObject* arrayClass);

#endif  // DALVIK_OO_TYPECHECK_H_
