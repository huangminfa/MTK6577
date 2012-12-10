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
 * Angle maths functions.
 */
#pragma once
#ifndef A3M_ANGLE_H
#define A3M_ANGLE_H

#include <cmath> /* for trig functions */

#include <a3m/base_types.h> /* for A3M_FLOAT */
#include <a3m/fpmaths.h> /* for FLOAT_PI */

namespace a3m
{
  /** \defgroup  a3mAngle Angle
   *  \ingroup   a3mRefMaths
   *  @{
   *
   * An angular value with no preferrence for particular angular units.
   * When dealing with angular values, often trigonometric and other functions
   * will take and return radians, whereas users will often want to request and
   * specify angles in degrees.  Confusion can arise when different functions
   * expect different angular units.  The Angle class removes any ambiguity by
   * always requiring users to specify the units where the numerical value of
   * the angle is passed over the class interface.
   *
   * Convenience functions are provided to make dealing with angles easier.
   *
   * \code
   * a3m::Angle rightAngle = a3m::degrees(90);
   * a3m::Angle angle = a3m::radians(3.14159);
   * printf("%f deg = %f rad\n",
   *     a3m::getDegrees(angle), a3m::getRadians(angle));
   * a3m::setDegrees(angle, 67);
   * printf("%f deg = %f rad\n",
   *     a3m::getDegrees(angle), a3m::getRadians(angle));
   * \endcode
   */

  /** Class representing an angular value.
   * The class supports addition, subtraction, multiplication and division.
   */
  template<typename T>
  class Angle
  {
  public:
    /**
     * Supported angular units.
     */
    enum Units
    {
      RADIANS, /**< There are 2 * pi radians in a circle */
      DEGREES /**< There are 360 degrees in a circle */
    };

    static Angle<T> const ZERO; /**< Zero angle constant */

    /**
     * Constructor initialising angle to zero.
     */
    Angle();

    /**
     * Copy constructor using different Angle types.
     * This constructor allows implicit conversion and interoperability between
     * different Angle types.
     */
    template<typename U>
    Angle(Angle<U> const& other /**< Angle to copy */);

    /**
     * Constructor initialising value of angle.
     */
    Angle(Units units, /**< Units in which the value is given */
          T value /**< Value of the angle */);

    /**
     * Sets the value of the angle in the specified units.
     */
    void set(Units units, /**< Units in which the value is given */
          T value /**< Value of the angle */);

    /**
     * Returns the value of the angle in the specified units.
     * \return The numerical value of the angle
     */
    A3M_FLOAT get(Units units /**< Units in which the value is given */) const;

    /**
     * Assignment angle addition operator.
     * \return Reference to this angle
     */
    Angle<T>& operator+=(Angle<T> const& rhs /**< Angle to add */);

    /**
     * Assignment angle subtraction operator.
     * \return Reference to this angle
     */
    Angle<T>& operator-=(Angle<T> const& rhs /**< Angle to add */);

    /**
     * Assignment scalar multiplication operator.
     * \return Reference to this angle
     */
    Angle<T>& operator*=(T rhs /**< Scalar by which to multiply */);

    /**
     * Assignment scalar division operator.
     * \return Reference to this angle
     */
    Angle<T>& operator/=(T rhs /**< Scalar by which to divide */);

    /**
     * Assignment scalar modulo operator.
     * \return Reference to this angle
     */
    Angle<T>& operator%(T rhs /**< Scalar by which to divide */);

    /**
     * Unary negation operator.
     * \return Angle with equal magnitude but opposite sign
     */
    Angle<T> operator-() const;

    // Many of the free functions are friends of Angle for speed purposes.

    /**
     * Addition operator.
     * \return Sum of angles
     */
    template<typename U>
    friend Angle<U> operator+(Angle<U> const& lhs, /**< LHS of operation */
                              Angle<U> const& rhs /**< RHS of operation */);

