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

/*
 ** Copyright 2010, The Android Open Source Project
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

#define LOG_TAG "RTT_N"
#include <utils/Log.h>

#include <sys/types.h>
#include <sys/resource.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <dirent.h>
#include <string.h>
#include <errno.h>


#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <sched.h>
#include <unistd.h>
#include <time.h>
#include <grp.h>
#include <pwd.h>
#include "jni.h"
#include "JNIHelp.h"
#include <android_runtime/AndroidRuntime.h>
#include <cutils/sched_policy.h>


#include "android_server_top.cpp"

static jint topFileEnable = 0;
/*******************************************************/
//wrap_dumpProcAnr
//  dump the information between latest top and anr happened
//  
/*******************************************************/
static jint wrap_dumpProcAnr(JNIEnv* env, jobject clazz,jint prio)
{                              
	return dump_procs_anr();
}


/*******************************************************/
//wrap_top_CreateThread
//  create top monitor thread to record the thread jeffies
//  information
/*******************************************************/
static jint wrap_top_CreateThread(JNIEnv* env, jobject clazz,jint topFileEn)
{   
	topFileEnable = topFileEn;
	                           
	pthread_t rttThread = 0;
	pthread_attr_t threadAttr;
	struct sched_param param;
	
	pthread_attr_init(&threadAttr);
	pthread_attr_setdetachstate(&threadAttr,PTHREAD_CREATE_DETACHED);
	pthread_create(&rttThread,&threadAttr,anr_top_thread,&topFileEnable);
	return 0;
	//return 0;
}



/*******************************************************/
//wrap_rtt_informMonitorProc
//  This function need to co-operate with native process 
//  monitorProcess and fifo "/data/fifoserver"
//  we will not use this method in official build
/*******************************************************/
static jint wrap_rtt_informMonitorProc(JNIEnv* env, jobject clazz,jintArray pidArray, int pidnum,jstring file_path)
{
	int fd;
	char* pBuf = NULL;
	jint * pids = env->GetIntArrayElements(pidArray, NULL);
	
	
	fd = open("/data/fifoserver",O_WRONLY|O_NONBLOCK);
	if(fd==-1){
		LOGI("open fifo failed:%d",errno);	
		return 0;
	}
	
	pBuf = (char*)malloc((pidnum+1)*sizeof(int));
	if(!pBuf){
		LOGI("malloc pBuf failed");	
		close(fd);
		return 0;	
	}
	
	*(int*)pBuf = pidnum;
	for(int i=0;i<pidnum;i++){
		*((int*)pBuf+i+0x1) = pids[i];
	}
	
	if(write(fd,(char*)pBuf,(pidnum+1)*sizeof(int))== -1){
		LOGI("write fifo failed");	
	}else{
		LOGI("has write pid array into monitor process");		
	}
	
	close(fd);
	free(pBuf);
	return 0;
	
}


/*******************************************************/
//wrap_rtt_SetRealPriority
//  if the caller process have root permission, the monitor
//  thread will run as real time thread
//
//  return:
//			0  success
//			-1 failed
/*******************************************************/
static jint wrap_rtt_SetRealPriority(JNIEnv* env, jobject clazz,jint prio)
{                              
	struct sched_param param;
	pthread_attr_t attr;
	
	LOGI("calling wrap_rtt_SetRealPriority");
	////
	////
	////
	//param.sched_priority = prio;    
	////struct sched_param param;
	////int policy = SCHED_OTHER;
	int tmp = 0;
	//////
	//if((tmp=sched_setscheduler(0,SCHED_RR,&param))!=0){
	//	LOGI("failed sched_setscheduler:%d",tmp);	
	//}
	//
	pthread_t thread_id = pthread_self();
	param.sched_priority = prio;
	if((tmp = pthread_setschedparam(thread_id,SCHED_RR,&param)!=0)){
		LOGI("failed pthread_setschedparam:%d",tmp);
		return -1;	
	}
    
	return 0;
	
}

