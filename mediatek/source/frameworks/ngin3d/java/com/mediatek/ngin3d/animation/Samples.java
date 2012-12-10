package com.mediatek.ngin3d.animation;

import java.util.HashMap;

/**
 * A helper class to store arrays of sample.
 * @hide
 */
public class Samples {
    // Types
    public static final int TRANSLATE = 1;
    public static final int ROTATE = 2;
    public static final int SCALE = 3;
    public static final int ALPHA = 4;
    public static final int X_ROTATE = 5;
    public static final int Y_ROTATE = 6;
    public static final int Z_ROTATE = 7;
    public static final int ANCHOR_POINT = 8;
    public static final int MARKER = 9;

    // Arrays
    public static final String VALUE = "v";
    public static final String X_AXIS = "x";
    public static final String Y_AXIS = "y";
    public static final String Z_AXIS = "z";
    public static final String CURVE_TYPE = "type";
    public static final String KEYFRAME_TIME = "time";
    public static final String IN_TANX = "itx";
    public static final String IN_TANY = "ity";
    public static final String IN_TANZ = "itz";
    public static final String OUT_TANX = "otx";
    public static final String OUT_TANY = "oty";
    public static final String OUT_TANZ = "otz";
    public static final String IN_TANVAL = IN_TANX;
    public static final String OUT_TANVAL = OUT_TANX;
    public static final String ACTION = "action";
    public static final String MARKER_TIME = "time";

    private final HashMap<String, float[]> mSampleArrays = new HashMap<String, float[]>();
    private final HashMap<String, int[]> mIntSampleArrays = new HashMap<String, int[]>();
    private final HashMap<String, String[]> mStringSampleArrays = new HashMap<String, String[]>();

    private final int mType;

    public Samples(int type) {
        mType = type;
    }

    public int getType() {
        return mType;
    }

    public Samples add(String name, float[] array) {
        mSampleArrays.put(name, array);
        return this;
    }

    public Samples remove(String name) {
        mSampleArrays.remove(name);
        mIntSampleArrays.remove(name);
        return this;
    }

    public float[] get(String name) {
        return mSampleArrays.get(name);
    }

    public Samples add(String name, int[] array) {
        mIntSampleArrays.put(name, array);
        return this;
    }

    public int[] getInt(String name) {
        return mIntSampleArrays.get(name);
    }

    public Samples add(String name, String[] array) {
        mStringSampleArrays.put(name, array);
        return this;
    }

    public String[] getString(String name) {
        return mStringSampleArrays.get(name);
    }
}