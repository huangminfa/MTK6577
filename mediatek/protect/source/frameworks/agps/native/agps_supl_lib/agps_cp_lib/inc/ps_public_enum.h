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
 * ps_public_enum.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This file contains the ABM network internal APIs 
 *   and these APIs can only be used by MMI CBM and DA
 *
 * Author:
 * -------
 * Karen Lin
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
 * 11 17 2010 danny.kuo
 * [MAUI_02835013] CMCC AT CMD support for PHB
 * .
 *
 * 10 28 2010 hogan.hou
 * [MAUI_02826881] [SIM] Max supported PLMN data size
 * move plmn data size definition due to MPM 
 *
 * 10 26 2010 hogan.hou
 * [MAUI_02826881] [SIM] Max supported PLMN data size
 * revise hard code 500 in plmn read/write interface and add size checking code
 *
 * 10 13 2010 mingtsung.sun
 * [MAUI_02827360] [Maui][MPM] check in MMI Protocol Modulation revise
 * <saved by Perforce>
 *
 * 10 13 2010 mingtsung.sun
 * [MAUI_02827360] [Maui][MPM] check in MMI Protocol Modulation revise
 * Integration change.
 *
 * 10 13 2010 hong.yu
 * [MAUI_02827360] [Maui][MPM] check in MMI Protocol Modulation revise
 * .
 *
 * 10 13 2010 hong.yu
 * [MAUI_02827360] [Maui][MPM] check in MMI Protocol Modulation revise
 * .
 * Add ps_public_enum.h and ps_public_struct.h
 * 
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef _PS_PUBLIC_ENUM_H_
#define _PS_PUBLIC_ENUM_H_
 
#define MAX_APN_LEN             100
#define MAX_PDP_ADDR_LEN        16
#define TCM_MAX_PEER_ACL_ENTRIES	 	10

#define MAX_GPRS_USER_NAME_LEN 32
#define MAX_GPRS_PASSWORD_LEN  32
#define MAX_GPRS_APN_LEN       100
#define MAX_GPRS_IP_ADDR       4 /* IP address length */

#define MAX_SIM_PASSWD_LEN      9

#define MAX_DIGITS_USSD          183
#define MAX_CC_ADDR_LEN          41
#define MAX_CC_ADDR_BCD_LEN      21
#define L4C_MAX_CALL_LIST_LEN    7
#define MAX_CC_SUB_ADDR_LEN      41
#define MAX_SIM_ADDR_BCD_LEN      21

//mtk02088_mpm
//mtk02088 mpm strat
#define SMSAL_MAX_CMD_LEN           (156)       /* max command data length */
#define SMSAL_MAX_MSG_LEN           (160) 
#if defined(__REL4__)
   #define SMSAL_MAX_MAILBOX_NUM  SMSAL_MAX_MBDN_NUM
#else
   #define SMSAL_MAX_MAILBOX_NUM  SMSAL_MAX_LINES
#endif
#ifdef __SMS_MSP_UP_TO_4__
#define SMSAL_MAX_MSP_NUM     4  //(max support msp)
#ifdef __REL6__
#define SMSAL_MAX_MBDN_NUM    10 // 20 is too much, reduce to 10
#else
#define SMSAL_MAX_MBDN_NUM    10 // 16 is too much, reduce to 10
#endif
#else //__SMS_MSP_UP_TO_4__
#define SMSAL_MAX_MSP_NUM     2 //(max support msp)
#ifdef __REL6__
#define SMSAL_MAX_MBDN_NUM    10 //(5 * SMSAL_MAX_MSP_NUM, max support mbdn)
#else
#define SMSAL_MAX_MBDN_NUM    8 //(4 * SMSAL_MAX_MSP_NUM, max support mbdn)
#endif
#endif //__SMS_MSP_UP_TO_4__
//mtk02088 mpm end

#define PHB_MAX_ASSOCIATE_GRP 10
#define PHB_MAX_EMAIL_LENGTH 61

#define MAX_SIM_NAME_LEN               (32)
#define MAX_PLMN_LEN             6
#define MAX_PLMN_LIST_LEN        32
#define MAX_NW_NAME_LEN 24

/* DO NOT MODIFY THIS BY YOURSELF!! Please confirm with SIM task owner first.  
   1. Enlarge this max support size will cause ctrl buffer usage grow.                      
   2. full SW library(ex: L4,SIM,MM lib) update is necessary                   */
