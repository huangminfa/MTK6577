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
 *  Renderer class
 */
#pragma once
#ifndef A3M_RENDERER_H
#define A3M_RENDERER_H

#include <a3m/base_types.h>             /* included for A3M_FLOAT */

/*****************************************************************************
 * A3M Namespace
 *****************************************************************************/
namespace a3m
{
  /** \defgroup a3mRenderer A3M Renderer
   * \ingroup  a3mRefRender
   *
   * Abstract base class for renderers.
   *
   * @{
   */

  class SceneNode;
  class Solid;
  class Camera;
  class Light;
  class FlagMask;

  /**
   * Renderer class
   *
   */
  class Renderer
  {
  public:
    /**
     * Virtual destructor so that subclasses are destroyed properly.
     */
    virtual ~Renderer() {}

    /**
     * Render the scene graph starting at the given node.
     *
     */
    virtual void render(
        Camera &camera,  /**< Camera used to render the scene */
        SceneNode &node, /**< Root of the rendered scene graph */
        FlagMask const& renderFlags, /**< Mask for specifying which scene nodes
                                          to include in render */
        FlagMask const& recursiveFlags /**< Mask specifying whether flags affect
                                            the scene graph recursively when in
                                            their non-default state */ ) = 0;

    /**
     * Update the time for shader based effects.
     */
    virtual void update( A3M_FLOAT timeInSeconds
                         /**< Time (mod 60) to set in uniform u_time */ ) {}
  };
  /** @} */

} /* namespace a3m */

#endif /* A3M_CAMERA_H */
