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
 * PVR Texture Loader class
 *
 */

#pragma once
#ifndef A3M_PVRTEXTURELOADER_H
#define A3M_PVRTEXTURELOADER_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/texture2d.h>         /* Texture2D etc. */
#include <a3m/assetloader.h>       /* AssetLoader */

namespace a3m
{
  /** \ingroup a3mAssetloader
   *
   * PVR Texture Loader - an AssetLoader for loading PVR image files.
   *
   * @{
   */

  /**
   * 2D texture loader based on STB image library.
   */
  class PvrTextureLoader : public AssetLoader<Texture2D>
  {
  public:
    /** Destructor.
     */
    virtual ~PvrTextureLoader() {}

    // Override
    Texture2D::Ptr load(
        Texture2DCache& cache, /**< Cache to use to create the asset */
        A3M_CHAR8 const* name /**< Name of the asset to load */);
  };

  /** @} */

} /* namespace a3m */

#endif /* A3M_PVRTEXTURELOADER_H */
