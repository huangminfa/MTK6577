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

package com.android.settings.wifi;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.mediatek.xlog.Xlog;

public class AdvancedWifiSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "AdvancedWifiSettings";
    private static final String KEY_MAC_ADDRESS = "mac_address";
    private static final String KEY_CURRENT_IP_ADDRESS = "current_ip_address";
    private static final String KEY_FREQUENCY_BAND = "frequency_band";
    private static final String KEY_NOTIFY_OPEN_NETWORKS = "notify_open_networks";
    private static final String KEY_SLEEP_POLICY = "sleep_policy";
    private static final String KEY_ENABLE_WIFI_WATCHDOG = "wifi_enable_watchdog_service";

    private static final String KEY_CONNECT_TYPE = "connect_type";
    private static final String KEY_PRIORITY_TYPE = "priority_type";
    private static final String KEY_PRIORITY_SETTINGS = "priority_settings";
    
    private static final String KEY_CURRENT_GATEWAY = "current_gateway";
    private static final String KEY_CURRENT_NETMASK = "current_netmask";

    // MTK_CTIA_START
    private static final String KEY_MTK_CTIA_DIALOG = "mtk_wifi_ctia_test";

    //specify whether settings will auto connect wifi 
    private static final String KEY_CONNECT_AP_TYPE = "connect_ap_type";

    private static final String KEY_SELECT_SSID_TYPE = "select_ssid_type";

    private static final int CTIA_TEST_DIALOG = 0;

    // MTK_CTIA_END


    private WifiManager mWifiManager;

    private ListPreference mConnectTypePref;
    private CheckBoxPreference mPriorityTypePref;
    private Preference mPrioritySettingPref;
    private CheckBoxPreference mConnectApTypePref;
    private ListPreference mSelectSsidTypePref;

    // MTK_CTIA_START
    private Preference mMtkCTIATest;
    private Dialog mMtkCTIADialog;
    // MTK_CTIA_END

    private int mCurOrientation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wifi_advanced_settings);
        mCurOrientation = this.getResources().getConfiguration().orientation;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    // MTK_CTIA_START
    @Override
    public Dialog onCreateDialog(int dialogId) {
        Dialog d = null;

        switch(dialogId) {
            case CTIA_TEST_DIALOG:
                d = new MtkCTIATestDialog(getActivity());
                break;
            default:
                break;
        }
        return d;
    }
    // MTK_CTIA_END

    @Override
    public void onResume() {
        super.onResume();
        initPreferences();
        refreshWifiInfo();

        ContentResolver contentResolver = getContentResolver();
//MTK_OP01_PROTECT_START         
        //CMCC WLAN customization
        if(Utils.isCmccLoad()){
            if(mConnectApTypePref!=null){
                mConnectApTypePref.setChecked(System.getInt(contentResolver, 
                        System.WIFI_CONNECT_AP_TYPE, System.WIFI_CONNECT_AP_TYPE_AUTO) == System.WIFI_CONNECT_AP_TYPE_AUTO);
            }
            if(mConnectTypePref!=null){
                int value = System.getInt(contentResolver,Settings.System.WIFI_CONNECT_TYPE, Settings.System.WIFI_CONNECT_TYPE_AUTO);
                mConnectTypePref.setValue(String.valueOf(value));
            }
            if(mSelectSsidTypePref!=null){
                int value = System.getInt(contentResolver,Settings.System.WIFI_SELECT_SSID_TYPE, Settings.System.WIFI_SELECT_SSID_ASK);
                mSelectSsidTypePref.setValue(String.valueOf(value));
            }
            if(mPriorityTypePref!=null){
                mPriorityTypePref.setChecked(System.getInt(contentResolver, 
                        System.WIFI_PRIORITY_TYPE, System.WIFI_PRIORITY_TYPE_DEFAULT) == System.WIFI_PRIORITY_TYPE_MAMUAL);
            }
        }
//MTK_OP01_PROTECT_END 
    }

    // MTK_CTIA_START
    @Override
    public void onPause() {
        super.onPause();
        removeDialog(CTIA_TEST_DIALOG);
    }
    // MTK_CTIA_END

    private void initPreferences() {
        CheckBoxPreference notifyOpenNetworks =
            (CheckBoxPreference) findPreference(KEY_NOTIFY_OPEN_NETWORKS);
        notifyOpenNetworks.setChecked(Secure.getInt(getContentResolver(),
                Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 0) == 1);
        notifyOpenNetworks.setEnabled(mWifiManager.isWifiEnabled());

        CheckBoxPreference watchdogEnabled =
            (CheckBoxPreference) findPreference(KEY_ENABLE_WIFI_WATCHDOG);
        if (watchdogEnabled != null) {
            watchdogEnabled.setChecked(Secure.getInt(getContentResolver(),
                        Secure.WIFI_WATCHDOG_ON, 1) == 1);

            //TODO: Bring this back after changing watchdog behavior
            getPreferenceScreen().removePreference(watchdogEnabled);
        }

        ListPreference frequencyPref = (ListPreference) findPreference(KEY_FREQUENCY_BAND);

        if (mWifiManager.isDualBandSupported()) {
            frequencyPref.setOnPreferenceChangeListener(this);
            int value = mWifiManager.getFrequencyBand();
            if (value != -1) {
                frequencyPref.setValue(String.valueOf(value));
            } else {
                Xlog.d(TAG, "Failed to fetch frequency band");
            }
        } else {
            if (frequencyPref != null) {
                // null if it has already been removed before resume
                getPreferenceScreen().removePreference(frequencyPref);
            }
        }

        ListPreference sleepPolicyPref = (ListPreference) findPreference(KEY_SLEEP_POLICY);
        if (sleepPolicyPref != null) {
            if (Utils.isWifiOnly(getActivity())) {
                sleepPolicyPref.setEntries(R.array.wifi_sleep_policy_entries_wifi_only);
            }
            sleepPolicyPref.setOnPreferenceChangeListener(this);
            int value;
            if(Utils.isCmccLoad()){
                value = Settings.System.getInt(getContentResolver(),
                    Settings.System.WIFI_SLEEP_POLICY,
                    Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
            }else{
                value = Settings.System.getInt(getContentResolver(),
                    Settings.System.WIFI_SLEEP_POLICY,
                    Settings.System.WIFI_SLEEP_POLICY_NEVER);
            }
            String stringValue = String.valueOf(value);
            sleepPolicyPref.setValue(stringValue);
            updateSleepPolicySummary(sleepPolicyPref, stringValue);
        }

        mConnectTypePref = (ListPreference)findPreference(KEY_CONNECT_TYPE);
        mPriorityTypePref = (CheckBoxPreference)findPreference(KEY_PRIORITY_TYPE);
        mPrioritySettingPref = findPreference(KEY_PRIORITY_SETTINGS);
        mConnectApTypePref = (CheckBoxPreference)findPreference(KEY_CONNECT_AP_TYPE);
        mSelectSsidTypePref = (ListPreference)findPreference(KEY_SELECT_SSID_TYPE);
        if(mConnectTypePref!=null && mPriorityTypePref!=null && mConnectApTypePref!=null 
                && mSelectSsidTypePref!=null && mPrioritySettingPref != null){
//MTK_OP01_PROTECT_START 
            if(Utils.isCmccLoad()){
                mConnectTypePref.setOnPreferenceChangeListener(this);
                mPriorityTypePref.setOnPreferenceChangeListener(this);
                mConnectApTypePref.setOnPreferenceChangeListener(this);
                mSelectSsidTypePref.setOnPreferenceChangeListener(this);
            }else{
//MTK_OP01_PROTECT_END 
                getPreferenceScreen().removePreference(mConnectApTypePref);
                getPreferenceScreen().removePreference(mConnectTypePref);
                getPreferenceScreen().removePreference(mSelectSsidTypePref);
                getPreferenceScreen().removePreference(mPriorityTypePref);
                getPreferenceScreen().removePreference(mPrioritySettingPref);
//MTK_OP01_PROTECT_START 
            }
//MTK_OP01_PROTECT_END 
        }else{
            Xlog.d(TAG, "Fail to get mAutoConnectPref and mPriorityTypePref");
        }

        // MTK_CTIA_START
        mMtkCTIATest = findPreference("mtk_wifi_ctia_test");
        if (SystemProperties.getInt("mediatek.wlan.ctia", 0) == 0) {
            if (mMtkCTIATest != null) {
                getPreferenceScreen().removePreference(mMtkCTIATest);
            }
        }
        // MTK_CTIA_END
    }

    private void updateSleepPolicySummary(Preference sleepPolicyPref, String value) {
        if (value != null) {
            String[] values = getResources().getStringArray(R.array.wifi_sleep_policy_values);
            final int summaryArrayResId = Utils.isWifiOnly(getActivity()) ?
                    R.array.wifi_sleep_policy_entries_wifi_only : R.array.wifi_sleep_policy_entries;
            String[] summaries = getResources().getStringArray(summaryArrayResId);
            for (int i = 0; i < values.length; i++) {
                if (value.equals(values[i])) {
                    if (i < summaries.length) {
                        sleepPolicyPref.setSummary(summaries[i]);
                        return;
                    }
                }
            }
        }

        sleepPolicyPref.setSummary("");
        Xlog.d(TAG, "Invalid sleep policy value: " + value);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        String key = preference.getKey();
        // MTK_CTIA_START
        if (KEY_MTK_CTIA_DIALOG.equals(key)) {
            removeDialog(CTIA_TEST_DIALOG);
            showDialog(CTIA_TEST_DIALOG);
        }
        // MTK_CTIA_END

        else if (KEY_NOTIFY_OPEN_NETWORKS.equals(key)) {
            Secure.putInt(getContentResolver(),
                    Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
        } else if (KEY_ENABLE_WIFI_WATCHDOG.equals(key)) {
            Secure.putInt(getContentResolver(),
                    Secure.WIFI_WATCHDOG_ON,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
        } else {
            return super.onPreferenceTreeClick(screen, preference);
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        if (KEY_FREQUENCY_BAND.equals(key)) {
            try {
                mWifiManager.setFrequencyBand(Integer.parseInt((String) newValue), true);
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), R.string.wifi_setting_frequency_band_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (KEY_SLEEP_POLICY.equals(key)) {
            try {
                String stringValue = (String) newValue;
                Settings.System.putInt(getContentResolver(), Settings.System.WIFI_SLEEP_POLICY,
                        Integer.parseInt(stringValue));
                updateSleepPolicySummary(preference, stringValue);
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), R.string.wifi_setting_sleep_policy_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if(key.equals(KEY_CONNECT_AP_TYPE)){
            boolean checked = ((Boolean) newValue).booleanValue();
            Settings.System.putInt(getContentResolver(), Settings.System.WIFI_CONNECT_AP_TYPE, 
                    checked?Settings.System.WIFI_CONNECT_AP_TYPE_AUTO:Settings.System.WIFI_CONNECT_AP_TYPE_MANUL);
        } else if(key.equals(KEY_CONNECT_TYPE)){
            Xlog.d(TAG, "Wifi connect type is " + newValue);
            try{
                Settings.System.putInt(getContentResolver(),
                        Settings.System.WIFI_CONNECT_TYPE, Integer.parseInt(((String) newValue)));
            }catch (NumberFormatException e) {
                Toast.makeText(getActivity(), R.string.wifi_setting_connect_type_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if(key.equals(KEY_SELECT_SSID_TYPE)){
            try{
                Settings.System.putInt(getContentResolver(),
                        Settings.System.WIFI_SELECT_SSID_TYPE, Integer.parseInt(((String) newValue)));
            }catch (NumberFormatException e) {
                return false;
            }
        } else if(key.equals(KEY_PRIORITY_TYPE)){
            boolean checked = ((Boolean) newValue).booleanValue();
            Settings.System.putInt(getContentResolver(), Settings.System.WIFI_PRIORITY_TYPE, 
                    checked?Settings.System.WIFI_PRIORITY_TYPE_MAMUAL:Settings.System.WIFI_PRIORITY_TYPE_DEFAULT);
        } 

        return true;
    }

    private void refreshWifiInfo() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

        Preference wifiMacAddressPref = findPreference(KEY_MAC_ADDRESS);
        String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
        wifiMacAddressPref.setSummary(!TextUtils.isEmpty(macAddress) ? macAddress
                : getActivity().getString(R.string.status_unavailable));

        Preference wifiIpAddressPref = findPreference(KEY_CURRENT_IP_ADDRESS);
        String ipAddress = Utils.getWifiIpAddresses(getActivity());
        wifiIpAddressPref.setSummary(ipAddress == null ?
                getActivity().getString(R.string.status_unavailable) : ipAddress);

        Preference wifiGatewayPref = findPreference(KEY_CURRENT_GATEWAY);
        Preference wifiNetmaskPref = findPreference(KEY_CURRENT_NETMASK);
        String gateway = null;
        String netmask = null;
//MTK_OP01_PROTECT_START 
        if(Utils.isCmccLoad()){
            DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
            if (wifiInfo != null) {
                if(dhcpInfo!=null){
                    gateway = ipTransfer(dhcpInfo.gateway);
                    netmask = ipTransfer(dhcpInfo.netmask);
                }
            }
            if(wifiGatewayPref!=null){
                wifiGatewayPref.setSummary(gateway == null ?
                        getString(R.string.status_unavailable) : gateway);
            }
            if(wifiNetmaskPref!=null){
                wifiNetmaskPref.setSummary(netmask == null ?
                        getString(R.string.status_unavailable) : netmask);
            }
        }else
//MTK_OP01_PROTECT_END
        {
            PreferenceScreen screen = getPreferenceScreen();
            if(screen!=null){
                if(wifiGatewayPref!=null){
                    screen.removePreference(wifiGatewayPref);
                }
                if(wifiNetmaskPref!=null){
                    screen.removePreference(wifiNetmaskPref);
                }
            }
        }
    }
    private String ipTransfer(int value){
        String result = null;
        if(value!=0){
            if (value < 0) value += 0x100000000L;
            result = String.format("%d.%d.%d.%d",
                    value & 0xFF, (value >> 8) & 0xFF, (value >> 16) & 0xFF, (value >> 24) & 0xFF);
        }
        return result;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Xlog.d(TAG, "onConfigurationChanged: newConfig = " + newConfig
                + ",mCurOrientation = " + mCurOrientation + ",this = " + this);        
        super.onConfigurationChanged(newConfig);
        if (newConfig != null && newConfig.orientation != mCurOrientation) {
            mCurOrientation = newConfig.orientation;
        }
        this.getListView().clearScrapViewsIfNeeded();
    }
}
