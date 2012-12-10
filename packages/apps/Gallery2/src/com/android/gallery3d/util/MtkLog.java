package com.android.gallery3d.util;

import java.io.File;

import android.os.Environment;

import com.mediatek.xlog.Xlog;

/**
 * Adapter for log system.
 */
public final class MtkLog {
    
    // on/off switch to control large amount of logs
    public static final boolean DBG;
    
    public static final boolean DBG_TILE;
    static {
        File cfg = new File(Environment.getExternalStorageDirectory(), "DEBUG_GALLERY2");
        if (cfg.exists()) {
            DBG = true;
        } else {
            DBG = false;
        }
        cfg = new File(Environment.getExternalStorageDirectory(), "DEBUG_TILE");
        if (cfg.exists()) {
            DBG_TILE = true;
        } else {
            DBG_TILE = false;
        }
        Xlog.v("MtkLog", "Gallery2 debug mode " + (DBG ? "ON" : "OFF"));
    }
    
    private MtkLog() {
    }

    public static int v(String tag, String msg) {
        return Xlog.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return Xlog.v(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        return Xlog.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return Xlog.d(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return Xlog.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return Xlog.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return Xlog.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return Xlog.w(tag, msg, tr);
    }

    public static int e(String tag, String msg) {
        return Xlog.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Xlog.e(tag, msg, tr);
    }
    
    public static boolean isDebugTile() {
        return DBG_TILE;
    }
    
}