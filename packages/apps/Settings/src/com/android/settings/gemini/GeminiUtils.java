package com.android.settings.gemini;

import android.content.Context;
import android.util.Log;
import com.android.internal.telephony.Phone;
import android.provider.Telephony;
import com.android.settings.Utils;

import com.android.settings.R;

/**
 * Contains utility functions for getting framework resource
 */
public class GeminiUtils {


	static int getStatusResource(int state) {
		

		switch (state) {
		case Phone.SIM_INDICATOR_RADIOOFF:
			return com.mediatek.internal.R.drawable.sim_radio_off;
		case Phone.SIM_INDICATOR_LOCKED:
			return com.mediatek.internal.R.drawable.sim_locked;
		case Phone.SIM_INDICATOR_INVALID:
			return com.mediatek.internal.R.drawable.sim_invalid;
		case Phone.SIM_INDICATOR_SEARCHING:
			return com.mediatek.internal.R.drawable.sim_searching;
		case Phone.SIM_INDICATOR_ROAMING:
			return com.mediatek.internal.R.drawable.sim_roaming;
		case Phone.SIM_INDICATOR_CONNECTED:
			return com.mediatek.internal.R.drawable.sim_connected;
		case Phone.SIM_INDICATOR_ROAMINGCONNECTED:
			return com.mediatek.internal.R.drawable.sim_roaming_connected;
		default:
			return -1;
		}
	}
	
	static int getSimColorResource(int color) {
		
		if((color>=0)&&(color<=7)) {
			return Telephony.SIMBackgroundRes[color];
		} else {
			return -1;
		}

		
	}
	static int getNetworkMode(int mode) {
		
		int networkMode = 0;
		switch (mode) {

		case Phone.NT_MODE_WCDMA_ONLY:
			networkMode = Phone.NT_MODE_WCDMA_ONLY;
			break;
		case Phone.NT_MODE_GSM_ONLY:
			networkMode = Phone.NT_MODE_GSM_ONLY;
			break;
		case Phone.NT_MODE_WCDMA_PREF:
			networkMode = Phone.NT_MODE_WCDMA_PREF;
			break;
		default:
			networkMode = Phone.PREFERRED_NT_MODE;
		}
		
		return networkMode;
		
	}
	
	static final int TYPE_VOICECALL = 1;
	static final int TYPE_VIDEOCALL = 2;
	static final int TYPE_SMS = 3;
	static final int TYPE_GPRS = 4;
	
	static final boolean mNeed3GText = false;
	static int m3GSlotID = Phone.GEMINI_SIM_1;
	//MTK_OP02_PROTECT_START
    static final String OLD_NETWORK_MODE = "com.android.phone.OLD_NETWORK_MODE";
    static final String NEW_NETWORK_MODE = "NEW_NETWORK_MODE";

    static final String NETWORK_MODE_CHANGE_BROADCAST = "com.android.phone.NETWORK_MODE_CHANGE";
    static final String NETWORK_MODE_CHANGE_RESPONSE = "com.android.phone.NETWORK_MODE_CHANGE_RESPONSE";
	//MTK_OP02_PROTECT_END
	static final String EXTRA_SIMID = "simid";

}
