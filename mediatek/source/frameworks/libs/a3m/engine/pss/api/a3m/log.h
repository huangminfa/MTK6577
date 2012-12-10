/*****************************************************************************
 *
 * Copyright (c) 2010 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 *****************************************************************************/
/** \file
 * Logging facility for the A3M middleware.
 */

#pragma once
#ifndef PSS_LOG_H
#define PSS_LOG_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/base_types.h>                     /* for A3M_INT32 etc.        */

/** \defgroup a3mPssLogApi PSS Logging Interface
 *  \ingroup a3mPss
 *  This section defines the PSS Logging Interface.
 *  This is a unified logging facility designed to be used by the whole
 *  A3M middleware framework.
 *  Several LoggingTypes "logging types or levels" are defined to allow
 *  some classification of the logging messages.
 *
 * @{
 */

/*****************************************************************************
 * Macro Definitions
 *****************************************************************************/

/**
 * \copybrief pssLogError()
 * \copydetails pssLogError()
 */
#define A3M_LOG_ERROR(format, ...) \
  pssLogError(__FILE__, __FUNCTION__, __LINE__, format, ##__VA_ARGS__)

/**
 * \copybrief pssLogWarn()
 * \copydetails pssLogWarn()
 */
#define A3M_LOG_WARN(format, ...) \
  pssLogWarn(__FILE__, __FUNCTION__, __LINE__, format, ##__VA_ARGS__)

/**
 * \copybrief pssLogInfo()
 * \copydetails pssLogInfo()
 */
#define A3M_LOG_INFO(format, ...) \
  pssLogInfo(__FILE__, __FUNCTION__, __LINE__, format, ##__VA_ARGS__)

/**
 * \copybrief pssLogDebug()
 * \copydetails pssLogDebug()
 */
#define A3M_LOG_DEBUG(format, ...) \
  pssLogDebug(__FILE__, __FUNCTION__, __LINE__, format, ##__VA_ARGS__)


/*****************************************************************************
 * Type Definitions
 *****************************************************************************/
/**
 * Define a synonym for a char pointer type.
 */
typedef A3M_INT32 (*PfPrint) (const A3M_CHAR8* pszString);

/**
 * Define a synonym for a void pointer type.
 */
typedef void (*PfFlush) (void);

/*****************************************************************************
 * Typed Constants
 *****************************************************************************/
/*
 * Several logging types are defined to allow some classification of the
 * logging messages
 */

/**
 *  Log message types / severities.
 */
typedef enum pssLogTypeTag
{
  PSS_LOG_ERROR, /**< Errors are events that \e must not happen. */
  PSS_LOG_WARN,  /**< Warnings are events that are unexpected but non-fatal. */
  PSS_LOG_INFO,  /**< Expected events that are worth logging. */
  PSS_LOG_DEBUG, /**< TEMPORARY debug ONLY - should normally be absent. */

} pssLogType;

/*****************************************************************************
 * Global Functions
 *****************************************************************************/
/**
 * Initialise the a3m logging interface.
 * \return none
 */
void pssLogInit();

/**
 * Deinitialise the a3m logging interface.
 * \return none
 */
void pssLogDeInit();

/**
 * Error log messages to be used to log critical errors using a format string
 * and a variable number of format arguments.
 * \return none
 */
void pssLogError(const A3M_CHAR8 *pszModule,
                 /**< File (module) name which emitted the message. */
                 const A3M_CHAR8 *pszFunction,
                 /**< Name of the function which emitted the message. */
                 A3M_INT32 line,
                 /**< Line number in the module which emitted the message. */
                 const A3M_CHAR8 *pszFormat,
                 /**< Format string for the message. */
                 ...
                 /**< Variable list of format arguments. */);

/** Log a warning message using a format string and a variable number of
 *  format arguments.
 * \return none
 */
void pssLogWarn(const A3M_CHAR8 *pszModule,
                 /**< File (module) name which emitted the message. */
                 const A3M_CHAR8 *pszFunction,
                 /**< Name of the function which emitted the message. */
                 A3M_INT32 line,
                 /**< Line number in the module which emitted the message. */
                 const A3M_CHAR8 *pszFormat,
                 /**< Format string for the message. */
                 ...
                 /**< Variable list of format arguments. */);

/** Log an information message using a format string and a variable number of
 * format arguments.
 * \return none
 */
void pssLogInfo (const A3M_CHAR8 *pszModule,
                 /**< File (module) name which emitted the message. */
                 const A3M_CHAR8 *pszFunction,
                 /**< Name of the function which emitted the message. */
                 A3M_INT32 line,
                 /**< Line number in the module which emitted the message. */
                 const A3M_CHAR8 *pszFormat,
                 /**< Format string for the message. */
                 ...
                 /**< Variable list of format arguments. */);

/** Log a "debug" message using a format string and a variable number of
 *  format arguments.
 * \return none
 */
void pssLogDebug (const A3M_CHAR8 *pszModule,
                 /**< File (module) name which emitted the message. */
                 const A3M_CHAR8 *pszFunction,
                 /**< Name of the function which emitted the message. */
                 A3M_INT32 line,
                 /**< Line number in the module which emitted the message. */
                 const A3M_CHAR8 *pszFormat,
                 /**< Format string for the message. */
                 ...
                 /**< Variable list of format arguments. */);


/** @} */

#endif
