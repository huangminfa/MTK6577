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
#define LOG_TAG "CameraSourceTimeLapse"

#include <binder/IPCThreadState.h>
#include <binder/MemoryBase.h>
#include <binder/MemoryHeapBase.h>
#include <media/stagefright/CameraSource.h>
#include <media/stagefright/CameraSourceTimeLapse.h>
#include <media/stagefright/MediaDebug.h>
#include <media/stagefright/MetaData.h>
#include <camera/Camera.h>
#include <camera/CameraParameters.h>
#include <utils/String8.h>
#include <utils/Vector.h>

using namespace android; 
#include <utils/KeyedVector.h> 
#include <utils/threads.h> 
static KeyedVector<int, CameraSourceTimeLapse_mtk*> mtkCameraSourceTimeLapseObjList; 
static Mutex mtkCameraSourceTimeLapseObjListLock; 

#include <media/stagefright/CameraSourceTimeLapse_mtkwrp.h>

namespace android { 

CameraSourceTimeLapse *    CameraSourceTimeLapse::CreateFromCamera(const sp<ICamera> & camera,const sp<ICameraRecordingProxy> & proxy,int32_t cameraId,Size videoSize,int32_t videoFrameRate,const sp<Surface> & surface,int64_t timeBetweenTimeLapseFrameCaptureUs)
{ 
    CameraSourceTimeLapse *    rtn; 
    const sp<ICamera> & a0 = NULL;
    const sp<ICameraRecordingProxy> & a1 = NULL;
    int32_t a2;
    Size a3;
    int32_t a4;
    const sp<Surface> & a5 = NULL;
    int64_t a6;
    CameraSourceTimeLapse * obj = new CameraSourceTimeLapse(a0,a1,a2,a3,a4,a5,a6); 
    CameraSourceTimeLapse_mtk * mtkInst = CameraSourceTimeLapse_mtk::CreateFromCamera(camera,proxy,cameraId,videoSize,videoFrameRate,surface,timeBetweenTimeLapseFrameCaptureUs); 
    if (mtkInst == NULL){ 
        if(obj != NULL) delete obj;
        return NULL;
    }
    mtkCameraSourceTimeLapseObjListLock.lock(); 
    mtkCameraSourceTimeLapseObjList.add((int)obj, mtkInst); 
    mtkCameraSourceTimeLapseObjListLock.unlock(); 
    return (CameraSourceTimeLapse *)obj; 
} 
 
CameraSourceTimeLapse::~CameraSourceTimeLapse()
{ 
    CameraSourceTimeLapse_mtk *inst = NULL; 
    mtkCameraSourceTimeLapseObjListLock.lock(); 
    inst = mtkCameraSourceTimeLapseObjList.valueFor((int)this); 
    mtkCameraSourceTimeLapseObjList.removeItem((int)this); 
    mtkCameraSourceTimeLapseObjListLock.unlock(); 
    if(inst) delete inst; 
} 
 
void    CameraSourceTimeLapse::startQuickReadReturns()
{ 
    CameraSourceTimeLapse_mtk *inst = NULL; 
    mtkCameraSourceTimeLapseObjListLock.lock(); 
    inst = mtkCameraSourceTimeLapseObjList.valueFor((int)this); 
    mtkCameraSourceTimeLapseObjListLock.unlock(); 
    return (void)inst->startQuickReadReturns(); 
} 
 
CameraSourceTimeLapse::CameraSourceTimeLapse(const sp<ICamera> & camera,const sp<ICameraRecordingProxy> & proxy,int32_t cameraId,Size videoSize,int32_t videoFrameRate,const sp<Surface> & surface,int64_t timeBetweenTimeLapseFrameCaptureUs)
:CameraSource(camera, proxy, cameraId, videoSize, videoFrameRate, surface, true)
{ 
} 
 
void    CameraSourceTimeLapse::signalBufferReturned(MediaBuffer * buffer)
{ 
    return; 
} 
 
status_t    CameraSourceTimeLapse::read(MediaBuffer * * buffer,const ReadOptions * options)
{ 
    status_t rtn; 
    return rtn; 
} 
 
void    CameraSourceTimeLapse::stopCameraRecording()
{ 
    return; 
} 
 
bool    CameraSourceTimeLapse::skipCurrentFrame(int64_t timestampUs)
{ 
    bool rtn; 
    return rtn; 
} 
 
void    CameraSourceTimeLapse::dataCallbackTimestamp(int64_t timestampUs,int32_t msgType,const sp<IMemory> & data)
{ 
    return; 
} 
 

}  // namespace android 
