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
 *   as2l4c_struct.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *
 * Author:
 * -------
 * Lexel Yu 
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
 * Mar 22 2010 mtk02285
 * [MAUI_02361221] [MT6276] HSPA on/off menu and H icon
 * 
 *
 * Nov 27 2009 mtk02647
 * [MAUI_01998646] [cell info] To add the tx power level in gas_nbr_cell_info_struct
 * update the tx power level for the CVM
 *
 * Sep 24 2009 mtk02480
 * [MAUI_01943476] [EM Request] Preference for PLMN List
 * 
 *
 * Sep 16 2009 mtk01760
 * [MAUI_01956206] Replace compile options for TDD
 * 
 *
 * Sep 2 2009 mtk02480
 * [MAUI_01936271] [3G AGPS][User Plane] Check-in 3G AGPS UP related files into MAUI on W09.37
 * 
 *
 * Jun 1 2009 mtk02480
 * [MAUI_01696144] [AGPS] 3G Cell info support
 *
 * 
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/

/********************************************************************************
*  Copyright Statement:
*  --------------------
*
*  This product has been developed using a protocol stack
*  developed by Sasken Communication Technologies Limited.
*
********************************************************************************/

/*******************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2002
*
*******************************************************************************
 *  FILENAME : as2l4c_struct.h
 *  SYNOPSIS : 
 *
 *                      R E V I S I O N    H I S T O R Y
 *
 */
#ifndef _AS2L4C_STRUCT_H
#define _AS2L4C_STRUCT_H

/*-----UAS cell info struct start-------------*/
//#ifdef __UMTS_RAT__

#if _FP_CR_SYNC_MAUI_03018028_ == 0
    #define UAS_MAX_MEASURED_RESULTS_NUM    8
    #define UAS_MAX_CELL_MEASURED_RESULTS_NUM    32
#else
    #define UAS_MAX_MEASURED_RESULTS_NUM    3
    #define UAS_MAX_CELL_MEASURED_RESULTS_NUM    6
    #define UAS_MAX_TIMESLOT_PER_SUBFRAME    7  
#endif

typedef struct
{
    kal_uint8       uarfcn_ul_used; // kal_bool
    kal_uint16      uarfcn_ul;
    kal_uint16      uarfcn_dl;
} uas_freq_info_fdd_struct;

typedef struct
{
    kal_uint16      uarfcn;
} uas_freq_info_tdd_struct;

typedef enum
{
    UAS_FREQ_INFO_MODE_FDD = 1,
    UAS_FREQ_INFO_MODE_TDD,
    UAS_FREQ_INFO_MODE_END
} uas_freq_info_mode_enum;

typedef struct
{
    kal_uint8             mode; // uas_freq_info_mode_enum
    union
    {
        uas_freq_info_fdd_struct        fdd;
        uas_freq_info_tdd_struct        tdd;
    } choice;
} uas_freq_info_mode_specific_info_struct;

typedef struct
{
    uas_freq_info_mode_specific_info_struct     mode_specific_info;
} uas_freq_info_struct;

/* Measurement results for FDD cell */
typedef struct
{
    kal_uint16              psc;
    kal_uint8               cpich_Ec_N0_used; // kal_bool
    kal_uint8               cpich_Ec_N0;
    kal_uint8               cpich_rscp_used; // kal_bool
    kal_uint8               cpich_rscp;
    kal_uint8               pathloss_used; // kal_bool
    kal_uint8               pathloss;
} uas_cell_measured_results_fdd_struct;

/* Measurement results for TDD cell, not implemented now */
#if _FP_CR_SYNC_MAUI_03018028_ != 0
typedef struct
{
    kal_uint8               num;
    kal_uint8               element[2*UAS_MAX_TIMESLOT_PER_SUBFRAME];
}uas_cell_measured_result_tdd_timeslot_iscp_struct;
#endif

typedef struct
{
  #if _FP_CR_SYNC_MAUI_03018028_ == 0
    kal_uint8               dummy;
  #else
    kal_uint8                                           cellParameterId;
    kal_uint8                                           tgsn_used; // kal_bool
    kal_uint8                                           tgsn;
    kal_uint8                                           pccpch_rscp_used; // kal_bool
    kal_uint8                                           pccpch_rscp;
    kal_uint8                                           pathloss_used; // kal_bool
    kal_uint8                                           pathloss;
    kal_uint8                                           timeSlot_iscp_used; // kal_bool
    uas_cell_measured_result_tdd_timeslot_iscp_struct   timeslot_iscp_list;
  #endif
} uas_cell_measured_results_tdd_struct;

