/**************************************************************************
 *
 * Copyright (c) 2010 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 **************************************************************************
 *   $Id: //swd1_mm/projects/a3m_android/jni/a3m/a3math/api/a3math_vector2.h#1 $
 *   $Revision: #1 $
 *   $DateTime: 2011/09/20 17:04:16 $
 ***************************************************************************/
/** \file
 * 2D Vector maths functions
 */
#pragma once
#ifndef A3MATH_VECTOR2_H
#define A3MATH_VECTOR2_H

#include <cmath> /* for sqrt */
#include <a3m/base_types.h>  /* for A3M_FLOAT */

namespace a3m
{
  /** \defgroup  a3mVector2 2D Vector class
   *  \ingroup   a3mVector
   *
   *  x-y vector
   *  @{
   */

  template< typename T >
  struct Vector3;

  /** Class representing a 2d vector.
   * The class supports addition, subtraction and member-wise multiplication.
   */
  template< typename T >
  struct Vector2
  {
    /** Type of each vector component */
    typedef T Scalar;

    /** The origin */
    static const Vector2<T> ZERO;
    /** The x axis */
    static const Vector2<T> X_AXIS;
    /** The y axis */
    static const Vector2<T> Y_AXIS;

    /** Default constructor. components are not initialised */
    Vector2(); // No default values for x & y
    /** Constructor taking initialisers for each component */
    Vector2( T x /**< x component */,  T y /**< y component */);
    /** Constructor taking Vector3 (z component is discarded) */
    explicit Vector2( Vector3<T> const &v );

    /** Assignment add operator.
     * \return reference to this vector*/
    Vector2<T> &operator+=( Vector2<T> const &b /**< vector to add */ );
    /** Assignment subtraction operator.
     * \return reference to this vector*/
    Vector2<T> &operator-=( Vector2<T> const &b
                            /**< vector to subtract */ );
    /** Assignment multiplication operator (component-wise by another vector).
     * \return reference to this vector*/
    Vector2<T> &operator*=( Vector2<T> const &b /**< vector to multiply
                                                     by */ );
    /** Assignment multiplication by scalar operator.
     * \return reference to this vector*/
    template< typename O >
    Vector2<T> &operator*=( O b /**< scalar number to multiply by */ );
    /** Unary negation operator.
     * \return vector with equal magnitude but opposite direction*/
    Vector2<T> operator-() const;

    /** Index operator. Returns a reference to a vector component
     * \return reference to component
     */
    T &operator[]( A3M_INT32 i /**< index of component in the
                                    range [0,1] */ );
    /** Index operator. Returns a constant reference to a vector component
     * \return constant reference to component
     */
    T const &operator[]( A3M_INT32 i /**< index of component in the
                                          range [0,1] */ ) const;

    T x /**< x component */;
    T y /**< y component */;
  };

  /** Specialisation for float */
  typedef Vector2< A3M_FLOAT > Vector2f;
  /** Specialisation for int */
  typedef Vector2< A3M_INT32 > Vector2i;

  /*
   * Origin constant.
   */
  template< typename T >
  const Vector2<T> Vector2<T>::ZERO = Vector2<T>((T)0.0, (T)0.0);

  /*
   * X axis constant.
   */
  template< typename T >
  const Vector2<T> Vector2<T>::X_AXIS = Vector2<T>((T)1.0, (T)0.0);

  /*
   * Y axis constant.
   */
  template< typename T >
  const Vector2<T> Vector2<T>::Y_AXIS = Vector2<T>((T)0.0, (T)1.0);

  /** Addition operator
   * \return sum of vectors
   */
  template< typename T >
  Vector2<T> operator+( Vector2<T> const &a, /**< left-hand operand */
                        Vector2<T> const &b  /**< right-hand operand */);

  /** Subtraction operator
   * \return difference of vectors
   */
  template< typename T >
  Vector2<T> operator-( Vector2<T> const &a, /**< left-hand operand */
                        Vector2<T> const &b  /**< right-hand operand */ );

  /** Component-wise multiplication operator - vector*vector
   * \return component-wise product of vectors
   */
  template< typename T >
  Vector2<T> operator*( Vector2<T> const &a, /**< left-hand operand */
                        Vector2<T> const & b /**< right-hand operand */ );

