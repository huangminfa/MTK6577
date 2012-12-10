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

#include <unistd.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <pthread.h>
#include <android/log.h>
#include "open_nfc_extension.h"
#include "card_emulation_ext.h"
#include "queue.h"
#include <sys/queue.h>

/* tag for log */
#define TAG "OpenNFCExtServer"

static pthread_t openNfcExtServerThreadId;

/* callback registry table: index is a client's id, i.e. tOpenNFCExtClient_ID */
static openNfcExtCallback_t callbackRegistry[NFC_EXT_CLIENT_NUM] =
{ NULL };

/* a queue to send notifications to Open NFC ext. clients */
static tQueue notifQueue;

/* a mutex for access to the list of the client connections */
static pthread_mutex_t extConListMutex;

/* list of client connections */
LIST_HEAD( tConListHead, extConnection)
conListHead;
struct tConListHead *pHead; /* List head. */
typedef struct extConnection
{
	LIST_ENTRY(extConnection) entries;
	int fdSocket;
	int appId;
} tExtConnection;

static int removeClientConnection(int clientSocket);

/*	registration of a callback function to be called when the client's message received */
bool_t registerOpenNfcExtCallback(tOpenNFCExtClient_ID clientId, openNfcExtCallback_t fCallback)
{
	LogInformation("registerOpenNfcExtCallback : clientId=%d, fCallback=0x%X", clientId, fCallback);
	if (clientId >= NFC_EXT_CLIENT_NUM)
	{
		LogWarning("registerOpenNfcExtCallback: wrong clientId (%d)", clientId);
		return W_FALSE;
	}
	callbackRegistry[clientId] = fCallback;
	LogInformation("registerOpenNfcExtCallback : clientId=%d, callbackRegistry[clientId]=0x%X", clientId, callbackRegistry[clientId]);
	return W_TRUE;
}

/*
 * The thread to receive commands from the clients
 */
