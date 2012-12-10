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

#ifndef _RV3G324MGENERAL_H_
#define _RV3G324MGENERAL_H_


#include "cmH245GeneralDefs.h"
#include "coder.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef enum
{
    OID_NumberForm,             /* e.g.:  0 2 3         */
    OID_NameForm,               /* e.g.:  itu-t 2 3     */
    OID_NameAndNumberForm       /* e.g.:  itu-t(0) 2 3  */
} OID_form;
    
    
    /*=========================================================================**
    **  == utlEncodeOID() ==                                                   **
    **                                                                         **
    **  Converts an OID string into a stack internal representation.           **
    **                                                                         **
    **  PARAMETERS:                                                            **
    **      oidSize         The length, in bytes, of the target OID buffer.    **
    **                                                                         **
    **      oid             A pointer to the target OID buffer, or NULL.       **
    **                                                                         **
    **      buff            A pointer to the string to encode.                 **
    **                                                                         **
    **  RETURNS:                                                               **
    **      The length, in bytes, of the resulting target OID string, or a     **
    **      negative integer error code.                                       **
    **                                                                         **
    **  Use this function to convert OID string into the representation used   **
    **  by stack components, such as PVT.                                      **
    **                                                                         **
    **  In order to pre-calculate the size needed for the target buffer, you   **
    **  can pass a NULL value in the <oid> parameter.  In this case, the       **
    **  <oidSize> parameter is ignored, and function will not attempt to fill  **
    **  out a target buffer.  The return value will indicate the needed        **
    **  length.                                                                **
    **                                                                         **
    **=========================================================================*/
    
RVAPI
    int RVCALLCONV utlEncodeOID(
    IN    int           oidSize,
    OUT   char *        oid,
    IN    const char *  buff);
    
    
        /*=========================================================================**
        **  == utlDecodeOID() ==                                                   **
        **                                                                         **
        **  Converts a stack-internal OID representation into a readable string.   **
        **                                                                         **
        **  PARAMETERS:                                                            **
        **      oidSize         The length, in bytes, of the encoded OID value.    **
        **                                                                         **
        **      oid             A pointer to the encoded OID value buffer.         **
        **                                                                         **
        **      buffSize        The length, in bytes, of the target buffer.        **
        **                                                                         **
        **      buff            A pointer to the target decoding buffer, or NULL.  **
        **                                                                         **
        **      f               The required for for the target parameter.         **
        **                      (See remarks below).                               **
        **                                                                         **
        **  RETURNS:                                                               **
        **      The length, in bytes, of the resulting decoded OID string (with-   **
        **      out the ending ASCIIZ), or a negative integer error code.          **
        **                                                                         **
        **  Use this function to decode OID values returned by stack components    **
        **  such as PVT.                                                           **
        **                                                                         **
        **  Three target format types are available, as expressed in the OID_form  **
        **  enumeration:                                                           **
        **  o OID_NameForm, such as "0 2 3"                                        **
        **  o OID_NumberForm, such as "itu-t 2 3"                                  **
        **  o OID_NameAndNumberForm, such as "itu-t(0) 2 3"                        **
        **  Not all values can be decoded into name forms.                         **
        **                                                                         **
        **  In order to pre-calculate the size needed for the target buffer, you   **
        **  can pass a NULL value in the <buff> parameter.  In this case, the      **
        **  <buffSize> parameter is ignored, and function will not attempt to fill **
        **  out a target buffer.  The return value will indicate the needed        **
        **  length.                                                                **
        **                                                                         **
    **=========================================================================*/
    
RVAPI
    int RVCALLCONV utlDecodeOID(
    IN   int            oidSize,
    IN   const char *   oid,
    IN   int            buffSize,
    OUT  char *         buff,
    IN   OID_form       f);
    


/******************************************************************************
 * Rv3G324mGetVersion
 * ----------------------------------------------------------------------------
 * General: Returns the version of the 3G-324m stack in use.
 *
 * Return Value: Returns a pointer to the string that represents the version. 
 *               For example: "1.0.0.0".
 * ----------------------------------------------------------------------------
 * Arguments: None.
 *****************************************************************************/
RVAPI
const RvChar* RVCALLCONV Rv3G324mGetVersion(void);


