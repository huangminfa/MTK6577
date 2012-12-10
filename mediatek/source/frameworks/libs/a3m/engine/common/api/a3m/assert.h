/**************************************************************************
 *
 * Copyright (c) 2010 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 ***************************************************************************/
/** \file
 * Assert Macros
 */

#pragma once
#ifndef A3M_ASSERT_H
#define A3M_ASSERT_H

/******************************************************************************
 * Include files
 ******************************************************************************/
#ifdef PLACEHOLDER_ONLY_TODO
/* TBD */
#else
#include <assert.h> /* for assert() */
#include <a3m/log.h>
#endif

/******************************************************************************
 * Macros
 ******************************************************************************/
/**
 * \defgroup a3mAssert Asserts
 * \ingroup  a3mInt
 * Use to prevent programming violations and non-sensical compilations.
 * @{
 */

/** Assert macro.  If the expression resolves to FALSE the system needs to take
 * drastic action.  The nature of the action depends on the platform and the
 * type of build.  In a debug build this action will be visible, and the system
 * may terminate aggressively.
 *
 * \warning If the build is a 'production' one the ASSERT call is transparent
 * and program flow will not be affected.
 * Asserts \b must \b only be used to trap programming errors.
 * Real-world error cases must be handled with recovery code.
 *
 * \todo Replace the copied 'kal' assert example with a proper PSS one
 * \todo Choose switches for debug vs. production and use them to remove the assert
 */
#ifdef PLACEHOLDER_ONLY_TODO

#define A3M_ASSERT(expr) {if(!(expr)) { kal_assert_fail((kal_char *)#expr,    \
   (kal_char *)__FILE__, __LINE__, KAL_FALSE, 0, 0, 0, NULL); }else {}}

#else /* fall-back default */

#define A3M_ASSERT(expr) { if ( !(expr) ) pssLogError( __FILE__, "", __LINE__, "A3M_ASSERT: " #expr ); }

#endif

/* @} */


/* Keep these details out of the global namespace and Doxygen */
/** \cond */
namespace a3m_assert_detail
{
  // Create a template structure called 'tAssert' that contains NO members
  template< bool B >
  struct CompileAssert
  {
  };

  // Specialization of the above template, with a single member: 'assert'
  template<>
  struct CompileAssert< true >
  {
    static void assert_func() {};
  };
} /* namespace a3m_assert_detail */
/** \endcond */

/**
 * \ingroup  a3mAssert
 *
 * Compile time assert macro. If the expression resolves to FALSE the code will
 * fail to compile. The expression must be a compile time constant. This can be
 * used to prevent templates compiling with the wrong kind of type or to test
 * assumptions about built-in types.
 * Examples:
 * \code
 *  template< typename T >
 *  A3M_UINT32 read( T &dest )
 *  {
 *    // Check that T is a built in arithmetic type
 *    A3M_COMPILE_ASSERT( std::numeric_limits< T >::is_specialized )
 *    return read( (void *)&dest, sizeof( T ) );
 *  }
 *
 *  A3M_COMPILE_ASSERT( sizeof( int ) == 4 )
 * \endcode
 * \todo This cannot be used at file scope. An alternative (more complicated)
 * version declaring a struct might be better.
 */

#define A3M_COMPILE_ASSERT( expr ) { a3m_assert_detail::CompileAssert< (bool)(expr) >::assert_func(); }

#endif /* A3M_ASSERT_H */

