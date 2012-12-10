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
 *  supl_def.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This file contains SUPL task entry function.
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
 * 05 02 2012 ck.chiang
 * [ALPS00266541] [Urgent]?Pre-test??CMCC??Critical??????GPS??GPS???????
 * Integration change.
 *
 * 03 17 2012 archilis.wang
 * [ALPS00254052] [Need Patch] [Volunteer Patch] Add the code for eCID
 * .
 *
 * Sep 30 2009 mtk00563
 * [MAUI_01935796] [AGPS] Set a timer if DATA_REQ is not sent after DATA_RSP.
 * 
 *
 * Sep 4 2009 mtk00563
 * [MAUI_01936271] [3G AGPS][User Plane] Check-in 3G AGPS UP related files into MAUI on W09.37
 * 
 *
 * Aug 24 2009 mtk00563
 * [MAUI_01943924] [AGPS] AGPS 2G control plane check-in
 * 
 *
 * Mar 28 2009 mtk00563
 * [MAUI_01656040] [SSL_AGPS] Network create fail when input label
 * 
 *
 * Aug 29 2008 mtk00563
 * [MAUI_01229667] [AGPS] solve compile error
 * 
 *
 * Aug 8 2008 mtk00563
 * [MAUI_00802242] [AGPS] APGS still working after GPS fixed position
 * 
 *
 * May 2 2008 mtk00563
 * [MAUI_00763856] [AGPS] revise SUPL trace info.
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
 * Feb 25 2008 mtk00563
 * [MAUI_00623349] [AGPS] AGPS feature check-in
 * 
 *
 * 
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef _SUPL_DEF_H
#define _SUPL_DEF_H

#if _FP_CR_SYNC_MAUI_02938153_ != 0
    /* gemini handle */
    //#ifndef __GEMINI__
    //#define SUPL_SIM_SOURCE_NUM 1
    //#else
    //#define SUPL_SIM_SOURCE_NUM GEMINI_PLUS
    //#endif
    #define SUPL_SIM_SOURCE_NUM  2
#endif

/* definition */
#define SUPL_MAX_SESSION_NUM    5
#define SUPL_MAX_SESSION_Q_NUM  (SUPL_MAX_SESSION_NUM - 1)
#define SUPL_MEM_SIZE           12*1024
#define SUPL_ULP_PORT           7275
#define SUPL_RECV_BUF_SIZE      2048
#define SUPL_MAX_PUSH_SIZE      1024
#define SUPL_CELL_INFO_Q_SIZE   10
/* supp_nwk_type */
#define SUPL_SUPP_NWK_INFO_NONE     0x00
#define SUPL_SUPP_NWK_INFO_WLAN     0x01
#define SUPL_SUPP_NWK_INFO_GSM      0x02
#define SUPL_SUPP_NWK_INFO_WCDMA    0x04
#define SUPL_SUPP_NWK_INFO_CDMA     0x08

/* supp_mea_info */
#define SUPL_SUPP_MEA_INFO_NONE     0x00
#define SUPL_SUPP_MEA_INFO_HISTORIC 0x01
#define SUPL_SUPP_MEA_INFO_NON_SERV 0x02

/* timer */
#define SUPL_TIMER_UT1_VALUE        (10)
#define SUPL_TIMER_UT2_VALUE        (10)    //!!== from 10 to 15 ==
#define SUPL_TIMER_UT3_VALUE        (10)
//C.K. modified-->
//#define SUPL_MAX_CONN_TIME          (90)
//Prevent blocking supl and cause multiple MNL si injected
#define SUPL_MAX_CONN_TIME          (40)
//C.K. modified<--
#define SUPL_TIMER_LCSP_REQ         (300)
#define SUPL_MAX_NMR_NUM            (15)

#define SUPL_INIT_CONTENT_TYPE_STR "vnd.omaloc-supl-init"

typedef void (*supl_conn_send_cb) (kal_uint16 session_id, kal_uint32 result, kal_uint32 cause);
typedef void (*supl_fsm_hdlr) (kal_uint32, kal_uint16, void*);


/* enum */

