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
 * 4D Vector maths functions
 */
#pragma once
#ifndef A3MATH_VECTOR4_H
#define A3MATH_VECTOR4_H

#include <cmath> /* for sqrt */
#include <a3m/base_types.h>  /* for A3M_FLOAT */

namespace a3m
{

  /** \defgroup  a3mVector4 4D Vector class
   *  \ingroup   a3mVector
   *
   *  x-y-z-w vector
   *  @{
   */

  template< typename T >
  struct Vector3;

  /** Class representing a 4d vector.
   * The class supports addition, subtraction and member-wise multiplication.
   */
  template< typename T >
  struct Vector4
  {
    /** Type of each vector component */
    typedef T Scalar;

    /** The origin */
    static const Vector4<T> ZERO;
    /** The x axis */
    static const Vector4<T> X_AXIS;
    /** The y axis */
    static const Vector4<T> Y_AXIS;
    /** The z axis */
    static const Vector4<T> Z_AXIS;
    /** The w axis */
    static const Vector4<T> ORIGIN;

    /** Default constructor. components are not initialised */
    Vector4();
    /** Constructor taking initialisers for each component */
    Vector4( T x, T y, T z, T w );
    /** Constructor taking Vector3 and w component */
    Vector4( Vector3<T> const &v3, T w );

    /** Assignment add operator.
     * \return reference to this vector*/
    Vector4<T> &operator+=( Vector4<T> const &b /**< vector to add */ );
    /** Assignment subtraction operator.
     * \return reference to this vector*/
    Vector4<T> &operator-=( Vector4<T> const &b /**< vector to
                                                     subtract */ );
    /** Assignment multiplication operator (component-wise by another vector).
     * \return reference to this vector*/
    Vector4<T> &operator*=( Vector4<T> const &b /**< vector to multiply
                                                     by */ );
    /** Assignment multiplication by scalar operator.
     * \return reference to this vector*/
    template< typename O >
    Vector4<T> &operator*=( O b /**< scalar number to multiply by */ );
    /** Unary negation operator.
     * \return vector with equal magnitude but opposite direction*/
    Vector4<T> operator-() const;

    /** Index operator. Returns a reference to a vector component
     * \return reference to component
     */
    T &operator[]( A3M_INT32 i  /**< index of component in the
                                     range [0,3] */ );
    /** Index operator. Returns a constant reference to a vector component
     * \return constant reference to component
     */
    T const &operator[]( A3M_INT32 i  /**< index of component in the
                                           range [0,3] */) const;

    T x /**< x component */;
    T y /**< y component */;
    T z /**< z component */;
    T w /**< w component */;
  };

  /** Specialisation for float */
  typedef Vector4< A3M_FLOAT > Vector4f;

  /** Addition operator
   * \return sum of vectors
   */
  template< typename T >
  Vector4<T> operator+( Vector4<T> const &a, Vector4<T> const &b );

  /** Subtraction operator
   * \return difference of vectors
   */
  template< typename T >
  Vector4<T> operator-( Vector4<T> const &a, Vector4<T> const &b );

  /** Component-wise multiplication operator - vector*vector
   * \return component-wise product of vectors
   */
  template< typename T >
  Vector4<T> operator*( Vector4<T> const &a, Vector4<T> const &b );

  /** Multiplication operator scalar*vector
   * \return product of vector and scalar
   */
  template< typename T, typename S >
  Vector4<T> operator*( S a, Vector4<T> const &b );

  /** Multiplication operator vector*scalar
   * \return product of vector and scalar
   */
  template< typename T, typename S >
  Vector4<T> operator*( Vector4<T> const &a, S b );

  /** Component-wise division operator vector/vector
   * \return component-wise result of vector division
   */
  template< typename T >
  Vector4<T> operator/( Vector4<T> const &a, /**< left-hand operand */
                        Vector4<T> const &b  /**< right-hand operand */ );

  /** Division operator vector/scalar
   * \return vector scaled by 1/b
   */
  template< typename T, typename S >
  Vector4<T> operator/( Vector4<T> const &a, S b );

  /** Equals operator
   * \return TRUE if a == b
   */
  template< typename T >
  A3M_BOOL operator==( Vector4<T> const &a, /**< left-hand operand */
                       Vector4<T> const &b  /**< right-hand operand */ );

