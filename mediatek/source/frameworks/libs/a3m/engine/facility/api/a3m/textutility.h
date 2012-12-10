/*****************************************************************************
 *
 * Copyright (c) 2011 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 *****************************************************************************/
/** \file
 * Text class
 *
 */
#pragma once
#ifndef A3M_TEXTUTILTY_H
#define A3M_TEXTUTILTY_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/appearance.h> /* Appearance */
#include <a3m/font.h> /* Font */
#include <a3m/mesh.h> /* Mesh */

namespace a3m
{
  /** \defgroup a3mTextUtility TextUtility
   * \ingroup  a3mRefText
   *
   * Utility functions for displaying text.
   *
   * Draw into a texture using the textDrawToTexture() function. Note that
   * you should supply a negative height to this function as it uses a
   * left-handed coordinate system.
   *
   * \code
   * // Create a texture to draw some text into.
   * a3m::Texture2D::Ptr texture = textureCache->create(
   *     1024, 64, a3m::Texture::RGBA, a3m::Texture::UNSIGNED_BYTE, 0 );
   *
   * textDrawToTexture( *meshCache, texture, *font, -64.0f, *appearance,
   *                    "Text message", 0,
   *                    a3m::TextAlign::HORIZONTAL_LEFT,
   *                    a3m::TextAlign::VERTICAL_TOP );
   *
   * // Now use the texture in the appearance for some mesh object.
   * \endcode
   *
   * Create a mesh representing a string and textured using a given font
   * with textCreateMesh(). Note that you should set a shader uniform in the
   * appearance you use to draw the mesh with the font's texture.
   *
   * \code
   * a3m::Mesh::Ptr textMesh = textCreateMesh( meshCache, *m_font, m_height,
   *                                           string, m_wrapWidth,
   *                                           m_alignHorizontal,
   *                                           m_alignVertical );
   * \endcode
   *  @{
   */

  namespace TextAlign
  {
    /** Horizontal aligment for text.
     */
    enum Horizontal
    {
      HORIZONTAL_LEFT,    /**< align to left of text (default) */
      HORIZONTAL_CENTRE,  /**< align to centre of text */
      HORIZONTAL_RIGHT    /**< align to right of text */
    };

    /** Vertical aligment for text.
     */
    enum Vertical
    {
      VERTICAL_TOP,           /**< align to top of text */
      VERTICAL_ASCENTMIDDLE,  /**< align to middle of font acsent */
      VERTICAL_MIDDLE,        /**< align to middle of character */
      VERTICAL_BASELINE,      /**< align with baseline of character*/
      VERTICAL_BOTTOM         /**< align to bottom of character */
    };
  }

  /** Create mesh.
   * \return mesh instance containing a collection of quads.
   */
  Mesh::Ptr textCreateMesh(
    MeshCache& meshCache, /**< Cache used to create the mesh */
    Font const &font, /**< Font to use */
    A3M_FLOAT height, /**< Character height */
    A3M_CHAR8 const *string, /**< String to represent */
    A3M_FLOAT wrapWidth = 0, /**< Maximum width of line or 0 for no text
                               wrapping */
    TextAlign::Horizontal alignHorizontal = TextAlign::HORIZONTAL_LEFT,
      /**< horizontal text alignment */
    TextAlign::Vertical alignVertical = TextAlign::VERTICAL_BASELINE
      /**< vertical text alignment */ );

  /** Draw text.
  */
  void textDraw(
    MeshCache& meshCache, /**< Cache used to create the mesh */
    Font const &font, /**< Font to use */
    A3M_FLOAT height, /**< Character height */
    Appearance &appearance, /**< Appearance */
    A3M_CHAR8 const *string, /**< String to represent */
    A3M_FLOAT wrapWidth = 0, /**< Maximum width of line or 0 for no text
                               wrapping */
    TextAlign::Horizontal alignHorizontal = TextAlign::HORIZONTAL_LEFT,
      /**< horizontal text alignment */
    TextAlign::Vertical alignVertical = TextAlign::VERTICAL_BASELINE
      /**< vertical text alignment */ );

  /** Measure text.
   * The textMeasure function calulates the width and height that a text
   * string drawn in a given font will have.
   */
  void textMeasure(
    Font const &font, /**< Font to use */
    A3M_FLOAT height, /**< Character height */
    A3M_CHAR8 const *string, /**< String to represent */
    A3M_FLOAT &widthCalc, /**< [out] calculated width of text */
    A3M_FLOAT &heightCalc /**< [out] calculated height of text */ );

  /** Draw text into texture.
   */
  void textDrawToTexture(
    MeshCache& meshCache, /**< Cache used to create the mesh */
    Texture2D::Ptr const &texture, /**< Texture to draw into */
    Font const &font, /**< Font to use */
    A3M_FLOAT height, /**< Character height */
    Appearance &appearance, /**< Appearance */
    A3M_CHAR8 const *string, /**< String to represent */
    A3M_FLOAT wrapWidth = 0, /**< Maximum width of line or 0 for no text
                               wrapping */
    TextAlign::Horizontal alignHorizontal = TextAlign::HORIZONTAL_LEFT,
      /**< horizontal text alignment */
    TextAlign::Vertical alignVertical = TextAlign::VERTICAL_BASELINE
      /**< vertical text alignment */ );

  /** @} */

} /* end of namespace */

#endif /* A3M_TEXTUTILTY_H */
