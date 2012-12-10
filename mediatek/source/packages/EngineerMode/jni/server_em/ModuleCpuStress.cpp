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

#define LOG_TAG "EMCPUSTRESS"
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include "utils/Log.h"
#include "ModuleCpuStress.h"
#include "RPCClient.h"

void * apmcu_test(void * argvoid) {
	struct thread_params_t * arg = (struct thread_params_t *) argvoid;
	int fd = -1;
	char value[10] = { 0 };
	size_t s = 0;
	do {
		fd = open(arg->file, O_RDWR);
		LOGD("open file: %s", arg->file);
		if (fd < 0) {
			snprintf(arg->result, sizeof(arg->result), "%s",
					"fail to open device");
			LOGE("fail to open device");
			break;
		}
		snprintf(value, sizeof(value), "%d", 1);
		write(fd, value, strlen(value));
		lseek(fd, 0, SEEK_SET);
		s = read(fd, arg->result, sizeof(arg->result));
		if (s <= 0) {
			snprintf(arg->result, sizeof(arg->result), "%s",
					"could not read response");
			break;
		}
	} while (0);
		if (fd >= 0) {
			close(fd);
		}
	pthread_exit(NULL);
	return NULL;
}

void doApMcuTest(int index, RPCClient* msgSender) {
	struct thread_status_t test_thread1 = {
		pid : 0,
		create_result : -1,
	};
	struct thread_status_t test_thread2 = {
		pid : 0,
		create_result : -1,
	};
	switch (index) {
		case INDEX_TEST_L2C:
			strcpy(test_thread1.param.file, (char *) FILE_L2C);
		test_thread1.create_result = pthread_create(&test_thread1.pid, NULL,
				apmcu_test, (void *) &test_thread1.param);
		break;
		case INDEX_TEST_NEON:
			strcpy(test_thread1.param.file, (char *) FILE_NEON);
		test_thread1.create_result = pthread_create(&test_thread1.pid, NULL,
				apmcu_test, (void *) &test_thread1.param);
		break;
		case INDEX_TEST_NEON_DUAL:
			strcpy(test_thread1.param.file, (char *) FILE_NEON_DUAL_0);
		test_thread1.create_result = pthread_create(&test_thread1.pid, NULL,
				apmcu_test, (void *) &test_thread1.param);
		strcpy(test_thread2.param.file, (char *) FILE_NEON_DUAL_1);
		test_thread2.create_result = pthread_create(&test_thread2.pid, NULL,
				apmcu_test, (void *) &test_thread2.param);
		break;
		case INDEX_TEST_CA9:
			strcpy(test_thread1.param.file, (char *) FILE_CA9);
		test_thread1.create_result = pthread_create(&test_thread1.pid, NULL,
				apmcu_test, (void *) &test_thread1.param);
		break;
		case INDEX_TEST_CA9_DUAL:
			strcpy(test_thread1.param.file, (char *) FILE_CA9_DUAL_0);
			test_thread1.create_result = pthread_create(&test_thread1.pid, NULL,
				apmcu_test, (void *) &test_thread1.param);
			strcpy(test_thread2.param.file, (char *) FILE_CA9_DUAL_1);
			test_thread2.create_result = pthread_create(&test_thread2.pid, NULL,
				apmcu_test, (void *) &test_thread2.param);
			break;
		default:
			break;
	}
	if (test_thread1.pid) {
		pthread_join(test_thread1.pid, NULL);
	}
	if (test_thread2.pid) {
		pthread_join(test_thread2.pid, NULL);
	}
	char result[CPUTEST_RESULT_SIZE] = { 0 };
	strncat(result, test_thread1.param.result, strlen(test_thread1.param.result)-1);
	strncat(result, ";", 1);
	strncat(result, test_thread2.param.result, strlen(test_thread2.param.result)-1);
	LOGD("apmcu result is %s", result);
	msgSender->PostMsg(result);
}

int ModuleCpuStress::ApMcu(RPCClient* msgSender) {
	int paraNum = msgSender->ReadInt();
	int index = 0;
	if (paraNum != 1) {
		msgSender->PostMsg((char*) ERROR);
		return -1;
	}
	int T = msgSender->ReadInt();
	if (T != PARAM_TYPE_INT) {
		return -1;
	}
	int L = msgSender->ReadInt();
	index = msgSender->ReadInt();
	switch (index) {
	case INDEX_TEST_L2C:
	case INDEX_TEST_NEON:
	case INDEX_TEST_NEON_DUAL:
	case INDEX_TEST_CA9:
	case INDEX_TEST_CA9_DUAL:
		doApMcuTest(index, msgSender);
		break;
	default:
		LOGE("apmcu unknow index: %d", index);
		break;
	}
	return 0;
}

