
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

//MTK_OP01_PROTECT_START
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


#define USE_SURFACE_ALLOC 1
#define FRAME_DROP_FREQ 0

using namespace android; 
#include <utils/KeyedVector.h> 
#include <utils/threads.h> 
static KeyedVector<int, AwesomePlayer_mtk*> mtkAwesomePlayerObjList; 
static Mutex mtkAwesomePlayerObjListLock; 

#include "include/AwesomePlayer_mtkwrp.h"

namespace android { 

AwesomePlayer::AwesomePlayer()
{ 
    AwesomePlayer_mtk* mtkInst = new AwesomePlayer_mtk(); 
    mtkAwesomePlayerObjListLock.lock(); 
    mtkAwesomePlayerObjList.add((int)this, mtkInst); 
    mtkAwesomePlayerObjListLock.unlock(); 
} 
 
AwesomePlayer::~AwesomePlayer()
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjList.removeItem((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    if(inst) delete inst; 
} 
 
void    AwesomePlayer::setListener(const wp<MediaPlayerBase> & listener)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (void)inst->setListener(listener); 
} 
 
void    AwesomePlayer::setUID(uid_t uid)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (void)inst->setUID(uid); 
} 
 
status_t    AwesomePlayer::setDataSource(const char * uri,const KeyedVector <String8,String8> * headers)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->setDataSource(uri,headers); 
} 
 
status_t    AwesomePlayer::setDataSource(int fd,int64_t offset,int64_t length)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->setDataSource(fd,offset,length); 
} 
 
status_t    AwesomePlayer::setDataSource(const sp<IStreamSource> & source)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->setDataSource(source); 
} 
 
void    AwesomePlayer::reset()
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (void)inst->reset(); 
} 
 
status_t    AwesomePlayer::prepare()
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->prepare(); 
} 
 
status_t    AwesomePlayer::prepare_l()
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->prepare_l(); 
} 
 
status_t    AwesomePlayer::prepareAsync()
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->prepareAsync(); 
} 
 
status_t    AwesomePlayer::prepareAsync_l()
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->prepareAsync_l(); 
} 
 
status_t    AwesomePlayer::play()
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->play(); 
} 
 
status_t    AwesomePlayer::pause()
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->pause(); 
} 
 
bool    AwesomePlayer::isPlaying()  const
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (bool)inst->isPlaying(); 
} 
 
status_t    AwesomePlayer::setSurfaceTexture(const sp<ISurfaceTexture> & surfaceTexture)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->setSurfaceTexture(surfaceTexture); 
} 
 
void    AwesomePlayer::setAudioSink(const sp<MediaPlayerBase::AudioSink> & audioSink)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (void)inst->setAudioSink(audioSink); 
} 
 
status_t    AwesomePlayer::setLooping(bool shouldLoop)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->setLooping(shouldLoop); 
} 
 
status_t    AwesomePlayer::getDuration(int64_t * durationUs)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->getDuration(durationUs); 
} 
 
status_t    AwesomePlayer::getPosition(int64_t * positionUs)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->getPosition(positionUs); 
} 
 
status_t    AwesomePlayer::setParameter(int key,const Parcel & request)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->setParameter(key,request); 
} 
 
status_t    AwesomePlayer::getParameter(int key,Parcel * reply)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->getParameter(key,reply); 
} 
 
status_t    AwesomePlayer::setCacheStatCollectFreq(const Parcel & request)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->setCacheStatCollectFreq(request); 
} 
 
status_t    AwesomePlayer::seekTo(int64_t timeUs)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->seekTo(timeUs); 
} 
 
uint32_t    AwesomePlayer::flags()  const
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (uint32_t)inst->flags(); 
} 
 
void    AwesomePlayer::postAudioEOS(int64_t delayUs)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (void)inst->postAudioEOS(delayUs); 
} 
 
void    AwesomePlayer::postAudioSeekComplete()
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (void)inst->postAudioSeekComplete(); 
} 
 
status_t    AwesomePlayer::setTimedTextTrackIndex(int32_t index)
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->setTimedTextTrackIndex(index); 
} 
 
status_t    AwesomePlayer::dump(int fd,const Vector<String16> & args)  const
{ 
    AwesomePlayer_mtk *inst = NULL; 
    mtkAwesomePlayerObjListLock.lock(); 
    inst = mtkAwesomePlayerObjList.valueFor((int)this); 
    mtkAwesomePlayerObjListLock.unlock(); 
    return (status_t)inst->dump(fd,args); 
} 
 

}  // namespace android 
