/*
 * Copyright (c) 2011-2012 Inside Secure, All Rights Reserved.
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

#ifndef __WME_SE_HAL_STATE_MACHINE_H
#define __WME_SE_HAL_STATE_MACHINE_H

#if ( (P_BUILD_CONFIG == P_CONFIG_DRIVER) || (P_BUILD_CONFIG == P_CONFIG_MONOLITHIC) ) && (defined P_INCLUDE_SE_SECURITY)

/* ----------------------------------------------------------------------------

      Raw APDU Interface

   ---------------------------------------------------------------------------- */

/**
 * @brief Opaque data type representing an instance of the ISO-7816 card
 **/
typedef struct __tPSeHalSmRawInstance tPSeHalSmRawInstance;

/* @todo - documentation */
typedef void tPSeHalSmRawOpenChannel(
                  tContext* pContext,
                  tPSeHalSmRawInstance* pInstance,
                  tPBasicGenericDataCallbackFunction* pCallback,
                  void* pCallbackParameter,
                  uint32_t nType,
                  const uint8_t* pAID,
                  uint32_t nAIDLength);

/**
 * @brief Sends an APDU command to an ISO-7816 card.
 *
 * @param[in]  pContext  The current context.
 *
 * @param[in]  pInstance  The instance of the ISO-7816 State Machine.
 *
 * @param[in]  pCallback  The callback used to notify the APDU exchange completion.
 *
 * @param[in]  pCallbackParameter  The callback parameter.
  *
 * @param[in]  pSendApduBuffer  The APDU command.
 *
 * @param[in]  nSendApduBufferLength  The length of the APDU command.
 *
 * @param[out] pReceivedApduBuffer  The buffer used to receive the APDU response.
 *
 * @param[in]  nReceivedApduBufferMaxLength  The size of the APDU response buffer.
 *
 **/
typedef void tPSeHalSmRawExchangeApdu(
                  tContext* pContext,
                  tPSeHalSmRawInstance* pInstance,
                  uint32_t nChannelIdentifier,
                  tPBasicGenericDataCallbackFunction* pCallback,
                  void* pCallbackParameter,
                  const uint8_t* pSendApduBuffer,
                  uint32_t nSendApduBufferLength,
                  uint8_t* pReceivedApduBuffer,
                  uint32_t nReceivedApduBufferMaxLength);

/* @todo - documentation */
typedef void tPSeHalSmRawCloseChannel(
                  tContext* pContext,
                  tPSeHalSmRawInstance* pInstance,
                  uint32_t nChannelIdentifier,
                  tPBasicGenericCallbackFunction* pCallback,
                  void* pCallbackParameter);

/* @todo - documentation */
typedef void tSeHalSmRawGetResponseApdu(
                  tContext* pContext,
                  tPSeHalSmRawInstance* pInstance,
                  uint32_t nChannelIdentifier,
                  uint8_t* pResponseApduBuffer,
                  uint32_t nResponseApduBufferLength,
                  uint32_t* pnResponseApduActualSize);

void PSeHalSmNotifyOperationCompletion(
                  tContext* pContext,
                  uint32_t nSlotIdentifier,
                  uint32_t nOperation,
                  bool_t bSuccess,
                  uint32_t nParam1,
                  uint32_t nParam2);

/**
 * @brief Data type containing the communication interface with a Secure Element.
 */
typedef const struct __tPSeHalSmRawInterface
{
   /** The function that is called to open the raw, the basic or a supplementary logical channel */
   tPSeHalSmRawOpenChannel* pOpenChannel;
   /** The function that is called to perform an APDU exchange */
   tPSeHalSmRawExchangeApdu* pExchange;
   /** The function that is called to get the last APDU response data */
   tSeHalSmRawGetResponseApdu* pGetResponseApdu;
   /** The function that is called to close a channel */
   tPSeHalSmRawCloseChannel* pCloseChannel;
} tPSeHalSmRawInterface;

/* ----------------------------------------------------------------------------

      Logical Channel Interface

   ---------------------------------------------------------------------------- */

/**
 * @brief Opaque data type representing an instance of the ISO-7816 State Machine.
 **/
typedef struct __tPSeHalSmInstance tPSeHalSmInstance;

