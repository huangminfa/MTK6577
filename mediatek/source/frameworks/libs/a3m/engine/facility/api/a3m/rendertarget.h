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
 * RenderTarget class
 *
 */

#pragma once
#ifndef A3M_RENDERTARGET_H
#define A3M_RENDERTARGET_H

/******************************************************************************
 * Include Files
 ******************************************************************************/
#include <a3m/texture2d.h>     /* for Texture2D                             */

/******************************************************************************
 * A3M Namespace
 ******************************************************************************/
namespace a3m
{
  /** \defgroup a3mRentarget Render Target
   * \ingroup  a3mRefScene
   * A texture to render to.
   *
   * @{
   */

  /**
   * A render target object contains a texture which may be rendered to and then
   * used as a normal texture. A RenderTarget object is constructed with a
   * Texture2D to be used as the colour buffer and parameters to specify whether
   * a depth buffer and/or stencil buffer is also required. Note that whilst it
   * is also possible to specify a texture to use as the depth buffer, not all
   * devices support this functionality (the GL_OES_depth_texture extension must
   * be present).
   *
   * After a RenderTarget has been constructed you may direct future draw
   * operations to it by calling the enable() method. You will typically want to
   * set the view port with RenderDevice::setViewport() so that screen space
   * coordinates are correctly mapped to the RenderTarget. Call disable() when
   * you have finished drawing to the render target and getColourTexture() to
   * access the texture. You will usually bind the texture to a ShaderUniform.
   *
   * \code
   * TextureParameters params(
   *   TextureParameters::NEAREST,
   *   TextureParameters::NEAREST,
   *   TextureParameters::CLAMP,
   *   TextureParameters::CLAMP );
   *
   * Texture2D::Ptr depthTexture( new Texture2D( SHADOW_MAP_SIZE,
   *   SHADOW_MAP_SIZE, 0, a3m::Texture::RGBA, a3m::Texture::UNSIGNED_BYTE,
   *   params ) );
   *
   * RenderTarget::Ptr renderTarget( new RenderTarget( depthTexture,
   *                                                   Texture2D::Ptr() ) );
   *
   * renderTarget->enable();
   *
   * RenderDevice::setViewport( 0, 0, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE );
   *
   * Background bg;
   * bg.setColour( 1.0, 1.0, 1.0, 1.0 );
   * RenderDevice::clear(bg);
   *
   * // More draw code would go here.
   *
   * renderTarget->disable();
   *
   * // Now use the render target texture whilst drawing on the device
   * appearance.setUniformValue( "u_l_shadowMap",
   *                             renderTarget->getColourTexture() );
   * \endcode
   */
  class RenderTarget : public Shared, NonCopyable
  {
  public:
    A3M_NAME_SHARED_CLASS( RenderTarget )

    /** Smart pointer type for this class */
    typedef SharedPtr< RenderTarget > Ptr;

    /**
     * Constructor
     */
    RenderTarget( Texture2D::Ptr const &colourTexture, /**< [in] colour texture
                                                       target */
                  Texture2D::Ptr const &depthTexture,  /**< [in] depth texture
                                                       target */
                  A3M_BOOL depthBuffer = A3M_TRUE, /**< [in] use depth buffer if
                                                   no depth texture specified */
                  A3M_BOOL stencilBuffer = A3M_FALSE /**< [in] use stencil
                                                     buffer */ );

    /**
     * Destructor
     */
    ~RenderTarget();

    /**
     * Colour texture access
     * \return shared pointer to colour texture
     */
    Texture2D::Ptr const &getColourTexture() const { return m_colourTexture; }

    /**
     * Depth texture access
     * \return shared pointer to depth texture
     */
    Texture2D::Ptr const &getDepthTexture() const { return m_depthTexture; }

    /** Enable this render target.
     * Make this the current render target. Subsequently drawn primitives will
     * be rendered to this target until disable is called.
     */
    void enable();

    /** Disable this render target.
     * Stop using this render target. Subsequently drawn primitives will be
     * rendered to the device buffer.
     */
    void disable() const;

    /** Validate the render target
     * \return A3M_TRUE if the render target is valid and complete
     */
    A3M_BOOL isValid() { return m_valid; }

  private:
    A3M_UINT32 m_framebuffer;
    Texture2D::Ptr m_colourTexture;
    Texture2D::Ptr m_depthTexture;
    A3M_UINT32 m_depthRenderbuffer;
    A3M_UINT32 m_stencilRenderbuffer;
    A3M_BOOL m_valid;
  };

  /** @} */
} // End of namespace a3m

#endif // A3M_RENDERTARGET_H
