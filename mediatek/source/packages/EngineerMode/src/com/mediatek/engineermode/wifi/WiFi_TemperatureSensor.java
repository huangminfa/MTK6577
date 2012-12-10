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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.mediatek.engineermode.wifi.EMWifi;
import com.mediatek.xlog.Xlog;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;
import android.content.Intent;

public class WiFi_TemperatureSensor extends Activity implements OnClickListener {

	private final String TAG = "EM/WiFi_TemperatureSensor";

	private Button mGoBtn;
	private Button mStopBtn;
	private Button mClearBtn;
	private EditText mLogShow;

	private Handler mHandler;
	private boolean fgOriThermoEn = false;
	private int u4RunNum = 0;
	private final String chipIDName = "WiFiChipID";
	private int chipID = 0x00;
	private WiFiStateManager mWiFiStateManager = null;
	private final int HANDLER_EVENT_TEMPERATURE_SENSOR = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_temperature_sensor);
		Intent intent = WiFi_TemperatureSensor.this.getIntent();
		chipID = intent.getIntExtra(chipIDName, 0x0);
		mGoBtn = (Button) findViewById(R.id.WiFi_Misc_GoBtn);
		mStopBtn = (Button) findViewById(R.id.WiFi_Misc_StopBtn);
		mClearBtn = (Button) findViewById(R.id.WiFi_Misc_ClearBtn);
		mLogShow = (EditText) findViewById(R.id.WiFi_Misc_TempShow);
		if(null != mGoBtn)
		{
			mGoBtn.setOnClickListener(this);
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_Misc_GoBtn) failed");
		}
		if(null != mStopBtn)
		{
			mStopBtn.setOnClickListener(this);
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_Misc_StopBtn) failed");
		}
		if(null != mClearBtn)
		{
			mClearBtn.setOnClickListener(this);
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_Misc_ClearBtn) failed");
		}

		mHandler = new Handler() {

			public void handleMessage(Message msg) {
				long[] u4ThermoRaw = new long[2];

				switch (msg.what) {
				case HANDLER_EVENT_TEMPERATURE_SENSOR:
					
					Xlog.d(TAG, "The Handle event is : HANDLER_EVENT_TEMPERATURE_SENSOR, chipID = " + chipID);
					if(chipID == 0x6620)
					{
						long[] temperature = new long[1];
						if(0 == EMWifi.getATParam(43, temperature))//get temperature sensor result
						{
						//temperature format:
						//Bits 15:0 Thermo-sensor ADC value
						//Bits 31:16 Estimated temperature (signed value)
							mLogShow.append("Run " + (++u4RunNum) + ", Thermo value 0x" + Long.toHexString(temperature[0] >> 16) + "\n");
						}
						else
						{
						mLogShow.append("Run " + (++u4RunNum) + ", Get thermo value "+ "failed\n");
						}
					}
					else if(chipID == 0x5921)
					{
						EMWifi.queryThermoInfo(u4ThermoRaw, 2);
						if (u4ThermoRaw[1] == 0xFFFFFFFF) {
							mLogShow
									.append("Hardware report busy after 99 times retry\n");
						} else {
							mLogShow.append("Run " + (++u4RunNum)
									+ ", Thermo value "
									+ Long.toHexString(u4ThermoRaw[1]) + "\n");
						}
					}
					
					mHandler.sendEmptyMessageDelayed(
						HANDLER_EVENT_TEMPERATURE_SENSOR, 1000);

					break;

				default:
					break;
				}
			}
		};
	}

	protected void onStart() {
		super.onStart();
		checkWiFiChipState();
				/*
                if (!EMWifi.isIntialed) {
                	//WiFi_Temperature is for both MT5921 and MT6620
					chipID = EMWifi.initial();
		                if (chipID != 0x6620 && chipID != 0x5921) {
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



	public void onClick(View view) {
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
		switch (view.getId()) {

		case R.id.WiFi_Misc_GoBtn:
			onClickThermoGoBtn();

			// mGoBtn.setEnabled(false);
			// EMWifi.setNormalMode();
			// EMWifi.setAnritsu8860bTestSupportEn(1);
			break;

		case R.id.WiFi_Misc_StopBtn:

			onClickThermoStopBtn();

			// EMWifi.setAnritsu8860bTestSupportEn(0);
			// EMWifi.setTestMode();
			// mGoBtn.setEnabled(true);
			break;

		case R.id.WiFi_Misc_ClearBtn:
			mLogShow.setText("");

			break;

		default:
			break;
		}
	}

	void onClickThermoStopBtn() {
		mHandler.removeMessages(HANDLER_EVENT_TEMPERATURE_SENSOR);
		if (fgOriThermoEn == false) {
			EMWifi.setThermoEn(0);
		}
		mGoBtn.setEnabled(true);
	}

	void onClickThermoGoBtn() {
		long[] i4AlcEn = new long[2];

		EMWifi.queryThermoInfo(i4AlcEn, 2);	
		if (i4AlcEn[0] == 1) {
			fgOriThermoEn = true;
		} else {
			fgOriThermoEn = false;
			EMWifi.setThermoEn(1);
		}

		u4RunNum = 0;
		mHandler.sendEmptyMessage(HANDLER_EVENT_TEMPERATURE_SENSOR);
		mGoBtn.setEnabled(false);
	}

	protected void onStop() {
		super.onStop();
		mHandler.removeMessages(HANDLER_EVENT_TEMPERATURE_SENSOR);
		mGoBtn.setEnabled(true);
		finish();
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
