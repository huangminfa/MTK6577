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

#ifndef __MTK_AGPS_TASK_H__
#define __MTK_AGPS_TASK_H__

#ifdef __cplusplus
extern "C" {
#endif

#include <pthread.h>
#include <sys/poll.h>
#include "mtk_agps_def.h"
//#include "supl2mmi_enums.h"

#define SOCKET_SUPL   "/data/agps_supl/soc_supl"
#define SOCKET_RRLP   "/data/agps_supl/soc_rrlp"
#define SOCKET_ULCS   "/data/agps_supl/soc_ulcs"
#define SOCKET_GPS    "/data/agps_supl/soc_gps"
#define SOCKET_MMI    "/data/agps_supl/soc_mmi"
#define SOCKET_TLS    "/data/agps_supl/soc_tls"
#define SOCKET_SOC    "/data/agps_supl/soc_soc"

/*
typedef enum {
    MOD_NIL = 0,
    MOD_SUPL,
    MOD_SUPL_CONN,
    MOD_RRLP,
    MOD_GPS,
    MOD_MMI,
    MOD_SOC,
    MOD_TLS,
    MOD_CNT
} agps_mod_enum;
*/
typedef enum {
    SOC_FD_ADD,
    SOC_FD_REMOVE,
    SOC_FD_EXIT
} agps_soc_cmd_enum;

typedef struct {
    int soc_cmd;
    int soc_fd;
} agps_soc_cmd_struct;

typedef struct {
    int                soc_fd;
    struct sockaddr_un soc_addr;
    socklen_t          soc_addr_len;
} soc_struct;

typedef struct
{
    unsigned char  srcMod;
    unsigned char  dstMod;
    unsigned short type;
    unsigned short length;
    char data[MTK_AGPS_PMTK_SZ];
} mtk_agps_msg_struct;

extern soc_struct g_socket_structs[];
extern agps_supl_context_struct g_rSuplCtx;
extern pthread_t g_agps_tid_tbl[AGPS_TASK_CNT];
extern pthread_mutex_t g_agps_verify_mtx;
extern pthread_cond_t g_agps_verify_cond;
extern agps_notify_ret_enum g_notify_ret;
//for open gps
extern pthread_mutex_t g_agps_openGps_mtx;
extern pthread_cond_t g_agps_openGps_cond;
//for reset gps
extern pthread_mutex_t g_agps_resetGps_mtx;
extern pthread_cond_t g_agps_resetGps_cond;
//for waiting gps status after send open/close command
extern pthread_mutex_t g_agps_waiting_GpsStatus_mtx;
extern pthread_cond_t g_agps_waiting_GpsStatus_cond;


#define GET_SUPL_CTX()       (&g_rSuplCtx)
#define GET_SUPL_PROFILE()   (&g_rSuplCtx.rProfile)
#define GET_SUPL_QOP()       (&g_rSuplCtx.rQop)
#define GET_SUPL_MODE()      (g_rSuplCtx.eSuplMode)
#define SET_SUPL_MODE(mode)  (g_rSuplCtx.eSuplMode = mode)

#define GET_AGPS_TID(t)      (g_agps_tid_tbl[t])

#define THREAD_START(t)      (g_agps_thd_enable[t] = 1)
#define THREAD_STOP(t)       (g_agps_thd_enable[t] = 0)
#define IS_THREAD_RUNNING(t) (g_agps_thd_enable[t])

#define THD_SOCKET(s)        (g_socket_structs[s])
#define THD_SOCKET_FD(s)     (g_socket_structs[s].soc_fd)

#define SENDTO(t, msg)                                  \
do {                                                    \
    sendto(THD_SOCKET(t).soc_fd,                        \
    &msg,                                               \
    sizeof(msg),                                        \
    0,                                                  \
    (const struct sockaddr *)&THD_SOCKET(t).soc_addr,   \
    (socklen_t)THD_SOCKET(t).soc_addr_len);             \
} while(0);

#define RECVFM(t, msg)                                  \
do {                                                    \
    recvfrom(THD_SOCKET(t).soc_fd,                      \
    &msg,                                               \
    sizeof(msg),                                        \
    0,                                                  \
    (struct sockaddr *)&THD_SOCKET(t).soc_addr,         \
    &THD_SOCKET(t).soc_addr_len);                       \
} while(0);

//C.K. add--> prevent ALPS00092036 similiar issue
#define RECVFM_RSLT(t, msg, len)                                  \
do {                                                    \
    len = recvfrom(THD_SOCKET(t).soc_fd,                      \
    &msg,                                               \
    sizeof(msg),                                        \
    0,                                                  \
    (struct sockaddr *)&THD_SOCKET(t).soc_addr,         \
    &THD_SOCKET(t).soc_addr_len);                       \
} while(0);
//C.K. add<-- prevent ALPS00092036 similiar issue

void agps_poll_register(struct pollfd *poll_fds, int* poll_cnt, int res_fd,short events);
void agps_poll_register_soc(int res_fd);
void agps_add_soc_fd(int res_fd);
void agps_remove_soc_fd(int res_fd);
void agps_exit_soc_fd();
/*
typedef void (*mtk_agps_ind_em_cb)(int em_enum, void *arg);
typedef void (*mtk_agps_ind_info_cb)(int info_enum, void *arg);
typedef void (*mtk_agps_ind_notify_cb)(int notify_enum, void *arg);
typedef void (*mtk_agps_ind_error_cb)(int error_enum, void *arg);
*/
mtk_agps_ind_em_cb agps_ind_em_cb;
mtk_agps_ind_info_cb agps_ind_info_cb;
mtk_agps_ind_notify_cb agps_ind_notify_cb;
mtk_agps_ind_error_cb agps_ind_error_cb;
mtk_agps_ind_opengps_cb agps_ind_opengps_cb;
mtk_agps_position_notify_cb agps_position_notify;

void agps_set_cb(
    mtk_agps_ind_em_cb em_cb,
    mtk_agps_ind_info_cb info_cb,
    mtk_agps_ind_notify_cb notify_cb,
    mtk_agps_ind_error_cb error_cb,
    mtk_agps_ind_opengps_cb opengps_cb,
    mtk_agps_position_notify_cb pos_notify_cb);

agps_ret_enum mtk_agps_init(const char *name_str, const char *addr_str, int port, int tls);
void mtk_agps_deinit();
void mtk_agps_update_cinfo(const char *imsi, const char *mccmnc, int lac, int cid);
//int mtk_get_cell_type();
void mtk_agps_config(int hacc,int vacc, int mla, int delay, int smod, agps_supl_set_id_enum sid, unsigned char nt, unsigned char vt, int cap, int ni_iot, int ecid_en);
void mtk_agps_response(int resp);
void mtk_agps_request_si(agps_supl_mode_enum mode);
void mtk_agps_stop();
void mtk_agps_ni_req(char *data, int len);

extern agps_notify_ret_enum agps_wait_response();

#ifdef __cpluscplus
}
#endif

#endif
