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
 *		Name:					omc_error.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			(Original)
 *
 *		Version:				$Id: //depot/main/base/omc/omc_error.h#62 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
]*/

/*! \file
 *		Defines the error codes returned by OMC funtions.
 *
 * \brief	Error codes
 *
 * Errors are defined in groups. The top bits of an error code
 * define the group to which it belongs; the bottom bits
 * distinguish different errors of that group. (The first error
 * of each group is conventionally reserved for a generic
 * error.)
 *
 * It is possible for implementation specific errors to be added
 * to the predefined groups but it is recommended that they be
 * defined using the spare groups (starting at OMC_ERR_TYPE_IMPL).
 * The exceptions are comms and time slicing error codes where
 * it is recommended that new error codes be defined in the
 * existing groups.
 */

#ifndef _OMC_OMC_ERROR_H_
#define _OMC_OMC_ERROR_H_

/*!
 * Number of bits used in the "error subtype" field.
 */
#define OMC_ERR_SUBTYPE_BITS		8

/*!
 * Mask to get the sub-type within a group of errors.  Note that this must have
 * all bits set in the bottom of the word (e.g. the bottom 8 bits).
 */
#define OMC_ERR_SUBTYPE_MASK		((1 << OMC_ERR_SUBTYPE_BITS) - 1)

/*!
 * Mask to get the error group type.
 */
#define OMC_ERR_TYPE_MASK			(~OMC_ERR_SUBTYPE_MASK)

/*!
 * Test whether an error belongs to a particular group.
 */
#define	OMC_ERR_IS_TYPE(err,typ)	(((err) & OMC_ERR_TYPE_MASK) == (typ))


/*! \defgroup OMC_ERR_defs OMC_ERR_
 * @{
 */

/*
 * Success 'error' codes (ie. not really errors at all)
 * ---------------------
 */
#define	OMC_ERR_TYPE_SUCCESS			(0x00 << OMC_ERR_SUBTYPE_BITS)

/*! Success */
#define OMC_ERR_OK						(OMC_ERR_TYPE_SUCCESS + 0x00)

/*! Break out of a loop */
#define OMC_ERR_BREAK					(OMC_ERR_TYPE_SUCCESS + 0x01)

/*! Large object item has been handled */
#define OMC_ERR_LO_HANDLED				(OMC_ERR_TYPE_SUCCESS + 0x02)

/*! Command does not need a status response */
#define OMC_ERR_NO_STATUS_RESPONSE		(OMC_ERR_TYPE_SUCCESS + 0x03)

/*! No more commands on a queue */
#define OMC_ERR_NO_MORE_COMMANDS		(OMC_ERR_TYPE_SUCCESS + 0x04)

/*! User selected cancel */
#define OMC_ERR_CANCEL					(OMC_ERR_TYPE_SUCCESS + 0x05)


/*
 * General errors
 * --------------
 */
#define	OMC_ERR_TYPE_GENERAL			(0x01 << OMC_ERR_SUBTYPE_BITS)

/*! Unspecific error */
#define OMC_ERR_UNSPECIFIC				(OMC_ERR_TYPE_GENERAL + 0x00)

/*! Memory error */
#define OMC_ERR_MEMORY					(OMC_ERR_TYPE_GENERAL + 0x01)

/*! Supplied buffer isn't long enough */
#define OMC_ERR_BUFFER_OVERFLOW			(OMC_ERR_TYPE_GENERAL + 0x02)

/*! Bad input */
#define OMC_ERR_BAD_INPUT				(OMC_ERR_TYPE_GENERAL + 0x03)

/*! Something impossible happened */
#define OMC_ERR_INTERNAL				(OMC_ERR_TYPE_GENERAL + 0x04)

/*! Client has decided to abort */
#define OMC_ERR_ABORT					(OMC_ERR_TYPE_GENERAL + 0x05)

/*! User has requested an abort */
#define OMC_ERR_USER_ABORT				(OMC_ERR_TYPE_GENERAL + 0x06)

/*! Server has requested an abort */
#define OMC_ERR_SESSION_ABORT			(OMC_ERR_TYPE_GENERAL + 0x07)


/*! Start of sub-range for general SyncML errors */
#define OMC_ERR_GENERAL_SYNCML			(OMC_ERR_TYPE_GENERAL + 0x80)


/*
 * SyncML errors
 * -------------
 */
