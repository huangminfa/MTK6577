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

#ifndef ml_connection_h
#define ml_connection_h

#define LOG_TAG "MobileLogD"

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/statfs.h>
#include <dirent.h>
#include <errno.h>
#include <sys/time.h>
#include <assert.h>
#include <time.h>
#include <stdarg.h>

#include <ctype.h>
#include <sys/types.h>
#include <sys/wait.h> 
#include <sys/un.h>

#include <cutils/log.h>
#include <cutils/xlog.h>
#include <cutils/sockets.h>

#include <cutils/properties.h>
#include <sys/system_properties.h>

#include <pthread.h>
#include <sys/select.h>
#include <sys/prctl.h>
//For shared memory
#include <sys/stat.h>
#include <sys/mman.h>
#include <semaphore.h>
#include <linux/shm.h>
#include <cutils/ashmem.h>
//For timeout
#include <linux/time.h>

#include "kmsgcat.h"
#include "logcat.h"
#include "btcat.h"
#include "debug_config.h"

#define PORT_NUM 5677
#define MAX_FILE_PATH 256
#define MAX_FILE_SIZE 20

typedef struct __CLIENT{
	int fd;
	struct sockaddr addr;
} CLIENT;


class MlConnection{

public:
	char *shared_memory;
	int shm_id;
	sem_t *sem;
	bool bootsuccess;
	bool shouldtimeout;
	timeval timeout;
	bool isMobileLogRun;
	char mFilePath[MAX_FILE_PATH];
	MlConnection();
	virtual ~MlConnection() {

	}

	void startListening();
	static void *threadStart(void *obj);
	void runListener();
	bool handleMessage(int fd);
int kernelLog();
int mainLog();
int radioLog();
int eventsLog();
int BTLog();
int information();

void checkPid(int fd);
void parseStartCmd(const char *buf);
void stop();
int createDir(const   char   *sPathName);
void readConfig();
void copyToSdcard(const char *srcpath, const char *despath, const char *postfix);
void copyFiles(const char *srcfilename, const char* desfilename);
int dumpBootProf();
int dumpVersion();
int dumpALEdb();
int dumpLastKmsg();

void rmTempDir();
void rmTempFile(const char *filename);
void redirect(const char *filename);
int AndroidBootFail(MlConnection *mc);
int MountSDcard(void);
int Select_sdcard(char *sdpath);
void StartLogProcess(void);
int getLeftSpace(const char *mode);

void mblogStatus(char *flag); 
private:
	int mSockListenId;
	
	pthread_t mThread;
	pthread_mutex_t mlock;
	fd_set read_fds;
	
	pid_t mKernelLogPid;
	pid_t mMainLogPid;
	pid_t mRadioLogPid;
	pid_t mEventsLogPid;
	pid_t mBTLogPid;

	
	char mFileSize[MAX_FILE_SIZE];
	int int_mFileSize;

	CLIENT				client[10];
};
static void getSysProp(const char *key, const char *name, void *cookies);

#endif


