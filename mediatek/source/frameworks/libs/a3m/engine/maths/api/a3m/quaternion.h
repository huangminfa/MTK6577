/**************************************************************************
 *
 * Copyright (c) 2012 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 ***************************************************************************/
/** \file
 * Quaternion maths functions
 */
#pragma once
#ifndef A3M_QUATERNION_H
#define A3M_QUATERNION_H

#include <algorithm>          /* for std::max */
#include <cmath>              /* for copysign */

#include <a3m/angle.h>        /* for Angle */
#include <a3m/base_types.h>   /* for A3M_FLOAT */
#include <a3m/fpmaths.h>      /* for cos and sin */
#include <a3m/matrix4.h>      /* for a3m::Matrix4 */

namespace a3m
{
  /** \defgroup  a3mQuaternion Quaternions
   *  \ingroup   a3mRefMaths
   *  @{
   *
   * Quaternions are a handy way of representing rotations in 3D space (the
   * most common alternatives being matrices or Euler angles).  A quaternion is
   * essentially an axis, represented by a 3D vector, and an angle, which
   * represents a rotation around the axis.  Quaternions avoid the problem of
   * gimble lock, which occurs when using Euler angles, while allowing
   * rotations to be combined using multiplication, in the same fashion as with
   * matrices.
   *
   * Mathematically, quaternions are an extension to complex numbers, and are
   * represented here by components (a, b, c, d) alternatively represented as:
   * "a + bi + cj + dk", where "a" is the scalar component.  In literature,
   * these are often represented as (w, x, y, z) or (x, y, z, w), where "w" is
   * the scalar component, but this notation is not used here to avoid confusion
   * with the Vector4 type.
   *
   */

  template< typename T >
  struct Vector3;

  /** Class representing a quaternion.
   * The class supports addition, subtraction, multiplication.
   */
  template< typename T >
  struct Quaternion
  {
    /** Type of each component */
    typedef T Scalar;

    /** Zero quaternion */
    static Quaternion<T> const ZERO;
    /** Identity quaternion */
    static Quaternion<T> const IDENTITY;

    /** Constructor initialising zero-rotation (identity) quaternion */
    Quaternion();
    /** Constructor taking initialisers for each component */
    Quaternion( T a, T b, T c, T d );
    /** Constructs quaternion from a scalar and vector */
    Quaternion( T a, Vector3<T> const &bcd );
    /** Constructor taking axis and angle */
    Quaternion( Vector3<T> const &axis, Angle<T> const& angle );
    /** Constructor taking rotation matrix */
    Quaternion( Matrix4<T> const &mat );

    /** Assignment add operator.
     * \return reference to this quaternion*/
    Quaternion<T> &operator+=( /** quaternion to add */Quaternion<T> const &q );

    /** Assignment subtraction operator.
     * \return reference to this quaternion*/
    Quaternion<T> &operator-=( /** quaternion to subtract */
                               Quaternion<T> const &q  );

    /** Assignment multiplication operator
     * \return reference to this quaternion*/
    Quaternion<T> &operator*=( /** quaternion number by which to multiply */
                                Quaternion<T> const &q  );

    /** Assignment multiplication by scalar operator.
     * \return reference to this quaternion*/
    template< typename S >
    Quaternion<T> &operator*=( /** scalar number by which to multiply */ S k );

    /** Assignment division by scalar operator.
     * \return reference to this quaternion*/
    template< typename S >
    Quaternion<T> &operator/=(/** scalar number by which to divide */ S k );

    /** Unary negation operator.
     * \return quaternion with equal magnitude but opposite direction*/
    Quaternion<T> operator-() const;

    /** Index operator. Returns a reference to a quaternion component
     * \return reference to component
     */
    T &operator[]( /** index of component in the range [0,3] */ A3M_INT32 i );

    /** Index operator. Returns a constant reference to a quaternion component
     * \return constant reference to component
     */
    T const &operator[]( /** index of component in the range [0,3] */
                         A3M_INT32 i ) const;

