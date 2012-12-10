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


#ifndef _CAPUTILS_
#define _CAPUTILS_

#include "cmH245GeneralDefs.h"
#include "cmctrl.h"


#ifdef __cplusplus
extern "C" {
#endif


int
capStructBuild(
           IN  HPVT hVal,
           IN  int capEntryId,
           cmCapStruct *capability
           );

int
capSetBuild(
        /* build array of capability set */
        IN  HPVT hVal,
        IN  int termCapSetId, /* terminalCapabilitySet node id */
        IN  int capabilitiesSize, /* number of elements in capabilities array */
        OUT cmCapStruct** capabilities /* cap names array */
        );

/******************************************************************************
 * capDescBuild
 * ----------------------------------------------------------------------------
 * General: Build an array of capability descriptors from a TCS message sorted
 *          in ascending order of capabilityDescriptorNumbers.
 *          The capDesc array is built in 4 hierarchical levels:
 *          CapabilityDescriptor -> simultaneousCapabilities ->
 *          AlternativeCapabilitySet -> CapabilityTableEntry.
 *          Each one of the above 4 levels is built on capDesc according to hierarchy
 *          and separated by NULL pointers.
 *          Therefore, the caller to this function, should do a (cmCapStruct****)
 *          casting to capDesc to be able to read the content of the array.
 *          The figure below displays how it is done:
 *
 *          A: Alternative
 *          S: Simultaneous
 *          D: Descriptor
 *          -: null
 *
 *          (capDesc)
 *          \/
 *          ---------------------------------------------------------
 *          | D D ..D - S S S - S S - ==>  ...  <== A A - A A A A - |
 *          ---------------------------------------------------------
 *
 *
 * Return Value: If successful - Non negative.
 *               other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hVal              - The PVT handle.
 *         termCapSetId      - TerminalCapabilitySet node id.
 *         capabilities      - The actual capabilities from capabilityTable in TCS.
 *         capSetArraySize   - Maximum size of the capabilities array.
 *         capDescArraySize  - Number of elements in capDesc array.
 * Output: capDesc           - The descriptors array. This array should be allocated
 *                             before calling this function.
 *****************************************************************************/
int capDescBuild(
         IN  HPVT           hVal,
         IN  int            termCapSetId,
         IN  cmCapStruct    **capabilities,
         IN  int            capSetArraySize,
         IN  int            capDescArraySize,
         OUT void           **capDesc);


int
capStructBuildFromStruct(
             /* build single capability entry */
             IN  HPVT hVal,
             IN  int confRootId, /* configuration root id */
             OUT int capId,
             IN  cmCapStruct *capability
             );
int
capSetBuildFromStruct(
              /* Build capability table from capability structure array.
             - The capabilityId field is updated here.
             - if name != 0 then the configuration channel data definition is used.
             - if name == 0 and capabilityHandle >=0 then the specified data tree is used.
             - type and direction values shall be set.
             */
              IN  HPVT hVal,
              IN  int confRootId, /* configuration root id */
              OUT int termCapSetId, /* terminalCapabilitySet node id */
              IN  cmCapStruct** capabilities /* cap names array */
              );

int
capDescBuildFromStruct(
               /* build capability combinations from nested array.
              - The capabilityId shall be set to correct value, meaning
              this is called after capStructBuildFromStruct().
              */
               IN  HPVT hVal,
               OUT int termCapSetId, /* terminalCapabilitySet node id */
               IN  cmCapStruct*** capabilities[] /* cap names array */
               );


#ifdef __cplusplus
}
#endif

#endif



