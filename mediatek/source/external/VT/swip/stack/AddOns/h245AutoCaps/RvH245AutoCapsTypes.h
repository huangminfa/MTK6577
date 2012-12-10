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

/***********************************************************************
        Copyright (c) 2002 RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Ltd.. No part of this document may be reproduced in any
form whatsoever without written prior approval by RADVISION Ltd..

RADVISION Ltd. reserve the right to revise this publication and make
changes without obligation to notify any person of such revisions or
changes.
***********************************************************************/

#ifndef _RV_H245_AUTO_CAPS_TYPES_H_
#define _RV_H245_AUTO_CAPS_TYPES_H_


/*-----------------------------------------------------------------------*/
/*                        INCLUDE HEADER FILES                           */
/*-----------------------------------------------------------------------*/

#include "cmH245GeneralDefs.h"
#include "cmctrl.h"
#include "RvGef.h"
#if (RV_H245_SUPPORT_H223_PARAMS == 1)
#include "Rv3G324m.h"
#include "Rv3G324mCall.h"
#else
#include "cm.h"
#endif /* (RV_H245_SUPPORT_H223_PARAMS == 1) */

#ifdef __cplusplus
extern "C" {
#endif

/*-----------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                            */
/*-----------------------------------------------------------------------*/

RV_DECLARE_HANDLE(HH245AUTOCAPS);
RV_DECLARE_HANDLE(HAUTOCAPSCALL);


/* RvH245AutoCapsErrorReason
 * ------------------------------------------------------------------------
 * This enumeration defines the possible error reasons for failure actions
 * of H.245 AutoCaps. */
typedef enum
{
    RvH245AutoCapsErrorReasonUnknown = -1,
    RvH245AutoCapsErrorReasonNone,
    RvH245AutoCapsErrorReasonAudioChannelOnlyCouldBeOpened,
    RvH245AutoCapsErrorReasonVideoChannelOnlyCouldBeOpened,
    RvH245AutoCapsErrorReasonAudioChannelOnlyOrVideoChannelOnlyCouldBeOpened,
    RvH245AutoCapsErrorReasonBadCallState,
    RvH245AutoCapsErrorReasonEmptyTCSReceived
}RvH245AutoCapsErrorReason;

/* RvH245AutoCapsChannelResponse
 * ------------------------------------------------------------------------
 * This enumeration defines the H.245 AutoCaps recommended response to an
 * incoming channel. */
typedef enum
{
    RvH245AutoCapsChannelResponseUnknown = -1,
    RvH245AutoCapsChannelResponseNone,
    RvH245AutoCapsChannelResponseAnswer,
    RvH245AutoCapsChannelResponseReject,
    RvH245AutoCapsChannelResponseNonStandard        /* The incoming channel does not fit the master (local side) preferences
                                                       but the local side has such capability with suitable parameters
                                                       and it fits the simultaneous constraints. The application should decide whether to
                                                       accept or reject the channel. */
}RvH245AutoCapsChannelResponse;


/******************************************************************************
 * RvH245AutoCapsMatchCapability
 * ----------------------------------------------------------------------------
 * General: Callback function called when a matching action between local and
 *          remote capabilities should be performed. It is called only when the 
 *          add-on does not support the capability type.
 *
 * Return Value: RV_OK if matching. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  context              - The context of the application.
 *         hPvt                 - The PVT handle.
 *         localCapNodeId       - The local capability ASN.1 node ID.
 *         remoteCapNodeId      - The remote capability ASN.1 node ID.
 * Output: None.
 *****************************************************************************/
typedef RvStatus (RVCALLCONV * RvH245AutoCapsMatchCapability) (
   IN  void         *context,
   IN  HPVT         hPvt,
   IN  RvPvtNodeId  localCapNodeId,
   IN  RvPvtNodeId  remoteCapNodeId);


/******************************************************************************
 * RvH245AutoCapsGetGCDCapability
 * ----------------------------------------------------------------------------
 * General: Callback function called when a finding GCD should be performed
 *          between local and remote capabilities. It is called only when the
 *          add-on does not support the capability type. 
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  context              - The context of the application.
 *         hPvt                 - The PVT handle.
 *         localCapNodeId       - The local capability ASN.1 node ID.
 *         remoteCapNodeId      - The remote capability ASN.1 node ID.
 * Output: bChanged             - Indication that one of the parameters was
 *                                changed during the GCD process.
 *****************************************************************************/
typedef RvStatus (RVCALLCONV * RvH245AutoCapsGetGCDCapability) (
   IN  void         *context,
   IN  HPVT         hPvt,
   IN  RvPvtNodeId  localCapNodeId,
   IN  RvPvtNodeId  remoteCapNodeId,
   OUT RvBool       *bChanged);



/* RvH245AutoCapsValidateFunctions
 * ------------------------------------------------------------------------
 * This structure defines the validation functions that the application
 * provides to handle capabilities that are not supported by the add-on.
 */
typedef struct
{
    RvH245AutoCapsMatchCapability             pfnMatch;
    RvH245AutoCapsGetGCDCapability            pfnGetGCD;
} RvH245AutoCapsValidateFunctions;


/******************************************************************************
 * RvH245AutoCapsGetNewChannelEv
 * ----------------------------------------------------------------------------
 * General: This callback function is invoked when a new incoming TCS arrives
 *          with capabilities that no longer fit. In this case the logical
 *          channel whose capability does not fit will be closed and a new logical
 *          will be opened. Therefore, this function is used to provide the add-on
 *          with the new HCHAN object.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hAutoCapsCall     - The handle to the H.245 AutoCaps call object.
 *                             The application must set all the information 
 *                             needed for the validation to this handle.
 *         context           - The context of the application.
 *         eChannelType      - The channel type.
 * Output: hChan             - The new HCHAN object provided by the application.
 *                             If the application decides not to provide the
 *                             handle, it should set hChan to NULL.
 *****************************************************************************/
typedef RvStatus (RVCALLCONV * RvH245AutoCapsGetNewChannelEv)(
    IN   HAUTOCAPSCALL             hAutoCapsCall,
    IN   void                      *context,
    IN   cmChannelDataType         eChannelType,
    OUT  HCHAN                     *hChan);

/******************************************************************************
 * RvH245AutoCapsUpdateCapabilityInformationEv
 * ----------------------------------------------------------------------------
 * General: This callback function is invoked when a new channel is about
 *          to be opened. The application shoulc check the chosen capability
 *          and complete it so it can be sent in an OpenLogicalChannel message.
 *          For example, for MPEG4 capability that is a genericVideoCapability,
 *          not all information is advertised in the TCS message and some parameters
 *          (like decoderConfigurationInformation) must be added. The application
 *          can add these parameters in this callback.
 *          Note that if the value of bMimiced parameter is RV_TRUE, the
 *          dataType was copied from the remote OLC message. This is done because
 *          the remote side has rejected the previous OLC. Parameters such as
 *          decoderConfigurationInformation in MPEG4 that are specific
 *          to the local side should still be replaced in this callback.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hAutoCapsCall     - The handle to the H.245 AutoCaps call object.
 *                             The application must set all information needed 
 *                             for the validation to this handle.
 *         context           - The context of the application.
 *         hChan             - The new HCHAN to be opened.
 *         eChannelType      - The channel type.
 *         bMimicked         - The forward dataType was mimicked, meaning 
 *                             that it was copied from the remote dataType.
 *         dataTypeNodeId    - The dataType node ID to update.
 * Output: dataTypeNodeId    - The dataType node ID to update.
 *****************************************************************************/
typedef RvStatus (RVCALLCONV * RvH245AutoCapsUpdateCapabilityInformationEv)(
    IN    HAUTOCAPSCALL             hAutoCapsCall,
    IN    void                      *context,
    IN    HCHAN                     hChan,
    IN    cmChannelDataType         eChannelType,
    IN    RvBool                    bMimicked,
    IN    cmCapStruct*                 remoteCap,
    INOUT RvPvtNodeId               dataTypeNodeId);


/* RvH245AutoCapsEvHandlers
 * ------------------------------------------------------------------------
 * Structure with function pointers to the H.245 AutoCaps callbacks.
 * This structure is used to set the application callbacks in the function
 * RvH245AutoCapsSetEvHandlers();
 */
typedef struct
{
    RvH245AutoCapsGetNewChannelEv                pfnGetNewChannelEv;
    RvH245AutoCapsUpdateCapabilityInformationEv  pfnUpdateCapInfoEv;
}RvH245AutoCapsEvHandlers;


#ifdef __cplusplus
}
#endif

#endif /* _RV_H245_AUTO_CAPS_TYPES_H_ */
