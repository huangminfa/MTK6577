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
 * AssetLoader class
 *
 */
#pragma once
#ifndef A3M_ASSETLOADER_H
#define A3M_ASSETLOADER_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/pointer.h>          /* SharedPtr etc.   */
#include <a3m/noncopyable.h>      /* NonCopyable      */

namespace a3m
{
  /** \defgroup a3mAssetloader AssetLoader
   * \ingroup  a3mRefAssets
   *
   * The asset loader is a base class for the loading of assets
   * from the file system.  Typically, a loader object gets registered
   * with an associated AssetCache for loading of new assets.
   *
   * A number of default asset loaders exist within A3M (e.g. regular
   * Texture2D, PVR Texture2D, TextureCube, ShaderProgram).  These are
   * registered automatically with their associated AssetCache objects by
   * AssetCachePool upon construction.  Therefore it is not necessary for the
   * client to explicitly register loaders when using AssetCachePool for loading
   * assets.
   *
   * The default asset loaders are not part of the public API of A3M.
   *
   * \code
   * using namespace a3m;
   *
   * // Instantiate the AssetCachePool.  This automatically registers default
   * // loaders for each asset and there's no need for the client to register
   * // loaders (provided the default loaders are sufficient), as well as
   * // creating a ResourceCache for tracking of allocated resources.
   * AssetCachePool::Ptr pool(new AssetCachePool());
   *
   * // Set the search path where assets should be read from.  This call will
   * // register the path with all caches owned by the pool.
   * registerSource(*pool, ".");
   *
   * // Load an asset using the appropriate cache
   * Texture2D::Ptr texture = pool->texture2DCache()->get("elephant.png");
   *
   * // The client can still register a custom loader...
   * CustomTextureLoader::Ptr customLoader(new CustomTextureLoader());
   * pool->texture2DCache()->registerLoader(customLoader);
   *
   * Texture2D::Ptr texture2 = pool->texture2DCache()->get("giraffe.custom");
   * // If the default loader can't load "giraffe.custom", the cache will
   * // try to load it with customLoader
   *
   * \endcode
   *
   * @{
   */

  /**
   * AssetLoader class.
   * Reads binary data from the filesystem and returns a new asset.
   */
  template <typename T>
  class AssetLoader : public Shared, NonCopyable
  {
  public:
    A3M_NAME_SHARED_CLASS( AssetLoader )

    /** Smart pointer type for this class */
    typedef SharedPtr< AssetLoader<T> > Ptr;

    /** Destructor
     */
    virtual ~AssetLoader() {};

    /** Asset load routine.
     * Loads a new asset by name.
     *
     * \return Smart pointer to a new asset
     */
    virtual SharedPtr<T> load(
        typename T::CacheType& cache, /**< Cache to use to create the asset */
        A3M_CHAR8 const* name /**< Name of the asset to load */) = 0;
  };

  /** @} */

} /* namespace a3m */

#endif /* A3M_ASSETLOADER_H */
