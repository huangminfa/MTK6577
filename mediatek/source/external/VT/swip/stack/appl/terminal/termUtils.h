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

#ifndef _RV_TERM_UTILS_H_
#define _RV_TERM_UTILS_H_

/***********************************************************************
termUtils.h

Utility functions widely used by this module.
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


/******************************************************************************
 * termUtilsEvent
 * ----------------------------------------------------------------------------
 * General: Send indication of an event to the application that uses this
 *          module.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term             - Terminal object to use.
 *         eventType        - Type of the event indicated.
 *         call             - Call this event belongs to (NULL if none).
 *         eventStr         - Event string, in printf() formatting.
 * Output: None.
 *****************************************************************************/
void termUtilsEvent(
    IN TermObj      *term,
    IN const RvChar *eventType,
    IN TermCallObj  *call,
    IN const RvChar *eventStr, ...);


/******************************************************************************
 * termUtilsLog
 * ----------------------------------------------------------------------------
 * General: Indicate the application that it can log a message for its user.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term             - Terminal object to use.
 *         call             - Call this log message belongs to (NULL if none).
 *         logStr           - Log string, in printf() formatting.
 * Output: None.
 *****************************************************************************/
void termUtilsLog(
    IN TermObj      *term,
    IN TermCallObj  *call,
    IN const RvChar *logStr, ...);


/******************************************************************************
 * termUtilsError
 * ----------------------------------------------------------------------------
 * General: Indicate the application that it can log an error message for its
 *          user.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term             - Terminal object to use.
 *         call             - Call this log message belongs to (NULL if none).
 *         logStr           - Log string, in printf() formatting.
 * Output: None.
 *****************************************************************************/
void termUtilsError(
    IN TermObj      *term,
    IN TermCallObj  *call,
    IN const RvChar *logStr, ...);


/******************************************************************************
 * termUtilsGetError
 * ----------------------------------------------------------------------------
 * General: Get and reset the last known error that occurred in the endpoint.
 *
 * Return Value: None.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term             - Terminal object to use.
 * Output: errStr           - Last known error string. This string is reset
 *                            by the call to this function.
 *****************************************************************************/
void termUtilsGetError(
    IN TermObj          *term,
    OUT const RvChar    **errStr);


/******************************************************************************
 * termUtilsGetStuffSize
 * ----------------------------------------------------------------------------
 * General: Counts the stuffing size of the given buffer. Looks for Annex B
 *          stuffing only.
 *
 * Return Value: The stuffing size.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  pBuffer    - The data buffer to check.
 *         bufSize    - The buffer size.
 * Output: None.
 *****************************************************************************/
RvUint32 termUtilsGetStuffSize(
    IN RvUint8  *pBuffer,
    IN RvUint32 bufSize);


/******************************************************************************
 * termUtilsCheckBadSequences
 * ----------------------------------------------------------------------------
 * General: Check bad sequences.
 *
 * Return Value: None
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  hCall                    - Handle of the call.
 *         dataBuffer               - The receiving data buffer.
 *         dataLength               - The size of the data.
 *         isdnBufferCorrectionSize - The bad sequence size to be corrected.
 * Output: none
 *****************************************************************************/
void termUtilsCheckBadSequences(
    IN HCALL        hCall,
    IN RvUint8      *dataBuffer,
    IN RvUint32     dataLength,
    IN RvInt        isdnBufferCorrectionSize);





#ifdef __cplusplus
}
#endif

#endif /* _RV_TERM_UTILS_H_ */
