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
 *		Name:					omc_cmdtable.h
 *
 *		Project:				OMC
 *
 *		Created On:				January 2006
 *
 *		Derived From:			Original
 *
 *		Version:				$Id: //depot/main/base/omc/omc_cmdtable.h#1 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2006
]*/

/*! \internal
 * \file
 *		Table of command names and command flags
 *
 * \brief	Command names etc
 */


#ifndef _OMC_OMC_CMDTABLE_H_
#define _OMC_OMC_CMDTABLE_H_

#ifdef __cplusplus
extern "C" {
#endif

/*
 * The SyncML commands
 */
typedef enum
{
	OMC_CMD_UNDEFINED		=  0,	/* Not a command (Must be 0!) */

	OMC_CMD_ADD				=  1,
	OMC_CMD_ALERT			=  2,
	OMC_CMD_ATOMIC_START	=  3,
	OMC_CMD_COPY			=  4,
	OMC_CMD_DELETE			=  5,
	OMC_CMD_EXEC			=  6,
	OMC_CMD_GET				=  7,
	OMC_CMD_MAP				=  8,
	OMC_CMD_MESSAGE_START	=  9,
	OMC_CMD_MOVE			= 10,
	OMC_CMD_PUT				= 11,
	OMC_CMD_REPLACE			= 12,
	OMC_CMD_SEARCH			= 13,
	OMC_CMD_SEQUENCE_START	= 14,
	OMC_CMD_STATUS			= 15,
	OMC_CMD_SYNC_START		= 16,

	OMC_CMD_COUNT_STATUS,			/* Commands before here need a Status
									 * to be returned. */

	OMC_CMD_ATOMIC_END		= 17,
	OMC_CMD_MESSAGE_END		= 18,
	OMC_CMD_RESULTS			= 19,
	OMC_CMD_SEQUENCE_END	= 20,
	OMC_CMD_SYNC_END		= 21,

	OMC_CMD_COUNT					/* Total number of commands */
} OMC_CmdType;


/*
 * The table of command names indexed by OMC_CmdType value
 */
extern const UTF8CStr OMC_cmdNames[OMC_CMD_COUNT];


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_OMC_CMDTABLE_H_ */
