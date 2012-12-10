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

/* rvnet2host.h - converst network/host organized header bytes */

/************************************************************************
        Copyright (c) 2002 RADVISION Inc. and RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Inc. and RADVISION Ltd.. No part of this document may be
reproduced in any form whatsoever without written prior approval by
RADVISION Inc. or RADVISION Ltd..

RADVISION Inc. and RADVISION Ltd. reserve the right to revise this
publication and make changes without obligation to notify any person of
such revisions or changes.
***********************************************************************/

#if !defined(RV_NET2HOST_H)
#define RV_NET2HOST_H

#ifdef __cplusplus
extern "C" {
#endif

#include "rvtypes.h"


/* Macros for conversion of byte ordering */
#if (RV_ARCH_ENDIAN == RV_ARCH_LITTLE_ENDIAN)

/* Always use these macros with a variable - never pass an argument or a function call as a
   parameter of these macros! */
/************************************************************************************************************************
 * RvConvertHostToNetwork64
 *
 * Converts a 64bit integer in host order to an integer in a network format.
 *
 * INPUT   :  host       : value to convert.
 * OUTPUT  :  None.
 * RETURN  :  The converted value.
 */
RVCOREAPI
RvUint64 RVCALLCONV RvConvertHostToNetwork64(RvUint64 host);

/************************************************************************************************************************
 * RvConvertHostToNetwork32
 *
 * Converts an integer in host order to an integer in a network format.
 *
 * INPUT   :  host       : value to convert.
 * OUTPUT  :  None.
 * RETURN  :  The converted value.
 */
RVCOREAPI
RvUint32 RVCALLCONV RvConvertHostToNetwork32(RvUint32 host);

/************************************************************************************************************************
 * RvConvertHostToNetwork16
 *
 * Converts a short integer in host order to a short integer in a network format.
 *
 * INPUT   :  host       : value to convert.
 * OUTPUT  :  None.
 * RETURN  :  The converted value.
 */
RVCOREAPI
RvUint16 RVCALLCONV RvConvertHostToNetwork16(RvUint16 host);

#define RvConvertNetworkToHost64(_network) RvConvertHostToNetwork64(_network)
#define RvConvertNetworkToHost32(_network) RvConvertHostToNetwork32(_network)
#define RvConvertNetworkToHost16(_network) RvConvertHostToNetwork16(_network)

#elif (RV_ARCH_ENDIAN == RV_ARCH_BIG_ENDIAN)

#define RvConvertHostToNetwork64(_host) (_host)
#define RvConvertNetworkToHost64(_network) (_network)
#define RvConvertHostToNetwork32(_host) (_host)
#define RvConvertNetworkToHost32(_network) (_network)
#define RvConvertHostToNetwork16(_host) (_host)
#define RvConvertNetworkToHost16(_network) (_network)

#endif

	
/************************************************************************************************************************
 * RvNet2Host2Network
 *
 * Converts an array of 4-byte integers from host format to network format.  The integers can
 * then be sent over the network.
 *
 * INPUT   :  buff       : A pointer to the buffer location where the array of 4-byte integers in host format are located.
 *            startIndex : The exact byte location in the buffer where the integers in host format begin.
 *            size       : The number of integers to convert.
 * OUTPUT  :  None.
 * RETURN  :  None
 */
RVCOREAPI
void RVCALLCONV RvNet2Host2Network(
	IN RvUint8	*buff,
	IN RvInt	startIndex,
	IN RvInt	size);


/***********************************************************************************************************************
 * RvNet2Host2Host
 * Converts an array of 4-byte integers from network format to host format.
 * INPUT   : buff       : A pointer to the buffer location where the array of 4-byte integers in network format are located,
 *                        and where the conversion occurs.
 *           startIndex : The exact byte location in the buffer where the integers in network format begin.
 *           size       : The number of integers to convert.
 * OUTPUT  : None.
 * RETURN  : None.
 */
RVCOREAPI
void RVCALLCONV RvNet2Host2Host(
	IN RvUint8	*buff,
	IN RvInt	startIndex,
	IN RvInt	size);

#ifdef __cplusplus
}
#endif

#endif

