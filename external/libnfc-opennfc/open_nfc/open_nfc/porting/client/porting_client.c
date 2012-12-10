/*
 * Copyright (c) 2007-2010 Inside Secure, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*******************************************************************************

  Implementation of the Android client porting

*******************************************************************************/
#define P_MODULE  P_MODULE_DEC( CLIENT )

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <errno.h>
#include "porting_os.h"
#include "open_nfc.h"
#include "porting_client.h"

#include <android/log.h>
static const char TAG[] = "porting_client";

#if 0
#define LogDebug(format, ...)              __android_log_print(ANDROID_LOG_DEBUG,  TAG, format, ##__VA_ARGS__)
#define LogWarning(format, ...)                        __android_log_print(ANDROID_LOG_WARN,    TAG, format, ##__VA_ARGS__)
#define LogInformation(format, ...)       __android_log_print(ANDROID_LOG_INFO,      TAG, format, ##__VA_ARGS__)
#define LogError(format, ...)                 __android_log_print(ANDROID_LOG_ERROR,  TAG, format, ##__VA_ARGS__)
#else
#define LogDebug(format, ...)
#define LogWarning(format, ...)
#define LogInformation(format, ...)
#define LogError(format, ...)
#endif

typedef struct __tClientInstance
{
   /* socket used for communication with the server side */
   int nConnectionSocket;

   /* socket used to force call to WBasicPumpEvent() */
   int nWBasicPumpEventSocket;

   /* boolean used to request termination of the event receive loop */
   bool_t bRequestStop;

   /* mutex used to avoid reentrancy of CUserCallFunction */
   pthread_mutex_t Mutex;

	/* socket pair used to wake up thread */
	int aSockets[2];

} tClientInstance;

/* function call request descriptor */
typedef struct __tDFCCLientServerMessageHeader
{
   /* input parameter size */
   uint32_t nSizeIn;

   /* output parameter size */
   uint32_t nSizeOut;

   /* function identifier */
   uint8_t nCode;

} tDFCCLientServerMessageHeader;

/* client buffer synchronization request descriptor */
typedef struct __tDFCCLientServerSyncOutputData
{
   /* address of the client buffer to be synchronized */
   void* pClientBuffer;

   /* length of the client buffer */
   uint32_t nClientBufferLength;

} tDFCCLientServerSyncOutputData;

