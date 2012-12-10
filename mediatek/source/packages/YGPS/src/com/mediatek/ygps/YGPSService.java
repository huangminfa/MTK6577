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

package com.mediatek.ygps;


import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.widget.Toast;
import com.mediatek.xlog.Xlog;

//import com.mediatek.mobilelog.R;

public class YGPSService extends Service {

	private static final String TAG = "EM/YGPS_Service";
		static final String SERVICE_START_ACTION = "com.mediatek.ygps.YGPSService";
	private boolean serviceOn = false;
	

	@Override
	public void onCreate() {
		Xlog.v(TAG, "YGPSService onCreate");
		// sSelf = this;
		//mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);	

	}

	@Override
	public void onDestroy() {
		Xlog.v(TAG, "YGPSService onDestroy");
		serviceOn = false;		

		//mNM.cancel(R.string.mobilelog_service_start);
		//mNM.cancelAll();
		// sSelf = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Xlog.v(TAG, "onStartCommand " + intent + " flags " + flags);
		
		if (intent == null || (intent.getAction().equals(SERVICE_START_ACTION) == false)) {
			Xlog.w(TAG, "intent null error: " + intent);
		//	mNM.cancelAll();			
			serviceOn = false;
			return START_STICKY;
		}
		
		if (serviceOn == true) {
			Xlog.w(TAG, "Service is already running");
			return START_STICKY;
		}
		
		
		serviceOn = true;		
		
		
		//mNM.cancelAll();

		return START_STICKY;
	}

	// BEGIN_INCLUDE(exposing_a_service)
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	// END_INCLUDE(exposing_a_service)


}
