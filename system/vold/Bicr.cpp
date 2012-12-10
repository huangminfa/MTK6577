/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>

#include <cutils/log.h>

#include "ResponseCode.h"
#include "Bicr.h"

#define TAG "BICR"

Bicr *Bicr::sInstance = NULL;

const char *Bicr::CD_ROM_PATH            = "/dev/block/loop0";
const char *Bicr::CD_ROM_LUN_PATH        = "/sys/class/android_usb/android0/f_mass_storage/lun-cdrom/file";
const char* Bicr::status_name[STATUS_COUNT]  = {
	"Not_Exist",
	"Unshared",
	"Shared",
	"Unsharing",
	"Sharing"
};

Bicr::Bicr() {
#ifdef MTK_BICR_SUPPORT
    mState = UNSHARED;
#else
    mState = NOT_EXIST;
#endif
    SLOGD("[%s]: cd-rom state: %s",__func__, status_name[mState]); 
}

Bicr *Bicr::Instance() {
    if (!sInstance)
        sInstance = new Bicr();
        
    SLOGD("[%s]: cd-rom state: %s", __func__, status_name[sInstance->mState]);  
    return sInstance;
}

int Bicr::shareCdRom() {
    int fd;
    int rc = 0;		
    
    if(mState == NOT_EXIST){
        SLOGD("[%s]: cd-rom doesn't exit!", __func__);
        return -1;
    }else if(mState != UNSHARED){
        SLOGD("[%s]: cd-rom cannot be shared!, mState=%s", __func__, status_name[mState]);
        return 0;
    }
    
    mState = SHARING;
    if ((fd = open(CD_ROM_LUN_PATH, O_WRONLY)) < 0) {
	        SLOGD("[%s]: failed to open cd-rom lunfile", __func__);
	        rc = -1;
    }
    else if (write(fd, CD_ROM_PATH, strlen(CD_ROM_PATH)) < 0) {
        SLOGD("[%s]: failed to write to cd-rom lunfile", __func__);
        rc = -1;
    }
    close(fd);
    
    if(!rc){
        mState = SHARED;
        SLOGD("[%s]: successfully shared cd-rom!", __func__);
    }
    
    SLOGD("[%s]: cd-rom state: %s", __func__, status_name[mState]);    
    return rc;
}

int Bicr::unShareCdRom() {
		int fd;
    int rc = 0;
    char ch = 0;
    
    if(mState == NOT_EXIST){
        SLOGD("[%s]: cd-rom doesn't exit!", __func__);
        return -1;
    }else if(mState != SHARED){
        SLOGD("[%s]: cd-rom cannot be unshared!, mState=%s",__func__, status_name[mState]);
        return 0;
    }
    
    mState = UNSHARING;
    if ((fd = open(CD_ROM_LUN_PATH, O_WRONLY)) < 0) {
	        SLOGD("[%s]: failed to open cd-rom lunfile", __func__);
	        rc = -1;
    } else if (write(fd, &ch, 1) < 0) {
        SLOGD("[%s]: failed to write to cd-rom lunfile", __func__);
        rc = -1;
    }
    close(fd);
    
    if(!rc){
        mState = UNSHARED;
        SLOGD("[%s]: successfully unshared cd-rom!", __func__);
    }
    
    SLOGD("[%s]: cd-rom state: %s", __func__, status_name[mState]);
    return rc;	
}

const char* Bicr::getStatus() {	
    return status_name[mState];
}