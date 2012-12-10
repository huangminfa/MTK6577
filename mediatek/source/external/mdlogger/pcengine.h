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
 *   pcengine.h
 *
 * Project:
 * --------
 *   YUSU
 *
 * Description:
 * ------------
 *   Header file of pcengine
 *
 * Author:
 * -------
 *   Siyang.Miao (MTK80734) 12/24/2010
 *
 *------------------------------------------------------------------------------
 * $Revision:$
 * $Modtime:$
 * $Log:$
 *
 *
 *******************************************************************************/
 
#ifndef __MDL_PC_ENGINE_H__
#define __MDL_PC_ENGINE_H__

#include <pthread.h>
#include <semaphore.h>
#include "mdltypes.h"

namespace mdlogger
{
	typedef enum
	{
		AP_CMD_BEGIN = 0,
		AP_CMD_CONN = AP_CMD_BEGIN,		// 0
		AP_CMD_DISC,					// 1
		AP_CMD_START_LOG_BUF,			// 2
		AP_CMD_STOP_LOG_BUF,			// 3
//		AP_CMD_CHECK_CONNECTION,		// 4
		AP_CMD_ACK = 255,				// 255
		AP_CMD_END = AP_CMD_ACK
	}AP_CMD;

	class PCCommHandler
	{
	public:
		PCCommHandler(){};
		virtual ~PCCommHandler(){};
		virtual MDL_BOOL onReceiveAPPacket(const void *data, unsigned int len,
				unsigned char cmdId) = 0;
		virtual MDL_BOOL onReceiveMDPacket(const void *data, unsigned int len,
				unsigned char bufId) = 0;
	};

	class PCCommEngine
	{
	private:
		static const int SERVER_PORT = 30017;
		static const int BACKLOG = 1;
		static const unsigned int RX_SIZE = 65535;
		static const unsigned int TX_SIZE = 65535;
		static const unsigned int MD_PACK_MAX_LEN = 65535;
		static const unsigned int HEADER_LEN = 8;
		static const unsigned int MD_DATA_MAX_LEN = MD_PACK_MAX_LEN - HEADER_LEN;
		static const unsigned int AP_PACK_MAX_LEN = 255;
		static const unsigned int AP_DATA_MAX_LEN = AP_PACK_MAX_LEN - HEADER_LEN;

		static const unsigned int TAG_LEN = 4;
		static const char TAG_AP[TAG_LEN];
		static const char TAG_MD[TAG_LEN];

		int m_nSockFd;
		int m_nClientFd;
		MDL_BOOL m_bTerminate;
		pthread_t m_thrReceiver;
		PCCommHandler *m_pPCHandler;
		char m_cRxBuf[RX_SIZE];
		char m_cTxBuf[TX_SIZE];
		sem_t m_semSend;
		MDL_BOOL init();
		MDL_BOOL connect();
		MDL_BOOL deinit();

		friend void *thrPCReceiver(void *arg);
	public:
		PCCommEngine();
		~PCCommEngine();
		MDL_BOOL start();
		MDL_BOOL stop();
		MDL_BOOL receive();
		MDL_BOOL sendAPPacket(const char *data, unsigned int len, unsigned char cmdId);
		MDL_BOOL sendMDPacket(const char *data, unsigned int len, unsigned char bufId);
		MDL_BOOL disconnect();
		MDL_BOOL forceDisconnect();
		MDL_BOOL isConnected();
		void registerHandler(PCCommHandler *pPCHandler);
		void unregisterHandler();
	};
}

#endif
