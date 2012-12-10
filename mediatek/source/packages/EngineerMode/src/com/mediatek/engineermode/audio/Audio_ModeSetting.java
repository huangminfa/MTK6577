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

public class Audio_ModeSetting extends Activity implements OnClickListener {

	// ==audio structure start
	private final int MAX_VOL_CATEGORY = 3;// normal, headset, handfree.
	private final int MAX_VOL_LEVEL = 7; // 7 level
	private final int MAX_VOL_TYPE = 8; // 8 level
	// 8 types
	private final int[] OFFEST = { MAX_VOL_CATEGORY * MAX_VOL_LEVEL * 0,
			MAX_VOL_CATEGORY * MAX_VOL_LEVEL * 1,
			MAX_VOL_CATEGORY * MAX_VOL_LEVEL * 2,
			MAX_VOL_CATEGORY * MAX_VOL_LEVEL * 3,
			MAX_VOL_CATEGORY * MAX_VOL_LEVEL * 4,
			MAX_VOL_CATEGORY * MAX_VOL_LEVEL * 5,
			MAX_VOL_CATEGORY * MAX_VOL_LEVEL * 6,
			MAX_VOL_CATEGORY * MAX_VOL_LEVEL * 7 };
	private final int TYPE_RING = 0;
	private final int TYPE_KEY = 1;
	private final int TYPE_MIC = 2;
	private final int TYPE_FMR = 3;
	private final int TYPE_SPH = 4;
	private final int TYPE_SID = 5;
	private final int TYPE_MEDIA = 6;
	private final int TYPE_MATV = 7;
	private final int STRUCT_SIZE = MAX_VOL_CATEGORY * MAX_VOL_LEVEL
			* MAX_VOL_TYPE;

	private final int GETAUDIOCUSTOMDATASIZE = 5;
	private final int SETAUDIOCUSTOMDATA = 6;
	private final int GETAUDIOCUSTOMDATA = 7;

	private void setValue(byte[] D, int category, int type, int level, byte val) {
		if (D == null || category >= MAX_VOL_CATEGORY || type >= MAX_VOL_TYPE
				|| level >= MAX_VOL_LEVEL) {
			Xlog.d(TAG, "assert! Check the setting value.");
		}

		D[category * MAX_VOL_LEVEL + level + OFFEST[type]] = val;
	}

	private int getValue(byte[] D, int category, int type, int level) {
		if (D == null || category >= MAX_VOL_CATEGORY || type >= MAX_VOL_TYPE
				|| level >= MAX_VOL_LEVEL) {
			Xlog.d(TAG, "assert! Check the setting value.");
		}

		int pad = 256;
		return 0xFF & (D[category * MAX_VOL_LEVEL + level + OFFEST[type]] + pad);
	}

	// ==audio structure end

	private int mCurrentCategory;
	private Spinner mModeSpinner;
	private int mModeIndex;
	// private String mModeType[] = { "Ring", "Key", "Mic", "FMR",
	// "Sph", "Sid", "Media", "Matv" };
	private String mModeType0[] = { "Sip", "Mic", "Sph", "Sid", "Media" };
	private String mModeType1[] = { "Sip", "Mic", "FMR", "Sph", "Sid", "Media",
			"Matv" };
	private String mModeType2[] = { "Ring", "Sip", "Mic", "FMR", "Sph", "Sid",
			"Media", "Matv" };
	private ArrayAdapter<String> ModeAdatper;

	private Spinner mLevelSpinner;
	private int mLevelIndex;
	private String mModeLevel[] = { "Level 0", "Level 1", "Level 2", "Level 3",
			"Level 4", "Level 5", "Level 6" };
	private ArrayAdapter<String> LevelAdatper;

	private int valRange = 255;
	private TextView mValText;
	private Button mBtnSet;
	private EditText mEdit;
	private Button mBtnSetMaxVol;
	private EditText mEditMaxVol;

	private Spinner mFirSpinner;
	private String mNormalModeFir[] = { "FIR 0", "FIR 1", "FIR 2", "FIR 3",
			"FIR 4", "FIR 5" };
	private ArrayAdapter<String> FirAdatper;

	private TextView mFir_summary;

	private String TAG = "EM/Audio";

	byte[] data = null;

