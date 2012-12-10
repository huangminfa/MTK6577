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

/* rvadtstamp.h - rvadtstamp header file */
/************************************************************************
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

#ifndef RV_ADTSTAMP_H
#define RV_ADTSTAMP_H


#include "rvccore.h"

/* Lets make error codes a little easier to type */
#define RvTimestampErrorCode(_e) RvErrorCode(RV_ERROR_LIBCODE_CCORE, RV_CCORE_MODULE_TIMESTAMP, (_e))


/* Module specific error codes (-512..-1023). See rverror.h dor more details */
#define RV_TIMESTAMP_ERROR_NOCPUINFO RvTimestampErrorCode(-512) /* Linux only: /proc/cpuinfo file not found */
#define RV_TIMESTAMP_ERROR_NOTSC     RvTimestampErrorCode(-513) /* Linux only: CPU does not have TSC */
#define RV_TIMESTAMP_ERROR_NOMHZ     RvTimestampErrorCode(-514) /* Linux only: can't find CPU speed in /proc/cpuinfo */
#define RV_TIMESTAMP_ERROR_ZEROSPEED RvTimestampErrorCode(-515) /* CPU frequency of 0 reported */



#ifdef __cplusplus
extern "C" {
#endif

/********************************************************************************************
 * RvAdTimestampInit
 *
 * Called by RvTimestampInit.
 * Allows the timestamp adapter to perform OS specific module initialization.
 *
 * INPUT   : None.
 * OUTPUT  : None.
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus RvAdTimestampInit(void);


/********************************************************************************************
 * RvAdTimestampEnd
 *
 * Called by RvTimestampEnd.
 * Allows the timestamp adapter to perform OS specific module clean up.
 *
 * INPUT   : None.
 * OUTPUT  : None.
 * RETURN  : None.
 */
void RvAdTimestampEnd(void);


/********************************************************************************************
 * RvAdTimestampGet
 *
 * Called by RvTimestampGet.
 * Returns a timestamp value in nanoseconds.  Values always grow up linearly.
 *
 * INPUT   : None.
 * OUTPUT  : None.
 * RETURN  : Nanosecond timestamp.
 */
RvInt64 RvAdTimestampGet(void);


/********************************************************************************************
 * RvAdTimestampResolution
 *
 * Called by RvTimestampResolution.
 * Returns the resolution of the timestamp in nanoseconds.
 *
 * INPUT   : None.
 * OUTPUT  : None.
 * RETURN  : Resolution of the timestamp in nanoseconds.
 */
RvInt64 RvAdTimestampResolution(void);

#ifdef __cplusplus
}
#endif


#endif
