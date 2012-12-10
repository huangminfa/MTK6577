/******************************************************************************
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
*******************************************************************************/

/******************************************************************************
 * Filename:
 * --------------------------------------------------------
 *   uagps_up_msg_hdler.h
 *
 * Project:
 * --------------------------------------------------------
 *   AGPS
 *
 * Description:
 * --------------------------------------------------------
 *   Declaration of funcitons of the UAGPS_UP.
 *
 * Author:
 * --------------------------------------------------------
 *   David Niu
 *
 * --------------------------------------------------------
 * $Log$
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
 * Jun 29 2009 mtk02119
 * [MAUI_01711600] [MT6268_AGPS_Dev] Add UAGPS_UP module related code
 * add to source control recursely
 *
 *****************************************************************************/

#ifndef _UAGPS_UP_MSG_HDLER_H
#define _UAGPS_UP_MSG_HDLER_H


/* GLOBAL VARIABLE DECLARATIONS *********************************************/
 

/* PUBLIC FUNCTION PROTOTYPES ***********************************************/
extern void uagps_up_init(void);

extern void uagps_up_reset(void);

extern kal_bool Uagps_up_suplStartReqHandler(ilm_struct* pIlm);

extern kal_bool Uagps_up_suplDataIndHandler(ilm_struct* pIlm);

extern kal_bool Uagps_up_gpsPosGadCnfHandler(ilm_struct* pIlm);

extern kal_bool Uagps_up_gpsMeasGadCnfHandler(ilm_struct* pIlm);

extern kal_bool Uagps_up_gpsAssistDataCnfHandler(ilm_struct* pIlm);

extern kal_bool Uagps_up_suplDataCnfHandler(ilm_struct* pIlm);

extern kal_bool Uagps_up_suplEndReqHandler(ilm_struct* pIlm);

extern kal_bool Uagps_up_tstInjectHandler(ilm_struct* pIlmInject);

extern void Uagps_up_callbackRecordPositioningAction(kal_uint8 measurementId, kal_bool environmentValid, kal_uint8 environment, kal_uint8 horizontalAccuracy, kal_bool verticalAccuracyValid, kal_uint8 verticalAccuracy, kal_uint32 reportingPeriod);

extern void Uagps_up_callbackRecordMeasurementAction(kal_uint8 measurementId, kal_bool environmentValid, kal_uint8 environment, kal_bool accuracyValid, kal_uint8 accuracy, kal_uint32 reportingPeriod);
extern void Uagps_up_callbackAsnEncodeSuplDataReq(RRC_UL_DCCH_Message *ulDcchMsg, kal_uint8 *encodeBuffer, 
                                                  RRC_UL_DCCH_MessageType_selector messageType, UAGPS_MeasurementReportType measurementReportType);
extern void Uagps_up_callbackAdditionalDataReqHandling(kal_uint8 measurementId, kal_bool additionalAssistanceDataRequest);
extern void Uagps_up_callbackClearTransaction(kal_uint8 measurementId);
extern void Uagps_up_callbackSetTransaction(kal_uint8 measurementId);
extern void Uagps_up_callbackForTraceOfAction(void);
extern void Uagps_up_callbackClearPreviousTransaction(kal_uint8 measurementId);
extern void Uagps_up_callbacksendSuplDataRsp(kal_bool result);
extern void Uagps_up_callbackPositioningMethodNotSupport(kal_uint8 measurementId, UAGPS_MeasurementInternalType measurementInternalType, RRC_RRC_TransactionIdentifier transactionId);
extern void Uagps_up_callbackRelMeasurement(kal_uint8 measurementId);
extern kal_bool Uagps_up_callbackModifyWithoutSetup(kal_uint8 measurementId, RRC_RRC_TransactionIdentifier transactionId);
extern void Uagps_up_callbackMeasurementTypeNotPositioning(kal_uint8 measurementId, kal_bool isFromSetup, RRC_RRC_TransactionIdentifier transactionId);
extern void Uagps_up_callbackUpdateTransactionForAssistanceDataDelivery(void);
extern kal_uint32 Uagps_up_callbackReportingPeriodHandling(RRC_ReportingIntervalLong reportingInterval);
extern kal_uint8 Uagps_up_callbackGetAssistanceDataDeliveryTransactionId(void);


#endif /* _UAGPS_UP_MSG_HDLER_H */