/* See porting guide */
void* CUserOpen(void)
{
   struct sockaddr_un clientService;
   tClientInstance * pInstance;

	char data[32], control[32];
	struct msghdr   msg;
	struct cmsghdr  *cmsg;
	struct iovec    iov;
   struct ucred *  credptr;

   pInstance = CMemoryAlloc(sizeof(tClientInstance));

   if (pInstance == null)
   {
      LogError("CUserOpen : Cannot create the user context");
      return null;
   }

   pInstance->nConnectionSocket = -1;
   pInstance->nWBasicPumpEventSocket = -1;
   pInstance->bRequestStop = W_FALSE;

	if (socketpair(AF_UNIX, SOCK_STREAM, 0, pInstance->aSockets))
	{
		LogError("CUserOpen : socketpair() failed");
		goto return_error;
	}

   pInstance->nConnectionSocket = socket(AF_UNIX, SOCK_STREAM, 0);

   if (pInstance->nConnectionSocket == -1)
   {
      LogError("CUserOpen: Cannot create the socket object");
      goto return_error;
   }

	memset(&clientService, 0, sizeof(clientService));

   clientService.sun_family = AF_UNIX;
   strcpy(& clientService.sun_path[1], "opennfc");

   if (connect( pInstance->nConnectionSocket, (struct sockaddr *) &clientService, sizeof(clientService)))
   {
      LogError("CUserOpen: Cannot connect to the server");
      goto return_error;
   }
   
   /* use SCM_CREDENTIALS to send client credential to the server */
   
   memset(&msg, 0, sizeof(msg));

   iov.iov_base = "OPENNFC";
   iov.iov_len  = 8;
   msg.msg_iov = &iov;
   msg.msg_iovlen = 1;
   msg.msg_control = control;
   msg.msg_controllen = sizeof(control);

   cmsg = CMSG_FIRSTHDR(&msg);
   cmsg->cmsg_level = SOL_SOCKET;
   cmsg->cmsg_type = SCM_CREDENTIALS;
   cmsg->cmsg_len = CMSG_LEN(sizeof(struct ucred));
   credptr = (struct ucred *) CMSG_DATA(cmsg);    
   credptr->uid = geteuid();
   credptr->gid = getegid();
   credptr->pid = getpid();

   msg.msg_controllen = cmsg->cmsg_len;

   if (sendmsg(pInstance->nConnectionSocket, &msg, 0) < 0)
   {

      LogError("CUserOpen: sendmsg() failed");
      goto return_error;
   }

   /* get the event socket created by the server */

	memset(&msg, 0, sizeof(msg));
	iov.iov_base   = data;
	iov.iov_len    = sizeof(data)-1;
	msg.msg_iov    = &iov;
	msg.msg_iovlen = 1;
	msg.msg_control = control;
	msg.msg_controllen = sizeof(control);

	if (recvmsg(pInstance->nConnectionSocket, &msg, 0) < 0)
	{
      LogError("CUserOpen: Cannot receive event fd");
		goto return_error;
	}

	if (strcmp(data, "OPENNFC"))
	{
      LogError("CUserOpen : unexpected message format %s", data);
		goto return_error;
	}

	/* Loop over all control messages */
	cmsg = CMSG_FIRSTHDR(&msg);

	while (cmsg != NULL)
	{
		if ((cmsg->cmsg_level == SOL_SOCKET)  && (cmsg->cmsg_type  == SCM_RIGHTS))
		{
			int * fdptr = (int *) CMSG_DATA(cmsg);

			memcpy(&pInstance->nWBasicPumpEventSocket, fdptr, sizeof(int));

         PDebugTrace("CUserOpen: nWBasicPumpEventSocket %d", pInstance->nWBasicPumpEventSocket);
			break;
		}
	}

   if (pthread_mutex_init(&pInstance->Mutex, NULL))
   {
      LogError("CUserOpen: unable to initialize the mutex");
      goto return_error;
   }

   return (void*)pInstance;

return_error:

	close(pInstance->aSockets[0]);
	close(pInstance->aSockets[1]);

	if (pInstance->nWBasicPumpEventSocket != -1)
	{
		shutdown(pInstance->nWBasicPumpEventSocket, SHUT_RDWR);
		close(pInstance->nWBasicPumpEventSocket);
		pInstance->nWBasicPumpEventSocket = -1;
	}

	if(pInstance->nConnectionSocket != -1)
	{
		shutdown(pInstance->nConnectionSocket, SHUT_RDWR);
		close(pInstance->nConnectionSocket);
		pInstance->nConnectionSocket = -1;
	}

	CMemoryFree(pInstance);
   return null;
}

/* See porting guide */
void CUserClose(
            void* pUserInstance)
{
   tClientInstance * pInstance = pUserInstance;

   if(pInstance != null)
   {

		close(pInstance->aSockets[0]);
		close(pInstance->aSockets[1]);

		pthread_mutex_destroy(&pInstance->Mutex);

		/* close the event socket */
      if (pInstance->nWBasicPumpEventSocket != -1)
      {
			shutdown(pInstance->nConnectionSocket, SHUT_RDWR);
			close(pInstance->nConnectionSocket);
      }

      if(pInstance->nConnectionSocket != -1)
      {
         /* terminate the connection */
         shutdown(pInstance->nConnectionSocket, SHUT_RDWR);
         close(pInstance->nConnectionSocket);
      }
   }
}

/* See porting guide */
W_ERROR PUserRead(
            void* pUserInstance,
            void* pBuffer,
            uint32_t nBufferLength)
{
   int socket = ((tClientInstance *)pUserInstance)->nConnectionSocket;

   int32_t nOffset = 0;
   int32_t nLength;

   while(nBufferLength != 0)
   {
      nLength = recv(socket, (char *)pBuffer + nOffset, nBufferLength, MSG_NOSIGNAL);

      if(nLength < 0)
      {
         LogError("PUserRead : recv() failed  %d", errno);
         return W_ERROR_CLIENT_SERVER_COMMUNICATION;
      }
      else if(nLength == 0)
      {
         /* Socket gracefully shutdown */
         LogError("PUserRead : recv() : connection has been shut down\n");
         return W_ERROR_CLIENT_SERVER_COMMUNICATION;
      }

      nOffset += nLength;
      nBufferLength -= nLength;
   }

   return W_SUCCESS;
}

