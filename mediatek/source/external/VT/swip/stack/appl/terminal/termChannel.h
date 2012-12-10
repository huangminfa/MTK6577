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
Copyright (c) 2003 RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Ltd.. No part of this document may be reproduced in any
form whatsoever without written prior approval by RADVISION Ltd..
RADVISION Ltd. reserve the right to revise this publication and make
changes without obligation to notify any person of such revisions or
changes.
***********************************************************************/

#ifndef _RV_TERM_CHANNEL_H_
#define _RV_TERM_CHANNEL_H_

/***********************************************************************
termChannel.h

Channel handling of the terminal.
***********************************************************************/


/*-----------------------------------------------------------------------*/
/*                        INCLUDE HEADER FILES                           */
/*-----------------------------------------------------------------------*/
#include "termDefs.h"




#ifdef __cplusplus
extern "C" {
#endif



/*-----------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                            */
/*-----------------------------------------------------------------------*/




/*-----------------------------------------------------------------------*/
/*                           CALLBACK HEADERS                            */
/*-----------------------------------------------------------------------*/


void RVCALLCONV CallLogicalChannelReleaseSendBufferEv(
    IN  HAPPCALL                h3G324mAppCall,
    IN  HCALL                   h3G324mCall,
    IN  HAPPCHAN                h3G324mAppChan,
    IN  HCHAN                   h3G324mChan,
    IN  RvUint8                 *pBuffer,
    IN  RvUint32                size);

void RVCALLCONV CallMonaMPCChannelReleaseSendBufferEv(
    IN  HAPPCALL                h3G324mAppCall,
    IN  HCALL                   h3G324mCall,
    IN  Rv3G324mCallMonaMPCType mpcChannel,
    IN  RvUint8                 *pBuffer,
    IN  RvUint32                size);

int RVCALLCONV cmEvCallNewChannel(
    IN  HAPPCALL   haCall,
    IN  HCALL      hsCall,
    IN  HCHAN      hsChan,
    OUT LPHAPPCHAN lphaChan);
int RVCALLCONV cmEvChannelStateChanged(
    IN HAPPCHAN			    haChan,
    IN HCHAN			    hsChan,
    IN cmChannelState_e     state,
    IN cmChannelStateMode_e stateMode);
int RVCALLCONV cmEvChannelNewRate(
    IN      HAPPCHAN            haChan,
    IN      HCHAN               hsChan,
    IN      RvUint32            rate);
int RVCALLCONV cmEvChannelMaxSkew(
    IN      HAPPCHAN            haChan1,
    IN      HCHAN               hsChan1,
    IN      HAPPCHAN            haChan2,
    IN      HCHAN               hsChan2,
    IN      RvUint32            skew);
int RVCALLCONV cmEvChannelParameters(
    IN      HAPPCHAN            haChan,
    IN      HCHAN               hsChan,
    IN      char*               channelName,
    IN      HAPPCHAN            haChanSameSession,
    IN      HCHAN               hsChanSameSession,
    IN      HAPPCHAN            haChanAssociated,
    IN      HCHAN               hsChanAssociated,
    IN      RvUint32            rate);
int RVCALLCONV cmEvChannelVideoFastUpdatePicture(
    IN      HAPPCHAN            haChan,
    IN      HCHAN               hsChan);
int RVCALLCONV cmEvChannelVideoFastUpdateGOB(
    IN      HAPPCHAN            haChan,
    IN      HCHAN               hsChan,
    IN      int                 firstGOB,
    IN      int                 numberOfGOBs);
int RVCALLCONV cmEvChannelVideoFastUpdateMB(
    IN      HAPPCHAN            haChan,
    IN      HCHAN               hsChan,
    IN      int                 firstGOB,
    IN      int                 firstMB,
    IN      int                 numberOfMBs);
int RVCALLCONV cmEvChannelHandle(
    IN      HAPPCHAN            haChan,
    IN      HCHAN               hsChan,
    IN      int                 dataTypeHandle,
    IN      cmCapDataType       dataType);
int RVCALLCONV cmEvChannelRequestCloseStatus(
    IN      HAPPCHAN              haChan,
    IN      HCHAN                 hsChan,
    IN      cmRequestCloseStatus  status);
int RVCALLCONV cmEvChannelTSTO(
    IN      HAPPCHAN              haChan,
    IN      HCHAN                 hsChan,
    IN      RvInt8                isCommand,
    IN      RvInt8                tradeoffValue);
int RVCALLCONV cmEvChannelMediaLoopStatus(
    IN      HAPPCHAN              haChan,
    IN      HCHAN                 hsChan,
    IN      cmMediaLoopStatus     status);
int RVCALLCONV cmEvChannelReplace(
     IN     HAPPCHAN              haChan,
     IN     HCHAN                 hsChan,
     IN     HAPPCHAN              haReplacedChannel,
     IN     HCHAN                 hsReplacedChannel);
int RVCALLCONV cmEvChannelMiscCommand(
     IN      HAPPCHAN             haChan,
     IN      HCHAN                hsChan,
     IN      cmMiscellaneousCommand miscCommand);
int RVCALLCONV cmEvChannelTransportCapInd(
     IN      HAPPCHAN             haChan,
     IN      HCHAN                hsChan,
     IN      int                  nodeId);
int RVCALLCONV cmEvChannelRecvMessage(
    IN      HAPPCHAN            haChan,
    IN      HCHAN               hsChan,
    IN      RvPvtNodeId         message);
int RVCALLCONV cmEvChannelSendMessage(
    IN      HAPPCHAN            haChan,
    IN      HCHAN               hsChan,
    IN      RvPvtNodeId         message);
int RVCALLCONV cmEvChannelMasterSlaveConflict(
    IN      HAPPCHAN                    haChan,
    IN      HCHAN                       hsChan,
    IN      cmH245ChannelConflictType   conflictType,
    IN      HCHAN                      *confChans,
    IN      int                         numConfChans,
    IN      RvPvtNodeId                 message);
void RVCALLCONV cmEvChannelSetRole(
    IN      HAPPCHAN                    haChan,
    IN      HCHAN                       hsChan,
    IN      RvPvtNodeId                 dataType,
    OUT     RvUint16*                   channelRole);
void RVCALLCONV cmEvMPCChannelClose(
    IN      HAPPCHAN                    haChan,
    IN      HCHAN                          hsChan,
    IN      RvInt32                         lcn,
    OUT   RvBool                            *bIsReadyToFree);

/******************************************************************************
 * CallSetMonaMPCChannelEv
 * ----------------------------------------------------------------------------
 * General: Report channel status to MED 
 * 
 * Arguments:
 * Input:  h3G324mAppCall       - Application's call handle of the call.
 *         h3G324mCall          - The call handle.
 *         
 * Output: None.
 *
 * Return Value: RV_OK on success. Other values on failure.
 *****************************************************************************/
void RVCALLCONV CallSetMonaMPCChannelEv(
    IN  HAPPCALL                h3G324mAppCall,
    IN  HCALL                   h3G324mCall,
    IN  RvUint16                supportedRxMPCChannels,
    IN  RvUint16                supportedTxMPCChannels);

/******************************************************************************
 * CallRemoveMonaMPCChannelEv
 * ----------------------------------------------------------------------------
 * General: Report channel status to MED
 * 
 * Arguments:
 * Input:  h3G324mAppCall   - Application's call handle of the call.
 *            h3G324mCall        - The call handle.
 *            mpcChannelType   - The channel that is about to close (Only one at a time)
 *            bIsOutgoing           - TRUE if it is an outgoing channel
 *
 * Output: None.
 *
 * Return Value: RV_OK on success. Other values on failure.
 *****************************************************************************/
void RVCALLCONV CallRemoveMonaMPCChannelEv (
    IN  HAPPCALL                h3G324mAppCall,
    IN  HCALL                   h3G324mCall,
    IN  RvUint16                 mpcChannelType,
    IN  RvBool                  bIsOutgoing);
/*-----------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                           */
/*-----------------------------------------------------------------------*/


/******************************************************************************
 * termChannelCreate
 * ----------------------------------------------------------------------------
 * General: Create a new channel object to use later on.
 *
 * Return Value: Channel object created.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term                 - Terminal object to use.
 *         call                 - Call this channel belongs to.
 *         id                   - Id to give to the new channel.
 * Output: None.
 *****************************************************************************/
TermChannelObj *termChannelCreate(
    IN TermObj      *term,
    IN TermCallObj  *call,
    IN RvInt32      id);

/******************************************************************************
 * termChannelClose
 * ----------------------------------------------------------------------------
 * General: Close a channel object.
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term                 - Terminal object to use.
 *         call                 - Call this channel belongs to.
 *         chan                 - Channel to be closed.
 * Output: None.
 *****************************************************************************/
void termChannelClose(
    IN TermObj         *term,
    IN TermCallObj     *call,
    IN TermChannelObj  *chan);


/******************************************************************************
 * termChannelGetExtraData
 * ----------------------------------------------------------------------------
 * General: Get the extra data of the channel object.
 *
 * Return Value: Pointer to the extra data of the channel.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel  - Channel object to use.
 * Output: None.
 *****************************************************************************/
void *termChannelGetExtraData(
    IN TermChannelObj       *channel);


/******************************************************************************
 * termChannelOpen
 * ----------------------------------------------------------------------------
 * General: Open a new outgoing channel.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel              - Channel object to open.
 *         dataTypeName         - Name of data type to use for this channel.
 *         sameSessionChannel   - Incoming channel with the same session Id.
 *                                Can be NULL.
 *         replacementChannel   - Outgoing channel this one replaces.
 *                                Can be NULL.
 *         alParams             - Adaptation layer configuration.
 *                                Can be one of:
 *                                - AL1 Framed
 *                                - AL1 UnFrames
 *                                - AL2 WithSequenceNumber
 *                                - AL2 WithoutSequenceNumber
 *                                - AL3 ControlFieldSize0
 *                                - AL3 ControlFieldSize1
 *                                - AL3 ControlFieldSize2
 *         frameSize            - Frame size/rate to use for the channel.
 * Output: None.
 *****************************************************************************/
RvStatus termChannelOpen(
    IN TermChannelObj   *channel,
    IN const RvChar     *dataTypeName,
    IN TermChannelObj   *sameSessionChannel,
    IN TermChannelObj   *replacementChannel,
    IN const RvChar     *alParams,
    IN RvInt32          frameSize);


/******************************************************************************
 * termChannelRespond
 * ----------------------------------------------------------------------------
 * General: Send a response for an incoming open logical channel
 *          or to an outgoing channel's requestChannelClose message.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel  - Incoming or outgoing channel to respond to.
 *         confirm  - RV_TRUE to confirm, RV_FALSE to reject.
 *         reason   - Reason to use for rejection.
 * Output: None.
 *****************************************************************************/
RvStatus termChannelRespond(
    IN TermChannelObj       *channel,
    IN RvBool               confirm,
    IN cmRejectLcnReason    reason);


RvStatus termMPCChannelDrop(
    IN TermChannelObj   *chan,
    IN cmCloseLcnReason dropReason,
    IN RvBool isOutgoing);

/* For Catcher Test only */
RvStatus termChannelDropNonMPCChannels(
    IN TermCallObj* call,
    IN RvInt32 chanType);



/******************************************************************************
 * termChannelDrop
 * ----------------------------------------------------------------------------
 * General: Drop a channel. This supports both incoming and outgoing channels.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  chan                 - Channel to drop.
 *         dropReason           - Reason for channel dropping.
 * Output: None.
 *****************************************************************************/
RvStatus termChannelDrop(
    IN TermChannelObj   *chan,
    IN cmCloseLcnReason dropReason);


/******************************************************************************
 * termChannelMediaLoopRequest
 * ----------------------------------------------------------------------------
 * General: Request a media loop on a channel.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel  - Channel object to use.
 * Output: None.
 *****************************************************************************/
RvStatus termChannelMediaLoopRequest(
    IN TermChannelObj       *channel);


/******************************************************************************
 * termChannelSetRate
 * ----------------------------------------------------------------------------
 * General: Set the rate of a channel.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel  - Channel object to use.
 *         rate     - Rate to use.
 * Output: None.
 *****************************************************************************/
RvStatus termChannelSetRate(
    IN TermChannelObj       *channel,
    IN RvUint32             rate);


/******************************************************************************
 * termChannelVideoFastUpdate
 * ----------------------------------------------------------------------------
 * General: Request for a fast update of the video channel.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel  - Channel object to use.
 * Output: None.
 *****************************************************************************/
RvStatus termChannelVideoFastUpdate(
    IN TermChannelObj       *channel);

/******************************************************************************
 * New Feature: 2009/03/03
 * termChannelVideoTSTO
 * ----------------------------------------------------------------------------
 * General: Request for TSTO of the video channel.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel  - Channel object to use.
 *            tradeoffvalue 
 * Output: None.
 *****************************************************************************/
 RvStatus termChannelVideoTSTO(
    IN TermChannelObj       *channel,
    IN RvInt32                  tradeoffvalue);

/******************************************************************************
 * termChannelActivate
 * ----------------------------------------------------------------------------
 * General: Indicate to the remote side that a channel has been activated or
 *          inactivated.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel  - Channel object to use.
 *         isActive - Is this channel active at the moment?
 * Output: None.
 *****************************************************************************/
RvStatus termChannelActivate(
    IN TermChannelObj       *channel,
    IN RvBool               isActive);


/******************************************************************************
 * termChannelMimic
 * ----------------------------------------------------------------------------
 * General: Mimic an incoming channel.
 *
 * Return Value: Channel object created.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  incomingChannel      - Incoming channel to mimic.
 *         id                   - Id to assign to the new outgoing channel.
 *                                0 means it will be allocated by the usual
 *                                resource id callback.
 * Output: None.
 *****************************************************************************/
TermChannelObj *termChannelMimic(
    IN TermChannelObj   *incomingChannel,
    IN RvInt32          id);


/******************************************************************************
 * termChannelSetMediaData
 * ----------------------------------------------------------------------------
 * General: Set a media file from a buffer in memory that will be sent on
 *          the given channel.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel          - Channel object to use.
 *         pBuf             - Media buffer to send.
 *         bufSize          - Size of the media buffer to send.
 *         bInstructions    - RV_TRUE for instructions file. RV_FALSE for
 *                            regular rates.
 *         bCyclic          - RV_TRUE to cycle the file all over again when
 *                            we're done sending it.
 * Output: None.
 *****************************************************************************/
RvStatus termChannelSetMediaData(
    IN TermChannelObj   *channel,
    IN RvUint8          *pBuf,
    IN RvSize_t         bufSize,
    IN RvBool           bInstructions,
    IN RvBool           bCyclic);

/******************************************************************************
 * termMonaMPCChannelSetMediaData
 * ----------------------------------------------------------------------------
 * General: Set a media file from a buffer in memory that will be sent on
 *          the given MPC channel.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  mpcChannel            - Channel object to use.
 *         pBuf                 - Media buffer to send.
 *         bufSize              - Size of the media buffer to send.
 *         bInstructions        - RV_TRUE for instructions file. RV_FALSE for
 *                                regular rates.
 *         bCyclic              - RV_TRUE to cycle the file all over again when
 *                                we're done sending it.
 * Output: None.
 *****************************************************************************/
RvStatus termMonaMPCChannelSetMediaData(
    IN TermChannelObj       *mpcChannel,
    IN RvUint8              *pBuf,
    IN RvSize_t             bufSize,
    IN RvBool               bInstructions,
    IN RvBool               bCyclic);

/******************************************************************************
 * termChannelCatchMediaData
 * ----------------------------------------------------------------------------
 * General: Indicate how the terminal should handle incoming media frames
 *          received on the given channel.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel          - Channel object to use.
 *         context          - Context to use for the indication function.
 *                            This value must be other than NULL.
 * Output: None.
 *****************************************************************************/
RvStatus termChannelCatchMediaData(
    IN TermChannelObj           *channel,
    IN void                     *context);

/******************************************************************************
 * termMonaMPCChannelCatchMediaData
 * ----------------------------------------------------------------------------
 * General: Indicate how the terminal should handle incoming media frames
 *          received on the given channel.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  mpcChannel          - MPC channel object to use.
 *         context            - Context to use for the indication function.
 *                              This value must be other than NULL.
 * Output: None.
 *****************************************************************************/
RvStatus termMonaMPCChannelCatchMediaData(
    IN TermChannelObj              *mpcChannel,
    IN void                       *context);

/******************************************************************************
 * termChannelAutoOpenAll
 * ----------------------------------------------------------------------------
 * General: Automatically open channels by the configuration on connected
 *          calls.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call     - Call to use.
 * Output: None.
 *****************************************************************************/
RvStatus termChannelAutoOpenAll(
    IN TermCallObj *call);


/******************************************************************************
 * termChannelGetById
 * ----------------------------------------------------------------------------
 * General: Find a channel object by its id.
 *
 * Return Value: The channel object if found, NULL otherwise.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term     - Terminal object to use.
 *         id       - Id of the channel to look for.
 * Output: None.
 *****************************************************************************/
TermChannelObj *termChannelGetById(
    IN TermObj  *term,
    IN RvInt32  id);


/******************************************************************************
 * termChannelLink
 * ----------------------------------------------------------------------------
 * General: Link a channel to the parallel remote channel no matter
 *          if it is outgoing or incoming channel.
 *
 * Return Value: RvStatus.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  linkedChan      - Channel to be linked.
 * Output: None.
 *****************************************************************************/
RvStatus termChannelLink(
    IN TermChannelObj   *linkedChan);

#ifdef USE_HANDSET
/******************************************************************************
 * termChannelSend
 * ----------------------------------------------------------------------------
 * General: Sends data on the channel
 *
 * Return Value: RvStatus.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel      - Channel used to for send.
 *         data         - data to be sent
 *         size         - size of above data
 * Output: None.
 *****************************************************************************/
void termChannelSend(IN TermChannelObj *channel,
                         IN RvUint8        *data,
                         IN RvUint32       size);

#endif /* USE_HANDSET */
/******************************************************************************
 * channelGetDataTypeName
 * ----------------------------------------------------------------------------
 * General: Update the dataTypeName and dataTypeNode.
 *
 * Return Value: The created call object or NULL on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term                - Terminal information object. 
 *         chan                - Channel information object.
 * Output: None.
 *****************************************************************************/
void termChannelGetDataTypeName(
    IN  TermObj        *term,
    IN  TermChannelObj *chan);


void vt_med_report_chl_para(void);

void vt_report_outgoing_acp_chl_cap(
    IN HAPP                                 hApp,
    IN HCHAN                               hChan,
    IN HCALL                                hsCall,
    IN cmChannelDataType           dataType);

void vt_set_mtk_chl_cap(
    IN HAPP             hApp,
    IN HCHAN            hChan,
    IN HCALL            hsCall,
    IN RvUint8          chanType,
    IN RvBool		bIsReverseChan);
    
void vt_reset_mtk_chl_cap(
    IN int              chanType);


#ifdef __cplusplus
}
#endif

#endif /* _RV_TERM_CHANNEL_H_ */
