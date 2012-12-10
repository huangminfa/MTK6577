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

#ifndef _RV_TERM_MEDIA_H_
#define _RV_TERM_MEDIA_H_

/***********************************************************************
termMedia.h

Media channels management.
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
/*                           FUNCTIONS HEADERS                           */
/*-----------------------------------------------------------------------*/

/******************************************************************************
 * termMediaInit
 * ----------------------------------------------------------------------------
 * General: Initialize the media. This function sets the codec decoder configuration that 
 * will be used in OpenLogicalChannel
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term 
 * Output: None
 *****************************************************************************/
RvStatus termMediaInit(
    IN TermObj *term);

/******************************************************************************
 * termMediaCallInit
 * ----------------------------------------------------------------------------
 * General: Initialize the media. This function handles the simulation
 *          of an actual ISDN card or any other facilities used to pass the
 *          H.223 bit-stream to the remote side of the channel.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call - The call that its channels are about to send media.
 * Output: None
 *****************************************************************************/
RvStatus termMediaCallInit(
    IN TermCallObj  *call);


/******************************************************************************
 * termMediaCallEnd
 * ----------------------------------------------------------------------------
 * General: Deinitialize the media.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call - The call instance.
 * Output: None
 *****************************************************************************/
RvStatus termMediaCallEnd(
    IN TermCallObj  *call);


/******************************************************************************
 * termMediaChannelInit
 * ----------------------------------------------------------------------------
 * General: Initialize the media information of a new channel object.
 *          This function allocates a buffers for the read file information and
 *          for the write file information.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel - Channel to initialize
 * Output: None
 *****************************************************************************/
RvStatus termMediaChannelInit(
    IN TermChannelObj   *channel);


/******************************************************************************
 * termMediaChannelEnd
 * ----------------------------------------------------------------------------
 * General: Deallocates the media information of a closed channel.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel - Channel to end
 * Output: None
 *****************************************************************************/
RvStatus termMediaChannelEnd(
    IN TermChannelObj   *channel);


/******************************************************************************
 * termMediaDisplayStatus
 * ----------------------------------------------------------------------------
 * General: Displays any information related to the current channel's media,
 *          and make sure this function is invoked again in the future if
 *          needed.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call     - Call to display.
 * Output: None
 *****************************************************************************/
void termMediaDisplayStatus(
    IN TermCallObj  *call);


/******************************************************************************
 * MediaLogicalChannelRtxRequestEv
 * ----------------------------------------------------------------------------
 * General: Callback function called when a bidirectional channel has found out
 *          that it might require a retransmission of media frames from the
 *          remote terminal.
 *          This callback is only invoked for bidirectional channels that were
 *          opened using AL3, AL1M or AL3M that support retransmissions.
 *          In this callback, the application may decide to ask for
 *          retransmissions of only part of the lost frames or even not to ask
 *          for retransmissions at all.
 *
 * Return Value: The maximum number of frames to ask retransmissions for.
 *               If for example, expectedSequenceNumber=10 while 
 *               receivedSequenceNumber=15. Then a return value of 2 will indicate
 *               that frames with sequence numbers 13 and 14 should be retransmitted.
 *               If the retransmission request is for a CRC error, then a return
 *               value of 1 indicates that the application would like a
 *               retransmission due to the CRC error.
 *               0 return value indicates that the application doesn't want
 *               any retransmission requests for the missing/bad frames.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324mAppCall           - Application's call handle of the call.
 *         h3G324mCall              - The new created call handle.
 *         h3G324mAppChan           - Application's channel handle.
 *         h3G324mChan              - The channel handle.
 *         expectedSequenceNumber   - The sequence number that was expected
 *                                    as the next incoming frame on this channel.
 *         receivedSequenceNumber   - The sequence number that was received.
 *                                    If this sequence number is equal to the one
 *                                    in expectedSequenceNumber, then it is
 *                                    an indication that we had a CRC error on
 *                                    the frame and the application may choose
 *                                    to ask for a retransmission for this frame.
 * Output: None.
 *****************************************************************************/
RvInt32 RVCALLCONV MediaLogicalChannelRtxRequestEv(
    IN  HAPPCALL                h3G324mAppCall,
    IN  HCALL                   h3G324mCall,
    IN  HAPPCHAN                h3G324mAppChan,
    IN  HCHAN                   h3G324mChan,
    IN  RvUint32                expectedSequenceNumber,
    IN  RvUint32                receivedSequenceNumber);


