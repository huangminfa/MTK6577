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
 * TextureResource class
 *
 */
#ifndef A3M_TEXTURERESOURCE_H
#define A3M_TEXTURERESOURCE_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/pointer.h>                /* for SharedPtr   */
#include <a3m/detail/glresource.h>      /* for GlResource */

namespace a3m
{
  namespace detail
  {
    /**
     * \ingroup  a3mResource
     * An OpenGL texture resource.
     */
    class TextureResource : public GlResource
    {
    public:
      /** Default constructor. */
      TextureResource() {}

      /** Constructor to wrap an existing OpenGL texture. */
      explicit TextureResource(A3M_UINT32 id /**< Integer ID of GL resource */);

      /** Smart pointer type for this class */
      typedef SharedPtr<TextureResource> Ptr;

    private:
      // Override
      A3M_UINT32 doAllocate();

      // Override
      void doDeallocate(A3M_UINT32 id);
    };

  } /* namespace detail */

} /* namespace a3m */

#endif /* A3M_TEXTURERESOURCE_H */

