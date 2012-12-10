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
 * 2D Texture class
 *
 */
#pragma once
#ifndef A3M_TEXTURE2D_H
#define A3M_TEXTURE2D_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <cstring>                            /* strncpy         */
#include <a3m/assetcache.h>                   /* for AssetCache */
#include <a3m/detail/textureresource.h>       /* for TextureResource */
#include <a3m/base_types.h>                   /* A3M_CHAR8 etc.  */
#include <a3m/pointer.h>                      /* for SharedPtr   */
#include <a3m/texture.h>                      /* for NonCopyable */
#include <a3m/detail/resourcecache.h>                /* for ResourceCache */

namespace a3m
{
  /** \defgroup a3mTexture2D 2D Texture
   * \ingroup  a3mTexture
   *
   * Texture2D encapsulates a 2D texture object.
   * Texture2D objects are assets and are therefore created and managed by a
   * Texture2DCache.
   *
   * \note
   * Generally, the client will want to use AssetCachePool to create and manage
   * asset caches, rather than creating them manually.
   *
   * Example:
   * \code
   * // Create a texture cache and add a path from which to load textures
   * Texture2DCache::Ptr cache( new Texture2DCache() );
   * registerSource( *cache, ".\\assets\\textures");
   *
   * // Register a loader for the cache to use
   * cache->registerLoader( myLoader );
   *
   * // Load Texture2D from ".\assets\textures"
   * Texture2D::Ptr texture2d = cache->get( "mandel.jpg" );
   * \endcode
   *  @{
   */

  // Forward declaration
  class Texture2DCache;

  /** Texture2D class.
   */
  class Texture2D : public Texture
  {
  public:
    A3M_NAME_SHARED_CLASS( Texture2D )
    /** Smart pointer type for this class */
    typedef SharedPtr< Texture2D > Ptr;

    /** Cache type for this class */
    typedef Texture2DCache CacheType;

    /** Sets repeat mode in horizontal direction.
     */
    void setHorizontalWrap( TextureParameters::WrapMode mode /**< Wrap mode */);

    /** Checks repeat mode in horizontal direction.
     * \return Wrap mode
     */
    TextureParameters::WrapMode getHorizontalWrap() const;

    /** Sets repeat mode in vertical direction.
     */
    void setVerticalWrap( TextureParameters::WrapMode mode /**< Wrap mode */);

    /** Checks repeat mode in vertical direction.
     * \return Wrap mode
     */
    TextureParameters::WrapMode getVerticalWrap() const;

    /** Enable texture object.
     * Makes this the current texture on the active texture unit.
     */
    virtual void enable();

    /** Get width of texture
     * \return width in pixels of this texture
     */
    A3M_UINT32 getWidth() const { return m_width; }

    /** Get height of texture
     * \return height in pixels of this texture
     */
    A3M_UINT32 getHeight() const { return m_height; }

    /** Sets filter mode for minification.
     */
    void setMinFilter( TextureParameters::FilterMode  /**< Filter mode */ );

    /** Gets filter mode for minification.
     * \return filter mode for minification.
     */
    TextureParameters::FilterMode getMinFilter() const;

    /** Sets filter mode for magnification.
     */
    void setMagFilter( TextureParameters::FilterMode /**< Filter mode */ );

    /** Gets filter mode for magnification.
     * \return filter mode for magnification.
     */
    TextureParameters::FilterMode getMagFilter() const;

    /** Equals operator
     * \return TRUE if this == rhs
     */
    A3M_BOOL operator==( Texture2D const& rhs /**< right-hand operand */ ) const;

    /** Not-equal operator
     * \return TRUE if this != rhs
     */
    A3M_BOOL operator!=( Texture2D const& rhs /**< right-hand operand */ ) const;

    /** Get size of texture in bytes
     *
     * \return Texture size in bytes
     */
    A3M_INT32 getSizeInBytes() { return m_sizeInBytes; }

    /** Get the OpenGL ID (integer) for the texture.
     * Making this information 'public' goes against the usual rules of data
     * hiding (ie the presence of OpenGL ideally should be hidden from the app
     * layers), however, it is required when configuring Android to provide
     * the raw live data for a 'live' texture such as from the camera or a
     * video playback.
     *
     * \return OpenGL texture ID (aka 'name' but it is an integer)
     */
    A3M_UINT32 getGlTexId()
    {
      return m_resource->getId();
    }


