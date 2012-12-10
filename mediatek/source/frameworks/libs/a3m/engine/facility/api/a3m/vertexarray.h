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
 * VertexArray class
 *
 */
#pragma once
#ifndef A3M_VERTEXARRAY_H
#define A3M_VERTEXARRAY_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/base_types.h> /* for A3M_INT32 */
#include <a3m/assert.h>     /* for A3M_ASSERT */
#include <stdlib.h>         /* for malloc */
#include <string.h>         /* for memcpy */
#include <a3m/pointer.h>    /* for SharedPtr */

namespace a3m
{
  /** \defgroup a3mVertexarray Vertex Array
   * \ingroup  a3mRefScene
   *
   * VertexArray objects contain data for one attribute of a vertex. They
   * should be created dynamically and have their lifetime managed using
   * VertexArray::Ptr. Add several VertexArray objects to a VertexBuffer
   * object for each attribute of the vertex (position, normal etc.)
   *
   * Example:
   * \code
   * A3M_FLOAT positions[] = { -0.5f,-0.5f,0, 0.5f,-0.5f,0,
                               0.5f,0.5f,0, -0.5f,0.5f,0 };
   * A3M_UINT8 colours[] = { 255,0,0, 0,255,0, 0,0,255, 255,255,255 };
   *
   * a3m::VertexArray::Ptr v_pos( new a3m::VertexArray( 4, 3, positions ) );
   * a3m::VertexArray::Ptr v_col( new a3m::VertexArray( 4, 3, colours ) );

   * a3m::VertexBuffer::Ptr vb( new a3m::VertexBuffer );
   *
   * vb->addAttrib( v_pos, "a_position" );
   * vb->addAttrib( v_col, "a_colour", a3m::VertexBuffer::ATTRIB_FORMAT_UCHAR,
   *                a3m::VertexBuffer::ATTRIB_USAGE_WRITE_MANY, true );
   * \endcode
   * @{
   */

  /** Vertex array class.
   * Holds a chunk of memory containing per-vertex data for an attribute
   * (position, normal, colour etc.)
   */
  class VertexArray : public Shared, NonCopyable
  {
  public:
    A3M_NAME_SHARED_CLASS( VertexArray )

    /** Smart pointer type */
    typedef a3m::SharedPtr< VertexArray > Ptr;

    /** Templated constructor. Reserves space for the array and optionally
     * initialises with supplied data.
     */
    template< typename T >
    VertexArray( A3M_INT32 vertexCount,
                 /**< [in] number of vertices */
                 A3M_INT32 componentCount,
                 /**< [in] number of components per vertex */
                 T const *dataInit = 0,
                 /**< [in] pointer to data to copy */
                 A3M_BOOL externalManaged = A3M_FALSE
                 /**< [in] allow the data to be managed externally */
                 );

    /** Destructor.
     * Frees the vertex array's data.
     */
    ~VertexArray();

    /** Access to the vertex array's data.
     * \return writable pointer to the vertex array's data
     */
    template< typename T >
    T *data();

    /** Const access to the vertex array's data.
     * \return non-writable pointer to the vertex array's data
     */
    template< typename T >
    T const *data() const;

    /** Const access to the vertex array's data as void *.
     * \return non-writable pointer to the vertex array's data
     */
    void const *voidData() const;

    /** Vertex count.
     * \returns the number of vertices contained in array.
     */
    A3M_INT32 vertexCount() const      {return m_vertexCount;}

    /** Component count.
     * \returns the number of components per vertex.
     */
    A3M_INT32 componentCount() const   {return m_componentCount;}

    /** Type size.
     * \returns the size in bytes of the type contained in this vertex array.
     */
    A3M_INT32 typeSize() const         {return m_typeSize;}

    /** Array size.
     * \returns the total size in bytes of this vertex array.
     */
    A3M_INT32 arraySizeInBytes() const {return m_arraySizeInBytes;}

  private:
    A3M_INT32 m_vertexCount;      /**< number of vertices */
    A3M_INT32 m_typeSize;         /**< size of type (float, char etc.) used for
                                       each component */
    A3M_INT32 m_componentCount;   /**< number of components per vertex */
    A3M_INT32 m_arraySizeInBytes; /**< size of array in bytes */
    void *m_data;                 /**< pointer to data held in this array */

    A3M_BOOL m_externalManaged;
  };


  /****************************************************************************
   * Implementation
   ****************************************************************************/

  /*
   * Constructor
   */
  template< typename T >
  inline VertexArray::VertexArray( A3M_INT32 vertexCount,
                                   A3M_INT32 componentCount,
                                   T const *dataInit,
                                   A3M_BOOL externalManaged )
  : m_vertexCount( vertexCount ),
    m_typeSize( sizeof( T ) ),
    m_componentCount( componentCount ),
    m_arraySizeInBytes( vertexCount * componentCount * sizeof( T ) ),
    m_externalManaged ( externalManaged )
  {
    if ( externalManaged )
    {
      m_data = (void*) dataInit;
    }
    else
    {
      m_data = malloc( m_arraySizeInBytes );
      if( m_data && dataInit )
      {
        memcpy( m_data, dataInit, m_arraySizeInBytes );
      }
    }
  }

  /*
   * Destructor
   */
  inline VertexArray::~VertexArray()
  {
    if ( !m_externalManaged )
    {
      if( m_data ) { free( m_data ); }
    }
  }

  /*
   * Data access as type
   */
  template< typename T >
  inline T *VertexArray::data()
  {
    A3M_ASSERT( sizeof( T ) == m_typeSize );
    return (T *)m_data;
  }

  /*
   * Constant data access as type
   */
  template< typename T >
  inline T const *VertexArray::data() const
  {
    A3M_ASSERT( sizeof( T ) == m_typeSize );
    return (T *)m_data;
  }

  /*
   * Generic data access
   */
  inline void const *VertexArray::voidData() const
  {
    return m_data;
  }

  /** @} */

} /* namespace a3m */

#endif /* A3M_VERTEXARRAY_H */