/*******************************************************/
//wrap_rtt_SetPriority
//  if the caller process do not have root permission, the monitor
//  thread will run as normal thread but nice could be
//	adjusted by prio
//
/*******************************************************/
static jint wrap_rtt_SetPriority(JNIEnv* env, jobject clazz,jint prio)
{                              
	LOGI("calling wrap_rtt_SetPriority");
	
	setpriority(PRIO_PROCESS,gettid(),prio);
	
	return 0;
	//return 0;
}

/*******************************************************/
//wrap_rtt_getPid
//  return the current thread's tid
//
//  return:
//		current thread's tid	
/*******************************************************/
static jint wrap_rtt_getPid(JNIEnv* env, jobject clazz,jint block)
{
	return gettid();
	//return 0;
}

static void wrap_dumpOneStack(int tid, int outFd) {
    char buf[64];

    snprintf(buf, sizeof(buf), "/proc/%d/stack", tid);
    int stackFd = open(buf, O_RDONLY);
    if (stackFd >= 0) {
        // header for readability
        strncat(buf, ":\n", sizeof(buf) - strlen(buf) - 1);
        write(outFd, buf, strlen(buf));

        // copy the stack dump text
        int nBytes;
        while ((nBytes = read(stackFd, buf, sizeof(buf))) > 0) {
            write(outFd, buf, nBytes);
        }

        // footer and done
        write(outFd, "\n", 1);
        close(stackFd);
    } else {
        LOGE("Unable to open stack of tid %d : %d (%s)", tid, errno, strerror(errno));
    }
}

static void wrap_dumpKernelStacks(JNIEnv* env, jobject clazz, jstring pathStr) {
    char buf[128];
    DIR* taskdir;

    LOGI("dumpKernelStacks");
    if (!pathStr) {
        jniThrowException(env, "java/lang/IllegalArgumentException", "Null path");
        return;
    }

    const char *path = env->GetStringUTFChars(pathStr, NULL);

    int outFd = open(path, O_WRONLY | O_APPEND | O_CREAT);
    if (outFd < 0) {
        LOGE("Unable to open stack dump file: %d (%s)", errno, strerror(errno));
        goto done;
    }

    snprintf(buf, sizeof(buf), "\n----- begin pid %d kernel stacks -----\n", getpid());
    write(outFd, buf, strlen(buf));

    // look up the list of all threads in this process
    snprintf(buf, sizeof(buf), "/proc/%d/task", getpid());
    taskdir = opendir(buf);
    if (taskdir != NULL) {
        struct dirent * ent;
        while ((ent = readdir(taskdir)) != NULL) {
            int tid = atoi(ent->d_name);
            if (tid > 0 && tid <= 65535) {
                // dump each stack trace
                wrap_dumpOneStack(tid, outFd);
            }
        }
        closedir(taskdir);
    }

    snprintf(buf, sizeof(buf), "----- end pid %d kernel stacks -----\n", getpid());
    write(outFd, buf, strlen(buf));

    close(outFd);
done:
    env->ReleaseStringUTFChars(pathStr, path);
}


#ifdef HAVE_AEE_FEATURE
#ifdef __cplusplus
extern "C" {
#endif 
extern int rtt_is_ready(int block);
extern int rtt_dump_backtrace(int pid,int tid,char* path);
extern int rtt_dump_all_backtrace(int pid,char* path);
#ifdef __cplusplus
}
#endif 

static jint wrap_rtt_is_ready(JNIEnv* env, jobject clazz,jint block)
{
	LOGI("calling wrap_rtt_is_ready");
	//return 0;
	return rtt_is_ready(block);
}

static jint wrap_rtt_dump_backtrace(JNIEnv* env, jobject clazz,jint pid, jint tid, jstring file_path)
{
	const char *path = env->GetStringUTFChars(file_path, NULL);
	//return 1;
	return rtt_dump_backtrace(pid,tid,(char*)path);
}

static jint wrap_rtt_dump_all_backtrace(JNIEnv* env, jobject clazz,jint pid, jstring file_path)
{
	const char *path = env->GetStringUTFChars(file_path, NULL);
	//return 1;
	return rtt_dump_all_backtrace(pid,(char*)path);
}

