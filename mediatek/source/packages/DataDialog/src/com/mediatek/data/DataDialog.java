/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.data;

import com.mediatek.data.R;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;

import android.util.Log;


public class DataDialog extends Activity implements OnCancelListener, OnClickListener{

	private final static String TAG = "DATADIALOG";
	
	private static final int DLG_DATADIALOG = 0;

	public static final String ACTION_DATACONNECTION_SETTING_CHANGED = "android.intent.action.DATASETTING_CHANGE";	
	public static final String ACTION_DATACONNECTION_SETTING_CHANGED_DIALOG = "android.intent.action.DATASETTING_CHANGE_DIALOG";

	PackageManager mPm;
	Intent intent;

	@Override
	protected void onCreate(Bundle icicle) {
	         Log.i(TAG,"onCreate()");
		super.onCreate(icicle);
		
		intent = getIntent();
		mPm = getPackageManager();
		if (mPm == null)
			System.exit(-1);

		//showDialog(DLG_ACWF, null);
		showDialog(DLG_DATADIALOG);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG,"onStop");
		cancelExit();
	}
	
    public void onClick(View v) {
	    Log.i(TAG,"onClick");
	}

    public void onCancel(DialogInterface dialog) {
        Log.i(TAG,"onCancel");
        cancelExit();
    }

	private void cancelExit() {
	    Log.i(TAG,"cancelExit");
            Intent broadcast = new Intent(ACTION_DATACONNECTION_SETTING_CHANGED_DIALOG);
	    broadcast.putExtra("user_selection",0);	
            Log.i(TAG,"before sendbroadcast and intent is ACTION_DATACONNECTION_SETTING_CHANGED_DIALOG");
	    this.sendBroadcast(broadcast);
	    Log.i(TAG,"in cancelExit()");

	    System.exit(0);
	}
    public void onPositive(){
        Log.i(TAG,"onPositive()");
        

	Intent broadcast = new Intent(ACTION_DATACONNECTION_SETTING_CHANGED_DIALOG);
	broadcast.putExtra("user_selection",1);	
        Log.i(TAG,"before sendbroadcast and intent is ACTION_DATACONNECTION_SETTING_CHANGED_DIALOG");
	this.sendBroadcast(broadcast);
	Log.i(TAG,"after sendbroadcast");
    }

	public void onNegative(){
            Intent broadcast = new Intent(ACTION_DATACONNECTION_SETTING_CHANGED_DIALOG);
	    broadcast.putExtra("user_selection",0);
            Log.i(TAG,"before sendbroadcast and intent is ACTION_DATACONNECTION_SETTING_CHANGED_DIALOG");
	    this.sendBroadcast(broadcast);
	    Log.i(TAG,"onNegative()");
	}

	@Override
	public Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(this)
						        .setTitle(R.string.dialog_title)
							.setPositiveButton(R.string.dialog_continue, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
								//mPm.setAcwfUserInstall(true);
								onPositive();
								System.exit(0);
								}})
							.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
								//mPm.setAcwfUserInstall(false);
								onNegative();
								System.exit(0);
								}})
                                                        .setMessage( getString(R.string.dialog_content))
							.setOnCancelListener(this)
                                                        .setIcon(android.R.drawable.ic_media_play)
                                                        .create();
	}
}

