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
 *  typedef.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   The AGPS SWIP adaption layer.
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
 * 03 17 2012 archilis.wang
 * [ALPS00254052] [Need Patch] [Volunteer Patch] Add the code for eCID
 * .
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
 /* data type definition */
#ifndef __TYPEDEF_H__
#define __TYPEDEF_H__

#include <memory.h>
#include "FPCRSync.h"

//C.K. moved
#ifdef __CDMA_AGPS_SUPPORT__
#ifndef uint32
	typedef unsigned int uint32;
#endif

#ifndef uint16
	typedef unsigned short uint16;
#endif

#ifndef uint8
	typedef unsigned char uint8;
#endif
#endif
//C.K. moved

typedef  unsigned char  kal_uint8;
typedef  unsigned short kal_uint16;
typedef  unsigned int   kal_uint32;

typedef  signed char    kal_int8;
typedef  signed short   kal_int16;   
typedef  signed int     kal_int32;
typedef  char           kal_char;
typedef  char           kal_wchar;

typedef void *KAL_ADM_ID;
//typedef signed short   kal_status;

typedef  unsigned char  U8;
typedef  unsigned short U16;
typedef  unsigned int   U32;
typedef  unsigned int   UINT;

typedef  unsigned short int *PU16;

#if 0 //!!Chiwei
typedef  unsigned short WCHAR;
#else
typedef  char           WCHAR;
#endif

#define  Bool kal_bool

//!!==  Chiwei [2010/10/23]: S8 should have signed modifier ==
typedef  signed char    S8;
typedef  signed short   S16;   
typedef  signed int     S32;

typedef void (* kal_timer_func_ptr)(void *);
typedef enum {
    uart_port1=0,
    uart_port2,
    uart_port3,
    uart_port_irda,
    uart_port_usb,
    uart_port_bluetooth,
    uart_port_swdbg,
    uart_max_port,      
    uart_port_null = 99 /* a dummy port for those who doesn't use physical port */
} UART_PORT;

typedef enum 
{
  MMI_FALSE,
  MMI_TRUE
} MMI_BOOL;

#define NU_OR                 0
#define NU_OR_CONSUME         1
#define NU_SUSPEND            0xFFFFFFFFUL

#define KAL_OR                NU_OR
#define KAL_OR_CONSUME        NU_OR_CONSUME
#define KAL_SUSPEND           NU_SUSPEND


#ifndef NULL
#define NULL            0
#endif

typedef enum 
{
  KAL_FALSE,
  KAL_TRUE
} kal_bool;

typedef enum
{
    MOD_NIL,
    MOD_SUPL,
    MOD_SUPL_CONN,
    MOD_RRLP,
    MOD_UAGPS_UP,
    MOD_GPS,
    MOD_MMI,
    MOD_SOC,
    MOD_TLS,
    MOD_PMTK,
    MOD_AS,
    MOD_L4C,
    MOD_TST_READER,
    MOD_SIM,
    MOD_GPSTASK,    //add for CP,Baochu
    MOD_MMI_2_CCCI, //add for CP,Baochu 
    MOD_CNT,
    MOD_MMI_MEDIA_APP  // Used by kal_wap_trace() only. But our implementation does not care about mod_id
} module_enum;

typedef enum
{
    GPS_SUPL_SAP,
    MMI_MMI_SAP,
    SUPL_LCSP_SAP,
    SUPL_MMI_SAP,
    SUPL_INT_SAP,
    LCSP_APP_SAP,
    GPS_LCSP_SAP,
    //add by Baochu,Wang
    MMI_L4C_SS_SAP,
    MMI_GPSTASK_SAP,
} sap_enum;

