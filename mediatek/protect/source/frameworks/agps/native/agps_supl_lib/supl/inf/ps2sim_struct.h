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
 * PS2SIM_STRUCT.H
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This file is intends for SIM's message
 *
 * Author:
 * -------
 * PH SHIH
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
 * Feb 22 2010 mtk01612
 * [MAUI_02356228] [DUMA] BT SIM Access Profile
 * Apply BT SIM profile for duma.
 *
 * Feb 16 2010 mtk01612
 * [MAUI_00479434] [WM][SIM]AT+CSIM is not supported
 * Suppot AT+CSIM feature.
 *
 * Feb 12 2010 mtk01612
 * [MAUI_01899190] Support +CRSM on USIM
 * Add global file tag, and extend STATUS_REQ structure.
 *
 * Jan 26 2010 mtk01612
 * [MAUI_02014578] [Lipton] Detect O2 PrePay and PostPay Cards
 * 
 *
 * Jan 24 2010 mtk01612
 * [MAUI_02338152] [New Feature] EHPLMN
 * Support R7 feature: EHPLMN
 *
 * Jan 10 2010 mtk01612
 * [MAUI_02023545] [SIM] BT SIM profile interface on DUMA
 * Replace the BT SIM MSG ID SAP and its MSG Structure.
 * BT SIM MSG ID SAP: bluetooth_sap.h --> sim_sap.h
 * its MSG Structure: bluetooth_struct.h --> ps2sim_struct.h
 *
 * Dec 24 2009 mtk80420
 * [MAUI_02129840] [SIM] Remove useless context and extern functions
 * Medium item 5: remove the ef_acc, sopname, opname field in sim_mmi_security_ind_struct
 *
 * Nov 17 2009 mtk02480
 * [MAUI_01986140] [klocwork issue] issueid : 5608 project : MAUI_AMBER38_DEMO_GPRS_W0945
 * 
 *
 * Nov 1 2009 mtk01612
 * [MAUI_01977566] [WISDOM] Merge WISE_Dev change into MAUI/09B/09A
 * 
 *
 * Sep 3 2009 mtk02374
 * [MAUI_01949579] CTA dual-SIM standard check-in
 * 
 *
 * Aug 6 2009 mtk02374
 * [MAUI_01934193] [SIM]sim_rr_ready_ind_struct interface change requested by RR
 * 
 *
 * Jul 8 2009 mtk02480
 * [MAUI_01716279] [SAT]Change the file_list in file_change_ind from kal_uint8 to kal_uint16
 * 
 *
 * Jun 12 2009 mtk01612
 * [MAUI_01702417] [SIM] AT+CRSM length is incorrect when the command is from SMU2
 * - Root cause: 
 *    The length is not correct assigned when the file_info_req is from SMU2
 * - Solution: 
 *    (1) add a enum that indicates what's the "length" usage in file_info_cnf
 *    (2) assign status word correctly after calling sim_al_select_rsp()
 *
 * May 16 2009 mtk02374
 * [MAUI_01688263] [SIM][New Feature]SIM_USIM_Integration
 * 
 *
 * May 15 2009 mtk02374
 * [MAUI_01688263] [SIM][New Feature]SIM_USIM_Integration
 * 
 *
 * Apr 15 2009 mtk02480
 * [MAUI_01669424] [Wise] Check in Modem change for wise
 * 
 *
 * Dec 8 2008 mtk01612
 * [MAUI_01292553] [Feature] Macro for ECC name length
 * Use MAX_SIM_NAME_LEN for ECC name length and alpha id length. L4, L4PHB use the same macro also.
 *
 * Aug 8 2008 mtk01488
 * [MAUI_00816769] [G+C dual mode SIM] GSM / CDMA dual mode SIM feature
 * 
 *
 * May 15 2008 mtk01488
 * [MAUI_00772733] [New Feature][MMI][L4C][TCM][SIM] APN control list (__ACL_SUPPORT__)
 * 
 *
 * May 14 2008 mtk01488
 * [MAUI_00770944] [SIM] SIM_READY_IND generic interface when sim is initialized
 * 
 *
 * Feb 21 2008 mtk01488
 * [MAUI_00622200] [R4R5]  check in R4 R5 modification
 * 
 *
 * Jan 24 2008 mtk01488
 * [MAUI_00610291] [GEMINI] Merge GEMINI to MAUI/07B
 * 
 *
 * Dec 3 2007 mtk01488
 * [MAUI_00240397] [AT]at+cpol the set command can't return any code
 * 
 *
 * Oct 8 2007 mtk01488
 * [MAUI_00555631] [MAUI][ENS] Cingular Spec - Enhanced Network Selection
 * 
 *
 * May 10 2007 mtk01488
 * [MAUI_00382966] Question on AT command: CRSM CSIM
 * 
 *
 * Apr 24 2007 mtk01488
 * [MAUI_00423214] [Network Setup: Added new item will be lost in Preferred Networks if turn off/on mob
 * 
 *
 * Feb 26 2007 mtk01488
 * [MAUI_00367964] [SUPC] EAP-SIM failed when SIM lock is enabled
 * 
 *
 * Feb 6 2007 mtk01488
 * [MAUI_00358474] SAP-It always show emergency when enter wrong PIN code in Nokia-616
 * 
 *
 * Jan 23 2007 mtk01488
 * [MAUI_00358474] SAP-It always show emergency when enter wrong PIN code in Nokia-616
 * 
 *
 * Jan 22 2007 mtk01488
 * [MAUI_00358474] SAP-It always show emergency when enter wrong PIN code in Nokia-616
 * 
 *
 * Dec 7 2006 mtk01488
 * [MAUI_00348766] 1SIM file content change when L4 receive msg MSG_ID_SIM_MMI_SECURITY_IND
 * 
 *
 * Nov 22 2006 mtk01488
 * [MAUI_00345429] [MONZA] [MM] [SIM] modify fplmn in SIM_MM_READY_IND for USIM test case
 * 
 *
 * Aug 27 2006 mtk00732
 * [MAUI_00322195] 2 more SIM files needed when L4 receive msg MSG_ID_SIM_MMI_SECURITY_IND
 * 
 *
 * Apr 27 2006 mtk00732
 * [MAUI_00190357] [USIM][Add interface] USIM_URR_WRITE_CNF
 * 
 *
 * Apr 10 2006 mtk00732
 * [MAUI_00186373] [MONZA][New Feature] Add sim_type_query_enum and sim_error_cause_enum
 * 
 *
 * Apr 3 2006 mtk00732
 * [MAUI_00183804] [R99 feature][MMI][SIM][L4C][AT][RR] new prefer PLMN file: EF-PLMNwACT and EF-OPLMNw
 * 
 *
 * Mar 20 2006 mtk00732
 * [MAUI_00180242] [New Feature] USIM Feature
 * 
 *
 * Feb 6 2006 MTK00955
 * [MAUI_00171217] [RR][Coding revise] The number of prefer PLMN list in interface SIM_RR_READY_IND sho
 * 
 *
 * Nov 28 2005 mtk00732
 * [MAUI_00158430] [SMU][New Feature] Support SMS-PP OTA de-personalisation
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
 *  FILENAME : ps2sim_struct.h
 *  SYNOPSIS : Functions for encoding/decoding.
 *
 *                      R E V I S I O N    H I S T O R Y
 *
 */
#ifndef _PS2SIM_STRUCT_H
#define _PS2SIM_STRUCT_H

#define MAX_SIM_NAME_LEN               (32)

/*mtk01612:  [MAUI_02023545] BT_SIM_Profile for MAUI and DUMA*/
#define APDU_REQ_MAX_LEN    261 /* 256 bytes for data + 5 bytes header information: CLA,INS,P1,P2,P3 */
#define APDU_RSP_MAX_LEN    258 /* 256 bytes for data + 2 bytes status word SW1 and SW2 */
#define ATR_MAX_LEN                40

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 mode;
    kal_uint8 access_id;
    kal_uint8 pin1[8];
} sim_start_req_struct;

