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

package com.android.NetworkTestCases;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.FuncitonTester.FunctionTesterActivity;
import com.android.FunctionTest.CommandResult;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gemini.MTKPhoneFactory;

public class NetworkUtil {
    static final String TAG = "NetworkUtil";

    static private Object mLock = new Object();
    static private Context AppContext = FunctionTesterActivity.getCurrentContext();
    static private TelephonyManager mTelephonyManager = (TelephonyManager) AppContext
            .getSystemService(Context.TELEPHONY_SERVICE);
    static private ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager
            .getService("phone"));

    static private int mServiceState;

    public NetworkUtil() {
    }

    static public void Init() {
        mTelephonyManager.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_SERVICE_STATE);
        IntentFilter intentFilter = new IntentFilter();
        AppContext.registerReceiver(mConnectionStateReceiver, intentFilter);
    }

    static public void Deinit() {
        AppContext.unregisterReceiver(mConnectionStateReceiver);
    }

    static public PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            mServiceState = serviceState.getState();
            Log.d(TAG, "onServiceStateChanged mServiceState=" + mServiceState);
            releaseLock(mLock);
        }
    };

    static public final BroadcastReceiver mConnectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive action=" + action);
        }
    };

    static public void abortTest() {
        releaseLock(mLock);
    }

    static public void releaseLock(Object lock) {
        Log.d(TAG, "releaseLock");
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    static public boolean waitLock(Object lock, long startTime, long mTimeout) {
        final long timePassed = System.currentTimeMillis() - startTime;
        final long timeRemained = mTimeout - timePassed;
        if (timeRemained < 1) {
            return false;
        }
        synchronized (lock) {
            try {
                lock.wait(timeRemained);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    public static void setAirplaneMode(boolean status) {
        boolean isAirplaneModeOn = isAirplaneModeOn(AppContext);
        if ((status && isAirplaneModeOn) || (!status && !isAirplaneModeOn)) {
            return;
        }
        int mode = status ? 1 : 0;
        Settings.System.putInt(AppContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON
                , mode);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra("state", status);
        AppContext.sendBroadcast(intent);
    }

    static public CommandResult setAirplaneModeOn() {
        CommandResult result = new CommandResult("setAirplaneModeOn");
        setAirplaneMode(true);

        while (waitLock(mLock, result.getStartTime(), 180000)) {
            if (mServiceState == ServiceState.STATE_POWER_OFF) {
                    result.setResult(true);
                    return result;
            } else {
                result.setResult(false);
                return result;
            }
        }
        // Timeout
        result.setResult(false, true);
        return result;
    }

    static public CommandResult setAirplaneModeOff() {
        CommandResult result = new CommandResult("setAirplaneModeOff");
        setAirplaneMode(false);

        while (waitLock(mLock, result.getStartTime(), 180000)) {
            if (mServiceState == ServiceState.STATE_OUT_OF_SERVICE) {
                continue;
            }
            if (mServiceState == ServiceState.STATE_IN_SERVICE) {
                result.setResult(true);
                return result;
            }
        }
        // Timeout
        result.setResult(false, true);
        return result;
    }

    static class PhoneHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            releaseLock(mLock);
        }
    };

    static PhoneHandler mHandler = new PhoneHandler();

    static public CommandResult availableNetworksTest() {
        CommandResult result = new CommandResult("availableNetworksTest");
        Phone sProxyPhone = MTKPhoneFactory.getDefaultPhone();
        sProxyPhone.getAvailableNetworks(mHandler.obtainMessage());

        while (waitLock(mLock, result.getStartTime(), 180000)) {
            if (mServiceState == ServiceState.STATE_OUT_OF_SERVICE) {
                continue;
            }
            if (mServiceState == ServiceState.STATE_IN_SERVICE) {
                result.setResult(true);
                return result;
            }
        }
        // Timeout
        result.setResult(false, true);
        return result;
    }

}
