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

#ifndef OSCLCONFIG_LIB_CHECK_H_INCLUDED
#define OSCLCONFIG_LIB_CHECK_H_INCLUDED

/*! \addtogroup osclconfig OSCL config
 *
 * @{
 */



/**
OSCL_HAS_RUNTIME_LIB_LOADING_SUPPORT should be set to 1 if the platform has basic explicit runtime DLL loading support.
*/
#if !defined(OSCL_HAS_RUNTIME_LIB_LOADING_SUPPORT )
#error "ERROR: OSCL_HAS_RUNTIME_LIB_LOADING_SUPPORT must be defined to 0 or 1"
#endif

#if(OSCL_HAS_RUNTIME_LIB_LOADING_SUPPORT)
/**
** When OSCL_HAS_RUNTIME_LIB_LOADING_SUPPORT is 1,
** OSCL_LIB_READ_DEBUG_LIBS should be set to 0 or 1.  Set to 1 to enable loading
** debug versions of libs.
*/
#if !defined(OSCL_LIB_READ_DEBUG_LIBS)
#error "ERROR: OSCL_LIB_READ_DEBUG_LIBS must be defined to 0 or 1"
#endif

/*
** When OSCL_HAS_RUNTIME_LIB_LOADING_SUPPORT is 1,
** PV_DYNAMIC_LOADING_CONFIG_FILE_PATH should be set.
*/
#if !defined(PV_DYNAMIC_LOADING_CONFIG_FILE_PATH)
#error "ERROR: PV_DYNAMIC_LOADING_CONFIG_FILE_PATH must be set to a path where the config files are expected to be present"
#endif

/*
** When OSCL_HAS_RUNTIME_LIB_LOADING_SUPPORT is 1,
** PV_RUNTIME_LIB_FILENAME_EXTENSION should be set.
*/
#if !defined(PV_RUNTIME_LIB_FILENAME_EXTENSION)
#error "ERROR: PV_RUNTIME_LIB_FILENAME_EXTENSION must be specified for use as the dynamic library file extension"
#endif

#endif // OSCL_HAS_RUNTIME_LIB_LOADING_SUPPORT


/*! @} */

#endif // OSCLCONFIG_LIB_CHECK_H_INCLUDED


