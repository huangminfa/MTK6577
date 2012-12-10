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
 *		Name:					debug_if.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			//depot/main/base/ssp/debug_if.h#15
 *
 *		Version:				$Id: //depot/main/base/omc/debug_if.h#21 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2005
]*/

/*! \file
 *  \brief
 *		Debug support for the OMC client.
 */

#ifndef _OMC_DEBUG_IF_H_
#define _OMC_DEBUG_IF_H_

#ifdef __cplusplus
extern "C" {
#endif

/******************************************************************************
 *****                           Debug messages                           *****
 ******************************************************************************/

/*
 * Debug messages are common in the agent code and are intended to aid
 * debugging the code. Before generating the message a flag bit is tested
 * and if the flag is not set the message is suppressed. This allows many
 * messages to be added to the code while limiting the ones seen by the
 * programmer to those that are of interest.
 *
 * It is expected that debug message are too long and frequent to be
 * displayed on the device itself. They would normally be expected to be
 * delivered to a debugging host in some way for display.
 *
 * Only debug builds of the OMC code will ever attempt to generate debug
 * messages.
 */

/*
================================================================================
 * The flag bits used to enable debug messages.
================================================================================
 */

/*! \defgroup OMC_DBG_defs OMC_DBG_
 * The flag bits used to enable debug messages.
 * \note
 * 		The OMC code tests the flag bits stored in the global variable
 *		DEBUG_flags. A pointer to the global variables structure can be
 *		fetched by calling \ref OMC_getGlobals().
 * @{
 */
#define OMC_DBG_TRACE		0x00000001		/*!< General */
#define OMC_DBG_SESS		0x00000002		/*!< Session Manager */
#define OMC_DBG_TRG			0x00000004		/*!< Trigger */
#define OMC_DBG_FUMO		0x00000008		/*!< General FUMO */
#define OMC_DBG_IO			0x00000010		/*!< Data read and written */
#define OMC_DBG_RTK			0x00000020		/*!< SyncML Toolkit */
#define OMC_DBG_XPT			0x00000040		/*!< SyncML Toolkit XPT package */
#define OMC_DBG_TREE		0x00000080		/*!< Property tree */
#define OMC_DBG_AUTH		0x00000100		/*!< Authentication */
#define OMC_DBG_QUEUE		0x00000200		/*!< Session manager queues */
#define OMC_DBG_WBXML		0x00000400		/*!< WBXML parser */
#define OMC_DBG_OMADL		0x00000800		/*!< OMA download */
#define OMC_DBG_PLUGIN		0x00001000		/*!< Plugin support */
#define OMC_DBG_SLICE		0x00002000		/*!< Time slicing support */
#define OMC_DBG_TMAN 		0x00004000		/*!< Tree manager */
#define OMC_DBG_TSTORE		0x00008000		/*!< Tree storage API */


/*
 * Flash driver specific flags.
 */
#define OMC_DBG_FD			0x00010000		/*!< FD entry calls (exc. read) */
#define OMC_DBG_FDREAD		0x00020000		/*!< FD read calls */
#define OMC_DBG_CFI			0x00040000		/*!< CFI operations */
#define OMC_DBG_QUERY		0x00080000		/*!< CFI query information */

/*
 * Flags for FOTO and workspace ops.
 */
#define OMC_DBG_FOTO		0x00100000		/*!< FOTO access */
#define OMC_DBG_WS			0x00200000		/*!< Workspace access */

/*
 * Flags for datasync.
 */
#define OMC_DBG_DSACC		0x00400000		/*!< DS Account handling */
#define OMC_DBG_DSTORE		0x00800000		/*!< Datastore handling */

/*
 * Flags for file access API ops.
 */
#define OMC_DBG_FILEAPI		0x01000000	/*!< show file access API operations */

/*
 * Mediatek extended flags.
 */
#define OMC_DBG_TMA         0x04000000      /*!< Tree manager agent */
#define OMC_DBG_MMI         0x08000000      /*!< Tree manager agent */

/*							0x10000000 */
/*							0x20000000 */
/*							0x40000000 */
/*							0x80000000 */

/* @} */


/*!
================================================================================
 * Write a debug message.
 *
 * This function need only be implemented for non-release builds.
 *
 * \param	format		The "printf" style message format string.
 * \param	...			The values to display as required by the format string.
================================================================================
 */
extern void OMC_DBG_debugPrintf(const char *format, ...);


/******************************************************************************
 *****                        Diagnostic messages                         *****
 ******************************************************************************/

/*
 * Diagnostic messages are short debug messages intended to report significant
 * events. They are not just generated in debug builds and are not protected
 * by any test of flag bits.
 *
 * It is intended that diagnostic messages are short enough and infrequent
 * enough to be displayed on the device itself. This does not preclude them
 * being trasnferred to a debug host for display though.
 *
 * Only non-release builds of the OMC code will ever attempt to generate
 * diagnostic messages.
 */

/*!
================================================================================
 * Write a diagnostic message.
 *
 * This function need only be implemented for non-release builds.
 *
 * \param	format		The "printf" style message format string.
 * \param	...			The values to display as required by the format string.
================================================================================
 */
extern void OMC_DBG_diagPrintf(const char *format, ...);


/******************************************************************************
 *****                              Asserts                               *****
 ******************************************************************************/

/*
 * The OMC code contains a number of validity checks called 'asserts'. These
 * are used to test for and trap supposedly impossible events.
 *
 * Only debug builds of the OMC code will ever make assert tests.
 */

/*!
================================================================================
 * Report an assertion failure.
 *
 * This function may return in which case the calling code will behave as it
 * would in a non-debug build. However, the recommended action is to report
 * the error to the user and never to return.
 *
 * This function need only be implemented for debug builds.
 *
 * \param	file	The name of the source file containing the failed assert.
 * \param	line	The line number in the source file of the failed assert.
================================================================================
 */
extern void OMC_DBG_assertFail(const char *file, int line);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_DEBUG_IF_H_ */
