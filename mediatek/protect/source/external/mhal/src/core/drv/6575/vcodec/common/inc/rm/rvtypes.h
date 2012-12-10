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

/* ***** BEGIN LICENSE BLOCK ***** 
 * Version: RCSL 1.0 and Exhibits. 
 * REALNETWORKS CONFIDENTIAL--NOT FOR DISTRIBUTION IN SOURCE CODE FORM 
 * Portions Copyright (c) 1995-2002 RealNetworks, Inc. 
 * All Rights Reserved. 
 * 
 * The contents of this file, and the files included with this file, are 
 * subject to the current version of the RealNetworks Community Source 
 * License Version 1.0 (the "RCSL"), including Attachments A though H, 
 * all available at http://www.helixcommunity.org/content/rcsl. 
 * You may also obtain the license terms directly from RealNetworks. 
 * You may not use this file except in compliance with the RCSL and 
 * its Attachments. There are no redistribution rights for the source 
 * code of this file. Please see the applicable RCSL for the rights, 
 * obligations and limitations governing use of the contents of the file. 
 * 
 * This file is part of the Helix DNA Technology. RealNetworks is the 
 * developer of the Original Code and owns the copyrights in the portions 
 * it created. 
 * 
 * This file, and the files included with this file, is distributed and made 
 * available on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER 
 * EXPRESS OR IMPLIED, AND REALNETWORKS HEREBY DISCLAIMS ALL SUCH WARRANTIES, 
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS 
 * FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT. 
 * 
 * Technology Compatibility Kit Test Suite(s) Location: 
 * https://rarvcode-tck.helixcommunity.org 
 * 
 * Contributor(s): 
 * 
 * ***** END LICENSE BLOCK ***** */ 

/*/////////////////////////////////////////////////////////////////////////// */
/*    RealNetworks, Inc. Confidential and Proprietary Information. */
/* */
/*    Copyright (c) 1995-2002 RealNetworks, Inc. */
/*    All Rights Reserved. */
/* */
/*    Do not redistribute. */
/* */
/*/////////////////////////////////////////////////////////////////////////// */
/*////////////////////////////////////////////////////////// */
/* */
/*    INTEL Corporation Proprietary Information */
/* */
/*    This listing is supplied under the terms of a license */
/*    agreement with INTEL Corporation and may not be copied */
/*    nor disclosed except in accordance with the terms of */
/*    that agreement. */
/* */
/*    Copyright (c) 1997 - 2001 Intel Corporation. */
/*    All Rights Reserved. */
/* */
/*////////////////////////////////////////////////////////// */

#ifndef RVTYPES_H__
#define RVTYPES_H__

/* $Header: /cvsroot/rarvcode-video/codec/rv89combo/rv89combo_c/cdeclib/rvtypes.h,v 1.3 2003/02/14 00:40:37 karll Exp $ */

/* This file defines the fundamental types used by the HIVE/RV interfaces. */
/* These types are intended to be portable across a wide variety of */
/* compilation environments. */
/* */
/* The following identifiers define scalar data types having a known size */
/* and known range of values, regardless of the host compiler. */
/* */
/* Name     Size     Comments      Range of Values */
/* -------+--------+-------------+-------------------------- */
/* U8        8 bits  unsigned                0 ..        255 */
/* I8        8 bits  signed               -128 ..        127 */
/* U16      16 bits  unsigned                0 ..      65535 */
/* I16      16 bits  signed             -32768 ..      32767 */
/* U32      32 bits  unsigned                0 .. 4294967295 */
/* I32      32 bits  signed        -2147483648 .. 2147483647 */
/* */
/* Bool8     8 bits  boolean                 0 ..   non-zero */
/* Bool16   16 bits  boolean                 0 ..   non-zero */
/* Bool32   32 bits  boolean                 0 ..   non-zero */
/* */
/* Enum32   32 bits  enumeration   -2147483648 .. 2147483647 */
/* */
/* F32      32 bits  floating point */
/* F64      64 bits  floating point */
/* */
/*          NOTE:  floating point representations are compiler specific */
/* */
/* The following identifiers define scalar data types whose size is */
/* compiler specific.  They should only be used in contexts where size */
/* is not relevant. */
/* */
/* RV_Boolean       boolean                 0 ..   non-zero */
/* */
/* */
/* The following pointers to the above types are also defined. */
/* */
/* PU8      PI8     PBool8      PEnum32 */
/* PU16     PI16    PBool16 */
/* PU32     PI32    PBool32 */
/* PF32     PF64    PBoolean */
/* */
/* */
/* The following macros are defined to support compilers that provide */
/* a variety of calling conventions.  They expand to nothing (i.e., empty) */
/* for compilation environments where they are not needed. */
/* */
/*     RV_CDECL */
/*     RV_FASTCALL */
/*     RV_STDCALL */
/*         These are the _WIN32 __cdecl, __fastcall and __stdcall conventions. */
/* */
/*     RV_CALL */
/* */
/*         This is the calling convention for HIVE/RV functions. */
/*         We use an explicit calling convention so that the HIVE/RV */
/*         functionality could be packaged in library, and linked to by */
/*         a codec built with an arbitrary calling convention. */
/* */
/*     RV_FREE_STORE_CALL */
/* */
/*         This represents the host compiler's calling convention for */
/*         the C++ new and delete operators. */


