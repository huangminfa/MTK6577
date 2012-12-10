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

import com.mediatek.xlog.Xlog;
import com.mediatek.engineermode.R;

import android.R.integer;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class BtRelayerModeActivity extends Activity implements OnClickListener {
	private Spinner mBtRelayerModeSpinner;
	private Spinner mSerialPortSpinner;
	private Button mStartButton;
	private ArrayAdapter<String> mBaudrateAdapter;
	private ArrayAdapter<String> mPortNumberAdapter;
	private BtTest mBT;
	
	
	private String [] mBaudrateStrings = {"9600","19200","38400","57600","115200",
										"230400","460800","500000","576000","921600"};
	
	private String [] mSerialPortStrings = {"UART1","UART2","UART3","UART4"};
	
	private String TAG = "EM/BT/RelayerMode";
	
	// dialog ID and MSG ID
	private static final int START_TEST = 100;
	private static final int CHECK_START_FINISHED = 0x10;
	private static final int CHECK_START_FAIL = 0x20;	

	private boolean mStartFlag = false;
	private int mBaudrate = 9600;
	private int mPortNumber = 3;
	private Handler mHandler = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
		Xlog.v(TAG, "-->onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_relayer_mode);
        
        mBtRelayerModeSpinner = (Spinner) findViewById(R.id.spinner1);
        mSerialPortSpinner = (Spinner) findViewById(R.id.spinner2);
        
        mStartButton = (Button) findViewById(R.id.button1);
        
        if (mBtRelayerModeSpinner == null|| mStartButton == null) {
			Xlog.w(TAG, "clocwork worked...");
			
		}
        
        
        mBaudrateAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);        
        mBaudrateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        for (int i = 0; i < mBaudrateStrings.length; i++) {
			mBaudrateAdapter.add(mBaudrateStrings[i]);			
		}
        mBtRelayerModeSpinner.setAdapter(mBaudrateAdapter);
        
        
        mPortNumberAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);        
        mPortNumberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        for (int i = 0; i < mSerialPortStrings.length; i++) {
        	mPortNumberAdapter.add(mSerialPortStrings[i]);			
		}
        mSerialPortSpinner.setAdapter(mPortNumberAdapter);
        
        mStartButton.setOnClickListener(this);
        
        
        mHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				Xlog.v(TAG, "-->handleMessage"+msg.what);
				if (msg.what == CHECK_START_FINISHED) {
					mStartButton.setText("END Test");
					removeDialog(START_TEST);
				} else if (msg.what == CHECK_START_FAIL) {
					removeDialog(START_TEST);
				}

			}
        	
        };
        
    }


	public void onClick(View v) {
		Xlog.v(TAG, "-->onClick");
		// TODO Auto-generated method stub
		Xlog.v(TAG, "mStartFlag--"+mStartFlag);
		if (v.getId() == mStartButton.getId()) {
			
			Log.i(TAG, "mBtRelayerModeSpinner.getSelectedItem()--"+mBtRelayerModeSpinner.getSelectedItem());
			try {
				mBaudrate = Integer.parseInt(mBtRelayerModeSpinner.getSelectedItem().toString().trim());
				Xlog.v(TAG, "mBaudrate--"+mBaudrate);
				
			} catch (Exception e) {
				// TODO: handle exception
				Xlog.v(TAG, e.getMessage());
			}

			Log.i(TAG, "mSerialPortSpinner()--"+mSerialPortSpinner.getSelectedItem());
			Log.i(TAG, "id--"+mSerialPortSpinner.getSelectedItemId());
			mPortNumber = (int)mSerialPortSpinner.getSelectedItemId();
			Log.i(TAG, "mPortNumber--"+mPortNumber);
			if (!mStartFlag) {
				showDialog(START_TEST);
				new Thread(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						int result = -1;
						try {
							mBT = new BtTest();
							result = mBT.relayerStart(mPortNumber,mBaudrate);
							
							Xlog.v(TAG, "-->relayerStart-"+mBaudrate + " uart "+mPortNumber+ "result 0 success,-1 fail: result= "+result);
						} catch (Exception e) {
							// TODO: handle exception
							Xlog.d(TAG, "-->relayerStart-error");
						}
						Message msg = new Message();
						if (result == 0) {
							msg.what = CHECK_START_FINISHED;
							mStartFlag = true;
						}else{
							msg.what = CHECK_START_FAIL;
						}
						mHandler.sendMessage(msg);
					}
					
				}.start();								


			}else{
				mBT.relayerExit();
				mStartButton.setText("Start");
				mStartFlag = false;
			}
		}
		
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		Xlog.v(TAG, "-->onCreateDialog");
		// TODO Auto-generated method stub
		if (id == START_TEST) {

			ProgressDialog dialog = new ProgressDialog(BtRelayerModeActivity.this);
			
			if (dialog != null) {
				dialog.setTitle("Progress");
				dialog.setMessage("BT relayer is initializing, please wait...");
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
	protected void onResume() {
		Xlog.v(TAG, "-->onResume");
		// TODO Auto-generated method stub
		super.onResume();
	}


	@Override
	protected void onStop() {
		Xlog.v(TAG, "-->onStop");
		// TODO Auto-generated method stub
		super.onStop();
	}
    
    
}