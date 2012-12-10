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

//               O S C L _ E R R O R _ I M P _ J U M P S

// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

/*! \addtogroup osclerror OSCL Error
 *
 * @{
 */


/** \file oscl_error_imp_jumps.h
    \brief Implemenation of using Setjmp / Longjmp.
*/

#ifndef OSCL_ERROR_IMP_JUMPS_H_INCLUDED
#define OSCL_ERROR_IMP_JUMPS_H_INCLUDED

#ifndef OSCL_ERROR_TRAPCLEANUP_H_INCLUDED
#include "oscl_error_trapcleanup.h"
#endif
#ifndef OSCL_ASSERT_H_INCLUDED
#include "oscl_assert.h"
#endif

// Implemenation of Leave using Setjmp / Longjmp.

//ANSI setjmp/longjmp implementation.  This is needed on any OS
//that does not support C++ exceptions.  This is a complete implementation.

#ifndef OSCLCONFIG_ERROR_H_INCLUDED
#include "osclconfig_error.h"
#endif

#ifndef OSCL_ERROR_TRAPCLEANUP_H_INCLUDED
#include "oscl_error_trapcleanup.h"
#endif
#ifndef OSCL_DEFALLOC_H_INCLUDED
#include "oscl_defalloc.h"
#endif
#ifndef OSCL_ERROR_H_INCLUDED
#include "oscl_error.h"
#endif

class Oscl_DefAlloc;

//this defines the maximum depth of the jump mark stack.
#define OSCL_JUMP_MAX_JUMP_MARKS OSCL_MAX_TRAP_LEVELS


//OsclJump class
class OsclJump
{
    public:
        //for use in macros only.

        OSCL_IMPORT_REF static void StaticJump(int a);

        void Jump(int a)
        {
            if (!Top())
            {
                //Note: you can't leave here, since leave would
                //invoke this routine again.  It is not safe to return
                //either, because calling code is expecting an execution
                //end.
                OSCL_ASSERT(false);
                _OSCL_Abort();
            }
            longjmp(*Top(), a);
        }

        jmp_buf *Top()
        {
            OSCL_ASSERT(iJumpIndex >= 0);
            return &iJumpArray[iJumpIndex];
        }

        ~OsclJump()
        {
            //jump mark stack should be empty at this point.
            OSCL_ASSERT(iJumpIndex == (-1));
        }

    private:
        OsclJump(): iJumpIndex(-1) {}

        void PushMark()
        {
            OSCL_ASSERT(iJumpIndex < (OSCL_JUMP_MAX_JUMP_MARKS - 1));//jump stack is full!
            iJumpIndex++;
        }

        void PopMark()
        {
            OSCL_ASSERT(iJumpIndex >= 0);//jump stack is empty!
            iJumpIndex--;
        }

        jmp_buf iJumpArray[OSCL_JUMP_MAX_JUMP_MARKS];

        //index to top of stack, or (-1) when stack is empty
        int32 iJumpIndex;

        friend class OsclErrorTrapImp;
};


//internal jump type codes.
#define internalLeave (-1)

//Leave uses the OsclJump methods
#define PVError_DoLeave() OsclJump::StaticJump(internalLeave)

//_PV_TRAP macro catches leaves.
//_r is leave code, _s is statements to execute.
#define _PV_TRAP(__r,__s)\
    __r=OsclErrNone;\
    {\
        OsclErrorTrapImp* __trap=OsclErrorTrapImp::Trap();\
        if(!__trap){__s;}else{\
        int __tr=setjmp(*(__trap->iJumpData->Top()));\
        if (__tr==0)\
        {__s;}\
        else if (__tr==internalLeave)\
        {\
            sigset_t set;\
            sigemptyset(&set);\
            sigaddset(&set,SIGSEGV);\
            sigprocmask(SIG_UNBLOCK, &set, NULL);\
            __r=__trap->iLeave;\
        }\
        __trap->UnTrap();}\
    }

//Same as _PV_TRAP but avoids a TLS lookup.
// __trapimp is the OsclErrorTrapImp* for the calling thread.
#define _PV_TRAP_NO_TLS(__trapimp,__r,__s)\
    __r=OsclErrNone;\
    {\
        OsclErrorTrapImp* __trap=OsclErrorTrapImp::TrapNoTls(__trapimp);\
        if(!__trap){__s;}else{\
        int __tr=setjmp(*(__trap->iJumpData->Top()));\
        if (__tr==0)\
        {__s;}\
        else if (__tr==internalLeave)\
        {__r=__trap->iLeave;}\
        __trap->UnTrap();}\
    }


#endif // OSCL_ERROR_IMP_JUMPS_H_INCLUDED

/*! @} */
