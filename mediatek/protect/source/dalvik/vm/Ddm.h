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
 * Dalvik Debug Monitor
 */
#ifndef DALVIK_DDM_H_
#define DALVIK_DDM_H_

/*
 * Handle a packet full of DDM goodness.
 *
 * Returns "true" if we have anything to say in return; in which case,
 * "*pReplyBuf" and "*pReplyLen" will also be set.
 */
bool dvmDdmHandlePacket(const u1* buf, int dataLen, u1** pReplyBuf,
    int* pReplyLen);

/*
 * Deal with the DDM server connecting and disconnecting.
 */
void dvmDdmConnected(void);
void dvmDdmDisconnected(void);

/*
 * Turn thread notification on or off.
 */
void dvmDdmSetThreadNotification(bool enable);

/*
 * If thread start/stop notification is enabled, call this when threads
 * are created or die.
 */
void dvmDdmSendThreadNotification(Thread* thread, bool started);

/*
 * If thread start/stop notification is enabled, call this when the
 * thread name changes.
 */
void dvmDdmSendThreadNameChange(int threadId, StringObject* newName);

/*
 * Generate a byte[] full of thread stats for a THST packet.
 */
ArrayObject* dvmDdmGenerateThreadStats(void);

/*
 * Let the heap know that the HPIF when value has changed.
 *
 * @return true iff the when value is supported by the VM.
 */
bool dvmDdmHandleHpifChunk(int when);

/*
 * Let the heap know that the HPSG or NHSG what/when values have changed.
 *
 * @param native false for an HPSG chunk, true for an NHSG chunk
 *
 * @return true iff the what/when values are supported by the VM.
 */
bool dvmDdmHandleHpsgNhsgChunk(int when, int what, bool native);

/*
 * Get an array of StackTraceElement objects for the specified thread.
 */
ArrayObject* dvmDdmGetStackTraceById(u4 threadId);

/*
 * Gather up recent allocation data and return it in a byte[].
 *
 * Returns NULL on failure with an exception raised.
 */
ArrayObject* dvmDdmGetRecentAllocations(void);

#endif  // DALVIK_DDM_H_
