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
 *      Project:    	    OMC
 *
 *      Name:				xlttagtbl.h
 *
 *      Derived From:		Original
 *
 *      Created On:			May 2004
 *
 *      Version:			$Id: //depot/main/base/syncml/sml/xlt/xlttagtbl.h#4 $
 *
 *      Coding Standards:	3.0
 *
 *      Purpose:            SyncML core code
 *
 *      (c) Copyright Insignia Solutions plc, 2004 - 2005
 *
]*/

/**
 * @file
 * Definition of en-/decoder tagtable data
 *
 * @target_system   all
 * @target_os       all
 */

/*
 * Copyright Notice
 * Copyright (c) Ericsson, IBM, Lotus, Matsushita Communication
 * Industrial Co., Ltd., Motorola, Nokia, Openwave Systems, Inc.,
 * Palm, Inc., Psion, Starfish Software, Symbian, Ltd. (2001).
 * All Rights Reserved.
 * Implementation of all or part of any Specification may require
 * licenses under third party intellectual property rights,
 * including without limitation, patent rights (such a third party
 * may or may not be a Supporter). The Sponsors of the Specification
 * are not responsible and shall not be held responsible in any
 * manner for identifying or failing to identify any or all such
 * third party intellectual property rights.
 *
 * THIS DOCUMENT AND THE INFORMATION CONTAINED HEREIN ARE PROVIDED
 * ON AN "AS IS" BASIS WITHOUT WARRANTY OF ANY KIND AND ERICSSON, IBM,
 * LOTUS, MATSUSHITA COMMUNICATION INDUSTRIAL CO. LTD, MOTOROLA,
 * NOKIA, PALM INC., PSION, STARFISH SOFTWARE AND ALL OTHER SYNCML
 * SPONSORS DISCLAIM ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO ANY WARRANTY THAT THE USE OF THE INFORMATION
 * HEREIN WILL NOT INFRINGE ANY RIGHTS OR ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT
 * SHALL ERICSSON, IBM, LOTUS, MATSUSHITA COMMUNICATION INDUSTRIAL CO.,
 * LTD, MOTOROLA, NOKIA, PALM INC., PSION, STARFISH SOFTWARE OR ANY
 * OTHER SYNCML SPONSOR BE LIABLE TO ANY PARTY FOR ANY LOSS OF
 * PROFITS, LOSS OF BUSINESS, LOSS OF USE OF DATA, INTERRUPTION OF
 * BUSINESS, OR FOR DIRECT, INDIRECT, SPECIAL OR EXEMPLARY, INCIDENTAL,
 * PUNITIVE OR CONSEQUENTIAL DAMAGES OF ANY KIND IN CONNECTION WITH
 * THIS DOCUMENT OR THE INFORMATION CONTAINED HEREIN, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH LOSS OR DAMAGE.
 *
 * The above notice and this paragraph must be included on all copies
 * of this document that are made.
 *
 */

#ifndef _XLT_TAGTBL_H
#define _XLT_TAGTBL_H


/*************************************************************************/
/*  Definitions                                                          */
/*************************************************************************/

#include <syncml/sml/xlt/xlttags.h>
#include <syncml/sml/xlt/xltdec.h>
#include <syncml/sml/xlt/xltdeccom.h>

typedef struct Tag_s
{
	XltTagID_t  id;
	Byte_t      wbxml;
	String_t    xml;
} Tag_t, *TagPtr_t;

/**
 * buildXXX
 *
 * These functions each decode one single SyncML element starting at the
 * current position within the SyncML document. Child elements are build
 * recursively.
 * The functions check that the pointer to the memory structures are
 * NULL when called and return an error otherwise. This will only happen
 * when a SyncML element contains several child elements of the same type
 * for which this is not allowed according to the SyncML DTD; e.g. a
 * SyncHdr with two or more MsgID tags. Items and other list types
 * are handled separately by the appendXXXList functions (see below).
 *
 * @pre ppElem is NULL
 *      The scanner's current token is the start tag (may be
 *      empty) of the SyncML element to be decoded.
 * @post The scanner's current token is the end tag (or empty
 *       start tag) of the SyncML element to be decoded.
 */
/* implemented in xltdec.c! */
Ret_t buildAlert(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
#if (defined ATOMIC_RECEIVE || defined SEQUENCE_RECEIVE)
Ret_t buildAtomOrSeq(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
#endif
Ret_t buildChal(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildCred(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildDelete(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildExec(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildGenericCmd(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildItem(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildMap(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildMapItem(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildPCData(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildPCDataList(XltDecoderPtr_t pDecoder, VoidPtr_t *ppPCData);
Ret_t buildPutOrGet(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildResults(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
#ifdef SEARCH_RECEIVE
Ret_t buildSearch(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
#endif
Ret_t buildStatus(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildSync(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildSyncHdr(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildTarget(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildSource(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildFilter(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);
Ret_t buildFieldOrRecord(XltDecoderPtr_t pDecoder, VoidPtr_t *ppElem);

#ifdef NOWSM
const // without WSM, the tag table is a global read-only constant
#endif
TagPtr_t getTagTable(SmlPcdataExtension_t cp);

#endif	  /* _XLT_TAGTBL_H */

