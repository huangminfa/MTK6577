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

package  com.android.pqtuningtool.util;

import java.util.Locale;

import android.content.Context;
import android.drm.DrmManagerClient;
import android.drm.DrmStore;
import android.drm.DrmUtils;
import android.drm.DrmManagerClient.DrmOperationListener;
import android.drm.DrmUtils.DrmProfile;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.Debug.MemoryInfo;

public class MtkUtils {
    private static final String TAG = "Gallery3D/MtkUtils";
    private static final boolean LOG = true;
    
    //operator info
    private static final String OPERATOR_OPTR = "ro.operator.optr";
    private static final String OPERATOR_SPEC = "ro.operator.spec";
    private static final String OPERATOR_SEG = "ro.operator.seg";
    private static final String OPERATOR_OPTR_CMCC = "OP01";
    private static final String OPERATOR_OPTR_CU = "OP02";
    private static final String OPERATOR_OPTR_ORANGE = "OP03";
    
    private static boolean sIsGetOperatorInfo = false;
    private static String sOperatorOptr;
    private static String sOperatorSpec;
    private static String sOperatorSeg;
    
    private static String getOperatorOptr() {
        if (!sIsGetOperatorInfo) {
            getOperatorInfo();
            sIsGetOperatorInfo = true;
        }
        if (LOG) MtkLog.v(TAG, "getOperatorOptr() sOperatorOptr=" + sOperatorOptr);
        return sOperatorOptr;
    }
    
    private static void getOperatorInfo() {
        sOperatorOptr = SystemProperties.get(OPERATOR_OPTR);
        sOperatorSpec = SystemProperties.get(OPERATOR_SPEC);
        sOperatorSeg = SystemProperties.get(OPERATOR_SEG);
        if (LOG) MtkLog.v(TAG, "getOperatorInfo() sOperatorOptr=" + sOperatorOptr);
        if (LOG) MtkLog.v(TAG, "getOperatorInfo() sOperatorSpec=" + sOperatorSpec);
        if (LOG) MtkLog.v(TAG, "getOperatorInfo() sOperatorSeg=" + sOperatorSeg);
    }
    
    public static boolean isOrangeOperator() {
        boolean result = false;
        String optr = getOperatorOptr();
        if (OPERATOR_OPTR_ORANGE.equals(optr)) {
            result = true;
        }
        if (LOG) MtkLog.v(TAG, "isOrangeOperator() return " + result);
        return result;
    }
    
    public static boolean isCmccOperator() {
        boolean result = false;
        String optr = getOperatorOptr();
        if (OPERATOR_OPTR_CMCC.equals(optr)) {
            result = true;
        }
        if (LOG) MtkLog.v(TAG, "isCmccOperator() return " + result);
        return result;
    }
    
    public static boolean isCuOperator() {
        boolean result = false;
        String optr = getOperatorOptr();
        if (OPERATOR_OPTR_CU.equals(optr)) {
            result = true;
        }
        if (LOG) MtkLog.v(TAG, "isCuOperator() return " + result);
        return result;
    }
    
    public static boolean isRtspStreaming(Uri uri, String mimeType) {
        boolean rtsp = false;
        if (uri != null) {
            if ("rtsp".equalsIgnoreCase(uri.getScheme())) {
                rtsp = true;
            }
        } 
        if (LOG) MtkLog.v(TAG, "isRtspStreaming(" + uri + ", " + mimeType + ") return " + rtsp);
        return rtsp;
    }
    
    public static boolean isHttpStreaming(Uri uri, String mimeType) {
        boolean http = false;
        if (uri != null) {
            if ("http".equalsIgnoreCase(uri.getScheme())) {
                http = true;
            }
        }
        if (LOG) MtkLog.v(TAG, "isHttpStreaming(" + uri + ", " + mimeType + ") return " + http);
        return http;
    }
    
    public static boolean isSdpStreaming(Uri uri, String mimeType) {
        boolean sdp = false;
        if (uri != null) {
            if ("application/sdp".equals(mimeType)) {
                sdp = true;
            } else if (uri.toString().toLowerCase(Locale.ENGLISH).endsWith(".sdp")) {
                sdp = true;
            }
        }
        if (LOG) MtkLog.v(TAG, "isSdpStreaming(" + uri + ", " + mimeType + ") return " + sdp);
        return sdp;
    }
    
