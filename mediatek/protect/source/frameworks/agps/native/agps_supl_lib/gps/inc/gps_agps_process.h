/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2007
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
*  gps_agps_process.h
*
* Project:
* --------
*  MAUI
*
* Description:
* ------------
*  AGPS related structure define in GPS task
*
* Author:
* -------
*  Hai Wang (MBJ06018)
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
 * 11 04 2011 archilis.wang
 * [ALPS00086880] [Critical Patch][Andrew][IOT][AGPS][SUPL] TC 2.8 Failed - MS should record GPS position when 2D/3D fixed in SIMB case
 * .
 *
 * 12 20 2010 ham.wang
 * [MAUI_02644816] [GPS]gps area event check in
 * .
 *
 * 12 02 2010 ham.wang
 * [MAUI_02639374] [AGPS] dual sim support
 * .
 *
 * 08 06 2010 james.liu
 * [MAUI_02603514] [GPS][Modify] Solve NMEA CRC errors in MNL library
 * .
 *
 * Oct 13 2009 mtk80018
 * [MAUI_01901941] [AGPS 2G CP] add quick response of assist data check
 * 
 *
 * Aug 6 2009 mtk80018
 * [MAUI_01886224] [AGPS 2G CP] RRLP R5
 * 
 *
 * May 16 2008 mbj06018
 * [MAUI_00768656] [AGPS] Store time stamp and gps week of the position returned by server
 * 
 *
 * Apr 26 2008 mbj06018
 * [MAUI_00655816] [AGPS]AGPS check in
 * 
 *
 * Apr 17 2008 mbj06018
 * [MAUI_00655816] [AGPS]AGPS check in
 * 
 *
 * Mar 11 2008 mbj06018
 * [MAUI_00633675] [AGPS]GPS task decode GAD error
 * 
 *
 * Feb 25 2008 mbj06018
 * [MAUI_00623349] [AGPS] AGPS feature check-in
 * 
*------------------------------------------------------------------------------
* Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
*============================================================================
*******************************************************************************/
#ifndef __GPS_AGPS_PROCESS_H__
#define __GPS_AGPS_PROCESS_H__

#ifndef __AGPS_SWIP_REL__
#include "kal_non_specific_general_types.h"
#include "stack_config.h"
#include "gps2lcsp_enum.h"
#include "gps2lcsp_struct.h"
#include "agps_struct.h"
#include "gps_main.h"
#include "gps_uart_hdlr.h"
#include "kal_release.h"
#include "app_ltlcom.h"
#endif

#define _ENABLE_RESPONSE_TIME_WORKAROUND_ 0

#if defined(__AGPS_SUPPORT__)
#include "uagps_cp_gps_struct.h"
#define GPS_CP_AREA_EVENTS_NUM    (3)
typedef enum
{
    GPS_ERR_INTERNAL_UNDEFINED = 0,           /*Undefined error*/
    GPS_ERR_INTERNAL_FEW_BTS = 1,             /*Not support*/
    GPS_ERR_INTERNAL_FEW_SAT = 2,             /*There were not enough GPS satellites*/
    GPS_ERR_INTERNAL_EOTD_LOCASS_MISS = 3,    /*Not support*/
    GPS_ERR_INTERNAL_EOTD_ASSIST_MISS = 4,    /*Not support*/
    GPS_ERR_INTERNAL_GPS_LOCASS_MISS = 5,     /*GPS location calculation assistance data missing*/
    GPS_ERR_INTERNAL_GPS_ASSIST_MISS = 6,     /*GPS assistance data missing*/
    GPS_ERR_INTERNAL_METHOD = 7,              /*Requested method not supported (Not support)*/
    GPS_ERR_INTERNAL_NOT_PROCESSED = 8,       /*Location request not processed (Not support)*/
    GPS_ERR_INTERNAL_GPS_NOT_SERV_BTS = 9,    /*Reference BTS is not the serving BTS (Not support)*/
    GPS_ERR_INTERNAL_EOTD_NOT_SERV_BTS = 10  /*Not support*/
} gps_aloc_internal_error_code_enum;


