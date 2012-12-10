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
import android.widget.RadioButton;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

public class BLE_Test_Mode extends Activity implements OnClickListener {

	private BluetoothAdapter mAdapter;

	private final String TAG = "BLETestMode";

	// views in this activity
	// button
	private Button mBtnStart = null;
	private Button mBtnStop = null;

	private TextView BLEResult_Text = null;
	private String BLEResult_String = "R:";

	// Radio Button
	// BLE test mode Tx/Rx RadioGroup
	private RadioButton mRBtnTx = null;
	private RadioButton mRBtnRx = null;
	// BLE test mode Hopping/Single RadioGroup
	private RadioButton mRBtnHopping = null;
	private RadioButton mRBtnSingle = null;

	// Checkbox
	private CheckBox mChkBoxConTx = null;

	// Spinner
	private Spinner mSpnChannel = null;
	private Spinner mSpnPattern = null;

	// adapter associate with spinner view
	private ArrayAdapter<String> mSpnChannelAdapter;
	private ArrayAdapter<String> mSpnPatternAdapter;

	// spinner value
	private byte mSpnChannelValue = 0x00;
	private byte mSpnPatternValue = 0x00;

	// btn values
	private boolean mbIsTxTest = true;
	private boolean mbIsHopping = false;

	// jni layer object
	private BtTest mBT = null;

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
	// execuate result
	private static final int STARTTESTSUCCESS = 5;
	private static final int STARTTESTFAILED = 6;
	private static final int STOPFINISH = 7;
	// activity exit message ID
	private static final int ACTIVITYEXIT = 8;

	@Override
	protected void onCreate(Bundle onSavedInstanceState) {
		Xlog.v(TAG, "-->onCreate");
		super.onCreate(onSavedInstanceState);
		setContentView(R.layout.ble_test_mode);

		mBtnStart = (Button) findViewById(R.id.BLEStart);
		mBtnStop = (Button) findViewById(R.id.BLEStop);
		BLEResult_Text = (TextView) findViewById(R.id.BLEResult_Text);
		if (mBtnStart == null || mBtnStop == null || BLEResult_Text == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

		mBtnStart.setOnClickListener(this);
		mBtnStop.setOnClickListener(this);

		mRBtnTx = (RadioButton) findViewById(R.id.BLETestModeTx);
		mRBtnRx = (RadioButton) findViewById(R.id.BLETestModeRx);
		if (mRBtnTx == null || mRBtnRx == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

		mRBtnTx.setChecked(true);
		mbIsTxTest = true;

		mRBtnTx.setOnClickListener(this);
		mRBtnRx.setOnClickListener(this);

		mRBtnHopping = (RadioButton) findViewById(R.id.BLEHopping);
		mRBtnSingle = (RadioButton) findViewById(R.id.BLESingle);
		if (mRBtnHopping == null || mRBtnSingle == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

		// new issue edited by mtk54040 Shuaiqiang @2011-10-12
		// mRBtnHopping.setChecked(true);
		mRBtnSingle.setChecked(true);
		mbIsHopping = false;

		mRBtnSingle.setOnClickListener(this);
		mRBtnHopping.setOnClickListener(this);

		mChkBoxConTx = (CheckBox) findViewById(R.id.BLEContiniousTx);

		mSpnChannel = (Spinner) findViewById(R.id.BLEChannelSpinner);
		mSpnPattern = (Spinner) findViewById(R.id.BLEPatternSpinner);
		if (mChkBoxConTx == null || mSpnChannel == null || mSpnPattern == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

		// filll "channel  " content and action handler set
		mSpnChannelAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnChannelAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < 40; i++) {
			mSpnChannelAdapter.add("CH " + i);
		}
		if (null != mSpnChannel) {
			mSpnChannel.setAdapter(mSpnChannelAdapter);
			mSpnChannel.setOnItemSelectedListener(new OnItemSelectedListener() {

				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					Xlog.i(TAG, "item id = " + arg2);
					mSpnChannelValue = (byte) arg2;
				}

				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
				}
			});
		} else {
			Xlog.i(TAG, "findViewById(R.id.BLEChannelSpinner) failed");
		}

		// filll "pattern  " content and action handler set
		mSpnPatternAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnPatternAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnPatternAdapter.add("PRBS9");
		mSpnPatternAdapter.add("11110000");
		mSpnPatternAdapter.add("10101010");
		if (null != mSpnPattern) {
			mSpnPattern.setAdapter(mSpnPatternAdapter);
			mSpnPattern.setOnItemSelectedListener(new OnItemSelectedListener() {

				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					Xlog.i(TAG, "item id = " + arg2);
					mSpnPatternValue = (byte) arg2;
				}

				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
				}
			});
		} else {
			Xlog.i(TAG, "findViewById(R.id.BLEPatternSpinner) failed");
		}

