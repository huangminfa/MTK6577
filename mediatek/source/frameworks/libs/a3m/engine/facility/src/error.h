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
 * Error checking and reporting
 *
 */

#pragma once
#ifndef A3M_ERROR_H
#define A3M_ERROR_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/base_types.h> /* for A3M_CHAR8 & A3M_INT32 */

/*****************************************************************************
 * Macro Definitions
 *****************************************************************************/
/** Calls the function a3m::checkGLError with the correct filename, function
 * and line.
 */
#if defined (ENABLE_OGL_ERROR_CHECK)
#define CHECK_GL_ERROR a3m::checkGLError( __FILE__, __FUNCTION__, __LINE__ )
#else
#define CHECK_GL_ERROR
#endif

/*****************************************************************************
 * Global Functions
 *****************************************************************************/
namespace a3m
{
  /** Check the OpenGL error status and log an error if one has occured.
   * Use the macro CHECK_GL_ERROR to call this function with the correct
   * filename, function and line.
   */
  void checkGLError( const A3M_CHAR8 *pszModule,
                     /**< [in] File name of caller. */
                     const A3M_CHAR8 *pszFunction,
                     /**< [in] Function name caller. */
                     A3M_INT32 line
                     /**< [in] Line number of call. */ );

} /* namespace a3m */

#endif /* A3M_ERROR_H */
