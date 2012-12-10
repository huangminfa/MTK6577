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
 * Background class
 *
 */
#pragma once
#ifndef A3M_BACKGROUND_H
#define A3M_BACKGROUND_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/base_types.h>            /* Base types used by all A3M modules */
#include <a3m/colour.h>               /* for Colour4f                       */

/*****************************************************************************
 * A3M Namespace
 *****************************************************************************/
namespace a3m
{
  /** \defgroup a3mBackground Background
   * \ingroup  a3mRefScene
   *
   * The background class contains member functions to clear buffers (colour, depth
   * and stencil) and buffer masks. You can use them by creating an object to
   * it and calling them. Mainly you need to call enable() function at the
   * start of rendering loop to clear buffers with previously set clear values.
   *
   * Example:
   * \code
   *   // Create an instance of Background class
   *   a3m::Background bg;
   *   // Define colour buffer clear value
   *   bg.setColour(0.5f, 0.5f, 0.5f, 1.0f);
   *   // Applie colour buffer clear mask
   *   bg.setColourClearMask( A3M_FALSE, A3M_TRUE, A3M_TRUE, A3M_FALSE );
   *
   *   // Clear colour and other buffers based on their corresponding masks
   *   bg.enable();
   * \endcode
   * @{
   */

  /**
   * Background class.
   *
   * It can set buffer clear values and masks (to decide whether buffers or
   * some components of them are write proof or not).
   * Currently set clear values and masks for buffers can be queried also.
   * Use enable() member function to clear buffers at the start of rendering
   * loop.
   */
  class Background
  {
  public:
    /**
     * Structure ColurMask
     *
     * Each element of this structure represents whether that corresponding
     * colour component in colour buffer can be modified or not. If mask is
     * A3M_FALSE then, that colour component is non-writable.
     *
     */
    struct ColourMask
    {
      /** ColourMask default constructor */
      ColourMask () : r(A3M_TRUE), g(A3M_TRUE), b(A3M_TRUE), a(A3M_TRUE) {}
      /** ColourMask constructor with arguments */
      ColourMask ( A3M_BOOL r, A3M_BOOL g, A3M_BOOL b, A3M_BOOL a )
        : r(r), g(g), b(b), a(a) {}

      A3M_BOOL r; /**< Mask for red colour component in colour buffer */
      A3M_BOOL g; /**< Mask for green colour component in colour buffer */
      A3M_BOOL b; /**< Mask for blue colour component in colour buffer */
      A3M_BOOL a; /**< Mask for alpha component in colour buffer */
    };

  /**
   * Constructor
   *
   * Initializes all buffer clear values and their masks with default values.
   */
  Background();

  /**
   * Constructor
   *
   * Initializes with the supplied values, each in the range 0.0 to 1.0.
   */
  Background( /** Red component   */ A3M_FLOAT r,
              /** Green component */ A3M_FLOAT g,
              /** Blue component  */ A3M_FLOAT b,
              /** Alpha, 0.0 = transparent */ A3M_FLOAT a );

  /**
   * Destructor
   *
   * It is default deconstructor.
   */
  ~Background();

  /**
   * Sets the background colour for colour buffer.
   *
   * \return None.
   */
  void setColour( A3M_FLOAT r, /**< Red colour component. Range [0 1.0f] */
                  A3M_FLOAT g, /**< Green colour component. Range [0 1.0f] */
                  A3M_FLOAT b, /**< Blue colour component. Range [0 1.0f] */
                  A3M_FLOAT a  /**< Alpha component. Range [0 1.0f] */ );

  /**
   * Sets the background colour which is used when colour buffer is cleared.
   *
   * \return None.
   */
  void setColour( const Colour4f& colour /**< Colour buffer clear value */ );

  /**
   * Sets clear value for depth buffer.
   *
   * \return None.
   */
  void setDepth( A3M_FLOAT depth /**< Depth clear value. Range [0 1.0f] */ );

