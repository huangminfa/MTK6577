/*
**
** Copyright 2008, The Android Open Source Project
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

// System headers required for setgroups, etc.
#include <sys/types.h>
#include <unistd.h>
#include <grp.h>
#include <linux/rtpm_prio.h>
#include <sys/prctl.h>
#include <private/android_filesystem_config.h>

#include <binder/IPCThreadState.h>
#include <binder/ProcessState.h>
#include <binder/IServiceManager.h>
#include <utils/Log.h>

#include <AudioFlinger.h>
#include <CameraService.h>
#include <MediaPlayerService.h>

#ifdef MTK_MATV_SUPPORT
#include <ATVCtrlService.h>
#endif
//MTK_OP01_PROTECT_START
#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_CMMBSP_SUPPORT   	
#include "ICmmbSp.h"
#endif
#endif
//MTK_OP01_PROTECT_END

#ifndef ANDROID_DEFAULT_CODE
#include <MemoryDumper.h>
#endif
#include <AudioPolicyService.h>
#include <private/android_filesystem_config.h>

#ifdef MTK_VT3G324M_SUPPORT
#include "VTSServiceForMediaServer.h"
#endif
#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_CMMBSP_SUPPORT   	
#include <dlfcn.h>
typedef void (*my_Instantiate)(void);
#endif
#endif

using namespace android;

int main(int argc, char** argv)
{
    sp<ProcessState> proc(ProcessState::self());
    sp<IServiceManager> sm = defaultServiceManager();
    LOGI("ServiceManager: %p", sm.get());
    
    AudioFlinger::instantiate();
#ifdef MTK_MATV_SUPPORT
    LOGE("Mediaserver ATVCtrlService register");
    ATVCtrlService::instantiate();
#endif

    MediaPlayerService::instantiate();
#ifndef ANDROID_DEFAULT_CODE
    MemoryDumper::instantiate();
#endif
    CameraService::instantiate();
#ifdef MTK_VT3G324M_SUPPORT
    VTService::VTSService_instantiate();
#endif
    AudioPolicyService::instantiate();
    //MTK_OP01_PROTECT_START
#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_CMMBSP_SUPPORT   	
#if 1
    void *handle=NULL;
    const char* dlerr;
    handle = dlopen("/data/data/com.mediatek.cmmb.app/lib/libcmmbsp.so",RTLD_NOW);
    dlerr = dlerror();
    if (dlerr != NULL) LOGE("dlopen() error: %s\n", dlerr);
    if(!handle){
	    LOGE("open /data/data/com.mediatek.cmmb.app/lib/libcmbmsp.so fail,then open /system/lib/so");
	    handle = dlopen("libcmmbsp.so",RTLD_NOW);
	    if(!handle){
		    LOGE("open /system/lib/libcmbmsp.so fail");
		    return 0;
	    }
    }
    LOGI("open libcmmbsp.so success");
    if(handle){
	    my_Instantiate F_instant;
	    F_instant =(my_Instantiate)dlsym(handle,"BnCmmbSpinstant");
	    dlerr = dlerror();
	    if (dlerr != NULL){
		    LOGE( "dlsym() error: %s\n", dlerr);
		    return 0;
	    }
	    if(F_instant)
		    F_instant();
    }
#else
        BnCmmbSp::instantiate();
#endif
#endif    	
#endif
        
    if (AID_ROOT == getuid()) {
        LOGI("[%s] re-adjust caps for its thread, and set uid to media", __func__);
        if (-1 == prctl(PR_SET_KEEPCAPS, 1, 0, 0, 0)) {
            LOGW("mediaserver prctl for set caps failed: %s", strerror(errno));
        } else {
            __user_cap_header_struct hdr;
            __user_cap_data_struct data;

            setuid(AID_MEDIA);         // change user to media
    
            hdr.version = _LINUX_CAPABILITY_VERSION;    // set caps again
            hdr.pid = 0;
            data.effective = (1 << CAP_SYS_NICE);
            data.permitted = (1 << CAP_SYS_NICE);
            data.inheritable = 0xffffffff;
            if (-1 == capset(&hdr, &data)) {
                LOGW("mediaserver cap re-setting failed, %s", strerror(errno));
            }
        }

    } else {
        LOGI("[%s] re-adjust caps is not in root user", __func__);
    }    

    //MTK_OP01_PROTECT_END
    ProcessState::self()->startThreadPool();
    IPCThreadState::self()->joinThreadPool();
#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_CMMBSP_SUPPORT
    if(handle){
         LOGI("cmmbspso,dlcose");
        dlclose(handle);
    }
#endif
#endif
}