/**
 * @brief Pointer-to-function data type that is used to open the basic channel or a logical channel.
 *
 * @param[in]  pContext  The current context.
 *
 * @param[in]  pInstance  The instance of the ISO-7816 State Machine.
 *
 * @param[in]  pCallback  The callback used to notify the channel opening completion.
 *
 * @param[in]  pCallbackParameter  The callback parameter.
 *
 * @param[in]  nType
 *
 * @param[in]  pAID  The AID of the application that must be selected on the newly opened
 *                logical channel. This can be the null pointer to indicate that the default
 *                application should be selected (in that case, nAIDLength must be 0).
 *
 * @param[in]  nAIDLength  The length of the AID, in range 5 to 16 (or 0 if pAID is null).
 *
 * @result     An error code, among which:
 *             - W_ERROR_OPERATION_PENDING, to indicate that the channel opening operation
 *                  is pending, and that completion shall be later notified using the callback.
 *             - W_ERROR_BAD_PARAMETER, to indicate that a passed parameter is incorrect.
 *             - W_ERROR_BAD_STATE, to indicate that an operation with the ISO-7816 card
 *                  is already ongoing.
 *             - W_ERROR_SECURITY if the opening a basic or logical channel is not allowed.
 *             - W_ERROR_EXCLUSIVE_REJECTED, if the raw channel is already opened.
 *             - W_ERROR_EXCLUSIVE_REJECTED, for the basic channel if the basic channel is already opened.
 *             - W_ERROR_FEATURE_NOT_SUPPORTED, if logical channels are not supported
 *                  by the ISO-7816 card.
 **/
typedef W_ERROR tPSeHalSmOpenLogicalChannel(
                  tContext* pContext,
                  tPSeHalSmInstance* pInstance,
                  tPBasicGenericDataCallbackFunction* pCallback,
                  void* pCallbackParameter,
                  uint32_t nType,
                  const uint8_t* pAID,
                  uint32_t nAIDLength);

/**
 * @brief Pointer-to-function data type that is used to close a channel. This may be
 *    the raw channel, the basic logical channel or any of the supplementary logical channels.
 *
 * @param[in]  pContext  The current context.
 *
 * @param[in]  pInstance  The instance of the ISO-7816 State Machine.
 *
 * @param[in]  nChannelReference  The reference of the channel to be closed.
 *             If nChannelReference is P_7816SM_NULL_CHANNEL, all open channels shall be closed.
 *
 * @param[in]  pCallback  The callback used to notify the channel opening completion.
 *
 * @param[in]  pCallbackParameter  The callback parameter number.
 *
 * @result     An error code, among which:
 *             - W_SUCCESS, to indicate that the raw channel or the basic logical channel
 *                  has been successfully closed.
 *             - W_ERROR_OPERATION_PENDING, to indicate that the channel closing operation
 *                  is pending, and that completion shall be later notified using the callback.
 *             - W_ERROR_BAD_PARAMETER, to indicate that a passed parameter is incorrect.
 *             - W_ERROR_BAD_STATE, to indicate that an operation with the ISO-7816 card
 *                  is already ongoing.
 *             - W_ERROR_ITEM_NOT_FOUND, to indicate that the channel is not associated
 *                  with the instance.
 *             - W_ERROR_BAD_STATE, to indicate that the channel is already closed.
 **/
typedef W_ERROR tPSeHalSmCloseChannel(
                  tContext* pContext,
                  tPSeHalSmInstance* pInstance,
                  uint32_t nChannelReference,
                  tPBasicGenericCallbackFunction* pCallback,
                  void* pCallbackParameter);

/**
 * @brief Pointer-to-function data type that is used to exchange an APDU command on a channel.
 *
 * @param[in]  pContext  The current context.
 *
 * @param[in]  pInstance  The instance of the ISO-7816 State Machine.
 *
 * @param[in]  nChannelReference  The reference of the channel to be used to send the APDU command.
 *
 * @param[in]  pCallback  The callback used to notify the channel opening completion.
 *
 * @param[in]  pCallbackParameter  The callback parameter.
 *
 * @param[in]  pSendApduBuffer  The buffer containing the APDU command to send.
 *
 * @param[in]  nSendApduBufferLength The length of the APDU command.
 *
 * @param[in]  pReceivedApduBuffer  The buffer that shall receive the APDU response.
 *
 * @param[in]  nReceivedApduBufferMaxLength  The length of the APDU response buffer.
 *
 * @result     An error code, among which:
 *             - W_ERROR_OPERATION_PENDING, to indicate that the APDU exchange operation
 *                  is pending, and that completion shall be later notified using the callback.
 *             - W_ERROR_BAD_PARAMETER, to indicate that a passed parameter is incorrect.
 *             - W_ERROR_BAD_STATE, to indicate that an operation with the ISO-7816 card
 *                  is already ongoing.
 *             - W_ERROR_ITEM_NOT_FOUND, to indicate that the channel is not associated
 *                  with the instance.
 *             - W_ERROR_BAD_STATE, to indicate that the channel is already closed.
 *             - W_ERROR_SECURITY if the specified APDU is not allowed.
 **/
