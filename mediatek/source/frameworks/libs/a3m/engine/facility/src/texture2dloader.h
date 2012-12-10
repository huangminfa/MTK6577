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
 * 2D Texture Loader class
 *
 */

#pragma once
#ifndef A3M_TEXTURE2DLOADER_H
#define A3M_TEXTURE2DLOADER_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/texture2d.h>         /* Texture2D etc. */
#include <a3m/assetloader.h>       /* AssetLoader */

namespace a3m
{
  /** \ingroup a3mAssetloader
   *
   * The 2D Texture Loader is a default AssetLoader for loading Texture2D objects
   * from the file system.  It makes use of the 3rd party STB image library.
   *
   * @{
   */

  /**
   * 2D texture loader based on STB image library.
   */
  class Texture2DLoader : public AssetLoader<Texture2D>
  {
  public:
    /** Destructor.
     */
    virtual ~Texture2DLoader() {}

    // Override
    Texture2D::Ptr load(
        Texture2DCache& cache, /**< Cache to use to create the asset */
        A3M_CHAR8 const* name /**< Name of the asset to load */);
  };

  /** @} */

} /* namespace a3m */

#endif /* A3M_TEXTURE2DLOADER_H */