typedef enum
{
    SUPL_INT_MODULE_SESSION,
    SUPL_INT_MODULE_CONN,
    SUPL_INT_MODULE_TOTAL
} supl_int_module_enum;

typedef enum
{
    SUPL_TIMER_START,
    SUPL_TIMER_UT1,
    SUPL_TIMER_UT2,
    SUPL_TIMER_UT3,
    SUPL_TIMER_CONN,
    SUPL_TIMER_TRIG,
    SUPL_TIMER_VERIFY,
    SUPL_TIMER_LCSP,
    SUPL_TIMER_TOTAL
} supl_timer_enum;

typedef enum
{
    /* MMI */
    SUPL_EVENT_MMI_PUSH_REQ,
    SUPL_EVENT_MMI_STATUS_RSP,
    SUPL_EVENT_MMI_START_REQ,
    SUPL_EVENT_MMI_NOTIFY_RSP,
    SUPL_EVENT_MMI_ABORT_REQ,

    /* LCSP */
    SUPL_EVENT_LCSP_DATA_REQ,
    SUPL_EVENT_LCSP_DATA_RSP,
    SUPL_EVENT_LCSP_LOC_ID_CNF,
    SUPL_EVENT_LCSP_MULTI_LOC_ID_CNF,
    SUPL_EVENT_LCSP_RESET_IND, /* for CP */

    /* GPS */
    SUPL_EVENT_GPS_POS_RSP,

    /* ULP MSG */
    SUPL_EVENT_ULP_RECV_MSG,

    /* CONN */
    SUPL_EVENT_CONN_CREATE_CNF,
    SUPL_EVENT_CONN_SEND_CNF,
    SUPL_EVENT_CONN_FAIL_IND,

    /* TIMER */
    SUPL_EVENT_TIMER_EXPIRY,

    SUPL_EVENT_CONN_CREATE,
    SUPL_EVENT_CONN_SEND,

    SUPL_EVENT_END
} supl_event_enum;
typedef enum
{
    SUPL_SESSION_STATE_TERMINATED,
    SUPL_SESSION_STATE_INIT,
    SUPL_SESSION_STATE_CREATE_CONN,
  #if 0 //__ULP_VER_1_5__
    SUPL_SESSION_STATE_GET_LOC,    // This is used only in __ULP_VER_1_5__
  #endif
    SUPL_SESSION_STATE_WAIT_RESP,
    SUPL_SESSION_STATE_POS_INIT,
    SUPL_SESSION_STATE_RECV_POS,
  #if 0 //__ULP_VER_1_5__
    SUPL_SESSION_STATE_GET_POS,    // This is used only if SUPL_SESSION_STATE_GET_LOC is used
  #endif
    SUPL_SESSION_STATE_WAIT_SEND_CNF,
  #if 0 //__ULP_VER_1_5__
    SUPL_SESSION_STATE_GET_MULTI_LOC,
    SUPL_SESSION_STATE_WAIT_NOTIFY_RSP,
    SUPL_SESSION_STATE_TRIG_WAIT_RESP,
    SUPL_SESSION_STATE_TRIG_PROC,
    SUPL_SESSION_STATE_TRIG_SUSPEND,
  #endif
    SUPL_SESSION_STATE_CONN_UT,
    SUPL_SESSION_STATE_TOTAL
} supl_session_state_enum;

typedef enum
{
    SUPL_CONN_STATE_TERMINATED,
    SUPL_CONN_STATE_SOC_CONNECT,
    SUPL_CONN_STATE_GET_DNS,
    SUPL_CONN_STATE_TLS_CREATE,
    SUPL_CONN_STATE_TLS_HANDSHAKE,
    SUPL_CONN_STATE_ACTIVATED,
    SUPL_CONN_STATE_BLOCKED,
    SUPL_CONN_STATE_TERMINATING,
    SUPL_CONN_STATE_TOTAL
} supl_conn_state_enum;

