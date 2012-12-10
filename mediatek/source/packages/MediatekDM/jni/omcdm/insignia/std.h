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
 *		Name:				std.h
 *
 *		Project:			SSP Client
 *
 *		Created On:			March 2003
 *
 *		Derived From:		Original
 *
 *		Version:			$Id: //depot/main/base/insignia/std.h#11 $
 *
 *		Coding Standards:	3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2003 - 2004
]*/

/*! \file
 *		This file contains the definition of the Insignia standard types
 *		and	constants.
 *
 * \brief Defines Insignia standard types, macros, and constants
 */

#ifndef _INSIGNIA_STD_H_
#define _INSIGNIA_STD_H_

/* Setup toolset dependent environment. */

#include <insignia/toolset.h>

/* Define standard Insignia types potentially requiring definitions dependent
 * on the toolset being used to compile the source. */

#include <insignia/toolset_types.h>

/* Setup OS dependent environment. */

#include <insignia/os.h>

/* Data types derived from standard Insignia types. */

/*! UTF8 encoded Unicode string, NUL terminated. */
typedef IU8 *UTF8Str;

/*! UTF8 encoded Unicode string constant, NUL terminated. */
typedef const IU8 *UTF8CStr;

#ifndef EXCLUDE_FROM_DOCS
/*!
 * Structure for handling the concept of a buffer, that is, a length delimited
 * pointer.
 */
typedef struct IBuffer_tag {
	IU8			*ptr;
	IU32		len;
} IBuffer;
#endif /* EXCLUDE_FROM_DOCS */

/* Storage types. */

#define GLOBAL
#define LOCAL	static
#define SAVED	static

/* TRUE/FALSE may sometimes be defined already, so only add ours if not. */

#ifndef TRUE
#define TRUE	1
#endif

#ifndef FALSE
#define FALSE	0
#endif

/*! Replacement for NULL when <stdio.h> not wanted. */

#ifndef NULL
#define	NULL	((void *)0)
#endif

/*! Macro to remove warnings relating to unused function arguments. */

#ifndef UNUSED
#define UNUSED(x)	{ (void)x; }
#endif

/* Macros for max/min - beware of the standard problem, namely that exactly one
 * of the arguments is evaluated twice. */

/*! Macros for min */
#ifndef min
#define min(a,b)	(((a)>(b))? (b):(a))
#endif

/*! Macros for max */
#ifndef max
#define max(a,b)	(((a)<(b))? (b):(a))
#endif

/* Other useful macros */

#define TABLE_SIZE(table)		(sizeof(table)/sizeof(table[0]))
#define EQUAL_BOOLEANS(b1, b2)	((!(b1)) == (!(b2)))

#endif /* !_INSIGNIA_STD_H_ */