/* See porting guide */
W_ERROR PUserWrite(
            void* pUserInstance,
            const void* pBuffer,
            uint32_t nBufferLength)
{
   int socket = ((tClientInstance *)pUserInstance)->nConnectionSocket;

   int32_t nLength;
   int32_t nOffset = 0;

   while(nBufferLength != 0)
   {
      nLength = send(socket, (char *) pBuffer + nOffset, nBufferLength, MSG_NOSIGNAL);

      if(nLength < 0)
      {
         LogError("PUserWrite : send() failed %d", errno);
         return W_ERROR_CLIENT_SERVER_COMMUNICATION;
      }
      nOffset += nLength;
      nBufferLength -= nLength;
   }

   return W_SUCCESS;
}

/* See porting guide */
W_ERROR CUserCallFunction(
            void * pUserInstance,
            uint8_t nCode,
            void* pParamInOut,
            uint32_t nSizeIn,
            const void* pBuffer1,
            uint32_t nBuffer1Length,
            const void* pBuffer2,
            uint32_t nBuffer2Length,
            uint32_t nSizeOut)
{LogError("porting_client.c : CUserCallFunction : 336");
   tClientInstance * pInstance = (tClientInstance *) pUserInstance;LogError("porting_client.c : CUserCallFunction : 337");
   tDFCCLientServerMessageHeader header;
   W_ERROR nError;

   /* avoid reentrancy to ensure commands are not interleaved */

   if (pthread_mutex_lock(&pInstance->Mutex))
   {LogError("porting_client.c : CUserCallFunction : 344");
      LogError("CUserCallFunction : pthread_mutex_lock failed");
      return W_ERROR_CLIENT_SERVER_COMMUNICATION;
   }LogError("porting_client.c : CUserCallFunction : 347");

   header.nCode = nCode;
   header.nSizeIn = nSizeIn;
   header.nSizeOut = nSizeOut;LogError("porting_client.c : CUserCallFunction : 351");

   nError = PUserWrite(pInstance, &header, sizeof(tDFCCLientServerMessageHeader));LogError("porting_client.c : CUserCallFunction : 353");
   if(nError != W_SUCCESS)
   {LogError("porting_client.c : CUserCallFunction : 355");
      LogError("CUserCallFunction: Error %d returned by PUserWrite()", nError);
      return nError;
   }LogError("porting_client.c : CUserCallFunction : 358");

   if(nSizeIn != 0)
   {LogError("porting_client.c : CUserCallFunction : 361");
      nError = PUserWrite(pInstance, pParamInOut, nSizeIn);LogError("porting_client.c : CUserCallFunction : 362");
      if(nError != W_SUCCESS)
      {LogError("porting_client.c : CUserCallFunction : 364");
         LogError("CUserCallFunction: Error %d returned by PUserWrite()", nError);
         return nError;
      }LogError("porting_client.c : CUserCallFunction : 367");
   }LogError("porting_client.c : CUserCallFunction : 368");

   if(nBuffer1Length != 0)
   {LogError("porting_client.c : CUserCallFunction : 371");
      nError = PUserWrite(pInstance, pBuffer1, nBuffer1Length);LogError("porting_client.c : CUserCallFunction : 372");
      if(nError != W_SUCCESS)
      {LogError("porting_client.c : CUserCallFunction : 374");
         LogError("CUserCallFunction: Error %d returned by PUserWrite()", nError);
         return nError;
      }LogError("porting_client.c : CUserCallFunction : 377");
   }LogError("porting_client.c : CUserCallFunction : 378");

   if(nBuffer2Length != 0)
   {LogError("porting_client.c : CUserCallFunction : 381");
      nError = PUserWrite(pInstance, pBuffer2, nBuffer2Length);LogError("porting_client.c : CUserCallFunction : 382");
      if(nError != W_SUCCESS)
      {LogError("porting_client.c : CUserCallFunction : 384");
         LogError("CUserCallFunction: Error %d returned by PUserWrite()", nError);
         return nError;
      }LogError("porting_client.c : CUserCallFunction : 387");
   }LogError("porting_client.c : CUserCallFunction : 388");
    /* Retreive every buffer synchronization until the function result is received */
    for(;;)
    {LogError("porting_client.c : CUserCallFunction : 391");
       uint8_t temp;
       nError = PUserRead(pInstance, &temp, 1);LogError("porting_client.c : CUserCallFunction : 393");
       if(nError != W_SUCCESS)
       {LogError("porting_client.c : CUserCallFunction : 395");
          LogError("CUserCallFunction: Error %d returned by PUserRead()", nError);
          break;
       }LogError("porting_client.c : CUserCallFunction : 398");

       if(temp == nCode)
       {LogError("porting_client.c : CUserCallFunction : 401");
         /* the code received is identical to the function identifier :
            we're receiving the answer */

         if(nSizeOut != 0)
         {LogError("porting_client.c : CUserCallFunction : 406");
            /* process the answer of the function */
            nError = PUserRead(pInstance, pParamInOut, nSizeOut);LogError("porting_client.c : CUserCallFunction : 408");
            if(nError != W_SUCCESS)
            {LogError("porting_client.c : CUserCallFunction : 410");
               LogError("CUserCallFunction: Error %d returned by PUserRead()", nError);
            }LogError("porting_client.c : CUserCallFunction : 412");
         }LogError("porting_client.c : CUserCallFunction : 413");

         /* the answer has been received, function call is now complete */
         break;
       }
       else if (temp == (uint8_t) P_Identifier_PSyncOutputData)
       {LogError("porting_client.c : CUserCallFunction : 419");
          /* process output data synchronization request */

          tDFCCLientServerSyncOutputData sSyncOutpuData;

          nError = PUserRead(pInstance, &sSyncOutpuData, sizeof(tDFCCLientServerSyncOutputData));LogError("porting_client.c : CUserCallFunction : 424");
          if(nError != W_SUCCESS)
          {LogError("porting_client.c : CUserCallFunction : 426");
             LogError("CUserCallFunction: Error %d returned by PUserRead()", nError);
             return nError;
          }LogError("porting_client.c : CUserCallFunction : 429");
          if((sSyncOutpuData.pClientBuffer == null) || (sSyncOutpuData.nClientBufferLength == 0))
          {LogError("porting_client.c : CUserCallFunction : 431");
             LogError("CUserCallFunction: Error wrong buffer message format from server");
             nError = W_ERROR_CLIENT_SERVER_PROTOCOL;
             break;
          }LogError("porting_client.c : CUserCallFunction : 435");
          nError = PUserRead(pInstance, sSyncOutpuData.pClientBuffer, sSyncOutpuData.nClientBufferLength);LogError("porting_client.c : CUserCallFunction : 436");
          if (nError != W_SUCCESS)
          {LogError("porting_client.c : CUserCallFunction : 438");
             LogError("CUserCallFunction: Error %d returned by PUserRead()", nError);
             return nError;
          }LogError("porting_client.c : CUserCallFunction : 441");
       }
       else
       {LogError("porting_client.c : CUserCallFunction : 444");
          LogError("CUserCallFunction: Error wrong message format from server");
          nError = W_ERROR_CLIENT_SERVER_PROTOCOL;
          break;
       }LogError("porting_client.c : CUserCallFunction : 448");
    }LogError("porting_client.c : CUserCallFunction : 449");

   if (pthread_mutex_unlock(&pInstance->Mutex))
   {LogError("porting_client.c : CUserCallFunction : 452");
      LogError("CUserCallFunction : pthread_mutex_unlock failed");
      return W_ERROR_CLIENT_SERVER_COMMUNICATION;
   }LogError("porting_client.c : CUserCallFunction : 455");

   return nError;
}

