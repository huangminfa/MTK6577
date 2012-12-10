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

#ifndef _RV_TERM_CONVERSIONS_H_
#define _RV_TERM_CONVERSIONS_H_

/***********************************************************************
epConversions.h

Conversion functions widely used by this module.
***********************************************************************/


/*-----------------------------------------------------------------------*/
/*                        INCLUDE HEADER FILES                           */
/*-----------------------------------------------------------------------*/
#include "termDefs.h"




#ifdef __cplusplus
extern "C" {
#endif



/*-----------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                            */
/*-----------------------------------------------------------------------*/





/*-----------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                           */
/*-----------------------------------------------------------------------*/

#if 0 // [Meggie] 2009/12/25: remove unused functions 
const RvChar *BooleanStr(IN RvBool value);


/* Enumeration conversions */

const RvChar *Status2String(IN int status);
int String2Status(IN const RvChar *string);


Rv3G324mCallState String2CMCallState(IN const RvChar *string);
const RvChar *CMCallState2String(IN Rv3G324mCallState state);

Rv3G324mCallStateMode String2CMCallStateMode(IN const RvChar *string);
const RvChar *CMCallStateMode2String(IN Rv3G324mCallStateMode stateMode);

cmChannelState_e String2CMChannelState(IN const RvChar *string);
const RvChar *CMChannelState2String(IN cmChannelState_e state);

cmChannelStateMode_e String2CMChannelStateMode(IN const RvChar *string);
const RvChar *CMChannelStateMode2String(IN cmChannelStateMode_e stateMode);

cmChannelDataType String2CMChannelDataType(IN const RvChar *string);
const RvChar *CMChannelDataType2String(IN cmChannelDataType dataType);

Rv3G324mCallMuxLevel String2Rv3G324mCallMuxLevel(IN const RvChar *string);
const RvChar *Rv3G324mCallMuxLevel2String(IN Rv3G324mCallMuxLevel muxLevel);

cmH245ChannelConflictType String2cmH245ChannelConflictType(IN const RvChar *string);
const RvChar *cmH245ChannelConflictType2String(IN cmH245ChannelConflictType conflictType);

RvStatus String2CMNonStandardParam(IN const RvChar *string, OUT cmNonStandardParam *param);
const RvChar *CMNonStandardParam2String(IN cmNonStandardParam *param);
#endif

#ifdef USE_GEF
#if 0 // [Meggie] 2009/12/25: remove unused functions 
RvGefCodecType String2GefCodecType(IN const RvChar *string);
#endif
const RvChar *GefCodecType2String(IN RvGefCodecType codecType);
#endif

#if 0 // [Meggie] 2009/12/25: remove unused functions 
#ifdef USE_H245AUTOCAPS
RvH245AutoCapsErrorReason H245AutoCapsString2ErrorReason(IN const RvChar *string);
const RvChar *H245AutoCapsErrorReason2String(IN RvH245AutoCapsErrorReason eErrorReason);

RvH245AutoCapsErrorReason H245AutoCapsString2ChannelResponse(IN const RvChar *string);
const RvChar *H245AutoCapsChannelResponse2String(IN RvH245AutoCapsChannelResponse eChannelResponse);
#endif

cmRejectLcnReason String2RejectLcnReason(IN const RvChar *string);
const RvChar *RejectLcnReason2String(IN cmRejectLcnReason eRejectReason);


/******************************************************************************
 * IsdnInfo2String
 * ----------------------------------------------------------------------------
 * General: Returns a string with an error description for the ISDN.
 *
 * Infos with values of 0x00xx are only warnings and the corresponding
 * messages have been processed.
 * The description for all info values but 0x34xx is taken from the CAPI 2.0
 * specification February 1994.
 * The description for the 0x34xx values is taken from ETS 300 102-1/Q.931
 *
 * Return Value: Reason string
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  info - Info to convert
 * Output: None
 *****************************************************************************/
const RvChar *IsdnInfo2String(IN RvUint info);
#endif

#ifdef __cplusplus
}
#endif

#endif /* _RV_TERM_CONVERSIONS_H_ */
