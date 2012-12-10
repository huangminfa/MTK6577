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

package com.mediatek.engineermode.wifi;

import com.mediatek.engineermode.R;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.app.Activity;
import android.os.Bundle;
import com.mediatek.engineermode.wifi.EMWifi;
import com.mediatek.xlog.Xlog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.Toast;
import android.text.TextUtils;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

public class WiFi_Rx extends Activity implements OnClickListener {

	private final String TAG = "EM/WiFi_Rx";

	private TextView mFCSText;
	private TextView mRxText;
	private TextView mPerText;
	private CheckBox mALCCheck;
	private Button mGoBtn;
	private Button mStopBtn;

	private Spinner mChannelSpinner;
	private int mChannelIndex = 0;
	private ArrayAdapter<String> mChannelAdatper;

	private Handler mHandler;
	private final int HANDLER_EVENT_RX = 2;
	private long chipID = 0x00;
	private WiFiStateManager mWiFiStateManager = null;
	private ChannelInfo mChannel;

	private long[] i4Init;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_rx);

		mFCSText = (TextView) findViewById(R.id.WiFi_FCS_Content);
		mRxText = (TextView) findViewById(R.id.WiFi_Rx_Content);
		mPerText = (TextView) findViewById(R.id.WiFi_PER_Content);

		mALCCheck = (CheckBox) findViewById(R.id.WiFi_ALC_Rx);
		mGoBtn = (Button) findViewById(R.id.WiFi_Go_Rx);
		mStopBtn = (Button) findViewById(R.id.WiFi_Stop_Rx);
		if(null != mGoBtn)
		{
			mGoBtn.setOnClickListener(this);
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_Go_Rx)");
		}
		if(null != mStopBtn)
		{
			mStopBtn.setOnClickListener(this);
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_Stop_Rx)");
		}

		i4Init = new long[2];
		mChannel = new ChannelInfo();

		mChannelSpinner = (Spinner) findViewById(R.id.WiFi_RX_Channel_Spinner);
		mChannelAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mChannelAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < mChannel.mChannelName.length; i++) {
			mChannelAdatper.add(mChannel.mFullChannelName[i]);
		}
		if(null != mChannelSpinner)
		{
			mChannelSpinner.setAdapter(mChannelAdatper);
			mChannelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					if (!EMWifi.isIntialed) {
						Xlog.w(TAG, "Wifi is not initialized");
						new AlertDialog.Builder(WiFi_Rx.this).setTitle("Error").setCancelable(false).setMessage(
			            		"Wifi is not initialized").setPositiveButton("OK",
			            		new DialogInterface.OnClickListener() {
			                    		public void onClick(DialogInterface dialog,int which) {
			                           	 // TODO Auto-generated method stub  
			                    		 dialog.dismiss();
			                          	 finish();
		    		                        }
					        }).show();
						return;
					}
					mChannel.mChannelSelect = mChannelAdatper.getItem(arg2);
					Xlog.i(TAG, "The mChannelSelect is : " + mChannel.mChannelSelect);
					int number = mChannel.getChannelFreq();
					EMWifi.setChannel(number);
					// uiUpdateTxPower();
				}
	
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
				}
			});
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_RX_Channel_Spinner)");
		}

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {

				case HANDLER_EVENT_RX:
					long[] i4Rx = new long[2];
					long i4RxCntOk = -1;
					long i4RxCntFcsErr = -1;
					long i4RxPer = -1;
					
					Xlog.i(TAG, "The Handle event is : HANDLER_EVENT_RX");

					try {
						i4RxPer = Long.parseLong(mPerText.getText().toString());
					} catch (NumberFormatException e) {
//						Toast.makeText(WiFi_Rx.this, "invalid input value",
//								Toast.LENGTH_SHORT).show();
					}

					EMWifi.getPacketRxStatus(i4Rx, 2);

					i4RxCntOk = i4Rx[0] - i4Init[0];
					i4RxCntFcsErr = i4Rx[1] - i4Init[1];

					if (i4RxCntFcsErr + i4RxCntOk != 0) {
						i4RxPer = i4RxCntFcsErr * 100
								/ (i4RxCntFcsErr + i4RxCntOk);
					}

					mFCSText.setText(String.valueOf(i4RxCntFcsErr));
					mRxText.setText(String.valueOf(i4RxCntOk));
					mPerText.setText(String.valueOf(i4RxPer));

					break;
					
				default:
					break;
				}

				mHandler.sendEmptyMessageDelayed(HANDLER_EVENT_RX, 1000);
			}
		};
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (!EMWifi.isIntialed) {
			Xlog.w(TAG, "Wifi is not initialized");
			new AlertDialog.Builder(this).setTitle("Error").setCancelable(false).setMessage(
            		"Wifi is not initialized").setPositiveButton("OK",
           		new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog,int which) {
        	                    // TODO Auto-generated method stub  
                	    	    dialog.dismiss();
                        	    finish();
                    	    }
	            	}).show();
			return;
		}
		if (arg0.getId() == mGoBtn.getId()) {
			// mFCSText.setText("FCSText");
			// mRxText.setText("RxText");
			// mPerText.setText("PERText");

			// Boolean ALC = mALCCheck.isChecked();
			// Log.i(TAG, "The ALC string is : " + ALC);

			onClickBtnRxGo();
		}

		if (arg0.getId() == mStopBtn.getId()) {
			onClickBtnRxStop();
		}
	}

	protected void onStop() {
		super.onStop();
		mHandler.removeMessages(HANDLER_EVENT_RX);
		mGoBtn.setEnabled(true);
        	finish();
	}

	private void onClickBtnRxGo() {
		int i = -1;
		int len = 2;

		EMWifi.getPacketRxStatus(i4Init, 2);

		Xlog.d("itiNIT[0]", String.valueOf(i4Init[0]));
		Xlog.d("itiNIT[1]", String.valueOf(i4Init[1]));

		if (mALCCheck.isChecked() == false) {
			i = 0;
		} else {
			i = 1;
		}
		EMWifi.setATParam(9, i);
		EMWifi.setATParam(1, 2);

		mHandler.sendEmptyMessage(HANDLER_EVENT_RX);

		mFCSText.setText("0");
		mRxText.setText("0");
		mPerText.setText("0");

		mGoBtn.setEnabled(false);
	}

	protected void onStart() {
		super.onStart();
		checkWiFiChipState();
/*
                if (!EMWifi.isIntialed) {
                	//WiFi_Rx is for MT5921 only
					chipID = EMWifi.initial();
		           	if (chipID != 0x5921) {
                                // Toast.makeText(this, "Wrong to configuration",
                                // Toast.LENGTH_LONG);
                                new AlertDialog.Builder(this).setTitle("Error").setCancelable(false).setMessage(
                                                "Please check your wifi state").setPositiveButton("OK",
                                                new DialogInterface.OnClickListener() {
        
                                                        public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                                // TODO Auto-generated method stub
                                                                finish();
                                                        }
                                                }).show();
                        } else {
                                Log.d(TAG, "Initialize succeed");
										int result = -1;
		                                result = EMWifi.setTestMode();
		                                if(result == 0)
		                                {
		                                	EMWifi.isIntialed = true;
		                                	Log.e(TAG, "setTestMode succeed");
		                                }
		                                else
		                                {
		                                	Log.e(TAG, "setTestMode failed, ERROR_CODE = " + result);
		                                }
                        }
                }
                */
        }


	private void onClickBtnRxStop() {
		long i4RxCntOk = -1;
		long i4RxCntFcsErr = -1;
		long i4RxPer = -1;

		long[] i4Rx = new long[2];
		long[] u4Value = new long[1];

		mHandler.removeMessages(HANDLER_EVENT_RX);

		for (int i = 0; i < 100; i++) {
			EMWifi.setATParam(1, 0);
			EMWifi.getATParam(1, u4Value);

			if (u4Value[0] == 0) {
				break;
			} else {
				SystemClock.sleep(10);
			}
		}

		EMWifi.getPacketRxStatus(i4Rx, 2);

		try {
			i4RxPer = Long.parseLong(mPerText.getText().toString());
		} catch (NumberFormatException e) {
			// Toast.makeText(WiFi_Rx.this, "invalid input value",
			// Toast.LENGTH_SHORT).show();
			// return;
		}

		i4RxCntOk = i4Rx[0] - i4Init[0];
		i4RxCntFcsErr = i4Rx[1] - i4Init[1];

		if (i4RxCntFcsErr + i4RxCntOk != 0) {
			i4RxPer = i4RxCntFcsErr * 100 / (i4RxCntFcsErr + i4RxCntOk);
		}

		mFCSText.setText(String.valueOf(i4RxCntFcsErr));
		mRxText.setText(String.valueOf(i4RxCntOk));
		mPerText.setText(String.valueOf(i4RxPer));

		mGoBtn.setEnabled(true);
	}
	private void checkWiFiChipState()
	{
		int result = 0x0;
				if(mWiFiStateManager == null)
				{
					mWiFiStateManager = new WiFiStateManager(this);
				}
				result = mWiFiStateManager.checkState(this);
				switch(result)
				{
					case WiFiStateManager.ENABLEWIFIFAIL:
						new AlertDialog.Builder(this).setTitle("Error").setCancelable(false).setMessage(
		                                                "enable Wi-Fi failed").setPositiveButton("OK",
		                                                new DialogInterface.OnClickListener() {
		                                                        public void onClick(DialogInterface dialog,int which) {
		                                                                // TODO Auto-generated method stub  
										dialog.dismiss();
		                                                                finish();
		                                                        }
		                                                }).show();
					break;
					
					case WiFiStateManager.INVALIDCHIPID:
					case WiFiStateManager.SETTESTMODEFAIL:
						new AlertDialog.Builder(this).setTitle("Error").setCancelable(false).setMessage(
		                                                "Please check your Wi-Fi state").setPositiveButton("OK",
		                                                new DialogInterface.OnClickListener() {
		                                                        public void onClick(DialogInterface dialog,int which) {
		                                                                // TODO Auto-generated method stub 
		                                                               	mWiFiStateManager.disableWiFi();	
										dialog.dismiss(); 
		                                                                finish();
		                                                        }
		                                                }).show();
					break;
					case WiFiStateManager.CHIPREADY:	
					case 0x6620:
					case 0x5921:
					break;
					default:
					
					break;
				}
	}
}
