/*****************************************************************************
 *
 * Copyright (c) 2012 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 *****************************************************************************/
/** \file
 * FlagSet and FlagMask classes
 *
 */
#pragma once
#ifndef A3M_FLAGS_H
#define A3M_FLAGS_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/assert.h>      /* for A3M_ASSERT */
#include <a3m/base_types.h>  /* for base types */

  /** \defgroup a3mRefTypeFlag Flags - Managing a set of Boolean flags
   *  \ingroup a3mRefTypes
   *
   * The classes FlagSet and FlagMask provide a generic way to hold and
   * manage a sets of boolean data.  This could be 'visibility' or 'clipping'
   * for a scene node, for example.  the main point of this class is to avoid
   * large numbers of application-specfic flags such as 'isVisible'.
   */

namespace a3m
{
  class FlagMask;

  /** \ingroup a3mRefTypeFlag
   * This class is a set of flags which can be set either to TRUE or FALSE.
   * Boolean arithmetic operations can be performed on the set in a
   * flag-wise manner, such as OR, AND and XOR, and individual flags may be set
   * or queried.
   * @{
   */
  class FlagSet
  {
    // FlagSet and FlagMask are dependent on each other
    friend class FlagMask;

  public:
    /**
     * Constructor initialising all flags to their default value.
     */
    FlagSet();

    /**
     * Sets all flags defined by a mask to the desired state.
     */
    void set(FlagMask const& mask, /**< Mask specifying all the flags to be
                                        set */
             A3M_BOOL state /**< State to which to set the flags */);

    /**
     * Checks whether all the flags defined by a mask are set.
     * \return TRUE if all specified flags are set
     */
    A3M_BOOL get(FlagMask const& mask /**< Mask specifying the flags to be
                                          checked */) const;

    /**
     * Checks whether any of the flags are set.
     * \return TRUE if any flag is set
     */
    A3M_BOOL any() const;

    /**
     * Unary inversion operator.
     * Result will have inverted flags.
     * \return Result of operation.
     */
    FlagSet operator~() const;

    /**
     * Assignment flag-wise AND operator.
     * Result will only have flags set which are set in both masks.
     * \param rhs  operand
     * \return Reference to this flag set.
     */
    FlagSet& operator&=(FlagSet const& rhs);

    /**
     * Assignment flag-wise OR operator.
     * Result will have flags set which are set in either mask.
     * \param rhs  operand
     * \return Reference to this flag set.
     */
    FlagSet& operator|=(FlagSet const& rhs);

    /**
     * Assignment flag-wise XOR operator.
     * Result will have flags set which are set in only one of the masks.
     * \param rhs  operand
     * \return Reference to this flag set.
     */
    FlagSet& operator^=(FlagSet const& rhs);

    /**
     * Flag-wise AND operator.
     * Result will only have flags set which are set in both masks.
     * \param lhs  lefthand operand
     * \param rhs  righthand operand
     * \return Result of operation.
     */
    friend FlagSet operator&(FlagSet const& lhs, FlagSet const& rhs);

    /**
     * Flag-wise OR operator.
     * Result will have flags set which are set in either mask.
     * \param lhs  lefthand operand
     * \param rhs  righthand operand
     * \return Result of operation.
     */
    friend FlagSet operator|(FlagSet const& lhs, FlagSet const& rhs);

    /**
     * Flag-wise XOR operator.
     * Result will have flags set which are set in only one of the masks.
     * \param lhs  lefthand operand
     * \param rhs  righthand operand
     * \return Result of operation.
     */
    friend FlagSet operator^(FlagSet const& lhs, FlagSet const& rhs);

    /**
     * Equality operator.
     * \param lhs  lefthand operand
     * \param rhs  righthand operand
     * \return TRUE if the flag sets are the same.
     */
    friend A3M_BOOL operator==(FlagSet const& lhs, FlagSet const& rhs);

    /**
     * Inequality operator.
     * \param lhs  lefthand operand
     * \param rhs  righthand operand
     * \return TRUE if the flag sets are not the same.
     */
    friend A3M_BOOL operator!=(FlagSet const& lhs, FlagSet const& rhs);

  private:
    /*
     * Constructs flag set from bits
     */
    FlagSet(A3M_UINT32 bits);

    A3M_UINT32 m_bits; /**< Represents which flags are set in this set */
  };
  /** @} */