    public static boolean isLocalFile(Uri uri, String mimeType) {
        boolean local = (!isSdpStreaming(uri, mimeType)
                && !isRtspStreaming(uri, mimeType)
                && !isHttpStreaming(uri, mimeType));
        if (LOG) MtkLog.v(TAG, "isLocalFile(" + uri + ", " + mimeType + ") return " + local);
        return local;
    }
    
    //for drm
    private static DrmManagerClient mDrmClient;
    public static boolean handleDrmFile(Context context, Uri uri, DrmOperationListener listener) {
        if (LOG) MtkLog.v(TAG, "handleDrmFile(" + uri + ", " + listener + ")");
        if (!com.mediatek.featureoption.FeatureOption.MTK_DRM_APP) {
            MtkLog.w(TAG, "not support DRM!");
            return false;
        }
        if (mDrmClient == null) {
            mDrmClient = new DrmManagerClient(context);
        }
        boolean result = false;
        DrmProfile info = DrmUtils.getDrmProfile(context, uri, mDrmClient);
        if (info != null && info.isDrm && info.method != DrmStore.DrmMethod.METHOD_FL) {
            int rightStatus = mDrmClient.checkRightsStatusForTap(uri, DrmStore.Action.PLAY);
            switch (rightStatus) {
            case DrmStore.RightsStatus.RIGHTS_VALID:
                DrmManagerClient.showConsume(context, listener);
                result = true;
                break;
            case DrmStore.RightsStatus.RIGHTS_INVALID:
                mDrmClient.showLicenseAcquisition(context, uri, listener);
                result = true;
                break;
            case DrmStore.RightsStatus.SECURE_TIMER_INVALID:
                DrmManagerClient.showSecureTimerInvalid(context, listener);
                result = true;
                break;
            default:
                break;
            }
        }
        if (LOG) MtkLog.v(TAG, "handleDrmFile() return " + result);
        return result;
    }
    
    public static int consume(Context context, Uri uri, int action) {
        if (mDrmClient == null) {
            mDrmClient = new DrmManagerClient(context);
        }
        int result = mDrmClient.consume(uri, action);
        if (LOG) MtkLog.v(TAG, "consume(" + uri + ", " + action + ") return " + result);
        return result;
    }
    
    public static boolean canShare(Context context, Uri uri) {
        if (!com.mediatek.featureoption.FeatureOption.MTK_DRM_APP) {
            MtkLog.w(TAG, "canShare() not support DRM!");
            return true;
        }
        if (mDrmClient == null) {
            mDrmClient = new DrmManagerClient(context);
        }
        int rightsStatus = mDrmClient.checkRightsStatus(uri, DrmStore.Action.TRANSFER);
        if (DrmStore.RightsStatus.RIGHTS_VALID != rightsStatus) {
            return true;
        }
        if (LOG) MtkLog.v(TAG, "canShare(" + uri + ") rightsStatus=" + rightsStatus);
        return false;
    }
    
    public static void logMemory(String title) {
        MemoryInfo mi = new MemoryInfo();
        android.os.Debug.getMemoryInfo(mi);
        String tagtitle = "logMemory() " + title;
        MtkLog.v(TAG, tagtitle + "         PrivateDirty    Pss     SharedDirty");
        MtkLog.v(TAG, tagtitle + " dalvik: " + mi.dalvikPrivateDirty + ", " + mi.dalvikPss + ", " + mi.dalvikSharedDirty + ".");
        MtkLog.v(TAG, tagtitle + " native: " + mi.nativePrivateDirty + ", " + mi.nativePss + ", " + mi.nativeSharedDirty + ".");
        MtkLog.v(TAG, tagtitle + " other: " + mi.otherPrivateDirty + ", " + mi.otherPss + ", " + mi.otherSharedDirty + ".");
        MtkLog.v(TAG, tagtitle + " total: " + mi.getTotalPrivateDirty() + ", " + mi.getTotalPss() + ", " + mi.getTotalSharedDirty() + ".");
    }
}
