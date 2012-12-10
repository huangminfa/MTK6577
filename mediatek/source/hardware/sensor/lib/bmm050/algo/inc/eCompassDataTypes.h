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

/* EasyCASE V6.5 25/03/2011 19:26:56 */
/* EasyCASE O
If=vertical
LevelNumbers=no
LineNumbers=no
Colors=16777215,0,12582912,12632256,0,0,0,16711680,8388736,0,33023,32768,0,0,0,0,0,32768,12632256,255,65280,255,255,16711935
ScreenFont=Courier New,,80,4,-11,0,400,0,0,0,0,0,0,3,2,1,49,96,96
PrinterFont=Courier New,,80,4,-66,0,400,0,0,0,0,0,0,3,2,1,49,600,600
LastLevelId=1005 */
/* EasyCASE ( 1
   eCompassDataTypes.h */
#ifndef __ECOMPASSDATATYPES_H__
#define __ECOMPASSDATATYPES_H__
/* EasyCASE - */
/*
 ***************************************************************************************************
 *
 * (C) All rights reserved by ROBERT BOSCH GMBH
 *
 **************************************************************************************************/
/*  $Date: 2011/01/10
 *  $Revision: 1.0 $
 *
 */

/**************************************************************************************************
* Copyright (C) 2011 Bosch Sensortec GmbH
*
* 
*
* Usage:        
*               
* 
* Author:       david.job@in.bosch.com
**************************************************************************************************/
/* EasyCASE ( 72
   Disclaimer */
/*************************************************************************************************/
/*  Disclaimer
*
* Common:
* Bosch Sensortec products are developed for the consumer goods industry. They may only be used 
* within the parameters of the respective valid product data sheet.  Bosch Sensortec products are 
* provided with the express understanding that there is no warranty of fitness for a particular purpose. 
* They are not fit for use in life-sustaining, safety or security sensitive systems or any system or device 
* that may lead to bodily harm or property damage if the system or device malfunctions. In addition, 
* Bosch Sensortec products are not fit for use in products which interact with motor vehicle systems.  
* The resale and/or use of products are at the purchaser’s own risk and his own responsibility. The 
* examination of fitness for the intended use is the sole responsibility of the Purchaser. 
*
* The purchaser shall indemnify Bosch Sensortec from all third party claims, including any claims for 
* incidental, or consequential damages, arising from any product use not covered by the parameters of 
* the respective valid product data sheet or not approved by Bosch Sensortec and reimburse Bosch 
* Sensortec for all costs in connection with such claims.
*
* The purchaser must monitor the market for the purchased products, particularly with regard to 
* product safety and inform Bosch Sensortec without delay of all security relevant incidents.
*
* Engineering Samples are marked with an asterisk (*) or (e). Samples may vary from the valid 
* technical specifications of the product series. They are therefore not intended or fit for resale to third 
* parties or for use in end products. Their sole purpose is internal client testing. The testing of an 
* engineering sample may in no way replace the testing of a product series. Bosch Sensortec 
* assumes no liability for the use of engineering samples. By accepting the engineering samples, the 
* Purchaser agrees to indemnify Bosch Sensortec from all claims arising from the use of engineering 
* samples.
*
* Special:
* This software module (hereinafter called "Software") and any information on application-sheets 
* (hereinafter called "Information") is provided free of charge for the sole purpose to support your 
* application work. The Software and Information is subject to the following terms and conditions: 
*
* The Software is specifically designed for the exclusive use for Bosch Sensortec products by 
* personnel who have special experience and training. Do not use this Software if you do not have the 
* proper experience or training. 
*
* This Software package is provided `` as is `` and without any expressed or implied warranties, 
* including without limitation, the implied warranties of merchantability and fitness for a particular 
* purpose. 
*
* Bosch Sensortec and their representatives and agents deny any liability for the functional impairment 
* of this Software in terms of fitness, performance and safety. Bosch Sensortec and their 
* representatives and agents shall not be liable for any direct or indirect damages or injury, except as 
* otherwise stipulated in mandatory applicable law.
* 
* The Information provided is believed to be accurate and reliable. Bosch Sensortec assumes no 
* responsibility for the consequences of use of such Information nor for any infringement of patents or 
* other rights of third parties which may result from its use. No license is granted by implication or 
* otherwise under any patent or patent rights of Bosch. Specifications mentioned in the Information are 
* subject to change without notice.
*
* It is not allowed to deliver the source code of the Software to any third party without permission of 
* Bosch Sensortec.
*/
/*************************************************************************************************/
/* EasyCASE ) */
/* EasyCASE ( 913
   File Name For Doxy */
