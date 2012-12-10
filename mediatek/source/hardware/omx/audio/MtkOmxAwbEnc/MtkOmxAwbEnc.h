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

#ifndef MTK_OMX_AWB_ENC
#define MTK_OMX_AWB_ENC

#include "MtkOmxAudioEncBase.h"

#include "awb_exp.h"

typedef struct {
    AWB_ENC_HANDLE* handle;
    AWB_BITRATE  bitrate;
    unsigned int intbuf_size;
    unsigned int tmpbuf_size;
    unsigned int bsbuf_size;
    unsigned int pcmbuf_size;
    void *intbuf;
    void *tmpbuf;
    int dtx;
} awbEncEngine;

typedef struct {
	OMX_U8*      buffer_base;
	OMX_U8*      write_ptr;
	OMX_U32      buffer_size;
	OMX_BOOL     buffer_partial;
	OMX_BOOL     buffer_full;
} awbEncTempBuffer;

// frame length is 20 milliseconds i.e. 20000 omx ticks/microseconds
#define MTK_OMX_AWB_FRAME_LENGTH_IN_TIMESTAMP 20000
#define MTK_OMX_MAX_AWB_FRAME_SIZE 64
#define MTK_OMX_MAX_NUM_OUTPUT_FRAMES_PER_BUFFER 10

#define MTK_OMX_INPUT_BUFFER_SIZE_AWB_ENC         (640 * MTK_OMX_MAX_NUM_OUTPUT_FRAMES_PER_BUFFER)  //6400
#define MTK_OMX_OUTPUT_BUFFER_SIZE_AWB_ENC        (MTK_OMX_MAX_NUM_OUTPUT_FRAMES_PER_BUFFER * MTK_OMX_MAX_AWB_FRAME_SIZE)  //10 * 64 = 640

//#define MTK_OMX_NUMBER_INPUT_BUFFER_AWB_ENC  5
//#define MTK_OMX_NUMBER_OUTPUT_BUFFER_AWB_ENC  2

#define MTK_OMX_NUMBER_INPUT_BUFFER_AWB_ENC  2
#define MTK_OMX_NUMBER_OUTPUT_BUFFER_AWB_ENC  1


class MtkOmxAwbEnc : public MtkOmxAudioEncBase {
public:
    MtkOmxAwbEnc();
    ~MtkOmxAwbEnc();

    // override base class functions:
    virtual OMX_BOOL InitAudioParams();
    virtual void EncodeAudio(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
    virtual void FlushAudioEncoder();
    virtual void DeinitAudioEncoder();
    
private:   
    
    bool InitAwbEncoder(OMX_U8* pInputBuffer); 
    OMX_ERRORTYPE CheckParams(OMX_PTR params);

    awbEncEngine *mAwbEnc;
    awbEncTempBuffer *mAwbTempBuf;

    OMX_BOOL mAwbEncInit;
    OMX_TICKS m_PrevTimeStamp;

//Dump File
    FILE *mpfile;
    int mdumpflag;

    
};


#endif  //MTK_OMX_AWB_ENC
