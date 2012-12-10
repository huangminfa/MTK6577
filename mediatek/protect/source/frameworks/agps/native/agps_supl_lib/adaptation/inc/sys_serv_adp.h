/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2006
*
*  BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
*  THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
*  RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON
*  AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
*  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
*  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
*  NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
*  SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
*  SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH
*  THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
*  NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S
*  SPECIFICATION OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
*
*  BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
*  LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
*  AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
*  OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY BUYER TO
*  MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
*
*  THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE
*  WITH THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF
*  LAWS PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING THEREOF AND
*  RELATED THERETO SHALL BE SETTLED BY ARBITRATION IN SAN FRANCISCO, CA, UNDER
*  THE RULES OF THE INTERNATIONAL CHAMBER OF COMMERCE (ICC).
*
*****************************************************************************/

/*****************************************************************************
 *
 * Filename:
 * ---------
 *  sys_serv_adp.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   The AGPS SWIP adaption layer.
 *
 * Author:
 * -------
 *  Leo Hu
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * 03 15 2012 archilis.wang
 * [ALPS00253400] [Need Patch] [Volunteer Patch]Move AGPS in userdata.img to system.img
 * .
 *
 * 01 16 2012 ck.chiang
 * [ALPS00116556] [0109 CMCC New Case] AGPS 5.1.1 AGPS ????
 * <saved by Perforce>
 *
 * 09 21 2011 archilis.wang
 * [ALPS00066919] [GIN-Dual-SIM] [SUPL] SET can't authenticate the server via checking the server's certificate.
 * .
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef __SYS_SERV_ADP_H__
#define __SYS_SERV_ADP_H__

#include <unistd.h>
#include <stdio.h>

#include "typedef.h"
#include "mtk_service.h"
#include "mtk_agps_def.h"

#ifndef GPS_UART_OPEN_FINISH_EVENT
#define GPS_UART_OPEN_FINISH_EVENT          (0x0001)
#endif
#ifndef GPS_UART_READ_FINISH_EVENT
#define GPS_UART_READ_FINISH_EVENT          (0x0002)
#endif
#ifndef GPS_UART_WRITE_FINISH_EVENT
#define GPS_UART_WRITE_FINISH_EVENT         (0x0004)
#endif
#ifndef GPS_UART_CLOSE_FINISH_EVENT
#define GPS_UART_CLOSE_FINISH_EVENT         (0x0008)
#endif

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

extern struct timespec g_null_timespec;
extern FILE *g_dbg_file_SUPL;
extern FILE *g_dbg_file;
#define SOC_NB 1

#define GET_TIME()     _kal_get_time()
#define GET_TICK_CNT() _kal_get_time()
#define _sys_mem_alloc(size) sys_mem_alloc(size, __FILE__, __LINE__)
#define _sys_mem_free(ptr)   sys_mem_free(ptr, __FILE__, __LINE__)

void *sys_mem_alloc(kal_uint32 len, kal_char *file, kal_uint32 line);
void sys_mem_free(void *mem_ptr, kal_char *file, kal_uint32 line);
void *sys_mem_cpy(void* dest, const void* src, kal_uint32 size);
void *sys_mem_set(void* dest, kal_uint8 value, kal_uint32 size);
kal_int32 sys_mem_cmp(const void* src1, const void* src2, kal_uint32 size);

void sys_assert(/*//$$int*/kal_bool a, kal_char *file, kal_uint32 line);
void sys_ext_assert(/*//$$int*/kal_bool a, int ext1, int ext2, int ext3);

S8 save_gps_logging_switch(U8 *data);
S8 load_gps_logging_switch(U8 *data);

module_type stack_get_active_module_id(void);

void msg_send_ext_queue(ilm_struct *ilm);
void mtk_send_msg(msg_type msg_id, module_type src_mod, module_type dst_mod, void *local_param_ptr);
//void send_pmtk(MTK_AGPS_SUPL_MSG_T type, char *Buf, unsigned int size);

/*OS related function*/
void mtk_mutex_init();
void mtk_mutex_destroy();
kal_mutexid kal_create_mutex (kal_char* mutex_name);
void kal_give_mutex(kal_mutexid mutex);
void kal_take_mutex(kal_mutexid mutex);

void mtk_event_init();
void mtk_event_destroy();
kal_eventgrpid kal_create_event_group(kal_char *name);
kal_status kal_set_eg_events(kal_eventgrpid evg, kal_uint32 events, kal_uint8 operation);
kal_status kal_retrieve_eg_events(kal_eventgrpid evg, kal_uint32 requested_events, kal_uint8 operation,
                                  kal_uint32 *retrieved_events, kal_uint32 suspend);

extern module_type stack_get_active_module_id(void);

