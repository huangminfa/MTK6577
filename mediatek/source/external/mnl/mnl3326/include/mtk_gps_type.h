/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2008
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
 *   mtk_gps_type.h
 *
 * Description:
 * ------------
 *   Data types used by MTK navigation library
 *
 ****************************************************************************/

#ifndef MTK_GPS_TYPE_H
#define MTK_GPS_TYPE_H

#ifdef __cplusplus
  extern "C" {
#endif

#define HAWAII_ENABLE           0

#define MTK_GPS_SV_MAX_NUM      16
#define MTK_GPS_SV_MAX_PRN      32

//*****************************************************************************
// User System Configurations
//*****************************************************************************
#define HOST_USER_CONFIG_SYS_1 1
#define USER_SYS_ENABLE_DEBUG ( 1 << 0)


/*******************************************************************************
 * Type Definitions
 *******************************************************************************/
typedef unsigned char           mtk_uint8;
typedef signed char             mtk_int8;

typedef unsigned short int      mtk_uint16;
typedef signed short int        mtk_int16;

typedef unsigned int            mtk_uint32;
typedef signed int              mtk_int32;

typedef float                   mtk_r4;
typedef double                  mtk_r8;

#if ( defined(__ARMCC_VERSION) && (__ARMCC_VERSION < 200000 ) ) 
// for ADS1.x
#elif ( defined(__ARMCC_VERSION) && (__ARMCC_VERSION < 400000 ) ) 
// for RVCT2.x or RVCT3.x
#else
#pragma pack(4)
#endif

typedef enum
{
  MTK_GPS_FALSE = 0,
  MTK_GPS_TRUE = 1
} mtk_bool;

typedef enum
{
  MTK_GPS_START_DEFAULT = 0,
  MTK_GPS_START_HOT,
  MTK_GPS_START_WARM,
  MTK_GPS_START_COLD,
  MTK_GPS_START_FULL
} mtk_gps_start_type;

typedef enum
{
  MTK_GPS_START_RESULT_ERROR = 0,
  MTK_GPS_START_RESULT_OK
} mtk_gps_start_result;

typedef enum
{
  MTK_GPS_MSG_FIX_READY,
  MTK_GPS_MSG_DEBUG_READY,
  MTK_GPS_MSG_CONFIG_READY,
  MTK_GPS_MSG_TEST_STATUS_READY,
  MTK_GPS_MSG_BUFFER_ENOUGH,
  MTK_GPS_MSG_AGPS_RESPONSE,
  MTK_GPS_MSG_PMTK_RESPONSE,
  MTK_GPS_MSG_FIX_PROHIBITED
} mtk_gps_notification_type;

typedef enum
{
  MTK_GPS_FIX_Q_INVALID = 0,
  MTK_GPS_FIX_Q_GPS = 1,
  MTK_GPS_FIX_Q_DGPS = 2,
  MTK_GPS_FIX_Q_EST = 6
} mtk_gps_fix_quality;

typedef enum
{
  MTK_GPS_FIX_TYPE_INVALID = 1,
  MTK_GPS_FIX_TYPE_2D = 2,
  MTK_GPS_FIX_TYPE_3D = 3
} mtk_gps_fix_type;

typedef enum
{
  MTK_DGPS_MODE_NONE = 0,
  MTK_DGPS_MODE_RTCM = 1,
  MTK_DGPS_MODE_SBAS = 2,
  MTK_DGPS_MODE_AUTO = 3
} mtk_gps_dgps_mode;

typedef enum
{
  MTK_AGPS_MODE_NONE = 1,
  MTK_AGPS_MODE_SUPL = 2,
  MTK_AGPS_MODE_EPO  = 4,
  MTK_AGPS_MODE_BEE  = 8,
  MTK_AGPS_MODE_AUTO = 16
} mtk_gps_agps_mode;

// Thread definition
typedef enum
{
    MTK_MOD_NIL = 0,
    MTK_MOD_MNL,
    MTK_MOD_DSPIN,
    MTK_MOD_PMTKIN,
    MTK_MOD_AGENT,
    MTK_MOD_BEE_GEN,
    MTK_MOD_DISPATCHER,
    MTK_MOD_RTCM,    
    MTK_MOD_END_LIST
} mtk_gps_module;

// API from Dispatcher to Agent
typedef enum
{
    MTK_AGPS_MSG_EPO = 0,
    MTK_AGPS_MSG_BEE,
    MTK_AGPS_MSG_SUPL,
    MTK_AGPS_MSG_SUPL_PMTK,
    MTK_AGPS_MSG_PROFILE,
    MTK_AGPS_MSG_SUPL_TERMINATE,  //dispatcher send terminate to indicate SUPL stop
    MTK_AGPS_MSG_REQ_NI,          //request AGPS aiding from dispatcher    
	MTK_AGPS_MSG_REQ,             //request AGPS aiding from mnl
#if HAWAII_ENABLE	
	MTK_AGPS_MSG_WIFI_CFG_CNF,    //HAWAII AGPS aiding    
    MTK_AGPS_MSG_WIFI_AIDING,
    MTK_AGPS_MSG_CELLID_CFG_CNF,
    MTK_AGPS_MSG_CELLID_AIDING,
#endif
    MTK_AGPS_MSG_END_LIST
} mtk_agps_agent_msg_type;

// Callback from Agent to Dispatcher
typedef enum
{
    MTK_AGPS_CB_SUPL_PMTK = 0,
    MTK_AGPS_CB_SUPL_SI_REQ,
    MTK_AGPS_CB_SUPL_NI_REQ,
#if HAWAII_ENABLE    
    MTK_AGPS_CB_WIFI_CFG,  //HAWAII Config
    MTK_AGPS_CB_WIFI_REQ,
    MTK_AGPS_CB_CELLID_CFG,
    MTK_AGPS_CB_CELLID_REQ,
#endif
    MTK_AGPS_CB_END_LIST
} mtk_agps_cb_msg_type;

// AGPS feature on/off
typedef enum
{
    MTK_AGPS_FEATURE_OFF = 0,
    MTK_AGPS_FEATURE_ON = 1
} mtk_agps_func_onoff;

// HAWAII WiFi & CellId message error code
typedef enum
{
    MTK_AGPS_SUCCESS = 0,	// 0
    MTK_AGPS_ERROR,    	    // 1
} mtk_agps_msg_err_code;

typedef struct
{
  mtk_uint8 EPO_enabled;
  mtk_uint8 BEE_enabled;
  mtk_uint8 SUPL_enabled;
  mtk_uint8 SUPLSI_enabled;
#if HAWAII_ENABLE
  mtk_uint8 CELLID_enabled; //Add for HAWAII
  mtk_uint8 WIFI_enabled;
#endif
  mtk_uint8 EPO_priority;
  mtk_uint8 BEE_priority;
  mtk_uint8 SUPL_priority;
} mtk_agps_user_profile;

typedef struct 
{
  mtk_uint16    srcMod;
  mtk_uint16    dstMod;
  mtk_uint16    type;           /* message ID */
  mtk_uint16    length;         /* length of 'data' */
  char          data[1];        /* message content */
} mtk_gps_msg;

#if HAWAII_ENABLE
// HAWAII --- CellID Aiding Information (Note: below structure doesn't consider the 3G Cell-ID related fields)
typedef struct
{
    unsigned short mcc;
    unsigned short mnc;
    unsigned short lac;
    unsigned short ci;
    unsigned char rxlev;
    unsigned char reservd[3];
} cell_id_struct;

typedef struct
{
    mtk_agps_msg_err_code err_code;	// SUCCESS: 0
    unsigned short num_svr_cell;    // 0: no, 1 exist base station cell  
    unsigned short num_nbr_cell;
    unsigned int   timestamp; 		// keep the received system time tick info.
    cell_id_struct serving_cell_info;
    cell_id_struct nbr_cell_info[6];
} CEllID_AIDING_INFO_s;

//HAWAII --- WiFi Aiding Information
typedef struct
{
    unsigned char mac[6];
    int rssi;                       //default long type
    unsigned int timestamp; 		// keep the received system time tick info.
} wifi_ap_struct;

typedef struct
{
    mtk_agps_msg_err_code err_code;	// SUCCESS: 0
    unsigned int num_of_ap;		// keep total number of ap 
    //wifi_ap_struct serving_ap_info;
    wifi_ap_struct ap_info[16];
} WIFI_AIDING_INFO_s;
#endif

typedef enum
{
  MTK_BEE_DISABLE = 0,
  MTK_BEE_ENABLE = 1
} mtk_gps_bee_en;

typedef enum
{
  MTK_BRDC_DISABLE = 0,
  MTK_BRDC_ENABLE = 1
} mtk_gps_brdc_en;

typedef struct
{
  mtk_uint16    year;           /* years since 1900 */
  mtk_uint8     month;          /* 0-11 */
  mtk_uint8     mday;           /* 1-31 */
  mtk_uint8     hour;           /* 0-23 */
  mtk_uint8     min;            /* 0-59 */
  mtk_uint8     sec;            /* 0-59 */
  mtk_uint8     pad1;
  mtk_uint16    msec;           /* 0-999 */
  mtk_uint16    pad2;
} mtk_time;

typedef struct
{
  mtk_uint16    version;
  mtk_uint16    size;
  mtk_time      utc_time;
  mtk_int16     leap_second;            /* GPS-UTC time difference */
  mtk_uint16    WeekNo;                 /* GPS week number */
  mtk_uint32    TOW;                    /* GPS time of week (integer part) */
  mtk_uint8     fix_quality;            /* mtk_gps_fix_quality for GPGGA */
  mtk_uint8     fix_type;               /* mtk_gps_fix_type for GPGSA */
  double        clock_drift;            /* clock drift (s/s) */
  double        clock_bias;             /* clock bias (s) */
  double        LLH[4];                 /* LLH[0]: Latitude (degree) */
                                        /* LLH[1]: Longitude (degree) */
                                        /* LLH[2]: Height WGS84 (m) */
                                        /* LLH[3]: Height Mean Sea Level (m) */
  float         gspeed;                 /* 2D ground speed (m/s) */
  float         vspeed;                 /* vertical speed (m/s) */
  float         heading;                /* track angle (deg) */
  float         PDOP, HDOP, VDOP, TDOP; /* dilution of precision */
  float         EPE[3];                 /* estimated position error (m) */
                                        /* EPE[0]: North accuracy */
                                        /* EPE[1]: East accuracy */
                                        /* EPE[2]: vertical accuracy */
  float         EVE[3];                 /* estimated velocity error (m/s) */
                                        /* EVE[0]: North accuracy */
                                        /* EVE[1]: East accuracy */
                                        /* EVE[2]: vertical accuracy */
  float         Pos_2D_Accuracy; // Position Accuracy in Horizontal Direction [m]
  float         Pos_3D_Accuracy; // Position Accuracy in 3-D space [m]
  float         Vel_3D_Accuracy;  // Velocity Accuracy in 3-D space [m]
  float         DGPS_age;               /* time since last DGPS update (s) */
  mtk_uint16    DGPS_station_ID;
  mtk_uint8     DGPS_fix_mode;          /* mtk_gps_dgps_mode: 0(N/A); 1(RTCM); 2(SBAS) */
  mtk_uint8     sv_used_num;            /* for GPGSA */
  mtk_uint8     sv_in_view_num;         /* for GPGSV */
  mtk_uint8     sv_used_prn_list[MTK_GPS_SV_MAX_NUM];
  mtk_uint8     sv_in_view_prn_list[MTK_GPS_SV_MAX_NUM];
  mtk_int8      sv_in_view_elev[MTK_GPS_SV_MAX_NUM];
  mtk_uint16    sv_in_view_azim[MTK_GPS_SV_MAX_NUM];
  mtk_uint8     sv_in_view_snr[MTK_GPS_SV_MAX_NUM];
  mtk_uint8    sv_eph_valid[MTK_GPS_SV_MAX_PRN];  /*  sv ephemeris status 0: no ephemeris, 1: broadcast eph, 2: BEE ephmeris, */
  mtk_uint8    sv_eph_day[MTK_GPS_SV_MAX_PRN]; /* day of BEE  */
} mtk_gps_position;

typedef struct
{ /* index 0~31 represents PRN 1~32, respectively */
  mtk_uint8     health[MTK_GPS_SV_MAX_PRN]; /* nonzero: healthy */
  mtk_uint8     eph[MTK_GPS_SV_MAX_PRN];  /* nonzero: ephemeris available */
  mtk_uint8     alm[MTK_GPS_SV_MAX_PRN];  /* nonzero: almanac available */
  mtk_uint8     dgps[MTK_GPS_SV_MAX_PRN]; /* nonzero: DGPS correction ready */
  mtk_uint8     codelock[MTK_GPS_SV_MAX_PRN]; /* nonzero: code lock */
  mtk_uint8     freqlock[MTK_GPS_SV_MAX_PRN]; /* nonzero: frequency lock */
  mtk_uint8     carrlock[MTK_GPS_SV_MAX_PRN]; /* nonzero: carrier lock */
} mtk_gps_sv_info;


typedef mtk_int32 (*mtk_gps_callback)(mtk_gps_notification_type msg);


typedef enum
{
  MTK_DATUM_WGS84,
  MTK_DATUM_TOKYO_M,
  MTK_DATUM_TOKYO_A,
  MTK_DATUM_USER_SETTING,
  MTK_DATUM_ADINDAN_BURKINA_FASO,
  MTK_DATUM_ADINDAN_CAMEROON,
  MTK_DATUM_ADINDAN_ETHIOPIA,
  MTK_DATUM_ADINDAN_MALI,
  MTK_DATUM_ADINDAN_MEAN_FOR_ETHIOPIA_SUDAN,
  MTK_DATUM_ADINDAN_SENEGAL,
  MTK_DATUM_ADINDAN_SUDAN,
  MTK_DATUM_AFGOOYE_SOMALIA,
  MTK_DATUM_AIN_EL_ABD1970_BAHRAIN,
  MTK_DATUM_AIN_EL_ABD1970_SAUDI_ARABIA,
  MTK_DATUM_AMERICAN_SAMOA1962_AMERICAN_SAMOA,
  MTK_DATUM_ANNA_1_ASTRO1965_COCOS_ISLAND,
  MTK_DATUM_ANTIGU_ISLAND_ASTRO1943_ANTIGUA,
  MTK_DATUM_ARC1950_BOTSWANA,
  MTK_DATUM_ARC1950_BURUNDI,
  MTK_DATUM_ARC1950_LESOTHO,
  MTK_DATUM_ARC1950_MALAWI,
  MTK_DATUM_ARC1950_MEAN_FOR_BOTSWANA,
  MTK_DATUM_ARC1950_SWAZILAND,
  MTK_DATUM_ARC1950_ZAIRE,
  MTK_DATUM_ARC1950_ZAMBIA,
  MTK_DATUM_ARC1950_ZIMBABWE,
  MTK_DATUM_ARC1960_MEAN_FOR_KENYA_TANZANIA,
  MTK_DATUM_ARC1960_KENYA,
  MTK_DATUM_ARC1960_TAMZAMIA,
  MTK_DATUM_ASCENSION_ISLAND1958_ASCENSION,
  MTK_DATUM_ASTRO_BEACONE1945_IWO_JIMA,
  MTK_DATUM_ASTRO_DOS714_ST_HELENA_ISLAND,
  MTK_DATUM_ASTRO_TERN_ISLAND_FRIG1961_TERN_ISLAND,
  MTK_DATUM_ASTRONOMICAL_STATION1952_MARCUS_ISLAND,
  MTK_DATUM_AUSTRALIAN_GEODETIC1966_AUSTRALIA_TASMANIA,
  MTK_DATUM_AUSTRALIAN_GEODETIC1984_AUSTRALIA_TASMANIA,
  MTK_DATUM_AYABELLE_LIGHTHOUSE_DJIBOUTI,
  MTK_DATUM_BELLEVUE_IGN_EFATE_ERROMANGO_ISLAND,
  MTK_DATUM_BERMUDA1957_BERMUDA,
  MTK_DATUM_BISSAU_GUUINEA_BISSAU,
  MTK_DATUM_BOGOTA_OBSERVAORY_COLOMBIA,
  MTK_DATUM_BUKIT_RIMPAH_INDONESIA,
  MTK_DATUM_CAMP_AREA_ASTRO_ANTARCTICA,
  MTK_DATUM_CAMPO_INCHAUSPE_ARGENTINA,
  MTK_DATUM_CANTON_ASTRO1966_PHOENIX_ISLAND,
  MTK_DATUM_CAPE_SOUTH_AFRICA,
  MTK_DATUM_CAPE_CANAVERAL_BAHAMAS_FLORIDA,
  MTK_DATUM_CARTHAGE_TUNISIA,
  MTK_DATUM_CHATHAM_ISLAND_ASTRO1971_NEW_ZEALAND,
  MTK_DATUM_CHUA_ASTRO_PARAGUAY,
  MTK_DATUM_CORREGO_ALEGRE_BRAZIL,
  MTK_DATUM_DABOLA_GUINEA,
  MTK_DATUM_DECEPTION_ISLAND_DECEPTION_ISLAND_ANTARCTIA,
  MTK_DATUM_DJAKARTA_BATAVIA_INDONESIA_SUMATRA,
  MTK_DATUM_DOS1968_NEW_GEORGIA_ISLAND_GIZO_ISLAND,
  MTK_DATUM_EASTER_ISLAND1967_EASTER_ISLAND,
  MTK_DATUM_ESTONIA_COORDINATE_SYSTEM1937_ESTONIA,
  MTK_DATUM_EUROPEAN1950_CYPRUS,
  MTK_DATUM_EUROPEAN1950_EGYPT,
  MTK_DATUM_EUROPEAN1950_ENGLAND_CHANNEL_ISLANDS_SCOTLAND_SHETLAND_ISLANDS,
  MTK_DATUM_EUROPEAN1950_ENGLAND_IRELAND_SCOTLAND_SHETLAND_ISLANDS,
  MTK_DATUM_EUROPEAN1950_FINLAND_NORWAY,
  MTK_DATUM_EUROPEAN1950_GREECE,
  MTK_DATUM_EUROPEAN1950_IRAN,
  MTK_DATUM_EUROPEAN1950_ITALY_SARDINIA,
  MTK_DATUM_EUROPEAN1950_ITALT_SLCILY,
  MTK_DATUM_EUROPEAN1950_MALTA,
  MTK_DATUM_EUROPEAN1950_MEAN_FOR_AUSTRIA_BELGIUM,//_DENMARK_FINLAND_FRANCE_WGERMANY_GIBRALTAR_GREECE_ITALY_LUXEMBOURG_NETHERLANDS_NORWAY_PORTUGAL_SPAIN_SWEDEN_SWITZERLAND,
  MTK_DATUM_EUROPEAN1950_MEAN_FOR_AUSTRIA_DEBNMARK,//_FRANCE_WGERMANY_NETHERLAND_SWITZERLAND,
  MTK_DATUM_EUROPEAN1950_MEAN_FOR_IRAG_ISRAEL_JORDAN,//_LEBANON_KUWAIT_SAUDI_ARABIA_SYRIA,
  MTK_DATUM_EUROPEAN1950_PORTUGAL_SPAIN,
  MTK_DATUM_EUROPEAN1950_TUNISIA,
  MTK_DATUM_EUROPEAN1979_MEAN_FOR_AUSTRIA_FINLAND_,//NETHERLANDS_NORWAY_SPAIN_SWEDEN_SWITZERLAND,
  MTK_DATUM_FORT_THOMAS1955_NEVIS_ST_KITTS_LEEWARD_ISLANDS,
  MTK_DATUM_GAN1970_REPUBLIC_OF_MALDIVES,
  MTK_DATUM_GEODETIC_DATAUM1970_NEW_ZEALAND,
  MTK_DATUM_GRACIOSA_BASE_SW1948_AZORES_FAIAL_GRACIOSA_PICO_SAO,
  MTK_DATUM_GUAM1963_GUAM,
  MTK_DATUM_GUNUNG_SEGARA_INDONESIA_KALIMANTAN,
  MTK_DATUM_GUX_1_ASTRO_GUADALCANAL_ISLAND,
  MTK_DATUM_HERAT_NORTH_AFGHANISTAN,
  MTK_DATUM_HERMANNSKOGEL_DATUM_CROATIA_SERBIA_BOSNIA_HERZEGOIVNA,
  MTK_DATUM_HJORSEY1955_ICELAND,
  MTK_DATUM_HONGKONG1963_HONGKONG,
  MTK_DATUM_HU_TZU_SHAN_TAIWAN,
  MTK_DATUM_INDIAN_BANGLADESH,
  MTK_DATUM_INDIAN_INDIA_NEPAL,
  MTK_DATUM_INDIAN_PAKISTAN,
  MTK_DATUM_INDIAN1954_THAILAND,
  MTK_DATUM_INDIAN1960_VIETNAM_CON_SON_ISLAND,
  MTK_DATUM_INDIAN1960_VIETNAM_NEAR16N,
  MTK_DATUM_INDIAN1975_THAILAND,
  MTK_DATUM_INDONESIAN1974_INDONESIA,
  MTK_DATUM_IRELAND1965_IRELAND,
  MTK_DATUM_ISTS061_ASTRO1968_SOUTH_GEORGIA_ISLANDS,
  MTK_DATUM_ISTS073_ASTRO1969_DIEGO_GARCIA,
  MTK_DATUM_JOHNSTON_ISLAND1961_JOHNSTON_ISLAND,
  MTK_DATUM_KANDAWALA_SRI_LANKA,
  MTK_DATUM_KERGUELEN_ISLAND1949_KERGUELEN_ISLAND,
  MTK_DATUM_KERTAU1948_WEST_MALAYSIA_SINGAPORE,
  MTK_DATUM_KUSAIE_ASTRO1951_CAROLINE_ISLANDS,
  MTK_DATUM_KOREAN_GEODETIC_SYSTEM_SOUTH_KOREA,
  MTK_DATUM_LC5_ASTRO1961_CAYMAN_BRAC_ISLAND,
  MTK_DATUM_LEIGON_GHANA,
  MTK_DATUM_LIBERIA1964_LIBERIA,
  MTK_DATUM_LUZON_PHILIPPINES_EXCLUDING_MINDANAO,
  MTK_DATUM_LUZON_PHILLIPPINES_MINDANAO,
  MTK_DATUM_MPORALOKO_GABON,
  MTK_DATUM_MAHE1971_MAHE_ISLAND,
  MTK_DATUM_MASSAWA_ETHIOPIA_ERITREA,
  MTK_DATUM_MERCHICH_MOROCCO,
  MTK_DATUM_MIDWAY_ASTRO1961_MIDWAY_ISLANDS,
  MTK_DATUM_MINNA_CAMEROON,
  MTK_DATUM_MINNA_NIGERIA,
  MTK_DATUM_MONTSERRAT_ISLAND_ASTRO1958_MONTSERRAT_LEEWARD_ISLAND,
  MTK_DATUM_NAHRWAN_OMAN_MASIRAH_ISLAND,
  MTK_DATUM_NAHRWAN_SAUDI_ARABIA,
  MTK_DATUM_NAHRWAN_UNITED_ARAB_EMIRATES,
  MTK_DATUM_NAPARIMA_BWI_TRINIDAD_TOBAGO,
  MTK_DATUM_NORTH_AMERICAN1927_ALASKA_EXCLUDING_ALEUTIAN_IDS,
  MTK_DATUM_NORTH_AMERICAN1927_ALASKA_ALEUTIAN_IDS_EAST_OF_180W,
  MTK_DATUM_NORTH_AMERICAN1927_ALASKA_ALEUTIAN_IDS_WEST_OF_180W,
  MTK_DATUM_NORTH_AMERICAN1927_BAHAMAS_EXCEPT_SAN_SALVADOR_ISLANDS,
  MTK_DATUM_NORTH_AMERICAN1927_BAHAMAS_SAN_SALVADOR_ISLANDS,
  MTK_DATUM_NORTH_AMERICAN1927_CANADA_ALBERTA_BRITISH_COLUMBIA,
  MTK_DATUM_NORTH_AMERICAN1927_CANADA_MANITOBA_ONTARIO,
  MTK_DATUM_NORTH_AMERICAN1927_CANADA_NEW_BRUNSWICK_NEWFOUNDLAND_NOVA_SCOTIA_QUBEC,
  MTK_DATUM_NORTH_AMERICAN1927_CANADA_NORTHWEST_TERRITORIES_SASKATCHEWAN,
  MTK_DATUM_NORTH_AMERICAN1927_CANADA_YUKON,
  MTK_DATUM_NORTH_AMERICAN1927_CANAL_ZONE,
  MTK_DATUM_NORTH_AMERICAN1927_CUBA,
  MTK_DATUM_NORTH_AMERICAN1927_GREENLAND_HAYES_PENINSULA,
  MTK_DATUM_NORTH_AMERICAN1927_MEAN_FOR_ANTIGUA_BARBADOS_BARBUDA_CAICOS, // _ISLANDS_CUBA_DOMINICAN_GRAND_CAYMAN_JAMAICA_TURKS_ISLANDS,
  MTK_DATUM_NORTH_AMERICAN1927_MEAN_FOR_BELIZE_COSTA_RICA_SALVADOR_GUATEMALA,//_HONDURAS_NICARAGUA,
  MTK_DATUM_NORTH_AMERICAN1927_MEAN_FOR_CANADA,
  MTK_DATUM_NORTH_AMERICAN1927_MEAN_FOR_CONUS,
  MTK_DATUM_NORTH_AMERICAN1927_MEAN_FOR_CONUS_EAST_OF_MISSISSIPPI_RIVER, //_INCLUDING_LOUISIANA_MISSOURI_MINNESOTA,
  MTK_DATUM_NORTH_AMERICAN1927_MEAN_FOR_CONUS_WEST_OF_MISSISSIPPI_RIVER,//_EXCLUDING_LOUISIANA_MINNESOTA_MISSOURI,
  MTK_DATUM_NORTH_AMERICAN1927_MEXICO,
  MTK_DATUM_NORTH_AMERICAN1983_ALASKA_EXCLUDING_ALEUTIAN_IDS,
  MTK_DATUM_NORTH_AMERICAN1983_ALEUTIAN_IDS,
  MTK_DATUM_NORTH_AMERICAN1983_CANADA,
  MTK_DATUM_NORTH_AMERICAN1983_CONUS,
  MTK_DATUM_NORTH_AMERICAN1983_HAHWII,
  MTK_DATUM_NORTH_AMERICAN1983_MEXICO_CENTRAL_AMERICA,
  MTK_DATUM_NORTH_SAHARA1959_ALGERIA,
  MTK_DATUM_OBSERVATORIO_METEOROLOGICO1939_AZORES_CORVO_FLORES_ISLANDS,
  MTK_DATUM_OLD_EGYPTIAN1907_EGYPT,
  MTK_DATUM_OLD_HAWAIIAN_HAWAII,
  MTK_DATUM_OLD_HAWAIIAN_KAUAI,
  MTK_DATUM_OLD_HAWAIIAN_MAUI,
  MTK_DATUM_OLD_HAWAIIAN_MEAN_FOR_HAWAII_KAUAI_MAUI_OAHU,
  MTK_DATUM_OLD_HAWAIIAN_OAHU,
  MTK_DATUM_OMAN_OMAN,
  MTK_DATUM_ORDNANCE_SURVEY_GREAT_BRITIAN1936_ENGLAND,
  MTK_DATUM_ORDNANCE_SURVEY_GREAT_BRITIAN1936_ENGLAND_ISLE_OF_MAN_WALES,
  MTK_DATUM_ORDNANCE_SURVEY_GREAT_BRITIAN1936_MEAN_FOR_ENGLAND_ISLE_OF_MAN,//_SCOTLAND_SHETLAND_ISLAND_WALES,
  MTK_DATUM_ORDNANCE_SURVEY_GREAT_BRITIAN1936_SCOTLAND_SHETLAND_ISLANDS,
  MTK_DATUM_ORDNANCE_SURVEY_GREAT_BRITIAN1936_WALES,
  MTK_DATUM_PICO_DE_LAS_NIEVES_CANARY_ISLANDS,
  MTK_DATUM_PITCAIRN_ASTRO1967_PITCAIRN_ISLAND,
  MTK_DATUM_POINT58_MEAN_FOR_BURKINA_FASO_NIGER,
  MTK_DATUM_POINTE_NOIRE1948_CONGO,
  MTK_DATUM_PORTO_SANTO1936_PORTO_SANTO_MADERIA_ISLANDS,
  MTK_DATUM_PROVISIONAL_SOUTH_AMERICAN1956_BOLOVIA,
  MTK_DATUM_PROVISIONAL_SOUTH_AMERICAN1956_CHILE_NORTHERN_NEAR_19DS,
  MTK_DATUM_PROVISIONAL_SOUTH_AMERICAN1956_CHILE_SOUTHERN_NEAN_43DS,
  MTK_DATUM_PROVISIONAL_SOUTH_AMERICAN1956_COLOMBIA,
  MTK_DATUM_PROVISIONAL_SOUTH_AMERICAN1956_ECUADOR,
  MTK_DATUM_PROVISIONAL_SOUTH_AMERICAN1956_GUYANA,
  MTK_DATUM_PROVISIONAL_SOUTH_AMERICAN1956_MEAN_FOR_BOLIVIA_CHILE,//_COLOMBIA_ECUADOR_GUYANA_PERU_VENEZUELA,
  MTK_DATUM_PROVISIONAL_SOUTH_AMERICAN1956_PERU,
  MTK_DATUM_PROVISIONAL_SOUTH_AMERICAN1956_VENEZUELA,
  MTK_DATUM_PROVISIONAL_SOUTH_CHILEAN1963_CHILE_NEAR_53DS_HITO_XV3,
  MTK_DATUM_PUERTO_RICO_PUERTO_RICO_VIRGIN_ISLANDS,
  MTK_DATUM_PULKOVO1942_RUSSIA,
  MTK_DATUM_QATAR_NATIONAL_QATAR,
  MTK_DATUM_QORNOQ_GREENLAND_SOUTH,
  MTK_DATUM_REUNION_MASCARENE_ISLAND,
  MTK_DATUM_ROME1940_ITALY_SARDINIA,
  MTK_DATUM_S42_PULKOVO1942_HUNGARY,
  MTK_DATUM_S42_PULKOVO1942_POLAND,
  MTK_DATUM_S42_PULKOVO1942_CZECHOSLAVAKIA,
  MTK_DATUM_S42_PULKOVO1942_LATIVA,
  MTK_DATUM_S42_PULKOVO1942_KAZAKHSTAN,
  MTK_DATUM_S42_PULKOVO1942_ALBANIA,
  MTK_DATUM_S42_PULKOVO1942_ROMANIA,
  MTK_DATUM_SJTSK_CZECHOSLAVAKIA_PRIOR1_JAN1993,
  MTK_DATUM_SANTO_DOS1965_ESPIRITO_SANTO_ISLAND,
  MTK_DATUM_SAO_BRAZ_AZORES_SAO_MIGUEL_SANTA_MARIA_IDS,
  MTK_DATUM_SAPPER_HILL1943_EAST_FALKLAND_ISLAND,
  MTK_DATUM_SCHWARZECK_NAMIBIA,
  MTK_DATUM_SELVAGEM_GRANDE1938_SALVAGE_ISLANDS,
  MTK_DATUM_SIERRA_LEONE1960_SIERRA_LEONE,
  MTK_DATUM_SOUTH_AMERICAN1969_ARGENTINA,
  MTK_DATUM_SOUTH_AMERICAN1969_BOLIVIA,
  MTK_DATUM_SOUTH_AMERICAN1969_BRAZIAL,
  MTK_DATUM_SOUTH_AMERICAN1969_CHILE,
  MTK_DATUM_SOUTH_AMERICAN1969_COLOMBIA,
  MTK_DATUM_SOUTH_AMERICAN1969_ECUADOR,
  MTK_DATUM_SOUTH_AMERICAN1969_ECUADOR_BALTRA_GALAPAGOS,
  MTK_DATUM_SOUTH_AMERICAN1969_GUYANA,
  MTK_DATUM_SOUTH_AMERICAN1969_MEAN_FOR_ARGENTINA_BOLIVIA_BRAZIL,//_CHILE_COLOMBIA_ECUADOR_GUYANA_PARAGUAY_PERU_TRINIDAD_TOBAGO_VENEZUELA,
  MTK_DATUM_SOUTH_AMERICAN1969_PARAGUAY,
  MTK_DATUM_SOUTH_AMERICAN1969_PERU,
  MTK_DATUM_SOUTH_AMERICAN1969_TRINIDAD_TOBAGO,
  MTK_DATUM_SOUTH_AMERICAN1969_VENEZUELA,
  MTK_DATUM_SOUTH_ASIA_SINGAPORE,
  MTK_DATUM_TANANARIVE_OBSERVATORY1925_MADAGASCAR,
  MTK_DATUM_TIMBALAI1948_BRUNEI_E_MALAYSIA_SABAH_SARAWAK,
  MTK_DATUM_TOKYO_JAPAN,
  MTK_DATUM_TOKYO_MEAN_FOR_JAPAN_SOUTH_KOREA_OKINAWA,
  MTK_DATUM_TOKYO_OKINAWA,
  MTK_DATUM_TOKYO_SOUTH_KOREA,
  MTK_DATUM_TRISTAN_ASTRO1968_TRISTAM_DA_CUNHA,
  MTK_DATUM_VITI_LEVU1916_FIJI_VITI_LEVU_ISLAND,
  MTK_DATUM_VOIROL1960_ALGERIA,
  MTK_DATUM_WAKE_ISLAND_ASTRO1952_WAKE_ATOLL,
  MTK_DATUM_WAKE_ENIWETOK1960_MARSHALL_ISLANDS,
  MTK_DATUM_WGS1972_GLOBAL_DEFINITION,
  MTK_DATUM_WGS1984_GLOBAL_DEFINITION,
  MTK_DATUM_YACARE_URUGUAY,
  MTK_DATUM_ZANDERIJ_SURINAME
} mtk_gps_datum;


/************************************************
 * defines for set_param and get_param
 ************************************************/
/*-------- keys --------*/
typedef enum
{
  MTK_PARAM_CMD_TERMINATE,
  MTK_PARAM_CMD_RESET_DSP,
  MTK_PARAM_CMD_CONFIG,
  MTK_PARAM_CMD_TESTMODE,
  MTK_PARAM_CMD_RESTART,
  MTK_PARAM_DGPS_CONFIG,
  MTK_PARAM_NAV_CONFIG,
  MTK_PARAM_BUF_CONFIG,
  MTK_PARAM_LIB_VERSION,
  MTK_PARAM_CMD_SLEEP,
  MTK_PARAM_CMD_WAKEUP,
  MTK_PARAM_BEE_CONFIG,
  MTK_PARAM_BRDC_CONFIG,
  MTK_MSG_EPO_REQ,   //TBD
  MTK_MSG_EPO_RESP,  //TBD
  MTK_MSG_BEE_REQ,
  MTK_MSG_BEE_RESP,  
  MTK_MSG_REQ_ASSIST,  // MNL generic request for assistance data 
  MTK_MSG_BEE_IND      // Agent tell MNL that BEE could be used
} mtk_gps_param;

/*------- values -------*/
/* MTK_PARAM_CMD_TERMINATE : terminate GPS main thread: no payload */

/* MTK_PARAM_CMD_CONFIG : schedule an event for get/set aiding data or config:
   no payload */

/* MTK_PARAM_CMD_TESTMODE : enter/leave a test mode */
typedef enum
{
  MTK_TESTMODE_OFF = 0,
  MTK_TESTMODE_CHANNEL,
  MTK_TESTMODE_JAMMER,
  MTK_TESTMODE_PHASE,
  MTK_TESTMODE_PER
} mtk_gps_testmode;

typedef struct
{
  mtk_gps_testmode test_mode;
  mtk_uint8     svid;
  mtk_uint32    time;
  mtk_uint8     threshold;
  mtk_uint16    targetCount;
} mtk_param_testmode;

/* MTK_PARAM_CMD_RESTART : trigger restart of GPS main thread and dsp code */
typedef struct
{
  mtk_gps_start_type restart_type;      /* MTK_GPS_START_HOT, MTK_GPS_START_WARM, MTK_GPS_START_COLD, MTK_GPS_START_FULL */
} mtk_param_restart;

/* MTK_PARAM_BEE_CONFIG : configuration of bee */
typedef struct
{
  mtk_gps_bee_en bee_en;      /* MTK_BEE_DISABLE MTK_BEE_ENABLE */
} mtk_param_bee_config;

/* MTK_PARAM_BRDC_CONFIG : configuration of broadcast satellite data */
typedef struct
{
  mtk_gps_brdc_en brdc_en;      /* MTK_BRDC_DISABLE MTK_BRDC_ENABLE */
} mtk_param_brdc_config;


/* MTK_PARAM_DGPS_CONFIG */
typedef struct
{
  mtk_gps_dgps_mode dgps_mode;          /* Off/RTCM/SBAS */
  mtk_uint8     dgps_timeout;           /* DGPS timeout in seconds */
  mtk_bool      sbas_test_mode;         /* TRUE=test; FALSE=integrity */
  mtk_uint8     sbas_prn;               /* 0=automatic; 120-139=specific PRN */
  mtk_bool      full_correction;        /* SBAS data correction mode */
                                        /* TRUE=full [Fast/Long-Term and IONO] */
                                        /* FALSE=partial [Fast/Long-Term or IONO] */
  mtk_bool      select_corr_only;       /* satellite selection mode */
                                        /* TRUE=only SVs with corrections */
                                        /* FALSE=any SVs available */
} mtk_param_dgps_cfg;

/* MTK_PARAM_NAV_CONFIG */
typedef struct
{
  mtk_uint32    fix_interval;           /* fix interval in milliseconds */
                                        /* 1000=>1Hz, 200=>5Hz, 2000=>0.5Hz */
  mtk_gps_datum datum;                  /* datum */
  mtk_int8      elev_mask;              /* elevation angle mask in degree */
} mtk_param_nav_cfg;

/*  MTK_PARAM_BUF_CONFIG */
typedef struct
{
  mtk_uint16    buf_empty_threshold;    /* If the buffer free space reach this value
                                           then it will report to callback once */
  mtk_uint16    buf_empty_size;         /* Buffer free space in bytes, read only */
} mtk_param_buf_cfg;

//BEE safety design
#define MTK_PMTK_CMD_MAX_SIZE            256
typedef struct
{
  char pmtk[MTK_PMTK_CMD_MAX_SIZE];         /* Buffer free space in bytes, read only */
} mtk_param_pmtk_cmd;

typedef struct
{
  mtk_uint16  u2Bitmap;
  mtk_int16   i2WeekNo;
  mtk_int32   i4Tow;

} mtk_param_agps_req;

typedef struct
{
  mtk_uint16 u2AckCmd;
  mtk_uint8 u1Flag;
} PMTKD_001_T;

typedef struct{
  mtk_r8 dfRtcDltTime;
} PMTKD_243_T;

typedef struct{
  mtk_bool fgSet; // reference location: 0 - clear,  1 - set
  mtk_r8 dfRefLat;
  mtk_r8 dfRefLon;
  mtk_r4 fRefAlt;
} PMTKD_244_T;

typedef struct
{
    mtk_uint16 u2Cmd;  // PMTK command ID:
                       // please get the data arguements in the following corresponding data structure.
    union
    {
        PMTKD_001_T rAck;              // PMTK001
    } uData;
} MTK_PMTK_RESPONSE_T;

typedef struct
{
  mtk_uint16    u2Cmd;  // PMTK command ID: if the PMTK command has data arguments,
                        // please assign the data in the following corresponding data structure.
  union
  {
    PMTKD_243_T r243;
    PMTKD_244_T r244;
  } uData;
} MTK_PMTK_DATA_T;


typedef enum
{
  MTK_IF_SW_EMULATION = 0,
  MTK_IF_UART_NO_HW_FLOW_CTRL,
  MTK_IF_UART_HW_FLOW_CTRL,
  MTK_IF_SPI
} mtk_if_type;

typedef enum
{
  MTK_PPS_DISABLE = 0,
  MTK_PPS_ENABLE_1PPS,
  MTK_PPS_ENABLE_NPPS
} mtk_pps_mode;

/* factory default configuration for mtk_gps_init() */
typedef struct
{
  mtk_if_type   if_type;        /* interface type UART/SPI */
  mtk_pps_mode  pps_mode;       /* 1PPS mode */
  mtk_uint16    pps_duty;       /* PPS pulse high duration (ms) */
  mtk_uint8     pps_port;        /* PPS output port select GIO 16 17 18 19 can't be used */
  mtk_uint32    if_init_spd;    /* interface initial speed (bps of UART) */
  mtk_uint32    if_link_spd;    /* interface link speed (bps of UART) */
  mtk_uint32    hw_TCXO_Hz;     /* TCXO frequency in Hz */
  mtk_uint16    hw_TCXO_ppb;    /* TCXO clock drift in ppb (500=0.5ppm; 2500=2.5ppm) */
  mtk_uint16    internal_LNA;   /* Use internal LNA or not , 0 = not use, 1 = use internal LNA */

  mtk_uint32    fix_interval;   /* fix interval in milliseconds */
                                /* 1000=>1Hz, 200=>5Hz, 2000=>0.5Hz */
  double        Lat;            /* initial Latitude of a cold start (degree) */
  double        Lon;            /* initial Longitude of a cold start (degree) */

  mtk_gps_datum datum;          /* datum */
  mtk_gps_dgps_mode dgps_mode;  /* Off/RTCM/SBAS */
  mtk_gps_agps_mode agps_mode;  /* Off/SUPL/EPO/BEE/Auto(BEE > EPO > SUPL) */
  mtk_uint16    opmode;         /* output the first fix ASAP */
  mtk_uint8     elev_mask;      /* elevation angle mask in degree */
  mtk_uint8     sbas_prn;       /* 0=automatic; 120-139=specific PRN */
  mtk_bool      sbas_test_mode; /* TRUE=test; FALSE=integrity */
  mtk_uint8     u1ClockType;    // FLASH_SIG_USE_16368_TCXO         0     // 16.368M integer, the most stable
                                // FLASH_SIG_USE_16369M_TCXO    1    // 16.369M integer, generated freq has bias
                                // FLASH_SIG_USE_26M_FRAC_TCXO  2    // not used
                                // FLASH_SIG_USE_26M_INT_TCXO   3    // 26M integer, generated freq has bias, 
                                // FLASH_SIG_USE_WIDERANGE_XTAL 0xFE // wide range, using crystal solution
                                // FLASH_SIG_USE_WIDERANGE      0xFF // wide range, more power consumption, no freq bias
  mtk_bool      fgFrameAiding;  // 0: disable, 1: enable
  mtk_uint32    reservedx;
  void*         reservedy;
} mtk_init_cfg;

// mtk_init_cfg.opmode defines
#define MTK_INITCFG_OPMODE_2D_FIRSTFIX  (1 << 2)

// Task synchronization related type
typedef enum
{
  MTK_MUTEX_BIN_Q = 0,
  MTK_MUTEX_MSG_CNT,
  MTK_MUTEX_SPI,
  MTK_MUTEX_END
} mtk_mutex_enum;

/* Return value for most APIs */
#define MTK_GPS_SUCCESS                 (0)
#define MTK_GPS_ERROR                   (-1)
#define MTK_GPS_ERROR_NMEA_INCOMPLETE   (-2)

#define MTK_GPS_ERROR_TIME_CHANGED      (1)

#define MTK_GPS_NO_MSG_RECEIVED         (1)


#if ( defined(__ARMCC_VERSION) && (__ARMCC_VERSION < 200000 ))
// for ADS1.x
#elif ( defined(__ARMCC_VERSION) && (__ARMCC_VERSION < 400000 ) ) 
// for RVCT2.x or RVCT3.x
#else
#pragma pack()
#endif

#ifdef __cplusplus
   }
#endif

#endif /* MTK_GPS_TYPE_H */
