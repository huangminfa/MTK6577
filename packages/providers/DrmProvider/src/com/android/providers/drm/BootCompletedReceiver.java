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

package com.android.providers.drm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.drm.DrmManagerClient;
import android.telephony.TelephonyManager;

import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;

// read & save IMEI number of this device. for gemini, the IMEI of first SIM is retrieved.
public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Xlog.d(TAG, "onReceive : BOOT_COMPLETED received.");
        if (FeatureOption.MTK_DRM_APP) {

            // first check the existence of unique device id
            String id = DrmManagerClient.getDeviceId();
            Xlog.d(TAG, "onReceive: device id: "+id);

            if (null == id || id.equals("000000000000000")) {
                id = getIMEI(context); // use imei as unique device id
                Xlog.d(TAG, "onReceive: get IMEI number as: [" + id + "]");
                DrmManagerClient.saveDeviceId(id); // and save it
            }

            DrmManagerClient.loadClock();
            DrmManagerClient.updateClockBase();
        }
    }

    private String getIMEI(Context context) {
        TelephonyManager tm =
                (TelephonyManager)(context.getSystemService(Context.TELEPHONY_SERVICE));
        if (null == tm) {
            return new String("000000000000000"); // 15 '0' digits. normal imei was 15 digits
        }

        String imei = tm.getDeviceId();
        if (imei == null || imei.isEmpty()) {
            return new String("000000000000000");
        }
        return imei;
    }

}

