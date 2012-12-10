/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.provider.Settings;
import android.util.Log;

import com.android.internal.telephony.TelephonyProperties;
import android.telephony.ServiceState;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;
import com.mediatek.featureoption.FeatureOption;

public class AirplaneModeEnabler implements Preference.OnPreferenceChangeListener {
	private static final String LOG_TAG = "AirplaneModeEnabler";
    private final Context mContext;
    private final CheckBoxPreference mCheckBoxPref;
    private TelephonyManager mTelephonyManager;
    
    private int mServiceState1 = ServiceState.STATE_POWER_OFF ;
    private int mServiceState2 = ServiceState.STATE_POWER_OFF ;

    public AirplaneModeEnabler(Context context, CheckBoxPreference airplaneModeCheckBoxPreference) {        
        mContext = context;
        mCheckBoxPref = airplaneModeCheckBoxPreference;
        mTelephonyManager = (TelephonyManager)context.getSystemService(
                Context.TELEPHONY_SERVICE);
        airplaneModeCheckBoxPreference.setPersistent(false);
    }

    public void resume() {
        mCheckBoxPref.setChecked(isAirplaneModeOn(mContext));        
    	// This is the widget enabled state, not the preference toggled state
        if(!Utils.isWifiOnly(mContext)){
            mCheckBoxPref.setEnabled(true);
            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                mTelephonyManager.listenGemini(mPhoneStateListener1,
                    PhoneStateListener.LISTEN_SERVICE_STATE,Phone.GEMINI_SIM_1);
                mTelephonyManager.listenGemini(mPhoneStateListener2,
                    PhoneStateListener.LISTEN_SERVICE_STATE,Phone.GEMINI_SIM_2);
            } else {
                mTelephonyManager.listen(mPhoneStateListener1,
                    PhoneStateListener.LISTEN_SERVICE_STATE);
            }
        }
		mCheckBoxPref.setOnPreferenceChangeListener(this);
    }
    
    PhoneStateListener mPhoneStateListener1 = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            Log.i(LOG_TAG, "PhoneStateListener1.onServiceStateChanged: serviceState="+serviceState);
            mServiceState1 = serviceState.getState();
            onAirplaneModeChanged();
        }            

    };

    PhoneStateListener mPhoneStateListener2 = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            Log.i(LOG_TAG, "PhoneStateListener2.onServiceStateChanged: serviceState="+serviceState);
            mServiceState2 = serviceState.getState();
            onAirplaneModeChanged();
        }                
    };
    
    public void pause() {
        mCheckBoxPref.setOnPreferenceChangeListener(null);
        if(!Utils.isWifiOnly(mContext)){
            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            	mTelephonyManager.listenGemini(mPhoneStateListener1, 
                        PhoneStateListener.LISTEN_NONE , Phone.GEMINI_SIM_1);

            	mTelephonyManager.listenGemini(mPhoneStateListener2, 
                		PhoneStateListener.LISTEN_NONE ,Phone.GEMINI_SIM_2);
            }
            else {
            	mTelephonyManager.listen(mPhoneStateListener1, PhoneStateListener.LISTEN_NONE );
            }
        }
    }

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    private void setAirplaneModeOn(boolean enabling) {
        // Change the system setting
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 
                                enabling ? 1 : 0);
        // Update the UI to reflect system setting
        if(!Utils.isWifiOnly(mContext)){
            mCheckBoxPref.setEnabled(false);
        }
        mCheckBoxPref.setChecked(enabling);

        // Post the intent
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enabling);
        mContext.sendBroadcast(intent);
    }

    /**
     * Called when we've received confirmation that the airplane mode was set.
     * TODO: We update the checkbox summary when we get notified
     * that mobile radio is powered up/down. We should not have dependency
     * on one radio alone. We need to do the following:
     * - handle the case of wifi/bluetooth failures
     * - mobile does not send failure notification, fail on timeout.
     */
    private void onAirplaneModeChanged() {
        boolean airplaneModeEnabled = isAirplaneModeOn(mContext);
        if (FeatureOption.MTK_GEMINI_SUPPORT != true) { 
            // [ALPS00127431]
            // When AirplaneMode On, make sure phone is radio off
            if (airplaneModeEnabled) {
                if (mServiceState1 != ServiceState.STATE_POWER_OFF) {
                	Log.d(LOG_TAG,"Unfinish! serviceState:" + mServiceState1);
                    return;
                }
            }
        } else {
        	 // [ALPS00225004]
            // When AirplaneMode On, make sure both phone1 and phone2 are radio off
            if (airplaneModeEnabled) {
                if (mServiceState1 != ServiceState.STATE_POWER_OFF ||
                	mServiceState2 != ServiceState.STATE_POWER_OFF) {
                	Log.d(LOG_TAG,"Unfinish! serviceState1:" + mServiceState1 + " serviceState2:" + mServiceState2);
                    return;
                }
            }            
        }
        Log.d(LOG_TAG,"Finish! airplaneModeEnabled:" + airplaneModeEnabled);
        mCheckBoxPref.setChecked(airplaneModeEnabled);     
   	    mCheckBoxPref.setEnabled(true);
    }
    
    /**
     * Called when someone clicks on the checkbox preference.
     */
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (Boolean.parseBoolean(
                    SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
            // In ECM mode, do not update database at this point
        } else {
            setAirplaneModeOn((Boolean) newValue);
        }
        return true;
    }

    public void setAirplaneModeInECM(boolean isECMExit, boolean isAirplaneModeOn) {
        if (isECMExit) {
            // update database based on the current checkbox state
            setAirplaneModeOn(isAirplaneModeOn);
        } else {
            // update summary
            if(!Utils.isWifiOnly(mContext)){
                onAirplaneModeChanged();
            }
        }
    }
}
