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
 *      Name:				xpt-http.h
 *
 *      Derived From:		Original
 *
 *      Created On:			May 2004
 *
 *      Version:			$Id: //depot/main/base/syncml/xpt/bindings/http/xpt-http.h#5 $
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
 * Industrial Co., LTD,Motorola, Nokia, Palm, Inc., Psion,
 * Starfish Software (2001).
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


#ifndef XPT_HTTP_H
#define XPT_HTTP_H


/*********************/
/* Required includes */
/*********************/

#include <syncml/xpt/manager/xpt.h>
#include <syncml/xpt/bindings/common/tcp/xpttypes.h>
#include <syncml/xpt/bindings/common/tcp/xpt-tcp.h>


#ifdef _cplusplus
extern "C" {
#endif

#define UNDEFINED_CONTENT_LENGTH  0x7FFFFFFFL

/*****************************/
/* Types and Data structures */
/*****************************/

typedef void * HttpHandle_t;   /**< instance handle */



/********************************************************/
/* Authentication info, Basic and Digest authentication */
/********************************************************/
#include <syncml/xpt/bindings/common/xpt-auth.h>

/** document context */
typedef struct
   {
   BufferSize_t cbSize;          /**< size of this structure */
   CString_t pszURL;             /**< document name */
   CString_t pszType;            /**< document MIME type */
   CString_t pszHost;            /**< host name (optional) */
   BufferSize_t cbLength;        /**< document length */
   CString_t pszReferer;         /**< referenced URL (optional) */
   CString_t pszRequest;         /**< type of the HTTP request */
   CString_t pszFrom;            /**< sender of the document */
   CString_t pszProxy;           /**< Proxy IP address */
   XptHmacInfoPtr_t pXSyncmlHmac; /**< digest values for transport header field */
   // %%% luz:2002-05-23: Auth support added
   HttpAuthenticationPtr_t auth;  /**< auth structure created by authInit() */
   } HttpDocumentContext_t, *HttpDocumentContextPtr_t;

/** HTTP document reply */
typedef struct
   {
   BufferSize_t cbSize;          /**< size of this structure */
   CString_t pszTime;            /**< creation date of the replied document */
   CString_t pszType;            /**< document MIME type */
   BufferSize_t cbLength;        /**< document length */
   HttpAuthenticationPtr_t auth; /**< authentication info */
   XptHmacInfoPtr_t pXSyncmlHmac; /**< digest values for transport header field */
   } HttpReplyBuffer_t, * HttpReplyBufferPtr_t;



/**************************/
/* Function return values */
/**************************/
typedef enum
   {
   HTTP_RC_RETRY         = -3,  /**< authentication required: resend the document */
   HTTP_RC_HTTP          = -2,  /**< server error */
   HTTP_RC_EOF           = -1,  /**< end of transmission */
   HTTP_RC_OK            =  0,
   HTTP_RC_COMMUNICATION =  1,  /**< communication problem, reported by TCP/IP */
   HTTP_RC_PARAMETER     =  2,  /**< one of the parameters was invalid */
   HTTP_RC_NOT_ALLOWED   =  3,  /**< this function call is not allowed in this context */
   // %%%luz:2003-04-17 added these extra codes
   HTTP_RC_TIMEOUT       =  4,  /**< timeout */
   HTTP_RC_CERT_EXPIRED  =  5,  /**< https: certificate expired */
   HTTP_RC_CERT_INVALID  =  6   /**< https: certificate invalid */
   } HttpRc_t;



/**
 * Opens a HTTP connection
 *
 * @pre The function is invoked if a client or server decides to process a
 *      HTTP request.\n
 *      The TCP/IP socket that is passed to the service must have been
 *      opened via tcpOpen(), and the socket must be in the right mode:
 *      If a "SERVER" request is selected, this must be a server socket,
 *      if a "SEND", "RECEIVE", or "EXCHANGE" request is selected, a client
 *      socket must be passed to the function.
 *      Although the HTTP services utilize this socket, the socket itself
 *      must be opened and closed by the caller.
 * @post - HTTP Clients: The HTTP header is transmitted to the server.
 *       If a "SEND" or "EXCHANGE" request was selected, the document
 *       that is specified in the 'settings' parameter can be transmitted
 *       to the host, using httpWrite().\n
 *       if a "RECEIVE" request was selected, no document is transmitted
 *       to the server. The application can directly call httpWait ()
 *       to wait for the requested document.
 *       - HTTP Server: The client's request type (either "SEND", "EXCHANGE"
 *       or "RECEIVE" are expected) as well as the document properties
 *       that are sent from the client to the server ("EXCHANGE" and "SEND"
 *       requests) are returned in the document context structure that is
 *       referenced by the 'settings' parameter.\n
 *       If a pointer to an authentication information structure
 *       is passed to the function, the structure is updated
 *       with the client's authorization information (userID: passphrase).
 * @param p (IN)
 *        instance handle
 * @param pSession (IN)
 *        potiner to an open TCP/IP socket.
 * @param pszMode  (IN)
 *        HTTP request type.
 *        "SEND", "EXCHANGE", "RECEIVE" for HTTP clients
 *        "SERVER" for HTTP servers
 * @param settings (IN/OUT)
 *        pointer to a structure that denotes the properties
 *        of the document to be sent or being received.
 * @param auth
 *        authorization info, may be NULL.
 * @return return code. Refer to the type definition above for details.
 *         If the return value is HTTP_RC_COMMUNICATION, further error
 *         information can be retrieved with the httpGetError() service;
 */
HttpRc_t httpOpen (HttpHandle_t p,
                   SocketPtr_t pSession,
                   CString_t pszMode,
                   HttpDocumentContextPtr_t settings,
                   HttpAuthenticationPtr_t auth);

/**
 * Opens a HTTP connection
 *
 * @pre The function is invoked if a client or server decides to process a
 *      HTTP request.\n
 *      The TCP/IP socket that is passed to the service must have been
 *      opened via tcpOpen(), and the socket must be in the right mode:
 *      If a "SERVER" request is selected, this must be a server socket,
 *      if a "SEND", "RECEIVE", or "EXCHANGE" request is selected, a client
 *      socket must be passed to the function.
 *      Although the HTTP services utilize this socket, the socket itself
 *      must be opened and closed by the caller.
 * @post - HTTP Clients: The HTTP header is transmitted to the server.
 *       If a "SEND" or "EXCHANGE" request was selected, the document
 *       that is specified in the 'settings' parameter can be transmitted
 *       to the host, using httpWrite().\n
 *       if a "RECEIVE" request was selected, no document is transmitted
 *       to the server. The application can directly call httpWait ()
 *       to wait for the requested document.
 *       - HTTP Server: The client's request type (either "SEND", "EXCHANGE"
 *       or "RECEIVE" are expected) as well as the document properties
 *       that are sent from the client to the server ("EXCHANGE" and "SEND"
 *       requests) are returned in the document context structure that is
 *       referenced by the 'settings' parameter.\n
 *       If a pointer to an authentication information structure
 *       is passed to the function, the structure is updated
 *       with the client's authorization information (userID: passphrase).
 * @param p (IN)
 *        instance handle
 * @param pSession (IN)
 *        potiner to an open TCP/IP socket.
 * @param pszMode  (IN)
 *        HTTP request type.
 *        "SEND", "EXCHANGE", "RECEIVE" for HTTP clients
 *        "SERVER" for HTTP servers
 * @param settings (IN/OUT)
 *        pointer to a structure that denotes the properties
 *        of the document to be sent or being received.
 * @param auth
 *        authorization info, may be NULL.
 * @param userAgent (IN)
 *        string to use for the UserAgent HTTP header. Only required to persist
 *        for the duration of this call.
 * @param rangeHeader (IN)
 *        string to use for the Range HTTP header. Only required to persist
 *        for the duration of this call. If not required, NULL or a zero length
 *        string should be passed instead.
 *
 * @return return code. Refer to the type definition above for details.
 *         If the return value is HTTP_RC_COMMUNICATION, further error
 *         information can be retrieved with the httpGetError() service;
 */
HttpRc_t httpOpenEx (HttpHandle_t p,
                   SocketPtr_t pSession,
                   CString_t pszMode,
                   HttpDocumentContextPtr_t settings,
                   HttpAuthenticationPtr_t auth,
                   CString_t userAgent,
                   CString_t rangeHeader);


/**
 * Write a chunk of data
 *
 * @pre the HTTP communication has been opened via httpOpen(), and the protocol
 *      is in a state where incoming data is expected:
 *       - HTTP Clients: BEFORE httpWait() has been invoked
 *       - HTTP Server:  AFTER httpReply () has been invoked
 * @post The data is transmitted to the communication partner.
 * @param p (IN)
 *        instance handle
 * @param pbBuffer (IN)
 *        pointer to a block of allocated memory for the received data
 * @param cbBufferSize (IN)
 *        size of the memory block above
 * @param bFinal (IN)
 *        flag indicating if input buffer is the last block to send!
 * @return return code. Refer to the type definition above for
 *         details. If the return value is HTTP_RC_COMMUNICATION, further
 *         error information can be retrieved with the httpGetError() service;
 */
HttpRc_t httpWrite (HttpHandle_t p,
                    DataBuffer_t pbBuffer,
                    BufferSize_t cbBufferSize,
                    Bool_t bFinal);

/**
 * Close an open HTTP communication
 *
 * @pre A HTTP communication has been opened via httpOpen(),
 *      and the data exchange has been done.
 * @post The HTTP instance handle is invalidated, and all secondary storage
 *       and system resources are freed. The TCP/IP socked remains open.
 * @param p (IN)
 *        instance handle
 * @return return code. Refer to the type definition above for details.
 *         If the return value is HTTP_RC_COMMUNICATION, further error
 *         information can be retrieved with the httpGetError() service;
 */
HttpRc_t httpClose (HttpHandle_t p);

/**
 * Read a chunk of data
 *
 * @pre the HTTP communication has been opened via httpOpen(),
 *      and the protocol is in a state where incoming data is expected:
 *       - HTTP Clients: AFTER httpWait() has been invoked
 *       - HTTP Server: BEFORE httpReply () has been invoked
 * @post A part of the receiving document is copied to the specified data
 *       buffer. The size of the received data buffer is returned in the
 *       variable that is referenced with the pointer 'pcbDataRead'.
 * @param p (IN)
 *        instance handle
 * @param pbDataBuffer (IN)
 *        pointer to a block of allocated memory for the received data
 * @param cbDataBufferSize (IN)
 *        size of the memory block above
 * @param pcbDataRead (OUT)
 *        pointer to a variable that is updated with the size
 *        of the received data block.
 * @return return code. Refer to the type definition above for
 *         details. If the return value is HTTP_RC_COMMUNICATION, further
 *         error information can be retrieved with the httpGetError() service;
 */
HttpRc_t httpRead  (HttpHandle_t p,
                    DataBuffer_t pbDataBuffer,
                    BufferSize_t cbDataBufferSize,
                    BufferSizePtr_t pcbDataRead);

/**
 * Wait for the HTTP server response
 *
 * @pre The HTTP instance has been opened in a client mode. A HTTP request
 *      has been selected that expects a response document from the server.\n
 *      This is in case of "RECEIVE" requests, immediately after httpOpen() has
 *      been invoked, or in case of "EXCHANGE" requests, after the posted
 *      document has been entirely transmitted to the server using
 *      the httpWrite() service. The application nust invoke
 *      the httpWait() function to wait for the response document.
 * @post When the function returns, the server started sending the response
 *       document. The document context (i.e. document length, creation date,
 *       MIME type) is returned to the caller in the pSettings parameter.
 * @param p (IN)
 *        instance handle
 * @param pSettings (IN/OUT)
 *        response document properties.
 * @param pAuth (OUT)
 *        pointer to an allocated authentication info structure,
 *        or NULL, if the caller does not want to examine the
 *        server's authentication data.
 * @return return code. Refer to the type definition above for details.
 *         If the return value is HTTP_RC_COMMUNICATION, further error
 *         information can be retrieved with the httpGetError() service;
 */
HttpRc_t httpWait  (HttpHandle_t p,
                    HttpDocumentContextPtr_t pSettings,
                    HttpAuthenticationPtr_t pAuth);

/**
 * Reply to a client HTTP request
 *
 * @pre The HTTP instance has been opened in the server mode. A HTTP request
 *      has been initiated by the client. The service httpReply() is invoked
 *      after the client HTTP request has been completely received.
 *      Dependend on the type of request, This service issues the
 *      transmission of the HTTP response header.
 * @post Dependent on the type of HTTP request, the client expects a response
 *       document. If this is the case, the application can now start sending
 *       the document with the httpWrite() service.
 * @param p (IN)
 *        instance handle
 * @param rcDocument (IN)
 *        HTTP return code of the request (i.e. 200 for OK)
 * @param pSettings (IN)
 *        response document properties.
 *        Dependent on the type of request,
 *        the structure elements can be filled:
 *        - pszTime - creation date of the response document
 *                    ("RECEIVE", "EXCHANGE" requests, optional)
 *        - pszType - MIME type of the response document\n
 *                    ("RECEIVE", "EXCHANGE" request, optional)
 *        - cbLength - Length of the response document\n
 *                     (must be 0 for "SEND" requests, otherwise optional)
 *        - rcDocument - HTTP return code of the request\n
 *                     (i.e. 200 for OK)
 * @param pAuth (OUT)
 *        pointer to a structure that contains authentication info (optional)
 *        The authentication structure contains the following elements:
 *        - cbSize - size of dataauth structure.
 *        - pbData - pointer to an allocated block of data that contains the
 *                   authentication data to be sent to the client.
 *        - fType - authentication type, must be 0.
 *        - cbDataLength - size of the data block above.
 *        - pcbDataRead - pointer to a variable that is updated with the size of
 *                        the received data block.
 * @return return code. Refer to the type definition above for details.
 *         If the return value is HTTP_RC_COMMUNICATION, further error
 *         information can be retrieved with the httpGetError() service;
 */
HttpRc_t httpReply (HttpHandle_t p,
                    int rcDocument,
                    const HttpReplyBufferPtr_t pSettings,
                    const HttpAuthenticationPtr_t pAuth);

/**
 * Return the return code of the last error, that a TCP/IP protocol
 * service returned.
 *
 * @pre The previous HTTP protocol service function failed with the return
 *      value HTTP_RC_COMMUNICATION. The caller invokes this function to
 *      retrieve the return code TCP/IP service that failed.
 * @param p (IN)
 *        instance handle
 * @return Return code of the failing TCP/IP service
 */
TcpRc_t  httpGetError (HttpHandle_t p);

/**
 * Return the HTTP return code in the HTTP response header.
 *
 * @pre The previous HTTP protocol service function failed with the return
 *      value HTTP_RC_SERVER. The caller invokes this function to retrieve
 *      the HTTP return code. (i.e. rc=404 means "resource not found")
 * @param p (IN)
 *        instance handle
 * @return int Return code of the failing HTTP request
 */
int  httpGetServerStatus (HttpHandle_t p);

/**
 * Return the number of Bytes that is required for the instance object
 *
 * @post The function returns the size of the data buffer that the caller must
 *       allocate for the object instance memory, that is required to call
 *       httpOpen().
 * @return required size of the HTTP instance handle in Bytes.
 */
BufferSize_t httpGetBufferSize (void);


/**
 * Check wether the HTTP document has been received completely.
 *
 * @pre the HTTP communication has been opened via httpOpen(), and the
 *      protocol is in a state where incoming data is expected:
 *       - HTTP Clients: AFTER httpWait() has been invoked
 *       - HTTP Server: BEFORE httpReply () has been invoked
 * @post The caller uses the function to get the exit conditions for a while
 *       loop that receives an incoming HTTP document, for example:
 *       <PRE>
 *       while (!httpIsEox (http_handle))  {
 *            httpRead (...);
 *            //do something interesting
 *       }
 *       </PRE>
 * @param p (IN)
 *        instance handle
 * @return true, if the document has been read entirely.\n
 *         false if further receive packets are expected.
 */
Bool_t httpIsEox (HttpHandle_t p);


#ifdef _cplusplus
}
#endif

#endif