//#define SYSTEM_CALL
//#define NONBLOCK

void * swcodec_test(void * argvoid) {
	struct thread_params_t * arg = (struct thread_params_t *) argvoid;
	int tid = gettid();
	LOGD("tid: %d, Enter swcodec_test: file: %s", tid, arg->file);
	FILE * fp;
	struct timeval timeout;
	struct timeval delay;
	delay.tv_sec = 0;
	delay.tv_usec = 100 * 1000;
#ifdef SYSTEM_CALL
	do {
		fp = popen(arg->file, "r");
		if (fp == NULL) {
			LOGE("popen fail: %s, errno: %d", arg->file, errno);
			strcpy(arg->result, "POPEN FAIL\n");
			break;
		}
		int fd = fileno(fp);
		char * find;
		fd_set rfd;
		int readsize;
		while(1) {
			timeout.tv_sec = 130;
			timeout.tv_usec = 0;
			FD_ZERO(&rfd);
			FD_SET(fd, &rfd);
			int res = select(fd+1, &rfd, NULL, NULL, &timeout);
			if (res < 0) {
				LOGE("tid: %d select error!",tid);
				strcpy(arg->result, "select error\n");
				break;
			} else if (res == 0) {
				LOGE("tid: %d timeout!", tid);
				strcpy(arg->result, "select timeout\n");
				break;
			} else {
				if (FD_ISSET(fd, &rfd)) {
					select(0, NULL, NULL, NULL, &delay);
					readsize = read(fd, arg->result, sizeof(arg->result));
					if (readsize) {
						LOGD("read size is %d, content is: %s", readsize, arg->result);
						if (readsize == -1) {
							LOGE("read error");
							break;
						} else if (readsize < sizeof(arg->result)) {
							LOGD("break;");
							break;
						}
					}
				}
			}
		}
	} while(0);
	if (fp != NULL) {
		LOGD("before pclose, tid: %d, errno: %d", gettid(), errno);
		select(0, NULL, NULL, NULL, &delay);
		LOGD("after pclose, tid: %d,close: %d, errno: %d", gettid(), pclose(fp), errno);
	}
#else
	do {
		fp = popen(arg->file, "r");
		select(0, NULL, NULL, NULL, &delay);
		if (fp == NULL) {
			LOGE("popen fail: %s, errno: %d", arg->file, errno);
			strcpy(arg->result, "POPEN FAIL\n");
			break;
		}
		pthread_mutex_lock(&lock);
#ifdef NONBLOCK
		int fd = fileno(fp);
		char * find;
		fd_set rfd;
		LOGD("begin to get result");
		while(1) {
			timeout.tv_sec = 130;
			timeout.tv_usec = 0;
			FD_ZERO(&rfd);
			FD_SET(fd, &rfd);
			int res = select(fd+1, &rfd, NULL, NULL, &timeout);
			if (res<0) {
				LOGE("tid: %d select error!",tid);
				strcpy(arg->result, "select error\n");
				break;
			} else if (res == 0) {
				LOGE("tid: %d timeout!", tid);
				strcpy(arg->result, "select timeout\n");
				break;
			} else {
				if (FD_ISSET(fd, &rfd)) {
					select(0, NULL, NULL, NULL, &delay);
					if (NULL != fgets(arg->result, sizeof(arg->result), fp)) {
					} else {
						LOGE("tid: %d fgets is null", tid);
						break;
					}
				}
			}
		}
#else
		select(0, NULL, NULL, NULL, &delay);
		LOGD("begin to get result");
		while(fgets(arg->result, sizeof(arg->result), fp)!=NULL) {
			select(0, NULL, NULL, NULL, &delay);
		}
#endif
		pthread_mutex_unlock(&lock);
		LOGD("swcodec result: %s", arg->result);
	} while(0);
	if (fp != NULL) {
		select(0, NULL, NULL, NULL, &delay);
		pthread_mutex_lock(&lock);
		LOGD("before pclose, tid: %d, errno: %d", gettid(), errno);
		//select(0, NULL, NULL, NULL, &delay);
		LOGD("after pclose, tid: %d, close: %d, errno: %d", gettid(), pclose(fp), errno);
		pthread_mutex_unlock(&lock);
	}
#endif
	pthread_exit(NULL);
	return NULL;
}