//mtk02374
typedef struct {
    kal_uint8 chv1_count;
    kal_uint8 chv2_count;
    kal_uint8 ubchv1_count;
    kal_uint8 ubchv2_count;
    kal_uint8 is_chv1_enabled;
    kal_uint8 is_chv1_verified;
    kal_uint8 is_chv2_enabled;
    kal_uint8 is_chv2_verified;
#ifdef __SIM_NEW_ARCH__
    kal_uint8 chv_key_ref[2];
#else    
    kal_uint8 chv1_key_ref;
    kal_uint8 chv2_key_ref;
#endif 
} sim_chv_info_struct;

typedef struct {
    kal_uint8 chv1_status;
    kal_uint8 chv2_status;
    kal_uint8 ubchv1_status;
    kal_uint8 ubchv2_status;
} sim_chv_status_struct;

typedef struct {
    kal_uint8 num_lp;
    kal_uint8 lp_file_type;
    kal_uint8 lp[10];
} sim_lp_struct;

#ifndef __AGPS_SWIP_REL__
typedef struct {
    kal_uint8 ecc[3];
    kal_uint8 esc; // emergency service category //
    kal_uint8 name_length;
    kal_uint8 name_dcs;
    kal_uint8 name[MAX_SIM_NAME_LEN]; 
} sim_ecc_entry_struct;

