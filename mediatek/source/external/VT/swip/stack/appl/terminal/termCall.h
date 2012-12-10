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

#ifndef _RV_TERM_CALL_H_
#define _RV_TERM_CALL_H_

/***********************************************************************
termCall.h

Call handling of the endpoint.
Handles the call procedures.
***********************************************************************/


/*-----------------------------------------------------------------------*/
/*                        INCLUDE HEADER FILES                           */
/*-----------------------------------------------------------------------*/
#include "termDefs.h"
#include "vt_option_cfg.h"



#ifdef __cplusplus
extern "C" {
#endif



/*-----------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                            */
/*-----------------------------------------------------------------------*/




/*-----------------------------------------------------------------------*/
/*                           CALLBACK HEADERS                            */
/*-----------------------------------------------------------------------*/


void RVCALLCONV CallStateChangedEv(
    IN  HAPPCALL                h3G324mAppCall,
    IN  HCALL                   h3G324mCall,
    IN  Rv3G324mCallState       callState,
    IN  Rv3G324mCallStateMode   callStateMode);
RvBool RVCALLCONV CallSendMessageEv(
    IN  HAPPCALL                h3G324mAppCall,
    IN  HCALL                   h3G324mCall,
    IN  RvPvtNodeId             messageNodeId);
RvBool RVCALLCONV CallReceiveMessageEv(
    IN  HAPPCALL                h3G324mAppCall,
    IN  HCALL                   h3G324mCall,
    IN  RvPvtNodeId             messageNodeId);
void RVCALLCONV CallGetRandomNumberEv(
    IN  HAPP       h3G324m,
    IN  HCALL      h3G324mCall,
    IN  HAPPCALL   h3G324mAppCall,
    OUT RvUint32   *value);
void RVCALLCONV CallMessageSendingFailureEv(
    IN  HAPPCALL   h3G324mAppCall,
    IN  HCALL      h3G324mCall);
void RVCALLCONV CallLogicalChannelReleaseSendBufferEv(
    IN  HAPPCALL                h3G324mAppCall,
    IN  HCALL                   h3G324mCall,
    IN  HAPPCHAN                h3G324mAppChan,
    IN  HCHAN                   h3G324mChan,
    IN  RvUint8                 *pBuffer,
    IN  RvUint32                size);
RvBool RVCALLCONV CallReceivedResetRequestEv(
    IN  HAPPCALL                h3G324mAppCall,
    IN  HCALL                   h3G324mCall);
void RVCALLCONV CallBitShiftIndicationEv(
    IN  HAPPCALL                h3G324mAppCall,
    IN  HCALL                   h3G324mCall,
    IN  RvUint8                 bitShift);

#if (RV_3G324M_USE_MONA == RV_YES)
RvUint16 termCallInitMonaMPCChannels(IN TermCallObj  *call);
#endif

/*-----------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                           */
/*-----------------------------------------------------------------------*/


/******************************************************************************
 * termCallCreate
 * ----------------------------------------------------------------------------
 * General: Create a complete call object, and sets call parameters for it.
 *
 * Return Value: Call object created.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  ep       - Endpoint object to use.
 *         id       - Id to give to the new call. If 0, will be allocated by
 *                    the allocation resource callback.
 * Output: None.
 *****************************************************************************/
TermCallObj *termCallCreate(
    IN TermObj  *term,
    IN RvInt32  id);


/******************************************************************************
 * termCallDestruct
 * ----------------------------------------------------------------------------
 * General: Close a call object.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 * Output: None.
 *****************************************************************************/
RvStatus termCallDestruct(
    IN TermCallObj          *call);


/******************************************************************************
 * termCallGetExtraData
 * ----------------------------------------------------------------------------
 * General: Get the extra data of the call object.
 *
 * Return Value: Pointer to the extra data of the call.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 * Output: None.
 *****************************************************************************/
void *termCallGetExtraData(
    IN TermCallObj          *call);


/******************************************************************************
 * termCallGetFromExtraData
 * ----------------------------------------------------------------------------
 * General: Get the call object from its extra data pointer.
 *
 * Return Value: Call handle.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  extra        - Pointer to the extra data of a call.
 * Output: None.
 *****************************************************************************/
TermCallObj *termCallGetFromExtraData(
    IN void                 *extra);


/******************************************************************************
 * termCallDial
 * ----------------------------------------------------------------------------
 * General: Dial out a call to a given IP address (optional).
 *          All settings for this call are done using cmCallSetParam() before
 *          calling this function.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 *         destStr      - Destination address. Can be NULL.
 * Output: None.
 *****************************************************************************/
RvStatus termCallDial(
    IN TermCallObj          *call,
    IN const RvChar         *destStr);


/******************************************************************************
 * termCallAnswer
 * ----------------------------------------------------------------------------
 * General: Answer an incoming call, for call plug-ins that support it.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 * Output: None.
 *****************************************************************************/
RvStatus termCallAnswer(
    IN TermCallObj          *call);


/******************************************************************************
 * termCallChannelsConnected
 * ----------------------------------------------------------------------------
 * General: Check to see if call channels of a given call are in their
 *          connected state, ignoring channels that MES message was already
 *          sent for.
 *
 * Return Value: RV_TRUE if all channel in connected state, RV_FALSE else.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 * Output: None.
 *****************************************************************************/
RvBool termCallChannelsConnected(
    IN TermCallObj          *call);


/******************************************************************************
 * termCallNetworkConnected
 * ----------------------------------------------------------------------------
 * General: Notification to the call object from the ISDN/TCP/other that a call
 *          got connected on the network. This function is used to start
 *          sending messages on the ISDN or TCP connection.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 * Output: None.
 *****************************************************************************/
RvStatus termCallNetworkConnected(
    IN TermCallObj          *call);


/******************************************************************************
 * termCallNetworkDisconnected
 * ----------------------------------------------------------------------------
 * General: Notification to the call object from the ISDN/TCP/other that a call
 *          got disconnected from the network.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 * Output: None.
 *****************************************************************************/
RvStatus termCallNetworkDisconnected(
    IN TermCallObj          *call);


/******************************************************************************
 * termCallReset
 * ----------------------------------------------------------------------------
 * General: Reset the current call.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call handle to use.
 * Output: None.
 *****************************************************************************/
RvStatus termCallReset(
    IN TermCallObj          *call);


/******************************************************************************
 * termCallDrop
 * ----------------------------------------------------------------------------
 * General: Drop a call.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call     - Call handle to use.
 * Output: None.
 *****************************************************************************/
RvStatus termCallDrop(
    IN TermCallObj          *call);


/******************************************************************************
 * termCallDropAll
 * ----------------------------------------------------------------------------
 * General: Drop all calls.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  pTerm    - Terminal object to use.
 * Output: None.
 *****************************************************************************/
RvStatus termCallDropAll(
    IN TermObj              **pTerm);


/******************************************************************************
 * termCallGetById
 * ----------------------------------------------------------------------------
 * General: Find a call object by its id.
 *
 * Return Value: The call object if found, NULL otherwise.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term     - Terminal object to use.
 *         id       - Id of the call to look for.
 * Output: None.
 *****************************************************************************/
TermCallObj *termCallGetById(
    IN TermObj  *term,
    IN RvInt32  id);




/******************************************************************************
 * termCallFileBufInit
 * ----------------------------------------------------------------------------
 * General: Open the binary files needed for writing the data streams of the
 *          calls.
 *          Only opens the files if the application states that it needs to.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call     - Call object to open binary files for
 * Output: None
 *****************************************************************************/
RvStatus termCallFileBufInit(
    IN TermCallObj  *call);


/******************************************************************************
 * termCallFileBufEnd
 * ----------------------------------------------------------------------------
 * General: Close and clear any resources related to the binary data files
 *          of a call.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call     - Call object to close binary files for
 * Output: None
 *****************************************************************************/
RvStatus termCallFileBufEnd(
    IN TermCallObj  *call);


/******************************************************************************
 * termCallFileBufWrite
 * ----------------------------------------------------------------------------
 * General: Write a buffer (incoming or outgoing) into the call's binary
 *          data files.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call object to modify
 *         isIncoming   - RV_TRUE for incoming buffers, RV_FALSE for outgoing
 *         buffer       - Buffer to write
 *         bufferSize   - Size of buffer in bytes
 * Output: None
 *****************************************************************************/
RvStatus termCallFileBufWrite(
    IN TermCallObj  *call,
    IN RvBool       isIncoming,
    IN RvUint8     *buffer,
    IN RvUint32     bufferSize);

/******************************************************************************
 * termCallInitFMChannels
 * ----------------------------------------------------------------------------
 * General: Initialize fmAudioChannel and fmVideoChannel of the call
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call object to modify
 * Output: None
 *****************************************************************************/
void termCallInitFMChannels(IN TermCallObj  *call);

/******************************************************************************
 * termCallRemoveFMChannels
 * ----------------------------------------------------------------------------
 * General: Cleanup of all FM channels resources in the application.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call object to modify
 * Output: None
 *****************************************************************************/
void termCallRemoveFMChannels(IN TermCallObj *call);


void termCallLostSync(IN TermCallObj          *call);

/******************************************************************************
 * termCallRemoveMonaMPCChannels
 * ----------------------------------------------------------------------------
 * General: Cleanup of all MPC channels resources in the application and stack.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call         - Call object to modify
 * Output: None
 *****************************************************************************/
void termCallRemoveMonaMPCChannels(IN TermCallObj *call);

#if (VT_SIM_MODE == SIM_STK_RX_FILE)
/******************************************************************************
 * termFileRead
 * ----------------------------------------------------------------------------
 * General: Read callback to use for a simulated call.
 *
 * Return Value: Bytes read on success, negative value when done.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call             - Call object to use.
 *         fName            - File name to open. NULL if we only want to read.
 *         size             - Buffer size.
 * Output: buf              - Buffer to read the file into.
 *****************************************************************************/
RvInt32 termFileRead(
    IN  TermCallObj         *call,
    IN  const RvChar        *fName,
    IN  RvSize_t                size,
    OUT RvUint8             *buf);
#endif

#ifdef __cplusplus
}
#endif

#endif /* _RV_TERM_CALL_H_ */