typedef enum
{
    SUPL_CONN_EVENT_CREATE_REQ,
    SUPL_CONN_EVENT_DNS_RESOLVED,
    SUPL_CONN_EVENT_DNS_FAIL,
    SUPL_CONN_EVENT_SOC_CONNECTED,
    SUPL_CONN_EVENT_SOC_CONNECT_FAIL,
    SUPL_CONN_EVENT_TLS_READY,
    SUPL_CONN_EVENT_TLS_DONE,
    SUPL_CONN_EVENT_SEND_REQ,
    SUPL_CONN_EVENT_READY_TO_SEND_IND,
    SUPL_CONN_EVENT_RECV_IND,
    SUPL_CONN_EVENT_CLOSE_REQ,
    SUPL_CONN_EVENT_FAIL_IND,
    SUPL_CONN_EVENT_END
} supl_conn_event_enum;

typedef enum
{
    SUPL_SESSION_SI,
    SUPL_SESSION_SI_PENDING,
    SUPL_SESSION_NI,
    SUPL_SESSION_NI_PENDING,
    SUPL_SESSION_NI_ENDING,
    SUPL_SESSION_NI_TRIG,
    SUPL_SESSION_NI_TRIG_REPEAT,
    SUPL_SESSION_END
} supl_session_type_enum;

typedef enum
{
    SUPL_SET_ID_MSISDN,
    SUPL_SET_ID_MDN,
    SUPL_SET_ID_MIN,
    SUPL_SET_ID_IMSI,
    SUPL_SET_ID_NAI,
    SUPL_SET_ID_IP_ADDR_IPv4,
    SUPL_SET_ID_IP_ADDR_IPv6,
    SUPL_SET_ID_END
} supl_set_id_enum;

typedef enum
{
    SUPL_SLP_ID_IPv4_ADDR,
    SUPL_SLP_ID_IPv6_ADDR,
    SUPL_SLP_ID_FQDN,
    SUPL_SLP_ID_END
} supl_slp_id_enum;

typedef enum
{
    SUPL_CLOSE_REASON_NONE,
    SUPL_CLOSE_REASON_INVALID_MSG,
    SUPL_CLOSE_REASON_INVALID_MSG_LCSP,
    SUPL_CLOSE_REASON_TIMER_EXPIRY,
    SUPL_CLOSE_REASON_TIMERR_EXPIRY_LCSP,
    SUPL_CLOSE_REASON_USER_REJECT,
    SUPL_CLOSE_REASON_END
} supl_close_reason_enum;

typedef enum
{
    SUPL_CELL_INFO_GSM,
    SUPL_CELL_INFO_WCDMA,
    SUPL_CELL_INFO_TOTAL
} supl_cell_info_type_enum;

#if _FP_CR_SYNC_MAUI_03018028_ != 0
typedef enum
{
    SUPL_ECELL_INFO_GSM,
    SUPL_ECELL_INFO_WCDMA,
    SUPL_ECELL_INFO_TOTAL
} supl_ecell_info_type_enum;
#endif

typedef enum
{
    SUPL_CELL_STATUS_STALE,
    SUPL_CELL_STATUS_CURRENT,
    SUPL_CELL_STATUS_UNKNOWN,
    SUPL_CELL_STATUS_END
} supl_cell_status_enum;

typedef enum
{
    SUPL_LCSP_RRLP,
    SUPL_LCSP_RRC,
    SUPL_LCSP_END
} supl_lcsp_type_enum;

/***************************************************************************** 
* Assert Macros
*****************************************************************************/	
#define SUPL_SYS_ASSERT(x)              ASSERT(x)
#define SUPL_SYS_EXT_ASSERT(x,y1,y2,y3) EXT_ASSERT(x,y1,y2,y3)

#if defined(__PRODUCTION_RELEASE__)
#define SUPL_ASSERT(x)                  {if (!(x)) {longjmp (supl_p->jump_buffer, 1);} }
#define SUPL_EXT_ASSERT(x,y1,y2,y3)     {if (!(x)) {longjmp (supl_p->jump_buffer, 1);} }
#else
#define SUPL_ASSERT(x)                  ASSERT(x)
#define SUPL_EXT_ASSERT(x,y1,y2,y3)     EXT_ASSERT(x,y1,y2,y3)
#endif


#endif /* _SUPL_DEF_H */

