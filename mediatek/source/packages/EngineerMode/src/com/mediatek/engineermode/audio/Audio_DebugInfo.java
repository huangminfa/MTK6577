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

package com.mediatek.engineermode.audio;

import com.mediatek.engineermode.R;

import android.app.Activity;
import android.os.Bundle;
import com.mediatek.xlog.Xlog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import android.media.AudioSystem;
import android.app.AlertDialog;
import android.content.SharedPreferences;

public class Audio_DebugInfo extends Activity implements OnItemSelectedListener, OnClickListener {
	
	private Spinner mSpinner;
	private EditText mEdit;
	private Button mBtnSet;
	private ArrayAdapter<String> SpinnerAdatper;
	private int mSpinnerIndex;

	private String TAG = "EM/Audio";

	private static final int VOLUME_SPEECH_SIZE = 310;
	private final int DATA_SIZE = 1444;
	private byte[] data;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_debuginfo);
        
        mSpinner = (Spinner)findViewById(R.id.Audio_DebugInfo_Spinner);
        mEdit = (EditText)findViewById(R.id.Audio_DebugInfo_EditText);
        mBtnSet = (Button)findViewById(R.id.Audio_DebugInfo_Button);
        if(mBtnSet == null)
        {
        	Xlog.d(TAG, "clocwork worked...");	
    		//not return and let exception happened.
        }
        mBtnSet.setOnClickListener(this);
        
        //create ArrayAdapter for Spinner
        SpinnerAdatper = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        SpinnerAdatper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(int i = 0; i < 16; i ++)
        {
        	SpinnerAdatper.add("Parameter " + String.valueOf(i));
        }       
        mSpinner.setAdapter(SpinnerAdatper);
        mSpinner.setOnItemSelectedListener(this);

	//get the current data
        data = new byte[DATA_SIZE];
        for(int n = 0; n < DATA_SIZE; n ++)
        {
        	data[n] = 0;
        }

        int ret = AudioSystem.GetEMParameter(data, DATA_SIZE);
        if(ret != 0)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Get data error");
		builder.setMessage("Get audio data error.");
		builder.setPositiveButton("OK" , null);
		builder.create().show();
		Xlog.i(TAG, "Audio_DebugInfo GetEMParameter return value is : " + ret);
	}
        
        final SharedPreferences preferences = this.getSharedPreferences(
				"audio_record", android.content.Context.MODE_PRIVATE);
        mSpinnerIndex = preferences.getInt("NUM", 0);

        mSpinner.setSelection(mSpinnerIndex);
        int initValue = data[VOLUME_SPEECH_SIZE + mSpinnerIndex * 2 + 1] * 256 + data[VOLUME_SPEECH_SIZE + mSpinnerIndex * 2];
		mEdit.setText(String.valueOf(initValue));

	}

	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		mSpinnerIndex = arg2;
		
		int initValue = data[VOLUME_SPEECH_SIZE + mSpinnerIndex * 2 + 1] * 256 + data[VOLUME_SPEECH_SIZE + mSpinnerIndex * 2];
		mEdit.setText(String.valueOf(initValue));
		
		final SharedPreferences preferences = this.getSharedPreferences(
				"audio_record", android.content.Context.MODE_PRIVATE);
		
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("NUM", mSpinnerIndex);
		editor.commit();
		
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if(arg0.getId() == mBtnSet.getId())
		{
			if(null == mEdit.getText().toString())
			{
				Toast.makeText(this, "Please input the value.", Toast.LENGTH_LONG).show();
				return;
			}
			if(5 < mEdit.getText().toString().length() || 0 == mEdit.getText().toString().length())
			{
				Toast.makeText(this, "The input is not correct. Please input the number between 0 and 65535.", Toast.LENGTH_LONG).show();
				return;
			}

			long inputValue = Long.valueOf(mEdit.getText().toString());
			if(inputValue > 65535)
			{
				Toast.makeText(this, "The input is not correct. Please input the number between 0 and 65535.", Toast.LENGTH_LONG).show();
				return;
			}
			int high = (int)(inputValue/256);
			int low = (int)(inputValue%256);
			data[VOLUME_SPEECH_SIZE + mSpinnerIndex * 2] = (byte)low;
			data[VOLUME_SPEECH_SIZE + mSpinnerIndex * 2 + 1] = (byte)high;

			int result = AudioSystem.SetEMParameter(data, DATA_SIZE);
			if(0 == result)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Setting success");
				builder.setMessage("Set speech enhancement value succeeded.");
				builder.setPositiveButton("OK" , null);
				builder.create().show();
			}
			else
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Setting error");
				builder.setMessage("Set speech enhancement value failed.");
				builder.setPositiveButton("OK" , null);
				builder.create().show();
				Xlog.i(TAG, "Audio_DebugInfo SetEMParameter return value is : " + result);
			}			
		}
		
	}

}
