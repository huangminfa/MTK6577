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
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef __SYS_SERV_ADP_H__
#define __SYS_SERV_ADP_H__

#include "typedef.h"
#include "mtk_agps_def.h"
#include "mtk_service.h"

#include "fs_errcode.h"
#include "tls_adp_defs.h"

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

#define SOC_NB 1

#define GET_TIME() _kal_get_time()
#define _sys_mem_alloc(size) sys_mem_alloc(size, __FILE__, __LINE__)
#define _sys_mem_free(ptr) sys_mem_free(ptr, __FILE__, __LINE__)

void *sys_mem_alloc(kal_uint32 len, kal_char *file, kal_uint32 line);
void sys_mem_free(void *mem_ptr, kal_char *file, kal_uint32 line);
void *sys_mem_cpy(void* dest, const void* src, kal_uint32 size);
void *sys_mem_set(void* dest, kal_uint8 value, kal_uint32 size);
kal_int32 sys_mem_cmp(const void* src1, const void* src2, kal_uint32 size);

void sys_assert(/*//$$int*/kal_bool a, kal_char *file, kal_uint32 line);
void sys_ext_assert(/*//$$int*/kal_bool a, int ext1, int ext2, int ext3);

void msg_send_ext_queue(ilm_struct *ilm);

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

/*OS related function*/
kal_mutexid kal_create_mutex (kal_char* mutex_name);
void kal_give_mutex(kal_mutexid mutex);
void kal_take_mutex(kal_mutexid mutex);

kal_eventgrpid kal_create_event_group(kal_char *name);
kal_status kal_set_eg_events(kal_eventgrpid evg, kal_uint32 events, kal_uint8 operation);
kal_status kal_retrieve_eg_events(kal_eventgrpid evg, kal_uint32 requested_events, kal_uint8 operation, 
                                  kal_uint32 *retrieved_events, kal_uint32 suspend);


extern module_type stack_get_active_module_id(void);

//!!extern void kal_trace(trace_class_enum trc_class, kal_uint32 msg_index,...);
extern void kal_trace(trace_class_enum trc_class, char *fmt, ...);

#define GET_TICK_CNT() _kal_get_time()

extern void kal_wap_trace(module_type mod_id, trace_class_enum trc_class, const char *fmt,...);
extern void kal_prompt_trace(module_type mod_id, const kal_char *fmt,...);
extern void kal_lib_trace(trace_class_enum trc_class, kal_char *fmt, ...);
//$$extern void kal_lib_trace(trace_class_enum trc_class, kal_uint32 msg_index,...);
//!!extern void kal_lib_trace(trace_class_enum trc_class, kal_uint32 msg_index, const kal_char* arg_type,...);
extern void GPS_TRACE(module_type mod_id, trace_class_enum trc_class, const char *fmt,...);
extern kal_uint32 kal_secs_to_ticks(kal_uint32 secs);

void kal_get_time(kal_uint32 *ticks_ptr);
kal_uint32 app_getcurrtime();

//!!== copy from fs_type.h ==
typedef int FS_HANDLE;

//FS_Open Parameter
#define FS_READ_WRITE            0x00000000L
#define FS_READ_ONLY             0x00000100L
#define FS_OPEN_SHARED           0x00000200L
#define FS_OPEN_NO_DIR           0x00000400L
#define FS_OPEN_DIR              0x00000800L
#define FS_CREATE                0x00010000L
#define FS_CREATE_ALWAYS         0x00020000L
#define FS_COMMITTED             0x01000000L
#define FS_CACHE_DATA            0x02000000L
#define FS_LAZY_DATA             0x04000000L
#define FS_NONBLOCK_MODE         0x10000000L
#define FS_PROTECTION_MODE       0x20000000L
#define FS_NOBUSY_CHECK_MODE     0x40000000L

#define FS_MOVE_COPY             0x00000001     // FS_Move only, Public
#define FS_MOVE_KILL             0x00000002     // FS_Move only, Public

