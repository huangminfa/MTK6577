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

public class BLE_Initiate extends Activity implements OnClickListener {

	private BluetoothAdapter mAdapter;

	private final String TAG = "BLEInitiate";

	// views in this activity
	// edit text
	private EditText mEditScanInterval = null;
	private EditText mEditScanWindow = null;
	private EditText mEditPeerAddress = null;
	private EditText mEditIntervalMin = null;
	private EditText mEditIntervalMax = null;
	private EditText mEditLatency = null;
	private EditText mEditSupvTimeout = null;
	private EditText mEditMinCELength = null;
	private EditText mEditMaxCELength = null;

	// spinner
	private Spinner mSpnFilter = null;
	private Spinner mSpnPeerAddressType = null;
	private Spinner mSpnOwnAddressType = null;

	// button
	private Button mBtnStart = null;
	private Button mBtnStop = null;

	// spinner values
	private byte mSpnFilterValue = 0;
	private byte mSpnPeerAddressTypeValue = 0;
	private byte mSpnOwnAddressTypeValue = 0;

	// adapters for spinner
	private ArrayAdapter<String> mSpnFilterAdapter;
	private ArrayAdapter<String> mSpnPeerAddressTypeAdapter;
	private ArrayAdapter<String> mSpnOwnAddressTypeAdapter;

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
	private static final int ACTIVITYEXIT = 9;

