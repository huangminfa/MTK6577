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
Filename   : rvstrutils.h
Description: rvstrutils header file
************************************************************************
      Copyright (c) 2001,2002 RADVISION Inc. and RADVISION Ltd.
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

/***********************************************************************
 This module provides string manipulations functions
 ***********************************************************************/

#ifndef RV_STRUTILS_H
#define RV_STRUTILS_H

#include "rvtypes.h"

#if (RV_OS_TYPE == RV_OS_TYPE_WIN32)

#define strcasecmp(a,b)		_stricmp(a,b)
#define strncasecmp(a,b,n)	_strnicmp(a,b,n)

#endif

#if (RV_OS_TYPE == RV_OS_TYPE_VXWORKS) || (RV_OS_TYPE == RV_OS_TYPE_NUCLEUS)   || \
    (RV_OS_TYPE == RV_OS_TYPE_MOPI)    || (RV_OS_TYPE == RV_OS_TYPE_PSOS)      || \
    (RV_OS_TYPE == RV_OS_TYPE_OSE)     || (RV_OS_TYPE == RV_OS_TYPE_INTEGRITY) || \
    (RV_OS_TYPE == RV_OS_TYPE_WINCE)

#if defined(__cplusplus)
extern "C" {
#endif 
    
#define NEED_STRCASECMP
    
    
/********************************************************************************************
 * strcasecmp
 * Performs case insensitive comparison of two null terminated strings
 * INPUT   : s1
 *           s2 - strings to compare
 * OUTPUT  : None
 * RETURN  : 0 if strings are the same or lexical difference in the
 *           first differing character.
 */
RVCOREAPI 
int RVCALLCONV strcasecmp(
    IN const char *s1,
    IN const char *s2);

/********************************************************************************************
 * strncasecmp
 * Performs case insensitive comparison of first 'n' bytes of two strings
 * INPUT   : s1
 *           s2 - strings to compare
 *           n  - number of bytes to compare
 * OUTPUT  : None
 * RETURN  : 0 if strings are the same or lexical difference in the
 *           first differing character.
 */
RVCOREAPI 
int RVCALLCONV strncasecmp(
    IN const char *s1,
    IN const char *s2,
    IN size_t n);
       

#if defined(__cplusplus)
}
#endif

#endif


#endif /* RV_STRUTILS_H */