/******************************************************************************
 * Rv3G324mInit
 * ----------------------------------------------------------------------------
 * General: Initializes the system for work with the 3G-324M Stack.
 *          This function must be called before the call to Rv3G324mConstruct().
 *          is made.
 *
 * Return Value: RV_OK if successful, or another value on failure.
 * ----------------------------------------------------------------------------
 * Arguments: None
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV Rv3G324mInit(void);


/******************************************************************************
 * Rv3G324mEnd
 * ----------------------------------------------------------------------------
 * General: Shuts down the system. This function must be the last one called 
 * when no more 3G-324M Stack instances are available. 
 *
 * Return Value: RV_OK if successful, or another value on failure.
 * ----------------------------------------------------------------------------
 * Arguments: None.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV Rv3G324mEnd(void);


/******************************************************************************
 * Rv3G324mConstruct
 * ----------------------------------------------------------------------------
 * General: Creates a Stack instance for handling 3G-324M calls.
 *
 * Return Value: RV_OK if successful, or another value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  configSourceName - The configuration to use. This can be one of 
 *							  the following: Configuration filename; Memory 
 *                            buffer holding the configuration.
 * Output: h3G324m          - The Stack instance created. 
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV Rv3G324mConstruct(
        IN  const RvChar    *configSourceName,
        OUT HAPP            *h3G324m);


/******************************************************************************
 * Rv3G324mDestruct
 * ----------------------------------------------------------------------------
 * General: Destructs this Stack instance. 
 *
 * Return Value: RV_OK if successful, or another value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324m          - The Stack instance to be deleted.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV Rv3G324mDestruct(
        IN HAPP h3G324m);


/******************************************************************************
 * Rv3G324mAttachThread
 * ----------------------------------------------------------------------------
 * General: Indicates that a thread created by the application is about to use 
 * the API functions of the Toolkit. If this function is not called, calls to 
 * API functions from that thread can cause a crash.
 *
 * Return Value: RV_OK if successful, or another value on failure.
 * ----------------------------------------------------------------------------
 * Arguments: threadName    - The name to give to this thread in the logs.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV Rv3G324mAttachThread(IN const RvChar *threadName);


/******************************************************************************
 * Rv3G324mDetachThread
 * ----------------------------------------------------------------------------
 * General: Indicates that a thread created by the user is being shut down.
 *          This function should be called to remove memory allocated by the 
 *          Stack for the current thread. 
 *
 * Return Value: RV_OK if successful, or another value on failure.
 * ----------------------------------------------------------------------------
 * Arguments: None.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV Rv3G324mDetachThread(void);


/******************************************************************************
 * Rv3G324mGetValTree
 * ----------------------------------------------------------------------------
 * General: Returns the PVT handle used by the Stack for H.245 messages. 
 *
 * Return Value: PVT handle on success, or NULL on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324m          - The Stack instance to check.
 *****************************************************************************/
RVAPI HPVT RVCALLCONV Rv3G324mGetValTree(IN HAPP h3G324m);


/******************************************************************************
 * Rv3G324mGetSynTreeByRootName
 * ----------------------------------------------------------------------------
 * General: Returns a syntax tree handle of a requested root name.
 *
 * Return Value: HPST handle on success, or NULL on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324m          - The Stack instance to check.
 *         name             - The root name of the required syntax tree.
 *****************************************************************************/
RVAPI HPST RVCALLCONV
Rv3G324mGetSynTreeByRootName(
    IN   HAPP           h3G324m,
    IN   const RvChar   *name);


/******************************************************************************
 * Rv3G324mLogMessage
 * ----------------------------------------------------------------------------
 * General: Writes a user message into the Log under the APPL filter. 
 *
 * Return Value: RV_OK if successful, or another value on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324m       - The Stack instance to be logged to.
 *         message       - The message to be put into the Log (null-terminated).
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV Rv3G324mLogMessage(
    IN HAPP          h3G324m,
    IN const RvChar *message);



/******************************************************************************
 * Rv3G324mProcessEvents
 * ----------------------------------------------------------------------------
 * General: Processes any pending events that should occur in the given 
 *          3G-324M Stack instance. The application should call this function 
 *          periodically. If it does not, events will not be processed properly.
 *
 * Return Value: RV_OK if successful, or another value on failure.
 * See Also:Rv3G324mCallSend, Rv3G324mCallReceive
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324m          - The Stack instance.
 *         msg              - The node id that contains the message.
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV Rv3G324mProcessEvents(
    IN HAPP         h3G324m);

/******************************************************************************
 * Rv3G324mGetH245Syntax
 * ----------------------------------------------------------------------------
 * General: Returns the PST handle used by the Stack for H.245 messages. 
 *
 * Return Value: PST handle if successful, or NULL on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324m          - The Stack instance.
 *****************************************************************************/
