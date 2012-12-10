
/*
 * Copyright (C) 2009 The Android Open Source Project
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

#undef DEBUG_HDCP

//#define LOG_NDEBUG 0
#ifndef ANDROID_DEFAULT_CODE
// for INT64_MAX
#undef __STRICT_ANSI__
#define __STDINT_LIMITS
#define __STDC_LIMIT_MACROS
#include <stdint.h>
#endif // #ifndef ANDROID_DEFAULT_CODE

//MTK_OP01_PROTECT_START
 #ifndef ANDROID_DEFAULT_CODE    
//cmmb added.
#include <utils/threads.h>
#include <utils/Errors.h>
#include <pthread.h>
#include <media/stagefright/ColorConverter.h>
#include <sys/resource.h>
#include <core/SkBitmap.h>
#include "SkImageEncoder.h"

#ifdef MTK_CMMB_SUPPORT
#include "CMMBExtractor.h"
#include "CMMBDataSource.h"
#endif
#endif
//MTK_OP01_PROTECT_END


#define LOG_TAG "AwesomePlayer"
#include <utils/Log.h>

#include <dlfcn.h>
#include <linux/rtpm_prio.h>
#include "include/ARTSPController.h"
#include "include/AwesomePlayer.h"
#include "include/DRMExtractor.h"
#include "include/SoftwareRenderer.h"
#include "include/NuCachedSource2.h"
#include "include/ThrottledSource.h"
#include "include/MPEG2TSExtractor.h"
#include "include/WVMExtractor.h"

#include "timedtext/TimedTextPlayer.h"

#include <binder/IPCThreadState.h>
#include <binder/IServiceManager.h>
#include <media/IMediaPlayerService.h>
#include <media/stagefright/foundation/hexdump.h>
#include <media/stagefright/foundation/ADebug.h>
#include <media/stagefright/AudioPlayer.h>
#include <media/stagefright/DataSource.h>
#include <media/stagefright/FileSource.h>
#include <media/stagefright/MediaBuffer.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MediaExtractor.h>
#include <media/stagefright/MediaSource.h>
#include <media/stagefright/MetaData.h>
#include <media/stagefright/OMXCodec.h>

#include <surfaceflinger/Surface.h>
#include <gui/ISurfaceTexture.h>
#include <gui/SurfaceTextureClient.h>
#include <surfaceflinger/ISurfaceComposer.h>

#include <media/stagefright/foundation/ALooper.h>
#include <media/stagefright/foundation/AMessage.h>

#include <cutils/properties.h>

#ifdef MTK_DRM_APP
#include <drm/DrmMtkUtil.h>
#include <drm/DrmMtkDef.h>
#endif

#ifndef ANDROID_DEFAULT_CODE
int64_t getTickCountMs()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (int64_t)(tv.tv_sec*1000LL + tv.tv_usec/1000);
}

int64_t getTickCountUs()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (int64_t)(tv.tv_sec*1000000LL + tv.tv_usec);
}

#define SF_SHOW_FPS (1 << 0)
#define SF_POST_BUFFER_PROFILING (1 << 1)
#endif

#define USE_SURFACE_ALLOC 1
#define FRAME_DROP_FREQ 0

namespace android {

#ifndef ANDROID_DEFAULT_CODE
static int64_t kRTSPEarlyEndTimeUs = 3000000ll; // 3secs
#endif // #ifndef ANDROID_DEFAULT_CODE

static int64_t kLowWaterMarkUs = 2000000ll;  // 2secs
#ifndef ANDROID_DEFAULT_CODE
static int64_t kHighWaterMarkUs = 10000000ll;  // 10secs
#else
static int64_t kHighWaterMarkUs = 5000000ll;  // 5secs
#endif
static int64_t kHighWaterMarkRTSPUs = 4000000ll;  // 4secs
static const size_t kLowWaterMarkBytes = 40000;
static const size_t kHighWaterMarkBytes = 200000;

struct AwesomeEvent : public TimedEventQueue::Event {
    AwesomeEvent(
            AwesomePlayer *player,
            void (AwesomePlayer::*method)())
        : mPlayer(player),
          mMethod(method) {
    }

protected:
    virtual ~AwesomeEvent() {}

    virtual void fire(TimedEventQueue *queue, int64_t /* now_us */) {
        (mPlayer->*mMethod)();
    }

private:
    AwesomePlayer *mPlayer;
    void (AwesomePlayer::*mMethod)();

    AwesomeEvent(const AwesomeEvent &);
    AwesomeEvent &operator=(const AwesomeEvent &);
};

struct AwesomeLocalRenderer : public AwesomeRenderer {
    AwesomeLocalRenderer(
            const sp<ANativeWindow> &nativeWindow, const sp<MetaData> &meta)
        : mTarget(new SoftwareRenderer(nativeWindow, meta)) {
    }

    virtual void render(MediaBuffer *buffer) {
        render((const uint8_t *)buffer->data() + buffer->range_offset(),
               buffer->range_length());
    }

    void render(const void *data, size_t size) {
        mTarget->render(data, size, NULL);
    }

protected:
    virtual ~AwesomeLocalRenderer() {
        delete mTarget;
        mTarget = NULL;
    }

private:
    SoftwareRenderer *mTarget;

    AwesomeLocalRenderer(const AwesomeLocalRenderer &);
    AwesomeLocalRenderer &operator=(const AwesomeLocalRenderer &);;
};

struct AwesomeNativeWindowRenderer : public AwesomeRenderer {
    AwesomeNativeWindowRenderer(
            const sp<ANativeWindow> &nativeWindow,
            int32_t rotationDegrees)
        : mNativeWindow(nativeWindow) {
        applyRotation(rotationDegrees);

#ifndef ANDROID_DEFAULT_CODE
        mDbgFlags = 0;
        mFrameCount = 0;
        mFirstPostBufferTime = 0;
        mQueueBufferInTs = 0;
        char value[PROPERTY_VALUE_MAX];
        property_get("sf.showfps", value, "1");	// enable by default temporarily
        bool _res = atoi(value);
        if (_res) mDbgFlags |= SF_SHOW_FPS;

        property_get("sf.postbuffer.prof", value, "0");	// disable by default
        _res = atoi(value);
        if (_res) mDbgFlags |= SF_POST_BUFFER_PROFILING;
#endif
    }

    virtual void render(MediaBuffer *buffer) {
#ifndef ANDROID_DEFAULT_CODE
        if (mDbgFlags & SF_SHOW_FPS) {
            if (0 == mFrameCount) {
                mFirstPostBufferTime = getTickCountMs();
            }
            else {
                if (0 == (mFrameCount % 60)) {        
                    int64_t _diff = getTickCountMs() - mFirstPostBufferTime;
                    double fps = (double)1000*mFrameCount/_diff;
                    LOGD ("FPS = %.2f", fps);
                }                
            }
            mFrameCount++;
        }
#endif        
	
        int64_t timeUs;
        CHECK(buffer->meta_data()->findInt64(kKeyTime, &timeUs));
        native_window_set_buffers_timestamp(mNativeWindow.get(), timeUs * 1000);

#ifndef ANDROID_DEFAULT_CODE
        if (mDbgFlags & SF_POST_BUFFER_PROFILING) {
            mQueueBufferInTs = getTickCountUs();
            LOGD ("+queueBuffer [%d]", mFrameCount);
        }
#endif        
    
        status_t err = mNativeWindow->queueBuffer(
                mNativeWindow.get(), buffer->graphicBuffer().get());
        if (err != 0) {
            LOGE("queueBuffer failed with error %s (%d)", strerror(-err),
                    -err);
            return;
        }

#ifndef ANDROID_DEFAULT_CODE
    if (mDbgFlags & SF_POST_BUFFER_PROFILING) {
        int64_t _out = getTickCountUs() - mQueueBufferInTs;
        LOGD ("-queueBuffer (%lld)", _out);
    }
#endif
    
        sp<MetaData> metaData = buffer->meta_data();
        metaData->setInt32(kKeyRendered, 1);
    }

protected:
    virtual ~AwesomeNativeWindowRenderer() {}

private:
    sp<ANativeWindow> mNativeWindow;

#ifndef ANDROID_DEFAULT_CODE
    uint32_t mDbgFlags;
    uint32_t mFrameCount;
    int64_t mFirstPostBufferTime;
    int64_t mQueueBufferInTs;
#endif

    void applyRotation(int32_t rotationDegrees) {
        uint32_t transform;
        switch (rotationDegrees) {
            case 0: transform = 0; break;
            case 90: transform = HAL_TRANSFORM_ROT_90; break;
            case 180: transform = HAL_TRANSFORM_ROT_180; break;
            case 270: transform = HAL_TRANSFORM_ROT_270; break;
            default: transform = 0; break;
        }

        if (transform) {
            CHECK_EQ(0, native_window_set_buffers_transform(
                        mNativeWindow.get(), transform));
        }
    }

    AwesomeNativeWindowRenderer(const AwesomeNativeWindowRenderer &);
    AwesomeNativeWindowRenderer &operator=(
            const AwesomeNativeWindowRenderer &);
};

// To collect the decoder usage
void addBatteryData(uint32_t params) {
    sp<IBinder> binder =
        defaultServiceManager()->getService(String16("media.player"));
    sp<IMediaPlayerService> service = interface_cast<IMediaPlayerService>(binder);
    CHECK(service.get() != NULL);

    service->addBatteryData(params);
}

////////////////////////////////////////////////////////////////////////////////
AwesomePlayer::AwesomePlayer()
    : mQueueStarted(false),
      mUIDValid(false),
      mTimeSource(NULL),
      mVideoRendererIsPreview(false),
      mAudioPlayer(NULL),
      mDisplayWidth(0),
      mDisplayHeight(0),
      mFlags(0),
      mExtractorFlags(0),
      mVideoBuffer(NULL),
      mDrmManagerClient(NULL),
      mDecryptHandle(NULL),
#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_DRM_APP
      isCurrentComplete(false),
#endif
       mLastPositionUs(-1),
#endif
      mLastVideoTimeUs(-1),
      //MTK_OP01_PROTECT_START
 #ifndef ANDROID_DEFAULT_CODE    
      mCMMBCaptureFinished(true),
      IsCMMBCaptureOneFrame(false),
      IsCMMBPlayer(false),
      IsCMMBFirstFrame(false),
      captureBuffer(NULL),
 #endif     
      //MTK_OP01_PROTECT_END
#ifndef ANDROID_DEFAULT_CODE	  
      mAVSyncThreshold(500000ll),
	  mAVSyncTimeUs(-1),
	  mFRAME_DROP_FREQ(0),
	  mLateMargin(250000ll),
	  mPrerollEnable(true),
      mHighWaterMarkUs(5000000ll),
      mHighWaterMarkRTSPUs(kHighWaterMarkRTSPUs),
      mFinalStopFlag(0),
#if defined(MT6575) || defined(MT6577)
      mFirstSubmit(true),
#endif	 
      mAudioPadEnable(false),
#endif 
      mTextPlayer(NULL)
{
    CHECK_EQ(mClient.connect(), (status_t)OK);

    DataSource::RegisterDefaultSniffers();

    mVideoEvent = new AwesomeEvent(this, &AwesomePlayer::onVideoEvent);
    mVideoEventPending = false;
    mStreamDoneEvent = new AwesomeEvent(this, &AwesomePlayer::onStreamDone);
    mStreamDoneEventPending = false;
    mBufferingEvent = new AwesomeEvent(this, &AwesomePlayer::onBufferingUpdate);
    mBufferingEventPending = false;
    mVideoLagEvent = new AwesomeEvent(this, &AwesomePlayer::onVideoLagUpdate);
    mVideoEventPending = false;

    mCheckAudioStatusEvent = new AwesomeEvent(
            this, &AwesomePlayer::onCheckAudioStatus);
	
#ifndef ANDROID_DEFAULT_CODE
	mDurationUpdateEvent = new AwesomeEvent(this, &AwesomePlayer::OnDurationUpdate);
	mDurationUpdateEventPending = false;
#ifdef MTK_S3D_SUPPORT
	mVideoStereoMode = VIDEO_STEREO_DEFAULT;
#endif
	mEnAudST = 0;
#endif

    mAudioStatusEventPending = false;
	
    //MTK_OP01_PROTECT_START
    //CMMB capture thread
 #ifndef ANDROID_DEFAULT_CODE    
    IsCMMBCaptureStopFlag = false;
 #endif
 //MTK_OP01_PROTECT_END
#ifndef ANDROID_DEFAULT_CODE 
	char jumpvalue[PROPERTY_VALUE_MAX];
    property_get("sf.video.late.jump.key.ms", jumpvalue, "-1");
    mAVSyncThreshold = atol(jumpvalue);
	if(mAVSyncThreshold>0)
	{
		mAVSyncThreshold = mAVSyncThreshold*1000;
		LOGD("@@[SF_PROPERTY]sf.video.jump.key.ms =%lld",mAVSyncThreshold/1000);
	}
	else
	{
		LOGD("@@[SF_PROPERTY]sf.video.jump.key.ms =%lld",mAVSyncThreshold);
	}

	char forcevalue[PROPERTY_VALUE_MAX];
	property_get("sf.video.force.display.cnt", forcevalue, "0");
	mFRAME_DROP_FREQ = atol(forcevalue);
	LOGD("@@[SF_PROPERTY]sf.video.force.display.cnt=%d",mFRAME_DROP_FREQ);


   
    char mLateMargin_value[PROPERTY_VALUE_MAX];
    property_get("sf.video.late.margin.ms", mLateMargin_value, "250");	
    mLateMargin = atoi(mLateMargin_value);
    
	if(mLateMargin>0)
	{
		mLateMargin = mLateMargin*1000;
		LOGD ("@@[SF_PROPERTY]sf.video.late.margin.ms = %d", mLateMargin/1000);
	}
	else
	{
		LOGD ("@@[SF_PROPERTY]sf.video.late.margin.ms = %d", mLateMargin);
	}

#endif




    reset();
}

AwesomePlayer::~AwesomePlayer() {
    if (mQueueStarted) {
        mQueue.stop();
    }
    //MTK_OP01_PROTECT_START
    //CMMB thread destroy.
 #ifndef ANDROID_DEFAULT_CODE    
    //void *dummy;
    IsCMMBCaptureStopFlag = true;
    mCMMBCaptureCondition.signal();
#endif
    //MTK_OP01_PROTECT_END

    reset();

    mClient.disconnect();
}

void AwesomePlayer::cancelPlayerEvents(bool keepNotifications) {
    mQueue.cancelEvent(mVideoEvent->eventID());
    mVideoEventPending = false;
    mQueue.cancelEvent(mVideoLagEvent->eventID());
    mVideoLagEventPending = false;

    if (!keepNotifications) {
        mQueue.cancelEvent(mStreamDoneEvent->eventID());
        mStreamDoneEventPending = false;
        mQueue.cancelEvent(mCheckAudioStatusEvent->eventID());
        mAudioStatusEventPending = false;

        mQueue.cancelEvent(mBufferingEvent->eventID());
        mBufferingEventPending = false;
    }
}

void AwesomePlayer::setListener(const wp<MediaPlayerBase> &listener) {
    Mutex::Autolock autoLock(mLock);
    mListener = listener;
}

void AwesomePlayer::setUID(uid_t uid) {
    LOGV("AwesomePlayer running on behalf of uid %d", uid);

    mUID = uid;
    mUIDValid = true;
}

status_t AwesomePlayer::setDataSource(
        const char *uri, const KeyedVector<String8, String8> *headers) {
    Mutex::Autolock autoLock(mLock);
    return setDataSource_l(uri, headers);
}

status_t AwesomePlayer::setDataSource_l(
        const char *uri, const KeyedVector<String8, String8> *headers) {
    reset_l();

    mUri = uri;

#if 0
    if (headers) {
        mUriHeaders = *headers;
        //print headers
    }
    char value[PROPERTY_VALUE_MAX];
    property_get("debug.streaming.cache", value, "10");
    mUriHeaders.add(String8("MTK-HTTP-CACHE-SIZE"), String8(value));
    LOGD("headers\n");
    for (int i = 0; i < mUriHeaders.size(); i ++) {
        LOGD("\t\t%s: %s", mUriHeaders.keyAt(i).string(), mUriHeaders.valueAt(i).string());
    } 
#else

    if (headers) {
        LOGD("setDataSource headers:\n");
        for (int i = 0; i < headers->size(); i ++) {
            LOGD("\t\t%s: %s", headers->keyAt(i).string(), headers->valueAt(i).string());
        } 
        mUriHeaders = *headers;

//        char value[PROPERTY_VALUE_MAX];
//        property_get("debug.streaming.cache", value, "10");
//        mUriHeaders.add(String8("MTK-HTTP-CACHE-SIZE"), String8(value));

        ssize_t index = mUriHeaders.indexOfKey(String8("x-hide-urls-from-log"));
        if (index >= 0) {
            // Browser is in "incognito" mode, suppress logging URLs.

            // This isn't something that should be passed to the server.
            mUriHeaders.removeItemsAt(index);

            modifyFlags(INCOGNITO, SET);
        }
    }
#endif

    LOGD("setDataSource_l('%s')", mUri.string());

    // The actual work will be done during preparation in the call to
    // ::finishSetDataSource_l to avoid blocking the calling thread in
    // setDataSource for any significant time.

    {
        Mutex::Autolock autoLock(mStatsLock);
        mStats.mFd = -1;
        mStats.mURI = mUri;
    }

    return OK;
}

status_t AwesomePlayer::setDataSource(
        int fd, int64_t offset, int64_t length) {
    Mutex::Autolock autoLock(mLock);

    reset_l();

    sp<DataSource> dataSource = new FileSource(fd, offset, length);

    status_t err = dataSource->initCheck();

    if (err != OK) {
        return err;
    }

    mFileSource = dataSource;

    {
        Mutex::Autolock autoLock(mStatsLock);
        mStats.mFd = fd;
        mStats.mURI = String8();
    }

    return setDataSource_l(dataSource);
}

status_t AwesomePlayer::setDataSource(const sp<IStreamSource> &source) {
    return INVALID_OPERATION;
}

status_t AwesomePlayer::setDataSource_l(
        const sp<DataSource> &dataSource) {
    sp<MediaExtractor> extractor = MediaExtractor::Create(dataSource);

    if (extractor == NULL) {
        return UNKNOWN_ERROR;
    }

    dataSource->getDrmInfo(mDecryptHandle, &mDrmManagerClient);
    if (mDecryptHandle != NULL) {
        CHECK(mDrmManagerClient);
        if (RightsStatus::RIGHTS_VALID != mDecryptHandle->status) {
            notifyListener_l(MEDIA_ERROR, MEDIA_ERROR_UNKNOWN, ERROR_DRM_NO_LICENSE);
        }
    }

    return setDataSource_l(extractor);
}