typedef enum
{
    MSG_ID_SUPL_MMI_PUSH_REQ,
    MSG_ID_SUPL_MMI_STATUS_IND,
    MSG_ID_SUPL_MMI_STATUS_RSP,
    MSG_ID_SUPL_MMI_NOTIFY_IND,
    MSG_ID_SUPL_MMI_NOTIFY_CNF,
    MSG_ID_SUPL_MMI_START_REQ,
    MSG_ID_SUPL_MMI_ABORT_REQ,
    MSG_ID_SUPL_MMI_ABORT_CNF,

    MSG_ID_LCSP_START_REQ,
    MSG_ID_LCSP_END_REQ,
    MSG_ID_LCSP_LOCATION_ID_REQ,
    MSG_ID_LCSP_LOCATION_ID_CNF,
    MSG_ID_SUPL_LCSP_DATA_IND,
    MSG_ID_SUPL_LCSP_DATA_RSP,
    MSG_ID_SUPL_LCSP_DATA_REQ,
    MSG_ID_SUPL_LCSP_DATA_CNF,
    MSG_ID_SUPL_LCSP_ABORT_REQ,

    /* Internal Messages */
    MSG_ID_SUPL_CONN_CREATE_REQ,
    MSG_ID_SUPL_CONN_CREATE_CNF,
    MSG_ID_SUPL_CONN_SEND_REQ,
    MSG_ID_SUPL_CONN_SEND_CNF,
    MSG_ID_SUPL_CONN_RECV_IND,
    MSG_ID_SUPL_CONN_FAIL_IND,
    MSG_ID_SUPL_CONN_CLOSE_REQ,
    MSG_ID_SUPL_CONN_CLOSE_CNF,

    MSG_ID_APP_SOC_GET_HOST_BY_NAME_IND,
    MSG_ID_APP_SOC_NOTIFY_IND,
    MSG_ID_APP_TLS_NOTIFY_IND,
    MSG_ID_APP_SOC_BEARER_INFO_IND,
    MSG_ID_APP_SOC_DEACTIVATE_CNF,
    MSG_ID_APP_TLS_INVALID_CERT_IND,    
    MSG_ID_APP_TLS_CLIENT_AUTH_IND,
    MSG_ID_APP_TLS_ALERT_IND,
    MSG_ID_APP_CBM_BEARER_INFO_IND,

    MSG_ID_RR_RRLP_CELL_INFO_REPORT,

    //== add in porting ==//
    MSG_ID_MMI_MMI_START_TIMER,
    MSG_ID_MMI_GPS_MGR_OPEN_GPS,
    MSG_ID_MMI_GPS_MGR_CLOSE_GPS,
    MSG_ID_MMI_UART_SEND_ALL_ASSIST_IND, // inform MMI that SI/MB has sent all assist data
    //==
    
    MSG_ID_GPS_UART_OPEN_REQ = 500, /*MSG_ID_GPS_MSG_CODE_BEGIN,*/
    MSG_ID_GPS_UART_READ_REQ,    
    MSG_ID_GPS_UART_WRITE_REQ,    
    MSG_ID_GPS_UART_CLOSE_REQ,
    MSG_ID_GPS_UART_NMEA_LOCATION,   
    MSG_ID_GPS_UART_NMEA_SENTENCE,   
    MSG_ID_GPS_UART_RAW_DATA,   
    MSG_ID_GPS_UART_DEBUG_RAW_DATA,   
    MSG_ID_GPS_UART_P_INFO_IND,   
    MSG_ID_GPS_UART_OPEN_SWITCH_REQ,
    MSG_ID_GPS_UART_CLOSE_SWITCH_REQ, 

    MSG_ID_GPS_POS_GAD_CNF,
    MSG_ID_GPS_LCSP_MSG_CODE_BEGIN = MSG_ID_GPS_POS_GAD_CNF,
    MSG_ID_GPS_LCSP_MEAS_GAD_CNF,
    MSG_ID_GPS_LCSP_ASSIST_DATA_CNF,
    MSG_ID_GPS_LCSP_MSG_CODE_END = MSG_ID_GPS_LCSP_ASSIST_DATA_CNF,
    MSG_ID_GPS_POS_GAD_REQ,
    MSG_ID_GPS_LCSP_MEAS_GAD_REQ,
    MSG_ID_GPS_LCSP_ASSIST_DATA_REQ,
    MSG_ID_GPS_LCSP_ABORT_REQ,

    MSG_ID_GPS_ASSIST_BIT_MASK_IND,
    MSG_ID_GPS_LCT_POS_REQ,
    MSG_ID_GPS_LCT_POS_RSP,
    MSG_ID_GPS_LCT_OP_ERROR,

    //!! bugfix for pmtk thread to start gps timer
    MSG_ID_GPS_UART_SEND_ALL_ASSIST_IND,
#if 1 //hiki, update history position for SI/MB
    MSG_ID_GPS_UART_UPDATE_HIS_POS_FOR_SIMB, //for TAS IOT Phase3
#endif

    /* RTC -> GPS */
    MSG_ID_RTC_GPS_TIME_CHANGE_IND,   
    /* GPS EINT HISR -> GPS */
    MSG_ID_GPS_HOST_WAKE_UP_IND,

    /* L4C */
    MSG_ID_L4C_NBR_CELL_INFO_REG_REQ,
    MSG_ID_L4C_NBR_CELL_INFO_REG_CNF,
    MSG_ID_L4C_NBR_CELL_INFO_DEREG_REQ,
    MSG_ID_L4C_NBR_CELL_INFO_DEREG_CNF,
    MSG_ID_L4C_NBR_CELL_INFO_IND,

    /*UART*/
    MSG_ID_UART_READY_TO_READ_IND,
    MSG_ID_UART_PLUGOUT_IND,

    MSG_ID_SIM_READY_IND
} mtk_msg_enum;