/*! \file eCompassDataTypes.h
    \brief Contains all the definitions & declarations used in the eCompass Software */
/* EasyCASE ) */
/* EasyCASE ( 73
   Includes */
/* EasyCASE ( 912
   Standard includes */
#include <limits.h>

/* EasyCASE ( 75
   #Define Constants */
/* EasyCASE = */
/* EasyCASE ( 1004
   typedefs */
#if USHRT_MAX == 0xFFFF
    typedef unsigned short ECOMPASS_U16; // 16 bit achieved with short
    typedef signed short ECOMPASS_S16;
#elif UINT_MAX == 0xFFFF
        typedef unsigned int ECOMPASS_U16;  // 16 bit achieved with int
        typedef signed int ECOMPASS_S16;
#else
    #error U16 and S16 could not be defined automatically, please do so manually
#endif

// find correct data type for signed 32 bit variables
#if INT_MAX == 0x7FFFFFFF
        typedef signed int ECOMPASS_S32; // 32 bit achieved with int
        typedef unsigned int ECOMPASS_U32;
#elif LONG_MAX == 0x7FFFFFFF
    typedef signed long int ECOMPASS_S32;       // 32 bit achieved with long int
        typedef unsigned long int ECOMPASS_U32; // 32 bit achieved with long int
#else
    #error S32 could not be defined automatically, please do so manually
#endif

typedef signed char           ECOMPASS_S8;
typedef unsigned char         ECOMPASS_U8;
typedef signed long long      ECOMPASS_S64;
typedef unsigned long long    ECOMPASS_U64;
typedef unsigned char         ECOMPASS_BIT;
typedef unsigned int          ECOMPASS_BOOL;
typedef float                 ECOMPASS_F32;
typedef double                ECOMPASS_F64;

#define         C_ECOMPASS_Null_U8X                                      (ECOMPASS_U8)0
#define         C_ECOMPASS_Zero_U8X                                      (ECOMPASS_U8)0
#define         C_ECOMPASS_One_U8X                                       (ECOMPASS_U8)1
#define         C_ECOMPASS_Two_U8X                                       (ECOMPASS_U8)2
#define         C_ECOMPASS_Three_U8X                                     (ECOMPASS_U8)3
#define         C_ECOMPASS_Four_U8X                                      (ECOMPASS_U8)4
#define         C_ECOMPASS_Five_U8X                                      (ECOMPASS_U8)5
#define         C_ECOMPASS_Six_U8X                                       (ECOMPASS_U8)6
#define         C_ECOMPASS_Seven_U8X                                     (ECOMPASS_U8)7
#define         C_ECOMPASS_Eight_U8X                                     (ECOMPASS_U8)8
#define         C_ECOMPASS_Nine_U8X                                      (ECOMPASS_U8)9
#define         C_ECOMPASS_Ten_U8X                                       (ECOMPASS_U8)10
#define         C_ECOMPASS_Eleven_U8X                                    (ECOMPASS_U8)11
#define         C_ECOMPASS_Twelve_U8X                                    (ECOMPASS_U8)12
#define         C_ECOMPASS_Sixteen_U8X                                   (ECOMPASS_U8)16
#define         C_ECOMPASS_TwentyFour_U8X                                (ECOMPASS_U8)24
#define         C_ECOMPASS_ThirtyTwo_U8X                                 (ECOMPASS_U8)32
#define         C_ECOMPASS_Hundred_U8X                                   (ECOMPASS_U8)100
#define         C_ECOMPASS_OneTwentySeven_U8X                            (ECOMPASS_U8)127
#define         C_ECOMPASS_TwoFiftyFive_U8X                              (ECOMPASS_U8)255
#define         C_ECOMPASS_TwoFiftySix_U16X                              (ECOMPASS_U16)256

/* EasyCASE ) */
/* EasyCASE ( 1005
   Constants */
