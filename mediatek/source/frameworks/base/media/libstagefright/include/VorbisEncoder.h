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

#ifndef VORBIS_ENCODER_H_

#define VORBIS_ENCODER_H_

//#define DUMP_PCM

#include <media/stagefright/MediaSource.h>
#include <media/stagefright/MetaData.h>
#include "minvorbis_encoder_exp.h"

namespace android {

struct MediaBufferGroup;

struct VorbisEncoder : public MediaSource {
    VorbisEncoder(const sp<MediaSource> &source, const sp<MetaData> &meta);

    virtual status_t start(MetaData *params);
    virtual status_t stop();

    virtual sp<MetaData> getFormat();

    virtual status_t read(
            MediaBuffer **buffer, const ReadOptions *options);
    
   	int m_bitrate;
   	int m_samplerate;
	int m_channelCount;

	int sz_shared, sz_encoder, sz_parser, sz_pcm_in, sz_bs_out, sz_rt_tab;
        void *p_pcm_in, *p_bs_out, *p_shared, *p_encoder, *p_parser, *p_rt_tab;
   	minvorbis_enc_handle h_minvorbis;


protected:
    virtual ~VorbisEncoder();

private:
//refer to minvorbis_encoder_exp.h
//default quality = 1, 2 high quality
    int fn_GetBitRate(int pSampleRate, int pQuality = 1);
    sp<MediaSource> mSource;
    sp<MetaData>    mMeta;
    bool mStarted;
    bool mIsStop;

    MediaBufferGroup *mBufferGroup;

    void *mEncState;
    void *mSidState;
    int64_t mAnchorTimeUs;
    int64_t mNumFramesOutput;
	
    int32_t           mFrameCount;
    MediaBuffer *mInputBuffer;
    int mMode;

    int32_t mNumInputSamples;

    enum {
            kNumSamplesPerFrame = 1024,
        };	

    VorbisEncoder (const VorbisEncoder  &);
    VorbisEncoder &operator=(const VorbisEncoder &);
    int mHeadSize;
#ifdef DUMP_PCM
    FILE *mpfile;
#endif

};

}  // namespace android

#endif  // OGG_ENCODER_H_

