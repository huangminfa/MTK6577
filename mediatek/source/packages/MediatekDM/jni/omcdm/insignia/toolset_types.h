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
 *		Name:				toolset_types.h
 *
 *		Created On:			March 2003
 *
 *		Derived From:		Original
 *
 *		Version:			$Id: //depot/main/base/insignia/toolset_types.h#7 $
 *
 *		Coding Standards:	3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2003 - 2004
]*/

/*! \file
 *		This file contains the default toolset-dependent definitions of the
 *		Insignia standard types and constants.
 *
 * \brief Defines the Insignia types
 */

#ifndef _INSIGNIA_TOOLSET_TYPES_H_
#define _INSIGNIA_TOOLSET_TYPES_H_

/* The following definitions should work on most modern 32bit compilers. */

typedef int				IS32;		/*!< 32bit integer */
typedef unsigned int	IU32;		/*!< 32bit integer (unsigned) */

typedef short			IS16;		/*!< 16bit integer */
typedef unsigned short	IU16;		/*!< 16bit integer (unsigned) */

typedef signed char		IS8;		/*!< 8bit integer */
typedef unsigned char	IU8;		/*!< 8bit integer (unsigned) */

/*! Integer type of equivalent size to a native pointer. */

typedef	IU32			IHPE;

/* Boolean definitions. Note that IBOOL must be at least as wide as the larger
 * of 'int' (the compiler's natural implicit boolean type), Ix32, and IHPE, to
 * allow bit-wise operations on any Insignia type to be stored in an IBOOL
 * without loss of information. */

typedef IS32			IBOOL;		/*!< Boolean type */

#endif /* !_INSIGNIA_TOOLSET_TYPES_H_ */
