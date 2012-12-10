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
#include <fcntl.h>
#include <dirent.h>
#include <string.h>
#include <errno.h>

#include "jni.h"
#include "JNIHelp.h"
#include <android_runtime/AndroidRuntime.h>


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

#define MAX_NAME_LEN 256
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

void Swap(int *a,int * b)
{
	int temp;
	temp= *a;
	*a= *b;
	*b= temp;
}

void BubbleSort( int *array,int length)
{
	for(int i=0 ; i<length;++ i)
	{		
		for(int j=i+1;j<length;++ j)
		{
			if(array[j]<array[i])
			{			
				Swap(&array[j],&array[i]);
			} 
		} 		
	} 
}

int get_proc_name(pid_t pid, char *proc_name, int proc_name_sz)
{
	int fd;
	int ret = 0;
	static char filename[MAX_NAME_LEN];

	sprintf(filename, "/proc/%d/cmdline", pid);

	memset(proc_name, 0, proc_name_sz);

	fd = open(filename, O_RDONLY);
	if (fd < 0) {
		return -1;
	}

	ret = read(fd, proc_name, proc_name_sz);

	close(fd);

	return ret;
}

static pid_t find_native_service_pid(const char *name)
{
    pid_t pid;
    DIR *dir;
    struct dirent *dirent;
    char proc_name[MAX_NAME_LEN];
    char search_name[MAX_NAME_LEN];
    int found = 0;

    dir = opendir("/proc");
    if (!dir)
        return 0;

    sprintf(search_name, "/system/bin/%s", name);

    while ((dirent = readdir(dir))) {
        if (sscanf(dirent->d_name, "%d", &pid) < 1) {
            continue;
        }

        if (get_proc_name(pid, proc_name, MAX_NAME_LEN) > 0) {
            if (strstr(proc_name, search_name)) {
                printf("%s, %d\n", proc_name, pid);
                found = 1;
                break;
            }
        }
    }
    closedir(dir);

    if (!found) {
        return 0;
    }
    return pid;
}


