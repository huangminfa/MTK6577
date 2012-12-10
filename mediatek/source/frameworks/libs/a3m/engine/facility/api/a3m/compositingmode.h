/*****************************************************************************
 *
 * Copyright (c) 2010 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 ******************************************************************************/
/** \file
 * CompositingMode class
 *
 */
#pragma once
#ifndef A3M_COMPOSITINGMODE_H
#define A3M_COMPOSITINGMODE_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/blender.h>     /* for Blender object */
#include <a3m/stencil.h>     /* for Stencil object */

namespace a3m
{
  /** \defgroup a3mCompositingMode Compositing Mode
   * \ingroup  a3mRefScene
   * Wrapper for depth, blend and stencil composition features.
   *
   * @{
   */

  /** The compositing mode class contains information about blending
   * an object into the scene as a whole.  It lets the application
   * control what is written to the frame buffer and how it
   * blends with the existing pixels for compositing and multi-pass rendering
   * effects.
   *
   * Depth offset is added to the depth (Z) value of a pixel prior to depth
   * test and depth write. The offset is constant across a polygon. Depth
   * offset is used to prevent Z fighting, which makes coplanar polygons
   * intersect each other on the screen due to the limited resolution of the
   * depth buffer. Depth offset allows, for example, white lines on a highway
   * or scorch marks on a wall (decals in general) to be implemented with
   * polygons instead of textures. Depth offset has no effect if depth
   * buffering is disabled. Blending combines the incoming fragment's R, G, B,
   * and A values with the R, G, B, and A values stored in the frame buffer at
   * the incoming fragment's location.
   */
  class CompositingMode
  {
  public:

    /**
     * Structure ColourMask
     * Each element of this structure represents whether that corresponding
     * colour component in colour buffer can be modified or not. If mask is
     * A3M_FALSE then, that colour component is non-writable.
     */
    struct ColourMask
    {
      A3M_BOOL r; /**< Mask for red colour component in colour buffer */
      A3M_BOOL g; /**< Mask for green colour component in colour buffer */
      A3M_BOOL b; /**< Mask for blue colour component in colour buffer */
      A3M_BOOL a; /**< Mask for alpha component in colour buffer */

      /** ColourMask default constructor */
      ColourMask () : r(A3M_TRUE), g(A3M_TRUE), b(A3M_TRUE), a(A3M_TRUE) {}
      /** ColourMask constructor with arguments */
      ColourMask ( A3M_BOOL r, A3M_BOOL g, A3M_BOOL b, A3M_BOOL a )
        : r(r), g(g), b(b), a(a) {}

      /** Equals operator for colourMask objects.
       * \return TRUE if the objects are the same.
       */
      A3M_BOOL operator==( ColourMask const &cm  /**< [in] other mask */) const
      {
        return ( r == cm.r ) && ( g == cm.g ) && ( b == cm.b ) && ( a == cm.a );
      }

      /** Not-Equals operator for colourMask objects.
       * \return TRUE if the objects are different.
       */
      A3M_BOOL operator!=( ColourMask const &cm  /**< [in] other mask */) const
      {
        return !( *this == cm );
      }
    };

    /** Default constructor */
    CompositingMode();

    /**
     * Describes the comparison function for depth test in enum.
     * ref: the depth test reference value
     * mask: the depth test bitmask
     */
    enum Function
    {
      NEVER,        /**< Always fails */
      LESS,         /**< Passes if ( ref & mask ) < ( depth & mask ) */
      EQUAL,        /**< Passes if ( ref & mask ) = ( depth & mask ) */
      LEQUAL,       /**< Passes if ( ref & mask ) <= ( depth & mask ) */
      GREATER,      /**< Passes if ( ref & mask ) > ( depth & mask ) */
      NOTEQUAL,     /**< Passes if ( ref & mask ) != ( depth & mask ) */
      GEQUAL,       /**< Passes if ( ref & mask ) >= ( depth & mask ) */
      ALWAYS        /**< Always passes */
    };

    /** Retrieves the currently set Blender object.
     * \return The current Blender, or null if no Blender is set.
     */
    Blender const& getBlender() const {return m_blender;}

