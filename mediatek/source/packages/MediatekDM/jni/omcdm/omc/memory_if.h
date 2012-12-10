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
 *		Name:					memory_if.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			//depot/main/base/ssp/memory_if.h#3
 *
 *		Version:				$Id: //depot/main/base/omc/memory_if.h#3 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004
]*/

/*! \file
 *		Defines the external memory handling APIs required by the OMC agent.
 *
 *		Since the porting layer is pure binary in nature, the interface
 *		must be strictly procedural. Thus no platform dependent constants,
 *		defines, additional include files etc can appear in the interface.
 *
 * \brief	Memory handling API
 */

#ifndef _OMC_MEMORY_IF_H_
#define _OMC_MEMORY_IF_H_

#ifdef __cplusplus
extern "C" {
#endif


/*!
===============================================================================
 * Compare two buffers of bytes.
 *
 * \param	buf1		A buffer.
 * \param	buf2		Another buffer.
 * \param	length		Number of bytes to compare.
 *
 * \retval	Negative if \a buf1 < \a buf2
 * \retval	Zero if \a buf1 == \a buf2
 * \retval	Positive if \a buf1 > \a buf2
===============================================================================
 */
extern int OMC_memcmp(const void *buf1, const void *buf2, IU32 length);


/*!
===============================================================================
 * Copies bytes from one place to another. The source and destination bytes
 * must not overlap.
 *
 * \param	dst			Destination pointer.
 * \param	src			Source pointer.
 * \param	length		Number of bytes to copy.
===============================================================================
 */
extern void OMC_memcpy(void *dst, const void *src, IU32 length);


/*!
===============================================================================
 * Copies bytes from one place to another. The source and destination bytes
 * may overlap.
 *
 * \param	dst			Destination pointer.
 * \param	src			Source pointer.
 * \param	length		Number of bytes to copy.
===============================================================================
 */
extern void OMC_memmove(void *dst, const void *src, IU32 length);


/*!
===============================================================================
 * Set a byte sequence to a common value.
 *
 * \param	dst			Pointer to first byte in memory to be set.
 * \param	val			Value to which bytes are set.
 * \param	length		Number of bytes to set.
===============================================================================
 */
extern void OMC_memset(void *dst, IU8 val, IU32 length);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_MEMORY_IF_H_ */
