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
 * 4D Matrix maths functions
 */
#pragma once
#ifndef A3MATH_MATRIX4_H
#define A3MATH_MATRIX4_H

#include <a3m/fpmaths.h> /* ensure fast sin, cos, sqrt are used */

#include <a3m/angle.h> /* for Angle type */
#include <a3m/vector4.h> /* for Vector4 type */

namespace a3m
{

  /** \defgroup a3mMatrix4 4x4 Matrices
   *  \ingroup  a3mRefMaths
   *
   *  4x4 matrix support, x-y-z-w by i-j-k-t
   *  @{
   */

  /** Class representing a 4 by 4 matrix.
   * The class stores each column of the matrix in a 4d vector.
   */

  template< typename T >
  struct Matrix4
  {
    /** Scalar type */
    typedef T Scalar;
    /** Vector type used for columns */
    typedef Vector4< T > Column;

    /** Identity matrix */
    static const Matrix4<T> IDENTITY;

    /** Default constructor. Sets this matrix to the identity.*/
    Matrix4();
    /** Constructor initialising columns. */
    Matrix4( Column const &i, /**< initialiser for column 0*/
             Column const &j, /**< initialiser for column 1*/
             Column const &k, /**< initialiser for column 2*/
             Column const &t  /**< initialiser for column 3*/);

    /** Index operator. Returns a reference to a matrix column.
    * \return reference to a column of the matrix
    */
    Column &operator[]( A3M_INT32 index /**< index of column in the
                                             range [0,3] */);
    /** Index operator. Returns a constant reference to a matrix column.
    * \return constant reference to a column of the matrix
    */
    Column const &operator[]( A3M_INT32 index /**< index of column in the
                                             range [0,3] */) const;

    /** First column (column 0) of matrix */
    Column i;
    /** First column (column 1) of matrix */
    Column j;
    /** First column (column 2) of matrix */
    Column k;
    /** First column (column 3) of matrix (represents translation for a
        homogenous 3d vector)*/
    Column t;
  };

  /** Specialisation for float */
  typedef Matrix4< A3M_FLOAT > Matrix4f;


  /** Multiplication operator matrix * vector.
   * \return transformed vector
   */
  template< typename T >
  Vector4<T> operator*( Matrix4<T> const &m, /**< matrix */
                        Vector4<T> const &v  /**< vector */ );

  /** Multiplication operator
   * \return matrix product
   */
  template< typename T >
  Matrix4<T> operator*( Matrix4<T> const &a, /**< left-hand operand */
                        Matrix4<T> const &b  /**< right-hand operand */ );

  /** Equals operator
   * \return TRUE if a == b
   */
  template< typename T >
  A3M_BOOL operator==( Matrix4<T> const &a, /**< left-hand operand */
                       Matrix4<T> const &b  /**< right-hand operand */);

  /** Not-equal operator
   * \return TRUE if a != b
   */
  template< typename T >
  A3M_BOOL operator!=( Matrix4<T> const &a, /**< left-hand operand */
                       Matrix4<T> const &b  /**< right-hand operand */);

  /** Transpose matrix
   * \return transposed matrix
   */
  template< typename T >
  Matrix4<T> transpose( Matrix4<T> const &m /**< matrix */ );

  /** Invert matrix
   * \return inverse of matrix
   */
  template< typename T >
  Matrix4<T> inverse( Matrix4<T> const &m /**< matrix */ );

  /** Invert matrix whose bottom row is 0 0 0 1
   * If the last row of a 4x4 matrix is 0 0 0 1, (usually the case if the matrix
   * represents a non-projecting transform) many of the terms calculated when
   * inverting it disappear or are simplified.
   * \return inverse of matrix
   */
  template< typename T >
  Matrix4<T> inverse43( Matrix4<T> const &m /**< matrix */ );

  /** Rotation matrix
   * \return matrix representing a rotation
   */
  template< typename T >
  Matrix4<T> rotation( Vector4<T> const &axis, /**< axis of rotation */
                       Angle<T> const &angle   /**< angle */ );

