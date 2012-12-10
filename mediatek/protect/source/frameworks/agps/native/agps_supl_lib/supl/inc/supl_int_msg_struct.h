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
 *  supl_int_msg_struct.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This file contains SUPL internal message structures.
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
 * 10 11 2011 archilis.wang
 * [ALPS00076515] [GIN-Dual-SIM][SUPL] Fail to generate default H-SLP address with SIM IMSI after TLS handshake failure.
 * .
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

#ifndef _SUPL_INT_MSG_STRUCT_H
#define _SUPL_INT_MSG_STRUCT_H



typedef enum
{
    SUPL_INT_RESULT_OK,
    SUPL_INT_RESULT_NETWORK_FAIL,
    SUPL_INT_RESULT_ULP_FAIL,
    SUPL_INT_RESULT_LCSP_FAIL,
    SUPL_INT_RESULT_GPS_FAIL,
    SUPL_INT_RESULT_END
} supl_int_result_enum;

typedef enum
{
    SUPL_INT_CAUSE_NONE,
    SUPL_INT_CAUSE_CLOSE_ALREADY,
    SUPL_INT_CAUSE_SOC_CREATE_FAIL,
    SUPL_INT_CAUSE_SOC_SET_OPT_FAIL,
    SUPL_INT_CAUSE_SOC_BIND_FAIL,
    SUPL_INT_CAUSE_SOC_CONNECT_FAIL,
    SUPL_INT_CAUSE_SOC_GET_DNS_FAIL,
    SUPL_INT_CAUSE_SOC_CONN_RESET,
    SUPL_INT_CAUSE_SOC_BEARER_FAIL,
    SUPL_INT_CAUSE_SOC_OTHERS,
    SUPL_INT_CAUSE_TLS_SHOTDOWNED,
    SUPL_INT_CAUSE_TLS_IO_ERROR,
    SUPL_INT_CAUSE_TLS_CREATE_FAIL,
    SUPL_INT_CAUSE_TLS_HANDSHAKE_FAIL,
    SUPL_INT_CAUSE_TLS_OTHERS,
    SUPL_INT_CAUSE_SEND_FAIL,
    SUPL_INT_CAUSE_TLS_INVALID_CERT,   // 2011/10/07: new error cause for ALPS00076515
    SUPL_INT_CAUSE_END
} supl_int_cause_enum;

typedef struct
{
    supl_int_result_enum    result;
    supl_int_cause_enum     cause;
} supl_int_result_struct;

typedef struct
{
    kal_uint8                   ref_count;
    kal_uint16                  msg_len;
    kal_uint16                  session_id;
    kal_uint8                   pad;
} supl_conn_create_req_struct;

typedef struct
{
    kal_uint8                   ref_count;
    kal_uint16                  msg_len;
    kal_uint16                  session_id;
    supl_int_result_struct      result;
} supl_conn_create_cnf_struct;

typedef struct
{
    kal_uint8                   ref_count;
    kal_uint16                  msg_len;
    kal_uint16                  session_id;
} supl_conn_send_req_struct;

typedef struct
{
    kal_uint8                   ref_count;
    kal_uint16                  msg_len;
    kal_uint16                  session_id;
    kal_uint32                  count;
    supl_int_result_struct      result;
} supl_conn_send_cnf_struct;

typedef struct
{
    kal_uint8                   ref_count;
    kal_uint16                  msg_len;
    kal_uint16                  session_id;
    kal_uint32                  buf_len;
} supl_conn_recv_ind_struct;

typedef struct
{
    kal_uint8                   ref_count;
    kal_uint16                  msg_len;
    kal_uint16                  session_id;
    supl_int_result_struct      result;
} supl_conn_fail_ind_struct;

typedef struct
{
    kal_uint8                   ref_count;
    kal_uint16                  msg_len;
    kal_uint16                  session_id;
    kal_bool                    wait_send;
} supl_conn_close_req_struct;

typedef struct
{
    kal_uint8                   ref_count;
    kal_uint16                  msg_len;
    kal_uint16                  session_id;
    supl_int_result_struct      result;
} supl_conn_close_cnf_struct;

#endif /* _SUPL_INT_MSG_STRUCT_H */



