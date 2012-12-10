/*****************************************************************************
 *
 * Copyright (c) 2010 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 *****************************************************************************/
/** \file
 * Stencil class
 *
 */
#pragma once
#ifndef A3M_STENCIL_H
#define A3M_STENCIL_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/pointer.h>  /* for Shared Ptr */

namespace a3m
{
  /** \defgroup a3mStencil Stencil
   * \ingroup  a3mRefScene
   * Stencil comparison function.
   * @{
   */

  /** Contains the stencil comparison function, a reference value for the
   * comparison, and the stencil operators. The attributes can be set
   * separately for front- and back-facing triangles. Point sprites and lines
   * are always taken to be front-facing.
   */
  class Stencil
  {
  public:
    /** Default constructor */
    Stencil();

    /** Destructor */
    ~Stencil();

    /**
     * Describes the stencil parameters for facing primitives in enum.
     */
    enum Face
    {
      BACK,         /**< Back-facing primitive */
      FRONT,        /**< Front-facing primitive */
      NUM_FACES     /**< Number of faces */
    };

    /**
     * Describes the comparison function for stencil test in enum.
     * ref: the stencil test reference value
     * mask: the stencil test bitmask
     */
    enum Function
    {
      NEVER,        /**< Always fails */
      LESS,         /**< Passes if ( ref & mask ) < ( stencil & mask ) */
      EQUAL,        /**< Passes if ( ref & mask ) = ( stencil & mask ) */
      LEQUAL,       /**< Passes if ( ref & mask ) <= ( stencil & mask ) */
      GREATER,      /**< Passes if ( ref & mask ) > ( stencil & mask ) */
      NOTEQUAL,     /**< Passes if ( ref & mask ) != ( stencil & mask ) */
      GEQUAL,       /**< Passes if ( ref & mask ) >= ( stencil & mask ) */
      ALWAYS        /**< Always passes */
    };

    /**
     * Describes the stencil operation in enum.
     */
    enum Operation
    {
      ZERO,      /**< Sets the stencil buffer value to zero */
      KEEP,      /**< Leaves the existing stencil buffer contents unmodified */
      REPLACE,   /**< Copies the stencil reference value to the buffer */
      INCR,      /**< Increments the stencil buffer value by one */
      DECR,      /**< Decrements the stencil buffer value by one */
      INVERT,    /**< Performs a bitwise inversion to the stencil buffer
                      value */
      INCR_WRAP, /**< Increments the stencil buffer value by one, but wrap the
                      value if the stencil value overflows. */
      DECR_WRAP  /**< Decrements the stencil buffer value by one, but wrap the
                      value if the stencil value underflows. */
    };

    /** Retrieves the stencil function of the corresponding facing primitive.
     * \return The current stencil function.
     */
    Function getFunction(Face face /**< the facing primitive */) const
    {
      return m_func[face];
    }

    /** Retrieves the stencil test reference value of the corresponding facing
     * primitive.
     * \return The current test reference value.
     */
    A3M_INT32 getFunctionRef(Face face /**< the facing primitive */) const
    {
      return m_funcRef[face];
    }

    /** Retrieves the stencil test bitmask value of the corresponding facing
     * primitive.
     * \return The current test bitmask value.
     */
    A3M_UINT32 getFunctionMask(Face face /**< the facing primitive */) const
    {
      return m_funcMask[face];
    }

    /** Sets the stencil function, reference value, and mask. These can be
     * separately for front-facing and back-facing primitives, or for both at
     * the same time.
     * \return None
     */
    void setFunction(Face face,      /**< the facing primitive             */
                     Function func,  /**< the stencil function             */
                     A3M_INT32 ref,  /**< the stencil test reference value */
                     A3M_UINT32 mask /**< the stencil test bitmask         */)
    {
      m_func[face] = func;
      m_funcRef[face] = ref;
      m_funcMask[face] = mask;
    }

