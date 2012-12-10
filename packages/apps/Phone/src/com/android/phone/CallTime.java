/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.phone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;

import android.util.Log;

import java.io.File;
import java.util.List;
import android.content.SharedPreferences;
import com.mediatek.featureoption.FeatureOption;


/**
 * Helper class used to keep track of various "elapsed time" indications
 * in the Phone app, and also to start and stop tracing / profiling.
 */
public class CallTime extends Handler {
    private static final String LOG_TAG = "PHONE/CallTime";
    private static final boolean DBG = true;
    /* package */ static final boolean PROFILE = true;
    private static final int CALL_TIME_UPDATE = 111;

    private static final int PROFILE_STATE_NONE = 0;
    private static final int PROFILE_STATE_READY = 1;
    private static final int PROFILE_STATE_RUNNING = 2;

    private static int sProfileState = PROFILE_STATE_NONE;

    private static int INTERVAL_TIME = 50;
    private static int MINUTE_TIME = 60;
    private static int MILLISECOND_TO_SECOND = 1000;
    private static int MINUTE_TO_MS = MINUTE_TIME * MILLISECOND_TO_SECOND;
	
    private Call mCall;
    private long mLastReportedTime;
    private boolean mTimerRunning;
    private long mInterval;
    private PeriodicTimerCallback mTimerCallback;
    private OnTickListener mListener;
    private static SharedPreferences mSP = null;
    
    public static String ACTION_REMINDER = "calltime_minute_reminder";
    AlarmManager mAlarm = null;
    Context mCtx = null;
    PendingIntent mReminderPendingIntent;
    CallTimeReceiver mReceiver;
    boolean mAlarmEnable = false;

    private CallTimeHandler mTimerThreadHandler;
    private static Looper mCallTimeHanderThreadLooper = null;

    interface OnTickListener {
        void onTickForCallTimeElapsed(long timeElapsed);
    }

    public CallTime(OnTickListener listener) {
        mListener = listener;
        mTimerCallback = new PeriodicTimerCallback();
        
        mCtx = PhoneApp.getInstance().getApplicationContext();
        mAlarm = (AlarmManager) mCtx.getSystemService(Context.ALARM_SERVICE);
        mReminderPendingIntent = PendingIntent.getBroadcast(mCtx, 0, new Intent(ACTION_REMINDER), 0);
        mReceiver = new CallTimeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REMINDER);
        mCtx.registerReceiver(mReceiver, filter);
        
