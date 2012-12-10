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

package com.android.CallControlTestCases;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.FuncitonTester.FunctionTesterActivity;
import com.android.FunctionTest.CommandResult;
import com.android.internal.telephony.ITelephony;


public class CallControlUtil {
    static final String TAG = "CallControlUtil";

    static private Object mLock = new Object();
    static private Context AppContext = FunctionTesterActivity.getCurrentContext();
    static private TelephonyManager mTelephonyManager = (TelephonyManager) AppContext.getSystemService(Context.TELEPHONY_SERVICE);  
    static private ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));

    public CallControlUtil() {
    }

    static public void Init() {
        mTelephonyManager.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
                        PhoneStateListener.LISTEN_CALL_STATE);
        IntentFilter intentFilter = new IntentFilter();
        AppContext.registerReceiver(mConnectionStateReceiver, intentFilter);
    }

    static public void Deinit() {
        AppContext.unregisterReceiver(mConnectionStateReceiver);
    }

    static public PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            // default implementation empty
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
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

    static public CommandResult dial(String number) {
        CommandResult result = new CommandResult("Dial " + number);
        String dialNum = "tel:" + number;
        Intent intent = new Intent(Intent.ACTION_CALL,
                Uri.parse(dialNum));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AppContext.startActivity(intent);

        while (waitLock(mLock, result.getStartTime(), 180000)) {
            try {
                if (iTelephony.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
                    try{
                         Thread.sleep(3000);
                    }catch(InterruptedException er){
                         break;
                	  }
                	  result.setResult(true);
                    return result;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                result.setResult(false);
                return result;
            }
        }
        // Timeout
        result.setResult(false, true);
        return result;
    }
    
    static public CommandResult waitingForRingingCall() {
        CommandResult result = new CommandResult("waitingForRingingCall");
        while (waitLock(mLock, result.getStartTime(), 180000)) {
            try {
                if (iTelephony.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
                    result.setResult(true);
                    return result;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                result.setResult(false);
                return result;
            }
        }
        // Timeout
        result.setResult(false, true);
        return result;
    }
    
    
    static public CommandResult answerRingingCall() {
        CommandResult result = new CommandResult("answerRingingCall");
        try {
            if (iTelephony.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
                iTelephony.answerRingingCallGemini(0);
                while (waitLock(mLock, result.getStartTime(), 180000)) {
                    if (iTelephony.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
                        result.setResult(true);
                        return result;
                    }
                }
                // Timeout
                result.setResult(false, true);
                return result;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            result.setResult(false);
            return result;
        }
        result.setResult(false);
        return result;
    }


    static public CommandResult hangupAll() {
        CommandResult result = new CommandResult("hangupAll");
        try {
            if (iTelephony.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                iTelephony.endCall();
                while (waitLock(mLock, result.getStartTime(), 180000)) {
                    if (iTelephony.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                        result.setResult(true);
                        return result;
                    }
                }
                // Timeout
                result.setResult(false, true);
                return result;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            result.setResult(false);
            return result;
        }
        result.setResult(false);
        return result;
    }
}
