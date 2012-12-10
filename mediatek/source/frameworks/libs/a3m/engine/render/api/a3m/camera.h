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
 * Camera class
 */
#pragma once
#ifndef A3M_CAMERA_H
#define A3M_CAMERA_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/angle.h>           /* for Anglef                              */
#include <a3m/scenenode.h>       /* SceneNode::Ptr                          */

/*****************************************************************************
 * A3M Namespace
 *****************************************************************************/
namespace a3m
{
  /** \defgroup a3mCamera A3M Camera
   * \ingroup  a3mRefRender
   *
   * Camera class stores a Camera object parameters and it is derived from
   * Entity. It contains frustum parameter setters and getters as well as
   * public functions for retrieving the camera transformation and projection.
   *
   * @{
   */

  /**
   * Camera class
   *
   */
  class Camera : public SceneNode
  {
  public:
    A3M_NAME_SHARED_CLASS( Camera )

    /** Smart pointer type for this class */
    typedef SharedPtr < Camera > Ptr;

    /** Type ID for this class */
    static A3M_UINT32 const NODE_TYPE;

    /**
     * Type of projection a Camera uses.
     */
    enum ProjectionType
    {
      ORTHOGRAPHIC,
      /**< A type of parallel projection, where all projection lines are
       * perpendicular to the projection plane.  This means that rendered
       * objects will always appear the same size regardless of their distance
       * from the camera.  Axonometric projections, such as isometric
       * projections, are a subset of orthographic projections, and can be
       * produced by rotating the camera. */
      PERSPECTIVE
      /**< A projection which emulates perspective, as seen by the human eye,
       * by drawing objects smaller the further from the camera they become. */
    };

    /*
     * Constructor
     *
     * Initializes Camera parameters
     */
    Camera();

    /**
     * Virtual destructor in case derived classes are destroyed through
     * pointers to this class.
     */
    virtual ~Camera();

    // Override
    virtual A3M_UINT32 getType() const { return NODE_TYPE; }

    /**
     * Sets the type of projection used by the camera.
     */
    void setProjectionType(ProjectionType type /**< Projection type */)
    {
      m_projectionType = type;
    }

    /**
     * Returns the type of projection used by the camera.
     * \return Projection type
     */
    ProjectionType getProjectionType() const
    {
      return m_projectionType;
    }

    /**
     * Gets current field-of-view angle when using a PERSPECTIVE projection, in
     * the x or y direction (whichever is smaller).
     * \return fov.
     */
    Anglef const& getFov() const {return m_fov;}

    /**
     * Sets field-of-view angle when using a PERSPECTIVE projection. The field
     * of view (FOV) is the angle subtended at the camera of the world scene
     * visible across the \e narrower screen axis, i.e. the full width of the
     * screen in a portrait orientation, or the full height in landscape
     * format.  This ensures that all the objects in the smallest view fustrum
     * implied by the FOV angle are always visible on the display area.
     *
     * When using an ORTHOGRAPHIC projection, this parameter has no visible
     * effect, and setting the FOV while in ORTHOGRAPHIC mode will raise a
     * warning (though the value will still be used when in PERSPECTIVE mode).
     */
    void setFov (Anglef const& fov /**< fov */)
    {
      if (m_projectionType == ORTHOGRAPHIC)
      {
        A3M_LOG_WARN("Setting FOV while using an ORTHOGRAPHIC projection will "
            "have no visible effect until a PERSPECTIVE projection is used.");
      }

      m_fov = fov;
    }

    /**
     * Sets the width of the viewing frustum in world-units when using an
     * ORTHOGRAPHIC projection.  When using an orthographic projection, objects
     * will always be drawn the same size on-screen as they are in the world
     * (assuming no scaling has been applied to the camera).  The frustum width
     * determines the scale of the screen coordinates (i.e how many world units
     * correspond to an entire width of the rendering viewport in pixels).
     * Often, a per-pixel (one world-space unit corresponds to one pixel)
     * orthographic projection is desired, in which case the width should be
     * equal to the width of the rendering viewport in pixels (e.g. 640; if
     * the viewport size changes, this value should be updated).  The default
     * frustum width is 2 world units (giving a range of -1 to 1 when the
     * camera has no additional transformations applied).
     *
     * When using an PERSPECTIVE projection, this parameter has no visible
     * effect, and setting the width while in PERSPECTIVE mode will raise a
     * warning (though the value will still be used when in ORTHOGRAPHIC mode).
     */
    void setWidth(A3M_FLOAT width /**< Width of viewing frustum */)
    {
      if (m_projectionType == PERSPECTIVE)
      {
        A3M_LOG_WARN("Setting width while using an PERSPECTIVE projection will "
            "have no visible effect until a ORTHOGRAPHIC projection is used.");
      }

      m_width = width;
    }

    /**
     * Returns the width of the viewing frustum when using an ORTHOGRAPHIC
     * projection.
     * \return Frustum width in world units
     */
    A3M_FLOAT getWidth() const
    {
      return m_width;
    }

    /**
     * Gets the distance from the camera to the near clipping plane (always
     * positive).
     * \return near clipping plane.
     */
    A3M_FLOAT getNear() const {return m_zNear;}

