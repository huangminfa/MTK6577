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

#ifndef __VT_KAL_DEF__
#define __VT_KAL_DEF__

#ifndef NULL
#define NULL            0
#endif

/***************************************************************************/
/*                         DATATYPE DEFINITIONS                            */
/***************************************************************************/
/* Basic type */
typedef  unsigned short    kal_wchar;
typedef  unsigned char     kal_uint8;
typedef  unsigned short   kal_uint16;
typedef  unsigned int     kal_uint32;

typedef  signed char        kal_int8;
typedef  signed short      kal_int16;   
typedef  signed int        kal_int32;
//typedef  unsigned int     kal_uint32;
//typedef  __int64           kal_int64;
//typedef  unsigned __int64 kal_uint64;
typedef  long long			kal_int64;
typedef  unsigned long long			kal_uint64;
typedef  char               kal_char;
typedef  void*            KAL_ADM_ID;
typedef  unsigned int    module_type;

typedef  void               kal_void;  

typedef enum 
{
  KAL_FALSE = 0,
  KAL_TRUE
} kal_bool;

/***************************************************************************
 ***************************************************************************/
typedef kal_char            KAL_CHAR;
typedef kal_wchar          KAL_WCHAR;
typedef kal_uint8             KAL_U8;
typedef kal_uint16           KAL_U16;
typedef kal_uint32           KAL_U32;
typedef kal_uint64           KAL_U64;

typedef kal_int8              KAL_I8;
typedef kal_int16            KAL_I16;
typedef kal_int32            KAL_I32;
typedef kal_int64            KAL_I64;
typedef kal_bool            KAL_BOOL;

#ifndef ASSERT
extern void mtk_sys_assert(kal_bool a, kal_char *file, kal_uint32 line);
#define ASSERT(exp) do{if(!(exp)) mtk_sys_assert(KAL_FALSE, (kal_char *)__FILE__, __LINE__);} while(0)
#endif
//heshun
#define STATIC_ASSERT(e) typedef char __STATIC_ASSERT__[(e)?1:-1]

#endif
