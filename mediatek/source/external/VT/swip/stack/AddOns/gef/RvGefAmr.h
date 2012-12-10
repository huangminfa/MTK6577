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

#ifndef _RV_GEF_AMR_H_
#define _RV_GEF_AMR_H_


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

#define RV_GEF_AMR_CAPABILITY_IDENTIFIER "itu-t(0) recommendation(0) h(8) 245 generic-capabilities(1) audio(1) amr(1)"

#define RV_GEF_AMR_MAX_BIT_RATE 122

#define RV_GEF_AMR_PARAMETER_IDENTIFIER_MAX_AL_SDU_FRAMES 0
#define RV_GEF_AMR_PARAMETER_IDENTIFIER_BIT_RATE 1 
#define RV_GEF_AMR_PARAMETER_IDENTIFIER_GSM_AMR_COMFORT_NOISE 2
#define RV_GEF_AMR_PARAMETER_IDENTIFIER_GSM_EFR_COMFORT_NOISE 3
#define RV_GEF_AMR_PARAMETER_IDENTIFIER_IS_641_COMFORT_NOISE 4
#define RV_GEF_AMR_PARAMETER_IDENTIFIER_PDC_EFR_COMFORT_NOISE 5


/* RvGefAmrComfortNoiseCapability
 * ------------------------------------------------------------------------
 * This enumeration describes the Comfort Noise capabilities.
 */
typedef enum
{
    RvGefAmrComfortNoiseCapabilityUnknown = -1,
    RvGefAmrComfortNoiseCapabilityGsmAmr,
    RvGefAmrComfortNoiseCapabilityGsmEfr,
    RvGefAmrComfortNoiseCapabilityIs641,
    RvGefAmrComfortNoiseCapabilityPdcEfr

} RvGefAmrComfortNoiseCapability;

/*-----------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                           */
/*-----------------------------------------------------------------------*/


/******************************************************************************
 * RvGefAmrBuild
 * ----------------------------------------------------------------------------
 * General: Builds the GenericCapability ASN.1 node ID for AMR codec. 
 *          If this node ID is built, it is the responsibility of the 
 *          application to delete it. Note that the Capability Identifier 
 *          and maxBitRate have already been set to this node ID.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt             - PVT handle for building ASN.1 node IDs.
 * Output: amrNodeId        - The new GenericCapability node ID for AMR codec.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefAmrBuild(
    IN  HPVT                    hPvt,
    OUT RvPvtNodeId             *amrNodeId);



/******************************************************************************
 * RvGefAmrAddMaxAlSduFrames
 * ----------------------------------------------------------------------------
 * General: Adds the maxAlSduFrames parameter to the GenericCapability 
 *          ASN.1 node ID for the AMR codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         amrNodeId            - The GenericCapability node ID for the AMR codec.
 *         maxAlSduFrames       - The maxAlSduFrames parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefAmrAddMaxAlSduFrames(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         amrNodeId,
    IN  RvUint16                            maxAlSduFrames);


/******************************************************************************
 * RvGefAmrGetMaxAlSduFrames
 * ----------------------------------------------------------------------------
 * General: Gets the maxAlSduFrames parameter from the GenericCapability 
 *          ASN.1 node ID of the AMR codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         amrNodeId            - The GenericCapability node ID for the AMR codec.
 * Output: pMaxAlSduFrames      - The maxAlSduFrames parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefAmrGetMaxAlSduFrames(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         amrNodeId,
    OUT RvUint16                            *pMaxAlSduFrames);


/******************************************************************************
 * RvGefAmrAddBitRate
 * ----------------------------------------------------------------------------
 * General: Adds the bitRate parameter to the GenericCapability ASN.1 
 *          node ID for the AMR codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         amrNodeId            - The GenericCapability node ID for the AMR codec.
 *         bitRate              - The bitRate parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefAmrAddBitRate(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         amrNodeId,
    IN  RvUint16                            bitRate);


/******************************************************************************
 * RvGefAmrGetBitRate
 * ----------------------------------------------------------------------------
 * General: Gets the bitRate parameter from the GenericCapability ASN.1 
 *          node ID of the AMR codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         amrNodeId            - The GenericCapability node ID for the AMR codec.
 * Output: pBitRate             - The bitRate parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefAmrGetBitRate(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         amrNodeId,
    OUT RvUint16                            *pBitRate);


/******************************************************************************
 * RvGefAmrAddComfortNoiseCapability
 * ----------------------------------------------------------------------------
 * General: Adds the Comfort Noise capability parameter to the 
 *          GenericCapability ASN.1 node ID for the AMR codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         amrNodeId            - The GenericCapability node ID for the AMR codec.
 *         eComfortNoiseCap     - The Comfort Noise capability.
 *         bCapabilityExists    - Defines whether the specified Comfort Noise 
 *                                capability is used.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefAmrAddComfortNoiseCapability(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         amrNodeId,
    IN  RvGefAmrComfortNoiseCapability      eComfortNoiseCap,
    IN  RvBool                              bCapabilityExists);


/******************************************************************************
 * RvGefAmrGetComfortNoiseCapability
 * ----------------------------------------------------------------------------
 * General: Gets the Comfort Noise capability parameter from the 
 *          GenericCapability ASN.1 node ID of the AMR codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         amrNodeId            - The GenericCapability node ID for the AMR codec.
 *         eComfortNoiseCap     - The Comfort Noise capability.
 * Output: pCapabilityExists    - The current support of Comfort Noise capability.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefAmrGetComfortNoiseCapability(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         amrNodeId,
    IN  RvGefAmrComfortNoiseCapability      eComfortNoiseCap,
    OUT RvBool                              *pCapabilityExists);


#ifdef __cplusplus
}
#endif

#endif /* _RV_GEF_AMR_H_ */
