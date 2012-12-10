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
 *   uagps_msg_ie_utils.h
 *
 * Project:
 * --------------------------------------------------------
 *   MONZA
 *
 * Description:
 * --------------------------------------------------------
 *   The UAGPS_UP context.
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
 * May 10 2010 mtk02119
 * [MAUI_02419345] [3G AGPS CP] AGPS code check-in for additional assistance data request and tst injec
 * 
 *
 * May 3 2010 mtk02119
 * [MAUI_02412594] [3G AGPS CP] ULCS code check-in for new CP implementation and UP modification
 * add to source control recursely
 *
 *****************************************************************************/

#ifndef _UAGPS_MSG_IE_UTILS_H
#define _UAGPS_MSG_IE_UTILS_H


/*****************************************************************************
 * Macros Without Parameters
 *****************************************************************************/
#define UAGPS_ASSISTANCE_DATA_ALMANAC               0x1
#define UAGPS_ASSISTANCE_DATA_UTC_MODEL             0x2
#define UAGPS_ASSISTANCE_DATA_IONOSPHERIC_MODEL     0x4
#define UAGPS_ASSISTANCE_DATA_NAVIGATION_MODEL      0x8
#define UAGPS_ASSISTANCE_DATA_DGPS_CORRECTIONS      0x10
#define UAGPS_ASSISTANCE_DATA_REFERENCE_LOCATION    0x20
#define UAGPS_ASSISTANCE_DATA_REFERENCE_TIME        0x40
#define UAGPS_ASSISTANCE_DATA_AQUISITION_ASSISTANCE 0x80
#define UAGPS_ASSISTANCE_DATA_REAL_TIME_INTEGRITY   0x100

#define UAGPS_MB_ADEQUATE_ASSISTANCE_DATA \
    UAGPS_ASSISTANCE_DATA_ALMANAC \
    | UAGPS_ASSISTANCE_DATA_UTC_MODEL \
    | UAGPS_ASSISTANCE_DATA_IONOSPHERIC_MODEL \
    | UAGPS_ASSISTANCE_DATA_NAVIGATION_MODEL \
    | UAGPS_ASSISTANCE_DATA_REFERENCE_LOCATION \
    | UAGPS_ASSISTANCE_DATA_REFERENCE_TIME \
    | UAGPS_ASSISTANCE_DATA_REAL_TIME_INTEGRITY

#define UAGPS_MA_ADEQUATE_ASSISTANCE_DATA \
    UAGPS_ASSISTANCE_DATA_REFERENCE_TIME \
    | UAGPS_ASSISTANCE_DATA_AQUISITION_ASSISTANCE


/*****************************************************************************
 * Macros With Parameters
 *****************************************************************************/


/*****************************************************************************
 * Enums without tags
 *****************************************************************************/
typedef enum
{
    UAGPS_MEASUREMENT_REPORT_MOBILE_BASED,
    UAGPS_MEASUREMENT_REPORT_MOBILE_ASSISTED,
    UAGPS_MEASUREMENT_REPORT_ADDITIONAL_ASSISTANCE_DATA_REQ,
    UAGPS_MEASUREMENT_REPORT_POSITIONING_FAILURE,
    UAGPS_MEASUREMENT_REPORT_EVENT_RESULT_7A
#ifdef __L1_GPS_REF_TIME_SUPPORT__
    ,UAGPS_MEASUREMENT_REPORT_EVENT_RESULT_7C
#endif
} UAGPS_MeasurementReportType;

typedef enum
{
    UAGPS_MEASUREMENT_CONTROL_FAILURE_CAUSE_ASN_DECODE_ERROR,
    UAGPS_MEASUREMENT_CONTROL_FAILURE_CAUSE_IE_NOT_ENOUGH,
    UAGPS_MEASUREMENT_CONTROL_FAILURE_CAUSE_MEASUREMENT_TYPE_NOT_SUPPORTED,
    UAGPS_MEASUREMENT_CONTROL_FAILURE_CAUSE_CONFIGURATION_INCOMPLETE
} UAGPS_MeasurementControlFailureCause;

typedef enum
{
    UAGPS_MEASUREMENT_CONTROL_R3,
    UAGPS_MEASUREMENT_CONTROL_R4,
    UAGPS_MEASUREMENT_CONTROL_R6
} UAGPS_MeasurementControlVersion;

