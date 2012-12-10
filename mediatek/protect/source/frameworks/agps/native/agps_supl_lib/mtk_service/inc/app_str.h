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

/*****************************************************************************
BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES THAT
THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE") RECEIVED
FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON AN "AS-IS"
BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT. NEITHER
DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE SOFTWARE OF
ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR SUPPLIED WITH THE
MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY
WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR
ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S SPECIFICATION OR TO CONFORM TO
A PARTICULAR STANDARD OR OPEN FORUM.

BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE, AT
MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE, OR
REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY BUYER TO MEDIATEK
FOR SUCH MEDIATEK SOFTWARE AT ISSUE.

THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE WITH
THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF LAWS
PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING THEREOF AND RELATED
THERETO SHALL BE SETTLED BY ARBITRATION IN SAN FRANCISCO, CA, UNDER THE RULES
OF THE INTERNATIONAL CHAMBER OF COMMERCE (ICC).
*****************************************************************************/

#ifndef _APP_STR_H_
#define _APP_STR_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "typedef.h"
#include "mtk_agps_def.h"


kal_uint16 app_ucs2_str_to_asc_str(kal_int8 *pOutBuffer, kal_int8 *pInBuffer);
void smsal_gsm7_unpack( kal_uint8  *old_data_ptr,
                        kal_uint8  *new_data_ptr,
                        kal_uint16      data_len,
                        kal_uint16        offset );

int smsal_gsm7_unpack_to_char_array(kal_uint8  *packed_data,
                       int packed_len,
                       kal_uint8  *unpacked_data,
                       int max_unpack_len,
                       UINT offset);

int smsal_gsm7_unpack_to_unicode_array(kal_uint8  *packed_data,
                       int packed_len,
                       kal_uint16  *unpacked_data,
                       int max_unpack_len,
                       UINT offset);

extern kal_uint8 app_unicode_to_ucs2encoding(kal_wchar unicode, kal_uint8 *charLength, kal_uint8 *arrOut);
extern kal_uint8 app_ucs2encoding_to_unicode(kal_wchar *pUnicode, kal_uint8 *arrOut);
extern kal_int32 app_ucs2_strlen(const kal_int8 *arrOut);
extern kal_int32 app_ucs2_strcmp(const kal_int8 *string1, const kal_int8 *string2);
extern kal_int32 app_ucs2_unconditional_strcmp(const kal_int8 *string1, const kal_int8 *string2);
extern kal_int8 *app_ucs2_strcpy(kal_int8 *strDestination, const kal_int8 *strSource);

extern kal_int32 app_ucs2_strncmp(const kal_int8 *string1, const kal_int8 *string2, kal_uint32 count);
extern kal_int8 *app_ucs2_strncpy(kal_int8 *strDestination, const kal_int8 *strSource, kal_uint32 size);
extern kal_int8 *app_ucs2_strcat(kal_int8 *strDestination, const kal_int8 *strSource);
extern kal_int8 *app_ucs2_strncat(kal_int8 *strDestination, const kal_int8 *strSource, kal_uint32 size);


extern kal_uint16 app_asc_str_to_ucs2_str(kal_int8 *pOutBuffer, kal_int8 *pInBuffer);
extern kal_uint16 app_asc_str_n_to_ucs2_str(kal_int8 *pOutBuffer, kal_int8 *pInBuffer, kal_uint32 len);
extern kal_uint16 app_ucs2_str_to_asc_str(kal_int8 *pOutBuffer, kal_int8 *pInBuffer);
extern kal_uint16 app_ucs2_str_n_to_asc_str(kal_int8 *pOutBuffer, kal_int8 *pInBuffer, kal_uint32 len);
extern kal_int32 app_unicode_to_utf8_len(kal_wchar ucs2);
extern kal_uint16 app_asc_str_to_ucs2_str_ex(kal_int8 *pOutBuffer, kal_int8 *pInBuffer);
extern kal_uint16 app_asc_str_n_to_ucs2_str_ex(kal_int8 *pOutBuffer, kal_int8 *pInBuffer, kal_uint32 len);


extern kal_int32 app_stricmp(kal_char *s, kal_char *t);
extern kal_int32 app_strnicmp(kal_char *s, kal_char *t, int n);
extern kal_char *app_strtolower(kal_char *s);