static jint wrap_rtt_dump_all_backtrace_in_one_file(JNIEnv* env, jobject clazz,jintArray pidArray, int pidnum,jstring file_path)
{
	int i = 0;
	int iRet = 0;
	char* tmpFilePath = "/data/anr/dump.tmp";
	FILE* fpTmp = NULL;
	FILE* fpDest = NULL;
	pid_t pid = 0;
	const char *path = env->GetStringUTFChars(file_path, NULL);
	jint * pids = env->GetIntArrayElements(pidArray, NULL);
	long file_size = 0;
	long default_file_size = 128*1024;  // 128KB for real use, 8K for test
	char *tmpBuf = NULL;
	char *allocBuf = NULL;
	const char *native_services[] = {"surfaceflinger", "mediaserver", NULL};

/*
work around:
rtt open file with O_CREATE, so it will overwrite existing content,
to work around this limitation, pass tmp file to rtt and append it to traces.txt
*/
	if( fpDest = fopen(path,"a+") )
	{
		
	}
	else
	{
		LOGI("ANR_NATIVE_DUMP: open %s fail, %s", path, strerror(errno));
		goto Exit;
	}		

	if(!pids || !pidnum) goto Exit;
	
	// To reduce memory alloc/free overhead and de-fragment, malloc a buffer
	tmpBuf = (char *)calloc(1, default_file_size);
	
	if(!tmpBuf)
	{
		LOGI("ANR_NATIVE_DUMP: malloc %d is failed", default_file_size);
		goto Exit;
	}
BubbleSort(pids,pidnum);
	for(i=0;i<pidnum;i++){
		LOGI("pid[%d]=%d",i,pids[i]);
	}
	
	for(i=0; i<pidnum; i++)
	{
		if(pids[i] == 0)
			continue;
		if( rtt_dump_all_backtrace(pids[i], (char *)tmpFilePath) < 0)
		{
			LOGI("ANR_NATIVE_DUMP: rtt pid[%d]=%d BT dump FAIL...", i, pids[i]);
		}
		else
		{
			LOGI("rtt pid[%d]=%d BT dump OK...", i, pids[i]);
			
			// rtt_dump_all_backtrace will close *fp before return, so it needs to re-open file
			if( ( fpTmp = fopen(tmpFilePath, "r") ) )  // open for read-write
			{
		
			}	
			else
			{
				LOGI("ANR_NATIVE_DUMP: open %s fail, %s", tmpFilePath, strerror(errno));				
				goto Exit;
			}
			
			// get file size
			fseek(fpTmp, 0, SEEK_END);
			file_size  = ftell(fpTmp);
			rewind(fpTmp);
			
			LOGI("file_size: %d", file_size);
			
			if(file_size == 0)
			{
				LOGI("pid %d may be not existing", pids[i]);
				goto Exit;
			}
			
			// use realloc instead of malloc to reduce VM fragmentation
			// if tmpBuf == NULL, realloc -> malloc
			// if enough memory, tmpBuf will not change when return from realloc
			// else, previous buffer will be free, and malloc a new bigger buffer
			// if realloc is fail, it will return NULL, and does not touch pre-buffer
			// anyway, make sure file_size should not be less than previous value			
			if(file_size > default_file_size)
			{
				LOGI("ANR_NATIVE_DUMP: +realloc at 0x%x (%d->%d)", tmpBuf, default_file_size, file_size);
				allocBuf = (char *)realloc(tmpBuf, file_size);
				default_file_size = file_size;
				
				if(!allocBuf)
				{
					LOGI("ANR_NATIVE_DUMP: realloc is fail");
					goto Exit;
				}
				else
				{
					// if realloc fail, it will return NULL, and does not touch old buffer and fail to free
					// so only change old buffer ptr when realloc sucessfully				

					if(tmpBuf != allocBuf){
						LOGI("ANR_NATIVE_DUMP: realloc extend old buffer FAIL, new a buffer at 0x%x", allocBuf);
						//tmpBuf[0] = 'S'; // since tmpBuf has been free so, it will raise NE here
						tmpBuf = allocBuf;
					}
					else
					{
						LOGI("ANR_NATIVE_DUMP: realloc extend old buffer OK");
					}
				}
			}

			// fread
			fread(tmpBuf, file_size, 1, fpTmp);

			//LOGI("%s", tmpBuf);
			
			// fwrite
			fwrite(tmpBuf, file_size, 1, fpDest);
									
			//free
			if(fpTmp){
				 fclose(fpTmp);
				 fpTmp = NULL;	
			}				
		}			
	}
	
    for (i = 0; native_services[i] != NULL; i++) {

        pid = find_native_service_pid(native_services[i]);

        if (pid != 0) {
            if (rtt_dump_all_backtrace(pid, (char *)tmpFilePath) < 0) {
                LOGI("ANR_NATIVE_DUMP: rtt pid[%d]=%d BT dump FAIL...", i, pid);
            } else {
                LOGI("rtt %s(pid:%d) BT dump OK...", native_services[i], pid);

                // rtt_dump_all_backtrace will close *fp before return, so it
                // needs to re-open file
                if ((fpTmp = fopen(tmpFilePath, "r"))) { // open for read-write

                } else {
                    LOGI("ANR_NATIVE_DUMP: open %s fail, %s", tmpFilePath, strerror(errno));
                    goto Exit;
                }

                // get file size
                fseek(fpTmp, 0, SEEK_END);
                file_size  = ftell(fpTmp);
                rewind(fpTmp);

                LOGI("file_size: %d", file_size);

                if (file_size == 0) {
                    LOGI("pid %d may be not existing", pid);
                    goto Exit;
                }

                // use realloc instead of malloc to reduce VM fragmentation
                // if tmpBuf == NULL, realloc -> malloc
                // if enough memory, tmpBuf will not change when return from
                // realloc else, previous buffer will be free, and malloc a
                // new bigger buffer if realloc is fail, it will return NULL,
                // and does not touch pre-buffer anyway, make sure file_size
                // should not be less than previous value			
                if (file_size > default_file_size) {
                    LOGI("ANR_NATIVE_DUMP: +realloc at 0x%x (%d->%d)", tmpBuf,
                        default_file_size, file_size);
                    allocBuf = (char *)realloc(tmpBuf, file_size);
                    default_file_size = file_size;

                    if (!allocBuf) {
                        LOGI("ANR_NATIVE_DUMP: realloc is fail");
                        goto Exit;
                    } else {
                        // if realloc fail, it will return NULL, and does not
                        // touch old buffer and fail to free so only change
                        // old buffer ptr when realloc sucessfully

                        if (tmpBuf != allocBuf) {
                            LOGI("ANR_NATIVE_DUMP: realloc extend old buffer"
                                 " FAIL, new a buffer at 0x%x", allocBuf);
                            // tmpBuf[0] = 'S'; // since tmpBuf has been free so,
                            // it will raise NE here
                            tmpBuf = allocBuf;
                        } else {
                            LOGI("ANR_NATIVE_DUMP: realloc extend old buffer OK");
                        }
                    }
                }

                // fread
                fread(tmpBuf, file_size, 1, fpTmp);

                // LOGI("%s", tmpBuf);

                // fwrite
                fwrite(tmpBuf, file_size, 1, fpDest);

                // free
                if (fpTmp) {
                    fclose(fpTmp);
                    fpTmp = NULL;
                }
            }
        }
    }  // end of for
	
#if 0
	if(!(fpDest = fopen(path,"a+"))) goto Exit;
	BubbleSort(pids,pidnum);
	for(i=0;i<pidnum;i++){
		LOGI("pid[%d]=%d",i,pids[i]);
	}
	for(i=0;i<pidnum;i++)
	{
		LOGI("dump %dth pid %d",i,pids[i]);
	
		if(rtt_dump_all_backtrace(pids[i],smallFile))
		{
			LOGI("rtt_dump_all_backtrace failed");
			if((fp = fopen(smallFile,"r")))
			{
				while(!feof(fp) && !ferror(fp))
				{
					fputc(fgetc(fp),fpDest);
				}
				fclose(fp);
				fp = NULL;
			}
			goto Exit;
		}
		else
		{
			if(!(fp = fopen(smallFile,"r")))
			{
				LOGI("rtt_dump_all_backtrace fopen failed");	
				goto Exit;
			}
			else
			{
				while(!feof(fp) && !ferror(fp))
				{
					// MTK80133 discard this method since it is not efficient to dump "system server like" process
					// however, it method does not touch memory, so it rasie memory leakage
					fputc(fgetc(fp),fpDest);
				}
				fclose(fp);
				fp = NULL;
			}
		}

	}
#endif
	iRet = 1;

Exit:
	
	if(tmpBuf)
		free(tmpBuf);
	
	// make sure no memory leakage in any exception case	
	if(fpTmp)  fclose(fpTmp);
	if(fpDest) fclose(fpDest);
	
	remove(tmpFilePath);
			
	return iRet;
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

#define BINDER_STATE_LOG_PATH "/sys/kernel/debug/binder/state"
//#define BINDER_MUTEX_LOG_PATH "/sys/kernel/debug/binder/mutex_log"

static void dump_binder_log(int fd, char *binder_log_path)
{
/*
    char  propEnable[PROPERTY_VALUE_MAX];
*/
    char buf[1024];
    int binder_fd/*, monkey*/;
    ssize_t actual;

/*
    property_get("ro.monkey", propEnable, "");
    if (!strcmp(propEnable, "true"))  {
        monkey = 1;
    } else {
        monkey = 0;
    }
*/

    binder_fd = open(binder_log_path, O_RDONLY, 0666);
    if (binder_fd < 0) {
        LOGD("Unable to open %s\n", binder_log_path);
    } else {
        LOGD("Successfully open %s\n", binder_log_path);
        while ((actual = read(binder_fd, buf, sizeof(buf)-1)) != 0) {
            if (actual < 0) {
                LOGD("actual=%d\n", (int)actual);
                break;
            }
/*
            if (monkey)  {
                LOGD("%s\n", buf);
            }
*/
            write(fd, buf, actual);
        }
        close(binder_fd);
    }
}

static jint dump_binder_state(JNIEnv* env, jobject clazz, jstring file_path)
{
    jboolean ret = JNI_FALSE;
    const char *path = env->GetStringUTFChars(file_path, NULL);
    int fd = open(path, O_WRONLY | O_APPEND | O_CREAT, 0666);

    if (fd < 0) {
        LOGE("Unable to open file '%s': %s\n", path, strerror(errno));
    } else {
        dump_binder_log(fd, BINDER_STATE_LOG_PATH);
        close(fd);
        ret = JNI_TRUE;
    }
Exit:
    return ret;
}

static jint stop_ftrace(JNIEnv* env, jobject clazz, jstring file_path)
{
	
    jboolean ret = JNI_FALSE;
#if 1
	char path[64];
	char buf[16];
	int r_size = 0;
	int size = 0;
	int fd = 0;

	strcpy(path, "/sys/kernel/debug/tracing/tracing_enabled");

    fd = open(path, O_RDWR);

	if(fd < 0)
	{
		LOGE("Unable to open file '%s': %s\n", path, strerror(errno));
	}
	else
	{
		memset(buf, 0, sizeof(buf));
		strcpy(buf, "0");
		
		size = strlen("0");
		
		r_size = write(fd, buf, size);
		
		if(r_size < size)
		{
			LOGE("disable ftrace fail\n");	
			goto Exit;
		}
		else
		{
			
			lseek(fd, 0, SEEK_SET);			

			memset(buf, '\0', sizeof(buf));
		
			r_size = read(fd, buf, size);
		
			if(r_size < size)
			{
				LOGE("read back ftrace status fail\n");
				goto Exit;
			}
			else
			{
				LOGE("ftrace status: %s\n", buf);
			}
			
		}

    	close(fd);
    	ret = JNI_TRUE;
	}

Exit:
#endif	
    return ret;
}
// ----------------------------------------

namespace android {

static const JNINativeMethod rtt_methods[] = {
  { "native_rtt_is_ready", "(I)I", (void*)wrap_rtt_is_ready},
	{ "native_rtt_dump_backtrace", "(IILjava/lang/String;)I", (void*)wrap_rtt_dump_backtrace},
	{ "native_rtt_dump_all_backtrace", "(ILjava/lang/String;)I", (void*)wrap_rtt_dump_all_backtrace},
	{ "native_rtt_dump_all_backtrace_in_one_file", "([IILjava/lang/String;)I", (void*)wrap_rtt_dump_all_backtrace_in_one_file},
	{ "dumpBinderState", "(Ljava/lang/String;)Z", (void*)dump_binder_state},
	{ "native_rtt_stop_trace", "()Z", (void*)stop_ftrace},
};

int register_android_server_RTT(JNIEnv* env) {
    return AndroidRuntime::registerNativeMethods(env, "com/android/server/ANRStats",
                                                 rtt_methods, NELEM(rtt_methods));
}

}