typedef struct {
    kal_uint8 num_ecc;
    sim_ecc_entry_struct ecc_entry[5];
    kal_uint8 ecc_rec_len;
    kal_uint8 *ecc_ptr;
} sim_ecc_struct;

typedef struct {
    unsigned int no_plmn_data;
    unsigned int plmn_data_size;
    kal_uint8 *plmn_data;
} sim_plmn_sel_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 result;
    sim_chv_info_struct chv_info;
    sim_lp_struct lp;
    sim_ecc_struct ecc;
    kal_uint8 iccid[10];
    kal_uint8 access_id;
} sim_start_cnf_struct;
#endif

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 cause;
} sim_error_ind_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 result;
    kal_uint8 ef_imsi[9];
    kal_uint8 is_valid_gid1;
    kal_uint8 gid1[20];
    kal_uint8 is_valid_gid2;
    kal_uint8 gid2[20];
/* moved from mmi_ready_ind */
    kal_uint16 num_plmnsel_data; /* zero means invalid! */
    kal_uint8 *plmnsel_data;
    kal_uint16 num_uplmnwact_data; /* zero means invalid! */
    kal_uint8 *uplmnwact_data;
    kal_uint16 num_oplmnwact_data; /* zero means invalid! */
    kal_uint8 *oplmnwact_data;
    kal_uint8 is_valid_dck;
    kal_uint8 dck[16];
/* [MAUI_02129840] mtk80420: Medium Item 5, Remove the ef_acc, sopname, opname from mmi_security_ind */

#if 0
    /* user plmn */
    kal_uint16 no_uplmn_data;
    kal_uint8 *uplmn_data;           /* Keep by SMU! */
    /* operator plmn */
    kal_uint16 no_oplmn_data;
    kal_uint8 *oplmn_data;           /* Keep by SMU! */
    /* HPLMN */
    kal_uint16 no_hplmn_data;
    kal_uint8 *hplmn_data;           /* Keep by SMU! */
#endif /* 0 */
} sim_mmi_security_ind_struct;

typedef struct {
   kal_uint8 alpha_id_size;
   kal_uint8 alpha_id[MAX_SIM_NAME_LEN];
   kal_uint8 bcd_len;
   kal_uint8 bcd_digit[41];
} sim_addr_struct;

