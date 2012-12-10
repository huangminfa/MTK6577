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

#ifndef __GPS_MGR_ADP_H__
#define __GPS_MGR_ADP_H__
#ifdef __AGPS_SWIP_REL__

#include "gps_type_def.h"
#include "gps_struct.h"
#include "GPSGProt.h"
#include "cm_mtk_agps_common_def.h"

#define AGPS_MAX_PROFILE_NAME_LEN         64
#define AGPS_MAX_PROFILE_ADDR_LEN         64
#define AGPS_MAX_PROFILE_SMS_NUM_LEN      20

typedef enum
{
    GPS_MGR_NOTIFY_TIMER_ID_1,
    GPS_MGR_NOTIFY_TIMER_ID_2,
    GPS_MGR_NOTIFY_TIMER_ID_3,
    GPS_MGR_NOTIFY_TIMER_ID_4,
    GPS_MGR_NOTIFY_TIMER_ID_5,
    GPS_MGR_SET_MODE_TIMER_ID,
    GPS_MGR_GPS_RE_AIDING_TIMER_ID, //!!
    EM_GPS_CLOSE_TIMER_ID
} MMI_TIMER_IDS;

#if 0
typedef enum
{
    MMI_CALL_PROTOCOL_UNKONWN,
    MMI_CALL_PROTOCOL_GSM,
    MMI_CALL_PROTOCOL_3G
} MMI_CALL_PROTOCOL;

extern MMI_CALL_PROTOCOL mmi_gps_mgr_adp_get_call_protocol();
#endif

typedef struct
{
    //!!kal_uint16 name[AGPS_MAX_PROFILE_NAME_LEN];
    //!!kal_uint16 addr[AGPS_MAX_PROFILE_ADDR_LEN];
    kal_char name[AGPS_MAX_PROFILE_NAME_LEN];
    kal_char addr[AGPS_MAX_PROFILE_ADDR_LEN];
    kal_uint32 port;
    kal_uint32 data_account;
    kal_uint16 sms_number[AGPS_MAX_PROFILE_SMS_NUM_LEN];
    kal_uint8 name_can_modify;
    kal_uint8 addr_can_modify;
    kal_uint8 port_can_modify;
    kal_uint8 data_account_can_modify;
}nvram_ef_agps_profile_struct;

typedef struct
{
    U16 req_id;
    supl_mmi_notify_struct notify;
    MMI_TIMER_IDS timer_id;
}mmi_gps_manager_notify_struct;

typedef struct
{
  kal_uint16 timer_id;
  kal_uint32 period;
  FuncPtr timer_expiry;
  void *arg;
} mmi_start_timer_struct;

extern void mmi_gps_mgr_adp_wap_push(void* data, U32 len);
extern U8* mmi_gps_mgr_adp_get_header(void);
extern void mmi_gps_mgr_adp_sms(void* data, U32 len);
extern MMI_BOOL mmi_gps_mgr_adp_check_port_and_num(void);
extern MMI_BOOL mmi_gps_mgr_adp_get_profile(nvram_ef_agps_profile_struct* profile);
extern MMI_BOOL mmi_gps_mgr_adp_can_use_agps(void);
extern MMI_BOOL mmi_gps_mgr_adp_is_in_call(void);
extern void mmi_gps_mgr_adp_call_end(void);
extern void mmi_gps_mgr_adp_show_status_icon(void);
extern void mmi_gps_mgr_adp_hide_status_icon(void);
extern void mmi_gps_mgr_adp_sync_time(gps_p_info_gps_fix_struct* data);
extern void mmi_gps_mgr_adp_notify(mmi_gps_manager_notify_struct *notify);
extern void mmi_gps_mgr_adp_popup(supl_mmi_cause_enum cause);
extern void mmi_gps_mgr_adp_supl_mmi_status_ind_hdlr(void *msg);
extern S32 mmi_gps_mgr_adp_get_port_number(void);
extern void mmi_gps_mgr_adp_start_timer(U16 timerid, U32 delay, FuncPtr funcPtr);
extern void mmi_gps_mgr_adp_stop_timer(U16 timerid);

void mmi_gps_mgr_adp_msg_start_timer_hdlr(void *msg);
void mmi_gps_mgr_adp_open_gps_hdlr(void *msg);
void mmi_gps_mgr_adp_close_gps_hdlr(void *msg);

//<< from maui/mcu/plutommi/MtkApp/GPS/GPSInc/GPSMgrProt.h
#if defined(__AGPS_USER_PLANE__)
#if defined(__AGPS_UP_CP_CONFLICT_HANDLING__)
    #define MAX_SUPL_REQ_NUM 1
#else
    #define MAX_SUPL_REQ_NUM 5
#endif
#define NOTIFY_TIMER_ID_BEGIN GPS_MGR_NOTIFY_TIMER_ID_1
#define NOTIFY_TIMER_ID_END GPS_MGR_NOTIFY_TIMER_ID_5
#define MAX_NOTIFY_TIMER_ID (NOTIFY_TIMER_ID_END - NOTIFY_TIMER_ID_BEGIN + 1)
#define UART_WORK_MODE_COUNT 3

#if 0 // Disable it in ALPS
#define MMI_GPS_MGR_DEBUG
#endif

#define MMI_GPS_MGR_MA_NOT_ABORT_SUPL_WHEN_POS

/*#define MMI_GPS_MGR_OP01*/  // comment out in maui
#endif /* __AGPS_USER_PLANE__ */

//#if defined(__AGPS_USER_PLANE__) || defined(__AGPS_CONTROL_PLANE__)
/*typedef enum
{
    MMI_AGPS_STATE_TERMINATE,
    MMI_AGPS_STATE_START,
    MMI_AGPS_STATE_ACTIVITATE,
    MMI_AGPS_STATE_WAIT_CNF,
    MMI_AGPS_STATE_WORKING,
    MMI_AGPS_STATE_STAND_BY,
    MMI_AGPS_STATE_WAIT_POS,
    MMI_AGPS_STATE_ABORT,
    MMI_AGPS_STATE_SET_WORK_MODE,
    MMI_AGPS_STATE_GPS_INIT,
    MMI_AGPS_STATE_PUSH_WAIT,
    MMI_AGPS_STATE_NI_ERROR,
    MMI_AGPS_STATE_WAIT_RSP,
    MMI_AGPS_STATE_END
} mmi_agps_state_enum;*///==Baochu, this definition is conflicted with cm_mtk_agps_common_def.h

//#endif /* __AGPS_USER_PLANE__ || __AGPS_CONTROL_PLANE__ */

typedef enum
{
    MMI_GPS_START_MODE_NI,
    MMI_GPS_START_MODE_SI,
    MMI_GPS_START_MODE_NORMAL,
    MMI_GPS_START_MODE_END
} mmi_gps_start_mode_enum;

typedef enum
{
    MMI_GPS_MGR_RESULT_SUCCESS = 0,
    MMI_GPS_MGR_RESULT_MORE_REQUEST = -1,
    MMI_GPS_MGR_RESULT_AGPS_OFF = -2,
    MMI_GPS_MGR_RESULT_WORK_MODE_DIFFER = -3,
    MMI_GPS_MGR_RESULT_OPEN_GPS_FAIL = -4,
    MMI_GPS_MGR_RESULT_FALSE_GPS_CALLBACK = -5,
    MMI_GPS_MGR_RESULT_GPS_ABORTING = -6,
    MMI_GPS_MGR_RESULT_UART_OPENING = -7,
    MMI_GPS_MGR_RESULT_PROFILE_ERROR = -8,
    MMI_GPS_MGR_MA_RAW_DATA = -9,
    MMI_GPS_MGR_RESULT_INVALID_REQ_ID = -10,
    MMI_GPS_MGR_RESULT_ECC_CALLING_ERROR = -11,
    MMI_GPS_MGR_RESULT_CP_IS_ONGOING = -12,
    MMI_GPS_MGR_RESULT_NO_HOME_PLMN = -13,         // for SP
    MMI_GPS_MGR_RESULT_END
} mmi_gps_mgr_result_enum;
//>>

//<< from maui/mcu/plutommi/MtkApp/GPS/GPSInc/GPSMgrAGPSUP.h
typedef struct
{
    mmi_gps_start_mode_enum start_mode;
    mmi_gps_work_mode_enum work_mode;
    mdi_gps_uart_work_mode_enum gps_mode;
    U16 gps_port;
    mmi_agps_state_enum state;
    U16 req_id;
    MMI_BOOL is_used;
    void (*gps_callback)(mdi_gps_parser_info_enum type, void *buffer, U32 length);
    supl_mmi_qop_struct qop;
    U8 sim;
    MMI_BOOL need_retry;

#ifdef __AGPS_SWIP_REL__
    MMI_BOOL is_re_aid; // add in porting
#endif
}mmi_gps_manager_request_struct;

typedef struct
{
    U16 req_id;
    S8 data[SUPL_MMI_MAX_PUSH_SIZE];
    U32 data_len;
}mmi_gps_manager_push_struct;
//>>

//<< from maui/mcu/plutommi/Service/Inc/SmsSrvGprot.h
/* SIM Card Type */
typedef enum
{
    SRV_SMS_SIM_1 = MMI_GSM | MMI_SIM1,     /* SIM Card 1, the SIM card inserted in the slot 1 */
    SRV_SMS_SIM_2 = MMI_GSM | MMI_SIM2,     /* SIM Card 2, the SIM card inserted in the slot 2 */
#if (MMI_MAX_SIM_NUM >= 3)
    SRV_SMS_SIM_3 = MMI_GSM | MMI_SIM3,     /* SIM Card 3, the SIM card inserted in the slot 3 */
#endif
#if (MMI_MAX_SIM_NUM >= 4)
    SRV_SMS_SIM_4 = MMI_GSM | MMI_SIM4,     /* SIM Card 4, the SIM card inserted in the slot 4 */
#endif

    SRV_SMS_SIM_TOTAL
} srv_sms_sim_enum;

/* SMS Event Structure */
typedef struct
{
    MMI_EVT_PARAM_HEADER
    void *event_info;               /* Event information data. For each event's event infomation structure,
                                     * refer the discription of each event in enum srv_sms_event_enum. */
} srv_sms_event_struct;

/*****************************************************************************
 * <GROUP  CallBackFunctions>
 *
 * FUNCTION
 *  SrvSmsEventFunc
 * DESCRIPTION
 *  SMS Service interal event callback function. for interal using
 * PARAMETERS
 * event_data    [IN]   Event data, refer to srv_sms_event_struct
 * RETURNS
 *  MMI_BOOL
*****************************************************************************/
typedef MMI_BOOL(*SrvSmsEventFunc)(srv_sms_event_struct* event_data);

#if 0
/*****************************************************************************
 * FUNCTION
 *  srv_sms_reg_port
 * DESCRIPTION
 *  This API is used by AP to register port number
 * PARAMETERS
 *  port_num    :   [IN]    port number
 *  sim_id      :   [IN]    SIM Card ID
 *  callback    :   [IN]    Event Handler Function
 *  user_data   :   [IN]    User Data
 * RETURNS
 *  void
 *****************************************************************************/
extern void srv_sms_reg_port(
                U16 port_num,
                srv_sms_sim_enum sim_id,
                SrvSmsEventFunc callback,
                void *user_data);
#endif
//>>

