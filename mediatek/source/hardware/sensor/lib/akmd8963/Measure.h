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
 * $Id: Measure.h 304 2011-08-12 07:52:29Z kihara.gb $
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
#ifndef AKMD_INC_MEASURE_H
#define AKMD_INC_MEASURE_H

// Include files for AK8963 library.
#include "AKCompass.h"
#include "misc.h"

/*** Constant definition ******************************************************/
#define AKRET_PROC_SUCCEED      0x00	/*!< The process has been successfully done. */
#define AKRET_FORMATION_CHANGED 0x01	/*!< The formation is changed */
#define AKRET_DATA_READERROR    0x02	/*!< Data read error occurred. */
#define AKRET_DATA_OVERFLOW     0x04	/*!< Data overflow occurred. */
#define AKRET_OFFSET_OVERFLOW	0x08	/*!< Offset values overflow. */
#define AKRET_HBASE_CHANGED	0x10	/*!< hbase was changed. */
#define AKRET_HFLUC_OCCURRED    0x20	/*!< A magnetic field fluctuation occurred. */
#define AKRET_VNORM_ERROR	0x40	/*!< AKSC_VNorm error. */
#define AKRET_PROC_FAIL         0x80	/*!< The process failes. */

#define AKMD_MAG_MIN_INTERVAL	 10000000	/*!< Minimum magnetometer interval */
#define AKMD_ACC_MIN_INTERVAL	 10000000	/*!< Minimum acceleration interval */
#define AKMD_ORI_MIN_INTERVAL	 10000000	/*!< Minimum orientation interval */
#define AKMD_LOOP_MARGIN	  3000000	/*!< Minimum sleep time */
#define AKMD_SETTING_INTERVAL	500000000	/*!< Setting event interval */

/*** Type declaration *********************************************************/
typedef int16(*OPEN_FORM)(void);
typedef void(*CLOSE_FORM)(void);
typedef int16(*CHECK_FORM)(void);

typedef struct _FORM_CLASS {
	OPEN_FORM	open;
	CLOSE_FORM	close;
	CHECK_FORM	check;
} FORM_CLASS;

/*** Global variables *********************************************************/

/*** Prototype of function ****************************************************/
void RegisterFormClass(
	FORM_CLASS* pt
);

void InitAK8963PRMS(
	AK8963PRMS*	prms
);

void SetDefaultPRMS(
	AK8963PRMS*	prms
);

int16 GetInterval(
	AKMD_LOOP_TIME* acc_acq,
	AKMD_LOOP_TIME* mag_acq,
	AKMD_LOOP_TIME* ori_acq,
	AKMD_LOOP_TIME* mag_mes,
	AKMD_LOOP_TIME* acc_mes,
	int16* hdoe_dec
);

int SetLoopTime(
	AKMD_LOOP_TIME* tm,
	int64_t execTime,
	int64_t* minDuration
);

int16 ReadAK8963FUSEROM(
	AK8963PRMS*	prms
);

int16 InitAK8963_Measure(
	AK8963PRMS*	prms
);

int16 FctShipmntTest_Body(
	AK8963PRMS*	prms
);

int16 FctShipmntTestProcess_Body(
	AK8963PRMS*	prms
);

void MeasureSNGLoop(
	AK8963PRMS*	prms
);

int16 GetMagneticVector(
	const int16	bData[],
	AK8963PRMS*	prms,
	const int16	curForm,
	const int16	hDecimator
);
int16 MeasuringEventProcess(
	const int16	bData[],	/*!< [in] Measuring block data */
	AK8963PRMS*	prms,		/*!< [in,out] Pointer to parameters of AK8963 */
	const int16	curForm,	/*!< [in] Current formation */
	const int16	hDecimator,	/*!< [in] Decimator */
	const int16	cntSuspend	/*!< [in] The counter of suspend */
);

int16 GetAccVec(
	AK8963PRMS * prms	/*!< [out] Pointer to parameters of AK8963 */
);
int16 CalcDirection(
	AK8963PRMS* prms
);

int16 GetAccVec(
	AK8963PRMS * prms	/*!< [out] Pointer to parameters of AK8975 */
);


#endif

