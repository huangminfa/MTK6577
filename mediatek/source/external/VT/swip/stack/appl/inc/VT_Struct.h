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

#ifndef _VT_STRUCT_H
#define _VT_STRUCT_H

//###############################################################################
//
//          Timer 
//
//################################################################################
#ifndef __VT_SWIP__
#include "kal_release.h"
#include "stack_common.h"  
#include "stack_msgs.h"
#include "stacklib.h"        	/* Basic type for dll, evshed, stacktimer */
#include "stack_timer.h"      	/* Stack timer */
#include "event_shed.h"
#include "app_ltlcom.h"       	/* Task message communiction */
#endif

#include "rvtypes.h"
#include "termDefs.h"
#include "vt_option_cfg.h"

#define VT_CSR_QUEUE_SIZE (80 * 30)
#define VT_CSR_DL_CONSUME_PER_ROUND 160

/* A/V Sync -- Max delay allowed by audio driver shall be less than 100 frames (<2s) 
     The audio delay function is implemented by inserting slience frame. If delay value is
     close to 100 frames, we may run the risk of dropping incoming audio packet. So we
     define MAX_AV_SKEW to avoid packet drop caused by network jitter */
#define MAX_AV_SKEW (80 * 20)

typedef struct 
{
   stack_timer_struct        VT_base_timer;
   event_scheduler           *VT_event_scheduler_ptr;
   ilm_struct                     VT_timer_ilm;
   eventid                        transport_event_id;
   eventid                        process_event_id;
   eventid                        media_event_id;
   eventid                        watchdog_event_id;
   eventid                        fastUpdate_event_id;
   kal_bool                       is_transport;
   kal_bool                       is_process;
   kal_bool                       is_media;
   kal_bool                       is_watchdog;
   kal_bool                       is_fastUpdate_running;
   void (* start_transport)();
   void (* stop_transport)();
   void (* start_process_event)();
   void (* stop_process_event)();
   void (* start_media)();
   void (* stop_media)();
   void (* start_watchdog_event)();
   void (* stop_watchdog_event)();   
   void (* timer_reset)();
#if (VT_SIM_MODE == SIM_STK_RX_FILE) 
   //SH Yang (2009/03/19): add for MODIS H223 Raw data parsing
   eventid                        simulated_incoming_event_id;
   kal_bool                         is_simulated_incoming;
   void (* start_simulated_incoming)();
   void (* stop_simulated_incoming)();
#endif   
   eventid                         call_keeper_event_id;
   kal_bool                        is_call_keeper_running;
   void (* start_call_keeper)();
   void (* stop_call_keeper)();
} 
vt_timer_context_struct;

typedef struct
{
    RvUint32 align;
    RvUint8 queue[VT_CSR_QUEUE_SIZE];
    kal_mutexid mid;
    RvUint16 r_idx;
    RvUint16 w_idx;
    kal_bool IsWrited;
    kal_bool is_drop;
    kal_bool is_csr_first_tick;
    kal_uint8 max_uplink_queuing_number;
    void (* UnderFlowFunc)();
    void (* OverFlowFunc)();
}
vt_csr_Q_struct;


/* MTK 01600 - Calculate Process Event time */
#ifdef   __DEBUG_GET_PROCESS_EVENT_TIME__         
typedef struct{
    kal_uint32 start;
    kal_uint32 end;
    kal_uint32 minTime;
    kal_uint32 maxTime;
    kal_uint32 timeSum;
    kal_uint32 evCount;
}CPEVT;
#endif

/* We should know the channel attribute because for legacy channels,
media data should be sent only when MES is sent. Channel attribute
is reset every time the channel is destoried.*/
typedef enum{
    VT_INVALID_CHANNEL = 0,
    VT_LEGACY_CHANNEL,
    VT_MPC_CHANNEL,
    VT_ACP_CHANNEL    
}vt_channel_attribute_enum;

/* Global information: terminal's channel info */
typedef struct{
    TermChannelObj *outgoingAudio;  /* NULL if outgoing audio is MPC channel */
    TermChannelObj *incomingAudio;
    TermChannelObj *outgoingVideo;
    TermChannelObj *incomingVideo;
    
    /* Patch for audio interrupt problem */
    kal_bool       is_audio_codec_ready;
    
    /* Codec Status */
    kal_bool       isIncomingAudioCodecOpen;              /* TRUE if DL audio codec is opened by MED */
    kal_bool       isOutgoingAudioCodecOpen;               /* TRUE if UL audio codec is opened by MED */   

    /* Channel Attributes - This affects the channel reporting time */
    vt_channel_attribute_enum   incomingAudioChannelAttribute;
    vt_channel_attribute_enum   outgoingAudioChannelAttribute;
    vt_channel_attribute_enum   incomingVideoChannelAttribute;
    vt_channel_attribute_enum   outgoingVideoChannelAttribute;
   
    /* Downlink A/V Sync */
    kal_uint32 audio_init_delay; /* Must ensure this value is less than MAX_AV_SKEW. 
                                                      This value can be changed only before the first audio packet is recived */
    kal_bool  recv_first_audio_pkt;
}vt_channels_info_struct;
/*
typedef struct vt_uplink_video_list_element_struct{
    kal_uint8 * buff;
    vt_uplink_video_list_element_struct * next;  
}vt_uplink_video_list_element_struct;

typedef struct vt_uplink_video_list_struct{
    kal_mutexid mutex;
    vt_uplink_video_list_element_struct * head;  
}vt_uplink_video_list_struct;
*/
/* define vt_channels_info_struct channelType */
typedef enum
{
    MTKINCOMINGAUDIOCHANNEL = 1,
    MTKOUTGOINGAUDIOCHANNEL = 2,
    MTKINCOMINGVIDEOCHANNEL = 4,
    MTKOUTGOINGVIDEOCHANNEL = 8
}vt_mtk_chan_type_enum;

#ifndef __VT_SWIP__ /* Event group is used for FP solution only */
typedef enum
{
    VT_EVT_NOWAIT = 0x00000000,
    VT_EVT_UL_VIDEO = 0x00000001
}
vt_wait_event_enum;

#define VT_WAIT_EVT(x,y)     kal_brief_trace(TRACE_GROUP_3, VT_WAIT_EVT_TRC, x, y);
#define VT_SET_EVT(x,y)        kal_brief_trace(TRACE_GROUP_3, VT_SET_EVT_TRC, x, y);

#define VT_WAIT_EVENT(ev_id, evt_) \
do{ \
	kal_uint32 retrieve_events = 0; \
	VT_WAIT_EVT(evt_,__LINE__); \
	kal_retrieve_eg_events( \
		ev_id, \
		(evt_), \
		KAL_OR_CONSUME, \
		&retrieve_events, \
		KAL_SUSPEND); \
}while(0)

#define VT_SET_EVENT(ev_id, evt_) \
do{ \
	kal_set_eg_events(ev_id ,(evt_),KAL_OR); \
	VT_SET_EVT(evt_,__LINE__); \
}while(0)

#else //For SWIP. SET_EVENT, WAIT_EVENT is no more used
#define VT_SET_EVENT(a, b) do{}while(0);
#define VT_WAIT_EVENT(a, b) do{}while(0);

#endif /* ifndef __VT_SWIP__ */

#endif

