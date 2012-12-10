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
 * 3D Matrix maths functions
 */
#pragma once
#ifndef A3MATH_MATRIX3_H
#define A3MATH_MATRIX3_H

#include <a3m/fpmaths.h> /* ensure fast sin, cos, sqrt are used */

#include <a3m/angle.h> /* for Angle type */
#include <a3m/vector3.h> /* for Vector3 type */

namespace a3m
{
  /** \defgroup a3mMatrix3 3x3 Matrices
   *  \ingroup  a3mRefMaths
   *
   *  3x3 matrix support, x-y-z by i-j-k
   *  @{
   */

  /** Class representing a 3 by 3 matrix.
   * The class stores each column of the matrix in a 3d vector.
   */

  template< typename T >
  struct Matrix3
  {
    /** Scalar type */
    typedef T Scalar;
    /** Vector type used for columns */
    typedef Vector3< T > Column;

    /** Identity matrix */
    static const Matrix3<T> IDENTITY;

    /** Default constructor. Sets this matrix to the identity.*/
    Matrix3();
    /** Constructor initialising columns. */
    Matrix3( Column const &i, /**< initialiser for column 0*/
             Column const &j, /**< initialiser for column 1*/
             Column const &k  /**< initialiser for column 2*/);

    /** Index operator. Returns a reference to a matrix column.
    * \return reference to a column of the matrix
    */
    Column &operator[]( A3M_INT32 index /**< index of column in the
                                             range [0,2] */ );
    /** Index operator. Returns a constant reference to a matrix column.
    * \return constant reference to a column of the matrix
    */
    Column const &operator[]( A3M_INT32 index /**< index of column in
                                                   the range [0,2] */ ) const;

    /** First column (column 0) of matrix */
    Column i;
    /** First column (column 1) of matrix */
    Column j;
    /** First column (column 2) of matrix */
    Column k;
  };

  /** Specialisation for float */
  typedef Matrix3< A3M_FLOAT > Matrix3f;

  /** Multiplication operator matrix * vector.
   * \return transformed vector
   */
  template< typename T >
  Vector3<T> operator*( Matrix3<T> const &m, /**< matrix */
                        Vector3<T> const &v  /**< vector */);

  /** Multiplication operator
   * \return matrix product
   */
  template< typename T >
  Matrix3<T> operator*( Matrix3<T> const &a, /**< left-hand operand */
                        Matrix3<T> const &b  /**< right-hand operand */ );

  /** Equals operator
   * \return TRUE if a == b
   */
  template< typename T >
  A3M_BOOL operator==( Matrix3<T> const &a, /**< left-hand operand */
                       Matrix3<T> const &b  /**< right-hand operand */);

  /** Not-equal operator
   * \return TRUE if a != b
   */
  template< typename T >
  A3M_BOOL operator!=( Matrix3<T> const &a, /**< left-hand operand */
                       Matrix3<T> const &b  /**< right-hand operand */);

  /** Transpose matrix
   * \return transposed matrix
   */
  template< typename T >
  Matrix3<T> transpose( Matrix3<T> const &m /**< matrix */ );

  /** Invert matrix
   * \return inverse of matrix
   */
  template< typename T >
  Matrix3<T> inverse( Matrix3<T> const &m /**< matrix */ );

  /** Calculate inverse transpose of matrix
   * \return inverse transpose of matrix
   */
  template< typename T >
  Matrix3<T> inverseTranspose( Matrix3<T> const &m /**< matrix */ );

  /** Rotation matrix
   * \return matrix representing a rotation
   */
  template< typename T >
  Matrix3<T> rotation( Vector3<T> const &axis, /**< axis of rotation */
                       Angle<T> const &angle  /**< angle */);

  /** Scaling matrix
   * \return matrix representing a scale operation
   */
  template< typename T >
  Matrix3<T> scale( T xScale, /**< X scaling factor */
                    T yScale, /**< Y scaling factor */
                    T zScale  /**< Z scaling factor */);

  /** Translation matrix
   * This only works if you are using the matrix to transform 2D homogeneous
   * vectors.
   * \return matrix representing a translation operation
   */
  template< typename T >
  Matrix3<T> translation( Vector3<T> const &offset  /**< translation
                                                        vector */ );




/******************************************************************************
 * Implementation
 ******************************************************************************/

