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

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import android.media.AudioSystem;
import android.app.AlertDialog;
import com.mediatek.featureoption.FeatureOption;

/*
 * C structure
 * struct_AUDIO_CUSTOM_WB_PARAM_STRUCT
 * {
 *    ushort speech_mode_wb_para[8][16];
 *    short  sph_wb_in_fir[6][90];
 *    short  sph_wb_out_fir[6][90];
 * }
 * sizeof() = 2416, 2 bytes alignment.
 * GET_WB_SPEECH_PARAMETER = 0X40;
 * SET_WB_SPEECH_PARAMETER = 0X41;
 * SetAudioData()&GetAudioData();
 * use MTK_WB_SPEECH_SUPPORT.
 * */
public class Audio_SpeechEnhancement extends Activity implements
		OnClickListener {
	private Button mBtnSet;
	private TextView mText;
	private EditText mEdit;
	private Spinner mModeSpinner;
	private Spinner mParaSpinner;
	private ArrayAdapter<String> ModeAdatper;
	private ArrayAdapter<String> ParaAdatper;
	private int mModeIndex;
	private int mParaIndex;
	private String mModeType[] = { "Common Parameter", "Normal Mode",
			"Headset Mode", "LoudSpeaker Mode", "BT Earphone Mode",
			"BT Cordless Mode", "BT Karkit Mode", "AUX1 Mode", "AUX2 Mode" };

	private String TAG = "EM/Audio";

	private static final int VOLUME_SIZE = 22;
	private static final int COMMON_PARA_SIZE = 24;
	private final int DATA_SIZE = 1444;
	private byte[] data;
	private byte[] WBdata;

	private int ACTURAL_PARAM_NUM = 16;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio_speechenhancement);

		mBtnSet = (Button) findViewById(R.id.Audio_SpEnhancement_Button);
		mText = (TextView) findViewById(R.id.Audio_SpEnhancement_TextView);
		mEdit = (EditText) findViewById(R.id.Audio_SpEnhancement_EditText);
		mModeSpinner = (Spinner) findViewById(R.id.Audio_SpEnhancement_ModeType);
		mParaSpinner = (Spinner) findViewById(R.id.Audio_SpEnhancement_ParaType);
		if (mBtnSet == null || mText == null || mEdit == null
				|| mModeSpinner == null || mParaSpinner == null) {
			Elog.e(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

		// if(false != FeatureOption.MTK_WB_SPEECH_SUPPORT){
		ACTURAL_PARAM_NUM = 32;
		// }

		// create ArrayAdapter for Spinner
		ModeAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		ModeAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < mModeType.length; i++) {
			ModeAdatper.add(mModeType[i]);
		}
		mModeSpinner.setAdapter(ModeAdatper);
		mModeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				mModeIndex = arg2;
				if (0 != arg2) {
					ParaAdatper.clear();
					for (int i = 0; i < ACTURAL_PARAM_NUM; i++) {
						ParaAdatper.add("Parameter " + String.valueOf(i));
					}
				} else {
					ParaAdatper.clear();
					for (int i = 0; i < 12; i++) {
						ParaAdatper.add("Parameter " + String.valueOf(i));
					}
				}

				mParaSpinner.setSelection(0);
				mParaIndex = 0;

				int initValue = getAudioData();
				mEdit.setText(String.valueOf(initValue));

			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		mBtnSet.setOnClickListener(this);
		int maxValue = 65535;
		mText.setText("Value is 0~" + String.valueOf(maxValue));

		// create ArrayAdapter for Spinner
		ParaAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		ParaAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < 12; i++) {
			ParaAdatper.add("Parameter " + String.valueOf(i));
		}
		mParaSpinner.setAdapter(ParaAdatper);
		mParaSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				mParaIndex = arg2;

				int initValue = getAudioData();
				mEdit.setText(String.valueOf(initValue));
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		// get the current data
		data = new byte[DATA_SIZE];
		for (int n = 0; n < DATA_SIZE; n++) {
			data[n] = 0;
		}
		WBdata = new byte[WB_DATA_SIZE];
		for (int n = 0; n < WB_DATA_SIZE; n++) {
			WBdata[n] = 0;
		}
		int ret = AudioSystem.GetEMParameter(data, DATA_SIZE);
		if (ret != 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Get data error");
			builder.setMessage("Get audio data error.");
			builder.setPositiveButton("OK", null);
			builder.create().show();
			Elog.i(TAG,
					"Audio_SpeechEnhancement GetEMParameter return value is : "
							+ ret);
		}
		/*
		 * String msg = "GET Audio data: "; for (int n = 0; n < DATA_SIZE; n++)
		 * { msg += data[n]; msg += " "; } Elog.i(TAG, msg);
		 */

		ret = AudioSystem.GetAudioData(GET_WB_SPEECH_PARAMETER, WB_DATA_SIZE,
				WBdata);
		if (ret != 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Get WB data error");
			builder.setMessage("Get audio WB data error.");
			builder.setPositiveButton("OK", null);
			builder.create().show();
		}
		/*
		 * msg = "GET Audio WB data: "; for (int n = 0; n < WB_DATA_SIZE; n++) {
		 * msg += WBdata[n]; msg += " "; } Elog.i(TAG, msg);
		 */

		mModeSpinner.setSelection(0);
		mParaSpinner.setSelection(0);
		mModeIndex = 0;
		mParaIndex = 0;
		int initValue = getAudioData();
		mEdit.setText(String.valueOf(initValue));

	}

	public void onClick(View arg0) {

		if (arg0.getId() == mBtnSet.getId()) {
			if (null == mEdit.getText().toString()
					|| "" == mEdit.getText().toString()) {
				Toast.makeText(this, "Please input the value.",
						Toast.LENGTH_LONG).show();
				return;
			}
			if (5 < mEdit.getText().toString().length()
					|| 0 == mEdit.getText().toString().length()) {
				Toast
						.makeText(
								this,
								"The input is not correct. Please input the number between 0 and 65535.",
								Toast.LENGTH_LONG).show();
				return;
			}
			int inputValue = Integer.valueOf(mEdit.getText().toString());
			if (inputValue > 65535) {
				Toast
						.makeText(
								this,
								"The input is not correct. Please input the number between 0 and 65535.",
								Toast.LENGTH_LONG).show();
				return;
			}

			setAudioData(inputValue);
		}

	}

	private int getAudioData() {
		if (mParaIndex > 15) {
			return getWBAudioData();
		} else {
			return getSpeechEnhanceAudioData();
		}
	}

	private void setAudioData(int inputValue) {
		if (mParaIndex > 15) {
			setWBAudioData(inputValue);
		} else {
			setSpeechEnhanceAudioData(inputValue);
		}
		return;
	}

	private int getSpeechEnhanceAudioData() {
		int high = 0;
		int low = 0;
		if (mModeIndex == 0) {
			high = data[VOLUME_SIZE + mParaIndex * 2 + 1];
			low = data[VOLUME_SIZE + mParaIndex * 2];

		} else {
			high = data[VOLUME_SIZE + COMMON_PARA_SIZE + (mModeIndex - 1) * 32
					+ mParaIndex * 2 + 1];
			low = data[VOLUME_SIZE + COMMON_PARA_SIZE + (mModeIndex - 1) * 32
					+ mParaIndex * 2];
		}

		high = high < 0 ? high + 256 : high;
		low = low < 0 ? low + 256 : low;
		return high * 256 + low;

	}

	private void setSpeechEnhanceAudioData(int inputValue) {
		int high = (int) (inputValue / 256);
		int low = (int) (inputValue % 256);
		if (mModeIndex == 0) {
			data[VOLUME_SIZE + mParaIndex * 2] = (byte) low;
			data[VOLUME_SIZE + mParaIndex * 2 + 1] = (byte) high;
		} else {
			data[VOLUME_SIZE + COMMON_PARA_SIZE + (mModeIndex - 1) * 32
					+ mParaIndex * 2] = (byte) low;
			data[VOLUME_SIZE + COMMON_PARA_SIZE + (mModeIndex - 1) * 32
					+ mParaIndex * 2 + 1] = (byte) high;
		}

		int result = AudioSystem.SetEMParameter(data, DATA_SIZE);
		if (0 == result) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Setting success");
			builder.setMessage("Set speech enhancement value succeeded.");
			builder.setPositiveButton("OK", null);
			builder.create().show();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Setting error");
			builder.setMessage("Set speech enhancement value failed.");
			builder.setPositiveButton("OK", null);
			builder.create().show();
			Elog.i(TAG,
					"Audio_SpeechEnhancement SetEMParameter return value is : "
							+ result);
		}
	}

	private final int WB_DATA_SIZE = 2416;
	private final int GET_WB_SPEECH_PARAMETER = 0x40;
	private final int SET_WB_SPEECH_PARAMETER = 0x41;

	private int getWBdata(int catalogIdx, int paraIdx) {
		int pad = 256; // turn byte to unsigned byte.
		int ii = 0xFF & (WBdata[catalogIdx * 8 * 4 + paraIdx * 2 + 1] + pad);
		int highByte = 256 * ii;

		int iii = 0xFF & (WBdata[catalogIdx * 8 * 4 + paraIdx * 2] + pad);
		int lowByte = iii;
		Elog.v(TAG, "getWBdata mode " + catalogIdx + ", paraIdx " + paraIdx
				+ "val " + (highByte + lowByte));
		return highByte + lowByte;
	}

	private void setWBdata(int catalogIdx, int paraIdx, int val) {
		int pad = 256; // make byte cast to int.
		WBdata[catalogIdx * 8 * 4 + paraIdx * 2] = (byte) (val % 256);
		WBdata[catalogIdx * 8 * 4 + paraIdx * 2 + 1] = (byte) (val / 256);
	}

	private int getWBAudioData() {
		return getWBdata(mModeIndex - 1, mParaIndex - 16);
	}

	private void setWBAudioData(int inputval) {
		if (mParaIndex < 16) {
			Elog.i(TAG, "Internal error. check the code.");
			return;
		}
		setWBdata(mModeIndex - 1, mParaIndex - 16, inputval);
		int result = AudioSystem.SetAudioData(SET_WB_SPEECH_PARAMETER,
				WB_DATA_SIZE, WBdata);
		if (0 == result) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Setting success");
			builder.setMessage("Set WB data succeeded.");
			builder.setPositiveButton("OK", null);
			builder.create().show();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Setting error");
			builder.setMessage("Set WB data failed.");
			builder.setPositiveButton("OK", null);
			builder.create().show();
			Elog.i(TAG, "WB data SetAudioData return value is : " + result);
		}
	}

}
