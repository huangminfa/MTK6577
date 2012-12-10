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
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Message;
import android.os.Handler;
import java.lang.Thread;
import android.os.HandlerThread;
import android.os.Looper;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

public class NoSigRxTestActivity extends Activity implements
		DialogInterface.OnCancelListener, OnClickListener {

	private BluetoothAdapter mAdapter;

	private Spinner sPattern;
	private Spinner sPocketType;
	private EditText edFreq;
	private EditText edAddr;

	private Button btnStartTest;

	private TextView packCnt;
	private TextView packErrRate;
	private TextView rxByteCnt;
	private TextView bitErrRate;
	public static final int TEST_STATUS_BEGIN = 100;
	public static final int TEST_STATUS_RESULT = 101;
	int[] result = null; //
	private int mTestStatus = TEST_STATUS_BEGIN;

	private static final String TAG = "NoSigRx";

	private int stateBt;

	private static Handler ghDoneBthAction = null; // used to handle the "done"
	// button clicked action
	private boolean gbIsDoneHandleFinished = true; // used to record weather the
	// "done" button clicked
	// event has been finished
	private HandlerThread handlerThread = null;
	public static final int OP_IN_PROCESS = 2;
	public static final int OP_FINISH = 0;
	public static final int OP_ADDR_DEFAULT = 11;
	public static final int OP_TEST_OK_STEP1 = 12;
	public static final int OP_TEST_OK_STEP2 = 13;
	private static Handler mainHandler = null;
	ProgressDialog mDialogSearchProgress = null;

	// end added
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rx_nosig_test);
		SetValuesSpinner();
		edFreq = (EditText) findViewById(R.id.NSRX_editFrequency);
		edAddr = (EditText) findViewById(R.id.NSRX_editTesterAddr);
		btnStartTest = (Button) findViewById(R.id.NSRX_StartTest);
		packCnt = (TextView) findViewById(R.id.NSRX_StrPackCnt);
		packErrRate = (TextView) findViewById(R.id.NSRX_StrPackErrRate);
		rxByteCnt = (TextView) findViewById(R.id.NSRX_StrPackByteCnt);
		bitErrRate = (TextView) findViewById(R.id.NSRX_StrBitErrRate);
		if (edFreq == null || edAddr == null || btnStartTest == null
				|| packCnt == null || packErrRate == null || rxByteCnt == null
				|| bitErrRate == null) {
			Xlog.w(TAG, "Fatal Error. Clokwork ignore.");
			return;
		}
		btnStartTest.setOnClickListener(this);

		handlerThread = new HandlerThread("doneHandler");
		if (null != handlerThread) {
			handlerThread.start();
		} else {
			Xlog.w(TAG, "new HandlerThread failed");
		}
		Looper looperInHandlerThread = handlerThread.getLooper();
		if (null != looperInHandlerThread) {
			ghDoneBthAction = new Handler(looperInHandlerThread);
		} else {
			Xlog.w(TAG, "handlerThread.getLooper() failed");
		}
		mainHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case OP_IN_PROCESS: {
					System.out.println("OP_IN_PROCESS");
					if (null == mDialogSearchProgress) {
						mDialogSearchProgress = new ProgressDialog(
								NoSigRxTestActivity.this);
						if (null != mDialogSearchProgress) {
							mDialogSearchProgress
									.setProgressStyle(ProgressDialog.STYLE_SPINNER);
							String text = "test operation in process, please wait ...";
							mDialogSearchProgress.setMessage(text);
							mDialogSearchProgress.setTitle("Bluetooth RX");
							mDialogSearchProgress.setCancelable(false);
							mDialogSearchProgress.show();
						} else {
							Xlog.w(TAG, "new mDialogSearchProgress failed");
						}
					}
				}
					break;
				case OP_FINISH:
					System.out.println("OP_FINISH");
					if (null != mDialogSearchProgress) {
						mDialogSearchProgress.dismiss();
						mDialogSearchProgress = null;
					}
					break;
				case OP_ADDR_DEFAULT:
					edAddr.setText("A5F0C3");
					break;
				case OP_TEST_OK_STEP1:
					mTestStatus = TEST_STATUS_RESULT;
					btnStartTest.setText("End Test");
					break;
				case OP_TEST_OK_STEP2:
					packCnt.setText(String.valueOf(result[0]));
					packErrRate.setText(String
							.format("%.2f", result[1] / 100.0)
							+ "%");
					rxByteCnt.setText(String.valueOf(result[2]));
					bitErrRate.setText(String.format("%.2f", result[3] / 100.0)
							+ "%");
					mTestStatus = TEST_STATUS_BEGIN;
					btnStartTest.setText("Start");
					break;
				default:
					break;
				}
			}
		};

	}

	private class myRunnable implements Runnable {
		public Handler mHandler;

		public void run() {
			mainHandler.sendEmptyMessage(OP_IN_PROCESS);
			gbIsDoneHandleFinished = false;
			doSendCommandAction();
			gbIsDoneHandleFinished = true;
			mainHandler.sendEmptyMessage(OP_FINISH);
		}
	}

	protected void SetValuesSpinner() {
		// for TX pattern
		sPattern = (Spinner) findViewById(R.id.NSRX_PatternSpinner);
		ArrayAdapter<CharSequence> adapterPattern = ArrayAdapter
				.createFromResource(this, R.array.nsrx_pattern,
						android.R.layout.simple_spinner_item);
		adapterPattern
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		if (null != sPattern) {
			sPattern.setAdapter(adapterPattern);
		} else {
			Xlog.w(TAG, "findViewById(R.id.PatternSpinner) failed");
		}

		// for TX pocket type
		sPocketType = (Spinner) findViewById(R.id.NSRX_PocketTypeSpinner);
		ArrayAdapter<CharSequence> adapterPocketType = ArrayAdapter
				.createFromResource(this, R.array.nsrx_Pocket_type,
						android.R.layout.simple_spinner_item);
		adapterPocketType
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		if (null != sPocketType) {
			sPocketType.setAdapter(adapterPocketType);
		} else {
			Xlog.w(TAG, "findViewById(R.id.PocketTypeSpinner) failed");
		}
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (gbIsDoneHandleFinished) {
			// if the last click action has been handled, send another event
			// request
			packCnt.setText("x");
			packErrRate.setText("x");
			rxByteCnt.setText("x");
			bitErrRate.setText("x");
			ghDoneBthAction.post(new myRunnable());
		} else {
			System.out.println("last click is not finished yet.");
		}
	}

	/**
	 * Send command the user has made, and finish the activity.
	 */
	private boolean doSendCommandAction() {
		if (mTestStatus == TEST_STATUS_BEGIN) {
			GetBtState();
			EnableBluetooth(false);
			GetValuesAndSend();
		} else if (mTestStatus == TEST_STATUS_RESULT) {
			getResult();
		}

		return true;
	}

	// implemented for DialogInterface.OnCancelListener
	public void onCancel(DialogInterface dialog) {
		// request that the service stop the query with this callback object.
		Xlog.v(TAG, "-->onCancel");
		finish();
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
	protected void onStop() {
		Xlog.v(TAG, "before handlerThread quit.");
		handlerThread.quit();
		handlerThread = null;
		Xlog.v(TAG, "after  handlerThread quit.");
		super.onStop();
		finish();
	}

	@Override
	protected void onDestroy() {
		Xlog.v(TAG, "TXOnlyTest onDestroy.");
		super.onDestroy();
		if (null != mDialogSearchProgress) {
			mDialogSearchProgress.dismiss();
			mDialogSearchProgress = null;
		}
		// mDialogSearchProgress = null;

	}

	private void GetBtState() {
		Xlog.v(TAG, "Enter GetBtState().");
		BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
		if (null == ba) {
			Xlog.v(TAG, "we can not find a bluetooth adapter.");
			Toast.makeText(getApplicationContext(),
					"We can not find a bluetooth adapter.", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		stateBt = ba.getState();
		Xlog.v(TAG, "Leave GetBtState().");
	}

	private void EnableBluetooth(boolean enable) {
		Xlog.v(TAG, "Enter EnableBluetooth().");
		BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
		if (null == ba) {
			Xlog.v(TAG, "we can not find a bluetooth adapter.");
			return;
		}

		if (enable) // need to enable
		{
			Xlog.v(TAG, "Bluetooth is enabled");
			ba.enable();
		} else // need to disable
		{
			Xlog.v(TAG, "Bluetooth is disabled");
			ba.disable();
		}
		Xlog.v(TAG, "Leave EnableBluetooth().");
	}

	private BtTest obj = null;

	public void GetValuesAndSend() {
		Xlog.v(TAG, "Enter GetValuesAndSend().");
		obj = new BtTest();
		if (obj == null) {
			Xlog.v(TAG, "We cannot find BtTest object.");
			return;
		}
		int nPatternIdx = sPattern.getSelectedItemPosition();
		int nPocketTypeIdx = sPocketType.getSelectedItemPosition();
		int nFreq = 0;
		int nAddress = 0;
		try {
            		nFreq = Integer.valueOf(edFreq.getText().toString());
            		long tmp = Long.valueOf(edAddr.getText().toString(), 16);
            		nAddress = (int) tmp;
//            	 	 nAddress = Integer.valueOf(edAddr.getText().toString(), 16);
			if (nFreq < 0 || nFreq > 78) {
				Toast.makeText(getApplicationContext(),
						"Error: Frequency error, must be 0-78.",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (nAddress == 0) {
				nAddress = 0xA5F0C3;
				mainHandler.sendEmptyMessage(OP_ADDR_DEFAULT);
			}
		} catch (NumberFormatException e) {
			Xlog.i(TAG, "input number error!");
			return;
		}

		// send command to....
		boolean rc = obj.NoSigRxTestStart(nPatternIdx, nPocketTypeIdx, nFreq,
				nAddress);
		if (!rc) {
			Xlog.i(TAG, "no signal rx test failed.");
			if ((BluetoothAdapter.STATE_TURNING_ON == stateBt)
					|| (BluetoothAdapter.STATE_ON == stateBt)) {
				EnableBluetooth(true);
			}
			// Toast.makeText(getApplicationContext(),
			// "no signal rx test failed.",
			// Toast.LENGTH_SHORT).show();
			new AlertDialog.Builder(this).setCancelable(false)
					.setTitle("Error").setMessage("no signal rx test failed.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// finish();
								}
							}).show();

		} else {
			mainHandler.sendEmptyMessage(OP_TEST_OK_STEP1);

		}

		Xlog.i(TAG, "Leave GetValuesAndSend().");
	}

	private void getResult() {
		if (obj == null) {
			return;
		}

		result = obj.NoSigRxTestResult();
		if (result == null) {
			Xlog.i(TAG, "no signal rx test failed.");
			if ((BluetoothAdapter.STATE_TURNING_ON == stateBt)
					|| (BluetoothAdapter.STATE_ON == stateBt)) {
				EnableBluetooth(true);
			}
			// Toast.makeText(getApplicationContext(),
			// "no signal rx test failed.",
			// Toast.LENGTH_SHORT).show();
			new AlertDialog.Builder(this).setCancelable(false)
					.setTitle("Error").setMessage("no signal rx test failed.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// finish();
								}
							}).show();

		} else {
			mainHandler.sendEmptyMessage(OP_TEST_OK_STEP2);

		}

		Xlog.i(TAG, "Leave getresult().");
	}

}
