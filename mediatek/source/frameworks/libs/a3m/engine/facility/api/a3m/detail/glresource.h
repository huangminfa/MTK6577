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
 * GlResource class
 *
 */
#ifndef A3M_GLRESOURCE_H
#define A3M_GLRESOURCE_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/detail/resource.h>   /* for Resource */

namespace a3m
{
  namespace detail
  {
    /**
     * \ingroup  a3mResource
     * A base class for OpenGL resources.
     */
    class GlResource : public Resource
    {
    public:
      /**
       * Constructor
       */
      GlResource();

      /**
       * Constructor to wrap an existing OpenGL resource.
       */
      explicit GlResource(A3M_UINT32 id /**< Integer ID of GL resource */);

      /**
       * Destructor
       */
      ~GlResource();

      // Override
      A3M_BOOL deallocate();

      // Override
      A3M_BOOL release();

      // Override
      State getState() const { return m_state; }

      /**
       * Allocates an OpenGL resource.
       * Allocation can only occur if the resource was previously UNALLOCATED.
       *
       * \return A3M_TRUE if successful, A3M_FALSE if state is not UNALLOCATED,
       * or allocation failed
       */
      A3M_BOOL allocate();

      /**
       * Returns the allocated OpenGL texture's name.
       *
       * \return Texture name if resource state is ALLOCATED, otherwise zero.
       */
      A3M_UINT32 getId() { return m_id; }

    private:
      /**
       * Performs actual allocation of resource.
       * This function must be implemented in concrete resources.
       *
       * \return Integer ID of OpenGL resource.
       */
      virtual A3M_UINT32 doAllocate() = 0;

      /**
       * Performs actual deallocation of resource.
       * This function must be implemented in concrete resources.
       */
      virtual void doDeallocate(A3M_UINT32 id
                                /**< Integer ID of GL resource */) = 0;

      State m_state;
      A3M_UINT32 m_id;
    };

  } /* namespace detail */

} /* namespace a3m */

#endif /* A3M_GLRESOURCE_H */

