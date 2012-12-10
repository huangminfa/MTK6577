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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import android.os.RemoteException;
import com.mediatek.CellConnService.CellConnMgr;
import android.telephony.TelephonyManager;
import android.os.ServiceManager;
import com.android.internal.telephony.ITelephony;
import com.mediatek.featureoption.FeatureOption;

/**
 * Launcher class. Serve as the app's MAIN activity, send an intent to the
 * UtkAppService and finish.
 *
 */
public class UtkLauncherActivity extends Activity {
    private static final String ACTION_FINISH_ACTIVITY = "finish activiy";

    private static final int REQUEST_TYPE = 302;
    BroadcastReceiver mReceiver = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCellMgr.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCellMgr.unregister();
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle args = new Bundle();

        if((hasIccCard(0) != false) && (IccCardReady(0) == false)) {
            int mSlot = 0;
            int nRet1 = mCellMgr.handleCellConn(mSlot, REQUEST_TYPE);
        } else {
            args.putInt(UtkAppService.OPCODE, UtkAppService.OP_LAUNCH_APP);
            startService(new Intent(this, UtkAppService.class).putExtras(args));
        }
        finish();
    }

    //deal with SIM status
    private Runnable serviceComplete = new Runnable() {
        public void run() {
            int nRet = mCellMgr.getResult();
            if (mCellMgr.RESULT_ABORT == nRet) {
                finish();
                return;
            } else {
                finish();
                return;
            }
        }
    };

    private CellConnMgr mCellMgr = new CellConnMgr(serviceComplete);

    public static boolean hasIccCard(int slot) {
        boolean bRet = false;

        if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
            try {
                final ITelephony iTelephony = ITelephony.Stub
                      .asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                if (null != iTelephony) {
                    bRet = iTelephony.isSimInsert(slot);
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                final ITelephony iTelephony = ITelephony.Stub
                      .asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                if (null != iTelephony) {
                    bRet = iTelephony.isSimInsert(0);
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }

        return bRet;
    }   

    public static boolean IccCardReady(int slot) {
        boolean bRet = false;
        if (true == FeatureOption.MTK_GEMINI_SUPPORT) {					
            bRet = (TelephonyManager.SIM_STATE_READY 
                 == TelephonyManager.getDefault().getSimStateGemini(slot));					
        } else {
            bRet = (TelephonyManager.SIM_STATE_READY 
                 == TelephonyManager.getDefault().getSimState());
        }
        return bRet;
     }

}