typedef struct {
   kal_uint8 alpha_id_size;
   kal_uint8 alpha_id[21];
   kal_uint8 ton_npi;
   kal_uint8 digit_size;
   kal_uint8 digit[4];//[41];
   kal_uint8 index_level;
   kal_bool network_specific;
   kal_bool premium_rate;
} sim_info_num_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 result;
    kal_uint8 dn_type;
    kal_uint8 phase;
    kal_uint8 is_valid_puct;
    kal_uint8 ef_puct[5];
    kal_uint8 is_spn_valid;
    kal_uint8 spn[17];
    kal_uint8 is_opname_valid;
    kal_uint8 opname[20];
    kal_uint8 is_sopname_valid;
    kal_uint8 sopname[10]; 
   kal_uint8 no_msisdn;        // MSISDN
   sim_addr_struct msisdn[2];
   kal_uint8 no_info_num;      // Information Numbers
   sim_info_num_struct *info_num;
   kal_bool is_valid_csp;
   kal_uint8 csp[22];          // Custom Service Profile
    /* Acting HPLMN */ /* mtk01488 */
    kal_bool is_valid_ef_acting_hplmn;
    kal_uint8 ef_acting_hplmn[3]; 
    kal_bool is_usim; 
} sim_mmi_ready_ind_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 ef_imsi[9];
    kal_uint8 is_valid_ef_kc;
    kal_uint8 ef_kc[9];
    kal_uint8 ef_acc[2];
    kal_uint8 is_valid_ef_loci;
    kal_uint8 ef_loci[11];
    kal_uint8 is_valid_ef_gkc;
    kal_uint8 ef_gkc[9];
    kal_uint8 is_valid_ef_gloci;
    kal_uint8 ef_gloci[14];
    /* Below is adding for 3G */
    kal_uint8 ef_keys[33];      /* Always valid in USIM */
    kal_uint8 ef_keysps[33];  /* Always valid in USIM */
    kal_uint8 ef_hplmn_time;
    /* forbidden plmn */
    kal_uint8 is_valid_ef_fplmn;	 /* Keep for backward compatible */
    kal_uint8 num_of_fplmn; /* Extened to 10 sets of FPLMN for USIM */	
    kal_uint8 ef_fplmn[30];
    /* prefer plmn */
    kal_uint16 no_plmnsel_data;
    kal_uint8 *plmnsel_data;         /* Please don't free this pointer in MM. Keep by SMU! */
    /* user plmn */
    kal_uint16 no_uplmn_data;
    kal_uint8 *uplmn_data;           /* Please don't free this pointer in MM. Keep by SMU! */
    /* operator plmn */
    kal_uint16 no_oplmn_data;
    kal_uint8 *oplmn_data;           /* Please don't free this pointer in MM. Keep by SMU! */
    /* HPLMN */
    kal_uint16 no_hplmn_data;
    kal_uint8 *hplmn_data;           /* Please don't free this pointer in MM. Keep by SMU! */
    /* Acting HPLMN */ /* mtk01488 */
    kal_bool is_valid_ef_acting_hplmn;
    kal_uint8 ef_acting_hplmn[3]; 
    kal_bool is_usim;
    kal_bool is_service_27_support;
    kal_bool is_service_38_support;
    kal_uint8 is_valid_ef_ehplmn;	//__R7_EHPLMN__ start
    kal_uint8 num_of_ehplmn; 
    kal_uint8 ef_ehplmn[12]; //__R7_EHPLMN__ end
} sim_mm_ready_ind_struct;
/* GAS: MOD_SIM -> MOD_RRM */
typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 is_valid_ms_op_mode; /* !!!Remove for MONZA!!! */
    kal_uint8 ms_op_mode; /* !!!Remove for MONZA!!! */
    kal_uint8 ef_imsi[9];
    kal_uint8 is_valid_ef_bcch; /* !!!Remove for MONZA!!! */
    kal_uint8 ef_bcch[16];  /* ONLY for SIM card */
    kal_uint8 ef_acc[2];
    kal_uint8 is_valid_ef_loci; /* !!!Remove for MONZA!!! */
    kal_uint8 ef_loci[11];
    /* Acting HPLMN */ /* mtk01488 */
    kal_bool is_valid_ef_acting_hplmn;
    kal_uint8 ef_acting_hplmn[3]; 
    /* For MONZA only */	
    kal_bool is_usim;
    kal_bool is_test_sim;
} sim_rr_ready_ind_struct;