  /** Multiplication operator scalar*vector
   * \return product of vector and scalar
   */
  template< typename T, typename S >
  Vector2<T> operator*( S scale,             /**< left-hand operand */
                        Vector2<T> const &v  /**< right-hand operand */ );

  /** Multiplication operator vector*scalar
   * \return product of vector and scalar
   */
  template< typename T, typename S >
  Vector2<T> operator*( Vector2<T> const &v, /**< left-hand operand */
                        S scale              /**< right-hand operand */ );

  /** Component-wise division operator - vector/vector
   * \return component-wise result of vector division
   */
  template< typename T >
  Vector2<T> operator/( Vector2<T> const &a, /**< left-hand operand */
                        Vector2<T> const &b  /**< right-hand operand */ );

  /** Division operator vector/scalar
   * \return vector scaled by 1/b
   */
  template< typename T, typename S >
  Vector2<T> operator/( Vector2<T> const &a, /**< left-hand operand */
                        S b                  /**< right-hand operand */ );

  /** Equals operator
   * \return TRUE if a == b
   */
  template< typename T >
  A3M_BOOL operator==( Vector2<T> const &a, /**< left-hand operand */
                       Vector2<T> const &b  /**< right-hand operand */ );

  /** Not-equal operator
   * \return TRUE if a != b
   */
  template< typename T >
  A3M_BOOL operator!=( Vector2<T> const &a, /**< left-hand operand */
                       Vector2<T> const &b  /**< right-hand operand */ );

  /** Find length (magnitude) of vector.
   * \return length of vector
   */
  template< typename T >
  T length( Vector2<T> const &v /**< vector */);

  /** Find length^2 of vector.
   * Useful for comparing two vector lengths where the actual length values are
   * not required, saving expensive sqrt calculations.
   * \return length^2 of vector
   */
  template< typename T >
  T lengthSquared( Vector2<T> const &v /**< vector */);

  /** Make vector with the same direction as v with unit length.
   * \return normalized vector
   */
  template< typename T >
  Vector2<T> normalize( Vector2<T> const &v /**< vector */);

  /** Find dot product of two vectors
   * \return dot product
   */
  template< typename T >
  T dot( Vector2<T> const &a, /**< left-hand operand */
         Vector2<T> const &b  /**< right-hand operand */ );


  /** Test intersection of two lines.
   * Returns true if line segment ab intersects line segment cd
   * if the segments intersect t will be the fraction of
   * ab which lies before the intersection
   * \return
   */
  template< typename T >
  A3M_BOOL intersect( Vector2<T> const &a, /**< start of line segment ab */
                      Vector2<T> const &b, /**< end of line segment ab */
                      Vector2<T> const &c, /**< start of line segment cd */
                      Vector2<T> const &d, /**< end of line segment cd */
                      A3M_FLOAT &t  /**< [out] fraction of line segment ab
                                         before the intersection*/);

} /* namespace a3m */



/******************************************************************************
 * Implementation
 ******************************************************************************/
/*
 * Include vector3 header here to prevent circular dependencies.
 * a3math_vector3.h must be included from outside namespace a3m or it will
 * attempt to nest another namespace a3m inside this one.
 */
#include <a3m/vector3.h> /* for Vector3 type */
namespace a3m
{


  /*
   * Default constructor. components are initialised to zero
   */
  template< typename T >
  Vector2<T>::Vector2() : x(T(0)), y(T(0)) {}

  /*
   * Constructor taking initialisers for each component
   */
  template< typename T >
  Vector2<T>::Vector2( T x, T y )
  : x(x), y(y)
  {
  }
   /*
    * Constructor taking Vector3 (z component is discarded)
    */
  template< typename T >
  Vector2<T>::Vector2( Vector3<T> const &v )
  : x( v.x ), y( v.y )
  {
  }

  /*
   * Assignment add
   */
  template< typename T >
  Vector2<T> &Vector2<T>::operator+=( Vector2<T> const &b )
  {
    x += b.x; y += b.y;
    return *this;
  }

  /*
   * Assignment subtract
   */
  template< typename T >
  Vector2<T> &Vector2<T>::operator-=( Vector2<T> const &b )
  {
    x -= b.x; y -= b.y;
    return *this;
  }

  /*
   * Assignment component-wise multiply
   */
  template< typename T >
  Vector2<T> &Vector2<T>::operator*=( Vector2<T> const &b )
  {
    x *= b.x; y *= b.y;
    return *this;
  }