static void* openNfcExtClientCmdThread(void* args)
{
	int fdClientSocket = (int) args;
	int cmd;
//	tOpenNFCExtClient_ID clientId = UNKNOWN_CLIENT;
	int length = -1;
	int status;
//	static tAppId appId = -1;
	tAppId appId = -1;
	tMessageOpenNFC* pMsg;
	tMessageOpenNFCHeader header;

	LogInformation("openNfcExtClientCmdThread : fdClientSocket=%d", fdClientSocket);

	while (W_TRUE)
	{
		if (read(fdClientSocket, &cmd, sizeof(cmd)) == 0)
		{
			LogWarning("openNfcExtClientCmdThread: can't get cmd");
		}
		else
		{
			LogWarning("openNfcExtClientCmdThread: cmd = %d", cmd);
			switch (cmd)
			{
				case OPEN_CONNECTION:

					if (read(fdClientSocket, &appId, sizeof(appId)) <= 0)
					{
						LogWarning("openNfcExtClientCmdThread: can't get appId");
					}
					else
					{
						tExtConnection* pNewCon = malloc(sizeof(tExtConnection));
						if (pNewCon == NULL)
						{
							LogError("openNfcExtClientCmdThread: malloc error");
						}
						else
						{
							LogInformation( "openNfcExtClientCmdThread : OPEN_CONNECTION: appId=%d", appId);

							pNewCon->appId = appId;
							pNewCon->fdSocket = fdClientSocket;

							/* add connection to the list */
							pthread_mutex_lock(&extConListMutex);
							LIST_INSERT_HEAD(&conListHead, pNewCon, entries);
							pthread_mutex_unlock(&extConListMutex);
						}
					}
					break;

				case SEND_MSG:
					if (read(fdClientSocket, &header, sizeof(header)) < (int) sizeof(header))
					{
						LogWarning("openNfcExtClientCmdThread: can't get message header");
					}
					else
					{
						if (header.clientId >= NFC_EXT_CLIENT_NUM)
						{
							LogWarning( "openNfcExtClientCmdThread: wrong clientId (%d)", header.clientId);
							header.clientId = UNKNOWN_CLIENT;
						}
						else
						{
							LogInformation( "openNfcExtClientCmdThread : SEND_MSG: length=%d, status=%d", header.length, status);
							pMsg = allocMessage(&header);
/*
							if (pMsg != NULL)
							{
								if (header.length > 0)
								{
									if ((status = read(fdClientSocket, pMsg->pData, header.length)) == -1)
									{
										LogWarning( "openNfcExtClientCmdThread: can't get msg");
									}
									else
									{
										if (header.clientId == UNKNOWN_CLIENT)
										{
											LogWarning( "openNfcExtClientCmdThread: clientId is not set");
										}
										else
										{
											openNfcExtCallback_t pCallback = callbackRegistry[header.clientId];
											LogInformation( "openNfcExtClientCmdThread : clientId=%d, pCallback=0x%X",
													header.clientId, pCallback);
											if (pCallback != NULL)
											{
												pCallback(pMsg);
											}
										}
									}
									freeMessage(pMsg);
								}
								else
								{
									LogWarning("openNfcExtClientCmdThread : SEND_MSG: received message with length=0");
								}
							}
*/

							if (pMsg != NULL)
							{
								if (header.length > 0)
								{
									if ((status = read(fdClientSocket, pMsg->pData, header.length)) == -1)
									{
										LogWarning( "openNfcExtClientCmdThread: can't get msg");
									}
								} else {
									status = 0;
								}
								if (status != -1) {
									if (header.clientId == UNKNOWN_CLIENT)
									{
										LogWarning( "openNfcExtClientCmdThread: clientId is not set");
									}
									else
									{
										openNfcExtCallback_t pCallback = callbackRegistry[header.clientId];
										LogInformation( "openNfcExtClientCmdThread : clientId=%d, pCallback=0x%X",
												header.clientId, pCallback);
										if (pCallback != NULL)
										{
											pCallback(pMsg);
										}
									}
								}
								freeMessage(pMsg);
							}
						}
					}
					break;

				case CLOSE_CONNECTION:
					/* remove the connection from the list */
					removeClientConnection(fdClientSocket);

					/* close the connection */
					close(fdClientSocket);
					return 0;
			}
		}
	}
}

/* sends the notification message to the NFC extension client */
bool_t sendOpenNfcExtNotification(tMessageOpenNFC* pMsg)
{
	LogInformation("sendOpenNfcExtNotification : pMsg->length=%d, appId=%d", pMsg->header.length, pMsg->header.appId);
	return putToQueue(&notifQueue, pMsg);
}

/* writes the notification message to the client's socket */
static bool_t writeNotification(int fdClientSocket, tMessageOpenNFC* pMsg)
{
	int status;

	LogInformation("writeNotification : clientId=%d, length=%d", pMsg->header.clientId, pMsg->header.length);

	/* no need for a mutex, cause it can be called just in one thread */
	status = write(fdClientSocket, &pMsg->header, sizeof(pMsg->header));
	if ((status != -1) && (pMsg->header.length > 0))
	{
		status = write(fdClientSocket, pMsg->pData, pMsg->header.length);
	}

	if (status == -1)
	{
		LogError("writeNotification: write error");
		return W_FALSE;
	}
	return W_TRUE;
}

/*
 * Gets client socket for provided appId
 */
static int getClientSocket(tAppId appId)
{
	tExtConnection* pCon;
	int clientSocket = -1;

	LogInformation("getClientSocket : appId=%d", appId);
	pthread_mutex_lock(&extConListMutex);
	for (pCon = LIST_FIRST(&conListHead); pCon != NULL; pCon = LIST_NEXT(pCon, entries))
	{
		LogInformation("getClientSocket : pCon->appId=%d", pCon->appId);
		if (pCon->appId == appId)
		{
			clientSocket = pCon->fdSocket;
			break;
		}
	}
	pthread_mutex_unlock(&extConListMutex);
	LogInformation("getClientSocket : clientSocket=%d", clientSocket);
	return clientSocket;
}

/*
 * Removes the client connection with the provided socket id
 */
