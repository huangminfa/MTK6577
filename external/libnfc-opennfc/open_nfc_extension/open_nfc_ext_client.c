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
#include <errno.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <android/log.h>
#include "open_nfc_extension.h"

/* tag for log */
#define TAG "OpenNFCExtClient"

/* the thread to receive Open NFC ext. notifications */
pthread_t openNfcExtRcvNotificationsThreadId;

/* the socket to communicate with the Open NFC extension server */
tOpenNFCExtClient extClient = { -1, {}};

static void* openNfcExtRcvNotificationsThread(void* args);

/* open NFC extension client (to communicate with OpenNFC internal stuff) */
bool_t openOpenNfcExtConnection()
{
	struct sockaddr_un name;
	int status;
	int cmd = (int) OPEN_CONNECTION;

	if (extClient.fdSocket != -1)
	{
		LogWarning("openOpenNfcExtConnection: already opened");
		return W_FALSE;
	}

	extClient.fdSocket = extClient.fdSocket = socket(AF_UNIX, SOCK_STREAM, 0);
	if (extClient.fdSocket == -1)
	{
		LogError("openOpenNfcExtConnection: socket error");
		return W_FALSE;
	}

	LogInformation("openOpenNfcExtConnection : socket = %d, getpid() = %d", extClient.fdSocket, getpid());

	memset(&name, 0, sizeof(name));
	name.sun_family = AF_UNIX;
	strcpy(&name.sun_path[1], NFC_EXT_SOCKET);

	status = connect(extClient.fdSocket, (struct sockaddr*) &name, sizeof(name));
	if (status == -1)
	{
		LogError("openOpenNfcExtConnection: connect error");
		return W_FALSE;
	}

	pthread_mutex_init(&extClient.mutex, NULL);

	pthread_mutex_lock(&extClient.mutex);
	status = write(extClient.fdSocket, &cmd, sizeof(cmd));
	if (status != -1)
	{
		tAppId appId = (int) getpid();
//		status = write(extClient.fdSocket, &nClientId, sizeof(nClientId));
		status = write(extClient.fdSocket, &appId, sizeof(appId));
	}
	pthread_mutex_unlock(&extClient.mutex);

	/* start thread to receive notifications from the server */
	if (pthread_create(&openNfcExtRcvNotificationsThreadId, NULL, openNfcExtRcvNotificationsThread, NULL) != 0)
	{
		LogError("Can't launch thread for openNfcExtRcvNotificationsThread");
		return W_FALSE;
	}

	if (status == -1)
	{
		LogError("openOpenNfcExtConnection: write error");
		return W_FALSE;
	}
	return W_TRUE;
}

/* close NFC extension client (to communicate with OpenNFC internal stuff) */
bool_t closeOpenNfcExtConnection()
{
	int status;
	int cmd = (int) CLOSE_CONNECTION;

	pthread_mutex_lock(&extClient.mutex);
	status = write(extClient.fdSocket, &cmd, sizeof(cmd));
	pthread_mutex_unlock(&extClient.mutex);

	pthread_mutex_destroy(&extClient.mutex);
	int socket = extClient.fdSocket;
	extClient.fdSocket = -1;
	close(socket);

	if (status == -1)
	{
		LogError("closeOpenNfcExtConnection: write error");
		return W_FALSE;
	}
	return W_TRUE;
}

/* send a message from the NFC extension client */
bool_t sendOpenNfcExtMessage(tMessageOpenNFC* pMsg)
{
	int status;
	int cmd = (int) SEND_MSG;
	if (pMsg == NULL)
	{
		LogError("sendOpenNfcExtMessage: pMsg NULL");
		return W_FALSE;
	}
	LogInformation("sendOpenNfcExtMessage : pMsg->length = %d", pMsg->header.length);
	pMsg->header.appId = (int) getpid();

	pthread_mutex_lock(&extClient.mutex);
	status = write(extClient.fdSocket, &cmd, sizeof(cmd));
	LogInformation("sendOpenNfcExtMessage : cmd - status = %d", status);

	if (status != -1)
	{
		status = write(extClient.fdSocket, &pMsg->header, sizeof(pMsg->header));
		LogInformation("sendOpenNfcExtMessage : clientId - header = %d", status);
	} else {
		LogError("sendOpenNfcExtMessage: can't send cmd (errno=0x%X)", errno);
	}
	if ((status != -1) && (pMsg->pData != NULL) && (pMsg->header.length > 0))
	{
		status = write(extClient.fdSocket, pMsg->pData, pMsg->header.length);
		LogInformation("sendOpenNfcExtMessage : data - status = %d", status);
	}
	pthread_mutex_unlock(&extClient.mutex);

	if (status == -1)
	{
		LogError("sendOpenNfcExtMessage: write error");
		return W_FALSE;
	}
	return W_TRUE;
}

/*
 * The thread to receive notifications from the server
 */
static void* openNfcExtRcvNotificationsThread(void* args)
{
//	tOpenNFCExtClient_ID clientId = UNKNOWN_CLIENT;
//	int length = -1;
	int status;
	tMessageOpenNFC* pMsg;
	tMessageOpenNFCHeader header;

	header.clientId = UNKNOWN_CLIENT;

	LogInformation("openNfcExtRcvNotificationsThread : socket=%d, thread_id=%d", extClient.fdSocket, gettid());
	while (W_TRUE)
	{
		if ((status = read(extClient.fdSocket, &header, sizeof(tMessageOpenNFCHeader))) < (int) sizeof(tMessageOpenNFCHeader))
		{
			LogError("openNfcExtRcvNotificationsThread: can't get clientId (status=%d), errno=%d", status, errno);
			// TODO: test
			return NULL;

			if (extClient.fdSocket == -1)
			{
				return NULL;
			}
		}
		else
		{
			LogInformation("openNfcExtRcvNotificationsThread : received notification: length=%d, clientId=%d",
					header.length, header.clientId);
			if (header.clientId >= NFC_EXT_CLIENT_NUM)
			{
				LogWarning( "openNfcExtRcvNotificationsThread: wrong clientId (%d)", header.clientId);
				header.clientId = UNKNOWN_CLIENT;
			}
			else
			{
				pMsg = allocMessage(&header);
				if (pMsg != NULL)
				{
					if ((status = read(extClient.fdSocket, pMsg->pData, header.length)) == -1)
					{
						LogWarning( "openNfcExtRcvNotificationsThread: can't get msg");
					}
					else
					{
						if (header.clientId == UNKNOWN_CLIENT)
						{
							LogWarning( "openNfcExtRcvNotificationsThread: clientId is not set");
						}
						else
						{
							/*	notify Open NFC Extension Manager that reply is received */
							openNfcExtNotifCallback(pMsg);
						}
					}
					freeMessage(pMsg);
				}
			}

		}
	}
	LogInformation("openNfcExtRcvNotificationsThread : finished");
	return NULL;
}
