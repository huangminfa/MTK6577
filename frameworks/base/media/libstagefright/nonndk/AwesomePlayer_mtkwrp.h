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

#ifndef AWESOME_PLAYER_H__DEF

#define AWESOME_PLAYER_H__DEF

#include "HTTPBase.h"
#include "TimedEventQueue.h"

#include <media/MediaPlayerInterface.h>
#include <media/stagefright/DataSource.h>
#include <media/stagefright/OMXClient.h>
#include <media/stagefright/TimeSource.h>
#include <utils/threads.h>
#include <drm/DrmManagerClient.h>




#ifdef AwesomePlayer 
#undef AwesomePlayer 
#endif 
#define AwesomePlayer    AwesomePlayer 

namespace android {

struct AwesomePlayer  {
public:
    AwesomePlayer();
    ~AwesomePlayer();
    void setListener(const wp<MediaPlayerBase> & listener);
    void setUID(uid_t uid);
    status_t setDataSource(const char * uri,const KeyedVector <String8,String8> * headers = NULL);
    status_t setDataSource(int fd,int64_t offset,int64_t length);
    status_t setDataSource(const sp<IStreamSource> & source);
    void reset();
    status_t prepare();
    status_t prepare_l();
    status_t prepareAsync();
    status_t prepareAsync_l();
    status_t play();
    status_t pause();
    bool isPlaying() const;
    status_t setSurfaceTexture(const sp<ISurfaceTexture> & surfaceTexture);
    void setAudioSink(const sp<MediaPlayerBase::AudioSink> & audioSink);
    status_t setLooping(bool shouldLoop);
    status_t getDuration(int64_t * durationUs);
    status_t getPosition(int64_t * positionUs);
    status_t setParameter(int key,const Parcel & request);
    status_t getParameter(int key,Parcel * reply);
    status_t setCacheStatCollectFreq(const Parcel & request);
    status_t seekTo(int64_t timeUs);
    uint32_t flags() const;
    void postAudioEOS(int64_t delayUs = 0ll);
    void postAudioSeekComplete();
    status_t setTimedTextTrackIndex(int32_t index);
    status_t dump(int fd,const Vector<String16> & args) const;
private:
    AwesomePlayer(const AwesomePlayer &);

};

}  // namespace android 

#endif //AWESOME_PLAYER_H__DEF