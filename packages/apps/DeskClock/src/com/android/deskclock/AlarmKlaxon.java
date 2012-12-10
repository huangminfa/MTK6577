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

package com.android.deskclock;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.provider.MediaStore;
import android.provider.Settings;
import java.io.IOException;

import com.android.internal.telephony.ITelephony;
import com.mediatek.featureoption.FeatureOption;

/**
 * Manages alarms and vibe. Runs as a service so that it can continue to play
 * if another activity overrides the AlarmAlert dialog.
 */
public class AlarmKlaxon extends Service {
    // Default of 10 minutes until alarm is silenced.
    private static final String DEFAULT_ALARM_TIMEOUT = "10";
    /* Retry to play rintone after 1 seconds if power off alarm can not find external resource. */
    private static final int MOUNT_TIMEOUT_SECONDS = 1;

    private static final long[] sVibratePattern = new long[] { 500, 500 };
    private static final int GIMINI_SIM_1 = 0;
    private static final int GIMINI_SIM_2 = 1;
	/** the times to retry play rintone */
	private int mRetryCount = 0;
	private static final int MAX_RETRY_COUNT = 3;

    private boolean mPlaying = false;
    private Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;
    private Alarm mCurrentAlarm;
    private long mStartTime;
    private TelephonyManager mTelephonyManager;
    private ITelephony mTelephonyService;
    private int mCurrentCallState;

    /* Whether the alarm is using an external alert. */
    private boolean mUsingExternalUri;

    // Internal messages
    private static final int KILLER = 1000;
    private static final int DELAY_TO_PLAY = 1001;
    
    private static final String POWER_OFF_FROM_ALARM = "isPoweroffAlarm";
    private boolean mbootFromPoweroffAlarm;

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KILLER:
                    if (Log.LOGV) {
                        Log.v("*********** Alarm killer triggered ***********");
                    }
                    stopPlayAlert((Alarm) msg.obj);
                    break;

                case DELAY_TO_PLAY:
                    Log.v("Alarm play external ringtone failed, retry to play after 1 seconds.");
                    play((Alarm) msg.obj);
                    break;

