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

package com.android.SimCardTest;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.FuncitonTester.FunctionTesterActivity;
import com.android.FunctionTest.CommandResult;
import com.android.FunctionTest.TestCase;

public class SimCardUtil {
    static final String TAG = "SimCardUtil";

    static private Object mLock = new Object();
    static private Context AppContext = FunctionTesterActivity.getCurrentContext();
    static private TelephonyManager mTelephonyManager = (TelephonyManager) AppContext
            .getSystemService(Context.TELEPHONY_SERVICE);

    static private int mServiceState;

    public SimCardUtil() {
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

    static public CommandResult readSimAdnTest(TestCase testCase) {
        CommandResult result = new CommandResult("SimAdnTest");
        try {
            Uri simUri = Uri.parse("content://icc/adn");
            Cursor cursorSim = AppContext.getContentResolver()
                    .query(simUri, null, null, null, null);
            while (cursorSim.moveToNext()) {
                String ClsSimPhonename = cursorSim.getString(cursorSim.getColumnIndex("name"));
                String ClsSimphoneNo = cursorSim.getString(cursorSim.getColumnIndex("number"));
                testCase.reportMessage("SimAdnTest ClsSimPhonename=" + ClsSimPhonename
                        + " ClsSimphoneNo="
                        + ClsSimphoneNo);
            }
        } catch(Exception e) {     
            e.printStackTrace(); 
            result.setResult(false);
            return result;
        }
        
        result.setResult(true);
        return result;
    }

    static public CommandResult insertSimAdnTest() {
        CommandResult result = new CommandResult("SimAdnTest");
        try {
            // Uri dstSimUri = SimCardUtils.SimUri.getSimUri(0);
            Uri simUri = Uri.parse("content://icc/adn1");
            ContentValues cv = new ContentValues();
            cv.put("name", "auto test");
            cv.put("number", "+886988123456");
            Uri uri = AppContext.getContentResolver().insert(simUri, cv);

            Cursor cursorSim = AppContext.getContentResolver()
                    .query(uri, null, null, null, null);
            while (cursorSim.moveToNext()) {
                String ClsSimPhonename = cursorSim.getString(cursorSim.getColumnIndex("name"));
                String ClsSimphoneNo = cursorSim.getString(cursorSim.getColumnIndex("number"));
                if (ClsSimPhonename.equals("auto test") && ClsSimphoneNo.equals("886988123456")) {
                    result.setResult(true);
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setResult(false);
            return result;
        }

        result.setResult(false);
        return result;
    }

}
