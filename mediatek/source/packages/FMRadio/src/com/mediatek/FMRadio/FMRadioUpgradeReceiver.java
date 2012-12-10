package com.mediatek.FMRadio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

public class FMRadioUpgradeReceiver extends BroadcastReceiver {
    private static final String TAG = "FMRadioUpgradeReceiver" ;
    private static final String REFS_DB_VERSION = "db_version";
    private static final String TABLE_NAME  = "StationList";
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        int prefVersion = prefs.getInt(REFS_DB_VERSION, 0);
        if (prefVersion != FMRadioContentProvider.DATABASE_VERSION) {
            prefs.edit().putInt(REFS_DB_VERSION, FMRadioContentProvider.DATABASE_VERSION).commit();
            FMRadioStation.cleanAllStations(context);
        }

    }

}
