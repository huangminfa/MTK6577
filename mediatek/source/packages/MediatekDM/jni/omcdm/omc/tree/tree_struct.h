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
 *		Name:					tree_struct.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			(Original)
 *
 *		Version:				$Id: //depot/main/base/omc/tree/tree_struct.h#12 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
]*/

/*! \internal
 * \file
 *		Defines the private structures used to represent the DM tree.
 *
 * \brief	Tree structure
 */

#ifndef _OMC_TREE_TREE_STRUCT_H_
#define _OMC_TREE_TREE_STRUCT_H_

#ifdef OMADM

#include <omc/tree/tree.h>	/* AccessType and Scope flags */
#include <omc/tree/tree_common.h>
#include <omc/tree/tree_if.h>

#ifdef __cplusplus
extern "C" {
#endif


/*! \internal
 * \struct TREE_NodeStruct
 *	Used to hold the OMC Tree information
 *
 *  Any changes to name, acl, title, tstamp, verno, flags, type, format, size
 *  and value must be replicated in tstore_if.h.
 *
 */
typedef struct TREE_NodeStruct
{
	struct TREE_NodeStruct *		next;	/*!< Pointer to next sibling node */
	struct TREE_NodeStruct *		parent;	/*!< Pointer to parent node */

	UTF8Str							name;	/*!< Name of this node */

	UTF8Str							acl;	/*!< Access control list */
#ifdef SUPPORT_OPTIONAL_PROPS
	UTF8Str							title;	/*!< Description */
	IU32							tstamp;	/*!< Time stamp */
	IU32							verno;	/*!< Version */
#endif
	IU32							flags;	/*!< Bit significant flags */

	OMC_TREE_ExecuteFunc			execFunc;	/*!< Execute function */
	void*							execCont;	/*!< Execute context */

	UTF8Str							type;	/*!< Collection description */
											/*!< string or MIME type string */

	/*! Type specific storage */
	union
	{
		/*! Interior node */
		struct
		{
			/*! tree data */
			struct TREE_NodeStruct *child;	/*!< Pointer to first child node */
		} interior;

		/*! Leaf node */
		struct
		{
			UTF8Str					format;	/*!< Value format */

			/*! Leaf data */
			union
			{
				/*! Locally stored value */
				struct
				{
					IU32			size;	/*!< Value length in bytes */
					IU8 *			value;	/*!< Value data */
				} normal;

				/*! Externally stored value */
				struct
				{
					void*						context;	/*!< Func context */
					OMC_TREE_ExternalReadFunc	read;		/*!< Value read */
					OMC_TREE_ExternalWriteFunc	write;		/*!< Value write */
				} external;
			} u;
		} leaf;
	} u;
} TREE_Node, *TREE_NodePtr;


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* OMADM */

#endif /* !_OMC_TREE_TREE_STRUCT_H_ */
