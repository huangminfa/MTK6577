package com.android.InternationalCardTester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
 
public class BootServiceReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent i = new Intent();
			i.setAction("com.android.InternationalCardTester.GlobalModeService");
			context.startService(i);
		}
	}
}