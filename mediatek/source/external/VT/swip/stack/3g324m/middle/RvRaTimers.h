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
        Copyright (c) 2002 RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Ltd.. No part of this document may be reproduced in any
form whatsoever without written prior approval by RADVISION Ltd..

RADVISION Ltd. reserve the right to revise this publication and make
changes without obligation to notify any person of such revisions or
changes.
***********************************************************************/

#ifndef _RV_RA_TIMERS_H
#define _RV_RA_TIMERS_H

#include "ra.h"
#include "rvtimer.h"


#ifdef __cplusplus
extern "C" {
#endif


RV_DECLARE_HANDLE(RvRaTimersHandle);

RvStatus RvRaTimersInit(IN int maxCalls);

/************************************************************************
 * RvRaTimersConstruct
 * purpose: Create a timers array.
 * input  : maxTimers   - Maximum number of timers needed
 *			hLogMgr     - The log manager
 * output : timersQueue - Queue of timers these timers are in
 * return : pointer to timers handle on success, NULL o.w.
 ************************************************************************/
RvRaTimersHandle RvRaTimersConstruct(
    IN    int               maxTimers,
	IN    RvLogMgr          *hLogMgr,
    OUT   RvTimerQueue**    timersQueue);


/************************************************************************
 * RvRaTimersDestruct
 * purpose: Create a timers array.
 * input  : timers          - pointer to timers array
 * output : none
 * return : none
 ************************************************************************/
void RvRaTimersDestruct(
    IN RvRaTimersHandle timersH);


/************************************************************************
 * RvRaTimerStartWithType
 * purpose: Set a timer of the stack.
 * input  : timers          - pointer to timers array
 *          eventHandler    - Callback to call when timer expires
 *          context         - Context to use as parameter for callback function
 *          timeOut         - Timeout of timer in nanoseconds (0 is not ignored)
 *          timerType       - Type of timer: RV_TIMER_TYPE_ONESHOT, RV_TIMER_TYPE_PERIODIC
 * output : None
 * return : Pointer to timer on success, NULL o.w.
 ************************************************************************/
RvTimer* RvRaTimerStartWithType(
    IN    RvRaTimersHandle      timersH,
    IN    RvTimerFunc           eventHandler,
    IN    void*                 context,
    IN    RvInt64               timeOut,
    IN    RvInt                 timerType);


/************************************************************************
 * RvRaTimerStart
 * purpose: Set a timer of the stack, reseting its value if it had one
 *          previously.
 * input  : timers          - pointer to timers array
 *          eventHandler    - Callback to call when timer expires
 *          context         - Context to use as parameter for callback function
 *          timeOut         - Timeout of timer in milliseconds
 * output : None
 * return : pointer to timer on success, NULL o.w.
 ************************************************************************/
#define RvRaTimerStart(_timersH, _eventHandler, _context, _timeOut) \
    ( ((_timeOut) <= 0) ? NULL :                                                 \
      RvRaTimerStartWithType((_timersH), (_eventHandler), (_context),         \
      ((RvInt64)(_timeOut)) * RV_TIME64_NSECPERMSEC, RV_TIMER_TYPE_ONESHOT) )


/************************************************************************
 * RvRaTimerStartPeriodic
 * purpose: Set a periodic timer for the stack
 * input  : timers          - pointer to timers array
 *          eventHandler    - Callback to call when timer expires
 *          context         - Context to use as parameter for callback function
 *          timeOut         - Timeout of timer in milliseconds
 * output : None
 * return : pointer to timer on success, NULL o.w.
 ************************************************************************/
#define RvRaTimerStartPeriodic(_timersH, _eventHandler, _context, _timeOut) \
    ( ((_timeOut) <= 0) ? NULL :                                                 \
      RvRaTimerStartWithType((_timersH), (_eventHandler), (_context),         \
      ((RvInt64)(_timeOut)) * RV_TIME64_NSECPERMSEC, RV_TIMER_TYPE_PERIODIC) )


/************************************************************************
 * RvRaTimerCancel
 * purpose: Reset a timer if it's set
 *          Used mainly for call timers.
 * input  : timer   - Timer to reset
 * output : timer   - Timer's value after it's reset
 * return : RV_OK on success, other on failure
 ************************************************************************/
RvStatus RvRaTimerCancel(
    IN    RvRaTimersHandle      timersH,
    INOUT RvTimer               **timer);


/************************************************************************
 * RvRaTimerClear
 * purpose: Clear a timer from the array
 * input  : timers  - pointer to timers array
 *          timer   - Timer to reset
 * output : none
 * return : RV_OK on success, other on failure
 ************************************************************************/
RvStatus RvRaTimerClear(
    IN    RvRaTimersHandle      timersH,
    IN    RvTimer               **timer);


/************************************************************************
 * RvRaTimerStatistics
 * purpose: Get timer pool statistics
 * input  : timers      - pointer to timers array
 * output : statistics  - Statistics information about the timer pool
 * return : RV_OK on success, other on failure
 ************************************************************************/
RvStatus RvRaTimerStatistics(
    IN    RvRaTimersHandle      timersH,
    OUT   RvRaStatistics*       statistics);



#ifdef __cplusplus
}
#endif

#endif  /* _RV_RA_TIMERS_H */


