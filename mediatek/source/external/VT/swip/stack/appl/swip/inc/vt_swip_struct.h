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

/* data type definition */
#ifndef __VT_SWIP_STRUCT_H__
#define __VT_SWIP_STRUCT_H__

#include <memory.h>
#include "rvosdefs.h"
//#include "termdefs.h"
#include "vt_kal_def.h"
#include "vt_option_cfg.h"

#if (RV_OS_TYPE == RV_OS_TYPE_NUCLEUS)
#error Should not include vt_swip_struct.h in KAL system
#endif

#ifdef __cplusplus
extern "C" {
#endif

/***************************************************************************
  * Platform-Dependent part
  * Enums and structures
 ***************************************************************************/

typedef struct {
	kal_uint8   unused;
} *kal_semid;

typedef enum
{
    RET_SUCC = 0,
    RET_FAIL
}VtStk_Status;

#define RV_OS_TYPE RV_OS_TYPE_LINUX
#if (RV_OS_TYPE == RV_OS_TYPE_LINUX)
#include <pthread.h>
typedef pthread_mutex_t kal_mutexid;
#else
#error os should be linux
typedef kal_uint32       kal_mutexid;
#endif

/*  */

/***************************************************************************
  * Platform-Independent part
  * Enums and structures
 ***************************************************************************/
#define KAL_TICKS_10_MSEC       (10)//!(2)         /* 10 msec */
#define KAL_TICKS_20_MSEC       (20)//             /* 20 msec */
#define KAL_TICKS_50_MSEC       (50)//!(10)        /* 50 msec */
#define KAL_TICKS_100_MSEC     (100)//!(21)        /* 100 msec */
#define KAL_TICKS_500_MSEC     (500)//!(108)       /* 500 msec */
#define KAL_TICKS_1_SEC       (1000)//!(216)       /* 1 sec */
#define KAL_TICKS_5_SEC       (5000)//!(1083)      /* 5 sec */
#define KAL_TICKS_30_SEC     (30000)//!(6500)      /* 30 sec */
#define KAL_TICKS_1_MIN      (60000)//!(13001)     /* 1 min */

typedef enum{
    MOD_VT = 0,
    MOD_MED,
    MOD_CSM,
    MOD_MMI,
    MOD_CSR,
    MOD_L1SP,
    MOD_L4C,
    MOD_END
}mod_id_enum;

#define MED_SAP 0
#define CSM_VT_SAP 1

/* TRACE */
typedef enum {
   TRACE_FUNC,      // 0
   TRACE_STATE,     // 1
   TRACE_INFO,      // 2
   TRACE_WARNING,   // 3
   TRACE_ERROR,     // 4
   TRACE_GROUP_1,   // 5
   TRACE_GROUP_2,   // 6
   TRACE_GROUP_3,   // 7
   TRACE_GROUP_4,   // 8
   TRACE_GROUP_5,   // 9
   TRACE_GROUP_6,   // 10
   TRACE_GROUP_7,   // 11
   TRACE_GROUP_8,   // 12
   TRACE_GROUP_9,   // 13
   TRACE_GROUP_10,  // 14
   TRACE_PEER       // 15
}trace_class_enum;

/* System */
typedef enum {
    KAL_SUCCESS,
    KAL_ERROR,
    KAL_Q_FULL,
    KAL_Q_EMPTY,
    KAL_SEM_NOT_AVAILABLE,
    KAL_WOULD_BLOCK,
    KAL_MESSAGE_TOO_BIG,
    KAL_INVALID_ID,
    KAL_NOT_INITIALIZED,
    KAL_INVALID_LENGHT,
    KAL_NULL_ADDRESS,
    KAL_NOT_RECEIVE,
    KAL_NOT_SEND,
    KAL_MEMORY_NOT_VALID,
    KAL_NOT_PRESENT,
    KAL_MEMORY_NOT_RELEASE
} kal_status;

typedef enum {
    KAL_NO_WAIT,
    KAL_INFINITE_WAIT
} kal_wait_mode;

typedef enum {
    STACK_TIMER_INITIALIZED,
    STACK_TIMER_NOT_RUNNING = STACK_TIMER_INITIALIZED,
    STACK_TIMER_RUNNING,
    STACK_TIMER_NOT_TIMED_OUT = STACK_TIMER_RUNNING,    /* Backward compatiable */
    STACK_TIMER_EXPIRED,
    STACK_TIMER_TIMED_OUT = STACK_TIMER_EXPIRED,        /* Backward compatiable */
    STACK_TIMER_STOPPED
} stack_timer_status_type;

typedef enum {
   TD_UL,      /* Uplink Direction */
   TD_DL,      /* Downlink Direction */
   TD_CTRL     /* Control Plane. Both directions */
} transfer_direction;

typedef enum
{
    MSG_ID_INVALID_TYPE = 0,
    #include "vt_sap.h"
    MSG_ID_VT_CODE_CHECK_POINT,
    MSG_ID_VT_TIMEOUT,
	MSG_ID_STK_TASK_THREAD_EXIT,
	MSG_ID_TOTAL_COUNT
} msg_type;

//typedef int   (*lcd_cmpfunc)(const void *, const void *);
typedef void  (* kal_timer_func_ptr)(void *);
typedef void *(*malloc_fp_t)(unsigned int);
typedef void *(*realloc_fp_t)(void *, unsigned int, unsigned int);
typedef void  (*free_fp_t)(void *);

typedef struct {
    kal_uint8 unused;
}* kal_timerid;

typedef struct stack_timer_struct_t {
    module_type             dest_mod_id;
    kal_timerid             kal_timer_id;
    kal_uint16              timer_indx;
    stack_timer_status_type timer_status;
    kal_uint8               invalid_time_out_count;
} stack_timer_struct;

/* Event scheduler */
typedef struct event_scheduler {
    void *evt_sched;
} event_scheduler;


#if (RV_OS_TYPE == RV_OS_TYPE_LINUX)
#include <time.h>
#include <signal.h>
typedef timer_t eventid;
#else
//!typedef lcd_dll_node *eventid;
typedef unsigned int eventid;
#endif

typedef kal_int32 task_indx_type;

/* ilm struct */
#define MTK_LOCAL_PARA_HDR         \
   kal_uint8    ref_count;         \
   kal_uint16   msg_len;

#define MTK_PEER_BUFF_HDR          \
   kal_uint16   pdu_len;           \
   kal_uint8    ref_count;         \
   kal_uint8    pb_resvered;       \
   kal_uint16   free_header_space; \
   kal_uint16   free_tail_space;

#define LOCAL_PARA_HDR MTK_LOCAL_PARA_HDR
#define PEER_BUFF_HDR  MTK_PEER_BUFF_HDR
#if 0
typedef struct {
    MTK_LOCAL_PARA_HDR
} mtk_local_para_struct;

typedef struct {
    MTK_PEER_BUFF_HDR
} mtk_peer_buff_struct;

#define local_para_struct mtk_local_para_struct
#define peer_buff_struct  mtk_peer_buff_struct
#endif
#define local_para_struct void*
#define peer_buff_struct  void*

typedef int sap_type;
//typedef int msg_type;

typedef struct ilm_struct {
   module_type       src_mod_id;
   module_type       dest_mod_id;
   sap_type          sap_id;
   msg_type          msg_id;
   local_para_struct *local_para_ptr;
   peer_buff_struct  *peer_buff_ptr;
} ilm_struct;

typedef struct {
   msg_type msg_id;
   void *local_para_ptr;
} user_msg_struct;

/***************************************************************************
  * Function Declarations
 ***************************************************************************/
#define VideoCall_SetDecParseCodec(a) do{} while(0);
#define vt_downlink_video_Q_reset(a) do{} while(0);
#define vt_downlink_video_Q_pause(a) do{} while(0);

typedef struct
{
    kal_uint8 unused;
} *kal_eventgrpid;

#if (RV_OS_TYPE == RV_OS_TYPE_LINUX)
#include <time.h>
#include <signal.h>
#endif

typedef struct
{
#if (RV_OS_TYPE == RV_OS_TYPE_LINUX)
	kal_int32 msg_fd;
#else
	#error os should be linux
	kal_uint32 thd_id;
#endif
#if (VT_EVT_DBG_LOG)    // [2010-04-05] Chiwei: Add for tracing event sequence
    kal_uint32 evt_cnt;
#endif
    kal_timer_func_ptr expiry_func_ptr;
    void *expiry_func_param;
#if (RV_OS_TYPE == RV_OS_TYPE_LINUX)
	struct sigevent sigevt;
	timer_t timer_id;
#endif
} evshed_cb_struct;

extern kal_mutexid    _rv_mem_pool_mutex;
extern kal_eventgrpid vt_event_g;

extern void *kal_mem_set(void *dest, kal_uint8 value, kal_uint32 size);
//void vt_start_base_timer(void *base_timer_p, unsigned int time_out);
//void vt_stop_base_timer (void *base_timer_p);
extern void vt_main(ilm_struct *ilm);
extern kal_bool vt_init (task_indx_type task_indx);
extern ilm_struct* allocate_ilm(module_type module_id);
extern void free_ilm(ilm_struct* ilm_ptr);
extern kal_bool msg_send_ext_queue(ilm_struct* ilm_ptr);


#ifdef __cplusplus
}
#endif

#endif /* __TYPEDEF_H__ */
