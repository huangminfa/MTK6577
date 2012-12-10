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
 *  supl_struct.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This file contains SUPL structures.
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
 * 03 30 2012 archilis.wang
 * [ALPS00254052] [Need Patch] [Volunteer Patch] Add the code for eCID
 * .
 *
 * 03 17 2012 archilis.wang
 * [ALPS00254052] [Need Patch] [Volunteer Patch] Add the code for eCID
 * .
 *
 * 10 05 2011 archilis.wang
 * [ALPS00076477] [GIN-Dual-SIM][SUPL] "requestAssistData" within SUPL POS INIT message missing when special cases run continuously.
 * .
 *
 * 12 10 2010 jinghan.wang
 * [MAUI_02845983] [AGPS] [Gemini] Support Gemini for RRLP & SUPL
 * .
 *
 * 10 27 2010 jinghan.wang
 * [MAUI_02830231] [AGPS] [SUPL] ULP messsage should report UCID(28 bits) instead of CID (16 bits) on 3G network
 * .
 *
 * Aug 24 2009 mtk00563
 * [MAUI_01943924] [AGPS] AGPS 2G control plane check-in
 * 
 *
 * May 13 2008 mtk00563
 * [MAUI_00770944] [SIM] SIM_READY_IND generic interface when sim is initialized
 * 
 *
 * Apr 18 2008 mtk00563
 * [MAUI_00759106] [AGPS] Check-in AGPS feature.
 * 
 *
 * Mar 18 2008 mtk00563
 * [MAUI_00733768] [AGPS] Revise for NI cases
 * 
 *
 * Mar 10 2008 mtk00563
 * [MAUI_00729219] [AGPS] OMA ETS Session ID fail
 * 
 *
 * Mar 1 2008 mtk00563
 * [MAUI_00726123] [AGPS] SLP session id error
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

#ifndef _SUPL_STRUCT_H
#define _SUPL_STRUCT_H

/* socket constants */
#include "soc_consts.h"
#include "supl2mmi_enums.h"
//#include "supl2lcsp_enums.h"
#include "supl2mmi_struct.h"
//#include "supl2lcsp_struct.h"
#include "lcsp2app_struct.h"

#if _FP_CR_SYNC_MAUI_03018028_ != 0
    #include "as2l4c_struct.h"
#endif

#ifndef GEN_FOR_PC
#include <setjmp.h>
#endif /* GEN_FOR_PC */

typedef struct
{
    kal_uint32      length;
    kal_uint8       *data_ptr;
} supl_raw_data_struct;

#ifndef __AGPS_SWIP_REL__
typedef struct
{
    kal_uint8               is_used;                        /* if the timer slot is in used */
    kal_uint8               timer_id;                       /* id of the timer */
    kal_uint16              session_id;                     /* session id */
    eventid                 event_id;                       /* ID from event scheduler */
} supl_timer_info_struct;

typedef struct
{
    event_scheduler         *evsh_sched;                    /* Event scheduler */
    stack_timer_struct      stack_timer;                    /* Stack timer */
    supl_timer_info_struct  timer_pool[SUPL_TIMER_TOTAL];
}supl_timer_struct;
#endif /* __AGPS_SWIP_REL__ */

typedef struct
{
    kal_int8                    soc_id;
    kal_int8                    tls_cntx_id;
    kal_bool                    create_tls;
    kal_bool                    invalid_cert;
    kal_int32                   request_id;
    kal_uint32                  nwk_account_id;
    kal_char                    slp_addr[SUPL_MMI_MAX_ADDR_LEN];
    kal_uint8                   slp_ip_addr[IP_ADDR_LEN];
    kal_uint16                  slp_port;
    kal_uint8                   *recv_buffer_p;
    kal_uint8                   *send_buffer_p;             /* send buffer pointer (pdu_ptr) */
    supl_conn_state_enum        conn_state;
    supl_conn_send_cb           send_cb;
    kal_uint32                  start_time_stamp;
} supl_conn_struct;

#if 0 //__ULP_VER_1_5__

    typedef struct
    {
        kal_uint32 supp_nwk_type;   /* network support information */
        kal_uint32 supp_mea_info;   /* measure support information */
        /* reserved
        kal_uint32 WLAN_info;       // WLAN info
        kal_uint32 WCDMA_info;      // WCDMA info
        */
    } supl_supp_nwk_info_struct;

    typedef struct
    {
        kal_uint16                  num_of_record;
        lcsp_location_id_struct     location_id[LCSP_MAX_RECORD_NUM];
        kal_uint16                  time_stamp[LCSP_MAX_RECORD_NUM];
        kal_bool                    serving_cell_flag[LCSP_MAX_RECORD_NUM];
    } supl_location_info_struct;
#endif

typedef struct
{
    kal_uint16          session_id;
    supl_set_id_enum    set_id_type;
    kal_uint32          set_id_len;
    kal_char            *set_id;
}supl_set_session_id_struct;

typedef struct
{
    kal_uint8           len;
    kal_uint8           slp_id_len;
    kal_char            session_id[4];
    supl_slp_id_enum    slp_id_type;
    kal_char            *slp_id;
} supl_slp_session_id_struct;


