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

#ifndef __MTK_AGPS_CONFLICT_MGR_H__
#define __MTK_AGPS_CONFLICT_MGR_H__

#ifdef __cplusplus
extern "C" {
#endif

#include <pthread.h>
#include <sys/poll.h>
#include <time.h>
#include <unistd.h>
#include <errno.h>
#include <stdarg.h>
#include <mtd/mtd-user.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <signal.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/stat.h>
#include <fcntl.h>
#include "mmi_msg_struct.h"
#include "typedef.h"
#include "mtk_agps_def.h"
#include "supl2mmi_enums.h"
#include "supl2mmi_struct.h"
#include "mtk_agps_task.h"
//C.K. Chiang add--> For Permanent CP
#include "mtk_agps_common_type.h"
//C.K. Chiang add<--

#include <time.h>
#include <sys/times.h>
#include <sys/time.h>

//#define SOCKET_MMI_2_CCCI        "/data/agps_supl/soc_mmi2ccci"
#define SOCKET_CCCI_L4C            "/data/agps_supl/soc_ccci_L4C"
#define SOCKET_CCCI_GPSTASK        "/data/agps_supl/soc_ccci_GpsTask"

#define AGPS_CP_SUPPORT  1
#define MAX_CP_MOLR_NUM  3

//#define AGPS_CP_RESET_CMD_BY_PMTK106  1 // Move to mtk_agps_common_type.h

/* Notification Only */
#define NOTIFY_LOCATION_ALLOWED                SUPL_MMI_NOTIFY_ONLY
/* Notification and verification, Allow on no answer. (If no answer is received from the SET User, the SET will assume that user consent has been granted and will proceed) */
#define LOCATION_ALLOWED_IF_NO_RESPONSE        SUPL_MMI_NOTIFY_ALLOW_NO_ANSWER
/* Notification and verification, Deny on no answer. (If no answer is received from the SET User, the SET will assume that user consent has been denied and will abort) */
#define LOCATION_NOT_ALLOWED_IF_NO_RESPONSE    SUPL_MMI_NOTIFY_DENY_NO_ANSWER
/*Do not notify user when receive location request */
#define NOTIFY_LOCATION_NOT_ALLOWED            SUPL_MMI_NOTIFY_PRIVACY

//#define MS_Assisted_GPS        L4C_SS_LocationMethod_msAssistedEOTD
//#define MS_Bassed_GPS        L4C_SS_LocationMethod_msBasedEOTD

//typedef struct supl_mmi_position_struct cp_mmi_position_struct;
//typedef struct agps_supl_mode_enum agps_cp_mode_enum;

//define the call state constant
#define CALL_STATE_IDLE 0
#define CALL_STATE_RINGING 1
#define CALL_STATE_OFFHOOK 2


typedef struct {
    char *temp;
    int len;
    char *pread;
    char *pwrite;
}cm_agps_temp_pmtk_buff;

extern char *cm_agps_task_name[CM_AGPS_TASK_CNT];
extern char *cm_agps_mod_name[CM_AGPS_TASK_CNT];

extern soc_struct cm_socket_structs[CM_AGPS_TASK_CNT];
extern int cm_agps_task_enable[CM_AGPS_TASK_CNT];
extern pthread_t cm_agps_thread_tbl[CM_AGPS_TASK_CNT];

#define CM_GET_AGPS_TID(t)       (cm_agps_thread_tbl[t])
#define CM_THREAD_START(t)      (cm_agps_task_enable[t] = 1)
#define CM_THREAD_STOP(t)       (cm_agps_task_enable[t] = 0)
#define CM_IS_THREAD_RUNNING(t) (cm_agps_task_enable[t])
#define CM_THD_SOCKET(s)        (cm_socket_structs[s])
#define CM_THD_SOCKET_FD(s)     (cm_socket_structs[s].soc_fd)
#define CM_GET_TASK_NAME(t)        (cm_agps_task_name[t])
#define CM_GET_MOD_NAME(m)      (cm_agps_mod_name[m])

//C.K. modified--> Change to a function call with log
/*
#define CM_SENDTO(t, msg)                               \
do {                                                    \
    sendto(CM_THD_SOCKET(t).soc_fd,                     \
    &msg,                                               \
    sizeof(msg),                                        \
    0,                                                  \
    (const struct sockaddr *)&CM_THD_SOCKET(t).soc_addr,\
    (socklen_t)CM_THD_SOCKET(t).soc_addr_len);          \
    _D("CM_SENDTO socket[%s] fd[%d]", CM_GET_TASK_NAME(t), CM_THD_SOCKET_FD(t) );   \
} while(0);
*/
//C.K. modified<--

//C.K. add--> prevent ALPS00092036 similiar issue
#define CM_RECVFM_RSLT(t, msg, len)                          \
do {                                                    \
    len = recvfrom(CM_THD_SOCKET(t).soc_fd,                   \
    &msg,                                               \
    sizeof(msg),                                        \
    0,                                                  \
    (struct sockaddr *)&CM_THD_SOCKET(t).soc_addr,      \
    &CM_THD_SOCKET(t).soc_addr_len);                    \
} while(0);
//C.k. add<-- prevent ALPS00092036 similiar issue

#define CM_RECVFM(t, msg)                               \
do {                                                    \
    recvfrom(CM_THD_SOCKET(t).soc_fd,                   \
    &msg,                                               \
    sizeof(msg),                                        \
    0,                                                  \
    (struct sockaddr *)&CM_THD_SOCKET(t).soc_addr,      \
    &CM_THD_SOCKET(t).soc_addr_len);                    \
} while(0);

typedef enum {
    CP_EMERGENCY_CALL   = 0x0001,
    CP_MOLR             = 0x0002,
    CP_MTLR_ss          = 0x0004,
    CP_NILR             = 0x0008,
    SUPL_MOLR           = 0x0010,
    SUPL_MTLR           = 0x0020,
    GPS_RESET           = 0x0040
#ifdef __CDMA_AGPS_SUPPORT__
    ,CDMA_MOLR           = 0x0080
    ,CDMA_MTLR           = 0x0100
#endif
} cm_agps_session_enum;

typedef enum {
    CM_AGPS_STATE_UNKNOWN         = 0x0000,
    CM_AGPS_STATE_TERMINATE     = 0x0001,
    CM_AGPS_STATE_EMCALL_DIALED = 0x0002,
    //CM_AGPS_STATE_START = ,
    CM_AGPS_STATE_WORKING = 0x0004,
    CM_AGPS_STATE_STAND_BY = 0x0008,
    CM_AGPS_STATE_WAIT_POS = 0x0010,
    CM_AGPS_STATE_ABORT = 0x0020 ,
    CM_AGPS_STATE_SET_WORK_MODE = 0x0040,
    CM_AGPS_STATE_GPS_INIT = 0x0080,
    CM_AGPS_STATE_PUSH_WAIT = 0x0100,
    CM_AGPS_STATE_NI_ERROR = 0x0200,
    CM_AGPS_STATE_WAIT_RSP = 0x0400,
    CM_AGPS_STATE_END = 0x0800
} cm_agps_session_state_enum;
//C.K. Chiang marked--> For Permanent CP
#if 0
typedef struct{
    int CP_EM_CALL_State;
    int CP_MOLR_State;
    int CP_MTLR_ss_State;
    int CP_NILR_State;
    int SUPL_MOLR_State;
    int SUPL_MTLR_State;
}cm_agps_session_state_struct;

/*typedef struct{
    //CP molr variables ! Used in cm_mtk_agps_request_cp_molr()
    mmi_ss_molr_begin_req_struct molr_info;
    mdi_gps_uart_work_mode_enum gps_mode;
    U16 req_id;
    void (*callback)(S32 type, void *buffer, U32 length);
}cp_molr_req_struct;*/

typedef struct{
    cm_agps_session_state_struct session_state;
    int ongoing_session;
    int supl_session_num;
    int NI_allowed;
    int MOLR_Type; //locationestimate(0), assistance data(1)
    int MOLR_positionType;//UP(0),CP(1),CPUP_UP_Pref(2),CPUP_CP_Pref(3)
    int external_address_enable;
    char external_address[32];
    int MLC_number_enable;
    char MLC_number[32];

    int CustomPolicy_Enable;
    int CustomPolicy_Type;
    int SUPL_SI_Req_Enable;
    int SUPL_Enable;
    int cur_SUPLSI_enabled;
    int cur_SUPL_enabled;
    int AGPS_Enable;
    int GPS_Status;
    int is_nilr_open_gps;
#if AGPS_CP_RESET_CMD_BY_PMTK106 != 0
    int is_CP_nilr_reset_gps;
#endif
    int is_open_gps_timeout;
    int log_file_max_num;
    int cm_open_gps_num;
    int sim1_status; //0= NOT_READY, 1=READY
    int sim2_status; //0= NOT_READY, 1=READY
    int simID_pref;
    int cur_molr_sim;
    int cur_mtlr_sim;
    //struct timeval lastclose;
    //struct timeval currentopen;
    time_t lastclose;
    time_t currentopen;
    int gps_reset_immediately;
    MMI_BOOL up_verify_timeout;
    MMI_BOOL is_waiting_GSP_status;
    int sim1_call_state;
    int sim2_call_state;
    int cur_data_conn_type;  // 1=sim1, 2= sim2, 3=wifi, others=no data connection
    int sim1_data_conn;
    int sim2_data_conn;
    int wifi_data_conn;

    int sim1_network_type ;   //0= 2G network, 1 = 3G network
    int sim2_network_type ;   //0= 2G network, 1 = 3G network
    MMI_BOOL recv_TTFF_fix;
    MMI_BOOL send_supl_end_msg;
    char nw_ip_addr[16];
    int sim1_isNetworkRoaming;
    int sim2_isNetworkRoaming;
    int roaming_enable; //0=only local network, 1= local+roam network
    int CA_enable;
    MMI_BOOL waiting_data_conn_after_call_end; //for 2G, during the call data_conn will disconnected, and when call end it will reconnected.
    int supl_end_session_num_for_log;
    int eCID_enable;
    int eCID_enable_last;
}cm_agps_up_cp_context_struct;
#endif
//C.K. Chiang <--

extern cm_agps_up_cp_context_struct cm_g_up_cp_ctx;
#define  CM_GET_UPCP_CTX()   (&cm_g_up_cp_ctx)


#define SS_ID_MAX_NUM 7   //In modem side, for ss modlue, there are no more than 7 (MO) and 7 (MT) at the same time !
typedef enum {
   SS_ID_MT_BASE = 0,
   SS_ID_MT_END = SS_ID_MT_BASE + SS_ID_MAX_NUM-1,       //MT: SS_ID_MT_BASE+0 <-->SS_ID_LCS_BASE+6

   //SS_ID_LCS_BASE = 16,
   //SS_ID_LCS_END = SS_ID_LCS_BASE + SS_ID_MAX_NUM-1,

   SS_ID_MO_BASE = 16,  //---LCS=AGPS_MO
   SS_ID_MO_END = SS_ID_MO_BASE + SS_ID_MAX_NUM-1,      //LCS: SS_ID_MO_BASE+0 <--> SS_ID_MO_BASE+6

   SS_ID_CISS_BASE = 32,
   SS_ID_CISS_END = SS_ID_CISS_BASE + SS_ID_MAX_NUM-1,  //CISS: SS_ID_CISS_BASE+0 <--> SS_ID_CISS_BASE+6

   SS_ID_INVALID = 255
} l4c_ss_id_enum;     // Defined by Tim Huang!! Example: Lcs_msg.ss_id = SS_ID_LCS_BASE + (lcs_app_count++)% SS_ID_MAX_NUM;


//------------------------------function---------------------------------------
void cm_notify_to_user(supl_mmi_notify_enum notify_type, kal_uint8 ss_id, void* arg);
void cm_msg_send_ext_queue(ilm_struct *ilm_ptr);

void send_pmtk(MTK_AGPS_SUPL_MSG_T type, char *Buf, unsigned int size);
int cm_UPCP_StateMachineLoop(int sessionID);
void cm_set_GpsAgent_Supl_State(int supl_enable, int supl_si_enable);
void cm_get_GpsAgent_Supl_State();

void cm_Emergency_Call_State(int status);
void cm_Sim_Status_Update(int sim_id, int sim_status);
void cm_Call_State_Update(int sim_id, int call_state);
void cm_Network_Type_Update(int sim_id, int network_type);
void cm_Network_Roaming_State_Update(int sim_id, int isRoaming);
void cm_nw_current_data_conn_update(int conn_type, int conn_state);

void cm_Location_Result_Update(agps_location_result_struct g_location);
void cm_nw_ipaddr_update(char *ipaddr);

void cm_GPS_State(int status);
void cm_Send_Open_GPS_State();
void cm_mtk_agps_create_major_thread(cm_agps_task_enum task_id);
//C.K. Chiang add--> For Permanent CP
void cm_mtk_agps_init_permanent(void);
void cm_mtk_agps_deinit_permanent(void);
//C.K. Chiang add<--

void cm_mtk_agps_init(const char *name_str, const char *addr_str, int port, int tls);
void cm_mtk_agps_deinit();
void cm_agps_set_cb(
    mtk_agps_ind_em_cb em_cb,
    mtk_agps_ind_info_cb info_cb,
    mtk_agps_ind_notify_cb notify_cb,
    mtk_agps_ind_error_cb error_cb,
    mtk_agps_ind_opengps_cb opengps_cb,
    mtk_agps_position_notify_cb pos_notify_cb);

//C.K. modified-->
void cm_mtk_agps_config(int hacc,int vacc, int mla, int delay,
    int smod, agps_supl_set_id_enum sid, unsigned char nt, unsigned char vt,int supl_captype,
    int ni_en, int motype, int add_en, char *add, int mlc_en, char *mlc,int policy_en,int policy,
    int supl_si_en,int supl_enable,int molrtype, int gpsStatus, int ni_iot, int logfile_maxnum,
    int sim_pref, int roam_enable, int ca, int ecid_en
    #ifdef __CDMA_AGPS_SUPPORT__
          ,int sim2_molrtype);
    #else
          );
    #endif