  /** Not-equal operator
   * \return TRUE if a != b
   */
  template< typename T >
  A3M_BOOL operator!=( Vector4<T> const &a, /**< left-hand operand */
                       Vector4<T> const &b  /**< right-hand operand */ );

  /** Find length (magnitude) of vector.
   * \return length of vector
   */
  template< typename T >
  T length( Vector4<T> const &v );

  /** Find length^2 of vector.
   * Useful for comparing two vector lengths where the actual length values are
   * not required, saving expensive sqrt calculations.
   * \return length^2 of vector
   */
  template< typename T >
  T lengthSquared( Vector4<T> const &v /**< vector */);

  /** Make vector with the same direction as v with unit length.
   * \return normalized vector
   */
  template< typename T >
  Vector4<T> normalize( Vector4<T> const &v );

  /** Find dot product of two vectors
   * \return dot product
   */
  template< typename T >
  T dot( Vector4<T> const &a, Vector4<T> const &b );

} /* namespace a3m */



/******************************************************************************
 * Implementation
 ******************************************************************************/
/*
 * Include vector3 header here to prevent circular dependencies.
 * a3math_vector3.h must be included from outside namespace a3m or it will
 * attempt to nest another namespace a3m inside this one.
 */
#include <a3m/vector3.h>   /* for Vector3  */

namespace a3m
{
  /*
   * Zero constant.
   */
  template< typename T >
  const Vector4<T> Vector4<T>::ZERO = Vector4<T>((T)0.0, (T)0.0, (T)0.0, (T)0.0);

  /*
   * X axis constant.
   */
  template< typename T >
  const Vector4<T> Vector4<T>::X_AXIS = Vector4<T>((T)1.0, (T)0.0, (T)0.0, (T)0.0);

  /*
   * Y axis constant.
   */
  template< typename T >
  const Vector4<T> Vector4<T>::Y_AXIS = Vector4<T>((T)0.0, (T)1.0, (T)0.0, (T)0.0);

  /*
   * Z axis constant.
   */
  template< typename T >
  const Vector4<T> Vector4<T>::Z_AXIS = Vector4<T>((T)0.0, (T)0.0, (T)1.0, (T)0.0);

  /*
   * W axis constant.
   */
  template< typename T >
  const Vector4<T> Vector4<T>::ORIGIN = Vector4<T>((T)0.0, (T)0.0, (T)0.0, (T)1.0);

  /*
   * Default constructor. components are initialised to zero
   */
  template< typename T >
  Vector4<T>::Vector4() : x(T(0)), y(T(0)), z(T(0)) , w(T(0))
  {
  }

  /*
   * Constructor taking initialisers for each component
   */
  template< typename T >
  Vector4<T>::Vector4( T x, T y, T z, T w )
  : x(x), y(y), z(z), w(w)
  {
  }

  /*
   * Constructor taking Vector3 and w component
   */
  template< typename T >
  Vector4<T>::Vector4( Vector3<T> const &v3, T w )
  : x(v3.x), y(v3.y), z(v3.z), w(w)
  {
  }

  /*
   * Assignment add
   */
  template< typename T >
  Vector4<T> &Vector4<T>::operator+=( Vector4<T> const &b )
  {
    x += b.x; y += b.y; z += b.z; w += b.w;
    return *this;

  }

  /*
   * Assignment subtract
   */
  template< typename T >
  Vector4<T> &Vector4<T>::operator-=( Vector4<T> const &b )
  {
    x -= b.x; y -= b.y; z -= b.z; w -= b.w;
    return *this;
  }

  /*
   * Assignment component-wise multiply
   */
  template< typename T >
  Vector4<T> &Vector4<T>::operator*=( Vector4<T> const &b )
  {
    x *= b.x; y *= b.y; z *= b.z; w *= b.w;
    return *this;
  }

  /*
   * Assignment scale
   */
  template< typename T > template< typename O >
  Vector4<T> &Vector4<T>::operator*=( O b )
  {
    x = T( b * x ); y = T( b * y ); z = T( b * z ); w = T( b * w );
    return *this;
  }


