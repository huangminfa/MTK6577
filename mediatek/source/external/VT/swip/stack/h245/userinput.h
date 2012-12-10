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
        Copyright (c) 2005 RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Ltd.. No part of this document may be reproduced in any
form whatsoever without written prior approval by RADVISION Ltd..

RADVISION Ltd. reserve the right to revise this publication and make
changes without obligation to notify any person of such revisions or
changes.
***********************************************************************/

#ifndef _USER_INPUT_H
#define _USER_INPUT_H

#include "rverror.h"

#ifdef __cplusplus
extern "C" {
#endif



typedef enum
{
    cmUserInputNonStandard = 0,
    cmUserInputAlphanumeric,
    cmUserInputSupport,
    cmUserInputSignal,
    cmUserInputSignalUpdate,
    cmUserInputExtendedAlphanumeric,
    cmUserInputEncryptedAlphanumeric,
    cmUserInputGenericInformation
} cmUserInputIndication;


typedef enum
{
    cmSupportNonStandard = 0,
    cmSupportBasicString, /* alphanumeric */
    cmSupportIA5String, /* alphanumeric */
    cmSupportGeneralString, /* alphanumeric */
    cmSupportDtmf, /* supports dtmf using signal and signalUpdate */
    cmSupportHookflash, /* supports hookflash using signal */
    cmSupportExtendedAlphanumeric,
    cmSupportEncryptedBasicString, /* encrypted Basic string in encryptedAlphanumeric */
    cmSupportEncryptedIA5String, /* encrypted IA5 string in encryptedSignalType */
    cmSupportEncryptedGeneralString, /* encrypted general string in extendedAlphanumeric.encryptedalphanumeric */
    cmSupportSecureDTMF /* secure DTMF using encryptedSignalType */
} cmUserInputSupportIndication;


typedef struct
{
    int timestamp;      /* -1 if optional; */
    int expirationTime; /* -1 if optional;*/
    int logicalChannelNumber;
} cmUserInputSignalRtpStruct;


typedef struct
{
    char                        signalType;
    int                         duration;
    cmUserInputSignalRtpStruct  cmUserInputSignalRtp;/* if logical channel == 0 -optional*/
} cmUserInputSignalStruct;


RVAPI int RVCALLCONV  /* userInput message node id or negative value on failure */
cmUserInputSupportIndicationBuild(
    /* Build userUser message with alphanumeric data */
    IN  HAPP                            hApp,
    IN  cmUserInputSupportIndication    userInputSupport,
    OUT int*                            nodeId);  /* nodeId of nonstandard UserInputSupportIndication */


RVAPI int RVCALLCONV  /* userInput message node id or negative value on failure */
cmUserInputSignalBuild(
    /* Build userUser message with alphanumeric data */
    IN  HAPP                        hApp,
    IN  cmUserInputSignalStruct*    userInputSignalStruct);


RVAPI int RVCALLCONV  /* userInput message node id or negative value on failure */
cmUserInputSignalUpdateBuild(
    /* Build userUser message with alphanumeric data */
    IN  HAPP                        hApp,
    IN  cmUserInputSignalStruct*    userInputSignalStruct);


RVAPI int RVCALLCONV  /* RV_TRUE or negative value on failure */
cmUserInputGetDetail(
    IN  HAPP                    hApp,
    IN  RvInt32                 userInputId,
    OUT cmUserInputIndication*  userInputIndication);

RVAPI int RVCALLCONV
cmUserInputGetSignal(
    IN  HAPP                        hApp,
    IN  RvInt32                     signalUserInputId,
    OUT cmUserInputSignalStruct*    userInputSignalStruct);

RVAPI int RVCALLCONV
cmUserInputGetSignalUpdate(
    IN  HAPP                        hApp,
    IN  RvInt32                     signalUserInputId,
    OUT cmUserInputSignalStruct*    userInputSignalStruct);

RVAPI int RVCALLCONV
cmUserInputSupportGet(
    IN  HAPP                            hApp,
    IN  RvInt32                         supportUserInputId,
    OUT cmUserInputSupportIndication*   userInputSupportIndication);

/******************************************************************************
 * cmUserInputBuildWithGenericInformation
 * ----------------------------------------------------------------------------
 * General: Create a userInputIndication message tree and add the 
 *          genericInformation node supplied by the user.
 *
 * Return Value: userInputIndication message PVT node ID on success.
 *               RV_PVT_INVALID_NODE_ID on failure. 
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hApp                 - The Stack handle for the application.
 *         genericInformation   - The PVT node ID of the genericInformation sub-tree
 *                                to be added to the message.
 * Output: None
 *****************************************************************************/ 
RVAPI RvPvtNodeId RVCALLCONV cmUserInputBuildWithGenericInformation(
    IN  HAPP            hApp,
    IN  RvPvtNodeId     genericInformation);

/******************************************************************************
 * cmUserInputGetGenericInformation
 * ----------------------------------------------------------------------------
 * General: Get the genericInformation node of the userInputIndication message..
 *
 * Return Value: RV_OK on success 
 *				 Negative value on error.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hApp                 - The Stack handle for the application. 
 *		   userInputId			- The userInput message PVT node.
 * Output: genericInformation   - The PVT node ID of the genericInformation sub-tree
 *                                to be added to the message.
 *****************************************************************************/ 
RVAPI RvStatus RVCALLCONV cmUserInputGetGenericInformation(
    IN  HAPP            hApp,
    IN  RvPvtNodeId     userInputId,
    OUT RvPvtNodeId	   *genericInformationNodeId);





#ifdef __cplusplus
}
#endif

#endif