//C.K. modified<--
void cm_mtk_agps_response(int resp);
void cm_mtk_agps_update_cinfo(const char *imsi, const char *mccmnc, int lac, int cid);
void cm_mtk_agps_ni_req(char *data, int len);
void cm_mtk_agps_request_si(agps_supl_mode_enum mode);
void cm_mtk_agps_stop();
void cm_mtk_agps_stop_all();
void gps_agps_parser_for_cp(kal_uint16 cmd, kal_uint16 port, const kal_char *buffer, kal_uint32 length);
void cm_gps_agps_parser(kal_uint16 cmd, kal_uint16 port, const kal_char *buffer, kal_uint32 length);
void cm_agps_send_ccci_msg_2_L4C(int c_fd, mtk_ilm_struct *recv_msg);
void *cm_agps_pmtk_task(void *arg);
void *cm_agps_L4C_2_mmi_task(void *arg);
void *cm_agps_gpstask_2_mmi_task(void *arg);
void cm_ccci_and_mmi_msg_hdlr(ilm_struct *ilm_ptr);
void cm_agps_reset_gps();
char *cm_get_thread_name();

agps_ret_enum cm_agps_open_thd_socket(cm_agps_task_enum t, char *s);
agps_ret_enum cm_agps_close_thd_socket(cm_agps_task_enum t);

