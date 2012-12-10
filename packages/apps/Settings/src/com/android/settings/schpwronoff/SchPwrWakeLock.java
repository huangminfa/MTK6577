package com.android.settings.schpwronoff;

import android.content.Context;
import android.os.PowerManager;

import com.mediatek.xlog.Xlog;
/**
 * Hold a wakelock that can be acquired in the AlarmReceiver and
 * released in the AlarmAlert activity
 */
class SchPwrWakeLock {
	private static final String TAG="SchPwrWakeLock";
    private static PowerManager.WakeLock sCpuWakeLock;

    static void acquireCpuWakeLock(Context context) {
        Xlog.d(TAG,"SchPwrWakeLock Acquiring cpu wake lock");
        if (sCpuWakeLock != null) {
            return;
        }

        PowerManager pm =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        sCpuWakeLock = pm.newWakeLock(
                //PowerManager.PARTIAL_WAKE_LOCK |
                PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "SchPwrOnOff");
        sCpuWakeLock.acquire();
    }

    static void releaseCpuLock() {
        Xlog.d(TAG,"SchPwrWakeLock Releasing cpu wake lock");
        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}
