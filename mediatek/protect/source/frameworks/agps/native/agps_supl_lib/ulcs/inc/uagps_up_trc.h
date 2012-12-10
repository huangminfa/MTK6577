/*******************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2007
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
 *	uagps_up_trc.h  !!! should be modified
 *
 * Project:
 * --------
 *   AGPS
 *
 * Description:
 * ------------
 *   This file is intends for trace messages.
 *
 * Author:
 * -------
 *	David Niu
 *
 *==============================================================================
 * 				HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *===========================================================================
 * $Log:$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * 07 30 2010 david.niu
 * [MAUI_02600931] [3G AGPS CP] AGPS related code checked in MT6276_DVT and MAUI
 * AGPS check-in for MT6276_DVT
 *
 * May 15 2010 mtk02119
 * [MAUI_02525171] [3G AGPS CP] ULCS code check-in for modification about Measurement Control Failure m
 * 
 *
 * May 3 2010 mtk02119
 * [MAUI_02412594] [3G AGPS CP] ULCS code check-in for new CP implementation and UP modification
 * 
 *
 * Sep 4 2009 mtk02119
 * [MAUI_01936271] [3G AGPS][User Plane] Check-in 3G AGPS UP related files into MAUI on W09.37
 * add to source control recursely
 *
 * Aug 3 2009 mtk02119
 * [MAUI_01932044] [MT6268_AGPS_Dev] Add GPS TOW Assist List and modify delay unit in QoP to ms in UAGP
 * 
 *
 * Jun 29 2009 mtk02119
 * [MAUI_01711600] [MT6268_AGPS_Dev] Add UAGPS_UP module related code
 * add to source control recursely
 *
 *===========================================================================
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *==============================================================================
 *******************************************************************************/
/*!!! should be modified */
#ifndef _UAGPS_UP_TRC_H
#define _UAGPS_UP_TRC_H

#ifdef GEN_FOR_PC
#include "kal_trace.h"
#endif /* GEN_FOR_PC */

/* !!!Note: Please define the group ID to TRACE_GROUP_1 ... or find and replace with correct one*/

