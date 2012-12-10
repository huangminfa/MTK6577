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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.settings.R;

public class WifiReselectApDialog extends AlertActivity implements DialogInterface.OnClickListener {
    private static final String TAG = "WifiReselectApDialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"WifiReselectApDialog onCreate");
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
        TextView messageView = new TextView(this);
        messageView.setText(R.string.wifi_signal_found_msg);
        return messageView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void onOK() {
        Intent intent = new Intent();
        intent.setAction("android.settings.WIFI_SETTINGS");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void onCancel() {    
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
}
//MTK_OP01_PROTECT_END
