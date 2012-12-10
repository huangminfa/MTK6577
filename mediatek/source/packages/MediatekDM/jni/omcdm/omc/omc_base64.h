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
 *		Name:					omc_base64.h
 *
 *		Project:				OMC
 *
 *		Created On:				June 2004
 *
 *		Derived From:			Original
 *
 *		Version:				$Id: //depot/main/base/omc/omc_base64.h#8 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2005 - 2006
]*/

/*! \internal
 * \file
 * \brief
 *		Provides Base64 encoding and decoding functions.
 */

#ifndef _OMC_OMC_BASE64_H_
#define _OMC_OMC_BASE64_H_

#ifdef __cplusplus
extern "C" {
#endif

extern IU32 OMC_b64EncodeLength(IU32 decLength);

extern OMC_Error OMC_b64Encode(const void* decData, IU32 decLength,
							   void* encData, IU32* pEncLength,
							   IU32 encMaxLength);

extern OMC_Error OMC_b64Decode(const void* encData, IU32 encLength,
							   void* decData, IU32* decLength,
							   IU32 decMaxLength);

#ifdef OMC_MEM_DEBUG

#define OMC_b64EncodeAlloc(dD,dL,eD,eL) \
		OMC_b64EncodeAllocDB(dD,dL,eD,eL,__FILE__,__LINE__)

extern OMC_Error OMC_b64EncodeAllocDB(const void* decData, IU32 decLength,
									  IU8** pEncData, IU32* pEncLength,
									  char* f, int l);

#define OMC_b64DecodeAlloc(eD,eL,dD,dL) \
		OMC_b64DecodeAllocDB(eD,eL,dD,dL,__FILE__,__LINE__)

extern OMC_Error OMC_b64DecodeAllocDB(const void* encData, IU32 encLength,
									  IU8** pDecData, IU32* decLength,
									  char* f, int l);


#else

extern OMC_Error OMC_b64EncodeAlloc(const void* decData, IU32 decLength,
									IU8** pEncData, IU32* pEncLength);

extern OMC_Error OMC_b64DecodeAlloc(const void* encData, IU32 encLength,
									IU8** pDecData, IU32* decLength);

#endif


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif/* !_OMC_OMC_BASE64_H_ */
