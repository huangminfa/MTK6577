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

/* rvtimestamp.h - rvtimestamp header file */
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
/***********************************************************************
 *	DESCRIPTION:
 *	This module provides functions for accessing a timestamp. Timestamp values
 *  are guaranteed to be linear (will never go backwards) and will effectively
 *  never wrap (it will actually wrap in about 292 years).
 ************************************************************************/
#ifndef RV_TIMESTAMP_H
#define RV_TIMESTAMP_H

#include "rvccore.h"
#include "rvlog.h"

#if !defined(RV_TIMESTAMP_TYPE) || \
    ((RV_TIMESTAMP_TYPE != RV_TIMESTAMP_USER_DEFINED)   && (RV_TIMESTAMP_TYPE != RV_TIMESTAMP_LINUX)             && \
     (RV_TIMESTAMP_TYPE != RV_TIMESTAMP_VXWORKS_NORMAL) && (RV_TIMESTAMP_TYPE != RV_TIMESTAMP_VXWORKS_TIMESTAMP) && \
     (RV_TIMESTAMP_TYPE != RV_TIMESTAMP_PSOS)           && (RV_TIMESTAMP_TYPE != RV_TIMESTAMP_OSE)               && \
     (RV_TIMESTAMP_TYPE != RV_TIMESTAMP_NUCLEUS)        && (RV_TIMESTAMP_TYPE != RV_TIMESTAMP_SOLARIS)           && \
     (RV_TIMESTAMP_TYPE != RV_TIMESTAMP_WIN32)          && (RV_TIMESTAMP_TYPE != RV_TIMESTAMP_POSIX)             && \
     (RV_TIMESTAMP_TYPE != RV_TIMESTAMP_UNIXWARE)       && (RV_TIMESTAMP_TYPE != RV_TIMESTAMP_SYMBIAN)           && \
	(RV_TIMESTAMP_TYPE != RV_TIMESTAMP_MOPI) 			&& (RV_TIMESTAMP_TYPE != RV_TIMESTAMP_OSA))
#error RV_TIMESTAMP_TYPE not set properly
#endif


#include "rvadtstamp.h"


#if defined(__cplusplus)
extern "C" {
#endif

/********************************************************************************************
 * RvTimestampInit - Initializes the Timestamp module.
 *
 * Must be called once (and only once) before any other functions in the module are called.
 *
 * INPUT   : none
 * OUTPUT  : none
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus RvTimestampInit(void);

/********************************************************************************************
 * RvTimestampEnd - Shuts down the Timestamp module. 
 *
 * Must be called once (and only once) when no further calls to this module will be made.
 *
 * INPUT   : none
 * OUTPUT  : none
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RvStatus RvTimestampEnd(void);

/********************************************************************************************
 * RvTimestampSourceConstruct - Constructs timestamp module log source.
 *
 * Constructs log source to be used by common core when printing log from the 
 * timestamp module. This function is applied per instance of log manager.
 * 
 * INPUT   : logMgr - log manager instance
 * OUTPUT  : none
 * RETURN  : RV_OK if successful otherwise an error code. 
 */
RvStatus RvTimestampSourceConstruct(
	IN RvLogMgr	*logMgr);

/********************************************************************************************
 * RvTimestampGet - Gets a timestamp value in nanoseconds. 
 *
 * Values returned by subsequent calls are guaranteed to be linear 
 * (will never go backwards) and will never wrap.
 *
 * INPUT   : logMgr - log manager instance
 * OUTPUT  : none
 * RETURN  : Nanosecond timestamp.
 */
RVCOREAPI 
RvInt64 RVCALLCONV RvTimestampGet(
	IN  RvLogMgr	*logMgr);

/********************************************************************************************
 * RvTimestampResolution - Gets the resolution of the timestamp in nanoseconds.
 *
 * INPUT   : none
 * OUTPUT  : logMgr - log manager instance
 * RETURN  : Resolution of the timestamp in nanoseconds.
 */
RVCOREAPI 
RvInt64 RVCALLCONV RvTimestampResolution(
	IN  RvLogMgr	*logMgr);

/********************************************************************************************
 * RvTimestampGetSecs - gets the number of whole seconds
 *
 * INPUT   : tstamp - 64bit long timestamp
 * RETURN  : Number of wholes seconds, e.g. tstamp / 10^9
 */
#define RvTimestampGetSecs(tstamp) (RvInt64ToRvInt32(RvInt64Div(tstamp, RV_TIME64_NSECPERSEC)))

/********************************************************************************************
 * RvTimestampGetNsecs - gets the remainder in nanosecondsseconds
 *
 * INPUT   : tstamp - 64bit long timestamp
 * RETURN  : remainder in nanoseconds, e.g. tstamp % 10^9
 */
#define RvTimestampGetNsecs(tstamp) (RvInt64ToRvInt32(RvInt64Mod(tstamp, RV_TIME64_NSECPERSEC)))

#if defined(__cplusplus)
}
#endif

#endif
