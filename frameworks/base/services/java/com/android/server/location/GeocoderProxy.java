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

package com.android.server.location;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Address;
import android.location.GeocoderParams;
import android.location.IGeocodeProvider;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;

import com.android.server.location.LocationServiceDebug;

import android.provider.Settings;
import com.mediatek.xlog.Xlog;

/**
 * A class for proxying IGeocodeProvider implementations.
 * 
 * {@hide}
 */
public class GeocoderProxy
{

	private static final String TAG = "GeocoderProxy";

	private final Context mContext;
	private Intent mIntent;
	private final Object mMutex = new Object(); // synchronizes access to
	// mServiceConnection
	private Connection mServiceConnection = null; // never null

	private final static String GEOCODER_BIND_SERVICENMAE = "GeocoderBindeServiceName";
	private final static String GEOCODER_BIND_SERVICESUCESS = "GeocoderBindeServiceSucess";

	public GeocoderProxy(Context context, String serviceName)
	{
		mContext = context;
		loadGeocoderProvider(serviceName);
	}

	private void loadGeocoderProvider(String serviceName)
	{
		if (serviceName != null)
		{
			boolean bRet = false;
			mIntent = new Intent(serviceName);
			mServiceConnection = new Connection();
			bRet = mContext.bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
			Xlog.d(TAG, "GeocoderProxy:bind geocoder service " + (bRet == true ? "sucess" : "fail") + "name: " + serviceName);
			Settings.Secure.putString(mContext.getContentResolver(), "ActualGeocoderServiceName", serviceName);
			Settings.Secure.putInt(mContext.getContentResolver(), "ActualGeocoderServiceSucess", bRet == true ? 1 : 0);
		}
	}
	
	/**
	 * When unbundled NetworkLocationService package is updated, we need to
	 * unbind from the old version and re-bind to the new one.
	 */
	public void reconnect()
	{
		synchronized (mMutex)
		{
			if(mIntent != null)
			{
				Xlog.d(TAG, "GeocoderProxy:reconnect() is called");
				mContext.unbindService(mServiceConnection);
				mServiceConnection = new Connection();
				mContext.bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
			}
		}
	}

	public void reconnectAfterIPO(String serviceName)
	{
		synchronized (mMutex)
		{
			Xlog.d(TAG, "GeocoderProxy:reconnectAfterIPO is called");
			loadGeocoderProvider(serviceName);
		}
	}

	private class Connection implements ServiceConnection
	{

		private IGeocodeProvider mProvider;

		public void onServiceConnected(ComponentName className, IBinder service)
		{
			Xlog.d(TAG, "onServiceConnected " + className);
			synchronized (this)
			{
				mProvider = IGeocodeProvider.Stub.asInterface(service);
			}
		}

		public void onServiceDisconnected(ComponentName className)
		{
			Xlog.d(TAG, "onServiceDisconnected " + className);
			synchronized (this)
			{
				mProvider = null;
			}
		}

		public IGeocodeProvider getProvider()
		{
			synchronized (this)
			{
				return mProvider;
			}
		}
	}

	public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs)
	{
		Xlog.d(TAG, "GeocoderProxy:getFromLocation() is called");
		IGeocodeProvider provider;
		synchronized (mMutex)
		{
			provider = mServiceConnection.getProvider();
		}
		String retString = "Service not Available";
		if (provider != null)
		{
			try
			{
				retString = provider.getFromLocation(latitude, longitude, maxResults, params, addrs);
				if (LocationServiceDebug.DEBUG_GEOPROVIDERPROXY)
				{
					Xlog.d(TAG, "getFromLocation retString:" + (retString != null ? retString : "null") + "addrs:" + addrs);
				}
				return retString;
			}
			catch (RemoteException e)
			{
				Xlog.e(TAG, "getFromLocation failed", e);
			}
		}
		return retString;
	}

	public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude,
	        double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs)
	{

		Xlog.d(TAG, "GeocoderProxy:getFromLocationName() is called");
		IGeocodeProvider provider;
		synchronized (mMutex)
		{
			provider = mServiceConnection.getProvider();
		}
		if (provider != null)
		{
			try
			{
				return provider.getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults,
				        params, addrs);
			}
			catch (RemoteException e)
			{
				Xlog.e(TAG, "getFromLocationName failed", e);
			}
		}
		return "Service not Available";
	}
}
