/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

#ifndef __MTK_VT_STK_H__
#define __MTK_VT_STK_H__

#include <semaphore.h>
#include <pthread.h>

#ifdef __cplusplus
extern "C" {
#endif

extern struct VTSQueueFunc gVTSQueueFunc;
void vt_swip_start();
void vt_swip_stop();


	#define QUEUE_NAME_SIZE 10

	#define EMPTY_QUEUE 0x01
	#define	FULL_QUEUE	0x02
	#define VIDEO_QUEUE 0x04
	#define AUDIO_QUEUE 0x08

	typedef struct VTSQueueNode{
		uint8_t *data ;
		unsigned int size;
		unsigned int len;
		int isError;
		int64_t timestamp;
		int sequenceNumber;
		struct VTSQueueNode* next;
	}VTSQueueNode, * PtrVTSQueueNode;
	
	typedef struct VTSQueue{
		char * mName;
		VTSQueueNode * mVTSQueueHeader;
		VTSQueueNode   *mVTSQueueTail;
		int mNoteDataSize;
		int mNoteNr;
		pthread_mutex_t mLock;
		sem_t mSem;
		int mIsValidQueue;
	}VTSQueue;

	enum VTSQUEUEIDX{
		DLVF = 0,
		ULVF,
		DLAF,
		ULAF,
		DLVE,
		ULVE,
		DLAE,
		ULAE,
		VTS_QUEUE_NR,
	};

	typedef struct VTSQueueContext {
		int index;
		VTSQueue ** queue;
	}VTSQueueContext;
		
	void _newVTSQueue(VTSQueue** queue, int VTSQueueType, char * name);
	PtrVTSQueueNode _deleteFromFront(int idx);
	int _insertToBack(int idx, VTSQueueNode *node);
	int _setQueueToInvalid(int idx);
	
	typedef struct VTSQueueFunc{
		void (*newVTSQueue)(VTSQueue**, int, char *);
		PtrVTSQueueNode (*deleteFromFront)(/*VTSQueue*,*/ int);
		int (*insertToBack)(/*VTSQueue*,*/ int,VTSQueueNode *);
		void (*deleteVTSQueue)(VTSQueue **);
		int (*setToInvalid)(int);
		int (*isQueueValid)(int);
	}VTSQueueFunc;

	

	void stkInterfaceInit();

	void stkInterfaceDeinit();

	void testTmpV();
	void testTmpA();

	void sendmsgToSVC (int msgId, void ** extData);

#define OPEN_THREAD_SOCKET	mtk_vt_open_thd_socket
#define CLOSE_THREAD_SOCKET mtk_vt_close_thd_socket

/*#define SENDMESSAGE(dstModId, dstThreadId, msgId, extData, socketPath) \
	do{ \
		int ret;	\
		mtk_ilm_struct ilm_msg; \
		ilm_msg.src_mod_id = MOD_NIL;	\
		ilm_msg.dest_mod_id = dstModId; \
		ilm_msg.msg_id = (msg_type)msgId; \
		ilm_msg.sap_id = NULL;	\
		ilm_msg.local_para_ptr = (void**)extData;	\
		ilm_msg.peer_buff_ptr = NULL;	\
		ret = write(THD_MSG_FD(dstThreadId), &ilm_msg, sizeof(mtk_ilm_struct)); \
		while(ret != sizeof(mtk_ilm_struct)){	\
			_E("SENDMESSAGE error!! dstModId [%d], dstThreadId[%d], msgId[%d], errno [%d], THD_MSG_FD(dstThreadId) [%d]", dstModId, dstThreadId, msgId, errno, THD_MSG_FD(dstThreadId));	\
			usleep(2000);	\
			ret = write(THD_MSG_FD(dstThreadId), &ilm_msg, sizeof(mtk_ilm_struct)); \
		}	\
		_D("SENDMESSAGE!! dstModId [%d], dstThreadId[%d], msgId[%d], THD_MSG_FD(dstThreadId) [%d]", dstModId, dstThreadId, msgId, THD_MSG_FD(dstThreadId));	\
	}while(0)
//#define SENDMSGTOSTK(msgId,extData)	SENDMESSAGE(MOD_STK,VT_TASK_STK,msgId,extData)
//#define SENDMSGTOTCV(msgId,extData) SENDMESSAGE(MOD_TCV,VT_TASK_TCV,msgId,extData)
//#define SENDMSGTOSCV(msgId,extData) SENDMESSAGE(MOD_SVC,VT_TASK_SVC,msgId,extData,SOCKET_VT_SVC, 0)
#define SENDMSGTOSCV(msgId,extData) SENDMESSAGE(MOD_SVC,VT_TASK_SVC,msgId,extData,SOCKET_VT_SVC, 0)*/

#ifdef __cplusplus
}
#endif

#endif /* __MTK_VT_STK_H__ */