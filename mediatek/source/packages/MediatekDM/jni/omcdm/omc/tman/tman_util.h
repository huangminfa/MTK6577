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
 *		Name:					tman_util.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			Original
 *
 *		Version:				$Id: //depot/main/base/omc/tman/tman_util.h#11 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
]*/

/*! \internal
 * \file
 *		Defines utility functions.
 *
 * \brief	Utilities
 */

#ifndef _OMC_TMAN_TMAN_UTIL_H_
#define _OMC_TMAN_TMAN_UTIL_H_

#ifdef OMADM

#include <omc/tree/tree.h>	/* For TREE_NodeRef and TREE_Flags */

#ifdef __cplusplus
extern "C" {
#endif


/*!
 * ACL check operations.
 */
typedef enum
{
	TMAN_ACLACTION_NORMAL,		/* Action and ACL */
	TMAN_ACLACTION_REPLACEACL,	/* Action of this and ACL of this or parent */
	TMAN_ACLACTION_ACL_ONLY		/* No action but only ACL */
} TMAN_AclAction;


extern IBOOL TMAN_checkPrefix(UTF8CStr path, UTF8CStr prefix,
	UTF8CStr* pSuffix);

extern OMC_Error TMAN_checkAcl(TREE_NodeRef node, TREE_Flags operation,
	TMAN_AclAction action, UTF8CStr server);

extern UTF8CStr TMAN_nodeName(UTF8CStr path);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* OMADM */

#endif /* !_OMC_TMAN_TMAN_UTIL_H_ */
