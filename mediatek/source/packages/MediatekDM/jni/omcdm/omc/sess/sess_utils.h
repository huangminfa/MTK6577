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
 *		Name:				sess_utils.h
 *
 *		Project:			OMC
 *
 *		Derived From:		Original
 *
 *		Created On:			May 2004
 *
 *		Version:			$Id: //depot/main/base/omc/sess/sess_utils.h#23 $
 *
 *		Coding Standards:	3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
 *
]*/

/*! \internal
 * \file
 *		Utility functions for converting RTK data into
 *		Insignia types and structures and back again.
 *
 * \brief	Utility functions for RTK <--> Insignia conversions
 */

#ifndef _OMC_SESS_UTILS_H_
#define _OMC_SESS_UTILS_H_


#include <syncml/sml/smldtd.h>
#include <omc/sess/sess_private.h>
#ifdef OMADM
#include <omc/sess/dm/sess_dm_utils.h>
#endif


#ifdef __cplusplus
extern "C" {
#endif

#ifdef OMC_MEM_DEBUG
#define SESS_iu32ToPcdata(sdp,v) SESS_iu32ToPcdataDB(sdp,v,__FILE__,__LINE__)
extern SmlPcdataPtr_t SESS_iu32ToPcdataDB(OMC_SessionDataPtr sdp, IU32 val,
										  char* f, int l);
#else
extern SmlPcdataPtr_t SESS_iu32ToPcdata(OMC_SessionDataPtr sdp, IU32 val);
#endif

extern OMC_Error SESS_pcdataToIU32(OMC_SessionDataPtr sdp, IU32 *store,
								   SmlPcdataPtr_t pcdata);

#ifdef OMC_MEM_DEBUG
#define SESS_dataToStr(store,dPtr) \
		SESS_dataToStrDB(store,dPtr,__FILE__,__LINE__);
extern OMC_Error SESS_dataToStrDB(UTF8Str *store, SESS_DataPtr dPtr,
		char *f,int l);
#else
extern OMC_Error SESS_dataToStr(UTF8Str *store, SESS_DataPtr dPtr);
#endif

#ifdef OMC_MEM_DEBUG
#define SESS_pcdataToStr(sdp,dest,pcdata) \
		SESS_pcdataToStrDB(sdp,dest,pcdata,__FILE__,__LINE__);
extern OMC_Error SESS_pcdataToStrDB(OMC_SessionDataPtr sdp, UTF8Str *dest,
									SmlPcdataPtr_t pcdata, char* f, int l);

#define SESS_strToPcdata(sdp,dst,str) \
		SESS_strToPcdataDB(sdp,dst,str,__FILE__,__LINE__)
extern OMC_Error SESS_strToPcdataDB(OMC_SessionDataPtr sdp, SmlPcdataPtr_t
									*dst, UTF8CStr str, char* f, int l);

#else
extern OMC_Error SESS_pcdataToStr(OMC_SessionDataPtr sdp, UTF8Str *dest,
								  SmlPcdataPtr_t pcdata);
extern OMC_Error SESS_strToPcdata(OMC_SessionDataPtr sdp, SmlPcdataPtr_t *dst,
								  UTF8CStr str);
#endif


extern OMC_Error SESS_pcdataToMeta(OMC_SessionDataPtr sdp, SESS_MetaPtr
								   *metaDest, SmlPcdataPtr_t metaSrc);
extern OMC_Error SESS_pcdataToData(OMC_SessionDataPtr sdp, SESS_DataPtr
								   *dataDest, SmlPcdataPtr_t dataSrc);
extern OMC_Error SESS_metaToPcdata(OMC_SessionDataPtr sdp, SmlPcdataPtr_t *ptr,
								   SESS_MetaPtr mPtr);
extern OMC_Error SESS_dataToPcdata(OMC_SessionDataPtr sdp, SmlPcdataPtr_t *ptr,
								   SESS_DataPtr dPtr);
#ifdef OMADS
extern OMC_Error SESS_devinfToPcdata(OMC_SessionDataPtr sdp, SmlPcdataPtr_t
									 *ptr, SESS_DevinfPtr dPtr);
#endif

extern OMC_Error SESS_copyItemList(OMC_SessionDataPtr sdp, SESS_ItemPtr *items,
		SmlItemListPtr_t listPtr);
extern OMC_Error SESS_decodeItemData(SESS_ItemPtr itemPtr);

extern void SESS_freeData(SESS_DataPtr dataDest);
extern void SESS_freeMeta(SESS_MetaPtr meta);
extern void SESS_freeItems(SESS_ItemPtr items);
#ifdef OMADS
extern void SESS_freeMapItems(SESS_MapItemPtr mappings);
extern void SESS_freeDevinf(SESS_DevinfPtr devinfPtr);
#endif
extern void SESS_freeCmd(SESS_CmdPtr cmdPtr);

extern void SESS_parseURI(UTF8CStr targetURI, IU32 length, UTF8CStr *attrib);
extern void SESS_decodeRFC2396format(UTF8Str dest, UTF8CStr val);

extern void SESS_setResultCodes(SESS_ItemPtr itPtr, IU32 resultCode);

extern IU32 SESS_getAvailableBuffer(OMC_SessionDataPtr sdp);

extern OMC_Error SESS_allocCmd(SESS_CmdPtr* pCmd, IBOOL cmdMeta,
		IU32 itemCount, IBOOL itemMeta, IBOOL itemData);

extern OMC_Error SESS_allocItem(SESS_ItemPtr* pItem, IBOOL meta, IBOOL data);

extern void SESS_freeAuth(SESS_AuthPtr auth);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_SESS_UTILS_H_ */
