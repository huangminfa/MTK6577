package com.mediatek.FMTransmitter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class FMTransmitterUpgradeReceiver extends BroadcastReceiver {
    static final String TAG = "FMTxUpgradeReceiver";
    static final String PREF_DB_VERSION = "db_version";
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        // We are now running with the system up, but no apps started,
        // so can do whatever cleanup after an upgrade that we want.
        mContext = context.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        int prefVersion = prefs.getInt(PREF_DB_VERSION, 0);

            if (prefVersion != FMTransmitterContentProvider.DATABASE_VERSION) {
                FMTxLogUtils.d(TAG, "prefVersion:" +"currentVersion:" + prefVersion + FMTransmitterContentProvider.DATABASE_VERSION);
                // if the preversion is not the same with the currentversion
                // put current version to database
                prefs.edit().putInt(PREF_DB_VERSION, FMTransmitterContentProvider.DATABASE_VERSION).commit();
                // delete all the searched data if prefVersion is not the same as current version
                FMTransmitterStation.cleanSearchedStations(mContext);
                
                // if currentVersion is MTK_50KHZ_SUPPORT,update currentstation to 10000 
                // else if is MTK_100KHZ_SUPPORT,update currentstation to 1000
                FMTransmitterStation.setCurrentStation(mContext, FMTransmitterStation.FIXED_STATION_FREQ);
               
            }  
    }
}
