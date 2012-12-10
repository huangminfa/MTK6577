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
 * AssetCache class
 *
 */
#pragma once
#ifndef A3M_ASSETCACHE_H
#define A3M_ASSETCACHE_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <list> /* for std::list */
#include <map> /* for std::map */
#include <string> /* for std::string */
#include <vector> /* for std::vector */
#include <a3m/assetloader.h> /* AssetLoader */
#include <a3m/detail/assetpath.h> /* for detail::AssetPath */
#include <a3m/base_types.h> /* for A3M base types */
#include <a3m/detail/resourcecache.h> /* AssetLoader */
#include <a3m/stream.h> /* for Stream and StreamSource */

namespace a3m
{
  /** \defgroup a3mAssetcache AssetCache
   * \ingroup  a3mRefAssets
   *
   * An asset cache is a container for any type of shared asset.
   * An asset is any object which consumes lots of memory (therefore is
   * expensive to load), yet can be shared by multiple clients.
   *
   * When a client requests an asset from the AssetCache, it first checks if
   * the asset already exists in its database, in which case a reference to
   * the existing asset is returned.  Otherwise, a new asset is created  and
   * placed in the database.
   *
   * Clients should instantiate the subclasses of AssetCache such as
   * Texture2DCache and ShaderProgramCache, which may also implement factory
   * functions, allowing the client to create assets on the fly as well as load
   * them from the filesystem.
   *
   * \note
   * In general, clients will not want to manage their own set of asset caches,
   * but instead use an AssetCachePool object, which ties all the types of cache
   * together into one interface.
   *
   * Example:
   * \code
   *
   * using namespace a3m;
   *
   * // Cache storing Texture2D assets
   * Texture2DCache::Ptr textureCache(new Texture2DCache());
   *
   * // Add a filesystem source
   * registerSource(*textureCache, "assets");
   *
   * // Create two textures, but because both textures reference the same
   * // image data, only one Texture object will be created and texture1 and
   * // texture2 will reference the same object.
   * Texture::Ptr texture1 = textureCache->get("image.bmp");
   * Texture::Ptr texture2 = textureCache->get("image.bmp");
   *
   * // After releasing both textures, flushing the cache will destroy the
   * // texture asset, as the only reference to it exists in the cache.
   * texture1.reset();
   * texture2.reset();
   * textureCache->flush();
   *
   * // ... device context is destroyed
   *
   * // Make the cache release device resources from its control, to stop the
   * // cache from trying to deallocate the resources using a destroyed context.
   * textureCache->release();
   * textureCache.reset(); // (represents textureCache going out of scope)
   *
   * \endcode
   *
   * @{
   */

  /**
   * AssetCache class.
   * Container class for shared assets.
   */
  template <typename T>
  class AssetCache : public Shared, NonCopyable
  {
  public:
    A3M_NAME_SHARED_CLASS( AssetCache )

    /** Smart pointer type for this class. */
    typedef SharedPtr< AssetCache<T> > Ptr;

    /**
     * Constructor.
     */
    AssetCache();

    /**
     * Virtual destructor.
     */
    virtual ~AssetCache();

    /**
     * Registers an AssetLoader for use when loading assets.
     * Asset loaders perform the actual reading of the asset data from a data
     * stream, and pass that information to the AssetCache.  A default loader is
     * generally automatically registered for the individual cache types, but if
     * other file formats require support, custom loaders will need to be
     * registered.
     */
    void registerLoader(
        SharedPtr< AssetLoader<T> > const& loader /**< Loader to register */);

    /**
     * Registers a StreamSource for use when loading assets.
     * When loading assets, the cache needs to know where to obtain the streams
     * from which the assets are loaded.  StreamSource objects generally
     * represent a location on disk or an archive, but can also represent a
     * source of streams in memory.
     */
    void registerSource(
        StreamSource::Ptr const& source /**< Source to register */);

    /**
     * Sets the source used to cache assets such as precompiled shader programs.
     * This should not be confused with the regular registerSource() function,
     * which specifies sources from which assets can be loaded.  The cache
     * source will generally only be used by asset loaders.
     */
    void setCacheSource(
        StreamSource::Ptr const& source /**< StreamSource to register */);

    /**
     * Acquires a read-only data stream from one of the registered sources.
     * This function will generally be used by registered loaders.
     * \return Stream, or null if the stream was not found
     */
    Stream::Ptr getStream(A3M_CHAR8 const* name /**< Name of the stream */);

    /**
     * Acquires a data stream from the cache source.
     * It is necessary to set the cache source using setCacheSource() before
     * this function can return anything other than null.
     * \return Stream, or null if the stream could not be found or created
     */
    Stream::Ptr getCacheStream(
        A3M_CHAR8 const* name,
        /**< Name of stream */
        A3M_BOOL writable = A3M_FALSE
        /**< A3M_TRUE = source is writable */);

