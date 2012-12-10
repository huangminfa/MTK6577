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

#ifndef MTK_OMX_FLAC_DEC
#define MTK_OMX_FLAC_DEC

#include "MtkOmxAudioDecBase.h"
// libFLAC parser
#include "FLAC/stream_decoder.h"
#include "libFLAC/include/protected/stream_decoder.h"

//#include "stream_decoder.h"
//#include "format.h"
///#include "flac_decoder_exp.h"

#ifdef FLAC_MULTI_CH_SUPPORT
#include  "MtkOmxAudUtil.h"
#endif

#define FLAC__BYTES_PER_WORD 4
#define FLAC__BITS_PER_WORD 32
static const unsigned FLAC__BITREADER_DEFAULT_CAPACITY = 65536u / FLAC__BITS_PER_WORD; /* in words */

#define MTK_OMX_FLAC_INPUT_BUFFER_COUNT  4
#define MTK_OMX_FLAC_INPUT_BUFFER_MIN_COUNT 4
#define MTK_OMX_FLAC_OUTPUT_BUFFER_COUNT  9
#define MTK_OMX_FLAC_OUTPUT_BUFFER_MIN_COUNT 1

#define MTK_OMX_FLAC_DEFAULT_OUTPUT_SAMPLE_RATE   44100
#define MTK_OMX_FLAC_DEFAULT_OUTPUT_BITWIDTH      16
#define MTK_OMX_FLAC_DEFAULT_OUTPUT_CHANNEL       2

//Byte-based
//#define MTK_OMX_FLAC_INPUT_BUFFER_SIZE   1024 
#define MTK_OMX_FLAC_INPUT_BUFFER_SIZE   FLAC__BITREADER_DEFAULT_CAPACITY*FLAC__BYTES_PER_WORD 
#define MTK_OMX_FLAC_OUTPUT_BUFFER_SIZE  FLAC__MAX_BLOCK_SIZE * FLAC__MAX_CHANNELS * sizeof(FLAC__int32)
#define MTK_OMX_FLAC_MIX_TEMP_BUFFER_SIZE   4608*FLAC__MAX_CHANNELS * sizeof(FLAC__int32)
#define MTK_OMX_FLAC_BUFFERING_SIZE   8192*4
#define OMX_MIMETYPE_FLAC "audio/flac"

typedef struct tMTKFLACDecoderExternal{

    OMX_U8      *pInputBuffer;
    OMX_U32     InBitStreamSize;
    int         inputBufferUsedLength;
    int         outputFrameSize;
    OMX_U8      *pOutputBuffer;
	
}mtkFLACDecoderExternal;


class MtkOmxFlacDec : public MtkOmxAudioDecBase {
public:
    MtkOmxFlacDec();
    ~MtkOmxFlacDec();

    OMX_BOOL  OmxFlacDecInit(); //decinit
    void  QueueInputBuffer(int index);//copy mtkomxvdec queue the bufffer in front of the queue
    void  QueueOutputBuffer(int index);// copy the bufffer in front of the queue
	
    // override base class functions:
    virtual OMX_BOOL InitAudioParams();

    virtual void DecodeAudio(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
    virtual void FlushAudioDecoder();
    virtual void DeinitAudioDecoder();
	
private:
    void InitPortFormat();
    OMX_BOOL AllocateBufferHeader();   
    void RampUp(int16_t * aRampBuff,uint32_t aHalfRampSample);//         
    void FLACDump(OMX_BUFFERHEADERTYPE* pBuf);
    void (*mCopy)(OMX_S16 *dst, const int *const *src, unsigned nSamples);	
    void SetFlacDecStreamInfo();	   
    OMX_BOOL FlacDecinit();
    void  HandleFormatChange(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);	   
    OMX_U32  DecodeOneFrame();       

   // FLAC Decoder related    
    // FLAC parser callbacks as C++ instance methods
    FLAC__StreamDecoderReadStatus readCallback(
            FLAC__byte buffer[], size_t *bytes);
    FLAC__StreamDecoderWriteStatus writeCallback(
            const FLAC__Frame *frame, const FLAC__int32 * const buffer[]);
    void errorCallback(FLAC__StreamDecoderErrorStatus status);

    // FLAC parser callbacks as C-callable functions
    static FLAC__StreamDecoderReadStatus read_callback(
            const FLAC__StreamDecoder *decoder,
            FLAC__byte buffer[], size_t *bytes,
            void *client_data);
    static FLAC__StreamDecoderWriteStatus write_callback(
            const FLAC__StreamDecoder *decoder,
            const FLAC__Frame *frame, const FLAC__int32 * const buffer[],
            void *client_data);
    static void error_callback(
            const FLAC__StreamDecoder *decoder,
            FLAC__StreamDecoderErrorStatus status,
            void *client_data);
    
    OMX_BOOL mFlacInitFlag; //Init flag
    OMX_S32 mFrameCount;//frame num

    off64_t mCurrentPos;
	OMX_S32 mRemindedData; 
	OMX_S32 mBufferReadOffset;
     OMX_BOOL  mEOF;
    // cached when the STREAMINFO metadata is parsed by libFLAC
    FLAC__StreamMetadata_StreamInfo mStreamInfo;
    bool mStreamInfoValid;

    // cached when a decoded PCM block is "written" by libFLAC parser
    bool mWriteRequested;
    bool mWriteCompleted;
    FLAC__FrameHeader mWriteHeader;
    const FLAC__int32 * const *mWriteBuffer;

    // most recent error reported by libFLAC parser
    FLAC__StreamDecoderErrorStatus mErrorStatus;
    
    // handle to underlying libFLAC parser
    FLAC__StreamDecoder *mDecoder;
    size_t mMaxBufferSize;
	//temp buffer for input
    ///void *pTempInputBuf;
    //remained buffer
    OMX_U32 mRemainingDataSize;
    OMX_U8* mpRemainderBuffer;
 
    const FLAC__int32 * const *  pTempOutBuff;
    bool pTempBuffEnabled;
    bool bTempBuffFlag;  /// to indicate need copy buffer first after pTempBuffEnabled is enabled.
    OMX_BOOL fgFlush; 
    OMX_BOOL mSeekEnable;

    void *pWorking_BUF;

    mtkFLACDecoderExternal mFlacConfig;

#ifdef FLAC_MULTI_CH_SUPPORT
OMX_AUDIO_DOWNMIX_PARM *m_pDownmixParm;
OMX_U8  *pDownMixTempBuf ;
#endif

};
#endif  //MTK_OMX_FLAC_DEC

