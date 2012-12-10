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

#ifndef _RV_GEF_H324_ANNEXI_H_
#define _RV_GEF_H324_ANNEXI_H_


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

/* AMR definitions according to H.245 / Annex I */

#define RV_GEF_H324_ANNEXI_CAPABILITY_IDENTIFIER "itu-t (0) recommendation (0) h (8) 324 generic-capabilities (1) 0"

#define RV_GEF_H324_ANNEXI_MODE_SERVER 1
#define RV_GEF_H324_ANNEXI_MODE_CLIENT 2
#define RV_GEF_H324_ANNEXI_MODE_SERVER_AND_CLIENT 3

#define RV_GEF_H324_ANNEXI_UNDERLYING_PROTOCOL_NONE 0
#define RV_GEF_H324_ANNEXI_UNDERLYING_PROTOCOL_TCP_IP_PPP 1

#define RV_GEF_H324_ANNEXI_PARAMETER_IDENTIFIER_MODE 0
#define RV_GEF_H324_ANNEXI_PARAMETER_IDENTIFIER_UNDERLYING_PROTOCOL 1 


/*-----------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                           */
/*-----------------------------------------------------------------------*/


/******************************************************************************
 * RvGefH324AnnexIBuild
 * ----------------------------------------------------------------------------
 * General: Builds the GenericCapability ASN.1 node ID for the H.324 Annex I 
 *          capability. After this node ID is built, it is the responsibility 
 *          of the application to delete it.
 *          Note that the Capability Identifier has already been set to this node ID.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt             - PVT handle for building ASN.1 node IDs.
 * Output: h324AnnexINodeId - The new GenericCapability node ID for the H.324 Annex capability.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefH324AnnexIBuild(
    IN  HPVT                    hPvt,
    OUT RvPvtNodeId             *h324AnnexINodeId);


/******************************************************************************
 * RvGefH324AnnexISetMaxBitRate
 * ----------------------------------------------------------------------------
 * General: Sets the maxBitRate parameter to the GenericCapability 
 *          ASN.1 node ID for the H.324 Annex I capability.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         h324AnnexINodeId     - The GenericCapability node ID for the H.324 Annex I capability.
 *         maxBitRate           - The maxBitRate parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefH324AnnexISetMaxBitRate(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         h324AnnexINodeId,
    IN  RvUint32                            maxBitRate);


/******************************************************************************
 * RvGefH324AnnexIGetMaxBitRate
 * ----------------------------------------------------------------------------
 * General: Gets the maxBitRate parameter from the GenericCapability 
 *          ASN.1 node ID of the H.324 Annex I capability.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         h324AnnexINodeId     - The GenericCapability node ID for the H.324 Annex I capability.
 * Output: pMaxBitRate          - The maxBitRate parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefH324AnnexIGetMaxBitRate(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         h324AnnexINodeId,
    OUT RvUint32                            *pMaxBitRate);


/******************************************************************************
 * RvGefH324AnnexIAddMode
 * ----------------------------------------------------------------------------
 * General: Adds the mode parameter to the GenericCapability ASN.1 node ID for 
 *          the H.324 Annex I capability.
 *          Note that the possible values are: RV_GEF_H324_ANNEXI_MODE_SERVER, 
 *          RV_GEF_H324_ANNEXI_MODE_CLIENT and RV_GEF_H324_ANNEXI_MODE_SERVER_AND_CLIENT.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         h324AnnexINodeId     - The GenericCapability node ID for the H.324 Annex I capability.
 *         mode                 - The mode parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefH324AnnexIAddMode(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         h324AnnexINodeId,
    IN  RvUint16                            mode);


/******************************************************************************
 * RvGefH324AnnexIGetMode
 * ----------------------------------------------------------------------------
 * General: Gets the mode parameter from the GenericCapability ASN.1 node ID 
 *          of the H.324 Annex I capability.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         h324AnnexINodeId     - The GenericCapability node ID for the H.324 Annex I capability.
 * Output: pMode                - The mode parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefH324AnnexIGetMode(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         h324AnnexINodeId,
    OUT RvUint16                            *pMode);


/******************************************************************************
 * RvGefH324AnnexIAddUnderlyingProtocol
 * ----------------------------------------------------------------------------
 * General: Adds the underlyingProtocol parameter to the GenericCapability 
 *          ASN.1 node ID for the H.324 Annex I capability.
 *          Note that the possible values are:
 *          RV_GEF_H324_ANNEXI_UNDERLYING_PROTOCOL_NONE and
 *          RV_GEF_H324_ANNEXI_UNDERLYING_PROTOCOL_TCP_IP_PPP.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         h324AnnexINodeId     - The GenericCapability node ID for the H.324 Annex I capability.
 *         underlyingProtocol   - The underlyingProtocol parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefH324AnnexIAddUnderlyingProtocol(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         h324AnnexINodeId,
    IN  RvUint16                            underlyingProtocol);


/******************************************************************************
 * RvGefH324AnnexIGetUnderlyingProtocol
 * ----------------------------------------------------------------------------
 * General: Gets the underlyingProtocol parameter from the GenericCapability 
 *          ASN.1 node ID of the H.324 Annex I capability.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         h324AnnexINodeId     - The GenericCapability node ID for the H.324 Annex I capability.
 * Output: pUnderlyingProtocol  - The underlyingProtocol parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefH324AnnexIGetUnderlyingProtocol(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         h324AnnexINodeId,
    OUT RvUint16                            *pUnderlyingProtocol);


#ifdef __cplusplus
}
#endif

#endif /* _RV_GEF_H324_ANNEXI_H_ */
