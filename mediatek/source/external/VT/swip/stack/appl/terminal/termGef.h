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

#ifndef _RV_TERM_GEF_H_
#define _RV_TERM_GEF_H_

/***********************************************************************
termGef.h

This module inside the test application is responsible for dealing with
the Generic Extensibility Framework that is required for handling
various codecs and features in 3G-324M.
It requires the GEF add-on to work.
***********************************************************************/


/*-----------------------------------------------------------------------*/
/*                        INCLUDE HEADER FILES                           */
/*-----------------------------------------------------------------------*/
#include "termDefs.h"




#ifdef __cplusplus
extern "C" {
#endif


#if defined(USE_GEF)



/*-----------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                            */
/*-----------------------------------------------------------------------*/





/*-----------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                           */
/*-----------------------------------------------------------------------*/


/******************************************************************************
 * termGefInit
 * ----------------------------------------------------------------------------
 * General: Initialize the use of GEF by the test application.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term             - Terminal object to use.
 * Output: None.
 *****************************************************************************/
RvStatus termGefInit(
    IN  TermObj         *term);


/******************************************************************************
 * termGefEnd
 * ----------------------------------------------------------------------------
 * General: Stop using GEF.
 *
 * Return Value: RV_OK on success, negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term             - Terminal object to use.
 * Output: None.
 *****************************************************************************/
RvStatus termGefEnd(
    IN TermObj          *term);


/******************************************************************************
 * termGefFindRole
 * ----------------------------------------------------------------------------
 * General: Find out the role of an incoming OLC request's data type.
 *          The role is primary, unless H.239 information exists in the
 *          data type.
 *
 * Return Value: 1 for secondary video, 0 for primary
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term             - Terminal object to use.
 *         dataTypeNodeId   - Node id of the data type to check.
 * Output: None.
 *****************************************************************************/
RvUint16 termGefFindRole(
    IN TermObj          *term,
    IN RvPvtNodeId      dataTypeNodeId);


/******************************************************************************
 * termGefCreateH239SecondaryChannel
 * ----------------------------------------------------------------------------
 * General: Create an H.245 dataType node with H.239 extendedVideoCapability
 *          for a secondary channel.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term             - Terminal object to use.
 *         dataTypeName     - Name of codec to use.
 *         dataTypeNodeId   - Node id of the data type to check.
 * Output: dataTypeNode     - The H.245 dataType node containing the
 *                            extendedVideoCapability.
 *                            This nodeId must be deleted by the caller of this
 *                            function.
 *****************************************************************************/
RvStatus termGefCreateH239SecondaryChannel(
    IN  TermObj         *term,
    IN  const RvChar    *dataTypeName,
    OUT RvPvtNodeId     *dataTypeNodeId);



#else 
/* !defined(USE_GEF) */
#define termGefInit(_term) RV_OK
#define termGefEnd(_term)
#define termGefFindRole(_term, _dataTypeNodeId) 0 /* Always primary video */
#define termGefCreateH239SecondaryChannel(_term, _dataTypeName, _dataTypeNodeId) RV_ERROR_NOTSUPPORTED

#endif  /* defined(USE_GEF) */



#ifdef __cplusplus
}
#endif

#endif /* _RV_TERM_GEF_H_ */
