package com.mediatek.weather3dwidget;

import android.util.Log;

public final class LogUtil {
    private LogUtil() {}

    public static void v(final String tag, final String msg) {
        if (Weather3D.DEBUG) {
            // If msg is null, assume caller is LogUtil.v(tag), bypass it to dump two-upper-levels caller.
            final boolean nullMsg = (msg == null);
            final int callerStack = (nullMsg) ? 4 : 3;
            final StackTraceElement caller = Thread.currentThread().getStackTrace()[callerStack];
            if (caller == null) {
                Log.v(tag, msg);
            } else {
                if (nullMsg) {
                    Log.v(tag, caller.getMethodName() + "(" + caller.getFileName() + ":" + caller.getLineNumber() + ")");
                } else {
                    Log.v(tag, caller.getMethodName() + ", " + msg + " (" + caller.getFileName() + ":" + caller.getLineNumber() + ")");
                }
            }
        }
    }

    public static void v(final String tag) {
        LogUtil.v(tag, null);
    }

    public static void i(final String tag, final String msg) {
        if (Weather3D.DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void w(final String tag, final String msg) {
        if (Weather3D.DEBUG) {
            Log.w(tag, msg);
        }
    }
    
    public static void e(final String tag, final String msg) {
        if (Weather3D.DEBUG) {
            Log.e(tag, msg);
        }
    }
}