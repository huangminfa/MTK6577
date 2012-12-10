/*****************************************************************************
 *
 * Copyright (c) 2011 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 *****************************************************************************/
/** \file
 * FontLoader class
 *
 */

#pragma once
#ifndef A3M_FONTLOADER_H
#define A3M_FONTLOADER_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/font.h>              /* Font etc. */
#include <a3m/assetloader.h>       /* AssetLoader */

namespace a3m
{
  /** \ingroup a3mAssetloader
   *
   * Font Loader is a default AssetLoader for loading Font objects
   * from the file system.  It makes use of the 3rd party STB true type library.
   *
   * @{
   */

  /**
   * Font loader based on STB true type library.
   */
  class FontLoader : public AssetLoader<Font>
  {
  public:
    /** Destructor.
     */
    virtual ~FontLoader() {}

    // Override
    Font::Ptr load(
        FontCache& cache, /**< Cache to use to create the asset */
        A3M_CHAR8 const* name /**< Name of the asset to load */);
  };

  /** @} */

} /* namespace a3m */

#endif /* A3M_FONTLOADER_H */
