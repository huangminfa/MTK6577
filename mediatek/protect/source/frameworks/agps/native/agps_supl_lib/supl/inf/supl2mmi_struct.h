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
 *  supl2mmi_struct.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This file contains SUPL task entry function.
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
 * Apr 18 2008 mtk00563
 * [MAUI_00759106] [AGPS] Check-in AGPS feature.
 * 
 *
 * Mar 18 2008 mtk00563
 * [MAUI_00733768] [AGPS] Revise for NI cases
 * 
 *
 * Feb 25 2008 mtk00563
 * [MAUI_00623349] [AGPS] AGPS feature check-in
 * 
 *
 * 
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/

#ifndef SUPL2MMI_STRUCT_H
#define SUPL2MMI_STRUCT_H

/* socket constants */
#include "soc_consts.h"

#include "supl2mmi_enums.h"

#define SUPL_MMI_MAX_REQ_LEN        50
#define SUPL_MMI_MAX_CLIENT_LEN     50
#define SUPL_MMI_MAX_ADDR_LEN       64
#define SUPL_MMI_MAX_PUSH_SIZE      1024
#define SUPL_MAX_SATELLITE_INFO     32


typedef struct
{
    supl_mmi_notify_enum    notify_type;
    supl_mmi_encoding_enum  encodeing_type;
    kal_char                req_id[SUPL_MMI_MAX_REQ_LEN];        /* requestor id */
    supl_mmi_id_enum        req_id_type;                        /* requestor id type */
    kal_char                client_name[SUPL_MMI_MAX_CLIENT_LEN];/* client name */
    supl_mmi_id_enum        client_name_type;                   /* client name type */
} supl_mmi_notify_struct;

typedef struct
{
    supl_mmi_result_enum    result;
    supl_mmi_cause_enum     cause;
} supl_mmi_notify_result_struct;

typedef struct
{
    kal_uint8   horacc;         /* horizontal accuracy */
    kal_bool    veracc_used;    /* if vertical accuracy exist */
    kal_uint8   veracc;         /* vertical accuracy */
    kal_bool    maxLocAge_used; /* if Maximun Location Age exist */
    kal_uint16  maxLocAge;      /* Maximun Location Age */
    kal_bool    delay_used;     /* if Delay exist */
    kal_uint8   delay;          /* Delay */
} supl_mmi_qop_struct;

typedef struct
{
    kal_uint8           semi_major;
    kal_uint8           semi_minor;
    kal_uint8           major_axis;
}supl_mmi_pos_uncert_struct;

typedef struct
{
    supl_mmi_direction_enum     direction;
    kal_uint16                  altitude;
    kal_uint8                   uncertainty;
} supl_mmi_alti_info_struct;

typedef struct
{
    supl_mmi_sign_enum          latitude_sign;
    kal_uint32                  latitude;
    kal_int32                   longtude;
    kal_bool                    uncertainty_used;
    supl_mmi_pos_uncert_struct  uncertainty;
    kal_bool                    confidence_used;
    kal_uint8                   confidence;
    kal_bool                    altitude_info_used;
    supl_mmi_alti_info_struct   altitude_info;
} supl_mmi_pos_estimate_struct;

typedef struct
{
    kal_uint8                   num_bits;
    kal_uint8                   data[1];
} supl_mmi_velocity_data_1_struct;

typedef struct
{
    kal_uint8                   num_bits;
    kal_uint8                   data[2];
} supl_mmi_velocity_data_2_struct;

typedef struct
{
   supl_mmi_velocity_data_2_struct  bearing;
   supl_mmi_velocity_data_2_struct  hor_speed;
} supl_mmi_h_velocity_struct;

typedef struct
{
    supl_mmi_velocity_data_1_struct ver_direction;
    supl_mmi_velocity_data_2_struct bearing;
    supl_mmi_velocity_data_2_struct hor_speed;
    supl_mmi_velocity_data_1_struct ver_speed;
} supl_mmi_hv_velocity_struct;

typedef struct
{
    supl_mmi_velocity_data_2_struct bearing;
    supl_mmi_velocity_data_2_struct hor_speed;
    supl_mmi_velocity_data_1_struct hor_speed_uncertainty;
} supl_mmi_h_velocity_uncert_struct;

typedef struct
{
    supl_mmi_velocity_data_1_struct ver_direction;
    supl_mmi_velocity_data_2_struct bearing;
    supl_mmi_velocity_data_2_struct hor_speed;
    supl_mmi_velocity_data_1_struct ver_speed;
    supl_mmi_velocity_data_1_struct hor_speed_uncertainty;
    supl_mmi_velocity_data_1_struct ver_speed_uncertainty;
} supl_mmi_hv_velocity_uncert_struct;

