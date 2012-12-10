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

#define DBG_LOG  1
#define DBG_MEM  1

#define ASCII_ONLY 1
#include <errno.h>
#include <stdlib.h>
#ifndef __WIN32SWIP__
#include <unistd.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <android/log.h>
#endif
#define LOG_TAG "MtkAgps"
#ifdef __WIN32SWIP__
#define _V(...)
#define _D(...)
#define _I(...)
#define _W(...)
#define _E(...)
#else

#if 0  //Baochu & Rex,2011/06/13, Seldom NE occurs in the place of log sentence. Change the following log function.
#if DBG_LOG
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
#else
#define _V(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define _D(...) __android_log_print(ANDROID_LOG_DEBUG,   LOG_TAG, __VA_ARGS__)
#define _I(...) __android_log_print(ANDROID_LOG_INFO,    LOG_TAG, __VA_ARGS__)
#define _W(...) __android_log_print(ANDROID_LOG_WARN,    LOG_TAG, __VA_ARGS__)
#define _E(...) __android_log_print(ANDROID_LOG_ERROR,   LOG_TAG, __VA_ARGS__)
#endif

#else 

#define _V(...)  mtk_agps_debug_printf(ANDROID_LOG_VERBOSE, __VA_ARGS__);
#define _D(...)  mtk_agps_debug_printf(ANDROID_LOG_DEBUG, __VA_ARGS__);
#define _I(...)  mtk_agps_debug_printf(ANDROID_LOG_INFO, __VA_ARGS__);
#define _W(...)  mtk_agps_debug_printf(ANDROID_LOG_WARN, __VA_ARGS__);
#define _E(...)  mtk_agps_debug_printf(ANDROID_LOG_ERROR, __VA_ARGS__);

#endif

#endif
#define DIR_AGPS_SUPL "/data/agps_supl"
#define DBG_AGPS_SUPL "/data/agps_supl/dbg_log"

#define AGPS_SUPL_MAX_DATA_ACCOUNT_LEN    32
#define AGPS_SUPL_MAX_PROFILE_NAME_LEN    32
#define AGPS_SUPL_MAX_PROFILE_ADDR_LEN    64
#define AGPS_SUPL_MAX_PROFILE_SMS_NUM_LEN 16
#define MTK_AGPS_PMTK_SZ                  256


typedef enum {
  AGPS_RET_TRUE = 0,
  AGPS_RET_SOCKET_INIT_FAIL,
  AGPS_RET_DBG_INIT_FAIL,
  AGPS_RET_CNT
} agps_ret_enum;

typedef enum
{
    AGPS_NOTIFY_RET_UNKNOWN,
    AGPS_NOTIFY_RET_DENY,
    AGPS_NOTIFY_RET_ALLOW,
    AGPS_NOTIFY_RET_NO_RESP,
    AGPS_NOTIFY_RET_CNT
} agps_notify_ret_enum, AGPS_NOTIFY_RESULT_ENUM;

typedef enum {
  AGPS_TASK_SUPL = 0,
  AGPS_TASK_RRLP,
  AGPS_TASK_ULCS,
  AGPS_TASK_GPS,
  AGPS_TASK_MMI,
  AGPS_TASK_SOC,
  AGPS_TASK_TLS,
  AGPS_TASK_PMTK,
  AGPS_TASK_CNT
} agps_task_enum;

typedef enum
{
    AGPS_SUPL_SET_ID_MSISDN = 0,
    AGPS_SUPL_SET_ID_IP_ADDR_IPv4,
    AGPS_SUPL_SET_ID_CNT
} agps_supl_set_id_enum;

typedef enum {
    AGPS_SUPL_MODE_SIMA,
    AGPS_SUPL_MODE_SIMB,
    AGPS_SUPL_MODE_CNT
} agps_supl_mode_enum;

typedef struct {
    int hor_acc;
    int ver_acc;
    int max_loc_age;
    int delay;
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
  char imsi[AGPS_SUPL_MAX_PROFILE_SMS_NUM_LEN]; // SIM card IMSI number string: 15-digit
} agps_supl_context_struct;

extern void mtk_agps_debug_printf(int prio, const char *fmt, ...);

#ifdef __cpluscplus
}
#endif

#endif
