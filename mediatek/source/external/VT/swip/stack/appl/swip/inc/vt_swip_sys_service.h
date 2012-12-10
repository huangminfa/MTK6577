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

#ifndef __VT_SWIP_SYS_SERVICE_H__
#define __VT_SWIP_SYS_SERVICE_H__

/***************************************************************************/
/*                         MACRO Definitions                               */
/***************************************************************************/
#include "check_heap.h"
#ifdef DEBUG_HEAP_CHECK
#define sys_malloc(sz)  MALLOC(sz);
#define sys_free(ptr)   FREE(ptr);
#else
#define sys_malloc(sz)  malloc(sz);
#define sys_free(ptr)   free(ptr);
#endif

#define sys_assert(exp) mtk_sys_assert((kal_bool)exp, __FILE__, __LINE__)
#define sys_printf      mtk_sys_printf

/*************************************************************************
 * SYS FUNCTIONS
*************************************************************************/
void mtk_sys_assert(kal_bool a, char *filename, unsigned int line);
void mtk_sys_printf(const kal_char *fmt,...);
void kal_print(const kal_char *fmt,...);
void kal_trace(trace_class_enum trc_class, kal_uint32 msg_index, ...);
//void kal_brief_trace(trace_class_enum trc_class, kal_uint32 msg_index, ...);
#define kal_brief_trace kal_trace
void kal_buffer_trace(trace_class_enum trc_class, kal_uint32 msg_index, kal_uint32 buf_len, kal_uint8* pData);
void kal_get_time(kal_uint32 *ticks_ptr);
void kal_get_time64(kal_int64 *ticks_ptr);
void kal_get_resln64(kal_int64 *resln_ptr);

/*************************************************************************
 * MEM FUNCTIONS
*************************************************************************/
void *construct_local_para(kal_uint32 size, kal_uint8 param);
void free_local_para(void *ptr);
void *construct_peer_buff(kal_uint16 pdu_len, kal_uint16 header_len, kal_uint16 tail_len, kal_uint8 param);
void *construct_peer_buff_ext(void *pdu, kal_uint16 pdu_len);
void free_peer_buff(void *ptr);
void *get_ctrl_buffer(kal_uint32 buff_size);
void free_ctrl_buffer(void *ptr);
#define adGetDynamicMem(size, name, line) get_ctrl_buffer(size)
#define adFreeDynamicMem(buff, name, line) free_peer_buff(buff)
//void *adGetDynamicMem(kal_uint32 size, kal_char* filename, kal_int32 lineno);
//void adFreeDynamicMem(void *pBuf, kal_char* filename, kal_int32 lineno);
void *kal_mem_cpy(void* dest, const void* src, signed size);
void *kal_mem_set(void *dest, kal_uint8 value, kal_uint32 size);

/*************************************************************************
*************************************************************************/
KAL_ADM_ID kal_adm_create(void *mem_addr, kal_uint32 size, kal_uint32 *subpool_size, kal_bool islogging);
kal_status kal_adm_delete(KAL_ADM_ID adm_id);
void *kal_adm_alloc(KAL_ADM_ID adm_id, kal_uint32 size);
void kal_adm_free(KAL_ADM_ID adm_id, void *mem_addr);
kal_int32 kal_adm_get_total_left_size(KAL_ADM_ID adm_id);

/*************************************************************************
 * MUTEX FUNCTIONS
*************************************************************************/
kal_mutexid kal_create_mutex(const kal_char* mutex_name);
void kal_give_mutex(kal_mutexid ext_mutex_id);
void kal_take_mutex(kal_mutexid ext_mutex_id);
void kal_release_mutex(kal_mutexid ext_mutex_id);

/*************************************************************************
 * EVSHED Memory FUNCTIONS
*************************************************************************/
void* kal_evshed_get_mem(kal_uint32 size);
void kal_evshed_free_mem( void* buff_ptr );

extern 	eventid timed_event_queue_set_event(event_scheduler *, kal_timer_func_ptr _fn, void *, kal_uint32 delay_ms);
extern void timed_event_queue_cancel_event(event_scheduler *, eventid *eid);
#define evshed_cancel_event timed_event_queue_cancel_event
#define evshed_set_event timed_event_queue_set_event
//kal_int32 evshed_cancel_event(event_scheduler *es, eventid *eid);
//eventid evshed_set_event(event_scheduler *es, kal_timer_func_ptr event_hf, void *event_hf_param, kal_uint32 elapse_time);

event_scheduler *new_evshed(void *timer_id, void (*start_timer)(void *, unsigned int),
                            void (*stop_timer)(void *), kal_uint32  fuzz, malloc_fp_t alloc_fn_p, 
                            free_fp_t free_fn_p, kal_uint8 max_delay_ticks);

/*************************************************************************
 * STACK Timer FUNCTIONS
*************************************************************************/
void stack_init_timer(stack_timer_struct *stack_timer, kal_char *timer_name, module_type mod_id);
void stack_start_timer(stack_timer_struct *stack_timer, kal_uint16 timer_indx, kal_uint32 init_time);
stack_timer_status_type stack_stop_timer(stack_timer_struct *stack_timer);

/*************************************************************************
 * STACK Timer FUNCTIONS
*************************************************************************/
ilm_struct* allocate_ilm(module_type module_id);
void free_ilm(ilm_struct* ilm_ptr);
kal_bool msg_send_ext_queue(ilm_struct* ilm_ptr);

#endif
