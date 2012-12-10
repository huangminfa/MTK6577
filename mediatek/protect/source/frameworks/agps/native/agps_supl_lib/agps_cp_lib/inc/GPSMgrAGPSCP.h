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

/*******************************************************************************
 * Filename:
 * ---------
 * GPSMgrAGPSCP.h
 *
 * Project:
 * --------
 *   
 *
 * Description:
 * ------------
 * GPS manager for AGPS control plane header file  
 *
 * Author:
 * -------
 * Sha Tao
 *
 *==============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *------------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * 11 17 2010 ham.wang
 * [MAUI_02641121] [Camera]fix maui build error of check RHR to it
 * .
 *
 * 11 11 2010 ham.wang
 * [MAUI_02640421] [RHR]check in
 * .
 *
 * 10 26 2010 ham.wang
 * [MAUI_02831307] [AGPS][DoCoMo pre-IOT] Cannot send any CP MOLR after the first one
 * .
 *
 * 08 16 2010 ham.wang
 * [MAUI_02603119] [3G AGPS CP] Spirent GCF test case fail - 17.2.4	Assisted GPS MT Tests
 * .
 *
 * Dec 21 2009 mtk80018
 * [MAUI_02019826] [FTA][AGPS] Fatal Error lr = 0xf1d043e4 - MM
 * 
 *
 * Sep 9 2009 mtk80068
 * [MAUI_01949469] [AGPS][CP] shall display operator number 0123456
 * 
 *
 * Aug 7 2009 mtk80068
 * [MAUI_01886896] [AGPS 2G CP] Check in MAUI branch
 * 
 *
 * Jul 6 2009 mtk80068
 * [MAUI_01712939] [AGPS 2G CP] 70.9.1.2 MT-LR MMI display wrong char
 * 
 *
 * Jun 29 2009 mtk80068
 * [MAUI_01873225] [AGPS CP] Check in
 * 
 *
 * May 3 2009 mtk80068
 * [MAUI_01828351] [AGPS] check in Control Plane code
 * 
 * 
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *============================================================================== 
 *******************************************************************************/
#ifndef __GPSMGRAGPSCP_H__
#define __GPSMGRAGPSCP_H__

//#if defined(__AGPS_CONTROL_PLANE__)

#include "ps_public_enum.h"
#include "ps_public_struct.h"
#include "mmi_msg_struct.h"
#include "cm_mtk_agps_common_def.h"
#include "mdi_gps.h"
#include "typedef.h"
#include "gps_mgr_adp.h"

#define MAX_MTLR_LN_REQ_NUM 5
#define MAX_MTLR_AE_REQ_NUM 5
/*#if defined(__AGPS_UP_CP_CONFLICT_HANDLING__)
    #define MAX_MOLR_REQ_NUM    1
#else
#define MAX_MOLR_REQ_NUM    5    //should not be larger than 7
#endif*/
#define MAX_MOLR_REQ_NUM	1    //changed by Baochu Wang

#define MMI_GPS_MGR_MTLR_NOTIFY_DELAY  20000
#define MMI_GPS_MGR_MTLR_CONFIRM_DELAY  20000
#define GPS_MGR_MTLR_NOTIFY_TIMER_ID GPS_MGR_NOTIFY_TIMER_ID_5
#define MAX_PLMN_STRING_LEN 6

typedef  signed char    CHAR;

typedef enum
{
   MMI_GPS_MGR_DEFAULT_DCS        = 0x00,  /* GSM 7-bit */
   MMI_GPS_MGR_8BIT_DCS           = 0x04,  /* 8-bit */
   MMI_GPS_MGR_UCS2_DCS           = 0x08,  /* UCS2 */  
   MMI_GPS_MGR_RESERVED_DCS       = 0x0c,  /* reserved alphabet,
                                        currently, MMI shall display "not support alphabet" 
                                        or "cannot display" when receive dcs indicated this value */ 
   MMI_GPS_MGR_EXT_DCS            = 0x10  /* Special dcs for non-standard character, 
                                       used by MMI and EMS */
}mmi_gps_mgr_dcs_enum;

typedef struct
{ 
    U16 req_id;
    mmi_ss_mtlr_begin_ind_struct    notify_info;
    mmi_agps_state_enum state;
    MMI_BOOL    is_used;    
}mmi_gps_manager_mtlr_notify_struct;

typedef struct
{ 
    U16 req_id;
    mmi_ss_aerq_begin_ind_struct    area_info;
    U32 last_event_time;
    mmi_agps_state_enum state;
    MMI_BOOL    need_send_req;
    MMI_BOOL    is_used;    
    MMI_BOOL    is_first;    
}mmi_gps_manager_mtlr_ae_req_struct;

