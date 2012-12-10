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
 *		Name:				sess_dm_processcmds.h
 *
 *		Project:			OMC
 *
 *		Derived From:		//depot/main/base/omc/sess/sess_get.h#3
 *
 *		Created On:			September 2005
 *
 *		Version:			$Id: //depot/main/base/omc/sess/dm/sess_dm_processcmds.h#2 $
 *
 *		Coding Standards:	3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
 *
]*/

/*! \internal
 * \file
 *		This file lists the prototype for the functions for processing
 *		DM SyncML commands.
 *
 * \brief	Perform DM commands
 */

#ifndef _OMC_SESS_DM_SESS_DM_PROCESSCMDS_H_
#define _OMC_SESS_DM_SESS_DM_PROCESSCMDS_H_

#ifdef OMADM

#ifdef __cplusplus
extern "C" {
#endif

extern SLICE SESS_dmGetCmd(OMC_SessionDataPtr sdp, SESS_ItemPtr itemPtr,
			SESS_ResultsPtr resultsPtr);

extern SLICE SESS_dmExecCmd(OMC_SessionDataPtr sdp, SESS_ItemPtr itemPtr,
			void* data, IU32 length);

extern SLICE SESS_dmAddCmd(OMC_SessionDataPtr sdp, SESS_ItemPtr itemPtr,
			SESS_MetaPtr metaPtr, SESS_DataPtr dataPtr);

extern SLICE SESS_dmDeleteCmd(OMC_SessionDataPtr sdp, SESS_ItemPtr itemPtr);

extern SLICE SESS_dmReplaceCmd(OMC_SessionDataPtr sdp, SESS_ItemPtr itemPtr,
			SESS_MetaPtr metaPtr, SESS_DataPtr dataPtr);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* OMADM */

#endif /* _OMC_SESS_DM_SESS_DM_PROCESSCMDS_H_ */