void doSwCodecTest(int index, int interation, RPCClient* msgSender) {
	LOGD("Enter doSwCodecTest");
	struct thread_status_t swcodec_test_thread1 = {
		pid : 0,
		create_result : -1,
	};
	struct thread_status_t swcodec_test_thread2 = {
		pid : 0,
		create_result : -1,
	};
	char buf[10];
	snprintf(buf, sizeof(buf), "%d", interation);
	switch(index) {
	case INDEX_SWCODEC_TEST_SINGLE:
		strcpy(swcodec_test_thread1.param.file, (char *) COMMAND_SWCODEC_TEST_SINGLE);
		strcat(swcodec_test_thread1.param.file, buf);
		swcodec_test_thread1.create_result = pthread_create(&swcodec_test_thread1.pid, NULL,
			swcodec_test, (void *) &swcodec_test_thread1.param);
		break;
	case INDEX_SWCODEC_TEST_FORCE_SINGLE:
		strcpy(swcodec_test_thread1.param.file, (char *) COMMAND_SWCODEC_TEST_FORCE_SINGLE);
		strcat(swcodec_test_thread1.param.file, buf);
		swcodec_test_thread1.create_result = pthread_create(&swcodec_test_thread1.pid, NULL,
			swcodec_test, (void *) &swcodec_test_thread1.param);
		break;
	case INDEX_SWCODEC_TEST_FORCE_DUAL:
		strcpy(swcodec_test_thread1.param.file, (char *) COMMAND_SWCODEC_TEST_DUAL_0);
		strcat(swcodec_test_thread1.param.file, buf);
		LOGD("thread1 param.file: %s", swcodec_test_thread1.param.file);
		swcodec_test_thread1.create_result = pthread_create(&swcodec_test_thread1.pid, NULL,
				swcodec_test, (void *) &swcodec_test_thread1.param);
		strcpy(swcodec_test_thread2.param.file, (char *) COMMAND_SWCODEC_TEST_DUAL_1);
		strcat(swcodec_test_thread2.param.file, buf);
		LOGD("thread2 param.file: %s", swcodec_test_thread2.param.file);
		swcodec_test_thread2.create_result = pthread_create(&swcodec_test_thread2.pid, NULL,
		swcodec_test, (void *) &swcodec_test_thread2.param);
		break;
	default:
		break;
	}
	if (swcodec_test_thread1.pid) {
		pthread_join(swcodec_test_thread1.pid, NULL);
	}
	if (swcodec_test_thread2.pid) {
		pthread_join(swcodec_test_thread2.pid, NULL);
	}
	char result[CPUTEST_RESULT_SIZE] = { 0 };
	strncat(result, swcodec_test_thread1.param.result, strlen(swcodec_test_thread1.param.result)-1);
	strncat(result, ";", 1);
	strncat(result, swcodec_test_thread2.param.result, strlen(swcodec_test_thread2.param.result)-1);
	LOGD("doSwCodecTest result is %s", result);
	msgSender->PostMsg(result);
}

int ModuleCpuStress::SwCodec(RPCClient* msgSender) {
	int paraNum = msgSender->ReadInt();
	int index = 0;
	int interation = 0;
	if (paraNum != 2) {
		msgSender->PostMsg((char*) ERROR);
		return -1;
	}
	int T = msgSender->ReadInt();
	if (T != PARAM_TYPE_INT) {
		//error
		return -1;
	}
	int L = msgSender->ReadInt();
	index = msgSender->ReadInt();
	LOGD("ModuleCpuStress:SwCodec index: %d", index);
	T = msgSender->ReadInt();
	if (T != PARAM_TYPE_INT) {
		return -1;
	}
	L = msgSender->ReadInt();
	interation = msgSender->ReadInt();
	LOGD("ModuleCpuStress:SwCodec interate: %d", interation);
	switch (index) {
		case INDEX_SWCODEC_TEST_SINGLE:
		case INDEX_SWCODEC_TEST_FORCE_SINGLE:
		case INDEX_SWCODEC_TEST_FORCE_DUAL:
			doSwCodecTest(index, interation, msgSender);
			break;
		default:
			LOGE("SwCodec unknow index: %d", index);
			break;
		}
	return 0;
}

