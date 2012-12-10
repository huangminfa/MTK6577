/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

//#define LOG_NDEBUG 0
#define LOG_TAG "VorbisEncoder"

#include <media/stagefright/MediaBufferGroup.h>
#include <media/stagefright/MediaDebug.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MediaErrors.h>
#include <media/stagefright/MetaData.h>

#include "VorbisEncoder.h"

#define GUARD_BYTES 512

namespace android {

VorbisEncoder::VorbisEncoder(const sp<MediaSource> &source, const sp<MetaData> &meta)
    : mSource(source),
      mMeta(meta),
      mStarted(false),
      mBufferGroup(NULL),
      mAnchorTimeUs(0),
      mNumFramesOutput(0),
      mInputBuffer(NULL),
      mNumInputSamples(0)
#ifdef DUMP_PCM
     ,mpfile(NULL)
#endif
{

    meta->findInt32(kKeyBitRate, &m_bitrate);
    meta->findInt32(kKeySampleRate, &m_samplerate);
    meta->findInt32(kKeyChannelCount, &m_channelCount);
	
LOGV("VorbisEncoder::VorbisEncoder,m_bitrate=%d,m_samplerate=%d,m_channelCount=%d",m_bitrate,m_samplerate,m_channelCount);

    sz_shared = sz_encoder = sz_parser = sz_pcm_in = sz_bs_out = sz_rt_tab = 0;
    p_pcm_in = p_bs_out = p_shared = p_encoder = p_parser = p_rt_tab = NULL;
    h_minvorbis = 0; 
    mHeadSize = 0;
    mIsStop = false;
}

VorbisEncoder::~VorbisEncoder() {
    if (mStarted) {
        stop();
    }
}

status_t VorbisEncoder::start(MetaData *params) {
    int ret;
	
    if (mStarted) {
        LOGW("Call start() when encoder already started");
        return OK;
    }

    minvorbis_get_mem_size_for_encoding(&sz_rt_tab,
                                        &sz_shared,
                                        &sz_encoder,
                                        &sz_parser,
                                        &sz_pcm_in,
                                        &sz_bs_out,
                                        0);
LOGD("VorbisEncoder::start:minvorbis_get_mem_size_for_encoding,sz_rt_tab=%d,sz_shared=%d,sz_encoder=%d,sz_parser=%d,sz_pcm_in=%d,sz_bs_out=%d",
sz_rt_tab,sz_shared,sz_encoder,sz_parser,sz_pcm_in,sz_bs_out);

    p_shared        = malloc(sz_shared  + 2*GUARD_BYTES);
    p_encoder       = malloc(sz_encoder + 2*GUARD_BYTES);
    p_parser        = malloc(sz_parser  + 2*GUARD_BYTES);
    p_pcm_in        = malloc(sz_pcm_in  + 2*GUARD_BYTES);
    p_bs_out        = malloc(sz_bs_out  + 2*GUARD_BYTES);
    p_rt_tab        = malloc(sz_rt_tab  + 2*GUARD_BYTES);

    memset(p_shared, 0xff, sz_shared    + 2*GUARD_BYTES);
    memset(p_encoder,0xff, sz_encoder   + 2*GUARD_BYTES);
    memset(p_parser, 0xff, sz_parser    + 2*GUARD_BYTES);
    memset(p_pcm_in, 0xff, sz_pcm_in    + 2*GUARD_BYTES);
    memset(p_bs_out, 0xff, sz_bs_out    + 2*GUARD_BYTES);
    memset(p_rt_tab, 0xff, sz_rt_tab    + 2*GUARD_BYTES);

    mBufferGroup = new MediaBufferGroup;
    mBufferGroup->add_buffer(new MediaBuffer(sz_bs_out  + 2*GUARD_BYTES));

    int pBitRate = fn_GetBitRate(m_samplerate);
	
    h_minvorbis = minvorbis_init_encoder(   (char*)p_rt_tab+GUARD_BYTES,
                                            (char*)p_shared+GUARD_BYTES,
                                            (char*)p_encoder+GUARD_BYTES,
                                            (char*)p_parser+GUARD_BYTES,
                                            m_channelCount,
                                            m_samplerate,
                                            pBitRate,
                                            (unsigned char*)p_bs_out+GUARD_BYTES,
                                            &ret,
                                            0);
	
LOGV("VorbisEncoder::start:minvorbis_init_encoder");
	
	
    CHECK(h_minvorbis != 0);	
    mFrameCount = -1;
    mHeadSize = ret;	

    mAnchorTimeUs = 0;
    mNumFramesOutput = 0;
    mStarted = true;
    mNumInputSamples = 0;
	
    mSource->start(params);

#ifdef DUMP_PCM
    mpfile = fopen("sdcard/ogg.pcm","wb");
#endif

    return OK;
}

status_t VorbisEncoder::stop() {
LOGD("VorbisEncoder::stop");	
    if (!mStarted) {
        LOGW("Call stop() when encoder has not started.");
        return OK;
    }

    if (mInputBuffer) {
        mInputBuffer->release();
        mInputBuffer = NULL;
    }

    delete mBufferGroup;
    mBufferGroup = NULL;

    mSource->stop();
	
    mEncState = mSidState = NULL;

    mStarted = false;
    mIsStop = false;
    free(p_shared);
    free(p_encoder);
    free(p_parser);
    free(p_pcm_in);
    free(p_bs_out);

#ifdef DUMP_PCM
    fclose(mpfile);
    mpfile = NULL;
#endif

    return OK;
}

sp<MetaData> VorbisEncoder::getFormat() {
    sp<MetaData> srcFormat = mSource->getFormat();

    mMeta->setCString(kKeyMIMEType, MEDIA_MIMETYPE_AUDIO_VORBIS);

    int64_t durationUs;
    if (srcFormat->findInt64(kKeyDuration, &durationUs)) {
        mMeta->setInt64(kKeyDuration, durationUs);
    }

    mMeta->setCString(kKeyDecoderComponent, "VorbisEncoder");

    return mMeta;
}

status_t VorbisEncoder::read(MediaBuffer **out, const ReadOptions *options) {
    LOGV("VorbisEncoder::read+");
    status_t err;
    //*out = NULL;

    if (mIsStop)
    {
        err = ERROR_END_OF_STREAM;
	LOGE("VorbisEncoder::read,ERROR_END_OF_STREAM");	
	return err;
    }
    int64_t seekTimeUs;
    ReadOptions::SeekMode mode;
    CHECK(options == NULL || !options->getSeekTo(&seekTimeUs, &mode));

    MediaBuffer *buffer = NULL;
    CHECK_EQ(mBufferGroup->acquire_buffer(&buffer), OK);
    uint8_t *outPtr = (uint8_t *)buffer->data();
    bool readFromSource = false;
    int64_t wallClockTimeUs = -1;

   if (mFrameCount == -1)
   {
       mFrameCount++;
       memcpy(outPtr, p_bs_out+GUARD_BYTES, mHeadSize);
       buffer->set_range(0, mHeadSize);
       buffer->meta_data()->setInt64(kKeyTime, -1);
       LOGV("VorbisEncoder::read:writehead");	   
        *out = buffer;
	return OK;
   }

    LOGV("VorbisEncoder::read:while+");	   
    while (mNumInputSamples < kNumSamplesPerFrame*m_channelCount) 
    {
        if (mInputBuffer == NULL) 
	{
            if (mSource->read(&mInputBuffer, options) != OK) 
	    {
                if (mNumInputSamples == 0) 
		{
                    buffer->release();
                    return ERROR_END_OF_STREAM;
                }
                mNumInputSamples = 0;
                break;
            }
            
            size_t align = mInputBuffer->range_length() % sizeof(int16_t);
            CHECK_EQ(align, 0);

            int64_t timeUs;
            if (mInputBuffer->meta_data()->findInt64(kKeyDriftTime, &timeUs)) 
	    {
                wallClockTimeUs = timeUs;
            }
            if (mInputBuffer->meta_data()->findInt64(kKeyAnchorTime, &timeUs)) 
	    {
                mAnchorTimeUs = timeUs;
            }
            readFromSource = true;
	    LOGV("VorbisEncoder::read:readFromSource,mNumberInputSamples=%d",mNumInputSamples);
        } 
	else 
        {
            readFromSource = false;
   	    LOGV("VorbisEncoder::read:not readFromSource,mNumberInputSamples=%d",mNumInputSamples);		
        }

        size_t copy = (kNumSamplesPerFrame*m_channelCount - mNumInputSamples) * sizeof(int16_t);
        LOGV("VorbisEncoder::read,copy=%d,mNumInputSamples=%d",copy,mNumInputSamples);

	if (copy > mInputBuffer->range_length()) 
	{
            copy = mInputBuffer->range_length();
        }
	
        memcpy((uint8_t *)p_pcm_in+GUARD_BYTES+mNumInputSamples*sizeof(int16_t),
               (const uint8_t *) mInputBuffer->data() + mInputBuffer->range_offset(),
               copy);

#ifdef DUMP_PCM
    fwrite(p_pcm_in+GUARD_BYTES+mNumInputSamples*sizeof(int16_t), 1, copy, mpfile);
#endif

        mInputBuffer->set_range(mInputBuffer->range_offset() + copy, mInputBuffer->range_length() - copy);

        if (mInputBuffer->range_length() == 0) 
	{
            mInputBuffer->release();
            mInputBuffer = NULL;
        }
        mNumInputSamples += copy / sizeof(int16_t);
		
        LOGV("VorbisEncoder::read:readFromSource,while mNumInputSamples=%d",mNumInputSamples);
		
        if (mNumInputSamples >= kNumSamplesPerFrame*m_channelCount) 
	{
            mNumInputSamples %= (kNumSamplesPerFrame*m_channelCount); 
            break;
        }
    }
    LOGV("VorbisEncoder::read:while-");
	
//*out == NULL:not stop, *out != NULL OggWriter::Stop, last frames finalize = 1
    int finalize = 0;
    LOGV("VorbisEncoder::read,*out=%p",*out);
    if (*out != NULL)
    {
        LOGD("VorbisEncoder::read,finalize=1,mIsStop=true");
	finalize = 1;	
	mIsStop = true;
    }
    int ret = minvorbis_encode_one_frame(h_minvorbis,
              (unsigned char*)p_pcm_in+GUARD_BYTES,
              (unsigned char*)p_bs_out+GUARD_BYTES,
              finalize,
              0);
	
    int64_t mediaTimeUs = ((mFrameCount - 1) * 1000000LL * kNumSamplesPerFrame) / m_samplerate;
    buffer->meta_data()->setInt64(kKeyTime, mAnchorTimeUs + mediaTimeUs);
    if (readFromSource && wallClockTimeUs != -1) 
    {
        buffer->meta_data()->setInt64(kKeyDriftTime, mediaTimeUs - wallClockTimeUs);
    }	
    //Accumulate finish, vorbis encode output
    if (ret > 0) 
    {
       	LOGV("VorbisEncoder::read:vorbis encode ret>%d",ret);
        memcpy(outPtr, p_bs_out+GUARD_BYTES, ret);		
        buffer->set_range(0, ret);
        ++mFrameCount;
        *out = buffer;
	return OK;
    }
    else if (ret < 0)
    {
        LOGE("minvorbis_encode_one_frame() error");
        return UNKNOWN_ERROR;
    }
    //Accumulating
    else
    {
    	LOGV("VorbisEncoder::read:vorbis encode ret==%d",ret);
        buffer->set_range(0, ret);
	++mFrameCount;
        *out = buffer;
	return OK;		
    }
}

/*
#define MINVORBIS_48K_NORMAL_QUALITY    2
#define MINVORBIS_44K_NORMAL_QUALITY    2
#define MINVORBIS_32K_HIGH_QUALITY      3
#define MINVORBIS_32K_NORMAL_QUALITY    5
#define MINVORBIS_16K_HIGH_QUALITY      3
#define MINVORBIS_16K_NORMAL_QUALITY    5
#define MINVORBIS_8K_NORMAL_QUALITY     5
*/
int VorbisEncoder::fn_GetBitRate(int pSampleRate, int pQuality)
{
    int rBitRate = 0;
	
    if (pSampleRate >= 44000)
        rBitRate = 2;
    else if (pSampleRate == 32000 || pSampleRate == 16000)
        rBitRate = (pQuality == 1 ? 3 : 5);
    else if (pSampleRate == 8000)
	rBitRate = 5;
    else
	LOGE("VorbisEncoder::GetBitRate error, samplerate=%d,Quality=%d", pSampleRate,pQuality);
	
    LOGV("VorbisEncoder::GetBitRate samplerate=%d,rBitRate=%d", pSampleRate,rBitRate);
    return rBitRate;
}
		
}  // namespace android