#ifdef LOW_COST_SUPPORT
#define MAX_SUPPORT_PLMN_DATA_SIZE       120  
#else
#define MAX_SUPPORT_PLMN_DATA_SIZE       500     
#endif

/* Max supported PLMN record number is decided by MAX_SUPPORT_PLMN_DATA_SIZE and record size
   SIM task can support up to MAX_SUPPORT_PLMN_DATA_SIZE / PLMN_record_size records
   NOTE: Record size is 5 bytes for EF_UPLMNWACT,EF_OPLMNWACT,etc.  3 bytes for EF_PLMNSEL   */
#define MAX_SUPPORT_EF_PLMNSEL_REC       MAX_SUPPORT_PLMN_DATA_SIZE/3  


typedef enum {
   PHB_LN_CALL,
   PHB_LN_VOIP,
   PHB_LN_VIDEO
} phb_ln_addr_type_enum;

typedef enum {
   PHB_STORAGE_NONE,
   PHB_STORAGE_SIM,
   PHB_STORAGE_NVRAM,
   PHB_STORAGE_SIM2,
   PHB_STORAGE_MAX
} phb_storage_enum;

typedef enum {
   PHB_ERRNO_FAIL,
   PHB_ERRNO_SUCCESS,
   PHB_ERRNO_NOT_SUPPORT,
   PHB_ERRNO_READ_FAIL,
   PHB_ERRNO_READ_SUCCESS,
   PHB_ERRNO_READ_EMPTY,
   PHB_ERRNO_DELETE_EMPTY,
   PHB_ERRNO_NOT_READY,
   PHB_ERRNO_BUSY,
   PHB_ERRNO_ANR_FULL,
   PHB_ERRNO_EMAIL_FULL,
   PHB_ERRNO_SNE_FULL
} phb_errno_enum;

typedef enum {
   PHB_LND,
   PHB_LNM,
   PHB_LNR,
   PHB_LN_NONE,
   PHB_LN_MAX = PHB_LN_NONE
} phb_ln_type_enum;

typedef enum {
   PHB_NONE,
   PHB_ECC,
   PHB_FDN,
   PHB_BDN,
   PHB_MSISDN,
   PHB_SDN,
   PHB_PHONEBOOK,
   PHB_EXT1,   
   /* USIM */
   PHB_SNE, 
   PHB_AAS,
   PHB_ANR,
   PHB_GAS,
   PHB_GRP,
   PHB_EMAIL,
   PHB_PBC,
   /* USIM */
   PHB_TYPE_ENUM_MAX
} phb_type_enum;

#ifndef VOIP_URI_LEN
#define VOIP_URI_LEN                        (41)
#endif


#ifdef __PHB_USIM_ADDITIONAL_SUPPORT__
#define PHB_TYPE_TOTAL  PHB_TYPE_ENUM_MAX
#else
#define PHB_TYPE_TOTAL  PHB_EXT1
#endif

#ifndef NVRAM_PHB_SIZE
#define NVRAM_PHB_SIZE (54+MAX_PS_NAME_SIZE)
#endif

/* Max entries could be packed in peer buffer */
#define PHB_MAX_PHB_ENTRIES            15

typedef enum
{
	L4C_PHB_UPDATE,
	L4C_PHB_DELETE,
	L4C_PHB_DELETE_ALL,
	L4C_PHB_WRITE_LND,
	L4C_PHB_WRITE_LNM,
	L4C_PHB_WRITE_LNR,
	L4C_PHB_DELETE_LND,
	L4C_PHB_DELETE_LNM,
	L4C_PHB_DELETE_LNR,
	L4C_PHB_UPDATE_NONE
}l4c_phb_update_enum;

typedef enum {
   PHB_NO_CAUSE,
   PHB_CAUSE_STORAGE_FULL,          /* This value is returned when just added entry causes storage full */
   PHB_CAUSE_CAPACITY_EXCEEDED      /* Since storage is full, attempt of adding an entry returns this value */
} phb_cause_enum;

typedef enum
{
    APPROVAL_TYPE_NONE,
    APPROVAL_TYPE_ECC_ONLY,
    APPROVAL_TYPE_FDN_ONLY,
    APPROVAL_TYPE_FULL		//check both ECC and FDN
}l4c_phb_approval_type_enum;

typedef enum {
   PHB_BCD = 0x01,
   PHB_ASCII = 0x00,
   PHB_UCS2 = 0x08
#ifdef __PHB_0x81_SUPPORT__	/*MTK 2004-04-20 Wisoln, support maximum length of 0x81 UCS2*/
   ,
   PHB_UCS2_81 = 0x09,
   PHB_UCS2_82 = 0x10
#endif
} phb_charset_enum;



