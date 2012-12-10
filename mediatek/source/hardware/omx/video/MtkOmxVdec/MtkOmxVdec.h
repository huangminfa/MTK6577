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

#ifndef MTK_OMX_VDEC
#define MTK_OMX_VDEC

#include "MtkOmxBase.h"

#if (defined(MT6573) && defined(MT6516))
#undef USE_VIDEO_M4U
#else
#define USE_VIDEO_M4U
#endif

#if defined(NO_MEM_DRIVER)
#undef USE_VIDEO_M4U
#endif
#ifdef USE_VIDEO_M4U
#define VIDEO_M4U_MAX_BUFFER 100
#include "m4u_lib.h"
#include "val_api.h"
#endif

#ifdef USE_MTK_HW_VCODEC
//#include "val_types.h"
#include "vdec_drv_if.h"
#define DEC_TRYALLOCMEMCOUNT 10
#define VC1_SEQUENCE_LAYER_LEN 36
#endif

#ifdef MTK_S3D_SUPPORT
#include "MTKAsvd.h"
#include "MTKAsvdErrCode.h"
#endif


#define SUPPORT_PARTIAL_FRAME 1

#define ANDROID_ICS 1

#define MTK_OMXVDEC_EXTENSION_INDEX_PARAM_3D_STEREO_PLAYBACK  "OMX.MTK.index.param.video.3DVideoPlayback"
#define MTK_OMXVDEC_EXTENSION_INDEX_PARAM_PRIORITY_ADJUSTMENT "OMX.MTK.index.param.video.EnablePriorityAdjustment"
#define MTK_OMX_EXTENSION_INDEX_PARTIAL_FRAME_QUERY_SUPPORTED "OMX.MTK.index.param.PartialFrameSupport"
#define MTK_OMXVDEC_EXTENSION_INDEX_PARAM_SWITCH_BW_TVOUT "OMX.MTK.index.param.video.SwitchBwTVout"
#define MTK_OMXVDEC_EXTENSION_INDEX_PARAM_STREAMING_MODE "OMX.MTK.index.param.video.StreamingMode"

typedef enum MTK_VDEC_CODEC_ID
{
    MTK_VDEC_CODEC_ID_H263,
    MTK_VDEC_CODEC_ID_MPEG4,
    MTK_VDEC_CODEC_ID_AVC,
    MTK_VDEC_CODEC_ID_RV,
    MTK_VDEC_CODEC_ID_VC1,
    MTK_VDEC_CODEC_ID_VPX,
    MTK_VDEC_CODEC_ID_INVALID = 0xFFFFFFFF,
} MTK_VDEC_CODEC_ID;

typedef enum
{
    AVC_NALTYPE_SLICE    = 1,    /* non-IDR non-data partition */
    AVC_NALTYPE_DPA      = 2,    /* data partition A */
    AVC_NALTYPE_DPB      = 3,    /* data partition B */
    AVC_NALTYPE_DPC      = 4,    /* data partition C */
    AVC_NALTYPE_IDR      = 5,    /* IDR NAL */
    AVC_NALTYPE_SEI      = 6,    /* supplemental enhancement info */
    AVC_NALTYPE_SPS      = 7,    /* sequence parameter set */
    AVC_NALTYPE_PPS      = 8,    /* picture parameter set */
    AVC_NALTYPE_AUD      = 9,    /* access unit delimiter */
    AVC_NALTYPE_EOSEQ    = 10,   /* end of sequence */
    AVC_NALTYPE_EOSTREAM = 11,  /* end of stream */
    AVC_NALTYPE_FILL     = 12    /* filler data */
} AVCNalType;

// adb shell property flags
#define MTK_OMX_VDEC_ENABLE_PRIORITY_ADJUSTMENT  (1 << 0)
#define MTK_OMX_VDEC_DUMP_BITSTREAM              (1 << 1)
#define MTK_OMX_VDEC_DUMP_OUTPUT                 (1 << 2)


// H.263
#define MTK_VDEC_H263_DEFAULT_INPUT_BUFFER_COUNT 10
#define MTK_VDEC_H263_DEFAULT_OUTPUT_BUFFER_COUNT 8
#define MTK_VDEC_H263_DEFAULT_INPUT_BUFFER_SIZE    16000
#define MTK_VDEC_H263_DEFAULT_OUTPUT_BUFFER_SIZE  38016