typedef enum
{
   TRACE_FUNC,
   TRACE_STATE,
   TRACE_INFO,
   TRACE_WARNING,
   TRACE_ERROR,
   TRACE_GROUP_1,
   TRACE_GROUP_2,
   TRACE_GROUP_3,
   TRACE_GROUP_4,
   TRACE_GROUP_5,
   TRACE_GROUP_6,
   TRACE_GROUP_7,
   TRACE_GROUP_8,
   TRACE_GROUP_9,
   TRACE_GROUP_10,
   TRACE_PEER
}trace_class_enum;

#define MMI_MEDIA_TRC_G2_APP    TRACE_GROUP_2


typedef unsigned int    msg_type;
typedef unsigned int    sap_type;
typedef unsigned int    module_type;

#if 0
typedef struct 
{
    kal_uint8 unused;
} *kal_eventgrpid;
#else
typedef kal_uint32 kal_eventgrpid;
#endif

#if 0
typedef struct
{
    kal_uint8 unused;
} *kal_mutexid;
#else
typedef kal_uint32 kal_mutexid;
#endif
typedef struct __rtc 
{
    kal_uint8 rtc_sec;    /* seconds after the minute   - [0,59]  */
    kal_uint8 rtc_min;    /* minutes after the hour     - [0,59]  */
    kal_uint8 rtc_hour;   /* hours after the midnight   - [0,23]  */
    kal_uint8 rtc_day;    /* day of the month           - [1,31]  */
    kal_uint8 rtc_mon;    /* months                     - [1,12]  */
    kal_uint8 rtc_wday;   /* days in a week             - [1,7]   */
    kal_uint8 rtc_year;   /* years                      - [0,127] */
} t_rtc;

typedef S32 MDI_RESULT;

typedef enum {
    TD_UL = 0x01 << 0,   /* Uplink Direction */
    TD_DL = 0x01 << 1,   /* Downlink Direction */
    TD_CTRL = 0x01 << 2, /* Control Plane. Both directions */
    TD_RESET = 0x01 << 3 /* Reset buffer content to 0 */
} transfer_direction;

#define kal_sprintf     sprintf
#define kal_mem_cpy     sys_mem_cpy
#define kal_mem_set     sys_mem_set
#define kal_mem_cmp     sys_mem_cmp

#define EXT_ASSERT(exp, para1, para2, para3)  sys_ext_assert((kal_bool)exp, (int)para1, para2, para3)
#define ASSERT(exp)      sys_assert((kal_bool)(exp), __FILE__, __LINE__)

#if 0   // AGPS_SWIP
#define BEGIN_TRACE_MAP(MOD) char *trace_table_##MOD[]={
#define END_TRACE_MAP(MOD) };
#define TRC_MSG(TRC_IDX,TRACE) TRACE,
#define TRC_TBL(MOD) trace_table_##MOD
#else
//#define BEGIN_TRACE_MAP(MOD) kal_uint8* ##MOD_TRACE[]={
//#define END_TRACE_MAP(MOD) } ;
#define _MOD_NAME(M) _##M
#define BEGIN_TRACE_MAP(MOD) typedef enum{
#define END_TRACE_MAP(MOD) }_MOD_NAME(MOD);//$$_trace_enum;
#define TRC_MSG(TRC_IDX,TRACE) TRC_IDX,
#endif

typedef enum{
    RAT_NONE     = 0, /* Ripple: Do NOT modify the sequence and values. */
    RAT_GSM      = 1,
    RAT_UMTS     = 2,
    RAT_GSM_UMTS = 3
}rat_enum;


#endif /* __TYPEDEF_H__ */
