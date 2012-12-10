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
 *		Name:					tstore.h
 *
 *		Project:				OMC
 *
 *		Created On:				August 2005
 *
 *		Derived From:			(Original)
 *
 *		Version:				$Id: //depot/main/base/omc/tree/tstore.h#5 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2005 - 2006
]*/

/*! \file
 *		Internal API for reading and writing the tree when using the
 *		fine-grained tree storage mechanism.
 *
 *
 * \brief	DM fine grained node storage
 */

#ifndef _OMC_TREE_TSTORE_H_
#define _OMC_TREE_TSTORE_H_

#if defined(OMADM) && defined(OMC_TREE_FGS)

#ifdef __cplusplus
extern "C" {
#endif


/*!
===============================================================================
 * Read tree from persistent storage after an error
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern SLICE TSTORE_resyncTree(OMC_TreeSessionPtr tsp);

/*!
===============================================================================
 * Read the entire tree from persistent storage using the fine-grained API
 *
 * \param	tsp			The tree session data pointer for the session.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield TSTORE_readTree(OMC_TreeSessionPtr tsp);

/*!
===============================================================================
 * Write a node of the tree to persistent storage using the fine-grained API
 * and reload the tree if this fails.
 *
 * NB. Releases the node reference passed to it.
 *
 * \param	tsp		The tree session data pointer for the session.
 *
 * \param	path	The path of the node to write.
 *
 * \param	node	The node to write (and the reference to free)
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if ok,
 *			OMC_ERR_TREE_WRITE if write failed, OMC_ERR_TREE_SYNC if resync
 *			failed after write failure.)
===============================================================================
 */
extern SLICE TSTORE_writeNodeOrResync(OMC_TreeSessionPtr tsp, UTF8CStr path,
	TREE_NodeRef node);

/*!
===============================================================================
 * Write a node of the tree to persistent storage using the fine-grained API
 *
 * NB. Releases the node reference passed to it.
 *
 * \param	tsp		The tree session data pointer for the session.
 *
 * \param	path	The path of the node to write.
 *
 * \param	node	The node to write (and the reference to free)
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern SLICE TSTORE_writeNode(OMC_TreeSessionPtr tsp, UTF8CStr path,
	TREE_NodeRef node);

/*!
===============================================================================
 * Delete a node of the tree from persistent storage using the fine-grained API
 *
 * \param	tsp		The tree session data pointer for the session.
 *
 * \param	path	The path of the node to delete.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern SLICE TSTORE_deleteNode(OMC_TreeSessionPtr tsp, UTF8CStr path);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* OMADM && OMC_TREE_FGS */

#endif /* _OMC_TREE_TSTORE_H_ */
