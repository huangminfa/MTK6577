/*
 * Copyright (c) 2007-2012 Inside Secure, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef __WME_7816_4_H
#define __WME_7816_4_H

#if (P_BUILD_CONFIG == P_CONFIG_USER) || (P_BUILD_CONFIG == P_CONFIG_MONOLITHIC)

/*******************************************************************************
   Contains the declaration of the 7816-4 functions
*******************************************************************************/

/**
 * @brief   Create the connection at 7816-4 level.
 *
 * @param[in]  pContext  The context.
 *
 * @param[in]  hUserConnection  The connection handle.
 *
 * @param[in]  pCallback  The callback function.
 *
 * @param[in]  pCallbackParameterThe callback parameter.
 *
 * @param[in]  nConnectionProperty  The connection property.
 **/
void P7816CreateConnection(
            tContext* pContext,
            W_HANDLE hUserConnection,
            tPBasicGenericCallbackFunction* pCallback,
            void* pCallbackParameter,
            uint8_t nConnectionProperty );

/**
 * @brief   Create the connection at 7816-4 level.
 *
 * @param[in]  pContext  The context.
 *
 * @param[in]  pSmInstance  The SM instance.
 *
 * @param[in]  pSmInterface  The SM interface.
 *
 * @param[in]  hUserConnection  The connection handle.
 *
 * @return The error code.
 **/
W_ERROR P7816CreateConnectionInternal(
            tContext* pContext,
            tP7816SmInstance* pSmInstance,
            tP7816SmInterface* pSmInterface,
            W_HANDLE hUserConnection );

/**
 * @brief   Exchanges data with the 7816 protocol. PReaderNotifyExchange is not used here.
 *          This method must be called from another connection, not directly by user.
 *
 * @param[in]  pContext  The context.
 *
 * @param[in]  hConnection  The user connection handle.
 *
 * @param[in]  pCallback  The callback function.
 *
 * @param[in]  pCallbackParameter The callback parameter.
 *
 * @param[in]  pSendAPDUBuffer  A pointer on the buffer containing the data to send to the card.
 *
 * @param[in]  nSendAPDUBufferLength  The length in bytes of the data to send to the card.
 *
 * @param[in]  pReceivedAPDUBuffer  A pointer on the buffer receiving the data returned by the card.
 *
 * @param[in]  nReceivedAPDUBufferMaxLength  The maximum length in bytes of the buffer pReceivedAPDUBuffer.
 **/
void P7816ExchangeApduInternal(
            tContext* pContext,
            W_HANDLE hConnection,
            tPBasicGenericDataCallbackFunction* pCallback,
            void* pCallbackParameter,
            const uint8_t* pSendAPDUBuffer,
            uint32_t nSendAPDUBufferLength,
            uint8_t* pReceivedAPDUBuffer,
            uint32_t nReceivedAPDUBufferMaxLength);


#endif /* P_CONFIG_USER || P_CONFIG_MONOLITHIC */

#endif /* __WME_7816_4_H */