// MPEG4
#define MTK_VDEC_MPEG4_DEFAULT_INPUT_BUFFER_COUNT 10
#define MTK_VDEC_MPEG4_DEFAULT_OUTPUT_BUFFER_COUNT 8
#define MTK_VDEC_MPEG4_DEFAULT_INPUT_BUFFER_SIZE    16000
#define MTK_VDEC_MPEG4_DEFAULT_OUTPUT_BUFFER_SIZE  38016

// AVC
#define MTK_VDEC_AVC_DEFAULT_INPUT_BUFFER_COUNT 10
#define MTK_VDEC_AVC_DEFAULT_OUTPUT_BUFFER_COUNT 21
#define MTK_VDEC_AVC_DEFAULT_INPUT_BUFFER_SIZE    16000
#define MTK_VDEC_AVC_DEFAULT_OUTPUT_BUFFER_SIZE  38016
#define MTK_VDEC_AVC_DEC_TIMESTAMP_ARRAY_SIZE 21

// RV
#define MTK_VDEC_RV_DEFAULT_INPUT_BUFFER_COUNT 10
#define MTK_VDEC_RV_DEFAULT_OUTPUT_BUFFER_COUNT 10
#define MTK_VDEC_RV_DEFAULT_INPUT_BUFFER_SIZE    16000
#define MTK_VDEC_RV_DEFAULT_OUTPUT_BUFFER_SIZE  38016

// VC1
#define MTK_VDEC_VC1_DEFAULT_INPUT_BUFFER_COUNT 10
#define MTK_VDEC_VC1_DEFAULT_OUTPUT_BUFFER_COUNT 8
#define MTK_VDEC_VC1_DEFAULT_INPUT_BUFFER_SIZE    16000
#define MTK_VDEC_VC1_DEFAULT_OUTPUT_BUFFER_SIZE  38016

// VPX
#define MTK_VDEC_VPX_DEFAULT_INPUT_BUFFER_COUNT  10
#define MTK_VDEC_VPX_DEFAULT_OUTPUT_BUFFER_COUNT 8
#define MTK_VDEC_VPX_DEFAULT_INPUT_BUFFER_SIZE   16000
#define MTK_VDEC_VPX_DEFAULT_OUTPUT_BUFFER_SIZE  38016

#ifdef ANDROID_ICS
#define MIN_UNDEQUEUED_BUFS 2
#endif


typedef struct  MTK_VDEC_PROFILE_LEVEL_ENTRY {
    OMX_U32 profile;
    OMX_U32 level;
} MTK_VDEC_PROFILE_LEVEL_ENTRY;


MTK_VDEC_PROFILE_LEVEL_ENTRY AvcProfileLevelTable[] = 
{
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel1},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel1b},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel11},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel12},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel13},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel2},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel21},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel22},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel3},
    {OMX_VIDEO_AVCProfileBaseline, OMX_VIDEO_AVCLevel31},

    {OMX_VIDEO_AVCProfileMain, OMX_VIDEO_AVCLevel1},
    {OMX_VIDEO_AVCProfileMain, OMX_VIDEO_AVCLevel1b},
    {OMX_VIDEO_AVCProfileMain, OMX_VIDEO_AVCLevel11},
    {OMX_VIDEO_AVCProfileMain, OMX_VIDEO_AVCLevel12},
    {OMX_VIDEO_AVCProfileMain, OMX_VIDEO_AVCLevel13},
    {OMX_VIDEO_AVCProfileMain, OMX_VIDEO_AVCLevel2},
    {OMX_VIDEO_AVCProfileMain, OMX_VIDEO_AVCLevel21},
    {OMX_VIDEO_AVCProfileMain, OMX_VIDEO_AVCLevel22},
    {OMX_VIDEO_AVCProfileMain, OMX_VIDEO_AVCLevel3},
    {OMX_VIDEO_AVCProfileMain, OMX_VIDEO_AVCLevel31},

    {OMX_VIDEO_AVCProfileHigh, OMX_VIDEO_AVCLevel1},
    {OMX_VIDEO_AVCProfileHigh, OMX_VIDEO_AVCLevel1b},
    {OMX_VIDEO_AVCProfileHigh, OMX_VIDEO_AVCLevel11},
    {OMX_VIDEO_AVCProfileHigh, OMX_VIDEO_AVCLevel12},
    {OMX_VIDEO_AVCProfileHigh, OMX_VIDEO_AVCLevel13},
    {OMX_VIDEO_AVCProfileHigh, OMX_VIDEO_AVCLevel2},
    {OMX_VIDEO_AVCProfileHigh, OMX_VIDEO_AVCLevel21},
    {OMX_VIDEO_AVCProfileHigh, OMX_VIDEO_AVCLevel22},
    {OMX_VIDEO_AVCProfileHigh, OMX_VIDEO_AVCLevel3},
    {OMX_VIDEO_AVCProfileHigh, OMX_VIDEO_AVCLevel31},
};


