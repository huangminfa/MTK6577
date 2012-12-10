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
 *   logwriter.h
 *
 * Project:
 * --------
 *   YUSU
 *
 * Description:
 * ------------
 *   Header file of logwriter
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
 * 09 29 2012 bo.shang
 * [ALPS00366041] [MK5][W1226] òž×CALPS00359238(For_FIHTPE77_CU_ICS2_ALPS.ICS2.MP.V1_P40)ÈÔŸo·¨¸Ä×ƒLOG Path
 * .
 * 
 * 09 23 2012 bo.shang
 * [ALPS00269605] [MP Feature Patch Back]Shared sdcard feature support
 * .
 *
 * 05 14 2012 bo.shang
 * [ALPS00281494] [modem log tool][daily use]limit log size and set as default setting.
 * .
 *
 * 04 16 2012 bo.shang
 * [ALPS00269389] [Need Patch] [Volunteer Patch]Copy modem db to mdlog
 * .
 *
 * 02 20 2012 bo.shang
 * [ALPS00234268] [Rose][ICS][eMMC][Free test][Modem log]The ANR about mediatek.mdlogger pops up after user taps the start button of modemlog.(once)
 * .
 *
 * 02 11 2012 bo.shang
 * [ALPS00234569] [Athens15V1]Way to Change log path from emmc to sdcard in network tool
 * .
 *
 *
 *******************************************************************************/
#ifndef __MDL_LOG_WRITER_H__
#define __MDL_LOG_WRITER_H__

#include <stdio.h>
#include <pthread.h>
#include "mdltypes.h"
#include "pcengine.h"
#include "mdengine.h"

namespace mdlogger
{
	class BaseFile
	{
	protected:
		static const int MAX_NUMBER_FAILED = 3;
		FILE *m_pFile;				//File pointer
		long m_lSize;				//File size
	public:
		static const int PATH_LEN = 256;
		int m_nBufId;				//Buffer ID
		char m_szPath[PATH_LEN];	//File path
		int m_nFailedNumber;
		BaseFile(int nBufId = 0);
		virtual ~BaseFile(){};
		long closedSize();
		long size();
		virtual MDL_BOOL open(const char *path);
		virtual MDL_BOOL close();
		virtual MDL_BOOL cleanClose(); // Close and check if file is empty, if true, remove it
		virtual MDL_BOOL write(const void *ptr, size_t size, size_t nobj);
		virtual unsigned int write(const char *ptr, unsigned int len);
		virtual MDL_BOOL write(const char *str);
		virtual MDL_BOOL flush();
		virtual long capacity();
		virtual MDL_BOOL isExceeded();
		virtual MDL_BOOL isExist();
	};

	class LogFile : public BaseFile
	{
	private:
		static const long FILE_SIZE_LIMIT = 52428800;	//max size of .dmp file is 50MBytes
		static const int FILE_HEADER_LEN = 4;
		static const char FILE_HEADER[FILE_HEADER_LEN];
	public:
		LogFile(int nBufId = 0);
		~LogFile(){};
		MDL_BOOL open(const char *path);
		MDL_BOOL cleanClose();
		long capacity();
		MDL_BOOL isExceeded();
	};

	class MemDumpFile : public BaseFile
	{
	public:
		MemDumpFile(int nBufId = 0);
		~MemDumpFile(){};
		MDL_BOOL open(const char *path);
		MDL_BOOL cleanClose();
		long capacity();
		MDL_BOOL isExceeded();
	};

	typedef enum
	{
		RET_SDREADY = 0,	// 0
		ERR_GETSDSYSTEM,	// 1
		ERR_SDNOTREADY,		// 2
		ERR_SDFULL,			// 3
		RET_STOPCHECK,		// 4
	}RETURN_NUM;

