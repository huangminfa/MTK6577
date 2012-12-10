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
 * Solid SceneNode class
 *
 */
#pragma once
#ifndef A3M_SOLID_H
#define A3M_SOLID_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/scenenode.h>       /* for SceneNode (base class)               */
#include <a3m/mesh.h>            /* for Mesh class                           */
#include <a3m/appearance.h>      /* for Appearance class                     */

namespace a3m
{
  /** \ingroup a3mScenenodes
   * A Solid object represents a geometrical entity within a scene
   */
  class Solid : public SceneNode
  {
  public:
    A3M_NAME_SHARED_CLASS( Solid )

    /** Smart pointer type for this class */
    typedef SharedPtr< Solid > Ptr;

    /** Type ID for this class */
    static A3M_UINT32 const NODE_TYPE;

    /** Constructor.
     * Constructs an empty solid node with an identity local transformation.
     */
    Solid();

    /** Solid constructor
     * Constructs a Solid from a Mesh and an Appearance
     */
    Solid( Mesh::Ptr const& mesh, /**< [in] mesh object */
           Appearance::Ptr const& appearance /**< [in] appearance object */ );

    // Override
    virtual A3M_UINT32 getType() const { return NODE_TYPE; }

    /** Get Mesh
     * \return The mesh used by this object
     */
    Mesh::Ptr const& getMesh() const { return m_mesh; }

    /** Set Mesh.
     * Changes the Mesh used by this MeshInstance.
     */
    void setMesh( Mesh::Ptr const &mesh /**< [in] new mesh object */ )
    {
      m_mesh = mesh;
    }

    /** Get Appearance
     * \return The appearance used by this object
     */
    Appearance::Ptr const& getAppearance() const { return m_appearance; }

    /** Set Appearance.
     * Changes the Appearance used by this MeshInstance.
     */
    void setAppearance( Appearance::Ptr const& appearance /**< [in] new Appearance */ )
    {
      m_appearance = appearance;
    }

    /** Accept a scene node visitor.
     */
    virtual void accept( SceneNodeVisitor &visitor /**< [in] visitor */ );

  private:
    Mesh::Ptr m_mesh;
    Appearance::Ptr m_appearance;
  };
} /* namespace a3m */

#endif /* A3M_SOLID_H */
