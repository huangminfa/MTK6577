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


#include "queue.h"
#include "open_nfc_extension.h"

/* tag for log */
#define TAG "OpenNFCExtQueue"

static bool_t checkQueue(tQueue* pQueue)
{
	if (pQueue == NULL)
	{
		LogError("checkQueue: pQueue is NULL");
		return W_FALSE;
	}
	return W_TRUE;
}

void initQueue(tQueue* pQueue)
{
	if(!checkQueue(pQueue))
	{
		return;
	}
	pQueue->pHead = NULL;
	pQueue->pTail = NULL;
	pQueue->count = 0;
	sem_init(&pQueue->sem, 0, 0);
	pthread_mutex_init(&pQueue->mutex, NULL);
}

bool_t isQueueEmpty(tQueue* pQueue)
{
	if(!checkQueue(pQueue))
	{
		return W_TRUE;
	}
	return (pQueue->pHead == NULL) ? W_TRUE : W_FALSE;
}

/* Get an element from the queue: it's a blocking function ! */
void* getFromQueue(tQueue* pQueue)
{
	void* pMsg;
	tQueueElement* pOldHead;

	if(!checkQueue(pQueue))
	{
		return NULL;
	}
	sem_wait(&pQueue->sem);
	pthread_mutex_lock(&pQueue->mutex);
	pMsg = pQueue->pHead->pMsg;
	pOldHead = pQueue->pHead;
	
	if (pOldHead->pNext != NULL) {
		pQueue->pHead = pOldHead->pNext;
	} else {
		pQueue->pHead = NULL;
		pQueue->pTail = NULL;
//		LogWarning("getFromQueue(): queue is empty");
	}

	free(pOldHead);
	pQueue->count--;
	pthread_mutex_unlock(&pQueue->mutex);
	return pMsg;
}

bool_t putToQueue(tQueue* pQueue, void* pMsg)
{
	tQueueElement* newElem;

	if(!checkQueue(pQueue))
	{
		return W_FALSE;
	}
	newElem = (tQueueElement*) malloc(sizeof(tQueueElement));
	if(newElem == NULL)
	{
		LogError("putToQueue: malloc() error");
		return W_FALSE;
	}

	newElem->pMsg = pMsg;
	newElem->pNext = NULL;

	pthread_mutex_lock(&pQueue->mutex);
	if (pQueue->pTail != NULL) {
		pQueue->pTail->pNext = newElem;
	}
	pQueue->pTail = newElem;

	if (pQueue->pHead == NULL) {
		pQueue->pHead = newElem;
	}
	pQueue->count++;
	pthread_mutex_unlock(&pQueue->mutex);
	sem_post(&pQueue->sem);
	return W_TRUE;
}

void* checkQueueElement(tQueue* pQueue)
{
	if(!checkQueue(pQueue))
	{
		return NULL;
	}
	return pQueue->pHead->pMsg;
}
