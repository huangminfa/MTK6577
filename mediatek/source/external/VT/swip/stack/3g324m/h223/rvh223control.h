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
        Copyright (c) 2001 RADVISION Inc. and RADVISION Ltd.
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

#ifndef _RV_H223_CONTROL_H
#define _RV_H223_CONTROL_H

#include "rvh223controlifc.h"
#include "vt_l4c_em.h"

#ifdef __cplusplus
extern "C" {
#endif


/*---------------------------------------------------------------------------*/
/*                            MODULE VARIABLES                               */
/*---------------------------------------------------------------------------*/

#define CONTROL_INPUT_QUEUE_DEPTH_MAX 10

#define CCSRL_SDU_SIZE_MAX  RV_H223_MAX_H245_MESSAGE_SIZE
#define CCSRL_NSRP_PDU_SIZE_MAX  (CCSRL_SDU_SIZE_MAX + 1 /*CCSRL header*/ + 4 /*NSRP header+crc*/)
#define CCSRL_LAST_SEGMENT_HEADER       0xff
#define CCSRL_NOT_LAST_SEGMENT_HEADER   0x00
#define CCSRL_HEADER_SIZE               1

#define NSRP_COMMAND_FRAME_HEADER   0xf9
#define SRP_RESPONSE_FRAME_HEADER   0xfb
#define NSRP_RESPONSE_FRAME_HEADER  0xf7
#define WNSRP_COMMAND_FRAME_HEADER   0xf1
#define WNSRP_RESPONSE_FRAME_HEADER  0xf3

#define NSRP_HEADER_SIZE            2
#define SRP_HEADER_SIZE             1
#define NSRP_SEQUENCE_NUMBER_MAX    256

/* Default values for WNSRP */
#define WNSRP_WINDOW_SIZE               RV_H223_WNSRP_WINDOW_SIZE

#define RV_H223_USE_IND_RTX_Q   RV_NO


/* Access Mark Definition - should be exclusive */
#define ACCESS_MARK_NSRP        0x01
#define ACCESS_MARK_WNSRP     0x02
#define EX_ACCESS_MARK_NSRP  0xfe
#define EX_ACCESS_MARK_WNSRP 0xfd

/* MAUI_01694868: Patch for workaround with SamSung W270 */
#define ILLEGAL_NSRP_SEQ_WORKAROUND

typedef enum
{
    NSRP_ACK = 0,
    NSRP_DATA,
    WNSRP_ACK,
    WNSRP_DATA
}ENsrpReason;

typedef enum
{
    CONTROL_MODE_TYPE_SRP = 0,
    CONTROL_MODE_TYPE_NSRP,
    CONTROL_MODE_TYPE_WNSRP,
    CONTROL_MODE_TYPE_NSRP_AND_WNSRP
}ControlModeType;

typedef struct
{
    RvUint8 *pbuf;
    RvUint16 size;
}TInputData;


typedef struct
{
    TRvMuxSdu muxSdu; /* Do not change the order of fields */
    RvUint8   bIsLastSegment;
}WnsrpDataUnit;

typedef struct{
    RvTimerQueue                   *pcontrolTimerQueue;/* Timer queue for timer creation in NSRP.*/    
    HRPOOL                         hWindowRpool;      /* Handle for WNSRP Rpool window */
    TRvMuxSdu                      emptyMuxSdu;
    RvH223ControlDataIndicationEv  ControlDataIndicationEv; /* Delivers data.*/
    RvH223IsClearEv                ControlIsClearEv;    /* Reports readiness to termination.*/
    RvLogSource                    hLog;
    RvLogSource                    hLogErr;
    RvLogMgr                       *hLogMgr;
}H223ControlClass;


typedef struct{ /*[2010.4.15][Meggie][VCT-8B alignment]Structure adjustment */

    /* SRP, NSRP and WNSRP parameters */
    RvUint64        nsrpTimerT401ExpirationValue;
    RvUint64        srpTimerT401ExpirationValue;
    TRvMuxSdu       dataMuxSdu;
    TRvMuxSdu       ackMuxSdu[WNSRP_WINDOW_SIZE];
    RvUint32        ackPayload[WNSRP_WINDOW_SIZE];
    RvInt16         nsrpRcvNumber;  /* Receiving sequence number. 0x7FFF if none received yet */
    RvUint8         ackCounter;
    RvInt8          nsrpInProcess;
    RvUint8         nsrpSendNumber; /* Transmitting sequence number.*/
    RvUint8         nsrpN400Counter;/* Retransmission counter.*/
    RvTimer         nsrpT401Timer;  /* Timer descriptor for NSRP stack.*/
    RvUint8         timerIsActive;/* Boolean: there is an active timer in the system.*/
#ifdef _VT_USE_TIMER_POOL_
    /* WNSRP N400Counter, T401Timer, TimerIsActive array are needed because the
    WNSRP commands can be sent simultaneously */    
    RvUint8         wnsrpN400Counter[WNSRP_WINDOW_SIZE]; 
    RvTimer        wnsrpT401Timer[WNSRP_WINDOW_SIZE];  
    RvUint8         wnsrpTimerIsActive[WNSRP_WINDOW_SIZE];
    
    RvUint8         accessMark[WNSRP_WINDOW_SIZE];
    /* The accessMark array will be modified by both WNSRP and NSRP frames */
    
    RvBool          wnsrpAlreadyTimeout[WNSRP_WINDOW_SIZE];
    /* True if wnsrp command k has exceeded max retry count but cannot be free because currently it is not WNSRP mode */

    WnsrpDataUnit   nsrpSendWindow[WNSRP_WINDOW_SIZE]; 
    /* Store NSRP command frames */
#endif

    RvUint8         srpSduWasSent; /* RV_TRUE if an SDU was sent and is waiting to be released/retransmitted */
    RvUint8         wnsrpExpectedRecvNumber; /* The next expected WNSRP sequence number. */
    RvUint8         wnsrpAckWaitingNumber; /* First WNSRP sequence number to be acked */
    WnsrpDataUnit   wnsrpSendWindow[WNSRP_WINDOW_SIZE];
    WnsrpDataUnit   wnsrpRecvWindow[WNSRP_WINDOW_SIZE];
    RvUint8         wnsrpFirstSendIndex;  /* First index in the send window */
    RvUint8         wnsrpLastSendIndex;   /* Last index in the send window */
    RvUint8         wnsrpFirstRecvIndex;  /* First index in the recv window */
    RvUint8         wnsrpLastRecvIndex;   /* Last index in the recv window */
    RvUint8         wnsrpSendSize;        /* The number of elements in the send window */
    RvUint8         wnsrpRecvSize;        /* The number of elements in the recv window */
    ControlModeType eControlModeType;   /* Used to indicate which control type is used */
    RvUint8         wnsrpN402Counter;  /* counter for WNSRP.*/
    RvUint8         nsrpCounterN400MaxValue; /* Maximum value of counter used for NSRP/SRP procedure */
    RvUint8         wnsrpCounterN402MaxValue; /* Maximum value of counter used for WNSRP procedure */
    
//    RvUint8         nsrpProcessState;/*prevent for nsrp ack race condition cause from w900i*/// mtk01567
    /* MTK: Add a new field */
    RvBool          bIsPeerSupportNSRP;  /* Default: True */
#ifdef ILLEGAL_NSRP_SEQ_WORKAROUND
    RvBool          bIsFirstNSRPSQNonZero; /* Default: FALSE */
#endif

    /*retransmission on idle params*/
    RvUint8         retransmitOnTimeoutDisabled; /*RV_TRUE when retransmission on idle is active.*/
   
    /* CCSRL parameters */
    RvUint8         receiveBuffer[CCSRL_NSRP_PDU_SIZE_MAX];
    RvUint16        maxCCSRLSegmentSize; /* Maximum size of a CCSRL segment */
    EMuxLevel       muxerLevel;     /* Multiplex level of Muxer for CCSRL dividing.*/
    EMuxLevel       demuxLevel;     /* Multiplex level of Demux for CCSRL dividing.*/

    /* General parameters */
    RvUint8         reportClear;
    void            *userContext;    /* Handle to use in the callbacks.*/
    H223ControlClass *pControlClass;


    /* Handles */
    HMUXER          hmuxer;         /* Handle of the associated muxer.*/
    HDEMUX          hdemux;         /* Handle of the associated demux.*/
    HMUXLCDESC      hmuxerChannel;  /* Handle the associated muxer's channel.*/
    HMUXLCDESC      hdemuxChannel;  /* Handle the associated demux's channel.*/
    RvMutex         controlMutex;
}H223ControlInstance;

#ifdef _VT_USE_TIMER_POOL_
typedef struct{
    void  *content;   
    RvInt32 wnsrpIndex;
}VT_TIMEOUT_CONTEXT;
#endif

#define RV_SET_CONTROL_MODE_TYPE(a, b, c) a=b
#if 0
#define RV_SET_CONTROL_MODE_TYPE(a, b, c)\
{\
(c || b!=CONTROL_MODE_TYPE_NSRP) ? (a=b) : (a=CONTROL_MODE_TYPE_SRP);\
kal_prompt_trace(MOD_VT,"support?=%d, mode=%d",c,a);\
vt_l4c_em_report_retransmission_protocol(a);}
#endif

#ifdef _VT_USE_TIMER_POOL_
#define RV_INC_UINT8(a) \
{\
RvUint8 b = a;\
a++;\
ASSERT(a>b);}

#define RV_DEC_UINT8(a) \
{\
RvUint8 b = a;\
a--;\
ASSERT(a<b);}

#define RV_GET_ARRAY_INDEX(top,base) (top - base)
#endif

#ifdef __cplusplus
}
#endif

#endif /* _RV_H223_CONTROL_H */

