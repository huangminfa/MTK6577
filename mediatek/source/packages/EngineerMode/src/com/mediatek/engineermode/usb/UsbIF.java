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
import com.mediatek.xlog.Xlog;

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


public class UsbIF extends Activity  implements OnClickListener {


	private Button mBtnEnVbusStart;
	private Button mBtnEnVbusStop;
	private Button mBtnDeVbusStart;
	private Button mBtnDeVbusStop;
	private Button mBtnEnSrpStart;
	private Button mBtnEnSrpStop;
	private Button mBtnDeSrpStart;
	private Button mBtnDeSrpStop;
	private Button mBtnAUutStart;
	private Button mBtnAUutStop;
	private Button mBtnBUutStart;
	private Button mBtnBUutStop;
	private Button mBtnBUutTD5_9;
	private ArrayList<Button> btnList = new ArrayList<Button>();

	private String TAG = "usbif";	
	
	public static final int DLGID_OP_IN_PROCESS = 1;
	public static final int OP_IN_PROCESS = 2;
	public static final int OP_FINISH = 0;
	public static final int UPDATAT_MSG = 100;
	public static final int ERROR_MSG = 101;
	private static Handler mainHandler = null;
	ProgressDialog mDialogSearchProgress = null;
	
	public static native boolean NativeInit();
	public static native void NativeDeInit();
	public static native boolean NativeCleanMsg();
	public static native int NativeGetMsg();
	public static native boolean NativeStartTest(int n);
	public static native boolean NativeStopTest(int n);
	
	private static final int ENABLE_VBUS = 0x01;
	private static final int ENABLE_SRP = 0x02;
	private static final int DETECT_SRP = 0x03;
	private static final int DETECT_VBUS = 0x04;
	private static final int A_UUT = 0x05;
	private static final int B_UUT = 0x06;
	private static final int TD_5_9 = 0X0e;
	
	public static final String[] MSG = {"Driver return 0.", "Attached device not support."
        , "Device not connected/responding.", "Unsupported HUB topology."};
	public static final int MSG_LEN = 4;
	
	static {
		System.loadLibrary("em_jni");
		
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.usb_test);

		mBtnEnVbusStart = (Button) findViewById(R.id.USB_IF_Elec_EnVBUS_Start_ID);
		mBtnEnVbusStop = (Button) findViewById(R.id.USB_IF_Elec_EnVBUS_Stop_ID);
		mBtnDeVbusStart = (Button) findViewById(R.id.USB_IF_Elec_DeVBUS_Start_ID);
		mBtnDeVbusStop = (Button) findViewById(R.id.USB_IF_Elec_DeVBUS_Stop_ID);
		mBtnEnSrpStart = (Button) findViewById(R.id.USB_IF_Elec_EnSRP_Start_ID);
		mBtnEnSrpStop = (Button) findViewById(R.id.USB_IF_Elec_EnSRP_Stop_ID);
		mBtnDeSrpStart = (Button) findViewById(R.id.USB_IF_Elec_DeSRP_Start_ID);
		mBtnDeSrpStop = (Button) findViewById(R.id.USB_IF_Elec_DeSRP_Stop_ID);
		mBtnAUutStart = (Button) findViewById(R.id.USB_IF_Proto_AUUT_Start_ID);
		mBtnAUutStop = (Button) findViewById(R.id.USB_IF_Proto_AUUT_Stop_ID);
		mBtnBUutStart = (Button) findViewById(R.id.USB_IF_Proto_BUUT_Start_ID);
		mBtnBUutStop = (Button) findViewById(R.id.USB_IF_Proto_BUUT_Stop_ID);	
		mBtnBUutTD5_9 = (Button) findViewById(R.id.USB_IF_Proto_BUUT_TD5_9_ID);	
			
