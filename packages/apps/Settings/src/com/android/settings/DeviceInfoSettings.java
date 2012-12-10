/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.internal.telephony.Phone;
import com.mediatek.featureoption.FeatureOption;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

public class DeviceInfoSettings extends SettingsPreferenceFragment {

    private static final String LOG_TAG = "DeviceInfoSettings";

    private static final String FILENAME_PROC_VERSION = "/proc/version";
    private static final String FILENAME_MSV = "/sys/board_properties/soc/msv";

    private static final String KEY_CONTAINER = "container";
    private static final String KEY_TEAM = "team";
    private static final String KEY_CONTRIBUTORS = "contributors";
    private static final String KEY_TERMS = "terms";
    private static final String KEY_LICENSE = "license";
    private static final String KEY_COPYRIGHT = "copyright";
    private static final String KEY_SYSTEM_UPDATE_SETTINGS = "system_update_settings";
    private static final String PROPERTY_URL_SAFETYLEGAL = "ro.url.safetylegal";
    private static final String KEY_KERNEL_VERSION = "kernel_version";
    private static final String KEY_BUILD_NUMBER = "build_number";
    private static final String KEY_DEVICE_MODEL = "device_model";
    private static final String KEY_BASEBAND_VERSION = "baseband_version";
    private static final String KEY_BASEBAND_VERSION_2 = "baseband_version_2";
    private static final String KEY_FIRMWARE_VERSION = "firmware_version";
    private static final String KEY_SCOMO= "scomo";
    private static final String KEY_UPDATE_SETTING = "additional_system_update_settings";

    private static final String KEY_DMSW_UPDATE = "software_update";
    private static final String KEY_SOFTWARE_UPDATE = "more_software_updates";
    //status info key
    private static final String KEY_STATUS_INFO = "status_info";
    private static final String KEY_STATUS_INFO_GEMINI = "status_info_gemini";
    //custom build version
    private static final String PROPERTY_BUILD_VERSION_CUSTOM = "ro.custom.build.version";

    long[] mHits = new long[3];

