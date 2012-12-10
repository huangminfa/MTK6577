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
 * IndexBuffer class
 *
 */
#pragma once
#ifndef A3M_INDEX_BUFFER_H
#define A3M_INDEX_BUFFER_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/detail/bufferresource.h> /* for BufferResource */
#include <a3m/assetcache.h> /* for AssetCache */
#include <a3m/pointer.h> /* for SharedPtr */
#include <a3m/renderdevice.h> /* for render() */
#include <a3m/detail/resourcecache.h> /* for ResourceCache */

/*****************************************************************************
 * A3M Namespace
 *****************************************************************************/
namespace a3m
{
  /** \defgroup a3mIndexbuffer Index Buffer
   * \ingroup  a3mRefScene
   *
   * Index buffer objects contain indices to the vertices used in a draw
   * operation.
   *
   * Index buffers are assets and are therefore created and managed by an
   * IndexBufferCache.  Often, the client will want to combine IndexBuffer
   * objects and VertexBuffer objects into a Mesh asset, using the MeshCache.
   *
   * \note
   * Generally, the client will want to use AssetCachePool to create and manage
   * asset caches, rather than creating them manually.
   *
   * Example:
   * \code
   * // Create an IndexBufferCache.
   * IndexBufferCache::Ptr cache( new IndexBufferCache() );
   *
   * // Index array storing indices
   * A3M_UINT16 indices[] = {0, 1, 2, 0, 2, 3, 1, 2, 4};
   *
   * // Creates shared index buffer pointer
   * a3m::IndexBuffer::Ptr ibptr = cache->create(
   *   a3m::IndexBuffer::PRIMITIVE_TRIANGLES, 9, indices );
   *
   * ibptr->commit(); // Commits index buffer to GPU
   *
   * program.bindAttribs( *vb );
   *
   * a3m::RenderDevice::render(*vb, *ibptr, *ap);
   *                 // Calls a3m::RenderDevice::render()
   *                 //   which is friend to IndexBuffer class.
   *                 // It renders primitive.
   * \endcode
   * @{
   */

  // Forward declarations
  class IndexBufferCache;

  /**
   * Index buffer class.
   *
   * Holds a chunk of memory containing index array. It contains member
   * functions to commit() vertices to GPU and to draw a mesh using specified
   * primitive.
   */
  class IndexBuffer : public Shared, NonCopyable
  {
  public:
    A3M_NAME_SHARED_CLASS( IndexBuffer )

    /** Smart pointer type */
    typedef a3m::SharedPtr< IndexBuffer > Ptr;

    /** Cache type for this class */
    typedef IndexBufferCache CacheType;

    /**
     * Enum Primitive
     * Describes the type of primitive in enum.
     */
    enum Primitive
    {
      PRIMITIVE_POINTS,         /**< Points */
      PRIMITIVE_LINE_STRIP,     /**< Joins vertices to form strip of line */
      PRIMITIVE_LINE_LOOP,      /**< Line strip where last vertex joins 1st */
      PRIMITIVE_LINES,          /**< Lines joining pair of vertices */
      PRIMITIVE_TRIANGLE_STRIP, /**< Triangle strip */
      PRIMITIVE_TRIANGLE_FAN,   /**< Triangle fan */
      PRIMITIVE_TRIANGLES       /**< Triangle for every 3 vertices */
    };

    /**
     * Destructor.
     *
     * Frees index array memory and deletes index buffer.
     */
    ~IndexBuffer();

    /**
     * Checks validity of construction of index buffer.
     *
     * \return A3M_TRUE if construction is succeded else A3M_FALSE.
     */
    // \todo Index buffer is now always valid.  Check whether this can be
    // removed.
    A3M_BOOL isValid() const { return m_valid; }

    /**
     * Access to the index array's data.
     *
     * \return Writable pointer to the index array's data.
     */
    A3M_UINT16 *data() { return m_indexArray ; }

    /**
     * Const access to the index array's data.
     *
     * \return Non-writable pointer to the index array's data.
     */
    A3M_UINT16 const *data() const { return m_indexArray ; }

    /**
     * Commits the IndexBuffer for rendering.
     *
     * Committing an IndexBuffer allows the implementation to optimize
     * the indices for better performance and reduced memory consumption.
     * It offloads data to GPU.
     * After a buffer is committed, the index data is no longer available to
     * change.
     *
     * \return None.
     */
    void commit();

    /**
     * Declaring a3m::RenderDevice::render() as friend of a3m::IndexBuffer
     * class.
     *
     * Renders primitives. This function should be called to draw a mesh from
     * index array or vertex array.
     *
     * \return None.
     */
    friend void RenderDevice::render( VertexBuffer &vb,
                                      /**< Reference to vertex buffer */
                                      IndexBuffer &ib,
                                      /**< Reference to index buffer */
                                      Appearance &app
                                      /**< Reference to appearance */ );

  private:
    friend class IndexBufferCache; /* Is IndexBuffer's factory class */

    /**
     * Private constructor.
     * This constructor is called by IndexBufferCache.
     */
    IndexBuffer(
        Primitive primitive, /**< Primitive type to be rendered */
        A3M_INT32 count, /**< Number of indices to be rendered */
        A3M_BOOL allocate, /**< A3M_TRUE = allocate index array memory  */
        detail::BufferResource::Ptr const& resource /**< Buffer resource */);

    Primitive     m_primitive;       /* Primitive type */
    A3M_UINT16*   m_indexArray;      /* Pointer to index array where indices
                                        to be rendered are stored */
    A3M_INT32     m_count;           /* Number of indices to be rendered */
    detail::BufferResource::Ptr m_resource; /* OpenGL index buffer resource */
    A3M_BOOL      m_valid;           /* True if construction of index buffer
                                        has succeded else false */

    /**
     * Renders primitives.
     * This is a private function called only by a3m::RenderDevice::render()
     * It renders primitives as specified in index array or vertex array.
     */
    void draw();
  };

  /**
   * AssetCache specialised for storing and creating IndexBuffer assets.
   */
  class IndexBufferCache : public AssetCache<IndexBuffer>
  {
  public:
    /** Smart pointer type for this class */
    typedef SharedPtr< IndexBufferCache > Ptr;

    /**
     * Constructs an IndexBuffer with an index array.
     * Dynamically creates memory and copies index array to it.
     * \return The index buffer, or null if the memory could not be allocated
     */
    IndexBuffer::Ptr create(
        IndexBuffer::Primitive primitive, /**< Primitive type to be rendered */
        A3M_UINT32 count, /**< Number of indices to be rendered */
        A3M_UINT16 const* indices, /**< Pointer to an array of unsigned 16-bit
                                     indices in client memory. If a null
                                     pointer is passed, no data will be copied
                                     into the index array. */
        A3M_CHAR8 const* name = 0 /**< Optional name to give the asset. If
                                    omitted, the asset will not be reachable
                                    via the AssetCache::get() function. */);

    /**
     * Creates an IndexBuffer without an index array.
     * An IndexBuffer without an array causes vertices to be rendered in the
     * order that they appear in the VertexBuffer.
     * \return The index buffer
     */
    IndexBuffer::Ptr create(
        IndexBufferCache& cache, /**< Cache to use */
        IndexBuffer::Primitive primitive, /**< Primitive type to be rendered */
        A3M_UINT32 count, /**< Number of indices to be rendered */
        A3M_CHAR8 const* name = 0 /**< Optional name to give the asset. If
                                    omitted, the asset will not be reachable
                                    via the AssetCache::get() function. */);
  };

  /** @} */

} /* namespace a3m */

#endif /* A3M_INDEX_BUFFER_H */