  /** Scaling matrix
   * \return matrix representing a scale operation
   */
  template< typename T >
  Matrix4<T> scale( T xScale, /**< X scaling factor */
                    T yScale, /**< Y scaling factor */
                    T zScale, /**< Z scaling factor */
                    T wScale  /**< W scaling factor */);

  /** Translation matrix
   * This only works if you are using the matrix to transform 3D homogeneous
   * vectors.
   * \return matrix representing a translation operation
   */
  template< typename T >
  Matrix4<T> translation( Vector4<T> const &offset /**< translation
                                                        vector */ );

  /** Decompose a rotation matrix to axis-angle pair.
   */
  template< typename T >
  void getAxisAngle( Matrix4<T> const &m, /**< rotation matrix */
                     Vector4<T> &axis, /**< [out] axis of rotation */
                     Angle<T> &angle /**< [out] angle of rotation */ );

  /** Interpolation.
   * Interpolate between two matrices. Note that the matrices are assumed to
   * only contain rotations and translations. This routine does the
   * equivalent of a quaternion SLERP. This is more costly to do using
   * matrices as the axis and angle of the rotation must be extracted from
   * the matrix representing the transform from one matrix to another.
   *
   * Note:
   * interpolate( m0, m1, 0 ) returns m0
   * interpolate( m0, m1, 1 ) returns m1
   *
   * \return matrix interpolation between the two given matrices.
   */
  template< typename T >
  Matrix4<T> interpolate( Matrix4<T> const &m0, /**< first matrix */
                          Matrix4<T> const &m1, /**< second matrix */
                          T t /**< interpolation factor in range [0,1] */
                         );
/******************************************************************************
 * Implementation
 ******************************************************************************/

  template< typename T >
  const Matrix4<T> Matrix4<T>::IDENTITY = Matrix4<T>();

  template< typename T >
  Matrix4<T>::Matrix4()
  : i(T(1),T(0),T(0),T(0)),
    j(T(0),T(1),T(0),T(0)),
    k(T(0),T(0),T(1),T(0)),
    t(T(0),T(0),T(0),T(1))
  {
  }

  template< typename T >
  Matrix4<T>::Matrix4( Column const &i, Column const &j,
                       Column const &k, Column const &t )
  : i(i), j(j), k(k), t(t)
  {
  }

  template< typename T >
  typename Matrix4<T>::Column &Matrix4<T>::operator[]( A3M_INT32 index )
  {
    return *( &i + index );
  }

  template< typename T >
  typename Matrix4<T>::Column const &
  Matrix4<T>::operator[]( A3M_INT32 index ) const
  {
    return *( &i + index );
  }

  template< typename T >
  Vector4<T> operator*( Matrix4<T> const &a, Vector4<T> const &b )
  {
    return a.i * b.x + a.j * b.y + a.k * b.z + a.t * b.w;
  }

  template< typename T >
  Matrix4<T> operator*( Matrix4<T> const &a, Matrix4<T> const & b )
  {
    return Matrix4<T>( a * b.i, a * b.j, a * b.k, a * b.t );
  }

  template< typename T >
  A3M_BOOL operator==( Matrix4<T> const &a, Matrix4<T> const &b  )
  {
    return ( a.i == b.i ) && ( a.j == b.j ) &&
           ( a.k == b.k ) && ( a.t == b.t );
  }

  template< typename T >
  A3M_BOOL operator!=( Matrix4<T> const &a, Matrix4<T> const &b )
  {
    return !( a == b );
  }

  template< typename T >
  Matrix4<T> transpose( Matrix4<T> const &m )
  {
    typedef Vector4<T> V;
    return Matrix4<T>( V( m.i.x, m.j.x, m.k.x, m.t.x ),
                       V( m.i.y, m.j.y, m.k.y, m.t.y ),
                       V( m.i.z, m.j.z, m.k.z, m.t.z ),
                       V( m.i.w, m.j.w, m.k.w, m.t.w ) );
  }

