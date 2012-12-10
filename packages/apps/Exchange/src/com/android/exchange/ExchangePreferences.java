package com.android.exchange;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;

public class ExchangePreferences {

    // Preferences file
    public static final String PREFERENCES_FILE = "AndroidExchange.Main";

    // Preferences field names
    private static final String LOW_STORAGE = "isLowStorage";
    private static final int STORAGE_OK_SIZE = 25*1024*1024;
    
    private static ExchangePreferences sPreferences;

    private final SharedPreferences mSharedPreferences;

    private ExchangePreferences(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    /**
     * TODO need to think about what happens if this gets GCed along with the
     * Activity that initialized it. Do we lose ability to read Preferences in
     * further Activities? Maybe this should be stored in the Application
     * context.
     */
    public static synchronized ExchangePreferences getPreferences(Context context) {
        if (sPreferences == null) {
            sPreferences = new ExchangePreferences(context);
        }
        return sPreferences;
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return getPreferences(context).mSharedPreferences;
    }

    public void setLowStorage(boolean value) {
        mSharedPreferences.edit().putBoolean(LOW_STORAGE, value).commit();
    }

    public boolean getLowStorage() {
        return mSharedPreferences.getBoolean(LOW_STORAGE, false);
    }

    /**
     * This fuction is used to avoid Email can not recover from low_storage state. 
     * Because we could miss the DEVICE_STORAGE_OK broadcast in some unknown cases.
     */
    public void checkLowStorage() {
        String storageDirectory = Environment.getDataDirectory().toString();
        StatFs stat = new StatFs(storageDirectory);
        int remaining = stat.getAvailableBlocks() * stat.getBlockSize();

        // Define the DEVICE_STORAGE_OK size is larger than system value to
        // avoid conflict with it.
        if (remaining > STORAGE_OK_SIZE) {
            setLowStorage(false);
        }
    }

}
