package com.android.phone;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.net.sip.SipManager;

public class AutoRejectSetting extends PreferenceActivity {

	private static final String LOG_TAG = "AutoRejectSetting";
	private static final boolean DBG = true;
	
	public final static String AUTO_REJECT_VOICE_CALL_KEY = "voice_call_auto_reject_key";
	public final static String AUTO_REJECT_VIDEO_CALL_KEY = "video_call_auto_reject_key";
	public final static String AUTO_REJECT_SIP_CALL_KEY = "sip_call_auto_reject_key";

	private static void log(String msg) {
		Log.d(LOG_TAG, msg);
	}

	@Override
	protected void onCreate(Bundle icicle) {

		if (DBG)
			log("onCreate!!");

		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.auto_reject_setting);

		if (!PhoneUtils.isVoipSupported()) {
			getPreferenceScreen().removePreference(
					findPreference("sip_call_auto_reject_key"));
		}

	}

	@Override
	protected void onResume() {
		if (DBG)
			log("onResume()...");
		super.onResume();

	}

	@Override
	public void onPause() {
		if (DBG)
			log("onPause()...");
		super.onPause();
	}

}
