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

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import com.mediatek.xlog.Xlog;

import com.mediatek.engineermode.R;

public class Configuration extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

	private static final String TAG = "Configuration";
	private ListPreference mAudioChanneAdaptPref;
	private ListPreference mVideoChanneAdaptlPref;
	private ListPreference mVideoChanneRevPref;
	private ListPreference mMultiPref;
	private ListPreference mVideoCdecPref;
	private ListPreference mUseWnsrpPref;
	private ListPreference mTerminalTypePref;

	private static final String AUDIO_CHANNEL_ADAPTATION = "audio_channel_adaptation";
	private static final String VIDEO_CHANNEL_ADAPTATION = "video_channel_adaptation";
	private static final String VIDEO_CHANNEL_REV = "video_channel_reverse_data_type";
	private static final String MULTIPLE_LEVEL = "multiplex_level";
	private static final String VIDEO_CODEC = "video_codec_preference";
	private static final String USE_WNSRP = "use_wnsrp";
	private static final String TERMINAL_TYPE = "terminal_type";
	
	private String[] mAudioArr;
	private String[] mVideoArr;
	private String[] mVideoRevArr;
	private String[] mMultiArr;
	private String[] mVideoCodecArr;
	private String[] mUseArr;
	private String[] mTerminalArr;

	private Resources res;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.configuration);

		res = this.getResources();

		try {
			mAudioArr = res.getStringArray(R.array.working_mode_audio_channels);
			mVideoArr = res.getStringArray(R.array.working_mode_video_channels);
			mVideoRevArr = res.getStringArray(R.array.working_mode_video_channels_reverse);
			mMultiArr = res.getStringArray(R.array.working_mode_multiplex_level);
			mVideoCodecArr = res.getStringArray(R.array.working_mode_video_codec_preference);
			mUseArr = res.getStringArray(R.array.working_mode_use_wnsrp);
			mTerminalArr = res.getStringArray(R.array.working_mode_terminal_type);

			mAudioChanneAdaptPref = (ListPreference) findPreference(AUDIO_CHANNEL_ADAPTATION);
			mVideoChanneAdaptlPref = (ListPreference) findPreference(VIDEO_CHANNEL_ADAPTATION);
			mVideoChanneRevPref = (ListPreference) findPreference(VIDEO_CHANNEL_REV);
			mMultiPref = (ListPreference) findPreference(MULTIPLE_LEVEL);
			mVideoCdecPref = (ListPreference) findPreference(VIDEO_CODEC);
			mUseWnsrpPref = (ListPreference) findPreference(USE_WNSRP);
			mTerminalTypePref = (ListPreference) findPreference(TERMINAL_TYPE);
			
			if(mAudioChanneAdaptPref == null 
					|| mVideoChanneAdaptlPref == null 
					|| mVideoChanneRevPref == null 
					|| mMultiPref == null 
					|| mVideoCdecPref == null 
					|| mUseWnsrpPref == null 
					|| mTerminalTypePref == null 
					)
			{
				Xlog.d(TAG, "clocwork worked...");	
				//not return and let exception happened.
			}

			mAudioChanneAdaptPref.setOnPreferenceChangeListener(this);
			mVideoChanneAdaptlPref.setOnPreferenceChangeListener(this);
			mVideoChanneRevPref.setOnPreferenceChangeListener(this);
			mMultiPref.setOnPreferenceChangeListener(this);
			mVideoCdecPref.setOnPreferenceChangeListener(this);
			mUseWnsrpPref.setOnPreferenceChangeListener(this);
			mTerminalTypePref.setOnPreferenceChangeListener(this);

			initSummary();

		} catch (Exception e) {
			e.printStackTrace();
			Xlog.e(TAG, "Exception : " + e.getMessage());
		}

	}

	private void initSummary() {
		
		String audioSummary = "AL2 WithSequenceNumber";
		String videoSummary = "AL2 WithSequenceNumber";
		String videoRevSummary = "No Change";
		String multiSummary = "MuxLevel 2";
		String videoCodecSummary = "MPEG4_H263";
		String useSummary = "ON";
		String terminalSummary = "Normal";
		
		try {
			SharedPreferences preferences = getSharedPreferences(VideoTelephony.ENGINEER_MODE_PREFERENCE, WorkingMode.MODE_WORLD_READABLE);
			String strIndex = "0";
			int index;
			
			strIndex = preferences.getString(VideoTelephony.CONFIG_AUDIO_CHANNEL_ADAPT, "1");
			index = Integer.valueOf(strIndex).intValue();
			audioSummary = mAudioArr[index];
			
			strIndex = preferences.getString(VideoTelephony.CONFIG_VIDEO_CHANNEL_ADAPT, "1");
			index = Integer.valueOf(strIndex).intValue();
			videoSummary = mVideoArr[index];
			
			strIndex = preferences.getString(VideoTelephony.CONFIG_VIDEO_CHANNEL_REVERSE, "0");
			index = Integer.valueOf(strIndex).intValue();
			videoRevSummary = mVideoRevArr[index];
			
			strIndex = preferences.getString(VideoTelephony.CONFIG_MULTIPLEX_LEVEL, "4");
			index = Integer.valueOf(strIndex).intValue();
			multiSummary = mMultiArr[index];
			
			strIndex = preferences.getString(VideoTelephony.CONFIG_VIDEO_CODEC_PREFERENCE, "1");
			index = Integer.valueOf(strIndex).intValue();
			videoCodecSummary = mVideoCodecArr[index];
			
			strIndex = preferences.getString(VideoTelephony.CONFIG_USE_WNSRP, "2");
			index = Integer.valueOf(strIndex).intValue();
			useSummary = mUseArr[index];
			
			strIndex = preferences.getString(VideoTelephony.CONFIG_TERMINAL_TYPE, "1");
			index = Integer.valueOf(strIndex).intValue();
			terminalSummary = mTerminalArr[index];
			
		} catch (Exception e) {
			e.printStackTrace();
			Xlog.e(TAG, "Exception : " + e.getMessage());
		}
		
		mAudioChanneAdaptPref.setSummary(audioSummary);
		mVideoChanneAdaptlPref.setSummary(videoSummary);
		mVideoChanneRevPref.setSummary(videoRevSummary);
		mMultiPref.setSummary(multiSummary);
		mVideoCdecPref.setSummary(videoCodecSummary);
		mUseWnsrpPref.setSummary(useSummary);
		mTerminalTypePref.setSummary(terminalSummary);
	}
	
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (null == newValue || null == preference) {
			return false;
		}

		final String key = preference.getKey();

		if (null == key) {
			return false;
		}

		SharedPreferences preferences = getSharedPreferences(VideoTelephony.ENGINEER_MODE_PREFERENCE,
				WorkingMode.MODE_WORLD_READABLE);
		Editor edit = preferences.edit();

		Xlog.v(TAG, "enter onPreferenceChange key is:" + key);

		ListPreference listPreference;

		String prefKey = "";
		String prefValue = "0";
		
		String newSummaryValue = "";
		int newSummaryIndex = 0;
		Xlog.v(TAG, "preference = " + preference);

		if (AUDIO_CHANNEL_ADAPTATION.equals(key)) {

			listPreference = mAudioChanneAdaptPref;
			prefKey = VideoTelephony.CONFIG_AUDIO_CHANNEL_ADAPT;
			
			Xlog.v(TAG, "newValue = " + newValue);
			String index = (String) newValue;
			newSummaryIndex = Integer.valueOf(index);
			newSummaryValue = mAudioArr[newSummaryIndex];

		} else if (VIDEO_CHANNEL_ADAPTATION.equals(key)) {

			listPreference = mVideoChanneAdaptlPref;
			prefKey = VideoTelephony.CONFIG_VIDEO_CHANNEL_ADAPT;
			
			Xlog.v(TAG, "newValue = " + newValue);
			String index = (String) newValue;
			newSummaryIndex = Integer.valueOf(index);
			newSummaryValue = mVideoArr[newSummaryIndex];

		} else if (VIDEO_CHANNEL_REV.equals(key)) {

			listPreference = mVideoChanneRevPref;
			prefKey = VideoTelephony.CONFIG_VIDEO_CHANNEL_REVERSE;
			
			Xlog.v(TAG, "newValue = " + newValue);
			String index = (String) newValue;
			newSummaryIndex = Integer.valueOf(index);
			newSummaryValue = mVideoRevArr[newSummaryIndex];

		} else if (MULTIPLE_LEVEL.equals(key)) {

			listPreference = mMultiPref;
			prefKey = VideoTelephony.CONFIG_MULTIPLEX_LEVEL;
			
			Xlog.v(TAG, "newValue = " + newValue);
			String index = (String) newValue;
			newSummaryIndex = Integer.valueOf(index);
			newSummaryValue = mMultiArr[newSummaryIndex];

		} else if (VIDEO_CODEC.equals(key)) {

			listPreference = mVideoCdecPref;
			prefKey = VideoTelephony.CONFIG_VIDEO_CODEC_PREFERENCE;
			
			Xlog.v(TAG, "newValue = " + newValue);
			String index = (String) newValue;
			newSummaryIndex = Integer.valueOf(index);
			newSummaryValue = mVideoCodecArr[newSummaryIndex];

		} else if (USE_WNSRP.equals(key)) {

			listPreference = mUseWnsrpPref;
			prefKey = VideoTelephony.CONFIG_USE_WNSRP;
			
			Xlog.v(TAG, "newValue = " + newValue);
			String index = (String) newValue;
			newSummaryIndex = Integer.valueOf(index);
			newSummaryValue = mUseArr[newSummaryIndex];

		} else { // if (TERMINAL_TYPE.equals(key))

			listPreference = mTerminalTypePref;
			prefKey = VideoTelephony.CONFIG_TERMINAL_TYPE;
			
			Xlog.v(TAG, "newValue = " + newValue);
			String index = (String) newValue;
			newSummaryIndex = Integer.valueOf(index);
			newSummaryValue = mTerminalArr[newSummaryIndex];

		}
		prefValue = (String) newValue;

		edit.putString(prefKey, prefValue);
		edit.commit();

		Xlog.v(TAG, "prefValue is:" + prefValue);

		setPreferenceSummary(newSummaryValue, key, listPreference);

		return true;
	}

	private void setPreferenceSummary(String value, String key, ListPreference listPreference) {
		if (listPreference != null && value != null) {
			if (value.length() == 0) {
				listPreference.setSummary(listPreference.getValue());
				Xlog.v(TAG, "setSummary : listPreference.getValue() = " + listPreference.getValue());
			} else {
				listPreference.setSummary(value);
				Xlog.v(TAG, "setSummary : value = " + value);
			}

		}
	}


}
