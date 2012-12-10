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
#include <unistd.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <string.h>
#include "open_nfc_extention.h"

/**
 * ----------------------------------------------------
 * --- Open NFC under JNI wrapper test launcher ---
 * ----------------------------------------------------
 */

/**TAG used in logs*/
static const char TAG[] = "OPEN_NFC_WRAPPER_TEST_LAUNCHER";

/**Log for debugging message*/
#define LogDebug(format, ...)		__android_log_print(ANDROID_LOG_DEBUG,	TAG, format, ##__VA_ARGS__)
/**Log for warning message*/
#define LogWarning(format, ...)		__android_log_print(ANDROID_LOG_WARN,	TAG, format, ##__VA_ARGS__)
/**Log for information message*/
#define LogInformation(format, ...)	__android_log_print(ANDROID_LOG_INFO,	TAG, format, ##__VA_ARGS__)
/**Log for error message*/
#define LogError(format, ...)		__android_log_print(ANDROID_LOG_ERROR,	TAG, format, ##__VA_ARGS__)

/**
 * *********************************************
 * *** HELPERS for conversion NXP<->Open NFC ***
 * *********************************************
 */

static const char HEXA_CHAR[16] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

static char * dumpUnit8Array(uint8_t * array, int length)
{
            char * dump = (char*)malloc(length*3 + 1);

            int i;
            char * write = dump;
            uint8_t * read = array;
            uint8_t element;

            for(i=0;i<length;i++)
            {
                        element = *(read);
                        read ++;

                        *(write) = HEXA_CHAR[(element>>4) & 0xF];
                        write++;

                        *(write) = HEXA_CHAR[element & 0xF];
                        write++;

                        *(write) = ' ';
                        write++;
            }

            *(write) = 0;

            return dump;
}

/*	callback function to be called when the notification is received */
static void openNfcExtNotifCallback (tMessageOpenNFC* pMessage)
{
	LogInformation("openNfcExtNotifCallback: ENTER");

	char* dump = dumpUnit8Array(pMessage->pData,  pMessage->length);
	LogInformation("openNfcExtNotifCallback: Message: length = %d, data=%s",
			pMessage->length, dump);
	free (dump);
	LogInformation("openNfcExtNotifCallback: EXIT");
}

/**
 * Launch the test framework
 * @param	nArgc	Number of arguments
 * @param	pArgv	Arguments give
 */
int main(int nArgc, char* pArgv[])
{
	int resultStart;
	bool_t status;
	uint8_t data[] = {0x01, 0x02};
	tMessageOpenNFC msg;

	msg.length = 2;
	msg.pData = &data;
	msg.clientId = OPEN_NFC_TEST;
	LogInformation("\n*****************************\n\t Starting Open NFC Wrapper Test Launcher !\n");

	registerOpenNfcExtNotificationCallback(OPEN_NFC_TEST, openNfcExtNotifCallback);

	status = openOpenNfcExtConnection();
	LogInformation("called openOpenNfcExtConnection(): status = %d", status);
	sleep(3);

	char* dump = dumpUnit8Array(msg.pData,  msg.length);
	LogInformation( "Message: length = %d, data=%s", msg.length, dump);
	free (dump);

	status = sendOpenNfcExtMessage(&msg);
	LogInformation("called sendOpenNfcExtMessage(): status = %d", status);

	sleep(300);
	status = closeOpenNfcExtConnection();
	LogInformation("called closeOpenNfcExtConnection(): status = %d", status);

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

/* EOF */