    /**
     * Loads or retrieves an asset from the cache.
     * If it already exists, a reference to the asset is returned; otherwise,
     * an attempt will be made to load the asset using the registered loaders.
     * If the asset cannot be loaded, a null pointer will be returned.  A unique
     * asset can optionally be requested, in which case a new copy of the asset
     * will be loaded even if a copy already exists in the cache.  This new copy
     * is guarenteed to be unique (i.e. it will not be returned by the cache at
     * any point in the future).
     * \return The requested asset, or null if it could not be loaded
     */
    SharedPtr<T> get(A3M_CHAR8 const* name /**< Name of the asset to get */);

    /**
     * Reports whether a particular asset exists (is loaded) in the cache.
     * \return A3M_TRUE if the asset exists
     */
    A3M_BOOL exists(A3M_CHAR8 const* name /**< Name of the asset */) const;

    /**
     * Removes assets which are unreferenced from outside the cache.
     * It is essential to call this function to prevent unused assets from
     * accumulating in the cache and taking up memory.
     */
    void flush();

    /**
     * Releases device resources from the control of the cache.
     * In some situations, the lifetime of the device context (e.g. OpenGL) is
     * not in the control of the client.  In these cases, it is essential to
     * ensure that A3M does not try to free device resources on an already
     * destroyed context (resources are freed automatically on context
     * destruction).  This function releases the resources from the control of
     * the cache, and leaves deallocation to the device context.
     */
    void release();

    /**
     * Get total number of assets registered with this asset cache.
     * \return Total number of assets
     */
    A3M_UINT32 getCount() const;

    /**
     * Get existing asset from the asset cache using its index.
     * Use this in combination with getAssetCount() to iterate over the contents
     * of the cache.
     * \return Asset corresponding to the index provided
     */
    SharedPtr<T> get(A3M_UINT32 index /**< Index of the asset required */) const;

  protected:
    /**
     * Returns this cache's resource cache.
     * \return Resource cache
     */
    detail::ResourceCache::Ptr getResourceCache();

    /** Load new asset.
     * Loads a new asset using one of the registered AssetLoaders.
     * \return Newly loaded asset
     */
    SharedPtr<T> load(A3M_CHAR8 const* name /**< Name of the asset to load */);

    /** Adds an existing asset to the cache.
     * If an asset with the same name already exists it will be replaced by the
     * new one.
     */
    void add(
        SharedPtr<T> const &asset,
        /**< Asset to add to the cache */
        A3M_CHAR8 const* name
        /**< Name by which to store the asset in the cache */);

  private:
    /** Predicate to use when flushing the asset list.
     */
    static A3M_BOOL isUnique(SharedPtr<T> const& asset /**< Asset to check */);

    typedef std::map< std::string, SharedPtr<T> > AssetMap;
    typedef std::list< SharedPtr<T> > AssetList;
    typedef typename AssetMap::iterator AssetMapItr;
    typedef typename AssetMap::const_iterator AssetMapConstItr;
    typedef typename AssetList::const_iterator AssetListConstItr;
    typedef std::vector< SharedPtr< AssetLoader<T> > > AssetLoaderVector;

    detail::ResourceCache::Ptr m_resourceCache; /**< Device resource cache */
    detail::AssetPath::Ptr m_assetPath; /**< Paths to search when loading assets */
    StreamSource::Ptr m_cacheSource; /**< Stream source for cached assets */
    AssetMap m_namedAssets;  /**< Cached mapped assets */
    AssetList m_unnamedAssets;  /**< Cached unmapped assets */
    AssetLoaderVector m_loaders; /**< List of all registered loaders */
  };

  /**
   * Registers a filesystem path as a source for loading assets.
   * \sa AssetCache::registerSource()
   */
  template<typename T>
  void registerSource(
      AssetCache<T>& cache,
      /**< Cache to use */
      A3M_CHAR8 const* path,
      /**< Filesystem path to register as source */
      A3M_BOOL isArchive = A3M_FALSE
      /**< A3M_TRUE = location is an archive */);

  /**
   * Registers a filesystem path as a source for caching assets.
   * \sa AssetCache::setCacheSource()
   */
  template<typename T>
  void setCacheSource(
      AssetCache<T>& cache,
      /**< Cache to use */
      A3M_CHAR8 const* path
      /**< Filesystem path to register as source */);

/******************************************************************************
 * Implementation
 ******************************************************************************/

  template <typename T>
  AssetCache<T>::AssetCache() :
    m_resourceCache(new detail::ResourceCache()),
    m_assetPath(new detail::AssetPath())
  {
  }

  template <typename T>
  AssetCache<T>::~AssetCache()
  {
  }

