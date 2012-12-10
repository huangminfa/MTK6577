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

package com.mediatek.matv;

//import com.mediatek.atv.AtvService;

import com.mediatek.matv.R;

import android.os.Bundle;
import android.app.AlertDialog;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import com.mediatek.atv.AtvService;

public class Matv extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {

	private static final String KEY_FRAME_RATE = "frame_rate";
	private static final String KEY_NR_LEVEL = "nr_level";

	private ListPreference LPframeRate;
	private ListPreference LPnrLevel;
	private String TAG = "EM/matv";

	private AtvService atv_svr = new AtvService(null);

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.layout.matv);

		LPframeRate = (ListPreference) findPreference(KEY_FRAME_RATE);
		LPnrLevel = (ListPreference) findPreference(KEY_NR_LEVEL);
		if (LPframeRate == null || LPnrLevel == null) {
			// not return and let exception happened.
		}

		LPframeRate.setOnPreferenceChangeListener(this);
		LPnrLevel.setOnPreferenceChangeListener(this);
		LPframeRate.setSummary("Frame rate "
				+ getPreferenceScreen().getSharedPreferences().getString(
						KEY_FRAME_RATE, "x"));
		LPnrLevel.setSummary("NR Level 0x"
				+ getPreferenceScreen().getSharedPreferences().getString(
						KEY_NR_LEVEL, "x"));
	}

	@Override
	public void onPause() {
		// EMbaseband.End();
		super.onPause();
	}

	@Override
	protected void onResume() {
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

	public boolean onPreferenceChange(Preference preference, Object objValue) {

		final String key = preference.getKey();

		if (KEY_FRAME_RATE.equals(key)) {
			try {
				int value = Integer.parseInt((String) objValue);
				Elog.v(TAG, KEY_FRAME_RATE + "value is. " + value);
				 atv_svr.setChipDep(180, value);
				LPframeRate.setSummary("Frame Rate " + (String) objValue);
			} catch (NumberFormatException e) {
				Elog.e(TAG, "set frame rate exception. ");
			}

		} else if (KEY_NR_LEVEL.equals(key)) {
			try {
				int value = Integer.parseInt((String) objValue, 16);
				Elog.v(TAG, KEY_NR_LEVEL + "value is. " + value);
			    atv_svr.setChipDep(185, value);
				LPnrLevel
						.setSummary("NR Level " + String.format("0x%X", value));
			} catch (NumberFormatException e) {
				Elog.e(TAG, "set NR level exception. ");
			}
		} else {
			ShowDialog("N", "N");
		}

		return true;
	}

}
