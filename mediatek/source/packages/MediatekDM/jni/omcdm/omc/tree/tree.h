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
 *		Name:					tree.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			(Original)
 *
 *		Version:				$Id: //depot/main/base/omc/tree/tree.h#24 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
]*/

/*! \internal
 * \file
 *		Defines the API used to manipulate the DM tree.
 *
 * \brief	DM tree implementation API
 */

#ifndef _OMC_TREE_TREE_H_
#define _OMC_TREE_TREE_H_

#ifdef OMADM

#include <omc/tree/tree_common.h>

#ifdef __cplusplus
extern "C" {
#endif


/*!
 * References to nodes are passed back and forth using the following
 * opaque type.
 */
typedef struct TREE_NodeStruct *TREE_NodeRef;

/*!
 * AccessType and Scope flags are passed to node creation and action
 * permission requests using the TREE_ACCESSTYPE_* and TREE_SCOPE_*
 * values defined in tree_common.h.
 */
typedef IU8 TREE_Flags;

/*!
 * Values passed to the TREE_WalkTree callback function to distinguish
 * the reasons for calling it.
 */
typedef enum
{
	TREE_WALK_LEAF,
	TREE_WALK_BEFORE,
	TREE_WALK_AFTER
} TREE_WalkReason;


/*!
===============================================================================
 * Initialise the tree.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Error TREE_init(void);


/*!
===============================================================================
 * Clean up the tree before exiting.
===============================================================================
 */
extern void TREE_term(void);


/*!
===============================================================================
 * Find a node in the DM tree.
 *
 * \param	path		The full pathname of the node.
 *
 * \return	The node reference or NULL if it is not found.
===============================================================================
 */
extern TREE_NodeRef TREE_findNode(UTF8CStr path);


/*!
===============================================================================
 * Find the parent of a node in the DM tree.
 *
 * \param	path		The full pathname of the node whose parent is wanted.
 *
 * \return	The parent node reference or NULL if it is not found.
===============================================================================
 */
extern TREE_NodeRef TREE_findParentNode(UTF8CStr path);


/*!
===============================================================================
 * Release any resources held open for this node reference. Once released the
 * reference cannot be used any more.
 *
 * \param	nodeRef		The node reference to release.
===============================================================================
 */
extern void TREE_releaseNodeRef(TREE_NodeRef nodeRef);


/*!
===============================================================================
 * Find a leaf node with a particular name and value.
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
 * \param	pNode		Where to store the reference to the leaf node.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error,
 *			OMC_ERR_NODE_MISSING if	not found)
===============================================================================
 */
extern OMC_Yield TREE_findLeafByValue(OMC_TreeSessionPtr tsp, UTF8CStr path,
	UTF8CStr name, const void* data, IU32 dLength, TREE_NodeRef* pNode);

/*!
===============================================================================
 * Store the path for a node in a buffer.
 *
 * \param	node		The node reference for the node.
 *
 * \param	buffer		Where to store the path.
 *
 * \param	bLength		The length of the supplied buffer. (May be 0)
 *
 * \param	pLength		Where to store the path length.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error,
 *			OMC_ERR_BUFFER_OVERFLOW if the supplied buffer is not long enough)
===============================================================================
 */
extern OMC_Error TREE_getPath(TREE_NodeRef node, IU8* buffer, IU32 bLength,
	IU32* pLength);

/*!
===============================================================================
 * Add a node to the DM tree.
 *
 * \param	parent		The node reference for the parent node.
 *
 * \param	name		The name of the node.
 *
 * \param	leaf		Whether the node is to be a leaf.
 *
 * \param	flags		AccessType and Scope flags.
 *
 * \param	pNode		Where to store the node reference for the new node.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Error TREE_addNode(TREE_NodeRef parent, UTF8CStr name, IBOOL leaf,
	TREE_Flags flags, TREE_NodeRef* pNode);


/*!
===============================================================================
 * Delete a node from the DM tree.
 *
 * \param	node		The node reference for the node to delete.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Error TREE_deleteNode(TREE_NodeRef node);


/*!
===============================================================================
 * Get a node property from the DM tree.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	node		The node reference for the node.
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
extern OMC_Yield TREE_getProperty(OMC_TreeSessionPtr tsp, TREE_NodeRef node,
	UTF8CStr name, void* buffer, IU32 bLength, IU32* pLength);


/*!
===============================================================================
 * Change an existing node property in the DM tree.
 *
 * \param	node		The node reference for the node.
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
extern OMC_Error TREE_setProperty(TREE_NodeRef node, UTF8CStr name,
	const void* value, IU32 length);


/*!
===============================================================================
 * Get (part of) a leaf node value from the DM tree.
 *
 * For a leaf node this is the value. For an interior node this is a list of
 * all the child node names separated by '/'.
 *
 * If the supplied buffer is too short then as much data as will fit in the
 * buffer is returned along with the error code \ref OMC_ERR_BUFFER_OVERFLOW.
 *
 * The total length of the value data is always returned.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	node		The node reference for the node.
 *
 * \param	offset		Byte offset in the value from which to start storing
 *						in the buffer.
 *
 * \param	buffer		Where to store the value.
 *
 * \param	bLength		The length of the supplied buffer in bytes. (May be 0)
 *
 * \param	vLength		Where to store the total value length in bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TREE_getValue(OMC_TreeSessionPtr tsp, TREE_NodeRef node,
	IU32 offset, void* buffer, IU32 bLength, IU32* vLength);


/*!
===============================================================================
 * Set (part of) a leaf node value in the DM tree.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	node		The node reference for the node.
 *
 * \param	offset		Byte offset in the value at which to start storing
 *						the supplied data.
 *
 * \param	data		A pointer to some value data.
 *
 * \param	dLength		The length of the supplied value data in bytes.
 *
 * \param	vLength		The total length of the value data in bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TREE_setValue(OMC_TreeSessionPtr tsp, TREE_NodeRef node,
	IU32 offset, const void* data, IU32 dLength, IU32 vLength);


/*!
===============================================================================
 * Fetch the access control list applying to a node. This is stored in the
 * node or one of its parents.
 *
 * \param	node		The node reference for the node.
 *
 * \param	parent		Whether to start search from the parent of the supplied
 *						node.
 *
 * \param	buffer		Where to store the ACL.
 *
 * \param	bLength		The length of the supplied buffer. (May be 0)
 *
 * \param	aLength		Where to store the ACL length.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error,
 *			OMC_ERR_BUFFER_OVERFLOW if the supplied buffer is not long enough)
===============================================================================
 */
extern OMC_Error TREE_getAcl(TREE_NodeRef node, IBOOL parent, IU8* buffer,
	IU32 bLength, IU32* aLength);


/*!
===============================================================================
 * Exec a node in the DM tree.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	node		The node reference for the node.
 *
 * \param	data		Pointer to the data supplied with the Exec command.
 *
 * \param	length		The length of the data.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TREE_execNode(OMC_TreeSessionPtr tsp, TREE_NodeRef node,
	void* data, IU32 length);


/*!
===============================================================================
 * Prepare to store a large object value in a node. Check that this will not
 * corrupt the node as viewed by external entities.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	node		The node reference for the node.
 *
 * \param	type		A MIME type string (or NULL).
 *
 * \param	format		A data format string (or NULL).
 *
 * \param	size		The total length of the value in bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error and able
 *			to store the large object incrementally; OMC_ERR_EXT_NOT_PARTIAL
 *			if no error but not able to store the large object incrementally;
 *			anything else is a real error)
===============================================================================
 */
extern OMC_Yield TREE_loPrepare(OMC_TreeSessionPtr tsp, TREE_NodeRef node,
	UTF8CStr type, UTF8CStr format, IU32 size);


/*!
===============================================================================
 * Stop storing a large object value in a node.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	node		The node reference for the node.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TREE_loAbort(OMC_TreeSessionPtr tsp, TREE_NodeRef node);


/*!
===============================================================================
 * Set part of a large object node value in the DM tree.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	node		The node reference for the node.
 *
 * \param	offset		Byte offset in the value at which to start storing
 *						the supplied data.
 *
 * \param	data		A pointer to some value data.
 *
 * \param	dLength		The length of the supplied value data in bytes.
 *
 * \param	vLength		The total length of the value data in bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TREE_loStore(OMC_TreeSessionPtr tsp, TREE_NodeRef node,
	IU32 offset, const void* data, IU32 dLength, IU32 vLength);


/*!
===============================================================================
 * Test whether a node is a leaf node.
 *
 * \param	node		The node reference for the node.
 *
 * \return	TRUE if the node is a leaf node; FALSE otherwise.
===============================================================================
 */
extern IBOOL TREE_isLeaf(TREE_NodeRef node);


/*!
===============================================================================
 * Test whether a node is a permanent node.
 *
 * \param	node		The node reference for the node.
 *
 * \return	TRUE if the node is permanent; FALSE otherwise.
===============================================================================
 */
extern IBOOL TREE_isPermanent(TREE_NodeRef node);


/*!
===============================================================================
 * Test whether an action is allowed on a node.
 *
 * \param	node		The node reference for the node.
 *
 * \param	action		The action to check.
 *
 * \return	TRUE if the action is allowed; FALSE otherwise.
===============================================================================
 */
extern IBOOL TREE_isAllowed(TREE_NodeRef node, TREE_Flags action);


/*!
===============================================================================
 * Callback function passed to TREE_WalkTree.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	node		The node reference for a node.
 *
 * \param	reason		Why this node is being processed.
 *
 * \param	context		A parameter to be passed to the callback function.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error and
 *			iteration is to continue. Any non-slicing error code will cause
 *			iteration to cease.)
===============================================================================
 */
typedef OMC_Yield (TREE_WalkFunc) (OMC_TreeSessionPtr tsp, TREE_NodeRef node,
	TREE_WalkReason reason, void* context);


/*!
===============================================================================
 * Iterate all nodes in a sub-tree.
 *
 * The callback function is called twice for each interior node. It is called
 * once before the child nodes are iterated and once after the child nodes are
 * iterated. The function is called once for each leaf node. A parameter is
 * passed to distinguish between these three cases.
 *
 * For the tree below the nodes will be called in the following order:
 *
 *           A
 *           |
 *      +----+----+          A B1 C1 C2 B1 B2 B2 B3 A
 *      |    |    |
 *      B1   B2   B3
 *      |    |
 *    +-+-+  +
 *    |   |
 *    C1  C2
 *
 * The callback function is allowed to return OMC_ERR_YIELD or OMC_ERR_WAIT
 * and this will be returned by this function and when this function is
 * called again it will call the calback function again for the same node.
 * If the callback function returns anything else other than OMC_ERR_OK then
 * iteration will cease immediately and this error will be returned. Any
 * operation may be carried out on the tree in the callback function but if
 * the current node (or any of its parent nodes) is deleted then the callback
 * must return an error. (OMC_ERR_BREAK is a good choice.)
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \param	path		The full pathname of the node at which to start
 *						iterating.
 *
 * \param	callback	The callback function.
 *
 * \param	context		A parameter to be passed to the callback function.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error,)
===============================================================================
 */
extern OMC_Yield TREE_walkTree(OMC_TreeSessionPtr tsp, UTF8CStr path,
	TREE_WalkFunc callback, void* context);


#ifndef OMC_TREE_FGS

/*!
===============================================================================
 * Read tree data from persistent storage
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TREE_read(OMC_TreeSessionPtr tsp);


/*!
===============================================================================
 * Write any modified tree data to persistent storage
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TREE_write(OMC_TreeSessionPtr tsp);

#endif /* !OMC_TREE_FGS */

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* OMADM */

#endif /* !_OMC_TREE_TREE_H_ */
