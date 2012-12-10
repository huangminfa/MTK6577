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
#define LOG_TAG "AudioPlayer"
#include <utils/Log.h>

#include <binder/IPCThreadState.h>
#include <media/AudioTrack.h>
#include <media/stagefright/foundation/ADebug.h>
#include <media/stagefright/AudioPlayer.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MediaErrors.h>
#include <media/stagefright/MediaSource.h>
#include <media/stagefright/MetaData.h>

#include "include/AwesomePlayer.h"


using namespace android; 
#include <utils/KeyedVector.h> 
#include <utils/threads.h> 
static KeyedVector<int, AudioPlayer_mtk*> mtkAudioPlayerObjList; 
static Mutex mtkAudioPlayerObjListLock; 

#include <media/stagefright/AudioPlayer_mtkwrp.h>

namespace android { 

AudioPlayer::AudioPlayer(const sp<MediaPlayerBase::AudioSink> & audioSink,AwesomePlayer * audioObserver)
{ 
    AudioPlayer_mtk* mtkInst = new AudioPlayer_mtk(audioSink,audioObserver); 
    mtkAudioPlayerObjListLock.lock(); 
    mtkAudioPlayerObjList.add((int)this, mtkInst); 
    mtkAudioPlayerObjListLock.unlock(); 
} 
 
AudioPlayer::~AudioPlayer()
{ 
    AudioPlayer_mtk *inst = NULL; 
    mtkAudioPlayerObjListLock.lock(); 
    inst = mtkAudioPlayerObjList.valueFor((int)this); 
    mtkAudioPlayerObjList.removeItem((int)this); 
    mtkAudioPlayerObjListLock.unlock(); 
    if(inst) delete inst; 
} 
 
void    AudioPlayer::setSource(const sp<MediaSource> & source)
{ 
    AudioPlayer_mtk *inst = NULL; 
    mtkAudioPlayerObjListLock.lock(); 
    inst = mtkAudioPlayerObjList.valueFor((int)this); 
    mtkAudioPlayerObjListLock.unlock(); 
    return (void)inst->setSource(source); 
} 
 
int64_t    AudioPlayer::getRealTimeUs()
{ 
    AudioPlayer_mtk *inst = NULL; 
    mtkAudioPlayerObjListLock.lock(); 
    inst = mtkAudioPlayerObjList.valueFor((int)this); 
    mtkAudioPlayerObjListLock.unlock(); 
    return (int64_t)inst->getRealTimeUs(); 
} 
 
status_t    AudioPlayer::start(bool sourceAlreadyStarted)
{ 
    AudioPlayer_mtk *inst = NULL; 
    mtkAudioPlayerObjListLock.lock(); 
    inst = mtkAudioPlayerObjList.valueFor((int)this); 
    mtkAudioPlayerObjListLock.unlock(); 
    return (status_t)inst->start(sourceAlreadyStarted); 
} 
 
void    AudioPlayer::pause(bool playPendingSamples)
{ 
    AudioPlayer_mtk *inst = NULL; 
    mtkAudioPlayerObjListLock.lock(); 
    inst = mtkAudioPlayerObjList.valueFor((int)this); 
    mtkAudioPlayerObjListLock.unlock(); 
    return (void)inst->pause(playPendingSamples); 
} 
 
void    AudioPlayer::resume()
{ 
    AudioPlayer_mtk *inst = NULL; 
    mtkAudioPlayerObjListLock.lock(); 
    inst = mtkAudioPlayerObjList.valueFor((int)this); 
    mtkAudioPlayerObjListLock.unlock(); 
    return (void)inst->resume(); 
} 
 
int64_t    AudioPlayer::getMediaTimeUs()
{ 
    AudioPlayer_mtk *inst = NULL; 
    mtkAudioPlayerObjListLock.lock(); 
    inst = mtkAudioPlayerObjList.valueFor((int)this); 
    mtkAudioPlayerObjListLock.unlock(); 
    return (int64_t)inst->getMediaTimeUs(); 
} 
 
bool    AudioPlayer::getMediaTimeMapping(int64_t * realtime_us,int64_t * mediatime_us)
{ 
    AudioPlayer_mtk *inst = NULL; 
    mtkAudioPlayerObjListLock.lock(); 
    inst = mtkAudioPlayerObjList.valueFor((int)this); 
    mtkAudioPlayerObjListLock.unlock(); 
    return (bool)inst->getMediaTimeMapping(realtime_us,mediatime_us); 
} 
 
status_t    AudioPlayer::seekTo(int64_t time_us)
{ 
    AudioPlayer_mtk *inst = NULL; 
    mtkAudioPlayerObjListLock.lock(); 
    inst = mtkAudioPlayerObjList.valueFor((int)this); 
    mtkAudioPlayerObjListLock.unlock(); 
    return (status_t)inst->seekTo(time_us); 
} 
 
bool    AudioPlayer::isSeeking()
{ 
    AudioPlayer_mtk *inst = NULL; 
    mtkAudioPlayerObjListLock.lock(); 
    inst = mtkAudioPlayerObjList.valueFor((int)this); 
    mtkAudioPlayerObjListLock.unlock(); 
    return (bool)inst->isSeeking(); 
} 
 
bool    AudioPlayer::reachedEOS(status_t * finalStatus)
{ 
    AudioPlayer_mtk *inst = NULL; 
    mtkAudioPlayerObjListLock.lock(); 
    inst = mtkAudioPlayerObjList.valueFor((int)this); 
    mtkAudioPlayerObjListLock.unlock(); 
    return (bool)inst->reachedEOS(finalStatus); 
} 
 

}  // namespace android 
