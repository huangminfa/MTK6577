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
 *		Name:					wbxml_common.h
 *
 *		Project:				OMC
 *
 *		Created On:				July 2004
 *
 *		Derived From:			//depot/main/base/ssp/da/wbxml_shared.h
 *
 *		Version:				$Id: //depot/main/base/omc/wbxml/wbxml_common.h#1 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2003 - 2004
]*/

/*! \internal
 * \file
 *		Define common WBXML token values and utility functions.
 *
 * \brief	Common WBXML definitions
 */

#ifndef _OMC_WBXML_WBXML_COMMON_H_
#define _OMC_WBXML_WBXML_COMMON_H_

/*===========================================================================*/
/*                    Global Definitions                                     */
/*===========================================================================*/

/* WBXML version identifier bytes */
#define WBXML_V_1_1					0x01
#define WBXML_V_1_2					0x02
#define WBXML_V_1_3					0x03

/* Representation of an unknown public identifier. */
#define UNKNOWN_PUBLIC_IDENTIFIER	0x01

/*
 * MIBenum values for various character encodings - see
 * http://www.iana.org/assignments/character-sets
 */
#define MIB_UNKNOWN 				0x00
#define MIB_US_ASCII 				0x03
#define MIB_ISO_8859_1				0x04
#define MIB_SJIS					0x17
#define MIB_EUC_JP					0x18
#define MIB_UTF_8					0x6A

/*
 * Tag token bits
 */
#define HAS_ATTRIBUTES				0x80
#define HAS_CONTENT					0x40

/*
 * Global WBXML tokens
 */
#define SWITCH_PAGE_TOKEN			0x00
#define END_TOKEN					0x01
#define STR_I_TOKEN					0x03
#define LITERAL_TOKEN				0x04
#define STR_T_TOKEN					0x83

/*
 * Base token for attribute values. In attribute code space, tokens less
 * than AV_BASE represent attribute starts, tokens greater than or equal to
 * AV_BASE represent attribute values.
 */
#define AV_BASE						0x80

/*
 * Attribute value table entry for unused tokens
 */
#define AV_UNUSED					NULL

/*
 * Structure to describe an attribute value prefix or attribute value table
 */
typedef struct
{
	const char * const	*table;			/* pointer to table */
	IU32				tableSize;		/* number of entries in table */
} WBXML_AvTable;


/*===========================================================================*/
/*                    Global Code                                            */
/*===========================================================================*/

/*!
 * Initialise a buffer to the given values.
 *
 * \param	pBuf		The buffer to be initialised.
 * \param	ptr			The buffer's pointer.
 * \param	len			The buffer's length.
 */
extern void WBXML_initBuffer(IBuffer *pBuf, IU8 *ptr, IU32 len);

/*!
 * Duplicate a buffer. The source buffers data is not copied, but shared.
 *
 * \param	pSrc		The buffer to be duplicated.
 * \param	pDst		The buffer to duplicate to.
 */
extern void WBXML_duplicateBuffer(IBuffer *pDst, IBuffer *pSrc);

/*!
 * Offset an IBuffer by the given number of bytes. This includes adjusting the
 * pointer as well as the number of bytes remaining in the buffer.
 *
 * \param	pBuf		The buffer to be offset.
 * \param	off			The number of bytes by which to offset.
 */
extern void WBXML_offsetBuffer(IBuffer *pBuf, IS32 off);

/*!
 * Get the next byte from the buffer, if there are any left. If a byte is taken
 * the buffer's pointer and length are updated accordingly.
 *
 * \param	pBuf	The buffer from which to take the byte.
 * \param	pVal	Pointer to place to put byte.
 *
 * \return	TRUE if the byte has been extracted, FALSE if there were no more to
 *			take.
 */
extern IBOOL WBXML_getIU8(IBuffer *pBuf, IU8 *pVal);

/*!
 * Get a WBXML multibyte integer from the buffer. The buffer's pointer and
 * length will be updated as data is extracted. The function returns FALSE if
 * the buffer runs out of data before the integer is completed (i.e. by a byte
 * without the top bit set).
 *
 * \param	pBuf	The buffer from which to take the integer.
 * \param	pVal	Pointer to place to put integer.
 *
 * \return	TRUE if the integer has been extracted successfully, FALSE if there
 *			was not enough data.
 */
extern IBOOL WBXML_getMBIU32(IBuffer *pBuf, IU32 *pVal);

/*!
 * Get a WBXML inline string from the buffer. The function returns FALSE if the
 * input buffer runs out of data before the string is completed (i.e. by a byte
 * of value zero) or the output buffer runs out of space (if its pointer was
 * non-NULL). If successful, the function updates both buffers' pointer and
 * length values. The terminating byte is not written to the output buffer.
 *
 * If the output buffer's pointer is NULL, no data is copied and the string
 * length is added to the output buffer length. Otherwise, the string is copied,
 * the pointer incremented and the output buffer length decremented by the
 * string length.
 *
 * \param	pbInput		The input buffer from which to take the string.
 * \param	encoding	The character encoding used.
 * \param	pbOutput	The buffer in which to write the string.
 *
 * \return	OMC_ERR_OK if the string has been extracted successfully, or an
 *			OMC error code if there	was not enough data or space.
 */
