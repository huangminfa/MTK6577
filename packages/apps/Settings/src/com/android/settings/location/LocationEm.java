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
 * limitations under the License.
 */
package com.android.settings.location;

import com.android.settings.R;
import android.preference.PreferenceCategory;

import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.provider.Settings;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.widget.Toast;
import android.net.wifi.WifiManager;
import com.mediatek.xlog.Xlog;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
//import com.android.settings.location.CellQueryActivity;
/**
 * LocationEm is the main UI interface for User which helps to do some of
 * configurations of Location Service {@hide}
 */
public class LocationEm extends PreferenceActivity implements Preference.OnPreferenceChangeListener
{
	private String TAG = "LocationEm";
	private LocationProfileManager m_LocationProfileManager = null;
	public ListPreference listBindNetworkService = null;
	public ListPreference listBindGeocoderService = null;
	private PreferenceScreen listUseLimite = null;
	//private PreferenceScreen listCellQuery = null;
	private ListPreference listLocationFrequency = null;
	private CheckBoxPreference checkboxAutoTest = null;
	private static final String LOCATIONEMINTENTACTIONNAME_LOCATIONFREQUENCY = "com.android.mediatek.locationem.locationfrequency";
	private static final String LOCATIONEMINTENTACTIONNAME_AUTO_WIFI = "com.android.mediatek.locationem.autowifi";
	private static final String LOCATIONEMINTENTACTIONNAME_AUTO_TEST = "com.android.mediatek.locationem.autotest";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (LocationEmDebugControl.DEBUG_LOCATIONEM)
		{
			Xlog.d(TAG, "onCreate() is called");
		}
		m_LocationProfileManager = new LocationProfileManager(this);
		m_LocationProfileManager.init();
		createPreference();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (LocationEmDebugControl.DEBUG_LOCATIONEM)
		{
			Xlog.d(TAG, "onPause() is called");
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (LocationEmDebugControl.DEBUG_LOCATIONEM)
		{
			Xlog.d(TAG, "onResume() is called");
		}
	}

