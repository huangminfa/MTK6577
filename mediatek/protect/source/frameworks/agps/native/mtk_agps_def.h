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

#ifndef __MTK_AGPS_DEF_H__
#define __MTK_AGPS_DEF_H__

#ifdef __cplusplus
extern "C" {
#endif

#include <errno.h>
#include <stdlib.h>
#include <unistd.h>

#include <netinet/in.h>

#include <sys/socket.h>
#include <sys/un.h>

//#include <linux/mtk_agps_common.h>
#include <mtk_agps_common.h>

#include <android/log.h>
#define LOG_TAG "MtkAgps"
#define LOG_TAG_SUPL "MtkAgps_SUPL"

#if 0
#define _V(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__); \
                mtk_agps_debug_printf(ANDROID_LOG_VERBOSE, __VA_ARGS__)
#define _D(...) __android_log_print(ANDROID_LOG_DEBUG,   LOG_TAG, __VA_ARGS__); \
                mtk_agps_debug_printf(ANDROID_LOG_DEBUG, __VA_ARGS__)
#define _I(...) __android_log_print(ANDROID_LOG_INFO,    LOG_TAG, __VA_ARGS__); \
                mtk_agps_debug_printf(ANDROID_LOG_INFO, __VA_ARGS__)
#define _W(...) __android_log_print(ANDROID_LOG_WARN,    LOG_TAG, __VA_ARGS__); \
                mtk_agps_debug_printf(ANDROID_LOG_WARN, __VA_ARGS__)
#define _E(...) __android_log_print(ANDROID_LOG_ERROR,   LOG_TAG, __VA_ARGS__); \
                mtk_agps_debug_printf(ANDROID_LOG_ERROR, __VA_ARGS__)
//////////////////////////////////////////////////////////////////////////////////
//To record some critical log for SUPL processing.
//////////////////////////////////////////////////////////////////////////////////
#define _V_SUPL(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG_SUPL, __VA_ARGS__); \
                mtk_agps_debug_printf_SUPL(ANDROID_LOG_VERBOSE, __VA_ARGS__)
#define _D_SUPL(...) __android_log_print(ANDROID_LOG_DEBUG,   LOG_TAG_SUPL, __VA_ARGS__); \
                mtk_agps_debug_printf_SUPL(ANDROID_LOG_DEBUG, __VA_ARGS__)
#define _I_SUPL(...) __android_log_print(ANDROID_LOG_INFO,    LOG_TAG_SUPL, __VA_ARGS__); \
                mtk_agps_debug_printf_SUPL(ANDROID_LOG_INFO, __VA_ARGS__)
#define _W_SUPL(...) __android_log_print(ANDROID_LOG_WARN,    LOG_TAG_SUPL, __VA_ARGS__); \
                mtk_agps_debug_printf_SUPL(ANDROID_LOG_WARN, __VA_ARGS__)
#define _E_SUPL(...) __android_log_print(ANDROID_LOG_ERROR,   LOG_TAG_SUPL, __VA_ARGS__); \
                mtk_agps_debug_printf_SUPL(ANDROID_LOG_ERROR, __VA_ARGS__)
#else

#define _V(...)  mtk_agps_debug_printf(ANDROID_LOG_VERBOSE, __VA_ARGS__);
#define _D(...)  mtk_agps_debug_printf(ANDROID_LOG_DEBUG, __VA_ARGS__);
#define _I(...)  mtk_agps_debug_printf(ANDROID_LOG_INFO, __VA_ARGS__);
#define _W(...)  mtk_agps_debug_printf(ANDROID_LOG_WARN, __VA_ARGS__);
#define _E(...)  mtk_agps_debug_printf(ANDROID_LOG_ERROR, __VA_ARGS__);
//////////////////////////////////////////////////////////////////////////////////
//To record some critical log for SUPL processing.
//////////////////////////////////////////////////////////////////////////////////
#define _V_SUPL(...)  mtk_agps_debug_printf_SUPL(ANDROID_LOG_VERBOSE, __VA_ARGS__);
#define _D_SUPL(...)  mtk_agps_debug_printf_SUPL(ANDROID_LOG_DEBUG, __VA_ARGS__);
#define _I_SUPL(...)  mtk_agps_debug_printf_SUPL(ANDROID_LOG_INFO, __VA_ARGS__);
#define _W_SUPL(...)  mtk_agps_debug_printf_SUPL(ANDROID_LOG_WARN, __VA_ARGS__);
#define _E_SUPL(...)  mtk_agps_debug_printf_SUPL(ANDROID_LOG_ERROR, __VA_ARGS__);

#endif

#define SUPL_VERSION "1.0.0"
#define AGPS_IOT_NI   1
#define DEBUG_LOG     0
#define DEBUG_MEM     0
#define DEBUG_EXE     0

#define PMTK_ENC      1

#define DIR_AGPS_SUPL "/data/agps_supl"
//#define DBG_AGPS_SUPL "/data/agps_supl/dbg_log_file"

#define AGPS_SUPL_MAX_NOTIFY_INFO_LEN      256
#define AGPS_SUPL_MAX_DATA_ACCOUNT_LEN     32
#define AGPS_SUPL_MAX_PROFILE_NAME_LEN     32
#define AGPS_SUPL_MAX_PROFILE_ADDR_LEN     64
#define AGPS_SUPL_MAX_PROFILE_SMS_NUM_LEN  9
#define MTK_AGPS_PMTK_SZ                  256


//status update message
#define NEW_AGPS_SESSION_START   "0x00000000" //A new AGPS Session is started
#define AGPS_SESSION_END         "0x00000001" //An AGPS session has ended
#define SESSION_QOP              "0x00000002" //Session QoP (Single or Periodic)
#define POSITION_RESULT          "0x00000003" //Position Result
#define EPH_AIDING_TO_GPS        "0x00000004" //Eph Aiding Sent to GPS
#define AGPS_SW_VERSION          "0x00000005" //AGPS SW version numbers
#define NET_CONN_SUCCESS         "0x01000000" //Network connection success
#define SERVER_CONN_SUCCESS      "0x01000001" //Server connection success
#define SUPL_MSG_RECV            "0x02000000" //OTA message type
// Warning Message
#define NETWORK_UNAVAILABLE      "0x10000000" //SET-Initiated session,  no network connection,default to standalone.
#define UNKNOWN_SUPL_MSG_RECV    "0x10000000" //Unknown OTA message received,  discard.
#define UNKNOWN_GPS_MSG_RECV     "0x10000000" //Unknown GPS message received,  discard.
#define POSITION_TIME_OUT        "0x13000000" //GPS cannot produce a position within resp_time.
//error message
#define UNEXPECTED_SUPL_MSG_RECV "0x20000000" //Unexpected OTA message received. FSM out-of-sync.
#define SI_CONFIG_ERROR          "0x20000001" //SET-Initiated Session: wrong input configuration
#define NETWORK_CONN_FAIL        "0x21000000" //Network Connection Failure
#define SERVER_CONN_FAIL         "0x21000001" //Server connection failure
#define GPS_CLOSE_OR_RESET       "0x21000002" //Connection to GPS lost during AGPS session,  or at GPS reset.
#define TLS_CONN_FAIL            "0x21000003" //TLS Failure during SUPL
#define SUPL_END_ERRCODE         "0x22000000" //SUPL END Status Code (When error occurs)
#define SUPL_MSG_TIME_OUT        "0x22000001" //OTA Message Time-Out
#define SUPL_MSG_DECODE_ERR      "0x22000002" //OTA Message Decoding Error
#define GPS_MSG_TIME_OUT         "0x23000000" //GPS Message Time-Out

typedef enum {
    CM_AGPS_TASK_PMTK,
    CM_AGPS_TASK_L4C_2_MMI,
    CM_AGPS_TASK_GPSTASK_2_MMI,
    CM_AGPS_TASK_CNT
} cm_agps_task_enum; // task enum, also be used as ilm_ptr.src_mod_id !!

typedef enum {
  AGPS_RET_TRUE = 0,
  AGPS_RET_SOCKET_INIT_FAIL,
  AGPS_RET_DBG_INIT_FAIL,
  AGPS_RET_CNT
} agps_ret_enum;

typedef enum
{
    AGPS_NOTIFY_RET_NO_RESP,
    AGPS_NOTIFY_RET_ALLOW,
    AGPS_NOTIFY_RET_DENY,
    AGPS_NOTIFY_RET_CNT
} agps_notify_ret_enum, AGPS_NOTIFY_RESULT_ENUM;

typedef enum {
  AGPS_TASK_SUPL = 0,
  AGPS_TASK_RRLP,
  AGPS_TASK_ULCS, // add for 3G/UP
  AGPS_TASK_GPS,
  AGPS_TASK_MMI,
  AGPS_TASK_SOC,
  AGPS_TASK_TLS,
  //AGPS_TASK_PMTK,
#if (AGPS_IOT_NI)
  AGPS_TASK_UDPR,
#endif
  AGPS_TASK_CNT
} agps_task_enum;

typedef enum
{
    AGPS_SUPL_SET_ID_IMSI = 0,
    AGPS_SUPL_SET_ID_IP_ADDR_IPv4,
    AGPS_SUPL_SET_ID_CNT
} agps_supl_set_id_enum;

typedef enum {
    AGPS_SUPL_MODE_SIMA,
    AGPS_SUPL_MODE_SIMB,
    AGPS_SUPL_MODE_GLB,
    AGPS_SUPL_MODE_CNT
} agps_supl_mode_enum;

typedef enum
{
    AGPS_SUPL_EM_NONE = 0,
    AGPS_SUPL_EM_RECV_SI_REQ,
    AGPS_SUPL_EM_EM_POS_FIXED,
    AGPS_SUPL_EM_END
} agps_supl_em_enum;

typedef enum
{
    AGPS_SUPL_INFO_NONE = 0,
    AGPS_SUPL_INFO_END
} agps_supl_info_enum;

//!!== same with supl_mmi_notify_enum in supl2mmi_enum.h
typedef enum
{
    AGPS_SUPL_MMI_NOTIFY_NONE = 0,       /* No notification & no verification */
    AGPS_SUPL_NOTIFY_ONLY,               /* Notification Only */
    AGPS_SUPL_NOTIFY_ALLOW_NO_ANSWER,    /* Notification and verification, Allow on no answer. (If no answer is received from the SET User, the SET will assume that user consent has been granted and will proceed) */
    AGPS_SUPL_NOTIFY_DENY_NO_ANSWER,     /* Notification and verification, Deny on no answer. (If no answer is received from the SET User, the SET will assume that user consent has been denied and will abort) */
    AGPS_SUPL_NOTIFY_PRIVACY,            /* Privacy-Override. (Is used for preventing notification and verification without leaving any traces of a performed position fix or position fix attempt in terms of log files etc. on the SET). */
    AGPS_SUPL_NOTIFY_END
} agps_supl_notify_enum;

//!!== same with supl_mmi_cause_enum in supl2mmi_enum.h
typedef enum
{
    AGPS_SUPL_CAUSE_NONE = 0,
    AGPS_SUPL_CAUSE_NETWORK_CREATE_FAIL, /* Network Create Fail */
    AGPS_SUPL_CAUSE_BAD_PUSH_CONTENT,    /* Incorrect PUSH content */
    AGPS_SUPL_CAUSE_NOT_SUPPORTED,       /* Unsupported Operation */
    AGPS_SUPL_CAUSE_REQ_NOT_ACCEPTED,    /* Requeset Not Accepted */
    AGPS_SUPL_CAUSE_NO_RESOURCE,         /* No Resourcce to Handle new process */
    AGPS_SUPL_CAUSE_NETWORK_DISCONN,     /* Network Connection is Down */
    AGPS_SUPL_CAUSE_REMOTE_ABORT,        /* Remote Side Abort the Session (receive SUPL END) */
    AGPS_SUPL_CAUSE_TIMER_EXPIRY,        /* Expect Message not Back During a Specific Time Interval */
    AGPS_SUPL_CAUSE_REMOTE_MSG_ERROR,    /* Receive Incorrect Message Content */
    AGPS_SUPL_CAUSE_USER_AGREE,          /* User agree on confirmation */
    AGPS_SUPL_CAUSE_USER_DENY,           /* User deny on confirmation */
    AGPS_SUPL_CAUSE_NO_POSITION,         /* Only for NO Position */
    AGPS_SUPL_CAUSE_TLS_AUTH_FAIL,       /* TLS authentication fail */
    //Add by Baochu,2011/05/08
    AGPS_CP_UP_VERIFY_TIMEOUT,
    AGPS_MODEM_RESET_HAPPEN,
    AGPS_SUPL_CAUSE_END
} agps_supl_error_enum;

typedef struct {
    int hor_acc;
    int ver_acc;
    int max_loc_age;    /* NI use ;how long ago the his_pos is view as valid */
    int delay;          /* time limit between reciving all assistance data and fix*/
} agps_supl_qop_struct;

typedef struct {
    int mnc;
    int mcc;
    int lac;
    int cid;
    int typ;
} agps_supl_cell_id_struct;

typedef struct
{
    unsigned int slp_port;
    char profile_name[AGPS_SUPL_MAX_PROFILE_NAME_LEN];
    char slp_addr[AGPS_SUPL_MAX_PROFILE_ADDR_LEN];
    //char data_account[AGPS_SUPL_MAX_DATA_ACCOUNT_LEN];
} agps_supl_profile_struct;

typedef struct
{
  unsigned char u1GpsPort;                 // 0 : disable
  unsigned char fgUseTLS;                  // 0 : disable, 1 : enable
  unsigned char u1NotifyTime;              // 0 : no notification
  unsigned char u1VerifyTime;              // 0 : no verification
  agps_supl_mode_enum eSuplMode;
  agps_supl_set_id_enum eSetIdType;        // only support SUPL_SET_ID_IMSI and SUPL_SET_ID_IP_ADDR_IPv4
  agps_supl_qop_struct  rQop;
  agps_supl_profile_struct rProfile;
  agps_supl_cell_id_struct rCellId;
  char imsi[9]; // SIM card IMSI number string: 15-digit but stored in 9 bytes array.
  int agps_ni_iot_support; //==Baochu, 0:disable NI IOT; 1: enalbe NI IOT.
  int eCID_enable;
} agps_supl_context_struct;

typedef struct
{
    //agps_supl_notify_enum notify_type;
    signed char requestor_id[AGPS_SUPL_MAX_NOTIFY_INFO_LEN];
    signed char client_name[AGPS_SUPL_MAX_NOTIFY_INFO_LEN];
} agps_supl_notify_info_struct;

typedef struct {
    double mlatitude;
    double mlongitude;
    double maltitude;
    long long mtimestamp;//Note: long JAVA(64 bit)
    float mspeed;
    float mbearing;
    float maccuracy;
    int mTTFF;
} AGPS_LOCATION_RESULT_T, agps_location_result_struct;

typedef void (*mtk_agps_ind_em_cb)(agps_supl_em_enum em_enum, void *arg);
typedef void (*mtk_agps_ind_info_cb)(agps_supl_info_enum info_enum, void *arg);
typedef void (*mtk_agps_ind_notify_cb)(agps_supl_notify_enum notify_enum, void *arg);
typedef void (*mtk_agps_ind_error_cb)(agps_supl_error_enum error_enum, void *arg);
typedef void (*mtk_agps_ind_opengps_cb)(int opengps, void *arg);
typedef void (*mtk_agps_position_notify_cb)(float lat, float lon);

//!!== functions defined in the agps_supl_lib ==

extern void cm_agps_set_cb(
    mtk_agps_ind_em_cb em_cb,
    mtk_agps_ind_info_cb info_cb,
    mtk_agps_ind_notify_cb notify_cb,
    mtk_agps_ind_error_cb error_cb,
    mtk_agps_ind_opengps_cb opengps_cb,
    mtk_agps_position_notify_cb pos_notify_cb);

extern void cm_mtk_agps_init(const char *name_str, const char *addr_str, int port, int tls);
extern void cm_mtk_agps_deinit();
extern void cm_mtk_agps_update_cinfo(const char *imsi, const char *mccmnc, int lac, int cid);
extern void cm_mtk_agps_config(int hacc,int vacc, int mla, int delay,
    int smod, agps_supl_set_id_enum sid, unsigned char nt, unsigned char vt,int supl_captype,
    int ni_en, int motype, int add_en, char *add, int mlc_en, char *mlc,int policy_en,int policy,
    int supl_si_en,int supl_enable,int molrtype,int gpsStatus, int ni_iot, int logfile_maxnum,
    int sim_pref, int roam_enable, int ca, int ecid_en
    #ifdef __CDMA_AGPS_SUPPORT__
          ,int sim2_molrtype);
    #else
          );
    #endif
