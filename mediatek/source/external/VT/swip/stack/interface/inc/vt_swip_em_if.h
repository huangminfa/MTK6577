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

#ifndef VT_SWIP_EM_IF_H
#define VT_SWIP_EM_IF_H

#include "vt_kal_def.h"


// ----------------------------------------
// Enum
// ----------------------------------------
typedef enum {
    VT_EM_REVERSE_INVALID = -1,
    VT_EM_REVERSE_DATA = 0,
    VT_EM_REVERSE_NULL
} vt_em_reverse_channel_data_type;

typedef enum {
    VT_EM_AL_INVALID = -1,
    VT_EM_AL2_WITH_SEQUENCE_NUMBER = 0,
    VT_EM_AL2_WITHOUT_SEQUENCE_NUMBER,
    VT_EM_AL3_CONTROL_FIELD_SIZE_0,
    VT_EM_AL3_CONTROL_FIELD_SIZE_1,
    VT_EM_AL3_CONTROL_FIELD_SIZE_2
} vt_em_al_choice;

typedef enum {
    VT_EM_MUX_LEVEL_INVALID = -1,
    VT_EM_MUX_LEVEL_0 = 0,
    VT_EM_MUX_LEVEL_1,
    VT_EM_MUX_LEVEL_1_WITH_DOUBLE_FLAG,
    VT_EM_MUX_LEVEL_2,
    VT_EM_MUX_LEVEL_2_WITH_OPTIONAL_HEADER,
    VT_EM_MUX_LEVEL_3,
    VT_EM_MUX_LEVEL_3_WITH_OPTIONAL_HEADER
} vt_em_mux_level_choice;

typedef enum {
    VT_EM_WNSRP_INVALID = -1,
    VT_EM_WNSRP_OFF = 0,
    VT_EM_WNSRP_ON
} vt_em_wnsrp_choice;

typedef enum {
    VT_EM_CODEC_INVALID = -1,
    VT_EM_CODEC_MPEG4_H263 = 0,
    VT_EM_CODEC_H263_MPEG4
} vt_em_video_codec_preference_choice;

typedef enum {
    VT_EM_AUTO_DROP_INVALID = -1,
    VT_EM_DISABLE_AUTO_DROP_IF_CHANNEL_FAIL = 0,
    VT_EM_ENABLE_AUTO_DROP_IF_CHANNEL_FAIL
} vt_em_auto_drop_if_channel_fail_choice;

typedef enum {
    VT_EM_TERMINAL_TYPE_INVALID = -1,
    VT_EM_TERMINAL_TYPE_NORMAL = 0,
    VT_EM_TERMINAL_TYPE_MASTER,
    VT_EM_TERMINAL_TYPE_SLAVE
} vt_em_terminal_type_choice;

typedef enum {
    VT_EM_DATA_TYPE_INVALID = -1,
    VT_EM_DATA_TYPE_AUDIO = 0,
    VT_EM_DATA_TYPE_VIDEO
} vt_em_channel_data_type;

typedef enum {
    VT_EM_CALL_STATE_INIT = -1,
    VT_EM_CALL_STATE_SYNCHRONIZED = 0,
    VT_EM_CALL_STATE_CONNECTED,
    VT_EM_CALL_STATE_CONNECTEDCHANNELS,
    VT_EM_CALL_STATE_IDLE,
    VT_EM_CALL_STATE_RESETTING,
    VT_EM_CALL_STATE_LOSTSYNC
} vt_em_call_state_choice; /* Refer to Rv3G324mCallState type */

/* Radvision strucuture independent */
typedef enum {
    VT_EM_MASTER_SLAVE_STATUS_INVALID = -1,
    VT_EM_SLAVE = 0,
    VT_EM_MASTER
} vt_em_master_slave_status_choice;

typedef enum {
    VT_EM_CODEC_TYPE_Unknown = -1,
    VT_EM_CODEC_TYPE_Amr,
    VT_EM_CODEC_TYPE_Mpeg4,
    VT_EM_CODEC_TYPE_H264,
    VT_EM_CODEC_TYPE_H239Control,
    VT_EM_CODEC_TYPE_H239ExtendedVideo,
    VT_EM_CODEC_TYPE_G7221,
    VT_EM_CODEC_TYPE_G726,
    VT_EM_CODEC_TYPE_H324AnnexI,
    VT_EM_CODEC_TYPE_SessionReset,
    VT_EM_CODEC_TYPE_G7222,
    VT_EM_CODEC_TYPE_G7231,
    VT_EM_CODEC_TYPE_H263,
    VT_EM_CODEC_TYPE_G711,
    VT_EM_CODEC_TYPE_G722,
    VT_EM_CODEC_TYPE_G729,
    VT_EM_CODEC_TYPE_H249NavigationKey,
    VT_EM_CODEC_TYPE_H249SoftKeys,
    VT_EM_CODEC_TYPE_H249PointingDevice,
    VT_EM_CODEC_TYPE_H249ModalInterface,
    VT_EM_CODEC_TYPE_Last
} vt_em_codec_type;

typedef enum {
    VT_EM_RETRANSMISSION_PROTOCOL_INVALID = -1,
    VT_EM_RETRANSMISSION_PROTOCOL_SRP = 0,
    VT_EM_RETRANSMISSION_PROTOCOL_NSRP,
    VT_EM_RETRANSMISSION_PROTOCOL_WNSRP
} vt_em_retransmission_protocol_choice;