    T a /**< a (scalar) component */;
    T b /**< b component */;
    T c /**< c component */;
    T d /**< d component */;
  };

  /** Specialisation for float */
  typedef Quaternion< A3M_FLOAT > Quaternionf;

  /** Addition operator
   * \return sum of quaternions
   */
  template< typename T >
  Quaternion<T> operator+( Quaternion<T> const &p,/**< LHS of operator */
                           Quaternion<T> const &q /**< RHS of operator */ );

  /** Subtraction operator
   * \return difference of quaternions
   */
  template< typename T >
  Quaternion<T> operator-( Quaternion<T> const &p,/**< LHS of operator */
                           Quaternion<T> const &q /**< RHS of operator */ );

  /** Multiplication operator - quaternion * quaternion
   * \return component-wise product of quaternions
   */
  template< typename T >
  Quaternion<T> operator*( Quaternion<T> const &p,/**< LHS of operator */
                           Quaternion<T> const &q /**< RHS of operator */ );

  /** Multiplication operator scalar * quaternion
   * \return product of quaternion and scalar
   */
  template< typename T, typename S >
  Quaternion<T> operator*( S k,                   /**< Scalar operand */
                           Quaternion<T> const &q /**< Quaternion operand */ );

  /** Multiplication operator quaternion * scalar
   * \return product of quaternion and scalar
   */
  template< typename T, typename S >
  Quaternion<T> operator*( Quaternion<T> const &q, /**< Quaternion operand */
                           S k                     /**< Scalar operand */ );

  /** Division operator quaternion / scalar
   * \return quaternion scaled by 1 / k
   */
  template< typename T, typename S >
  Quaternion<T> operator/( Quaternion<T> const &q, /**< Quaternion operand */
                           S k                     /**< Scalar operand */ );

  /** Equals operator
   * \return TRUE if a == b
   */
  template< typename T >
  A3M_BOOL operator==( Quaternion<T> const &p, /**< left-hand operand */
                       Quaternion<T> const &q  /**< right-hand operand */ );

  /** Not-equal operator
   * \return TRUE if a != b
   */
  template< typename T >
  A3M_BOOL operator!=( Quaternion<T> const &p, /**< left-hand operand */
                       Quaternion<T> const &q  /**< right-hand operand */ );

  /** Find the absolute value (aka the norm or modulus) of quaternion:
    \code
    abs(Quaternion(a, b, c, d)) == sqrt(a^2 + b^2 + c^2 + d^2)
    abs(q) == sqrt(q * conjugate(q))
    \endcode
   * \return absolute value of quaternion
   */
  template< typename T >
  T abs( Quaternion<T> const &q /**< the given quaterion */ );

  /** Find the square of the absolute value of the quaternion.
    If you only need the square of the absolute value, it is quicker to
    calculate this directly than to square the absolute value.
   * \return square of the absolute value of quaternion
   */
  template< typename T >
  T abs2( Quaternion<T> const &q /**< the given quaterion */ );

  /** Returns the conjugate of the quaternion.
   * conjugated(Quaternion(a, b, c, d)) == Quaternion(a, -b, -c, -d)
   * \return conjugate of the quaternion
   */
  template< typename T >
  Quaternion<T> conjugate( Quaternion<T> const &q /**< the given quaterion */ );

  /** Make a quaternion which represents the same rotation but has an absolute
   * value of 1.
   * \return normalized quaternion
   */
  template< typename T >
  Quaternion<T> normalize( Quaternion<T> const &q /**< the given quaterion */ );

  /** Make a quaternion which is the multiplicative inverse (reciprocal) of q.
   * Use the result to perform quaternion division, which is ambiguous since
   * quaternion multiplication is non-commutative:
   * (q * inverse(p) != inverse(p) * q)
   * \return inverse of the quaternion
   */
  template< typename T >
  Quaternion<T> inverse( Quaternion<T> const &q /**< the quaterion to invert */ );