  private:
    friend class RenderTarget; /* Is a friend so that it can access m_resource */
    friend class Texture2DCache; /* Is Texture2D's factory class */

    /**
     * Private constructor.
     * This constructor is called by Texture2DCache.
     */
    Texture2D(
        A3M_UINT32 width, /**< Width of the texture in pixels */
        A3M_UINT32 height, /**< Height of the texture in pixels */
        A3M_FLOAT bytesPerPixel, /**< Average number of bytes per pixel */
        A3M_BOOL hasMipMaps, /**< A3M_TRUE = texture has generated mipmaps */
        detail::TextureResource::Ptr const& resource, /**< Texture resource */
        A3M_BOOL isExternal = A3M_FALSE /**< Is the texture real or external */);

    detail::TextureResource::Ptr m_resource; /**< OpenGL texture resource */
    A3M_UINT32 m_width; /**< Width of the texture in pixels */
    A3M_UINT32 m_height /**< Height of the texture in pixels */;
    A3M_BOOL m_hasMipMaps; /**< A3M_TRUE if the texture has generated mipmaps */
    A3M_INT32 m_sizeInBytes; /**< Size of the texture in bytes */
    A3M_BOOL m_isExternal; /**< True when pointing to an external texture */
  };

  /**
   * AssetCache specialised for storing and creating Texture2D assets.
   */
  class Texture2DCache : public AssetCache<Texture2D>
  {
  public:
    /** Smart pointer type for this class */
    typedef SharedPtr< Texture2DCache > Ptr;

    /**
     * Constructs a Texture2D from a block of data.
     * \return New texture, or null if a texture with the given name existed
     */
    Texture2D::Ptr create(
        A3M_UINT32 width, /**< Width of the texture in pixels */
        A3M_UINT32 height, /**< Height of the texture in pixels */
        Texture::Format format, /**< Channel format (e.g. RGBA) */
        Texture::Type type, /**< Channel type (e.g. UNSIGNED_BYTE) */
        void const* pixels, /**< Data */
        A3M_CHAR8 const* name = 0 /**< Optional name to give the texture. If
                                    omitted, the asset will not be reachable
                                    via the AssetCache::get() function. */);

    /**
     * Constructs a Texture2D from an existing OpenGL texture ID.
     * Call this factory function if you have an existing OpenGL resource ID
     * which you want A3M to take ownership of.
     * \return New texture, or null if a texture with the given name existed
     */
    Texture2D::Ptr create(
        A3M_UINT32 width, /**< Width of the texture in pixels */
        A3M_UINT32 height, /**< Height of the texture in pixels */
        A3M_FLOAT bytesPerPixel, /**< Average bytes per pixel */
        A3M_BOOL hasMipMaps, /**< A3M_TRUE = texture has generated mipmaps */
        A3M_UINT32 glId, /**< OpenGL texture ID */
        A3M_CHAR8 const* name = 0 /**< Optional name to give the texture. If
                                    omitted, the asset will not be reachable
                                    via the AssetCache::get() function. */);

    /**
     * Constructs a Texture2D from the contents of the backbuffer.
     * \return New texture, or null if a texture with the given name existed
     */
    Texture2D::Ptr createFromBackbuffer(
        Texture::Format format,
        /**< Channel format (e.g. RGBA); DEPTH is not permitted. */
        A3M_CHAR8 const* name = 0
        /**< Optional name to give the texture. If omitted, the asset will not
         * be reachable via the AssetCache::get() function. */);

    /**
     * Constructs a non-existent (ie zero size, as opposed to 'empty' ie blank)
     * Texture2D for use as a video texture.
     * \return New apparent texture
     */
    Texture2D::Ptr createForExternalSource( void );
  };

  /**
   * Returns total texture memory consumed by all assets in a cache.
   * \return Texture size in Bytes
   */
  A3M_UINT32 getTotalAssetSizeInBytes(
      Texture2DCache const& cache /**< Cache to check */);

/******************************************************************************
 * Implementation
 ******************************************************************************/

  /*
   * Equals operator
   */
  inline A3M_BOOL Texture2D::operator==( Texture2D const& rhs ) const
  {
    return m_resource->getId() == rhs.m_resource->getId();
  }

  /*
   * Not-equal operator
   */
  inline A3M_BOOL Texture2D::operator!=( Texture2D const& rhs ) const
  {
    return !operator==( rhs );
  }

  /** @} */

} /* namespace a3m */

#endif /* A3M_TEXTURE2D_H */
