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
 *		Name:					string_if.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			//depot/main/base/ssp/da/string_if.h#4
 *
 *		Version:				$Id: //depot/main/base/omc/string_if.h#5 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2005
]*/

/*! \file
 *		Since the porting layer is pure binary in nature, the interface
 *		must be strictly procedural. Thus no platform dependent constants,
 *		defines, additional include files etc, can appear in the interface.
 *
 * \brief
 *		External string handling APIs
 */

#ifndef _OMC_STRING_IF_H_
#define _OMC_STRING_IF_H_

#ifdef __cplusplus
extern "C" {
#endif

/*!
================================================================================
 * Calculates the length of a NUL terminated byte character string.
 *
 * \param	string		The string.
 *
 * \return	The length of the string in bytes.
================================================================================
 */
extern IU32 OMC_strlen(const char *string);


/*!
================================================================================
 * Search for a character in a NUL terminated byte character string.
 *
 * \param	string		The string.
 * \param	ch			The character.
 *
 * \return	A pointer to the first occurrence of the character in the string,
 *			or NUL if the character is not found.
================================================================================
 */
extern char *OMC_strchr(const char *string, char ch);


/*!
================================================================================
 * Search for the last occurrence of a character in a NUL terminated byte
 * character string.
 *
 * \param	string		The string.
 * \param	ch			The character.
 *
 * \return	A pointer to the last occurrence of the character in the string,
 *			or NUL if the character is not found.
================================================================================
 */
extern char *OMC_strrchr(const char *string, char ch);


/*!
================================================================================
 * Compares two NUL terminated byte character strings.
 *
 * \param	string1		A string.
 * \param	string2		A string.
 *
 * \retval	  0		if the \a string1 == \a string2
 * \retval	< 0		if \a string1 is lexically less than \a stri < ng2
 * \retval	> 0		if \a string1 is lexically greater than \a string2
================================================================================
 */
extern IS32 OMC_strcmp(const char *string1, const char *string2);


/*!
================================================================================
 * Compares up to \a len characters between the array pointed to by \a string1
 * and the array pointed to by \a string2. Characters that follow a NUL
 * character are not compared.
 *
 * \param	string1		A string.
 * \param	string2		A string.
 * \param	len			Number of characters to compare
 *
 * \retval	  0		if the \a string1 == \a string2
 * \retval	< 0		if \a string1 is lexically less than \a string2
 * \retval	> 0		if \a string1 is lexically greater than \a string2
================================================================================
 */
extern IS32 OMC_strncmp(const char *string1, const char *string2, IU32 len);


/*!
================================================================================
 * Copy a NUL terminated byte character string to the specified destination.
 *
 * \param	dest		The destination location.
 * \param	src			The src string.
 *
 * \return	A pointer to the destination NUL terminated byte character string.
================================================================================
 */
extern char *OMC_strcpy(char *dest, const char *src);


/*!
================================================================================
 * Copy \a len characters from a NUL terminated byte character string to the
 * specified destination.
 *
 * \param	dest		The destination location.
 * \param	src			The src string.
 * \param	len			The number of characters to copy
 *
 * \return	A pointer to the destination character string.
================================================================================
 */
extern char *OMC_strncpy(char *dest, const char *src, IU32 len);


/*!
================================================================================
 * Concatenate a NUL terminated byte character string to the specified NUL
 * terminated byte character string.
 *
 * \param	dest		The destination string.
 * \param	src			The src string to concatenate.
 *
 * \return	A pointer to the destination NUL terminated byte character string.
================================================================================
 */
extern char *OMC_strcat(char *dest, const char *src);


/*!
===============================================================================
 * Compare two NUL terminated byte character strings ignoring case.
 *
 * \param	string1		A string.
 * \param	string2		A string.
 *
 * \return	0 if string1 == string2
 *			< 0 if string1 < string2
 *			> 0 if string1 > string2
===============================================================================
 */
extern IS32 OMC_strcasecmp(const char *string1, const char *string2);

/*!
===============================================================================
 * Compare len characters of two NUL terminated byte character strings ignoring
 * case.
 *
 * \param	string1		A string.
 * \param	string2		A string.
 * \param	len			Number of characters to compare
 *
 * \return	0 if string1 == string2
 *			< 0 if string1 < string2
 *			> 0 if string1 > string2
===============================================================================
 */
extern IS32 OMC_strncasecmp(const char *string1, const char *string2, IU32 len);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_STRING_IF_H_ */