MTK_VDEC_PROFILE_LEVEL_ENTRY H263ProfileLevelTable[] = 
{
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level10},
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level20},
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level30},
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level40},
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level45},
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level50},
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level60},
    {OMX_VIDEO_H263ProfileBaseline, OMX_VIDEO_H263Level70},
};


MTK_VDEC_PROFILE_LEVEL_ENTRY MPEG4ProfileLevelTable[] = 
{
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level0},
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level0b},
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level1},
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level2},
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level3},
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level4},
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level4a},
    {OMX_VIDEO_MPEG4ProfileSimple, OMX_VIDEO_MPEG4Level5},

    {OMX_VIDEO_MPEG4ProfileAdvancedSimple, OMX_VIDEO_MPEG4Level0},
    {OMX_VIDEO_MPEG4ProfileAdvancedSimple, OMX_VIDEO_MPEG4Level0b},
    {OMX_VIDEO_MPEG4ProfileAdvancedSimple, OMX_VIDEO_MPEG4Level1},
    {OMX_VIDEO_MPEG4ProfileAdvancedSimple, OMX_VIDEO_MPEG4Level2},
    {OMX_VIDEO_MPEG4ProfileAdvancedSimple, OMX_VIDEO_MPEG4Level3},
    {OMX_VIDEO_MPEG4ProfileAdvancedSimple, OMX_VIDEO_MPEG4Level4},
    {OMX_VIDEO_MPEG4ProfileAdvancedSimple, OMX_VIDEO_MPEG4Level4a},
    {OMX_VIDEO_MPEG4ProfileAdvancedSimple, OMX_VIDEO_MPEG4Level5},

    {OMX_VIDEO_MPEG4ProfileMain, OMX_VIDEO_MPEG4Level0},
    {OMX_VIDEO_MPEG4ProfileMain, OMX_VIDEO_MPEG4Level0b},
    {OMX_VIDEO_MPEG4ProfileMain, OMX_VIDEO_MPEG4Level1},
    {OMX_VIDEO_MPEG4ProfileMain, OMX_VIDEO_MPEG4Level2},
    {OMX_VIDEO_MPEG4ProfileMain, OMX_VIDEO_MPEG4Level3},
    {OMX_VIDEO_MPEG4ProfileMain, OMX_VIDEO_MPEG4Level4},
};

typedef struct BITBUF_STM {
    OMX_U8 *StartAddr;
    OMX_U32 nSize;
    OMX_U32 nBitCnt;
    OMX_U32 nZeroCnt;
    OMX_U32 Cur32Bits;
    OMX_U32 CurBitCnt;
} BITBUF_STM;

#define MAX_AVC_PROFILE_LEVEL_TABLE_SZIE       sizeof(AvcProfileLevelTable)/sizeof(MTK_VDEC_PROFILE_LEVEL_ENTRY)
#define MAX_H263_PROFILE_LEVEL_TABLE_SZIE     sizeof(H263ProfileLevelTable)/sizeof(MTK_VDEC_PROFILE_LEVEL_ENTRY)
#define MAX_MPEG4_PROFILE_LEVEL_TABLE_SZIE   sizeof(MPEG4ProfileLevelTable)/sizeof(MTK_VDEC_PROFILE_LEVEL_ENTRY)


