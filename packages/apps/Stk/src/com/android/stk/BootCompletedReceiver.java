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

import com.android.internal.telephony.cat.CatLog;
/**
 * Boot completed receiver. used to reset the app install state every time the
 * device boots.
 *
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();     
        StkAppInstaller appInstaller = StkAppInstaller.getInstance();
        StkAppService appService = StkAppService.getInstance();
        
        CatLog.d("BootCompleteReceiver", "[onReceive]+");
        // make sure the app icon is removed every time the device boots.
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Bundle args = new Bundle();
            args.putInt(StkAppService.OPCODE, StkAppService.OP_BOOT_COMPLETED);
            CatLog.d("BootCompleteReceiver", "[ACTION_BOOT_COMPLETED]");
            context.startService(new Intent(context, StkAppService.class)
                    .putExtras(args));
        }
        if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            boolean enabled = intent.getBooleanExtra("state", false);
            CatLog.d("BootCompleteReceiver", "[ACTION_AIRPLANE_MODE_CHANGED]");
            if(StkAppService.isSetupMenuCalled()) {
                CatLog.d("BootCompleteReceiver", "[ACTION_AIRPLANE_MODE_CHANGED][SetupMenuCalled]");
                Bundle bundle = new Bundle();
                bundle.putString("affinity", "com.android.stk");
                final Intent it = new Intent();
                it.putExtras(bundle);
                
                // AirPlane mode: uninstall
                if(enabled == true) {
                    it.setAction("android.intent.action.ADD_RECENET_IGNORE");
                    context.sendBroadcast(it);
                    CatLog.d("BootCompleteReceiver", "[ACTION_AIRPLANE_MODE_CHANGED][start unInstall]+");
                    appInstaller.unInstall(context);
                    CatLog.d("BootCompleteReceiver", "[ACTION_AIRPLANE_MODE_CHANGED][start unInstall]-");
                    if(appService != null) {
                        appService.setUserAccessState(false);
                    }
                } else {
                    it.setAction("android.intent.action.REMOVE_RECENET_IGNORE");
                    context.sendBroadcast(it);
                    CatLog.d("BootCompleteReceiver", "[ACTION_AIRPLANE_MODE_CHANGED][start Install]+");
                    appInstaller.install(context);
                    CatLog.d("BootCompleteReceiver", "[ACTION_AIRPLANE_MODE_CHANGED][start Install]-");
                }
            }
        }
        CatLog.d("BootCompleteReceiver", "[onReceive]-");
    }
}
