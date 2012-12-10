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

/*****************************************************************************
*
* Filename:
* ---------
* cbm_api.h
*
* Project:
* --------
*   MAUI
*
* Description:
* ------------
*   This file contains the CBM APIs
*
* Author:
* -------
* Leona Chou
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
 * Jun 13 2008 mtk01890
 * [MAUI_00786908] [CBM 2.0] check in CBM 2.0 bearer mangement
 * 
*
*------------------------------------------------------------------------------
* Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
*============================================================================
****************************************************************************/
#ifndef _CBM_API_H
#define _CBM_API_H

#ifndef _SOC_CONSTS_H
#include "soc_consts.h"
#endif

/*************************************************************************
* Constant values
*************************************************************************/
#define CBM_INVALID_MOD_ID      (0)
#define CBM_INVALID_APP_ID      (0)
#define CBM_INVALID_NWK_ACCT_ID (0x3e)
#define CBM_ALL_NWK_ACCT_ID     (0x3f)
#define CBM_MAX_APP_ID          (0xff)
#define CBM_MAX_ACCT_NUM        (2)
#define CBM_ALL_QOS_ID          (0xff)
#define CBM_MAX_DL_FILTER_NUM  (8)

/*************************************************************************
* Enum values
*************************************************************************/
/* sim id */
typedef enum
{
    CBM_SIM_ID_SIM1,
    CBM_SIM_ID_SIM2,
    CBM_SIM_ID_TOTAL
} cbm_sim_id_enum;

/* Bearer state */
typedef enum
{
    CBM_DEACTIVATED             = 0x01,
    CBM_ACTIVATING              = 0x02,
    CBM_ACTIVATED               = 0x04,
    CBM_DEACTIVATING            = 0x08,
    CBM_CSD_AUTO_DISC_TIMEOUT   = 0x10,
    CBM_BEARER_NEED_FALLBACK    = 0x20,
    CBM_BEARER_STATE_TOTAL
} cbm_bearer_state_enum;

typedef enum
{
    CBM_ACCT_NONE,
    CBM_ACCT_FB_L1,
    CBM_ACCT_FB_L2,
    CBM_ACCT_ALWAYS_ASK,
    CBM_ACCT_TOTOAL
} cbm_acct_query_enum;

typedef enum
{
    CBM_APP_OPT_BEARER_FB_L1       = 0x01,
    CBM_APP_OPT_BEARER_FB_L2           = 0x02,
    CBM_APP_OPT_NON_AUTO_FB_L1   = 0x04,
    CBM_APP_OPT_TOTAL
} cbm_app_fallback_option_enum;

typedef enum
{
    CBM_BEARER_NONE,
    CBM_CSD = 0x01,
    CBM_BEARER_GSM_CSD = CBM_CSD,
    CBM_GPRS = 0x02,
    CBM_BEARER_GSM_GPRS = CBM_GPRS,
    CBM_PS  = CBM_GPRS,
    CBM_BEARER_PS = CBM_PS,
    CBM_WIFI = 0x04,
    CBM_BEARER_WIFI = CBM_WIFI,
    CBM_BEARER_ANY = 0xff
} cbm_bearer_enum;

typedef enum
{
    CBM_FB_NONE,
    CBM_FB_CSD_TO_CSD = 0x0001,
    CBM_FB_CSD_TO_GPRS = 0x0002,
    CBM_FB_CSD_TO_WIFI = 0x0004,
    CBM_FB_GPRS_TO_CSD = 0x0008,
    CBM_FB_GPRS_TO_GPRS = 0x0010,
    CBM_FB_GPRS_TO_WIFI = 0x0020,
    CBM_FB_WIFI_TO_CSD  = 0x0040,
    CBM_FB_WIFI_TO_GPRS = 0x0080,
    CBM_FB_ANY  = 0xffff
} cbm_fallback_enum;

typedef enum
{
    CBM_OK                  = 0,
    CBM_ERROR               = -1, /* error */
    CBM_WOULDBLOCK          = -2, /* would block */
    CBM_LIMIT_RESOURCE      = -3, /* limited resource */
    CBM_INVALID_ACCT_ID     = -4, /* invalid account id*/
    CBM_INVALID_AP_ID       = -5, /* invalid application id*/
    CBM_INVALID_SIM_ID      = -6, /* invalid SIM id */
    CBM_BEARER_FAIL         = -7, /* bearer fail */
    CBM_DHCP_ERROR          = -8, /* DHCP get IP error */
    CBM_CANCEL_ACT_BEARER   = -9 /* cancel the account query screen */
} cbm_result_error_enum;