	class LogWriter : public MDCommHandler
	{
	private:
		static const int MAX_BUFFER_COUNT = 6;
		static char *SD_FILE_SYSTEM;
		static char *SD_LOG_ROOT;
		static const char *NAND_LOG_ROOT;
		static const char *NAND_LOG_FOLDER;
		static char *SD_LOG_CONFIG;
		static const char *FOLDER_NAME;
		static const char *LOG_FILE_NAME;
		static const char *MEMDUMP_FILE_NAME;
		static const char *VERSION_FILE_NAME;
		static const char *VERSION_CONTENT;
		static const char *FILE_TREE_NAME;
		static const unsigned long SD_RESERVED_SPACE = 10*1048576;
		static const char* bufNameArray[MAX_BUFFER_COUNT];
		int m_nBufCnt;
		unsigned int m_nIOBlkSize;
		unsigned long m_nReservedSpace;
		char m_szTime[17];
		char m_szFolderPath[BaseFile::PATH_LEN];

		BaseFile **m_ppFiles;
		BaseFile **m_ppNandFiles;
		BaseFile **m_ppSDFiles;

		MDL_BOOL *m_pbMemDump;
		MDL_BOOL m_bDiskAvailable;
		MDL_BOOL m_bInformNoLogFile;
		MDL_BOOL m_bPaused;
		MDL_BOOL m_bLog2SD;
		MDL_BOOL m_bWritingLog[MAX_BUFFER_COUNT];
		MDL_BOOL m_bHasMemoryDump;
		MDCommEngine* m_pMDEngine;
		pthread_t m_thrWaitingSD;
    pthread_t m_thrCopyMDToSD;
		MDL_BOOL m_bCheckingSD;
        MDL_BOOL m_bBootup;
        MDL_BOOL m_bFolderFirstCreate;

		static MDL_BOOL getCurrentTimeStr(char *out, int *pLen);
		static void intToDateStr(char *out, int in);
		void clearOldLogFiles();
		MDL_BOOL createLogFolder(const char* logRoot);
		MDL_BOOL createLogFiles(BaseFile** &ppFiles);
		MDL_BOOL createLogFile(BaseFile* &pFile, int nBufId);
		MDL_BOOL closeLogFiles(BaseFile** &ppFiles);
		MDL_BOOL closeLogFile(BaseFile* &pFile);
		MDL_BOOL generateVersionFile();
		MDL_BOOL writeLogFile(BaseFile* &pFile, const char *ptr, unsigned int len);
		MDL_BOOL saveLogToNand(const char *data, unsigned int len, unsigned char bufId);
		MDL_BOOL saveLogToSD(const char *data, unsigned int len, unsigned char bufId);
		friend void *thrWaitingSDReady(void *arg);

        char logRoot[MAX_PATH_LEN];
		char logConfig[MAX_PATH_LEN];
		char sdRoot[64];
        char topTreeFile[MAX_PATH_LEN];

	public:
		LogWriter(MDCommEngine *pMDEngine = NULL);
		~LogWriter();
		unsigned long getSDLeftSpace();
		unsigned long getNandLeftSpace();
		static MDL_BOOL isSDAvailable();
		MDL_BOOL init(int nBufCnt);
		MDL_BOOL deinit();
		MDL_BOOL onReceiveLogData(const char *data, unsigned int len, unsigned char bufId);
		MDL_BOOL onStartMemeryDumpOnBuf(unsigned char bufId);
		MDL_BOOL onStopMemoryDumpOnBuf(unsigned char bufId);
		MDL_BOOL onStartLogging();
		MDL_BOOL onPauseLogging();
		MDL_BOOL onResumeLogging();
		MDL_BOOL onStartPolling();
		MDL_BOOL onStopPolling();

		MDL_BOOL flushFile(int nBufId);
		MDL_BOOL flushAllFiles(void);
		void getLogFolder(char* folderPath);
        void initSDCardPath();
        void moveMDDBToSD();
		MDL_BOOL isWritingLog();

		MDL_BOOL createLogFilesOnSD();
		MDL_BOOL createLogFilesOnNand();
		MDL_BOOL moveBootupLog();
		MDL_BOOL pauseLogging();

		int startCheckSD(int timeout);
		void stopCheckSD();
		int checkSDStatus(int retryNumber);

		void notifySDFull();
		void notifyNoSDCard();
		void notifyFileNotExist();
        MDL_BOOL checkAndNotifyFileExist();
        MDL_BOOL generateLogTreeFile(const char *data, int len, MDL_BOOL mBFolderTree);
        char *getLogFolder();
		MDL_BOOL isLog2SD();
		void setLog2SD(MDL_BOOL bLog2SD);
	};
}

#endif
