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

/* MTK */     
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
#ifndef _CM_CTRL_RME_H_
#define _CM_CTRL_RME_H_


#ifdef __cplusplus
extern "C" {
#endif

#if (RV_H245_SUPPORT_H223_PARAMS == 1)    
    
/******************************************************************************
 * rmeInit
 * ----------------------------------------------------------------------------
 * General: Initialize a requestMultiplexEntry object of the control.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  ctrl - Control object
 *****************************************************************************/
RvStatus rmeInit(IN H245Control* ctrl);

/******************************************************************************
 * rmeEnd
 * ----------------------------------------------------------------------------
 * General: Destruct a requestMultiplexEntry object of the control.
 *
 * Return Value: RV_OK  - if successful.
 *               Other on failure
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  ctrl - Control object
 *****************************************************************************/
RvStatus rmeEnd(IN H245Control* ctrl);

/******************************************************************************
 * rmeCancelTimer
 * ----------------------------------------------------------------------------
 * General: cancel the outMES timer
 *
 * Return Value: None
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  ctrl  - The control object.
 *         app   - The h245 object.
 *****************************************************************************/
void rmeCancelTimer(IN H245Control* ctrl, IN H245Object* app);

/************************************************************************
 * requestMultiplexEntry
 * purpose: Handle an incoming requestMultiplexEntry message
 * input  : ctrl    - Control object
 *          message - MES message node
 * output : none
 * return : Non-negative value on success
 *          Negative value on failure
 ************************************************************************/
int requestMultiplexEntry(IN H245Control* ctrl, IN int message);

/************************************************************************
 * requestMultiplexEntryAck
 * purpose: Handle an incoming requestMultiplexEntryAck message
 * input  : ctrl    - Control object
 *          message - MES.Ack message node
 * output : none
 * return : Non-negative value on success
 *          Negative value on failure
 ************************************************************************/
int requestMultiplexEntryAck(IN H245Control* ctrl, IN int message);

/************************************************************************
 * requestMultiplexEntryReject
 * purpose: Handle an incoming requestMultiplexEntryReject message
 * input  : ctrl    - Control object
 *          message - MES.Reject message node
 * output : none
 * return : Non-negative value on success
 *          Negative value on failure
 ************************************************************************/
int requestMultiplexEntryReject(IN H245Control* ctrl, IN int message);

/************************************************************************
 * requestMultiplexEntryRelese
 * purpose: Handle an incoming requestMultiplexEntryRelese message
 * input  : ctrl    - Control object
 *          message - MES message node
 * output : none
 * return : Non-negative value on success
 *          Negative value on failure
 ************************************************************************/
int requestMultiplexEntryRelease(IN H245Control* ctrl, IN int message);	


#else /*RV_H245_SUPPORT_H223_PARAMS == 1*/

#define requestMultiplexEntry(ctrl, message)
#define requestMultiplexEntryAck(ctrl, message)
#define requestMultiplexEntryReject(ctrl, message)
#define requestMultiplexEntryRelease(ctrl, message)	



#endif /*RV_H245_SUPPORT_H223_PARAMS == 1*/

#ifdef __cplusplus
}
#endif

#endif /* _CM_CTRL_RME_H_ */
