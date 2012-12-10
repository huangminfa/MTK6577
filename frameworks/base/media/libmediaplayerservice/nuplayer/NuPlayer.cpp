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

//#define LOG_NDEBUG 0
#define LOG_TAG "NuPlayer"
#include <utils/Log.h>

#include "NuPlayer.h"

#include "HTTPLiveSource.h"
#include "NuPlayerDecoder.h"
#include "NuPlayerDriver.h"
#include "NuPlayerRenderer.h"
#include "NuPlayerSource.h"
#include "RTSPSource.h"
#include "StreamingSource.h"

#include "ATSParser.h"

#include <media/stagefright/foundation/hexdump.h>
#include <media/stagefright/foundation/ABuffer.h>
#include <media/stagefright/foundation/ADebug.h>
#include <media/stagefright/foundation/AMessage.h>
#include <media/stagefright/ACodec.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MediaErrors.h>
#include <media/stagefright/MetaData.h>
#include <surfaceflinger/Surface.h>
#include <gui/ISurfaceTexture.h>

#include "avc_utils.h"

namespace android {

////////////////////////////////////////////////////////////////////////////////

NuPlayer::NuPlayer()
    : mUIDValid(false),
      mVideoIsAVC(false),
      mAudioEOS(false),
      mVideoEOS(false),
      mScanSourcesPending(false),
      mScanSourcesGeneration(0),
      mTimeDiscontinuityPending(false),
      mFlushingAudio(NONE),
      mFlushingVideo(NONE),
      mResetInProgress(false),
      mResetPostponed(false),
      mSkipRenderingAudioUntilMediaTimeUs(-1ll),
      mSkipRenderingVideoUntilMediaTimeUs(-1ll),
      mVideoLateByUs(0ll),
      mNumFramesTotal(0ll),
#ifndef ANDROID_DEFAULT_CODE
      mSeekTimeUs(-1),
      mPrepare(UNPREPARED),
      mPlayState(STOPPED),
      mVideoFirstRenderTimestamp(-1),
      mVideoWidth(320),
      mVideoHeight(240),
      mRenderer(NULL),
      mPrepareNotified(false),
      mDataSourceType(SOURCE_Default),
#endif
      mNumFramesDropped(0ll) {
}

NuPlayer::~NuPlayer() {
    LOGD("~NuPlayer");
}

void NuPlayer::setUID(uid_t uid) {
    mUIDValid = true;
    mUID = uid;
}

void NuPlayer::setDriver(const wp<NuPlayerDriver> &driver) {
    mDriver = driver;
}

void NuPlayer::setDataSource(const sp<IStreamSource> &source) {
    sp<AMessage> msg = new AMessage(kWhatSetDataSource, id());

    msg->setObject("source", new StreamingSource(source));
    msg->post();
}

void NuPlayer::setDataSource(
        const char *url, const KeyedVector<String8, String8> *headers) {
    sp<AMessage> msg = new AMessage(kWhatSetDataSource, id());

    if (!strncasecmp(url, "rtsp://", 7)) {
        // disable
#if 0
        msg->setObject(
                "source", new RTSPSource(url, headers, mUIDValid, mUID));
#endif
    } else {
        msg->setObject(
                "source", new HTTPLiveSource(url, headers, mUIDValid, mUID));
#ifndef ANDROID_DEFAULT_CODE
        mDataSourceType = SOURCE_HttpLive;
#endif
 
    }

    msg->post();
}

void NuPlayer::setVideoSurfaceTexture(const sp<ISurfaceTexture> &surfaceTexture) {
    sp<AMessage> msg = new AMessage(kWhatSetVideoNativeWindow, id());
    sp<SurfaceTextureClient> surfaceTextureClient(surfaceTexture != NULL ?
                new SurfaceTextureClient(surfaceTexture) : NULL);
    msg->setObject("native-window", new NativeWindowWrapper(surfaceTextureClient));
    msg->post();
}

void NuPlayer::setAudioSink(const sp<MediaPlayerBase::AudioSink> &sink) {
    sp<AMessage> msg = new AMessage(kWhatSetAudioSink, id());
    msg->setObject("sink", sink);
    msg->post();
}

void NuPlayer::start() {
    (new AMessage(kWhatStart, id()))->post(); 
}

void NuPlayer::pause() {
    (new AMessage(kWhatPause, id()))->post();
}

void NuPlayer::resume() {
    (new AMessage(kWhatResume, id()))->post();
}

void NuPlayer::resetAsync() {
    (new AMessage(kWhatReset, id()))->post();
}

#ifndef ANDROID_DEFAULT_CODE
void NuPlayer::prepareAsync() {
    mPrepareNotified = false;
    (new AMessage(kWhatPrepare, id()))->post();
}
#endif

void NuPlayer::seekToAsync(int64_t seekTimeUs) {
#ifndef ANDROID_DEFAULT_CODE
    CHECK(seekTimeUs != -1);
    Mutex::Autolock autoLock(mLock); 
    mSeekTimeUs = seekTimeUs;   //seek complete later
#endif
#ifndef ANDROID_DEFAULT_CODE
    if (mRenderer != NULL) {
        if (mPlayState == PLAYING) {
            mRenderer->pause();
        }
    }
#endif
    sp<AMessage> msg = new AMessage(kWhatSeek, id());
    msg->setInt64("seekTimeUs", seekTimeUs);
    msg->post();
}

// static
bool NuPlayer::IsFlushingState(FlushStatus state, bool *needShutdown) {
    switch (state) {
        case FLUSHING_DECODER:
            if (needShutdown != NULL) {
                *needShutdown = false;
            }
            return true;

        case FLUSHING_DECODER_SHUTDOWN:
#ifndef ANDROID_DEFAULT_CODE
        case SHUTTING_DOWN_DECODER:
#endif
            if (needShutdown != NULL) {
                *needShutdown = true;
            }
            return true;

        default:
            return false;
    }
}

void NuPlayer::onMessageReceived(const sp<AMessage> &msg) {
    switch (msg->what()) {
        case kWhatSetDataSource:
        {
            LOGD("kWhatSetDataSource");

            CHECK(mSource == NULL);

            sp<RefBase> obj;
            CHECK(msg->findObject("source", &obj));

            mSource = static_cast<Source *>(obj.get());
            break;
        }

        case kWhatSetVideoNativeWindow:
        {
            LOGD("kWhatSetVideoNativeWindow");

            sp<RefBase> obj;
            CHECK(msg->findObject("native-window", &obj));

            mNativeWindow = static_cast<NativeWindowWrapper *>(obj.get());
            break;
        }

        case kWhatSetAudioSink:
        {
            LOGD("kWhatSetAudioSink");

            sp<RefBase> obj;
            CHECK(msg->findObject("sink", &obj));

            mAudioSink = static_cast<MediaPlayerBase::AudioSink *>(obj.get());
            break;
        }
#ifndef ANDROID_DEFAULT_CODE
//#if 0  //debug: disable prepare
        case kWhatPrepare:
        {
            LOGD("kWhatPrepare, source type = %d", (int)mDataSourceType);
            if (mPrepare == PREPARING)
                break;
            mPrepare = PREPARING;
            
            mVideoIsAVC = false;
            mAudioEOS = false;
            mVideoEOS = false;
            mSkipRenderingAudioUntilMediaTimeUs = -1;
            mSkipRenderingVideoUntilMediaTimeUs = -1;
            mVideoLateByUs = 0;
            mNumFramesTotal = 0;
            mNumFramesDropped = 0;

            if (mSource != NULL) {
                mSource->start();
                postScanSources();
            } else {
                LOGW("prepare error: source is not ready");
                CHECK(mDriver != NULL);
                finishPrepare(false);
                mPrepare = PREPARE_CANCELED;
                break;

            }
            if (SOURCE_HttpLive != mDataSourceType) {
                CHECK(mDriver != NULL);
                finishPrepare(true);
                mPrepare = PREPARED;

            }
            break;
        }
        case kWhatStart:
        {
            LOGD("kWhatStart");

            if (mPlayState == PLAYING) {
                break;
            }

            mRenderer = new Renderer(
                    mAudioSink,
                    new AMessage(kWhatRendererNotify, id()));

            looper()->registerHandler(mRenderer);          

            if  (mDataSourceType == SOURCE_HttpLive) {
                if ((mAudioDecoder == NULL) && (mVideoDecoder == NULL)) {
                    notifyListener(MEDIA_ERROR,  MEDIA_ERROR_TYPE_NOT_SUPPORTED, 0); 
                    LOGE("unsupported format");
                    break;
                }
            } 

            // the decoder Idle->Executing
            if (mAudioDecoder != NULL) {
                mAudioDecoder->signalResume(); 
            }
            if (mVideoDecoder != NULL) {
                mVideoDecoder->signalResume();
            }



            mPlayState = PLAYING;

            break;
        }
#else
        case kWhatStart:
        {
            LOGD("kWhatStart");

            mVideoIsAVC = false;
            mAudioEOS = false;
            mVideoEOS = false;
            mSkipRenderingAudioUntilMediaTimeUs = -1;
            mSkipRenderingVideoUntilMediaTimeUs = -1;
            mVideoLateByUs = 0;
            mNumFramesTotal = 0;
            mNumFramesDropped = 0;

            mSource->start();

            mRenderer = new Renderer(
                    mAudioSink,
                    new AMessage(kWhatRendererNotify, id()));

            looper()->registerHandler(mRenderer);

            postScanSources();
            break;
        }
#endif
        case kWhatScanSources:
        {
            int32_t generation;
            CHECK(msg->findInt32("generation", &generation));
            if (generation != mScanSourcesGeneration) {
                // Drop obsolete msg.
                break;
            }

            mScanSourcesPending = false;
#ifndef ANDROID_DEFAULT_CODE
//#if 0 //debug: disable prepare
            bool needScanAgain = onScanSources();
            if (needScanAgain) {     //scanning source is not completed, continue
                msg->post(100000ll);
                mScanSourcesPending = true;
            } else {
                LOGD("scanning sources done ! haveAudio=%d, haveVideo=%d",
                    mAudioDecoder != NULL, mVideoDecoder != NULL);
            }
#else
            LOGV("scanning sources haveAudio=%d, haveVideo=%d",
                 mAudioDecoder != NULL, mVideoDecoder != NULL);

            instantiateDecoder(false, &mVideoDecoder);

            if (mAudioSink != NULL) {
                instantiateDecoder(true, &mAudioDecoder);
            }

            status_t err;
            if ((err = mSource->feedMoreTSData()) != OK) {
                if (mAudioDecoder == NULL && mVideoDecoder == NULL) {
                    // We're not currently decoding anything (no audio or
                    // video tracks found) and we just ran out of input data.

                    if (err == ERROR_END_OF_STREAM) {
                        notifyListener(MEDIA_PLAYBACK_COMPLETE, 0, 0);
                    } else {
                        notifyListener(MEDIA_ERROR, MEDIA_ERROR_UNKNOWN, err);
                    }
                }
                break;
            }

            if (mAudioDecoder == NULL || mVideoDecoder == NULL) {
                msg->post(100000ll);
                mScanSourcesPending = true;
            //    LOGV("scanning sources haveAudio=%d, haveVideo=%d",
            //       mAudioDecoder != NULL, mVideoDecoder != NULL);
            } else {
                LOGV("scanning sources done ! haveAudio=%d, haveVideo=%d",
                    mAudioDecoder != NULL, mVideoDecoder != NULL);
            }
#endif
            break;
        }

        case kWhatVideoNotify:
        case kWhatAudioNotify:
        {
            bool audio = msg->what() == kWhatAudioNotify;

            sp<AMessage> codecRequest;
            CHECK(msg->findMessage("codec-request", &codecRequest));

            int32_t what;
            CHECK(codecRequest->findInt32("what", &what));

            if (what == ACodec::kWhatFillThisBuffer) {
                status_t err = feedDecoderInputData(
                        audio, codecRequest);

                if (err == -EWOULDBLOCK) {
                    if (mSource->feedMoreTSData() == OK) {
                        msg->post(10000ll);
                    }
                }
            } else if (what == ACodec::kWhatEOS) {
                int32_t err;
                CHECK(codecRequest->findInt32("err", &err));

                if (err == ERROR_END_OF_STREAM) {
                    LOGD("got %s decoder EOS", audio ? "audio" : "video");
                } else {
                    LOGD("got %s decoder EOS w/ error %d",
                         audio ? "audio" : "video",
                         err);
                }

                mRenderer->queueEOS(audio, err);
            } else if (what == ACodec::kWhatFlushCompleted) {
                bool needShutdown;

                if (audio) {
                    CHECK(IsFlushingState(mFlushingAudio, &needShutdown));
                    mFlushingAudio = FLUSHED;
                } else {
                    CHECK(IsFlushingState(mFlushingVideo, &needShutdown));
                    mFlushingVideo = FLUSHED;

                    mVideoLateByUs = 0;
                }

                LOGD("decoder %s flush completed", audio ? "audio" : "video");

                if (needShutdown) {
                    LOGD("initiating %s decoder shutdown",
                         audio ? "audio" : "video");

                    (audio ? mAudioDecoder : mVideoDecoder)->initiateShutdown();

                    if (audio) {
                        mFlushingAudio = SHUTTING_DOWN_DECODER;
                    } else {
                        mFlushingVideo = SHUTTING_DOWN_DECODER;
                    }
                }

                finishFlushIfPossible();
           } else if (what == ACodec::kWhatOutputFormatChanged) {
                if (audio) {
                    int32_t numChannels;
                    CHECK(codecRequest->findInt32("channel-count", &numChannels));

                    int32_t sampleRate;
                    CHECK(codecRequest->findInt32("sample-rate", &sampleRate));

                    LOGD("Audio output format changed to %d Hz, %d channels",
                         sampleRate, numChannels);

                    mAudioSink->close();
                    CHECK_EQ(mAudioSink->open(
                                sampleRate,
                                numChannels,
                                AUDIO_FORMAT_PCM_16_BIT,
                              #ifndef ANDROID_DEFAULT_CODE   
                                4 /* bufferCount */
                              #else
				    8 /* bufferCount */
				  #endif
                                ),
                             (status_t)OK);
                    mAudioSink->start();
                    LOGD("@debug: mRenderer %p to signal audio sink changed", mRenderer.get());

                    mRenderer->signalAudioSinkChanged();
                } else {
                    // video

                    int32_t width, height;
                    CHECK(codecRequest->findInt32("width", &width));
                    CHECK(codecRequest->findInt32("height", &height));

                    int32_t cropLeft, cropTop, cropRight, cropBottom;
                    CHECK(codecRequest->findRect(
                                "crop",
                                &cropLeft, &cropTop, &cropRight, &cropBottom));

                    LOGI("Video output format changed to %d x %d "
                         "(crop: %d x %d @ (%d, %d))",
                         width, height,
                         (cropRight - cropLeft + 1),
                         (cropBottom - cropTop + 1),
                         cropLeft, cropTop);

                    notifyListener(
                            MEDIA_SET_VIDEO_SIZE,
                            cropRight - cropLeft + 1,
                            cropBottom - cropTop + 1);
                }
            } else if (what == ACodec::kWhatShutdownCompleted) {
                LOGD("%s shutdown completed", audio ? "audio" : "video");
                if (audio) {
                    looper()->unregisterHandler(mAudioDecoder->id());
                    mAudioDecoder.clear();

                    CHECK_EQ((int)mFlushingAudio, (int)SHUTTING_DOWN_DECODER);
                    mFlushingAudio = SHUT_DOWN;
                } else {
                    looper()->unregisterHandler(mVideoDecoder->id());
                    mVideoDecoder.clear();

                    CHECK_EQ((int)mFlushingVideo, (int)SHUTTING_DOWN_DECODER);
                    mFlushingVideo = SHUT_DOWN;
               }

                finishFlushIfPossible();
            } else if (what == ACodec::kWhatError) {
#ifndef ANDROID_DEFAULT_CODE
                if (!(IsFlushingState(audio ? mFlushingAudio : mFlushingVideo))) {
                    LOGE("Received error from %s decoder, aborting playback.",
                        audio ? "audio" : "video");
                    mRenderer->queueEOS(audio, UNKNOWN_ERROR);
                } else {
                    LOGD("Ignore error from %s decoder when flushing");
                }
#else
                LOGE("Received error from %s decoder, aborting playback.",
                     audio ? "audio" : "video");
                mRenderer->queueEOS(audio, UNKNOWN_ERROR);
#endif
            } 
#ifndef ANDROID_DEFAULT_CODE
            else if (what == ACodec::kWhatPrepareCompleted) {
                int32_t nSuccess;
				
		  if (PREPARED == mPrepare)
		  	break;
                CHECK(codecRequest->findInt32("success", &nSuccess));
                CHECK(nSuccess);

                finishPrepareIfPossible(audio);
            }
#endif
            else {

                CHECK_EQ((int)what, (int)ACodec::kWhatDrainThisBuffer);
                renderBuffer(audio, codecRequest);

                }


            break;
        }

        case kWhatRendererNotify:
        {
            int32_t what;
            CHECK(msg->findInt32("what", &what));

            if (what == Renderer::kWhatEOS) {
                int32_t audio;
                CHECK(msg->findInt32("audio", &audio));

                int32_t finalResult;
                CHECK(msg->findInt32("finalResult", &finalResult));

                if (audio) {
                    mAudioEOS = true;
                } else {
                    mVideoEOS = true;
                }

                if (finalResult == ERROR_END_OF_STREAM) {
                    LOGD("reached %s EOS", audio ? "audio" : "video");
                } else {
                    LOGE("%s track encountered an error (%d)",
                         audio ? "audio" : "video", finalResult);

                    notifyListener(
                            MEDIA_ERROR, MEDIA_ERROR_UNKNOWN, finalResult);
                }

                if ((mAudioEOS || mAudioDecoder == NULL)
                        && (mVideoEOS || mVideoDecoder == NULL)) {
                    notifyListener(MEDIA_PLAYBACK_COMPLETE, 0, 0);
                }
            } else if (what == Renderer::kWhatPosition) {
                int64_t positionUs;
                CHECK(msg->findInt64("positionUs", &positionUs));

                CHECK(msg->findInt64("videoLateByUs", &mVideoLateByUs));

                if (mDriver != NULL) {
                    sp<NuPlayerDriver> driver = mDriver.promote();
                    if (driver != NULL) {
                        driver->notifyPosition(positionUs);
                        driver->notifyFrameStats(
                                mNumFramesTotal, mNumFramesDropped);
                    }
                }
            } else if (what == Renderer::kWhatFlushComplete) {
                CHECK_EQ(what, (int32_t)Renderer::kWhatFlushComplete);

                int32_t audio;
                CHECK(msg->findInt32("audio", &audio));

                LOGD("renderer %s flush completed.", audio ? "audio" : "video");
            }
            break;
        }

        case kWhatMoreDataQueued:
        {
            break;
        }

        case kWhatReset:
        {
            LOGD("kWhatReset");

            if (mRenderer != NULL) {
                // There's an edge case where the renderer owns all output
                // buffers and is paused, therefore the decoder will not read
                // more input data and will never encounter the matching
                // discontinuity. To avoid this, we resume the renderer.

                if (mFlushingAudio == AWAITING_DISCONTINUITY
                        || mFlushingVideo == AWAITING_DISCONTINUITY) {
                    mRenderer->resume();
                }
            }

#ifndef ANDROID_DEFAULT_CODE
            if ( (mAudioDecoder != NULL && IsFlushingState(mFlushingAudio)) ||
                 (mVideoDecoder != NULL && IsFlushingState(mFlushingVideo)) ) {
#else
            if (mFlushingAudio != NONE || mFlushingVideo != NONE) {
#endif
                // We're currently flushing, postpone the reset until that's
                // completed.

                LOGD("postponing reset mFlushingAudio=%d, mFlushingVideo=%d",
                        mFlushingAudio, mFlushingVideo);

                mResetPostponed = true;
                break;
            }

            if (mAudioDecoder == NULL && mVideoDecoder == NULL) {
                finishReset();
                break;
            }

            mTimeDiscontinuityPending = true;

            if (mAudioDecoder != NULL) {
                flushDecoder(true /* audio */, true /* needShutdown */);
            }

            if (mVideoDecoder != NULL) {
                flushDecoder(false /* audio */, true /* needShutdown */);
            }

            mResetInProgress = true;
            break;
        }

        case kWhatSeek:
        {
            int64_t seekTimeUs;
            CHECK(msg->findInt64("seekTimeUs", &seekTimeUs));

            LOGD("kWhatSeek seekTime(%.2f secs)", seekTimeUs / 1E6);
#ifndef ANDROID_DEFAULT_CODE
            if (mDataSourceType != SOURCE_HttpLive) {
                mSource->seekTo(seekTimeUs);
                if (mRenderer != NULL) {  //when seek done, resume render
                    if (mPlayState == PLAYING)
                        mRenderer->resume();
                }
                if (mDriver != NULL) {
                    sp<NuPlayerDriver> driver = mDriver.promote();
                    if (driver != NULL) {
                        driver->notifySeekComplete();
                    }
                }
                mSeekTimeUs = -1;

            } else {
                status_t seekStatus = mSource->seekTo(seekTimeUs);
                LOGD("@debug: SeekTime %.2f", seekTimeUs / 1E6);

                bool bWaitingFlush = false;
 
                if (OK == seekStatus) {
                    if (mAudioDecoder == NULL) {
                        LOGD("audio is not there, reset the flush flag");
                        mFlushingAudio = NONE;
                    } else if ( mFlushingAudio == NONE || mFlushingAudio == AWAITING_DISCONTINUITY)  {
                        flushDecoder(true /* audio */, true /* needShutdown */);
                        bWaitingFlush = true;
                    } else {
                        LOGD("audio is already being flushed");
                    }
                    
                    if (mVideoDecoder == NULL) {
                        LOGD("video is not there, reset the flush flag");
                    } else if (mFlushingVideo == NONE || mFlushingVideo == AWAITING_DISCONTINUITY) {
                        flushDecoder(false /* video */, true /* needShutdown */);
                        bWaitingFlush = true;
                    } else {
                        LOGD("video is already being flushed");
                    }

                    mSkipRenderingVideoUntilMediaTimeUs = seekTimeUs;
                    mSkipRenderingAudioUntilMediaTimeUs = seekTimeUs;
                    mTimeDiscontinuityPending = true;
                }

                if (mRenderer != NULL) {  //resume render
                    if (mPlayState == PLAYING)
                        mRenderer->resume();
                }

                //
                //complete the seek until flush is done
                //if need no flush, complete seek now
                if (!bWaitingFlush) {   
                    if (mDriver != NULL) {
                        sp<NuPlayerDriver> driver = mDriver.promote();
                        if (driver != NULL) {
                            driver->notifyPosition(mSeekTimeUs);
                            driver->notifySeekComplete();
                            LOGI("seek(%.2f)  complete without flushed", mSeekTimeUs / 1E6);
                        }
                    }
                    mSeekTimeUs = -1;    
                }
            }

#else
            mSource->seekTo(seekTimeUs);

            if (mDriver != NULL) {
                sp<NuPlayerDriver> driver = mDriver.promote();
                if (driver != NULL) {
                    driver->notifySeekComplete();
                }
            }
#endif
            
            break;
        }

        case kWhatPause:
        {
            LOGD("kWhatPause");
#ifndef ANDROID_DEFAULT_CODE
            mPlayState = PAUSED;
#endif
            CHECK(mRenderer != NULL);
            mRenderer->pause();
            break;
        }

        case kWhatResume:
        {
#ifndef ANDROID_DEFAULT_CODE
            mPlayState = PLAYING;
#endif
            CHECK(mRenderer != NULL);
            mRenderer->resume();
            break;
        }

        default:
            TRESPASS();
            break;
    }
}

#ifndef ANDROID_DEFAULT_CODE
bool NuPlayer::onScanSources() {
    bool needScanAgain = false;
    instantiateDecoder(false, &mVideoDecoder);
    
    if (mAudioSink != NULL) {
        instantiateDecoder(true, &mAudioDecoder);
    }

    status_t err;
    if ((err = mSource->feedMoreTSData()) != OK) {
        if (mAudioDecoder == NULL && mVideoDecoder == NULL) {
            // We're not currently decoding anything (no audio or
            // video tracks found) and we just ran out of input data.

            if (err == ERROR_END_OF_STREAM) {
                notifyListener(MEDIA_PLAYBACK_COMPLETE, 0, 0);
            } else {
                notifyListener(MEDIA_ERROR, MEDIA_ERROR_UNKNOWN, err);
            }
        }
        return false;
    } 

    needScanAgain = ((mAudioSink != NULL) && (mAudioDecoder == NULL))
        || ((mNativeWindow != NULL) && (mVideoDecoder == NULL));
    return needScanAgain;

}

void NuPlayer::finishPrepareIfPossible(bool bAudio) {
//#if 0 //debug: disable prepare
    if (mPrepare != PREPARING) {
        return;
    }
    bool bNotify = false;
    if (bAudio) {
        if (mAudioSink != NULL)
            bNotify = true;
    } else {
        if (mNativeWindow != NULL)
            bNotify = true;
    }
    if (bNotify) {
        CHECK(mDriver != NULL);
        finishPrepare(true);
        mPrepare = PREPARED;
    }
//#endif
}

void NuPlayer::finishPrepare(bool bSuccess) {
    if (mPrepareNotified)
        return;
    if (mDriver == NULL)
        return;
    sp<NuPlayerDriver> driver = mDriver.promote();
    if (driver != NULL) {
        driver->notifyPrepareComplete(bSuccess);
        LOGD("complete prepare %s", bSuccess?"success":"fail");
    }
    mPrepareNotified = true;
}
#endif

void NuPlayer::finishFlushIfPossible() {
#ifndef ANDROID_DEFAULT_CODE
    //If reset was postponed after one of the streams is flushed, complete it now
    if (mResetPostponed) {
        if ( (mAudioDecoder != NULL) &&
                (mFlushingAudio == NONE || mFlushingAudio == AWAITING_DISCONTINUITY) ) {
            flushDecoder(true /* audio */, true /* needShutdown */);
        }

        if ( (mVideoDecoder != NULL) &&
                (mFlushingVideo == NONE || mFlushingVideo == AWAITING_DISCONTINUITY) ) {
            flushDecoder(false /* video */, true /* needShutdown */);
        }
    }
#endif

    if (mFlushingAudio != FLUSHED && mFlushingAudio != SHUT_DOWN) {
        LOGD("not flushed, mFlushingAudio = %d", mFlushingAudio);
        return;
    }

    if (mFlushingVideo != FLUSHED && mFlushingVideo != SHUT_DOWN) {
        LOGD("not flushed, mFlushingVideo = %d", mFlushingVideo);
        return;
    }

    LOGD("both audio and video are flushed now.");

    if (mTimeDiscontinuityPending) {
    	if (mRenderer != NULL) {
	        mRenderer->signalTimeDiscontinuity();
	    } 
        mTimeDiscontinuityPending = false;
    }

#ifndef ANDROID_DEFAULT_CODE
    if (isSeeking_l()) {
        if (mDriver != NULL) {
            sp<NuPlayerDriver> driver = mDriver.promote();
            if (driver != NULL) {
                driver->notifyPosition(mSeekTimeUs);
                driver->notifySeekComplete();
                LOGI("seek(%.2f)  complete", mSeekTimeUs / 1E6);
            }
        }
        mSeekTimeUs = -1;
    }
#endif

    if (mAudioDecoder != NULL) {
        mAudioDecoder->signalResume();
    }

    if (mVideoDecoder != NULL) {
        mVideoDecoder->signalResume();
    }

    mFlushingAudio = NONE;
    mFlushingVideo = NONE;

    if (mResetInProgress) {
        LOGD("reset completed");

        mResetInProgress = false;
        finishReset();
    } else if (mResetPostponed) {
        (new AMessage(kWhatReset, id()))->post();
        mResetPostponed = false;
    } else if (mAudioDecoder == NULL || mVideoDecoder == NULL) {
        LOGD("Start scanning source after shutdown");
        postScanSources();
    }
}

void NuPlayer::finishReset() {
    CHECK(mAudioDecoder == NULL);
    CHECK(mVideoDecoder == NULL);

#ifndef ANDROID_DEFAULT_CODE
    mPlayState = STOPPED;
#endif
    ++mScanSourcesGeneration;
    mScanSourcesPending = false;

    if (mRenderer != NULL) {
        looper()->unregisterHandler(mRenderer->id());
        mRenderer.clear();
        mRenderer = NULL;
    }

    if (mSource != NULL) {
        mSource->stop();
        mSource.clear();
    }

    if (mDriver != NULL) {
        sp<NuPlayerDriver> driver = mDriver.promote();
        if (driver != NULL) {
            driver->notifyResetComplete();
        }
    }
}

void NuPlayer::postScanSources() {
    if (mScanSourcesPending) {
        return;
    }

    sp<AMessage> msg = new AMessage(kWhatScanSources, id());
    msg->setInt32("generation", mScanSourcesGeneration);
    msg->post();

    mScanSourcesPending = true;
}

status_t NuPlayer::instantiateDecoder(bool audio, sp<Decoder> *decoder) {
    if (*decoder != NULL) {
        return OK;
    }

    sp<MetaData> meta = mSource->getFormat(audio);

    if (meta == NULL) {
        return -EWOULDBLOCK;
    }

#ifndef ANDROID_DEFAULT_CODE
if(!audio)
{
    CHECK(meta->findInt32(kKeyWidth, &mVideoWidth));
    CHECK(meta->findInt32(kKeyHeight, &mVideoHeight));
   if (mDriver != NULL)
   {
      sp<NuPlayerDriver> driver = mDriver.promote();
      if (driver != NULL) 
            driver->notifyResolution(mVideoWidth, mVideoHeight);
   }
    
    meta->findInt64(kKeyHLSVideoFirestRender, &mVideoFirstRenderTimestamp);
    LOGE("mVideoFirstRenderTimestamp is %.2f sec, width = %d, height = %d", mVideoFirstRenderTimestamp / 1E6, mVideoWidth, mVideoHeight); 
}
    
#endif

    if (!audio) {
        const char *mime;
        CHECK(meta->findCString(kKeyMIMEType, &mime));
        mVideoIsAVC = !strcasecmp(MEDIA_MIMETYPE_VIDEO_AVC, mime);
    }

    sp<AMessage> notify =
        new AMessage(audio ? kWhatAudioNotify : kWhatVideoNotify,
                     id());

    *decoder = audio ? new Decoder(notify) :
                       new Decoder(notify, mNativeWindow);
    looper()->registerHandler(*decoder);

#ifndef ANDROID_DEFAULT_CODE
    CHECK(mFlushingAudio == NONE || mFlushingAudio == SHUT_DOWN); 
    CHECK(mFlushingAudio == NONE || mFlushingVideo == SHUT_DOWN); 
    (*decoder)->configure(meta, (mPlayState == PLAYING || mPlayState == PAUSED));
    LOGD("@debug: config decoder when mPlayState = %d", (int)mPlayState);
    //if preparing, don't let decoder autorun
#else
    (*decoder)->configure(meta);
#endif

    int64_t durationUs;
    if (mDriver != NULL && mSource->getDuration(&durationUs) == OK) {
        sp<NuPlayerDriver> driver = mDriver.promote();
        if (driver != NULL) {
            driver->notifyDuration(durationUs);
        }
    }

    return OK;
}

status_t NuPlayer::feedDecoderInputData(bool audio, const sp<AMessage> &msg) {
    sp<AMessage> reply;
    CHECK(msg->findMessage("reply", &reply));

    if ((audio && IsFlushingState(mFlushingAudio))
            || (!audio && IsFlushingState(mFlushingVideo))) {
        reply->setInt32("err", INFO_DISCONTINUITY);
        reply->post();
        return OK;
    }

    sp<ABuffer> accessUnit;

    bool dropAccessUnit;
    do {
        status_t err = mSource->dequeueAccessUnit(audio, &accessUnit);

        if (err == -EWOULDBLOCK) {
            return err;
        } else if (err != OK) {
            if (err == INFO_DISCONTINUITY) {
                int32_t type;
                CHECK(accessUnit->meta()->findInt32("discontinuity", &type));

                bool formatChange =
                    (audio &&
                     (type & ATSParser::DISCONTINUITY_AUDIO_FORMAT))
                    || (!audio &&
                            (type & ATSParser::DISCONTINUITY_VIDEO_FORMAT));

                bool timeChange = (type & ATSParser::DISCONTINUITY_TIME) != 0;

                LOGI("%s discontinuity (formatChange=%d, time=%d)",
                     audio ? "audio" : "video", formatChange, timeChange);

                if (audio) {
                    mSkipRenderingAudioUntilMediaTimeUs = -1;
                } else {
                    mSkipRenderingVideoUntilMediaTimeUs = -1;
                }

                if (timeChange) {
                    sp<AMessage> extra;
                    if (accessUnit->meta()->findMessage("extra", &extra)
                            && extra != NULL) {
                        int64_t resumeAtMediaTimeUs;
                        if (extra->findInt64(
                                    "resume-at-mediatimeUs", &resumeAtMediaTimeUs)) {
                            LOGI("suppressing rendering of %s until %lld us",
                                    audio ? "audio" : "video", resumeAtMediaTimeUs);

                            if (audio) {
                                mSkipRenderingAudioUntilMediaTimeUs =
                                    resumeAtMediaTimeUs;
                            } else {
                                mSkipRenderingVideoUntilMediaTimeUs =
                                    resumeAtMediaTimeUs;
                            }
                        }
                    }
                }

                mTimeDiscontinuityPending =
                    mTimeDiscontinuityPending || timeChange;

                if (formatChange || timeChange) {
                     
                    LOGD("flush decoder, formatChange = %d", formatChange);
                    flushDecoder(audio, formatChange);
                } else {
                    // This stream is unaffected by the discontinuity

                    if (audio) {
                        mFlushingAudio = FLUSHED;
                    } else {
                        mFlushingVideo = FLUSHED;
                    }

                    finishFlushIfPossible();

                    return -EWOULDBLOCK;
                }
            }

            reply->setInt32("err", err);
            reply->post();
            return OK;
        }

        if (!audio) {
            ++mNumFramesTotal;
        }

        dropAccessUnit = false;
        if (!audio
                && mVideoLateByUs > 100000ll
                && mVideoIsAVC
                && !IsAVCReferenceFrame(accessUnit)) {
            LOGD("drop %lld / %lld", mNumFramesDropped, mNumFramesTotal);
            dropAccessUnit = true;
            ++mNumFramesDropped;
        }
    } while (dropAccessUnit);

    // LOGV("returned a valid buffer of %s data", audio ? "audio" : "video");

#if 0
    int64_t mediaTimeUs;
    CHECK(accessUnit->meta()->findInt64("timeUs", &mediaTimeUs));
    LOGV("feeding %s input buffer at media time %.2f secs",
         audio ? "audio" : "video",
         mediaTimeUs / 1E6);
#endif

    reply->setObject("buffer", accessUnit);
    reply->post();

    return OK;
}

void NuPlayer::renderBuffer(bool audio, const sp<AMessage> &msg) {
    // LOGV("renderBuffer %s", audio ? "audio" : "video");

    sp<AMessage> reply;
    CHECK(msg->findMessage("reply", &reply));
#ifndef ANDROID_DEFAULT_CODE
    {
        Mutex::Autolock autoLock(mLock);
        if (mSeekTimeUs != -1) {
            sp<RefBase> obj0;
            CHECK(msg->findObject("buffer", &obj0));
            sp<ABuffer> buffer0 = static_cast<ABuffer *>(obj0.get());
            int64_t mediaTimeUs;
            CHECK(buffer0->meta()->findInt64("timeUs", &mediaTimeUs));
            LOGD("seeking, %s buffer (%.2f) drop", 
                    audio ? "audio" : "video", mediaTimeUs / 1E6);
            reply->post();
            return;
        }
    }
#endif
    if (IsFlushingState(audio ? mFlushingAudio : mFlushingVideo)) {
        // We're currently attempting to flush the decoder, in order
        // to complete this, the decoder wants all its buffers back,
        // so we don't want any output buffers it sent us (from before
        // we initiated the flush) to be stuck in the renderer's queue.

        LOGD("we're still flushing the %s decoder, sending its output buffer"
             " right back.", audio ? "audio" : "video");
#if 0
        sp<RefBase> obj0;
        CHECK(msg->findObject("buffer", &obj0));
        sp<ABuffer> buffer0 = static_cast<ABuffer *>(obj0.get());
        int64_t mediaTimeUs;
        CHECK(buffer0->meta()->findInt64("timeUs", &mediaTimeUs));
        LOGD("\t\t buffer (%.2f) drop", mediaTimeUs / 1E6);

#endif

        reply->post();
        return;
    }

    sp<RefBase> obj;
    CHECK(msg->findObject("buffer", &obj));

    sp<ABuffer> buffer = static_cast<ABuffer *>(obj.get());

    int64_t &skipUntilMediaTimeUs =
        audio
            ? mSkipRenderingAudioUntilMediaTimeUs
            : mSkipRenderingVideoUntilMediaTimeUs;

#ifndef ANDROID_DEFAULT_CODE

    if (skipUntilMediaTimeUs >= 0) {
        int64_t mediaTimeUs;
        CHECK(buffer->meta()->findInt64("timeUs", &mediaTimeUs));

        if (mediaTimeUs < skipUntilMediaTimeUs) {
            LOGV("dropping %s buffer at time %.2f s as requested.",
                 audio ? "audio" : "video",
                 mediaTimeUs / 1E6);

            reply->post();
            return;
        }
        LOGI("mediaTime > skipUntilMediaTimeUs ,skip done.mediaTimeUs = %.2f s, skiptime = %.2f", 
			                                                             mediaTimeUs / 1E6, skipUntilMediaTimeUs / 1E6);
        skipUntilMediaTimeUs = -1;
    }
	
#else

    if (skipUntilMediaTimeUs >= 0) {
        int64_t mediaTimeUs;
        CHECK(buffer->meta()->findInt64("timeUs", &mediaTimeUs));

        if (mediaTimeUs < skipUntilMediaTimeUs) {
            LOGV("dropping %s buffer at time %.2f s as requested.",
                 audio ? "audio" : "video",
                 mediaTimeUs / 1E6);

            reply->post();
            return;
        }
        LOGE("mediaTime > skipUntilMediaTimeUs ,skip done.mediaTimeUs = %.2f s, skiptime = %.2f", mediaTimeUs / 1E6, skipUntilMediaTimeUs / 1E6);
        skipUntilMediaTimeUs = -1;
    }
#endif	

#ifndef ANDROID_DEFAULT_CODE
    if(audio)
    {
        int64_t mediaTimeUs;
        CHECK(buffer->meta()->findInt64("timeUs", &mediaTimeUs));
	 if (mediaTimeUs < mVideoFirstRenderTimestamp)
	 {
             LOGE("Nuplayer::render audio is early than video drop  audio tp = %.2f sec, video first TP = %.2f sec", mediaTimeUs / 1E6, mVideoFirstRenderTimestamp / 1E6);
	      reply->post();
	      return;
	 }
    }
#endif

    mRenderer->queueBuffer(audio, buffer, reply);
}

void NuPlayer::notifyListener(int msg, int ext1, int ext2) {
    if (mDriver == NULL) {
        return;
    }

    sp<NuPlayerDriver> driver = mDriver.promote();

    if (driver == NULL) {
        return;
    }

#ifndef ANDROID_DEFAULT_CODE
    //try to report a more meaningful error
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

#endif
    driver->notifyListener(msg, ext1, ext2);
}

void NuPlayer::flushDecoder(bool audio, bool needShutdown) {
    LOGD("++flushDecoder[%s], mFlushing %d, %d", audio?"audio":"video", mFlushingAudio, mFlushingVideo);
    if ((audio && mAudioDecoder == NULL) || (!audio && mVideoDecoder == NULL)) {
        LOGI("flushDecoder %s without decoder present",
             audio ? "audio" : "video");
    }

    // Make sure we don't continue to scan sources until we finish flushing.
    ++mScanSourcesGeneration;
    mScanSourcesPending = false;

    (audio ? mAudioDecoder : mVideoDecoder)->signalFlush();
#ifndef ANDROID_DEFAULT_CODE
    if (mRenderer != NULL)
#endif
    mRenderer->flush(audio);

    FlushStatus newStatus =
        needShutdown ? FLUSHING_DECODER_SHUTDOWN : FLUSHING_DECODER;

    if (audio) {
        CHECK(mFlushingAudio == NONE
                || mFlushingAudio == AWAITING_DISCONTINUITY);

        mFlushingAudio = newStatus;

        if (mFlushingVideo == NONE) {
            mFlushingVideo = (mVideoDecoder != NULL)
                ? AWAITING_DISCONTINUITY
                : FLUSHED;
        }
    } else {
        CHECK(mFlushingVideo == NONE
                || mFlushingVideo == AWAITING_DISCONTINUITY);

        mFlushingVideo = newStatus;

#ifndef ANDROID_DEFAULT_CODE
                    mVideoFirstRenderTimestamp = -1;
#endif
 
        if (mFlushingAudio == NONE) {
            mFlushingAudio = (mAudioDecoder != NULL)
                ? AWAITING_DISCONTINUITY
                : FLUSHED;
        }
    }
    LOGD("--flushDecoder[%s] end, mFlushing %d, %d", audio?"audio":"video", mFlushingAudio, mFlushingVideo);
}



}  // namespace android
