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

/***********************************************************************
Filename   : rvads.h
Description: config file ARM ADS compiler tools
************************************************************************
        Copyright (c) 2001 RADVISION Inc. and RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Inc. and RADVISION Ltd.. No part of this document may be
reproduced in any form whatsoever without written prior approval by
RADVISION Inc. or RADVISION Ltd..

RADVISION Inc. and RADVISION Ltd. reserve the right to revise this
publication and make changes without obligation to notify any person of
such revisions or changes.
***********************************************************************/
#ifndef RV_ADS_H
#define RV_ADS_H

#include <limits.h>
#include <string.h>
#include "kal_release.h"
/* Possible type definitions. Used only for mapping into rvtypes */
/* definitions, not for use outside of this file. */
#define RV_SIZET_TYPE             size_t
#define RV_PTRDIFFT_TYPE          ptrdiff_t
#define RV_CHARACTER_TYPE         char
#define RV_SIGNED_CHAR_TYPE       signed char
#define RV_UNSIGNED_CHAR_TYPE     unsigned char
#define RV_SIGNED_SHORT_TYPE      signed short
#define RV_UNSIGNED_SHORT_TYPE    unsigned short
#define RV_SIGNED_INT_TYPE        signed int
#define RV_UNSIGNED_INT_TYPE      unsigned int
#define RV_SIGNED_LONG_TYPE       signed long
#define RV_UNSIGNED_LONG_TYPE     unsigned long
#define RV_SIGNED_LONGLONG_TYPE   kal_int64
#define RV_UNSIGNED_LONGLONG_TYPE kal_uint64

/* Corresponding macros for attaching proper type suffixes to */
/* constants Used only for mapping into rvtypes definitions, not */
/* Outside of this file. */
#define RV_SIGNED_CHAR_SUFFIX(_n)       _n
#define RV_UNSIGNED_CHAR_SUFFIX(_n)     _n
#define RV_SIGNED_SHORT_SUFFIX(_n)      _n
#define RV_UNSIGNED_SHORT_SUFFIX(_n)    _n ##U
#define RV_SIGNED_INT_SUFFIX(_n)        _n
#define RV_UNSIGNED_INT_SUFFIX(_n)      _n ## U
#define RV_SIGNED_LONG_SUFFIX(_n)       _n ## L
#define RV_UNSIGNED_LONG_SUFFIX(_n)     _n ## UL
#define RV_SIGNED_LONGLONG_SUFFIX(_n)   _n ## LL
#define RV_UNSIGNED_LONGLONG_SUFFIX(_n) _n ## ULL

/* Provide type and suffix mappings for specific bit models. */
/* See rvtypes.h for requred definitions. These definitionions */
/* are only for use by the rvtypes.h file. */
#define RV_CHAR_TYPE           RV_CHARACTER_TYPE
#define RV_VAR_INT_TYPE        RV_SIGNED_INT_TYPE
#define RV_VAR_UINT_TYPE       RV_UNSIGNED_INT_TYPE
#define RV_VAR_LONG_TYPE       RV_SIGNED_LONG_TYPE
#define RV_VAR_ULONG_TYPE      RV_UNSIGNED_LONG_TYPE
#define RV_SIGNED_INT8_TYPE    RV_SIGNED_CHAR_TYPE
#define RV_UNSIGNED_INT8_TYPE  RV_UNSIGNED_CHAR_TYPE
#define RV_SIGNED_INT16_TYPE   RV_SIGNED_SHORT_TYPE
#define RV_UNSIGNED_INT16_TYPE RV_UNSIGNED_SHORT_TYPE
#define RV_SIGNED_INT32_TYPE   RV_SIGNED_INT_TYPE
#define RV_UNSIGNED_INT32_TYPE RV_UNSIGNED_INT_TYPE
#define RV_SIGNED_INT64_TYPE   RV_SIGNED_LONGLONG_TYPE
#define RV_UNSIGNED_INT64_TYPE RV_UNSIGNED_LONGLONG_TYPE

#define RV_VAR_INT_SUFFIX        RV_SIGNED_INT_SUFFIX
#define RV_VAR_UINT_SUFFIX       RV_UNSIGNED_INT_SUFFIX
#define RV_SIGNED_INT8_SUFFIX    RV_SIGNED_CHAR_SUFFIX
#define RV_UNSIGNED_INT8_SUFFIX  RV_UNSIGNED_CHAR_SUFFIX
#define RV_SIGNED_INT16_SUFFIX   RV_SIGNED_SHORT_SUFFIX
#define RV_UNSIGNED_INT16_SUFFIX RV_UNSIGNED_SHORT_SUFFIX
#define RV_SIGNED_INT32_SUFFIX   RV_SIGNED_INT_SUFFIX
#define RV_UNSIGNED_INT32_SUFFIX RV_UNSIGNED_INT_SUFFIX
#define RV_SIGNED_INT64_SUFFIX   RV_SIGNED_LONGLONG_SUFFIX
#define RV_UNSIGNED_INT64_SUFFIX RV_UNSIGNED_LONGLONG_SUFFIX

/* Since RvInt and RvUint are variable size types, min and */
/* max must be determined for them. */
//#define INT_MAX 2147483647
//#define INT_MIN -2147483648
//#define UINT_MAX 4294967296
#define RV_VAR_INT_MAX  INT_MAX 
#define RV_VAR_INT_MIN  INT_MIN
#define RV_VAR_UINT_MAX UINT_MAX

#endif /* RV_ADS_H */