    /** Sets the Blender object to use. Setting the Blender to null detaches
     * the current Blender from this CompositingMode.
     * \return None
     */
    void setBlender (Blender const& blender /**< the Blender object to set */)
    {
       m_blender = blender;
    }

    /** Retrieves the colour write mask. The components of the mask are either
     * zero or 0xFF, depending on whether the corresponding channel is
     * disabled or enabled for writing, respectively.
     * \return The current write enable mask.
     */
    const ColourMask& getColourWriteMask() const {return m_colourWriteMask;}

    /** Sets the colour write mask in RGBA format. Each colour component must
     * be either A3M_FALSE, indicating that the corresponding channel is disabled
     * for writing, or A3M_TRUE, indicating that the channel is enabled.
     * \return None
     */
    void setColourWriteMask( A3M_BOOL r, /**< RED mask value */
                             A3M_BOOL g, /**< GREEN mask value */
                             A3M_BOOL b, /**< BLUE mask value */
                             A3M_BOOL a  /**< ALPHA mask value */)
    {
      m_colourWriteMask.r = r;
      m_colourWriteMask.g = g;
      m_colourWriteMask.b = b;
      m_colourWriteMask.a = a;
    }

    /** Set the colour write mask together.
     * \return None.
     */
    void setColourWriteMask( ColourMask const &colourMask
                             /**< the colour write enable mask */ )
    {
       m_colourWriteMask = colourMask;
    }

    /** Retrieves the current depth offset slope factor.
     * \return The current depth offset factor.
     */
    A3M_FLOAT getDepthOffsetFactor() const {return m_depthOffsetFactor;}

    /** Retrieves the current constant depth offset in depth units.
     * \return The current depth offset in depth units.
     */
    A3M_FLOAT getDepthOffsetUnits() const {return m_depthOffsetUnits;}

    /** Defines a value that is added to the screen space Z coordinate of a
     * fragment immediately before depth test and depth write.
     * \return None
     */
    void setDepthOffset(A3M_FLOAT factor, /**< slope dependent depth offset */
                        A3M_FLOAT units   /**< constant depth offset*/ )
    {
      m_depthOffsetFactor = factor;
      m_depthOffsetUnits  = units;
    }

    /** Retrieves the depth test function.
     * \return The current depth comparison function.
     */
    Function getDepthTestFunc() const {return m_depthTestFunc;}

    /** Set the comparison function for the stencil depth test.
     * \return None
     */
    void setDepthTestFunc ( Function function
                            /**< the depth test function to set */ )
    {
      m_depthTestFunc = function;
    }

    /** Retrieves the currently set Stencil object. Stencil buffering is
     * disabled if no Stencil object is set.
     * \return The current Stencil, or null if no Stencil is set.
     */
    Stencil const& getStencil() const {return m_stencil;}

    /** Sets the Stencil object to use. Setting the Stencil to null disables
     * stencil buffering.
     * \return None
     */
    void setStencil (Stencil const& stencil /**< the Stencil object to set */)
    {
      m_stencil = stencil;
    }

    /** Queries whether depth testing is enabled.
     * \return A3M_TRUE if depth testing is enabled; A3M_FALSE if it's
     * disabled.
     */
    A3M_BOOL isDepthTestEnabled() const {return m_depthTestEnabled;}

    /** Enables or disables depth testing. If depth testing is enabled, a
     * fragment is written to the frame buffer if and only if its depth
     * component is less than or equal to the corresponding value in the
     * depth buffer, and  similarly for depth comparison modes other than
     * LEQUAL. If there is no depth buffer in the current rendering target,
     * this setting has no effect; the fragment will be written anyway..
     * \return None
     */
    void setDepthTestEnabled(A3M_BOOL enable = A3M_TRUE /**< the depth test
                                                        * enable flag */)
    {
      m_depthTestEnabled = enable;
    }

    /** Queries whether depth writing is enabled.
     * \return A3M_TRUE if depth writing is enabled; A3M_FALSE if it's
     * disabled.
     */
    A3M_BOOL isDepthWriteEnabled() const {return m_depthWriteEnabled;}

