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

/******************************************************************************
 *
 * $Id: AKCompass.h 304 2011-08-12 07:52:29Z kihara.gb $
 *
 * -- Copyright Notice --
 *
 * Copyright (c) 2004 Asahi Kasei Microdevices Corporation, Japan
 * All Rights Reserved.
 *
 * This software program is proprietary program of Asahi Kasei Microdevices
 * Corporation("AKM") licensed to authorized Licensee under Software License
 * Agreement (SLA) executed between the Licensee and AKM.
 *
 * Use of the software by unauthorized third party, or use of the software
 * beyond the scope of the SLA is strictly prohibited.
 *
 * -- End Asahi Kasei Microdevices Copyright Notice --
 *
 ******************************************************************************/
#ifndef AKMD_INC_AKCOMPASS_H
#define AKMD_INC_AKCOMPASS_H

#include "AKCommon.h"
#include "CustomerSpec.h"
#include "AK8963Driver.h" // for using BYTE

//**************************************
// Include files for AK8963  library.
//**************************************
#include "libAK8963/AK8963.h"
#include "libAK8963/AKConfigure.h"
#include "libAK8963/AKMDevice.h"
#include "libAK8963/AKCertification.h"
#include "libAK8963/AKDirection6D.h"
#include "libAK8963/AKHDOE.h"
#include "libAK8963/AKHFlucCheck.h"
#include "libAK8963/AKManualCal.h"
#include "libAK8963/AKVersion.h"

/*** Constant definition ******************************************************/
#define	THETAFILTER_SCALE	4128
#define	HFLUCV_TH		2500

#define	OUTBIT_14		0
#define	OUTBIT_16		1
#define	OUTBIT_INVALID		-1

/*** Type declaration *********************************************************/
typedef enum _AKMD_PATNO {
	PAT_INVALID = 0,
	PAT1,
	PAT2,
	PAT3,
	PAT4,
	PAT5,
	PAT6,
	PAT7,
	PAT8
} AKMD_PATNO;

/*! A parameter structure which is needed for HDOE and Direction calculation. */
typedef struct _AK8963PRMS{

	// Variables for magnetic sensor.
	int16vec	m_ho;
	int16vec	HSUC_HO[CSPEC_NUM_FORMATION];
	int32vec	m_ho32;
	int16vec	m_hs;
	int16vec	HFLUCV_HREF[CSPEC_NUM_FORMATION];
	AKSC_HFLUCVAR	m_hflucv;

	// Variables for Decomp8963.
	int16vec	m_hdata[AKSC_HDATA_SIZE];
	int16		m_hn;		// Number of acquired data
	int16vec	m_hvec;		// Averaged value
	int16vec	m_asa;

	// Variables for HDOE.
	AKSC_HDOEVAR	m_hdoev;
	AKSC_HDST	m_hdst;
	AKSC_HDST	HSUC_HDST[CSPEC_NUM_FORMATION];

	// Variables for formation change
	int16		m_form;
	int16		m_cntSuspend;

	// Variables for Direction6D.
	int16		m_ds3Ret;
	int16		m_hnave;
	int16vec	m_dvec;
	int16		m_theta;
	int16		m_delta;
	int16		m_hr;
	int16		m_hrhoriz;
	int16		m_ar;
	int16		m_phi180;
	int16		m_phi90;
	int16		m_eta180;
	int16		m_eta90;
	I16MATRIX	m_mat;
	I16QUAT		m_quat;

	// Variables for acceleration sensor.
	int16vec	m_avec;

	//I16MATRIX	m_hlayout[CSPEC_NUM_FORMATION];
	//I16MATRIX	m_alayout[CSPEC_NUM_FORMATION];
	AKMD_PATNO	m_layout;

	// Variables for decimation.
	int16		m_callcnt;

        // Variables for outbit.
	int16		m_outbit;

	// Ceritication
	uint8		m_licenser[AKSC_CI_MAX_CHARSIZE+1];	//end with '\0'
	uint8		m_licensee[AKSC_CI_MAX_CHARSIZE+1];	//end with '\0'
	int16		m_key[AKSC_CI_MAX_KEYSIZE];

	// base
	int32vec	m_hbase;
	int32vec	HSUC_HBASE[CSPEC_NUM_FORMATION];

} AK8963PRMS;


/*** Global variables *********************************************************/

/*** Prototype of function ****************************************************/

#endif //AKMD_INC_AKCOMPASS_H

