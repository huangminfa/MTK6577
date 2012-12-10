package com.android.gallery3d.util;

import java.util.Locale;
import java.io.File;

import android.content.Context;
import android.drm.DrmManagerClient;
import android.drm.DrmStore;
import android.drm.DrmUtils;
import android.drm.DrmManagerClient.DrmOperationListener;
import android.drm.DrmUtils.DrmProfile;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.Debug.MemoryInfo;
import android.os.storage.StorageManager;
import android.provider.MediaStore;

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

    // used for movie player to check for videos. Action type PLAY
    public static boolean handleDrmFile(Context context, Uri uri, DrmOperationListener listener) {
        if (LOG) MtkLog.v(TAG, "handleDrmFile(" + uri + ", " + listener + ")");
        if (!com.mediatek.featureoption.FeatureOption.MTK_DRM_APP) {
            MtkLog.w(TAG, "handleDrmFile() not support DRM!");
            return false;
        }
        if (mDrmClient == null) {
            mDrmClient = new DrmManagerClient(context);
        }
        boolean result = false;
        DrmProfile info = DrmUtils.getDrmProfile(context, uri, mDrmClient);
        if (info != null && info.isDrm && info.method != DrmStore.DrmMethod.METHOD_FL) {
            int rightsStatus = DrmStore.RightsStatus.RIGHTS_INVALID;
            try {
                rightsStatus = mDrmClient.checkRightsStatusForTap(uri, DrmStore.Action.PLAY);
            } catch (IllegalArgumentException e) {
                MtkLog.w(TAG, "handleDrmFile() : raise exception, we assume invalid rights");
            }
            switch (rightsStatus) {
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
        if (LOG) MtkLog.v(TAG, "consume(" + uri + ", action=" + action + ") return " + result);
        return result;
    }
    
    public static boolean canShare(Context context, Uri uri) {
        if (LOG) MtkLog.v(TAG, "canShare(" + uri + ")");
        if (!com.mediatek.featureoption.FeatureOption.MTK_DRM_APP) {
            MtkLog.w(TAG, "canShare() not support DRM!");
            return true;
        }
        boolean share = false;
        if (mDrmClient == null) {
            mDrmClient = new DrmManagerClient(context);
        }

        boolean isDrm = false;
        try {
            isDrm = mDrmClient.canHandle(uri, null);
        } catch (IllegalArgumentException e) {
            MtkLog.w(TAG, "canShare() : raise exception, we assume it's not a OMA DRM file");
        }

        if (isDrm) {
            int rightsStatus = DrmStore.RightsStatus.RIGHTS_INVALID;
            try {
                rightsStatus = mDrmClient.checkRightsStatus(uri, DrmStore.Action.TRANSFER);
            } catch (IllegalArgumentException e) {
                MtkLog.w(TAG, "canShare() : raise exception, we assume it has no rights to be shared");
            }
            share = (DrmStore.RightsStatus.RIGHTS_VALID == rightsStatus);
            if (LOG) MtkLog.v(TAG, "canShare(" + uri + "), rightsStatus=" + rightsStatus);
        } else {
            share = true;
        }
        if (LOG) MtkLog.v(TAG, "canShare(" + uri + "), share=" + share);
        return share;
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
    
    private static final String MMS_AUTHORITY = "mms";
    public static boolean isFromMms(Uri uri) {
        boolean mms = false;
        if (uri != null) {
            mms = MMS_AUTHORITY.equalsIgnoreCase(uri.getAuthority());
        }
        if (LOG) MtkLog.v(TAG, "isFromMms(" + uri + ") return " + mms);
        return mms;
    }
    
    private static final String EXTRA_CAN_SHARE = "CanShare";
    public static boolean canShare(Bundle extra) {
        boolean canshare = true;
        if (extra != null) {
            canshare = extra.getBoolean(EXTRA_CAN_SHARE, true);
        }
        if (LOG) MtkLog.v(TAG, "canShare(" + extra + ") return " + canshare);
        return canshare;
    }

    private static StorageManager mStorageManager = null;
    public static File getMTKExternalCacheDir(Context context) {
        if (context == null) {
            return null;
        }
        if (mStorageManager == null) {
            mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        }
        File ret = mStorageManager.getMTKExternalCacheDir(context.getPackageName());
        return ret;
    }

    public static String getMtkDefaultPath() {
        String path = StorageManager.getDefaultPath();
        if (LOG) MtkLog.v(TAG, "getMtkDefaultPath() return " + path);
        return path;
    }
    
    public static boolean isSupport3d() {
        boolean support = com.mediatek.featureoption.FeatureOption.MTK_S3D_SUPPORT;
        MtkLog.w(TAG, "isSupport3d() return " + support);
        return support;
    }
    
    private static final String STEREO_TYPE_2D = "" + MediaStore.Video.Media.STEREO_TYPE_2D;
    private static final String STEREO_TYPE_UNKNOWN =
                                  "" + MediaStore.Video.Media.STEREO_TYPE_UNKNOWN;
    public static boolean isStereo3D(String stereoType) {
        boolean stereo3d = true;
        if (stereoType == null || STEREO_TYPE_2D.equals(stereoType) ||
            STEREO_TYPE_UNKNOWN.equals(stereoType)) {
            stereo3d = false;
        }
        if (LOG) MtkLog.v(TAG, "isStereo3D(" + stereoType + ") return " + stereo3d);
        return stereo3d;
    }
}