status_t AwesomePlayer::setDataSource_l(const sp<MediaExtractor> &extractor) {
    // Attempt to approximate overall stream bitrate by summing all
    // tracks' individual bitrates, if not all of them advertise bitrate,
    // we have to fail.
#ifndef ANDROID_DEFAULT_CODE 
		mMetaData = extractor->getMetaData();
#endif

#ifndef ANDROID_DEFAULT_CODE
    void *sdp = NULL;
    if (extractor->getMetaData().get()!= NULL && extractor->getMetaData()->findPointer(kKeySDP, &sdp)) {
        mSessionDesc = (ASessionDescription*)sdp;
        if (!mSessionDesc->isValid())
            return ERROR_MALFORMED;

        if (mSessionDesc->countTracks() == 1u)
            return ERROR_UNSUPPORTED;

        status_t err = mSessionDesc->getSessionUrl(mUri);
        if (err != OK)
            return err;

        if (mConnectingDataSource != NULL)
            mConnectingDataSource->disconnect();
        mFileSource.clear();
        mCachedSource.clear();
        mFinishAgain = true;
        mPrerollEnable = true;
        return OK;
    }
#endif // #ifndef ANDROID_DEFAULT_CODE
    int64_t totalBitRate = 0;

    for (size_t i = 0; i < extractor->countTracks(); ++i) {
        sp<MetaData> meta = extractor->getTrackMetaData(i);

        int32_t bitrate;
        if (!meta->findInt32(kKeyBitRate, &bitrate)) {
            const char *mime;
            CHECK(meta->findCString(kKeyMIMEType, &mime));
            LOGV("track of type '%s' does not publish bitrate", mime);

            totalBitRate = -1;
            break;
        }

        totalBitRate += bitrate;
    }

    mBitrate = totalBitRate;

    LOGV("mBitrate = %lld bits/sec", mBitrate);

    {
        Mutex::Autolock autoLock(mStatsLock);
        mStats.mBitrate = mBitrate;
        mStats.mTracks.clear();
        mStats.mAudioTrackIndex = -1;
        mStats.mVideoTrackIndex = -1;
    }

    bool haveAudio = false;
    bool haveVideo = false;
    for (size_t i = 0; i < extractor->countTracks(); ++i) {
        sp<MetaData> meta = extractor->getTrackMetaData(i);
#ifndef ANDROID_DEFAULT_CODE
		meta->setPointer(kKeyDataSourceObserver,this); //save awesomeplayer pointer
#endif
        const char *_mime;
        CHECK(meta->findCString(kKeyMIMEType, &_mime));

        String8 mime = String8(_mime);

        if (!haveVideo && !strncasecmp(mime.string(), "video/", 6)) {
            setVideoSource(extractor->getTrack(i));
            haveVideo = true;

            // Set the presentation/display size
            int32_t displayWidth, displayHeight;
            bool success = meta->findInt32(kKeyDisplayWidth, &displayWidth);
            if (success) {
                success = meta->findInt32(kKeyDisplayHeight, &displayHeight);
            }
            if (success) {
                mDisplayWidth = displayWidth;
                mDisplayHeight = displayHeight;
            }
#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_S3D_SUPPORT
             if (mVideoStereoMode != VIDEO_STEREO_DEFAULT) {			 	
			meta->setInt32(kKeyVideoStereoMode, (int32_t)mVideoStereoMode);
			LOGI("set mVideoStereoMode=%d", mVideoStereoMode);
             }
#endif
#endif

            {
                Mutex::Autolock autoLock(mStatsLock);
                mStats.mVideoTrackIndex = mStats.mTracks.size();
                mStats.mTracks.push();
                TrackStat *stat =
                    &mStats.mTracks.editItemAt(mStats.mVideoTrackIndex);
                stat->mMIME = mime.string();
            }
        } else if (!haveAudio && !strncasecmp(mime.string(), "audio/", 6)) {
            setAudioSource(extractor->getTrack(i));
            haveAudio = true;

            {
                Mutex::Autolock autoLock(mStatsLock);
                mStats.mAudioTrackIndex = mStats.mTracks.size();
                mStats.mTracks.push();
                TrackStat *stat =
                    &mStats.mTracks.editItemAt(mStats.mAudioTrackIndex);
                stat->mMIME = mime.string();
            }

            if (!strcasecmp(mime.string(), MEDIA_MIMETYPE_AUDIO_VORBIS)) {
                // Only do this for vorbis audio, none of the other audio
                // formats even support this ringtone specific hack and
                // retrieving the metadata on some extractors may turn out
                // to be very expensive.
                sp<MetaData> fileMeta = extractor->getMetaData();
                int32_t loop;
                if (fileMeta != NULL
                        && fileMeta->findInt32(kKeyAutoLoop, &loop) && loop != 0) {
                    modifyFlags(AUTO_LOOPING, SET);
                }
            }
        } else if (!strcasecmp(mime.string(), MEDIA_MIMETYPE_TEXT_3GPP)) {
            addTextSource(extractor->getTrack(i));
        }
    }

    if (!haveAudio && !haveVideo) {
#ifndef ANDROID_DEFAULT_CODE
        // report unsupport for new Gallery[3D]
        notifyListener_l(MEDIA_ERROR, MEDIA_ERROR_TYPE_NOT_SUPPORTED);
#endif // ifndef ANDROID_DEFAULT_CODE
        return UNKNOWN_ERROR;
    }

#ifndef ANDROID_DEFAULT_CODE
	if (!haveVideo) {
		int32_t hasUnsupportVideo = 0;
		sp<MetaData> fileMeta = extractor->getMetaData();
		if (fileMeta != NULL && fileMeta->findInt32(kKeyHasUnsupportVideo, &hasUnsupportVideo)
			&& hasUnsupportVideo != 0) {
			notifyListener_l(MEDIA_INFO, MEDIA_INFO_HAS_UNSUPPORT_VIDEO);
			LOGD("Notify APP that file has unsupportted video");
		}
			
	}
#endif

#ifndef ANDROID_DEFAULT_CODE
    if (mRTSPController != NULL) {
        mAudioPadEnable = true;
        mMetaData = mRTSPController->getMetaData();
    }
#endif // #ifndef ANDROID_DEFAULT_CODE
    mExtractorFlags = extractor->flags();

#ifndef ANDROID_DEFAULT_CODE
    if ((extractor->flags() & MediaExtractor::MAY_PARSE_TOO_LONG)) {
        Mutex::Autolock autoLock(mMiscStateLock);
        if (mStopped) {
            LOGI("user has already stopped");
            extractor->stopParsing();
        } else {
            LOGI("this extractor may take long time to parse, record for stopping");
            mExtractor = extractor;
        }
    }
#endif 

    return OK;
}

void AwesomePlayer::reset() {
    LOGV("reset");
#ifndef ANDROID_DEFAULT_HTTP_STREAM
    disconnectSafeIfNeccesary();
//    if (mCachedSource != NULL) {
//        mCachedSource->stop();
//    }
#endif

#ifndef ANDROID_DEFAULT_CODE
    sp<ARTSPController> rtsp = NULL;
    {
        Mutex::Autolock autoLock(mMiscStateLock);
        rtsp = mRTSPController;
    }
    if (rtsp != NULL) {
        rtsp->stopRequests();
        rtsp.clear();
    }

    {
        Mutex::Autolock autoLock(mMiscStateLock);
        if (mExtractor != NULL) {
            LOGI("stop extractor in reset");
            mExtractor->stopParsing();
        } else {
            LOGI("set flag for stopped");
            mStopped = true;
        }
    }
#endif // #ifndef ANDROID_DEFAULT_CODE
    Mutex::Autolock autoLock(mLock);
    reset_l();
}

void AwesomePlayer::reset_l() {
    mDisplayWidth = 0;
    mDisplayHeight = 0;

    if (mDecryptHandle != NULL) {
            mDrmManagerClient->setPlaybackStatus(mDecryptHandle,
                    Playback::STOP, 0);
            mDecryptHandle = NULL;
            mDrmManagerClient = NULL;
    }

    if (mFlags & PLAYING) {
        uint32_t params = IMediaPlayerService::kBatteryDataTrackDecoder;
        if ((mAudioSource != NULL) && (mAudioSource != mAudioTrack)) {
            params |= IMediaPlayerService::kBatteryDataTrackAudio;
        }
        if (mVideoSource != NULL) {
            params |= IMediaPlayerService::kBatteryDataTrackVideo;
        }
        addBatteryData(params);
    }

    if (mFlags & PREPARING) {
        modifyFlags(PREPARE_CANCELLED, SET);
        if (mConnectingDataSource != NULL) {
            LOGI("interrupting the connection process");
#ifndef ANDROID_DEFAULT_CODE
			if (mCachedSource != NULL) {
				mCachedSource->finishCache();
			}
#endif
            mConnectingDataSource->disconnect();
        } else if (mConnectingRTSPController != NULL) {
            LOGI("interrupting the connection process");
            mConnectingRTSPController->disconnect();
        }

        if (mFlags & PREPARING_CONNECTED) {
            // We are basically done preparing, we're just buffering
            // enough data to start playback, we can safely interrupt that.
            finishAsyncPrepare_l();
        }
#ifndef ANDROID_DEFAULT_CODE
        // give a chance to let APacketSource return from read
        if (mRTSPController != NULL)
            mRTSPController->stop();
#endif // #ifndef ANDROID_DEFAULT_CODE
    }

    while (mFlags & PREPARING) {
        mPreparedCondition.wait(mLock);
    }

    cancelPlayerEvents();
#ifndef ANDROID_DEFAULT_CODE
    mQueue.cancelEvent(mDurationUpdateEvent->eventID());
    mDurationUpdateEventPending = false;
#endif
    mWVMExtractor.clear();
    mCachedSource.clear();
    mAudioTrack.clear();
    mVideoTrack.clear();

    // Shutdown audio first, so that the respone to the reset request
    // appears to happen instantaneously as far as the user is concerned
    // If we did this later, audio would continue playing while we
    // shutdown the video-related resources and the player appear to
    // not be as responsive to a reset request.
    if ((mAudioPlayer == NULL || !(mFlags & AUDIOPLAYER_STARTED))
            && mAudioSource != NULL) {
        // If we had an audio player, it would have effectively
        // taken possession of the audio source and stopped it when
        // _it_ is stopped. Otherwise this is still our responsibility.
        mAudioSource->stop();
    }
    mAudioSource.clear();

    mTimeSource = NULL;

    delete mAudioPlayer;
    mAudioPlayer = NULL;

    if (mTextPlayer != NULL) {
        delete mTextPlayer;
        mTextPlayer = NULL;
    }

    mVideoRenderer.clear();

    if (mRTSPController != NULL) {
        mRTSPController->disconnect();
#ifndef ANDROID_DEFAULT_CODE
        {
            Mutex::Autolock autoLock(mMiscStateLock);
            mRTSPController.clear();
        }
#else
        mRTSPController.clear();
#endif // #ifndef ANDROID_DEFAULT_CODE
    }

    if (mVideoSource != NULL) {
        shutdownVideoDecoder_l();
    }

    mDurationUs = -1;
    modifyFlags(0, ASSIGN);
    mExtractorFlags = 0;
    mTimeSourceDeltaUs = 0;
    mVideoTimeUs = 0;
#ifndef ANDROID_DEFAULT_CODE
    mSessionDesc.clear();
    mFinishAgain = false;
    mMetaData.clear();
    mWatchForAudioSeekComplete = false;
    mAudioNormalEOS = false;
    mLastAudioSeekUs = 0;
    mExtractor.clear();
    mStopped = false;
    mLatencyUs = 0;
    mFirstVideoBuffer = NULL;
    mFirstVideoBufferStatus = OK;
#endif // #ifndef ANDROID_DEFAULT_CODE
#ifndef ANDROID_DEFAULT_HTTP_STREAM
    mCachedSourcePauseResponseState = 0;
#endif
    mSeeking = NO_SEEK;
    mSeekNotificationSent = true;
    mSeekTimeUs = 0;

    mUri.setTo("");
    mUriHeaders.clear();

    mFileSource.clear();
    //MTK_OP01_PROTECT_START
#ifndef ANDROID_DEFAULT_CODE 	
    m_cmmbUri.setTo("");
    mCMMBSource.clear();
#endif
    //MTK_OP01_PROTECT_END

    mBitrate = -1;
    mLastVideoTimeUs = -1;

    {
        Mutex::Autolock autoLock(mStatsLock);
        mStats.mFd = -1;
        mStats.mURI = String8();
        mStats.mBitrate = -1;
        mStats.mAudioTrackIndex = -1;
        mStats.mVideoTrackIndex = -1;
        mStats.mNumVideoFramesDecoded = 0;
        mStats.mNumVideoFramesDropped = 0;
        mStats.mVideoWidth = -1;
        mStats.mVideoHeight = -1;
        mStats.mFlags = 0;
        mStats.mTracks.clear();
    }

    mWatchForAudioSeekComplete = false;
    mWatchForAudioEOS = false;

}

void AwesomePlayer::notifyListener_l(int msg, int ext1, int ext2) {
#ifndef ANDROID_DEFAULT_CODE
	if ((mCachedSource != NULL) && (msg == MEDIA_ERROR)) {
		 status_t cache_stat = mCachedSource->getRealFinalStatus();
		 bool bCacheSuccess = (cache_stat == OK || cache_stat == ERROR_END_OF_STREAM);
	
		 if (!bCacheSuccess) {
			 if (cache_stat == ERROR_FORBIDDEN) {
				 ext1 = MEDIA_ERROR_INVALID_CONNECTION;//httpstatus = 403
			 } else if (cache_stat == ERROR_POOR_INTERLACE) {
				 ext1 = MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK;
			 } else {
				 ext1 = MEDIA_ERROR_CANNOT_CONNECT_TO_SERVER;
			 }
			ext2 = cache_stat;
			 LOGE("report 'cannot connect' to app, cache_stat = %d", cache_stat);
		 }
	 } 
    // try to report a more meaningful error
    if (msg == MEDIA_ERROR && ext1 == MEDIA_ERROR_UNKNOWN) {
        switch(ext2) {
            case ERROR_MALFORMED:
                ext1 = MEDIA_ERROR_BAD_FILE;
                break;
            case ERROR_CANNOT_CONNECT:
                ext1 = MEDIA_ERROR_CANNOT_CONNECT_TO_SERVER;
                break;
            case ERROR_UNSUPPORTED:
                ext1 = MEDIA_ERROR_TYPE_NOT_SUPPORTED;
                break;
            case ERROR_FORBIDDEN:
                ext1 = MEDIA_ERROR_INVALID_CONNECTION;
                break;
        }
    }
#endif // ifndef ANDROID_DEFAULT_CODE
    if (mListener != NULL) {
        sp<MediaPlayerBase> listener = mListener.promote();

        if (listener != NULL) {
            listener->sendEvent(msg, ext1, ext2);
        }
    }
}

bool AwesomePlayer::getBitrate(int64_t *bitrate) {
    off64_t size;
    if (mDurationUs >= 0 && mCachedSource != NULL
            && mCachedSource->getSize(&size) == OK) {
        *bitrate = size * 8000000ll / mDurationUs;  // in bits/sec
        return true;
    }

    if (mBitrate >= 0) {
        *bitrate = mBitrate;
        return true;
    }

    *bitrate = 0;

    return false;
}

// Returns true iff cached duration is available/applicable.
bool AwesomePlayer::getCachedDuration_l(int64_t *durationUs, bool *eos) {
    int64_t bitrate;

    if (mRTSPController != NULL) {
        *durationUs = mRTSPController->getQueueDurationUs(eos);
        return true;
    } else if (mCachedSource != NULL && getBitrate(&bitrate)) {
        status_t finalStatus;
        size_t cachedDataRemaining = mCachedSource->approxDataRemaining(&finalStatus);
        *durationUs = cachedDataRemaining * 8000000ll / bitrate;
        *eos = (finalStatus != OK);
        return true;
    } else if (mWVMExtractor != NULL) {
        status_t finalStatus;
        *durationUs = mWVMExtractor->getCachedDurationUs(&finalStatus);
        *eos = (finalStatus != OK);
        return true;
    }

    return false;
}

void AwesomePlayer::ensureCacheIsFetching_l() {
    if (mCachedSource != NULL) {
        mCachedSource->resumeFetchingIfNecessary();
    }
}

void AwesomePlayer::onVideoLagUpdate() {
    Mutex::Autolock autoLock(mLock);
    if (!mVideoLagEventPending) {
        return;
    }
    mVideoLagEventPending = false;

    int64_t audioTimeUs = mAudioPlayer->getMediaTimeUs();
    int64_t videoLateByUs = audioTimeUs - mVideoTimeUs;

    if (!(mFlags & VIDEO_AT_EOS) && videoLateByUs > 300000ll) {
        LOGV("video late by %lld ms.", videoLateByUs / 1000ll);

        notifyListener_l(
                MEDIA_INFO,
                MEDIA_INFO_VIDEO_TRACK_LAGGING,
                videoLateByUs / 1000ll);
    }

    postVideoLagEvent_l();
}

void AwesomePlayer::onBufferingUpdate() {
    Mutex::Autolock autoLock(mLock);
    if (!mBufferingEventPending) {
        return;
    }
    mBufferingEventPending = false;
#ifndef ANDROID_DEFAULT_CODE
    return onBufferingUpdate_l();
}
#endif

#ifndef ANDROID_DEFAULT_CODE
void AwesomePlayer::onBufferingUpdate_l() {
#endif // #ifndef ANDROID_DEFAULT_CODE

    if (mCachedSource != NULL) {
#ifndef ANDROID_DEFAULT_CODE
        return onBufferingUpdateCachedSource_l();
#endif
        status_t finalStatus;
        size_t cachedDataRemaining = mCachedSource->approxDataRemaining(&finalStatus);
        bool eos = (finalStatus != OK);

        if (eos) {
            if (finalStatus == ERROR_END_OF_STREAM) {
                notifyListener_l(MEDIA_BUFFERING_UPDATE, 100);
            }
            if (mFlags & PREPARING) {
                LOGV("cache has reached EOS, prepare is done.");
                finishAsyncPrepare_l();
            }
        } else {
            int64_t bitrate;
            if (getBitrate(&bitrate)) {
                size_t cachedSize = mCachedSource->cachedSize();
                int64_t cachedDurationUs = cachedSize * 8000000ll / bitrate;

                int percentage = 100.0 * (double)cachedDurationUs / mDurationUs;
                if (percentage > 100) {
                    percentage = 100;
                }

                notifyListener_l(MEDIA_BUFFERING_UPDATE, percentage);
            } else {
                // We don't know the bitrate of the stream, use absolute size
                // limits to maintain the cache.

                if ((mFlags & PLAYING) && !eos
                        && (cachedDataRemaining < kLowWaterMarkBytes)) {
                    LOGI("cache is running low (< %d) , pausing.",
                         kLowWaterMarkBytes);
                    modifyFlags(CACHE_UNDERRUN, SET);
                    pause_l();
                    ensureCacheIsFetching_l();
                    sendCacheStats();
                    notifyListener_l(MEDIA_INFO, MEDIA_INFO_BUFFERING_START);
                } else if (eos || cachedDataRemaining > kHighWaterMarkBytes) {
                    if (mFlags & CACHE_UNDERRUN) {
                        LOGI("cache has filled up (> %d), resuming.",
                             kHighWaterMarkBytes);
                        modifyFlags(CACHE_UNDERRUN, CLEAR);
                        play_l();
                        notifyListener_l(MEDIA_INFO, MEDIA_INFO_BUFFERING_END);
                    } else if (mFlags & PREPARING) {
                        LOGV("cache has filled up (> %d), prepare is done",
                             kHighWaterMarkBytes);
                        finishAsyncPrepare_l();
                    }
                }
            }
        }
    } else if (mWVMExtractor != NULL) {
        status_t finalStatus;

        int64_t cachedDurationUs
            = mWVMExtractor->getCachedDurationUs(&finalStatus);

        bool eos = (finalStatus != OK);

        if (eos) {
            if (finalStatus == ERROR_END_OF_STREAM) {
                notifyListener_l(MEDIA_BUFFERING_UPDATE, 100);
            }
            if (mFlags & PREPARING) {
                LOGV("cache has reached EOS, prepare is done.");
                finishAsyncPrepare_l();
            }
        } else {
            int percentage = 100.0 * (double)cachedDurationUs / mDurationUs;
            if (percentage > 100) {
                percentage = 100;
            }

            notifyListener_l(MEDIA_BUFFERING_UPDATE, percentage);
        }
    }

    int64_t cachedDurationUs;
    bool eos;
    if (getCachedDuration_l(&cachedDurationUs, &eos)) {
        LOGV("cachedDurationUs = %.2f secs, eos=%d",
             cachedDurationUs / 1E6, eos);

#ifndef ANDROID_DEFAULT_CODE        
        int64_t highWaterMarkUs = mHighWaterMarkUs;
#else
        int64_t highWaterMarkUs =
            (mRTSPController != NULL) ? kHighWaterMarkRTSPUs : kHighWaterMarkUs;
#endif

        if ((mFlags & PLAYING) && !eos
                && (cachedDurationUs < kLowWaterMarkUs)) {
            LOGI("cache is running low (%.2f secs) , pausing.",
                 cachedDurationUs / 1E6);
            modifyFlags(CACHE_UNDERRUN, SET);
            pause_l();
            ensureCacheIsFetching_l();
            sendCacheStats();
            notifyListener_l(MEDIA_INFO, MEDIA_INFO_BUFFERING_START);
#ifndef ANDROID_DEFAULT_CODE
            // AP need 0% notification
            notifyListener_l(MEDIA_BUFFERING_UPDATE, 0);
#endif // #ifndef ANDROID_DEFAULT_CODE
#ifndef ANDROID_DEFAULT_CODE
        } else if (eos || cachedDurationUs >= highWaterMarkUs) {
#else
        } else if (eos || cachedDurationUs > highWaterMarkUs) {
#endif // #ifndef ANDROID_DEFAULT_CODE
            if (mFlags & CACHE_UNDERRUN) {
                LOGI("cache has filled up (%.2f secs), resuming.",
                     cachedDurationUs / 1E6);
                modifyFlags(CACHE_UNDERRUN, CLEAR);
                play_l();
#ifndef ANDROID_DEFAULT_CODE
                // AP need 100% notification
                notifyListener_l(MEDIA_BUFFERING_UPDATE, 100);
                if (cachedDurationUs > 0 && cachedDurationUs != INT64_MAX)
                    notifyListener_l(MEDIA_INFO, MEDIA_INFO_BUFFERING_DATA, cachedDurationUs);
#endif // #ifndef ANDROID_DEFAULT_CODE
                notifyListener_l(MEDIA_INFO, MEDIA_INFO_BUFFERING_END);
            } else if (mFlags & PREPARING) {
                LOGV("cache has filled up (%.2f secs), prepare is done",
                     cachedDurationUs / 1E6);
#ifndef ANDROID_DEFAULT_CODE
                // AP need 100% notification
                notifyListener_l(MEDIA_BUFFERING_UPDATE, 100);
                if (cachedDurationUs > 0 && cachedDurationUs != INT64_MAX)
                    notifyListener_l(MEDIA_INFO, MEDIA_INFO_BUFFERING_DATA, cachedDurationUs);
#endif // #ifndef ANDROID_DEFAULT_CODE
                finishAsyncPrepare_l();
            }
#ifndef ANDROID_DEFAULT_CODE
            // report buffering status for RTSP
        } else if (mFlags & (PREPARING | CACHE_UNDERRUN)){
            int percentage = 100.0 * (double)cachedDurationUs / highWaterMarkUs;
            // only report 100 in the above else-if branch
            if (percentage >= 100) {
                percentage = 99;
            }
            notifyListener_l(MEDIA_BUFFERING_UPDATE, percentage);
#endif // #ifndef ANDROID_DEFAULT_CODE
        }
    }

    postBufferingEvent_l();
}

