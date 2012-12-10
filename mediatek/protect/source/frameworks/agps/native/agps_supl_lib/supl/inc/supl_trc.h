/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2006
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

/*******************************************************************************
 * Filename:
 * ---------
 *   supl_trc.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This is trace map definition of SUPL task.
 *
 * Author:
 * -------
 *   Leo Hu
 *
 *==============================================================================
 * 				HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *------------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * Aug 24 2009 mtk00563
 * [MAUI_01943924] [AGPS] AGPS 2G control plane check-in
 * 
 *
 * May 19 2008 mtk00563
 * [MAUI_00763856] [AGPS] revise SUPL trace info.
 * 
 *
 * May 13 2008 mtk00563
 * [MAUI_00763856] [AGPS] revise SUPL trace info.
 * 
 *
 * May 13 2008 mtk00563
 * [MAUI_00770944] [SIM] SIM_READY_IND generic interface when sim is initialized
 * 
 *
 * May 2 2008 mtk00563
 * [MAUI_00763856] [AGPS] revise SUPL trace info.
 * 
 *
 * Apr 18 2008 mtk00563
 * [MAUI_00759106] [AGPS] Check-in AGPS feature.
 * 
 *
 * Feb 25 2008 mtk00563
 * [MAUI_00623349] [AGPS] AGPS feature check-in
 * 
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *==============================================================================
 *******************************************************************************/

#ifndef _SUPL_TRC_H
#define _SUPL_TRC_H
//$$
#if 0
#ifndef GEN_FOR_PC

   #ifndef _STACK_CONFIG_H
   #error "stack_config.h should be included before tst_config.h"
   #endif

#else
   #include "kal_trace.h"
#endif /* GEN_FOR_PC */


#ifndef _KAL_TRACE_H
   #error "kal_trace.h should be included before tst_trace.h"
#endif
#endif //(0)
#define SUPL_TRACE_GROUP    TRACE_GROUP_1

/* FUNC */
#define SUPL_FUNC_LOG(TAG)                      kal_trace(TRACE_FUNC, TAG)

/* STATE */
#define SUPL_STATE_CHANGE_LOG(sid, old_state, new_state) \
                                                kal_trace (TRACE_STATE, SUPL_STATE_CHANGE_TRC, (sid), (old_state), (new_state))
#define SUPL_CONN_STATE_CHANGE_LOG(sid, old_state, new_state) \
                                                kal_trace (TRACE_STATE, SUPL_CONN_STATE_CHANGE_TRC, (sid), (old_state), (new_state))

/* INFO */
#define SUPL_FSM_EVENT_LOG(sid, state, event) \
                                                kal_trace(TRACE_INFO, SUPL_FSM_EVENT_TRC, (sid), (state), (event))

#define SUPL_CONN_FSM_EVENT_LOG(sid, state, event) \
                                                kal_trace(TRACE_INFO, SUPL_CONN_FSM_EVENT_TRC, (sid), (state), (event))
/* GPS_GET_POS, GPS_NO_POS */
#define SUPL_INFO_LOG(TAG)                      kal_trace(TRACE_INFO, TAG)
#define SUPL_INFO_LOG1(TAG, ARG1)               kal_trace(TRACE_INFO, TAG, (ARG1))
#define SUPL_INFO_LOG2(TAG, ARG1, ARG2)         kal_trace(TRACE_INFO, TAG, (ARG1), (ARG2))
#define SUPL_INFO_LOG3(TAG, ARG1, ARG2, ARG3)   kal_trace(TRACE_INFO, TAG, (ARG1), (ARG2), (ARG3))


/* WARNNING */
#define SUPL_WARNING_LOG(TAG)                   kal_trace(TRACE_WARNING, TAG)
#define SUPL_WARNING_LOG1(TAG, ARG1)            kal_trace(TRACE_WARNING, TAG, (ARG1))
#define SUPL_WARNING_LOG2(TAG, ARG1,ARG2)       kal_trace(TRACE_WARNING, TAG, (ARG1),(ARG2))
#define SUPL_WARNING_LOG3(TAG, ARG1,ARG2,ARG3)  kal_trace(TRACE_WARNING, TAG, (ARG1),(ARG2),(ARG3))


