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

#ifndef _RV_TERM_CONTROL_H_
#define _RV_TERM_CONTROL_H_

/***********************************************************************
termControl.h

H.245 control handling of the terminal.
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
int RVCALLCONV cmEvCallCapabilities(
    IN      HAPPCALL            haCall,
    IN      HCALL               hsCall,
    IN      cmCapStruct*        capabilities[]);
int RVCALLCONV cmEvCallCapabilitiesExt(
    IN      HAPPCALL            haCall,
    IN      HCALL               hsCall,
    IN      cmCapStruct***      capabilities[]);
int RVCALLCONV cmEvCallCapabilitiesMuxCap(
    IN      HAPPCALL            haCall,
    IN      RvBool              bIsSupportNSRP);
int RVCALLCONV cmEvCallCapabilitiesResponse(
    IN      HAPPCALL            haCall,
    IN      HCALL               hsCall,
    IN      RvUint32            status);
int RVCALLCONV cmEvCallMasterSlaveStatus(
    IN      HAPPCALL            haCall,
    IN      HCALL               hsCall,
    IN      RvUint32            status);
RvStatus RVCALLCONV cmEvCallMasterSlaveExpectedStatus(
    IN      HAPPCALL            haCall,
    IN      HCALL               hsCall,
    IN      RvUint32            status);
int RVCALLCONV cmEvCallRoundTripDelay(
    IN      HAPPCALL            haCall,
    IN      HCALL               hsCall,
    IN      RvInt32             delay);
int RVCALLCONV cmEvCallUserInput(
    IN      HAPPCALL            haCall,
    IN      HCALL               hsCall,
    IN      RvInt32             userInputId);
int RVCALLCONV cmEvCallRequestMode(
    IN      HAPPCALL            haCall,
    IN      HCALL               hsCall,
    IN      cmReqModeStatus     status,
    IN      RvInt32             nodeId);
int RVCALLCONV cmEvCallMiscStatus(
    IN      HAPPCALL            haCall,
    IN      HCALL               hsCall,
    IN      cmMiscStatus        status);
int RVCALLCONV cmEvCallControlStateChanged(
    IN      HAPPCALL            haCall,
    IN      HCALL               hsCall,
    IN      cmControlState      state,
    IN      cmControlStateMode  stateMode);
int RVCALLCONV cmEvCallMasterSlave(
    IN      HAPPCALL            haCall,
    IN      HCALL               hsCall,
    IN      RvUint32            terminalType,
    IN      RvUint32            statusDeterminationNumber);
int RVCALLCONV cmEvCallControlMessage(
    IN  HAPPCALL            haCall,
    IN  HCALL               hsCall,
    IN  HAPPCHAN            haChan,
    IN  HCHAN               hsChan,
    IN  RvPvtNodeId         message,
    IN  cmH245MessageType   messageType);
int RVCALLCONV cmEvCallMultiplexEntry(
    IN      HAPPCALL            haCall,
    IN      HCALL               hsCall,
    IN      RvPvtNodeId         descriptors);
int RVCALLCONV cmEvCallMultiplexEntryResponse(
    IN      HAPPCALL            haCall,
    IN      HCALL               hsCall,
    IN      RvBool              isACK,
    IN      RvUint16            includedEntries,
    IN      RvPvtNodeId         descriptions);

int RVCALLCONV cmEvCallVendorIdentification(
    IN      HAPPCALL                 haCall,
    IN      HCALL                    hsCall,
    IN      cmNonStandardIdentifier  *nonStandardId,
    IN      RvUint8                  *productNumber,
    IN      RvUint16                 productNumberSize,
    IN      RvUint8                  *versionNumber,
    IN      RvUint16                 versionNumberSize);

/* MTK */
int RVCALLCONV cmEvCallSendTerminalCapabilitySet(
    IN      HAPPCALL                 haCall,
    IN      HCALL                    hsCall);
/* MTK */

#if (RV_3G324M_USE_MONA == RV_YES)
/******************************************************************************
 * Rv3G324mH223ChannelReceiveEv
 * ----------------------------------------------------------------------------
 * General: Callback function called when the first MONA preference message 
 *          is received (not including messages with crc errors).
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324mAppCall       - Application's call handle of the call.
 *         h3G324mCall          - The call handle.
 *         mpcRx                - Bit mask of the remote receiving mpc channels.
 *         mpcTx                - Bit mask of the remote transmitting mpc channels.
 *         pMsg                 - The mona preference message.
 *         bufferSize           - The size of the preference message in bytes.
 *         bAnyError            - RV_TRUE if the frame might contain errors (crc check failed).
 * Output: None.
 *****************************************************************************/