#ifndef ANDROID_DEFAULT_CODE
void AwesomePlayer::onBufferingUpdateCachedSource_l() {
    status_t finalStatus;
    size_t cachedDataRemaining = mCachedSource->approxDataRemaining(&finalStatus);
    bool eos = (finalStatus != OK);

#ifndef ANDROID_DEFAULT_HTTP_STREAM
	if (mStopped) {
		LOGD("I'm stopped, exit on buffering");
		return;
	}

    if (eos && (finalStatus != ERROR_END_OF_STREAM))
    {
    	LOGD("Notify, onBufferingUpdateCachedSource_l, finalStatus=%d", finalStatus);
    	notifyListener_l(MEDIA_ERROR, finalStatus, 0);
    }

   if (mFlags & CACHE_MISSING) {
        //TODO: to update buffer in current seek time
        if (cachedDataRemaining > 0) {
            LOGI("cache is shot again, mSeeking = %d", (int)mSeeking);
            if (mVideoSource != NULL) {
                //recover omxcodec
				//mVideoSource->start();
			   LOGD("video resume");
			   reinterpret_cast<OMXCodec *>(mVideoSource.get())->resume();

                if (mSeeking != NO_SEEK) {
                    modifyFlags(SEEK_PREVIEW, SET);
                }

                if (mFlags & PLAYING) {
                    //the CACHE_MISSING flag will reset in video event
                    //the reason is that the video event may complete the pending seek
                    postVideoEvent_l();
                    //buffering event will be activated after CACHE_MISSING reset
                } else {
                    modifyFlags(CACHE_MISSING, CLEAR);
                    postBufferingEvent_l();
                    LOGD("CACHE_MISSING reset in BufferingEvent");
                }
                return;
            }
       }

        postBufferingEvent_l();
        return;
    }
#endif

    int64_t bitrate = 0;
    bool bitrateAvailable = false;

    //
    //update percent
    if (eos) {
        //check if network failed
        if (finalStatus == ERROR_END_OF_STREAM) {
            notifyListener_l(MEDIA_BUFFERING_UPDATE, 100);
        }

        if (mFlags & PREPARING) {
            LOGD("cache has reached EOS, prepare is done.");
            finishAsyncPrepare_l();
        }
    } else {
        bitrateAvailable = getBitrate(&bitrate);
        if (bitrateAvailable) {
            size_t cachedSize = mCachedSource->cachedSize();
            int64_t cachedDurationUs = cachedSize * 8000000ll / bitrate;

            int percentage = 100.0 * (double)cachedDurationUs / mDurationUs;
            if (percentage > 100) {
                percentage = 100;
            }

            notifyListener_l(MEDIA_BUFFERING_UPDATE, percentage);
        } else {
            // We don't know the bitrate of the stream, use absolute size
            // limits to maintain the cache.

            if ((mFlags & PLAYING) && !eos
                    && (cachedDataRemaining < kLowWaterMarkBytes)) {
                LOGI("cache is running low (< %d) , pausing.",
                        kLowWaterMarkBytes);
                modifyFlags(CACHE_UNDERRUN, SET);
                pause_l();
                ensureCacheIsFetching_l();
                sendCacheStats();
                notifyListener_l(MEDIA_INFO, MEDIA_INFO_BUFFERING_START);
            } else if (eos || cachedDataRemaining > kHighWaterMarkBytes) {
                if (mFlags & CACHE_UNDERRUN) {
                    LOGI("cache has filled up (> %d), resuming.",
                            kHighWaterMarkBytes);
                    modifyFlags(CACHE_UNDERRUN, CLEAR);
                    play_l();
                    notifyListener_l(MEDIA_INFO, MEDIA_INFO_BUFFERING_END);
                } else if (mFlags & PREPARING) {
                    LOGV("cache has filled up (> %d), prepare is done",
                            kHighWaterMarkBytes);
                    finishAsyncPrepare_l();
                }
            }
        }
    }
    int64_t cachedDurationUs;
    if (getCachedDuration_l(&cachedDurationUs, &eos)) {
        LOGV("cachedDurationUs = %.2f secs, eos=%d",
                cachedDurationUs / 1E6, eos);

        int64_t highWaterMarkUs = mHighWaterMarkUs;
        /*if (!mSeekNotificationSent) {
            highWaterMarkUs = kLowWaterMarkUs + 100000ll;
            //the seek complete is only done in Audio, 
            //so the if the seek is not complete, we should complete the seek asap:
            //1. trigger audio to complete the seek asap (set highWaterMark a little more than lowWaterMark)
            //2. don't auto-pause until seek completed (not impletemented yet)
        } else {*/
            if (bitrateAvailable) {
                CHECK(mCachedSource.get() != NULL);
                int64_t nMaxCacheDuration = mCachedSource->getMaxCacheSize() * 8000000ll / bitrate;
                if (nMaxCacheDuration < highWaterMarkUs) {
                    //LOGV("highwatermark = %lld, cache maxduration = %lld", highWaterMarkUs, nMaxCacheDuration);
                    highWaterMarkUs = nMaxCacheDuration;
                }
            }
        //}

        if ((mFlags & PLAYING) && !eos
                && (cachedDurationUs < kLowWaterMarkUs)) {
            LOGI("cache is running low (%.2f secs), pausing.",
                    cachedDurationUs / 1E6);
            modifyFlags(CACHE_UNDERRUN, SET);
            pause_l();
            ensureCacheIsFetching_l();
            sendCacheStats();
            notifyListener_l(MEDIA_INFO, MEDIA_INFO_BUFFERING_START);
        } else if (eos || cachedDurationUs > highWaterMarkUs) {
            if (mFlags & CACHE_UNDERRUN) {
                LOGI("cache has filled up (%.2f secs), resuming.",
                        cachedDurationUs / 1E6);
                modifyFlags(CACHE_UNDERRUN, CLEAR);
                play_l();
                notifyListener_l(MEDIA_INFO, MEDIA_INFO_BUFFERING_END);
            } else if (mFlags & PREPARING) {
                LOGV("cache has filled up (%.2f secs), prepare is done",
                        cachedDurationUs / 1E6);
                finishAsyncPrepare_l();
            }
        }
    }

    postBufferingEvent_l();

}
#endif

void AwesomePlayer::sendCacheStats() {
    sp<MediaPlayerBase> listener = mListener.promote();
    if (listener != NULL && mCachedSource != NULL) {
        int32_t kbps = 0;
        status_t err = mCachedSource->getEstimatedBandwidthKbps(&kbps);
        if (err == OK) {
            listener->sendEvent(
                MEDIA_INFO, MEDIA_INFO_NETWORK_BANDWIDTH, kbps);
        }
    }
}

void AwesomePlayer::onStreamDone() {
    // Posted whenever any stream finishes playing.

    Mutex::Autolock autoLock(mLock);
    if (!mStreamDoneEventPending) {
        return;
    }
    mStreamDoneEventPending = false;

    if (mStreamDoneStatus != ERROR_END_OF_STREAM) {
        LOGV("MEDIA_ERROR %d", mStreamDoneStatus);

#ifndef ANDROID_DEFAULT_CODE
        if (mStreamDoneStatus == ERROR_UNSUPPORTED) {
            // report unsupport for new Gallery[3D]
            notifyListener_l(MEDIA_ERROR, MEDIA_ERROR_TYPE_NOT_SUPPORTED);
        } else if ((mStreamDoneStatus == ERROR_CANNOT_CONNECT) || (mStreamDoneStatus == ERROR_CONNECTION_LOST)) {
            notifyListener_l(MEDIA_ERROR, MEDIA_ERROR_CANNOT_CONNECT_TO_SERVER);
        } else {
            // report bad file for new Gallery[3D] if error occurs
            // FIXME there may be other errors than bad file
            notifyListener_l(MEDIA_ERROR, MEDIA_ERROR_BAD_FILE, mStreamDoneStatus);
        }
#else
        notifyListener_l(
                MEDIA_ERROR, MEDIA_ERROR_UNKNOWN, mStreamDoneStatus);
#endif // ifndef ANDROID_DEFAULT_CODE

        pause_l(true /* at eos */);

        modifyFlags(AT_EOS, SET);
        return;
    }

    const bool allDone =
        (mVideoSource == NULL || (mFlags & VIDEO_AT_EOS))
            && (mAudioSource == NULL || (mFlags & AUDIO_AT_EOS));

    if (!allDone) {
        return;
    }
#ifndef ANDROID_DEFAULT_CODE
	if(allDone &&  mFinalStopFlag ==(FINAL_HAS_UNSUPPORT_VIDEO|FINAL_HAS_UNSUPPORT_AUDIO))
      {
		notifyListener_l(MEDIA_ERROR, MEDIA_ERROR_TYPE_NOT_SUPPORTED);
	      pause_l(true /* at eos */);		  
            modifyFlags(AT_EOS, SET);
	      mFinalStopFlag=0;
		LOGE("AT_EOS mFinalStopFlag=3");
            return;
	}
#endif
	

    if ((mFlags & LOOPING)
            || ((mFlags & AUTO_LOOPING)
                && (mAudioSink == NULL || mAudioSink->realtime()))) {
        // Don't AUTO_LOOP if we're being recorded, since that cannot be
        // turned off and recording would go on indefinitely.

        seekTo_l(0);

        if (mVideoSource != NULL) {
            postVideoEvent_l();
        }
    } else {
        LOGV("MEDIA_PLAYBACK_COMPLETE");
#ifndef ANDROID_DEFAULT_CODE
        bool notifyComplete = true;
        if (mRTSPController != NULL && mDurationUs >= 0) {
            int64_t timeUs = 0;
            if (mAudioPlayer != NULL)
                timeUs = mAudioPlayer->getMediaTimeUs();
            if (mVideoSource != NULL && mVideoTimeUs > timeUs)
                timeUs = mVideoTimeUs;
            timeUs = mRTSPController->getNormalPlayTimeUs(timeUs);

            if (mDurationUs == 0 || mDurationUs - timeUs > kRTSPEarlyEndTimeUs) {
                LOGE("RTSP play end at %lld before duration %lld", 
                        timeUs, mDurationUs);
                notifyListener_l(MEDIA_ERROR, MEDIA_ERROR_CANNOT_CONNECT_TO_SERVER);
                notifyComplete = false;
            }
        }
        
        if (notifyComplete)
#endif // #ifndef ANDROID_DEFAULT_CODE
        notifyListener_l(MEDIA_PLAYBACK_COMPLETE);

#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_DRM_APP
        isCurrentComplete = true;
#endif
#endif

        pause_l(true /* at eos */);

        modifyFlags(AT_EOS, SET);
    }
}

status_t AwesomePlayer::play() {
#ifndef ANDROID_DEFAULT_HTTP_STREAM
    LOGD("play");

    if ((mCachedSource != NULL) && (mCachedSourcePauseResponseState & PausePending))
    {
    
        mCachedSourcePauseResponseState &= ~PausePending;
        LOGD("play return because mCachedSource PausePending %x", mCachedSourcePauseResponseState);
        return OK;
    }
#endif
    Mutex::Autolock autoLock(mLock);

    modifyFlags(CACHE_UNDERRUN, CLEAR);

#ifndef ANDROID_DEFAULT_CODE
    status_t err;
    if (mRTSPController != NULL) {
        LOGI("[rtsp]rtsp send play!!!");
        err = mRTSPController->sendPlay();
        if (err != OK) {
            LOGE("[rtsp]RTSPController send PLAY completed with result %d (%s)",
                    err, strerror(-err));
            return err;
        }else{
            LOGI("[rtsp]RTSPController send PLAY OK");
        }
    }
#endif

    return play_l();
}

status_t AwesomePlayer::play_l() {
    modifyFlags(SEEK_PREVIEW, CLEAR);

    if (mFlags & PLAYING) {
        return OK;
    }

    if (!(mFlags & PREPARED)) {
        status_t err = prepare_l();

        if (err != OK) {
            return err;
        }
    }

    modifyFlags(PLAYING, SET);
    modifyFlags(FIRST_FRAME, SET);

#ifndef ANDROID_DEFAULT_CODE
    if ((mRTSPController != NULL) || (mCachedSource != NULL)) {
        // check cache before playing
        onBufferingUpdate_l();
        if ((mFlags & CACHE_UNDERRUN) || (mFlags & CACHE_MISSING)) {
            return OK;
        }
    }
#endif // #ifndef ANDROID_DEFAULT_CODE

    if (mDecryptHandle != NULL) {
        int64_t position;
        getPosition(&position);
        mDrmManagerClient->setPlaybackStatus(mDecryptHandle,
                Playback::START, position / 1000);
#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_DRM_APP
        // OMA DRM v1 implementation, when the playback is done and position comes to 0, consume rights.
        if (isCurrentComplete && position == 0) { // single recursive mode
            LOGD("AwesomePlayer, consumeRights @play_l()");
            // in some cases, the mFileSource may be NULL (E.g. play audio directly in File Manager)
            // We don't know, but we assume it's a OMA DRM v1 case (DecryptApiType::CONTAINER_BASED)
            if ((mFileSource.get() != NULL && (mFileSource->flags() & OMADrmFlag) != 0)
                || (DecryptApiType::CONTAINER_BASED == mDecryptHandle->decryptApiType)) {
                if (!DrmMtkUtil::isTrustedVideoClient(mDrmValue)) {
                    mDrmManagerClient->consumeRights(mDecryptHandle, Action::PLAY, false);
                }
            }
            isCurrentComplete = false;
        }
#endif
#endif
    }

    //MTK_OP01_PROTECT_START
#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_CMMB_SUPPORT
    if (true == IsCMMBPlayer)
    {
        LOGE("AwesomePlayer::play_l in");
        //sleep(1);
        LOGE("AwesomePlayer::play_l sleep done");
    }
#endif
#endif
    //MTK_OP01_PROTECT_END
    if (mAudioSource != NULL) {
        if (mAudioPlayer == NULL) {
            if (mAudioSink != NULL) {
                mAudioPlayer = new AudioPlayer(mAudioSink, this);
                mAudioPlayer->setSource(mAudioSource);
#ifndef ANDROID_DEFAULT_CODE
                // set before seekAudioIfNecessary_l, or seek will not callback
                mWatchForAudioSeekComplete = false;
#endif // #ifndef ANDROID_DEFAULT_CODE

                mTimeSource = mAudioPlayer;

                // If there was a seek request before we ever started,
                // honor the request now.
                // Make sure to do this before starting the audio player
                // to avoid a race condition.
                seekAudioIfNecessary_l();
#ifdef ANDROID_DEFAULT_CODE
                mWatchForAudioSeekComplete = false;
#endif // #ifndef ANDROID_DEFAULT_CODE
            }
        }

        CHECK(!(mFlags & AUDIO_RUNNING));

        if (mVideoSource == NULL) {
            // We don't want to post an error notification at this point,
            // the error returned from MediaPlayer::start() will suffice.

            status_t err = startAudioPlayer_l(
                    false /* sendErrorNotification */);

            if (err != OK) {
                delete mAudioPlayer;
                mAudioPlayer = NULL;

                modifyFlags((PLAYING | FIRST_FRAME), CLEAR);

                if (mDecryptHandle != NULL) {
                    mDrmManagerClient->setPlaybackStatus(
                            mDecryptHandle, Playback::STOP, 0);
                }

                return err;
            }
        }
    }

    if (mTimeSource == NULL && mAudioPlayer == NULL) {
        mTimeSource = &mSystemTimeSource;
    }

    if (mVideoSource != NULL) {
        // Kick off video playback
        postVideoEvent_l();

        if (mAudioSource != NULL && mVideoSource != NULL) {
            postVideoLagEvent_l();
        }
    }

    if (mFlags & AT_EOS) {
        // Legacy behaviour, if a stream finishes playing and then
        // is started again, we play from the start...
#ifndef ANDROID_DEFAULT_CODE
        // RTSP doesn't support play again without prepare again
        if (mRTSPController == NULL)
#endif // #ifndef ANDROID_DEFAULT_CODE
        seekTo_l(0);
    }

    uint32_t params = IMediaPlayerService::kBatteryDataCodecStarted
        | IMediaPlayerService::kBatteryDataTrackDecoder;
    if ((mAudioSource != NULL) && (mAudioSource != mAudioTrack)) {
        params |= IMediaPlayerService::kBatteryDataTrackAudio;
    }
    if (mVideoSource != NULL) {
        params |= IMediaPlayerService::kBatteryDataTrackVideo;
#ifndef ANDROID_DEFAULT_CODE
    	{
    	      if (reinterpret_cast<OMXCodec *>(mVideoSource.get())->
				vDecSwitchBwTVout(false) != OK)
			LOGE("play:set vDecSwitchBwTVout error");
	}		
#endif
    }
    addBatteryData(params);

    return OK;
}

