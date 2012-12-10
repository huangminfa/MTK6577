package com.android.settings.schpwronoff;

import com.android.settings.R;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import com.mediatek.xlog.Xlog;

public class AlarmInitReceiver extends BroadcastReceiver {
	private static final String TAG = "AlarmInitReceiver";
    /**
     * Sets alarm on ACTION_BOOT_COMPLETED.  Resets alarm on
     * TIME_SET, TIMEZONE_CHANGED
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Xlog.d(TAG,"AlarmInitReceiver" + action);

        if (context.getContentResolver() == null) {
            Xlog.e(TAG,"AlarmInitReceiver: FAILURE unable to get content resolver.  Alarms inactive.");
            return;
        }
        Intent mIntent=new Intent(context,AlarmReceiverService.class);
        mIntent.setAction(action);
        context.startService(mIntent);
    }
}
