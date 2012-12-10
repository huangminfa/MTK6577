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

#ifndef btcat_h
#define btcat_h

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
//For xlog api
#include <cutils/xlog.h>
#include <cutils/sockets.h>

#include <cutils/properties.h>
#include <sys/system_properties.h>
//For shared memory
#include <sys/mman.h>
#include <semaphore.h>

#include <pthread.h>
#include <sys/select.h>
#include <sys/prctl.h>

#define DEFAULT_LOG_ROTATE_SIZE_KBYTES 16
#define DEFAULT_MAX_ROTATED_LOGS 4

#define MAX_FILE_PATH 256
class BTCat{
public:
	char *shared_memory;
	int shm_id;
	sem_t *sem;
	char log_type;
	char g_outputFileName[MAX_FILE_PATH];
	int g_logRotateSizeKBytes;                   // 0 means "no log rotation"
	int g_maxRotatedLogs; // 0 means "unbounded"
	int g_outFD;
	off_t g_outByteCount;
	/*
	 * Modified by Ji, 2011-6-07
	 * save the path before and after sdcard switch
	 * {
	 */
	char before_switch[MAX_FILE_PATH];
	char filename[MAX_FILE_PATH];
	/*
	 * }
	 * Modified by Ji, 2011-6-07
	 */

	BTCat();
	virtual ~BTCat() {

	}
	int openLogFile (const char *pathname);
	void rotateLogs();
	void readLogLines(int fd);
	void setupOutput();
	int doBTCat(const char *before_s, const char *fname, int fileSize, int shm_id);
	int buffer_write(int fd, const char *buffer, int len);
	int buffer_flush(int fd);
	void BTCat_exit(int ret);
	
	
private:	
	int mBTSockListenId;


	bool flushlogbuf;
};



#endif


