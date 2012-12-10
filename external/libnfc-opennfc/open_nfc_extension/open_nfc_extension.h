/**
 Copyright (C) 2010 Inside Contactless

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

#ifndef	OPEN_NFC_EXTENSION_H
#	define	OPEN_NFC_EXTENSION_H

#define NFC_EXT_SOCKET "nfcext"

#include <unistd.h>
#include <stdint.h>
#include <open_nfc.h>
#include <pthread.h>
#include <utilities.h>

/*
#define DEBUG_MODE


#	ifdef DEBUG_MODE
#		define LogDebug(format, ...)              __android_log_print(ANDROID_LOG_DEBUG,  __FILE__, format, ##__VA_ARGS__)
#		define LogInformation(format, ...)       __android_log_print(ANDROID_LOG_INFO,      __FILE__, format, ##__VA_ARGS__)
#	else
#		define LogDebug(format, ...)
#		define LogInformation(format, ...)
#	endif

#define LogWarning(format, ...) __android_log_print(ANDROID_LOG_WARN, TAG, format, ##__VA_ARGS__)
#define LogError(format, ...)   __android_log_print(ANDROID_LOG_ERROR, TAG, format, ##__VA_ARGS__)
*/

	/* number of NFC extension clients (except UNKNOWN_CLIENT) */
	#define NFC_EXT_CLIENT_NUM 5

	typedef enum
	{
		OPEN_CONNECTION = 0x00,
		CLOSE_CONNECTION = 0x01,
		SEND_MSG = 0x02,
	} tOpenNFCExtCmd_ID;

	typedef enum
	{
		UNKNOWN_CLIENT = -1,
		OPEN_NFC_TEST = 0x00,
		OPEN_NFC_CARD_EMULATION_POLICY = 0x01,
	} tOpenNFCExtClient_ID;

#define	OPEN_NFC_GET_CARD_EMULATION_POLICY	((uint8_t)0x00)
#define	OPEN_NFC_SET_CARD_EMULATION_POLICY	((uint8_t)0x01)

	typedef enum
	{
		OPEN_NFC_EXTERNAL_API_SUCCES = 0x00,
		OPEN_NFC_EXTERNAL_API_FAILED = 0x01,
		OPEN_NFC_EXTERNAL_API_CARD_EMULATION_NOT_AVAILABLE = 0x02,
	}tOpenNFCExternalAPIErrorCode;

	typedef pid_t tAppId;

	typedef struct
	{
		tOpenNFCExtClient_ID 			clientId;
		tAppId 							appId;
		int 							reqId;
		tOpenNFCExternalAPIErrorCode	errStatus;
		uint8_t     					commandId;
		int								length;
	} tMessageOpenNFCHeader;

	typedef struct MessageOpenNFC
	{
		tMessageOpenNFCHeader	header;
		uint8_t*				pData;
	} tMessageOpenNFC;

/*	callback function to be called when the client's message received */
	typedef void (*openNfcExtCallback_t) (tMessageOpenNFC* pMessage);

	typedef struct OpenNFCExtClient
	{
		int						fdSocket;
		pthread_mutex_t			mutex;
//		tOpenNFCExtClient_ID	clientId;
	} tOpenNFCExtClient;

	tMessageOpenNFC* allocMessage(tMessageOpenNFCHeader* pHeader);
	void freeMessage(tMessageOpenNFC* pMsg);

	/*
	 * Open NFC extension server functions:
	 */

/* start NFC extension server */
	bool_t startOpenNfcExtServer();

/*	registration of a callback function to be called when the client's message received */
	bool_t registerOpenNfcExtCallback(tOpenNFCExtClient_ID clientId, openNfcExtCallback_t fCallback);

/* sends the notification message to the NFC extension client */
	bool_t sendOpenNfcExtNotification(tMessageOpenNFC* pMsg);


	/*
	 * Open NFC extension client functions:
	 */

/* open NFC extension client (to communicate with OpenNFC internal stuff) */
	bool_t openOpenNfcExtConnection();

/* close NFC extension client (to communicate with OpenNFC internal stuff) */
	bool_t closeOpenNfcExtConnection();

/* send a message from the NFC extension client */
	bool_t sendOpenNfcExtMessage(tMessageOpenNFC* pMsg);

/*	callback function to be called when the notification for Open NFC Extension Manager's reply is received */
	void openNfcExtNotifCallback (tMessageOpenNFC* pMessage);


	/*
	 * Open NFC extension utilities:
	 */
	tMessageOpenNFC * createMessageOpenNFC(tOpenNFCExtClient_ID clientID, tAppId applicationID, int requestID, tOpenNFCExternalAPIErrorCode error, uint8_t commandID, int length);

	void serializeUint32(uint8_t* data, int length, int * offset, uint32_t integer);
	uint32_t parseUint32(uint8_t* data, int length, int * offset);

#endif	//OPEN_NFC_EXTENTION
