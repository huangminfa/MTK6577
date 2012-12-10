/*
 * Copyright (C) 2010 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.browser.preferences;

import com.android.browser.BrowserActivity;
import com.android.browser.BrowserSettings;
import com.android.browser.PreferenceKeys;
import com.android.browser.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebStorage;

import java.util.Map;
import java.util.Set;

import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import com.mediatek.xlog.Xlog;

public class AdvancedPreferencesFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {
	
    //M: Add TAG for XLOG
    private static final String XLOG = "browser/AdvancedPreferencesFragment";
	
    //M: Add download path setting for OP01 integrated with File Manager
    private final static String DEFAULT_DOWNLOAD_DIRECTORY_OP01 = "/mnt/sdcard/MyFavorite";
    private final static String FILEMANAGER_EXTRA_NAME = "download path";
    private final static int RESULT_CODE_START_FILEMANAGER = 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the XML preferences file
        String optr_02 = SystemProperties.get("ro.operator.optr");
        if (optr_02 !=null && optr_02.equals("OP01")) {
        	addPreferencesFromResource(R.xml.advanced_preferences_op01);
        } else {
        	addPreferencesFromResource(R.xml.advanced_preferences);
        }

        PreferenceScreen searchEngineSettings = (PreferenceScreen) findPreference(
                BrowserSettings.PREF_SEARCH_ENGINE);
        searchEngineSettings.setFragment(SearchEngineSettings.class.getName());

        PreferenceScreen websiteSettings = (PreferenceScreen) findPreference(
                PreferenceKeys.PREF_WEBSITE_SETTINGS);
        websiteSettings.setFragment(WebsiteSettingsFragment.class.getName());

        Preference e = findPreference(PreferenceKeys.PREF_DEFAULT_ZOOM);
        e.setOnPreferenceChangeListener(this);
        e.setSummary(getVisualDefaultZoomName(
                getPreferenceScreen().getSharedPreferences()
                .getString(PreferenceKeys.PREF_DEFAULT_ZOOM, null)) );

        e = findPreference(PreferenceKeys.PREF_DEFAULT_TEXT_ENCODING);
        // Add UTF-16 and GB2312 text encoding for op02
        String optr = SystemProperties.get("ro.operator.optr");
        if (optr !=null && optr.equals("OP02") && (e != null)) {
            ((ListPreference) e).setEntries(R.array.pref_op02_text_encoding_choices);
            ((ListPreference) e).setEntryValues(R.array.pref_op02_text_encoding_values);
        }
        String encoding = getPreferenceScreen().getSharedPreferences()
            .getString(PreferenceKeys.PREF_DEFAULT_TEXT_ENCODING, "");
    	if (encoding != null && encoding.length() != 0 && encoding.equals("auto-detector")) {
    		encoding = getActivity().getString(R.string.pref_default_text_encoding_default);
    	}
    	e.setSummary(encoding);
        e.setOnPreferenceChangeListener(this);

        e = findPreference(PreferenceKeys.PREF_RESET_DEFAULT_PREFERENCES);
        e.setOnPreferenceChangeListener(this);

        e = findPreference(PreferenceKeys.PREF_SEARCH_ENGINE);
        e.setOnPreferenceChangeListener(this);
//        updateListPreferenceSummary((ListPreference) e);

        e = findPreference(PreferenceKeys.PREF_PLUGIN_STATE);
        e.setOnPreferenceChangeListener(this);
        updateListPreferenceSummary((ListPreference) e);
        
        //M: Add download path setting for OP01 integrated with File Manager
        PackageManager pm = this.getActivity().getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo("com.mediatek.filemanager", PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                // MTK_OP01_PROTECT_START
                String optr_01 = SystemProperties.get("ro.operator.optr");
                if (optr_01 != null && optr_01.equals("OP01")) {
                    addPreferencesFromResource(R.xml.download_preferences);
                    PreferenceScreen downloadDirectorySetting = (PreferenceScreen)findPreference(PreferenceKeys.PREF_DOWNLOAD_DIRECTORY_SETTING);
                    downloadDirectorySetting.setOnPreferenceClickListener(clickDownloadDirectorySetting());
                    
                    // Add for multi SDcard storage
                    String downloadDir = downloadDirectorySetting.getSharedPreferences().
                        getString(PreferenceKeys.PREF_DOWNLOAD_DIRECTORY_SETTING, 
                                BrowserSettings.getInstance().getDefaultDownloadPathWithMultiSDcard());
                    downloadDirectorySetting.setSummary(downloadDir);
                }
                // MTK_OP01_PROTECT_END
            } else {
                Xlog.e(XLOG, "FileManager does not exist!");
            }
        } catch (NameNotFoundException exception) {
        	Xlog.e(XLOG, "FileManager does not exist!");
        }
        
    }
    
    /*M:
     * This function is used to OP01 customization and integrate with FileManger
     * If user click the download directory setting, this function will start 
     * FileManager activity for download path.
     */
    private Preference.OnPreferenceClickListener clickDownloadDirectorySetting(){
        return new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // TODO Auto-generated method stub
                Intent i = new Intent("com.mediatek.filemanager.DOWNLOAD_LOCATION");
                String selectedPath = preference.getSharedPreferences().
                    getString(PreferenceKeys.PREF_DOWNLOAD_DIRECTORY_SETTING, 
                            BrowserSettings.getInstance().getDefaultDownloadPathWithMultiSDcard());
                i.putExtra(FILEMANAGER_EXTRA_NAME, selectedPath);
                AdvancedPreferencesFragment.this.startActivityForResult(i, RESULT_CODE_START_FILEMANAGER);
                Xlog.d(XLOG, "DownloadPathSetting preferecce is clicked.");
                return true;
            }
        };	
    }
    
    /*M:
     * Add this function for OP01 and integrate with FileManger.
     * This function will receive the download path which is set by FileManager
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == RESULT_CODE_START_FILEMANAGER) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null){
                    String downloadPath = extras.getString(FILEMANAGER_EXTRA_NAME);
                    if (downloadPath != null) {
                        // While get the download path, set it to preference
                        PreferenceScreen downloadDirectorySetting = (PreferenceScreen)findPreference(PreferenceKeys.PREF_DOWNLOAD_DIRECTORY_SETTING);
                        Editor ed = downloadDirectorySetting.getEditor();
                        ed.putString(PreferenceKeys.PREF_DOWNLOAD_DIRECTORY_SETTING, downloadPath);
                        ed.apply();
                        // Set preference summary
                        downloadDirectorySetting.setSummary(downloadPath);
                    }
                    Xlog.d(XLOG, "DownloadPathSetting get path form FileManger is:" + downloadPath);
                    return;
                }
            } 
        } 
        return;
    }

    void updateListPreferenceSummary(ListPreference e) {
        e.setSummary(e.getEntry());
    }

    /*
     * We need to set the PreferenceScreen state in onResume(), as the number of
     * origins with active features (WebStorage, Geolocation etc) could have
     * changed after calling the WebsiteSettingsActivity.
     */
    @Override
    public void onResume() {
        super.onResume();
        final PreferenceScreen websiteSettings = (PreferenceScreen) findPreference(
                PreferenceKeys.PREF_WEBSITE_SETTINGS);
        websiteSettings.setEnabled(false);
        WebStorage.getInstance().getOrigins(new ValueCallback<Map>() {
            @Override
            public void onReceiveValue(Map webStorageOrigins) {
                if ((webStorageOrigins != null) && !webStorageOrigins.isEmpty()) {
                    websiteSettings.setEnabled(true);
                }
            }
        });
        GeolocationPermissions.getInstance().getOrigins(new ValueCallback<Set<String> >() {
            @Override
            public void onReceiveValue(Set<String> geolocationOrigins) {
                if ((geolocationOrigins != null) && !geolocationOrigins.isEmpty()) {
                    websiteSettings.setEnabled(true);
                }
            }
        });
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object objValue) {
        if (getActivity() == null) {
            // We aren't attached, so don't accept preferences changes from the
            // invisible UI.
            Log.w("PageContentPreferencesFragment", "onPreferenceChange called from detached fragment!");
            return false;
        }

        if (pref.getKey().equals(PreferenceKeys.PREF_DEFAULT_ZOOM)) {
            pref.setSummary(getVisualDefaultZoomName((String) objValue));
            return true;
        } else if (pref.getKey().equals(PreferenceKeys.PREF_DEFAULT_TEXT_ENCODING)) {
        	String encoding = (String) objValue;
        	if (encoding != null && encoding.length() != 0 && encoding.equals("auto-detector")) {
        		encoding = getActivity().getString(R.string.pref_default_text_encoding_default);
        	}
        	pref.setSummary(encoding);
            return true;
        } else if (pref.getKey().equals(PreferenceKeys.PREF_RESET_DEFAULT_PREFERENCES)) {
            Boolean value = (Boolean) objValue;
            if (value.booleanValue() == true) {
                startActivity(new Intent(BrowserActivity.ACTION_RESTART, null,
                        getActivity(), BrowserActivity.class));
                return true;
            }
        } else if (pref.getKey().equals(PreferenceKeys.PREF_PLUGIN_STATE)
                || pref.getKey().equals(PreferenceKeys.PREF_SEARCH_ENGINE)) {
            ListPreference lp = (ListPreference) pref;
            lp.setValue((String) objValue);
            updateListPreferenceSummary(lp);
            return false;
        }
        return false;
    }

    private CharSequence getVisualDefaultZoomName(String enumName) {
        Resources res = getActivity().getResources();
        CharSequence[] visualNames = res.getTextArray(R.array.pref_default_zoom_choices);
        CharSequence[] enumNames = res.getTextArray(R.array.pref_default_zoom_values);

        // Sanity check
        if (visualNames.length != enumNames.length) {
            return "";
        }

        int length = enumNames.length;
        for (int i = 0; i < length; i++) {
            if (enumNames[i].equals(enumName)) {
                return visualNames[i];
            }
        }

        return "";
    }
}