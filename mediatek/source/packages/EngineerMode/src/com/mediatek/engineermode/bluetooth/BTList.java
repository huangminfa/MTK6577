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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.os.SystemClock;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.util.Log;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;

public class BTList extends ListActivity {
	private BluetoothAdapter mAdapter;

	private static final String TAG = "BTListActivity";

	// dialog ID and MSG ID
	private static final int DIALOG_CHECK_BLEFEATURE = 100;
	private static final int CHECK_BLEFEATURE_FINISHED = 0x10;

	ListActivity thisActivity = null;
	// flags
	private boolean mbIsBLESupport = false;
	private boolean mbIsDialogShowed = false;
	private boolean mbIsBLEFeatureDetected = false;
	private boolean mbIsBTPoweredOff = true;
	private boolean mDoubleFlag = false;

	private Handler mHandler = null;

	private BtTest mBT = null;
	List<String> list = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Xlog.v(TAG, "-->onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.btlist);
		thisActivity = this;
		if (mAdapter == null) {
			mAdapter = BluetoothAdapter.getDefaultAdapter();
		}
		if (mAdapter != null) {
			if (mAdapter.getState() != mAdapter.STATE_OFF) {
				mbIsBTPoweredOff = false;
				new AlertDialog.Builder(this).setCancelable(false).setTitle(
						"Error").setMessage("Please turn off Bluetooth first")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										finish();
									}
								}).show();

			}
		}
		list = new ArrayList<String>();
		list.add("TX Only test");
		list.add("Test Mode");
		list.add("SSP Debug Mode");
		
		// add for ble test 2010-11.30
		// before add the following 5 feature into the listview, we need to read
		// the "BLE feature bit" to confirm chip support BLE feature
		//
		ArrayAdapter<String> la = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == CHECK_BLEFEATURE_FINISHED) {
					list = new ArrayList<String>();
					list.add("Chip Information");
					list.add("TX Only test");

					if (mBT != null) {
						int chipId = mBT.getChipId();
						Xlog.v(TAG, "chipId@d" + "___" + chipId);
						
						if (chipId != 0x6622) {
							list.add("Non-signaling RX Test");
						}
						
						list.add("Test Mode");
						list.add("SSP Debug Mode");
						
						if (mbIsBLESupport) {
//							list.add("BLE Scan");
//							list.add("BLE Initiate");
//							list.add("BLE WhiteList");
//							list.add("BLE Advertise");
							list.add("BLE Test Mode");
						}
						
						if (0x6620 == chipId||0x6628 == chipId) {
							list.add("BT Relayer Mode");
						}
						
						ArrayAdapter<String> la = new ArrayAdapter<String>(
								thisActivity,
								android.R.layout.simple_list_item_1, list);
						thisActivity.setListAdapter(la);
					} else {
						Xlog.v(TAG, "BtTest object is not created yet.");
					}
					mbIsBLEFeatureDetected = true;
					if (mbIsDialogShowed) {
						removeDialog(DIALOG_CHECK_BLEFEATURE);
					}
				}
			}
		};
		thisActivity.setListAdapter(la);
		/*
		 * move this part to onResume function new Thread() { public void run()
		 * { mBT = new BtTest(); Message msg = new Message(); if(mAdapter ==
		 * null) { mAdapter = BluetoothAdapter.getDefaultAdapter(); }
		 * 
		 * if (mAdapter != null) { if (mAdapter.getState() !=
		 * mAdapter.STATE_OFF) { //if Bluetooth is in ON state, just sleep for
		 * 500 ms and send the message to main thread try{ sleep(500); }
		 * catch(InterruptedException e) { e.printStackTrace(); } } else {
		 * //check if BLE is supported or not if(mBT != null) { if
		 * (mBT.isBLESupport() == 1) { Log.d(TAG,
		 * "BLE is supported by this chip"); mbIsBLESupport = true; } else {
		 * mbIsBLESupport = false; Log.e(TAG,
		 * "BLE is not supported by this chip"); }
		 * 
		 * } else { Log.e(TAG, "new BtTest failed"); } } msg.what =
		 * CHECK_BLEFEATURE_FINISHED; mHandler.sendMessage(msg); } } }.start();
		 */

	}

	protected void onStart() {
		Xlog.v(TAG, "-->onStart");
		super.onStart();
		mDoubleFlag = false;
		if (mAdapter == null) {
			mAdapter = BluetoothAdapter.getDefaultAdapter();
		}
		if (mAdapter != null) {
			/*
			 * if (mAdapter.getState() != mAdapter.STATE_OFF){ new
			 * AlertDialog.Builder
			 * (this).setCancelable(false).setTitle("Error").setMessage(
			 * "Please turn off Bluetooth first").setPositiveButton("OK", new
			 * DialogInterface.OnClickListener() {
			 * 
			 * public void onClick(DialogInterface dialog, int which) { // TODO
			 * Auto-generated method stub finish(); } }).show();
			 * 
			 * }
			 */
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

	protected void onResume() {
		Xlog.v(TAG, "-->onResume");
		super.onResume();
		if (!mbIsBLEFeatureDetected && mbIsBTPoweredOff) {
			showDialog(DIALOG_CHECK_BLEFEATURE);
			new Thread() {
				public void run() {
					mBT = new BtTest();
					Message msg = new Message();
					if (mAdapter == null) {
						mAdapter = BluetoothAdapter.getDefaultAdapter();
					}

					if (mAdapter != null) {
						if (mAdapter.getState() != mAdapter.STATE_OFF) {
							// if Bluetooth is in ON state, just sleep for 500
							// ms and send the message to main thread
							try {
								sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else {
							// check if BLE is supported or not
							if (mBT != null) {
								if (mBT.isBLESupport() == 1) {
									Xlog
											.i(TAG,
													"BLE is supported by this chip");
									mbIsBLESupport = true;
								} else {
									mbIsBLESupport = false;
									Xlog
											.i(TAG,
													"BLE is not supported by this chip");
								}

							} else {
								Xlog.i(TAG, "new BtTest failed");
							}
						}
						msg.what = CHECK_BLEFEATURE_FINISHED;
						mHandler.sendMessage(msg);
					}
				}
			}.start();
			mbIsDialogShowed = true;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Log.d(TAG, "-->onCreateDialog");
		if (id == DIALOG_CHECK_BLEFEATURE) {
			ProgressDialog dialog = new ProgressDialog(BTList.this);

			// It would be more efficient to reuse this dialog by moving
			// this setMessage() into onPreparedDialog() and NOT use
			// removeDialog(). However, this is not possible since the
			// message is rendered only 2 times in the ProgressDialog -
			// after show() and before onCreate.
			if (dialog != null) {
				dialog.setTitle("Progress");
				dialog.setMessage("Please wait for device to initialize ...");
				dialog.setCancelable(false);
				dialog.setIndeterminate(true);
				dialog.show();
				Xlog.i(TAG, "new ProgressDialog succeed");
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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (!mDoubleFlag) {

			if ("TX Only test" == list.get(position)) {
				startActivity(new Intent(BTList.this, TXOnlyTestActivity.class));
			} else if ("Non-signaling RX Test" == list.get(position)) {
				startActivity(new Intent(BTList.this, NoSigRxTestActivity.class));
			} else if ("Test Mode" == list.get(position)) {
				startActivity(new Intent(BTList.this, TestModeActivity.class));
			} else if ("SSP Debug Mode" == list.get(position)) {
				startActivity(new Intent(BTList.this,
						SSPDebugModeActivity.class));

			} else if ("Rx only test" == list.get(position)) {
				startActivity(new Intent(BTList.this, RxOnlyTestActivity.class));
			} else if ("Chip Information" == list.get(position)) {
					startActivity(new Intent(BTList.this, BtChipInfoActivity.class));
			} else if ("BT Relayer Mode" == list.get(position)) {
				startActivity(new Intent(BTList.this, BtRelayerModeActivity.class));
			}
			
			if (mbIsBLESupport) {
				if ("BLE Scan" == list.get(position)) {
					Log.d(TAG, "BLE_Scan item is selected");
					startActivity(new Intent(BTList.this, BLE_Scan.class));
				} else if ("BLE Initiate" == list.get(position)) {
					Log.d(TAG, "BLE_Initiate item is selected");
					startActivity(new Intent(BTList.this, BLE_Initiate.class));
				} else if ("BLE WhiteList" == list.get(position)) {
					Log.d(TAG, "BLE_WhiteList item is selected");
					startActivity(new Intent(BTList.this, BLE_WhiteList.class));
				} else if ("BLE Advertise" == list.get(position)) {
					Log.d(TAG, "BLE_Advertise item is selected");
					startActivity(new Intent(BTList.this, BLE_Advertise.class));
				} else if ("BLE Test Mode" == list.get(position)) {
					Log.d(TAG, "BLE_Test_Mode item is selected");
					startActivity(new Intent(BTList.this, BLE_Test_Mode.class));
				}

			}
			mDoubleFlag = true;
		}
	}

	protected void onDestroy() {
		super.onDestroy();
	}
}
