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
 * VertexBuffer class
 *
 */
#pragma once
#ifndef A3M_VERTEXBUFFER_H
#define A3M_VERTEXBUFFER_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/detail/bufferresource.h> /* for BufferResource */
#include <a3m/assetcache.h> /* file IO */
#include <a3m/stream.h> /* file IO */
#include <a3m/pointer.h> /* for SharedPtr */
#include <a3m/noncopyable.h> /* for NonCopyable */
#include <a3m/vertexarray.h> /* for VertexArray */
#include <a3m/detail/resourcecache.h> /* for ResourceCache */

namespace a3m
{
  /** \defgroup a3mVertexbuffer Vertex Buffer
   * \ingroup  a3mRefScene
   *
   * VertexBuffer objects contain all the data for all vertices used in a draw
   * operation. Add several VertexArray objects to a VertexBuffer object for
   * each attribute of the vertex (position, normal etc.)
   *
   * Vertex buffers are assets and are therefore created and managed by an
   * VertexBufferCache.
   *
   * \note
   * Generally, the client will want to combine IndexBuffer objects and
   * VertexBuffer objects into a Mesh asset, using the MeshCache.
   *
   * Example:
   * \code
   * // Create a VertexBufferCache.
   * VertexBufferCache::Ptr cache( new VertexBufferCache() );
   *
   * A3M_FLOAT positions[] = { -0.5f,-0.5f,0, 0.5f,-0.5f,0,
   *                           0.5f,0.5f,0, -0.5f,0.5f,0 };
   * A3M_UINT8 colours[] = { 255,0,0, 0,255,0, 0,0,255, 255,255,255 };
   *
   * a3m::VertexArray::Ptr v_pos( new a3m::VertexArray( 4, 3, positions ) );
   * a3m::VertexArray::Ptr v_col( new a3m::VertexArray( 4, 3, colours ) );
   *
   * a3m::VertexBuffer::Ptr vb = cache->create();
   *
   * vb->addAttrib( v_pos, "a_position" );
   * vb->addAttrib( v_col, "a_colour", a3m::VertexBuffer::ATTRIB_FORMAT_UCHAR,
   *                a3m::VertexBuffer::ATTRIB_USAGE_WRITE_MANY, true );
   * \endcode
   * @{
   */
  /** Maximum significant length for a vertex attribute name */
  const A3M_UINT32 VERTEX_ATTRIBUTE_MAX_NAME_LENGTH = 32;

  // Forward declarations
  class VertexBufferCache;

  /** Vertex buffer class.
   * Contains all the vertex data (positions, normals etc.) for drawing a mesh.
   * Any number of VertexArrays can be added to a VertexBuffer. When you add a
   * VertexArray you must associate it with an attribute name corresponding to
   * an vertex attribute used in a shader program (a varying variable). After
   * the commit() method is called, no new arrays should be added. Any arrays
   * that were marked with ATTRIB_USAGE_COMMIT will be copied to GPU memory and
   * cannot be accessed anymore.
   */
  class VertexBuffer : public Shared, NonCopyable
  {
  public:
    A3M_NAME_SHARED_CLASS( VertexBuffer )

    /** Smart pointer type */
    typedef a3m::SharedPtr< VertexBuffer > Ptr;

    /** Cache type for this class */
    typedef VertexBufferCache CacheType;

    /** Destructor */
    ~VertexBuffer();

    /** Attribute format.
     * Describes the type contained in the associated VertexArray
     */
    enum AttribFormat
    {
      ATTRIB_FORMAT_INT8,       /**< signed 8 bit integer values */
      ATTRIB_FORMAT_UINT8,      /**< unsigned 8 bit integer values */
      ATTRIB_FORMAT_INT16,      /**< signed 16 bit integer values */
      ATTRIB_FORMAT_UINT16,     /**< unsigned 16 bit integer values */
      ATTRIB_FORMAT_FLOAT,      /**< floating point 32 bit values */
      ATTRIB_FORMAT_FIXED       /**< fixed point 32 bit values */
      /* ATTRIB_FORMAT_HALF_FLOAT, fp 16-bit values, are not supported */
    };

    /** Attribute usage.
     * Describes whether the associated VertexArray should be placed in
     * GPU memory or stay in client memory
     */
    enum AttribUsage
    {
      ATTRIB_USAGE_WRITE_ONCE, /**< The attribute will not change and can be
                                    placed in GPU memory */
      ATTRIB_USAGE_WRITE_MANY, /**< The attribute might change and should not
                                    be placed in GPU memory */
    };

