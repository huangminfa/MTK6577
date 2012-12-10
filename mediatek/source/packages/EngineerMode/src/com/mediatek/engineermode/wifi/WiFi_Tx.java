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

public class WiFi_Tx extends Activity implements OnClickListener {
	private final String TAG = "EM/WiFi_Tx";
	private final long MAX_VALUE = 0xFFFFFFFF;
	private final int MIN_VALUE = 0x00;

	private Spinner mChannelSpinner;
	private EditText mPktEdit;
	private EditText mPktcntEdit;
	private EditText mTxEdit;
	private Spinner mRateSpinner;
	private Spinner mModeSpinner;
	private EditText mXTEdit;
	private Button mWriteBtn;
	private Button mReadBtn;
	private CheckBox mALCCheck;
	private Button mGoBtn;
	private Button mStopBtn;
	private ArrayAdapter<String> mChannelAdapter;
	private ArrayAdapter<String> mRateAdapter;
	private ArrayAdapter<String> mModeAdapter;

	private int mModeIndex = 0;
	private int u4Antenna = 0;
	private long chipID = 0x00;
	
	private RateInfo mRate;
	private ChannelInfo mChannel;

	private myHandler mHandler;
	private Handler eventHandler;
	// private boolean fgTxHandlerUsed = false;
	private WiFiStateManager mWiFiStateManager = null;
	private final int HANDLER_EVENT_GO = 1;
	private final int HANDLER_EVENT_STOP = 2;
	private final int HANDLER_EVENT_TIMER = 3;

	String[] mMode = { "continuous packet tx", "100% duty cycle",
			"carrier suppression", "local leakage", "enter power off" };

	class RateInfo {
		private final short EEPROM_RATE_GROUP_CCK = 0;
		private final short EEPROM_RATE_GROUP_OFDM_6_9M = 1;
		private final short EEPROM_RATE_GROUP_OFDM_12_18M = 2;
		private final short EEPROM_RATE_GROUP_OFDM_24_36M = 3;
		private final short EEPROM_RATE_GROUP_OFDM_48_54M = 4;

		int mRateIndex = 0;

		private final short[] mUcRateGroupEep = { EEPROM_RATE_GROUP_CCK,
				EEPROM_RATE_GROUP_CCK, EEPROM_RATE_GROUP_CCK,
				EEPROM_RATE_GROUP_CCK, EEPROM_RATE_GROUP_OFDM_6_9M,
				EEPROM_RATE_GROUP_OFDM_6_9M, EEPROM_RATE_GROUP_OFDM_12_18M,
				EEPROM_RATE_GROUP_OFDM_12_18M, EEPROM_RATE_GROUP_OFDM_24_36M,
				EEPROM_RATE_GROUP_OFDM_24_36M, EEPROM_RATE_GROUP_OFDM_48_54M,
				EEPROM_RATE_GROUP_OFDM_48_54M };

		private final String[] mpszRate = { "1M", "2M", "5.5M", "11M", "6M",
				"9M", "12M", "18M", "24M", "36M", "48M", "54M" };

		private final int[] mRateCfg = { 2, 4, 11, 22, 12, 18, 24, 36, 48, 72,
				96, 108 };

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

	class myHandler extends Handler {

//		public myHandler(Looper looper) {
//			super(looper);
//		}
		public myHandler() {
		}

		public void handleMessage(Message msg) {

			switch (msg.what) {

			case HANDLER_EVENT_GO:
				EMWifi.setATParam(1, 1);
				eventHandler.sendEmptyMessage(HANDLER_EVENT_TIMER);
				Xlog.i(TAG, "The Handle event is : HANDLER_EVENT_GO");
				break;

			case HANDLER_EVENT_STOP:
				long[] u4Value = new long[1];
				Xlog.i(TAG, "The Handle event is : HANDLER_EVENT_STOP");

				for (int i = 0; i < 100; i++) {
					EMWifi.setATParam(1, 0);
					EMWifi.getATParam(1, u4Value);

					if (u4Value[0] == 0) {		
						eventHandler.removeMessages(HANDLER_EVENT_TIMER);
						mGoBtn.setEnabled(true);
						break;
					} else {
						SystemClock.sleep(1);
					}
				}

				break;

			default:
				break;
			}
		}
	}


