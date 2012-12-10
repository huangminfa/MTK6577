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

#ifndef OSCLCONFIG_NO_OS_H_INCLUDED
#define OSCLCONFIG_NO_OS_H_INCLUDED

/*! \addtogroup osclconfig OSCL config
 *
 * @{
 */

//a file to turn off ALL os-specific switches.

//osclconfig
#define OSCL_HAS_UNIX_SUPPORT               0
#define OSCL_HAS_MSWIN_SUPPORT              0
#define OSCL_HAS_MSWIN_PARTIAL_SUPPORT      0
#define OSCL_HAS_SYMBIAN_SUPPORT            0
#define OSCL_HAS_SAVAJE_SUPPORT             0
#define OSCL_HAS_PV_C_OS_SUPPORT            0

//osclconfig_error
#define OSCL_HAS_SYMBIAN_ERRORTRAP 0

//osclconfig_memory
#define OSCL_HAS_SYMBIAN_MEMORY_FUNCS 0
#define OSCL_HAS_PV_C_OS_API_MEMORY_FUNCS 0

//osclconfig_time
#define OSCL_HAS_PV_C_OS_TIME_FUNCS 0
#define OSCL_HAS_UNIX_TIME_FUNCS    0

//osclconfig_util
#define OSCL_HAS_SYMBIAN_TIMERS 0
#define OSCL_HAS_SYMBIAN_MATH   0

//osclconfig_proc
#define OSCL_HAS_SYMBIAN_SCHEDULER 0
#define OSCL_HAS_SEM_TIMEDWAIT_SUPPORT 0
#define OSCL_HAS_PTHREAD_SUPPORT 0

//osclconfig_io
#define OSCL_HAS_SYMBIAN_COMPATIBLE_IO_FUNCTION 0
#define OSCL_HAS_SAVAJE_IO_SUPPORT 0
#define OSCL_HAS_SYMBIAN_SOCKET_SERVER 0
#define OSCL_HAS_SYMBIAN_DNS_SERVER 0
#define OSCL_HAS_BERKELEY_SOCKETS 0


/*! @} */

#endif // OSCLCONFIG_CHECK_H_INCLUDED


