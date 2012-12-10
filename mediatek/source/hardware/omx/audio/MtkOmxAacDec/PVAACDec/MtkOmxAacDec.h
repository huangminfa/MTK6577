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

#ifndef MTK_OMX_AAC_DEC
#define MTK_OMX_AAC_DEC

#include "MtkOmxAudioDecBase.h"
#include "MtkAacDecFrame.h"
#include "pvmp4audiodecoder_api.h"

#define MTK_ADIF_READ_SIZE 1536
#define MTK_OMX_INPUT_BUFFER_SIZE_AAC 4096
#define MTK_OMX_OUTPUT_BUFFER_SIZE_AAC 8192
#define MTK_OMX_NUMBER_INPUT_BUFFER_AAC  10
#define MTK_OMX_NUMBER_OUTPUT_BUFFER_AAC 9


class MtkOmxAacDec : public MtkOmxAudioDecBase {
public:
    MtkOmxAacDec();
    ~MtkOmxAacDec();

    OMX_BOOL  OmxAacDecInit(); //decinit
    void  QueueInputBuffer(int index);//copy mtkomxvdec queue the bufffer in front of the queue
    void  QueueOutputBuffer(int index);// copy the bufffer in front of the queue
	
    // override base class functions:
    virtual OMX_BOOL InitAudioParams();
    virtual void DecodeAudio(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
    virtual void FlushAudioDecoder();
    virtual void DeinitAudioDecoder();
	
private:
	void AACDump(OMX_BUFFERHEADERTYPE* pBuf);
	// AAC related

	MtkOmxAacDecoder* pAacDec; //aacdecoder handle
	OMX_BOOL mAacInitFlag; //Init flag
	OMX_S32 mFrameCount;//frame num

	OMX_U32 mInputCurrLength;// input current length
	OMX_U32 mSamplesPerFrame; // sample per frame
	OMX_U8* pFrameDecodeBuffer; //input buffer
	OMX_BOOL	mNewOutBufRequired; //required 
	OMX_BOOL	mNewInBufferRequired;
	OMX_U32 mOutputFrameLength;//output buffer length

	//temp buffer for input
	OMX_U8*	pTempInputBuffer;
	OMX_U32	mTempInputBufferLength;
	OMX_U32	mTempBufferSize;

	OMX_BOOL   mRemainBuffer;//Remain Buffer flag
	OMX_TICKS  mLastTimeStamp;//last buffer 's timestamp
	int mResetFlag;//ramp up	
};
#endif  //MTK_OMX_AAC_DEC

