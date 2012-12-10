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

/**
 * @file message_definition.h
 *
 * @brief Definition of all message identifiers.
 *
 */

#ifndef __MESSAGE_DEFINITION__
#define __MESSAGE_DEFINITION__

#include "common.h"

#ifdef __cplusplus
extern "C" {
#endif


enum mdm_message_id_enum_e {
	/** This message id is used by timer API, do not touch it. */
    MSG_ID_TIMER_EXPIRY,
	/** Request to stop the engine. */
	MSG_ID_ENGINE_STOP,
	/** Response to MSG_ID_ENGINE_STOP. */
    MSG_ID_ENGINE_STOP_RESPONSE,
    /** Pause current session, supports DM, DL and BS. */
    MSG_ID_PAUSE_SESSION,
	/** Resume paused session, supports DM, DL and BS. */
    MSG_ID_RESUME_SESSION,
	/** Cancel current session, supports DM, DL and BS. */
    MSG_ID_CANCEL_SESSION,
	/** Start a client initiated DM session. */
    MSG_ID_DM_SESSION_TRIGGER,
    /** Trigger a report session. */
    MSG_ID_DM_REPORT_SESSION_TRIGGER,
    /** Start a network initiated DM session. */
    MSG_ID_DM_NIA_SESSION_TRIGGER,
    /** Tell the engine to proceed the processing of NIA session. */
    MSG_ID_DM_NIA_SESSION_PROCEED_NOTIFICATION,
	/** Notify engine thread that the session thread state has changed. */
    MSG_ID_SESSION_STATE_NOTIFICATION,
    /** Notify engine thread that the session thread sub-state has changed. */
    MSG_ID_SESSION_SUB_STATE_NOTIFICATION,
	/** Notify engine thread the MMI response of confirmation. */
    MSG_ID_MMI_NOTIFY_COMFIRMATION_RESULT,
	/** Notify engine thread the MMI response of choice list. */
    MSG_ID_MMI_NOTIFY_CHOICELIST_SELECTION,
	/** Notify engine thread the MMI interaction is cancelled. */
    MSG_ID_MMI_NOTIFY_CANCEL_EVENT,
	/** Notify engine thread the info messsage is closed. */
    MSG_ID_MMI_NOTIFY_INFO_MSG_CLOSED,
	/** Notify engine thread the MMI response of input. */
    MSG_ID_MMI_NOTIFY_INPUT_RESULT,
	/** Notify engine thread the MMI interaction is timed out. */
    MSG_ID_MMI_NOTIFY_TIMEOUT_EVENT,
    /** @todo Trigger BS session. */
    MSG_ID_BS_SESSION_TRIGGER,
    /** @todo Trigger DL session. */
    MSG_ID_DL_SESSION_PROCEED_NOTIFICATION,

    /* Add message IDs should be above this line. */
    MSG_ID_INVALID = -1,
} mdm_message_id_enum;


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !__MESSAGE_DEFINITION__ */
