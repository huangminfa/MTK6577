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

#ifndef QUEUE
#define QUEUE

#include <semaphore.h>
#include <pthread.h>
#include <open_nfc.h>

typedef struct QueueElement {
	void* pMsg;
	struct QueueElement* pNext;
} tQueueElement;

typedef struct Queue {
	tQueueElement* pHead;
	tQueueElement* pTail;
	int count;
	pthread_mutex_t	mutex;
	sem_t sem;
} tQueue;

void initQueue(tQueue* queue);
bool_t isQueueEmpty(tQueue* queue);
void* getFromQueue(tQueue* queue);
bool_t putToQueue(tQueue* queue, void* pMsg);
void* checkQueueElement(tQueue* queue);

#endif
