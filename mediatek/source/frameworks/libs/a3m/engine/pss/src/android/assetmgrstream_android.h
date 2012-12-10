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
 * Android AssetMgrStreamSource
 *
 */

#ifndef PSS_ASSETMGRSTREAM_ANDROID_H
#define PSS_ASSETMGRSTREAM_ANDROID_H

/******************************************************************************
 * Include Files
 ******************************************************************************/
#include <a3m/stream.h>                 /* StreamSource API */

class AAssetManager;

namespace a3m
{
  /*
   * Concrete StreamSource class for obtaining streams residing in the "assets"
   * folder on Android
   */
  class AssetMgrStreamSource : public StreamSource
  {
  public:
    /*
     * Constructor
     */
    AssetMgrStreamSource(AAssetManager* mgr);

    /*
     * Destructor
     */
    virtual ~AssetMgrStreamSource();

    /*
     * Checks if a stream exists within this source.
     * Returns A3M_TRUE if it does, else A3M_FALSE.
     */
    virtual A3M_BOOL exists(const A3M_CHAR8* stream);

    /*
     * Opens a file stream for reading or writing.
     * Returns a smart pointer to a new archive stream.
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
    virtual A3M_CHAR8 const* getName() const { return m_name; }

  private:
    /* Maximum length for an absolute path + archive name */
    static const A3M_UINT32 MAX_NAME_LENGTH = 128;

    /* Local copy of the path+name of this ZIP archive */
    A3M_CHAR8 m_name[MAX_NAME_LENGTH];

    /* hold AssetManager information */
    AAssetManager* m_manager;   // asset manager from Android
  };
};

#endif