	protected void onStart() {
		super.onStart();
		checkWiFiChipState();
				/*
                if (!EMWifi.isIntialed) {
                	//WiFi_Tx is for MT5921 only
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


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_tx);

		mChannelSpinner = (Spinner) findViewById(R.id.WiFi_Channel_Spinner);
		mPktEdit = (EditText) findViewById(R.id.WiFi_Pkt_Edit);
		mPktcntEdit = (EditText) findViewById(R.id.WiFi_Pktcnt_Edit);
		mTxEdit = (EditText) findViewById(R.id.WiFi_Tx_Edit);
		mRateSpinner = (Spinner) findViewById(R.id.WiFi_Rate_Spinner);
		mModeSpinner = (Spinner) findViewById(R.id.WiFi_Mode_Spinner);
		mXTEdit = (EditText) findViewById(R.id.WiFi_XtalTrim_Edit);
		mWriteBtn = (Button) findViewById(R.id.WiFi_XtalTrim_Write);
		mReadBtn = (Button) findViewById(R.id.WiFi_XtalTrim_Read);
		mALCCheck = (CheckBox) findViewById(R.id.WiFi_ALC);
		mGoBtn = (Button) findViewById(R.id.WiFi_Go);
		mStopBtn = (Button) findViewById(R.id.WiFi_Stop);

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
		 * mThread = new Thread(null, setNormalPktTx, "WiFi_Tx");
		 * fgTxHandlerUsed = false; pfgEnable = false;
		 */
		if(null != mPktcntEdit)
		{
			mPktcntEdit.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent Event) {
					CharSequence inputVal = mPktcntEdit.getText();
					if (TextUtils.equals(inputVal, "0")) {
						Toast
							.makeText(
									WiFi_Tx.this,
									"Do not accept packet count = 0, unlimited on Android",
									Toast.LENGTH_SHORT).show();
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
		mHandler = new myHandler();

		eventHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {

				case HANDLER_EVENT_TIMER:
					long[] u4Value = new long[1];
					long pktCnt = 0;
					Xlog.i(TAG, "The Handle event is : HANDLER_EVENT_TIMER");
					try {
						pktCnt = Long.parseLong(mPktcntEdit.getText()
								.toString());
					} catch (NumberFormatException e) {
						Toast.makeText(WiFi_Tx.this, "invalid input value",
								Toast.LENGTH_SHORT).show();
						return;
					}

					if (pktCnt != 0) {
						EMWifi.getATParam(1, u4Value);
						if (u4Value[0] == 0) {
							// if (fgTxHandlerUsed == true) {
							removeMessages(HANDLER_EVENT_TIMER);
							// fgTxHandlerUsed = false;
							// }
							mGoBtn.setEnabled(true);
							break;
						}
					}

					sendEmptyMessageDelayed(HANDLER_EVENT_TIMER, 1000);

					break;

				default:
					break;
				}
			}
		};

		mChannelAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mChannelAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < mChannel.mChannelName.length; i++) {
			mChannelAdapter.add(mChannel.mFullChannelName[i]);
		}
		if(null != mChannelSpinner)
		{
			mChannelSpinner.setAdapter(mChannelAdapter);
			mChannelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					if (!EMWifi.isIntialed) {
						Xlog.w(TAG, "Wifi is not initialized");
						new AlertDialog.Builder(WiFi_Tx.this).setTitle("Error").setCancelable(false).setMessage(
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
		mRateAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mRateAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < mRate.getRateNumber(); i++) {
			mRateAdapter.add(mRate.mpszRate[i]);
		}
		if(null != mRateSpinner)
		{
			mRateSpinner.setAdapter(mRateAdapter);
			mRateSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					mRate.mRateIndex = arg2;
					Xlog.i(TAG, "The mRateIndex is : " + arg2);
					uiUpdateTxPower();
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
		mModeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mModeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

		case R.id.WiFi_XtalTrim_Write:
			onClickBtnXtaltrimWrite();

			break;

		case R.id.WiFi_XtalTrim_Read:
			onClickBtnXtalTrimRead();
			break;

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
		eventHandler.removeMessages(HANDLER_EVENT_TIMER);
		finish();
	}

	private void uiUpdateTxPower() {
		short ucGain = 0;
		long i4TxPwrGain = 0;
		long i4OutputPower = 0;
		long i4targetAlc = 0;
		int len = 3;
		long[] Gain = new long[len];

		// may change to array[3];
		EMWifi.readTxPowerFromEEPromEx(mChannel.getChannelFreq(), mRate
				.getRateCfg(), Gain, len);
		i4TxPwrGain = Gain[0];
		i4OutputPower = Gain[1];
		i4targetAlc = Gain[2];

		Xlog.i(TAG, "i4TxPwrGain from uiUpdateTxPower is " + i4TxPwrGain);
		ucGain = (short) (i4TxPwrGain & 0X01FF);
		if (ucGain == 0x00 || ucGain == 0xFF) {
			if (mRate.getUcRateGroupEep() <= mRate.EEPROM_RATE_GROUP_CCK) {
				mTxEdit.setText("20");
			} else {
				mTxEdit.setText("22");
			}
		} else {
//			long val = ucGain;
			mTxEdit.setText(Long.toHexString(ucGain));
		}
	}

	private void onClickBtnXtalTrimRead() {
		long[] val = new long[1];
		EMWifi.getXtalTrimToCr(val);
		Xlog.d(TAG, "VAL=" + val[0]);
		mXTEdit.setText(String.valueOf(val[0]));
	}

	private void onClickBtnXtaltrimWrite() {
		long u4XtalTrim = 0;
		try {
			u4XtalTrim = Long.parseLong(mXTEdit.getText().toString());
		} catch (NumberFormatException e) {
			Toast.makeText(WiFi_Tx.this, "invalid input value",
					Toast.LENGTH_SHORT).show();
			return;
		}

		Xlog.d(TAG, "u4XtalTrim =" + u4XtalTrim);
		EMWifi.setXtalTrimToCr(u4XtalTrim);
	}

	private void onClickBtnTxGo() {
		long u4TxGainVal = 0;
		int i = 0;
		long pktNum;
		long cntNum;
		CharSequence inputVal;

		try {
			u4TxGainVal = Long.parseLong(mTxEdit.getText().toString(), 16);
		} catch (NumberFormatException e) {
			Toast.makeText(WiFi_Tx.this, "invalid input value",
					Toast.LENGTH_SHORT).show();
			return;
		}

//		Xlog.i(TAG, "The mModeIndex is : " + mModeIndex);

		switch (mModeIndex) {
		case 0:
			try {
				pktNum = Long.parseLong(mPktEdit.getText().toString());
				cntNum = Long.parseLong(mPktcntEdit.getText().toString());
			} catch (NumberFormatException e) {
				Toast.makeText(WiFi_Tx.this, "invalid input value",
						Toast.LENGTH_SHORT).show();
				return;
			}

			mGoBtn.setEnabled(false);

			EMWifi.setATParam(2, u4TxGainVal);
			EMWifi.setATParam(3, mRate.mRateIndex);
			EMWifi.setATParam(4, 0);
			EMWifi.setATParam(5, u4Antenna);
			EMWifi.setATParam(6, pktNum);
			EMWifi.setATParam(7, cntNum);
			EMWifi.setATParam(8, 0);

			if (mALCCheck.isChecked() == false) {
				i = 0;
			} else {
				i = 1;
			}
			EMWifi.setATParam(9, i);

			EMWifi.setATParam(10, 0x00020000);

			EMWifi.setATParam(12, 0xff220004);
			EMWifi.setATParam(12, 0x33440006);
			EMWifi.setATParam(12, 0x55660008);
			EMWifi.setATParam(12, 0x55550019);
			EMWifi.setATParam(12, 0xaaaa001b);
			EMWifi.setATParam(12, 0xbbbb001d);

			EMWifi.setATParam(13, 1);

			EMWifi.setATParam(14, 2);

			// thread;
			// pfgEnable = true;

			mHandler.sendEmptyMessage(HANDLER_EVENT_GO);
			// mThread.start();
			break;

		case 1:
			EMWifi.setOutputPower(mRate.getRateCfg(), u4TxGainVal, u4Antenna);
			break;

		case 2:
			int i4ModeType;
			if (mRate.getRateCfg() <= mRate.EEPROM_RATE_GROUP_CCK) {
				i4ModeType = 0;
			} else {
				i4ModeType = 1;
			}

			EMWifi.setCarrierSuppression(i4ModeType, u4TxGainVal, u4Antenna);
			break;

		case 3:
			EMWifi.setLocalFrequecy(u4TxGainVal, u4Antenna);
			break;

		case 4:
			mGoBtn.setEnabled(false);

			EMWifi.setNormalMode();
			EMWifi.setOutputPin(20, 0);
			EMWifi.setPnpPower(4);
			break;
		default:
			break;
		}
	}

	private void onClickBtnTxStop() {
		switch (mModeIndex) {
		case 0:

			mHandler.sendEmptyMessage(HANDLER_EVENT_STOP);
			// Thread
			// pfgEnable = false;
			// mThread.start();

			break;

		case 4:
			EMWifi.setPnpPower(1);
			EMWifi.setTestMode();
			EMWifi.setChannel(mChannel.getChannelFreq());
			uiUpdateTxPower();
			mGoBtn.setEnabled(true);

			break;

		default:
			EMWifi.setStandBy();
			mGoBtn.setEnabled(true);

			break;
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
