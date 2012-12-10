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
import android.os.Message;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import java.io.IOException;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.app.AlertDialog;

public class TestModeActivity extends Activity implements
		DialogInterface.OnCancelListener {
	private BluetoothAdapter mAdapter;

	private static final String TAG = "TestModeActivity";
	private static final int DIALOG_BLUETOOTH_INIT = 100;
	private static final int HANDLER_TEST_FAILED = 0;
	private CheckBox mIsChecked;
	private EditText BTTestMode_edit;

	private int stateBt;
	BluetoothAdapter mBTAdapter;
	private Handler mHandler;

	private BtTest mBT;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.test_mode);

		CharSequence str1 = getString(R.string.strTestMode);
		TextView tv = (TextView) findViewById(R.id.TestModetxv);
		BTTestMode_edit = (EditText) findViewById(R.id.BTTestMode_edit);
		if (BTTestMode_edit == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}
		if (null != tv) {
			tv.setText(Html.fromHtml(str1.toString()));
		} else {
			Xlog.w(TAG, "findViewById(R.id.TestModetxv) failed");
		}

		mIsChecked = (CheckBox) findViewById(R.id.TestModeCb);
		if (null != mIsChecked) {
			mIsChecked.setOnCheckedChangeListener(mCheckedListener);
		} else {
			Xlog.w(TAG, "findViewById(R.id.TestModeCb) failed");
		}

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				mIsChecked.setEnabled(true);
				Toast.makeText(getApplicationContext(),
						"transmit data failed, please try again.",
						Toast.LENGTH_SHORT).show();
			}
		};
	}

	// implemented for DialogInterface.OnCancelListener
	public void onCancel(DialogInterface dialog) {
		// request that the service stop the query with this callback object.
		Xlog.v(TAG, "onCancel");
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

	public void onDestroy() {
		Xlog.v(TAG, "-->onDestroy");
		// super.onDestroy();
		final Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("su");
			Xlog.w(TAG, "excute su command.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (mBT != null) {
			if (-1 == mBT.doBtTest(2)) {
				Xlog.w(TAG, "transmit data failed 1.");

			}
			mBT = null;
		} else {
			Xlog.i(TAG, "BtTest does not start yet.");
		}
		// mBT.UnInit();
		super.onDestroy();
	}

	public void onStop() {
		Xlog.v(TAG, "-->onStop");
		super.onStop();
		// finish();
	}

	private CheckBox.OnCheckedChangeListener mCheckedListener = new CheckBox.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean Checked) {
			boolean ischecked = mIsChecked.isChecked();

			if (ischecked) // is checked
			{

				showDialog(DIALOG_BLUETOOTH_INIT);
				String val = BTTestMode_edit.getEditableText().toString();
				if (val == null || val.length() < 1) {
					BTTestMode_edit.setText("7");
					val = "7";
				}
				int v = Integer.valueOf(val);
				if (v > 7) {
					BTTestMode_edit.setText("7");
				}
				new Thread() {
					public void run() {

						final Runtime rt = Runtime.getRuntime();
						try {
							rt.exec("su");
							Xlog.v(TAG, "excute su command.");
						} catch (IOException e) {
							e.printStackTrace();
						}

						mBT = new BtTest();
						if (mBT != null) {
							String val = BTTestMode_edit.getEditableText()
									.toString();

							mBT.SetPower(Integer.valueOf(val));
							Xlog.i(TAG, "power set " + val);
							if (-1 == mBT.doBtTest(1)) {
								Xlog.v(TAG, "transmit data failed.");
								removeDialog(DIALOG_BLUETOOTH_INIT);
								mHandler.sendEmptyMessage(HANDLER_TEST_FAILED);
							}
						} else {
							Xlog.i(TAG, "We cannot find BtTest object.");
						}
						removeDialog(DIALOG_BLUETOOTH_INIT);
					}
				}.start();
			} else {

				final Runtime rt = Runtime.getRuntime();
				try {
					rt.exec("su");
					Xlog.i(TAG, "excute su command.");
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (mBT != null) {
					if (-1 == mBT.doBtTest(2)) {
						Xlog.i(TAG, "transmit data failed 1.");

					}
					mBT = null;
				} else {
					Xlog.i(TAG, "BtTest does not start yet.");
				}
			}
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_BLUETOOTH_INIT) {
			ProgressDialog dialog = new ProgressDialog(TestModeActivity.this);

			// It would be more efficient to reuse this dialog by moving
			// this setMessage() into onPreparedDialog() and NOT use
			// removeDialog(). However, this is not possible since the
			// message is rendered only 2 times in the ProgressDialog -
			// after show() and before onCreate.
			if (dialog != null) {
				dialog.setTitle("Progress!");
				dialog.setMessage("Please wait for device initialize");
				dialog.setCancelable(false);
				dialog.setIndeterminate(true);
				dialog.show();
				dialog.setOnCancelListener(this);
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

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// super.onCreateOptionsMenu(menu);
	// MenuInflater inflater = getMenuInflater();
	// inflater.inflate(R.menu.menu_show, menu);
	// return true;
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case R.id.menu_done: {
	// return doSendTestModeCommAction();
	// }
	//
	// case R.id.menu_discard: {
	// return doRevertAction();
	// }
	// }
	// return false;
	// }

	// /**
	// * Send command the user has made, and finish the activity.
	// */
	// private boolean doSendTestModeCommAction() {
	// //Log.e(TAG, "Send test mode Command to ...");
	// return true;
	// }
	//
	// /**
	// * Revert any changes the user has made, and finish the activity.
	// */
	// private boolean doRevertAction() {
	// finish();
	// return true;
	// }

}
