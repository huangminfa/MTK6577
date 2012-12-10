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
/**
 * Android FileStreamSource
 *
 */

#ifndef PSS_FILESTREAM_ANDROID_H
#define PSS_FILESTREAM_ANDROID_H

/******************************************************************************
 * Include Files
 ******************************************************************************/
#include <string>       /* std::string used as member of FileStreamSource */
#include <a3m/stream.h> /* StreamSource API */

namespace a3m
{
  /*
   * Concrete StreamSource class for obtaining file streams.
   */
  class FileStreamSource : public StreamSource
  {
  public:
    /*
     * Constructor
     */
    FileStreamSource(const A3M_CHAR8* name);

    /*
     * Destructor
     */
    virtual ~FileStreamSource();

    /*
     * Checks if a stream exists within this source.
     * Returns A3M_TRUE if it does, else A3M_FALSE.
     */
    virtual A3M_BOOL exists(const A3M_CHAR8* stream);

    /*
     * Opens a file stream for reading or writing.
     * Returns a smart pointer to a new file stream.
     */
    virtual Stream::Ptr open(const A3M_CHAR8* stream
                             /* [in] name of the stream to open */,
                             A3M_BOOL writable = A3M_FALSE
                             /* [in] A3M_TRUE = open a stream for writing
                                A3M_FALSE = open stream for reading */);

    /*
     * Get the name of this StreamSource.
     *
     * Returns the name/path of the StreamSource as a NULL-terminated string
     */
    virtual A3M_CHAR8 const* getName() const { return m_path.c_str(); }

  private:
    /* Maximum length for a file path */
    static const A3M_UINT32 MAX_PATH_LENGTH = 128;

    /* Local copy of the path name of this FileStreamSource */
    std::string m_path;
  };
};

#endif