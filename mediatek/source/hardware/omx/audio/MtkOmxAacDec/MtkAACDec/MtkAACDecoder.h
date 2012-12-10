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

#ifndef _MTK_AAC_DECODER_H_

#include "heaacdec_exp.h"
#include "osal_utils.h" 
#include "OMX_Core.h"
#include "OMX_Audio.h"



typedef enum AACDecoderErrorCode
{
	MP4AUDEC_SUCCESS		   =  0,
	MP4AUDEC_INVALID_FRAME	   = 10,
	MP4AUDEC_INCOMPLETE_FRAME  = 20,
	MP4AUDEC_LOST_FRAME_SYNC   = 30 	//Cannot happen since no ADTS 
} tAACDecoderErrorCode;

typedef enum eAudioObjectType
{
	MP4AUDIO_NULL			 =	0, /*									*/
	MP4AUDIO_AAC_MAIN		 =	1, /*									*/
	MP4AUDIO_AAC_LC 		 =	2, /* LC = Low Complexity					*/
	MP4AUDIO_AAC_SSR		 =	3, /* SSR = Scalable Sampling Rate			*/
	MP4AUDIO_LTP			 =	4, /* LTP = Long Term Prediction			*/
	MP4AUDIO_SBR			 =	5, /* SBR = Spectral Band Replication		*/
	MP4AUDIO_AAC_SCALABLE	 =	6, /* scales both bitrate and sampling rate */
	MP4AUDIO_TWINVQ 		 =	7, /* low bit rate							*/
	MP4AUDIO_CELP			 =	8,
	MP4AUDIO_HVXC			 =	9,
	/* 10 is reserved						 */
	/* 11 is reserved						 */
	MP4AUDIO_TTSI			 = 12,
	/* 13-16 are synthesis and MIDI types	 */
	MP4AUDIO_ER_AAC_LC		 = 17, /*										*/
	/* 18 is reserved						 */
	MP4AUDIO_ER_AAC_LTP 	 = 19, /*										*/
	MP4AUDIO_ER_AAC_SCALABLE = 20, /*										*/
	MP4AUDIO_ER_TWINVQ		 = 21, /*										*/
	MP4AUDIO_ER_BSAC		 = 22, /*										*/
	MP4AUDIO_ER_AAC_LD		 = 23, /*										*/
	MP4AUDIO_ER_CELP		 = 24, /*										*/
	MP4AUDIO_ER_HVXC		 = 25, /*										*/
	MP4AUDIO_ER_HILN		 = 26, /*										*/
	MP4AUDIO_PARAMETRIC 	 = 27, /*										*/
	MP4AUDIO_PS 			 = 29  /*  Explicit Parametric Stereo			*/

} tAudioObjectType;

enum {
	OMXMSG 			= 0x01,
	RECONFIG		= 0x02,
};


typedef struct {

	HEAACDEC_HANDLE *pHEAACDecHdl; 
	int InterBufSize; 
	int TmpBufSize;
	int PcmBufSize;
	int BsBufSize;
	void *pInterBuf;
    void *pTmpBuf;
    void *pPcmBuf;
	void *pDownmixParm;
	
} MtkAACDecEngine;


#define AACDEC_PCM_FRAME_SAMPLE_SIZE 1024 // 1024 samples 


class MtkAACDecoder{

public:
	
	virtual ~MtkAACDecoder();
		
