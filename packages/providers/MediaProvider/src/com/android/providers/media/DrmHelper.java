package com.android.providers.media;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Binder;

/**
 * Helper class for Drm implementation. 
 * Only Drm Service Supported process can call this class. 
 *
 */
public class DrmHelper {
    private static final String TAG = "MediaProvider/DrmHelper";
    private static final boolean LOG = true;
    private static HashMap<Integer, Boolean> mCurrentProcesses;
    private static Set<String> mPermitedProcessNames;
    
    /**
     * Is current process can access drm files.
     * @param pid
     * @return
     */
    public static synchronized boolean isPermitedAccessDrm(Context context ,int pid) {
        if (!com.mediatek.featureoption.FeatureOption.MTK_DRM_APP) {
            MtkLog.w(TAG, "not support DRM!");
            return false;
        }
        Boolean result = null;//not set
        if (mCurrentProcesses == null) {
            mCurrentProcesses = new HashMap<Integer, Boolean>();
        } else {
            result = mCurrentProcesses.get(pid);
        }
        if (result == null) {//no this process
            if (LOG) MtkLog.v(TAG, "permitAccessDrm(" + pid + ") can not get result!");
            if (mPermitedProcessNames == null) {
                //if not set permited process names, here set the default names.
                setDefaultProcessNames();
            }
            mCurrentProcesses.clear();//clear old map
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
            int size = list.size();
            for(int i = 0; i < size; i++) {
                RunningAppProcessInfo runInfo = list.get(i);
                boolean allow = mPermitedProcessNames.contains(runInfo.processName);
                mCurrentProcesses.put(runInfo.pid, allow);
                if (LOG) MtkLog.v(TAG, "pid=" + runInfo.pid + ", name=" + runInfo.processName + ", allow=" + allow);
            }
            result = mCurrentProcesses.get(pid);
            if (result == null) {
                MtkLog.e(TAG, "Can not get current pid's access drm info! pid=" + pid);
                return true;
            }
        }
        if (LOG) MtkLog.v(TAG, "synchronized permitAccessDrm(" + pid + ") return " + result);
        return result;
    }
    
    /**
     * Set the permited process names for DRM.
     * @param permitedProcessNames
     */
    public static synchronized void setPermitedProcessNames(String[] permitedProcessNames) {
        if (mPermitedProcessNames == null) {
            mPermitedProcessNames = new HashSet<String>();
        } else {
            mPermitedProcessNames.clear();
        }
        if (permitedProcessNames == null) {
            MtkLog.w(TAG, "setPermitedProcessNames() none permited access drm process!");
        } else {
            int length = permitedProcessNames.length;
            for(int i = 0; i < length; i++) {
                mPermitedProcessNames.add(permitedProcessNames[i]);
                if (LOG) MtkLog.v(TAG, "setPermitedProcessNames() add [" + i + "]=" + permitedProcessNames[i]);
            }
        }
    }
    
    private static void setDefaultProcessNames() {
        String[] permitedProcessNames = new String[] {
                "com.android.music",
                "com.android.gallery",
                "com.android.gallery:CropImage",
                "com.cooliris.media",
                "android.process.media",
                "com.mediatek.videoplayer",
                "com.mediatek.videoplayer2",
                "com.android.settings",
                "com.android.gallery3d",
                "com.android.gallery3d:crop",
                "com.android.deskclock"
              };
        setPermitedProcessNames(permitedProcessNames);
    }
}