  /*
   * Unary negation operator
   */
  template< typename T >
  Vector4<T> Vector4<T>::operator-() const
  {
    return Vector4<T>( -x, -y, -z, -w );
  }


  /*
   * Component access
   */
  template< typename T >
  T &Vector4<T>::operator[]( A3M_INT32 i )
  {
    return *( &x + i );
  }

  /*
   * Constant component access
   */
  template< typename T >
  T const &Vector4<T>::operator[]( A3M_INT32 i ) const
  {
    return *( &x + i );
  }

  /*
   * Addition operator
   */
  template< typename T >
  Vector4<T> operator+( Vector4<T> const &a, /**< left-hand operand */
                        Vector4<T> const &b  /**< right-hand operand */ )
  {
    return Vector4<T>( a.x + b.x, a.y + b.y, a.z + b.z, a.w + b.w );
  }

  /*
   * Subtraction operator
   */
  template< typename T >
  Vector4<T> operator-( Vector4<T> const &a, /**< left-hand operand */
                        Vector4<T> const &b  /**< right-hand operand */ )
  {
    return Vector4<T>( a.x - b.x, a.y - b.y, a.z - b.z, a.w - b.w );
  }

  /*
   * Multiplication operator
   */
  template< typename T >
  Vector4<T> operator*( Vector4<T> const &a, /**< left-hand operand */
                        Vector4<T> const &b  /**< right-hand operand */ )
  {
    return Vector4<T>( a.x * b.x, a.y * b.y, a.z * b.z, a.w * b.w );
  }

  /*
   * Multiplication operator (by scalar)
   */
  template< typename T, typename S >
  Vector4<T> operator*( S a,                 /**< left-hand operand */
                        Vector4<T> const &b  /**< right-hand operand */ )
  {
    return Vector4<T>( a * b.x, a * b.y, a * b.z, a * b.w );
  }

  /*
   * Multiplication operator (by scalar)
   */
  template< typename T, typename S >
  Vector4<T> operator*( Vector4<T> const &a, /**< left-hand operand */
                        S b                  /**< right-hand operand */ )
  {
    return Vector4<T>( a.x * b, a.y * b, a.z * b, a.w * b );
  }

  /*
   * Division operator
   */
  template< typename T >
  Vector4<T> operator/( Vector4<T> const &a, Vector4<T> const &b )
  {
    return Vector4<T>( a.x / b.x, a.y / b.y, a.z / b.z, a.w / b.w );
  }

  /*
   * Division operator (by scalar)
   */
  template< typename T, typename S >
  Vector4<T> operator/( Vector4<T> const &a, /**< left-hand operand */
                        S b                  /**< right-hand operand */ )
  {
    return Vector4<T>( a.x / b, a.y / b, a.z / b, a.w / b );
  }

  /*
   * Equals operator
   */
  template< typename T >
  A3M_BOOL operator==( Vector4<T> const &a, Vector4<T> const &b )
  {
    return ( a.x == b.x ) && ( a.y == b.y ) &&
           ( a.z == b.z ) && ( a.w == b.w );
  }

  /*
   * Not-equal operator
   */
  template< typename T >
  A3M_BOOL operator!=( Vector4<T> const &a, Vector4<T> const &b )
  {
    return !(a == b);
  }

  /*
   * Find length of vector
   */
  template< typename T >
  T length( Vector4<T> const &v /**< vector */ )
  {
    return T( std::sqrt( dot( v, v ) ) );
  }

  /*
   * Find length^2 of vector
   */
  template< typename T >
  T lengthSquared( Vector4<T> const &v /**< vector */ )
  {
    return T( dot( v, v ) );
  }

  /*
   * Normalize vector
   */
  template< typename T >
  Vector4<T> normalize( Vector4<T> const &v /**< vector */ )
  {
    T l = length(v);
    if( l > T(0) )
    {
      return v / length(v);
    }
    else
    {
      return v;
    }
  }

  /*
   * Get dot product of two vectors
   */
  template< typename T >
  T dot( Vector4<T> const &a, /**< left-hand operand */
         Vector4<T> const &b  /**< right-hand operand */ )
  {
    return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
  }

  /** @} */

} /* namespace a3m */

#endif /* A3MATH_VECTOR4_H */
