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
import android.widget.RemoteViews;

class DefaultNfcStatusBar implements NfcAddon.INfcStatusBar {
	public void updateStatusBar(Context context, boolean isNfcEnabled, boolean isNdefPushEnabled, int screenState){ /*dummy*/ }
}

class BasicNfcStatusBar implements NfcAddon.INfcStatusBar {	
	private final static String TAG = "BasicNfcStatusBar";
	final static int mId = 1;
	private int mIcon;
    private int mResCustomNotif;
    private int mResIdImage; 
    private int mResMainIcon;
    private int mResIdTitle;
    private int mResIdText;

    final static int STRING_BEAM = 0;
    final static int STRING_CARDEMU = 1;
    final static int STRING_TAGRW = 2;
    final static int STRING_NONE = 3;
    final static int STRING_ENABLED = 4;
    final static int STRING_NFCACTIVE = 5;
    private String[] mResStrings;

	private NfcAddon.INfcModeController mTagRwController;
	private NfcAddon.INfcModeController mP2pRecvController;
	private NfcAddon.INfcModeController mCardEmuController;
	
	public BasicNfcStatusBar(	int icon,
                                int resCustomNotif,
                                int resIdImage,
                                int resMainIcon,
                                int resIdTitle,
                                int resIdText,
                                String[] resStrings,
								NfcAddon.INfcModeController tagRwController, 
								NfcAddon.INfcModeController p2pRecvController, 
								NfcAddon.INfcModeController cardEmuController) {
		mIcon = icon;
        mResCustomNotif = resCustomNotif;
        mResIdImage = resIdImage; 
        mResMainIcon = resMainIcon;
        mResIdTitle = resIdTitle;
        mResIdText = resIdText;
        mResStrings = resStrings;
		mTagRwController = tagRwController;
		mP2pRecvController = p2pRecvController;
		mCardEmuController = cardEmuController;
	}
	
	public void updateStatusBar(Context context, boolean isNfcEnabled, boolean isNdefPushEnabled, int screenState) {
		NotificationManager nm = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
		if (isNfcEnabled) {
			Log.d(TAG, "updateStatusBar: NFC is on, notify");
			boolean tagRw = mTagRwController.isEnabled();
			boolean ndefPush = isNdefPushEnabled;
			boolean ndefRecv = mP2pRecvController.isEnabled();
			boolean cardMode = mCardEmuController.isEnabled();
			if (screenState < mTagRwController.getEnabledWhen()) {
				tagRw = false;
			}
			if (screenState < mP2pRecvController.getEnabledWhen()) {
				ndefPush = false;
				ndefRecv = false;
			}
			if (screenState < mCardEmuController.getEnabledWhen()) {
				cardMode = false;
			}				
            String notifMsg = "";
            boolean isFirst = true;
            boolean nothingEnabled = true;
            if (ndefPush) {
                notifMsg += mResStrings[STRING_BEAM];//"Android Beam";   
                isFirst = false;
                nothingEnabled = false;
            }
            if (cardMode) {
                if (isFirst)
                    notifMsg += mResStrings[STRING_CARDEMU];//"Card emulation";
                else
                    notifMsg += ", " + mResStrings[STRING_CARDEMU];//Card emulation";
                isFirst = false;
                nothingEnabled = false;
            }
            if (tagRw) {
                if (isFirst) 
                    notifMsg += mResStrings[STRING_TAGRW];//"Tag reading & writing";
                else 
                    notifMsg += ", " + mResStrings[STRING_TAGRW];//Tag reading & writing";
                isFirst = false;
                nothingEnabled = false;
            }
            if (nothingEnabled) 
                notifMsg = mResStrings[STRING_NONE];//"None";
            else 
                notifMsg += ": " + mResStrings[STRING_ENABLED];//enabled";
			Notification.Builder nb = new Notification.Builder(context);
			nb.setWhen(System.currentTimeMillis());
			nb.setSmallIcon(mIcon);
			//nb.setContentTitle("NFC Status Summary");
			//nb.setContentText("[RW] " + tagRw + ". [P2P Push/Recv] " + ndefPush + "/" + ndefRecv + ". [Card] " + cardMode + ".");			
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent("android.settings.NFC_SETTINGS"), 0);
			nb.setContentIntent(pendingIntent);
			Notification notif = nb.getNotification();
			notif.flags |= Notification.FLAG_NO_CLEAR;    
            //add for customized notification bar
            RemoteViews contentView = new RemoteViews(context.getPackageName(), mResCustomNotif);
            contentView.setImageViewResource(mResIdImage, mResMainIcon);
            contentView.setTextViewText(mResIdTitle, mResStrings[STRING_NFCACTIVE]);//"NFC active");
            contentView.setTextViewText(mResIdText, notifMsg );
            notif.contentView = contentView;
			nm.notify(mId, notif);
		} else {
			Log.d(TAG, "updateStatusBar: NFC is off, cancel");
			nm.cancel(mId);
		}
	}	
}
