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
#ifndef JA3M_RESOURCE_STREAM_H
#define JA3M_RESOURCE_STREAM_H

/******************************************************************************
 * Include Files
 ******************************************************************************/
#include <string> /* for std::string */

#include <common.h> /* for GlobalRef */
#include <a3m/stream.h> /* for Stream and StreamSource */

namespace ja3m
{

  /**
   * Stream encapsulating an Android resource.
   * \note
   * This class calls code through the JNI interface, and so is coupled with
   * Java.
   */
  class ResourceStream : public a3m::Stream
  {
  public:
    /** Smart pointer type for this class */
    typedef a3m::SharedPtr< ResourceStream > Ptr;

    /**
     * Constructor.
     */
    ResourceStream(
      JNIEnv* env,
      /**< JNI environment */
      jbyteArray jByteArray
      /**< Java ByteArray to stream */);

    // Override
    A3M_BOOL eof();

    // Override
    A3M_INT32 size();

    // Override
    A3M_INT32 seek(A3M_UINT32 offset);

    // Override
    A3M_INT32 tell();

    // Override
    A3M_INT32 read(void* dest, A3M_UINT32 byteLength);

    // Override
    A3M_INT32 write(const void* source, A3M_UINT32 byteLength);

  private:
    CByteArray m_byteArray; /**< Byte array object holding Java ByteArray */
    A3M_INT32 m_index; /**< Pointer into the byte array */
  };

  /**
   * StreamSource for Android resources.
   * \note
   * This class calls code through the JNI interface, and so is coupled with
   * Java.
   */
  class ResourceStreamSource : public a3m::StreamSource
  {
  public:
    /** Smart pointer type for this class */
    typedef a3m::SharedPtr< ResourceStreamSource > Ptr;

    /**
     * Constructor.
     */
    ResourceStreamSource(
      JNIEnv* env,
      /**< JNI environment */
      jobject jResourceDataSource
      /**< Android Resources object */);

    // Override
    A3M_BOOL exists(const A3M_CHAR8* stream);

    // Override
    a3m::Stream::Ptr open(
      const A3M_CHAR8* stream, A3M_BOOL writable = A3M_FALSE);

    // Override
    A3M_CHAR8 const* getName() const;

  private:
    std::string m_name; /**< Name of the stream source */
    JavaVM* m_vm; /**< Java VM */
    GlobalRef<jobject> m_jResourceDataSource; /**< A3M ResourceDataSource */
    jmethodID m_jGet; /**< Class get() method */
    jmethodID m_jExists; /**< Class exists() method */
  };

  /** @} */

}; /* namespace a3m */

#endif /* JA3M_RESOURCE_STREAM_H */

