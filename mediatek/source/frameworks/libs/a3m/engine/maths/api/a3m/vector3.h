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
 * 3D Vector maths functions
 */
#pragma once
#ifndef A3MATH_VECTOR3_H
#define A3MATH_VECTOR3_H

#include <cmath> /* for sqrt */
#include <a3m/base_types.h>  /* for A3M_FLOAT */

namespace a3m
{

  /** \defgroup  a3mVector3 3D Vector class
   *  \ingroup   a3mVector
   *
   *  x-y-z vector
   *  @{
   */
  template< typename T >
  struct Vector2;

  template< typename T >
  struct Vector4;

  /** Class representing a 3d vector.
   * The class supports addition, subtraction and member-wise multiplication.
   */
  template< typename T >
  struct Vector3
  {
    /** Type of each vector component */
    typedef T Scalar;

    /** The origin */
    static const Vector3<T> ZERO;
    /** The x axis */
    static const Vector3<T> X_AXIS;
    /** The y axis */
    static const Vector3<T> Y_AXIS;
    /** The z axis */
    static const Vector3<T> Z_AXIS;

    /** Default constructor. components are not initialised */
    Vector3();
    /** Constructor taking initialisers for each component */
    Vector3( T x, T y, T z );
    /** Constructor taking Vector2 and z */
    Vector3( Vector2<T> const &v, T z );
    /** Constructor taking a Vector4 (w component is discarded) */
    explicit Vector3( Vector4<T> const &v );

    /** Assignment add operator
     * \return reference to this vector*/
    Vector3<T> &operator+=( Vector3<T> const &b /**< vector to add */ );
    /** Assignment subtraction operator.
     * \return reference to this vector*/
    Vector3<T> &operator-=( Vector3<T> const &b /**< vector to
                                                     subtract */ );
    /** Assignment multiplication operator (component-wise by another vector).
     * \return reference to this vector*/
    Vector3<T> &operator*=( Vector3<T> const &b /**< vector to multiply
                                                     by */ );
    /** Assignment multiplication by scalar operator.
     * \return reference to this vector*/
    template< typename O >
    Vector3<T> &operator*=( O b /**< scalar number to multiply by */ );

    /** Assignment division operator (component-wise by another vector).
     * \return reference to this vector*/
    //Vector3<T> &operator/=( Vector3<T> const &b /**< vector to divide
                                                     //by */ );
    /** Assignment division by scalar operator.
     * \return reference to this vector*/
   // template< typename O >
   // Vector3<T> &operator/=( O b /**< scalar number to divide by */ );

    /** Unary negation operator.
    * \return vector with equal magnitude but opposite direction*/
    Vector3<T> operator-() const;

    /** Index operator. Returns a reference to a vector component
     * \return reference to component
     */
    T &operator[]( A3M_INT32 i /**< index of component in the
                                    range [0,2] */ );
    /** Index operator. Returns a constant reference to a vector component
     * \return constant reference to component
     */
    T const &operator[]( A3M_INT32 i  /**< index of component in the
                                           range [0,2] */) const;

    T x /**< x component */;
    T y /**< y component */;
    T z /**< z component */;
  };

  /** Specialisation for float */
  typedef Vector3< A3M_FLOAT > Vector3f;

  /** Addition operator
   * \return sum of vectors
   */
  template< typename T >
  Vector3<T> operator+( Vector3<T> const &a, /**< left-hand operand */
                        Vector3<T> const &b  /**< right-hand operand */ );

  /** Subtraction operator
   * \return difference of vectors
   */
  template< typename T >
  Vector3<T> operator-( Vector3<T> const &a, /**< left-hand operand */
                        Vector3<T> const &b  /**< right-hand operand */ );

  /** Component-wise multiplication operator - vector*vector
   * \return component-wise product of vectors
   */
  template< typename T >
  Vector3<T> operator*( Vector3<T> const &a, /**< left-hand operand */
                        Vector3<T> const &b  /**< right-hand operand */ );

  /** Multiplication operator scalar*vector
   * \return product of vector and scalar
   */
  template< typename T, typename S >
  Vector3<T> operator*( S a,                 /**< left-hand operand */
                        Vector3<T> const &b  /**< right-hand operand */ );

  /** Multiplication operator vector*scalar
   * \return product of vector and scalar
   */
  template< typename T, typename S >
  Vector3<T> operator*( Vector3<T> const &a, /**< left-hand operand */
                        S b                  /**< right-hand operand */ );

