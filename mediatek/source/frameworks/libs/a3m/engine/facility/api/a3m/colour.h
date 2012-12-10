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
 * Colour type
 *
 */
#pragma once
#ifndef A3M_COLOUR_H
#define A3M_COLOUR_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/base_types.h>               /* A3M base type defines           */

namespace a3m
{
  /** \defgroup a3mTypeCol Colour (RGBA) type and constants
   * \ingroup  a3mRefTypes
   * Type to hold colour values as R,G,B and Alpha quartets. At present
   * only the Float implementation exists.
   *
   * @{
   */

  /**
   * Colour4f structure.
   * Stores RGBA colour values as A3M_FLOAT's.  No
   * attempt is made to clamp values to [0.0 1.0].
   */
  struct Colour4f
  {
    A3M_FLOAT r;  /**< Red colour channel */
    A3M_FLOAT g;  /**< Green colour channel */
    A3M_FLOAT b;  /**< Blue colour channel */
    A3M_FLOAT a;  /**< Alpha colour channel */

    /** Red colour constant. */
    static const Colour4f RED;

    /** Green colour constant. */
    static const Colour4f GREEN;

    /** Blue colour constant. */
    static const Colour4f BLUE;

    /** White colour constant. */
    static const Colour4f WHITE;

    /** Black colour constant (alpha = 1). */
    static const Colour4f BLACK;

    /** 33% grey colour constant. */
    static const Colour4f LIGHT_GREY;

    /** 67% grey colour constant. */
    static const Colour4f DARK_GREY;

    /** Default constructor */
    Colour4f() : r(0.0), g(0.0), b(0.0), a(1.0) {}

    /**
     * Constructor: initialises local member variables
     * with the supplied values
     */
    Colour4f(A3M_FLOAT r /**< Red coefficient 0..1 */,
             A3M_FLOAT g /**< Green coefficient 0..1 */,
             A3M_FLOAT b /**< Blue coefficient 0..1 */,
             A3M_FLOAT a /**< Alpha coefficient 0..1 */);

    /** Set Colour
     *
     * Sets colour with byte vales
     */
    void setColour( A3M_UINT8 rbyte /**< red channel */,
                    A3M_UINT8 gbyte /**< green channel */,
                    A3M_UINT8 bbyte /**< blue channel */,
                    A3M_UINT8 abyte /**< alpha channel */ );

    /** Scale
     *
     * Scales the float values of the colour components
     */
    void scale(A3M_FLOAT scalar /**< amount to scale colour */);

    /** Equals operator for Colour4f objects.
     * \return TRUE if the objects are the same.
     */
    A3M_BOOL operator==( Colour4f const &c  /**< other colour */) const
    {
      return ( r == c.r ) && ( g == c.g ) && ( b == c.b ) && ( a == c.a );
    }

    /** Not-Equals operator for Colour4f objects.
     * \return TRUE if the objects are different.
     */
    A3M_BOOL operator!=( Colour4f const &c  /**< other colour */) const
    {
      return !( *this == c );
    }
  };

  /** Component-wise addition for Colour4f.
   */
  Colour4f operator+( Colour4f const& lhs, /**< left-hand operand */
                      Colour4f const& rhs  /**< right-hand operand */ );

  /** Component-wise subtract for Colour4f.
   */
  Colour4f operator-( Colour4f const& lhs, /**< left-hand operand */
                      Colour4f const& rhs  /**< right-hand operand */ );

  /** Component-wise multiply for Colour4f.
   */
  Colour4f operator*( Colour4f const& lhs, /**< left-hand operand */
                      Colour4f const& rhs  /**< right-hand operand */ );

  /** Component-wise divide for Colour4f.
   */
  Colour4f operator/( Colour4f const& lhs, /**< left-hand operand */
                      Colour4f const& rhs  /**< right-hand operand */ );
  /** @} */

}; /* namespace a3m */


#endif /* A3M_COLOUR_H */
