/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2005
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

/*******************************************************************************
 * Filename:
 * ---------
 *   fs_utility.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This is the header file of File System utility
 *
 * Author:
 * -------
 *   Stanley Chu
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision$
 * $Modtime$
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * 08 16 2010 stanley.chu
 * [MAUI_02606730] [System Service][File System][Change Feature] Shrink code size for ULC projects by removing kal_check_stack from FS codes
 * <saved by Perforce>
 *
 * 08 09 2010 stanley.chu
 * [MAUI_02562433] [System Service][File System][Debug] Transform FS Trace from SYS Trace mechanism to Primitive Trace (kal_trace)
 * FS Trace MAUI
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef _FS_UTILITY_H
#define _FS_UTILITY_H

#include "custom_fs.h"
#include "fs_type.h"
#include "kal_release.h"
#include "kal_trace.h"

#ifdef __FS_TRACE_SUPPORT__
#define FS_TRACE_MAX_TASKNAME_LEN   (3)
#define FS_TRACE_MAX_FILENAME_LEN   (128)
#define MAX_TRACE_STR_LEN           (128)
#define MT_TRACE_TMP_LEN            (1536)
extern kal_uint32 g_TraceFlag;
//extern kal_bool   gFS_TraceTask[][2];
extern char       TraceStrBuf[MAX_TRACE_STR_LEN];
extern char       TraceTmpBuf[MT_TRACE_TMP_LEN];
#endif // __FS_TRACE_SUPPORT__

/*-------------------------------------
 * Enumerations and Structures
 *-------------------------------------*/

typedef enum {
    rtfiles_c           = 0
   ,fs_func_c
   ,fs_internal_c
   ,fs_unknown_src_file
} FS_TRACE_SRC_FILE;

typedef struct
{
   kal_uint32 RTCTime;
   kal_uint32 SYSTime;
} FS_TRACE_TIME;

/*-------------------------------------
 * Macro Definitions
 *-------------------------------------*/

// Trace extra info options
#define MT_TRACE_INFO_FILE    (0x00000100)
#define MT_TRACE_INFO_WSTR    (0x00000200)
#define MT_TRACE_INFO_BSTR    (0x00000400)
#define MT_TRACE_INFO_MASK    (0x00000F00)

// Trace class
#define MT_TRACE_CLASS_MASK   (0x000000FF)

#if defined(__FS_TRACE_SUPPORT__)
#define fs_util_time_init()               kal_uint32 _time_start, _time_stop
#define fs_util_time_init_with_duration() kal_uint32 _time_start, _time_stop, _time_duration
#define fs_util_time_start()              fs_util_get_time(&_time_start)
#define fs_util_time_stop()               fs_util_get_time(&_time_stop)
#define fs_util_time_get_duration()       fs_util_get_duration(_time_start, _time_stop)
#define fs_util_time_set_duration()       _time_duration = fs_util_get_duration(_time_start, _time_stop)
#define fs_util_time_query_duration()     _time_duration

#else // !__FS_TRACE_SUPPORT__
#define fs_util_time_init()
#define fs_util_time_init_with_duration()
#define fs_util_time_start()
#define fs_util_time_stop()
#define fs_util_time_get_duration()       0
#define fs_util_time_set_duration()
#define fs_util_time_query_duration()     0
#endif // __FS_TRACE_SUPPORT__

#ifndef GEN_FOR_PC

#ifdef __FS_TRACE_SUPPORT__
extern void fs_util_trace_info4(kal_uint32 flag, kal_uint32 msg_index, const char *arg_type, kal_uint32 data1, kal_uint32 data2, kal_uint32 data3, kal_uint32 data4, void *extra_info);
extern void fs_util_trace_info2(kal_uint32 flag, kal_uint32 msg_index, const char *arg_type, kal_uint32 data1, kal_uint32 data2, void *extra_info);
extern void fs_util_trace_info1(kal_uint32 flag, kal_uint32 msg_index, const char *arg_type, kal_uint32 data1, void *extra_info);
extern void fs_util_trace_info0(kal_uint32 flag, kal_uint32 msg_index, const char *arg_type, void *extra_info);
extern void fs_util_trace_err(kal_uint32 flag, kal_int32 errcode, FS_TRACE_SRC_FILE filecode, kal_uint32 line, void *extra_info);
extern void fs_util_trace_str(kal_uint32 flag, void *str);
#else // !__FS_TRACE_SUPPORT__
#define fs_util_trace_info4(A, B, C, D, E, F, G)
#define fs_util_trace_info2(A, B, C, D, E)
#define fs_util_trace_info1(A, B, C, D)
#define fs_util_trace_info0(A, B, C)
#define fs_util_trace_err(A, B, C, D, E)
#define fs_util_trace_str(A, B)

#endif // __FS_TRACE_SUPPORT__

#endif // !GEN_FOR_PC


#if (defined(DEBUG_KAL) && !defined(__FS_PROPRIETARY_SET__))
   #define fs_util_check_stack   kal_check_stack
#else  // !DEBUG_KAL || __FS_PROPRIETARY_SET__
   #define fs_util_check_stack()
#endif // DEBUG_KAL && !__FS_PROPRIETARY_SET__

/*-------------------------------------
 * Exported Functions
 *-------------------------------------*/

extern void                fs_util_wstr_to_bstr(const WCHAR *wstr, char *bstr, kal_uint32 bstr_len);
extern void                fs_util_get_time(kal_uint32 *time);
extern void                fs_util_get_time_aux(FS_TRACE_TIME *Time);
extern kal_uint32          fs_util_get_duration(kal_uint32 start_time, kal_uint32 end_time);
extern kal_uint32          fs_util_get_duration_aux(FS_TRACE_TIME *StartTime, FS_TRACE_TIME *EndTime);
extern kal_uint32          fs_util_get_devtype_by_devflag(kal_uint32 dev_flag);
extern FS_File*            fs_util_get_file_by_fh(FS_HANDLE fh);

/*-------------------------------------
 * External Function References
 *-------------------------------------*/

extern kal_bool            kal_trace_check_filter_on(trace_class_enum trc_class, kal_uint32 msg_index);

#if (defined(DEBUG_KAL) && !defined(__FS_PROPRIETARY_SET__))
   extern void             kal_check_stack(void);
#endif // DEBUG_KAL && !__FS_PROPRIETARY_SET__

#endif // _FS_UTILITY_H
