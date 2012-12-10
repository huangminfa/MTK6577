/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <semaphore.h>
#include <errno.h>

#include "com_android_nfc.h"

namespace android {

typedef enum SocketStatus
{
	SOCKET_WAITING_FOR_CONNECTION,
	SOCKET_CONNECTED,
	SOCKET_ERROR
} SocketStatus;

typedef struct Socket
{
	uint8_t			index;
	SocketStatus	status;
	char16_t	*	serviceName;
	uint8_t			sap;
	W_HANDLE		handleLinkP2P;
	W_HANDLE		handleSocket;
	W_HANDLE		handleCancelEstablishLink;
	sem_t			semaphoreWaitConnection;
	sem_t			semaphoreWaitConnectionStep2;
} Socket;

#define	MAX_NUMBER_OF_SOCKET	8
static	Socket	sockets[MAX_NUMBER_OF_SOCKET];
static	uint8_t	indexNextSocket	=	0;

extern bool_t internalP2PWrite(JNIEnv * jniEnvironment, W_HANDLE handle, jbyteArray data);
extern jbyteArray internalReadP2P(JNIEnv * jniEnvironment, W_HANDLE handle);
extern W_HANDLE obtainServerSocketHandleOpenNFC(uint32_t handle);
extern bool_t isServerSoketConnected(uint32_t handle);

void initP2PSockets()
{
	memset(sockets, 0, sizeof(sockets));
}

bool_t isClientSoketValid(uint32_t handle)
{
	Socket * socket = & (sockets[GET_SOCKET_INDEX(handle)]);
	LogInformation("isClientSoketValid: status=%d", socket->status);

	switch(socket->status)
	{
		case SOCKET_ERROR :
			LogPosition("isClientSoketValid");
			return W_FALSE;
		case SOCKET_WAITING_FOR_CONNECTION :
//			sleep(1);
			break;
		case SOCKET_CONNECTED :
			break;
	}
	return W_TRUE;
}


static Socket * createSocket(uint8_t sap)
{
	LogInformation("createSocket: sap=%d", sap);
	uint8_t index = 0;
	for(;index<indexNextSocket;index++)
	{
		if((sockets[index].sap == sap) && (sockets[index].serviceName == NULL))
		{
			break;
		}
	}
	if(index>=indexNextSocket)
	{
		if(indexNextSocket>=MAX_NUMBER_OF_SOCKET)
		{
			LogError("No more space to create a new socket");
			return NULL;
		}
		LogInformation("createSocket: index=%d, sap=%d", index, sap);
		indexNextSocket ++;

		sockets[index].index = index;
		sockets[index].sap = sap;
		sockets[index].serviceName = NULL;

		SEMAPHORE_CREATE(& (sockets[index].semaphoreWaitConnection));
		SEMAPHORE_CREATE(& (sockets[index].semaphoreWaitConnectionStep2));
	}

	sockets[index].status = SOCKET_WAITING_FOR_CONNECTION;
	LogInformation("INDEX = %d",index);
	return & sockets[index];
}

static void linkEstablishedListener(void * parameter, W_HANDLE handleLinkP2P, W_ERROR error)
{
	LogInformation("linkEstablishedListener");

	Socket * socket = (Socket *)parameter;

	socket->handleLinkP2P = handleLinkP2P;

	if (error != W_SUCCESS)
	{
		LogError("linkEstablishedListener : Error while establish link : %x", error);

		socket->status = SOCKET_ERROR;

		SEMAPHORE_POST(&(socket->semaphoreWaitConnection));
		return;
	}
	W_ERROR result = WP2PCreateSocket
	(
		W_P2P_TYPE_CLIENT,
		socket->serviceName,
		socket->sap,
		&(socket->handleSocket)
	);
	LogInformation("linkEstablishedListener: handleSocket = %x, sap=%d",socket->handleSocket, socket->sap);


	if(result != W_SUCCESS)
	{
		LogError("linkEstablishedListener : Error while create socket : %x", result);

		socket->status = SOCKET_ERROR;

		SEMAPHORE_POST(&(socket->semaphoreWaitConnection));

		return;
	}
	socket->status = SOCKET_CONNECTED;
	SEMAPHORE_POST(&(socket->semaphoreWaitConnection));
}

/**
 * Called when RF link lost
 * @param	parameter	Socket description (Opaque)
 * @param	error		Error description
 */
static void linkReleaseListener(void * parameter, W_ERROR error)
{
	Socket * socket = (Socket *)parameter;

	socket->status = SOCKET_WAITING_FOR_CONNECTION;

	LogInformation("linkReleaseListener : socket->handleSocket=0x%x index=%d",socket->handleSocket, socket->index);
	LogInformation("com_NativeLlcpServiceSocket: close handles: handleSocket=0x%X",
			socket->handleSocket);

	WBasicCloseHandle(socket->handleSocket);

	switch (error) {
	case W_SUCCESS:
		break;
	case W_ERROR_TIMEOUT:
		LogWarning("linkReleaseListener : W_ERROR_TIMEOUT");
		break;
	default:
		LogWarning("linkReleaseListener : default error=%d", error);
		break;
	}
	LogPosition("linkReleaseListener");
}

uint32_t createSocketLinkedHandle(uint8_t sap)
{
	Socket * socket = createSocket(sap);

	if(socket==NULL)
	{
		LogPosition("createSocketLinkedHandle: socket=NULL");
		return 0;
	}

	LogInformation("Handle JAVA = %x",CREATE_CLIENT_SOCKET(socket->index));
	return (uint32_t)CREATE_CLIENT_SOCKET(socket->index);
}

jbyteArray socketTransceive(JNIEnv * jniEnvironment, uint32_t handle, jbyteArray data)
{
	W_HANDLE handleOpenNFC = 0;

	if(IS_SERVER_SOCKET(handle))
	{
		handleOpenNFC = obtainServerSocketHandleOpenNFC(handle);
	}
	else
	{
		handleOpenNFC=sockets[GET_SOCKET_INDEX(handle)].handleSocket;
	}

	if(internalP2PWrite(jniEnvironment, handleOpenNFC, data) == W_FALSE)
	{
		LogError("Error in socketTransceive, internalP2PWrite");
		return NULL;
	}
	return internalReadP2P(jniEnvironment, handleOpenNFC);
}

jbyteArray socketReceive(JNIEnv * jniEnvironment, uint32_t handle)
{
	W_HANDLE handleOpenNFC = 0;

	if(IS_SERVER_SOCKET(handle))
	{
		handleOpenNFC = obtainServerSocketHandleOpenNFC(handle);
	}
	else
	{
		handleOpenNFC=sockets[GET_SOCKET_INDEX(handle)].handleSocket;
	}
	return internalReadP2P(jniEnvironment, handleOpenNFC);
}

jboolean socketSend(JNIEnv * jniEnvironment, uint32_t handle, jbyteArray data)
{
	W_HANDLE handleOpenNFC = 0;

	if(IS_SERVER_SOCKET(handle))
	{
		if(isServerSoketConnected(handle)==W_FALSE)
		{
			return JNI_FALSE;
		}

		handleOpenNFC = obtainServerSocketHandleOpenNFC(handle);
	}
	else
	{
		if(sockets[GET_SOCKET_INDEX(handle)].status!=SOCKET_CONNECTED)
		{
			return JNI_FALSE;
		}

		handleOpenNFC=sockets[GET_SOCKET_INDEX(handle)].handleSocket;
	}

	if(internalP2PWrite(jniEnvironment, handleOpenNFC, data) == W_TRUE)
	{
		return JNI_TRUE;
	}
	return JNI_FALSE;
}

jboolean socketDisconnect(uint32_t handle)
{
	return JNI_TRUE;
}

static void connectListener(void *pCallbackParameter, W_ERROR nResult)
{
	LogInformation("Connection error=0x%x", nResult);

	if(nResult != W_SUCCESS)
	{
		((Socket *)(pCallbackParameter))->status = SOCKET_ERROR;
	}

	SEMAPHORE_POST(& (((Socket *)(pCallbackParameter))->semaphoreWaitConnectionStep2));
}

/*
 * Methods
 */
static jboolean com_android_nfc_NativeLlcpSocket_doConnect(JNIEnv *e, jobject o, jint nSap)
{
	jboolean status = JNI_TRUE;
	uint32_t handle = nfc_jni_get_nfc_socket_handle(e,o);

	Socket * socket = & (sockets[GET_SOCKET_INDEX(handle)]);

	socket->sap = nSap;
	socket->serviceName = NULL;

	WP2PEstablishLink
	(
			linkEstablishedListener, socket,
			linkReleaseListener, socket,
			& (socket->handleCancelEstablishLink)
	);

	SEMAPHORE_WAIT(&(socket->semaphoreWaitConnection));

	LogInformation("com_android_nfc_NativeLlcpSocket_doConnect: handle=0x%X, handleSocket=0x%X, handleLinkP2P=0x%X, sap=%d, nSap=%d",
			handle, socket->handleSocket, socket->handleLinkP2P, socket->sap, nSap);
	WP2PConnect(socket->handleSocket, socket->handleLinkP2P, connectListener, socket);
	SEMAPHORE_WAIT(& (socket->semaphoreWaitConnectionStep2));

	if(socket->status == SOCKET_ERROR)
	{
		LogError("Can't connect to the p2p device (sap=%d)", nSap);
		status = JNI_FALSE;
	}

	return status;
}

static jboolean com_android_nfc_NativeLlcpSocket_doConnectBy(JNIEnv *e, jobject o, jstring sn)
{
	jboolean status = JNI_TRUE;

	uint32_t handle = nfc_jni_get_nfc_socket_handle(e,o);

	Socket * socket = & (sockets[GET_SOCKET_INDEX(handle)]);

	/* Service socket */
	if (sn == NULL)
	{
		LogError("The service name for p2p connection is not set");
		return JNI_FALSE;
	}

	jboolean isCopy = JNI_TRUE;

	uint32_t serviceNameLength = (uint32_t) e->GetStringLength(sn);

	LogInformation("com_android_nfc_NativeLlcpSocket_doConnectBy: isCopy=%s length=%d, sn=BUG_FOUND",
			((isCopy==JNI_TRUE) ? "TRUE" : "FALSE"), serviceNameLength);

	socket->sap = 0;
	int length = (serviceNameLength + 1) * sizeof(uint16_t);
	socket->serviceName = (uint16_t*) malloc(length);
	if (socket->serviceName == NULL)
	{
		LogError("No memory for the service name for p2p connection");
		return JNI_FALSE;
	}
	jchar* pSrcName = (jchar*) e->GetStringChars(sn, &isCopy);
	memcpy(socket->serviceName, pSrcName, length);
	e->ReleaseStringChars(sn, pSrcName);

	WP2PEstablishLink
	(
			linkEstablishedListener, socket,
			linkReleaseListener, socket,
			& (socket->handleCancelEstablishLink)
	);

	SEMAPHORE_WAIT(&(socket->semaphoreWaitConnection));

	LogInformation("com_android_nfc_NativeLlcpSocket_doConnectBy: handle=0x%X, handleSocket=0x%X, handleLinkP2P=0x%X, sap=%d, sn=%s",
			handle, socket->handleSocket, socket->handleLinkP2P, socket->sap, socket->serviceName);
	WP2PConnect(socket->handleSocket, socket->handleLinkP2P, connectListener, socket);
	SEMAPHORE_WAIT(& (socket->semaphoreWaitConnectionStep2));

	if(socket->status == SOCKET_ERROR)
	{
		LogError("Can't connect to the p2p device (service  name = BUG_FOUND)");
		status = JNI_FALSE;
	}

	return status;
}

static jboolean com_android_nfc_NativeLlcpSocket_doClose(JNIEnv *e, jobject o)
{
	uint32_t handle = nfc_jni_get_nfc_socket_handle(e,o);
	if (IS_CLIENT_SOCKET(handle))
	{
		LogInformation("com_android_nfc_NativeLlcpSocket_doClose: handle=0x%X",handle);
		Socket * socket = & (sockets[GET_SOCKET_INDEX(handle)]);
		WBasicCancelOperation(socket->handleCancelEstablishLink);

		LogInformation("com_NativeLlcpServiceSocket: close handles: handleCancelEstablishLink=0x%X",
				socket->handleSocket);

		WBasicCloseHandle(socket->handleCancelEstablishLink);
		if(socket->serviceName != NULL)
		{
			free(socket->serviceName);
			socket->serviceName = NULL;
		}
		socket->sap = 0;
	}
	return JNI_TRUE;
}

static jboolean com_android_nfc_NativeLlcpSocket_doSend(JNIEnv *e, jobject o, jbyteArray  data)
{
	uint32_t handle = nfc_jni_get_nfc_socket_handle(e,o);

	return socketSend(e, handle, data);
}

static jint com_android_nfc_NativeLlcpSocket_doReceive(JNIEnv *e, jobject o, jbyteArray  buffer)
{
	uint32_t handle = nfc_jni_get_nfc_socket_handle(e,o);

	LogInformation("NativeLlcpSocket_doReceive(): Handle JAVA=0x%X", handle);

	W_HANDLE handleOpenNFC = 0;

	if(IS_SERVER_SOCKET(handle))
	{
		if(isServerSoketConnected(handle)==W_FALSE)
		{
			return -1;
		}
		handleOpenNFC = obtainServerSocketHandleOpenNFC(handle);
	}
	else
	{
		if(sockets[GET_SOCKET_INDEX(handle)].status!=SOCKET_CONNECTED)
		{
			return -1;
		}

		handleOpenNFC=sockets[GET_SOCKET_INDEX(handle)].handleSocket;
	}

	LogInformation("Handle Open NFC=%x",handleOpenNFC);

	uint32_t dataLength = 0;

	uint8_t * receiveData = (uint8_t*)e->GetByteArrayElements(buffer, NULL);
	uint32_t receiveLength = (uint32_t)e->GetArrayLength(buffer);

	W_ERROR error = WP2PReadSync
	(
		handleOpenNFC,
		receiveData, receiveLength,
		& dataLength
	);

	e->ReleaseByteArrayElements(buffer, (jbyte*)receiveData, 0);
	if(error != W_SUCCESS)
	{
		LogError("Error in com_android_nfc_NativeLlcpSocket_doReceive error=0x%x",error);
		return -1;
	}
	return (jint)dataLength;
}

static jint com_android_nfc_NativeLlcpSocket_doGetRemoteSocketMIU(JNIEnv *e, jobject o)
{
	uint32_t handle = nfc_jni_get_nfc_socket_handle(e,o);
	Socket* pSocket = &sockets[GET_SOCKET_INDEX(handle)];
	uint32_t remoteMIU = 0;
	W_HANDLE handleOpenNFC;

	if(IS_SERVER_SOCKET(handle))
	{
		handleOpenNFC = obtainServerSocketHandleOpenNFC(handle);
	}
	else
	{
		handleOpenNFC = sockets[GET_SOCKET_INDEX(handle)].handleSocket;
	}

	LogInformation("doGetRemoteSocketMIU(): handleSocket = 0x%X, isServerSocket = %d",
			handleOpenNFC, IS_SERVER_SOCKET(handle));
	W_ERROR error = WP2PGetSocketParameter(handleOpenNFC, W_P2P_REMOTE_MIU, &remoteMIU);
	if (error != W_SUCCESS)
	{
		LogWarning("Can't get Remote MIU (error=0x%X)", error);
		remoteMIU = 0;
	}
	else
	{
		LogInformation("remoteMIU = %d", remoteMIU);
	}
	return (jint) remoteMIU;
}

static jint com_android_nfc_NativeLlcpSocket_doGetRemoteSocketRW(JNIEnv *e, jobject o)
{
	uint32_t handle = nfc_jni_get_nfc_socket_handle(e,o);
	Socket* pSocket = &sockets[GET_SOCKET_INDEX(handle)];
	uint32_t remoteRW = 0;
	LogInformation("doGetRemoteSocketRW(): handleSocket = 0x%X", pSocket->handleSocket);
	W_ERROR error = WP2PGetSocketParameter(pSocket->handleSocket, W_P2P_REMOTE_RW, &remoteRW);
	if (error != W_SUCCESS)
	{
		LogWarning("Can't get Remote RW (error=0x%X)", error);
		remoteRW = 0;
	}
	else
	{
		LogInformation("remoteRW = %d", remoteRW);
	}
	return (jint) remoteRW;
}


/*
 * JNI registration.
 */
static JNINativeMethod gMethods[] =
{
   {"doConnect", "(I)Z",
      (void *)com_android_nfc_NativeLlcpSocket_doConnect},

   {"doConnectBy", "(Ljava/lang/String;)Z",
      (void *)com_android_nfc_NativeLlcpSocket_doConnectBy},
      
   {"doClose", "()Z",
      (void *)com_android_nfc_NativeLlcpSocket_doClose},
      
   {"doSend", "([B)Z",
      (void *)com_android_nfc_NativeLlcpSocket_doSend},

   {"doReceive", "([B)I",
      (void *)com_android_nfc_NativeLlcpSocket_doReceive},
      
   {"doGetRemoteSocketMiu", "()I",
      (void *)com_android_nfc_NativeLlcpSocket_doGetRemoteSocketMIU},
           
   {"doGetRemoteSocketRw", "()I",
      (void *)com_android_nfc_NativeLlcpSocket_doGetRemoteSocketRW},
};


int register_com_android_nfc_NativeLlcpSocket(JNIEnv *e)
{
   return jniRegisterNativeMethods(e,
      "com/android/nfc/nxp/NativeLlcpSocket",gMethods, NELEM(gMethods));
}

} // namespace android