void RVCALLCONV CallMonaPreferenceMsgRcvEv(
    IN      HAPPCALL                 haCall,
    IN      HCALL                    hsCall,
    IN      RvUint16                 mpcRx,
    IN      RvUint16                 mpcTx,
    IN      RvUint8                  *pMsg,
    IN      RvSize_t                 msgSize,
    IN      RvBool                   bAnyError);

/******************************************************************************
 * Rv3G324mMONAPreferenceSendEv
 * ----------------------------------------------------------------------------
 * General: Callback function called in two events:
 *          1. After the required minimum of MONA preference messages were sent.
 *          2. After the last MONA preference message was sent.
 *          Note that these events can occur simultaneously and in that case
 *          the event will be raised only once to the application.
 *          This event should be used by the application to decide when to start
 *          sending media on MPC channels.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324mAppCall       - Application's call handle of the call.
 *         h3G324mCall          - The call handle.
 *         lastMessage          - RV_TRUE if no more preference messages 
 *                                are going to be sent.
 * Output: None.
 *****************************************************************************/
void RVCALLCONV CallMonaPreferenceMsgSendEv(
    IN      HAPPCALL                 haCall,
    IN      HCALL                    hsCall,
    IN      RvBool                   lastMessage);

#endif/* USE_MONA */



/*-----------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                           */
/*-----------------------------------------------------------------------*/


/******************************************************************************
 * termCallMediaLoopOff
 * ----------------------------------------------------------------------------
 * General: Indicate all channels of a call to stop loopback.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 * Output: None.
 *****************************************************************************/
RvStatus termCallMediaLoopOff(
    IN TermCallObj          *call);


/******************************************************************************
 * termCallRoundTripDelay
 * ----------------------------------------------------------------------------
 * General: Check the round trip delay on H.245.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 *         delay        - Maximum delay in seconds.
 * Output: None.
 *****************************************************************************/
RvStatus termCallRoundTripDelay(
    IN TermCallObj          *call,
    IN RvInt32              delay);


/******************************************************************************
 * termCallH223SkewIndication
 * ----------------------------------------------------------------------------
 * General: Indicate the remote side on the delay between audio and video.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 *         skew         - Skew between the channels.
 * Output: None.
 *****************************************************************************/
RvStatus termCallH223SkewIndication(
    IN TermCallObj          *call,
    IN RvUint32             skew);


/******************************************************************************
 * termCallSendCapabilities
 * ----------------------------------------------------------------------------
 * General: Send a TCS message.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 *         nodeId       - RV_PVT_INVALID_NODEID to send the capability set from
 *                        the configuration.
 *         isEmpty      - RV_TRUE to send an empty capability set.
 * Output: None.
 *****************************************************************************/
RvStatus termCallSendCapabilities(
    IN TermCallObj          *call,
    IN RvPvtNodeId          nodeId,
    IN RvBool               isEmpty);


/******************************************************************************
 * termCallSendCapabilitiesAck
 * ----------------------------------------------------------------------------
 * General: Send a TCS ack message.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 * Output: None.
 *****************************************************************************/
RvStatus termCallSendCapabilitiesAck(
    IN TermCallObj          *call);


/******************************************************************************
 * termCallSendMasterSlave
 * ----------------------------------------------------------------------------
 * General: Send an MSD message.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 * Output: None.
 *****************************************************************************/
RvStatus termCallSendMasterSlave(
    IN TermCallObj          *call);


/******************************************************************************
 * termCallSendMutliplexEntryTable
 * ----------------------------------------------------------------------------
 * General: Send the multiplexing entry table.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 *         bCheckAuto   - Check if we need to automatically set the manual
 *                        mux table entries set in the configuration.
 * Output: None.
 *****************************************************************************/
RvStatus termCallSendMutliplexEntryTable(
    IN TermCallObj          *call,
    IN RvBool               bCheckAuto);


/******************************************************************************
 * termCallSendMutliplexEntryTableAck
 * ----------------------------------------------------------------------------
 * General: Send the multiplexing entry table acknowledgment.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 * Output: None.
 *****************************************************************************/
RvStatus termCallSendMutliplexEntryTableAck(
    IN TermCallObj          *call);


