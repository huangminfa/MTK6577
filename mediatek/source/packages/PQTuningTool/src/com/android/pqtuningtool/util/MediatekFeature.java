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
 * Copyright (C) 2010 The Android Open Source Project
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
package  com.android.pqtuningtool.util;

import com.mediatek.featureoption.FeatureOption;

//this class is used to identify what features are added to Gallery3D
//by MediaTek Corporation
public class MediatekFeature {

    //GIF animation is a feature developed by MediaTek. It avails Skia
    //to play GIF animation
    private static final boolean supportGifAnimation = true;

    //DRM (Digital Rights management) is developed by MediaTek.
    //Gallery3d avails MtkPlugin via android DRM framework to manage
    //digital rights of videos and images
    private static final boolean supportDrm = true;//to be replaced by FeatureOptions("DRM")

    //MPO (Multi-Picture Object) is series of 3D features developed by
    //MediaTek. Camera can shot MAV file or stereo image. Gallery is
    //responsible to list all mpo files add call corresponding module
    //to playback them.
    private static final boolean supportMpo = true;

    //MyFavorite is a short cut type application developed by MediaTek
    //It provides entries like Camera Photo, My Music, My Videos
    private static final boolean customizedForMyFavorite = true;

    //VLW stands for Video Live Wallpaper developed by MediaTek. It is 
    //like Live Wallpaper which can display dynamic wallpapers, by unlike
    //Live wallpaper, VLW's source is common video/videos, not OpenGL 
    //program
    private static final boolean customizedForVLW = true;

    //Bluetooth Print feature avails Bluetooth capacity on MediaTek
    //platform to print specified kind of image to a printer.
    private static final boolean supportBluetoothPrint = 
                                        FeatureOption.MTK_BT_PROFILE_BPP;

    //Picture quality enhancement feature avails Camera ISP hardware
    //to improve image quality displayed on the screen.
    private static final boolean supportPictureQualityEnhance = true;

    public static boolean isGifAnimationSupported() {
        return supportGifAnimation;
    }

    public static boolean isDrmSupported() {
        return supportDrm;
    }

    public static boolean isMpoSupported() {
        return supportMpo;
    }

    public static boolean hasCustomizedForMyFavorite() {
        return customizedForMyFavorite;
    }

    public static boolean hasCustomizedForVLW() {
        return customizedForVLW;
    }

    public static boolean isBluetoothPrintSupported() {
        return supportBluetoothPrint;
    }

    public static boolean isPictureQualityEnhanceSupported() {
        return supportPictureQualityEnhance;
    }

    //the following are variables or settings for Mediatek feature

    //gif background color
    private static final int gifBackGroundColor = 0xFFFFFFFF;

    public static int getGifBackGroundColor() {
        return gifBackGroundColor;
    }
}