//<< from maui/mcu/plutommi/Service/Inc/UcmSrvGprot.h
/* Action opcode enum */
typedef enum
{
    SRV_UCM_NO_ACT = 0, /* No action */
    SRV_UCM_DIAL_ACT, /* Dial action, srv_ucm_dial_act_req_struct, srv_ucm_act_rsp_struct*/
    SRV_UCM_ACCEPT_ACT, /* Accept action, srv_ucm_single_call_act_req_struct, srv_ucm_act_rsp_struct*/
    SRV_UCM_HOLD_ACT, /* Hold action, srv_ucm_single_group_act_req_struct, srv_ucm_act_rsp_struct*/
    SRV_UCM_RETRIEVE_ACT, /* Retrieve action, srv_ucm_single_group_act_req_struct, srv_ucm_act_rsp_struct */
    SRV_UCM_SWAP_ACT,  /* Swap action, srv_ucm_multiple_group_act_req_struct, srv_ucm_act_rsp_struct */
    SRV_UCM_CONFERENCE_ACT, /* Conference action, srv_ucm_multiple_group_act_req_struct, srv_ucm_act_rsp_struct */
    SRV_UCM_SPLIT_ACT, /* Split action, srv_ucm_single_call_act_req_struct, srv_ucm_act_rsp_struct */
    SRV_UCM_DEFLECT_ACT, /* Deflect action, srv_ucm_cd_or_ct_act_req_struct, srv_ucm_act_rsp_struct */
    SRV_UCM_TRANSFER_ACT, /* Transfer action, srv_ucm_cd_or_ct_act_req_struct, srv_ucm_act_rsp_struct */
    SRV_UCM_EXPLICIT_CALL_TRANSFER_ACT, /* ECT action, srv_ucm_ect_act_req_struct, srv_ucm_act_rsp_struct */
    SRV_UCM_END_SINGLE_ACT, /* End single action, srv_ucm_single_call_act_req_struct, srv_ucm_act_rsp_struct */
    SRV_UCM_END_CONFERENCE_ACT, /* End conference action, srv_ucm_single_group_act_req_struct, srv_ucm_act_rsp_struct */
    SRV_UCM_END_ALL_ACT, /* End all action, null, srv_ucm_act_rsp_struct */
    SRV_UCM_END_ALL_ACTIVE_ACT, /* End all active action, null, srv_ucm_act_rsp_struct */
    SRV_UCM_END_ALL_HELD_ACT, /* End all held action, null, srv_ucm_act_rsp_struct */
    SRV_UCM_END_ALL_PROCESSING_ACT, /* End all processing action, null, srv_ucm_act_rsp_struct */
    SRV_UCM_END_ALL_AND_DIAL_ACT, /* End all and dial action, srv_ucm_dial_act_req_struct, srv_ucm_act_rsp_struct*/
    SRV_UCM_END_ALL_AND_ACCEPT_ACT, /* End all and accept action, srv_ucm_single_call_act_req_struct, srv_ucm_act_rsp_struct*/
    SRV_UCM_END_ALL_ACTIVE_AND_RETRIEVE_ACT, /* End all active and retrieve action, srv_ucm_single_group_act_req_struct, srv_ucm_act_rsp_struct */
    SRV_UCM_END_ALL_ACTIVE_AND_ACCEPT_ACT, /* End all active and accept action, srv_ucm_single_call_act_req_struct, srv_ucm_act_rsp_struct */
    SRV_UCM_HOLD_AND_DIAL_ACT, /* Hold and dial action, srv_ucm_hold_and_dial_act_req_struct, srv_ucm_act_rsp_struct */
    SRV_UCM_HOLD_AND_ACCEPT_ACT, /* Hold and accept action, srv_ucm_hold_and_accept_act_req_struct, srv_ucm_act_rsp_struct */
    SRV_UCM_START_DTMF_ACT, /* Start DTMF action, srv_ucm_dtmf_struct, no response */
    SRV_UCM_STOP_DTMF_ACT, /* Stop DTMF action, null, no response */
    SRV_UCM_SEND_DTMF_DIGITS_ACT, /* Send DTMF string action, srv_ucm_dtmf_digits_struct, srv_ucm_act_rsp_struct */
#if (defined(__OP01__) && defined(__MMI_VIDEO_TELEPHONY__))
    SRV_UCM_VT_FALLTO_VOICE_ACT, /* Accept Video call by Voice call action, srv_ucm_single_call_act_req_struct, srv_ucm_act_rsp_struct*/
#endif /* (defined(__OP01__) && defined(__MMI_VIDEO_TELEPHONY__)) */
    SRV_UCM_ACT_OPCODE_TOTAL /* Total enum number */
} srv_ucm_act_opcode_enum;

/*****************************************************************************
 * FUNCTION
 *  srv_ucm_query_curr_action
 * DESCRIPTION
 *  Query current ongoing action
 * PARAMETERS
 *
 * RETURNS
 *  srv_ucm_act_opcode_enum: current action
 *****************************************************************************/
extern srv_ucm_act_opcode_enum srv_ucm_query_curr_action(void);

/*****************************************************************************
 * FUNCTION
 *  srv_ucm_is_emergency_call
 * DESCRIPTION
 *  check if there is emergency call exists
 * PARAMETERS
 *  void
 * RETURNS
 *  MMI_TRUE means there is emergency call.
 *  MMI_FALSE means there is no emergency call.
 *****************************************************************************/
extern MMI_BOOL srv_ucm_is_emergency_call(void);
//>>

//<< from maui/mcu/plutommi/Framework/CommonFiles/CommonInc/mmi_frm_events_gprot.h
#if 0
#define SetProtocolEventHandler(func, event)       mmi_frm_set_protocol_event_handler(event, (PsIntFuncPtr)func, MMI_FALSE)

/*****************************************************************************
 * FUNCTION
 *  mmi_frm_set_protocol_event_handler
 * DESCRIPTION
 *  This function is used for register the protocol event handler. Whenever
 *  an event is received from the protocol or system corresponding function
 *  is executed.
 * PARAMETERS
 *  eventID:         [IN] Unique Protocol/System EventID.
 *  funcPtr:         [IN] Function to be executed whenever a event is received from the protocol or system
 *  isMultiHandler:  [IN] is multi-handler or not. for multi-handler, it shall return MMI_TRUE when event is handled (taken) by it.
 * RETURNS
 *  void
 *****************************************************************************/
extern void mmi_frm_set_protocol_event_handler(U16 eventID, PsIntFuncPtr funcPtr, MMI_BOOL isMultiHandler);
#endif

/* This enum is for identify different screen types */
typedef enum
{
    /* The enum begin */
    MMI_FRM_UNKNOW_SCRN,
    /* The screen type is the idle screen */
    MMI_FRM_IDLE_SCRN,
    /* The screen type is the full screen */
    MMI_FRM_FULL_SCRN,
    /* The screen type is the small screen */
    MMI_FRM_SMALL_SCRN,
    /* The screen type is the tab page screen */
    MMI_FRM_TAB_PAGE,
    /* The screen type is the tab screen */
    MMI_FRM_TAB,
    /* The screen type is the full screen */
    MMI_FRM_SCRN_GROUP,
    /* The screen will not enter history, such as dummy screen */
    MMI_FRM_FG_ONLY_SCRN,
    /* The enum total number */
    MMI_FRM_SCRN_TYPE_MAX
} mmi_frm_scrn_type_enum;

typedef enum
{
   MMI_EVENT_DEFAULT,
#ifdef MMI_NOTI_MGR_UT
   MMI_EVENT_UT_INFO,
   MMI_EVENT_UT_QUERY,
   MMI_EVENT_UT_WARNING,
   MMI_EVENT_UT_ALARM,
#endif /* MMI_NOTI_MGR_UT */
   MMI_EVENT_FAILURE,   /* Basic popup failure type */
   MMI_EVENT_INFO,      /* Basic popup infomation type */
   MMI_EVENT_WARNING,   /* Basic popup warning type */
   MMI_EVENT_SUCCESS,   /* Basic popup success type */
   MMI_EVENT_PROGRESS,  /* Basic popup progress type */
   MMI_EVENT_QUERY,     /* Basic popup query type */
   MMI_EVENT_MESSAGE_SENT,          /* __OP11__ */
   MMI_EVENT_PROPLEM,               /* __OP11__ */
   MMI_EVENT_EXPLICITLY_SAVED,      /* __OP11__ */
   MMI_EVENT_EXPLICITLY_DELETED,    /* __OP11__ */
   MMI_EVENT_CONFIRM,               /* __OP11__ */
   MMI_EVENT_BATTERY_WARNING,       /* __OP11__ */
   MMI_EVENT_NOT_AVAILABLE,         /* __OP11__ */
   MMI_EVENT_ANSPHONE,              /* __OP11__ */
   MMI_EVENT_REMIND,                /* __OP11__ */
   MMI_EVENT_BATTERY_NOTIFY,        /* __OP11__ */
   MMI_EVENT_TOTAL_NUM,		/* used for original popup event */
   /* App should define event id for scenario to configure notification information in mmi_events_notify_tbl */
   MMI_EVENT_NON_TONE,
   MMI_EVENT_INCOMING_CALL,
   MMI_EVENT_ALARM_EXPIRES,
   MMI_EVENT_VOBJECT,
   MMI_EVENT_MESSAGE_WAITING_OTHERS,
   MMI_EVENT_MESSAGE_WAITING_SLAVE,
   MMI_EVENT_MESSAGE_SIM1_WAITING = MMI_EVENT_MESSAGE_WAITING_OTHERS,
   MMI_EVENT_MESSAGE_SIM2_WAITING = MMI_EVENT_MESSAGE_WAITING_SLAVE,
   MMI_EVENT_MESSAGE_SIM3_WAITING,
   MMI_EVENT_MESSAGE_SIM4_WAITING,
   MMI_EVENT_MESSAGE_WAITING_VOICE_MAIL,
   MMI_EVENT_SMS,
   MMI_EVENT_SLAVE_SMS,
   MMI_EVENT_SIM1_SMS = MMI_EVENT_SMS,
   MMI_EVENT_SIM2_SMS = MMI_EVENT_SLAVE_SMS,
   MMI_EVENT_SIM3_SMS,
   MMI_EVENT_SIM4_SMS,
   MMI_EVENT_MMS,
   MMI_EVENT_SLAVE_MMS,
   MMI_EVENT_SIM1_MMS = MMI_EVENT_MMS,
   MMI_EVENT_SIM2_MMS = MMI_EVENT_SLAVE_MMS,
   MMI_EVENT_SIM3_MMS,
   MMI_EVENT_SIM4_MMS,
   MMI_EVENT_EMAIL,
   MMI_EVENT_EMAIL_NEW_EMAIL = MMI_EVENT_EMAIL,
   MMI_EVENT_EMAIL_SEND_FAIL,
   MMI_EVENT_EMAIL_REFRESH_FAIL,
   MMI_EVENT_REMINDER_ALARM,
   MMI_EVENT_REMINDER_OTHER,
   MMI_EVENT_REMINDER_CAL,
   MMI_EVENT_SCHEDULE_POWER_OFF,
   MMI_EVENT_INFO_BALLOON,


   MMI_EVENT_SEND_FAIL_MSG,
   MMI_EVENT_SENDING_MSG,
   MMI_EVENT_UNREAD_MSG,
   MMI_EVENT_JAVA_PUSH,
   MMI_EVENT_INVALID_CERT,
   MMI_EVENT_SELECT_CERT,

   MMI_EVENT_SIM1_NEW_CLASS0_SMS,
   MMI_EVENT_SIM2_NEW_CLASS0_SMS,
   MMI_EVENT_SIM1_NEW_SMS_REPORT,
   MMI_EVENT_SIM2_NEW_SMS_REPORT,
   MMI_EVENT_SIM1_NEW_MSG_WAITING_VOICEMAIL,
   MMI_EVENT_SIM2_NEW_MSG_WAITING_VOICEMAIL,
   MMI_EVENT_SIM1_NEW_MSG_WAITING_FAX,
   MMI_EVENT_SIM2_NEW_MSG_WAITING_FAX,
   MMI_EVENT_SIM1_NEW_MSG_WAITING_EMAIL,
   MMI_EVENT_SIM2_NEW_MSG_WAITING_EMAIL,
   MMI_EVENT_SIM1_NEW_MSG_WAITING_NET_MSG,
   MMI_EVENT_SIM2_NEW_MSG_WAITING_NET_MSG,
   MMI_EVENT_SIM1_NEW_MSG_WAITING_VEDIO,
   MMI_EVENT_SIM2_NEW_MSG_WAITING_VEDIO,

   MMI_EVENT_SIM1_NEW_CB_MSG,
   MMI_EVENT_SIM2_NEW_CB_MSG,

   MMI_EVENT_SIM_FAILURE,
   MMI_EVENT_SIM_VERIFICATION,
   MMI_EVENT_NW_REGISTER_FAIL,

   MMI_EVENT_BLUETOOTH,

   MMI_EVENT_SYNC_FINISH,

   MMI_EVENT_DA_DOWNLOAD_COMPLETED,
   MMI_EVENT_DA_DOWNLOAD_FAILED,
   MMI_EVENT_DA_MEMORY_FULL,

   MMI_EVENT_CONTACT_BACKUP_ERROR,
   MMI_EVENT_CONTACT_RESTORE_ERROR,
   MMI_EVENT_CONTACT_IMPORT_ERROR,

   MMI_EVENT_MMS_REPORTS,

   MMI_EVENT_SAT_IDLE_TEXT,

   MMI_EVENT_TOTAL_COUNT		/* total event*/
} mmi_event_notify_enum;
//>>

//<< from maui/mcu/interface/inet_ps/cbm_consts.h
/* sim id */
typedef enum
{
    CBM_SIM_ID_SIM1, /* sim card one */
    CBM_SIM_ID_SIM2, /* sim card two */
    CBM_SIM_ID_SIM3, /* sim card there */
    CBM_SIM_ID_SIM4, /* sim card four */
    CBM_SIM_ID_TOTAL /* total sim card number */
} cbm_sim_id_enum;

/* error cause */
typedef enum
{
    CBM_OK                  = 0,  /* success */
    CBM_ERROR               = -1, /* error */
    CBM_WOULDBLOCK          = -2, /* would block */
    CBM_LIMIT_RESOURCE      = -3, /* limited resource */
    CBM_INVALID_ACCT_ID     = -4, /* invalid account id*/
    CBM_INVALID_AP_ID       = -5, /* invalid application id*/
    CBM_INVALID_SIM_ID      = -6, /* invalid SIM id */
    CBM_BEARER_FAIL         = -7, /* bearer fail */
    CBM_DHCP_ERROR          = -8, /* DHCP get IP error */
    CBM_CANCEL_ACT_BEARER   = -9, /* cancel the account query screen, such as always ask or bearer fallback screen */
    CBM_DISC_BY_CM          = -10 /* bearer is deactivated by the connection management */
} cbm_result_error_enum;
//>>

//<< from mcu/interface/inet_ps/cbm_api.h
/*****************************************************************************
 * <GROUP  Bearer>
 *
 * FUNCTION
 *  cbm_register_app_id_with_app_info
 * DESCRIPTION
 *  get a registered application id.
 *  APP could call this API to register itself and get an app_id.
 *  This returned app id is a dynamical ID. App had better not save the app id to nvram or file,
 *  because the app id will be changed when power on again or call register function again.
 *  The lifetime of app id: before app call deregister function or before power off.
 *
 *  This API is mainly provided for application which doesn't have MMI and profile menu.
 *  However, it still needs an application id. It can call this API to get one.
 *  All application who use network service must register application id.
 *
 *  Please remember to wrap API by __TCPIP__ if you may call this API when network is disable
 * PARAMETERS
 *  app_id  : [OUT]    application identification
 * RETURN VALUES
 *  CBM_OK : register app id successfully
 *  CBM_ERROR : wrong input or output parameters
 *  CBM_LIMIT_RESOURCE : no available application slot
 * SEE ALSO
 *  cbm_deregister_app_id
 * EXAMPLE
 * <code>
 * kal_uint8 app_id;
 * kal_int8 ret;
 *
 * ret = cbm_register_app_id(&app_id);
 * if (ret == CBM_OK)
 * {
 *     //the valid app_id is obtained
 * }
 * </code>
 *****************************************************************************/