/* UAS: MOD_SIM -> MOD_USIME */
typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_bool  is_usim;
    kal_bool is_test_sim;
    kal_bool is_gsm_access_allowed;
    kal_uint8 ef_acc[2];
    kal_uint8 ef_imsi[9];
    kal_uint8 ef_start_hfn[6];
    kal_uint8 ef_threshold[3];
    kal_uint8 ef_loci[11];
    kal_bool is_valid_ef_psloci; /* If USIM inserted, this is EF-PSLOCI; if SIM inserted, this is EF_LOCIGPRS */
    kal_uint8 ef_psloci[14]; /* If USIM inserted, this is EF-PSLOCI; if SIM inserted, this is EF_LOCIGPRS */
    kal_uint16 size_ef_netpar;
    kal_uint8 ef_netpar[512];
} usim_urr_ready_ind_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 size_ef_netpar;
    kal_uint8 ef_netpar[512];
} usim_urr_update_netpar_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 result;
    kal_uint16 status_word;
} usim_urr_update_netpar_cnf_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 size_ef_netpar;
    kal_uint8 ef_netpar[512];
} usim_urr_write_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 result;
    kal_uint16 status_word;
} usim_urr_write_cnf_struct;


typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_bool    cipher_ind;   // 20050202 Benson add for display cipher indication
} sim_mmrr_ready_ind_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
} sim_cc_ready_ind_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
} sim_sms_ready_ind_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 random_val[16];
} sim_run_algo_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 cipher_key[8];
    kal_uint8 sres[4];
} sim_run_algo_cnf_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 file_idx;
    kal_uint16 para;
    kal_uint16 length;
    kal_uint8 access_id;
    kal_uint8 path[6];
    kal_uint8 src_id; 
} sim_read_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 file_idx;
    kal_uint16 para;
    kal_uint16 length;
    kal_uint8 data[258]; // 256 + 2 for driver usage in T=1
    kal_uint16 result;
    kal_uint16 status_word;
    kal_uint8 access_id;
    kal_uint8 path[6];
    kal_uint8 src_id; 
} sim_read_cnf_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 file_idx;
    kal_uint16 para;
    kal_uint16 length;
    kal_uint8 data[260]; // 255 + 5 for command header space
    kal_uint8 access_id;
    kal_uint8 path[6];
    kal_uint8 src_id; 
} sim_write_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 file_idx;
    kal_uint16 para;
    kal_uint16 length;
    kal_uint16 result;
    kal_uint16 status_word;
    kal_uint8 access_id;
    kal_uint8 path[6];
    kal_uint8 src_id; 
} sim_write_cnf_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 file_idx;
    kal_uint8 data[127];
    kal_uint8 data_len;
    kal_uint8 access_id;
    kal_uint8 path[6];
} sim_increase_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 file_idx;
    kal_uint8 data[258]; // 256 +2 for driver usage in T=1
    kal_uint16 result;
    kal_uint16 status_word;
    kal_uint16 length;
    kal_uint8 access_id;    
    kal_uint8 path[6];
} sim_increase_cnf_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 op;
    kal_uint8 which_chv;
    kal_uint8 old_value[8];
    kal_uint8 new_value[8];
    kal_uint8 access_id;
} sim_security_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 result;
    kal_uint16 status_word;
    kal_uint8 op;
    kal_uint8 which_chv;
    kal_uint8 access_id;
    sim_chv_info_struct chv_info;
} sim_security_cnf_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 access_id;
    kal_uint8 src_id; 
    kal_uint16 file_idx;
    kal_uint8 path[6];
    kal_uint8 info_type;
    kal_uint16 length; /*[MAUI_01702417] mtk01612: WinMo_GEMINI : +crsm*/  
} sim_file_info_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 access_id;
    kal_uint8 src_id;      
    kal_uint8 file_type; 
    kal_uint16 file_idx;
    kal_uint8 path[6];
    kal_uint8 info_type;
    kal_uint16 length;
    kal_uint16 result;
    kal_uint16 status_word;
    kal_uint16 file_size;
    kal_uint8 num_of_rec;
    kal_uint8 res_data[256];
} sim_file_info_cnf_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 file_idx;
    kal_uint8 pattern[16];
    kal_uint8 length;
    kal_uint8 mode;
} sim_seek_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 file_idx;
    kal_uint8 length;
    kal_uint16 result;
    kal_uint8 data[258]; //  256 + 2 for driver usage in T=1
} sim_seek_cnf_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 access_id;
    kal_uint8 dn_type;
    kal_uint8 switch_on;
    
} sim_dial_mode_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 access_id;
    kal_uint16 result;
    kal_uint8 dn_type;
    kal_uint8 switch_on;    
} sim_dial_mode_cnf_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 access_id;
    kal_uint16 length; /*mtk01612: [MAUI_01899190] usim_on_duma*/
} sim_status_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 access_id;
    kal_uint16 result;
    kal_uint16 status_word;
    kal_uint8 data[258]; // 256 + 2 for driver usage in T=1
    kal_uint16 length;
} sim_status_cnf_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 switch_mode;
} sim_error_test_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 file_idx;
    kal_uint8 access_id;
    kal_uint8 state;		/* 1: read after update */
    kal_uint8 src_id;
} sim_read_plmn_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 file_idx;
    kal_uint16 result;
    kal_uint16 status_word;
    kal_uint16 file_size;
    kal_uint8 file[500]; /* depends on the maximum supported file size of plmn type files */
    kal_uint8 access_id;
    kal_uint8 state;		/* 1: read after update */
    kal_uint8 src_id;    
} sim_read_plmn_cnf_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 file_idx;
    kal_uint16 file_size;
    kal_uint8 file[500];  /* depends on the maximum supported file size of plmn type files */
    kal_uint8 access_id;
    kal_uint8 src_id;    
} sim_write_plmn_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 file_idx;
    kal_uint16 result;
    kal_uint16 status_word;
    kal_uint8 access_id;
    kal_uint8 src_id;    
} sim_write_plmn_cnf_struct;

