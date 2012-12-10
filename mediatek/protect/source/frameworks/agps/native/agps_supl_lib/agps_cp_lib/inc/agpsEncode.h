/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/****************************************************************************************************
*****************************************************************************************************/
#include "typedef.h"


#define CP_MAX_SATELLITE_INFO 32
#define CP_MAX_GPS_ASSISTANCE_DATA_LEN 38

/* This structure is used to define satellite information */
typedef struct
{
    kal_uint8           sat_id;                 /* Satellite ID */
    kal_uint8           iODE;                   /* iODE */
} cp_mmi_satellite_info_struct;

/* This structure is used to define navigation model */
typedef struct
{
    kal_uint16                      gps_week;                       /* GPS week info. */
    kal_uint8                       gps_toe;                        /* GPS toe */
    kal_uint8                       nsat;                           /* Number of satellite */
    kal_uint8                       toe_limit;                      /* toe limit */
    kal_bool                        sat_info_used;                  /* If satellite info present */
    cp_mmi_satellite_info_struct    sat_info[CP_MAX_SATELLITE_INFO];/* Satellite information */
} cp_mmi_navigation_model_struct;

/* This structure is used to define the request of assistance data */
typedef struct
{
    kal_bool                            almanac;                    /* If alamanac needed */
    kal_bool                            utc_model;                  /* If utc model needed */
    kal_bool                            ionospheric_model;          /* If ionospheric model needed */
    kal_bool                            dgps_correction;            /* If dgps correction needed */
    kal_bool                            ref_location;               /* If reference location needed */
    kal_bool                            ref_time;                   /* If reference time needed */
    kal_bool                            acquisition_assist;         /* If acquisition assistence data needed */
    kal_bool                            realtime_integrity;         /* If realtime integrity needed */
    kal_bool                            navigation_model;           /* If navigation model needed */
    cp_mmi_navigation_model_struct      nav_model_data;             /* Navigation model data */
} cp_mmi_req_assist_data_struct;

/* accord to ts49.031 R6 */
kal_uint8 AsnEncode_SS2_gpsAssistanceData(cp_mmi_req_assist_data_struct *gpsData, kal_uint8 *encodeBuf);