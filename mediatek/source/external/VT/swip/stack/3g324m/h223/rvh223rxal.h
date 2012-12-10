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

#ifndef _RV_H223_RXAL_H
#define _RV_H223_RXAL_H

#ifdef __cplusplus
extern "C" {
#endif

/*---------------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                                */
/*---------------------------------------------------------------------------*/

#if (RV_H223_USE_AL3_RETRANSMISSIONS == RV_YES)
#define RXAL_SREJ_QUEUE_SIZE                10
#else
#define RXAL_SREJ_QUEUE_SIZE                0
#endif

#define RXAL_SEQ_NUM_INVALID                0xffff

#define INFINITE_RETRANSMISSIONS            255

#define SET_SEQ_NUM(_error, _x)   (EExtractionResult)((RvUint32)(_error) | (RvUint32)(_x))

typedef struct{
    TRvAlSdu    alSdu;
    RvUint16    size;
    RvUint16    referenceSeqNum;
    RvUint16    ownSeqNum;
}TAlSduDesc;

typedef struct{
    RvUint16    requestedSeqNum;
    RvUint8     retransmissionsNum; /* 255 for infinite */
    RvUint8     wasRemoved; /* indicates the readIndex that the current packet was removed from SrejList because,
                               it was received before a predecessor packet */
    RvUint8     rtxCodeRate; /* saves retransmission code rate of the current packet - for ARQ II */ // TODO: Do we need this???
}TAlSrej;

/* TH223TxALClass
 * ----------------------------------------------------------------------------
 * Outgoing logical channel Adaptation Layer class.
 */
typedef struct
{
#if (RV_H223_USE_MEMORY_LOCALITY == RV_NO)
    HRA             instance;
#endif
    RvUint16        alSduQueueDepth;
    RvLogSource     hLog;
    RvLogSource     hLogErr;
    RvLogMgr        *hLogMgr;
#if (RV_H223_USE_MUX_LEVEL_3 == RV_YES)
    HRPOOL          hRpool;
#endif
#if (RV_H223_USE_AL3_RETRANSMISSIONS == RV_YES)
    RvTimerQueue   *prtxTimerQueue;
#endif
}TH223RxALClass;


/* TH223RxALInstance
 * ----------------------------------------------------------------------------
 * Incoming logical channel Adaptation Layer instance.
 */
typedef struct
{
    TH223RxALCfg        cfg;
    TH223RxALClass      *pAlClass;
    TRvAlSdu            curAlSdu;
    void                *context;
    RvUint16            expectedSequenceNumber;
    RvUint16            lastDeliveredSequenceNumber;
    RvUint16            maxSequenceNumber;
    RvBool              incrementSequenceNumber;
    RvUint8             headerSize; /* Size of the header for the received channel */
    RvUint8             headerAlignment; /* Number of bytes to offset before placing the header */
    HDEMUX              hdemux;
    HMUXLCDESC          hlcdesc;
    RvMutex             instanceMutex;
    TAlSduDesc          *palSduDesc;
    TCyclicParams       alSduList; /* List of saved packets for future delivery */
#if (RV_H223_USE_MUX_LEVEL_3 == RV_YES)
    RvUint8             *pAccBuff; /* Accumulate Buffer for splitting mode*/
    RvUint32            accOffset; /* Calculate payload size in splitting mode*/
    RvUint32            bufMaxSize; /* Application max size for Accumulate Buffer*/
    RvUint32            pAccExtractionResult; /* Accumulate results for splitting mode */
#endif /* (RV_H223_USE_MUX_LEVEL_3 == RV_YES) */
#if (RV_H223_USE_AL3_RETRANSMISSIONS == RV_YES)
    TAlSrej             *palSrej;
    TCyclicParams       alSrejList; /* List of requested SREJs */
    RvTimer             srejTimer; /* timer for the SREJ requests*/
#endif
}TH223RxALInstance;

#ifdef __cplusplus
}
#endif

#endif /* _RV_H223_RXAL_H */