extern kal_int8 cbm_register_app_id(kal_uint8 *app_id);

#if 0
/*****************************************************************************
 * <GROUP  Bearer>
 *
 * FUNCTION
 *  cbm_encode_data_account_id
 * DESCRIPTION
 *  encode the info into the 32-bits account id (for single account use only).
 *  APP must call this function to set app_id,, sim card info and always-ask feature
 *  after application gets the account id. The app_id is returned by cbm_register_app_id function.
 *  This API is for single account only. If multiple account id contained in the 32-bits account id,
 *  please use the cbm_encode_data_account_id_ext.
 *  Application can use this API to encode the info into the data account id.
 *  This API is only for one account only. In other words,
 *  applications which need multiple accounts should not use this API.
 *
 *  P.S. We assume applications shall set its module id or register the application id
 *  before calling this API.
 *
 *  Please remember to wrap API by __TCPIP__ if you may call this API when network is disable
 * PARAMETERS
 *  acct_id     : [IN]    This is application account id.
 *  sim_id      : [IN]    This is Sim card info; sim 1 or sim 2 (cbm_sim_id_enum)
 *  app_id      : [IN]    This is app id, it could be get by cbm register api.
 *  always_ask  : [IN]    This is application Always ask flag.
 * RETURNS
 *  Encoded data account id
 *  1.  The data account id may contain the sim info, app_id or always-ask feature if application fill in these parameters in this API.
 *  2.  If the parameter that sim info or app id is invalid, this api will return CBM_INVALID_ACCT_ID.
 * SEE ALSO
 *  cbm_encode_data_account_id_ext
 * EXAMPLE
 * Generate a SIM2 account id example:
 * <code>
 * kal_uint8 app_id;
 * cbm_sim_id_enum sim_id;
 * app_id = cbm_register_app_id(APP_STR_ID, APP_ICON_ID);
 * sim_id = CBM_SIM_ID_SIM2;
 * account_id = cbm_encode_data_account_id(ori_acct_id, sim_id, app_id, 0);
 * </code>
 *****************************************************************************/
extern kal_uint32 cbm_encode_data_account_id(kal_uint32 acct_id, cbm_sim_id_enum sim_id,
                                      kal_uint8 app_id, kal_bool always_ask);
#endif
//>>

#if 0 // Due to mmi_gps_mgr_da_file() is not used in SP.
//<< from maui/mcu/plutommi/Service/DLAgentSrv/DLAgentPlutoTypdefs.h
#define srv_da_get_header             mmi_da_get_header
//>>
//<<from CW's 1048 GPSMgr.c
extern const S8* mmi_da_get_header(S32 session_id, U32* header_len);
//>>
#endif

//<< from maui/mcu/plutommi/framework/interface/Unicodexdcl.h
#if 0
/*****************************************************************************
 * FUNCTION
 *  mmi_ucs2_to_asc
 * DESCRIPTION
 *  The function is used for convert UCS2 string to ANSII string.
 *  The caller need to make sure the pOutBuffer  size must have enough space
 *  or the function causes the memory corruption. The function will add the
 *  terminated character at the end of pOutBuffer array. The byte order of
 *  UCS2 character(input param) is little endian.
 * PARAMETERS
 *  pOutBuffer    :  [OUT]  The output Buffer.
 *  pInBuffer     :  [IN]   The input Buffer
 * RETURNS
 *  U16
 *****************************************************************************/
extern U16 mmi_ucs2_to_asc(S8 *pOutBuffer, S8 *pInBuffer); // We use S8 instead of CHAR
#endif
//>>

//<< from maui/mcu/plutommi/service/inc/SimCtrlSrvGprot.h
/*****************************************************************************
 * FUNCTION
 *  srv_sim_ctrl_get_home_plmn
 * DESCRIPTION
 *  Get home PLMN specified in the SIM.
 *  If multiple home PLMN exist, e.g. EHPLMN, this API will return the
 *  highest-priority one.
 *
 *  NOTE: It requires time to read home PLMN. Home PLMN is ready only if
 *  SRV_SIM_CTRL_HOME_PLMN_CHANGED has been emitted after SIM is available.
 * PARAMETERS
 *  sim             : [IN]  Which SIM
 *  out_plmn_buffer : [OUT] PLMN buffer to be filled in, it shall at least
 *                          (SRV_MAX_PLMN_LEN + 1) bytes. // maui uses CHAR. We uses char
 *  buffer_size     : [IN]  Byte size of out_plmn_buffer.
 *                          It shall >= (SRV_MAX_PLMN_LEN + 1.)
 * RETURN VALUES
 *  MMI_TRUE    : If home PLMN is available and returned.
 *  MMI_FALSE   : Home PLMN is unavailable or not ready.
 * SEE ALSO
 *  SRV_SIM_CTRL_HOME_PLMN_CHANGED
 *****************************************************************************/
extern MMI_BOOL srv_sim_ctrl_get_home_plmn(mmi_sim_enum sim, char *out_plmn_buffer, U32 buffer_size);
//>>

//<< from maui/mcu/plutommi/framework/gui/gui_inc/Wgui_categories_util.h
#if 0
/*****************************************************************************
 * <group dom_status_icon_wgui_layer_basic>
 * FUNCTION
 *  wgui_status_icon_bar_animate_icon
 * DESCRIPTION
 *  animate status icon
 * PARAMETERS
 *  icon_id         : [IN]    The id of status icon
 * RETURNS
 *  void
 *****************************************************************************/
extern void wgui_status_icon_bar_animate_icon(S32 icon_id);
#endif

#if 0
/*****************************************************************************
 * <group dom_status_icon_wgui_layer_basic>
 * FUNCTION
 *  wgui_status_icon_bar_hide_icon
 * DESCRIPTION
 *  Hide the status icon
 * PARAMETERS
 *  icon_id         : [IN]    The id of status icon
 * RETURNS
 *  void
 *****************************************************************************/
extern void wgui_status_icon_bar_hide_icon(S32 icon_id);
#endif

/*****************************************************************************
 * <group dom_softkey_wgui_layer_property_setting>
 * FUNCTION
 *  SetLeftSoftkeyFunction
 * DESCRIPTION
 *  Set the key event function to left softkey and register left softkey to KEY_LSK.
 * PARAMETERS
 *  f        : [IN] Is the key event function handler which will be called when one specific key event occurs
 *  k        : [IN] Is the key event
 * RETURNS
 *  void
 *****************************************************************************/
#if 0
extern void SetLeftSoftkeyFunction(void (*f) (void), MMI_key_event_type k);
#else
#define SetLeftSoftkeyFunction(f, k)
#endif

/*****************************************************************************
 * <group dom_softkey_wgui_layer_property_setting>
 * FUNCTION
 *  SetRightSoftkeyFunction
 * DESCRIPTION
 *  Set the key event function to right softkey and register right softkey to KEY_RSK.
 * PARAMETERS
 *  f        : [IN] Is the key event function handler which will be called when one specific key event occurs
 *  k        : [IN] Is the key event
 * RETURNS
 *  void
 *****************************************************************************/
#if 0
extern void SetRightSoftkeyFunction(void (*f) (void), MMI_key_event_type k);
#else
#define SetRightSoftkeyFunction(f, k)
#endif
//>>

//<< from maui/mcu/plutommi/framework/gui/gui_inc/Wgui_categories_popup.h
/*****************************************************************************
 * FUNCTION
 *  ShowCategory65Screen
 * DESCRIPTION
 *  Displays the category65 screen
 *  [FTE]: support(tool bar is not avaiable)
 * <img name="wgui_cat062_img1" />
 * PARAMETERS
 *   message                       : [IN ] <N/A /> Message string
 *   message_icon                  : [IN ] <N/A /> Message icon
 *   history_buffer                : [IN ] <N/A /> History buffer
 * RETURNS
 *  void
 * EXAMPLE
 * <code>
 * void EntryScreen_65(void)
 * {
 *     U8 *guiBuffer = NULL;
 * 
 *     EntryNewScreen(SCR_ID_CSB_DUMMY_SCR_ID, ExitScreen_Generic, EntryCSBStartView, NULL);
 * 
 *     ShowCategory65Screen((U8*)get_string(STR_ID_CSB_CATEGORY_TEXT), PopupImageGroup[current_popup_index], guiBuffer);
 *     current_popup_index++;
 *     if (current_popup_index > MAX_POPUP_IMAGE_INDEX)
 *     {
 *         current_popup_index = 0;
 *     }
 *     csb_set_key_handlers();
 * }
 * </code>
 *****************************************************************************/
extern void ShowCategory65Screen(U8 *message, U16 message_icon, U8 *history_buffer);

/*****************************************************************************
 * FUNCTION
 *  ShowCategory165Screen_ext
 * DESCRIPTION
 *  Displays the category165 screen
 *  [FTE]: support(tool bar is not avaiable)
 * <img name="wgui_cat163_img1" />
 * PARAMETERS
 *   left_softkey                  : [IN ] <LSK /> left softkey label
 *   left_softkey_icon             : [IN ] <N/A /> left softkey icon
 *   right_softkey                 : [IN ] <RSK /> right softkey label
 *   right_softkey_icon            : [IN ] <N/A /> right softkey icon
 *   message                       : [IN ] <POPUPCONFIRM /> Message string
 *   message_icon                  : [IN ] <N/A /> message icon
 *   history_buffer                : [IN ] <N/A /> History buffer
 * RETURNS
 *  void
 * EXAMPLE
 * <code>
 * void EntryScreen_165(void)
 * {
 * 
 *     EntryNewScreen(SCR_ID_CSB_DUMMY_SCR_ID, ExitScreen_Generic, EntryCSBStartView, NULL);
 * 
 *     ShowCategory165Screen(
 *         STR_ID_CSB_LSK_TEXT,
 *         0,
 *         STR_ID_CSB_RSK_TEXT,
 *         0,
 *         get_string(STR_ID_CSB_DUMMY_TEXT),
 *         IMG_GLOBAL_QUESTION,
 *         0);
 * 
 *     csb_set_key_handlers();
 * }
 * </code>
 *****************************************************************************/
extern void ShowCategory165Screen(
                U16 left_softkey,
                U16 left_softkey_icon,
                U16 right_softkey,
                U16 right_softkey_icon,
                UI_string_type message,
                U16 message_icon,
                U8 *history_buffer);
//>>

//<< from maui/mcu/plutommi/framework/gui/gui_inc/Gui_typedef.h
/* <group dom_status_icon_enum> */
/* Icon names that the applications must use when they call
   wgui_status_icon_show_status_icon or wgui_status_icon_hide_status_icon                   */
enum STATUS_ICON_LIST
{
    /* The invalid staus icon */
    STATUS_ICON_INVALID_ID = -1,
    /* The mainlcd status icon id start marker */
    STATUS_ICON_MAINLCD_START = 0,
	STATUS_ICON_MAINLCD_RIGHT_REGION_START = STATUS_ICON_MAINLCD_START,
	/* Icon of signal strength */
    STATUS_ICON_SIGNAL_STRENGTH = STATUS_ICON_MAINLCD_START,
    /* Icon of battery strength */
    STATUS_ICON_BATTERY_STRENGTH,

    //  :
    //  :

#ifdef __AGPS_SUPPORT__
    /* Icon of AGPS state */
    STATUS_ICON_AGPS_STATE,
#endif

    //  :
    //  :
    /* The sublcd status icon end mark */
    STATUS_ICON_SUBLCD_END,
    /* The indicator of status icon end */
    MAX_STATUS_ICONS = STATUS_ICON_SUBLCD_END
};
//>>

//<<
/*****************************************************************************
* FUNCTION
*  mmi_gps_setting_get_is_agps_on
* DESCRIPTION
*  save GPS setting and (mmi_void_funcptr_type)mmi_frm_scrn_close_active_id
* PARAMETERS
*  void
* RETURNS
*  void
*****************************************************************************/
MMI_BOOL mmi_gps_setting_get_is_agps_on(U8 sim);
//>>

//#define __DM_LAWMO_SUPPORT__
#ifdef __DM_LAWMO_SUPPORT__
//<< from maui/mcu/plutommi/mtkapp/dmuiapp/dmuiappinc/Dmuigprot.h
/*****************************************************************************
 * FUNCTION
 *  mmi_dmui_is_phone_lock
 * DESCRIPTION
 *  function to check is the phone locked
 * PARAMETERS
 *  void
 * RETURNS
 *  TRUE:   phone locked
 *  FALSE:  phone unlocked
 *****************************************************************************/
kal_int8 mmi_dmui_is_phone_lock(void);
#define MMI_GPS_MGR_IS_PHONELOCK ((MMI_BOOL)mmi_dmui_is_phone_lock())
//>>
#else
#define MMI_GPS_MGR_IS_PHONELOCK (MMI_FALSE)
#endif

