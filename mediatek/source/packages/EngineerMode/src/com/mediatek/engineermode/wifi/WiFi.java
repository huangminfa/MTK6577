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

import java.util.ArrayList;
import java.util.List;

import com.mediatek.engineermode.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Message;
import android.os.SystemProperties;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import com.mediatek.engineermode.wifi.EMWifi;
import com.mediatek.xlog.Xlog;
import android.widget.Toast;
import com.mediatek.engineermode.wifi.WiFiStateManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

public class WiFi extends Activity implements OnItemClickListener {

	private final String TAG = "EM/WiFi";
	private static final String KEY_PROP_WPAWPA2 = "persist.radio.wifi.wpa2wpaalone";
	private static final String KEY_PROP_CTIA = "mediatek.wlan.ctia";
	private static final String KEY_PROP_OPEN_AP_WPS = "mediatek.wlan.openap.wps";
	
	private boolean isInitialized = false;
	private boolean isWifiOpened = false;
	private WifiManager mWifiManager;
	private Intent intent;
	private int miChipID = 0x00;
	private Handler mHandler;
	private final int HANDLER_EVENT_INIT = 0x011;
	private final int DIALOG_WIFI_INIT = 0x100;
	private final String chipIDName = "WiFiChipID";
	private WiFiStateManager mWiFiStateManager = null;
	private ListView WiFi_listView = null;
	private Context thisContext = null;
	private ProgressDialog mInitDialog = null;
	private boolean wifiStateFlag = false;
	private boolean mbBackgroundFlag = true;
	private CheckBox mWFACheckBox = null;
	private CheckBox mCTIACheckBox = null;
    private CheckBox mOpenApWpsCheckBox = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Xlog.v(TAG, "-->onCreate");
		thisContext = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi);

		mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

		intent = new Intent(WiFi.this, WifiStateListener.class);
		
		if(mWifiManager != null)
		{
			if (mWifiManager.getWifiState() != mWifiManager.WIFI_STATE_DISABLED) {
				wifiStateFlag = false; 
	      		new AlertDialog.Builder(this).setTitle("Error").setCancelable(false).setMessage(
	                "Please turn off Wi-Fi first!").setPositiveButton("OK",
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog,int which) {
								dialog.dismiss();
								finish();
						}
					}).show();
	        	}
			else
			{
				wifiStateFlag = true; 
			}
		}else
		{
			new AlertDialog.Builder(this)
			.setTitle("Error")
			.setCancelable(false)
			.setMessage("Please check your Wi-Fi state")
			.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						finish();
					}
					}).show();
		}
		WiFi_listView = (ListView) findViewById(R.id.ListView_WiFi);
		List<String> items = new ArrayList<String>();
					items.add("Tx");
					items.add("Rx");
					items.add("MCR");
					items.add("NVRAM");
					//items.add("Temperature Sensor");
					
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
							android.R.layout.simple_list_item_1, items);
					WiFi_listView.setAdapter(adapter);
		if(null != WiFi_listView)
					{
						WiFi_listView.setOnItemClickListener(this);
					}
					else
					{
						Xlog.w(TAG, "findViewById(R.id.ListView_WiFi) failed");
					}
		mHandler = new Handler() {

			public void handleMessage(Message msg) {

				switch (msg.what) {
				case HANDLER_EVENT_INIT:
					//if(!mbBackgroundFlag)
					//{
					//	removeDialog(DIALOG_WIFI_INIT);  
					//}
                                        removeDialog(DIALOG_WIFI_INIT);
					checkWiFiChipState();
					Xlog.d(TAG, "The Handle event is : HANDLER_EVENT_INIT, miChipID = " + miChipID);
					List<String> items = new ArrayList<String>();
					items.add("Tx");
					items.add("Rx");
					items.add("MCR");
					items.add("NVRAM");
					if(miChipID == 0x6620)
					{
						//items.add("DPD Calibration");
					}
					//items.add("Temperature Sensor");
					
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(thisContext,
							android.R.layout.simple_list_item_1, items);
					WiFi_listView.setAdapter(adapter);
					showVersion();
					break;

				default:
					break;
				}
			}
		};
		if(wifiStateFlag)
		{
			showDialog(DIALOG_WIFI_INIT);
			new Thread() {
					public void run() {			
						if(mWiFiStateManager == null)
						{
							mWiFiStateManager = new WiFiStateManager(thisContext);
						}
						miChipID = mWiFiStateManager.checkState(thisContext);
						mHandler.sendEmptyMessage(HANDLER_EVENT_INIT);
					}
				}.start();
			
		}
		
		startService(intent);
		
		mWFACheckBox = (CheckBox)findViewById(R.id.wifi_wfa_switcher);
		if(mWFACheckBox!=null){
		    mWFACheckBox.setOnClickListener(new View.OnClickListener() {
		        public void onClick(View v) {
		            boolean newState = mWFACheckBox.isChecked();
		            SystemProperties.set(KEY_PROP_WPAWPA2, newState?"true":"false");
		        }
		    });
		}
		mCTIACheckBox = (CheckBox)findViewById(R.id.wifi_ctia_switcher);
		if (mCTIACheckBox!=null) {
			mCTIACheckBox.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
		            boolean newState = mCTIACheckBox.isChecked();
		            SystemProperties.set(KEY_PROP_CTIA, newState?"1":"0");
		        }
			});
		}
		mOpenApWpsCheckBox = (CheckBox)findViewById(R.id.wifi_open_ap_wps_switcher);
		if (mOpenApWpsCheckBox!=null) {
			mOpenApWpsCheckBox.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
		            boolean newState = mOpenApWpsCheckBox.isChecked();
		            SystemProperties.set(KEY_PROP_OPEN_AP_WPS, newState?"true":"false");
		        }
			});
		}
}

		@Override
	protected Dialog onCreateDialog(int id) {
		Xlog.v(TAG, "-->onCreateDialog");
		if (id == DIALOG_WIFI_INIT) {
			ProgressDialog dialog = new ProgressDialog(WiFi.this);

			// It would be more efficient to reuse this dialog by moving
			// this setMessage() into onPreparedDialog() and NOT use
			// removeDialog(). However, this is not possible since the
			// message is rendered only 2 times in the ProgressDialog -
			// after show() and before onCreate.
			if(dialog != null)
			{
				dialog.setTitle("Progress");
				dialog.setMessage("Please wait for device to initialize ...");
				dialog.setCancelable(false);
				dialog.setIndeterminate(true);
				dialog.show();
				Xlog.v(TAG, "new ProgressDialog succeed");
			}
			else
			{
				Xlog.w(TAG, "new ProgressDialog failed");
			}

			return dialog;
		}

		return null;
	}
	
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    //	mIsChecked.setEnabled(false);
	}
	
		protected void onStart() {
			Xlog.v(TAG, "-->onStart");
			super.onStart();	
			
			
		}
		
		protected void onResume() {
			Xlog.v(TAG, "-->onResume");
			super.onResume();
			mbBackgroundFlag = false;
			if(mWFACheckBox!=null){
			    mWFACheckBox.setChecked("true".equals(SystemProperties.get(KEY_PROP_WPAWPA2, "false")));
			}
			if (mCTIACheckBox!=null) {
				mCTIACheckBox.setChecked(1 == SystemProperties.getInt(KEY_PROP_CTIA, 0));
			}
			if(mOpenApWpsCheckBox!=null){
			    mOpenApWpsCheckBox.setChecked("true".equals(SystemProperties.get(KEY_PROP_OPEN_AP_WPS, "false")));
			}
		}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Xlog.v(TAG, "-->onItemClick, item index = " + arg2);
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.putExtra(chipIDName, miChipID);
		if(miChipID == 0x6620)
		{
		//for mt6620
			switch (arg2) {
			case 0:	//Tx test
				intent.setClass(this, WiFi_Tx_6620.class);
				break;
			case 1:	//Rx test
				intent.setClass(this, WiFi_Rx_6620.class);
				break;
			case 2:	//MCR read and write
				intent.setClass(this, WiFi_MCR.class);
				break;
			case 3:	//EEPROM read and write
				intent.setClass(this, WiFi_EEPROM.class);	
				break;
			case 4:	//DPD calibration
				intent.setClass(this, WiFi_DPDCalibration.class);
				break;
			case 5:	//Temperature sensor test
				intent.setClass(this, WiFi_TemperatureSensor.class);
				break;
			}
		}else
		{
		//for mt5921
			switch (arg2) {
			case 0:	//Tx test
				intent.setClass(this, WiFi_Tx.class);
				break;
			case 1:	//Rx test
				intent.setClass(this, WiFi_Rx.class);
				break;
			case 2:	//MCR read and write
				intent.setClass(this, WiFi_MCR.class);
				break;
			case 3:	//EEPROM read and write
				intent.setClass(this, WiFi_EEPROM.class);	
				break;
			case 4:	//Temperature sensor test
				intent.setClass(this, WiFi_TemperatureSensor.class);
				break;
			}
		}
		this.startActivity(intent);
	}
	
	private void showVersion() {
	    TextView mVersion = (TextView) findViewById(R.id.wifi_version);
        if (null != mVersion) {
            if (EMWifi.isIntialed) {
                String version = "VERSION: CHIP = MT";
                long[] u4Version = new long[1];
                int result = EMWifi.getATParam(47, u4Version);
                if (0 == result) {
                    Xlog.v(TAG, "version number is: 0x" + Long.toHexString(u4Version[0]));
                    version += Long.toHexString((u4Version[0] & 0xFFFF0000) >> 16);
                    version += "  FW VER = v";
                    version += Long.toHexString((u4Version[0] & 0xFF00) >> 8);
                    version += ".";
                    version += Long.toHexString(u4Version[0] & 0xFF);
                    mVersion.setText(version);
                } else {
                    mVersion.setText("VERSION: Fail to get");
                }
            } else {
                mVersion.setText("VERSION: UNKNOWN");
            }
            Xlog.v(TAG, "Wifi Chip Version is: " + mVersion.getText());
        } else {
            Xlog.d(TAG, "Version TextView is null");
        }
	}

	protected void onStop() {
		Xlog.v(TAG, "-->onStop");
		super.onStop();
		mbBackgroundFlag = true;
	}

	protected void onDestroy() {
		Xlog.v(TAG, "-->onDestroy");
		mWiFiStateManager = null;
		super.onDestroy();
		/*
		if (mInitDialog != null){
							mInitDialog.dismiss();
							mInitDialog = null;
		}
		*/
		if (EMWifi.isIntialed) {
			EMWifi.setNormalMode();
			EMWifi.UnInitial();
			EMWifi.isIntialed = false;
			if (mWifiManager.setWifiEnabled(false))
			{
				Xlog.v(TAG, "disable wifi power succeed");
			}
			else
			{
				Xlog.w(TAG, "disable wifi power failed");
			}	
		}
	}
	
