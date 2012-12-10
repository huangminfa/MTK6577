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

import com.mediatek.engineermode.ChipSupport;
import com.mediatek.engineermode.R;
import java.lang.Thread;
import java.lang.InterruptedException;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;
import com.mediatek.engineermode.wifi.EMWifi;
import com.mediatek.xlog.Xlog;
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

public class WiFi_Tx_6620 extends Activity implements OnClickListener {
	private final String TAG = "EM/WiFi_Tx";
	private final long MAX_VALUE = 0xFFFFFFFF;
	private final int MIN_VALUE = 0x00;
	private final int mMaxLowRateNumber = 12;
	private final int mMaxHighRateNumber = 21;
	private final int mCCKRateNumber = 4;
	private boolean mHighRateSelected = false;
	private boolean mCCKRateSelected = true;
	private long chipID = 0x00;
	private int mLastRateGroup = 0;
	private int mLastBandwidth = 0;
	private Spinner mChannelSpinner;
	private Spinner mGuardIntervalSpinner;
	private Spinner mBandwidthSpinner;
	private Spinner mPreambleSpinner;
	private EditText mPktEdit;
	private EditText mPktcntEdit;
	private EditText mTxGainEdit;
	private Spinner mRateSpinner;
	private Spinner mModeSpinner;
	//private EditText mXTEdit;
	//private Button mWriteBtn;
	//private Button mReadBtn;
	//private CheckBox mALCCheck;
	private Button mGoBtn;
	private Button mStopBtn;
	private ArrayAdapter<String> mChannelAdapter;
	private ArrayAdapter<String> mRateAdapter;
	private ArrayAdapter<String> mModeAdapter;
	private ArrayAdapter<String> mPreambleAdapter;
	private ArrayAdapter<String> mGuardIntervalAdapter;
	private ArrayAdapter<String> mBandwidthAdapter;
	private int mModeIndex = 0;
	private int mPreambleIndex = 0;
	private int mBandwidthIndex = 0;
	private int mGuardIntervalIndex = 0;
	private int u4Antenna = 0;

	private RateInfo mRate;
	private ChannelInfo mChannel;
	private long mPktNum = 1024;
	private long mCntNum = 3000;
	private long mTxGainVal = 0;
	private boolean mbTestInPorcess = false;
	private WiFiStateManager mWiFiStateManager = null;
	private Handler mHandler;
	private Handler eventHandler;
	// private boolean fgTxHandlerUsed = false;
	private TxTestThread txTestThread = null;
	private final int HANDLER_EVENT_GO = 1;
	private final int HANDLER_EVENT_STOP = 2;
	private final int HANDLER_EVENT_TIMER = 3;
	private final int HANDLER_EVENT_FINISH = 4;
	String[] mMode = { 
		"continuous packet tx", 
		"100% duty cycle",
		"carrier suppression",
		"local leakage", 
		"enter power off" 
		};
	String[] mPreamble = { 
		"Normal",
		"CCK short",
		"802.11n mixed mode", 
		"802.11n green field",
		};
	String[] mBandwidth = { 
		"20MHz", 
		"40MHz",
		"U20MHz",
		"L20MHz",
		};
	String[] mGuardInterval = { 
		"800ns", 
		"400ns",
		};
		
		
	class RateInfo {
		private final short EEPROM_RATE_GROUP_CCK = 0;
		private final short EEPROM_RATE_GROUP_OFDM_6_9M = 1;
		private final short EEPROM_RATE_GROUP_OFDM_12_18M = 2;
		private final short EEPROM_RATE_GROUP_OFDM_24_36M = 3;
		private final short EEPROM_RATE_GROUP_OFDM_48_54M = 4;
		private final short EEPROM_RATE_GROUP_OFDM_MCS0_32 = 5;

		int mRateIndex = 0;

