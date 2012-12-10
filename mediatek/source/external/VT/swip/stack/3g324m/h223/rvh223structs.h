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

/******************************************************************************
        Copyright (c) 2006 RADVISION Inc. and RADVISION Ltd.
*******************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Inc. and RADVISION Ltd.. No part of this document may be
reproduced in any form whatsoever without written prior approval by
RADVISION Inc. or RADVISION Ltd..

RADVISION Inc. and RADVISION Ltd. reserve the right to revise this
publication and make changes without obligation to notify any person of
such revisions or changes.
******************************************************************************/

#ifndef _RV_H223_STRUCTS_H
#define _RV_H223_STRUCTS_H

#include "rvh223muxer.h"
#include "rvh223demux.h"
#include "rvh223control.h"
#include "muxtable.h"
#include "rvtimer.h"

#ifdef __cplusplus
extern "C" {
#endif

/*---------------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                                */
/*---------------------------------------------------------------------------*/


/* TH223Class
 * ----------------------------------------------------------------------------
 * The H.223 class, holding it all together.
 */
typedef struct
{
    HDEMUXMODULE         hDemuxModule;
    HMUXERMODULE         hMuxerModule;
    HH223CONTROLMODULE   hH223ControlModule;
    HTXALMODULE          htxAlModule;
    HRXALMODULE          hrxAlModule;
    HRA                  instance;
    HRPOOL               hRpool; /* RPOOL of ALxM messages */
    RvLogSource          hLog;      /* Log source for the log printing */
    RvLogSource          hLogErr;   /* Log source for the log printing of errors */
//    RvWatchdog          *hWatchdog;
    RvTimerQueue        *pcontrolTimerQueue;/* Timer queue for use in SRP/NSRP module.*/
    RvTimerQueue        *prtxTimerQueue;    /* Timer queue for use in Rtx module.*/
    RvLogMgr            *hLogMgr;

#if (RV_H223_USE_MEMORY_LOCALITY == RV_YES)
    /* Offset calculation information... */
    RvSize_t            demuxDescriptors; /* Starting position of de-multiplexer LC descriptors */
    RvSize_t            muxerDescriptors; /* Starting position of multiplexer LC descriptors */
#endif
} TH223Class;


/* TH223Instance
 * ----------------------------------------------------------------------------
 * An H.223 call instance.
 * For data locality, this struct comes with more data after it as follows:
 * TH223Instance                    - Base call information
 * h223AcnBufferElem * numBuffers   - ACN Demux video buffers
 */
typedef struct
{
    /* 2010/4/21 Meggie: RVCT 8B alignment - structure rearrange */
    /* If the structure contains 8B type inside, it shall be put to the top */
    TH223DemuxInstance  hdemux;
    H223ControlInstance hcontrol;

    TH223MuxerInstance  hmuxer; 
    
    
#if (RV_H223_USE_STATISTICS == RV_YES)
    TH223Statistics     stats; /* Statistics information gathered for this instance */
#endif
    TH223Class          *pH223Class;
} TH223Instance;




/* Access macros go here. They are used in order for us to be able to handle
   memory locality feature only when required. */
#if (RV_H223_USE_MEMORY_LOCALITY == RV_YES)
#define MuxerChannelIsNull(_pmux, _idx) ((_pmux)->channelOffset[_idx] == 0)
#define MuxerChannelGet(_pmux, _idx) \
    ( (TH223MuxerLCDesc *)((RvUint8*)(_pmux) + (_pmux)->channelOffset[_idx]) )
#define MuxerChannelSet(_pmux, _idx, _pdesc) \
    ((_pmux)->channelOffset[_idx] = (RvUint32)((RvUint8*)(_pdesc) - (RvUint8*)(_pmux)))
#define MuxerChannelSetNULL(_pmux, _idx) (_pmux)->channelOffset[_idx] = 0

#define DemuxDescNULL 0
#define DemuxDescGetPtr(_pdemux, _pdesc) \
    ( ((_pdesc) != 0) ?                                             \
        (TH223DemuxLCDesc *)((RvUint8*)(_pdemux) + (_pdesc)) :      \
        NULL)
#define DemuxDescGetOffset(_pdemux, _pdesc) \
    (RvUint32)((RvUint8*)(_pdesc) - (RvUint8*)(_pdemux))


#elif (RV_H223_USE_MEMORY_LOCALITY == RV_NO)
#define MuxerChannelIsNull(_pmux, _idx) ((_pmux)->pChannel[_idx] == NULL)
#define MuxerChannelGet(_pmux, _idx) (_pmux)->pChannel[_idx]
#define MuxerChannelSet(_pmux, _idx, _pdesc) (_pmux)->pChannel[_idx] = _pdesc
#define MuxerChannelSetNULL(_pmux, _idx) (_pmux)->pChannel[_idx] = NULL

#define DemuxDescNULL NULL
#define DemuxDescGetPtr(_pdemux, _pdesc) (_pdesc)
#define DemuxDescGetOffset(_pdemux, _pdesc) (_pdesc)

#else
#error RV_H223USE_MEMORY_LOCALITY not set properly!

#endif /* RV_H223_USE_MEMORY_LOCALITY */


#ifdef __cplusplus
}
#endif

#endif /* _RV_H223_STRUCTS_H */
