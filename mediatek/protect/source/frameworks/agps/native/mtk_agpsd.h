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

#ifndef __MTK_AGPSD_H__
#define __MTK_AGPSD_H__

#ifdef __cplusplus
extern "C" {
#endif

#include "mtk_agps_def.h"

//////////////////////////////////////////////////////////////////////////
#define AGPSD_SOCKET_NAME    "agpsd"
#define AGPSD_MAX_POLL_CNT   4
#define AGPSD_MAX_SOC_CONN   4
#define AGPSD_TIMEOUT        600000 //600s

//!!== Chiwei: HDR_SZ shoule match the data written from framework ==
#define AGPSD_NI_DATA_SZ     256
#define AGPSD_HDR_SZ         20
#define AGPSD_BUF_SZ         32
#define AGPSD_STUFFING       0xFFFFFFFF
#define AGPSD_MAX_RETRY_CNT  10

//////////////////////////////////////////////////////////////////////////
typedef enum {
    AGPS_CMD_UNKNOWN = 0,
    AGPS_CMD_ENABLE,
    AGPS_CMD_DISABLE,
    AGPS_CMD_CONFIG,
    AGPS_CMD_RESP,
    AGPS_CMD_CNT,
    AGPS_CMD_INFORM = 10,
    AGPS_CMD_SIMA,
    AGPS_CMD_SIMB,
    AGPS_CMD_STOP_SI,
    AGPS_CMD_CELLINFO,
    AGPS_CMD_NI_REQ,
    AGPS_CMD_LOG_TO_FILE_ENABLE  = 16,
    AGPS_CMD_LOG_TO_FILE_DISABLE = 17,
    AGPS_CMD_LOG_TO_UART_ENABLE  = 18,
    AGPS_CMD_LOG_TO_UART_DISABLE = 19,
    AGPS_CMD_NI_ENABLE= 20,
    AGPS_CMD_EMERGENCY_CALL_DIALED=21,
    AGPS_CMD_EMERGENCY_CALL_ENDED=22,
    AGPS_CMD_GPS_OPEN =23,
    AGPS_CMD_GPS_CLOSE =24,
    AGPS_CMD_LOG_SUPL_TO_FILE_ENABLE=25,
    AGPS_CMD_LOG_SUPL_TO_FILE_DISABLE=26,
    AGPS_CMD_SIM_1_UPDATE=27,
    AGPS_CMD_SIM_2_UPDATE=28,
    AGPS_CMD_SIM_1_CALL_STATE_UPDATE=29,
    AGPS_CMD_SIM_2_CALL_STATE_UPDATE=30,
    AGPS_CMD_SIM_1_NW_TYPE_UPDATE=31,
    AGPS_CMD_SIM_2_NW_TYPE_UPDATE=32,
    AGPS_CMD_LOCATION_RESULT_UPDATE=33,
    AGPS_CMD_NW_IPADDR_UPDATE=34,
    AGPS_CMD_NW_SIM1_ROAMING_STATE_UPDATE=35,
    AGPS_CMD_NW_SIM2_ROAMING_STATE_UPDATE=36,
    AGPS_CMD_SIM1_DATA_CONN_UPDATE=37,
    AGPS_CMD_SIM2_DATA_CONN_UPDATE=38,
    AGPS_CMD_WIFI_DATA_CONN_UPDATE=39,
    //C.K. Chiang add--> For Permanent CP
    AGPS_CMD_DISABLE_NILR = 40,
    AGPS_CMD_ENABLE_NILR = 41,
    AGPS_CMD_RESET_AGPSD   = 42,
    AGPS_CMD_EXTRA   = 43,
    //C.K. Chiang add<--
    AGPS_CMD_UPDATE_CDMA_PROFILE        = 60,
} AGPS_CMD_E, agps_cmd_enum;

typedef enum {
    AGPS_IND_EM       = 1111,
    AGPS_IND_INFO     = 2222,
    AGPS_IND_NOTIFY   = 3333,
    AGPS_IND_ERROR    = 4444,
    AGPS_IND_OPENGPS  = 9000,
    AGPS_IND_CLOSEGPS = 9001,
    AGPS_IND_RESETGPS = 9002,
    AGPS_IND_POSITION_NOTIFY = 5555,
} AGPS_SUPL_IND_E, apgs_supl_ind_enum;

//////////////////////////////////////////////////////////////////////////
typedef struct {
    agps_cmd_enum cmd;
    int lac;
    int cid;
    int imsi_len;
    int mccmnc_len;
} AGPS_CELLINFO_HDR_T, agps_cellinfo_hdr_struct;

typedef struct {
    agps_cmd_enum cmd;
    int tls;
    int port;
    int name_len;
    int addr_len;
} AGPS_PROFILE_HDR_T, agps_profile_hdr_struct;

typedef struct {
    agps_cellinfo_hdr_struct hdr;
    //int cell_type;
    char imsi[AGPSD_BUF_SZ];
    char mccmnc[AGPSD_BUF_SZ];
} AGPS_CELLINFO_T, agps_cellinfo_struct;

typedef struct {
    agps_profile_hdr_struct hdr;
    char name[AGPSD_BUF_SZ];
    char addr[AGPSD_BUF_SZ];
} AGPS_PROFILE_T, agps_profile_struct;

typedef struct {
    agps_cmd_enum cmd;
    short si_mode;
    short set_id;
    short qop_hacc;
    short qop_vacc;
    short qop_mlage;
    short qop_delay;
    short tmr_notify;
    short tmr_verify;

    short NI_enable;
    //short MOLR_positionType;
    short external_address_enable;
    int MLC_number_enable;
    char external_address[AGPSD_BUF_SZ];
    char MLC_number[AGPSD_BUF_SZ];
    int SUPL_CapabilityType;
	int CustomPolicy_Enable;
	int CustomPolicy_Type;
	int SUPL_SI_Req_Enable;
	int SUPL_Enable;
    int MOLR_Type;
    int gpsStatus;
	int ni_iot;
	int log_file_max_num;
    int cp_gemini_sim_pref;
    int Network_local_or_roaming;
    int CA_enable;
    int eCID_enable;
    int simID_pref;
    int sim1_agps_protocol; // 0 up, 1 cp
    int sim2_agps_protocol; // 0 up, 1 cp
} AGPS_CONFIG_T, agps_config_struct;

typedef struct {
    short NI_enable;
    //short MOLR_positionType;
    short external_address_enable;
    int MLC_number_enable;
    int external_address_len;
    int MLC_number_len;
    int SUPL_CapabilityType;
	int CustomPolicy_Enable;
	int CustomPolicy_Type;
	int SUPL_SI_Req_Enable;
	int SUPL_Enable;
    int MOLR_Type;
    int gpsStatus;
	int ni_iot;
	int log_file_max_num;
    int cp_gemini_sim_pref;
    int Network_local_or_roaming;
    int CA_enable;
    int eCID_enable;
    int simID_pref;
    int sim1_agps_protocol; // 0 up, 1 cp
    int sim2_agps_protocol; // 0 up, 1 cp
} agps_config_extension;

typedef struct {
    int pde_port;
    int pde_url_vaild;
    int name_len;
    int mcp_addr_len;
    int pde_ip4_addr_len;
    int pde_ip6_addr_len;
    int pde_url_addr_len;
} agps_cdma_profile_extension;

typedef struct {
    agps_cmd_enum cmd;
    int field_1;
    int field_2;
    int field_3;
    int field_4;
} AGPS_COMMON_CMD_T, agps_common_cmd_struct;

//////////////////////////////////////////////////////////////////////////
extern agps_profile_struct  g_profile;
extern agps_cellinfo_struct g_cellinfo;
extern agps_config_struct   g_config;


#ifdef __cpluscplus
}
#endif

#endif
