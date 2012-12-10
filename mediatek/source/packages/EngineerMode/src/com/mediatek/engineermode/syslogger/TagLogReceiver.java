/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.engineermode.syslogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
/**
 * To receive exception broadcast and start main service.
 * @author MTK54043
 *
 */
public class TagLogReceiver extends BroadcastReceiver {

    private static final String TAG = "Syslog_taglog";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null) {
            return;
        }

        final SharedPreferences preferences = context.getSharedPreferences(
                SysUtils.TAGLOG_SWITCH, TagLogSwitch.MODE_WORLD_WRITEABLE);

        String buildType = Build.TYPE;
        if (buildType != null) {
            Log.d(TAG, "Build type :" + buildType);            
        }
        if (buildType != null && buildType.trim().equalsIgnoreCase("user")) {
            Log.d(TAG, "This is a user load!");
            if (preferences.getInt(SysUtils.TAGLOG_SWITCH_KEY, -1) == -1) {
                return;
            }
        }

        if (SysUtils.ACTION_TAGLOG_SWITCH.equals(intent.getAction())) {
            final Editor edit = preferences.edit();
            edit.putInt(SysUtils.TAGLOG_SWITCH_KEY, 0);
            edit.commit();
            Log.d(TAG, "--> TagLogReceiver --> Close Taglog from broadcast");
            return;
        }

        if (preferences.getInt(SysUtils.TAGLOG_SWITCH_KEY, -1) == 0) {
            Log.d(TAG, "--> Tag Log switch is closed");
            return;
        }

        if (SysUtils.ACTION_EXP_HAPPENED.equals(intent.getAction())) {
            Log.d(TAG, "--> TagLogReceiver --> Start Service");
            Intent serviceIntent = new Intent(context, TagLogService.class);
            Bundle extras = intent.getExtras();
            if (extras == null) {
                Log.e(TAG, "--> intent.getExtras() == null");
                return;
            } else {
                serviceIntent.putExtras(extras);
            }
            context.startService(serviceIntent);
        }
    }
}
