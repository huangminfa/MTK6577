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
 *		Name:					tman.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			Original
 *
 *		Version:				$Id: //depot/main/base/omc/tman/tman.h#21 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
]*/

/*! \internal
 * \file
 *		Defines the API used to manipulate the DM tree.
 *
 * \brief	DM tree manager API
 */

#ifndef _OMC_TMAN_TMAN_H_
#define _OMC_TMAN_TMAN_H_

#ifdef OMADM

#ifdef __cplusplus
extern "C" {
#endif

/*!
 * The special ServerId used by OMC when it is accessing the tree for its
 * own purposes. This bypasses the ACL checks and therefore allows values
 * which cannot normally be fetched from the tree, such as the ClientPW,
 * to be fetched.
 */
extern const IU8 TMAN_BYPASS_ACL[];

/*
 * In some ports it is convenient to make the tree manager into a server.
 * To make it possible to implement the client TMAN functions as calls
 * to server interface functions and yet retain the OMC and tree code
 * unmodified it is necessary to ensure the functions in the client and
 * the server have different names. The following defines do this.
 */
#ifdef TMAN_CLIENT_SERVER_SPLIT
#ifdef TMAN_SERVER

#define TMAN_init				TMAN_SERVER_init
#define TMAN_term				TMAN_SERVER_term
#define TMAN_initSession		TMAN_SERVER_initSession
#define TMAN_termSession		TMAN_SERVER_termSession
#define TMAN_addInterior		TMAN_SERVER_addInterior
#define TMAN_addLeaf			TMAN_SERVER_addLeaf
#define TMAN_deleteNode			TMAN_SERVER_deleteNode
#define TMAN_getProperty		TMAN_SERVER_getProperty
#define TMAN_replaceProperty	TMAN_SERVER_replaceProperty
#define TMAN_getValue			TMAN_SERVER_getValue
#define TMAN_replaceValue		TMAN_SERVER_replaceValue
#define TMAN_setValueOnly		TMAN_SERVER_setValueOnly
#define TMAN_findValue			TMAN_SERVER_findValue
#define TMAN_execNode			TMAN_SERVER_execNode
#define TMAN_loAddStart			TMAN_SERVER_loAddStart
#define TMAN_loAddAbort			TMAN_SERVER_loAddAbort
#define TMAN_loReplaceStart		TMAN_SERVER_loReplaceStart
#define TMAN_loReplaceAbort		TMAN_SERVER_loReplaceAbort
#define TMAN_loStoreData		TMAN_SERVER_loStoreData

#else /* TMAN_SERVER */

#define TMAN_init				TMAN_CLIENT_init
#define TMAN_term				TMAN_CLIENT_term
#define TMAN_initSession		TMAN_CLIENT_initSession
#define TMAN_termSession		TMAN_CLIENT_termSession
#define TMAN_addInterior		TMAN_CLIENT_addInterior
#define TMAN_addLeaf			TMAN_CLIENT_addLeaf
#define TMAN_deleteNode			TMAN_CLIENT_deleteNode
#define TMAN_getProperty		TMAN_CLIENT_getProperty
#define TMAN_replaceProperty	TMAN_CLIENT_replaceProperty
#define TMAN_getValue			TMAN_CLIENT_getValue
#define TMAN_replaceValue		TMAN_CLIENT_replaceValue
#define TMAN_setValueOnly		TMAN_CLIENT_setValueOnly
#define TMAN_findValue			TMAN_CLIENT_findValue
#define TMAN_execNode			TMAN_CLIENT_execNode
#define TMAN_loAddStart			TMAN_CLIENT_loAddStart
#define TMAN_loAddAbort			TMAN_CLIENT_loAddAbort
#define TMAN_loReplaceStart		TMAN_CLIENT_loReplaceStart
#define TMAN_loReplaceAbort		TMAN_CLIENT_loReplaceAbort
#define TMAN_loStoreData		TMAN_CLIENT_loStoreData

#endif /* TMAN_SERVER */
#endif /* TMAN_CLIENT_SERVER_SPLIT */

/*!
===============================================================================
 * Initialise the tree manager.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Error TMAN_init(void);

/*!
===============================================================================
 * Clean up the tree manager before exiting.
===============================================================================
 */
extern void TMAN_term(void);

/*!
===============================================================================
 * Allocate session data for running TMAN/TREE sessions
 *
 * \param	udp			The user session data pointer.
 *
 * \param	pTsp		Where to store the tree session data pointer
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Error TMAN_initSession(OMC_UserDataPtr udp,
								  OMC_TreeSessionPtr* pTsp);

/*!
===============================================================================
 * Free a TMAN/TREE session data pointer
 *
 * \param	tsp			The tree session data pointer.
===============================================================================
 */
extern void TMAN_termSession(OMC_TreeSessionPtr tsp);

/*!
===============================================================================
 * Add an interior node to the DM tree.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	server		The server requesting the add.
 *
 * \param	aclServer	The server to put in the ACL if one has to be created.
 *
 * \param	path		The full pathname of the node to add.
 *
 * \param	type		A MIME type string or NULL to set no string.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMAN_addInterior(OMC_TreeSessionPtr tsp, UTF8CStr server,
	UTF8CStr aclServer, UTF8CStr path, UTF8CStr type);

/*!
===============================================================================
 * Add a leaf node to the DM tree.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	server		The server requesting the add.
 *
 * \param	path		The full pathname of the node to add.
 *
 * \param	type		A MIME type string or NULL to set no string.
 *
 * \param	format		A data format string or NULL to set no string.
 *
 * \param	data		A pointer to the value data.
 *
 * \param	length		The length of the value data in bytes. (May be 0)
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMAN_addLeaf(OMC_TreeSessionPtr tsp, UTF8CStr server,
	UTF8CStr path, UTF8CStr type, UTF8CStr format, void* data, IU32 length);

/*!
===============================================================================
 * Delete a node from the DM tree.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	server		The server requesting the delete.
 *
 * \param	path		The full pathname of the node to delete.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMAN_deleteNode(OMC_TreeSessionPtr tsp, UTF8CStr server,
	UTF8CStr path);

/*!
===============================================================================
 * Get a node property from the DM tree.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	server		The server requesting the get.
 *
 * \param	path		The full pathname of the node.
 *
 * \param	name		The name of the property.
 *
 * \param	buffer		Where to store the property.
 *
 * \param	bLength		The length of the supplied buffer. (May be 0)
 *
 * \param	pLength		Where to store the property length.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error,
 *			OMC_ERR_BUFFER_OVERFLOW if the supplied buffer is not long enough)
===============================================================================
 */
extern OMC_Yield TMAN_getProperty(OMC_TreeSessionPtr tsp, UTF8CStr server,
	UTF8CStr path, UTF8CStr name, void* buffer, IU32 bLength, IU32* pLength);

/*!
===============================================================================
 * Change an existing node property in the DM tree.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	server		The server requesting the replace.
 *
 * \param	path		The full pathname of the node.
 *
 * \param	name		The name of the property.
 *
 * \param	value		The value string.
 *
 * \param	length		The length of the value string.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMAN_replaceProperty(OMC_TreeSessionPtr tsp, UTF8CStr server,
	UTF8CStr path, UTF8CStr name, const void* value, IU32 length);

/*!
===============================================================================
 * Get a node value from the DM tree.
 *
 * For a leaf node this is the value. For an interior node this is a list of
 * all the child node names separated by '/'.
 *
 * If the supplied buffer is too short then as much data as will fit in the
 * buffer is returned along with the error code OMC_ERR_BUFFER_OVERFLOW.
 *
 * The total length of the value data is always returned.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	server		The server requesting the get.
 *
 * \param	path		The full pathname of the node.
 *
 * \param	buffer		Where to store the value.
 *
 * \param	bLength		The length of the supplied buffer. (May be 0)
 *
 * \param	vLength		Where to store the value length.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMAN_getValue(OMC_TreeSessionPtr tsp, UTF8CStr server,
	UTF8CStr path, void* buffer, IU32 bLength, IU32* vLength);

/*!
===============================================================================
 * Change an existing leaf node value in the DM tree.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	server		The server requesting the replace.
 *
 * \param	path		The full pathname of the node.
 *
 * \param	type		A MIME type string or NULL to set no string.
 *
 * \param	format		A data format string or NULL to set no string.
 *
 * \param	data		A pointer to the value data.
 *
 * \param	length		The length of value data in bytes. (May be 0)
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMAN_replaceValue(OMC_TreeSessionPtr tsp, UTF8CStr server,
	UTF8CStr path, UTF8CStr type, UTF8CStr format, const void* data,
	IU32 length);

/*!
===============================================================================
 * Change the value in an existing leaf node value without changing the type
 * or format properties for the value.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	server		The server requesting the replace.
 *
 * \param	path		The full pathname of the node.
 *
 * \param	data		A pointer to the value data.
 *
 * \param	length		The length of value data in bytes. (May be 0)
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMAN_setValueOnly(OMC_TreeSessionPtr tsp, UTF8CStr server,
	UTF8CStr path, const void* data, IU32 length);

/*!
===============================================================================
 * Find a leaf node in the DM tree with a particular name and value.
 *
 * This function might be used to search for a server account tree given the
 * server ID.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	path		The full pathname of the node below which to start
 *						searching.
 *
 * \param	name		The name of the leaf node to search for.
 *
 * \param	data		The data expected in the leaf.
 *
 * \param	dLength		The length of the data (in bytes).
 *
 * \param	buffer		Where to store the path of the leaf node found.
 *
 * \param	bLength		The length of the supplied buffer in bytes. (May be 0)
 *
 * \param	pLength		Where to store the path length.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error,
 *			OMC_ERR_NODE_MISSING if	not found. OMC_ERR_BUFFER_OVERFLOW if the
 *			supplied buffer is not long enough.)
===============================================================================
 */
extern OMC_Yield TMAN_findValue(OMC_TreeSessionPtr tsp, UTF8CStr path,
	UTF8CStr name, const void* data, IU32 dLength, IU8* buffer, IU32 bLength,
	IU32* pLength);

/*!
===============================================================================
 * Execute a node in the DM tree.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	server		The server requesting the get.
 *
 * \param	path		The full pathname of the node.
 *
 * \param	data		The data supplied with the Exec command.
 *
 * \param	length		The length of the data.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMAN_execNode(OMC_TreeSessionPtr tsp, UTF8CStr server,
	UTF8CStr path, void* data, IU32 length);

/*!
===============================================================================
 * Set up to add a leaf node containing a large object.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	server		The server requesting the add.
 *
 * \param	path		The full pathname of the node to add.
 *
 * \param	type		A MIME type string or NULL to set no string.
 *
 * \param	format		A data format string or NULL to set no string.
 *
 * \param	size		The total length of the value in bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error and able
 *			to store the large object incrementally; OMC_ERR_EXT_NOT_PARTIAL
 *			if no error but not able to store the large object incrementally;
 *			anything else is a real error)
===============================================================================
 */
extern OMC_Yield TMAN_loAddStart(OMC_TreeSessionPtr tsp, UTF8CStr server,
	UTF8CStr path, UTF8CStr type, UTF8CStr format, IU32 size);

/*!
===============================================================================
 * Stop adding a leaf node containing a large object and delete the node.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	path		The full pathname of the node to add.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMAN_loAddAbort(OMC_TreeSessionPtr tsp, UTF8CStr path);

/*!
===============================================================================
 * Set up to replace a leaf node value with a large object.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	server		The server requesting the replace.
 *
 * \param	path		The full pathname of the node.
 *
 * \param	type		A MIME type string or NULL to set no string.
 *
 * \param	format		A data format string or NULL to set no string.
 *
 * \param	size		The total length of the value in bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error and able
 *			to store the large object incrementally; OMC_ERR_EXT_NOT_PARTIAL
 *			if no error but not able to store the large object incrementally;
 *			anything else is a real error)
===============================================================================
 */
extern OMC_Yield TMAN_loReplaceStart(OMC_TreeSessionPtr tsp, UTF8CStr server,
	UTF8CStr path, UTF8CStr type, UTF8CStr format, IU32 size);

/*!
===============================================================================
 * Stop replacing a leaf node value with a large object.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	path		The full pathname of the node.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMAN_loReplaceAbort(OMC_TreeSessionPtr tsp, UTF8CStr path);

/*!
===============================================================================
 * Store some large object data.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	path		The full pathname of the node to add.
 *
 * \param	offset		The offset in bytes of where to start storing.
 *
 * \param	data		A pointer to the value data.
 *
 * \param	length		The length of the value data in bytes.
 *
 * \param	size		The total length of the value in bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMAN_loStoreData(OMC_TreeSessionPtr tsp, UTF8CStr path,
	IU32 offset, void* data, IU32 length, IU32 size);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* OMADM */

#endif /* !_OMC_TMAN_TMAN_H_ */
