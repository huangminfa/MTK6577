/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2008
*
*  BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
*  THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
*  RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON
*  AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
*  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
*  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
*  NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
*  SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
*  SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH
*  THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
*  NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S
*  SPECIFICATION OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
*
*  BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
*  LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
*  AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
*  OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY BUYER TO
*  MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
*
*  THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE
*  WITH THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF
*  LAWS PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING THEREOF AND
*  RELATED THERETO SHALL BE SETTLED BY ARBITRATION IN SAN FRANCISCO, CA, UNDER
*  THE RULES OF THE INTERNATIONAL CHAMBER OF COMMERCE (ICC).
*
*****************************************************************************/

// -*- c++ -*-
// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

//                  O S C L _ P M E M

// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

/*! \addtogroup osclmemory OSCL Memory
 *
 * @{
 */


/*! \file oscl_pmem.h
    \brief This file contains the definition of physical continuous memory functions
*/

#ifndef OSCL_PMEM_H_INCLUDED
#define OSCL_PMEM_H_INCLUDED

#ifndef OSCL_TYPES_H_INCLUDE
#include "oscl_types.h"
#endif

#include "../../../../../../frameworks/base/media/libstagefright/pmem_util/pmem_util.h"

/*
#define INVALID_FD        -1   //!<: invalid file descriptor

struct PmemInfo{
    int    fd;          //!<: master file description used to mmap memory
    int    shared_fd;   //!<: shared fd, used for IPC
    void*  base;        //!<: address of pmem chunk
    size_t size;        //!<: size of this memory chunk
    int    offset;      //!<: distance between virtual address and base
};


//!<: External pmem information
struct ExPmemInfo{
    int    shared_fd;   //!<: shared fd, used for IPC
    void*  base;        //!<: virtual address of pmem chunk
    int    offset;		//!<: offset of this sub-chunk
    size_t size;        //!<: size of this sub-chunk
};
*/

/**
 * Allocates a memory block which is continuous in physical memory.
 * @param aSize  number of bytes to allocate
 * @return a void pointer to the allocated space, or NULL if there is insufficient
 *         memory available.
 */
OSCL_IMPORT_REF void* oscl_pmem_alloc(size_t aSize);


/**
 * Deallocates or frees a memory block which is continuous in physical memory.
 * @param aPtr  pointer to previously allocated memory block which is allocated by oscl_pmem_alloc().
 */
OSCL_IMPORT_REF void  oscl_pmem_free(void* aPtr);


/**
 * Convert the address of a memory block from virtual address to physical address.
 * @param aVirPtr  pointer to previously allocated memory block which is allocated by oscl_pmem_alloc().
 * @return a void pointer contains the physical address, or NULL if aVirPtr is invalid.
 */
OSCL_IMPORT_REF void* oscl_pmem_vir2phy(void* aVirPtr);


/**
 * Get complete pmem information associated to the address
 * @param aVirPtr  pointer to previously allocated memory block which is allocated by oscl_pmem_alloc().
 * @param info     pointer to PmemInfo, which contains the output information.
 * @return true if succeed; otherwise, false is returned.
 */
OSCL_IMPORT_REF bool oscl_pmem_get_info(void* aPtr, PmemInfo* info);


/**
 * Register the pmem chunk, which is allocated by another process
 * @param info     pointer to ExPmemInfo.
 * @return true if succeed; otherwise, false is returned.
 */
OSCL_IMPORT_REF bool oscl_pmem_register(ExPmemInfo* info);


/**
 * Unregister the pmem chunk, which is allocated by another process
 * @param info     pointer to ExPmemInfo.
 * @return true if succeed; otherwise, false is returned.
 */
OSCL_IMPORT_REF bool oscl_pmem_unregister(ExPmemInfo* info);


/**
 * Shoe the pmem sAddrMap
 * @param void
 * @return true if succeed; otherwise, false is returned.
 */
OSCL_IMPORT_REF bool oscl_pmem_show_addr_map(void);

/**
 * Map the pmem into non-cached
 * @param aVirPtr  pointer to previously allocated memory block which is allocated by oscl_pmem_alloc().
 * @return true if succeed; otherwise, false is returned.
 */
OSCL_IMPORT_REF bool oscl_pmem_map_into_noncached(void* aVirPtr);

#endif


/*! @} */
