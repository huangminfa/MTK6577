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

#ifndef _H245_AUTO_CAPS_H223_H_
#define _H245_AUTO_CAPS_H223_H_


/*-----------------------------------------------------------------------*/
/*                        INCLUDE HEADER FILES                           */
/*-----------------------------------------------------------------------*/

#include "H245AutoCapsInternal.h"
#include "RvH245AutoCapsValidation.h"

#ifdef __cplusplus
extern "C" {
#endif

#if (RV_H245_SUPPORT_H223_PARAMS == 1)
/*-----------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                            */
/*-----------------------------------------------------------------------*/


/*-----------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                           */
/*-----------------------------------------------------------------------*/

/******************************************************************************
 * H245AutoCapsValidateAL
 * ----------------------------------------------------------------------------
 * General: Validate adaptation layers of both sides. This function take into
 *          consideration the preferences of the application provided in the HCHAN
 *          objects.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  eChannelType             - Channel type to validate.
 *         autoCapsInfo             - The information structure that holds
 *                                    all the information needed for the validation.
 *         localCapsNodeId          - The local TCS message ASN.1 node id.
 *         remoteCapsNodeId         - The remote TCS message ASN.1 node id.
 *         hInChan                  - The incoming channel handle.
 * Output: None.
 *****************************************************************************/
RvStatus RVCALLCONV H245AutoCapsValidateAL(
    IN     cmChannelDataType          eChannelType,
    IN     RvH245AutoCapsCall         *autoCapsInfo,
    IN     RvPvtNodeId                localCapsNodeId,
    IN     RvPvtNodeId                remoteCapsNodeId,
    IN     HCHAN                      hInChan);

/******************************************************************************
 * H245AutoCapsValidateSessionReset
 * ----------------------------------------------------------------------------
 * General: Validate the existence of SessionResetCapability inside the
 *          incoming and outgoing TCS messages.
 *
 * Return Value: RV_OK  - if both sides support SessionResetCapability.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  autoCapsInfo             - The information structure that holds
 *                                    all the information needed for the validation.
 *         localCaps                - The local capability table.
 *         remoteCaps               - The remote capability table.
 * Output: None.
 *****************************************************************************/
RvStatus RVCALLCONV H245AutoCapsValidateSessionReset(
    IN     RvH245AutoCapsCall         *autoCapsInfo,
    IN     cmCapStruct                **localCaps,
    IN     cmCapStruct                **remoteCaps);
/******************************************************************************
 * H245AutoCapsGetTCSGenericInformation
 * ----------------------------------------------------------------------------
 * General: Get the generic information from the remote TCS.
 *
 * Return Value: RV_OK  - On success.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:	autoCapsInfo			  - The information structure that holds
 *										all the information needed for the validation.
 *			remoteCapsNodeId          - The TCS node id.
 * Output:	genericInformationId      - The generic information nodeId of thr TCS.
 *****************************************************************************/
RvStatus RVCALLCONV H245AutoCapsGetTCSGenericInformation(
	IN     RvH245AutoCapsCall       *autoCapsInfo,
    IN     RvPvtNodeId               remoteCapsNodeId,
	OUT    RvPvtNodeId				*genericInformationId);

/******************************************************************************
 * H245AutoCapsValidateMONA
 * ----------------------------------------------------------------------------
 * General: Validate the existence of MONA capability inside the
 *          incoming and outgoing TCS messages and sets the ACPstatus and
 *          set receiving mux table entries in the call.
 *
 * Return Value: RV_OK  - if both sides support MONA capability.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  autoCapsInfo             - The information structure that holds
 *                                    all the information needed for the validation.
 *         genericInformationNodeId	- The genericInformation nodeId in the TCS.
 * Output: None.
 *****************************************************************************/
RvStatus H245AutoCapsValidateMONA(
    IN     RvH245AutoCapsCall         *autoCapsInfo,
    IN	   RvPvtNodeId				   genericInformationNodeId);

/******************************************************************************
 * H245AutoCapsGetH223GenericInformation
 * ----------------------------------------------------------------------------
 * General: Get H.223 generic information from the remote TCS and fill the
 *          information object.
 *
 * Return Value: RV_OK  - if both sides support SessionResetCapability.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  autoCapsInfo             - The information structure that holds
 *         remoteCapsNodeId          - The TCS node id.
 * Output: None.
 *****************************************************************************/
RvStatus RVCALLCONV H245AutoCapsGetH223GenericInformation(
    IN     RvH245AutoCapsCall         *autoCapsInfo,
    IN     RvPvtNodeId                remoteCapsNodeId);

#endif  /* RV_H245_SUPPORT_H223_PARAMS */

#ifdef __cplusplus
}
#endif

#endif /* RV_H245_AUTO_CAPS_H223 */
