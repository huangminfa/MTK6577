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

#ifndef _RV_H223_EGOLAY_H_
#define _RV_H223_EGOLAY_H_

/* GOLAY (23, 12, 7) and Extended GOLAY (24, 12, 8) code implementations

 References: *	http://mathworld.wolfram.com/GolayCode.html
 *	http://planetmath.org/encyclopedia/ExtendedBinaryGolayCode.html
 *    http://www.maths.ex.ac.uk/~rjc/etc/golay11.pdf 
 *    John Wiley - The Art of Error Correcting Coding

 GOLAY (X,Y,Z): There are 2^Y codewords, each X bits long, which are different from
                each other by at least Z bits.
 The EGOLAY(X,Y,Z) code is a GOLAY(X-1,Y,Z-1) code with an added parity bit.
 Parity is defined at (mod 2) sum of the bits in the code (even 1s --> zero parity)

 Requirements for the implementation:
 Memory:
		EGOLAY_LOOKUP table:	8K byte
		EGOLAY_DATA_CORRECTION: 8K byte
 CPU:
      Minimal (table lookup)

 Note: Code for computing content of lookup tables is supplied, but commented out
*/

#include "rvtypes.h"

#ifdef __cplusplus
extern "C" {
#endif

#define EGOLAY_OK			 0x00
#define EGOLAY_FIXED	 	 0x01
#define EGOLAY_UNFIXABLE	 0xff


RvUint16 RvH223EGolayEncode (IN  RvUint16  data);
RvUint8  RvH223EGolayDecode (IN  RvUint16  received_data,
                             IN  RvUint16  received_egolay,
                             OUT RvUint16 *corrected_data);



#ifdef __cplusplus
}
#endif


#endif /* _RV_H223_EGOLAY_H_ */
