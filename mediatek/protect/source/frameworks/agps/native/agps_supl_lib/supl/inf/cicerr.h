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

/* $Id: cicerr.h,v 1.12 2005/08/30 14:04:34 sroberts Exp $
 * $Source: /cvs/ca/eng/consec/sbi/src/cic/include/cicerr.h,v $
 * $Name: hu-2_6_30 $
 */
#ifndef CICERR_H
#define CICERR_H

#include "sbreturn.h"

#ifdef __cplusplus
extern "C" {
#endif

/*
    General error codes.

    These are general error codes that exist in most products.
    All general errors are in the range (0x0000 - 0x0FFFF)
*/
#define CIC_ERR_NONE                        0x0000  /**< No error */
#define CIC_ERR_NO_PTR                      0x0001  /**< Null pointer */
#define CIC_ERR_ILLEGAL_PARAM               0x0002  /**< Illegal param */
#define CIC_ERR_SMALL_BUFFER                0x0003  /**< Buffer too small */
#define CIC_ERR_WOULD_BLOCK                 0x0004  /**< IO Blocking */
#define CIC_ERR_TIMEOUT                     0x0005  /**< Timeout */
#define CIC_ERR_BAD_LENGTH                  0x0006  /**< Bad length */
#define CIC_ERR_NOT_FOUND                   0x0007  /**< Not found */
#define CIC_ERR_BAD_CTX                     0x0008  /**< Bad context */
#define CIC_ERR_BAD_INDEX                   0x0009  /**< Bad index */
#define CIC_ERR_RANDOM                      0x000A  /**< Entropy generation */
#define CIC_ERR_MEM_UNDERRUN                0x000B  /**< Memory underrun detected by memory test harness. */
#define CIC_ERR_MEM_OVERRUN                 0x000C  /**< Memory overrun detected by memory test harness. */
#define CIC_ERR_MEM_WAS_FREED               0x000D  /**< Duplicate free detected by the memory test harness. */
#define CIC_ERR_MEM_NOT_OURS                0x000E  /**< Attempt to free unallocated memory detected by test harness. */
#define CIC_ERR_MEM_ZERO_ALLOCED            0x000F  /**< Attempt to allocate zero bytes of memory. */
#define CIC_ERR_NOT_IMPLEMENTED             0x0FFE  /**< Functionality not implemented */
#define CIC_ERR_INTERNAL                    0x0FFF  /**< Some unknown internal error */

#define CIC_ERR_CODING_BAD_ENCODING  0x0221
#define CIC_ERR_BASE64_BAD_ENCODING  CIC_ERR_CODING_BAD_ENCODING
#define CIC_ERR_CODING_BAD_PEM       0x0222
#define CIC_ERR_BASE64_BAD_PEM       CIC_ERR_CODING_BAD_PEM

/*
    General error codes that are identical to Security Builder errors.

    These are general error codes that exist in most products and the value of the
    error code is identical to an error code from Security Builder.
*/
#define CIC_ERR_MEMORY                      SB_FAIL_ALLOC  /**< SB_FAIL_ALLOC Allocation error */

#ifdef __cplusplus
}
#endif

#endif