class MtkOmxVdec : public MtkOmxBase {
public:
    MtkOmxVdec();
    ~MtkOmxVdec();
    
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

    friend void* MtkOmxVdecThread (void* pData);
    friend void* MtkOmxVdecDecodeThread (void* pData);

// TODO: remove
    friend void FakeVdecDrvDecode(MtkOmxVdec* pVdec, OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);

#ifdef DYNAMIC_PRIORITY_ADJUSTMENT
    void PriorityAdjustment();
#endif
    void EnableRRPriority(OMX_BOOL bEnable);

#ifdef USE_MTK_HW_VCODEC
    OMX_BOOL InitVideoDecodeHW(OMX_S32* aWidth, OMX_S32* aHeight, OMX_S32* aAspectRatioWidth, OMX_S32* aAspectRatioHeight, OMX_U8* aBuffer, OMX_U32* aSize);
    OMX_BOOL DeInitVideoDecodeHW();
#ifndef USE_VIDEO_M4U // use PMEM
    OMX_BOOL AllocateBitstreamBuffer(OMX_U8* aInputBuf, OMX_U32 aInputSize);
#endif
    VDEC_DRV_FRAMEBUF_T* GetFrmBuf(OMX_TICKS iTimestamp, OMX_BUFFERHEADERTYPE* ipOutputBuffer);
    void InsertFrmBuf(OMX_BUFFERHEADERTYPE* ipOutputBuffer);
    void RemoveFrmBuf(OMX_BUFFERHEADERTYPE* ipOutputBuffer);
    OMX_BOOL PutFrmBuf(OMX_BUFFERHEADERTYPE* ipOutputBuffer);
    OMX_BUFFERHEADERTYPE* GetDisplayBuffer(OMX_BOOL bGetResolution = OMX_FALSE);
    OMX_BUFFERHEADERTYPE* GetFreeBuffer();
    OMX_U32 CheckFreeBuffer(OMX_BOOL bLOG);
    OMX_BOOL IsFreeBuffer(OMX_BUFFERHEADERTYPE* ipOutputBuffer);

