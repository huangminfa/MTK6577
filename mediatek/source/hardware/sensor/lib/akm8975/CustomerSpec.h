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
 * $Id: CustomerSpec.h 200 2010-03-19 10:25:52Z rikita $
 *
 * -- Copyright Notice --
 *
 * Copyright (c) 2009 Asahi Kasei Microdevices Corporation, Japan
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
#ifndef AKMD_INC_CUSTOMERSPEC_H
#define AKMD_INC_CUSTOMERSPEC_H

/*******************************************************************************
 User defines parameters.
 ******************************************************************************/

// Certification information
#define CSPEC_CI_AK_DEVICE	8975
#define CSPEC_CI_LICENSER	"ASAHIKASEI"
#define CSPEC_CI_LICENSEE	"MediaTek_Ref"

// Parameters for Average
//	The number of magnetic data block to be averaged.
//	 NBaveh*(*nh) must be 1, 2, 4, 8 or 16.
#define CSPEC_HNAVE		4

// Parameters for Direction Calculation
#define CSPEC_DVEC_X		0
#define CSPEC_DVEC_Y		0
#define CSPEC_DVEC_Z		0

////////////////////////////////////////
// AK8975
//2 AK8975 QFN Setting
//4 Formation 0 setting
extern int AK8975Form0Layout;
//4 Formation 1 setting, if no use this formation, 
//4 just set same value with formation 0 setting.
extern int AK8975Form1Layout;

//2 AK8975B BGA Setting
//4 Formation 0 setting
extern int AK8975BForm0Layout;
//4 Formation 1 setting, if no use this formation, 
//4 just set same value with formation 0 setting.
extern int AK8975BForm1Layout;
/*
// Formation 0
#define CSPEC_FORM0_HLAYOUT_11	0//-1
#define CSPEC_FORM0_HLAYOUT_12	1//0
#define CSPEC_FORM0_HLAYOUT_13	0
#define CSPEC_FORM0_HLAYOUT_21	-1//0
#define CSPEC_FORM0_HLAYOUT_22	0//1
#define CSPEC_FORM0_HLAYOUT_23	0
#define CSPEC_FORM0_HLAYOUT_31	0
#define CSPEC_FORM0_HLAYOUT_32	0
#define CSPEC_FORM0_HLAYOUT_33	1//-1

// Formation 1
#define CSPEC_FORM1_HLAYOUT_11	0
#define CSPEC_FORM1_HLAYOUT_12	-1
#define CSPEC_FORM1_HLAYOUT_13	0
#define CSPEC_FORM1_HLAYOUT_21	-1
#define CSPEC_FORM1_HLAYOUT_22	0
#define CSPEC_FORM1_HLAYOUT_23	0
#define CSPEC_FORM1_HLAYOUT_31	0
#define CSPEC_FORM1_HLAYOUT_32	0
#define CSPEC_FORM1_HLAYOUT_33	-1
*/
// Parameters for Acceleration sensor
// Formation 0
#define CSPEC_FORM0_ALAYOUT_11	1
#define CSPEC_FORM0_ALAYOUT_12	0
#define CSPEC_FORM0_ALAYOUT_13	0
#define CSPEC_FORM0_ALAYOUT_21	0
#define CSPEC_FORM0_ALAYOUT_22	1
#define CSPEC_FORM0_ALAYOUT_23	0
#define CSPEC_FORM0_ALAYOUT_31	0
#define CSPEC_FORM0_ALAYOUT_32	0
#define CSPEC_FORM0_ALAYOUT_33	1

// Formation 1
#define CSPEC_FORM1_ALAYOUT_11	0
#define CSPEC_FORM1_ALAYOUT_12	-1
#define CSPEC_FORM1_ALAYOUT_13	0
#define CSPEC_FORM1_ALAYOUT_21	1
#define CSPEC_FORM1_ALAYOUT_22	0
#define CSPEC_FORM1_ALAYOUT_23	0
#define CSPEC_FORM1_ALAYOUT_31	0
#define CSPEC_FORM1_ALAYOUT_32	0
#define CSPEC_FORM1_ALAYOUT_33	1

// measurement time + extra time
#define	CSPEC_TIME_MEASUREMENTDRDY	20

// The number of formation
#define CSPEC_NUM_FORMATION		2	

// the counter of Suspend
#define CSPEC_CNTSUSPEND_SNG	8

// Parameters for FctShipmntTest
//  1 : USE SPI
//  0 : NOT USE SPI(I2C)
#define CSPEC_SPI_USE			0     


/*** Deprecate ****************************************************************/
// Set Decimator for HDOEProcess( ) 
// 8-16Hz : 1
//   20Hz : 2
//   30Hz : 3
//   50Hz : 5
//  100Hz : 10
#define CSPEC_HDECIMATOR_SNG				1

// Default interval
#define CSPEC_INTERVAL_SNG				125000

#endif

