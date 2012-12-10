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
 *  mtk_agps_common_type.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   The common type used by mtk_agpsd and agps_cp_lib
 *
 * Author:
 * -------
 *  C.K. Chiang
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
 * 04 10 2012 archilis.wang
 * [ALPS00245081] [SW.GIN2_SINGLE][GIN2]AGPS RF Certification case fail in lab
 * .
 *
 * 04 03 2012 archilis.wang
 * [ALPS00254052] [Need Patch] [Volunteer Patch] Add the code for eCID
 * Add the support for DRVB.
 *
 * 03 30 2012 archilis.wang
 * [ALPS00254052] [Need Patch] [Volunteer Patch] Add the code for eCID
 * .
 *
 * 03 17 2012 archilis.wang
 * [ALPS00254052] [Need Patch] [Volunteer Patch] Add the code for eCID
 * .
 *
 * 01 18 2012 ck.chiang
 * [ALPS00116556] [0109 CMCC New Case] AGPS 5.1.1 AGPS ????
 * [ALPS00116556] [0109 CMCC New Case] AGPS 5.1.1 AGPS ????
 * [ALPS00116556] [0109 CMCC New Case] AGPS 5.1.1 AGPS Log enhancement<saved by Perforce>
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
 /* data type definition */
#ifndef __MTK_AGPS_COMMON_TYPE_H__
#define __MTK_AGPS_COMMON_TYPE_H__

#ifdef __cplusplus
extern "C" {
#endif

#include "typedef.h"

#define AGPS_CP_RESET_CMD_BY_PMTK106  1

typedef struct{
    int CP_EM_CALL_State;
    int CP_MOLR_State;
    int CP_MTLR_ss_State;
    int CP_NILR_State;
    int SUPL_MOLR_State;
    int SUPL_MTLR_State;
}cm_agps_session_state_struct;

typedef enum {
    SI_GSM_CP,
    SI_GSM_UP,
    SI_CDMA_CP,
    SI_CDMA_UP,
    SI_UNKNOWN
} MOLR_type;

typedef enum {
    SIM_NETWORK_TYPE_2G = 0,
    SIM_NETWORK_TYPE_3G = 1,
    SIM_NETWORK_TYPE_CDMA = 2,
    SIM_NETWORK_TYPE_SIP = 3,
    SIM_NETWORK_TYPE_UNKNOWN
} sim_network_type;

typedef struct{
    cm_agps_session_state_struct session_state;
    int ongoing_session;
    int supl_session_num;
    int NI_allowed;
    int MOLR_Type; //locationestimate(0), assistance data(1)
    int SIM1_MOLR_positionType;//UP(0),CP(1),CPUP_UP_Pref(2),CPUP_CP_Pref(3)
    //C.K. add-->
    #ifdef __CDMA_AGPS_SUPPORT__
    int SIM2_MOLR_positionType;
    #endif
    MOLR_type cur_MOLR_positionType;
    //C.K. add<--
    int external_address_enable;
    char external_address[32];
    int MLC_number_enable;
    char MLC_number[32];

    int CustomPolicy_Enable;
    int CustomPolicy_Type;
    int SUPL_SI_Req_Enable;
    int SUPL_Enable;
    int cur_SUPLSI_enabled;
    int cur_SUPL_enabled;
    int AGPS_Enable;
    int GPS_Status;
    int is_nilr_open_gps;
#if AGPS_CP_RESET_CMD_BY_PMTK106 != 0
    int is_CP_nilr_reset_gps;
#endif
    int is_open_gps_timeout;
    int log_file_max_num;
    int cm_open_gps_num;
    int sim1_status; //0= NOT_READY, 1=READY
    int sim2_status; //0= NOT_READY, 1=READY
    int simID_pref;
    int cur_molr_sim;
    int cur_mtlr_sim;
    //struct timeval lastclose;
    //struct timeval currentopen;
    time_t lastclose;
    time_t currentopen;
    int gps_reset_immediately;
    MMI_BOOL up_verify_timeout;
    MMI_BOOL is_waiting_GSP_status;
    int sim1_call_state;
    int sim2_call_state;
    int cur_data_conn_type;  // 1=sim1, 2= sim2, 3=wifi, others=no data connection
    int sim1_data_conn;
    int sim2_data_conn;
    int wifi_data_conn;

    int sim1_network_type ;   //0= 2G network, 1 = 3G network
    int sim2_network_type ;   //0= 2G network, 1 = 3G network
    MMI_BOOL recv_TTFF_fix;
    MMI_BOOL send_supl_end_msg;
    char nw_ip_addr[16];
    int sim1_isNetworkRoaming;
    int sim2_isNetworkRoaming;
    int roaming_enable; //0=only local network, 1= local+roam network
    int CA_enable;
    MMI_BOOL waiting_data_conn_after_call_end; //for 2G, during the call data_conn will disconnected, and when call end it will reconnected.
    int supl_end_session_num_for_log;
    int eCID_enable;
    int nCur_3G_switch;   // Introduced for ECID but it will be necessary for CP
#if _FP_CR_SYNC_MAUI_03018028__SP2 != 0 && _FP_CR_SYNC_MAUI_03018028__SP3 == 0
    int eCID_enable_last;
#endif
#if _FP_CR_SYNC_MAUI_03018028__SP3 != 0
    MMI_BOOL b_supl_need_ecid;
    int      nSIM_for_ecid;     // 0: off,   1: SIM1,  2: SIM2
#endif
    int      nDrvb_val;
}cm_agps_up_cp_context_struct;


#ifdef __cpluscplus
}
#endif

#endif /* __MTK_AGPS_COMMON_TYPE_H__ */
