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
 * RenderDevice class
 *
 */
#pragma once
#ifndef A3M_RENDERDEVICE_H
#define A3M_RENDERDEVICE_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/base_types.h>            /* Base types used by all A3M modules */

/*****************************************************************************
 * A3M Namespace
 *****************************************************************************/
namespace a3m
{
/** \defgroup a3mRendevice Render Device
 *  \ingroup  a3mRefScene
 *
 *  The Render Device provides a low-level API for rendering operations such as
 *  state manipulation, state queries and primitive rendering.
 *
 *  @{
 */

  // Forward declarations
  class VertexBuffer;
  class IndexBuffer;
  class Background;
  class Appearance;

  /**
   * Render device namespace. This namespace encapsulates functions used for
   * rendering operations.
   *
   */
  namespace RenderDevice
  {

    /** \name Enumerated type 'ErrorCode'.
     * ErroCode enum lists various errors returned by rendering device.
     *
     */
    enum ErrorCode
    {
      ERROR_CODE_NO_ERROR,
      ERROR_CODE_INVALID_ENUM,
      ERROR_CODE_INVALID_VALUE,
      ERROR_CODE_INVALID_OPERATION,
      ERROR_CODE_INVALID_FRAMEBUFFER_OPERATION,
      ERROR_CODE_OUT_OF_MEMORY,
      ERROR_CODE_UNDEFINED
    };

    /*************************************************************************
     * RenderDevice Functions
     *************************************************************************/
    /** \name RenderDevice Functions
     * Below functions are defined within RenderDevice namespace.
     * @{
     */

    /**
     * Returns the far mapping limit for the depth buffer. It specifies
     * the normalized device coordinate mapping of the far clipping plane to
     * window coordinates.
     *
     * \return Normalized far distance of the depth range.
     *         Value ranges between 0 - 1.0 in float.
     */
    A3M_FLOAT getDepthRangeFar();

    /**
     * Returns the near mapping limit for the depth buffer. It specifies
     * the normalized device coordinate mapping of the near clipping plane to
     * window coordinates.
     *
     * \return Normalized near distance of the depth range.
     *         Value ranges between 0 - 1.0 in float.
     */
    A3M_FLOAT getDepthRangeNear();

    /**
     * Returns the height of the current viewport in screen pixels.
     *
     * \return Viewport height in screen pixels. Value [0 - +ve integer].
     */
    A3M_INT32 getViewportHeight();

    /**
     * Returns the width of the current viewport in screen pixels.
     *
     * \return Viewport width in screen pixels.  Value [0 - +ve integer].
     */
    A3M_INT32 getViewportWidth();

    /**
     * Returns the horizontal position of the lower left corner of
     * viewport, in screen pixels. The origin is at the bottom left
     * corner of the screen.
     *
     * \return Viewport x. X increases to the right. Value [-ve to +ve int].
     */
    A3M_INT32 getViewportX();

    /**
     * Returns the vertical position of the lower left corner of
     * viewport, in screen pixels. The origin is at the bottom left
     * corner of the screen.
     *
     * \return Viewport y. Y increases upwards. Value [-ve to +ve integer].
     */
    A3M_INT32 getViewportY();

    /**
     * Specifies the mapping of depth values from normalized device coordinates
     * to window coordinates.
     *
     * \return None
     */
    void setDepthRange(A3M_FLOAT depthNear,
                       /**< Mapping of the near clipping plane to window
                            coordinates. Value is clamped to range [0, 1.0]. */
                       A3M_FLOAT depthFar
                       /**< Mapping of the far clipping plane to window
                            coordinates. Clamped to range [0, 1.0]. */);

