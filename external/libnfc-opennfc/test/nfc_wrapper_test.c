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

#include <android/log.h>
#include <phLibNfc.h>
#include <unistd.h>
#include <pthread.h>

#include <sys/socket.h>
#include <sys/un.h>
#include <string.h>
#include "open_nfc_extention.h"

/*
#include <dirent.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <fcntl.h>


#include <hardware/hardware.h>
#include <hardware/nfcc.h>
#include "open_nfc.h"
#include "porting_startup.h"
#include "ccclient.h"
#include "linux_porting_hal.h"

#include <arpa/inet.h>
#include <sys/time.h>
*/

/**
 * ----------------------------------------------------
 * --- Open NFC under JNI wrapper test utility ---
 * ----------------------------------------------------
 */

/*CONSTANTS --------------------------- start */

/* polling interval for phLibNfc_RemoteDev_CheckPresence() in sec. */
#define TAG_CHECK_PRESENCE_POLLING 5

/* size of the buffer to read NDEF message */
#define NDEF_READ_BUFFER_SIZE (4 * 1024)

/*CONSTANTS --------------------------- end */



/**TAG used in logs*/
static const char TAG[] = "OPEN_NFC_WRAPPER_TEST";

/**Log for debugging message*/
#define LogDebug(format, ...)		__android_log_print(ANDROID_LOG_DEBUG,	TAG, format, ##__VA_ARGS__)
/**Log for warning message*/
#define LogWarning(format, ...)		__android_log_print(ANDROID_LOG_WARN,	TAG, format, ##__VA_ARGS__)
/**Log for information message*/
#define LogInformation(format, ...)	__android_log_print(ANDROID_LOG_INFO,	TAG, format, ##__VA_ARGS__)
/**Log for error message*/
#define LogError(format, ...)		__android_log_print(ANDROID_LOG_ERROR,	TAG, format, ##__VA_ARGS__)


//static void nfc_jni_init_callback(void *pContext, NFCSTATUS status);

void *gHWRef;

void  updateRegistration(phLibNfc_NtfRegister_RspCb_t pNotificationHandler,	void* pContext);
void  saveRegistration(phLibNfc_NtfRegister_RspCb_t* pfNotificationHandler,	void** pContext);
char * dumpUnit8Array(uint8_t * array, int length);

/* saved registration */
phLibNfc_NtfRegister_RspCb_t savedNotificationHandler = NULL;
void* pSavedContext;
static pthread_t nfcTestThreadId;
static int appId;

/**
 * NFC initialization callback
 */
static void wrapper_init_callback(void *pContext, NFCSTATUS status)
{
	LogInformation("wrapper_init_callback : ENTER");
	LogInformation("wrapper_init_callback : status=0x%X", status);
}

/**
 * NFC deinitialization callback
 */
static void wrapper_deinit_callback(void *pContext, NFCSTATUS status)
{
	LogInformation("wrapper_deinit_callback : ENTER");
	LogInformation("wrapper_deinit_callback : status=0x%X", status);
}


/**
 * NFC initialization
 */
void initialization()
{
//	struct nfc_jni_callback_data cb_data;
	NFCSTATUS status;

	LogInformation("initialization : ENTER");
	status = phLibNfc_Mgt_Initialize(gHWRef, wrapper_init_callback,
			(void *) NULL);
	LogInformation("initialization : EXIT: status = 0x%X", status);
}

/**
 * NFC initialization
 */
void deinitialization()
{
	NFCSTATUS status;

	LogInformation("Deinitialization : ENTER");
	status = phLibNfc_Mgt_DeInitialize(gHWRef, wrapper_deinit_callback,
			(void *) NULL);
	LogInformation("Deinitialization : EXIT: status = 0x%X", status);
}

/* Callback function called on completion  of the presence check */
static void checkPresenceCallback (void* pContext, NFCSTATUS status)
{
	LogInformation("checkPresenceCallback : status = 0x%X", status);
}

/* Response callback for disconnect request */
static void disconnectCallback (void* pContext, phLibNfc_Handle hRemoteDev, NFCSTATUS status)
{
	LogInformation("disconnectCallback : hRemoteDev=%d, status = 0x%X", hRemoteDev, status);
}

/* Response callback for check NDEF */
static void checkNdefCallback (void* pContext, phLibNfc_ChkNdef_Info_t ndefInfo, NFCSTATUS status)
{
	LogInformation("checkNdefCallback : status = 0x%X", status);
	LogInformation("checkNdefCallback : NdefCardState=%d, ActualNdefMsgLength = %d, MaxNdefMsgLength = %d",
			ndefInfo.NdefCardState, ndefInfo.ActualNdefMsgLength, ndefInfo.MaxNdefMsgLength);
}

/* Response callback for read NDEF */
static void ndefReadCallback (void* pContext, NFCSTATUS status)
{
	phNfc_sData_t* pNdefReadData = (phNfc_sData_t*) pContext;
	LogInformation("ndefReadCallback : status = 0x%X, length = %d", status, pNdefReadData->length);

	char* dump = dumpUnit8Array(pNdefReadData->buffer, pNdefReadData->length);
	LogInformation("ndefReadCallback : dump=%s",(char *)dump);
	free(dump);

/*	 send notification to the application */
	/* allocMessage() should be used to create the notification, cause it'll be automatically freed after sending */
	tMessageOpenNFC* pMsg = allocMessage(pNdefReadData->length);
	memcpy(pMsg->pData, pNdefReadData->buffer, pNdefReadData->length);
	pMsg->appId = appId;
	pMsg->clientId = OPEN_NFC_TEST;

	sendOpenNfcExtNotification(pMsg);

	free(pNdefReadData->buffer);
	free(pNdefReadData);
}

