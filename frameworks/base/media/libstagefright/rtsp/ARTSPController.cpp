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

#ifndef ANDROID_DEFAULT_CODE 
// for INT64_MAX 
#undef __STRICT_ANSI__
#define __STDINT_LIMITS
#define __STDC_LIMIT_MACROS
#include <stdint.h>
#endif // #ifndef ANDROID_DEFAULT_CODE
#include "ARTSPController.h"

#include "MyHandler.h"

#include <media/stagefright/foundation/ADebug.h>
#include <media/stagefright/MediaErrors.h>
#include <media/stagefright/MediaSource.h>
#include <media/stagefright/MetaData.h>
#ifndef ANDROID_DEFAULT_CODE 
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/DataSource.h>
#endif // #ifndef ANDROID_DEFAULT_CODE

namespace android {

ARTSPController::ARTSPController(const sp<ALooper> &looper)
    : mState(DISCONNECTED),
#ifndef ANDROID_DEFAULT_CODE 
      mMetaData(new MetaData),
      mEnableSendPause(true), //haizhen
      m_playStatus(INIT),   //haizhen
#endif // #ifndef ANDROID_DEFAULT_CODE
      mLooper(looper),
      mUIDValid(false),
      mSeekDoneCb(NULL),
      mSeekDoneCookie(NULL),
      mLastSeekCompletedTimeUs(-1) {
    mReflector = new AHandlerReflector<ARTSPController>(this);
    looper->registerHandler(mReflector);
#ifndef ANDROID_DEFAULT_CODE 
	LOGI("====================================="); 
    LOGI("[RTSP Playback capability info]: "); 
    LOGI("====================================="); 
#if defined(MT6575) || defined(MT6577) 
    LOGI("Resolution = \"[(8,8) ~ (1280,720)]\""); 
    LOGI("Support Codec = \"Video:MPEG4, H263, H264 ; Audio: AAC, AMR-NB/WB\""); 
    LOGI("Profile_Level = \"MPEG4: SP/ASP ; H263: Baseline ; H264: BL/MP*/HP* level 3.1\""); 
    LOGI("Max Bitrate  = 4Mbps"); 
#else
    LOGI("Resolution = \"[(8,8) ~ (864,480)]\""); 
    LOGI("Support Codec = \"Video:MPEG4, H263, H264 ; Audio: AAC, AMR-NB/WB\""); 
    LOGI("Profile_Level = \"MPEG4: Simple Profile ; H263: Baseline ; H264: Baseline/3.1, Main/3.1\""); 
    LOGI("Max Bitrate  = H264: 6Mbps  (720*480@30fps) ; MPEG4/H263: 20Mbps (864*480@30fps)"); 
#endif
    LOGI("=====================================\n"); 
#endif // #ifndef ANDROID_DEFAULT_CODE
}

ARTSPController::~ARTSPController() {
    CHECK_EQ((int)mState, (int)DISCONNECTED);
    mLooper->unregisterHandler(mReflector->id());
}

void ARTSPController::setUID(uid_t uid) {
    mUIDValid = true;
    mUID = uid;
}

#ifndef ANDROID_DEFAULT_CODE 
status_t ARTSPController::connect(const char *url,
        const KeyedVector<String8, String8> *headers,
        sp<ASessionDescription> desc) {
#else
status_t ARTSPController::connect(const char *url) {
#endif
    Mutex::Autolock autoLock(mLock);

    if (mState != DISCONNECTED) {
        return ERROR_ALREADY_CONNECTED;
    }

    sp<AMessage> msg = new AMessage(kWhatConnectDone, mReflector->id());

#ifndef ANDROID_DEFAULT_CODE 
    mHandler = new MyHandler(url, mLooper, mUIDValid, mUID, headers);
    if (desc != NULL) {
        status_t err = mHandler->setSessionDesc(desc);
        if (err != OK)
            return err;
    } 
#else
    mHandler = new MyHandler(url, mLooper, mUIDValid, mUID);
#endif // #ifndef ANDROID_DEFAULT_CODE

    mState = CONNECTING;

    mHandler->connect(msg);

    while (mState == CONNECTING) {
        mCondition.wait(mLock);
    }

    if (mState != CONNECTED) {
        mHandler.clear();
    }

#ifndef ANDROID_DEFAULT_CODE 
    if (mConnectionResult == OK) {
        mMetaData->setInt32(kKeyServerTimeout, mHandler->getServerTimeout());

        AString val;
        sp<ASessionDescription> desc = mHandler->getSessionDesc();
        if (desc->findAttribute(0, "s=", &val)) {
            LOGI("rtsp s=%s ", val.c_str());
            mMetaData->setCString(kKeyTitle, val.c_str());
        }

        if (desc->findAttribute(0, "i=", &val)) {
            LOGI("rtsp i=%s ", val.c_str());
            mMetaData->setCString(kKeyAuthor, val.c_str());
        }
    }
    int v;
    if (msg->findInt32("unsupport-video", &v) && v) {
        mMetaData->setInt32(kKeyHasUnsupportVideo, true);
    }
#endif // #ifndef ANDROID_DEFAULT_CODE
    return mConnectionResult;
}

void ARTSPController::disconnect() {
    Mutex::Autolock autoLock(mLock);

    if (mState == CONNECTING) {
        mState = DISCONNECTED;
        mConnectionResult = ERROR_IO;
        mCondition.broadcast();

        mHandler.clear();
        return;
    } else if (mState != CONNECTED) {
        return;
    }

    sp<AMessage> msg = new AMessage(kWhatDisconnectDone, mReflector->id());
    mHandler->disconnect(msg);

    while (mState == CONNECTED) {
        mCondition.wait(mLock);
    }

    mHandler.clear();
}

#ifndef ANDROID_DEFAULT_CODE // haizhen
status_t ARTSPController::sendPause() {
	LOGI("[rtsp]ARTSPController::sendPause!!!");
    Mutex::Autolock autoLock(mLock);
	/*if use Async there will be some conflict
	sp<AMessage> reply = new AMessage(kWhatPauseDone, mReflector->id());
	 mHandler->sendPause(reply);*/
	if(mEnableSendPause && (m_playStatus != STOPPED)&&(m_playStatus != PAUSED)){
		mHandler->sendPause(prepareSyncCall());
		int32_t pauseDoneRes;
		pauseDoneRes = finishSyncCall(false);
   		if( pauseDoneRes == OK){
			m_playStatus = PAUSED;
			LOGI("[rtsp]Send Pause Ok");
		}
		else if(pauseDoneRes == ALREADY_EXISTS){
			LOGE("[rtsp]Send Pause too frequently\n");
			
		}
		else if(pauseDoneRes == INVALID_OPERATION){
			LOGE("[rtsp]Pause is not valid!!!\n");
		}
		else
			LOGE("[rtsp]Server return fail for Pause, will abort!!!\n");
		
		return pauseDoneRes;
		
	}
	else
		return OK;   
   
}

#endif // #ifndef ANDROID_DEFAULT_CODE



#ifndef ANDROID_DEFAULT_CODE 
status_t ARTSPController::sendPlay() {
    Mutex::Autolock autoLock(mLock);
	if(m_playStatus != PLAYING) {   //haizhen
		status_t playRes;
		if(m_playStatus == PAUSED){
			mHandler->play(prepareSyncCall(),true);
			playRes = finishSyncCall(false);
		}
		else if(m_playStatus == STOPPED){
				LOGE("[rtsp]SendPlay after stopped!!!");
				mHandler->play(prepareSyncCall());
				playRes = finishSyncCall(false);
			}else{
                                mHandler->play(prepareSyncCall());
				playRes = finishSyncCall(false);
			}
		if(playRes == OK){
			m_playStatus = PLAYING;
		}
		return playRes;
	}
	return OK;
   
}

status_t ARTSPController::preSeek(
        int64_t timeUs,
        void (*seekDoneCb)(void *), void *cookie) {
    Mutex::Autolock autoLock(mLock);
    bool tooEarly =
        mLastSeekCompletedTimeUs >= 0
            && ALooper::GetNowUs() < mLastSeekCompletedTimeUs + 500000ll;
#ifdef MTK_BSP_PACKAGE
     //cancel  ignore seek --do every seek for bsp package
     // because ignore seek and notify seek complete will cause progress return back
     tooEarly = false;
#endif
    bool needCallback = mState != CONNECTED || tooEarly;
    status_t err = ALREADY_EXISTS;
    if (!needCallback) {
        mHandler->preSeek(timeUs, prepareSyncCall());
        err = finishSyncCall(false);
        LOGI("ARTSPController::preSeek end err=%d",err);  
    }
    if (needCallback || err == INVALID_OPERATION) {
          LOGW("not do seek really, needCallback=%d,err=%d",needCallback,err);
        (*seekDoneCb)(cookie);
    }
    return err;
}

sp<AMessage> ARTSPController::prepareSyncCall() {
    mSyncCallResult = OK;
    mSyncCallDone = false;
    sp<AMessage> msg = new AMessage(kWhatSyncCallDone, mReflector->id());
    // initialize result as error
    msg->setInt32("result", UNKNOWN_ERROR);
    return msg;
}

status_t ARTSPController::finishSyncCall(bool clearOnError) {
    while(mSyncCallDone == false) {
        mCondition.wait(mLock);
    }

    if (mSyncCallResult != OK && clearOnError) {
        mState = DISCONNECTED;
        stop();
        mHandler.clear();
    }

    return mSyncCallResult;
}
#endif // #ifndef ANDROID_DEFAULT_CODE

void ARTSPController::seekAsync(
        int64_t timeUs,
        void (*seekDoneCb)(void *), void *cookie) {
    Mutex::Autolock autoLock(mLock);

    CHECK(seekDoneCb != NULL);
    CHECK(mSeekDoneCb == NULL);

    // Ignore seek requests that are too soon after the previous one has
    // completed, we don't want to swamp the server.

#ifdef ANDROID_DEFAULT_CODE 
    // check this in preSeek
    bool tooEarly =
        mLastSeekCompletedTimeUs >= 0
            && ALooper::GetNowUs() < mLastSeekCompletedTimeUs + 500000ll;

    if (mState != CONNECTED || tooEarly) {
        (*seekDoneCb)(cookie);
        return;
    }
#endif // #ifndef ANDROID_DEFAULT_CODE

    mSeekDoneCb = seekDoneCb;
    mSeekDoneCookie = cookie;

    sp<AMessage> msg = new AMessage(kWhatSeekDone, mReflector->id());
    mHandler->seek(timeUs, msg);
#ifndef ANDROID_DEFAULT_CODE //haizhen
	m_playStatus = PLAYING;
#endif
}

size_t ARTSPController::countTracks() {
    if (mHandler == NULL) {
        return 0;
    }

    return mHandler->countTracks();
}

sp<MediaSource> ARTSPController::getTrack(size_t index) {
    CHECK(mHandler != NULL);

    return mHandler->getPacketSource(index);
}

sp<MetaData> ARTSPController::getTrackMetaData(
        size_t index, uint32_t flags) {
    CHECK(mHandler != NULL);

    return mHandler->getPacketSource(index)->getFormat();
}

void ARTSPController::onMessageReceived(const sp<AMessage> &msg) {
    switch (msg->what()) {
        case kWhatConnectDone:
        {
            Mutex::Autolock autoLock(mLock);

            CHECK(msg->findInt32("result", &mConnectionResult));
            mState = (mConnectionResult == OK) ? CONNECTED : DISCONNECTED;

            mCondition.signal();
            break;
        }

        case kWhatDisconnectDone:
        {
            Mutex::Autolock autoLock(mLock);
            mState = DISCONNECTED;
            mCondition.signal();
            break;
        }

        case kWhatSeekDone:
        {
            LOGI("seek done");

            mLastSeekCompletedTimeUs = ALooper::GetNowUs();

            void (*seekDoneCb)(void *) = mSeekDoneCb;
            mSeekDoneCb = NULL;

            (*seekDoneCb)(mSeekDoneCookie);
            break;
        }

#ifndef ANDROID_DEFAULT_CODE 
        case kWhatSyncCallDone:
        {
            Mutex::Autolock autoLock(mLock);
            CHECK(msg->findInt32("result", &mSyncCallResult));
            mSyncCallDone = true;
            mCondition.signal();
            break;
        }
#endif // #ifndef ANDROID_DEFAULT_CODE

        default:
            TRESPASS();
            break;
    }
}

#ifndef ANDROID_DEFAULT_CODE 
int64_t ARTSPController::getNormalPlayTimeUs(int64_t timeUs) {
    CHECK(mHandler != NULL);
    return mHandler->getNormalPlayTimeUs(timeUs);
}
#else
int64_t ARTSPController::getNormalPlayTimeUs() {
    CHECK(mHandler != NULL);
    return mHandler->getNormalPlayTimeUs();
}
#endif // #ifndef ANDROID_DEFAULT_CODE

int64_t ARTSPController::getQueueDurationUs(bool *eos) {
    *eos = true;

#ifndef ANDROID_DEFAULT_CODE 
    int64_t minQueuedDurationUs = INT64_MAX;
#else
    int64_t minQueuedDurationUs = 0;
#endif // #ifndef ANDROID_DEFAULT_CODE
    for (size_t i = 0; i < mHandler->countTracks(); ++i) {
        sp<APacketSource> source = mHandler->getPacketSource(i);

        bool newEOS;
        int64_t queuedDurationUs = source->getQueueDurationUs(&newEOS);

        if (!newEOS) {
            *eos = false;
        }

#ifndef ANDROID_DEFAULT_CODE 
        // don't let the EOS stream block buffering
        if (!newEOS && queuedDurationUs < minQueuedDurationUs) {
            minQueuedDurationUs = queuedDurationUs;
        }
#else
        if (i == 0 || queuedDurationUs < minQueuedDurationUs) {
            minQueuedDurationUs = queuedDurationUs;
        }
#endif // #ifndef ANDROID_DEFAULT_CODE
    }

    return minQueuedDurationUs;
}

#ifndef ANDROID_DEFAULT_CODE 
sp<MetaData> ARTSPController::getMetaData() {
    return mMetaData;
}

uint32_t ARTSPController::flags() const {
    int64_t durationUs;
    if (mHandler->getSessionDesc()->getDurationUs(&durationUs))
        return CAN_SEEK_BACKWARD | CAN_SEEK_FORWARD | CAN_SEEK | CAN_PAUSE;
    else
        return 0;
}

void ARTSPController::stopRequests() {
    Mutex::Autolock autoLock(mLock);
    if (mHandler == NULL)
        return;

    if (mState == CONNECTING) {
        mState = DISCONNECTED;
        mConnectionResult = FAILED_TRANSACTION;
        mHandler->exit();
        mCondition.signal();
    } else {
        mSyncCallDone = true;
        mSyncCallResult = FAILED_TRANSACTION;
        mCondition.signal();
    }
}

void ARTSPController::stop() {
    if (mHandler == NULL)
        return;
    for (size_t i = 0; i < mHandler->countTracks(); ++i) {
        sp<APacketSource> source = mHandler->getPacketSource(i);
        source->flushQueue();
        source->signalEOS(ERROR_END_OF_STREAM);
    }
    mHandler->stopTCPTrying();
	m_playStatus = STOPPED;
}

SDPExtractor::SDPExtractor(const sp<DataSource> &source)
    :mMetaData(new MetaData), mSessionDesc(new ASessionDescription)
{
    off64_t fileSize;
    if (source->getSize(&fileSize) != OK) {
        fileSize = 4096 * 2;
        LOGW("no lenth of SDP, try max of %lld", fileSize);
    }

    void* data = malloc(fileSize);
    if (data != NULL) {
        ssize_t n = source->readAt(0, data, fileSize);
        if (n > 0) {
            if (n != fileSize) {
                LOGW("data read may be incomplete %d vs %lld", (int)n, fileSize);
            }
            mSessionDesc->setTo(data, n);
        }
        free(data);
    } else {
        LOGW("out of memory in SDPExtractor");
    }

    mMetaData->setCString(kKeyMIMEType, MEDIA_MIMETYPE_APPLICATION_SDP);
    mMetaData->setPointer(kKeySDP, mSessionDesc.get());
}

size_t SDPExtractor::countTracks() {
    return 0;
}

sp<MediaSource> SDPExtractor::getTrack(size_t index) {
    return NULL;
}

sp<MetaData> SDPExtractor::getTrackMetaData(size_t index, uint32_t flags) {
    return NULL;
}

sp<MetaData> SDPExtractor::getMetaData() {
    return mMetaData;
}

bool SniffSDP(
        const sp<DataSource> &source, String8 *mimeType, float *confidence,
        sp<AMessage> *meta) {
    const int testLen = 7;
    uint8_t line[testLen];
    ssize_t n = source->readAt(0, line, testLen);
    if (n < testLen)
        return false;

    const char* nline = "v=0\no=";
    const char* rnline = "v=0\r\no=";

    if (!memcmp(line, nline, sizeof(nline) - 1) ||
            !memcmp(line, rnline, sizeof(rnline) - 1)) {
        *mimeType = MEDIA_MIMETYPE_APPLICATION_SDP;
        *confidence = 0.5;
        return true;
    }

    return false;
}
#endif // #ifndef ANDROID_DEFAULT_CODE

}  // namespace android
