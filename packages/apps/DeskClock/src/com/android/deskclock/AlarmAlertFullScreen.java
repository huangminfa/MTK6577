/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;

import java.util.Calendar;

/**
 * Alarm Clock alarm alert: pops visible indicator and plays alarm
 * tone. This activity is the full screen version which shows over the lock
 * screen with the wallpaper as the background.
 */
public class AlarmAlertFullScreen extends Activity {

    private static final String CLOSE_FULLSCREEN_VIEW = "close.fullscreen.view";
    // These defaults must match the values in res/xml/settings.xml
    private static final String DEFAULT_SNOOZE = "10";
    // delay time to stop boot anim
    private static final int DELAY_TIME_SECONDS = 7;
    // delay time to finish activity after the boot anim start
    private static final int DELAY_FINISH_TIME = 2;
    // the priority for receiver to receive the kill alarm broadcast first
    private static final int PRIORITY = 100;
    private static final String DEFAULT_VOLUME_BEHAVIOR = "2";
    private static final String DEFAULT_POWER_ON_VOLUME_BEHAVIOR = "0";
    private static final String KEY_VOLUME_BEHAVIOR ="power_on_volume_behavior";
    private static final String POWER_ON_VOLUME_BEHAVIOR_PREFERENCES = "PowerOnVolumeBehavior";
    private static final String POWER_OFF_FROM_ALARM = "isPoweroffAlarm";
    protected static final String SCREEN_OFF = "screen_off";
    private static final String ALARM_REQUEST_SHUTDOWN_ACTION = "android.intent.action.ACTION_ALARM_REQUEST_SHUTDOWN";
    private static final String NORMAL_SHUTDOWN_ACTION = "android.intent.action.normal.shutdown";
    private static final String NORMAL_BOOT_ACTION = "android.intent.action.normal.boot";
    private static final String DISABLE_POWER_KEY_ACTION = "android.intent.action.DISABLE_POWER_KEY";
    private static final String NORMAL_BOOT_DONE_ACTION = "android.intent.action.normal.boot.done";

    protected Alarm mAlarm;
    private int mVolumeBehavior;
    boolean mFullscreenStyle;
    private boolean mBootFromPoweroffAlarm;
    private boolean mIsPoweroffAlarm;
    private LayoutInflater mInflater;
    private Context mContext;

