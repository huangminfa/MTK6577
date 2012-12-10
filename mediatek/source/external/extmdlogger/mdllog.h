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

/*******************************************************************************
 *
 * Filename:
 * ---------
 *   mdllog.h
 *
 * Project:
 * --------
 *   ALPS
 *
 * Description:
 * ------------
 *   Header file of MDLogger logging
 *
 * Author:
 * -------
 *   Siyang.Miao (MTK80734) 12/30/2010
 *
 *------------------------------------------------------------------------------
 * $Revision:$
 * $Modtime:$
 * $Log:$
 *
 * 05 02 2012 bo.shang
 * [ALPS00277288] [Need Patch] [Volunteer Patch]ExtModemLog Feature check in
 * .
 *
 *
 *******************************************************************************/
 
#ifndef __MDL_LOG_H__
#define __MDL_LOG_H__

#include <utils/Log.h>	//Logcat
#include <cutils/xlog.h>
#include "mdltypes.h"
#undef LOG_TAG
#define LOG_TAG "EXTMDLOGGER"

// Log enable/disable
#define LOG_ENABLE 1
#define V_ENABLE 1
#define D_ENABLE 1
#define I_ENABLE 1
#define W_ENABLE 1
#define E_ENABLE 1

#define DUMP_ROW_LEN 16

#if LOG_ENABLE && V_ENABLE
	#define MDL_LOGV(...) XLOGV(__VA_ARGS__);
#else
	#define MDL_LOGV(...) LOGV(__VA_ARGS__);
#endif

#if LOG_ENABLE && D_ENABLE
	#define MDL_LOGD(...) XLOGD(__VA_ARGS__);
#else
	#define MDL_LOGD(...) LOGD(__VA_ARGS__);
#endif

#if LOG_ENABLE && I_ENABLE
	#define MDL_LOGI(...) XLOGI(__VA_ARGS__);
#else
	#define MDL_LOGI(...) LOGI(__VA_ARGS__);
#endif

#if LOG_ENABLE && W_ENABLE
	#define MDL_LOGW(...) XLOGW(__VA_ARGS__);
#else
	#define MDL_LOGW(...) LOGW(__VA_ARGS__);
#endif

#if LOG_ENABLE && E_ENABLE
	#define MDL_LOGE(...) XLOGE(__VA_ARGS__);
#else
	#define MDL_LOGE(...) LOGE(__VA_ARGS__);
#endif

static MDL_BOOL g_bDumpEnabled = MDL_FALSE;

void dumpHex(const char* con, unsigned int length, unsigned int bytesPerRow);
void dumpHex(const char* con, unsigned int length);
void testPacketTrans(const unsigned char* con, unsigned int length);

#endif
