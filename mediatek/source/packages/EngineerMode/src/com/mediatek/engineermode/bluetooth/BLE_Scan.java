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
import com.mediatek.xlog.Xlog;
import com.mediatek.engineermode.bluetooth.BtTest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.bluetooth.BluetoothAdapter;
import android.app.Dialog;

public class BLE_Scan extends Activity implements OnClickListener {

	private BluetoothAdapter mAdapter;

	private final String TAG = "BLEScan";

	// view need to be operateed dynamically
	private Button mBtnStart = null;
	private Button mBtnStop = null;
	private Button mBtnSetResData = null;
	private Spinner mSpnScanType = null;
	private Spinner mSpnAddressType = null;
	private Spinner mSpnFilterPolicy = null;
	private Spinner mSpnFilterDuplicate = null;
	private EditText mEditInterval = null;
	private EditText mEditWindow = null;
	private EditText mEditResData = null;

	// adapter need by Spinner
	private ArrayAdapter<String> mSpnScanTypeAdapter;
	private ArrayAdapter<String> mSpnAddressTypeAdapter;
	private ArrayAdapter<String> mSpnFilterPolicyAdapter;
	private ArrayAdapter<String> mSpnFilterDuplicateAdapter;

	// spinner item related values
	private byte mScanTypeValue = 0x00;
	private byte mAddressTypeValue = 0x00;
	private byte mFilterPolicyValue = 0x00;
	private byte mFilterDuplicateValue = 0x00;

	private BtTest mBT;

	// UI thread's handler
	private Handler mainHandler = null;
	// WorkThraed and its Handler
	private WorkThread gtWorkThread = null;
	private Handler workThreadHandler = null;

	// BtTest object init and start test flag
	private boolean mbIsInit = false;
	private boolean mbIsTestStared = false;// this flag is not necessary in this
	// module
	private boolean mbIsIniting = false;

	// Message ID
	private static final int TESTSTART = 1;
	private static final int TESTSTOP = 2;
	private static final int SETSCANRES = 3;
	// execuate result
	private static final int STARTTESTSUCCESS = 5;
	private static final int STARTTESTFAILED = 6;
	private static final int STOPFINISH = 7;
	private static final int SETSCANRESFINISH = 8;
	// activity exit message ID
	private static final int ACTIVITYEXIT = 9;

