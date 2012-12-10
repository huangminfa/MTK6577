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
//For Operator Custom
//MTK_OP01_PROTECT_START
package com.android.settings.wifi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.net.wifi.WifiManager;
import android.provider.Settings.System;
import android.content.ContentResolver;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.internal.util.AsyncChannel;
import com.android.settings.R;

import com.mediatek.xlog.Xlog;

public class WifiNotifyDialog extends AlertActivity implements DialogInterface.OnClickListener {
    private static final String TAG = "WifiNotifyDialog";

    private WifiManager mWm;
    private String mSsid;
    private int mNetworkId;

    private boolean mConnectApType;

    private static void log(String msg) {
        Xlog.d(TAG, msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mWm.asyncConnect(this, new WifiServiceHandler());
        Intent intent = getIntent();
        String action = intent.getAction();
        mSsid = intent.getStringExtra(WifiManager.EXTRA_NOTIFICATION_SSID);
        mNetworkId = intent.getIntExtra(WifiManager.EXTRA_NOTIFICATION_NETWORKID, -1);
        log("WifiNotifyDialog onCreate " + action);
        if (!action.equals(WifiManager.WIFI_NOTIFICATION_ACTION) || mNetworkId == -1) {
            Xlog.e(TAG, "Error: this activity may be started only with intent WIFI_NOTIFICATION_ACTION");
            finish();
        }
        createDialog();
    }

    private void createDialog() {
        final AlertController.AlertParams p = mAlertParams;
        p.mIconId = android.R.drawable.ic_dialog_info;
        p.mTitle = getString(R.string.confirm_title);
        p.mView = createView();
        p.mViewSpacingSpecified=true;
        p.mViewSpacingLeft=15;
        p.mViewSpacingRight=15;
        p.mViewSpacingTop=5;
        p.mViewSpacingBottom=5;
        p.mPositiveButtonText = getString(android.R.string.yes);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(android.R.string.no);
        p.mNegativeButtonListener = this;
        setupAlert();
    }

    private View createView() {
        log("createView mSsid="+mSsid);
        TextView messageView = new TextView(this);
        messageView.setText(getString(R.string.msg_wlan_signal_found, mSsid));
        return messageView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void onOK() {
        log("onOK mNetworkId=" + mNetworkId);
        mConnectApType = System.getInt(getContentResolver(), System.WIFI_CONNECT_AP_TYPE, System.WIFI_CONNECT_AP_TYPE_AUTO)==0;
        if(mConnectApType){
//            mWm.enableNetwork(mNetworkId, true);
//            mWm.reconnect();
            log("onOK auto connect AP");
            mWm.connectNetwork(mNetworkId);
        }else{
            Intent intent = new Intent();
            intent.setAction("android.settings.WIFI_SETTINGS");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        finish();
    }

    private void onCancel() {
        log("onCancel mNetworkId=" + mNetworkId);  
        mWm.suspendNotification();     
        finish();
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                onOK();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                onCancel();
                break;
        }
    }
    private class WifiServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED:
                    if (msg.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
                        //AsyncChannel in msg.obj
                    } else {
                        //AsyncChannel set up failure, ignore
                        log("Failed to establish AsyncChannel connection");
                    }
                    break;
                default:
                    //Ignore
                    break;
            }
        }
    }
}
//MTK_OP01_PROTECT_END
