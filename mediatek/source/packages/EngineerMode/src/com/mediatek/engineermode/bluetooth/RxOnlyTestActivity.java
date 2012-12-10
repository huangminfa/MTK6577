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

public class RxOnlyTestActivity extends Activity implements OnClickListener {
	private BluetoothAdapter mAdapter;

	private Button mBtnStart = null;
	private Button mBtnStop = null;
	private Button mBtnSetPwr = null;
	private Spinner mSpnRxPattern = null;
	private Spinner mSpnRxChannel = null;
	private Spinner mSpnRxPacketType = null;
	private Spinner mSpnRxPowerControl = null;

	private EditText mEditRxFrequency = null;
	private EditText mEditRxPollPeriod = null;
	private EditText mEditRxDataLength = null;
	private EditText mEditRxAccessCode = null;

	private CheckBox mcRxWritten = null;
	private CheckBox mcRxPowerControl = null;

	// adapter need by Spinner
	private ArrayAdapter<String> mSpnRxPatternAdatper;
	private ArrayAdapter<String> mSpnRxChannelAdatper;
	private ArrayAdapter<String> mSpnRxPacketTypeAdatper;
	private ArrayAdapter<String> mSpnRxPowerControlAdatper;

	// value recorder
	private byte mRxPatternValue = 0x00;
	private byte mRxChannelValue = 0x00;
	private byte mRxRxPacketTypeValue = 0x00;
	private byte mRxRxPowerControlValue = 0x00;

	// private unsigned char mRxFrequencyValue = 0x0000;

	private final String TAG = "BLEWhiteList";

	private BtTest mBT;
	// UI thread's handler
	private Handler mainHandler = null;
	// WorkThraed and its Handler
	private WorkThread gtWorkThread = null;
	private Handler workThreadHandler = null;

	// BtTest object init and start test flag
	private boolean mbIsInit = false;
	private boolean mbIsTestStared = false;
	private boolean mbIsIniting = false;
	// Message ID
	private static final int RXONLYTESTSTART = 1;
	private static final int RXONLYTESTSTOP = 2;
	private static final int RXONLYSETPWRCTL = 3;

	private static final int STARTTESTSUCCESS = 4;
	private static final int STARTTESTFAILED = 5;
	private static final int STOPFINISH = 6;
	private static final int SETPWRCTLFINISH = 7;

	private static final int ACTIVITYEXIT = 8;

