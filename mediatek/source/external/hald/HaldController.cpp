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
 * Copyright (C) 2010 The Android Open Source Project
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
#include <errno.h>
#include <fcntl.h>

#include <cutils/properties.h>

#define LOG_TAG "Hald"
#include <cutils/log.h>

#include "HaldController.h"
#ifdef MTK_SDIORETRY_SUPPORT
#include "libnvram.h"
#endif

#define MAX_NVRAM_RESTORE_READY_RETRY_NUM (10)
#define NVRAM_RESTORE_POLLING_TIME_USEC   (500 * 1000)

RfkillCtrl *HaldController::sRfkillCtrl = NULL;
DriverCtrl *HaldController::sDriverCtrl = NULL;

HaldController::HaldController() {
    if (!sRfkillCtrl) {
        sRfkillCtrl = new RfkillCtrl();
    }
    if (!sDriverCtrl) {
        sDriverCtrl = new DriverCtrl();
    }

    isWifiActive = false;
    isP2pActive = false;
    isHotspotActive = false;
}

HaldController::~HaldController() {
}

#ifdef MTK_SDIORETRY_SUPPORT
typedef struct
{
 int clk_src;
 int clk_src_freq;
 int data_red;
 int cmd_driving;
 int clk_latch;
 int clkpad_red;
 int cmd_phase;
 int data_phase;
} SDIO_RETRY_REG;

typedef struct
{
 int retry_flag;
 SDIO_RETRY_REG sdio;
}MT6573_SDIO_RETRY;

int  sdio_retry = {0};
int rec_size = 0;
int rec_num = 0;
MT6573_SDIO_RETRY  sdioRetrySetting;
int needReadSetting = 0;

extern int iFileSDIO_RETRYLID;

int writeRetrySetting()
{
	int ret = -1, fd = -1;

	fd = open("/proc/sdio_retry", O_WRONLY);
	if(0 < fd)
		ret = write(fd, &sdioRetrySetting, sizeof(sdioRetrySetting));
	else
		LOGD("Open /proc/sdio_retry failed\n");

	close(fd);
	LOGD("Write /proc/sdio_retry return %d\n", ret);

	return ret;
}

int readRetrySetting()
{
	int ret = -1, fd = -1;

	fd = open("/proc/sdio_retry", O_RDONLY);
	if(0 < fd)
		ret = read(fd, &sdioRetrySetting, sizeof(sdioRetrySetting));
	else
		LOGD("Open /proc/sdio_retry failed\n");

	close(fd);
	LOGD("Read /proc/sdio_retry return %d\n", ret);

	return ret;
}
#endif

int HaldController::loadDriver(const char *ifname){
#ifdef CFG_ENABLE_NVRAM_CHECK
    int nvram_restore_ready_retry = 0;
    char nvram_init_val[32];
	static bool fg_is_nvram_chk_failed = false;

    LOGD("Check NvRAM status.");
    while(nvram_restore_ready_retry < MAX_NVRAM_RESTORE_READY_RETRY_NUM) {
        nvram_restore_ready_retry++;
        property_get("nvram_init", nvram_init_val, NULL);
        if(strcmp(nvram_init_val, "Ready") == 0) {
            LOGD("NvRAM is READY!");
			fg_is_nvram_chk_failed = false;
            break;
        } else if (fg_is_nvram_chk_failed){
			LOGE("NvRAM status check is still failed! NvRAM content may be WRONG!");
			break;
        } else {
            usleep(NVRAM_RESTORE_POLLING_TIME_USEC);
		}
    }
    if(nvram_restore_ready_retry >= MAX_NVRAM_RESTORE_READY_RETRY_NUM) {
		fg_is_nvram_chk_failed = true;
        LOGE("NvRAM status check timeout(%dus)! NvRAM content may be WRONG!", MAX_NVRAM_RESTORE_READY_RETRY_NUM * NVRAM_RESTORE_POLLING_TIME_USEC);
    }
#endif

#ifdef MTK_SDIORETRY_SUPPORT
	//write the sdio retry setting. add by mtk80743
	{

		sdio_retry = NVM_GetFileDesc(iFileSDIO_RETRYLID, &rec_size, &rec_num, true);
		if(read(sdio_retry, &sdioRetrySetting, rec_num*rec_size) < 0){
					LOGD("read iFileSDIO_RETRYLID failed %s\n", strerror(errno));
		}else{
			if(sdioRetrySetting.retry_flag == 0 && sdioRetrySetting.sdio.clk_src == 0){
				needReadSetting = 1;
			}else{
				needReadSetting = 0;
				writeRetrySetting();
			}
		}
		NVM_CloseFileDesc(sdio_retry);
	}
#endif
    /*LOAD WIFI*/
    if (!strcmp(ifname, "wifi")) {
        /*if wifi or its sub function is on, no need to load wifi again*/
        if(isHotspotActive || isP2pActive || isWifiActive) {
            /*do nothing*/
            LOGE("Wifi driver is already loaded, no need to load again.");
            isWifiActive = true;
            return 0;
        /*load wifi driver*/
        } else {
            LOGD("Start load wifi driver.");
            /*load wifi driver*/
            sDriverCtrl->load(NETWORK_IFACE_WIFI);
            /*turn on wifi power*/
            powerOn();
            /*set flag*/
            isWifiActive = true;
            return 0;
        }
    /*LOAD HOTSPOT*/
    } else if (!strcmp(ifname, "hotspot")) {
        if(isHotspotActive) {
            LOGE("Hotspot driver is already loaded, no need to load again.");
            return 0;
        }
		/*if wifi is not on, MUST load wifi and turn on wifi power first*/
		if(!isWifiActive){
			sDriverCtrl->load(NETWORK_IFACE_WIFI);
			powerOn();
		}
		/*if p2p is on, unload p2p driver*/
		if(isP2pActive) {
            LOGE("Unload P2P driver first.");
            sDriverCtrl->unload(NETWORK_IFACE_P2P);
            isP2pActive = false;
        }
        /*load hotspot driver*/
        LOGD("Start load hotspot driver.");
        sDriverCtrl->load(NETWORK_IFACE_HOTSPOT);
        isHotspotActive = true;
#ifdef CFG_ENABLE_RFKILL_IF_FOR_CFG80211
		/*enable hotspot rfkill interface for cfg80211*/
		sRfkillCtrl->setAllState(1);
#endif
        return 0;
    /*LOAD P2P*/
    } else if (!strcmp(ifname, "p2p")) {
        if(isP2pActive) {
            LOGE("P2P driver is already loaded, no need to load again.");
            return 0;
        }
        /*if wifi is not on, MUST load wifi and turn on wifi power first*/
        if(!isWifiActive){
            sDriverCtrl->load(NETWORK_IFACE_WIFI);
            powerOn();
        }
		/*if hotspot is on, unload hotspot driver*/
		if(isHotspotActive) {
            LOGE("Unload Hotspot driver first.");
            sDriverCtrl->unload(NETWORK_IFACE_HOTSPOT);
            isHotspotActive = false;
		}
        /*load p2p driver*/
        LOGD("Start load P2P driver.");
        sDriverCtrl->load(NETWORK_IFACE_P2P);
        isP2pActive = true;
#ifdef CFG_ENABLE_RFKILL_IF_FOR_CFG80211
		/*enable p2p rfkill interface for cfg80211*/
		sRfkillCtrl->setAllState(1);
#endif
        return 0;
    }
    return -1;
}

