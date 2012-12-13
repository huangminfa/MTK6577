package com.baoxue.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PackageReceiver extends BroadcastReceiver {

	final static String ACTION_RUN_TASK = "baoxue.action.RUN_TASK";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
			String addedPackageName = intent.getData().getSchemeSpecificPart();
			if ("com.baoxue.task".equals(addedPackageName)) {
				Intent i = new Intent(ACTION_RUN_TASK);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.sendBroadcast(i);
			}

		}

	}
}
