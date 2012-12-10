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

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.os.PowerManager.WakeLock;

public class AlarmInitReceiver extends BroadcastReceiver {
    private static final String IPO_BOOT_ACTION = "android.intent.action.ACTION_BOOT_IPO";
    private static boolean mBlockTimeChange = false;

    /**
     * Sets alarm on ACTION_BOOT_COMPLETED. Resets alarm on TIME_SET,
     * TIMEZONE_CHANGED.
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        Log.v("AlarmInitReceiver: action = " + action + ",mBlockTimeChange = " + mBlockTimeChange);

        /*
         * Note: Never call setNextAlert when the device is boot from power off
         * alarm, since it would make the power off alarm dismiss the wrong
         * alarm.
         */
        if (IPO_BOOT_ACTION.equals(action)) {
            Log.v("Receive android.intent.action.ACTION_BOOT_IPO intent.");
            mBlockTimeChange = true;
            return;
        }

        if (mBlockTimeChange && Intent.ACTION_TIME_CHANGED.equals(action)) {
            Log.v("Ignore time change broadcast because it is sent from ipo.");
            return;
        }

        final PendingResult result = goAsync();        
        AsyncHandler.post(new Runnable() {
            @Override
            public void run() {
                WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
                wl.acquire();
                // Remove the snooze alarm after a boot.
                if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                    mBlockTimeChange = false;                    
                    if (Alarms.bootFromPoweroffAlarm()) {
                        /*
                         * If the boot complete is from power off alarm, do not
                         * call setNextAlert and disableExpiredAlarms, because
                         * it will change the nearest alarm in the preference,
                         * and the power off alarm receiver may get the wrong
                         * alarm.
                         */
                        Log.v("AlarmInitReceiver recieves boot complete because power off alarm.");  
                        Alarms.disableAllSnoozedAlarms(context);
                        Alarms.disableExpiredAlarms(context);
                    } else {
                        Alarms.saveSnoozeAlert(context, Alarms.INVALID_ALARM_ID, -1);
                        Alarms.disableExpiredAlarms(context);
                        Alarms.setNextAlert(context);
                    }
                } else {
                    Alarms.disableExpiredAlarms(context);
                    /* If time changes, we need to reset the time column of alarms in database. */
                    if (Intent.ACTION_TIME_CHANGED.equals(action)
                            || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                        Alarms.resetAlarmTimes(context);
                    } 
                    Alarms.setNextAlert(context);
                }
                result.finish();
                Log.v("AlarmInitReceiver finished.");
                wl.release();
            }
        });
    }
}