#define	OMC_ERR_TYPE_SYNCML				(0x02 << OMC_ERR_SUBTYPE_BITS)

/*
 * Actual error codes defined in smlerr.h
 */


/*
 * Authentication errors
 * ---------------------
 */
#define	OMC_ERR_TYPE_AUTH				(0x03 << OMC_ERR_SUBTYPE_BITS)

/*! Authentication failure */
#define OMC_ERR_AUTHENTICATION			(OMC_ERR_TYPE_AUTH + 0x00)

/*! Command failed authentication but not terminally. */
#define OMC_ERR_UNAUTHENTICATED			(OMC_ERR_TYPE_AUTH + 0x01)

/*! Server supplied the wrong type of credentials after a challenge. */
#define OMC_ERR_WRONG_CRED_TYPE			(OMC_ERR_TYPE_AUTH + 0x02)

/*! Server supplied incorrect credentials after a challenge. */
#define OMC_ERR_BAD_CREDENTIALS			(OMC_ERR_TYPE_AUTH + 0x03)

/*! Server supplied incorrect BASIC credentials. */
#define OMC_ERR_BAD_BASIC_CREDENTIALS	(OMC_ERR_TYPE_AUTH + 0x04)

/*! Server failed to supply credentials after a challenge. */
#define OMC_ERR_CRED_MISSING			(OMC_ERR_TYPE_AUTH + 0x05)

/*! Server specified an unknown HMAC algorithm. */
#define OMC_ERR_HMAC_ALGORITHM			(OMC_ERR_TYPE_AUTH + 0x06)

/*! Server failed to supply or supplied incomplete HMAC credentials. */
#define OMC_ERR_HMAC_INCOMPLETE			(OMC_ERR_TYPE_AUTH + 0x07)

/*! Server rejected the client's credentials but did not challenge. */
#define OMC_ERR_NO_CHALLENGE			(OMC_ERR_TYPE_AUTH + 0x08)

/*! Server returned unexpected result code for SyncHdr. */
#define OMC_ERR_BAD_SYNCHDR_STATUS		(OMC_ERR_TYPE_AUTH + 0x09)


/*
 * Protocol and similar errors
 * ---------------------------
 */
#define	OMC_ERR_TYPE_PROTO				(0x04 << OMC_ERR_SUBTYPE_BITS)

/*! Unspecific protocol error */
#define OMC_ERR_PROTO					(OMC_ERR_TYPE_PROTO + 0x00)

/*! The message failed to fit in the buffer (fatal)
 *  cf. \ref OMC_ERR_MESSAGE_OVERFLOW */
#define OMC_ERR_MESSAGE_TOO_LONG		(OMC_ERR_TYPE_PROTO + 0x01)

/*! The command failed to fit in the buffer (recoverable).
 *  cf. \ref OMC_ERR_MESSAGE_TOO_LONG */
#define OMC_ERR_MESSAGE_OVERFLOW		(OMC_ERR_TYPE_PROTO + 0x02)

/*! SyncML message protocol error */
#define OMC_ERR_INVALID_PROTOCOL		(OMC_ERR_TYPE_PROTO + 0x03)

/*! SyncML message protocol version error */
#define OMC_ERR_INVALID_VERSION			(OMC_ERR_TYPE_PROTO + 0x04)

/*! Data is too long to pass back as a large object */
#define OMC_ERR_TOO_BIG					(OMC_ERR_TYPE_PROTO + 0x05)

/*! Large object length does not match specified size */
#define OMC_ERR_SIZE_MISMATCH			(OMC_ERR_TYPE_PROTO + 0x06)

/*! Missing start message command */
#define OMC_ERR_MISSING_START_MESSAGE_CMD			\
										(OMC_ERR_TYPE_PROTO + 0x07)

/*! Missing end message command */
#define OMC_ERR_MISSING_STATUS_CMD		(OMC_ERR_TYPE_PROTO + 0x08)

/*! Optional feature not implemented */
#define OMC_ERR_OPTIONAL_FEATURE_NOT_IMPLEMENTED	\
										(OMC_ERR_TYPE_PROTO + 0x09)

/*! No commands in a message */
#define OMC_ERR_NO_COMMANDS				(OMC_ERR_TYPE_PROTO + 0x0A)