    /**
     * Sets a rectangular viewport to render to.
     *
     * This function sets lower left corner of viewport by (x, y) and
     * when a GL context is first attached to a window, viewport width and
     * height are set to the dimensions of that window.
     *
     * \return None
     */
    void setViewport(A3M_INT32 x,
                     /**< Horizontal position of lower left corner of the
                          viewport rectangle. Range [-ve to +ve integer]. */
                     A3M_INT32 y,
                     /**< Vertical position of lower left corner of the
                          viewport rectangle. Range [-ve to +ve integer]. */
                     A3M_INT32 width,
                     /**< Viewport width. Must be positive integer. Range
                          depends on the implementation. To find range use
                          glGet with argument GL_MAX_VIEWPORT_DIMS */
                     A3M_INT32 height
                     /**< Viewport height. Must be positive integer. Range
                          depends on the implementation. To find range use
                          glGet with argument GL_MAX_VIEWPORT_DIMS */);

    /**
     * Retrieves the last error reported by the
     * rendering device as an ErrorCode enum.
     *
     * \return Enumerated error code.
     */
    ErrorCode getErrorCode();

    /**
     * Retrieves the last error reported by the
     * rendering device as a string.
     *
     * It converts ErrorCode enum in to an appropriate string.
     *
     * \return Pointer to NULL-terminated error string
     */
    const A3M_CHAR8* getErrorString();

    /**
     * Retrieves the vendor name for this rendering device
     * as a string.
     *
     * \return Pointer to NULL-terminated string.
     */
    const A3M_CHAR8* getVendor();

    /**
     * Retrieves the version for this rendering device
     * as a string.
     *
     * \return Pointer to NULL-terminated version string.
     */
    const A3M_CHAR8* getVersion();

    /**
     * Returns a space-separated string list of supported extensions.
     *
     * \return Pointer to space seperated and NULL-terminated list of strings.
     */
    const A3M_CHAR8* getExtensions();

    /**
     * Retrieves the shader language version supported by
     * this rendering device as a string.
     *
     * \return Pointer to NULL-terminated shader (language) version string.
     */
    const A3M_CHAR8* getShaderVersion();

    /**
     * Renders primitives specified by vertex buffer and index buffer.
     *
     * \return None.
     */
    void render(VertexBuffer& vb,
                /**< Reference to vertex buffer object */
                IndexBuffer& ib,
                /**< Reference to index buffer object */
                Appearance& app
                /**< Reference to appearance object */);


    /**
     * Clear frame buffer.
     * Clears the colour, depth and stencil buffers with clear values specified
     * by bg. Applies buffer clear-masks before clearing buffers.
     *
     * \return None.
     */
    void clear(Background const& bg /**< Reference to Background object */);

    /**
     * Clears specified parts of the frame buffer.
     * Clears the specified parts of the frame buffer with the clear values
     * currently in the global state.
     */
    void clear(A3M_BOOL clearColour, /**< Set TRUE to clear colour buffer */
               A3M_BOOL clearDepth, /**< Set TRUE to clear depth buffer */
               A3M_BOOL clearStencil /**< Set TRUE to clear stencil buffer */);

    /**
     * Clears all parts of the frame buffer.
     */
    void clear();

    /**
     * Get pixels from the colour buffer
     * Use this function to read the pixel values from a rectangular region of
     * the current colour buffer (may be the device or a RenderTarget object)
     * into a supplied region of memory. The pointer you supply to this routine
     * must point to an array of at least width*height*4 bytes. Each pixel is
     * four bytes in the order RGBA.
     */
    void getPixels( A3M_INT32 x, /**< [in] viewport x coordinate of region */
                    A3M_INT32 y, /**< [in] viewport y coordinate of region
                                      (from bottom of viewport)*/
                    A3M_UINT32 width,  /**< [in] width of region to read */
                    A3M_UINT32 height, /**< [in] height of region to read */
                    A3M_UINT8 *pixels  /**< [out] pixel buffer */ );

    void resetState();

  } /* namespace RenderDevice */

  /** @} */

} /* namespace a3m */

#endif /* A3M_RENDERDEVICE_H */