typedef enum
{
   L4C_OK,
   L4C_ERROR,
   L4C_NVRAM_ERROR_INIT = 5

} l4c_result_enum;

/* Johnny: for RMMI internal usage, do not add other enums here */
typedef enum
{
   L4C_NO_CAUSE,
   L4C_GEN_CAUSE
} l4c_cause_enum;

typedef enum
{
    ATCI_REQ_QOS = 0,
    ATCI_MIN_QOS,
    ATCI_NEG_QOS 	/* SATe */
} atci_qos_enum;

#ifndef __AGPS_SWIP_REL__
typedef enum {
    SUBSCRIBED_DELAY_CLASS = 0,
    RESERVED_NW_TO_MS_DELAY_CLASS = 0,
    DELAY_CLASS1,
    DELAY_CLASS2,
    DELAY_CLASS3,
    DELAY_CLASS4,
    RESERVED_BIDIRECT_DELAY_CLASS = 7,
    TOT_DELAY_CLASS = 8
} delay_class_enum;

typedef enum {
    SUBSCRIBED_REL_CLASS = 0,
    RESERVED_NW_TO_MS_REL_CLASS  = 0,
    REL_CLASS1,
    REL_CLASS2,
    REL_CLASS3,
    REL_CLASS4,
    REL_CLASS5,
    RESERVED_BIDIRECT_REL_CLASS = 7
} reliability_class_enum;

typedef enum {
    SUBSCRIBED_PTC = 0,
    RESERVED_NW_TO_MS_PTC = 0,
    PTC_1,
    PTC_2,
    PTC_3,
    PTC_4,
    PTC_5,
    PTC_6,
    PTC_7,
    PTC_8,
    PTC_9,
    RESERVED_BIDIRECT_PTC = 15
} peak_throughput_class;

typedef enum {
    SUBSCRIBED_PREC_CLASS = 0,
    RESERVED_NW_TO_MS_PREC_CLASS  = 0,
    PREC_CLASS1,
    PREC_CLASS2,
    PREC_CLASS3,
    RESERVED_BIDIRECT_PREC_CLASS = 7
} precedence_class_enum;

typedef enum {
    SUBSCRIBED_MEAN_THROUGHPUT = 0,
    RESERVED_NW_TO_MS_MEAN_THROUGHPUT = 0,
    MEAN_THROUGHPUT1,
    MEAN_THROUGHPUT2,
    MEAN_THROUGHPUT3,
    MEAN_THROUGHPUT4,
    MEAN_THROUGHPUT5,
    MEAN_THROUGHPUT6,
    MEAN_THROUGHPUT7,
    MEAN_THROUGHPUT8,
    MEAN_THROUGHPUT9,
    MEAN_THROUGHPUT10,
    MEAN_THROUGHPUT11,
    MEAN_THROUGHPUT12,
    MEAN_THROUGHPUT13,
    MEAN_THROUGHPUT14,
    MEAN_THROUGHPUT15,
    MEAN_THROUGHPUT16,
    MEAN_THROUGHPUT17,
    MEAN_THROUGHPUT18,
    RESERVED_BIDIRECT_MEAN_THROUGHPUT = 30,
    BEST_EFFORT_THROUGHPUT = 31
} mean_throughput_enum;
#endif /*#ifndef __AGPS_SWIP_REL__*/

typedef enum
{
     IPV4_ADDR_TYPE = 0X21,
     IPV6_ADDR_TYPE = 0X57,
     PPP_ADDR_TYPE  = 0X01,
     OSP_IHOSS_ADDR_TYPE = 0X02,
     NULL_PDP_ADDR_TYPE = 0X03 /* This is added incase if no pdpaddrtype is 
                                  specified */
}pdp_addr_type_enum;

typedef enum
{
     IPV4_ADDR_LEN = 0X04,
     IPV6_ADDR_LEN = 0X10,
     PPP_ADDR_LEN  = 0X0,
     OSP_IHOSS_ADDR_LEN = 0X0,
    /* NULL_PDP_ADDR_LEN = 0X02  Incase if no pdpaddr is specified then this
                                 length will be null */
    NULL_PDP_ADDR_LEN = 0X01
}pdp_addr_len_enum;