/* See porting guide */
void CUserExecuteEventLoop(
   void * pUserInstance)
{
	LogError("CUserExecuteEventLoop : START");
   tClientInstance * pInstance = (tClientInstance *) pUserInstance;
   int ret;
	char buffer[1];

   for (;;)
   {
		LogError("CUserExecuteEventLoop : LOOP");

		fd_set readfds;
		int nfds;

		LogError("CUserExecuteEventLoop : FD ZERO");
		FD_ZERO(&readfds);

		FD_SET(pInstance->nWBasicPumpEventSocket, &readfds);	LogError("CUserExecuteEventLoop set socket");
		FD_SET(pInstance->aSockets[1], &readfds);LogError("CUserExecuteEventLoop : set socket 2");

		if (pInstance->nWBasicPumpEventSocket > pInstance->aSockets[1])
		{LogError("CUserExecuteEventLoop : asocket[1] is true");
			nfds = pInstance->nWBasicPumpEventSocket + 1;
		}
		else
		{LogError("CUserExecuteEventLoop : asocket[1] is false");
			nfds = pInstance->aSockets[1] + 1;
		}
		LogError("CUserExecuteEventLoop : will select");
		ret = select(nfds, &readfds, NULL, NULL, NULL);LogError("CUserExecuteEventLoop : selected %d",ret);

		if (ret < 0)
		{
			LogError("CUserExecuteEventLoop : select failed %d", errno);
         return;
		}
		LogError("CUserExecuteEventLoop : test if need stop");
      if (pInstance->bRequestStop == W_TRUE)
	   {
         PDebugTrace("shutdown of the event loop has been requested");
	      return;
	   }
      LogError("CUserExecuteEventLoop : isset valid ?");
		if (FD_ISSET(pInstance->nWBasicPumpEventSocket, & readfds))
		{LogError("CUserExecuteEventLoop : isset valid !");
			ret = recv(pInstance->nWBasicPumpEventSocket,  buffer, 1, 0);LogError("CUserExecuteEventLoop : recv %d",ret);

			if (ret != 1)
			{
				LogError("CUserExecuteEventLoop : recv failed %d", errno);
				return;
			}
			LogError("CUserExecuteEventLoop : PUMP event !");
			PDebugTrace("calling WBasicPumpEvent");
			WBasicPumpEvent(W_FALSE);
		}
   }

	LogError("CUserExecuteEventLoop : END");
}