#if 1
#define UAGPS_UP_SUPL_START_REQ_HDLER "[UAGPS_UP]: Uagps_up_suplStartReqHandler()"
#define UAGPS_UP_SUPL_DATA_IND_HDLER "[UAGPS_UP]: Uagps_up_suplDataIndHandler()"
#define UAGPS_UP_SUPL_DATA_CNF_HDLER "[UAGPS_UP]: Uagps_up_suplDataCnfHandler()"
#define UAGPS_UP_SUPL_END_REQ_HDLER "[UAGPS_UP]: Uagps_up_suplEndReqHandler()"
#define UAGPS_UP_GPS_POS_GAD_CNF_HDLER "[UAGPS_UP]: Uagps_up_gpsPosGadCnfHandler()"
#define UAGPS_UP_GPS_MEAS_GAD_CNF_HDLER "[UAGPS_UP]: Uagps_up_gpsMeasGadCnfHandler()"
#define UAGPS_UP_GPS_ASSISTANCE_DATA_CNF_HDLER "[UAGPS_UP]: Uagps_up_gpsAssistDataCnfHandler()"
#define UAGPS_UP_TST_INJECT_HDLER "[UAGPS_UP]: Uagps_up_tstInjectHandler()"
#define UAGPS_UP_INIT "[UAGPS_UP]: uagps_up_init()"
#define UAGPS_UP_RESET "[UAGPS_UP]: uagps_up_reset()"
#define UAGPS_UP_SUPL_DATA_IND_PDU_DECODED_LENGTH "[UAGPS_UP]: Decoded pdu length in SUPL DATA IND: %d bytes"
#define UAGPS_UP_SUPL_DATA_IND_PDU_ASN_DECODE_RESULT "[UAGPS_UP]: Decode result for pdu in SUPL DATA IND: %d"
#define UAGPS_UP_MEASUREMENT_CONTROL_MSG_RECEIVED "[UAGPS_UP]: Measurement control message in SUPL POS"
#define UAGPS_UP_ASSISTANCE_DATA_DELIVERY_MSG_RECEIVED "[UAGPS_UP]: Assistance data delivery message in SUPL POS"
#define UAGPS_UP_ABORT_GPS_FOR_SUPL_END_REQ "[UAGPS_UP]: Abort GPS transaction id: [%d] for SUPL END REQ"
#define UAGPS_UP_GPS_POS_GAD_CNF_TRANSACTION_ID_MATCHED "[UAGPS_UP]: Transaction id: [%d] matched in pos gad cnf"
#define UAGPS_UP_GPS_POS_GAD_CNF_ERROR_CODE "[UAGPS_UP]: Error code in pos gad cnf: [%Mgps_error_code_enum]"
#define UAGPS_UP_GPS_POS_GAD_CNF_ASSIST_DATA_MISS_WITH_BITMAP "[UAGPS_UP]: Error code in pos gad cnf is assistance data miss and bitmap is existed"
#define UAGPS_UP_ADDITIONAL_DATA_REQ_PERMITTED_STATUS "[UAGPS_UP]: Assistance data request permitted status in context: %X"
#define UAGPS_UP_GPS_MEAS_GAD_CNF_TRANSACTION_ID_MATCHED "[UAGPS_UP]: Transaction id: [%d] matched in meas gad cnf"
#define UAGPS_UP_GPS_MEAS_GAD_CNF_ERROR_CODE "[UAGPS_UP]: Error code in meas gad cnf: [%Mgps_error_code_enum]"
#define UAGPS_UP_GPS_MEAS_GAD_CNF_ASSIST_DATA_MISS_WITH_BITMAP "[UAGPS_UP]: Error code in meas gad cnf is assistance data miss and bitmap is existed"
#define UAGPS_UP_SEND_SUPL_DATA_RSP "[UAGPS_UP]: Uagps_up_sendSuplDataRsp() with result: %Mkal_bool"
#define UAGPS_UP_SEND_SUPL_DATA_REQ "[UAGPS_UP]: Uagps_up_sendSuplDataReq() with pduLen: %d"
#define UAGPS_UP_SEND_RRC_STATUS_MSG "[UAGPS_UP]: Uagps_up_sendRrcStatusMsg()"
#define UAGPS_UP_UL_DCCH_MSG_ASN_ENCODE_RESULT "[UAGPS_UP]: Encode result for UL DCCH msg: %d"
#define UAGPS_UP_ABORT_PREVIOUS_GPS_REQ_WITH_SAME_MEASUREMENT_ID "[UAGPS_UP]: Abort previous GPS request for the same measurement id setup command is received"
#define UAGPS_UP_ABORT_PREVIOUS_GPS_REQ_BY_RELEASE_COMMAND_WITH_SAME_MEASUREMENT_ID "[UAGPS_UP]: Abort previous GPS request for the same measurement id release command is received"
#define UAGPS_UP_CURRENT_MEASUREMENT_ID_AFTER_NEW_REQ "[UAGPS_UP]: Current uagps_up.currentMeasurementIds: %X"
#define UAGPS_UP_CURRENT_MEASUREMENT_CONTROL_TRANSACTIONS_AFTER_NEW_REQ "[UAGPS_UP]: Current uagps_up.currentMeasurementControlTransactions: %X"
#define UAGPS_UP_CURRENT_ADDITIONAL_ASSISTANCE_DATA_REQ_AFTER_NEW_REQ "[UAGPS_UP]: Current uagps_up.additionalAssistanceDataReq: %X"
#define UAGPS_UP_CURRENT_TRANSACTION_ID_FOR_ASSISTANCE_DATA_DELIVERY "[UAGPS_UP]: Current transaction id for ASSISTANCE DATA DELIVERY message: %d"
#define UAGPS_UP_CALL_BACK_RECORD_MEASUREMENT_ACTION "[UAGPS_UP]: Uagps_up_callbackRecordMeasurementAction()"
#define UAGPS_UP_CALL_BACK_ASN_ENCODE_SUPL_DATA_REQ "[UAGPS_UP]: Uagps_up_callbackAsnEncodeSuplDataReq()"
#define UAGPS_UP_CALL_BACK_ADDITIONAL_DATA_REQ_HANDLING "[UAGPS_UP]: Uagps_up_callbackAdditionalDataReqHandling()"
#define UAGPS_UP_CALL_BACK_CLEAR_TRANSACTION "[UAGPS_UP]: Uagps_up_callbackClearTransaction()"
#define UAGPS_UP_CALL_BACK_SET_TRANSACTION "[UAGPS_UP]: Uagps_up_callbackSetTransaction()"
#define UAGPS_UP_CALL_BACK_CLEAR_PREVIOUS_TRANSACTION "[UAGPS_UP]: Uagps_up_callbackClearPreviousTransaction()"
#define UAGPS_UP_CALL_BACK_SEND_SUPL_DATA_RSP "[UAGPS_UP]: Uagps_up_callbacksendSuplDataRsp()"
#define UAGPS_UP_CALL_BACK_POSITIONING_METHOD_NOT_SUPPORT "[UAGPS_UP]: Uagps_up_callbackPositioningMethodNotSupport()"
#define UAGPS_UP_CALL_BACK_REL_MEASUREMENT "[UAGPS_UP]: Uagps_up_callbackRelMeasurement()"
#define UAGPS_UP_CALL_BACK_MODIFY_WITHOUT_SETUP "[UAGPS_UP]: Uagps_up_callbackModifyWithoutSetup()"
#define UAGPS_UP_CALL_BACK_MEASUREMENT_TYPE_NOT_POSITIONING "[UAGPS_UP]: Uagps_up_callbackMeasurementTypeNotPositioning()"
#define UAGPS_UP_CALL_BACK_UPDATE_TRANSACTION_FOR_ASSISTANCE_DATA_DELIVERY "[UAGPS_UP]: Uagps_up_callbackUpdateTransactionForAssistanceDataDelivery()"
#define UAGPS_UP_CALL_BACK_REPORTING_PERIOD_HANDLING "[UAGPS_UP]: Uagps_up_callbackReportingPeriodHandling()"
#define UAGPS_UP_CALL_BACK_GET_ASSISTANCE_DATA_DELIVERY_TRANSACTION_ID "[UAGPS_UP]: Uagps_up_callbackGetAssistanceDataDeliveryTransactionId()"

