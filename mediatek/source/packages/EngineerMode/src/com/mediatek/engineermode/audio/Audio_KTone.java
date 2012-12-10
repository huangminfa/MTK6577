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
import android.content.Intent;
import android.os.Bundle;
import com.mediatek.xlog.Xlog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.media.AudioSystem;
import android.widget.Toast;
import android.app.AlertDialog;

public class Audio_KTone extends Activity implements OnClickListener {
		
	private static int mCurrentK = 0;
	private static boolean mCurrentKstatus = false;
	
	private Spinner mKSpinner;	
	private int mKIndex;
	private String mKType[] = {"1K", "2K", "3K", "4K", "5K", "6K", "7K", 
			"8K", "9K", "10K", "11K", "12K", "13K", "14K", "15K"};	
	private ArrayAdapter<String> KAdatper;		
		
	
	private Button mBtnStart;	
	private Button mBtnStop;

	private String TAG = "EM/Audio";
	
	//AudioSystem::SetAudioCommand(8, 1); //start
	//AudioSystem::SetAudioCommand(9, 666); //stop

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio_ktone);
		
		mBtnStart = (Button) findViewById(R.id.Audio_KTone_StartBtn);		
		mBtnStop = (Button) findViewById(R.id.Audio_KTone_StopBtn);		
		mKSpinner = (Spinner) findViewById(R.id.Audio_KTone_Selector);
		if(mBtnStart == null
        		|| mBtnStop == null
        		|| mKSpinner == null)
        {
			Xlog.d(TAG, "clocwork worked...");	
    		//not return and let exception happened.
        }	

		KAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		KAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < mKType.length; i++) {
			KAdatper.add(mKType[i]);
		}
		mKSpinner.setAdapter(KAdatper);
		mKSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				mKIndex = arg2;
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});		
		
		mBtnStart.setOnClickListener(this);
		mBtnStop.setOnClickListener(this);

		mKSpinner.setSelection(mCurrentK, true);
		if(mCurrentKstatus)
		{
			mBtnStop.setEnabled(true);
			mBtnStart.setEnabled(false);
			mKSpinner.setEnabled(false);
		}
		else
		{
			mBtnStop.setEnabled(false);
			mBtnStart.setEnabled(true);
			mKSpinner.setEnabled(true);
		}
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (arg0.getId() == mBtnStart.getId()) {
			AudioSystem.SetAudioCommand(8, mKIndex+1); //start
			mCurrentK = mKIndex;
			mCurrentKstatus = true;
			mBtnStop.setEnabled(true);
			mBtnStart.setEnabled(false);
			mKSpinner.setEnabled(false);
			Toast
			.makeText(
					this,
					"Start tone "+mKType[mCurrentK],
					Toast.LENGTH_LONG).show();
		}
		
		else if(arg0.getId() == mBtnStop.getId()) {
			AudioSystem.SetAudioCommand(9, 666); //stop
			//mCurrentK = 0;
			mCurrentKstatus = false;
			mBtnStop.setEnabled(false);
			mBtnStart.setEnabled(true);
			mKSpinner.setEnabled(true);
		}
	}
}
