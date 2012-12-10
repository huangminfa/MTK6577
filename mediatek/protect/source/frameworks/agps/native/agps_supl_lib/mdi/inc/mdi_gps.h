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
 *  mdi_gps.h
 *
 * Project:
 * --------
 *  MAUI
 *
 * Description:
 * ------------
 *  GPS related interface hand file, such as GPS uart and NMEA parser
 *
 * Author:
 * -------
 *  Ham Wang (MTK80018)
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
 * Mar 17 2009 mtk80018
 * [MAUI_01409525] [Popup rule]MDI and camera popup rule check in
 * 
 *
 * Aug 5 2008 mbj06078
 * [MAUI_01096918] [check in] Category267
 * 
 *
 * Jun 25 2008 mbj06018
 * [MAUI_01081074] GPS modify head file for vendor support
 * 
 *
 * Jun 19 2008 mbj06018
 * [MAUI_00789313] [AGPS] Fatal Error (421, 1) - TR
 * 
 *
 * Jun 12 2008 mbj06018
 * [MAUI_01046026] [GIS_Sunavi]Check in BT GPS support
 * 
 *
 * Jun 5 2008 mbj06018
 * [MAUI_01065164] [GPS] Add a switch of GPS power saving in engineer mode
 * 
 *
 * May 23 2008 mbj06018
 * [MAUI_01046192] [AGPS]Check in RTC syn EM104 return can write at mdi_gps
 * 
 *
 * May 12 2008 mbj06018
 * [MAUI_00769497] [Mapbar_Navi] Fatal Error (305): msg_send_ext_queue() failed (88880026) - L4
 * 
 *
 * Apr 17 2008 mbj06018
 * [MAUI_00655816] [AGPS]AGPS check in
 * 
 *
 * Apr 14 2008 mbj06018
 * [MAUI_00654075] [GIS]Check in RAW data callback one sentence
 * 
 *
 * Mar 5 2008 mbj06018
 * [MAUI_00633684] [GIS]check in gps structure modify
 * 
 *
 * Jan 26 2008 mbj06018
 * [MAUI_00521341] [GIS][Sunavi]patch vendor lib E
 * 
 *
 * Jan 11 2008 mbj06018
 * [MAUI_00515786] [GIS][Sunavi][Check in]08.02 to 07b maui with new lib
 * 
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 *******************************************************************************/

#ifndef _MDI_GPSUART_H_
#define _MDI_GPSUART_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "mtk_service.h"
#include "sys_serv_adp.h"

#if defined(__GPS_SUPPORT__)||defined(__BT_GPS_SUPPORT__)

/***************************************************************************** 
* Include
*****************************************************************************/
/***************************************************************************** 
* Define
*****************************************************************************/
#define MDI_GPS_UART_PORT_MAX               3
#define MDI_GPS_UART_APP_PER_PORT_MAX       5
#define MDI_GPS_DUMMY_HANDLE                0xFF
#define MDI_GPS_DUMMY_PORT                  0xFF

#define MDI_GPS_NMEA_CATCHER_LOGGING_FLAG       (1<<0)
#define MDI_GPS_NMEA_FILE_LOGGING_FLAG          (1<<1)
#define MDI_GPS_NMEA_AGENT_LOGGING_FLAG         (1<<2)
#define MDI_GPS_NMEA_DEBUG_INFO_FLAG            (1<<3)
#define MDI_GPS_NMEA_POWER_SAVING_FLAG          (1<<4)
#define MDI_GPS_NMEA_LOGGING_DEFAULT_FLAG       (0x01)
#define MDI_GPS_NMEA_MESSAGE_MAX_LENGTH         (100)
#define MDI_GPS_NMEA_FILENAME_LENGTH            (60)
#define MDI_GPS_NMEA_FILE_LOGGING_WRITE_TIME    (20)
#define MDI_GPS_NMEA_FILE_DEBUGINOF_WRITE_TIME  (200)

#define MDI_GPS_RAW_DATA_PER_CB            (256)
#define MDI_GPS_RAW_DATA_PER_EXTEND             (1024*5)