	@Override
	protected void onCreate(Bundle onSavedInstanceState) {
		Xlog.v(TAG, "-->onCreate");
		super.onCreate(onSavedInstanceState);
		setContentView(R.layout.rx_only_test);

		mBtnStart = (Button) findViewById(R.id.RxStart);
		mBtnStop = (Button) findViewById(R.id.RxStop);
		mBtnSetPwr = (Button) findViewById(R.id.RxSetPwr);
		mEditRxFrequency = (EditText) findViewById(R.id.RxFrequency);
		mEditRxPollPeriod = (EditText) findViewById(R.id.RxPollPeriod);
		mEditRxDataLength = (EditText) findViewById(R.id.RxDataLength);
		mEditRxAccessCode = (EditText) findViewById(R.id.RxAccessCode);
		mcRxWritten = (CheckBox) findViewById(R.id.RxWritten);
		mcRxPowerControl = (CheckBox) findViewById(R.id.RxPowerControl);
		mSpnRxPattern = (Spinner) findViewById(R.id.RxPatternSpinner);

		if (mBtnStart == null || mBtnStop == null || mBtnSetPwr == null
				|| mEditRxFrequency == null || mEditRxPollPeriod == null
				|| mEditRxDataLength == null || mEditRxAccessCode == null
				|| mcRxWritten == null || mcRxPowerControl == null
				|| mSpnRxPattern == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

		mBtnStart.setOnClickListener(this);
		mBtnStop.setOnClickListener(this);
		mBtnSetPwr.setOnClickListener(this);
		mcRxWritten.setOnClickListener(this);
		mcRxPowerControl.setOnClickListener(this);

		mEditRxFrequency.setText("15");
		mEditRxPollPeriod.setText("1a");
		mEditRxDataLength.setText("aa");
		mEditRxAccessCode.setText("1122334455667788");

		// address type spinner content fill and event handler
		mSpnRxPatternAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnRxPatternAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnRxPatternAdatper.add("Rx Test");
		/*
		 * mSpnRxPatternAdatper.add("0000"); mSpnRxPatternAdatper.add("1111");
		 * mSpnRxPatternAdatper.add("1010"); mSpnRxPatternAdatper.add("1100");
		 * mSpnRxPatternAdatper.add("pseudorandom bit sequence");
		 * mSpnRxPatternAdatper.add("loopback ACL with whitening");
		 * mSpnRxPatternAdatper.add("loopback SCO with whitening");
		 * mSpnRxPatternAdatper.add("loopback SCO without whitening");
		 */
		if (null != mSpnRxPattern) {
			mSpnRxPattern.setAdapter(mSpnRxPatternAdatper);
			mSpnRxPattern
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							Xlog.v(TAG, "item id = " + arg2);
							// mRxPatternValue = (byte)arg2;
							mRxPatternValue = 7; // this should be fixed to 6
													// when take Rx test
						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			Xlog.w(TAG, "findViewById(R.id.RxPattern) failed");
		}

		mSpnRxChannel = (Spinner) findViewById(R.id.RxChannelSpinner);
		mSpnRxChannelAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnRxChannelAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnRxChannelAdatper.add("Single Frequency");
		mSpnRxChannelAdatper.add("Hopping Frequency");
		if (null != mSpnRxChannel) {
			mSpnRxChannel.setAdapter(mSpnRxChannelAdatper);
			mSpnRxChannel
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							Xlog.v(TAG, "item id = " + arg2);
							mRxChannelValue = (byte) arg2;
							mEditRxFrequency
									.setEnabled(mRxChannelValue == 0 ? true
											: false);
						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			Xlog.w(TAG, "findViewById(R.id.RxChannel) failed");
		}

		mSpnRxPacketType = (Spinner) findViewById(R.id.RxPacketTypeSpinner);
		mSpnRxPacketTypeAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnRxPacketTypeAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnRxPacketTypeAdatper.add("NULL");
		mSpnRxPacketTypeAdatper.add("POLL");
		mSpnRxPacketTypeAdatper.add("FHS");
		mSpnRxPacketTypeAdatper.add("DM1");
		mSpnRxPacketTypeAdatper.add("DH1");
		mSpnRxPacketTypeAdatper.add("HV1");
		mSpnRxPacketTypeAdatper.add("HV2");
		mSpnRxPacketTypeAdatper.add("HV3");
		mSpnRxPacketTypeAdatper.add("DV");
		mSpnRxPacketTypeAdatper.add("AUX");
		mSpnRxPacketTypeAdatper.add("DM3");
		mSpnRxPacketTypeAdatper.add("DM5");
		mSpnRxPacketTypeAdatper.add("DH3");
		mSpnRxPacketTypeAdatper.add("DH5");
		mSpnRxPacketTypeAdatper.add("EV3");
		mSpnRxPacketTypeAdatper.add("EV4");
		mSpnRxPacketTypeAdatper.add("EV5");
		if (null != mSpnRxPacketType) {
			mSpnRxPacketType.setAdapter(mSpnRxPacketTypeAdatper);
			mSpnRxPacketType
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							Xlog.v(TAG, "item id = " + arg2);
							mRxRxPacketTypeValue = (byte) arg2;
						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			Xlog.w(TAG, "findViewById(R.id.RxPacketType) failed");
		}

		mSpnRxPowerControl = (Spinner) findViewById(R.id.RxPowerControlSpinner);
		mSpnRxPowerControlAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnRxPowerControlAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnRxPowerControlAdatper.add("0");
		mSpnRxPowerControlAdatper.add("1");
		mSpnRxPowerControlAdatper.add("2");
		mSpnRxPowerControlAdatper.add("3");
		mSpnRxPowerControlAdatper.add("4");
		mSpnRxPowerControlAdatper.add("5");
		mSpnRxPowerControlAdatper.add("6");
		mSpnRxPowerControlAdatper.add("7");

		if (null != mSpnRxPowerControl) {
			mSpnRxPowerControl.setAdapter(mSpnRxPowerControlAdatper);
			mSpnRxPowerControl
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							Xlog.v(TAG, "item id = " + arg2);
							mRxRxPowerControlValue = (byte) arg2;
						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			Xlog.w(TAG, "findViewById(R.id.RxPowerControl) failed");
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
				case SETPWRCTLFINISH:
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
			Xlog.w(TAG, "create WorkThread failed");
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
			workThreadHandler.sendEmptyMessage(RXONLYTESTSTART);
		} else if (v.equals(mBtnStop)) {
			mBtnStop.setEnabled(false);
			workThreadHandler.sendEmptyMessage(RXONLYTESTSTOP);
		} else if (v.equals(mcRxPowerControl)) {
			Xlog.v(TAG, "mcRxPowerControl clicked");
			if (mcRxPowerControl.isChecked()) {
				// mSpnRxPowerControl.setEnabled(true);
			} else {
				// mSpnRxPowerControl.setEnabled(false);
			}
		} else if (v.equals(mBtnSetPwr)) {
			mBtnStop.setEnabled(false);
			setViewState(true);
			workThreadHandler.sendEmptyMessage(RXONLYSETPWRCTL);
		} else if (v.equals(mcRxWritten)) {

		} else {
			Xlog.v(TAG, "error occurs");
		}
	}

	// handle "Add Device To WhiteList" button event
	private boolean handleStartBtnClick() {
		/*
		 * If pressing "Add Device To White List" button Tx: 01 15 FC 01 written
		 * Rx: 04 0E 04 01 15 FC 00 written = 00 (NOT CHECKED) 01 (CHECKED)
		 */
		int cmdLen = 5;
		char[] cmd = new char[cmdLen];
		char[] response = null;
		String sPrint = null;
		int i = 0;
		String s = null;
		CharSequence cData = null;
		CharSequence subCData = null;
		int charSLen = 0;
		int dataLen = 0;
		long inputValue = 0;
		boolean mbWritten = false;
		Xlog.v(TAG, "-->handleAddBtnClick");
		char mcPollPeriod = 0x00;
		char mcDataLength = 0x00;
		char mcFrequency = 0x00;
		char[] mcAccessCode = new char[8];
		/* check parameters first */
		cData = mEditRxPollPeriod.getText();
		try {
			inputValue = Long.parseLong(cData.toString(), 16);
		} catch (Exception e) {
			Toast.makeText(RxOnlyTestActivity.this,
					"invalid Poll period value", Toast.LENGTH_SHORT).show();
			return false;
		}

		if (inputValue > 0xff) {
			mEditRxPollPeriod.setText("FF");
			inputValue = 0xFF;
		}
		if (inputValue < 0) {
			mEditRxPollPeriod.setText("0");
			inputValue = 0;
		}
		mcPollPeriod = (char) inputValue;

		mcDataLength = 0x0; // this should always be 0x00

		cData = mEditRxFrequency.getText();
		try {
			inputValue = Long.parseLong(cData.toString(), 16);
		} catch (Exception e) {
			Toast.makeText(RxOnlyTestActivity.this, "invalid Frequency value",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if (inputValue > 0xff) {
			mEditRxFrequency.setText("FF");
			inputValue = 0xFF;
		}
		if (inputValue < 0) {
			mEditRxFrequency.setText("0");
			inputValue = 0;
		}
		mcFrequency = (char) inputValue;

		cData = mEditRxAccessCode.getText();
		charSLen = cData.length();
		dataLen = (charSLen + 1) / 2;
		if (dataLen != 8) {
			Toast.makeText(RxOnlyTestActivity.this, "invalid access code",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		for (i = 0; i < dataLen; i++) {
			try {
				subCData = cData.subSequence(2 * i,
						2 * i + 2 > charSLen ? charSLen : 2 * i + 2);
				mcAccessCode[i] = (char) Long
						.parseLong(subCData.toString(), 16);
			} catch (Exception e) {
				Toast.makeText(RxOnlyTestActivity.this, "invalid access code",
						Toast.LENGTH_SHORT).show();
				return false;
			}
			s = String.format("mcAccessCode[%d] = 0x%02x", i,
					(long) mcAccessCode[i]);
			Xlog.v(TAG, s);
		}

		mbWritten = mcRxWritten.isChecked();
		cmd[0] = 0x01;
		cmd[1] = 0x15;
		cmd[2] = 0xFC;
		cmd[3] = 0x01;
		cmd[4] = (char) (mbWritten ? 1 : 0);

		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {

			for (i = 0; i < response.length; i++) {
				sPrint = String.format("response[%d] = 0x%02x", i,
						(long) response[i]);
				Xlog.v(TAG, sPrint);
			}
		} else {
			Xlog.v(TAG, "BtTest_HCICommandRun failed");
		}
		cmd = null;
		/*
		 * Tx: 01 0D FC 17 00 00 PATTERN FREQ_TYPE FREQ FREQ PWR_CTL POLL_PERIOD
		 * PACKET_TYPE DATA_LEN 00 02 00 01 00 00 00 00 00 00 00 00 00 Rx: ?? ??
		 * ?? ??//04 0E 04 01 15 FC 00 Rx: ?? ?? ?? ?? PATTERN = 7 //for rx test
		 * FREQ_TYPE = 0 (single frequency) 1-(hopping) FREQ = single frequency
		 * (0 ~ 255) PWR_CTL = 0(ON) 1(OFF)
		 */
		cmdLen = 27;
		cmd = new char[cmdLen];
		cmd[0] = 0x01;
		cmd[1] = 0x0D;
		cmd[2] = 0xFC;
		cmd[3] = 0x17;
		cmd[4] = 0x00;
		cmd[5] = 0x00;
		cmd[6] = (char) mRxPatternValue;
		cmd[7] = (char) mRxChannelValue;
		cmd[8] = (char) mcFrequency;
		cmd[9] = (char) mcFrequency;
		cmd[10] = (char) mRxRxPowerControlValue;
		cmd[11] = (char) mcPollPeriod;
		cmd[12] = (char) mRxRxPacketTypeValue;
		cmd[13] = (char) mcDataLength;
		cmd[14] = 0x00;
		cmd[15] = 0x02;
		cmd[16] = 0x00;
		cmd[17] = 0x01;
		for (i = 18; i < cmdLen; i++) {
			cmd[i] = 0x00;
		}
		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {

			for (i = 0; i < response.length; i++) {
				sPrint = String.format("response[%d] = 0x%02x", i,
						(long) response[i]);
				Xlog.v(TAG, sPrint);
			}
		} else {
			Xlog.v(TAG, "BtTest_HCICommandRun failed");
		}
		response = null;
		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {

			for (i = 0; i < response.length; i++) {
				sPrint = String.format("response[%d] = 0x%02x", i,
						(long) response[i]);
				Xlog.v(TAG, sPrint);
			}
		} else {
			Xlog.v(TAG, "BtTest_HCICommandRun failed");
		}

		/*
		 * Tx:01 12 FC 08 ACCESS_CODE[8] Rx:04 0E 04 01 12 FC 00
		 */
		cmdLen = 12;

		cmd[0] = 0x01;
		cmd[1] = 0x12;
		cmd[2] = 0xFC;
		cmd[3] = 0x08;
		for (i = 0; i < 8; i++) {
			cmd[4 + i] = mcAccessCode[i];
		}

		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {

			for (i = 0; i < response.length; i++) {
				sPrint = String.format("response[%d] = 0x%02x", i,
						(long) response[i]);
				Xlog.v(TAG, sPrint);
			}
		} else {
			Xlog.v(TAG, "BtTest_HCICommandRun failed");
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;
		return true;
	}

	private void handleStopBtnClick() {
		Xlog.v(TAG, "-->handleAddBtnClick");
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
				Xlog.v(TAG, s);
			}
		} else {
			Xlog.v(TAG, "HCICommandRun failed");
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;
	}

	// handle "Power Ctrl" button event
	private void handleSetPwrCtlBtnClick() {
		/*
		 * If pressing "Power Ctrl" button Tx: 01 17 FC 04 00 00 level 00 Rx: 04
		 * 0E 04 *** After pressing "Power Ctrl" button, all state will also be
		 * reset
		 */
		int cmdLen = 8;
		char[] cmd = new char[cmdLen];
		char[] response = null;
		int i = 0;
		Xlog.v(TAG, "-->handleHCIResetBtnClick");
		cmd[0] = 0x01;
		cmd[1] = 0x17;
		cmd[2] = 0xFC;
		cmd[3] = 0x04;
		cmd[4] = 0x00;
		cmd[5] = 0x00;
		cmd[6] = (char) mSpnRxPowerControl.getSelectedItemId();
		cmd[7] = 0x00;

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
				Xlog.w(TAG, "mBT initialization failed");
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
				Xlog.w(TAG, "mBT un-initialization failed");
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
					case RXONLYTESTSTART:
						if (initBtTestOjbect()) {
							if (handleStartBtnClick()) {
								mainHandler.sendEmptyMessage(STARTTESTSUCCESS);
							}
						}
						if (!mbIsInit) {
							mainHandler.sendEmptyMessage(STARTTESTFAILED);
						}

						break;
					case RXONLYTESTSTOP:
						if (initBtTestOjbect()) {
							handleStopBtnClick();
						}
						mainHandler.sendEmptyMessage(STOPFINISH);
						break;

					case RXONLYSETPWRCTL:
						if (initBtTestOjbect()) {
							handleSetPwrCtlBtnClick();
						}
						mainHandler.sendEmptyMessage(SETPWRCTLFINISH);
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
			Xlog.v(TAG, "WorkThread exits");
		}
	}

	/*
	 * set view to state(true/false --> enable(stop btn pressed)/disable(start
	 * btn pressed)) enable -stop btn pressed disable -start btn pressed
	 */
	private void setViewState(boolean state) {

		mBtnStop.setEnabled(state);
		mcRxPowerControl.setEnabled(!state);
		mEditRxAccessCode.setEnabled(!state);
		mBtnStart.setEnabled(!state);
		mBtnSetPwr.setEnabled(!state);
		mSpnRxPattern.setEnabled(!state);
		mSpnRxChannel.setEnabled(!state);
		mSpnRxPacketType.setEnabled(!state);
		mSpnRxPowerControl.setEnabled(!state);
		mEditRxFrequency.setEnabled(!state);
		mEditRxPollPeriod.setEnabled(!state);
		mEditRxDataLength.setEnabled(!state);
		mcRxWritten.setEnabled(!state);

	}
}
