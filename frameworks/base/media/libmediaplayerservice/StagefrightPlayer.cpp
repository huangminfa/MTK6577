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

//#define LOG_NDEBUG 0
#define LOG_TAG "StagefrightPlayer"
#include <utils/Log.h>
#include <cutils/xlog.h>

#include "StagefrightPlayer.h"

#include "AwesomePlayer.h"

#include <media/Metadata.h>
#include <media/stagefright/MediaExtractor.h>

#ifndef ANDROID_DEFAULT_CODE 
#include <media/stagefright/MetaData.h>
#include <cutils/properties.h>
#include <linux/rtpm_prio.h>
#endif

namespace android {

#ifndef ANDROID_DEFAULT_CODE 
 //qian 2011-01-14
int gPlayerPriority  = ANDROID_PRIORITY_NORMAL;
int gPlayerScheduler = SCHED_NORMAL;
int gPlayerSetFlag   = 0;

enum
{
	SET_HIGH=0,
	SET_RESUME=1
};
void get_player_scheduler(void)
{ 
	char value[PROPERTY_VALUE_MAX];
    property_get("set.player.scheduler", value, "0");
    gPlayerSetFlag = atol(value);
	//LOGE("gPlayerSetFlag=%ld",gPlayerSetFlag);

	if(gPlayerSetFlag==1)
	{
		struct sched_param sched_p;
		sched_getparam(0, &sched_p);
	    gPlayerPriority = sched_p.sched_priority ;
	    gPlayerScheduler = sched_getscheduler(gettid());
		//LOGE("get_player_scheduler :scheduler=%d priority:%d", gPlayerScheduler,gPlayerPriority);
   }		
}
void set_player_scheduler(bool flag)
{
	if(gPlayerSetFlag==1)
	{
	
#ifdef MT6575
		switch(flag)
		{
			case SET_HIGH:
			{
				struct sched_param sched_p;
			    sched_getparam(0, &sched_p);				
			    sched_p.sched_priority = RTPM_PRIO_VIDEO_PLAYBACK_THREAD + 1;

			    if (0 != sched_setscheduler(0, SCHED_RR, &sched_p)) {
			        LOGE("set_player_scheduler SET_HIGH fail...");
			    }
			    else {
			        sched_p.sched_priority = 0;
			        sched_getparam(0, &sched_p);
			       // LOGE("set_player_scheduler SET_HIGH ok...,scheduler=%d priority:%d", SCHED_RR,sched_p.sched_priority);
			    } 
				break;
			}
				
				
			case SET_RESUME:
			{
				struct sched_param sched_p;
				
			    sched_p.sched_priority = gPlayerPriority;

			    if (0 != sched_setscheduler(0, gPlayerScheduler, &sched_p)) {
			        LOGE("set_player_scheduler SET_RESUME fail...");
			    }
			    else {
			        sched_p.sched_priority = 0;
			        sched_getparam(0, &sched_p);
			//        LOGE("set_player_scheduler SET_RESUME ok...,scheduler=%d priority:%d", gPlayerScheduler,sched_p.sched_priority);
			    }
				break;	 
			}
				
				
			default:
				break;
		}
#endif//#ifdef MT6575
	}
}
    
#endif

StagefrightPlayer::StagefrightPlayer()
    : mPlayer(new AwesomePlayer) {
    LOGV("StagefrightPlayer");

    mPlayer->setListener(this);
}

StagefrightPlayer::~StagefrightPlayer() {
    LOGV("~StagefrightPlayer");
    reset();

    delete mPlayer;
    mPlayer = NULL;
}

status_t StagefrightPlayer::initCheck() {
    LOGV("initCheck");
    return OK;
}

status_t StagefrightPlayer::setUID(uid_t uid) {
    mPlayer->setUID(uid);

    return OK;
}

status_t StagefrightPlayer::setDataSource(
        const char *url, const KeyedVector<String8, String8> *headers) {
    return mPlayer->setDataSource(url, headers);
}

// Warning: The filedescriptor passed into this method will only be valid until
// the method returns, if you want to keep it, dup it!
status_t StagefrightPlayer::setDataSource(int fd, int64_t offset, int64_t length) {
    LOGV("setDataSource(%d, %lld, %lld)", fd, offset, length);
    return mPlayer->setDataSource(dup(fd), offset, length);
}

status_t StagefrightPlayer::setDataSource(const sp<IStreamSource> &source) {
    return mPlayer->setDataSource(source);
}

status_t StagefrightPlayer::setVideoSurfaceTexture(
        const sp<ISurfaceTexture> &surfaceTexture) {
    LOGV("setVideoSurfaceTexture");

    return mPlayer->setSurfaceTexture(surfaceTexture);
}

status_t StagefrightPlayer::prepare() {
    return mPlayer->prepare();
}

status_t StagefrightPlayer::prepareAsync() {
    return mPlayer->prepareAsync();
}

status_t StagefrightPlayer::start() {
    LOGV("start");

    return mPlayer->play();
}

status_t StagefrightPlayer::stop() {
    LOGV("stop");

#ifndef ANDROID_DEFAULT_CODE
    return mPlayer->pause(true);
#else
    return pause();  // what's the difference?
#endif // #ifndef ANDROID_DEFAULT_CODE
}

status_t StagefrightPlayer::pause() {
    LOGV("pause");
#ifndef ANDROID_DEFAULT_CODE 
	get_player_scheduler();
	set_player_scheduler(SET_HIGH);
	status_t sts= mPlayer->pause();
	set_player_scheduler(SET_RESUME);
	return sts;
#else
    return mPlayer->pause();
#endif
}

bool StagefrightPlayer::isPlaying() {
    LOGV("isPlaying");
    return mPlayer->isPlaying();
}

status_t StagefrightPlayer::seekTo(int msec) {
    LOGV("seekTo %.2f secs", msec / 1E3);
#ifndef ANDROID_DEFAULT_CODE 
	get_player_scheduler();
	set_player_scheduler(SET_HIGH);
	status_t err = mPlayer->seekTo((int64_t)msec * 1000);
	set_player_scheduler(SET_RESUME);
	return err;
#else
    status_t err = mPlayer->seekTo((int64_t)msec * 1000);
    return err;
#endif
}

status_t StagefrightPlayer::getCurrentPosition(int *msec) {
    LOGV("getCurrentPosition");

    int64_t positionUs;
    status_t err = mPlayer->getPosition(&positionUs);

    if (err != OK) {
        return err;
    }

    *msec = (positionUs + 500) / 1000;

    return OK;
}

status_t StagefrightPlayer::getDuration(int *msec) {
    LOGV("getDuration");

    int64_t durationUs;
    status_t err = mPlayer->getDuration(&durationUs);

    if (err != OK) {
        *msec = 0;
        return OK;
    }

    *msec = (durationUs + 500) / 1000;

    return OK;
}

status_t StagefrightPlayer::reset() {
    LOGV("reset");

    mPlayer->reset();

    return OK;
}

status_t StagefrightPlayer::setLooping(int loop) {
    LOGV("setLooping");

    return mPlayer->setLooping(loop);
}

player_type StagefrightPlayer::playerType() {
    LOGV("playerType");
    return STAGEFRIGHT_PLAYER;
}

status_t StagefrightPlayer::invoke(const Parcel &request, Parcel *reply) {
    return INVALID_OPERATION;
}

void StagefrightPlayer::setAudioSink(const sp<AudioSink> &audioSink) {
    MediaPlayerInterface::setAudioSink(audioSink);

    mPlayer->setAudioSink(audioSink);
}

status_t StagefrightPlayer::setParameter(int key, const Parcel &request) {
    LOGV("setParameter");
    return mPlayer->setParameter(key, request);
}

status_t StagefrightPlayer::getParameter(int key, Parcel *reply) {
    LOGV("getParameter");
    return mPlayer->getParameter(key, reply);
}

status_t StagefrightPlayer::getMetadata(
        const media::Metadata::Filter& ids, Parcel *records) {
    using media::Metadata;

    uint32_t flags = mPlayer->flags();

    Metadata metadata(records);

    metadata.appendBool(
            Metadata::kPauseAvailable,
            flags & MediaExtractor::CAN_PAUSE);

    metadata.appendBool(
            Metadata::kSeekBackwardAvailable,
            flags & MediaExtractor::CAN_SEEK_BACKWARD);

    metadata.appendBool(
            Metadata::kSeekForwardAvailable,
            flags & MediaExtractor::CAN_SEEK_FORWARD);

    metadata.appendBool(
            Metadata::kSeekAvailable,
            flags & MediaExtractor::CAN_SEEK);

#ifndef ANDROID_DEFAULT_CODE
    // Set video width/height to indicate that video exists
    // This is for new Gallery/Gallery3D
    int32_t width, height;
    if (mPlayer->getVideoDimensions(&width, &height) == OK) {
        if (width > 0 && height > 0) {
            metadata.appendInt32(Metadata::kVideoWidth, width);
            metadata.appendInt32(Metadata::kVideoHeight, height);
        }
    }

    sp<MetaData> meta = mPlayer->getMetaData();
    if (meta != NULL) {
        int timeout = 0;
        if (meta->findInt32(kKeyServerTimeout, &timeout) && timeout > 0) {
            metadata.appendInt32(Metadata::kServerTimeout, timeout);
        }

        const char *val;
        if (meta->findCString(kKeyTitle, &val)) {
            LOGI("meta title %s ", val);
            metadata.appendString(Metadata::kTitle, val);
        }
        if (meta->findCString(kKeyAuthor, &val)) {
            LOGI("meta author %s ", val);
            metadata.appendString(Metadata::kAuthor, val);
        }
    }
#endif
    return OK;
}
//MTK_OP01_PROTECT_START
#ifndef ANDROID_DEFAULT_CODE
status_t StagefrightPlayer::capture(const char* uri) 
{
    LOGV("Capture");
    return mPlayer->capture(uri);
}
#endif
//MTK_OP01_PROTECT_END
status_t StagefrightPlayer::dump(int fd, const Vector<String16> &args) const {
    return mPlayer->dump(fd, args);
}

#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_DRM_APP
status_t StagefrightPlayer::addValue(String8 value)
{
    XLOGV("StagefrightPlayer::addValue()");
    return mPlayer->addValue(value);
}
#endif
#endif
}  // namespace android
