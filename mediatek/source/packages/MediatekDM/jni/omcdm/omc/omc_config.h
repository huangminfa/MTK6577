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
 *		Name:					omc_config.h
 *
 *		Project:				OMC
 *
 *		Created On:				June 2004
 *
 *		Derived From:			(Original)
 *
 *		Version:				$Id: //depot/main/base/omc/omc_config.h#35 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
]*/

/*! \file
 *		Configuration for OMC.
 *
 * \brief	Configuration
 */

#ifndef _OMC_OMC_CONFIG_H_
#define _OMC_OMC_CONFIG_H_

/*****************************************************************************
 *                                                                           *
 *                         FULL SOURCE CONFIGURATION                         *
 *                                                                           *
 * The following configuration items are for full source builds - do not     *
 * change these values unless you can rebuild the entire OMC source          *
 * otherwise you may introduce runtime incompatabilities.                    *
 *                                                                           *
 *****************************************************************************/

/******************** DM SPECIFIC CONFIGURATION ********************/
#ifdef OMADM

/*
 * The maximum depth of the DM tree. If not defined then
 * there is no limit.
 */
#define OMC_URI_MAX_DEPTH			 16
#define OMC_URI_MAX_DEPTH_TXT		"16"

/*
 * The maximum total length of a node URI. If not defined
 * then there is no limit.
 */
#define OMC_URI_MAX_TOT_LEN			 255
#define OMC_URI_MAX_TOT_LEN_TXT		"255"

/*
 * The maximum length of any node name. If not defined then
 * there is no limit.
 */
#define OMC_URI_MAX_SEG_LEN			 31
#define OMC_URI_MAX_SEG_LEN_TXT		"31"

/*
 * This should be defined to enable the fine-grained tree
 * storage mechanism. (It also switches the win32 port to use
 * a directory structure to store the tree.)
 */
/* #define OMC_TREE_FGS */

#endif


/******************** DS SPECIFIC CONFIGURATION ********************/
#ifdef OMADS

/*
 * The maximum length of a GUID (global unique identifier) that
 * the server is allowed to send to the client. (Actually OMC
 * doesn't care but the protocol demands that a value be sent.)
 */
#define OMC_GUID_MAX_SIZE			31

/*
 * Whether OMC is to support Ext values in the DevInf. Without
 * this defined OMC will receive Ext values and ignore them but
 * can never generate them. With this defined OMC calls the
 * DSEXT API functions to fetch values from or pass values to
 * porting code. (It does nothing with the values itself.)
 */
#define SUPPORT_DEVINF_EXT

/*
 * Whether OMC is to support EMI (Experimental Meta Information)
 * values in the MetInf. Without this defined OMC will receive
 * EMI values and ignore them but can never generate them. With
 * this defined OMC calls the DSEXT API functions to fetch values
 * from or pass values to porting code. (It does nothing with
 * the values itself.)
 */
#define SUPPORT_METINF_EMI

#endif


/******************** SHARED CONFIGURATION ********************/

/*
 * The maximum length of the value of the ServerId node
 * in a DM acoount subtree.
 */
#define OMC_SERVERID_MAX_LEN		256

/*
 * The maximum length of a PIN
 */
#define OMC_PIN_MAX_LEN				20

/*
 * The maximum length of the Network Shared Secret
 */
#define OMC_NSS_MAX_LEN				16

/*
 * Defining OMC_ENABLE_TEST_MODE enables various command line
 * arguments useful for configuring the client during testing.
 * (Particularly arguments to do with authentication.)
 */
/* #define OMC_ENABLE_TEST_MODE */

/*
 * The server should not send status code 212 when using HMAC
 * authentication. Defining OMC_ALLOW_212_WITH_HMAC makes OMC
 * treat status code 212 as if it was status code 200 in these
 * circumstances.
 */
/* #define OMC_ALLOW_212_WITH_HMAC */


/*****************************************************************************
 *                                                                           *
 *                             SDK CONFIGURATION                             *
 *                                                                           *
 * The following configuration items are for all builds. They are variables  *
 * defined in omc_config.c and the values can be altered by editing that     *
 * file.                                                                     *
 *                                                                           *
 *****************************************************************************/

#ifdef OMC_ENABLE_TEST_MODE
#define CONFIG_CONST
#else
#define CONFIG_CONST	const
#endif

