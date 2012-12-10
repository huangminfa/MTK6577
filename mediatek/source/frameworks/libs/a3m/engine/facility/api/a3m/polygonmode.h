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
 * PolygonMode class
 *
 */
#pragma once
#ifndef A3M_POLYGON_MODE_H
#define A3M_POLYGON_MODE_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/pointer.h>    /* for Shared Ptr */

namespace a3m
{
  /** \defgroup a3mPolygonMode Polygon Mode (Winding order, etc.)
   * \ingroup  a3mRefScene
   * Wrapper for vertex winding order (for back face culling) and line width
   * @{
   */

  /** Polygon Mode class:
   * Facilities for setting and retrieve OpenGL's polygon back-face culling,
   * winding order and line width.
   * Culling determines which side of a polygon is removed from processing
   * prior to rasterization.
   * Winding specifies which side of a polygon is the front face, either
   * clockwise (CW) or counter-clockwise (CCW).
   */

  class PolygonMode
  {
  public:
    /** Default constructor */
    PolygonMode();

    /**
     * Describes the mode of culling in enum.
     */
    enum Culling
    {
      CULL_BACK,           /**< The back-facing side of a polygon is not to be
                                drawn */
      CULL_FRONT,          /**< The front-facing side of a polygon is not to be
                                drawn */
      CULL_FRONT_AND_BACK, /**< Both front and back facing polygons are not to
                                be drawn. This is not the mode usually set. It
                                is here to allow an application to access
                                OpenGL ES 2.0 corresponding function which in
                                essence renders nothing. */
      CULL_NONE            /**< Both faces of a polygon are to be draw. This is
                                not the mode usually set. This disables
                                culling. */
    };

    /**
    * Describes the type of winding in enum.
    */
    enum Winding
    {
      WINDING_CCW,         /**< Front-facing is a polygon having its vertices
                                in counter-clockwise order in screen space. */
      WINDING_CW           /**< Front-facing is a polygon having its vertices
                                in clockwise order in screen space. */
    };

    /** Retrieves the current polygon culling mode.
     * \return The current culling mode.
     */
    Culling getCulling() const {return m_culling;}

    /** Retrieves the current polygon winding type.
     * \return The current winding type.
     */
    Winding getWinding() const {return m_winding;}

    /** Retrieves the current line width.
     * \return The currently set line width.
     */
    A3M_FLOAT getLineWidth() const {return m_lineWidth;}

    /** Set the polygon culling mode. The culling mode defines which sides of
     * a polygon are culled (that is, not rendered).
     * \return None.
     */
    void setCulling(Culling culling /**< the culling mode to set */)
    {
      m_culling = culling;
    }

    /** Set the polygon winding type to clockwise or count-clockwise. The
     * winding type defines which side of a polygon is considered to be the
     * front.
     * \return None.
     */
    void setWinding(Winding winding /**< The winding type to set */)
    {
      m_winding = winding;
    }

    /** Set the polygon winding type to clockwise or count-clockwise. The
     * winding type defines which side of a polygon is considered to be the
     * front.
     * \return None.
     */
    void setLineWidth(A3M_FLOAT lineWidth /**< the currently set line width */)
    {
      m_lineWidth = lineWidth;
    }

    /** Enable Polygon Mode.
     * Enable Polygon Mode with set polygon parameters (i.e. culling mode,
     * winding type and line width.
     * \return None.
     */
    void enable() const;

    /** Enable Polygon Mode.
     * Enable Polygon Mode with set polygon parameters (i.e. culling mode,
     * winding type and line width.
     * \return None.
     */
    void enable( PolygonMode &other /**< [in] PolygonMode representing the
                                         current state */ ) const;

  private:
    /** Polygon mode default value */
    static const A3M_FLOAT DEFAULT_LINE_WIDTH;

    Culling m_culling;     /**< culling mode setting */
    Winding m_winding;     /**< winding type setting */
    A3M_FLOAT m_lineWidth; /**< line width setting   */
  };

  /** @} */

} /* namespace a3m */

#endif /* A3M_POLYGON_MODE_H */