  template< typename T >
  Matrix4<T> inverse( Matrix4<T> const &m )
  {
    /* If the bottom row is 0 0 0 1 do a much cheaper inverse */
    if( ( m.i.w == T(0) ) && ( m.j.w == T(0) ) &&
        ( m.k.w == T(0) ) && ( m.t.w == T(1) ) )
    {
      return inverse43( m );
    }

    T det = m.i.w * m.j.z * m.k.y * m.t.x - m.i.z * m.j.w * m.k.y * m.t.x -
            m.i.w * m.j.y * m.k.z * m.t.x + m.i.y * m.j.w * m.k.z * m.t.x +
            m.i.z * m.j.y * m.k.w * m.t.x - m.i.y * m.j.z * m.k.w * m.t.x -
            m.i.w * m.j.z * m.k.x * m.t.y + m.i.z * m.j.w * m.k.x * m.t.y +
            m.i.w * m.j.x * m.k.z * m.t.y - m.i.x * m.j.w * m.k.z * m.t.y -
            m.i.z * m.j.x * m.k.w * m.t.y + m.i.x * m.j.z * m.k.w * m.t.y +
            m.i.w * m.j.y * m.k.x * m.t.z - m.i.y * m.j.w * m.k.x * m.t.z -
            m.i.w * m.j.x * m.k.y * m.t.z + m.i.x * m.j.w * m.k.y * m.t.z +
            m.i.y * m.j.x * m.k.w * m.t.z - m.i.x * m.j.y * m.k.w * m.t.z -
            m.i.z * m.j.y * m.k.x * m.t.w + m.i.y * m.j.z * m.k.x * m.t.w +
            m.i.z * m.j.x * m.k.y * m.t.w - m.i.x * m.j.z * m.k.y * m.t.w -
            m.i.y * m.j.x * m.k.z * m.t.w + m.i.x * m.j.y * m.k.z * m.t.w;

    /* This does not protect against overflow when det is very small, but does
       at least guard against singular matrices with e.g. all zero elements */
    if( det != T( 0 ) )
    {
      T scale = T( 1 ) / det;
      Matrix4<T> inv;
      inv.i.x = m.j.z*m.k.w*m.t.y - m.j.w*m.k.z*m.t.y + m.j.w*m.k.y*m.t.z -
                m.j.y*m.k.w*m.t.z - m.j.z*m.k.y*m.t.w + m.j.y*m.k.z*m.t.w;
      inv.i.y = m.i.w*m.k.z*m.t.y - m.i.z*m.k.w*m.t.y - m.i.w*m.k.y*m.t.z +
                m.i.y*m.k.w*m.t.z + m.i.z*m.k.y*m.t.w - m.i.y*m.k.z*m.t.w;
      inv.i.z = m.i.z*m.j.w*m.t.y - m.i.w*m.j.z*m.t.y + m.i.w*m.j.y*m.t.z -
                m.i.y*m.j.w*m.t.z - m.i.z*m.j.y*m.t.w + m.i.y*m.j.z*m.t.w;
      inv.i.w = m.i.w*m.j.z*m.k.y - m.i.z*m.j.w*m.k.y - m.i.w*m.j.y*m.k.z +
                m.i.y*m.j.w*m.k.z + m.i.z*m.j.y*m.k.w - m.i.y*m.j.z*m.k.w;
      inv.j.x = m.j.w*m.k.z*m.t.x - m.j.z*m.k.w*m.t.x - m.j.w*m.k.x*m.t.z +
                m.j.x*m.k.w*m.t.z + m.j.z*m.k.x*m.t.w - m.j.x*m.k.z*m.t.w;
      inv.j.y = m.i.z*m.k.w*m.t.x - m.i.w*m.k.z*m.t.x + m.i.w*m.k.x*m.t.z -
                m.i.x*m.k.w*m.t.z - m.i.z*m.k.x*m.t.w + m.i.x*m.k.z*m.t.w;
      inv.j.z = m.i.w*m.j.z*m.t.x - m.i.z*m.j.w*m.t.x - m.i.w*m.j.x*m.t.z +
                m.i.x*m.j.w*m.t.z + m.i.z*m.j.x*m.t.w - m.i.x*m.j.z*m.t.w;
      inv.j.w = m.i.z*m.j.w*m.k.x - m.i.w*m.j.z*m.k.x + m.i.w*m.j.x*m.k.z -
                m.i.x*m.j.w*m.k.z - m.i.z*m.j.x*m.k.w + m.i.x*m.j.z*m.k.w;
      inv.k.x = m.j.y*m.k.w*m.t.x - m.j.w*m.k.y*m.t.x + m.j.w*m.k.x*m.t.y -
                m.j.x*m.k.w*m.t.y - m.j.y*m.k.x*m.t.w + m.j.x*m.k.y*m.t.w;
      inv.k.y = m.i.w*m.k.y*m.t.x - m.i.y*m.k.w*m.t.x - m.i.w*m.k.x*m.t.y +
                m.i.x*m.k.w*m.t.y + m.i.y*m.k.x*m.t.w - m.i.x*m.k.y*m.t.w;
      inv.k.z = m.i.y*m.j.w*m.t.x - m.i.w*m.j.y*m.t.x + m.i.w*m.j.x*m.t.y -
                m.i.x*m.j.w*m.t.y - m.i.y*m.j.x*m.t.w + m.i.x*m.j.y*m.t.w;
      inv.k.w = m.i.w*m.j.y*m.k.x - m.i.y*m.j.w*m.k.x - m.i.w*m.j.x*m.k.y +
                m.i.x*m.j.w*m.k.y + m.i.y*m.j.x*m.k.w - m.i.x*m.j.y*m.k.w;
      inv.t.x = m.j.z*m.k.y*m.t.x - m.j.y*m.k.z*m.t.x - m.j.z*m.k.x*m.t.y +
                m.j.x*m.k.z*m.t.y + m.j.y*m.k.x*m.t.z - m.j.x*m.k.y*m.t.z;
      inv.t.y = m.i.y*m.k.z*m.t.x - m.i.z*m.k.y*m.t.x + m.i.z*m.k.x*m.t.y -
                m.i.x*m.k.z*m.t.y - m.i.y*m.k.x*m.t.z + m.i.x*m.k.y*m.t.z;
      inv.t.z = m.i.z*m.j.y*m.t.x - m.i.y*m.j.z*m.t.x - m.i.z*m.j.x*m.t.y +
                m.i.x*m.j.z*m.t.y + m.i.y*m.j.x*m.t.z - m.i.x*m.j.y*m.t.z;
      inv.t.w = m.i.y*m.j.z*m.k.x - m.i.z*m.j.y*m.k.x + m.i.z*m.j.x*m.k.y -
                m.i.x*m.j.z*m.k.y - m.i.y*m.j.x*m.k.z + m.i.x*m.j.y*m.k.z;
      for( A3M_INT32 col = 0; col != 4; ++col )
      {
        inv[ col ] *= scale;
      }
      return inv;
    }
    /* we couldn't invert matrix so return the original */
    return m;
  }