#define MDI_GPS_NMEA_MAX_SVVIEW             20
#define MDI_GPS_PARSER_P_VERSION_MAX        32
#define MDI_GPS_OPEN_MODE_ERROR             0


/***************************************************************************** 
* Typedef 
*****************************************************************************/
// AGPS_SWIP_PORTING
typedef enum
{
    MDI_GPS_PARSER_RAW_DATA = 0,
    MDI_GPS_PARSER_NMEA_GGA,
    MDI_GPS_PARSER_NMEA_GLL,
    MDI_GPS_PARSER_NMEA_GSA,
    MDI_GPS_PARSER_NMEA_GSV,
    MDI_GPS_PARSER_NMEA_RMC,
    MDI_GPS_PARSER_NMEA_VTG,
    MDI_GPS_PARSER_NMEA_PMTK010,
    MDI_GPS_PARSER_NMEA_SENTENCE,
    MDI_GPS_UART_EVENT_VPORT_LOST, //virtual port is lost, maybe bluetooth connection is break
    MDI_GPS_SHOW_AGPS_ICON,
    MDI_GPS_HIDE_AGPS_ICON,

    MDI_GPS_SWITCH_MODE_TIMEOUT,
    MDI_GPS_POS_EST,
    MDI_GPS_RESPONSE_TIME,
    MDI_GPS_SIMA_START,
    MDI_GPS_SIMA_START_RE_AID,
    MDI_GPS_SIMB_START,
    MDI_GPS_SIMB_START_RE_AID,
    MDI_GPS_SIMA_FINISH,
    MDI_GPS_SIMA_FINISH_RE_AID,    
    MDI_GPS_SIMB_FINISH,
    MDI_GPS_SIMB_FINISH_RE_AID,       
    MDI_GPS_PARSER_NMEA_END
} mdi_gps_parser_info_enum;
/*
typedef enum
{
    MDI_GPS_PARSER_RAW_DATA = 0,
    MDI_GPS_PARSER_NMEA_GGA,
    MDI_GPS_PARSER_NMEA_GLL,
    MDI_GPS_PARSER_NMEA_GSA,
    MDI_GPS_PARSER_NMEA_GSV,
    MDI_GPS_PARSER_NMEA_RMC,
    MDI_GPS_PARSER_NMEA_VTG,
    MDI_GPS_PARSER_NMEA_SENTENCE,
    MDI_GPS_UART_EVENT_VPORT_LOST, //virtual port is lost, maybe bluetooth connection is break
    MDI_GPS_SHOW_AGPS_ICON,
    MDI_GPS_HIDE_AGPS_ICON,
    MDI_GPS_PARSER_NMEA_END
} mdi_gps_parser_info_enum;
*/

typedef enum
{
    MDI_GPS_PARSER_P_VERSION = 0,
    MDI_GPS_PARSER_P_GPS_MODE_SWITCH_OK,
    MDI_GPS_PARSER_P_SYSTEM_START_OK,
    MDI_GPS_PARSER_P_INTERNAL_VERSION,
    MDI_GPS_PARSER_P_GPS_FIXED,
    MDI_GPS_PARSER_P_GPS_FIXED_AND_TIME_OK,
    MDI_GPS_PARSER_P_END
} mdi_gps_parser_p_info_enum;

/*Enum of GPS work mode*/
typedef enum
{
    MDI_GPS_UART_MODE_RAW_DATA = 0,         /*Just need raw data*/
    MDI_GPS_UART_MODE_LOCATION,             /*Just need location*/
    MDI_GPS_UART_MODE_LOCATION_WITH_QOP     /*Need AGPS data with Qop*/
} mdi_gps_uart_work_mode_enum;

