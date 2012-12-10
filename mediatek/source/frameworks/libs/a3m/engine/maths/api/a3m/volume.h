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
 * Bounding volume types - may refactor at a later date.
 *
 */
#pragma once
#ifndef A3MATH_VOLUME_H
#define A3MATH_VOLUME_H

/******************************************************************************
* Include Files
******************************************************************************/
#include <a3m/plane.h>             // Planef
#include <a3m/base_types.h>           // A3M_FLOAT
#include <vector>                     // std::vector
#include <limits>                     // std::numeric_limits
#include <algorithm>                  // std::max

namespace a3m
{
  /** \defgroup a3mGeomVolume Bounding volumes.
      \ingroup  a3mCarefulNow

   @{
   */

  /**
   *Bounding Sphere
   */
  template< typename T >
  struct Sphere
  {
    /// Type of each vector component.
    typedef T Scalar;

    /// Local vector type.
    typedef Vector3<Scalar> Vector;

    /// The position of the centre of the sphere.
    Vector centre;

    /// Distance from the centre the sphere surface.
    Scalar radius;

    /// Construct the sphere from a centre position and radius.
    Sphere(Vector const& aPosition, Scalar aRadius);

    /// Compute the distance of the given point from the sphere.
    Scalar distanceFromPoint(Vector const& point) const;

    /** Intersect the given point with this sphere.
        \return       false if the point is outside the sphere, true otherwise.
        */
    A3M_BOOL intersects(Vector const& point /**< any point */) const;
  };

  /// Instantiated type for float.
  typedef Sphere<A3M_FLOAT> Spheref;


  /**
   * Represents a convex polygon such as a frustum.
   */
  template< typename T >
  struct ConvexHull
  {
    /// Type of each vector component
    typedef T Scalar;

    /// Local vector type.
    typedef Vector3<Scalar> Vector;

    /// The planes!
    std::vector< Plane< T > > planes;

    /// Default constructor is empty.
    ConvexHull(){}

    /// Construct convex hull from a series of planes.
    ConvexHull(std::vector< Plane< T > > const& thePlanes);

    /// Compute the distance of the given point from the convex hull.
    Scalar distanceFromPoint(Vector const& point) const;

    /** Intersect the given point with this convex hull.
        \return       false if the point is outside the hull, true otherwise.
        */
    A3M_BOOL intersects(Vector const& point /**< any point */) const;
  };

  /// Instantiated type for float.
  typedef ConvexHull<A3M_FLOAT> ConvexHullf;

  /** Intersect the given convex hull and the sphere.
      \param convexHull any ConvexHull object,
      \param sphere     any Sphere object,
      \return false if the volumes do not intersect, true otherwise.
      */
  template<typename T >
  A3M_BOOL intersect(ConvexHull<T> const& convexHull, Sphere<T> const& sphere);

  /** Intersect the given convex hull and the sphere.
      \param sphere     any Sphere object,
      \param convexHull any ConvexHull object,
      \return false if the volumes do not intersect, true otherwise.
      */
  template<typename T >
  A3M_BOOL intersect(Sphere<T> const& sphere, ConvexHull<T> const& convexHull);

  /** @} */

} /* namespace a3m */

/******************************************************************************
 * Implementation
 ******************************************************************************/
namespace a3m
{
  template< typename T >
  Sphere<T>::Sphere(Vector const& aPosition, Scalar aRadius) :
    centre(aPosition), radius(aRadius)
  {}

  template< typename T >
  T Sphere<T>::distanceFromPoint(Vector const& point) const
  {
    Vector centreToPoint(point);
    centreToPoint -= centre;

    return length(centreToPoint) - radius;
 };

  template< typename T >
  A3M_BOOL Sphere<T>::intersects(Vector const& point) const
  {
    Vector centreToPoint(point);
    centreToPoint -= centre;

    const Scalar lengthSquared = dot(centreToPoint, centreToPoint);
    const Scalar radiusSquared = radius * radius;

    return (lengthSquared <= radiusSquared);
  }

  template< typename T >
  ConvexHull<T>::ConvexHull(std::vector< Plane< T > > const& thePlanes) :
    planes(thePlanes)
  {}

  template< typename T >
  T ConvexHull<T>::distanceFromPoint(Vector const& point) const
  {
    /* Find the maximum signed distance from any plane to the point.
        Points lying within the hull have distances < 0
        Points lying on the hull have distance <= 0
        Points lying outside the hull have positive & negative distances.
        */
    Scalar maxDistance = -std::numeric_limits<Scalar>::max();

    typedef typename std::vector< Plane< T > >::const_iterator Itr;
    for ( Itr it = planes.begin(); it != planes.end(); ++it )
    {
        const Scalar distance = it->distanceFromPoint(point);
        maxDistance = std::max(distance, maxDistance);
    }

    return maxDistance;
  }

  template< typename T >
  A3M_BOOL ConvexHull<T>::intersects(Vector const& point) const
  {
    for ( std::vector<Planef>::const_iterator it = planes.begin();
          it != planes.end();
          ++it)
    {
        // If outside any plane, point is outside the entire hull.
        const Scalar distance = it->distanceFromPoint(point);
        if (distance > Scalar(0))
          return false;
    }

    return true;
  }

  /// Intersect the given convex hull and the sphere.
  template< typename T >
  A3M_BOOL intersects(ConvexHull<T> const& convexHull, Sphere<T> const& sphere)
  {
    for (std::vector<Planef>::const_iterator it = convexHull.planes.begin();
         it != convexHull.planes.end();
         ++it)
    {
      const T distance = it->distanceFromPoint(sphere.centre);
      if (distance > sphere.radius)
        return false;
    }

    return true;
  }

  /// Intersect the given convex hull and the sphere.
  template< typename T >
  A3M_BOOL intersects(Sphere<T> const& sphere, ConvexHull<T> const& convexHull)
  {
    return intersects(sphere, convexHull);
  }


} /* namespace a3m */

#endif /* A3MATH_VOLUME_H */