typedef enum
{
   L4C_GPRS_ATTACHED, //0
   L4C_NONE_GPRS, // 1
   L4C_GPRS_DETACHED, // 2
   L4C_INVALID_SIM,// 3
   L4C_GPRS_ATTEMPT_ATTACH,// 4
   L4C_GPRS_COVERAGE,//5 
   L4C_PDP_ATTEMPT_ACT,//6
   L4C_PDP_DEACTIVED,//7
   L4C_PDP_ACTIVED//8
} l4c_gprs_status_enum;

#ifndef __AGPS_SWIP_REL__
typedef enum
{
    NONE_SPECIFIED = 0x00,
    GPRS_SUPPORT = 0x01,
    EDGE_SUPPORT = 0x02,
    UMTS_SUPPORT = 0x04,
    HSDPA_SUPPORT = 0x08,
    HSUPA_SUPPORT = 0x10,
    HSDPA_UPA_SUPPORT = 0x18,
    NOT_CHANGE = 0xff     
}data_speed_support_enum;
#endif

typedef enum
{
	DATA_SPEED_NONE_ACTIVATE = 0,
	DATA_SPEED_GPRS_CAPABILITY,
	DATA_SPEED_EDGE_CAPABILITY,
	DATA_SPEED_UMTS_CAPABILITY,
	DATA_SPEED_HSDPA_CAPABILITY,
	DATA_SPEED_HSUPA_CAPABILITY,
	DATA_SPEED_HSDPA_HSUPA_CAPABILITY
} data_speed_activate_enum; 

typedef enum
{
    RMMI_UCM_ATD = 0,
    RMMI_UCM_ATD_DATA, //for ATD dialing data call
    RMMI_UCM_ATH,
    RMMI_UCM_ATA,
    RMMI_UCM_BLDN,
    RMMI_UCM_CHLD,
    RMMI_UCM_CHUP,
    RMMI_UCM_MEM_DIAL,  //mem dial from SIM1
    RMMI_UCM_MEM_DIAL_2, //mem dial from SIM2
    RMMI_UCM_ATD_2
} rmmi_ucm_action_enum;

typedef enum
{
    RMMI_UCM_CAUSE_NONE = 0,
    RMMI_UCM_CAUSE_ATD_NOT_ALLOWED,
    RMMI_UCM_CAUSE_ATA_NOT_ALLOWED,
    RMMI_UCM_CAUSE_ATH_NOT_ALLOWED,
    RMMI_UCM_CAUSE_CHLD_NOT_ALLOWED,
    RMMI_UCM_CAUSE_NO_CALL_TO_ANSWER,   //specific cause for ATA
    RMMI_UCM_CAUSE_NO_CALL_TO_HANGUP,   //specific cause  for ATH   
    RMMI_UCM_CAUSE_NO_CALL_TO_HOLD,     //specific cause  for CHLD              
    RMMI_UCM_CAUSE_NO_CALL_TO_REL,      //specific cause for CHLD
    RMMI_UCM_CAUSE_NO_CALL_TO_ECT,      //specific cause for CHLD
    RMMI_UCM_CAUSE_NO_CALL_TO_CONFERENCE,       // specific cause for CHLD                
    RMMI_UCM_CAUSE_INVALID_CALLID,      //specific cause for CHLD
    RMMI_UCM_CAUSE_NO_NUM_EXIST, //specific cause for memory dial or bldn when number not exist ( BQB expect return "ERROR") 
    RMMI_UCM_CAUSE_ATD_DATA_NOT_ALLOWED //MAUI_02634907
} rmmi_ucm_cause_enum;

typedef enum
{
    RMMI_UCM_RCODE_OK,
    RMMI_UCM_RCODE_CONNECT,
    RMMI_UCM_RCODE_RING,
    RMMI_UCM_RCODE_NO_CARRIER,
    RMMI_UCM_RCODE_ERROR,
    RMMI_UCM_RCODE_CCWA
} rmmi_rcode_type_enum;

typedef enum
{
    CLIR_INVOKE,
    CLIR_SUPPRESS,
    CLIR_AUTO
} rmmi_clir_enum;

typedef enum 
{
   CALL_DIAL,
   CALL_ANSWER
}l4c_data_call_op_enum;

typedef enum
{
    L4C_DISCONNECT_NONE,
    L4C_DISCONNECT_MO,
    L4C_DISCONNECT_MT
} l4c_ath_req_enum;

typedef enum
{
    L4C_SS_MO,
    L4C_SS_MT,
    L4C_SS_NONE
} l4c_ss_direction_enum;