/* ERROR */
#define SUPL_ULP_PROTO_ERR_LOG(type)            kal_trace(TRACE_ERROR, SUPL_PROTO_ERR_TRC, (type))
#define SUPL_ERROR_LOG(TAG)                     kal_trace(TRACE_ERROR, TAG)
#define SUPL_ERROR_LOG1(TAG,ARG1)               kal_trace(TRACE_ERROR, TAG, (ARG1))
#define SUPL_ERROR_LOG2(TAG,ARG1,ARG2)          kal_trace(TRACE_ERROR, TAG, (ARG1), (ARG2))
#define SUPL_ERROR_LOG3(TAG,ARG1,ARG2,ARG3)     kal_trace(TRACE_ERROR, TAG, (ARG1), (ARG2), (ARG3))  


/* TRACE PEER */
#define SUPL_TRACE_PEER_LOG(TAG)                kal_trace(TRACE_PEER, TAG)

/* SUPL TRACE GROUP */

/* FSM transition */
#define SUPL_FSM_RECV_ULP_MSG_LOG(type)         kal_trace(SUPL_TRACE_GROUP, SUPL_FSM_RECV_ULP_MSG_TRC, (type))
#define SUPL_FSM_SEND_ULP_MSG_LOG(type)         kal_trace(SUPL_TRACE_GROUP, SUPL_FSM_SEND_ULP_MSG_TRC, (type))
/* Timer */
#define SUPL_TIMER_EXPIRY_LOG(tid)              kal_trace(SUPL_TRACE_GROUP, SUPL_TIMER_EXPIRED_TRC, (tid))
#define SUPL_TIMER_START_LOG(tid, tval)         kal_trace(SUPL_TRACE_GROUP, SUPL_TIMER_START_TRC, (tid), (tval))
#define SUPL_TIMER_STOP_LOG(tid)                kal_trace(SUPL_TRACE_GROUP, SUPL_TIMER_STOP_TRC, (tid))


/* ULP Messages */
#define SUPL_ULP_DECODE_MSG_LOG(type)           kal_trace(TRACE_PEER, SUPL_ULP_MESSAGE_DECODE_TRC, (type))
#define SUPL_ULP_ENCODE_MSG_LOG(type)           kal_trace(TRACE_PEER, SUPL_ULP_MESSAGE_ENCODE_TRC, (type))

#define SUPL_ULP_STATUS_LOG(code)               kal_trace(TRACE_PEER, SUPL_ULP_STATUS_TRC,(code))
#define SUPL_ULP_POS_METHOD_LOG(type)           kal_trace(TRACE_PEER, SUPL_ULP_POS_METHOD_TRC, (type))
#define SUPL_ULP_QOP_LOG(HA, VA, AGE, DELAY)    kal_trace(TRACE_PEER, SUPL_ULP_QOP_TRC, (HA), (VA), (AGE), (DELAY))
#define SUPL_ULP_LID_LOG(rat, mcc, mnc, lac, ci, c)  kal_trace(TRACE_PEER, SUPL_ULP_LID_TRC, (rat), (mcc), (mnc), (lac), (ci), (c))
#define SUPL_ULP_HDR_VERSION_LOG(v1, v2, v3)    kal_trace(TRACE_PEER, SUPL_ULP_HDR_VERSION_TRC, (v1), (v2), (v3))
#define SUPL_ULP_HDR_SET_SESSION_ID_LOG(v1, v2) kal_trace(TRACE_PEER, SUPL_ULP_HDR_SET_SESSION_ID_TRC, (v1), (v2))
#define SUPL_ULP_NOTIFY_TYPE_LOG(type)          kal_trace(TRACE_PEER, SUPL_ULP_NOTIFY_TYPE_TRC, (type))
#define SUPL_ULP_IPV4_LOG(ip1, ip2, ip3, ip4)   kal_trace(TRACE_PEER, SUPL_ULP_IPV4_TRC, (ip1), (ip2), (ip3), (ip4))
#define SUPL_ULP_SLP_MODE_LOG(type)             kal_trace(TRACE_PEER, SUPL_ULP_SLP_MODE_TRC, (type))
#define SUPL_ULP_HDR_SLP_SESSION_ID_LOG(v1, v2, v3, v4, v5)\
                                                kal_trace(TRACE_PEER, SUPL_ULP_HDR_SLP_SESSION_ID_TRC, (v1), (v2), (v3), (v4), (v5))

