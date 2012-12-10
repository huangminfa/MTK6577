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
 * 2D Matrix maths functions
 */
#pragma once
#ifndef A3MATH_MATRIX2_H
#define A3MATH_MATRIX2_H

#include <a3m/fpmaths.h> /* ensure fast sin, cos, sqrt are used */

#include <a3m/angle.h> /* for Angle type */
#include <a3m/vector2.h> /* for Vector2 type */

namespace a3m
{
  /** \defgroup a3mMatrix2 2x2 Matrices
   *  \ingroup  a3mRefMaths
   *
   *  2x2 matrix support, x-y by i-j
   *  @{
   */

  /** Class representing a 2 by 2 matrix.
   * The class stores each column of the matrix in a 2d vector.
   */
  template< typename T >
  class Matrix2
  {
  public:
    /** Scalar type */
    typedef T Scalar;
    /** Vector type used for columns */
    typedef Vector2< T > Column;

    /** Identity matrix */
    static const Matrix2<T> IDENTITY;

    /** Default constructor. Sets this matrix to the identity.*/
    Matrix2();
    /** Constructor initialising columns. */
    Matrix2( Column const &i, /**< initialiser for column 0*/
             Column const &j  /**< initialiser for column 1*/ );

    /** Index operator. Returns a reference to a matrix column.
    * \return reference to a column of the matrix
    */
    Column &operator[]( A3M_INT32 index /**< index of column in the
                                             range [0,1] */);
    /** Index operator. Returns a constant reference to a matrix column.
    * \return constant reference to a column of the matrix
    */
    Column const &operator[]( A3M_INT32 index /**< index of column in the
                                                   range [0,1] */) const;

    /** First column (column 0) of matrix */
    Column i;
    /** Second column (column 1) of matrix */
    Column j;
  };

  /** Specialisation for float */
  typedef Matrix2< A3M_FLOAT > Matrix2f;

  /** Multiplication operator matrix * vector.
   * \return transformed vector
   */
  template< typename T >
  Vector2<T> operator*( Matrix2<T> const &a, /**< matrix */
                        Vector2<T> const &b  /**< vector */ );

  /** Multiplication operator
   * \return matrix product
   */
  template< typename T >
  Matrix2<T> operator*( Matrix2<T> const &a, /**< left-hand operand */
                        Matrix2<T> const &b  /**< right-hand operand */);

  /** Equals operator
   * \return TRUE if a == b
   */
  template< typename T >
  A3M_BOOL operator==( Matrix2<T> const &a, /**< left-hand operand */
                       Matrix2<T> const &b  /**< right-hand operand */);

  /** Not-equal operator
   * \return TRUE if a != b
   */
  template< typename T >
  A3M_BOOL operator!=( Matrix2<T> const &a, /**< left-hand operand */
                       Matrix2<T> const &b  /**< right-hand operand */);

  /** Transpose matrix
   * \return transposed matrix
   */
  template< typename T >
  Matrix2<T> transpose( Matrix2<T> const &m /**< matrix */ );

  /** Invert matrix
   * \return inverse of matrix
   */
  template< typename T >
  Matrix2<T> inverse( Matrix2<T> const &m /**< matrix */ );

  /** Rotation matrix
   * \return matrix representing a rotation
   */
  template< typename T >
  Matrix2<T> rotation( Angle<T> const &angle /**< angle */ );

  /** Scaling matrix
   * \return matrix representing a scale operation
   */
  template< typename T >
  Matrix2<T> scale( T xScale, /**< X scaling factor */
                    T yScale  /**< Y scaling factor */);



/******************************************************************************
 * Implementation
 ******************************************************************************/

  template< typename T >
  const Matrix2<T> Matrix2<T>::IDENTITY = Matrix2<T>();

  template< typename T >
  Matrix2<T>::Matrix2()
  : i(T(1), T(0)), j(T(0),T(1))
  {
  }

  template< typename T >
  Matrix2<T>::Matrix2( Column const &i, Column const &j )
  : i(i), j(j)
  {
  }

  template< typename T >
  typename Matrix2<T>::Column &Matrix2<T>::operator[]( A3M_INT32 index )
  {
    return *( &i + index );
  }

  template< typename T >
  typename Matrix2<T>::Column const
    &Matrix2<T>::operator[]( A3M_INT32 index ) const
  {
    return *( &i + index );
  }

  template< typename T >
  Vector2<T> operator*( Matrix2<T> const &a, Vector2<T> const &b )
  {
    return a.i * b.x + a.j * b.y;
  }

  template< typename T >
  Matrix2<T> operator*( Matrix2<T> const &a, Matrix2<T> const & b )
  {
    return Matrix2<T>( a * b.i, a * b.j );
  }

  template< typename T >
  A3M_BOOL operator==( Matrix2<T> const &a, Matrix2<T> const &b  )
  {
    return ( a.i == b.i ) && ( a.j == b.j );
  }

  template< typename T >
  A3M_BOOL operator!=( Matrix2<T> const &a, Matrix2<T> const &b )
  {
    return !( a == b );
  }

  template< typename T >
  Matrix2<T> transpose( Matrix2<T> const &m )
  {
    return Matrix2<T>( Vector2<T>( m.i.x, m.j.x ),
                       Vector2<T>( m.i.y, m.j.y ) );
  }

  template< typename T >
  Matrix2<T> inverse( Matrix2<T> const &m )
  {
    T det = m.Column[0].x * m.Column[1].y - m.Column[1].x * m.Column[0].y;
    if( det != T(0) )
    {
      Matrix2<T> inverse;
      A3M_FLOAT scale = 1.0f / det;
      inverse.Column[0].x = m.Column[1].y * scale;
      inverse.Column[0].y = -m.Column[0].y * scale;
      inverse.Column[1].x = -m.Column[1].x * scale;
      inverse.Column[1].y = m.Column[0].x * scale;
    }
    return inverse;
  }

  template< typename T >
  Matrix2<T> rotation( Angle<T> const &angle )
  {

    T c = cos( angle );
    T s = sin( angle );
    T t = T(1) - c;

    return Matrix2<T>( Vector2<T>( c, s ),
                       Vector2<T>( -s, c ) );
  }

  template< typename T >
  Matrix2<T> scale( T xScale, T yScale )
  {
    return Matrix2<T>( Vector2<T>( xScale, T(0)   ),
                       Vector2<T>( T(0),   yScale ) );
  }

  /** @} */

} /* namespace a3m */

#endif /* A3MATH_MATRIX2_H */
