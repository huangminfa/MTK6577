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

 /******************************************************************************
 * Filename:
 * ---------
 *   rrlp_trc.h
 *
 * Project:
 * -------- 
 *   MAUI
 *
 * Description:
 * ------------
 *
 * Author:
 * -------
 * Lanslo Yang (mtk01162)
 *
 *-----------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * Aug 10 2009 mtk01600
 * [MAUI_01936101] [AGPS 2G CP] AGPS Feature check-in
 * 
 *
 * Dec 26 2008 mtk01600
 * [MAUI_01305588] New Cell-ID arch check-in
 * 
 *
 * Apr 21 2008 mtk01162
 * [MAUI_00759867] [AGPRS] check-in AGPS RRLP part
 * 
 *
 * Feb 25 2008 mtk00563
 * [MAUI_00623349] [AGPS] AGPS feature check-in
 * 
 *
 *
 ******************************************************************************/

/********************************************************************************
*  Copyright Statement:
*  --------------------
*
*  This product has been developed using a protocol stack
*  developed by MediaTek Inc.
*
********************************************************************************/

#ifndef _RRLP_TRC_H
#define _RRLP_TRC_H

#ifndef __AGPS_SWIP_REL__
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

#endif /* __AGPS_SWIP_REL__ */

BEGIN_TRACE_MAP(MOD_RRLP)

   /* TRACE_STATE trace class */
   TRC_MSG(RRLP_SET_STATE, "[RRLP] Set state to %d")
   TRC_MSG(RRLP_STATE_PROC_STATE_CHANGE,"[Proc_State]from %Mrrlp_state_enum to %Mrrlp_state_enum")
   
   /* TRACE_FUNC trace class */

   TRC_MSG(RRLP_FN_ENTER_MAIN_SUBOP, "[RRLP] Enter rrlp_main()")
   TRC_MSG(RRLP_FN_EXIT_MAIN_SUBOP, "[RRLP] Exit rrlp_main()")
   TRC_MSG(RRLP_FN_ENTER_PROPRIETARY_CMD_SUBOP, "[RRLP] Enter rrlp_proprietary_cmd_hdlr()")
   TRC_MSG(RRLP_FN_EXIT_PROPRIETARY_CMD_SUBOP, "[RRLP] Exit rrlp_proprietary_cmd_hdlr()")   

   TRC_MSG(RRLP_FN_ENTER_SUPL_MSG_HDLR_SUBOP, "[RRLP] Enter rrlp_supl_msg_hdlr()")   
   TRC_MSG(RRLP_FN_EXIT_SUPL_MSG_HDLR_SUBOP, "[RRLP] Exit rrlp_supl_msg_hdlr()")   
   TRC_MSG(RRLP_FN_ENTER_START_REQ_HDLR_SUBOP, "[RRLP] Enter rrlp_start_req_hdlr()")   
   TRC_MSG(RRLP_FN_EXIT_START_REQ_HDLR_SUBOP, "[RRLP] Exit rrlp_start_req_hdlr()")   
   TRC_MSG(RRLP_FN_ENTER_END_REQ_HDLR_SUBOP, "[RRLP] Enter rrlp_end_req_hdlr()")   
   TRC_MSG(RRLP_FN_EXIT_END_REQ_HDLR_SUBOP, "[RRLP] Exit rrlp_end_req_hdlr()")   
   TRC_MSG(RRLP_FN_ENTER_LOC_ID_REQ_HDLR_SUBOP, "[RRLP] Enter rrlp_location_id_req_hdlr()")   
   TRC_MSG(RRLP_FN_EXIT_LOC_ID_REQ_HDLR_SUBOP, "[RRLP] Exit rrlp_location_id_req_hdlr()")   
   TRC_MSG(RRLP_FN_ENTER_MULTI_LOC_ID_REQ_HDLR_SUBOP, "[RRLP] Enter rrlp_multi_location_id_req_hdlr()")   
   TRC_MSG(RRLP_FN_EXIT_MULTI_LOC_ID_REQ_HDLR_SUBOP, "[RRLP] Exit rrlp_multi_location_id_req_hdlr()")   
   TRC_MSG(RRLP_FN_ENTER_GET_LCSP_VER_SUBOP, "[RRLP] Enter rrlp_get_lcsp_version()")   
   TRC_MSG(RRLP_FN_EXIT_GET_LCSP_VER_SUBOP, "[RRLP] Exit rrlp_get_lcsp_version()")   
   TRC_MSG(RRLP_FN_ENTER_LCSP_VER_REQ_HDLR_SUBOP, "[RRLP] Enter rrlp_supl_lcsp_vers_req_hdlr()")   
   TRC_MSG(RRLP_FN_EXIT_LCSP_VER_REQ_HDLR_SUBOP, "[RRLP] Exit rrlp_supl_lcsp_vers_req_hdlr()")   

   TRC_MSG(RRLP_FN_ENTER_SUPL_DATA_IND_HDLR_SUBOP, "[RRLP] Enter rrlp_supl_data_ind_hdlr()")   
   TRC_MSG(RRLP_FN_EXIT_SUPL_DATA_IND_HDLR_SUBOP, "[RRLP] Exit rrlp_supl_data_ind_hdlr()")   
   TRC_MSG(RRLP_FN_ENTER_SUPL_DATA_CNF_HDLR_SUBOP, "[RRLP] Enter rrlp_supl_data_cnf_hdlr()")   
   TRC_MSG(RRLP_FN_EXIT_SUPL_DATA_CNF_HDLR_SUBOP, "[RRLP] Exit rrlp_supl_data_cnf_hdlr()")   

   TRC_MSG(RRLP_FN_ENTER_SEND_SUPL_DATA_RSP_SUBOP, "[RRLP] Enter rrlp_send_supl_data_rsp()")   
   TRC_MSG(RRLP_FN_EXIT_SEND_SUPL_DATA_RSP_SUBOP, "[RRLP] Exit rrlp_send_supl_data_rsp()")   
   TRC_MSG(RRLP_FN_ENTER_SEND_SUPL_DATA_REQ_SUBOP, "[RRLP] Enter rrlp_send_supl_data_req()")   
   TRC_MSG(RRLP_FN_EXIT_SEND_SUPL_DATA_REQ_SUBOP, "[RRLP] Exit rrlp_send_supl_data_req()")   

   TRC_MSG(RRLP_FN_ENTER_ENCODE_PDU_SUBOP, "[RRLP] Enter rrlp_encode_pdu()")   
   TRC_MSG(RRLP_FN_EXIT_ENCODE_PDU_SUBOP, "[RRLP] Exit rrlp_encode_pdu()")   
   TRC_MSG(RRLP_FN_ENTER_PACK_AND_SEND_PE_SUBOP, "[RRLP] Enter rrlp_pack_and_send_protocol_err()")   
   TRC_MSG(RRLP_FN_EXIT_PACK_AND_SEND_PE_SUBOP, "[RRLP] Exit rrlp_pack_and_send_protocol_err()")   
   TRC_MSG(RRLP_FN_ENTER_PACK_AND_SEND_ASSIST_ACK_SUBOP, "[RRLP] Enter rrlp_pack_and_send_assist_ack()")   
   TRC_MSG(RRLP_FN_EXIT_PACK_AND_SEND_ASSIST_ACK_SUBOP, "[RRLP] Exit rrlp_pack_and_send_assist_ack()")   
   TRC_MSG(RRLP_FN_ENTER_PACK_AND_SEND_POS_RSP_SUBOP, "[RRLP] Enter rrlp_pack_and_send_pos_rsp()")   
   TRC_MSG(RRLP_FN_EXIT_PACK_AND_SEND_POS_RSP_SUBOP, "[RRLP] Exit rrlp_pack_and_send_pos_rsp()")   
   TRC_MSG(RRLP_FN_ENTER_PACK_AND_SEND_MEAS_RSP_SUBOP, "[RRLP] Enter rrlp_pack_and_send_meas_rsp()")   
   TRC_MSG(RRLP_FN_EXIT_PACK_AND_SEND_MEAS_RSP_SUBOP, "[RRLP] Exit rrlp_pack_and_send_meas_rsp()")      
   TRC_MSG(RRLP_FN_ENTER_PACK_AND_SEND_LOC_ERR_SUBOP, "[RRLP] Enter rrlp_pack_and_send_loc_err()")   
   TRC_MSG(RRLP_FN_EXIT_PACK_AND_SEND_LOC_ERR_SUBOP, "[RRLP] Exit rrlp_pack_and_send_loc_err()")   

   TRC_MSG(RRLP_FN_ENTER_GPS_MSG_HDLR_SUBOP, "[RRLP] Enter rrlp_gps_msg_hdlr()")   
   TRC_MSG(RRLP_FN_EXIT_GPS_MSG_HDLR_SUBOP, "[RRLP] Exit rrlp_gps_msg_hdlr()")   
   TRC_MSG(RRLP_FN_ENTER_GPS_POS_CNF_HDLR_SUBOP, "[RRLP] Enter rrlp_gps_pos_cnf_hdlr()")   
   TRC_MSG(RRLP_FN_EXIT_GPS_POS_CNF_HDLR_SUBOP, "[RRLP] Exit rrlp_gps_pos_cnf_hdlr()")   
   TRC_MSG(RRLP_FN_ENTER_GPS_MEAS_CNF_HDLR_SUBOP, "[RRLP] Enter rrlp_gps_meas_cnf_hdlr()")   
   TRC_MSG(RRLP_FN_EXIT_GPS_MEAS_CNF_HDLR_SUBOP, "[RRLP] Exit rrlp_gps_meas_cnf_hdlr()")   
   TRC_MSG(RRLP_FN_ENTER_GPS_ASSIST_DATA_CNF_HDLR_SUBOP, "[RRLP] Enter rrlp_gps_assist_data_cnf_hdlr()")   
   TRC_MSG(RRLP_FN_EXIT_GPS_ASSIST_DATA_CNF_HDLR_SUBOP, "[RRLP] Exit rrlp_gps_assist_data_cnf_hdlr()")   

   TRC_MSG(RRLP_FN_ENTER_SEND_GPS_ABORT_REQ_SUBOP, "[RRLP] Enter rrlp_send_gps_abort_req()")   
   TRC_MSG(RRLP_FN_EXIT_SEND_GPS_ABORT_REQ_SUBOP, "[RRLP] Exit rrlp_send_gps_abort_req()")   
   TRC_MSG(RRLP_FN_ENTER_SEND_GPS_POS_REQ_SUBOP, "[RRLP] Enter rrlp_send_gps_pos_req()")   
   TRC_MSG(RRLP_FN_EXIT_SEND_GPS_POS_REQ_SUBOP, "[RRLP] Exit rrlp_send_gps_pos_req()")   
   TRC_MSG(RRLP_FN_ENTER_SEND_GPS_MEAS_REQ_SUBOP, "[RRLP] Enter rrlp_send_gps_meas_req()")   
   TRC_MSG(RRLP_FN_EXIT_SEND_GPS_MEAS_REQ_SUBOP, "[RRLP] Exit rrlp_send_gps_meas_req()")   
   TRC_MSG(RRLP_FN_ENTER_SEND_GPS_ASSIST_DATA_REQ_SUBOP, "[RRLP] Enter rrlp_send_gps_assist_data_req()")   
   TRC_MSG(RRLP_FN_EXIT_SEND_GPS_ASSIST_DATA_REQ_SUBOP, "[RRLP] Exit rrlp_send_gps_assist_data_req()")   

   TRC_MSG(RRLP_FN_ENTER_RR_MSG_HDLR_SUBOP, "[RRLP] Enter rrlp_rr_msg_hdlr()")   
   TRC_MSG(RRLP_FN_EXIT_RR_MSG_HDLR_SUBOP, "[RRLP] Exit rrlp_rr_msg_hdlr()")   
   TRC_MSG(RRLP_FN_ENTER_GET_DATA_BUF_FROM_HEAD_SUBOP, "[RRLP] Enter rrlp_get_data_buf_from_head()")   
   TRC_MSG(RRLP_FN_EXIT_GET_DATA_BUF_FROM_HEAD_SUBOP, "[RRLP] Exit rrlp_get_data_buf_from_head()")   
   TRC_MSG(RRLP_FN_ENTER_FREE_DATA_BUF_INSERT_TO_TAIL_SUBOP, "[RRLP] Enter rrlp_free_data_buf_insert_to_tail()")   
   TRC_MSG(RRLP_FN_EXIT_FREE_DATA_BUF_INSERT_TO_TAIL_SUBOP, "[RRLP] Exit rrlp_free_data_buf_insert_to_tail()")   
  
   TRC_MSG(RRLP_FN_ENTER_MEAS_POS_REQ_HDLR_SUBOP, "[RRLP] Enter rrlp_meas_pos_req_hdlr()")
   TRC_MSG(RRLP_FN_EXIT_MEAS_POS_REQ_HDLR_SUBOP, "[RRLP] Exit rrlp_meas_pos_req_hdlr()")
   TRC_MSG(RRLP_FN_ENTER_ASSIST_DATA_HDLR_SUBOP, "[RRLP] Enter rrlp_assistance_data_hdlr()")
   TRC_MSG(RRLP_FN_EXIT_ASSIST_DATA_HDLR_SUBOP, "[RRLP] Exit rrlp_assistance_data_hdlr()")

   TRC_MSG(RRLP_FN_ENTER_FILL_LOCATION_ID_SUBOP, "[RRLP] Enter rrlp_fill_location_id()")
   TRC_MSG(RRLP_FN_EXIT_FILL_LOCATION_ID_SUBOP, "[RRLP] Exit rrlp_fill_location_id()")

   TRC_MSG(RRLP_FN_ENTER_SEND_ALL_ASSIST_DATAS_SUBOP, "[RRLP] Enter rrlp_send_all_assist_datas()")
   TRC_MSG(RRLP_FN_EXIT_SEND_ALL_ASSIST_DATAS_SUBOP, "[RRLP] Exit rrlp_send_all_assist_datas()")   

    TRC_MSG(RRLP_FN_ENTER_GAS_MSG_HDLR_SUBOP,"[RRLP] Enter rrlp_gas_msg_hdlr() <<Bear:%Mrrlp_bearer_type_enum, Proc State: %Mrrlp_state_enum, RATCM State: %Mrrlp_cp_state_enum>>")
    TRC_MSG(RRLP_FN_EXIT_GAS_MSG_HDLR_SUBOP,"[RRLP] Exit rrlp_gas_msg_hdlr() <<Bear:%Mrrlp_bearer_type_enum, Proc State: %Mrrlp_state_enum, RATCM State: %Mrrlp_cp_state_enum>>")

    TRC_MSG(RRLP_FN_ENTER_ENCODE_AND_SEND_PDU_SUBOP,"Enter rrlp_encode_and_send_pdu()")
    TRC_MSG(RRLP_FN_EXIT_ENCODE_AND_SEND_PDU_SUBOP,"Exit rrlp_encode_and_send_pdu()")

    /* TRACE_INFO trace class */
    TRC_MSG(RRLP_INFO_DOWNLINK_DATA_HDLR,"rrlp_downlink_data_hdlr: pdu length=%d")
    TRC_MSG(RRLP_INFO_ENCODE_PDU_SIZE,"[Encode]PDU size=%d, payload Type=%Mrrlp_cp_ev_enum [Ln:%d]")