/*! Data element with no data */
#define OMC_ERR_NO_DATA					(OMC_ERR_TYPE_PROTO + 0x0B)

/*! Not enough items with Alert */
#define OMC_ERR_ALERT_MISSING_ITEMS		(OMC_ERR_TYPE_PROTO + 0x0C)

/*! Unknown mime type for message data */
#define OMC_ERR_MESSAGE_MIME_TYPE		(OMC_ERR_TYPE_PROTO + 0x0D)

/*! Command without devinf present when expected. */
#define OMC_ERR_DEVINF_MISSING			(OMC_ERR_TYPE_PROTO + 0x0E)

/*! Missing Source tag. */
#define OMC_ERR_SOURCE_MISSING			(OMC_ERR_TYPE_PROTO + 0x0F)

/*! Command is missing items/data/etc. */
#define OMC_ERR_MALFORMED_COMMAND		(OMC_ERR_TYPE_PROTO + 0x10)


/*
 * DM specific protocol errors
 */
#define TYPE_DM_PROTO_BASE				(OMC_ERR_TYPE_PROTO + 0x80)

/*! Alert options parsing error */
#define OMC_ERR_ALERT_PARSING_ERROR		(TYPE_DM_PROTO_BASE + 0x00)

/*! Item without any data in Alert */
#define OMC_ERR_ALERT_MISSING_DATA		(TYPE_DM_PROTO_BASE + 0x01)

/*! Too many choices passed to implementation */
#define OMC_ERR_ALERT_TOO_MANY_CHOICES	(TYPE_DM_PROTO_BASE + 0x02)


/*
 * DS specific protocol errors
 */
#define TYPE_DS_PROTO_BASE				(OMC_ERR_TYPE_PROTO + 0xC0)

/*! Missing Target tag. */
#define OMC_ERR_TARGET_MISSING			(TYPE_DS_PROTO_BASE + 0x00)

/*! Unknown meta type. */
#define OMC_ERR_BAD_META_TYPE			(TYPE_DS_PROTO_BASE + 0x01)


/*
 * Tree errors (DM only)
 *  ----------
 */
#define	OMC_ERR_TYPE_TREE				(0x05 << OMC_ERR_SUBTYPE_BITS)

/*! Tree node already exists */
#define OMC_ERR_NODE_EXISTS				(OMC_ERR_TYPE_TREE + 0x01)

/*! Tree node is missing */
#define OMC_ERR_NODE_MISSING			(OMC_ERR_TYPE_TREE + 0x02)

/*! Parent node is missing */
#define OMC_ERR_PARENT_MISSING			(OMC_ERR_TYPE_TREE + 0x03)

/*! Error in leaf node */
#define OMC_ERR_LEAF_NODE				(OMC_ERR_TYPE_TREE + 0x04)

/*! Leaf node expected */
#define OMC_ERR_NOT_LEAF_NODE			(OMC_ERR_TYPE_TREE + 0x05)

/*! Unknown property */
#define OMC_ERR_UNKNOWN_PROPERTY		(OMC_ERR_TYPE_TREE + 0x06)

/*! An attempt was made to delete a permanent node */
#define OMC_ERR_PERMANENT_NODE			(OMC_ERR_TYPE_TREE + 0x07)

/*! Not allowed by AccessType */
#define OMC_ERR_NOT_ALLOWED				(OMC_ERR_TYPE_TREE + 0x08)

/*! Bad program name passed to Tree Access API */
#define OMC_ERR_PROGRAM_NAME			(OMC_ERR_TYPE_TREE + 0x09)

/*! Partial write of external data not allowed */
#define OMC_ERR_EXT_NOT_PARTIAL			(OMC_ERR_TYPE_TREE + 0x0A)

/*! Write of external data not allowed at this time */
#define	OMC_ERR_EXT_NOT_ALLOWED			(OMC_ERR_TYPE_TREE + 0x0B)

/*! May not replace */
#define OMC_ERR_MAY_NOT_REPLACE			(OMC_ERR_TYPE_TREE + 0x0C)

/*! Tree read error */
#define OMC_ERR_TREE_READ				(OMC_ERR_TYPE_TREE + 0x0D)

/*! Tree write error */
#define OMC_ERR_TREE_WRITE				(OMC_ERR_TYPE_TREE + 0x0E)

/*! Access denied by ACL */
#define OMC_ERR_ACCESS_DENIED			(OMC_ERR_TYPE_TREE + 0x0F)

