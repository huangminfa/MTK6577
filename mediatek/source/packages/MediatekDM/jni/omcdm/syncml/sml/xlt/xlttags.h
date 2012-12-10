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
 *      Name:				xlttags.h
 *
 *      Derived From:		Original
 *
 *      Created On:			May 2004
 *
 *      Version:			$Id: //depot/main/base/syncml/sml/xlt/xlttags.h#6 $
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
 * Definition of WBXML/XML tags for the en-/decoder
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

/*************************************************************************/
/*  Definitions                                                          */
/*************************************************************************/

#ifndef _XLT_TAGS_H
#define _XLT_TAGS_H

#include <syncml/sml/smldtd.h>
#include <syncml/sml/smldef.h>

#define XML_MAX_TAGLEN 35

#ifdef _cplusplus
extern "C" {
#endif

	// Tag IDs
	typedef enum {
		TN_UNDEF = 0,

		TN_ADD,
		TN_ALERT,
		TN_ARCHIVE,
		TN_ATOMIC,
		TN_ATOMIC_END, /* 5 */
		TN_CHAL,
		TN_CMD,
		TN_CMDID,
		TN_CMDREF,
		TN_COPY,     /* 10 */
		TN_CRED,
		TN_DATA,
		TN_DELETE,
		TN_EXEC,
		TN_FINAL,    /* 15 */
		TN_GET,
		TN_ITEM,
		TN_LANG,
		TN_LOCNAME,
		TN_LOCURI,  /* 20 */
		TN_MAP,
		TN_MAPITEM,
		TN_META,
		TN_MSGID,
		TN_MSGREF,  /* 25 */
		TN_NORESP,
		TN_NORESULTS,
		TN_PUT,
		TN_REPLACE,
		TN_RESPURI, /* 30 */
		TN_RESULTS,
		TN_SEARCH,
		TN_SEQUENCE,
		TN_SEQUENCE_END,
		TN_SESSIONID, /* 35 */
		TN_SFTDEL,
		TN_SOURCE,
		TN_SOURCEREF,
		TN_STATUS,
		TN_SYNC, /* 40 */
		TN_SYNCBODY,
		TN_SYNCHDR,
		TN_SYNCML,
		TN_SYNC_END,
		TN_TARGET, /* 45 */
		TN_TARGETREF,
		TN_VERSION,
		TN_PROTO,
		TN_FIELD,
		TN_METINF_EMI, /* 50 */
		TN_METINF_FORMAT,
		TN_METINF_FREEID,
		TN_METINF_FREEMEM,
		TN_METINF_LAST,
		TN_METINF_MARK, /* 55 */
		TN_METINF_MAXMSGSIZE,
		TN_METINF_MEM,
		TN_METINF_METINF,
		TN_METINF_NEXT,
		TN_METINF_NEXTNONCE, /* 60 */
		TN_METINF_SHAREDMEM,
		TN_METINF_SIZE,
		TN_METINF_TYPE,
		TN_METINF_VERSION,
		TN_DEVINF_CTCAP,    /* 65 */
		TN_DEVINF_CTTYPE,
		TN_DEVINF_DATASTORE,
		TN_DEVINF_DATATYPE,
		TN_DEVINF_DEVID,
		TN_DEVINF_DEVINF, /* 70 */
		TN_DEVINF_DEVTYP,
		TN_DEVINF_DISPLAYNAME,
		TN_DEVINF_DSMEM,
		TN_DEVINF_EXT,
		TN_DEVINF_FWV, /* 75 */
		TN_DEVINF_HWV,
		TN_DEVINF_MAN,
		TN_DEVINF_MAXGUIDSIZE,
		TN_DEVINF_MAXID,
		TN_DEVINF_MAXMEM, /* 80 */
		TN_DEVINF_MOD,
		TN_DEVINF_OEM,
		TN_DEVINF_PARAMNAME,
		TN_DEVINF_PROPNAME,
		TN_DEVINF_RX, /* 85 */
		TN_DEVINF_RXPREF,
		TN_DEVINF_SHAREDMEM,
		TN_DEVINF_SIZE,
		TN_DEVINF_SOURCEREF,
		TN_DEVINF_SWV, /* 90 */
		TN_DEVINF_SYNCCAP,
		TN_DEVINF_SYNCTYPE,
		TN_DEVINF_TX,
		TN_DEVINF_TXPREF,
		TN_DEVINF_VALENUM, /* 95 */
		TN_DEVINF_VERCT,
		TN_DEVINF_VERDTD,
		TN_DEVINF_XNAM,
		TN_DEVINF_XVAL,
		/* SCTSTK - 18/03/2002, S.H. 2002-04-05 : SyncML 1.1 */
		TN_NUMBEROFCHANGES, /* 100 */
		TN_MOREDATA,
		TN_METINF_MAXOBJSIZE,
		TN_DEVINF_UTC,
		TN_DEVINF_NOFM,
		TN_DEVINF_LARGEOBJECT, /* 105 */

		/* CC 2005-06-20 : SyncML 1.2 */
		TN_FILTER,
		TN_RECORD,
		TN_FILTERTYPE,
		TN_SOURCEPARENT,
		TN_TARGETPARENT,		/* 110 */
		TN_MOVE,
		TN_METINF_ANCHOR,
		TN_CORRELATOR,
		TN_DEVINF_PROPERTY,
		TN_DEVINF_PROPPARAM,	/* 115 */
		TN_DEVINF_MAXOCCUR,
		TN_DEVINF_MAXSIZE,
		TN_DEVINF_NOTRUNCATE,
		TN_DEVINF_FILTERRX,
		TN_DEVINF_FILTERCAP,	/* 120 */
		TN_DEVINF_FILTERKEY,
		TN_DEVINF_FIELDLEVEL,
		TN_DEVINF_HIERARCHICAL,

		TAG_ID_MAXIMUM
	} XltTagID_t;


	extern const char * const SyncMLNamespaces[SML_NUM_VERS];

	typedef struct Dtd_s
	{
		String_t	         name;
		SmlPcdataExtension_t ext;
	} Dtd_t, *DtdPtr_t;


	Ret_t getTagString(XltTagID_t tagID, String_t tagString, SmlPcdataExtension_t ext);

#ifdef __SML_WBXML__
	Ret_t getTagByte(XltTagID_t tagID, SmlPcdataExtension_t ext, Byte_t *tagByte);
#endif

	Byte_t     getCodePage(SmlPcdataExtension_t ext);
	Ret_t getTagIDByStringAndExt(String_t tag, SmlPcdataExtension_t ext, XltTagID_t *pTagID);

#ifdef __SML_WBXML__
	Ret_t getTagIDByByteAndExt(Byte_t tag, SmlPcdataExtension_t ext, XltTagID_t *pTagID);
#endif

#ifdef __SML_XML__
	Ret_t getTagIDByStringAndNamespace(String_t tag, String_t ns, XltTagID_t *pTagID);
#endif

	Ret_t getExtById(XltTagID_t tagID, SmlPcdataExtension_t *pExt);

	// %%% luz:2003-07-31: added vers parameter
	Ret_t getExtName(SmlPcdataExtension_t ext, String_t *name, SmlVersion_t vers);
	Ret_t getTagIDByStringAndExt(String_t tag, SmlPcdataExtension_t ext, XltTagID_t *pTagID);
	SmlPcdataExtension_t getExtByName(String_t ns);


#ifdef _cplusplus
}
#endif

#endif