status_t AwesomePlayer::startAudioPlayer_l(bool sendErrorNotification) {
    CHECK(!(mFlags & AUDIO_RUNNING));

    if (mAudioSource == NULL || mAudioPlayer == NULL) {
        return OK;
    }

    if (!(mFlags & AUDIOPLAYER_STARTED)) {
        modifyFlags(AUDIOPLAYER_STARTED, SET);

        bool wasSeeking = mAudioPlayer->isSeeking();

        // We've already started the MediaSource in order to enable
        // the prefetcher to read its data.
        status_t err = mAudioPlayer->start(
                true /* sourceAlreadyStarted */);

        if (err != OK) {
            if (sendErrorNotification) {
                notifyListener_l(MEDIA_ERROR, MEDIA_ERROR_UNKNOWN, err);
            }

            return err;
        }

#ifndef ANDROID_DEFAULT_CODE
        mLatencyUs = -mAudioPlayer->getRealTimeUs();
        if (mVideoSource == NULL || mLatencyUs < 0)
            mLatencyUs = 0;
        LOGI("AudioPlayer mLatencyUs %lld", mLatencyUs);
#endif

        if (wasSeeking) {
            CHECK(!mAudioPlayer->isSeeking());

            // We will have finished the seek while starting the audio player.
            postAudioSeekComplete();
        }
    } else {
        mAudioPlayer->resume();
    }

    modifyFlags(AUDIO_RUNNING, SET);

    mWatchForAudioEOS = true;

    return OK;
}

void AwesomePlayer::notifyVideoSize_l() {
    sp<MetaData> meta = mVideoSource->getFormat();

    int32_t cropLeft, cropTop, cropRight, cropBottom;
    if (!meta->findRect(
                kKeyCropRect, &cropLeft, &cropTop, &cropRight, &cropBottom)) {
        int32_t width, height;
        CHECK(meta->findInt32(kKeyWidth, &width));
        CHECK(meta->findInt32(kKeyHeight, &height));

        cropLeft = cropTop = 0;
        cropRight = width - 1;
        cropBottom = height - 1;

        LOGV("got dimensions only %d x %d", width, height);
    } else {
        LOGV("got crop rect %d, %d, %d, %d",
             cropLeft, cropTop, cropRight, cropBottom);
    }

    int32_t displayWidth;
    if (meta->findInt32(kKeyDisplayWidth, &displayWidth)) {
        LOGV("Display width changed (%d=>%d)", mDisplayWidth, displayWidth);
        mDisplayWidth = displayWidth;
    }
    int32_t displayHeight;
    if (meta->findInt32(kKeyDisplayHeight, &displayHeight)) {
        LOGV("Display height changed (%d=>%d)", mDisplayHeight, displayHeight);
        mDisplayHeight = displayHeight;
    }

    int32_t usableWidth = cropRight - cropLeft + 1;
    int32_t usableHeight = cropBottom - cropTop + 1;
    if (mDisplayWidth != 0) {
        usableWidth = mDisplayWidth;
    }
    if (mDisplayHeight != 0) {
        usableHeight = mDisplayHeight;
    }

#ifndef ANDROID_DEFAULT_CODE
    if (mDisplayWidth == 0 || mDisplayHeight == 0) { // in case there is no resolution info in the container
        int32_t videoAspectRatioWidth;
        int32_t videoAspectRatioHeight;
        if (!meta->findInt32(kKeyAspectRatioWidth, &videoAspectRatioWidth)) {
            LOGE ("Cannot find kKeyAspectRatioWidth");
            videoAspectRatioWidth = 1;
        }
        if (!meta->findInt32(kKeyAspectRatioHeight, &videoAspectRatioHeight)) {
            LOGE ("Cannot find kKeyAspectRatioHeight");
            videoAspectRatioHeight = 1;
        }
        usableWidth = usableWidth * videoAspectRatioWidth;
        usableHeight = usableHeight * videoAspectRatioHeight;
        LOGI ("videoAspectRatioWidth(%d), videoAspectRatioHeight(%d), usableWidth(%d), usableHeight(%d)", videoAspectRatioWidth, videoAspectRatioHeight, usableWidth, usableHeight);
   }
#endif
                
    {
        Mutex::Autolock autoLock(mStatsLock);
        mStats.mVideoWidth = usableWidth;
        mStats.mVideoHeight = usableHeight;
    }

    int32_t rotationDegrees;
    if (!mVideoTrack->getFormat()->findInt32(
                kKeyRotation, &rotationDegrees)) {
        rotationDegrees = 0;
    }

    if (rotationDegrees == 90 || rotationDegrees == 270) {
        notifyListener_l(
                MEDIA_SET_VIDEO_SIZE, usableHeight, usableWidth);
    } else {
        notifyListener_l(
                MEDIA_SET_VIDEO_SIZE, usableWidth, usableHeight);
    }
}

void AwesomePlayer::initRenderer_l() {
    if (mNativeWindow == NULL) {
        return;
    }

    sp<MetaData> meta = mVideoSource->getFormat();

    int32_t format;
    const char *component;
    int32_t decodedWidth, decodedHeight;
    CHECK(meta->findInt32(kKeyColorFormat, &format));
    CHECK(meta->findCString(kKeyDecoderComponent, &component));
    CHECK(meta->findInt32(kKeyWidth, &decodedWidth));
    CHECK(meta->findInt32(kKeyHeight, &decodedHeight));

    int32_t rotationDegrees;
    if (!mVideoTrack->getFormat()->findInt32(
                kKeyRotation, &rotationDegrees)) {
        rotationDegrees = 0;
    }

    mVideoRenderer.clear();

    // Must ensure that mVideoRenderer's destructor is actually executed
    // before creating a new one.
    IPCThreadState::self()->flushCommands();

    if (USE_SURFACE_ALLOC
            && !strncmp(component, "OMX.", 4)
            && strncmp(component, "OMX.google.", 11)
            && strcmp(component, "OMX.Nvidia.mpeg2v.decode")) {
        // Hardware decoders avoid the CPU color conversion by decoding
        // directly to ANativeBuffers, so we must use a renderer that
        // just pushes those buffers to the ANativeWindow.
        mVideoRenderer =
            new AwesomeNativeWindowRenderer(mNativeWindow, rotationDegrees);
    } else {
        // Other decoders are instantiated locally and as a consequence
        // allocate their buffers in local address space.  This renderer
        // then performs a color conversion and copy to get the data
        // into the ANativeBuffer.
        mVideoRenderer = new AwesomeLocalRenderer(mNativeWindow, meta);
    }
}

