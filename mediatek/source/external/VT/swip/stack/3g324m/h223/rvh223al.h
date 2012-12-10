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

#ifndef _RV_H223_AL_H
#define _RV_H223_AL_H

#ifdef __cplusplus
extern "C" {
#endif

/*---------------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                                */
/*---------------------------------------------------------------------------*/

#define AL2_SEQUENCE_NUMBER_MAX         0x100
#define AL3_SHORT_SEQUENCE_NUMBER_MAX   0x80
#define AL3_LONG_SEQUENCE_NUMBER_MAX    0x8000
#define ALXM_SEBCH_SEQUENCE_NUMBER_MAX  0x20
#define AL2M_GOLAY_SEQUENCE_NUMBER_MAX  0x1000
#define AL1M_GOLAY_SEQUENCE_NUMBER_MAX  0x400

#define SEBCH_CF_SIZE	0x2
#define GOLAY_CF_SIZE	0x3
#define GET_PAYLOAD_TYPE(x) ((ERtxPayloadType)((x) & PATTERN_1))

typedef enum{
    RTX_REQ_RTX = 0,
    RTX_REQ_SREJ,
    RTX_RESP_DRTX
}ERtxReqCode;

typedef enum{
    RTX_TYPE_SPDU = 0,
    RTX_TYPE_IPDU = 1
}ERtxPayloadType;

#if (RV_H223_USE_AL3_RETRANSMISSIONS == RV_YES)
/******************************************************************************
 * RvH223TxAlMuxDataRequest
 * ----------------------------------------------------------------------------
 * General: Sends user data.
 *
 * Return Value: RV_OK  - if successful.
 *               RV_ERROR_OUTOFRESOURCES - if the retransmission request queue is full.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:   hal   - Handle to a TXAL instance.
 *          ERtxReqCode  - Retransmission request code.
 *          sequenceNumber - Sequence number for SPDU creation.
 *****************************************************************************/
RvStatus RvH223TxAlSpduRequest (IN HTXAL hal,
                                IN ERtxReqCode code,
                                IN RvUint16 sequenceNumber);
#endif /* (RV_H223_USE_AL3_RETRANSMISSIONS == RV_YES) */

#ifdef __cplusplus
}
#endif

#endif /* _RV_H223_AL_H */