/******************************************************************************
 * termCallMutliplexEntryTableClear
 * ----------------------------------------------------------------------------
 * General: Clear up the multiplexing entry table, setting it to its default
 *          value if so desired.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call             - Call handle to use.
 *         bSetToDefault    - RV_TRUE to set the table to the default value.
 * Output: None.
 *****************************************************************************/
RvStatus termCallMutliplexEntryTableClear(
    IN TermCallObj          *call,
    IN RvBool               bSetToDefault);


/******************************************************************************
 * termCallMutliplexEntryAdd
 * ----------------------------------------------------------------------------
 * General: Add a manual entry to the multiplexing entry table.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call             - Call handle to use.
 *         audioSize        - Size of a frame of audio to allow.
 *         video1Size       - Primary video channel ratio.
 *         video2Size       - Secondary video channel ratio.
 *         dataSize         - Data channel ratio.
 * Output: None.
 *****************************************************************************/
RvStatus termCallMutliplexEntryAdd(
    IN TermCallObj          *call,
    IN RvUint32             audioSize,
    IN RvUint32             video1Size,
    IN RvUint32             video2Size,
    IN RvUint32             dataSize);


/* MTK */
/******************************************************************************
 * termCallSendAlphanumbericUII
 * ----------------------------------------------------------------------------
 * General: Send a user input indication message/ alphanumberic only
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 *         uii          - User input indication.
 * Output: None.
 *****************************************************************************/
RvStatus termCallSendAlphanumericUII(
    IN TermCallObj          *call,
    IN const RvChar         *uiiString,
	IN const RvUint8        uiiLength);

/******************************************************************************
 * termCallSendUserInputIndication
 * ----------------------------------------------------------------------------
 * General: Send a user input indication message.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 *         uii          - User input indication.
 *         nsd          - Non standard parameter to use (can be NULL).
 * Output: None.
 *****************************************************************************/
RvStatus termCallSendUserInputIndication(
    IN TermCallObj          *call,
    IN const RvChar         *uii,
    IN cmNonStandardParam   *nsd);


/******************************************************************************
 * termCallMaxH223MuxPduSize
 * ----------------------------------------------------------------------------
 * General: Command the remote side to limit the size of MUX - PDUs.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call          - Call handle to use.
 *         maxMuxPduSize - The maximum MUX - PDU size.
 * Output: None.
 *****************************************************************************/
RvStatus termCallMaxH223MuxPduSize(
    IN TermCallObj          *call,
    IN RvUint16             maxMuxPduSize);

/******************************************************************************
 * termCallSendVendorIdentification
 * ----------------------------------------------------------------------------
 * General: Sending VendorIdentification indication.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 *         nonStandardId     - The non standard identifier.
 *         productNumber     - The product number parameter.
 *         productNumberSize - The product number parameter size.
 *         versionNumber     - The version number parameter.
 *         versionNumberSize - The version number parameter size.
 * Output: None.
 *****************************************************************************/
RvStatus termCallSendVendorIdentification(
    IN TermCallObj             *call,
    IN cmNonStandardIdentifier *nonStandardId,
    IN RvUint8                 *productNumber,
    IN RvUint16                productNumberSize,
    IN RvUint8                 *versionNumber,
    IN RvUint16                versionNumberSize);


/******************************************************************************
 * termCallAddMONACapability
 * ----------------------------------------------------------------------------
 * General: Adds the MONA generic capability to the capability table in the TCS.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 * Output: None.
 *****************************************************************************/
RvStatus termCallAddMONACapability(IN TermCallObj *call);

/******************************************************************************
 * termCallRemoveMONACapability
 * ----------------------------------------------------------------------------
 * General: Removes the MONA generic capability from the TCS.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 * Output: None.
 *****************************************************************************/
RvStatus termCallRemoveMONACapability(IN TermCallObj *call);

/******************************************************************************
 * MTK: termCallVIIConstructAndSend
 * ----------------------------------------------------------------------------
 * General: Construct and send vendor Identfication
 *****************************************************************************/
RVAPI RvStatus termCallVIIConstructAndSend(
    IN HCALL h3G324mCall);

/* MTK */
/******************************************************************************
 * termCallSendRequestMutliplexEntry
 * ----------------------------------------------------------------------------
 * General: Request to send the multiplexing entry table.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 *         
 * Output: None.
 *****************************************************************************/
RvStatus termCallSendRequestMultiplexEntry(
    IN TermCallObj          *call,
    IN RvUint16             entries);

#ifdef __cplusplus
}
#endif

#endif /* _RV_TERM_CONTROL_H_ */
