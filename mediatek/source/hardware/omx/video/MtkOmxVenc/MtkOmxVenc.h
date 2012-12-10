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

#ifndef MTK_OMX_VENC
#define MTK_OMX_VENC

#include "MtkOmxBase.h"

#if defined(MT6575) || defined(MT6577)
#define USE_VIDEO_M4U
#endif

#define ANDROID_ICS 1

#ifdef USE_VIDEO_M4U
#define VIDEO_M4U_MAX_BUFFER 100
#include "m4u_lib.h"
#include "val_api.h"
#define MTK_OMX_VENC_USE_PMEM  0
#define MTK_OMX_VENC_USE_MDP   0
#define MTK_OMX_VENC_ALLOC_BS_PMEM 0
#else
#define MTK_OMX_VENC_USE_PMEM 1    // turn on when input is MTKYUV
#define MTK_OMX_VENC_USE_MDP   0   // turn on when input is YUV420
#define MTK_OMX_VENC_ALLOC_BS_PMEM 1 // Morris Yang 20110621 set to 0 if IL client provide output pmem (reduce one memcpy)
#endif

// Note:
// encode from file: 0 1 1
// encode from camera: 1 0 0


//#ifdef MT6573_MFV_HW
#ifdef USE_MTK_HW_VCODEC
#include "MediaHal.h"
#include "val_types.h"
#include "venc_drv_if.h"
#ifdef MT6573_MFV_HW
#include "venc_drv_h264.h"
#include "venc_drv_mpeg4.h"
#endif
#define MFV_OUTPUT_BUFFER_NUM 8
#define DEC_TRYALLOCMEMCOUNT 10
#define H264_MAX_FRAME_NUM  0xFFFF
#endif


// Morris Yang 20110610 [
#define MTK_VENC_DEFAULT_INPUT_BUFFER_COUNT 12  // for non-camera encode
//#define MTK_VENC_DEFAULT_INPUT_BUFFER_COUNT 1   // for camera encode
// ]
#define MTK_VENC_DEFAULT_OUTPUT_BUFFER_COUNT 8

#define MTK_VENC_DEFAULT_INPUT_BUFFER_SIZE                  38016
#define MTK_VENC_DEFAULT_OUTPUT_BUFFER_SIZE_AVC       200*1024   //69*1024  //38581
#define MTK_VENC_DEFAULT_OUTPUT_BUFFER_SIZE_MPEG4   1024*1024 //128*1024


#define MTK_OMXVENC_EXTENSION_INDEX_PARAM_SET_FORCE_I_FRAME "OMX.MTK.index.param.video.EncSetForceIframe"


typedef enum MTK_VENC_CODEC_ID
{
    MTK_VENC_CODEC_ID_AVC,
#if defined(MT6577)     //VCODEC_MULTI_THREAD  
    MTK_VENC_CODEC_ID_AVC_VGA,
#endif
    MTK_VENC_CODEC_ID_MPEG4,
#if defined(MT6577)     //VCODEC_MULTI_THREAD  
    MTK_VENC_CODEC_ID_MPEG4_1080P,
#endif
    MTK_VENC_CODEC_ID_MPEG4_SHORT,
    MTK_VENC_CODEC_ID_INVALID = 0xFFFFFFFF,
} MTK_VENC_CODEC_ID;


typedef struct  MTK_VENC_PROFILE_LEVEL_ENTRY {
    OMX_U32 profile;
    OMX_U32 level;
} MTK_VENC_PROFILE_LEVEL_ENTRY;


MTK_VENC_PROFILE_LEVEL_ENTRY AvcProfileLevelTable[] = 
{
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel1},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel1b},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel11},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel12},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel13},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel2},    // => MFV spec
#if 0
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel21},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel22},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel3},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel31},
#endif    
};


MTK_VENC_PROFILE_LEVEL_ENTRY H263ProfileLevelTable[] = 
{
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level10},  // => MFV spec
#if 0
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level20},
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level30},
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level40},
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level45},
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level50},
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level60},
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level70},
#endif    
};