		mainHandler = new Handler() {
			// @Override
			public void handleMessage(Message msg) {
				Xlog.i(TAG, "-->main Handler - handleMessage");
				BLEResult_Text.setText(BLEResult_String);
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
				default:
					break;
				}
			}
		};

		setViewState(false);

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

	/* BLE test mode Tx test */
	public boolean handleTxTestStart() {
		Xlog.v(TAG, "-->handleTxTestStart");
		/*
		 * If pressing "Start" button Tx: 01 1E 20 03 XX 25 YY //HCI LE
		 * Transmitter Test CMD XX is based on Channel selection YY is based on
		 * Pattern selection Rx: 04 0E 04 01 1E 20 00 //HCI Command Complete
		 * Event
		 */
		int cmdLen = 7;
		char[] cmd = new char[cmdLen];
		char[] response = null;
		int i = 0;
		Xlog.v(TAG, "-->handleTxTestStart");
		cmd[0] = 0x01;
		cmd[1] = 0x1E;
		cmd[2] = 0x20;
		cmd[3] = 0x03;
		cmd[4] = (char) mSpnChannelValue;
		cmd[5] = 0x25;
		cmd[6] = (char) mSpnPatternValue;

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

	public void handleTxTestStop() {

		/*
		 * If pressing "Stop" button Tx: 01 1F 20 00 //HCI LE Test End CMD For
		 * Rx, we have two cases of HCI Command Complete Event Case A) Rx: 04 0E
		 * 0A 01 1F 20 00 00 00 00 00 00 00 Case B) Rx: 04 0E 06 01 1F 20 00 00
		 * 00
		 */
		int cmdLen = 4;
		char[] cmd = new char[cmdLen];
		char[] response = null;
		int i = 0;

		Xlog.v(TAG, "-->handleTxTestStop");
		cmd[0] = 0x01;
		cmd[1] = 0x1F;
		cmd[2] = 0x20;
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
			return;
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;
	}

	/* BLE test mode test */
	public boolean handleRxTestStart() {
		Xlog.v(TAG, "-->handleRxTestStart");
		/*
		 * If pressing "Start" button Tx: 01 1D 20 01 ZZ //HCI LE Receiver Test
		 * CMD ZZ is based on Channel selection Rx: 04 0E 04 01 1D 20 00 //HCI
		 * Command Complete Event If pressing "Stop" button Tx: 01 1F 20 00
		 * //HCI LE Test End CMD For Rx, we have two cases of HCI Command
		 * Complete Event Case A) Rx: 04 0E 0A 01 1F 20 00 BB AA ?? ?? ?? ??
		 * Case B) Rx: 04 0E 06 01 1F 20 00 BB AA ?? means do not care Packet
		 * Count = 0xAABB
		 */
		int cmdLen = 5;
		char[] cmd = new char[cmdLen];
		char[] response = null;
		int i = 0;
		cmd[0] = 0x01;
		cmd[1] = 0x1D;
		cmd[2] = 0x20;
		cmd[3] = 0x01;
		cmd[4] = (char) mSpnChannelValue;
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

	public void handleRxTestStop() {
		Xlog.v(TAG, "-->handleRxTestStop");
		/*
		 * If pressing "Start" button Tx: 01 1D 20 01 ZZ //HCI LE Receiver Test
		 * CMD ZZ is based on Channel selection Rx: 04 0E 04 01 1D 20 00 //HCI
		 * Command Complete Event If pressing "Stop" button Tx: 01 1F 20 00
		 * //HCI LE Test End CMD For Rx, we have two cases of HCI Command
		 * Complete Event Case A) Rx: 04 0E 0A 01 1F 20 00 BB AA ?? ?? ?? ??
		 * Case B) Rx: 04 0E 06 01 1F 20 00 BB AA ?? means do not care Packet
		 * Count = 0xAABB
		 */
		int cmdLen = 4;
		char[] cmd = new char[cmdLen];
		char[] response = null;
		int i = 0;
		cmd[0] = 0x01;
		cmd[1] = 0x1F;
		cmd[2] = 0x20;
		cmd[3] = 0x00;

		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {
			String s = null;
			for (i = 0; i < response.length; i++) {
				s = String.format("response[%d] = 0x%x", i, (long) response[i]);
				Xlog.v(TAG, s);
			}
			// Response format: 04 0e 0a/06 01 1f 20 00 BB AA 00 00...
			// packet count = 0xAABB
			BLEResult_String = String.format("***Packet Count: %d",
					(long) response[8] * 256 + (long) response[7]);
		} else {
			Xlog.i(TAG, "HCICommandRun failed");
			return;
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;
	}

	/* after push "start" button */
	public boolean handleStartBtnClick() {
		Xlog.v(TAG, "-->handleStartBtnClick");
		// BLEResult_String = "Response:";
		// judge if Rx or Tx test is selected
		/*
		 * Each time before executing, HCI Reset CMD is needed to reset
		 * Bluetooth Controller Tx: 01 03 0C 00 //HCI Reset CMD Rx: 04 0E 04 01
		 * 03 0C 00 //HCI Command Complete Event
		 */
		int cmdLen = 4;
		char[] cmd = new char[cmdLen];
		char[] response = null;
		int i = 0;
		cmd[0] = 0x01;
		cmd[1] = 0x03;
		cmd[2] = 0x0C;
		cmd[3] = 0x00;
		// HCI reset command
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

		if (mbIsTxTest) {
			return handleTxTestStart();
		} else {
			return handleRxTestStart();
		}
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
		Xlog.i(TAG, "-->runHCIResetCmd");
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
			Xlog.v(TAG, "HCICommandRun failed");
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;
	}

	public void handleStopBtnClick() {
		Xlog.v(TAG, "-->handleStopBtnClick");
		if (mbIsTxTest) {
			handleTxTestStop();
		} else {
			handleRxTestStop();
		}
		mbIsTestStared = false;
	}

	// @Override
	public void onClick(View v) {
		Xlog.v(TAG, "-->onClick");
		if (v.equals(mBtnStart)) {
			setViewState(true);
			workThreadHandler.sendEmptyMessage(TESTSTART);
			/*
			 * if(!handleStartBtnClick()) { setViewState(false); }
			 */
		} else if (v.equals(mBtnStop)) {
			// Rx test button is clicked
			mBtnStop.setEnabled(false);
			workThreadHandler.sendEmptyMessage(TESTSTOP);
			/*
			 * handleStopBtnClick(); setViewState(false);
			 */

		} else if (v.equals(mRBtnRx)) {
			// Rx test button is clicked
			mbIsTxTest = false;

		} else if (v.equals(mRBtnTx)) {
			// Rx test button is clicked
			mbIsTxTest = true;

		} else if (v.equals(mRBtnHopping)) {
			// Hopping test button is clicked
			mbIsHopping = true;

		} else if (v.equals(mRBtnSingle)) {
			// Single test button is clicked
			mbIsHopping = false;
		} else {
			Xlog.i(TAG, "no view matches current view");
		}
	}

	/*
	 * set view to state(true/false --> enable(stop btn pressed)/disable(start
	 * btn pressed)) true - enable -stop btn pressed false - disable -start btn
	 * pressed
	 */
	private void setViewState(boolean state) {

		mRBtnTx.setEnabled(!state);
		mRBtnRx.setEnabled(!state);
		mRBtnHopping.setEnabled(false);
		mRBtnSingle.setEnabled(!state);
		mChkBoxConTx.setEnabled(!state);
		mSpnChannel.setEnabled(!state);
		mSpnPattern.setEnabled(!state);

		mBtnStart.setEnabled(!state);
		mBtnStop.setEnabled(state);
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
					case ACTIVITYEXIT:
						uninitBtTestOjbect();
						workThreadHandler.getLooper().quit();
					default:
						break;
					}

				}
			};

			Looper.loop();
			Xlog.v(TAG, "WorkThread exits");
		}
	}
}
