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
 * BufferResource class
 *
 */
#ifndef A3M_BUFFERRESOURCE_H
#define A3M_BUFFERRESOURCE_H

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
     * An OpenGL buffer resource.
     */
    class BufferResource : public GlResource
    {
    public:
      /** Smart pointer type for this class */
      typedef SharedPtr<BufferResource> Ptr;

    private:
      // Override
      A3M_UINT32 doAllocate();

      // Override
      void doDeallocate(A3M_UINT32 id);
    };

  } /* namespace detail */

} /* namespace a3m */

#endif /* A3M_BUFFERRESOURCE_H */