extern void cm_mtk_agps_response(int resp);
extern void cm_mtk_agps_request_si(agps_supl_mode_enum mode);
extern void cm_mtk_agps_stop();
extern void cm_mtk_agps_ni_req(char *data, int len);

extern void mtk_agps_debug_printf(int prio, const char *fmt, ...);
extern void mtk_agps_debug_printf_SUPL(int prio, const char *fmt, ...);
extern void create_log_file(int type);
extern void destroy_log_file(int type);

extern void cm_Emergency_Call_State(int status);
extern void cm_GPS_State(int status);
extern void cm_Send_Open_GPS_State();
extern void cm_Sim_Status_Update(int sim_id, int sim_status);
extern void cm_Call_State_Update(int sim_id, int call_state);
extern void cm_Network_Type_Update(int sim_id, int network_type);
extern void cm_Network_Roaming_State_Update(int sim_id, int isRoaming);
extern void cm_nw_current_data_conn_update(int conn_type, int conn_state);
extern void cm_mtk_agps_create_major_thread(cm_agps_task_enum task_id);
//C.K. Chiang add--> For Permanent CP
extern void cm_mtk_agps_init_permanent(void);
//C.K. Chiang add<--

extern void cm_Location_Result_Update(agps_location_result_struct g_location);
extern void cm_nw_ipaddr_update(char *ipaddr);

typedef enum
{
    REJECT_NEW,
    ACCEPT_AND_QUEUE_NEW,
    ACCEPT_NEW_AND_ABORT_CURRENT,
} agps_statemachine_result_enum;

#ifdef __cpluscplus
}
#endif

#endif
