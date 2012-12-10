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
 *		Name:					stdlib_if.h
 *
 *		Project:				OMC
 *
 *		Created On:				June 2004
 *
 *		Derived From:			//depot/main/base/ssp/da/stdlib_if.h#6
 *
 *		Version:				$Id: //depot/main/base/omc/stdlib_if.h#2 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2003 - 2004
]*/

/*! \file
 *		Since the porting layer is pure binary in nature, the interface
 *		must be strictly procedural. Thus no platform dependent constants,
 *		defines, additional include files, etc can appear in the interface.
 *
 * \brief
 *		External stdlib API's
 */

#ifndef _OMC_STDLIB_IF_H_
#define _OMC_STDLIB_IF_H_

#ifdef __cplusplus
extern "C" {
#endif


/*!
===============================================================================
 * Convert a NUL terminated byte character string to an integer. This function
 * is only required to handle positive integer decimal or hexadecimal strings.
 * A sign character ('+') is considered to be an illegal character.
 *
 * \param	string		The source string
 *
 * \param	radix		The base to use for conversion (10 decimal, 16 hex)
 *
 * \retval	success		Where to store a success/failure flag. If this
 *						parameter is NULL, success/failure is not returned.
 *
 * \return	The IU32 value
===============================================================================
 */
extern IU32 OMC_atoIU32(const char *string, IU8 radix, IBOOL *success);


/*!
===============================================================================
 * Convert an integer value into a decimal or hexadecimal string
 * representation. The supplied buffer should be large enough to receive the
 * generated string (11 bytes for decimal, 9 bytes for hexadecimal). Note that
 * this function does NOT prepend the value with '0x' for hexadecimal values.
 *
 * \param	value		The integer value to convert
 *
 * \param	radix		The base to use for conversion (10 decimal, 16 hex)
 *
 * \retval	string		Pointer to a buffer to receive the ascii
 *						representation.
===============================================================================
 */
extern void OMC_IU32toa(IU32 value, char *string, IU8 radix);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_STDLIB_IF_H_ */