  template< typename T >
  Matrix4<T> inverse43( Matrix4<T> const &m )
  {

    T det = -m.i.z * m.j.y * m.k.x + m.i.y * m.j.z * m.k.x +
             m.i.z * m.j.x * m.k.y - m.i.x * m.j.z * m.k.y -
             m.i.y * m.j.x * m.k.z + m.i.x * m.j.y * m.k.z;

    /* This does not protect against overflow when det is very small, but does
       at least guard against singular matrices with e.g. all zero elements */
    if( det != T( 0 ) )
    {
      T scale = T( 1 ) / det;
      Matrix4<T> inv;
      inv.i.x = -m.j.z*m.k.y + m.j.y*m.k.z;
      inv.i.y =  m.i.z*m.k.y - m.i.y*m.k.z;
      inv.i.z = -m.i.z*m.j.y + m.i.y*m.j.z;
      inv.i.w = T(0);
      inv.j.x =  m.j.z*m.k.x - m.j.x*m.k.z;
      inv.j.y = -m.i.z*m.k.x + m.i.x*m.k.z;
      inv.j.z =  m.i.z*m.j.x - m.i.x*m.j.z;
      inv.j.w = T(0);
      inv.k.x = -m.j.y*m.k.x + m.j.x*m.k.y;
      inv.k.y =  m.i.y*m.k.x - m.i.x*m.k.y;
      inv.k.z = -m.i.y*m.j.x + m.i.x*m.j.y;
      inv.k.w = T(0);
      inv.t.x = m.j.z*m.k.y*m.t.x - m.j.y*m.k.z*m.t.x - m.j.z*m.k.x*m.t.y +
                m.j.x*m.k.z*m.t.y + m.j.y*m.k.x*m.t.z - m.j.x*m.k.y*m.t.z;
      inv.t.y = m.i.y*m.k.z*m.t.x - m.i.z*m.k.y*m.t.x + m.i.z*m.k.x*m.t.y -
                m.i.x*m.k.z*m.t.y - m.i.y*m.k.x*m.t.z + m.i.x*m.k.y*m.t.z;
      inv.t.z = m.i.z*m.j.y*m.t.x - m.i.y*m.j.z*m.t.x - m.i.z*m.j.x*m.t.y +
                m.i.x*m.j.z*m.t.y + m.i.y*m.j.x*m.t.z - m.i.x*m.j.y*m.t.z;
      inv.t.w = det; /* will scale back to 1 */
      for( A3M_INT32 col = 0; col != 4; ++col )
      {
        inv[ col ] *= scale;
      }
      inv.t.w = T(1); /* set exactly to that comparisons in future will work */
      return inv;
    }
    /* we couldn't invert matrix so return the original */
    return m;
  }

