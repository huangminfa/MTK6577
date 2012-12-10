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
 *		Name:				callbacks.h
 *
 *		Project:			OMC
 *
 *		Derived From:		Original
 *
 *		Created On:			May 2004
 *
 *		Version:			$Id: //depot/main/base/omc/sess/callbacks.h#4 $
 *
 *		Coding Standards:	3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2005
 *
]*/

/*! \internal
 * \file
 *		This file lists the prototypes for the callback functions
 *		we have implemented.
 *
 * \brief	Provide the callbacks required by the RTK
 */

#ifndef _OMC_SESS_CALLBACKS_H_
#define _OMC_SESS_CALLBACKS_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <syncml/sml/smlerr.h>
#include <syncml/sml/smldtd.h>
#include <syncml/sml/smldef.h>

extern Ret_t omcStartMessageCmd(InstanceID_t id, VoidPtr_t userData,
		SmlSyncHdrPtr_t pSyncHdr);
extern Ret_t omcEndMessageCmd(InstanceID_t id, VoidPtr_t userData,
		Boolean_t final);
extern Ret_t omcStartAtomic(InstanceID_t id, VoidPtr_t userData,
		SmlAtomicPtr_t pAtomic);
extern Ret_t omcEndAtomic (InstanceID_t id, VoidPtr_t userData);
extern Ret_t omcStartSequence(InstanceID_t id, VoidPtr_t userData,
		SmlSequencePtr_t pSequence);
extern Ret_t omcEndSequence (InstanceID_t id, VoidPtr_t userData);
extern Ret_t omcAddCmd(InstanceID_t id, VoidPtr_t userData, SmlAddPtr_t pAdd);
extern Ret_t omcAlertCmd(InstanceID_t id, VoidPtr_t userData,
		SmlAlertPtr_t pAlert);
extern Ret_t omcDeleteCmd(InstanceID_t id, VoidPtr_t userData,
		SmlDeletePtr_t pDelete);
extern Ret_t omcGetCmd(InstanceID_t id, VoidPtr_t userData, SmlGetPtr_t pGet);
extern Ret_t omcStatusCmd(InstanceID_t id, VoidPtr_t userData,
		SmlStatusPtr_t pStatus);
extern Ret_t omcReplaceCmd(InstanceID_t id, VoidPtr_t userData,
		SmlReplacePtr_t pReplace);
extern Ret_t omcCopyCmd(InstanceID_t id, VoidPtr_t userData,
		SmlCopyPtr_t pCopy);
extern Ret_t omcMoveCmd(InstanceID_t id, VoidPtr_t userData,
		SmlCopyPtr_t pCopy);
extern Ret_t omcExecCmd(InstanceID_t id, VoidPtr_t userData,
		SmlExecPtr_t pExec);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_SESS_CALLBACKS_H_ */
