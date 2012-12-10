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
 * Resource class
 *
 */
#ifndef A3M_RESOURCE_H
#define A3M_RESOURCE_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/pointer.h>       /* for SharedPtr   */
#include <a3m/noncopyable.h>   /* for NonCopyable */

namespace a3m
{
  namespace detail
  {
    /** \defgroup a3mResource Resources
     * \ingroup  a3mInt
     *
     * A Resource is a graphical resource which can be released on demand.
     * In some situations, such as when a device context switch is about to be
     * performed, it is important to be able to release certain resources even
     * if the objects using them still exist.
     *
     * @{
     */

    /**
     * Defines an interface to be implemented for specific resource types.
     * Implementations must ensure that they set the resource state
     * appropriately.  Resources can either be deallocated or released directly,
     * or can wait to be deallocated or released by an owning object (e.g.
     * ResourceCache).
     */
    class Resource : public Shared, NonCopyable
    {
    public:
      A3M_NAME_SHARED_CLASS( Resource )

      /** Smart pointer type for this class */
      typedef SharedPtr<Resource> Ptr;

      /** Describes the state of a resource */
      enum State
      {
        UNALLOCATED,      /**< Resource has not yet been created */
        ALLOCATED,        /**< Resource has been created */
        DEALLOCATED,      /**< Resource has been created and deleted */
        RELEASED          /**< Resource has been released without deletion */
      };

      /**
       * Virtual destructor
       */
      virtual ~Resource() {}

      /**
       * Deallocates the resource if not released or deallocated already.
       * The resource will only actually be DEALLOCATED if it was previously
       * ALLOCATED, and will become RELEASED otherwise.
       *
       * \return A3M_TRUE if resource was deallocated, or A3M_FALSE if not
       */
      virtual A3M_BOOL deallocate() = 0;

      /**
       * Releases the resource without deallocation.
       * The resource will only become RELEASED if not previously DEALLOCATED,
       * in which case it will remain DEALLOCATED.
       *
       * \return A3M_TRUE if resource was released, or A3M_FALSE if not
       */
      virtual A3M_BOOL release() = 0;

      /**
       * Returns the state of the resource.
       *
       * \return Current state of the resource
       */
      virtual State getState() const = 0;
    };

    /** @} */

  } /* namespace detail */

} /* namespace a3m */

#endif /* A3M_RESOURCE_H */

