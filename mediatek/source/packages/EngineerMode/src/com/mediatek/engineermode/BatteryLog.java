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

package com.mediatek.engineermode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Environment;


public class BatteryLog extends Activity implements OnClickListener{
	
	private int mLogRecordInterval = 10000;
	private File mLogFile;
	private boolean mIsRecording = false;
	
	private File batterylog = null;
	
	private TextView mStatus;
    private TextView mLevel;
    private TextView mScale;
    private TextView mHealth;
    private TextView mVoltage;
    private TextView mTemperature;
    private TextView mTechnology;
    private TextView mUptime;
    private EditText mIntervalEdit;
    private Button mLogRecord;

    private static final int EVENT_TICK = 1;
    private static final int EVENT_LOG_RECORD = 2;

	private String TAG = "EM-BatteryLog";
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_TICK:
                    updateBatteryStats();
                    sendEmptyMessageDelayed(EVENT_TICK, 1000);                   
                    break;
            }
        }

		private void updateBatteryStats() {
			// TODO Auto-generated method stub
			long uptime = SystemClock.elapsedRealtime();
	        mUptime.setText(DateUtils.formatElapsedTime(uptime / 1000));
		}
    };
    
    /**
     * Format a number of tenths-units as a decimal string without using a
     * conversion to float.  E.g. 347 -> "34.7"
     */
    private final String tenthsToFixedString(int x) {
        int tens = x / 10;
        return new String("" + tens + "." + (x - 10*tens));
    }
    
    /**
     *Listens for intent broadcasts
     */
     private IntentFilter   mIntentFilter;
     private IntentFilter   mIntentFilterSDCard;

     private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
    	 
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String action = arg1.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int plugType = arg1.getIntExtra("plugged", 0);

                mLevel.setText("" + arg1.getIntExtra("level", 0));
                mScale.setText("" + arg1.getIntExtra("scale", 0));
                mVoltage.setText("" + arg1.getIntExtra("voltage", 0) + " "
                        + getString(R.string.battery_info_voltage_units));
                mTemperature.setText("" + tenthsToFixedString(arg1.getIntExtra("temperature", 0))
                        + getString(R.string.battery_info_temperature_units));
                mTechnology.setText("" + arg1.getStringExtra("technology"));
                
                int status = arg1.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                String statusString;
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    statusString = getString(R.string.battery_info_status_charging);
                    if (plugType > 0) {
                        statusString = statusString + " " + getString(
                                (plugType == BatteryManager.BATTERY_PLUGGED_AC)
                                        ? R.string.battery_info_status_charging_ac
                                        : R.string.battery_info_status_charging_usb);
                    }
                } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                    statusString = getString(R.string.battery_info_status_discharging);
                } else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                    statusString = getString(R.string.battery_info_status_not_charging);
                } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                    statusString = getString(R.string.battery_info_status_full);
                } else {
                    statusString = getString(R.string.battery_info_status_unknown);
                }
                mStatus.setText(statusString);

                int health = arg1.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN);
                String healthString;
                if (health == BatteryManager.BATTERY_HEALTH_GOOD) {
                    healthString = getString(R.string.battery_info_health_good);
                } else if (health == BatteryManager.BATTERY_HEALTH_OVERHEAT) {
                    healthString = getString(R.string.battery_info_health_overheat);
                } else if (health == BatteryManager.BATTERY_HEALTH_DEAD) {
                    healthString = getString(R.string.battery_info_health_dead);
                } else if (health == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE) {
                    healthString = getString(R.string.battery_info_health_over_voltage);
                } else if (health == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE) {
                    healthString = getString(R.string.battery_info_health_unspecified_failure);
                } else {
                    healthString = getString(R.string.battery_info_health_unknown);
                }
                mHealth.setText(healthString);
            }
                        
			
		}
     };
     private BroadcastReceiver mIntentReceiverSDCard = new BroadcastReceiver() {
    	 
 		@Override
 		public void onReceive(Context arg0, Intent arg1) {
 			// TODO Auto-generated method stub
 			String action = arg1.getAction();
             if(action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)
             		|| action.equals(Intent.ACTION_MEDIA_REMOVED)
             		|| action.equals(Intent.ACTION_MEDIA_EJECT)
             		 )
             {   
             	if(mIsRecording == false) 
             	{
             		return;
             	}        	
             	mIsRecording = false;
             	mLogHandler.removeMessages(EVENT_LOG_RECORD);
             	mLogRecord.setText(R.string.battery_info_Log_Start);
             	AlertDialog.Builder builder = new AlertDialog.Builder(BatteryLog.this);
 							builder.setTitle("SD card error");
 							builder.setMessage("SD card has been removed.");
 							builder.setPositiveButton("OK" , null);
 							builder.create().show();
             }
 			
 		}
      };
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battery_info);
        
        mLogRecord = (Button) findViewById(R.id.Log_Record);        
        if(mLogRecord == null)
        {
        	Log.e(TAG, "clocwork worked...");	
    		//not return and let exception happened.
        }
        mLogRecord.setOnClickListener(this);
        
        // create the IntentFilter that will be used to listen
        // to battery status broadcasts
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

	mIntentFilterSDCard = new IntentFilter();
        mIntentFilterSDCard.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        mIntentFilterSDCard.addAction(Intent.ACTION_MEDIA_REMOVED);
        mIntentFilterSDCard.addAction(Intent.ACTION_MEDIA_EJECT);
        mIntentFilterSDCard.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        mIntentFilterSDCard.addDataScheme("file");

        //check whether the sdcard exists, if yes, set up batterylog directory, and if not, notify user to plug in it
        File sdcard = null;
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED))        
		{
			sdcard =  Environment.getExternalStorageDirectory();
			batterylog = new File(sdcard.getParent()+"/" + sdcard.getName() + "/batterylog/");
			Log.e(TAG, sdcard.getParent() +"/"+ sdcard.getName() + "/batterylog/");
			if(!batterylog.isDirectory())
			{
				batterylog.mkdirs();
			}
			
		}

	}
	
	@Override
    public void onResume() {
        super.onResume();

        mStatus = (TextView)findViewById(R.id.status);
        mLevel = (TextView)findViewById(R.id.level);
        mScale = (TextView)findViewById(R.id.scale);
        mHealth = (TextView)findViewById(R.id.health);
        mTechnology = (TextView)findViewById(R.id.technology);
        mVoltage = (TextView)findViewById(R.id.voltage);
        mTemperature = (TextView)findViewById(R.id.temperature);
        mUptime = (TextView) findViewById(R.id.uptime);
        mIntervalEdit = (EditText)findViewById(R.id.Log_Record_Interval);
        if(mIntervalEdit == null)
        {
        	Log.e(TAG, "clocwork worked...");	
    		//not return and let exception happened.
        }
        
        mHandler.sendEmptyMessageDelayed(EVENT_TICK, 1000);
        
        registerReceiver(mIntentReceiver, mIntentFilter);
        registerReceiver(mIntentReceiverSDCard, mIntentFilterSDCard);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(EVENT_TICK);
        
        // we are no longer on the screen stop the observers
        unregisterReceiver(mIntentReceiver);
        unregisterReceiver(mIntentReceiverSDCard);
    }

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
		if(arg0.getId() == mLogRecord.getId())
		{	
			if(false == mIsRecording)
			{
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED)
				||  Environment.getExternalStorageState().equals(Environment.MEDIA_BAD_REMOVAL)
				||  Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED))
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("SD Card not available");
					builder.setMessage("Please insert an SD Card.");
					builder.setPositiveButton("OK" , null);
					builder.create().show();
					return;
				}
				
				String state = Environment.getExternalStorageState();
				Log.i(TAG, "Environment.getExternalStorageState() is : " + state);
				
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_SHARED))
        {
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("sdcard is busy");
					builder.setMessage("Sorry, your SD card is busy.");
					builder.setPositiveButton("OK" , null);
					builder.create().show();
					return;
        }				
				//check if the EditText control has no content, if not, check the content whether is right
				
				if(3 < mIntervalEdit.getText().toString().length() || 0 == mIntervalEdit.getText().toString().length())
				{
					Toast.makeText(this, "The input is not correct. Please input the number between 1 and 100.", Toast.LENGTH_LONG).show();
					return;
				}
				if(Integer.valueOf(mIntervalEdit.getText().toString()) > 100 || Integer.valueOf(mIntervalEdit.getText().toString()) < 1)
				{
					Toast.makeText(this, "The input is not correct. Please input the number between 1 and 100.", Toast.LENGTH_LONG).show();
					return;
				}
				mLogRecordInterval = Integer.valueOf(mIntervalEdit.getText().toString()) * 1000;
				Log.i(TAG, String.valueOf(mLogRecordInterval));
				
				mLogRecord.setText(R.string.battery_info_Log_End);
				
				//Create a new file under the "/sdcard/batterylog" path				
				Calendar rightNow = Calendar.getInstance();
				SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddhhmmss");
				String sysDateTime = fmt.format(rightNow.getTime());
				String fileName = "";
				fileName = fileName + sysDateTime;
				fileName = fileName + ".txt";
				Log.i(TAG, fileName);
				
				mLogFile = new File("/sdcard/batterylog/" + fileName);				
				try {
					mLogFile.createNewFile();
					String BatteryInfoLable = "Battery status, level, scale, health, voltage, temperature, technology, time since boot:\n";
					FileWriter fileWriter = new FileWriter(mLogFile);
					fileWriter.write(BatteryInfoLable);
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				mLogHandler.sendEmptyMessageDelayed(EVENT_LOG_RECORD, 1000);
				
				mIsRecording = true;
			}
			else
			{
				mLogRecord.setText(R.string.battery_info_Log_Start);
				mLogHandler.removeMessages(EVENT_LOG_RECORD);
				mIsRecording = false;
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("BatteryLog Saved");
				builder.setMessage("BatteryLog has been saved under" + " /sdcard/batterylog.");
				builder.setPositiveButton("OK" , null);
				builder.create().show();
			}

		}
		
	}
	
	public Handler mLogHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_LOG_RECORD:
                	Log.i(TAG, "Record one time");
                	WriteCurrentBatteryInfo();
                	sendEmptyMessageDelayed(EVENT_LOG_RECORD, mLogRecordInterval);  
                  break;
            }
        }
        
        private void WriteCurrentBatteryInfo() {
        	String LogContent = "";
        	LogContent = LogContent + mStatus.getText() + ", " + mLevel.getText() + ", " + mScale.getText()
        				+ ", " + mHealth.getText() + ", " + mVoltage.getText() + ", " + mTemperature.getText()
        				+ ", " + mTechnology.getText() + ", " + mUptime.getText() + "\n";
        	
        	try {
        		FileWriter fileWriter = new FileWriter(mLogFile, true);
        		fileWriter.write(LogContent);
        		fileWriter.flush();
        		fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }
        
	};
}