  /** Component-wise division operator - vector/vector
   * \return component-wise result of vector division
   */
  template< typename T >
  Vector3<T> operator/( Vector3<T> const &a, /**< left-hand operand */
                        Vector3<T> const &b  /**< right-hand operand */ );

  /** Division operator vector/scalar
   * \return vector scaled by 1/b
   */
  template< typename T, typename S >
  Vector3<T> operator/( Vector3<T> const &a,  /**< left-hand operand */
                        S b                   /**< right-hand operand */ );

  /** Equals operator
   * \return TRUE if a == b
   */
  template< typename T >
  A3M_BOOL operator==( Vector3<T> const &a, /**< left-hand operand */
                       Vector3<T> const &b  /**< right-hand operand */ );

  /** Not-equal operator
   * \return TRUE if a != b
   */
  template< typename T >
  A3M_BOOL operator!=( Vector3<T> const &a, /**< left-hand operand */
                       Vector3<T> const &b  /**< right-hand operand */ );

  /** Find length (magnitude) of vector.
   * \return length of vector
   */
  template< typename T >
  T length( Vector3<T> const &v /**< vector */ );

  /** Find length^2 of vector.
   * Useful for comparing two vector lengths where the actual length values are
   * not required, saving expensive sqrt calculations.
   * \return length^2 of vector
   */
  template< typename T >
  T lengthSquared( Vector3<T> const &v /**< vector */);

  /** Make vector with the same direction as v with unit length.
   * \return normalized vector
   */
  template< typename T >
  Vector3<T> normalize( Vector3<T> const &v /**< vector */ );

  /** Find cross product of two vectors
   * \return cross product
   */
  template< typename T >
  Vector3<T> cross( Vector3<T> const &a, /**< left-hand operand */
                    Vector3<T> const &b  /**< right-hand operand */ );

  /** Find dot product of two vectors
   * \return dot product
   */
  template< typename T >
  T dot( Vector3<T> const &a, /**< left-hand operand */
         Vector3<T> const &b  /**< right-hand operand */ );


} /* namespace a3m */



/******************************************************************************
 * Implementation
 ******************************************************************************/
/*
 * Include vector2 and vector4 header here to prevent circular dependencies.
 * a3math_vector4.h must be included from outside namespace a3m or it will
 * attempt to nest another namespace a3m inside this one.
 */
#include <a3m/vector2.h> /* for Vector2 type */
#include <a3m/vector4.h> /* for Vector4 type */
namespace a3m
{
  /*
   * Origin constant.
   */
  template< typename T >
  const Vector3<T> Vector3<T>::ZERO = Vector3<T>((T)0.0, (T)0.0, (T)0.0);

  /*
   * X axis constant.
   */
  template< typename T >
  const Vector3<T> Vector3<T>::X_AXIS = Vector3<T>((T)1.0, (T)0.0, (T)0.0);

  /*
   * Y axis constant.
   */
  template< typename T >
  const Vector3<T> Vector3<T>::Y_AXIS = Vector3<T>((T)0.0, (T)1.0, (T)0.0);

  /*
   * Z axis constant.
   */
  template< typename T >
  const Vector3<T> Vector3<T>::Z_AXIS = Vector3<T>((T)0.0, (T)0.0, (T)1.0);

  /*
   * Default constructor. components are initialised to zero
   */
  template< typename T >
  Vector3<T>::Vector3() : x(T(0)), y(T(0)), z(T(0))
  {
  }

  /*
   * Constructor taking initialisers for each component
   */
  template< typename T >
  Vector3<T>::Vector3( T x, T y, T z )
  : x(x), y(y), z(z)
  {
  }

  /*
   * Constructor taking Vector2 and z component
   */
  template< typename T >
  Vector3<T>::Vector3( Vector2<T> const &v, T z )
  : x( v.x ), y( v.y ), z( z )
  {
  }

  /*
   * Constructor taking Vector4
   */
  template< typename T >
  Vector3<T>::Vector3( Vector4<T> const &v )
  : x( v.x ), y( v.y ), z( v.z )
  {
  }

  /*
   * Assignment add
   */
  template< typename T >
  Vector3<T> &Vector3<T>::operator+=( Vector3<T> const &b )
  {
    x += b.x; y += b.y; z += b.z;
    return *this;
  }

  /*
   * Assignment subtract
   */
  template< typename T >
  Vector3<T> &Vector3<T>::operator-=( Vector3<T> const &b )
  {
    x -= b.x; y -= b.y; z -= b.z;
    return *this;
  }

  /*
   * Assignment component-wise multiply
   */
  template< typename T >
  Vector3<T> &Vector3<T>::operator*=( Vector3<T> const &b )
  {
    x *= b.x; y *= b.y; z *= b.z;
    return *this;
  }

