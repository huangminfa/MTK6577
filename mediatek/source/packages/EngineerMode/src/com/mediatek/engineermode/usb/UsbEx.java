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

package com.mediatek.engineermode.usb;

import com.mediatek.engineermode.R;
import com.mediatek.engineermode.ShellExe;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost.OnTabChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import android.content.DialogInterface;


public class UsbEx extends Activity  implements OnClickListener {


	private Button mBtn1Start;
	private Button mBtn1Stop;
	private Button mBtn2Start;
	private Button mBtn2Stop;
	private Button mBtn3Start;
	private Button mBtn3Stop;
	private Button mBtn4Start;
	private Button mBtn4Stop;
	private Button mBtn5Start;
	private Button mBtn5Stop;
	private Button mBtn6Start;
	private Button mBtn6Stop;
	private Button mBtn7Start;
	private Button mBtn7Stop;
	private ArrayList<Button> btnList = new ArrayList<Button>();

	private String TAG = "usbif";	
	
	public static final int DLGID_OP_IN_PROCESS = 1;
	public static final int OP_IN_PROCESS = 2;
	public static final int OP_FINISH = 0;
	public static final int UPDATAT_MSG = 100;
	public static final int ERROR_MSG = 101;
	private static Handler mainHandler = null;
	ProgressDialog mDialogSearchProgress = null;
	