        synchronized (CallTime.class) {
            if (null == mCallTimeHanderThreadLooper) {
                HandlerThread handlerThread = new HandlerThread("CallTimeHandlerThread");
                handlerThread.start();
                mCallTimeHanderThreadLooper = handlerThread.getLooper();
            }
        }
        mTimerThreadHandler = new CallTimeHandler(mCallTimeHanderThreadLooper);
    }

    @Override
    public void handleMessage(Message msg) {

        switch (msg.what) {
            case CALL_TIME_UPDATE:
                log("receive CALL_TIME_UPDATE message");
                if (PROFILE && isTraceRunning()) {
                    stopTrace();
                }
                mTimerRunning = false;
                periodicUpdateTimer();
                break;
            default:
                break;
        }
    }
    /**
     * Sets the call timer to "active call" mode, where the timer will
     * periodically update the UI to show how long the specified call
     * has been active.
     *
     * After calling this you should also call reset() and
     * periodicUpdateTimer() to get the timer started.
     */
    /* package */ void setActiveCallMode(Call call) {
        if (DBG) log("setActiveCallMode(" + call + ")...");
        mCall = call;

        // How frequently should we update the UI?
        mInterval = 1000;  // once per second
        mSP= PhoneApp.getInstance().getApplicationContext().getSharedPreferences("com.android.phone_preferences" , Context.MODE_PRIVATE);
        if(null == mSP)
        {
            if (DBG) log("setActiveCallMode: can not find 'com.android.phone_preferences'...");
        }	
        startReminder(getCallDuration(call));
    }

    /* package */ void reset() {
        if (DBG) log("reset()...");
        mLastReportedTime = SystemClock.uptimeMillis() - mInterval;
    }

    /* package */ void periodicUpdateTimer() {
        if (!mTimerRunning) {
            mTimerRunning = true;

            long now = SystemClock.uptimeMillis();
            long nextReport = mLastReportedTime + mInterval;

            while (now >= nextReport) {
                nextReport += mInterval;
            }

            if (DBG) log("periodicUpdateTimer() @ " + nextReport);
            mTimerThreadHandler.postAtTime(mTimerCallback, nextReport);
            mLastReportedTime = nextReport;

            if (mCall != null) {
                Call.State state = mCall.getState();

                if (state == Call.State.ACTIVE) {
                    updateElapsedTime(mCall);
                }
            }

            if (PROFILE && isTraceReady()) {
                startTrace();
            }
        } else {
            if (DBG) log("periodicUpdateTimer: timer already running, bail");
        }
    }

    /* package */ void cancelTimer() {
        if (DBG) log("cancelTimer()...");
        mTimerThreadHandler.removeCallbacks(mTimerCallback);
        removeMessages(CALL_TIME_UPDATE);
        mTimerRunning = false;
        stopReminder();
    }

    private void updateElapsedTime(Call call) {
        if (mListener != null) {
            long duration = getCallDuration(call);
            mListener.onTickForCallTimeElapsed(duration / 1000);
        }
    }

    /**
     * Returns a "call duration" value for the specified Call, in msec,
     * suitable for display in the UI.
     */
    /* package */ static long getCallDuration(Call call) {
        if (true == FeatureOption.MTK_VT3G324M_SUPPORT && null != call.getLatestConnection()
                                                       && call.getLatestConnection().isVideo()) {
            if (call.getLatestConnection() != VTInCallScreenFlags.getInstance().mVTConnectionStarttime.mConnection) {
                return 0;
            }
            if (VTCallUtils.VTTimingMode.VT_TIMING_NONE == VTCallUtils.checkVTTimingMode(call.getLatestConnection().getAddress())) {
                return 0;
            } else if ( VTCallUtils.VTTimingMode.VT_TIMING_DEFAULT == VTCallUtils.checkVTTimingMode(call.getLatestConnection().getAddress())) {
                if (VTInCallScreenFlags.getInstance().mVTConnectionStarttime.mStarttime < 0) {
                    return 0;
                } else {
                    return SystemClock.elapsedRealtime()
                            - VTInCallScreenFlags.getInstance().mVTConnectionStarttime.mStarttime;
                }
            } else {
                // Never happen here, only 2 mode for VTTimingMode
                return 0;
            }
        } else {
            long duration = 0;
            List connections = call.getConnections();
            int count = connections.size();
            Connection c;
            boolean tReminder = false;
            if (count == 1) {
                c = (Connection) connections.get(0);
                duration = c.getDurationMillis();
            } else {
                for (int i = 0; i < count; i++) {
                    c = (Connection) connections.get(i);
                    long t = c.getDurationMillis();
                    if (t > duration) {
                        duration = t;
                    }
                }
            }
    
            if (DBG) log("updateElapsedTime, count=" + count + ", duration=" + duration);
            return duration;
        }
    }

	public void setCallTimeListener(OnTickListener listener){
		mListener = listener;
	}
    private static void log(String msg) {
        Log.d(LOG_TAG, "[CallTime] " + msg);
    }

    private class PeriodicTimerCallback implements Runnable {
        PeriodicTimerCallback() {

        }

        public void run() {
            log("PeriodicTimerCallback's run() is called");
            CallTime.this.sendMessageAtFrontOfQueue(Message.obtain(CallTime.this, CALL_TIME_UPDATE));
        }
    }

    static void setTraceReady() {
        if (sProfileState == PROFILE_STATE_NONE) {
            sProfileState = PROFILE_STATE_READY;
            log("trace ready...");
        } else {
            log("current trace state = " + sProfileState);
        }
    }

    boolean isTraceReady() {
        return sProfileState == PROFILE_STATE_READY;
    }

    boolean isTraceRunning() {
        return sProfileState == PROFILE_STATE_RUNNING;
    }

    void startTrace() {
        if (PROFILE & sProfileState == PROFILE_STATE_READY) {
            // For now, we move away from temp directory in favor of
            // the application's data directory to store the trace
            // information (/data/data/com.android.phone).
            File file = PhoneApp.getInstance().getDir ("phoneTrace", Context.MODE_PRIVATE);
            if (file.exists() == false) {
                file.mkdirs();
            }
            String baseName = file.getPath() + File.separator + "callstate";
            String dataFile = baseName + ".data";
            String keyFile = baseName + ".key";

            file = new File(dataFile);
            if (file.exists() == true) {
                file.delete();
            }

            file = new File(keyFile);
            if (file.exists() == true) {
                file.delete();
            }

            sProfileState = PROFILE_STATE_RUNNING;
            log("startTrace");
            Debug.startMethodTracing(baseName, 8 * 1024 * 1024);
        }
    }

    void stopTrace() {
        if (PROFILE) {
            if (sProfileState == PROFILE_STATE_RUNNING) {
                sProfileState = PROFILE_STATE_NONE;
                log("stopTrace");
                Debug.stopMethodTracing();
            }
        }
    }
    
    // Not used now
    void startReminder(long duration) {
        
        if (mSP == null) return;
        mAlarm.cancel(mReminderPendingIntent);
        mAlarmEnable = true;
        long rem = duration % MINUTE_TO_MS;
        if (rem < INTERVAL_TIME * MILLISECOND_TO_SECOND) {
            duration = INTERVAL_TIME * MILLISECOND_TO_SECOND - rem;
        } else {
            duration = MINUTE_TO_MS - rem + INTERVAL_TIME * MILLISECOND_TO_SECOND;
        }
        
        boolean tReminder = mSP.getBoolean("minute_reminder_key", false);
        if (tReminder) {
            mAlarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + duration, mReminderPendingIntent);
        }
    }
    
    // Not used now
    void stopReminder() {
        mAlarmEnable = false;
        mAlarm.cancel(this.mReminderPendingIntent);
    }
    
    // Not used now
    void updateRminder() {
        if (mCall != null) {
            Call.State state = mCall.getState();
            if (state == Call.State.ACTIVE) {
                final CallNotifier notifier = PhoneApp.getInstance().notifier;
                notifier.onTimeToReminder();
                mAlarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60 * MILLISECOND_TO_SECOND, mReminderPendingIntent);
            }
        }
    }
    
    class CallTimeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (ACTION_REMINDER.equals(intent.getAction())) {
                updateRminder();
            }
        }
        
    }

    class CallTimeHandler extends Handler {
        public CallTimeHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
        }
    }
}