#define FS_FILE_TYPE             0x00000004     // Recursive Type API Common, Public
#define FS_DIR_TYPE              0x00000008     // Recursive Type API Common, Public
#define FS_RECURSIVE_TYPE        0x00000010     // Recursive Type API Common, Public

#define FS_ATTR_READ_ONLY        0x01
#define FS_ATTR_HIDDEN           0x02
#define FS_ATTR_SYSTEM           0x04
#define FS_ATTR_VOLUME           0x08
#define FS_ATTR_DIR              0x10
#define FS_ATTR_ARCHIVE          0x20
#define FS_LONGNAME_ATTR         0x0F

typedef struct
{
   unsigned int Second2:5;
   unsigned int Minute:6;
   unsigned int Hour:5;
   unsigned int Day:5;
   unsigned int Month:4;
   unsigned int Year1980:7;
} FS_DOSDateTime;

typedef struct
{
   char           FileName[8];
   char           Extension[3];
   unsigned char  Attributes;
   unsigned char  NTReserved;
   unsigned char  CreateTimeTenthSecond;
   FS_DOSDateTime CreateDateTime;
   unsigned short LastAccessDate;
   unsigned short FirstClusterHi;
   FS_DOSDateTime DateTime;
   unsigned short FirstCluster;
   unsigned int   FileSize;
   // FS_FileOpenHint members (!Note that RTFDOSDirEntry structure is not changed!)
   unsigned int   Cluster;
   unsigned int   Index;
   unsigned int   Stamp;
   unsigned int   Drive;
   unsigned int   SerialNumber;
} FS_DOSDirEntry;

/*file system related function*/
int FS_Open(const WCHAR *FileName, UINT Flag);
S32 FS_Close(S32 handle);
int FS_SetCurrentDir(const WCHAR * DirName);
int FS_CreateDir(const WCHAR *DirName);
int FS_GetDrive(UINT Type, UINT Serial, UINT AltMask);
int FS_Commit(FS_HANDLE FileHandle);
int FS_Read(FS_HANDLE FileHandle, void * DataPtr, UINT Length, UINT * Read);
int FS_Write(FS_HANDLE FileHandle, void *DataPtr, UINT Length, UINT *Written);

//Information
int FS_GetFileSize(FS_HANDLE FileHandle, UINT * Size);
int FS_GetAttributes(const WCHAR * FileName);

//File Only Operation
int FS_SetAttributes(const WCHAR * FileName, BYTE Attributes);
int FS_Delete(const WCHAR * FileName);

//File and Folder Operations
int FS_Extend(FS_HANDLE FileHandle, UINT Length);

//Find File
int FS_FindFirst(const WCHAR * NamePattern, BYTE Attr, BYTE AttrMask, FS_DOSDirEntry * FileInfo, WCHAR * FileName, UINT MaxLength);
int FS_FindNext(FS_HANDLE FileHandle, FS_DOSDirEntry * FileInfo, WCHAR * FileName, UINT MaxLength);
int FS_FindClose(FS_HANDLE FileHandle);

S8    save_gps_logging_switch(U8 *data);
S8    load_gps_logging_switch(U8 *data);

#if DBG_LOG
agps_ret_enum mtk_agps_debug_init();
void mtk_agps_debug_uninit();
void mtk_agps_debug_printf(int prio, const char *fmt, ...);
#endif

#if DBG_MEM
void mtk_agps_debug_mem_init();
void mtk_agps_debug_mem_deinit();
void mtk_agps_debug_mem_record_dump();
void *mtk_agps_debug_malloc(kal_uint32 sz, kal_char *file, kal_uint32 line);
void mtk_agps_debug_free(void *mem_ptr, kal_char *file, kal_uint32 line);
#endif

void kal_get_date_time(char *dt_str, int sz);

extern struct timespec g_null_timespec;

extern void kal_print_string_trace(module_type mod_id, trace_class_enum trc_class, const char *fmt, ...);

#endif /* __SYS_SERVICE_H__ */
