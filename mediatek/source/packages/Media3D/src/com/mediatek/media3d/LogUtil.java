package com.mediatek.media3d;

import android.util.Log;

public final class LogUtil {
    private LogUtil() {}

    public static void v(final String tag, final String msg) {
        if (Media3D.DEBUG) {
            // If msg is null, assume caller is LogUtil.v(tag), bypass it to dump two-upper-levels caller.
            final boolean nullMsg = (msg == null);
            final int callerStack = (nullMsg) ? 4 : 3;
            final StackTraceElement caller = Thread.currentThread().getStackTrace()[callerStack];
            if (caller == null) {
                Log.v(tag, msg);
            } else {
                if (nullMsg) {
                    Log.v(tag, "[Class] ==> " + caller.getClassName() + ", [Method] ==> " + caller.getMethodName() + "()");
                } else {
                    Log.v(tag, "[Class] ==> " + caller.getClassName() + ", [Method] ==> " + caller.getMethodName() + "(), [Msg] ==> " + msg);
                }
            }
        }
    }

    public static void v(final String tag) {
        LogUtil.v(tag, null);
    }
}