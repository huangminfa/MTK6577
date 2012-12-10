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
 * Simple Renderer
 *
 */
#pragma once
#ifndef A3M_SIMPLERENDERER_H
#define A3M_SIMPLERENDERER_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/propertylinker.h>    /* for PropertyLinker                     */
#include <a3m/assetcachepool.h>    /* for AssetCachePool                     */
#include <a3m/renderer.h>          /* Renderer interface                     */
#include <a3m/scenenodevisitor.h>  /* for SceneNodeVisitor                   */
#include <vector>                  /* for std::vector                        */
#include <a3m/rendertarget.h>      /* for reflection render target           */

/*****************************************************************************
 * A3M Namespace
 *****************************************************************************/
namespace a3m
{
  class RenderTarget;

  /** \todo Integrate into documentation (this is not currently in any groups) */


  /** SimpleRenderer class.
   * An implementation of the Renderer interface. This class was originally
   * written to do the minimum necessary work to display a scene graph. It has
   * grown now to set lighting uniforms and render to texture for reflection
   * effects.
   * \todo design an alternative (a selection of different renderers or a
   *       generic means to assemble a number of render stages).
   */
  class SimpleRenderer : public Renderer, public SceneNodeVisitor
  {
  public:
    /** Default constructor.
     */
    SimpleRenderer(AssetCachePool::Ptr const& pool
                   /**< Cache pool for loading renderer assets */);

    // Override
    void render( Camera &camera, SceneNode &node, FlagMask const& renderFlags,
        FlagMask const& recursiveFlags );

    // Override
    void update( A3M_FLOAT time ) { m_time = time; }

    // Override
    void visit( Solid *solid );

    // Override
    void visit( Light *light );

    /** \cond */
    // These types are public so that free functions in the implementation
    // can access them. They are not part of the interface.
    typedef std::vector< Solid * > RenderList;
    typedef std::vector< Light * > LightList;
    /** \endcond */

  private:
    RenderList m_opaqueSolidsToRender;  // List of solids to be rendered
                                        // (generated each frame).
    RenderList m_blendedSolidsToRender; // List of blended solids to be
                                        // rendered (generated each frame).
    RenderList m_reflectionSolidsToRender; // List of solids using reflection.
                                           // (generated each frame).
    LightList m_lights;                 // List of lights in the scene.

    AssetCachePool::Ptr m_pool; // Pool for asset creation
    PropertyLinker m_linker; // Links global properties with shader programs

    // A render target used for reflection effect. Only created when it is
    // requested.
    RenderTarget::Ptr m_reflectionTarget;

    A3M_FLOAT m_time; // Time to set in uniform "u_time"

    A3M_INT32 m_lightZOrder; // Used to check for an MTK platform (security
                             // code, therefore name is misleading).
  };
  /** @} */

} /* namespace a3m */

#endif /* A3M_SIMPLERENDERER_H */
