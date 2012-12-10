package com.mediatek.media3d;

import android.app.Application;

public final class Media3D extends Application {
    public static final boolean DEBUG = true;

    private static boolean mDemoMode = false;

    private Media3D() {}

    public static void setDemoMode(boolean demoMode) {
        mDemoMode = demoMode;
    }

    public static boolean isDemoMode() {
        return mDemoMode;
    }
}