    //google ota info
    private static final String OTA_PREFERENCE = "googleota";
    private static final String OTA_PRE_DOWNLOAND_PERCENT = "downloadpercent";
    private static final String OTA_PRE_VER = "version";
    private static final String KEY_MOTA_UPDATE_SETTINGS = "mota_system_update";
    private Preference mPreference;
    private String mSummary = null;
    private String mSummaryF = null;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.device_info_settings);

        setStringSummary(KEY_FIRMWARE_VERSION, Build.VERSION.RELEASE);
        findPreference(KEY_FIRMWARE_VERSION).setEnabled(true);
        int modemSlot = getExternalModemSlot();
        String baseversion = "gsm.version.baseband";
        if (modemSlot == Phone.GEMINI_SIM_1) {
            baseversion = "gsm.version.baseband";
        } else {
            baseversion = "gsm.version.baseband.2";
        }
        Log.d(LOG_TAG,"baseversion=" + baseversion);
        setValueSummary(KEY_BASEBAND_VERSION, baseversion);
        if(FeatureOption.MTK_DT_SUPPORT == true) {
        	String version2;
            if (FeatureOption.EVDO_DT_SUPPORT == true) {
            	version2="cdma.version.baseband";
            } else {
            	version2="gsm.version.baseband.2";
            }
            Log.i(LOG_TAG,"version2="+version2);
            setValueSummary(KEY_BASEBAND_VERSION_2, version2);
            updateBasebandTitle();
        } else {
        	getPreferenceScreen().removePreference(findPreference(KEY_BASEBAND_VERSION_2));
        }
        setStringSummary(KEY_DEVICE_MODEL, Build.MODEL + getMsvSuffix());
        setStringSummary(KEY_BUILD_NUMBER, Build.DISPLAY);
        findPreference(KEY_KERNEL_VERSION).setSummary(getFormattedKernelVersion());
        setValueSummary("custom_build_version", PROPERTY_BUILD_VERSION_CUSTOM);

        // Remove Safety information preference if PROPERTY_URL_SAFETYLEGAL is not set
        removePreferenceIfPropertyMissing(getPreferenceScreen(), "safetylegal",
                PROPERTY_URL_SAFETYLEGAL);

        // Remove Baseband version if wifi-only device
        if (Utils.isWifiOnly(getActivity())) {
            getPreferenceScreen().removePreference(findPreference(KEY_BASEBAND_VERSION));
        }

        /*
         * Settings is a generic app and should not contain any device-specific
         * info.
         */
        final Activity act = getActivity();
        // These are contained in the "container" preference group
        PreferenceGroup parentPreference = (PreferenceGroup) findPreference(KEY_CONTAINER);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_TERMS,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_LICENSE,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_COPYRIGHT,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_TEAM,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);

        // These are contained by the root preference screen
        parentPreference = getPreferenceScreen();
        Log.i("GoogleOta","DeviceInfoSettings:Start");
        boolean hasSpecial = false;
        if (FeatureOption.MTK_GOOGLEOTA_SUPPORT) {
        	hasSpecial = updatePreferenceToSpecificActivity(act, parentPreference,
        			KEY_MOTA_UPDATE_SETTINGS);

        } else {
            Preference preference = parentPreference.findPreference(KEY_MOTA_UPDATE_SETTINGS);
            if (preference != null) {
                parentPreference.removePreference(preference);
            }
        }
        Log.i("GoogleOta","DeviceInfoSettings:Stop, hasSpecial = "+hasSpecial);

        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference,
                KEY_SYSTEM_UPDATE_SETTINGS,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_CONTRIBUTORS,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);

        // Read platform settings for additional system update setting
        boolean isUpdateSettingAvailable =
                getResources().getBoolean(R.bool.config_additional_system_update_setting_enable);
        if (isUpdateSettingAvailable == false) {
            getPreferenceScreen().removePreference(findPreference(KEY_UPDATE_SETTING));
        }
        // DM SCOMO
        if (FeatureOption.MTK_SCOMO_ENTRY == false) {
            Preference scomoPreference = findPreference(KEY_SCOMO);
            if (scomoPreference != null) {
                getPreferenceScreen().removePreference(scomoPreference);
            }
        }
        if(false == FeatureOption.MTK_GEMINI_SUPPORT) {
            //delete the Gemini preference if it is single sim
            parentPreference.removePreference(findPreference(KEY_STATUS_INFO_GEMINI));
            if(Utils.isCuLoad() && findPreference(KEY_STATUS_INFO)!=null) {
                findPreference(KEY_STATUS_INFO).setSummary("");
            }
        } else {
            //if it is Gemini, then delete the single preference
            parentPreference.removePreference(findPreference(KEY_STATUS_INFO));
            if(Utils.isCuLoad() && findPreference(KEY_STATUS_INFO_GEMINI)!=null) {
                findPreference(KEY_STATUS_INFO_GEMINI).setSummary("");
            }
        }
        
        if(false == FeatureOption.MTK_FOTA_ENTRY){
        	parentPreference.removePreference(findPreference(KEY_DMSW_UPDATE));
        }
        softwareUpdatePreference();
    }

    private void updateBasebandTitle() {
    	String basebandversion=getString(R.string.baseband_version);
    	String slot1;
    	String slot2;
    	if (FeatureOption.EVDO_DT_SUPPORT == true) {
    		Locale tr=Locale.getDefault();//For chinese there is no space
    		slot1="GSM "+basebandversion;
    		slot2="CDMA "+basebandversion;
    		if (tr.getCountry().equals(Locale.CHINA.getCountry()) || tr.getCountry().equals(Locale.TAIWAN.getCountry())){
    			slot1=slot1.replace("GSM ", "GSM");
    			slot2=slot2.replace("CDMA ", "CDMA");//delete the space
        	}
    	} else {
    		slot1=getString(R.string.status_imei_slot1);
        	slot1=basebandversion+slot1.replace(getString(R.string.status_imei), " ");
        	slot2=getString(R.string.status_imei_slot2);
        	slot2=basebandversion+slot2.replace(getString(R.string.status_imei), " ");
    	}
    	findPreference(KEY_BASEBAND_VERSION).setTitle(slot1);
    	findPreference(KEY_BASEBAND_VERSION_2).setTitle(slot2);
	}
    private int getExternalModemSlot() {
        int modemSlot;
        String md = SystemProperties.get("ril.external.md",
                    getResources().getString(R.string.device_info_default));
        if (md.equals(getResources().getString(R.string.device_info_default))) {
            modemSlot = Phone.GEMINI_SIM_1;
        } else {
            modemSlot = Integer.valueOf(md).intValue();
        }
        Log.d(LOG_TAG,"modemSlot="+modemSlot);
        return modemSlot;
    }
    private void softwareUpdatePreference() {
    	Log.i(LOG_TAG, "softwareUpdatePreference"+ 
    		  "FeatureOption.MTK_GOOGLEOTA_SUPPORT="+FeatureOption.MTK_GOOGLEOTA_SUPPORT
    		  +" FeatureOption.MTK_FOTA_ENTRY="+FeatureOption.MTK_FOTA_ENTRY
    		  +" FeatureOption.MTK_SCOMO_ENTRY="+FeatureOption.MTK_SCOMO_ENTRY);
    	PreferenceGroup parentPreference = getPreferenceScreen();
    	if (FeatureOption.MTK_GOOGLEOTA_SUPPORT && 
    			parentPreference.findPreference(KEY_MOTA_UPDATE_SETTINGS)!=null) {
    		
    		if(false == FeatureOption.MTK_FOTA_ENTRY && false == FeatureOption.MTK_SCOMO_ENTRY &&
    				parentPreference.findPreference(KEY_SYSTEM_UPDATE_SETTINGS)==null) {
    			Log.i(LOG_TAG, "Remove software updates item as no item available");
    			parentPreference.removePreference(findPreference(KEY_SOFTWARE_UPDATE));
    			return;
    		}
    		if (parentPreference.findPreference(KEY_DMSW_UPDATE)!=null) {
    			Log.i(LOG_TAG, "Remove fota");
    			parentPreference.removePreference(findPreference(KEY_DMSW_UPDATE));
    		}
    		if (parentPreference.findPreference(KEY_SCOMO)!=null) {
    			Log.i(LOG_TAG, "Remove scomo");
    			parentPreference.removePreference(findPreference(KEY_SCOMO));
    		}
    		if (parentPreference.findPreference(KEY_SYSTEM_UPDATE_SETTINGS)!=null) {
    			Log.i(LOG_TAG, "Remove GMS");
    			parentPreference.removePreference(findPreference(KEY_SYSTEM_UPDATE_SETTINGS));
    		}
    	}
    	else {
    		Log.i(LOG_TAG, "Remove software updates item");
    		parentPreference.removePreference(findPreference(KEY_SOFTWARE_UPDATE));
    	}
	}

    @Override
    public void onResume() {
        super.onResume();
        PreferenceGroup parentPreference = getPreferenceScreen();

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("GoogleOta","DeviceInfoSettings:onDestroy");
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(KEY_FIRMWARE_VERSION)) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
            mHits[mHits.length-1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis()-500)) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("android",
                        com.android.internal.app.PlatLogoActivity.class.getName());
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Unable to start activity " + intent.toString());
                }
            }
        } 
        else if(preference.getKey().equals(KEY_DMSW_UPDATE)) {
        	Intent i = new Intent();
        	i.setAction("com.mediatek.DMSWUPDATE");
        	getActivity().sendBroadcast(i);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void removePreferenceIfPropertyMissing(PreferenceGroup preferenceGroup,
            String preference, String property ) {
        if (SystemProperties.get(property).equals(""))
        {
            // Property is missing so remove preference from group
            try {
                preferenceGroup.removePreference(findPreference(preference));
            } catch (RuntimeException e) {
                Log.d(LOG_TAG, "Property '" + property + "' missing and no '"
                        + preference + "' preference");
            }
        }
    }

    private void setStringSummary(String preference, String value) {
        try {
            findPreference(preference).setSummary(value);
        } catch (RuntimeException e) {
            findPreference(preference).setSummary(
                getResources().getString(R.string.device_info_default));
        }
    }

    private void setValueSummary(String preference, String property) {
        try {
            findPreference(preference).setSummary(
                    SystemProperties.get(property,
                            getResources().getString(R.string.device_info_default)));
        } catch (RuntimeException e) {
            // No recovery
        }
    }

    /**
     * Reads a line from the specified file.
     * @param filename the file to read from
     * @return the first line, if any.
     * @throws IOException if the file couldn't be read
     */
    private String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    private String getFormattedKernelVersion() {
        String procVersionStr;

        try {
            procVersionStr = readLine(FILENAME_PROC_VERSION);

            final String PROC_VERSION_REGEX =
                "\\w+\\s+" + /* ignore: Linux */
                "\\w+\\s+" + /* ignore: version */
                "([^\\s]+)\\s+" + /* group 1: 2.6.22-omap1 */
                "\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" + /* group 2: (xxxxxx@xxxxx.constant) */
                "\\((?:[^(]*\\([^)]*\\))?[^)]*\\)\\s+" + /* ignore: (gcc ..) */
                "([^\\s]+)\\s+" + /* group 3: #26 */
                "(?:PREEMPT\\s+)?" + /* ignore: PREEMPT (optional) */
                "(.+)"; /* group 4: date */

            Pattern p = Pattern.compile(PROC_VERSION_REGEX);
            Matcher m = p.matcher(procVersionStr);

            if (!m.matches()) {
                Log.e(LOG_TAG, "Regex did not match on /proc/version: " + procVersionStr);
                return "Unavailable";
            } else if (m.groupCount() < 4) {
                Log.e(LOG_TAG, "Regex match on /proc/version only returned " + m.groupCount()
                        + " groups");
                return "Unavailable";
            } else {
                return (new StringBuilder(m.group(1)).append("\n").append(
                        m.group(2)).append(" ").append(m.group(3)).append("\n")
                        .append(m.group(4))).toString();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG,
                "IO Exception when getting kernel version for Device Info screen",
                e);

            return "Unavailable";
        }
    }
    private boolean updatePreferenceToSpecificActivity(Context context,
            PreferenceGroup parentPreferenceGroup, String preferenceKey) {

        Preference preference = parentPreferenceGroup.findPreference(preferenceKey);
        if (preference == null) {
            return false;
        }

        Intent intent = preference.getIntent();
        if (intent != null) {
            // Find the activity that is in the system image
            PackageManager pm = context.getPackageManager();
            Log.i("GoogleOta","DeviceInfoSettings:intent.getAction() = "+intent.getAction());
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            int listSize = list.size();
            Log.i("GoogleOta","DeviceInfoSettings:listSize = "+listSize);
            for (int i = 0; i < listSize; i++) {
                ResolveInfo resolveInfo = list.get(i);
                Log.i("GoogleOta","DeviceInfoSettings:updatePreferenceToSpecificActivity, resolveInfo.activityInfo.packageName = "+resolveInfo.activityInfo.packageName);
                boolean is = resolveInfo.activityInfo.name.equals("com.mediatek.GoogleOta.GoogleOtaClient");
                Log.i("GoogleOta","DeviceInfoSettings:is = "+is);
                if (!is) continue;
                // Replace the intent with this specific activity
                preference.setIntent(new Intent().setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name));

                // Set the preference title to the activity's label
                preference.setTitle(resolveInfo.loadLabel(pm));

                return true;
            }
        }
        parentPreferenceGroup.removePreference(preference);
        return false;
    }
    


    /**
     * Returns " (ENGINEERING)" if the msv file has a zero value, else returns "".
     * @return a string to append to the model number description.
     */
    private String getMsvSuffix() {
        // Production devices should have a non-zero value. If we can't read it, assume it's a
        // production device so that we don't accidentally show that it's an ENGINEERING device.
        try {
            String msv = readLine(FILENAME_MSV);
            // Parse as a hex number. If it evaluates to a zero, then it's an engineering build.
            if (Long.parseLong(msv, 16) == 0) {
                return " (ENGINEERING)";
            }
        } catch (IOException ioe) {
            // Fail quietly, as the file may not exist on some devices.
        } catch (NumberFormatException nfe) {
            // Fail quietly, returning empty string should be sufficient
        }
        return "";
    }
}
