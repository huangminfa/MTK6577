/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Xml;
import android.provider.Settings;
import com.mediatek.xlog.Xlog;
import java.lang.Long;
/**
 * A class for helping to find the LocationBindService {@hide}
 */

public class LocationBindServiceSelection
{
	private static final String TAG = "LocationBindServiceSelection";
	private static LocationBindServiceSelection mLocationBindServiceSelection = null;
	private Context mContext = null;
	private String mGeocoderServiceName = null;
	private String mNetworkServiceName = null;
	public static final String BIND_NETWORK_SERVICE_NAME = "NetworkServiceName";
	public static final String BIND_GEOCODER_SERVICE_NAME = "PrivateInnerGeocoderServiceName";
	private static final String LOCATION_SERVICE_CONFIGFILE_NAME = "location_service_conf.xml";
	private static final String LOCATION_SERVICE_CONFIGFILE_PATH = "/system/etc";

	private LocationBindServiceSelection(Context context)
	{
		mContext = context;
	}

	/**
	 * A single instance of LocationBindServiceSelection which is used by
	 * LocationManagerService construct funcation()
	 * 
	 * @see LocationManagerService. {@hide}
	 */
	public static LocationBindServiceSelection getSingleInstance(Context context)
	{
		if (mLocationBindServiceSelection == null)
		{
			mLocationBindServiceSelection = new LocationBindServiceSelection(context);
		}
		return mLocationBindServiceSelection;
	}

	/**
	 * getGeocoderServiceName() is returning the geocdoer service name which
	 * will be bind in the system. It first read the database with
	 * Settings.Secure.getString(key) method which might be changed by the user
	 * settings in location em, if can not get this value, then it will read XML
	 * configuration file.
	 * 
	 * @return The geocoder service name
	 */
	public String getGeocoderServiceName()
	{
		mGeocoderServiceName = "com.google.android.location.GeocodeProvider";
		String tempGeocoderServiceName = null;
		tempGeocoderServiceName = Settings.Secure.getString(mContext.getContentResolver(), "GeocoderServiceName");
		Xlog.v(TAG, "getGeocoderServiceName from the database geocoderServiceName:" + (tempGeocoderServiceName != null ? tempGeocoderServiceName : "null"));
		if (tempGeocoderServiceName == null)
		{
			tempGeocoderServiceName = getBindServiceFromXMLFile(BIND_GEOCODER_SERVICE_NAME);
			Xlog.v(TAG, "getGeocoderServiceName from the XML file geocoderServiceName:" + (tempGeocoderServiceName != null ? tempGeocoderServiceName : "null"));
		}
		if (tempGeocoderServiceName != null)
		{
			mGeocoderServiceName = tempGeocoderServiceName;
		}
		Xlog.d(TAG, "getGeocoderServiceName() is returnning mGeocoderServiceName:"+mGeocoderServiceName);
		return mGeocoderServiceName;
	}

	/**
	 * getNetworkLocationServiceName() is returning the network location service
	 * name which will be bind in the system. It first read the database with
	 * Settings.Secure.getString(key) method which might be changed by the user
	 * settings in location em, if can not get this value, then it will read XML
	 * configuration file.
	 * 
	 * @return The NetworkLocation service name
	 */
	public String getNetworkLocationServiceName()
	{
		mNetworkServiceName = "com.google.android.location.NetworkLocationProvider";
		String tempNetworkLocationServiceName = null;
		tempNetworkLocationServiceName = Settings.Secure.getString(mContext.getContentResolver(), "NetworkServiceName");
		Xlog.v(TAG, "getNetworkLocationServiceName from the database networkLocationServiceName:"
		        + (tempNetworkLocationServiceName != null ? tempNetworkLocationServiceName : "null"));
		if (tempNetworkLocationServiceName == null)
		{
			tempNetworkLocationServiceName = getBindServiceFromXMLFile(BIND_NETWORK_SERVICE_NAME);
			Xlog.v(TAG, "getNetworkLocationServiceName from the XML file networkLocationServiceName:"
			        + (tempNetworkLocationServiceName != null ? tempNetworkLocationServiceName : "null"));
		}
		if (tempNetworkLocationServiceName != null)
		{
			mNetworkServiceName = tempNetworkLocationServiceName;
		}
		Xlog.d(TAG, "getNetworkLocationServiceName is returnning mNetworkServiceName:"+mNetworkServiceName);
		return mNetworkServiceName;
	}

	/**
	 * getBindServiceFromXMLFile is called when user did not set default bind
	 * service in settings-->locationEM or the phone has just been refalshed.
	 */
	private String getBindServiceFromXMLFile(String searchTag)
	{
		long startTime = System.currentTimeMillis();
		String serviceName = null;
		XmlPullParser parser = Xml.newPullParser();
		InputStream is = null;
		try
		{
			File configFile = new File(LOCATION_SERVICE_CONFIGFILE_PATH, LOCATION_SERVICE_CONFIGFILE_NAME);
			is = new FileInputStream(configFile);
			parser.setInput(is, null);
			int eventType = parser.getEventType();
			boolean done = false;
			while (eventType != XmlPullParser.END_DOCUMENT && !done)
			{
				String name = null;
				switch (eventType)
				{
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase(searchTag))
					{
						serviceName = parser.nextText();
					}
					break;
				case XmlPullParser.END_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase("LocationService"))
						done = true;
					break;
				}
				eventType = parser.next();
			}
		}
		catch(IOException e)
		{
			Xlog.e(TAG, e.toString());
			serviceName = null;
		}
		catch (Exception e)
		{
			Xlog.e(TAG, e.toString());
			serviceName = null;
		}
		finally
		{
			if(null != is)
			{
				try
				{
					is.close();
				}
				catch(IOException e)
				{
					Xlog.e(TAG, e.toString());
				}
			}
		}
		long endTime = System.currentTimeMillis();
		Xlog.d(TAG, "getBindServiceFromXMLFile: "+searchTag + "Using time: "+ (endTime - startTime));
		return serviceName;
	}
}