	private static final int TEST_SE0_NAK = 0x07;
	private static final int TEST_J = 0x08;
	private static final int TEST_K = 0x09;
	private static final int TEST_PACKET = 0x0a;
	private static final int SUSPEND_RESUME = 0x0b;
	private static final int GET_DESCRIPTOR = 0x0c;
	private static final int SET_FEATURE = 0x0d;	
	
	
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.usb_test_ex);

		mBtn1Start = (Button) findViewById(R.id.USB_EX_ITEM1_Start_ID);
		mBtn1Stop = (Button) findViewById(R.id.USB_EX_ITEM1_Stop_ID);
		mBtn2Start = (Button) findViewById(R.id.USB_EX_ITEM2_Start_ID);
		mBtn2Stop = (Button) findViewById(R.id.USB_EX_ITEM2_Stop_ID);
		mBtn3Start = (Button) findViewById(R.id.USB_EX_ITEM3_Start_ID);
		mBtn3Stop = (Button) findViewById(R.id.USB_EX_ITEM3_Stop_ID);
		mBtn4Start = (Button) findViewById(R.id.USB_EX_ITEM4_Start_ID);
		mBtn4Stop = (Button) findViewById(R.id.USB_EX_ITEM4_Stop_ID);
		mBtn5Start = (Button) findViewById(R.id.USB_EX_ITEM5_Start_ID);
		mBtn5Stop = (Button) findViewById(R.id.USB_EX_ITEM5_Stop_ID);
		mBtn6Start = (Button) findViewById(R.id.USB_EX_ITEM6_Start_ID);
		mBtn6Stop = (Button) findViewById(R.id.USB_EX_ITEM6_Stop_ID);
		mBtn7Start = (Button) findViewById(R.id.USB_EX_ITEM7_Start_ID);
		mBtn7Stop = (Button) findViewById(R.id.USB_EX_ITEM7_Stop_ID);
		
		if(mBtn1Start == null
        		|| mBtn1Stop == null
        		|| mBtn2Start == null
        		|| mBtn2Stop == null
        		|| mBtn3Start == null
        		|| mBtn3Stop == null
        		|| mBtn4Start == null
        		|| mBtn4Stop == null
        		|| mBtn5Start == null
        		|| mBtn5Stop == null
        		|| mBtn6Start == null
        		|| mBtn6Stop == null
        		|| mBtn7Start == null
        		|| mBtn7Stop == null
        		)
        {
        	Log.e(TAG, "clocwork worked...");	
    		//not return and let exception happened.
        }
		btnList.add(mBtn1Start);
		btnList.add(mBtn1Stop);
		btnList.add(mBtn2Start);
		btnList.add(mBtn2Stop);
		btnList.add(mBtn3Start);
		btnList.add(mBtn3Stop);
		btnList.add(mBtn4Start);
		btnList.add(mBtn4Stop);
		btnList.add(mBtn5Start);
		btnList.add(mBtn5Stop);
		btnList.add(mBtn6Start);
		btnList.add(mBtn6Stop);
		btnList.add(mBtn7Start);
		btnList.add(mBtn7Stop);

		for(Button btn : btnList)
		{
			btn.setOnClickListener(this);
		}	
		
		mainHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case OP_IN_PROCESS: {					
					if (null == mDialogSearchProgress) {
						mDialogSearchProgress = new ProgressDialog(
								UsbEx.this);
						if (null != mDialogSearchProgress) {
							mDialogSearchProgress
									.setProgressStyle(ProgressDialog.STYLE_SPINNER);
							String text = "Stopping, please wait ...";
							mDialogSearchProgress.setMessage(text);
							mDialogSearchProgress.setTitle("USB Ex Test");
							mDialogSearchProgress.setCancelable(false);
							mDialogSearchProgress.show();
						} else {
							Log.e(TAG, "new mDialogSearchProgress failed");
						}
					}
				}
					break;
				case OP_FINISH:					
					if (null != mDialogSearchProgress) {
						mDialogSearchProgress.dismiss();
						mDialogSearchProgress = null;
					}
					break;
				case UPDATAT_MSG:
					if(mMsg >= UsbIF.MSG_LEN ||mMsg < 0)
					{
						ShowDialog("Message", "Ghost Msg = "+mMsg);
					}
					else
					{
						ShowDialog("Message", UsbIF.MSG[mMsg]);
					}
					break;
				case ERROR_MSG:
				  ShowDialog("Error", "Execute Command Error.");
				    break;
				default:
					break;
				}
			}
		};
		
		if(!UsbIF.NativeInit()) {
		    Toast.makeText(this, "Phone not support this module", Toast.LENGTH_SHORT).show();
		    finish();
		}
	}
	
	private void updateAllBtn(boolean enable)
	{
		for(Button btn : btnList)
		{
			btn.setEnabled(enable);
		}
	}

	private void makeOneBtnEnable(Button selBtn) {
		for (Button btn : btnList) {
			if (btn == selBtn) {
				btn.setEnabled(true);
			} else {
				btn.setEnabled(false);
			}		
		}
	}


	@Override
	public void onPause() {		
		super.onPause();		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub		
		super.onResume();			
				
	}

	@Override
	protected void onStop() {
		super.onStop();		
	}	
	@Override
	protected void onDestroy() {
		UsbIF.NativeDeInit();
		super.onDestroy();				
	}
	
	private void ShowDialog(String title, String info) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(info);
		builder.setPositiveButton("OK", null);
		builder.create().show();
	}
	private TaskRunnable task = null;
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		//TaskRunnable task = null;
		boolean isSTART = false;
		Button stopBtn = null;
		int command = 0;
		if (arg0.getId() == mBtn1Start.getId()) {			
			command = TEST_SE0_NAK;
			isSTART = true;
			stopBtn = mBtn1Stop;
		} else if (arg0.getId() == mBtn2Start.getId()) {
			command = TEST_J;
			isSTART = true;
			stopBtn = mBtn2Stop;
		}else if (arg0.getId() == mBtn3Start.getId()) {
			command = TEST_K;
			isSTART = true;
			stopBtn = mBtn3Stop;
		}else if (arg0.getId() == mBtn4Start.getId()) {
			command = TEST_PACKET;
			isSTART = true;
			stopBtn = mBtn4Stop;
		}else if (arg0.getId() == mBtn5Start.getId()) {
			command = SUSPEND_RESUME;
			isSTART = true;
			stopBtn = mBtn5Stop;
		}else if (arg0.getId() == mBtn6Start.getId()) {
			command = GET_DESCRIPTOR;
			isSTART = true;
			stopBtn = mBtn6Stop;
		}else if (arg0.getId() == mBtn7Start.getId()) {
			command = SET_FEATURE;
			isSTART = true;
			stopBtn = mBtn7Stop;
		}
		
		else if (arg0.getId() == mBtn1Stop.getId()) {				
			command = TEST_SE0_NAK;
			isSTART = false;
		} else if (arg0.getId() == mBtn2Stop.getId()) {
			command = TEST_J;
			isSTART = false;
		}else if (arg0.getId() == mBtn3Stop.getId()) {
			command = TEST_K;
			isSTART = false;
		}else if (arg0.getId() == mBtn4Stop.getId()) {
			command = TEST_PACKET;
			isSTART = false;
		}else if (arg0.getId() == mBtn5Stop.getId()) {
			command = SUSPEND_RESUME;
			isSTART = false;
		}else if (arg0.getId() == mBtn6Stop.getId()) {
			command = GET_DESCRIPTOR;
			isSTART = false;
		}else if (arg0.getId() == mBtn7Stop.getId()) {
			command = SET_FEATURE;
			isSTART = false;
		}
		else{
			ShowDialog("Error", "Unknown button is pressed.");
			return;
		}
		
		if(isSTART)
		{
			if(task != null)
			{
				return;
			}
			
			TaskThread thr = new TaskThread();
			thr.setParam(command);
			thr.start();
			
			makeOneBtnEnable(stopBtn);
			task = new TaskRunnable();
			task.start();
		}
		else
		{
			if(task != null)
			{		
				task.mRun = false;					
				if(!UsbIF.NativeStopTest(command))
				{
					ShowDialog("Error", "NativeStopTest Fail.");
					UsbIF.NativeCleanMsg();
				}				
				task = null;	
				updateAllBtn(true);
			}
		}
	}
	private int mMsg = 0;
	private class TaskRunnable extends Thread {
		public boolean mRun = false;
		public void run() {
			//mainHandler.sendEmptyMessage(OP_IN_PROCESS);			
			//doSendCommandAction();
			mRun = true;
			while(mRun)
			{
				if(0 != (mMsg = UsbIF.NativeGetMsg()))
				{
					mainHandler.sendEmptyMessage(UPDATAT_MSG);
				}
				
				try {
					sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}			
			
			//mainHandler.sendEmptyMessage(OP_FINISH);
		}
	}
	
	private class TaskThread extends Thread {
		public void setParam(int command)
		{
			this.command = command;
		}
		private int command = 0;
		public void run() {
			if(!UsbIF.NativeStartTest(command))
			{
				mainHandler.sendEmptyMessage(ERROR_MSG);						
			}	
			Log.d(TAG, "Task finish");		
		}
	}

}
