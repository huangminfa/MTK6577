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

import java.util.HashMap;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import com.android.settings.R;
import com.mediatek.xlog.Xlog;

/**
 * A class for get value for Location Service Settings. {@hide}
 */
public class LocationProfileManager
{
	private String TAG = "LocationProfileManager";
	private static final String PROFILE_KEY = "com.android.settings_preferences";
	private Context m_Context = null;
	private HashMap<String, Object> mDefaultValueMap;
	public static final String BIND_NETWORK_SERVICE_NAME = "NetworkServiceName";
	public static final String BIND_GEOCODER_SERVICE_NAME = "GeocoderServiceName";
	public static final String LOCATION_FREQUENCY = "LocationFrequency";
	public static final String LOCATION_AUTOTEST = "LocationAutoTest";
	public static final String LOCATION_AUTOTEST_SHOW = "LocationAutoTestShow";
	public String mActualBindNetworkLocationServiceName = null;
	public int IsBindNetworkLocationServiceSucess = 0;
	public String mActualBindGeocoderServiceName = null;
	public int IsBindGeocoderServiceSucess = 0;
	public static boolean IsFirstStarted = true;

	/**
	 * This funcation is called for each LocationEm.onCreate() is called
	 */
	public LocationProfileManager(Context context)
	{
		m_Context = context;
		if (LocationEmDebugControl.DEBUG_LOCATIONEM)
		{
			Xlog.d(TAG, "LocationProfileManager() is called");
		}
	}

	/**
	 * This funcation is called for each LocationEm.onCreate() is called This
	 * funcation is used to get default values of Location Service from the
	 * SharedPreference file
	 */
	public void init()
	{
		Xlog.d(TAG, "LocationProfileManager init() is called");
		try
		{
			mActualBindNetworkLocationServiceName = Settings.Secure.getString(m_Context.getContentResolver(), "ActualNetworkServiceName");
			IsBindNetworkLocationServiceSucess = Settings.Secure.getInt(m_Context.getContentResolver(), "ActualNetworkServiceSucess");
			mActualBindGeocoderServiceName = Settings.Secure.getString(m_Context.getContentResolver(), "ActualGeocoderServiceName");
			IsBindGeocoderServiceSucess = Settings.Secure.getInt(m_Context.getContentResolver(), "ActualGeocoderServiceSucess");
		}
		catch (SettingNotFoundException e)
		{
			Xlog.e(TAG, "SettingNotFoundException: " + e);
		}
		SharedPreferences setting = m_Context.getSharedPreferences(PROFILE_KEY, Context.MODE_PRIVATE);
		Xlog.d(TAG, "NetworkLocationServiceName: " + mActualBindNetworkLocationServiceName + "Sucess: "+IsBindNetworkLocationServiceSucess +"finish");
		Xlog.d(TAG, "GeocoderServiceName: " + mActualBindGeocoderServiceName + "Sucess: "+IsBindGeocoderServiceSucess+"finish");
		mDefaultValueMap = new HashMap<String, Object>();
		String networkLocationServiceName = setting.getString(BIND_NETWORK_SERVICE_NAME, null);
		Xlog.d(TAG, "get the value from SharedPreferences" + (networkLocationServiceName != null ? networkLocationServiceName : "null"));
		if(networkLocationServiceName == null)
		{
			networkLocationServiceName = mActualBindNetworkLocationServiceName;
		}
		Xlog.d(TAG, "The init network location value put to the ValueMap: " + (networkLocationServiceName != null ? networkLocationServiceName : "null"));
		mDefaultValueMap.put(BIND_NETWORK_SERVICE_NAME, networkLocationServiceName);
		String geocoderServiceName = setting.getString(BIND_GEOCODER_SERVICE_NAME, null);
		Xlog.d(TAG, "get the geocoderServiceName value from SharedPreferences" + (geocoderServiceName != null ? geocoderServiceName : "null"));
		if(geocoderServiceName == null)
		{
			geocoderServiceName = mActualBindGeocoderServiceName;
		}
		Xlog.d(TAG, "The init geocoder value put to the ValueMap: " + (geocoderServiceName != null ? geocoderServiceName : "null"));
		mDefaultValueMap.put(BIND_GEOCODER_SERVICE_NAME, geocoderServiceName);
		int LocationFrequency = setting.getInt(LOCATION_FREQUENCY, 10);
		if (LocationEmDebugControl.DEBUG_LOCATIONEM)
		{
			Xlog.d(TAG, "get the LocationFrequency value from SharedPreferences LocationFrequency:" + LocationFrequency);
		}
		mDefaultValueMap.put(LOCATION_FREQUENCY, LocationFrequency);
		boolean bIsLOCATION_AUTOTEST_SHOW = setting.getBoolean(LOCATION_AUTOTEST_SHOW, false);
		Xlog.d(TAG, "bIsLOCATION_AUTOTEST_SHOW: " + bIsLOCATION_AUTOTEST_SHOW);
		mDefaultValueMap.put(LOCATION_AUTOTEST_SHOW, bIsLOCATION_AUTOTEST_SHOW);
		if (IsFirstStarted)
		{
			IsFirstStarted = false;
			mDefaultValueMap.put(LOCATION_AUTOTEST, false);
		}
		else
		{
			boolean isLocationAutoTestEnabled = setting.getBoolean(LOCATION_AUTOTEST, false);
			if (LocationEmDebugControl.DEBUG_LOCATIONEM)
			{
				Xlog.d(TAG, "get the LocationFrequency value from SharedPreferences isLocationAutoTestEnabled:" + isLocationAutoTestEnabled);
			}
			mDefaultValueMap.put(LOCATION_AUTOTEST, isLocationAutoTestEnabled);
		}
	}

