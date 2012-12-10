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
import android.app.AlertDialog;
import android.content.DialogInterface;


public class GPIO extends Activity  implements OnClickListener {


	private Button mBtnDirIn;
	private Button mBtnDirOut;
	private Button mBtnDataHigh;
	private Button mBtnDataLow;
	
	private EditText mEdit;
	private TextView mMaxGpioNum;

	private String TAG = "EM-GPIO";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.gpio);

		mBtnDirIn = (Button) findViewById(R.id.GPIO_Direction_In);
		mBtnDirOut = (Button) findViewById(R.id.GPIO_Direction_Out);
		mBtnDataHigh = (Button) findViewById(R.id.GPIO_Data_High);
		mBtnDataLow = (Button) findViewById(R.id.GPIO_Data_Low);
		
		mEdit = (EditText) findViewById(R.id.GPIO_Edit_Value);
		mMaxGpioNum = (TextView) findViewById(R.id.GPIO_Setnomax_Text);
		
		if(mBtnDirIn == null
        		|| mBtnDirOut == null
        		|| mBtnDataHigh == null
        		|| mBtnDataLow == null
        		|| mEdit == null
        		|| mMaxGpioNum == null
        		)
        {
			Xlog.w(TAG, "clocwork worked...");	
    		//not return and let exception happened.
        }

		mBtnDirIn.setOnClickListener(this);
		mBtnDirOut.setOnClickListener(this);
		mBtnDataHigh.setOnClickListener(this);
		mBtnDataLow.setOnClickListener(this);	
		
	
		//mMaxGpioNum.setText("max");	
	}
	


	@Override
	public void onPause() {
		Xlog.v(TAG, "-->onPause");	
		EMgpio.GPIOUnInit();
		super.onPause();		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Xlog.v(TAG, "-->onResume");	
		super.onResume();		
		boolean ret = EMgpio.GPIOInit();
		if(!ret)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Error");
			builder.setMessage("Driver Init error!");
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
							public void onClick(DialogInterface dialog, int id) { 
								GPIO.this.finish(); 
							} 
						});
			builder.create().show();	
		}
		else
		{
			mMaxGpioNum.setText("Max Num:"+String.valueOf(EMgpio.GetGpioMaxNumber()-1));
		}
				
	}

	@Override
	protected void onStop() {
		Xlog.v(TAG, "-->onStop");			
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
		String editString = mEdit.getText().toString();
			if (null == editString|| editString.equals("")|| editString.length()>4) {
				Toast.makeText(this, "Please input the value.",
						Toast.LENGTH_LONG).show();				
				return;
			}
			int inputValue = Integer.valueOf(mEdit.getText().toString());
			if (inputValue > EMgpio.GetGpioMaxNumber()-1 || inputValue < 0) {
				Toast.makeText(this, "ERR: Value is too small or too big.",
						Toast.LENGTH_LONG).show();				
				return;
			}

	
		if (arg0.getId() == mBtnDirIn.getId()) {	
				
			
				boolean ret = EMgpio.SetGpioInput(inputValue);
				if (ret) {
					//ShowDialog("Set Success", "Set Direction Input succeeded.");
					Toast.makeText(this, "Set Direction Input succeeded.",
						Toast.LENGTH_LONG).show();
					
				} else
				{
					ShowDialog("Set Failed",  "Set Direction Input Failed.");
					
				}				

			
			
		} else if (arg0.getId() == mBtnDirOut.getId()) {
					boolean ret = EMgpio.SetGpioOutput(inputValue);
				if (ret) {
					//ShowDialog("Set Success", "Set Direction Output succeeded.");
					Toast.makeText(this, "Set Direction Output succeeded.",
						Toast.LENGTH_LONG).show();
					
				} else
				{
					ShowDialog("Set Failed",  "Set Direction Output Failed.");
					
				}
			
		} else if (arg0.getId() == mBtnDataHigh.getId()) {
					boolean ret = EMgpio.SetGpioDataHigh(inputValue);
				if (ret) {
					//ShowDialog("Set Success", "Set Data High succeeded.");
					Toast.makeText(this, "Set Data High succeeded.",
						Toast.LENGTH_LONG).show();
					
				} else
				{
					ShowDialog("Set Failed",  "Set Data High Failed.");
					
				}						
			
		} else if (arg0.getId() == mBtnDataLow.getId()) {
				boolean ret = EMgpio.SetGpioDataLow(inputValue);
				if (ret) {
					//ShowDialog("Set Success", "Set Data Low succeeded.");
					Toast.makeText(this, "Set Data Low succeeded.",
						Toast.LENGTH_LONG).show();
					
				} else
				{
					ShowDialog("Set Failed",  "Set Data Low Failed.");
					
				}	
			
		} else {
		}
		
		
	}
	

}