#else
/* !!! should be modified */
BEGIN_TRACE_MAP(MOD_UAGPS_UP)

/* TRACE_INFO */

/* TRACE_WARNING */

/* TRACE_GROUP_1 */
TRC_MSG(UAGPS_UP_SUPL_START_REQ_HDLER, "[UAGPS_UP]: Uagps_up_suplStartReqHandler()")

TRC_MSG(UAGPS_UP_SUPL_DATA_IND_HDLER, "[UAGPS_UP]: Uagps_up_suplDataIndHandler()")

TRC_MSG(UAGPS_UP_SUPL_DATA_CNF_HDLER, "[UAGPS_UP]: Uagps_up_suplDataCnfHandler()")

TRC_MSG(UAGPS_UP_SUPL_END_REQ_HDLER, "[UAGPS_UP]: Uagps_up_suplEndReqHandler()")

TRC_MSG(UAGPS_UP_GPS_POS_GAD_CNF_HDLER, "[UAGPS_UP]: Uagps_up_gpsPosGadCnfHandler()")

TRC_MSG(UAGPS_UP_GPS_MEAS_GAD_CNF_HDLER, "[UAGPS_UP]: Uagps_up_gpsMeasGadCnfHandler()")

TRC_MSG(UAGPS_UP_GPS_ASSISTANCE_DATA_CNF_HDLER, "[UAGPS_UP]: Uagps_up_gpsAssistDataCnfHandler()")

TRC_MSG(UAGPS_UP_TST_INJECT_HDLER, "[UAGPS_UP]: Uagps_up_tstInjectHandler()")