mtk_ilm_struct *cm_ilm_construct(void *buff, int data_size);
mtk_local_para_struct *cm_ilm_local_para_construct(void *buff, int offset, int data_size);
mtk_peer_buff_struct *cm_ilm_peer_buff_construct(void *buff, int offset,int data_size);

agps_notify_ret_enum cm_agps_wait_response();
void cm_mmi_gps_mgr_mmi_nw_attach_ind_hdlr(const char *imsi, const char *mccmnc, int lac, int cid);
void cm_agps_start_cp_molr_callback(S32 type, void *buffer, unsigned int length);
void cm_mtk_agps_request_cp_molr(agps_supl_mode_enum mode);
void cm_msg_send_ext_queue(ilm_struct *ilm_ptr);
void cm_agps_poll_register(struct pollfd *poll_fds, int* poll_cnt, int res_fd, short events);
//C.K. added--> Change to a function call with log
int CM_SENDTO(module_type t, ilm_struct msg, soc_struct soc);
//C.K. added<--

void cm_agps_poll_unregister(struct pollfd *poll_fds, int* poll_cnt, int res_fd);
int cm_agps_uart_data_handler(cm_agps_temp_pmtk_buff *dest, char *src, int src_size);
void cm_open_gps_time_out();
void set_cur_mtlr_session_simID(ilm_struct *ilm_ptr);
int get_cur_session_simID(ilm_struct *ilm_ptr);
int is_mnl_process_exit();



char* cm_get_thread_name();
#define CM_GET_THD_NAME()   (cm_get_thread_name())

//---------------------------------------------------------------------------

#ifdef __CDMA_AGPS_SUPPORT__
void cm_mtk_agps_set_cur_position_type();
#endif

#ifdef __cpluscplus
}
#endif

#endif



