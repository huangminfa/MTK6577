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
 *   extmdlogger_ctrl.h
 *
 * Project:
 * --------
 *   YUSU
 *
 * Description:
 * ------------
 *   Header file of extmdlogger_ctrl
 *
 * Author:
 * -------
 *   Bo Shang (MTK80204) 3/27/2012
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
#ifndef __MDLOGGER_CTRL_H__
#define __MDLOGGER_CTRL_H__

#include <limits.h>
#include "mdltypes.h"

#undef LOG_TAG
#define LOG_TAG "EXTMDLOGGER_CTRL"
#include <utils/Log.h>	//Logcat

#define MDL_DATA_DIR 	"/data/extmdl/"
#define SERVER_FIFO		"/data/extmdl/extmdl_serv_fifo"
#define CLIENT_FIFO		"/data/extmdl/extmdl_cli_fifo"
#define FIFO_BUF_SIZE 	PIPE_BUF

typedef enum
{
	MODE_UNKNOWN = -1,	// -1
	MODE_IDLE,			// 0
	MODE_USB,			// 1
	MODE_SD,			// 2
	MODE_POLLING,		// 3
	MODE_WAITSD,		// 4
}LOGGING_MODE;

typedef enum
{
	MDL_OP_RESERVED = 0,				// 0
	MDL_OP_GET_CURRENT_LOGGING_MODE,	// 1
	MDL_OP_GET_AUTOSTART_LOGGING_MODE,	// 2
	MDL_OP_SET_AUTOSTART_LOGGING_MODE,	// 3
	MDL_OP_START_LOGGING,				// 4
	MDL_OP_STOP_LOGGING,				// 5
	MDL_OP_RESUME_LOGGING,				// 6
	MDL_OP_PAUSE_LOGGING,				// 7
	MDL_OP_IS_LOGGING_PAUSED,			// 8
	// Only used to test polling mode
	MDL_OP_TEST_POLLING_MODE,			// 9
	// Enable/disable mdlogger's hex dump log
	MDL_OP_ENABLE_DUMP,					// 10
	MDL_OP_DISABLE_DUMP,				// 11
	MDL_OP_STOP_WAITINGSD,
	MDL_OP_ENABLE_MEMORYDUMP,
	MDL_OP_DISABLE_MEMORYDUMP,
	MDL_OP_RESET_MODEM,
	MDL_OP_IS_MEMORYDUMP_DONE,
    MDL_OP_SET_SIZE_LIMIT,
	MDL_OP_END
}MDL_OP;

typedef struct
{
	int 	client_pid;
	MDL_OP	op;
}MDL_REQ_H;

typedef union
{
	int	logging_mode;
	MDL_BOOL result;
}MDL_REQ_BODY;

typedef struct
{
	MDL_REQ_H 		header;
	MDL_REQ_BODY 	body;
}MDL_REQ;

typedef struct
{
	MDL_OP	op;
}MDL_RSP_H;

typedef union
{
	int	logging_mode;
	MDL_BOOL result;
}MDL_RSP_BODY;

typedef struct
{
	MDL_RSP_H 		header;
	MDL_RSP_BODY 	body;
}MDL_RSP;

int getCurrentLoggingMode(void);
int getAutoStartLoggingMode(void);
int setAutoStartLoggingMode(int mode);
MDL_BOOL startLogging(int mode);
MDL_BOOL stopLogging(void);
MDL_BOOL resumeLogging(void);
MDL_BOOL pauseLogging(void);
MDL_BOOL isLoggingPaused(void);
MDL_BOOL testPollingMode(void);
// This "dump" does not indicate modem memory dump but mdlogger's hex dump
MDL_BOOL enableDump(void);
MDL_BOOL disableDump(void);
MDL_BOOL enableMemoryDump(void);
MDL_BOOL disableMemoryDump(void);
MDL_BOOL stopWaitingSD(void);
void resetModem(void);
MDL_BOOL isMemoryDumpDone(void);
MDL_BOOL setSDcardRingBufferLimit(int nSize);
#endif