  /** Find dot product of two quaternions
   * \return dot product
   */
  template< typename T >
  T dot( Quaternion<T> const &p, /**< quaterion multiplicand */
         Quaternion<T> const &q  /**< quaterion multiplicand */ );

  /** Make quaternion from an axis angle pair.
   * Angle is around the axis in 'right-hand-rule' direction.  (Use your right
   * hand "thumbs up", fingers indicate rotation direction around 'thumb'
   * axis.)
   *
   * \return Quaternion describing rotation of angle around the axis.
   */
  template< typename T >
  Quaternion<T> toQuaternion( Vector3<T> const &axis,
                              /**< Vector descrbing rotation axis */
                              Angle<T> const& angle
                              /**< Rotation around axis */ );

  /** Sets value of an axis angle pair from a quaternion.
   */
  template< typename T >
  void toAxisAngle( Vector3<T> &axis,      /**< [out] Rotation axis */
                    Angle<T> &angle,       /**< [out] Angle */
                    Quaternion<T> const &q /**< [in] Quaterion */);

  /** Make quaternion from a rotation matrix.
   * \return Quaternion describing rotation described by matrix.
   */
  template< typename T >
  Quaternion<T> toQuaternion( Matrix4<T> const &mat /**< 4x4 Rotation matrix */ );

  /** Makes a rotation matrix from a quaternion.
   * \return Matrix describing rotation described by quaternion.
   */
  template< typename T >
  Matrix4<T> toMatrix4( Quaternion<T> const &q /**< Original Quaterion */ );

  /** Make a quaternion that rotates between two vectors.
   * \return Quaternion representing rotation between the vectors
   */
  template< typename T >
  Quaternion<T> toQuaternion(
      Vector3<T> const &from,
      /**< Vector from which rotation starts */
      Vector3<T> const &to
      /**< Vector at which rotation ends */);

  /** @} */

} /* namespace a3m */


/******************************************************************************
 * Implementation
 ******************************************************************************/

namespace a3m
{
  /*
   * Zero quaternion.
   */
  template<typename T>
  Quaternion<T> const Quaternion<T>::ZERO =
      Quaternion<T>((T)0.0, (T)0.0, (T)0.0, (T)0.0);

  /*
   * Identity quaternion.
   */
  template<typename T>
  Quaternion<T> const Quaternion<T>::IDENTITY = Quaternion<T>();

  /********************
   * Member functions *
   ********************/

  template< typename T >
  Quaternion<T>::Quaternion() :
    a(1), b(0), c(0), d(0)
  {
  }

  template< typename T >
  Quaternion<T>::Quaternion( T a, T b, T c, T d ) :
    a(a), b(b), c(c), d(d)
  {
  }

  template< typename T >
  Quaternion<T>::Quaternion( T a, Vector3<T> const &bcd ) :
    a(a), b(bcd.x), c(bcd.y), d(bcd.z)
  {
  }

  template< typename T >
  Quaternion<T>::Quaternion( Vector3<T> const &axis, Angle<T> const &angle )
  {
    *this = toQuaternion(axis, angle);
  }

  template< typename T >
  Quaternion<T>::Quaternion( Matrix4<T> const &mat )
  {
    *this = toQuaternion(mat);
  }

  template< typename T >
  Quaternion<T> &Quaternion<T>::operator+=( Quaternion<T> const &q )
  {
    a += q.a;
    b += q.b;
    c += q.c;
    d += q.d;

    return *this;
  }

  template< typename T >
  Quaternion<T> &Quaternion<T>::operator-=( Quaternion<T> const &q )
  {
    a -= q.a;
    b -= q.b;
    c -= q.c;
    d -= q.d;

    return *this;
  }

  template< typename T >
  Quaternion<T> &Quaternion<T>::operator*=( Quaternion<T> const &q )
  {
    *this = *this * q;
    return *this;
  }

  template< typename T >
  template< typename S >
  Quaternion<T> &Quaternion<T>::operator*=( S k )
  {
    a *= k;
    b *= k;
    c *= k;
    d *= k;

    return *this;
  }