typedef struct
{ 
    U16 req_id;
    mmi_ss_molr_begin_req_struct    molr_info;
    mmi_agps_state_enum state;
    mdi_gps_uart_work_mode_enum gps_mode;
    void (*callback)(S32 type, void *buffer, U32 length);
    MMI_BOOL    is_send_location;
    MMI_BOOL    is_single;
    U32 delay;
    U32 last_send_time;
    MMI_BOOL    is_used;    
    MMI_BOOL    is_first;    
}mmi_gps_manager_molr_req_struct;

typedef struct
{ 
    U8   plmn[MAX_PLMN_STRING_LEN + 1];
    U8   lac[2];
}mmi_gps_manager_l4c_area_info_struct;

typedef struct
{ 
    /* mtlr location notification */
    mmi_gps_manager_mtlr_notify_struct  notify_list[MAX_MTLR_LN_REQ_NUM]; 
    MMI_BOOL is_time_out;
    U8 request_id[322]; /* (160 + 1) * 2 */
    U16 client_name[161]; /* (160 + 1) * 2 */
    //---------------------------------Baochu Wang make change!
	//U8 request_id[50];
	//U16 client_name[50]; 
	//--------------------------------------------------------
    U8 extrenal_address[42]; /* (20 + 1) * 2 */
    /* mtlr area event */
    U8 last_mtlr_ae_ref_num;
    mmi_gps_manager_mtlr_ae_req_struct  mtlr_ae_req_list[MAX_MTLR_AE_REQ_NUM];
    MMI_BOOL cur_mtlr_ae_req_result;
    U8 cur_mtlr_ae_req_index;
    mmi_gps_manager_l4c_area_info_struct last_area_info;
    mmi_gps_manager_l4c_area_info_struct cur_area_info;
    /* molr */
    mmi_gps_manager_molr_req_struct molr_req_list[MAX_MOLR_REQ_NUM];
    S32 port;
    MMI_BOOL is_pos;
    MMI_BOOL is_ready;
    U8 waiting_index;
    /* public */
    U16 cur_id;
    U16 mtlr_count;
	U8	ss_id;
}mmi_gps_manager_cp_context_struct;

/*************************************************************************************
* Below definition are add by Baochu,Wang 2010/12/16
*This definition are in fact include in FeaturePhone MAUI namespace, but in smartPhone plateform we 
*can not get the definition directly, so here we give our own definition, it is may not match the MAUI 
*namespace, but it dosen't matter, these definition have no usage anywhere!
*************************************************************************************/
/*
typedef enum
{
    MMI_GPS_MGR_RESULT_SUCCESS = 0,
    MMI_GPS_MGR_RESULT_MORE_REQUEST = -1,
    MMI_GPS_MGR_RESULT_AGPS_OFF = -2,
    MMI_GPS_MGR_RESULT_WORK_MODE_DIFFER = -3,
    MMI_GPS_MGR_RESULT_OPEN_GPS_FAIL = -4,
    MMI_GPS_MGR_RESULT_FALSE_GPS_CALLBACK = -5,
    MMI_GPS_MGR_RESULT_GPS_ABORTING = -6,
    MMI_GPS_MGR_RESULT_UART_OPENING = -7,
    MMI_GPS_MGR_RESULT_PROFILE_ERROR = -8,
    MMI_GPS_MGR_MA_RAW_DATA = -9,
    MMI_GPS_MGR_RESULT_INVALID_REQ_ID = -10,
    MMI_GPS_MGR_RESULT_ECC_CALLING_ERROR = -11,
    MMI_GPS_MGR_RESULT_CP_IS_ONGOING = -12,
    MMI_GPS_MGR_RESULT_END
} mmi_gps_mgr_result_enum;
*///==Baochu, this is already defined in gps_mgr_adp.h

//MTLR
void mmi_gps_mgr_mmi_ss_mtlr_begin_ind_hdlr(void *msg);
void mmi_gps_mgr_mtlr_notify_handler(kal_uint8 ss_id, kal_int8 index);
void mmi_gps_mgr_show_mtlr_notify_src(kal_int8 index);
void mmi_gps_mgr_exit_mtlr_notify_src(void);
void mmi_gps_mgr_show_mtlr_confirm_src(void);
void mmi_gps_mgr_mtlr_notify_accept(void);
void mmi_gps_mgr_mtlr_notify_decline(void);
void mmi_gps_mgr_mtlr_notify_time_out();
void mmi_gps_mgr_send_mmi_ss_mtlr_begin_res_req(MMI_BOOL is_success, kal_uint8	ss_id, kal_int8 index);
void mmi_gps_mgr_mmi_ss_mtlr_begin_res_rsp_hdlr(void *msg);