extern kal_int32 app_log2(kal_uint32 i);
extern kal_int32 app_log10(kal_uint32 i);
extern kal_int32 app_intdigits(kal_uint32 i);
extern kal_wchar *app_ucs2_strstr(const kal_wchar *a, const kal_wchar *p);
extern kal_int8* app_ucs2_tolower(kal_int8 *c);
extern kal_int8* app_ucs2_toupper(kal_int8 *c);
extern kal_wchar *app_ucs2_strtolower(kal_wchar *s);

extern kal_int8 *app_ucs2_strchr(const kal_int8 *string,  kal_wchar ch);
extern kal_int8 *app_ucs2_strrchr(const kal_int8 * string, kal_wchar ch);
extern kal_int8 *app_ucs2_strupr(kal_int8 *string);
extern kal_int8 *app_ucs2_strlwr(kal_int8 *string);
extern kal_int32 app_ucs2_stricmp(const kal_int8 *str_src, const kal_int8 *str_dst);
extern kal_int32 app_ucs2_strnicmp(const kal_int8 *str_src, 
                            const kal_int8 *str_dst, 
                            kal_uint32 count);
extern kal_uint16 app_asc_str_to_ucs2_wcs(kal_wchar *pOutBuffer, kal_int8 *pInBuffer);
extern kal_uint16 app_asc_str_n_to_ucs2_wcs(kal_wchar *pOutBuffer, kal_int8 *pInBuffer, kal_uint32 len);
extern kal_uint16 app_asc_str_to_ucs2_wcs_ex(kal_wchar *dest, kal_int8 *src, kal_uint32 *src_end_pos);
extern kal_uint16 app_asc_str_n_to_ucs2_wcs_ex(kal_wchar *dest, kal_int8 *src, kal_uint32 len, kal_uint32 *src_end_pos);
extern kal_uint16 app_ucs2_wcs_to_asc_str(kal_int8 *pOutBuffer, kal_wchar *pInBuffer);
extern kal_uint16 app_ucs2_wcs_n_to_asc_str(kal_int8 *pOutBuffer, kal_wchar *pInBuffer, kal_uint32 len);

extern kal_wchar app_ucs2_towupper(kal_wchar wc);
extern kal_wchar app_ucs2_towlower(kal_wchar wc);
extern kal_int32 app_ucs2_wcslen(const kal_wchar *string);
extern kal_int32 app_ucs2_wcscmp(const kal_wchar *str_src, const kal_wchar *str_dst);
extern kal_int32 app_ucs2_wcsncmp(const kal_wchar *str_src, 
                           const kal_wchar *str_dst, 
                           kal_uint32 count);
extern kal_wchar *app_ucs2_wcscpy(kal_wchar *str_dst, const kal_wchar *str_src);
extern kal_wchar *app_ucs2_wcsncpy(kal_wchar *str_dst, const kal_wchar *str_src, kal_uint32 count);
extern kal_wchar *app_ucs2_wcscat(kal_wchar *str_dst, const kal_wchar *str_src);
extern kal_wchar *app_ucs2_wcsncat(kal_wchar *str_dst, const kal_wchar *str_src, kal_uint32 count);
extern kal_wchar *app_ucs2_wcsstr(const kal_wchar *string, const kal_wchar *str_char_set);
extern kal_wchar *app_ucs2_wcschr(const kal_wchar *string,  kal_wchar ch);
extern kal_wchar *app_ucs2_wcsrchr(const kal_wchar * string, kal_wchar ch);
extern kal_wchar *app_ucs2_wcslwr(kal_wchar *string);
extern kal_wchar *app_ucs2_wcsupr(kal_wchar *string);
extern kal_int32 app_ucs2_wcsicmp(const kal_wchar *str_src, const kal_wchar *str_dst);
extern kal_int32 app_ucs2_wcsnicmp(const kal_wchar *str_src, 
                            const kal_wchar *str_dst, 
                            kal_uint32 count);

extern kal_int32 app_ucs2_atoi(const kal_int8 *nptr);
extern kal_int32 app_ucs2_wtoi(const kal_wchar *nptr);
extern kal_int8 *app_ucs2_itoa (kal_int32 val, kal_int8 *buf, kal_int32 radix);
extern kal_wchar *app_ucs2_itow (kal_int32 val, kal_wchar *buf, kal_int32 radix);

#ifdef __cplusplus
}
#endif

#endif