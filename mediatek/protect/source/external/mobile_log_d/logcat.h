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

#ifndef logcat_h
#define logcat_h

#include <cutils/logger.h>
#include <cutils/logd.h>
#include <cutils/xlog.h>
#include <cutils/sockets.h>
#include <cutils/logprint.h>
#include <cutils/event_tag_map.h>

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <time.h>
#include <errno.h>
#include <assert.h>
#include <ctype.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <arpa/inet.h>
//For shared memory
#include <sys/mman.h>
#include <semaphore.h>

#include <kmsgcat.h>

#define DEFAULT_LOG_ROTATE_SIZE_KBYTES 16
#define DEFAULT_MAX_ROTATED_LOGS 4
/* logd prefixes records with a length field */
#define RECORD_LENGTH_FIELD_SIZE_BYTES sizeof(uint32_t)
#define LOG_FILE_DIR    "/dev/log/"

#define MAX_FILE_PATH 256

struct queued_entry_t {
	union {
		unsigned char buf[LOGGER_ENTRY_MAX_LEN + 1] __attribute__((aligned(4)));
		struct logger_entry entry __attribute__((aligned(4)));
	};
	queued_entry_t* next;

	queued_entry_t() {
		next = NULL;
	}
};

static int cmp(queued_entry_t* a, queued_entry_t* b) {
	int n = a->entry.sec - b->entry.sec;
	if (n != 0) {
		return n;
	}
	return a->entry.nsec - b->entry.nsec;
}

struct log_device_t {
	char* device;
	bool binary;
	int fd;
	bool printed;
	char label;

	queued_entry_t* queue;
	log_device_t* next;

	log_device_t(char* d, bool b, char l) {
		device = d;
		binary = b;
		label = l;
		queue = NULL;
		next = NULL;
		printed = false;
	}

	void enqueue(queued_entry_t* entry) {
		if (this->queue == NULL) {
			this->queue = entry;
		} else {
			queued_entry_t** e = &this->queue;
			while (*e && cmp(entry, *e) >= 0) {
				e = &((*e)->next);
			}
			entry->next = *e;
			*e = entry;
		}
	}
};

class LogCat{

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
	char filename[MAX_FILE_PATH];
	/*
	 * Modified by Ji, 2011-6-07
	 * save the path before and after sdcard switch
	 * {
	 */
	char before_switch[MAX_FILE_PATH];
	char after_switch[MAX_FILE_PATH];
	/*
	 * }
	 * Modified by Ji, 2011-6-07
	 */
	LogCat();
	virtual ~LogCat() {

	}
	int openLogFile (const char *pathname);
	void rotateLogs();
	void printBinary(struct logger_entry *buf);
	void processBuffer(log_device_t* dev, struct logger_entry *buf);
	void chooseFirst(log_device_t* dev, log_device_t** firstdev);
	void maybePrintStart(log_device_t* dev);
	void skipNextEntry(log_device_t* dev);
	void printNextEntry(log_device_t* dev);
	int setLogFormat(const char * formatString);
	void readLogLines(log_device_t* devices);
	void setupOutput();
	int doLogCat(const char *before_s, const char *fname, int fileSize, const char* bufIndex, int shm_id);
	int android_log_buf_printLogLine(AndroidLogFormat *p_format, int fd, const AndroidLogEntry *entry);
	void LogCat_exit(int ret);


private:
	AndroidLogFormat * g_logformat;
	bool g_nonblock;
	int g_tail_lines;


	int g_printBinary;
	int g_devCount;



	EventTagMap* g_eventTagMap;

	bool flushlogbuf;

};

#endif





