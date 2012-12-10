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
 *		Name:					sha1.h
 *
 *		Project:				OMC
 *
 *		Created On:				June 2004
 *
 *		Derived From:			RFC3174
 *
 *		Version:				$Id: //depot/main/base/omc/lib/sha1/sha1.h#2 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004
]*/

/*! \internal
 * \file
 *		This is the header file for code which implements the Secure
 *		Hashing Algorithm 1 as defined in FIPS PUB 180-1 published
 *		April 17, 1995.
 *
 * \brief	SHA-1
 */

#ifndef _SHA1_H_
#define _SHA1_H_

/*
 * If you do not have the ISO standard stdint.h header file, then you
 * must typdef the following:
 *	  name			   meaning
 *	uint32_t		unsigned 32 bit integer
 *	uint8_t			unsigned 8 bit integer (i.e., unsigned char)
 *	int_least16_t	integer of >= 16 bits
 */
typedef IU32	uint32_t;
typedef IU8		uint8_t;
typedef IS16	int_least16_t;

#define SHA_DIGESTSIZE	20
#define SHA_BLOCKSIZE	64

/*
 *	This structure will hold context information for the SHA-1
 *	hashing operation
 */
typedef struct
{
	uint32_t Intermediate_Hash[SHA_DIGESTSIZE/4];	/* Message Digest */
	uint32_t Length_Low;					/* Message length in bits */
	uint32_t Length_High;					/* Message length in bits */
	int_least16_t Message_Block_Index;		/* Index into message block array */
	uint8_t Message_Block[SHA_BLOCKSIZE];	/* 512-bit message blocks */
} SHA_CTX;

/*
 *	Function Prototypes
 */
void SHAInit(SHA_CTX *);
void SHAUpdate(SHA_CTX *, const uint8_t *, unsigned int);
void SHAFinal(uint8_t [SHA_DIGESTSIZE], SHA_CTX *);
void HMAC_SHA(const IU8 *key, IU32 keyLen, const IU8 *data, IU32 dataLen,
			  IU8 outBuf[SHA_DIGESTSIZE]);

#endif /* _SHA1_H */
