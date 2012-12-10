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

#ifndef _CMI_FAST_START_H
#define _CMI_FAST_START_H

#include "cmCall.h"
#include "cmH245Object.h"

#ifdef __cplusplus
extern "C" {
#endif



/******************************************************************************************
 * analyzeFastStartMsg
 *
 * Purpose:  This function is called upon receipt of a setup message.
 *           The function checks for any fast-start channels in the message,
 *           decodes the data and builds it into a structure that is passed in a CallBack
 *           to the application, such that the application may ack some of them.
 *
 * Input:    hsCall     - call object instance
 *           message    - The node id to the setup message
 *
 * Returned Value: Non-negative value on success
 *                 Negative value on failure
 ****************************************************************************************/
int analyzeFastStartMsg(
                IN  HCALL       hsCall,
                IN  int         message
                );


/******************************************************************************************
 * cmFastStartReply
 *
 * Purpose:  This function is called from the CM whenever a response message after SETUP
 *           is received (CallProceeding, Alerting, Connect, Facility and Progress).
 *           It checks for FastStart response. If such exists, it processes it and opens
 *           the relevant channels.
 *
 * Input:    hsCall     - call object instance
 *           message    - node ID of the message
 *           isConnect  - True iff this is a connect message
 *
 * Returned Value: Non-negative value on success
 *                 Negative value on failure
 ****************************************************************************************/
int  cmFastStartReply(
    IN HCALL    hsCall,
    IN int      message,
    IN RvBool   isConnect);


/******************************************************************************************
 * addFastStart
 *
 * Purpose:  This function adds the fast start messages that are stored on the call
 *           to an outgoing message
 *
 * Input:    hsCall     - call object instance
 *           message    - message's root node ID to add fast start information to
 *
 * Returned Value: none
 ****************************************************************************************/
void addFastStart(
    IN HCALL    hsCall,
    IN int      message);


void deleteFastStart(HCALL hsCall);
void deleteOutgoingFastStartOffer(HCALL hsCall);
void deleteIncomingFastStartOffer(HCALL hsCall);
int  fastStartInit(IN HH245 hApp, IN int maxCalls, IN int proposed, IN int accepted);
void fastStartEnd(IN HH245 hApp);
void fastStartCallInit(HCALL hsCall);

#ifdef __cplusplus
}
#endif


#endif  /* _CMI_FAST_START_H */