#ifdef __AGPS_CONTROL_PLANE__
    TRC_MSG(RRLP_INFO_RATCM_FSM_TRANSITION,"[FSM]from %Mrrlp_cp_state_enum to %Mrrlp_cp_state_enum, EV: %Mrrlp_cp_ev_enum [Ln:%d]")
    TRC_MSG(RRLP_INFO_FSM_CP_ACT_RR_TX_RECV_DATA_CNF,"Bearer: %Mrrlp_bearer_type_enum, Proc State: %Mrrlp_state_enum, CNF Wait: %d")
    TRC_MSG(RRLP_INFO_RESET_CTRL_PLANE_VAIABLES,"-----> Reset C-Plane Variables <-----")
    TRC_MSG(RRLP_INFO_GAS_ABORT_IND_PROC_STATE_RATCM_STATE,"gas_abort_ind_hdlr, Proc_State: %Mrrlp_state_enum, RATCM_State: %Mrrlp_cp_state_enum")
    TRC_MSG(RRLP_INFO_GAS_RRLP_DATA_CNF_HDLR,"gas_rrlp_data_cnf_hdlr: num_data_req_sent=%d, mui_waited=%d, mui=%d")
    TRC_MSG(RRLP_INFO_SEND_GAS_DATA_REQ,"rrlp_send_gas_data_req: num_data_req_sent=%d, mui_waited=%d, mui=%d")
    TRC_MSG(RRLP_INFO_IGNORE_SUPL_DATA_IND,"rrlp_supl_data_ind_hdlr: state = %Mrrlp_state_enum, CP state = %Mrrlp_cp_state_enum")       
    TRC_MSG(RRLP_WARNING_ABORT_AGPS_USER_PLANE,"[Warning] Abort A-GPS User-Plane!<<Proc State: %Mrrlp_state_enum>>")
    TRC_MSG(RRLP_WARNING_IGNORE_RRLP_MSG,"[Warning] Ingore RRLP MSG!<<Proc State: %Mrrlp_state_enum, RATCM state: %Mrrlp_cp_state_enum>>")
    TRC_MSG(RRLP_WARNING_MSG_IS_DISCARD,"[Warning] RRLP PDU is incomplete!<<Proc State: %Mrrlp_state_enum, RATCM state: %Mrrlp_cp_state_enum>>")
    TRC_MSG(RRLP_WARNING_GAS_RRLP_DATA_CNF_HDLR,"[Warning][mui mismatch] gas_rrlp_data_cnf_hdlr: num_data_req_sent=%d, mui_waited=%d, mui=%d")