  template< typename T >
  Matrix4<T> rotation( Vector4<T> const &axis, Angle<T> const &angle )
  {
    typedef Vector4<T> V;
    V a = normalize( axis );

    T c = cos( angle );
    T s = sin( angle );
    T t = T(1) - c;

    return Matrix4<T>(
      V( t*a.x*a.x + c,     t*a.x*a.y + s*a.z, t*a.x*a.z - s*a.y, T(0) ),
      V( t*a.x*a.y - s*a.z, t*a.y*a.y + c,     t*a.y*a.z + s*a.x, T(0) ),
      V( t*a.x*a.z + s*a.y, t*a.y*a.z - s*a.x, t*a.z*a.z + c,     T(0) ),
      V( T(0),              T(0),              T(0),              T(1)) );
  }

  template< typename T >
  Matrix4<T> scale( T xScale, T yScale, T zScale, T wScale )
  {
    typedef Vector4<T> V;
    return Matrix4<T>( V( xScale, T(0),   T(0),   T(0)   ),
                       V( T(0),   yScale, T(0),   T(0)   ),
                       V( T(0),   T(0),   zScale, T(0)   ),
                       V( T(0),   T(0),   T(0),   wScale ) );
  }


  template< typename T >
  Matrix4<T> translation( Vector4<T> const &offset )
  {
    typedef Vector4<T> V;
    return Matrix4<T>( V( T(1), T(0), T(0), T(0) ),
                       V( T(0), T(1), T(0), T(0) ),
                       V( T(0), T(0), T(1), T(0) ),
                       V( offset.x, offset.y, offset.z, T(1) ) );
  }

