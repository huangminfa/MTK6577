/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.email.service;

import com.android.emailcommon.Logging;
import com.android.emailcommon.service.EmailExternalConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * The broadcast receiver.
 */
public class EmailExternalReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logging.i("EmailExternalReceiver", " *** EmailExternal receive:" + action + "*** ");
        if (EmailExternalConstants.OMACP_SETTING_ACTION.equals(action)) {
            Intent omacpIntent = new Intent(context, EmailExternalOmacpService.class);
            omacpIntent.putExtra(Intent.EXTRA_INTENT, intent);
            context.startService(omacpIntent);
        } else if (EmailExternalConstants.OMACP_CAPABILITY_ACTION.equals(action)) {
            EmailExternalOmacpService.buildCapabilityResultToOmacp(context);
        } else if (action.equals(EmailExternalConstants.ACTION_DIRECT_SEND)
                || action.equals(EmailExternalConstants.ACTION_UPDATE_INBOX)) {
            Intent serviceIntent = new Intent(context, EmailExternalService.class);
            serviceIntent.putExtra(Intent.EXTRA_INTENT, intent);
            context.startService(serviceIntent);
        } 
//        else if (action.equals(EmailExternalConstants.ACTION_BACKGROUND_SEND)
//              || action.equals(EmailExternalConstants.ACTION_BACKGROUND_SEND_MULTIPLE)) {
//              new EmailSendBackground(context, intent);
//          }
    }

}
