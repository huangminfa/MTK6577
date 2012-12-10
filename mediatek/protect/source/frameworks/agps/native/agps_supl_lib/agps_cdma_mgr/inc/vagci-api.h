/*************************************************************
*
* This Software is the property of VIA Telecom, Inc. and may only be used pursuant to a license from VIA Telecom, Inc.
*
* Any unauthorized use inconsistent with the terms of such license is strictly prohibited.
*
* Copyright (c) 2012 VIA Telecom, Inc.  All rights reserved.
*
*************************************************************/
#ifndef _VIA_GPS_API_H
#define _VIA_GPS_API_H
#ifdef __cplusplus
extern "C" {
#endif

#include "vagci-api_adp.h"

typedef unsigned char   UINT8;
typedef signed char     INT8;
typedef unsigned short  UINT16;
typedef signed short    INT16;
typedef unsigned int    UINT32;
typedef signed int      INT32;

#define RPC_LOC_API_SUCCESS           0
#define RPC_LOC_API_GENERAL_FAILURE   1
#define RPC_LOC_API_UNSUPPORTED       2
#define RPC_LOC_API_INVALID_HANDLE    4
#define RPC_LOC_API_INVALID_PARAMETER 5
#define RPC_LOC_API_ENGINE_BUSY       6
#define RPC_LOC_API_PHONE_OFFLINE     7
#define RPC_LOC_API_TIMEOUT           8


#define  RPC_REQ_SUCCESS   0
#define  RPC_REQ_ERROR     1

#define MAX_SV_NUM         12
#define MAX_EPH_PRN_NUM    16
#define MAX_ALM_PRN_NUM    32

typedef struct {
    UINT8 ref_loc_req;     /*Reference Location Request, 0: Does not need, 1: Needed*/
    UINT8 ionospheric_req; /*Ionospheric Request, 0: Does not need, 1: Needed*/
    UINT8 almanac_req;     /*Almanac Request, 0: Does not need, 1: Needed*/
    UINT8 ephemeris_req;   /*Ephemeris Request 0: Does not need, 1: Needed*/
} rpc_gps_assist_data_type;

typedef struct {
    UINT32 id;             /*TBD*/
    UINT8 fix_mode;        /*Unkown: 0; MSA: 1, MSB: 2, MSS: 3, Control Plane: 4*/
    UINT32 fixes_num;      /*A value of 1 means is interested in only one fix. */
                           /*A value > 1, multiple fixes with some time in btw the attempts*/
    UINT32 fix_rate_time;  /*Time, in seconds, between position fix attempts*/
    UINT32 qos_haccuracy;  /*Horizontal Accuracy, in meters*/
    UINT32 qos_vaccuracy;  /*Vertical Acuracy, in meters*/
    UINT32 qos_performance;/*Performance response quality in terms of time, in seconds*/
} gps_power_on_type;


typedef struct {
    UINT8 fix_mode;        /*Unkown: 0; MSA: 1, MSB: 2, MSS: 3, Control Plane: 4*/
    UINT32 fixes_num;      /*A value of 1 means is interested in only one fix. */
                           /*A value > 1, multiple fixes with some time in btw the attempts*/
    UINT32 fix_rate_time;  /*Time, in seconds, between position fix attempts*/
    UINT32 qos_haccuracy;  /*Horizontal Accuracy, in meters*/
    UINT32 qos_vaccuracy;  /*Vertical Acuracy, in meters*/
    UINT32 qos_performance;/*Performance response quality in terms of time, in seconds*/
} gps_session_cfg;

typedef struct  {
    UINT32 IpAddr;
    UINT16 IpPort;
} gps_mcp_cfg;

typedef struct {
    UINT8 valid_ref_loc;   /*0: Not valid, 1: Valid Time Zone Only,2: Valid Time Zone and BS Location.*/
    float tz_lat;
    float tz_long;         /*Time zone Range [-16h ~+15.5h] and specific location is predefined in CBP*/
    UINT16 sid;            /*System ID, Range [0..32767]*/
    UINT16 nid;            /*Network ID, Range [0..65535]*/
    UINT16 bs_id;          /*Base Station ID, Range [0..65535]*/
    float bs_lat;          /*WGS84 Geodetic Latitude [degrees], latitude from base last registered on*/
    float bs_long;         /*WGS84 Geodetic Longitude [degrees], Longitude from base last registered on*/
} gps_refloc_data;

typedef struct {
  char Request_id[64];
  int Request_id_len;   /* zero length means invalid */
  char client_name[64];
  int client_name_len;    /* zero length means invalid */
  int client_name_valid;
  int type;    /* 0:No notification and no verification, 1:Notification only,
2:Verification with accept if no answer, 3:Verification with deny if no answer
, 4:Privacy Override*/
} gps_rpc_notify_info;


typedef struct {
    UINT8 sv_id;           /*Satelite Vehicle ID, Range[1..32]*/
    INT8 af2;              /*Apparent satelite clock correction af2*/
    UINT8 iode;            /*Issue of data*/
    UINT16 toc;            /*Clock data reference time*/
    UINT16 toe;            /*Ephemeris reference time*/
    INT16 af1;             /*Apparent satelite clock correction af1*/
    INT16 delta_n;         /*Mean motion difference from the computed value*/
    INT16 idot;            /*Rate of inclination angle*/
    INT16 c_rs;            /*Amplitude of the sine harmonic correction term to the orbit radius*/
    INT16 c_rc;            /*Amplitude of the cosine harmonic correction term to the orbit radius*/
    INT16 c_us;            /*Amplitude of the sine harmonic correction term to the argument of latitude*/
    INT16 c_uc;            /*Amplitude of the cosine harmonic correction term to the argument of latitude.*/
    INT16 c_is;            /*Amplitude of the sine harmonic correction term to the angle of inclination*/
    INT16 c_ic;            /*Amplitude of the cosine harmonic correction term to the angle of inclination*/
    INT32 af0;             /*Apparent satellite clock correction af0*/
    INT32 m0;              /*Mean anomaly at the reference time*/
    UINT32 a_sqrt;         /*Square root of the semi-major axis*/
    UINT32 eccentricity;   /*eccentricity*/
    INT32 i_angle;         /*Inclination angle at the reference time*/
    INT32 omega_0;         /*Longitude of ascending node of orbit plane at weekly epoch*/
    INT32 omega;           /*Argument of perigee*/
    INT32 omegadot;        /*Rate of right ascension*/
} eph_data;

typedef struct
{
    UINT8 num_sv;          /* Number of SVs,Range [1..32] */
    eph_data ephdata[MAX_EPH_PRN_NUM]; /* Set Max 16 PRNs, 3 + (57 * 16) = 915 Bytes */
}gps_eph_prn_resp;


typedef struct {
    UINT8 sv_id;           /*Satelite vehicle id, Range [1..32]*/
    INT16 delta_i;         /*Correction to inclination*/
    INT16 af0;             /*Apparent satellite clock correction af0. */
    INT16 af1;             /*Apparent satellite clock correction af1. */
    INT16 omegadot;        /*rate of right ascension*/
    UINT16 eccentricity;   /*Eccentricity*/
    UINT32 a_sqrt;         /*Square root of the semi-major axis*/
    INT32 omega_0;         /*Longitude of ascending node of orbit plane. */
    INT32 omega;           /*Argument of perigee. */
    INT32 m0;              /*Mean anoaly at reference time. */
} alm_data;

typedef struct {
    UINT8 sv_num;          /*Number of SVs, Range [1..32]*/
    UINT8 week_num;        /*GPS week number, Range [0..255]*/
    UINT8 toa;             /*Time of almanac, in units of 4096 seconds, Range [0..602112]*/
    alm_data ALMData[MAX_ALM_PRN_NUM];  /* Set Max 32 PRNs, 5 + (27 * 32)= 869 bytes */
} alm_resp;

typedef struct {
    UINT8 abpar_incl;      /*Alpha and beta parameters included or not. 0: Not include, 1: included*/
    INT8 alpha0;           /*Ionospheric correction parameter Alpha0*/
    INT8 alpha1;           /*Ionospheric correction parameter Alpha1*/
    INT8 alpha2;           /*Ionospheric correction parameter Alpha2*/
    INT8 alpha3;           /*Ionospheric correction parameter Alpha3*/
    INT8 beta0;            /*Ionospheric correction parameter Beta0*/
    INT8 beta1;            /*Ionospheric correction parameter Beta1*/
    INT8 beta2;            /*Ionospheric correction parameter Beta2*/
    INT8 beta3;            /*Ionospheric correction parameter Beta3*/
    UINT32 z_count;        /*TBD*/
} ion_data;

typedef struct {
    UINT8 val_ref_time;    /*Not Available: 0, Available: 1. If CDMA not accquired, this filed set with 0.*/
    double tow;            /*GPS Time of Week, in seconds.*/
    UINT16 week_num;       /*GPS Week number*/
    UINT32 os_time;        /*TBD*/
    UINT32 abs_rms_acc;    /*Absolute Pulse RMS Accuracy, in microseconds.*/
    UINT32 rel_rms_acc;    /*Relative Pulse RMS Accuracy, in nanoseconds. Not used and set 0xFFFFFFFF.*/
} precise_time_info;

typedef struct {
    UINT32 freq_data_type; /*00: Should not be used, 01: Absolute center freq of the ECLK (Nominal Freq + delta), */
                           /*                        02: Delta from the nominal frequency*/
    UINT32 accu_data_type; /*00: Should not be used, 01: in units of PPM, 02: in units of Hz*/
    UINT32 os_time;        /*TBD*/
    INT32 cal_ppb;         /*Clock Frequency Calibration value, in ppb*/
    double cal_rms_ppb;    /*Calibration RMS, in ppb*/
    double freq;           /*e.g. If accuracy data type set 0x2, then 19.6698 MHz => (19.6608 * 1000000)*/
    double accu;           /*in ppb, e.g, if GPS requirement is 0.1 ppm, the set 0.1*/
} freq_aiding_info;

typedef struct{
    UINT8 duration;        /* in units of Seconds */
} freq_aiding_cfg;

typedef struct {
    UINT8 sv_id;           /*Satellite vehicle ID*/
    UINT8 sv_cn0;          /*Satellite C/No. [db-HZ], range [0..63]*/
    UINT8 mulpath_ind;     /*Pseudorange Multipath Indicator, range [0..3]*/
    UINT8 ps_rms_err;      /*Pseudorange RMS Error, Range [0..63]*/
    INT16 ps_dopp;         /*Measured Doppler frequency, in units of 0.2Hz, range [-6553.6..+6553.4] Hz*/
    UINT16 sv_code_ph_whole;    /*Satellite code Phase Whole Chips, in units of 1 GPS chip, range [0..1022]*/
    UINT16 sv_code_ph_fract;    /*Satellite code Phase Fractional Chips, in units of 1/2^10 of GPS chips, range [0.. (2^10-1)/2^10] GPS chips*/
} prm_data;

typedef struct {
    UINT8 prm_valid;          /*0--not valid, 1--valid*/
    UINT32 meas_tow;       /*Measurement GPS Time of Week, in units of 1ms*/
    UINT8 meas_tow_unc;    /*Measurement GPS Time of Week Uncertainty.*/
    UINT8 meas_num;        /*Number of measurements, Range [0..15]*/
    prm_data meas_data[MAX_SV_NUM];
} prm_data_info;

typedef struct {
    BOOL bAddrValid;
    BOOL IPType;
    UINT32 Ip4Addr;
    UINT32 IP6Addr[4];
    UINT32 PortNum;
    BOOL bURLValid;
    char URLAddr[256];
}pde_config_info;

typedef struct {
    UINT8 sv_id;              /*Satellite Vehicle ID*/
    INT8 doppler1;           /*Doppler 1st order term Hz/s, BS shall set the field to the two's complement value */
                              /*of the 1st order doppler, in units of 1/64 Hz/s, in the range from -1Hz/s to +63/64 Hz/s*/
    UINT8 doppler_win;        /*Satellite doppler uncertainty, range [0..4] Please refer to IS801-1 page 4.30*/
    UINT8 sv_code_phase_int;  /*Integer number of Code periods that have dlapsed since the latest GPS bit boundary, range [0..19]*/
    UINT8 gps_bit_num;        /*GPS bit number relative to GPS_TOW, range [0..3]*/
    UINT8 sv_code_phase_win;  /*Total code phase window, range [0..31]*/
    UINT8 azimuth;            /*Satellite Azimuth, in units of 11.25 degrees*/
    UINT8 elevation;          /*Satellite Elevation, in units of 11.25 degrees*/
    UINT16 sv_code_phase;     /*The GPS Code Phase, range [0..1022] chips*/
    INT16 doppler0;           /*Doppler 0th order term, in units of 2.5Hz, in the range from -5120Hz to +5117.5 Hz*/
} acqassist_data;

typedef struct {
    UINT32 ref_tow;        /*GPS time of week in ms at AA's Time of Application*/
    UINT8 sv_num;          /*Number of satellites for which data is available*/
    UINT8 doppler0_inc;    /*Doppler0 field included or not, 0: Not included, 1: included*/
    UINT8 add_doppler_inc; /*Doppler1 field included or not, 0: Not included, 1: included*/
    UINT8 code_phase;      /*Code Phase information included or not, 0: Not included, 1: included*/
    UINT8 az_el;           /*Azimuth and Elevation angle included or not, 0: Not included, 1: included*/
    acqassist_data  aa_data[MAX_SV_NUM];  /*AA data array */
} acqassist_resp;

typedef struct {
//TODO
} reset_assistdata_type;

typedef struct {
    UINT8 Velocity_Incl;    /*Velocity information included or not. 0: Not included, 1: included. */
    UINT8 Height_Incl;      /*Height and Loc Unc V included or not. 0: Not included, 1: included. */
    UINT8 Clock_incl;       /*Clock Information included or not. 0: Not included, 1: included. */
    UINT8 FixType;          /*0: 2D Fix, 1:3D Fix */
    INT16 Loc_Unc_ang;      /*Angle of axis with respect to True North for position uncertainty*/
    INT32 Clock_bias;       /*Clock Bias, Range [-13000..+249143nsec] */
    INT16 Clock_drift;      /*Clock Drift, Range [-32768 .. +32767] ppb, in units of ppb (ns/s)*/
    float Latitude;         /*Latitude, In Degrees north of equator. */
    float Longitude;        /*Longitude, In Degrees west of Greenwich meridian */
    float Loc_Unc_A;        /*Standard deviation of axis along angle specified for position uncertainty. In units meters.*/
    float Loc_Unc_P;        /*Standard deviation of axis perpendicular to angle specified for position uncertainty.  In units meters.*/
    float Velocity_Hor;     /*Horizontal velocity Magnitude. In units meter/sec. Range [0..127.75]. */
    float Heading;          /*Heading. Range [ 0..360(1-2**10) degrees, */
    float Height;           /*Height, range [-500..+15883] m, */
    float Vvelocity;        /*Vertical velocity. Range [-64 .. +63.5] m/s. Thus [PDE value / 2] m/s */
    float Loc_Unc_V;        /*Standard deviation of vertical error for position uncertainty.   In units meters.*/
} aflt_refloc_data;

typedef struct
{
   UINT16 Year;
   UINT16 Month;
   UINT16 DayOfWeek;
   UINT16 Day;
   UINT16 Hour;
   UINT16 Minute;
   UINT16 Second;
   UINT16 Milliseconds;
   UINT32 GPSweek;          /*GPS week as the number of whole weeks since GPS time zero*/
   UINT32 GPSTimeOfWeek;    /*GPS time of week in milliseconds*/
} gps_sys_time;

#if 0
typedef struct {
    gps_sys_time UTCTime;    /*Current System Date and Time */
    double latitude;         /*Latitude, In degrees north of equator*/
    double longitude;        /*Longitude, in degrees west of greenwich meridian*/
    float loc_unc_ang;       /*Angle of axis with respect to true north for position. in degrees.*/
    float loc_unc_a;         /*Standard deviation of axis alone angle specified for position uncertainty. In meters.*/
    float loc_unc_p;         /*Standard deviation of axis perpendicular to angle specified for position uncertainty.  In meters.*/
    UINT8 fix_type;          /*Fix Type. 0: 2D and 1: 3D fix*/
    float velocity_hor;      /*Horizontal velocity magnitude. In Meters/Seconds. */
    float heading;           /*Heading. In Degrees. */
    float velocity_ver;      /*Vertical velocity.  In Meters/seconds*/
    INT32 height;            /*Height. In Meters. */
    float loc_unc_v;         /*Standard deviation of vertical for position uncertainty. */
} posi_data;
#endif

enum gps_fix_quality
{
   GPS_FIX_QUALITY_UNKNOWN, /*Fix uses information from GPS satellites only.*/
   GPS_FIX_QUALITY_GPS,     /*Fix uses information from GPS satellites and also a differential GPS (DGPS) station. */
   GPS_FIX_QUALITY_DGPS,
   GPS_FIX_QUALITY_MAX=0x10000000
} ;

enum gps_fix_type
{
   GPS_FIX_UNKNOWN,
   GPS_FIX_2D,
   GPS_FIX_3D,
   GPS_FIX_TYPE_MAX = 0x10000000,
} ;

typedef struct {
    UINT32 sess_id;         /*for multiple fixes*/
} gps_rpc_fix_id;

typedef struct{
    UINT32 sess_id;
    UINT8 fix_event;/*0: start; 1:stop; 2 in progress*/
    UINT8 event_type;/*0: MPC ERROR  1: PDE ERROR*/
    UINT32 GPSSessionCount; /*total fix number*/
    UINT32 NumberOfPositions; /*current fix number*/
}gps_event_resp;

enum gps_selection_type
{
   GPS_FIX_SELECTION_UNKNOWN,
   GPS_FIX_SELECTION_AUTO,
   GPS_FIX_SELECTION_MANUAL,
   GPS_FIX_SELECTION_TYPE_MAX=0x10000000,
} ;

typedef struct
{
   UINT32 HorizontalErrorAlong;
   UINT32 HorizontalErrorAngle;
   UINT32 HorizontalErrorPerp;
   UINT32 VerticalError;
   UINT32 HorizontalConfidence;
   UINT32 HorizontalVelocityError;  /*Horizontal velocity uncertainty in m/s*/
   UINT32 VerticalVelocityError;    /*Vertical velocity uncertainty in m/s*/
   UINT32 HorinzontalHeadingError;  /*Horizontal heading uncertainty in degrees*/
   UINT32 LatitudeUncertainty;      /*Latitude uncertainty*/
   UINT32 LongitudeUncertainty;     /*Longitude Uncertainty*/
}gps_position_error;

enum gps_fix_mode_t
{
   GPS_RPC_FIX_MODE_UNKNOWN = 0,
   GPS_RPC_FIX_MODE_MSA,
   GPS_RPC_FIX_MODE_MSB,
   GPS_RPC_FIX_MODE_MSS,
   GPS_RPC_FIX_MODE_AFLT,
   GPS_RPC_FIX_MODE_SPEED_OPTIMAL,
   GPS_RPC_FIX_MODE_ACCURACY_OPTIMAL,
   GPS_RPC_FIX_MODE_DATA_OPTIMAL,
   GPS_RPC_FIX_MODE_CONTROL_PLANE,
   GPS_RPC_FIX_MODE_COUNT,
   GPS_RPC_FIX_MODE_MAX = 0x10000000,
} ;

enum gps_valid_mask
{
   GPS_RPC_POSITION_UTC_TIME =                                       0x000001,
   GPS_RPC_POSITION_LATITUDE =                                       0x000002,
   GPS_RPC_POSITION_LONGITUDE =                                      0x000004,
   GPS_RPC_POSITION_SPEED =                                          0x000008,
   GPS_RPC_POSITION_HEADING =                                        0x000010,
   GPS_RPC_POSITION_MAGNETIC_VARIATION =                             0x000020,
   GPS_RPC_POSITION_WRT_SEA_LEVEL =                                  0x000040,
   GPS_RPC_POSITION_WRT_ELLIPSOID =                                  0x000080,
   GPS_RPC_POSITION_DILUTION_OF_PRECISION =                          0x000100,
   GPS_RPC_POSITION_HORIZONTAL_DILUTION_OF_PRECISION =               0x000200,
   GPS_RPC_POSITION_VERTICAL_DILUTION_OF_PRECISION =                 0x000400,
   GPS_RPC_POSITION_VALID_SATELLITE_COUNT =                          0x000800,
   GPS_RPC_POSITION_VALID_SATELLITE_USED_PRNS =                      0x001000,
   GPS_RPC_POSITION_VALID_SATELLITE_IN_VIEW =                        0x002000,
   GPS_RPC_POSITION_VALID_SATELLITE_IN_VIEW_PRNS =                   0x004000,
   GPS_RPC_POSITION_VALID_SATELLITE_IN_VIEW_ELEVATION =              0x008000,
   GPS_RPC_POSITION_VALID_SATELLITE_IN_VIEW_AZIMUTH =                0x010000,
   GPS_RPC_POSITION_VALID_SATELLITE_IN_VIEW_SIGNAL_TO_NOISE_RATIO =  0x020000,
   GPS_RPC_POSITION_UNCERTAINTY_ERROR =                              0x040000,
   GPS_RPC_POSITION_FIX_MODE =                                       0x080000,
   GPS_RPC_POSITION_FIX_ERROR =                                      0x100000,
} ;

typedef struct {
   UINT32 ValidityMask;     /*gps_valid_mask*/
   gps_sys_time UTCTime;
   double Latitude;          /*in degrees, positive number indicate north latitude*/
   double Longitude;         /*in degrees, positive number indicate east longitude*/
   double  Speed;           /*in knots (nautical miles)*/
   double  Heading;         /*in degrees, a heading of zero is true north*/
   double  MagneticVariation;    /*the difference between the bearing to true north and the bearing shown
                                    on a magnetic compass, positive numbers indicate east*/
   double  AltitudeWRTSeaLevel;  /*in meters, with respect to sea level*/
   double  AltitudeWRTEllipsoid; /*in meters, with respect to the WGS84 ellipsoid*/

   enum gps_fix_quality FixQuality;
   enum gps_fix_type  FixType;
   enum gps_selection_type  SelectionType;

   double  PositionDilutionOfPrecision;
   double  HorizontalDilutionOfPrecision;
   double  VerticalDilutionOfPrecision;
   UINT16  SatelliteCount; /*number of satellites used to obtain the position*/

   UINT16  SatellitesUsedPRNs[MAX_SV_NUM];
   UINT16  SatellitesInView;
   UINT16  SatellitesInViewPRNs[MAX_SV_NUM];
   INT16  SatellitesInViewElevation[MAX_SV_NUM];
   UINT16  SatellitesInViewAzimuth[MAX_SV_NUM];
   UINT16  SatellitesInViewSNR[MAX_SV_NUM];
   gps_position_error  GPSPositionError;
   enum gps_fix_mode_t  FixMode;
   UINT32 GPSSessionCount;         /*the number of GPS fixes attempted*/
   UINT32 NumberOfPositions;       /*the number of positions*/
   UINT32 HorizontalVelocity;      /*Horizontal velocity in m/s*/
   UINT32 VerticalVelocity;        /*Vertical velocity in m/s*/
}posi_data;

typedef struct {
    UINT8 loc_result;         /*0--not valid, 1 valid*/
    gps_sys_time UTCTime;    /* Current System Date and Time */
    double   Latitude;            /* In units degree, computed (LAT * 180/2^25) degrees */
                                                /*   Range [-90..+90x(1-2^-24)]degrees */
                                                /*   positive angles north of the equator and negative angles south of the equator.*/
    double   Longitude;        /* In units degrees, computed (LONG * 360/2^26) degrees */
                                                /*   Range [-180 .. +180x(1-2^-25)] degrees */
                                                /*   Positive angles east of the Greenwich meridian and negative angles west */
    float    LocUncAng;        /* In units degrees, computed (ANG * 5.625) degrees */
                                                /*   Range [0..84.375] degrees. */
    float    LocUncAx;            /* In uints meters, Converted Position Table 4.2.4.2-6 */
    float    LocUncPe;            /* In uints meters, Converted Position Table 4.2.4.2-6 */
    UINT8    FixType;             /* 0: For 2D Fix, 1: 3D fix */
    float    VelocityHor;         /* In units of meter/seconds, computed (VH x 0.25) meter/seconds */
                                                 /*   Range [0..127.75] meter/seconds */
    float    Heading;               /* In units degrees, computed (Heading * (360/2^10)) */
                                                /*   Range [0..360x (1-2^-10)] degrees and a heading of zero is true north*/
    float    VelocityVer;         /* In units of meter/seconds, computed (VV x 0.5) meter/seconds */
                                                /*   Range [-64..+63.5] meter/seconds */
    INT32    Height;              /* In units of meter, Binary value of the field conveys the hight plus 500m */
    float    LocUncVe;            /* In uints meters, Converted Position Table 4.2.4.2-6 */
} posi_data_resp;

typedef struct {
    UINT8 mode;            /*Unkown:0, MSA:1, MSB:2, MSS:3, and Control Plane:4*/
    UINT32 fix_num;        /*A value of 1 means is interested in only one fix.
                              A value > 1, multiple fixes with some time in btw the attempts.*/
    UINT32 fix_time;       /*Time, in seconds, between position fix attempts.*/
    UINT32 qos_haccu;      /*Horizontal Accuracy, in meters*/
    UINT32 qos_vaccu;      /*Vertical Accuracy, in meters*/
    UINT32 qos_perf;       /*Performance response quality in terms of time, in seconds*/
} rpc_agps_type;

typedef struct {
    UINT8 resp ;
}gps_rpc_resp_type ;

/*===========================================================================
DESCRIPTION
   AP sent to CBP to request CBP init AGPS.

parameter: NULL

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/

#define GPS_RPC_REQUEST_AGPS_INIT 1

/*===========================================================================
DESCRIPTION
   AP sent to CBP to CBP by AP to start GPS engine with configuration.

parameter: rpc_agps_type

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/

#define GPS_RPC_REQUEST_AGPS_START 2

/*===========================================================================
DESCRIPTION
   AP sent to CBP to request Assist Data.

PARAMETER
   rpc_gps_assist_data_type

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/

#define GPS_RPC_REQUEST_ASSIST_DATA 3


/*===========================================================================
DESCRIPTION
   AP sent to CBP to request start fix.

PARAMETER
   NULL

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/

#define GPS_RPC_REQUEST_START_FIX 4

/*===========================================================================
DESCRIPTION
   AP sent to CBP to request stop fix.

PARAMETER
   NULL

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/

#define GPS_RPC_REQUEST_STOP_FIX 5

/*===========================================================================
DESCRIPTION
   AP sent to CBP to close rpc device.

PARAMETER
   NULL

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/

#define GPS_RPC_REQUEST_RPC_CLOSE 6

/*===========================================================================
DESCRIPTION
   AP sent to CBP to request precise time aiding.

PARAMETER
   NULL

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/

#define GPS_RPC_REQUEST_TIME_AIDING 7


/*===========================================================================
DESCRIPTION
   AP sent to CBP to request frequency aiding.

PARAMETER
   freq_aiding_cfg

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/

#define GPS_RPC_REQUEST_FREQ_AIDING 8

/*===========================================================================
DESCRIPTION
   AP writes PDE server address/port to CBP.

PARAMETER
   pde_config_info

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/
#define GPS_RPC_REQUEST_SET_PDE_SERVER 9

/*===========================================================================
DESCRIPTION
   AP sent to CBP to report pseudo range measurement data

PARAMETER
   prm_data_info

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/
#define GPS_RPC_REQUEST_REPORT_PRM_DATA 10

/*===========================================================================
DESCRIPTION
   AP sets GPS parameters, such as QOS, fix rate, fix performance and fix mode to CBP.

PARAMETER
   gps_session_cfg

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/
#define GPS_RPC_REQUEST_SET_POSITION_MODE 11


/*===========================================================================
DESCRIPTION
   AP sets the MPC server address and port for China Telecom carrier..

PARAMETER
   gps_mcp_cfg.

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/
#define GPS_RPC_REQUEST_SET_MPC_SERVADDR 12

/*===========================================================================
DESCRIPTION
   AP send to CBP to request reference location based on acquired CDMA network

PARAMETER
   NULL

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/
#define GPS_RPC_REQUEST_REFLOC_DATA 13

/*===========================================================================
DESCRIPTION
    For PLTS, AP GPS engine provide location update to CBP and then pass on to PLTS.

PARAMETER
   posi_data

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/
#define GPS_RPC_REQUEST_PROVIDE_REFLOC_DATA 14

/*===========================================================================
DESCRIPTION
    .AP GPS engine send to CBP to request start fix.

PARAMETER
   gps_power_on_type

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/
#define GPS_RPC_REQUEST_POWER_ON 15

/*===========================================================================
DESCRIPTION
    .AP GPS engine send to CBP to ask it stop GPS session.

PARAMETER
   gps_rpc_fix_id

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/
#define GPS_RPC_REQUEST_STOP 16

/*===========================================================================
DESCRIPTION
    .AP GPS engine send to tell CBP it has received stop request from CBP.

PARAMETER
   gps_rpc_fix_id

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/
#define GPS_RPC_REQUEST_SEND_STOP_ACK 17

/*===========================================================================
DESCRIPTION
    .AP GPS engine send to CBP.

PARAMETER
   gps_rpc_resp_type

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/
#define GPS_RPC_REQUEST_NI_POS_RESP 18

/*===========================================================================
DESCRIPTION
    .AP GPS engine send to tell CBP it has received poweron request from CBP.

PARAMETER
   NULL

RETURN VALUE
   RPC_REQ_SUCCESS
   RPC_REQ_ERROR

===========================================================================*/
#define GPS_RPC_REQUEST_SEND_POWERON_ACK 19


/*===========================================================================
DESCRIPTION
  .AP GPS engine send to CBP to request AA data in MSA case.

PARAMETER
  NULL

RETURN VALUE
  RPC_REQ_SUCCESS
  RPC_REQ_ERROR

===========================================================================*/
#define GPS_RPC_REQUEST_SEND_AA_REQ 20

/*===========================================================================
DESCRIPTION
    RPC response message if CBP wants to report acquisition assist data.

PARAMETER
   acqassist_resp

===========================================================================*/
#define GPS_RPC_RESPONSE_REPORT_ACQASSIST_DATA  101

/*===========================================================================
FUNCTION    rpc_report_alm_callback

DESCRIPTION
   RPC response message if CBP wants to report almanac data.

PARAMETER
   alm_resp
===========================================================================*/
#define GPS_RPC_RESPONSE_REPORT_ALM  102

/*===========================================================================
DESCRIPTION
   RPC response message if CBP wants to report ephemeris data.

PARAMETER
   gps_eph_prn_resp

===========================================================================*/
#define GPS_RPC_RESPONSE_REPORT_EPH  103

/*===========================================================================
DESCRIPTION
   RPC response message if CBP wants to report ionospheric data.

PARAMETER
   ion_data

===========================================================================*/
#define GPS_RPC_RESPONSE_REPORT_ION  104

/*===========================================================================
DESCRIPTION
   RPC response message if CBP wants to report reference location in IS801-1 message.

PARAMETER
   aflt_refloc_data
===========================================================================*/
#define GPS_RPC_RESPONSE_REPORT_ALFT_LOC 105

/*===========================================================================
DESCRIPTION
   RPC response message if CBP wants GPS AL to erase assist data.

PARAMETER
   NULL

===========================================================================*/
#define GPS_RPC_RESPONSE_DELASSIST_REQ 106

/*===========================================================================
DESCRIPTION
   CBP send this response with configuration if it wants to start GPS engine.

PARAMETER
   gps_power_on_type

===========================================================================*/
#define GPS_RPC_RESPONSE_GPS_POWERON_REQ 107

/*===========================================================================
DESCRIPTION
   CBP send this response with configuration if it wants to cancel IS801-1 session.

PARAMETER

===========================================================================*/
#define GPS_RPC_RESPONSE_CANCEL_IS801_SESSION 108


/*===========================================================================
DESCRIPTION
   It is the asynchronous response for GPS_RPC_REQUEST_FREQ_AIDING.

PARAMETER: freq_aiding_info
===========================================================================*/
#define GPS_RPC_RESPONSE_REF_CLK  109


/*===========================================================================
DESCRIPTION
   It is the asynchronous response for GPS_RPC_REQUEST_TIME_AIDING.

PARAMETER
precise_time_info
===========================================================================*/
#define GPS_RPC_RESPONSE_FRAMESYNC  110

/*===========================================================================
DESCRIPTION
   For CP/MSA, CBP sends this response to provide location result calculated by PDE to AP.

PARAMETER
posi_data_resp
===========================================================================*/
#define GPS_RPC_RESPONSE_PARSED_POSI_REPORT  111


/*===========================================================================
DESCRIPTION
    CBP sends this response to report reference location. It is the asynchronous response
    for GPS_RPC_REQUEST_REFLOC_DATA.

PARAMETER
gps_refloc_data
===========================================================================*/
#define GPS_RPC_RESPONSE_REFLOC_REPORT  112

/*===========================================================================
DESCRIPTION
    CBP sends this response to AP to pass on the network initiated GPS session received by CDMA
    modem MT SMS.

PARAMETER
gps_rpc_notify_info
===========================================================================*/
#define GPS_RPC_RESPONSE_NETWORK_INITATED_SESSION_INFO 113

/*===========================================================================
DESCRIPTION
    CBP sends to AP to ask stop GPS.

PARAMETER
gps_rpc_fix_id
===========================================================================*/
#define GPS_RPC_RESPONSE_STOP_REQ 114


/*===========================================================================
DESCRIPTION
    CBP sends to tell AP it has received AP's start request
PARAMETER
NULL
===========================================================================*/
#define GPS_RPC_RESPONSE_START_ACK 115

/*===========================================================================
DESCRIPTION
    CBP sends to tell AP it has received AP's stop request
PARAMETER
gps_rpc_fix_id
===========================================================================*/
#define GPS_RPC_RESPONSE_STOP_ACK 116

/*===========================================================================
DESCRIPTION
    CBP sends to tell AP assist data has been finished.
PARAMETER
NULL
===========================================================================*/
#define GPS_RPC_RESPONSE_ASSIST_FINISH 117

/*===========================================================================
DESCRIPTION
    CBP sends to AP to ask stop GPS due to session exception..

PARAMETER
gps_event_resp
===========================================================================*/
#define GPS_RPC_RESPONSE_GPS_EVENT 118

/*===========================================================================
FUNCTION    send_request

DESCRIPTION
   AP sent requests to CBP.

PARAMETER:
id: request number
*param:  pointer to the data for transferring;
datalen:   length of the data.


RETURN VALUE
   RPC_LOC_API_SUCCESS

===========================================================================*/
int send_request(int id, void *param, int datalen);



/*===========================================================================
FUNCTION    response_callback

DESCRIPTION
   This is a callback which should be registered in GPS AL. RPC call this function to send response to GPS AL.

PARAMETER
id: request number
*resp:  pointer to  responsed data;
datalen:   length of the data.

===========================================================================*/
typedef void (* response_callback)(int id, void *resp, int datalen);


/*===========================================================================
FUNCTION    err_callback

DESCRIPTION
   This is a callback which should be registered in GPS AL. RPC call this function to send response to GPS AL.

PARAMETER
id: request or response number
errno:   error types.
RPC_LOC_API_GENERAL_FAILURE   1
RPC_LOC_API_UNSUPPORTED       2
RPC_LOC_API_INVALID_HANDLE    4
RPC_LOC_API_INVALID_PARAMETER 5
RPC_LOC_API_ENGINE_BUSY       6
RPC_LOC_API_PHONE_OFFLINE     7
RPC_LOC_API_TIMEOUT           8
===========================================================================*/
typedef void (*err_callback)(int id,  int errno);
/**
*  @brief GPS callback structure related to GPS / LBS procedures.
*  This must be marked as NULL if response is not expected.
*/
typedef struct {
    response_callback  resp_cb;                                                  /*RPC call this function to send response to GPS AL.*/
    err_callback err_cb;
} t_viarpc_Callbacks;

/**
  *  @brief  This function is for initializing the  GPS AL callback for RPC .
  *  @param callbacks          Structure which has pointer to various call back functions
  *  @return                   returns 0 on success
  */
int viarpc_init( t_viarpc_Callbacks *callbacks );
#ifdef __cplusplus
}
#endif
#endif
