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
 *		Name:				sess_dm_utils.h
 *
 *		Project:			OMC
 *
 *		Derived From:		//depot/main/base/omc/sess/sess_utils.h#12
 *
 *		Created On:			July 2005
 *
 *		Version:			$Id: //depot/main/base/omc/sess/dm/sess_dm_utils.h#3 $
 *
 *		Coding Standards:	3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
 *
]*/

/*! \internal
 * \file
 *		DM specific utility functions
 *
 * \brief	DM utility functions
 */

#ifndef _OMC_SESS_DM_SESS_DM_UTILS_H_
#define _OMC_SESS_DM_SESS_DM_UTILS_H_

#ifdef OMADM

#ifdef __cplusplus
extern "C" {
#endif


#ifdef OMC_MEM_DEBUG

#define SESS_dmFetchAccountValue(s,n,v,l) \
		SESS_dmFetchAccountValueDB(s,n,v,l,__FILE__,__LINE__)

extern SLICE SESS_dmFetchAccountValueDB(OMC_SessionDataPtr sdp,
			UTF8CStr name, void** pValue, IU32* pLength, char* f, int l);

#define SESS_dmFetchTreeValue(s,n,v,l) \
		SESS_dmFetchTreeValueDB(s,n,v,l,__FILE__,__LINE__)

extern SLICE SESS_dmFetchTreeValueDB(OMC_SessionDataPtr sdp,
			UTF8CStr name, void** pValue, IU32* pLength, char* f, int l);

#else /* OMC_MEM_DEBUG */

extern SLICE SESS_dmFetchAccountValue(OMC_SessionDataPtr sdp,
			UTF8CStr name, void** pValue, IU32* pLength);

extern SLICE SESS_dmFetchTreeValue(OMC_SessionDataPtr sdp,
			UTF8CStr name, void** pValue, IU32* pLength);

#endif /* OMC_MEM_DEBUG */

extern SLICE SESS_dmSetAccountValue(OMC_SessionDataPtr sdp,
			UTF8CStr name, void* value, IU32 length);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* OMADM */

#endif /* !_OMC_SESS_DM_SESS_DM_UTILS_H_ */