typedef struct 
{
#if __AGPS_SWIP_REL__
    double       semi_maj_uncertain;         
    double       semi_min_uncertain;         
    kal_bool     altitude_uncertain_valid;   
    double       altitude_uncertain;        
#else
    kal_uint32   semi_maj_uncertain;         
    kal_uint32   semi_min_uncertain;         
    kal_bool     altitude_uncertain_valid;   
    kal_uint32   altitude_uncertain;
#endif
    kal_bool     delay_valid;	             
    kal_uint8    delay;                      
    kal_bool     age_valid;                  
    kal_uint16   age;                        
    kal_bool     is_check_assist_data;      /*Set to TRUE will response error if assist data not enough */   
#if defined(__L1_GPS_AUTO_TIMING_SYNC_SUPPORT__) || defined(__L1_GPS_REF_TIME_SUPPORT__)      
    kal_bool     is_gsm_gps_ref_time_wanted; /* set to TRUE if GSM GPS ref time was required to be reported */      
    kal_bool     is_utran_gps_ref_time_wanted; /* set to TRUE if UTRAN GPS ref time was required to be reported */                            
    /* uarfcn and phyCellId are valid when is_utran_gps_ref_time_wanted == KAL_TRUE */
    kal_uint16 uarfcn;
    kal_uint16 phyCellId;  
#endif     
} gps_pos_qop_decoded_struct;

typedef struct 
{
    kal_bool    is_valid;
    gps_pos_qop_decoded_struct    qop;
    module_type mod_id;
    kal_uint8   transaction_id;
    gps_error_code_enum error_code;
} gps_cntx_pos_qop_struct;

typedef struct 
{
    kal_bool    is_valid;
    gps_meas_qop_struct    qop;
    module_type mod_id;
    kal_uint8   transaction_id;
    gps_error_code_enum error_code;
} gps_cntx_meas_qop_struct;

typedef struct 
{
    kal_uint8 sat_data;
} gps_assist_trans_id_struct;


typedef struct 
{
    kal_uint32  latitude;         /*latitude */
    kal_bool    sign_latitude;    /* true: SOUTH, false: NORTH */
    kal_int32   longtitude;       /* N2, encoded longtitude with 2's complement */
    kal_bool    sign_altitude;    /* true: DEPTH, false: HEIGHT */
    kal_uint16  altitude;         /* no encoding, unit in meter */
    kal_uint16  unc_major;        /* K1, encoded r1 */
    kal_uint16  unc_minor;        /* K2, encoded r2 */
    kal_uint16  unc_bear;         /* N3, encoded a = 2N3, Bearing of semi-major axis (degrees)*/
    kal_uint16  unc_vert;         /* K3, encode r3, vertical uncertain */
    kal_uint8   confidence;       /* %, The confidence by which the position of a target 
                                  entity is known to be within the shape description, 
                                  expressed as a percentage. [0 ~ 100] (%)*/
    kal_int32   h_speed;          /*horizontal speed*/
    kal_uint16  bearing;          /*Direction of the horizontal speed*/
    kal_uint16  gps_week;
    kal_uint32  tow;
} gps_agps_set_pos_struct;


typedef struct 
{
    kal_uint16                          last_cmd;
    gps_assist_navigation_model_struct  navigation;
    kal_uint8                           navigation_index;
    kal_uint8                           navigation_transaction_id;
    module_type                         navigation_mod;
    kal_bool                            navigation_data_valid;
    gps_assist_almanac_struct           almanac;
    kal_uint8                           almanac_index;
    kal_uint8                           almanac_transaction_id;
    module_type                         almanac_mod;
    kal_bool                            almanac_data_valid;
    gps_assist_acquisition_struct       acquisition;
    kal_uint8                           acquisition_index;
    kal_uint8                           acquisition_transaction_id;
    module_type                         acquisition_mod;
    kal_bool                            acquisition_data_valid;
    gps_assist_ref_location_struct      ref_location;
    kal_uint8                           ref_location_transaction_id;
    module_type                         ref_location_mod;
    kal_bool                            ref_location_data_valid;
    gps_assist_ionosphere_struct        ionosphere;
    kal_uint8                           ionosphere_transaction_id;
    module_type                         ionosphere_mod;
    kal_bool                            ionosphere_data_valid;
    gps_assist_ref_time_struct          ref_time;
    kal_uint8                           ref_time_index;
    kal_uint8                           ref_time_transaction_id;
    module_type                         ref_time_mod;
    kal_bool                            ref_time_data_valid;
    gps_assist_utc_struct               utc;
    kal_uint8                           utc_transaction_id;
    module_type                         utc_mod;
    kal_bool                            utc_data_valid;
    gps_assist_real_time_integrity_struct   rti;
    kal_uint8                           rti_transaction_id;
    module_type                         rti_mod;
    kal_bool                            rti_data_valid;
    gps_assist_dgps_correction_struct   dgps_correction;
    kal_uint8                           dgps_correction_transaction_id;
    module_type                         dgps_correction_mod;
    kal_bool                            dgps_correction_data_valid;
} gps_assist_data_need_send_struct;

