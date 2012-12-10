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
 *		Name:					comms_if.h
 *
 *		Project:				OMC
 *
 *		Created On:				July 2004
 *
 *		Derived From:			Original
 *
 *		Version:				$Id: //depot/main/base/omc/comms_if.h#7 $
 *
 *		Coding Standards:		3.0
 *
 *		(c) Copyright Insignia Solutions plc, 2004 - 2006
]*/

/*! \file
 *		Defines the API used to provide support for using a platforms
 *		communications stack. The APIs purpose is to allow the client to
 *		send and receive data to/from the SyncML server.
 *
 * \brief
 *		Communications API
 */

#ifndef _OMC_COMMS_IF_H_
#define _OMC_COMMS_IF_H_

#ifdef __cplusplus
extern "C" {
#endif


/*!
===============================================================================
 * Initialise the communications support code.
 *
 * \param	pUser		Pointer to implementation user data.
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_COMMS_init(OMC_UserDataPtr pUser);


/*!
===============================================================================
 * Provide the Addr, AddrType and PortNbr about to be used for the initial
 * SyncMLDM access. This is the address to which the client must first contact.
 * Subsequent contact may be redirected to a different address by the SyncML
 * server using the 'RespURI' of the SyncML protocol.
 *
 * \param	pUser		Pointer to implementation user data.
 *
 * \param	addr		The address of the SyncML Server.
 *
 * \param	addrType	The type of the address specified. From the DMStdObj
 *						specification the possible values are
 *						1 - HTTP
 *						2 - WSP
 *						3 - OBEX
 *
 * \param	portNbr		The portNbr specified for this SyncML DM server.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_COMMS_initConnection(OMC_UserDataPtr pUser,
										  UTF8CStr addr, IU16 addrType,
										  IU16 portNbr);

/*!
===============================================================================
 * Mark the start of a new message to the server.
 *
 * If HMAC authentication is to be sent then both username and mac must be
 * supplied but algorithm need only be supplied if not MD5.
 *
 * \param	pUser		Pointer to implementation user data.
 *
 * \param	addr		The address of the SyncML Server.
 *
 * \param	msgLen		The length of the message to be sent.
 *
 * \param	mimeType	The mime type of the message data.
 *
 * \param	username	The username to send for HMAC authentication or NULL.
 *
 * \param	mac			The mac to send for HMAC authentication or NULL.
 *
 * \param	algorithm	The algorithm used for HMAC authentication or NULL.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
 ===============================================================================
 */
extern OMC_Yield OMC_COMMS_startMessage(OMC_UserDataPtr pUser, UTF8CStr addr,
						IS32 msgLen, UTF8CStr mimeType, UTF8CStr username,
						UTF8CStr mac, UTF8CStr algorithm);


/*!
===============================================================================
 * Send the data for the next msg to the specified address. This function is
 * not expected to return until all the data has been sent.
 *
 * \param	pUser		Pointer to implementation user data.
 *
 * \param	msg			Pointer to the data to be sent.
 *
 * \param	len			The length of the data to send.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_COMMS_sendMessage(OMC_UserDataPtr pUser,
									   IU8 *msg, IS32 len);


/*!
===============================================================================
 * Receive a reply. The data is expected to be written to the buffer supplied
 * in the argument 'msg', which can hold a maximum of 'bufSize' bytes of data.
 * The length of the data read in is returned in 'len'. This function is
 * expected to read the full content of the reply before returning. If the
 * buffer is not large enough, OMC_ERR_BUFFER_OVERFLOW should be returned and
 * the required buffer size returned in len.
 *
 * \param	pUser		Pointer to implementation user data.
 *
 * \param	msg			Pointer to a buffer to receive the incoming message.
 *
 * \param	len			Pointer to be set to the number of bytes read.
 *
 * \param	bufSize		The maximum number of bytes which can be held in the
 *						supplied buffer.
 *
 * \param	mimeType	Where to store the mime type of the message data. This
 *						pointer need only remain valid until
 *						OMC_COMMS_endMessage() is called.
 *
 * \param	username	Where to store a pointer to the username from the
 *						HMAC credentials or NULL if no HMAC credentials were
 *						supplied. This pointer need only remain valid until
 *						OMC_COMMS_endMessage() is called.
 *
 * \param	mac			Where to store a pointer to the mac from the HMAC
 *						credentials or NULL if no HMAC credentials were
 *						supplied. This pointer need only remain valid until
 *						OMC_COMMS_endMessage() is called.
 *
 * \param	algorithm	Where to store a pointer to the algorithm from the
 *						HMAC credentials or NULL if no HMAC credentials were
 *						supplied. This pointer need only remain valid until
 *						OMC_COMMS_endMessage() is called.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_COMMS_recvMessage(OMC_UserDataPtr pUser,
									   IU8 *msg, IS32 *len, IS32 bufSize,
									   UTF8CStr* mimeType, UTF8CStr* username,
									   UTF8CStr* mac, UTF8CStr* algorithm);


/*!
===============================================================================
 * End the request/response exchange for the current message.
 *
 * \param	pUser		Pointer to implementation user data.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_COMMS_endMessage(OMC_UserDataPtr pUser);


/*!
===============================================================================
 * Terminate the connection.
 *
 * \param	pUser		Pointer to implementation user data.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_COMMS_termConnection(OMC_UserDataPtr pUser);


/*!
===============================================================================
 * Terminate the communications support code and perform any cleanup required.
 *
 * \param	pUser		Pointer to implementation user data.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield OMC_COMMS_term(OMC_UserDataPtr pUser);


/*
 * Functions defined by the Open Management Client, called externally.
 */

/*!
===============================================================================
 * Fetch a value from the ./SyncML/Con sub-tree associated with the DMAcc
 * account. This area of the tree contains additional information which may be
 * required for the communications access including details of a Network
 * Access Point (NAP) and Proxy (PX).
 * If the path specified is an interior node, the returned buffer will contain
 * a list of child nodes separated by '/'.
 * If the path specified is a leaf node, the returned buffer will contain the
 * nodes current value.
 * It is important to note that this function allows access to the value of the
 * Auth/z/Secret nodes.
 *
 * \param	pSess	Pointer to OMC session data.
 *
 * \param	path	The relative URI for the node in the /SyncML/Con/x
 *					sub-tree. The path supplied should be relative to the
 *					/SyncML/Con/x node. The OMC will prefix the path with the
 *					appropriate '/SyncML/Con/x/' when retrieving the data.
 * \param	buffer	A pointer which will be set to point to the allocated
 *					memory holding the retrieved value, or NULL if the value
 *					could not be retrieved.
 * \param	len		The length of the buffer holding retrieved value.
 *
 * \return	\ref OMC_ERR_defs "An error code" (OMC_ERR_OK if no error)
===============================================================================
 */
extern OMC_Yield COMMS_OMC_fetchValue(OMC_SessionDataPtr pSess,
									  UTF8CStr path, IU8 **buffer, IU32 *len);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* !_OMC_COMMS_IF_H_ */
