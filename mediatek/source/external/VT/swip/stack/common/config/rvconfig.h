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

/***********************************************************************
Filename   : rvconfig.h
Description: config files which incorporates other config files
************************************************************************
        Copyright (c) 2001 RADVISION Inc. and RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Inc. and RADVISION Ltd.. No part of this document may be
reproduced in any form whatsoever without written prior approval by
RADVISION Inc. or RADVISION Ltd..

RADVISION Inc. and RADVISION Ltd. reserve the right to revise this
publication and make changes without obligation to notify any person of
such revisions or changes.
***********************************************************************/
#ifndef RV_SYSCONFIG_H
#define RV_SYSCONFIG_H

#ifdef __VT_SWIP__
#include "vt_swip_struct.h" /* Common adaptation header for non-KAL OS */
#include "vt_swip_sys_service.h"  //[2010-05-31] Chiwei: Add for removing undefine warnings
#else
#include "kal_release.h"
#include "app_buff_alloc.h"
#endif /* End: ifdef __VT_SWIP__ */


#include "rvarchdefs.h"
#include "rvosdefs.h"
#include "rvtooldefs.h"
#include "rvinterfacesdefs.h"

//#define RVCOREAPI   
//#define RVAPI      
#define __VT_LOG_H245_CONTENT__
#define __VT_MONA__
#ifdef _DEBUG
#define RV_DEBUG
#define SIP_DEBUG
#else
#define RV_RELEASE
#endif

#ifdef __MTK_TARGET__
#define RV_TOOL_TYPE  RV_TOOL_TYPE_ADS
#define RV_TOOL_VERSION RV_TOOL_ADS_1_2
#define RV_OS_TYPE RV_OS_TYPE_NUCLEUS
#define RV_OS_VERSION RV_OS_NUCLEUS_4_4
#define RV_THREADNESS_TYPE  RV_THREADNESS_SINGLE 
#elif defined __MTK_ANDROID__

#define RV_TOOL_TYPE  RV_TOOL_TYPE_GNU
#define RV_TOOL_VERSION RV_TOOL_GNU_3_3
#define RV_OS_TYPE RV_OS_TYPE_LINUX
#define RV_OS_VERSION RV_OS_LINUX_REDHAT_9
#define RV_THREADNESS_TYPE  RV_THREADNESS_MULTI

#else
#define RV_TOOL_TYPE RV_TOOL_TYPE_MSVC
#define RV_TOOL_VERSION RV_TOOL_MSVC_6
#ifdef __VT_SWIP__
#define RV_OS_TYPE RV_OS_TYPE_WINCE//RV_OS_TYPE_WIN32
#define RV_OS_VERSION RV_OS_WINCE_4_0//RV_OS_WIN32_XP
#else
#define RV_OS_TYPE RV_OS_TYPE_WIN32
#define RV_OS_VERSION RV_OS_WIN32_XP
#endif
#define RV_THREADNESS_TYPE  RV_THREADNESS_MULTI
#endif

#define RV_ARCH_ENDIAN RV_ARCH_LITTLE_ENDIAN
#define RV_ARCH_BITS RV_ARCH_BITS_32

#ifdef  __VT_MONA__
#define RV_3G324M_USE_MONA              RV_YES//Should turn on RV_GEF_USE_MONA as well
#define RV_GEF_USE_MONA                 RV_YES
#else
#define RV_3G324M_USE_MONA              RV_NO  //Should turn on RV_GEF_USE_MONA as well
#define RV_GEF_USE_MONA                 RV_NO
#endif
#define RV_H245_LEAN_H223 RV_NO
#define RV_H245_USE_HIGH_AVAILABILITY RV_NO
#define RV_H245_AUTO_CAPS_CHECK_CAP RV_NO
#define RV_3G324M_USE_HIGH_AVAILABILITY RV_NO
#define RV_H223_USE_AL3_RETRANSMISSIONS RV_NO
#define RV_H223_USE_MEMORY_LOCALITY RV_NO
#define VT_T_EVENT_TRIGGER
#define RV_ASN1_LEAN_3G324M RV_NO
/* Assemble global definitions and configuration headers. */
/* Actual configuration parameters are set in rvbuildconfig.h */
/* which is generated by the makefiles (from default.mak). */

/* Pull in definitions required for configuration */
#include "rvinterfacesdefs.h" /* Core interfaces */

#if (RV_OS_TYPE != RV_OS_TYPE_LINUX)
#error os is android
#endif

#include "rvccoreconfig.h"
#include "rvarchconfig.h"
#include "rvosconfig.h"
#include "rvtoolconfig.h"

#if !defined(RV_NOUSRCONFIG)
/* Pull in user (override) definitions */
#include "rvusrconfig.h"
#endif

/* Pull in CFLAGS definitions */
#include "rvcflags.h"

/* Calculated dependencies */
#include "rvdependencies.h"

#define termHaDeleteStandByCall(call)
#define termIsdnCallDial(_call, _calledPartyNumber) RV_ERROR_NOTSUPPORTED
#define termIsdnCallAnswer(_call) RV_ERROR_NOTSUPPORTED
#define termIsdnCallDrop(_call,_unlinkCall) RV_ERROR_NOTSUPPORTED
#define termIsdnCallVerifySend(_call)
#define termIsdnCallAttach(_call) RV_ERROR_NOTSUPPORTED
#define termIsdnCallDetach(_call) RV_ERROR_NOTSUPPORTED
#define termIsdnInit(_term)
#define termIsdnEnd(_term)
#define termIsdnIsInitialized(_term) RV_FALSE
#define termIsdnIsLineBusy(_term) RV_TRUE
#define termIsdnHasActiveCalls(_term) RV_FALSE

#define termSerialInit(_term, _comPort)
#define termSerialEnd(_term)
#define termSerialIsInitialized(_term) RV_ERROR_NOTSUPPORTED
#define termSerialReadyForCall(_term) RV_ERROR_NOTSUPPORTED 
#define termSerialCallDial(_call) RV_ERROR_NOTSUPPORTED
#define termSerialCallAnswer(_call) RV_ERROR_NOTSUPPORTED
#define termSerialCallDrop(_call) RV_ERROR_NOTSUPPORTED

#define termWcdmaInit(_term, _comPort)
#define termWcdmaEnd(_term)
#define termWcdmaIsInitialized(_term) RV_ERROR_NOTSUPPORTED
#define termWcdmaIsLineBusy(_term) !RV_ERROR_NOTSUPPORTED
#define termWcdmaCallDial(_call, _calledPartyNumber) RV_ERROR_NOTSUPPORTED
#define termWcdmaCallAnswer(_call) RV_ERROR_NOTSUPPORTED
#define termWcdmaCallDrop(_call) RV_ERROR_NOTSUPPORTED

/* You need to add _MAUI_SOFTWARE_LA_ to make\3G324M\3G324M.def 
    and re-build so that you can enable custom logging */
#ifdef _MAUI_SOFTWARE_LA_
#define VT_SLA_CustomLogging(a_, b_) SLA_CustomLogging(a_, b_)
#else
#define VT_SLA_CustomLogging(a_, b_)
#endif

#define raConstruct(elemSize, maxNumOfElements,threadSafe,name, logMgr) _raConstruct(elemSize, maxNumOfElements,threadSafe,name, logMgr,__FILE__,__LINE__)
#endif /* RV_SYSCONFIG_H */
