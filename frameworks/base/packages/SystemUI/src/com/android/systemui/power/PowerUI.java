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

package com.android.systemui.power;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.io.FilenameFilter;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Slog;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.server.NotificationPlayer;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.mediatek.xlog.Xlog;
import android.os.SystemProperties;
public class PowerUI extends SystemUI {
    static final String TAG = "PowerUI";

    static final boolean DEBUG = true;

    // [SystemUI] Support "Low Battery Sound". {
    Handler mHandler = new BatteryHandler();
    // [SystemUI] Support "Low Battery Sound". }
	boolean mHideLowBDialog = true; 
    int mBatteryLevel = 100;
    int mBatteryStatus = BatteryManager.BATTERY_STATUS_UNKNOWN;
    int mPlugType = 0;
    int mInvalidCharger = 0;

    int mLowBatteryAlertCloseLevel;
    int[] mLowBatteryReminderLevels = new int[2];

    AlertDialog mInvalidChargerDialog;
    AlertDialog mLowBatteryDialog;
    TextView mBatteryLevelTextView;

    public void start() {

        mLowBatteryAlertCloseLevel = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_lowBatteryCloseWarningLevel);
        mLowBatteryReminderLevels[0] = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_lowBatteryWarningLevel);
        mLowBatteryReminderLevels[1] = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_criticalBatteryWarningLevel);

        // Register for Intent broadcasts for...
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
		filter.addAction("android.intent.action.normal.boot");
		filter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        mContext.registerReceiver(mIntentReceiver, filter, null, mHandler);
        
    }

    /**
     * Buckets the battery level.
     *
     * The code in this function is a little weird because I couldn't comprehend
     * the bucket going up when the battery level was going down. --joeo
     *
     * 1 means that the battery is "ok"
     * 0 means that the battery is between "ok" and what we should warn about.
     * less than 0 means that the battery is low
     */
    private int findBatteryLevelBucket(int level) {
        if (level >= mLowBatteryAlertCloseLevel) {
            return 1;
        }
        if (level > mLowBatteryReminderLevels[0]) {
            return 0;
        }
        final int N = mLowBatteryReminderLevels.length;
        for (int i=N-1; i>=0; i--) {
            if (level <= mLowBatteryReminderLevels[i]) {
                return -1-i;
            }
        }
        throw new RuntimeException("not possible!");
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d(TAG, "action        " + action );
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {

                String bootReason = SystemProperties.get("sys.boot.reason");
                boolean ret = (bootReason != null && bootReason.equals("1")) ? true : false;
                Slog.d(TAG, "Intent start() ret = " + ret+" mHideLowBDialog= "+mHideLowBDialog );
                if(ret == true && mHideLowBDialog == true){
                	return;
                }
                
                final int oldBatteryLevel = mBatteryLevel;
                mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
                Slog.d(TAG, "oldBatteryLevel        " + oldBatteryLevel + "mBatteryLevel     " +  mBatteryLevel );
                final int oldBatteryStatus = mBatteryStatus;
                mBatteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN);
                final int oldPlugType = mPlugType;
                mPlugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 1);
                final int oldInvalidCharger = mInvalidCharger;
                mInvalidCharger = intent.getIntExtra(BatteryManager.EXTRA_INVALID_CHARGER, 0);

                final boolean plugged = mPlugType != 0;
                final boolean oldPlugged = oldPlugType != 0;

                // [SystemUI] Support "Low Battery Sound". {
                if (mInBatteryLow && mMediaPlayerInUse) {
                    if (plugged) {
                        if (mNP != null) {
                            mNP.stop();
                        }
                    }
                }
                // [SystemUI] Support "Low Battery Sound". }
                int oldBucket = findBatteryLevelBucket(oldBatteryLevel);
                int bucket = findBatteryLevelBucket(mBatteryLevel);

                if (DEBUG) {
                    Slog.d(TAG, "buckets   ....." + mLowBatteryAlertCloseLevel
                            + " .. " + mLowBatteryReminderLevels[0]
                            + " .. " + mLowBatteryReminderLevels[1]);
                    Slog.d(TAG, "level          " + oldBatteryLevel + " --> " + mBatteryLevel);
                    Slog.d(TAG, "status         " + oldBatteryStatus + " --> " + mBatteryStatus);
                    Slog.d(TAG, "plugType       " + oldPlugType + " --> " + mPlugType);
                    Slog.d(TAG, "invalidCharger " + oldInvalidCharger + " --> " + mInvalidCharger);
                    Slog.d(TAG, "bucket         " + oldBucket + " --> " + bucket);
                    Slog.d(TAG, "plugged        " + oldPlugged + " --> " + plugged);
                }

                if (oldInvalidCharger == 0 && mInvalidCharger != 0) {
                    Slog.d(TAG, "showing invalid charger warning");
                    showInvalidChargerDialog();
                    return;
                } else if (oldInvalidCharger != 0 && mInvalidCharger == 0) {
                	 Slog.d(TAG, "dismissInvalidChargerDialog***************      " );
                    dismissInvalidChargerDialog();
                } else if (mInvalidChargerDialog != null) {
                    // if invalid charger is showing, don't show low battery
                    return;
                }

                if (!plugged
                        && (bucket < oldBucket || oldPlugged)
                        && mBatteryStatus != BatteryManager.BATTERY_STATUS_UNKNOWN
                        && bucket < 0) {
                    showLowBatteryWarning();

                    // only play SFX when the dialog comes up or the bucket changes
                    if (bucket != oldBucket || oldPlugged) {
                    	Slog.d(TAG, "playLowBatterySound1*************        " );
                        playLowBatterySound();
                    }
                }
				else if (plugged || (bucket > oldBucket && bucket > 0)) {
					 Slog.d(TAG, "dismissLowBatteryWarning***************      " );
                    dismissLowBatteryWarning();
                    mNP.stop();
                } else if (mBatteryLevelTextView != null) {
                    showLowBatteryWarning();
                }
            }
            // [SystemUI] Support "Low Battery Sound". {
            else if (action.equals(Intent.ACTION_BATTERY_LOW)) {
                mInBatteryLow = true;
            } else if (action.equals(Intent.ACTION_BATTERY_OKAY) || action.equals(Intent.ACTION_POWER_CONNECTED)) {
                mInBatteryLow = false;
            }
            // [SystemUI] Support "Low Battery Sound". }
            else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
            	if(mLowBatteryDialog!= null && mLowBatteryDialog.isShowing()){
            	   CharSequence levelText = mContext.getString(
                           R.string.battery_low_percent_format, mBatteryLevel);
                   if (mBatteryLevelTextView != null) {
                       mBatteryLevelTextView.setText(levelText);
                   } else {
                       View v = View.inflate(mContext, R.layout.battery_low, null);
                       mBatteryLevelTextView = (TextView)v.findViewById(R.id.level_percent);

                       mBatteryLevelTextView.setText(levelText);
                   }
            	}
            }
            else if (action.equals("android.intent.action.normal.boot")){
            	Slog.d(TAG, "Intetnt android.intent.action.normal.boot mHideLowBDialog = " + mHideLowBDialog );
            	mHideLowBDialog = false;
            }
            else if(action.equals("android.intent.action.ACTION_SHUTDOWN_IPO")) {
            	Slog.d(TAG, "Intent android.intent.action.ACTION_SHUTDOWN_IPO mHideLowBDialog = " + mHideLowBDialog );
            	mHideLowBDialog = true;
            	mBatteryLevel = 100;
            }
            else {
                Slog.w(TAG, "unknown intent: " + intent);
            }
        }
    };

    void dismissLowBatteryWarning() {
        if (mLowBatteryDialog != null) {
            Slog.i(TAG, "closing low battery warning: level=" + mBatteryLevel);
            mLowBatteryDialog.dismiss();
        }
    }

    void showLowBatteryWarning() {
        Slog.i(TAG,
                ((mBatteryLevelTextView == null) ? "showing" : "updating")
                + " low battery warning: level=" + mBatteryLevel
                + " [" + findBatteryLevelBucket(mBatteryLevel) + "]");

        CharSequence levelText = mContext.getString(
                R.string.battery_low_percent_format, mBatteryLevel);

        if (mBatteryLevelTextView != null) {
            mBatteryLevelTextView.setText(levelText);
        } else {
            View v = View.inflate(mContext, R.layout.battery_low, null);
            mBatteryLevelTextView = (TextView)v.findViewById(R.id.level_percent);

            mBatteryLevelTextView.setText(levelText);

            AlertDialog.Builder b = new AlertDialog.Builder(mContext);
                b.setCancelable(true);
                b.setTitle(R.string.battery_low_title);
                b.setView(v);
                b.setIconAttribute(android.R.attr.alertDialogIcon);
                b.setPositiveButton(android.R.string.ok, null);

            final Intent intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_NO_HISTORY);
            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                b.setNegativeButton(R.string.battery_low_why,
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mContext.startActivity(intent);
                        dismissLowBatteryWarning();
                    }
                });
            }

            AlertDialog d = b.create();
            d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        mLowBatteryDialog = null;
                        mBatteryLevelTextView = null;
                    }
                });
            d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            d.show();
            mLowBatteryDialog = d;
        }
    }

    void playLowBatterySound() {
        if (DEBUG) {
            Slog.i(TAG, "playing low battery sound. WOMP-WOMP!");
        }

        // [SystemUI] Support "Low Battery Sound". {
        Xlog.d(TAG, "playLowBatterySound");
        Message msg = Message.obtain();
        msg.what = EVENT_LOW_BATTERY_WARN_SOUND;
        mHandler.sendMessage(msg);
        // [SystemUI] Support "Low Battery Sound". }

        final ContentResolver cr = mContext.getContentResolver();
        if (Settings.System.getInt(cr, Settings.System.POWER_SOUNDS_ENABLED, 1) == 1) {
            final String soundPath = Settings.System.getString(cr,
                    Settings.System.LOW_BATTERY_SOUND);
            if (soundPath != null) {
                final Uri soundUri = Uri.parse("file://" + soundPath);
                if (soundUri != null) {
                    final Ringtone sfx = RingtoneManager.getRingtone(mContext, soundUri);
                    if (sfx != null && mNP == null) {
                        sfx.setStreamType(AudioManager.STREAM_NOTIFICATION);
                        sfx.play();
                    }
                }
            }
        }
    }

    void dismissInvalidChargerDialog() {
        if (mInvalidChargerDialog != null) {
            mInvalidChargerDialog.dismiss();
        }
    }

    void showInvalidChargerDialog() {
        Slog.d(TAG, "showing invalid charger dialog");

        dismissLowBatteryWarning();

        AlertDialog.Builder b = new AlertDialog.Builder(mContext);
            b.setCancelable(true);
            b.setMessage(R.string.invalid_charger);
            b.setIconAttribute(android.R.attr.alertDialogIcon);
            b.setPositiveButton(android.R.string.ok, null);

        AlertDialog d = b.create();
            d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        mInvalidChargerDialog = null;
                        mBatteryLevelTextView = null;
                    }
                });

        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        d.show();
        mInvalidChargerDialog = d;
    }
    
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("mLowBatteryAlertCloseLevel=");
        pw.println(mLowBatteryAlertCloseLevel);
        pw.print("mLowBatteryReminderLevels=");
        pw.println(Arrays.toString(mLowBatteryReminderLevels));
        pw.print("mInvalidChargerDialog=");
        pw.println(mInvalidChargerDialog == null ? "null" : mInvalidChargerDialog.toString());
        pw.print("mLowBatteryDialog=");
        pw.println(mLowBatteryDialog == null ? "null" : mLowBatteryDialog.toString());
        pw.print("mBatteryLevel=");
        pw.println(Integer.toString(mBatteryLevel));
        pw.print("mBatteryStatus=");
        pw.println(Integer.toString(mBatteryStatus));
        pw.print("mPlugType=");
        pw.println(Integer.toString(mPlugType));
        pw.print("mInvalidCharger=");
        pw.println(Integer.toString(mInvalidCharger));
        pw.print("bucket: ");
        pw.println(Integer.toString(findBatteryLevelBucket(mBatteryLevel)));
    }

    // [SystemUI] Support "Low Battery Sound". {

    private static final int EVENT_LOW_BATTERY_WARN_SOUND = 10;

    private ToneGenerator mToneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, 100);
    private NotificationPlayer mNP = new NotificationPlayer("StatusBarPolicy");
    private static final String SOUNDDIRECTORY = "/system/media/audio/ui/";
    private boolean mInBatteryLow = false;
    private boolean mMediaPlayerInUse = false;

    private class BatteryHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EVENT_LOW_BATTERY_WARN_SOUND:
                final AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                String path = findTestFile("battery");
                if (mNP == null) {
                    mNP = new NotificationPlayer("StatusBarPolicy");
                }
                if (path != null) {
                    mMediaPlayerInUse = true;
                    String totolPath = SOUNDDIRECTORY + path;
                    File soundFile = new File(totolPath);
                    if (audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM) != 0) {
                        Xlog.d(TAG, "handleMessage, soundFile=" + soundFile);
                        mNP.play(mContext, Uri.fromFile(soundFile), false, AudioManager.STREAM_SYSTEM);
                    }
                } else {
                    mMediaPlayerInUse = false;
                    if (mToneGenerator != null) {
                        mToneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP);
                    }
                }
                break;
            }
        }
    }

   private String findTestFile(final String name) {
       Xlog.d(TAG, "findTestFile, name=" + name);
       File directory = new File("/system/media/audio/ui/");
       File[] list = directory.listFiles(new FilenameFilter(){
       public boolean accept(File dir,String filename) {
             return filename.startsWith (name);
       }
         });
      if(list.length>0){
      File file=list[0];
      for (int i = 1; i < list.length; i++) {
      if(list[i] .lastModified()<file.lastModified()){
      file=list[i];
      }
    }
            return file.getName();
   }
      
    Xlog.d(TAG, "return=null");
            return null;
}
// [SystemUI] Support "Low Battery Sound". }
}

