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

#ifndef _RV_GEF_MPEG4_H_
#define _RV_GEF_MPEG4_H_


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

/* MPEG4 definitions according to H.245/Annex E. */

#define RV_GEF_MPEG4_CAPABILITY_IDENTIFIER "itu-t(0) recommendation(0) h(8) 245 generic-capabilities(1) video(0) ISO/IEC 14496-2(0)"

#define RV_GEF_MPEG4_PARAMETER_IDENTIFIER_PROFILE_AND_LEVEL 0
#define RV_GEF_MPEG4_PARAMETER_IDENTIFIER_OBJECT 1 
#define RV_GEF_MPEG4_PARAMETER_IDENTIFIER_DECODER_CONFIGURATION_INFORMATION 2
#define RV_GEF_MPEG4_PARAMETER_IDENTIFIER_DRAWING_ORDER 3
#define RV_GEF_MPEG4_PARAMETER_IDENTIFIER_VISUAL_BACK_CHANNEL_HANDLE 4
    


/*-----------------------------------------------------------------------*/
/*                           FUNCTION HEADERS                           */
/*-----------------------------------------------------------------------*/


/******************************************************************************
 * RvGefMpeg4Build
 * ----------------------------------------------------------------------------
 * General: Builds the GenericCapability ASN.1 node ID for the MPEG4 codec. 
 *          If this node ID is built, it is the responsibility of the application 
 *          to delete it. Note that the Capability Identifier has already been 
 *          set to this node ID.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt             - PVT handle for building ASN.1 node IDs.
 * Output: amrNodeId        - The new GenericCapability node ID for the MPEG4 codec.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefMpeg4Build(
    IN  HPVT                    hPvt,
    OUT RvPvtNodeId             *mpeg4NodeId);



/******************************************************************************
 * RvGefMpeg4SetMaxBitRate
 * ----------------------------------------------------------------------------
 * General: Sets the maxBitRate parameter to the GenericCapability 
 *          ASN.1 node ID for the MPEG4 codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         mpeg4NodeId          - The GenericCapability node ID for the MPEG4 codec.
 *         maxBitRate           - The maxBitRate parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefMpeg4SetMaxBitRate(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         mpeg4NodeId,
    IN  RvUint32                            maxBitRate);


/******************************************************************************
 * RvGefMpeg4GetMaxBitRate
 * ----------------------------------------------------------------------------
 * General: Gets the maxBitRate parameter from the GenericCapability 
 *          ASN.1 node ID of the MPEG4 codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         mpeg4NodeId          - The GenericCapability node ID for the MPEG4 codec.
 * Output: pMaxBitRate          - The maxBitRate parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefMpeg4GetMaxBitRate(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         mpeg4NodeId,
    OUT RvUint32                            *pMaxBitRate);


/******************************************************************************
 * RvGefMpeg4AddProfileAndLevel
 * ----------------------------------------------------------------------------
 * General: Adds the profileAndLevel parameter to the GenericCapability 
 *          ASN.1 node ID for the MPEG4 codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         mpeg4NodeId          - The GenericCapability node ID for the MPEG4 codec.
 *         profileAndLevel      - The profileAndLevel parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefMpeg4AddProfileAndLevel(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         mpeg4NodeId,
    IN  RvUint8                             profileAndLevel);


/******************************************************************************
 * RvGefMpeg4GetProfileAndLevel
 * ----------------------------------------------------------------------------
 * General: Gets the profileAndLevel parameter from the GenericCapability 
 *          ASN.1 node ID of the MPEG4 codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         mpeg4NodeId          - The GenericCapability node ID for the MPEG4 codec.
 * Output: pProfileAndLevel     - The profileAndLevel parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefMpeg4GetProfileAndLevel(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         mpeg4NodeId,
    OUT RvUint8                             *pProfileAndLevel);


/******************************************************************************
 * RvGefMpeg4AddObject
 * ----------------------------------------------------------------------------
 * General: Adds the object parameter to the GenericCapability ASN.1 node ID 
 *          for the MPEG4 codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         mpeg4NodeId          - The GenericCapability node ID for the MPEG4 codec.
 *         object               - The object parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefMpeg4AddObject(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         mpeg4NodeId,
    IN  RvUint8                             object);


/******************************************************************************
 * RvGefMpeg4GetObject
 * ----------------------------------------------------------------------------
 * General: Gets the object parameter from the GenericCapability ASN.1 node ID 
 *          of the MPEG4 codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         mpeg4NodeId          - The GenericCapability node ID for the MPEG4 codec.
 * Output: pObject              - The object parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefMpeg4GetObject(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         mpeg4NodeId,
    OUT RvUint8                             *pObject);


/******************************************************************************
 * RvGefMpeg4AddDecoderConfigurationInformation
 * ----------------------------------------------------------------------------
 * General: Adds the decoderConfigurationInformation parameter to the 
 *          GenericCapability ASN.1 node ID for the MPEG4 codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         mpeg4NodeId          - The GenericCapability node ID for the MPEG4 codec.
 *         pDecoderConfInfo     - The decoderConfigurationInformation parameter.
 *         decoderConfInfoSize  - The size of the pDecoderConfInfo string, in bytes.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefMpeg4AddDecoderConfigurationInformation(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         mpeg4NodeId,
    IN  const RvUint8                       *pDecoderConfInfo,
    IN  RvUint32                            decoderConfInfoSize);


/******************************************************************************
 * RvGefMpeg4GetDecoderConfigurationInformation
 * ----------------------------------------------------------------------------
 * General: Sets the decoderConfigurationInformation parameter to the 
 *          GenericCapability ASN.1 node ID for the MPEG4 codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         mpeg4NodeId          - The GenericCapability node IDd for the MPEG4 codec.
 * Output: pDecoderConfInfo     - The decoderConfigurationInformation parameter.
 *                                The string should be allocated by the application.
 *                                The pDecoderConfInfoSize argument will be filled with
 *                                the actual size of the string.
 *                                If this argument is set to NULL, the application can 
 *                                use the size returned in pDecoderConfInfoSize and call this
 *                                function again with the buffer allocated in the correct size.
 *         pDecoderConfInfoSize - The size of the pDecoderConfInfo string, in bytes.
 *                                Use this argument to pass the size of pDecoderConfInfo
 *                                that was allocated.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefMpeg4GetDecoderConfigurationInformation(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         mpeg4NodeId,
    OUT RvUint8                             *pDecoderConfInfo,
    INOUT RvUint32                          *pDecoderConfInfoSize);


/******************************************************************************
 * RvGefMpeg4AddDrawingOrder
 * ----------------------------------------------------------------------------
 * General: Adds the drawingOrder parameter to the GenericCapability 
 *          ASN.1 node ID for the MPEG4 codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         mpeg4NodeId          - The GenericCapability node ID for the MPEG4 codec.
 *         drawingOrder         - The drawingOrder parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefMpeg4AddDrawingOrder(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         mpeg4NodeId,
    IN  RvUint16                            drawingOrder);


/******************************************************************************
 * RvGefMpeg4GetDrawingOrder
 * ----------------------------------------------------------------------------
 * General: Gets the drawingOrder parameter from the GenericCapability 
 *          ASN.1 node ID of the MPEG4 codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                 - PVT handle for handling ASN.1 node IDs.
 *         mpeg4NodeId          - The GenericCapability node ID for the MPEG4 codec.
 * Output: pDrawingOrder        - The drawingOrder parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefMpeg4GetDrawingOrder(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         mpeg4NodeId,
    OUT RvUint16                            *pDrawingOrder);


/******************************************************************************
 * RvGefMpeg4AddVisualBackChannelHandle
 * ----------------------------------------------------------------------------
 * General: Adds the visualBackChannelHandle parameter to the 
 *          GenericCapability ASN.1 node ID for the MPEG4 codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                      - PVT handle for handling ASN.1 node IDs.
 *         mpeg4NodeId               - The GenericCapability node ID for the MPEG4 codec.
 *         bVisualBackChannelHandle  - The visualBackChannelHandle parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefMpeg4AddVisualBackChannelHandle(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         mpeg4NodeId,
    IN  RvBool                              bVisualBackChannelHandle);


/******************************************************************************
 * RvGefMpeg4GetVisualBackChannelHandle
 * ----------------------------------------------------------------------------
 * General: Gets the visualBackChannelHandle parameter from the 
 *          GenericCapability ASN.1 node ID of the MPEG4 codec.
 *
 * Return Value: RV_OK if successful. Other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hPvt                      - PVT handle for handling ASN.1 node IDs.
 *         mpeg4NodeId               - The GenericCapability node ID for the MPEG4 codec.
 * Output: pbVisualBackChannelHandle - The visualBackChannelHandle parameter.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV RvGefMpeg4GetVisualBackChannelHandle(
    IN  HPVT                                hPvt,
    IN  RvPvtNodeId                         mpeg4NodeId,
    OUT RvBool                              *pbVisualBackChannelHandle);


#ifdef __cplusplus
}
#endif

#endif /* _RV_GEF_MPEG4_H_ */
