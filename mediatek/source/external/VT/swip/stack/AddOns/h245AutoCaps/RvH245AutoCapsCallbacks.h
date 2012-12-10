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

#ifndef _RV_H245_AUTO_CAPS_CALLBACKS_H_
#define _RV_H245_AUTO_CAPS_CALLBACKS_H_


/*-----------------------------------------------------------------------*/
/*                        INCLUDE HEADER FILES                           */
/*-----------------------------------------------------------------------*/

#include "RvH245AutoCapsTypes.h"
#include "RvH245AutoCapsValidation.h"

#ifdef __cplusplus
extern "C" {
#endif

/*-----------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                            */
/*-----------------------------------------------------------------------*/


/*-----------------------------------------------------------------------*/
/*                           FUNCTION HEADERS                           */
/*-----------------------------------------------------------------------*/

/******************************************************************************
 * RvH245AutoCapsChannelStateChangeEv
 * ----------------------------------------------------------------------------
 * General: This function should be called from inside the implementation of the
 *          application of the cmEvCallControlStateChangedT() callback.
 *          The application must fill the information object with all the parameters
 *          at hand.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hAutoCapsCall     - The handle to the H.245 AutoCaps call object.
 *                             The application must set all the information
 *                             needed for the validation to this handle.
 *         hsChan            - The channel handle.
 *         state             - The state of the channel.
 *         stateMode         - The state mode of the channel.
 * Output: eChannelResponse  - The recommended channel response. If the recommended
 *                             response is Reject, then the reject reason can be found 
 *                             in the eRejectReason parameter.
 *                             It is the responsibility of the application to respond to the
 *                             channel.
 *         eRejectReason     - The reject reason of the channel. This parameter should be 
 *                             considered only if the eChannelResponse parameter recommends 
 *                             that the channel be rejected. Otherwise, this parameter 
 *                             should be ignored.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvH245AutoCapsChannelStateChangeEv(
    IN  HAUTOCAPSCALL                  hAutoCapsCall,
    IN  HCHAN                          hsChan,
    IN  cmChannelState_e               state,
    IN  cmChannelStateMode_e           stateMode,
    OUT RvH245AutoCapsChannelResponse  *eChannelResponse,
    OUT cmRejectLcnReason              *eRejectReason);


/******************************************************************************
 * RvH245AutoCapsChannelMasterSlaveConflictEv
 * ----------------------------------------------------------------------------
 * General: This function should be called from inside the implementation of the
 *          application of the cmEvChannelMasterSlaveConflictT() callback.
 *          The application must fill the information object with all the parameters
 *          at hand.
 *          Note that this module assumes that the application
 *          returns a negative value in the cmEvChannelMasterSlaveConflictT()
 *          callback.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hAutoCapsCall  - The handle to the H.245 AutoCaps call object.
 *                          The application must set all the information needed 
 *                          for the validation to this handle.
 *         hsChan         - Stack's handle for the channel.
 *         conflictType   - Type of conflict.
 *         confChans      - Array of handles of conflicting channels.
 *         numConfChans   - Number of channels in the array.
 *         message        - PVT node ID of the incoming OLC.
 * Output: None.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvH245AutoCapsChannelMasterSlaveConflictEv(
    IN      HAUTOCAPSCALL               hAutoCapsCall,
    IN      HCHAN                       hsChan,
    IN      cmH245ChannelConflictType   conflictType,
    IN      HCHAN                       *confChans,
    IN      int                         numConfChans,
    IN      RvPvtNodeId                 message);

/******************************************************************************
 * RvH245AutoCapsCallCapabilitiesExtEv
 * ----------------------------------------------------------------------------
 * General: This function should be called from inside the implementation of the
 *          application of the cmEvCallCapabilitiesExtT() callback.
 *          The application must fill the information object with all the parameters
 *          at hand.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hAutoCapsCall            - The handle to the H.245 AutoCaps call object.
 *                                    The application must set all the information 
 *                                    needed for the validation to this handle.
 * Output: None.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvH245AutoCapsCallCapabilitiesExtEv(
    IN  HAUTOCAPSCALL hAutoCapsCall);


/******************************************************************************
 * RvH245AutoCapsGetRemoteH263ResolutionEv
 * ----------------------------------------------------------------------------
 * General: This function is called by termH245AutoCapsUpdateCapInfo for obtaining
 *              the remote's h263 resolution info (which is set by validateNewCap).
 *
 * Return Value: autoCapsInfo->remoteH263Resolution
 *               
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hAutoCapsCall            - The handle to the H.245 AutoCaps call object.
 *                                    The application must set to this handle
 *                                    all the information needed for the
 *                                    validation.
 * Output: None
 *****************************************************************************/
RVAPI RvUint8 RVCALLCONV RvH245AutoCapsGetRemoteH263ResolutionEv(
    IN  HAUTOCAPSCALL hAutoCapsCall);

#ifdef __cplusplus
}
#endif

#endif /* _RV_H245_AUTO_CAPS_CALLBACKS_H_ */
