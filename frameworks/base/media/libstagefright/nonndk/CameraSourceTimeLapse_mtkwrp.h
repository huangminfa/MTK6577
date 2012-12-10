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

#ifndef CAMERA_SOURCE_TIME_LAPSE_H__DEF

#define CAMERA_SOURCE_TIME_LAPSE_H__DEF

#include <pthread.h>

#include <utils/RefBase.h>
#include <utils/threads.h>

#ifdef CameraSourceTimeLapse 
#undef CameraSourceTimeLapse 
#endif 
#define CameraSourceTimeLapse    CameraSourceTimeLapse 

namespace android {

class CameraSourceTimeLapse : public CameraSource {
public:
    static CameraSourceTimeLapse * CreateFromCamera(const sp<ICamera> & camera,const sp<ICameraRecordingProxy> & proxy,int32_t cameraId,Size videoSize,int32_t videoFrameRate,const sp<Surface> & surface,int64_t timeBetweenTimeLapseFrameCaptureUs);
    virtual ~CameraSourceTimeLapse();
    void startQuickReadReturns();
private:
    CameraSourceTimeLapse(const sp<ICamera> & camera,const sp<ICameraRecordingProxy> & proxy,int32_t cameraId,Size videoSize,int32_t videoFrameRate,const sp<Surface> & surface,int64_t timeBetweenTimeLapseFrameCaptureUs);
    virtual void signalBufferReturned(MediaBuffer * buffer);
    virtual status_t read(MediaBuffer * * buffer,const ReadOptions * options = NULL);
    virtual void stopCameraRecording();
    virtual bool skipCurrentFrame(int64_t timestampUs);
    virtual void dataCallbackTimestamp(int64_t timestampUs,int32_t msgType,const sp<IMemory> & data);
    CameraSourceTimeLapse(const CameraSourceTimeLapse &);

};

}  // namespace android 

#endif //CAMERA_SOURCE_TIME_LAPSE_H__DEF