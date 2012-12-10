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
 *		Name:				sess_alert.h
 *
 *		Project:			OMC
 *
 *		Derived From:		Original
 *
 *		Created On:			June 2004
 *
 *		Version:			$Id: //depot/main/base/omc/sess/sess_alert.h#11 $
 *
 *		Coding Standards:	3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
 *
]*/

/*! \internal
 * \file
 *		This file lists the prototype for the function to perform the SyncML
 *		Alert operation.
 *
 * \brief	Provide the prototype for the function to perform SyncML Alert
 */

#ifndef _OMC_SESS_SESS_ALERT_H_
#define _OMC_SESS_SESS_ALERT_H_

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Enumerate the types of Alerts we will be dealing with
 */
typedef enum {

	/*
	 ****************************************************************
	 *               Data Synchronization Alert Codes               *
	 ****************************************************************
	 */

#ifdef OMADS

	/* User interaction */

	ALERT_DS_DISPLAY						= 100,

	/* Session type */

	ALERT_DS_TWO_WAY_SYNC					= 200,
	ALERT_DS_SLOW_SYNC						= 201,
	ALERT_DS_ONE_WAY_FROM_CLIENT			= 202,
	ALERT_DS_REFRESH_FROM_CLIENT			= 203,
	ALERT_DS_ONE_WAY_FROM_SERVER			= 204,
	ALERT_DS_REFRESH_FROM_SERVER			= 205,
	ALERT_DS_TWO_WAY_BY_SERVER				= 206,
	ALERT_DS_ONE_WAY_FROM_CLIENT_BY_SERVER	= 207,
	ALERT_DS_REFRESH_FROM_CLIENT_BY_SERVER	= 208,
	ALERT_DS_ONE_WAY_FROM_SERVER_BY_SERVER	= 209,
	ALERT_DS_REFRESH_FROM_SERVER_BY_SERVER	= 210,

	/* Session/message control */

	ALERT_DS_RESULT							= 221,
	ALERT_DS_NEXT_MESSAGE					= 222,
	ALERT_DS_NO_END_OF_DATA					= 223,
	ALERT_DS_SUSPEND						= 224,
	ALERT_DS_RESUME							= 225,

#endif

	/*
	 ****************************************************************
	 *                Device Management Alert Codes                 *
	 ****************************************************************
	 */

#ifdef OMADM

	/* User interaction */

	ALERT_DM_DISPLAY						= 1100,
	ALERT_DM_CONTINUE_ABORT					= 1101,
	ALERT_DM_TEXT_INPUT						= 1102,
	ALERT_DM_SINGLE_CHOICE					= 1103,
	ALERT_DM_MULTI_CHOICE					= 1104,

	/* Session type */

	ALERT_DM_SERVER_INITIATED				= 1200,
	ALERT_DM_CLIENT_INITIATED				= 1201,

	/* Session/message control */

	ALERT_DM_NEXT_MESSAGE					= 1222,
	ALERT_DM_SESSION_ABORT					= 1223,
	ALERT_DM_CLIENT_EVENT					= 1224,
	ALERT_DM_NO_END_OF_DATA					= 1225,

	/* Notifications */

	ALERT_DM_GENERIC						= 1226,

#endif

	ALERT_UNKNOWN							= 0
} SESS_AlertCmds;

extern SLICE SESS_doAlertCmd(OMC_SessionDataPtr sdp,
		SESS_CmdPtr command);

extern SLICE SESS_showScreenAndWaitForCmd(OMC_SessionDataPtr sdp,
		OMC_MMI_ScreenId screen, OMC_MMI_ScreenOptionsPtr options);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* _OMC_SESS_SESS_ALERT_H_ */
