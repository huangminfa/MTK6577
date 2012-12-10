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
 *		Name:					trg_private.h
 *
 *		Project:				OMC
 *
 *		Created On:				June 2004
 *
 *		Derived From:			(original)
 *
 *		Version:				$Id: //depot/main/base/omc/trg/trg_private.h#6 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
]*/

/*! \internal
 * \file
 *			Definitions private to trigger module
 *
 * \brief	Private trigger definitions
 */

#ifndef _OMC_TRG_TRG_PRIVATE_H
#define _OMC_TRG_TRG_PRIVATE_H

#ifdef __cplusplus
extern "C" {
#endif

/*!
 * Parse General Notification Initiated Session Alert message
 *
 * This function should be called when a WAP or SMS message with MIME
 * type "application/vnd.syncml.notification" (Content-Type code 0x44) and
 * X-WAP-Application-ID code 0x07 is received on port 2948.
 *
 * \param		sdp		Session data pointer
 * \param		msg		Raw binary message
 * \param		length	Message length
 * \param		tip		Pointer to trigger information structure to be
 *						filled with trigger details.
 *
 * \return		OMC error code
 */
extern SLICE TRG_parseNotification(OMC_SessionDataPtr sdp, IU8 *msg,
						IU32 length, TRG_Info *tip);

/*!
 * Authenticate a bootstrap message
 *
 * This function should be called when a WAP or SMS message with
 * X-WAP-Application-ID code 0x07 is received on port 2948, with a MIME
 * type of either "application/vnd.syncml.dm+wbxml" (Content-Type code 0x42)
 * for Plain Profile or "application/vnd.wap.connectivity-wbxml" for
 * WAP Profile.  See [DM-Bootstrap] 5.4.1.1 and [PROVBOOT] 5.1.
 *
 * \param	sdp		Session data pointer
 * \param	sec		Security method, from the SEC parameter in the WAP
 *					Content-Type header. If the SEC parameter is not present,
 *					the value NONE should be supplied.
 * \param	mac		Hex-encoded message authentication code, from the MAC
 *					parameter in the WAP Content-Type header.
 * \param	msg		WBXML message content.
 * \param	msgLen	Length of message content in bytes.
 *
 * \return	OMC error code
 *
 * Note that the message content is not parsed here: this occurs after
 * SESS_init() has been called.
 */
extern SLICE TRG_authBootstrap(OMC_SessionDataPtr sdp, TRG_Security sec,
						const char *mac, IU8 *msg, IU32 msgLen);

/*!
 * Process boot trigger
 *
 * This function initializes the trigger information structure
 * for a bootstrap session.
 *
 * \param		msg			WBXML message content
 * \param		msgLen		Length of message content in bytes
 * \param		tip			Pointer to trigger information structure to be
 *							filled with trigger details.
 *
 * \return		OMC error code
 */
extern OMC_Error TRG_bootTrigger(IU8 *msg, IU32 msgLen, TRG_Info *tip);

/*!
 * Process DM user trigger
 *
 * This function initializes the trigger information structure
 * for a user initiated DM session.
 *
 * \param		serverId	DM server ID.
 * \param		tip			Pointer to trigger information structure to be
 *							filled with trigger details.
 *
 * \return		OMC error code
 */
#ifdef OMADM
extern OMC_Error TRG_dmUserTrigger(UTF8CStr serverId, TRG_Info *tip);
#endif

/*!
 * Process DS user trigger
 *
 * This function initializes the trigger information structure
 * for a user initiated DS session.
 *
 * \param		sdp			Session data pointer
 * \param		serverId	DS server ID.
 * \param		syncList	The head of the list of datastore sync requests.
 * \param		tip			Pointer to trigger information structure to be
 *							filled with trigger details.
 *
 * \return		OMC error code
 */
#ifdef OMADS
extern SLICE TRG_dsUserTrigger(OMC_SessionDataPtr sdp, UTF8CStr serverId,
						TRG_SyncPtr syncList, TRG_Info *tip);
#endif

/*!
 * Process DS resume trigger
 *
 * This function initializes the trigger information structure
 * for resuming a suspended DS session.
 *
 * \param		sdp			Session data pointer
 * \param		serverId	DS server ID.
 * \param		tip			Pointer to trigger information structure to be
 *							filled with trigger details.
 *
 * \return		OMC error code
 */
#ifdef OMADS
extern SLICE TRG_dsResumeTrigger(OMC_SessionDataPtr sdp, UTF8CStr serverId,
		TRG_Info *tip);
#endif

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_TRG_TRG_PRIVATE_H */