/*************************************************************************
* Structures
*************************************************************************/
typedef struct
{
    kal_uint8   pfi;                  /* filter id */
    kal_uint8   epi;                  /* evaluation precedence */
    kal_uint8   protocol;             /* TCP, UDP, ... */
    kal_uint8   remote_addr[IP_ADDR_LEN]; 
    kal_uint8   remote_addr_prefix_len;     /* 0..32 */
    kal_uint16  remote_start_port;    /* inclusive */
    kal_uint16  remote_end_port;      /* inclusive */
    kal_uint16  local_start_port;
    kal_uint16  local_end_port;
    kal_uint32  ipsec_spi;            /* IPSec */
    kal_uint8   tos_mask;             /* set to 0 if not required to exam ToS */
    kal_uint8   tos;
    kal_uint32  flow_label;           /* IPv6 */
} cbm_dl_filter_struct  ;

typedef struct
{
    kal_uint8   qos_profile_id;
    kal_uint8   app_type;
    kal_uint16  bandwidth;
    kal_uint8   dl_filter_num;
    cbm_dl_filter_struct    dl_filter[CBM_MAX_DL_FILTER_NUM];
} cbm_qos_profile_struct;

typedef struct
{
    module_type mod_id;
    kal_uint8 app_id;
} cbm_app_struct;

typedef struct
{
    kal_bool                is_always_ask; /* is always_ask account or not */
    cbm_sim_id_enum         sim_id; /* sim card id */
    kal_uint8               account_id; /* real account id */
} cbm_acct_profile_struct;

typedef struct
{
    cbm_acct_profile_struct account[CBM_MAX_ACCT_NUM]; /* account profile */
    kal_uint8               acct_num; /* number of real accounts in 32-bits account id */
    kal_uint8               app_id; /* application identification */
} cbm_account_info_struct;

/*************************************************************************
* External APIs
*************************************************************************/
extern kal_uint8 cbm_register_app_id(kal_uint16 app_str_id, kal_uint16 app_icon_id);
extern kal_bool cbm_deregister_app_id(kal_uint8 app_id);

extern kal_uint32 cbm_encode_data_account_id(kal_uint32 acct_id, cbm_sim_id_enum sim_id, 
                                      kal_uint8 app_id, kal_bool always_ask);
extern kal_uint32 cbm_encode_app_id_data_account_id(kal_uint32 acct_id, kal_uint8 app_id);
extern kal_bool cbm_decode_data_account_id(kal_uint32 acct_id, 
                                           cbm_sim_id_enum *sim_id, 
                                           kal_uint8 *app_id, 
                                           kal_bool *always_ask, 
                                           kal_uint32* ori_acct_id);
extern kal_bool cbm_is_always_ask_data_account(kal_uint32 acct_id);
extern cbm_sim_id_enum cbm_get_sim_info_data_account(kal_uint32 acct_id);
extern kal_uint8 cbm_get_app_id_data_account(kal_uint32 acct_id);
extern kal_uint8 cbm_get_original_id_data_account(kal_uint32 acct_id);
extern kal_int8  cbm_reset_account_info(cbm_account_info_struct *acct_info);
extern kal_int8  cbm_encode_data_account_id_ext(kal_uint32 ori_acct_id, 
                                        kal_uint32 *encoded_acct_id, 
                                        cbm_account_info_struct *acct_info);
extern kal_int8  cbm_decode_data_account_id_ext(kal_uint32 acct_id, 
                                        cbm_account_info_struct *acct_info);
extern kal_int8  cbm_set_app_fallback_option(cbm_app_struct *app, kal_uint8 option);
extern kal_int8  cbm_get_app_fallback_option(cbm_app_struct *app, kal_uint8 *option);
extern kal_int8  cbm_set_app_always_ask_bearer(kal_uint8 app_id, cbm_bearer_enum bearer);
extern kal_int8  cbm_get_app_always_ask_bearer(kal_uint8 app_id, cbm_bearer_enum *bearer);
extern kal_int8  cbm_set_app_fallback_bearer(kal_uint8 app_id, cbm_fallback_enum fb_type);
extern kal_int8  cbm_get_app_fallback_bearer(kal_uint8 app_id, cbm_fallback_enum *fb_type);
extern kal_int8  cbm_register_app_always_ask_result(cbm_app_struct *app);
extern kal_int8  cbm_deregister_app_always_ask_result(cbm_app_struct *app);
extern kal_int8  cbm_set_bearer_info_event(module_type mod_id, kal_uint8 event);
extern kal_int8  cbm_get_bearer_info_event(module_type mod_id, kal_uint8 *event);
extern kal_int8  cbm_hold_bearer(module_type mod_id, kal_uint8 app_id);
extern kal_int8  cbm_release_bearer(module_type mod_id, kal_uint8 app_id);
extern kal_int8  cbm_open_bearer(module_type mod_id, kal_uint32 account_id);
extern kal_int32 cbm_get_bearer_status(module_type mod_id, kal_uint32 account_id);
extern kal_int8 cbm_get_bearer_type(module_type mod_id, kal_uint32 account_id,
                                    kal_uint8 *bearer_type);
#endif /* _CBM_API_H */