typedef struct OMC_Config_s
{
	/*
	 * Set the mimeType we wish to use by default.
	 */
	IBOOL		wbxmlMsgEncoding;

	/*
	 * The default level of authentication for the client to send in the
	 * first package of a DM session. One of:
	 *
	 *   "syncml:auth-none"        (CONST_auth_none)
	 *   "syncml:auth-basic"       (CONST_auth_basic)
	 *   "syncml:auth-md5"         (CONST_auth_md5)
	 *   "syncml:auth-MAC"         (CONST_auth_hmac)
	 */
	UTF8CStr	defaultClientAuthType;

	/*
	 * The minimum level of authentication required from the server.
	 * One of (in "increasing" order):
	 *
	 *   "syncml:auth-none"        (CONST_auth_none)
	 *   "syncml:auth-basic"       (CONST_auth_basic)
	 *   "syncml:auth-md5"         (CONST_auth_md5)
	 *   "syncml:auth-MAC"         (CONST_auth_hmac)
	 */
	UTF8CStr	minServerAuthType;

	/*
	 * The maximum level of authentication required from the server.
	 * One of (in "decreasing" order):
	 *
	 *   "syncml:auth-MAC"         (CONST_auth_hmac)
	 *   "syncml:auth-md5"         (CONST_auth_md5)
	 *   "syncml:auth-basic"       (CONST_auth_basic)
	 *   "syncml:auth-none"        (CONST_auth_none)
	 */
	UTF8CStr	maxServerAuthType;

	/*
	 * Whether the server delivers a new client nonce to be used for
	 * the next message or just for the next session. (This is only
	 * relevant for HMAC authentication.)
	 *
	 * Change request OMA-DM-2004-0162R01-CR_ClarifyHMACAndNonceChal
	 * mandated the former behaviour but the specification was unclear
	 * before this and different servers behaved in different ways.
	 */
	IBOOL		clientNoncePerMessage;

	/*
	 * Whether the client is to generate a new server nonce for every
	 * message or just for the next session. (This is only relevant
	 * for HMAC authentication.)
	 *
	 * Change request OMA-DM-2004-0162R01-CR_ClarifyHMACAndNonceChal
	 * mandated the former behaviour but the specification was unclear
	 * before this and different servers behaved in different ways.
	 */
	IBOOL		serverNoncePerMessage;

	/*
	 * Maximum size of a message.
	 */
	IU32		maxMsgSize;

	/*
	 * Maximum size of a data object.
	 */
	IU32		maxObjSize;

	/*
	 * Resend commands for package1 until they are sent successfully
	 * even if this means replying to an authenticated message with
	 * no commands from the server which would normally terminate
	 * the session.
	 */
	IBOOL		ensurePackage1Sent;

	/*
	 * Proxy setting for DM.
	 */
	UTF8CStr	dmProxy;

	/*
	 * Root element of dm tree in persistent storage.
	 */
	UTF8CStr	rootElement;

	/*
	 * Max retry times for connect operations from client to server.
	 */
	IU32		maxNetRetries;

} OMC_Config;


/******************** DM SPECIFIC CONFIGURATION ********************/
#ifdef OMADM

/*
 * DM session configuration (see structure definition above for details).
 */
extern OMC_Config OMC_dmConfig;

/*
 * Enable/Disable automatic initiation of a followup management session
 * after a DM/Plain Profile Bootstrap.  Note that for CP/WAP Profile
 * bootstraps this is determined by the presence or absence of the "INIT"
 * parameter in the message.
 */
extern CONFIG_CONST IBOOL		OMC_bootDMFollowup;

#endif


/******************** DS SPECIFIC CONFIGURATION ********************/
#ifdef OMADS

/*
 * DS session configuration (see structure definition above for details).
 */
extern CONFIG_CONST OMC_Config	OMC_dsConfig;

#endif


/******************** SHARED CONFIGURATION ********************/

/*
 * Maximum total size of all the session workspaces. This is the same
 * as the total of the maxMsgSize values for all the sessions allowed
 * to run simultaneously. A value of zero means that there is no limit
 * on the total size of the workspaces.
 */
extern CONFIG_CONST IU32 OMC_maxTotalWorkspaceSize;


/** Default root element name if rootElement in OMC_Config is NULL. */
#define MDM_DEFAULT_ROOT_ELEMENT "rdmtree"

/** Get root element name. */
extern UTF8CStr mdm_get_default_root_element(void);

#endif /* !_OMC_OMC_CONFIG_H_ */
