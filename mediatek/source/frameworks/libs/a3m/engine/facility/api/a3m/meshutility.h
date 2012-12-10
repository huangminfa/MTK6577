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
 * Mesh utility functions
 *
 */
#pragma once
#ifndef A3M_MESHUTILITY_H
#define A3M_MESHUTILITY_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/mesh.h> /* for Mesh */

namespace a3m
{
  /** \defgroup a3mMeshUtility MeshUtility
   * \ingroup  a3mRefScene
   *
   * Utility functions for creating commonly used mesh primitives.
   *
   * @{
   */

  /**
   * Creates a unit square mesh.
   * The square will be centred around the origin, with its normal in the
   * positive z-direction: (0, 0, 1).
   *
   * \return Square mesh
   */
  Mesh::Ptr createSquareMesh(
      MeshCache& meshCache,
      /**< Cache used to create Mesh */
      Vector2f const& uvScale
      /**< Scale factor applied to the UV coordinates */);

  /**
   * Creates a unit cube mesh.
   * The cube will be centred around the origin, with the face normals
   * aligned with the axes.
   *
   * \return Cube mesh
   */
  Mesh::Ptr createCubeMesh(
      MeshCache& meshCache,
      /**< Cache used to create Mesh */
      Vector2f const& uvScale
      /**< Scale factor applied to the UV coordinates */);

  /**
   * Creates a unit sphere mesh (radius == 0.5).
   * The sphere will be centred around the origin, and constructed from a
   * specified number of latitudinal and longitudinal divisions, where the
   * north and south poles are aligned with the y-axis.  The segment and wedge
   * counts will be constrained to the minimum value.
   *
   * \return Sphere mesh
   */
  Mesh::Ptr createSphereMesh(
      MeshCache& meshCache,
      /**< Cache used to create Mesh */
      A3M_UINT32 segmentCount,
      /**< The number of spherical segments (latitudinal).
       * The minimum number of segments is 2 */
      A3M_UINT32 wedgeCount,
      /**< The number of spherical wedges (longitudinal).
       * The minimum number of wedges is 2 */
      Vector2f const& uvScale
      /**< Scale factor applied to the UV coordinates */);

  /** @} */

} /* namespace a3m */

#endif /* A3M_MESHUTILITY_H */