RVAPI HPST RVCALLCONV Rv3G324mGetH245Syntax(IN HAPP h3G324m);

#ifdef RV_USE_RESOURCE_COUNTING
/******************************************************************************
 * Rv3G324mGetResourceCounting
 * ----------------------------------------------------------------------------
 * General: Reports resource usage to the string in a readable format.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:   h3G324m     - The Stack instance. 
 *          buf         - Reports buffer. Should be large enough for building a 
 *                        report, otherwise the report will be truncated.
 *         bufLen       - size of 'buf' in bytes.
 *****************************************************************************/
RVAPI void RVCALLCONV Rv3G324mGetResourceCounting(
    IN HAPP     h3G324m,
    IN RvChar   *buf, 
    IN RvSize_t bufLen);
#endif /* RV_USE_RESOURCE_COUNTING */

/******************************************************************************
 * vt_Rv3G324mGetCodecPriority
 * ----------------------------------------------------------------------------
 * General: Get the codec priority in the h245 configuration tree 
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324m          - Stack instance 
 * Output:              firstPriorityCodec
 *                          secondPriorityCodec
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV vt_Rv3G324mGetCodecPriority(
        IN HAPP               h3G324m,
        OUT RvInt32         *firstPriorityCodec,
        OUT RvInt32         *secondPriorityCodec);

/******************************************************************************
 * vt_Rv3G324mGetTerminalType
 * ----------------------------------------------------------------------------
 * General: Get the terminal type in the h245 configuration tree 
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:            3G324m          - Stack instance 
 * Output:          erminalType    - New Terminal Type
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV vt_Rv3G324mGetTerminalType(
        IN HAPP                 h3G324m,
        OUT RvInt32         *terminalType);

/******************************************************************************
 * vt_Rv3G324mChangeTerminalType
 * ----------------------------------------------------------------------------
 * General: Change the terminal type in the h245 configuration tree 
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:    h3G324m        - Stack instance 
 *              terminalType   - New Terminal Type
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV vt_Rv3G324mChangeTerminalType(
        IN HAPP                  h3G324m,
        IN RvInt32              terminalType);

/******************************************************************************
 * vt_Rv3G324mChangeCodecPriority
 * ----------------------------------------------------------------------------
 * General: Change the codec priority in the h245 configuration tree 
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:    h3G324m          - Stack instance 
 *              firstPriorityCodec
 *              secondPriorityCodec
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV vt_Rv3G324mChangeCodecPriority(
        IN HAPP                  h3G324m,
        IN RvInt32              firstPriorityCodec,
        IN RvInt32              secondPriorityCodec);

/******************************************************************************
 * vt_Rv3G324mChangeNSRPTimer
 * ----------------------------------------------------------------------------
 * General: Change the NSRPTimer in the h245 configuration tree 
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324m          - Stack instance 
 *            newNSRPTimer  - new Timer value
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV vt_Rv3G324mChangeNSRPTimer(
        IN HAPP                 h3G324m,
        IN RvInt32             newNSRPTimer);

/******************************************************************************
 * vt_Rv3G324mChangeN400Counter
 * ----------------------------------------------------------------------------
 * General: Change the N400 counter in the h245 configuration tree 
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  h3G324m          - Stack instance 
 *            value  - new N400 counter
 *****************************************************************************/
RVAPI RvStatus RVCALLCONV vt_Rv3G324mChangeN400Counter(
        IN HAPP                 h3G324m,
        IN RvInt32             value);

#ifdef __cplusplus
}
#endif

#endif /* _RV3G324MGENERAL_H_ */
