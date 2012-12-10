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
 * Blender class
 *
 */
#pragma once
#ifndef A3M_BLENDER_H
#define A3M_BLENDER_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/colour.h>  /* for Colour4f structure */

namespace a3m
{
  /** \defgroup a3mBlender Blender
   * \ingroup  a3mRefScene
   * Facilities for setting and retrieving OpenGL's blending functions.
   * @{
   */

  /**
   * Facilities for setting and retrieving OpenGL's blender functions, blending
   * factors, and the constant blend colour. It supports separately for colour
   * and alpha channels.
   */
  class Blender
  {
  public:
    /** Default constructor */
    Blender();

    /** Destructor */
    ~Blender();

     /**
     * Describes the blender function in enum.
     */
    enum BlendFunc
    {
      ADD,                      /**< The default additive blending mode */
      REVERSE_SUBTRACT,         /**< Reversed subtractive blending      */
      SUBTRACT,                 /**< Subtractive blending              */
    };

    /**
     * Describes the blender factors in enum.
     */
    enum BlendFactor
    {
      CONSTANT_ALPHA,           /**< The source or destination colour (or
                                 * equivalently alpha) is to be multiplied by
                                 * the alpha component of the constant blend
                                 * colour. */
      CONSTANT_COLOUR,          /**< The source or destination colour (or
                                 * equivalently alpha) is to be multiplied
                                 * component-wise by the constant blend colour
                                 * (or alpha). */
      DST_ALPHA,                /**< The source or destination colour (or
                                 * equivalently alpha) is to be multiplied by
                                 * the alpha component of the destination
                                 * colour. */
      DST_COLOUR,               /**< The source or destination colour (or
                                 * equivalently alpha) is to be multiplied
                                 * component-wise by the destination colour
                                 * and alpha. */
      ONE,                      /**< The source or destination colour (or
                                 * equivalently alpha) is to be multiplied by
                                 * one (that is, taken as such). */
      ONE_MINUS_CONSTANT_ALPHA, /**< The source or destination colour (or
                                 * equivalently alpha) is to be multiplied by
                                 * one minus the alpha component of the
                                 * constant blend colour. */
      ONE_MINUS_CONSTANT_COLOUR,/**< The source or destination colour (or
                                 * equivalently alpha) is to be multiplied
                                 * component-wise by one minus the constant
                                 * blend colour (or alpha). */
      ONE_MINUS_DST_ALPHA,      /**< The source or destination colour (or
                                 * equivalently alpha) is to be multiplied by
                                 * one minus the alpha component of the
                                 * destination blend colour. */
      ONE_MINUS_DST_COLOUR,      /**< The source or destination colour (or
                                 * equivalently alpha) is to be multiplied
                                 * component-wise by one minus the destination
                                 * colour and alpha. */
      ONE_MINUS_SRC_ALPHA,      /**< The source or destination colour (or
                                 * equivalently alpha) is to be multiplied by
                                 * one minus the alpha component of the source
                                 * colour. */
      ONE_MINUS_SRC_COLOUR,     /**< The source or destination colour (or
                                 * equivalently alpha) is to be multiplied
                                 * component-wise by one minus the source
                                 * colour and alpha. */
      SRC_ALPHA,                /**< The source or destination colour (or
                                 * equivalently alpha) is to be multiplied by
                                 * the alpha component of the source colour. */
      SRC_ALPHA_SATURATE,       /**< The source colour is to be multiplied by
                                 * the source alpha, or one minus the
                                 * destination  alpha, whichever is the
                                 * smallest. */
      SRC_COLOUR,               /**< The source or destination colour (or
                                 * equivalently alpha) is to be multiplied
                                 * component-wise by the source colour and
                                 * alpha. */
      ZERO                      /**< The source or destination colour (or
                                 * equivalently alpha) is to be multiplied
                                 * by zero (that is, ignored). */
    };

    /** Retrieves the constant blend colour and alpha.
     * \returns The current blend colour and alpha.
     */
    const Colour4f& getBlendColour() const { return m_colour; }

