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
 *		Name:					tree_if.h
 *
 *		Project:				OMC
 *
 *		Created On:				June 2004
 *
 *		Derived From:			//depot/main/base/omc/tree/tree_indirect.h#3
 *
 *		Version:				$Id: //depot/main/base/omc/tree/tree_if.h#12 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2005
]*/

/*! \file
 *		Defines the API used to allow DM tree leaf nodes to access
 *		value data stored outside the tree.
 *
 * \brief	DM tree external data access API
 */

#ifndef _OMC_TREE_TREE_IF_H_
#define _OMC_TREE_TREE_IF_H_

#ifdef __cplusplus
extern "C" {
#endif


/*****************************************************************************
 **                                                                         **
 **                            External Data API                            **
 **                                                                         **
 *****************************************************************************/

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
typedef OMC_Yield (*OMC_TREE_ExternalReadFunc) (OMC_UserDataPtr udp,
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
typedef OMC_Yield (*OMC_TREE_ExternalWriteFunc) (OMC_UserDataPtr udp,
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
extern OMC_Error TREE_OMC_registerExternal(UTF8CStr path, void* context,
	OMC_TREE_ExternalReadFunc readFunc, OMC_TREE_ExternalWriteFunc writeFunc);



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
typedef OMC_Yield (*OMC_TREE_ExecuteFunc) (OMC_UserDataPtr udp, void* context,
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
extern OMC_Error TREE_OMC_registerExecute(UTF8CStr path,
	OMC_TREE_ExecuteFunc func, void* context);


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
typedef OMC_Error (*OMC_TREE_PersistenceReadFunc)(void *context, void* buffer,
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
typedef OMC_Error (*OMC_TREE_PersistenceWriteFunc)(void *context,
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
extern OMC_Yield OMC_TREE_storageOpenForRead(OMC_UserDataPtr udp,
			OMC_TREE_PersistenceReadFunc *pReadFunc, void **pContext);
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
extern OMC_Yield OMC_TREE_storageOpenForWrite(OMC_UserDataPtr udp,
			OMC_TREE_PersistenceWriteFunc *pWriteFunc, void **pContext);

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
extern OMC_Yield OMC_TREE_storageClose(OMC_UserDataPtr udp, void *context,
			IBOOL commit);

#endif /* !OMC_TREE_FGS */

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_TREE_TREE_IF_H_ */