typedef union
{
    supl_mmi_h_velocity_struct          hor_velocity;
    supl_mmi_hv_velocity_struct         hv_velocity;
    supl_mmi_h_velocity_uncert_struct   hor_uncertainty;
    supl_mmi_hv_velocity_uncert_struct  hv_uncertainty;
} supl_mmi_velocity_union;

typedef struct
{
    supl_mmi_velocity_type_enum type;
    supl_mmi_velocity_union v;
} supl_mmi_velocity_struct;

typedef struct
{
    kal_uint32                      time_stamp;
    kal_uint16                      week;
    supl_mmi_pos_estimate_struct    pos_estimate;
    kal_bool                        velocity_used;
    supl_mmi_velocity_struct        velocity;
} supl_mmi_position_struct;

typedef struct
{
    kal_uint8           sat_id;
    kal_uint8           iODE;
} supl_mmi_satellite_info_struct;

typedef struct
{
    kal_uint16                      gps_week;
    kal_uint8                       gps_toe;
    kal_uint8                       nsat;
    kal_uint8                       toe_limit;
    kal_bool                        sat_info_used;
    supl_mmi_satellite_info_struct  sat_info[SUPL_MAX_SATELLITE_INFO];
} supl_mmi_navigation_model_struct;

typedef struct
{
    kal_bool                            almanac;
    kal_bool                            utc_model;
    kal_bool                            ionospheric_model;
    kal_bool                            dgps_correction;
    kal_bool                            ref_location;
    kal_bool                            ref_time;
    kal_bool                            acquisition_assist;
    kal_bool                            realtime_integrity;
    kal_bool                            navigation_model;
    supl_mmi_navigation_model_struct    nav_model_data;
} supl_mmi_req_assist_data_struct;

/* PRIMITIVES */

typedef struct
{
    kal_uint8           ref_count;
    kal_uint16          msg_len;
    kal_uint16          req_id;
    kal_uint16          len;
    kal_uint32          nwk_account_id;
    char                data[SUPL_MMI_MAX_PUSH_SIZE];
    kal_char            slp_addr[SUPL_MMI_MAX_ADDR_LEN];
    kal_uint16          slp_port;
} supl_mmi_push_req_struct;

typedef struct
{
    kal_uint8                   ref_count;
    kal_uint16                  msg_len;
    kal_uint32                  obj_valid;
    kal_uint16                  req_id;
    supl_mmi_status_enum        status;
    supl_mmi_result_enum        result;
    supl_mmi_cause_enum         cause;
    supl_mmi_pos_method_enum    pos_method;
    supl_mmi_slp_mode_enum      slp_mode;
    supl_mmi_qop_struct         qop;
    supl_mmi_notify_struct      notify;
    supl_mmi_position_struct    position;
} supl_mmi_status_ind_struct;

typedef struct
{
    kal_uint8                       ref_count;
    kal_uint16                      msg_len;
    kal_uint32                      obj_valid;
    kal_uint16                      req_id;
    kal_uint32                      nwk_account_id;
    supl_mmi_position_struct        pos;
    supl_mmi_req_assist_data_struct filter;
    supl_mmi_notify_result_struct   notify_rsp;
} supl_mmi_status_rsp_struct;

typedef struct
{
    kal_uint8                       ref_count;
    kal_uint16                      msg_len;
    kal_uint16                      req_id;
    kal_uint32                      nwk_account_id;
    supl_sim_id_enum                sim_id;
    supl_mmi_pos_method_enum        pos_method;
    supl_mmi_qop_struct             qop;
    supl_mmi_req_assist_data_struct filter;
    kal_char                        slp_addr[SUPL_MMI_MAX_ADDR_LEN];
    kal_uint16                      slp_port;
} supl_mmi_start_req_struct;

typedef struct
{
    kal_uint8                   ref_count;
    kal_uint16                  msg_len;
    kal_uint16                  req_id;
} supl_mmi_abort_req_struct;

typedef struct
{
    kal_uint8                   ref_count;
    kal_uint16                  msg_len;
    kal_uint16                  req_id;
} supl_mmi_abort_cnf_struct;

typedef struct
{
    kal_uint8               ref_count;
    kal_uint16              msg_len;
    kal_uint16              req_id;
    supl_mmi_notify_struct  notify;
} supl_mmi_notify_ind_struct;

typedef struct
{
    kal_uint8                   ref_count;
    kal_uint16                  msg_len;
    kal_uint16                  req_id;
    supl_mmi_notify_result_struct  notify_rsp;
} supl_mmi_notify_rsp_struct;


#endif /* SUPL2APP_STRUCT_H */

