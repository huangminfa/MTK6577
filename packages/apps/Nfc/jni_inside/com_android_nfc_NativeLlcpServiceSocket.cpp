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

extern jmethodID cached_NfcManager_notifyLlcpLinkActivation;
extern jmethodID cached_NfcManager_notifyLlcpLinkDeactivated;
extern jobject genericCached_P2pDevice;
extern jobject genericManager;

namespace android
{

typedef enum ServerSocketStatus
{
	SERVER_SOCKET_WAITING_FOR_CONNECTION, SERVER_SOCKET_CONNECTED, SERVER_SOCKET_ERROR
} ServerSocketStatus;

typedef struct SocketServer
{
	uint8_t index;
	ServerSocketStatus status;
	char16_t * serviceName;
	uint8_t sap;
	W_HANDLE handleLinkP2P;
	W_HANDLE handleServerSocket;
	W_HANDLE handleCancelEstablishLink;
	sem_t semaphoreWaitConnection;
	sem_t semaphoreWaitConnectionStep2;
	tWP2PLinkProperties linkProperties;
	jobject tag;
} SocketServer;

#define	MAX_NUMBER_OF_SERVER_SOCKET	8
static SocketServer socketServers[MAX_NUMBER_OF_SERVER_SOCKET];
static uint8_t indexNextServerSocket = 0;

extern bool_t internalP2PWrite(JNIEnv * jniEnvironment, W_HANDLE handle, jbyteArray data);
extern jbyteArray internalReadP2P(JNIEnv * jniEnvironment, W_HANDLE handle);

static void establishLink(SocketServer* pSocketServer);

static SocketServer * createServerSocket(char16_t * serviceName, uint8_t sap)
{
	LogInformation("createServerSocket: sap=%d", sap);
	uint8_t index = 0;
	for (; index < indexNextServerSocket; index++)
	{
		if (socketServers[index].sap == sap)
		{
			break;
		}
	}

	if (index >= indexNextServerSocket)
	{
		if (indexNextServerSocket >= MAX_NUMBER_OF_SERVER_SOCKET)
		{
			LogError("No more space to create a new server socket");
			return NULL;
		}
		indexNextServerSocket++;

		socketServers[index].index = index;
		socketServers[index].sap = sap;
		socketServers[index].serviceName = serviceName;

		SEMAPHORE_CREATE(& (socketServers[index].semaphoreWaitConnection));
		SEMAPHORE_CREATE(& (socketServers[index].semaphoreWaitConnectionStep2));
	}
	socketServers[index].status = SERVER_SOCKET_WAITING_FOR_CONNECTION;
	return &socketServers[index];
}

static void linkEstablishedListener(void * parameter, W_HANDLE handleLinkP2P, W_ERROR error)
{
	SocketServer * socketServer = (SocketServer *) parameter;

	socketServer->handleLinkP2P = handleLinkP2P;
	if (error != W_SUCCESS)
	{
		LogError("linkEstablishedListener : Error while establish link : %x", error);

		socketServer->status = SERVER_SOCKET_ERROR;
		SEMAPHORE_POST(&(socketServer->semaphoreWaitConnection));
		return;
	}

	W_ERROR result = WP2PGetLinkProperties(handleLinkP2P, &(socketServer->linkProperties));
	if (result != W_SUCCESS)
	{
		LogWarning("linkEstablishedListener : Error while getting link properties : %x", result);
	}

	/* the link is established */
	socketServer->status = SERVER_SOCKET_CONNECTED;

	SEMAPHORE_POST(&(socketServer->semaphoreWaitConnection));
}

static void p2pShutdownCallback(void * parameter, W_ERROR error)
{
	LogInformation("p2pShutdownCallback: error=0x%X", error);
	SocketServer * socketServer = (SocketServer *) parameter;
}

/**
 * Called when RF link lost
 * @param	parameter	Socket description (Opaque)
 * @param	error		Error description
 */
static void linkReleaseListener(void * parameter, W_ERROR error)
{
	LogInformation("linkReleaseListener");

	SocketServer * socketServer = (SocketServer *) parameter;
	socketServer->status = SERVER_SOCKET_ERROR;

	//CallBack java
	if (socketServer->tag != NULL)
	{
		JavaVM * javaVM = getJavaVM();
		JNIEnv * jniEnvironment = NULL;

		javaVM->GetEnv((void **) &jniEnvironment, JNI_VERSION_1_6);
		jniEnvironment->CallVoidMethod(genericManager, cached_NfcManager_notifyLlcpLinkDeactivated, socketServer->tag);
		jniEnvironment->DeleteGlobalRef(socketServer->tag);
	}
	socketServer->tag = NULL;

	LogInformation("linkReleaseListener: close handles: handleCancelEstablishLink=0x%X, handleLinkP2P=0x%X",
			socketServer->handleCancelEstablishLink, socketServer->handleLinkP2P);
	WBasicCloseHandle(socketServer->handleCancelEstablishLink);
	WBasicCloseHandle(socketServer->handleLinkP2P);

	/* reestablish link */
	establishLink(socketServer);
}

uint32_t createServerSocketLinkedHandle(char16_t * serviceName, uint32_t serviceNameLength, uint8_t sap)
{
	LogInformation("createServerSocketLinkedHandle()");
	SocketServer * socketServer = createServerSocket(serviceName, sap);
	uint32_t length = (serviceNameLength+1) * sizeof(char16_t);

	if (socketServer == NULL)
	{
		LogError("Can't create server p2p socket (sap= %d)", sap);
		return 0;
	}

	socketServer->serviceName = (serviceName == NULL) ? NULL : (char16_t*) malloc(length);
	if (socketServer->serviceName != NULL)
	{
		memcpy(socketServer->serviceName, serviceName, length);
	}
//	socketServer->status = SERVER_SOCKET_WAITING_FOR_CONNECTION;
	socketServer->tag = NULL;

	/*
	 * Sockets are not associated to a specific P2P link, and remain valid as long as they have not been destroyed.
	 * They are typically created during application startup prior requesting establishment of the P2P link, and are
	 * destroyed only when the application is terminating.
	 */
	LogInformation("createServerSocketLinkedHandle(): serviceName=%s, sap=%d, serviceNameLength=%d",
			serviceName, sap, serviceNameLength);
	W_ERROR result = WP2PCreateSocket(W_P2P_TYPE_SERVER, socketServer->serviceName, socketServer->sap, &(socketServer->handleServerSocket));
	LogInformation("createServerSocketLinkedHandle(): WP2PCreateSocket() ret 0x%X", result);
	if (result != W_SUCCESS)
	{
		LogError("Can't create server p2p socket (error= 0x%X)", result);
		socketServer->status = SERVER_SOCKET_ERROR;
		return 0;
	}

	/* establish initial link */
	establishLink(socketServer);

	return (uint32_t) CREATE_SERVER_SOCKET(socketServer->index);
}

W_HANDLE obtainServerSocketHandleOpenNFC(uint32_t handle)
{
	return socketServers[GET_SOCKET_INDEX(handle)].handleServerSocket;
}

bool_t isServerSoketConnected(uint32_t handle)
{
	SocketServer * socketServer = &(socketServers[GET_SOCKET_INDEX(handle)]);

	LogInformation("isServerSoketConnected(): status = %d", socketServer->status);
	if(socketServer->status == SERVER_SOCKET_CONNECTED)
	{
		return W_TRUE;
	}
	return W_FALSE;
}

bool_t isServerSoketValid(uint32_t handle)
{
	SocketServer * socketServer = &(socketServers[GET_SOCKET_INDEX(handle)]);

	LogInformation("isServerSocketValid: status=%d", socketServer->status);
	return (socketServer->status != SERVER_SOCKET_ERROR);
}

jbyteArray serverSocketTransceive(JNIEnv * jniEnvironment, uint32_t handle, jbyteArray data)
{
	SocketServer * socketServer = &(socketServers[GET_SOCKET_INDEX(handle)]);

	W_HANDLE handleOpenNFC = socketServer->handleServerSocket;
	if (internalP2PWrite(jniEnvironment, handleOpenNFC, data) == W_FALSE)
	{
		LogError("Error in serverSocketTransceive, internalP2PWrite");
		return NULL;
	}
	return internalReadP2P(jniEnvironment, handleOpenNFC);
}

jbyteArray serverSocketReceive(JNIEnv * jniEnvironment, uint32_t handle)
{
	SocketServer * socketServer = &(socketServers[GET_SOCKET_INDEX(handle)]);

	W_HANDLE handleOpenNFC = socketServer->handleServerSocket;
	return internalReadP2P(jniEnvironment, handleOpenNFC);
}

jboolean serverSocketSend(JNIEnv * jniEnvironment, uint32_t handle, jbyteArray data)
{
	SocketServer * socketServer = &(socketServers[GET_SOCKET_INDEX(handle)]);

	W_HANDLE handleOpenNFC = socketServer->handleServerSocket;
	if (internalP2PWrite(jniEnvironment, handleOpenNFC, data) == W_TRUE)
	{
		return JNI_TRUE;
	}
	return JNI_FALSE;
}

jboolean serverSocketDisconnect(uint32_t handle)
{
	return JNI_TRUE;
}

static void establishLink(SocketServer* pSocketServer)
{
	pSocketServer->tag = NULL;
//	pSocketServer->status = SERVER_SOCKET_WAITING_FOR_CONNECTION;

	LogInformation("establishLink(): sap=%d", pSocketServer->sap);
	WP2PEstablishLink(linkEstablishedListener, pSocketServer, linkReleaseListener, pSocketServer,
			&pSocketServer->handleCancelEstablishLink);
}

static void connectServerListener(void *pCallbackParameter, W_ERROR nResult)
{
	SocketServer* pSocketServer = (SocketServer*) pCallbackParameter;
	LogInformation("connectServerListener(): error=0x%X, handleServerSocket=0x%X, handleLinkP2P=0x%X", nResult,
			pSocketServer->handleServerSocket, pSocketServer->handleLinkP2P);

	pSocketServer->status = SERVER_SOCKET_CONNECTED;

	if (nResult != W_SUCCESS)
	{
		LogError("connectServerListener : Can't connect to the p2p client (error = 0x%X)", nResult);
		pSocketServer->status = SERVER_SOCKET_ERROR;
	}
	SEMAPHORE_POST(&pSocketServer->semaphoreWaitConnectionStep2);
}
/*
 * Methods
 */
static jobject com_NativeLlcpServiceSocket_doAccept(JNIEnv *e, jobject o, jint miu, jint rw, jint linearBufferLength)
{
	jclass tag_cls = NULL;
	jobject tag;
	jmethodID ctor;
	jfieldID f;
	jclass clsNativeLlcpSocket;
	jobject clientSocket = NULL;
	uint32_t handle = nfc_jni_get_nfc_socket_handle(e, o);
	SocketServer * socketServer = &(socketServers[GET_SOCKET_INDEX(handle)]);

	do
	{
		LogInformation("com_NativeLlcpServiceSocket_doAccept BEFORE WAIT_1: sap=%d", socketServer->sap);
		SEMAPHORE_WAIT(&(socketServer->semaphoreWaitConnection));

		LogInformation("doAccept() AFTER WAIT_1: sap= %d, handleServerSocket=0x%X, handleLinkP2P=0x%X",
				socketServer->sap, socketServer->handleServerSocket, socketServer->handleLinkP2P);

		if (socketServer->status != SERVER_SOCKET_CONNECTED)
		{
			LogError("ATTENTION!: Error happened during server socket creation");
			continue;
		}
		//Signal that link established
		jbyteArray generalBytes = NULL;
		tag_cls = e->GetObjectClass(genericCached_P2pDevice);
		if (e->ExceptionCheck())
		{
			LogError("Get Object Class Error");
			return NULL;
		}
/*		 New target instance*/
		ctor = e->GetMethodID(tag_cls, "<init>", "()V");
		tag = e->NewObject(tag_cls, ctor);

/*		 Set P2P Target mode*/
		f = e->GetFieldID(tag_cls, "mMode", "I");
		LogPosition("com_NativeLlcpServiceSocket_doAccept");
		if (socketServer->linkProperties.bIsInitiator)
		{
			LogPosition("com_NativeLlcpServiceSocket_doAccept");
			LogInformation("The device is P2P Initiator");
			e->SetIntField(tag, f, (jint) MODE_P2P_INITIATOR);

			f = e->GetFieldID(tag_cls, "mGeneralBytes", "[B");
			generalBytes = e->NewByteArray(GENERAL_BYTES_LENGTH);
			e->SetByteArrayRegion(generalBytes, 0, GENERAL_BYTES_LENGTH, (jbyte *) GENERAL_BYTES);
			e->SetObjectField(tag, f, generalBytes);
		}
		else
		{
			LogPosition("com_NativeLlcpServiceSocket_doAccept");
			LogInformation("The device is P2P Target");
			e->SetIntField(tag, f, (jint) MODE_P2P_TARGET);
		}

		LogPosition("com_NativeLlcpServiceSocket_doAccept");
		f = e->GetFieldID(tag_cls, "mHandle", "I");
		e->SetIntField(tag, f, (jint) handle);
		LogPosition("com_NativeLlcpServiceSocket_doAccept");
		socketServer->tag = e->NewGlobalRef(tag);
		e->CallVoidMethod(genericManager, cached_NfcManager_notifyLlcpLinkActivation, tag);
		if (e->ExceptionCheck())
		{
			LogPosition("com_NativeLlcpServiceSocket_doAccept");
			LogError("Exception occured");

			return NULL;
		}

		//Wait for connection with client
		LogInformation("doAccept() BEFORE WAIT_2: calling WP2PConnect(): sap= %d",
				socketServer->sap);
		WP2PConnect(socketServer->handleServerSocket, socketServer->handleLinkP2P, connectServerListener, socketServer);
		SEMAPHORE_WAIT(& (socketServer->semaphoreWaitConnectionStep2));
		LogInformation("doAccept() AFTER WAIT_2: sap= %d",
				socketServer->sap);

		if (socketServer->status == SERVER_SOCKET_ERROR)
		{
			LogError("Can't connect to the p2p client");

			LogInformation("com_NativeLlcpServiceSocket_doAccept: close handles: handleCancelEstablishLink=0x%X, handleLinkP2P=0x%X",
					socketServer->handleCancelEstablishLink, socketServer->handleLinkP2P);


			WBasicCancelOperation(socketServer->handleCancelEstablishLink);
			WBasicCloseHandle(socketServer->handleCancelEstablishLink);
			WBasicCloseHandle(socketServer->handleLinkP2P);


			continue;
		}
	}
	while (socketServer->status == SERVER_SOCKET_ERROR);

	//Create new LlcpSocket object
	if (nfc_jni_cache_object(e, "com/android/nfc/nxp/NativeLlcpSocket", &(clientSocket)) == -1)
	{
		LogError("LLCP Socket creation error");
		return NULL;
	}
	//Get NativeConnectionOriented class object
	clsNativeLlcpSocket = e->GetObjectClass(clientSocket);
	if (e->ExceptionCheck())
	{
		LogError("LLCP Socket get class object error");
		return NULL;
	}

	uint32_t localMIU, localRW;
	W_ERROR error = WP2PGetSocketParameter(socketServer->handleServerSocket, W_P2P_LOCAL_MIU, &localMIU);
	if (error != W_SUCCESS)
	{
		LogWarning("Can't get local MIU (error=0x%X)", error);
	}
	else
	{
		LogInformation("localMIU = %d", localMIU);
	}

	error = WP2PGetSocketParameter(socketServer->handleServerSocket, W_P2P_LOCAL_RW, &localRW);
	if (error != W_SUCCESS)
	{
		LogWarning("Can't get local RW (error=0x%X)", error);
	}
	else
	{
		LogInformation("localRW = %d", localRW);
	}

	// Set socket handle
	f = e->GetFieldID(clsNativeLlcpSocket, "mHandle", "I");
	e->SetIntField(clientSocket, f, (jint) handle);

	// Set socket MIU
	f = e->GetFieldID(clsNativeLlcpSocket, "mLocalMiu", "I");
	e->SetIntField(clientSocket, f, (jint) localMIU);

	// Set socket RW
	f = e->GetFieldID(clsNativeLlcpSocket, "mLocalRw", "I");
	e->SetIntField(clientSocket, f, (jint) localRW);

	LogInformation("socket handle = 0x%02x: MIU = %d, RW = %d, mLocalLinearBufferLength=%d\n",
			handle, localMIU, localRW, linearBufferLength);
	return clientSocket;
}

static jboolean com_NativeLlcpServiceSocket_doClose(JNIEnv *e, jobject o)
{
	//Nothing to do here, because already manage by linkReleaseListener

	uint32_t handle = nfc_jni_get_nfc_socket_handle(e, o);
	SocketServer * socketServer = & (socketServers[GET_SOCKET_INDEX(handle)]);

	LogInformation("com_NativeLlcpServiceSocket_doClose: handle=0x%X", socketServer->handleServerSocket);
	WBasicCloseHandle(socketServer->handleServerSocket);
	return JNI_TRUE;
}

/*
 * JNI registration.
 */
static JNINativeMethod gMethods[] =
{
{ "doAccept", "(III)Lcom/android/nfc/nxp/NativeLlcpSocket;", (void *) com_NativeLlcpServiceSocket_doAccept },

{ "doClose", "()Z", (void *) com_NativeLlcpServiceSocket_doClose }, };

int register_com_android_nfc_NativeLlcpServiceSocket(JNIEnv *e)
{
	return jniRegisterNativeMethods(e, "com/android/nfc/nxp/NativeLlcpServiceSocket", gMethods, NELEM(gMethods));
}

} // namespace android
