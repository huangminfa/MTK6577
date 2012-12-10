package com.android.settings;

import com.android.settings.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class RestoreRotationReceiver extends BroadcastReceiver {

	public static boolean bRestoreRetore = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction(); 
		Log.v("RestoreRotationReceiver_IPO", action);
		if(action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals("android.intent.action.ACTION_BOOT_IPO")){
		  bRestoreRetore = Settings.System.getInt(context
					.getContentResolver(),
					Settings.System.ACCELEROMETER_ROTATION_RESTORE, 0) != 0;
			if (bRestoreRetore) {
				Settings.System.putInt(context.getContentResolver(),
						Settings.System.ACCELEROMETER_ROTATION, 1);
				Settings.System.putInt(context.getContentResolver(),
						Settings.System.ACCELEROMETER_ROTATION_RESTORE, 0);
			}
		}
	}
}
