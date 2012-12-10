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
 *      Name:				smlerr.h
 *
 *      Derived From:		Original
 *
 *      Created On:			May 2004
 *
 *      Version:			$Id: //depot/main/base/syncml/sml/smlerr.h#5 $
 *
 *      Coding Standards:	3.0
 *
 *      Purpose:            SyncML core code
 *
 *      (c) Copyright Insignia Solutions plc, 2004 - 2006
 *
]*/

/**
 * @file
 * ErrorLibrary
 *
 * @target_system   all
 * @target_os       all
 * @description Definition of the used Error Codes
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


#ifndef _SML_ERR_H
  #define _SML_ERR_H

#include <omc/omc_error.h>

/*************************************************************************
 *  Definitions
 *************************************************************************/

/*
 * No error, success code
 */
#define SML_ERR_OK                          OMC_ERR_OK /**< OK */


/*
 * General Error Codes
 */

/** unspecific error */
#define SML_ERR_UNSPECIFIC                  OMC_ERR_UNSPECIFIC

/** not enough memory to perform this operation */
#define SML_ERR_NOT_ENOUGH_SPACE            OMC_ERR_MEMORY

/*
 * SyncML Common Error Codes
 */

/** function was called in wrong context */
#define SML_ERR_WRONG_USAGE                 (OMC_ERR_GENERAL_SYNCML + 0x01)

/** wrong parameter */
#define SML_ERR_WRONG_PARAM                 OMC_ERR_BAD_INPUT

/** param has an invalid size */
#define SML_ERR_INVALID_SIZE                (OMC_ERR_GENERAL_SYNCML + 0x02)

/** if handle is invalid/unknown */
#define SML_ERR_INVALID_HANDLE              (OMC_ERR_GENERAL_SYNCML + 0x03)

/** unknown or unallowed options */
#define SML_ERR_INVALID_OPTIONS             (OMC_ERR_GENERAL_SYNCML + 0x04)


/*
 * SyncML Mgr Error Codes
 */

/** a template */
#define SML_ERR_A_MGR_ERROR                 (OMC_ERR_TYPE_SYNCML + 0x00)

/** a invalid Instance Info structure is used */
#define SML_ERR_MGR_INVALID_INSTANCE_INFO   (OMC_ERR_TYPE_SYNCML + 0x01)

/** no callback function is available to handle this command */
#define SML_ERR_COMMAND_NOT_HANDLED         (OMC_ERR_TYPE_SYNCML + 0x02)

/** Mgr allready initialized */
#define SML_ERR_ALREADY_INITIALIZED         (OMC_ERR_TYPE_SYNCML + 0x03)


/*
 * SyncML Xlt Error Codes
 */

/** Required field content missing */
#define SML_ERR_XLT_MISSING_CONT            (OMC_ERR_TYPE_SYNCML + 0x10)

/** Buffer too small  */
#define SML_ERR_XLT_BUF_ERR                 OMC_ERR_MESSAGE_TOO_LONG

/** Invalid (WBXML) Element Type (STR_I etc.) */
#define SML_ERR_XLT_INVAL_PCDATA_TYPE       (OMC_ERR_TYPE_SYNCML + 0x11)

/** Invalid List Type (COL_LIST etc.) */
#define SML_ERR_XLT_INVAL_LIST_TYPE         (OMC_ERR_TYPE_SYNCML + 0x12)

/** Invalid Tag Type (TT_BEG etc.) */
#define SML_ERR_XLT_INVAL_TAG_TYPE          (OMC_ERR_TYPE_SYNCML + 0x13)

/** Unknown Encoding (WBXML, XML) */
#define SML_ERR_XLT_ENC_UNK	                (OMC_ERR_TYPE_SYNCML + 0x14)

/** Invalid Protocol Element (ADD, Delete, ...) */
#define SML_ERR_XLT_INVAL_PROTO_ELEM        (OMC_ERR_TYPE_SYNCML + 0x15)

/** Missing Content of List Elements  */
#define SML_ERR_MISSING_LIST_ELEM           (OMC_ERR_TYPE_SYNCML + 0x16)

/** Incompatible WBXML Content Format Version */
#define SML_ERR_XLT_INCOMP_WBXML_VERS       (OMC_ERR_TYPE_SYNCML + 0x17)

/** Document does not conform to SyncML DTD  */
#define SML_ERR_XLT_INVAL_SYNCML_DOC        (OMC_ERR_TYPE_SYNCML + 0x18)

/** Invalid PCData elem (e.g. not encoded as OPAQUE data) */
#define SML_ERR_XLT_INVAL_PCDATA            (OMC_ERR_TYPE_SYNCML + 0x19)

/** Unspecified tokenizer error */
#define SML_ERR_XLT_TOKENIZER_ERROR         (OMC_ERR_TYPE_SYNCML + 0x1A)

/** Document does not conform to WBXML specification */
#define SML_ERR_XLT_INVAL_WBXML_DOC         (OMC_ERR_TYPE_SYNCML + 0x1B)

/** Document contains unknown WBXML token */
#define SML_ERR_XLT_WBXML_UKN_TOK           (OMC_ERR_TYPE_SYNCML + 0x1C)

/** Non-empty start tag without matching end tag */
#define SML_ERR_XLT_MISSING_END_TAG         (OMC_ERR_TYPE_SYNCML + 0x1D)

/** WBXML document uses unspecified code page */
#define SML_ERR_XLT_INVALID_CODEPAGE        (OMC_ERR_TYPE_SYNCML + 0x1E)

/** End of buffer reached */
#define SML_ERR_XLT_END_OF_BUFFER           (OMC_ERR_TYPE_SYNCML + 0x1F)

/** Document does not conform to XML 1.0 specification */
#define SML_ERR_XLT_INVAL_XML_DOC           (OMC_ERR_TYPE_SYNCML + 0x20)

/** Document contains unknown XML tag */
#define SML_ERR_XLT_XML_UKN_TAG             (OMC_ERR_TYPE_SYNCML + 0x21)

/** Invalid Public Identifier */
#define SML_ERR_XLT_INVAL_PUB_IDENT         (OMC_ERR_TYPE_SYNCML + 0x22)

/** Invalid Codepage Extension */
#define SML_ERR_XLT_INVAL_EXT               (OMC_ERR_TYPE_SYNCML + 0x23)

/** No matching Codepage could be found */
#define SML_ERR_XLT_NO_MATCHING_CODEPAGE    (OMC_ERR_TYPE_SYNCML + 0x24)

/** Data missing in input structure */
#define SML_ERR_XLT_INVAL_INPUT_DATA        (OMC_ERR_TYPE_SYNCML + 0x25)

/** Data supplied for wrong SyncML version */
#define SML_ERR_XLT_VERSION_DATA            (OMC_ERR_TYPE_SYNCML + 0x26)


/*
 * SyncML Wsm Error Codes
 */

/** no more empty entries in buffer table available */
#define SML_ERR_WSM_BUF_TABLE_FULL          (OMC_ERR_TYPE_SYNCML + 0x30)

/*
 * SyncML Util Error Codes
 */

/** Unknown type of protocol element */
#define SML_ERR_A_UTI_UNKNOWN_PROTO_ELEMENT (OMC_ERR_TYPE_SYNCML + 0x40)


#endif

