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

#ifndef __MTK_VT_TASKS_H__
#define __MTK_VT_TASKS_H__

#ifdef __cplusplus
extern "C" {
#endif

#include <pthread.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <sys/un.h>

#include "mtk_vt_defs.h"

//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////
#define SOCKET_VT_TCV  "soc_vt_tcv"
//#define SOCKET_VT_TCVR "/data/soc_vt_tcvr"
//#define SOCKET_VT_TCVT "/data/soc_vt_tcvt"
#define SOCKET_VT_STK  "soc_vt_stk"
#define SOCKET_VT_SVC  "soc_vt_svc"
#define SOCKET_VT_DLVP	"soc_vt_dlvp"
#define SOCKET_VT_DLAP	"soc_vt_dlap"
#define SOCKET_VT_ULVP	"soc_vt_ulvp"
#define SOCKET_VT_ULAP	"soc_vt_ulap"


#define THREAD_START(t)      (g_vt_thd_enable[t] = 1)
#define THREAD_STOP(t)       (g_vt_thd_enable[t] = 0)
#define IS_THREAD_RUNNING(t) (g_vt_thd_enable[t])

char *get_thd_name();
//#define GET_MOD_NAME(m)  (g_vt_mod_name[m])
#define GET_THD_NAME()   (get_thd_name())
#define GET_TASK_NAME(t) (g_vt_task_name[t])

#define THD_SOCKET(s)    (g_socket_structs[s])
#define THD_SOCKET_FD(s) (g_socket_structs[s].soc_fd)
#define THD_MSG_FD(s)    (g_socket_structs[s].msg_fd)

typedef struct {
    int                msg_fd;
    int                soc_fd;
    struct sockaddr_un soc_addr;
    socklen_t          soc_addr_len;
} soc_struct;

//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////

extern int g_mux_fd;
extern int g_vt_thd_enable[VT_TASK_CNT];
extern pthread_t g_vt_tid_tbl[VT_TASK_CNT];
extern soc_struct g_socket_structs[VT_TASK_CNT];
extern char *g_vt_task_name[VT_TASK_CNT];

vt_ret_enum mtk_vt_open_thd_socket(vt_task_enum t, char *s);
vt_ret_enum mtk_vt_close_thd_socket(vt_task_enum t);
vt_ret_enum mtk_vt_init();
vt_ret_enum mtk_vt_deinit();



#ifdef __cplusplus
}
#endif

#endif /* __MTK_VT_TASKS_H__ */
