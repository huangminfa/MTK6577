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

#ifndef __VT_L4C_EM_H__
#define  __VT_L4C_EM_H__

#include "vt_kal_def.h"

/**************************************************************************/
/******************************  EM Configuration  ****************************/
/**************************************************************************/
kal_int32 vt_em_get_channel_al(IN void* termObj, kal_int32 dataType);
kal_int32 vt_em_get_reverse_channel_type(IN void* termObj,kal_int32 dataType);
kal_int32 vt_em_get_mux_level(IN void* termObj);
kal_int32 vt_em_get_WNSRP(IN void* termObj);
kal_int32 vt_em_get_codec_priority(IN void* termObj);
kal_int32 vt_em_get_terminal_type(IN void* termObj);

void vt_em_set_channel_al(IN void* termObj, kal_int32 dataType, kal_int32 al_choice);
void vt_em_set_mux_level(IN void* termObj, kal_int32 mux_level_choice);
void vt_em_set_WNSRP(IN void* termObj, kal_int32 choice);
void vt_em_set_reverse_channel_type(IN void* termObj, kal_int32 dataType, kal_int32 type_choice);
void vt_em_set_codec_priority(IN void* termObj, kal_int32 firstPriority, kal_int32 secondPriority);
void vt_em_set_terminal_type(IN void* termObj, kal_int32 terminalType);
/* Unspecified */
void vt_em_set_nsrp_retransmission_on_idle(IN void* termObj, kal_int32 retransmission_on_idle_choice);
void vt_em_set_nsrpTimer(IN void* termObj, IN kal_int32 newNSRPTimer);
void vt_em_set_n400Counter(IN void* termObj, IN kal_int32 value);
void vt_em_set_mpc_capability(IN void* termObj, IN RvInt16 mpcMediaTypes);
void vt_em_set_acp_audio_entry(IN void* termObj,IN RvUint8 audioEntry);
void vt_em_set_acp_video_entry(IN void* termObj,IN RvUint8 audioEntry);
void vt_em_set_use_mona(IN void* termObj, IN RvInt32 value);
void vt_em_set_use_acp(IN void* termObj, IN RvInt32 value);
/**************************************************************************/
/********************************  EM Display  *******************************/
/**************************************************************************/
#if defined(__VT_USE_STAT__) && !defined(__VT_SWIP__)
/* VT_EM_DISPLAY_TYPE_CALL_STATE */
void vt_l4c_em_report_call_state(kal_int32 value);

/* VT_EM_DISPLAY_TYPE_MASTER_SLAVE_STATUS */
void vt_l4c_em_report_master_slave_status(kal_int32 value);

/* VT_EM_DISPLAY_TYPE_RETRANSMISSION_PROTOCOL */
void vt_l4c_em_report_retransmission_protocol(kal_int32 value);

/* VT_EM_DISPLAY_TYPE_INCOMING_AUDIO_CHANNEL_INFO */
/* VT_EM_DISPLAY_TYPE_OUTGOING_AUDIO_CHANNEL_INFO */
void vt_l4c_em_report_audio_channel_info(
    kal_bool    isIncoming,
    kal_uint16 lcn,
    kal_uint8   is_chan_duplex,
    kal_uint8   codec_type,
    kal_int32   al_type,
    kal_int32   max_sdu_size);

/* VT_EM_DISPLAY_TYPE_INCOMING_VIDEO_CHANNEL_INFO*/
/* VT_EM_DISPLAY_TYPE_OUTGOING_VIDEO_CHANNEL_INFO*/
void vt_l4c_em_report_video_channel_info(
    kal_bool    isIncoming,
    kal_uint16 lcn,
    kal_uint8   is_chan_duplex,
    kal_uint8   codec_type,
    kal_int32   resolution,
    kal_int32   al_type,
    kal_int32   max_sdu_size);
 
/* VT_EM_DISPLAY_TYPE_ADM_MEM_MAX_USED */
void vt_l4c_em_report_adm_mem_max_used(kal_uint32 value);

/* VT_EM_DISPLAY_TYPE_ROUND_TRIP_DELAY */
void vt_l4c_em_report_round_trip_delay(kal_uint32 delay);

/* VT_EM_DISPLAY_TYPE_INCOMING_XSRP  */
void vt_l4c_em_report_incoming_xsrp(
    kal_uint8 sq,
    kal_bool isAck);

/* VT_EM_DISPLAY_TYPE_OUTGOING_XSRP */
void vt_l4c_em_report_outgoing_xsrp(
    kal_uint8 sq,
    kal_bool isAck);

/* VT_EM_DISPLAY_TYPE_STATISTIC_INFO */
void vt_l4c_em_report_statistic_info(void* update_info);


#else
#define vt_l4c_em_report_call_state(s) do{} while(0);  
#define vt_l4c_em_report_master_slave_status(s)  do{} while(0);  
#define vt_l4c_em_report_retransmission_protocol(s) do{} while(0);  
#define vt_l4c_em_report_audio_channel_info(a,b,c,d,e,f)  do{} while(0);  
#define vt_l4c_em_report_video_channel_info(a,b,c,d,e,f,g)  do{} while(0);  
#define vt_l4c_em_report_adm_mem_max_used(s) do{} while(0);  
#define vt_l4c_em_report_round_trip_delay(s)  do{} while(0);  
#define vt_l4c_em_report_incoming_xsrp(a,b) do{} while(0);  
#define vt_l4c_em_report_outgoing_xsrp(a,b) do{} while(0);  
#define vt_l4c_em_report_statistic_info(a) do{} while(0);
#endif

#endif
