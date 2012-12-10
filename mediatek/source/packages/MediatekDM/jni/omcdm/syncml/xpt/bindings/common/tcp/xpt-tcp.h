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
 *      Name:				xpt-tcp.h
 *
 *      Derived From:		Original
 *
 *      Created On:			May 2004
 *
 *      Version:			$Id: //depot/main/base/syncml/xpt/bindings/common/tcp/xpt-tcp.h#3 $
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
 * Communication Services, TCP/IP functions
 *
 * @target_system   all
 * @target_os       all
 * @description Definition of error codes and function prototypes
 * of the TCP/IP services
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

#ifndef XPT_TCP_H
#define XPT_TCP_H

#include <syncml/xpt/bindings/common/tcp/xpttypes.h>

/******************************************************/
/*        TCP/IP services return values               */
/******************************************************/

// default port numbers
// %%% luz:2003-04-17: added for SSL support
#define HTTP_PORT             "80"
#define HTTPS_PORT            "443"


// Berkeley compatible return values
#define  TCP_RC_ERROR              -2  // an internal error occurred
#define  TCP_RC_EOF                -1  // end of transmission
#define  TCP_RC_OK                  0

// Berkeley compatible return values
#define  TCP_RC_EWOULDBLOCK        35
#define  TCP_RC_EINPROGRESS        36
#define  TCP_RC_EALREADY           37
#define  TCP_RC_ENOTSOCK           38
#define  TCP_RC_EDESTADDRREQ       39
#define  TCP_RC_EMSGSIZE           40
#define  TCP_RC_EPROTOTYPE         41
#define  TCP_RC_ENOPROTOOPT        42
#define  TCP_RC_EPROTONOSUPPORT    43
#define  TCP_RC_ESOCKTNOSUPPORT    44
#define  TCP_RC_EOPNOTSUPP         45
#define  TCP_RC_EPFNOSUPPORT       46
#define  TCP_RC_EAFNOSUPPORT       47
#define  TCP_RC_EADDRINUSE         48
#define  TCP_RC_EADDRNOTAVAIL      49
#define  TCP_RC_ENETDOWN           50
#define  TCP_RC_ENETUNREACH        51
#define  TCP_RC_ENETRESET          52
#define  TCP_RC_ECONNABORTED       53
#define  TCP_RC_ECONNRESET         54
#define  TCP_RC_ENOBUFS            55
#define  TCP_RC_EISCONN            56
#define  TCP_RC_ENOTCONN           57
#define  TCP_RC_ESHUTDOWN          58
#define  TCP_RC_ETOOMANYREFS       59
#define  TCP_RC_ETIMEDOUT          60
#define  TCP_RC_ECONNREFUSED       61
#define  TCP_RC_ELOOP              62
#define  TCP_RC_ENAMETOOLONG       63
#define  TCP_RC_EHOSTDOWN          64
#define  TCP_RC_EHOSTUNREACH       65
#define  TCP_RC_ENOTEMPTY          66
#define  TCP_RC_EPROCLIM           67
#define  TCP_RC_EUSERS             68
#define  TCP_RC_EDQUOT             69
#define  TCP_RC_ESTALE             70
#define  TCP_RC_EREMOTE            71
#define  TCP_RC_EDISCON           101

#define  TCP_RC_OTHER              10  // an unknown error occurred.
// %%% luz:2003-04-17: added these for SSL
#define  TCP_TC_SLL_CERT_EXPIRED   20
#define  TCP_TC_SLL_CERT_INVALID   21

// #ifdef _cplusplus
// extern "C" {
// #endif


typedef enum { TCP_FIREWALL_DIRECT, TCP_FIREWALL_PROXY, TCP_FIREWALL_SOCKS } TcpFirewallType_t;

typedef struct _tcp_firewall_info {
    TcpFirewallType_t type;
    StringBuffer_t    serverName;
    int               serverPort;
} TcpFirewallInfo_t, *TcpFirewallInfoPtr_t;


/********************/
/* Type definitions */
/********************/
typedef int TcpRc_t;
typedef long           Socket_t;
typedef Socket_t      *SocketPtr_t;


/**
 * Generate a TCP/IP socket, establish a connection
 * with the specified communication partner
 *
 * @post The communication has been established. The generates socket can be
 *       used to exchange data with the communication partner.
 * @param pszPort (IN)
 *        host address / port.
 *        - If the communication open mode is "c", the IP address
 *        (and optional the TCP/IP port) is specified here.\n
 *        Examples: If no TCP/IP port has been specified, the default port 80
 *        is taken. "192.168.5.1" (use default port 80) or "192.168.5.2:81"
 *        (use port 81)
 *        - If the communication open mode is "s", this parameter denotes
 *        the TCP/IP port that is used for this connection. If the string
 *        is empty, the default port 80 is used.\n
 *        Examples: "" (use default port 80) or "81" (use port 81).
 * @param pszOpenMode (IN)
 *        communication flags. "c" denotes that a client socket is created, "s"
 *        specifies a server socket to be generated.
 * @param pSocket (OUT)
 *        Generated TCP/IP socket.
 * @return Return code. Refer to the return code list above for details.
 */
