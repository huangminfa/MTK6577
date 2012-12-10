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
 * TextureCube class
 *
 */
#pragma once
#ifndef A3M_TEXTURECUBE_H
#define A3M_TEXTURECUBE_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/detail/textureresource.h> /* for TextureResource */
#include <a3m/assetcache.h> /* A3M base type defines */
#include <a3m/base_types.h> /* A3M base type defines */
#include <a3m/pointer.h> /* for SharedPtr */
#include <a3m/detail/resourcecache.h> /* for ResourceCache */
#include <a3m/texture.h> /* for NonCopyable */

namespace a3m
{
  /** \defgroup a3mTextureCube Texture Cube
   * \ingroup  a3mTexture
   *
   * TextureCube encapsulates a cube map texture object.
   * TextureCube objects are assets and are therefore created and managed by a
   * Texture2DCache.
   *
   * \note
   * Generally, the client will want to use AssetCachePool to create and manage
   * asset caches, rather than creating them manually.
   *
   * Example:
   * \code
   * // Create a texture cache and add a path from which to load textures
   * TextureCubeCache::Ptr cache( new TextureCubeCache() );
   * registerSource( *cache, ".\\assets\\textures");
   *
   * // Register a loader for the cache to use
   * cache->registerLoader( myLoader );
   *
   * // Load TextureCube from ".\assets\textures"
   * TextureCube::Ptr textureCube = get(*cache,
   *     "posx.jpg", "negx.jpg",
   *     "posy.jpg", "negy.jpg",
   *     "posz.jpg", "negz.jpg" );
   * \endcode
   *  @{
   */

  // Forward declarations
  class TextureCubeCache;

  /** TextureCube class.
   */
  class TextureCube : public Texture
  {
  public:
    A3M_NAME_SHARED_CLASS( TextureCube )
    /** Smart pointer type for this class */
    typedef SharedPtr< TextureCube > Ptr;

    /** Cache type for this class */
    typedef TextureCubeCache CacheType;

    /** Face enumeration
     * Specifies which face is being initialized in setFace()
     */
    enum Face
    {
      POSITIVE_X,  /**< positive x direction */
      NEGATIVE_X,  /**< negative x direction */
      POSITIVE_Y,  /**< positive y direction */
      NEGATIVE_Y,  /**< negative y direction */
      POSITIVE_Z,  /**< positive z direction */
      NEGATIVE_Z   /**< negative z direction */
    };

    /** Set pixel data for a face.
     * Sets the data for one face of the cubemap. The data pointed to by pixels
     * must match the parameters used to construct the TextureCube object.
     */
    void setFace( Face face,
                  /**< [in] specifies face to be initialized */
                  const void *pixels
                  /**< [in] pixel data to initialize face with */ );

    /** Enable texture object.
     * Makes this the current texture on the active texture unit.
     */
    virtual void enable();

    /** Equals operator
     * \return TRUE if this == rhs
     */
    A3M_BOOL operator==(
        TextureCube const& rhs /**< right-hand operand */ ) const;

    /** Not-equal operator
     * \return TRUE if this != rhs
     */
    A3M_BOOL operator!=(
        TextureCube const& rhs /**< right-hand operand */ ) const;

  private:
    friend class RenderTarget; /* Is a friend so that it can access m_resource */
    friend class TextureCubeCache; /* Is TextureCube's factory class */

    /**
     * Private constructor.
     * This constructor is called by TextureCubeCache.
     */
    TextureCube(
        A3M_UINT32 width, /**< Width of the texture in pixels */
        A3M_UINT32 height, /**< Height of the texture in pixels */
        Texture::Format format, /**< Channel format (e.g. RGBA) */
        Texture::Type type, /**< Channel type (e.g. UNSIGNED_BYTE) */
        detail::TextureResource::Ptr const& resource /**< Texture resource */);

    detail::TextureResource::Ptr m_resource; /**< OpenGL texture resource */
    A3M_UINT32 m_width;             /**< width of texture in pixels */
    A3M_UINT32 m_height;            /**< height of texture in pixels */
    Format m_format;                /**< format of texture */
    Type m_type;                    /**< type of data in pixels parameter */
    TextureParameters m_parameters; /**< filter and wrapping modes */
    A3M_UINT32 m_nFacesSet;         /**< number of faces set so far */
  };

  /**
   * AssetCache specialised for storing and creating TextureCube assets.
   */
  class TextureCubeCache : public AssetCache<TextureCube>
  {
  public:
    /** Smart pointer type for this class */
    typedef SharedPtr< TextureCubeCache > Ptr;

    /**
     * Constructs a TextureCube with uninitialised pixel data.
     * Call TextureCube::setFace() to set the pixel data for each face.
     * \return New texture, or null if a texture with the given name existed
     */
    TextureCube::Ptr create(
        A3M_UINT32 width, /**< Width of each face in pixels */
        A3M_UINT32 height, /**< Height of each face in pixels */
        Texture::Format format, /**< Channel format (e.g. RGBA) */
        Texture::Type type, /**< Channel type (e.g. UNSIGNED_BYTE) */
        A3M_CHAR8 const* name = 0 /**< Optional name to give the texture. If
                                    omitted, the asset will not be reachable
                                    via the AssetCache::get() function. */);
  };

  /**
   * Loads or retrieves a TextureCube from the cache, using individual textures
   * for each face.
   * \copydetails AssetCache::get()
   */
  TextureCube::Ptr get(
      TextureCubeCache& cache, /**< Cache to use to retrieve the texture*/
      A3M_CHAR8 const* positiveX, /**< Name of positive-X-facing texture */
      A3M_CHAR8 const* negativeX, /**< Name of negative-X-facing texture */
      A3M_CHAR8 const* positiveY, /**< Name of positive-Y-facing texture */
      A3M_CHAR8 const* negativeY, /**< Name of negative-Y-facing texture */
      A3M_CHAR8 const* positiveZ, /**< Name of positive-Z-facing texture */
      A3M_CHAR8 const* negativeZ /**< Name of negative-Z-facing texture */);

  /**
   * Reports whether a particular asset exists (is loaded) in the cache, using
   * individual textures for each face.
   * \copydetails AssetCache::exists()
   */
  A3M_BOOL exists(
      TextureCubeCache& cache, /**< Cache in which to check */
      A3M_CHAR8 const* positiveX, /**< Name of positive-X-facing texture */
      A3M_CHAR8 const* negativeX, /**< Name of negative-X-facing texture */
      A3M_CHAR8 const* positiveY, /**< Name of positive-Y-facing texture */
      A3M_CHAR8 const* negativeY, /**< Name of negative-Y-facing texture */
      A3M_CHAR8 const* positiveZ, /**< Name of positive-Z-facing texture */
      A3M_CHAR8 const* negativeZ /**< Name of negative-Z-facing texture */);

/******************************************************************************
 * Implementation
 ******************************************************************************/

  /*
   * Equals operator
   */
  inline A3M_BOOL TextureCube::operator==( TextureCube const& rhs ) const
  {
    return m_resource->getId() == rhs.m_resource->getId();
  }

  /*
   * Not-equal operator
   */
  inline A3M_BOOL TextureCube::operator!=( TextureCube const& rhs ) const
  {
    return !operator==( rhs );
  }

  /** @} */

} /* namespace a3m */

#endif /* A3M_TEXTURECUBE_H */
