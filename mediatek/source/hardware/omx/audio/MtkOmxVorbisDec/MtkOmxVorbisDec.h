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

#ifndef MTK_OMX_VORBIS_DEC
#define MTK_OMX_VORBIS_DEC

#include "MtkOmxAudioDecBase.h"

#ifdef USE_MTK_DECODER
extern "C" {
    #include <vs_decoder_exp.h>
}
#ifdef VORBIS_MULTI_CH_SUPPORT
#include "MtkOmxAudUtil.h"
#endif
#else
struct vorbis_dsp_state;
struct vorbis_info;
extern "C" {
    #include <Tremolo/codec_internal.h>
    int _vorbis_unpack_books(vorbis_info *vi,oggpack_buffer *opb);
    int _vorbis_unpack_info(vorbis_info *vi,oggpack_buffer *opb);
    int _vorbis_unpack_comment(vorbis_comment *vc,oggpack_buffer *opb);
}
#endif

#define MTK_OMX_INPUT_BUFFER_SIZE_VORBIS 8192*2*2
#define MTK_OMX_OUTPUT_BUFFER_SIZE_VORBIS 8192*2*2

#define MTK_OMX_NUMBER_INPUT_BUFFER_VORBIS  5
#define MTK_OMX_NUMBER_OUTPUT_BUFFER_VORBIS  4

class MtkOmxVorbisDec : public MtkOmxAudioDecBase {
public:
    MtkOmxVorbisDec();
    ~MtkOmxVorbisDec();

    // override base class functions:
    virtual OMX_BOOL InitAudioParams();
    virtual void DecodeAudio(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
    virtual void FlushAudioDecoder();
    virtual void DeinitAudioDecoder();
    
private:   
    enum {
       kMaxNumSamplesPerBuffer = 8192 * 2
    };
#ifdef USE_MTK_DECODER
    int ch, srate, out_ch;
    vs_decoder_handle h_vs; 
    unsigned char *p_internal_mem, *p_dsp_mem, *p_setup_mem;     
#ifdef VORBIS_MULTI_CH_SUPPORT
    unsigned char *p_pcm_mem;     
    OMX_AUDIO_DOWNMIX_PARM *m_pDownmixParm;
#endif
    OMX_BOOL InitVorbisDecoder(OMX_U8 headertype,OMX_U8* pBitStreamBuf,OMX_U32 nBitStreamSize); 
#else
    vorbis_dsp_state *mVState;
    vorbis_info *mVi;      
    OMX_BOOL InitVorbisDecoder(OMX_U8 headertype,oggpack_buffer* opb);
#endif
    
  	OMX_U32  decodeVorbisPacket(OMX_U8* pInbuf,OMX_U32 nBitstreamLen,OMX_U8* pOutbuf);
  	OMX_BOOL mDecoderInitCompleteFlag;
    OMX_BOOL mFlush; 
    OMX_U32  ulNumTotalSamplesOut;
    OMX_TICKS inPutTimeStamp;
};

#endif  //MTK_OMX_VORBIS_DEC
