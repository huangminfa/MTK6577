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
    Harris corner detector (harris_corner.h)
    Author: Thomas Tzeng (thomas.tzeng@mediatek.com)
    Copyright 2010 by MediaTek
*/

#ifndef MAV_MOTION_CORE_H
#define MAV_MOTION_CORE_H

#include "MTKMotionType.h"
#include "MTKMotion.h"

// Tuning parameters
#define TV_TH       10      // trust value threshold
#define MAX_MV      8       // the accumulated motion that should shot
#define BN_TH       3       // the threshold of block number that exceed MAX_MV
#define STEP_LB	    3       // the lower bound of frame jump step
#define STEP_UB	    8       // the upper bound of frame jump step

// working buffer size details
#define MOTION_MV_SIZE          (MOTION_TOTAL_BN*2)
#define MOTION_TV_SIZE          (MOTION_TOTAL_BN*2)
#define MOTION_TV_CNT_SIZE      (MOTION_TOTAL_BN*2)
#define MOTION_ACC_MV_SIZE      (MOTION_TOTAL_BN*2)
#define MOTION_GMV_SIZE         (2)
#define MOTION_TMP_MV_SIZE      (2)
#define MOTION_CGMV_SIZE        (2)

// computational data structure
typedef struct {
    // input
    MUINT32 ProcBufAddr;

    // tuning parameters
    MFLOAT max_mv;          // maximum mv
    MINT32 step_lb;         // step lower bound
    MINT32 step_ub;         // step upper bound

    // intermediate         // offset   size(Byte)
    MFLOAT *mv;             //      0   32*4 = 128
    MFLOAT *tv;             //    128   32*4 = 128
    MINT32 *tv_cnt;         //    256   32*4 = 128
    MFLOAT *acc_mv;         //    384   32*4 = 128
    MFLOAT *tmp_mv;         //    512    2*4 =   8
    MINT32 shot_step;       //    520    1*4 =   4
    MINT32 cur_step;        //    524    1*4 =   4
    MBOOL moving;           //    528            1
                            //    529 (total size)
}motion_cal_struct;

// functions declaration
void MotionDetectInit(MTKMotionEnvInfo *);
void MotionDetect(MTKMotionProcInfo *, MTKMotionResultInfo *);

#endif
