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
 * ShaderProgramLoader class
 *
 */

#pragma once
#ifndef A3M_SHADERPROGRAMLOADER_H
#define A3M_SHADERPROGRAMLOADER_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/shaderprogram.h>     /* ShaderProgram etc. */
#include <a3m/assetloader.h>       /* AssetLoader */

namespace a3m
{
  /** \ingroup a3mAssetloader
   *
   * Shader Program Loader is a default AssetLoader for loading ShaderProgram
   * objects from the file system.
   *
   * @{
   */

  /**
   * ShaderProgramLoader class.
   * Creates new ShaderPrograms from data read from the file system.
   */
  class ShaderProgramLoader : public AssetLoader<ShaderProgram>
  {
  public:
    /** Destructor.
     */
    virtual ~ShaderProgramLoader() {}

    // Override
    ShaderProgram::Ptr load(
        ShaderProgramCache& cache, /**< Cache to use to create the asset */
        A3M_CHAR8 const* name /**< Name of the asset to load */);
  };

  /** @} */

} /* namespace a3m */

#endif /* A3M_SHADERPROGRAMLOADER_H */
