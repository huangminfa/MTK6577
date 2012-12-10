/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2005
*
*  BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
*  THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
*  RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON
*  AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
*  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
*  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
*  NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
*  SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
*  SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH
*  THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
*  NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S
*  SPECIFICATION OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
*
*  BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
*  LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
*  AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
*  OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY BUYER TO
*  MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE. 
*
*  THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE
*  WITH THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF
*  LAWS PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING THEREOF AND
*  RELATED THERETO SHALL BE SETTLED BY ARBITRATION IN SAN FRANCISCO, CA, UNDER
*  THE RULES OF THE INTERNATIONAL CHAMBER OF COMMERCE (ICC).
*
*****************************************************************************/

/*****************************************************************************
 *
 * Filename:
 * ---------
 *   rm_decoder_component_v2.h
 *
 * Project:
 * --------
 *	MTK
 *
 * Description:
 * ------------
 *   
 *
 * Author:
 * -------
 *   Eason Lin
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision$
 * $Modtime$
 * $Log:   $
 *
 * 11 23 2010 annu.wang
 * [MAUI_02840772] [HAL] video hal check-in W1049
 * .
 *
 * 10 20 2010 gary.huang
 * [MAUI_02829100] [RHR][MAUIW1038OF_RHR] Integration to W10.43
 * .
 *
 * 08 04 2010 gary.huang
 * [MAUI_02602513] [V2 Development] Modify EOF processing
 * .
 *
 * Jul 14 2010 mtk01891
 * [MAUI_02589671] [V2 Development]RMVB jump I frame flush buffer
 * 
 *
 * Jun 8 2010 mtk01891
 * [MAUI_02554245] [V2 Development] RM adopt repeated frame mechanism
 * 
 *
 * Mar 26 2010 mtk01891
 * [MAUI_02381881] v2 RM integration
 * 
 *
 * Feb 23 2010 mtk02600
 * [MAUI_02361845] [V2 Integration] Fix MoDIS issue
 * 
 *
 * Feb 11 2010 mtk02600
 * [MAUI_02355914] [V2_Integration] Update player to flexible architecture and all the customization
 * 
 *
 * Jan 28 2010 mtk02600
 * [MAUI_02346858] [Video][v2 architecture] Rename files and update include header
 * 
 *
 * Jan 12 2010 mtk02600
 * [MAUI_02333174] [Video][v2 architecture] Update encoder/decoder architecture
 * 
 *
 * Dec 22 2009 mtk02600
 * [MAUI_02020588] [V2 check in] check in video v2 with w09.49 temp load
 * 
 *
 * 
 *
 * 
 ****************************************************************************/
#ifndef RM_DECODER_COMPONENT_V2_H
#define RM_DECODER_COMPONENT_V2_H



//#ifdef __RV_DEC_SW_SUPPORT__
#if defined(WIN32) || defined(ARMULATOR)

#define LOCAL_PARA_HDR     \
     U8  ref_count;        \
     U16 msg_len;
#else

//#include "app_ltlcom.h"

#endif
//#include "iem.h"
#include "strm_iem.h"
#include "vcodec_dec_demuxer_if.h"
#include "rvbuffer.h"
//#include "rvtypes.h"
#include "rvstruct.h"
#include "vcodec_if.h"
#include "vcodec_customization.h"

//#include "video_codec_mem_v2.h"
//#include "drv_comm.h"
//#include "video_types_v2.h"
//#include "video_comm_component_v2.h"
//#include "video_codec_if_v2.h"
//#include "video_dbg_v2.h"
//#include "kal_non_specific_general_types.h"

/*
#ifndef S8    
    #define S8	signed char
#endif
#ifndef U8
    #define U8	unsigned char
#endif
#ifndef I8    
    #define I8	signed char
#endif
#ifndef U16    
    #define	U16	unsigned short
#endif    
#ifndef I16    
    #define I16	signed short
#endif
#ifndef U32    
    #define	U32	unsigned int
#endif
#ifndef I32    
    #define I32	signed int
#endif
*/
#ifndef BOOL
	#define BOOL 	unsigned int
#endif

