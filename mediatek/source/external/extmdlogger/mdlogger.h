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
 *   mdlogger.h
 *
 * Project:
 * --------
 *   YUSU
 *
 * Description:
 * ------------
 *   Header file of mdlogger
 *
 * Author:
 * -------
 *   Siyang.Miao (MTK80734) 12/14/2010
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
 
#ifndef __MDL_MAIN_H__
#define __MDL_MAIN_H__

#include <semaphore.h>
#include <pthread.h>
#include "mdltypes.h"
#include "pcengine.h"
#include "mdengine.h"
#include "cmdreader.h"
#include "logwriter.h"
#include "logrelayer.h"
#include "extmdlogger_ctrl.h"
#include "mdllog.h"

using namespace extmdlogger;

#define MDLOGGER_CONFIG_FILE "/data/extmdl/extmdl_config"

//Global variables
static int g_loggingMode;
//static sem_t g_semLoggingMode;

static PCCommEngine *g_pPCEngine = NULL;
static MDCommEngine *g_pMDEngine = NULL;
static CmdReader *g_pCmdReader = NULL;
static LogWriter *g_pLogWriter = NULL;
static LogRelayer *g_pLogRelayer = NULL;
static MDL_BOOL g_bMDInited;
static MDL_BOOL g_bMDStarting;
static pthread_t readComThreadTid;
static pthread_t threadStartLogging;
static int g_selectMode;
static MDL_BOOL g_bStartResult;
static MDL_BOOL g_bStartReadComThrad = MDL_FALSE;
static MDL_BOOL g_bAutoStart;
static MDL_BOOL g_bStopMDLogger;
static MDL_BOOL gbStopWaitingSD;
#endif