  template <typename T>
  void AssetCache<T>::registerLoader(SharedPtr< AssetLoader<T> > const& loader)
  {
    m_loaders.push_back(loader);
  }

  template <typename T>
  void AssetCache<T>::registerSource(StreamSource::Ptr const& source)
  {
    m_assetPath->add(source);
  }

  template <typename T>
  void AssetCache<T>::setCacheSource(StreamSource::Ptr const& source)
  {
    m_cacheSource = source;
  }

  template <typename T>
  Stream::Ptr AssetCache<T>::getStream(A3M_CHAR8 const* name)
  {
    return m_assetPath->find(name);
  }

  template <typename T>
  Stream::Ptr AssetCache<T>::getCacheStream(
      A3M_CHAR8 const* name,
      A3M_BOOL writable)
  {
    Stream::Ptr stream;

    if (m_cacheSource)
    {
      stream = m_cacheSource->open(name, writable);
    }

    return stream;
  }

  template <typename T>
  SharedPtr<T> AssetCache<T>::get(A3M_CHAR8 const* name)
  {
    AssetMapItr existing = m_namedAssets.find(name);
    SharedPtr<T> asset;

    // If asset doesn't already exist, try to load it and add it to the cache
    if (existing == m_namedAssets.end())
    {
      asset = load(name);

      if (asset)
      {
        add(asset, name);
      }
    }
    else
    {
      // Asset already exists, so return it!
      asset = existing->second;
    }

    return asset;
  }

  template <typename T>
  A3M_BOOL AssetCache<T>::exists(A3M_CHAR8 const* name) const
  {
    return (m_namedAssets.find(name) != m_namedAssets.end());
  }

  template <typename T>
  void AssetCache<T>::flush()
  {
    // Flush mapped assets
    AssetMapItr itr = m_namedAssets.begin();
    AssetMapItr end = m_namedAssets.end();

    while (itr != end)
    {
      if (itr->second.isUnique())
      {
        m_namedAssets.erase(itr);
      }

      ++itr;
    }

    // Flush unmapped assets
    m_unnamedAssets.remove_if(&AssetCache<T>::isUnique);
    m_resourceCache->flush();
  }

  template <typename T>
  void AssetCache<T>::release()
  {
    m_resourceCache->release();
  }

  template <typename T>
  A3M_UINT32 AssetCache<T>::getCount() const
  {
    return m_namedAssets.size() + m_unnamedAssets.size();
  }

  template <typename T>
  SharedPtr<T> AssetCache<T>::get(A3M_UINT32 index) const
  {
    // \todo Be more efficient with the iteration.  This is currently O(n^2)
    // if the client is iterating over the entire range (the most likely
    // scenario).
    SharedPtr<T> asset;
    if (index < m_namedAssets.size())
    {
      AssetMapConstItr itr = m_namedAssets.begin();
      std::advance(itr, index);
      return itr->second;
    }
    else
    {
      AssetListConstItr itr = m_unnamedAssets.begin();
      std::advance(itr, index - m_namedAssets.size());
      return *itr;
    }
  }

  template <typename T>
  SharedPtr<T> AssetCache<T>::load(A3M_CHAR8 const* name)
  {
    Stream::Ptr stream = m_assetPath->find(name);
    SharedPtr<T> asset;

    // Loader needs AssetCache pointer cast to derived cache type, since the
    // loader needs to call functions not in the base class.
    typename T::CacheType& self = *static_cast<typename T::CacheType*>(this);
    for (A3M_UINT32 i = 0; i < m_loaders.size(); ++i)
    {
      asset = m_loaders[i]->load(self, name);

      if (asset)
      {
        break;
      }
    }

    return asset;
  }

  template <typename T>
  void AssetCache<T>::add(
      SharedPtr<T> const &asset,
      A3M_CHAR8 const* name)
  {
    if (name)
    {
      m_namedAssets[name] = asset;
    }
    else
    {
      m_unnamedAssets.push_back(asset);
    }
  }

  template<typename T>
  detail::ResourceCache::Ptr AssetCache<T>::getResourceCache()
  {
    return m_resourceCache;
  }

  template<typename T>
  void registerSource(
      AssetCache<T>& cache,
      A3M_CHAR8 const* path,
      A3M_BOOL isArchive)
  {
    cache.registerSource(StreamSource::get(path, isArchive));
  }

  template<typename T>
  void setCacheSource(
      AssetCache<T>& cache,
      A3M_CHAR8 const* path)
  {
    cache.setCacheSource(StreamSource::get(path));
  }

  template <typename T>
  A3M_BOOL AssetCache<T>::isUnique( SharedPtr<T> const& asset )
  {
    return asset.isUnique();
  }

  /** @} */

} /* namespace a3m */

#endif /* A3M_ASSETCACHE_H */