MTK_VENC_PROFILE_LEVEL_ENTRY MPEG4ProfileLevelTable[] = 
{
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level0},
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level0b},
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level1},
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level2},
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level3},  // => MFV spec
#if 0
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level4},
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level4a},
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level5},
#endif    
};

#define MAX_AVC_PROFILE_LEVEL_TABLE_SZIE       sizeof(AvcProfileLevelTable)/sizeof(MTK_VENC_PROFILE_LEVEL_ENTRY)
#define MAX_H263_PROFILE_LEVEL_TABLE_SZIE     sizeof(H263ProfileLevelTable)/sizeof(MTK_VENC_PROFILE_LEVEL_ENTRY)
#define MAX_MPEG4_PROFILE_LEVEL_TABLE_SZIE  sizeof(MPEG4ProfileLevelTable)/sizeof(MTK_VENC_PROFILE_LEVEL_ENTRY)


class MtkOmxVenc : public MtkOmxBase {
public:
    MtkOmxVenc();
    ~MtkOmxVenc();
    
    virtual OMX_ERRORTYPE ComponentInit(OMX_IN OMX_HANDLETYPE hComponent,
                                        OMX_IN OMX_STRING componentName);

    virtual OMX_ERRORTYPE  ComponentDeInit(OMX_IN OMX_HANDLETYPE hComponent);

    virtual OMX_ERRORTYPE  GetComponentVersion(OMX_IN OMX_HANDLETYPE hComponent,
                                               OMX_IN OMX_STRING componentName,
                                               OMX_OUT OMX_VERSIONTYPE* componentVersion,
                                               OMX_OUT OMX_VERSIONTYPE* specVersion,
                                               OMX_OUT OMX_UUIDTYPE* componentUUID);

    virtual OMX_ERRORTYPE  SendCommand(OMX_IN OMX_HANDLETYPE hComponent,
                                       OMX_IN OMX_COMMANDTYPE Cmd,
                                       OMX_IN OMX_U32 nParam1,
                                       OMX_IN OMX_PTR pCmdData);

    virtual OMX_ERRORTYPE  GetParameter(OMX_IN OMX_HANDLETYPE hComponent,
                                        OMX_IN  OMX_INDEXTYPE nParamIndex,
                                        OMX_INOUT OMX_PTR ComponentParameterStructure);

    virtual OMX_ERRORTYPE  SetParameter(OMX_IN OMX_HANDLETYPE hComp, 
                                        OMX_IN OMX_INDEXTYPE nParamIndex,
                                        OMX_IN OMX_PTR pCompParam);

    virtual OMX_ERRORTYPE  GetConfig(OMX_IN OMX_HANDLETYPE hComponent, 
                                     OMX_IN OMX_INDEXTYPE nConfigIndex,
                                     OMX_INOUT OMX_PTR ComponentConfigStructure);

    virtual OMX_ERRORTYPE  SetConfig(OMX_IN OMX_HANDLETYPE hComponent, 
                                     OMX_IN OMX_INDEXTYPE nConfigIndex,
                                     OMX_IN OMX_PTR ComponentConfigStructure);

    virtual OMX_ERRORTYPE GetExtensionIndex(OMX_IN OMX_HANDLETYPE hComponent,
    	                              OMX_IN OMX_STRING parameterName,
    	                              OMX_OUT OMX_INDEXTYPE* pIndexType);

    virtual OMX_ERRORTYPE  GetState(OMX_IN OMX_HANDLETYPE hComponent, 
                                    OMX_INOUT OMX_STATETYPE* pState);

    virtual OMX_ERRORTYPE  UseBuffer(OMX_IN OMX_HANDLETYPE hComponent,
                                     OMX_INOUT OMX_BUFFERHEADERTYPE** ppBufferHdr,
                                     OMX_IN OMX_U32 nPortIndex,
                                     OMX_IN OMX_PTR pAppPrivate,
                                     OMX_IN OMX_U32 nSizeBytes,
                                     OMX_IN OMX_U8* pBuffer);