#define SUPL_ULP_VER_LOG(v1, v2, v3, v4, v5, v6, v7, v8)\
                                                kal_trace(TRACE_PEER, SUPL_ULP_VER_TRC, (v1), (v2), (v3), (v4), (v5), (v6), (v7), (v8))

#define SUPL_ULP_FILTER_LOG(v1, v2, v3, v4, v5, v6, v7, v8, v9)\
                                                kal_trace(TRACE_PEER, SUPL_ULP_FILTER_TRC, (v1), (v2), (v3), (v4), (v5), (v6), (v7), (v8), (v9))

#define SUPL_ULP_POSITION_LOG(time, week, year, month, day, hour, min, sec, la, lo, al)\
                                                kal_trace(TRACE_PEER, SUPL_ULP_POSITION_TRC, (time), (week), (year), (month), (day), (hour), (min), (sec), (la), (lo), (al))

#define SUPL_RECV_IMSI_LOG( v1, v2, v3, v4, v5, v6, v7, v8, v9)\
                                                kal_trace(TRACE_INFO, SUPL_IMSI_RECV_TRC, (v1), (v2), (v3), (v4), (v5), (v6), (v7), (v8), (v9))
#define SUPL_IMSI_SET_ID_LOG( v1, v2, v3, v4, v5, v6, v7, v8)\
                                                kal_trace(TRACE_PEER, SUPL_IMSI_SET_ID_TRC, (v1), (v2), (v3), (v4), (v5), (v6), (v7), (v8))