		private final short[] mUcRateGroupEep = { 
			EEPROM_RATE_GROUP_CCK,
			EEPROM_RATE_GROUP_CCK, 
			EEPROM_RATE_GROUP_CCK,
			EEPROM_RATE_GROUP_CCK, 
			EEPROM_RATE_GROUP_OFDM_6_9M,
			EEPROM_RATE_GROUP_OFDM_6_9M, 
			EEPROM_RATE_GROUP_OFDM_12_18M,
			EEPROM_RATE_GROUP_OFDM_12_18M, 
			EEPROM_RATE_GROUP_OFDM_24_36M,
			EEPROM_RATE_GROUP_OFDM_24_36M, 
			EEPROM_RATE_GROUP_OFDM_48_54M,
			EEPROM_RATE_GROUP_OFDM_48_54M,
			/*for future use*/
			EEPROM_RATE_GROUP_OFDM_MCS0_32,
			EEPROM_RATE_GROUP_OFDM_MCS0_32,
			EEPROM_RATE_GROUP_OFDM_MCS0_32,
			EEPROM_RATE_GROUP_OFDM_MCS0_32,
			EEPROM_RATE_GROUP_OFDM_MCS0_32,
			EEPROM_RATE_GROUP_OFDM_MCS0_32,
			EEPROM_RATE_GROUP_OFDM_MCS0_32,
			EEPROM_RATE_GROUP_OFDM_MCS0_32,
			EEPROM_RATE_GROUP_OFDM_MCS0_32,
			};
		private final String[] mpszRate = { 
			"1M", 
			"2M", 
			"5.5M", 
			"11M", 
			"6M",
			"9M", 
			"12M", 
			"18M", 
			"24M", 
			"36M", 
			"48M", 
			"54M",
			/*for future use*/
			"MCS0",
			"MCS1",
			"MCS2",
			"MCS3",
			"MCS4",
			"MCS5",
			"MCS6",
			"MCS7",
			"MCS32",
			
			};

		private final int[] mRateCfg = { 
			2, 
			4, 
			11, 
			22, 
			12, 
			18, 
			24, 
			36, 
			48, 
			72,
			96, 
			108,
			/*here we need to add cfg data for MCS****/
			22, 
			12, 
			18, 
			24, 
			36, 
			48, 
			72,
			96,
			108
			};

		int getRateNumber() {
			return mpszRate.length;
		}

		String getRate() {
			return mpszRate[mRateIndex];
		}

		int getRateCfg() {
			return mRateCfg[mRateIndex];
		}

		int getUcRateGroupEep() {
			return mUcRateGroupEep[mRateIndex];
		}
	}

	protected void onStart() {
		super.onStart();
		checkWiFiChipState();
		mChannelAdapter.clear();
		for(int i=1; i<=mChannel.mChannels[0]; i++) {
		    if (mChannel.mChannels[i] > 14) {
		        break;
		    }
		    for (String s : mChannel.mFullChannelName) {
		        if (s.startsWith("Channel " + mChannel.mChannels[i] + " ")) {
		            mChannelAdapter.add(s);
		            break;
		        }
		    }
		}
				/*
                if (!EMWifi.isIntialed) {
                	//WiFi_Tx_6620 is for both MT6620 only
						chipID = EMWifi.initial();
		            	if (chipID != 0x6620) {
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


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_tx_6620);

		mChannelSpinner = (Spinner) findViewById(R.id.WiFi_Channel_Spinner);
		mPreambleSpinner = (Spinner) findViewById(R.id.WiFi_Preamble_Spinner);
		mPktEdit = (EditText) findViewById(R.id.WiFi_Pkt_Edit);
		mPktcntEdit = (EditText) findViewById(R.id.WiFi_Pktcnt_Edit);
		mTxGainEdit = (EditText) findViewById(R.id.WiFi_Tx_Gain_Edit);//Tx gain
		mRateSpinner = (Spinner) findViewById(R.id.WiFi_Rate_Spinner);
		mModeSpinner = (Spinner) findViewById(R.id.WiFi_Mode_Spinner);
		//mXTEdit = (EditText) findViewById(R.id.WiFi_XtalTrim_Edit);
		//mWriteBtn = (Button) findViewById(R.id.WiFi_XtalTrim_Write);
		//mReadBtn = (Button) findViewById(R.id.WiFi_XtalTrim_Read);
		//mALCCheck = (CheckBox) findViewById(R.id.WiFi_ALC);
		mGoBtn = (Button) findViewById(R.id.WiFi_Go);
		mStopBtn = (Button) findViewById(R.id.WiFi_Stop);
		mBandwidthSpinner = (Spinner) findViewById(R.id.WiFi_Bandwidth_Spinner);
		mGuardIntervalSpinner = (Spinner) findViewById(R.id.WiFi_Guard_Interval_Spinner);

		 txTestThread = new TxTestThread();
		 if(txTestThread != null)
		 {
		 	txTestThread.start();
		 }
		 /*
		if(null != mWriteBtn)
		{
			mWriteBtn.setOnClickListener(this);
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_XtalTrim_Write) failed");
		}
		
		if(null != mReadBtn)
		{
			mReadBtn.setOnClickListener(this);
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_XtalTrim_Read) failed");
		}
		*/
		if(null != mGoBtn)
		{
			mGoBtn.setOnClickListener(this);
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_Go) failed");
		}
		if(null != mStopBtn)
		{
			mStopBtn.setOnClickListener(this);
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.mStopBtn) failed");
		}
		

		mChannel = new ChannelInfo();
		mRate = new RateInfo();

		/*
		 * mThread = new Thread(null, setNormalPktTx, "WiFi_Tx_6620");
		 * fgTxHandlerUsed = false; pfgEnable = false;
		 */
		if(null != mPktcntEdit)
		{
			mPktcntEdit.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent Event) {
					CharSequence inputVal = mPktcntEdit.getText();
					if (TextUtils.equals(inputVal, "0")) {
						Toast.makeText(WiFi_Tx_6620.this,"Do not accept packet count = 0, unlimited on Android",Toast.LENGTH_SHORT).show();
						mPktcntEdit.setText("3000");
					}
					return false;
				}
			});
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_Pkt_Edit) failed");
		}

