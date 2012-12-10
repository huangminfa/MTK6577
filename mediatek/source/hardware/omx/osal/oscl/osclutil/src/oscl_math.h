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

/* ------------------------------------------------------------------
 * Copyright (C) 1998-2009 PacketVideo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 * -------------------------------------------------------------------
 */
// -*- c++ -*-
// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

//                     O S C L _ M A T H

// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

/*! \addtogroup osclutil OSCL Util
 *
 * @{
 */


/*!
 * \file oscl_math.h
 * \brief Provides math functions.
 *
 */

#ifndef OSCL_MATH_H_INCLUDED
#define OSCL_MATH_H_INCLUDED

#ifndef OSCLCONFIG_UTIL_H_INCLUDED
#include "osclconfig_util.h"
#endif

#ifndef OSCL_BASE_H_INCLUDED
#include "oscl_base.h"
#endif

/**
 * Calculates the natural log of a number
 *
 * @param value  source value
 */
OSCL_COND_IMPORT_REF double oscl_log(double value);
/**
 * Calculates tthe logarithm to base 10 of a number
 *
 * @param value  source value
 */
OSCL_COND_IMPORT_REF double oscl_log10(double value);
/**
 * Calculates the square root of a number
 *
 * @param value  source value
 */
OSCL_COND_IMPORT_REF double oscl_sqrt(double value);
/**
 * Calculates the value of x to the power of y
 *
 * @param x  base value
 * @param y  power
 */
OSCL_COND_IMPORT_REF double oscl_pow(double x, double y);
/**
 * Calculates the exponential of e for a number
 *
 * @param value  source value
 */
OSCL_COND_IMPORT_REF double oscl_exp(double value);

/**
 * Calculates the sine of a number
 *
 * @param value  source value
 */
OSCL_COND_IMPORT_REF double oscl_sin(double value);

/**
 * Calculates the cosine of a number
 *
 * @param value  source value
 */
OSCL_COND_IMPORT_REF double oscl_cos(double value);

/**
 * Calculates the tangential of a number
 *
 * @param value  source value
 */
OSCL_COND_IMPORT_REF double oscl_tan(double value);

/**
 * Calculates the arc since of a number
 *
 * @param value  source value
 */
OSCL_COND_IMPORT_REF double oscl_asin(double value);

/**
 * Calculates the arc tangent of a number
 *
 * @param value  source value
 */
OSCL_COND_IMPORT_REF double oscl_atan(double value);

/**
 * Calculates the floor of a number
 *
 * @param value  source value
 */
OSCL_COND_IMPORT_REF double oscl_floor(double value);

#if (!OSCL_DISABLE_INLINES)
#include "oscl_math.inl"
#endif

#endif

/*! @} */