//<< from maui/mcu/plutommi/framework/interface/MMIDataType.h
typedef mmi_ret (*mmi_proc_func) (mmi_event_struct *param);
//>>

//<< from maui/mcu/plutommi/framework/interface/ScreenRotationGprot.h
typedef enum
{
    MMI_FRM_SCREEN_ROTATE_0,
    MMI_FRM_SCREEN_ROTATE_90,
    MMI_FRM_SCREEN_ROTATE_180,
    MMI_FRM_SCREEN_ROTATE_270,
    MMI_FRM_SCREEN_ROTATE_MIRROR_0,
    MMI_FRM_SCREEN_ROTATE_MIRROR_90,
    MMI_FRM_SCREEN_ROTATE_MIRROR_180,
    MMI_FRM_SCREEN_ROTATE_MIRROR_270,
    MMI_FRM_SCREEN_ROTATE_MAX_TYPE
} mmi_frm_screen_rotate_enum;
//>>

//<< from maui/mcu/plutommi/service/inc/ProfilesSrvGprot.h
/* This enum defines the tone types for other apps to refer to for play tone request*/
typedef enum
{
    SRV_PROF_TONE_NONE,                 /* None */
    SRV_PROF_TONE_ERROR,                /* Error tone */
    SRV_PROF_TONE_CONNECT,              /* Connect tone */
    SRV_PROF_TONE_CAMP_ON,              /* Camp on tone */
    SRV_PROF_TONE_WARNING,              /* Warning tone */
    SRV_PROF_TONE_INCOMING_CALL,        /* Incoming call tone */
    SRV_PROF_TONE_INCOMING_CALL_CARD2,  /* Incoming call tone - card2 */
    SRV_PROF_TONE_INCOMING_CALL_CARD3,  /* Incoming call tone - card3 */
    SRV_PROF_TONE_INCOMING_CALL_CARD4,  /* Incoming call tone - card4 */
    SRV_PROF_TONE_VIDEO_CALL,           /* Video call tone */
    SRV_PROF_TONE_VIDEO_CALL_CARD2,     /* Video call tone - card2 */

                                        /*11*/
    SRV_PROF_TONE_VIDEO_CALL_CARD3,     /* Video call tone - card3 */
    SRV_PROF_TONE_VIDEO_CALL_CARD4,     /* Video call tone - card4 */
    SRV_PROF_TONE_SMS,                  /* SMS tone */
    SRV_PROF_TONE_SMS_CARD2,            /* SMS tone - card2 */
    SRV_PROF_TONE_SMS_CARD3,            /* SMS tone - card3 */
    SRV_PROF_TONE_SMS_CARD4,            /* SMS tone - card4 */
    SRV_PROF_TONE_MMS,                  /* MMS tone */
    SRV_PROF_TONE_MMS_CARD2,            /* MMS tone - card2 */
    SRV_PROF_TONE_MMS_CARD3,            /* MMS tone - card3 */
    SRV_PROF_TONE_MMS_CARD4,            /* MMS tone - card4 */

                                        /*21*/
    SRV_PROF_TONE_EMAIL,                /* Email tone */
    SRV_PROF_TONE_EMAIL_CARD2,          /* Email tone - card2 */
    SRV_PROF_TONE_EMAIL_CARD3,          /* Email tone - card3 */
    SRV_PROF_TONE_EMAIL_CARD4,          /* Email tone - card4 */
    SRV_PROF_TONE_VOICE,                /* Voice tone */
    SRV_PROF_TONE_VOICE_CARD2,          /* Voice tone - card2 */
    SRV_PROF_TONE_VOICE_CARD3,          /* Voice tone - card3 */
    SRV_PROF_TONE_VOICE_CARD4,          /* Voice tone - card4 */
    SRV_PROF_TONE_ALARM,                /* Alarm tone */
    SRV_PROF_TONE_POWER_ON,             /* Power on tone */

                                        /*31*/
    SRV_PROF_TONE_POWER_OFF,            /* Power off tone */
    SRV_PROF_TONE_COVER_OPEN,           /* Cover open tone */
    SRV_PROF_TONE_COVER_CLOSE,          /* Cover close tone */
    SRV_PROF_TONE_SUCCESS,              /* Success tone */
    SRV_PROF_TONE_SAVE,                 /* Save tone */
    SRV_PROF_TONE_EMPTY_LIST,           /* Empty tone */
    SRV_PROF_TONE_GENERAL_TONE,         /* General tone */
    SRV_PROF_TONE_AUX,                  /* AUX tone */
    SRV_PROF_TONE_BATTERY_LOW,          /* Battery low tone */
    SRV_PROF_TONE_BATTERY_WARNING,      /* Battery warning tone */

                                        /*41*/
    SRV_PROF_TONE_CALL_REMINDER,        /* Call reminder tone */
    SRV_PROF_TONE_CCBS,                 /* CCBS tone */
    SRV_PROF_TONE_CONGESTION,           /* Congestion tone */
    SRV_PROF_TONE_AUTH_FAIL,            /* Authentication fail tone */
    SRV_PROF_TONE_NUM_UNOBTAIN,         /* Number un-obtained tone */
    SRV_PROF_TONE_CALL_DROP,            /* Call drop tone */
    SRV_PROF_TONE_SMS_INCALL,           /* SMS in call tone */
    SRV_PROF_TONE_WARNING_INCALL,       /* Warning in call tone */
    SRV_PROF_TONE_ERROR_INCALL,         /* Error in call tone */
    SRV_PROF_TONE_CONNECT_INCALL,       /* Connect in call tone */

                                        /*51*/
    SRV_PROF_TONE_SUCCESS_INCALL,       /* Success in call tone */
    SRV_PROF_TONE_IMPS_CONTACT_ONLINE,          /* IMPS tone - contact online */
    SRV_PROF_TONE_IMPS_NEW_MESSAGE,             /* IMPS tone - new message */
    SRV_PROF_TONE_IMPS_CONTACT_INVITATION,      /* IMPS tone - contact invitation */
    SRV_PROF_TONE_IMPS_CHATROOM_NOTIFICATION,   /* IMPS tone - chat room notification */
    SRV_PROF_TONE_FILE_ARRIVAL,         /* File arrival tone */
    SRV_PROF_TONE_SENT,                 /* Sent tone */
    SRV_PROF_TONE_DELETED,              /* Deleted tone */
    SRV_PROF_TONE_PROBLEM,              /* Problem tone */
    SRV_PROF_TONE_CONFIRM,              /* Confirm tone */

                                        /*61*/
    SRV_PROF_TONE_EXPLICITLY_SAVE,      /* Explicitly save tone */
    SRV_PROF_TONE_NOT_AVAILABLE,        /* Not available tone */
    SRV_PROF_TONE_ANS_PHONE,            /* Answer phone tone */
    SRV_PROF_TONE_WAP_PUSH,             /* WAP push tone */
    SRV_PROF_TONE_REMIND,               /* Remind tone */
    SRV_PROF_TONE_KEYPAD,               /* Key pad tone */
    SRV_PROF_TONE_EM,                   /* Engineering mode tone */
    SRV_PROF_TONE_FM,                   /* Factory mode tone */
    SRV_PROF_TONE_PHONEBOOK,            /* Phonebook tone */
    SRV_PROF_TONE_POC,                  /* PoC tone */

                                        /*71*/
    SRV_PROF_TONE_VOIP,                 /* Voip tone */
    SRV_PROF_TONE_SAT,                  /* SAT tone */
    SRV_PROF_TONE_GSM_BUSY,             /* GSM busy */
    SRV_PROF_TONE_WAITING_CALL,         /* Waiting call */
    SRV_PROF_TONE_ALARM_IN_CALL,        /* Alarm in call */

    NONE_TONE = SRV_PROF_TONE_NONE,
    ERROR_TONE = SRV_PROF_TONE_ERROR,
    CONNECT_TONE = SRV_PROF_TONE_CONNECT,
    CAMP_ON_TONE = SRV_PROF_TONE_CAMP_ON,
    WARNING_TONE = SRV_PROF_TONE_WARNING,
    INCOMING_CALL_TONE = SRV_PROF_TONE_INCOMING_CALL,
    ALARM_TONE = SRV_PROF_TONE_ALARM,
    POWER_ON_TONE = SRV_PROF_TONE_POWER_ON,
    POWER_OFF_TONE = SRV_PROF_TONE_POWER_OFF,
    COVER_OPEN_TONE = SRV_PROF_TONE_COVER_OPEN,
    COVER_CLOSE_TONE = SRV_PROF_TONE_COVER_CLOSE,
    MESSAGE_TONE = SRV_PROF_TONE_SMS,
    KEYPAD_PLAY_TONE = SRV_PROF_TONE_KEYPAD,
    SUCCESS_TONE = SRV_PROF_TONE_SUCCESS,
    SAVE_TONE = SRV_PROF_TONE_SAVE,
    EMPTY_LIST_TONE = SRV_PROF_TONE_EMPTY_LIST,
    GENERAL_TONE = SRV_PROF_TONE_GENERAL_TONE,
    SMS_IN_CALL_TONE = SRV_PROF_TONE_SMS_INCALL,
    AUX_TONE = SRV_PROF_TONE_AUX,
    WARNING_TONE_IN_CALL = SRV_PROF_TONE_WARNING_INCALL,
    ERROR_TONE_IN_CALL = SRV_PROF_TONE_ERROR_INCALL,
    CONNECT_TONE_IN_CALL = SRV_PROF_TONE_CONNECT_INCALL,
    SUCCESS_TONE_IN_CALL = SRV_PROF_TONE_SUCCESS_INCALL,
    BATTERY_LOW_TONE = SRV_PROF_TONE_BATTERY_LOW,
    BATTERY_WARNING_TONE = SRV_PROF_TONE_BATTERY_WARNING,
    CALL_REMINDER_TONE = SRV_PROF_TONE_CALL_REMINDER,
    CCBS_TONE = SRV_PROF_TONE_CCBS,
    CONGESTION_TONE = SRV_PROF_TONE_CONGESTION,
    AUTH_FAIL_TONE = SRV_PROF_TONE_AUTH_FAIL,
    NUM_UNOBTAIN_TONE = SRV_PROF_TONE_NUM_UNOBTAIN,
    CALL_DROP_TONE = SRV_PROF_TONE_CALL_DROP,
    IMPS_CONTACT_ONLINE_TONE = SRV_PROF_TONE_IMPS_CONTACT_ONLINE,
    IMPS_NEW_MESSAGE_TONE = SRV_PROF_TONE_IMPS_NEW_MESSAGE,
    IMPS_CONTACT_INVITATION_TONE = SRV_PROF_TONE_IMPS_CONTACT_INVITATION,
    IMPS_CHATROOM_NOTIFICATION_TONE = SRV_PROF_TONE_IMPS_CHATROOM_NOTIFICATION,
    CARD2_INCOMING_CALL_TONE = SRV_PROF_TONE_INCOMING_CALL_CARD2,
    CARD2_MESSAGE_TONE = SRV_PROF_TONE_SMS_CARD2,
    FILE_ARRIVAL_TONE = SRV_PROF_TONE_FILE_ARRIVAL,

    /*for op11*/
    SENT_TONE = SRV_PROF_TONE_SENT,
    DELETED_TONE = SRV_PROF_TONE_DELETED,
    PROBLEM_TONE = SRV_PROF_TONE_PROBLEM,
    NETWORK_PROBLEM_TONE = SRV_PROF_TONE_PROBLEM,
    CONFIRM_TONE = SRV_PROF_TONE_CONFIRM,
    EXPLICITLY_SAVE_TONE = SRV_PROF_TONE_EXPLICITLY_SAVE,
    NOT_AVAILABLE_TONE = SRV_PROF_TONE_NOT_AVAILABLE,        /*Oops tone*/
    ANS_PHONE_TONE = SRV_PROF_TONE_ANS_PHONE,
    SMS_TONE = SRV_PROF_TONE_SMS,
    MMS_TONE = SRV_PROF_TONE_MMS,
    EMAIL_TONE = SRV_PROF_TONE_EMAIL,
    VOICE_TONE = SRV_PROF_TONE_VOICE,
    WAP_PUSH_TONE = SRV_PROF_TONE_WAP_PUSH,
    REMIND_TONE = SRV_PROF_TONE_REMIND,

    SRV_PROF_TONE_END_OF_ENUM           /* End of enum */
}srv_prof_tone_enum;
//>>

//<< from maui/mcu/plutommi/framework/commonscreens/commonscreensinc/AlertScreen.h
/* popup property struct */
typedef struct {
    mmi_proc_func               callback;               /* callback proc */
    void *                      user_tag;               /* user parameter that will pass to callback proc */
    mmi_frm_screen_rotate_enum  rotation;               /* rotation type */
    U32                         f_sliding_effect   : 1; /* enable sliding effect or not (default: true) */
    U32                         f_msg_icon         : 1; /* show msg icon or not (default: true) */
    U32                         f_auto_dismiss     : 1; /* auto dismiss or not (default: true) */
    U32                         f_play_tone        : 1; /* play tone or not (default: true) */
    MMI_ID                      parent_id;              /* parent group id */
    U32                         duration;               /* duration in ms. */
    U16                         msg_icon;               /* img id for msg icon. only works if f_msg_icon is true */
    srv_prof_tone_enum               tone_id;                /* tone id. only works if f_play_tone is true */
} mmi_popup_property_struct;

