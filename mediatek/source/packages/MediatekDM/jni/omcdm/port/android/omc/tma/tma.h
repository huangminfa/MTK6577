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

#ifndef __TMA_H__
#define __TMA_H__

#ifdef OMADM

#include <omc/omc_if.h>

#ifdef __cplusplus
extern "C" {
#endif

extern const IU8 TMAN_BYPASS_ACL[];

/*!
===============================================================================
 * Initialise the tree manager. If TMA is already initialized, it will returns
 * OMC_ERR_OK.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Error TMA_init(void);

/*!
===============================================================================
 * Clean up the tree manager before exiting.
===============================================================================
 */
extern void TMA_term(void);

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
extern OMC_Error TMA_initSession(OMC_UserDataPtr udp,
								  OMC_TreeSessionPtr* pTsp);

/*!
===============================================================================
 * Free a TMAN/TREE session data pointer
 *
 * \param	tsp			The tree session data pointer.
===============================================================================
 */
extern void TMA_termSession(OMC_TreeSessionPtr tsp);

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
extern OMC_Yield TMA_addInterior(OMC_TreeSessionPtr tsp, UTF8CStr server,
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
extern OMC_Yield TMA_addLeaf(OMC_TreeSessionPtr tsp, UTF8CStr server,
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
extern OMC_Yield TMA_deleteNode(OMC_TreeSessionPtr tsp, UTF8CStr server,
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
extern OMC_Yield TMA_getProperty(OMC_TreeSessionPtr tsp, UTF8CStr server,
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
extern OMC_Yield TMA_replaceProperty(OMC_TreeSessionPtr tsp, UTF8CStr server,
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
extern OMC_Yield TMA_getValue(OMC_TreeSessionPtr tsp, UTF8CStr server,
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
extern OMC_Yield TMA_replaceValue(OMC_TreeSessionPtr tsp, UTF8CStr server,
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
extern OMC_Yield TMA_setValueOnly(OMC_TreeSessionPtr tsp, UTF8CStr server,
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
extern OMC_Yield TMA_findValue(OMC_TreeSessionPtr tsp, UTF8CStr path,
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
extern OMC_Yield TMA_execNode(OMC_TreeSessionPtr tsp, UTF8CStr server,
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
extern OMC_Yield TMA_loAddStart(OMC_TreeSessionPtr tsp, UTF8CStr server,
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
extern OMC_Yield TMA_loAddAbort(OMC_TreeSessionPtr tsp, UTF8CStr path);

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
extern OMC_Yield TMA_loReplaceStart(OMC_TreeSessionPtr tsp, UTF8CStr server,
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
extern OMC_Yield TMA_loReplaceAbort(OMC_TreeSessionPtr tsp, UTF8CStr path);

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
extern OMC_Yield TMA_loStoreData(OMC_TreeSessionPtr tsp, UTF8CStr path,
	IU32 offset, void* data, IU32 length, IU32 size);


/*!
===============================================================================
 * Read tree data from persistent storage
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
GLOBAL OMC_Yield TMA_treeRead(OMC_TreeSessionPtr tsp);


/*!
===============================================================================
 * Write any modified tree data to persistent storage
 *
 * In this implementation, we write the entire tree.
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
GLOBAL OMC_Yield TMA_treeWrite(OMC_TreeSessionPtr tsp);


/*!
===============================================================================
 * Function used by the tree to read value data from an external store.
 *
 * \param	udp			The user data pointer for the session.
 *
 * \param	context		The context pointer supplied when the external access
 *						function was registered.
 *
 * \param	offset		The byte offset from the start of the value data.
 *
 * \param	buffer		Where to store the value data.
 *
 * \param	bLength		The length of the supplied buffer in bytes. This may
 *						be 0 in which case all that will be return is the
 *						total length of the value data,
 *
 * \param	vLength		Where to store the total length of the value data in
 *						bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
typedef OMC_Yield (*TMA_Tree_ExternalReadFunc) (OMC_UserDataPtr udp,
	void *context, IU32 offset, void* buffer, IU32 bLength, IU32* vLength);


/*!
===============================================================================
 * Function used by the tree to write value data to an external store.
 *
 * Normally the value data is supplied in a single call. However, when
 * handling large objects it may be supplied in a series of calls. If this
 * partial write mechanism isn't supported then the error
 * OMC_ERR_EXT_NOT_PARTIAL should be returned.
 *
 * If the value data is not supplied in a single call then it will always be
 * supplied in a series of calls with increasing offset starting with an
 * offset of 0. If offset+bLength is equal to vLength then all the value data
 * has been supplied and this is the time to store the data if it has not been
 * stored incrementally.
 *
 * Before attempting partial writes this function is called with offset and
 * dLength set to 0 and vLength set to the maximum length of the data. This is
 * the only time a dLength of 0 will be supplied unless vLength is also 0.
 *
 * BEWARE: In certain circumstances the vLength supplied with the last chunk
 * of data may be a few bytes shorter than that supplied with the preceeding
 * chunks. This is due to the way the SyncML protocol handles Base64 encoded
 * data.
 *
 * If OMC fails part way through writing a data value using the partial write
 * mechanism then it will normally try to set the value data to zero length
 * but it is possible that the value data will just be left unfinished. If
 * this is a problem then partial writes should be rejected.
 *
 * If it is not possible to write the value for some reason (like it is in
 * use by another program) then the error OMC_ERR_EXT_NOT_ALLOWED should be
 * returned.
 *
 * \param	udp			The user data pointer for the session.
 *
 * \param	context		The context pointer supplied when the external access
 *						function was registered.
 *
 * \param	offset		The byte offset from the start of the value data.
 *
 * \param	data		A pointer to the value data.
 *
 * \param	dLength		The length of the supplied value data in bytes.
 *
 * \param	vLength		The total length of the value data.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
typedef OMC_Yield (*TMA_Tree_ExternalWriteFunc) (OMC_UserDataPtr udp,
	void *context, IU32 offset, const void* data, IU32 dLength, IU32 vLength);


/*!
===============================================================================
 * Register external access functions for a node.
 *
 * The node must exist and must be a leaf node.
 *
 * \param	path		The full pathname of the node.
 *
 * \param	context		An opaque pointer passed to the read and write
 *						functions.
 *
 * \param	readFunc	The function for reading value data. If NULL then the
 *						value is write only.
 *
 * \param	writeFunc	The function for writing value data. If NULL then the
 *						value is read only.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Error TMA_Tree_registerExternal(UTF8CStr path, void* context,
	TMA_Tree_ExternalReadFunc readFunc, TMA_Tree_ExternalWriteFunc writeFunc);


/*!
===============================================================================
 * Get the context of external node.
 *
 * The node must exist, must be a leaf node and must be external. It will free
 * the context asso
 *
 * \param	path		The full pathname of the node.
 *
 * \param	pContext	The context to return.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
GLOBAL OMC_Error TMA_Tree_getExternalContext(UTF8CStr path, void **pContext);


/*!
===============================================================================
 * Unregister external access functions for a node.
 *
 * The node must exist, must be a leaf node and must be external. It will free
 * the context asso
 *
 * \param	path		The full pathname of the node.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
GLOBAL OMC_Error TMA_Tree_unregisterExternal(UTF8CStr path);



/*****************************************************************************
 **                                                                         **
 **                              Execution API                              **
 **                                                                         **
 *****************************************************************************/

/*!
===============================================================================
 * Function used by the tree to Exec a node.
 *
 * \param	udp			The user data pointer for the session.
 *
 * \param	context		The context pointer supplied when the function was
 *						registered.
 *
 * \param	data		Pointer to the data supplied with the Exec command.
 *
 * \param	length		The length of the data.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
typedef OMC_Yield (*TMA_Tree_ExecuteFunc) (OMC_UserDataPtr udp, void* context,
	void* data, IU32 length);


/*!
===============================================================================
 * Register an execute function for a node.
 *
 * The node must exist.
 *
 * \param	path		The full pathname of the node.
 *
 * \param	func		The function to call when the node is executed.
 *
 * \param	context		An opaque pointer passed to the execute
 *						function.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Error TMA_Tree_registerExecute(UTF8CStr path,
	TMA_Tree_ExecuteFunc func, void* context);


/*****************************************************************************
 **                                                                         **
 **                             Persistence API                             **
 **                                                                         **
 *****************************************************************************/

/*
 * See omc/tree/tstore_if.h for the definition of the Fine Grained Storage
 * API which replaces the Persistence API if OMC_TREE_FGS is defined.
 */

/*
 * For the convenience of omctree the function type definitions below are
 * present even in fine grained storage builds.
 */

/*!
===============================================================================
 * Function used by the tree to read tree data from persistent storage.
 *
 * \param	context		The context pointer supplied when the function was
 *						registered.
 *
 * \param	buffer		Where to store the data read.
 *
 * \param	bLength		The length of the supplied buffer in bytes.
 *
 * \param	rLength		The length of data read. A zero length means that no
 *						more data is available to be read.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
typedef OMC_Error (*TMA_Tree_PersistenceReadFunc)(void *context, void* buffer,
	IU32 bLength, IU32* rLength);

/*!
===============================================================================
 * Function used by the tree to write tree data to persistent storage.
 *
 * \param	context		The context pointer supplied when the function was
 *						registered.
 *
 * \param	data		The data to write.
 *
 * \param	length		The length of the data to write in bytes.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
typedef OMC_Error (*TMA_Tree_PersistenceWriteFunc)(void *context,
	const void* data, IU32 length);

#ifndef OMC_TREE_FGS

/*!
===============================================================================
 * Open OMC tree in persistent storage for read access
 *
 * \param	udp				The user data pointer.
 * \param	pReadFunc		Where to store pointer to data read function
 * \param	pContext		Where to store parameter to pass to read function
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMA_Tree_storageOpenForRead(OMC_UserDataPtr udp,
			TMA_Tree_PersistenceReadFunc *pReadFunc, void **pContext);
/*!
===============================================================================
 * Open OMC tree in persistent storage for write access
 *
 * \param	udp				The user data pointer.
 * \param	pWriteFunc		Where to store pointer to data write function
 * \param	pContext		Where to store parameter to pass to write function
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMA_Tree_storageOpenForWrite(OMC_UserDataPtr udp,
			TMA_Tree_PersistenceWriteFunc *pWriteFunc, void **pContext);

/*!
===============================================================================
 * Close access to OMC tree in persistent storage
 *
 * If open for writing, any buffered data should be flushed out to storage,
 * unless the commit flag is FALSE, in which case all written data should be
 * discarded (if possible).
 *
 * \param	udp			The user data pointer.
 * \param	context		parameter set by open function
 * \param	commit		TRUE if written data should be committed. Always
 *						FALSE if storage was open for reading.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMA_Tree_storageClose(OMC_UserDataPtr udp, void *context,
			IBOOL commit);

#endif /* !OMC_TREE_FGS */


extern void TMA_lock(void);
extern void TMA_unlock(void);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* OMADM */

#endif /* !__TMA_H__ */
