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

public class BLE_Advertise extends Activity implements OnClickListener {
	private BluetoothAdapter mAdapter;

	private final String TAG = "BLEAdvertise";

	// view need to be operateed dynamically
	private Button mBtnStop = null;
	private Button mBtnStart = null;
	private Button mBtnSet = null;

	private Spinner mSpnAdvertiseType = null;
	private Spinner mSpnOwnAddressType = null;
	private Spinner mSpnDirectAddressType = null;
	private Spinner mSpnFilterPolicy = null;

	private EditText mEditIntervalMin = null;
	private EditText mEditIntervalMax = null;
	private EditText mEditDirectAddress = null;
	private EditText mEditAdvertiseData = null;

	private CheckBox mCheckChannel37 = null;
	private CheckBox mCheckChannel38 = null;
	private CheckBox mCheckChannel39 = null;

	private ArrayAdapter<String> mSpnAdvertiseTypeAdatper;
	private ArrayAdapter<String> mSpnOwnAddressTypeAdatper;
	private ArrayAdapter<String> mSpnDirectAddressTypeAdatper;
	private ArrayAdapter<String> mSpnFilterPolicyAdatper;

	private byte mSpnAdvertiseTypeValue = 0;
	private byte mSpnOwnAddressTypeValue = 0;
	private byte mSpnDirectAddressTypeValue = 0;
	private byte mSpnFilterPolicyValue = 0;

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
	private static final int SETADVERTISE = 3;
	// execuate result
	private static final int STARTTESTSUCCESS = 5;
	private static final int STARTTESTFAILED = 6;
	private static final int STOPFINISH = 7;
	private static final int SETADVERTISEFINISH = 8;
	private static final int CHKCHANNEL137 = 9;
	// activity exit message ID
	private static final int ACTIVITYEXIT = 9;