typedef enum {
    VT_EM_RESOLUTION_INVALID = -1,
    VT_EM_RESOLUTION_SQCIF = 0,
    VT_EM_RESOLUTION_QCIF,
    VT_EM_RESOLUTION_CIF
} vt_em_video_resolution_choice;

typedef enum {
    VT_EM_H223_AL_TYPE_UNKNOWN = -1,
    VT_EM_H223_AL_TYPE_1 = 0,
    VT_EM_H223_AL_TYPE_2,
    VT_EM_H223_AL_TYPE_3
} vt_em_al_type;

typedef enum {
    VT_EM_RP_UNKNOWN = -1,
    VT_EM_RP_COMMAND = 0,
    VT_EM_RP_RESPONSE
} vt_em_xSRP_data_type;

// ----------------------------------------
// Struct
// ----------------------------------------
typedef struct {
    kal_uint16 lcn;
    kal_uint8   is_chan_duplex;
    vt_em_codec_type   codec_type;
    vt_em_video_resolution_choice  resolution_choice;
    vt_em_al_type forward_al;
    kal_int32   max_sdu_size;
} vt_em_video_channel_info_struct;

typedef struct {
    kal_uint16 lcn;
    kal_uint8   is_chan_duplex;
    vt_em_codec_type   codec_type;
    vt_em_al_type forward_al;
    kal_int32   max_sdu_size;
} vt_em_audio_channel_info_struct;

typedef struct {
    kal_uint8 sq;
    vt_em_xSRP_data_type type;
} vt_em_outgoing_xSRP;

typedef struct {
    kal_uint8 sq;
    vt_em_xSRP_data_type type;
} vt_em_incoming_xSRP;

typedef struct {
    kal_uint32 vt_csr_DL_Q_frame_num;
    kal_uint32 vt_csr_UL_Q_frame_num;
    kal_uint32 vt_MED_DL_Q_frame_num;
    kal_uint32 vt_MED_DL_Q_state;

    kal_uint32 vt_DL_video_frame_num;
    kal_uint32 vt_DL_video_frame_size;
    kal_uint32 vt_DL_video_I_frame_num;
    kal_uint32 vt_DL_video_P_frame_num;

    kal_uint32 vt_DL_audio_frame_num;
    kal_uint32 vt_DL_audio_frame_size;

    kal_uint32 vt_UL_video_frame_num;
    kal_uint32 vt_UL_video_frame_size;
    kal_uint32 vt_UL_video_drop_frame_num;
    kal_uint32 vt_UL_video_drop_frame_size;

    kal_uint32 vt_UL_audio_frame_num;
    kal_uint32 vt_UL_audio_frame_size;
    kal_uint32 vt_UL_audio_drop_frame_num;
    kal_uint32 vt_UL_audio_drop_frame_size;

    kal_uint32 vt_UL_csr_size;
    kal_uint32 vt_UL_csr_num;
    kal_uint32 vt_DL_csr_size;
    kal_uint32 vt_DL_csr_num;

    kal_uint32 vt_downlink_mc_tbl[16];  /* # pkts using this mc entry */
    kal_uint32 vt_uplink_mc_tbl[16];

    kal_uint32 vt_ra_add_failures;
    kal_uint32 vt_incoming_audio_crc_errors;
    kal_uint32 vt_incoming_video_crc_errors;
    kal_uint32 vt_incoming_control_crc_errors;
    kal_uint32 vt_incoming_control_total_packets;
} vt_em_statistic_info_struct;

#if 0
// currently not used.
typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    em_info_enum em_info;
} l4cvt_em_display_ind_struct;   /* MSG: VT to L4C */
#endif

typedef struct {
    vt_em_mux_level_choice mux_level_choice;
    vt_em_wnsrp_choice  wnsrp_choice;
    vt_em_al_choice audio_al_choice;
    vt_em_al_choice video_al_choice;
    vt_em_reverse_channel_data_type video_reverse_data_type_choice;
    vt_em_video_codec_preference_choice video_codec_preference_choice;
    vt_em_auto_drop_if_channel_fail_choice auto_drop_if_channel_fail_choice;
    vt_em_terminal_type_choice  terminal_type_choice;
    kal_int32 timer_T101; //default =  -1  (if its value equals -1, no change should be made)
    kal_int32 timer_T109; //default =  -1  (if its value equals -1, no change should be made)
    kal_int32 timer_T401; //default =  -1  (if its value equals -1, no change should be made)
    kal_int32 user_specified_1; //default =  -1  (if its value equals -1, no change should be made)
    kal_int32 user_specified_2; //default =  -1  (if its value equals -1, no change should be made)
    kal_uint8 user_specified_3[64]; //get input length by strlen() (end by EOF)
} vt_em_config_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    vt_em_config_struct em_config;
} l4cvt_em_set_config_req_struct;   /* MSG: MSG_ID_L4C_VT_EM_SET_CONFIG_REQ */

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    vt_em_config_struct em_config;
} l4cvt_em_get_config_cnf_struct;   /* MSG: MSG_ID_L4C_VT_EM_GET_CONFIG_CNF */

#endif

