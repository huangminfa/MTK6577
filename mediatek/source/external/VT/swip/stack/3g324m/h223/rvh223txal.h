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

#ifndef _RV_H223_TXAL_H
#define _RV_H223_TXAL_H

#ifdef __cplusplus
extern "C" {
#endif

/*---------------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                                */
/*---------------------------------------------------------------------------*/
#define SPDU_PAYLOAD_SIZE       1
#define TAIL_BIT_LENGTH         4
#define AL_SDU_SPLITTING_SIZE   10 // TODO: think of how to fix it
#define SRS_MAX_BLOCK_SIZE      255


typedef struct{
    ERtxReqCode     rtxReqCode;
    RvUint16        sequenceNumber;
    RvUint8         rtxCodeRate; /* For ARQ II*/
}TRtxQueueMsg;

typedef struct{
    RvUint8     pbuf[SPDU_PAYLOAD_SIZE];
    TRvMuxSdu   muxSdu;
}TSpdu;


/* TH223TxALClass
 * ----------------------------------------------------------------------------
 * Outgoing logical channel Adaptation Layer class.
 */
typedef struct
{
#if (RV_H223_USE_MEMORY_LOCALITY == RV_NO)
    HRA             instance;
#endif
    RvUint16        muxSduQueueDepth;
    TRvMuxSdu       emptyMuxSdu;
    RvLogSource     hLog;
    RvLogSource     hLogErr;
    RvLogMgr        *hLogMgr;
#if (RV_H223_USE_MUX_LEVEL_3 == RV_YES)
    HRPOOL          hRpool;
    HRA             hInterleaving; /* Interleaving buffers pool. Used for ALxM. */
#endif
}TH223TxALClass;


/* TRtxCyclicParams
 * ----------------------------------------------------------------------------
 * Helper structure for cyclic buffer management.
 */
typedef struct
{
    RvUint32  readIndex;
    RvUint32  writeIndex;
    RvUint32  usedSize;
}TRtxCyclicParams;


/* TH223TxALInstance
 * ----------------------------------------------------------------------------
 * Outgoing logical channel Adaptation Layer instance.
 */
typedef struct
{
    TH223TxALCfg    cfg;
    TH223TxALClass *pAlClass;
    TCyclicParams   muxSduList;
    TRvMuxSdu      *pmuxSdu;
#if (RV_H223_USE_AL3_RETRANSMISSIONS == RV_YES)
    TCyclicParams   spduList;
    TSpdu          *pspdu;
    TRtxQueueMsg   *rtxQueue; /* Retransmission requests queue */
    RvUint16        rtxQueueSize; /* Number of requests currently in rtx queue */
    RvLock          rtxQueueLock; /* Lock for rtx queue */
#endif
    void           *context;
    RvUint16        sequenceNumber;
    RvBool          incrementSequenceNumber;
    TRvAlSdu        curAlSdu;
    HMUXER          hmuxer;
    HMUXLCDESC      hlcdesc;
    RvMutex         instanceMutex;
    RvBool           isDrop;
}TH223TxALInstance;

#ifdef __cplusplus
}
#endif

#endif /* _RV_H223_TXAL_H */
