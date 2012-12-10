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


#include "AudioResampler.h"
#include "AudioResamplermtk.h"

//#define LOG_NDEBUG 0
#include <cutils/log.h>
#include <utils/threads.h>
#include <stdint.h>
#include <sys/types.h>
#include <stdlib.h>
#include <unistd.h>

#define LOG_TAG "AudioResamplerMTK"


#ifdef ENABLE_LOG_REAMPLERMTK
    #define LOG_REAMPLERMTK LOGD
#else
    #define LOG_REAMPLERMTK LOGV
#endif


namespace android {
// ----------------------------------------------------------------------------

static inline
int32_t mulAdd32(uint32_t left ,uint32_t in, uint32_t v, uint32_t a)
{
    if(left){
//modify by chipeng , use intrinsic function.
#if 1
    int32_t out;
    asm  ( "smlabt %[out], %[in], %[v], %[a] \n"
         : [out]"=r"(out)
         : [in]"%r"(in), [v]"r"(v), [a]"r"(a)
         : );
    return out;
#else
    return a + (in)* (v>>16);
#endif
    	}
	else{
#if 1
    int32_t out;
    asm ( "smlabb %[out], %[in], %[v], %[a] \n"
         : [out]"=r"(out)
         : [in]"%r"(in), [v]"r"(v), [a]"r"(a)
         : );
    return out;
#else
    return a + (in)* (v&0xffff);
#endif
	}
}



AudioResamplerMtk::AudioResamplerMtk(int bitDepth,
        int inChannelCount, int32_t sampleRate)
    : AudioResampler(bitDepth, inChannelCount, sampleRate)
{
    mWorkBufSize = 0;
    mWorkBuf = NULL;
    pSrcHdl = NULL;
    mInputIndex = 0;
    mOutputTemp = NULL;
    mOutputTempSize = 0;
    ResetFlag = false;
}

AudioResamplerMtk::~AudioResamplerMtk()
{
    LOG_REAMPLERMTK("~~AudioResamplerMtk close blisrc handle");
    BLI_Close(pSrcHdl);
    delete[] mWorkBuf;
    if(mOutputTemp){
        delete[] mOutputTemp;
        mOutputTemp = NULL;
    }
    pSrcHdl = NULL;
    #ifdef DUMP_AP_STREAMIN
    if(pinputFile){
        fclose(pinputFile);
        pinputFile = NULL;
    }
    if(presampleFile){
        fclose(presampleFile);
        presampleFile = NULL;
    }
    #endif

}

void AudioResamplerMtk::reset()
{
    LOGD("AudioResamplerMtk resetResampler pSrcHdl = %p",pSrcHdl);
    if(pSrcHdl){
        BLI_Close(pSrcHdl);
        pSrcHdl = NULL;
    }
    LOGD("BLI_Close(pSrcHdl)");
    if(mWorkBuf){
        delete[] mWorkBuf;
        mWorkBuf = NULL;
    }
    LOGD("delete[] mWorkBuf;");
    if(mOutputTemp){
        delete[] mOutputTemp;
        mOutputTemp = NULL;
    }
    LOGD(" delete[] mOutputTemp;");
    mWorkBufSize = 0;
    pSrcHdl = NULL;
    mInputIndex = 0;
    mOutputTempSize = 0;
    ResetFlag = false;
    return;
}

void AudioResamplerMtk::init()
{

}

void AudioResamplerMtk::resample(int32_t* out, size_t outFrameCount,
        AudioBufferProvider* provider) {
    if(pSrcHdl == NULL){
        BLI_GetMemSize(mInSampleRate, mChannelCount, mSampleRate, 2, &mWorkBufSize);
        LOG_REAMPLERMTK("mInSampleRate = %d mChannelCount = %d mSampleRate = %d mWorkBufSize = %d",mInSampleRate, mChannelCount,mSampleRate,mWorkBufSize);
        mWorkBuf = new char[mWorkBufSize];
        memset((void*)mWorkBuf,0,mWorkBufSize);
        pSrcHdl = BLI_Open(mInSampleRate, mChannelCount, mSampleRate, 2,mWorkBuf);
	if(!pSrcHdl)
		LOGE("BLI_Open(%d,%d,%d,2,...) fail!!!",mInSampleRate,mChannelCount,mSampleRate);

	if(mOutputTempSize < outFrameCount << 2){
	    if(mOutputTemp != NULL){
	        delete[] mOutputTemp;
	        mOutputTemp = NULL;
	    }
	    mOutputTempSize = outFrameCount << 2;
	    mOutputTemp = new short[mOutputTempSize>>1];
	    memset((void*)mOutputTemp,0,(mOutputTempSize>>1));
	         LOG_REAMPLERMTK("allocate temp ouput size = %d",mOutputTempSize>>1);
	    if(!mOutputTemp)
	        LOGE("allocate temp working buf error");
	}
    }

    size_t inputIndex = mInputIndex;
    size_t outputIndex = 0;
    size_t outputSampleCount = outFrameCount * 2;
    size_t inFrameCount = ((outFrameCount*mInSampleRate)+4)/mSampleRate;
    unsigned int inputLength =0;
    unsigned int outputLength = 0;
    LOG_REAMPLERMTK("starting resample %d frames, inputIndex=%d inputLength = %d outputLength = %d\n",
          outFrameCount, inputIndex,inputLength,outputLength);

    // still have data to ouput
    while(outFrameCount){
        if(mBuffer.frameCount == 0) {
            mBuffer.frameCount = inFrameCount;
	   if(inputLength == 0){
                provider->getNextBuffer(&mBuffer);  // get bufferNumber
	   }
            if (mBuffer.raw == NULL) {
                goto Mtkresample_exit;
            }
        }
        int16_t *in = mBuffer.i16;
        inputLength = mBuffer.frameCount * mChannelCount * (mBitDepth>>3);
        outputLength = outFrameCount <<2 ;
        int Consume =0;

        if(pSrcHdl)
        {
            LOG_REAMPLERMTK("BLI_Convert mBuffer.frameCount, %d , outFrameCount=%d inputLength = %d outputLength = %d\n",
                mBuffer.frameCount, outFrameCount,inputLength,outputLength);

            Consume = BLI_Convert(pSrcHdl, (short *) in, &inputLength,  (short*)mOutputTemp + outputIndex, &outputLength);

            LOG_REAMPLERMTK("BLI_Convert finish mBuffer.frameCount, %d , Consume=%d inputLength = %d outputLength = %d\n",
                mBuffer.frameCount, Consume,inputLength,outputLength);
        }
        else
        {
            //LOGE("Can not Resampler, pad 0");
            Consume = inputLength;
            memset((void*)(mOutputTemp + outputIndex),0,outputLength);
        }

        outFrameCount -= outputLength >>2;
        outputIndex += outputLength >>1;
        mBuffer.frameCount = Consume>>mChannelCount;
        provider->releaseBuffer(&mBuffer);// release buffer
    }

Mtkresample_exit:
    int16_t VolumeR,VolumeL;
    //outputIndex =0;
    inputIndex =0;
    VolumeR = mVolume[0];
    VolumeL = mVolume[1];
    while(inputIndex < outputIndex){
        out[inputIndex] = out[inputIndex] + mOutputTemp[inputIndex] * VolumeR;
        inputIndex++;
        out[inputIndex] = out[inputIndex] + mOutputTemp[inputIndex] * VolumeL;
        inputIndex++;
    }
    mInputIndex = 0;

}


// ----------------------------------------------------------------------------
}
; // namespace android

