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
 * Texture Cube Loader class
 *
 */

#pragma once
#ifndef A3M_TEXTURECUBELOADER_H
#define A3M_TEXTURECUBELOADER_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/texturecube.h>       /* TextureCube etc. */
#include <a3m/assetloader.h>       /* AssetLoader */

namespace a3m
{
  /** \ingroup a3mAssetloader
   *
   * Texture Cube Loader is a default AssetLoader for loading TextureCube
   * objects from the file system.  It makes use of the 3rd party STB image
   * library.
   *
   * @{
   */

  /**
   * Cube texture loader based on STB image library.
   */
  class TextureCubeLoader : public AssetLoader<TextureCube>
  {
  public:
    /** Destructor.
     */
    virtual ~TextureCubeLoader() {}

    // Override
    TextureCube::Ptr load(
        TextureCubeCache& cache, /**< Cache to use to create the asset */
        A3M_CHAR8 const* name /**< Name of the asset to load */);
  };

  /** @} */

} /* namespace a3m */

#endif /* A3M_TEXTURECUBELOADER_H */