/* USIM new interface */
typedef struct {
    kal_uint8   ref_count;
    kal_uint16 msg_len;
    kal_uint16 file_idx;
    kal_uint8   abs_path[6];
    kal_uint8   pattern[20];
    kal_uint8   p_len;
    kal_uint8   mode;
    kal_uint16 rsp_len;
    kal_uint8   access_id;
} sim_search_rec_req_struct;

typedef struct {
    kal_uint8   ref_count;
    kal_uint16 msg_len;
    kal_uint16 file_idx;
    kal_uint8   abs_path[6];
    kal_uint16  result;
    kal_uint16  status_word;
    kal_uint8    rec_list[258]; // 256 + 2 for driver usage in T=1
    kal_uint16  rec_len;
    kal_uint8    access_id;
} sim_search_rec_cnf_struct;

typedef struct {
    kal_uint8    ref_count;
    kal_uint16  msg_len;
    kal_uint16  file_idx;
    kal_uint8   abs_path[6];
    kal_uint8   access_id;
} sim_deactivate_file_req_struct;

typedef struct {
    kal_uint8   ref_count;
    kal_uint16 msg_len;
    kal_uint16 file_idx;
    kal_uint8   abs_path[6];
    kal_uint16 result;
    kal_uint16 status_word;
    kal_uint8   access_id;
} sim_deactivate_file_cnf_struct;

