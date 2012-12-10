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

#ifndef VT_COMMON_ENUM_H
#define VT_COMMON_ENUM_H
typedef enum{
    VT_MO_DISC=0,
    VT_MT_DISC,
    VT_3G324M_DISC_ABNORMAL,
    VT_3G324M_DISC_NORMAL
}
vt_call_end_type_enum;

typedef enum{
    VT_CALL_DIAL_FAIL=0
}
vt_activate_cfn_cause_enum;

typedef enum{
    VT_CALL_DROP_FAIL=0
}
vt_deactivate_cfn_cause_enum;

typedef enum{
    VT_EVENT_TIMER_ID=0,
    VT_F_CSR_TIMER_ID
}
vt_timer_id_enum;

typedef enum
{
    VCALL_DATA_SESSION_ID_UNMATCH=-1,
    VCALL_DATA_INIT=0,
    VCALL_DATA_STARVATION,
    VCALL_DATA_REPLENISH,
    VCALL_DATA_CONSUMING,
    VCALL_DATA_PAUSE,
    VCALL_DATA_RESUME,
    VCALL_DATA_TOTAL_STATE
} 
vt_downlink_video_Q_state_enum;

typedef enum
{
    VT_STATE_INIT=0,
    VT_STATE_NORMAL,
    VT_STATE_WAIT_I,
    VT_STATE_PAUSE,
    VT_STATE_ERROR,
    VT_STATE_NOT_COMPLETE
} 
vt_downlink_video_Q_frame_state_enum;
typedef enum
{
    VT_DL_VIDEO_PKT_I_FRAME = 0,
    VT_DL_VIDEO_PKT_P_FRAME,
    VT_DL_VIDEO_PKT_CONT_FRAME,
    VT_DL_VIDEO_PKT_ERROR_FRAME,
    VT_DL_VIDEO_PKT_NOT_COMPLETE_FRAME
} 
vt_downlink_video_packet_type_enum;

typedef enum{
    VT_NO_LOOPBACK = 0,
    VT_MEDIA_LOOPBACK_AUDIO = 1,
    VT_MEDIA_LOOPBACK_VIDEO = 2,
    VT_MEDIA_LOOPBACK_ALL   = 3,    /* (VT_MEDIA_LOOPBACK_AUDIO | VT_MEDIA_LOOPBACK_VIDEO) mtk02651: Fix RVCT Warning */ 
    VT_NETWORK_LOOPBACK_AUDIO = 4,
    VT_NETWORK_LOOPBACK_VIDEO = 8,
    VT_NETWORK_LOOPBACK_ALL   = 12  /* (VT_NETWORK_LOOPBACK_AUDIO | VT_NETWORK_LOOPBACK_VIDEO) mtk02651: Fix RVCT Warning */ 
}
vt_loopback_mode_enum;

typedef enum{
    VT_NSRP_INIT = 0,
    VT_NSRP_GENERATE = 1,
    VT_NSRP_SENT,
    VT_NSRP_UNKNOW    
} 
vt_nsrp_state_enum;

typedef enum
{
    VT_UL_VIDEO_PKT_I_FRAME_HDR = 0,
    VT_UL_VIDEO_PKT_P_FRAME_HDR,
    VT_UL_VIDEO_PKT_CONT_FRAME,
    VT_UL_VIDEO_PKT_MAX_TYPE
} 
vt_uplink_video_packet_type_enum;

typedef enum{
    VT_UL_VIDEO_WAIT_HDR = 0,
    VT_UL_VIDEO_NORMAL,
    VT_UL_VIDEO_DROP
} vt_uplink_video_drop_state_enum;

typedef enum
{
    VT_UL_VIDEO_NO_DROP = 0,
    VT_UL_VIDEO_DROP_P,
    VT_UL_VIDEO_DROP_ANY
} 
vt_uplink_video_drop_status_enum;

typedef enum
{
    VT_ENCODER_MPEG4 = 0,
    VT_ENCODER_H263,
    VT_ENCODER_MPEG4_VT,
    VT_ENCODER_H263_VT,
    VT_ENCODER_RECORD_YUV,
    VT_ENCODER_UNKNOWN
}
vt_encoder_type_enum;

typedef enum{
    vt_command_end =-1,
    vt_video_input_on = 1,
    vt_video_output_on,
    vt_video_input_off,
    vt_video_output_off
}vt_loopback_command_enum;

#endif //VT_COMMON_ENUM_H
