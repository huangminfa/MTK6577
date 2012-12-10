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

/*[
 *      Project:    	    OMC
 *
 *      Name:				xpt-b64.h
 *
 *      Derived From:		Original
 *
 *      Created On:			May 2004
 *
 *      Version:			$Id: //depot/main/base/syncml/xpt/bindings/common/xpt-b64.h#3 $
 *
 *      Coding Standards:	3.0
 *
 *      Purpose:            SyncML core code
 *
 *      (c) Copyright Insignia Solutions plc, 2004
 *
]*/

/**
 * @file
 * Communication Services, base64 encoding/decoding fns.
 *
 * @target_system   all
 * @target_os       all
 * @description function prototypes and return codes
 * for base64 encoding/ decoding functions.
 */


/*
 * Copyright Notice
 * Copyright (c) Ericsson, IBM, Lotus, Matsushita Communication
 * Industrial Co., Ltd., Motorola, Nokia, Openwave Systems, Inc.,
 * Palm, Inc., Psion, Starfish Software, Symbian, Ltd. (2001).
 * All Rights Reserved.
 * Implementation of all or part of any Specification may require
 * licenses under third party intellectual property rights,
 * including without limitation, patent rights (such a third party
 * may or may not be a Supporter). The Sponsors of the Specification
 * are not responsible and shall not be held responsible in any
 * manner for identifying or failing to identify any or all such
 * third party intellectual property rights.
 *
 * THIS DOCUMENT AND THE INFORMATION CONTAINED HEREIN ARE PROVIDED
 * ON AN "AS IS" BASIS WITHOUT WARRANTY OF ANY KIND AND ERICSSON, IBM,
 * LOTUS, MATSUSHITA COMMUNICATION INDUSTRIAL CO. LTD, MOTOROLA,
 * NOKIA, PALM INC., PSION, STARFISH SOFTWARE AND ALL OTHER SYNCML
 * SPONSORS DISCLAIM ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO ANY WARRANTY THAT THE USE OF THE INFORMATION
 * HEREIN WILL NOT INFRINGE ANY RIGHTS OR ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT
 * SHALL ERICSSON, IBM, LOTUS, MATSUSHITA COMMUNICATION INDUSTRIAL CO.,
 * LTD, MOTOROLA, NOKIA, PALM INC., PSION, STARFISH SOFTWARE OR ANY
 * OTHER SYNCML SPONSOR BE LIABLE TO ANY PARTY FOR ANY LOSS OF
 * PROFITS, LOSS OF BUSINESS, LOSS OF USE OF DATA, INTERRUPTION OF
 * BUSINESS, OR FOR DIRECT, INDIRECT, SPECIAL OR EXEMPLARY, INCIDENTAL,
 * PUNITIVE OR CONSEQUENTIAL DAMAGES OF ANY KIND IN CONNECTION WITH
 * THIS DOCUMENT OR THE INFORMATION CONTAINED HEREIN, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH LOSS OR DAMAGE.
 *
 * The above notice and this paragraph must be included on all copies
 * of this document that are made.
 *
 */

#ifndef XPT_B64_H
#define XPT_B64_H

#include <syncml/xpt/bindings/common/tcp/xpttypes.h>

/**
 * Precalculates the size of an encoded document with the given size
 *
 * @pre The function is called to get the size of the document that
 *      will be encoded with the base64Encode() service.
 * @param cbRealDataSize (IN)
 *        the size of the non-encoded document.
 * @return the size of the encoded document that will be generated
 *         using the base64Encode() service.
 */
BufferSize_t base64GetSize (BufferSize_t cbRealDataSize);

/**
 * Encodes a chunk of data according to the base64 encoding rules
 *
 * @pre A chunk of data os copied to the source data buffer pbData, and the
 *      length of the data chunk is specified in *pcbDataLength;
 * @post A block of encoded data is available in the specified target buffer.
 *       The length of the encoded data is returned by the function.
 * @param pbTarget (IN)
 *        pointer to an allocated chunk of memory that receives the
 *        encoded data block.
 * @param cbTargetSize (IN)
 *        size of the data buffer above.
 * @param bLast (IN)
 *        flag that indicates if the specified block is the last part of the
 *        document. If the value is 0, the funciton expects that other blocks
 *        will follow, a value of 1 indicates that the data block that is
 *        presented in the input buffer is the last data block to be encoded.
 * @param pbSavebytes (IN)
 *        pointer to a block of at least 3 Bytes. When this function is invoked
 *        the first time, the first byte of this buffer MUST be set to 0.
 * @param pbData (IN/OUT)
 *        pointer to a data block that contains the clear data that are to be
 *        encoded. On return, the remaining piece of the input data block that
 *        could not be encoded is copied to the memory that pbData points at.
 * @param pcbDataLength (IN/OUT)
 *        pointer to a variable that denotes the length of the data block
 *        that is to be encoded, The function updates this value
 *        with the size of the data block that could not be processed.
 *        If all data were able to be encoded, the value will be 0.
 * @param pcbOffset (IN/OUT)
 *        pointer to a variable that is internally used by the function.
 *        before the first call of base64encode() for a certain document is
 *        made, the variable that pcbOffset points at must be set to 0.
 *        The variable will be updated by the function, and should not be
 *        modified by the caller.
 * @return the size of the data block that are available in pbTarget.
 */
BufferSize_t base64Encode (DataBuffer_t pbTarget,
                     BufferSize_t cbTargetSize,
                     DataBuffer_t pbData,
                     BufferSize_t *pcbDataLength,
                     BufferSize_t *pcbOffset,
                     unsigned int bLast,
                     unsigned char *pbSavebytes);
/**
 * Decodes a chunk of data according to the base64 encoding rules
 *
 * @pre A chunk of data os copied to the source data buffer pbData, and the
 *      length of the data chunk is specified in *pcbDataLength;
 * @post A block of decoded data is available in the specified target buffer.
 *       The length of the decoded data is returned by the function.
 * @param pbTarget (IN)
 *        pointer to an allocated chunk of memory
 *        that receives the decoded data block.
 * @param cbTargetSize (IN)
 *        size of the data buffer above.
 * @param pbData (IN/OUT)
 *        pointer to a data block that contains the clear data that are to be
 *        decoded. On return, the remaining piece of the input data block
 *        that could not be decoded is copied to the memory
 *        that pbData points at.
 * @param pcbDataLength (IN/OUT)
 *        pointer to a variable that denotes the length of the data block
 *        that is to be decoded, The function updates this value with
 *        the size of the data block that could not be processed.
 *        If all data were able to be decoded, the value will be 0.
 * @return the size of the data block that are available in pbTarget.
 *         If some invalid data were detected in the input data buffer,
 *         or if cbTargetSize is less than 3, the function returns 0.
 *         The caller should treat this as an error condition.
 */
BufferSize_t base64Decode (DataBuffer_t pbTarget,
                     BufferSize_t cbTargetSize,
                     DataBuffer_t pbData,
                     BufferSize_t *pcbDataLength);


#endif