    virtual OMX_ERRORTYPE  AllocateBuffer(OMX_IN OMX_HANDLETYPE hComponent,
                                          OMX_INOUT OMX_BUFFERHEADERTYPE** pBuffHead,
                                          OMX_IN OMX_U32 nPortIndex,
                                          OMX_IN OMX_PTR pAppPrivate,
                                          OMX_IN OMX_U32 nSizeBytes);


    virtual OMX_ERRORTYPE  FreeBuffer(OMX_IN OMX_HANDLETYPE hComponent,
                                      OMX_IN OMX_U32 nPortIndex,
                                      OMX_IN OMX_BUFFERHEADERTYPE* pBuffHead);


    virtual OMX_ERRORTYPE  EmptyThisBuffer(OMX_IN OMX_HANDLETYPE hComponent, 
                                           OMX_IN OMX_BUFFERHEADERTYPE* pBuffHead);


    virtual OMX_ERRORTYPE  FillThisBuffer(OMX_IN OMX_HANDLETYPE hComponent,
                                          OMX_IN OMX_BUFFERHEADERTYPE* pBuffHead);

    virtual OMX_ERRORTYPE  SetCallbacks(OMX_IN OMX_HANDLETYPE hComponent, 
                                        OMX_IN OMX_CALLBACKTYPE* pCallBacks, 
                                        OMX_IN OMX_PTR pAppDat);

    virtual OMX_ERRORTYPE  ComponentRoleEnum(OMX_IN OMX_HANDLETYPE hComponent,
                                             OMX_OUT OMX_U8 *cRole,
                                             OMX_IN OMX_U32 nIndex);

