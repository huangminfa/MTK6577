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
 *	uagps_trc.h  !!! should be modified
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
 * Jun 28 2010 mtk02119
 * [MAUI_02572033] [3G AGPS CP] Modifications of ULCS functions
 * 
 *
 * May 15 2010 mtk02119
 * [MAUI_02525171] [3G AGPS CP] ULCS code check-in for modification about Measurement Control Failure m
 * 
 *
 * May 3 2010 mtk02119
 * [MAUI_02412594] [3G AGPS CP] ULCS code check-in for new CP implementation and UP modification
 * add to source control recursely
 *
 *===========================================================================
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *==============================================================================
 *******************************************************************************/
/*!!! should be modified */
#ifndef _UAGPS_TRC_H
#define _UAGPS_TRC_H

#ifdef GEN_FOR_PC
#include "kal_trace.h"
#endif /* GEN_FOR_PC */

/* !!!Note: Please define the group ID to TRACE_GROUP_1 ... or find and replace with correct one*/

#if 1

#define UAGPS_SEND_GPS_ABORT_REQ "[UAGPS]: Uagps_sendGpsAbortReq() with measurementId: %d"
#define UAGPS_SEND_GPS_POS_GAD_REQ "[UAGPS]: Uagps_sendGpsPosGadReq() with measurementId: %d, environmentValid: %Mkal_bool, environment: %d, horizontalAccuracy: %d, verticalAccuracyValid: %Mkal_bool, verticalAccuracy: %d"
#define UAGPS_SEND_GPS_MEAS_GAD_REQ "[UAGPS]: Uagps_sendGpsLcspMeasGadReq() with measurementId: %d, environmentValid: %Mkal_bool, environment: %d, accuracyValid: %Mkal_bool, accuracy: %d, delay: %d"
#define UAGPS_FILL_POSITION_RESULT "[UAGPS]: Uagps_fillPositionResult()"
#define UAGPS_FILL_POSITION_RESULT_LATITUDE_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - latitude with value: %d"
#define UAGPS_FILL_POSITION_RESULT_LONGITUDE_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - longitude with value: %d"
#define UAGPS_FILL_POSITION_RESULT_ALTITUDE_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - altitude with value: %d"
#define UAGPS_FILL_POSITION_RESULT_UNCERTAIN_SEMI_MAJOR_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - uncertain semi major with value: %d"
#define UAGPS_FILL_POSITION_RESULT_UNCERTAIN_SEMI_MINOR_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - uncertain semi minor with value: %d"
#define UAGPS_FILL_POSITION_RESULT_ORIENTATION_MAJOR_AXIS_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - orientation major axis with value: %d"
#define UAGPS_FILL_POSITION_RESULT_UNCERTAIN_ALTITUDE_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - uncertain altitude with value: %d"
#define UAGPS_FILL_POSITION_RESULT_CONFIDENCE_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - confidence with value: %d"
#define UAGPS_FILL_MEASUREMENT_RESULT "[UAGPS]: Uagps_fillMeasurementResult()"
#define UAGPS_FILL_MEASUREMENT_RESULT_SAT_ID_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - SAT id with value: %d"
#define UAGPS_FILL_MEASUREMENT_RESULT_CARRIER_NOISE_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - carrier noise with value: %d"
#define UAGPS_FILL_MEASUREMENT_RESULT_WHOLE_GPS_CHIPS_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - whole GPS chips with value: %d"
#define UAGPS_FILL_MEASUREMENT_RESULT_FRACTIONAL_GPS_CHIPS_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - fractional GPS chips with value: %d"
#define UAGPS_FILL_MEASUREMENT_RESULT_MULTIPATH_INDICATOR_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - multipath indicator with value: %d"
#define UAGPS_FILL_MEASUREMENT_RESULT_PSEUDO_RANGE_RMS_ERROR_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - pseudo range RMS error with value: %d"
#define UAGPS_FILL_REF_TIME_RESULT "[UAGPS]: Uagps_fillRefTimeResult()"
#define UAGPS_FILL_REF_TIME_RESULT_REF_TIME_MS_PART_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - ref time ms part with value: %d"
#define UAGPS_FILL_REF_TIME_RESULT_FDD_REF_ID_PSC_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - FDD PSC with value: %d"
#define UAGPS_FILL_REF_TIME_RESULT_SFN_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - SFN with value: %d"
#define UAGPS_SEND_MEASUREMENT_REPORT_MSG "[UAGPS]: Uagps_sendMeasurementReportMsg() with measurementReportType: %MUAGPS_MeasurementReportType, measurementId: %d"
#define UAGPS_GPS_POSITIONING_RESULT_TOW "[UAGPS]: GPS positioning result -- tow: %d"
#define UAGPS_GPS_POSITIONING_RESULT_FIX_TYPE "[UAGPS]: GPS positioning result -- fix type: %d"
#define UAGPS_GPS_POSITIONING_RESULT_POS_ESTIMATE_HORIZONTAL "[UAGPS]: GPS positioning result -- sign_latitude: %Mkal_bool, latitude: %d, longtitude: %d"
#define UAGPS_GPS_POSITIONING_RESULT_POS_ESTIMATE_VERTICAL "[UAGPS]: GPS positioning result -- sign_altitude: %Mkal_bool, altitude: %d"
#define UAGPS_GPS_POSITIONING_RESULT_POS_ESTIMATE_UNCERTAINTY "[UAGPS]: GPS positioning result -- unc_major: %d, unc_minor: %d, unc_bear: %d, unc_altitude: %d, confidence: %d"
#define UAGPS_GPS_MEASUREMENT_RESULT_TOW "[UAGPS]: GPS measurement result -- tow: %d"
#define UAGPS_GPS_MEASUREMENT_REPORT_RESULT_GPS_TOW_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - GPS TOW with value: %d"
#define UAGPS_GPS_MEASUREMENT_RESULT_SAT_NUM "[UAGPS]: GPS measurement result -- sate_num: %d"
#define UAGPS_GPS_MEASUREMENT_RESULT_SAT_ID "[UAGPS]: GPS measurement result -- sat_id: %d"
#define UAGPS_GPS_MEASUREMENT_RESULT_PARAMETERS_1 "[UAGPS]: GPS measurement result -- carrier_noise: %d, doppler: %d, whole_chips: %d"
#define UAGPS_GPS_MEASUREMENT_RESULT_PARAMETERS_2 "[UAGPS]: GPS measurement result -- fractional_chips: %d, multipath: %d, pseudorang_e: %d, pseudorang_m: %d"
#define UAGPS_ADDITIONAL_DATA_REQ_BITMAP "[UAGPS]: Assistance data request bitmap: %X"
#define UAGPS_ADDITIONAL_DATA_REQ_FOR_ALMANAC "[UAGPS]: Assistance data request for almanac"
#define UAGPS_ADDITIONAL_DATA_REQ_FOR_UTC_MODEL "[UAGPS]: Assistance data request for utc model"
#define UAGPS_ADDITIONAL_DATA_REQ_FOR_IONOSPHERIC_MODEL "[UAGPS]: Assistance data request for ionospheric model"
#define UAGPS_ADDITIONAL_DATA_REQ_FOR_NAVIGATION_MODEL "[UAGPS]: Assistance data request for navigation model"
#define UAGPS_ADDITIONAL_DATA_REQ_FOR_DGPS_CORRECTION "[UAGPS]: Assistance data request for dgps correction"
#define UAGPS_ADDITIONAL_DATA_REQ_FOR_REF_LOCATION "[UAGPS]: Assistance data request for reference location"
#define UAGPS_ADDITIONAL_DATA_REQ_FOR_REF_TIME "[UAGPS]: Assistance data request for reference time"
#define UAGPS_ADDITIONAL_DATA_REQ_FOR_AQUISITION_ASSISTANCE "[UAGPS]: Assistance data request for aquisition assistance"
#define UAGPS_ADDITIONAL_DATA_REQ_FOR_REAL_TIME_INTEGRITY "[UAGPS]: Assistance data request for real time integrity"
#define UAGPS_UL_DCCH_MSG_ASN_ENCODE_RESULT "[UAGPS]: Encode result for UL DCCH msg: %d"
#define UAGPS_SEND_MEASUREMENT_CONTROL_FAILURE_MSG "[UAGPS]: Uagps_sendMeasurementControlFailureMsg() with measurementIdentity: %d, failureCause: %MUAGPS_MeasurementControlFailureCause, transactionId: %d, protocolErrorCauseOfAsn: %MRRC_ProtocolErrorCause"
#define UAGPS_REF_TIME_IE_HDLER "[UAGPS]: Uagps_referenceTimeIeHandler() with measurementIdentity: %d"
#define UAGPS_REF_TIME_IE_CONTENT "[UAGPS]: Assistance data for reference time with gps_Week: %d, gps_tow_1msec: %d"
#define UAGPS_REF_TIME_IE_TOW_ASSIST_LIST "[UAGPS]: Assistance data for reference time include GPS TOW Assist List with numElements for SAT: %d"
#define UAGPS_REF_LOCATION_IE_HDLER "[UAGPS]: Uagps_referenceLocationIeHandler() with measurementIdentity: %d"
#define UAGPS_REF_LOCATION_IE_CONTENT_HORIZONTAL_INFO "[UAGPS]: Assistance data for reference location horizontal information with latitudeSign: %d, latitude: %d, longitude: %d"
#define UAGPS_REF_LOCATION_IE_CONTENT_VERTICAL_INFO "[UAGPS]: Assistance data for reference location vertical information with altitudeDirection: %d, altitude: %d"
#define UAGPS_REF_LOCATION_IE_CONTENT_UNCERTAINTY_INFO "[UAGPS]: Assistance data for reference location uncertainty information with uncertaintySemiMajor: %d, uncertaintySemiMinor: %d, orientationMajorAxis: %d, uncertaintyAltitude: %d, confidence: %d"
#define UAGPS_DGPS_CORRECTION_IE_HDLER "[UAGPS]: Uagps_dgpsCorrectionsIeHandler() with measurementIdentity: %d"
#define UAGPS_DGPS_CORRECTION_IE_CONTENT_TOW "[UAGPS]: Assistance data for dgps correction with gps_TOW: %d"
#define UAGPS_DGPS_CORRECTION_IE_CONTENT_STATUS_HEALTH "[UAGPS]: Assistance data for dgps correction with statusHealth: %d"
#define UAGPS_DGPS_CORRECTION_IE_CONTENT_SAT_NUM "[UAGPS]: Assistance data for dgps correction with numElements for SAT: %d"
#define UAGPS_DGPS_CORRECTION_IE_CONTENT_FOR_ONE_SAT "[UAGPS]: Assistance data for dgps correction for one SAT with satID: %d, iode: %d, udre: %d, prc: %d, rrc: %d"
#define UAGPS_NAVIGATION_MODEL_IE_HDLER "[UAGPS]: Uagps_navigationModelIeHandler() with measurementIdentity: %d"
#define UAGPS_NAVIGATION_MODEL_IE_CONTENT_SAT_NUM "[UAGPS]: Assistance data for navigation model with numElements for SAT: %d"
#define UAGPS_NAVIGATION_MODEL_REAL_SAT_INFO_NUM "[UAGPS]: Assistance data for navigation model with ephemerisParameter information for %d SAT"
#define UAGPS_NAVIGATION_MODEL_IE_CONTENT_FOR_ONE_SAT_1 "[UAGPS]: Assistance data for navigation model for one SAT with sv: %d, wn: %d, c_a: %d, ura: %d, sv_health: %d, iodc: %d, l2p: %d"
#define UAGPS_NAVIGATION_MODEL_IE_CONTENT_FOR_ONE_SAT_2 "[UAGPS]: Assistance data for navigation model for one SAT with sf1_23msb: %d, sf1_24mb1: %d, sf1_24mb2: %d, sf1_16lsb: %d, tgd: %d, toc: %d, af2: %d, af1: %d, af0: %d"
#define UAGPS_NAVIGATION_MODEL_IE_CONTENT_FOR_ONE_SAT_3 "[UAGPS]: Assistance data for navigation model for one SAT with iode: %d, crs: %d, delta_n: %d, m0: %d, cuc: %d, e: %d"
#define UAGPS_NAVIGATION_MODEL_IE_CONTENT_FOR_ONE_SAT_4 "[UAGPS]: Assistance data for navigation model for one SAT with cus: %d, sqrt_a: %d, toe: %d, fit: %d, aodo: %d, cic: %d"
#define UAGPS_NAVIGATION_MODEL_IE_CONTENT_FOR_ONE_SAT_5 "[UAGPS]: Assistance data for navigation model for one SAT with omega0: %d, cis: %d, i0: %d, crc: %d, w: %d, omega_dot: %d, idot: %d"
#define UAGPS_IONOSPHERIC_MODEL_IE_HDLER "[UAGPS]: Uagps_ionosphericModelIeHandler() with measurementIdentity: %d"
#define UAGPS_IONOSPHERIC_MODEL_IE_CONTENT "[UAGPS]: Assistance data for ionospheric model with a0: %d, a1: %d, a2: %d, a3: %d, b0: %d, b1: %d, b2: %d, b3: %d"
#define UAGPS_UTC_MODEL_IE_HDLER "[UAGPS]: Uagps_utcModelIeHandler() with measurementIdentity: %d"
#define UAGPS_UTC_MODEL_IE_CONTENT "[UAGPS]: Assistance data for utc model with a1: %d, a0: %d, tot: %d, wnt: %d, delta_ls: %d, wnlsf: %d, dn: %d, delta_lsf: %d"
#define UAGPS_ALMANAC_IE_HDLER "[UAGPS]: Uagps_almanacIeHandler() with measurementIdentity: %d"
#define UAGPS_ALMANAC_IE_CONTENT_SAT_NUM "[UAGPS]: Assistance data for almanac with numElements for SAT: %d"
#define UAGPS_ACQUISITION_ASSISTANCE_IE_HDLER "[UAGPS]: Uagps_acquisitionAssistanceIeHandler() with measurementIdentity: %d"
#define UAGPS_ACQUISITION_ASSISTANCE_IE_CONTENT_SAT_NUM "[UAGPS]: Assistance data for acqusition assistance with numElements for SAT: %d"
#define UAGPS_ACQUISITION_ASSISTANCE_IE_CONTENT_EXTRA_DOPPLER_INFO_VALID "[UAGPS]: Assistance data for acqusition assistance with extraDopplerInfo"
#define UAGPS_ACQUISITION_ASSISTANCE_IE_CONTENT_AZIMUTH_AND_ELEVATION_VALID "[UAGPS]: Assistance data for acqusition assistance with azimuthAndElevation"
#define UAGPS_REAL_TIME_INTEGRITY_IE_HDLER "[UAGPS]: Uagps_acquisitionAssistanceIeHandler() with measurementIdentity: %d"
#define UAGPS_REAL_TIME_INTEGRITY_IE_CONTENT_SAT_NUM "[UAGPS]: Assistance data for real time integrity with numElements for bad SAT: %d"
#define UAGPS_ADDITIONAL_DATA_REF_TIME_RECEIVED "[UAGPS]: Assistance data for REFERENCE TIME is received"
#define UAGPS_ADDITIONAL_DATA_REF_LOCATION_RECEIVED "[UAGPS]: Assistance data for REFERENCE LOCATION is received"
#define UAGPS_ADDITIONAL_DATA_DGPS_CORRECTION_RECEIVED "[UAGPS]: Assistance data for DGPS CORRECTION is received"
#define UAGPS_ADDITIONAL_DATA_NAVIGATION_MODEL_RECEIVED "[UAGPS]: Assistance data for NAVIGATION MODEL is received"
#define UAGPS_ADDITIONAL_DATA_IONOSPHERIC_MODEL_RECEIVED "[UAGPS]: Assistance data for IONOSPHERIC MODEL is received"
#define UAGPS_ADDITIONAL_DATA_UTC_MODEL_RECEIVED "[UAGPS]: Assistance data for UTC MODEL is received"
#define UAGPS_ADDITIONAL_DATA_ALMANAC_RECEIVED "[UAGPS]: Assistance data for ALMANAC is received"
#define UAGPS_ADDITIONAL_DATA_AQUISITION_ASSISTANCE_RECEIVED "[UAGPS]: Assistance data for AQUISITION ASSISTANCE is received"
#define UAGPS_ADDITIONAL_DATA_REAL_TIME_INTEGRITY_RECEIVED "[UAGPS]: Assistance data for REAL TIME INTEGRITY is received"
#define UAGPS_MEASUREMENT_CONTROL_IE_VERSION "[UAGPS]: IE in MEASUREMENT CONTROL message is version: %MUAGPS_MeasurementControlVersion"
#define UAGPS_MEASUREMENT_CONTROL_IE_TYPE "[UAGPS]: IE in MEASUREMENT CONTROL message is type: %MUAGPS_MeasurementInternalType"
#define UAGPS_MEASUREMENT_CONTROL_IE_CONTENT "[UAGPS]: IE in MEASUREMENT CONTROL message with measurementId: %d, positioningMethod: %MRRC_PositioningMethod, assistanceDataValid: %Mkal_bool, transactionId: %d, verticalAccuracyValid: %Mkal_bool"
#define UAGPS_MODIFY_COMMAND_WITHOUT_ASSISTANCE_DATA "[UAGPS]: Modify command without assistance data"
#define UAGPS_MODIFY_COMMAND_WITHOUT_MEASUREMENT_INFORMATION "[UAGPS]: Modify command without measurement data"
#define UAGPS_MEASUREMENT_CONTROL_MSG_HDLER "[UAGPS]: Uagps_measurementControlMsgHandler()"
#define UAGPS_MEASUREMENT_CONTROL_IS_NOT_SUPPORTED_VERSION "[UAGPS]: IE in MEASUREMENT CONTROL message is not supported version"
#define UAGPS_MEASUREMENT_TYPE_HDLER "[UAGPS]: Uagps_measurementTypeHandler()"
#define UAGPS_UE_POSITIONING_REPORTING_QUANTITY_IE_HDLER "[UAGPS]: Uagps_uePositioningReportingQuantityIeHandler()"
#define UAGPS_NEW_MEASUREMENT_REQ_TYPE "[UAGPS]: Positioning method type: %MRRC_UE_Positioning_MethodType"
#define UAGPS_UE_POSITIONING_REPORTING_QUANTITY_R4_IE_HDLER "[UAGPS]: Uagps_uePositioningReportingQuantityR4IeHandler()"
#define UAGPS_ASSISTANCE_DATA_DELIVERY_MSG_HDLER "[UAGPS]: Uagps_assistanceDataDeliveryMsgHandler()"
#define UAGPS_ASSISTANCE_DATA_DELIVERY_WITHOUT_ASSISTANCE_DATA "[UAGPS]: ASSISTANCE DATA DELIVERY message without assistance data"
#define UAGPS_ASSISTANCE_DATA_DELIVERY_IS_NOT_SUPPORTED_VERSION "[UAGPS]: IE in ASSISTANCE DATA DELIVERY message is not supported version"

