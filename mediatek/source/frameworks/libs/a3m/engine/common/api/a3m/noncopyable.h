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
 * Non-copyable base class.
 */
#pragma once
#ifndef A3M_NONCOPYABLE_H
#define A3M_NONCOPYABLE_H

namespace a3m
{
  /** \ingroup  a3mIntUtil
   * A simple class to prevent derived classes from being copied.
   * Inherit from this class to make the copy constructor and assignment
   * operator of your class private, and hence objects of that class
   * uncopyable.
   */
  class NonCopyable
  {
  public:
    /** Default constructor */
    NonCopyable() {}

    /** Destructor */
    virtual ~NonCopyable() {}

  private:
    /** Private copy constructor */
    NonCopyable( NonCopyable const & /**< [in] other object */ );
    /** Private assignment operator
     * \return reference to this object
     */
    NonCopyable &operator=( NonCopyable const & /**< [in] other object */ );
  };

} /* namespace a3m */

#endif /* A3M_NONCOPYABLE_H */