    /**
     * Subtraction operator.
     * \return Sum of angles
     */
    template<typename U>
    friend Angle<U> operator-(Angle<U> const& lhs, /**< LHS of operation */
                              Angle<U> const& rhs /**< RHS of operation */);

    /**
     * Scalar multiplication operator.
     * \return Product of angle and scalar
     */
    template<typename U, typename V>
    friend Angle<U> operator*(Angle<U> const& lhs, /**< LHS of operation */
                              V rhs /**< RHS of operation */);

    /**
     * Scalar multiplication operator.
     * \return Product of scalar and angle
     */
    template<typename U, typename V>
    friend Angle<U> operator*(V lhs, /**< LHS of operation */
                              Angle<U> const& rhs /**< RHS of operation */);

    /**
     * Scalar division operator.
     * \return Angle divided by scalar
     */
    template<typename U, typename V>
    friend Angle<U> operator/(Angle<U> const& lhs, /**< LHS of operation */
                              V rhs /**< RHS of operation */);

    /**
     * Angle division operator.
     * \return Angle divided by angle
     */
    template<typename U>
    friend U operator/(Angle<U> const& lhs, /**< LHS of operation */
                       Angle<U> const& rhs /**< RHS of operation */);

    /**
     * Scalar modulo operator.
     * \return Remainder after division of angle by scalar
     */
    template<typename U, typename V>
    friend Angle<U> operator%(Angle<U> const& lhs, /**< LHS of operation */
                              V rhs /**< RHS of operation */);
    /**
     * Angle modulo operator.
     * \return Remainder after division of angles
     */
    template<typename U>
    friend U operator%(Angle<U> const& lhs, /**< LHS of operation */
                       Angle<U> const& rhs /**< RHS of operation */);

    /**
     * Angle equality operator.
     * \return A3M_TRUE if angles are equal
     */
    template<typename U>
    friend A3M_BOOL operator==(Angle<U> const& lhs, /**< LHS of operation */
                               Angle<U> const& rhs /**< RHS of operation */);

    /**
     * Angle inequality operator.
     * \return A3M_TRUE if angles are unequal
     */
    template<typename U>
    friend A3M_BOOL operator!=(Angle<U> const& lhs, /**< LHS of operation */
                               Angle<U> const& rhs /**< RHS of operation */);

    /**
     * Calculates the absolute (positive) value of the angle.
     * \return The angle with a postive sign
     */
    template<typename U>
    friend Angle<U> abs(Angle<U> const& angle /**< Angle to use */);

    /**
     * Calculates the sine of an angle.
     * \return Sine of the angle
     */
    template<typename U>
    friend U sin(Angle<U> const& angle /**< Angle to use */);

    /**
     * Calculates the cosine of an angle.
     * \return Cosine of the angle
     */
    template<typename U>
    friend U cos(Angle<U> const& angle /**< Angle to use */);

    /**
     * Calculates the tangent of an angle.
     * \return Tangent of the angle
     */
    template<typename U>
    friend U tan(Angle<U> const& angle /**< Angle to use */);

    /**
     * Calculates an angle from its sine (inverse sine function).
     * \return The angle corresponding to the given sine value
     */
    template<typename U>
    friend Angle<U> asin(U value /**< Value to use */);

    /**
     * Calculates an angle from its cosine (inverse cosine function).
     * \return The angle corresponding to the given sine value
     */
    template<typename U>
    friend Angle<U> acos(U value /**< Value to use */);

    /**
     * Calculates an angle from its tangent (inverse tangent function).
     * \return The angle corresponding to the given tangent value
     */
    template<typename U>
    friend Angle<U> atan(U value /**< Value to use */);

    /**
     * Calculates an angle from 2D cartesian coordinates.
     * If you plot a line on a graph from (0, 0) to (x, y), the angle returned
     * will be that between the the line and the positive x-axis.
     * \return The angle corresponding to the given coordinate
     */
    template<typename U>
    friend Angle<U> atan2(
        U y, /**< Coordinate y-component */
        U x /**< Coordinate x-component */);

