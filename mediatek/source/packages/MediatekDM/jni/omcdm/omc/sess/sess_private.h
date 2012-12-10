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
 *		Name:				sess_private.h
 *
 *		Project:			OMC
 *
 *		Derived From:		Original
 *
 *		Created On:			May 2004
 *
 *		Version:			$Id: //depot/main/base/omc/sess/sess_private.h#87 $
 *
 *		Coding Standards:	3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
 *
]*/

/*! \internal
 * \file
 *		Defines the structures and enumerations required for the Session
 *		Manager. Many of these are sued for holding the details of commands
 *		sent via the SyncML server. This also defines the structure used to
 *		hold the session information which is passed around via the RTK to
 *		each of the callback functions.
 *
 * \brief	Define the data structures used within the Session Manager
 */

#ifndef _OMC_SESS_SESS_PRIVATE_H_
#define _OMC_SESS_SESS_PRIVATE_H_

#include <omc/omc_cmdtable.h>		/* For OMC_CmdType */
#include <omc/omc_timeslice.h>
#include <omc/lib/sha1/sha1.h>		/* For SHA_DIGESTSIZE */
#include <omc/trg/trg_info.h>		/* For TRG_Info */
#include <syncml/sml/sml.h>			/* For InstanceID_t */

#ifdef OMADM
#include <omc/wbxml/wbxml_omacp.h>	/* For OMACP_Characteristic */
#endif

#ifdef FUMO
#include <omc/fumo/fumo_session.h>	/* For OMC_FumoSessionType */
#endif

#ifdef OMADL
#include <omc/dl/omadl_parsedd.h>	/* For OMADL_DownloadDescriptor */
#endif