//		HandlerThread setNormalPktTx = new HandlerThread("Wifi_Tx_Event");
//		setNormalPktTx.start();
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {

				case HANDLER_EVENT_FINISH:
					Xlog.v(TAG, "receive HANDLER_EVENT_FINISH");
					setViewEnabled(true);
					//mGoBtn.setEnabled(true);
					break;

				default:
					break;
				}
			}
		};
		mChannelAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		mChannelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
						new AlertDialog.Builder(WiFi_Tx_6620.this).setTitle("Error").setCancelable(false).setMessage(
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
					Xlog.i(TAG, "The channel freq =" + number);
					uiUpdateTxPower();
				}
	
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
				}
			});
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_Channel_Spinner) failed");
		}
		mRateAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
		mRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < mRate.getRateNumber(); i++) {
			mRateAdapter.add(mRate.mpszRate[i]);
		}
		if(null != mRateSpinner)
		{
			mRateSpinner.setAdapter(mRateAdapter);
			mRateSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					// TODO Auto-generated method stub
					if (!EMWifi.isIntialed) {
						Xlog.w(TAG, "Wifi is not initialized");
						new AlertDialog.Builder(WiFi_Tx_6620.this).setTitle("Error").setCancelable(false).setMessage(
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
				
					mRate.mRateIndex = arg2;
					
					//set Tx Rate
					Xlog.i(TAG, "The mRateIndex is : " + arg2);
					//TODO: need to change preamble items for user to select
					//judge if high rate item selected MCS0~MCS7 MCS32
					mHighRateSelected = arg2 >= mMaxLowRateNumber ? true : false;
					if(mHighRateSelected)
					{
						mPreambleAdapter.clear();
						mPreambleAdapter.add(mPreamble[2]);
						mPreambleAdapter.add(mPreamble[3]);
					}
					else
					{
						mPreambleAdapter.clear();
						mPreambleAdapter.add(mPreamble[0]);
						mPreambleAdapter.add(mPreamble[1]);
					}
					
					mPreambleIndex = mHighRateSelected ? 2 : 0;
					uiUpdateTxPower();
					
					
					if (arg2 >= mCCKRateNumber) {
						if (mCCKRateSelected) {
							mCCKRateSelected = false;
							mModeAdapter.remove(mMode[2]);
							mModeSpinner.setSelection(0);
						}
					} else {
						if (!mCCKRateSelected) {
							mCCKRateSelected = true;
							mModeAdapter.insert(mMode[2], 2);
							mModeSpinner.setSelection(0);
						}
					}
					updateChannels();
					mLastRateGroup = mRate.getUcRateGroupEep();
				}
	
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
				}
			});
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_Rate_Spinner) failed");
		}
		mModeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		mModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < mMode.length; i++) {
			mModeAdapter.add(mMode[i]);
		}
		if(null != mModeSpinner)
		{
			mModeSpinner.setAdapter(mModeAdapter);
			mModeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					
					mModeIndex = arg2;
					Xlog.i(TAG, "The mModeIndex is : " + arg2);
					if (!mCCKRateSelected) {
						if (arg2 >= 2) {
							mModeIndex++;
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
			Xlog.w(TAG, "findViewById(R.id.WiFi_Mode_Spinner) failed");
		}
		//802.11n select seetings
		mPreambleAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
		mPreambleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < mPreamble.length; i++) {
			mPreambleAdapter.add(mPreamble[i]);
		}
		if(null != mPreambleAdapter)
		{
			mPreambleSpinner.setAdapter(mPreambleAdapter);
			mPreambleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					mPreambleIndex = arg2  + (mHighRateSelected ? 2 : 0);
					
				}
	
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
				}
			});
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_11n_Mode_Spinner) failed");
		}
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
					updateChannels();
					mLastBandwidth = mBandwidthIndex;
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
		//Guard Interval seetings
		mGuardIntervalAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
		mGuardIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < mGuardInterval.length; i++) {
			mGuardIntervalAdapter.add(mGuardInterval[i]);
		}
		if(null != mGuardIntervalAdapter)
		{
			mGuardIntervalSpinner.setAdapter(mGuardIntervalAdapter);
			mGuardIntervalSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					//max  GuardInterval setting value is 2
					mGuardIntervalIndex = arg2 < 2 ? arg2 : mGuardIntervalIndex;
					
				}
	
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
				}
			});
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.WiFi_GuardInterval_Spinner) failed");
		}
		setViewEnabled(true);
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
		Xlog.d(TAG, "view_id = " + view.getId());

		switch (view.getId()) {

		/*
		case R.id.WiFi_XtalTrim_Write:
			//onClickBtnXtaltrimWrite();

			break;

		case R.id.WiFi_XtalTrim_Read:
			//onClickBtnXtalTrimRead();
			break;
		*/
		case R.id.WiFi_Go:
			onClickBtnTxGo();

			break;

		case R.id.WiFi_Stop:
			onClickBtnTxStop();

			break;

		default:
			break;
		}
	}

	protected void onStop() {
		super.onStop();
		if(true)return;
		if(eventHandler != null)
		{
		//	eventHandler.sendEmptyMessage(HANDLER_EVENT_STOP);
			eventHandler.removeMessages(HANDLER_EVENT_TIMER);
			if (mbTestInPorcess)
			{
				EMWifi.setATParam(1, 0);
				mbTestInPorcess = false;
			}
			eventHandler.getLooper().quit();
		}
		super.onStop();
		finish();
	}

        protected void onDestroy() {
                if(eventHandler != null)
		{
		//	eventHandler.sendEmptyMessage(HANDLER_EVENT_STOP);
			eventHandler.removeMessages(HANDLER_EVENT_TIMER);
			if (mbTestInPorcess)
			{
				if (EMWifi.isIntialed) {
				        EMWifi.setATParam(1, 0);
                                }
				mbTestInPorcess = false;
			}
			eventHandler.getLooper().quit();
		}
                super.onDestroy();
        }

	private void uiUpdateTxPower() {
		short ucGain = 0;
		long i4TxPwrGain = 0;
		long i4OutputPower = 0;
		long i4targetAlc = 0;
		int len = 3;
		long[] Gain = new long[len];
		int comboChannelIndex = mChannel.getChannelIndex();
		//40MHz 0x8000 | mChannel.mChannelIndex else mChannel.mChannelIndex
		comboChannelIndex |= ((mBandwidthIndex == 1) ? 0x8000 : 0x0000);
		// may change to array[3];
		Xlog.w(TAG, "channelIdx "+comboChannelIndex+" rateIdx "+mRate.mRateIndex+ " gain "+ Gain+" Len "+len);
		if (0 == EMWifi.readTxPowerFromEEPromEx(comboChannelIndex, mRate.mRateIndex, Gain, len))
		{
			i4TxPwrGain = Gain[0];
			i4OutputPower = Gain[1];
			i4targetAlc = Gain[2];
	
			Xlog.i(TAG, "i4TxPwrGain from uiUpdateTxPower is " + i4TxPwrGain);
			ucGain = (short) (i4TxPwrGain & 0xFF);
		}
		/*
		if (ucGain == 0x00 || ucGain == 0xFF) {
			if (mRate.getUcRateGroupEep() <= mRate.EEPROM_RATE_GROUP_CCK) {
				mTxGainEdit.setText("20");
			} else {
				mTxGainEdit.setText("22");
			}
		} else {
//			long val = ucGain;
			mTxGainEdit.setText(Long.toHexString(ucGain));
		}
		*/
		mTxGainEdit.setText(String.format("%.1f", ucGain/2.0));
		//mTxGainEdit.setText(Long.toHexString(ucGain));
	}
	
	private void updateChannels() {
	    boolean bUpdateWifiChannel = false;
	    if (mChannel.mHas14Ch) {
	        if (mLastRateGroup != mRate.getUcRateGroupEep()) {
	            if (mRate.EEPROM_RATE_GROUP_CCK == mRate.getUcRateGroupEep()) {
	                if (mChannel.mHasUpper14Ch) {
	                    int index = mChannelAdapter.getPosition(mChannel.mFullChannelName[14]);
	                    if (-1 != index) {
	                        mChannelAdapter.insert(mChannel.mFullChannelName[13], index);
	                        bUpdateWifiChannel = true;
	                    } else {
	                        mChannelAdapter.add(mChannel.mFullChannelName[13]);
	                        bUpdateWifiChannel = true;
	                    }
	                } else {
	                    mChannelAdapter.add(mChannel.mFullChannelName[13]);
	                    bUpdateWifiChannel = true;
	                }
	            } else if (mRate.EEPROM_RATE_GROUP_CCK == mLastRateGroup) {
	                mChannelAdapter.remove(mChannel.mFullChannelName[13]);
	                bUpdateWifiChannel = true;
	            }
	        }
	    }
	    if (mChannel.mHasUpper14Ch) {
	        if (mLastRateGroup != mRate.getUcRateGroupEep()) {
	            if (mRate.EEPROM_RATE_GROUP_CCK == mLastRateGroup) {
	                for (int i = 1; i <= mChannel.mChannels[0];i++) {
	                    if (mChannel.mChannels[i] > 14) {
	                        for (String s : mChannel.mFullChannelName) {
	                            if (s.startsWith("Channel " + mChannel.mChannels[i] + " ")) {
	                                mChannelAdapter.add(s);
	                                bUpdateWifiChannel = true;
	                                break;
	                            }
	                        }
	                    }
	                }
	            } else if (mRate.EEPROM_RATE_GROUP_CCK == mRate.getUcRateGroupEep()) {
	                for (int i = 14; i<mChannel.mFullChannelName.length;i++) {
	                    mChannelAdapter.remove(mChannel.mFullChannelName[i]);
	                    bUpdateWifiChannel = true;
	                }
	            }
	        }
	    }
	    if (mLastBandwidth != mBandwidthIndex) {
	        if (mBandwidthIndex == 1) {
	            mChannelAdapter.remove(mChannel.mFullChannelName[0]);
	            mChannelAdapter.remove(mChannel.mFullChannelName[1]);
	            mChannelAdapter.remove(mChannel.mFullChannelName[11]);
	            mChannelAdapter.remove(mChannel.mFullChannelName[12]);
	            bUpdateWifiChannel = true;
	        }
	        if (mLastBandwidth == 1) {
	            mChannelAdapter.insert(mChannel.mFullChannelName[0], 0);
	            mChannelAdapter.insert(mChannel.mFullChannelName[1], 1);
	            if (mChannel.isContains(12)) {
	                mChannelAdapter.insert(mChannel.mFullChannelName[11], 11);
	            }
	            if (mChannel.isContains(13)) {
	                mChannelAdapter.insert(mChannel.mFullChannelName[12], 12);
	            }
	            bUpdateWifiChannel = true;
	        }
	    }
	    if (bUpdateWifiChannel) {
	        updateWifiChannel();
	    }
	}
	
	private void updateWifiChannel() {
	    if (!EMWifi.isIntialed) {
            Xlog.w(TAG, "Wifi is not initialized");
            new AlertDialog.Builder(WiFi_Tx_6620.this).setTitle("Error").setCancelable(false).setMessage(
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
	        uiUpdateTxPower();
	    }
	}
/*
	private void onClickBtnXtalTrimRead() {
		long[] val = new long[1];
		EMWifi.getXtalTrimToCr(val);
		Log.d(TAG, "VAL=" + val[0]);
		mXTEdit.setText(String.valueOf(val[0]));
	}

	private void onClickBtnXtaltrimWrite() {
		long u4XtalTrim = 0;
		try {
			u4XtalTrim = Long.parseLong(mXTEdit.getText().toString());
		} catch (NumberFormatException e) {
			Toast.makeText(WiFi_Tx_6620.this, "invalid input value",
					Toast.LENGTH_SHORT).show();
			return;
		}

		Log.d(TAG, "u4XtalTrim =" + u4XtalTrim);
		EMWifi.setXtalTrimToCr(u4XtalTrim);
	}
*/
	private void onClickBtnTxGo() {
		long u4TxGainVal = 0;
		int i = 0;
		long pktNum;
		long cntNum;
		CharSequence inputVal;

		try {
			float pwrVal = Float.parseFloat(mTxGainEdit.getText().toString());
			u4TxGainVal = (long)(pwrVal*2);
			mTxGainEdit.setText(String.format("%.1f", u4TxGainVal/2.0));
			//u4TxGainVal = Long.parseLong(mTxGainEdit.getText().toString(), 16);
		} catch (NumberFormatException e) {
			Toast.makeText(WiFi_Tx_6620.this, "invalid input value",
					Toast.LENGTH_SHORT).show();
			return;
		}
		mTxGainVal = u4TxGainVal;
		mTxGainVal = mTxGainVal > 0xff ? 0xff : mTxGainVal;
		mTxGainVal = mTxGainVal < 0 ? 0 : mTxGainVal;
		Xlog.i(TAG, "Wifi Tx Test : " + mMode[mModeIndex]);

		switch (mModeIndex) {
			case 0:
				try {
					pktNum = Long.parseLong(mPktEdit.getText().toString());
					cntNum = Long.parseLong(mPktcntEdit.getText().toString());
				} catch (NumberFormatException e) {
					Toast.makeText(WiFi_Tx_6620.this, "invalid input value",
							Toast.LENGTH_SHORT).show();
					return;
				}
				mPktNum = pktNum;
				mCntNum = cntNum;
				
				break;

			case 1:
				//EMWifi.setOutputPower(mRate.getRateCfg(), u4TxGainVal, u4Antenna);//for mt5921
				//set output power 
				//setp 1:set rate
				//setp 2:set Tx gain			
				//setp 3:set Antenna
				//setp 4:start output power test
				break;

			case 2:
				/*
				int i4ModeType;
				if (mRate.getRateCfg() <= mRate.EEPROM_RATE_GROUP_CCK) {
					i4ModeType = 0;
				} else {
					i4ModeType = 1;
				}

				//EMWifi.setCarrierSuppression(i4ModeType, u4TxGainVal, u4Antenna);//for mt5921
				*/
				//setp 1:set EEPROMRate Info
				//setp 2:set Tx gain			
				//setp 3:set Antenna
				//step 4:start RF Carriar Suppression Test
				
				break;

			case 3:
				//EMWifi.setLocalFrequecy(u4TxGainVal, u4Antenna);//for mt5921
				//setp 1:set Tx gain			
				//setp 2:set Antenna
				//step 3:start Local Frequency Test
				
				break;

			case 4:
				//EMWifi.setNormalMode();
				//EMWifi.setOutputPin(20, 0);
				//EMWifi.setPnpPower(4);
				break;
			default:
				break;
		}
		if(eventHandler != null)
		{
			eventHandler.sendEmptyMessage(HANDLER_EVENT_GO);
			//mGoBtn.setEnabled(false);
			setViewEnabled(false);
		}
		else
		{
			Xlog.w(TAG, "eventHandler = null");
		}
	}
	private void setViewEnabled(boolean state)
	{
		mChannelSpinner.setEnabled(state);
		mGuardIntervalSpinner.setEnabled(state);
		mBandwidthSpinner.setEnabled(state);
		mPreambleSpinner.setEnabled(state);
		mPktEdit.setEnabled(state);
		mPktcntEdit.setEnabled(state);
		mTxGainEdit.setEnabled(state);
		mRateSpinner.setEnabled(state);
		mModeSpinner.setEnabled(state);
		//mXTEdit.setEnabled(state);
		//mWriteBtn.setEnabled(state);
		//mReadBtn.setEnabled(state);
		//mALCCheck.setEnabled(state);
		mGoBtn.setEnabled(state);
		mStopBtn.setEnabled(!state);
	}
	private void onClickBtnTxStop() {
			if(eventHandler != null)
			{
				eventHandler.sendEmptyMessage(HANDLER_EVENT_STOP);
			}
			else
			{
				Xlog.w(TAG, "eventHandler = null");
			}
		switch (mModeIndex) {
		case 0:
			break;

		case 4:
			EMWifi.setPnpPower(1);
			EMWifi.setTestMode();
			EMWifi.setChannel(mChannel.getChannelFreq());
			uiUpdateTxPower();
			//mGoBtn.setEnabled(true);
			break;

		default:
			EMWifi.setStandBy();
			//mGoBtn.setEnabled(true);
			break;
		}
	}

	private class TxTestThread extends Thread
		{
			
			public void run () 
			{
				Xlog.d(TAG, "before Looper.prepare()");
				Looper.prepare();
				Xlog.d(TAG, "after Looper.prepare()");
				eventHandler = new Handler()
				{
					public void handleMessage(Message msg) {
						if (!EMWifi.isIntialed) {
						Xlog.w(TAG, "Wifi is not initialized");
						new AlertDialog.Builder(WiFi_Tx_6620.this).setTitle("Error").setCancelable(false).setMessage(
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
						Xlog.d(TAG, "new msg)");
						long i = 0;
						int rateIndex;
						long[] u4Value = new long[1];
						switch (msg.what) {
						case HANDLER_EVENT_GO:
							{
								switch (mModeIndex) {
									case 0:	
										//EMWifi.setChannel(mChannel.getChannelFreq());
										//set Tx gain of RF
										EMWifi.setATParam(2, mTxGainVal);
										Xlog.i(TAG, "The mPreambleIndex is : " + mPreambleIndex);
										EMWifi.setATParam(4, mPreambleIndex);
										//u4Antenna = 0, never be changed since first valued
										EMWifi.setATParam(5, u4Antenna);
										//set package length, is there a maximum packet length? mtk80758-2010-11-2
										EMWifi.setATParam(6, mPktNum);
										//set package length, is there a maximum packet length? mtk80758-2010-11-2
										//if cntNum = 0, send continious unless stop button is pressed
										EMWifi.setATParam(7, mCntNum);
										//package interval in unit of us, no need to allow user to set this value
										EMWifi.setATParam(8, 0);

										//if (mALCCheck.isChecked() == false) {
											i = 0;
										//} else {
										//	i = 1;
										//}
										//9, means temperature conpensation
										EMWifi.setATParam(9, i);
										
										//TX enable enable ? what does this mean
										EMWifi.setATParam(10, 0x00020000);
										//set Tx content
										EMWifi.setATParam(12, 0xff220004);
										EMWifi.setATParam(12, 0x33440006);
										EMWifi.setATParam(12, 0x55660008);
										EMWifi.setATParam(12, 0x55550019);
										EMWifi.setATParam(12, 0xaaaa001b);
										EMWifi.setATParam(12, 0xbbbb001d);
										//packet retry limit
										EMWifi.setATParam(13, 1);
										//QoS queue	-AC2
										EMWifi.setATParam(14, 2);				
										
										Xlog.i(TAG, "The mGuardIntervalIndex is : " + mGuardIntervalIndex);
										//GuardInterval setting
										EMWifi.setATParam(16, mGuardIntervalIndex);
										Xlog.i(TAG, "The mBandwidthIndex is : " + mBandwidthIndex);
										//Bandwidth setting
										EMWifi.setATParam(15, mBandwidthIndex);
										
										rateIndex = mRate.mRateIndex;
										if(mHighRateSelected)
										{
											rateIndex -= mMaxLowRateNumber;
											if(rateIndex > 0x07)
											{
												rateIndex = 0x20; //for MCS32
											}
											rateIndex |= (1 << 31);
										}
										
										//rateIndex |= (1 << 31);
										Xlog.i(TAG, String.format("TXX rate index = 0x%08x", rateIndex));
										EMWifi.setATParam(3, rateIndex);
										int number = mChannel.getChannelFreq();
										EMWifi.setChannel(number);
										Xlog.i(TAG, "target channel freq =" + mChannel.getChannelFreq());
										//start tx test
										if( 0 == EMWifi.setATParam(1, 1))
										{
											mbTestInPorcess = true;
										}
										sendEmptyMessageDelayed(HANDLER_EVENT_TIMER, 1000);
										break;
									case 1:
										//EMWifi.setOutputPower(mRate.getRateCfg(), u4TxGainVal, u4Antenna);//for mt5921
										//set output power 
										//setp 1:set rate
										//setp 2:set Tx gain			
										//setp 3:set Antenna
										//setp 4:start output power test
										rateIndex = mRate.mRateIndex;
										if(mHighRateSelected)
										{
											rateIndex -= mMaxLowRateNumber;
											if(rateIndex > 0x07)
											{
												rateIndex = 0x20; //for MCS32
											}
											rateIndex |= (1 << 31);
										}
										Xlog.i(TAG, String.format("Tx rate index = 0x%08x", rateIndex));
										EMWifi.setATParam(3, rateIndex);
										EMWifi.setATParam(2, mTxGainVal);
										EMWifi.setATParam(5, u4Antenna);
										//start  output power test
										if( 0 == EMWifi.setATParam(1, 4))
										{
											mbTestInPorcess = true;
										}
										break;

									case 2:
										//setp 1:set EEPROMRate Info
										//setp 2:set Tx gain			
										//setp 3:set Antenna
										//step 4:start RF Carriar Suppression Test
										EMWifi.setATParam(2, mTxGainVal);
										EMWifi.setATParam(5, u4Antenna);
										//start  carriar suppression test
										if(ChipSupport.GetChip() == ChipSupport.MTK_6573_SUPPORT) {
											if( 0 == EMWifi.setATParam(1, 6)) {
												mbTestInPorcess = true;
											}
										} else {
											if (mCCKRateSelected) {
												EMWifi.setATParam(65, 5);
											} else {
												EMWifi.setATParam(65, 2);
											}
											if ( 0 == EMWifi.setATParam(1, 10))	{
												mbTestInPorcess = true;
											}
										}
										break;

									case 3:
										//Wifi.setLocalFrequecy(u4TxGainVal, u4Antenna);
										//setp 1:set Tx gain			
										//setp 2:set Antenna
										//step 3:start Local Frequency Test
										EMWifi.setATParam(2, mTxGainVal);
										EMWifi.setATParam(5, u4Antenna);
										//start  carriar suppression test
										if( 0 == EMWifi.setATParam(1, 5))
										{
											mbTestInPorcess = true;
										}
										break;

									case 4:
										//TODO: need to implement
										//Wifi.setNormalMode();
										//Wifi.setOutputPin(20, 0); 
										//Wifi.setPnpPower(4);
										break;
									default:
										break;
									}
								}
							break;
						
						case HANDLER_EVENT_STOP:
							Xlog.i(TAG, "The Handle event is : HANDLER_EVENT_STOP"); 
								if (mbTestInPorcess)
								{
									u4Value[0] = EMWifi.setATParam(1, 0);
									mbTestInPorcess = false;
								}
								//driver does not support query operation on functionIndex = 1 , we can only judge whether this operation is processed successfully through the return value
									if(eventHandler != null)
									{
										eventHandler.removeMessages(HANDLER_EVENT_TIMER);
									}
								mHandler.sendEmptyMessage(HANDLER_EVENT_FINISH);
						break;
						case HANDLER_EVENT_TIMER:
							u4Value[0] = 0;
							long pktCnt = 0;
							Xlog.i(TAG, "The Handle event is : HANDLER_EVENT_TIMER");
							try {
								pktCnt = Long.parseLong(mPktcntEdit.getText()
										.toString());
							} catch (NumberFormatException e) {
								Toast.makeText(WiFi_Tx_6620.this, "invalid input value",
										Toast.LENGTH_SHORT).show();
								return;
							}
							//here we need to judge whether target number packet is finished sent or not
							if( 0 == EMWifi.getATParam(32, u4Value))
							{
								Xlog.d(TAG, "query Transmitted packet count succeed, count = " + u4Value[0] + " target count = " + pktCnt);
								if(u4Value[0] ==  pktCnt)
								{
									removeMessages(HANDLER_EVENT_TIMER);
									mHandler.sendEmptyMessage(HANDLER_EVENT_FINISH);
									break;
								}
							}
							else
							{
				
								Xlog.w(TAG, "query Transmitted packet count failed");
							}
							
							sendEmptyMessageDelayed(HANDLER_EVENT_TIMER, 1000);

							break;

						default:
							break;
						}
					}
				};
				Xlog.d(TAG, "before Looper.loop()");
				Looper.loop();
				Xlog.d(TAG, "after Looper.loop()");
			}
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