#ifdef __AGPS_CONTROL_PLANE__
typedef struct 
{
    uagps_cp_gps_pos_change_evaluation_req_struct event;
    module_type mod_id;
    kal_bool is_valid;
    kal_bool is_match;
    kal_bool is_need_report;
} uagps_cp_gps_pos_change_evaluation_cntx_struct;
#endif

typedef struct 
{
    gps_assist_bitmap_struct    assist_bitmap;
    gps_cntx_pos_qop_struct     pos_qop_array[GPS_AGPS_QOP_POS_ARRAY_MAX];
    gps_cntx_meas_qop_struct    meas_qop_array[GPS_AGPS_QOP_MEAS_ARRAY_MAX];
    gps_meas_result_struct      meas;
    gps_agps_pos_struct         last_pos;
    gps_assist_data_need_send_struct  assist_data;
    gps_agps_pos_struct         pos;
    gps_p_response_struct       response;
    gps_agps_loc_error_struct   loc_error;
#if defined(__L1_GPS_AUTO_TIMING_SYNC_SUPPORT__) || defined(__L1_GPS_REF_TIME_SUPPORT__)  
    kal_bool                    is_gsm_gps_ref_time_wanted;
    kal_bool                    gsm_gps_ref_time_result_valid;        
    gsm_gps_ref_time_result_struct gsm_gps_ref_time_result;    
    kal_bool                    is_utran_gps_ref_time_wanted;
    /* uarfcn and phyCellId are valid when is_utran_gps_ref_time_wanted == KAL_TRUE */
    kal_uint16 uarfcn;
    kal_uint16 phyCellId;
    kal_bool                    utran_gps_ref_time_result_valid;    
    utran_gps_ref_time_result_struct utran_gps_ref_time_result;    
#endif    
    kal_mutexid                 history_query_mutex;
    kal_uint16                  agps_port;
    kal_bool                    need_pos_query;
    kal_bool                    need_area_query;
    kal_bool                    need_meas_query;
    kal_bool                    is_cmd_in_processing;
    kal_bool                    is_pos_fixed;
#ifdef __AGPS_CONTROL_PLANE__
    uagps_cp_gps_pos_change_evaluation_cntx_struct cp_area_events[GPS_CP_AREA_EVENTS_NUM];
#endif

#if _ENABLE_RESPONSE_TIME_WORKAROUND_ //Hiki, bugfix for response time
    kal_mutexid                 meas_query_mutex;
    gps_agps_pos_struct         candidate_pos;   // candidate pos without checking QoP
    kal_bool                    is_pos_rcvd;     // rcvd pos or not    
    gps_meas_result_struct      candidate_meas;  // candidate meas without checking QoP
    kal_bool                    is_meas_rcvd;    // rcvd meas or not
#endif

#if _FP_CR_SYNC_MAUI_02929319_
    kal_bool                    is_pos_report_for_fixed;
//    kal_bool                    is_pos_report_delay;  // for 1igpst, CP fine time sync, UP code does not need them
//    kal_uint16                  pos_delay_cnt_tar;    //
//    kal_uint16                  pos_delay_cnt_curr;   //
#endif
    } gps_agps_cntx_struct;

extern void gps_agps_set_port(kal_int16 port);
extern void gps_agps_parser(kal_uint16 cmd, kal_uint16 port, const kal_char *buffer, kal_uint32 length);
extern void gps_agps_req_hdlr(ilm_struct *ilm_ptr);
extern void gps_agps_init(void);
extern kal_bool gps_agps_get_history_pos(gps_pos_qop_struct *qop,gps_pos_result_struct *history_pos,kal_uint16 *bitmap);
extern void gps_lcsp_clear_asssist_data(void);
extern void gps_agps_reset_assist_bitmap(void);
extern void gps_agps_reset_history(void);
void gps_agps_set_history_pos(gps_agps_set_pos_struct* pos);
void gps_lcsp_proc_asssist_cmd_proc(void);
void gps_agps_switch_mode_by_meas_loc_req(void);

#if _FP_CR_SYNC_MAUI_02929319_
extern void gps_agps_update_pos_when_fixed(void);
#endif

#ifdef __AGPS_SWIP_REL__
extern void gps_uart_send_all_assist_hdlr(void);
#endif

#endif /*__AGPS_SUPPORT__*/
#endif /*__GPS_AGPS_H__*/

