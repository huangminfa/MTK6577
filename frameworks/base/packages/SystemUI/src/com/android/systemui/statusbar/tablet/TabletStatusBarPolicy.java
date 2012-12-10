/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.systemui.statusbar.tablet;

import android.app.StatusBarManager;
//import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.util.Slog;

import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.cdma.TtyIntent;
import com.android.systemui.R;
import com.mediatek.xlog.Xlog;

/**
 * This class contains all of the policy about which icons are installed in the status
 * bar at boot time.  It goes through the normal API for icons, even though it probably
 * strictly doesn't need to.
 */
public class TabletStatusBarPolicy {
    private static final String TAG = "TabletStatusBarPolicy";

    private static final boolean SHOW_SYNC_ICON = false;
    
    private final Context mContext;
    private final StatusBarManager mService;
    private final Handler mHandler = new Handler();

    // storage
    private StorageManager mStorageManager;

    // Assume it's all good unless we hear otherwise.  We don't always seem
    // to get broadcasts that it *is* there.
    IccCard.State mSimState = IccCard.State.READY;

    // ringer volume
    private boolean mVolumeVisible;

    // bluetooth device status
    //private boolean mBluetoothEnabled = false;



    // sync state
    // If sync is active the SyncActive icon is displayed. If sync is not active but
    // sync is failing the SyncFailing icon is displayed. Otherwise neither are displayed.

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_ALARM_CHANGED)) {
                updateAlarm(intent);
            }
            else if (action.equals(Intent.ACTION_SYNC_STATE_CHANGED)) {
                updateSyncState(intent);
            }
            //else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED) ||
            //        action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
            //    updateBluetooth(intent);
            //}
            else if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION) ||
                    action.equals(AudioManager.VIBRATE_SETTING_CHANGED_ACTION)) {
                updateVolume();
            }
            else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                updateSimState(intent);
            }
            else if (action.equals(TtyIntent.TTY_ENABLED_CHANGE_ACTION)) {
                updateTTY(intent);
            }
            // [SystemUI] Support "Headset icon". {
            else if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                updateHeadSet(intent);
            }
            // [SystemUI] Support "Headset icon". }
        }
    };


    public TabletStatusBarPolicy(Context context) {
        mContext = context;
        mService = (StatusBarManager)context.getSystemService(Context.STATUS_BAR_SERVICE);

        // listen for broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_ALARM_CHANGED);
        filter.addAction(Intent.ACTION_SYNC_STATE_CHANGED);
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        filter.addAction(AudioManager.VIBRATE_SETTING_CHANGED_ACTION);
        //filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        filter.addAction(TtyIntent.TTY_ENABLED_CHANGE_ACTION);
        // [SystemUI] Support "Headset icon". {
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        // [SystemUI] Support "Headset icon". }
        mContext.registerReceiver(mIntentReceiver, filter, null, mHandler);

        // storage
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        mStorageManager.registerListener(
                new com.android.systemui.usb.StorageNotification(context));

        // TTY status
        mService.setIcon("tty",  R.drawable.stat_sys_tty_mode, 0, null);
        mService.setIconVisibility("tty", false);

        // Cdma Roaming Indicator, ERI
        mService.setIcon("cdma_eri", R.drawable.stat_sys_roaming_cdma_0, 0, null);
        mService.setIconVisibility("cdma_eri", false);

        // bluetooth status        
        //mService.setIcon("bluetooth", bluetoothIcon, 0, null);
        //mService.setIconVisibility("bluetooth", mBluetoothEnabled);

        // Alarm clock
        mService.setIcon("alarm_clock", R.drawable.stat_sys_alarm, 0, null);
        mService.setIconVisibility("alarm_clock", false);

        // Sync state
        mService.setIcon("sync_active", R.drawable.stat_sys_sync, 0, null);
        mService.setIcon("sync_failing", R.drawable.stat_sys_sync_error, 0, null);
        mService.setIconVisibility("sync_active", false);
        mService.setIconVisibility("sync_failing", false);

        // volume
        mService.setIcon("volume", R.drawable.stat_sys_ringer_silent, 0, null);
        mService.setIconVisibility("volume", false);
        updateVolume();

        // [SystemUI] Support "Headset icon". {
        mService.setIcon("headset", R.drawable.zzz_headset_with_mic, 0, null);
        mService.setIconVisibility("headset", false);
        // [SystemUI] Support "Headset icon". }
        
    }

    private final void updateAlarm(Intent intent) {
         boolean alarmSet = intent.getBooleanExtra("alarmSet", false);
         mService.setIconVisibility("alarm_clock", alarmSet);
    }

    private final void updateSyncState(Intent intent) {
        
    }

    private final void updateSimState(Intent intent) {
        
    }

    private final void updateVolume() {
    	
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        final int ringerMode = audioManager.getRingerMode();
        final boolean visible = ringerMode == AudioManager.RINGER_MODE_SILENT ||
                ringerMode == AudioManager.RINGER_MODE_VIBRATE;

        final int iconId;
        String contentDescription = null;
        if (audioManager.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER)) {
            iconId = R.drawable.stat_sys_ringer_vibrate;
            contentDescription = mContext.getString(R.string.accessibility_ringer_vibrate);
        } else {
            iconId =  R.drawable.stat_sys_ringer_silent;
            contentDescription = mContext.getString(R.string.accessibility_ringer_silent);
        }
       
        if (visible) {
            mService.setIcon("volume", iconId, 0, contentDescription);
        }
        if (visible != mVolumeVisible) {
            mService.setIconVisibility("volume", visible);
            mVolumeVisible = visible;
        }
    }

    //private final void updateBluetooth(Intent intent) {       
    //}

    private final void updateTTY(Intent intent) {
        
    }

    // [SystemUI] Support "Headset icon". {
    private final void updateHeadSet(Intent intent) {    
        int state = intent.getIntExtra("state", -1);
        int mic = intent.getIntExtra("microphone", -1);
        Xlog.d(TAG, "updateHeadSet, state=" + state + ", mic=" + mic + ".");
        if (state == -1 || mic == -1) {
            return;
        }
        if (state == 1) {
            if (mic == 1) {
                mService.setIcon("headset", R.drawable.zzz_headset_with_mic, 0, null);
                mService.setIconVisibility("headset", true);
            } else {
                mService.setIcon("headset", R.drawable.zzz_headset_without_mic, 0, null);
                mService.setIconVisibility("headset", true);
            }
        } else {
            mService.setIconVisibility("headset", false);
        }
    }
    // [SystemUI] Support "Headset icon". }
  
}


