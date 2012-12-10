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
 *		Name:					tree_common.h
 *
 *		Project:				OMC
 *
 *		Created On:				August 2005
 *
 *		Version:				$Id: //depot/main/base/omc/tree/tree_common.h#3 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2005 - 2006
]*/


/*! \file
 *		This file contains definitions and structures that are part of the
 *		internal tree but that need to be exposed to the outside world
 *		via the tree storage API.
 *
 * \brief	Common internal and external tree definitions
 */

#ifndef _OMC_TREE_TREE_COMMON_H_
#define _OMC_TREE_TREE_COMMON_H_

#ifdef OMADM

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Flag bits
 * ---------
 */
#define TREE_ACCESSTYPE_ADD			 0x01
#define TREE_ACCESSTYPE_COPY		 0x02
#define TREE_ACCESSTYPE_DELETE		 0x04
#define TREE_ACCESSTYPE_EXEC		 0x08
#define TREE_ACCESSTYPE_GET			 0x10
#define TREE_ACCESSTYPE_REPLACE		 0x20

#define TREE_SCOPE_PERMANENT		 0x40

/*      Spare						 0x80 */

#define TREE_FLAG_LEAF				0x100
#define TREE_FLAG_EXTERNAL			0x200

/*
 * Testing flag bits
 * -----------------
 */
#define TREE_FLAG_ADD_ALLOWED(f)		(f & TREE_ACCESSTYPE_ADD)
#define TREE_FLAG_COPY_ALLOWED(f)		(f & TREE_ACCESSTYPE_COPY)
#define TREE_FLAG_DELETE_ALLOWED(f)		(f & TREE_ACCESSTYPE_DELETE)
#define TREE_FLAG_EXEC_ALLOWED(f)		(f & TREE_ACCESSTYPE_EXEC)
#define TREE_FLAG_GET_ALLOWED(f)		(f & TREE_ACCESSTYPE_GET)
#define TREE_FLAG_REPLACE_ALLOWED(f)	(f & TREE_ACCESSTYPE_REPLACE)

#define TREE_FLAG_IS_PERMANENT(f)		(f & TREE_SCOPE_PERMANENT)

#define TREE_FLAG_IS_LEAF(f)			(f & TREE_FLAG_LEAF)
#define TREE_FLAG_IS_EXTERNAL(f)		(f & TREE_FLAG_EXTERNAL)

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* OMADM */

#endif /* !_OMC_TREE_TREE_COMMON_H_ */