extern OMC_Error WBXML_getInlineString(IBuffer *pbInput, IU32 encoding,
		IBuffer *pbOutput);

/*!
 * Get a WBXML table string from the buffer. An error is returned if the
 * message or table runs out of data before the string is completed (i.e. by a
 * byte of value zero) or the output buffer runs out of space (if its pointer
 * was non-NULL). If successful the function updates both buffers' pointer
 * and length values. The terminating byte is not written to the output buffer.
 *
 * If the output buffer's pointer is NULL, no data is copied and the string
 * length is added to the output buffer length. Otherwise, the string is copied,
 * the pointer incremented and the output buffer length decremented by the
 * string length.
 *
 * \param	pbInput		The WBXML message containing a string table offset.
 * \param	pTable		The string table.
 * \param	encoding	The character encoding used.
 * \param	pbOutput	The buffer in which to write the string.
 *
 * RETURNS:	OMC_ERR_OK if the string has been extracted successfully, or an
 *			OMC error code if there	was not enough data or space.
 */
extern OMC_Error WBXML_getTableString(IBuffer *pbInput, IBuffer *pTable,
		IU32 encoding, IBuffer *pbOutput);

/*!
 * Get a string content from the message. Content acrual is terminated when any
 * token except a valid attribute value, table or inline string is found, or
 * when there are no more tokens. When reading a tag's content, use 0 for the
 * number of attribute values.
 *
 * If the output buffer's pointer is NULL, the length of the string is added to
 * the output buffer length, otherwise the string's bytes will have been copied
 * starting at the output buffer pointer, the pointer will end up immediately
 * after the final byte of the string and the string length will have been
 * deducted from the output buffer's length.
 *
 * The function returns an error if inline or table strings run out of data
 * before the string is completed or if the output buffer runs out of space.
 * If successful, no terminating byte is written to the output buffer and the
 * token causing termination is left unread.
 *
 * \param	pbMsg		The WBXML message from the attribute value token.
 *						On exit, left at the terminating token.
 * \param	pTable		The string table.
 * \param	encoding	The character encoding used.
 * \param	avTables	Pointer to table of WBXML_AvTable structures: indexing
 *						by code page yields the attribute value string array
 *						for that code page.
 * \param	pCodePage	Pointer to current code page in attribute code space.
 *						This is updated if a code page switch occurs.
 * \param	pbOutput	The buffer in which to write the string.
 *
 * \return	OMC_ERR_OK if the string has been extracted successfully, or an
 *			OMC error code if there	was not enough data or space.
 */
extern OMC_Error WBXML_getString(IBuffer *pbMsg, IBuffer *pTable, IU32 encoding,
		const WBXML_AvTable *avTables, IU8 *pCodePage, IBuffer *pbOutput);

/*!
 * If the output buffer pointer is non-NULL, copy a NUL-terminated string into
 * the buffer, if there is enough space, updating the pointer and space
 * remaining as necessary. Note that the NUL is not copied.
 *
 * If the output buffer pointer is NULL, just add the length of the string (not
 * including the NUL) to the output buffer length.
 *
 * \param	pBuf		The buffer to receive the string.
 * \param	str			String to copy.
 *
 * \return	OMC_ERR_OK if the string was copied or buffer length adjusted
 *			successfully, OMC error code otherwise.
 */
extern OMC_Error WBXML_copyStringToBuffer(IBuffer *pBuf, const char *str);

/*!
 * Decode a WBXML header. If there are any formatting errors, or the message
 * runs out of data before the header is completed, an error code is returned.
 * If all the elements are successfully extracted, OMC_ERR_OK is returned.
 * The string table is not allocated, but set to point into the message buffer.
 *
 * If the function is successful, the message buffer is left pointing at the
 * body of the message. If it is unsuccessful, the buffer's state is
 * indeterminate.
 *
 * \param	pbMsg			The WBXML message.
 * \param	pVersion		Place to put the version byte (note: just a byte,
 * 							not a byte sequenece).
 * \param	pPublicID		Place to put the public ID value.
 * \param	pPublicIDIndex	Place to put the index of the public ID string in
 *							the string table, if the public ID value is zero.
 * \param	pEncoding		Place to put the encoding value.
 * \param	pTable			Will be set to point at the message's string table.
 *
 * \return	OMC_ERR_OK on success, or an OMC error code if header is invalid.
 */
extern OMC_Error
WBXML_decodeHeader(IBuffer *pbMsg, IU8 *pVersion, IU32 *pPublicID,
		IU32 *pPublicIDIndex, IU32 *pEncoding, IBuffer *pTable);

#endif /* !_OMC_WBXML_WBXML_COMMON_H_ */
