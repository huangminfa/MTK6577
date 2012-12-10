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

#ifndef _RV_CM_CHAN_H
#define _RV_CM_CHAN_H

#include "ema.h"
#include "cmConf.h"
#include "cmControl.h"
#include "cmH245Object.h"
#include "cmH245Mib.h"
#include "cmH245.h"
//Huibin Add For MONA BEGIN
#include "rvtypes.h"
#include "cmH245GeneralDefs.h"
#include "cmctrl.h"
#include "psyntree.h"
//Huibin Add For MONA END
#ifdef __cplusplus
extern "C" {
#endif


int openLogicalChannel(H245Control* ctrl, int base, int message);
int openLogicalChannelAck(H245Control* ctrl, int base, int message);
int openLogicalChannelConfirm(H245Control* ctrl, int base, int message);
int openLogicalChannelReject(H245Control* ctrl, int base, int message);
int closeLogicalChannel(H245Control* ctrl, int base, int message);
int closeLogicalChannelAck(H245Control* ctrl, int base, int message);
int flowH245ControlCommand(H245Control* ctrl, int base, int message);
int requestChannelClose(H245Control* ctrl, int base, int message);
int requestChannelCloseAck(H245Control* ctrl, int base, int message);
int requestChannelCloseReject(H245Control* ctrl, int base, int message);
int requestChannelCloseRelease(H245Control* ctrl, int base, int message);
int maintenanceLoopRequest(H245Control* ctrl, int base, int message);
int maintenanceLoopAck(H245Control* ctrl, int base, int message);
int maintenanceLoopReject(H245Control* ctrl, int base, int message);
int maintenanceLoopOffCommand(H245Control* ctrl, int message);

H245Channel*allocateChannel(HCONTROL  ctrl);
void deriveChannels(HCONTROL ctrl);
void closeChannels(HCONTROL ctrl);


/******************************************************************************
 * incomingChannelMessage
 * ----------------------------------------------------------------------------
 * General: Make sure cmEvChannelRecvMessage() is invoked in necessary on
 *          incoming channel related messages.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  app      - H.245 instance used
 *         channel  - Channel this message belongs to
 *         message  - Message received
 * Output: None.
 *****************************************************************************/
void incomingChannelMessage(
    IN H245Object*      app,
    IN H245Channel*     channel,
    IN int              message);


/******************************************************************************
 * outgoingChannelMessage
 * ----------------------------------------------------------------------------
 * General: Make sure cmEvChannelSendMessage() is invoked in necessary on
 *          outgoing channel related messages.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  app      - H.245 instance used.
 *         channel  - Channel this message belongs to.
 *         message  - Message to send.
 * Output: None.
 *****************************************************************************/
void outgoingChannelMessage(
    IN H245Object*      app,
    IN H245Channel*     channel,
    IN int              message);


HEMA H245InitChannels(
    IN HH245        hH245,
    IN HAPP         hApp,
    IN int          maxCalls,
    IN int          maxChannels);

void H245EndChannels(IN HEMA channels);

int
cmcCallDataTypeHandleCallback(
                  /* Call the data type handle callback */
                  IN  HH245 hApp,
                  IN  HCHAN hsChan, /* channel protocol */
                  IN  int dataType, /* channel data type node id */
                  IN  confDataType type
                  );

int
cmcCallChannelParametersCallback(
                 /* Call the channel parameter callback */
                 IN  HH245 hApp,
                 IN  HCHAN hsChan, /* channel protocol */
                 IN  int dataType, /* channel data type node id */
                 OUT confDataType* type
                 );


/************************************************************************
 * startEstablishment
 * purpose: Start establishing a channel. This function creates an OLC
 *          message from the channel element and sends it.
 * input  : app     - Stack object
 *          channel - H245Channel element to send OLC for
 * output : none
 * return : Non-negative value on success
 *          Negative value on failure
 ************************************************************************/
int startEstablishment(IN HH245 hApp, IN HCHAN hChan);


/************************************************************************
 * chanGetMibParam
 * purpose: Get channel related MIB parameters
 * input  : hsChan      - Channel to check
 *          type        - Parameter type to get
 * output : valueSize   - Value, if numeric
 *                        String's length if string value type
 *          value       - String value if applicable
 * return : Non-negative value on success
 *          Negative value on failure
 ************************************************************************/
int chanGetMibParam(
    IN  HCHAN                   hsChan,
    IN  mibControlParamTypeT    type,
    OUT int*                    valueSize,
    OUT RvUint8*                value);


/************************************************************************
 * notifyChannelState
 * purpose: Notify the application about the state of a channel
 * input  : channel     - H245Channel object
 *          state       - State of channel
 *          stateMode   - State mode of channel
 * output : none
 * return : none
 ************************************************************************/
int notifyChannelState(
    IN H245Channel*         channel,
    IN cmChannelState_e     state,
    IN cmChannelStateMode_e stateMode);


int rejectChannel(H245Channel* channel, RvPstFieldId rejectReason, cmChannelStateMode_e rejectMode);
//Huibin  Add For MONA
int channelFreeMemory(
           IN   HCHAN       hsChan);

#ifdef __cplusplus
}
#endif

#endif  /* _RV_CM_CHAN_H */


