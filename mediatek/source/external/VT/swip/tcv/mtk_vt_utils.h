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

#ifndef __MTK_VT_UTILS_H__
#define __MTK_VT_UTILS_H__

#ifdef __cplusplus
extern "C" {
#endif

#include <time.h>
#include <sys/times.h>
#include <sys/time.h>
#include <pthread.h>
#include "check_heap.h"
#include "mtk_vt_defs.h"

#define MAX_NUM_OF_TIMER 10
#define DEBUG_PATTERN 0xF3

typedef enum VTSCLIENTSTATUS
{
	VTSCLIENT_NOT_INIT = -1,
	VTSCLIENT_IDLE,
	VTSCLIENT_OPENED,
	VTSCLIENT_INITED,
	VTSCLIENT_STARTED,
	VTSCLIENT_STOPPING,
	VTSCLIENT_ERROR
} VTSCLIENTSTATUS;

typedef struct {
    pthread_mutex_t mtx;
    timer_t timer_hdl;
    struct itimerspec tmr_spec;
} mtk_sys_tmr_sturct;

typedef struct
{
    kal_uint32 r_idx; 
    kal_uint32 w_idx;  
    kal_uint32 max_q_size; 
    kal_uint8 *queue;
	kal_mutexid mtx;
}std_q_struct;

/////////////////////////////////////////////////////////////////////
vt_ret_enum mtk_vt_debug_init();
void mtk_vt_debug_uninit();
void mtk_vt_timer_create (kal_uint8 timer_id, kal_timer_func_ptr timer_expiry, void *arg);

/////////////////////////////////////////////////////////////////////
kal_int32 mtk_vt_stdQInit(
    IN  std_q_struct    *q,
    IN  kal_uint8       *buffer,
    IN  kal_uint32      size);

void mtk_vt_stdQDeinit(IN std_q_struct *q);
kal_uint32 mtk_vt_stdQReadData(
    IN  std_q_struct    *q,
    IN  kal_uint8       *buffer,
    IN  kal_uint32      size);

kal_uint32 mtk_vt_stdQWriteData(
    IN      std_q_struct    *q,
    IN      kal_uint8       *data,
    IN      kal_uint32      size);

kal_uint32 mtk_vt_stdQGetUsedSize(IN std_q_struct *q);
kal_uint32 mtk_vt_stdQGetAvailSize(IN std_q_struct *q);

/////////////////////////////////////////////////////////////////////
void mtk_sys_assert(kal_bool a, kal_char *file, kal_uint32 line);
void mtk_sys_get_datetime(char *dt_str, int sz);

void mtk_vt_timer_init();
void mtk_vt_timer_deinit();
void mtk_vt_timer_start(kal_uint8 timer_id, kal_uint32 period, kal_timer_func_ptr timer_expiry, void *arg);
void mtk_vt_timer_stop(kal_uint8 timer_id);
kal_mutexid kal_create_mutex(const kal_char* mutex_name);
void kal_give_mutex(kal_mutexid ext_mutex_id);
void kal_take_mutex(kal_mutexid ext_mutex_id);
void kal_release_mutex(kal_mutexid ext_mutex_id);

#ifdef VT_OS_TYPE_LINUX
#define _T
#define RETAILMSG(ignore, p) _D(p)

#include <semaphore.h>
typedef sem_t event_t;
#define INVALID_EVENT_HANDLE_VALUE {0}
//typedef struct {
//	pthread_mutex_t mutex;
//	pthread_cond_t condvar;
//}event_t;
#else
#error os type windows here
typedef HANDLE  event_t;
#endif

kal_bool create_event(event_t *event);
kal_bool destroy_event(event_t *event);
kal_bool set_event(event_t *event);
kal_bool wait_event(event_t *event);
kal_int32 wait_event_timeout(event_t *event, kal_int32 timeout_sec, kal_int32 timeout_ns);

#define SEM_INIT(s) sem_init(&s,0,0)
#define SEM_INIT_MORE(s,m) sem_init(&s,0,m)
#define SEM_WAIT(s) do{sem_wait(&s);}while(0);
#define SEM_WAIT_TIMEOUT(sem, sec, ret) do { \
		struct timespec ts; \
		ts.tv_sec = time(NULL) + sec; \
		ts.tv_nsec = 0; \
		ret = sem_timedwait(&sem, &ts); \
		} while(0);

#define SEM_POST(s) sem_post(&s)
#define SEM_DESTROY(s) sem_destroy(&s)
#define SAFE_DELETE(ptr)	do{ if(ptr){free(ptr); ptr = NULL;} }while(0)

#ifdef __cplusplus
}
#endif

#endif /* __MTK_VT_UTILS_H__ */
