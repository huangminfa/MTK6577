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

import android.app.Activity;
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

public class MSDC_HopSet extends Activity implements OnClickListener{
	private String mHostType[] = { "Host Number 0", "Host Number 1", "Host Number 2", "Host Number 3"};
	private String mHoppingBit[]={"None", "2.26MHZ","4.52MHZ","9.04MHZ"};
	private String mHoppingTime[]={ "30.5us","61.0us","122.1us","244.1us","488.3us"};
	private ArrayAdapter<String> HostAdatper;
	private  ArrayAdapter<String>  mHopBitAdatper;
	private ArrayAdapter<String>   mHopTimeAdatper;
	private Spinner mHostSpinner;
	private int mHostIndex = 0;
	private Spinner mHoppingBitSpinner ;
	private int mHoppingBitIndex = 0;
	private Spinner mHoppingTimeSpinner;
	private int mHoppingTimeIndex = 0;
	private Button mBtnGet;
	private Button mBtnSet;
	
	private String TAG = "MSDC_HOPSET_IOCTL";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.msdc_hopset);
		mBtnGet =(Button)findViewById(R.id.MSDC_HopSet_Get);
		mBtnSet =(Button)findViewById(R.id.MSDC_HopSet_Set);
		mHostSpinner=(Spinner)findViewById(R.id.MSDC_HopSet_HOST_sppiner);
		mHoppingBitSpinner=(Spinner) findViewById(R.id.MSDC_hopping_bit_spinner);
		mHoppingTimeSpinner=(Spinner) findViewById(R.id.MSDC_hopping_time_spinner);
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
		
		mHopBitAdatper = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		mHopBitAdatper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for(int i=0;i<mHoppingBit.length;i++){
			mHopBitAdatper.add(mHoppingBit[i]);
		}
		mHoppingBitSpinner.setAdapter(mHopBitAdatper);
		
		mHopTimeAdatper = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		mHopTimeAdatper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for(int i=0;i<mHoppingTime.length;i++){
			mHopTimeAdatper.add(mHoppingTime[i]);
		}
		mHoppingBitSpinner.setAdapter(mHopBitAdatper);
		mHoppingBitSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				mHoppingBitIndex = arg2;
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}	
		});
		mHoppingTimeSpinner.setAdapter(mHopTimeAdatper);
		mHoppingTimeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				mHoppingTimeIndex = arg2;
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mHostSpinner.setSelection(0);
	}
	
	private void ShowDialog(String title , String info){
		  
		  AlertDialog.Builder builder = new AlertDialog.Builder(this);
		  builder.setTitle(title);
		  builder.setMessage(info);
		  builder.setPositiveButton("OK", null);
		  builder.create().show();
	  }

	public void onClick(View arg0) {
		if(arg0.getId() == mBtnGet.getId()){
			Xlog.i(TAG, "SD_IOCTL: click GetCurrent");
			int idx = EMgpio.NewGetCurrent(mHostIndex,1);			
			if(idx !=-1)
			{
             int mHopbitIdx = (idx & 0xF000000 )>>24;
             int mHoptimeIdx =(idx & 0xF0000000 )>>28;
			  mHoppingBitSpinner.setSelection(mHopbitIdx);
			  mHoppingTimeSpinner.setSelection(mHoptimeIdx);
			}else{
				ShowDialog("Failed",  "Get current Failed.");	
			}
	     
		}else if(arg0.getId() == mBtnSet.getId()){
			Xlog.i(TAG, "SD_IOCTL: click SetCurrent");	
			boolean ret = EMgpio.NewSetCurrent(mHostIndex, 0, 0, 0, 0, 0, 0,mHoppingBitIndex,mHoppingTimeIndex,1);
			if(ret){
				ShowDialog("Set Success", "Set Direction Input succeeded.");
				}else {
					ShowDialog("Failed",  "Set current Failed.");	
			}
		}
		
	}
	
	

}
