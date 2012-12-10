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
        Copyright (c) 2005 RADVISION Inc. and RADVISION Ltd.
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
#ifndef _RVH223_RCPC_H_
#define _RVH223_RCPC_H_

#include "rvtypes.h"

#ifdef __cplusplus
extern "C" {
#endif

#define DECODING_DEPTH      280     /* In bits, must be multiple of 8
                                       Note: This can be changed to any value and
                                       the algorithm will still work. The longer it
                                       is, the better it is for error correction. But
                                       it also increases the decoding chunks implementable */
#define MOTHER_RATE         4
#define OUT_BITS            4   /* MOTHER_RATE */

#define MEMORY_SIZE         4   /* in bits */
#define NUM_STATES          16  /* 1 << MEMORY_SIZE */

#define INPUT_SYMBOL_BASE   2   /* Input alphabet is binary */

#define FIRST_PUNCT_RATE    8
#define LAST_PUNCT_RATE     32
#define NUM_PUNCT_RATES     (LAST_PUNCT_RATE - FIRST_PUNCT_RATE + 1)

#define TAIL_LENGTH         4

#define ANTI_TAIL_MASK      0xf0  /* 0xff << TAIL_LENGTH */

/* Note: All data lengths for all routines are specified in bits, _never_ in bytes.
   To round up to bytes: "byte_len = (bit_len+7) >> 3" */

int RvH223CalculateRcpcAlPdu  (IN RvUint16 source_bit_len,
                               IN RvUint8  puncture_rate,
                               IN RvUint16 bit_offset_within_source);
/* When encoding in parts, make sure you update the "bit_offset_within_source" from call to call, starting from 0
   This routine also works for the RCPC_encode_tail, by specifying source_bit_len=TAIL_BITS
   Alternatively, the total encoded length can be computed by
   calc_RCPC_encoded_data_bit_length (length_part_1 + length_part2 + .. + TAIL_BITS, puncture_rate, 0) */

RvUint32 RvH223CalculateRcpcAlSdu  (IN RvUint32 received_bit_len,
                               IN RvUint8  puncture_rate);
/* Returns the length of the entire decoded bit stream (length_part_1 + length_part2 + .. + TAIL_BITS) */


int RvH223RcpcEncode(IN  RvUint8        data_rate,
                     IN  const RvUint8 *source_data,
                     IN  RvUint16       first_source_bit_index,
                     IN  RvUint16       source_bits_to_encode,
                     IN  RvUint8       *target_data,
                     IN  RvUint16       first_target_bit_index,
                     IN  RvUint16       empty_bits_in_target_buffer,
                     IN  RvUint32       in_state,
                     OUT RvUint32      *out_state,
                     OUT RvUint16      *in_out_source_message_global_bit_pos,
                     OUT RvUint16      *next_first_source_bit_index,
                     OUT RvUint16      *next_first_target_bit_index);

int RvH223RcpcEncodeTail(IN  RvUint8    data_rate,
                         IN  RvUint8   *target_data,
                         IN  RvUint16   first_target_bit_index,
                         IN  RvUint16   empty_bits_in_target_buffer,
                         IN  RvUint32   in_state,
                         OUT RvUint16  *in_out_source_message_global_bit_pos,
                         OUT RvUint16  *next_first_target_bit_index);

/* Notes:
 1. Return values
        0       ok - encoded everything
        1       Missing space in output buffer
 2. Make sure there is enough room for the tail encoding (or re-do it if it fails, but realize it
    cannot be done in parts). To see how much space is needed call
    calc_bit_len_of_RCPC_AL_PDU(TAIL_LENGTH, data_rate,in_out_source_message_global_bit_pos)
 3. The RCPC_decode needs extra space for TAIL_LENGTH bits in the output buffer. */
void RvH223RcpcDecode(IN  RvUint8   data_rate,
                      IN  RvUint8  *received_data,
                      IN  RvUint16  received_bit_len,
                      OUT RvUint8  *decoded_data,
                      OUT RvUint16 *decoded_bit_len_including_tail);

#ifdef __cplusplus
}
#endif

#endif  /* _RVH223_RCPC_H_ */
