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

#ifndef __MED_VT_STRUCT__
#define  __MED_VT_STRUCT__

#ifndef __VT_SWIP__
#define LOCAL_PARA_HDR \
   kal_uint8	ref_count; \
   kal_uint16	msg_len;
#endif

typedef enum
{
    VT_VIDEO_CAP_UNKNOWN_VIDEO=-1,
    VT_VIDEO_CAP_H263=0,
    VT_VIDEO_CAP_H264,
    VT_VIDEO_CAP_MPEG4
} vt_enum_video_capability;

typedef enum
{
    VT_AUDIO_CAP_UNKNOWN_AUDIO  = -1,
    VT_AUDIO_CAP_AMR = 0,
    /* AMR WIDEBAND */
    VT_AUDIO_CAP_G7222
} vt_enum_audio_capability;

typedef enum
{
    VT_VIDEO_SIZE_UNKNOWN = -1,
    VT_VIDEO_SIZE_SQCIF = 0,
    VT_VIDEO_SIZE_QCIF
}vt_video_size_enum;

typedef struct
{
    kal_uint8 sqcifMPI; /* sqcifMPI (1..32) */
    kal_uint8 qcifMPI;  /* sqcifMPI (1..32) */
    vt_video_size_enum vt_video_size;
    kal_uint32 max_bit_rate;
} vt_struct_H263_capability;

//mtk81058/20110525,CMCC 7.1.7.6 request
typedef enum
{
	INIT,
	ACTIVE,
	INACTIVE
}vt_video_replace_active_enum;

typedef struct
{
    kal_uint8   session_id;
    kal_uint32 channel_num;
	vt_video_replace_active_enum isactive;
    vt_enum_video_capability video_type;
    vt_struct_H263_capability h263_cap;
    kal_uint32 max_packet_size;
} vt_struct_video_capability;

typedef struct
{
    kal_uint8   session_id;
    kal_uint32 channel_num;
    vt_enum_audio_capability audio_type;
    kal_uint32 max_packet_size;
} vt_struct_audio_capability;

typedef struct
{
    LOCAL_PARA_HDR
    vt_struct_video_capability input_video_chl;
    vt_struct_video_capability output_video_chl;
    vt_struct_audio_capability input_audio_chl;
    vt_struct_audio_capability output_audio_chl;
} media_vcall_channel_status_ind_struct;

typedef enum
{
    VT_VIDEO_QUALITY_UNKNOW  = -1,
    VT_VIDEO_QUALITY_FINE = 0,
    VT_VIDEO_QUALITY_NORMAL ,
    VT_VIDEO_QUALITY_LOW
} vt_video_quality_enum;

typedef enum
{
    VT_CHAN_TYPE_INVALID = 0,
    VT_CHAN_TYPE_INCOMING_AUDIO,
    VT_CHAN_TYPE_INCOMING_VIDEO,
    VT_CHAN_TYPE_OUTGOING_AUDIO,
    VT_CHAN_TYPE_OUTGOING_VIDEO
}vt_channel_type_enum;

#define VT_VIDEO_QUALITY_SCALE	11

typedef struct
{
    LOCAL_PARA_HDR
    vt_video_quality_enum  vt_video_quality;
}media_vt_adjust_video_quality_ind_struct;

typedef struct
{
    LOCAL_PARA_HDR
    vt_video_size_enum  vt_video_size;
}media_vt_switch_video_size_ind_struct;

typedef struct
{
    LOCAL_PARA_HDR
    kal_uint8  vt_session_id;
}vt_med_video_pause_struct;

typedef struct
{
    LOCAL_PARA_HDR
    kal_uint8  vt_session_id;
}vt_med_video_resume_struct;

typedef struct vt_loopback_video_data_struct{
    LOCAL_PARA_HDR
    kal_uint32 data_size;
    kal_uint8 raw_data [8000];
}
vt_loopback_video_data_struct;

typedef struct vt_loopback_audio_data_struct{
    LOCAL_PARA_HDR
    kal_uint32 data_size;
    kal_uint8 raw_data [50];
}
vt_loopback_audio_data_struct;


typedef struct
{
    LOCAL_PARA_HDR
    kal_uint8 vt_session_id;
} media_vcall_video_replenish_data_ready_ind_struct;

typedef struct
{
    LOCAL_PARA_HDR
    vt_channel_type_enum    chan_type;
    kal_uint8   session_id;
}med_vt_codec_open_ack_struct;

typedef struct
{
    LOCAL_PARA_HDR
    kal_uint8* data; 
    kal_uint32 size;
    kal_uint8 pkt_type;
    kal_uint8 session_id;
}
med_vt_put_ul_video_ind_struct;

typedef enum
{    
    VT_VQ_SHARP = 0,
    VT_VQ_NORMAL,
    VT_VQ_SMOOTH,
    VT_VQ_OPTION_END
}vt_vq_option_enum;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    vt_vq_option_enum vq_option;
}med_vt_video_misc_cmd_struct;


#ifdef __VT_SWIP__
#define VT_DOWNLINK_VIDEO_Q_HDR_SIZE (256)
typedef struct {
   
    //mpeg4 information
    kal_uint8 mp4_session_hdr[VT_DOWNLINK_VIDEO_Q_HDR_SIZE];
    kal_uint32 mp4_session_hdr_length;
    kal_uint8 vt_dl_video_session_id;    
}
vt_downlink_video_Q_struct;


typedef enum{
	VIDEO_CALL_STATUS_OK = 0,
	VIDEO_CALL_STATUS_CODEC_UNSUPPORT,
	VIDEO_CALL_STATUS_NOT_CODED,
	VIDEO_CALL_STATUS_BUFFER_UNAVIL,
	VIDEO_CALL_STATUS_ERROR,
	VIDEO_CALL_STATUS_ENCODER_CLOSED,
	VIDEO_CALL_STATUS_MAX    
}VIDEO_CALL_STATUS;

typedef enum
{
    VIDEO_CALL_CODEC_NONE,
    VIDEO_CALL_CODEC_MPEG4 = 1,
    VIDEO_CALL_CODEC_H263 = 2,
    VIDEO_CALL_CODEC_H264 = 4,	
    VIDEO_CALL_CODEC_VC1 = 8
}VIDEO_CALL_CODEC_TYPE;
#endif //end-of-wince

#endif