static jint wrap_rtt_dump_all_backtrace_in_one_file(JNIEnv* env, jobject clazz,jintArray pidArray, int pidnum,jstring file_path)
{

	int i = 0;
	int iRet = 0;
	char* smallFile = "/data/anr/full_traces.out";
	FILE* fp = NULL;
	FILE* fpDest = NULL;
	const char *path = env->GetStringUTFChars(file_path, NULL);
	jint * pids = env->GetIntArrayElements(pidArray, NULL);

	if(!pids || !pidnum) goto Exit;
	
	if(!(fpDest = fopen(path,"a+"))) goto Exit;

	for(i=0;i<pidnum;i++){
		LOGI("pid[%d]=%d",i,pids[i]);
	}

	for(i=0;i<pidnum;i++){
		LOGI("dump %dth pid %d",i,pids[i]);
		if(rtt_dump_all_backtrace(pids[i],smallFile)){
			LOGI("rtt_dump_all_backtrace failed");			
			goto Exit;
		}else{
			if(!(fp = fopen(smallFile,"r"))){
				LOGI("rtt_dump_all_backtrace failed");	
				goto Exit;
			}else{
				while(!feof(fp) && !ferror(fp)){
					fputc(fgetc(fp),fpDest);
				}
				fclose(fp);
				fp = NULL;
			}
		}
		
	}

	iRet = 1;
Exit:	
	if(fpDest) fclose(fpDest);
	remove(smallFile);
	return iRet;
/*
	int i = 0;
	int iRet = 0;
	const char *path = env->GetStringUTFChars(file_path, NULL);
	jint * pids = env->GetIntArrayElements(pidArray, NULL);

	if(!pids || !pidnum) goto Exit;

	for(i=0;i<pidnum;i++){
		if(rtt_dump_all_backtrace(pids[i],(char*)path)){
			LOGI("rtt_dump_all_backtrace failed");			
			goto Exit;
		}
	}
	iRet = 1;
Exit:
	return iRet;

*/
}
#else

static jint wrap_rtt_is_ready(JNIEnv* env, jobject clazz,jint block)
{
	return 0;
}

static jint wrap_rtt_dump_backtrace(JNIEnv* env, jobject clazz,jint pid, jint tid, jstring file_path)
{
	return 0;
}

static jint wrap_rtt_dump_all_backtrace(JNIEnv* env, jobject clazz,jint pid, jstring file_path)
{
    return 0;
}

static jint wrap_rtt_dump_all_backtrace_in_one_file(JNIEnv* env, jobject clazz,jintArray pidArray, int pidnum,jstring file_path)
{
    return 0;  
}
#endif

// ----------------------------------------

namespace android {

static const JNINativeMethod monitor_thread_methods[] = {
	{ "native_rtt_informMonitorProc", "([IILjava/lang/String;)I", (void*)wrap_rtt_informMonitorProc},  
	{ "native_rtt_SetPriority", "(I)I", (void*)wrap_rtt_SetPriority},
	{ "native_rtt_SetRealPriority", "(I)I", (void*)wrap_rtt_SetRealPriority},
	{ "native_rtt_GetPid", "(I)I", (void*)wrap_rtt_getPid},
	{ "native_rtt_is_ready", "(I)I", (void*)wrap_rtt_is_ready},
	{ "native_rtt_dump_backtrace", "(IILjava/lang/String;)I", (void*)wrap_rtt_dump_backtrace},
	{ "native_rtt_dump_all_backtrace", "(ILjava/lang/String;)I", (void*)wrap_rtt_dump_all_backtrace},
	{ "native_rtt_dump_all_backtrace_in_one_file", "([IILjava/lang/String;)I", (void*)wrap_rtt_dump_all_backtrace_in_one_file},
	{ "native_dumpKernelStacks", "(Ljava/lang/String;)V", (void*)wrap_dumpKernelStacks },
	{ "native_top_CreateThread", "(I)I", (void*)wrap_top_CreateThread},
	{ "native_dumpProcAnr", "(I)I", (void*)wrap_dumpProcAnr},
};

int register_android_server_MonitorThread(JNIEnv* env) {
    return AndroidRuntime::registerNativeMethods(env, "android/os/MonitorThread",
                                                 monitor_thread_methods, NELEM(monitor_thread_methods));
}

}
