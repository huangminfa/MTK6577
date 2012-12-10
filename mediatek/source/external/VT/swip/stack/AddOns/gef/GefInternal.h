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

#ifndef _GEF_INTERNAL_H_
#define _GEF_INTERNAL_H_


/*-----------------------------------------------------------------------*/
/*                        INCLUDE HEADER FILES                           */
/*-----------------------------------------------------------------------*/

#include "RvGefGeneral.h"

#ifdef __cplusplus
extern "C" {
#endif

/*-----------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                            */
/*-----------------------------------------------------------------------*/
#define MAX_ENCODED_SIZE 20


/* PST handles */
extern HPST hSynExtendedVideoCap;
extern HPST hSynGenericCap;
extern HPST hSynGenericParam;
extern HPST hSynGenericMessage;

/******************************************************************************
 * GefGenericCapabilityInitializeEncodedCapIdArray
 * ----------------------------------------------------------------------------
 * General: Initialize the encoded CapabilityIdentifier array.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  none
 *****************************************************************************/
RvStatus RVCALLCONV GefGenericCapabilityInitializeEncodedCapIdArray(void);

/******************************************************************************
 * GefGenericMessageInitializeEncodedMessageIdArray
 * ----------------------------------------------------------------------------
 * General: Initialize the encoded MessageIdentifier array.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  none
 *****************************************************************************/
RvStatus RVCALLCONV GefGenericMessageInitializeEncodedMessageIdArray(void);

/******************************************************************************
 * GefGenericCapabilitySetEncodedCapId
 * ----------------------------------------------------------------------------
 * General: Set the encoded CapabilityIdentifier to the GenericCapability node
 *          according to the codec type.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node ids.
 *         genericCapNodeId     - The GenericCapability node id.
 *         eGefCodecType        - The codec type of the genericCapNodeId.
 *****************************************************************************/
RvStatus RVCALLCONV GefGenericCapabilitySetEncodedCapId(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         genericCapNodeId,
    IN  RvGefCodecType                      eGefCodecType);

/******************************************************************************
 * GefGenericCapabilityGetCodecType
 * ----------------------------------------------------------------------------
 * General: Get the coded type of the given GenericCapability according to the
 *          CapabilityIdentifier.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node ids.
 *         genericCapNodeId     - The GenericCapability node id.
 * Output: pGefCodecType        - The codec type of the genericCapNodeId.
 *****************************************************************************/
RvStatus RVCALLCONV GefGenericCapabilityGetCodecType(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         genericCapNodeId,
    OUT RvGefCodecType                      *pGefCodecType);

/******************************************************************************
 * GefGenericMessageSetEncodedCapId
 * ----------------------------------------------------------------------------
 * General: Set the encoded MessageIdentifier to the GenericMessage node
 *          according to the message type.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node ids.
 *         genericMessageNodeId - The GenericMessage node id.
 *         eGefCodecType        - The codec type of the genericMessageNodeId.
 *****************************************************************************/
RvStatus RVCALLCONV GefGenericMessageSetEncodedMessageId(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         genericMessageNodeId,
    IN  RvGefMessageType                    eGefMessageType);

/******************************************************************************
 * GefGenericMessageGetCodecType
 * ----------------------------------------------------------------------------
 * General: Get the coded type of the given GenericMessage according to the
 *          MessageIdentifier.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node ids.
 *         genericMessageNodeId - The GenericMessage node id.
 * Output: pGefMessageType      - The message type of the genericMessageNodeId.
 *****************************************************************************/
RvStatus RVCALLCONV GefGenericMessageGetMessageType(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         genericMessageNodeId,
    OUT RvGefMessageType                    *pGefMessageType);

/******************************************************************************
 * GefGetGenericParameterByIdentifier
 * ----------------------------------------------------------------------------
 * General: Get the generic parameter from GenericParameter sequence according
 *          to the parameterIdentifier.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                           - PVT handle for handling ASN.1 node ids.
 *         genericParamSequenceNodeId     - The GenericParameter sequence node id.
 *         eParamIdentifier               - The parameter identifier type.
 *         paramId                        - The parameter integer value. Used for the integer types like
 *                                          standard and h221NonStandard.
 *         pStrParamId                    - The parameter string value. Used for the string types like
 *                                          uuid and domainBased. Use the paramId argument to pass
 *                                          the size of the string.
 * Output: genericParamNodeId             - The generic parameter node id. This node id is a specific
 *                                          GenericParameter taken from the GenericParameter sequence.
 *****************************************************************************/
RvStatus RVCALLCONV GefGetGenericParameterByIdentifier(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         genericParamSequenceNodeId,
    IN  RvGefGenericParameterIdentifierType eParamIdentifier,
    IN  RvInt32                             paramId,
    IN  const RvUint8                       *pStrParamId,
    OUT RvPvtNodeId                         *genericParamNodeId);

/******************************************************************************
 * GefGetEncodedMessageType
 * ----------------------------------------------------------------------------
 * General: Get the encoded capability identifier and its size for the given
 *          capability.
 *
 * Return None
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  capabilityId                   - The codecType of the required capability/codec.
 *         encodedCapabilityId            - The encoded identifier of the given capability/codec.
 *         encodedCapabilitySize          - The encoded size.
 *****************************************************************************/
void RVCALLCONV GefGetEncodedMessageType(
       IN  RvGefMessageType                 capabilityId,
       OUT RvUint8                          *encodedCapabilityId,
       OUT RvUint32                         *encodedCapabilitySize);

/******************************************************************************
 * GefGetEncodedMessageType
 * ----------------------------------------------------------------------------
 * General: Get message type enumeration from the encoded capability id.
 *
 * Return None
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input: encodedCapabilityId            - The encoded identifier of the required messageType.
 *        encodedCapabilitySize          - The encoded sizeof the required messageType.
 * Output:capabilityId                   - The messageType for given encoded capability id.
 *****************************************************************************/
void RVCALLCONV GefGetMessageTypeFromOid(
        IN               RvUint8                          *encodedCapabilityId,
        IN               RvUint32                         encodedCapabilitySize,
        OUT              RvGefMessageType                 *capabilityId);


#ifdef __cplusplus
}
#endif

#endif /* _GEF_INTERNAL_H_ */