/* The function to start phLibNfc_Ndef_Read() */
static bool_t checkNdefRead(phLibNfc_Handle hRemoteDevice)
{
	phNfc_sData_t* pNdefReadData = (phNfc_sData_t*) malloc(sizeof(phNfc_sData_t));
	bool_t status = FALSE;
	if (pNdefReadData != NULL)
	{
		pNdefReadData->length = NDEF_READ_BUFFER_SIZE;
		pNdefReadData->buffer = malloc(NDEF_READ_BUFFER_SIZE);
		if (pNdefReadData->buffer != NULL)
		{
			phLibNfc_Ndef_Read(hRemoteDevice, pNdefReadData, phLibNfc_Ndef_EBegin, ndefReadCallback, pNdefReadData);
			status = TRUE;
		}
	}
	if (status == FALSE) {
		LogError("Error: malloc() failed");
		if (pNdefReadData != NULL)
		{
			free(pNdefReadData);
		}
	}
	return status;
}

/*
 * The thread to check the tag presence: to disconnect the tag when it's not available anymore
 */
static void* checkPresenceThread(void* args)
{
	NFCSTATUS status = NFCSTATUS_PENDING;
	phLibNfc_Handle hTargetDev = (phLibNfc_Handle) args;

	LogInformation("checkPresenceThread : ENTER");

	/* check ndef */
	phLibNfc_Ndef_CheckNdef(hTargetDev, checkNdefCallback, NULL);

	/* read NDEF */
	checkNdefRead(hTargetDev);

	while (status != NFCSTATUS_TARGET_NOT_CONNECTED)
	{
		status = phLibNfc_RemoteDev_CheckPresence(hTargetDev, checkPresenceCallback, NULL);
		sleep(TAG_CHECK_PRESENCE_POLLING);
	}
	/* disconnect the tag */
	phLibNfc_RemoteDev_Disconnect(hTargetDev, NFC_DISCOVERY_RESTART, disconnectCallback, NULL);

	LogInformation("checkPresenceThread : EXIT");
	return NULL;
}

static void discoveryNotificationHandler(
    void*                           pContext,
    phLibNfc_RemoteDevList_t*       psRemoteDevList,
    uint8_t                         uNofRemoteDev,
    NFCSTATUS                       Status)
{
	int presenceThreadId;
	int i;

	LogInformation("discoveryNotificationHandler : ENTER");
	LogInformation("discoveryNotificationHandler : uNofRemoteDev=%d", uNofRemoteDev);
	for(i=0; i<uNofRemoteDev; i++)
	{
		LogInformation("discoveryNotificationHandler : device[%d].hTargetDev=%d", i, psRemoteDevList[i].hTargetDev);
	}
	if (uNofRemoteDev > 0)
	{
	/* start thread to check the tag presence */
		if (pthread_create(&presenceThreadId, NULL, checkPresenceThread,
				(void*) psRemoteDevList[0].hTargetDev) != 0)
		{
			LogWarning("Can't launch thread for checkPresenceThread");
		}
	}
	LogInformation("discoveryNotificationHandler : EXIT");
}


/**
 * Launch the test framework
 * @param	nArgc	Number of arguments
 * @param	pArgv	Arguments
 */
void* nfc_test_thread(void* args)
{
	int resultStart;

	LogInformation("nfc_test_thread: savedNotificationHandler=%d",
			savedNotificationHandler);

	/* while needs only until we get the real test framework:
	 * just to be sure that NFC is enabled and initial callback registration is done */
	while (savedNotificationHandler == NULL)
	{
		sleep(1);
		/* save registration info */
		saveRegistration(&savedNotificationHandler, &pSavedContext);
	}

	LogInformation("nfc_test_thread: TEST READY - SHOW ME THE TAG !\n savedNotificationHandler=%d",
			savedNotificationHandler);

	/* set new handler for tests */
	updateRegistration(discoveryNotificationHandler, NULL);


	sleep(300);

	/* restore registration info */
	updateRegistration(savedNotificationHandler, pSavedContext);

	LogInformation("\n*****************************\n\t Finished Open NFC Wrapper Test  !\n");

/*
	if(nArgc<3)
	{
		LogError("Not enough parameter !");
		LogError("Usage : %s <TARGET> <CONFIGURATION>", pArgv[0]);
		LogError("     <TARGET>        : Target to reach, use M for Microread and S for Simulator");
		LogError("     <CONFIGURATION> : Configuration (IP)");

		return -1;
	}
*/

	return 0;
}

/*	callback function to be called when the client's message received */
static void openNfcExtCallback (tMessageOpenNFC* pMessage)
{
	LogInformation("openNfcExtCallback: ENTER");

	appId = pMessage->appId;

	char* dump = dumpUnit8Array(pMessage->pData,  pMessage->length);
	LogInformation("openNfcExtCallback: appId = %d, Message: length = %d, data=%s",
			pMessage->appId, pMessage->length, dump);
	free (dump);

	if (pthread_create(&nfcTestThreadId, NULL, nfc_test_thread, NULL) != 0)
	{
		LogWarning("Can't launch thread for nfc_test_thread");
	}
}

void launchNFCWrapperTest()
{
	LogInformation("launchNFCWrapperTest: ENTER");
	registerOpenNfcExtCallback(OPEN_NFC_TEST, openNfcExtCallback);
}
/* EOF */
