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
 *      Name:				xltdeccom.h
 *
 *      Derived From:		Original
 *
 *      Created On:			May 2004
 *
 *      Version:			$Id: //depot/main/base/syncml/sml/xlt/xltdeccom.h#3 $
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
 * XML/WBXML scanner
 *
 * @target_system  all
 * @target_os      all
 * @description Common header file for the WBXML and the XML scanner.
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

/*************************************************************************/
/* Definitions                                                           */
/*************************************************************************/
#ifndef _XLT_DEC_COM_H
#define _XLT_DEC_COM_H

#include <syncml/sml/smldef.h>
#include <syncml/sml/smldtd.h>
#include <syncml/sml/xlt/xlttags.h>

#define DEC_ID          XLT_DEC_ID + 0
#define WBXML_ID        XLT_DEC_ID + 200
#define XML_ID          XLT_DEC_ID + 400
#define STACK_ID        XLT_DEC_ID + 600

#ifdef _cplusplus
extern "C" {
#endif


/* Token types returned by the scanner */
typedef enum {
    TOK_UNDEF = 0,
    TOK_TAG_START,
    TOK_TAG_END,
    TOK_TAG_EMPTY,
    TOK_CONT
} XltTokType_t;

/**
 * Struct containing a token returned by the scanner
 */
typedef struct
{
    XltTokType_t type;        /**< XLT_TOK_TAG_START, etc. */
    XltTagID_t tagid;         /**< TN_SYNCHDR, etc. - valid for token of type
				               * XLT_TOK_TAG_xxx */
    SmlPcdataExtension_t ext; /**< type of extension this tag belongs to -
				               *   valid for token of type XLT_TOK_TAG_xxx */
    SmlPcdataPtr_t pcdata;    /**< valid for token of type XLT_TOK_CONT_xxx */
    MemPtr_t start;           /**< pointer to the start of this token within
				               * the document - needed for pushback */
} XltDecToken_t, *XltDecTokenPtr_t;

/** @copydoc XltDecScanner_s */
typedef struct XltDecScanner_s XltDecScanner_t, *XltDecScannerPtr_t;
/**
 * Public interface for the XML/WBXML scanner components.
 *
 * The struct used for holding the XML and WBXML scanner state information
 * are the same - function pointers are used to map the "objects" public
 * methods to the right functions for scanning XML and WBXML documents.
 * This object-oriented interface enables the parser component to use the
 * two scanners interchangeably.
 * The interface contains serveral public methods and attributes:
 */
struct XltDecScanner_s
{
    /* public methods */
    /**
     * Decodes the next valid token at the current position within the
     * document. Information about this token is placed into the curtok
     * attribute of the scanner object.
     *
     * @post curtok contains the last valid token.
     * @param pScanner (IN/OUT)
     *        the scanner
     * @return SML_ERR_OK or an appropriate error code
     */
    Ret_t (*nextTok)(XltDecScannerPtr_t pScanner);

    /**
     * Free the memory allocated by the scanner.
     *
     * @param pScanner (IN/OUT)
     *        the scanner
     * @return SML_ERR_OK or an appropriate error code
     */
    Ret_t (*destroy)(XltDecScannerPtr_t pScanner);

    /**
     * Resets the scanner to the beginning position within the document of the
     * last valid token stored in curtok. Only the last found token can be
     * pushed back. It is not possible to go back more than one token.
     *
     * @pre curtok contains a valid token.
     * @post the next call of nextTok will find the token that was pushed back.
     * @param pScanner (IN/OUT)
     *        the scanner
     * @return SML_ERR_OK or an appropriate error code
     */
    Ret_t (*pushTok)(XltDecScannerPtr_t pScanner);

    /**
     * Set the buffer the scanner works on.
     */
    void (*setBuf)(XltDecScannerPtr_t pScanner, const MemPtr_t pBufStart, const MemPtr_t pBufEnd);

    /**
     * Get the current position of the scanner within the working buffer.
     */
    MemPtr_t (*getPos)(XltDecScannerPtr_t pScanner);

    /* public attributes */

    /** Contains the last valid token found by a call to nextTok. */
    XltDecTokenPtr_t curtok;

    /**
     * The charset information as specified in the XML/WBXML document.
     * This is  the IANA assigned MIBEnum value.
     */
    Long_t charset;

    /**
     * String representation of the charset. This attribute is valid only when
     * charset equals zero. Otherwise charsetStr will be NULL.
     */
    String_t charsetStr;

    /**
     * The document public identifier as specified in the XML/WBXML document.
     * This is the numeric identifier assigned by the WAP Forum. If this value
     * is zero, the public ID is instead specified as a string contained in the
     * pubIDStr public attribute.
     */
    Long_t pubID;

    /**
     * The string representation of the document public identifier as specified
     * in the XML/WBXML document (e.g. "-//WAPFORUM//DTD WML 1.0//EN"). This
     * attribute is valid only when pubID equals zero. Otherwise pubIDStr will
     * be NULL.
     */
    String_t pubIDStr;

    /**
     * This flag is set by the nextTok method when the scanner reaches the end
     * of the buffer.
     */
    Flag_t finished;
};

/**
 * Initialize a new WBXML scanner.
 *
 * @pre ppScanner is NULL
 * @post ppScanner points to an initialized scanner status object
 * @param pBufEnd (IN)
 *        buffer containing the WBXML/XML document
 * @param ppBufPos (IN/OUT)
 *        pointer to the current position within the buffer
 * @param ppScanner (OUT)
 *        a new WBXML/XML scanner status object
 * @return SML_ERR_OK or an appropriate error code
 */
Ret_t xltDecWbxmlInit(const MemPtr_t pBufEnd, MemPtr_t *ppBufPos, XltDecScannerPtr_t *ppScanner);
/**
 * Initialize a new XML scanner.
 *
 * @copydoc xltDecWbxmlInit
 */
Ret_t xltDecXmlInit(const MemPtr_t pBufEnd, MemPtr_t *ppBufPos, XltDecScannerPtr_t *ppScanner);

#ifdef _cplusplus
}
#endif

#endif