	@Override
	protected void onCreate(Bundle onSavedInstanceState) {
		Xlog.v(TAG, "-->onCreate");
		super.onCreate(onSavedInstanceState);
		mBT = new BtTest();

		setContentView(R.layout.ble_normal_mode_scan);
		mBtnStart = (Button) findViewById(R.id.BLEScanStart);
		mBtnStop = (Button) findViewById(R.id.BLEScanStop);
		mBtnSetResData = (Button) findViewById(R.id.BLEScanSet);
		if (mBtnStart == null || mBtnStop == null || mBtnSetResData == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}
		// btn click monitor setting
		mBtnStart.setOnClickListener(this);
		mBtnStop.setOnClickListener(this);
		mBtnSetResData.setOnClickListener(this);

		mSpnScanType = (Spinner) findViewById(R.id.BLEScanTypeSpinner);
		mSpnAddressType = (Spinner) findViewById(R.id.BLEScanOwnAddressTypeSpinner);
		mSpnFilterPolicy = (Spinner) findViewById(R.id.BLEFilterPolicySpinner);
		mSpnFilterDuplicate = (Spinner) findViewById(R.id.BLEScanFilterDuplicate);

		mEditInterval = (EditText) findViewById(R.id.BLEScanInterval);
		mEditWindow = (EditText) findViewById(R.id.BLEScanWindow);
		mEditResData = (EditText) findViewById(R.id.BLEScanResponseData);
		if (mSpnScanType == null || mSpnAddressType == null
				|| mSpnFilterPolicy == null || mSpnFilterDuplicate == null
				|| mEditInterval == null || mEditWindow == null
				|| mEditResData == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

		mEditInterval.setText("12AB");
		mEditWindow.setText("12AB");
		mEditResData.setText("11AABB2233CC");

		// fill scan type spinner content and item selected event handler
		mSpnScanTypeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnScanTypeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnScanTypeAdapter.add("Passive");
		mSpnScanTypeAdapter.add("Active");
		if (null != mSpnScanType) {
			mSpnScanType.setAdapter(mSpnScanTypeAdapter);
			mSpnScanType
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							Xlog.v(TAG, "item id = " + arg2);
							mScanTypeValue = (byte) arg2;
						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			Xlog.i(TAG, "findViewById(R.id.BLEScanTypeSpinner) failed");
		}

		// fill address type spinner content and item selected event handler
		mSpnAddressTypeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnAddressTypeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnAddressTypeAdapter.add("Public Device Address");
		mSpnAddressTypeAdapter.add("Random Device Address");
		if (null != mSpnAddressType) {
			mSpnAddressType.setAdapter(mSpnAddressTypeAdapter);
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
			Xlog.i(TAG,
					"findViewById(R.id.BLEScanOwnAddressTypeSpinner) failed");
		}

		// fill filter policy spinner content and item selected event handler
		mSpnFilterPolicyAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnFilterPolicyAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnFilterPolicyAdapter.add("Accept All Advertise Packet");
		mSpnFilterPolicyAdapter
				.add("Ignore Advertise Packet Not In White List ");/*
																	 * only in
																	 * white
																	 * list
																	 */
		if (null != mSpnFilterPolicy) {
			mSpnFilterPolicy.setAdapter(mSpnFilterPolicyAdapter);
			mSpnFilterPolicy
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							Xlog.v(TAG, "item id = " + arg2);
							mFilterPolicyValue = (byte) arg2;
						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			Xlog.i(TAG, "findViewById(R.id.BLEScanFilterPolicySpinner) failed");
		}

		// fill filter duplicate spinner content and item selected event handler
		mSpnFilterDuplicateAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnFilterDuplicateAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnFilterDuplicateAdapter.add("Enable Duplicate Filtering");
		mSpnFilterDuplicateAdapter.add("Disable Duplicate Filtering");
		if (null != mSpnFilterDuplicate) {
			mSpnFilterDuplicate.setAdapter(mSpnFilterDuplicateAdapter);
			mSpnFilterDuplicate
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							Xlog.v(TAG, "item id = " + arg2);
							mFilterDuplicateValue = (byte) arg2;
						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			Xlog.i(TAG, "findViewById(R.id.BLEScanFilterDuplicate) failed");
		}
		setViewState(false);

		mainHandler = new Handler() {
			// @Override
			public void handleMessage(Message msg) {
				Xlog.v(TAG, "-->main Handler - handleMessage");
				switch (msg.what) {
				case STARTTESTSUCCESS:
					mbIsTestStared = true;
					break;
				case STARTTESTFAILED:
					// here we can give some notification
					mbIsTestStared = false;
					setViewState(false);
					break;
				case STOPFINISH:
					mbIsTestStared = false;
					setViewState(false);
					break;
				case SETSCANRESFINISH:
					setViewState(false);
					break;

				default:
					break;
				}
			}
		};
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
		if (v.equals(mBtnStart)) {
			setViewState(true);
			/*
			 * if(!handleStartBtnClick()) { setViewState(false); }
			 */
			workThreadHandler.sendEmptyMessage(TESTSTART);
		} else if (v.equals(mBtnStop)) {
			mBtnStop.setEnabled(false);
			workThreadHandler.sendEmptyMessage(TESTSTOP);
			/*
			 * handleStopBtnClick(); setViewState(false);
			 */
		} else if (v.equals(mBtnSetResData)) {
			setViewState(true);
			// stop button should be disabled as well
			mBtnStop.setEnabled(false);
			workThreadHandler.sendEmptyMessage(SETSCANRES);
			/*
			 * handleSetBtnClick(); setViewState(false);
			 */
		} else {
			Xlog.i(TAG, "error occurs");
		}
	}

	/*
	 * set view to state(true/false --> enable(stop btn pressed)/disable(start
	 * btn pressed)) enable -stop btn pressed disable -start btn pressed
	 */
	private void setViewState(boolean state) {

		mBtnStart.setEnabled(!state);
		mBtnStop.setEnabled(state);
		mBtnSetResData.setEnabled(!state);
		mSpnScanType.setEnabled(!state);
		mSpnAddressType.setEnabled(!state);
		mSpnFilterPolicy.setEnabled(!state);
		mSpnFilterDuplicate.setEnabled(!state);
		mEditInterval.setEnabled(!state);
		mEditWindow.setEnabled(!state);
		mEditResData.setEnabled(!state);

	}

	private boolean handleStartBtnClick() {
		Xlog.v(TAG, "-->handleStartBtnClick");

		/*
		 * start pushed Tx: 01 0B 20 07 ZZ XX YY UU VV KK JJ 0xZZ = Scan Type
		 * (00 = Passive Scan, 01 = Active Scan) 0xYYXX = LE Scan Interval
		 * (0x0004 ~ 0x4000) 0xVVUU = LE Scan Window (0x0004 ~ 0x4000) 0xKK =
		 * Own Address Type (00 = Public Device Address, 01 = Random Device
		 * Address) 0xJJ = Scan Filter Policy (00 = Accept All Advertise Packet,
		 * 01 = Ignore Advertise Packet Not In White List) Rx: 04 0E 04 01 0B 20
		 * 00 Tx: 01 0C 20 02 01 PP 0xPP = Filter Duplicate (00 = Disable
		 * Duplicate Filtering, 01 = Enable Duplicate Filtering) Rx: 04 0E 04 01
		 * 0C 20 00
		 */
		char[] response = null;
		int cmdLen = 11;
		long mIntervalValue = 0x0;
		long mWindowValue = 0x0;
		int i = 0;

		try {
			mWindowValue = (char) Long.parseLong(mEditWindow.getText()
					.toString(), 16);
			;
			mIntervalValue = (char) Long.parseLong(mEditInterval.getText()
					.toString(), 16);
			;
		} catch (Exception e) {
			Toast.makeText(BLE_Scan.this, "invalid input value",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		char[] cmd = new char[11];
		cmd[0] = 0x01;
		cmd[1] = 0x0b;
		cmd[2] = 0x20;
		cmd[3] = 0x07;
		cmd[4] = (char) mScanTypeValue;

		mIntervalValue = mIntervalValue < 0x0004 ? 0x0004 : mIntervalValue;
		mIntervalValue = mIntervalValue > 0x4000 ? 0x4000 : mIntervalValue;

		mWindowValue = mWindowValue < 0x0004 ? 0x0004 : mWindowValue;
		mWindowValue = mWindowValue > 0x4000 ? 0x4000 : mWindowValue;

		cmd[5] = (char) (mIntervalValue & 0xff);
		cmd[6] = (char) ((mIntervalValue & 0xff00) >> 8);
		cmd[7] = (char) (mWindowValue & 0xff);
		cmd[8] = (char) ((mWindowValue & 0xff00) >> 8);

		cmd[9] = (char) mAddressTypeValue;
		cmd[10] = (char) mFilterPolicyValue;

		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {
			String s = null;
			for (i = 0; i < response.length; i++) {
				s = String.format("response[%d] = 0x%x", i, (long) response[i]);
				Xlog.v(TAG, s);
			}
		} else {
			Xlog.i(TAG, "HCICommandRun failed");
			return false;
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;

		cmdLen = 6;
		cmd[0] = 0x01;
		cmd[1] = 0x0C;
		cmd[2] = 0x20;
		cmd[3] = 0x02;
		cmd[4] = 0x01;
		cmd[5] = (char) mFilterDuplicateValue;
		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {
			String s = null;
			for (i = 0; i < response.length; i++) {
				s = String.format("response[%d] = 0x%x", i, (long) response[i]);
				Xlog.v(TAG, s);
			}
		} else {
			Xlog.i(TAG, "HCICommandRun failed");
			return false;
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;
		return true;
	}

	private void handleStopBtnClick() {
		Xlog.v(TAG, "-->handleStopBtnClick");
		/*
		 * If pressing "Stop" button Tx: 01 0C 20 02 00 PP 0xPP = Filter
		 * Duplicate (00 = Disable Duplicate Filtering, 01 = Enable Duplicate
		 * Filtering) Rx: 04 0E 04 01 0C 20 00
		 */
		int cmdLen = 6;
		char[] cmd = new char[cmdLen];
		char[] response = null;
		int i = 0;
		cmd[0] = 0x01;
		cmd[1] = 0x0C;
		cmd[2] = 0x20;
		cmd[3] = 0x02;
		cmd[4] = 0x00;
		cmd[5] = (char) mFilterDuplicateValue;
		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {
			String s = null;
			for (i = 0; i < response.length; i++) {
				s = String.format("response[%d] = 0x%x", i, (long) response[i]);
				Xlog.v(TAG, s);
			}
		} else {
			Xlog.i(TAG, "HCICommandRun failed");
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;
	}

	private void handleSetBtnClick() {
		Xlog.v(TAG, "-->handleSetBtnClick");
		/*
		 * If pressing "Set Scan Response Data" button Tx: 01 09 20 0A XX 11 AA
		 * BB 22 33 CC Rx: 04 0E 04 01 09 20 00 XX = Length of data & XX must be
		 * 0 ~ 31 In this example XX = 06
		 */
		int i = 0;
		CharSequence cData = mEditResData.getText();
		CharSequence subCData = null;
		int charSLen = cData.length();
		int dataLen = (charSLen + 1) / 2;
		int cmdLen = dataLen + 5;
		if (dataLen < 0) {
			Toast.makeText(BLE_Scan.this, "invalid input value",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (dataLen > 0x31) {
			cmdLen = 0x31;
		}
		char[] cmd = new char[cmdLen];
		char[] response = null;
		for (i = 0; i < dataLen; i++) {
			String s = null;
			try {
				subCData = cData.subSequence(2 * i,
						2 * i + 2 > charSLen ? charSLen : 2 * i + 2);
				cmd[5 + i] = (char) Long.parseLong(subCData.toString(), 16);
			} catch (Exception e) {
				Toast.makeText(BLE_Scan.this, "invalid input value",
						Toast.LENGTH_SHORT).show();
				return;
			}
			s = String.format("cmd[%d] = 0x%x", 5 + i, (long) cmd[5 + i]);
			Xlog.v(TAG, s);
		}
		cmd[0] = 0x01;
		cmd[1] = 0x09;
		cmd[2] = 0x20;
		cmd[3] = (char) (dataLen + 1);
		cmd[4] = (char) dataLen;
		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {
			String s = null;
			for (i = 0; i < response.length; i++) {
				s = String.format("response[%d] = 0x%x", i, (long) response[i]);
				Xlog.v(TAG, s);
			}
		} else {
			Xlog.i(TAG, "HCICommandRun failed");
			return;
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;
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
				Xlog.v(TAG, s);
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
			if (mbIsTestStared) {
				// handleStopBtnClick();
				runHCIResetCmd();
			}
			if (mBT.UnInit() != 0) {
				Xlog.i(TAG, "mBT un-initialization failed");
			}
		}
		mBT = null;
		mbIsInit = false;
		mbIsTestStared = false;
		return true;
	}

	private class WorkThread extends Thread {

		public void run() {
			Xlog.v(TAG, "WorkThread begins, thread ID = " + getId());
			Looper.prepare();
			workThreadHandler = new Handler() {
				// @Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case TESTSTART:
						if (initBtTestOjbect()) {
							if (handleStartBtnClick()) {
								mainHandler.sendEmptyMessage(STARTTESTSUCCESS);
							}
						}
						if (!mbIsInit) {
							mainHandler.sendEmptyMessage(STARTTESTFAILED);
						}

						break;
					case TESTSTOP:
						if (mbIsInit) {
							handleStopBtnClick();
						}
						mainHandler.sendEmptyMessage(STOPFINISH);
						break;
					case SETSCANRES:
						if (initBtTestOjbect()) {
							handleSetBtnClick();
						}
						mainHandler.sendEmptyMessage(SETSCANRESFINISH);
						break;
					case ACTIVITYEXIT:
						uninitBtTestOjbect();
						workThreadHandler.getLooper().quit();
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
