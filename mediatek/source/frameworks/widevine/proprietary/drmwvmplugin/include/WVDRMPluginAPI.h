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
 * Copyright 2011 Widevine Technologies, Inc., All Rights Reserved
 *
 * Declarations for Widevine DRM Plugin API
 */

#ifndef __WVMDRMPLUGIN_API_H__
#define __WVMDRMPLUGIN_API_H__

#include <string>
#include "WVStreamControlAPI.h"

class WVDRMPluginAPI {
 public:
    virtual ~WVDRMPluginAPI() {}

    enum {
        RIGHTS_VALID,
        RIGHTS_INVALID,
        RIGHTS_EXPIRED,
        RIGHTS_NOT_ACQUIRED
    };

    enum {
        PLAYBACK_START,
        PLAYBACK_STOP,
        PLAYBACK_PAUSE,
        PLAYBACK_INVALID
    };

    static const int PlaybackMode_Default = 0;
    static const int PlaybackMode_Streaming = 1;
    static const int PlaybackMode_Offline = 2;
    static const int PlaybackMode_Any = PlaybackMode_Streaming |
                                        PlaybackMode_Offline;

    static WVDRMPluginAPI *create();
    static void destroy(WVDRMPluginAPI *plugin);

    virtual bool OpenSession(const char *uri) = 0;
    virtual void CloseSession() = 0;
    virtual bool IsSupportedMediaType(const char *uri) = 0;

    virtual bool RegisterDrmInfo(std::string &portal, std::string &dsPath) = 0;
    virtual bool UnregisterDrmInfo(std::string &portal, std::string &dsPath) = 0;
    virtual bool AcquireDrmInfo(std::string &assetPath, WVCredentials &credentials,
                                std::string &dsPath, const std::string &systemIdStr,
                                const std::string &assetIdStr,
                                const std::string &keyIdStr,
                                uint32_t *systemId, uint32_t *assetId,
                                uint32_t *keyId) = 0;

    virtual bool ProcessDrmInfo(std::string &assetPath, int playbackMode) = 0;
    virtual int CheckRightsStatus(std::string &path) = 0;

    virtual bool GetConstraints(std::string &path, uint32_t *timeSincePlayback, 
                                uint32_t *timeRemaining,
                                uint32_t *licenseDuration, std::string &lastError,
                                bool &allowOffline, bool &allowStreaming, 
                                bool &denyHD) = 0;

    virtual bool SetPlaybackStatus(int playbackStatus, off64_t position) = 0;
    virtual bool RemoveRights(std::string &path) = 0;
    virtual bool RemoveAllRights() = 0;
    virtual bool Prepare(char *data, int len) = 0;
    virtual int Operate(char *in, char *out, int len, char *iv) = 0;

    enum EventType {
        EventType_AcquireDrmInfoFailed,
        EventType_ProcessDrmInfoFailed,
        EventType_RightsInstalled,
        EventType_RightsRemoved,

        EventType_HeartbeatServer,
        EventType_HeartbeatPeriod,
        EventType_AssetId,
        EventType_DeviceId,
        EventType_StreamId,
        EventType_UserData
    };

    enum EventDestination {
        EventDestination_JavaAPI,
        EventDestination_MediaPlayer
    };

    // Returns true if event sent, false if no handler
    typedef bool (*EventHandler)(EventType type, EventDestination destination,
                                 const std::string &path);
    virtual void SetEventHandler(EventHandler handler) = 0;

protected:
    // use create factory method, don't construct directly
    WVDRMPluginAPI() {}
};

#endif
