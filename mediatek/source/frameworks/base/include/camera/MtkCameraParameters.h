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
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef ANDROID_HARDWARE_MTK_CAMERA_PARAMETERS_H
#define ANDROID_HARDWARE_MTK_CAMERA_PARAMETERS_H

#include <camera/CameraParameters.h>

namespace android {

class MtkCameraParameters : public CameraParameters
{
public:
    MtkCameraParameters() : CameraParameters() {}
    MtkCameraParameters(const String8 &params) { unflatten(params); }
    ~MtkCameraParameters()  {}

    MtkCameraParameters& operator=(CameraParameters const& params)
    {
        unflatten(params.flatten());
        return  (*this);
    }

    static const char SCENE_MODE_NORMAL[];

    static const char KEY_EXPOSURE[];
    static const char KEY_EXPOSURE_METER[];
    static const char KEY_ISO_SPEED[];
    static const char KEY_AE_MODE[];
    static const char KEY_FOCUS_METER[];
    static const char KEY_EDGE[];
    static const char KEY_HUE[];
    static const char KEY_SATURATION[];
    static const char KEY_BRIGHTNESS[];
    static const char KEY_CONTRAST[];
    static const char KEY_ZSD_MODE[];	
    static const char KEY_SUPPORTED_ZSD_MODE[];
    static const char KEY_AWB2PASS[];	
    static const char KEY_AF_LAMP_MODE [];
    //
    static const char KEY_FPS_MODE[];       // normal,fix
    //
    static const char KEY_FOCUS_DRAW[];     // 0,1
    //
    static const char KEY_CAPTURE_MODE[];   // normal,bestshot,evbracketshot,burstshot,smileshot,panoramashot
    static const char KEY_SUPPORTED_CAPTURE_MODES[];
    static const char KEY_CAPTURE_PATH[];
    static const char KEY_BURST_SHOT_NUM[];
    //
    static const char KEY_MATV_PREVIEW_DELAY[];
    //
    static const char KEY_PANORAMA_IDX[];
    static const char KEY_PANORAMA_DIR[];   // right,left,top,bottom
    //
    static const char KEY_SENSOR_DEV[];    // main,sub,atv
    static const char KEY_SUPPORTED_SENSOR_DEVS[];
	
    // Values for KEY_EXPOSURE
    static const char EXPOSURE_METER_SPOT[];
    static const char EXPOSURE_METER_CENTER[];
    static const char EXPOSURE_METER_AVERAGE[];

    // Valeus for KEY_ISO_SPEED
    static const char ISO_SPEED_AUTO[];
    static const char ISO_SPEED_100[];
    static const char ISO_SPEED_200[];
    static const char ISO_SPEED_400[];
    static const char ISO_SPEED_800[];
    static const char ISO_SPEED_1600[];

    // Values for KEY_FOCUS_METER
    static const char FOCUS_METER_SPOT[];
    static const char FOCUS_METER_MULTI[];

    static const char KEY_CAMERA_MODE[];    // normal,mtk-prv,mtk-vdo
    // Values for KEY_CAMERA_MODE
    static const int CAMERA_MODE_NORMAL;
    static const int CAMERA_MODE_MTK_PRV;
    static const int CAMERA_MODE_MTK_VDO;
    static const int CAMERA_MODE_MTK_VT;

    // Values for KEY_FPS_MODE
    static const int FPS_MODE_NORMAL;
    static const int FPS_MODE_FIX;

    // Values for KEY_CAPTURE_MODE
    static const char CAPTURE_MODE_NORMAL[];
    static const char CAPTURE_MODE_BEST_SHOT[];
    static const char CAPTURE_MODE_EV_BRACKET_SHOT[];
    static const char CAPTURE_MODE_BURST_SHOT[];
    static const char CAPTURE_MODE_SMILE_SHOT[];
    static const char CAPTURE_MODE_PANORAMA_SHOT[];
    static const char CAPTURE_MODE_AUTO_PANORAMA_SHOT[]; 
    static const char CAPTURE_MODE_MAV_SHOT[]; 
    static const char CAPTURE_MODE_HDR_SHOT[]; 
    static const char CAPTURE_MODE_ASD_SHOT[]; 
    static const char CAPTURE_MODE_PANO_3D[]; 
    static const char CAPTURE_MODE_SINGLE_3D[]; 
    static const char CAPTURE_MODE_FACE_BEAUTY[]; 	
	static const char CAPTURE_MODE_CONTINUOUS_SHOT[];

    // Values for KEY_PANORAMA_DIR
    static const char PANORAMA_DIR_RIGHT[];
    static const char PANORAMA_DIR_LEFT[];
    static const char PANORAMA_DIR_TOP[];
    static const char PANORAMA_DIR_DOWN[];
    //
    static const int ENABLE;
    static const int DISABLE;

    // Values for KEY_EDGE, KEY_HUE, KEY_SATURATION, KEY_BRIGHTNESS, KEY_CONTRAST
    static const char HIGH[];
    static const char MIDDLE[];
    static const char LOW[];

    // Preview Internal Format.
    static const char KEY_PREVIEW_INT_FORMAT[];

    // Pixel color formats for KEY_PREVIEW_FORMAT, KEY_PICTURE_FORMAT,
    // and KEY_VIDEO_FRAME_FORMAT
    static const char PIXEL_FORMAT_YUV420I[]; // I420
    // ISP Operation mode for meta mode use 
    static const char KEY_ISP_MODE[];
    // AF 
    static const char KEY_AF_X[]; 
    static const char KEY_AF_Y[]; 
    // Values for effect 
    static const char EFFECT_SEPIA_BLUE[];
    static const char EFFECT_SEPIA_GREEN[];    
    // Values for AWB 
    static const char WHITE_BALANCE_TUNGSTEN[];
    // Eng
    static const char ISO_SPEED_ENG[];
    static const char KEY_FOCUS_ENG_MODE[]; // 0,1,2,3 (0: normal)
    static const char KEY_FOCUS_ENG_STEP[];	
    static const char KEY_RAW_SAVE_MODE[];  // on, off
    static const char KEY_RAW_PATH[];	

	// KEY for Continuous shot speed
    static const char KEY_FAST_CONTINUOUS_SHOT[];
	
	//    
public:     ////    on/off => FIXME: should be replaced with TRUE[]
    static const char ON[];
    static const char OFF[];
};

}; // namespace android

#endif