	private boolean isFirstFirSet = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio_modesetting);

		Intent intent = getIntent();
		mCurrentCategory = intent.getIntExtra("CurrentMode", 0);

		int dataSize = AudioSystem.GetAudioCommand(GETAUDIOCUSTOMDATASIZE);
		if (dataSize != STRUCT_SIZE) {
			Xlog.d(TAG, "assert! Check the structure size!");
		}
		data = new byte[dataSize];
		for (int n = 0; n < dataSize; n++) {
			data[n] = 0;
		}

		int ret = AudioSystem.GetAudioData(GETAUDIOCUSTOMDATA, dataSize, data);

		// get the current data
		if (ret != 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Get data error");
			builder.setMessage("Get audio data error.");
			builder.setPositiveButton("OK", null);
			builder.create().show();
			Xlog.i(TAG, "Audio_ModeSetting GetAudioData return value is : "
					+ ret);
		}
		String msg = "GET: ";
		for (int n = 0; n < dataSize; n++) {
			msg += data[n];
			msg += " ";
		}
		Xlog.v(TAG, msg);
		mBtnSet = (Button) findViewById(R.id.Audio_ModeSetting_Button);
		mEdit = (EditText) findViewById(R.id.Audio_ModeSetting_EditText);
		mBtnSetMaxVol = (Button) findViewById(R.id.Audio_MaxVol_Set);
		mEditMaxVol = (EditText) findViewById(R.id.Audio_MaxVol_Edit);
		mModeSpinner = (Spinner) findViewById(R.id.Audio_ModeSetting);
		mFirSpinner = (Spinner) findViewById(R.id.Audio_Fir_Spinner);
		mLevelSpinner = (Spinner) findViewById(R.id.Audio_Level);
		mFir_summary = (TextView) findViewById(R.id.Audio_Fir_Title);
		mValText = (TextView) findViewById(R.id.Audio_ModeSetting_TextView);

		if (mBtnSet == null || mEdit == null || mBtnSetMaxVol == null
				|| mEditMaxVol == null || mModeSpinner == null
				|| mFirSpinner == null || mLevelSpinner == null
				|| mFir_summary == null) {
			Xlog.d(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

		ModeAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		ModeAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		if (mCurrentCategory == 0) {
			for (int i = 0; i < mModeType0.length; i++) {
				ModeAdatper.add(mModeType0[i]);
			}
		} else if (mCurrentCategory == 1) {
			for (int i = 0; i < mModeType1.length; i++) {
				ModeAdatper.add(mModeType1[i]);
			}
		} else {
			for (int i = 0; i < mModeType2.length; i++) {
				ModeAdatper.add(mModeType2[i]);
			}
		}
		mModeSpinner.setAdapter(ModeAdatper);
		mModeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				mValText.setText("Value is 0~255");
				valRange = 255;
				if (mCurrentCategory == 0) {
					if (arg2 == 0 || arg2 == 1) {
						mModeIndex = arg2 + 1;
					} else {
						mModeIndex = arg2 + 2;
					}
				} else if (mCurrentCategory == 1) {
					mModeIndex = arg2 + 1;
				} else {
				//	if (arg2 == 0) {
						mModeIndex = arg2;
//					} else {
//						mModeIndex = arg2 + 1;
//					}
				}
				Xlog.d(TAG, "mModeIndex is:" + mModeIndex);
				if (mModeIndex == 4 || mModeIndex == 5 || mModeIndex == 2) // Mode
				// Sph,
				// Sid,
				// Mic
				{
					// mEditMaxVol.setText("0");
					mEditMaxVol.setEnabled(false);
					mBtnSetMaxVol.setEnabled(false);
					if (mModeIndex == 4)// Mode Sph
					{
						mValText.setText("Value is 0~160");
						valRange = 160;
					}
				} else {
					mEditMaxVol.setEnabled(true);
					mBtnSetMaxVol.setEnabled(true);
				}
				mEdit.setText(String.valueOf(getValue(data, mCurrentCategory,
						mModeIndex, mLevelIndex)));
				setMaxVolEdit();
				Xlog.v(TAG, "SMode: " + mCurrentCategory + " " + mModeIndex
						+ " " + mLevelIndex);
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		LevelAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		LevelAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < mModeLevel.length; i++) {
			LevelAdatper.add(mModeLevel[i]);
		}
		mLevelSpinner.setAdapter(LevelAdatper);
		mLevelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				mLevelIndex = arg2;
				mEdit.setText(String.valueOf(getValue(data, mCurrentCategory,
						mModeIndex, mLevelIndex)));
				setMaxVolEdit();
				Xlog.v(TAG, "SLevel: " + mCurrentCategory + " " + mModeIndex
						+ " " + mLevelIndex);

			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		FirAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		FirAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < mNormalModeFir.length; i++) {
			FirAdatper.add(mNormalModeFir[i]);
		}
		mFirSpinner.setAdapter(FirAdatper);
		mFirSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				if (isFirstFirSet) {
					mFir_summary.setText("***NO FIR Selected***");
					isFirstFirSet = false;
					return;
				}

				int ret = -1;
				if (mCurrentCategory == 0)// normal mode
				{
					ret = AudioSystem.SetAudioCommand(0x20, arg2);
					Xlog.v(TAG, "set normal fir Z" + arg2);
				} else if (mCurrentCategory == 1)// headset mode
				{
					ret = AudioSystem.SetAudioCommand(0x21, arg2);
					Xlog.v(TAG, "set headset fir Z" + arg2);
				} else if (mCurrentCategory == 2)// loudspeaker mode
				{
					ret = AudioSystem.SetAudioCommand(0x22, arg2);
					Xlog.v(TAG, "set loudspeaker fir Z" + arg2);
				}

				if (-1 == ret) {
					mFir_summary.setText("FIR set error!");
					Toast.makeText(Audio_ModeSetting.this,
							"Set error, check permission.", Toast.LENGTH_LONG)
							.show();
				} else {
					mFir_summary.setText("Current selected: " + arg2);
				}

			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
		mBtnSet.setOnClickListener(this);
		mBtnSetMaxVol.setOnClickListener(this);

		mEdit.setText(String.valueOf(getValue(data, mCurrentCategory,
				mModeIndex, mLevelIndex)));
		setMaxVolEdit();
		/*
		 * int retx = -1; if(mCurrentCategory == 0)//normal mode { retx =
		 * AudioSystem.GetAudioCommand(0x10); } else if(mCurrentCategory ==
		 * 1)//headset mode { retx = AudioSystem.GetAudioCommand(0x11); } else
		 * if(mCurrentCategory == 2)//loudspeaker mode { retx =
		 * AudioSystem.GetAudioCommand(0x12); }
		 * 
		 * 
		 * if (-1 == retx) { mFir_summary.setText("Current FIR is unknown");
		 * Toast.makeText(this, "Get data error!", Toast.LENGTH_LONG).show();
		 * mFirSpinner.setSelected(false); } mFirSpinner.setSelection(retx);
		 */

	}

	private void setMaxVolEdit()// Hard code
	{
		Xlog.i(TAG, "Set max vol Edit.");
		if (mCurrentCategory == 0)// normal mode
		{
			mEditMaxVol.setText(String
					.valueOf(getValue(data, 0, TYPE_MEDIA, 4)));
			Xlog.i(TAG, "0 is "
					+ String.valueOf(getValue(data, 0, TYPE_MEDIA, 4)));
		} else if (mCurrentCategory == 1)// headset mode
		{
			mEditMaxVol.setText(String
					.valueOf(getValue(data, 0, TYPE_MEDIA, 5)));
			Xlog.i(TAG, "1 is "
					+ String.valueOf(getValue(data, 0, TYPE_MEDIA, 5)));
		} else if (mCurrentCategory == 2)// loudspeaker mode
		{
			mEditMaxVol.setText(String
					.valueOf(getValue(data, 0, TYPE_MEDIA, 6)));
			Xlog.i(TAG, "2 is "
					+ String.valueOf(getValue(data, 0, TYPE_MEDIA, 6)));
		} else {
			mEditMaxVol.setText("0");
			Xlog.i(TAG, "error is " + 0);
		}
	}

	private void setMaxVolData(byte val)// Hard code
	{
		if (mCurrentCategory == 0)// normal mode
		{
			setValue(data, 0, TYPE_MEDIA, 4, val);
		} else if (mCurrentCategory == 1)// headset mode
		{
			setValue(data, 0, TYPE_MEDIA, 5, val);
		} else if (mCurrentCategory == 2)// loudspeaker mode
		{
			setValue(data, 0, TYPE_MEDIA, 6, val);
		} else {
			// do nothing.
		}
	}

	private void setAudioData() {
		int result = AudioSystem.SetAudioData(SETAUDIOCUSTOMDATA, STRUCT_SIZE,
				data);
		if (0 == result) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Setting success");
			builder.setMessage("Set audio volume succeeded.");
			builder.setPositiveButton("OK", null);
			builder.create().show();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Setting error");
			builder.setMessage("Set audio volume failed.");
			builder.setPositiveButton("OK", null);
			builder.create().show();
			Xlog.i(TAG, "Audio_ModeSetting SetAudioData return value is : "
					+ result);
		}
	}

	private boolean checkEditNumber(EditText edit, int maxValue) {
		String s = edit.getText().toString();
		if (null == s || s.length() == 0) {
			Toast.makeText(this, "Please input the value.", Toast.LENGTH_LONG)
					.show();
			return false;
		}
		String editString = edit.getText().toString();
		try {
			if (Integer.valueOf(editString) > maxValue) {
				Toast.makeText(
						this,
						"The input is not correct. Please input the number between 0 and "
								+ maxValue, Toast.LENGTH_LONG).show();
				return false;
			}
		} catch (Exception e) {
			Toast.makeText(
					this,
					"The input is not correct. Please input the number between 0 and "
							+ maxValue, Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	public void onClick(View arg0) {

		if (arg0.getId() == mBtnSet.getId()) {

			if (!checkEditNumber(mEdit, valRange)) {
				return;
			}
			String editString = mEdit.getText().toString();
			int ii = Integer.valueOf(editString);
			byte x = (byte) ii;
			setValue(data, mCurrentCategory, mModeIndex, mLevelIndex, x);
			setAudioData();
		}

		else if (arg0.getId() == mBtnSetMaxVol.getId()) {
			if (!checkEditNumber(mEditMaxVol, 160)) {
				return;
			}
			String editString = mEditMaxVol.getText().toString();
			int ii = Integer.valueOf(editString);
			byte x = (byte) ii;
			setMaxVolData(x);
			setAudioData();
		}
	}
}