void doBackupRestore(int index) {
	char command[CPUTEST_RESULT_SIZE] = { 0 };
	FILE * fp;
	switch(index) {
		case INDEX_TEST_BACKUP:
			strcpy(command, "cat ");
			fp = popen(strcat(command, FILE_CPU0_SCAL), "r");
			if (fp == NULL) {
				LOGE("INDEX_TEST_BACKUP popen fail, errno: %d", errno);
				return;
			}
			fgets(backup_first, sizeof(backup_first), fp);
			LOGD("backup_first: %s", backup_first);
			pclose(fp);
			strcpy(command, "echo performance > ");
			strcat(command, FILE_CPU0_SCAL);
			LOGD("INDEX_TEST_BACKUP: %s", command);
			system(command);
			break;
		case INDEX_TEST_BACKUP_TEST:
			strcpy(command, "echo 1 > ");
			strcat(command, FILE_CPU1_ONLINE);
			LOGD("INDEX_TEST_BACKUP_TEST: %s", command);
			system(command);
			strcpy(command, "echo 0 > ");
			strcat(command, FILE_HOTPLUG);
			LOGD("INDEX_TEST_BACKUP_TEST: %s", command);
			system(command);
			break;
		case INDEX_TEST_BACKUP_SINGLE:
			strcpy(command, "cat ");
			fp = popen(strcat(command, FILE_CPU0_SCAL), "r");
			if (fp == NULL) {
				LOGE("INDEX_TEST_BACKUP_SINGLE popen fail, errno: %d", errno);
				return;
			}
			fgets(backup_first, sizeof(backup_first), fp);
			LOGD("backup_first: %s", backup_first);
			pclose(fp);
			strcpy(command, "echo performance > ");
			strcat(command, FILE_CPU0_SCAL);
			LOGD("INDEX_TEST_BACKUP_SINGLE: %s", command);
			system(command);
			strcpy(command, "echo 0 > ");
			strcat(command, FILE_CPU1_ONLINE);
			LOGD("INDEX_TEST_BACKUP_SINGLE: %s", command);
			system(command);
			strcpy(command, "echo 0 > ");
			strcat(command, FILE_HOTPLUG);
			LOGD("INDEX_TEST_BACKUP_SINGLE: %s", command);
			system(command);
			break;
		case INDEX_TEST_BACKUP_DUAL:
			strcpy(command, "cat ");
			fp = popen(strcat(command, FILE_CPU0_SCAL), "r");
			if (fp == NULL) {
				LOGE("INDEX_TEST_BACKUP_DUAL popen fail, errno: %d", errno);
				return;
			}
			fgets(backup_first, sizeof(backup_first), fp);
			LOGD("backup_first: %s", backup_first);
			pclose(fp);
			strcpy(command, "cat ");
			fp = popen(strcat(command, FILE_CPU1_SCAL), "r");
			if (fp == NULL) {
				LOGE("INDEX_TEST_BACKUP_DUAL popen fail, errno: %d", errno);
				return;
			}
			fgets(backup_second, sizeof(backup_second), fp);
			LOGD("backup_second: %s", backup_second);
			pclose(fp);
			strcpy(command, "echo performance > ");
			strcat(command, FILE_CPU0_SCAL);
			LOGD("INDEX_TEST_BACKUP_DUAL: %s", command);
			system(command);
			strcpy(command, "echo 1 > ");
			strcat(command, FILE_CPU1_ONLINE);
			LOGD("INDEX_TEST_BACKUP_DUAL: %s", command);
			system(command);
			strcpy(command, "echo performance > ");
			strcat(command, FILE_CPU1_SCAL);
			LOGD("INDEX_TEST_BACKUP_DUAL: %s", command);
			system(command);
			strcpy(command, "echo 0 > ");
			strcat(command, FILE_HOTPLUG);
			LOGD("INDEX_TEST_BACKUP_DUAL: %s", command);
			system(command);
			break;
		case INDEX_TEST_RESTORE:
			strcpy(command, "echo ");
			strncat(command, backup_first, strlen(backup_first) - 1);
			strcat(command, " > ");
			strcat(command, FILE_CPU0_SCAL);
			LOGD("INDEX_TEST_RESTORE: %s", command);
			system(command);
			break;
		case INDEX_TEST_RESTORE_TEST:
			strcpy(command, "echo 0 > ");
			strcat(command, FILE_CPU1_ONLINE);
			LOGD("INDEX_TEST_RESTORE_TEST: %s", command);
			system(command);
			strcpy(command, "echo 1 > ");
			strcat(command, FILE_HOTPLUG);
			LOGD("INDEX_TEST_RESTORE_TEST: %s", command);
			system(command);
			break;
		case INDEX_TEST_RESTORE_SINGLE:
			strcpy(command, "echo ");
			strncat(command, backup_first, strlen(backup_first) - 1);
			strcat(command, " > ");
			strcat(command, FILE_CPU0_SCAL);
			LOGD("INDEX_TEST_RESTORE_SINGLE: %s", command);
			system(command);
			//strcpy(command, "echo 1 > ");
			//strcat(command, FILE_CPU1_ONLINE);
			//LOGD("INDEX_TEST_RESTORE_SINGLE: %s", command);
			//system(command);
			strcpy(command, "echo 1 > ");
			strcat(command, FILE_HOTPLUG);
			LOGD("INDEX_TEST_RESTORE_SINGLE: %s", command);
			system(command);
			break;
		case INDEX_TEST_RESTORE_DUAL:
			strcpy(command, "echo ");
			strncat(command, backup_first, strlen(backup_first) - 1);
			strcat(command, " > ");
			strcat(command, FILE_CPU0_SCAL);
			LOGD("INDEX_TEST_RESTORE_DUAL: %s", command);
			system(command);
			strcpy(command, "echo ");
			strncat(command, backup_second, strlen(backup_second) - 1);
			strcat(command, " > ");
			strcat(command, FILE_CPU1_SCAL);
			LOGD("INDEX_TEST_RESTORE_DUAL: %s", command);
			system(command);
			strcpy(command, "echo 1 > ");
			strcat(command, FILE_HOTPLUG);
			LOGD("INDEX_TEST_RESTORE_DUAL: %s", command);
			system(command);
			break;
		default:
			break;
	}
}

