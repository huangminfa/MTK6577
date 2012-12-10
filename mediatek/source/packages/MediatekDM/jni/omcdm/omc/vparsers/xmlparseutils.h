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
 *      Project:            OMC
 *
 *      Name:               xmlparseutils.h
 *
 *      Derived From:       Original
 *
 *      Created On:         24 January 2006
 *
 *      Version:            $Id: //depot/main/base/omc/vparsers/xmlparseutils.h#2 $
 *
 *      Coding Standards:   3.0
 *
 *      Purpose:            OMC DS
 *
 *      (c) Copyright Insignia Solutions plc, 2006
 *
]*/

/*! \internal
 * \file
 *		XML parsing utility functions.
 *
 * \brief	XML parsing utility functions.
 */

#ifndef _XMLPARSEUTILS_H
#define _XMLPARSEUTILS_H

#ifdef __cplusplus
extern "C" {
#endif

extern IS32 OMC_xmlEncodeBufferLen(const void *inptr, IS32 inlen);

extern IS32 OMC_xmlEncodeSpecial(const void *inptr, IS32 inlen,
						  void *outptr, IS32 outlen, const void *special);

/**
 * Normal version of XML encoding when no extra characters need to be encoded.
 * @param ip	pointer to input buffer.
 * @param il	length of input buffer (bytes).
 * @param op	pointer to output buffer (or NULL).
 * @param ol	length of output buffer (or zero).
 * @return number of characters (excluding terminating NUL) in the output
 * string; if this is greater than the length of the output buffer then the
 * buffer will have been filled and not terminated with a NUL.
 */
#define OMC_xmlEncode(ip,il,op,ol)	OMC_xmlEncodeSpecial(ip,il,op,ol,NULL)

extern IS32 OMC_xmlDecodeBufferLen(const void *inptr, IS32 inlen);

extern IS32 OMC_xmlDecode(const void *inptr, IS32 inlen,
						  void *outptr, IS32 outlen);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif
