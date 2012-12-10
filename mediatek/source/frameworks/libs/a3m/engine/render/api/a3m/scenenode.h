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
 * Scene Node class
 *
 */
#pragma once
#ifndef A3M_SCENENODE_H
#define A3M_SCENENODE_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/pointer.h>       /* for SharedPtr                     */
#include <a3m/matrix4.h>       /* 4D Matrix maths - Matrix4f        */
#include <a3m/quaternion.h>    /* for Quaternionf                   */
#include <a3m/vector3.h>       /* for Vector3f                      */
#include <a3m/assert.h>        /* for A3M_ASSERT                    */
#include <a3m/flags.h>         /* for FlagMask and FlagSet          */

#include <string>            /* std::string in m_name (node's name) */
#include <vector>              /* std::vector                       */

namespace a3m
{
  class SceneNodeVisitor;

  /** \todo Add top-level description of scene graph, then make
   * these sub-sections of it */

  /** \defgroup a3mScenenodes Scene Node
   *  \ingroup a3mRefScene
   *
   * @{
   */

  /**
   * A SceneNode object keeps its transformation relative to its parent and a
   * list of child nodes.
   */
  class SceneNode : public Shared, NonCopyable
  {
  public:
    A3M_NAME_SHARED_CLASS( SceneNode )

    /** Smart pointer type for this class */
    typedef SharedPtr< SceneNode > Ptr;

    /** Type ID for this class */
    static A3M_UINT32 const NODE_TYPE;

    /** Constructor.
     * Constructs an empty scene node with an identity local transformation.
     */
    SceneNode();

    /**
     * Virtual destructor so that subclasses are destroyed properly.
     * Note that children do not need to be explicitly destroyed (they will be
     * deleted if no other shared pointer points to them)
     */
    virtual ~SceneNode() {}

    /** Get the node type ID.
     * This can be compared again the static NODE_TYPE fields for the different
     * scene node classes, to determine which type was instantiated.
     * \return type ID of this scene node
     */
    virtual A3M_UINT32 getType() const { return NODE_TYPE; }

    /** Get scene node name.
     * \return name for this scene node.
     */
    A3M_CHAR8 const *getName() const { return m_name.c_str(); }

    /** Get scene node name.
     */
    void setName( A3M_CHAR8 const * name /**< [in] new name for this scene node */ )
    {
      m_name = name;
    }

    /** Set parent of a scene node.
     * If the node already had a parent it will be removed from there before
     * being added to the new parent.
     */
    void setParent( Ptr const &parent, /**< [in] new parent node */
                    A3M_BOOL preserveWorldTransform = A3M_FALSE
                      /**< [in] adjust local transform so that the
                       child's world transform will be the same as
                       it was when the scene graph is next updated */ );

    /** Set rotation relative to parent.
     */
    void setRotation( Quaternionf const &rotation /**< [in] axis of rotation */);

    /** Set scale relative to parent.
     */
    void setScale( Vector3f const &scale /**< [in] scale */ );

    /** Set position relative to parent.
     */
    void setPosition( Vector3f const &position /**< [in] position */ );

    /** Set transform relative to parent.
     * Note that this transform will be over-written if any of setRotation,
     * setScale or setPosition is called.
     */
    void setLocalTransform( Matrix4f const &tran /**< [in] transform */ );

    /** Get rotation relative to parent.
     * \return the quaternion representing the rotation
     */
    Quaternionf const &getRotation() const { return m_rotation; }

    /** Set scale relative to parent.
     * \return the scale relative to parent.
     */
    Vector3f const &getScale() const { return m_scale; }

    /** Get position relative to parent.
     * \return the position relative to parent.
     */
    Vector3f const &getPosition() const { return m_position; }

    /** Get scene node's local transformation.
     * \return the transformation of this node relative to its parent.
     */
    Matrix4f const &getLocalTransform() const;

    /** Get scene node's global transformation.
     * \return the absolute transformation of this node.
     */
    Matrix4f const &getWorldTransform() const;

    /** Find the given node anywhere under this node.
     * \return pointer to the node if found, null pointer otherwise. */
    Ptr find( A3M_CHAR8 const * name /**< [in] name of node to find */ );

    /** Get this node's parent.
     * \return pointer to parent node.
     */
    Ptr getParent() const { return Ptr( m_parent ); }

    /** Accept a scene node visitor.
     */
    virtual void accept( SceneNodeVisitor &visitor /**< [in] visitor */ );

    /** Get number of children.
     * \return number of children
     */
    A3M_UINT32 getChildCount() const { return m_children.size(); }

    /** Get child.
     * \return child pointer for given index
     */
    Ptr getChild( A3M_UINT32 i /**< index of child */ ) const
    {
      A3M_ASSERT( i < m_children.size() );
      return m_children[ i ];
    }

    /** Sets the general purpose flags for this scene node.
     * Flags will generally be used to filter nodes when querying the scene
     * graph.  A common usage is to control the visibility of scene nodes by
     * cutting out parts of the scene graph when rendering.  However, flags may
     * be used to represent any generic boolean setting.
     */
    void setFlags(FlagSet const& flags /**< Flags */) { m_flags = flags; }

    /** Returns the general purpose flags for this scene node.
     * \return Flags
     */
    FlagSet const& getFlags() const { return m_flags; }

  private:
    /* Remove child.
     * Removes the specified child from this scene node. The child will
     * not be deleted unless this operation causes the shared reference count
     * to drop to zero.
     */
    void removeChild( Ptr const &child /**< Child to be removed */ );

    /* Sets the transform changed flag recursively.
     */
    void setTransformChanged();

    /* Sets the transform changed flag recursively.
     */
    void setWorldTransformChanged();

    /* Update scene graph transformations.
     * This function is called by getWorldTransform() to lazily update the scene
     * graph transformation matrices.  Only portions of the scene graph marked
     * as dirty are updated.
     */
    void update() const;

    /* Update world transformations of all nodes. */
    void updateAll( Matrix4f const &parentTransform ) const;

    /* Update local transformation of this nodes. */
    void updateLocal() const;

    /* find the given node anywhere under this node. */
    A3M_BOOL findNode( a3m::SceneNode::Ptr const &node ) const;

    std::string m_name;           /* This node's name */

    Vector3f m_scale;             /* This node's relative scale */
    Quaternionf m_rotation;       /* This node's relative rotation */
    Vector3f m_position;          /* This node's relative position */
    mutable Matrix4f m_localTransform;    /* This node's local transformation */
    mutable Matrix4f m_worldTransform;    /* This node's world transformation */
    mutable A3M_BOOL m_localTransformChanged;  /* true if this node's local
                                             transformation has changed since
                                             the last update */
    mutable A3M_BOOL m_worldTransformChanged;  /* true if this node's world
                                             transformation has changed since
                                             the last update */
    FlagSet m_flags;              /* General-purpose flags */

    typedef std::vector <Ptr> ChildList;
    ChildList m_children; /* Collection of children */

    SceneNode *m_parent;
  };
  /** @} */

} /* namespace a3m */

#endif /* A3M_SCENENODE_H */