    // Receives the ALARM_KILLED action from the AlarmKlaxon,
    // and also ALARM_SNOOZE_ACTION / ALARM_DISMISS_ACTION from other applications
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Alarms.ALARM_SNOOZE_ACTION)) {
                snooze();
            } else if (action.equals(Alarms.ALARM_DISMISS_ACTION)) {
                dismiss(false);
            }else if(CLOSE_FULLSCREEN_VIEW.equals(action)){
                Log.v("receive the close fullscreen view bRs.");
                dismiss(true);
            } else {
        if (mBootFromPoweroffAlarm && mIsPoweroffAlarm) {
            if (!intent.getBooleanExtra("dismissAlarm", false)) {
            snooze();
            }
        } else {
            Alarm alarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
            if (alarm != null && mAlarm.id == alarm.id) {
            dismiss(true);
            }
        }
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.v("onCreate");
        mContext = AlarmAlertFullScreen.this;
        // +MediaTek 2012-02-28 enable key dispatch
        try {
            final IWindowManager wm = IWindowManager.Stub.asInterface(
                    ServiceManager.getService(Context.WINDOW_SERVICE));
            wm.setEventDispatching(true);
        } catch (RemoteException e) {}
        // -MediaTek 2012-02-28 enable key dispatch

        mAlarm = getIntent().getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        mIsPoweroffAlarm = getIntent().getBooleanExtra(POWER_OFF_FROM_ALARM,false);
        mBootFromPoweroffAlarm = Alarms.bootFromPoweroffAlarm();   
        mInflater = LayoutInflater.from(this);

        // Get the volume/camera button behavior setting
        if (mBootFromPoweroffAlarm && mIsPoweroffAlarm) {
            AlarmAlertWakeLock.acquireAlarmAlertFSCpuWakeLock(this);
            Log.v("AlarmAlertFullScreen acquireCpuWakeLock");
            getPowerOnVolumeBehaviod();
        } else {
            final String vol =
                    PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(SettingsActivity.KEY_VOLUME_BEHAVIOR,
                            DEFAULT_VOLUME_BEHAVIOR);
            mVolumeBehavior = Integer.parseInt(vol);
        }

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        // Turn on the screen unless we are being launched from the AlarmAlert
        // subclass as a result of the screen turning off.
        if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }

        if (mBootFromPoweroffAlarm && mIsPoweroffAlarm) {
            win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED);
            updateLayoutForPowerOn();
        } else {
            updateLayout();
        }

        // Register to get the alarm killed/snooze/dismiss intent.
        IntentFilter filter = new IntentFilter(Alarms.ALARM_KILLED);
        filter.addAction(Alarms.ALARM_SNOOZE_ACTION);
        filter.addAction(Alarms.ALARM_DISMISS_ACTION);
        filter.addAction(CLOSE_FULLSCREEN_VIEW);
        filter.setPriority(PRIORITY);
        registerReceiver(mReceiver, filter);
    }

    private void setTitle() {
        final String titleText = mAlarm.getLabelOrDefault(this);
        
        setTitle(titleText);
    }

    protected int getLayoutResId() {
        return R.layout.alarm_alert_fullscreen;
    }
    
    protected int getPowerOnLayoutResId() {
        return R.layout.alarm_alert_power_on_fullscreen;
    }
    
    private void updateLayout() {
        setContentView(mInflater.inflate(getLayoutResId(), null));

        /* snooze behavior: pop a snooze confirmation view, kick alarm
           manager. */
        Button snooze = (Button) findViewById(R.id.snooze);
        snooze.requestFocus();
        snooze.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                snooze();
            }
        });

        /* dismiss button: close notification */
        findViewById(R.id.dismiss).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        dismiss(false);
                    }
                });

        /* Set the title from the passed in alarm */
        setTitle();
    }
    
    private void closeFullScreenView(){
        Log.v("close fired from alarm alert dialog");
        Intent intent = new Intent(CLOSE_FULLSCREEN_VIEW);
        sendBroadcast(intent);
    }
    private void updateLayoutForPowerOn() {
        setContentView(mInflater.inflate(getPowerOnLayoutResId(), null));
        Button snooze = (Button) findViewById(R.id.snooze);
        Button powerOn = (Button) findViewById(R.id.power_on);
        Button powerOff = (Button) findViewById(R.id.power_off);
        snooze.requestFocus();
        snooze.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                snooze();
            }
        });
        powerOn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                powOn();
            }
        });
        powerOff.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                powOff();
            }
        });
        /* Set the title from the passed in alarm */
        setTitle();
        /* disable the power key */
        disablePowerKey();
    }

    // Attempt to snooze this alert.
    private void snooze() {
        if (mBootFromPoweroffAlarm && mIsPoweroffAlarm) {
            setButtonCannotClick();
        }
        // Do not snooze if the snooze button is disabled.
        if (!findViewById(R.id.snooze).isEnabled()) {
            dismiss(false);
            return;
        }
        final String snooze =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_ALARM_SNOOZE, DEFAULT_SNOOZE);
        int snoozeMinutes = Integer.parseInt(snooze);

        final long snoozeTime = System.currentTimeMillis()
                + ((long)1000 * 60 * snoozeMinutes);
        Alarms.saveSnoozeAlert(AlarmAlertFullScreen.this, mAlarm.id,
                snoozeTime);

        // Get the display time for the snooze and update the notification.
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(snoozeTime);

        // Append (snoozed) to the label.
        String label = mAlarm.getLabelOrDefault(this);
        label = getString(R.string.alarm_notify_snooze_label, label);

        // Notify the user that the alarm has been snoozed.
        Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
        cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
        cancelSnooze.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
        PendingIntent broadcast =
                PendingIntent.getBroadcast(this, mAlarm.id, cancelSnooze, 0);
        NotificationManager nm = getNotificationManager();
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                label, 0);
        n.setLatestEventInfo(this, label,
                getString(R.string.alarm_notify_snooze_text,
                    Alarms.formatTime(this, c)), broadcast);
        n.flags |= Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONGOING_EVENT;
        nm.notify(mAlarm.id, n);

        String displayTime = getString(R.string.alarm_alert_snooze_set,
                snoozeMinutes);
        // Intentionally log the snooze time for debugging.
        Log.v(displayTime);

        // Display the snooze minutes in a toast.
        Toast.makeText(AlarmAlertFullScreen.this, displayTime,
                Toast.LENGTH_LONG).show();
        stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        if(this instanceof AlarmAlert){
            closeFullScreenView();            
        }
        // if it is poweron alarm,then shut down the device
        if (mBootFromPoweroffAlarm && mIsPoweroffAlarm) {
            shutDown();
        }else{
            finish();
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    // Dismiss the alarm.
    private void dismiss(boolean killed) {
        Log.i(killed ? "Alarm killed" : "Alarm dismissed by user");
        // The service told us that the alarm has been killed, do not modify
        // the notification or stop the service.
        if (!killed) {
            stopPlayAlarm();
        }
        if(this instanceof AlarmAlert){
            closeFullScreenView();
        }
        finish();
    }
    
    // power on the device
    private void powOn() {
        setButtonCannotClick();
        // +MediaTek 2012-02-28 disable key dispatch
        try {
            final IWindowManager wm = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
            wm.setEventDispatching(false);
        } catch (RemoteException e) {}
        // -MediaTek 2012-02-28 disable key dispatch
        if (mBootFromPoweroffAlarm && mIsPoweroffAlarm) {
            AlarmAlertWakeLock.releaseAlarmAlertFSCpuLock();
            Log.v("AlarmAlertFullScreen releaseCpuLock");
        }
        stopPlayAlarm();
        // start boot animation
        SystemProperties.set("ctl.start", "bootanim");
        Log.v("start boot animation");
        // send broadcast to power on the phone
        Intent bootIntent = new Intent(NORMAL_BOOT_ACTION);
        sendBroadcast(bootIntent);
        Log.v("send broadcast: android.intent.action.normal.boot");
        enablePowerKey();
        Handler handler = new Handler();
        // close boot animation after 5 seconds
        AlarmManager am = (AlarmManager) mContext
                .getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                (SystemClock.elapsedRealtime() + 1000 * DELAY_TIME_SECONDS),
                PendingIntent.getBroadcast(mContext, 0, new Intent(
                        NORMAL_BOOT_DONE_ACTION),
                        PendingIntent.FLAG_CANCEL_CURRENT));
        Log.v("stop boot animation");

        // finish after the boot animation start
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000 * DELAY_FINISH_TIME);
        SystemProperties.set("sys.boot.reason", "0");
    }
    
    // power off the device
    private void powOff() {
        setButtonCannotClick();
        // +MediaTek 2012-02-28 disable key dispatch
        try {
            final IWindowManager wm = IWindowManager.Stub.asInterface(
            ServiceManager.getService(Context.WINDOW_SERVICE));
            wm.setEventDispatching(false);
        } catch (RemoteException e) {}
        // -MediaTek 2012-02-28 disable key dispatch
  
        stopPlayAlarm();
        shutDown();
    }
    
    // Cancel the notification and stop playing the alarm
    private void stopPlayAlarm() {
        NotificationManager nm = getNotificationManager();
        nm.cancel(mAlarm.id);
        stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
    }

    // shut down the device
    private void shutDown() {
        // send normal shutdown broadcast
        Intent shutdownIntent = new Intent(NORMAL_SHUTDOWN_ACTION);
        sendBroadcast(shutdownIntent);
        Log.v("send shutdown broadcast: android.intent.action.normal.shutdown");
        enablePowerKey();
        // shutdown the device
        Intent intent = new Intent(ALARM_REQUEST_SHUTDOWN_ACTION);
        intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    
    /**
     * this is called when a second alarm is triggered while a
     * previous alert window is still active.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Log.LOGV) Log.v("AlarmAlert.OnNewIntent()");
        mAlarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        setTitle();
    }

    @Override
    protected void onResume() {
        Log.v("onResume");
        super.onResume();
        // If the alarm was deleted at some point, disable snooze.
        if (Alarms.getAlarm(getContentResolver(), mAlarm.id) == null) {
            Button snooze = (Button) findViewById(R.id.snooze);
            snooze.setEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
        if (Log.LOGV) Log.v("AlarmAlert.onDestroy()");
        // No longer care about the alarm being killed.
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Do this on key down to handle a few of the system keys.
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        switch (event.getKeyCode()) {
            // Volume keys and camera keys dismiss the alarm
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
                if (up) {
                    switch (mVolumeBehavior) {
                        case 1:
                            snooze();
                            break;

                        case 2:
                            dismiss(false);
                            break;

                        default:
                            break;
                    }
                }
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss. This method is overriden by AlarmAlert
        // so that the dialog is dismissed.
        return;
    }
    
    // disable the power key when the device is boot from alarm but not ipo boot
    private void disablePowerKey() {
        Intent disablePowerKeyIntent = new Intent(DISABLE_POWER_KEY_ACTION);
        disablePowerKeyIntent.putExtra("state", true);
        sendBroadcast(disablePowerKeyIntent);
        Log.v("send disablePowerKey broadcast: android.intent.action.DISABLE_POWER_KEY");
    }
    
    // enable the power key when power on or power off the device
    private void enablePowerKey() {
        Intent enablePowerKeyIntent = new Intent(DISABLE_POWER_KEY_ACTION);
        enablePowerKeyIntent.putExtra("state", false);
        sendBroadcast(enablePowerKeyIntent);
        Log.v("send enablePowerKey broadcast: android.intent.action.DISABLE_POWER_KEY");
    }
    
    // get the power on volume behaviod , if the file is not exist,then new the
    // file
    private void getPowerOnVolumeBehaviod() {
        SharedPreferences prefs = getSharedPreferences(
                POWER_ON_VOLUME_BEHAVIOR_PREFERENCES, 0);
        if (!prefs.contains(KEY_VOLUME_BEHAVIOR)) {
            SharedPreferences.Editor ed = prefs.edit();
            ed.putString(KEY_VOLUME_BEHAVIOR, DEFAULT_POWER_ON_VOLUME_BEHAVIOR);
            ed.apply();
        }
        final String poweronVol = prefs.getString(KEY_VOLUME_BEHAVIOR,
                DEFAULT_POWER_ON_VOLUME_BEHAVIOR);
        mVolumeBehavior = Integer.parseInt(poweronVol);
        if (mVolumeBehavior == 2) {
            mVolumeBehavior = 0;
        }
    }
    
    private void setButtonCannotClick(){
        findViewById(R.id.power_on).setClickable(false);
        findViewById(R.id.snooze).setClickable(false);
        findViewById(R.id.power_off).setClickable(false);
    }
}
