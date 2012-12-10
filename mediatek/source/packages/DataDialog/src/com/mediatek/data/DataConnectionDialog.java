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

package com.mediatek.data;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.provider.Telephony.SIMInfo;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.ITelephony;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

public class DataConnectionDialog extends AlertActivity implements DialogInterface.OnClickListener {
    private static final int DISMISS_DIALOG = 1;
    private static final long DIALOG_DISMISS_DELAY = 10 * 1000;
    private static final String ACTION_IPO_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN_IPO";
    private ITelephony mService;

    Handler mHandle = new Handler(){
        public void handleMessage(Message msg) {
            Log.d("DataConnectionDialog", "dismiss dialog and setup data.");
            if (msg.what == DISMISS_DIALOG) {
                ConnectivityManager cm =
                    (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    if (cm.getMobileDataEnabled()){
                        onUserPositive();
                    }
                    else {
                        onUserNegative();
                    }
                }
                finish();
            }
        }
    };

    private void onUserPositive(){
        ConnectivityManager cm = 
            (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            cm.setMobileDataEnabled(true);
            if (mService != null) {
                try {
                    mService.enableApnType(Phone.APN_TYPE_DEFAULT);
                } catch(Exception e) {
                    Log.e("DataConnectionDialog", "Fail to call mService.enableApnType():" + e.getMessage());
                }
            }
        } 
    }
    
    private void onUserNegative(){
    	ConnectivityManager cm =
        (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            cm.setMobileDataEnabled(false);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_IPO_SHUTDOWN.equals(action)) {
                Log.d("DataConnectionDialog", "receive IPO shutdown, abort dialog");
                mHandle.removeMessages(DISMISS_DIALOG);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setFinishOnTouchOutside(false);

        // Set up the "dialog"
        final AlertController.AlertParams p = mAlertParams;
        p.mIconId = android.R.drawable.ic_dialog_alert;
        p.mTitle = getString(R.string.dialog_title);
        p.mView = createView();
        p.mPositiveButtonText = getString(android.R.string.ok);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(android.R.string.cancel);
        p.mNegativeButtonListener = this;
        setupAlert();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_IPO_SHUTDOWN);
        registerReceiver(mReceiver, intentFilter);

        mService = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));

        mHandle.sendEmptyMessageDelayed(DISMISS_DIALOG, DIALOG_DISMISS_DELAY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandle.removeMessages(DISMISS_DIALOG);
        unregisterReceiver(mReceiver);
    }

    private View createView() {
        View view = getLayoutInflater().inflate(R.layout.confirm_dialog, null);
        TextView contentView = (TextView)view.findViewById(R.id.content);
        contentView.setText(getString(R.string.wifi_failover_gprs_content));
        return view;
    }

    public void onClick(DialogInterface dialog, int which) {
        mHandle.removeMessages(DISMISS_DIALOG);
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                onUserPositive();
                finish();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                onUserNegative();
                finish();
                break;

            default:
                Log.d("DataConnectionDialog", "onClick(): which=" + which);
                break;
        }
    }
}
