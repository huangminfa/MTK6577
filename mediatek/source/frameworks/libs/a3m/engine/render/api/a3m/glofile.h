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
 * GLO file class
 *
 */
#pragma once
#ifndef A3M_GLO_FILE_H
#define A3M_GLO_FILE_H
#include <a3m/scenenode.h> /* SceneNode class (member of Glo struct)      */
#include <a3m/animation.h> /* AnimationGroup class (member of Glo struct) */

namespace a3m
{
  class AnimationGroup;
  class AssetCachePool;

  /** \todo Integrate into documentation (this is not currently in any groups) */

  /** GLO struct
   * A simple container for a scene node and an animation (returned from
   * loadGloFile).
   */
  struct Glo
  {
    SceneNode::Ptr node;            /**< root of scene created from file */
    AnimationGroup::Ptr animation;  /**< animation created from file */
  };

  /** Load GLO File.
   * The file specified by fileName is loaded. The contents of the file are
   * returned in the GLO node and animation members of a GLO struct. Either
   * of these may be null if the file contains only geometry or only animation.
   * The SceneNode contained in the GLO structure is NOT added to the scene.
   * The scene parameter exists to link any animation in the file to existing
   * scene nodes.
   *
   * \return GLO struct representing the contents of the file.
   */
  Glo loadGloFile( AssetCachePool &pool, /**< Pool of asset caches to use to
                                           create the assets (textures, shader
                                           programs, etc.)*/
                   SceneNode::Ptr const &scene, /**< Root of scene graph to use
                                                  when linking animations to
                                                  scene nodes */
                   A3M_CHAR8 const *fileName /**< Name of file to load */ );
}


#endif // A3M_GLO_FILE_H
