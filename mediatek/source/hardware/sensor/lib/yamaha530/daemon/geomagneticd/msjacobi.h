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

/*
   CONFIDENTIAL
   Copyright(c) 2006-2007 Yamaha Corporation
*/
/*!
   @file msjacobi.h
   @brief An interface of "msjacobi.c"
   @date 2007/02/02
   @note revision: 22
*/

#ifndef __MSJACOBI_H_INCLUDED__
#define __MSJACOBI_H_INCLUDED__

#include "ms3axestypes.h"
#include "msfloat.h"

typedef struct {
    MSFLOAT value;
    Ms3AxesData vec;
} MsJacobiEigenPair;

typedef struct {
    MsJacobiEigenPair eigenpair[3];
} MsJacobiEigenPairSet;

typedef struct {
    MSFLOAT val[6];
} MsJacobiSymmetricMatrix33;

#ifdef __cplusplus
extern "C" {
#endif

/*!
  @brief
  Calculates eigen-values and eigen-vectors of a symmetric matrix.
  This function expects that the matrix is positive semidefinite
  such as covariant matrix, but can be applied for any symmetric matrix.
  @param[in] pCovariantMatrix
  A pointer to the matrix of which you want to calculate eigen values
  and eigen vectors.
  @param[out] pEigenPairSet
  Sets of pair of eigen-value and eigen-vector.
  The sets are sorted according to the value of eigen-values.
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT MsJacobiCalcEigenPair(const MsJacobiSymmetricMatrix33 *pCovariantMatrix,
                            MsJacobiEigenPairSet *pEigenPairSet);

#ifdef __cplusplus
}
#endif


#endif /* __MSJACOBI_H_INCLUDED__ */