#define RV_CDECL 
#define RV_FASTCALL
#define RV_STDCALL
#define RV_FREE_STORE_CALL

#define GNUSTDCALL
#define GNUCDECL
#define GNUFASTCALL
#define GNUFASTCALL_RV8


#define RV_CALL                RV_STDCALL


    /* These definitions should work for most other "32-bit" environments. */
    /* If not, an additional "#elif" section can be added above. */
#undef	U8
#undef	U16
#undef	U32
#undef	S8

#ifndef S8    
    //typedef signed char         S8;
    #define S8	signed char
#endif
#ifndef U8
    //typedef unsigned char       U8;
    #define U8	unsigned char
#endif
#ifndef I8    
    typedef signed char         I8;
#endif
#ifndef U16    
    //typedef unsigned short      U16;
    #define	U16	unsigned short
#endif    
#ifndef I16    
    typedef signed short        I16;
#endif
#ifndef U32    
    //typedef unsigned int        U32;
    #define	U32	unsigned int
#endif
#ifndef I32    
    //typedef signed int          I32;
    #define I32	signed int
#endif
#ifndef F32    
    typedef float               F32;
#endif
#ifndef F64    
    typedef double              F64;
#endif    
#if !defined(BOOL) && !defined(HAVE_BOOL)
   //typedef unsigned char         BOOL;
   #define HAVE_BOOL
#endif
/* Enumerations */
/* */
/* The size of an object declared with an enumeration type is */
/* compiler-specific.  The Enum32 type can be used to represent */
/* enumeration values when the representation is significant. */

typedef I32         Enum32;


/* RV_Boolean values */
/* */
/* The "RV_Boolean" type should be used only when an object's size is not */
/* significant.  Bool8, Bool16 or Bool32 should be used elsewhere. */
/* */
/* "TRUE" is defined here for assignment purposes only, for example */
/* "is_valid = TRUE;".  As per the definition of C and C++, any */
/* non-zero value is considered to be TRUE.  So "TRUE" should not be used */
/* in tests such as "if (is_valid == TRUE)".  Use "if (is_valid)" instead. */


typedef int         RV_Boolean;
enum                { RV_FALSE, RV_TRUE };

/*
typedef I8          Bool8;
typedef I16         Bool16;
typedef I32         Bool32;
*/

/* */
/* Define the "P*" pointer types */
/* */

/*
typedef U8             *PU8;
typedef I8             *PI8;
typedef U16            *PU16;
typedef I16            *PI16;
typedef U32            *PU32;
typedef I32            *PI32;
typedef F32            *PF32;
typedef F64            *PF64;
typedef Bool8          *PBool8;
typedef Bool16         *PBool16;
typedef Bool32         *PBool32;
typedef RV_Boolean     *PBoolean;
typedef Enum32         *PEnum32;
*/

/* NULL is defined here so that you don't always have to */
/* include <stdio.h> or some other standard include file. */

#undef  NULL
#define NULL 0


/* */
/* Define some useful macros */
/* */



//#define ABS(a)            ((U8)(~((signed int )a>>31)&a))

#undef  MAX
#define MAX(a, b)       (((a) > (b)) ? (a) : (b))

#undef  MIN
#define MIN(a, b)       (((a) < (b)) ? (a) : (b))


/* Perform byte swapping on a 16 or 32-byte value. */

#define RV_SWAP16(x) (U16( (((x) & 0xff) << 8) | (((x) & 0xff00) >> 8) ))

#define RV_SWAP32(x) (U32(  (((x) & 0xff)     << 24) \
                          | (((x) & 0xff00)   <<  8) \
                          | (((x) & 0xff0000) >>  8) \
                          | (((x) >> 24) & 0xff) ))




#if defined (WIN32) || defined (ARMULATOR)
#include <stdio.h>

//#define ZTRACEF	printf

#define CLOCK()			clock()
#else
#define CLOCK()			0
static __inline void ZTRACEF(const char *fmt, ...) {
}

#endif

#define CSECT(name)

#define DSECT(name)




#endif /* RVTYPES_H__ */