#define UAGPS_GPS_MEASUREMENT_REPORT_RESULT_GPS_TOE_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - GPS TOE with value: %d"
#define UAGPS_GPS_MEASUREMENT_REPORT_RESULT_T_TOE_LIMIT_ERROR "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - T TOE LIMIT with value: %d"
#else
/* !!! should be modified */
BEGIN_TRACE_MAP(MOD_UAGPS)

/* TRACE_INFO */

/* TRACE_WARNING */

/* TRACE_GROUP_1 */
TRC_MSG(UAGPS_SEND_GPS_ABORT_REQ, "[UAGPS]: Uagps_sendGpsAbortReq() with measurementId: %d")

TRC_MSG(UAGPS_SEND_GPS_POS_GAD_REQ, "[UAGPS]: Uagps_sendGpsPosGadReq() with measurementId: %d, environmentValid: %Mkal_bool, environment: %d, horizontalAccuracy: %d, verticalAccuracyValid: %Mkal_bool, verticalAccuracy: %d")

TRC_MSG(UAGPS_SEND_GPS_MEAS_GAD_REQ, "[UAGPS]: Uagps_sendGpsLcspMeasGadReq() with measurementId: %d, environmentValid: %Mkal_bool, environment: %d, accuracyValid: %Mkal_bool, accuracy: %d, delay: %d")