/*! External data value is not readable */
#define OMC_ERR_VALUE_NOT_READABLE		(OMC_ERR_TYPE_TREE + 0x10)

/*! External data value is not writeable */
#define OMC_ERR_VALUE_NOT_WRITEABLE		(OMC_ERR_TYPE_TREE + 0x11)

/*! Node not registered for execute */
#define OMC_ERR_NOT_EXECUTABLE			(OMC_ERR_TYPE_TREE + 0x12)

/*! Tree out of sync error */
#define OMC_ERR_TREE_SYNC				(OMC_ERR_TYPE_TREE + 0x13)


/*
 * Datastore errors (DS only)
 * ----------------
 */
#define	OMC_ERR_TYPE_DSTORE				(0x06 << OMC_ERR_SUBTYPE_BITS)

/*! Datastore name not found. */
#define OMC_ERR_UNKNOWN_DATASTORE		(OMC_ERR_TYPE_DSTORE + 0x01)

/*! No more changes in datastore objects for this server. */
#define OMC_ERR_NO_MORE_CHANGES			(OMC_ERR_TYPE_DSTORE + 0x02)

/*! No more matching datastore objects. */
#define OMC_ERR_NO_MORE_OBJECTS			(OMC_ERR_TYPE_DSTORE + 0x03)

/*! Datastore already exists. */
#define OMC_ERR_DUPLICATE_DATASTORE		(OMC_ERR_TYPE_DSTORE + 0x04)

/*! Failed to find an object in a datastore. */
#define OMC_ERR_OBJECT_NOT_FOUND		(OMC_ERR_TYPE_DSTORE + 0x05)

/*! Unknown search grammar specified */
#define OMC_ERR_UNKNOWN_GRAMMAR			(OMC_ERR_TYPE_DSTORE + 0x06)

/*! Failed to find an open datastore. */
#define OMC_ERR_DATASTORE_NOT_OPEN		(OMC_ERR_TYPE_DSTORE + 0x07)

/*! Failed to delete an object. This should be returned when not found. */
#define OMC_ERR_OBJECT_NOT_DELETED		(OMC_ERR_TYPE_DSTORE + 0x08)

/*! Object deleted rather than archived. */
#define OMC_ERR_OBJECT_NOT_ARCHIVED		(OMC_ERR_TYPE_DSTORE + 0x09)

/*! Invalid object data */
#define OMC_ERR_BAD_OBJECT				(OMC_ERR_TYPE_DSTORE + 0x0A)

/*! Mime type not supported. */
#define OMC_ERR_BAD_MIME_TYPE			(OMC_ERR_TYPE_DSTORE + 0x0B)

/*! Mime data format not supported. */
#define OMC_ERR_BAD_FORMAT				(OMC_ERR_TYPE_DSTORE + 0x0C)

/*! A sync failed because the server returned a failure code for a command */
#define OMC_ERR_SYNC_FAILED				(OMC_ERR_TYPE_DSTORE + 0x0D)


/*
 * Trigger errors
 * --------------
 */
#define	OMC_ERR_TYPE_TRIGGER			(0x07 << OMC_ERR_SUBTYPE_BITS)

/*! Notification message has invalid length */
#define OMC_ERR_NOTIF_BAD_LENGTH		(OMC_ERR_TYPE_TRIGGER + 0x01)

/*! Notification message has invalid digest */
#define OMC_ERR_NOTIF_BAD_DIGEST		(OMC_ERR_TYPE_TRIGGER + 0x02)

/*! Boot message has invalid digest */
#define OMC_ERR_BOOT_DIGEST				(OMC_ERR_TYPE_TRIGGER + 0x03)

/*! Could not get NSS for bootstrap */
#define OMC_ERR_BOOT_NSS				(OMC_ERR_TYPE_TRIGGER + 0x04)

/*! Could not get PIN for bootstrap */
#define OMC_ERR_BOOT_PIN				(OMC_ERR_TYPE_TRIGGER + 0x05)

/*! Bad bootstrap PIN length */
#define OMC_ERR_BOOT_PINLENGTH			(OMC_ERR_TYPE_TRIGGER + 0x06)

/*! Bad bootstrap SEC value */
#define OMC_ERR_BOOT_BAD_SEC			(OMC_ERR_TYPE_TRIGGER + 0x07)

