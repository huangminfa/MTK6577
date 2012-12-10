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
 *		Name:					tstore_if.h
 *
 *		Project:				OMC
 *
 *		Created On:				August 2005
 *
 *		Derived From:			(Original)
 *
 *		Version:				$Id: //depot/main/base/omc/tree/tstore_if.h#6 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2005
]*/

/*! \file
 *		Implements the API used to allow OMC to read and write from storage, as
 *		written using the fine-grained storage API.
 *
 * \brief	DM fine grained node storage OS functions
 */

#ifndef _OMC_TSTORE_IF_H_
#define _OMC_TSTORE_IF_H_

#ifdef OMC_TREE_FGS

#include <omc/tree/tree_common.h>	/* Flag bit definitions */

#ifdef __cplusplus
extern "C" {
#endif

/*!
===============================================================================
 * Node representation used in fine-grained tree storage API
===============================================================================
 */
typedef struct {

	UTF8Str			name;	/*!< Name of this node */
	UTF8Str			acl;	/*!< Access control list */
#ifdef SUPPORT_OPTIONAL_PROPS
	UTF8Str			title;	/*!< Description */
	IU32			tstamp;	/*!< Time stamp */
	IU32			verno;	/*!< Version */
#endif
	IU32			flags;	/*!< Bit significant flags */
	UTF8Str			type;	/*!< Collection description */
							/*!< string or MIME type string */
	UTF8Str			format;	/*!< Value format */
	IU32			size;	/*!< Value length in bytes */
	IU8 *			value;	/*!< Value data */

} OMC_TSTORE_Node;


/*!
===============================================================================
 * Callback function typedef for OMC_TSTORE_readTree()
===============================================================================
 */
typedef OMC_Error (*OMC_TSTORE_readCallbackFunc)(UTF8CStr path,
												 OMC_TSTORE_Node *node);


/*!
===============================================================================
 * Read the entire DM tree from persistent storage.  Call function "addNode"
 * for each node (root node downwards).
 *
 * \param	udp       The user data pointer.
 *
 * \param   addNode   A callback function, called to add the read node to the
 *                    DM tree.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_TSTORE_readTree(OMC_UserDataPtr udp,
	OMC_TSTORE_readCallbackFunc addNode);


/*!
===============================================================================
 * Write a node.
 *
 * \param	udp       The user data pointer for the session.
 *
 * \param	path      The path of the node to write.
 *
 * \param   node      A pointer to the node data.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_TSTORE_writeNode(OMC_UserDataPtr udp, UTF8CStr path,
	OMC_TSTORE_Node *node);

/*!
===============================================================================
 * Delete a node.
 *
 * \param	udp       The user data pointer for the session.
 *
 * \param	path      The path of the node to delete.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_TSTORE_deleteNode(OMC_UserDataPtr udp, UTF8CStr path);


/*!
===============================================================================
 * Start an atomic operation.  All OMC_TSTORE calls between this call and
 * the next one to OMC_TSTORE_endAtomic() must either succeed or be rolled
 * back.
 *
 * \param	udp       The user data pointer for the session.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_TSTORE_startAtomic(OMC_UserDataPtr udp);

/*!
===============================================================================
 * End an atomic operation.
 *
 * \param	udp         The user data pointer for the session.
 *
 * \param   success     TRUE if the atomic operation was successful,
 *                      FALSE if it failed and should be rolled back.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
 *			This function should never return an error code unless catastrophic
 *			and unrecoverable failure has ocurred.
===============================================================================
 */

extern OMC_Yield OMC_TSTORE_endAtomic(OMC_UserDataPtr udp, IBOOL success);

/*!
===============================================================================
 * Initialize the TSTORE interface.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Error OMC_TSTORE_init(void);

/*!
===============================================================================
 * Terminate the TSTORE interface.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Error OMC_TSTORE_term(void);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* OMC_TREE_FGS */

#endif /* !_OMC_TSTORE_IF_H_ */