TRC_MSG(UAGPS_FILL_POSITION_RESULT, "[UAGPS]: Uagps_fillPositionResult()")

TRC_MSG(UAGPS_FILL_POSITION_RESULT_LATITUDE_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - latitude with value: %d")

TRC_MSG(UAGPS_FILL_POSITION_RESULT_LONGITUDE_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - longitude with value: %d")

TRC_MSG(UAGPS_FILL_POSITION_RESULT_ALTITUDE_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - altitude with value: %d")

TRC_MSG(UAGPS_FILL_POSITION_RESULT_UNCERTAIN_SEMI_MAJOR_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - uncertain semi major with value: %d")

TRC_MSG(UAGPS_FILL_POSITION_RESULT_UNCERTAIN_SEMI_MINOR_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - uncertain semi minor with value: %d")

TRC_MSG(UAGPS_FILL_POSITION_RESULT_ORIENTATION_MAJOR_AXIS_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - orientation major axis with value: %d")

TRC_MSG(UAGPS_FILL_POSITION_RESULT_UNCERTAIN_ALTITUDE_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - uncertain altitude with value: %d")

TRC_MSG(UAGPS_FILL_POSITION_RESULT_CONFIDENCE_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - confidence with value: %d")

TRC_MSG(UAGPS_FILL_MEASUREMENT_RESULT, "[UAGPS]: Uagps_fillMeasurementResult()")

TRC_MSG(UAGPS_FILL_MEASUREMENT_RESULT_SAT_ID_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - SAT id with value: %d")

TRC_MSG(UAGPS_FILL_MEASUREMENT_RESULT_CARRIER_NOISE_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - carrier noise with value: %d")

TRC_MSG(UAGPS_FILL_MEASUREMENT_RESULT_WHOLE_GPS_CHIPS_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - whole GPS chips with value: %d")

TRC_MSG(UAGPS_FILL_MEASUREMENT_RESULT_FRACTIONAL_GPS_CHIPS_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - fractional GPS chips with value: %d")

TRC_MSG(UAGPS_FILL_MEASUREMENT_RESULT_MULTIPATH_INDICATOR_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - multipath indicator with value: %d")

TRC_MSG(UAGPS_FILL_MEASUREMENT_RESULT_PSEUDO_RANGE_RMS_ERROR_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - pseudo range RMS error with value: %d")

TRC_MSG(UAGPS_FILL_REF_TIME_RESULT, "[UAGPS]: Uagps_fillRefTimeResult()")

TRC_MSG(UAGPS_FILL_REF_TIME_RESULT_REF_TIME_MS_PART_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - ref time ms part with value: %d")

TRC_MSG(UAGPS_FILL_REF_TIME_RESULT_FDD_REF_ID_PSC_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - FDD PSC with value: %d")

TRC_MSG(UAGPS_FILL_REF_TIME_RESULT_SFN_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - SFN with value: %d")

TRC_MSG(UAGPS_SEND_MEASUREMENT_REPORT_MSG, "[UAGPS]: Uagps_sendMeasurementReportMsg() with measurementReportType: %MUAGPS_MeasurementReportType, measurementId: %d")

TRC_MSG(UAGPS_GPS_POSITIONING_RESULT_TOW, "[UAGPS]: GPS positioning result -- tow: %d")

TRC_MSG(UAGPS_GPS_POSITIONING_RESULT_FIX_TYPE, "[UAGPS]: GPS positioning result -- fix type: %d")

TRC_MSG(UAGPS_GPS_POSITIONING_RESULT_POS_ESTIMATE_HORIZONTAL, "[UAGPS]: GPS positioning result -- sign_latitude: %Mkal_bool, latitude: %d, longtitude: %d")

TRC_MSG(UAGPS_GPS_POSITIONING_RESULT_POS_ESTIMATE_VERTICAL, "[UAGPS]: GPS positioning result -- sign_altitude: %Mkal_bool, altitude: %d")

TRC_MSG(UAGPS_GPS_POSITIONING_RESULT_POS_ESTIMATE_UNCERTAINTY, "[UAGPS]: GPS positioning result -- unc_major: %d, unc_minor: %d, unc_bear: %d, unc_altitude: %d, confidence: %d")

TRC_MSG(UAGPS_GPS_MEASUREMENT_RESULT_TOW, "[UAGPS]: GPS measurement result -- tow: %d")

TRC_MSG(UAGPS_GPS_MEASUREMENT_REPORT_RESULT_GPS_TOW_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - GPS TOW with value: %d")

TRC_MSG(UAGPS_GPS_MEASUREMENT_REPORT_RESULT_GPS_TOE_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - GPS TOE with value: %d")

TRC_MSG(UAGPS_GPS_MEASUREMENT_REPORT_RESULT_T_TOE_LIMIT_ERROR, "[UAGPS]: ERROR - GPS REPORTED VALUE EXCEEDS VALID RANGE - T TOE LIMIT with value: %d")

TRC_MSG(UAGPS_GPS_MEASUREMENT_RESULT_SAT_NUM, "[UAGPS]: GPS measurement result -- sate_num: %d")

TRC_MSG(UAGPS_GPS_MEASUREMENT_RESULT_SAT_ID, "[UAGPS]: GPS measurement result -- sat_id: %d")

TRC_MSG(UAGPS_GPS_MEASUREMENT_RESULT_PARAMETERS_1, "[UAGPS]: GPS measurement result -- carrier_noise: %d, doppler: %d, whole_chips: %d")

TRC_MSG(UAGPS_GPS_MEASUREMENT_RESULT_PARAMETERS_2, "[UAGPS]: GPS measurement result -- fractional_chips: %d, multipath: %d, pseudorang_e: %d, pseudorang_m: %d")

TRC_MSG(UAGPS_ADDITIONAL_DATA_REQ_BITMAP, "[UAGPS]: Assistance data request bitmap: %X")

TRC_MSG(UAGPS_ADDITIONAL_DATA_REQ_FOR_ALMANAC, "[UAGPS]: Assistance data request for almanac")

TRC_MSG(UAGPS_ADDITIONAL_DATA_REQ_FOR_UTC_MODEL, "[UAGPS]: Assistance data request for utc model")

TRC_MSG(UAGPS_ADDITIONAL_DATA_REQ_FOR_IONOSPHERIC_MODEL, "[UAGPS]: Assistance data request for ionospheric model")

TRC_MSG(UAGPS_ADDITIONAL_DATA_REQ_FOR_NAVIGATION_MODEL, "[UAGPS]: Assistance data request for navigation model")

TRC_MSG(UAGPS_ADDITIONAL_DATA_REQ_FOR_DGPS_CORRECTION, "[UAGPS]: Assistance data request for dgps correction")

TRC_MSG(UAGPS_ADDITIONAL_DATA_REQ_FOR_REF_LOCATION, "[UAGPS]: Assistance data request for reference location")

TRC_MSG(UAGPS_ADDITIONAL_DATA_REQ_FOR_REF_TIME, "[UAGPS]: Assistance data request for reference time")

TRC_MSG(UAGPS_ADDITIONAL_DATA_REQ_FOR_AQUISITION_ASSISTANCE, "[UAGPS]: Assistance data request for aquisition assistance")

TRC_MSG(UAGPS_ADDITIONAL_DATA_REQ_FOR_REAL_TIME_INTEGRITY, "[UAGPS]: Assistance data request for real time integrity")

TRC_MSG(UAGPS_UL_DCCH_MSG_ASN_ENCODE_RESULT, "[UAGPS]: Encode result for UL DCCH msg: %d")

TRC_MSG(UAGPS_SEND_MEASUREMENT_CONTROL_FAILURE_MSG, "[UAGPS]: Uagps_sendMeasurementControlFailureMsg() with measurementIdentity: %d, failureCause: %MUAGPS_MeasurementControlFailureCause, transactionId: %d, protocolErrorCauseOfAsn: %MRRC_ProtocolErrorCause")

TRC_MSG(UAGPS_REF_TIME_IE_HDLER, "[UAGPS]: Uagps_referenceTimeIeHandler() with measurementIdentity: %d")

TRC_MSG(UAGPS_REF_TIME_IE_CONTENT, "[UAGPS]: Assistance data for reference time with gps_Week: %d, gps_tow_1msec: %d")

TRC_MSG(UAGPS_REF_TIME_IE_TOW_ASSIST_LIST, "[UAGPS]: Assistance data for reference time include GPS TOW Assist List with numElements for SAT: %d")

TRC_MSG(UAGPS_REF_LOCATION_IE_HDLER, "[UAGPS]: Uagps_referenceLocationIeHandler() with measurementIdentity: %d")

TRC_MSG(UAGPS_REF_LOCATION_IE_CONTENT_HORIZONTAL_INFO, "[UAGPS]: Assistance data for reference location horizontal information with latitudeSign: %d, latitude: %d, longitude: %d")

TRC_MSG(UAGPS_REF_LOCATION_IE_CONTENT_VERTICAL_INFO, "[UAGPS]: Assistance data for reference location vertical information with altitudeDirection: %d, altitude: %d")

TRC_MSG(UAGPS_REF_LOCATION_IE_CONTENT_UNCERTAINTY_INFO, "[UAGPS]: Assistance data for reference location uncertainty information with uncertaintySemiMajor: %d, uncertaintySemiMinor: %d, orientationMajorAxis: %d, uncertaintyAltitude: %d, confidence: %d")

TRC_MSG(UAGPS_DGPS_CORRECTION_IE_HDLER, "[UAGPS]: Uagps_dgpsCorrectionsIeHandler() with measurementIdentity: %d")

TRC_MSG(UAGPS_DGPS_CORRECTION_IE_CONTENT_TOW, "[UAGPS]: Assistance data for dgps correction with gps_TOW: %d")

TRC_MSG(UAGPS_DGPS_CORRECTION_IE_CONTENT_STATUS_HEALTH, "[UAGPS]: Assistance data for dgps correction with statusHealth: %d")

TRC_MSG(UAGPS_DGPS_CORRECTION_IE_CONTENT_SAT_NUM, "[UAGPS]: Assistance data for dgps correction with numElements for SAT: %d")

TRC_MSG(UAGPS_DGPS_CORRECTION_IE_CONTENT_FOR_ONE_SAT, "[UAGPS]: Assistance data for dgps correction for one SAT with satID: %d, iode: %d, udre: %d, prc: %d, rrc: %d")

TRC_MSG(UAGPS_NAVIGATION_MODEL_IE_HDLER, "[UAGPS]: Uagps_navigationModelIeHandler() with measurementIdentity: %d")

TRC_MSG(UAGPS_NAVIGATION_MODEL_IE_CONTENT_SAT_NUM, "[UAGPS]: Assistance data for navigation model with numElements for SAT: %d")

TRC_MSG(UAGPS_NAVIGATION_MODEL_REAL_SAT_INFO_NUM, "[UAGPS]: Assistance data for navigation model with ephemerisParameter information for %d SAT")

TRC_MSG(UAGPS_NAVIGATION_MODEL_IE_CONTENT_FOR_ONE_SAT_1, "[UAGPS]: Assistance data for navigation model for one SAT with sv: %d, wn: %d, c_a: %d, ura: %d, sv_health: %d, iodc: %d, l2p: %d")

TRC_MSG(UAGPS_NAVIGATION_MODEL_IE_CONTENT_FOR_ONE_SAT_2, "[UAGPS]: Assistance data for navigation model for one SAT with sf1_23msb: %d, sf1_24mb1: %d, sf1_24mb2: %d, sf1_16lsb: %d, tgd: %d, toc: %d, af2: %d, af1: %d, af0: %d")

TRC_MSG(UAGPS_NAVIGATION_MODEL_IE_CONTENT_FOR_ONE_SAT_3, "[UAGPS]: Assistance data for navigation model for one SAT with iode: %d, crs: %d, delta_n: %d, m0: %d, cuc: %d, e: %d")

TRC_MSG(UAGPS_NAVIGATION_MODEL_IE_CONTENT_FOR_ONE_SAT_4, "[UAGPS]: Assistance data for navigation model for one SAT with cus: %d, sqrt_a: %d, toe: %d, fit: %d, aodo: %d, cic: %d")

TRC_MSG(UAGPS_NAVIGATION_MODEL_IE_CONTENT_FOR_ONE_SAT_5, "[UAGPS]: Assistance data for navigation model for one SAT with omega0: %d, cis: %d, i0: %d, crc: %d, w: %d, omega_dot: %d, idot: %d")

TRC_MSG(UAGPS_IONOSPHERIC_MODEL_IE_HDLER, "[UAGPS]: Uagps_ionosphericModelIeHandler() with measurementIdentity: %d")

TRC_MSG(UAGPS_IONOSPHERIC_MODEL_IE_CONTENT, "[UAGPS]: Assistance data for ionospheric model with a0: %d, a1: %d, a2: %d, a3: %d, b0: %d, b1: %d, b2: %d, b3: %d")

TRC_MSG(UAGPS_UTC_MODEL_IE_HDLER, "[UAGPS]: Uagps_utcModelIeHandler() with measurementIdentity: %d")

TRC_MSG(UAGPS_UTC_MODEL_IE_CONTENT, "[UAGPS]: Assistance data for utc model with a1: %d, a0: %d, tot: %d, wnt: %d, delta_ls: %d, wnlsf: %d, dn: %d, delta_lsf: %d")

TRC_MSG(UAGPS_ALMANAC_IE_HDLER, "[UAGPS]: Uagps_almanacIeHandler() with measurementIdentity: %d")

TRC_MSG(UAGPS_ALMANAC_IE_CONTENT_SAT_NUM, "[UAGPS]: Assistance data for almanac with numElements for SAT: %d")

TRC_MSG(UAGPS_ACQUISITION_ASSISTANCE_IE_HDLER, "[UAGPS]: Uagps_acquisitionAssistanceIeHandler() with measurementIdentity: %d")

TRC_MSG(UAGPS_ACQUISITION_ASSISTANCE_IE_CONTENT_SAT_NUM, "[UAGPS]: Assistance data for acqusition assistance with numElements for SAT: %d")

TRC_MSG(UAGPS_ACQUISITION_ASSISTANCE_IE_CONTENT_EXTRA_DOPPLER_INFO_VALID, "[UAGPS]: Assistance data for acqusition assistance with extraDopplerInfo")

TRC_MSG(UAGPS_ACQUISITION_ASSISTANCE_IE_CONTENT_AZIMUTH_AND_ELEVATION_VALID, "[UAGPS]: Assistance data for acqusition assistance with azimuthAndElevation")

TRC_MSG(UAGPS_REAL_TIME_INTEGRITY_IE_HDLER, "[UAGPS]: Uagps_acquisitionAssistanceIeHandler() with measurementIdentity: %d")

TRC_MSG(UAGPS_REAL_TIME_INTEGRITY_IE_CONTENT_SAT_NUM, "[UAGPS]: Assistance data for real time integrity with numElements for bad SAT: %d")

TRC_MSG(UAGPS_ADDITIONAL_DATA_REF_TIME_RECEIVED, "[UAGPS]: Assistance data for REFERENCE TIME is received")

TRC_MSG(UAGPS_ADDITIONAL_DATA_REF_LOCATION_RECEIVED, "[UAGPS]: Assistance data for REFERENCE LOCATION is received")

TRC_MSG(UAGPS_ADDITIONAL_DATA_DGPS_CORRECTION_RECEIVED, "[UAGPS]: Assistance data for DGPS CORRECTION is received")

TRC_MSG(UAGPS_ADDITIONAL_DATA_NAVIGATION_MODEL_RECEIVED, "[UAGPS]: Assistance data for NAVIGATION MODEL is received")

TRC_MSG(UAGPS_ADDITIONAL_DATA_IONOSPHERIC_MODEL_RECEIVED, "[UAGPS]: Assistance data for IONOSPHERIC MODEL is received")

TRC_MSG(UAGPS_ADDITIONAL_DATA_UTC_MODEL_RECEIVED, "[UAGPS]: Assistance data for UTC MODEL is received")

TRC_MSG(UAGPS_ADDITIONAL_DATA_ALMANAC_RECEIVED, "[UAGPS]: Assistance data for ALMANAC is received")

TRC_MSG(UAGPS_ADDITIONAL_DATA_AQUISITION_ASSISTANCE_RECEIVED, "[UAGPS]: Assistance data for AQUISITION ASSISTANCE is received")

TRC_MSG(UAGPS_ADDITIONAL_DATA_REAL_TIME_INTEGRITY_RECEIVED, "[UAGPS]: Assistance data for REAL TIME INTEGRITY is received")

TRC_MSG(UAGPS_MEASUREMENT_CONTROL_IE_VERSION, "[UAGPS]: IE in MEASUREMENT CONTROL message is version: %MUAGPS_MeasurementControlVersion")

TRC_MSG(UAGPS_MEASUREMENT_CONTROL_IE_TYPE, "[UAGPS]: IE in MEASUREMENT CONTROL message is type: %MUAGPS_MeasurementInternalType")

TRC_MSG(UAGPS_MEASUREMENT_CONTROL_IE_CONTENT, "[UAGPS]: IE in MEASUREMENT CONTROL message with measurementId: %d, positioningMethod: %MRRC_PositioningMethod, assistanceDataValid: %Mkal_bool, transactionId: %d, verticalAccuracyValid: %Mkal_bool")

TRC_MSG(UAGPS_MODIFY_COMMAND_WITHOUT_ASSISTANCE_DATA, "[UAGPS]: Modify command without assistance data")

TRC_MSG(UAGPS_MODIFY_COMMAND_WITHOUT_MEASUREMENT_INFORMATION, "[UAGPS]: Modify command without measurement data")

TRC_MSG(UAGPS_MEASUREMENT_CONTROL_MSG_HDLER, "[UAGPS]: Uagps_measurementControlMsgHandler()")

TRC_MSG(UAGPS_MEASUREMENT_CONTROL_IS_NOT_SUPPORTED_VERSION, "[UAGPS]: IE in MEASUREMENT CONTROL message is not supported version")

TRC_MSG(UAGPS_MEASUREMENT_TYPE_HDLER, "[UAGPS]: Uagps_measurementTypeHandler()")

TRC_MSG(UAGPS_UE_POSITIONING_REPORTING_QUANTITY_IE_HDLER, "[UAGPS]: Uagps_uePositioningReportingQuantityIeHandler()")

TRC_MSG(UAGPS_NEW_MEASUREMENT_REQ_TYPE, "[UAGPS]: Positioning method type: %MRRC_UE_Positioning_MethodType")

TRC_MSG(UAGPS_UE_POSITIONING_REPORTING_QUANTITY_R4_IE_HDLER, "[UAGPS]: Uagps_uePositioningReportingQuantityR4IeHandler()")

TRC_MSG(UAGPS_ASSISTANCE_DATA_DELIVERY_MSG_HDLER, "[UAGPS]: Uagps_assistanceDataDeliveryMsgHandler()")

TRC_MSG(UAGPS_ASSISTANCE_DATA_DELIVERY_WITHOUT_ASSISTANCE_DATA, "[UAGPS]: ASSISTANCE DATA DELIVERY message without assistance data")

TRC_MSG(UAGPS_ASSISTANCE_DATA_DELIVERY_IS_NOT_SUPPORTED_VERSION, "[UAGPS]: IE in ASSISTANCE DATA DELIVERY message is not supported version")

/* !!! should be modified */
END_TRACE_MAP(MOD_UAGPS)
#endif
#endif  /* _UAGPS_TRC_H */
