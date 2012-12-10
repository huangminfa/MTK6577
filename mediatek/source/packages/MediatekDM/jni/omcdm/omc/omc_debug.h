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
 *		Name:					omc_debug.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			//depot/main/base/ssp/debug.h#7
 *
 *		Version:				$Id: //depot/main/base/omc/omc_debug.h#4 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2003 - 2004
]*/

/*! \internal
 * \file
 *		Conditional debug tracing support for OMC.
 *
 * \brief	Debug tracing
 */

#ifndef _OMC_OMC_DEBUG_H_
#define _OMC_OMC_DEBUG_H_

#include <omc/omc_if.h>

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Debug output (all non-PROD_MIN)
 * ------------
 *
 * OMC_debug() is used for conditional tracing, via the porting layer function
 * OMC_DBG_debugPrintf(). The args parameter is the format string and arguments
 * to the printf enclosed in brackets (these are essential). For example:
 *
 *   OMC_debug(OMC_DBG_TRACE, ("Authorization failed: error=%d\n", error));
 *
 * However, the data dump function does not require any extra brackets. For
 * example:
 *
 *   OMC_debugDump(OMC_DBG_INPUT, buffer, bufLen);
 */
#ifdef PROD_MIN

#define OMC_debug(flags, args)
#define OMC_debugDump(flags, data, length)

#define OMC_debugG(flags, args)
#define OMC_debugDumpG(flags, data, length)

#else

extern void OMC_debugDumpPrint(const void* data, IU32 length);

/* Use these if no globals pointer is available */

#define OMC_debug(flags, args)												\
	do {																	\
		OMC_GlobalsPtr globals = OMC_getGlobals();							\
		OMC_debugG(flags, args);											\
	} while (0)

#define OMC_debugDump(flags, data, length)									\
	do {																	\
		OMC_GlobalsPtr globals = OMC_getGlobals();							\
		OMC_debugDumpG(flags, data, length);								\
	} while (0)

/* Use these if a globals pointer is available */

#define OMC_debugG(flags, args)												\
	do {																	\
		if ((globals->DEBUG_flags & (flags)) != 0)							\
			OMC_DBG_debugPrintf args;										\
	} while (0)

#define OMC_debugDumpG(flags, data, length)									\
	do {																	\
		if ((globals->DEBUG_flags & (flags)) != 0)							\
			OMC_debugDumpPrint(data, length);								\
	} while (0)


/*!
 * Parse a debug transport specific command line parameter.
 *
 * \param		argc	Count of command line parameters available (>0).
 * \param		argv	The vector of command line parameters.
 *
 * \return		Count of parameters used.
 * \retval			0 => Not a debug parameter.
 * \retval			<0 => Malformed parameter.
 */
extern int DBG_cmdlineParse(int argc, const char **argv);


/*!
 * Generate a usage string for the debug transport command line parameters.
 *
 * \return 		The usage string.
 */
extern const char *DBG_cmdlineUsage(void);


/*!
 * Initialise any functionality required to support OMC_DBG_debugPrintf().
 *
 * \return	 	Nothing.
 */
extern void DBG_debugInit(void);


/*!
 * Terminate any functionality used purely to support OMC_DBG_debugPrintf().
 *
 * \return		Nothing.
 */
extern void DBG_debugTerm(void);

#endif


/*
 * Diagnostic output (all non-PROD_MIN)
 * -----------------
 *
 * OMC_diag() is used for diagnostic messages, via the porting layer function
 * OMC_DBG_diagPrintf(). The args parameter is the format string and arguments
 * to the printf enclosed in brackets (these are essential). For example:
 *
 *   OMC_diag(("Authorization failed: error=%d\n", error));
 */
#ifdef PROD_MIN

#define OMC_diag(args)

#else

#define OMC_diag(args)														\
	do {																	\
		OMC_DBG_diagPrintf args;											\
	} while (0)


/*!
 * Initialise any functionality required to support	OMC_DBG_diagPrintf().
 *
 * \return	 	Nothing.
 */
extern void DBG_diagInit(void);


/*!
 * Terminate any functionality used purely to support
 * OMC_DBG_diagPrintf().
 *
 * \return	 	Nothing.
 */
extern void DBG_diagTerm(void);

#endif


/*
 * Assertions (non-PROD only)
 * ----------
 */
#ifdef PROD

#define OMC_assert(cond)

#else

#define OMC_assert(cond)													\
	do {																	\
		if (!(cond)) OMC_DBG_assertFail(__FILE__, __LINE__);				\
	} while (0)

#endif

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_OMC_DEBUG_H_ */