	virtual OMX_BOOL AACDecoderInit();
	virtual void AACDecoderDeinit();    
    virtual int GetAACDecoderSWIPVersion();
	virtual void AACDecoderReset();
	virtual int MtkAACDecodeConfig(OMX_U8* pConfigBuf);
	virtual int AACDecodeFrames(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf,OMX_S32 *notifyFlags);
	virtual OMX_BOOL ReceiveEOS(OMX_BUFFERHEADERTYPE* pOutputBuf,OMX_S32 *notifyFlags){return OMX_FALSE;}
	virtual void setAACStreamType(OMX_AUDIO_AACSTREAMFORMATTYPE streamtype);
	virtual OMX_AUDIO_AACSTREAMFORMATTYPE getAACStreamType();

public:
	void SetNumSampleOut(OMX_TICKS TimeStamp);
	OMX_BOOL GetAudioParam(OMX_AUDIO_PARAM_PCMMODETYPE *AudioPcmParam,OMX_AUDIO_PARAM_AACPROFILETYPE *AudioAacParam);
	void ProcessMutiChannel(OMX_BUFFERHEADERTYPE* pOutputBuf);
	int GetFrameCount(){return mFrameNum;}
	OMX_TICKS GetCurrentTime();
		

protected:
	MtkAACDecoder();
	void RampUp(OMX_S16* RampBuff,OMX_U32 HalfRampSample);
	OMX_U32 RetrieveDecodedStreamType(MtkAACDecEngine *pAACDec);
	OMX_AUDIO_AACPROFILETYPE MtkRetrieveDecodedStreamType(OMX_U32 profile);
	void AACDecoderErrDeclare(int status);
	int Downmix(OMX_U8* OutputBuffer);
	void DecPCMDump(void* input,HEAACDEC_HANDLE *pHEAACDecHdl);
    void DecInputDump(void *pBsBuf, int BsBufLen);
    void DecInputLog(void *pBsBuf);
	virtual OMX_BOOL Init(){return OMX_TRUE;}	

protected:
	MtkAACDecEngine *mAACDec;
	OMX_AUDIO_AACPROFILETYPE mProfile;
	OMX_S32 	mFrameNum;
	OMX_S32		mSamplingRate; 
    OMX_U32 	mNumOfChannels; 
	OMX_S32 	mOutputSampleLen;//decoded frame pcm sample
	OMX_S32     mSamplePerFrame;//sample per frame              
	OMX_S64 	mNumSamplesOutput;//used calcualte timestamp
	OMX_BOOL    mReset;
	OMX_BOOL	mNoFinishedConfig;
	OMX_S64     mDecNum;
private:

	int Get_sample_rate(const OMX_U32 sf_index);
	

private:
	

	OMX_BOOL 	mAACInitFlag;


	OMX_U32     m_LastAudioObjectType;
	OMX_U32   	m_LastSamplingFreqIndex;
	OMX_U8   	m_LastChannelNum;	
	
		
	OMX_U8*		pTempOutBufferForPortReconfig;
	OMX_U32 	mSizeOutBufferForPortReconfig;
	OMX_TICKS	mTimestampOutBufferForPortReconfig;

	OMX_AUDIO_AACSTREAMFORMATTYPE     mAACStreamFormat;

};


class AdtsAACDecoder :public MtkAACDecoder{

public:
	AdtsAACDecoder();
	virtual ~AdtsAACDecoder();
	int AACDecodeFrames(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf,OMX_S32 *notifyFlags);

	
protected:

	OMX_BOOL ReceiveEOS(OMX_BUFFERHEADERTYPE* pOutputBuf,OMX_S32 *notifyFlags);
	void AACDump();
private:

};


class AdifAACDecoder:public MtkAACDecoder{

public:
	AdifAACDecoder();
	virtual ~AdifAACDecoder();
	int AACDecodeFrames(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf,OMX_S32 *notifyFlags);

protected:
	OMX_BOOL Init();
	OMX_BOOL ReceiveEOS(OMX_BUFFERHEADERTYPE* pOutputBuf,OMX_S32 *notifyFlags);
	void AACDecoderReset();

private:

	void* m_pBsRead;
	void* m_pBsWrite;
	void* m_pBsBuf;
	void* m_pEndBsBuf;

};



class Factory
{
public:
	Factory(){};
	virtual ~Factory(){}
	MtkAACDecoder* CreateProduct(OMX_AUDIO_AACSTREAMFORMATTYPE streamtype);
public:
	//static Factory* CreateInstance();	
protected:
	//Factory(){};
private:
	//static Factory* _instance;

};


#endif // ~ _MTK_AAC_DECODER_H_
