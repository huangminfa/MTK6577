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

import com.mediatek.engineermode.R;

import com.mediatek.engineermode.bluetooth.BtTest;
import com.mediatek.xlog.Xlog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;
import com.mediatek.engineermode.wifi.EMWifi;
import android.text.TextUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import android.os.SystemClock;
import android.os.Message;
import android.os.Handler;
import android.os.Looper;
import android.os.HandlerThread;
import android.content.Context;
import android.content.DialogInterface;
import android.bluetooth.BluetoothAdapter;
import android.app.Dialog;
import android.app.AlertDialog;
import android.util.Log;
import android.app.Dialog;

public class BLE_WhiteList extends Activity implements OnClickListener {

	private BluetoothAdapter mAdapter;

	private Button mBtnAdd = null;
	private Button mBtnRemove = null;
	private Button mBtnClear = null;
	private Button mBtnHCIReset = null;

	private Spinner mSpnAddressType = null;

	private EditText mEditMACAddress = null;

	// adapter need by Spinner
	private ArrayAdapter<String> mSpnAddressTypeAdatper;

	// value recorder
	private byte mAddressTypeValue = 0x00;

	private final String TAG = "BLEWhiteList";

	private BtTest mBT;
	// UI thread's handler
	private Handler mainHandler = null;
	// WorkThraed and its Handler
	private WorkThread gtWorkThread = null;
	private Handler workThreadHandler = null;

	// BtTest object init and start test flag
	private boolean mbIsInit = false;
	private boolean mbIsIniting = false;
	// this flag is no needed any more ,since we take all the HCI cmd related
	// operation in separate thread, but we can just leave it here
	// private boolean mbIsTestStared = false;//this flag is not necessary in
	// this module

	// Message ID
	private static final int ADDTOWHITELIST = 1;
	private static final int REMOVEFROMWHITELIST = 2;
	private static final int CLEARWHITELIST = 3;
	private static final int HCIRESET = 4;

	private static final int ACTIVITYEXIT = 5;

