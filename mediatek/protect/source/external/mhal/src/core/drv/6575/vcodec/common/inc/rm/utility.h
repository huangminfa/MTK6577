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

#ifndef UTILITY_H__
#define UTILITY_H__
#include <stdio.h>
//#include <stdlib.h>
//#include "rvtypes.h"
#include "rvstruct.h"
#include "alg_info.h"
#include "rv_format_info.h"
#include "strm_iem.h"
#include "decdefs.h"
#include "vcodec_if.h"
#include "vcodec_customization.h"
#include "vcodec_dec_demuxer_if.h"
#include "rm_decoder_component_v2.h"
#define ZTRACEF	printf

#ifndef S8    
    #define S8	signed char
#endif

#define RV9DEC_MAX_WIDTH     1280//640
#define RV9DEC_MAX_HEIGHT    720//608//480	//480, temporary for internal buffer size limit, to be check later !!!
#ifdef WIN32
#define FCLOSEALL() fcloseall()
#else
#define FCLOSEALL()
#endif
/* Define the version information that describes our generated bitstream. */
/* See the description of the RV_MSG_ID_Get_Bitstream_Version message */
/* for the meaning of these values. */
#define RV_BITSTREAM_MAJOR_VERSION          4
#define RV_BITSTREAM_MAJOR_VERSION_RV8      3
#define RV_BITSTREAM_MINOR_VERSION          0
#define RV_BITSTREAM_MINOR_VERSION_RV8      2
#define RV_BITSTREAM_RELEASE_VERSION        0
#define RAW_BITSTREAM_MINOR_VERSION       128


/* definitions for decoding opaque data in bitstream header */
/* Defines match ilvcmsg.h so that ulSPOExtra == rv10init.invariants */
#define RV40_SPO_FLAG_UNRESTRICTEDMV        0x00000001  /* ANNEX D */
#define RV40_SPO_FLAG_EXTENDMVRANGE         0x00000002  /* IMPLIES NEW VLC TABLES */
#define RV40_SPO_FLAG_ADVMOTIONPRED         0x00000004  /* ANNEX F */
#define RV40_SPO_FLAG_ADVINTRA              0x00000008  /* ANNEX I */
#define RV40_SPO_FLAG_INLOOPDEBLOCK         0x00000010  /* ANNEX J */
#define RV40_SPO_FLAG_SLICEMODE             0x00000020  /* ANNEX K */
#define RV40_SPO_FLAG_SLICESHAPE            0x00000040  /* 0: free running; 1: rect */
#define RV40_SPO_FLAG_SLICEORDER            0x00000080  /* 0: sequential; 1: arbitrary */
#define RV40_SPO_FLAG_REFPICTSELECTION      0x00000100  /* ANNEX N */
#define RV40_SPO_FLAG_INDEPENDSEGMENT       0x00000200  /* ANNEX R */
#define RV40_SPO_FLAG_ALTVLCTAB             0x00000400  /* ANNEX S */
#define RV40_SPO_FLAG_MODCHROMAQUANT        0x00000800  /* ANNEX T */
#define RV40_SPO_FLAG_BFRAMES               0x00001000  /* SETS DECODE PHASE */
#define RV40_SPO_BITS_DEBLOCK_STRENGTH      0x0000e000  /* deblocking strength */
#define RV40_SPO_BITS_NUMRESAMPLE_IMAGES    0x00070000  /* max of 8 RPR images sizes */
#define RV40_SPO_FLAG_FRUFLAG               0x00080000  /* FRU BOOL: if 1 then OFF; */
#define RV40_SPO_FLAG_FLIP_FLIP_INTL        0x00100000  /* FLIP-FLOP interlacing; */
#define RV40_SPO_FLAG_INTERLACE             0x00200000  /* de-interlacing prefilter has been applied; */
#define RV40_SPO_FLAG_MULTIPASS             0x00400000  /* encoded with multipass; */
#define RV40_SPO_FLAG_INV_TELECINE          0x00800000  /* inverse-telecine prefilter has been applied; */
#define RV40_SPO_FLAG_VBR_ENCODE            0x01000000  /* encoded using VBR; */
#define RV40_SPO_BITS_DEBLOCK_SHIFT            13
#define RV40_SPO_BITS_NUMRESAMPLE_IMAGES_SHIFT 16



#define FRAMEBUF_NUM	5
#define MAX_TEST_CNT_RV9 500//142


// RealVideo Test Pattern Buffer
#define MAX_INFOPAT_BUF_SIZE   512*1024
#define MAX_CRCPAT_BUF_SIZE    128*1024
#define	MAX_VDOPAT_BUF_SIZE    512*1024//128*1024
#define MAX_RV_SEGMENT_SIZE    200