typedef struct
{
    kal_uint16                      id;
    kal_uint16                      req_id;
    kal_uint16                      app_id;
    kal_bool                        is_used;
    kal_bool                        in_queue;
    kal_uint8                       ver[8];
    kal_uint32                      supl_end_status;            /* supl_ulp_status_enum*/
    supl_close_reason_enum          close_reason;
    supl_session_type_enum          session_type;
    supl_session_state_enum         original_state;
    supl_session_state_enum         session_state;
    supl_session_state_enum         next_state;

#if _FP_CR_SYNC_MAUI_02938153__FSM != 0
    supl_ulp_msg_type_enum          to_send_msg;
#endif

    supl_mmi_pos_method_enum        pos_method;
    supl_mmi_slp_mode_enum          slp_mode;
    supl_lcsp_type_enum             lcsp_type;
    supl_set_session_id_struct      set_session_id;
    supl_slp_session_id_struct      *slp_session_id;
    supl_conn_struct                conn;
    supl_mmi_qop_struct             qop;
    supl_mmi_position_struct        *position;
    kal_uint16                      pos_week;
//    kal_bool                        pos;
    supl_mmi_req_assist_data_struct *filter;
    supl_mmi_notify_struct          *notify_info;
#if 0 //__ULP_VER_1_5__
    kal_uint16                      trig_interval;
    kal_uint16                      num_trigger;
    kal_bool                        notify_by_location;
    supl_location_info_struct       loc_info;
    supl_supp_nwk_info_struct       *supp_nwk_info;
#endif
} supl_session_struct;

typedef struct
{
    kal_uint8   ver_maj;
    kal_uint8   ver_tech;
    kal_uint8   ver_edit;
} supl_lcsp_version_struct;

typedef struct
{
    kal_uint16      session_id;
    supl_event_enum event;
    void            *param;
} supl_session_queue_struct;

typedef struct
{
    kal_uint16              arfcn;
    kal_uint8               bsic;
    kal_uint8               rx_lev;
} supl_nmr_element_struct;

typedef struct
{
    kal_uint8               num;
    supl_nmr_element_struct element[SUPL_MAX_NMR_NUM];
} supl_nmr_struct;

typedef struct
{
    kal_uint16          mcc;
    kal_uint16          mnc;
    kal_uint16          lac;
    kal_uint16          ci;
    kal_bool            nmr_used;
    lcsp_nmr_struct     nmr;
    kal_bool            ta_used;
    kal_uint8           ta;
} supl_gsm_cell_info_struct;

typedef struct
{
    kal_uint16          mcc;
    kal_uint16          mnc;
    kal_uint32          ucid;
} supl_wcdma_cell_info_struct;

typedef struct
{
    supl_cell_info_type_enum    cell_type;
    supl_cell_status_enum       cell_status;
    union
    {
      supl_gsm_cell_info_struct     gsm_cell_info;
      supl_wcdma_cell_info_struct   wcdma_cell_info;
    }
    cell_info;
  #if _FP_CR_SYNC_MAUI_03018028_ != 0
    supl_ecell_info_type_enum    ecell_type;
    union
    {
        gas_nbr_cell_info_struct    gsm_ecell;
      //#ifdef __UMTS_RAT__
        uas_nbr_cell_info_struct    wcdma_ecell;       
      //#endif
    }
    ecell_info;
  #endif
} supl_location_id_struct;

typedef struct
{
#if 0  // due to removed in FP for [MAUI_02918147] [AGPS][SUPL] SUPL refactoring: remove unused code
    kal_uint8                   max_session_num;
#endif
    kal_uint8                   num_session_in_queue;
//  kal_uint8                   pos_method_idx;          // not useful after we remove some code for ALPS00076477
    supl_sim_id_enum            sim_id;
#ifdef __GEMINI__
    kal_uint8                   imsi[9];
    kal_uint8                   imsi_2[9];
#else
    kal_uint8                   imsi[9];
#endif
    kal_uint16                  set_session_id;
    kal_uint16                  lcsp_session_id;
#ifndef __AGPS_SWIP_REL__
    KAL_ADM_ID                  mem_pool_id;
    supl_timer_struct           supl_timer;
#endif /* __AGPS_SWIP_REL__ */
    supl_session_struct         session[SUPL_MAX_SESSION_NUM];
    supl_session_queue_struct   session_queue[SUPL_MAX_SESSION_Q_NUM];

    /* support fragmented SUPL message */
    kal_uint8                   *temp_fragmented_data;
    kal_uint16                   total_composed_data_size;
    kal_uint16                   expected_composed_data_size;
#if 0 //__ULP_VER_1_5__
    gas_nbr_cell_info_struct    *cell_info_Q_head;
    gas_nbr_cell_info_struct    *cell_info_Q_tail;
    gas_nbr_cell_info_struct    cell_info_Q[SUPL_CELL_INFO_Q_SIZE];
#else

#ifdef __GEMINI__
    supl_location_id_struct     loc_id[2];
#else
    supl_location_id_struct     loc_id;
#endif

#endif
   jmp_buf                      jump_buffer;
#if 0 //__ULP_VER_1_5__
    supl_lcsp_version_struct    rrlp_version;
#endif

#if _FP_CR_SYNC_MAUI_02938153_ != 0 && _FP_CR_SYNC_MAUI_03018028__SP3 == 0
    kal_bool                    loc_ready_from_sim[SUPL_SIM_SOURCE_NUM];
#endif

#if _FP_CR_SYNC_MAUI_02938153__FSM != 0 && _FP_CR_SYNC_MAUI_03018028__SP3 == 0
    kal_uint8                   req_cnt_from_sim[SUPL_SIM_SOURCE_NUM];
#endif

#if _FP_CR_SYNC_MAUI_03018028__SP3 != 0
    kal_uint8                   ecell_info_req_cnt;
    kal_bool                    ecell_info_valid;
#endif

} supl_context_struct;

extern supl_context_struct      *supl_p;

#endif /* _SUPL_STRUCT_H */


