/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager; 
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Switch;

import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.nfc.AndroidBeam;
import com.android.settings.nfc.NfcEnabler;
import com.mediatek.settings.nfc.MtkNfcEnabler;
import com.mediatek.settings.nfc.NfcPreference;
import com.mediatek.settings.nfc.NfcSettings;
import com.android.settings.wifi.p2p.WifiP2pEnabler;

import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;

import java.util.List;

public class WirelessSettings extends SettingsPreferenceFragment {

    private static final String TAG = "WirelessSettings";
    private static final String KEY_TOGGLE_AIRPLANE = "toggle_airplane";
    private static final String KEY_TOGGLE_NFC = "toggle_nfc";
    private static final String KEY_MTK_TOGGLE_NFC = "toggle_mtk_nfc";
    private static final String KEY_WIMAX_SETTINGS = "wimax_settings";
    private static final String KEY_ANDROID_BEAM_SETTINGS = "android_beam_settings";
    private static final String KEY_VPN_SETTINGS = "vpn_settings";
    private static final String KEY_TOGGLE_WIFI_P2P = "toggle_wifi_p2p";
    private static final String KEY_WIFI_P2P_SETTINGS = "wifi_p2p_settings";
    private static final String KEY_TETHER_SETTINGS = "tether_settings";
    private static final String KEY_PROXY_SETTINGS = "proxy_settings";
    private static final String KEY_MOBILE_NETWORK_SETTINGS = "mobile_network_settings";
    private static final String RCSE_SETTINGS_INTENT = "com.mediatek.rcse.RCSE_SETTINGS";
    private static final String KEY_RCSE_SETTINGS = "rcse_settings";

    public static final String EXIT_ECM_RESULT = "exit_ecm_result";
    public static final int REQUEST_CODE_EXIT_ECM = 1;

    private AirplaneModeEnabler mAirplaneModeEnabler;
    private CheckBoxPreference mAirplaneModePreference;
    private NfcEnabler mNfcEnabler;
    private MtkNfcEnabler mMtkNfcEnabler;
    private NfcAdapter mNfcAdapter;

    private WifiP2pEnabler mWifiP2pEnabler;
    private PreferenceScreen mNetworkSettingsPreference;
    
    private NfcPreference mNfcPreference;
    
    //Gemini phone instance
    //In order to do not run with phone process
    private ITelephony iTelephony;
    //Monitor PCH
    private static final String KEY_TOGGLE_PCH = "toggle_pch";
    private CheckBoxPreference mPCHPreference;
    public static final int PCH_DATA_PREFER = 0;
    public static final int PCH_CALL_PREFER = 1;