BEGIN_TRACE_MAP(MOD_SUPL)

    /* FUNC */
    TRC_MSG(SUPL_ULP_MESSAGE_DECODE_FUNC_TRC, "[SUPL][ULP] supl_ulp_decode()")
    TRC_MSG(SUPL_ULP_MESSAGE_ENCODE_FUNC_TRC, "[SUPL][ULP] supl_ulp_encode()")
    TRC_MSG(SUPL_ULP_CHECK_SESSION_ID_FUNC_TRC, "[SUPL][ULP] supl_ulp_check_session_id()")

    /* Error */
    TRC_MSG(SUPL_INVALID_MSG_TRC, "[SUPL] wrong message: src=[%Mmodule_type], msg_id=[%Mmsg_type]")
    TRC_MSG(SUPL_CONN_FSM_ERROR_TRC, "[SUPL][CONN_FSM] Fail - Event:[%Msupl_conn_event_enum], Result:[%Msupl_int_result_enum], Cause:[%Msupl_int_cause_enum]")
    TRC_MSG(SUPL_CONN_SOC_FAIL_TRC, "[SUPL][SOC] Fail - cause:[%Msoc_error_enum]")
    TRC_MSG(SUPL_CONN_TLS_FAIL_TRC, "[SUPL][TLS] Fail - cause:[%Mtls_error_enum]")
    TRC_MSG(SUPL_ULP_ASN_DECODE_FAIL_TRC, "[SUPL][ULP] ASN decode fail:(%d)")
    TRC_MSG(SUPL_ULP_ASN_DECODE_ERROR_TRC, "[SUPL][ULP] ASN decode error:%Msupl_ulp_decode_fail_enum, asn1_ret:%d")
    TRC_MSG(SUPL_ULP_ASN_DECODE_LEN_ERROR_TRC, "[SUPL][ULP] ASN decode error:%Msupl_ulp_decode_fail_enum, dec_len:%d, len:%d")
    TRC_MSG(SUPL_UNPACK_PUSH_FAIL_TRC, "[SUPL] Unpack PUSH message fail")

    /* TRACE_STATE */
    TRC_MSG(SUPL_STATE_CHANGE_TRC, "[SUPL][FSM] State Change! Session_id:[%d], Old State:[%Msupl_session_state_enum], New State:[%Msupl_session_state_enum]")
    TRC_MSG(SUPL_CONN_STATE_CHANGE_TRC, "[SUPL][CONN_FSM] Connection State Change! Session_id:[%d], Old State:[%Msupl_conn_state_enum], New State:[%Msupl_conn_state_enum]")

    /* TRACE_INFO */
    TRC_MSG(SUPL_SESSION_TYPE_CHANGE_TRC, "[SUPL]Session type is changed to %Msupl_session_type_enum")
    TRC_MSG(SUPL_GPS_NO_POSITION_TRC, "[SUPL][GPS] No Available Position")
    TRC_MSG(SUPL_GPS_GET_POSITION_TRC, "[SUPL][GPS] Get Available Position")
    TRC_MSG(SUPL_GPS_RECV_POS_TRC, "[SUPL] Receive Position from MMI")
    

    /* TRACE_WARNING */
    TRC_MSG(SUPL_FSM_DANGLING_EVENT_TRC, "[SUPL]Dangling Event -- Session:[%d], state:[%Msupl_session_state_enum], event:[%Msupl_event_enum]")
    TRC_MSG(SUPL_CONN_DANGLING_EVENT_TRC, "[SUPL]Dangling Event -- Session:[%d], state:[%Msupl_conn_state_enum], event:[%Msupl_conn_event_enum]")
    TRC_MSG(SUPL_ULP_RECV_UNEXPECT_MSG_TRC, "[SUPL][ULP] Receive unexpected message, type: %Msupl_ulp_msg_type_enum")
    TRC_MSG(SUPL_CONN_SOC_INVALID_SOC_ID_TRC, "[SUPL][SOC] Receive soc_id: %d, but no matched session id")
    TRC_MSG(SUPL_LCSP_RECV_LOC_ID_ON_CLOSED_SESSION_TRC, "[SUPL][LCSP]Receive LOCATION_ID_RSP on closed session")
    TRC_MSG(SUPL_LCSP_RECV_GPS_POS_ON_CLOSED_SESSION_TRC, "[SUPL][LCSP]Receive POS_GAD_RSP on closed session")

    /* Timer */
    TRC_MSG(SUPL_TIMER_EXPIRED_TRC, "[SUPL] Tmer expired, timer type = [%Msupl_timer_enum]")
    TRC_MSG(SUPL_TIMER_START_TRC,   "[SUPL] Start timer, type = [%Msupl_timer_enum], timeout=[%d]")
    TRC_MSG(SUPL_TIMER_STOP_TRC,   "[SUPL] Stop timer, type = [%Msupl_timer_enum]") 
    TRC_MSG(SUPL_TIMER_NOT_ENOUGH_TRC, "[SUPL] There is NO more timer slots!")

    /* SUPL TRACE GROUP */
    TRC_MSG(SUPL_FSM_EVENT_TRC, "[SUPL][FSM] Session_id:[%d], FSM STATE:[%Msupl_session_state_enum], EVENT:[%Msupl_event_enum]")
    TRC_MSG(SUPL_CONN_FSM_EVENT_TRC, "[SUPL][CONN_FSM] Session_id:[%d], FSM STATE:[%Msupl_conn_state_enum], EVENT:[%Msupl_conn_event_enum]")
    TRC_MSG(SUPL_LCSP_INVALID_PAYLOAD_TYPE_TRC, "[SUPL]][LCSP] Invalid payload type: %d")

    TRC_MSG(SUPL_WAIT_SUPL_END_IGNORE_MSG_TRC, "[SUPL] Waiting for SUPL END being Sent, ignore incoming messages")
    TRC_MSG(SUPL_FSM_RECV_ULP_MSG_TRC,"[SUPL][FSM] Receive ULP MSG - %Msupl_ulp_msg_type_enum")
    TRC_MSG(SUPL_FSM_SEND_ULP_MSG_TRC,"[SUPL][FSM] Send ULP MSG - %Msupl_ulp_msg_type_enum")
    TRC_MSG(SUPL_ULP_ERROR_TRC,"[SUPL][ULP] Status Error!! [%Msupl_ulp_status_enum]")
    TRC_MSG(SUPL_PROTO_ERR_TRC,"[SUPL][ULP] Protocol Error!! [%Msupl_proto_err_enum]")
    TRC_MSG(SUPL_FSM_SESSION_ENQUEUE_TRC, "[SUPL][FSM] Session[%d] is enqueued. Total sessions in queue: %d")
    TRC_MSG(SUPL_FSM_SESSION_DEQUEUE_TRC, "[SUPL][FSM] Session[%d] is dequeued. Total sessions in queue: %d")
    TRC_MSG(SUPL_FSM_SESSION_PREEMPT_TRC, "[SUPL][FSM] Session[%d] preempts session[%d], and session[%d] is enqueued.")

    /* TRACE_PEER */
    TRC_MSG(SUPL_ULP_MESSAGE_DECODE_TRC, "[NW->MS][SUPL]ULP message: %Msupl_ulp_msg_type_enum")
    TRC_MSG(SUPL_ULP_MESSAGE_ENCODE_TRC, "[MS->NW][SUPL]ULP message: %Msupl_ulp_msg_type_enum")


    /* ASN */
    TRC_MSG(SUPL_ASN_ENCODE_FUNC_TRC, "[SUPL][ASN] Encode ULP message. -- START")
    TRC_MSG(SUPL_ASN_DECODE_FUNC_TRC, "[SUPL][ASN] Decode ULP message. -- START")
    TRC_MSG(SUPL_ASN_ENCODE_FUNC_END_TRC, "[SUPL][ASN] Encode ULP message. -- END")
    TRC_MSG(SUPL_ASN_DECODE_FUNC_END_TRC, "[SUPL][ASN] Decode ULP message. -- END")
    TRC_MSG(SUPL_ASN_RET_TRC, "[SUPL][ASN] Return value:%d")
    TRC_MSG(SUPL_ASN_ENCODE_LENGTH_TRC, "[SUPL][ASN] ASN encoded length: %d")

    /* ULP */
    TRC_MSG(SUPL_ULP_HDR_VERSION_TRC, "[SUPL][ULP] Version:%d.%d.%d")
    TRC_MSG(SUPL_ULP_HDR_SET_SESSION_ID_TRC, "[SUPL][ULP] SET Session ID:%d, SET ID type: %Msupl_set_id_enum")
    TRC_MSG(SUPL_ULP_HDR_SLP_SESSION_ID_TRC, "[SUPL][ULP] SLP Session ID:0x%02x 0x%02x 0x%02x 0x%02x, SLP ID type: %Msupl_slp_id_enum") 
    TRC_MSG(SUPL_ULP_NOTIFY_TYPE_TRC, "[SUPL][ULP]Notification type: %Msupl_mmi_notify_enum")
    TRC_MSG(SUPL_ULP_QOP_TRC, "[SUPL][ULP] QoP(HorAcc:%d, VerAcc:%d, Age:%d, Delay:%d)")
    TRC_MSG(SUPL_ULP_POS_METHOD_TRC, "[SUPL][ULP] Position Method: %Msupl_mmi_pos_method_enum")
    TRC_MSG(SUPL_ULP_FILTER_TRC, "[SUPL][ULP] Assist Data filter(alam:%d, utc:%d, iono:%d, dgps:%d, ref_loc:%d, ref_time:%d, acqu:%d rt_int:%d nav:%d)")
    TRC_MSG(SUPL_ULP_LID_TRC, "[SUPL][ULP] RAT: %Msupl_cell_info_type_enum, MCC:%d, MNC:%d, LAC:%d, CI:%d, cell_status:%Msupl_cell_status_enum")
    TRC_MSG(SUPL_ULP_VER_TRC, "[SUPL][ULP] VER:0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x")
    TRC_MSG(SUPL_ULP_POSITION_TRC, "[SUPL][ULP] Position(time_stamp:%d, week:%d (%d%d%d%d%d%d%dZ), latitude:%d, longtitude:%d, altitude:%d")
    TRC_MSG(SUPL_ULP_STATUS_TRC, "[SUPL][ULP] Status code: %Msupl_ulp_status_enum")
    TRC_MSG(SUPL_ULP_IPV4_TRC, "[SUPL][ULP] IPv4 Addr:%d.%d.%d.%d")
    TRC_MSG(SUPL_ULP_SLP_MODE_TRC, "[SUPL][ULP] SLP mode:%Msupl_mmi_slp_mode_enum")