typedef enum
{
    UAGPS_MEASUREMENT_INTERNAL_TYPE_SETUP,
    UAGPS_MEASUREMENT_INTERNAL_TYPE_MODIFY,
    UAGPS_MEASUREMENT_INTERNAL_TYPE_RELEASE
} UAGPS_MeasurementInternalType;

typedef enum
{
    UAGPS_SRC_UP,
    UAGPS_SRC_CP
} UAGPS_SrcMod;

/*****************************************************************************
 * Type Definitions
 *****************************************************************************/


/*****************************************************************************
 * Declarations Of Exported Globals
 *****************************************************************************/

extern kal_uint32 BIT_STRING_TO_VALUE(kal_uint8 numBits, kal_uint8 *string);

extern kal_bool Uagps_measurementControlMsgHandler(RRC_MeasurementControl *measurementControl, UAGPS_SrcMod srcMod, kal_uint16 externalTransId);
extern kal_bool Uagps_assistanceDataDeliveryMsgHandler(RRC_AssistanceDataDelivery *assistanceDataDelivery, UAGPS_SrcMod srcMod);
extern void Uagps_sendGpsLcspMeasGadReq(kal_uint8 measurementId, kal_bool environmentValid, kal_uint8 environment, 
                                        kal_bool accuracyValid, kal_uint8 accuracy, kal_uint32 reportingPeriod, kal_bool isCheckAssistData, UAGPS_SrcMod srcMod);
extern void Uagps_sendMeasurementReportMsg(UAGPS_MeasurementReportType measurementReportType, 
                                           gps_pos_gad_cnf_struct *posGadCnf, 
                                           gps_lcsp_meas_gad_cnf_struct *measGadCnf, 
                                           RRC_MeasurementIdentity      measurementId, 
                                           UAGPS_SrcMod srcMod);
extern void Uagps_sendMeasurementControlFailureMsg(kal_uint8 measurementId, 
                                                   UAGPS_MeasurementControlFailureCause failureCause, 
                                                   RRC_RRC_TransactionIdentifier transactionId, 
                                                   RRC_ProtocolErrorCause protocolErrorCauseOfAsn, 
                                                   UAGPS_SrcMod srcMod);
extern kal_bool Uagps_assistanceDataDeliveryMsgHandler(RRC_AssistanceDataDelivery *assistanceDataDelivery, UAGPS_SrcMod srcMod);
extern void Uagps_sendGpsPosGadReq(kal_uint8 measurementId, kal_bool environmentValid, kal_uint8 environment, 
                                   kal_uint8 horizontalAccuracy, kal_bool verticalAccuracyValid, kal_uint8 verticalAccuracy, 
                                   kal_uint32 reportingPeriod, kal_bool isCheckAssistData, UAGPS_SrcMod srcMod);
extern void Uagps_sendGpsAbortReq(kal_uint8 measurementId, UAGPS_SrcMod srcMod);
extern void Uagps_referenceLocationIeHandler(RRC_ReferenceLocation *uePositioningGPSReferenceLocation, RRC_MeasurementIdentity measurementIdentity, UAGPS_SrcMod srcMod);
extern void Uagps_referenceTimeIeHandler(RRC_UE_Positioning_GPS_ReferenceTime *uePositioningGPSReferenceTime, 
                                         RRC_MeasurementIdentity measurementIdentity, 
                                         UAGPS_SrcMod srcMod);
extern void Uagps_realTimeIntegrityIeHandler(RRC_BadSatList *uePositioningGPSRealTimeIntegrity, RRC_MeasurementIdentity measurementIdentity, UAGPS_SrcMod srcMod);
extern void Uagps_navigationModelIeHandler(RRC_UE_Positioning_GPS_NavigationModel *uePositioningGPSNavigationModel, RRC_MeasurementIdentity measurementIdentity, UAGPS_SrcMod srcMod);
extern void Uagps_almanacIeHandler(RRC_UE_Positioning_GPS_Almanac *uePositioningGPSAlmanac, RRC_MeasurementIdentity measurementIdentity, UAGPS_SrcMod srcMod);
extern void Uagps_ionosphericModelIeHandler(RRC_UE_Positioning_GPS_IonosphericModel *uePositioningGPSIonosphericModel, RRC_MeasurementIdentity measurementIdentity, UAGPS_SrcMod srcMod);
extern void Uagps_utcModelIeHandler(RRC_UE_Positioning_GPS_UTC_Model *uePositioningGPSUTCModel, RRC_MeasurementIdentity measurementIdentity, UAGPS_SrcMod srcMod);

#endif /* _UAGPS_MSG_IE_UTILS_H */

