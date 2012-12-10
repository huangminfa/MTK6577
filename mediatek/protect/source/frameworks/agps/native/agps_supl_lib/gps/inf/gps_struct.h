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
 *  gps_struct.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This file is intends for local parameter declaration about GIS sap
 *
 * Author:
 * -------
 *  Hai Wang (MBJ06018)
 *
 *==============================================================================
 * 				HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *------------------------------------------------------------------------------
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * Aug 6 2009 mtk80018
 * [MAUI_01886224] [AGPS 2G CP] RRLP R5
 * 
 *
 * May 23 2008 mbj06018
 * [MAUI_01046192] [AGPS]Check in RTC syn EM104 return can write at mdi_gps
 * 
 *
 * May 23 2008 mbj06018
 * [MAUI_01046026] [GIS_Sunavi]Check in BT GPS support
 * 
 *
 * Apr 17 2008 mbj06018
 * [MAUI_00655816] [AGPS]AGPS check in
 * 
 *
 * Dec 31 2007 mtk01813
 * [MAUI_00590473] Update feature options of makefile
 * help Wanghai to check-in these two file at interface\gps
 *
 * Nov 30 2007 mtk01206
 * [MAUI_00574860] [GPS][Modify] Update some GPS task files
 * 
 *
 * Nov 12 2007 mtk01206
 * [MAUI_00574860] [GPS][Modify] Update some GPS task files
 * 
 *
 * Nov 5 2007 mtk01206
 * [MAUI_00571439] [GPS][New] Add new GPS task for QC
 * 
 *
 * Nov 5 2007 mtk01206
 * [MAUI_00571439] [GPS][New] Add new GPS task for QC
 * 
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!! 
 *==============================================================================
 *******************************************************************************/
#ifndef __GPS_STRUCT_H__
#define __GPS_STRUCT_H__

#ifdef __GPS_SUPPORT__

#ifndef __AGPS_SWIP_REL__
#include "rtc_sw.h"
#endif

#endif
#include "gps2lcsp_enum.h"


typedef enum
{
    GPS_PARSER_P_VERSION = 0,
    GPS_PARSER_P_GPS_MODE_SWITCH_OK,
    GPS_PARSER_P_SYSTEM_START_OK,
    GPS_PARSER_P_INTERNAL_VERSION,
    GPS_PARSER_P_GPS_FIXED,
    GPS_PARSER_P_GPS_BT_VPORT_LOST,
    GPS_PARSER_P_GPS_BT_VPORT_CONN,
    GPS_PARSER_P_GPS_FIXED_AND_TIME_OK,
    GPS_PARSER_P_END
} gps_parser_p_info_enum;


typedef struct 
{
    LOCAL_PARA_HDR
    kal_eventgrpid  event_id;
    module_type     module_id;
    kal_uint16      port; 
    kal_int16       mode; 
    kal_int32      *return_val;
}gps_uart_open_req_struct;

typedef struct 
{
    LOCAL_PARA_HDR
    kal_eventgrpid  event_id;
    module_type     module_id;
    kal_uint16      port; 
}gps_uart_read_req_struct;

typedef struct 
{
    LOCAL_PARA_HDR
    kal_eventgrpid  event_id;
    module_type     module_id;
    kal_uint16      port; 
    kal_bool        is_rawdata; 
    kal_char*       buffer; 
    kal_uint32      length; 
    kal_int16       cmd; 
    kal_int32      *return_val;
    kal_uint32     *return_written;
}gps_uart_write_req_struct;

typedef struct 
{
    LOCAL_PARA_HDR
    kal_eventgrpid  event_id;
    module_type     module_id;
    kal_uint16      port; 
    kal_int16       mode; /*mdi_gps_uart_work_mode_enum*/
}gps_uart_close_req_struct;

typedef struct 
{
    LOCAL_PARA_HDR
    kal_uint16      port;
    kal_int16       type;   /*mdi_gps_parser_info_enum*/
    kal_char*       buffer; 
}gps_uart_nmea_location_struct;


typedef struct 
{
    LOCAL_PARA_HDR
    kal_uint32      length;
    kal_uint16      port;
    kal_char*       buffer; 
}gps_uart_nmea_sentence_struct;


typedef struct 
{
    LOCAL_PARA_HDR
    kal_uint16      port;
    module_type  module_id;
}gps_uart_raw_data_struct;


typedef struct 
{
    LOCAL_PARA_HDR
    kal_uint16      port;
}gps_uart_debug_raw_data_struct;

typedef struct
{            
    kal_int8        hour;
    kal_int8        minute;
    kal_int8        second;
    kal_int8        millisecond;
} gps_nmea_utc_time_struct;

typedef struct 
{
    kal_int8        year;
    kal_int8        month;
    kal_int8        day;
} gps_nmea_utc_date_struct;


typedef struct
{            
    gps_nmea_utc_time_struct    utc_time;
    gps_nmea_utc_date_struct    utc_date;
} gps_p_info_gps_fix_struct;


typedef struct
{            
    kal_uint16      port;
} gps_p_info_gps_vport_struct;



typedef union {
    gps_p_info_gps_fix_struct   gps_fix;
    gps_p_info_gps_vport_struct vport;
} gps_p_info_union;


typedef struct 
{
    LOCAL_PARA_HDR
    gps_parser_p_info_enum      type;
    gps_p_info_union            p_info;
}gps_uart_p_info_ind_struct;


#ifdef __GPS_SUPPORT__
typedef struct 
{
    LOCAL_PARA_HDR
    t_rtc previous_rtc;
    t_rtc new_rtc;
}rtc_gps_time_change_ind_struct;
#endif


typedef struct 
{
    LOCAL_PARA_HDR
    gps_error_code_enum error_code;
} gps_lct_op_error_struct;


typedef struct 
{
    LOCAL_PARA_HDR
    kal_uint16  bitmap;
} gps_assist_bit_mask_ind_struct;

typedef struct 
{
    LOCAL_PARA_HDR
    kal_uint16  port; /*set to GPS_DEFAULT_PORT*/
    kal_int16   prefer_gps_mode; /*mb ma*/
}gps_uart_open_switch_req_struct;

typedef struct 
{
    LOCAL_PARA_HDR
    kal_uint16  port; /*set to GPS_DEFAULT_PORT*/
}gps_uart_close_switch_req_struct;

#ifdef __AGPS_SWIP_REL__

typedef struct  
{       
    kal_uint8       ref_count;
    kal_uint16      msg_len;        /* LOCAL_PARA_HDR */	
    UART_PORT       port;
}uart_ready_to_read_ind_struct; 
#endif /* __AGPS_SWIP_REL__ */

#endif /*__GPS_STRUCT_H__*/