/* trace log related function */
extern void kal_trace(trace_class_enum trc_class, kal_uint32 msg_index,...);
extern void kal_trace_ulcs(trace_class_enum trc_class, const kal_char *fmt, ...);

#define _USE_SERV_ADP_TRACE_IMP_    1

#if _USE_SERV_ADP_TRACE_IMP_
    extern void kal_brief_trace(trace_class_enum trc_class, const kal_char *fmt, ...);
#else
    #define kal_brief_trace
#endif

extern void kal_wap_trace(module_type mod_id, trace_class_enum trc_class, const char *fmt,...);
extern void kal_prompt_trace(module_type mod_id, const kal_char *fmt,...);
#if 0
extern void kal_lib_trace(trace_class_enum trc_class, kal_uint32 msg_index, /*int type,*/ ...);
#else
extern void kal_lib_trace(trace_class_enum trc_class, char *fmt, ...);
#endif

//extern void kal_lib_trace(trace_class_enum trc_class, kal_uint32 msg_index, ...);
extern void GPS_TRACE(module_type mod_id, trace_class_enum trc_class, const char *fmt,...);
extern kal_uint32 kal_secs_to_ticks(kal_uint32 secs);
extern void kal_print_string_trace(module_type mod_id, trace_class_enum trc_class, const char *fmt, ...);
extern void kal_wsprintf(WCHAR *outstr, char *fmt,...);

/* time related function */
kal_uint32 _kal_get_time();
void kal_get_time(kal_uint32 *ticks_ptr);
kal_uint32 kal_secs_to_ticks(kal_uint32 secs);
void kal_get_date_time(char *dt_str, int sz);
void kal_get_date_time_str(char *dt_str, int sz);
void kal_get_date_time_CMCC_short(char *dt_str, int sz);
void RTC_GetTime(t_rtc *rtctime);

/* file system related function */
typedef int FS_HANDLE;

#define FS_DRIVE_V_REMOVABLE     0x00000010
#define FS_DRIVE_V_NORMAL        0x00000008
#define FS_NO_ALT_DRIVE          0x00000001
#define FS_NO_ERROR                       0
#define FS_CREATE_ALWAYS         0x00020000L

int FS_Open(const WCHAR *FileName, UINT Flag);
S32 FS_Close(S32 handle);
int FS_SetCurrentDir(const WCHAR * DirName);
int FS_CreateDir(const WCHAR *DirName);
int FS_GetDrive(UINT Type, UINT Serial, UINT AltMask);
int FS_Commit(FS_HANDLE FileHandle);
int FS_Write(FS_HANDLE FileHandle, void *DataPtr, UINT Length, UINT *Written);
int FS_Extend(FS_HANDLE FileHandle, UINT Length);
int FS_XDelete(const WCHAR *FullPath, unsigned int Flag, unsigned char *RecursiveStack, const unsigned int StackSize);
int FS_Delete(const WCHAR * FileName);

S8    save_gps_logging_switch(U8 *data);
S8    load_gps_logging_switch(U8 *data);

/* debug related function */
agps_ret_enum mtk_agps_debug_init();
void mtk_agps_debug_uninit();
void mtk_agps_debug_printf(int prio, const char *fmt, ...);
void mtk_agps_debug_printf_SUPL(int prio, const char *fmt, ...);
void create_log_file(int type);
void destroy_log_file(int type);
void mtk_log_file_delete();
void reopen_supl_log(void);

#if DEBUG_MEM
void mtk_agps_debug_mem_init();
void mtk_agps_debug_mem_deinit();
void mtk_agps_debug_mem_record_dump();
void *mtk_agps_debug_malloc(kal_uint32 sz, kal_char *file, kal_uint32 line);
void mtk_agps_debug_free(void *mem_ptr, kal_char *file, kal_uint32 line);
#endif

#define ALLOC_SEND_ILM(macro_src_mod, macro_dest_mod, macro_sap_id, macro_msg_id, macro_local_para_ptr, macro_peer_buff_ptr) \
{ \
   ilm_struct *macro_ilm_ptr = allocate_ilm(macro_src_mod); \
   macro_ilm_ptr->src_mod_id  = macro_src_mod;  \
   macro_ilm_ptr->dest_mod_id = macro_dest_mod; \
   macro_ilm_ptr->sap_id = macro_sap_id; \
   macro_ilm_ptr->msg_id = macro_msg_id; \
   macro_ilm_ptr->local_para_ptr = (local_para_struct *)macro_local_para_ptr; \
   macro_ilm_ptr->peer_buff_ptr = (peer_buff_struct *)macro_peer_buff_ptr; \
   msg_send_ext_queue(macro_ilm_ptr); \
}
#endif /* __SYS_SERVICE_H__ */
