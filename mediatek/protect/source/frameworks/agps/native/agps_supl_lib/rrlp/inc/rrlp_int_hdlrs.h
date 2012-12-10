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
 *   rrlp_int_hdlrs.h
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
 * May 9 2008 mtk01162
 * [MAUI_00765688] [Build Warning] 08A.W08.18 build warning for TIANYU30_GPS_DEMO (Low Priority Bug)
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


#ifndef _RRLP_INT_HDLRS_H
#define _RRLP_INT_HDLRS_H

/* asn */
#include "applib_asn_common.h"
#include "applib_asn_uper_common.h"
#include "applib_asn_memory.h"

#ifdef __RRLP_REL_99__
#include "rrlp_r99_asn.h"
#endif
#ifdef __RRLP_REL_5__
#include "rrlp_r5_asn.h"
#endif
//$$
//#include "rrlp_asn.h"

/* interface to GPS */
#include "gps2lcsp_enum.h"
#include "gps2lcsp_struct.h"

/* interface to SUPL */
#include "supl2lcsp_enums.h"
#include "supl2lcsp_struct.h"
#include "lcsp2app_enums.h"
#include "lcsp2app_struct.h"

/*****************************************************************************
 * Common function prototypes for both C-Plane and U-Plane
 *****************************************************************************/
extern void rrlp_send_msg(module_type dest_mod_id,
                   kal_uint16 msg_id,
                   sap_type sap_id,
                   local_para_struct *local_para_ptr,
                   peer_buff_struct *peer_buff_ptr);

void rrlp_change_session_state(
    IN rrlp_session_context_struct* rrlp_sctx_ptr,
    IN rrlp_state_enum new_state,
    IN kal_bool abort_meas_pos_flag);

/* Downlink RRLP data processing function */
void rrlp_downlink_data_hdlr(
        IN rrlp_session_context_struct* rrlp_sctx_ptr,
        IN kal_uint8    *enc_data_ptr,
        IN kal_uint32   enc_length);

void rrlp_meas_pos_req_hdlr(
    IN rrlp_session_context_struct* rrlp_sctx_ptr,
    IN kal_uint8 ref_num,
    IN RRLP_MsrPosition_Req *MsrPos_req);

void rrlp_assistance_data_hdlr(
    IN rrlp_session_context_struct* rrlp_sctx_ptr,    
    IN kal_uint8 ref_num,
    IN RRLP_AssistanceData *AssistData);

/* Uplink RRLP data processing function */
kal_int32 rrlp_send_uplink_data(
    IN rrlp_session_context_struct* rrlp_sctx_ptr,
    IN rrlp_payload_enum payload_type, 
    IN kal_uint8 *pdu_ptr, 
    IN kal_uint16 pdu_len,
    IN kal_bool more);

kal_bool rrlp_pack_and_send_loc_err(
    IN rrlp_session_context_struct* rrlp_sctx_ptr,
    IN gps_error_code_enum error_code, 
    IN kal_bool bitmap_valid,
    IN kal_uint16 bitmap,
    IN gps_sat_related_data_struct *pSatData);

kal_bool rrlp_pack_and_send_protocol_err(
    IN rrlp_session_context_struct* rrlp_sctx_ptr,
    rrlp_msg_err_cause_enum err_cause);

kal_bool rrlp_pack_and_send_assist_ack(
    IN rrlp_session_context_struct* rrlp_sctx_ptr);

kal_bool rrlp_pack_and_send_pos_rsp(
    IN rrlp_session_context_struct* rrlp_sctx_ptr,
    IN kal_bool time_assist_meas_valid,
    IN gps_time_assist_meas_struct *time_assist_meas,
    IN gps_pos_result_struct *pPos_result);

kal_bool rrlp_pack_and_send_meas_rsp(
    IN rrlp_session_context_struct* rrlp_sctx_ptr,
    IN kal_bool time_assist_meas_valid,
    IN gps_time_assist_meas_struct *time_assist_meas,
    IN gps_meas_result_struct *pMeas_result);


/* Internal layer processing function */
void rrlp_send_data_rsp(
    IN rrlp_session_context_struct* rrlp_sctx_ptr,
    IN kal_uint32       asn_err, 
    IN RRLP_PDU     *dec_rrlp_pdu_ptr);

void rrlp_send_all_assist_datas(
    IN rrlp_session_context_struct *rrlp_sctx_ptr,
    IN kal_bool req_time_assist_meas, 
    IN RRLP_ControlHeader *pAssistData,
    IN kal_bool fgLastAssist);  //!!Chiwei: fgLastAssist is added in porting


kal_uint32 rrlp_encode_and_send_pdu(RRLP_PDU *pRrlpPdu, 
                           kal_uint8 **ppEncOut, 
                           kal_uint32 *pEncByteLen,
                           kal_bool     bUsePseudoSeg,
                           rrlp_payload_enum payload_type,
                           rrlp_session_context_struct *rrlp_sctx_ptr);

#endif /* _RRLP_INT_HDLRS_H */
