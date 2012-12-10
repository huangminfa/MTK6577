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
import android.util.Log;

import com.mediatek.featureoption.FeatureOption;

// when user changes devices time, update the offset between device time and real world time.
public class TimeChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "TimeChangedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive : TIME_SET received.");
        if (FeatureOption.MTK_DRM_APP) {
            DrmManagerClient.updateClockOffset();
        }
    }
}

