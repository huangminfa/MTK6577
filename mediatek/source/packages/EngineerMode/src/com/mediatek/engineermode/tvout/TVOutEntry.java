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

package com.mediatek.engineermode.tvout;

import com.mediatek.engineermode.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import com.mediatek.xlog.Xlog;
import com.mediatek.tvOut.TvOut;

public class TVOutEntry extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {

	private static final String TAG = "EM/TVOut";
	private static final String TVOUT_ENABLE = "tvout_en_disable";
	private static final String CHINESE_TEST_PATT = "chinese_test_patter";
	private static final String KEY_TV_SYSTEM = "tv_system";
	private PreferenceScreen mPreferenceScreen;
	private CheckBoxPreference mIsTvoutCheck;
	private Preference mChineseTestPat;
	private ListPreference mTVSystem;
	private TvOut mTvOut;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Xlog.i(TAG, "TVOutEntry->onCreate().");
		addPreferencesFromResource(R.xml.tvout_entry);
		mPreferenceScreen = this.getPreferenceScreen();
		mIsTvoutCheck = (CheckBoxPreference) findPreference(TVOUT_ENABLE);
		mChineseTestPat = findPreference(CHINESE_TEST_PATT);
		mTVSystem = (ListPreference) findPreference(KEY_TV_SYSTEM);
		mTvOut = new TvOut();

		if (mPreferenceScreen == null || mIsTvoutCheck == null
				|| mChineseTestPat == null) {
			Xlog.d(TAG, "clocwork worked...");
			this.finish();
		}
		mIsTvoutCheck.setOnPreferenceChangeListener(this);
		mChineseTestPat.setOnPreferenceChangeListener(this);
		mTVSystem.setOnPreferenceChangeListener(this);
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		Xlog.i(TAG, "TVOutEntry->onPreferenceChange().");
		if (preference == null || newValue == null) {
			return false;
		}

		if (preference.getKey().equals(TVOUT_ENABLE)) {

			if (newValue.equals(false)) {
				Xlog.v(TAG, "TVOutEntry->checkbox disable.");
				 mTvOut.enableTvOutManual(false);
			} else {
				Xlog.v(TAG, "TVOutEntry->checkbox enable.");
				 mTvOut.enableTvOutManual(true);
			}
		}
		 if (preference.getKey().equals(KEY_TV_SYSTEM)) {
	            if (newValue.equals("NTSC")){
	                mTvOut.setTvSystem(TvOut.NTSC);
	            }
	            else{
	                mTvOut.setTvSystem(TvOut.PAL);
	            }   
	            mTVSystem.setSummary(newValue.toString());
	        }
		return true;
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		super.onPreferenceTreeClick(preferenceScreen, preference);
		Xlog.i(TAG, "TVOutEntry->onPreferenceTreeClick().");
		if (preference.getKey().equals(CHINESE_TEST_PATT)) {
			Xlog.v(TAG, "TVOutEntry->start TVOutActivity.");
			startActivity(new Intent(this, TVOutActivity.class));
		}
		return true;
	}

}
