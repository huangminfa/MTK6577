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

package com.mediatek.engineermode.wifi;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.engineermode.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import com.mediatek.engineermode.wifi.EMWifi;
import com.mediatek.xlog.Xlog;
import android.widget.Toast;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;


public class WiFiStateManager
{
	private final String TAG = "EM/WiFi_WiFiStateManager";
	private WifiManager mWifiManager = null;
	private int chipID = 0x00;
	public static final int ENABLEWIFIFAIL = -1;
	public static final int INVALIDCHIPID = -2;
	public static final int SETTESTMODEFAIL = -3;
	public static final int CHIPREADY = -4;
	
	//private final int DIALOG_WIFI_INIT = 0x100;
	public Context callerContext = null;
	public WiFiStateManager(Context activityContext)
	{
		callerContext = activityContext;
		mWifiManager = (WifiManager)activityContext.getSystemService(Context.WIFI_SERVICE);
	}
	public int checkState(Context activityContext) {
		if(mWifiManager != null)
		{
			//showDialog(DIALOG_WIFI_INIT);
			if (mWifiManager.getWifiState() != mWifiManager.WIFI_STATE_ENABLED) {
	      		if (mWifiManager.setWifiEnabled(true))
				{
					Xlog.d(TAG, "enable wifi power succeed");
                                        Xlog.d(TAG, "After enable wifi, state is : " + mWifiManager.getWifiState());
					while (mWifiManager.getWifiState() == mWifiManager.WIFI_STATE_ENABLING
                                                || mWifiManager.getWifiState() == mWifiManager.WIFI_STATE_DISABLED) {
						SystemClock.sleep(100);
					}
				}
				else
				{
					Xlog.w(TAG, "enable wifi power failed");
					//callerContext.removeDialog(DIALOG_WIFI_INIT);
					return ENABLEWIFIFAIL;
				}
	        }
				if (mWifiManager.getWifiState() != mWifiManager.WIFI_STATE_ENABLED) {
					Xlog.w(TAG, "enable wifi power failed");
					//callerContext.removeDialog(DIALOG_WIFI_INIT);
					return ENABLEWIFIFAIL;
				}
				
				if (!EMWifi.isIntialed) {
					//WiFi is for both MT6620 and MT5921
					chipID = EMWifi.initial();
		                        if (chipID != 0x6620 && chipID != 0x5921) {
		                        		//callerContext.removeDialog(DIALOG_WIFI_INIT);
		                                return INVALIDCHIPID;
		                        } else {
		                                Xlog.d(TAG, "Initialize succeed");
		                                long result = -1;
		                                result = EMWifi.setTestMode();
		                                if(result == 0)
		                                {
		                                	EMWifi.isIntialed = true;
		                                	Xlog.i(TAG, "setTestMode succeed");
		                                }
		                                else
		                                {
		                                	Xlog.w(TAG, "setTestMode failed, ERROR_CODE = " + result);
		                                	//callerContext.removeDialog(DIALOG_WIFI_INIT);
		                                	return SETTESTMODEFAIL;
		                                }
		                        }
		      	}
		      	else
		      	{
		      		//callerContext.removeDialog(DIALOG_WIFI_INIT);
		      		return CHIPREADY;
		      	}
		    }
		//callerContext.removeDialog(DIALOG_WIFI_INIT);
		return chipID;
	}
	/*
	@Override
    protected void onPrepareDialog(int id, Dialog dialog) {
		Log.e(TAG, "-->onPrepareDialog");
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		Log.e(TAG, "-->onCreateDialog");
		if (id == DIALOG_WIFI_INIT) {
			ProgressDialog dialog = new ProgressDialog(callerContext);

			// It would be more efficient to reuse this dialog by moving
			// this setMessage() into onPreparedDialog() and NOT use
			// callerContext.removeDialog(). However, this is not possible since the
			// message is rendered only 2 times in the ProgressDialog -
			// after show() and before onCreate.
			if(dialog != null)
			{
				dialog.setTitle("Progress");
				dialog.setMessage("Initialization ...");
				dialog.setCancelable(false);
				dialog.setIndeterminate(true);
				dialog.show();
			}
			else
			{
				Log.e(TAG, "new ProgressDialog failed");
			}

			return dialog;
		}

		return null;
	}
	*/
	public void disableWiFi()
	{
		if(mWifiManager != null)
		{
			if (mWifiManager.setWifiEnabled(false))
			{
				Xlog.d(TAG, "disable wifi power succeed");
			}
			else
			{
				Xlog.w(TAG, "disable wifi power failed");
			}
		}	
	}
}