  /*
   * Assignment scale
   */
  template< typename T > template< typename O >
  Vector3<T> &Vector3<T>::operator*=( O b )
  {
    x = T( b * x ); y = T( b * y ); z = T( b * z );
    return *this;
  }

  /*
   * Assignment component-wise divide
  template< typename T >
  Vector3<T> &Vector3<T>::operator/=( Vector3<T> const &b )
  {
    x /= b.x; y /= b.y; z /= b.z;
    return *this;
  }
   */

  /*
   * Assignment scale
  template< typename T > template< typename O >
  Vector3<T> &Vector3<T>::operator/=( O b )
  {
    return (*this = T(1) / b);
  }
   */

  /*
   * Unary negation operator
   */
  template< typename T >
  Vector3<T> Vector3<T>::operator-() const
  {
    return Vector3<T>( -x, -y, -z );
  }


  /*
   * Component access
   */
  template< typename T >
  T &Vector3<T>::operator[]( A3M_INT32 i )
  {
    return *( &x + i );
  }

  /*
   * Constant component access
   */
  template< typename T >
  T const &Vector3<T>::operator[]( A3M_INT32 i ) const
  {
    return *( &x + i );
  }

  /*
   * Addition operator
   */
  template< typename T >
  Vector3<T> operator+( Vector3<T> const &a, Vector3<T> const &b )
  {
    return Vector3<T>( a.x + b.x, a.y + b.y, a.z + b.z );
  }

  /*
   * Subtraction operator
   */
  template< typename T >
  Vector3<T> operator-( Vector3<T> const &a, Vector3<T> const &b )
  {
    return Vector3<T>( a.x - b.x, a.y - b.y, a.z - b.z );
  }

  /*
   * Multiplication operator
   */
  template< typename T >
  Vector3<T> operator*( Vector3<T> const &a, Vector3<T> const &b )
  {
    return Vector3<T>( a.x * b.x, a.y * b.y, a.z * b.z );
  }

  /*
   * Multiplication operator (by scalar)
   */
  template< typename T, typename S >
  Vector3<T> operator*( S a, Vector3<T> const &b )
  {
    return Vector3<T>( a * b.x, a * b.y, a * b.z );
  }

  /*
   * Multiplication operator (by scalar)
   */
  template< typename T, typename S >
  Vector3<T> operator*( Vector3<T> const &a, S b )
  {
    return Vector3<T>( a.x * b, a.y * b, a.z * b );
  }

  /*
   * Division operator
   */
  template< typename T >
  Vector3<T> operator/( Vector3<T> const &a, Vector3<T> const &b )
  {
    return Vector3<T>( a.x / b.x, a.y / b.y, a.z / b.z );
  }

  /*
   * Division operator (by scalar)
   */
  template< typename T, typename S >
  Vector3<T> operator/( Vector3<T> const &a, S b )
  {
    return Vector3<T>( a.x / b, a.y / b, a.z / b );
  }

  /*
   * Equals operator
   */
  template< typename T >
  A3M_BOOL operator==( Vector3<T> const &a, Vector3<T> const &b )
  {
    return ( a.x == b.x ) && ( a.y == b.y ) && ( a.z == b.z );
  }

  /*
   * Not-equal operator
   */
  template< typename T >
  A3M_BOOL operator!=( Vector3<T> const &a, Vector3<T> const &b )
  {
    return !(a == b);
  }

  /*
   * Find length of vector
   */
  template< typename T >
  T length( Vector3<T> const &v )
  {
    return T( std::sqrt( dot( v, v ) ) );
  }

  /*
   * Find length^2 of vector
   */
  template< typename T >
  T lengthSquared( Vector3<T> const &v /**< vector */ )
  {
    return T( dot( v, v ) );
  }

  /*
   * Normalize vector
   */
  template< typename T >
  Vector3<T> normalize( Vector3<T> const &v )
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
   * Get cross product of two vectors
   */
  template< typename T >
  Vector3<T> cross( Vector3<T> const &a, Vector3<T> const &b )
  {
    return Vector3<T>( a.y * b.z - b.y * a.z,
                       b.x * a.z - a.x * b.z,
                       a.x * b.y - b.x * a.y );
  }

  /*
   * Get dot product of two vectors
   */
  template< typename T >
  T dot( Vector3<T> const &a, Vector3<T> const &b )
  {
    return a.x * b.x + a.y * b.y + a.z * b.z;
  }

  /** @} */

} /* namespace a3m */

#endif /* A3MATH_VECTOR3_H */
