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
 *		Name:				sess_auth.h
 *
 *		Project:			OMC
 *
 *		Derived From:		Original
 *
 *		Created On:			June 2004
 *
 *		Version:			$Id: //depot/main/base/omc/sess/sess_auth.h#11 $
 *
 *		Coding Standards:	3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
 *
]*/

/*! \internal
 * \file
 *		Prototypes for functions used for authenticating
 *
 * \brief	Authentication utilities
 */

#ifndef _OMC_SESS_SESS_AUTH_H_
#define _OMC_SESS_SESS_AUTH_H_

#include <omc/sess/sess_private.h>

#ifdef __cplusplus
extern "C" {
#endif

extern SESS_AuthType SESS_getAuthType(UTF8CStr metaType);

/* Authenticating client to server */
extern OMC_Error SESS_prepareUserCred(OMC_SessionDataPtr sdp,
									  SmlCredPtr_t *userCredPtr,
									  UTF8CStr credType,
									  UTF8CStr credFormat, UTF8CStr credData);

#ifdef OMADS
extern void SESS_freeCmdCred(SESS_CmdPtr cmd);

extern OMC_Error SESS_setCmdCred(SESS_AuthPtr auth, SESS_CmdPtr cmd,
	SESS_CmdPtr status);
#endif

extern OMC_Error SESS_createBasicDigest(SESS_AuthPtr auth, UTF8Str* pDigest);

extern OMC_Error SESS_createMD5Digest(SESS_AuthPtr auth, IBOOL useNewNonce,
	UTF8Str* pDigest);

extern OMC_Error SESS_createHMACDigest(SESS_AuthPtr auth, const IU8* message,
	IU32 mLength, UTF8Str* pDigest);

extern OMC_Error SESS_storeChallenge(OMC_SessionDataPtr sdp,
	SESS_MetaPtr meta);

extern SLICE SESS_updateClientNonce(OMC_SessionDataPtr sdp);

/* Authenticating server to client */
extern OMC_Error SESS_prepareChallenge(OMC_SessionDataPtr sdp,
									   SESS_AuthPtr auth,
									   SmlChalPtr_t *chalPtr);

extern OMC_Error SESS_checkCredentials(OMC_SessionDataPtr sdp,
	SESS_AuthType authType);

#ifdef OMADS
extern OMC_Error SESS_checkCmdCred(SESS_CmdPtr cmd, SESS_AuthPtr auth);
#endif

extern OMC_Error SESS_checkHMACCredentials(OMC_SessionDataPtr sdp,
	UTF8CStr username, UTF8CStr mac, UTF8CStr algorithm, IU8* message,
	IU32 mLength);

extern SLICE SESS_updateServerNonce(OMC_SessionDataPtr sdp);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_SESS_SESS_AUTH_H_ */
