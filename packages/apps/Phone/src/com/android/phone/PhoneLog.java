package com.android.phone;

import android.util.Log;

public class PhoneLog {

    private static final String TAG = "Phone";

    public static void i(String tag, String msg) {
        Log.i(TAG, tag+"/"+msg);
    }

    public static void d(String tag, String msg) {
        Log.d(TAG, tag+"/"+msg);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag+"/"+msg);
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag+"/"+msg);
    }
}