//    TRC_MSG(SUPL_ULP_SUPL_END_POS_TRC, "[SUPL][ULP] Received SUPL END with Position")

    /* INET */
    TRC_MSG(SUPL_INET_ERROR_TRC, "[SUPL][INET] INET error: %Minet_result_enum")
    
    TRC_MSG(SUPL_IMSI_RECV_TRC, "Receive IMSI from SIM:0x%02X, 0x%02X, 0x%02X, 0x%02X, 0x%02X, 0x%02X, 0x%02X, 0x%02X, 0x%02X")
    TRC_MSG(SUPL_IMSI_SET_ID_TRC, "set_id:0x%02X, 0x%02X, 0x%02X, 0x%02X, 0x%02X, 0x%02X, 0x%02X, 0x%02X")

    /* L4C cell info */
#ifdef __SUPL_VER_1_5__
    TRC_MSG(SUPL_REMOVE_SAME_CELL_INFO, "[SUPL] Remove latest same cell => mcc: %d mnc: %d lac: %d ci: %d arfcn: %d bsic: %d")
    TRC_MSG(SUPL_REMOVE_OLDEST_CELL_INFO, "[SUPL] Remove oldest cell => mcc: %d mnc: %d lac: %d ci: %d arfcn: %d bsic: %d")
    TRC_MSG(SUPL_INSERT_CURRENT_REPORT_CELL_INFO, "[SUPL] Insert current cell => mcc: %d mnc: %d lac: %d ci: %d arfcn: %d bsic: %d")
    TRC_MSG(SUPL_DROP_ERROR_SERV_CELL_INFO, "[SUPL] Drop error serv cell info! mcc=%d, mnc=%d")
    TRC_MSG(SUPL_DROP_ERROR_NEIGHBOR_CELL_INFO, "[SUPL] Drop error neighbor cell [%d] info! mcc=%d, mnc=%d")