    /** Enables or disables writing of fragment depth values into the depth
     * buffer. If depth buffering is not enabled in the current rendering
     * target, this setting has no effect; nothing will be written anyway.
     * If both depth testing and depth writing are enabled, and a fragment
     * passes the depth test, that fragment's depth value is written to the
     * depth buffer at the corresponding position. If depth testing is disabled
     * and depth writing is enabled, a fragment's depth value is always written
     * to the depth buffer. If depth writing is disabled, a fragment's depth
     * value is never written to the depth buffer.
     * \return None
     */
    void setDepthWriteEnabled(A3M_BOOL enable = A3M_TRUE
                             /**< the depth write enable flag */)
    {
      m_depthWriteEnabled = enable;
    }

    /** Queries whether the scissor test is enabled.
     * \sa setScissorRectangle()
     * \return TRUE if enabled
     */
    A3M_BOOL isScissorTestEnabled() const { return m_scissorTestEnabled; }

    /** Enables or disables a scissor (clipping area) test.
     * If enabled, a scissor test is performs to determine whether each pixel
     * lies within a scissor (clipping) rectangle.  The buffer is written to
     * only if this test is true.
     */
    void setScissorTestEnabled(A3M_BOOL enable = A3M_TRUE /**< Boolean flag */)
    {
      m_scissorTestEnabled = enable;
    }

    /** Sets the scissor (clipping) rectangle.
     * The clipping rectangle is the area used for the scissor test if it is
     * enabled.
     * \sa setScissorTestEnabled()
     */
    void setScissorRectangle(
        A3M_INT32 left, /**< The left edge of the rectangle */
        A3M_INT32 bottom, /**< The bottom edge of the rectangle */
        A3M_UINT32 width, /**< The width of the rectangle */
        A3M_UINT32 height /**< The height of the rectangle */)
    {
      m_scissorRectangleLeft = left;
      m_scissorRectangleBottom = bottom;
      m_scissorRectangleWidth = width;
      m_scissorRectangleHeight = height;
    }

    /** Gets the scissor (clipping) rectangle.
     */
    void getScissorRectangle(
        A3M_INT32& left, /**< [out] The left edge of the rectangle */
        A3M_INT32& bottom, /**< [out] The bottom edge of the rectangle */
        A3M_UINT32& width, /**< [out] The width of the rectangle */
        A3M_UINT32& height /**< [out] The height of the rectangle */) const
    {
      left = m_scissorRectangleLeft;
      bottom = m_scissorRectangleBottom;
      width = m_scissorRectangleWidth;
      height = m_scissorRectangleHeight;
    }

    /** Enable Compositing Mode with set parameters. The depth buffer is
     * enabled as default. It should be adjusted as need from the
     * application to work combine with isDepthWriteEnable and
     * isDepthTestEnabled functions.
     * \return None.
     */
    void enable() const;

    /** Enable Compositing Mode with set parameters. This
     * version minimises state changes by comparing appearance settings
     * with the supplied CompositingMode which reflects the current state.
     */
    void enable( CompositingMode &other /**< [inout] Compositing mode reflecting
                                             the current state */ ) const;

  private:
    A3M_FLOAT  m_depthOffsetFactor;    /**< the current depth offset factor */
    A3M_FLOAT  m_depthOffsetUnits;     /**< the current depth offset in depth
                                            units */
    A3M_BOOL   m_depthTestEnabled;     /**< the depth testing flag */
    A3M_BOOL   m_depthWriteEnabled;    /**< the depth writing flag */
    ColourMask m_colourWriteMask;      /**< the colour write enable mask in
                                            RGBA format */
    Function   m_depthTestFunc;        /**< the current depth test function */
    Blender    m_blender;              /**< the current Blender object */
    Stencil    m_stencil;              /**< the current Stencil object */

    A3M_BOOL   m_scissorTestEnabled; /**< the scissor test flag */
    A3M_INT32  m_scissorRectangleLeft; /**< the left edge of the scissor
                                            rectangle */
    A3M_INT32  m_scissorRectangleBottom; /**< the left edge of the scissor
                                              rectangle */
    A3M_UINT32 m_scissorRectangleWidth; /**< the width of the scissor
                                             rectangle */
    A3M_UINT32 m_scissorRectangleHeight; /**< the height of the scissor
                                              rectangle */
  };

  /** @} */

} /* namespace a3m */

#endif /* A3M_COMPOSITINGMODE_H */