/* Accelerometer Magnetometer datatype */
#define C_ACCELEROMETER_U8X   (ECOMPASS_U8)1
#define C_MAGNETOMETER_U8X    (ECOMPASS_U8)2

/*Initial Value*/
#define C_INTIALVALUE_U8R     (ECOMPASS_U8)0

#define C_MULTIPLY_U8R        (ECOMPASS_U8)0
#define C_DIVIDE_U8R          (ECOMPASS_U8)1

/* Status */
#define C_ERROR_U8R           (ECOMPASS_U8)1
#define C_OK_U8R              (ECOMPASS_U8)0
#define C_FALSE_U8X           (ECOMPASS_U8)0
#define C_TRUE_U8X            (ECOMPASS_U8)1

/* SET- RESET Constants */
#define C_ECOMPASS_SET_U8X    (ECOMPASS_U8)1
#define C_ECOMPASS_RESET_U8X  (ECOMPASS_U8)0

/*Rounding Constant*/
#define C_ROUNDINGCONSTANT_F32R           (ECOMPASS_F32)0.5

/*Scaling Range Setting for g*/
#define C_SCALING2G_F32R           (ECOMPASS_F32)3.9063
#define C_SCALING4G_F32R           (ECOMPASS_F32)7.8125
#define C_SCALING8G_F32R           (ECOMPASS_F32)15.625
#define C_SCALING16G_F32R          (ECOMPASS_F32)31.25

/*Scaling Range Setting for g*/
#define C_RANGE2G_U16R           (ECOMPASS_U16)0
#define C_RANGE4G_U16R           (ECOMPASS_U16)1
#define C_RANGE8G_U16R           (ECOMPASS_U16)2
#define C_RANGE16G_U16R          (ECOMPASS_U16)3

/* Orientation Data Array*/
#define C_QW_U16R              (ECOMPASS_U16)0
#define C_QX_U16R              (ECOMPASS_U16)1
#define C_QY_U16R              (ECOMPASS_U16)2
#define C_QZ_U16R              (ECOMPASS_U16)3

/*Remap Sensor Data constants*/
#define C_AXISXTOY_U8R              (ECOMPASS_U8)0
#define C_AXISXTOZ_U8R              (ECOMPASS_U8)1

#define C_AXISYTOX_U8R              (ECOMPASS_U8)2
#define C_AXISYTOZ_U8R              (ECOMPASS_U8)3

#define C_AXISZTOX_U8R              (ECOMPASS_U8)4
#define C_AXISZTOY_U8R              (ECOMPASS_U8)5

#define C_SIGNX_U8R				   (ECOMPASS_U8)0
#define C_SIGNY_U8R					(ECOMPASS_U8)1
#define C_SIGNZ_U8R					(ECOMPASS_U8)2

#define C_SET_U8R				    (ECOMPASS_U8)1
#define	C_RESET_U8R					(ECOMPASS_U8)0

#define C_POSITIVE_U8R				(ECOMPASS_U8)0
#define C_NEGATIVE_U8R				(ECOMPASS_U8)1

/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 76
   ENUM and struct Definitions */
/*user defined Structures*/
/* EasyCASE - */
/* EasyCASE < */
/* #Variable Definition */
/* Global Variable definition */
typedef struct{
ECOMPASS_S16 x;
ECOMPASS_S16 y;
ECOMPASS_S16 z;
}dataxyz_t;

typedef struct{
ECOMPASS_U8 V_X_U8R;
ECOMPASS_U8 V_Y_U8R;
ECOMPASS_U8 V_Z_U8R;
}ts_XYZdataU8;

typedef struct{
ECOMPASS_F32 x;
ECOMPASS_F32 y;
ECOMPASS_F32 z;
}dataxyzF32_t ;

/* Structure definition for Sensitivity & Offset Estimated Parameters */
typedef struct{
dataxyz_t S_Sensitivity;                /**< Estimated Sensitivity X,Y,Z  */
dataxyz_t S_Offset;                     /**< Estimated Offset X,Y,Z  */
ECOMPASS_S16 V_Radius_S16R;                      /**< Fitted Radius       */
}ts_EstimatedParam;
/* EasyCASE > */
/* EasyCASE ) */
/* EasyCASE ( 79
   Public API Declarations */
/* EasyCASE ) */
#endif
/* EasyCASE ) */
