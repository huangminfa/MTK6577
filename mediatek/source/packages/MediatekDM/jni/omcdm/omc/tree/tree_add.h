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
 *		Name:					tree_add.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			(Original)
 *
 *		Version:				$Id: //depot/main/base/omc/tree/tree_add.h#6 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
]*/

/*! \internal
 * \file
 *		Defines the private API used to manipulate the DM tree.
 *
 * \brief	Add and delete nodes
 */

#ifndef _OMC_TREE_TREE_ADD_H_
#define _OMC_TREE_TREE_ADD_H_

#ifdef OMADM

#include <omc/tree/tree_struct.h>
#include <omc/tree/tree_if.h>

#ifdef __cplusplus
extern "C" {
#endif


extern OMC_Error TREE_allocInterior(UTF8CStr name, UTF8CStr acl,
	UTF8CStr title, IU32 tstamp, IU16 verno, UTF8CStr type, IU32 flags,
	TREE_NodePtr parent, TREE_NodePtr* node, TREE_NodePtr* chain);

extern OMC_Error TREE_allocLeaf(UTF8CStr name, UTF8CStr acl, UTF8CStr title,
	IU32 tstamp, IU16 verno, UTF8CStr type, UTF8CStr format, void* data,
	IU32 size, IU32 flags, TREE_NodePtr parent, TREE_NodePtr* node,
	TREE_NodePtr* chain);

extern void TREE_freeNode(TREE_NodePtr node);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* OMADM */

#endif /* !_OMC_TREE_TREE_ADD_H_ */
