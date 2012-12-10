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
 * AssetCachePool API
 *
 */
#pragma once
#ifndef A3M_ASSETCACHEPOOL_H
#define A3M_ASSETCACHEPOOL_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/font.h> /* for FontCache */
#include <a3m/indexbuffer.h> /* for IndexBufferCache */
#include <a3m/mesh.h> /* for MeshCache */
#include <a3m/noncopyable.h> /* for NonCopyable */
#include <a3m/pointer.h> /* for Shared and SharedPtr */
#include <a3m/shaderprogram.h> /* for ShaderProgramCache */
#include <a3m/texture2d.h> /* for Texture2DCache */
#include <a3m/texturecube.h> /* for TextureCubeCache */
#include <a3m/vertexbuffer.h> /* for VertexBufferCache */

namespace a3m
{
  /** \defgroup a3mFacAssetCachePool AssetCachePool
   * \ingroup  a3mRefAssets
   *
   * AssetCachePool is a convenience class for creating and managing all the
   * different types of AssetCache.
   * To save the user from having to create the different types of AssetCache
   * separately, register custom loaders for each type of asset, and manually
   * manage each cache, the AssetCachePool creates one of each type of
   * AssetCache, registers one or more default loaders where applicable, and
   * provides convenience functions to operate of all the caches
   * simultaneously.  It also provides a function for acquiring data streams
   * for direct use.
   *
   * @{
   */

  /**
   * AssetCachePool class.
   * Collection of AssetCache object for different asset types in the system.
   */
  class AssetCachePool : public Shared, NonCopyable
  {
  public:
    A3M_NAME_SHARED_CLASS( AssetCachePool )

    /** Smart pointer type for this class. */
    typedef SharedPtr<AssetCachePool> Ptr;

    /**
     * Constructor.
     */
    AssetCachePool();

    /**
     * \copybrief AssetCache::registerSource()
     * \copydetails AssetCache::registerSource()
     */
    void registerSource(
        StreamSource::Ptr const& source /**< StreamSource to register */);

    /**
     * \copybrief AssetCache::setCacheSource()
     * \copydetails AssetCache::setCacheSource()
     */
    void setCacheSource(
        StreamSource::Ptr const& source /**< StreamSource to register */);

    /**
     * \copybrief AssetCache::getStream()
     * Unlike flush() and release(), this function uses the AssetPool's own
     * registered sources to acquire a stream, rather than calling the
     * corresponding AssetCache function.
     * \return Stream, or null if the stream was not found
     */
    Stream::Ptr getStream(A3M_CHAR8 const* name /**< Name of stream */);

    /**
     * \copybrief AssetCache::getCacheStream()
     * \copydetails getStream()
     * \return Stream, or null if the stream could not be found or created
     */
    Stream::Ptr getCacheStream(
        A3M_CHAR8 const* name, /**< Name of stream */
        A3M_BOOL writable = A3M_FALSE /**< A3M_TRUE = source is writable */);

    /**
     * Removes assets which are unreferenced from outside each cache.
     * \copydetails AssetCache::flush()
     */
    void flush();

    /**
     * Releases device resources from the control of each cache.
     * \copydetails AssetCache::release()
     */
    void release();

    /**
     * Returns the FontCache.
     * \return AssetCache object
     */
    FontCache::Ptr fontCache() { return m_fontCache; }

    /**
     * Returns the IndexBufferCache.
     * \return IndexBufferCache object
     */
    IndexBufferCache::Ptr indexBufferCache() { return m_indexBufferCache; }

    /**
     * Returns the MeshCache.
     * \return MeshCache object
     */
    MeshCache::Ptr meshCache() { return m_meshCache; }

    /**
     * Returns the ShaderProgramCache.
     * \return AssetCache object
     */
    ShaderProgramCache::Ptr shaderProgramCache()
    {
      return m_shaderProgramCache;
    }

    /**
     * Returns the Texture2DCache.
     * \return AssetCache object
     */
    Texture2DCache::Ptr texture2DCache() { return m_texture2DCache; }

    /**
     * Returns the TextureCubeCache.
     * \return AssetCache object
     */
    TextureCubeCache::Ptr textureCubeCache() { return m_textureCubeCache; }

    /**
     * Returns the VertexBufferCache.
     * \return VertexBufferCache object
     */
    VertexBufferCache::Ptr vertexBufferCache()
    {
      return m_vertexBufferCache;
    }

  private:
    detail::AssetPath::Ptr m_assetPath; /**< AssetPath for acquiring streams */
    StreamSource::Ptr m_cacheSource; /**< StreamSource for caching assets */
    IndexBufferCache::Ptr m_indexBufferCache; /**< IndexBuffer cache */
    ShaderProgramCache::Ptr m_shaderProgramCache; /**< ShaderProgram cache */
    Texture2DCache::Ptr m_texture2DCache; /**< Texture2D cache */
    TextureCubeCache::Ptr m_textureCubeCache; /**< TextureCube cache */
    VertexBufferCache::Ptr m_vertexBufferCache; /**< VertexBuffer cache */
    FontCache::Ptr m_fontCache; /**< Font cache */
    MeshCache::Ptr m_meshCache; /**< Mesh cache */
  };

  /**
   * Registers a filesystem path as a source for loading assets.
   * \sa AssetCachePool::registerSource()
   */
  void registerSource(
      AssetCachePool& pool, /**< Cache pool to use */
      A3M_CHAR8 const* path, /**< Filesystem path to register as source */
      A3M_BOOL isArchive = A3M_FALSE /**< A3M_TRUE = location is an archive */);

  /**
   * Registers a filesystem path as a source for caching assets.
   * \sa AssetCachePool::setCacheSource()
   */
  void setCacheSource(
      AssetCachePool& pool, /**< Cache pool to use */
      A3M_CHAR8 const* path /**< Filesystem path to register as source */);

  /** @} */

} /* namespace a3m */

#endif /* A3M_ASSETCACHEPOOL_H */

