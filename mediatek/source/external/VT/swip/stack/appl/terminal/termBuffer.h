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

/***********************************************************************
Copyright (c) 2003 RADVISION Ltd.
************************************************************************
NOTICE:
This document contains information that is confidential and proprietary
to RADVISION Ltd.. No part of this document may be reproduced in any
form whatsoever without written prior approval by RADVISION Ltd..
RADVISION Ltd. reserve the right to revise this publication and make
changes without obligation to notify any person of such revisions or
changes.
***********************************************************************/

#ifndef _RV_TERM_BUFFER_H_
#define _RV_TERM_BUFFER_H_

/***********************************************************************
termBuffer.h

Buffer handling routines.
***********************************************************************/


/*-----------------------------------------------------------------------*/
/*                        INCLUDE HEADER FILES                           */
/*-----------------------------------------------------------------------*/
#include "termDefs.h"




#ifdef __cplusplus
extern "C" {
#endif



/*-----------------------------------------------------------------------*/
/*                           TYPE DEFINITIONS                            */
/*-----------------------------------------------------------------------*/





/*-----------------------------------------------------------------------*/
/*                           FUNCTIONS HEADERS                           */
/*-----------------------------------------------------------------------*/


/******************************************************************************
 * termBufferInit
 * ----------------------------------------------------------------------------
 * General: Initialize a buffer object.
 *
 * Return Value: Buffer allocation that was generated.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  term             - Terminal object used.
 *         minSendSize      - Minimum size of bytes needed for sending data.
 *         cyclicBufferSize - Size of cyclic buffer to use in bytes.
 *         anyPending       - RV_TRUE if we support pending in this buffer.
 *         bSupportErrors   - RV_TRUE to support errors insertion to this
 *                            buffer.
 * Output: None
 *****************************************************************************/
BufferHANDLE termBufferInit(
    IN TermObj  *term,
    IN RvUint32 minSendSize,
    IN RvUint32 cyclicBufferSize,
    IN RvBool   anyPending,
    IN RvBool   bSupportErrors);


/******************************************************************************
 * termBufferEnd
 * ----------------------------------------------------------------------------
 * General: Deinitialize a buffer.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  buf  - Buffer object
 * Output: None
 *****************************************************************************/
RvStatus termBufferEnd(IN BufferHANDLE buf);


/******************************************************************************
 * termBufferGetForRead
 * ----------------------------------------------------------------------------
 * General: Get from the buffer the data available for reading.
 *          This will return the data linearly, and doesn't insure that no
 *          more data is available.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  buf  - Buffer object
 * Output: data - Data pointer
 *         size - Amount of data available for reading at the data pointer
 *****************************************************************************/
RvStatus termBufferGetForRead(
    IN  BufferHANDLE    buf,
    OUT RvUint8         **data,
    OUT RvUint32        *size);


/******************************************************************************
 * termBufferBytesRead
 * ----------------------------------------------------------------------------
 * General: Indicate the amount of data we read from the buffer up till now.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  buf          - Buffer object
 *         bytesRead    - Number of bytes we read
 * Output: None
 *****************************************************************************/
RvStatus termBufferBytesRead(
    IN BufferHANDLE buf,
    IN RvUint32     bytesRead);


/******************************************************************************
 * termBufferBytesReleasePending
 * ----------------------------------------------------------------------------
 * General: Indicate the amount of data we read and acked from the buffer up
 *          till now.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  buf          - Buffer object
 *         bytesRead    - Number of bytes we read
 * Output: None
 *****************************************************************************/
RvStatus termBufferBytesReleasePending(
    IN BufferHANDLE buf,
    IN RvUint32     bytesRead);


/******************************************************************************
 * termBufferGetForWrite
 * ----------------------------------------------------------------------------
 * General: Get from the buffer a place to write information to.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  buf  - Buffer object
 * Output: data - Data pointer
 *         size - Amount of data available for writing at the data pointer
 *****************************************************************************/
RvStatus termBufferGetForWrite(
    IN  BufferHANDLE    buf,
    OUT RvUint8         **data,
    OUT RvUint32        *size);


/******************************************************************************
 * termBufferBytesWritten
 * ----------------------------------------------------------------------------
 * General: Indicate the amount of data we wrote into the buffer up till now.
 *
 * Return Value: RV_OK on success, other on failure.
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  buf          - Buffer object
 *         bytesWritten - Number of bytes we wrote
 * Output: None
 *****************************************************************************/
RvStatus termBufferBytesWritten(
    IN BufferHANDLE buf,
    IN RvUint32     bytesWritten);


/******************************************************************************
 * termBufferInsertErrors
 * ----------------------------------------------------------------------------
 * General: Simulate receiving buffers with errors in BER of 1/maxRandNumber.
 *
 * Return Value: None
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  buffer       - The receiving buffer.
 *         bufSize      - The size of the buffer.
 *         ber          - 1/bit error rate.
 *         ptrn         - Error pattern to use if applicable.
 * Output: none
 *****************************************************************************/
void termBufferInsertErrors(
    IN RvUint8          *buffer,
    IN RvUint32         bufSize,
    IN RvUint32         ber,
    IN TermErrorPattern *ptrn);


/******************************************************************************
 * termBufferSetTerm
 * ----------------------------------------------------------------------------
 * General: Sets the terminal handle in the buffer object's data.
 *
 * Return Value: None
 * ----------------------------------------------------------------------------
 * Arguments:
 * Input:  buff          - The bufferHandle.
 *         term          - The terminal object.
 * Output: none
 *****************************************************************************/
void termBufferSetTerm(
    IN BufferHANDLE buf, 
    IN TermObj*     term);




#ifdef __cplusplus
}
#endif

#endif /* _RV_TERM_BUFFER_H_ */