typedef enum
{
   CSMCC_REL_HELD_OR_UDUB, /*0*/
   CSMCC_REL_ACTIVE_AND_ACCEPT, /*1*/
   CSMCC_REL_SPECIFIC_CALL, /*2*/
   CSMCC_HOLD_ACTIVE_AND_ACCEPT, /*3*/
   CSMCC_HOLD_ACTIVE_EXCEPT_SPECIFIC_CALL, /*4*/
   CSMCC_ADD_HELD_CALL, /*5*/
   CSMCC_EXPLICIT_CALL_TRANSFER, /*6*/
   CSMCC_ACTIVATE_CCBS_CALL, /*7*/
   CSMCC_REL_ALL_EXCEPT_WAITING_CALL, /*8*/
   CSMCC_REL_SPECIFIC_ACTIVE_CALL, /* 9 */
   CSMCC_SWAP_CALL, /* 10 */
   CSMCC_REL_HELD,  /* 11 */
   CSMCC_REL_ACTIVE,  /* 12 */
   CSMCC_REL_ALL,  /* 13 */
   CSMCC_REL_UDUB,  /* 14 */
   CSMCC_REL_CCBS,  /* 15 */
   CSMCC_REL_ACTIVE_AND_ACCEPT_WAITING,  /* 16 */
   CSMCC_REL_ACTIVE_AND_ACCEPT_CCBS,  /* 17 */
   CSMCC_REL_ACTIVE_AND_ACCEPT_HELD,  /* 18 */
   CSMCC_HOLD_ACTIVE_AND_ACCEPT_WAITING,  /* 19 */
   CSMCC_HOLD_ACTIVE_AND_ACCEPT_CCBS,  /* 20 */
   
   CSMCC_INVALID_CRSS_TYPE = 255
} csmcc_crss_req_enum;

typedef enum
{
   CLCC_MO_CALL,
   CLCC_MT_CALL,
   CLCC_UNKNOWN_DIR
} clcc_dir_enum;

typedef enum
{
   CSMCC_SETUP_MSG,
   CSMCC_DISCONNECT_MSG,
   CSMCC_ALERT_MSG,
   CSMCC_CALL_PROCESS_MSG,
   CSMCC_SYNC_MSG,
   CSMCC_PROGRESS_MSG,
   CSMCC_CALL_CONNECTED_MSG,
   CSMCC_ALL_CALLS_DISC_MSG = 129,
   CSMCC_MO_CALL_ID_ASSIGN_MSG = 130,
   CSMCC_STATE_CHANGE_HELD = 131,
   CSMCC_STATE_CHANGE_ACTIVE = 132,
   CSMCC_STATE_CHANGE_DISCONNECTED = 133,
   CSMCC_STATE_CHANGE_MO_DISCONNECTING =134, 
   CSMCC_CPI_END = 255
}csmcc_cpi_msg_type_enum; /*mtk00924 add 041210 for +ECPI*/

typedef enum
{
   RSAT_BY_TE,
   RSAT_BY_SIM,
   RSAT_BY_L4C,
   RSAT_NULL   
} rsat_config_enum;

typedef enum {
   INVALID_OPERATION,
   SS_OPERATION,
   SIM_OPERATION,
   GPRS_OPERATION,
   CC_OPERATION
} csmss_string_op_enum;

//agps begin
/* ENUMERATED NotificationToMSUser */
typedef enum
{
   L4C_SS_NotificationToMSUser_notifyLocationAllowed,
   L4C_SS_NotificationToMSUser_notifyAndVerify_LocationAllowedIfNoResponse,
   L4C_SS_NotificationToMSUser_notifyAndVerify_LocationNotAllowedIfNoResponse,
    // ...
   L4C_SS_NotificationToMSUser_locationNotAllowed
}
L4C_SS_NotificationToMSUser;

/* ENUMERATED OccurrenceInfo */
typedef enum
{
   L4C_SS_OccurrenceInfo_oneTimeEvent,
   L4C_SS_OccurrenceInfo_multipleTimeEvent
    // ...
}
L4C_SS_OccurrenceInfo;

/* ENUMERATED LocationEstimateType */
typedef enum
{
   L4C_SS_LocationEstimateType_currentLocation,
   L4C_SS_LocationEstimateType_currentOrLastKnownLocation,
   L4C_SS_LocationEstimateType_initialLocation,
    // ...
   L4C_SS_LocationEstimateType_activateDeferredLocation,
   L4C_SS_LocationEstimateType_cancelDeferredLocation
}
L4C_SS_LocationEstimateType;

