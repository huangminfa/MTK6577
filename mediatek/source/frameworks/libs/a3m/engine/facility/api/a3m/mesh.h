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
 * Mesh class
 *
 */
#pragma once
#ifndef A3M_MESH_H
#define A3M_MESH_H

#include <a3m/assetcache.h>  /* AssetCache */
#include <a3m/indexbuffer.h>  /* IndexBuffer */
#include <a3m/matrix4.h>    /* 4D Matrix maths - Matrix4f */
#include <a3m/vertexbuffer.h> /* VertexBuffer */

namespace a3m
{
  /** \defgroup a3mMesh Mesh
   * \ingroup  a3mRefScene
   *
   * Mesh is a container for an IndexBuffer and VertexBuffer and defines an
   * object which has geometry and can be drawn.
   * Meshes are assets and are therefore created and managed by a MeshCache.
   *
   * \note
   * Generally, the client will want to use AssetCachePool to create and manage
   * asset caches, rather than creating them manually.
   *
   * \code
   * // Meshes use IndexBuffer and VertexBuffer objects, so we create
   * // IndexBufferCache and VertexBufferCache objects first.
   * IndexBufferCache::Ptr ibCache( new IndexBufferCache() );
   * VertexBufferCache::Ptr vbCache( new VertexBufferCache() );
   *
   * // Create a mesh cache
   * MeshCache::Ptr cache( new MeshCache( ibCache, vbCache ) );
   *
   * // ... create your header, and index and vertex buffers here...
   *
   * // Create a mesh from existing vertex and index buffers
   * Mesh::Ptr model = cache->create(
   *     myMeshHeader, myIndexBuffer, myVertexBuffer);
   *
   * \endcode
   *
   *  @{
   */

  /** Mesh Header to contain localised mesh attributes */
  struct MeshHeader
  {
    /// Indicates missing bounding information -> cannot frustum cull.
    static const A3M_FLOAT INVALID_BOUNDING_RADIUS;

    A3M_FLOAT boundingRadius;  /**< bounding radius for fustrum culling */
    a3m::Vector3f boundingBox; /**< bounding box is unused */

    a3m::Matrix4f offsetOrigin; /**< internal pivot for packed objects */

    /// Default constructor has invalid bounding volume.
    MeshHeader();
  };

  // Forward declarations
  class MeshCache;

  /** Mesh class.
   *
   * Initial implementation of a mesh class that can be drawn and has geometry
   *
   */
  class Mesh : public Shared, NonCopyable
  {
  public:
    A3M_NAME_SHARED_CLASS( Mesh )

    /** Smart pointer type */
    typedef SharedPtr< Mesh > Ptr;

    /** Cache type for this class */
    typedef MeshCache CacheType;

    /** Get IndexBuffer
     */
    IndexBuffer& getIndexBuffer() { return *m_indexBuffer; }

    /** Get VertexBuffer
     */
    VertexBuffer& getVertexBuffer() { return *m_vertexBuffer; }

    /** Get Header
     */
    MeshHeader& getHeader() { return m_meshHeader; }

  private:
    friend class MeshCache; /* Is Mesh's factory class */

    /**
     * Private constructor.
     * This constructor is called by MeshCache.
     */
    Mesh(MeshHeader const& header,
         /**< Mesh header to be copied */
         IndexBuffer::Ptr const& indexBuffer,
         /**< Index buffer pointer */
         VertexBuffer::Ptr const& vertexBuffer
         /**< Vertex buffer pointer */);

    MeshHeader        m_meshHeader;   /**< Mesh header */
    IndexBuffer::Ptr  m_indexBuffer;  /**< Indices */
    VertexBuffer::Ptr m_vertexBuffer; /**< Vertices */
  };

  /**
   * AssetCache specialised for storing and creating Mesh assets.
   */
  class MeshCache : public AssetCache<Mesh>
  {
  public:
    /** Smart pointer type for this class */
    typedef SharedPtr< MeshCache > Ptr;

    /**
     * Constructor.
     * MeshCache requires an IndexBufferCache and VertexBufferCache as Mesh
     * assets contain IndexBuffer and VertexBuffer assets.
     */
    MeshCache(
        IndexBufferCache::Ptr const& indexBufferCache,
        /**< IndexBuffer cache */
        VertexBufferCache::Ptr const& vertexBufferCache
        /**< VertexBuffer cache */);

    /**
     * Constructs a Mesh from a header, index buffer and vertex buffer.
     * \return The mesh
     */
    Mesh::Ptr create(
        MeshHeader const& header,
        /**< Mesh header to be copied */
        IndexBuffer::Ptr const& indexBuffer,
        /**< Index buffer pointer */
        VertexBuffer::Ptr const& vertexBuffer,
        /**< Vertex buffer pointer */
        A3M_CHAR8 const* name = 0
        /**< Optional name to give the asset. If omitted, the asset will not be
         * reachable via the AssetCache::get() function. */);


    /**
     * Returns the IndexBufferCache associated with this cache.
     * \return IndexBuffer cache
     */
    IndexBufferCache::Ptr indexBufferCache() const
    {
      return m_indexBufferCache;
    }

    /**
     * Returns the VertexBufferCache associated with this cache.
     * \return VertexBuffer cache
     */
    VertexBufferCache::Ptr vertexBufferCache() const
    {
      return m_vertexBufferCache;
    }

  private:
    IndexBufferCache::Ptr m_indexBufferCache; /**< IndexBuffer cache */
    VertexBufferCache::Ptr m_vertexBufferCache; /**< VertexBuffer cache */
  };

/******************************************************************************
 * Implementation
 ******************************************************************************/

  /** Utility function to compute bounding radius from an array of positions.
      \return             the length of the outlying position.
      */
  extern A3M_FLOAT computeMaximumLength(
    VertexArray::Ptr vertexArray
      /**< containing {float, float, float} vertex positions*/);

  /** @} */

} /* namespace a3m */

#endif /* A3M_MESH_H */