  private:
    /**
     * Private constructor initialising value of angle to radians.
     */
    explicit Angle(T radians /**< Angle in radians */);

    T m_radians /**< Internally the angle is stored in radians */;
  };

  /** Specialisation for float */
  typedef Angle<A3M_FLOAT> Anglef;

  /******************
   * Free functions *
   ******************/

  /**
   * Convenience function for creating an angle in radians.
   * \return The angle corresponding to the given value
   */
  template<typename T>
  Angle<T> radians(T value /**< Value of angle in radians */);

  /**
   * Convenience function for creating an angle in degrees.
   * \return The angle corresponding to the given value
   */
  template<typename T>
  Angle<T> degrees(T value /**< Value of angle in degrees */);

  /**
   * Convenience function for setting the value of an angle in radians.
   */
  template<typename T>
  void setRadians(Angle<T>& angle, /**< Angle */
                  T value /**< Value of angle in radians */);

  /**
   * Convenience function for setting the value of an angle in degrees.
   */
  template<typename T>
  void setDegrees(Angle<T>& angle, /**< Angle */
                  T value /**< Value of angle in degrees */);

  /**
   * Convenience function for getting the value of an angle in radians.
   * \return The value of the angle
   */
  template<typename T>
  T getRadians(Angle<T> const& value /**< Angle */);

  /**
   * Convenience function for getting the value of an angle in degrees.
   * \return The value of the angle
   */
  template<typename T>
  T getDegrees(Angle<T> const& value /**< Angle */);

  /** @} */

} /* namespace a3m */


/******************************************************************************
 * Implementation
 ******************************************************************************/

namespace a3m
{
  template<typename T>
  Angle<T> const Angle<T>::ZERO;

  /********************
   * Member functions *
   ********************/

  template<typename T>
  Angle<T>::Angle() :
    m_radians(0)
  {
  }

  template<typename T>
  Angle<T>::Angle(Units units, T value) :
    m_radians()
  {
    set(units, value);
  }

  // Private constructor
  template<typename T>
  Angle<T>::Angle(T radians) :
    m_radians(radians)
  {
  }

  template<typename T>
  template<typename U>
  Angle<T>::Angle(Angle<U> const& other) :
    // Different Angle types are not friends, so must get value indirectly.
    m_radians(static_cast<T>(other.get(Angle<U>::RADIANS)))
  {
  }

  template<typename T>
  void Angle<T>::set(Units units, T value)
  {
    switch (units)
    {
      case RADIANS:
        m_radians = value;
        break;

      case DEGREES:
        m_radians = value * FLOAT_PI / 180;
        break;
    }
  }

  template<typename T>
  A3M_FLOAT Angle<T>::get(Units units) const
  {
    A3M_FLOAT value;

    switch (units)
    {
      case RADIANS:
        value = m_radians;
        break;

      case DEGREES:
        value = m_radians * 180 / FLOAT_PI;
        break;
    }

    return value;
  }

  template<typename T>
  Angle<T>& Angle<T>::operator+=(Angle<T> const& rhs)
  {
    m_radians += rhs.m_radians;
    return *this;
  }

  template<typename T>
  Angle<T>& Angle<T>::operator-=(Angle<T> const& rhs)
  {
    m_radians -= rhs.m_radians;
    return *this;
  }

  template<typename T>
  Angle<T>& Angle<T>::operator*=(T rhs)
  {
    m_radians *= rhs;
    return *this;
  }

  template<typename T>
  Angle<T>& Angle<T>::operator/=(T rhs)
  {
    m_radians /= rhs;
    return *this;
  }

  template<typename T>
  Angle<T>& Angle<T>::operator%(T rhs)
  {
    m_radians %= rhs;
    return *this;
  }

