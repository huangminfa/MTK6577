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
import com.mediatek.engineermode.R;
import com.mediatek.xlog.Xlog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class New_MSDC extends Activity implements OnClickListener{
	private Spinner mHostSpinner;	
	private int mHostIndex = 0;
	private String mHostType[] = { "Host Number 0", 
			"Host Number 1", 
			"Host Number 2", 
			"Host Number 3"};
	
	private ArrayAdapter<String> HostAdatper;	
	
	private Spinner mClkPuSpinner;
	private int mClkPuIndex = 0;
	private String mCommanType[]={"0", "1","2","3","4","5","6","7"};
	private ArrayAdapter<String> CommanAdatper;
	
	private Spinner mClkPdSpinner;
	private int mClkPdIndex = 0;
	
	private Spinner mCmdPuSpinner;
	private int mCmdPuIndex = 0;
	
	private Spinner mCmdPdSpinner;
	private int mCmdPdIndex = 0;
	
	private Spinner mDataPuSpinner;
	private int mDataPuIndex = 0;
	
	private Spinner mDataPdSpinner;
	private int mDataPdndex = 0;
	private Button mBtnGet;
	private Button mBtnSet;
	
	private String TAG = "NEW_MSDC_IOCTL";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_msdc);
		
		mBtnGet =(Button)findViewById(R.id.NEW_MSDC_Get);
		mBtnSet =(Button)findViewById(R.id.NEW_MSDC_Set);
		
		mHostSpinner = (Spinner) findViewById(R.id.NEW_MSDC_HOST_sppiner);
		
		mClkPuSpinner = (Spinner) findViewById(R.id.MSDC_Clk_pu_spinner);
		mClkPdSpinner = (Spinner) findViewById(R.id.MSDC_clk_pd_spinner);
		
		mCmdPuSpinner=(Spinner) findViewById(R.id.MSDC_cmd_pu_spinner);
		mCmdPdSpinner=(Spinner) findViewById(R.id.MSDC_cmd_pd_spinner);
		
		mDataPuSpinner=(Spinner) findViewById(R.id.MSDC_data_pu_spinner);
		mDataPdSpinner=(Spinner) findViewById(R.id.MSDC_data_pd_spinner);
		
		if(mBtnGet == null
				||mBtnSet == null
				||mHostSpinner == null
				||mClkPuSpinner == null
				||mClkPdSpinner == null
				||mCmdPuSpinner == null
				||mCmdPdSpinner == null
				||mDataPuSpinner == null
				||mDataPdSpinner == null){
			android.util.Log.e(TAG, "clocwork worked...");
		}
		
		mBtnGet.setOnClickListener(this);
		mBtnSet.setOnClickListener(this);
		
		HostAdatper = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		HostAdatper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for(int i=0;i<mHostType.length;i++){
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
		
		CommanAdatper = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		CommanAdatper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for(int i=0;i<mCommanType.length;i++){
		    CommanAdatper.add(mCommanType[i]);	
		}
		mClkPuSpinner.setAdapter(CommanAdatper);
		mClkPuSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				mClkPuIndex = arg2;
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
		}
	});
		
	mClkPdSpinner.setAdapter(CommanAdatper);
	mClkPdSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			mClkPdIndex = arg2;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
	});
	
	mCmdPuSpinner.setAdapter(CommanAdatper);
	mCmdPuSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			mCmdPuIndex = arg2;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	});
	
	mCmdPdSpinner.setAdapter(CommanAdatper);
	mCmdPdSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			mCmdPdIndex=arg2;
			
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	});
	
	mDataPuSpinner.setAdapter(CommanAdatper);
	mDataPuSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			mDataPuIndex = arg2;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	});
	
	mDataPdSpinner.setAdapter(CommanAdatper);
	mDataPdSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			mDataPdndex = arg2;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	});
	
	mHostSpinner.setSelection(0);
		
   }
	
	

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
   
	


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
   


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

  private void ShowDialog(String title , String info){
	  
	  AlertDialog.Builder builder = new AlertDialog.Builder(this);
	  builder.setCancelable(false);
	  builder.setTitle(title);
	  builder.setMessage(info);
	  builder.setPositiveButton("OK", null);
	  builder.create().show();
  }
  
  
  

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if(arg0.getId() == mBtnGet.getId()){
			Xlog.i(TAG, "SD_IOCTL: click GetCurrent");
			int idx = EMgpio.NewGetCurrent(mHostIndex,0);			
			if(idx !=-1)
			{

			 int clkpuIdx = idx & 0x0F;
			 int clkpdIdx = (idx & 0xF0)>>4;
			 int cmdpuIdx = (idx & 0xF00)>>8;
			 int cmdpdIdx =	(idx & 0xF000)>>12;
			 int datapuIdx = (idx & 0xF0000)>>16;
			 int datapdIdx = (idx & 0xF00000)>>20;
			  mClkPuSpinner.setSelection(clkpuIdx);
			  mClkPdSpinner.setSelection(clkpdIdx);
			  mCmdPuSpinner.setSelection(cmdpuIdx);
			  mCmdPdSpinner.setSelection(cmdpdIdx);
			  mDataPuSpinner.setSelection(datapuIdx);
			  mDataPdSpinner.setSelection(datapdIdx);
			}else{
				ShowDialog("Failed",  "Get current Failed.");	
			}
	     
		}else if(arg0.getId() == mBtnSet.getId()){
			Xlog.i(TAG, "SD_IOCTL: click SetCurrent");	
			boolean ret = EMgpio.NewSetCurrent(mHostIndex, mClkPuIndex, mClkPdIndex, mCmdPuIndex, mCmdPdIndex, mDataPuIndex, mDataPdndex,0,0,0);
			if(ret){
				ShowDialog("Set Success", "Set Direction Input succeeded.");
				}else {
					ShowDialog("Failed",  "Set current Failed.");	
			}
		}

	}
	
	
	
	
	
	
}
