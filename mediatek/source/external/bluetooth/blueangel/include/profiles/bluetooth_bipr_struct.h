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

/*****************************************************************************
 *
 * Filename:
 * ---------
 *   bluetooth_bipr_struct.h
 *
 * Project:
 * --------
 *   Maui_Software
 *
 * Description:
 * ------------
 *   This file is defines SAP for MTK Bluetooth.
 *
 * Author:
 * -------
 *   Zhigang Yu
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Log$ 
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef __BLUETOOTH_BIPR_STRUCT_H_
#define __BLUETOOTH_BIPR_STRUCT_H_

#include "bt_types.h"

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 req_id;
    kal_uint8 bip_service_set;
} bt_bip_activate_req_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 req_id;
    kal_uint8 cnf_code;
} bt_bip_activate_cnf_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 req_id;
    kal_uint8 bip_service_set;
} bt_bip_deactivate_req_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 req_id;
    kal_uint8 cnf_code;
} bt_bip_deactivate_cnf_struct;


typedef struct
{
    kal_uint8               ref_count;
    kal_uint16              msg_len;
    bt_bip_bd_addr_struct   bd_addr;
    kal_uint8               dev_name[BT_BIP_MAX_DEV_NAME_LEN];
} bt_bip_authorize_ind_struct;


typedef struct
{
    kal_uint8               ref_count;
    kal_uint16              msg_len;
    kal_uint8               cnf_code;
} bt_bip_authorize_rsp_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    bt_bip_bd_addr_struct bd_addr;
    kal_uint8 dev_name[BT_BIP_MAX_DEV_NAME_LEN];
} bt_bip_connect_ind_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint8 cnf_code;
} bt_bip_connect_rsp_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
} bt_bip_get_capabilities_ind_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint8 cnf_code;

    bt_bip_img_format_enum supported_img_formats[BT_BIP_MAX_IMG_FORMATS];
    bt_bip_pixel_struct supported_img_size[BT_BIP_MAX_IMG_FORMATS];
    U32 supported_maxsize[BT_BIP_MAX_IMG_FORMATS];
    
    bt_bip_img_format_enum preferred_format;
    bt_bip_pixel_struct preferred_pixel;
    U32 preferred_maxsize;
    U32 preferred_trans;

    U8 created_time_filter;
    U8 modified_time_filter;
    U8 encoding_filter;
    U8 pixel_filter;
} bt_bip_get_capabilities_rsp_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint8 img_name[BT_BIP_MAX_IMG_NAME_LEN];
    kal_uint32 img_size;
    bt_bip_img_desc_struct img_descriptor;
    U8 r_last_pkt;
} bt_bip_put_img_ind_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint16 img_path[BT_BIP_MAX_PATH_LEN];   /* checked done */
    kal_uint8 img_handle[BT_BIP_IMG_HANDLE_LEN];
    kal_uint8 cnf_code;
} bt_bip_put_img_rsp_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint8 img_handle[BT_BIP_IMG_HANDLE_LEN];
    U8 r_last_pkt;
} bt_bip_put_linked_thumbnail_ind_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint16 img_path[BT_BIP_MAX_PATH_LEN];   /* checked done */
    kal_uint8 cnf_code;
} bt_bip_put_linked_thumbnail_rsp_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    bt_bip_img_info_struct img_list_descriptor;
    kal_uint16 max_img_handle_number;
    kal_uint16 start_index;
    kal_bool latest_captured;
} bt_bip_get_img_list_ind_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint16 img_list_path[BT_BIP_MAX_PATH_LEN];
    kal_uint16 img_count;
    kal_uint8 cnf_code;
} bt_bip_get_img_list_rsp_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint8 img_handle[BT_BIP_IMG_HANDLE_LEN];
} bt_bip_get_img_prop_ind_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint8 img_handle[BT_BIP_IMG_HANDLE_LEN];
    bt_bip_img_info_struct native_img;
    bt_bip_img_info_struct *variant_img_p;
    bt_bip_img_info_struct *attachment_p;
    kal_uint8 cnf_code;
} bt_bip_get_img_prop_rsp_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint8 img_handle[BT_BIP_IMG_HANDLE_LEN];
    bt_bip_img_info_struct img_descriptor;
} bt_bip_get_img_ind_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint16 img_path[BT_BIP_MAX_PATH_LEN];
    kal_uint8 img_handle[BT_BIP_IMG_HANDLE_LEN];
    kal_uint8 cnf_code;
} bt_bip_get_img_rsp_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint8 img_handle[BT_BIP_IMG_HANDLE_LEN];
} bt_bip_get_linked_thumbnail_ind_struct;

/*
 * typedef struct
 * {
 * kal_uint8       ref_count;
 * kal_uint16      msg_len;
 * kal_uint32      cm_conn_id;
 * kal_uint8       cnf_code;
 * kal_uint16      thumbnail_path[BT_BIP_MAX_PATH_LEN];
 * } bt_bip_get_linked_thumbnail_rsp_struct;
 */

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint8 store_flag;
} bt_bip_get_monitoring_img_ind_struct;

/*
 * typedef struct
 * {
 * kal_uint8       ref_count;
 * kal_uint16      msg_len;
 * kal_uint32      cm_conn_id;
 * kal_uint8       cnf_code;
 * kal_uint16      img_path[BT_BIP_MAX_PATH_LEN];
 * kal_uint8               img_handle[BT_BIP_IMG_HANDLE_LEN];
 * } bt_bip_get_monitoring_img_rsp_struct;
 */

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint8 ind_code;
} bt_bip_abort_ind_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
} bt_bip_complete_ind_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint32 obj_len;
    kal_uint32 data_len;
    U8  r_last_pkt;
} bt_bip_continue_ind_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    kal_uint8 cnf_code;
} bt_bip_continue_rsp_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 req_id;
    kal_uint32 cm_conn_id;
    U8 disconnect_tp_directly;
    bt_bip_session_role_enum session_role;
} bt_bip_disconnect_req_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint32 cm_conn_id;
    bt_bip_session_role_enum session_role;
} bt_bip_disconnect_ind_struct;

typedef struct
{
    kal_uint8 img_handle[BT_BIP_IMG_HANDLE_LEN];
    kal_uint8 created[BT_BIP_MAX_TIME_LEN];     /* (YYYYMMDDTHHMMSS)(Z) */
    kal_uint8 modified[BT_BIP_MAX_TIME_LEN];    /* (YYYYMMDDTHHMMSS)(Z) */
} bt_bip_img_min_info_struct;

typedef bt_bip_get_img_rsp_struct bt_bip_get_linked_thumbnail_rsp_struct;
typedef bt_bip_get_img_rsp_struct bt_bip_get_monitoring_img_rsp_struct;

typedef struct
{
    U8 ref_count;
    U16 msg_len;
    U32 opcode;
    void* handle;
} bt_bip_cmd_agent_struct;


#endif /* __BLUETOOTH_BIPR_STRUCT_H_ */ 

