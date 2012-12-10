/*
 * Copyright (C) 2012 ALPS ELECTRIC CO., LTD.
 * This file must be operated as based on the NDA (Non-Disclosure Agreement)
 * with ALPS ELECTRIC CO., LTD.
 *
 */

#ifndef _ACDAPI_DAEMON_H
#define _ACDAPI_DAEMON_H

#include <stdio.h>
#include <fcntl.h>
#include <dirent.h>

#include <utils/Log.h>

#include "acdapi.h"
#include "acdsinst.h"
#include "AlpsLib_Param.h"

#include <hardware/sensors.h>

/*****************************************************************************/
#define ACC_SENSITIVITY    1024
#define ACC_MOUNT          0
#define MAG_MOUNT          0
/*****************************************************************************/
typedef struct {
    TInt    cl3Mode;
    TInt    cl3MinLen;
    TInt    cl3MaxInCnt;
    TInt    cl3MinNum0;
    TInt    cl3spwMaxNum2;
    TInt    cl3spwCnt2;
    TInt    cl3spwMaxCnt2;
    TInt    cl3spwAreaCnt2;
    TInt    cl3spwRWidth2;
    TInt    cl3spwMaxNum3;
    TInt    cl3spwCnt3;
    TInt    cl3spwMaxCnt3;
    TInt    cl3spwAreaCnt3;
    TInt    cl3spwRWidth3;
    TInt    dr3MagAvLen;
    TInt    dr3AccAvLen;
    TInt    dr3AziAve;
} TACDPARAM_CL3, *PTACDPARAM_CL3;
typedef struct {
    TACDPARAM_CL3    ori_t10;
    TACDPARAM_CL3    ori_t20;
    TACDPARAM_CL3    ori_t50;
    TACDPARAM_CL3    ori_t100;
} TACDPARAM_CAL, *PTACDPARAM_CAL;
static TACDPARAM_CAL    acdapi_prm_cal;
/*****************************************************************************/
#ifdef ALPS_DEBUG
static int flgDebug = 1;
#else
static int flgDebug = 0;  
#endif
static int alps_ad_io_fd = -1;
static int gsensor_io_fd = -1;
static int magnT[3], ori[3];
//static int flg_mag, flg_azi, flg_att;
static int flg_mag, flg_putmag, flg_putacc;
static int actSns_SS = 0, delay_SS = 200;
static TACDRUNLIST   rl;
static TACDCALIBINFO acdapi_clb;
static TACDPARAM     acdapi_prm;
static int clb_lvl = 4;
static int ori_accuracy_state = SENSOR_STATUS_UNRELIABLE;
static int magMnt = 0, accMnt = 0;
/*****************************************************************************/
static int mag_lvl = KACDSNSLV3_STD, acc_lvl = 1024;
static int mag_Pwr = 450, mag_Inc = 48, mag_Dec = 0;
static int acc_Off_X = 0, acc_Off_Y = 0, acc_Off_Z = 0;
/*****************************************************************************/

static void init_acdapi();
static int  open_param(void);
static int  open_calib(void);
static int  save_calib(void);
static int  open_sensors_prm(void);
static void open_debug(void);
static void setSnsAccuracy(int Lvl, int *pstate);
static void exchange_mount(int mnt, int *px, int *py, int *pz);
static void MACM_CALLBACK cbCmpsNtcEvt (TACDEVT evt, long ip, void *vp, void *xp);
static void MACM_CALLBACK cbCmpsNtcRaw (TACDSNS snsCode, int x, int y, int z, void *xp);
static void MACM_CALLBACK cbCmpsNtcFilt(TACDSNS snsCode, int x, int y, int z, void *xp);


typedef struct {
	unsigned short	x;		/**< X axis */
	unsigned short	y;		/**< Y axis */
	unsigned short	z;		/**< Z axis */
} HSCDTD_U_VECTOR3D;


#endif
