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
 *		Name:					wbxml_omacp.h
 *
 *		Project:				OMC
 *
 *		Created On:				July 2004
 *
 *		Derived From:			(original)
 *
 *		Version:				$Id: //depot/main/base/omc/wbxml/wbxml_omacp.h#3 $
 *
 *		Coding Standards:		3.0
 *
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2005
]*/

/*! \internal
 * \file
 *		Define WBXML token values and functions for OMA Client Provisioning
 *
 * \brief	OMA-CP WBXML definitions
 */

#ifndef _OMA_WBXML_WBXML_OMACP_H_
#define _OMA_WBXML_WBXML_OMACP_H_

/*===========================================================================*/
/* WBXML definitions specific to OMA Client Provisioning                     */
/*===========================================================================*/

/*
 * WBXML public identifier for "-//WAPFORUM//DTD PROV 1.0//EN"
 */
#define OMACP_WBXML_IDENTIFIER	0x0B

/*
 * Maximum nesting depth for <characteristic> elements
 */
#define OMACP_MAXDEPTH			2

/*
 * Maximum bytes needed for an attribute name (including NUL).
 */
/* #define MAX_ATTR_NAME_BYTES		See globals_if.h */

/*
 * Maximum bytes needed for an attribute value (including NUL).
 */
/* #define MAX_ATTR_VALUE_BYTES		See globals_if.h */

/*
 * Number of code pages (attribute code space)
 */
#define NCODEPAGES				2

/*
 * OMA-CP element tokens
 *
 * The [PROVCONT] spec indicates that CHARACTERISTIC_TAG and PARM_TAG are
 * valid in code page 1 as well as code page 0: this seems irrelevant,
 * however, since WBXML requires that the current code page is maintained
 * separately for tag and attribute code spaces, and there is no reason for
 * an OMA-CP message to switch code page in tag code space.
 */
#define WAP_PROVISIONINGDOC_TAG		(0x05 | HAS_ATTRIBUTES | HAS_CONTENT)
#define CHARACTERISTIC_TAG			(0x06 | HAS_ATTRIBUTES | HAS_CONTENT)
#define PARM_TAG					(0x07 | HAS_ATTRIBUTES)

/*
 * OMA-CP attribute start tokens
 *
 * Only the principal tokens are defined here: the full set including
 * tokens for predefined attribute values are listed in the tables in
 * wbxml_omacp.c.
 */

/* Element <wap-provisioningdoc> */
#define AS_VERSION					0x45	/* attribute "version" */
#define AS_VERSION_1_0				0x46	/* version="1.0" */

/* Element <characteristic> */
#define AS_TYPE						0x50	/* attribute "type" */

/* Element <parm> */
#define AS_NAME						0x05	/* attribute "name" */
#define AS_VALUE					0x06	/* attribute "value" */

/*===========================================================================*/
/* Data structures to hold parser output								     */
/*===========================================================================*/

/* <characteristic> element */
typedef struct OMACP_Characteristic_s
{
	UTF8Str							type;		/* "type" attribute */
	struct OMACP_Parm_s				*parmHead;	/* head of parameter list */
	struct OMACP_Parm_s				*parmTail;	/* tail of parameter list */
	struct OMACP_Characteristic_s	*childHead;	/* head of child list */
	struct OMACP_Characteristic_s	*childTail;	/* tail of child list */
	struct OMACP_Characteristic_s	*next;		/* next sibling */
} OMACP_Characteristic;

/* <parm> element */
typedef struct OMACP_Parm_s
{
	UTF8Str					name;		/* "name" attribute */
	UTF8Str					value;		/* "value" attribute */
	struct OMACP_Parm_s		*next;		/* next parameter in list */
} OMACP_Parm;

/*===========================================================================*/
/* Macros																	 */
/*===========================================================================*/

/*
 * Append characteristic to child list of specified parent characteristic
 */
#define OMACP_appendCharacteristic(pParent, pChar)			\
	do {													\
		OMC_assert((pParent) != NULL && (pChar) != NULL &&	\
				   (pChar)->next == NULL); 					\
		if ((pParent)->childTail == NULL)					\
			(pParent)->childHead = (pChar);					\
		else												\
			(pParent)->childTail->next = (pChar);			\
															\
		(pParent)->childTail = (pChar);						\
	} while (0)

/*
 * Append parameter to parameter list of specified parent characteristic
 */
#define OMACP_appendParm(pParent, pParm)					\
	do {													\
		OMC_assert((pParent) != NULL && (pParm) != NULL &&	\
				   (pParm)->next == NULL); 					\
		if ((pParent)->parmTail == NULL)					\
			(pParent)->parmHead = (pParm);					\
		else												\
			(pParent)->parmTail->next = (pParm);			\
															\
		(pParent)->parmTail = (pParm);						\
	} while (0)

/*===========================================================================*/
/* Function Prototypes														 */
/*===========================================================================*/

/*!
 * Attempt to decode an OMA Client Provisioning WBXML message. The output
 * tree of characteristics and parameters is returned in pRoot.
 * The caller must use WBXML_destroyTree() to free the output tree when
 * it is finished with the data.  Note that WBXML_destroyTree() must be
 * called even if this function returns an error.
 *
 * \param	sdp			Pointer to session data
 * \param	pbMsg		The WBXML message to decode.
 * \param	pRoot		Pointer to root characteristic structure: note
 *						that this is a dummy, the first characteristic
 *						will be stored in pRoot->childHead.
 *
 * \return	OMC error code
 */
extern OMC_Error WBXML_decodeOMACPMessage(OMC_SessionDataPtr sdp,
						IBuffer *pbMsg, OMACP_Characteristic *pRoot);

/*!
 * Free parser output tree
 *
 * \input	pChar		Pointer to a node in the parser output tree. For the
 *						initial call this should be the root node.
 */
extern void WBXML_destroyTree(OMACP_Characteristic *pChar);

#ifndef PROD
/*!
 * Dump parser output tree
 *
 * \input	pChar		Pointer to a node in the parser output tree. For the
 *						initial call this should be the root node.
 * \input	depth		Nesting depth: zero for the initial call.
 */
extern void WBXML_dumpTree(OMACP_Characteristic *pChar, IU32 depth);
#endif

#endif /* !_OMA_WBXML_WBXML_OMACP_H_ */
