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

#ifndef WVMMEDIA_SOURCE_H_
#define WVMMEDIA_SOURCE_H_

#include "AndroidConfig.h"
#include "WVStreamControlAPI.h"
#include <media/stagefright/DataSource.h>
#include <media/stagefright/MediaSource.h>
#include <media/stagefright/MetaData.h>
#include <media/stagefright/MediaBufferGroup.h>
#include <utils/RefBase.h>
#ifdef REQUIRE_SECURE_BUFFERS
#include "OEMCrypto_L1.h"
#endif


namespace android {

class WVMFileSource;

class WVMMediaSource : public MediaSource {
public:
    WVMMediaSource(WVSession *session, WVEsSelector esSelector,
                   const sp<MetaData> &metaData, bool isLive);

    void delegateFileSource(sp<WVMFileSource> fileSource);
    void delegateDataSource(sp<DataSource> dataSource);

    virtual status_t start(MetaData *params = NULL);
    virtual status_t stop();

    virtual sp<MetaData> getFormat();

    virtual status_t setBuffers(const Vector<MediaBuffer *> &buffers);
    virtual status_t read(MediaBuffer **buffer, const ReadOptions *options = NULL);

    static int sLastError;

#ifdef REQUIRE_SECURE_BUFFERS
    class DecryptContext {
    public:
        void Initialize(MediaBuffer *mediaBuf) {
            mMediaBuf = mediaBuf;
            mOffset = 0;
            memset(mIV, 0, sizeof(mIV));
        }
        MediaBuffer *mMediaBuf;
        size_t mOffset;
        static const int kCryptoBlockSize = 16;
        unsigned char mIV[kCryptoBlockSize];
    };
    static void DecryptCallback(WVEsSelector esType, void* input, void* output,
                                size_t length, int key);
    static DecryptContext sDecryptContext[2]; // audio vs. video
#endif

protected:
    virtual ~WVMMediaSource();

private:
    Mutex mLock;

    WVSession *mSession;
    WVEsSelector mESSelector;  // indicates audio vs. video

    sp<MetaData> mTrackMetaData;

    bool mStarted;
    bool mLogOnce;
    bool mIsLiveStream;
    bool mNewSegment;

    MediaBufferGroup *mGroup;

    unsigned long long mDts;
    unsigned long long mPts;

    sp<WVMFileSource> mFileSource;
    sp<DataSource> mDataSource;

    void allocBufferGroup();

    WVMMediaSource(const WVMMediaSource &);
    WVMMediaSource &operator=(const WVMMediaSource &);
};

}  // namespace android

#endif // WVMMEDIA_SOURCE_H_