#ifdef __cplusplus
extern "C" {
#endif


/*
 * Avoid circular inclusions by defining types here.
 */
#ifdef OMADS
typedef const struct OBJ_Datastore_s *	OBJ_DatastorePtr;
typedef       struct OBJ_Session_s *	OBJ_SessionPtr;
#endif


/*
 * Structures for recording errors
 * -------------------------------
 */

/*
 * Structure for holding error details
 */
typedef struct SESS_ErrorInfo_s
{
	OMC_Error		errorCode;			/* OMC_ERR_OK => Not set */

	OMC_CmdType		cmdType;			/* If known and relevant */
	IU32			resultCode;			/* If known and relevant */
	UTF8Str			explanation;		/* If known and present */
} SESS_ErrorInfo, *SESS_ErrorInfoPtr;


/*
 * Structures and enums for handling authentication.
 * ------------------------------------------------
 */

/*
 * Enumeration of possible authentication methods.
 */
typedef enum
{
	SESS_AUTHTYPE_UNKNOWN	= -1,
	SESS_AUTHTYPE_NONE		=  0,
	SESS_AUTHTYPE_BASIC		=  1,
	SESS_AUTHTYPE_MD5 		=  2,
	SESS_AUTHTYPE_HMAC 		=  3
} SESS_AuthType;


/*
 * Enumeration of states of authentication.
 */
typedef enum
{
	/* Not authenticated */
	SESS_AUTHSTATE_NONE				= 0,

	/* Not authenticated, incorrect credentials supplied.
	 * (A server state only.) */
	SESS_AUTHSTATE_FAILED			= 1,

	/* Not authenticated, challenge issued/received */
	SESS_AUTHSTATE_CHALLENGED		= 2,

	/* Authenticated for a single message but a new nonce needs
	 * to be sent to the server. (A server state only.) */
	SESS_AUTHSTATE_MESSAGE_NONCE	= 3,

	/* Authenticated for single message */
	SESS_AUTHSTATE_MESSAGE			= 4,

	/* Authenticated for entire session but a new nonce needs
	 * to be sent to the server. (A server state only.) */
	SESS_AUTHSTATE_SESSION_NONCE 	= 5,

	/* Authenticated for entire session */
	SESS_AUTHSTATE_SESSION			= 6
} SESS_AuthState;

#define SESS_AUTH_HAVE_CHALLENGED(state)   (SESS_AUTHSTATE_CHALLENGED <= state)
#define SESS_AUTHENTICATED(state)		(SESS_AUTHSTATE_MESSAGE_NONCE <= state)
#define SESS_AUTH_FOR_SESSION(state)	(SESS_AUTHSTATE_SESSION_NONCE <= state)


/*
 * A structure to hold the authentication data and the
 * authentication state of one side of a session. One
 * of these is used for client authentication and
 * another for server authentication.
 */
typedef struct SESS_Auth_s {
	/* The current state of authentication */
	SESS_AuthState		state;

	/* SERVER: The range of acceptable types of authentication */
	/* CLIENT: The type of credentials to send is stored in minType
	 *         and maxType is ignored */
	SESS_AuthType		minType;
	SESS_AuthType		maxType;

	/* The username  to use to construct credentials */
	/* For the server session authentication this is the ServerId */
	UTF8Str				username;
	IU32				usernameLength;

	/* The password to use to construct credentials */
	UTF8Str				password;
	IU32				passwordLength;

	/* The nonce to use for creating/checking credentials */
	IU8*				nonce;
	IU32				nonceLength;

	/* Whether a new nonce has been received/generated */
	IBOOL 				haveNewNonce;

	/* The new nonce (not yet to be used for creating/checking credentials) */
	IU8*				newNonce;
	IU32				newNonceLength;
} SESS_Auth, *SESS_AuthPtr;

/*
 * Structures and enums for holding the SyncML commands
 * ---------------------------------------------------
 *
 * These are easier to manager and understand than the toolkit SML structures.
 */

/*
 * Holds the <Data>...</Data> portion of a cmd or Item
 */
typedef struct SESS_Data_s {
	IU32		len;
	IU8			*data;
} SESS_Data, *SESS_DataPtr;


/*
 * Hold the relevant Meta information for a command or Item
 */
typedef struct SESS_Meta_s {
	IU32				maxMsgSize;
	IU32				maxObjSize;
	IU32				size;
	UTF8Str				type;
	UTF8Str				format;
	UTF8Str				nextNonce;
#ifdef FUMO
	UTF8Str				mark;
#endif
#ifdef OMADS
	UTF8Str				lastAnchor;
	UTF8Str				nextAnchor;
	IBOOL				sharedMem;	/* Ignore unless freeMem set */
	IU32				freeMem;	/* NB: Includes offset defined below */
	IU32				freeId;		/* NB: Includes offset defined below */
#ifdef SUPPORT_METINF_EMI
	/* If the EMI values have been fetched by calling OMC_DSEXT_getEmi
	 * then they need to be freed by calling OMC_DSEXT_freeEmi. The
	 * emiFree flag is set if this is the case (and the userData pointer
	 * is also stored so that the free function has access to it). */
	IBOOL						emiFree;
	OMC_UserDataPtr				emiUserData;

	OMC_DSEXT_EmiListPtr		emi;
#endif
#endif
} SESS_Meta, *SESS_MetaPtr;

/*
 * Because zero is a valid value for the free mem and id fields
 * we add an offset to values before we store them. Thus the
 * real value is the field minus the offset.
 */
#ifdef OMADS
#define SESS_FREEMEM_OFFSET	(IU32)1
#define SESS_FREEID_OFFSET	(IU32)1
#endif


/*
 * Hold the details of a Datastore (part of the Devinf information)
 */
#ifdef OMADS
typedef struct SESS_Datastore_s {
	UTF8Str						sourceRef;
	OMC_STORE_Description		desc;

	/* If the description has been fetched from a datastore then the
	 * descOwner is set to that datastore and this allows the correct
	 * free function to be called. If the description is not owned by
	 * a datastore and should therefore be freed by OMC then the
	 * descOwner is set to NULL. */
	OBJ_DatastorePtr			descOwner;
	/* The description free function requires the udp to be passed to
	 * it but the freeDatastore function that calls it does not have
	 * access to the udp so we record it here. */
	OMC_UserDataPtr				udp;

	struct SESS_Datastore_s	*	next;
} SESS_Datastore, *SESS_DatastorePtr;
#endif


/*
 * Hold the Devinf information for a Get or Put command item
 */
#ifdef OMADS
typedef struct SESS_Devinf_s {
	UTF8Str						man;
	UTF8Str						mod;
	UTF8Str						oem;
	UTF8Str						fwV;
	UTF8Str						swV;
	UTF8Str						hwV;
	UTF8Str						devId;
	UTF8Str						devTyp;
	SESS_DatastorePtr			datastores;
	IU32						flags;

#ifdef SUPPORT_DEVINF_EXT
	/* If the Ext values have been fetched by calling OMC_DSEXT_getExt
	 * then they need to be freed by calling OMC_DSEXT_freeExt. The
	 * extFree flag is set if this is the case (and the userData pointer
	 * is also stored so that the free function has access to it). */
	IBOOL						extFree;
	OMC_UserDataPtr				extUserData;

	OMC_DSEXT_ExtListPtr		ext;
#endif
} SESS_Devinf, *SESS_DevinfPtr;

/* Values used for flags of SESS_Devinf */
#define SESS_DEVINF_UTC					0x0100	/* == SmlDevInfUTC_f */
#define SESS_DEVINF_NUMBER_OF_CHANGES	0x0200	/* == SmlDevInfNOfM_f */
#define SESS_DEVINF_LARGE_OBJECTS		0x0400	/* == SmlDevInfLargeObject_f */
#endif


/*
 * An entry in a list of strings. (For example, a list of sources.)
 */
typedef struct SESS_UTF8List_s {
	UTF8Str					string;

	struct SESS_UTF8List_s	*next;
} SESS_UTF8List, *SESS_UTF8ListPtr;


/*
 * Holds the information for a single Item. If this Item is in a list, the
 * element 'next' points to the next Item.
 */
typedef struct SESS_Item_s {
	UTF8Str				sourceURI;
	UTF8Str				targetURI;
#ifdef OMADS
	UTF8Str				sourceParentURI;
	UTF8Str				targetParentURI;
	SESS_DevinfPtr		devinf;
#endif
	SESS_MetaPtr		meta;
	SESS_DataPtr		data;
	IU32				flag;
	IU32				resultCode;
	struct SESS_Item_s	*next;
} SESS_Item, *SESS_ItemPtr;

/* Values used for flag of SESS_Item */
#define SESS_ITEM_HAS_MORE_DATA				0x01
#define SESS_ITEM_STATUS_GENERATED_OK		0x02
#define SESS_ITEM_RESULT_GENERATED_OK		0x04
#define SESS_ITEM_HAS_SOURCE_LOCNAME		0x08
#define SESS_ITEM_HAS_TARGET_LOCNAME		0x10


/*
 * Holds the information for a single MapItem of a Map command.
 */
typedef struct SESS_MapItem_s {
	UTF8Str					source;
	UTF8Str					target;

	IBOOL					generated;

	struct SESS_MapItem_s	*next;
} SESS_MapItem, *SESS_MapItemPtr;


/*
 * The Command information for an incoming or outgoing command.
 */
typedef struct SESS_Cmd_s {
	OMC_CmdType			cmdType;	/* Which command this is */
	IU32				cmdId;		/* The cmdID the server assigned it */
	IU32				msgId;		/* The msgID the server assigned it */
	IU32				resultCode; /* Used for incoming Status or outgoing
									 * response */
	SESS_ItemPtr		item;		/* Chain of items for this command */
	SESS_MetaPtr		meta;		/* Meta data specific to this command */

#ifdef OMADS
	SESS_MetaPtr		credMeta;	/* Credentials */
	SESS_DataPtr		credData;

	SESS_AuthPtr		chalAuth;	/* Auth info to use to build a challenge */

	IU32				flags;		/* Command flags like NoResp (see below) */
#endif

	union
	{
		/* SESS_CMD_END_MESSAGE */
		struct
		{
			IBOOL		final;		/* Indicates whether this is the end
									 * of a set of messages */
		} end;

		/* SESS_CMD_STATUS */
		struct
		{
			IU32			cmdRef;		/* Command for which this is status */

			SESS_MetaPtr	chal;		/* Meta data containing challenge */
			SESS_AuthType 	chalType;	/* Type of challenge */
		} status;

		/* SESS_CMD_ALERT */
		struct
		{
			IU32		number;		/* The alert number */
			/*
			 * The following fields are used when sending
			 * alert commands to avoid building an item
			 * list of a single item.
			 */
			UTF8Str		itemSource;	/* Item source reference, if any */
			UTF8Str		itemTarget;	/* Item target reference, if any */
			UTF8Str		itemData;	/* Item data, if any */
		} alert;

		/* SESS_CMD_START_ATOMIC */
		struct
		{
			struct SESS_Cmd_s 	*chain;
		} atomic;

		/* SESS_CMD_START_SEQUENCE */
		struct
		{
			struct SESS_Cmd_s 	*chain;
		} sequence;

#ifdef OMADS
		/* SESS_CMD_START_SYNC */
		struct
		{
			UTF8Str				sourceUri;
			UTF8Str				targetUri;

			IU32				noc;		/* With offset (see below) */

			struct SESS_Cmd_s 	*chain;
		} sync;

		/* SESS_CMD_MAP */
		struct
		{
			UTF8Str			sourceUri;
			UTF8Str			targetUri;

			SESS_MapItemPtr	mappings;	/* Chain of map items */
		} map;

		/* SESS_CMD_RESULTS */
		struct
		{
			IU32			msgRef;
			IU32			cmdRef;
		} results;

		/* SESS_CMD_SEARCH */
		struct
		{
			UTF8Str				query;		/* Search query */
			SESS_UTF8ListPtr	sourceUri;	/* List of sources */
			UTF8Str				lang;		/* Preferred language */
		} search;
#endif
	} u;

	struct SESS_Cmd_s 	*next;
} SESS_Cmd, *SESS_CmdPtr;

/* Values used for flags of SESS_CMD */
#define SESS_CMD_SFT_DEL	0x01	/* Delete command - soft delete. */
#define SESS_CMD_ARCHIVE	0x02	/* Delete command - archive and delete. */
#define SESS_CMD_NO_RESP	0x04	/* No status response required. */

/*
 * Because zero is a valid value for the number of changes
 * we add an offset to values before we store them. Thus
 * the real value is the field minus the offset.
 */
#ifdef OMADS
#define SESS_NOC_OFFSET		(IU32)1
#endif


/*
 * Hold the response information for a Get operation
 */
typedef struct SESS_Results_s {
	UTF8Str					sourceURI;
	SESS_Meta				meta;
	SESS_Data				dataItem;
#ifdef OMADS
	SESS_DevinfPtr			devinfPtr;
#endif
	struct SESS_Results_s	*next;
} SESS_Results, *SESS_ResultsPtr;


/*
 * The structure holding a queue of commands.
 */
typedef struct SESS_Queue_s
{
	SESS_CmdPtr		head;
	SESS_CmdPtr		tail;
#ifndef PROD_MIN
	const char*		name;
#endif
} SESS_Queue, *SESS_QueuePtr;

/*
 * Structure used for holding session specific data.
 */
struct OMC_SessionData_s
{
	/* The instance ID required by the RTK */
	InstanceID_t	id;

	/* The current session ID as a string */
	IU8 			sessionId[5];

	/* The actual value of the sessionID */
	IU16			sessionIdVal;

	/* The client's current msgID */
	IU32 			msgId;

	/* The client's current cmdID */
	IU32 			cmdId;

	/* Flag to indicate if we need to send another message to the server. */
	IU32 			replyRequired;

	/* The current msgID from the server */
	IU32 			serverMsgID;

	/* The maxMsgSize from the server or 0 */
	IU32 			maxMsgSize;

	/* The maxObjSize from the server or 0 */
	IU32 			maxObjSize;

	/* State of client authentication */
	SESS_Auth		clientAuth;

	/* State of server authentication */
	SESS_Auth		serverAuth;

	/* The server's credentials received in the last StartMessage cmd */
	SESS_MetaPtr	serverCredMeta;	/* Meta details */
	SESS_DataPtr	serverCredData;	/* Data details */

	/* The sourceURI from the last StartMessage cmd */
	UTF8Str			msgSourceUri;

	/* The URI to use to contact the server */
	UTF8Str			serverUri;

	/* The URI to set as the sourceURI on messages the client creates */
	UTF8Str			clientUri;

	/* Flag to indicate if the server has sent a <Final/> as part of the
	 * last message */
	IBOOL			final;

	/* In DS the NoResp flag in the SyncHdr command applies to all
	 * the commands in the message. */
#ifdef OMADS
	IBOOL			noResp;
#endif

#ifdef OMADM
	/* The DM account being used for this session. */
	IU8				accUri[OMC_URI_MAX_TOT_LEN + 1];

	/* Points after the account path in the accUri. Store a leaf node
	 * name here to make accUri the full name of an account node. */
	UTF8Str			accUriEnd;
#endif

	/* The ServerId value to be used to check ACL permissions.
	 * This usually points at the same string as serverId and
	 * when it doesn't it points at a static string.
	 * ie. This pointer never needs freeing! */
#ifdef OMADM
	UTF8Str			chkServerId;
#endif

	/* The datastore session corresponding to the sync being processed */
#ifdef OMADS
	OBJ_SessionPtr	dssMsg;
#endif

	/* Whether the session has been requested to suspend. */
#ifdef OMADS
	IBOOL			suspendSession;
#endif

	/* Whether the session has been suspended. */
#ifdef OMADS
	IU32			sessionSuspended;
#endif

	/* Whether the session has been requested to abort. */
	enum
	{
		ABORT_NOT_REQUESTED = 0,
		ABORT_REQUESTED,
		ABORT_IMMEDIATE
	}				abortSession;

	/* Whether the session has been aborted. */
	enum
	{
		ABORT_NOT_ABORTED = 0,
		ABORT_ALERTED,
		ABORT_NOW
	}				sessionAborted;


	/* Whether the last message received was in XML or WBXML encoding. */
	IBOOL			recvWbxml;

	/* Whether the next message to send is to be in XML or WBXML encoding. */
	IBOOL			sendWbxml;


	/*
	 * Large object reading state. Most of this is initialized
	 * from the command and item containing first chunk of the
	 * large object.
	 */

	/* The command type - used for checking subsequent chunks and to
	 * indicate whether large object reading is active */
	OMC_CmdType		loCmdType;

	/* The meta data - not all meta data need be supplied on subsequent
	 * chunks so we save that from the first chunk. */
	SESS_MetaPtr	loMeta;

	/* The source node - used for checking subsequent chunks. According
	 * to the spec this needn't be supplied on subsequent chunks! */
#ifdef OMADS
	UTF8Str			loSourceURI;
#endif

	/* The target node - used for checking subsequent chunks. According
	 * to the spec this needn't be supplied on subsequent chunks! */
	UTF8Str			loTargetURI;

	/* Where to store the object data */
	IU8*			loBuffer;

	/* The number of bytes stored */
	IU32			loLength;

	/* The expected number of bytes */
	IU32			loSize;

	/* Whether this object is Base64 encoded */
	IBOOL			loB64;

	/* The number of pending chars stored */
	IU32			loB64Pending;

	/* Storage for pending chars from the end of the previous chunk */
	IU8				loB64Buffer[4];

	/* It is not possible to report an error while in the process
	 * of reading a large object so we store the error here to be
	 * returned on the last chunk. */
	OMC_Error		loError;


	/*
	 * Large object writing state.
	 */

	/* The free space in the RTK buffer after the header and first
	 * status - used to determine whether an object could ever fit
	 * in the buffer without being treated as a large object */
	IU32			loMaxFree;

	/* The number of bytes of the current large object sent so far.
	 * 0 => No large object */
	IU32			loSent;

	/* The cmdID of command containing the last large object chunk
	 * sent. */
	IU32			loCmdId;

	/*
	 * Command queues
	 */
	SESS_Queue		messageQ;
	SESS_Queue		statusQ;
	SESS_Queue		commandQ;
	SESS_Queue		resultQ;

	/*
	 * Details of the error that caused the session to terminate.
	 */
	SESS_ErrorInfo		errorInfo;

/*****************************************************************
 ** BEFORE HERE GETS ZEROIZED BEFORE EACH ACTUAL SESSION IS RUN **
 *****************************************************************
 *
 * A single session from the porting layers point of view can
 * actually contain multiple DM sessions internally. In particular
 * this is so for DM profile bootstraps.
 */

	OMC_UserDataPtr		userData; /* MUST BE FIRST NON-ZEROIZED VARIABLE */
	OMC_TreeSessionPtr	tsp;
	TRG_Data*			triggerData;
	TRG_Info			triggerInfo;
	CONFIG_CONST OMC_Config*	config;

	/* Whether to send messages in XML or WBXML encoding. */
	IBOOL				configSendWbxml;


/********** General time slicing **********/

	DEF_SLICE_SESSION


/********** dm_session.c **********/

#ifdef OMADM
	struct {
		DEF_SLICE;
	} SESS_dmBootstrap;

	struct {
		DEF_SLICE;
		IU32					accLen;
		IU32					apLength;
		UTF8Str					authPref;
	} SESS_dmInitAccount;
#endif


/********** ds_session.c **********/

#ifdef OMADS
	struct {
		DEF_SLICE;
		OMC_DSACC_Account		account;
		OMC_Error				result;
	} SESS_dsInitAccount;

	struct {
		DEF_SLICE;
		OMC_DSACC_Account		account;
	} SESS_dsUpdateNonces;

#endif


/********** fumo_download.c **********/

#ifdef FUMO
	struct {
		DEF_YIELD;
	} OMC_fumoDownload;

	struct {
		DEF_YIELD;
	} OMC_fumoDlAndUp;
#endif


/********** fumo_prompt.c **********/

#ifdef FUMO
	FUMO_MMI_Cmd				FUPR_response;

	struct {
		DEF_SLICE;
		char					buffer[12];
		IU32					len;
	} getNumericValue;

	struct {
		DEF_SLICE;
		FUMO_MMI_ScreenId		screen;
		IU8						prompt[120];
		IS32					timeout;
		IS32					count;
		IS32					interval;
		IU32					length;
		char					mandatory;
		char					buffer[12];
	} FUMO_promptUser;
#endif


/********** fumo_session.c **********/

#ifdef FUMO
	struct {
		DEF_SLICE;
	} FUMOSESS_storeValue;

	struct {
		DEF_SLICE;
		OMC_FumoSessionType		fumoType;
		UTF8Str					pkgURL;
	} FUMO_run;

	struct {
		DEF_SLICE;
		IU32					length;
	} OMC_storeFumoSession;

	struct {
		DEF_SLICE;
		IU32					length;
	} OMC_getFumoPendingTrigger;

	struct {
		DEF_SLICE;
		IU32					length;
		IU8						buffer[OMC_URI_MAX_TOT_LEN + 1];
	} OMC_getFumoSessionType;

	struct {
		DEF_SLICE;
		IU32					length;
		IU8						buffer[OMC_URI_MAX_TOT_LEN + 1];
		IU32					sLength;
		UTF8Str					strPtr;
	} OMC_getDLOTAURL;

	struct {
		DEF_SLICE;
	} OMC_clearFumoSession;
#endif


/********** fumo_storage.c **********/

#ifdef FUMO
	struct {
		DEF_YIELD;
	} OMC_writeFoto;
#endif


/********** fumo_update.c **********/

#ifdef FUMO
	struct {
		DEF_SLICE;
	} OMC_beginUpdate;

	struct {
		DEF_YIELD;
	} OMC_fumoUpdate;
#endif


/********** omadl.c **********/

#ifdef OMADL
	int /* enum dlotaResponseCode */ OMADL_storedResultCode;

	struct {
		DEF_SLICE;
		OMC_Error				result;
		IS32					readLen;
		IU32					offset;
		IU32					len;
		IU16 					rcode;
	} OMADL_getDownloadDescriptor;

	struct {
		DEF_SLICE;
		OMC_Error				result;
		IU8 *					newBuf;
		IU32					newOffset;
		IS32					readLen;
		IU32					len;
		IU16 					rcode;
	} OMADL_downloadObject;

	struct {
		DEF_SLICE;
		OMC_Error				result;
		IU32					len;
		IU16 					rcode;
	} OMADL_reportStatus;

	struct {
		DEF_SLICE;
		OMC_Error				result;
		OMADL_DownloadDescriptor dd;
		IU8 *					buf;
		IU32					bufLen;
		IU32					offset;
		UTF8Str					deviceID;
		IU32					maxUpdatePackageLen;
		int						attempts;
		IU32					rc;
	} OMC_omadlDownload;
#endif


/********** omadl_dev_if.c **********/

#ifdef OMADL
	struct {
		DEF_YIELD;
		IU32					bLength;
		IU32					vLength;
	} OMADL_DEV_getDeviceID;
#endif


/********** obj_datastore.c **********/

#ifdef OMADS
	struct {
		DEF_SLICE;
		UTF8Str					dbName;
	} OBJ_findServerDatastore;

	struct {
		DEF_SLICE;
		OMC_Error				result;
		OMC_STORE_Credentials	cred;
	} OBJ_openSession;

	struct {
		DEF_SLICE;
		OMC_STORE_Anchor		serverAnchor;
		OMC_STORE_Anchor		suspendAnchor;
	} OBJ_closeAllSessions;
#endif


/********** omc_control.c **********/

	enum {
		CTRL_RUN_NOTHING = 0,
#ifdef FUMO
		CTRL_RUN_UPDATE_AGENT,
		CTRL_RUN_FUMO,
#endif
		CTRL_RUN_SESSION
	} CTRL_runAction;

	struct {
		DEF_SLICE;
		int						i;
		int						count;
		int						total;
	} test;

	struct {
		DEF_SLICE;
		OMC_Error				result;
	} runInner;


/********** sess_postboot.c **********/

#if defined(OMADM) && defined(_WIN32_WCE)
	struct {
		DEF_SLICE;
		IU8						dmAccPath[OMC_URI_MAX_TOT_LEN + 1];
		IU32					dmAccLen;
	} OMC_postBootstrap;
#endif


/********** sess_alert.c **********/

	OMC_MMI_UserCmd				ALRT_recvdCmd;

	struct {
		DEF_SLICE;
#ifdef OMADS
		OBJ_SessionPtr			dss;
#endif
	} SESS_doAlertCmd;

	struct {
		DEF_SLICE;
	} SESS_showScreenAndWaitForCmd;


/********** sess_auth.c **********/

	struct {
		DEF_SLICE;
#ifdef OMADS
		OBJ_SessionPtr			dss;
		OMC_STORE_Credentials	cred;
#endif
	} SESS_updateClientNonce;

	struct {
		DEF_SLICE;
#ifdef OMADS
		OBJ_SessionPtr			dss;
		OMC_STORE_Credentials	cred;
#endif
	} SESS_updateServerNonce;


/********** sess_commands.c **********/

	struct {
		DEF_SLICE;
		SESS_CmdPtr				command;
		IBOOL					inSync;
	} SESS_processCmdQ;


/********** sess_comms.c **********/

	struct {
		DEF_SLICE;
	} SESS_openComms;

	struct {
		DEF_SLICE;
	} SESS_releaseComms;

	struct {
		DEF_SLICE;
		OMC_Error				result;
		IU8 *					buffer;
		IS32					bufSize;
		IS32					byteCount;
		UTF8CStr				username;
		UTF8Str					digest;
		UTF8CStr				algorithm;
		UTF8CStr				mimeType;
	} SESS_sendPkg;


/********** sess_dm_alert.c **********/

#ifdef OMADM
	IU32						DMALRT_choices;
	IU32						DMALRT_len;

	struct {
		DEF_SLICE;
		OMC_MMI_ScreenId		mmiScreen;
		OMC_MMI_ScreenOptions	options;
		UTF8Str *				array;
		UTF8Str					displayText;
	} SESS_dmHandleUIAlert;
#endif


/********** sess_dm_boot.c **********/

#ifdef OMADM
#define DMBOOT_NACCNODES	12
	IBOOL						DMBOOT_accNodesFound[DMBOOT_NACCNODES];

	struct {
		DEF_SLICE;
		OMC_Error				result;
		SESS_CmdPtr				addCommand;
		UTF8CStr				dmAccURI;
		UTF8CStr				conURI;
	} SESS_processDMBoot;

	struct {
		DEF_SLICE;
		IU32					serverIdLen;
		IU32					length;
	} SESS_verifyBootAdd;
#endif


/********** sess_dm_comms.c **********/

#ifdef OMADM
	IU8							COMMS_connNodeName[OMC_URI_MAX_TOT_LEN + 1];
	IU32						COMMS_connNodeNameLen;

	struct {
		DEF_SLICE;
		UTF8Str					string;
		IU32					length;
		IU32					addrType;
		IU32					portNbr;
	} SESS_dmOpenComms;

	struct {
		DEF_SLICE;
	} COMMS_OMC_fetchValue;
#endif


/********** sess_dm_processcmds.c **********/

#ifdef OMADM
	struct {
		DEF_SLICE;
		IU32					bLength;
	} SESS_dmGetProperty;

	struct {
		DEF_SLICE;
		UTF8CStr				attrib;
		IU32					bLength;
		IU32					vLength;
		UTF8Str					path;
	} SESS_dmGetCmd;

	struct {
		DEF_SLICE;
	} SESS_dmAddCmd;

	struct {
		DEF_SLICE;
		UTF8CStr				attrib;
		UTF8Str					path;
	} SESS_dmReplaceCmd;
#endif


/********** sess_dm_setup.c **********/

#ifdef OMADM
	struct {
		DEF_SLICE;
		IU32					bLength;
	} DMSETUP_makeItem;

	struct {
		DEF_SLICE;
		SESS_CmdPtr				cmd;
#ifdef FUMO
		OMC_FumoSessionType		sessType;
#endif
	} SESS_dmPreparePkg1;
#endif


/********** sess_dm_utils.c **********/

#ifdef OMADM
	struct {
		DEF_SLICE;
	} SESS_dmFetchAccountValueFn;

	struct {
		DEF_SLICE;
		IU32					bLength;
		IU32					vLength;
	} SESS_dmFetchTreeValueFn;

	struct {
		DEF_SLICE;
	} SESS_dmSetAccountValue;
#endif


/********** sess_ds_alert.c **********/

#ifdef OMADS
	struct {
		DEF_SLICE;
		UTF8Str					displayText;
	} SESS_dsHandleUIAlert;
#endif


/********** sess_ds_comms.c **********/

#ifdef OMADS
	struct {
		DEF_SLICE;
	} SESS_dsOpenComms;
#endif


/********** sess_ds_processcmds.c **********/

#ifdef OMADS
	struct {
		DEF_SLICE;
	} SESS_dsGetCmd;

	struct {
		DEF_SLICE;
		OMC_STORE_Object		object;
		SESS_MapItemPtr			mapItem;
	} SESS_dsAddCmd;

	struct {
		DEF_SLICE;
		OMC_STORE_Object		*srcObject;
		SESS_MapItemPtr			mapItem;
	} SESS_dsCopyCmd;

	struct {
		DEF_SLICE;
		OMC_STORE_Object		object;
	} SESS_dsReplaceCmd;

	struct {
		DEF_SLICE;
		OMC_Error				finalResult;
		SESS_ItemPtr			itemPtr;
		SESS_DatastorePtr		dsPtr;
		OBJ_DatastorePtr		datastore;
	} SESS_dsHandlePutCmd;

	struct {
		DEF_SLICE;
		OMC_Error				finalResult;
		SESS_ItemPtr			itemPtr;
		SESS_DatastorePtr		dsPtr;
		OBJ_DatastorePtr		datastore;
	} SESS_dsHandleResultsCmd;

	struct {
		DEF_SLICE;
		SESS_UTF8ListPtr		source;
		OBJ_SessionPtr			dss;
		OMC_STORE_Luid			luid;
		OMC_STORE_Object		object;
	} SESS_dsHandleSearchCmd;
#endif


/********** sess_ds_setup.c **********/

#ifdef OMADS
	struct {
		DEF_SLICE;
		IU32					bLength;
		IU32					vLength;
	} DSSETUP_storeValue;

	struct {
		DEF_SLICE;
		OBJ_DatastorePtr		datastore;
	} SESS_dsBuildDevinf;

	struct {
		DEF_SLICE;
		SESS_CmdPtr				cmd;
		OBJ_SessionPtr			dss;
	} SESS_dsPreparePkg1;
#endif


/********** sess_ds_sync.c **********/

#ifdef OMADS
	struct {
		DEF_SLICE;
	} SESS_dsStartSyncCmd;

	struct {
		DEF_SLICE;
	} SESS_dsEndSyncCmd;

	struct {
		DEF_SLICE;
		SESS_CmdPtr				cmd;
#ifdef SUPPORT_METINF_EMI
		OMC_DSEXT_EmiListPtr	emi;
#endif
	} DSSYNC_genSyncStart;

	struct {
		DEF_SLICE;
		OMC_STORE_Object		syncObject;
	} DSSYNC_genSyncAddOrReplace;

	struct {
		DEF_SLICE;
		OBJ_SessionPtr			dss;
		IBOOL					allObjects;
		OMC_STORE_ChangeInfo	syncChange;
		IU32					noc;
	} SESS_dsGenSync;
#endif


/********** sess_large.c **********/

	struct {
		DEF_SLICE;
	} cancelLargeObject;

	struct {
		DEF_SLICE;
	} alertNoEndOfData;

	struct {
		DEF_SLICE;
		IU8 *					ptr;
	} decodeLargeObjectData;

	struct {
		DEF_SLICE;
		IU8 *					dataPtr;
		IU32					dataLen;
		IU32					len;
	} storeLargeObjectData;

	struct {
		DEF_SLICE;
	} SESS_handleLargeObject;

	struct {
		DEF_SLICE;
	} SESS_checkLargeObject;


/********** sess_processcmds.c **********/

	struct {
		DEF_SLICE;
		OMC_Error				finalResult;
		SESS_ItemPtr			item;
	} handleDeleteCmd;

	struct {
		DEF_SLICE;
		OMC_Error				finalResult;
		SESS_ItemPtr			item;
		SESS_Meta				meta;
		SESS_Data				data;
	} handleReplaceCmd;

	struct {
		DEF_SLICE;
		OMC_Error				finalResult;
		SESS_ItemPtr			item;
		SESS_Meta				meta;
		SESS_Data				data;
	} SESS_handleAddCmd;

	struct {
		DEF_SLICE;
		OMC_Error				finalResult;
		SESS_ItemPtr			item;
		SESS_Meta				meta;
	} SESS_handleCopyCmd;

	struct {
		DEF_SLICE;
		void *					data;
		IU32					length;
	} handleExecCmd;

	struct {
		DEF_SLICE;
		OMC_Error				finalResult;
		SESS_ItemPtr			itemPtr;
		SESS_Meta				meta;
		SESS_Results			res;
	} handleGetCmd;

	struct {
		DEF_SLICE;
		SESS_CmdPtr				command;
		SESS_CmdPtr				sequenceCmd;
		SESS_CmdPtr				atomicCmd;
#ifdef OMADS
		SESS_CmdPtr				syncCmd;
#endif
		int						failFlags;
	} SESS_processMsgQ;


/********** sess_prot.c **********/

	struct {
		DEF_SLICE;
		SmlSyncHdr_t			hdr;
		SmlSource_t				source;
		SmlTarget_t				target;
		IU8*					digest;
		SESS_Meta				meta;
	} SESS_prepareMessage;


/********** sess_setup.c **********/

	struct {
		DEF_SLICE;
		int						loopCount;
		IBOOL					sentPkg1;
	} SESS_setupSession;


/********** sess_status.c **********/

	struct {
		DEF_SLICE;
	} SESS_processStatusQ;


/********** sess_wapboot.c **********/

#ifdef OMADM
#define WAPBOOT_NMAPNODES	12
	IBOOL						WAPBOOT_nodeMapFound[WAPBOOT_NMAPNODES];
	UTF8CStr					WAPBOOT_nodeMapValue[WAPBOOT_NMAPNODES];

	struct {
		DEF_SLICE;
		OMACP_Characteristic	rootNode;
	} SESS_processWAPBoot;

	struct {
		DEF_SLICE;
		IU32					length;
	} SESS_verifyBootData;

	struct {
		DEF_SLICE;
		OMC_Error				result;
		IU32					dmAccURILen;
		IU8						uri[OMC_URI_MAX_TOT_LEN + 1];
		IBOOL					initSession;
		UTF8CStr				addr;
		UTF8CStr				addrType;
		UTF8CStr				format;
		UTF8CStr				value;
		int						i;
	} SESS_addBootData;
#endif


/********** session.c **********/

	struct {
		DEF_SLICE;
	} SESS_init;

	struct {
		DEF_SLICE;
	} SESS_terminate;

	struct {
		DEF_SLICE;
	} manageSession;

	struct {
		DEF_SLICE;
		OMC_Error				result;
	} SESS_runSession;


/********** trg_bootstrap.c **********/

	struct {
		DEF_SLICE;
		IU8						bmac[SHA_DIGESTSIZE];
		IU8						key[OMC_PIN_MAX_LEN + OMC_NSS_MAX_LEN];
		IU32					keyLength;
		IU32					pinLength;
	} TRG_authBootstrap;


/********** trg_conref.c **********/

#if defined(OMADM) && defined(_WIN32_WCE) && (_WIN32_WCE >= 420)
	struct {
		DEF_SLICE;
		IU32					bLength;
		IU32					vLength;
	} CONREF_getNodeValue;

	struct {
		DEF_SLICE;
		UTF8Str					path;
		UTF8Str					pathEnd;
		IU32					i;
	} TRG_handleConDetails;
#endif


/********** trg_dm_notification.c **********/

#ifdef OMADM
	struct {
		DEF_SLICE;
		IU32					leafPathLen;
		IU8						dmAccPath[OMC_URI_MAX_TOT_LEN + 1];
	} TRG_dmGetAccountInfo;

	struct {
		DEF_SLICE;
		IU32					length;
		IU8						path[OMC_URI_MAX_TOT_LEN + 1];
	} TRG_getTreeValue;
#endif


/********** trg_ds_resume.c **********/

#ifdef OMADS
	struct {
		DEF_SLICE;
		OBJ_DatastorePtr		datastore;
		OBJ_SessionPtr			dss;
		IBOOL					workToDo;
		OMC_STORE_Anchor		anchor;
		IU32					count;
} TRG_dsResumeTrigger;
#endif


/********** trg_ds_user.c **********/

#ifdef OMADS
	struct {
		DEF_SLICE;
		TRG_SyncPtr				sync;
		OBJ_SessionPtr			dss;
	} TRG_dsUserTrigger;
#endif

/********** trg_ds_notification.c **********/

#ifdef OMADS
	struct {
		DEF_SLICE;
		OMC_Error				result;
		OMC_DSACC_Account		account;
	} TRG_dsGetAccountInfo;

	struct {
		DEF_SLICE;
		OMC_Error				result;
		IU8						*pbody;
		IU8						i;
		IU8						numSyncs;
		IU32					syncType;
		IU32					contentType;
		UTF8Str					datastoreName;
		OBJ_SessionPtr			dss;
		OBJ_DatastorePtr		ds;
	} TRG_dsParseNotificationBody;
#endif


/********** trg_info.c **********/

	struct {
		DEF_SLICE;
	} TRG_getInfo;


/********** trg_notification.c **********/

	struct {
		DEF_SLICE;
	} TRG_parseNotification;

	struct {
		DEF_SLICE;
		IU8 *					password;
		IU32					passwordLen;
		IU8 *					nonce;
		IU32					nonceLen;
	} TRG_verifyNotification;


/********** wbxml_omacp.c **********/

#ifdef OMADM
	IU8							WBXML_codePage;
#define MAX_ATTR_NAME_BYTES		18
#define MAX_ATTR_VALUE_BYTES	257

	struct
	{
		IU8						aName[MAX_ATTR_NAME_BYTES];
		IU8						aValue[MAX_ATTR_VALUE_BYTES];
	} WBXML_getAttribute;
#endif
};

/*
 * Obtain the next CmdID the client should use
 */
#define getNextCmdId(x)		((x)->cmdId)++

/*
 * Obtain the next MsgID the client should use
 */
#define getNextMsgId(x)		((x)->msgId)++

/*
 * Depending on how the Reference Toolkit is built, define what an
 * invalid InstanceID_t should be
 */
#ifdef NOWSM
#define INVALID_INSTANCEID (NULL)
#else
#define INVALID_INSTANCEID (0)
#endif


/*
 * The macros below are intended to make code that differs
 * between DM and DS neater to write. The following is an
 * example of their use:
 *
 * #ifdef OMADM
 *     IF_DM_SESSION
 *     {
 *         result = DM_action();
 *     }
 * #endif
 * #ifdef OMADS
 *     ELSE_DS_SESSION
 *     {
 *         result = DS_action();
 *     }
 * #endif
 */
#if defined(OMADM) && defined(OMADS)	/********** DM & DS **********/

extern IBOOL SESS_isDMSession(OMC_SessionDataPtr sdp);

#define IF_DM_SESSION			if ( SESS_isDMSession(sdp) )

#define ELSE_DS_SESSION			else

#define IF_DM_SESSION_AND(test)	if ( SESS_isDMSession(sdp) && (test) )

#define IF_DM_SESSION_OR(test)	if ( SESS_isDMSession(sdp) || (test) )

#define IF_DS_SESSION			if ( !SESS_isDMSession(sdp) )

#define IF_DS_SESSION_AND(test)	if ( !SESS_isDMSession(sdp) && (test) )

#define IF_DS_SESSION_OR(test)	if ( !SESS_isDMSession(sdp) || (test) )

#elif defined(OMADM)					/********** DM ONLY **********/

#define IF_DM_SESSION

/* ELSE_DS_SESSION should never be encountered! */

#define IF_DM_SESSION_AND(test)	if ( test )

#define IF_DM_SESSION_OR(test)

/* IF_DS_SESSION should never be encountered! */

/* IF_DS_SESSION_AND should never be encountered! */

#define IF_DS_SESSION_OR(test)	if ( test )

#elif defined(OMADS)					/********** DS ONLY **********/

/* IF_DM_SESSION should never be encountered! */

#define ELSE_DS_SESSION

/* IF_DM_SESSION_AND should never be encountered! */

#define IF_DM_SESSION_OR(test)	if ( test )

#define IF_DS_SESSION

#define IF_DS_SESSION_AND(test)	if ( test )

#define IF_DS_SESSION_OR(test)

#endif									/*****************************/


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_SESS_SESS_PRIVATE_H_ */
