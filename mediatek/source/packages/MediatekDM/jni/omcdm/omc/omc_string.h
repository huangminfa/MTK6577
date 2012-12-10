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
 *		Name:					omc_string.h
 *
 *		Project:				OMC
 *
 *		Created On:				May 2004
 *
 *		Derived From:			Original
 *
 *		Version:				$Id: //depot/main/base/omc/omc_string.h#7 $
 *
 *		Coding Standards:		3.0
 *
 *		Purpose:
 *
 *			String utility functions.
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2005
]*/


/*! \internal
 * \file
 *		String utility functions
 *
 * \brief	String utility functions
 */


#ifndef _OMC_OMC_STRING_H_
#define _OMC_OMC_STRING_H_

#ifdef __cplusplus
extern "C" {
#endif

/*
 * The UTF8Str type that is used throughout OMC is based upon unsigned char.
 * This means that it is necessary to cast UTF8 string pointers to use the
 * OMC_str* functions without producing warnings. To avoid these explicit
 * casts the following macros have been invented:
 */
#define OMC_utf8len(s)							\
						OMC_strlen((const char *)s)

#define OMC_utf8chr(s, c)							\
			((UTF8Str)	OMC_strchr((const char *)s, c))

#define OMC_utf8rchr(s, c)							\
			((UTF8Str)	OMC_strrchr((const char *)s, c))

#define OMC_utf8casecmp(s1, s2)							\
						OMC_strcasecmp((const char *)s1, (const char *)s2)

#define OMC_utf8cmp(s1, s2)							\
						OMC_strcmp((const char *)s1, (const char *)s2)

#define OMC_utf8ncmp(s1, s2, l)							\
						OMC_strncmp((const char *)s1, (const char *)s2, l)

#define OMC_utf8cpy(d, s)							\
			((UTF8Str)	OMC_strcpy((char *)d, (const char *)s))

#define OMC_utf8ncpy(d, s, l)							\
			((UTF8Str)	OMC_strncpy((char *)d, (const char *)s, l))

#define OMC_utf8cat(d, s)							\
			((UTF8Str)	OMC_strcat((char *)d, (const char *)s))

#define OMC_utf8dup(s)							\
			((UTF8Str)	OMC_strdup((const char *)s))

#ifdef OMC_MEM_DEBUG
extern char * OMC_strdupDB(const char *src, char *file, int line);
#define OMC_strdup(src) OMC_strdupDB(src,__FILE__,__LINE__)
#else
extern char * OMC_strdup(const char *src);
#endif

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_OMC_STRING_H_ */