/*! Bad bootstrap MAC */
#define OMC_ERR_BOOT_BAD_MAC			(OMC_ERR_TYPE_TRIGGER + 0x08)

/*! Bad bootstrap message */
#define OMC_ERR_BOOT_BAD_MESSAGE		(OMC_ERR_TYPE_TRIGGER + 0x09)

/*! Bad bootstrap profile */
#define OMC_ERR_BOOT_BAD_PROF			(OMC_ERR_TYPE_TRIGGER + 0x0A)

/*! Bad trigger reason */
#define OMC_ERR_TRG_BAD_REASON			(OMC_ERR_TYPE_TRIGGER + 0x0B)

/*! No trigger set */
#define OMC_ERR_TRG_MISSING				(OMC_ERR_TYPE_TRIGGER + 0x0C)

/*! Account (server ID) not found */
#define OMC_ERR_TRG_BAD_ACCOUNT			(OMC_ERR_TYPE_TRIGGER + 0x0D)

/*! Datastore sync request missing */
#define OMC_ERR_TRG_BAD_SYNC			(OMC_ERR_TYPE_TRIGGER + 0x0E)

/*! No suspended session to resume */
#define OMC_ERR_NO_SUSPENDED_SESSION	(OMC_ERR_TYPE_TRIGGER + 0x0F)


/*
 * FUMO errors
 * -----------
 */
#define	OMC_ERR_TYPE_FUMO				(0x08 << OMC_ERR_SUBTYPE_BITS)

/*! Error accessing SSP workspace */
#define OMC_ERR_WORKSPACE				(OMC_ERR_TYPE_FUMO + 0x01)

/*! Error accessing SSP foto */
#define OMC_ERR_FOTO					(OMC_ERR_TYPE_FUMO + 0x02)

/*! Could not initiate update client */
#define OMC_ERR_UPDATE_INIT				(OMC_ERR_TYPE_FUMO + 0x03)


/*
 * Communication errors
 * --------------------
 */
#define	OMC_ERR_TYPE_COMMS				(0x09 << OMC_ERR_SUBTYPE_BITS)

/*! General transport error */
#define OMC_ERR_COMMS					(OMC_ERR_TYPE_COMMS + 0x00)

/*
 * If using XPT then specific sub-error codes are defined in xpt.h
 */



/*
 * Parsing library errors
 * ----------------------
 */
#define	OMC_ERR_TYPE_PARSE				(0x0A << OMC_ERR_SUBTYPE_BITS)

/*! General parsing error */
#define OMC_ERR_PARSE					(OMC_ERR_TYPE_PARSE + 0x00)

/*! End of data in the buffer while parsing (unexpected) */
#define OMC_ERR_PARSE_EOF				(OMC_ERR_TYPE_PARSE + 0x01)

/*! XML tag mismatch (end not same as start) */
#define OMC_ERR_PARSE_MISMATCH			(OMC_ERR_TYPE_PARSE + 0x02)

/*! Mixed nodes and text as XML PCDATA */
#define OMC_ERR_PARSE_MIXED				(OMC_ERR_TYPE_PARSE + 0x03)

/*! Unexpected XML end tag */
#define OMC_ERR_PARSE_ENDTAG			(OMC_ERR_TYPE_PARSE + 0x04)

/*! XML tag format error (end tag with attributes, etc.) */
#define OMC_ERR_PARSE_TAGFMT			(OMC_ERR_TYPE_PARSE + 0x05)

/*! Error in text encoding */
#define OMC_ERR_PARSE_ENC				(OMC_ERR_TYPE_PARSE + 0x06)

/*! Error in object tree */
#define OMC_ERR_PARSE_TREE				(OMC_ERR_TYPE_PARSE + 0x20)

/*! Error in DTD parsing */
#define OMC_ERR_DTD_PARSE				(OMC_ERR_TYPE_PARSE + 0x40)

/*! Error in DTD attlist parsing */
#define OMC_ERR_DTD_PARSE_ATT			(OMC_ERR_TYPE_PARSE + 0x41)

/*! Error in DTD validation */
#define OMC_ERR_VALIDATE				(OMC_ERR_TYPE_PARSE + 0x50)

/*! Error in validation -- required element missing */
#define OMC_ERR_VAL_REQ_MISSING			(OMC_ERR_TYPE_PARSE + 0x51)