#define MAX_NUM_RPR_SIZES 8
#define MAX_PAYLOAD_NUM 128
typedef struct
{
    BOOL (*pfnOpenReqHanlder)(alg_cntx_hdr_st *context_p, RV_open_info_st *req);
    BOOL (*pfnStrmDataReqHandler)(IN HANDLE hCodec, alg_cntx_hdr_st *context_p, RV_strm_decode_st *req,RV_decode_info_st *RVDriver_dec_info);
    BOOL (*pfnCloseReqHanlder)(IN HANDLE hCodec, alg_cntx_hdr_st *context_p, RV_decode_info_st *req);
    BOOL (*pfnFlushReqHanlder)(IN HANDLE hCodec, alg_cntx_hdr_st *context_p, RV_decode_info_st *req);
}RM_CODEC_API_T;
/*
typedef enum
{
    RM_SWITCH_TO_CACHE = 0,
    RM_SWITCH_TO_NONCACHE,
    RM_SWITCH_NONE
}RM_MEMORY_SWITCH_T;
*/
typedef struct
{
    int fgIsValid;
    VCODEC_DEC_PRIVATE_OUTPUT_T rPrivateDecOutput;
}RM_PRIVATE_OUTPUT_T;

typedef struct
{
   // int fgIsBufferDefaultCacheable;
    int fgBufferingState;
    int fgIsCodecInit;
    int fgFirstFrame;
    int fgLossyDecoder;
   // int fgBSBufferCacheable;
   // int fgFrmBufferCacheable;
   // int fgCheckedBS;

   // RM_MEMORY_SWITCH_T eBSBufferOperation;
   // RM_MEMORY_SWITCH_T eFrmBufferOperation;

    unsigned int u4MaxFrameWidth;
    unsigned int u4MaxFrameHeight;	
    unsigned int u4FrameBufferSize;
    unsigned int u4BufferNumber;
    unsigned int u4DecodeFrameNumber;
    UINT64 u8NextDisplayTimeStamp;    
    UINT64 u8LastDisplayTimeStamp;
    UINT64 u8PreviousDisplayTR;
    UINT64 u4LastSyncTimeStamp;
    alg_cntx_hdr_st rScratches;  
    payload_inf_st arInputParam[MAX_PAYLOAD_NUM];
    RM_CODEC_API_T rCodecAPI;
    UINT64 u8SequenceNumber;

    unsigned int u4NumPrivateOutput;
    unsigned int u4CurPrivateOutput;
    RM_PRIVATE_OUTPUT_T rPrivateOutput[RV_DECODE_OUTPUT_BUF_SIZE+1];

    unsigned short u2LastDisplayFrameWidth;
    unsigned short u2LastDisplayFrameHeight;
    void * hDrv;
    VCODEC_DEC_CALLBACK_T pfnCallback;
    //RV9_DEC_CUSTOM_SETTING_T *prCustomValue;
	unsigned int u4MaxSupportFrameWidth;
	unsigned int u4MaxSupportFrameHeight;	
	unsigned int ExternalMEMSize;
	unsigned int Input_Flag;	
	unsigned int u4PreViewNormalModeFrameWidth;
	unsigned int u4PreViewNormalModeFrameHeight;
    //debug info
    unsigned int u4SkipNumber;
    unsigned int u4ErrNumber;
}RM_DECODER_CTRL_T;


typedef struct
{
    //LOCAL_PARA_HDR 
    unsigned int u4Param1;    
    unsigned int u4Param2;  
}RM_DECODER_IND_T;

#if 0
typedef enum
{
    I_FRAME = 0,
    P_FRAME = 1,
    B_FRAME = 2    
}RM_FRAME_TYPE_T;



typedef enum
{
    SKIP_ALWAYS = 1,
    SKIP_NEVER = 0,
    SKIP_SOMETIMES = 2
}RM_SKIP_SETTING_T;
#endif

typedef enum
{
    NO_SKIP_B = 0,
    SKIP_B = 1
}RM_IS_SKIP_B_FRAME_T;



//#endif /* __RV_DEC_SW_SUPPORT__*/

#endif /* RM_DECODER_COMPONENT_V2_H */ 

