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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * event receiver. used to download UserActivity,Language change event to SAT
 * framework device
 * 
 */
public class EventReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final Context c = context;
        final String action = intent.getAction();
        Thread r = new Thread() {
            public void run() {
                // BroadcastReceiver from WindowManagerService
                if (action.equals("android.intent.action.stk.USER_ACTIVITY")) {
                    sendDownloadEvent(c,StkAppService.EVDL_ID_USER_ACTIVITY);
                } else if (action.equals(Intent.ACTION_LOCALE_CHANGED)) {
                    sendDownloadEvent(c,StkAppService.EVDL_ID_LANGUAGE_SELECT);
                } else if (action.equals("android.intent.action.stk.BROWSER_TERMINATION")) {
                    sendDownloadEvent(c,StkAppService.EVDL_ID_BROWSER_TERMINATION);
                } else if (action.equals("android.intent.action.stk.IDLE_SCREEN_AVAILABLE")) {
                    sendDownloadEvent(c,StkAppService.EVDL_ID_IDLE_SCREEN_AVAILABLE);
                }
            }
        };
        r.start();
    }

    private void sendDownloadEvent(Context context,int evdlId){
        Bundle args = new Bundle();
        args.putInt(StkAppService.OPCODE, StkAppService.OP_EVENT_DOWNLOAD);
        args.putInt(StkAppService.EVDL_ID, evdlId);
        context.startService(new Intent (context, StkAppService.class).putExtras(args));
    }
}