	public Object getSettingContent(String key)
	{
		Object value = null;
		if (mDefaultValueMap.containsKey(key))
		{
			value = mDefaultValueMap.get(key);
		}
		return value;
	}

	public void storeDataBack(String key, Object newValue)
	{
		try
		{
			Xlog.d(TAG, "storeDataBack() is called key:" + key + "newValue:" + newValue);
			SharedPreferences settings = m_Context.getSharedPreferences(PROFILE_KEY, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			if (editor != null)
			{
				if (key.equalsIgnoreCase(BIND_NETWORK_SERVICE_NAME))
				{
					editor.putString(key, (String) newValue);
				}
				else if (key.equalsIgnoreCase(BIND_GEOCODER_SERVICE_NAME))
				{
					editor.putString(key, (String) newValue);
				}
				else if (key.equalsIgnoreCase(LOCATION_FREQUENCY))
				{
					editor.putInt(key, Integer.parseInt(((String) newValue)));
				}
				else if (key.equalsIgnoreCase(LOCATION_AUTOTEST))
				{
					editor.putBoolean(key, (Boolean) newValue);
				}
				editor.commit();
			}
		}
		catch (NumberFormatException e)
		{
			if (LocationEmDebugControl.DEBUG_LOCATIONEM)
			{
				Xlog.d(TAG, "storeDataBack() throws NumberFormatException" + e);
			}
		}
	}

	public String getNetworkLocationServiceBindAbstract()
	{
		String retString = null;
		String[] networkLocationServiceArray;
		String[] networkLocationServiceArrayValues;
		StringBuilder localStringBuilder = new StringBuilder();
		networkLocationServiceArray = m_Context.getResources().getStringArray(R.array.networklocation_bindservice_entries);
		networkLocationServiceArrayValues = m_Context.getResources().getStringArray(R.array.networklocation_bindservice_entriesValues);
		String serviceName = null;
		if (mActualBindNetworkLocationServiceName == null && networkLocationServiceArray != null && networkLocationServiceArrayValues.length > 0)
		{
			String networkLocationDefaultValue = (String)this.getSettingContent(LocationProfileManager.BIND_NETWORK_SERVICE_NAME);
			int pos = 0;
			if (networkLocationDefaultValue != null)
			{
				LocationEm em = (LocationEm)m_Context;
				pos = em.listBindNetworkService.findIndexOfValue(networkLocationDefaultValue);
			}
			serviceName = networkLocationServiceArray[pos];
		}
		else if(mActualBindNetworkLocationServiceName != null)
		{
			for (int i = 0; i < networkLocationServiceArrayValues.length; i++)
			{
				if (mActualBindNetworkLocationServiceName.equals(networkLocationServiceArrayValues[i]))
				{
					serviceName = networkLocationServiceArray[i];
					break;
				}
			}
		}
		if (serviceName == null)
		{
			localStringBuilder.append("can not get the bind service");
		}
		else
		{
			localStringBuilder.append(serviceName);
			localStringBuilder.append(" ");
			if (IsBindNetworkLocationServiceSucess == 0)
			{
				localStringBuilder.append(m_Context.getResources().getString(R.string.net_failed));
			}
			else if (IsBindNetworkLocationServiceSucess == 1)
			{
				localStringBuilder.append(m_Context.getResources().getString(R.string.net_sucess));
			}
		}
		retString = localStringBuilder.toString();
		return retString;
	}

	public String getGeocoderServiceBindAbstract()
	{
		String retString = null;
		String[] geocoderServiceArray;
		String[] geocoderServiceArrayValues;
		StringBuilder localStringBuilder = new StringBuilder();
		geocoderServiceArray = m_Context.getResources().getStringArray(R.array.geocoder_bindservice_entries);
		geocoderServiceArrayValues = m_Context.getResources().getStringArray(R.array.geocoder_bindservice_entriesValues);
		String serviceName = null;
		if(mActualBindGeocoderServiceName == null && geocoderServiceArray != null && geocoderServiceArrayValues.length > 0)
		{
			String geocoderDefaultValue = (String) this.getSettingContent(LocationProfileManager.BIND_GEOCODER_SERVICE_NAME);
			int pos = 0;
			if (geocoderDefaultValue != null)
			{
				LocationEm em = (LocationEm)m_Context;
				pos = em.listBindGeocoderService.findIndexOfValue(geocoderDefaultValue);
			}
			serviceName = geocoderServiceArray[pos];
		}
		else if(mActualBindGeocoderServiceName != null)
		{
			for (int i = 0; i < geocoderServiceArrayValues.length; i++)
			{
				if (mActualBindGeocoderServiceName.equals(geocoderServiceArrayValues[i]))
				{
					serviceName = geocoderServiceArray[i];
					break;
				}
			}
		}
		if (serviceName == null)
		{
			localStringBuilder.append("can not get the bind service");
		}
		else
		{
			localStringBuilder.append(serviceName);
			localStringBuilder.append(" ");
			if (IsBindGeocoderServiceSucess == 0)
			{
				localStringBuilder.append(m_Context.getResources().getString(R.string.geo_failed));
			}
			else if (IsBindGeocoderServiceSucess == 1)
			{
				localStringBuilder.append(m_Context.getResources().getString(R.string.geo_sucess));
			}
		}
		retString = localStringBuilder.toString();
		return retString;
	}

	public String getNetworkLocationToastString(String key)
	{
		String[] networkLocationServiceArray;
		String[] networkLocationServiceArrayValues;
		networkLocationServiceArray = m_Context.getResources().getStringArray(R.array.networklocation_bindservice_entries);
		networkLocationServiceArrayValues = m_Context.getResources().getStringArray(R.array.networklocation_bindservice_entriesValues);
		StringBuilder localStringBuilder = new StringBuilder();
		for (int i = 0; i < networkLocationServiceArrayValues.length; i++)
		{
			if (key.equals(networkLocationServiceArrayValues[i]))
			{
				localStringBuilder.append(networkLocationServiceArray[i]);
				localStringBuilder.append(" ");
				break;
			}
		}
		if (key.equals(mActualBindNetworkLocationServiceName) && IsBindNetworkLocationServiceSucess == 1)
		{
			localStringBuilder.append(m_Context.getResources().getString(R.string.net_now));
		}
		else
		{
			localStringBuilder.append(m_Context.getResources().getString(R.string.net_reboot));
		}
		return localStringBuilder.toString();
	}

	public String getGeocoderServiceToastString(String key)
	{
		String[] geocoderServiceArray;
		String[] geocoderServiceArrayValues;
		geocoderServiceArray = m_Context.getResources().getStringArray(R.array.geocoder_bindservice_entries);
		geocoderServiceArrayValues = m_Context.getResources().getStringArray(R.array.geocoder_bindservice_entriesValues);
		StringBuilder localStringBuilder = new StringBuilder();
		for (int i = 0; i < geocoderServiceArrayValues.length; i++)
		{
			if (key.equals(geocoderServiceArrayValues[i]))
			{
				localStringBuilder.append(geocoderServiceArray[i]);
				localStringBuilder.append(" ");
				break;
			}
		}
		if (key.equals(mActualBindGeocoderServiceName) && IsBindGeocoderServiceSucess == 1)
		{
			localStringBuilder.append(m_Context.getResources().getString(R.string.geo_now));
		}
		else
		{
			localStringBuilder.append(m_Context.getResources().getString(R.string.geo_reboot));
		}
		return localStringBuilder.toString();
	}

	public Boolean IsMediaTekLocationServiceBinding()
	{
		Xlog.d(TAG, "IsMediaTekLocationServiceBinding() is called mActualBindNetworkLocationServiceName: " + (mActualBindNetworkLocationServiceName != null?mActualBindNetworkLocationServiceName:"null") 
		        + "IsBindNetworkLocationServiceSucess: " + IsBindNetworkLocationServiceSucess);
		if(mActualBindNetworkLocationServiceName == null)
		{
			return false;
		}
		String serviceName = "com.mediatek.android.location.NetworkLocationProvider";
		if (mActualBindNetworkLocationServiceName.equals(serviceName) && IsBindNetworkLocationServiceSucess == 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