#if 0
/*****************************************************************************
 * FUNCTION
 *  mmi_popup_display
 * DESCRIPTION
 *  display popup with given properties.
 * PARAMETERS
 *  title           : [IN]  pointer to title string
 *  event_type      : [IN]  alert event type
 *  arg             : [IN]  pointer to popup property
 * RETURNS
 *  alert id
 *****************************************************************************/
extern MMI_ID mmi_popup_display(
                    WCHAR* title,
                	mmi_event_notify_enum event_type,
                	mmi_popup_property_struct *arg);
#endif
//>>

#if 0
//<< from maui/mcu/plutommi/mmi/inc/CustDataRes.h
/*****************************************************************************
 * <GROUP STRING>
 * FUNCTION
 *  GetString
 * DESCRIPTION
 *  Function to get string associated with the stringId
 *
 *  The function uses the variable CurrMaxStringId inside the for loop which
 *  is set inside the WriteRes.c file.
 * PARAMETERS
 *  StringId        : [IN]        StringId
 * RETURNS
 *  S8* the String Mapped with the Id.
 *****************************************************************************/
extern S8 *GetString(U16 StringId);
//>>
#else
#define GetString(x)   NULL
#endif

//<< from maui/mcu/plutommi/framework/commonfiles/commoninc/Mmi_frm_scenario_gprot.h
/*
 * If you want to config the group node behavior, 
 * using this value to decide need to smart close the group by the framework.
 */
typedef enum _mmi_group_enter_flag
{
    /* The default behavior of the group node. The group will exist even there are no screens in it. */
	MMI_FRM_NODE_NONE_FLAG = 0,

    /* enable smart close group when group is empty */
    MMI_FRM_NODE_SMART_CLOSE_FLAG,

    /* trigger smart close only when close procedure */
    MMI_FRM_NODE_SMART_CLOSE_CAUSED_BY_CLOSE_FLAG,
    /* group enter flag max */
	MMI_FRM_GROUP_ENTER_MAX
}mmi_group_enter_flag, mmi_group_attrib_enum;

/*****************************************************************************
 * FUNCTION
 *  mmi_frm_group_create
 * DESCRIPTION
 *  This function is used to create the new group. When the applications call
 *  this function, framework create group instance.
 * PARAMETERS
 *  parent_id :         [IN] The parent group ID will have the new group.
 *                           If the parm is GRP_ID_ROOT, it means the
 *                           created group(group_id) is the stand-alone scenario.
 *  group_id :          [IN] The new group ID will enter. If the param is
 *                           GRP_ID_AUTO_GEN, framework will generate 
 *                           group id automatically and return to the caller.
 *  proc :              [IN] The proc function of the new group.
 *  user_data :         [IN] The user_data of this group.
 * RETURNS
 *  return the group id
 *****************************************************************************/
MMI_ID mmi_frm_group_create (MMI_ID parent_id, MMI_ID group_id, mmi_proc_func proc, void *user_data);

/*****************************************************************************
 * FUNCTION
 *  mmi_frm_group_enter
 * DESCRIPTION
 *  This function is used for enter the new group after the caller uses
 *  mmi_frm_group_create() to create the group. When the applications call
 *  this function, it executes the previous node (group/screen) inactive process.
 *  After the applications call mmi_frm_group_enter(), they should call
 *  mmi_frm_scrn_enter to enter the new screen.
 * PARAMETERS
 *  group_id :          [IN] The new group ID will enter. The param value 
 *                           should be group_id from mmi_frm_group_create().
 *  flag :              [IN] It is the group's flag
 *                           MMI_FRM_NODE_SMART_CLOSE_FLAG: will close group automatically
 *                           when the gorup is empty
 *                           0: use group default behavior
 * RETURNS
 *  return the group id
 *****************************************************************************/
MMI_ID mmi_frm_group_enter (MMI_ID group_id, mmi_group_enter_flag flag);

/*****************************************************************************
 * FUNCTION
 *  mmi_frm_scrn_enter
 * DESCRIPTION
 *  This function is used for enter the new screen. When the applications call
 *  this function, it executes the previous screen's exit handler, and adds the
 *  previous screen in the history, then clears the key handlers and entries the
 *  new screen with the parameter setting if the parent id is active group. 
 *  If the parent id isn't active group, we only add the screen in scenario tree.
 * PARAMETERS
 *  parent_id :         [IN] The parent group ID will have the new group.
 *                           If the parm is GRP_ID_ROOT, it means the
 *                           created group(group_id) is the stand-alone scenario.
 *  scrn_id :           [IN] The new screen ID will enter.
 *  exit_proc :         [IN] The exit function of the new screen.
 *  entry_proc :        [IN] The entry function of the new screen.
 *  scrn_type :         [IN] The screen type of the new screen.
 * RETURN VALUES
 *  MMI_TRUE: Because the parent id is active group, executing entry new 
 *              screen procedure and this screen to be the active screen.
 *  MMI_FALSE: Because the parent is isn't active group, we only add this screen
 *             in scenario tree without executing entry new screen procedure.
 *****************************************************************************/
MMI_BOOL mmi_frm_scrn_enter (
    MMI_ID parent_id,
    MMI_ID scrn_id,
    FuncPtr exit_proc,
    FuncPtr entry_proc,
    mmi_frm_scrn_type_enum scrn_type);

/*****************************************************************************
 * FUNCTION
 *  mmi_frm_scrn_get_gui_buf
 * DESCRIPTION
 *  This function is used for get the gui buffer of the screen.
 * PARAMETERS
 *  parent_id :         [IN] The group ID.
 *  scrn_id :           [IN] The screen ID.
 * RETURNS
 *  Get the gui buffer of queried screen ID
 *****************************************************************************/
U8* mmi_frm_scrn_get_gui_buf (MMI_ID parent_id, MMI_ID scrn_id);

/*****************************************************************************
 * FUNCTION
 *  mmi_frm_scrn_get_active_id
 * DESCRIPTION
 *  This function is used for get the active screen ID.
 *  If the active screen belongs to the original screen-history mechanism,
 *  we will invoke GetActiveScreenId();
 * PARAMETERS
 *  void
 * RETURNS
 *  Return the active screen ID.
 *****************************************************************************/
#if 0
MMI_ID mmi_frm_scrn_get_active_id (void);
#else
#define mmi_frm_scrn_get_active_id() 0
#endif

/*****************************************************************************
 * FUNCTION
 *  mmi_frm_scrn_close
 * DESCRIPTION
 *  This function is used for close the screen in scenario tree. 
 *  If the closed screen is the active screen, framework will find out the
 *  previous screen and execute the goback process (EVT_ID_SCRN_GOBACK, EVT_ID_SCRN_DEINIT).
 *  If the closed screen isn't the active screen, framework will delete
 *  the screen in the scenario tree (EVT_ID_SCRN_DELETE_REQ, EVT_ID_SCRN_DEINIT).
 *  The application could return non-MMI_RET_OK in screen's leave_proc
 *  to notify the framework can't delete its screen.
 *  When the framework close the screen, framework will emit mmi_scenario_evt_enum
 *  to the screen leave function.
 * PARAMETERS
 *  parent_id :         [IN] The group ID.
 *  scrn_id:            [IN] The screen ID.
 * RETURN VALUES
 *  MMI_RET_OK: close the screen successfully.
 *  others: the return value from the screen leave function that don't allow to close it.
 *****************************************************************************/
#if 0
mmi_ret mmi_frm_scrn_close (MMI_ID parent_id, MMI_ID scrn_id);
#else
#define mmi_frm_scrn_close(pid, sid) MMI_RET_OK
#endif
//>>

//<< from maui/mcu/plutommi/service/inc/gsinc/Gs_srv_vfx_framework.h
#define GLOBAL_BASE                     (1)
//>>

//<< from maui/mcu/plutommi/mmi/inc/GlobalResDef.h
#define FRM_RESERVED_GROUP_NUMBER   	(64)

/* The global screen or goup id */
typedef enum _GLOBAL_SCR_ENUM
{
    SCR_ID_GLOBAL_DUMMY = GLOBAL_BASE + 1,   /* all screen id shall be put below this one */
    SCR_ID_DUMMY = SCR_ID_GLOBAL_DUMMY,
    SCR_ID_DUMMY_INTERNAL,
    GRP_ID_AUTO_GEN,                        /* the auto-gen id, framework will genearte the group id automatically */
    GRP_ID_ROOT,                            /* the scenario root id */
    GRP_ID_AUTO_GEN_BASE,
    GRP_ID_AUTO_GEN_MAX = GRP_ID_AUTO_GEN_BASE + FRM_RESERVED_GROUP_NUMBER,
    GRP_ID_ORIG_MECH,
    GRP_ID_DANGLE,
    SRV_ID_ROOT,
    GRP_ID_BK,

    GLOBAL_SCR_MAX
}GLOBAL_SCR_ENUM;

