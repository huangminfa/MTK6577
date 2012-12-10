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

/*[
 *		Name:					sess_queue.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			(Original)
 *
 *		Version:				$Id: //depot/main/base/omc/sess/sess_queue.h#10 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2005
]*/

/*! \file
 *		Defines the API and structures used to hold commands in a queue
 *
 * \brief	Command queue handling API
 */

#ifndef _OMC_SESS_QUEUE_H_
#define _OMC_SESS_QUEUE_H_

#ifdef __cplusplus
extern "C" {
#endif

/*
 * The functions to handle queues.
 */
extern void SESS_initQueues(OMC_SessionDataPtr sdp);
extern void SESS_destroyQueues(OMC_SessionDataPtr sdp);
extern void SESS_queueAddTail(SESS_QueuePtr q, SESS_CmdPtr commandPtr);
extern void SESS_queueAddHead(SESS_QueuePtr q, SESS_CmdPtr commandPtr);
extern OMC_Error SESS_queueGetHead(SESS_QueuePtr q, SESS_CmdPtr *commandPtr);
extern OMC_Error SESS_queuePeekHead(SESS_QueuePtr q, SESS_CmdPtr *commandPtr);
extern OMC_Error SESS_queuePeekTail(SESS_QueuePtr q, SESS_CmdPtr *commandPtr);
extern OMC_Error SESS_queuePeekNext(SESS_CmdPtr currPtr,
		SESS_CmdPtr *commandPtr);
extern OMC_Error SESS_queueRemove(SESS_QueuePtr q, SESS_CmdPtr commandPtr);

#ifdef PROD_MIN
#define SESS_dumpQueues(sdp)
#else
extern void SESS_dumpQueues(OMC_SessionDataPtr sdp);
#endif

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_SESS_QUEUE_H_ */