typedef W_ERROR tPSeHalSmExchangeApdu(
                  tContext* pContext,
                  tPSeHalSmInstance* pInstance,
                  uint32_t nChannelReference,
                  tPBasicGenericDataCallbackFunction* pCallback,
                  void* pCallbackParameter,
                  const uint8_t* pSendApduBuffer,
                  uint32_t nSendApduBufferLength,
                  uint8_t* pReceivedApduBuffer,
                  uint32_t nReceivedApduBufferMaxLength);

/**
 * @brief Pointer-to-function data type that is used to return some data from a channel.
 *
 * @param[in]  pContext  The current context.
 *
 * @param[in]  pInstance  The instance of the ISO-7816 State Machine.
 *
 * @param[in]  nChannelReference  The reference of the channel.
 *
 * @param[in]  nType  The type of the data to retreive:
 *              - P_7816SM_DATA_TYPE_IS_RAW_CHANNEL return W_SUCCESS if the channel is the raw channel, an error code if not
 *              - P_7816SM_DATA_TYPE_IS_BASIC_CHANNEL return W_SUCCESS if the channel is the basic channel, an error code if not
 *              - P_7816SM_DATA_TYPE_AID returns the AID slected on this channel.
 *              - P_7816SM_DATA_TYPE_LAST_RESPONSE_APDU returns the last response APDU received on the channel, including the status word.
 *
 * @param[in]  pBuffer  The buffer receiving the data.
 *
 * @param[in]  nBufferMaxLength  The length in bytes of the buffer.
 *
 * @param[out] pnActualLength  The actual length in bytes of the data stored in the buffer.
 *
 * @result     An error code, among which:
 *             - W_SUCCESS, to indicate that the data is returned. If the data
 *                  is not available, *pnActualLength contains 0.
 *             - W_ERROR_BAD_PARAMETER, to indicate that a parameter is incorrect.
 *             - W_ERROR_ITEM_NOT_FOUND, to indicate that the channel is not associated
 *                  with the instance.
 *             - W_ERROR_BUFFER_TOO_SHORT, to indicate that the buffer is too
 *                  short. The required length is then available in *pnAidActualLength.
 **/
typedef W_ERROR tPSeHalSmGetData(
                  tContext* pContext,
                  tPSeHalSmInstance* pInstance,
                  uint32_t nChannelReference,
                  uint32_t nType,
                  uint8_t* pBuffer,
                  uint32_t nBufferMaxLength,
                  uint32_t* pnActualLength);

/**
 * @brief The default instance of the State Machine interface.
 *
 * All pointer-to-function data fields are guaranteed to be non-null pointers.
 **/
extern tP7816SmInterface g_sPSeHalSmInterface;

/**
 * @brief Creates a new instance of the ISO-7816 State Machine.
 *
 * @param[in]  pContext  The current context.
 *
 * @param[in]  pRawInterface  The communication interface to the ISO-7816 card.
 *
 * @param[in]  pRawInstance  The instance of the ISO-7816 card.
 *
 * @param[out] ppInstance  The pointer that is used to receive the newly created
 *                 ISO-7816 State Machine instance.
 *
 * @result     An error code, among which:
 *             - W_SUCCESS, to indicate success.
 *             - W_ERROR_BAD_PARAMETER, to indicate that a passed parameter is incorrect.
 *             - W_ERROR_OUT_OF_RESOURCE, in case of an out-of-memory condition.
 **/
W_ERROR PSeHalSmCreateInstance(
                  tContext* pContext,
                  tPSeHalSmRawInterface* pRawInterface,
                  tPSeHalSmRawInstance* pRawInstance,
                  tPSeHalSmInstance** ppInstance);

/**
 * @brief Destroys an ISO-7816 State Machine.
 *
 * Associated memory is freed and the passed instance must not be used any longer.
 *
 * @param[in]  pContext  The current context.
 *
 * @param[in]  pInstance  The instance of the ISO-7816 State Machine.
 *
 **/
void PSeHalSmDestroyInstance(
                  tContext* pContext,
                  tPSeHalSmInstance* pInstance);

#endif /* (P_CONFIG_DRIVER ||P_CONFIG_MONOLITHIC) && P_INCLUDE_SE_SECURITY */

#endif /* __WME_SE_HAL_STATE_MACHINE_H */
