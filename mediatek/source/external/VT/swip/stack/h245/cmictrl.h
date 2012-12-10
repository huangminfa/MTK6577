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

#ifndef _CMI_CTRL_
#define _CMI_CTRL_

#include "cmControl.h"
#include "cmctrl.h"

#ifdef __cplusplus
extern "C" {
#endif




void clearControl(HCONTROL ctrl, RvBool callInitiator, int controlOffset);
void initControl(HCONTROL ctrl, RvH245ChannelParamsType eParamsType, RvInt32 terminalType);
void startControl(HCONTROL ctrl);

/******************************************************************************
 * freeControl
 * ----------------------------------------------------------------------------
 * General: Clear up any PVT resources used by the control object of a given call,
 *          without sending anything on to the network.
 *          This is used by the stopControl() function and when we want to
 *          switch from a dummy control to a real one in the GK.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  ctrl     - Control object to use.
 * Output: None.
 *****************************************************************************/
void freeControl(IN HCONTROL ctrl);

/************************************************************************
 * stopControl
 * purpose: Stop the H245 Control connection for a call.
 * input  : ctrl    - Control object
 * output : none
 * return : Non-negative value on success
 *          Negative value on failure
 ************************************************************************/
void stopControl(IN HCONTROL ctrl);

/************************************************************************
 * resetControl
 * purpose: Reset the H245 Control connection for a call.
 * input  : ctrl         - Control object
 *          bDropControl - Defines whether the control is totally dropped
 *          and only reset. The function stopControl() should call this function
 *          with RV_TRUE.
 * output : none
 * return : Non-negative value on success
 *          Negative value on failure
 ************************************************************************/
void resetControl(IN HCONTROL ctrl, IN RvBool bDropControl);

/************************************************************************
 * closeChannels
 * purpose: Close any channels of a given control object.
 *          This function only notifies the application about closing the
 *          channels due to disconnection.
 * input  : ctrl    - Control object
 * output : none
 * return : none
 ************************************************************************/
void closeChannels(IN HCONTROL ctrl);



/************************************************************************
 * sendMessageH245
 * purpose: Send H245 message for a given call.
 * input  : hsCall  - Stack handle for the H245 call control
 *          message - root node ID of the message to send.
 *                    This function won't delete this node ID.
 *          bDelete - Defines whether to delete the message nodeId
 *                    inside the function or not.
 * output : none
 * return : RV_OK on success
 *          Other on failure
 ************************************************************************/
RvStatus sendMessageH245(
    IN  HCONTROL     ctrl,
    IN  RvPvtNodeId  message,
    IN  RvBool       bDelete,
    OUT RvBool*      sentOnDummy);


/************************************************************************
 * sendMessageH245Chan
 * purpose: Send H245 message for a given channel.
 * input  : ctrl    - Stack handle for the H245 call control
 *          hsChan  - Channel handle for the H245 channel
 *          message - root node ID of the message to send
 *          bDelete - Defines whether to delete the message nodeId
 *                    inside the function or not.
 * output : none
 * return : RV_OK on success
 *          Other on failure
 ************************************************************************/
RvStatus sendMessageH245Chan(
    IN  HCONTROL     ctrl,
    IN  HCHAN        hsChan,
    IN  RvPvtNodeId  message,
    IN  RvBool       bDelete,
    OUT RvBool*      sentOnDummy);


void h245ProcessIncomingMessage(HCONTROL ctrl, int message);


#if (RV_H245_SUPPORT_H225_PARAMS == RV_YES)
void getGoodAddressForCtrl(HCONTROL ctrl, cmTransportAddress* ta);
void getGoodAddressForCall(HCALL hCall, cmTransportAddress* ta);
#endif /* (RV_H245_SUPPORT_H225_PARAMS == 1) */


#if (RV_H245_LEAN_H223 == RV_NO)
int getQosParameters(IN HPVT hVal,
                     IN int qosElemId,
                     INOUT  cmQosCapability * cmQOSCapability);


int buildQosCapability(IN HPVT hVal,
                       IN int qosId,
                       IN cmRSVPParameters *rsvpParam,
                       IN cmATMParameters * atmParam,
                       IN cmNonStandardParam *nonStandard);
#endif


int cmiReportControl(
    IN HCONTROL             ctrl,
    IN cmControlState       state,
    IN cmControlStateMode   stateMode);


/************************************************************************
 * cmiSetSaveCallIndication
 * purpose: Sets the callSaveIndication if the call not using MPC. 
 * input  : hCtrl               - Stack handle for the H245 call control
 *          saveCallIndication  - Value of to set.
 * output : none
 * return : RV_OK on success, positive values if MPC call,
 *          RV_ERRORs on failure
 ************************************************************************/
RvStatus cmiSetSaveCallIndication(
    IN HCONTROL     hCtrl,
    IN RvBool       saveCallIndication);

/************************************************************************
 * cmiOpenMonaMPCChannels
 * purpose: Opens a H245 channel object for a MPC channel.
 *          This is done to enable sending and receiving of H245 channel 
 *          massages even when only MPC channels are active.
 * input  : ctrl    - Stack handle for the H245 call control
 *          mpcTypes - bitmask of MPC channels to open.
 *          isOutGoing  -   RV_TRUE if an outgoing channel.
 * output : none
 * return : RV_OK on success
 *          Other on failure
 ************************************************************************/
RvStatus cmiOpenMonaMPCChannels(
         IN HCONTROL    ctrl, 
         IN RvUint16    mpcTypes, 
         IN RvBool      isOutGoing);

/************************************************************************
 * cmiRemoveMonaMPCChannel
 * purpose: Remove a MPC channel object.
 * input  : ctrl            - Stack handle for the H245 call control
 *          mpcChannelType   - The MPC channel type.
 *          isOutGoing  -   RV_TRUE if an outgoing channel.
 * output : none
 * return : RV_OK on success
 *          Other on failure
 ************************************************************************/
RvStatus cmiRemoveMonaMPCChannel(
         IN HCONTROL    ctrl, 
         IN RvUint16    mpcChannelType, 
         IN RvBool      isOutGoing);




#ifdef __cplusplus
}
#endif

#endif  /* _CMI_CTRL_ */

