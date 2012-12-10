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

#ifndef _RV_H223_UTILS_H
#define _RV_H223_UTILS_H

#ifdef __cplusplus
extern "C" {
#endif

/*---------------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                               */
/*---------------------------------------------------------------------------*/

/******************************************************************************
 * RvH223CalculateCRC16
 * ----------------------------------------------------------------------------
 * General: Calculates CRC16.
 *
 * Return Value: CRC16.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  	reg     - Initial value of register.
 *          buffer  - Data buffer.
 *          size    - Size of buffer.
 *          xorout  - Flag indicating whether we need to XOR the output value.
 *****************************************************************************/
RvUint16 RvH223CalculateCRC16 (RvUint16 reg, RvUint8 *buffer, RvUint32 size, RvBool xorout);

/******************************************************************************
 * RvH223CalculateCRC8
 * ----------------------------------------------------------------------------
 * General: Calculates CRC8.
 *
 * Return Value: CRC.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:   reg     - Initial value of register.
 *          buffer  - Data buffer.
 *          size    - Size of buffer.
 *****************************************************************************/
RvUint8 RvH223CalculateCRC8 (RvUint8 reg, RvUint8 *buffer, RvUint32 size);

/******************************************************************************
 * RvH223CalculateCRC8AndReflect
 * ----------------------------------------------------------------------------
 * General: Calculates CRC8 and reflects the final value.
 *
 * Return Value: CRC.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  	reg     - Initial value of register.
 *          buffer  - Data buffer.
 *          size    - Size of buffer.
 *****************************************************************************/
RvUint8 RvH223CalculateCRC8AndReflect (RvUint8 reg, RvUint8 *buffer, RvUint32 size);

/******************************************************************************
 * RvH223EncodeGolay
 * ----------------------------------------------------------------------------
 * General: Calculates Golay 24-12-8 code.
 *
 * Return Value: Golay code in 12 low bits.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  	data  - 12 low bits.
 *****************************************************************************/
RvUint16 RvH223EncodeGolay (IN RvUint16 data);

/******************************************************************************
 * RvH223DecodeGolay
 * ----------------------------------------------------------------------------
 * General: Decodes Golay 24-12-8 codeword.
 *
 * Return Value: Decoded data in 12 low bits.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  	codeword  - 24 low bits.
 * Output:  errors    - pointer to be filled with the number of errors.
 *****************************************************************************/
RvUint16  RvH223DecodeGolay (RvUint32 codeword, RvUint8 *errors);


#ifdef __cplusplus
}
#endif

#endif /* _RV_H223_UTILS_H */