    /**
     * Sets near clipping plane.
     */
    void setNear (A3M_FLOAT zNear /**< frustum zNear */ )
    {
      m_zNear = zNear;
    }

    /**
     * Gets the distance from the camera to the far clipping plane (always
     * positive).
     * \return far clipping plane.
     */
    A3M_FLOAT getFar() const {return m_zFar;}

    /**
     * Sets far clipping plane.
     */
    void setFar (A3M_FLOAT zFar /**< frustum zFar */)
    {
      m_zFar = zFar;
    }

    /**
     * The process of using a pair of camera positions to generate a
     * stereoscopic view (on suitable hardware) requires a change to the
     * camera projection matrix.
     *
     * The factors feeding in to this include the focal distance (the
     * distance between camera and the camera-Z plane that (stereoscopically)
     * relates to the physical screen, the field of view and the separation
     * of the 'eyes'.
     *
     * When using an ORTHOGRAPHIC projection, these parameters have no visible
     * effect, and setting them while in ORTHOGRAPHIC mode will raise a warning
     * (though the values will still be used when in PERSPECTIVE mode).
     */
    void setStereo(
      A3M_FLOAT zFocal /**< distance camera to focal plane in world units */,
      A3M_FLOAT eyeSep /**< distance between camera positions in world units */
      )
    {
      if (m_projectionType == ORTHOGRAPHIC)
      {
        A3M_LOG_WARN("Setting stereo parameters while using an ORTHOGRAPHIC "
            "projection will have no visible effect until a PERSPECTIVE "
            "projection is used.");
      }

      m_stereoZFocal = zFocal;
      m_stereoEyeSep = eyeSep;
    }

    /**
     * Gets whether the camera is stereo or mono. Uses the fact that a zero
     * eye separation is meaningless for a stereo camera - which ensures we
     * don't get an on/off flag and settings out of sync.
     *
     * \return TRUE if the camera is stereo
     */
    A3M_BOOL isStereo()
    {
      return (m_stereoEyeSep != 0.0f);
    }

    /** Stereo version of getWorldTransform. Provides the 4x4 matrices
    * for the left and right eye camera positions and orientations.
    */
    void getStereoWorldTransform(
      Matrix4f *leftEye, /**< [out] LH camera position and orientation matrix */
      Matrix4f *rightEye /**< [out] RH camera */) ;

    /** The projection function is same as gluPerspective, except it takes the
     * smaller aspect ratio value from either in x or y direction.
     * It specifies a view frustum into the world coordinate system.  The
     * matrix projected is multipled by the current matrix. The aspect ratio is
     * set in application.  There is no set function available in Camera class.
     */
    void getProjection(
      Matrix4f *projection, /**< [out] projection matrix */
      A3M_FLOAT aspect      /**< frustum aspect ratio */) const;

    /** Stereo version of getProjection
     */
    void getStereoProjection(
        Matrix4f *left    /**< [out] LH camera projection matrix */,
        Matrix4f *right   /**< [out] RH camera projection matrix */,
        A3M_FLOAT aspect  /**< frustum aspect ratio */) const;


    /** Accept a scene node visitor.
     */
    virtual void accept( SceneNodeVisitor &visitor /**< visitor */ );

  private:
    /* Type of projection returned by getProjection() */
    ProjectionType m_projectionType;

    /* Frustum field of view when in PERSPECTIVE mode. */
    Anglef m_fov;

    /* Frustum width when in ORTHOGRAPHIC mode (when the frustum is cuboid).
     * The width is the dimension corresponding to the local x-axis of the
     * camera. */
    A3M_FLOAT m_width;

    /* Frustum zNear in world units */
    A3M_FLOAT m_zNear;

    /* Frustum zFar in world units */
    A3M_FLOAT m_zFar;

    /* Items relating to stereoscopic projections */
    /* Z distance from camera to screen focal plane in world */
    A3M_FLOAT m_stereoZFocal;

    /* Camera-X distance between stereo camera positions */
    A3M_FLOAT m_stereoEyeSep;
  };

  template < typename T > struct ConvexHull;

  /// Concrete instantiation for float type.
  typedef ConvexHull<A3M_FLOAT> ConvexHullf;

  /** Build a convex hull representing the camera frustum. */
  extern void buildFrustum(
    ConvexHullf& frustum  /**< [out] upon return contains the resulting
                            world-space frustum */,
    Camera const& camera  /**< any camera */,
    A3M_FLOAT aspect      /**< the viewport aspect ratio (width / height) */);

  /** Build a ray from the camera position passing through
      the given viewport coordinate.
      */
  extern void rayThruViewportPosition(
    A3M_INT32 x
      /**< X coordinate of a point on the viewport */,
    A3M_INT32 y
      /**< Y coordinate of a point on the viewport */,
    A3M_INT32 width
      /**< Viewport width */,
    A3M_INT32 height
      /**< Viewport height */,
    a3m::Camera::Ptr camera
      /**< The currently active camera */,
    a3m::Vector3f& rayOrigin
      /**< [out] Upon return contains the ray start position */,
    a3m::Vector3f& rayDirection
      /**< [out] Upon return contains the ray unit direction vector */);

  /** @} */

} /* namespace a3m */

#endif /* A3M_CAMERA_H */