static int removeClientConnection(int clientSocket)
{
	tExtConnection* pCon;
	pthread_mutex_lock(&extConListMutex);
	for (pCon = LIST_FIRST(&conListHead); pCon != NULL; pCon = LIST_NEXT(pCon, entries))
	{
		if (pCon->fdSocket == clientSocket)
		{
			LIST_REMOVE(pCon, entries);
			free(pCon);
			break;
		}
	}
	pthread_mutex_unlock(&extConListMutex);
	return clientSocket;
}

/*
 * The thread to send notifications to the clients the clients
 */
static void* openNfcExtSendNotificationsThread(void* args)
{
	LogInformation("openNfcExtSendNotificationsThread : ENTER");
	initQueue(&notifQueue);
	tMessageOpenNFC* pMsg = NULL;
	int fdClientSocket;

	while (W_TRUE)
	{
		pMsg = getFromQueue(&notifQueue);
		LogInformation("openNfcExtSendNotificationsThread : pMsg=0x%X", pMsg);
		fdClientSocket = getClientSocket(pMsg->header.appId);
		LogInformation("openNfcExtSendNotificationsThread : fdClientSocket=%d", fdClientSocket);
		if (fdClientSocket != -1)
		{
			writeNotification(fdClientSocket, pMsg);
		}
		else
		{
			LogWarning("openNfcExtSendNotificationsThread: can't find connection");
		}
		freeMessage(pMsg);
	}
	return NULL;
}

/*
 * The thread to receive connections from the clients
 */
static void* openNfcExtServerThread(void* args)
{
	int fdSocket;
	struct sockaddr_un name;
	int status;
	pthread_t openNfcExtSendNotificationsThreadId, openNfcExtClientThreadId;

	LogInformation("openNfcExtServerThread : ENTER");

//	memset(callbackRegistry, 0, sizeof(callbackRegistry));

	fdSocket = socket(AF_UNIX, SOCK_STREAM, 0);
	if (fdSocket == -1)
	{
		LogError("openNfcExtServerThread: socket error");
		return W_FALSE;
	}

	LogInformation("openNfcExtServerThread : socket=%d", fdSocket);
	memset(&name, 0, sizeof(name));
	name.sun_family = AF_UNIX;
	strcpy(&name.sun_path[1], NFC_EXT_SOCKET);

	status = bind(fdSocket, (struct sockaddr*) &name, sizeof(name));
	if (status == -1)
	{
		LogError("openNfcExtServerThread: bind error");
		return W_FALSE;
	}
	/* Listen for connections. */
	status = listen(fdSocket, 5);
	if (status == -1)
	{
		LogError("openNfcExtServerThread: listen error");
		return W_FALSE;
	}

	/*	initialization of connections list */
	LIST_INIT(&conListHead);

	pthread_mutex_init(&extConListMutex, NULL);

	/* start thread to send notifications to the clients */
	if (pthread_create(&openNfcExtSendNotificationsThreadId, NULL, openNfcExtSendNotificationsThread, NULL) != 0)
	{
		LogError("Can't launch thread for openNfcExtSendNotificationsThread");
		return W_FALSE;
	}

	/* Repeatedly accept connections, spinning off new thread to get commands from the client */
	while (W_TRUE)
	{
		struct sockaddr_un clientName;
		socklen_t clientNameLen;
		int fdClientSocket;
		/* Accept a connection */
		fdClientSocket = accept(fdSocket, &clientName, &clientNameLen);
		if (fdSocket == -1)
		{
			LogWarning("openNfcExtServerThread: accept error");
		}

		/* start thread to communicate with the client (i.e. to receive commands) */
		if (pthread_create(&openNfcExtClientThreadId, NULL, openNfcExtClientCmdThread, (void*) fdClientSocket) != 0)
		{
			LogWarning("Can't launch thread for openNfcExtClientCmdThread");
		}
	}
	/* Remove the socket file */
	close(fdSocket);
	unlink('/' + NFC_EXT_SOCKET);

	LogInformation("openNfcExtServerThread : EXIT");
	return W_TRUE;
}