    /**
     * Invoked on each preference click in this hierarchy, overrides
     * PreferenceActivity's implementation.  Used to make sure we track the
     * preference click events.
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mAirplaneModePreference && Boolean.parseBoolean(
                SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
            // In ECM mode launch ECM app dialog
            startActivityForResult(
                new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null),
                REQUEST_CODE_EXIT_ECM);
            return true;
        }else if(preference == mPCHPreference){
        	if(iTelephony==null)
				iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
        	  if(mPCHPreference.isChecked()){
        	      mPCHPreference.setChecked(false);
				  new AlertDialog.Builder(getActivity())
				  .setTitle(android.R.string.dialog_alert_title)
				  .setCancelable(false)
				  .setIcon(android.R.drawable.ic_dialog_alert)
				  .setMessage(R.string.pch_warning_message)
				  .setPositiveButton(android.R.string.yes, new OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						if(AlertDialog.BUTTON_POSITIVE==which){
							mPCHPreference.setChecked(true);
							try {
								Settings.System.putInt(getContentResolver(), Settings.System.GPRS_TRANSFER_SETTING, 1);
								if(FeatureOption.MTK_GEMINI_SUPPORT){
									if(iTelephony != null){
	                                         iTelephony.setGprsTransferTypeGemini(PCH_CALL_PREFER, Phone.GEMINI_SIM_1);
                                             iTelephony.setGprsTransferTypeGemini(PCH_CALL_PREFER, Phone.GEMINI_SIM_2);
									}
								}
								else{
									if(iTelephony != null){
									        iTelephony.setGprsTransferType(PCH_CALL_PREFER);
									}
								}
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
					}
				  })
				  .setNegativeButton(android.R.string.no, new OnClickListener(){

						public void onClick(DialogInterface dialog, int which) {
							if(AlertDialog.BUTTON_NEGATIVE==which){
								mPCHPreference.setChecked(false);
							}
						}
					  })
				  .show();
			  }else {
				  try{
                        Settings.System.putInt(getContentResolver(), Settings.System.GPRS_TRANSFER_SETTING,0);
				        if(FeatureOption.MTK_GEMINI_SUPPORT){
				        	if(iTelephony != null){
						            iTelephony.setGprsTransferTypeGemini(PCH_DATA_PREFER, Phone.GEMINI_SIM_1);
						            iTelephony.setGprsTransferTypeGemini(PCH_DATA_PREFER, Phone.GEMINI_SIM_2);
				        	}
					    }
					   else{
						   if(iTelephony != null){
						            iTelephony.setGprsTransferType(PCH_DATA_PREFER);
						   }
					    }
				  }catch(RemoteException e) {
						e.printStackTrace();
					}
			  }
            return true;
        } else if (preference == mNfcPreference) {
            ((PreferenceActivity)getActivity()).startPreferencePanel(
                    NfcSettings.class.getName(), null, 0, null, null, 0);
        }
        // Let the intents be launched by the Preference manager
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public static boolean isRadioAllowed(Context context, String type) {
        if (!AirplaneModeEnabler.isAirplaneModeOn(context)) {
            return true;
        }
        // Here we use the same logic in onCreate().
        String toggleable = Settings.System.getString(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        return toggleable != null && toggleable.contains(type);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.wireless_settings);

        final Activity activity = getActivity();
        mAirplaneModePreference = (CheckBoxPreference) findPreference(KEY_TOGGLE_AIRPLANE);

        PreferenceScreen androidBeam = (PreferenceScreen) findPreference(KEY_ANDROID_BEAM_SETTINGS);
        mNfcPreference = (NfcPreference) findPreference(KEY_MTK_TOGGLE_NFC);
        CheckBoxPreference nfc = (CheckBoxPreference) findPreference(KEY_TOGGLE_NFC);
        
        mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);

        if (FeatureOption.MTK_NFC_ADDON_SUPPORT) {
            getPreferenceScreen().removePreference(nfc);
            mMtkNfcEnabler = new MtkNfcEnabler(activity, mNfcPreference, androidBeam, null, mNfcAdapter);
        } else {
            getPreferenceScreen().removePreference(mNfcPreference);
            mNfcEnabler = new NfcEnabler(activity, nfc, androidBeam);
        }
        
        // Remove NFC if its not available
        if (mNfcAdapter == null) {
            if (FeatureOption.MTK_NFC_ADDON_SUPPORT) {
                getPreferenceScreen().removePreference(mNfcPreference);
                mMtkNfcEnabler = null;
            } else {
                getPreferenceScreen().removePreference(nfc);
                mNfcEnabler = null;
            }
            getPreferenceScreen().removePreference(androidBeam);
        }
        
		mNetworkSettingsPreference = (PreferenceScreen) findPreference(KEY_MOBILE_NETWORK_SETTINGS);
		
        CheckBoxPreference wifiP2p = (CheckBoxPreference) findPreference(KEY_TOGGLE_WIFI_P2P);

        mAirplaneModeEnabler = new AirplaneModeEnabler(activity, mAirplaneModePreference);

        String toggleable = Settings.System.getString(activity.getContentResolver(),
                Settings.System.AIRPLANE_MODE_TOGGLEABLE_RADIOS);

          //enable/disable wimax depending on the value in config.xml
          boolean isWimaxEnabled = this.getResources().getBoolean(
                  com.android.internal.R.bool.config_wimaxEnabled);
	  Xlog.i(TAG, "isWimaxEnabled : " + isWimaxEnabled);
          if (!isWimaxEnabled) {
              PreferenceScreen root = getPreferenceScreen();
              Preference ps = (Preference) findPreference(KEY_WIMAX_SETTINGS);
              if (ps != null) root.removePreference(ps);
          } else {
              if (toggleable == null || !toggleable.contains(Settings.System.RADIO_WIMAX )
                      && isWimaxEnabled) {
		  Xlog.i(TAG, "ps is WIMAX ");
                  Preference ps = (Preference) findPreference(KEY_WIMAX_SETTINGS);
                  ps.setDependency(KEY_TOGGLE_AIRPLANE);
              }
          }
	

        // Manually set dependencies for Wifi when not toggleable.
        if (toggleable == null || !toggleable.contains(Settings.System.RADIO_WIFI)) {
            findPreference(KEY_VPN_SETTINGS).setDependency(KEY_TOGGLE_AIRPLANE);
        }

        // Manually set dependencies for Bluetooth when not toggleable.
        if (toggleable == null || !toggleable.contains(Settings.System.RADIO_BLUETOOTH)) {
            // No bluetooth-dependent items in the list. Code kept in case one is added later.
        }

        // Manually set dependencies for NFC when not toggleable.
        if (toggleable == null || !toggleable.contains(Settings.System.RADIO_NFC)) {
            findPreference(KEY_TOGGLE_NFC).setDependency(KEY_TOGGLE_AIRPLANE);
            findPreference(KEY_ANDROID_BEAM_SETTINGS).setDependency(KEY_TOGGLE_AIRPLANE);
        }

        // Remove Mobile Network Settings if it's a wifi-only device.
        if (Utils.isWifiOnly(getActivity())) {
            getPreferenceScreen().removePreference(mNetworkSettingsPreference);
        }

        WifiP2pManager p2p = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)||FeatureOption.MTK_WLAN_SUPPORT == false ||
                 (SystemProperties.getInt("ro.mediatek.wlan.p2p", 0) == 0)) {
            getPreferenceScreen().removePreference(wifiP2p);
        } else {
            mWifiP2pEnabler = new WifiP2pEnabler(activity, wifiP2p);
        }
        getPreferenceScreen().removePreference(findPreference(KEY_WIFI_P2P_SETTINGS));

        // Enable Proxy selector settings if allowed.
        Preference mGlobalProxy = findPreference(KEY_PROXY_SETTINGS);
        DevicePolicyManager mDPM = (DevicePolicyManager)
                activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // proxy UI disabled until we have better app support
        getPreferenceScreen().removePreference(mGlobalProxy);
        mGlobalProxy.setEnabled(mDPM.getGlobalProxyAdmin() == null);

        // Disable Tethering if it's not allowed or if it's a wifi-only device
        ConnectivityManager cm =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!cm.isTetheringSupported()) {
            getPreferenceScreen().removePreference(findPreference(KEY_TETHER_SETTINGS));
        } else {
            Preference p = findPreference(KEY_TETHER_SETTINGS);
            p.setTitle(Utils.getTetheringLabel(cm));
        }

	if (!(Utils.isWifiOnly(getActivity()))) {
            iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
            if(iTelephony != null){
                updateMobileNetworkEnabled();
            }

            mPCHPreference = new CheckBoxPreference(getActivity());
            mPCHPreference.setKey(KEY_TOGGLE_PCH);
            mPCHPreference.setTitle(R.string.pch_toggle_title);
            mPCHPreference.setSummaryOn(R.string.pch_call_prefer_summary_on);
            mPCHPreference.setSummaryOff(R.string.pch_data_prefer_summary_off);

            int pchFlag=Settings.System.getInt(this.getContentResolver(), Settings.System.GPRS_TRANSFER_SETTING, Settings.System.GPRS_TRANSFER_SETTING_DEFAULT);
            if(Utils.isVoiceCapable(getActivity())){
                this.getPreferenceScreen().addPreference(mPCHPreference);
                if(PCH_CALL_PREFER==pchFlag)
                    mPCHPreference.setChecked(true);
                else
                    mPCHPreference.setChecked(false);
            }else{
                pchFlag = 0;
            }
            Settings.System.putInt(this.getContentResolver(), Settings.System.GPRS_TRANSFER_SETTING, pchFlag);
	}
         //If rcse apk was not installed, then should hide rcse settings ui
        Intent intent = new Intent(RCSE_SETTINGS_INTENT);
        List<ResolveInfo> rcseApps = getPackageManager().queryIntentActivities(intent, 0 );
        if(rcseApps == null || rcseApps.size() == 0){
            Xlog.w(TAG, RCSE_SETTINGS_INTENT + " is not installed");
            getPreferenceScreen().removePreference(findPreference(KEY_RCSE_SETTINGS));
        }else{
            Xlog.w(TAG, RCSE_SETTINGS_INTENT + " is installed");
            findPreference(KEY_RCSE_SETTINGS).setIntent(intent);
        }
    }

    private void updateMobileNetworkEnabled(){
	    if(null == iTelephony){
            Xlog.e(TAG, "Could not get iTelephony object");
            return;
        }
        boolean Sim1Exist=true;
        boolean Sim2Exist=true;
		if(true == FeatureOption.MTK_GEMINI_SUPPORT){
	        boolean dualHasSim = true;
	        try{
	            Sim1Exist = iTelephony.isSimInsert(Phone.GEMINI_SIM_1);
	            Sim2Exist = iTelephony.isSimInsert(Phone.GEMINI_SIM_2);
	            dualHasSim = Sim1Exist || Sim2Exist;
	        }catch(RemoteException e){
	           Xlog.i(TAG, "RemoteException happens......");
	        }
            Xlog.i(TAG, "dualHasSim state: sim1exist?"+Sim1Exist+", sim2exist?"+Sim2Exist);
            mNetworkSettingsPreference.setEnabled(dualHasSim);
        } else {
            boolean hasSim = true;
            try{
                hasSim = iTelephony.isSimInsert(Phone.GEMINI_SIM_1);
            }catch(RemoteException e){
                Xlog.i(TAG, "RemoteException happens......");
            }
            Xlog.i(TAG, "single SIM version, hasSim?"+hasSim);
            mNetworkSettingsPreference.setEnabled(hasSim);
        }        
    }
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener(){
    	@Override
    	public void onCallStateChanged(int state, String incomingNumber) {
    		super.onCallStateChanged(state, incomingNumber);
    		Xlog.d(TAG, "PhoneStateListener, new state="+state);
    		switch(state){
    			case TelephonyManager.CALL_STATE_IDLE:
                    if(getActivity() != null){
        			    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        			    int currPhoneCallState = telephonyManager.getCallState();
        			    
        			    Xlog.d(TAG, "Total PhoneState ="+currPhoneCallState);
        				if(currPhoneCallState==TelephonyManager.CALL_STATE_IDLE){//only if both SIM are in call state, we will enable mobile network settings
        				    iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
                            updateMobileNetworkEnabled();
        				}
                    }
    				break;
    		} 
    	}
    };
    
        
    @Override
    public void onResume() {
        super.onResume();

        mAirplaneModeEnabler.resume();
        if (FeatureOption.MTK_NFC_ADDON_SUPPORT) {
            if (mMtkNfcEnabler != null) {
                mMtkNfcEnabler.resume();
            }
        } else {
            if (mNfcEnabler != null) {
                mNfcEnabler.resume();
            }
        }

        if (mWifiP2pEnabler != null) {
            mWifiP2pEnabler.resume();
        }
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        Xlog.d(TAG, "onResume(), call state="+ telephonyManager.getCallState());
        if(telephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE){
        	mNetworkSettingsPreference.setEnabled(false);
        }else{
        	mNetworkSettingsPreference.setEnabled(true);
        }        
    }

    @Override
    public void onPause() {
        super.onPause();

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        mAirplaneModeEnabler.pause();
        if (FeatureOption.MTK_NFC_ADDON_SUPPORT) {
            if (mMtkNfcEnabler != null) {
                mMtkNfcEnabler.pause();
            }
        } else {
            if (mNfcEnabler != null) {
                mNfcEnabler.pause();
            }
        }

        if (mWifiP2pEnabler != null) {
            mWifiP2pEnabler.pause();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EXIT_ECM) {
            Boolean isChoiceYes = data.getBooleanExtra(EXIT_ECM_RESULT, false);
            // Set Airplane mode based on the return value and checkbox state
            mAirplaneModeEnabler.setAirplaneModeInECM(isChoiceYes,
                    mAirplaneModePreference.isChecked());
        }
    }
}
