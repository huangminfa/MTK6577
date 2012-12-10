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
 *		Name:					trg_info.h
 *
 *		Project:				OMC
 *
 *		Created On:				June 2004
 *
 *		Derived From:			(original)
 *
 *		Version:				$Id: //depot/main/base/omc/trg/trg_info.h#8 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2005
]*/

/*! \internal
 * \file
 *			Definitions for OMC internal trigger information
 *
 * \brief	Trigger information definitions
 */

#ifndef _OMC_TRG_TRG_INFO_H
#define _OMC_TRG_TRG_INFO_H

#ifdef __cplusplus
extern "C" {
#endif

/* User Interface Mode */
typedef enum {
	TRG_UIM_UNSPEC		= 0x00,		/* User interaction unspecified */
	TRG_UIM_BACKGROUND	= 0x01,		/* Perform session in background */
	TRG_UIM_INFORM		= 0x02,		/* Announce beginning of session */
	TRG_UIM_INTERACT	= 0x03		/* Prompt before starting session */
} TRG_UIMode;

/*
 * OMC internal trigger information structure
 */
typedef struct {
	TRG_Reason	reason;			/* Why client was invoked */
	IU16		version;		/* DM Version */
	TRG_UIMode	uiMode;			/* User interaction mode */
	IBOOL		flags;			/* Flags, see below */
	IU16		sessionId;		/* Session identifier */
	TRG_Profile	bootProfile;	/* Bootstrap profile */
	IU8*		message;		/* Bootstrap message or notification body */
	IU32		messageLen;		/* Length of message */
	IU8			serverId[OMC_SERVERID_MAX_LEN + 1];	/* Server ID */
#ifdef OMADS
	struct OBJ_Session_s* dssList;	/* The list of open datastore sessions */
#endif

} TRG_Info;

/* Flag values */
#define TRG_FLG_SERVERINITIATED	0x01	/* Server initiated session */
#define TRG_FLG_FOLLOWUPSESSION	0x02	/* Initiate mgmt session after boot */

extern SLICE TRG_getInfo(OMC_SessionDataPtr sdp, TRG_Data *tdp, TRG_Info *tip);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_TRG_TRG_INFO_H */
