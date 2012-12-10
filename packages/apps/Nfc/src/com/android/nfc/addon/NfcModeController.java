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
import android.os.Vibrator;
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

class DefaultModeController implements NfcAddon.INfcModeController {
	public boolean isDefault(){ return true; }
	public boolean isEnabled(){ return true; }
	public boolean enable(SharedPreferences.Editor editor){ return true; }
	public boolean disable(SharedPreferences.Editor editor){ return true; }
	public int getEnabledWhen(){ return NfcAddon.INfcModeController.ENABLED_WHEN_SCREEN_UNLOCK; }
	public boolean setEnabledWhen(SharedPreferences.Editor editor, int enabledWhen){ return true; }
}

class BasicNfcModeController implements NfcAddon.INfcModeController {
	protected String mPreferenceLabel;
	protected boolean mIsEnabled;
	protected NfcAddon.INfcConfigurationObserver mObserver;
	
	public BasicNfcModeController(String prefLabel, boolean prefDefault, SharedPreferences preference, 
						NfcAddon.INfcConfigurationObserver observer) {
		mObserver = observer;
		mPreferenceLabel = prefLabel;
		mIsEnabled = preference.getBoolean(prefLabel, prefDefault);
	}

	public boolean isDefault() {
		return false;
	}
	
	public boolean isEnabled() {
		return mIsEnabled;
	}

	public boolean enable(SharedPreferences.Editor editor) {
		if (mIsEnabled) {
			return true;
		}
		editor.putBoolean(mPreferenceLabel, true);
		editor.apply();
		mIsEnabled = true;
		mObserver.onStateChanged();
		return true;
	}

	public boolean disable(SharedPreferences.Editor editor) {
		if (!mIsEnabled) {
			return true;
		}
		editor.putBoolean(mPreferenceLabel, false);
		editor.apply();
		mIsEnabled = false;
		mObserver.onStateChanged();
		return true;
	}

	public int getEnabledWhen(){ 
		return NfcAddon.INfcModeController.ENABLED_WHEN_SCREEN_UNLOCK; 
	}

	public boolean setEnabledWhen(SharedPreferences.Editor editor, int enabledWhen){ 
		return true; 
	}
}

class CardModeController extends BasicNfcModeController {
	private final static String mCardModeEnabledWhenLabel = "CardModeEnabledWhen";
	private int mEnabledWhen;
	private NfcAddon.INfcConfigurationObserver mConfigureChangeObserver;
	
	public CardModeController(	String prefLabel, 	
								boolean prefDefault, 
								SharedPreferences preference, 
								NfcAddon.INfcConfigurationObserver observer, 
								NfcAddon.INfcConfigurationObserver configObserver) {
		super(prefLabel, prefDefault, preference, observer);
		mEnabledWhen = preference.getInt(mCardModeEnabledWhenLabel, ENABLED_WHEN_ALWAYS);
		mConfigureChangeObserver = configObserver;
	}
	
	public boolean enable(SharedPreferences.Editor editor) {
		editor.putBoolean(mPreferenceLabel, true);
		editor.apply();
		mIsEnabled = true;
		mObserver.onStateChanged();
		return true;
	}

	public boolean disable(SharedPreferences.Editor editor) {
		editor.putBoolean(mPreferenceLabel, false);
		editor.apply();
		mIsEnabled = false;
		mObserver.onStateChanged();
		return true;
	}

	public int getEnabledWhen() {
		return mEnabledWhen;
	}

	public boolean setEnabledWhen(SharedPreferences.Editor editor, int enabledWhen) {
		if (enabledWhen < NfcAddon.INfcModeController.ENABLED_WHEN_ALWAYS || 
			enabledWhen > NfcAddon.INfcModeController.ENABLED_WHEN_SCREEN_UNLOCK) {
			Log.d("CardModeController", "[incorrect set enable when value]" + enabledWhen); 
			return false;
			}
		editor.putInt(mCardModeEnabledWhenLabel, enabledWhen);
		editor.apply();
		mEnabledWhen = enabledWhen;
		mConfigureChangeObserver.onStateChanged();
		return true;
	}
}

class VibratorWrapper implements SharedPreferences.OnSharedPreferenceChangeListener, NfcAddon.IVibrator {
	static final private String TAG = "VibratorWrapper";
	boolean mIsEnabled;
	final Vibrator mVibrator;

	public void onSharedPreferenceChanged (SharedPreferences preference, String key) {
		mIsEnabled = preference.getBoolean("nfc_vibration_enabled", true);
		if (key.equals("nfc_vibration_enabled")) {
			Log.d(TAG,"nfc_vibration_enabled changed to " + mIsEnabled);
		}
	}
	
	public VibratorWrapper(Context context, String ID) {
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		SharedPreferences prefs = context.getSharedPreferences(ID, Context.MODE_PRIVATE);
		mIsEnabled = prefs.getBoolean("nfc_vibration_enabled", true);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	public void vibrate(long[] pattern, int repeat) {
		synchronized(this) {
			if (mIsEnabled) {
				mVibrator.vibrate(pattern, repeat);
			} 
		}
	}
}

