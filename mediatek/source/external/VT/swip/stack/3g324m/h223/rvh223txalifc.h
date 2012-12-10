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

#ifndef _RV_H223_TXAL_IFC_H
#define _RV_H223_TXAL_IFC_H

#ifdef __cplusplus
extern "C" {
#endif

/*---------------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                                */
/*---------------------------------------------------------------------------*/

/* This routine is used by transmitting AL in order to notify the user
   that a requested to retransmission PDU is not available.*/
typedef void (*RvH223AlNotifyDrtxEv) (IN void *,    /* Context.*/
                                      IN RvUint16   /* Sequence number.*/
                                     );
/* Common callbacks for all TXAL's.*/
typedef struct{
    RvH223ReleaseBufEv  ReleaseBufEv;   /* Callback for input MuxSdu release.*/
    RvH223IsClearEv     IsClearEv;      /* Ready for LC termination callback.*/
}TTxCallbacks;

/* Configuration parameters of AL instance.*/
typedef struct{
    RvBool  isFramed;
}TTxAl1Params;

typedef struct{
    RvBool  useSequenceNumbering;
}TTxAl2Params;

typedef struct{
    RvUint8                 controlFieldSize;
    RvUint8                 useRtx;
    RvH223AlNotifyDrtxEv    NotifyDrtxEv;
}TTxAl3Params;

/* AL1M, AL2M & AL3M related parameters */
typedef struct{
    RvBool                  isFramed;
    RvBool                  isUseGolay;
    ECrcLength              crcLength;
    RvBool                  isInterleaving;
    RvBool                  isALSDUSplitting;
    RvUint8                 codeRate; /* can be RCPC(Annex C) or RS(Annex D) */
    EArqType                arqType;
    /* Configuration parameters of ARQ I & II.*/
    RvUint8                 retransmissionsNum; /* 255 for infinite */
    RvUint32                sendBufferSize;
    RvH223AlNotifyDrtxEv    NotifyDrtxEv;
}TTxAlxMParams;

typedef struct{
    EAL               layer;
    RvBool            isSegmentable;
    RvH223LCDataType  dataType;
    RvUint16          lcn;
    RvBool            bIsMPC;
    union{
        TTxAl1Params txal1;
        TTxAl2Params txal2;
        TTxAl3Params txal3;
#if (RV_H223_USE_MUX_LEVEL_3 == RV_YES)
        TTxAlxMParams   txalxM;
#endif
    }txalParams;
#define txal1Params         txalParams.txal1
#define txal2Params         txalParams.txal2
#define txal3Params         txalParams.txal3
#define txalxMParams        txalParams.txalxM

    void                    *context;
    RvInt32                 subContext;
    TTxCallbacks            txalCallbacks;
#define AlReleaseBufEv      txalCallbacks.ReleaseBufEv
#define AlIsClearEv         txalCallbacks.IsClearEv
#define AlNotifyDrtxEv      txal3Params.NotifyDrtxEv
}TH223TxALCfg;

/*---------------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                               */
/*---------------------------------------------------------------------------*/

/******************************************************************************
 * RvH223TxALInit
 * ----------------------------------------------------------------------------
 * General: Initializes the transmitting AL module.
 *
 * Return Value: RV_OK  - if successful.
 *               otherwise - other modules' error codes.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:   interleavingBufferSize      - Size of an interleaving buffer.
 *          interleavingNumOfBuffers    - Number of interleaving buffers.
 *          numOfInstances              - Maximal number of concurrent instances.
 *          numOfMuxSduPerInstance      - Size of MuxSdu pool.
 *          hRpool                      - handle to the rpool
 *          hLog                        - handle to the log source.
 *          hLog                        - handle to the error log source.
 *          hLogMgr                     - handle to the log manager.
 *          hWatchdog                   - handle to the watchdog.
 * Output:  hAlModule                   - handle to the AL module.
 *****************************************************************************/
RvStatus RvH223TxALInit (
    IN RvUint16         interleavingBufferSize,
    IN RvUint16         interleavingNumOfBuffers,
    IN RvUint16         numOfInstances,
    IN RvUint16         numOfMuxSduPerInstance,
    OUT HRPOOL          hRpool,
    IN RvLogSource      hLog,
    IN RvLogSource      hLogErr,
    IN RvLogMgr         *hLogMgr,
    OUT HTXALMODULE     *hAlModule);

/******************************************************************************
 * RvH223TxALGetLCDescSizeOf
 * ----------------------------------------------------------------------------
 * General: Find the size of a multiplexer AL.
 *
 * Return Value: Size of the data required for allocation.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input: muxSduQueueDepth    - Number of MUX-SDUs that can be pending.
 *****************************************************************************/
RvSize_t RvH223TxALGetLCDescSizeOf(
    IN RvUint16 muxSduQueueDepth);

/******************************************************************************
 * RvH223TxALEnd
 * ----------------------------------------------------------------------------
 * General: Terminates the class.
 *
 * Return Value: RV_OK  if successful.
 *               otherwise - other modules' error codes.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input: hAlModule - handle to the AL module.
 *****************************************************************************/
RvStatus RvH223TxALEnd (HTXALMODULE hAlModule);

/******************************************************************************
 * RvH223TxALConstruct
 * ----------------------------------------------------------------------------
 * General: Constructs an instance of transmitting AL.
 *
 * Return Value: RV_OK  - if successful.
 *               RV_ERROR_OUTOFRESOURCES - if there are no more free instances.
 *               otherwise - other modules' error codes.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input  : hAlModule - handle to the AL module.
 *          pCfg      - Configuration parameters.
 *          hMuxer    - Handle to Muxer.
 * Output : phnd      - Pointer to be filled with logical channel handle.
 *****************************************************************************/
RvStatus RvH223TxALConstruct (IN   HTXALMODULE   hAlModule,
                              IN   TH223TxALCfg *pcfg,
                              IN   HMUXER        hMuxer,
                              OUT  HTXAL        *phnd);

/******************************************************************************
 * RvH223TxALDestruct
 * ----------------------------------------------------------------------------
 * General: Destructs an instance of transmitting AL.
 *
 * Return Value: RV_OK  - if successful.
 *               RV_ERROR_BADPARAM - if the handle is invalid.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input  : hal - Logical channel's handle.
 *****************************************************************************/
RvStatus RvH223TxALDestruct (IN HTXAL hal);

/******************************************************************************
 * RvH223TxAlMuxDataRequest
 * ----------------------------------------------------------------------------
 * General: Sends user data.
 *
 * Return Value: RV_OK  - if successful.
 *               RV_ERROR_BADPARAM - if the handle is invalid.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:   hal             - Handle to a TXAL instance.
 *          pbuf            - Data buffer.
 *          size            - Size of data buffer.
 * Output:  pOccupiedSlots  - Number of occupied slots in Muxer queue,
 *                            including the latter request
 *****************************************************************************/
RvStatus RvH223TxAlMuxDataRequest(
    IN  HTXAL    hal,
    IN  RvUint8  *pbuf,
    IN  RvUint16 size,
    OUT RvUint16 *pOccupiedSlots);


/******************************************************************************
 * RvH223TxAlClear
 * ----------------------------------------------------------------------------
 * General: Clear the channel's send queues.
 *
 * Return Value: RV_OK - if successful.
 *               RV_ERROR_BADPARAM - if the handle is invalid.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:   hal             - Handle to a TXAL instance.
 * Output:  None
 *****************************************************************************/
RvStatus RvH223TxAlClear(
    IN  HTXAL    hal);


#ifdef __cplusplus
}
#endif

#endif /* _RV_H223_TXAL_IFC_H */
