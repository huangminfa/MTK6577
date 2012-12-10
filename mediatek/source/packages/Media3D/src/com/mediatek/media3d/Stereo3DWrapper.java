package com.mediatek.media3d;

import android.view.SurfaceView;
import android.view.WindowManager;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Stereo3DWrapper {
    private static final String TAG = "Media3D.Stereo3DWrapper";
    private static Method sSet3DLayout;
    private static Field s3DDisabled;
    private static Field s3DSideBySide;

    private Stereo3DWrapper() {};

    static {
        initCompatibility();
    }

    private static void initCompatibility() {
        try {
            sSet3DLayout = SurfaceView.class.getMethod(
                "set3DLayout", new Class[] { int.class } );
            s3DDisabled = WindowManager.LayoutParams.class.getField("LAYOUT3D_DISABLED");
            s3DSideBySide = WindowManager.LayoutParams.class.getField("LAYOUT3D_SIDE_BY_SIDE");
        } catch (NoSuchMethodException ex) {
            LogUtil.v(TAG, "exception : " + ex);
        } catch (NoSuchFieldException ex) {
            LogUtil.v(TAG, "exception : " + ex);
        }
    }

    public static final int INVALID_PARAM = -1;
    public static final int LAYOUT3D_DISABLED = 0;
    public static final int LAYOUT3D_SIDE_BY_SIDE = 1;

    private static int convert3DLayoutParam(int param) {
        try {
            if (param == LAYOUT3D_DISABLED && s3DDisabled != null) {
                return s3DDisabled.getInt(null);
            } else if (param == LAYOUT3D_SIDE_BY_SIDE && s3DSideBySide != null) {
                return s3DSideBySide.getInt(null);
            }
        } catch (IllegalAccessException ex) {
            LogUtil.v(TAG, "exception : " + ex);
        }
        return INVALID_PARAM;
    }

    public static void set3DLayout(SurfaceView view, int param) {
        try {
            if (sSet3DLayout != null) {
                int convertParam = convert3DLayoutParam(param);
                if (convertParam != INVALID_PARAM) {
                    sSet3DLayout.invoke(view, convertParam);
                }
            }
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                throw new RuntimeException(ite);
            }
        } catch (IllegalAccessException ex) {
            LogUtil.v(TAG, "exception : " + ex);
        }
    }

    public static boolean isStereo3DSupported() {
        return (sSet3DLayout != null) && (s3DDisabled != null) && (s3DSideBySide != null);
    }
}
