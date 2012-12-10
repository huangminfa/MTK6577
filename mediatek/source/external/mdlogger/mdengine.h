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
 *   mdengine.h
 *
 * Project:
 * --------
 *   YUSU
 *
 * Description:
 * ------------
 *   Header file of mdengine
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
 * 09 29 2012 bo.shang
 * [ALPS00366041] [MK5][W1226] òž×CALPS00359238(For_FIHTPE77_CU_ICS2_ALPS.ICS2.MP.V1_P40)ÈÔŸo·¨¸Ä×ƒLOG Path
 * .
 *
 * 07 17 2012 bo.shang
 * [ALPS00316184] [MT6517][Gemini][Rose][Free Test][KE][KERNEL-PANIC]The device reboot and the KE pops up.(5/5)
 * .
 *
 * 06 14 2012 bo.shang
 * [ALPS00270150] [URGENT]Init service faults
 * .
 *
 * 05 03 2012 bo.shang
 * [ALPS00270645] [Symbio][GB2-TDD][LCA42][settings][ANR]ANR happened after waking up phone
 * add more log.
 *
 * 02 20 2012 bo.shang
 * [ALPS00234268] [Rose][ICS][eMMC][Free test][Modem log]The ANR about mediatek.mdlogger pops up after user taps the start button of modemlog.(once)
 * .
 *
 * 02 17 2012 bo.shang
 * [ALPS00238047] [modem log tool new feature]Add EE type information into Modem log dump file name
 * .
 *
 * 02 11 2012 bo.shang
 * [ALPS00234569] [Athens15V1]Way to Change log path from emmc to sdcard in network tool
 * .
 *
 *
 *******************************************************************************/
 
#ifndef __MDL_MD_ENGINE_H__
#define __MDL_MD_ENGINE_H__

#include <pthread.h>
#include <semaphore.h>
#include <sys/ioctl.h>
#include <sys/types.h>

#include "sharedmem.h"
#include "mdltypes.h"
#include "globeconfig.h"

#define CCCI_IOC_MAGIC 'C'
#define CCCI_IOC_ALLOC_MD_LOG_MEM _IO(CCCI_IOC_MAGIC, 5)

namespace mdlogger
{
	typedef struct
	{
		unsigned int header;		// data[0]
		unsigned int cmdId;			// data[1]
		unsigned int channel;		// channel
		unsigned int para;			// reserved
	} CCCI_BUFF_T;

	class MDCommHandler
	{
	public:
		MDCommHandler(){};
		virtual ~MDCommHandler(){};
		virtual MDL_BOOL onReceiveLogData(const char *data, unsigned int len, unsigned char bufId) = 0;
		virtual MDL_BOOL onStartMemeryDumpOnBuf(unsigned char bufId) = 0;
		virtual MDL_BOOL onStopMemoryDumpOnBuf(unsigned char bufId) = 0;
		virtual MDL_BOOL onStartPolling() = 0;
		virtual MDL_BOOL onStopPolling() = 0;
		virtual MDL_BOOL onStartLogging() = 0;
		virtual MDL_BOOL onPauseLogging() = 0;
		virtual MDL_BOOL onResumeLogging() = 0;
        virtual MDL_BOOL checkAndNotifyFileExist() = 0;
	};

	class MDCommEngine
	{
	private:
		static const char *CCCI_MD_LOG_RX;
		static const char *CCCI_MD_LOG_TX;

		// M2A, MD asks AP to create the share mem
		static const unsigned int CCCI_M2A_SHARED_MEM_CREATE_REQ	= 0x00000001;
		// A2D, AP reports the shared mem addr to MD
		static const unsigned int CCCI_A2M_SHARED_MEM_ADDR_REPORT   = 0x00000002;
		// M2A, MD informs that the shared mem info is completed
		static const unsigned int CCCI_M2A_READY_FOR_LOGGING      	= 0x00000003;
		// M2A, MD informs it enters the exception mode
		static const unsigned int CCCI_M2A_EXCEPTION_MODE_REPORT    = 0x00000004;
		// M2A, MD asks AP to move logs
		static const unsigned int CCCI_M2A_TRIGGER_LOG_MOVE         = 0x00000005;
		// A2M, AP asks MD to move cmd
		static const unsigned int CCCI_A2M_TRIGGER_CMD_MOVE         = 0x00000006;
		// M2A, MD asks AP to move next cmd
		static const unsigned int CCCI_M2A_NEXT_CMD_REQ             = 0x00000007;
		// A2M, AP tells MD the current logging mode is USB
		static const unsigned int CCCI_A2M_DUMP_TO_USB              = 0x00000008;
		// A2M, AP tells MD the current logging mode is log2File
		static const unsigned int CCCI_A2M_DUMP_TO_FILE             = 0x00000009;
		// A2M, AP tells MD it's ready for logging
		static const unsigned int CCCI_A2M_START_LOGGING          	= 0x0000000A;
		// M2A, MD tells AP to pass logging filter
		static const unsigned int CCCI_M2A_REQUEST_LOGGING_FILTER   = 0x0000000B;
		// A2M, AP asks MD to switch to AP logging mode
		static const unsigned int CCCI_A2M_SWITCH_MD_LOGGING_MODE	= 0x0000000C;
		// A2M, AP asks MD to perform some UI operations
		static const unsigned int CCCI_A2M_UT_CMD					= 0x0000000D;
		// A2M, AP asks to enable memory dump
		static const unsigned int CCCI_MSG_ID_TST_MEMORY_DUMP_ENABLE_REQ = 0x0000000E;
		// A2M, AP asks to disable memory dump
		static const unsigned int CCCI_MSG_ID_TST_MEMORY_DUMP_DISABLE_REQ = 0x0000000F;
		// M2A, MD tells AP that success to handshake
		static const unsigned int CCCI_MSG_ID_TST_HANDSHAKE_DONE_IDX = 0x000000013;