TcpRc_t tcpOpenConnection (CString_t   pszPort,
                           SocketPtr_t pSocket,
                           CString_t   pszOpenMode);



TcpRc_t tcpOpenConnectionEx (CString_t            pszPort,
                             SocketPtr_t          pSocket,
                             CString_t            pszOpenMode,
                             TcpFirewallInfoPtr_t pFirewallInfo );


/**
 * Closes an open TCP/IP connection for both client and server socket.
 *
 * @pre the specified socket has been generated before, in invokng the
 *      tcpOpenConnection() service
 * @post The communication has been dropped. System resources that have been
 *       assigned with the specified TCP/IP socket are freed.
 * @param pSocket (IN/OUT)
 *        pointer to an open TCP/IP socket. The function resets the
 *        socket to an 'unused' state.
 * @return Return code. Refer to the return code list above for details.
 */
TcpRc_t tcpCloseConnection (SocketPtr_t pSocket);

/**
 * Wait until a client has been connected to the open server socket specified.
 *
 * @pre The specified socket has been opened in the server mode (that means,
 *      pszOpenMode was set to "s" when the socket has been created by a
 *      tcpOpenConnection () invocation)
 * @post A new socket has been generated and can be used to communicate with
 *       the client.
 * @param pSocket (IN)
 *        pointer to an open TCP/IP server socket.
 * @param pNewSocket (OUT)
 *        pointer to a socket variable that will be set by
 *        this function with the generated communication socket.
 * @param pchSenderAddress (OUT)
 *        pointer to at least 24 Bytes of memory that will be
 *        updated by this function with the IP address of the client that
 *        connected.\n
 *        Example: "192.168.5.1"
 * @return Return code. Refer to the return code list above for details.
 */
TcpRc_t tcpWaitforConnections (SocketPtr_t    pSocket,
                               SocketPtr_t    pNewSocket,
                               StringBuffer_t pchSenderAddress);

/**
 * Send a block of data to the communication partner
 *
 * @pre The specified socket has been created either by the service
 *      tcpWaitforConnections() (server mode), or by the service
 *      tcpOpenConnection () (Client mode). A server socket generated
 *      by tcpOpenConnection () must not be used for this function.
 * @post The specified data are transmitted to the communication partner.
 *       If the communication was broken or terminated by the communication
 *       partner, a return code TCP_RC_EOF is returned.
 * @param pSocket (IN)
 *        pointer to an open TCP/IP server socket.
 * @param pbBuffer (IN)
 *        pointer to an allocated chunk of memory that contain
 *        the data to be transmitted.
 * @param cbBufferSize (IN)
 *        size in Bytes of the data to be transmitted.
 * @return Return code or transmission status.
 */
TcpRc_t tcpSendData (SocketPtr_t        pSocket,
                     const DataBuffer_t pbBuffer,
                     BufferSize_t       cbBufferSize);

/**
 * Receive a block of data from the communication partner
 *
 * @pre The specified socket has been created either by the service
 *      tcpWaitforConnections() (server mode), or by the service
 *      tcpOpenConnection() (Client mode). A server socket generated by
 *      tcpOpenConnection () must not be used for this function.
 * @post The specified data are received from the communication partner.
 *       If the communication was broken or terminated by the communication
 *       partner, a return code TCP_RC_EOF is returned.
 * @param pSocket (IN)
 *        pointer to an open TCP/IP server socket.
 * @param pbBuffer (IN)
 *        pointer to an allocated chunk of memory that is filled
 *        up with the data that are received from the communication partner.
 * @param pcbBufferSize (IN/OUT)
 *        The parameter points to a variable that is set with the
 *        size in bytes of the data buffer by the caller. The function replaces
 *        this value with the length of the data block that has been received.
 * @return Return code or transmission status.
 *         Refer to the return code list above for details.
 */
TcpRc_t tcpReadData (SocketPtr_t     pSocket,
                     DataBuffer_t    pbBuffer,
                     BufferSizePtr_t pcbBufferSize);



// %%% luz 2003-06-26: Added tcpEnableSSL() declaration here
/**
 * Enables Socket for SSL
 *
 * @pre The specified socket has been created either by the service
 *      tcpWaitforConnections() (server mode), or by the service
 *      tcpOpenConnection () (Client mode).
 *      A server socket generated by tcpOpenConnection () must not be
 *      used for this function.
 * @post The socket is configured for SSL; subsequenct
 *       tcpReadData()/tcpWriteData() will use SSL. If SSL is not available,
 *       tcpEnableSSL returns TCP_RC_ENOPROTOOPT
 * @param pSocket (IN)
 *        pointer to an open TCP/IP socket.
 * @param aConnected (IN)
 *        set if socket is already connected
 * @return Return code
 */
TcpRc_t tcpEnableSSL(SocketPtr_t pSocket, Bool_t aConnected);


// %%% luz 2003-06-26: Added tcpBeforeSocketClose() declaration here
/**
 * Called before tcp socket closes
 */
void tcpBeforeSocketClose(SocketPtr_t pSocket);


// #ifdef _cplusplus
// }
// #endif

#endif
