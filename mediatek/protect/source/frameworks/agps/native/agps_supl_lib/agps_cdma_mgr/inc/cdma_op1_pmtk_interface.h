/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2012. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2012
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
 *  cdma_op1_pmtk_interface.h
 *
 * Project:
 * --------
 *   VIA G+C
 *
 * Description:
 * ------------
 *   The adaption layer between PMTK and VIA data transformation
 *
 ***************************************************************************/

#ifndef _CDMA_OP1_PMTK_INTERFACE_H
#define _CDMA_OP1_PMTK_INTERFACE_H
#ifdef __cplusplus
extern "C" {
#endif

#include <stdio.h>
#include "vagci-api.h"
#include "typedef.h"

//#define GPS_NMEA_SEND_BUFFER_SIZE               300
typedef struct {
    int pmtk_num;
    char pmtk_str[32][256];
} pmtk_array;

/*===========================================================================
DESCRIPTION
   Encode pseudo range measurement data from PTMK732

PARAMETER
   prm_data_info

===========================================================================*/
kal_bool cdma_op1_enc_prm_data(const pmtk_array* pmtk, prm_data_info* p_prm_data_info);


/*===========================================================================
DESCRIPTION
    Encode location result data from PMTK731

PARAMETER
   posi_data
===========================================================================*/
kal_bool cdma_op1_enc_posi(const char* pmtk, posi_data* p_posi_data);

/*===========================================================================
DESCRIPTION
    Decode acquisition assist data into correct PMTK messages PMTK718

PARAMETER
   acqassist_resp

===========================================================================*/
kal_bool cdma_op1_dec_acqassist(const acqassist_resp* p_acqassist_resp ,pmtk_array* p_pmtk_array );

/*===========================================================================
DESCRIPTION
    Decode almanac data into correct PMTK messages PMTK711

PARAMETER
   alm_resp

===========================================================================*/
kal_bool cdma_op1_dec_alm(const alm_resp* p_alm_resp ,pmtk_array* p_pmtk_array );

/*===========================================================================
DESCRIPTION
    Decode ephemeris data into correct PMTK messages PMTK710

PARAMETER
   gps_eph_prn_resp

===========================================================================*/
kal_bool cdma_op1_dec_gps_eph_prn(const gps_eph_prn_resp* p_gps_eph_prn_resp ,pmtk_array* p_pmtk_array );

/*===========================================================================
DESCRIPTION
    Decode ionospheric data into correct PMTK messages PMTK715

PARAMETER
   ion_data

===========================================================================*/
kal_bool cdma_op1_dec_ion(const ion_data* p_ion_data ,pmtk_array* p_pmtk_array );

/*===========================================================================
DESCRIPTION
    Decode reference location in IS801-1 message into correct PMTK messages PMTK713

PARAMETER
   aflt_refloc_data

===========================================================================*/
kal_bool cdma_op1_dec_aflt_refloc(const aflt_refloc_data* p_aflt_refloc_data ,pmtk_array* p_pmtk_array );

/*===========================================================================
DESCRIPTION
    Decode reference time into correct PMTK messages PMTK712

PARAMETER
   precise_time_info

===========================================================================*/
kal_bool cdma_op1_dec_precise_time(const precise_time_info* p_precise_time_info ,pmtk_array* p_pmtk_array );

/*===========================================================================
DESCRIPTION
    Decode reference location into correct PMTK messages PMTK713

PARAMETER
   gps_refloc_data

===========================================================================*/
kal_bool cdma_op1_dec_gps_refloc(const gps_refloc_data* p_gps_refloc_data ,pmtk_array* p_pmtk_array );

/*===========================================================================
DESCRIPTION
    Decode power on config data into correct PMTK messages PMTK293
gps_power_on_type
PARAMETER
   gps_power_on_type

===========================================================================*/
kal_bool cdma_op1_dec_qop(const gps_power_on_type* power_on_cfg ,pmtk_array* p_pmtk_array );

#ifdef __cplusplus
}
#endif
#endif
