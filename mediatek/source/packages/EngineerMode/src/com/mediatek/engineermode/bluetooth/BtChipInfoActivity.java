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

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.view.View;
/*
typedef enum {
	  BT_CHIP_ID_MT6611 = 0,
	  BT_CHIP_ID_MT6612,
	  BT_CHIP_ID_MT6616,
	  BT_CHIP_ID_MT6620,
	  BT_CHIP_ID_MT6622,
	  BT_CHIP_ID_MT6626,
	  BT_CHIP_ID_MT6628
	} BT_CHIP_ID;

	typedef enum {
	  BT_HW_ECO_UNKNOWN = 0,
	  BT_HW_ECO_E1,
	  BT_HW_ECO_E2,
	  BT_HW_ECO_E3,
	  BT_HW_ECO_E4,
	  BT_HW_ECO_E5,
	  BT_HW_ECO_E6,
	  BT_HW_ECO_E7
	} BT_HW_ECO;

*/


public class BtChipInfoActivity extends Activity {
	private final String TAG = "EM/BT/ChipInfo";
	
	// flags
	private boolean mbIsDialogShowed = false;
	private boolean mbIsBTPoweredOff = true;
	private boolean mbIsBLEFeatureDetected = false;
	// dialog ID and MSG ID
	private static final int DIALOG_CHECK_BLEFEATURE = 100;
	private static final int CHECK_BLEFEATURE_FINISHED = 0x10;
	
	private BluetoothAdapter mAdapter;
	private Handler mHandler = null;

	private BtTest mBT;
	
	// string
	private String chipId = "UNKNOWN";
	private String chipEco ="UNKNOWN";
	private String chipPatchId ="UNKNOWN";
	private String chipPatchLen ="UNKNOWN";
	// TextView
	private TextView chipIdTextView = null;
	private TextView ecoVerTextView = null;
	private TextView patchSizeTextView = null;
	private TextView patchDateTextView = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle onSavedInstanceState) {
		
	
		Xlog.v(TAG, "onCreate");
		super.onCreate(onSavedInstanceState);
		setContentView(R.layout.bt_chip_info);
		
		chipIdTextView = (TextView) findViewById(R.id.chipId);
		ecoVerTextView = (TextView) findViewById(R.id.ecoVersion);
		patchSizeTextView = (TextView) findViewById(R.id.patchSize);
		patchDateTextView = (TextView) findViewById(R.id.patchDate);
		
		if (chipIdTextView == null || ecoVerTextView == null
				|| patchSizeTextView == null || patchDateTextView == null) {
			Xlog.w(TAG, "clocwork worked...");
		}	
		
		chipIdTextView.setText("x");
		ecoVerTextView.setText("xx");
		patchSizeTextView.setText("xxx");
		patchDateTextView.setText("xxxx");
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
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == CHECK_BLEFEATURE_FINISHED) {
					chipIdTextView.setText(chipId);
					ecoVerTextView.setText(chipEco);
					patchSizeTextView.setText(""+chipPatchLen);
					patchDateTextView.setText(chipPatchId);	
					
					mbIsBLEFeatureDetected = true;
					if (mbIsDialogShowed) {
						removeDialog(DIALOG_CHECK_BLEFEATURE);
					}
				}
			}
		};
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		Xlog.v(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		Xlog.v(TAG, "onResume");
		super.onResume();
		if (!mbIsBLEFeatureDetected) {
			showDialog(DIALOG_CHECK_BLEFEATURE);
			new Thread() {
				public void run() {
					mBT = new BtTest();
					Message msg = new Message();

					// check if BLE is supported or not
					if (mBT != null) {
						if (mBT.Init() == 0) {
							try {
								chipId = "UNKNOWN";
								switch (mBT.GetChipIdInt()) {
								case 0:
									chipId = "MT6611";
									break;
								case 1:
									chipId = "MT6612";
									break;
								case 2:
									chipId = "MT6616";
									break;
								case 3:
									chipId = "MT6620";
									break;
								case 4:
									chipId = "MT6622";
									break;
								case 5:
									chipId = "MT6626";
									break;
								case 6:
									chipId = "MT6628";
									break;
								default:
									chipId = "UNKNOWN";
									break;
								}
								Xlog.v(TAG, "chipId@d" + "___" + chipId);

								chipEco = "UNKNOWN";
								switch (mBT.GetChipEcoNum()) {
								case 0:
									chipEco = "UNKNOWN";
									break;
								case 1:
									chipEco = "E1";
									break;
								case 2:
									chipEco = "E2";
									break;
								case 3:
									chipEco = "E3";
									break;
								case 4:
									chipEco = "E4";
									break;
								case 5:
									chipEco = "E5";
									break;
								case 6:
									chipEco = "E6";
									break;
								case 7:
									chipEco = "E7";
									break;
								default:
									chipEco = "UNKNOWN";
									break;

								}
								Xlog.v(TAG, "chipEco@" + "___" + chipEco);

//								Xlog.v(TAG, "GetPatchId!");
//								if (null == mBT.GetPatchId()) {
//									Xlog.v(TAG, "GetPatchId-is null!!!");
//								}
								char[] patchIdArray = mBT.GetPatchId();
//								Xlog.v(TAG, "GetPatchId--"
//										+ patchIdArray.toString());
								chipPatchId = "" + new String(patchIdArray);
								Xlog.v(TAG, "chipPatchId@@"
										+ chipPatchId.length() + "___"
										+ chipPatchId);
								chipPatchLen = "" + mBT.GetPatchLen();
								Xlog.v(TAG, "GetPatchLen@@" + chipPatchLen);

							} catch (Exception e) {
								Xlog.v(TAG, "Exception--" + e.getMessage());
							}
							mBT.UnInit();
						} else {
							Xlog.i(TAG, "new BtTest failed");
						}
					}
					msg.what = CHECK_BLEFEATURE_FINISHED;
					mHandler.sendMessage(msg);
				}
			}.start();
			mbIsDialogShowed = true;
		}
	}

	@Override
	protected void onStart() {
		Xlog.v(TAG, "onStart");
		super.onStart();

	}

	@Override
	protected void onStop() {
		Xlog.v(TAG, "onStop");
		super.onStop();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Log.d(TAG, "-->onCreateDialog");
		if (id == DIALOG_CHECK_BLEFEATURE) {
			ProgressDialog dialog = new ProgressDialog(BtChipInfoActivity.this);

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
}