		if(mBtnEnVbusStart == null
        		|| mBtnEnVbusStop == null
        		|| mBtnDeVbusStart == null
        		|| mBtnDeVbusStop == null
        		|| mBtnEnSrpStart == null
        		|| mBtnEnSrpStop == null
        		||mBtnDeSrpStart == null
        		|| mBtnDeSrpStop == null
        		|| mBtnAUutStart == null
        		|| mBtnAUutStop == null
        		|| mBtnBUutStart == null
        		|| mBtnBUutStop == null
        		|| mBtnBUutTD5_9 == null
        		)
        {
//        	Log.e(TAG, "clocwork worked...");	
        	Xlog.e(TAG, "clocwork worked...");
    		//not return and let exception happened.
        }
		btnList.add(mBtnEnVbusStart);
		btnList.add(mBtnEnVbusStop);
		btnList.add(mBtnDeVbusStart);
		btnList.add(mBtnDeVbusStop);		
		btnList.add(mBtnEnSrpStart);
		btnList.add(mBtnEnSrpStop);		
		btnList.add(mBtnDeSrpStart);
		btnList.add(mBtnDeSrpStop);
		btnList.add(mBtnAUutStart);		
		btnList.add(mBtnAUutStop);
		btnList.add(mBtnBUutStart);
		btnList.add(mBtnBUutStop);
		btnList.add(mBtnBUutTD5_9);

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
								UsbIF.this);
						if (null != mDialogSearchProgress) {
							mDialogSearchProgress
									.setProgressStyle(ProgressDialog.STYLE_SPINNER);
							String text = "Stopping, please wait ...";
							mDialogSearchProgress.setMessage(text);
							mDialogSearchProgress.setTitle("USB IF Test");
							mDialogSearchProgress.setCancelable(false);
							mDialogSearchProgress.show();
						} else {
							Xlog.v(TAG, "new mDialogSearchProgress failed");
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
					if(mMsg >= MSG_LEN ||mMsg < 0)
					{
						ShowDialog("Message", "Ghost Msg = "+mMsg);
					}
					else
					{
						ShowDialog("Message", MSG[mMsg]);
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
		
		if(!NativeInit()) {
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
		Xlog.v(TAG, "-->onPause");
		super.onPause();		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub		
		Xlog.v(TAG, "-->onResume");
		super.onResume();			
				
	}

	@Override
	protected void onStop() {
		Xlog.v(TAG, "-->onStop");
		super.onStop();		
	}	
	@Override
	protected void onDestroy() {
		Xlog.v(TAG, "-->onDestroy");
		NativeDeInit();
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
		if (arg0.getId() == mBtnEnVbusStart.getId()) {			
			command = ENABLE_VBUS;
			isSTART = true;
			stopBtn = mBtnEnVbusStop;
		} else if (arg0.getId() == mBtnDeVbusStart.getId()) {
			command = DETECT_VBUS;
			isSTART = true;
			stopBtn = mBtnDeVbusStop;
		}else if (arg0.getId() == mBtnEnSrpStart.getId()) {
			command = ENABLE_SRP;
			isSTART = true;
			stopBtn = mBtnEnSrpStop;
		}else if (arg0.getId() == mBtnDeSrpStart.getId()) {
			command = DETECT_SRP;
			isSTART = true;
			stopBtn = mBtnDeSrpStop;
		}else if (arg0.getId() == mBtnAUutStart.getId()) {
			command = A_UUT;
			isSTART = true;
			stopBtn = mBtnAUutStop;
		}else if (arg0.getId() == mBtnBUutStart.getId()) {
			command = B_UUT;
			isSTART = true;
			stopBtn = mBtnBUutStop;
		}else if (arg0.getId() == mBtnBUutTD5_9.getId()) {
			command = TD_5_9;
			isSTART = true;
			stopBtn = mBtnBUutStart;
		}
		
		else if (arg0.getId() == mBtnEnVbusStop.getId()) {				
			command = ENABLE_VBUS;
			isSTART = false;
		} else if (arg0.getId() == mBtnDeVbusStop.getId()) {
			command = DETECT_VBUS;
			isSTART = false;
		}else if (arg0.getId() == mBtnEnSrpStop.getId()) {
			command = ENABLE_SRP;
			isSTART = false;
		}else if (arg0.getId() == mBtnDeSrpStop.getId()) {
			command = DETECT_SRP;
			isSTART = false;
		}else if (arg0.getId() == mBtnAUutStop.getId()) {
			command = A_UUT;
			isSTART = false;
		}else if (arg0.getId() == mBtnBUutStop.getId()) {
			command = B_UUT;
			isSTART = false;
		}
		else{
			ShowDialog("Error", "Unknown button is pressed.");
			return;
		}
		Xlog.v(TAG, "isSTART--"+ isSTART);
		Xlog.v(TAG, "command--"+ command);
		if(isSTART)
		{
			if(task != null)
			{
				return;
			}
			
			TaskThread thr = new TaskThread();
			
			thr.setParam(command);
			thr.start();
			if(command !=TD_5_9){
				makeOneBtnEnable(stopBtn);
				task = new TaskRunnable();
				task.start();
			}else{
				task = null;
			}

		}
		else
		{
			if(task != null)
			{		
				task.mRun = false;					
				if(!NativeStopTest(command))
				{
					ShowDialog("Error", "NativeStopTest Fail.");
					NativeCleanMsg();
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
				if(0 != (mMsg = NativeGetMsg()))
				{
					mainHandler.sendEmptyMessage(UPDATAT_MSG);
				}
				
				try {
					sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					Xlog.e(TAG,"sleep 300 error");		
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
			Xlog.v(TAG, "command--"+ command);
			if(!NativeStartTest(command))
			{
				mainHandler.sendEmptyMessage(ERROR_MSG);						
			}	
			Xlog.v(TAG, "Task finish");		
		}
	}

}
