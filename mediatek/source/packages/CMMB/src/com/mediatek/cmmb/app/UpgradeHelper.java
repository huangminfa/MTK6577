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

package com.mediatek.cmmb.app;

import com.mediatek.mbbms.service.SettingsUtils;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.util.Log;
import android.widget.Toast;

public final class UpgradeHelper
{
    private static final String TAG = "CMMB::UpgradeHelper";
	    
    public static final int RESULT_FAILED = 0;
	/*user paused the upgrade,user can resume it in DM application*/
    public static final int RESULT_PAUSED = 1;
    public static final int RESULT_ABORTED = 2;
	
	private UpgradeCallback mUpgradeCallback;
	private ProgressDialog mProgressDialog;
	private BroadcastReceiver mReceiver;

    private static UpgradeHelper sUpgradeHelper = new UpgradeHelper();
	
	public static UpgradeHelper getInstance() {
		return sUpgradeHelper;
	} 

	public void doUpgrade(Context context,UpgradeCallback callback) {
		Log.d(TAG, "doUpgrade");
		
		if (mUpgradeCallback != null) {
			Log.e(TAG, "Someone calls doUpgrade while mUpgradeCallback != null");
			handleResponse(context,RESULT_ABORTED);
		}
		mUpgradeCallback = callback;		
		
		mProgressDialog = new ProgressDialog(context);
		//mProgressDialog.setCancelable(false);
		mProgressDialog.setOnCancelListener(new Dialog.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				Log.d(TAG, "mProgressDialog onCancel");
				Context c = ((ProgressDialog)dialog).getContext();
				Toast.makeText(c,c.getResources().getString(R.string.upgrade_is_ongoing)
							,2000).show();
				handleResponse(c,RESULT_ABORTED);
			}
		});  
		
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setMessage(context.getResources().getString(R.string.please_wait));
		mProgressDialog.show();

		mReceiver = new DmBroadcastReceiver();

		//call DM to start upgrade.
		Intent intent = new Intent("com.mediatek.dm.scomo_ci_request");
		intent.putExtra("pkg_name",SettingsUtils.getUserAgent());
		context.sendBroadcast(intent);	

		//register listener for upgrade result.
        IntentFilter intentFilter = new IntentFilter("com.mediatek.dm.scomo_ci_response"/*DmScomoCINotifier.BROADCAST_ACTION*/);
        context.getApplicationContext().registerReceiver(mReceiver, intentFilter);		
		
		Log.d(TAG, "doUpgrade mReceiver is registered");
	}	

	private void handleResponse(Context context,int response) {
		Log.d(TAG, "handleResponse response = "+response);
		
		if (mReceiver != null) {
		context.getApplicationContext().unregisterReceiver(mReceiver);
		mReceiver = null;	
		}
		
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		
		if (mUpgradeCallback != null) {
			mUpgradeCallback.onUpgradeFinish(response);
			mUpgradeCallback = null;
		}
	}
	
	private class DmBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) { 
			String pkg = intent.getStringExtra("pkg_name");
			if (pkg == null || !pkg.equals("com.mediatek.cmmb")) {
				Log.d(TAG, "onReceive wrong pkg = "+pkg);
				return;
			}
			
			int response = intent.getIntExtra("response",-1);
			Log.d(TAG, "onReceive response = "+response);					

			if (response == 2/*DmScomoCINotifier.RESPONSE_DOWNLOAD_PAUSED*/) {
				response = RESULT_PAUSED;
			} else if (response == 3/*DmScomoCINotifier.RESPONSE_DOWNLOAD_ABORTED*/) {
				response = RESULT_ABORTED;
			} else {
				//simply treat all other cases as failure case because in successful case
				//our application is already killed thus impossible to receive this broadcast.
				response = RESULT_FAILED;
				Toast.makeText(context, R.string.upgrade_failed, 1000).show();
			}
			handleResponse(context,response);
		}
	}

	public interface UpgradeCallback {
		public void onUpgradeFinish(int result);
	}		
}