typedef enum
{
    UAS_CELL_MEASURED_RESULTS_MODE_FDD = 1,
    UAS_CELL_MEASURED_RESULTS_MODE_TDD,
    UAS_CELL_MEASURED_RESULTS_MODE_END
} uas_cell_measured_results_mode_enum;

typedef struct
{
    kal_uint8         mode; // uas_cell_measured_results_mode_enum

    union
    {
        uas_cell_measured_results_fdd_struct    fdd;
        uas_cell_measured_results_tdd_struct    tdd;
    } choice;
} uas_cell_measured_results_mode_specific_info_struct;

typedef struct
{
    kal_uint8                                                   cell_id_used; // kal_bool
    kal_uint32                                                  cell_id;
    uas_cell_measured_results_mode_specific_info_struct         mode_specific_info;
} uas_cell_measured_results_struct;

/* Measured results of most 32 different cells in one UARFCN */
typedef struct
{
    kal_uint8                               num;
    uas_cell_measured_results_struct        element[UAS_MAX_CELL_MEASURED_RESULTS_NUM];
} uas_cell_measured_results_list_struct;

typedef struct
{
    kal_uint8                               freq_info_used; // kal_bool
    uas_freq_info_struct                    freq_info;
    kal_uint8                               utra_carrier_rssi_used; // kal_bool
    kal_uint8                               utra_carrier_rssi;
    kal_uint8                               cell_measured_results_list_used; // kal_bool
    uas_cell_measured_results_list_struct   cell_measured_results_list;
} uas_measured_results_struct;

/* Measurement results of most 8 different UARFCN's */
typedef struct
{
    kal_uint8                           num;
    uas_measured_results_struct         element[UAS_MAX_MEASURED_RESULTS_NUM];
} uas_measured_results_list_struct;

/* WCDMA cell info */
typedef struct
{
    kal_uint16                          mcc;
    kal_uint16                          mnc;
    kal_uint32                          uc;
    kal_uint8                           freq_info_used; // kal_bool
    uas_freq_info_struct                freq_info;
    kal_uint8                           psc_used; // kal_bool
    kal_uint16                          psc;
    kal_uint8                           measured_results_list_used; // kal_bool
    uas_measured_results_list_struct    measured_results_list;
} uas_nbr_cell_info_struct;

//#endif  // __UMTS_RAT__
/*-----UAS (3G) cell info struct end-------------*/


/*-----GAS (2G) cell info struct--------- */
typedef struct
{
    kal_uint16 arfcn;
    kal_uint8 bsic;
    kal_uint8 rxlev;
}gas_nbr_cell_meas_struct;

typedef struct
{
    kal_uint16 mcc;
    kal_uint16 mnc;
    kal_uint16 lac;
    kal_uint16 ci;
}global_cell_id_struct;

typedef struct
{
    kal_int8 nbr_meas_num;
    gas_nbr_cell_meas_struct nbr_cells[15];
}gas_nbr_meas_struct;


typedef struct
{
    global_cell_id_struct gci;
    kal_uint8 nbr_meas_rslt_index;
}gas_cell_info_struct;

typedef struct
{
    gas_cell_info_struct serv_info;
    kal_uint8 ta;
/* 091125 shuang CVM_PWR_LEV add for CVM network command power level*/
    kal_uint8 ordered_tx_pwr_lev;
    kal_uint8 nbr_cell_num;
    gas_cell_info_struct nbr_cell_info[6];
    gas_nbr_meas_struct nbr_meas_rslt;
}gas_nbr_cell_info_struct;

/*-----GAS (2G) cell info struct--end------- */

typedef union
{
    gas_nbr_cell_info_struct gas_nbr_cell_info;
//#ifdef __UMTS_RAT__
    uas_nbr_cell_info_struct uas_nbr_cell_info;  
//#endif
}ps_nbr_cell_info_union_type;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 rat_mode;
    kal_uint8 is_nbr_info_valid;  // kal_bool
    ps_nbr_cell_info_union_type ps_nbr_cell_info_union;
} l4cps_nbr_cell_info_start_cnf_struct, l4cps_nbr_cell_info_ind_struct,l4c_nbr_cell_info_reg_cnf_struct,l4c_nbr_cell_info_ind_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
} l4cps_nbr_cell_info_start_req_struct, l4cps_nbr_cell_info_stop_req_struct;

#ifdef __PLMN_LIST_PREF_SUPPORT__
typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    plmn_list_preference_enum preference;
} l4cas_set_plmn_list_preference_req_struct;
#endif

#ifdef __DYNAMIC_HSPA_PREFERENCE__
typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
#ifdef __RMMI_UT__
    kal_uint8 hspa_preference;
#else
    hspa_preference_enum hspa_preference;
#endif
} l4cas_set_hspa_preference_req_struct;
#endif

#endif // _AS2L4C_STRUCT_H