#ifdef ShowBlockType
//#define RV_FRAMEBUF_SIZE	((RV9DEC_MAX_WIDTH*RV9DEC_MAX_HEIGHT*2)+((RV9DEC_MAX_WIDTH/16)*(RV9DEC_MAX_HEIGHT/16)*4))
#define RV_FRAMEBUF_SIZE	((RV9DEC_MAX_WIDTH*RV9DEC_MAX_HEIGHT*3)>>1)
#else
#define RV_FRAMEBUF_SIZE	((RV9DEC_MAX_WIDTH*RV9DEC_MAX_HEIGHT*3)>>1)

#endif

//typedef struct rv_backend_out_params_struct
struct rv_backend_out_params
{
	U32 numFrames;
	RV_EnumPicCodType DisplayPicCodType;
	
#ifdef _RV9_SkipFrame_	
	RV_EnumPicCodType DisplayPicCodType_org;
	
	U8 skipB;
#endif
	U32 TR;
    /* This is the width and the height as signalled in the bitstream. */
    U32 width;
    U32 height;
    U32 width_Full;
    U32 height_Full;
	// Display frame buffer start address
	U8	*pDisplayFrame_Y;
	U8	*pDisplayFrame_U;
	U8	*pDisplayFrame_V;
	
	// Free frame buffer start address
	U8	*pFreeFrame_Y;
	

};//} rv_backend_out_params;



struct rv_catcher_log_params
{
	U32 Timestamp;
	U32 Total_Frame_Buffers;
	U32 buffer_stock;
	U32 skipB_FrameBuf;
};


typedef struct tagRVTesting
{
	S8	file[60];	
	U32	enable;
}tRVTesting;
extern const char *RV9DEBUG_PicType2 [4]; 
extern U8 * pInfoBuf_rv9;                                                  
extern U32 * pCrcBuf_rv9;                                                  
extern U8 * pVdoBSBuf_rv9;                                                 
       
#ifdef _RV_ARRAY_INPUT_   
extern U32 _gvod_idx;                                                             
#endif
extern U32 gDecodedInFrameNum;		// Currently decoded input frame number.
extern U32 gDecodedOutFrameNum;	// Currently decoded output frame number.
                                                                
extern U32 gTotalDecodedFrameNum;  // Total number of frames to be decoded.
                                                                
extern U32 gSkipDbgFrameNum;		// Skip number of frames debug information.
                                                                
extern U32 gSkipDecodeFrameNum;	// Skip number of input frame to be decoded
                                                                
extern RV_Boolean gSkipDecodeFrameFlag;  


extern alg_cntx_hdr_st context_p_rv9;


extern payload_inf_st payload_inf_tab_rv9[200];

extern RM_DECODER_PAYLOAD_INFO_T payload_inf_tab_rv9_MAUI[200];

extern U32 rv9_open_msg_buf[128 / 4];


extern U32 rv9_cfr_msg_buf[128 / 4];


//extern U32 rv9_cfr_msg_buf_1[128 / 4];

extern U32 rv9_msg_buf[128 / 4];


extern U32 rv9_err_buf[128/4];
#ifndef _RV_ARRAY_INPUT_                                                            
extern FILE *fpInfo_rv9;                                                   
extern FILE *fpVdoBS_rv9;      
extern FILE *fpCrc_rv9;
#ifdef _RV_CRC_OUT_
extern FILE *fpCrc_rv9_out;   
extern FILE *fpCrc_rv9_out_DecOrder;   
#endif
#endif
#ifdef _YUV_OUTPUT_                                       
extern FILE *fpOut_rv9;
#endif  
#ifdef _YUV_DEC_ORDER_OUTPUT_                                                  
extern FILE *fpOut_rv9_dec_order;   
#endif 
extern U32	TestCnt_rv9;


// RealVideo Test Pattern Buffer

extern U32 RV9_PatInfoBuf[MAX_INFOPAT_BUF_SIZE];


extern U32 RV9_PatCrcBuf[MAX_CRCPAT_BUF_SIZE];


extern U32 RV9_PatVdoBSBuf[MAX_VDOPAT_BUF_SIZE];


extern U32 RV9_SegmentBuf[2*MAX_RV_SEGMENT_SIZE];
extern U32 RV9_SegmentBuf_BS[2*MAX_RV_SEGMENT_SIZE];

// End of RealVideo Test Pattern Buffer


extern tRVTesting	RV9Testing[MAX_TEST_CNT_RV9];
extern U32 RV9_FrameBuf[FRAMEBUF_NUM][RV_FRAMEBUF_SIZE/4+32];
extern int FrameBuf_InUsed[FRAMEBUF_NUM];

