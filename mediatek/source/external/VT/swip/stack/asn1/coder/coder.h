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
Copyright (c) 2003 RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Ltd.. No part of this document may be reproduced in any
form whatsoever without written prior approval by RADVISION Ltd..
RADVISION Ltd. reserve the right to revise this publication and make
changes without obligation to notify any person of such revisions or
changes.
***********************************************************************/


#ifndef CODER_H
#define CODER_H

#include "pvaltree.h"


#ifdef __cplusplus
extern "C" {
#endif

RVAPI
int RVCALLCONV cmEmEncode(
            IN      HPVT            valH,
            IN      RvPvtNodeId     vNodeId,
            OUT     RvUint8*        buffer,
            IN      int             length,
            OUT     int*            encoded);

RVAPI
int RVCALLCONV cmEmEncodeTolerant(
            IN      HPVT            valH,
            IN      RvPvtNodeId     vNodeId,
            OUT     RvUint8*        buffer,
            IN      int             length,
            OUT     int*            encoded);

RVAPI
int RVCALLCONV cmEmDecode(
            IN      HPVT            valH,
            IN      RvPvtNodeId     vNodeId,
            IN      RvUint8*        buffer,
            IN      int             length,
            OUT     int*            decoded);


/************************************************************************
 * cmEmInstall
 * purpose: Initializes the encode/decode manager.
 *          This function should be called by applications that want to
 *          encode and decode ASN.1 messages without initializing and using 
 *          the CM for this purpose.
 * input  : maxBufSize  - Maximum size of the buffer that is supported (messages 
 *                        larger than this size in bytes cannot be decoded/encoded).
 * output : None.
 * return : Non-negative value on success. Negative value on failure.
 ************************************************************************/
RVAPI
int RVCALLCONV cmEmInstall(IN int maxBufSize);


/************************************************************************
 * cmEmEnd
 * purpose: Un-initializes the encode/decode manager.
 *          This function should be called by applications that called
 *          cmEmInstall() directly to clean up used memory.
 * input  : None.
 * output : None.
 * return : Non-negative value on success. Negative value on failure.
 ************************************************************************/
RVAPI
int RVCALLCONV cmEmEnd(void);



#ifdef __cplusplus
}
#endif

#endif

