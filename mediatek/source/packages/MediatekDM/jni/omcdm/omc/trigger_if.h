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
 *		Name:					trigger_if.h
 *
 *		Project:				OMC
 *
 *		Created On:				June 2004
 *
 *		Derived From:			(original)
 *
 *		Version:				$Id: //depot/main/base/omc/trigger_if.h#24 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
]*/

/*! \file
 *		Defines the OMC external trigger API.
 *
 *		Since the porting layer is pure binary in nature, the interface must be
 *		strictly procedural. Thus no platform dependent constants,	defines,
 *		additional include files etc can appear in the interface.
 *
 * \brief	Trigger API
 */

#ifndef _OMC_TRIGGER_IF_H_
#define _OMC_TRIGGER_IF_H_

#ifdef __cplusplus
extern "C" {
#endif


/*
 * Initialization data passed to TRG_OMC_init()
 */
typedef struct {
	IU32			debugFlags;		/* Only used if PROD_MIN not defined */
	const char*		memFailure;		/* Only used if OMC_MEM_DEBUG defined */
} TRG_InitData;

/* Trigger reason */
typedef enum {
	/*
	 * DM sessions
	 */
	TRG_REASON_DM_NOTIFY,	/* Notification trigger */
	TRG_REASON_DM_USER,		/* User trigger */
	TRG_REASON_FUMO,		/* Continue postponed FUMO download/update */
	TRG_REASON_FUMO_DONE,	/* Run FUMO DM session after download/update */

	/*
	 * DS sessions
	 */
	TRG_REASON_DS_NOTIFY,	/* Notification trigger */
	TRG_REASON_DS_USER,		/* User trigger */
	TRG_REASON_DS_RESUME,	/* Continue interrupted session */

	/*
	 * Non-specific sessions
	 */
	TRG_REASON_BOOT			/* Bootstrap trigger */
} TRG_Reason;

/*
 * Bootstrap profile
 */
typedef enum {
	TRG_PROFILE_CP,			/* CP/WAP profile; OMA-CP format message */
	TRG_PROFILE_DM			/* DM/Plain profile; OMA-DM format message */
} TRG_Profile;

/*
 * SEC Media Type Parameter used on bootstrap messages,
 * see [PROVCONT] section 4.3.
 *
 * If no SEC parameter is present in the message, no security is used.
 * The value TRG_SECURITY_NONE is used internally to represent this.
 */
typedef enum {
	TRG_SECURITY_NONE			= -1,
	TRG_SECURITY_NETWPIN		= 0,
	TRG_SECURITY_USERPIN		= 1,
	TRG_SECURITY_USERNETWPIN	= 2,
	TRG_SECURITY_USERPINMAC		= 3
} TRG_Security;

/*
 * Sync session to run
 */
#ifdef OMADS
typedef struct TRG_Sync_s {
	UTF8CStr		dbName;			/* Client database name */
	UTF8CStr		contentType;	/* Content mime type */
	IU32			syncType;		/* Type of DS sync to do */
	struct TRG_Sync_s *next;		/* Next sync in list */
} TRG_Sync, *TRG_SyncPtr;
#endif

/*
 * Trigger data passed to TRG_OMC_setReason()
 */
typedef struct {
	TRG_Reason		reason;		/* Why client was invoked */
	IU8				*message;	/* Notification or bootstrap message (binary)*/
	IU32			messageLen;	/* Length of message */
	TRG_Profile		profile;	/* Profile used for bootstrap */
	TRG_Security	sec;		/* Security method for bootstrap */
	const char		*mac;		/* Message authentication code for boot (hex)*/
	UTF8CStr		serverId;	/* DM/DS server ID to use for a user initiated
								 * session. */
#ifdef OMADS
	TRG_SyncPtr		sync;		/* List of syncs to perform */
#endif
} TRG_Data;


/* ========================================================================== */
/* Generic Trigger entry points												  */
/* ========================================================================== */

/*!
 * Initialise OMC.
 *
 * This function must be called before any other OMC function. If it
 * returns an error then no other OMC function should be called except
 * for \ref TRG_OMC_term.
 *
 * \param	idp			Pointer to initialization data
 *
 * \param	udp			The user data pointer.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
 */
extern OMC_Yield TRG_OMC_init(TRG_InitData *idp, OMC_UserDataPtr udp);

/*!
 * Terminate OMC.
 *
 * This function must be called when the client is shutting down to
 * release all resources it owns. It should be called even if
 * \ref TRG_OMC_init fails.
 */
extern void TRG_OMC_term(void);

/*!
 * Create an OMC session.
 *
 * \param	tdp			Pointer to trigger data
 *
 * \param	udp			User session identifier. This is stored by OMC
 *						and passed to porting APIs to identify the
 *						session making the call.
 *
 * \param	pSdp		Where to store OMC session identifier. This is
 *						to be passed to OMC APIs to identify the session
 *						being run.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
 */
extern OMC_Error TRG_OMC_initSession(TRG_Data *tdp, OMC_UserDataPtr udp,
	OMC_SessionDataPtr *pSdp);

/*!
 * Destroy an OMC session.
 *
 * This function should be called after TRG_OMC_run() has returned a
 * non-slicing error code or if TRG_OMC_initSession() fails.
 *
 * \param	sdp			OMC session identifier
 */
extern void TRG_OMC_termSession(OMC_SessionDataPtr sdp);

/*!
 * Set the message encoding to use for messages sent to the server.
 *
 * If this function is not called then the default set by the
 * wbxmlMsgEncoding configuration variable will be used.
 *
 * \param	sdp			OMC session identifier
 *
 * \param	wbxml		TRUE => use WBXML encoding; FALSE => use XML encoding.
 */
extern void TRG_OMC_setEncoding(OMC_SessionDataPtr sdp, IBOOL wbxml);

/*!
 * Run OMC.
 *
 * The general operation of OMC is driven by repeated calls to TRG_OMC_run().
 * This function performs some work and then returns. If there is further
 * work to be done it returns one of the time slicing error codes
 * OMC_ERR_YIELD or OMC_ERR_WAIT; any other error code means that OMC has
 * completed the session.
 *
 * \param	sdp			OMC session identifier
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
 */
extern OMC_Yield TRG_OMC_run(OMC_SessionDataPtr sdp);

/*!
 * Request the current session to suspend.
 *
 * \param	sdp			OMC session identifier
 */
#ifdef OMADS
extern void TRG_OMC_suspendSession(OMC_SessionDataPtr sdp);
#endif

/*!
 * Request the current session to abort.
 *
 * Note that even in the immediate abort case OMC will continue calling
 * a porting API function that has been started and is returning yield
 * or wait errors until it returns OK or a real error. It may even start
 * calling other such functions before the abort request is noticed.
 * Also, other porting API functions will be called as OMC is terminating.
 *
 * DS sessions can only be aborted "immediately" as there is no mechanism
 * to report an abort request to the server in DS.
 *
 * \param	sdp			OMC session identifier
 *
 * \param	immediate	Whether to abort without notifying the server
 */
extern void TRG_OMC_abortSession(OMC_SessionDataPtr sdp, IBOOL immediate);


/* ========================================================================== */
/* Platform dependent implementations										  */
/* ========================================================================== */

/*!
 * Fetch the OMC session given the user session identifier.
 *
 * \param	udp			User session identifier.
 *
 * \return	The OMC session identifier.
 */
#if defined(FUMO) || defined(OMADL)
extern OMC_SessionDataPtr OMC_TRG_getSession(OMC_UserDataPtr udp);
#endif

/*!
 * Get PIN from user
 *
 * The PIN must be a string of ascii-encoded decimal digits (octets with
 * values 0x30 to 0x39).
 *
 * \param	udp			Pointer to user session data
 * \param	buffer		Where to store pin
 * \param	bufferLen	Length of supplied buffer
 * \param	pinLenPtr	Where to store length of pin
 *
 * \return	OMC error code (OMC_ERR_BOOT_PIN if PIN is unobtainable)
 */
extern OMC_Yield OMC_TRG_getPin(OMC_UserDataPtr udp, char *buffer,
			IU32 bufferLen, IU32 *pinLenPtr);

/*!
 * Get Network specific Shared Secret (NSS)
 *
 * The NSS is network specific: e.g. for GSM it is the IMSI while for
 * CDMA or TDMA it is the ESN appended with SSD or SSD_S.
 * The value returned by this function should be in a form ready for input
 * to the MAC calculation and must be derived as described in [PROVBOOT]
 * section 6.
 *
 * \param	udp			Pointer to user session data
 * \param	buffer		Where to store NSS
 * \param	bufferLen	Length of supplied buffer
 * \param	nssLenPtr	Where to store length of NSS
 *
 * \return	OMC error code (OMC_ERR_BOOT_NSS if NSS is unobtainable)
 */
extern OMC_Yield OMC_TRG_getNss(OMC_UserDataPtr udp, IU8 *buffer,
			IU32 bufferLen, IU32 *nssLenPtr);

/*!
 * Get Address Type
 *
 * This function is called on receipt of a WAP Profile bootstrap message,
 * in order to determine the value of the DM account node "AddrType".
 * The specification says that the "DM Client chooses the value according
 * to the transports it supports. The DM Server may modify this in a
 * subsequent DM session." ([DM-Bootstrap] 5.3.2).  It is unclear how such
 * a session can take place if AddrType is wrong however!
 *
 * \param	udp			Pointer to user session data
 * \param	addr		Contents of DM account node "Addr", as set by WAP
 *						bootstrap.
 * \param	pAddrType	Where to store address type. Possible values are
 *						"1" for HTTP, "2" for WSP and "3" for OBEX.
 *
 * \return	OMC error code
 */
extern OMC_Yield OMC_TRG_getAddrType(OMC_UserDataPtr udp, UTF8CStr addr,
			UTF8CStr *pAddrType);

/*!
 * Get Session ID
 *
 * This function is called to fetch a session ID for all DM sessions except
 * those started by a notification message (which includes the required
 * session ID in the message).
 *
 * It is acceptable to return a constant value but this can cause problems
 * with some DM servers as they may try to continue a previous interrupted
 * session. It is better to return a different value each time (perhaps
 * derived from the time).
 *
 * \param	udp			Pointer to user session data
 * \param	pId			Where to store the session ID.
 *
 * \return	OMC error code
 */
extern OMC_Yield OMC_TRG_getSessionId(OMC_UserDataPtr udp, IU16* pId);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_TRIGGER_IF_H_ */
