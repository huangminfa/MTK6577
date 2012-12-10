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
 * Plane equation class a3m::Plane
 */
#pragma once
#ifndef A3MATH_PLANE_H
#define A3MATH_PLANE_H

#include <a3m/vector3.h>   // Vector3
#include <a3m/base_types.h>   // A3M_FLOAT
#include <a3m/assert.h>       // assert

namespace a3m
{
  /** \defgroup a3mGeomPlane Planes.
      \ingroup  a3mCarefulNow
   @{
   */

  /**
   * Planes are created from a triangle or a position and
   * direction vector. Represents plane equation Ax + By + Cz + D = 0.
   */
  template< typename T >
  class Plane
  {
  public:
    /// Type of each vector component.
    typedef T Scalar;

    /// Local vector type.
    typedef Vector3<Scalar> Vector;

    /// Default plane is located at the origin and faces +Z axis.
    Plane();

    /** Plane from a point and direction. If the normal is not unit
        length then the plane is not normalized.
        */
    Plane(
      Vector const& position /**< any position vector */,
      Vector const& normal /**< direction vector - need not be normalised */);

    /// Plane formed by a triangle.
    Plane(
      Vector const& pointA /**< 1st point */,
      Vector const& pointB /**< 2nd point */,
      Vector const& pointC /**< 3rd point */,
      A3M_BOOL normalize = true
        /**< true if the computed normal vector is to be normalised
             to unit length, false otherwise */);

    // Compiler generated copy constructor, assignment operator & destructor.

    /// Ensure this plane has unit length normal.
    void normalize();

    /// Retrieve the normal vector - this may be none-unit length.
    Vector const& normal() const;

    /// Retrieve the distance of the plane from the origin.
    Scalar distance() const;

    /// Set the place facing direction.
    void setNormal(Vector const& normal /**< need not be normalized */);

    /// Set the plane's distance from the origin.
    void setDistance(Scalar distance /**< the distance from the origin */);

    /** Plane from a point and direction. If the normal is not unit
        length then the plane is not normalized.
        */
    void set(
      Vector const& position /**< A point on the plane */,
      Vector const& normal   /**< direction vector - need not be normalised */);

    /// Plane formed by a triangle.
    void set(
      Vector const& pointA /**< 1st point */,
      Vector const& pointB /**< 2nd point */,
      Vector const& pointC /**< 3rd point */,
      A3M_BOOL normalize = true
        /**< true if the computed normal vector is to be normalised
             to unit length, false otherwise */);

    /** Evaluate the given point wrt to the plane.
        \return       the distance of the point from the plane.
                      Positive values indicate the point is on the
                      normal or front side of the plane. Negative
                      values are behind the plane. If the plane is
                      not normalized only the sign of the result is valid.
                      */
    Scalar distanceFromPoint(
      Vector const& point /**< any point in space */) const;

  private:
    /// Unit direction vector.
    Vector m_normal;

    /// Distance from origin.
    Scalar m_distance;
  };

  /// Instantiation for float.
  typedef Plane< A3M_FLOAT > Planef;

  /** @} */

} /* namespace a3m */


/******************************************************************************
 * Implementation
 ******************************************************************************/
namespace a3m
{
  template< typename T >
  Plane<T>::Plane() :
    m_normal(Scalar(0), Scalar(0), Scalar(1)),
    m_distance(Scalar(0))
  {}

  template< typename T >
  Plane<T>::Plane(Vector const& position, Vector const& normal)
    // : Uninitialised
  {
    set(position, normal);
  }

  template< typename T >
  Plane<T>::Plane(
    Vector const& pointA, Vector const& pointB, Vector const& pointC,
    A3M_BOOL normalize)
    // : Uninitialised
  {
    set(pointA, pointB, pointC, normalize);
  }

  template< typename T >
  void Plane<T>::normalize()
  {
    const Scalar len = length(m_normal);
    m_normal /= len;
    m_distance /= len;
  }

  template< typename T >
  typename Plane<T>::Vector const& Plane<T>::normal() const
  {
    return m_normal;
  }

  template< typename T >
  T Plane<T>::distance() const
  {
    return m_distance;
  }

  template< typename T >
  void Plane<T>::setNormal(Vector const& normal)
  {
    m_normal = normal;
  }

  template< typename T >
  void Plane<T>::setDistance(Scalar distance)
  {
    m_distance = distance;
  }

  template< typename T >
  void Plane<T>::set(Vector const& position, Vector const& normal)
  {
    m_normal = normal;
    m_distance = dot(m_normal, position);
  }

  template< typename T >
  void Plane<T>::set(
    Vector const& pointA, Vector const& pointB, Vector const& pointC,
    A3M_BOOL normalize)
  {
    // Edge vectors leading from A.
    Vector ab(pointB);
    ab -= pointA;

    Vector ac(pointC);
    ac -= pointA;

    // Calculate normal to edges.
    m_normal = cross(ab, ac);

    static const Vector ZERO_VECTOR(Scalar(0), Scalar(0), Scalar(0));
    A3M_ASSERT((m_normal != ZERO_VECTOR) && "Points must not be in a line");

    if (normalize)
      m_normal = a3m::normalize(m_normal);

    m_distance = dot(m_normal, pointA);
  }

  template< typename T >
  T Plane<T>::distanceFromPoint(Vector const& point) const
  {
    return dot(m_normal, point) - m_distance;
  }

  /** @} */

} /* namespace a3m */

#endif /* A3MATH_PLANE_H */
