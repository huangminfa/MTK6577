/*
 * Copyright (C) 2007 The Android Open Source Project
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

#ifndef ANDROID_AUDIO_RESAMPLER_MTK_H
#define ANDROID_AUDIO_RESAMPLER_MTK_H

#include <stdint.h>
#include <string.h>
#include <sys/types.h>
#include <cutils/log.h>
#include <utils/threads.h>
#include <stdlib.h>
#include <unistd.h>

#include "AudioResampler.h"
extern "C" {
#include "bli_exp.h"
}


namespace android {
// ----------------------------------------------------------------------------

class AudioResamplerMtk : public AudioResampler {

public:
    AudioResamplerMtk(int bitDepth, int inChannelCount, int32_t sampleRate);

    ~AudioResamplerMtk();
    virtual void resample(int32_t* out, size_t outFrameCount,
            AudioBufferProvider* provider);
    void reset(void);

private:

    void init();
    uint32_t mWorkBufSize;
    char  *mWorkBuf;
    BLI_HANDLE *pSrcHdl	;
    short *mOutputTemp ;
    uint32_t mOutputTempSize ;
    bool ResetFlag;
};

// ----------------------------------------------------------------------------
}; // namespace android

#endif /*ANDROID_AUDIO_RESAMPLER_MTK_H*/

