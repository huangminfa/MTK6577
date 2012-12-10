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

import com.mediatek.engineermode.ShellExe;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import android.app.AlertDialog;

import java.io.File;
import java.util.ArrayList;
import java.io.IOException;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

public class EINT extends Activity implements OnClickListener {

	private TextView mDispSensitivity;
	private TextView mDispPolarity;
	private Button mBtnQuery;
	private EditText mEdit;
	private String TAG = "EM-IO";
	private String rootDir = "/sys/bus/platform/drivers/eint/";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eint);

		mDispSensitivity = (TextView) findViewById(R.id.EINT_sensitivity);
		mDispPolarity = (TextView) findViewById(R.id.EINT_polarity);		

		mBtnQuery = (Button) findViewById(R.id.EINT_query);
		mEdit = (EditText) findViewById(R.id.EINT_edit);

		if(mDispSensitivity == null
        		|| mDispPolarity == null
        		|| mBtnQuery == null
        		|| mEdit == null
        		)
        {
			Xlog.w(TAG, "clocwork worked...");	
    		//not return and let exception happened.
        }
		
		mBtnQuery.setOnClickListener(this);	

	}

	@Override
	public void onResume() {
		Xlog.v(TAG, "-->onResume");	
		super.onResume();
	}

	@Override
	public void onPause() {
		Xlog.v(TAG, "-->onPause");	
		super.onPause();

	}
	private void ShowDialog(String title, String info) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(info);
		builder.setPositiveButton("OK", null);
		builder.create().show();
	}


	public void onClick(View arg0) {
		Xlog.v(TAG, "-->onClick");	
		// TODO Auto-generated method stub
		try {
			if (arg0.getId() == mBtnQuery.getId()) {
				String editString = mEdit.getText().toString();
				if (null == editString|| editString.equals("")|| editString.length()>4) {
					Toast.makeText(this, "Please input the NO..",
							Toast.LENGTH_LONG).show();					
					return;
				}
				String[] cmd = { "/system/bin/sh", "-c",
					"echo " + editString + " > "+ rootDir+ "current_eint" }; 
				int ret = ShellExe.execCommand(cmd);
				if (0 != ret) {
					mDispSensitivity.setText("Set EINT NO. Error.");
					mDispPolarity.setText("Set EINT NO. Error.");
					return;
				} 
				
				cmd[2] = "cat  "+ rootDir+ "current_eint";
				ret = ShellExe.execCommand(cmd);
				if (0 != ret) {
					mDispSensitivity.setText("Query Error.");
					mDispPolarity.setText("Query Error.");
					return;
				}
				
				if(!ShellExe.getOutput().equalsIgnoreCase(editString))
				{
					mDispSensitivity.setText("No Such EINT NO..");
					mDispPolarity.setText("No Such EINT NO..");
					return;
				}
				
				cmd[2] = "cat " + rootDir+ "current_eint_sens";
				ret = ShellExe.execCommand(cmd);
				if (0 != ret) {
					mDispSensitivity.setText("Get Sensitivity Error.");
				}
				else
				{
					if(ShellExe.getOutput().equalsIgnoreCase("0"))
					{
						mDispSensitivity.setText("edge");
					}
					else
					{
						mDispSensitivity.setText("level");
					}
					
				} 
				
				
				
				cmd[2] = "cat "+ rootDir+ "current_eint_pol";
				ret = ShellExe.execCommand(cmd);
				if (0 != ret) {					
					mDispPolarity.setText("Get Polarity Error.");
					return;
				} 
				else
				{
					if(ShellExe.getOutput().equalsIgnoreCase("0"))
					{
						mDispPolarity.setText("active-low");
					}
					else
					{
						mDispPolarity.setText("active-high");
					}
				}
				
				return;
			} 

		} catch (IOException e) {
			Xlog.i(TAG, e.toString());
			ShowDialog("Shell Exception!", e.toString());			
		}
	}
}