  template< typename T >
  template< typename S >
  Quaternion<T> &Quaternion<T>::operator/=( S k )
  {
    a /= k;
    b /= k;
    c /= k;
    d /= k;

    return *this;
  }

  template< typename T >
  Quaternion<T> Quaternion<T>::operator-() const
  {
    return Quaternion<T>(-a, -b, -c, -d);
  }

  template< typename T >
  T &Quaternion<T>::operator[]( A3M_INT32 i )
  {
    return *(&a + i);
  }

  template< typename T >
  T const & Quaternion<T>::operator[]( A3M_INT32 i ) const
  {
    return *(&a + i);
  }

  /******************
   * Free functions *
   ******************/

  template< typename T >
  Quaternion<T> operator+( Quaternion<T> const &p, Quaternion<T> const &q )
  {
    return Quaternion<T>(p.a + q.a, p.b + q.b, p.c + q.c, p.d + q.d);
  }

  template< typename T >
  Quaternion<T> operator-( Quaternion<T> const &p, Quaternion<T> const &q )
  {
    return Quaternion<T>(p.a - q.a, p.b - q.b, p.c - q.c, p.d - q.d);
  }

  template< typename T >
  Quaternion<T> operator*( Quaternion<T> const &p, Quaternion<T> const &q )
  {
    return Quaternion<T>(
      (p.a * q.a) - (p.b * q.b) - (p.c * q.c) - (p.d * q.d),
      (p.a * q.b) + (p.b * q.a) + (p.c * q.d) - (p.d * q.c),
      (p.a * q.c) - (p.b * q.d) + (p.c * q.a) + (p.d * q.b),
      (p.a * q.d) + (p.b * q.c) - (p.c * q.b) + (p.d * q.a)
    );
  }

  template< typename T, typename S >
  Quaternion<T> operator*( S k, Quaternion<T> const &q )
  {
    return Quaternion<T>(q.a * k, q.b * k, q.c * k, q.d * k);
  }

  template< typename T, typename S >
  Quaternion<T> operator*( Quaternion<T> const &q, S k )
  {
    return Quaternion<T>(q.a * k, q.b * k, q.c * k, q.d * k);
  }

  template< typename T, typename S >
  Quaternion<T> operator/( Quaternion<T> const &q, S k )
  {
    return Quaternion<T>(q.a / k, q.b / k, q.c / k, q.d / k);
  }

  template< typename T >
  A3M_BOOL operator==( Quaternion<T> const &p, Quaternion<T> const &q )
  {
    return (p.a == q.a) && (p.b == q.b) && (p.c == q.c) && (p.d == q.d);
  }

  template< typename T >
  A3M_BOOL operator!=( Quaternion<T> const &p, Quaternion<T> const &q )
  {
    return !(p == q);
  }

  template< typename T >
  T abs( Quaternion<T> const &q )
  {
    return std::sqrt(abs2(q));
  }

  template< typename T >
  T abs2( Quaternion<T> const &q )
  {
    return dot(q, q);
  }

  template< typename T >
  Quaternion<T> conjugate( Quaternion<T> const &q )
  {
    return Quaternion<T>(q.a, -q.b, -q.c, -q.d);
  }

  template< typename T >
  Quaternion<T> normalize( Quaternion<T> const &q )
  {
    // If quaternion is already normalized, we can avoid accumulating errors by
    // not normalizing again
    T absolute2 = abs2(q);
    if (absolute2 < T(0.99) || absolute2 > T(1.01))
    {
      return q / std::sqrt(absolute2);
    }
    else
    {
      return q;
    }
  }

  template< typename T >
  Quaternion<T> inverse( Quaternion<T> const &q )
  {
    return conjugate(q) / abs2(q);
  }

  template< typename T >
  T dot( Quaternion<T> const &p, Quaternion<T> const &q )
  {
    return (p.a * q.a) + (p.b * q.b) + (p.c * q.c) + (p.d * q.d);
  }