typedef enum _STR_GLOBAL_LIST_ENUM
{
                                    /* [English], [Description] */
    STR_GLOBAL_0 = GLOBAL_BASE,     /* "0", "Global String- 0" */
    STR_GLOBAL_1,                   /* "1", "Global String- 1" */
    STR_GLOBAL_2,                   /* "2", "Global String- 2" */
    STR_GLOBAL_3,                   /* "3", "Global String- 3" */
    STR_GLOBAL_4,                   /* "4", "Global String- 4" */
    STR_GLOBAL_5,                   /* "5", "Global String- 5" */
    STR_GLOBAL_6,                   /* "6", "Global String- 6" */
    STR_GLOBAL_7,                   /* "7", "Global String- 7" */
    STR_GLOBAL_8,                   /* "8", "Global String- 8" */
    STR_GLOBAL_9,                   /* "9", "Global String- 9" */
    STR_GLOBAL_10,                  /* "10", "Global String- 10" */
    STR_GLOBAL_ABORT,               /* "Abort", "Global String- Abort" */
    STR_GLOBAL_ABORTING,            /* "Aborting", "Global String- Aborting" */
    STR_GLOBAL_ACCEPT,              /* "Accept", "Global String- Accept" */
    STR_GLOBAL_ACCOUNTS,            /* "Accounts",	"Accounts- configuration folder" */
    STR_GLOBAL_ACTIVATE,            /* "Activate", "Global String- Activate" */
    STR_GLOBAL_ACTIVATED,           /* "Activated", "Global String- Activated" */  
    STR_GLOBAL_ADD,                 /* "Add", "Global String- Add" */
    STR_GLOBAL_ADD_TO_EXISTING_CONTACT,
                                    /* "Add to existing contact", "Global String- Add to existing contact" */
    STR_GLOBAL_ADDRESS,             /* "Address", "Global String- Address" */
    STR_GLOBAL_ADVANCED,            /* "Advanced", "Global String- Advanced" */
    STR_GLOBAL_ALARM,               /* "Alarm", "Global String- Alarm" */
    STR_GLOBAL_ALWAYS_ASK,          /* "Always ask", "Global String- Always ask" */
    STR_GLOBAL_AM,                  /* "AM", "Global String- AM" */
    STR_GLOBAL_AS_EMAIL,            /* "As Email", "Global String- As Email- sub option of sending command" */
    STR_GLOBAL_AS_MULTIMEDIA_MSG,   /* "As multimedia message", "Global String- As multimedia message- sub option of sending command" */
    STR_GLOBAL_AS_TEXT_MESSAGE,     /* "As text message", "Global String- As text message- sub option of sending command" */
    STR_GLOBAL_ATTACHMENT,          /* "Attachment", "Global String- Attachment" */
    STR_GLOBAL_AUDIO,               /* "Audio", "Global String- Audio" */
    STR_GLOBAL_AUTHENTICATION,      /* "Authentication","Global String- Authentication" */
    STR_GLOBAL_AUTOMATIC,           /* "Automatic","Global String- Automatic" */
    STR_GLOBAL_BACK,                /* "Back", "Global String- Back" */
    STR_GLOBAL_BLUETOOTH,           /* "Bluetooth", "Global String- Bluetooth" */
    STR_GLOBAL_BOOKMARKS,           /* "Bookmarks", "Global String- Bookmarks" */
    STR_GLOBAL_BUSY_TRY_LATER,      /* "Busy", "STR_GLOBAL_BUSY_TRY_LATER" */
    STR_GLOBAL_CANCEL,              /* "Cancel", "Global String- Cancel" */
    STR_GLOBAL_CANCELLING,          /* "Cancelling", "Global String- Cancelling" */
    STR_GLOBAL_CHOOSE_STORAGE,      /* "Choose storage", "Global String- Choose storage" */
    STR_GLOBAL_CLEAR,               /* "Clear", "Global String- Clear" */
    STR_GLOBAL_CLOSE,               /* "Close", "Global String- Close" */
    STR_GLOBAL_CONFIRM,             /* "Confirm", "Global String- Confirm" */
    STR_GLOBAL_CONNECT,             /* "Connect", "Global String- Connect" */
    STR_GLOBAL_CONNECTING,          /* "Connecting", "Global String- Connecting" */
    STR_GLOBAL_CONTENT,             /* "Content", "Global String- Content" */
    STR_GLOBAL_CONTINUE,            /* "Cont.", "Global String- Cont.- use as LSK for length limit" */
    STR_GLOBAL_COPY,                /* "Copy", "Global String- Copy" */
    STR_GLOBAL_COPYING,             /* "Copying", "Global String- Copying" */
    STR_GLOBAL_CREATE_NEW_CONTACT,  /* "Create new contact", "Global String- Create new contact" */
    STR_GLOBAL_CURRENTLY_NOT_AVAILABLE,
                                    /* "Currently not available", "Global String- the services/operations aren't workable in some situations" */
    STR_GLOBAL_CURRENTLY_NOT_AVAILABLE_IN_CALL,
                                    /* "Not available during the call", "Global String- the services/operations aren't workable during the call" */
    STR_GLOBAL_CUSTOM,              /* "Custom", "Global String- Custom- user defined" */
    STR_GLOBAL_DATA_ACCOUNT,        /* "Data account", "Global String- Data account" */
    STR_GLOBAL_DATE,                /* "Date", "Global String- Date" */
    STR_GLOBAL_DAY,                 /* "Day", "Global String- Day" */
    STR_GLOBAL_DEACTIVATE,          /* "Deactivate", "Global String- Deactivate" */
    STR_GLOBAL_DEFAULT,             /* "Default", "Global String- Default" */
    STR_GLOBAL_DELETE,              /* "Delete", "Global String- Delete" */
    STR_GLOBAL_DELETE_ALL,          /* "Delete All", "Global String- Delete All" */
    STR_GLOBAL_DELETED,             /* "Deleted", "Global String- Deleted" */
    STR_GLOBAL_DELETING,            /* "Deleting", "Global String- Deleting" */
    STR_GLOBAL_DETAILS,             /* "Details", "Global String- Details" */
    STR_GLOBAL_DIAL,                /* "Call", "Global String- Call" */
    STR_GLOBAL_DIALLED_CALLS,       /* "Dialled call", "Global string- Dialled calls" */
    STR_GLOBAL_DISCARD,             /* "Discard","Global String- Discard" */
    STR_GLOBAL_DISCONNECT,          /* "Disconnect", "Global String- Disconnect" */
    STR_GLOBAL_DISCONNECTING,       /* "Disconnecting", "Global String- Disconnecting" */
    STR_GLOBAL_DONE,                /* "Done", "Global String- Done" */
    STR_GLOBAL_DOWNLOAD,            /* "Download", "STR_GLOBAL_DOWNLOAD" */
    STR_GLOBAL_DOWNLOAD_ASK,        /* "Download?", "STR_GLOBAL_DOWNLOAD_ASK" */
    STR_GLOBAL_DOWNLOADING,         /* "Downloading", "STR_GLOBAL_DOWNLOADING" */
    STR_GLOBAL_DRAFTS,              /* "Drafts", "Global String- Drafts" */
    STR_GLOBAL_DRM_FILE_EXISTS,     /* "File Exists", "Global String- File Exists" */
    STR_GLOBAL_DRM_FS_ERROR,        /* "File Access Error", "Global String- File Access Error" */
    STR_GLOBAL_DRM_INVALID_FORMAT,  /* "Format Error", "Global String- Format Error" */
    STR_GLOBAL_DRM_NON_DRM,         /* "Not DRM File", "Global String- Not DRM File" */
    STR_GLOBAL_DRM_PROCESSING,      /* "DRM Processing", "Global String- DRM Processing" */
    STR_GLOBAL_DRM_PROHIBITED,      /* "DRM Prohibited", "Global String- DRM Prohibited" */
    STR_GLOBAL_DRM_RO_RECEIVED,     /* "DRM Rights Received", "Global String- DRM Rights Received" */
    STR_GLOBAL_DRM_SIZE_TOO_BIG,    /* "Size Too Big", "Global String- Size Too Big" */
    STR_GLOBAL_EARPHONE,            /* "Earphone", "Global String- Earphone" */
    STR_GLOBAL_EARPHONE_IN,         /* "Earphone plugged in!", "Global String- String Associated with Ear Phone Plug In Popup." */
    STR_GLOBAL_EARPHONE_OUT,        /* "Earphone plugged Out", "Global String- Earphone plugged Out" */
    STR_GLOBAL_EDIT,                /* "Edit", "Global String- Edit" */
    STR_GLOBAL_EDIT_BEFORE_CALL,    /* "Edit before call", "Global String- Edit before call" */
    STR_GLOBAL_EMAIL,               /* "Email", "Global String- Email" */
    STR_GLOBAL_EMPTY,               /* "Empty", "Global String- Empty" */
    STR_GLOBAL_EMPTY_LIST,          /* "<Empty>", "Global String- <Empty>" */
    STR_GLOBAL_ERROR,               /* "Error", "Global String- Error" */
    STR_GLOBAL_EXIT,                /* "Exit", "Global String- Exit" */
    STR_GLOBAL_EXPORT,              /* "Export", "Global String- Export" */
    STR_GLOBAL_FAILED,              /* "Failed", "Global String- Failed" */
    STR_GLOBAL_FAILED_TO_SAVE,      /* "Failed to save", "Global String- Failed to save" */
    STR_GLOBAL_FAILED_TO_SEND,      /* "Failed to send", "Global String- Failed to send" */
    STR_GLOBAL_FAST,                /* "Fast", "Global String- Fast" */
    STR_GLOBAL_FILE_ALREADY_EXISTS, /* "File already exists", "Global String- File already exists" */
    STR_GLOBAL_FILE_NOT_FOUND,      /* "File not found", "Global string- File not found" */
    STR_GLOBAL_FILENAME,            /* "Filename", "Global String- Filename" */
    STR_GLOBAL_FILENAME_ALREADY_EXISTS,
                                    /* "Filename already exists", "Global String- Filename already exists" */
    STR_GLOBAL_FOLD,                /* "Fold", "Global String- Fold" */
    STR_GLOBAL_FORMAT,              /* "Format", "Global String- Format- the type of file" */
    STR_GLOBAL_FORWARD,             /* "Forward", "Global String- Forward- send message to other recipients" */
    STR_GLOBAL_GO_TO,               /* "Go to", "Global String- Go to- connect the URL" */
    STR_GLOBAL_GO_TO_URL,           /* "Go to URL", "Global String- Go to URL" */
    STR_GLOBAL_GPRS,                /* "GPRS", "Global String- GPRS" */
    STR_GLOBAL_GSM,                 /* "GSM", "Global String- GSM" */
    STR_GLOBAL_HELP,                /* "Help", "Global String- Help- show the usage tips or notes" */
    STR_GLOBAL_HIGH,                /* "High", "Global String- High" */
    STR_GLOBAL_HOME,                /* "Home", "Global String- Home" */
    STR_GLOBAL_HOUR,                /* "Hour", "Global String- Hour" */
    STR_GLOBAL_HTTP,                /* "HTTP", "Global String- HTTP" */
    STR_GLOBAL_IMPORT,              /* "Import", "Global String- Import" */
    STR_GLOBAL_INBOX,               /* "Inbox", "Global String- Inbox" */
    STR_GLOBAL_INCOMING_CALL,       /* "Incoming call", "Global String- Incoming call" */
    STR_GLOBAL_INPUT_METHOD,        /* "Input Method", "Global String- Input Method" */
    STR_GLOBAL_INPUT_METHOD_OPTIONS,        /* "Input Method", "Global String- Input Method" */
    STR_GLOBAL_INSTALL,             /* "Install", "Global String- Install" */
    STR_GLOBAL_INSTALLING,          /* "Installing", "Global String- Installing" */
    STR_GLOBAL_INSUFFICIENT_MEMORY, /* "Insufficient memory", "Global String- Insufficient memory- not enough RAM to operate" */
    STR_GLOBAL_INVALID,             /* "Invalid", "Global String- Invalid" */
    STR_GLOBAL_INVALID_DATA_ACCOUNT,    /* "Invalid data account. Configure?", "Global String- Invalid data account. Configure?" */
    STR_GLOBAL_INVALID_EMAIL_ADDRESS,   /* "Invalid Email address", "Global String- Invalid Email address" */
    STR_GLOBAL_INVALID_EMAIL_ADDRESSES, /* "Invalid Email addresses", "Global String- Invalid Email addresses" */
    STR_GLOBAL_INVALID_FILENAME,    /* "Invalid filename", "Global String- Invalid filename" */
    STR_GLOBAL_INVALID_FORMAT,      /* "Invalid format", "Global String- Invalid format" */
    STR_GLOBAL_INVALID_INPUT,       /* "Invalid input", "Global String- Invalid input" */
    STR_GLOBAL_INVALID_NUMBER,      /* "Invalid number", "Global String- Invalid number" */
    STR_GLOBAL_INVALID_NUMBERS,     /* "Invalid numbers", "Global String- Invalid numbers" */
    STR_GLOBAL_INVALID_RECIPIENTS,  /* "Invalid recipients", "Global String- Invalid recipients" */
    STR_GLOBAL_INVALID_URL,         /* "Invalid URL", "Global String- Invalid URL" */
    STR_GLOBAL_LANDSCAPE,           /* "Landscape", "Global String- Landscape" */
    STR_GLOBAL_LIST,                /* "List", "Global String- List" */
    STR_GLOBAL_LOADING,             /* "Loading", "Global String- Loading" */
    STR_GLOBAL_LOGIN,               /* "Login", "Global String- Login" */
    STR_GLOBAL_LOGO,                /* "Logo", "Global String- Logo" */
    STR_GLOBAL_LOGOUT,              /* "Logout", "Global String- Logout" */
    STR_GLOBAL_LOW,                 /* "Low", "Global String- Low" */
    STR_GLOBAL_MANUAL,              /* "Manual", "Global String- Manual" */
    STR_GLOBAL_MARK,                /* "Mark", "Global String- Mark" */
    STR_GLOBAL_MARK_ALL,            /* "Mark all", "Global String- Mark all" */
    STR_GLOBAL_MAXIMUM,             /* "Maximum", "Global String- Maximum" */
    STR_GLOBAL_MEDIUM,              /* "Medium", "Global String- Medium" */
    STR_GLOBAL_MEMORY_CARD,         /* "Memory card", "Global String- Memory card" */
    STR_GLOBAL_MEMORY_FULL,         /* "Memory full", "Global String- Memory full- indicate user not save again" */
    STR_GLOBAL_MEMORY_STATUS,       /* "Memory status", "Global String- Memory status" */
    STR_GLOBAL_MESSAGE,             /* "Message", "Global String- Message" */
    STR_GLOBAL_MIN,                 /* "Min", "Global String- Min" */
    STR_GLOBAL_MISSED_CALLS,        /* "Missed calls", "Global String- Missed calls" */
    STR_GLOBAL_MMS,                 /* "MMS", "Global String- MMS" */
    STR_GLOBAL_MODE,                /* "Mode", "Global String- Mode" */
    STR_GLOBAL_MORE,                /* "More", "Global String- More" */
    STR_GLOBAL_MOVE,                /* "Move", "Global String- Move" */
    STR_GLOBAL_MOVING,              /* "Moving", "Global String- Moving" */
    STR_GLOBAL_MULTIMEDIA,          /* "Multimedia", "Global String- Multimedia" */
    STR_GLOBAL_MULTIMEDIA_MESSAGE,  /* "Multimedia message ", "Global String- Multimedia message " */
    STR_GLOBAL_MUTE,                /* "Mute", "Global String- Mute" */
    STR_GLOBAL_NETWORK_SETTINGS,    /* "Network settings", "Global String- Network settings" */
    STR_GLOBAL_NEXT,                /* "Next", "Global String- Next" */
    STR_GLOBAL_NO,                  /* "No", "Global String- No" */
    STR_GLOBAL_NO_MEMORY_CARD,      /* "No Memory Card", "[Notify-No Memory Card]" */
    STR_GLOBAL_NONE,                /* "None", "Global String- None" */
    STR_GLOBAL_NORMAL,              /* "Normal", "Global String- Normal" */
    STR_GLOBAL_NOT_AVAILABLE,       /* "Not Available", "Global String- Not Available" */
    STR_GLOBAL_NOT_AVAILABLE_DURING_VIDEO_CALL,
                                    /* "Not available during video call", "Global String- Not available during video call" */
    STR_GLOBAL_NOT_ENOUGH_MEMORY,   /* "Not enough memory", "Global String- Not enough memory- memory is too small to save" */
    STR_GLOBAL_NOT_SUPPORT_AT_PC_SIMULATOR, 
                                    /* "Not Support at PC Simulator", "Global String- Not Support at PC Simulator" */
    STR_GLOBAL_NOT_SUPPORTED,       /* "Not supported", "Global String- Not supported" */
    STR_GLOBAL_OFF,                 /* "Off", "Global String- Off" */
    STR_GLOBAL_OK,                  /* "Ok", "Global String- OK" */
    STR_GLOBAL_ON,                  /* "On", "Global String- On"*/
    STR_GLOBAL_OPEN,                /* "Open", "Global String- Open" */
    STR_GLOBAL_OPTIONS,             /* "Option", "Global String- Options" */
    STR_GLOBAL_OUTBOX,              /* "Outbox", "Global String- Outbox" */
    STR_GLOBAL_OVERWRITE_EXISTING_FILE, 
                                    /* "Overwrite existing file?", "Global String- Overwrite existing file?" */
    STR_GLOBAL_PASSWORD,            /* "Password","Global String- Password menu" */
    STR_GLOBAL_PAUSE,               /* "Pause","Global String- Pause" */
    STR_GLOBAL_PHONE,               /* "Phone", "Global String- Phone" */
    STR_GLOBAL_PICTURE,             /* "Picture","Global String- Picture" */
    STR_GLOBAL_PLAY,                /* "Play","Global String- Play" */
    STR_GLOBAL_PLEASE_INPUT_THE_FILENAME,
                                    /* "Please input the filename", "Global String- Prompt user the filename is empty" */
    STR_GLOBAL_PLEASE_WAIT,         /* "Please Wait", "Global String- Please Wait" */
    STR_GLOBAL_PM,                  /* "PM", "Global String- PM" */
    STR_GLOBAL_PORTRAIT,            /* "Portrait", "Global String- Portrait" */
    STR_GLOBAL_PREFERRED_STORAGE,   /* "Preferred storage", "Global String- Preferred storage" */
    STR_GLOBAL_PREV,                /* "Prev.", "Global String- Prev." */
    STR_GLOBAL_PREVIEW,             /* "Preview", "Global String- Preview" */
    STR_GLOBAL_PRIMARY_SIM,         /* "Primary SIM", "Global String- Primary SIM" */
    STR_GLOBAL_PRINT,               /* "Print", "Global String- Print" */
    STR_GLOBAL_PRIORITY,            /* "Priority", "Global String- Priority" */
    STR_GLOBAL_PROXY,               /* "Proxy", "Global String- Proxy" */
    STR_GLOBAL_QUIT,                /* "Quit", "Global String- Quit" */
    STR_GLOBAL_READ,                /* "Read", "Global String- Read" */
    STR_GLOBAL_RECEIVED,            /* "Received", "Global String- Received" */
    STR_GLOBAL_RECEIVED_CALLS,      /* "Received calls", "Global string- Received calls" */
    STR_GLOBAL_RECEIVING,           /* "Receiving", "Global String- Receiving" */
    STR_GLOBAL_RECIPIENTS,          /* "Recipients", "Global String- Recipients- recipient list name" */
    STR_GLOBAL_REFRESH,             /* "Refresh", "Global String- Refresh" */
    STR_GLOBAL_REFRESHING,          /* "Refreshing", "Global String- Refreshing" */
    STR_GLOBAL_REMINDER,            /* "Reminder", "Global String- Reminder" */
    STR_GLOBAL_REMOVE,              /* "Remove", "Global String- Remove- delete the reference link or remove accessory from phone" */
    STR_GLOBAL_REMOVED,             /* "Removed", "Global String- Removed- remove successfully" */
    STR_GLOBAL_RENAME,              /* "Rename", "Global String- Rename" */
    STR_GLOBAL_REPLACE,             /* "Replace", "Global String- Replace" */
    STR_GLOBAL_REPLY,               /* "Reply", "Global String- Reply" */
    STR_GLOBAL_RESEND,              /* "Resend", "Global String- Resend" */
    STR_GLOBAL_RESET,               /* "Reset", "Global String- Reset" */
    STR_GLOBAL_RESTORE,             /* "Restore", "Global String- Restore" */
    STR_GLOBAL_RESUME,              /* "Resume", "Global String- Resume" */
    STR_GLOBAL_RINGTONE,            /* "Ringtone", "Global String- Ringtone" */
    STR_GLOBAL_SAVE,                /* "Save", "Global String- Save" */
    STR_GLOBAL_SAVE_ASK,            /* "Save?", "Global String- Save?" */
    STR_GLOBAL_SAVE_TO_PHONEBOOK,   /* "Save to Phonebook", "Global String- Save to Phonebook" */
    STR_GLOBAL_SAVED,               /* "Saved", "Global String- Saved"); */
    STR_GLOBAL_SAVING,              /* "Saving", "Global String- Saving" */
    STR_GLOBAL_SEARCH,              /* "Search", "Global String- Search" */
    STR_GLOBAL_SEARCHING,           /* "Searching", "Global String- Searching" */
    STR_GLOBAL_SEC,                 /* "Sec", "Global String- Sec" */
    STR_GLOBAL_SECONDARY_SIM,       /* "Secondary SIM", "Global String- Secondary SIM" */
    STR_GLOBAL_SECURITY,            /* "Security", "Global String- Security" */
    STR_GLOBAL_SELECT,              /* "Select", "Global String- Select" */
    STR_GLOBAL_SELECT_ALL,          /* "Select all", "Global String- Select all" */
    STR_GLOBAL_SEND,                /* "Send", "Global String- Send" */
    STR_GLOBAL_SEND_MESSAGE,        /* "Send message", "Global String- Send message" */
    STR_GLOBAL_SEND_MULTIMEDIA_MESSAGE,
                                    /* "Send multimedia message", "The option of send multimedia message" */
    STR_GLOBAL_SEND_TEXT_MESSAGE,   /* "Send text message", "The option of send text message" */
    STR_GLOBAL_SENDING,             /* "Sending", "Global String- Sending"*/
    STR_GLOBAL_SENT,                /* "Sent", "Global String- Sent- send successfully" */
    STR_GLOBAL_SET_AS,              /* "Set as", "Global String- Set as" */
    STR_GLOBAL_SETTINGS,            /* "Settings", "Global String- Settings" */
    STR_GLOBAL_SHARE,               /* "Share", "Global String- Share" */
    STR_GLOBAL_SILENT,              /* "Silent", "Global String- Silent" */
    STR_GLOBAL_SIM,                 /* "SIM", "Global String- SIM" */
    STR_GLOBAL_SIM_1,               /* "SIM 1", "Global String- SIM 1" */
    STR_GLOBAL_SIM_2,               /* "SIM 2", "Global String- SIM 2" */
    STR_GLOBAL_SLOW,                /* "Slow", "Global String- Slow" */
    STR_GLOBAL_SMS,                 /* "SMS", "Global String- SMS" */
    STR_GLOBAL_SORT,                /* "Sort", "Global String- Sort" */
    STR_GLOBAL_SORT_BY,             /* "Sort by", "Global String- Sort by- include sub options" */
    STR_GLOBAL_START,               /* "Start", "Global String- Start" */
    STR_GLOBAL_STOP,                /* "Stop", "Global String- Stop" */
    STR_GLOBAL_STORAGE,             /* "Storage", "Global String- Storage" */
    STR_GLOBAL_SUBJECT,             /* "Subject", "Global String- Subject" */
    STR_GLOBAL_SUBMIT,              /* "Submit","Accept" */
    STR_GLOBAL_TEXT,                /* "Text", "Global String- Text" */
    STR_GLOBAL_TEXT_MESSAGE,        /* "Text message", "Global String- Text message- SMS" */
    STR_GLOBAL_TEMPLATE,            /* "Template", "Global String- Template" */
    STR_GLOBAL_TIME,                /* Time", "Global String- Time" */
    STR_GLOBAL_UDP_PORT,            /* "UDP port", "Global String- UDP port" */
    STR_GLOBAL_UNAVAILABLE_IN_FLIGHT_MODE,
                                    /* "Unavailable in flight mode", "Global String- Unavailable in flight mode" */
    STR_GLOBAL_UNAVAILABLE_SIM,     /* "Unavailable SIM", "Global String- Unavailable SIM" */
    STR_GLOBAL_UNFINISHED,          /* "Unfinished", "Global String- Unfinished" */
    STR_GLOBAL_UNMARK,              /* "Unmark", "Global String- Unmark" */
    STR_GLOBAL_UNMARK_ALL,          /* "Unmark all", "Global String- Unmark all" */
    STR_GLOBAL_UNSELECT_ALL,        /* "Unselect all", "Global String- Unselect all" */
    STR_GLOBAL_UNSUPPORTED_FORMAT,  /* "Unsupported Format", "Global String- Unsupported Format" */
    STR_GLOBAL_URL,                 /* "URL", "Global String- URL" */
    STR_GLOBAL_UPDATE,              /* "Update", "Global String- Update" */
    STR_GLOBAL_UPLOAD,              /* "Upload", "Global String- Upload" */
    STR_GLOBAL_USE,                 /* "Use", "Global String- Use" */
    STR_GLOBAL_USE_TEMPLATE,        /* "Use Template", "Global String- Use Template" */
    STR_GLOBAL_USERNAME,            /* "User Name", "Global String- User name menu" */
    STR_GLOBAL_VIA_BLUETOOTH,       /* "Via Bluetooth", "Global String- Via Bluetooth- sub option of sending command" */
    STR_GLOBAL_VIA_INFRARED,        /* "Via infrared", "Global String- Via infrared- sub option of sending command" */
    STR_GLOBAL_VIBRATION,           /* "Vibration", "Global String- Vibration" */
    STR_GLOBAL_VIDEO,               /* "Video", "Global String- Video" */
    STR_GLOBAL_VIEW,                /* "View", "Global String- View" */
    STR_GLOBAL_VOLUME,              /* "Volume", "Global String- Volume" */
    STR_GLOBAL_WALLPAPER,           /* "Wallpaper", "Global String- Wallpaper" */
    STR_GLOBAL_WAP,                 /* "WAP", "Global String- WAP" */
    STR_GLOBAL_WEEK,                /* "WEEK", "Global String- Week" */
    STR_GLOBAL_WIFI,                /* "Wi-Fi", "Global String- Wi-Fi" */
    STR_GLOBAL_WIFI_ONLY,           /* "Wi-Fi only", "Global String- Wi-Fi only" */
    STR_GLOBAL_YEAR,                /* "Year", "Global String- Year" */
    STR_GLOBAL_YES,	                /* "Yes", "Global String- Yes" */
    STR_GLOBAL_CLIPBOARD,           /* "Clipboard", "Global String- Clipboard" */
    STR_GLOBAL_INSERT_MEMORY_CARD,  /* "Please insert memory card", "Global String- Please insert memory card" */
    STR_GLOBAL_MC_REMOVED_USE_PHONE,    /* "Memory card removed. Use phone?", "Global String- Memory card removed. Use phone?" */
    STR_GLOBAL_MC_REMOVED,              /* "Memory card removed", "Global String- Memory card removed" */
    STR_GLOBAL_MC_INSERTED,             /* "Memory card inserted", "Global String- Memory card inserted" */
    STR_GLOBAL_OTG_REMOVED_USE_PHONE,   /* "OTG device removed. Phone will be used.", "Global String- OTG device removed. Phone will be used." */
    STR_GLOBAL_OTG_REMOVED_USE_PHONE_QUERY,   /* "OTG device removed. Use phone?", "Global String- OTG device removed. Use phone?" */    
    STR_GLOBAL_OTG_REMOVED,             /* "OTG device removed", "Global String- OTG device removed" */
    STR_GLOBAL_OTG_CONNECTED,           /* "OTG device connected", "Global String- OTG device connected" */
    STR_GLOBAL_NOT_AVAILABLE_IN_MASS_STORAGE_MODE,  /* "Not available in mass storage mode", "Global String- Not available in mass storage mode" */
	STR_GLOBAL_NO_NUMBER,           /* "No number", "Global String- No number" */

    STR_GLOBAL_SUNDAY,              /* "Sunday", "Global String- Sunday" */
    STR_GLOBAL_MONDAY,              /* "Monday", "Global String- Monday" */
    STR_GLOBAL_TUESDAY,             /* "Tuesday", "Global String- Tuesday" */
    STR_GLOBAL_WEDNESDAY,           /* "Wednesday", "Global String- Wednesday" */
    STR_GLOBAL_THURSDAY,            /* "Thursday", "Global String- Thursday" */
    STR_GLOBAL_FRIDAY,              /* "Friday", "Global String- Friday" */
    STR_GLOBAL_SATURDAY,            /* "Saturday", "Global String- Saturday" */
    STR_GLOBAL_SUNDAY_SHORT,        /* "Sun", "Global String- Sun" */
    STR_GLOBAL_MONDAY_SHORT,        /* "Mon", "Global String- Mon" */
    STR_GLOBAL_TUESDAY_SHORT,       /* "Tue", "Global String- Tue" */
    STR_GLOBAL_WEDNESDAY_SHORT,     /* "Wed", "Global String- Wed" */
    STR_GLOBAL_THURSDAY_SHORT,      /* "Thu", "Global String- Thu" */
    STR_GLOBAL_FRIDAY_SHORT,        /* "Fri", "Global String- Fri" */
    STR_GLOBAL_SATURDAY_SHORT,      /* "Sat", "Global String- Sat" */
    STR_GLOBAL_JANUARY_SHORT,       /* "Jan", "Global String- Jan" */
    STR_GLOBAL_FEBRUARY_SHORT,      /* "Feb", "Global String- Feb" */
    STR_GLOBAL_MARCH_SHORT,         /* "Mar", "Global String- Mar" */
    STR_GLOBAL_APRIL_SHORT,         /* "Apr", "Global String- Apr" */
    STR_GLOBAL_MAY_SHORT,           /* "May", "Global String- May" */
    STR_GLOBAL_JUNE_SHORT,          /* "Jun", "Global String- Jun" */
    STR_GLOBAL_JULY_SHORT,          /* "Jul", "Global String- Jul" */
    STR_GLOBAL_AUGEST_SHORT,        /* "Aug", "Global String- Aug" */
    STR_GLOBAL_SEPTEMBER_SHORT,     /* "Sep", "Global String- Sep" */
    STR_GLOBAL_OCTOBER_SHORT,       /* "Oct", "Global String- Oct" */
    STR_GLOBAL_NOVEMBER_SHORT,      /* "Nov", "Global String- Nov" */
    STR_GLOBAL_DECEMBER_SHORT,      /* "Dec", "Global String- Dec" */

    STR_GLOBAL_DIAL_VIDEO_CALL,

    STR_GLOBAL_SIM_3,
    STR_GLOBAL_SIM_4,
    /* add new resource before here */
	STR_GLOBAL_END

}STR_GLOBAL_LIST_ENUM;

