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

#ifndef MTK_AAC_DEC_FRAME
#define MTK_AAC_DEC_FRAME


#include "MtkOmxAudioDecBase.h"

#include "pvmp4audiodecoder_api.h"

#define AACDEC_PCM_FRAME_SAMPLE_SIZE 1024 // 1024 samples 


class MtkOmxAacDecoder
{  

	public: 
	  	MtkOmxAacDecoder();
		~MtkOmxAacDecoder();
		OMX_BOOL MtkAacDecInit(OMX_U32  DesiredChannels);
		void MtkAacDecDeinit();
		void MtkResetDecoder(); // for repositioning
		int MtkAacDecodeFrames(OMX_S16* OutputBuffer,OMX_U32* OutputLength, OMX_U8** InBuffer,
			                        OMX_U32* InBufSize, OMX_S32* IsFirstBuffer,
		                           OMX_AUDIO_PARAM_PCMMODETYPE* AudioPcmParam,
		                           OMX_AUDIO_PARAM_AACPROFILETYPE* AudioAacParam,
		                           OMX_U32* SamplesPerFrame, OMX_BOOL* ResizeFlag,OMX_BOOL* RemainFlag);
		   
        void MtkUpdateAACPlusEnabled(OMX_BOOL flag);
		int AACDecodeConfig(OMX_BUFFERHEADERTYPE* pInputBuf,OMX_AUDIO_PARAM_AACPROFILETYPE* AudioAacParam,OMX_AUDIO_PARAM_PCMMODETYPE* AudioPcmParam);
		OMX_TICKS MtkGetCurrentTime();
		void SetNumSampleOut(OMX_TICKS TimeStamp);//convert timestamp into sample numbers
		void GetSampleRate(OMX_U32 *samplerate);
		void GetChannels(OMX_U32 *channel);
		OMX_BOOL GetReconfigFlag();
	public:
		
		OMX_AUDIO_AACSTREAMFORMATTYPE mAACStreamFormat;

	private:
        OMX_AUDIO_AACPROFILETYPE MtkRetrieveDecodedStreamType(OMX_U32 profile);

		void RampUp(OMX_S16* RampBuff,OMX_U32 HalfRampSample,OMX_U32 ChanMode);  //ramp up for heaac
		
		OMX_S32 mAacInitFlag;
		OMX_S32 mInputUsedLength;//current input buffer's size and update it		
		OMX_U32  	mMemReq;//decoder need buffer's size
		void*    	pMem;//decoder need buffer pointer
		tPVMP4AudioDecoderExternal *mConfig;//aac deocder handle
		OMX_U32 	mNumOfChannels; 
		OMX_S32 	mConfigUpSamplingFactor;//pure aac or heaac
		int32_t 	mSamplingRate; 
		OMX_AUDIO_AACPROFILETYPE     mProfile;
		OMX_U32     mLastBufferRemainLength;
		OMX_S64     mNumSamplesOutput;
		OMX_BOOL    mReconfig;//record reconfig status for after reconifg 		
	    int 		mResetFlag;//for ramp up calcuate times
	    
};





#endif  //MTK_OMX_AAC_DEC
