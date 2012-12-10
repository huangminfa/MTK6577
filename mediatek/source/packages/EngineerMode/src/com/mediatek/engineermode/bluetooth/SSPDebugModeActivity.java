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

package com.mediatek.engineermode.bluetooth;

import com.mediatek.engineermode.EngineerMode;
import com.mediatek.engineermode.R;
import com.mediatek.xlog.Xlog;

import com.mediatek.engineermode.bluetooth.BtTest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import java.io.IOException;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;


public class SSPDebugModeActivity extends Activity {

	private static final String TAG = "SSPDebugModeActivity";
	private CheckBox mIsChecked;
	
	private Handler mHandler = null;

	private static final int DIALOG_OPEN_BLUETOOTH = 100;
	private static final int DIALOG_OPEN_BLUETOOTH_FINISHED = 0x10;

	BluetoothAdapter mAdapter = null;

	private boolean mbIsBTOn = false;
	private boolean mbIsDialogShowed = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.ssp_debug_mode);

		CharSequence str1 = getString(R.string.SSPDebugMode);
		TextView tv = (TextView) findViewById(R.id.SSPDebugModetxv);
		if (null != tv) {
			tv.setText(Html.fromHtml(str1.toString()));
		} else {
			Xlog.w(TAG, "findViewById(R.id.SSPDebugModetxv) failed");
		}

		mIsChecked = (CheckBox) findViewById(R.id.SSPDebugModeCb);
		if (null != mIsChecked) {
			mIsChecked.setOnCheckedChangeListener(mCheckedListener);
		} else {
			Xlog.w(TAG, "findViewById(R.id.SSPDebugModeCb) failed");
		}
		mIsChecked.setEnabled(false);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == DIALOG_OPEN_BLUETOOTH_FINISHED) {
					if (mAdapter.getState() == mAdapter.STATE_ON) {
						mIsChecked.setEnabled(true);
						mIsChecked.setChecked(mAdapter.getSSPDebugMode());
						mbIsBTOn = true;
					}
					if (mbIsDialogShowed) {
						removeDialog(DIALOG_OPEN_BLUETOOTH);
					}
				}
			}
		};
		mAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	protected void onStart() {
		Xlog.v(TAG, "-->onStart");
		super.onStart();
		/*
		 * mAdapter = BluetoothAdapter.getDefaultAdapter(); if (mAdapter !=
		 * null) { if (mAdapter.getState() != mAdapter.STATE_ON){ new
		 * AlertDialog
		 * .Builder(this).setCancelable(false).setTitle("Error").setMessage(
		 * "Please turn on bluetooth first").setPositiveButton("OK", new
		 * DialogInterface.OnClickListener() {
		 * 
		 * public void onClick(DialogInterface dialog, int which) { // TODO
		 * Auto-generated method stub finish(); } }).show();
		 * 
		 * } } mIsChecked.setEnabled(true);
		 * mIsChecked.setChecked(mAdapter.getSSPDebugMode());
		 */
	}

	protected void onResume() {
		Xlog.v(TAG, "-->onResume");
		super.onResume();
		if (!mbIsBTOn) {
			showDialog(DIALOG_OPEN_BLUETOOTH);
			mbIsDialogShowed = true;
			new Thread() {
				public void run() {
					if (mAdapter.getState() != mAdapter.STATE_ON) {
						// Open Bluetooth through mAdapter
						mAdapter.enable();
						while (mAdapter.getState() == mAdapter.STATE_TURNING_ON) {
							/*
							 * if(mAdapter.getState() !=
							 * mAdapter.STATE_TURNING_ON) { }
							 */
							Xlog.i(TAG, "Bluetooth turning on ...");
							try {
								sleep(300);
							} catch (InterruptedException e) {
								Xlog
										.i(TAG,
												"sleep(300) ..InterruptedException");
							}
						}
					}
					mHandler.sendEmptyMessage(DIALOG_OPEN_BLUETOOTH_FINISHED);
				}
			}.start();

		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Xlog.v(TAG, "-->onCreateDialog");
		if (id == DIALOG_OPEN_BLUETOOTH) {
			ProgressDialog dialog = new ProgressDialog(
					SSPDebugModeActivity.this);

			// It would be more efficient to reuse this dialog by moving
			// this setMessage() into onPreparedDialog() and NOT use
			// removeDialog(). However, this is not possible since the
			// message is rendered only 2 times in the ProgressDialog -
			// after show() and before onCreate.
			if (dialog != null) {
				dialog.setTitle("Progress");
				dialog.setMessage("Please wait for Bluetooth to turn on ...");
				dialog.setCancelable(false);
				dialog.setIndeterminate(true);
				dialog.show();
				Xlog.v(TAG, "new ProgressDialog succeed");
			} else {
				Xlog.i(TAG, "new ProgressDialog failed");
			}

			return dialog;
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		// mIsChecked.setEnabled(false);
	}

	public void onStop() {
		Xlog.v(TAG, "-->onStop");
		super.onStop();
		// finish();
	}

	public void onDestroy() {
		Xlog.v(TAG, "-->onDestroy");
		if (mbIsBTOn) {
			if(!mIsChecked.isChecked()){
	        	Xlog.v(TAG, "setSSPDebugMode() back BTList");
				mAdapter.disable();
				while (mAdapter.getState() == mAdapter.STATE_TURNING_OFF) {
					// Log.d(TAG, "Bluetooth turning off ...");
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						Xlog.i(TAG, "sleep(200) ..InterruptedException");
					}
				}
				mbIsBTOn = false;
			}
		}
		super.onDestroy();
	}

	private CheckBox.OnCheckedChangeListener mCheckedListener = new CheckBox.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean Checked) {
			Xlog.i(TAG, "-->onCheckedChanged");
			if (!mAdapter.setSSPDebugMode(Checked)) {
				mIsChecked.setEnabled(false);
				Xlog.i(TAG, "setSSPDebugMode() failed");
			}
        	Xlog.v(TAG, "setSSPDebugMode() isChecked--"+mIsChecked.isChecked());
    		if (mbIsBTOn) {
	    		if(mIsChecked.isChecked()){
		        	Xlog.v(TAG, "setSSPDebugMode() back EngineerMode");
		        	new AlertDialog.Builder(SSPDebugModeActivity.this).setCancelable(false).setTitle(
					"Success").setMessage("SSP Debug mode is open, exit EngineerMode Bluetooth test.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
							        try {
							            Intent intent = new Intent();
							            intent.setClassName(SSPDebugModeActivity.this, "com.mediatek.engineermode.EngineerMode");
							            SSPDebugModeActivity.this.startActivity(intent);
							        } catch (Exception e) {
							        	Xlog.i(TAG, "setSSPDebugMode() back EngineerMode failed!!!");
							        }
								}
							}).show();
	    		}
    		}
		}
		
	};
}