typedef struct {
    kal_uint8   ref_count;
    kal_uint16 msg_len;
    kal_uint16 file_idx;
    kal_uint8   abs_path[6];
    kal_uint8   access_id;
} sim_activate_file_req_struct;

typedef struct {
    kal_uint8   ref_count;
    kal_uint16  msg_len;
    kal_uint16  file_idx;
    kal_uint8   abs_path[6];
    kal_uint8   access_id;
    kal_uint8   result;
    kal_uint16 status_word;
} sim_activate_file_cnf_struct;

typedef struct {
    kal_uint8   ref_count;
    kal_uint16 msg_len;
    kal_uint8   rand[16];
    kal_bool    is_auth_present;
    kal_uint8   auth[16];
} sim_authenticate_req_struct;

typedef struct {
    kal_uint8   ref_count;
    kal_uint16 msg_len;
    kal_uint16 result;
    kal_uint16 status_word;
    kal_uint8   kc[8];
    kal_uint8   sres[4];
    kal_uint8   ck[16];
    kal_uint8   ik[16];
    kal_uint8   res[16];
    kal_uint8   res_len;
    kal_uint8   auts[16];
    kal_uint8   auts_len;
} sim_authenticate_cnf_struct;

typedef struct {
    kal_uint8   ref_count;
    kal_uint16 msg_len;
    kal_uint8   access_id;
    kal_uint8   length;
} sim_get_challenge_req_struct;

typedef struct {
    kal_uint8   ref_count;
    kal_uint16 msg_len;
    kal_uint8   access_id;
    kal_uint16 result;
    kal_uint16 status_word;
    kal_uint8   challenge[130]; // 256 +2 for driver usage in T=1
    kal_uint16 length;
} sim_get_challenge_cnf_struct;

typedef struct {
    kal_uint8   ref_count;
    kal_uint16 msg_len;
    kal_uint8   access_id;
    kal_uint8   ch_op;
    kal_uint8   ch_id;
} sim_manage_channel_req_struct;

typedef struct {
    kal_uint8   ref_count;
    kal_uint16 msg_len;
    kal_uint8   access_id;
    kal_uint16 result;
    kal_uint16 status_word;
    kal_uint8   ch_op;
    kal_uint8   ch_id;
} sim_manage_channel_cnf_struct;

typedef struct {
    kal_uint8   ref_count;
    kal_uint16 msg_len;
    kal_uint8	next_type;
    sim_chv_info_struct	chv_info;
} sim_l4c_verify_pin_result_ind_struct;

typedef struct {
    kal_uint8  ref_count;
    kal_uint16 msg_len;
    kal_uint8  ef_imsi[9];
} sim_ready_ind_struct;

typedef struct {
   kal_uint8 ref_count;
   kal_uint16 msg_len;
   kal_bool is_sim_inserted;
} sim_status_update_ind_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 access_id;
    kal_uint8 switch_on;    
} sim_acl_mode_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 access_id;
    kal_uint16 result;
    kal_uint8 switch_on;    
} sim_acl_mode_cnf_struct;

typedef struct {
   kal_uint8 ref_count;
   kal_uint16 msg_len;
   kal_uint8 src_id;
} l4c_sim_get_gsmcdma_dualsim_info_req_struct;

typedef struct {
   kal_uint8 ref_count;
   kal_uint16 msg_len;
   kal_uint8 src_id;
   kal_uint8 is_sim_inserted;
   kal_uint8 is_df_gsm_existed;
   kal_uint8 is_df_cdma_existed;
} l4c_sim_get_gsmcdma_dualsim_info_cnf_struct;