/*	
	@Override
    protected void onPrepareDialog(int id, Dialog dialog) {
		Log.e(TAG, "-->onPrepareDialog");
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		Log.e(TAG, "-->onCreateDialog");
		if (id == DIALOG_WIFI_INIT) {
			Log.e(TAG, "dialog id = " + id);
			ProgressDialog dialog = new ProgressDialog(WiFi.this);

			// It would be more efficient to reuse this dialog by moving
			// this setMessage() into onPreparedDialog() and NOT use
			// callerContext.removeDialog(). However, this is not possible since the
			// message is rendered only 2 times in the ProgressDialog -
			// after show() and before onCreate.
			if(dialog != null)
			{
				dialog.setTitle("Progress");
				dialog.setMessage("Initialization ...");
				dialog.setCancelable(false);
				dialog.setIndeterminate(true);
				dialog.show();
			}
			else
			{
				Log.e(TAG, "new ProgressDialog failed");
			}

			return dialog;
		}

		return null;
	}
*/	
	private void checkWiFiChipState()
	{
	/*
		mInitDialog = new ProgressDialog(WiFi.this);
	            mInitDialog.setMessage("Please wait");
	            mInitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            mInitDialog.setMax(100);
	            mInitDialog.setProgress(0);
	            mInitDialog.show();
	*/
		//showDialog(DIALOG_WIFI_INIT);
				
				switch(miChipID)
				{
					case WiFiStateManager.ENABLEWIFIFAIL:
						//removeDialog(DIALOG_WIFI_INIT);
						/*
						if (mInitDialog != null){
							mInitDialog.dismiss();
							mInitDialog = null;
						}
						*/
						if(!mbBackgroundFlag)
						{
							new AlertDialog.Builder(this).setTitle("Error").setCancelable(false).setMessage(
		                                                "enable Wi-Fi failed").setPositiveButton("OK",
		                                                new DialogInterface.OnClickListener() {
		                                                        public void onClick(DialogInterface dialog,int which) {
		                                                                // TODO Auto-generated method stub  
										dialog.dismiss();
		                                                                finish();
		                                                        }
		                                                }).show();
		                 }
		                 else
		                {
		                	finish();
		                }
					break;
					case WiFiStateManager.CHIPREADY:	
					//when wifi activity is first created, this state is a fault state
					case WiFiStateManager.INVALIDCHIPID:
					case WiFiStateManager.SETTESTMODEFAIL:
						//removeDialog(DIALOG_WIFI_INIT);
						/*
						if (mInitDialog != null){
							mInitDialog.dismiss();
							mInitDialog = null;
						}
						*/
						if(!mbBackgroundFlag)
						{
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
		                }
		                 else
		                {
		                		mWiFiStateManager.disableWiFi();	 
		                        finish();
		                }
					break;
					case 0x6620:
					case 0x5921:
					break;
					default:
					
					break;
				}
				//removeDialog(DIALOG_WIFI_INIT);
				/*
				if (mInitDialog != null){
							mInitDialog.dismiss();
							mInitDialog = null;
				}
				*/
	}
	

}
