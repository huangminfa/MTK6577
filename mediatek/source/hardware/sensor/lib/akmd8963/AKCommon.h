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
 * $Id: AKCommon.h 322 2011-08-24 08:18:44Z yamada.rj $
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
#ifndef AKMD_INC_AKCOMMON_H
#define AKMD_INC_AKCOMMON_H

#include <stdio.h>     //frpintf
#include <stdlib.h>    //atoi
#include <string.h>    //memset
#include <unistd.h>
#include <stdarg.h>    //va_list
#include <utils/Log.h> //LOGV
#include <errno.h>     //errno

/*** Constant definition ******************************************************/
#undef LOG_TAG
#define LOG_TAG "AKMD2"

#define DBG_LEVEL0   0x0001	// Critical
#define DBG_LEVEL1   0x0002	// Notice
#define DBG_LEVEL2   0x0003	// Information
#define DBG_LEVEL3   0x0004	// Debug
#define DBGFLAG      DBG_LEVEL2
#define DBG_LEVEL DBG_LEVEL3


#define DATA_AREA01	0x0001
#define DATA_AREA02	0x0002
#define DATA_AREA03	0x0004
#define DATA_AREA04	0x0008
#define DATA_AREA05	0x0010
#define DATA_AREA06	0x0020
#define DATA_AREA07	0x0040
#define DATA_AREA08	0x0080
#define DATA_AREA09	0x0100
#define DATA_AREA10	0x0200
#define DATA_AREA11	0x0400
#define DATA_AREA12	0x0800
#define DATA_AREA13	0x1000
#define DATA_AREA14	0x2000
#define DATA_AREA15	0x4000
#define DATA_AREA16	0x8000


/* Debug area definition */
#define AKMDATA_BDATA		DATA_AREA01	/*<! AK8963's BDATA */
#define AKMDATA_AVEC		DATA_AREA02	/*<! Acceleration data */
#define AKMDATA_EXECTIME	DATA_AREA03	/*<! Time of each loop cycle */
#define AKMDATA_EXECFLAG	DATA_AREA04	/*<! Execution flags */
#define AKMDATA_MAGDRV		DATA_AREA05	/*<! AK8963 driver's data */
#define AKMDATA_ACCDRV		DATA_AREA06	/*<! Acceleration driver's data */
#define AKMDATA_GETINTERVAL	DATA_AREA07	/*<! Interval */
#define AKMDATA_D6D			DATA_AREA08 /*<! Direction6D */

#ifndef ENABLE_AKMDEBUG
#define ENABLE_AKMDEBUG		1	/* Eanble debug output when it is 1. */
#endif

#ifndef OUTPUT_STDOUT
#define OUTPUT_STDOUT		1	/* Output to stdout when it is 1. */
#endif

/***** Debug output ******************************************/
#if ENABLE_AKMDEBUG
#if OUTPUT_STDOUT
#define AKMDEBUG(level, format, ...) \
    (((level) <= DBG_LEVEL) \
	  ? (fprintf(stdout, (format), ##__VA_ARGS__)) \
	  : ((void)0))
#else
#define AKMDEBUG(level, format, ...) \
	LOGD_IF(((level) <= DBG_LEVEL), (format), ##__VA_ARGS__)
#endif
#else
#define AKMDEBUG(level, format, ...)
#endif

/***** Dbg Area Output ***************************************/
#if ENABLE_AKMDEBUG
#define AKMDATA(flag, format, ...)  \
	((((int)flag) & g_dbgzone) \
	  ? (fprintf(stdout, (format), ##__VA_ARGS__)) \
	  : ((void)0))
#else
#define AKMDATA(flag, format, ...)
#endif

/***** Data output *******************************************/
#if OUTPUT_STDOUT
#define AKMDUMP(format, ...) \
	fprintf(stderr, (format), ##__VA_ARGS__)
#else
#define AKMDUMP(format, ...) \
	LOGD((format), ##__VA_ARGS__)
#endif

/***** Log output ********************************************/  
#ifdef AKM_LOG_ENABLE  
#define AKM_LOG(format, ...)	LOGD((format), ##__VA_ARGS__)  
#else  
#define AKM_LOG(format, ...)  
#endif

extern int g_stopRequest;
extern int g_opmode;
extern int g_dbgzone;
extern int g_mainQuit;

/***** Error output *******************************************/
#define AKMERROR \
	((g_opmode == 0) \
	  ? (LOGE("%s:%d Error.", __FUNCTION__, __LINE__)) \
	  : (fprintf(stderr, "%s:%d Error.\n", __FUNCTION__, __LINE__)))

#define AKMERROR_STR(api) \
	((g_opmode == 0) \
	  ? (LOGE("%s:%d %s Error (%s).", \
	  		  __FUNCTION__, __LINE__, (api), strerror(errno))) \
	  : (fprintf(stderr, "%s:%d %s Error (%s).\n", \
	  		  __FUNCTION__, __LINE__, (api), strerror(errno))))
	  		  

#endif //INC_AKCOMMON_H