  template< typename T >
  const Matrix3<T> Matrix3<T>::IDENTITY = Matrix3<T>();

  template< typename T >
  Matrix3<T>::Matrix3()
  : i(T(1),T(0),T(0)),
    j(T(0),T(1),T(0)),
    k(T(0),T(0),T(1))
  {
  }

  template< typename T >
  Matrix3<T>::Matrix3( Column const &i, Column const &j, Column const &k)
  : i(i), j(j), k(k)
  {
  }

  template< typename T >
  typename Matrix3<T>::Column &Matrix3<T>::operator[]( A3M_INT32 index )
  {
    return *( &i + index );
  }

  template< typename T >
  typename Matrix3<T>::Column const &
  Matrix3<T>::operator[]( A3M_INT32 index ) const
  {
    return *( &i + index );
  }


  template< typename T >
  Vector3<T> operator*( Matrix3<T> const &a, Vector3<T> const &b )
  {
    return a.i * b.x + a.j * b.y + a.k * b.z;
  }

  template< typename T >
  Matrix3<T> operator*( Matrix3<T> const &a, Matrix3<T> const & b )
  {
    return Matrix3<T>( a * b.i, a * b.j, a * b.k );
  }

  template< typename T >
  A3M_BOOL operator==( Matrix3<T> const &a, Matrix3<T> const &b  )
  {
    return ( a.i == b.i ) && ( a.j == b.j ) && ( a.k == b.k );
  }

  template< typename T >
  A3M_BOOL operator!=( Matrix3<T> const &a, Matrix3<T> const &b )
  {
    return !( a == b );
  }

  template< typename T >
  Matrix3<T> transpose( Matrix3<T> const &m )
  {
    return Matrix3<T>( Vector3<T>( m.i.x, m.j.x, m.k.x ),
                       Vector3<T>( m.i.y, m.j.y, m.k.y ),
                       Vector3<T>( m.i.z, m.j.z, m.k.z ) );
  }

  template< typename T >
  Matrix3<T> inverseTranspose( Matrix3<T> const &m )
  {
    /* determinant is triple product of the column vectors */
    T det = dot( m.i, cross( m.j, m.k ) );
    if( det != T(0) )
    {
      T scale = T(1) / det;
      return Matrix3<T>( cross( m.j, m.k ) * scale,
                         cross( m.k, m.i ) * scale,
                         cross( m.i, m.j ) * scale );
    }
    /* we couldn't invert matrix so return the original */
    return m;
  }

  template< typename T >
  Matrix3<T> inverse( Matrix3<T> const &m )
  {
    return transpose( inverseTranspose( m ) );
  }

  template< typename T >
  Matrix3<T> rotation( Vector3<T> const &axis, Angle<T> const &angle )
  {
    Vector3<T> a = normalize( axis );

    T c = cos( angle );
    T s = sin( angle );
    T t = T(1) - c;

    return Matrix3<T>(
      Vector3<T>( t*a.x*a.x + c,      t*a.x*a.y + s*a.z,  t*a.x*a.z - s*a.y ),
      Vector3<T>( t*a.x*a.y - s*a.z,  t*a.y*a.y + c,      t*a.y*a.z + s*a.x ),
      Vector3<T>( t*a.x*a.z + s*a.y,  t*a.y*a.z - s*a.x,  t*a.z*a.z + c     ) );
  }

  template< typename T >
  Matrix3<T> scale( T xScale, T yScale, T zScale )
  {
    return Matrix3<T>( Vector3<T>( xScale, T(0),   T(0)   ),
                       Vector3<T>( T(0),   yScale, T(0)   ),
                       Vector3<T>( T(0),   T(0),   zScale ) );
  }

  template< typename T >
  Matrix3<T> translation( Vector3<T> const &offset )
  {
    return Matrix3<T>( Vector3<T>( T(1), T(0), T(0) ),
                       Vector3<T>( T(0), T(1), T(0) ),
                       Vector3<T>( offset.x, offset.y, T(1) ) );
  }

  /** @} */

} /* namespace a3m */

#endif /* A3MATH_MATRIX3_H */
