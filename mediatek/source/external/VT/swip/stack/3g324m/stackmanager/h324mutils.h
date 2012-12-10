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

#ifndef _H324M_UTILS_H_
#define _H324M_UTILS_H_

#include "cmH245GeneralDefs.h"
#include "h324mStackObject.h"

#ifdef __cplusplus
extern "C" {
#endif


/************************************************************************
 * H324mGetWatchdogHandle
 * purpose: Get the watchdog object from the stack's instance
 * input  : hApp    - Stack's application handle
 * output : none
 * return : Watchdog module pointer on success
 *          NULL on failure
 ************************************************************************/
//RVAPI RvWatchdog* RVCALLCONV H324mGetWatchdogHandle(IN HAPP hApp);

/************************************************************************
 * H324mGetWatchdogHandle
 * purpose: Get the watchdog log source from the stack's instance
 * input  : hApp    - Stack's application handle
 * output : none
 * return : Watchdog module pointer on success
 *          NULL on failure
 ************************************************************************/
RvLogSource* H324mGetWatchdogLogSource(IN HAPP hApp);


/***************************** H.223 Buffer manager ***********************************/

/******************************************************************************
 * H324mBuffersInit
 * ----------------------------------------------------------------------------
 * General: Init the buffer manager of the H.223 module.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324m      - handle to the the stack.
 *         pStackCfg    - the stack configuration structure.
 * Output: none.
 *****************************************************************************/
RvStatus RVCALLCONV H324mBuffersInit(
                                    IN  HAPP             h3G324m,
                                    IN  H324mStackConfig *pStackCfg);

/******************************************************************************
 * H324mBuffersEnd
 * ----------------------------------------------------------------------------
 * General: End the buffer manager of the H.223 module.
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324m
 * Output: none.
 *****************************************************************************/
void RVCALLCONV H324mBuffersEnd(IN  HAPP             h3G324m);

/******************************************************************************
 * H324mLCGetBufEv
 * ----------------------------------------------------------------------------
 * General: the implementation for the H.223 logical channel RvH223GetBufEv
 *          callback.
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  context - the context of the logical channel.
 *         pBuffer - the buffer to be supplied.
 *         pSize   - the size of the buffer that was supplied.
 * Output: none.
 *****************************************************************************/
void H324mLCGetBufEv (
                   IN   void     *context,
                   OUT  RvUint8  **pBuffer,
                   OUT  RvUint32 *pSize);

/******************************************************************************
 * H324mLCReleaseBufEv
 * ----------------------------------------------------------------------------
 * General: the implementation for the H.223 logical channel RvH223ReleaseBufEv
 *          callback.
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  context - the context of the control.
 *         pBuffer - the buffer to be released.
 * Output: none.
 *****************************************************************************/
void H324mLCReleaseBufEv (
                           IN void    *context,
                           IN RvUint8 *pBuffer);

/******************************************************************************
 * H324mACPLCGetBufferEv
 * ----------------------------------------------------------------------------
 * General: the implementation for the H.223 logical channel RvH223GetBufEv callback
 *          for ACP calls before incoming OLCs received. .
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  context          - the context of the call.
 *         pBuffer          - the buffer to be supplied.
 *         pSize            - the size of the buffer that was supplied.
 * Output: None..
 *****************************************************************************/
void H324mACPLCGetBufferEv(
    IN   void     *context,
                           OUT  RvUint8  **pBuffer,
                           OUT  RvUint32 *pSize);

/******************************************************************************
 * H324mACPLCReleaseBufferEv
 * ----------------------------------------------------------------------------
 * General: the implementation for the H.223 logical channel RvH223ReleaseBufEv 
 *          for ACP calls before incoming OLCs received,
 *          callback.
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  context          - the context of the call.
 *         subContext       - 
 *         pBuffer          - the buffer to be released.
 *         bufSize          - size to be released.
 *         indicationType   - Type of indication of the release procedure.
 * Output: none.
 *****************************************************************************/
void H324mACPLCReleaseBufferEv(
                           IN void                      *context,
                           IN RvInt32                   subContext,
                           IN RvUint8                   *pBuffer,
                           IN RvUint32                  bufSize,
                           IN EReleaseIndicationType    indicationType);

#if (RV_3G324M_USE_MONA == RV_YES)
/******************************************************************************
 * H324mMPCLCGetBufferEv
 * ----------------------------------------------------------------------------
 * General: the implementation for the H.223 logical channel RvH223GetBufEv callback
 *          for MPC calls. .
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  context          - the context of the call.
 *         pBuffer          - the buffer to be supplied.
 *         pSize            - the size of the buffer that was supplied.
 * Output: None..
 *****************************************************************************/
void H324mMPCLCGetBufferEv(IN   void        *context,
                           OUT  RvUint8     **pBuffer,
                           OUT  RvUint32    *pSize);

/******************************************************************************
 * H324mMPCLCReleaseBufferEv
 * ----------------------------------------------------------------------------
 * General: the implementation for the H.223 logical channel RvH223ReleaseBufEv 
 *          for MPC calls with no H245 logical cahannels available,
 *          callback.
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  context          - the context of the call.
 *         pBuffer          - the buffer to be released.
 * Output: none.
 *****************************************************************************/
void H324mMPCLCReleaseBufferEv(
                           IN void                      *context,
                           IN RvUint8                   *pBuffer);

/******************************************************************************
 * H324mMPCLCReleaseMuxSduEv
 * ----------------------------------------------------------------------------
 * General: The implementation for the H.223 logical channel ReleaseMuxSduEv
 *          callback for MPC calls. Notify the application that the send buffer should be
 *          released.
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  context          - the context of the call.
 *         subContext       - the type of MPC channel.
 *         pBuffer          - the buffer to be released.
 *         bufSize          - the buffer size.
 *         indicationType   - Type of indication of the release procedure.
 * Output: none.
 *****************************************************************************/
void H324mMPCLCReleaseMuxSduEv(
    IN void                     *context,
    IN RvInt32                  subContext,
    IN RvUint8                  *pBuffer,
    IN RvUint32                 bufSize,
    IN EReleaseIndicationType   indicationType);

/******************************************************************************
 * H324mMONAUpdateAckStatusEv
 * ----------------------------------------------------------------------------
 * General: Update the monaAckStatus in the muxer
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  context          - the context of the call.
 *         ackStatus        - the ack status in the last received preference message.
 * Output: none.
 *****************************************************************************/
void H324mMONAUpdateAckStatusEv(
    IN void                     *context,
    IN RvUint8                  ackStatus);

/******************************************************************************
 * MTK Patch: H324mReceiveFirstH223PDUEv
 * ----------------------------------------------------------------------------
 * General: Disable MONA stuffing when receiving first H223 PDU
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  context          - the context of the call.
 * Output: none.
 *****************************************************************************/
void H324mReceiveFirstH223PDUEv(
    IN void                     *context);

/******************************************************************************
 * H324mMONAPreferenceRcvEv
 * ----------------------------------------------------------------------------
 * General: Notify the application that a preference message was received.
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  context          - The context of the call.
 *         mpcRx            - The bitmask of matching mpc types for receiving media.
 *         mpcTx            - The bitmask of matching mpc types for transmitting media.
 *         *pMsg            - The preference message. 
 *         msgSize          - The preference message size.
 *         bAnyError        - If RV_TRUE - there is crc error on the preference message. 
 * Output: none.
 *****************************************************************************/
void H324mMONAPreferenceRcvEv(
    IN  void *                  context, 
    IN  RvUint16                mpcRx,
    IN  RvUint16                mpcTx,
    IN  RvUint8                 *pMsg, 
    IN  RvSize_t                msgSize,
    IN  RvBool                  bAnyError);

/******************************************************************************
 * H324mMONAPreferenceSendEv
 * ----------------------------------------------------------------------------
 * General: Notify the application of the MONA preference message sending status.
 *          Raised once when the minimum required number of preference messages
 *          have been sent.
 *          Raised also after the last preference message was sent (both events 
 *          can occur in the same time or one after the other)
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  context          - The context of the call.
 *         lastMessage      - RV_TRUE if no more preference messages are 
 *                            going to be sent
 * Output: none.
 *****************************************************************************/
void H324mMONAPreferenceSendEv(
    IN  void *                  context,
    IN  RvBool                  lastMessage);

#endif/* USE_MONA */
/******************************************************************************
 * H324mLCReleaseMuxSduEv
 * ----------------------------------------------------------------------------
 * General: The implementation for the H.223 logical channel ReleaseMuxSduEv
 *          callback. Notify the application that the send buffer should be
 *          released.
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  context          - the context of the control.
 *         subContext       - Currently not used.
 *         pBuffer          - the buffer to be released.
 *         bufSize          - the buffer size.
 *         indicationType   - Type of indication of the release procedure.
 * Output: none.
 *****************************************************************************/
void H324mLCReleaseMuxSduEv(
    IN void                     *context,
    IN RvInt32                  subContext,
    IN RvUint8                  *pBuffer,
    IN RvUint32                 bufSize,
    IN EReleaseIndicationType   indicationType);

/******************************************************************************
 * H324mGetBuffer
 * ----------------------------------------------------------------------------
 * General: Get buffer (usually for encode or decode actions).
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324m    - the context of the control.
 *         bIsControl - defines whether the buffer is for control or for regular
 *                      logical channel.
 * Output: pBuffer    - the buffer that the stack allocates.
 *         pSize      - the size of the buffer.
 *****************************************************************************/
RvStatus RVCALLCONV H324mGetBuffer (
                               IN  HAPP     h3G324m,
                               IN  RvBool   bIsControl,
                               OUT RvUint8  **pBuffer,
                               OUT RvUint32 *pSize);

/******************************************************************************
 * H324mReleaseBuffer
 * ----------------------------------------------------------------------------
 * General: Release the buffer.
 *
 * Return Value: none.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324m    - the context of the control.
 *         bIsControl - defines whether the buffer is for control or for regular
 *                      logical channel.
 * Output: pBuffer    - the buffer that the stack allocates.
 *         pSize      - the size of the buffer.
 *****************************************************************************/
RvStatus RVCALLCONV H324mReleaseBuffer (
                               IN  HAPP     h3G324m,
                               IN  RvBool   bIsControl,
                               IN  RvUint8  *pBuffer);


#ifdef __cplusplus
}
#endif

#endif /* _H324M_UTILS_H_ */
