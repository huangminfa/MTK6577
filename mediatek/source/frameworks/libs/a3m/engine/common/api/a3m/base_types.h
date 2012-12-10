/**************************************************************************
 *
 * Copyright (c) 2010 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 ***************************************************************************/
/** \file
 * Generic type definitions
 */

#pragma once
#ifndef A3M_BASE_TYPES_H
#define A3M_BASE_TYPES_H

/**
 * \defgroup a3mTypesIntrinsic Wrappers for intrinsic types
 * \ingroup  a3mRefTypes
 *
 * A3M_INT8, A3M_UINT32, A3M_BOOL, A3M_FLOAT etc.
 *
 * These types are defined for use within the A3M and by A3M API users.
 *
 * @{
 */

/** Boolean type */
typedef bool               A3M_BOOL;

/** Boolean false */
const A3M_BOOL A3M_FALSE = false;

/** Boolean true */
const A3M_BOOL A3M_TRUE  = true;

/** Byte type used to reference raw memory where signed/unsigned has no
    meaning */
typedef unsigned char      A3M_BYTE;

/** 8-bit character type used in character string manipulation */
typedef char               A3M_CHAR8;

/** Signed 8-bit integer type used to represent a signed integer number for
    mathematical operations */
typedef signed char        A3M_INT8;

/** Unsigned 8-bit integer type used to represent an unsigned integer number
    for mathematical operations */
typedef unsigned char      A3M_UINT8;

/** Signed 16-bit integer */
typedef signed short       A3M_INT16;

/** Unsigned 16-bit integer */
typedef unsigned short     A3M_UINT16;

/** Signed 32-bit integer */
typedef signed int         A3M_INT32;

/** Unsigned 32-bit integer */
typedef unsigned int       A3M_UINT32;

/** Signed 64-bit integer */
typedef signed long long   A3M_INT64;

/** Unsigned 64-bit integer */
typedef unsigned long long A3M_UINT64;

/** 32-bit floating point number */
typedef float              A3M_FLOAT;

/** @} */


/******************************************************************************
 * Macros
 ******************************************************************************/
/** \ingroup a3mIntUtil
 *  Macro to remove compiler warnings for unused function parameters */
#define A3M_PARAM_NOT_USED(p)   ((void)(p))

#endif
