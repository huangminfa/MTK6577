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
 *  Workfile: AKDirection6D.h 
 *
 *  Author: Kitamura
 *  Date: 09/12/04 21:35
 *  Revision: 34
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
#ifndef AKSC_INC_AKDIRECTION6D_H
#define AKSC_INC_AKDIRECTION6D_H

#include "AKMDevice.h"


//========================= Constant definition =========================//

//========================= Type declaration  ===========================//

//========================= Prototype of Function =======================//
AKLIB_C_API_START
int16 AKSC_VNorm(
	const	int16vec*	v,			//(i)	: Vector
	const	int16vec*	o,			//(i)	: Offset
	const	int16vec*	s,			//(i)	: Amplitude
	const	int16		tgt,		//(i)	: Target sensitivity value
			int16vec*	nv			//(o)	: Resulted normalized vector
);

void AKSC_SetLayout(				//		: 
			int16vec*	v,			//(i/o)	: Magnetic/Gravity vector data
	const	I16MATRIX*	layout		//(i)	: Layout matrix
);

int16 AKSC_DirectionS3(
	const	uint8		licenser[],	//(i)	: Licenser
	const	uint8		licensee[],	//(i)	: Licensee
	const	int16		key[],		//(i)	: Key
	const	int16vec*	h,			//(i)	: Geomagnetic vector (offset and sensitivity are  compensated)
	const	int16vec*	a,			//(i)	: Acceleration vector (offset and sensitivity are  compensated)
	const	int16vec*	dvec,		//(i)	: A vector to define reference axis of the azimuth on the terminal coordinate system
	const	I16MATRIX*	hlayout,	//(i)	: Layout matrix for geomagnetic vector
	const	I16MATRIX*	alayout,	//(i)	: Layout matrix for acceleration vector
			int16*		theta,		//(o)	: Azimuth direction (degree)
			int16*		delta,		//(o)	: The inclination (degree)
			int16*		hr,			//(o)	: Geomagnetic vector size
			int16*		hrhoriz,	//(o)	: Horizontal element of geomagnetic vector
			int16*		ar,			//(o)	: Acceleration vector size
			int16*		phi180,		//(o)	: Pitch angle (-180 to +180 degree)
			int16*		phi90,		//(o)	: Pitch angle (-90 to +90 degree)
			int16*		eta180,		//(o)	: Roll angle  (-180 to +180 degree)
			int16*		eta90,		//(o)	: Roll angle  (-90 to +90 degree)
			I16MATRIX*	mat			//(o)	: Rotation matrix
);

int16 AKSC_ThetaFilter(			//(o)	: theta filterd
	const	int16	the,		//(i)	: current theta(Q6)
	const	int16	pre_the,	//(i)	: previous theta(Q6)
	const	int16	scale		//(i)	: Q12.
);
AKLIB_C_API_END

#endif

