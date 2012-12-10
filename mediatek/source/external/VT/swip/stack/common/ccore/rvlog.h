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

/***********************************************************************
Filename   : rvlog.h
Description: log handling
************************************************************************
        Copyright (c) 2001 RADVISION Inc. and RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Inc. and RADVISION Ltd.. No part of this document may be
reproduced in any form whatsoever without written prior approval by
RADVISION Inc. or RADVISION Ltd..

RADVISION Inc. and RADVISION Ltd. reserve the right to revise this
publication and make changes without obligation to notify any person of
such revisions or changes.
***********************************************************************/

#ifndef RV_LOG_H
#define RV_LOG_H

#include "rvtypes.h"
#include "rverror.h"
#include "rvadlock.h"
#include "VT_trc.h"


#if defined(__cplusplus)
extern "C" {
#endif


/* Module specific error codes (-512..-1023). See rverror.h for more details */
#define RV_LOG_ERROR_RECURSION -512 /* Logging an error might cause an endless recursion */
#define RV_LOG_ERROR_ALREADY_REGISTERED -513 /* Listener is already registered to this log object */

/* Log message id's: in opposite to log level values these are index numbers */
#define RV_LOGID_EXCEP      0
#define RV_LOGID_ERROR      1
#define RV_LOGID_WARNING    2
#define RV_LOGID_INFO       3
#define RV_LOGID_DEBUG      4
#define RV_LOGID_ENTER      5
#define RV_LOGID_LEAVE      6
#define RV_LOGID_SYNC       7


/* Log manager object */
typedef struct RvLogMgrInternal RvLogMgr;


/* Source of log messages */
typedef struct RvLogSourceInternal* RvLogSource;


/* Type of message being logged */
typedef RvUint8 RvLogMessageType;


/* Log information associated with a log message. This struct is given
   to the printing function on each log message. */
typedef struct RvLogRecordInternal RvLogRecord;


/********************************************************************************************
 * RvLogPrintCb - Callback that is executed whenever a message has to be logged somewhere
 *
 * The log record holds a pointer to the message to log. This message has
 * a set amount of bytes before it allocated but unused to allow better
 * formatting of messages. That amount is RV_LOG_RESERVED_BYTES.
 *
 * INPUT   : logRecord  - Information related with the logged message
 *           userData   - User related data, given when the printing function was set
 * OUTPUT  : None
 * RETURN  : None
 */
typedef void
    (RVCALLCONV* RvLogPrintCb)(
        IN RvLogRecord* logRecord,
        IN void*        userData);


/* Maximum number of listeners for a logging object */
#define RV_LOG_MAX_LISTENERS RvInt32Const(10)

/* Amount of reserved bytes before message when inside the log callback */
#define RV_LOG_RESERVED_BYTES RvInt32Const(139)

/* Maximum size of each message, without the reserved bytes */
#define RV_LOG_MESSAGE_SIZE RvInt32Const(1024)

/* Maximum number of message types */
#define RV_LOG_NUM_MESSAGE_TYPES 8


#include "rvloginternal.h"



/* Prototypes and macros */

RvStatus RvLogInit(void);
RvStatus RvLogEnd(void);


/********************************************************************************************
 * RvLogConstruct
 *
 * Create a log object. Only a single such object is used by the core and the
 * stacks on top of it.
 * 
 * INPUT   : logMgr - Log manager to construct
 * OUTPUT  : None
 * RETURN  : RV_OK on success, other on failure
 */
RVCOREAPI
RvStatus RVCALLCONV RvLogConstruct(IN RvLogMgr* logMgr);


/********************************************************************************************
 * RvLogDestruct - Kill a log object
 *
 * INPUT   : logMgr - Log manager to destruct
 * OUTPUT  : None
 * RETURN  : RV_OK on success, other on failure
 */
RVCOREAPI
RvStatus RVCALLCONV RvLogDestruct(IN RvLogMgr* logMgr);


/********************************************************************************************
 * RvLogRegisterListener - Set a listener function to the log.
 *
 * Multiple listeners can be set on each log
 * object. The listener is used to actually log the messages.
 *
 * INPUT   : logMgr     - Log manager to use
 *           listener   - Listener function, called on each message
 *           userData   - User data set as a parameter of the listener function
 * OUTPUT  : None
 * RETURN  : RV_OK on success, other on failure
 */
RVCOREAPI
RvStatus RVCALLCONV RvLogRegisterListener(
    IN RvLogMgr*    logMgr,
    IN RvLogPrintCb listener,
    IN void*        userData);


/********************************************************************************************
 * RvLogUnregisterListener - Unset a listener function from the log.
 *
 * INPUT   : logMgr     - Log manager to use
 *           listener   - Listener function, called on each message
 * OUTPUT  : None
 * RETURN  : RV_OK on success, other on failure
 */
RVCOREAPI
RvStatus RVCALLCONV RvLogUnregisterListener(
    IN RvLogMgr*    logMgr,
    IN RvLogPrintCb listener);


/********************************************************************************************
 * RvLogSourceConstruct - Create a new source of messages in a log manager
 *
 * INPUT   : logMgr     - Log manager
 *           source     - Source of messages to create
 *           name       - Name of the source
 *           description- Description of the source
 * OUTPUT  : None
 * RETURN  : RV_OK on success, other on failure
 */
RVCOREAPI
RvStatus RVCALLCONV RvLogSourceConstruct(
    IN RvLogMgr*        logMgr,
    IN RvLogSource*     source,
    IN const RvChar*    name,
    IN const RvChar*    description);


/********************************************************************************************
 * RvLogSourceDestruct - Delete a source of messages from a log manager
 *
 * INPUT   : source     - Source of messages to delete
 * OUTPUT  : None
 * RETURN  : RV_OK on success, other on failure
 */
RVCOREAPI
RvStatus RVCALLCONV RvLogSourceDestruct(
    IN RvLogSource*     source);


/********************************************************************************************
 * RvLogGetSourceByName
 *
 * purpose : Get the source for a specific log source name
 * input   : logMgr     - Log manager
 *           name       - Name of the source to find
 * output  : source     - Source found on success
 * return  : RV_OK on success, other on failure
 ********************************************************************************************/
RVCOREAPI RvStatus RVCALLCONV RvLogGetSourceByName(
    IN  RvLogMgr*       logMgr,
    IN  const RvChar*   name,
    OUT RvLogSource*    source);


/********************************************************************************************
 * RvLogIsSelected
 *
 * Check to see if a specific message type should be sent to the log by a given source
 *
 * INPUT   : source         - Source of message to log
 *           messageType    - Type of the message to log
 * OUTPUT  : None
 * RETURN  : RV_TRUE if message should be logged, RV_FALSE otherwise
 */
RVCOREAPI
RvBool RVCALLCONV RvLogIsSelected(
    IN RvLogSource*     source,
    IN RvLogMessageType messageType);


/********************************************************************************************
 * RvLogSetLevel
 *
 * Set the level of logging, while leaving the masks of all log sources without a change.
 * 
 * INPUT   : logMgr - Log manager
 *           level  - 0 stop logging, 1 log by the masks of the sources, 2 log everything
 * OUTPUT  : None
 * RETURN  : RV_OK on success, other on failure
 */
RVCOREAPI
RvStatus RVCALLCONV RvLogSetLevel(
    IN RvLogMgr*    logMgr,
    IN RvInt32      level);


/********************************************************************************************
 * RvLogSetGlobalMask
 *
 * Set the mask of messages to log on all the sources of the log object
 *
 * INPUT   : logMgr         - Log manager
 *           messageMask    - Type of the messages to log
 * OUTPUT  : None
 * RETURN  : RV_OK on success, other on failure
 */
RVCOREAPI
RvStatus RVCALLCONV RvLogSetGlobalMask(
    IN RvLogMgr*        logMgr,
    IN RvLogMessageType messageMask);


/********************************************************************************************
 * RvLogSourceSetMask
 *
 * Set the mask of messages to log for a specific source
 *
 * INPUT   : source         - Source of messages to set
 *           messageMask    - Type of the messages to log
 * OUTPUT  : None
 * RETURN  : RV_OK on success, other on failure
 */
RVCOREAPI
RvStatus RVCALLCONV RvLogSourceSetMask(
    IN RvLogSource*     source,
    IN RvLogMessageType messageMask);


/********************************************************************************************
 * RvLogSourceGetMask - Get the mask of messages to log for a specific source
 *
 * INPUT   : source         - Source of messages to get
 * OUTPUT  : None
 * RETURN  : Message mask of messages that are logged
 */
RVCOREAPI
RvLogMessageType RVCALLCONV RvLogSourceGetMask(
    IN RvLogSource*     source);


/********************************************************************************************
 * RvLogSourceGetName - Get the name for a specific log source
 *
 * INPUT   : source         - Source of messages to get
 * OUTPUT  : None
 * RETURN  : Name of the source on success, NULL on failure
 */
RVCOREAPI const RvChar* RVCALLCONV RvLogSourceGetName(
    IN RvLogSource*     source);


/********************************************************************************************
 * RvLogXXX
 *
 * Log a text message with variable amount of arguments, to the relevant
 * logging level.These macros should be used and not RvLogTextXXX functions!
 * example - RvLogDebug((RvLogSource*)source, ((RvLogSource*)source, "Example %d", i));
 *
 * INPUT   : source     - Source of message to log
 *           line       - Formatted string to log
 * OUTPUT  : None
 * RETURN  : None
 */

#define RvLogExcep( funcParams) kal_brief_trace  funcParams
#define RvLogError( funcParams) kal_brief_trace funcParams
#define RvLogWarning( funcParams) kal_brief_trace funcParams
#define RvLogInfo( funcParams) kal_brief_trace funcParams
#define RvLogDebug( funcParams) kal_brief_trace funcParams
#define RvLogEnter( funcParams) kal_brief_trace funcParams
#define RvLogEnter2( funcParams) kal_trace funcParams
#define RvLogLeave( funcParams) kal_brief_trace funcParams
#define RvLogLeave2( funcParams) kal_trace funcParams
#define RvLogSync( funcParams) kal_brief_trace funcParams
#define RvLogState( funcParams) kal_brief_trace funcParams 

RvStatus RVCALLCONV RvLogSysTextAny(
    IN const RvChar*    line,
    ...);

#ifdef __MTK_VT_DEBUG_ALL__
#define RvLogPrintStr( funcParams) RvLogSysTextAny funcParams
#define RV_LOG_USED_VAR(var)
#else
#define RvLogPrintStr( funcParams)
#define RV_LOG_USED_VAR(var) RV_UNUSED_ARG(var)
//#define RvLogPrintStr( funcParams) ((void)(funcParams))
#endif


/********************************************************************************************
 * RvLogRecordGetXXX - Retrieves specific information from a log record. 
 *
 * This set of functions should be used inside the listener functions when messages have 
 * to be logged.
 *
 * INPUT   : logRecord  - Log record to check
 * OUTPUT  : None
 * RETURN  : Desired field
 */
#define RvLogRecordGetThread(logRecord) ((logRecord)->threadName)
#define RvLogRecordGetSource(logRecord) ((logRecord)->source)
#define RvLogRecordGetMessageType(logRecord) ((logRecord)->messageType)
#define RvLogRecordGetText(logRecord) ((logRecord)->text)


/********************************************************************************************
 * RvLogSourcePrintInterfacesData - Prints information of the interfaces data to the log.
 *
 * The interfaces data string should be given from the function RvCCoreInterfaces().
 *
 * INPUT   : source             - Source of messages to log to
 *           interfacesString   - Strings to log, as given by RvCCoreInterfaces()
 * OUTPUT  : None
 * RETURN  : None
 */
#if (RV_LOGMASK & RV_LOGLEVEL_INFO)
RVCOREAPI void RVCALLCONV RvLogSourcePrintInterfacesData(
    IN RvLogSource*     source,
    IN const RvChar*    interfacesString);
#else
#define RvLogSourcePrintInterfacesData(_source, _interfacesString)
#endif

/********************************************************************************************
 * RvLogGetMessageCount - Returns the number of messages of a specified type that
 *                        sent to log.
 *
 * INPUT   : logMgr         - The log manager
 *           messageType    - The required message type
 * OUTPUT  : None
 * RETURN  : None
 */
RVCOREAPI RvInt RVCALLCONV RvLogGetMessageCount(
    IN RvLogMgr*        logMgr,
    IN RvInt            messageType);

/********************************************************************************************
 * RvLogGetMessageCount - Configures the log manager to print the thread id instead
 *                        of the thread name.
 *
 * INPUT   : logMgr     - The log manager
 * OUTPUT  : None
 * RETURN  : None
 */
RVCOREAPI void RVCALLCONV RvLogPrintThreadId(
    IN RvLogMgr*        logMgr);


/* The checks below removes functions and macros by the specified log mask we're using.
   This allows us to compile the code with partial logging (for example, only compile with
   error messages being able to log) */

   #if (RV_LOGMASK & RV_LOGLEVEL_INFO)
RVCOREAPI
RvStatus RVCALLCONV RvLogTextInfo(
    IN  RvLogSource*        source,
    IN  const char*         line, ...);
RVCOREAPI
RvStatus RVCALLCONV RvLogTextDebug(
    IN  RvLogSource*        source,
    IN  const char*         line, ...);
RVCOREAPI
RvStatus RVCALLCONV RvLogTextEnter(
    IN  RvLogSource*        source,
    IN  const char*         line, ...);
RVCOREAPI
RvStatus RVCALLCONV RvLogTextLeave(
    IN  RvLogSource*        source,
    IN  const char*         line, ...);    
#endif  /* (RV_LOGMASK & RV_LOGLEVEL_DEBUG) */


#if defined(__cplusplus)
}
#endif

#endif /* RV_LOG_H */

