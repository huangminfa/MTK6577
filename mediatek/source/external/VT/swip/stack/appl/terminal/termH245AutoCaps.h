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

#ifndef _RV_TERM_H245_AUTO_CAPS_H_
#define _RV_TERM_H245_AUTO_CAPS_H_

/***********************************************************************
termH245AutoCaps.h

This module inside the test application is responsible for dealing with
the automatic capability validation that is required for handling
various codecs and features in 3G-324M.
It requires the H.245 AutoCaps and GEF add-ons to work.
***********************************************************************/


/*-----------------------------------------------------------------------*/
/*                        INCLUDE HEADER FILES                           */
/*-----------------------------------------------------------------------*/
#include "termDefs.h"




#ifdef __cplusplus
extern "C" {
#endif


#if defined(USE_H245AUTOCAPS) && defined(USE_GEF)



/*-----------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                            */
/*-----------------------------------------------------------------------*/

#define MAX_NUMBER_OF_VALIDATE_FUNCTIONS 5

typedef struct
{
    HCALL                                       hCall;
    HCHAN                                       hAudioChan;
    HCHAN                                       hVideoChan;
    HCHAN                                       hVideo2Chan;
    HPVT                                        hPvt;
    void                                        *context;
    RvH245AutoCapsEvHandlers                    pfnEvHandlers;

    /* Do not change the following parameters */
    RvBool                                      bSessionResetCapPresent;
    RvUint16                                    maximumAl2SDUSize;
    RvUint16                                    maximumAl3SDUSize;
    RvUint16                                    maximumDelayJitter;
}H245AutoCapsParams;



/*-----------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                           */
/*-----------------------------------------------------------------------*/


/******************************************************************************
 * termH245AutoCapsInit
 * ----------------------------------------------------------------------------
 * General: Initialize the use of H.245 AutoCaps module.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term                           - The terminal object.
 *         maxNumberOfValidationFunctions - Maximum number of validation functions.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsInit(
    IN  TermObj          *term,
    IN  RvUint32         maxNumberOfValidationFunctions);

/******************************************************************************
 * termH245AutoCapsEnd
 * ----------------------------------------------------------------------------
 * General: End the H.245 AutoCaps module.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term     - The terminal object.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsEnd(
    IN  TermObj          *term);

/******************************************************************************
 * termH245AutoCapsGetSize
 * ----------------------------------------------------------------------------
 * General: Get the size of H.245 AutoCaps call object.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  None.
 * Output: objectSize - The object size.
 *****************************************************************************/
RvStatus termH245AutoCapsGetSize(
    OUT  RvUint32         *objectSize);

/******************************************************************************
 * termH245AutoCapsConstruct
 * ----------------------------------------------------------------------------
 * General: Construct the use of H.245 AutoCaps for a call in the test
 *          application.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  termCall               - Terminal call object to use.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsConstruct(
    IN  TermCallObj         *termCall);

/******************************************************************************
 * termH245AutoCapsCopyConstruct
 * ----------------------------------------------------------------------------
 * General: Construct the use of H.245 AutoCaps for a call in the test
 *          application as a copy of another call's AutoCaps.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  copyToCall             - Terminal call object to use.
 *         copyFromCall           - Terminal call object to use.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsCopyConstruct(
    IN  TermCallObj         *copyToCall,
    IN  TermCallObj         *copyFromCall);

/******************************************************************************
 * termH245AutoCapsDestruct
 * ----------------------------------------------------------------------------
 * General: Destruct using H.245 AutoCaps for a specific call.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  termCall            - Terminal call object to use.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsDestruct(
    IN TermCallObj          *termCall);

/******************************************************************************
 * termH245AutoCapsReset
 * ----------------------------------------------------------------------------
 * General: Reset using H.245 AutoCaps when the call is being reset.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  termCall            - Terminal call object to use.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsReset(
    IN TermCallObj          *termCall);

/******************************************************************************
 * termH245AutoCapsChannelStateChangeEv
 * ----------------------------------------------------------------------------
 * General: H.245 AutoCaps implementation for cmEvChannelStateChanged callback.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  termCall            - Terminal call object to use.
 *         chan                - Terminal channel object to use.
 *         eState              - The state of the channel.
 *         eStateMode          - The state mode of the channel.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsChannelStateChangeEv(
    IN TermCallObj          *termCall,
    IN TermChannelObj       *termChan,
    IN cmChannelState_e     eState,
    IN cmChannelStateMode_e eStateMode);

/******************************************************************************
 * termH245AutoCapsChannelMasterSlaveConflictEv
 * ----------------------------------------------------------------------------
 * General: H.245 AutoCaps implementation for cmEvChannelMasterSlaveConflict
 *          callback.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  termCall            - Terminal call object to use.
 *         hChan               - Handle to the channel.
 *         conflictType        - The conflict type to handle.
 *         confChans           - List of conflicting channels.
 *         numConfChans        - Number of conflicting channels.
 *         message             - The message node Id.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsChannelMasterSlaveConflictEv(
    IN      TermCallObj                 *termCall,
    IN      HCHAN                       hChan,
    IN      cmH245ChannelConflictType   conflictType,
    IN      HCHAN                       *confChans,
    IN      int                         numConfChans,
    IN      RvPvtNodeId                 message);

/******************************************************************************
 * termH245AutoCapsCallCapabilitiesExtEv
 * ----------------------------------------------------------------------------
 * General: H.245 AutoCaps implementation for cmEvCallCapabilitiesExt
 *          callback.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  termCall            - Terminal call object to use.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsCallCapabilitiesExtEv(
    IN      TermCallObj                 *termCall);

/******************************************************************************
 * termH245AutoCapsSetParam
 * ----------------------------------------------------------------------------
 * General: Setting H.245 AutoCaps parameters.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  termCall            - Terminal call object to use.
 *         params              - The params to be set to the module.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsSetParam(
    IN      TermCallObj                 *termCall,
    IN      H245AutoCapsParams          *params);

/******************************************************************************
 * MTK patch: termH245AutoCapsSetParam
 * ----------------------------------------------------------------------------
 * General: Setting H.245 AutoCaps parameters.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  termCall            - Terminal call object to use.
 *         params              - The params to be set to the module.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsSetParam_bOpenNullData(
    IN      TermCallObj                 *termCall,
    IN      H245AutoCapsParams          *params);

/******************************************************************************
 * termH245AutoCapsSetValidateFunctions
 * ----------------------------------------------------------------------------
 * General: Setting H.245 AutoCaps validate functions.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term - The terminal object.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsSetValidateFunctions(
    IN TermObj  *term);

/******************************************************************************
 * termH245AutoCapsGetParam
 * ----------------------------------------------------------------------------
 * General: Getting H.245 AutoCaps parameters.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  termCall            - Terminal call object to use.
 *         params              - The params to be set to the module.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsGetParam(
    IN      TermCallObj                 *termCall,
    INOUT   H245AutoCapsParams          *params);

/******************************************************************************
 * termH245AutoCapsChannelOpen
 * ----------------------------------------------------------------------------
 * General: Open a new outgoing audio/video channels.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call                 - The call object.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsChannelOpen(
    IN TermCallObj *call);

/******************************************************************************
 * termH245AutoCapsChannelVerifyReset
 * ----------------------------------------------------------------------------
 * General: Verify that both sides can reset the session.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  call                 - The call object.
 * Output: None.
 *****************************************************************************/
RvStatus termH245AutoCapsChannelVerifyReset(TermCallObj *call);



#else
/* !defined(USE_H245AUTOCAPS && USE_GEF) */
#define termH245AutoCapsInit(_term, _maxNumberOfValidationFunctions) RV_OK
#define termH245AutoCapsEnd(_term)
#define termH245AutoCapsGetSize(_objectSize) RV_OK
#define termH245AutoCapsConstruct(_termCall) RV_OK
#define termH245AutoCapsDestruct(_termCall) RV_OK
#define termH245AutoCapsReset(_termCall)
#define termH245AutoCapsOpenChannels(_termCall, _channelType) RV_OK
#define termH245AutoCapsChannelStateChangeEv(_termCall, _chan, _state, _stateMode)
#define termH245AutoCapsChannelMasterSlaveConflictEv(_termCall, _chan, _conflictType, _confChans, _numOfChans, _message) RV_OK
#define termH245AutoCapsCallCapabilitiesExtEv(_termCall)
#define termH245AutoCapsSetParam(_termCall, _params) RV_OK
#define termH245AutoCapsSetValidateFunctions(_term) RV_OK
#define termH245AutoCapsChannelOpen(_termCall) RV_OK
#define termH245AutoCapsChannelVerifyReset(_termCall) RV_OK
#endif  /* defined(USE_H245AUTOCAPS && USE_GEF) */


#ifdef __cplusplus
}
#endif

#endif /* _RV_TERM_H245_AUTO_CAPS_H_ */