    /** Get the operation when the stencil test fails.
     * \return The operation value.
     */
    Operation getStencilFailOp(Face face /**< the facing primitive */) const
    {
      return m_sFail[face];
    }

    /** Gets the operation when the stencil test passes but the depth test
     * fails.
     * \return The operation value.
     */
    Operation getStencilPassAndDepthFailOp(Face face /**< the facing
                                                      * primitive */) const
    {
      return m_sPassDepthFail[face];
    }

    /** Gets the operation when both the stencil test and the depth test pass.
     * \return The operation value.
     */
    Operation getStencilPassAndDepthPassOp(Face face /**< the facing
                                                      * primitive */) const
    {
      return m_sPassDepthPass[face];
    }

    /** Sets the stencil operators to execute depending on the outcome of the
     * depth and stencil tests. The set of operators can be set for
     * font-facing, back-facing, or both kinds of primitives at the same time.
     *\return None
     */
     void setOperations(Face face,
                        /**< Front or back? */
                        Operation sFail,
                        /**< the operator to execute if both the stencil
                             test fails */
                        Operation sPassDepthFail,
                        /**< the operator to execute if the stencil test
                             pass but the depth test fails */
                        Operation sPassDepthPass
                        /**< the operator to execute if both the stencil test
                             and the depth test pass */)
     {
       m_sFail[face] = sFail;
       m_sPassDepthFail[face] = sPassDepthFail;
       m_sPassDepthPass[face] = sPassDepthPass;
     }

    /** Retrieves the stencil buffer write mask for either back-facing or
     * front-facing primitive. For a stencil buffer with s bitplanes, only the
     * s low-order bits of the result are meaningful. The higher-order bits of
     * the mask are undefined and can have any value.
     * \return The current stencil buffer write enable mask.
     */
     A3M_UINT32 getWriteMask(Face face /**< the facing primitive */) const
     {
       return m_writeMask[face];
     }

    /** Sets the stencil buffer write enable mask for either or both of
     * back-facing and front-facing primitives.
     * If a particular bit in the write mask is set, the corresponding bitplane
     * in the stencil buffer is enabled for writing. A zero bit in the mask
     * indicates that the corresponding bitplane will not be written to. For a
     * stencil buffer with s bitplanes, only the s low-order bits of the given
     * mask are effective; the higher-order bits are ignored and can have any
     * value.
     *\return None
     */
     void setWriteMask( Face face,
                        /**< Front or back? */
                        A3M_UINT32 mask
                        /**< A bitmask in which the "1" bits indicates the
                             stencil buffer bitplanes that are enabled for
                             writing, and "0" bits indicate those that are
                             not. */ )
    {
      m_writeMask[face] = mask;
    }

    /** Enable Stencil test.
     * Enable stencil test with set factor parameters.
     * \return None.
     */
    void enable() const;

    /** Enable Stencil test.
     * Enable stencil test with set factor parameters.
     * \return None.
     */
    void enable( Stencil &other /**< [in] Stencil object representing the
                                     current state */) const;

  private:
    /**< Function settings for each face */
    Function   m_func[NUM_FACES];

    /**< Function reference value for each face */
    A3M_INT32  m_funcRef[NUM_FACES];

    /**< Function bitmask settings for each face */
    A3M_UINT32 m_funcMask[NUM_FACES];

    /**< Operator for stencil test fails */
    Operation  m_sFail[NUM_FACES];

    /**< Operator for stencil test pass but depth test fails */
    Operation  m_sPassDepthFail[NUM_FACES];

    /**< Operator for stencil test pass and depth test pass */
    Operation  m_sPassDepthPass[NUM_FACES];

    /**< Stencil buffer write enable mask settings */
    A3M_UINT32 m_writeMask[NUM_FACES];

    /* Enable stencil options for given face */
    void enable( Stencil &other, Face face ) const;
  };

  /** @} */

} /* namespace a3m */

#endif /* A3M_STENCIL_H */
