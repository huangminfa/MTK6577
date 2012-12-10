/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.android.nfc.addon;

import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.app.Activity;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.util.Log;
import android.nfc.NdefMessage;
import java.util.concurrent.ExecutionException;
import java.lang.Thread;
import java.lang.Exception;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.AlertDialog;
import com.android.nfc.extension.ndef.wkt.Signature;

/**
 * Only for NdefSignaturePromptActivity
 */
interface Promptable {
	void doPrompt(Context context);
}

class NdefSignatureParser implements NfcAddon.INdefSignatureParser, Runnable, Promptable {
	private static final String TAG = "NdefSignatureParser";

	private BasicNfcModeController mPromptController;
	private boolean mIsParsing;
	private Object mCachedTag;
	private NdefMessage[] mCachedNdefMsgs;
	private Handler mCachedHandler;
	private Context mCachedContext;

	// members below are used to pass to inner class
	private Signature.ISignatureParser mParser;
	private boolean mIsConfirmed = false;
	private Activity mPromptActivity;
	private int mTimeout;
    private static final int RES_STR_NOTTRUSTED = 0;
    private static final int RES_STR_YES = 1;
    private static final int RES_STR_NO = 2;
    private String[] mResStrings;

	NdefSignatureParser(String prefLabel, boolean prefDefault, SharedPreferences preference, String[] resStrings) {
		mPromptController = new BasicNfcModeController(prefLabel, prefDefault, preference, 
			new NfcAddon.INfcConfigurationObserver() {
				public void onStateChanged() { } //<-- dummy one
			}
		);
		mIsParsing = false; //<-- just to make it clear
        mResStrings = resStrings;
	}
	
	/**
	 * following functions called from Setting
	 */
	public boolean isPromptEnabled() {
		return mPromptController.isEnabled();
	}
	
	public boolean enablePrompt(SharedPreferences.Editor editor) {
		return mPromptController.enable(editor);
	}
	
	public boolean disablePrompt(SharedPreferences.Editor editor) {
		return mPromptController.disable(editor);
	}

	/**
	 * following functions only called in NfcService's handleMessage
	 */	
	public boolean isParsing() {
		return mIsParsing;
	}
	
	public boolean parse(Object tag, NdefMessage[] ndefMsgs, Handler handler, Context context, int timeout) {
		if (!mPromptController.isEnabled())//<-- if not enabled, we can thus skip the Ndef signature parsing
			return false;
		mIsParsing = true;
		mCachedTag = tag;
		mCachedNdefMsgs = ndefMsgs;
		mCachedHandler = handler;
		mCachedContext = context;
		mTimeout = timeout; 
		new Thread(this).start();
		return true;
	}

	/**
	 * implement the async behavior for NDEF signature parsing.
	 * watchdog timer will be used to make sure about the total parsing time.
	 *
	 * @override
	 */
	public void run() {
		boolean isTrusted = true; 
		mParser = Signature.createParser();
		mParser.setConnectTimeout(3000);
		mParser.setReadTimeout(3000);

		Timer watchdog1 = new Timer();				
		watchdog1.schedule( 
			new TimerTask() { 
				public void run() { 
					Log.d(TAG,"watchdog1 time out, set to non trust");
					mParser.setDisable(true); 
				} 
			}, 
			mTimeout 
		);
		try {
			Signature.Segment[] segments = mParser.parse(mCachedNdefMsgs[0].toByteArray());//<-- if stopParse() called during parsing, throw some exception out
			/**
			 * verify the parsed result, we're strict here:
			 * if one signature is untrusted, we consider the whole NDEF message is untrusted
			 */
			for (Signature.Segment seg : segments) {
				Signature.SignedStatus status = seg.getSignedStatus();
				if (status != null) {								
					if (!status.isValidate() || !status.isTrust()) {
						isTrusted = false;
						break;
					}
				}
			}			
		} catch (Exception e) {
			isTrusted = false;
			Log.d(TAG, e.toString());
		} 
		watchdog1.cancel();		

		if (!isTrusted) {
			Log.d(TAG, "NDEF Signature Validation fail!");
			sendMessage(MSG_NDEF_PROMPT_AFTER_PARSING_SIGNATURE);
		} else {
			Log.d(TAG, "NDEF Signature Validation pass!");
			sendMessage(MSG_NDEF_DISPATCH_AFTER_PARSING_SIGNATURE);
		}
	}
	
	public void prompt(int timeout) {
		//if not verified, pop dialog for user, also start a timer for user 
		Log.d(TAG, "prompt user when NDEF Signature Validation fail...");
		mTimeout = timeout;
		mIsConfirmed = false;		
		/**
		 * Since this call is from non-activity class (NfcService),
		 * we cannot build our dialog here because the dialog needs to be passed with a context obtained from activity.
		 * We thus create a new fowarding activity NdefSignaturePromptActivity, and build our dialog from that activity.
		 */
		Intent i = new Intent(mCachedContext, NdefSignaturePromptActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		mCachedContext.startActivity(i);
	}

	/**
	 * Called from NdefSignaturePromptActivity, to realy pop-up a confirmation dialog.
	 * Watchdog timer is also used here to make sure the confirmation dialog is closed.
	 * The passing "context" must be a activity context.
	 */
	public void doPrompt(Context context) {
		Log.d(TAG,"doPrompt()...");
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		mPromptActivity = (Activity)context;
		final Timer watchdog2 = new Timer();
		builder.setMessage(new StringBuilder(mResStrings[RES_STR_NOTTRUSTED]).toString())
			.setCancelable(false)
			.setPositiveButton(mResStrings[RES_STR_YES], new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG,"User confirm YES");
					mIsConfirmed = true;
					sendMessage(MSG_NDEF_DISPATCH_AFTER_PARSING_SIGNATURE);
					mPromptActivity.finish();
					watchdog2.cancel();
				}
			})
			.setNegativeButton(mResStrings[RES_STR_NO], new DialogInterface.OnClickListener() {						
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG,"User confirm NO");
					mIsConfirmed = true;
					sendMessage(MSG_NDEF_DISCONNECT_AFTER_PROMPT);
					mPromptActivity.finish();
					watchdog2.cancel();
				}
			});
		final AlertDialog dialog = builder.create();
		dialog.show();
		Log.d(TAG,"confirmation dialog is created and shown...");
		watchdog2.schedule(
			new TimerTask() {
				public void run() {
					Log.d(TAG,"watchdog2 time out");
					if (!mIsConfirmed) {
						dialog.dismiss();
						mPromptActivity.finish();
						sendMessage(MSG_NDEF_DISCONNECT_AFTER_PROMPT);
					}
					cancel();
				}
			},
			mTimeout
		);
	}
	
	private void sendMessage(int what) {
		Message msg = mCachedHandler.obtainMessage();										
		msg.obj = NdefSignatureParser.this;
		msg.what = what;
		mCachedHandler.sendMessage(msg);	
	}
	
	public Object getCachedTag() {
		return mCachedTag;
	}
	
	public NdefMessage[] getCachedNdefMessages() {
		return mCachedNdefMsgs;
	}

	public void clearCachedObjects() {
		mIsParsing = false; 		
		mCachedTag = null;
		mCachedNdefMsgs = null;		
		mCachedHandler = null;
		mCachedContext = null;
		mParser = null;
		mPromptActivity = null;
	}
}
