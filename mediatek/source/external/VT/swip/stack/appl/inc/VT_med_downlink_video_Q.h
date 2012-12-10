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

#ifndef _MED_VT_Q_H
#define _MED_VT_Q_H
#include "l1audio.h"
#include "bmd.h"
#ifdef __VIDEO_ARCHI_V2__
#include "video_vt_api_v2.h"
#else
#include "video_call_if.h"
#endif
#include "kal_release.h"
#include "VT_common_enum.h"

/*-------------------------------------------------------------------------
*    #define
*------------------------------------------------------------------------*/

#define VT_DOWNLINK_VIDEO_Q_FRAME_SIZE (10 *1024)
#define VT_DOWNLINK_VIDEO_Q_FRAME_NUM (50)
#define VT_DOWNLINK_VIDEO_Q_HDR_SIZE (256)
#define VT_DOWNLINK_VIDEO_Q_BUFFER_SIZE (10 *1024 *6)
/*-------------------------------------------------------------------------
*    #define function
*------------------------------------------------------------------------*/

#define VT_DOWNLINK_VIDEO_Q_STATE_TRANS(Q, s, d) do\
{ kal_take_mutex(Q->mid);\
    if(Q->state==s)\
        Q->state=d;\
kal_give_mutex(Q->mid);}while(0)

#define VT_DOWNLINK_VIDEO_Q_STATE_TRANS_ASSERT(Q, s, d, a) do\
{kal_take_mutex(Q->mid);\
    ASSERT(Q->state!= a);\
    if(Q->state==s)\
        Q->state=d;\
kal_give_mutex(Q->mid);}while (0)

#define VT_DOWNLINK_META_Q_ROLLBACK(Q) do\
{if(Q->w_idx == 0)\
    Q->w_idx = VT_DOWNLINK_VIDEO_Q_FRAME_NUM - 1;\
  else\
    (Q->w_idx)--;\
}while(0) 
/*-------------------------------------------------------------------------
*    extern function
*------------------------------------------------------------------------*/

extern VIDEO_CALL_STATUS VideoCall_ParsePacket(
    kal_uint8 *p_data, 
    kal_uint32 length, 
    Video_Call_Parse_Info *p_info);


/*-------------------------------------------------------------------------
*    struct
*------------------------------------------------------------------------*/    

typedef struct
{
    kal_bool b_new_frame;
    kal_bool b_sync_frame;
    kal_uint32 width;
    kal_uint32 height;
    kal_uint32 real_width; 
    kal_uint32 real_height;
    kal_uint32 duration;
    kal_uint32 size;
    kal_uint8* data;
    kal_bool b_CRC_error;
    kal_bool b_ready;
}
vt_downlink_video_Q_meta_struct;

/*
typedef struct{
    kal_uint8 data[VT_DOWNLINK_VIDEO_Q_FRAME_SIZE];
}
vt_downlink_video_Q_frame_struct;
*/
typedef struct {
    vt_downlink_video_Q_meta_struct meta[VT_DOWNLINK_VIDEO_Q_FRAME_NUM];
//    vt_downlink_video_Q_frame_struct frames[VT_DOWNLINK_VIDEO_Q_FRAME_NUM];
    kal_uint8 buffer[VT_DOWNLINK_VIDEO_Q_BUFFER_SIZE];  
    kal_uint32 buffer_write_idx;
    kal_uint32 buffer_read_idx;
    kal_uint32 w_idx;
    kal_uint32 r_idx;
    kal_uint32 q_size;
    kal_mutexid mid;
    kal_bool vt_IsFrameHeaderError;
    kal_bool vt_is_prev_new_session;
    kal_bool vt_is_hdr_not_complete;
    kal_bool vt_is_new_session;
    kal_bool vt_is_drop;
    vt_downlink_video_Q_state_enum state;
    vt_downlink_video_Q_frame_state_enum enqueue_state;
    
    //mpeg4 information
    kal_uint8 mp4_session_hdr[VT_DOWNLINK_VIDEO_Q_HDR_SIZE];
    kal_uint32 mp4_session_hdr_length;
    kal_uint8 vt_dl_video_session_id;

    //H263 Information
    kal_uint32 h263_height;
    kal_uint32 h263_weight;
}
vt_downlink_video_Q_struct;
/*-------------------------------------------------------------------------
*    extern struct
*------------------------------------------------------------------------*/    

extern  vt_downlink_video_Q_struct MED_VT_DL_video_Q;

/*-------------------------------------------------------------------------
*    function declare
*------------------------------------------------------------------------*/    

/*-------------------------------------------------------------------------
*
*------------------------------------------------------------------------*/


kal_bool  vt_downlink_video_Q_attach_to_cur_frame( 
    vt_downlink_video_Q_struct *queue,
    kal_uint8* p_buff,
    kal_uint32 * buff_len,
    kal_bool is_any_error);

kal_bool  vt_downlink_video_Q_get_frame_to_write( 
    vt_downlink_video_Q_struct *queue );

kal_bool vt_downlink_video_Q_write_cur_meta( 
    vt_downlink_video_Q_struct *queue,
    Video_Call_Parse_Info *p_info);

kal_bool vt_downlink_video_Q_update_prev_meta(
    vt_downlink_video_Q_struct *queue,
    Video_Call_Parse_Info *p_info);

void vt_downlink_video_Q_init(void);

void vt_downlink_video_Q_reset(
    vt_downlink_video_Q_struct *queue);

void vt_downlink_video_Q_enqueue_packet(
    kal_uint8 *pBuffer,
    kal_uint32  size,
    kal_bool is_any_error
    );

void vt_downlink_video_Q_pause(kal_uint8 session_id);

void vt_downlink_video_Q_resume(kal_uint8 session_id);

void vt_downlink_video_Q_roll_back(void);

#ifndef VT_DL_VIDEO_Q_DEBUG
#define VT_DL_VIDEO_Q_DEBUG
#endif

#endif

