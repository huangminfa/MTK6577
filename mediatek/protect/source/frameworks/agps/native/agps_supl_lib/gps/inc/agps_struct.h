/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2007
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
*  agps_struct.h
*
* Project:
* --------
*  MAUI
*
* Description:
* ------------
*  AGPS related structure define in GPS task
*
* Author:
* -------
*  Hai Wang (MBJ06018)
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
 * Apr 17 2008 mbj06018
 * [MAUI_00655816] [AGPS]AGPS check in
 *
 *
 * Mar 24 2008 mbj06018
 * [MAUI_00640336] [AGPS]gps task assert of event scheduler
 *
 *
 * Feb 25 2008 mbj06018
 * [MAUI_00623349] [AGPS] AGPS feature check-in
 *
*------------------------------------------------------------------------------
* Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
*============================================================================
*******************************************************************************/
#ifndef __GPS_AGPS_H__
#define __GPS_AGPS_H__

#include "gps2lcsp_enum.h"
#include "gps2lcsp_struct.h"

#if defined(__AGPS_SUPPORT__)
#define GPS_AGPS_QOP_POS_ARRAY_MAX   GPS_AGPS_POS_REQ_TIMER_MAX
#define GPS_AGPS_QOP_MEAS_ARRAY_MAX  GPS_AGPS_MEAS_REQ_TIMER_MAX

typedef struct
{
    kal_uint16    bitmap;    /*see define of GPS_ASSIST_MASK_RTI enum*/
    gps_sat_related_data_struct sat_data;
} gps_assist_bitmap_struct;


typedef struct
{
    kal_uint16  frame;
    kal_uint16  week;
    kal_uint32  tow;
    kal_uint8   fix;
    double       latitude;         /*latitude */
    double       longtitude;       /* N2, encoded longtitude with 2's complement */
    double       altitude;         /* no encoding, unit in meter */
#if 1//Hiki, bugfix for uncertainty
    double       unc_major;        /* K1, encoded r1 */
    double       unc_minor;        /* K2, encoded r2 */
    kal_uint16   unc_bear;         /* N3, encoded a = 2N3, Bearing of semi-major axis (degrees)*/
    double       unc_vert;         /* K3, encode r3, vertical uncertain */
#else
    kal_uint16   unc_major;        /* K1, encoded r1 */
    kal_uint16   unc_minor;        /* K2, encoded r2 */
    kal_uint16   unc_bear;         /* N3, encoded a = 2N3, Bearing of semi-major axis (degrees)*/
    kal_uint16   unc_vert;         /* K3, encode r3, vertical uncertain */
#endif
    kal_uint8   confidence;       /* %, The confidence by which the position of a target
                                   entity is known to be within the shape description,
                                   expressed as a percentage. [0 ~ 100] (%)*/
    double       h_speed; /*horizontal speed*/
    kal_uint16   bearing;    /*Direction of the horizontal speed*/
    kal_uint32  ticks;
} gps_agps_pos_struct;

typedef struct
{
    gps_error_code_enum error_code;
} gps_agps_loc_error_struct;

#if 0
typedef struct
{
    kal_uint8   sat_id;           /*The particular satellite for which the measurement data is valid. [1 ~ 32]*/
    kal_uint8   carrier_noise;    /*The estimate of the carrier-to-noise ratio of the received signal from the particular satellite used in the measurement. [0 ~ 63] (dB-Hz)*/
    kal_uint16  doppler;          /*0.2 Hz The Doppler measured by the MS for the particular satellite signal. [-32768 ~ 32767]*/
    kal_uint16  whole_chips;      /*chips The whole value of the code-phase measurement made by the GPS receiver for the particular satellite signal at the time of measurement. [0 ~ 1022] (chips)*/
    kal_uint16  fractional_chips; /*2^-10 chips  The fractional value of the code-phase measurement made by the GPS receiver for the particular satellite signal at the time of measurement. (2^-10 C/A chips) [0 ~ 1023]*/
    kal_uint8   multipath;        /*The multipath Indicator value. Possible values are listed below:
                                   '0' = Not measured
                                   '1' = Low, MP error < 5m
                                   '2' = Medium, 5m < MP error < 43m
                                   '3' = High, MP error > 43m*/
    kal_uint8   pseudorang_m;      /*Pseudorange RMS Error Mantissa*/
    kal_uint8   pseudorang_e;      /*Pseudorange RMS Error Exponent*/
} gps_agps_meas_single_struct;


typedef struct
{
    kal_uint16  frame;/*default to 42432 (this value shall be ignored by server)*/
    kal_uint32  tow;
    /*GPS TOW [0 ~ 14399999] (msec) for which the location estimate is valid, rounded down to the nearest millisecond unit.
    RRLP GPS Measurement Information element only contains the 24 LSBs of GPS TOW.*/
    kal_uint8   sate_num;/*The number of GPS measurements for which measurement satellites are provided at the time of measurement*/
    kal_uint8   seq;
    gps_agps_meas_single_struct    meas_param[GPS_ASSIST_DATA_N_SATE_MEAS];
} gps_agps_meas_struct;
#endif

#endif /*__AGPS_SUPPORT__*/
#endif /*__GPS_AGPS_H__*/