//AERQ
void mmi_gps_mgr_mmi_ss_aerq_begin_ind_hdlr(void *msg);
void mmi_gps_mgr_send_mmi_ss_aerq_begin_res_req(MMI_BOOL is_success, U8 ref_num);
void mmi_gps_mgr_mmi_ss_aerq_begin_res_rsp_hdlr(void *msg);

//AERP
void mmi_gps_mgr_send_mmi_ss_aerp_begin_req(U8 begin_index);
void mmi_gps_mgr_mmi_ss_aerp_begin_rsp_hdlr(void *msg);
/////////////////////////////////////////////////////////////////////////////////////
S32 mmi_gps_mgr_send_mmi_ss_aerp_end_req(void);//mmi_ss_aerp_end_req
void mmi_gps_mgr_mmi_ss_aerp_end_rsp_hdlr(void *msg);//mmi_ss_aerp_end_rsp
////////////////////////////////////////////////////////////////////////////////////
MMI_BOOL mmi_gps_mgr_check_area_event(U8 index);
void mmi_gps_mgr_mmi_nw_attach_ind_hdlr(void *msg);

//AECL
void mmi_gps_mgr_mmi_ss_aecl_begin_ind_hdlr(void *msg);
void mmi_gps_mgr_send_mmi_ss_aecl_begin_res_req(MMI_BOOL is_success, U8 index);
void mmi_gps_mgr_mmi_ss_aecl_begin_res_rsp_hdlr(void *msg);

//MOLR
void mmi_gps_mgr_switch_mode_callback(mdi_gps_parser_p_info_enum type);
S32 mmi_gps_mgr_start_send_location(
    mmi_ss_molr_begin_req_struct * molr_info, 
    U16* req_id, 
    MMI_BOOL is_single,
    U32 delay,
    void (*callback)(S32 type, void *buffer, U32 length));

S32 mmi_gps_mgr_start_molr(
    mmi_ss_molr_begin_req_struct *molr_info, 
    mdi_gps_uart_work_mode_enum gps_mode,
    U16* req_id, 
    void (*callback)(S32 type, void *buffer, U32 length));

S32 mmi_gps_mgr_start_molr_internal(
    mmi_ss_molr_begin_req_struct *molr_info, 
    mdi_gps_uart_work_mode_enum gps_mode,
    MMI_BOOL is_send_location,
    MMI_BOOL is_single,
    U32 delay,
    U16* req_id, 
    void (*callback)(S32 type, void *buffer, U32 length));
void mmi_gps_mgr_send_mmi_ss_molr_begin_req(U8 index);
void mmi_gps_mgr_mmi_ss_molr_begin_rsp_hdlr(void *msg);
void mmi_gps_mgr_send_mmi_ss_molr_end_req(MMI_BOOL is_success);
/////////////////////////////////////////////////////////////////////////////////////
void mmi_gps_mgr_mmi_ss_molr_end_rsp_hdlr(void *msg); 
void mmi_gps_mgr_clean_molr_req_item(U8 index);
////////////////////////////////////////////////////////////////////////////////////
void mmi_gps_mgr_mmi_ss_mtlr_start_hdle(void *msg);
void mmi_gps_mgr_mmi_ss_mtlr_end_hdlr(void *msg);


void mmi_gps_mgr_molr_gps_callback(mdi_gps_parser_info_enum type, void *buffer, U32 length);
void mmi_gps_mgr_send_location(void *param);

mmi_gps_mgr_dcs_enum mmi_gps_mgr_decode_cbsdcs(U8 dcs);

U8 mmi_gps_mgr_convert_bcd_to_asc(U8 *source, U8 *dest, U32 count);
U8 mmi_gps_mgr_get_ch_byte_24008(U8 bcd);
void mmi_gps_mgr_cp_init(void);

U16 mmi_asc_to_ucs2(CHAR *pOutBuffer, CHAR *pInBuffer);
CHAR *mmi_ucs2cat(CHAR *strDestination, const CHAR *strSource);
int mmi_ucs2_len(S8 *arrOut);


//#endif /* __AGPS_CONTROL_PLANE__ */

#endif /* __GPSMGRAGPSCP_H__ */


