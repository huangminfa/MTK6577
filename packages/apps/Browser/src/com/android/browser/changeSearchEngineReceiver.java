package com.android.browser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mediatek.xlog.Xlog;

public class changeSearchEngineReceiver extends BroadcastReceiver {
    private final static String XLOGTAG = "browser/changeSearchEngineReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = p.edit();
        if (null == intent.getExtras()) return;
        editor.putString(BrowserSettings.PREF_SEARCH_ENGINE, intent.getExtras().getString(BrowserSettings.PREF_SEARCH_ENGINE));
        editor.commit();
        Xlog.d(XLOGTAG,"changeSearchEngineReceiver" + BrowserSettings.PREF_SEARCH_ENGINE + "---" + intent.getExtras().getString(BrowserSettings.PREF_SEARCH_ENGINE));
    }
}
