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

#ifndef A_RTSP_CONTROLLER_H_

#define A_RTSP_CONTROLLER_H_

#ifndef ANDROID_DEFAULT_CODE 
#include <utils/String8.h>
#include "../rtsp/ASessionDescription.h"
#endif // #ifndef ANDROID_DEFAULT_CODE
#include <media/stagefright/foundation/ABase.h>
#include <media/stagefright/foundation/AHandlerReflector.h>
#include <media/stagefright/MediaExtractor.h>

namespace android {

struct ALooper;
struct MyHandler;

struct ARTSPController : public MediaExtractor {
    ARTSPController(const sp<ALooper> &looper);

    void setUID(uid_t uid);

#ifndef ANDROID_DEFAULT_CODE 
    status_t connect(const char *url,
            const KeyedVector<String8, String8> *headers = NULL, sp<ASessionDescription> desc = NULL);
#else
    status_t connect(const char *url);
#endif // #ifndef ANDROID_DEFAULT_CODE
    void disconnect();

#ifndef ANDROID_DEFAULT_CODE 
    status_t sendPlay();
    status_t sendPause(); //haizhen
    // preSeek must be called before seekAsync
    status_t preSeek(int64_t timeUs, void (*seekDoneCb)(void *), void *cookie);
    void stopRequests();
    void stop();
#endif // #ifndef ANDROID_DEFAULT_CODE
    void seekAsync(int64_t timeUs, void (*seekDoneCb)(void *), void *cookie);

    virtual size_t countTracks();
    virtual sp<MediaSource> getTrack(size_t index);

    virtual sp<MetaData> getTrackMetaData(
            size_t index, uint32_t flags);

#ifndef ANDROID_DEFAULT_CODE 
    virtual sp<MetaData> getMetaData();
    int64_t getNormalPlayTimeUs(int64_t timeUs = 0);
#else
    int64_t getNormalPlayTimeUs();
#endif // #ifndef ANDROID_DEFAULT_CODE
    int64_t getQueueDurationUs(bool *eos);

    void onMessageReceived(const sp<AMessage> &msg);

#ifndef ANDROID_DEFAULT_CODE 
    // move this to ARTSPController.cpp
    virtual uint32_t flags() const;
#else
    virtual uint32_t flags() const {
        // Seeking 10secs forward or backward is a very expensive operation
        // for rtsp, so let's not enable that.
        // The user can always use the seek bar.

        return CAN_PAUSE | CAN_SEEK;
    }
#endif // #ifndef ANDROID_DEFAULT_CODE

protected:
    virtual ~ARTSPController();

private:
    enum {
        kWhatConnectDone    = 'cdon',
        kWhatDisconnectDone = 'ddon',
        kWhatSeekDone       = 'sdon',
#ifndef ANDROID_DEFAULT_CODE 
        kWhatSyncCallDone   = 'ndon',
#endif // #ifndef ANDROID_DEFAULT_CODE
    };

    enum State {
        DISCONNECTED,
        CONNECTED,
        CONNECTING,
    };

#ifndef ANDROID_DEFAULT_CODE  //haizhen
   enum PlayStatus{
	INIT = 0,
	PAUSED,
	PLAYING,
	STOPPED,		
    };
#endif

    Mutex mLock;
    Condition mCondition;
#ifndef ANDROID_DEFAULT_CODE 
    status_t mSyncCallResult;
    bool mSyncCallDone;
    status_t finishSyncCall(bool clearOnError = true);
    sp<AMessage> prepareSyncCall();
#endif // #ifndef ANDROID_DEFAULT_CODE

    State mState;
#ifndef ANDROID_DEFAULT_CODE 
    sp<MetaData> mMetaData;
	bool mEnableSendPause; //haizhen
	PlayStatus m_playStatus; //haizhen
#endif // #ifndef ANDROID_DEFAULT_CODE
    status_t mConnectionResult;

    sp<ALooper> mLooper;
    sp<MyHandler> mHandler;
    sp<AHandlerReflector<ARTSPController> > mReflector;

    bool mUIDValid;
    uid_t mUID;

    void (*mSeekDoneCb)(void *);
    void *mSeekDoneCookie;
    int64_t mLastSeekCompletedTimeUs;

    DISALLOW_EVIL_CONSTRUCTORS(ARTSPController);
};

#ifndef ANDROID_DEFAULT_CODE 
class SDPExtractor : public MediaExtractor {
    public:
        SDPExtractor(const sp<DataSource> &source);
        virtual size_t countTracks();
        virtual sp<MediaSource> getTrack(size_t index);
        virtual sp<MetaData> getTrackMetaData(
                size_t index, uint32_t flags = 0);
        virtual sp<MetaData> getMetaData();
    private:
        sp<MetaData> mMetaData;
        sp<ASessionDescription> mSessionDesc;
    DISALLOW_EVIL_CONSTRUCTORS(SDPExtractor);
};

bool SniffSDP(
        const sp<DataSource> &source, String8 *mimeType, float *confidence,
        sp<AMessage> *meta);
#endif // #ifndef ANDROID_DEFAULT_CODE
}  // namespace android

#endif  // A_RTSP_CONTROLLER_H_
