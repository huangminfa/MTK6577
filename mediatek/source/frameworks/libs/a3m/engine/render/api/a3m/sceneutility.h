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
 * Scene Node Utility Functions
 *
 */
#pragma once
#ifndef A3M_SCENEUTILITY_H
#define A3M_SCENEUTILITY_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/pointer.h>  /* for SharedPtr */
#include <a3m/solid.h>  /* for Solid */
#include <a3m/vector2.h>  /* for Vector2f */

namespace a3m
{
  /** \defgroup a3mSceneutility Scene Utility
   * \ingroup  a3mScenenodes
   *
   * Utility functions for performing commonly required scene operations.
   *
   * @{
   */

  // Forward declarations
  class Appearance;
  class AssetCachePool;
  class SceneNode;
  class ShaderProgram;

  /** Set Shader Program for Node.
   * Sets the shader program for all Solid objects belonging to the supplied
   * SceneNode recursively.
   */
  void setShaderProgram( SceneNode &node, /**< Scene node to change */
                         SharedPtr< ShaderProgram > const &program
                         /**< ShaderProgram to use for all descendent Solids */
                        );

  /** Set Appearance for Node.
   * Sets the shader program for all Solid objects belonging to the supplied
   * SceneNode recursively.
   */
  void setAppearance( SceneNode &node, /**< Scene node to change */
                      SharedPtr< Appearance > const &appearance
                         /**< Appearance to use for all descendent Solids */
                    );

  /**
   * Points a scene node at a global coordinate in space.
   * The node's local position Z-axis will be pointed at the coordinate.
   */
  void pointAt(
      SceneNode& node,
      /**< Scene node to point */
      Vector3f const& target,
      /**< Global coordinate to point at */
      Vector3f const& up = Vector3f::Y_AXIS
      /**< Global "up" vector with which to align node */);

  /**
   * Points a scene node at another scene node.
   * The node's local position Z-axis will be pointed at the other node.
   */
  void pointAt(
      SceneNode& node,
      /**< Scene node to point */
      SceneNode const& target,
      /**< Scene node to point at */
      Vector3f const& up = Vector3f::Y_AXIS
      /**< Global "up" vector with which to align node */);

  /**
   * Creates a unit square solid.
   * \copydetails createSquareMesh()
   *
   * \return Square solid
   */
  Solid::Ptr createSquare(
      AssetCachePool& assetCachePool,
      /**< Asset pool used to manage assets and resources */
      Vector2f const& uvScale
      /**< Scale factor applied to the UV coordinates */);

  /**
   * Creates a unit cube solid.
   * \copydetails createCubeMesh()
   *
   * \return Cube solid
   */
  Solid::Ptr createCube(
      AssetCachePool& assetCachePool,
      /**< Asset pool used to manage assets and resources */
      Vector2f const& uvScale
      /**< Scale factor applied to the UV coordinates */);

  /**
   * Creates a unit sphere solid (radius == 0.5).
   * \copydetails createSphereMesh()
   *
   * \return Sphere solid
   */
  Solid::Ptr createSphere(
      AssetCachePool& assetCachePool,
      /**< Asset pool used to manage assets and resources */
      A3M_UINT32 segmentCount,
      /**< The number of spherical segments (latitudinal)
       * The minimum number of segments is 2 */
      A3M_UINT32 wedgeCount,
      /**< The number of spherical wedges (longitudinal)
       * The minimum number of wedges is 2 */
      Vector2f const& uvScale
      /**< Scale factor applied to the UV coordinates */);

  /**
   * Visits a node and all of its children recursively.
   */
  void visitScene(SceneNodeVisitor& visitor, /**< Visitor to use */
                  SceneNode& node /**< First node to visit */);

  /**
   * Conditionally visits a node and all of its children recursively.
   * The condition for visiting any node is set by the filter flags.  All flags
   * passed to the function must be set in a node for the visit to occur.  A
   * particular flag can recursively affect child nodes if it is set in the
   * recursive flags mask.  Flags only recurse if they are set to a value other
   * than their default (which may be TRUE or FALSE).
   */
  void visitScene(SceneNodeVisitor& visitor, /**< Visitor to use */
                  SceneNode& node, /**< First node to visit */
                  FlagMask const& filterFlags, /**< Flags required for visit */
                  FlagMask const& recursiveFlags /**< Flags treated as
                                                      recursive */);

  /**
   * Sets the state of one or more flags for this node.
   */
  void setFlags(
      SceneNode& node, /**< Scene node whose flags to set */
      FlagMask const& mask, /**< Mask defining which flags to set */
      A3M_BOOL state /**< State to which the flags will be set */ );

  /** Returns the state of one or more flags for this node.
   * The state of several flags may be checked at once by providing a mask with
   * multiple set bits (TRUE will be returned only if all of the flags are
   * set).
   * \return TRUE if all the specified flags are set.
   */
  inline A3M_BOOL getFlags(
      SceneNode const& node, /**< Scene node whose flags to check */
      FlagMask const& mask /**< Mask defining which flags to check */ )
  {
    return node.getFlags().get( mask );
  }

  /** Checks the derived state of one or more flags for this node.
   * Derived flags take into account the state of the flags of parents of the
   * scene node (not just the direct parent, but also their parents, and so
   * on).  A flag state is only inherited from a parent when it is set to the
   * non-default state (e.g. a flag called VISIBLE whose default state is TRUE
   * will only be inherited if it is set to FALSE; conversely, a flag called
   * HIDDEN whose default state is FALSE will only be inherited when it is set
   * to TRUE).
   *
   * \return TRUE if all the specified derived flags are set.
   */
  A3M_BOOL getDerivedFlags(
      SceneNode const& node, /**< Scene node whose flags to check */
      FlagMask const& mask /**< Mask defining which flags to check */ );

  /** @} */

} /* namespace a3m */

#endif /* A3M_SCENEUTILITY_H */
