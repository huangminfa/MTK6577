/*
 * Copyright (C) 2011 ALPS ELECTRIC CO., LTD. All Rights Reserved.
 *           This file must be operated as  based on the NDA (Non-Disclosure Agreement)
 *            with  ALPS ELECTRIC CO., LTD 
 *
 */
#ifndef ACMDEF_H_
#define ACMDEF_H_
#include "acd_copt.h"
#define ACM_VERSION 0x0100
#define ACMOPT_TINT64
#ifdef _MSC_VER
#include "windows.h"
#ifdef __cplusplus
#define MACM_EXTERN extern "C" __declspec ( dllexport )
#else
#define MACM_EXTERN extern __declspec ( dllexport )
#define MACM_EXPORT __declspec ( dllexport )
#endif
#define MACM_API WINAPI
#define MACM_CALLBACK CALLBACK
#ifdef ACMOPT_NOWINBOOL
#undef TRUE
#undef FALSE
#endif
#else
#ifdef __cplusplus
#define MACM_EXTERN extern "C"
#define MACM_EXPORT
#else
#define MACM_EXTERN extern
#define MACM_EXPORT
#endif
#define MACM_API
#define MACM_CALLBACK
#endif
#ifdef ACMOPT_FORCEDUSE
#define MACM_FORCEDUSE(VARIABLE) VARIABLE = VARIABLE
#else
#define MACM_FORCEDUSE(VARIABLE)
#endif
#ifdef ACMOPT_REAL
#define ACD_PAI ((TReal)3.14159265358979323846)
#define MACD_RAD2DEG(RAD) (TReal)(((RAD) * (TReal)180.0 / ACD_PAI))
#define MACD_DEG2RAD(DEG) (TReal)(((DEG) * ACD_PAI / (TReal)180.0))
#endif
#ifndef ACMOPT_DEFINED_BASE
typedef void TAny;typedef signed char TInt8;typedef signed short TInt16;typedef signed int TInt;typedef signed long TInt32;
typedef unsigned char TUInt8;typedef unsigned short TUInt16;typedef unsigned int TUInt;typedef unsigned long TUInt32;
#endif
#ifndef ACMOPT_DEFINED_PTR
typedef TAny *PTAny;typedef TInt8 *PTInt8;typedef TInt16 *PTInt16;typedef TInt *PTInt;typedef TInt32 *PTInt32;typedef TUInt8 *
PTUInt8;typedef TUInt16 *PTUInt16;typedef TUInt *PTUInt;typedef TUInt32 *PTUInt32;
#endif
#ifndef ACMOPT_DEFINED_CONST
typedef const TAny *PCTAny;typedef const TInt8 *PCTInt8;typedef const TInt16 *PCTInt16;typedef const TInt *PCTInt;typedef const
TInt32 *PCTInt32;typedef const TUInt8 *PCTUInt8;typedef const TUInt16 *PCTUInt16;typedef const TUInt *PCTUInt;typedef const 
TUInt32 *PCTUInt32;
#endif
#ifndef ACMOPT_DEFINED_BOOL
#if 0
typedef enum {EFalse =0,ETrue =1 }TBool,*PTBool;typedef const TBool *PCTBool;
#else
#define EFalse 0
#define ETrue 1
typedef TInt TBool;
#endif
#endif
#ifdef _MSC_VER
typedef signed __int64 TInt64;typedef unsigned __int64 TUInt64;typedef TInt64 *PTInt64;typedef TUInt64 *PTUInt64;typedef const 
TInt64 *PCTInt64;typedef const TUInt64 *PCTUInt64;
#else
#ifdef ACMOPT_TINT64
typedef signed long long TInt64;typedef unsigned long long TUInt64;typedef TInt64 *PTInt64;typedef TUInt64 *PTUInt64;typedef 
const TInt64 *PCTInt64;typedef const TUInt64 *PCTUInt64;
#endif
#endif
#ifdef ACMOPT_REAL
#ifdef ACMOPT_DOUBLE
typedef double TReal;typedef double *PTReal;typedef const double *PCTReal;
#define REAL_MAX DBL_MAX
#define REAL_MIN DBL_MIN
#define ACDPOW pow
#define ACDSQRT sqrt
#define ACDFABS fabs
#define ACDASIN asin
#define ACDACOS acos
#define ACDATAN atan
#define ACDATAN2 atan2
#define ACDSIN sin
#define ACDCOS cos
#define ACDTAN tan
#else
typedef float TReal;typedef float *PTReal;typedef const float *PCTReal;
#define REAL_MAX FLT_MAX
#define REAL_MIN FLT_MIN
#define ACDPOW powf
#define ACDSQRT sqrtf
#define ACDFABS fabsf
#define ACDASIN asinf
#define ACDACOS acosf
#define ACDATAN atanf
#define ACDATAN2 atan2f
#define ACDSIN sinf
#define ACDCOS cosf
#define ACDTAN tanf
#endif
#define ACDPOW2(NNN) ACDPOW(NNN, (TReal)2.0)
#endif
#endif