  /*
   * Assignment scale
   */
  template< typename T > template< typename O >
  Vector2<T> &Vector2<T>::operator*=( O b )
  {
    x = T( b * x ); y = T( b * y );
    return *this;
  }

  /*
   * Unary negation operator
   */
  template< typename T >
  Vector2<T> Vector2<T>::operator-() const
  {
    return Vector2<T>( -x, -y );
  }

  /*
   * Component access
   */
  template< typename T >
  T &Vector2<T>::operator[]( A3M_INT32 i ) {return *( &x + i );}

  /*
   * Constant component access
   */
  template< typename T >
  T const &Vector2<T>::operator[]( A3M_INT32 i ) const {return *( &x + i );}


  /*
   * Addition operator
   */
  template< typename T >
  Vector2<T> operator+( Vector2<T> const &a, Vector2<T> const &b )
  {
    return Vector2<T>( a.x + b.x, a.y + b.y );
  }

  /*
   * Subtraction operator
   */
  template< typename T >
  Vector2<T> operator-( Vector2<T> const &a, Vector2<T> const &b )
  {
    return Vector2<T>( a.x - b.x, a.y - b.y );
  }

  /*
   * Multiplication operator
   */
  template< typename T >
  Vector2<T> operator*( Vector2<T> const &a, Vector2<T> const &b )
  {
    return Vector2<T>( a.x * b.x, a.y * b.y );
  }

  /*
   * Multiplication operator (by scalar)
   */
  template< typename T, typename S >
  Vector2<T> operator*( S a, Vector2<T> const &b )
  {
    return Vector2<T>( a * b.x, a * b.y );
  }

  /*
   * Multiplication operator (by scalar)
   */
  template< typename T, typename S >
  Vector2<T> operator*( Vector2<T> const &a, S b )
  {
    return Vector2<T>( a.x * b, a.y * b );
  }

  /*
   * Division operator
   */
  template< typename T >
  Vector2<T> operator/( Vector2<T> const &a, Vector2<T> const &b )
  {
    return Vector2<T>( a.x / b.x, a.y / b.y );
  }

  /*
   * Division operator (by scalar)
   */
  template< typename T, typename S >
  Vector2<T> operator/( Vector2<T> const &a, S b )
  {
    return Vector2<T>( a.x / b, a.y / b );
  }

  /*
   * Equals operator
   */
  template< typename T >
  A3M_BOOL operator==( Vector2<T> const &a, Vector2<T> const &b )
  {
    return ( a.x == b.x ) && ( a.y == b.y );
  }

  /*
   * Not-equal operator
   */
  template< typename T >
  A3M_BOOL operator!=( Vector2<T> const &a, Vector2<T> const &b )
  {
    return !(a == b);
  }

  /*
   * Find length of vector
   */
  template< typename T >
  T length( Vector2<T> const &v )
  {
    return T( std::sqrt( dot( v, v ) ) );
  }

  /*
   * Find length^2 of vector
   */
  template< typename T >
  T lengthSquared( Vector2<T> const &v /**< vector */ )
  {
    return T( dot( v, v ) );
  }

  /*
   * Normalize vector
   */
  template< typename T >
  Vector2<T> normalize( Vector2<T> const &v )
  {
    T l = length(v);
    if( l > 0 )
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
  T dot( Vector2<T> const &a, Vector2<T> const &b )
  {
    return a.x * b.x + a.y * b.y;
  }

  /*
   * Returns true if line segment ab intersects line segment cd
   * if the segments intersect t will be the fraction of
   * ab which lies before the intersection
   */
  template< typename T >
  A3M_BOOL intersect( Vector2<T> const &a, Vector2<T> const &b,
                      Vector2<T> const &c, Vector2<T> const &d,
                      A3M_FLOAT &t )
  {
    T denominator = (b.x-a.x)*(d.y-c.y)-(b.y-a.y)*(d.x-c.x);

    if( denominator != 0 ) return false;

    T r = ((a.y-c.y)*(b.x-a.x)-(a.x-c.x)*(b.y-a.y)) / denominator;

    if( r < 0 || r > 1 ) return false;

    r = ((a.y-c.y)*(d.x-c.x)-(a.x-c.x)*(d.y-c.y)) / denominator;

    if( r < 0 || r > 1 ) return false;

    t = r;
    return true;
  }
  /** @} */

} /* namespace a3m */

#endif /* A3MATH_VECTOR2_H */
