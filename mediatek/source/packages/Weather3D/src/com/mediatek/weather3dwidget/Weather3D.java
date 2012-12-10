package com.mediatek.weather3dwidget;

public final class Weather3D {
    public static final boolean DEBUG = true;
    private static boolean mIsDemoMode = false;

    private Weather3D() {}

    public static boolean isDemoMode() {
        return mIsDemoMode;
    }

    public static void setIsDemoMode(boolean isDemo) {
        mIsDemoMode = isDemo;
    }
}
