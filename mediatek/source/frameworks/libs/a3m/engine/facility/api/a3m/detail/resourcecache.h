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
 * ResourceCache class
 *
 */
#ifndef A3M_RESOURCECACHE_H
#define A3M_RESOURCECACHE_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <list>                         /* for std::list */

#include <a3m/detail/resource.h>        /* for Resource   */
#include <a3m/pointer.h>                /* for SharedPtr   */
#include <a3m/noncopyable.h>            /* for NonCopyable */

namespace a3m
{
  namespace detail
  {
    /** \defgroup a3mResourcecache ResourceCache
     * \ingroup  a3mInt
     *
     * A resource cache is a collection of Resource objects.
     * All resources should be stored in a resource cache, so that they can all
     * be released on demand from a centralised location. Resource caches are an
     * implementation detail of the the different AssetCache subclasses (e.g.
     * ShaderProgramCache, Texture2DCache) which need access to device
     * resources.
     *
     * @{
     */

    /**
     * ResourceCache class.
     */
    class ResourceCache : public Shared, NonCopyable
    {
    public:
      A3M_NAME_SHARED_CLASS( ResourceCache )

      /** Smart pointer type for this class */
      typedef SharedPtr<ResourceCache> Ptr;

      /**
       * Destructor
       */
      ~ResourceCache();

      /**
       * Adds a resource to the cache.
       */
      void add(detail::Resource::Ptr const& resource /**< Resource to add */);

      /**
       * Releases (not deallocates) and removes all resources held in the cache.
       */
      void release();

      /**
       * Deallocates unique resources and removes DEALLOCATED and RELEASED
       * resources from cache.
       * This function should be called periodically to ensure that the cache
       * does not fill up with unique, DEALLOCATED and RELEASED resource objects.
       * When a resource is unique (only referenced by the cache) it is
       * unreachable and should be released by calling this function.
       */
      void flush();

    private:
      typedef std::list<detail::Resource::Ptr> ResourceList;

      ResourceList m_resources; /* List of cached resources */
    };

    /** @} */

  } /* namespace detail */

} /* namespace a3m */

#endif /* A3M_RESOURCECACHE_H */