/*Enum of GPS command*/
typedef enum
{
    MDI_GPS_UART_GPS_WARM_START = 0,        /*Let GPS do warm start*/
    MDI_GPS_UART_GPS_HOT_START,             /*Let GPS do hot start*/
    MDI_GPS_UART_GPS_COLD_START,            /*Let GPS do cold start*/
    MDI_GPS_UART_GPS_VERSION,
    MDI_GPS_UART_GPS_ENABLE_DEBUG_INFO,    
    MDI_GPS_UART_GPS_SWITCH_MODE_MA,
    MDI_GPS_UART_GPS_SWITCH_MODE_MB,
    MDI_GPS_UART_GPS_SWITCH_MODE_NORMAL,
    MDI_GPS_UART_GPS_QUERY_POS,
    MDI_GPS_UART_GPS_QUERY_MEAS,
    MDI_GPS_UART_GPS_CLEAR_NVRAM,           /*Clear GPS NVRAM*/
    MDI_GPS_UART_GPS_AGPS_START,            /*Clear GPS data*/
    MDI_GPS_UART_GPS_SLEEP,                 /*Let GPS chip goto sleep mode*/
    MDI_GPS_UART_GPS_STOP,                  /*Let GPS chip stop*/
    MDI_GPS_UART_GPS_WAKE_UP,               /*Let GPS chip wake up from sleep mode*/
    MDI_GPS_UART_GPS_DUMMY = -1
} mdi_gps_uart_cmd_type_enum;

typedef struct
{            
    kal_int8      hour;
    kal_int8      minute;
    kal_int8      second;
    kal_int8      millisecond;
} mdi_gps_nmea_utc_time_struct;

typedef struct
{            
    kal_int8      year;
    kal_int8      month;
    kal_int8      day;
} mdi_gps_nmea_utc_date_struct;

/*GPGGA -- Global Positioning System Fix Data*/
typedef struct
{            
    double  latitude;           /*Latitude South<0  North>0*/
    double  longitude;          /*Longitude West<0 east>0*/
    float   h_precision;       /*Horizontal Dilution of precision*/
    double  altitude;          /*Antenna Altitude above/below mean-sea-level (geoid)*/
    float   unit_of_altitude;  /*Units of antenna altitude, meters*/
    float   geoidal ;          /*Geoidal separation, the difference between the WGS-84 earth*/
    float   unit_of_geoidal;   /*Units of geoidal separation, meters*/
    float   gps_age;           /*Age of differential GPS data, time in seconds since last SC104*/
    kal_uint16    station_id;        /*Differential reference station ID, 0000-1023*/
    kal_uint8     sat_in_view;       /*Number of satellites in view*/
    mdi_gps_nmea_utc_time_struct        utc_time;     /*Time (UTC)*/
    kal_int8      north_south;       /*north or south*/
    kal_int8      east_west;         /*east or west*/
    kal_int8      quality;           /*GPS Quality Indicator*/
} mdi_gps_nmea_gga_struct;


 /*GPGSA -- GNSS DOP and Active Satellites*/
typedef struct 
{           
    float   pdop;
    float   hdop;
    float   vdop;
    kal_uint16     sate_id[12];
    kal_int8      op_mode;
    kal_int8      fix_mode;
} mdi_gps_nmea_gsa_struct;


/*GPGSV -- GNSS Satellites in View*/
typedef struct 
{            
    kal_int16     msg_sum;
    kal_int16     msg_index;
    kal_int16     sates_in_view;
    kal_int16     max_snr;
    kal_int16     min_snr;
    kal_int16     num_sv_trk;
    struct
    {
        kal_uint8 sate_id;
        kal_uint8 elevation;
        kal_int16 azimuth;
        kal_uint8 snr;
    }       rsv[MDI_GPS_NMEA_MAX_SVVIEW];
} mdi_gps_nmea_gsv_struct;


/*GPRMC -- Recommended Minimum Specific GNSS Data*/
typedef struct
{            
    double   latitude;
    double   longitude;
    float   ground_speed;   /*Speed over ground, knots*/
    float   trace_degree;    /*Track mode degrees,north is 0*/
    float   magnetic;
    mdi_gps_nmea_utc_time_struct      utc_time;
    mdi_gps_nmea_utc_date_struct      utc_date;
    kal_int8      status;
    kal_int8      north_south;
    kal_int8      east_west;
    kal_int8      magnetic_e_w;
    kal_int8      cmode;
} mdi_gps_nmea_rmc_struct;