#if defined(_RV9_SkipFrame_)||defined(_RV9_ADAPTIVE_FRAME_DEBLOCKING_)||defined(_RV_VAR_DIM_MODE_)
extern U32 skipB_FrameBuf;
extern I32 Buf_Stock;
extern U32 Prev_ulDataLen;
extern U32 input_frame;
extern I32 BS_Buf;
I32 Add_BS_Buf_rv9(U32 ulLength);
I32 Sub_BS_Buf_rv9(U32 ulLength);
void Polling(U32 time, U32 tt1);
int sync_frame_info_rv9_BS(rv_frame *rv_frame_info, U8 *InfoBuf_ptr) ; 
#endif

void InitFrameBuffer(U32 width, U32 height);
void CleanFrameBuf(void);
void FreeFramBufPtr(U8 *ptr);
int parse_command_line_rv9(void);
U32 rv9_adler32_update(U32 adler, U8 *buf, U32 xlen, U32 ylen, U32 pitch);
U32 rv9pat_buffer_init(void);
void rv9pat_load_vdobs(U32 read_len);
void set_format_info_rv9(rv_format_info *rv_format, U8 *InfoBuf_ptr);
int sync_frame_info_rv9(rv_frame *rv_frame_info, U8 *InfoBuf_ptr)  ;

U32 rv9_frame_write(struct rv_backend_out_params *pOutParams, U32 ulNumOutputFrames); 
U32 rv9_frame_write_LC(struct rv_backend_out_params *pOutParams, U32 ulNumOutputFrames);
void RV9_GetLogInfo(struct rv_catcher_log_params *pam);
U8 *GetFreeFrameBufPtr(void *);
#if 0
#if defined(WIN32) || defined(ARMULATOR)

extern void RMSwDecReturnBitstream(IN HANDLE hCodec,U8* pu1Addr, U32 u4Length);
extern U8* RMSwDecMallocYUVBuffer(IN HANDLE hCodec);
extern U8* RMSwDecMallocYUVBuffer_VC(void);
extern RV_Boolean RMSwDecFreeYUVBuffer(IN HANDLE hCodec,U8* pu1Addr);
#else
extern void RMSwDecReturnBitstream(unsigned char* pu1Addr, unsigned int u4Length);
extern unsigned char* RMSwDecMallocYUVBuffer(void);
extern int RMSwDecFreeYUVBuffer(unsigned char* pu1Addr);
#endif
#endif
#ifdef _RV9_CMD_INPUT_
extern int parse_command_line_rv9_cmd(int argc , char* argv[]) ;
#endif
#ifdef _RV_SHOW_TIME_
extern U32 gMB_VLD_time;
extern U32 gMB_VLD_time1;
extern U32 gMB_IPMC_time;
extern U32 gMB_REF_MOVE_time;
extern U32 gMB_Deblocking_time;
extern U32 gMB_BUFWB_time;
extern U32 gMV_move_time;
#endif

#ifdef _RV_SHOW_WAIT_TIME_
extern U32 gMB_WAIT_time;
extern U32 gWAIT_DONE_cnt;
#endif

typedef enum 
{
	CODEC_H264,
	CODEC_MPEG4,
	CODEC_H263,
	CODEC_RV9_10,
	CODEC_H263_VT,
	CODEC_MPEG4_VT,
	CODEC_ID_MAX
}VIDEO_CODEC_T;
typedef enum 
{
	MP4ENC_RECORDER,
	MP4ENC_VT
}MPEG4_ENC_SCENARIO_T;




extern U8 vdec_context_buffer[MAX_NUM_VDEC_HANDLES * MAX_VDEC_CONTEXT_SIZE];

extern U8 vdec_scratch_L1[MAX_VDEC_SCRATCH_L1_SIZE];

extern U8 vdec_scratch_L20[MAX_VDEC_SCRATCH_L20_SIZE+4];

extern U8 vdec_scratch_L21[MAX_VDEC_SCRATCH_L21_SIZE+4];

extern U8 vdec_scratch_EXT[MAX_VDEC_SCRATCH_EXT_SIZE];


extern HANDLE ghCodec;
extern HANDLE ghDrv;
// Morris Yang 20110622 mark
/*
typedef struct 
{
	int				YUVBufferSize;
}DRIVER_HANDLER_T;
extern DRIVER_HANDLER_T g_Drv;
*/
extern VCODEC_DEC_CALLBACK_T rCall_Back_API;

extern RM_DECODER_CTRL_T rRMDecCtrl; 
extern VCODEC_DEC_API_T *prRM_API;
extern VCODEC_DEC_CALLBACK_T *pfnCallback;

extern RV9_DEC_CUSTOM_SETTING_T rmvbCustomSetting;

#endif