TRC_MSG(UAGPS_UP_INIT, "[UAGPS_UP]: uagps_up_init()")

TRC_MSG(UAGPS_UP_RESET, "[UAGPS_UP]: uagps_up_reset()")

TRC_MSG(UAGPS_UP_SUPL_DATA_IND_PDU_DECODED_LENGTH, "[UAGPS_UP]: Decoded pdu length in SUPL DATA IND: %d bytes")

TRC_MSG(UAGPS_UP_SUPL_DATA_IND_PDU_ASN_DECODE_RESULT, "[UAGPS_UP]: Decode result for pdu in SUPL DATA IND: %d")

TRC_MSG(UAGPS_UP_MEASUREMENT_CONTROL_MSG_RECEIVED, "[UAGPS_UP]: Measurement control message in SUPL POS")

TRC_MSG(UAGPS_UP_ASSISTANCE_DATA_DELIVERY_MSG_RECEIVED, "[UAGPS_UP]: Assistance data delivery message in SUPL POS")

TRC_MSG(UAGPS_UP_ABORT_GPS_FOR_SUPL_END_REQ, "[UAGPS_UP]: Abort GPS transaction id: [%d] for SUPL END REQ")

TRC_MSG(UAGPS_UP_GPS_POS_GAD_CNF_TRANSACTION_ID_MATCHED, "[UAGPS_UP]: Transaction id: [%d] matched in pos gad cnf")

TRC_MSG(UAGPS_UP_GPS_POS_GAD_CNF_ERROR_CODE, "[UAGPS_UP]: Error code in pos gad cnf: [%Mgps_error_code_enum]")

TRC_MSG(UAGPS_UP_GPS_POS_GAD_CNF_ASSIST_DATA_MISS_WITH_BITMAP, "[UAGPS_UP]: Error code in pos gad cnf is assistance data miss and bitmap is existed")

TRC_MSG(UAGPS_UP_ADDITIONAL_DATA_REQ_PERMITTED_STATUS, "[UAGPS_UP]: Assistance data request permitted status in context: %X")

TRC_MSG(UAGPS_UP_GPS_MEAS_GAD_CNF_TRANSACTION_ID_MATCHED, "[UAGPS_UP]: Transaction id: [%d] matched in meas gad cnf")

TRC_MSG(UAGPS_UP_GPS_MEAS_GAD_CNF_ERROR_CODE, "[UAGPS_UP]: Error code in meas gad cnf: [%Mgps_error_code_enum]")

TRC_MSG(UAGPS_UP_GPS_MEAS_GAD_CNF_ASSIST_DATA_MISS_WITH_BITMAP, "[UAGPS_UP]: Error code in meas gad cnf is assistance data miss and bitmap is existed")

TRC_MSG(UAGPS_UP_SEND_SUPL_DATA_RSP, "[UAGPS_UP]: Uagps_up_sendSuplDataRsp() with result: %Mkal_bool")

TRC_MSG(UAGPS_UP_SEND_SUPL_DATA_REQ, "[UAGPS_UP]: Uagps_up_sendSuplDataReq() with pduLen: %d")

TRC_MSG(UAGPS_UP_SEND_RRC_STATUS_MSG, "[UAGPS_UP]: Uagps_up_sendRrcStatusMsg()")

TRC_MSG(UAGPS_UP_UL_DCCH_MSG_ASN_ENCODE_RESULT, "[UAGPS_UP]: Encode result for UL DCCH msg: %d")

TRC_MSG(UAGPS_UP_ABORT_PREVIOUS_GPS_REQ_WITH_SAME_MEASUREMENT_ID, "[UAGPS_UP]: Abort previous GPS request for the same measurement id setup command is received")

TRC_MSG(UAGPS_UP_ABORT_PREVIOUS_GPS_REQ_BY_RELEASE_COMMAND_WITH_SAME_MEASUREMENT_ID, "[UAGPS_UP]: Abort previous GPS request for the same measurement id release command is received")

