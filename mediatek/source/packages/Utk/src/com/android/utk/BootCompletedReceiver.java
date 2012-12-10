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

package com.android.utk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.cat.CatLog;

import android.telephony.ServiceState;
import android.util.Log;
import com.android.internal.telephony.Phone;
import android.telephony.TelephonyManager;


/**
 * Boot completed receiver. used to reset the app install state every time the
 * device boots.
 *
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private static final int GEMINI_SIM_1 = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        UtkAppInstaller appInstaller = UtkAppInstaller.getInstance();

        // make sure the app icon is removed every time the device boots.
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            CatLog.d(this, "UTK ACTION_BOOT_COMPLETED");
            Bundle args = new Bundle();
            args.putInt(UtkAppService.OPCODE, UtkAppService.OP_BOOT_COMPLETED);
            context.startService(new Intent(context, UtkAppService.class)
                    .putExtras(args));
        } else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
            CatLog.d(this, "UTK get ACTION_SIM_STATE_CHANGED");

            int SIMID = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY,-1);
            String SIMStatus = intent.getStringExtra(IccCard.INTENT_KEY_ICC_STATE);
            CatLog.d(this, "UTK [ACTION_SIM_STATE_CHANGED][simId] : " + SIMID);
            CatLog.d(this, "UTK [ACTION_SIM_STATE_CHANGED][SimStatus] : " + SIMStatus);
            if(SIMID == GEMINI_SIM_1){
                CatLog.d(this, "UTK [ACTION_SIM_STATE_CHANGED][GEMINI_SIM_1]");
                Bundle bundle = new Bundle();
                bundle.putString("affinity", "com.android.stk");
                final Intent it = new Intent();
                it.putExtras(bundle);

                boolean bUnInstall = true;
                if (((IccCard.INTENT_VALUE_ICC_READY).equals(SIMStatus))||((IccCard.INTENT_VALUE_ICC_IMSI).equals(SIMStatus))||((IccCard.INTENT_VALUE_ICC_LOADED).equals(SIMStatus))) {
                    bUnInstall = false;
                }

                int miSTKInstalled = appInstaller.getIsInstalled();
                
                if (bUnInstall && (miSTKInstalled == 1)) {
                    CatLog.d(this, " UTK ADD_RECENET_IGNORE");
                    it.setAction("android.intent.action.ADD_RECENET_IGNORE");
                    context.sendBroadcast(it);
                        
                    CatLog.d(this, "UTK get ACTION_SIM_STATE_CHANGED - unInstall");
                    //appInstaller.unInstall(context);
                } else if ((!bUnInstall) && (miSTKInstalled == 0)){
                    if (TelephonyManager.getDefault().getPhoneTypeGemini(Phone.GEMINI_SIM_1) == Phone.PHONE_TYPE_CDMA) {
                        CatLog.d(this, "UTK REMOVE_RECENET_IGNORE");
                        it.setAction("android.intent.action.REMOVE_RECENET_IGNORE");
                        context.sendBroadcast(it);
                        
                        CatLog.d(this, "UTK get ACTION_SIM_STATE_CHANGED - install");
                        appInstaller.install(context);
                    }
                }
            }
            CatLog.d(this, "UTK get ACTION_SIM_STATE_CHANGED  finish");
        }
    }
}