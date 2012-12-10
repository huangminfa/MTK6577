/**************************************************************************
 *
 * Copyright (c) 2012 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 ***************************************************************************/
/** \file
 * Memory Stream API.
 */

#pragma once
#ifndef A3M_MEMORY_STREAM_H
#define A3M_MEMORY_STREAM_H

/******************************************************************************
 * Include Files
 ******************************************************************************/
#include <string> /* for std::string */
#include <map> /* for std::map */

#include <a3m/stream.h> /* for Stream and StreamSource */

namespace a3m
{
  /** \defgroup a3mMemorystream MemoryStream
   * \ingroup  a3mRefAssets
   *
   * The MemoryStream and MemoryStreamSource pair allow blocks of data in
   * memory to be read using a stream.
   * MemoryStream objects are generally used in the asset loading process.
   *
   * @{
   */

  /**
   * Stream encapsulating a "filesystem" resource in memory.
   */
  class MemoryStream : public a3m::Stream
  {
  public:
    /** Smart pointer type for this class */
    typedef a3m::SharedPtr< MemoryStream > Ptr;

    /**
     * Constructor, taking a pointer to the data and the size of the data.
     * The stream will simply point at the data given to it, and will not try
     * to deallocate the memory.  It is up to the client to ensure that the
     * memory is valid for the lifetime of the stream.  It is generally safest
     * to only point memory streams at globally defined blocks of memory.
     */
    MemoryStream(
        void const* data,
        /**< Pointer to data source from which to stream */
        A3M_INT32 size
        /**< Size of data in bytes */);

    /**
     * Clones this stream.
     * The cloned stream will have its index reset to zero.
     */
    MemoryStream::Ptr clone();

    // Override
    A3M_BOOL eof();

    // Override
    A3M_INT32 size();

    // Override
    A3M_INT32 seek(
        A3M_UINT32 offset
        /**< Byte offset from the start of the stream to which to seek */);

    // Override
    A3M_INT32 tell();

    // Override
    A3M_INT32 read(
        void* dest,
        /**< Pointer to destination buffer */
        A3M_UINT32 byteLength
        /**< Number of bytes to read from the stream */);

    // Override
    A3M_INT32 write(
        const void* source,
        /**< Pointer to source data */
        A3M_UINT32 byteLength
        /**< Number of bytes to write to the stream */);

  private:
    A3M_CHAR8 const* m_data; /**< Pointer to stream data */
    A3M_INT32 m_size; /**< Size of the data */
    A3M_INT32 m_index; /**< Pointer into the data */
  };

  /**
   * StreamSource for memory accessing "filesystem" resources in memory.
   */
  class MemoryStreamSource : public a3m::StreamSource
  {
  public:
    /** Smart pointer type for this class */
    typedef a3m::SharedPtr< MemoryStreamSource > Ptr;

    /**
     * Constructor.
     */
    MemoryStreamSource();

    /**
     * Adds a stream to this source.
     */
    void add(A3M_CHAR8 const* name, MemoryStream::Ptr const& stream);

    // Override
    A3M_BOOL exists(
        const A3M_CHAR8* stream
        /**< Stream whose existence to check */);

    // Override
    a3m::Stream::Ptr open(
        const A3M_CHAR8* stream,
        /**< Stream to open */
        A3M_BOOL writable = A3M_FALSE
        /**< Memory stream sources are never writable, so this parameter should
         * always be set to A3M_FALSE. */);

    // Override
    A3M_CHAR8 const* getName() const;

  private:
    typedef std::map<std::string, MemoryStream::Ptr> MemoryStreamMap;

    std::string m_name; /**< Name of the stream source */
    MemoryStreamMap m_streams; /**< Collection of streams */
  };

/** @} */

}; /* namespace a3m */

#endif /* A3M_MEMORY_STREAM_H */