#endif

#ifdef __SUPL_VER_1_5__
    TRC_MSG(SUPL_ENTER_RR_CELL_INFO_IND_HDLR_SUBOP, "[SUPL] Enter supl_rr_cell_info_ind_hdlr()")
    TRC_MSG(SUPL_EXIT_RR_CELL_INFO_IND_HDLR_SUBOP, "[SUPL] Exit supl_rr_cell_info_ind_hdlr()") 
    TRC_MSG(SUPL_ENTER_GET_DATA_BUF_FROM_HEAD_SUBOP, "[SUPL] Enter supl_get_data_buf_from_head()")
    TRC_MSG(SUPL_EXIT_GET_DATA_BUF_FROM_HEAD_SUBOP, "[SUPL] Exit supl_get_data_buf_from_head()") 
    TRC_MSG(SUPL_ENTER_FREE_DATA_BUF_INSERT_TO_TAIL_SUBOP, "[SUPL] Enter supl_free_data_buf_insert_to_tail()")
    TRC_MSG(SUPL_EXIT_FREE_DATA_BUF_INSERT_TO_TAIL_SUBOP, "[SUPL] Exit supl_free_data_buf_insert_to_tail()")
#endif
    TRC_MSG(SUPL_L4_MSG_HDLR_FUNC_TRC, "[SUPL][L4] supl_l4_msg_hdlr()")
    TRC_MSG(SUPL_L4_NBR_CELL_INFO_REG_CNF, "[SUPL][L4] nbr_cell_info_reg_cnf(Valid: %d, RAT:%d)")
    TRC_MSG(SUPL_L4_NBR_CELL_INFO_IND, "[SUPL][L4] nbr_cell_info_ind_hdlr(Valid: %d, RAT:%d)")
END_TRACE_MAP(MOD_SUPL)

#endif /* _SUPL_TRC_H */


