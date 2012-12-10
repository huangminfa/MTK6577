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
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
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

public class TXOnlyTestActivity extends Activity implements
		DialogInterface.OnCancelListener {

	private BluetoothAdapter mAdapter;

	private Spinner sPattern;
	private Spinner sChannels;
	private Spinner sPocketType;

	private BtTest mBT;

	private boolean mbIsInit = false;
	private boolean mbIsIniting = false;
	private boolean mbIsNonModulate = false;
	private boolean mbIsPocketType = false;

	private static final int MAP_TO_PATTERN = 0;
	private static final int MAP_TO_CHANNELS = 1;
	private static final int MAP_TO_POCKET_TYPE = 2;
	private static final int MAP_TO_FREQ = 3;
	private static final int MAP_TO_POCKET_TYPE_LEN = 4;

	private static final String TAG = "TXOnlyTestActivity";

	private int stateBt;
	// added by chaozhong @2010-10-10
	private static Handler ghDoneBthAction = null; // used to handle the "done"
													// button clicked action
	private boolean gbIsDoneHandleFinished = true; // used to record weather the
													// "done" button clicked
													// event has been finished
	private HandlerThread handlerThread = null;
	public static final int DLGID_OP_IN_PROCESS = 1;
	public static final int OP_IN_PROCESS = 2;
	public static final int OP_FINISH = 0;
	private static Handler mainHandler = null;
	ProgressDialog mDialogSearchProgress = null;

	// end added
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Xlog.v(TAG, "-->onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tx_only_test);
		SetValuesSpinner();
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
								TXOnlyTestActivity.this);
						if (null != mDialogSearchProgress) {
							mDialogSearchProgress
									.setProgressStyle(ProgressDialog.STYLE_SPINNER);
							String text = "test operation in process, please wait ...";
							mDialogSearchProgress.setMessage(text);
							mDialogSearchProgress.setTitle("Bluetooth TX");
							mDialogSearchProgress.setCancelable(false);
							mDialogSearchProgress.show();
						} else {
							Xlog.w(TAG, "new mDialogSearchProgress failed");
						}
					}
				}
					break;
				case OP_FINISH:
					Xlog.i(TAG, "OP_FINISH");
					if (null != mDialogSearchProgress) {
						mDialogSearchProgress.dismiss();
						mDialogSearchProgress = null;
					}
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

	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem doneItem = menu.getItem(Menu.FIRST - 1);
		if (null != doneItem) {
			if (!gbIsDoneHandleFinished) {
				doneItem.setEnabled(false);
				menu.close();
			} else {
				doneItem.setEnabled(true);
			}
		} else {
			Xlog.i(TAG, "menu_done is not found.");
		}
		return true;
	}

	protected void SetValuesSpinner() {
		// for TX pattern
		sPattern = (Spinner) findViewById(R.id.PatternSpinner);
		ArrayAdapter<CharSequence> adapterPattern = ArrayAdapter
				.createFromResource(this, R.array.tx_pattern,
						android.R.layout.simple_spinner_item);
		adapterPattern
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		if (null != sPattern) {
			sPattern.setAdapter(adapterPattern);
		} else {
			Xlog.i(TAG, "findViewById(R.id.PatternSpinner) failed");
		}

		// for TX channels
		sChannels = (Spinner) findViewById(R.id.ChannelsSpinner);
		ArrayAdapter<CharSequence> adapterChannels = ArrayAdapter
				.createFromResource(this, R.array.tx_channels,
						android.R.layout.simple_spinner_item);
		adapterChannels
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		if (null != sChannels) {
			sChannels.setAdapter(adapterChannels);
		} else {
			Xlog.i(TAG, "findViewById(R.id.ChannelsSpinner) failed");
		}

		// for TX pocket type
		sPocketType = (Spinner) findViewById(R.id.PocketTypeSpinner);
		ArrayAdapter<CharSequence> adapterPocketType = ArrayAdapter
				.createFromResource(this, R.array.tx_Pocket_type,
						android.R.layout.simple_spinner_item);
		adapterPocketType
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		if (null != sPocketType) {
			sPocketType.setAdapter(adapterPocketType);
		} else {
			Xlog.i(TAG, "findViewById(R.id.PocketTypeSpinner) failed");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_show, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_done: {
			// edited by chaozhogn @ 2010-10-10
			// return doSendCommandAction();
			System.out.println("menu_done is clicked.");
			if (gbIsDoneHandleFinished) {
				// if the last click action has been handled, send another event
				// request
				ghDoneBthAction.post(new myRunnable());
			} else {
				Xlog.i(TAG, "last click is not finished yet.");
			}
			Xlog.i(TAG, "menu_done is handled.");
			return true;
			// edit end
		}

		case R.id.menu_discard: {
			return doRevertAction();
		}
		}
		return false;
	}

	/**
	 * Send command the user has made, and finish the activity.
	 */
	private boolean doSendCommandAction() {
		GetBtState();
		EnableBluetooth(false);
		GetValuesAndSend();
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
		if(handlerThread != null){
			handlerThread.quit();
			handlerThread = null;
		}

		Xlog.v(TAG, "after  handlerThread quit.");
		super.onStop();
		// finish();
	}

	@Override
	protected void onDestroy() {
		Xlog.v(TAG, "TXOnlyTest onDestroy.");

		if (null != mDialogSearchProgress) {
			mDialogSearchProgress.dismiss();
			mDialogSearchProgress = null;
		}

		if (obj != null) {
			if (-1 == obj.doBtTest(3)) {
				Xlog.i(TAG, "stop failed.");

			}
			obj = null;
		} else {
			Xlog.i(TAG, "BtTest does not start yet.");
		}

		super.onDestroy();
		// mDialogSearchProgress = null;

	}

	private void GetBtState() {
		Xlog.v(TAG, "Enter GetBtState().");
		BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
		if (null == ba) {
			Xlog.i(TAG, "we can not find a bluetooth adapter.");
			Toast.makeText(getApplicationContext(),
					"We can not find a bluetooth adapter.", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		stateBt = ba.getState();
		Log.e(TAG, "Leave GetBtState().");
	}

	private void EnableBluetooth(boolean enable) {
		Xlog.v(TAG, "Enter EnableBluetooth().");
		BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
		if (null == ba) {
			Xlog.i(TAG, "we can not find a bluetooth adapter.");
			return;
		}

		if (enable) // need to enable
		{
			Xlog.i(TAG, "Bluetooth is enabled");
			ba.enable();
		} else // need to disable
		{
			Xlog.i(TAG, "Bluetooth is disabled");
			ba.disable();
		}
		Xlog.i(TAG, "Leave EnableBluetooth().");
	}

	/**
	 * Revert any changes the user has made, and finish the activity.
	 */
	private boolean doRevertAction() {
		finish();
		return true;
	}

	private BtTest obj = null;

	public void GetValuesAndSend() {
		Xlog.i(TAG, "Enter GetValuesAndSend().");
		obj = new BtTest();
		if (obj != null) {
			GetSpinnerValue(sPattern, MAP_TO_PATTERN, obj);
			GetSpinnerValue(sChannels, MAP_TO_CHANNELS, obj);
			GetSpinnerValue(sPocketType, MAP_TO_POCKET_TYPE, obj);

			GetEditBoxValue(R.id.edtFrequency, MAP_TO_FREQ, obj);
			GetEditBoxValue(R.id.edtPocketLength, MAP_TO_POCKET_TYPE_LEN, obj);

			// send command to....
			// new issue added by mtk54040 Shuaiqiang @2011-10-12
			Xlog.i(TAG, "PocketType().+" + obj.GetPocketType());
			Xlog.i(TAG, "edtFrequency+" + obj.GetFreq());
			if (27 == obj.GetPocketType()) {
				Xlog.i(TAG, "enter handleNonModulated(obj)");
				Xlog.i(TAG, "mbIsNonModulate--" + mbIsNonModulate
						+ "   mbIsPocketType--" + mbIsPocketType);
				if (mbIsPocketType) { // mbIsPocketType for avoid mBT is null
					runHCIResetCmd();
				}
				if (initBtTestOjbect()) {
					mbIsNonModulate = true;
					mbIsPocketType = false;
					handleNonModulated(obj);
				}

			} else {
				if (mbIsNonModulate) {
					runHCIResetCmd();
					mbIsNonModulate = false;
				}
				mbIsPocketType = true;
				if (-1 == obj.doBtTest(0)) {
					Xlog.i(TAG, "transmit data failed.");
					if ((BluetoothAdapter.STATE_TURNING_ON == stateBt)
							|| (BluetoothAdapter.STATE_ON == stateBt)) {
						EnableBluetooth(true);
					}
					Toast.makeText(getApplicationContext(),
							"transmit data failed.", Toast.LENGTH_SHORT).show();
				}
			}
		} else {
			Xlog.i(TAG, "We cannot find BtTest object.");
		}
		Xlog.i(TAG, "Leave GetValuesAndSend().");
	}

	private void handleNonModulated(BtTest obj) {
		Xlog.i(TAG, "-->handleNonModulated TX first");
		/*
		 * If pressing "Stop" button Tx: 01 0C 20 02 00 PP 0xPP = Filter
		 * Duplicate (00 = Disable Duplicate Filtering, 01 = Enable Duplicate
		 * Filtering) Rx: 04 0E 04 01 0C 20 00
		 */

		/*
		 * TX: 01 15 FC 01 00 RX: 04 0E 04 01 15 FC 00 TX: 01 D5 FC 01 XX (XX =
		 * Channel) RX: 04 0E 04 01 D5 FC 00
		 */
		int cmdLen = 5;
		char[] cmd = new char[cmdLen];
		char[] response = null;
		int i = 0;
		cmd[0] = 0x01;
		cmd[1] = 0x15;
		cmd[2] = 0xFC;
		cmd[3] = 0x01;
		cmd[4] = 0x00;
		response = obj.HCICommandRun(cmd, cmdLen);
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

		Xlog.i(TAG, "-->handleNonModulated TX second");
		cmdLen = 5;
		cmd[0] = 0x01;
		cmd[1] = 0xD5;
		cmd[2] = 0xFC;
		cmd[3] = 0x01;
		cmd[4] = (char) obj.GetFreq();
		response = obj.HCICommandRun(cmd, cmdLen);
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
		Xlog.i(TAG, "-->initBtTestOjbect");
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
		Xlog.i(TAG, "-->uninitBtTestOjbect");
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
		if (mBT == null) {
			mBT = new BtTest();
		}
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

	private boolean GetSpinnerValue(Spinner sSpinner, int flag, BtTest obj) {
		boolean bSuccess = false;
		int index = sSpinner.getSelectedItemPosition();
		if (0 > index) {
			return bSuccess;
		}

		switch (flag) {
		case MAP_TO_PATTERN: // Pattern
			obj.SetPatter(index);
			break;
		case MAP_TO_CHANNELS: // Channels
			obj.SetChannels(index);
			break;
		case MAP_TO_POCKET_TYPE: // Pocket type
			obj.SetPocketType(index);
			break;
		default:
			bSuccess = false;
			break;
		}
		bSuccess = true;
		return bSuccess;
	}

	private boolean GetEditBoxValue(int id, int flag, BtTest obj) {
		boolean bSuccess = false;

		TextView text = (TextView) findViewById(id);
		String str = null;
		if (null != text) {
			str = text.getText().toString();
		}
		if ((null == str) || str.equals("")) {
			return bSuccess;
		}
		int iLen = 0;
		try{
			iLen = Integer.parseInt(str);
		}catch (Exception e){
			Xlog.i(TAG, "parseInt failed--invalid number!");
			return bSuccess;
		}

		if (MAP_TO_FREQ == flag) // frequency
		{
			obj.SetFreq(iLen);
			bSuccess = true;
		} else if (MAP_TO_POCKET_TYPE_LEN == flag) // pocket type length
		{
			obj.SetPocketTypeLen(iLen);
			bSuccess = true;
		}
		return bSuccess;
	}
}