/* ENUMERATED LCS-FormatIndicator */
typedef enum
{
   L4C_SS_LCS_FormatIndicator_logicalName,
   L4C_SS_LCS_FormatIndicator_e_mailAddress,
   L4C_SS_LCS_FormatIndicator_msisdn,
   L4C_SS_LCS_FormatIndicator_url,
   L4C_SS_LCS_FormatIndicator_sipUrl
    // ...
}
L4C_SS_LCS_FormatIndicator;

/* ENUMERATED VerificationResponse */
typedef enum
{
   L4C_SS_VerificationResponse_permissionDenied,
   L4C_SS_VerificationResponse_permissionGranted
    // ...
}
L4C_SS_VerificationResponse;

/* ENUMERATED AreaType */
typedef enum
{
   L4C_SS_AreaType_countryCode,
   L4C_SS_AreaType_plmnId,
   L4C_SS_AreaType_locationAreaId,
   L4C_SS_AreaType_routingAreaId,
   L4C_SS_AreaType_cellGlobalId,
    // ...
   L4C_SS_AreaType_utranCellId
}
L4C_SS_AreaType;

/* ENUMERATED MOLR-Type */
typedef enum
{
   L4C_SS_MOLR_Type_locationEstimate,
   L4C_SS_MOLR_Type_assistanceData,
   L4C_SS_MOLR_Type_deCipheringKeys
    // ...
}
L4C_SS_MOLR_Type;

/* ENUMERATED LocationMethod */
typedef enum
{
   L4C_SS_LocationMethod_msBasedEOTD,
   L4C_SS_LocationMethod_msAssistedEOTD,
   L4C_SS_LocationMethod_assistedGPS,    //==Baochu Guess:CP only need to tell network LocationMethod capability, no need to tell MA or MB 
    // ...
   L4C_SS_LocationMethod_msBasedOTDOA
}
L4C_SS_LocationMethod;

/* ENUMERATED ResponseTimeCategory */
typedef enum
{
   L4C_SS_ResponseTimeCategory_lowdelay,
   L4C_SS_ResponseTimeCategory_delaytolerant
    // ...
}
L4C_SS_ResponseTimeCategory;
//agps end

//mtk02088_mpm
//mtk02088 mpm strat
typedef enum
{
   SMSAL_DEFAULT_PID = 0x00,   /* Text SMS */
   SMSAL_TELEX_PID   = 0x21,   /* Telex */
   SMSAL_G3_FAX_PID  = 0x22,   /* Group 3 telefax */
   SMSAL_G4_FAX_PID  = 0x23,   /* Group 4 telefax */
   SMSAL_VOICE_PID   = 0x24,   /* Voice Telephone */
   SMSAL_ERMES_PID   = 0x25,   /* ERMES (European Radio Messaging System) */
   SMSAL_PAGING_PID  = 0x26,   /* National Paging system */
   SMSAL_X400_PID    = 0x31,   /* Any public X.400-based message system */
   SMSAL_EMAIL_PID   = 0x32    /* E-mail SMS */
   
} smsal_pid_enum;

typedef enum
{
   SMSAL_DEFAULT_DCS        = 0x00,  /* GSM 7-bit */
   SMSAL_8BIT_DCS           = 0x04,  /* 8-bit */
   SMSAL_UCS2_DCS           = 0x08,  /* UCS2 */  
   SMSAL_RESERVED_DCS       = 0x0c,  /* reserved alphabet,
                                        currently, MMI shall display "not support alphabet" 
                                        or "cannot display" when receive dcs indicated this value */ 
   SMSAL_EXT_DCS            = 0x10  /* Special dcs for non-standard character, 
                                       used by MMI and EMS */
} smsal_dcs_enum;

typedef enum
{
   /* Mailbox */
   SMSAL_LINE_1 = 0,
   SMSAL_LINE_2 = 1,
   SMSAL_MAX_LINES = 2,          

   /* SMS profile parameter */
   SMSAL_PROFILE_1 = 0,
   SMSAL_PROFILE_2 = 1,
   SMSAL_PROFILE_3 = 2,
   SMSAL_PROFILE_4 = 3, /* SMSAL_MAX_PROFILE_NUM (4) */
   SMSAL_PROFILE_NONE = 0xff /* invalid profile ID */
} smsal_dest_no_enum;
//mtk02088 mpm end

#endif
