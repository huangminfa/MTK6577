/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/*[
 *		Name:					omc_alloc.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			//depot/main/base/ssp/ssp_alloc.h#2
 *
 *		Version:				$Id: //depot/main/base/omc/omc_alloc.h#5 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2005
]*/

/*! \internal
 * \file
 *			Defines a wrapper round the external memory allocation APIs
 *			required by OMC.
 *
 *			This wrapper is used to enable the core codes use of memory to
 *			be tracked, memory-leaks and/or memory scribbles to be detected
 *			and memory errors to be programmatically generated for testing
 *			purposes.
 *
 * \brief	Memory allocation APIs
 */

#ifndef _OMC_OMC_ALLOC_H_
#define _OMC_OMC_ALLOC_H_

#ifdef __cplusplus
extern "C" {
#endif

/*!
 * Macro to free a non-NULL pointer
 */
#define OMC_freeIfNotNull(a)				\
	do {									\
		if (NULL != (a))					\
		{									\
			OMC_free((a));					\
			(a) = NULL;						\
		}									\
	} while (0)


/*!
===============================================================================
 * RETURNS:	Character string for the comand line usage of the memory debugging
 *			facility.
===============================================================================
 */
extern const char* OMC_memDebugUsage(void);


/*
 * Define this at the start of omc_if.h to enable some simple memory debugging
 * #define OMC_MEM_DEBUG
 */

/*
 * Routine used to convert an address of allocated into a 'real' address
 * which could be used directly with OMC_MEM_Free(). This should only be used
 * if memory must be registered with the OS for free-ing in certain situations.
 */

/*!
===============================================================================
 * Takes a ptr to memory and determines the real address of the allocated
 * memory when OMC_MEM_DEBUG has been defined. This should only be used
 * if memory must be registered with the OS for free-ing in certain situations
 * rather than being freed via OMC_free();
 *
 * \param	ptr		A memory address
 *
 * \return	The real address of the allocated memory, suitably adjusted.
===============================================================================
 */

 #ifdef OMC_MEM_DEBUG
 extern void* OMC_memAddr(void* ptr);

 #else /* !OMC_MEM_DEBUG */

 #define OMC_memAddr(ptr) (ptr)

 #endif /* OMC_MEM_DEBUG */


/*!
===============================================================================
 * Allocates an area of uninitialised memory for use by the Download Agent.
 * The area allocated can be freed using OMC_free().
 *
 * \param	size		Number of bytes to allocate.
 *
 * \return	Pointer to allocated block of data of the specified size, or NULL
 * 			if the allocation can not be satisfied.
===============================================================================
 */
#ifdef OMC_MEM_DEBUG
extern void *OMC_dbgMalloc (IU32 size, char* file, int line);

 /* Use define to allow tracking of caller locations */
 #ifdef PROD_MIN
  #define OMC_malloc(size) (OMC_dbgMalloc(size, NULL, 0))
 #else
  #define OMC_malloc(size) (OMC_dbgMalloc(size, __FILE__, __LINE__))
 #endif
#else /* OMC_MEM_DEBUG */

#define OMC_malloc(size)	(OMC_MEM_alloc(size))

#endif /* OMC_MEM_DEBUG */


/*!
===============================================================================
 * Frees an area of memory previously allocated by OMC_malloc().
 *
 * \param	p			Pointer to memory to free.
===============================================================================
 */
#ifdef OMC_MEM_DEBUG
extern void OMC_dbgFree(const void *p, char* file, int line);

 /* Use define to allow tracking of caller locations */
 #ifdef PROD_MIN
  #define OMC_free(p) (OMC_dbgFree(p, NULL, 0))
 #else
  #define OMC_free(p) (OMC_dbgFree(p, __FILE__, __LINE__))
 #endif
#else /* OMC_MEM_DEBUG */

#define OMC_free(p) (OMC_MEM_free(p))

#endif /* OMC_MEM_DEBUG */


#ifdef OMC_MEM_DEBUG


/*!
===============================================================================
 * Called when about to exit/reboot. Verifies that all allocated memory has
 * been freed. Dumps out some simple memory statistics.
===============================================================================
 */
extern void OMC_memoryFinished(void);

/*!
===============================================================================
 * Called on startup (if the appropriate cmd-line flag was present) to define
 * specific settings for testing.
 *
 * This function is only required on debug builds which have enabled the
 * internal OMC memory debug features.
 *
 * \param	str		Pointer to a string containing the memory debug
 *					requirements specified via a command-line or other
 *					suitable method.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Error OMC_memSetFailureData(const char *str);

#endif /* OMC_MEM_DEBUG */


#ifdef __cplusplus
} /* extern "C" */
#endif


#endif /* _OMC_OMC_ALLOC_H_ */