  /** \ingroup a3mRefTypeFlag
   * A mask for determining the state of flags in a FlagSet.
   * This class performs two functions: the masking of individual flags in a
   * set, and determining what the default state of the flags are.  Flag masks
   * are different from plain bit masks because they allow set or queried as
   * either TRUE or FALSE.  The ability to "invert" a flag mask means that it is
   * easy to tell whether a particular flag is set or unset in a flag set.
   *
   * Example:
   * \code
   *   // Define a flag which is TRUE by default, using a mask
   *   a3m::FlagMask CONDITION(0, A3M_TRUE);
   *
   *   // Create a default flag set
   *   a3m::FlagSet flags;
   *
   *   // By default, the condition will test TRUE on a default set!
   *   A3M_ASSERT(flags.get(CONDITION));
   *
   *   // The inverse condition can be tested by using the inversion operator
   *   A3M_ASSERT(!flags.get(~CONDITION));
   *
   *   // These two operations set the condition to FALSE (the non-default
   *   // option), and are equivilent
   *   flags.set(CONDITION, A3M_FALSE);
   *   flags.set(~CONDITION, A3M_TRUE);
   * \endcode
   * @{
   */
  class FlagMask
  {
    // FlagSet and FlagMask are dependent on each other
    friend class FlagSet;

  public:
    /**
     * Constructor defining empty flag mask.
     */
    FlagMask();

    /**
     * Constructor for defining a flag mask.
     * The flag index uniquely defines a particular flag used in the scene
     * graph; no two flags should share the same index.  The default state
     * defines whether the flag is considered TRUE or FALSE by default.
     */
    FlagMask(A3M_UINT32 flagIndex, /**< Flag index can be in range 0 to 31 */
        A3M_BOOL defaultState /**< The flag's default state */);

    /**
     * Returns the flags for this mask.
     * A flag mask comprises the flags themselves, and the default state of
     * each flag.  This function returns the former.
     */
    FlagSet const& getFlags() const;

    /**
     * Returns the default state of the flags for this mask.
     * A flag mask comprises the flags themselves, and the default state of
     * each flag.  This function returns the former.
     */
    FlagSet const& getDefaultState() const;

    /**
     * Unary inversion operator.
     * Rather than inverting the flags themselves, which wouldn't make sense,
     * as the mask does not know the default state of flags which are not set,
     * inverting the mask inverts the default state of each of the set flags.
     * \return Result of operation.
     */
    FlagMask operator~() const;

    /**
     * Assignment flag-wise AND operator.
     * Result will contain only flags which exist in both masks.
     * \param rhs  operand
     * \return Reference to this mask.
     */
    FlagMask& operator&=(FlagMask const& rhs);

    /**
     * Assignment flag-wise OR operator.
     * Result will contain flags which exist in either mask.
     * \param rhs  operand
     * \return Reference to this mask.
     */
    FlagMask& operator|=(FlagMask const& rhs);

    /**
     * Assignment flag-wise XOR operator.
     * Result will contain flags which exist only one of the masks.
     * \param rhs  operand
     * \return Reference to this mask.
     */
    FlagMask& operator^=(FlagMask const& rhs);

    /**
     * Flag-wise AND operator.
     * Result will contain only flags which exist in both masks.
     * \param lhs  lefthand operand
     * \param rhs  righthand operand
     * \return Result of operation.
     */
    friend FlagMask operator&(FlagMask const& lhs, FlagMask const& rhs);

    /**
     * Flag-wise OR operator.
     * Result will contain flags which exist in either mask.
     * \param lhs  lefthand operand
     * \param rhs  righthand operand
     * \return Result of operation.
     */
    friend FlagMask operator|(FlagMask const& lhs, FlagMask const& rhs);

    /**
     * Flag-wise XOR operator.
     * Result will contain flags which exist only one of the masks.
     * \param lhs  lefthand operand
     * \param rhs  righthand operand
     * \return Result of operation.
     */
    friend FlagMask operator^(FlagMask const& lhs, FlagMask const& rhs);

  private:
    /*
     * Constructs mask from bits and flip bits.
     */
    FlagMask(FlagSet bits, FlagSet flipBits);

    FlagSet m_bits; /**< Defines which flags are set in the mask */
    FlagSet m_flipBits; /**< Defines whether flags should be inverted before
                             being tested against the mask (also represents the
                             default state of each flag) */
  };

  /** @} */
} // namespace a3m

/******************************************************************************
 * Implementation
 ******************************************************************************/

namespace a3m
{

  /****************************************************************************
   * FlagSet
   ****************************************************************************/

  inline FlagSet::FlagSet() :
    m_bits(0)
  {
  }

  inline FlagSet::FlagSet(A3M_UINT32 bits) :
    m_bits(bits)
  {
  }

  inline void FlagSet::set(FlagMask const& mask, A3M_BOOL state)
  {
    A3M_UINT32 bits = state ? ~mask.m_flipBits.m_bits : mask.m_flipBits.m_bits;
    m_bits = (bits & mask.m_bits.m_bits) | (m_bits & ~mask.m_bits.m_bits);
  }

