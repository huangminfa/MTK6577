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

#ifndef GENERAL_DEFS_H
#define GENERAL_DEFS_H

#include "rvtypes.h"
#include "pvaltree.h"


#ifdef __cplusplus
extern "C" {
#endif


/* This file contains definitions that were declared in the CM module. These definitions
   cannot remain in the CM because they are needed in the H.245 module. */
RV_DECLARE_HANDLE(HAPP);
RV_DECLARE_HANDLE(HAPPCALL);
RV_DECLARE_HANDLE(HCALL);


typedef struct
{
  /* Sets one of the following choices */

  /* 1. object id */
  char object[128]; /* in object identifier ASN format: "0 1 2" */
  int objectLength; /* in bytes. <=0 if not set */

  /* 2. h.221 id */
  RvUint8 t35CountryCode;
  RvUint8 t35Extension;
  RvUint16 manufacturerCode;
} cmNonStandardIdentifier;



typedef struct
{
    cmNonStandardIdentifier   info;
    int               length;
    char*             data;
} cmNonStandardParam;


/* nonStandard handling_______________________________________________________________*/

/******************************************************************************
 * cmNonStandardParameterCreate
 * ----------------------------------------------------------------------------
 * General: Creates a NonStandardParameter PVT node id.
 *
 * See also: cmNonStandardParam, cmNonStandardIdentifier 
 * Return Value: The root PVT node ID if successful.
 *               Negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hVal       - The PVT handle.
 *         rootID     - The root node ID provided by the application (nonStandardData)   
 *                      where the nonStandardParameter will be built.
 *         identifier - The cmNonStandardIdentifier structure that is included in nonStandardParameter.
 *         data       - The data string included in nonStandardParameter.
 *         dataLength - The length, in bytes, of the string provided in the data parameter. 
 *****************************************************************************/
RVAPI int RVCALLCONV cmNonStandardParameterCreate(
                 IN  HPVT                     hVal,
                 IN  int                      rootId, 
                 IN  cmNonStandardIdentifier  *identifier,
                 IN  const RvChar             *data,
                 IN  int                      dataLength 
                 );


/******************************************************************************
 * cmNonStandardParameterGet
 * ----------------------------------------------------------------------------
 * General: Converts the nonStandardParameter PVT node ID into structures in C.
 *
 * See also: cmNonStandardParam, cmNonStandardIdentifier 
 * Return Value: The root PVT node ID if successful.
 *               Negative value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:    hVal       - The PVT handle.
 *           rootID     - The root node id (nonStandardData) which includes the nonStandardParameter.
 * Output:   identifier - The cmNonStandardIdentifier structure which is included in nonStandardParameter.
 *           data       - The data string that is included in nonStandardParameter.
 *           dataLength - The length, in bytes, of the string provided in the data parameter. The application should set this
 *                        parameter to the allocated length of the data parameter, but the actual size of this
 *                        parameter will be set when the function returns.
 *****************************************************************************/
RVAPI int RVCALLCONV cmNonStandardParameterGet (
                 IN    HPVT                     hVal,
                 IN    int                      rootId, 
                 OUT   cmNonStandardIdentifier  *identifier,
                 OUT   RvChar                   *data,
                 INOUT RvInt32                  *dataLength 
                 );


/* General functions */

/************************************************************************
 * cmEmGetH245Syntax
 * purpose: Returns the syntax of h245.asn.
 *          This syntax holds the H.245 standard's ASN.1 syntax.
 *          The syntax is the buffer passed to pstConstruct as syntax.
 * input  : None.
 * output : None.
 * return : Syntax of h245.asn.
 ************************************************************************/
RVAPI
RvUint8* RVCALLCONV cmEmGetH245Syntax(void);


#if (RV_H245_SUPPORT_H225_PARAMS == RV_YES)

#include "cmctrl.h"

/* Mib functions */
/************************************************************************************
 *
 * MIB Related
 * This part handles the communication between the MIB module of the stack and the
 * SNMP application (applSnmp).
 * Applications that want to use applSnmp should NOT use this API functions, as they
 * are used by applSnmp.
 *
 ************************************************************************************/
RV_DECLARE_HANDLE(HMibHandleT);

typedef int (*h341AddNewCallT )(HMibHandleT mibHandle,HCALL hsCall);
typedef void (*h341DeleteCallT  )(HMibHandleT mibHandle,HCALL hsCall);

typedef int (*h341AddControlT  )(HMibHandleT mibHandle,HCALL hsCall);
typedef void (*h341DeleteControlT  )(HMibHandleT mibHandle,HCALL hsCall);

typedef int (*h341AddNewLogicalChannelT  )(HMibHandleT mibHandle,HCHAN hsChan);
typedef void (*h341DeleteLogicalChannelT )(HMibHandleT mibHandle,HCHAN hsChan);


typedef struct
{
    h341AddNewCallT h341AddNewCall;
    h341DeleteCallT h341DeleteCall;
    h341AddControlT h341AddControl;
    h341DeleteControlT h341DeleteControl;
    h341AddNewLogicalChannelT h341AddNewLogicalChannel;
    h341DeleteLogicalChannelT h341DeleteLogicalChannel;
} MibEventT;


/************************************************************************
 * cmMibEventSet
 * purpose: Sets MIB notifications from the Stack to an SNMP application.
 * input  : hApp        - Stack's application handle.
 *          mibEvent    - Event callbacks to be set.
 *          mibHandle   - Handle of MIB instance for the Stack.
 * output : None.
 * return : Non-negative on success.
 *          Negative value on failure.
 ************************************************************************/
RVAPI
int RVCALLCONV cmMibEventSet(IN HAPP hApp, IN MibEventT* mibEvent, IN HMibHandleT mibHandle);




/* EFC enumeration TODO:_______________________________________________________________*/

typedef enum
{
   cmExtendedFastConnectNo = 0,
   cmExtendedFastConnectSupported = 1,
   cmExtendedFastConnectRequired = 2,
   cmExtendedFastConnectUndetermined = 3,
   cmExtendedFastConnectApproved = 4
} cmExtendedFastConnectState_e;


#endif /* (RV_H245_SUPPORT_H225_PARAMS == RV_YES) */




#ifdef __cplusplus
}
#endif


#endif  /* GENERAL_DEFS_H */
