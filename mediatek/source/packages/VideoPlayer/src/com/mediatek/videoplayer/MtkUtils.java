package com.mediatek.videoplayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.drm.DrmManagerClient;
import android.drm.DrmStore;
import android.drm.DrmUtils;
import android.drm.DrmManagerClient.DrmOperationListener;
import android.drm.DrmUtils.DrmProfile;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.Debug.MemoryInfo;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.Window;

public class MtkUtils {
    private static final String TAG = "MtkUtils";
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
    
    public static boolean isRtspStreaming(Uri uri) {
        boolean rtsp = false;
        if (uri != null && "rtsp".equalsIgnoreCase(uri.getScheme())) {
            rtsp = true;
        }
        if (LOG) MtkLog.v(TAG, "isRtspStreaming(" + uri + ") return " + rtsp);
        return rtsp;
    }
    
    public static boolean isHttpStreaming(Uri uri) {
        boolean http = false;
        if (uri != null && "http".equalsIgnoreCase(uri.getScheme())) {
            http = true;
        }
        if (LOG) MtkLog.v(TAG, "isHttpStreaming(" + uri + ") return " + http);
        return http;
    }
    
    public static boolean isLocalFile(Uri uri) {
        boolean local = (!isRtspStreaming(uri) && !isHttpStreaming(uri));
        if (LOG) MtkLog.v(TAG, "isLocalFile(" + uri + ") return " + local);
        return local;
    }
    
    //for drm
    private static DrmManagerClient mDrmClient;
    public static DrmManagerClient getDrmManager(Context context) {
        if (mDrmClient == null) {
            mDrmClient = new DrmManagerClient(context);
        }
        return mDrmClient;
    }
    
    public static boolean isSupportDrm() {
        boolean support = com.mediatek.featureoption.FeatureOption.MTK_DRM_APP;
        MtkLog.w(TAG, "isSupportDrm() return " + support);
        return support;
    }
    
    public static Bitmap overlayDrmIcon(Context context, String path, int action, Bitmap bkg) {
        Bitmap bitmap = getDrmManager(context).overlayDrmIcon(context.getResources(), path, action, bkg);
        if (LOG) MtkLog.v(TAG, "overlayDrmIcon(" + path + ") return " + path);
        //saveBitmap("overlayDrmIconSkew() ", path, bitmap);
        return bitmap;
    }
    
    public static void showDrmDetails(final Context context, String path) {
        getDrmManager(context).showProtectionInfoDialog(context, path);
    }
    
    public static boolean isMediaScanning(Context context) {
        boolean result = false;
        Cursor cursor = query(context, MediaStore.getMediaScannerUri(), 
                new String [] { MediaStore.MEDIA_SCANNER_VOLUME }, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String scanVolumne = cursor.getString(0);
                result = "external".equals(scanVolumne);
                if (LOG) MtkLog.v(TAG, "isMediaScanning() scanVolumne=" + scanVolumne);
            }
            cursor.close(); 
        } 
        if (LOG) MtkLog.v(TAG, "isMediaScanning() cursor=" + cursor + ", result=" + result);
        return result;
    }
    
    public static Cursor query(Context context, Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        return query(context, uri, projection, selection, selectionArgs, sortOrder, 0);
    }
    
    public static Cursor query(Context context, Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder, int limit) {
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                return null;
            }
            if (limit > 0) {
                uri = uri.buildUpon().appendQueryParameter("limit", "" + limit).build();
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
         } catch (UnsupportedOperationException ex) {
            return null;
        }
        
    }
    
    public static void enableSpinnerState(Activity a) {
        if (LOG) MtkLog.v(TAG, "enableSpinnerState(" + a + ")");
        a.getWindow().setFeatureInt(
                Window.FEATURE_PROGRESS,
                Window.PROGRESS_START);
        a.getWindow().setFeatureInt(
                Window.FEATURE_PROGRESS,
                Window.PROGRESS_VISIBILITY_ON);
    }
    
    public static void disableSpinnerState(Activity a) {
        if (LOG) MtkLog.v(TAG, "disableSpinnerState(" + a + ")");
        a.getWindow().setFeatureInt(
                Window.FEATURE_PROGRESS,
                Window.PROGRESS_END);
        a.getWindow().setFeatureInt(
                Window.FEATURE_PROGRESS,
                Window.PROGRESS_VISIBILITY_OFF);
    }
    
    public static boolean isMediaMounted(Context context) {
        boolean mounted = false;
        String defaultStoragePath = null;
        String defaultStorageState = null;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mounted = true;
        } else {
            StorageManager storageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
            if (storageManager != null) {
                defaultStoragePath = StorageManager.getDefaultPath();
                defaultStorageState = storageManager.getVolumeState(defaultStoragePath);
                if (Environment.MEDIA_MOUNTED.equals(defaultStorageState) ||
                        Environment.MEDIA_MOUNTED_READ_ONLY.equals(defaultStorageState)) {
                    mounted = true;
                }
            }
        }
        if (LOG) MtkLog.v(TAG, "isMediaMounted() return " + mounted + ", state=" + state
                + ", defaultStoragePath=" + defaultStoragePath + ", defaultStorageState=" + defaultStorageState);
        return mounted;
    }
    
    public static String stringForTime(long millis) {
        int totalSeconds = (int) millis / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
    
    private static Date mDate = new Date();//cause lots of CPU
    public static String localTime(long millis) {
        mDate.setTime(millis);
        return mDate.toLocaleString();
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
    
    public static void saveBitmap(String tag, String msg, Bitmap bitmap) {
        if (bitmap == null) {
            MtkLog.v(tag, "[" + msg + "] bitmap=null");
        }
        long now = System.currentTimeMillis();
        String fileName = "/mnt/sdcard/nomedia/" + now + ".jpg";
        File temp = new File(fileName);
        File dir = temp.getParentFile();
        if (!dir.exists()) {//create debug folder
            dir.mkdir();
        }
        File nomedia = new File("/mnt/sdcard/nomedia/.nomedia");
        if (!nomedia.exists()) {//add .nomedia file
            try {
                nomedia.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream os = new FileOutputStream(temp);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MtkLog.v(tag, "[" + msg + "] write file filename=" + fileName);
    }
    
    public static boolean isSupport3d() {
        boolean support = com.mediatek.featureoption.FeatureOption.MTK_S3D_SUPPORT;
        MtkLog.w(TAG, "isSupport3d() return " + support);
        return support;
    }
    
    private static final String STEREO_TYPE_2D = "" + MediaStore.Video.Media.STEREO_TYPE_2D;
    public static boolean isStereo3D(String stereoType) {
        boolean stereo3d = true;
        if (stereoType == null || STEREO_TYPE_2D.equals(stereoType)) {
            stereo3d = false;
        }
        if (LOG) MtkLog.v(TAG, "isStereo3D(" + stereoType + ") return " + stereo3d);
        return stereo3d;
    }
}
