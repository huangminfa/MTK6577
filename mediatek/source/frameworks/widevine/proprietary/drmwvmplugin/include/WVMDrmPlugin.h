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
 * Copyright (C) 2011 The Android Open Source Project
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

#ifndef __WVMDRMPLUGIN_H__
#define __WVMDRMPLUGIN_H__


#include <AndroidConfig.h>
#include <DrmEngineBase.h>

#include "WVDRMPluginAPI.h"


namespace android {

    class WVMDrmPlugin : public DrmEngineBase
    {

public:
    WVMDrmPlugin();
    virtual ~WVMDrmPlugin();

protected:
    DrmConstraints* onGetConstraints(int uniqueId, const String8* path, int action);

    DrmMetadata* onGetMetadata(int uniqueId, const String8* path);

    status_t onInitialize(int uniqueId);

    status_t onSetOnInfoListener(int uniqueId, const IDrmEngine::OnInfoListener* infoListener);

    status_t onTerminate(int uniqueId);

    bool onCanHandle(int uniqueId, const String8& path);

    DrmInfoStatus* onProcessDrmInfo(int uniqueId, const DrmInfo* drmInfo);

    status_t onSaveRights(int uniqueId, const DrmRights& drmRights,
            const String8& rightsPath, const String8& contentPath);

    DrmInfo* onAcquireDrmInfo(int uniqueId, const DrmInfoRequest* drmInfoRequest);

    String8 onGetOriginalMimeType(int uniqueId, const String8& path);

    int onGetDrmObjectType(int uniqueId, const String8& path, const String8& mimeType);

    int onCheckRightsStatus(int uniqueId, const String8& path, int action);

    status_t onConsumeRights(int uniqueId, DecryptHandle* decryptHandle, int action, bool reserve);

    status_t onSetPlaybackStatus(
            int uniqueId, DecryptHandle* decryptHandle, int playbackStatus, off64_t position);

    bool onValidateAction(
            int uniqueId, const String8& path, int action, const ActionDescription& description);

    status_t onRemoveRights(int uniqueId, const String8& path);

    status_t onRemoveAllRights(int uniqueId);

    status_t onOpenConvertSession(int uniqueId, int convertId);

    DrmConvertedStatus* onConvertData(int uniqueId, int convertId, const DrmBuffer* inputData);

    DrmConvertedStatus* onCloseConvertSession(int uniqueId, int convertId);

    DrmSupportInfo* onGetSupportInfo(int uniqueId);

    status_t onOpenDecryptSession(int uniqueId, DecryptHandle *decryptHandle,
                                        int fd, off64_t offset, off64_t length);

    status_t onOpenDecryptSession(int uniqueId, DecryptHandle *decryptHandle,
                                  const char *uri);

    status_t onCloseDecryptSession(int uniqueId, DecryptHandle* decryptHandle);

    status_t onInitializeDecryptUnit(int uniqueId, DecryptHandle* decryptHandle,
                                     int decryptUnitId, const DrmBuffer* headerInfo);

    status_t onDecrypt(int uniqueId, DecryptHandle* decryptHandle, int decryptUnitId,
                       const DrmBuffer* encBuffer, DrmBuffer** decBuffer,
                       DrmBuffer *ivBuffer);


    status_t onFinalizeDecryptUnit(int uniqueId, DecryptHandle* decryptHandle, int decryptUnitId);

    ssize_t onPread(int uniqueId, DecryptHandle* decryptHandle,
            void* buffer, ssize_t numBytes, off64_t offset);

    class Listener {
    public:
        Listener() : mListener(NULL), mUniqueId(-1) {}

        Listener(IDrmEngine::OnInfoListener *listener, int uniqueId)
            : mListener(listener), mUniqueId(uniqueId) {};

        IDrmEngine::OnInfoListener *GetListener() const {return mListener;}
        int GetUniqueId() const {return mUniqueId;}

    private:
        IDrmEngine::OnInfoListener *mListener;
        int mUniqueId;
    };

    enum MessageType {
        MessageType_HeartbeatServer = 4000,
        MessageType_HeartbeatPeriod = 4001,
        MessageType_AssetId = 4002,
        MessageType_DeviceId = 4003,
        MessageType_StreamId = 4004,
        MessageType_UserData = 4005
    };

private:
    static bool SendEvent(WVDRMPluginAPI::EventType code, WVDRMPluginAPI::EventDestination dest,
                          const std::string &path);

    static Vector<Listener> *sNativeListeners;
    static Vector<Listener> *sJavaAPIListeners;

    WVDRMPluginAPI *mDrmPluginImpl;
};

};

#endif /* __WVMDRMPLUGIN__ */

