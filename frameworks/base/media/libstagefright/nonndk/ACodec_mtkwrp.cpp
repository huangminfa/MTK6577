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
//    #define LOG_NDEBUG 0
    #define LOG_TAG "ACodec"
    #include <utils/Log.h>

#include <media/stagefright/ACodec.h>

#include <binder/MemoryDealer.h>

#include <media/stagefright/foundation/hexdump.h>
#include <media/stagefright/foundation/ABuffer.h>
#include <media/stagefright/foundation/ADebug.h>
#include <media/stagefright/foundation/AMessage.h>

#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/NativeWindowWrapper.h>
#include <media/stagefright/OMXClient.h>
#include <media/stagefright/OMXCodec.h>

#include <surfaceflinger/Surface.h>
#include <gui/SurfaceTextureClient.h>

#include <OMX_Component.h>


using namespace android; 
#include <utils/KeyedVector.h> 
#include <utils/threads.h> 
static KeyedVector<int, ACodec_mtk*> mtkACodecObjList; 
static Mutex mtkACodecObjListLock; 

#include <media/stagefright/ACodec_mtkwrp.h>

namespace android { 

ACodec::ACodec()
{ 
    ACodec_mtk* mtkInst = new ACodec_mtk(); 
    mtkACodecObjListLock.lock(); 
    mtkACodecObjList.add((int)this, mtkInst); 
    mtkACodecObjListLock.unlock(); 
} 
 
void    ACodec::setNotificationMessage(const sp<AMessage> & msg)
{ 
    ACodec_mtk *inst = NULL; 
    mtkACodecObjListLock.lock(); 
    inst = mtkACodecObjList.valueFor((int)this); 
    mtkACodecObjListLock.unlock(); 
    return (void)inst->setNotificationMessage(msg); 
} 
 
void    ACodec::initiateSetup(const sp<AMessage> & msg)
{ 
    ACodec_mtk *inst = NULL; 
    mtkACodecObjListLock.lock(); 
    inst = mtkACodecObjList.valueFor((int)this); 
    mtkACodecObjListLock.unlock(); 
    return (void)inst->initiateSetup(msg); 
} 
 
void    ACodec::signalFlush()
{ 
    ACodec_mtk *inst = NULL; 
    mtkACodecObjListLock.lock(); 
    inst = mtkACodecObjList.valueFor((int)this); 
    mtkACodecObjListLock.unlock(); 
    return (void)inst->signalFlush(); 
} 
 
void    ACodec::signalResume()
{ 
    ACodec_mtk *inst = NULL; 
    mtkACodecObjListLock.lock(); 
    inst = mtkACodecObjList.valueFor((int)this); 
    mtkACodecObjListLock.unlock(); 
    return (void)inst->signalResume(); 
} 
 
void    ACodec::initiateShutdown()
{ 
    ACodec_mtk *inst = NULL; 
    mtkACodecObjListLock.lock(); 
    inst = mtkACodecObjList.valueFor((int)this); 
    mtkACodecObjListLock.unlock(); 
    return (void)inst->initiateShutdown(); 
} 
 
ACodec::~ACodec()
{ 
    ACodec_mtk *inst = NULL; 
    mtkACodecObjListLock.lock(); 
    inst = mtkACodecObjList.valueFor((int)this); 
    mtkACodecObjList.removeItem((int)this); 
    mtkACodecObjListLock.unlock(); 
} 
 

}  // namespace android 