typedef enum _IMG_GLOBAL_LIST_ENUM
{
    IMG_GLOBAL_OK = GLOBAL_BASE,
    IMG_GLOBAL_BACK,
    IMG_GLOBAL_YES,
    IMG_GLOBAL_NO,
    IMG_GLOBAL_OPTIONS,
    /* Start of numeric list icons */
    IMG_GLOBAL_LSTART,
    IMG_GLOBAL_L1,
    IMG_GLOBAL_L2,
    IMG_GLOBAL_L3,
    IMG_GLOBAL_L4,
    IMG_GLOBAL_L5,
    IMG_GLOBAL_L6,
    IMG_GLOBAL_L7,
    IMG_GLOBAL_L8,
    IMG_GLOBAL_L9,
    IMG_GLOBAL_L10,
    IMG_GLOBAL_L11,
    IMG_GLOBAL_L12,
    IMG_GLOBAL_L13,
    IMG_GLOBAL_L14,
    IMG_GLOBAL_L15,
    IMG_GLOBAL_L16,
    IMG_GLOBAL_L17,
    IMG_GLOBAL_L18,
    IMG_GLOBAL_L19,
    IMG_GLOBAL_L20,
    IMG_GLOBAL_L21,
    IMG_GLOBAL_L22,
    IMG_GLOBAL_L23,
    IMG_GLOBAL_L24,
    IMG_GLOBAL_L25,
    IMG_GLOBAL_L26,
    IMG_GLOBAL_L27,
    IMG_GLOBAL_L28,
    IMG_GLOBAL_L29,
    IMG_GLOBAL_L30,
    IMG_GLOBAL_LEND,
    /* end of numeric list icons */
    IMG_STATUS,
    IMG_TIME,
    IMG_REPEAT,
    IMG_CAL,
    IMG_GLOBAL_WARNING,
    IMG_GLOBAL_QUESTION,
    IMG_GLOBAL_SAVE,
    IMG_GLOBAL_DELETED,
    IMG_GLOBAL_DEFAULT,
    IMG_GLOBAL_PROGRESS,
    IMG_GLOBAL_LOADING = IMG_GLOBAL_PROGRESS, /* Do not add image id between this two */
    IMG_GLOBAL_ERROR,
    IMG_GLOBAL_UNFINISHED = IMG_GLOBAL_ERROR, /* Do not add image id between this two */
    IMG_GLOBAL_INFO,
    IMG_GLOBAL_EMPTY = IMG_GLOBAL_INFO, /* Do not add image id between this two */
    IMG_VICON,
    IMG_GLOBAL_CLEAR,
    IMG_EARPHONE_POPUP_SUBLCD,
    IMG_GLOBAL_SUB_MENU_BG,
    IMG_GLOBAL_MAIN_MENU_BG,
    IMG_NONE,
    IMG_GLOBAL_FONT_1,
    IMG_GLOBAL_FONT_2,
    IMG_GLOBAL_FONT_3,
    IMG_GLOBAL_FONT_4,
    IMG_GLOBAL_FONT_5,
    IMG_GLOBAL_FONT_6,
    IMG_GLOBAL_FONT_7,
    IMG_GLOBAL_FONT_8,
    IMG_GLOBAL_FONT_9,
    IMG_GLOBAL_FONT_10,
    IMG_GLOBAL_FONT_11,
    IMG_GLOBAL_FONT_12,
    IMG_GLOBAL_FONT_13,
    IMG_GLOBAL_FONT_14,
    IMG_GLOBAL_FONT_15,
    IMG_GLOBAL_FONT_16,
    IMG_GLOBAL_FONT_MAX = IMG_GLOBAL_FONT_16,
    IMG_GLOBAL_SUCCESS,
    IMG_GLOBAL_ACTIVATED = IMG_GLOBAL_SUCCESS, /* Do not add image id between this two */
    IMG_GLOBAL_FAIL,
    
    IMG_GLOBAL_CALL_CSK,
    IMG_GLOBAL_COMMON_CSK,
    IMG_GLOBAL_DIAL_PAD_CSK,
    IMG_GLOBAL_FORWARD_MSG_CSK,
    IMG_GLOBAL_MARK_CSK,
    IMG_GLOBAL_NEXT_CSK,
    IMG_GLOBAL_OPTION_CSK,
    IMG_GLOBAL_REPLY_MSG_CSK,
    IMG_GLOBAL_SAVE_CSK,
    IMG_GLOBAL_SEARCH_CSK,
    IMG_GLOBAL_SEND_MSG_CSK,
    IMG_GLOBAL_WEB_BROWSER_CSK,

    /* Remove these CSK icon after W10.17 - Start */
    IMG_GLOBAL_PHB_CSK,
    IMG_GLOBAL_SELECT_CSK,
    IMG_GLOBAL_VIEW_CSK,
    IMG_GLOBAL_READ_MSG_CSK,
    IMG_GLOBAL_BOOKMARK_CSK,    
    /* Remove these CSK icon after W10.17 - End */


#ifdef __MMI_FTE_SUPPORT__
    IMG_GLOBAL_TOOLBAR_ADD_CONTACT,
    IMG_GLOBAL_TOOLBAR_ADD_CONTACT_DISABLED,
    IMG_GLOBAL_TOOLBAR_ADD,
    IMG_GLOBAL_TOOLBAR_ADD_DISABLED,
    IMG_GLOBAL_TOOLBAR_ATTACHMENT,
    IMG_GLOBAL_TOOLBAR_ATTACHMENT_DISABLED,
    IMG_GLOBAL_TOOLBAR_BOOKMARK,
    IMG_GLOBAL_TOOLBAR_BOOKMARK_DISABLED,
    IMG_GLOBAL_TOOLBAR_CALL,
    IMG_GLOBAL_TOOLBAR_CALL_DISABLED,
    IMG_GLOBAL_TOOLBAR_DELETE,
    IMG_GLOBAL_TOOLBAR_DELETE_DISABLED,
    IMG_GLOBAL_TOOLBAR_DETAIL,
    IMG_GLOBAL_TOOLBAR_DETAIL_DISABLED,
    IMG_GLOBAL_TOOLBAR_EDIT,
    IMG_GLOBAL_TOOLBAR_EDIT_DISABLED,
    IMG_GLOBAL_TOOLBAR_FORWARD_MESSAGE,
    IMG_GLOBAL_TOOLBAR_FORWARD_MESSAGE_DISABLED,
    IMG_GLOBAL_TOOLBAR_MEETING,
    IMG_GLOBAL_TOOLBAR_MEETING_DISABLED,
    IMG_GLOBAL_TOOLBAR_OPEN,
    IMG_GLOBAL_TOOLBAR_OPEN_DISABLED,
    IMG_GLOBAL_TOOLBAR_REPLY_MESSAGE,
    IMG_GLOBAL_TOOLBAR_REPLY_MESSAGE_DISABLED,
    IMG_GLOBAL_TOOLBAR_SEARCH,
    IMG_GLOBAL_TOOLBAR_SEARCH_DISABLED,
    IMG_GLOBAL_TOOLBAR_SEND_MESSAGE,
    IMG_GLOBAL_TOOLBAR_SEND_MESSAGE_DISABLED,
    IMG_GLOBAL_TOOLBAR_WRITE_MESSAGE,
    IMG_GLOBAL_TOOLBAR_WRITE_MESSAGE_DISABLED,
#endif /* __MMI_FTE_SUPPORT__ */

    IMG_GLOBAL_SIM1,
    IMG_GLOBAL_SIM2,
    IMG_GLOBAL_SIM3,
    IMG_GLOBAL_SIM4,

    IMG_GLOBAL_TABBAR_SIM1,
    IMG_GLOBAL_TABBAR_SIM2,
    IMG_GLOBAL_TABBAR_SIM3,
    IMG_GLOBAL_TABBAR_SIM4,
    
    IMG_GLOBAL_TOOLBAR_VIDEO_CALL,
    IMG_GLOBAL_TOOLBAR_VIDEO_CALL_DISABLED,

    /* add new resource before here */
    IMG_GLOBAL_END
}IMG_GLOBAL_LIST_ENUM;
//>>

