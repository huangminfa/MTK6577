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
 * Texture class
 *
 */
#pragma once
#ifndef A3M_TEXTURE_H
#define A3M_TEXTURE_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/base_types.h>       /* A3M base type defines      */
#include <a3m/pointer.h>          /* for SharedPtr              */
#include <a3m/noncopyable.h>      /* for NonCopyable            */

namespace a3m
{
  /** \defgroup a3mTexture Textures (base class)
   * \ingroup  a3mRefScene
   *
   * Texture is an abstract base class for the classes Texture2D and
   * TextureCube.
   *
   * Textures are Assets, which means they are managed by a corresponding
   * AssetCache in AssetPool.
   *
   * \code
   * // Set path where textures should be read from
   * AssetPath path;
   * path.add(".\\assets\\textures");
   *
   * // Instantiate the AssetPool
   * AssetPool assets;
   *
   * // Load Texture2D from ".\assets\textures"
   * Texture2D::Ptr texture = assets.texture2DLibrary.get(path, "image.bmp");
   * \endcode
   *
   *  @{
   *
   */

  /** Texture Parameters structure.
   * Specifies filtering and wrapping modes for Texture objects.
   */
  struct TextureParameters
  {
    /** Filter mode.
     * Specifies filtering used for texture.
     */
    enum FilterMode
    {
      NEAREST,                  /**< Choose nearest pixel in texture */
      LINEAR,                   /**< Interpolate between neighboring pixels */
      NEAREST_MIPMAP_NEAREST,   /**< Choose nearest pixel from nearest mipmap */
      NEAREST_MIPMAP_LINEAR,    /**< Choose nearest pixel from interpolation
                                     between two mipmaps */
      LINEAR_MIPMAP_NEAREST,    /**< Interpolate between neighboring pixels in
                                     nearest mipmap */
      LINEAR_MIPMAP_LINEAR      /**< Interpolate between neighboring pixels in
                                     interpolation between two mipmaps */
    };

    /** Wrapping type.
     * Specifies wrapping mode for texture.
     */
    enum WrapMode
    {
      REPEAT,      /**< Repeat the texture */
      CLAMP,       /**< Clamp to edge of texture */
      MIRROR       /**< Repeat the texture and mirror */
    };

    /** Constructor.
     */
    TextureParameters( FilterMode magFilter = LINEAR,
                       /**< Filter used when magnifying texture */
                       FilterMode minFilter = LINEAR_MIPMAP_NEAREST,
                       /**< Filter used when minifying texture */
                       WrapMode horizontalWrap = REPEAT,
                       /**< Repeat mode in horizontal direction */
                       WrapMode verticalWrap = REPEAT
                       /**< Repeat mode in vertical direction */ );

    FilterMode magFilter;    /**< Filter used when magnifying texture */
    FilterMode minFilter;    /**< Filter used when minifying texture */
    WrapMode horizontalWrap; /**< Repeat mode in horizontal direction */
    WrapMode verticalWrap;   /**< Repeat mode in vertical direction */
  };

  /** Texture class.
   * Abstract base class used as base for Texture2D and TextureCube.
   */
  class Texture : public Shared, NonCopyable
  {
  public:
    A3M_NAME_SHARED_CLASS( Texture )

    /** Smart pointer type for this class */
    typedef SharedPtr< Texture > Ptr;

    /** Virtual destructor in case derived classes are destroyed through
     * pointers to this class. */
    virtual ~Texture() {}

    /** Texture format.
     * specifies number of channels and their usage.
     */
    enum Format
    {
      RGBA,             /**< Red, Green, Blue, Alpha */
      RGB,              /**< Red, Green, Blue */
      LUMINANCE_ALPHA,  /**< Luminance, Alpha */
      LUMINANCE,        /**< Luminance */
      ALPHA,            /**< Alpha */
      DEPTH             /**< Depth */
    };

    /** Data type.
     * Specifies type used for texture data.
     */
    enum Type
    {
      UNSIGNED_BYTE,          /**< One byte per channel */
      UNSIGNED_SHORT,         /**< Two byte per channel (use for depth) */
      UNSIGNED_SHORT_4_4_4_4, /**< RGBA packed into 2 bytes (4 bits each) */
      UNSIGNED_SHORT_5_5_5_1, /**< RGBA packed into 2 bytes (5 bits for colour,
                                   1 for alpha) */
      UNSIGNED_SHORT_5_6_5    /**< RGB packed into 2 bytes (5 bits red,
                                   6 bits green, 5 bits blue ) */
    };

    /** Enable texture object.
     * Makes this the current texture on the active texture unit.
     */
    virtual void enable() = 0;

    /** Maximum length of a texture filename */
    static const A3M_UINT32 MAX_NAME_LENGTH = 64;
  };

  /** Equals operator for TextureParameters
   * \return TRUE if lhs == rhs
   */
  A3M_BOOL operator==( TextureParameters const& lhs, /**< left-hand operand */
                       TextureParameters const& rhs  /**< right-hand operand */ );

  /** Not-equal operator for TextureParameters
   * \return TRUE if lhs != rhs
   */
  A3M_BOOL operator!=( TextureParameters const& lhs, /**< left-hand operand */
                       TextureParameters const& rhs  /**< right-hand operand */ );

/******************************************************************************
 * Implementation
 ******************************************************************************/

  /*
   * Equals operator for TextureParameters
   */
  inline A3M_BOOL operator==( TextureParameters const& lhs,
                              TextureParameters const& rhs )
  {
    return ( lhs.magFilter      == rhs.magFilter )    &&
           ( lhs.minFilter      == rhs.minFilter )    &&
           ( lhs.verticalWrap   == rhs.verticalWrap ) &&
           ( lhs.horizontalWrap == rhs.horizontalWrap );
  }

  /*
   * Not-equal operator for TextureParameters
   */
  inline A3M_BOOL operator!=( TextureParameters const& lhs,
                              TextureParameters const& rhs )
  {
    return !( lhs == rhs );
  }

  /** @} */

} /* namespace a3m */

#endif /* A3M_TEXTURE_H */