  /**
   * Sets clear value for stencil buffer.
   *
   * \return None.
   */
  void setStencil( A3M_INT32 stencil /**< Stencil clear value. */ );

  /**
   * Specifies whether red, green, blue, and alpha can or cannot be written
   * into the colour buffer. If mask for a colour component is 0 (A3M_FALSE)
   * then, that colour component in colour buffer is not writable.
   *
   * \return None.
   */
  void setColourClearMask( A3M_BOOL rMask,
                           /**< Mask for red colour component */
                           A3M_BOOL gMask,
                           /**< Mask for green colour component */
                           A3M_BOOL bMask,
                           /**< Mask for blue colour component */
                           A3M_BOOL aMask
                           /**< Mask for alpha component */ );

  /**
   * Specifies whether red, green, blue, and alpha can or cannot be written
   * into the colour buffer. If mask for a colour component is 0 (A3M_FALSE)
   * then, that colour component in colour buffer is not writable.
   *
   * \return None.
   */
  void setColourClearMask( const ColourMask& colourMask
                           /**< Reference to ColourMask struct variable */ );

  /**
   * It sets the depth buffer clear mask value.
   * If mask is A3M_FALSE then, depth buffer writing is disabled. Otherwise,
   * it is enabled.
   *
   * \return None.
   */
  void setDepthClearMask( A3M_BOOL depthMask /**< Depth buffer clear mask */ );

  /**
   * Controls the front and back writing of individual bits in the stencil
   * planes.
   * Where a 1 (A3M_TRUE) appears in the mask, it's possible to write to
   * the corresponding bit in the stencil buffer. Where a 0 (A3M_FALSE)
   * appears, the corresponding bit is write-protected.
   *
   * \return None.
   */
  void setStencilClearMask( A3M_UINT32 stencilMask
                            /**< Bit mask to enable and disable writing of
                                 individual bits in the stencil planes */ );

  /**
   * Retrieves the background colour clear value.
   *
   * \return Colour clear value in a3m::Colour4f struct format.
   */
  const Colour4f& getColour() { return m_colour; }

  /**
   * Returns the background depth clear value.
   *
   * \return Depth clear value.
   */
  A3M_FLOAT getDepth() { return m_depth; }

  /**
   * Returns the background stencil clear value.
   *
   * \return Stencil clear value.
   */
  A3M_INT32 getStencil() { return m_stencil; }

  /**
   * Retrieves the background colour clear mask value.
   *
   * \return Colour clear mask value in ColourMask struct format.
   */
  const ColourMask& getColourClearMask() { return m_colourMask; }

  /**
   * Returns the background depth clear mask value.
   *
   * \return Depth clear mask value.
   */
  A3M_BOOL getDepthClearMask() { return m_depthMask; }

  /**
   * Returns the background stencil clear mask value.
   *
   * \return Stencil clear mask value.
   */
  A3M_UINT32 getStencilClearMask() { return m_stencilMask; }

  /**
   * Clears all buffers (colour, depth and stencil) with set values.
   * It applies all buffer clear-masks, clears buffers with set clear values
   * controlled by set buffer masks.
   * This should be called at the start of rendering loop to clear all buffers
   * before starting to render scene.
   *
   * \return None.
   */
  void enable() const;

  private:
    Colour4f     m_colour;        /* Colour storing all colour components
                                     in a3m::Colour4f struct format       */
    A3M_FLOAT    m_depth;         /* Depth clear value                    */
    A3M_INT32    m_stencil;       /* Stencil clear value                  */
    ColourMask   m_colourMask;    /* Colour mask for colour buffer        */
    A3M_BOOL     m_depthMask;     /* Depth mask for depth buffer          */
    A3M_UINT32   m_stencilMask;   /* Stencil mask for stencil buffer      */
  };

  /** @} */

} /* a3m namespace */

#endif /* A3M_BACKGROUND_H */
