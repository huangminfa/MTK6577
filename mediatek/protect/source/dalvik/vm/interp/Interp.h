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
 * Dalvik interpreter public definitions.
 */
#ifndef DALVIK_INTERP_INTERP_H_
#define DALVIK_INTERP_INTERP_H_

/*
 * Stash the dalvik PC in the frame.  Called  during interpretation.
 */
INLINE void dvmExportPC(const u2* pc, const u4* fp)
{
    SAVEAREA_FROM_FP(fp)->xtra.currentPc = pc;
}

/*
 * Extract the Dalvik opcode
 */
#define GET_OPCODE(_inst) (((_inst & 0xff) == OP_DISPATCH_FF) ? \
                           (0x100 + ((_inst >> 8) & 0xff)) : (_inst & 0xff))

/*
 * Interpreter entry point.  Call here after setting up the interpreted
 * stack (most code will want to get here via dvmCallMethod().)
 */
void dvmInterpret(Thread* thread, const Method* method, JValue* pResult);

/*
 * Throw an exception for a problem detected by the verifier.
 *
 * This is called from the handler for the throw-verification-error
 * instruction.  "method" is the method currently being executed.
 */
extern "C" void dvmThrowVerificationError(const Method* method,
                                          int kind, int ref);

/*
 * One-time initialization and shutdown.
 */
bool dvmBreakpointStartup(void);
void dvmBreakpointShutdown(void);
void dvmInitInterpreterState(Thread* self);

/*
 * Breakpoint implementation.
 */
void dvmInitBreakpoints();
void dvmAddBreakAddr(Method* method, unsigned int instrOffset);
void dvmClearBreakAddr(Method* method, unsigned int instrOffset);
bool dvmAddSingleStep(Thread* thread, int size, int depth);
void dvmClearSingleStep(Thread* thread);

/*
 * Recover the opcode that was replaced by a breakpoint.
 */
extern "C" u1 dvmGetOriginalOpcode(const u2* addr);

/*
 * Flush any breakpoints associated with methods in "clazz".
 */
void dvmFlushBreakpoints(ClassObject* clazz);

/*
 * Debugger support
 */
extern "C" void dvmCheckBefore(const u2 *dPC, u4 *fp, Thread* self);
extern "C" void dvmReportExceptionThrow(Thread* self, Object* exception);
extern "C" void dvmReportPreNativeInvoke(const Method* methodToCall, Thread* self, u4* fp);
extern "C" void dvmReportPostNativeInvoke(const Method* methodToCall, Thread* self, u4* fp);
extern "C" void dvmReportInvoke(Thread* self, const Method* methodToCall);
extern "C" void dvmReportReturn(Thread* self);

/*
 * InterpBreak & subMode control
 */
void dvmDisableSubMode(Thread* thread, ExecutionSubModes subMode);
extern "C" void dvmEnableSubMode(Thread* thread, ExecutionSubModes subMode);
void dvmDisableAllSubMode(ExecutionSubModes subMode);
void dvmEnableAllSubMode(ExecutionSubModes subMode);
void dvmAddToSuspendCounts(Thread* thread, int delta, int dbgDelta);
void dvmCheckInterpStateConsistency();
void dvmInitializeInterpBreak(Thread* thread);

/*
 * Register a callback to occur at the next safe point for a single thread.
 * If funct is NULL, the previous registration is cancelled.
 *
 * The callback prototype is:
 *        bool funct(Thread* thread, void* arg)
 *
 *  If funct returns false, the callback will be disarmed.  If true,
 *  it will stay in effect.
 */
void dvmArmSafePointCallback(Thread* thread, SafePointCallback funct,
                             void* arg);


#ifndef DVM_NO_ASM_INTERP
extern void* dvmAsmInstructionStart[];
extern void* dvmAsmAltInstructionStart[];
#endif

#endif  // DALVIK_INTERP_INTERP_H_
