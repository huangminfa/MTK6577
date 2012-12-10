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
   Copyright(c) 2006-2008 Yamaha Corporation
*/
/*!
   @file msfloat.h
   @brief Library for MSFLOAT-typed floating point number.
   @date 2008/11/05
   @note revision: 27
*/

#ifndef __MSFLOAT_H_INCLUDED__
#define __MSFLOAT_H_INCLUDED__

#include "mstypes.h"

#define MSFLOAT_ERR_ARG       (-1)
#define MSFLOAT_ERR_DIV0      (-2)
#define MSFLOAT_ERR_OVERFLOW  (-127)
#define MSFLOAT_ERR_UNDERFLOW (-126)

#define MSFLOAT_FRACTION_BITS 16

#if MSFLOAT_FRACTION_BITS == 16
  typedef MSUINT16 MSFLOAT_FRACTION;
#elif MSFLOAT_FRACTION_BITS == 32
  typedef MSUINT32 MSFLOAT_FRACTION;
#else
# error
#endif

 /*! The value expressed by this is 'sign' * 'fraction' * 2^('exponent' - 0x80).
  The concrete expression of zero is defined in "msfloat.c", and users of this library
  need not know it. And it is not recommended to access the members of this struct
  directly since they are what we call protected members in terms of object-oriented
  programming. */
typedef struct {
    MSSINT8 sign; /*!< Sign of value. If the value is positive this is 1, if negative -1, and if zero 0. */
    MSUINT8 exponent; /*!< Exponent part. */
    MSFLOAT_FRACTION fraction; /*!< Fraction part. The most significant bit of this is 1 in the case other than zero. */
} MSFLOAT;

#ifdef __cplusplus
extern "C" {
#endif

extern const MSFLOAT MSFLOAT_0;

/*!
  @brief
  Converts an MSSINT32-typed value to an MSFLOAT-typed.
  @param[out] pThis
  A pointer to an argument to which you want to assign the converted value.
  A converted value.
  @param[in] val
  An MSSINT32-typed value to be converted.
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT MSFLOAT_I2F(MSFLOAT *pThis, MSSINT32 val);

/*!
  @brief
  Converts an MSFLOAT-typed value to an MSSINT32-typed value.
  @param[out] pIntVal
  A pointer to an argument to which you want to assign the converted value.
  @param[in] pVal
  A pointer to the MSFLOAT-typed value which you want to convert to an MSSINT32-typed one.
  @return
  A non-negative number for success, or a negative number for failure.
  @note
  If the integerized value of '*pVal' exceeds the range which is
  able to be described with MSSINT32, this function returns an error code.
*/
MSINT MSFLOAT_F2I(MSSINT32 *pIntVal, const MSFLOAT *pVal);

/*!
  @brief
  Checks the sign of a value.
  @param[in] pThis
  A pointer to an argument which you want to check.
  @retval 2
  The value is positive.
  @retval 1
  The value is zero.
  @retval 0
  The value is negative.
  @retval negative-number
  Failure.
*/
MSINT MSFLOAT_Sign(const MSFLOAT *pThis);

/*!
  @brief
  Multiplies '*pThis' with '*pVal'.
  @param[out] pThis
  A pointer to the value to be multiplied with '*pVal'.
  @param[in] pVal
  A pointer to the value with which you want to multiply the '*pThis'.
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT MSFLOAT_OpMultiEq(MSFLOAT *pThis, const MSFLOAT *pVal);

/*!
  @brief
  Adds '*pVal' to '*pThis'.
  @param[out] pThis
  A pointer to the value to be added with '*pVal'.
  @param[in] pVal
  A pointer to the value to which you want to add '*pThis'.
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT MSFLOAT_OpPlusEq(MSFLOAT *pThis, const MSFLOAT *pVal);

/*!
  @brief
  Subtracts '*pVal' from '*pThis'.
  @param[out] pThis
  A pointer to the value to be subtracted with '*pVal'.
  @param[in] pVal
  A pointer to the value from which you want to subtract '*pThis'.
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT MSFLOAT_OpMinusEq(MSFLOAT *pThis, const MSFLOAT *pVal);

/*!
  @brief
  Divides '*pThis' with '*pVal'.
  @param[out] pThis
  A pointer to the value to be divided with '*pVal'.
  @param[in] pVal
  A pointer to the value with which you want to divide '*pThis'.
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT MSFLOAT_OpDivEq(MSFLOAT *pThis, const MSFLOAT *pVal);

/*!
  @brief
  Multiplies '*pThis' with integer power of 2.
  @param[out] pThis
  A pointer to the value to be multiplied.
  @param[in] bits
  An exponent value. '*pThis' is multiplied with 2 raised to the power of this.
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT MSFLOAT_OpShiftEq(MSFLOAT *pThis, MSSINT16 bits);

/*!
  @brief
  Multiplies '*pThis' with -1.
  @param[out] pThis
  A pointer to the value to be multiplied with -1.
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT MSFLOAT_OpNeg(MSFLOAT *pThis);

/*!
  @brief
  Converts '*pThis' to its absolute number.
  @param[out] pThis
  A pointer to the value.
  @return
  A non-negative number for success, or a negative number for failure.
*/
MSINT MSFLOAT_OpAbs(MSFLOAT *pThis);

/*!
  @brief
  Compares the absolute value of '*pThis' and that of '*pVal'.
  @param[in] pThis
  A pointer to the first value.
  @param[in] pVal
  A pointer to the second value.
  @retval 1
  The absolute value of '*pThis' is greater than that of '*pVal'.
  @retval 0
  The absolute value of '*pThis' is not greater than that of '*pVal'.
  @retval negative-number
  Failure.
*/
MSINT MSFLOAT_AbsCompGreater(const MSFLOAT *pThis, const MSFLOAT *pVal);

/*!
  @brief
  Compares '*pThis' and '*pVal'.
  @param[in] pThis
  A pointer to the first value.
  @param[in] pVal
  A pointer to the second value.
  @retval 1
  '*pThis' is greater than '*pVal'.
  @retval 0
  '*pThis' is not greater than '*pVal'.
  @retval negative-number
  Failure.
*/
MSINT MSFLOAT_OpCompGreater(const MSFLOAT *pThis, const MSFLOAT *pVal);

#ifdef MSFLOAT_TEST
double MSFLOAT_F2double(const MSFLOAT *pThis);
#endif

#ifdef __cplusplus
}
#endif

#endif /* __MSFLOAT_H_INCLUDED__ */
