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

#ifndef _H324M_STACK_OBJECT_H_
#define _H324M_STACK_OBJECT_H_


#include "rvmutex.h"
#include "rvlog.h"
#include "rvtimer.h"
#include "ema.h"
#include "RvRaTimers.h"
#include "ci.h"
#include "rvh223defs.h"
#include "cmH245GeneralDefs.h"
#include "cmH245.h"
#include "Rv3G324mCallTypes.h"
#include "Rv3G324mCallH223Glue.h"
//#include "rvloglistener.h"
#include "rvthread.h"

/* The following are here for configuration by MTK */
#include "vt_cfg_struct.h" 

#ifdef __cplusplus
extern "C" {
#endif
	

/* default configuration values */
#define H324M_NUMBER_OF_H223_MUX_SDU_PER_MUX_LC 100
#define H324M_CONTROL_BUFFER_SIZE 261
#define H324M_LOGICAL_CHANNEL_BUFFER_SIZE 261
#define H324M_SRP_TIMER 2000
#define H324M_NSRP_TIMER 2000
#define H324M_AL3_RETRANSMISSION_TIMER 1000
#define H324M_NUM_OF_AL_SDU_SAVED 10
#define H324M_N400_COUNTER 10
#define H324M_N402_COUNTER 5


typedef struct
{
    /* H245 */
    HH245                   pH245; /* handle to the H245 instance */

    /* H223 */
    HH223MODULE             hH223; /* handle to the H223 module */
    HRA                     logicalChannelBuffers;   /* RA handle for the buffers of the logical channels */

    /*[2010.4.15][Meggie][VCT-8B alignment]Structure adjustment */
    RvUint8                 n400Counter; /* Maximum value of the the counter used in NSRP/SRP procedure. */
    RvUint8                 n402Counter; /* Maximum value of the the counter used in WNSRP procedure. */ 
    RvInt64                 srpTimer;  /* The timer used in SRP procedure. (In nanoseconds) */
    RvInt64                 nsrpTimer;  /* The timer used in NSRP procedure. (In nanoseconds) */
    RvUint16                al3RetransmissionTimer; /* The timer used in Al3 retransmission procedure. (In nanoseconds) */
    RvInt16                 numOfAlSduSaved; /* The number of out of sequence AL-SDU queue depth when Al3 retransmissions are used. */

    /* Callbacks */
    Rv3G324mCallEvHandlers      callEvent; /* Call event handlers */
#if (RV_H223_USE_SPLIT == RV_YES)
    Rv3G324mH223GlueEvHandlers  glueEvent; /* H.223 glue event handlers */
#endif

    HPVT                    hVal;               /* PVT used by the stack */
    HCFG                    hCfg;               /* Configuration of the stack */

    vt_cfg*                 vtCfg;              /* Configuration of the stack - by MTK */

    /* Configuration */
    RvPvtNodeId h245Conf;           /* H.245 configuration node id */

    /* syntax */
    HPST          hSynProtH245; /* Syntax of H.245 messages (MultimediaSystemControlMessage) */
    HPST          hSynConfH245;
    HPST          hMuxElemSynH245; /* Syntax for MultiplexElement type */
    HPST          hMuxEntryDescriptorsSynH245; /* Syntax for MultiplexEntrySend.multiplexEntryDescriptors type */
    HPST          h245DataType;
    HPST          hSynOLC;
#if (RV_H245_LEAN_H223 == RV_NO)
    HPST          h245TransCap;
    HPST          h245RedEnc;
    HPST          hAddrSynH245;
#endif

    /* Log sources used by an H324m instance */
    RvLogMgr        *logMgr;
    RvLogSource     log;
    RvLogSource     logAPI;
    RvLogSource     logCB;
    RvLogSource     logConfig;
    RvLogSource     logAppl;
    RvLogSource     logWatchdog; /* WATCHDOG log source */
	RvLogSource     logH223Strm; /* H223CHAN log source for bit stream logging */
#if (RV_H223_USE_STATISTICS == RV_YES)
    RvLogSource     logStat; /* 324MSTAT log source */
#endif
    RvUint32        tlsLevelIndex; /* Index used for the level of calls to API and CB functions */
    RvLock          lock; /* Lock used to protect parts of this struct (level parameter) */

    RvInt           maxChannels;

    /* Watchdog */
    RvTimer*      watchdogTimer; /* Timer activating the watchdog at a set interval */

    /* Resource enumerations allocated by watchdog */
    RvUint32    h324mTimersResourceVal;
    RvUint32    h324mValTreeResourceVal;

    RvRaTimersHandle        hTimers; /* Timers pool */
    RvTimerQueue           *pTimersQueue;

    /*Calls*/
    HEMA                    hCalls;

    /* Thread information */
    RvThread               *pThreadInfo; /* Thread that constructed this instance */
    RvThread                threadData; /* Thread that constructed this instance if first call in this thread */

} H324mStack;

typedef struct
{
    RvInt32     maxCalls;       /* number of calls per instance of the stack */
    RvInt32     maxTransmittingChannels;  /* number of transmitting channels per instance of the stack */
    RvInt32     maxReceivingChannels;  /* number of receiving channels per instance of the stack */
    RvInt32     totalPvtNodes;  /* number of PVT nodes per instance of the stack */
    RvInt32     maxAsn1BufferSize; /* Maximum size of H.245 ASN.1 message buffers */

    /* H.223 specific parameters */
    RvUint16    numOfH223MuxSduPerMuxLC;  /* number of H.223 MUX-SDU per logical channel */
    RvInt32     srpTimer; /* The timeout used in SRP procedure. (In milliseconds) */
    RvInt32     nsrpTimer; /* The timeout used in NSRP procedure. (In milliseconds) */
    RvInt32     al3RetransmissionTimer; /* The timeout used in Al3 retransmission procedure. (In milliseconds) */
    RvInt16     numOfAlSduSaved; /* The number of out of sequence AL-SDU queue depth when Al3 retransmissions are used. */
    RvUint8     n400Counter; /* Maximum value of the counter used in NSRP procedure. */
    RvUint8     n402Counter; /* Maximum value of the counter used in WNSRP procedure. */

    /* buffers */
    RvInt32     controlBufferSize;     /* buffer size for the H.245 control messages */ 
    RvInt32     controlNumOfBuffers;   /* number of buffers to be allocated for the H.245 control */
    RvInt32     logicalChannelBufferSize;   /* buffer size for media logical channels */
    RvInt32     logicalChannelNumOfBuffers; /* number of buffers to be allocated for media logical channels */
    RvInt32     alxmBufferSize; /* Size of an ALxM buffer */
    RvInt32     alxmNumOfBuffers; /* Number of ALxM buffers */
    RvInt32     interleavingBufferSize; /* Size of an ALxM interleaving buffer */
    RvInt32     interleavingNumOfBuffers; /* Number of ALxM interleaving buffers */
    RvInt32     maxVideoBuffers;        /*Max video buffers per call for ACP*/

}H324mStackConfig;

#ifdef __cplusplus
}
#endif

#endif /* _H324M_STACK_OBJECT_H_ */
