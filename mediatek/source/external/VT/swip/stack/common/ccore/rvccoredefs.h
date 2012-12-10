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
Filename   : rvccoredefs.h
Description: ccore configuration definitions
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
#ifndef RV_CCOREDEFS_H
#define RV_CCOREDEFS_H

#include "rvinterfacesdefs.h"

/* Module codes (1..1023). One for each module in ccore. See rverror.h for details */
#define RV_CCORE_MODULE_CCORE 1
#define RV_CCORE_MODULE_TM 2
#define RV_CCORE_MODULE_CLOCK 3
#define RV_CCORE_MODULE_TIMESTAMP 4
#define RV_CCORE_MODULE_64ASCII 5
#define RV_CCORE_MODULE_SEMAPHORE 6
#define RV_CCORE_MODULE_MUTEX 7
#define RV_CCORE_MODULE_THREAD 8
#define RV_CCORE_MODULE_LOCK 9
#define RV_CCORE_MODULE_MEMORY 10
#define RV_CCORE_MODULE_OSMEM 11 /* MEMORY driver */
#define RV_CCORE_MODULE_POOLMEM 12 /* MEMORY driver */
#define RV_CCORE_MODULE_SELECT 13
#define RV_CCORE_MODULE_SOCKET 14
#define RV_CCORE_MODULE_PORTRANGE 15
#define RV_CCORE_MODULE_LOGLISTENER 16
#define RV_CCORE_MODULE_ADDRESS 17
#define RV_CCORE_MODULE_HOST 18
#define RV_CCORE_MODULE_STDIO 19
#define RV_CCORE_MODULE_LOG 20
#define RV_CCORE_MODULE_TLS 21
#define RV_CCORE_MODULE_DNS 22
#define RV_CCORE_MODULE_SYMNET 23

#define RV_CCORE_MODULE_EHD    24        /* /etc/hosts file based resolver */
#define RV_CCORE_MODULE_LOCAL  1023      /* Error codes local for specific module - should be used between module internal functions */



#endif /* RV_CCOREDEFS_H */
