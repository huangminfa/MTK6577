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
#include "queue.h"
#include <sys/queue.h>

/* tag for log */
#define TAG "OpenNFCExtCommon"

tMessageOpenNFC* allocMessage(tMessageOpenNFCHeader* pHeader)
{
	tMessageOpenNFC* pMsg = malloc(sizeof(tMessageOpenNFC));
	if (pMsg == NULL)
	{
		goto err_label;
	}
	if (pHeader->length != 0)
	{
		pMsg->pData = malloc(pHeader->length);
		if (pMsg->pData == NULL)
		{
			goto err_label;
		}
	}
	else
	{
		pMsg->pData = NULL;
	}
	memcpy(&pMsg->header, pHeader, sizeof(tMessageOpenNFCHeader));
	return pMsg;

err_label:
	if (pMsg != NULL)
	{
		free(pMsg);
	}
	LogError("allocMessage: malloc() error");
	return NULL;
}

void freeMessage(tMessageOpenNFC* pMsg)
{
//	LogInformation("freeMessage(): pMsg=");
	if (pMsg != NULL)
	{
		free(pMsg->pData);
		free(pMsg);
	}
}

