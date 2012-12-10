package com.android.phone;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.os.Bundle;
import android.content.Intent;
import android.preference.PreferenceActivity;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.provider.Settings;

import com.mediatek.featureoption.FeatureOption;

import java.util.ArrayList;

public class CallRejectSetting extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

	private static final String LOG_TAG = "CallRejectSetting";
	private static final boolean DBG = true;
	
	private final static String VOICE_CALL_REJECT_MODE_KEY = "voice_call_reject_mode_key";
	private final static String VOICE_CALL_REJECT_LIST_KEY = "voice_call_reject_list_key";
	private final static String VIDEO_CALL_REJECT_MODE_KEY = "video_call_reject_mode_key";
	private final static String VIDEO_CALL_REJECT_LIST_KEY = "video_call_reject_list_key";

	private final static String CALL_REJECT_TARGET_CLASS = "com.android.phone.CallRejectListSetting";

	private final static int VOICE_CALL_ALL_NUMBERS = 100;
	private final static int VIDEO_CALL_ALL_NUMBERS = 200;

	private String[] mCallRejectModeArray;
    	private ListPreference mVoiceRejectSetting;
    	private Preference mVoiceRejectList;
    	private ListPreference mVideoRejectSetting;
    	private Preference mVideoRejectList;

	private static void log(String msg) {
		Log.d(LOG_TAG, msg);
	}

	@Override
	protected void onCreate(Bundle icicle) {
		if (DBG) log("onCreate!!");

		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.call_reject_setting);
		mCallRejectModeArray = getResources().getStringArray(R.array.call_reject_mode_entries);

        	mVoiceRejectSetting = (ListPreference) findPreference(VOICE_CALL_REJECT_MODE_KEY); 
		mVoiceRejectSetting.setOnPreferenceChangeListener(this); 

        	mVoiceRejectList= (Preference) findPreference(VOICE_CALL_REJECT_LIST_KEY); 
		mVoiceRejectList.setOnPreferenceChangeListener(this); 

        	mVideoRejectSetting = (ListPreference) findPreference(VIDEO_CALL_REJECT_MODE_KEY);  
		mVideoRejectSetting.setOnPreferenceChangeListener(this); 

        	mVideoRejectList = (Preference) findPreference(VIDEO_CALL_REJECT_LIST_KEY);  
		mVideoRejectList.setOnPreferenceChangeListener(this); 

        if (!FeatureOption.MTK_VT3G324M_SUPPORT)
        {
            getPreferenceScreen().removePreference(mVideoRejectSetting);
            getPreferenceScreen().removePreference(mVideoRejectList);
        }
	}

	@Override
	protected void onResume() {
            if (DBG) log("onResume()...");
	    super.onResume();
	    int voiceRejectMode = Settings.System.getInt(getContentResolver(), Settings.System.VOICE_CALL_REJECT_MODE, 0);
	    mVoiceRejectSetting.setValueIndex(voiceRejectMode);
	    mVoiceRejectSetting.setSummary(mCallRejectModeArray[voiceRejectMode]);
            mVoiceRejectList.setEnabled(voiceRejectMode == 2);

	    int videoRejectMode = Settings.System.getInt(getContentResolver(), Settings.System.VT_CALL_REJECT_MODE, 0);
	    mVideoRejectSetting.setValueIndex(videoRejectMode);
	    mVideoRejectSetting.setSummary(mCallRejectModeArray[videoRejectMode]);
            mVideoRejectList.setEnabled(videoRejectMode == 2);
	}

	@Override
	public void onPause() {
		if (DBG) log("onPause()...");
		super.onPause();
	}

	@Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		log("Enter onPreferenceTreeClick function.");
		if(preference == mVoiceRejectList){
		    Intent intent = new Intent(this, CallRejectListSetting.class);
		    intent.putExtra("type", "voice");
		    startActivity(intent);
		}else if(preference == mVideoRejectList){
		    Intent intent = new Intent(this, CallRejectListSetting.class);
		    intent.putExtra("type", "video");
		    startActivity(intent);
		}
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		log("Enter onPreferenceChange function.");

        	final String key = arg0.getKey();
		int value = Integer.parseInt(String.valueOf(arg1));
        	if (VOICE_CALL_REJECT_MODE_KEY.equals(key)) {
		    Settings.System.putInt(getContentResolver(), Settings.System.VOICE_CALL_REJECT_MODE, value);
	            mVoiceRejectSetting.setSummary(mCallRejectModeArray[value]);
                    mVoiceRejectList.setEnabled(value == 2);
		    if(value == 1){
		        showDialog(VOICE_CALL_ALL_NUMBERS); 
		    }
		}else if (VIDEO_CALL_REJECT_MODE_KEY.equals(key)){
		    Settings.System.putInt(getContentResolver(), Settings.System.VT_CALL_REJECT_MODE, value);
		    mVideoRejectSetting.setSummary(mCallRejectModeArray[value]);
                    mVideoRejectList.setEnabled(value == 2);
		    if(value == 1){
		        showDialog(VIDEO_CALL_ALL_NUMBERS); 
		    }
		}
		return true;
	}

        @Override
	public Dialog onCreateDialog(int id) {
    	    Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(android.R.string.dialog_alert_title);
	    builder.setIcon(android.R.drawable.ic_dialog_alert);
            AlertDialog alertDlg;
	    switch(id){
	    case VOICE_CALL_ALL_NUMBERS:
                builder.setMessage(getResources().getString(R.string.voice_call_all_numbers));
	        break;
	    case VIDEO_CALL_ALL_NUMBERS:
                builder.setMessage(getResources().getString(R.string.video_call_all_numbers));
	        break;
	    default:
		break;
	    }
            builder.setPositiveButton(android.R.string.yes, null);
            alertDlg = builder.create();
	    return alertDlg;
	}
}