    VDEC_DRV_VIDEO_FORMAT_T GetVdecFormat(MTK_VDEC_CODEC_ID codecId);
    OMX_BOOL DecodeVideo(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
    OMX_BOOL AvcDecode(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
    OMX_BOOL RvDecode(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
    OMX_BOOL Mpeg4Decode(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
    OMX_BOOL H263Decode(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
    OMX_BOOL VpxDecode(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
    OMX_BOOL Vc1Decode(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
    void MakeVc1SequenceLayer(OMX_U8* pInput, OMX_U8* pOutput);
    OMX_BOOL FlushDecoder();

    // for H.264
    OMX_BOOL InsertionSortForInputPTS(OMX_TICKS timeTS);
    OMX_TICKS RemoveForInputPTS();
    OMX_BOOL RemoveForInputAtPTS(OMX_TICKS timeTS);

    OMX_BOOL IsVideoResolutionSupported(OMX_U32 width, OMX_U32 height, OMX_U32& maxWidth, OMX_U32& maxHeight);

#ifdef USE_VIDEO_M4U
#ifdef SUPPORT_PARTIAL_FRAME
    OMX_BOOL SetupM4UPartialBsAndFrameBuffer(VDEC_DRV_FRAMEBUF_T* aFrame, OMX_U8* aInputBuf, OMX_U32 aInputSize, OMX_U8* aOutputBuf);
#endif
    OMX_BOOL GetM4UFrameandBitstreamBuffer(VDEC_DRV_FRAMEBUF_T* aFrame, OMX_U8* aInputBuf, OMX_U32 aInputSize, OMX_U8* aOutputBuf);
#endif

#endif
		
private:

    OMX_BOOL InitH263Params();
    OMX_BOOL InitMpeg4Params();
    OMX_BOOL InitAvcParams();
    OMX_BOOL InitRvParams();
    OMX_BOOL InitVc1Params();
    OMX_BOOL InitVpxParams();
    
    OMX_BOOL PortBuffersPopulated();
    OMX_BOOL FlushInputPort();
    OMX_BOOL FlushOutputPort();
    OMX_BOOL CheckBufferAvailability();	// check if we have at least one input buffer and one output buffer
    int DequeueInputBuffer();
    int DequeueOutputBuffer();
    int FindQueueOutputBuffer(OMX_BUFFERHEADERTYPE* pBuffHdr);
    void QueueOutputBuffer(int index);
    void QueueInputBuffer(int index);    // queue input buffer to the head of the empty buffer list
    void CheckOutputBuffer();

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

    OMX_BOOL SearchForVOLHeader(OMX_U8* aInputBuf, OMX_U32 aInputSize);
#ifdef MTK_S3D_SUPPORT
    void Prepare32Bits(BITBUF_STM *pBitBuf);
    OMX_U32 GetBits(BITBUF_STM *pBitBuf, OMX_U32 numBits);
    OMX_U32 GetUEGolomb(BITBUF_STM *pBitBuf);
    OMX_VIDEO_H264FPATYPE ParserSEI(OMX_U8* aInputBuf, OMX_U32 aInputSize);
#endif

#ifdef SUPPORT_PARTIAL_FRAME
    OMX_BOOL HandleAssemblePartialFrame(OMX_BUFFERHEADERTYPE* pInputBuf, OMX_BUFFERHEADERTYPE* pOutputBuf);
#endif

    OMX_VIDEO_PARAM_PORTFORMATTYPE mInputPortFormat;
    OMX_VIDEO_PARAM_PORTFORMATTYPE mOutputPortFormat;

    OMX_VIDEO_PARAM_RVTYPE mRvType;

    int mCmdPipe[2];	  // commands from IL client to component
    pthread_mutex_t mCmdQLock;
    
    pthread_t mVdecThread;
    pthread_t mVdecDecodeThread;

// Morris Yang 20111101 [
    VAL_UINT32_T mVdecThreadTid;
    VAL_UINT32_T mVdecDecThreadTid;
// ]

    VAL_UINT32_T mFramesDecoded;
    VAL_UINT32_T mTotalDecodeTime;
    
    unsigned int mPendingStatus;
    OMX_BOOL mDecodeStarted;
    OMX_BOOL mIsComponentAlive;

    pthread_mutex_t mEmptyThisBufQLock;
    pthread_mutex_t mFillThisBufQLock;

    pthread_mutex_t mDecodeLock;

    // for UseBuffer/AllocateBuffer
    sem_t mInPortAllocDoneSem;
    sem_t mOutPortAllocDoneSem;

    // for FreeBuffer
    sem_t mInPortFreeDoneSem;
    sem_t mOutPortFreeDoneSem;

    sem_t mDecodeSem;

    OMX_U32  mNumPendingInput;
    OMX_U32  mNumPendingOutput;

    MTK_VDEC_CODEC_ID mCodecId;

    int mCurrentSchedPolicy;

    OMX_U32 mPropFlags;
    OMX_U32 mForceOutputBufferCount;
#ifdef MTK_S3D_SUPPORT
    OMX_U32 mFramesDisplay;
    OMX_BOOL AvsdInit();
    OMX_VIDEO_H264FPATYPE m3DStereoMode;
    MTKAsvd *s3dAsvd;
    ASVD_STATE_ENUM asvdStatus;
    ASVD_SET_ENV_INFO_STRUCT asvdInitInfo;
    ASVD_SET_WORK_BUF_INFO_STRUCT asvdWorkBufInfo;
    OMX_U8 *asvdWorkingBuffer;
#endif

    OMX_BOOL mbH263InMPEG4;
#ifdef DYNAMIC_PRIORITY_ADJUSTMENT
    OMX_U32 mErrorCount;
    OMX_U32 mPendingOutputThreshold;
    OMX_TICKS mTimeThreshold;
    OMX_TICKS mllLastDispTime; // include NOT_DISPLAY
#endif

#ifdef USE_MTK_HW_VCODEC
    typedef struct _FrmBufStruct
    {
        OMX_BOOL				bUsed;
        OMX_BOOL                bDisplay;
        OMX_BOOL                bNonRealDisplay;
        OMX_BOOL                bFillThis;
        VDEC_DRV_FRAMEBUF_T 	frame_buffer;
        OMX_TICKS 				iTimestamp;
        OMX_BUFFERHEADERTYPE	*ipOutputBuffer;
    } FrmBufStruct;

    OMX_BOOL mDecoderInitCompleteFlag;
    VAL_HANDLE_T mDrvHandle;
    VDEC_DRV_RINGBUF_T  mRingbuf;
    VDEC_DRV_SEQINFO_T  mSeqInfo;
    FrmBufStruct* mFrameBuf;
    OMX_U32 mBitstreamBufferSize;
    OMX_BOOL mBitStreamBufferAllocated;
    VAL_UINT32_T mBitStreamBufferVa; 
    VAL_UINT32_T mBitStreamBufferPa;

    // for speedy mode
    OMX_U32  mNumFreeAvailOutput;
    OMX_U32  mNumAllDispAvailOutput;
    OMX_U32  mNumNotDispAvailOutput;
    AwesomePlayer *mpAwePlayer;

    // for video subsample
    OMX_S32 mAspectRatioWidth;
    OMX_S32 mAspectRatioHeight;

    // for input semaphore
    OMX_S32  mNumSemaphoreCountForInput;
    OMX_S32  mNumSemaphoreCountForOutput;

#ifdef USE_VIDEO_M4U
    MTKM4UDrv*   mM4UBufferHandle;
    VAL_UINT32_T mM4UBufferCount;
    VAL_UINT32_T mM4UBufferSize[VIDEO_M4U_MAX_BUFFER];
    VAL_UINT32_T mM4UBufferVa[VIDEO_M4U_MAX_BUFFER]; 
    VAL_UINT32_T mM4UBufferPa[VIDEO_M4U_MAX_BUFFER];
    VAL_UINT32_T mM4UBufferHdr[VIDEO_M4U_MAX_BUFFER];

#ifdef SUPPORT_PARTIAL_FRAME
    VAL_UINT32_T mM4UPartialFrameBufferVa;
    VAL_UINT32_T mM4UPartialFrameBufferPa;
    VAL_UINT32_T mM4UPartialFrameBufferSize;
#endif
#endif
    
    // for VC1
    OMX_TICKS mFrameTsInterval;
    OMX_TICKS mCurrentFrameTs;
    OMX_BOOL  mFirstFrameRetrieved;
    OMX_BOOL  mResetFirstFrameTs;
    OMX_BOOL  mCorrectTsFromOMX;

    // for H.264
    OMX_TICKS DisplayTSArray[MTK_VDEC_AVC_DEC_TIMESTAMP_ARRAY_SIZE];
    OMX_S32 iTSIn;

    // for output dump
    OMX_BOOL  mDumpOutputFrame;
#endif

#ifdef ANDROID_ICS
    OMX_BOOL mIsUsingNativeBuffers;
#endif

    // for UI response improvement
    OMX_U32   mRRSlidingWindowLength;
    OMX_U32   mRRSlidingWindowCnt;
    OMX_U32   mRRSlidingWindowLimit;
    OMX_U32   mRRCntCurWindow;
    unsigned long long mLastCpuIdleTime;
    unsigned long long mLastSchedClock;

#ifdef SUPPORT_PARTIAL_FRAME
    OMX_U8* mPartialFrameBuffer;
    OMX_U32 mPartialFrameBufferSize;
    OMX_U32 mPartialFrameBufferOffset;
    OMX_BOOL mAssemblePartialFrame;
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

    OMX_U32 FNum; // for CRC check

    // for seek and thumbnail
    OMX_BOOL mThumbnailMode;
    OMX_TICKS mSeekTargetTime;
    OMX_BOOL mSeekMode;

    OMX_S32 mConcealLevel;
    OMX_BOOL mEOSFound;
    OMX_BOOL mFATALError;
    OMX_BOOL mStreamingMode;
    
#ifdef MT6577 // Morris Yang 20120627
    OMX_BOOL mCodecTidInitialized;
    OMX_U32 mNumCodecThreads;
    pid_t mCodecTids[8];
#endif
};


#endif
