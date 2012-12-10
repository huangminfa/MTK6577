package com.android.settings.schpwronoff;

import com.android.settings.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.Service;
import android.os.IBinder;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.mediatek.xlog.Xlog;

public class SchPwrOnOffService extends Service {
	private static final String TAG="SchPwrOnOffService";
    public static boolean isInCall = false;
    private TelephonyManager mTelephonyManager;

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String ignored) {
            // The user might already be in a call when the alarm fires. When
            // we register onCallStateChanged, we get the initial in-call state
            // which kills the alarm. Check against the initial call state so
            // we don't kill the alarm during a call.
            Xlog.d(TAG,"SchPwrOnOffService - call_state = " + state);
            if (state != TelephonyManager.CALL_STATE_IDLE)
            {
                isInCall = true;
                Xlog.d(TAG,"SchPwrOnOffService - isInCall = true");
            }
            else
            {
                isInCall = false;
                Xlog.d(TAG,"SchPwrOnOffService - isInCall = false");
            }
        }
    };
    
    @Override
    public void onCreate() {
        super.onCreate();
        // REGISTER RECEIVER THAT HANDLES SCREEN ON AND SCREEN OFF LOGIC
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        Xlog.d(TAG,"SchPwrOnOffService - onCreate");
        mTelephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(
                mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        if (mTelephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE)
            {
                isInCall = true;
                Xlog.d(TAG,"SchPwrOnOffService - isInCall = true");
            }
            else
            {
                isInCall = false;
                Xlog.d(TAG,"SchPwrOnOffService - isInCall = false");
            }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
