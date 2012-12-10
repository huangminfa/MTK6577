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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.telephony.cat.TextMessage;
import com.android.internal.telephony.cat.ToneSettings;
import com.android.internal.telephony.cat.CatLog;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.provider.Settings.System;

/**
 * Activity used for PLAY TONE command.
 *
 */
public class ToneDialog extends Activity {
    TextMessage toneMsg = null;
    ToneSettings settings = null;
    TonePlayer player = null;
    private final BroadcastReceiver mReceiver = new AirplaneBroadcastReceiver(); 
    
    private boolean mbSendResp = false;
    
    /**
     * Handler used to stop tones from playing when the duration ends.
     */
    Handler mToneStopper = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_ID_STOP_TONE:
                sendResponse(StkAppService.RES_ID_DONE);
                finish();
                break;
            }
        }
    };

    Vibrator mVibrator = new Vibrator();

    // Message id to signal tone duration timeout.
    private static final int MSG_ID_STOP_TONE = 0xda;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        CatLog.d(this, "onCreate - mbSendResp[" + mbSendResp + "]");

       initFromIntent(getIntent());

        // remove window title
        View title = findViewById(com.android.internal.R.id.title);
        title.setVisibility(View.GONE);

        if ((null == toneMsg.text) ||(toneMsg.text.equals("")) ) {
        	
        }
        else
        {
        // set customized content view
        setContentView(R.layout.stk_tone_dialog);

        TextView tv = (TextView) findViewById(R.id.message);
        ImageView iv = (ImageView) findViewById(R.id.icon);

        // set text and icon
        if (null != toneMsg) {
            tv.setText(toneMsg.text);
        } else {
            CatLog.d(this, "onCreate - null tone text");
        }
        
        if (toneMsg.icon == null) {
            iv.setImageResource(com.android.internal.R.drawable.ic_volume);
        } else {
            iv.setImageBitmap(toneMsg.icon);
        }
        }

        // Start playing tone and vibration
        if (null == settings) {
            CatLog.d(this, "onCreate - null settings - finish");
            finish();
        }
        
        player = new TonePlayer();
        player.play(settings.tone);
        int timeout = StkApp.calculateDurationInMilis(settings.duration);
        if (timeout == 0) {
            timeout = StkApp.TONE_DFEAULT_TIMEOUT;
        }
        mToneStopper.sendEmptyMessageDelayed(MSG_ID_STOP_TONE, timeout);
        if (settings.vibrate) {
            mVibrator.vibrate(timeout);
        }

        IntentFilter intentFilter =
            new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        CatLog.d(this, "onDestroy - before Send End Session mbSendResp[" + mbSendResp + "]");
        if (!mbSendResp) {
            CatLog.d(this, "onDestroy - Send End Session");
            sendResponse(StkAppService.RES_ID_END_SESSION);
        }

        mToneStopper.removeMessages(MSG_ID_STOP_TONE);
        if (null != player) {
            player.stop();
            player.release();
        }
        if (null != mVibrator) {
            mVibrator.cancel();
        }
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            sendResponse(StkAppService.RES_ID_END_SESSION);
            finish();
            break;
        }
        return false;
    }

    private void initFromIntent(Intent intent) {
        if (intent == null) {
            finish();
        }
        toneMsg = intent.getParcelableExtra("TEXT");
        settings = intent.getParcelableExtra("TONE");
    }

    private void sendResponse(int resId) {
        if(StkAppService.getInstance().haveEndSession()) {
            // ignore current command
            CatLog.d(this, "Ignore response, id is " + resId);
            return;
        }
        mbSendResp = true;
        Bundle args = new Bundle();
        args.putInt(StkAppService.OPCODE, StkAppService.OP_RESPONSE);
        args.putInt(StkAppService.RES_ID, resId);
        startService(new Intent(this, StkAppService.class).putExtras(args));
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
                ToneDialog.this.finish();
            }
        }
    }
}