#if 0
//<< from maui/mcu/plutommi/MMI/Inc/mmi_res_range_def.h
#define GPS_BASE                    ((U16) GET_RESOURCE_BASE(SRV_GPS))
//>>
#else
#define GPS_BASE                    0                  
#endif

//<< from maui/mcu/plutommi/customer/customerinc/Screen_enum.h
typedef enum
{
	SCR_ID_GPS_SETTING_MAIN = GPS_BASE + 1,
	SCR_ID_GPS_SETTING_AGPS_SETTING,
	SCR_ID_GPS_SETTING_AGPS_PROFILE,
	SCR_ID_GPS_SETTING_AGPS_PROFILE_OPTION,
	SCR_ID_GPS_SETTING_AGPS_PROFILE_EDIT,
	SCR_ID_GPS_SETTING_ASK_SAVE_SCREEN,
	SCR_ID_GPS_SETTING_BT_PAIRING,
	SCR_ID_GPS_SETTING_INPUT_EDITOR,
	SCR_ID_GPS_SETTING_INPUT_EDITOR_OPTION,
	SCR_ID_GIS_SETTING_PORT_CHOOSE,
	SCR_ID_GPS_SETTING_DUMMY,
	SRC_ID_GPS_MGR_NOTIFY,
	SRC_ID_GPS_MGR_MTLR_NOTIFY,
	SCR_ID_AGPS_SUPL_OTAP_PROF_INFO,
	SCR_ID_AGPS_SUPL_OTAP_PROF_LIST,

	SCR_ID_SRV_GPS_ALL
} mmi_srv_gpsscr_id_enum;
//>>

//<< from maui/mcu/custom/common/venus_mmi/Custom_events_notify.h
/*****************************************************************************
 * FUNCTION
 *  mmi_get_event_based_image
 * DESCRIPTION
 *  Get image based popup event from table.
 * PARAMETERS
 *  event_id      : [IN]			Popup event enum
 * RETURNS
 *  image id.
 *****************************************************************************/
extern U16 mmi_get_event_based_image(mmi_event_notify_enum event_id);
//>>

//<< from maui/mcu/applib/misc/include/App_datetime.h
typedef struct
{
    kal_uint16 nYear;
    kal_uint8 nMonth;
    kal_uint8 nDay;
    kal_uint8 nHour;
    kal_uint8 nMin;
    kal_uint8 nSec;
    kal_uint8 DayIndex; /* 0=Sunday */
} applib_time_struct;
//>>

//<< from maui/mcu/plutommi/service/inc/NwInfoSrvGprot.h
/*****************************************************************************
 * FUNCTION
 *  srv_nw_info_get_protocol
 * DESCRIPTION
 *  Get the protocol of current network.
 * PARAMETERS
 *  sim         : [IN] Which protocol layer of SIM
 * RETURNS
 *  MMI_TRUE if supports.
 * SEE ALSO
 *  srv_nw_info_cell_supports, EVT_ID_SRV_NW_INFO_PROTOCOL_CAPABILITY_CHANGED
 *****************************************************************************/
extern mmi_network_enum srv_nw_info_get_protocol(mmi_sim_enum sim);
//>>

//<< ???: I can not find how maui defines these...
#define MMI_TRC_GPS_MGR_ICON_HIDE    "MMI_TRC_GPS_MGR_ICON_HIDE"
#define MMI_TRC_GPS_MGR_ICON_SHOW    "MMI_TRC_GPS_MGR_ICON_SHOW"
#define MMI_TRC_GPS_MGR_REQ_LIST_PRT "[%d] start_mode=%d, work_mode=%d, gps_mode=%d, state=%d, reqID=%d, is_used=%d"
//>>

//<<Refer to CW's 1048 GPSMgr.c, so I add these...
#define StartTimer mmi_gps_mgr_adp_start_timer
#define StopTimer  mmi_gps_mgr_adp_stop_timer

#define mmi_gps_get_port_number     mmi_gps_mgr_adp_get_port_number

#define isInCall                    mmi_gps_mgr_adp_is_in_call

#define gui_start_timer(count, callback)     // Nothing for SP
//>>

#define NETWORK_TYPE_2G 0
#define NETWORK_TYPE_3G 1


typedef struct
{
#if defined(__AGPS_USER_PLANE__)
    mmi_gps_manager_request_struct req_list[MAX_SUPL_REQ_NUM];
    U16 cur_id;
    S32 port;
    mmi_gps_manager_notify_struct notify_list[MAX_SUPL_REQ_NUM];
    U16 req_id_name[SUPL_MMI_MAX_REQ_LEN];
    mmi_gps_manager_push_struct push_list[MAX_SUPL_REQ_NUM];
    mmi_gps_manager_push_struct temp_push;
    S16 cur_push_num;
    /* L modify */
    S16 cur_notify_num;
    mmi_gps_work_mode_enum cur_work_mode;
    U8 open_gps_num[UART_WORK_MODE_COUNT];
    MMI_BOOL is_pos;
    MMI_BOOL is_last_pos;
    MMI_BOOL is_count_duration;
    U32 pos_start_time;
    MMI_BOOL is_in_call;
    MMI_BOOL is_time_out;
    MMI_BOOL is_regiest_call_notify;
    U8 app_id;
    srv_gps_notify_callback app_callback;
    void *app_user_data;
#endif
    MMI_BOOL time_is_sync;
    MMI_BOOL emerge_call;
    U8 rrc_setting;
    MMI_ID screen_group_gid;
}mmi_gps_manager_context_struct;

mmi_gps_manager_context_struct g_gps_mgr_ctx;


#endif /* __AGPS_SWIP_REL__ */
#endif /* __GPS_MGR_ADP_H__ */
