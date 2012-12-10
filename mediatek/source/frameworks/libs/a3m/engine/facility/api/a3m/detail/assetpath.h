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
 * AssetPath class
 *
 */
#ifndef A3M_ASSETPATH_H
#define A3M_ASSETPATH_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <vector>             /* std::vector - used in PathList */
#include <string>             /* to store cache directory location */
#include <a3m/stream.h>       /* StreamSource   */
#include <a3m/pointer.h>      /* Shared etc.    */
#include <a3m/noncopyable.h>  /* NonCopyable    */
#include <a3m/base_types.h>   /* A3M_CHAR8 etc. */

namespace a3m
{
  namespace detail
  {
    /** \defgroup a3mAssetpath AssetPath
     * \ingroup  a3mInt
     *
     * The asset path stores a set of search paths containing assets.  Search
     * paths can be either a relative or absolute path to a folder on disk or an
     * archive file.
     *
     * AssetPath is an implementation detail of higher-level objects which need
     * to access filesystem resources.
     *
     * \code
     *
     * // Declare a path to read asset data from
     * AssetPath path;
     *
     * // Add current directory to the search path
     * path.add(".");
     * // Add a zip archive to the search path
     * path.add(".\\assets.zip")
     *
     * \endcode
     *
     * \todo Full zip archive support needs to be added.
     *
     * @{
     */

    /**
     * AssetPath class.
     * Stores locations where assets should be loaded from.
     */
    class AssetPath : public Shared, NonCopyable
    {
    public:
      A3M_NAME_SHARED_CLASS( AssetPath )

      /** Smart pointer type for this class */
      typedef SharedPtr< AssetPath > Ptr;

      /** Destructor
       */
      ~AssetPath();

      /**
       * Adds a new asset location/path to the list of search paths.
       *
       * \return A3M_TRUE if successful, else A3M_FALSE
       */
      A3M_BOOL add(A3M_CHAR8 const* path,
                   /**< [in] name of path/location to add */
                   A3M_BOOL archive = A3M_FALSE
                   /**< [in] TRUE = treat this location as an archive */);

      /**
       * Adds a new asset stream to the list of search paths based on stream source.
       *
       * \return A3M_TRUE if successful, else A3M_FALSE
       */
      A3M_BOOL add(StreamSource::Ptr const &streamSource
                   /**<[in] stream source  to add */);

      /**
       * Removes an asset location/path from the list of search paths.
       *
       * \return A3M_TRUE if successful, else A3M_FALSE
       */
      A3M_BOOL remove(A3M_CHAR8 const* path
                      /**< [in] name of path/location to remove */);

      /**
       * Searches through the list of paths to locate the asset
       * with the specified name.  If successful, a valid Stream::Ptr
       * is returned from which the binary asset data can be read.
       *
       * \return Smart pointer to a Stream from which the asset can be read
       */
      Stream::Ptr find(A3M_CHAR8 const* assetName
                       /**< [in] name of asset to finde */) const;

      /** Set cache directory
       * Set the location of a directory to use to cache assets such as pre-
       * compiled shader programs.
       */
      void setCacheDirectory( A3M_CHAR8 const *dirName
                              /** location of cached files */ );

      /** Get cache directory
       * Get the location of a directory to use to cache assets such as pre-
       * compiled shader programs.
       */
      A3M_CHAR8 const *getCacheDirectory() const;

    private:
      /* Type representing the list of search paths */
      typedef std::vector< StreamSource::Ptr > PathList;

      /* List of all StreamSources (or search paths) which contain
       * assets (in the form of Streams) */
      PathList m_paths;

      /* cache directory location */
      std::string m_cacheDirectory;
    };

    /** @} */

  } /* namespace detail */

} /* namespace a3m */

#endif /* A3M_ASSETPATH_H */
