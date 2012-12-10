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
 *		Name:				tman_session.h
 *
 *		Project:			OMC
 *
 *		Derived From:		Original
 *
 *		Created On:			September 2005
 *
 *		Version:			$Id: //depot/main/base/omc/tman/tman_session.h#4 $
 *
 *		Coding Standards:	3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2005 - 2006
 *
]*/

/*! \internal
 * \file
 *		Defines the session data for TMAN and TREE
 *
 * \brief	TMAN/TREE session data
 */

#ifndef _OMC_TMAN_TMAN_SESSION_H_
#define _OMC_TMAN_TMAN_SESSION_H_

#ifdef OMADM

#include <omc/omc_timeslice.h>
#include <omc/tree/tree.h>			/* For TREE_NodeRef */
#include <omc/tree/tree_if.h>

#ifdef __cplusplus
extern "C" {
#endif


typedef struct FindChildInfo_s
{
	UTF8CStr		name;
	const void *	data;
	IU32			dLength;
	void *			buffer;
	TREE_NodeRef *	pNode;
} FindChildInfo, *FindChildInfoPtr;


/*
 * Structure used for holding session specific data.
 */
struct OMC_TreeSession_s
{
	OMC_UserDataPtr				udp;
	UTF8Str						accessId;


/********** General time slicing **********/

	DEF_SLICE_SESSION


/********** tman_add.c **********/

	struct {
		DEF_YIELD;
		TREE_NodeRef			node;
	} TMAN_addInterior;

	struct {
		DEF_YIELD;
		TREE_NodeRef			node;
	} TMAN_addLeaf;

	struct {
		DEF_YIELD;
		TREE_NodeRef			node;
	} TMAN_loAddStart;


/********** tman_delete.c **********/

	struct {
		DEF_YIELD;
		TREE_NodeRef			node;
		TREE_NodeRef			serverIdNode;
		IU32					length;
		IU32					idLength;
		IU8 *					serverId;
#ifdef OMC_TREE_FGS
		OMC_Yield				result;
#endif
	} TMAN_deleteNode;

	struct {
		DEF_YIELD;
		IU8 					buffer[100];
		IU32					length;
		IU32					aclLength;
		UTF8Str					acl;
		IU8 *					aclEnd;
		UTF8Str					ptr;
		UTF8Str					found;
		UTF8Str					op;
		IU32					opLength;
		UTF8Str					serverId;
		IU32					serverIdLength;
		IBOOL					changed;
		IU8						name;
#ifdef OMC_TREE_FGS
		IU8 *					pbuffer;
#endif
	} pruneAcl;


/********** tman_exec.c **********/

	struct {
		DEF_YIELD;
		TREE_NodeRef			node;
	} TMAN_execNode;


/********** tman_find.c **********/

	struct {
		DEF_YIELD;
		TREE_NodeRef			node;
	} TMAN_findValue;


/********** tman_get.c **********/

	struct {
		DEF_YIELD;
		TREE_NodeRef			node;
	} TMAN_getProperty;

	struct {
		DEF_YIELD;
		TREE_NodeRef			node;
	} TMAN_getValue;


/********** tman_replace.c **********/

	struct {
		DEF_YIELD;
		TREE_NodeRef			node;
	} TMAN_replaceProperty;

	struct {
		DEF_YIELD;
		TREE_NodeRef			node;
		OMC_Error				result;
	} TMAN_replaceValue;

	struct {
		DEF_YIELD;
		TREE_NodeRef			node;
	} TMAN_setValueOnly;

	struct {
		DEF_YIELD;
		TREE_NodeRef			node;
	} TMAN_loReplaceStart;

	struct {
		DEF_YIELD;
		TREE_NodeRef			node;
	} TMAN_loReplaceAbort;

	struct {
		DEF_YIELD;
		TREE_NodeRef			node;
	} TMAN_loStoreData;


/********** tree_exec.c **********/

#ifdef TREE_IN_MEMORY
	struct {
		DEF_YIELD;
	} TREE_execNode;
#endif

/********** tree_find.c **********/

#ifdef TREE_IN_MEMORY
	struct {
		DEF_YIELD;
		IU32					length;
	} checkNodeValue;

	struct {
		DEF_YIELD;
		FindChildInfo			cfi;
	} TREE_findLeafByValue;

	struct {
		DEF_YIELD;
		TREE_NodeRef			start;
		TREE_NodeRef			node;
		IBOOL					first;
	} TREE_walkTree;
#endif


/********** tree_get.c **********/

#ifdef TREE_IN_MEMORY
	struct {
		DEF_YIELD;
		IU32					size;
	} TREE_getProperty;

	struct {
		DEF_YIELD;
	} TREE_getValue;

	struct {
		DEF_YIELD;
	} TREE_setValue;

	struct {
		DEF_YIELD;
	} TREE_loPrepare;

	struct {
		DEF_YIELD;
	} TREE_loAbort;

	struct {
		DEF_YIELD;
	} TREE_loStore;
#endif


/********** tree_read.c **********/

#if defined(OMADM) && !defined(OMC_TREE_FGS)
	struct {
		DEF_YIELD;
		OMC_TREE_PersistenceWriteFunc	func;
		void *							context;
		OMC_Error						result;
	} TREE_write;

	struct {
		GLOBAL_DEF_YIELD;
		OMC_TREE_PersistenceReadFunc	func;
		void *							context;
		OMC_Error						result;
	} TREE_read;
#endif


/********** tstore_read.c **********/

#ifdef OMC_TREE_FGS
	struct {
		DEF_SLICE;
	} TSTORE_resyncTree;
#endif


/********** tstore_write.c **********/

#ifdef OMC_TREE_FGS
	struct {
		DEF_SLICE;
	} TSTORE_writeNodeOrResync;

	struct {
		DEF_SLICE;
		OMC_TSTORE_Node			aNode;
	} TSTORE_writeNode;
#endif

};


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* OMADM */

#endif /* !_OMC_TMAN_TMAN_SESSION_H_ */
