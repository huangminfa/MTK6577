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
 *      Name:				httptrans.h
 *
 *      Derived From:		Original
 *
 *      Created On:			May 2004
 *
 *      Version:			$Id: //depot/main/base/syncml/xpt/bindings/http/httptrans.h#4 $
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
 * Communication Services, HTTP functions
 *
 * @target_system   all
 * @target_os       all
 * @description HTTP protocol services, function prototypes and return codes
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

#ifndef XPT_HTTP_TRANS_H
#define XPT_HTTP_TRANS_H


/*********************/
/* Required includes */
/*********************/

#include <syncml/xpt/manager/xptTransport.h>
#include <syncml/xpt/bindings/common/tcp/xpttypes.h>
#include <syncml/xpt/bindings/http/xpt-http.h>


#ifdef __cplusplus
extern "C" {
#endif


typedef struct _http_transport_info {
    BufferSize_t                  cbSize;        /**< size of this structure */
} HttpTransportInfo_t,  *HttpTransportInfoPtr_t;

struct _http_transport_service_client_info {
    TcpFirewallInfo_t       firewallInfo;
    StringBuffer_t          proxyString;
    StringBuffer_t          pchServerAddress;
    // %%% luz:2003-04-16 added SSL support
    Bool_t                  useSSL;
};

typedef struct _http_transport_service_info {
    BufferSize_t                         cbSize;    /**< size of this structure */
    union {
        struct _http_transport_service_client_info clientInfo;
    } info;

} HttpTransportServiceInfo_t,  *HttpTransportServiceInfoPtr_t;

struct _http_transport_conn_client_info {
    char tmp;
};

typedef enum  { HTTP_TRANSPORT_OPEN, HTTP_TRANSPORT_SENDING, HTTP_TRANSPORT_RECEIVING, HTTP_TRANSPORT_CLOSED } HttpTransportConnState_t ;

typedef struct _http_transport_conn_info {
    BufferSize_t                  cbSize;        /**< size of this structure */
    Socket_t                      socket;
    HttpTransportServiceInfoPtr_t pServiceInfo;
    HttpHandle_t                  http;
    HttpDocumentContext_t         docContext;

    union {
        struct _http_transport_conn_client_info clientInfo;
    } info;
} HttpTransportConnInfo_t,  *HttpTransportConnInfoPtr_t;



Ret_t XPTAPI  HTTP_selectProtocol(void *privateTransportInfo,
                                     const char *metaInformation,
                                     unsigned int flags,
                                     void **pPrivateServiceInfo);

Ret_t XPTAPI HTTP_deselectProtocol(void *privateServiceInfo);


Ret_t XPTAPI HTTP_openCommunication(void *privateServiceInfo,
                                    int role,
                                    void **pPrivateConnectionInfo);

Ret_t XPTAPI  HTTP_closeCommunication(void *privateConnectionInfo);

Ret_t XPTAPI  HTTP_beginExchange(void *privateConnectionInfo);

Ret_t XPTAPI  HTTP_endExchange(void *privateConnectionInfo);

Ret_t XPTAPI  HTTP_receiveData(void *privateConnectionInfo,
                               void *buffer, size_t bufferLen,
                               size_t *dataLen);

Ret_t XPTAPI  HTTP_sendData(void *privateConnectionInfo,
                            const void *buffer, size_t bufferLen,
                            size_t *bytesSent);

Ret_t XPTAPI  HTTP_sendComplete(void *privateConnectionInfo);

Ret_t XPTAPI  HTTP_setDocumentInfo(void *privateConnectionInfo,
                                   const XptCommunicationInfo_t *pDoc);


Ret_t XPTAPI  HTTP_getDocumentInfo(void *privateConnectionInfo,
                                   XptCommunicationInfo_t *pDoc);


#ifdef __cplusplus
}
#endif

#endif