    /** Set the constant blend colour and alpha with individual parameters.
     * \return None.
     */
    void setBlendColour( A3M_FLOAT r, /**< The new colour RED value   */
                         A3M_FLOAT g, /**< The new colour GREEN value */
                         A3M_FLOAT b, /**< The new colour BLUE value */
                         A3M_FLOAT a  /**< The new ALPHA value        */)
    {
      m_colour.r = r;
      m_colour.g = g;
      m_colour.b = b;
      m_colour.a = a;
    }

    /** Set the constant blend colour and alpha together.
     * \return None.
     */
    void setBlendColour(Colour4f& colour /**< The new colour to set */)
    {
      m_colour = colour;
    }

    /** Retrieves the source colour factor.
     * \return The current source colour factor.
     */
    BlendFactor getSrcColour() const { return m_srcColourFactor; }

    /** Retrieves the source alpha factor.
     * \return The current source alpha factor.
     */
    BlendFactor getSrcAlpha() const { return m_srcAlphaFactor; }

    /** Retrieves the destination colour factor.
     * \return The current destination colour factor.
     */
    BlendFactor getDstColour() const { return m_dstColourFactor; }

    /** Retrieves the destination alpha factor.
     * \return The current destination alpha factor.
     */
    BlendFactor getDstAlpha() const { return m_dstAlphaFactor; }

    /** Sets the colour and alpha blend factors.
     * \return None
     */
    void setFactors( BlendFactor srcColour, /**< source colour to set */
                     BlendFactor srcAlpha, /**< source alpha to set */
                     BlendFactor dstColour, /**< destination colour to set */
                     BlendFactor dstAlpha /**< destination alpha to set */ )
    {
      m_srcColourFactor = srcColour;
      m_srcAlphaFactor = srcAlpha;
      m_dstColourFactor = dstColour;
      m_dstAlphaFactor = dstAlpha;
    }

    /** Retrieves the source colour function.
     * \return The current source colour function.
     */
    BlendFunc getFuncColour() const { return m_colourFunction; }

    /** Retrieves the source alpha function.
     * \returns The current source alpha function.
     */
    BlendFunc getFuncAlpha() const { return m_alphaFunction; }

    /** Sets the colour and alpha blend functions.
     * \return None
     */
    void setFunctions( BlendFunc colourFunction,
                       /**< the blend function for the colour channels */
                      BlendFunc alphaFunction
                      /**< the blend function for the alpha channel */ )
    {
      m_colourFunction = colourFunction;
      m_alphaFunction = alphaFunction;
    }

    /** Opaque query function.
     * \return TRUE if the colour blend settings for this Blender instance
     * specify an opaque object (i.e. not transparent)
     */
    A3M_BOOL isOpaque() const;

    /** Retrieve the state of the isOpague locking override.
        \return A3M_TRUE if isOpaque is locked to true, A3M_FALSE otherwise. */
    A3M_BOOL getForceOpaque() const
    { return m_forceOpaque;}

    /** Lock isOpaque to force it to ignore the current blend func
        and always return A3M_TRUE. */
    void setForceOpaque(A3M_BOOL forceOpaque = A3M_TRUE /**< lock flag */)
    { m_forceOpaque = forceOpaque;}

    /** Enable Blender.
     * Enable Blender with set factor parameters.
     * \return None.
     */
    void enable() const;

    /** Enable Blender.
     * Enable Blender with set factor parameters.
     * \return None.
     */
    void enable( Blender &other /**< [in] Blender object representing the
                                     current state */) const;

  private:
    Colour4f    m_colour;           /**< store RGBA colour components    */
    BlendFactor m_srcColourFactor;  /**< store source colour factor      */
    BlendFactor m_dstColourFactor;  /**< store destination colour factor */
    BlendFactor m_srcAlphaFactor;   /**< store source alpha factor       */
    BlendFactor m_dstAlphaFactor;   /**< store destination alpha factor  */
    BlendFunc   m_colourFunction;   /**< store colour function           */
    BlendFunc   m_alphaFunction;    /**< store alpha function            */
    A3M_BOOL    m_forceOpaque;      /**< isOpaque override lock          */
  };
  /** @} */

} /* namespace a3m */

#endif /* A3M_BLENDER_H */
