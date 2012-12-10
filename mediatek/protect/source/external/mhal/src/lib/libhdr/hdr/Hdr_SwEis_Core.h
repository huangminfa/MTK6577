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

#define UNIFORM_SEARCH_RANGE_LARGE      (7)       //Larger full search range for uniform case. For right and left, it is for x dimension; otherwise, it is for y dimension.
#define UNIFORM_SEARCH_RANGE_SMALL      (3)       //Smaller full search range for uniform case. For up and down, it is for y dimension; otherwise, it is for x dimension.

#define UNIFORM_SEARCH_RANGE_NO_DIR     (4)       //Full search range for no direction situation

#define NORMAL_CASE_NUM                 (10)      //Totally 10 differcne normal cases
#define MAX_TYPE_THRESHOLD              (900000)  //max_block threshold to check if the uniform case or not, if edge value is bigger than this, type is 0~9 not uniform, else type is 10-27, uniform case

#define MV_THRESHOLD                    (2)       //Search range around 0 for uniform region
#define	SAD_THRESHOLD                   (13)      //Real threshold = SAD_THRESHOLD/10.  The best SAD should be higher than first or center SAD with this ratio to prove it reliablity in uniform frame

#define MAX_COUNT                       (200)     //Control the largest iteration of motion estimation

#define SUBSAMPLE_D                     (4)       //Sub-smaple ratio in both direction 

#define MARGIN_X                        (4)       //must not zero!! should be a multiple of 4. Define the non-use region of the boundary.
#define MARGIN_Y                        (4)       //must not zero!! should be a multiple of 4. Define the non-use region of the boundary.

#define UNIFORM_FAST_ENABLE             (0)       //0: no fast search for uniform case, 1: open fast search for uniform case 
#define NORMAL_FULL_SEARCH_RANGE_LARGE  (4)       //The search range of normal case
#define NORMAL_FULL_SEARCH_RANGE_SMALL  (2)       //The search range of normal case


typedef enum
{   
    AUTOCAP_DIR_RIGHT=0,
    AUTOCAP_DIR_LEFT,
    AUTOCAP_DIR_UP,
    AUTOCAP_DIR_DOWN,
    AUTOCAP_DIR_NO,
} AUTOCAP_DIRECTION_ENUM;

void SW_EIS(MUINT32 Y1, MUINT32 Y2, MINT32 saturation_value, MINT32 *mv);
static void autoCapMotionEstimation(MINT32 *mv, MUINT32 *step_count, float *min_sad, MINT16 saturation_value);

