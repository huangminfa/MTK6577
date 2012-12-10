/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.settings.deviceinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.os.SystemProperties;
import com.mediatek.featureoption.FeatureOption;
/**
 * USB storage settings.
 */
public class UsbSettings extends SettingsPreferenceFragment {

    private static final String TAG = "UsbSettings";

    private static final String KEY_UMS = "usb_ums";
    private static final String KEY_MTP = "usb_mtp";
    private static final String KEY_PTP = "usb_ptp";
    private static final String KEY_CHARGE = "usb_charge";

    private UsbManager mUsbManager;
    private CheckBoxPreference mMtp;
    private CheckBoxPreference mPtp;
    private CheckBoxPreference mUms;
    private CheckBoxPreference mCharge;
    
    private boolean mUmsExist = true;    
    private boolean mChargeExist = true;   

    //VIA-START VIA USB
    private static final String KEY_VIA_CDROM = "usb_via_cdrom";
    private CheckBoxPreference mViaCdrom;
    private boolean mViaCdromEjected = false;
    private final boolean mPCModeEnable =!SystemProperties.getBoolean("sys.usb.pcmodem.disable",false);
    //VIA-END VIA USB

    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context content, Intent intent) {
            boolean isHwUsbConnected = !intent.getBooleanExtra("USB_HW_DISCONNECTED", false);
            boolean isSwUsbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
            Log.d(TAG, "USB connection status hw : " + isHwUsbConnected + ", SW :" + isSwUsbConnected);

           //VIA-START VIA USB
            if(FeatureOption.EVDO_DT_VIA_SUPPORT == true){
            	boolean isViaCdromEjected = intent.getBooleanExtra("USB_VIA_CDROM_EJECTED", false);
                if(mViaCdromEjected!=isViaCdromEjected){
                    mViaCdromEjected = isViaCdromEjected;
                    //createPreferenceHierarchy();//never remove mViaCdrom
	        }
            }
            //VIA-END VIA USB

            if (isHwUsbConnected && getCurrentFuction().equals("charging")) {
            	updateToggles(getCurrentFuction());
            } else if (isHwUsbConnected && isSwUsbConnected) {
            	updateToggles(getCurrentFuction());
            } else {
            	finish();
            }            
        }
    };

    private String getCurrentFuction() {
    	 String functions = android.os.SystemProperties.get("sys.usb.config", "none");
    	 Log.d(TAG, "current function: " + functions);
    	 int commandIndex = functions.indexOf(',');
    	 if(commandIndex > 0) {
    		 return functions.substring(0, commandIndex);
    	 } else {
    		 return functions;
    	 }
    }
    
    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.usb_settings);
        root = getPreferenceScreen();

        mMtp = (CheckBoxPreference)root.findPreference(KEY_MTP);
        mPtp = (CheckBoxPreference)root.findPreference(KEY_PTP);

        mUms = (CheckBoxPreference)root.findPreference(KEY_UMS);
        
        mCharge = (CheckBoxPreference)root.findPreference(KEY_CHARGE);

        //VIA-START VIA USB
        mViaCdrom = (CheckBoxPreference)root.findPreference(KEY_VIA_CDROM);
        //VIA-END VIA USB

        String config = android.os.SystemProperties.get("ro.sys.usb.storage.type", "mtp");
        if(!config.equals(UsbManager.USB_FUNCTION_MTP + "," + UsbManager.USB_FUNCTION_MASS_STORAGE)) {
            root.removePreference(mUms);
            mUmsExist = false;
        }
        
        String chargeConfig = android.os.SystemProperties.get("ro.sys.usb.charging.only", "no");
        Log.d(TAG, "ro.sys.usb.charging.only: " + chargeConfig);
        if(chargeConfig.equals("no")) {
            Log.d(TAG, "Usb Charge does not exist!");
        	root.removePreference(mCharge);
            mChargeExist = false;
        }

        //VIA-START VIA USB
        if(FeatureOption.EVDO_DT_VIA_SUPPORT == true){
            if(!mPCModeEnable){
                root.removePreference(mViaCdrom);
            }
	} else {
            root.removePreference(mViaCdrom);
	}
	 //VIA-END VIA USB

        return root;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mStateReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure we reload the preference hierarchy since some of these settings
        // depend on others...
        createPreferenceHierarchy();

        // ACTION_USB_STATE is sticky so this will call updateToggles
        getActivity().registerReceiver(mStateReceiver,
                new IntentFilter(UsbManager.ACTION_USB_STATE));
    }

    private void updateToggles(String function) {
        if (UsbManager.USB_FUNCTION_MTP.equals(function)) {
            mMtp.setChecked(true);
            mPtp.setChecked(false);
            if(mUmsExist) {
            	mUms.setChecked(false);
            }
            if(mChargeExist) {
            	mCharge.setChecked(false);
            }
            //VIA-START VIA USB
            mViaCdrom.setChecked(false);
            //VIA-END VIA USB
        } else if (UsbManager.USB_FUNCTION_PTP.equals(function)) {
            mMtp.setChecked(false);
            mPtp.setChecked(true);
            if(mUmsExist) {
            	mUms.setChecked(false);
            }
            if(mChargeExist) {
            	mCharge.setChecked(false);
            }
            //VIA-START VIA USB
            mViaCdrom.setChecked(false);
            //VIA-END VIA USB
        } else if (UsbManager.USB_FUNCTION_MASS_STORAGE.equals(function)){
            mMtp.setChecked(false);
            mPtp.setChecked(false);
            if(mUmsExist) {
            	mUms.setChecked(true);
            }
            if(mChargeExist) {
            	mCharge.setChecked(false);
            }
            //VIA-START VIA USB
            mViaCdrom.setChecked(false);
            //VIA-END VIA USB
        } else if(UsbManager.USB_FUNCTION_CHARGING_ONLY.equals(function)) {
            mMtp.setChecked(false);
            mPtp.setChecked(false);
            if(mUmsExist) {
            	mUms.setChecked(false);
            }
            if(mChargeExist) {
            	mCharge.setChecked(true);
            }
            //VIA-START VIA USB
            mViaCdrom.setChecked(false);
            //VIA-END VIA USB
        } else {
            mMtp.setChecked(false);
            mPtp.setChecked(false);
            if(mUmsExist) {
            	mUms.setChecked(false);
            }
            if(mChargeExist) {
            	mCharge.setChecked(false);
            }
            //VIA-START VIA USB
            if(UsbManager.USB_FUNCTION_VIA_CDROM.equals(function)) {
                mViaCdrom.setChecked(true);
            }
            //VIA-END VIA USB
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        // Don't allow any changes to take effect as the USB host will be disconnected, killing
        // the monkeys
        if (Utils.isMonkeyRunning()) {
            return true;
        }
        // temporary hack - using check boxes as radio buttons
        // don't allow unchecking them
        if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference checkBox = (CheckBoxPreference)preference;
            Log.d(TAG, "" + checkBox.getTitle() + checkBox.isChecked());
            if (!checkBox.isChecked()) {
                checkBox.setChecked(true);
                return true;
            }
        }
        if (preference == mMtp) {
            mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, true);
            updateToggles(UsbManager.USB_FUNCTION_MTP);
        } else if (preference == mPtp) {
            mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_PTP, true);
            updateToggles(UsbManager.USB_FUNCTION_PTP);
        } else if(preference == mUms) {
            mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MASS_STORAGE, true);
            updateToggles(UsbManager.USB_FUNCTION_MASS_STORAGE);
        } else if(preference == mCharge) {
            mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_CHARGING_ONLY, false);
            updateToggles(UsbManager.USB_FUNCTION_CHARGING_ONLY);
        } else if(preference == mViaCdrom) {
            //VIA-START VIA USB
            if(FeatureOption.EVDO_DT_VIA_SUPPORT == true){
                mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_VIA_CDROM, false);
                updateToggles(UsbManager.USB_FUNCTION_VIA_CDROM);
            }
             //VIA-END VIA USB
        }
        return true;
    }
}