    friend void* MtkOmxVencThread (void* pData);
    friend void* MtkOmxVencEncodeThread (void* pData);

#ifdef USE_MTK_HW_VCODEC
    VENC_DRV_VIDEO_FORMAT_T GetVencFormat(MTK_VENC_CODEC_ID codecId);
    OMX_BOOL InitVideoEncodeHW(OMX_S32* aWidth, OMX_S32* aHeight, OMX_U8* aBuffer, OMX_U32* aSize);
    OMX_BOOL DeInitVideoEncodeHW();
    void EncodeVideo(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
    void EncodeAVC(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
    void EncodeMPEG4(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
#endif
		
#ifdef USE_VIDEO_M4U
    OMX_BOOL GetM4UFrameandBitstreamBuffer(OMX_U8* aInputBuf, OMX_U32 aInputSize, OMX_U8* aOutputBuf, OMX_U32 aOutputSize);
#endif
		
private:

    OMX_BOOL InitH263EncParams();
    OMX_BOOL InitMpeg4EncParams();
    OMX_BOOL InitAvcEncParams();
    
    OMX_BOOL PortBuffersPopulated();
    OMX_BOOL FlushInputPort();
    OMX_BOOL FlushOutputPort();
    OMX_BOOL CheckBufferAvailability();	// check if we have at least one input buffer and one output buffer
    int DequeueInputBuffer();
    int DequeueOutputBuffer();
    void QueueOutputBuffer(int index);  // queue output buffer to the tail of the fill buffer list
    void QueueInputBuffer(int index);    // queue input buffer to the head of the empty buffer list

    OMX_ERRORTYPE HandleStateSet(OMX_U32 nNewState);
    OMX_ERRORTYPE HandlePortEnable(OMX_U32 nPortIndex);
    OMX_ERRORTYPE HandlePortDisable(OMX_U32 nPortIndex);
    OMX_ERRORTYPE HandlePortFlush(OMX_U32 nPortIndex);
    OMX_ERRORTYPE HandleMarkBuffer(OMX_U32 nParam1, OMX_PTR pCmdData);

    OMX_ERRORTYPE HandleEmptyThisBuffer(OMX_BUFFERHEADERTYPE* pBuffHdr);
    OMX_ERRORTYPE HandleFillThisBuffer(OMX_BUFFERHEADERTYPE* pBuffHdr);
    OMX_ERRORTYPE HandleEmptyBufferDone(OMX_BUFFERHEADERTYPE* pBuffHdr);
    OMX_ERRORTYPE HandleFillBufferDone(OMX_BUFFERHEADERTYPE* pBuffHdr);

    void ReturnPendingInputBuffers();
    void ReturnPendingOutputBuffers();

    int findBufferHeaderIndex(OMX_U32 nPortIndex, OMX_BUFFERHEADERTYPE* pBuffHdr);

    OMX_VIDEO_PARAM_PORTFORMATTYPE mInputPortFormat;
    OMX_VIDEO_PARAM_PORTFORMATTYPE mOutputPortFormat;

    OMX_VIDEO_PARAM_AVCTYPE        mAvcType;
    OMX_VIDEO_PARAM_MPEG4TYPE    mMpeg4Type;
    OMX_VIDEO_PARAM_H263TYPE       mH263Type;

    OMX_VIDEO_PARAM_ERRORCORRECTIONTYPE mErrorCorrectionType;
    OMX_VIDEO_PARAM_PROFILELEVELTYPE     mProfileLevelType;
    OMX_VIDEO_PARAM_BITRATETYPE             mBitrateType;
    OMX_VIDEO_PARAM_QUANTIZATIONTYPE   mQuantizationType;
    OMX_VIDEO_PARAM_VBSMCTYPE               mVbsmcType;
    OMX_VIDEO_PARAM_MOTIONVECTORTYPE  mMvType;
    OMX_VIDEO_PARAM_INTRAREFRESHTYPE   mIntraRefreshType;
    OMX_VIDEO_PARAM_AVCSLICEFMO           mAvcSliceFMO;
    OMX_CONFIG_FRAMERATETYPE                  mFrameRateType;
    OMX_VIDEO_CONFIG_BITRATETYPE           mConfigBitrate;
    OMX_CONFIG_INTRAREFRESHVOPTYPE       mConfigIntraRefreshVopType;
    
    // Morris Yang 20110822 [
#ifndef MT6573_MFV_HW
    OMX_BUFFERHEADERTYPE* mLastFrameBufHdr;
    OMX_BUFFERHEADERTYPE* mLastBsBufHdr;
#endif    
    // ]
    

    int mCmdPipe[2];	  // commands from IL client to component
    pthread_mutex_t mCmdQLock;
    
    pthread_t mVencThread;
    pthread_t mVencEncodeThread;
    
    VAL_UINT32_T mVencThreadTid;
    VAL_UINT32_T mVencEncThreadTid;

    unsigned int mPendingStatus;
    OMX_BOOL mEncodeStarted;
    OMX_BOOL mIsComponentAlive;

    pthread_mutex_t mEmptyThisBufQLock;
    pthread_mutex_t mFillThisBufQLock;

    pthread_mutex_t mEncodeLock;

    // for UseBuffer/AllocateBuffer
    sem_t mInPortAllocDoneSem;
    sem_t mOutPortAllocDoneSem;

    // for FreeBuffer
    sem_t mInPortFreeDoneSem;
    sem_t mOutPortFreeDoneSem;

    sem_t mEncodeSem;

    OMX_U32  mNumPendingInput;
    OMX_U32  mNumPendingOutput;

    MTK_VENC_CODEC_ID mCodecId;

    OMX_TICKS mLastFrameTimeStamp;
#ifdef MTK_S3D_SUPPORT
    OMX_VIDEO_H264FPATYPE m3DVideoRecordMode;
#endif

#ifdef USE_MTK_HW_VCODEC
    //VENC_DRV_PARAM_FRM_BUF_T    mFrmBuf[H264ENC_BUFPOOLCOUNT];
    VENC_DRV_QUERY_VIDEO_FORMAT_T   mQueryInfo; 
    VENC_DRV_PROPERTY_T                      mDrvProperty;
    VAL_HANDLE_T                                    mDrvHandle;

    VENC_DRV_PARAM_BS_BUF_T          mBitStreamBuf;
    VENC_DRV_PARAM_FRM_BUF_T        mFrameBuf;

    OMX_BOOL mEncoderInitCompleteFlag;
    OMX_BOOL mBitStreamBufferAllocated;
    VAL_UINT32_T mBitStreamBufferVa; 
    VAL_UINT32_T mBitStreamBufferPa;
    OMX_U32 mBitstreamBufferSize;

#if MTK_OMX_VENC_USE_MDP    
    OMX_BOOL mFrameBufferAllocated;
    VAL_UINT32_T mFrameBufferVa;
    VAL_UINT32_T mFrameBufferPa;
    OMX_U32 mFrameBufferSize;
#endif
    
#ifdef USE_VIDEO_M4U
    MTKM4UDrv*   mM4UBufferHandle;
    VAL_UINT32_T mM4UBufferCount;
    VAL_UINT32_T mM4UBufferSize[VIDEO_M4U_MAX_BUFFER];
    VAL_UINT32_T mM4UBufferVa[VIDEO_M4U_MAX_BUFFER]; 
    VAL_UINT32_T mM4UBufferPa[VIDEO_M4U_MAX_BUFFER];
    VAL_UINT32_T mM4UBufferHdr[VIDEO_M4U_MAX_BUFFER];
#endif

#ifdef ANDROID_ICS
    OMX_BOOL  mForceIFrame;
    OMX_BOOL  mIsTimeLapseMode;
// Morris Yang 20120214 add for live effect recording [
    OMX_BOOL mStoreMetaDataInBuffers;
    OMX_U8*   mEffectYUVBuffer;
    VAL_UINT32_T mEffectM4UBufferSize;
    VAL_UINT32_T mEffectM4UBufferVa;
    VAL_UINT32_T mEffectM4UBufferPa;
    VAL_UINT32_T mEffectM4UBufferHdr;
// ]
#endif
    
    VAL_UINT32_T mSpsPpsSize;
    VAL_UINT32_T mFrameCount;

    VAL_BOOL_T QueryDriverH264Enc(VENC_DRV_QUERY_VIDEO_FORMAT_T *a_prQueryInfo, VENC_DRV_PROPERTY_T *a_prPropertyInfo);
    VAL_BOOL_T RCSettingH264Enc();
    VAL_BOOL_T MESettingH264Enc();
    VAL_BOOL_T EncSettingH264Enc();

    VAL_BOOL_T QueryDriverMPEG4Enc(VENC_DRV_QUERY_VIDEO_FORMAT_T *a_prQueryInfo, VENC_DRV_PROPERTY_T *a_prPropertyInfo);
    VAL_BOOL_T RCSettingMPEG4Enc();
    VAL_BOOL_T MESettingMPEG4Enc();
    VAL_BOOL_T EncSettingMPEG4Enc();

    OMX_BOOL AllocateBitstreamBuffer();
#if MTK_OMX_VENC_USE_MDP
    OMX_BOOL AllocateFrameBuffer();
#endif
    OMX_BOOL HWYUV420ToMTKYUV(const uint8_t *yuvBuf, uint8_t *mtkyuvBuf);
#if 0
    typedef struct _FrmBufStruct
    {
        OMX_BOOL				bUsed;
        VDEC_DRV_FRAMEBUF_T 	frame_buffer;
        OMX_TICKS 				iTimestamp;
        OMX_BUFFERHEADERTYPE	*ipOutputBuffer;
    } FrmBufStruct;

    OMX_BOOL mDecoderInitCompleteFlag;
    VAL_HANDLE_T mDrvHandle;
    VDEC_DRV_RINGBUF_T  mRingbuf;
    VDEC_DRV_SEQINFO_T  mSeqInfo;
    VDEC_DRV_PROPERTY_T mDrvProperty;
    FrmBufStruct mFrmBuf[MTK_VENC_DEFAULT_OUTPUT_BUFFER_COUNT];	// TODO: modify this to dynamic allocate
    OMX_U32 mBitstreamBufferSize;
    OMX_BOOL mBitStreamBufferAllocated;
    VAL_UINT32_T mBitStreamBufferVa; 
    VAL_UINT32_T mBitStreamBufferPa;
#endif
#endif

    void DumpETBQ();
    void DumpFTBQ();
    
#if CPP_STL_SUPPORT    
    vector<int> mEmptyThisBufQ;
    vector<int> mFillThisBufQ;
#endif

#if ANDROID
    Vector<int> mEmptyThisBufQ;
    Vector<int> mFillThisBufQ;
#endif

};


#endif