  inline A3M_BOOL FlagSet::get(FlagMask const& mask) const
  {
    // XORing the state with the default state gives you the state in positive
    // logic, which we can then mask to find out if the flags are set.
    return (mask.m_bits & (m_bits ^ mask.m_flipBits)) == mask.m_bits;
  }

  inline A3M_BOOL FlagSet::any() const
  {
    return m_bits != 0;
  }

  inline FlagSet FlagSet::operator~() const
  {
    return FlagSet(~m_bits);
  }

  inline FlagSet& FlagSet::operator&=(FlagSet const& rhs)
  {
    m_bits &= rhs.m_bits;
    return *this;
  }

  inline FlagSet& FlagSet::operator|=(FlagSet const& rhs)
  {
    m_bits |= rhs.m_bits;
    return *this;
  }

  inline FlagSet& FlagSet::operator^=(FlagSet const& rhs)
  {
    m_bits ^= rhs.m_bits;
    return *this;
  }

  /****************************************************************************
   * FlagSet (Free Functions)
   ****************************************************************************/

  inline FlagSet operator&(FlagSet const& lhs, FlagSet const& rhs)
  {
    return FlagSet(lhs.m_bits & rhs.m_bits);
  }

  inline FlagSet operator|(FlagSet const& lhs, FlagSet const& rhs)
  {
    return FlagSet(lhs.m_bits | rhs.m_bits);
  }

  inline FlagSet operator^(FlagSet const& lhs, FlagSet const& rhs)
  {
    return FlagSet(lhs.m_bits ^ rhs.m_bits);
  }

  inline A3M_BOOL operator==(FlagSet const& lhs, FlagSet const& rhs)
  {
    return lhs.m_bits == rhs.m_bits;
  }

  inline A3M_BOOL operator!=(FlagSet const& lhs, FlagSet const& rhs)
  {
    return !(lhs == rhs);
  }

  /****************************************************************************
   * FlagMask
   ****************************************************************************/

  inline FlagMask::FlagMask() :
    m_bits(0),
    m_flipBits(0)
  {
  }

  inline FlagMask::FlagMask(A3M_UINT32 flagIndex, A3M_BOOL defaultState) :
    m_bits(1 << flagIndex),
    m_flipBits(defaultState << flagIndex)
  {
    A3M_ASSERT(flagIndex < 32);
  }

  inline FlagMask::FlagMask(FlagSet bits, FlagSet flipBits) :
    m_bits(bits),
    m_flipBits(flipBits)
  {
  }

  inline FlagSet const& FlagMask::getFlags() const
  {
    return m_bits;
  }

  inline FlagSet const& FlagMask::getDefaultState() const
  {
    return m_flipBits;
  }

  inline FlagMask FlagMask::operator~() const
  {
    FlagSet flipBits = ~m_flipBits & m_bits;
    return FlagMask(m_bits, flipBits);
  }

  inline FlagMask& FlagMask::operator&=(FlagMask const& rhs)
  {
    m_bits &= rhs.m_bits;
    m_flipBits = (m_flipBits | rhs.m_flipBits) & m_bits;
    return *this;
  }

  inline FlagMask& FlagMask::operator|=(FlagMask const& rhs)
  {
    m_bits |= rhs.m_bits;
    m_flipBits = (m_flipBits | rhs.m_flipBits) & m_bits;
    return *this;
  }

  inline FlagMask& FlagMask::operator^=(FlagMask const& rhs)
  {
    m_bits ^= rhs.m_bits;
    m_flipBits = (m_flipBits | rhs.m_flipBits) & m_bits;
    return *this;
  }

  /****************************************************************************
   * FlagMask (Free Functions)
   ****************************************************************************/

  inline FlagMask operator&(FlagMask const& lhs, FlagMask const& rhs)
  {
    FlagSet bits = lhs.m_bits & rhs.m_bits;
    FlagSet flipBits = (lhs.m_flipBits | rhs.m_flipBits) & bits;
    return FlagMask(bits, flipBits);
  }

  inline FlagMask operator|(FlagMask const& lhs, FlagMask const& rhs)
  {
    FlagSet bits = lhs.m_bits | rhs.m_bits;
    FlagSet flipBits = (lhs.m_flipBits | rhs.m_flipBits) & bits;
    return FlagMask(bits, flipBits);
  }

  inline FlagMask operator^(FlagMask const& lhs, FlagMask const& rhs)
  {
    FlagSet bits = lhs.m_bits ^ rhs.m_bits;
    FlagSet flipBits = (lhs.m_flipBits | rhs.m_flipBits) & bits;
    return FlagMask(bits, flipBits);
  }

} // namespace a3m

#endif // A3M_FLAGS_H

