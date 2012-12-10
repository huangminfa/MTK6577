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
 * SceneNodeVisitor base class
 *
 */
#pragma once
#ifndef A3M_SCENENODEVISITOR_H
#define A3M_SCENENODEVISITOR_H

/*****************************************************************************
 * A3M Namespace
 *****************************************************************************/
namespace a3m
{
  /** \defgroup a3mNodevisitor Scene Node 'Visitor'
   * \ingroup  a3mRefRender
   *
   * Abstract base class for scene node visitors.
   *
   * This is an implementation of the 'Vistor Pattern'. The Visitor pattern is
   * useful when you have a fixed class (and reasonably small) inheritance
   * hierarchy, but might have a large (and extensible) number of operations
   * you wish to perform on objects belonging to the hierarchy. It is an
   * alternative to having to constantly increase the number of virtual
   * functions in the base class.
   *
   * Example:
   * Imagine we want a new function to replace the shader program on all Solid
   * objects in a scene graph. We could do this by adding a new virtual
   * function to SceneNode:
   *
   * \code
   * class SceneNode
   * {
   *   ...
   *   void replaceShaderProgram( ShaderProgram::Ptr const &program )
   *   {
   *      // Call replaceShaderProgram() on all children.
   *      ...
   *   }
   *   ...
   * };
   * \endcode
   *
   * this is overridden in Solid, but ignored in Camera and Light
   *
   * \code
   * class Solid
   * {
   *   ...
   *   void replaceShaderProgram( ShaderProgram::Ptr const &program )
   *   {
   *      // Remember to call base class in case we have any children.
   *      SceneNode::replaceShaderProgram( program );
   *      m_appearance->setShaderProgram( program );
   *   }
   *   ...
   * };
   * \endcode
   *
   * Obviously, each time we add an operation, we have to add a new function
   * to the base class. This has three big disadvantages:
   *
   * -# Our base class is going to be huge!
   * -# Our base class will constantly change. We'd prefer it to be stable.
   * -# Our base class will have to 'know' about all the classes used as
   *    parameters (or at least have them forward declared)
   *
   * Alternatively, we could do a lot of run-time type checking and/or dynamic
   * casting, to make sure we only call replaceShaderProgram() on Solid objects.
   * This has its own draw-backs.
   *
   * Isn't there a better way?
   *
   * Why yes; there is.
   *
   * We can take advantage of the fixed number of classes in our hierarchy to
   * make the objects belonging to the hierarchy do some of the virtual
   * function calling. This is sometimes called double-dispatch in the object
   * oriented world (where function calls are messages being dispatched),
   * because we are choosing the actual method to be called dynamically
   * (i.e. at runtime) depending both on the type of the object and the
   * operation we are performing. Let's see it in action.
   *
   * Again we want to change the shader program of all the solids in a scene:
   *
   * \code
   * class ShaderProgramSetter : public SceneNodeVisitor
   * {
   * public:
   *   ShaderProgramSetter( ShaderProgram::Ptr program ) :
   *       m_program( program ) {}
   *
   *   void visit( Solid *solid )
   *   {
   *     solid->getAppearance()->setShaderProgram( m_program );
   *   }
   *
   * private:
   *   ShaderProgram::Ptr m_program;
   * };
   * \endcode
   *
   * To use it, create a ShaderProgramSetter object and use the visitScene()
   * utility function in sceneutility.h, which traverses the scene graph,
   * asking all of the scene nodes to 'accept' it:
   *
   * \code
   * ShaderProgramSetter setter( program );
   * visitScene( setter, *m_scene ); // Calls accept() for each SceneNode
   * \endcode
   *
   * We can write this class anywhere. It could be in an anonymous namespace in
   * a cpp file. We haven't had to change the base class or the Solid class to
   * implement a new feature which works polymorphically on a scene graph.
   *
   * The SceneNode::accept() member function is overridden by each SceneNode
   * subclass to call the correct SceneNodeVisitor::visit() overloaded member.
   *
   * There are variations on the Visitor pattern. In the version used here, the
   * base-class (SceneNode) version of accept() does not call accept() on any
   * of its children, hence why visitScene() is provided.  Visiting a single
   * node is as simple as:
   *
   * \code
   * m_node->accept( setter );
   * \endcode
   *
   * @{
   */

  class SceneNode;
  class Solid;
  class Camera;
  class Light;

  /**
   * SceneNodeVisitor class
   *
   */
  class SceneNodeVisitor
  {
  public:
    /**
     * Virtual destructor so that subclasses are destroyed properly.
     */
    virtual ~SceneNodeVisitor() {}

    /** Visit scene node.
     */
    virtual void visit( SceneNode *node ) {};

    /** Visit solid.
     */
    virtual void visit( Solid *solid ) {};

    /** Visit solid.
     */
    virtual void visit( Light *light ) {};

    /** Visit camera.
     */
    virtual void visit( Camera *camera ) {};
  };
  /** @} */

} /* namespace a3m */

#endif /* A3M_SCENENODEVISITOR_H */