#ifndef ANDROID_DEFAULT_CODE
status_t AwesomePlayer::pause(bool stop) {
#else
status_t AwesomePlayer::pause() {
#endif // #ifndef ANDROID_DEFAULT_CODE

#ifndef ANDROID_DEFAULT_HTTP_STREAM
    //in http streaming, the mLock may be busy in onVideoEvent, before aquire it:
    //1. stop the cachedsource
    //or 2. aquire it with timeout    
    if (mCachedSource != NULL) { 
        if (stop) {
            disconnectSafeIfNeccesary();
//            mCachedSource->stop();//stop the cache and aquire lock in normal
            LOGD("pause: stop cachedsource");
        } else {
            //work around alps00072030: if Pause is already timeout, wait only 1 ms to avoid ANR
            uint32_t nWaitTime = (mCachedSourcePauseResponseState & PauseTimeOut) ? 1 : 6000; 
            status_t status = mLock.timedlock(nWaitTime);
            if (status != OK) {
                mCachedSourcePauseResponseState = (PauseTimeOut | PausePending);
                LOGI("pause: aquire lock failed(%d), set pause pending flag %x", status, mCachedSourcePauseResponseState);
                return OK;
            }
            else if (mFlags & CACHE_MISSING) {
                mCachedSourcePauseResponseState = PausePending;
                LOGD("pause: pending because CACHE_MISSING");
                mLock.unlock();
                return OK;
            }
            else {
                mCachedSourcePauseResponseState = 0;
                modifyFlags(CACHE_UNDERRUN, CLEAR);
                LOGD("pause: aquire lock success");
                status = pause_l();
                mLock.unlock();
                return status;
            }
        }
    }
#endif 

#ifndef ANDROID_DEFAULT_CODE
    // give a chance to let APacketSource return from read
    if (stop) {
        Mutex::Autolock autoLock(mMiscStateLock);
        if (mRTSPController != NULL)
            mRTSPController->stop();

       if (mExtractor != NULL) {
            LOGI("stop extractor in reset");
            mExtractor->stopParsing();
        } else {
            LOGI("set flag for stopped");
            mStopped = true;
        }
    } else {
        Mutex::Autolock autoLock(mMiscStateLock);
        if(mRTSPController != NULL){
            if (!(mExtractorFlags & MediaExtractor::CAN_PAUSE)){
                return OK;
            }
        }
    }
#endif // #ifndef ANDROID_DEFAULT_CODE
    Mutex::Autolock autoLock(mLock);
#ifndef ANDROID_DEFAULT_CODE
    if ((mFlags & CACHE_UNDERRUN)) {
        LOGI("pausing when buffering, notify 100 for AP");
        notifyListener_l(MEDIA_BUFFERING_UPDATE, 100);
        notifyListener_l(MEDIA_INFO, MEDIA_INFO_BUFFERING_END);
    }
#endif

    modifyFlags(CACHE_UNDERRUN, CLEAR);

    return pause_l();
}

status_t AwesomePlayer::pause_l(bool at_eos) {
    if (!(mFlags & PLAYING)) {
        return OK;
    }

    cancelPlayerEvents(true /* keepNotifications */);

    if (mAudioPlayer != NULL && (mFlags & AUDIO_RUNNING)) {
        if (at_eos) {
            // If we played the audio stream to completion we
            // want to make sure that all samples remaining in the audio
            // track's queue are played out.
            mAudioPlayer->pause(true /* playPendingSamples */);
        } else {
            mAudioPlayer->pause();
        }

        modifyFlags(AUDIO_RUNNING, CLEAR);
    }

    if (mFlags & TEXTPLAYER_STARTED) {
        mTextPlayer->pause();
        modifyFlags(TEXT_RUNNING, CLEAR);
    }

    modifyFlags(PLAYING, CLEAR);

#ifndef ANDROID_DEFAULT_CODE
    status_t pauseDoneRes = OK;
    if(!(mFlags & CACHE_UNDERRUN) && (!at_eos)){  //if is not  auto pause when buffer upadat and is not eos,we will not send puase to server
        if( mRTSPController!= NULL){
            LOGI("[rtsp] rtsp send pause to server!!!\n");
            pauseDoneRes = mRTSPController->sendPause();  //Send PAUSE to Server
        }
    }
#endif
    if (mDecryptHandle != NULL) {
        mDrmManagerClient->setPlaybackStatus(mDecryptHandle,
                Playback::PAUSE, 0);
    }

    uint32_t params = IMediaPlayerService::kBatteryDataTrackDecoder;
    if ((mAudioSource != NULL) && (mAudioSource != mAudioTrack)) {
        params |= IMediaPlayerService::kBatteryDataTrackAudio;
    }
    if (mVideoSource != NULL) {
        params |= IMediaPlayerService::kBatteryDataTrackVideo;
#ifndef ANDROID_DEFAULT_CODE
    	{
    	      
		if (reinterpret_cast<OMXCodec *>(mVideoSource.get())->
				vDecSwitchBwTVout(true) != OK)
			LOGE("pasue:reset vDecSwitchBwTVout error");
	}
#endif
    }

    addBatteryData(params);

#ifndef ANDROID_DEFAULT_CODE
    if(mRTSPController != NULL){
        if (at_eos) {
            mRTSPController->stop();
        }
        return pauseDoneRes;
    } else {
        return OK;
    }
#else
    return OK;
#endif

}

bool AwesomePlayer::isPlaying() const {
#ifndef ANDROID_DEFAULT_HTTP_STREAM
    if (mCachedSourcePauseResponseState & PausePending) {
        return false;
    }
#endif
#ifndef ANDROID_DEFAULT_CODE
	if (mCachedSource != NULL || mRTSPController != NULL) {//DO NOT use lock in streaming
		return (mFlags & PLAYING) || (mFlags & CACHE_UNDERRUN);
	}
	Mutex::Autolock autoLock(mLock);
#endif
    return (mFlags & PLAYING) || (mFlags & CACHE_UNDERRUN);
}

#ifndef ANDROID_DEFAULT_CODE
bool AwesomePlayer::isPlaying_l() const {
    if (mCachedSourcePauseResponseState & PausePending) {
        return false;
    }
    return (mFlags & PLAYING) || (mFlags & CACHE_UNDERRUN);
}
#endif

status_t AwesomePlayer::setSurfaceTexture(const sp<ISurfaceTexture> &surfaceTexture) {
    Mutex::Autolock autoLock(mLock);

    status_t err;
    if (surfaceTexture != NULL) {
        err = setNativeWindow_l(new SurfaceTextureClient(surfaceTexture));
    } else {
        err = setNativeWindow_l(NULL);
    }

    return err;
}

void AwesomePlayer::shutdownVideoDecoder_l() {
#ifndef ANDROID_DEFAULT_CODE
    if (mFirstVideoBuffer) {
        mFirstVideoBuffer->release();
        mFirstVideoBuffer = NULL;
        mFirstVideoBufferStatus = OK;
    }
#endif
    if (mVideoBuffer) {
        mVideoBuffer->release();
        mVideoBuffer = NULL;
    }

    mVideoSource->stop();

    // The following hack is necessary to ensure that the OMX
    // component is completely released by the time we may try
    // to instantiate it again.
    wp<MediaSource> tmp = mVideoSource;
    mVideoSource.clear();
    while (tmp.promote() != NULL) {
        usleep(1000);
    }
    IPCThreadState::self()->flushCommands();
    LOGV("video decoder shutdown completed");
}

status_t AwesomePlayer::setNativeWindow_l(const sp<ANativeWindow> &native) {
    mNativeWindow = native;

    if (mVideoSource == NULL) {
        return OK;
    }

    LOGV("attempting to reconfigure to use new surface");

    bool wasPlaying = (mFlags & PLAYING) != 0;

    pause_l();
    mVideoRenderer.clear();

    shutdownVideoDecoder_l();

    status_t err = initVideoDecoder();

    if (err != OK) {
        LOGE("failed to reinstantiate video decoder after surface change.");
        return err;
    }

    if (mLastVideoTimeUs >= 0) {
//ALPS00108664, using audioTimeUs to replace videoTimeus
#ifndef ANDROID_DEFAULT_CODE
	int64_t position;
    int64_t lastPositionUs = mLastPositionUs;
	getPosition(&position);
	LOGD("lastPositionUs =%lld, position=%lld",lastPositionUs,position);
	// second getpositon > first getpostion, should seek to first postion,or else CTS fail
	if(lastPositionUs != -1 && position - lastPositionUs < 200*1000
       && position - lastPositionUs >0 ){
                      mSeekTimeUs = lastPositionUs ;			 	
	}	
	else		
                      mSeekTimeUs = position;
        mSeeking = SEEK;
#else  //ANDROID_DEFAULT_CODE
        mSeeking = SEEK;
	mSeekTimeUs = mLastVideoTimeUs;
#endif
        modifyFlags((AT_EOS | AUDIO_AT_EOS | VIDEO_AT_EOS), CLEAR);
    }

    if (wasPlaying) {
        play_l();
    }

    return OK;
}

void AwesomePlayer::setAudioSink(
        const sp<MediaPlayerBase::AudioSink> &audioSink) {
    Mutex::Autolock autoLock(mLock);

    mAudioSink = audioSink;
}

status_t AwesomePlayer::setLooping(bool shouldLoop) {
    Mutex::Autolock autoLock(mLock);

    modifyFlags(LOOPING, CLEAR);

    if (shouldLoop) {
        modifyFlags(LOOPING, SET);
    }

    return OK;
}

status_t AwesomePlayer::getDuration(int64_t *durationUs) {
    Mutex::Autolock autoLock(mMiscStateLock);

    if (mDurationUs < 0) {
        return UNKNOWN_ERROR;
    }

    *durationUs = mDurationUs;

    return OK;
}

#ifndef ANDROID_DEFAULT_CODE
status_t AwesomePlayer::getPosition(int64_t *positionUs) {

    if (mSeeking != NO_SEEK) {
        *positionUs = mSeekTimeUs;
         mLastPositionUs = *positionUs;
        return OK;
    } else if (mVideoSource != NULL
            && (mAudioPlayer == NULL || !(mFlags & VIDEO_AT_EOS))) {
        Mutex::Autolock autoLock(mMiscStateLock);
        *positionUs = mVideoTimeUs + mLatencyUs;
    } else if (mAudioPlayer != NULL) {
        *positionUs = mAudioPlayer->getMediaTimeUs();// + mLatencyUs;
    } else {
        *positionUs = 0;
    }

    if (mRTSPController != NULL) {
        *positionUs = mRTSPController->getNormalPlayTimeUs(*positionUs);
    }
    mLastPositionUs = *positionUs;
    return OK;
}
#else
status_t AwesomePlayer::getPosition(int64_t *positionUs) {
    if (mRTSPController != NULL) {
        *positionUs = mRTSPController->getNormalPlayTimeUs();
    }
    else if (mSeeking != NO_SEEK) {
        *positionUs = mSeekTimeUs;
    } else if (mVideoSource != NULL
            && (mAudioPlayer == NULL || !(mFlags & VIDEO_AT_EOS))) {
        Mutex::Autolock autoLock(mMiscStateLock);
        *positionUs = mVideoTimeUs;
    } else if (mAudioPlayer != NULL) {
        *positionUs = mAudioPlayer->getMediaTimeUs();
    } else {
        *positionUs = 0;
    }

    return OK;
}
#endif // ANDROID_DEFAULT_CODE

status_t AwesomePlayer::seekTo(int64_t timeUs) {
	LOGD("seekTo %lld ms",timeUs/1000);
	
    if (mExtractorFlags & MediaExtractor::CAN_SEEK) {
        Mutex::Autolock autoLock(mLock);
        return seekTo_l(timeUs);
    }
#ifndef ANDROID_DEFAULT_CODE

    {
        Mutex::Autolock autoLock(mLock);
        notifyListener_l(MEDIA_SEEK_COMPLETE);
    }
#endif  //#ifndef ANDROID_DEFAULT_CODE

    return OK;
}

status_t AwesomePlayer::setTimedTextTrackIndex(int32_t index) {
    if (mTextPlayer != NULL) {
        if (index >= 0) { // to turn on a text track
            status_t err = mTextPlayer->setTimedTextTrackIndex(index);
            if (err != OK) {
                return err;
            }

            modifyFlags(TEXT_RUNNING, SET);
            modifyFlags(TEXTPLAYER_STARTED, SET);
            return OK;
        } else { // to turn off the text track display
            if (mFlags  & TEXT_RUNNING) {
                modifyFlags(TEXT_RUNNING, CLEAR);
            }
            if (mFlags  & TEXTPLAYER_STARTED) {
                modifyFlags(TEXTPLAYER_STARTED, CLEAR);
            }

            return mTextPlayer->setTimedTextTrackIndex(index);
        }
    } else {
        return INVALID_OPERATION;
    }
}

// static
void AwesomePlayer::OnRTSPSeekDoneWrapper(void *cookie) {
    static_cast<AwesomePlayer *>(cookie)->onRTSPSeekDone();
}

void AwesomePlayer::onRTSPSeekDone() {
    if (!mSeekNotificationSent) {
        notifyListener_l(MEDIA_SEEK_COMPLETE);
        mSeekNotificationSent = true;
    }
}

status_t AwesomePlayer::seekTo_l(int64_t timeUs) {
	LOGD("seekTo_l");
    if (mRTSPController != NULL) {
        mSeekNotificationSent = false;
#ifndef ANDROID_DEFAULT_CODE
        status_t err = mRTSPController->preSeek(timeUs, OnRTSPSeekDoneWrapper, this);
        if (err != OK) {
            LOGW("AwesomePlayer: ignore too frequently seeks");
            return OK;
        }
#endif // #ifndef ANDROID_DEFAULT_CODE
        mRTSPController->seekAsync(timeUs, OnRTSPSeekDoneWrapper, this);
#ifdef ANDROID_DEFAULT_CODE
        return OK;
#endif // #ifndef ANDROID_DEFAULT_CODE
    }

    if (mFlags & CACHE_UNDERRUN) {
        modifyFlags(CACHE_UNDERRUN, CLEAR);
		LOGD("play_l in underrun");
        play_l();
    }
#ifndef ANDROID_DEFAULT_CODE
    // quickly report buffering status when seeking in playing
    else if (mRTSPController != NULL && (mFlags & PLAYING)) {
        onBufferingUpdate_l();
    }
#endif // #ifndef ANDROID_DEFAULT_CODE

    if ((mFlags & PLAYING) && mVideoSource != NULL && (mFlags & VIDEO_AT_EOS)) {
        // Video playback completed before, there's no pending
        // video event right now. In order for this new seek
        // to be honored, we need to post one.

		LOGD("Video at eos when seek");
        postVideoEvent_l();
    }

    mSeeking = SEEK;
    mSeekNotificationSent = false;
    mSeekTimeUs = timeUs;
    modifyFlags((AT_EOS | AUDIO_AT_EOS | VIDEO_AT_EOS), CLEAR);
    
    //TODO:
    //add try read here
#ifndef ANDROID_DEFAULT_HTTP_STREAM

    if (INFO_TRY_READ_FAIL == tryReadIfNeccessary_l()) {
        LOGI("try read fail, cache is missing (flag = 0x%x | MISSING)", mFlags);
        modifyFlags(CACHE_MISSING, SET);
        if (mVideoSource != NULL) {
            mVideoSource->pause();  //pause the omxcodec
        }
        if (mFlags & PLAYING) {
              LOGD("trying read: mFlags = 0x%x", mFlags); 
//            pause_l();
            cancelPlayerEvents(true);
            if (mAudioPlayer != NULL && (mFlags & AUDIOPLAYER_STARTED)) {
				LOGD("mAudioPlayer->pause()");
				mAudioPlayer->pause();
            }            
        }
    }
#endif


    seekAudioIfNecessary_l();

    if (mFlags & TEXTPLAYER_STARTED) {
        mTextPlayer->seekTo(mSeekTimeUs);
    }

    if (!(mFlags & PLAYING)) {
        LOGV("seeking while paused, sending SEEK_COMPLETE notification"
             " immediately.");

#ifndef ANDROID_DEFAULT_CODE
        // preview for rtsp is not a good thing, return here
        if(mRTSPController != NULL){
            return OK;
        }
#endif

        notifyListener_l(MEDIA_SEEK_COMPLETE);
        mSeekNotificationSent = true;

       if ((mFlags & PREPARED) && mVideoSource != NULL 
#ifndef ANDROID_DEFAULT_HTTP_STREAM
               && !(mFlags & CACHE_MISSING)   // seek preview will be done when cache shot again
#endif
               ) {
            modifyFlags(SEEK_PREVIEW, SET);
            postVideoEvent_l();
        }
    }

    return OK;
}

void AwesomePlayer::seekAudioIfNecessary_l() {
    if (mSeeking != NO_SEEK && mVideoSource == NULL && mAudioPlayer != NULL) {
#ifndef ANDROID_DEFAULT_CODE
        // reset/set variables before async call
        mWatchForAudioSeekComplete = true;
        mWatchForAudioEOS = true;
        mSeekNotificationSent = false;
        if (mRTSPController != NULL)
            mAudioPlayer->seekTo(INT64_MAX);
        else
#endif // #ifndef ANDROID_DEFAULT_CODE
        mAudioPlayer->seekTo(mSeekTimeUs);

#ifdef ANDROID_DEFAULT_CODE
        mWatchForAudioSeekComplete = true;
        mWatchForAudioEOS = true;
#endif // #ifndef ANDROID_DEFAULT_CODE

        if (mDecryptHandle != NULL) {
            mDrmManagerClient->setPlaybackStatus(mDecryptHandle,
                    Playback::PAUSE, 0);
            mDrmManagerClient->setPlaybackStatus(mDecryptHandle,
                    Playback::START, mSeekTimeUs / 1000);
        }
    }
}

void AwesomePlayer::setAudioSource(sp<MediaSource> source) {
    CHECK(source != NULL);

    mAudioTrack = source;
}

void AwesomePlayer::addTextSource(sp<MediaSource> source) {
    Mutex::Autolock autoLock(mTimedTextLock);
    CHECK(source != NULL);

    if (mTextPlayer == NULL) {
        mTextPlayer = new TimedTextPlayer(this, mListener, &mQueue);
    }

    mTextPlayer->addTextSource(source);
}

status_t AwesomePlayer::initAudioDecoder() {
    sp<MetaData> meta = mAudioTrack->getFormat();

    const char *mime;
    CHECK(meta->findCString(kKeyMIMEType, &mime));

    if (!strcasecmp(mime, MEDIA_MIMETYPE_AUDIO_RAW)) {
        mAudioSource = mAudioTrack;
    } else {
        mAudioSource = OMXCodec::Create(
                mClient.interface(), mAudioTrack->getFormat(),
                false, // createEncoder
                mAudioTrack);
    }

    if (mAudioSource != NULL) {
        int64_t durationUs;
        if (mAudioTrack->getFormat()->findInt64(kKeyDuration, &durationUs)) {
            Mutex::Autolock autoLock(mMiscStateLock);
            if (mDurationUs < 0 || durationUs > mDurationUs) {
                mDurationUs = durationUs;
            }
        }

#if !defined(ANDROID_DEFAULT_CODE) && (defined(MT6575) || defined(MT6577))
        status_t err;
        if (mRTSPController != NULL && !strcasecmp(mime, MEDIA_MIMETYPE_AUDIO_AAC)) {
            sp<MetaData> meta = new MetaData;
            meta->setInt32(kKeyInputBufferNum, 1);
            err = mAudioSource->start(meta.get());
        } else if((true == IsCMMBPlayer) && !strcasecmp(mime, MEDIA_MIMETYPE_AUDIO_AAC)){
            LOGE("initAudioDecoder cmmb into");
            sp<MetaData> meta = new MetaData;
            meta->setInt32(kKeyInputBufferNum, 2);
			meta->setInt32(kKeyMaxQueueBuffer, 4);
            err = mAudioSource->start(meta.get());
        
		} else {
            err = mAudioSource->start();
        }
#else
        status_t err = mAudioSource->start();
#endif // #ifndef ANDROID_DEFAULT_CODE

        if (err != OK) {
            mAudioSource.clear();
            return err;
        }
    } else if (!strcasecmp(mime, MEDIA_MIMETYPE_AUDIO_QCELP)) {
        // For legacy reasons we're simply going to ignore the absence
        // of an audio decoder for QCELP instead of aborting playback
        // altogether.
        return OK;
    }

    if (mAudioSource != NULL) {
        Mutex::Autolock autoLock(mStatsLock);
        TrackStat *stat = &mStats.mTracks.editItemAt(mStats.mAudioTrackIndex);

        const char *component;
        if (!mAudioSource->getFormat()
                ->findCString(kKeyDecoderComponent, &component)) {
            component = "none";
        }

        stat->mDecoderName = component;
    }

    return mAudioSource != NULL ? OK : UNKNOWN_ERROR;
}

void AwesomePlayer::setVideoSource(sp<MediaSource> source) {
    CHECK(source != NULL);

    mVideoTrack = source;
}


#ifndef ANDROID_DEFAULT_CODE 
void AwesomePlayer::mtk_omx_get_current_time(int64_t* pReal_time)
{

	//if((mFlags & FIRST_FRAME) || mSeeking!=NO_SEEK )//disable seek video only
	if((mFlags & FIRST_FRAME) || mSeeking == SEEK )
	{
		*pReal_time = -1;
	}
	else
	{
		*pReal_time = mAVSyncTimeUs;
	}
 	//LOGE("###*pReal_time=%lld us",(*pReal_time));
	
}
#endif 


status_t AwesomePlayer::initVideoDecoder(uint32_t flags) {

    // Either the application or the DRM system can independently say
    // that there must be a hardware-protected path to an external video sink.
    // For now we always require a hardware-protected path to external video sink
    // if content is DRMed, but eventually this could be optional per DRM agent.
    // When the application wants protection, then
    //   (USE_SURFACE_ALLOC && (mSurface != 0) &&
    //   (mSurface->getFlags() & ISurfaceComposer::eProtectedByApp))
    // will be true, but that part is already handled by SurfaceFlinger.

#ifndef ANDROID_DEFAULT_CODE 
	 sp<MetaData> meta = mVideoTrack->getFormat();
	 meta->setPointer(kkeyOmxTimeSource, this);
#endif	

#ifdef DEBUG_HDCP
    // For debugging, we allow a system property to control the protected usage.
    // In case of uninitialized or unexpected property, we default to "DRM only".
    bool setProtectionBit = false;
    char value[PROPERTY_VALUE_MAX];
    if (property_get("persist.sys.hdcp_checking", value, NULL)) {
        if (!strcmp(value, "never")) {
            // nop
        } else if (!strcmp(value, "always")) {
            setProtectionBit = true;
        } else if (!strcmp(value, "drm-only")) {
            if (mDecryptHandle != NULL) {
                setProtectionBit = true;
            }
        // property value is empty, or unexpected value
        } else {
            if (mDecryptHandle != NULL) {
                setProtectionBit = true;
            }
        }
    // can' read property value
    } else {
        if (mDecryptHandle != NULL) {
            setProtectionBit = true;
        }
    }
    // note that usage bit is already cleared, so no need to clear it in the "else" case
    if (setProtectionBit) {
        flags |= OMXCodec::kEnableGrallocUsageProtected;
    }
#else
    if (mDecryptHandle != NULL) {
#ifndef ANDROID_DEFAULT_CODE
        if (mDecryptHandle->status != RightsStatus::RIGHTS_VALID)
#endif
            flags |= OMXCodec::kEnableGrallocUsageProtected;
    }
#endif
    LOGV("initVideoDecoder flags=0x%x", flags);
    mVideoSource = OMXCodec::Create(
            mClient.interface(), mVideoTrack->getFormat(),
            false, // createEncoder
            mVideoTrack,
            NULL, flags, USE_SURFACE_ALLOC ? mNativeWindow : NULL);

    if (mVideoSource != NULL) {
        int64_t durationUs;
#ifdef ANDROID_DEFAULT_CODE
        // set when video is ok
        if (mVideoTrack->getFormat()->findInt64(kKeyDuration, &durationUs)) {
            Mutex::Autolock autoLock(mMiscStateLock);
            if (mDurationUs < 0 || durationUs > mDurationUs) {
                mDurationUs = durationUs;
            }
        }
#endif

#ifndef ANDROID_DEFAULT_CODE
        status_t err;
        if (mRTSPController != NULL) {
            sp<MetaData> meta = new MetaData;
            meta->setInt32(kKeyRTSPSeekMode, 1);
            meta->setInt32(kKeyMaxQueueBuffer, 1);
            meta->setInt32(kKeyInputBufferNum, 4);
            err = mVideoSource->start(meta.get());
        }else if(true == IsCMMBPlayer){
            sp<MetaData> meta = new MetaData;
            //meta->setInt32(kKeyMaxQueueBuffer, 1);
            meta->setInt32(kKeyInputBufferNum, 4);
            err = mVideoSource->start(meta.get());
        } else {
            err = mVideoSource->start();
        }
        if (mCachedSource == NULL && err == OK && mMetaData != NULL) {
            int check = false;
            if (mMetaData->findInt32(kKeyVideoPreCheck, &check) && check) {
                err = mVideoSource->read(&mFirstVideoBuffer);
                LOGI("detect video capability by decoder %d %d", err, mFirstVideoBuffer != NULL);
                mFirstVideoBufferStatus = err;
                if (err == INFO_FORMAT_CHANGED || err == ERROR_END_OF_STREAM) {
                    err = OK;
                } else if (err != OK) {
                    shutdownVideoDecoder_l();
                }
            }
        }
#else
        status_t err = mVideoSource->start();
#endif // #ifndef ANDROID_DEFAULT_CODE

        if (err != OK) {
            mVideoSource.clear();
            return err;
        }
#ifndef ANDROID_DEFAULT_CODE
        if (mVideoTrack->getFormat()->findInt64(kKeyDuration, &durationUs)) {
            Mutex::Autolock autoLock(mMiscStateLock);
            if (mDurationUs < 0 || durationUs > mDurationUs) {
                mDurationUs = durationUs;
            }
    }
#endif
    }

    if (mVideoSource != NULL) {
        const char *componentName;
        CHECK(mVideoSource->getFormat()
                ->findCString(kKeyDecoderComponent, &componentName));

        {
            Mutex::Autolock autoLock(mStatsLock);
            TrackStat *stat = &mStats.mTracks.editItemAt(mStats.mVideoTrackIndex);

            stat->mDecoderName = componentName;
        }

        static const char *kPrefix = "OMX.Nvidia.";
        static const char *kSuffix = ".decode";
        static const size_t kSuffixLength = strlen(kSuffix);

        size_t componentNameLength = strlen(componentName);

        if (!strncmp(componentName, kPrefix, strlen(kPrefix))
                && componentNameLength >= kSuffixLength
                && !strcmp(&componentName[
                    componentNameLength - kSuffixLength], kSuffix)) {
            modifyFlags(SLOW_DECODER_HACK, SET);
        }
#ifndef ANDROID_DEFAULT_CODE		
		modifyFlags(SLOW_DECODER_HACK, SET);
#endif
    }

    return mVideoSource != NULL ? OK : UNKNOWN_ERROR;
}

void AwesomePlayer::finishSeekIfNecessary(int64_t videoTimeUs) {
    if (mSeeking == SEEK_VIDEO_ONLY) {
        mSeeking = NO_SEEK;
        return;
    }

    if (mSeeking == NO_SEEK || (mFlags & SEEK_PREVIEW)) {
        return;
    }

    if (mAudioPlayer != NULL) {
        LOGV("seeking audio to %lld us (%.2f secs).", videoTimeUs, videoTimeUs / 1E6);

        // If we don't have a video time, seek audio to the originally
        // requested seek time instead.

#ifndef ANDROID_DEFAULT_CODE
        // reset/set variables before async call
        mWatchForAudioSeekComplete = true;
        mWatchForAudioEOS = true;
        if (mRTSPController != NULL)
            mAudioPlayer->seekTo(INT64_MAX);
        else
#endif // #ifndef ANDROID_DEFAULT_CODE
        mAudioPlayer->seekTo(videoTimeUs < 0 ? mSeekTimeUs : videoTimeUs);
#ifdef ANDROID_DEFAULT_CODE
        mWatchForAudioSeekComplete = true;
        mWatchForAudioEOS = true;
#endif // #ifndef ANDROID_DEFAULT_CODE
    } else if (!mSeekNotificationSent) {
        // If we're playing video only, report seek complete now,
        // otherwise audio player will notify us later.
        notifyListener_l(MEDIA_SEEK_COMPLETE);
        mSeekNotificationSent = true;
    }

    modifyFlags(FIRST_FRAME, SET);
    mSeeking = NO_SEEK;

    if (mDecryptHandle != NULL) {
        mDrmManagerClient->setPlaybackStatus(mDecryptHandle,
                Playback::PAUSE, 0);
        mDrmManagerClient->setPlaybackStatus(mDecryptHandle,
                Playback::START, videoTimeUs / 1000);
    }
}

void AwesomePlayer::onVideoEvent() {
    Mutex::Autolock autoLock(mLock);
    if (!mVideoEventPending) {
        // The event has been cancelled in reset_l() but had already
        // been scheduled for execution at that time.
        return;
    }
    mVideoEventPending = false;

    if (mSeeking != NO_SEEK) {
#ifndef ANDROID_DEFAULT_CODE
        mAudioNormalEOS = false;
        if (mFirstVideoBuffer) {
            mFirstVideoBuffer->release();
            mFirstVideoBuffer = NULL;
        }
        mFirstVideoBufferStatus = OK;
#endif // #ifndef ANDROID_DEFAULT_CODE
        if (mVideoBuffer) {
            mVideoBuffer->release();
            mVideoBuffer = NULL;
        }

        if (mSeeking == SEEK && isStreamingHTTP() && mAudioSource != NULL
                && !(mFlags & SEEK_PREVIEW)) {
            // We're going to seek the video source first, followed by
            // the audio source.
            // In order to avoid jumps in the DataSource offset caused by
            // the audio codec prefetching data from the old locations
            // while the video codec is already reading data from the new
            // locations, we'll "pause" the audio source, causing it to
            // stop reading input data until a subsequent seek.

            if (mAudioPlayer != NULL && (mFlags & AUDIO_RUNNING)) {
                mAudioPlayer->pause();

                modifyFlags(AUDIO_RUNNING, CLEAR);
            }
            mAudioSource->pause();
        }
    }

    if (!mVideoBuffer) {
		MediaSource::ReadOptions options;
		
#ifndef ANDROID_DEFAULT_CODE 
        bool bSEEK_VIDEO_ONLY=false;
        if ((mRTSPController==NULL) && (mCachedSource == NULL))
        {
            const char *mime;			
            CHECK(mMetaData->findCString(kKeyMIMEType, &mime));
            if((mSeeking == SEEK_VIDEO_ONLY)&& (!strcasecmp("video/mp4", mime)))
            {
                LOGD("### mime=%s======SEEK_VIDEO_ONLY now====",mime);
				bSEEK_VIDEO_ONLY =true;
            }
        }
#endif //#ifndef ENABLE_PERF_JUMP_KEY_MECHANISM

        
        if (mSeeking != NO_SEEK) {
            LOGV("seeking to %lld us (%.2f secs)", mSeekTimeUs, mSeekTimeUs / 1E6);

            options.setSeekTo(
                    mSeekTimeUs,
#ifndef ANDROID_DEFAULT_CODE
					bSEEK_VIDEO_ONLY
#else
                    mSeeking == SEEK_VIDEO_ONLY
#endif                    
                        ? MediaSource::ReadOptions::SEEK_NEXT_SYNC
#ifndef ANDROID_DEFAULT_CODE
						: (mPrerollEnable ? (MediaSource::ReadOptions::SEEK_CLOSEST) : (MediaSource::ReadOptions::SEEK_CLOSEST_SYNC)));                        
#else
						: MediaSource::ReadOptions::SEEK_CLOSEST_SYNC);
#endif
        }
        for (;;) {
#ifndef ANDROID_DEFAULT_CODE
            status_t err = OK;
            if (mFirstVideoBuffer != NULL) {
                mVideoBuffer = mFirstVideoBuffer;
                err = mFirstVideoBufferStatus;
                mFirstVideoBuffer = NULL;
                mFirstVideoBufferStatus = OK;
                LOGI("using first video buffer and status %d", mFirstVideoBufferStatus);
            } else {
                err = mVideoSource->read(&mVideoBuffer, &options);
            }
#else
            status_t err = mVideoSource->read(&mVideoBuffer, &options);
#endif
            options.clearSeekTo();


//wait for 			
#ifndef ANDROID_DEFAULT_CODE
#if defined(MT6575) || defined(MT6577)
            if (mFirstSubmit) 
            {
                char value[PROPERTY_VALUE_MAX];
                sp<MetaData> _meta = mVideoSource->getFormat();
                int32_t _videowidth;
                int32_t _videoheight;
                CHECK(_meta->findInt32(kKeyWidth, &_videowidth));
                CHECK(_meta->findInt32(kKeyHeight, &_videoheight));
                if ((_videowidth <= 864) && (_videoheight <= 480)) {
                    property_get("sf.video.prebuffer.cnt", value, "1");
                } 
                else {
                    property_get("sf.video.prebuffer.cnt", value, "5");
                }
                size_t prebufferCount = atoi(value);
                LOGD("@@[SF_PROPERTY]sf.video.prebuffer.cnt=%d, VideoWidth(%d), VideoHeight(%d)", prebufferCount, _videowidth, _videoheight);

                size_t buffersOwn;
                int loopCount = 0;
                while ((buffersOwn = ((OMXCodec*)mVideoSource.get())->buffersOwn()) < prebufferCount) {
                //	            LOGD ("@@buffersOwn = %d", buffersOwn);
                    usleep (10*1000);
                    loopCount++;
                    if (loopCount == 100) {
                        LOGE ("Oops, prebuffer time > 1s");
                        break;
                    }
                }
                mFirstSubmit = false;
            }
#endif
#endif
            if (err != OK) {
                CHECK(mVideoBuffer == NULL);
                if (err == INFO_FORMAT_CHANGED) {
                    LOGV("VideoSource signalled format change.");

                    notifyVideoSize_l();

                    if (mVideoRenderer != NULL) {
                        mVideoRendererIsPreview = false;
                        initRenderer_l();
                    }
                    continue;
                }

                // So video playback is complete, but we may still have
                // a seek request pending that needs to be applied
                // to the audio track.
                if (mSeeking != NO_SEEK) {
                    LOGV("video stream ended while seeking!");
#ifndef ANDROID_DEFAULT_CODE
                    //when video EOS, set mVideoTimeUs to avoid of getposition error
                    if (mRTSPController == NULL) {
                        LOGI("mSeekTimeUs=%lld,mDurationUs=%lld",mSeekTimeUs,mDurationUs);
                        if (mSeekTimeUs > mDurationUs)
                            mVideoTimeUs = mDurationUs;
                        else
                            mVideoTimeUs = mSeekTimeUs;
                    }
#endif
                }
                finishSeekIfNecessary(-1);

                if (mAudioPlayer != NULL && !(mFlags & (AUDIO_RUNNING | SEEK_PREVIEW))) {
                    startAudioPlayer_l();
                }

                modifyFlags(VIDEO_AT_EOS, SET);
#ifndef ANDROID_DEFAULT_CODE
                if (err == ERROR_UNSUPPORTED_VIDEO) {
                    LOGW("unsupportted video detected");
                    if (mAudioTrack != NULL || mAudioSource != NULL) {
                        notifyListener_l(MEDIA_SET_VIDEO_SIZE, 0, 0);
                        notifyListener_l(MEDIA_INFO, MEDIA_INFO_HAS_UNSUPPORT_VIDEO);
                        postStreamDoneEvent_l(ERROR_END_OF_STREAM);
                        mFinalStopFlag |= FINAL_HAS_UNSUPPORT_VIDEO;
                        if (mRTSPController != NULL) {
                            mVideoTrack->stop();
                            int64_t durationUs;
                            if (mAudioTrack->getFormat()->findInt64(kKeyDuration, &durationUs)) {
                                Mutex::Autolock autoLock(mMiscStateLock);
                                if (durationUs > 0 && durationUs < mDurationUs) {
                                    LOGI("update duration from %lld to %lld", mDurationUs, durationUs);
                                    mDurationUs = durationUs;
                                }
                            }
                        }
                        const char *mime;			
                        //CHECK(mMetaData->findCString(kKeyMIMEType, &mime));
                        if( mMetaData.get()!=NULL && mMetaData->findCString(kKeyMIMEType, &mime)){
	                        if(!strcasecmp(MEDIA_MIMETYPE_CONTAINER_MPEG2TS, mime) || !strcasecmp(MEDIA_MIMETYPE_VIDEO_WMV, mime))
	                        {
	                            mVideoTrack->stop();
	                            LOGE("stop video track");
	                        }
                      }
                    } 
                    else {
                        postStreamDoneEvent_l(ERROR_UNSUPPORTED);
                    }
                } 
                else {
                    postStreamDoneEvent_l(err);
                }
#else
                postStreamDoneEvent_l(err);
#endif
                return;
            }

            if (mVideoBuffer->range_length() == 0) {
                // Some decoders, notably the PV AVC software decoder
                // return spurious empty buffers that we just want to ignore.

                mVideoBuffer->release();
                mVideoBuffer = NULL;
                continue;
            }
#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_S3D_SUPPORT
           if (mVideoBuffer->meta_data()->findInt32(kKeyVideoStereoMode, (int32_t*)&mVideoStereoMode)){
		     LOGD("notify mVideoStereoMode:%d", mVideoStereoMode);
                   notifyListener_l(MEDIA_INFO, MEDIA_INFO_3D,(int32_t)mVideoStereoMode);
           }
#endif
#endif
#ifndef ANDROID_DEFAULT_HTTP_STREAM
            if (mFlags & CACHE_MISSING) {

                //the cache is shot again
                modifyFlags(CACHE_MISSING, CLEAR);
                if (isPlaying_l()) {
                    LOGD("CACHE_MISSING --> CACHE_UNDERRUN in VideoEvent");
                    modifyFlags(CACHE_UNDERRUN, SET);
                    pause_l();
                    notifyListener_l(MEDIA_INFO, MEDIA_INFO_BUFFERING_START);
                } else {
                    LOGD("CACHE_MISSING reset");
                }
                postBufferingEvent_l();
            }

#endif
            break;
        }

        {
            Mutex::Autolock autoLock(mStatsLock);
            ++mStats.mNumVideoFramesDecoded;
        }
    }

#ifndef ANDROID_DEFAULT_HTTP_STREAM
    if (mCachedSourcePauseResponseState & PausePending) {
        mCachedSourcePauseResponseState = 0; 
        pause_l();
        LOGI("pending pause done");
        return;
    }
#endif

    int64_t timeUs;
    CHECK(mVideoBuffer->meta_data()->findInt64(kKeyTime, &timeUs));

    mLastVideoTimeUs = timeUs;

    if (mSeeking == SEEK_VIDEO_ONLY) {
        if (mSeekTimeUs > timeUs) {
            LOGI("XXX mSeekTimeUs = %lld us, timeUs = %lld us",
                 mSeekTimeUs, timeUs);
        }
    }

#ifdef ANDROID_DEFAULT_CODE
    // set in the below
    {
        Mutex::Autolock autoLock(mMiscStateLock);
        mVideoTimeUs = timeUs;
    }
#endif // #ifndef ANDROID_DEFAULT_CODE

    SeekType wasSeeking = mSeeking;
#ifndef ANDROID_DEFAULT_CODE
    finishSeekIfNecessary(mPrerollEnable ? -1 : timeUs);
#else
	finishSeekIfNecessary(timeUs);
#endif

    if (mAudioPlayer != NULL && !(mFlags & (AUDIO_RUNNING | SEEK_PREVIEW))) {
#ifndef ANDROID_DEFAULT_CODE
		if (wasSeeking == SEEK && isStreamingHTTP() && mAudioSource != NULL) {
			LOGD("audio resume");
			reinterpret_cast<OMXCodec *>(mAudioSource.get())->resume();
		}
#endif
		status_t err = startAudioPlayer_l();
        if (err != OK) {
            LOGE("Starting the audio player failed w/ err %d", err);
            return;
        }
    }

    if ((mFlags & TEXTPLAYER_STARTED) && !(mFlags & (TEXT_RUNNING | SEEK_PREVIEW))) {
        mTextPlayer->resume();
        modifyFlags(TEXT_RUNNING, SET);
    }

    TimeSource *ts =
        ((mFlags & AUDIO_AT_EOS) || !(mFlags & AUDIOPLAYER_STARTED))
            ? &mSystemTimeSource : mTimeSource;

    if (mFlags & FIRST_FRAME) {
        modifyFlags(FIRST_FRAME, CLEAR);
        mSinceLastDropped = 0;
#ifndef ANDROID_DEFAULT_CODE
            mTimeSourceDeltaUs = mSystemTimeSource.getRealTimeUs() - timeUs; 
        LOGI("first frame delta %lld = real %lld - timeUs %lld", mTimeSourceDeltaUs, mSystemTimeSource.getRealTimeUs(), timeUs);
#else // #ifndef ANDROID_DEFAULT_CODE
        mTimeSourceDeltaUs = ts->getRealTimeUs() - timeUs;
#endif
    }

#ifndef ANDROID_DEFAULT_CODE
    ts = &mSystemTimeSource;
    int64_t realTimeUs, mediaTimeUs;
    if (!mAudioNormalEOS && mAudioPlayer != NULL && !(mFlags & SEEK_PREVIEW)) {
        status_t finalStatus;
        bool mapping = mAudioPlayer->getMediaTimeMapping(&realTimeUs, &mediaTimeUs);
        if (mWatchForAudioSeekComplete) {       
            LOGI("audio is seeking, seek time %lld", mSeekTimeUs);
            //if the audio seek is not complete, mediaTimeUs from AudioPlayer is wrong, so use "mSeekTimeUs"
            mediaTimeUs = mSeekTimeUs;
        }

        if (mAudioPlayer->reachedEOS(&finalStatus)) {
            LOGI("audio eos detected");
            int64_t mediaTimeNowUs = mAudioPlayer->getMediaTimeUs();
            if (mediaTimeNowUs > mLastAudioSeekUs && mapping) {
                mTimeSourceDeltaUs = mSystemTimeSource.getRealTimeUs() - mAudioPlayer->getRealTimeUs()
                    + realTimeUs - mediaTimeUs;
                LOGI("audio is normal EOS delta %lld now %lld real %lld media %lld", 
                        mTimeSourceDeltaUs, mediaTimeNowUs, realTimeUs, mediaTimeUs);
            }
            mAudioNormalEOS = true;
        } else if (mapping) {
            ts = mAudioPlayer;
            mTimeSourceDeltaUs = realTimeUs - mediaTimeUs;
        }
        else{
                ts = mAudioPlayer;
        	realTimeUs = mediaTimeUs = 0;
        	mTimeSourceDeltaUs = realTimeUs - mediaTimeUs;
        	LOGW("AudioPlayer have not completed real-Media Time maping,mTimeSourceDeltaUs=%lld",mTimeSourceDeltaUs);
        }
    }
    
    {
        Mutex::Autolock autoLock(mMiscStateLock);
        int64_t realTimeUs = ts->getRealTimeUs();
        if (realTimeUs < 0) {
            LOGW("realTimeUs %lld", realTimeUs);
            realTimeUs = 0;
        }
        mVideoTimeUs = realTimeUs - mTimeSourceDeltaUs;
    }
#else
    int64_t realTimeUs, mediaTimeUs;
    if (!(mFlags & AUDIO_AT_EOS) && mAudioPlayer != NULL
        && mAudioPlayer->getMediaTimeMapping(&realTimeUs, &mediaTimeUs)) {
        mTimeSourceDeltaUs = realTimeUs - mediaTimeUs;
        }
#endif

    if (wasSeeking == SEEK_VIDEO_ONLY) {
        int64_t nowUs = ts->getRealTimeUs() - mTimeSourceDeltaUs;

        int64_t latenessUs = nowUs - timeUs;

        if (latenessUs > 0) {
            LOGI("after SEEK_VIDEO_ONLY we're late by %.2f secs", latenessUs / 1E6);
        }
    }

    if (wasSeeking == NO_SEEK) {
        // Let's display the first frame after seeking right away.

        int64_t nowUs = ts->getRealTimeUs() - mTimeSourceDeltaUs;

        int64_t latenessUs = nowUs - timeUs;

    //qian  
#ifndef ANDROID_DEFAULT_CODE
	  mAVSyncTimeUs = nowUs;
#endif


#ifndef ANDROID_DEFAULT_CODE 
	    if((latenessUs > mAVSyncThreshold && mAVSyncThreshold >0) 
#else
        if (latenessUs > 500000ll
#endif 	
                && mRTSPController == NULL
                && mAudioPlayer != NULL
                && mAudioPlayer->getMediaTimeMapping(
                    &realTimeUs, &mediaTimeUs)) {
            LOGE("we're much too late (%.2f secs), video skipping ahead",
                 latenessUs / 1E6);

            mVideoBuffer->release();
            mVideoBuffer = NULL;

            mSeeking = SEEK_VIDEO_ONLY;
            mSeekTimeUs = mediaTimeUs;

#ifndef ANDROID_DEFAULT_CODE 
        // fast skip late frames
        	postVideoEvent_l(0);
#else
        	postVideoEvent_l();
#endif // #ifndef ANDROID_DEFAULT_CODE
            return;
        }

#ifndef ANDROID_DEFAULT_CODE 
		if (latenessUs > mLateMargin) 
#else
        if (latenessUs > 40000) 
#endif
		{
            // We're more than 40ms late.
            LOGE("we're late by %lld us (%.2f secs), timeUs(%lld), nowUs(%lld)", latenessUs, latenessUs / 1E6, timeUs, nowUs);
            if (!(mFlags & SLOW_DECODER_HACK)
#ifndef ANDROID_DEFAULT_CODE 
                     || (true == IsCMMBPlayer)
                     || (mSinceLastDropped > mFRAME_DROP_FREQ))//force Consective display Frames,can adjust,default =6
#else
        		|| mSinceLastDropped > FRAME_DROP_FREQ)
#endif // #ifndef ANDROID_DEFAULT_CODE				
                    
            {
                LOGE("we're late by %lld us (%.2f secs) dropping "
                     "one after %d frames",
                     latenessUs, latenessUs / 1E6, mSinceLastDropped);

                mSinceLastDropped = 0;
                mVideoBuffer->release();
                mVideoBuffer = NULL;

                {
                    Mutex::Autolock autoLock(mStatsLock);
                    ++mStats.mNumVideoFramesDropped;
                }

#ifndef ANDROID_DEFAULT_CODE 
       			 // fast skip late frames
        		postVideoEvent_l(0);
#else
        		postVideoEvent_l();
#endif // #ifndef ANDROID_DEFAULT_CODE
                return;
            }
        }
    //MTK_OP01_PROTECT_START
#ifndef ANDROID_DEFAULT_CODE
    //CMMB
     if ((true == IsCMMBPlayer) && (latenessUs < -5000000)) {//10000000 chage to wait 4s
	 // if early 10s we think it's error, discard it
        LOGE("error timestamp we are early by %lld us (%.2f secs)", latenessUs, latenessUs / 1E6);

        mVideoBuffer->release();
        mVideoBuffer = NULL;

        postVideoEvent_l();
        return;
    }
#endif
   //MTK_OP01_PROTECT_END
        if (latenessUs < -10000) {
            // We're more than 10ms early.
            //LOGD ("@@ 10ms early timeUs(%lld), nowUs(%lld)", timeUs, nowUs);
            LOGV("error timestamp we are early by %lld us (%.2f secs)", latenessUs, latenessUs / 1E6);
            postVideoEvent_l(10000);
            return;
        }
    }

    if ((mNativeWindow != NULL)
            && (mVideoRendererIsPreview || mVideoRenderer == NULL)) {
        mVideoRendererIsPreview = false;

        initRenderer_l();
    }

    if (mVideoRenderer != NULL) {
	//MTK_OP01_PROTECT_START
#ifndef ANDROID_DEFAULT_CODE
        //CMMB added.
	 if (true == IsCMMBCaptureOneFrame)
	 {
		 
	     mVideoBuffer->add_ref();
	     captureBuffer = mVideoBuffer;
	     
            IsCMMBCaptureOneFrame = false;
	     mCMMBCaptureCondition.signal();
	     LOGE("Capture one buffer");
	 }
#endif
        //MTK_OP01_PROTECT_END
        mSinceLastDropped++;
	//MTK_OP01_PROTECT_START
#ifndef ANDROID_DEFAULT_CODE
        if ((true == IsCMMBFirstFrame) && (true == IsCMMBPlayer))
        {
            IsCMMBFirstFrame = false;
	     notifyListener_l(MEDIA_INFO, MEDIA_INFO_CMMB_START_RENDER) ;
	}
#endif
        //MTK_OP01_PROTECT_END
        mVideoRenderer->render(mVideoBuffer);
    }

    mVideoBuffer->release();
    mVideoBuffer = NULL;

    if (wasSeeking != NO_SEEK && (mFlags & SEEK_PREVIEW)) {
        modifyFlags(SEEK_PREVIEW, CLEAR);
        return;
    }

    postVideoEvent_l();
}

