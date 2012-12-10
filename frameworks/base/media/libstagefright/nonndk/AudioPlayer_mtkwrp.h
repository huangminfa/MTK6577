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

#ifndef AUDIO_PLAYER_H__DEF

#define AUDIO_PLAYER_H__DEF

#include <media/MediaPlayerInterface.h>
#include <media/stagefright/MediaBuffer.h>
#include <media/stagefright/TimeSource.h>
#include <utils/threads.h>

#ifdef AudioPlayer 
#undef AudioPlayer 
#endif 
#define AudioPlayer    AudioPlayer 

namespace android {

class AudioPlayer : public TimeSource {
public:

    enum {
        REACHED_EOS,
        SEEK_COMPLETE
    };
public:
    AudioPlayer(const sp<MediaPlayerBase::AudioSink> & audioSink,AwesomePlayer * audioObserver = NULL);
    virtual ~AudioPlayer();
    void setSource(const sp<MediaSource> & source);
    virtual int64_t getRealTimeUs();
    status_t start(bool sourceAlreadyStarted = false);
    void pause(bool playPendingSamples = false);
    void resume();
    int64_t getMediaTimeUs();
    bool getMediaTimeMapping(int64_t * realtime_us,int64_t * mediatime_us);
    status_t seekTo(int64_t time_us);
    bool isSeeking();
    bool reachedEOS(status_t * finalStatus);
private:
    AudioPlayer(const AudioPlayer &);

};

}  // namespace android 

#endif //AUDIO_PLAYER_H__DEF