#endif    

   /* TRACE_WARNING trace class */
   TRC_MSG(RRLP_WARNING_MEAS_TIMER_EXPIRED, "[RRLP] Meas timer expires at state %d")
   TRC_MSG(RRLP_WARNING_POS_TIMER_EXPIRED, "[RRLP] Pos timer expires at state %d")
   TRC_MSG(RRLP_WARNING_DROP_OTHER_ASSIST_DATA_TYPE, "[RRLP] Drop other assist data type %x at state %d")
   TRC_MSG(RRLP_WARNING_DROP_POS_CNF_STATE_MISMATCH, "[RRLP] Drop pos cnf bc state mismatch at state %d")
   TRC_MSG(RRLP_WARNING_DROP_POS_CNF_TRANS_MISMATCH, "[RRLP] Drop pos cnf bc trans id mismatch: req %d != cnf %d at state %d")
   TRC_MSG(RRLP_WARNING_DROP_MEAS_CNF_STATE_MISMATCH, "[RRLP] Drop meas cnf bc state mismatch at state %d")
   TRC_MSG(RRLP_WARNING_DROP_MEAS_CNF_TRANS_MISMATCH, "[RRLP] Drop meas cnf bc trans id mismatch: req %d != cnf %d at state %d")
   TRC_MSG(RRLP_WARNING_DROP_ASSIST_CNF_TRANS_MISMATCH, "[RRLP] Drop assist data cnf bc trans id mismatch: req %d != cnf %d at state %d")
   TRC_MSG(RRLP_WARNING_DROP_SUPL_DATA_IND_REF_NUMBER_SAME, "[RRLP] Drop supl data ind bc ref num same %d state %d")
   TRC_MSG(RRLP_WARNING_DROP_ASSIST_CNF_DATA_ERR, "[RRLP] Assist data error: tras id %d, type %d, state %d")
   TRC_MSG(RRLP_WARNING_IGNORE_NEW_MPR, "Warning! Ingore MeasurePositionRequest! (LN:%d)")
   TRC_MSG(RRLP_WARNING_IGNORE_SUPL_START_REQ,"Ignore LCSP_START_REQ [Bearer: %Mrrlp_bearer_type_enum]") 

   /* TRACE_ERROR trace class */
   TRC_MSG(RRLP_ERROR_ASN_DECODE_ERR, "[RRLP] ASN decode err: cause %d, asn err %d !")
   TRC_MSG(RRLP_ERROR_ASN_ENCODE_ERR_PE, "[RRLP] ASN encode err (%d) for Protocol Error!")   
   TRC_MSG(RRLP_ERROR_ASN_ENCODE_ERR_ASSIST_ACK, "[RRLP] ASN encode err (%d) for Assist Data Ack!")   
   TRC_MSG(RRLP_ERROR_ASN_ENCODE_ERR_POS_RSP, "[RRLP] ASN encode err (%d) for Measure Position Response (POS RSP)!")   
   TRC_MSG(RRLP_ERROR_ASN_ENCODE_ERR_MEAS_RSP, "[RRLP] ASN encode err (%d) for Measure Position Response (MEAS RSP)!")   
   TRC_MSG(RRLP_ERROR_ASN_ENCODE_ERR_LOC_ERR, "[RRLP] ASN encode err (%d) for Measure Position Response (LOC ERR)!")   

   TRC_MSG(RRLP_ERROR_IMPROPER_SUPL_START,"[RRLP] Improper SUPL Start, state = %Mrrlp_state_enum")
   TRC_MSG(RRLP_ERROR_IMPROPER_SUPL_END, "[RRLP] Improper SUPL END comes at RRLP idle state!")

   TRC_MSG(RRLP_ERROR_ASSIST_CNF_BITMAP_MISMATCH, "[RRLP] Assist bitmap mismatch trans id %d (cnf 0x%x, but rcv 0x%x) at state %d!")

   /* TRACE_PEER */
   TRC_MSG(RRLP_RECEV_PEER_MSG_TRACE,"[NW->MS] %Mrrlp_peer_msg_name_enum")
   TRC_MSG(RRLP_SEND_PEER_MSG_TRACE,  "[MS->NW] %Mrrlp_peer_msg_name_enum")
   TRC_MSG(RRLP_RECEV_PEER_MSG_WITH_PARAMS_TRACE,  "[NW->MS] %Mrrlp_peer_msg_name_enum : no = %d, req time = %d, bitmap = 0x%x")   
   TRC_MSG(RRLP_SEND_PEER_MSG_WITH_PARAMS_TRACE,  "[MS->NW] %Mrrlp_peer_msg_name_enum : no = %d, time meas/err = %d, bitmap/tow valid = 0x%x)")

   /* TRACE_GROUP1 */   
   TRC_MSG(RRLP_GROUP_1_ASSIST_BITMAP_MATCH, "[RRLP] Assist bitmap matched (0x%x) trans id %d at state %d")
   TRC_MSG(RRLP_GROUP_1_GPS_TASK_REQ_BITMAP_IN_LOC_ERR, "[RRLP] GPS task req bitmap in loc err")

   TRC_MSG(RRLP_GROUP_1_MA_REQ_ACC_VALID, "[RRLP] MA req accuracy valid = %d, accuracy = %d")
   TRC_MSG(RRLP_GROUP_1_MA_PREF_REQ_ACC_VALID, "[RRLP] MA pref req accuracy valid = %d, accuracy = %d")
   
   /* TRACE_GROUP2 */   
   TRC_MSG(RRLP_GROUP_2_ENCODE_PDU_SIZE, "[RRLP] Encode RRLP PDU size = %d")
   TRC_MSG(RRLP_GROUP_2_SEND_PDU_PAYLOAD_LEN, "[RRLP] Send PDU payload = %d with len = %d")

   /* TRACE_GROUP3 */
   TRC_MSG(RRLP_GROUP_3_MPR_R5_EXT_REF,"[R5][MPR] EXT_REF_IE: smlc = %d, TX id = %d")
   TRC_MSG(RRLP_GROUP_3_ASSIST_R5_EXT_REF,"[R5][ASSIST] EXT_REF_IE: smlc = %d, TX id = %d")

   /* TRACE_GROUP7 */   

   /* TRACE_GROUP8 */   
   TRC_MSG(RRLP_GROUP_8_ALLOC_ADM, "[RRLP] Allocate ADM ptr = 0x%x, size = %d")
   TRC_MSG(RRLP_GROUP_8_FREE_ADM, "[RRLP] Free ADM ptr = 0x%x")
   TRC_MSG(RRLP_GROUP_8_FREE_CTRL_BUF, "[RRLP] Free ctrl buf ptr = 0x%x")
   TRC_MSG(RRLP_GROUP_8_ALLOCATE_CTRL_BUF, "[RRLP] Allocate ctrl buf ptr = 0x%x")   

   /* TRACE_GROUP9 : Timer */   
   TRC_MSG(RRLP_GROUP_9_BASE_TIMER_INIT,"[Timer] Base timer/event scheduler init")

   /* Event scheduler trace */
   TRC_MSG(RRLP_GROUP_9_START_TIMER,"[EVS] Start Timer <<Timer ID: %d, Period: %d>>")
   TRC_MSG(RRLP_GROUP_9_STOP_TIMER,"[EVS] Stop Timer <<Timer ID: %d>>")
   TRC_MSG(RRLP_GROUP_9_PROLONG_MODE_TIMER_TIMEOUT_HDLR,"[EVS] PROLONG_MODE_TIMER Timeout <<CP_State: %Mrrlp_cp_state_enum>>")
   TRC_MSG(RRLP_GROUP_9_SLEEP_MODE_TIMER_TIMEOUT_HDLR,"[EVS] SLEEP_MODE_TIMER Timeout <<CP_State: %Mrrlp_cp_state_enum>>")

END_TRACE_MAP(MOD_RRLP)

#endif /* _RRLP_TRC_H */