void AwesomePlayer::postVideoEvent_l(int64_t delayUs) {
    if (mVideoEventPending) {
        return;
    }

    mVideoEventPending = true;
    mQueue.postEventWithDelay(mVideoEvent, delayUs < 0 ? 10000 : delayUs);
}

void AwesomePlayer::postStreamDoneEvent_l(status_t status) {
    if (mStreamDoneEventPending) {
        return;
    }
    mStreamDoneEventPending = true;

    mStreamDoneStatus = status;
    mQueue.postEvent(mStreamDoneEvent);
}

void AwesomePlayer::postBufferingEvent_l() {
    if (mBufferingEventPending) {
        return;
    }
    mBufferingEventPending = true;
#ifndef ANDROID_DEFAULT_CODE
    // reduce interval to 200ms as opencore
    mQueue.postEventWithDelay(mBufferingEvent, 200000ll);
#else
    mQueue.postEventWithDelay(mBufferingEvent, 1000000ll);
#endif // #ifndef ANDROID_DEFAULT_CODE
}

void AwesomePlayer::postVideoLagEvent_l() {
    if (mVideoLagEventPending) {
        return;
    }
    mVideoLagEventPending = true;
    mQueue.postEventWithDelay(mVideoLagEvent, 1000000ll);
}

void AwesomePlayer::postCheckAudioStatusEvent(int64_t delayUs) {
    Mutex::Autolock autoLock(mAudioLock);
    if (mAudioStatusEventPending) {
        return;
    }
    mAudioStatusEventPending = true;
    // Do not honor delay when looping in order to limit audio gap
    if (mFlags & (LOOPING | AUTO_LOOPING)) {
        delayUs = 0;
    }
    
    // AudioCache needn't delay post EOS, just AudioOutput which with a valid UID needed
    if(!mUIDValid){
        LOGI("AudioCache needn't delay post EOS!!!");
        delayUs = 0;
    }
    mQueue.postEventWithDelay(mCheckAudioStatusEvent, delayUs);
}

