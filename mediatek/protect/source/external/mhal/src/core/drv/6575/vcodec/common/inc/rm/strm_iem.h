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

#ifndef __IEM_H
   //#error strm_iem.h is not included inside iem.h !
#endif

#ifndef __STRM_IEM_H
#define __STRM_IEM_H
#include "decdefs.h"
#include "rvstatus.h"
#include "rvtypes.h"
#include "vcodec_if.h"
/*****************************************************************************
 * Structure Definition
 *****************************************************************************/
typedef struct _stream_payload_info_struct_ {
   U32               address;
   U32               length;
} payload_inf_st;

/*****************************************************************************
 * Video Driver interface Structure Definition
 *****************************************************************************/
#define RV_DECODE_OUTPUT_BUF_SIZE 2
#define MAX_NUM_RPR_SIZES 8

#define TR_WRAP  	256
#define TR_WRAP_RV  8192
    /* RealVideo bitstreams as of "G2 with SVT" use a 13-bit TR, */
    /* giving millisecond granularity.  RealVideo "G2 Gold", and */
    /* RV89Combo use an 8-bit TR. */
#if 0
enum Max_support_size_idx_enum {   
	RV_RESOLUTION_720x576,
	RV_RESOLUTION_800x480,
	RV_RESOLUTION_720x480,
	RV_RESOLUTION_640x480,
	RV_RESOLUTION_320x480,
	RV_RESOLUTION_352x288,
	RV_RESOLUTION_400x240,
	RV_RESOLUTION_320x240,
	RV_RESOLUTION_176x144,
	NUM_Support_Size
};
enum Max_support_size_idx_enum {   
	RESOLUTION_176x144,    //0  QCIF  
	RESOLUTION_320x240,    //1  QVGA  
	RESOLUTION_400x240,    //2  WQVGA 
	RESOLUTION_352x288,    //3  CIF   
	RESOLUTION_320x480,    //4  HVGA  
	RESOLUTION_640x480,    //5  VGA   
	RESOLUTION_720x480,    //6  D1    
	RESOLUTION_720x576,    //7  D1    
	RESOLUTION_800x480,    //8  WVGA  	
	NUM_Support_Size
};
#else

#endif

typedef struct _RV_open_info_struct_ {
	U32				Max_Dim_Width;
	U32				Max_Dim_Height;
	U32				Max_Support_Size;
	U32				Max_Support_Width;
	U32				Max_Support_Height;
	U32				Num_RPR_Sizes;                 //Reserved for RV8 
	U32				RPR_Sizes[2 * MAX_NUM_RPR_SIZES];//Reserved for RV8 	
	U32				RV_quality_Level;
	U32            Total_Buffer_Size;
    #if defined(RV_RV8_SUPPORT)
    U32 isRV8;
    #endif // RV8
} RV_open_info_st;


typedef struct _RV_stream_decode_info_struct_{
	//Bitstream buffer adr
	//U8					    * BS_Buffer_adr;
	VCODEC_BUFFER_T			rBS_Buffer_adr;
	//RV stream data total size of frame
    U32						BS_total_size; 		
    //Frame buffer adr
  	U8	*pFrameY;
	U8	*pFrameU;
	U8	*pFrameV;
// modify by SK,2011/2/23 ¤U¤È 01:03:51	
#if  1//defined(_RV9_ADAPTIVE_FRAME_DEBLOCKING_)||defined(_RV9_SkipFrame_)||defined(_RV_VAR_DIM_MODE_)
	I32 buffer_stock;
	I32 Total_Frame_Buffers;
	U32 skipB_FrameBuf;
#endif

} RV_strm_decode_info_st;



typedef struct _RV_stream_decode_struct_ {
   U32						seqno;
   payload_inf_st			* first_payload_info;   
   U32						num_payload_info;  
   RV_strm_decode_info_st   decode_info;   
} RV_strm_decode_st;