/* start NFC extension server */
bool_t startOpenNfcExtServer()
{
	LogInformation("startOpenNfcExtServer");
	initializeCardEmulationExt();

	/* start thread to communicate with clients */
	if (pthread_create(&openNfcExtServerThreadId, NULL, openNfcExtServerThread, NULL) != 0)
	{
		LogError("Can't launch thread for openNfcExtServerThread");
		return W_FALSE;
	}
	return W_TRUE;
}

/**
 * Parse an integer from a byte array
 * @param[in]		data	Byte array to extract the integer
 * @param[in]		length	Byte array length
 * @param[in/out]	offset	Offset to start reading. After reading the integer, the offset is place after the integer
 * @return	Parsed integer
 */
static int parseInteger(uint8_t* data, int length, int * offset)
{
	if ((*offset) + 4 > length)
	{
		LogError("Not enough data to extract integer length=%d offset=%d", length, *offset);

		return -1;
	}

	int result = (*(data + (*offset)) & 0xFF) << 24     |
			     (*(data + (*offset) + 1) & 0xFF) << 16 |
			     (*(data + (*offset) + 2) & 0xFF) << 8  |
			     (*(data + (*offset) + 3) & 0xFF);

	*offset += 4;

	return result;
}

uint32_t parseUint32(uint8_t* data, int length, int * offset)
{
	if ((*offset) + 4 > length)
	{
		LogError("Not enough data to extract integer length=%d offset=%d", length, *offset);

		return -1;
	}

	uint32_t result = (uint32_t)((*(data + (*offset)) & 0xFF) << 24)     |
			          (uint32_t)((*(data + (*offset) + 1) & 0xFF) << 16) |
					  (uint32_t)((*(data + (*offset) + 2) & 0xFF) << 8)  |
					  (uint32_t)((*(data + (*offset) + 3) & 0xFF));

	*offset += 4;

	return result;
}


/**
 * Serialize integer inside a byte array
 * @param[in]		data	Array where write
 * @param[in]		length	Array length
 * @param[in/out]	offset	Offset to start writing. After writing the integer, the offset is place after the integer
 * @param[in]		integer	Integer to write
 */
static void serializeInteger(uint8_t* data, int length, int * offset, int integer)
{
	if ((*offset) + 4 > length)
	{
		LogError("Not enough data to serialize integer length=%d offset=%d", length, *offset);

		return;
	}

	*(data + (*offset)) = (uint8_t)((integer >> 24) & 0xFF);
	*(data + (*offset) + 1) = (uint8_t)((integer >> 16) & 0xFF);
	*(data + (*offset) + 2) = (uint8_t)((integer >> 8) & 0xFF);
	*(data + (*offset) + 3) = (uint8_t)(integer & 0xFF);

	*offset += 4;
}

void serializeUint32(uint8_t* data, int length, int * offset, uint32_t integer)
{
	if ((*offset) + 4 > length)
	{
		LogError("Not enough data to serialize integer length=%d offset=%d", length, *offset);

		return;
	}

	*(data + (*offset)) = (uint8_t)((integer >> 24) & 0xFF);
	*(data + (*offset) + 1) = (uint8_t)((integer >> 16) & 0xFF);
	*(data + (*offset) + 2) = (uint8_t)((integer >> 8) & 0xFF);
	*(data + (*offset) + 3) = (uint8_t)(integer & 0xFF);

	*offset += 4;
}


tMessageOpenNFC * createMessageOpenNFC(tOpenNFCExtClient_ID clientID, tAppId applicationID, int requestID, tOpenNFCExternalAPIErrorCode error, uint8_t commandID, int length)
{
	tMessageOpenNFCHeader messageHeader;
	messageHeader.clientId = clientID;
	messageHeader.appId = applicationID;
	messageHeader.reqId = requestID;
	messageHeader.errStatus = error;
	messageHeader.commandId = commandID;
	messageHeader.length = length;

	return allocMessage(& messageHeader);
}