/******************************************************************************
 * MediaLogicalChannelReceivedDataExtEv
 * ----------------------------------------------------------------------------
 * General: Callback function called when to notify that data was received
 *          for the specified channel.
 *          This callback can be used instead of
 *          Rv3G324mCallLogicalChannelReceivedDataEv() to indicate lost packets
 *          (using the sequence number when applicable) and to indicate that a
 *          given data had a CRC error.
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324mAppCall   - Application's call handle of the call.
 *         h3G324mCall      - The new created call handle.
 *         h3G324mAppChan   - Application's channel handle.
 *         h3G324mChan      - The channel handle.
 *         pBuffer          - The buffer that contains the received data.
 *         size             - The buffer size.
 *         bAnyError        - RV_TRUE if the buffer contains an error (CRC
 *                            check failed for this buffer).
 *         sequenceNumber   - Sequence number of the buffer received (0 if
 *                            sequence numbering is not supported by this
 *                            channel). The sequence number can be used to
 *                            identify a situation of lost packets.
 * Output: none.
 *****************************************************************************/
void RVCALLCONV MediaLogicalChannelReceivedDataExtEv(
    IN  HAPPCALL                h3G324mAppCall,
    IN  HCALL                   h3G324mCall,
    IN  HAPPCHAN                h3G324mAppChan,
    IN  HCHAN                   h3G324mChan,
    IN  RvUint8                 *pBuffer,
    IN  RvUint32                size,
    IN  RvBool                  bAnyError,
    IN  RvUint16                sequenceNumber);

/******************************************************************************
 * MediaMonaMPCChannelReceivedDataEv
 * ----------------------------------------------------------------------------
 * General: Callback function called when to notify that data was received
 *          for the specified MPC channel.
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324mAppCall   - Application's call handle of the call.
 *         h3G324mCall      - The new created call handle.
 *         mpcChannel        - MPC channel type/index.
 *         pBuffer          - The buffer that contains the received data.
 *         size             - The buffer size.
 *         bAnyError        - RV_TRUE if the buffer contains an error (CRC
 *                            check failed for this buffer).
 *         sequenceNumber   - Sequence number of the buffer received (0 if
 *                            sequence numbering is not supported by this
 *                            channel). The sequence number can be used to
 *                            identify a situation of lost packets.
 * Output: none.
 *****************************************************************************/
void RVCALLCONV  MediaMonaMPCChannelReceivedDataEv(
    IN  HAPPCALL                h3G324mAppCall,
    IN  HCALL                   h3G324mCall,
    IN  Rv3G324mCallMonaMPCType mpcChannel,
    IN  RvUint8                 *pBuffer,
    IN  RvUint32                size,
    IN  RvBool                  bAnyError,
    IN  RvUint16                sequenceNumber);
#ifdef USE_HANDSET
/******************************************************************************
 * termMediaSend
 * ----------------------------------------------------------------------------
 * General: encodes media buffer, copies it into an allocated buffer, 
 *          which is sent over the channel
 *
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  channel      - channel used for send
 *         outBuf       - buffer to be encoded and sent
 *         bufSize      - size of buffer
 * Output: None.
 *****************************************************************************/
void termMediaSend(IN TermChannelObj   *channel,
                   IN RvUint8          *outBuf,
                   IN RvUint32         bufSize);
#endif /* USE_HANDSET */

/******************************************************************************
 * mediaFindTypeFromNodeId
 * ----------------------------------------------------------------------------
 * General: Get the field id of the given data type we're dealing with.
 *
 * Return Value: Media type found.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hVal         - PVT handle used.
 *         dataTypeId   - DataType as known by the channel.
 *         channel      - Channel object that needs to use the data type.
 * Output: None.
 *****************************************************************************/
cmChannelDataType mediaFindTypeFromNodeId(
    IN HPVT             hVal,
    IN RvPvtNodeId      dataTypeId,
    IN TermChannelObj   *channel);


//20090326: MAUI_01652413
/* Patch 2009/03/24 FastUpdateSending Improvement */
void termMediaRequestIFrame(void);

#ifdef __cplusplus
}
#endif

#endif /* _RV_TERM_MEDIA_H_ */
