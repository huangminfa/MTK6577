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

/*
***********************************************************************************

NOTICE:
This document contains information that is proprietary to RADVISION LTD..
No part of this publication may be reproduced in any form whatsoever without
written prior approval by RADVISION LTD..

RADVISION LTD. reserves the right to revise this publication and make changes
without obligation to notify any person of such revisions or changes.

***********************************************************************************
*/

/*
  copybits.h

  */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef _COPYBITS_
#define _COPYBITS_

#include "rvtypes.h"

#define getBit(ptr, bit) \
    ((unsigned int)(((RvUint8 *)(ptr))[((RvUint32)(bit))>>3] & (0x80 >> ((RvUint32)(bit) & 7))))


/************************************************************************
 * setBit
 * purpose: This marco is used to set a bit within a bits array to a RV_TRUE
 *          or RV_FALSE value
 * syntex : void setBit(RvUint8 *ptr, RvUint32 bit, unsigned int value);
 * input  : bitsBuffer  - Pointer to bits buffer
 *          bitIndex    - Bit to change (0-based)
 *          bitValue    - RV_TRUE/RV_FALSE value of the bit
 * output : none
 * return : none
 ************************************************************************/
#define setBit(bitsBuffer, bitIndex, bitValue)  \
{                                                                                       \
    if ((unsigned int)(bitValue))                                                       \
        ((RvUint8*)(bitsBuffer))[((RvUint32)(bitIndex)) >> 3] |=                        \
            (0x80 >> (((RvUint32)(bitIndex)) & 7));                                     \
    else                                                                                \
        ((RvUint8*)(bitsBuffer))[((RvUint32)(bitIndex)) >> 3] &=                        \
            ~(0x80 >> (((RvUint32)(bitIndex)) & 7));                                    \
}

#define setBitTrue(bitsBuffer, bitIndex) \
{                                                                                       \
        ((RvUint8*)(bitsBuffer))[((RvUint32)(bitIndex)) >> 3] |=                        \
            (0x80 >> (((RvUint32)(bitIndex)) & 7));                                     \
}

#define setBitFalse(bitsBuffer, bitIndex) \
{                                                                                       \
        ((RvUint8*)(bitsBuffer))[((RvUint32)(bitIndex)) >> 3] &=                        \
            ~(0x80 >> (((RvUint32)(bitIndex)) & 7));                                    \
}



/* copy bits from src to dest */
void memcpyb(
         OUT RvUint8 *dest,
         IN  RvUint32 destBitPos,
         IN  RvUint8 *src,
         IN  RvUint32 srcBitPos,
         IN  RvUint32 numOfBits);



/************************************************************************
 * get1st0BitNumber
 * purpose: Find the first bit set to 0 within an array of bits.
 *          This function is used by H.245 for handling the session IDs
 *          and the logical channel numbers of faststart channels.
 * input  : bitsBuffer  - Pointer to bits buffer
 *          startBit    - First bit to look from
 *          stopBit     - Last bit in the buffer
 * output : none
 * return : The index of the first bit set to 0 in the array of bits
 *          It will return stopBit+1 if no 0 bit was found
 ************************************************************************/
RVINTAPI RvUint32 RVCALLCONV
get1st0BitNumber(
    IN RvUint8* bitsBuffer,
    IN RvUint32 startBit,
    IN RvUint32 stopBit);



#endif

#ifdef __cplusplus
}
#endif