		typedef enum {
			MD_EX_TYPE_INVALID = 0,
		    MD_EX_TYPE_UNDEF = 1,
		    MD_EX_TYPE_SWI = 2,
		    MD_EX_TYPE_PREF_ABT = 3,
		    MD_EX_TYPE_DATA_ABT = 4,
		    MD_EX_TYPE_ASSERT = 5,
		    MD_EX_TYPE_FATALERR_TASK = 6,
		    MD_EX_TYPE_FATALERR_BUF = 7,
		    MD_EX_TYPE_LOCKUP = 8,
		    MD_EX_TYPE_ASSERT_DUMP = 9,
		    MD_EX_TYPE_ASSERT_FAIL = 10,
		    DSP_EX_TYPE_ASSERT = 11,
		    DSP_EX_TYPE_EXCEPTION = 12,
		    DSP_EX_FATAL_ERROR = 13,
		    NUM_EXCEPTION
		}EE_TYPE_NUM;
		//bin file content layout
		// header (4 bytes)
		// version (4 bytes)
		// l1 data length(4 bytes)
		// l1 data ( (l1_n+3)/4*4 bytes )
		// ps module length (4 bytes)
		// ps module data ( ps_m_n bytes )
		// ps trace length (4 bytes)
		// ps trace data ( ps_t_n bytes )
		// ps sap length (4 bytes)
		// ps sap data ( ps_s_n bytes )
		static const unsigned int BIN_FILE_HEADER = 0x2454ABCD;
		static const unsigned int BIN_FILE_VERSION = 0x00000001;

		static const char AP_NORMAL_MODE = 0;
		static const char AP_PAUSE_MODE = 3;
        char sdRoot[64];
        char catherFilter[MAX_PATH_LEN];
		char catherFilterOld[MAX_PATH_LEN];
        
		int m_nRxFd;
		int m_nTxFd;

		CCCI_BUFF_T m_ccciBuf;
		SharedMem *m_pShm;

		MDL_BOOL m_bTerminate;
		pthread_t m_thrReceiver;
		pthread_t m_thrFilterPasser;
		pthread_t m_thrPoller;
		pthread_t m_thrFilterDetecter;
		MDCommHandler *m_pMDHandler;

		int m_nM2ABufCnt;
		MDL_BOOL *m_pbLogBufOnOff;
		sem_t m_semA2M;
		MDL_BOOL m_bInited;
		MDL_BOOL m_bPaused;
		MDL_BOOL m_bInPolling;
		MDL_BOOL m_bSuccessSendFilter;
		MDL_BOOL *m_pbMemDumpFinished;
		MDL_BOOL m_bEnableMemoryDump;
		int m_currentMode;

		MDL_BOOL m_bThreadDetectFilterRunning;

		MDL_BOOL readCCCI(CCCI_BUFF_T &buf);
        MDL_BOOL readCCCIForMoveLog(CCCI_BUFF_T &buf);
		MDL_BOOL writeCCCI(CCCI_BUFF_T &buf);
		MDL_BOOL moveLog(unsigned char bufId);
		MDL_BOOL moveLog();
		void clearBuffer();
        void initCatherBinPath();
		unsigned int encodeCatcherCmdPacket(unsigned int type, unsigned char* dst_buf, unsigned int len, unsigned char* src_buf);

		friend void *thrMDReceiver(void *arg);
		friend void *thrFilterPasser(void *arg);
		friend void *thrPoller(void *arg);
		friend void *thrDetectFilter(void *arg);
	public:
		static char *CATCHER_FILTER_PATH;
		static char *CATCHER_FILTER_PATH_OLD;
		static const char *CATCHER_FILTER_PATH_PHONE;
		static const char *CATCHER_FILTER_PATH_PHONE_SYSTEM;
		time_t m_filterModifiedTime;
		int m_nCountReadCCCI;
		MDCommEngine();
		~MDCommEngine();
		MDL_BOOL init();
		MDL_BOOL deinit();
		MDL_BOOL start(int mode);
		MDL_BOOL stop();
		MDL_BOOL pause();
		MDL_BOOL resume();
		void getLogFolderPath(char* logFolderPath);
		void registerHandler(MDCommHandler *pMDHandler);
		void unregisterHandler();
        char * getSDCardRootPath();
		MDL_BOOL startLogBuf(unsigned char bufId);
		MDL_BOOL stopLogBuf(unsigned char bufId);
		MDL_BOOL startAllLogBuf();
		MDL_BOOL stopAllLogBuf();
		MDL_BOOL setLogBufOnOff(unsigned char bufId, MDL_BOOL bOnOff);
		MDL_BOOL passCmd(const void *data, unsigned int len, unsigned char bufId);
		MDL_BOOL passFilter();
		int getBufCnt();
		MDL_BOOL isInited();
		MDL_BOOL isPaused();
		MDL_BOOL isFilterExist();
		MDL_BOOL testPollingMode();
		MDL_BOOL enableMemoryDump();
		MDL_BOOL disableMemoryDump();
		MDL_BOOL startPolling();
		MDL_BOOL stopPolling();
		MDL_BOOL isInPolling();
		void setRunningStatus(MDL_BOOL bRunning);
		MDL_BOOL resetModem();
	 	MDL_BOOL renameFileByType(char * szPath, int type);
    int getEEType();
		void setSendFilterFlag(MDL_BOOL bSuccess);
		MDL_BOOL isSuccessSendFilter();

		MDL_BOOL isThreadDetectFilterRunning();
		void startDetectFilter();
		void stopDetectFilter(); 
	};
}

#endif
