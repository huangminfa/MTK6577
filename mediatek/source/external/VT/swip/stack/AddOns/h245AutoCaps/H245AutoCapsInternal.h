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

#ifndef _H245_AUTO_CAPS_TYPES_INTERNAL_H_
#define _H245_AUTO_CAPS_TYPES_INTERNAL_H_


/*-----------------------------------------------------------------------*/
/*                        INCLUDE HEADER FILES                           */
/*-----------------------------------------------------------------------*/

#include "rvtypes.h"
#if (RV_H245_SUPPORT_H223_PARAMS == 1)
#include "Rv3G324m.h"
#else
#include "cm.h"
#endif /* (RV_H245_SUPPORT_H223_PARAMS == 1) */
#include "RvH245AutoCapsTypes.h"
#include "rvlog.h"

#ifdef __cplusplus
extern "C" {
#endif

/*-----------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                            */
/*-----------------------------------------------------------------------*/

#define MAX_CHANNEL_REJECTIONS 2 /* Number of times that we allow the remote terminal
                                    reject a channel before the local terminal stop
                                    opening the channel. */
    
/* ACP */

/* RvH245AutoCapsMONAStatus
 * ------------------------------------------------------------------------
 * This enumeration Defines the MONA status of the call's autoCaps.*/
typedef enum
{
    RvH245AutoCapsACPStatusOff,
    RvH245AutoCapsACPStatusOn
}RvH245AutoCapsACPStatus;

/* RvH245AutoCaps
 * ------------------------------------------------------------------------
 * This structure defines the information needed for the H.245 AutoCaps
 * module. This structure is used by all H.245 AutoCaps instances.
 */
typedef struct
{
    HPST                            hPstDataType;                  /* The DataType PST handle */
    RvLogSource                     hLog;                          /* Log manager for log printing. */
    RvH245AutoCapsValidateFunctions *validateFunctionsArray;       /* The validation function provided by the application to match caps and get
                                                                      GCD of caps which are not supported in this add-on */
    RvUint32                        numberOfValidationFunctions;   /* Maximum number of validation functions allocated */
#ifdef USE_H263_SWITCHING_VIDEO_SIZE
    HPST                            rmH223ModeParameterSyn;
#endif
} RvH245AutoCaps;

#ifdef USE_H263_SWITCHING_VIDEO_SIZE
/* RvRMState
 * ------------------------------------------------------------------------
 * This structure defines the state of the requestMode procedure
 */
typedef enum
{
    rmStateInactive = 0, /* Not in the process of closing a channel */
    rmStateClosing    
}RvRMState;

#endif

/* RvH245AutoCapsCall
 * ------------------------------------------------------------------------
 * This structure defines the information that the application should pass
 * to in each API call.
 */
typedef struct
{
    RvH245AutoCaps                              *autoCaps;   /* The H.245 AutoCaps object */
    HCALL                                       hCall;       /* The handle of the call. */
    HCHAN                                       hAudioChan;  /* The handle of the outgoing audio channel. */
    HCHAN                                       hVideoChan;  /* The handle of the outgoing video channel. */
    HCHAN                                       hVideo2Chan; /* The handle of the second outgoing video channel - not
                                                                in use currently. */
    HPVT                                        hPvt;        /* The PVT handle used for PVT manipulations. */
    void                                        *context;    /* The context handle provided by application
                                                                that can then be used in validation functions
                                                                and in callbacks. */
    RvH245AutoCapsGetNewChannelEv               pfnGetNewChannelEv; /* Event handler of application when new channel
                                                                       handle is needed by the add-on. */
    RvH245AutoCapsUpdateCapabilityInformationEv pfnUpdateCapabilityInformationEv; /* Event handler of application that is
                                                                                     used when the application wants to update parameters
                                                                                     to a dataType of an OLC just before the opening. */
    RvBool                                      bOpenNewAudioChannel;    /* Defines an indication where an an audio channel gets closed
                                                                            and a new channel should be opened instead. */
    RvBool                                      bOpenNewVideoChannel;    /* Defines an indication where an an video channel gets closed
                                                                            and a new channel should be opened instead. */
    RvBool                                      bEmptyTCS;   /* Defines whether the stack received an empty TCS message. */
    RvBool                                      bLocalEmptyTCS;   /* Defines whether the stack sent an empty TCS message. */
    RvPvtNodeId                                 incomingAudioDataType;   /* Defines the dataType of the incoming audio channel. We keep it
                                                                            for the reopening of our channel after our previous channel was rejected. */
    RvPvtNodeId                                 incomingVideoDataType;   /* Defines the dataType of the incoming video channel. We keep it
                                                                            for the reopening of our channel after our previous channel was rejected. */
    RvBool                                      bMimicAudioChannel;    /* Defines an indication where an audio channel gets rejected from unknown
                                                                          and a new channel should be opened instead. */
    RvBool                                      bMimicVideoChannel;    /* Defines an indication where a video channel gets rejected from unknown
                                                                          and a new channel should be opened instead. */
    RvBool                                      bMimicVideoReverseChannel;    /* Defines an indication where a bi-directional video channel gets rejected
                                                                                 due to unsuitableReverseParameters reason and a new channel should be
                                                                                 opened instead. */
    RvUint8                                     audioReject;            /* Defines the number of rejections for the audio outgoing channel. */
    RvUint8                                     videoReject;            /* Defines the number of rejections for the video outgoing channel. */

    RvUint8                                     remoteH263Resolution; /* resolutions supported by remote
                                                                                                sqcif = 1, qcif = 2, cif = 4, ... */

    RvBool                                      bAudioCapFound;         /* Check if no simultaneous audio and video capabilities were found but only audio */
    RvBool                                      bVideoCapFound;         /* Check if no simultaneous audio and video capabilities were found but only video */

#ifdef USE_H263_SWITCHING_VIDEO_SIZE
    RvPvtNodeId                                 requestedVideoAl;                    /* node of h223 logical channel parameters */                
    RvPvtNodeId                                 rmDataTypeNodeId;       /* videoData/audioData nodeId */
    RvPvtNodeId                                 rmReverseDataTypeNodeId;/* dataType nodeId */
    RvBool                                      bRMOpenNewVideoChan;    /* True if a new video channel is requested by requestMode */
    RvBool                                      bRMOpenNewAudioChan;
    RvRMState                                   rmState;
    HCHAN                                       rmAbandonVideoChan;
    HCHAN                                       rmAbandonAudioChan;    
#endif
    /* ACP */
    RvH245AutoCapsACPStatus                     ACPStatus;              /*The status of the autoCaps regarding the ACP*/
    RvBool                                      bVideoAL3Only;          /* Defines whether only AL3 video channel is supported by both sides */
    RvBool                                      bOpenNullData;          /* Defines whether Null data channel should be opened */

	/* Non standard channels */
	RvPvtNodeId									nonStandardNodeId;      /* The node id to be used in outgoing channel in case that
																		   the remote side acted in a non standard way */
    RvInt32  									nonStandardCapEntryNum; /* The capability entry number in cap set of the non standard channel */

    /* Do not change the following parameters */
    RvBool                                      bSessionResetCapPresent;  /* Defines whether SessionReset is supported by both sides. */
    RvUint16                                    maximumAl2SDUSize;   /* Holds the incoming maximumAl2SDUSize parameter value. */
    RvUint16                                    maximumAl3SDUSize;   /* Holds the incoming maximumAl3SDUSize parameter value. */
    RvUint16                                    maximumDelayJitter;  /* Holds the incoming maximumDelayJitter parameter value. */
} RvH245AutoCapsCall;


/*-----------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                           */
/*-----------------------------------------------------------------------*/

/******************************************************************************
 * H245AutoCapsValidateNewCaps
 * ----------------------------------------------------------------------------
 * General: Validate the capabilities with the new received capabilities.
 *          If the already opened channels do not fit anymore, they will be
 *          closed and reopened with new capabilities.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hAutoCapsCall            - The handle to the H.245 AutoCaps call object.
 *                                    The application must set to this handle
 *                                    all the information needed for the
 *                                    validation.
 * Output: None.
 *****************************************************************************/
RvStatus RVCALLCONV H245AutoCapsValidateNewCaps(
    IN     HAUTOCAPSCALL              hAutoCapsCall);

/******************************************************************************
 * H245AutoCapsValidateIncomingChannel
 * ----------------------------------------------------------------------------
 * General: Validate that the capability of the incoming channel is under
 *          the constraints of capabilities from both sides. If it is valid
 *          and the outgoing channel hasn't been opened yet then the outgoing
 *          channel will use the capability of the incoming channel.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hAutoCapsCall            - The handle to the H.245 AutoCaps call object.
 *                                    The application must set to this handle
 *                                    all the information needed for the
 *                                    validation.
 *         hChan                    - The handle of the incoming channel.
 * Output: eChannelResponse         - The recommended channel response. If the recommended
 *									  response is Reject, then the reject reason can be found in
 *									  eRejectReason parameter.
 *         eRejectReason            - The reject reason of the channel. This parameter should be considered
 *									  only if eChannelResponse parameter recommends to reject the channel.
 *									  Otherwise, this parameter should be ignored.
 *****************************************************************************/
RvStatus RVCALLCONV H245AutoCapsValidateIncomingChannel(
    IN     HAUTOCAPSCALL				  hAutoCapsCall,
    IN     HCHAN						  hChan,
	OUT    RvH245AutoCapsChannelResponse  *eChannelResponse,
	OUT	   cmRejectLcnReason			  *eRejectReason);

#ifdef __cplusplus
}
#endif

#endif /* _H245_AUTO_CAPS_TYPES_INTERNAL_H_ */
