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

#ifndef __TMA_IF_H__
#define __TMA_IF_H__

#ifdef __cplusplus
extern "C" {
#endif


/*
 * ACL checks and the program parameter
 * ------------------------------------
 *
 * The tree session initialisation function takes a program parameter. This
 * is a string to which the string ".localhost" is appended to form a server
 * name. If a NULL string is supplied then just "localhost" will be used as
 * the server name. (ie. No leading dot.) The maximum length of the program
 * name is OMC_SERVERID_MAX_LEN-10.
 *
 * The server name is used to make ACL checks. It prevents a program
 * accessing areas of the tree that do not belong to it. Setting the ACL for
 * a node appropriately can be used to prevent others accessing areas of the
 * tree that belong to this program.
 */

/*!
===============================================================================
 * Allocate session data for running an session accessing the DM tree.
 *
 * \param	program		The program requesting the session or NULL.
 *
 * \param	udp			The user session data pointer.
 *
 * \param	pTsp		Where to store the tree session data pointer.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Error TMA_OMC_initSession(UTF8CStr program, OMC_UserDataPtr udp,
	OMC_TreeSessionPtr* pTsp);

/*!
===============================================================================
 * Allocate session data for running an session accessing the DM tree.
 *
 * \param	tsp			The tree session data pointer.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern void TMA_OMC_termSession(OMC_TreeSessionPtr tsp);

/*!
===============================================================================
 * Add an interior node to the DM tree.
 *
 * \param	tsp			The tree session data pointer.
 *
 * \param	path		The full pathname of the node to add.
 *
 * \param	type		A name representing the Device Description Framework
 *						document describing the collection of nodes rooted at
 *						this node, or NULL.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMA_OMC_addInterior(OMC_TreeSessionPtr tsp, UTF8CStr path,
	UTF8CStr type);

/*!
===============================================================================
 * Add a leaf node to the DM tree.
 *
 * \param	tsp			The tree session data pointer.
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
extern OMC_Yield TMA_OMC_addLeaf(OMC_TreeSessionPtr tsp, UTF8CStr path,
	UTF8CStr type, UTF8CStr format, void* data, IU32 length);

/*!
===============================================================================
 * Delete a node from the DM tree.
 *
 * \param	tsp			The tree session data pointer.
 *
 * \param	path		The full pathname of the node to delete.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMA_OMC_deleteNode(OMC_TreeSessionPtr tsp, UTF8CStr path);

/*!
===============================================================================
 * Get a node property from the DM tree.
 *
 * \param	tsp			The tree session data pointer.
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
extern OMC_Yield TMA_OMC_getProperty(OMC_TreeSessionPtr tsp, UTF8CStr path,
	UTF8CStr name, void* buffer, IU32 bLength, IU32* pLength);

/*!
===============================================================================
 * Change an existing node property in the DM tree.
 *
 * \param	tsp			The tree session data pointer.
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
extern OMC_Yield TMA_OMC_replaceProperty(OMC_TreeSessionPtr tsp, UTF8CStr path,
	UTF8CStr name, const void* value, IU32 length);

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
 * \param	tsp			The tree session data pointer.
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
extern OMC_Yield TMA_OMC_getValue(OMC_TreeSessionPtr tsp, UTF8CStr path,
	void* buffer, IU32 bLength, IU32* vLength);

/*!
===============================================================================
 * Change an existing leaf node value in the DM tree.
 *
 * \param	tsp			The tree session data pointer.
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
extern OMC_Yield TMA_OMC_replaceValue(OMC_TreeSessionPtr tsp, UTF8CStr path,
	UTF8CStr type, UTF8CStr format, const void* data, IU32 length);

/*!
===============================================================================
 * Execute a node in the DM tree.
 *
 * \param	tsp			The tree session data pointer.
 *
 * \param	path		The full pathname of the node.
 *
 * \param	data		The data to be supplied to the execution function.
 *
 * \param	length		The length of the data.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TMA_OMC_execNode(OMC_TreeSessionPtr tsp, UTF8CStr path,
	void* data, IU32 length);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !__TMA_IF_H__ */