  /*
   * Decompose a rotation matrix to axis-angle pair.
   * A description of this method can be found here:
   * http://www.euclideanspace.com/maths/geometry/rotations/conversions/...
   * ...matrixToAngle/index.htm
   * also: Geometric Tools for Computer Graphics p859
   */
  template< typename T >
  void getAxisAngle( Matrix4<T> const &m, /* rotation matrix */
                     Vector4<T> &axis, /* axis of rotation */
                     Angle<T> &angle /* angle of rotation */ )
  {
    T epsilon = T( 0.01f ); // margin to allow for rounding errors
    T epsilon2 = T( 0.1f ); // margin to distinguish between 0 and 180 degrees
    const T rSqr2 = T( 0.7071068f ); // 1/sqrt(2)

    if( ( abs( m[1][0] - m[0][1] ) < epsilon ) &&
        ( abs( m[2][0] - m[0][2] ) < epsilon ) &&
        ( abs( m[2][1] - m[1][2] ) < epsilon ) )
    {
      // Singularity found
      // first check for identity matrix which must have +1 for all terms
      // in leading diagonaland zero in other terms
      if( ( abs( m[1][0] + m[0][1] ) < epsilon2 ) &&
          ( abs( m[2][0] + m[0][2] ) < epsilon2 ) &&
          ( abs( m[2][1] + m[1][2] ) < epsilon2 ) &&
          ( abs( m[0][0] + m[1][1] + m[2][2] - T(3) ) < epsilon2 ) )
      {
        // this singularity is identity matrix so angle = 0, use arbitrary axis
        axis = Vector4<T>( T(0), T(1), T(0), T(0) );
        angle = Angle<T>::ZERO;
        return;
      }
      // otherwise this singularity is angle = 180
      setRadians( angle, FLOAT_PI );
      T xx = ( m[0][0] + T(1) ) / T(2);
      T yy = ( m[1][1] + T(1) ) / T(2);
      T zz = ( m[2][2] + T(1) ) / T(2);
      T xy = ( m[1][0] + m[0][1] ) / T(4);
      T xz = ( m[2][0] + m[0][2] ) / T(4);
      T yz = ( m[2][1] + m[1][2] ) / T(4);
      if( ( xx > yy ) && ( xx > zz ) )
      { // m[0][0] is the largest diagonal term
        if( xx < epsilon )
        {
          axis = Vector4<T>( T(0), rSqr2, rSqr2, T(0) );
        }
        else
        {
          T x( sqrt(xx) );
          axis = Vector4<T>( x, xy/x, xz/x, T(0) );
        }
      }
      else if( yy > zz )
      { // m[1][1] is the largest diagonal term
        if( yy < epsilon)
        {
          axis = Vector4<T>( rSqr2, T(0), rSqr2, T(0) );
        }
        else
        {
          T y( sqrt(yy) );
          axis = Vector4<T>( xy/y, y, yz/y, T(0) );
        }
      }
      else
      { // m[2][2] is the largest diagonal term so base result on this
        if( zz< epsilon )
        {
          axis = Vector4<T>( rSqr2, rSqr2, T(0), T(0) );
        }
        else
        {
          T z( sqrt(zz) );
          axis = Vector4<T>( xz/z, yz/z, z, T(0) );
        }
      }
      return;
    }
    T cosAngle = ( m[0][0] + m[1][1] + m[2][2] - T(1) ) / T(2);
    // This value may creep outside range [-1,1] and cause a not-a-number
    // result from acos().
    if( cosAngle > T(1) )
    {
      angle = Angle<T>::ZERO;
    }
    else if( cosAngle < T(-1) )
    {
      setRadians(angle, a3m::FLOAT_PI);
    }
    else
    {
      angle = acos( cosAngle );
    }
    axis = normalize( Vector4<T>( (m[1][2] - m[2][1] ),
                                  (m[2][0] - m[0][2] ),
                                  (m[0][1] - m[1][0] ), T(0) ) );
  }


  /*
   * Interpolation
   */
  template< typename T >
  Matrix4<T> interpolate( Matrix4<T> const &m0, /**< first matrix */
                          Matrix4<T> const &m1, /**< second matrix */
                          T t /**< interpolation factor in range [0,1] */
                         )
  {
    // if m1 = delta * m0
    //    m1 * inverse(m0) = delta * m0 * inverse(m0) = delta
    Matrix4<T> delta = m1 * inverse( m0 );
    Vector4<T> axis;
    Angle<T> deltaAngle;
    getAxisAngle( delta, axis, deltaAngle );

    // interpolate rotation
    Matrix4<T> result( rotation( axis, deltaAngle * t ) * m0 );

    // interpolate translation
    result.t   = m0.t + t * (m1.t - m0.t);
    result.t.w = T(1);

    return result;
  }
  /** @} */

} /* namespace a3m */


#endif
