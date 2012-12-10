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
 *		Name:					trg_notification.h
 *
 *		Project:				OMC
 *
 *		Created On:				June 2004
 *
 *		Derived From:			(original)
 *
 *		Version:				$Id: //depot/main/base/omc/trg/trg_notification.h#2 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004-2005
]*/

/*! \internal
 * \file
 *			OMA Notification Initiated Session Trigger Message definitions
 *
 * \brief	Notification Trigger definitions
 */

#ifndef _OMC_TRG_TRG_NOTIFICATION_H
#define _OMC_TRG_TRG_NOTIFICATION_H

/*
 * The default format for the Notification Initiated Session Trigger Message
 * (General Package#0) is as follows:
 *
 *		Digest
 *		Trigger-hdr
 *		Trigger-body (vendor specific)
 *
 * The following structure is used to access the digest and trigger header
 * in network byte order.
 */
typedef struct {
	HASH	digest;			/* message digest */
	IU8		verMode[5];		/* version, UI mode, initiator & reserved fields */
	IU8		sessIdHi;		/* session ID, high byte */
	IU8		sessIdLo;		/* session ID, low byte */
	IU8		serverIdLen;	/* server ID length */
	IU8		serverId[1];	/* server ID */
} TRG_GP0TriggerHdr;

/*
 * Accessor macros
 */

/* SyncML GP0 and protocol version number is in first 10 bits of verMode */
#define TRG_GP0_version(hp) \
	((IU16)(((hp)->verMode[0] << 2) | ((hp)->verMode[1] >> 6)))

/* UI mode is in bits 5,4 of verMode[1] */
#define TRG_GP0_uiMode(hp) \
	((TRG_UIMode)(((hp)->verMode[1] >> 4) & 0x3))

/* Initiator is in bit 3 of verMode[1]: 0 => user, 1 => server */
#define TRG_GP0_initiator(hp) \
	(((hp)->verMode[1] >> 3) & 0x1)

/* Session ID */
#define TRG_GP0_sessionId(hp) \
	((IU16)(((hp)->sessIdHi << 8) | (hp)->sessIdLo))

#endif /* !_OMC_TRG_TRG_NOTIFICATION_H */