  template<typename T>
  Angle<T> Angle<T>::operator-() const
  {
    return Angle(-m_radians);
  }

  /********************
   * Friend functions *
   ********************/

  template<typename U>
  Angle<U> operator+(Angle<U> const& lhs, Angle<U> const& rhs)
  {
    return Angle<U>(lhs.m_radians + rhs.m_radians);
  }

  template<typename U>
  Angle<U> operator-(Angle<U> const& lhs, Angle<U> const& rhs)
  {
    return Angle<U>(lhs.m_radians - rhs.m_radians);
  }

  template<typename U, typename V>
  Angle<U> operator*(Angle<U> const& lhs, V rhs)
  {
    return Angle<U>(lhs.m_radians * rhs);
  }

  template<typename U, typename V>
  Angle<U> operator*(V lhs, Angle<U> const& rhs)
  {
    return Angle<U>(lhs * rhs.m_radians);
  }

  template<typename U, typename V>
  Angle<U> operator/(Angle<U> const& lhs, V rhs)
  {
    return Angle<U>(lhs.m_radians / rhs);
  }

  template<typename U>
  U operator/(Angle<U> const& lhs, Angle<U> const& rhs)
  {
    return lhs.m_radians / rhs.m_radians;
  }

  template<typename U, typename V>
  Angle<U> operator%(Angle<U> const& lhs, V rhs)
  {
    return Angle<U>(lhs.m_radians % rhs);
  }

  template<typename U>
  U operator%(Angle<U> const& lhs, Angle<U> const& rhs)
  {
    return lhs.m_radians % rhs.m_radians;
  }

  template<typename U>
  A3M_BOOL operator==(Angle<U> const& lhs, Angle<U> const& rhs)
  {
    return lhs.m_radians == rhs.m_radians;
  }

  template<typename U>
  A3M_BOOL operator!=(Angle<U> const& lhs, Angle<U> const& rhs)
  {
    return !(lhs == rhs);
  }

  template<typename U>
  Angle<U> abs(Angle<U> const& angle)
  {
    return Angle<U>(abs(angle.m_radians));
  }

  template<typename U>
  U sin(Angle<U> const& angle)
  {
    return std::sin(angle.m_radians);
  }

  template<typename U>
  U cos(Angle<U> const& angle)
  {
    return std::cos(angle.m_radians);
  }

  template<typename U>
  U tan(Angle<U> const& angle)
  {
    return std::tan(angle.m_radians);
  }

  template<typename U>
  Angle<U> asin(U value)
  {
    return Angle<U>(std::asin(value));
  }

  template<typename U>
  Angle<U> acos(U value)
  {
    return Angle<U>(std::acos(value));
  }

  template<typename U>
  Angle<U> atan(U value)
  {
    return Angle<U>(std::atan(value));
  }

  template<typename U>
  Angle<U> atan2(U y, U x)
  {
    return Angle<U>(std::atan2(y, x));
  }

  /******************
   * Free functions *
   ******************/

  template<typename T>
  Angle<T> radians(T value)
  {
    return Angle<T>(Angle<T>::RADIANS, value);
  }

  template<typename T>
  Angle<T> degrees(T value)
  {
    return Angle<T>(Angle<T>::DEGREES, value);
  }

  template<typename T>
  void setRadians(Angle<T>& angle, T value)
  {
    return angle.set(Angle<T>::RADIANS, value);
  }

  template<typename T>
  void setDegrees(Angle<T>& angle, T value)
  {
    return angle.set(Angle<T>::DEGREES, value);
  }

  template<typename T>
  T getRadians(Angle<T> const& angle)
  {
    return angle.get(Angle<T>::RADIANS);
  }

  template<typename T>
  T getDegrees(Angle<T> const& angle)
  {
    return angle.get(Angle<T>::DEGREES);
  }

} /* namespace a3m */

#endif /* A3M_ANGLE_H */