typedef struct _RV_decode_frame_out_struct_{
	U32			Dec_Frame_Width;
   	U32			Dec_Frame_Height;
	U32			DecPicCodeType;			// 0: I-frame, 1: P-frame, 2: B-frame
	U32			DisplayPicCodeType;		// 0: I-frame, 1: P-frame, 2: B-frame
	U32			DisplayPicCodeType_org; // 0: I-frame, 1: P-frame, 2: B-frame
	
	/*enum */RV_DECODE_STATUS 	Decode_Status;         //decode ok or return error code.
	
	U32			Display_Frame_Width;
	U32			Display_Frame_Height;
	U32			Display_Frame_Width_Full;
	U32			Display_Frame_Height_Full;

	U32			Display_time;
	U32			Display_cnt;           //Display_cnt   0:hold ; 1:show
	U32			Display_skipB;         //Display_skipB 1:frame skip 0:frame decoded		
	
	U8			*pDisplayFrame_Y;
	U8			*pDisplayFrame_U;
	U8			*pDisplayFrame_V;
	
	U8			*pFreeFrame_Y;
#ifdef _RV_DEBUG_INFO_
	U32			Dec_ulFrameChecksumY;                                   
	U32			Dec_ulFrameChecksumU;
	U32			Dec_ulFrameChecksumV;        
#endif

}RV_decode_frame_out_st;



typedef struct _RV_decode_info_struct_ {	
	U32        Num_Output;
	RV_decode_frame_out_st			*pOutputInfoBuf;
	// module time profile , debug info.
#if defined(WIN32) || defined(ARMULATOR)
	U32 		total_dec_time;
	U32			vld_time;
	U32        it_time;
	U32 		ipmc_time;		
	U32 		mc_time;
	U32 		deblock_time;
	U32 		bufwb_time;
	U32 		ref_move_time;
	U32 		ref_wait_time;
	U32 		MVref_wait_time;
	U32 		wait_done_cnt;	
#endif		
} RV_decode_info_st;

typedef struct _RV_vdec_decode_finish_indication_struct_ {
   U8                seqno;
   RV_decode_info_st decode_info;
} RV_vdec_decode_finish_ind_st;

typedef struct _RV_vdec_close_request_struct_ {
	U32			DecPicCodeType;			// 0: I-frame, 1: P-frame, 2: B-frame
	U32			DisplayPicCodeType;
	U32			DisplayPicCodeType_org;	
	/*enum */RV_DECODE_STATUS Decode_Status;//decode ok or return error code.	
	U32			Display_Frame_Width;
	U32			Display_Frame_Height;
   	U32			Dec_Frame_Width;
   	U32			Dec_Frame_Height;
	U32			Display_cnt;           //Display_cnt 2:frame skip 1:frame decoded
	U8			*pDisplayFrame_Y;	
	U8			*pFreeFrame_Y;
} RV_vdec_close_req_st;

enum rv_scenario_enum {
    RV_DECODER,
    NUM_SCENARIO
};
enum scratch_idx_enum {

   SC_0_AT_L1,          //memory segment 1    
   SC_1_AT_L20,         //memory segment 2    
   SC_2_AT_L21,         //memory segment 3    
   SC_3_AT_EXT,			//External memory
   SC_4_AT_CONTX,		//memory segment 0    
   NUM_SCRATCH_BUFFERS
 };

enum memory_type_idx_enum {   
	INTERNAL_MEM_1,        //internal memory type 1       
	INTERNAL_MEM_2,        //internal memory type 2 
   INTERNAL_MEM_TCM_1,    	//internal memory type 1 
   INTERNAL_MEM_TCM_2,    	//internal memory type 2 
   INTERNAL_MEM_TCM_3,    	//internal memory type 3 
	EXTERNAL_MEM_1,			//External memory   
	NUM_MEMORY_TYPE
};

typedef struct {
   void  *addr;
   U32   length;
   U32   MemoryType;
#if defined(RV_HW_DEBLOCK)
    U32 phyAddr;
#endif // HW
} scratch_info_st;

typedef struct {
   scratch_info_st   scratch[NUM_SCRATCH_BUFFERS];
} alg_cntx_hdr_st;

typedef struct _RV_query_info_struct_ {	
	scratch_info_st memory_info[NUM_SCRATCH_BUFFERS];
	U32			Max_Support_Dim_Width;
	U32			Max_Support_Dim_Height;
	RV_Boolean	Bitstream_buffer_cacheable;
	RV_Boolean	YUV_buffer_cacheable;
} RV_query_info_st;


#endif