                default:
                    break;
            }
        }
    };

    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String ignored) {
            // The user might already be in a call when the alarm fires. When
            // we register onCallStateChanged, we get the initial in-call state
            // which kills the alarm. Check against the initial call state so
            // we don't kill the alarm during a call.
            int newPhoneState = TelephonyManager.CALL_STATE_IDLE;
			if (mTelephonyService != null) {
		        try {
		            newPhoneState = mTelephonyService.getPreciseCallState();
		        }catch (RemoteException ex) {
		            Log.v("Catch exception when getPreciseCallState: ex = " + ex.getMessage());
		        }
			}
            
            if (newPhoneState == TelephonyManager.CALL_STATE_IDLE) {
                mCurrentCallState = TelephonyManager.CALL_STATE_IDLE;
            }
            
            Log.v("onCallStateChanged : current state = " + newPhoneState + ",state = " + state
                    + ",mInitialCallState = " + mCurrentCallState);
            if (newPhoneState != TelephonyManager.CALL_STATE_IDLE
                    && newPhoneState != mCurrentCallState) {
                Log.v("Call state changed: mInitialCallState = " + mCurrentCallState
                        + ",mCurrentAlarm = " + mCurrentAlarm);
				if (mCurrentAlarm != null) {
					stopPlayAlert(mCurrentAlarm);
				}
            }
        }
    };

    @Override
    public void onCreate() {
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Listen for incoming calls to kill the alarm.
        mTelephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyService = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        
        // Check if the device is gemini supported
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mTelephonyManager.listenGemini(mPhoneStateListener,
                    PhoneStateListener.LISTEN_CALL_STATE | PhoneStateListener.LISTEN_SERVICE_STATE,
                    GIMINI_SIM_1);
            mTelephonyManager.listenGemini(mPhoneStateListener,
                    PhoneStateListener.LISTEN_CALL_STATE | PhoneStateListener.LISTEN_SERVICE_STATE,
                    GIMINI_SIM_2);
        } else {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        AlarmAlertWakeLock.acquireCpuWakeLock(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mediaActionMonitor, filter);
    }

    @Override
    public void onDestroy() {
        stop();
        // Stop listening for incoming calls.
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mTelephonyManager.listenGemini(mPhoneStateListener, 0, GIMINI_SIM_1);
            mTelephonyManager.listenGemini(mPhoneStateListener, 0, GIMINI_SIM_2);
        } else {
            mTelephonyManager.listen(mPhoneStateListener, 0);
        }
		mHandler.removeMessages(DELAY_TO_PLAY);
		Log.v("mHandler.removeMessages DELAY_TO_PLAY");
        AlarmAlertWakeLock.releaseCpuLock();
        super.onDestroy();
        unregisterReceiver(mediaActionMonitor);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // No intent, tell the system not to restart us.
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        final Alarm alarm = intent.getParcelableExtra(
                Alarms.ALARM_INTENT_EXTRA);

        if (alarm == null) {
            Log.v("AlarmKlaxon failed to parse the alarm from the intent");
            stopSelf();
            return START_NOT_STICKY;
        }
        
		if (intent.getBooleanExtra(POWER_OFF_FROM_ALARM, false)
				&& Alarms.bootFromPoweroffAlarm()) {
			mbootFromPoweroffAlarm = true;
			Log.v("AlarmKlaxon mbootFromPoweroffAlarm is true");
		}
        if (mCurrentAlarm != null && mCurrentAlarm.time != alarm.time && mCurrentAlarm.id != alarm.id) {     
            Log.v("*********** onStartCommand ***********");
			if (mbootFromPoweroffAlarm) {
				long millis = System.currentTimeMillis() - mStartTime;
				int minutes = (int) Math.round(millis / 60000.0);
				Intent alarmKilled = new Intent(Alarms.ALARM_KILLED);
				alarmKilled.putExtra(Alarms.ALARM_INTENT_EXTRA, mCurrentAlarm);
				alarmKilled.putExtra(Alarms.ALARM_KILLED_TIMEOUT, minutes);
				alarmKilled.putExtra("dismissAlarm", true);
				Log.v("sendKillBroadcast: mStartTime = " + mStartTime
						+ ",millis = " + millis + ",minutes = " + minutes
						+ ",this = " + this);
				sendBroadcast(alarmKilled);
			} else {
				sendKillBroadcast(mCurrentAlarm);
			}
        }

        Log.v("onStartCommand: intent = " + intent + "alarm id = " + alarm.id + ",alert = "
                + alarm.alert);
        if (alarm.alert != null) {
            mUsingExternalUri = usingExternalUri(alarm.alert);
        }

        play(alarm);
        mCurrentAlarm = alarm;
        // Record the initial call state here so that the new alarm has the
        // newest state.
 		if (mTelephonyService != null) {
		    try {
		        mCurrentCallState = mTelephonyService.getPreciseCallState();
		    } catch (RemoteException ex) {
		        Log.v("Catch exception when getPreciseCallState: ex = " + ex.getMessage());
		    }
		}

        return START_STICKY;
    }

    private void sendKillBroadcast(Alarm alarm) {
        long millis = System.currentTimeMillis() - mStartTime;
        int minutes = (int) Math.round(millis / 60000.0);
        Intent alarmKilled = new Intent(Alarms.ALARM_KILLED);
        alarmKilled.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        alarmKilled.putExtra(Alarms.ALARM_KILLED_TIMEOUT, minutes);
        Log.v("sendKillBroadcast: mStartTime = " + mStartTime + ",millis = " + millis
                + ",minutes = " + minutes + ",this = " + this);
        sendBroadcast(alarmKilled);
    }

    // Volume suggested by media team for in-call alarms.
    private static final float IN_CALL_VOLUME = 0.125f;

    private void play(Alarm alarm) {
        // stop() checks to see if we are already playing.
        stop();

        if (Log.LOGV) {
            Log.v("AlarmKlaxon.play() " + alarm.id + " alert " + alarm.alert);
        }

        if (!alarm.silent) {
            Uri alert = alarm.alert;
            // Fall back on the default alarm if the database does not have an
            // alarm stored.
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_ALARM);
                if (Log.LOGV) {
                    Log.v("Using default alarm: " + alert.toString());
                }
            }

            // TODO: Reuse mMediaPlayer instead of creating a new one and/or use
            // RingtoneManager.
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e("Error occurred while playing audio.");
                    mp.stop();
                    mp.release();
                    mMediaPlayer = null;
                    return true;
                }
            });

            try {
                // Check if we are in a call. If we are, use the in-call alarm
                // resource at a low volume to not disrupt the call.
                if (mCurrentCallState != TelephonyManager.CALL_STATE_IDLE) {
                    Log.v("Using the in-call alert: mUsingExternalUri = " + mUsingExternalUri);
                    mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
                    setDataSourceFromResource(getResources(), mMediaPlayer,
                            R.raw.in_call_alarm);
                } else {
                    mMediaPlayer.setDataSource(this, alert);
                }
                startAlarm(mMediaPlayer);
            } catch (IOException ex) {
                Log.v("Exception occured mUsingExternalUri = " + mUsingExternalUri);
				Log.v("Exception occured retryCount = " + mRetryCount);
				if (mUsingExternalUri && mRetryCount < MAX_RETRY_COUNT) {
                    delayToPlayAlert(alarm);
                    // Reset it to false.
//                    mUsingExternalUri = false;
                    mRetryCount++;
                    mStartTime = System.currentTimeMillis();
                    return;
                } else {
                    Log.v("Using the fallback ringtone");
                    // The alert may be on the sd card which could be busy right
                    // now. Use the fallback ringtone.
                    try {
                        // Must reset the media player to clear the error state.
                        mMediaPlayer.reset();
                        setDataSourceFromResource(getResources(), mMediaPlayer,
                                R.raw.fallbackring);
                        startAlarm(mMediaPlayer);
                    } catch (IOException ioe2) {
                        // At this point we just don't play anything.
                        Log.e("Failed to play fallback ringtone", ioe2);
                    }
                }
            }
        }

        /* Start the vibrator after everything is ok with the media player */
        if (alarm.vibrate) {
            mVibrator.vibrate(sVibratePattern, 0);
        } else {
            mVibrator.cancel();
        }

        enableKiller(alarm);
        mPlaying = true;
        mStartTime = System.currentTimeMillis();
    }

    // Do the common stuff when starting the alarm.
    private void startAlarm(MediaPlayer player)
            throws java.io.IOException, IllegalArgumentException,
                   IllegalStateException {
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        // do not play alarms if stream volume is 0
        // (typically because ringer mode is silent).
	Log.v("the audio volume: " + audioManager.getStreamVolume(AudioManager.STREAM_ALARM));
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(true);
            player.prepare();
            player.start();
        }
    }

    private void setDataSourceFromResource(Resources resources,
            MediaPlayer player, int res) throws java.io.IOException {
        AssetFileDescriptor afd = resources.openRawResourceFd(res);
        if (afd != null) {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                    afd.getLength());
            afd.close();
        }
    }

    /**
     * Stops alarm audio and disables alarm if it not snoozed and not
     * repeating
     */
    public void stop() {
        if (Log.LOGV) Log.v("AlarmKlaxon.stop().");
        if (mPlaying) {
            mPlaying = false;

            Intent alarmDone = new Intent(Alarms.ALARM_DONE_ACTION);
            sendBroadcast(alarmDone);

            // Stop audio playing
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            // Stop vibrator
            mVibrator.cancel();
        }
        disableKiller();
    }

    /**
     * Kills alarm audio after ALARM_TIMEOUT_SECONDS, so the alarm
     * won't run all day.
     *
     * This just cancels the audio, but leaves the notification
     * popped, so the user will know that the alarm tripped.
     */
    private void enableKiller(Alarm alarm) {
        Log.v("enableKiller: alarm = " + alarm + ",this = " + this);
        final String autoSnooze =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_AUTO_SILENCE,
                        DEFAULT_ALARM_TIMEOUT);
        int autoSnoozeMinutes = Integer.parseInt(autoSnooze);
        if (autoSnoozeMinutes != -1) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER, alarm),
                    1000 * autoSnoozeMinutes * 60);
        }
    }

    private void disableKiller() {
        mHandler.removeMessages(KILLER);
    }

    private void delayToPlayAlert(Alarm alarm) {
        Log.v("delayToPlayAlert: alarm = " + alarm + ",this = " + this);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(DELAY_TO_PLAY, alarm),
                1000 * MOUNT_TIMEOUT_SECONDS);
    }

    private boolean usingExternalUri(Uri alert) {
        Uri mediaUri = null;      
        final String scheme = alert.getScheme();
        if ("content".equals(scheme)) {
            if (Settings.AUTHORITY.equals(alert.getAuthority())) {
                String uriString = android.provider.Settings.System.getString(this.getContentResolver(), "alarm_alert");
				if (uriString != null) {
					mediaUri = Uri.parse(uriString);
				} else {
					mediaUri = alert;
				}
            } else {
                mediaUri = alert;
            }
            
            if (MediaStore.AUTHORITY.equals(mediaUri.getAuthority())) {
                Log.v("AlarmKlaxon onStartCommand mediaUri = " +
                        mediaUri + ",segment 0 = " + mediaUri.getPathSegments().get(0));
                if (mediaUri.getPathSegments().get(0).equalsIgnoreCase("external")) {
                    // Alert is using an external ringtone.
                    return true;
                }
            }
        }
        return false;
    }

    private void stopPlayAlert(Alarm alarm) {
        Log.v("stopPlayAlert: alarm = " + alarm); 
        mHandler.removeMessages(DELAY_TO_PLAY);
        sendKillBroadcast(alarm);
        stopSelf();
    }
    
    private BroadcastReceiver mediaActionMonitor = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent){
            if(intent == null) return;
            String action = intent.getAction();
            if(Intent.ACTION_MEDIA_EJECT.equals(action) || 
                    Intent.ACTION_MEDIA_UNMOUNTED.equals(action)){
                if(mMediaPlayer != null){
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            }
        }
    };
}