	@Override
	protected void onCreate(Bundle onSavedInstanceState) {
		Xlog.v(TAG, "-->onCreate");
		super.onCreate(onSavedInstanceState);
		setContentView(R.layout.ble_normal_mode_whitelist);

		mBtnAdd = (Button) findViewById(R.id.BLEAddToWhiteList);
		mBtnRemove = (Button) findViewById(R.id.BLERemoveWhiteList);
		mBtnClear = (Button) findViewById(R.id.BLEClearWhiteList);
		mBtnHCIReset = (Button) findViewById(R.id.BLEHCIReset);

		if (mBtnAdd == null || mBtnRemove == null || mBtnClear == null
				|| mBtnHCIReset == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

		mBtnAdd.setOnClickListener(this);
		mBtnRemove.setOnClickListener(this);
		mBtnClear.setOnClickListener(this);
		mBtnHCIReset.setOnClickListener(this);

		mEditMACAddress = (EditText) findViewById(R.id.BLEMACAddress);
		mSpnAddressType = (Spinner) findViewById(R.id.BLEAddressTypeSpinner);

		if (mEditMACAddress == null || mSpnAddressType == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

		mEditMACAddress.setText("AABBCCDDEEFF");
		// address type spinner content fill and event handler
		mSpnAddressTypeAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnAddressTypeAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnAddressTypeAdatper.add("Public");
		mSpnAddressTypeAdatper.add("Random");
		if (null != mSpnAddressType) {
			mSpnAddressType.setAdapter(mSpnAddressTypeAdatper);
			mSpnAddressType
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							Xlog.v(TAG, "item id = " + arg2);
							mAddressTypeValue = (byte) arg2;
						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			Xlog.i(TAG, "findViewById(R.id.BLEScanTypeSpinner) failed");
		}
		if (gtWorkThread == null) {
			gtWorkThread = new WorkThread();
		}
		if (gtWorkThread != null) {
			gtWorkThread.start();
		} else {
			Xlog.i(TAG, "create WorkThread failed");
		}

	}

	@Override
	protected void onStart() {
		Xlog.v(TAG, "-->onStart");
		super.onStart();
		if (mAdapter == null) {
			mAdapter = BluetoothAdapter.getDefaultAdapter();
		}
		if (mAdapter != null) {
			if (mAdapter.getState() != mAdapter.STATE_OFF) {
				new AlertDialog.Builder(this).setCancelable(false).setTitle(
						"Error").setMessage("Please turn off bluetooth first")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										finish();
									}
								}).show();
			}
		} else {
			new AlertDialog.Builder(this).setTitle("Error").setMessage(
					"Can't find any bluetooth device").setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							finish();
						}
					}).show();
		}
	}

	@Override
	protected void onResume() {
		Xlog.v(TAG, "-->onResume");
		super.onResume();

	}

	@Override
	protected void onPause() {
		Xlog.v(TAG, "-->onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Xlog.v(TAG, "-->onStop");
		super.onStop();
		finish();
	}

	@Override
	protected void onDestroy() {
		Xlog.v(TAG, "-->onDestroy");
		super.onDestroy();
		workThreadHandler.sendEmptyMessage(ACTIVITYEXIT);
		gtWorkThread = null;
	}

	// @Override
	public void onClick(View v) {
		Xlog.v(TAG, "-->onClick");
		if (v.equals(mBtnAdd)) {
			// handleAddBtnClick();
			workThreadHandler.sendEmptyMessage(ADDTOWHITELIST);
		} else if (v.equals(mBtnRemove)) {
			// handleRemoveBtnClick();
			workThreadHandler.sendEmptyMessage(REMOVEFROMWHITELIST);
		} else if (v.equals(mBtnClear)) {
			// handleClearBtnClick();
			workThreadHandler.sendEmptyMessage(CLEARWHITELIST);
		} else if (v.equals(mBtnHCIReset)) {
			// handleHCIResetBtnClick();
			workThreadHandler.sendEmptyMessage(HCIRESET);
		} else {
			Xlog.v(TAG, "error occurs");
		}
	}

	// handle "Add Device To WhiteList" button event
	private void handleAddBtnClick() {
		/*
		 * If pressing "Add Device To White List" button Tx: 01 11 20 07 XX FF
		 * EE DD CC BB AA Rx: 04 0E 04 01 12 20 00 XX = 00 for Public Device
		 * Address, 01 for Random Device Address
		 */

		char[] cmd = new char[11];
		int cmdLen = 11;
		char[] response = null;
		String sPrint = null;
		int i = 0;
		Xlog.v(TAG, "-->handleAddBtnClick");
		CharSequence cData = mEditMACAddress.getText();
		CharSequence subCData = null;
		int charSLen = cData.length();
		int dataLen = (charSLen + 1) / 2;
		if (dataLen != 6) {
			Toast.makeText(BLE_WhiteList.this, "invalid input value",
					Toast.LENGTH_SHORT).show();
			return;
		}

		for (i = 0; i < dataLen; i++) {
			try {
				subCData = cData.subSequence(2 * i,
						2 * i + 2 > charSLen ? charSLen : 2 * i + 2);
				cmd[cmdLen - 1 - i] = (char) Long.parseLong(
						subCData.toString(), 16);
			} catch (Exception e) {
				Toast.makeText(BLE_WhiteList.this, "invalid input value",
						Toast.LENGTH_SHORT).show();
				return;
			}
			sPrint = String.format("cmd[%d] = 0x%02x", cmdLen - 1 - i,
					(long) cmd[cmdLen - 1 - i]);
			Xlog.v(TAG, sPrint);
		}

		cmd[0] = 0x01;
		cmd[1] = 0x11;
		cmd[2] = 0x20;
		cmd[3] = 0x07;
		cmd[4] = (char) mAddressTypeValue;

		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {

			for (i = 0; i < response.length; i++) {
				sPrint = String.format("response[%d] = 0x%02x", i,
						(long) response[i]);
				Xlog.v(TAG, sPrint);
			}
		} else {
			Xlog.i(TAG, "BtTest_HCICommandRun failed");
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;

	}

	// handle "Remove Device From WhiteList" button event
	private void handleRemoveBtnClick() {
		/*
		 * If pressing "Remove Device To White List" button Tx: 01 12 20 07 XX
		 * FF EE DD CC BB AA Rx: 04 0E 04 01 12 20 00 XX = 00 for Public Device
		 * Address, 01 for Random Device Address
		 */
		char[] cmd = new char[11];
		int cmdLen = 11;
		char[] response = null;
		String sPrint = null;
		int i = 0;
		CharSequence cData = mEditMACAddress.getText();
		CharSequence subCData = null;
		int charSLen = cData.length();
		int dataLen = (charSLen + 1) / 2;
		Xlog.v(TAG, "-->handleRemoveBtnClick");
		if (dataLen != 6) {
			Toast.makeText(BLE_WhiteList.this, "invalid MAC address",
					Toast.LENGTH_SHORT).show();
			return;
		}

		for (i = 0; i < dataLen; i++) {
			try {
				subCData = cData.subSequence(2 * i,
						2 * i + 2 > charSLen ? charSLen : 2 * i + 2);
				cmd[cmdLen - 1 - i] = (char) Long.parseLong(
						subCData.toString(), 16);
			} catch (Exception e) {
				Toast.makeText(BLE_WhiteList.this, "invalid MAC address",
						Toast.LENGTH_SHORT).show();
				return;
			}
			sPrint = String.format("cmd[%d] = 0x%02x", cmdLen - 1 - i,
					(long) cmd[cmdLen - 1 - i]);
			Xlog.i(TAG, sPrint);
		}

		cmd[0] = 0x01;
		cmd[1] = 0x12;
		cmd[2] = 0x20;
		cmd[3] = 0x07;
		cmd[4] = (char) mAddressTypeValue;

		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {
			for (i = 0; i < response.length; i++) {
				sPrint = String.format("response[%d] = 0x%02x", i,
						(long) response[i]);
				Xlog.i(TAG, sPrint);
			}
		} else {
			Xlog.i(TAG, "BtTest_HCICommandRun failed");
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;
	}

	// handle "Clear Device From WhiteList" button event
	private void handleClearBtnClick() {
		/*
		 * If pressing "Clear White List" button Tx: 01 10 20 00 Rx: 04 0E 04 01
		 * 10 20 00
		 */
		int cmdLen = 4;
		char[] cmd = new char[cmdLen];
		char[] response = null;
		String s = null;
		int i = 0;
		Xlog.v(TAG, "-->handleClearBtnClick");
		cmd[0] = 0x01;
		cmd[1] = 0x10;
		cmd[2] = 0x20;
		cmd[3] = 0x00;
		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {

			for (i = 0; i < response.length; i++) {
				s = String.format("response[%d] = 0x%x", i, (long) response[i]);
				Xlog.i(TAG, s);
			}
		} else {
			Xlog.i(TAG, "HCICommandRun failed");
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;
	}

	// handle "HCI Reset" button event
	private void handleHCIResetBtnClick() {
		runHCIResetCmd();
	}

	// run "HCI Reset" command
	private void runHCIResetCmd() {
		/*
		 * If pressing "HCI Reset" button Tx: 01 03 0C 00 Rx: 04 0E 04 01 03 0C
		 * 00 After pressing "HCI Reset" button, all state will also be reset
		 */
		char[] cmd = new char[4];
		int cmdLen = 4;
		char[] response = null;
		int i = 0;
		Xlog.v(TAG, "-->runHCIResetCmd");
		cmd[0] = 0x01;
		cmd[1] = 0x03;
		cmd[2] = 0x0C;
		cmd[3] = 0x00;
		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {
			String s = null;
			for (i = 0; i < response.length; i++) {
				s = String.format("response[%d] = 0x%x", i, (long) response[i]);
				Xlog.i(TAG, s);
			}
		} else {
			Xlog.i(TAG, "HCICommandRun failed");
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;
	}

	// init BtTest -call init function of BtTest
	private boolean initBtTestOjbect() {
		Xlog.v(TAG, "-->initBtTestOjbect");
		if (mbIsIniting) {
			return false;
		}
		if (mbIsInit) {
			return mbIsInit;
		}
		if (mBT == null) {
			mBT = new BtTest();
		}
		if (mBT != null && !mbIsInit) {
			mbIsIniting = true;
			if (mBT.Init() != 0) {
				mbIsInit = false;
				Xlog.i(TAG, "mBT initialization failed");
			} else {
				runHCIResetCmd();
				mbIsInit = true;
			}
		}
		mbIsIniting = false;
		return mbIsInit;
	}

	// clear BtTest object -call deInit function of BtTest
	private boolean uninitBtTestOjbect() {
		Xlog.v(TAG, "-->uninitBtTestOjbect");
		if (mBT != null && mbIsInit) {
			runHCIResetCmd();
			if (mBT.UnInit() != 0) {
				Xlog.i(TAG, "mBT un-initialization failed");
			}
		}
		mBT = null;
		mbIsInit = false;
		return true;
	}

	private class WorkThread extends Thread {

		public void run() {
			Xlog.i(TAG, "WorkThread begins, thread ID = " + getId());
			Looper.prepare();
			workThreadHandler = new Handler() {
				// @Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case ADDTOWHITELIST:
						if (initBtTestOjbect()) {
							handleAddBtnClick();
						}

						break;
					case REMOVEFROMWHITELIST:
						if (initBtTestOjbect()) {
							handleRemoveBtnClick();
						}
						break;
					case CLEARWHITELIST:
						if (initBtTestOjbect()) {
							handleClearBtnClick();
						}
						break;
					case HCIRESET:
						if (initBtTestOjbect()) {
							handleHCIResetBtnClick();
						}
						break;
					case ACTIVITYEXIT:
						uninitBtTestOjbect();
						workThreadHandler.getLooper().quit();
						break;
					default:
						break;
					}

				}
			};

			Looper.loop();
			Xlog.i(TAG, "WorkThread exits");
		}
	}
}
