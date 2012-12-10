/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.stk;

import com.android.internal.telephony.cat.TextMessage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.android.internal.telephony.cat.CatLog;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.provider.Settings.System;

/**
 * AlretDialog used for DISPLAY TEXT commands.
 *
 */
public class StkDialogActivity extends Activity implements View.OnClickListener {
    // members
    TextMessage mTextMsg;
    private static final int MIN_LENGTH = 6;
    private static final int MIN_WIDTH = 170;
    private final BroadcastReceiver mReceiver = new AirplaneBroadcastReceiver(); 

    private boolean mbSendResp = false;

    StkAppService appService = StkAppService.getInstance();

    Handler mTimeoutHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case MSG_ID_TIMEOUT:
                sendResponse(StkAppService.RES_ID_TIMEOUT);
                finish();
                break;
            }
        }
    };

    //keys) for saving the state of the dialog in the icicle
    private static final String TEXT = "text";

    // message id for time out
    private static final int MSG_ID_TIMEOUT = 1;

    // buttons id
    public static final int OK_BUTTON = R.id.button_ok;
    public static final int CANCEL_BUTTON = R.id.button_cancel;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        CatLog.d(this, "onCreate - mbSendResp[" + mbSendResp + "]");
        initFromIntent(getIntent());
        if (mTextMsg == null) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        Window window = getWindow();

        setContentView(R.layout.stk_msg_dialog);
        TextView mMessageView = (TextView) window
                .findViewById(R.id.dialog_message);

        Button okButton = (Button) findViewById(R.id.button_ok);
        Button cancelButton = (Button) findViewById(R.id.button_cancel);

        okButton.setOnClickListener(this);
        //okButton.setHighFocusPriority(true);
        cancelButton.setOnClickListener(this);

        setTitle(mTextMsg.title);
        if (!(mTextMsg.iconSelfExplanatory && mTextMsg.icon != null)) {
            if ((mTextMsg.text==null) || (mTextMsg.text.length() < MIN_LENGTH)) {
                mMessageView.setMinWidth(MIN_WIDTH);
            }
            mMessageView.setText(mTextMsg.text);
        }

        if (mTextMsg.icon == null) {
            window.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                    com.android.internal.R.drawable.stat_notify_sim_toolkit);
        } else {
            window.setFeatureDrawable(Window.FEATURE_LEFT_ICON,
                    new BitmapDrawable(mTextMsg.icon));
        }
        
        IntentFilter intentFilter =
            new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(mReceiver, intentFilter);
    }

    private void init() {
        Window window = getWindow();
        TextView mMessageView = (TextView) window
                .findViewById(R.id.dialog_message);

        Button okButton = (Button) findViewById(R.id.button_ok);
        Button cancelButton = (Button) findViewById(R.id.button_cancel);

        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        setTitle(mTextMsg.title);
        if (!(mTextMsg.iconSelfExplanatory && mTextMsg.icon != null)) {
            if ((mTextMsg.text==null) || (mTextMsg.text.length() < MIN_LENGTH)) {
                mMessageView.setMinWidth(MIN_WIDTH);
            }
            mMessageView.setText(mTextMsg.text);
        }

        if (mTextMsg.icon == null) {
            window.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                    com.android.internal.R.drawable.stat_notify_sim_toolkit);
        } else {
            window.setFeatureDrawable(Window.FEATURE_LEFT_ICON,
                    new BitmapDrawable(mTextMsg.icon));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        CatLog.d(this, "onNewIntent");
        initFromIntent(intent);
        if (mTextMsg == null) {
            finish();
            return;
        }
        init();
    }

    public void onClick(View v) {
        String input = null;

        switch (v.getId()) {
        case OK_BUTTON:
            CatLog.d(this, "OK Clicked! isCurCmdSetupCall[" + appService.isCurCmdSetupCall() + "]");
            if (appService.isCurCmdSetupCall()) {
                CatLog.d(this, "stk call sendBroadcast(STKCALL_REGISTER_SPEECH_INFO)");
                Intent intent = new Intent("com.android.stk.STKCALL_REGISTER_SPEECH_INFO");
                sendBroadcast(intent);
            }
            sendResponse(StkAppService.RES_ID_CONFIRM, true);
            finish();
            break;
        case CANCEL_BUTTON:
            CatLog.d(this, "Cancel Clicked!");
            sendResponse(StkAppService.RES_ID_CONFIRM, false);
            finish();
            break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            sendResponse(StkAppService.RES_ID_BACKWARD);
            finish();
            break;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        CatLog.d(this, "onResume - mbSendResp[" + mbSendResp + "]");
        appService.indicateDialogVisibility(true);
        startTimeOut();
    }

    @Override
    public void onPause() {
        super.onPause();

        CatLog.d(this, "onPause");
        appService.indicateDialogVisibility(false);
        cancelTimeOut();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        CatLog.d(this, "onDestroy - before Send CONFIRM false mbSendResp[" + mbSendResp + "]");
        if (!mbSendResp) {
            CatLog.d(this, "onDestroy - Send CONFIRM false");
            sendResponse(StkAppService.RES_ID_CONFIRM, false);
        }
        
        appService.indicateDialogVisibility(false);
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        CatLog.d(this, "onSaveInstanceState");

        super.onSaveInstanceState(outState);
        outState.putParcelable(TEXT, mTextMsg);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mTextMsg = savedInstanceState.getParcelable(TEXT);
        CatLog.d(this, "onRestoreInstanceState - [" + mTextMsg + "]");
    }

    private void sendResponse(int resId, boolean confirmed) {
        if(StkAppService.getInstance().haveEndSession()) {
            // ignore current command
            CatLog.d(this, "Ignore response, id is " + resId);
            return;
        }
        CatLog.d(this, "sendResponse resID[" + resId + "] confirmed[" + confirmed + "]");
        
        mbSendResp = true;
        Bundle args = new Bundle();
        args.putInt(StkAppService.OPCODE, StkAppService.OP_RESPONSE);
        args.putInt(StkAppService.RES_ID, resId);
        args.putBoolean(StkAppService.CONFIRMATION, confirmed);
        // startService(new Intent(this, StkAppService.class).putExtras(args));
        StkAppService service = StkAppService.getInstance();
        if(service != null) {
            service.sendMessageToServiceHandler(
                    StkAppService.OP_RESPONSE, args);
        }
    }

    private void sendResponse(int resId) {
        sendResponse(resId, true);
    }

    private void initFromIntent(Intent intent) {

        if (intent != null) {
            mTextMsg = intent.getParcelableExtra("TEXT");
        } else {
            finish();
        }

        CatLog.d(this, "initFromIntent - [" + mTextMsg + "]");
    }

    private void cancelTimeOut() {
        mTimeoutHandler.removeMessages(MSG_ID_TIMEOUT);
    }

    private void startTimeOut() {
        // Reset timeout.
        cancelTimeOut();
        int dialogDuration = StkApp.calculateDurationInMilis(mTextMsg.duration);
        // case 1  userClear = true & responseNeeded = false,
        // Dialog always exists. 
        if(mTextMsg.userClear == true && mTextMsg.responseNeeded == false) {
            return;
        } else {
            // userClear = false. will dissapear after a while.
            if (dialogDuration == 0) {
                dialogDuration = StkApp.DIALOG_DEFAULT_TIMEOUT;
            }
            mTimeoutHandler.sendMessageDelayed(mTimeoutHandler
                .obtainMessage(MSG_ID_TIMEOUT), dialogDuration);
        }
    }
    
    /**
     * Receiver for misc intent broadcasts the Stk app cares about.
     */
    private class AirplaneBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                // do not care about whether enable areplane mode or not, just finish the screen.
                StkDialogActivity.this.cancelTimeOut();
                sendResponse(StkAppService.RES_ID_CONFIRM, false);
                StkDialogActivity.this.finish();
            }
        }
    }
}
