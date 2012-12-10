package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class SmartSwitchReceiver extends BroadcastReceiver {
	
	private static final String TRANSACTION_START = "com.android.mms.transaction.START";
	private static final String TRANSACTION_STOP = "com.android.mms.transaction.STOP";
	/**
	 * MMS_TRANSACTION: 
	 * 		1 if mms transaction started
	 * 		0 if mms transaction stoped
	 */
	private static final String MMS_TRANSACTION = "mms.transaction";
	private static boolean mMmsTransaction = false;
//	private static final String TAG = "SmartSwitchReceiver";
	private static final String SS_TAG = "SmartSwitchReceiver";
	

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction(); 
		if(action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals("android.intent.action.ACTION_BOOT_IPO")){
		    Log.d(SS_TAG, "Boot completed, clear database, set MMS_TRANSACTION as 0");
		    Settings.System.putInt(context.getContentResolver(), MMS_TRANSACTION, 0);
		}else if(action.equals(TRANSACTION_START)) {
        	Log.d(SS_TAG, "receiver: TRANSACTION_START in SmartSwitchReceiver");
        	if(!mMmsTransaction){
            	Settings.System.putInt(context.getContentResolver(), MMS_TRANSACTION, 1);
            	mMmsTransaction = true;
            	Log.d(SS_TAG, "set MMS_TRANSACTION as 1; set flag as true");
        	}
     	} else if(action.equals(TRANSACTION_STOP)) {
        	Log.d(SS_TAG, "receiver: TRANSACTION_STOP in SmartSwitchReceiver");
        	if(mMmsTransaction){
            	Settings.System.putInt(context.getContentResolver(), MMS_TRANSACTION, 0);
            	mMmsTransaction = false;
            	Log.d(SS_TAG, "set MMS_TRANSACTION as 0; set flag as false");
        	}
     	}
	}

}