	@Override
	protected void onCreate(Bundle onSavedInstanceState) {
		Xlog.v(TAG, "-->onCreate");
		super.onCreate(onSavedInstanceState);
		setContentView(R.layout.ble_normal_mode_advertise);

		mBtnStop = (Button) findViewById(R.id.BLEAdvertiseStop);
		mBtnStart = (Button) findViewById(R.id.BLEAdvertiseStart);
		mBtnSet = (Button) findViewById(R.id.BLEAdvertiseSet);

		if (mBtnStop == null || mBtnStart == null || mBtnSet == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}
		mBtnStop.setOnClickListener(this);
		mBtnStart.setOnClickListener(this);
		mBtnSet.setOnClickListener(this);

		mSpnAdvertiseType = (Spinner) findViewById(R.id.BLEAdvertiseTypeSpinner);
		mSpnOwnAddressType = (Spinner) findViewById(R.id.BLEOwnAddressTypeSpinner);
		mSpnDirectAddressType = (Spinner) findViewById(R.id.BLEDirectAddressTypeSpinner);
		mSpnFilterPolicy = (Spinner) findViewById(R.id.BLEFilterPolicySpinner);

		mEditIntervalMin = (EditText) findViewById(R.id.BLEIntervalMin);
		mEditIntervalMax = (EditText) findViewById(R.id.BLEIntervalMax);
		mEditDirectAddress = (EditText) findViewById(R.id.BLEDirectAddress);
		mEditAdvertiseData = (EditText) findViewById(R.id.BLEAdvertiseData);
		if (mSpnAdvertiseType == null || mSpnOwnAddressType == null
				|| mSpnDirectAddressType == null || mSpnFilterPolicy == null
				|| mEditIntervalMin == null || mEditIntervalMax == null
				|| mEditDirectAddress == null || mEditAdvertiseData == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}
		mEditIntervalMin.setText("20");
		mEditIntervalMax.setText("100");
		mEditDirectAddress.setText("AABBCCDDEEFF");
		mEditAdvertiseData.setText("112233445566778899");

		mCheckChannel37 = (CheckBox) findViewById(R.id.BLEChannel37Checkbox);
		mCheckChannel38 = (CheckBox) findViewById(R.id.BLEChannel38Checkbox);
		mCheckChannel39 = (CheckBox) findViewById(R.id.BLEChannel39Checkbox);
		if (mCheckChannel37 == null || mCheckChannel38 == null
				|| mCheckChannel39 == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}
		// advertise type spinner content fill and event handler
		mSpnAdvertiseTypeAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnAdvertiseTypeAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnAdvertiseTypeAdatper.add("ADV_IND");
		mSpnAdvertiseTypeAdatper.add("ADV_DIRECT_IND");
		mSpnAdvertiseTypeAdatper.add("ADV_DISCOVER_IND");
		mSpnAdvertiseTypeAdatper.add("ADV_NONCONn_IND");
		if (null != mSpnAdvertiseType) {
			mSpnAdvertiseType.setAdapter(mSpnAdvertiseTypeAdatper);
			mSpnAdvertiseType
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							Xlog.i(TAG, "item id = " + arg2);
							mSpnAdvertiseTypeValue = (byte) arg2;
						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			Xlog.i(TAG, "findViewById(R.id.BLEOwnAddressTypeSpinner) failed");
		}

		// own address type spinner content fill and event handler
		mSpnOwnAddressTypeAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnOwnAddressTypeAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnOwnAddressTypeAdatper.add("Public");
		mSpnOwnAddressTypeAdatper.add("Random");
		if (null != mSpnOwnAddressType) {
			mSpnOwnAddressType.setAdapter(mSpnOwnAddressTypeAdatper);
			mSpnOwnAddressType
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							Xlog.i(TAG, "item id = " + arg2);
							mSpnOwnAddressTypeValue = (byte) arg2;
						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			Xlog.i(TAG, "findViewById(R.id.BLEOwnAddressTypeSpinner) failed");
		}

		// direct address type spinner content fill and event handler
		mSpnDirectAddressTypeAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnDirectAddressTypeAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnDirectAddressTypeAdatper.add("Public");
		mSpnDirectAddressTypeAdatper.add("Random");
		if (null != mSpnDirectAddressType) {
			mSpnDirectAddressType.setAdapter(mSpnDirectAddressTypeAdatper);
			mSpnDirectAddressType
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							Xlog.i(TAG, "item id = " + arg2);
							mSpnDirectAddressTypeValue = (byte) arg2;
						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			Xlog
					.i(TAG,
							"findViewById(R.id.BLEDirectAddressTypeSpinner) failed");
		}

		// filter policy spinner content fill and event handler
		mSpnFilterPolicyAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnFilterPolicyAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnFilterPolicyAdatper.add("All Allow");
		mSpnFilterPolicyAdatper.add("Scan Request From White List");
		mSpnFilterPolicyAdatper.add("Connection Request From White List");
		mSpnFilterPolicyAdatper.add("Both From White List");
		if (null != mSpnFilterPolicy) {
			mSpnFilterPolicy.setAdapter(mSpnDirectAddressTypeAdatper);
			mSpnFilterPolicy
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							Xlog.i(TAG, "item id = " + arg2);
							mSpnFilterPolicyValue = (byte) arg2;
						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			Xlog.i(TAG, "findViewById(R.id.BLEFilterPolicySpinner) failed");
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
				case SETADVERTISEFINISH:
					setViewState(false);
					break;
				case CHKCHANNEL137:
					mCheckChannel37.setChecked(true);

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

	/*
	 * set view to state(true/false --> enable(stop btn pressed)/disable(start
	 * btn pressed)) enable -stop btn pressed disable -start btn pressed
	 */
	private void setViewState(boolean state) {

		mBtnStop.setEnabled(state);
		mBtnSet.setEnabled(!state);
		mBtnStart.setEnabled(!state);
		mSpnAdvertiseType.setEnabled(!state);
		mSpnOwnAddressType.setEnabled(!state);
		mSpnDirectAddressType.setEnabled(!state);
		mSpnFilterPolicy.setEnabled(!state);
		mEditIntervalMin.setEnabled(!state);
		mEditIntervalMax.setEnabled(!state);
		mEditDirectAddress.setEnabled(!state);
		mEditAdvertiseData.setEnabled(!state);
		mCheckChannel37.setEnabled(!state);
		mCheckChannel38.setEnabled(!state);
		mCheckChannel39.setEnabled(!state);

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
			mBtnStop.setEnabled(false);
			workThreadHandler.sendEmptyMessage(TESTSTOP);
			/*
			 * handleStopBtnClick(); setViewState(false);
			 */
		} else if (v.equals(mBtnSet)) {
			setViewState(true);
			workThreadHandler.sendEmptyMessage(SETADVERTISE);

			/*
			 * handleSetBtnClick();
			 */
		} else if (v.equals(mCheckChannel37)) {
			handleCheckBox37Click();
		} else if (v.equals(mCheckChannel38)) {
			handleCheckBox38Click();
		} else if (v.equals(mCheckChannel39)) {
			handleCheckBox39Click();
		} else {
			Xlog.i(TAG, "no view matches current view");
		}

	}

	public boolean handleStartBtnClick() {
		Xlog.v(TAG, "-->handleStartBtnClick");
		/*
		 * HCI CMD & Event For Set Advertise Parameter & Start To Advertise
		 * (1/2) If pressing "Start" button Tx: 01 06 20 0F XX YY UU VV ZZ KK JJ
		 * FF EE DD CC BB AA PP QQ 0xYYXX = Advertising Interval Min (0x0020 ~
		 * 0x4000) 0xVVUU = Advertising Interval Max (0x0020 ~ 0x4000) 0xZZ =
		 * Advertising Type (00 = ADV_IND, 01 = ADV_DIRECT_IND, 02 =
		 * ADV_DISCOVER_IND, 03 = ADV_NONCONN_IND) 0xKK = Own Address Type (00 =
		 * Public Device Address, 01 = Random Device Address) 0xJJ = Direct
		 * Address Type (The same definition as Own Address Type) 0xPP = Channel
		 * 37 indicate 0x01, Channel 38 indicate 0x02, Channel 39 indicate 0x04
		 * (At least one channel should be selected) 0xQQ = Advertise Filter
		 * Policy (00 = All Allow, 01 = Scan Request From White List, 02 =
		 * Connection Request From White List, 03 = Both From White List) Rx: 04
		 * 0E 04 01 08 20 00 Tx: 01 0A 20 01 01 Rx: 04 0E 04 01 0A 20 00
		 */
		long mIntervalMinValue = 0x00;
		long mIntervalMaxValue = 0x00;
		long mChannelValue = 0x00;
		char[] response = null;
		int cmdLen = 19;
		char[] cmd = new char[cmdLen];
		CharSequence cData = mEditDirectAddress.getText();
		CharSequence subCData = null;
		int charSLen = cData.length();
		int dataLen = (charSLen + 1) / 2;
		int i = 0;
		String s = null;

		if (dataLen != 6) {
			// Toast.makeText(BLE_Advertise.this, "invalid MAC address",
			// Toast.LENGTH_SHORT).show();
			new AlertDialog.Builder(this).setCancelable(false)
					.setTitle("Error").setMessage("invalid MAC address")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// finish();
								}
							}).show();
			return false;
		}

		try {
			mIntervalMinValue = (char) Long.parseLong(mEditIntervalMin
					.getText().toString(), 16);
			mIntervalMaxValue = (char) Long.parseLong(mEditIntervalMax
					.getText().toString(), 16);
		} catch (Exception e) {
			// Toast.makeText(BLE_Advertise.this, "invalid input value",
			// Toast.LENGTH_SHORT).show();
			new AlertDialog.Builder(this).setCancelable(false)
					.setTitle("Error").setMessage("invalid input value")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// finish();
								}
							}).show();

			return false;
		}

		for (i = 0; i < dataLen; i++) {
			try {
				subCData = cData.subSequence(2 * i,
						2 * i + 2 > charSLen ? charSLen : 2 * i + 2);
				cmd[16 - i] = (char) Long.parseLong(subCData.toString(), 16);
			} catch (Exception e) {
				// Toast.makeText(BLE_Advertise.this, "invalid MAC address",
				// Toast.LENGTH_SHORT).show();
				new AlertDialog.Builder(this).setCancelable(false).setTitle(
						"Error").setMessage("invalid MAC address")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										// finish();
									}
								}).show();
				return false;
			}
			s = String.format("cmd[%d] = 0x%02x", 16 - i, (long) cmd[16 - i]);
			Xlog.i(TAG, s);
		}

		cmd[0] = 0x01;
		cmd[1] = 0x06;
		cmd[2] = 0x20;
		cmd[3] = 0x0F;
		mIntervalMinValue = mIntervalMinValue < 0x0020 ? 0x0020
				: mIntervalMinValue;
		mIntervalMinValue = mIntervalMinValue > 0x4000 ? 0x4000
				: mIntervalMinValue;
		mIntervalMaxValue = mIntervalMaxValue < 0x0020 ? 0x0020
				: mIntervalMaxValue;
		mIntervalMaxValue = mIntervalMaxValue > 0x4000 ? 0x4000
				: mIntervalMaxValue;

		cmd[4] = (char) (mIntervalMinValue & 0x00FF);
		cmd[5] = (char) ((mIntervalMinValue & 0xFF00) >> 8);
		cmd[6] = (char) (mIntervalMaxValue & 0x00FF);
		cmd[7] = (char) ((mIntervalMaxValue & 0xFF00) >> 8);

		cmd[8] = (char) mSpnAdvertiseTypeValue;
		cmd[9] = (char) mSpnOwnAddressTypeValue;
		cmd[10] = (char) mSpnDirectAddressTypeValue;

		// check the channel37~channel39 state
		mChannelValue = 0x00;
		mChannelValue |= (mCheckChannel37.isChecked() ? 1 : 0);
		mChannelValue |= (mCheckChannel38.isChecked() ? 2 : 0);
		mChannelValue |= (mCheckChannel39.isChecked() ? 4 : 0);
		if (mChannelValue == 0x0) {
			mChannelValue = 0x1;
			// mCheckChannel37.setChecked(true);
			mainHandler.sendEmptyMessage(CHKCHANNEL137);
		}

		cmd[17] = (char) mChannelValue;
		cmd[18] = (char) mSpnFilterPolicyValue;

		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {

			for (i = 0; i < response.length; i++) {
				s = String.format("response[%d] = 0x%02x", i,
						(long) response[i]);
				Xlog.i(TAG, s);
			}
		} else {
			Xlog.i(TAG, "BtTest_HCICommandRun failed");
			return false;
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;

		cmdLen = 5;
		cmd[0] = 0x01;
		cmd[1] = 0x0A;
		cmd[2] = 0x20;
		cmd[3] = 0x01;
		cmd[4] = 0x01;
		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {
			for (i = 0; i < response.length; i++) {
				s = String.format("response[%d] = 0x%x", i, (long) response[i]);
				Xlog.i(TAG, s);
			}
		} else {
			Xlog.i(TAG, "HCICommandRun failed");
			return false;
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;
		return true;

	}

	public void handleStopBtnClick() {
		Xlog.v(TAG, "-->handleStopBtnClick");
		/*
		 * If pressing "Stop" button Tx: 01 0A 20 01 00 Rx: 04 0E 04 01 0A 20 ??
		 * Do not care the status
		 */
		int cmdLen = 5;
		char[] cmd = new char[cmdLen];
		char[] response = null;
		String s = null;
		int i = 0;

		cmd[0] = 0x01;
		cmd[1] = 0x0A;
		cmd[2] = 0x20;
		cmd[3] = 0x01;
		cmd[4] = 0x00;
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

	public void handleSetBtnClick() {
		Xlog.v(TAG, "-->handleSetBtnClick");
		/*
		 * HCI CMD & Event For Set Advertise Data If pressing
		 * "Set Advertise Data" button Tx: 01 08 20 0A XX 11 22 33 44 55 66 77
		 * 88 99 Rx: 04 0E 04 01 08 20 00 XX = Length of data & XX must be 0 ~
		 * 31 In this example XX = 09
		 */
		int cmdLen = 0;
		char[] cmd = null;
		char[] response = null;
		String s = null;
		int i = 0;
		CharSequence cData = mEditAdvertiseData.getText();
		CharSequence subCData = null;
		int charSLen = cData.length();
		int dataLen = (charSLen + 1) / 2;
		if (dataLen == 0) {
			Toast.makeText(BLE_Advertise.this, "invalid advertise data",
					Toast.LENGTH_SHORT).show();
			return;
		}
		cmdLen = 5 + dataLen;
		cmd = new char[cmdLen];
		for (i = 0; i < dataLen; i++) {
			try {
				subCData = cData.subSequence(2 * i,
						2 * i + 2 > charSLen ? charSLen : 2 * i + 2);
				cmd[5 + i] = (char) Long.parseLong(subCData.toString(), 16);
			} catch (Exception e) {
				// Toast.makeText(BLE_Advertise.this, "invalid MAC address",
				// Toast.LENGTH_SHORT).show();
				new AlertDialog.Builder(this).setCancelable(false).setTitle(
						"Error").setMessage("invalid MAC address")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										// finish();
									}
								}).show();
				return;
			}
			s = String.format("cmd[%d] = 0x%02x", 5 + i, (long) cmd[5 + i]);
			Xlog.i(TAG, s);
		}

		cmd[0] = 0x01;
		cmd[1] = 0x08;
		cmd[2] = 0x20;
		cmd[3] = (char) (dataLen + 1);
		cmd[4] = (char) dataLen;
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

	public void handleCheckBox37Click() {
		Xlog.v(TAG, "-->handleCheckBox37Click");
	}

	public void handleCheckBox38Click() {
		Xlog.v(TAG, "-->handleCheckBox38Click");
	}

	public void handleCheckBox39Click() {
		Xlog.v(TAG, "-->handleCheckBox39Click");
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
			Xlog.i(TAG, "WorkThread begins, thread ID = " + getId());
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
					case SETADVERTISE:
						if (initBtTestOjbect()) {
							handleSetBtnClick();
						}
						mainHandler.sendEmptyMessage(SETADVERTISEFINISH);
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
