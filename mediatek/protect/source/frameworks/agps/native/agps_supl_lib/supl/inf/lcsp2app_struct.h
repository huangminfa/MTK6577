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
 *  lcsp2app_struct.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   
 *
 * Author:
 * -------
 *  Lanslo Yang
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
 * 03 17 2012 archilis.wang
 * [ALPS00254052] [Need Patch] [Volunteer Patch] Add the code for eCID
 * .
 *
 * Oct 13 2009 mtk01600
 * [MAUI_01949555] [AGPS][CP] 70.9.4.3, assistance data missing error
 * 
 *
 * Apr 20 2008 mtk01162
 * [MAUI_00759867] [AGPRS] check-in AGPS RRLP part
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

#ifndef LCSP2APP_STRUCT_H
#define LCSP2APP_STRUCT_H

#include "lcsp2app_enums.h"

#define LCSP_MAX_RECORD_NUM     10
#define LCSP_MAX_NMR_NUM   15


typedef struct
{
    kal_uint8               ref_count;
    kal_uint16              msg_len;
    lcsp_session_enum  session_type;
    lcsp_pos_method_enum    pos_method;
} lcsp_start_req_struct;

typedef struct
{
    kal_uint8               ref_count;
    kal_uint16              msg_len;
} lcsp_end_req_struct;

typedef struct
{
    kal_uint8               ref_count;
    kal_uint16              msg_len;
} lcsp_location_id_req_struct;

typedef struct
{
    kal_uint16              arfcn;
    kal_uint8               bsic;
    kal_uint8               rx_lev;
} lcsp_nmr_element_struct;

typedef struct
{
    kal_uint8               num;
    lcsp_nmr_element_struct element[LCSP_MAX_NMR_NUM];
} lcsp_nmr_struct;

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
} lcsp_gsm_cell_info_struct;

typedef struct
{
    lcsp_gsm_cell_info_struct   gsm_cell_info;
} lcsp_cell_info_struct;

typedef struct
{
    lcsp_cell_info_struct   cell_info;
    lcsp_cell_status_enum   cell_status;
} lcsp_location_id_struct;

typedef struct
{
    kal_uint8                   ref_count;
    kal_uint16                  msg_len;
    lcsp_location_id_struct     locaion_id;
} lcsp_location_id_cnf_struct;

#if 0 //#ifdef __ULP_VER_1_5__    // Removed even in FP with [MAUI_02918147] [AGPS][SUPL] SUPL refactoring: remove unused code, GPS interface, code in __ULP_VER_1_5__
    typedef struct
    {
        kal_uint8               ref_count;
        kal_uint16              msg_len;
        kal_uint8               major;
        kal_uint8               technical;
        kal_uint8               editorial;
    } lcsp_version_cnf_struct;

    typedef struct
    {
        kal_uint8               ref_count;
        kal_uint16              msg_len;
        kal_bool                current_serv;
        kal_bool                current_non_serv;
        kal_bool                historic_serv;
        kal_bool                historic_non_serv;
    } lcsp_multi_location_id_req_struct;

typedef struct
{
    kal_uint8                   ref_count;
    kal_uint16                  msg_len;
    kal_uint16                  num_of_record;
    lcsp_location_id_struct locaion_id[LCSP_MAX_RECORD_NUM];
    kal_uint16                  time_stamp[LCSP_MAX_RECORD_NUM];
    kal_bool                    serving_cell_flag[LCSP_MAX_RECORD_NUM];
} lcsp_multi_location_id_cnf_struct;
#endif

#endif /* LCSP2APP_STRUCT_H */