typedef struct {
   kal_uint8 ref_count;
   kal_uint16 msg_len;
   kal_uint8 src_id;
   kal_uint8 op;
} l4c_sim_set_gsmcdma_dualsim_mode_req_struct;

typedef struct {
   kal_uint8 ref_count;
   kal_uint16 msg_len;
   kal_uint8 src_id;
   kal_uint8 op;
   kal_uint8 result;
} l4c_sim_set_gsmcdma_dualsim_mode_cnf_struct;

/* MAUI_01949579 mtk02374 20090903 for CTA GEMINI new bootup*/
typedef struct {
   kal_uint8 ref_count;
   kal_uint16 msg_len;
   kal_uint8 src_id;
} sim_reset_req_struct;

typedef struct {
   kal_uint8 ref_count;
   kal_uint16 msg_len;
   kal_uint8 src_id;
   kal_uint8 is_sim_inserted;
} sim_reset_cnf_struct;

/*mtk01612: wise_vsim start*/
typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 src_id;
    kal_bool  switch_on;    
} l4c_sim_set_vsim_mode_req_struct;

typedef struct {
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 src_id;
    kal_uint16 result;
    kal_bool  switch_on;   
} l4c_sim_set_vsim_mode_cnf_struct;
/*mtk01612: wise_vsim end*/

/*mtk01612:  [MAUI_02023545] BT_SIM_Profile for MAUI and DUMA*/
typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 src_id;
} bt_sim_connect_req_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 result;
    kal_uint8 current_transport_protocol_type;
    kal_uint8 supported_transport_protocol_type_capability;
    kal_uint16 atr_len;
    kal_uint8 atr[ATR_MAX_LEN];
    kal_uint8 src_id;    
} bt_sim_connect_cnf_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 transport_protocol_type;
    kal_uint8 src_id;    
} bt_sim_reset_req_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 result;
    kal_uint8 current_transport_protocol_type;
    kal_uint16 atr_len;
    kal_uint8 atr[ATR_MAX_LEN];
    kal_uint8 src_id;    
} bt_sim_reset_cnf_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 transport_protocol_type;
    kal_uint16 apdu_req_len;
    kal_uint8 apdu_req[APDU_REQ_MAX_LEN];
    kal_uint8 src_id;    
} bt_sim_apdu_req_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 result;
    kal_uint16 apdu_rsp_len;
    kal_uint8 apdu_rsp[APDU_RSP_MAX_LEN];
    kal_uint8 src_id;    
} bt_sim_apdu_cnf_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 src_id;
} bt_sim_disconnect_req_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 result;
    kal_uint8 src_id;
} bt_sim_disconnect_cnf_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 src_id;
} bt_sim_power_off_req_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint16 result;
    kal_uint8 src_id;    
} bt_sim_power_off_cnf_struct;

typedef bt_sim_reset_req_struct bt_sim_power_on_req_struct;
typedef bt_sim_reset_cnf_struct bt_sim_power_on_cnf_struct;

/*mtk01612: [MAUI_02014578] wise detect O2 prepay SIM*/
typedef struct {
   kal_uint8 ref_count;
   kal_uint16 msg_len;
   kal_bool is_o2_prepaid_sim;
} sim_o2_prepaid_sim_ind_struct;

/*mtk01612: MAUI_00479434 __CSIM__*/
typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;
    kal_uint8 src_id;
    kal_uint16 req_len;
    kal_uint8 req[APDU_REQ_MAX_LEN];
} sim_csim_req_struct;

typedef struct
{
    kal_uint8 ref_count;
    kal_uint16 msg_len;  
    kal_uint8 src_id;
    kal_uint8 result; //csim_result_enum
    kal_uint16 rsp_len;
    kal_uint8 rsp[APDU_RSP_MAX_LEN];
} sim_csim_cnf_struct;

#endif 


