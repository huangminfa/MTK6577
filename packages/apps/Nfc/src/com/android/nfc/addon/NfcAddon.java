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

public class NfcAddon {

	public interface INfcConfigurationObserver {
		public void onStateChanged();
	}

	public interface INfcModeController {
        public static final int DISABLED = 0;
		public static final int ENABLED_WHEN_ALWAYS = 1;
		public static final int ENABLED_WHEN_SCREEN_ON = 2;
		public static final int ENABLED_WHEN_SCREEN_UNLOCK = 3;	
		public boolean isDefault();
		public boolean isEnabled();
		public boolean enable(SharedPreferences.Editor editor);
		public boolean disable(SharedPreferences.Editor editor);
		public int getEnabledWhen();
		public boolean setEnabledWhen(SharedPreferences.Editor editor, int enabledWhen);
	}

	public interface IVibrator {
		public void vibrate(long[] pattern, int repeat);
	}

	public interface INfcStatusBar {
		public void updateStatusBar(Context context, boolean isNfcEnabled, boolean isNdefPushEnabled, int screenState);
	}

	public interface INdefSignatureParser {
		public static final int MSG_NDEF_DISPATCH_AFTER_PARSING_SIGNATURE = 777;
		public static final int MSG_NDEF_PROMPT_AFTER_PARSING_SIGNATURE = 778;
		public static final int MSG_NDEF_DISCONNECT_AFTER_PROMPT = 779;

		public boolean isPromptEnabled();
		public boolean enablePrompt(SharedPreferences.Editor editor);
		public boolean disablePrompt(SharedPreferences.Editor editor);

		/**
		 * for NFC service to query whether we're in parsing status now
		 */
		public boolean isParsing();

		/**
		 * [description]
		 * ack the interface to parse and verify the signature, and this is a async behavior,
		 * signature parser instance will create a active thread to do the parsing
		 *
		 * [return]
		 * true : parsing thread is started
		 * false : parsing thread isn't invoked, probably because the prompt is disabled,
		 * so we aren't required to do parsing, we can do normal stuffs and no more async behaviors.
		 * 
		 * [async]
		 * if the NDEF is trusted after parsing, this signature parser instance will use the passed
		 * handler to send a MSG_NDEF_DISPATCH_AFTER_PARSING_SIGNATURE.
		 * else the signature parser instance will send a MSG_NDEF_PROMPT_AFTER_PARSING_SIGNATURE
		 *
		 */
		public boolean parse(Object tag, NdefMessage[] ndefMsgs, Handler handler, Context context, int timeout);

		/**
		 * [description]
		 * if a NDEF is considered to be untrust, signature parser instance will use cached 
		 * handler to send a MSG_NDEF_PROMPT_AFTER_PARSING_SIGNATURE.
		 * this prompt() function should be invoked to corresponding case in the handler's handleMessage()
		 * Notice we seperate the prompt() from parse() because prompt() will trigger UI operation,
		 * so we cannot to it in parse(), since those actions in parse() is executed in a new thread.
		 * this function will pop-up a confirmation dialog for the user to choose whether accepting this
		 * NDEF message or not with a watchdog timer set to 5000 ms.
		 * if the user doesn't response in 5000ms, this NDEF message will be considered as reject.
		 * depending on the accept or reject, different message will be sent back to the cached handler,
		 * check the async section.
		 *
		 * [return]
		 * none
		 * 
		 * [async]
		 * accept: MSG_NDEF_DISPATCH_AFTER_PARSING_SIGNATURE will be sent to the cached handler
		 * reject: MSG_NDEF_DISCONNECT_AFTER_PROMPT will be sent to the cached handler
		 */
		public void prompt(int timeout);

		/**
		 * [description]
		 * allow the client to retrieve the cached tag
		 */
		public Object getCachedTag();

		/**
		 * [description]
		 * allow the client to retrieve the cached Ndef message for 
		 */
		public NdefMessage[] getCachedNdefMessages();

		/**
		 * [description]
		 * allow the client to clear all cached object in signature parser instance.
		 * this method MUST be called in the message handle section for 
		 * MSG_NDEF_DISCONNECT_AFTER_PROMPT and MSG_NDEF_DISPATCH_AFTER_PARSING_SIGNATURE.
		 * this is not only to clear the object reference but also turn the internal "isParsing"
		 * flag to false.
		 */
		public void clearCachedObjects();	
	}

	public static INfcModeController createDefaultModeController() {
		return new DefaultModeController();
	}

	public static INfcModeController createBasicNfcModeController(String prefLabel, boolean prefDefault, SharedPreferences preference, 
							INfcConfigurationObserver observer) {
		return new BasicNfcModeController(prefLabel, prefDefault, preference, observer);
	}

	public static INfcModeController createCardModeController(String prefLabel, boolean prefDefault, SharedPreferences preference, 
							INfcConfigurationObserver observer, INfcConfigurationObserver configObserver) {
		return new CardModeController(prefLabel, prefDefault, preference, observer, configObserver);
	}

	public static INfcStatusBar createDefaultNfcStatusBar() {
		return new DefaultNfcStatusBar();
	}

	public static INfcStatusBar createBasicNfcStatusBar(
		int icon,
        int resCustomNotif,
        int resIdImage,
        int resMainIcon,
        int resIdTitle,
        int resIdText,
        String[] resStrings,
		INfcModeController tagRwController, 
		INfcModeController p2pRecvController, 
        INfcModeController cardEmuController) {
		return new BasicNfcStatusBar(icon, resCustomNotif, resIdImage,
                   resMainIcon, resIdTitle, resIdText, resStrings, 
                   tagRwController, p2pRecvController, cardEmuController);
	}

	public static IVibrator createVibratorWrapper(Context context, String ID) {
		return new VibratorWrapper(context, ID);
	}

	/**
	  * currently use dirty way, make a static instance to pass to activity
	  */
	static Promptable mNdefParser;
	public static INdefSignatureParser createNdefSignatureParser(String prefLabel, boolean prefDefault, SharedPreferences preference, String[] resStrings) {
		NdefSignatureParser parser = new NdefSignatureParser(prefLabel, prefDefault, preference, resStrings);
		mNdefParser = parser;
		return parser;
	}
}