TRC_MSG(UAGPS_UP_CURRENT_MEASUREMENT_ID_AFTER_NEW_REQ, "[UAGPS_UP]: Current uagps_up.currentMeasurementIds: %X")

TRC_MSG(UAGPS_UP_CURRENT_MEASUREMENT_CONTROL_TRANSACTIONS_AFTER_NEW_REQ, "[UAGPS_UP]: Current uagps_up.currentMeasurementControlTransactions: %X")

TRC_MSG(UAGPS_UP_CURRENT_ADDITIONAL_ASSISTANCE_DATA_REQ_AFTER_NEW_REQ, "[UAGPS_UP]: Current uagps_up.additionalAssistanceDataReq: %X")

TRC_MSG(UAGPS_UP_CURRENT_TRANSACTION_ID_FOR_ASSISTANCE_DATA_DELIVERY, "[UAGPS_UP]: Current transaction id for ASSISTANCE DATA DELIVERY message: %d")

TRC_MSG(UAGPS_UP_CALL_BACK_RECORD_MEASUREMENT_ACTION, "[UAGPS_UP]: Uagps_up_callbackRecordMeasurementAction()")

TRC_MSG(UAGPS_UP_CALL_BACK_ASN_ENCODE_SUPL_DATA_REQ, "[UAGPS_UP]: Uagps_up_callbackAsnEncodeSuplDataReq()")

TRC_MSG(UAGPS_UP_CALL_BACK_ADDITIONAL_DATA_REQ_HANDLING, "[UAGPS_UP]: Uagps_up_callbackAdditionalDataReqHandling()")

TRC_MSG(UAGPS_UP_CALL_BACK_CLEAR_TRANSACTION, "[UAGPS_UP]: Uagps_up_callbackClearTransaction()")

TRC_MSG(UAGPS_UP_CALL_BACK_SET_TRANSACTION, "[UAGPS_UP]: Uagps_up_callbackSetTransaction()")

TRC_MSG(UAGPS_UP_CALL_BACK_CLEAR_PREVIOUS_TRANSACTION, "[UAGPS_UP]: Uagps_up_callbackClearPreviousTransaction()")

TRC_MSG(UAGPS_UP_CALL_BACK_SEND_SUPL_DATA_RSP, "[UAGPS_UP]: Uagps_up_callbacksendSuplDataRsp()")

TRC_MSG(UAGPS_UP_CALL_BACK_POSITIONING_METHOD_NOT_SUPPORT, "[UAGPS_UP]: Uagps_up_callbackPositioningMethodNotSupport()")

TRC_MSG(UAGPS_UP_CALL_BACK_REL_MEASUREMENT, "[UAGPS_UP]: Uagps_up_callbackRelMeasurement()")

TRC_MSG(UAGPS_UP_CALL_BACK_MODIFY_WITHOUT_SETUP, "[UAGPS_UP]: Uagps_up_callbackModifyWithoutSetup()")

TRC_MSG(UAGPS_UP_CALL_BACK_MEASUREMENT_TYPE_NOT_POSITIONING, "[UAGPS_UP]: Uagps_up_callbackMeasurementTypeNotPositioning()")

TRC_MSG(UAGPS_UP_CALL_BACK_UPDATE_TRANSACTION_FOR_ASSISTANCE_DATA_DELIVERY, "[UAGPS_UP]: Uagps_up_callbackUpdateTransactionForAssistanceDataDelivery()")

TRC_MSG(UAGPS_UP_CALL_BACK_REPORTING_PERIOD_HANDLING, "[UAGPS_UP]: Uagps_up_callbackReportingPeriodHandling()")

TRC_MSG(UAGPS_UP_CALL_BACK_GET_ASSISTANCE_DATA_DELIVERY_TRANSACTION_ID, "[UAGPS_UP]: Uagps_up_callbackGetAssistanceDataDeliveryTransactionId()")


/* !!! should be modified */
END_TRACE_MAP(MOD_UAGPS_UP)
#endif
#endif  /* _UAGPS_UP_TRC_H */