int ModuleCpuStress::BackupRestore(RPCClient* msgSender) {
	int paraNum = msgSender->ReadInt();
	int index = 0;
	if (paraNum != 1) {
		msgSender->PostMsg((char*) ERROR);
		return -1;
	}
	int T = msgSender->ReadInt();
	if (T != PARAM_TYPE_INT) {
		return -1;
	}
	int L = msgSender->ReadInt();
	index = msgSender->ReadInt();
	switch (index) {
	case INDEX_TEST_BACKUP:
		doBackupRestore(index);
		msgSender->PostMsg((char *)"INDEX_TEST_BACKUP");
		break;
	case INDEX_TEST_BACKUP_TEST:
		doBackupRestore(index);
		msgSender->PostMsg((char *)"INDEX_TEST_BACKUP_TEST");
		break;
	case INDEX_TEST_BACKUP_SINGLE:
		doBackupRestore(index);
		msgSender->PostMsg((char *)"INDEX_TEST_BACKUP_SINGLE");
		break;
	case INDEX_TEST_BACKUP_DUAL:
		doBackupRestore(index);
		msgSender->PostMsg((char *)"INDEX_TEST_BACKUP_DUAL");
		break;
	case INDEX_TEST_RESTORE:
		doBackupRestore(index);
		msgSender->PostMsg((char *)"INDEX_TEST_RESTORE");
		break;
	case INDEX_TEST_RESTORE_TEST:
		doBackupRestore(index);
		msgSender->PostMsg((char *)"INDEX_TEST_RESTORE_TEST");
		break;
	case INDEX_TEST_RESTORE_SINGLE:
		doBackupRestore(index);
		msgSender->PostMsg((char *)"INDEX_TEST_RESTORE_SINGLE");
		break;
	case INDEX_TEST_RESTORE_DUAL:
		doBackupRestore(index);
		msgSender->PostMsg((char *)"INDEX_TEST_RESTORE_DUAL");
		break;
	default:
		LOGE("BackupRestore unknow index: %d", index);
		msgSender->PostMsg((char *)"BackRestore unknow index");
		break;
	}
	return 0;
}


int ModuleCpuStress::ThermalUpdate(RPCClient* msgSender) {
	int paraNum = msgSender->ReadInt();
	int index = 0;
	if (paraNum != 1) {
		msgSender->PostMsg((char*) ERROR);
		return -1;
	}
	int T = msgSender->ReadInt();
	if (T != PARAM_TYPE_INT) {
		return -1;
	}
	int L = msgSender->ReadInt();
	index = msgSender->ReadInt();
	switch(index) {
	case INDEX_THERMAL_DISABLE:
		system(THERMAL_DISABLE_COMMAND);
		LOGD("disable thermal: %s", THERMAL_DISABLE_COMMAND);
		msgSender->PostMsg((char *)"INDEX_THERMAL_DISABLE");
		break;
	case INDEX_THERMAL_ENABLE:
		system(THERMAL_ENABLE_COMMAND);
		LOGD("enable thermal: %s", THERMAL_ENABLE_COMMAND);
		msgSender->PostMsg((char *)"INDEX_THERMAL_ENABLE");
		break;
	default:
		break;
	}
	return 0;
}

ModuleCpuStress::ModuleCpuStress(void) {
	pthread_mutex_init(&lock, NULL);
}
ModuleCpuStress::~ModuleCpuStress(void) {
	pthread_mutex_destroy(&lock);
}

