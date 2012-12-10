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

#include "cmH245GeneralDefs.h"
#if (RV_H245_SUPPORT_H225_PARAMS == 1)
#include "rv_defs.h"

#ifdef __cplusplus
extern "C" {
#endif

#ifndef _FAST_START_H
#define _FAST_START_H


#define MAX_ALTERNATIVE_CHANNEL 10
#define MAX_FS_CHANNELS 3 /* restricted by well known sessionId 1 for audio,2 for video, 3 for data*/

typedef struct
{
  cmTransportAddress rtp; /* not used for outgoing (transmit) channels */
  cmTransportAddress rtcp;
  RvPvtNodeId dataTypeHandle;
  RvChar *channelName;
  int index;
}cmFastStartChannel;


typedef enum
{
    dirReceive,
    dirTransmit,
    dirBoth
} cmChannelDirection;


typedef struct
{
  cmFastStartChannel channels[MAX_ALTERNATIVE_CHANNEL];
  int altChannelNumber;
}cmAlternativeChannels; /* alternative proposes channel list.
                      Only one channel must be chose by called endpoint.*/


typedef struct
{
  cmCapDataType type;
  cmAlternativeChannels transmit;
  cmAlternativeChannels receive;
}cmPartnerChannels;

typedef struct
{
  cmPartnerChannels partnerChannels[MAX_FS_CHANNELS];
  int partnerChannelsNum;

}cmFastStartMessage;


/******************************************************************************
 * cmFastStartChannelsAckIndex
 * ----------------------------------------------------------------------------
 * General: Builds and adds an ACK-OLC message to the given channel index.
 *
 * Return Value: Non-negative value on success, Negative value on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * input : hsCall      - Stack handle for the call
 *         index       - Index of the FS offer to acknowledge
 *         rtcp        - Pointer to the RTCP address for the OLC-ACK
 *         rtp         - Pointer to the RTP address for the OLC-ACK
 * Output: None.
 *****************************************************************************/
RVAPI int RVCALLCONV
cmFastStartChannelsAckIndex(HCALL hsCall,int index,cmTransportAddress *rtcp,cmTransportAddress *rtp);


/******************************************************************************
 * cmFastStartChannelsAck
 * ----------------------------------------------------------------------------
 * General: Builds and adds an ACK-OLC message to the given channel structure.
 *
 * Return Value: Non-negative value on success, Negative value on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * input : hsCall       - Stack handle for the call
 *         pFastChannel - Pointer to the FS channel structure to acknowledge
 * Output: None.
 *****************************************************************************/
RVAPI int RVCALLCONV
cmFastStartChannelsAck(
    IN HCALL hsCall,
    IN  cmFastStartChannel *pFastChannel);


/******************************************************************************************
 * cmFastStartOpenChannels
 *
 * Purpose:  This API function enables the caller to supply a structure with data about
 *           the offered logical channels for fast start procedure. The structure includes
 *           all offered channels, both incoming and outgoing, arranged by their type, i.e.
 *           Audio channels, Video channels, etc.
 *
 * Input:    hsCall - A handle to the call whose setup message shall carry the
 *                    fast start offer.
 *
 *           fsMessage - A pointer to the structure containing the channels data.
 *
 * Reurned Value: RV_TRUE or negative value on failure.
 *
 ****************************************************************************************/
RVAPI int RVCALLCONV
cmFastStartOpenChannels(IN HCALL hsCall, IN cmFastStartMessage* fsMessage);


/******************************************************************************
 * cmFastStartChannelsReady
 * ----------------------------------------------------------------------------
 * General: Indicate to the stack that the channel acknowledgements are all
 *          done, and that the OLC-ACK messages can be sent.
 *
 * Return Value: Non-negative value on success, Negative value on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * input : hsCall      - Stack handle for the call
 * Output: None.
 *****************************************************************************/
RVAPI int RVCALLCONV
cmFastStartChannelsReady(IN HCALL hsCall);


/******************************************************************************
 * cmFastStartChannelsRefuse
 * ----------------------------------------------------------------------------
 * General: Indicate to the stack that we do not want any of the offered
 *          channels
 *
 * Return Value: Non-negative value on success, Negative value on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * input : hsCall      - Stack handle for the call
 * Output: None.
 *****************************************************************************/
RVAPI int RVCALLCONV
cmFastStartChannelsRefuse(IN HCALL hsCall);




/************************************************************************
 *
 *          FASTSTART API: Supporting more than 10 channels
 *
 ************************************************************************/


/************************************************************************
 * cmFastStartBuild
 * purpose: Build a single OpenLogicalChannel message for use in fast start
 *          proposals
 * input  : hsCall      - Stack handle for the call
 *          sessionId   - Session id of the proposed channel
 *          direction   - Direction of the proposed channel
 *          fsChannel   - FastStart channel information
 * return : Node ID of created OpenLogicalChannel on success
 *          Negative value on failure
 ************************************************************************/
RVAPI int RVCALLCONV
cmFastStartBuild(
                 IN HCALL                hsCall,
                 IN int                  sessionId,
                 IN cmChannelDirection   direction,
                 IN cmFastStartChannel*  fsChannel);


/************************************************************************
 * cmExtFastStartBuildReconf
 * purpose: Build a single OpenLogicalChannelAck message for use in fast
 *          start reconfiguration, rerouting or suspending
 * input  : hsCall      - Stack handle for the call
 *          sessionId   - Session id of the proposed channel
 *          direction   - Direction of the proposed channel
 *          fsChannel   - FastStart channel information
 * return : Node ID of created OpenLogicalChannel on success
 *          Negative value on failure
 ************************************************************************/
RVAPI int RVCALLCONV
cmExtFastStartBuildAck(
    IN HCALL                hsCall,
    IN int                  sessionId,
    IN cmChannelDirection   direction,
    IN cmFastStartChannel*  fsChannel);


/************************************************************************
 * cmCallAddFastStartMessage
 * purpose: Add an OpenLogicalChannel message to the fast start proposal
 *          on the origin side of the call.
 * input  : hsCall      - Stack handle for the call
 *          fsMessageId - Node ID of the OpenLogicalChannel to add
 * return : Non-negative value on success
 *          Negative value on failure
 ************************************************************************/
RVAPI int RVCALLCONV
cmCallAddFastStartMessage(IN HCALL hsCall, IN int fsMessageId);


/******************************************************************************
 * cmCallAddExtFastConnectApproval
 * ----------------------------------------------------------------------------
 * General: Add an OpenLogicalChannel message to the fast start approval.
 *          used to send pause and reconfigure when using EFC.
 *
 * Return Value: Non-negative value on success, Negative value on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * input : hsCall      - Stack handle for the call
 *         fsMessageId - Node ID of the OpenLogicalChannel to add
 * Output: None.
 *****************************************************************************/
RVAPI int RVCALLCONV
cmCallAddExtFastConnectApproval(IN HCALL hsCall, IN int fsMessageId);


/************************************************************************
 * cmFastStartGetByIndex
 * purpose: Get faststart information of a single OpenLogicalChannel
 *          message from the faststart proposal string
 * input  : hsCall  - Stack handle for the call
 *          index   - Index of the faststart proposal to get (0-based)
 * return : Node ID of the OpenLogicalChannel message proposal on success
 *          Negative value on failure
 ************************************************************************/
RVAPI int RVCALLCONV
cmFastStartGetByIndex(IN HCALL hsCall, IN int index);


/************************************************************************
 * cmFastStartGet
 * purpose: Get faststart information of a single OpenLogicalChannel
 *          message from the faststart proposal string
 * input  : hsCall      - Stack handle for the call
 *          fsChannelId - PVT node ID of the root of the faststart proposal
 *                        of one of the channels
 * output : sessionId   - Session ID of the proposed channel
 *          direction   - Direction of the proposed channel
 *          fsChannel   - FastStart channel information
 * return : Non-negative value on success
 *          Negative value on failure
 ************************************************************************/
RVAPI int RVCALLCONV
cmFastStartGet(
        IN  HCALL               hsCall,
        IN  int                 fsChannelId,
        OUT int*                sessionId,
        OUT cmChannelDirection* direction,
        OUT cmFastStartChannel* fsChannel);


/************************************************************************
 * cmCallFastStartOpenChannels
 * purpose: Set the answered information to be sent for a fast start
 *          proposal and open the channels on the destination side of the
 *          call. This function should be called after receiving
 *          cmEvCallFastStart() or cmEvCallFastStartSetup()
 * input  : hsCall      - Call to use
 *          proposed    - List of proposed channels. This list is searched for
 *                        their RTP and RTCP addresses.
 *                        Each parameter in this list is a root node of an
 *                        OpenLogicalChannel message to propose in faststart
 *                        The last element in this list should be a negative value.
 *          accepted    - List of accepted channels.
 *                        Each paramenter in this list is a root node of an
 *                        OpenLogicalChannel message sent to the origin of the call
 *                        by this endpoint.
 *                        The last element in this list should be a negative value.
 *          origin      - RV_TRUE if this application is the origin of the call
 *                        RV_FALSE if this application is not the origin of the call
 * output : none
 * return : non-negative value on success
 *          negative value on failure
 ************************************************************************/
RVAPI int RVCALLCONV
cmCallFastStartOpenChannels(
    IN HCALL    hsCall,
    IN int*     proposed,
    IN int*     accepted,
    IN RvBool   origin);


/******************************************************************************
 * cmCallExtFastConnectCloseAllChannels
 * ----------------------------------------------------------------------------
 * General: Close all the channels on a call.
 *
 * Return Value: Non-negative value on success, Negative value on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * input : hsCall      - Stack handle for the call
 * Output: None.
 *****************************************************************************/
RVAPI int RVCALLCONV
cmCallExtFastConnectCloseAllChannels(
    IN HCALL    hsCall);


#endif  /* _FAST_START_H */

#ifdef __cplusplus
}
#endif

#endif /* #if (RV_H245_SUPPORT_H225_PARAMS == 1) */


