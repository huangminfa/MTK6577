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

public class WiFi_Rx_6620 extends Activity implements OnClickListener {

	private final String TAG = "EM/WiFi_Rx";

	private TextView mFCSText;
	private TextView mRxText;
	private TextView mPerText;
	//private CheckBox mALCCheck;
	private Button mGoBtn;
	private Button mStopBtn;

	private Spinner mChannelSpinner;
	private Spinner mBandwidthSpinner;
	private int mChannelIndex = 0;
	private int mBandwidthIndex = 0;
	private ArrayAdapter<String> mBandwidthAdapter;
	private ArrayAdapter<String> mChannelAdapter;
	private long chipID = 0x00;
	
	String[] mBandwidth = { 
			"20MHz", 
			"40MHz",
			"U20MHz",
			"L20MHz",
			};
	
	private Handler mHandler;
	private final int HANDLER_EVENT_RX = 2;
	private WiFiStateManager mWiFiStateManager = null;
	private ChannelInfo mChannel;

	private long[] i4Init;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_rx_6620);

		mFCSText = (TextView) findViewById(R.id.WiFi_FCS_Content);
		mRxText = (TextView) findViewById(R.id.WiFi_Rx_Content);
		mPerText = (TextView) findViewById(R.id.WiFi_PER_Content);
		if(mFCSText == null
				||mRxText == null
				||mPerText == null)
        {
        	Xlog.w("WiFi_Rx_6620", "clocwork worked...");	
    		//not return and let exception happened.
        }
		
		mFCSText.setText("");
		mRxText.setText("");
		mPerText.setText("");
		
		//mALCCheck = (CheckBox) findViewById(R.id.WiFi_ALC_Rx);
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
		mChannelAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mChannelAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mChannelAdapter.add(mChannel.mFullChannelName[0]);
		if(null != mChannelSpinner)
		{
			mChannelSpinner.setAdapter(mChannelAdapter);
			mChannelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					if (!EMWifi.isIntialed) {
						Xlog.w(TAG, "Wifi is not initialized");
						new AlertDialog.Builder(WiFi_Rx_6620.this).setTitle("Error").setCancelable(false).setMessage(
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
					mChannel.mChannelSelect = mChannelAdapter.getItem(arg2);
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
		
		mBandwidthSpinner = (Spinner) findViewById(R.id.WiFi_Bandwidth_Spinner);
		//Bandwidth seetings
		mBandwidthAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
		mBandwidthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < mBandwidth.length; i++) {
			mBandwidthAdapter.add(mBandwidth[i]);
		}
		if(null != mBandwidthAdapter)
		{
			mBandwidthSpinner.setAdapter(mBandwidthAdapter);
			mBandwidthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					//max  Bandwidth setting value is 4
					mBandwidthIndex = arg2 < 4 ? arg2 : mBandwidthIndex;
					if (1 == mBandwidthIndex) {
					    mChannelAdapter.remove(mChannel.mFullChannelName[0]);
		                mChannelAdapter.remove(mChannel.mFullChannelName[1]);
		                mChannelAdapter.remove(mChannel.mFullChannelName[11]);
		                mChannelAdapter.remove(mChannel.mFullChannelName[12]);
		                updateWifiChannel();
					} else {
					    boolean bUpdate = false;
					    if (-1 == mChannelAdapter.getPosition(mChannel.mFullChannelName[0])) {
					        mChannelAdapter.insert(mChannel.mFullChannelName[0], 0);
					        bUpdate = true;
					    }
					    if (-1 == mChannelAdapter.getPosition(mChannel.mFullChannelName[1])) {
					        mChannelAdapter.insert(mChannel.mFullChannelName[1], 1);
					        bUpdate = true;
					    }
		                if (mChannel.isContains(12) && -1 == mChannelAdapter.getPosition(mChannel.mFullChannelName[11])) {
		                    mChannelAdapter.insert(mChannel.mFullChannelName[11], 11);
		                    bUpdate = true;
		                }
		                if (mChannel.isContains(13) && -1 == mChannelAdapter.getPosition(mChannel.mFullChannelName[12])) {
		                    mChannelAdapter.insert(mChannel.mFullChannelName[12], 12);
		                    bUpdate = true;
		                }
		                if (bUpdate) {
		                    updateWifiChannel();
		                }
				    }
				}
	
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
				}
			});
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_Bandwidth_Spinner) failed");
		}
		

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (!EMWifi.isIntialed) {
						Xlog.w(TAG, "Wifi is not initialized");
						new AlertDialog.Builder(WiFi_Rx_6620.this).setTitle("Error").setCancelable(false).setMessage(
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
					Xlog.d(TAG, "after rx test: rx ok = " + String.valueOf(i4Rx[0]));
					Xlog.d(TAG, "after rx test: fcs error = " + String.valueOf(i4Rx[1]));
					
					i4RxCntOk = i4Rx[0]/* - i4Init[0]*/;
					i4RxCntFcsErr = i4Rx[1]/* - i4Init[1]*/;

					if (i4RxCntFcsErr + i4RxCntOk != 0) {
						i4RxPer = i4RxCntFcsErr * 100 / (i4RxCntFcsErr + i4RxCntOk);
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
		setViewEnabled(true);
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

		Xlog.d(TAG, "before rx test: rx ok = " + String.valueOf(i4Init[0]));
		Xlog.d(TAG, "before rx test: fcs error = " +  String.valueOf(i4Init[1]));

		//if (mALCCheck.isChecked() == false) {
			i = 0;
		//} else {
		//	i = 1;
		//}
		//temperature conpensation
		EMWifi.setATParam(9, i);
		
		//Bandwidth setting
		EMWifi.setATParam(15, mBandwidthIndex);
		//start Rx 
		EMWifi.setATParam(1, 2);	

		mHandler.sendEmptyMessage(HANDLER_EVENT_RX);

		mFCSText.setText("0");
		mRxText.setText("0");
		mPerText.setText("0");

		//mGoBtn.setEnabled(false);
		setViewEnabled(false);
	}

	protected void onStart() {
		super.onStart();
		checkWiFiChipState();
		mChannelAdapter.clear();
        for(int i=1; i<=mChannel.mChannels[0]; i++) {
            for (String s : mChannel.mFullChannelName) {
                if (s.startsWith("Channel " + mChannel.mChannels[i] + " ")) {
                    mChannelAdapter.add(s);
                    break;
                }
            }
        }
				/*
                if (!EMWifi.isIntialed) {
                	//WiFi_Rx_6620 is for MT6620 only
						chipID = EMWifi.initial();
		                if (chipID != 0x6620 ) {
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

		for (int i = 0; i < 10; i++) {
			u4Value[0] = EMWifi.setATParam(1, 0);
			//driver does not support query operation on functionIndex = 1 , we can only judge whether this operation is processed successfully through the return value
			//EMWifi.getATParam(1, u4Value);

			if (u4Value[0] == 0) {
				break;
			} else {
				SystemClock.sleep(10);
				Xlog.w(TAG, "stop Rx test failed at the " + i  + "times try");
			}
		}
		/*
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
		*/
		//mGoBtn.setEnabled(true);
		setViewEnabled(true);
	}
	private void setViewEnabled(boolean state)
	{
		//mALCCheck.setEnabled(state);
		mGoBtn.setEnabled(state);
		mStopBtn.setEnabled(!state);
		mChannelSpinner.setEnabled(state);
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
	
	private void updateWifiChannel() {
        if (!EMWifi.isIntialed) {
            Xlog.w(TAG, "Wifi is not initialized");
            new AlertDialog.Builder(WiFi_Rx_6620.this).setTitle("Error").setCancelable(false).setMessage(
                    "Wifi is not initialized").setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog,int which) {
                                dialog.dismiss();
                                finish();
                             }
                    }).show();
            return;
        }
        if (0 != mChannel.getChannelIndex()) {
            mChannelSpinner.setSelection(0);
        } else {
            mChannel.mChannelSelect = mChannelAdapter.getItem(0);
            int number = mChannel.getChannelFreq();
            EMWifi.setChannel(number);
            Xlog.i(TAG, "The channel freq =" + number);
        }
    }
}