  template< typename T >
  Quaternion<T> toQuaternion( Vector3<T> const &axis, Angle<T> const &angle )
  {
    Angle<T> a = angle * T(0.5);
    T sinA = sin(a);
    return Quaternion<T>(
      cos(a),
      sinA * axis.x,
      sinA * axis.y,
      sinA * axis.z
    );
  }

  template< typename T >
  void toAxisAngle( Vector3<T> &axis, Angle<T> &angle, Quaternion<T> const &q )
  {
    if (abs(q.a) == 1)
    {
      // Any axis will do with no rotation: chose Y because it is usually "up"
      axis = Vector3<T>::Y_AXIS;
      angle = Angle<T>::ZERO;
    }
    else
    {
      T k = 1 / std::sqrt(1 - q.a * q.a);
      axis.x = q.b * k;
      axis.y = q.c * k;
      axis.z = q.d * k;
      angle = 2 * acos(q.a);
    }
  }

  template< typename T >
  Quaternion<T> toQuaternion( Matrix4<T> const &mat )
  {
    // This method is more accurate than the most commonly suggested method,
    // and may actually be more efficient on modern compilers.
    T a = std::sqrt(std::max(0.0f, 1 + mat[0][0] + mat[1][1] + mat[2][2])) / 2;
    T b = std::sqrt(std::max(0.0f, 1 + mat[0][0] - mat[1][1] - mat[2][2])) / 2;
    T c = std::sqrt(std::max(0.0f, 1 - mat[0][0] + mat[1][1] - mat[2][2])) / 2;
    T d = std::sqrt(std::max(0.0f, 1 - mat[0][0] - mat[1][1] + mat[2][2])) / 2;
    b = copysign(b, mat[1][2] - mat[2][1]);
    c = copysign(c, mat[2][0] - mat[0][2]);
    d = copysign(d, mat[0][1] - mat[1][0]);
    return Quaternion<T>(a, b, c, d);
  }

  template< typename T >
  Matrix4<T> toMatrix4( Quaternion<T> const &q )
  {
    T bb = q.b * q.b;
    T bc = q.b * q.c;
    T bd = q.b * q.d;
    T ba = q.b * q.a;

    T cc = q.c * q.c;
    T cd = q.c * q.d;
    T ca = q.c * q.a;

    T dd = q.d * q.d;
    T da = q.d * q.a;

    Matrix4<T> mat;

    mat[0][0] = 1 - 2 * (cc + dd);
    mat[1][0] = 2 * (bc - da);
    mat[2][0] = 2 * (bd + ca);

    mat[0][1] = 2 * (bc + da);
    mat[1][1] = 1 - 2 * (bb + dd);
    mat[2][1] = 2 * (cd - ba);

    mat[0][2] = 2 * (bd - ca);
    mat[1][2] = 2 * (cd + ba);
    mat[2][2] = 1 - 2 * (bb + cc);

    mat[3][0] = mat[3][1] = mat[3][2] = mat[0][3] = mat[1][3] = mat[2][3] = 0;
    mat[3][3] = 1;

    return mat;
  }

  template< typename T >
  Quaternion<T> toQuaternion( Vector3<T> const &from, Vector3<T> const &to )
  {
    static T const ZERO_THRESHOLD = T(0.0001);

    // We can easily construct a quaternion to rotate twice the angle between
    // vectors, so if we calculate the half vector, we can calculate the
    // rotation between vectors.
    Vector3<T> half = from + to;
    T halfLength = length(half);

    Quaternion<T> q;

    // In the special case where the angle between vectors is 180 degrees,
    // rotate by 180 degrees (assuming the inputs are normalized).
    if (halfLength < ZERO_THRESHOLD)
    {
      q = Quaternion<T>( T(0), T(0), T(1), T(0) );
    }
    else
    {
      half = half / halfLength;
      q = Quaternion<T>( dot( from, half ), cross( from, half ) );
    }

    return q;
  }

} /* namespace a3m */

#endif /* A3M_QUATERNION_H */

