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
 * Fast floating point maths functions
 *
 */
#pragma once
#ifndef A3M_FPMATHS_H
#define A3M_FPMATHS_H

/******************************************************************************
* Include Files
******************************************************************************/
#include <a3m/base_types.h>             /* included for A3M_FLOAT */

namespace a3m
{

  /** \defgroup  a3mMathsConst Constants
   *  \ingroup   a3mRefMaths
   *  pi, 2.pi, pi/2, float-epsilon, etc.
   *  @{
   */

  /** Pi */
  const A3M_FLOAT FLOAT_PI = 3.1415926535897932f;

  /** Two times Pi */
  const A3M_FLOAT FLOAT_TWO_PI = (2.f * FLOAT_PI);

  /** Pi over two */
  const A3M_FLOAT FLOAT_HALF_PI = (FLOAT_PI / 2.f);

  /** Smallest such that 1.0+FLT_EPSILON != 1.0 */
  const A3M_FLOAT A3M_FLOAT_EPSILON = 1.192092896e-07f;

  /** @} */




  /** \defgroup  a3mFastFloat Fast floating point maths functions
   *  \ingroup   a3mRefMaths
   * These are designed to be a faster alternative to those in math.h.
   * They may be less accurate, but should be accurate
   * enough for 3D graphics applications.
   *  @{
   */

/******************************************************************************
 * Square Root Functions
 ******************************************************************************/

  /**
  * Calculate square root
  * \return square root of given number
  */
  A3M_FLOAT sqrt( A3M_FLOAT x  /**< number */ );

  /**
  * Calculate inverse square root (1/sqrt(x))
  * \return inverse square root of given number
  */
  A3M_FLOAT invSqrt( A3M_FLOAT x  /**< number */ );

/******************************************************************************
 * Absolute function
 ******************************************************************************/

  /**
   * Returns absolute value of the number of type < T >.
   *
   * \tparam T A3M_INT32, A3M_INT64, A3M_FLOAT etc.
   * \return Absolute value of given number
   */
  template < typename T >
  T abs( T x /**< number of type T */ )
  {
    return ( ( x < T(0) ) ? -x : x );
  }

/******************************************************************************
 * Clamp
 ******************************************************************************/

  /** Clamp the value to the given range.
      \return a value between min and max.
      */
  template < typename T >
  T clamp(
    T value /**< the input value */,
    T min   /**< the minimum value */,
    T max   /**< the maximum value */)
  {
    if (value <= min)
    {
      return min;
    }

    if (max <= value)
    {
      return max;
    }

    return value;
  }

/******************************************************************************
 * Miscellaneous
 ******************************************************************************/
  /** Compute the floating-point remainder of numerator/denominator.
      \return the result of subtracting the integral quotient
              multiplied by the denominator from the numerator. */
  A3M_FLOAT fmod(
    A3M_FLOAT numerator   /**< the division numerator */,
    A3M_FLOAT denominator /**< the division denominator */);

  /** @} */
} /* namespace a3m */

#endif /* A3M_FPMATHS_H */
