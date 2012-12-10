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

#ifndef _MNL_LINUX_H_
#define _MNL_LINUX_H_

/******************************************************************************
* Configuration
******************************************************************************/
#define MNL_PORTING_LAYER //Steve test
#define SUPPORT_HOTSTILL
//#define SUPPORT_DSP_RW
//#define SUPPORT_AGPS
//#define SUPPORT_MP_TEST
//#define MNL_DEBUG
/******************************************************************************
* Dependency
******************************************************************************/
#include <stdio.h>   /* Standard input/output definitions */
#include <string.h>  /* String function definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <fcntl.h>   /* File control definitions */
#include <errno.h>   /* Error number definitions */
#include <termios.h> /* POSIX terminal control definitions */
#include <time.h>
#include <pthread.h>
#include <stdlib.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <sys/wait.h>
#include <sys/ipc.h>
#include <sys/time.h>
#include <sys/timeb.h>
#include <linux/mtk_agps_common.h>
#include "mtk_gps.h"
#include "mtk_gps_agps.h"
#include "mtk_gps_agent.h"
#include "mnl_common.h"
#include "MTK_BEE.h"
#include "mtk_gps_bee.h"


/******************************************************************************
* Macro & Definition
******************************************************************************/
#define DSP_UART_IN_READ_SIZE       (256)
#define PMTK_CMD_BUFFER_SIZE        (256)
#define PMTK_UART_IN_BUFFER_SIZE    (512)
#define PMODE                       (0600)
#define MNL_MQ_NAME                 "/mnl_msg_queue"
#define MNL_AGPS_MQ_NAME            "/mnl_agps_msg_queue"
#define DSP_DEV                     "/dev/ttyMT1"
#define GPS_DEV                     "/dev/gps"
#define DBG_DEV                     "/dev/ttygserial"
#define BEE_PATH                    "/data/misc/"
#define BEE_DISABLE_FILE            "/data/misc/no_hotstill"
#define LOG_FILE                    "/data/misc/gpsdebug.log"
#define PARM_FILE                   "/data/misc/gpsparm.dat"
#define DSP_IMG // auto load /data/misc/dsp.bin
#define DSP_FILE                    "/data/misc/dsp.bin"

#define DBGOUT(buf, size)  \
    do {    \
        if (dbg_fd != C_INVALID_FD) \
            write(dbg_fd, buf, size); \
    } while(0)

/******************************************************************************
* Structure & Enumeration 
******************************************************************************/
typedef struct 
{
    int msg_type;
    mtk_gps_msg *msg_ptr;
}mnl_msg_struct;

/* Ring buffer for message */
typedef struct      // Ring buffer
{
	mtk_gps_msg** next_write;     // next position to write to
	mtk_gps_msg** next_read;      // next position to read from
	mtk_gps_msg** start_buffer;   // start of buffer
	mtk_gps_msg** end_buffer;     // end of buffer + 1
} msg_ring_buf;
/******************************************************************************
* share global variables
******************************************************************************/
#ifdef _MTK_GPS_C_
    #define C_EXT   
#else
    #define C_EXT   extern
#endif    
/*---------------------------------------------------------------------------*/
C_EXT int dsp_fd 
    #ifdef _MTK_GPS_C_
    = C_INVALID_FD
    #endif
    ;
/*---------------------------------------------------------------------------*/    
C_EXT int gps_fd 
    #ifdef _MTK_GPS_C_
    = C_INVALID_FD
    #endif 
    ;
/*---------------------------------------------------------------------------*/    
C_EXT int dbg_fd
    #ifdef _MTK_GPS_C_
    = C_INVALID_FD
    #endif 
    ;
/*---------------------------------------------------------------------------*/    
C_EXT int nmea_debug_level
    #ifdef _MTK_GPS_C_
    #ifdef MNL_DEBUG
    = MNL_NMEA_DEBUG_FULL
    #else
    = MNL_NMEA_DEBUG_NORMAL
    #endif
    #endif
    ;
/******************************************************************************
* Function Prototye
******************************************************************************/
C_EXT int mtk_sys_init();
C_EXT int mtk_sys_uninit();
C_EXT mtk_int32 mtk_sys_nmea_output (char* buffer, mtk_uint32 length);
C_EXT mtk_int32 mtk_sys_nmea_input (const char* buffer, mtk_uint32 length);
C_EXT unsigned char calc_nmea_checksum (const char* sentence);
C_EXT unsigned char calc_dsp_checksum (const char* sentence);
C_EXT double translate_nmea_deg_min_sec (double deg);
C_EXT void mtk_gps_debug_wrapper(int group);
C_EXT void mtk_sys_ttff_handler(int type);
/*---------------------------------------------------------------------------*/    
#undef C_EXT
/*---------------------------------------------------------------------------*/    
#endif 