/*! Error in validation -- element did not match DTD */
#define OMC_ERR_VAL_UNMATCHED			(OMC_ERR_TYPE_PARSE + 0x52)

/*! Error in validation -- required content of an element missing */
#define OMC_ERR_VAL_CONTENT_MISSING		(OMC_ERR_TYPE_PARSE + 0x53)

/*! Error in validation -- content where it should be empty */
#define OMC_ERR_VAL_NON_EMPTY			(OMC_ERR_TYPE_PARSE + 0x54)



/*
 * Error types up to and including 0x7F are reserved for future Insignia error
 * classes.
 *
 * Error types from 0x80 onwards may be used by inplementations for their own
 * error values.
 */


/*
 * Insignia implementation errors
 * ------------------------------
 *
 * These are at the top of the OMC range.
 *
 * The following error codes are specific to Insignia reference
 * implementations. They should not really be defined here but
 * it is convenient to do so.
 */
#define	OMC_ERR_TYPE_IMPL				(0x7F << OMC_ERR_SUBTYPE_BITS)

/*! Text too large to display on the screen. */
#define OMC_ERR_TEXT_TOO_LARGE			(OMC_ERR_TYPE_IMPL + 0x01)

/*! Configuration open error */
#define OMC_ERR_CONFIG_OPEN				(OMC_ERR_TYPE_IMPL + 0x02)

/*! Configuration read error */
#define OMC_ERR_CONFIG_READ				(OMC_ERR_TYPE_IMPL + 0x03)

/*! Unsuppported protocol */
#define OMC_ERR_BAD_PROTOCOL			(OMC_ERR_TYPE_IMPL + 0x04)

/*! Error getting account details */
#define OMC_ERR_DSACC					(OMC_ERR_TYPE_IMPL + 0x05)

/*! Tree open error */
#define OMC_ERR_TREE_OPEN				(OMC_ERR_TYPE_IMPL + 0x06)

/*! Tree commit error */
#define OMC_ERR_TREE_COMMIT				(OMC_ERR_TYPE_IMPL + 0x07)

/*! Failed to open datastore for reading/writing. */
#define OMC_ERR_DATASTORE_OPEN			(OMC_ERR_TYPE_IMPL + 0x08)

/*! Failed to read datastore. */
#define OMC_ERR_DATASTORE_READ			(OMC_ERR_TYPE_IMPL + 0x09)

/*! Failed to write datastore. */
#define OMC_ERR_DATASTORE_WRITE			(OMC_ERR_TYPE_IMPL + 0x0A)

/*! Failed to commit newly written datastore. */
#define OMC_ERR_DATASTORE_COMMIT		(OMC_ERR_TYPE_IMPL + 0x0B)

/*! Account not found */
#define OMC_ERR_DATASTORE_ACCOUNT		(OMC_ERR_TYPE_IMPL + 0x0C)

/*! Failed to open datastore config file for reading/writing. */
#define OMC_ERR_DATASTORE_CONFIG_OPEN	(OMC_ERR_TYPE_IMPL + 0x0D)

/*! Failed to read datastore config file. */
#define OMC_ERR_DATASTORE_CONFIG_READ	(OMC_ERR_TYPE_IMPL + 0x0E)

/*! Error accessing flash */
#define OMC_ERR_FLASH					(OMC_ERR_TYPE_IMPL + 0x0F)


/*
 * Time slicing 'error' codes
 * --------------------------
 */

/*
 * These are not true error values (i.e. they don't indicate that there is a
 * fault), and are the only negative values used by the OMC_Error type. They
 * are used by the porting interface to indicate waits and yields and should
 * never appear as real errors when running OMC.
 */
#define	OMC_ERR_TYPE_SLICE				(-1 & OMC_ERR_TYPE_MASK)

/*! OMC needs to run again soon */
#define OMC_ERR_YIELD					(-1)

/*! OMC needs to run again once an external event has occurred */
#define OMC_ERR_WAIT					(-2)

/* @} */


/*
 * ALL negative error codes are reserved for use by time slicing code.
 * (This test is used a lot so it is worth making it cheaper than
 * testing the type using OMC_ERR_IS_TYPE.)
 */
#define IS_SLICE_REQUESTED(ERROR)	(ERROR < 0)

#endif /* !_OMC_OMC_ERROR_H_ */
