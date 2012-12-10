/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.deskclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

/**
 * This receiver is used to clear status bar icon when the application data is
 * cleared by settings. 
 */
public class PackageDataClearedReceiver extends BroadcastReceiver {
    private static final String ACTION_PACKAGE_DATA_CLEARED = Intent.ACTION_SETTINGS_PACKAGE_DATA_CLEARED;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_PACKAGE_DATA_CLEARED.equals(intent.getAction())) {
            return;
        }

        String pkgName = intent.getStringExtra("packageName");
        Log.v("PackageDataClearedReceiver recevied pkgName = " + pkgName);
        if (pkgName == null || !pkgName.equals(context.getPackageName())) {
            return;
        }

        Alarms.setStatusBarIcon(context, false);
        Settings.System.putString(context.getContentResolver(),
        		Settings.System.NEXT_ALARM_FORMATTED,null);
    }
}