	@Override
	protected void onCreate(Bundle onSavedInstanceState) {
		Xlog.v(TAG, "-->onCreate");
		super.onCreate(onSavedInstanceState);
		setContentView(R.layout.ble_normal_mode_initiate);
		mEditScanInterval = (EditText) findViewById(R.id.BLEInitiateScanInterval);
		mEditScanWindow = (EditText) findViewById(R.id.BLEInitiateScanWindow);
		mEditPeerAddress = (EditText) findViewById(R.id.BLEPeerAddress);
		mEditIntervalMin = (EditText) findViewById(R.id.BLEInitiateIntervalMin);
		mEditIntervalMax = (EditText) findViewById(R.id.BLEInitiateIntervalMax);
		mEditLatency = (EditText) findViewById(R.id.BLELatency);
		mEditSupvTimeout = (EditText) findViewById(R.id.BLESupervisionTimeout);
		mEditMinCELength = (EditText) findViewById(R.id.BLEMinCELength);
		mEditMaxCELength = (EditText) findViewById(R.id.BLEMaxCELength);
		if (mEditScanInterval == null || mEditScanWindow == null
				|| mEditPeerAddress == null || mEditIntervalMin == null
				|| mEditIntervalMax == null || mEditLatency == null
				|| mEditSupvTimeout == null || mEditMinCELength == null
				|| mEditMaxCELength == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

//		mEditScanInterval.setText("12AB");
//		mEditScanWindow.setText("12AB");
//		mEditPeerAddress.setText("112233445566");
//		mEditIntervalMin.setText("20");
//		mEditIntervalMax.setText("100");
//		mEditLatency.setText("AB");
//		mEditSupvTimeout.setText("1C");
//		mEditMinCELength.setText("AABB");
//		mEditMaxCELength.setText("BBAA");
		
		mEditScanInterval.setText("0010");
		mEditScanWindow.setText("0008");
		mEditPeerAddress.setText("112233445566");
		mEditIntervalMin.setText("0050");
		mEditIntervalMax.setText("0050");
		mEditLatency.setText("0000");
		mEditSupvTimeout.setText("0140");
		mEditMinCELength.setText("0050");
		mEditMaxCELength.setText("0050");

		mSpnFilter = (Spinner) findViewById(R.id.BLEInitiateFilterSpinner);
		mSpnPeerAddressType = (Spinner) findViewById(R.id.BLEInitiatePeerAddressTypeSpinner);
		mSpnOwnAddressType = (Spinner) findViewById(R.id.BLEInitiateOwnAddressTypeSpinner);

		mBtnStart = (Button) findViewById(R.id.BLEInitiateStart);
		mBtnStop = (Button) findViewById(R.id.BLEInitiateStop);
		if (mSpnFilter == null || mSpnPeerAddressType == null
				|| mSpnOwnAddressType == null || mBtnStart == null
				|| mBtnStop == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

		mBtnStart.setOnClickListener(this);
		mBtnStop.setOnClickListener(this);

		// filll "Initiate Filter Policy " content and action handler set
		mSpnFilterAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnFilterAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnFilterAdapter.add("White List Is Not Used");
		mSpnFilterAdapter.add("White List Is Used");
		if (null != mSpnFilter) {
			mSpnFilter.setAdapter(mSpnFilterAdapter);
			mSpnFilter.setOnItemSelectedListener(new OnItemSelectedListener() {

				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					Xlog.v(TAG, "item id = " + arg2);
					mSpnFilterValue = (byte) arg2;
				}

				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
				}
			});
		} else {
			Xlog.i(TAG, "findViewById(R.id.BLEInitiateFilterSpinner) failed");
		}

		// filll "Peer Address Type  " content and action handler set
		mSpnPeerAddressTypeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnPeerAddressTypeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnPeerAddressTypeAdapter.add("Public");
		mSpnPeerAddressTypeAdapter.add("Random");
		if (null != mSpnPeerAddressType) {
			mSpnPeerAddressType.setAdapter(mSpnPeerAddressTypeAdapter);
			mSpnPeerAddressType
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							Xlog.v(TAG, "item id = " + arg2);
							mSpnPeerAddressTypeValue = (byte) arg2;
						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			Xlog
					.i(TAG,
							"findViewById(R.id.BLEInitiatePeerAddressTypeSpinner) failed");
		}

		// filll "Owner Address Type  " content and action handler set
		mSpnOwnAddressTypeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mSpnOwnAddressTypeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnOwnAddressTypeAdapter.add("Public");
		mSpnOwnAddressTypeAdapter.add("Random");
		if (null != mSpnOwnAddressType) {
			mSpnOwnAddressType.setAdapter(mSpnOwnAddressTypeAdapter);
			mSpnOwnAddressType
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							Xlog.v(TAG, "item id = " + arg2);
							mSpnOwnAddressTypeValue = (byte) arg2;
						}

						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			Xlog
					.i(TAG,
							"findViewById(R.id.BLEInitiateOwnAddressTypeSpinner) failed");
		}
		setViewState(false);
		mainHandler = new Handler() {
			// @Override
			public void handleMessage(Message msg) {
				Xlog.v(TAG, "-->main Handler - handleMessage msg.what="+msg.what);
				switch (msg.what) {
				case STARTTESTSUCCESS:
					Xlog.v(TAG, "-->main Handler - handleMessage STARTTESTSUCCESS");
					mbIsTestStared = true;
					break;
				case STARTTESTFAILED:
					// here we can give some notification
					Xlog.v(TAG, "-->main Handler - handleMessage STARTTESTFAILED");
					mbIsTestStared = false;
					setViewState(false);
					break;
				case STOPFINISH:
					Xlog.v(TAG, "-->main Handler - handleMessage STOPFINISH");
					mbIsTestStared = false;
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
			Xlog.v(TAG, "-->onClick mBtnStart ");
			setViewState(true);
			workThreadHandler.sendEmptyMessage(TESTSTART);
			/*
			 * if(!handleStartBtnClick()) { setViewState(false); }
			 */
		} else if (v.equals(mBtnStop)) {
			Xlog.v(TAG, "-->onClick mBtnStop ");
//			mBtnStop.setEnabled(false);
			setViewState(false);
			workThreadHandler.sendEmptyMessage(TESTSTOP);
			/*
			 * handleStopBtnClick(); setViewState(false);
			 */
		} else {
			Xlog.i(TAG, "no view matches current view");
		}
	}

	public boolean handleStartBtnClick() {
		Xlog.v(TAG, "-->handleStartBtnClick");
		/*
		 * HCI CMD & Event For Start To Initiate (1/2) If pressing "Start"
		 * button Tx: 01 0D 20 19 XX YY UU VV ZZ KK 66 55 44 33 22 11 JJ PP QQ
		 * RR SS MM NN GG HH LL TT OO WW 0xYYXX = LE Scan Interval (0x0004 ~
		 * 0x4000) 0xVVUU = LE Scan Window (0x0004 ~ 0x4000) 0xZZ = Initiate
		 * Filter Policy (00 = White List Is Not Used, 01 = White List Is Used)
		 * 0xKK = Peer Address Type (00 = Public Device Address, 01 = Random
		 * Device Address) 0xJJ = Own Address Type (The same definition as Peer
		 * Address Type) 0xQQPP = Connection Interval Min (0x0006 ~ 0x0C80)
		 * 0xSSRR = Connection Interval Max (0x0006 ~ 0x0C80) 0xNNMM =
		 * Connection Latency (0x0000 ~ 0x01F4) 0xHHGG = Supervision Timeout
		 * (0x000A ~ 0x0C80) 0xTTLL = Minimum CE Length (0x0000~0xFFFF) 0xWWOO =
		 * Maximum CE Length (0x0000~0xFFFF) Rx: 04 0F 04 00 01 0D 20 Rx: 04 3E
		 * 13 01 ?? ?? ?? ?? ?? 66 55 44 33 22 11 ?? ?? ?? ?? ?? ?? ??
		 */
		int cmdLen = 29;
		char[] cmd = new char[cmdLen];
		char[] response = null;
		CharSequence cData = mEditPeerAddress.getText();
		CharSequence subCData = null;
		int charSLen = cData.length();
		int dataLen = (charSLen + 1) / 2;
		int i = 0;
		String s = null;
		long mScanIntervalValue = 0x0;
		long mScanWindowValue = 0x0;
		long mConIntervalMinValue = 0x0;
		long mConIntervalMaxValue = 0x0;
		long mConLatencyValue = 0x0;
		long mSpvTimeoutValue = 0x0;
		long mCELengthMinValue = 0x0;
		long mCELengthMaxValue = 0x0;

		if (dataLen != 6) {
//			Toast.makeText(BLE_Initiate.this, "invalid MAC address",
//					Toast.LENGTH_SHORT).show();
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

		for (i = 0; i < dataLen; i++) {
			try {
				subCData = cData.subSequence(2 * i,
						2 * i + 2 > charSLen ? charSLen : 2 * i + 2);
				cmd[15 - i] = (char) Long.parseLong(subCData.toString(), 16);
			} catch (Exception e) {
//				Toast.makeText(BLE_Initiate.this, "invalid MAC address",
//						Toast.LENGTH_SHORT).show();
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
			s = String.format("cmd[%d] = 0x%02x", 15 - i, (long) cmd[15 - i]);
			Xlog.v(TAG, s);
		}

		try {
			mScanIntervalValue = (char) Long.parseLong(mEditScanInterval
					.getText().toString(), 16);
			mScanWindowValue = (char) Long.parseLong(mEditScanWindow.getText()
					.toString(), 16);
			mConIntervalMinValue = (char) Long.parseLong(mEditIntervalMin
					.getText().toString(), 16);
			mConIntervalMaxValue = (char) Long.parseLong(mEditIntervalMax
					.getText().toString(), 16);
			mConLatencyValue = (char) Long.parseLong(mEditLatency.getText()
					.toString(), 16);
			mSpvTimeoutValue = (char) Long.parseLong(mEditSupvTimeout.getText()
					.toString(), 16);
			mCELengthMinValue = (char) Long.parseLong(mEditMinCELength
					.getText().toString(), 16);
			mCELengthMaxValue = (char) Long.parseLong(mEditMaxCELength
					.getText().toString(), 16);
		} catch (Exception e) {
//			Toast.makeText(BLE_Initiate.this, "invalid input value",
//					Toast.LENGTH_SHORT).show();
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

		cmd[0] = 0x01;
		cmd[1] = 0x0D;
		cmd[2] = 0x20;
		cmd[3] = 0x19;
		/*
		 * 0xYYXX = LE Scan Interval (0x0004 ~ 0x4000) 0xVVUU = LE Scan Window
		 * (0x0004 ~ 0x4000) 0xZZ = Initiate Filter Policy (00 = White List Is
		 * Not Used, 01 = White List Is Used) 0xKK = Peer Address Type (00 =
		 * Public Device Address, 01 = Random Device Address) 0xJJ = Own Address
		 * Type (The same definition as Peer Address Type) 0xQQPP = Connection
		 * Interval Min (0x0006 ~ 0x0C80) 0xSSRR = Connection Interval Max
		 * (0x0006 ~ 0x0C80) 0xNNMM = Connection Latency (0x0000 ~ 0x01F4)
		 * 0xHHGG = Supervision Timeout (0x000A ~ 0x0C80) 0xTTLL = Minimum CE
		 * Length (0x0000~0xFFFF) 0xWWOO = Maximum CE Length (0x0000~0xFFFF)
		 */
		mScanIntervalValue = mScanIntervalValue < 0x0004 ? 0x0004
				: mScanIntervalValue;
		mScanIntervalValue = mScanIntervalValue > 0x4000 ? 0x4000
				: mScanIntervalValue;
		cmd[4] = (char) (mScanIntervalValue & 0xff);
		cmd[5] = (char) ((mScanIntervalValue & 0xff00) >> 8);

		mScanWindowValue = mScanWindowValue < 0x0004 ? 0x0004
				: mScanWindowValue;
		mScanWindowValue = mScanWindowValue > 0x4000 ? 0x4000
				: mScanWindowValue;
		cmd[6] = (char) (mScanWindowValue & 0xff);
		cmd[7] = (char) ((mScanWindowValue & 0xff00) >> 8);

		cmd[8] = (char) mSpnFilterValue;
		cmd[9] = (char) mSpnPeerAddressTypeValue;
		// six char data for MAC address
		cmd[16] = (char) mSpnOwnAddressTypeValue;

		mConIntervalMinValue = mConIntervalMinValue < 0x0006 ? 0x0006
				: mConIntervalMinValue;
		mConIntervalMinValue = mConIntervalMinValue > 0x0C80 ? 0x0C80
				: mConIntervalMinValue;
		cmd[17] = (char) (mConIntervalMinValue & 0xff);
		cmd[18] = (char) ((mConIntervalMinValue & 0xff00) >> 8);

		mConIntervalMaxValue = mConIntervalMaxValue < 0x0006 ? 0x0006
				: mConIntervalMaxValue;
		mConIntervalMaxValue = mConIntervalMaxValue > 0x0C80 ? 0x0C80
				: mConIntervalMaxValue;
		cmd[19] = (char) (mConIntervalMaxValue & 0xff);
		cmd[20] = (char) ((mConIntervalMaxValue & 0xff00) >> 8);

		mConLatencyValue = mConLatencyValue < 0x0000 ? 0x0000
				: mConLatencyValue;
		mConLatencyValue = mConLatencyValue > 0x01F4 ? 0x01F4
				: mConLatencyValue;
		cmd[21] = (char) (mConLatencyValue & 0xff);
		cmd[22] = (char) ((mConLatencyValue & 0xff00) >> 8);

		mSpvTimeoutValue = mSpvTimeoutValue < 0x000A ? 0x000A
				: mSpvTimeoutValue;
		mSpvTimeoutValue = mSpvTimeoutValue > 0x0C80 ? 0x0C80
				: mSpvTimeoutValue;
		cmd[23] = (char) (mSpvTimeoutValue & 0xff);
		cmd[24] = (char) ((mSpvTimeoutValue & 0xff00) >> 8);

		cmd[25] = (char) (mCELengthMinValue & 0xff);
		cmd[26] = (char) ((mCELengthMinValue & 0xff00) >> 8);

		cmd[27] = (char) (mCELengthMaxValue & 0xff);
		cmd[28] = (char) ((mCELengthMaxValue & 0xff00) >> 8);

		response = mBT.HCICommandRun(cmd, cmdLen);
		if (response != null) {
			s = null;
			for (i = 0; i < response.length; i++) {
				s = String.format("response[%d] = 0x%x", i, (long) response[i]);
				Xlog.v(TAG, s);
			}
			
			if (response.length > 3){
				if (response[3]!=0x00) {
					new AlertDialog.Builder(this).setCancelable(false)
					.setTitle("Error").setMessage("Wrong parameter: "+String.format("response[%d] = 0x%x", i, (long) response[3]))
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
			}
			
			if (response.length > 3 + response[2]) {
				// this means this response including more than one event data,
				// so we can return true now, the next event read API should not
				// be called
				return true;
			}

		} else {
			Xlog.i(TAG, "HCICommandRun failed");
			return false;
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;

		// the next read event will be blocked until the last comman is in
		// process
		// test for temperialy
		response = mBT.HCIReadEvent();
		if (response != null) {
			s = null;
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

	public void handleStopBtnClick() {
		Xlog.v(TAG, "-->handleStopBtnClick");
		/*
		 * If pressing "Stop" button Tx: 01 0E 20 00 Rx: 04 0E 04 01 0E 20 ?? Do
		 * not care the status
		 */
		int cmdLen = 4;
		char[] cmd = new char[cmdLen];
		char[] response = null;
		int i = 0;
		cmd[0] = 0x01;
		cmd[1] = 0x0E;
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
		}
		// TODO:here we need to judge whether this operation is succeeded or not
		response = null;

	}

	/*
	 * set view to state(true/false --> enable(stop btn pressed)/disable(start
	 * btn pressed)) true - enable -stop btn pressed false - disable -start btn
	 * pressed
	 */
	private void setViewState(boolean state) {
		Xlog.v(TAG, "-->setViewState state="+state);
		mEditScanInterval.setEnabled(!state);
		mEditScanWindow.setEnabled(!state);
		mEditPeerAddress.setEnabled(!state);
		mEditIntervalMin.setEnabled(!state);
		mEditIntervalMax.setEnabled(!state);
		mEditLatency.setEnabled(!state);
		mEditSupvTimeout.setEnabled(!state);
		mEditMinCELength.setEnabled(!state);
		mEditMaxCELength.setEnabled(!state);

		mSpnFilter.setEnabled(!state);
		mSpnPeerAddressType.setEnabled(!state);
		mSpnOwnAddressType.setEnabled(!state);

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
					Xlog.v(TAG, "WorkThread handleMessage, msg.what = " + msg.what + "   mbIsInit = "+mbIsInit);
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
			Xlog.i(TAG, "WorkThread exits");
		}
	}

}
