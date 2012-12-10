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

package com.mediatek.engineermode.io;

import com.mediatek.engineermode.io.EMgpio;
import com.mediatek.engineermode.R;
import com.mediatek.engineermode.ShellExe;
import com.mediatek.xlog.Xlog;

import java.io.File;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TabHost.OnTabChangeListener;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;


public class MSDC extends Activity  implements OnClickListener {


	private Spinner mHostSpinner;	
	private int mHostIndex = 0;
	private String mHostType[] = { "Host Number 0", 
			"Host Number 1", 
			"Host Number 2", 
			"Host Number 3"};
	private ArrayAdapter<String> HostAdatper;	
	
	private Spinner mCurrentDataSpinner;	
	private int mCurrentDataIndex = 0;
	private String mCurrentLevel[] = { "4 mA", "8 mA", "12 mA", "16 mA"};
	private ArrayAdapter<String> CurrentAdatper;
	
	private Spinner mCurrentCmdSpinner;	
	private int mCurrentCmdIndex = 0;
	
	private Button mBtnGet;
	private Button mBtnSet;	

	private String TAG = "MSDC_IOCTL";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.msdc);

		mBtnGet = (Button) findViewById(R.id.MSDC_Get);
		mBtnSet = (Button) findViewById(R.id.MSDC_Set);
		
		mHostSpinner = (Spinner) findViewById(R.id.MSDC_Host_spininer);
		mCurrentDataSpinner = (Spinner) findViewById(R.id.MSDC_Current_data_spininer);
		mCurrentCmdSpinner = (Spinner) findViewById(R.id.MSDC_Current_cmd_spininer);
				
		if(mBtnGet == null
        		|| mBtnSet == null
        		|| mHostSpinner == null
        		|| mCurrentDataSpinner == null
        		|| mCurrentCmdSpinner == null
        		)
        {
        	Xlog.w(TAG, "clocwork worked...");	
    		//not return and let exception happened.
        }
		
		mBtnGet.setOnClickListener(this);
		mBtnSet.setOnClickListener(this);		
		
		HostAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		HostAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < mHostType.length; i++) {
			HostAdatper.add(mHostType[i]);
		}
		mHostSpinner.setAdapter(HostAdatper);
		mHostSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				mHostIndex = arg2;
				
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
		
		CurrentAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		CurrentAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < mCurrentLevel.length; i++) {
			CurrentAdatper.add(mCurrentLevel[i]);
		}
		mCurrentDataSpinner.setAdapter(CurrentAdatper);
		mCurrentDataSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				mCurrentDataIndex = arg2;				
				
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
		
		mCurrentCmdSpinner.setAdapter(CurrentAdatper);
		mCurrentCmdSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				mCurrentCmdIndex = arg2;				
				
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
		
		
		mHostSpinner.setSelection(0);
	}
	


	@Override
	public void onPause() {
		EMgpio.GPIOUnInit();
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
	
	private void ShowDialog(String title, String info) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(info);
		builder.setPositiveButton("OK", null);
		builder.create().show();
	}
	public void onClick(View arg0) {
		// TODO Auto-generated method stub		
		if (arg0.getId() == mBtnGet.getId()) {			
			Xlog.i(TAG, "SD_IOCTL: click GetCurrent");	
			int idx = EMgpio.GetCurrent(mHostIndex);
			if(idx != -1)
			{
				int dataIdx = idx & 0xFFFF;
				int cmdIdx = (idx>>16) & 0xFFFF;
				//ShowDialog("Set Success", "Set Direction Input succeeded.");
				Toast.makeText(this, "Get current successed! dat_drving[" + mCurrentLevel[dataIdx]
					                    + "], cmd_drving[" + mCurrentLevel[cmdIdx] + "]",Toast.LENGTH_LONG).show();		
				mCurrentDataSpinner.setSelection(dataIdx);
				mCurrentCmdSpinner.setSelection(cmdIdx);
				Xlog.i(TAG, "SD_IOCTL: Get current successed! dat_drving["+ dataIdx + "]:"  + mCurrentLevel[dataIdx]
				                    + ", cmd_drving[" + cmdIdx + "]:"+ mCurrentLevel[cmdIdx]);	
			} 
			else
			{
				ShowDialog("Failed",  "Get current Failed.");					
			}
		
		} else if (arg0.getId() == mBtnSet.getId()) {
			Xlog.i(TAG, "SD_IOCTL: click SetCurrent");	
			boolean ret = EMgpio.SetCurrent(mHostIndex, mCurrentDataIndex, mCurrentCmdIndex);
			if(ret)
			{
				//ShowDialog("Set Success", "Set Direction Input succeeded.");
				Toast.makeText(this, "Set current successed! dat_drving[" + mCurrentLevel[mCurrentDataIndex]
				                    + "], cmd_drving[" + mCurrentLevel[mCurrentCmdIndex] + "]",Toast.LENGTH_LONG).show();					
				Xlog.i(TAG, "SD_IOCTL: Set current successed! dat_drving[" + mCurrentDataIndex + "]:" + mCurrentLevel[mCurrentDataIndex]
				                    + ", cmd_drving[" + mCurrentCmdIndex + "]:" + mCurrentLevel[mCurrentCmdIndex]);				
			} 
			else
			{
				ShowDialog("Failed",  "Set current Failed.");					
			}	
			
		} else {
		}
		
		
	}
	

}