/*GPGLL -- Geographic Position - Latitude/Longitude*/
typedef struct
{            
    double  latitude;            
    double  longitude;           
    mdi_gps_nmea_utc_time_struct      utc_time; 
    kal_int8      north_south;
    kal_int8      east_west;
    kal_int8      status;           /*Status A - Data Valid, V - Data Invalid*/
    kal_int8      mode;
} mdi_gps_nmea_gll_struct;


/*GPVTG -- VTG Data*/
typedef struct
{
    float   true_heading;
    float   mag_heading;
    float   hspeed_knot;
    float   hspeed_km;
    kal_int8      mode;
} mdi_gps_nmea_vtg_struct;


/*Version*/
typedef struct
{
    kal_char    version[MDI_GPS_PARSER_P_VERSION_MAX];
    kal_uint32  number;
} mdi_gps_p_release_struct;


typedef struct
{
    mdi_gps_p_release_struct    release;
} mdi_gps_p_info_struct;


typedef struct
{
    module_type     mod_id[MDI_GPS_UART_APP_PER_PORT_MAX];
    void            (*gps_uart_raw_cb[MDI_GPS_UART_APP_PER_PORT_MAX])(mdi_gps_parser_info_enum type, void *buffer, U32 length);           /*uart port number*/
    void            (*gps_uart_lct_cb[MDI_GPS_UART_APP_PER_PORT_MAX])(mdi_gps_parser_info_enum type, void *buffer, U32 length);           /*uart port number*/
    void            (*gps_uart_lct_qop_cb[MDI_GPS_UART_APP_PER_PORT_MAX])(mdi_gps_parser_info_enum type, void *buffer, U32 length);           /*uart port number*/
    S16             port;
    MMI_BOOL        is_opened;
    MMI_BOOL        is_ready_to_write;
    MMI_BOOL        is_data_arrive;
} mdi_gps_uart_handle_struct;


typedef struct 
{
    LOCAL_PARA_HDR
    U32 length; 
    U8  data[MDI_GPS_NMEA_MESSAGE_MAX_LENGTH]; 
}mdi_gps_uart_write_req_struct;





typedef struct 
{
    LOCAL_PARA_HDR
    U32 length; 
    U8  data[MDI_GPS_NMEA_MESSAGE_MAX_LENGTH]; 
}mdi_gps_uart_read_req_struct;


typedef struct
{
    FS_HANDLE           file_handle;            /*store the handle that logging should use*/
    FS_HANDLE           debug_info_file_handle; /*store the debug info that logging should use*/
    U32                 file_pre_allocate;      /*store the number pre-allocated*/
    U32                 debug_info_file_pre_allocate;   /*store the number pre-allocated*/
    U8                  port;            /*store the port nubmer that logging should use*/
    MMI_BOOL            debug_info_file_pre_allocate_failed;      /*store the number pre-allocated*/
    MMI_BOOL            file_pre_allocate_failed;      /*store the number pre-allocated*/
    MMI_BOOL            catcher_logging;
    MMI_BOOL            file_logging;
    MMI_BOOL            agent_logging;
    MMI_BOOL            debug_info;
    MMI_BOOL            power_saving;
    U32                 file_write_count;            
    U32                 debug_info_file_write_count; 
} mdi_gps_logging_ctx_struct;


