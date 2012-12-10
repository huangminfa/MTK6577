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

package com.mediatek.engineermode;

public class ChipSupport {
    // constants below.
    public final static int MTK_UNKNOWN_SUPPORT = 0;
    public final static int MTK_6573_SUPPORT = 1;
    public final static int MTK_6516_SUPPORT = 2;
    public final static int MTK_6575_SUPPORT = 4;
    public final static int MTK_6577_SUPPORT = 8;

    public static native int GetChip();

    public static String GetChipString() {
        if (GetChip() == MTK_6573_SUPPORT) {
            return "Chip 6573 ";
        } else if (GetChip() == MTK_6516_SUPPORT) {
            return "Chip 6516 ";
        } else if (GetChip() == MTK_6575_SUPPORT) {
            return "Chip 6575 ";
        } else if (GetChip() == MTK_6577_SUPPORT) {
            return "Chip 6577 ";
        } else {
            return "Chip unknown ";
        }
    }

    public final static int MTK_FM_SUPPORT = 0;
    public final static int MTK_FM_TX_SUPPORT = 1;
    public final static int MTK_RADIO_SUPPORT = 2;
    public final static int MTK_AGPS_APP = 3;
    public final static int MTK_GPS_SUPPORT = 4;
    public final static int HAVE_MATV_FEATURE = 5;
    public final static int MTK_BT_SUPPORT = 6;
    public final static int MTK_WLAN_SUPPORT = 7;
    public final static int MTK_TTY_SUPPORT = 8;

    // FEATURE SUPPORTED
    public static native boolean IsFeatureSupported(int feature_id);

    static {
        System.loadLibrary("em_chip_support_jni");

    }
}
