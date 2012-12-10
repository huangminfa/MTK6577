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
 *  supl_ulp_hdlr.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This file contains function prototypes of supl_asn1.c.
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
 * Aug 24 2009 mtk00563
 * [MAUI_01943924] [AGPS] AGPS 2G control plane check-in
 * 
 *
 * Apr 18 2008 mtk00563
 * [MAUI_00759106] [AGPS] Check-in AGPS feature.
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
#ifndef _SUPL_ULP_HDLR_H
#define _SUPL_ULP_HDLR_H

#include "supl_ulp_def.h"

/* Decode result of SUPL ULP messages */

typedef struct
{
    kal_uint8 maj;
    kal_uint8 min;
    kal_uint8 servind;
}supl_version_info_struct;

typedef struct
{
    supl_version_info_struct    version;
    supl_set_session_id_struct  *set_session_id;
    supl_slp_session_id_struct  *slp_session_id;
} supl_ulp_header_info_struct;

typedef struct
{
    supl_ulp_header_info_struct header;
    supl_mmi_qop_struct         *qop;
    kal_char                    slp_addr[SUPL_MMI_MAX_ADDR_LEN];
    supl_mmi_slp_mode_enum      slp_mode;
    supl_mmi_pos_method_enum    pos_method;
    supl_mmi_notify_struct      *notify_info;
#if 0 //__ULP_VER_1_5__
    supl_supp_nwk_info_struct   *network_info;
    kal_bool                    *notify_by_location;
#endif
} supl_init_info_struct;

typedef struct
{
    supl_ulp_header_info_struct header;
    kal_uint16                  session_id;
    supl_mmi_pos_method_enum    pos_method;
    supl_location_id_struct     *loc_id;
    supl_mmi_qop_struct         *qop;
#if 0 //__ULP_VER_1_5__
    supl_location_info_struct   *loc_info;
#endif
} supl_start_info_struct;

typedef struct
{
    supl_ulp_header_info_struct header;
    supl_mmi_pos_method_enum    pos_method;
    /* reserved 
    kal_char                    slp_addr[MAX_SOC_ADDR_LEN];
    kal_char                    *set_auth_key;
    kal_char                    *key_id4;
    */
} supl_resp_info_struct;

typedef struct
{
    supl_ulp_header_info_struct     header;
    kal_uint16                      session_id;
    supl_mmi_pos_method_enum        pos_method;
    supl_mmi_req_assist_data_struct *filter;
    supl_location_id_struct         *loc_id;
    supl_mmi_position_struct        *pos;
//    kal_uint16                      week;
    kal_uint8                       *ver;
#if 0 //__ULP_VER_1_5__
    supl_location_info_struct       *loc_info;
#endif
} supl_pos_init_info_struct;

typedef struct
{
    supl_ulp_header_info_struct header;
    kal_uint16                  session_id;
    supl_lcsp_type_enum         payload_type;
    kal_uint16                  payload_len;
    kal_uint8                   *payload;
    supl_mmi_velocity_struct    *velocity;
} supl_pos_info_struct;

typedef struct
{
    supl_ulp_header_info_struct header;
    kal_uint8                   *ver;
    supl_ulp_status_enum        status_code;
    supl_mmi_position_struct    *position;
//    kal_uint16                  week;
} supl_end_info_struct;

#if 0 //__ULP_VER_1_5__

typedef struct
{
    kal_uint32                  num_of_fix;
    kal_uint32                  interval;
} supl_ulp_perd_trig_param_struct;

typedef struct
{
    supl_ulp_perd_trig_param_struct     *period_trig_param;
} supl_ulp_trig_param_struct;

typedef struct
{
    lcsp_location_id_struct      *loc_id;
    supl_location_info_struct    *loc_info;
    supl_mmi_qop_struct          *qop;
} supl_trig_start_info_struct;

typedef struct
{
    supl_mmi_pos_method_enum    pos_method;
    supl_ulp_trig_param_struct  trig_param;
    /* reserved 
    kal_char                    slp_addr[MAX_SOC_ADDR_LEN];
    kal_char                    *set_auth_key;
    kal_char                    *key_id4;
    */
    supl_supp_nwk_info_struct   network_info;
} supl_trig_resp_info_struct;

typedef struct
{
    kal_uint16                      num;
    supl_mmi_position_struct        *pos;
    supl_mmi_pos_method_enum        *pos_method;
    /* location id */
    kal_uint32                      *ret_code;
} supl_report_info_struct;

#endif

typedef union
{
    supl_init_info_struct       supl_init;
    supl_start_info_struct      supl_start;
    supl_resp_info_struct       supl_resp;
    supl_pos_init_info_struct   supl_pos_init;
    supl_pos_info_struct        supl_pos;
    supl_end_info_struct        supl_end;
#if 0 //__ULP_VER_1_5__
    supl_trig_start_info_struct supl_trig_start;
    supl_trig_resp_info_struct  supl_trig_resp;
    supl_report_info_struct     supl_report;
#endif
} supl_info_msg_union;

typedef struct
{
    supl_ulp_msg_type_enum  msg_type;
    supl_ulp_status_enum    status;
    supl_info_msg_union     msg;
} supl_info_struct;


extern kal_uint32 supl_ulp_decode(kal_uint8 *data, kal_uint32 length, supl_info_struct *supl_msg_info);
extern void supl_ulp_decode_free(supl_info_struct *supl_msg_info);
extern kal_bool supl_ulp_check_version(supl_info_struct *supl_info);
extern kal_bool supl_ulp_encode(kal_uint16 session_id, supl_info_struct *supl_msg_info, peer_buff_struct **data);//, kal_uint32 buf_len, kal_uint32 *ret_len);
#endif /* _SUPL_ULP_HDLR_H */