int HaldController::unloadDriver(const char *ifname){
#ifdef MTK_SDIORETRY_SUPPORT
    //read the sdio retry setting. add by mtk80743
    if(needReadSetting == 1){
	readRetrySetting();
	sdio_retry = NVM_GetFileDesc(iFileSDIO_RETRYLID, &rec_size, &rec_num, false);
	if(lseek(sdio_retry,0,SEEK_SET)<0){
		LOGD("lseek %d iFileSDIO_RETRYLID failed %s\n",
			sdio_retry, strerror(errno));
		}
	if(write(sdio_retry, &sdioRetrySetting, rec_num*rec_size) < 0){
		LOGD("write %d iFileSDIO_RETRYLID failed %s\n",
			sdio_retry, strerror(errno));
	}else{
		LOGD("write iFileSDIO_RETRYLID successed\n");
	}
	NVM_CloseFileDesc(sdio_retry);
    }
#endif
    /*UNLOAD WIFI*/
    if (!strcmp(ifname, "wifi")) {
        /*if sub function is on or wifi is already off, do nothing*/
        if(isHotspotActive || isP2pActive || (!isWifiActive)) {
            LOGD("No need to unload wifi driver");
            isWifiActive = false;
            /*do nothing*/
            return 0;
        /*if sub function are off and wifi is on*/
        } else {
            LOGD("Start unload wifi driver.");
            /*No need to unload wifi driver*/
            powerOff();
            isWifiActive = false;
            return 0;
        }
    /*UNLOAD HOTSPOT*/
    } else if (!strcmp(ifname, "hotspot")) {
    	/* Note: for cfg80211 rfkill issue,
    		  *           the power off sequence shall be garanteed,
		  *	1. remove p2p module
		  *   2. power off wlan
    		  */
        if(false == isHotspotActive) {
            LOGE("Hotspot driver is already unloaded, no need to unload again.");
    	} else {
			LOGD("Start unload Hotspot driver.");
			/*unload hotspot driver*/
			sDriverCtrl->unload(NETWORK_IFACE_HOTSPOT);
		}
        /*if wifi is not on, turn off power*/
    	if(false == isWifiActive){
        	powerOff();
    	}
		isHotspotActive = false;
        return 0;
    /*UNLOAD P2P*/
    } else if (!strcmp(ifname, "p2p")) {
    	/* Note: for cfg80211 rfkill issue,
    		  *           the power off sequence shall be garanteed,
		  *	1. remove p2p module
		  *   2. power off wlan
    		  */
        if(false == isP2pActive) {
            LOGE("P2P driver is already unloaded, no need to unload again.");
		} else {
			LOGD("Start unload P2P driver.");
			/*unload p2p driver*/
			sDriverCtrl->unload(NETWORK_IFACE_P2P);
		}
        /*if wifi is not on, turn off power*/
        if(false == isWifiActive){
            powerOff();
        }
        isP2pActive = false;
        return 0;
    }
    return -1;
}

void HaldController::powerOff() {
	LOGD("Power off wlan.");
    sRfkillCtrl->setWifiState(0);
}

void HaldController::powerOn() {
	LOGD("Power on wlan.");
    sRfkillCtrl->setWifiState(1);
}

/*check wifi or sub function is active or not*/
bool HaldController::isFuncActive() {
    return (isWifiActive || isP2pActive || isHotspotActive);
}

