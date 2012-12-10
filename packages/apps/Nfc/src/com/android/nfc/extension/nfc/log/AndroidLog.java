package com.android.nfc.extension.nfc.log;

import com.android.nfc.extension.nfc.log.Logger.ILog;

import android.util.Log;

/**
 * @hide
 */
public class AndroidLog implements ILog 
{
	private static final String PROJECT_NAME = "NDEFExtension";
	private static final boolean isDebug = true;
	
	private StringBuilder sb = new StringBuilder();
	
	/**
	 * Create message
	 * @param tag
	 * @param msg
	 * @return
	 */
	private synchronized String createLog(String tag, String ...msg)
	{
		sb.setLength(0);
		sb.append(" [").append(tag).append("]");
		for(int i = 0; i < msg.length; i++){
			sb.append(msg[i]);
		}
		return sb.toString();
	}
	
	public void d(String TAG, String ...msg) {
		if(isDebug){
			Log.d(PROJECT_NAME, createLog(TAG, msg));
		}
	}

	public void i(String TAG, String ...msg) {
		if(isDebug){
			Log.i(PROJECT_NAME, createLog(TAG, msg));
		}
	}

	public void e(String TAG, String ...msg) {
		if(isDebug){
			Log.e(PROJECT_NAME, createLog(TAG, msg));
		}
	}

}
