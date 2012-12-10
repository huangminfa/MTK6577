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
 *   logrelayer.h
 *
 * Project:
 * --------
 *   YUSU
 *
 * Description:
 * ------------
 *   Header file of logrelayer
 *
 * Author:
 * -------
 *   Siyang.Miao (MTK80734) 03/08/2011
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
 * 02 20 2012 bo.shang
 * [ALPS00234268] [Rose][ICS][eMMC][Free test][Modem log]The ANR about mediatek.mdlogger pops up after user taps the start button of modemlog.(once)
 * .
 *
 *
 *******************************************************************************/
#ifndef __MDL_LOG_RELAYER_H__
#define __MDL_LOG_RELAYER_H__

#include <stdio.h>
#include "mdltypes.h"
#include "pcengine.h"
#include "mdengine.h"

namespace extmdlogger
{
	class LogRelayer : public MDCommHandler
	{
	private:
		MDCommEngine* m_pMDEngine;
		PCCommEngine* m_pPCEngine;
	public:
		LogRelayer(PCCommEngine *pPCEngine = NULL, MDCommEngine *pMDEngine = NULL);
		~LogRelayer();
		MDL_BOOL init();
		MDL_BOOL deinit();
		MDL_BOOL onReceiveLogData(const char *data, unsigned int len, unsigned char bufId);
		MDL_BOOL onStartMemeryDumpOnBuf(unsigned char bufId);
		MDL_BOOL onStopMemoryDumpOnBuf(unsigned char bufId);
		MDL_BOOL onStartLogging();
		MDL_BOOL onPauseLogging();
		MDL_BOOL onResumeLogging();
		MDL_BOOL onStartPolling();
		MDL_BOOL onStopPolling();
        MDL_BOOL checkAndNotifyFileExist();
	};
}

#endif