/* See porting guide */
void CUserStopEventLoop(
   void * pUserInstance)
{
   tClientInstance * pInstance = (tClientInstance *) pUserInstance;
	ssize_t nResult;

   pInstance->bRequestStop = W_TRUE;

	nResult = write(pInstance->aSockets[0], "X", 1);
	if (nResult <= 0)
	{
		LogError("%s: write error %d", __FUNCTION__, nResult);
	}
}

/* see porting guide*/
bool_t CUserWaitForServerEvent(void * pUserInstance, bool_t * bWaitInServer)
{
   tClientInstance * pInstance = (tClientInstance *) pUserInstance;
	char buffer[1];

   /* the wait is done on the client side, we must not wait in the server side */
   * bWaitInServer = W_FALSE;

   for (;;)
   {
		fd_set readfds;
		int nfds;
		int ret;

		FD_ZERO(&readfds);

		FD_SET(pInstance->nWBasicPumpEventSocket, &readfds);
		FD_SET(pInstance->aSockets[1], &readfds);

		if (pInstance->nWBasicPumpEventSocket > pInstance->aSockets[1])
		{
			nfds = pInstance->nWBasicPumpEventSocket + 1;
		}
		else
		{
			nfds = pInstance->aSockets[1] + 1;
		}

		ret = select(nfds, &readfds, NULL, NULL, NULL);

		if (ret < 0)
		{
			LogError("CUserWaitForServerEvent : select failed %d", errno);
         return W_TRUE;
      }

      if (pInstance->bRequestStop == W_TRUE)
	   {
         PDebugTrace("shutdown of the event loop has been requested");
	      return W_TRUE;
	   }

		if (FD_ISSET(pInstance->nWBasicPumpEventSocket, & readfds))
		{
			ret = recv(pInstance->nWBasicPumpEventSocket,  buffer, 1, 0);

			if (ret != 1)
			{
				LogError("CUserWaitForServerEvent : recv failed %d", errno);
				return W_TRUE;
			}

   return W_FALSE;

		}
   }   
}

/* EOF */
