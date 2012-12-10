/**************************************************************************
 *
 * Copyright (c) 2010 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 ***************************************************************************/
/** \file
 * PSS Stream API (part of the PSS file system abstraction layer).
 */

#pragma once
#ifndef PSS_STREAM_H
#define PSS_STREAM_H
/******************************************************************************
 * Include Files
 ******************************************************************************/
#include <a3m/pointer.h>     /* SharedPtr<T> */
#include <a3m/base_types.h>  /* A3M_BOOL etc. */
#include <a3m/noncopyable.h> /* for NonCopyable */
#include <a3m/assert.h>      /* for A3M_COMPILE_ASSERT */
#include <limits>    /* for numeric_limits::is_specialized */

namespace a3m
{

/** \defgroup  a3mPssStream PSS Stream API (File System Abstraction Layer)
 *  \ingroup   a3mPss
 *
 * The PSS Stream API provides platform-independent access to the file system.
 * The API consists of two classes: Stream and StreamSource.
 *
 * StreamSource class represents a location (either on disk or in an archive)
 * containing Streams.  In order to obtain a StreamSource, the class static
 * "get" method is used:
 * \code
 *  // Obtain a folder representing a directory on disk
 *  StreamSource::Ptr folder = a3m::StreamSource::get("path\to\folder");
 *
 *  // Obtain a reference to an archive on disk
 *  StreamSource::Ptr arch = a3m::StreamSource::get("path\to\arch\arch.zip");
 * \endcode
 *
 * The Stream class represents either an input or output data stream.  Input
 * streams are used to read data from disk (or archive).  Output streams are
 * used to write data to disk.  Writing to an archive is not supported.
 * Streams can only be obtained from a StreamSource, for example:
 * \code
 *  // Get a reference to the directory containing files
 *  StreamSource::Ptr folder = a3m::StreamSource::get("path\to\folder");
 *
 *  // Open "path\to\folder\in_file_name.ext" for reading
 *  Stream::Ptr inFile folder->open("in_file_name.ext");
 *
 *  // Open "path\to\folder\out_file_name.ext" for writing
 *  Stream::Ptr outFile folder->open("out_file_name.ext", A3M_TRUE);
 * \endcode
 *
 *  @{
 */

  /**
   * Abstract Stream base class
   *
   * The Stream class represents either an input or output data stream.  Input
   * streams are used to read data from disk (or archive).  Output streams are
   * used to write data to disk.
   */
  class Stream : public Shared
  {
  public:
    A3M_NAME_SHARED_CLASS( Stream )

    /** Smart pointer type for this class */
    typedef SharedPtr< Stream > Ptr;

    /**
     * Virtual destructor in case derived classes are destroyed through
     * pointers to this class.
     */
    virtual ~Stream() {}

    /**
     * End-of-file check.
     * \return A3M_TRUE if the end-of-file has been reached.
     */
    virtual A3M_BOOL eof() = 0;

    /**
     * Size of the stream.
     *
     * \return Size of the stream in bytes.
     */
    virtual A3M_INT32 size() = 0;

    /**
     * Stream seek routine.
     * Modifies the internal read/write pointer of the stream.  All seek
     * operations are referenced from the start of the stream.
     *
     * \return Current position (in bytes) of the internal read/write pointer
     * or -1 if unsuccessful.
     */
    virtual A3M_INT32 seek(A3M_UINT32 offset
                           /**< [in] Byte offset from the start of the stream
                                to seek to */) = 0;

    /**
     * Stream tell routine.
     * Returns the current position (in bytes) of the internal read/write
     * pointer of the stream referenced from the start of the stream.
     *
     * \return Current position (in bytes) of the internal read/write pointer
     * or -1 if unsuccessful.
     */
    virtual A3M_INT32 tell() = 0;

    /**
     * Stream data read routine.
     * Reads raw data from the stream and copies it to the specified
     * memory location.  All data reads are from the current read location
     * (maintained internally) which gets updated after each successful read
     * operation.  The read pointer can be modified (or reset back to the
     * start) with the #seek method.
     *
     * \return Number of bytes successfully read from the stream.
     */
    virtual A3M_INT32 read(void* dest
                           /**< [in] pointer to destination buffer */,
                           A3M_UINT32 byteLength
                           /**< [in] number of bytes to read */) = 0;

    /**
     * Function template to read arithmetic variable.
     * Reads data from the stream and copies it to the specified
     * variable.  All data reads are from the current read location
     * (maintained internally) which gets updated after each successful read
     * operation.  The read pointer can be modified (or reset back to the
     * start) with the #seek method.
     * This function will not compile if the variable passed isn't a built
     * in arithmetic type (char, int, short, long, float, double and unsigned
     * variations on those themes).
     *
     * \return Number of bytes successfully read from the stream.
     * \todo Improvements could be made to use the returned value from stream
     * reads and writes.   Logging errors or use of assert
     */
    template< typename T >
    A3M_INT32 read( T &dest /**< [in] variable to read */)
    {
      /* Check that T is a built in arithmetic type */
      A3M_COMPILE_ASSERT( std::numeric_limits< T >::is_specialized )
      return read( (void *)&dest, sizeof( T ) );
    }

    /**
     * Stream data write routine.
     * This copies data from the specified memory location to the output
     * stream.  Writes take place from the current write position maintained
     * internally by the stream and gets updated after each successful write
     * operation.  The write pointer can be modified (or reset back to the
     * start) with the #seek method.
     *
     * \return Number of bytes successfully written to the stream.
     */
    virtual A3M_INT32 write(const void* source
                            /**< [in] pointer to source data */,
                            A3M_UINT32 byteLength
                            /**< [in] number of bytes to write */) = 0;
  };

  /**
   * Abstract StreamSource base class
   *
   * This represents a location (either on disk or in an archive)
   * containing Streams.
   */
  class StreamSource : public Shared
  {
  public:
    A3M_NAME_SHARED_CLASS( StreamSource )

    /** Smart pointer type for this class */
    typedef SharedPtr< StreamSource > Ptr;

    /**
     * Virtual destructor in case derived classes are destroyed through
     * pointers to this class.
     */
    virtual ~StreamSource() {}

    /**
     * Static method for obtaining a reference to a StreamSource.
     *
     * \return Smart pointer to a new StreamSource.
     */
    static Ptr get(const A3M_CHAR8* name
                   /**< [in] name of the StreamSource to get */,
                   A3M_BOOL archive = A3M_FALSE
                   /**< [in] A3M_TRUE -> get an archive StreamSource
                        A3M_FALSE -> StreamSource is a normal folder */);

    /**
     * Checks if a stream exists within this source.
     *
     * \return A3M_TRUE if a stream is present, else A3M_FALSE.
     */
    virtual A3M_BOOL exists(const A3M_CHAR8* stream
                            /**< [in] name of the stream to search for */) = 0;

    /**
     * Open stream for reading or writing.
     * This creates a new instance of the stream and returns a reference
     * to it.  By default, streams are opened for reading.
     *
     * \return Smart pointer to a new Stream
     */
    virtual Stream::Ptr open(const A3M_CHAR8* stream
                             /**< [in] name of the stream to open */,
                             A3M_BOOL writable = A3M_FALSE
                             /**< [in] A3M_TRUE -> open a stream for writing
                                  A3M_FALSE -> open stream for reading */) = 0;

    /**
     * Get the name of this StreamSource.
     *
     * \return Name/path of the StreamSource as a NULL-terminated string
     */
    virtual A3M_CHAR8 const* getName() const = 0;
  };

/** @} */

}; /* namespace a3m */

#endif