    /** Add an attribute (with associated array) to the buffer.
    */
    void addAttrib( VertexArray::Ptr const &va,
                    /**< pointer to array */
                    const A3M_CHAR8 *name,
                    /**< name of the attribute (should match a shader
                         attribute name) */
                    AttribFormat format = ATTRIB_FORMAT_FLOAT,
                    /**< numerical format of data (eg ATTRIB_FORMAT_INT8) */
                    AttribUsage usage = ATTRIB_USAGE_WRITE_ONCE,
                    /**< usage type, ATTRIB_USAGE_WRITE_ONCE /_MANY */
                    A3M_BOOL normalise = A3M_FALSE
                    /**< if true, the attribute will be normalised to
                         [0,1] (unsigned types) or [-1,1] (signed types)
                         before being passed to the shader */ );

    /** Enable attribute.
     * The VertexBuffer will attempt to associate the attribute identified by
     * 'name' at the given index.
     * \return true if the attribute was found and successfuly enabled.
     */
    A3M_BOOL enableAttrib( A3M_UINT32 index,
                           /**< position of vertex attribute */
                           A3M_CHAR8 const* name
                           /**< name of vertex attribute */ );

    /** Commit the buffer.
     * After this method is used any VertexArrays added to the buffer using
     * the flag ATTRIB_USAGE_WRITE_ONCE will be optimised for drawing (they
     * may be copied to GPU memory and/or interleaved with other VertexArrays).
     * If the contents of such a VertexArray are changed by the client code,
     * those changes will not be reflected in the data held in the
     * VertexBuffer.
     */
    void commit();

    /** Write the binary data to an output stream.
     * For use by the Mesh class.
     * \return Success or failure
     */
    A3M_BOOL save( Stream::Ptr const& outStream
                  /**< output file stream mesh data*/ );

    /** Read the binary data from an input stream.
     * For use by the Mesh class. This function creates a vertex buffer object
     * and can only be used if there is no existing VBO
     * \return Success or failure
     */
    A3M_BOOL load( Stream::Ptr const& inStream
                  /**< input file stream mesh data*/ );

    /** Attribute description struct.
     * Describes an individual vertex attribute as it is packed into a chunk
     * of vertex data. This struct is intended for use with the function
     * setAllAttributes().
     */
    struct AttribDescription
    {
      A3M_CHAR8 name[VERTEX_ATTRIBUTE_MAX_NAME_LENGTH];
        /**< This name should match a shader attribute name */
      AttribFormat type;
        /**< Type of the data held in corresponding array */
      A3M_BOOL normalise;
        /**< True if attribute data should be normalised (i.e. 0-255 maps to
           0.0-1.0) on passing to the vertex shader*/
      A3M_INT32 offset;
        /**< offset from start of vertex data chunk to first instance of data
             for this attribute */
      A3M_INT32 componentCount;
        /**< Number of components per vertex (eg 3 for vector3) */
      A3M_INT32 stride;
        /**< Offset from one instance of data for this attribute to the next */
    };

    /** Set vertex buffer data for all attributes.
     * Intended to be used when you have a block of data containing all vertex
     * data (for instance reading from a file).
     */
    void setAllAttributes( AttribDescription const *attributes,
                           /**< pointer to the first attribute description */
                           A3M_UINT32 attributeCount, /**< number of vertex
                                                           attributes */
                           A3M_BYTE const *data, /**< vertex data */
                           A3M_UINT32 dataSize /**< size of vertex data
                                                   in bytes */ );
  private:
    friend class VertexBufferCache; /* Is VertexBuffer's factory class */

    /** Private constructor.
     * This constructor is called by VertexBufferCache.
     */
    VertexBuffer(
        detail::BufferResource::Ptr const& resource /**< Buffer resource */);

    /** Internal use utilty calulates vertex stride and size*/
    void getSizeAndStride( A3M_UINT32& size, A3M_INT32& stride );

    /** Internal use utilty to allocate tempData for Attribute commit  */
    void createTempData( A3M_UINT32 size,
                         A3M_INT32 stride,
                         A3M_BYTE* tempData );

    /* Forward declare struct describing an attribute -
     * private to VertexBuffer */
    struct Attrib;

    Attrib*    m_firstAttrib;  /* Pointer to first attribute in list */
    A3M_BYTE*  m_rawData;      /* storage pointer for the loaded data */
    detail::BufferResource::Ptr m_resource; /* OpenGL vertex buffer resource */
  };

  /**
   * AssetCache specialised for storing and creating VertexBuffer assets.
   */
  class VertexBufferCache : public AssetCache<VertexBuffer>
  {
  public:
    /** Smart pointer type for this class */
    typedef SharedPtr< VertexBufferCache > Ptr;

    /**
     * Constructs an empty VertexBuffer.
     * addAttrib() can be used to add vertex data to the buffer.
     * \return The vertex buffer
     */
    VertexBuffer::Ptr create(
        A3M_CHAR8 const* name = 0 /**< Optional name to give the asset. If
                                    omitted, the asset will not be reachable
                                    via the AssetCache::get() function. */);
  };

  /** @} */

} /* namespace a3m */

#endif /* A3M_VERTEXBUFFER_H */
