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

package com.mediatek.engineermode.videotelephone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import com.mediatek.xlog.Xlog;

import com.mediatek.engineermode.R;

public class TagsSelectActivity extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {

	private String mTagName;
	private String mTagNameKey;
	private String mTagValueKey;

	private CheckBoxPreference mVerbosePref;
	private CheckBoxPreference mDebugPref;
	private CheckBoxPreference mInfoPref;
	private CheckBoxPreference mWarningPref;
	private CheckBoxPreference mErrorPref;
	private CheckBoxPreference mGroup1Pref;
	private CheckBoxPreference mGroup2Pref;
	private CheckBoxPreference mGroup3Pref;
	private CheckBoxPreference mGroup4Pref;
	private CheckBoxPreference mGroup5Pref;
	private CheckBoxPreference mGroup6Pref;
	private CheckBoxPreference mGroup7Pref;
	private CheckBoxPreference mGroup8Pref;
	private CheckBoxPreference mGroup9Pref;
	private CheckBoxPreference mGroup10Pref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.tag_select);

		mVerbosePref = (CheckBoxPreference) findPreference("log_filter_tag_0");
		mDebugPref = (CheckBoxPreference) findPreference("log_filter_tag_1");
		mInfoPref = (CheckBoxPreference) findPreference("log_filter_tag_2");
		mWarningPref = (CheckBoxPreference) findPreference("log_filter_tag_3");
		mErrorPref = (CheckBoxPreference) findPreference("log_filter_tag_4");
		mGroup1Pref = (CheckBoxPreference) findPreference("log_filter_tag_5");
		mGroup2Pref = (CheckBoxPreference) findPreference("log_filter_tag_6");
		mGroup3Pref = (CheckBoxPreference) findPreference("log_filter_tag_7");
		mGroup4Pref = (CheckBoxPreference) findPreference("log_filter_tag_8");
		mGroup5Pref = (CheckBoxPreference) findPreference("log_filter_tag_9");
		mGroup6Pref = (CheckBoxPreference) findPreference("log_filter_tag_10");
		mGroup7Pref = (CheckBoxPreference) findPreference("log_filter_tag_11");
		mGroup8Pref = (CheckBoxPreference) findPreference("log_filter_tag_12");
		mGroup9Pref = (CheckBoxPreference) findPreference("log_filter_tag_13");
		mGroup10Pref = (CheckBoxPreference) findPreference("log_filter_tag_14");

	}

	@Override
	protected void onResume() {
		super.onResume();

		try {
			Intent intent = this.getIntent();
			mTagNameKey = intent.getStringExtra("tag_name_key");
			mTagName = intent.getStringExtra("tag_name");
			mTagValueKey = intent.getStringExtra("tag_value_key");

			this.setTitle(mTagName);
			initStatus();
		} catch (Exception e) {
			e.printStackTrace();
			Xlog.e("VideoTelephony",
					"TagsSelectActivity get string form intent exception");
		}
	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// // TODO Auto-generated method stub
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// saveStatus();
	// this.finish();
	// return true;
	// }
	// return super.onKeyDown(keyCode, event);
	// }

	// private String toBinary(int num) {
	// StringBuffer result = new StringBuffer();
	// do {
	// result.append(num % 2);
	//
	// Log.v("VideoTelephony", "toBinary() result = " + result.toString());
	// num = num / 2;
	// Log.v("VideoTelephony", "toBinary() num = " + num);
	// } while (num > 0);
	// return result.toString();
	// }

	private void initStatus() {
		try {
			SharedPreferences preferences = getSharedPreferences(
					VideoTelephony.ENGINEER_MODE_PREFERENCE,
					WorkingMode.MODE_WORLD_READABLE);

			int value = preferences.getInt(mTagValueKey, -1);
			if (-1 == value) {
				mInfoPref.setChecked(true);
				mWarningPref.setChecked(true);
				mErrorPref.setChecked(true);
				return;
			}

			if ((value & 1) != 0) {
				mVerbosePref.setChecked(true);
			}
			if ((value & (1 << 1)) != 0) {
				mDebugPref.setChecked(true);
			}
			if ((value & (1 << 2)) != 0) {
				mInfoPref.setChecked(true);
			}
			if ((value & (1 << 3)) != 0) {
				mWarningPref.setChecked(true);
			}
			if ((value & (1 << 4)) != 0) {
				mErrorPref.setChecked(true);
			}
			if ((value & (1 << 5)) != 0) {
				mGroup1Pref.setChecked(true);
			}
			if ((value & (1 << 6)) != 0) {
				mGroup2Pref.setChecked(true);
			}
			if ((value & (1 << 7)) != 0) {
				mGroup3Pref.setChecked(true);
			}
			if ((value & (1 << 8)) != 0) {
				mGroup4Pref.setChecked(true);
			}
			if ((value & (1 << 9)) != 0) {
				mGroup5Pref.setChecked(true);
			}
			if ((value & (1 << 10)) != 0) {
				mGroup6Pref.setChecked(true);
			}
			if ((value & (1 << 11)) != 0) {
				mGroup7Pref.setChecked(true);
			}
			if ((value & (1 << 12)) != 0) {
				mGroup8Pref.setChecked(true);
			}
			if ((value & (1 << 13)) != 0) {
				mGroup9Pref.setChecked(true);
			}
			if ((value & (1 << 14)) != 0) {
				mGroup10Pref.setChecked(true);
			}

			// String result = toBinary(value);
			// Log.v("VideoTelephony", "value = " + value +
			// " toBinary result = " + result);

			// int length = result.length();
			// while (length < 15) {
			// result = "0" + result;
			// length++;
			// }
			// Log.v("VideoTelephony", "After while result = " + result);
			//
			// if (result.charAt(0) == '1') {
			// mVerbosePref.setChecked(true);
			// }
			// if (result.charAt(1) == '1') {
			// mDebugPref.setChecked(true);
			// }
			// if (result.charAt(2) == '1') {
			// mInfoPref.setChecked(true);
			// }
			// if (result.charAt(3) == '1') {
			// mWarningPref.setChecked(true);
			// }
			// if (result.charAt(4) == '1') {
			// mErrorPref.setChecked(true);
			// }
			// if (result.charAt(5) == '1') {
			// mGroup1Pref.setChecked(true);
			// }
			// if (result.charAt(6) == '1') {
			// mGroup2Pref.setChecked(true);
			// }
			// if (result.charAt(7) == '1') {
			// mGroup3Pref.setChecked(true);
			// }
			// if (result.charAt(8) == '1') {
			// mGroup4Pref.setChecked(true);
			// }
			// if (result.charAt(9) == '1') {
			// mGroup5Pref.setChecked(true);
			// }
			// if (result.charAt(10) == '1') {
			// mGroup6Pref.setChecked(true);
			// }
			// if (result.charAt(11) == '1') {
			// mGroup7Pref.setChecked(true);
			// }
			// if (result.charAt(12) == '1') {
			// mGroup8Pref.setChecked(true);
			// }
			// if (result.charAt(13) == '1') {
			// mGroup9Pref.setChecked(true);
			// }
			// if (result.charAt(14) == '1') {
			// mGroup10Pref.setChecked(true);
			// }

		} catch (Exception e) {
			e.printStackTrace();
			Xlog.e("VideoTelephony",
					"TagsSelectActivity get string from pref exception");
		}
	}

	private void saveStatus() {
		try {
			int value = 0;
			if (mVerbosePref.isChecked()) {
				value = value + 1;
			}
			if (mDebugPref.isChecked()) {
				value = value + (1 << 1);
			}
			if (mInfoPref.isChecked()) {
				value = value + (1 << 2);
			}
			if (mWarningPref.isChecked()) {
				value = value + (1 << 3);
			}
			if (mErrorPref.isChecked()) {
				value = value + (1 << 4);
			}
			if (mGroup1Pref.isChecked()) {
				value = value + (1 << 5);
			}
			if (mGroup2Pref.isChecked()) {
				value = value + (1 << 6);
			}
			if (mGroup3Pref.isChecked()) {
				value = value + (1 << 7);
			}
			if (mGroup4Pref.isChecked()) {
				value = value + (1 << 8);
			}
			if (mGroup5Pref.isChecked()) {
				value = value + (1 << 9);
			}
			if (mGroup6Pref.isChecked()) {
				value = value + (1 << 10);
			}
			if (mGroup7Pref.isChecked()) {
				value = value + (1 << 11);
			}
			if (mGroup8Pref.isChecked()) {
				value = value + (1 << 12);
			}
			if (mGroup9Pref.isChecked()) {
				value = value + (1 << 13);
			}
			if (mGroup10Pref.isChecked()) {
				value = value + (1 << 14);
			}

			Xlog.v("VideoTelephony", "TagsSelectActivity saveStatus() value = "
					+ value);

			SharedPreferences preferences = getSharedPreferences(
					VideoTelephony.ENGINEER_MODE_PREFERENCE,
					WorkingMode.MODE_WORLD_READABLE);
			Editor edit = preferences.edit();
			edit.putString(mTagNameKey, mTagName);
			edit.putInt(mTagValueKey, value);
			edit.commit();
			Xlog.v("VideoTelephony", "mTagNameKey = " + mTagNameKey
					+ " mTagName = " + mTagName);
			Xlog.v("VideoTelephony", "mTagValueKey = " + mTagValueKey
					+ " tagValue = " + value);
		} catch (Exception e) {
			e.printStackTrace();
			Xlog.v("VideoTelephony",
					"TagsSelectActivity get string from pref exception");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveStatus();
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return false;
	}

}
