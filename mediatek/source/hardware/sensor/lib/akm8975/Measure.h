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
 * $Id: Measure.h 211 2010-03-24 05:52:24Z rikita $
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
#ifndef AKMD_INC_MEASURE_H
#define AKMD_INC_MEASURE_H

// Include files for AK8975 library.
#include "AKCompass.h"

/*** Constant definition ******************************************************/
#define AKRET_PROC_FAIL         0x00	/*!< The process failes. */
#define AKRET_PROC_SUCCEED      0x01	/*!< The process has been successfully done. */
#define AKRET_FORMATION_CHANGED 0x02	/*!< The formation is changed */
#define AKRET_HFLUC_OCCURRED    0x03	/*!< A magnetic field fluctuation occurred. */
#define AKRET_DATA_OVERFLOW     0x04	/*!< Data overflow occurred. */
#define AKRET_DATA_READERROR    0x05	/*!< Data read error occurred. */

/*** Type declaration *********************************************************/

/*** Global variables *********************************************************/

/*** Prototype of function ****************************************************/
void InitAK8975PRMS(
	AK8975PRMS*	prms	/*!< [out] Pointer to parameters of AK8975 */
);

void SetDefaultPRMS(
	AK8975PRMS*	prms	/*!< [in,out] Pointer to parameters of AK8975 */
);

int16 ReadAK8975FUSEROM(
	AK8975PRMS*	prms	/*!< [out] Pointer to parameters of AK8975 */
);

int16 InitAK8975_Measure(
	AK8975PRMS*	prms	/*!< [in,out] Pointer to parameters of AK8975 */
);

int16 SwitchFormation(
	AK8975PRMS*	prms	/*!< [in,out] Pointer to parameters of AK8975 */
);

int16 FctShipmntTest_Body(
	AK8975PRMS*	prms	/*!< [in,out] Pointer to parameters of AK8975 */
);

int16 FctShipmntTestProcess_Body(
	AK8975PRMS*	prms	/*!< [in,out] Pointer to parameters of AK8975 */
);

void MeasureSNGLoop(
	AK8975PRMS*	prms	/*!< [in,out] Pointer to parameters of AK8975 */
);

int16 MeasuringEventProcess(
	const int16	bData[],	/*!< [in] Measuring block data */
	AK8975PRMS*	prms,		/*!< [in,out] Pointer to parameters of AK8975 */
	const int16	curForm,	/*!< [in] Current formation */
	const int16	hDecimator,	/*!< [in] Decimator */
	const int16	cntSuspend	/*!< [in] The counter of suspend */
);

int16 GetAccVec(
	AK8975PRMS * prms	/*!< [out] Pointer to parameters of AK8975 */
);


#endif