void AwesomePlayer::onCheckAudioStatus() {
    {
        Mutex::Autolock autoLock(mAudioLock);
        if (!mAudioStatusEventPending) {
            // Event was dispatched and while we were blocking on the mutex,
            // has already been cancelled.
            return;
        }

        mAudioStatusEventPending = false;
    }

    Mutex::Autolock autoLock(mLock);

    if (mWatchForAudioSeekComplete && !mAudioPlayer->isSeeking()) {
        mWatchForAudioSeekComplete = false;

#ifndef ANDROID_DEFAULT_CODE
        // this is used to detect a EOS right after seek
        mLastAudioSeekUs = mAudioPlayer->getMediaTimeUs();

        // RTSP has its own notification
        if (!mSeekNotificationSent && mRTSPController == NULL) {
#else
        if (!mSeekNotificationSent) {
#endif // #ifndef ANDROID_DEFAULT_CODE
            notifyListener_l(MEDIA_SEEK_COMPLETE);
            mSeekNotificationSent = true;
        }

        mSeeking = NO_SEEK;
    }

    status_t finalStatus;
    if (mWatchForAudioEOS && mAudioPlayer->reachedEOS(&finalStatus)) {
        mWatchForAudioEOS = false;
        modifyFlags(AUDIO_AT_EOS, SET);
        modifyFlags(FIRST_FRAME, SET);
#ifndef ANDROID_DEFAULT_CODE
		if(finalStatus == ERROR_UNSUPPORTED_AUDIO){
			notifyListener_l(MEDIA_INFO, MEDIA_INFO_HAS_UNSUPPORT_AUDIO);
			 mFinalStopFlag |=FINAL_HAS_UNSUPPORT_AUDIO;
			postStreamDoneEvent_l(ERROR_END_OF_STREAM);
		}else{
#endif		
			postStreamDoneEvent_l(finalStatus);
#ifndef ANDROID_DEFAULT_CODE
		}
#endif
    }
}

status_t AwesomePlayer::prepare() {
    Mutex::Autolock autoLock(mLock);
    return prepare_l();
}

status_t AwesomePlayer::prepare_l() {
    if (mFlags & PREPARED) {
        return OK;
    }

    if (mFlags & PREPARING) {
        return UNKNOWN_ERROR;
    }

    mIsAsyncPrepare = false;
    status_t err = prepareAsync_l();

    if (err != OK) {
        return err;
    }

    while (mFlags & PREPARING) {
        mPreparedCondition.wait(mLock);
    }

#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_DRM_APP
    // OMA DRM v1 implementation: consume rights.
    isCurrentComplete = false;
    if (mDecryptHandle != NULL) {
        LOGD("AwesomePlayer, consumeRights @prepare_l()");
        // in some cases, the mFileSource may be NULL (E.g. play audio directly in File Manager)
        // We don't know, but we assume it's a OMA DRM v1 case (DecryptApiType::CONTAINER_BASED)
        if ((mFileSource.get() != NULL && (mFileSource->flags() & OMADrmFlag) != 0)
            || (DecryptApiType::CONTAINER_BASED == mDecryptHandle->decryptApiType)) {
            if (!DrmMtkUtil::isTrustedVideoClient(mDrmValue)) {
                mDrmManagerClient->consumeRights(mDecryptHandle, Action::PLAY, false);
            }
        }
    }
#endif
#endif

    return mPrepareResult;
}

status_t AwesomePlayer::prepareAsync() {
    Mutex::Autolock autoLock(mLock);

    if (mFlags & PREPARING) {
        return UNKNOWN_ERROR;  // async prepare already pending
    }

    mIsAsyncPrepare = true;
    return prepareAsync_l();
}

status_t AwesomePlayer::prepareAsync_l() {
    if (mFlags & PREPARING) {
        return UNKNOWN_ERROR;  // async prepare already pending
    }

    if (!mQueueStarted) {
        mQueue.start();
        mQueueStarted = true;
    }

    modifyFlags(PREPARING, SET);
    mAsyncPrepareEvent = new AwesomeEvent(
            this, &AwesomePlayer::onPrepareAsyncEvent);

    mQueue.postEvent(mAsyncPrepareEvent);

    return OK;
}

status_t AwesomePlayer::finishSetDataSource_l() {
    sp<DataSource> dataSource;
#ifndef ANDROID_DEFAULT_CODE
    mFinishAgain = false;
#endif // #ifndef ANDROID_DEFAULT_CODE

    bool isWidevineStreaming = false;
    if (!strncasecmp("widevine://", mUri.string(), 11)) {
        isWidevineStreaming = true;

        String8 newURI = String8("http://");
        newURI.append(mUri.string() + 11);

        mUri = newURI;
    }

    AString sniffedMIME;
    //MTK_OP01_PROTECT_START	
#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_CMMB_SUPPORT
    //CMMB added	
       if (!strncasecmp("CMMB", mUri.string(), 4))
	{
	  LOGE("goto cmmb://");	
	  IsCMMBPlayer = true;
	  IsCMMBFirstFrame = true;
 
         mCMMBSource = new CMMBDataSource(cmmb_fd);
	  dataSource = mCMMBSource;

         sp<MediaExtractor> extractor = new CMMBExtractor(dataSource);
     
         if (extractor == NULL) {
             return UNKNOWN_ERROR;
         }

         return setDataSource_l(extractor);
	
    }else
#endif
#endif
    //MTK_OP01_PROTECT_END
    {
    if (!strncasecmp("http://", mUri.string(), 7)
            || !strncasecmp("https://", mUri.string(), 8)
            || isWidevineStreaming) {
        mConnectingDataSource = HTTPBase::Create(
                (mFlags & INCOGNITO)
                    ? HTTPBase::kFlagIncognito
                    : 0);

        if (mUIDValid) {
            mConnectingDataSource->setUID(mUID);
        }
#ifndef ANDROID_DEFAULT_CODE 
        String8 cacheSize;
        if (removeSpecificHeaders(String8("MTK-HTTP-CACHE-SIZE"), &mUriHeaders, &cacheSize)) {
            mHighWaterMarkUs = (int64_t)atoi(cacheSize.string()) * 1000000ll;
        } else {
            mHighWaterMarkUs = kHighWaterMarkUs;
        }
        LOGD("cache size = %lld", mHighWaterMarkUs);

        //save MTK-RTSP-CACHE-SIZE value for rtsp sdp mode because http need
        //remove useless headers
        if(removeSpecificHeaders(String8("MTK-RTSP-CACHE-SIZE"), &mUriHeaders, &cacheSize)){
			mHighWaterMarkRTSPUs = (int64_t)atoi(cacheSize.string()) * 1000000ll;
        }else{
        	mHighWaterMarkRTSPUs = kHighWaterMarkRTSPUs;
		}
		LOGI("RTSP cache size = %lld", mHighWaterMarkRTSPUs);
#endif
        String8 cacheConfig;
        bool disconnectAtHighwatermark;
        NuCachedSource2::RemoveCacheSpecificHeaders(
                &mUriHeaders, &cacheConfig, &disconnectAtHighwatermark);

        mLock.unlock();
        status_t err = mConnectingDataSource->connect(mUri, &mUriHeaders);
        mLock.lock();

        if (err != OK) {
#ifndef ANDROID_DEFAULT_CODE
            Mutex::Autolock autoLock(mMiscStateLock);
            if (mConnectingDataSource != NULL) {
                mConnectingDataSource.clear();
            }
            LOGI("mConnectingDataSource->connect() returned %d", err);
			err = ERROR_CANNOT_CONNECT;//notify this when connect fail whatever DataSource returned
#else
            LOGI("mConnectingDataSource->connect() returned %d", err);
            mConnectingDataSource.clear();
#endif

            return err;
        }

        if (!isWidevineStreaming) {
            // The widevine extractor does its own caching.

#if 0
            mCachedSource = new NuCachedSource2(
                    new ThrottledSource(
                        mConnectingDataSource, 50 * 1024 /* bytes/sec */));
#else
            mCachedSource = new NuCachedSource2(
                    mConnectingDataSource,
                    cacheConfig.isEmpty() ? NULL : cacheConfig.string(),
                    disconnectAtHighwatermark);
#ifndef ANDROID_DEFAULT_CODE
        	mPrerollEnable = false;
#endif
#endif

            dataSource = mCachedSource;
        } else {
            dataSource = mConnectingDataSource;
        }
#ifndef ANDROID_DEFAULT_CODE
#else
        mConnectingDataSource.clear();
#endif


        String8 contentType = dataSource->getMIMEType();

        if (strncasecmp(contentType.string(), "audio/", 6)) {
#ifndef ANDROID_DEFAULT_CODE
            //We're doing something for audio-only stream to avoid ANR
#else
            // We're not doing this for streams that appear to be audio-only
            // streams to ensure that even low bandwidth streams start
            // playing back fairly instantly.
#endif

            // We're going to prefill the cache before trying to instantiate
            // the extractor below, as the latter is an operation that otherwise
            // could block on the datasource for a significant amount of time.
            // During that time we'd be unable to abort the preparation phase
            // without this prefill.
            if (mCachedSource != NULL) {
                // We're going to prefill the cache before trying to instantiate
                // the extractor below, as the latter is an operation that otherwise
                // could block on the datasource for a significant amount of time.
                // During that time we'd be unable to abort the preparation phase
                // without this prefill.

                mLock.unlock();

                // Initially make sure we have at least 192 KB for the sniff
                // to complete without blocking.
                static const size_t kMinBytesForSniffing = 192 * 1024;

                off64_t metaDataSize = -1ll;
                for (;;) {
                    status_t finalStatus;
                    size_t cachedDataRemaining =
                        mCachedSource->approxDataRemaining(&finalStatus);

                    if (finalStatus != OK
                            || (metaDataSize >= 0
                                && cachedDataRemaining >= metaDataSize)
                            || (mFlags & PREPARE_CANCELLED)) {
                        break;
                    }

                    LOGV("now cached %d bytes of data", cachedDataRemaining);

                    if (metaDataSize < 0
                            && cachedDataRemaining >= kMinBytesForSniffing) {
                        String8 tmp;
                        float confidence;
                        sp<AMessage> meta;
						LOGD("content type=%s", contentType.string());
//#ifndef ANDROID_DEFAULT_CODE
//						if (strcmp(contentType.string(), MEDIA_MIMETYPE_CONTAINER_WVM) 
//							&& strncasecmp(contentType.string(), "drm", 3)
//							&& (!isWidevineStreaming)) {
//							
//							LOGD("Must not drm");
//							meta = new AMessage;
//							meta->setInt32("must-not-drm", 1);
//						}
//#endif
						LOGD("SNIFF+");
                        if (!dataSource->sniff(&tmp, &confidence, &meta)) {
                            mLock.lock();
                            return UNKNOWN_ERROR;
                        }
						LOGD("SNIFF-");

                        // We successfully identified the file's extractor to
                        // be, remember this mime type so we don't have to
                        // sniff it again when we call MediaExtractor::Create()
                        // below.
                        sniffedMIME = tmp.string();

                        if (meta == NULL
                                || !meta->findInt64(
                                    "meta-data-size", &metaDataSize)) {
                            metaDataSize = kHighWaterMarkBytes;
                        }

                        CHECK_GE(metaDataSize, 0ll);
                        LOGV("metaDataSize = %lld bytes", metaDataSize);
                    }

                    usleep(200000);
                }

                mLock.lock();
            }

            if (mFlags & PREPARE_CANCELLED) {
                LOGI("Prepare cancelled while waiting for initial cache fill.");
                return UNKNOWN_ERROR;
            }
       }
#ifndef ANDROID_DEFAULT_CODE
        else {
           // sniffedMIME = contentType.string();
            //use the mime type from contentType reported by DataSource

            if (mCachedSource != NULL) {
                mLock.unlock();
                for (;;) {
                    status_t finalStatus;
                    size_t cachedDataRemaining = mCachedSource->approxDataRemaining(&finalStatus);

                    //if (finalStatus != OK || (cachedDataRemaining >= mHighWaterMarkUs)
					if (finalStatus != OK || (cachedDataRemaining >= 192 * 1024)//High water mark may be changed according to header. 192k is enough to sniff audio-only media content..
                            || (mFlags & PREPARE_CANCELLED)) {
                        break;
                    }
                    LOGV("cached %d bytes for %s", cachedDataRemaining, contentType.string());
                    usleep(200000);
                }

                mLock.lock();
            }

            if (mFlags & PREPARE_CANCELLED) {
                LOGI("Prepare cancelled while waiting for initial cache fill.");
                return UNKNOWN_ERROR;
            }
        }
#endif
    } else if (!strncasecmp("rtsp://", mUri.string(), 7)) {
        if (mLooper == NULL) {
            mLooper = new ALooper;
            mLooper->setName("rtsp");
            mLooper->start();
        }
#ifndef ANDROID_DEFAULT_CODE
        {
            Mutex::Autolock autoLock(mMiscStateLock);
            mRTSPController = new ARTSPController(mLooper);
#if defined(MT6575) || defined(MT6577)
            mFirstSubmit = false;
#endif
        }
#else
        mRTSPController = new ARTSPController(mLooper);
#endif // #ifndef ANDROID_DEFAULT_CODE
        mConnectingRTSPController = mRTSPController;

        if (mUIDValid) {
            mConnectingRTSPController->setUID(mUID);
        }

        mLock.unlock();
#ifndef ANDROID_DEFAULT_CODE
        status_t err = mRTSPController->connect(mUri.string(), &mUriHeaders, mSessionDesc);
#else
        status_t err = mRTSPController->connect(mUri.string());
#endif // #ifndef ANDROID_DEFAULT_CODE
        mLock.lock();

        mConnectingRTSPController.clear();

        LOGI("ARTSPController::connect returned %d", err);

        if (err != OK) {
#ifndef ANDROID_DEFAULT_CODE
            Mutex::Autolock autoLock(mMiscStateLock);
#endif // #ifndef ANDROID_DEFAULT_CODE
            mRTSPController.clear();
            return err;
        }

#ifndef ANDROID_DEFAULT_CODE
        String8 cacheSize;
        if (removeSpecificHeaders(String8("MTK-RTSP-CACHE-SIZE"), &mUriHeaders, &cacheSize)) {
            mHighWaterMarkUs = atoi(cacheSize.string()) * 1000000ll;
        } else {
        	 //for sdp mode--MTK-RTSP-CACHE-SIZE header has been cleared by previous finishsetDataSource
            //we should use the the saved MTK-RTSP-CACHE-SIZE on the previous finishsetdatasource
            //and mHighWaterMarkRTSPUs is with default value of kHighWaterMarkRTSPUs for normal case

            //mHighWaterMarkUs = kHighWaterMarkRTSPUs;
			mHighWaterMarkUs = mHighWaterMarkRTSPUs;
        }
		LOGI("RTSP cache size = %lld", mHighWaterMarkUs);
        //remove useless headers
        removeSpecificHeaders(String8("MTK-HTTP-CACHE-SIZE"), &mUriHeaders, &cacheSize);
#endif

        sp<MediaExtractor> extractor = mRTSPController.get();
        return setDataSource_l(extractor);
    } else {
#ifndef ANDROID_DEFAULT_CODE
        if ((!strncasecmp("/system/media/audio/", mUri.string(), 20)) && (strcasestr(mUri.string(),".ogg") != NULL))
           sniffedMIME = MEDIA_MIMETYPE_CONTAINER_OGG;
#endif
        dataSource = DataSource::CreateFromURI(mUri.string(), &mUriHeaders);
    }

    if (dataSource == NULL) {
        return UNKNOWN_ERROR;
    }

    sp<MediaExtractor> extractor;

    if (isWidevineStreaming) {
        String8 mimeType;
        float confidence;
        sp<AMessage> dummy;
        bool success = SniffDRM(dataSource, &mimeType, &confidence, &dummy);

        if (!success
                || strcasecmp(
                    mimeType.string(), MEDIA_MIMETYPE_CONTAINER_WVM)) {
            return ERROR_UNSUPPORTED;
        }

        mWVMExtractor = new WVMExtractor(dataSource);
        mWVMExtractor->setAdaptiveStreamingMode(true);
        extractor = mWVMExtractor;
    } else {
        extractor = MediaExtractor::Create(
                dataSource, sniffedMIME.empty() ? NULL : sniffedMIME.c_str());

        if (extractor == NULL) {
            return UNKNOWN_ERROR;
        }
    }

#ifndef ANDROID_DEFAULT_CODE
    if ((extractor->flags() & MediaExtractor::MAY_PARSE_TOO_LONG)) {
        Mutex::Autolock autoLock(mMiscStateLock);
        if (mStopped) {
            LOGI("user has already stopped");
            extractor->stopParsing();
        } else {
            LOGI("this extractor may take long time to parse, record for stopping");
            mExtractor = extractor;
        }
    }
#endif // #ifndef ANDROID_DEFAULT_CODE

    dataSource->getDrmInfo(mDecryptHandle, &mDrmManagerClient);

    if (mDecryptHandle != NULL) {
        CHECK(mDrmManagerClient);
        if (RightsStatus::RIGHTS_VALID != mDecryptHandle->status) {
            notifyListener_l(MEDIA_ERROR, MEDIA_ERROR_UNKNOWN, ERROR_DRM_NO_LICENSE);
        }
    }

    status_t err = setDataSource_l(extractor);

    if (err != OK) {
        mWVMExtractor.clear();

        return err;
    }

    return OK;
  }
}

void AwesomePlayer::abortPrepare(status_t err) {
    CHECK(err != OK);

    if (mIsAsyncPrepare) {
        notifyListener_l(MEDIA_ERROR, MEDIA_ERROR_UNKNOWN, err);
    }

    mPrepareResult = err;
    modifyFlags((PREPARING|PREPARE_CANCELLED|PREPARING_CONNECTED), CLEAR);
    mAsyncPrepareEvent = NULL;
    mPreparedCondition.broadcast();
}

// static
bool AwesomePlayer::ContinuePreparation(void *cookie) {
    AwesomePlayer *me = static_cast<AwesomePlayer *>(cookie);

    return (me->mFlags & PREPARE_CANCELLED) == 0;
}

void AwesomePlayer::onPrepareAsyncEvent() {
    Mutex::Autolock autoLock(mLock);

    if (mFlags & PREPARE_CANCELLED) {
        LOGI("prepare was cancelled before doing anything");
        abortPrepare(UNKNOWN_ERROR);
        return;
    }

    if (mUri.size() > 0) {
        status_t err = finishSetDataSource_l();

#ifndef ANDROID_DEFAULT_CODE
        if (mFinishAgain){
        	LOGI("RTSP is SDP over http mode");
            err = finishSetDataSource_l();
		}
#endif // #ifndef ANDROID_DEFAULT_CODE
        if (err != OK) {
            abortPrepare(err);
            return;
        }
    }
#ifndef ANDROID_DEFAULT_CODE
	if((mExtractorFlags & MediaExtractor::MAY_PARSE_TOO_LONG))
	{
		Mutex::Autolock autoLock(mMiscStateLock);
		int err;
		if (mExtractor != NULL) {
			LOGE("parsing index of avi file!");
			if(mStopped){
				err=mExtractor->stopParsing();
			}else{
				err=mExtractor->finishParsing();
			}
			mExtractor.clear();
			if (err != OK) {          
				abortPrepare(err);
	            		return;
	        	}			
		}
	}
#endif

    if (mVideoTrack != NULL && mVideoSource == NULL) {
        status_t err = initVideoDecoder();

#ifndef ANDROID_DEFAULT_CODE
        if (err == ERROR_UNSUPPORTED_VIDEO) {
            LOGW("unsupportted video detected, has audio = %d %d", mAudioTrack != NULL, mAudioSource != NULL);
            if (mAudioTrack != NULL || mAudioSource != NULL) {
                notifyListener_l(MEDIA_INFO, MEDIA_INFO_HAS_UNSUPPORT_VIDEO);
                err = OK;
	          mFinalStopFlag |=FINAL_HAS_UNSUPPORT_VIDEO;
            } else {
                err = MEDIA_ERROR_TYPE_NOT_SUPPORTED;
                notifyListener_l(MEDIA_ERROR, err);
            }
            const char *mime;			
            CHECK(mMetaData->findCString(kKeyMIMEType, &mime));
            if(!strcasecmp(MEDIA_MIMETYPE_CONTAINER_MPEG2TS, mime) || !strcasecmp(MEDIA_MIMETYPE_VIDEO_WMV, mime))
            {

                mVideoTrack->stop();
                LOGE("onPrepareAsyncEvent stop video track");
            }
        }
#endif
        if (err != OK) {
            abortPrepare(err);
            return;
        }
    }

    if (mAudioTrack != NULL && mAudioSource == NULL) {
        status_t err = initAudioDecoder();

        if (err != OK) {
            abortPrepare(err);
            return;
        }
    }

    modifyFlags(PREPARING_CONNECTED, SET);

#ifndef ANDROID_DEFAULT_CODE
    // split RTSP play from ARTSPController::connect
    // to give a chance to decoder to find error and start APacketSource
    if (mRTSPController != NULL) {
        status_t err = OK;//mRTSPController->sendPlay();
        if (err != OK) {
            if (err == FAILED_TRANSACTION)
                abortPrepare(UNKNOWN_ERROR);
            else
                abortPrepare(ERROR_CANNOT_CONNECT);
            return;
        } else {
            finishAsyncPrepare_l();
        }
    }
#endif

    if (isStreamingHTTP() || mRTSPController != NULL) {
        postBufferingEvent_l();
    } else {
        finishAsyncPrepare_l();
    }

#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_DRM_APP
    if (mDecryptHandle != NULL) {
        LOGD("AwesomePlayer, consumeRights @onPrepareAsyncEvent()");
        // in some cases, the mFileSource may be NULL (E.g. play audio directly in File Manager)
        // We don't know, but we assume it's a OMA DRM v1 case (DecryptApiType::CONTAINER_BASED)
        if ((mFileSource.get() != NULL && (mFileSource->flags() & OMADrmFlag) != 0)
            || (DecryptApiType::CONTAINER_BASED == mDecryptHandle->decryptApiType)) {
            if (!DrmMtkUtil::isTrustedVideoClient(mDrmValue)) {
                mDrmManagerClient->consumeRights(mDecryptHandle, Action::PLAY, false);
            }
        }
    }
#endif
#endif

#ifndef ANDROID_DEFAULT_CODE 
#if defined(MT6575) || defined(MT6577)
    struct sched_param sched_p;
    // Change the scheduling policy to SCHED_RR
    sched_getparam(0, &sched_p);
    sched_p.sched_priority = RTPM_PRIO_VIDEO_PLAYBACK_THREAD;

    if (0 != sched_setscheduler(0, SCHED_RR, &sched_p)) {
        LOGE("@@[SF_PROPERTY]sched_setscheduler fail...");
    }
    else {
        sched_p.sched_priority = 0;
        sched_getparam(0, &sched_p);
        LOGD("@@[SF_PROPERTY]sched_setscheduler ok..., priority:%d", sched_p.sched_priority);
    } 
#endif
#endif


}

void AwesomePlayer::finishAsyncPrepare_l() {
    if (mIsAsyncPrepare) {
        if (mVideoSource == NULL) {
            notifyListener_l(MEDIA_SET_VIDEO_SIZE, 0, 0);
        } else {
            notifyVideoSize_l();
        }

        notifyListener_l(MEDIA_PREPARED);
#ifndef ANDROID_DEFAULT_CODE
        // notify check live to AP to start playing
        if (mRTSPController != NULL) {
            notifyListener_l(MEDIA_INFO, MEDIA_INFO_CHECK_LIVE_STREAMING_COMPLETE);
        }
        if (mCachedSource != NULL) {
            notifyListener_l(MEDIA_INFO, MEDIA_INFO_BUFFERING_DATA);
        }
#endif // #ifndef ANDROID_DEFAULT_CODE
    }

    mPrepareResult = OK;
    modifyFlags((PREPARING|PREPARE_CANCELLED|PREPARING_CONNECTED), CLEAR);
    modifyFlags(PREPARED, SET);
    mAsyncPrepareEvent = NULL;
    mPreparedCondition.broadcast();
}

uint32_t AwesomePlayer::flags() const {
    return mExtractorFlags;
}

void AwesomePlayer::postAudioEOS(int64_t delayUs) {
    postCheckAudioStatusEvent(delayUs);
}

void AwesomePlayer::postAudioSeekComplete() {
    postCheckAudioStatusEvent(0);
}

status_t AwesomePlayer::setParameter(int key, const Parcel &request) {
    switch (key) {
        case KEY_PARAMETER_TIMED_TEXT_TRACK_INDEX:
        {
            Mutex::Autolock autoLock(mTimedTextLock);
            return setTimedTextTrackIndex(request.readInt32());
        }
        case KEY_PARAMETER_TIMED_TEXT_ADD_OUT_OF_BAND_SOURCE:
        {
            Mutex::Autolock autoLock(mTimedTextLock);
            if (mTextPlayer == NULL) {
                mTextPlayer = new TimedTextPlayer(this, mListener, &mQueue);
            }

            return mTextPlayer->setParameter(key, request);
        }
        case KEY_PARAMETER_CACHE_STAT_COLLECT_FREQ_MS:
        {
            return setCacheStatCollectFreq(request);
        }
#ifndef ANDROID_DEFAULT_CODE
		case KEY_PARAMETER_AUDIO_SEEKTABLE:
		{
			request.readInt32(&mEnAudST);
			LOGV("setParameter mEnAudST %d",mEnAudST);
		}			
#ifdef MTK_S3D_SUPPORT		
        case KEY_PARAMETER_3D_INFO:
	{
		request.readInt32((int32_t*)&mVideoStereoMode);
		LOGD("setParameter mVideoStereoMode %d",mVideoStereoMode);		
	}
	case KEY_PARAMETER_3D_OFFSET:
	{
		int s3dOffset = 0;
		request.readInt32((int32_t*)&s3dOffset);
              LOGD("setParameter mVideoStereoMode %d,s3dOffset:%d, mNativeWindow:0X%x",mVideoStereoMode,s3dOffset, mNativeWindow.get());

		if (mNativeWindow != NULL){
                    native_window_set_s3d_offset(mNativeWindow.get(), s3dOffset);
		}

	}
#endif
#endif        
        default:
        {
            return ERROR_UNSUPPORTED;
        }
    }
}

status_t AwesomePlayer::setCacheStatCollectFreq(const Parcel &request) {
    if (mCachedSource != NULL) {
        int32_t freqMs = request.readInt32();
        LOGD("Request to keep cache stats in the past %d ms",
            freqMs);
        return mCachedSource->setCacheStatCollectFreq(freqMs);
    }
    return ERROR_UNSUPPORTED;
}

status_t AwesomePlayer::getParameter(int key, Parcel *reply) {
    switch (key) {
    case KEY_PARAMETER_AUDIO_CHANNEL_COUNT:
        {
            int32_t channelCount;
            if (mAudioTrack == 0 ||
                    !mAudioTrack->getFormat()->findInt32(kKeyChannelCount, &channelCount)) {
                channelCount = 0;
            }
            reply->writeInt32(channelCount);
        }
        return OK;
    default:
        {
            return ERROR_UNSUPPORTED;
        }
    }
}

bool AwesomePlayer::isStreamingHTTP() const {
    return mCachedSource != NULL || mWVMExtractor != NULL;
}

status_t AwesomePlayer::dump(int fd, const Vector<String16> &args) const {
    Mutex::Autolock autoLock(mStatsLock);

    FILE *out = fdopen(dup(fd), "w");

    fprintf(out, " AwesomePlayer\n");
    if (mStats.mFd < 0) {
        fprintf(out, "  URI suppressed");
    } else {
        fprintf(out, "  fd(%d)", mStats.mFd);
    }

    fprintf(out, ", flags(0x%08x)", mStats.mFlags);

    if (mStats.mBitrate >= 0) {
        fprintf(out, ", bitrate(%lld bps)", mStats.mBitrate);
    }

    fprintf(out, "\n");

    for (size_t i = 0; i < mStats.mTracks.size(); ++i) {
        const TrackStat &stat = mStats.mTracks.itemAt(i);

        fprintf(out, "  Track %d\n", i + 1);
        fprintf(out, "   MIME(%s)", stat.mMIME.string());

        if (!stat.mDecoderName.isEmpty()) {
            fprintf(out, ", decoder(%s)", stat.mDecoderName.string());
        }

        fprintf(out, "\n");

        if ((ssize_t)i == mStats.mVideoTrackIndex) {
            fprintf(out,
                    "   videoDimensions(%d x %d), "
                    "numVideoFramesDecoded(%lld), "
                    "numVideoFramesDropped(%lld)\n",
                    mStats.mVideoWidth,
                    mStats.mVideoHeight,
                    mStats.mNumVideoFramesDecoded,
                    mStats.mNumVideoFramesDropped);
        }
    }

    fclose(out);
    out = NULL;

    return OK;
}

void AwesomePlayer::modifyFlags(unsigned value, FlagMode mode) {
    switch (mode) {
        case SET:
            mFlags |= value;
            break;
        case CLEAR:
            mFlags &= ~value;
            break;
        case ASSIGN:
            mFlags = value;
            break;
        default:
            TRESPASS();
    }

    {
        Mutex::Autolock autoLock(mStatsLock);
        mStats.mFlags = mFlags;
    }
}
#ifndef ANDROID_DEFAULT_CODE
bool AwesomePlayer::isNotifyDuration()
{
	if(mEnAudST==1)
	  return true;
	else
		return false;	
}
void AwesomePlayer::postDurationUpdateEvent(int64_t duration)
{
		postDurationUpdateEvent_l(duration);
}
void AwesomePlayer::postDurationUpdateEvent_l(int64_t duration)
{
	if(mDurationUpdateEventPending)
		return ;
	mDurationUpdateEventPending=true;
        mDurationUs = duration;
	mQueue.postEvent(mDurationUpdateEvent);
}

void AwesomePlayer::OnDurationUpdate(){
	Mutex::Autolock autoLock(mLock);
	//for MtkAACExtractor
	if(mAudioTrack != NULL)
	{
		sp<MetaData> meta = mAudioTrack->getFormat();
		const char *mime;
		CHECK(meta->findCString(kKeyMIMEType, &mime));
		if (!strcasecmp(MEDIA_MIMETYPE_AUDIO_AAC, mime)) 
		{
			int32_t nIsAACADIF;
			if (meta->findInt32(kKeyIsAACADIF, &nIsAACADIF))
			{
				if(0 != nIsAACADIF)
				{
					mExtractorFlags |= (MediaExtractor::CAN_SEEK_BACKWARD | MediaExtractor::CAN_SEEK_FORWARD | MediaExtractor::CAN_SEEK);
					LOGW("AwesomePlayer::OnDurationUpdate--ADIF seekable");
				}
			}
		}  
	}
	if(!mDurationUpdateEventPending)
		return ;
	mDurationUpdateEventPending=false;
	notifyListener_l(MEDIA_DURATION_UPDATE,mDurationUs/1000,0);
}

sp<MetaData> AwesomePlayer::getMetaData() const {
    return mMetaData;
}

status_t AwesomePlayer::getVideoDimensions(
        int32_t *width, int32_t *height) const {
    Mutex::Autolock autoLock(mStatsLock);

    if (mStats.mVideoWidth < 0 || mStats.mVideoHeight < 0) {
        return UNKNOWN_ERROR;
    }

    *width = mStats.mVideoWidth;
    *height = mStats.mVideoHeight;

    return OK;
}
//MTK_OP01_PROTECT_START
/////////////////////////////////////////////////////////////////////////////////////////////
//CMMB added.

// static
void *AwesomePlayer::ThreadWrapper(void *me) {

    //detach
    pthread_detach(pthread_self());
	
    setpriority(PRIO_PROCESS, 0, ANDROID_PRIORITY_BACKGROUND);
    static_cast<AwesomePlayer *>(me)->CaptureThreadEntry();

    return NULL;
}

void AwesomePlayer::CaptureThreadEntry()
{
    bool ret;
    
    //while(false == IsCMMBCaptureStopFlag){
       Mutex::Autolock autoLock(mCaptureLock);
#if 1 //turn to high priority
    struct sched_param sched_p;
    // Change the scheduling policy to SCHED_RR
    sched_getparam(0, &sched_p);
    LOGE("CaptureThreadEntry original priority = %d", sched_p.sched_priority);
	
    sched_p.sched_priority = RTPM_PRIO_VIDEO_PLAYBACK_THREAD;

    if (0 != sched_setscheduler(0, SCHED_RR, &sched_p)) {
        LOGE("@@[SF_PROPERTY]sched_setscheduler fail...");
    }
#endif

	IsCMMBCaptureOneFrame = true;
	LOGE("AwesomePlayer::CaptureThreadEntry go into");
	status_t err = mCMMBCaptureCondition.waitRelative(mCaptureLock, 100*1000*100011);//100ms
	LOGE("AwesomePlayer::CaptureThreadEntry wait mCMMBCaptureCondition end");

	if (NULL != captureBuffer)
	{
	       int32_t videowith, videoheight, CropLeft, CropRight, CropTop, CropBottom;
		int32_t returnvalue = 0;
		const uint8_t * rawbuffer;
	       sp<MetaData> meta = mVideoSource->getFormat();

		CHECK(meta->findInt32(kKeyWidth, &videowith));
              CHECK(meta->findInt32(kKeyHeight, &videoheight));
              CropLeft = CropTop = 0;
              CropRight = videowith - 1;
              CropBottom = videoheight - 1;
		
       	//YUV convert
       	SkBitmap bitmap;
		LOGE("capture go into colorconvert, width = %d, height = %d", videowith, videoheight);
              
		bitmap.setConfig(SkBitmap::kRGB_565_Config, videowith, videoheight);
              bitmap.allocPixels(); 
	       void* pRGB = bitmap.getPixels();
		   
              ColorConverter converter(
                     (OMX_COLOR_FORMATTYPE)OMX_COLOR_FormatYUV420Planar, OMX_COLOR_Format16bitRGB565);
              CHECK(converter.isValid());

	       //rawbuffer = (const uint8_t *)(captureBuffer->graphicBuffer());
              status_t err = (captureBuffer->graphicBuffer())->lock(GRALLOC_USAGE_SW_WRITE_OFTEN, (void**)(&rawbuffer));
              if (err != 0) {
                  LOGE("capture nBuf->lock(...) failed: %d", err);
                  captureBuffer->release();
                  captureBuffer = NULL; 
                  notifyListener_l(MEDIA_ERROR,  SKIA_ERROR) ;
		    mCMMBCaptureFinished = true;
                  return;
              }


              LOGE("convert before rawbuff = %x,stride = %d, width = %d, height = %d", rawbuffer, (captureBuffer->graphicBuffer())->stride, 
			  	                                                                             (captureBuffer->graphicBuffer())->width,
			  	                                                                             (captureBuffer->graphicBuffer())->height);

       	(captureBuffer->graphicBuffer())->unlock();
       
       	captureBuffer->release();
       	captureBuffer = NULL;
		
#if 0     //turn to original
              setpriority(PRIO_PROCESS, 0, ANDROID_PRIORITY_BACKGROUND);
#endif
#if 1  //turn to original
    struct sched_param sched_p;
    // Change the scheduling policy to SCHED_RR
    sched_getparam(0, &sched_p);
    LOGE("CaptureThreadEntry original priority = %d", sched_p.sched_priority);
	
    sched_p.sched_priority = 0;

    if (0 != sched_setscheduler(0, SCHED_OTHER, &sched_p)) {
        LOGE("CaptureThreadEntry sched_setscheduler fail...");
    }
#endif


               //save yuv file.
		/*{
		    char name[260];
		    FILE*	file_;
		    sprintf(name, "/data/cmmb_yuv.yuv");
	            file_ = fopen(name, "wb");

		    fwrite(rawbuffer, 1, 
				((captureBuffer->graphicBuffer())->stride * (captureBuffer->graphicBuffer())->height * 3 / 2),
			       file_);

		    fclose(file_);

		}*/
              returnvalue = converter.convert(
               rawbuffer,
               videowith, videoheight,
               CropLeft, CropTop,
               CropRight, CropBottom,
               (uint8_t *)pRGB, 
               videowith, videoheight,
               CropLeft, CropTop,
               CropRight, CropBottom);
               LOGE("convert after");



	      if(returnvalue != 0)
	      {
                  LOGE("capture convert fail, errorcode = %d,", returnvalue);
		    return;
	      }		
			   
       	ret = SkImageEncoder::EncodeFile(m_cmmbUri, bitmap, SkImageEncoder::kJPEG_Type, 100);
		if (!ret)
		{
		    LOGE("AwesomePlayer::CaptureThreadEntry skia error ret = %x", ret);
                  notifyListener_l(MEDIA_ERROR,  SKIA_ERROR) ;
		    mCMMBCaptureFinished = true;
		    return;
		}

		LOGE("AwesomePlayer::CaptureThreadEntry capture finished");

		//don't need bitmap release,etc.

		
	}
        else
        {
            IsCMMBCaptureOneFrame = false;
#if 0     //turn to original
              setpriority(PRIO_PROCESS, 0, ANDROID_PRIORITY_BACKGROUND);
#endif
#if 1  //turn to original
    struct sched_param sched_p;
    // Change the scheduling policy to SCHED_RR
    sched_getparam(0, &sched_p);
    LOGE("CaptureThreadEntry original priority = %d", sched_p.sched_priority);
	
    sched_p.sched_priority = 0;

    if (0 != sched_setscheduler(0, SCHED_OTHER, &sched_p)) {
        LOGE("CaptureThreadEntry sched_setscheduler fail...");
    }
#endif


            LOGE("AwesomePlayer::CaptureThreadEntry capturebuffer is null");
            notifyListener_l(MEDIA_ERROR,  SKIA_ERROR) ;
            mCMMBCaptureFinished = true;
            return;
        }

	
	mCMMBCaptureFinished = true;
	notifyListener_l(MEDIA_INFO, MEDIA_INFO_CMMB_CAPTURE_OK) ;
	LOGE("AwesomePlayer::CaptureThreadEntry exit");
    //}
}

status_t AwesomePlayer::capture(const char* uri)
{
    pthread_t thr;


    LOGE("AwesomePlayer::Capture");
	
    if (false == mCMMBCaptureFinished)
    {
        return  CAPTURE_BUSY_ERROR;
        //return -1;
    }
    m_cmmbUri = uri;

    //tag  state to be running.	
    mCMMBCaptureFinished = false;
	

    //create thread
   int ret;
   ret = pthread_create( &thr, NULL, ThreadWrapper, (void*)this);
   if ( ret != 0 )
   {
        LOGE("pthread_create error");
        return CAPTURE_BUSY_ERROR;

   }
    //to trigger onvideoevent to give one video buffer to encode jpeg.
    //IsCMMBCaptureOneFrame = true;
    
    return OK;
}
//MTK_OP01_PROTECT_END
#endif

#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_DRM_APP
status_t AwesomePlayer::addValue(const String8 &value)
{
    mDrmValue = value;
    return OK;
}
#endif
#endif

#ifndef ANDROID_DEFAULT_CODE
status_t AwesomePlayer::tryReadIfNeccessary_l() {
    if ((mCachedSource == NULL) || (mVideoSource == NULL)) {
        return OK;
    }
    sp<MetaData> meta = mVideoTrack->getFormat();
    int32_t nSupported = 0;
    status_t tryReadResult = OK;
    if (meta->findInt32(kKeySupportTryRead, &nSupported) && (nSupported == 1)) {
    //if (1) {
        MediaSource::ReadOptions opt;
        opt.setSeekTo(mSeekTimeUs, MediaSource::ReadOptions::SEEK_TRY_READ);
        MediaBuffer *pBuffer;
        tryReadResult = mVideoTrack->read(&pBuffer, &opt);

    }
    LOGD("the video track try read nSupported = %d, mFlags = 0x%x", nSupported, mFlags);
    return tryReadResult;
 
}

void AwesomePlayer::disconnectSafeIfNeccesary() {
    Mutex::Autolock autoLock(mMiscStateLock);
    if (mConnectingDataSource != NULL) {
        LOGD("reset: disconnect mConnectingDataSource");
		if (mCachedSource != NULL) {
			mCachedSource->finishCache();
		}
        mConnectingDataSource->disconnect();
    }
}


bool AwesomePlayer::removeSpecificHeaders(const String8 MyKey, KeyedVector<String8, String8> *headers, String8 *pMyHeader) {
    LOGD("removeSpecificHeaders %s", MyKey.string());
    *pMyHeader = "";
    if (headers != NULL) {
        ssize_t index;
        if ((index = headers->indexOfKey(MyKey)) >= 0) {
            *pMyHeader = headers->valueAt(index);
            headers->removeItemsAt(index);
            LOGD("special headers: %s = %s", MyKey.string(), pMyHeader->string());
            return true;
        }
    }
    return false;
}

#endif

}  // namespace android