typedef struct
{
    void (*gga_callback)(mdi_gps_nmea_gga_struct *param);
    void (*gll_callback)(mdi_gps_nmea_gll_struct *param);
    void (*gsa_callback)(mdi_gps_nmea_gsa_struct *param);
    void (*gsv_callback)(mdi_gps_nmea_gsv_struct *param);
    void (*rmc_callback)(mdi_gps_nmea_rmc_struct *param);
    void (*vtg_callback)(mdi_gps_nmea_vtg_struct *param);
    void (*sentence_callback)(const U8 *buffer, U32 length);
    module_type     mod_id;
    U16             port_num;
} mdi_gps_parser_ctx_struct;

    
typedef struct
{
    U32      counter;        /*remember the nmea buffer in bytes*/
    mdi_gps_parser_ctx_struct   parser;
    mdi_gps_logging_ctx_struct  logging;
    mdi_gps_uart_handle_struct  handle[MDI_GPS_UART_PORT_MAX];
    void (*gps_uart_p_callback)(mdi_gps_parser_p_info_enum type);
    void (*gps_uart_p_info_callback)(mdi_gps_parser_p_info_enum type,void *param);
    MMI_BOOL                    is_need_assist_data;
    kal_eventgrpid              event_id;
    kal_mutexid                 mutex;
} mdi_gps_ctx_struct;


/*****************************************************************************              
* Extern Global Variable                                                                    
*****************************************************************************/

/*****************************************************************************              
* Extern Global Function                                                                    
*****************************************************************************/
/*Can export to vendor begin*/
MDI_RESULT mdi_gps_uart_open(
    U16 port,
    mdi_gps_uart_work_mode_enum mode,
    void (*gps_uart_event_callback)(mdi_gps_parser_info_enum type, void *buffer, U32 length));
MDI_RESULT mdi_gps_uart_close(
    U16 port,
    mdi_gps_uart_work_mode_enum mode, 
    void (*gps_uart_event_callback)(mdi_gps_parser_info_enum type, void *buffer, U32 length));
extern S16 mdi_get_gps_port(void);
/*Can export to vendor end*/
    
extern MDI_RESULT   mdi_gps_uart_write(U16 port, void* buffer, U32 length, U32 *write);
extern MDI_RESULT   mdi_gps_uart_cmd(U16 port, mdi_gps_uart_cmd_type_enum cmd, void *param);

extern void         mdi_gps_set_work_port(U8 port);
extern void         mdi_gps_enable_parser(void);
extern MMI_BOOL     mdi_gps_disable_parser(void);
extern MMI_BOOL     mdi_gps_is_parser_enabled(void);

extern void         mdi_gps_nmea_set_catcher_logging(MMI_BOOL param);
extern void         mdi_gps_nmea_set_file_logging(MMI_BOOL param);
extern void         mdi_gps_nmea_set_agent_logging(MMI_BOOL param);
extern void         mdi_gps_nmea_set_debug_info(MMI_BOOL param);
extern void         mdi_gps_nmea_set_power_saving(MMI_BOOL param);
extern MMI_BOOL     mdi_gps_nmea_get_catcher_logging(void);
extern MMI_BOOL     mdi_gps_nmea_get_file_logging(void);
extern MMI_BOOL     mdi_gps_nmea_get_agent_logging(void);
extern MMI_BOOL     mdi_gps_nmea_get_debug_info(void);
extern MMI_BOOL     mdi_gps_nmea_get_power_saving(void);

/*for system use*/
extern MMI_BOOL     mdi_gps_uart_message_dispatcher(void *ilm_ptr);
/*for eng mode special use*/
extern void mdi_gps_eng_disable_sentence(void);
extern void mdi_gps_eng_enable_sentence(void);
extern S8* mdi_gps_get_fireware_version(void);
extern void* mdi_gps_register_p_callback(void (*gps_uart_p_callback)(mdi_gps_parser_p_info_enum type));
extern void* mdi_gps_register_p_info_callback(void (*gps_uart_p_info_callback)(mdi_gps_parser_p_info_enum type,void *param));
extern void mdi_gps_uart_callback_app_ind_hdler(mdi_gps_parser_info_enum type, UART_PORT port, void *buffer, U32 length);

extern void mdi_gps_gps_raw_data_ind_hdler(void *msg);
extern void mdi_gps_gps_debug_info_ind_hdler(void *msg);

#endif /* __MMI_GPS__ */ 

#ifdef __cpluscplus
}
#endif

#endif /* _MDI_GPSUART_H_ */ 