	private void createPreference()
	{
		if (LocationEmDebugControl.DEBUG_LOCATIONEM)
		{
			Xlog.d(TAG, "createPreference() is called");
		}
		addPreferencesFromResource(R.layout.location_settings_eg);
		PreferenceCategory NetLocation = (PreferenceCategory) findPreference(this.getResources().getString(R.string.NetworkLocationSettings));
		listBindNetworkService = (ListPreference) findPreference(this.getResources().getString(R.string.networklocation_bindservice));
		if (listBindNetworkService != null)
		{
			String networkLocationDefaultValue = (String) m_LocationProfileManager.getSettingContent(LocationProfileManager.BIND_NETWORK_SERVICE_NAME);
			Xlog.d(TAG, "networkLocationDefaultValue: " + networkLocationDefaultValue);
			int pos = -1;
			if (networkLocationDefaultValue != null)
			{
				pos = listBindNetworkService.findIndexOfValue(networkLocationDefaultValue);
			}
			if (pos != -1)
			{
				listBindNetworkService.setValueIndex(pos);
			}
			listBindNetworkService.setSummary(m_LocationProfileManager.getNetworkLocationServiceBindAbstract());
			listBindNetworkService.setOnPreferenceChangeListener(this);
		}
		listBindGeocoderService = (ListPreference) findPreference(this.getResources().getString(R.string.geocoder_bindservice));
		if (listBindGeocoderService != null)
		{
			String geocoderDefaultValue = (String) m_LocationProfileManager.getSettingContent(LocationProfileManager.BIND_GEOCODER_SERVICE_NAME);
			int pos = -1;
			if (geocoderDefaultValue != null)
			{
				pos = listBindGeocoderService.findIndexOfValue(geocoderDefaultValue);
			}
			if (pos != -1)
			{
				listBindGeocoderService.setValueIndex(pos);
			}
			listBindGeocoderService.setSummary(m_LocationProfileManager.getGeocoderServiceBindAbstract());
			listBindGeocoderService.setOnPreferenceChangeListener(this);
		}
		
///////////////////
		listUseLimite = (PreferenceScreen) findPreference(this.getResources().getString(R.string.use_limit));
		//listCellQuery = (PreferenceScreen) findPreference(this.getResources().getString(R.string.cell_query));
		///////////////////
		listLocationFrequency = (ListPreference) findPreference(this.getResources().getString(R.string.networklocation_reportfrequency));
		checkboxAutoTest = (CheckBoxPreference) findPreference(this.getResources().getString(R.string.networklocation_testmodel));
		if (m_LocationProfileManager.IsMediaTekLocationServiceBinding())
		{
			if (LocationEmDebugControl.DEBUG_LOCATIONEM)
			{
				Xlog.d(TAG, "extra em configuration is created");
			}
			if (listLocationFrequency != null)
			{
				Integer frequencyDefaultValue = (Integer) m_LocationProfileManager.getSettingContent(LocationProfileManager.LOCATION_FREQUENCY);
				int pos = -1;
				if (frequencyDefaultValue != null)
				{
					pos = listLocationFrequency.findIndexOfValue(Integer.toString(frequencyDefaultValue));
				}
				if (pos != -1)
				{
					listLocationFrequency.setValueIndex(pos);
					StringBuilder localStringBuilder = new StringBuilder();
					localStringBuilder.append(getResources().getString(R.string.update_frequency, frequencyDefaultValue));
					listLocationFrequency.setSummary(localStringBuilder.toString());
				}
				listLocationFrequency.setOnPreferenceChangeListener(this);
			}

			if (checkboxAutoTest != null)
			{
				Boolean isAutoTestChecked = (Boolean) m_LocationProfileManager.getSettingContent(LocationProfileManager.LOCATION_AUTOTEST);
				if (LocationEmDebugControl.DEBUG_LOCATIONEM)
				{
					Xlog.d(TAG, "createPreference() isAutoTestChecked: " + isAutoTestChecked);
				}
				checkboxAutoTest.setChecked(isAutoTestChecked);
				checkboxAutoTest.setOnPreferenceChangeListener(this);
			}
			boolean bRet = (Boolean) m_LocationProfileManager.getSettingContent(LocationProfileManager.LOCATION_AUTOTEST_SHOW);
			if (LocationEmDebugControl.DEBUG_LOCATIONEM)
			{
				Xlog.d(TAG, "createPreference() LOCATION_AUTOTEST_SHOW: " + bRet);
			}
			if (!bRet)
			{
				if (checkboxAutoTest != null)
				{
					NetLocation.removePreference(checkboxAutoTest);
				}
			}
		}
		else
		{
			if (LocationEmDebugControl.DEBUG_LOCATIONEM)
			{
				Xlog.d(TAG, "extra em configuration is removed");
			}
			if (listLocationFrequency != null)
			{
				NetLocation.removePreference(listLocationFrequency);
			}
			if (checkboxAutoTest != null)
			{
				NetLocation.removePreference(checkboxAutoTest);
			}
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		if (null == newValue || null == preference)
		{
			return true;
		}
		Xlog.d(TAG, "onPreferenceChange is called preference: " + preference + "newValue: " + newValue);
		String key = preference.getKey();
		if (listBindNetworkService.getKey().equals(key))
		{
			m_LocationProfileManager.storeDataBack(LocationProfileManager.BIND_NETWORK_SERVICE_NAME, newValue);
			Settings.Secure.putString(getContentResolver(), LocationProfileManager.BIND_NETWORK_SERVICE_NAME, (String) newValue);
			Toast.makeText(this, m_LocationProfileManager.getNetworkLocationToastString((String) newValue), Toast.LENGTH_SHORT).show();
		}
		else if (listBindGeocoderService.getKey().equals(preference.getKey()))
		{
			m_LocationProfileManager.storeDataBack(LocationProfileManager.BIND_GEOCODER_SERVICE_NAME, newValue);
			Settings.Secure.putString(getContentResolver(), LocationProfileManager.BIND_GEOCODER_SERVICE_NAME, (String) newValue);
			Toast.makeText(this, m_LocationProfileManager.getGeocoderServiceToastString((String) newValue), Toast.LENGTH_SHORT).show();
		}
		else if (listLocationFrequency.getKey().equals(key))
		{
			StringBuilder localStringBuilder = new StringBuilder();
			localStringBuilder.append("Network location update frequency: ");
			localStringBuilder.append((String) newValue);
			localStringBuilder.append("s");
			listLocationFrequency.setSummary(localStringBuilder.toString());
			m_LocationProfileManager.storeDataBack(LocationProfileManager.LOCATION_FREQUENCY, newValue);
			Intent intent = new Intent(LOCATIONEMINTENTACTIONNAME_LOCATIONFREQUENCY);
			intent.putExtra(LocationProfileManager.LOCATION_FREQUENCY, Integer.getInteger((String) newValue));
			this.sendBroadcast(intent);
		}
		else if (checkboxAutoTest.getKey().equals(preference.getKey()))
		{
			m_LocationProfileManager.storeDataBack(LocationProfileManager.LOCATION_AUTOTEST, newValue);
			Intent intent = new Intent(LOCATIONEMINTENTACTIONNAME_AUTO_TEST);
			intent.putExtra(LocationProfileManager.LOCATION_AUTOTEST, (Boolean) newValue);
			this.sendBroadcast(intent);
		}
		return true;
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
	{
		super.onPreferenceTreeClick(preferenceScreen, preference);
		Xlog.d(TAG, "onPreferenceTreeClick-preference : " + preference.getKey());
        if(listUseLimite !=null && listUseLimite.getKey().equals(preference.getKey())){
            new AlertDialog.Builder(this)
            .setTitle(R.string.use_limit_detail_title)
            .setMessage(R.string.use_limit_detail_description)
            .setPositiveButton("Understand",
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialoginterface,int i) {
                }
            }).show();
        }
        //else if(listCellQuery != null && listCellQuery.getKey().equals(preference.getKey()))
       // {
        	//Intent intent = new Intent(LocationEm.this, CellQueryActivity.class);
        	//this.startActivity(intent);
       // }
		return false;
	}
}
