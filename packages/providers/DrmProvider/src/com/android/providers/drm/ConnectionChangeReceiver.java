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
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

import com.android.internal.telephony.Phone;
import com.mediatek.featureoption.FeatureOption;

import java.net.InetAddress;
import java.net.UnknownHostException;

// when connection is available, sync secure timer
public class ConnectionChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectionChangeReceiver";

    private static Thread thrd = null; // the thread is static so that only one thread is running to do SNTP sync
    private static final String NETWORK_TYPE_MOBILE_NET =
            Phone.FEATURE_ENABLE_NET;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "onReceive : CONNECTIVITY_CHANGE received.");
        if (FeatureOption.MTK_DRM_APP) {

            if (DrmManagerClient.checkSecureTimerStatus()) {
                Log.d(TAG, "onReceive : secure timer is already in valid state.");
                return;
            }

            if (null == thrd || (null != thrd && !thrd.isAlive())) {
                Log.v(TAG, "onReceive : thread is not running, launch one to do SNTP synchronization.");

                // check if the network is usable
                ConnectivityManager conManager
                        = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
                if (null == networkInfo) {
                    Log.e(TAG, "onReceive : invalid active network info.");
                    return;
                }

                if (networkInfo.isAvailable()) {
                    Log.d(TAG, "onReceive : type of active network info: [" + networkInfo.getType() + "].");
                    launchSimpleThread(conManager, networkInfo.getType());
                } else {
                    Log.e(TAG, "onReceive : the active network is not available.");
                }
            } else {
                Log.w(TAG, "onReceive : the thread for SNTP synchronization is running.");
            }
        }
    }

    private void launchSimpleThread(
            final ConnectivityManager conManager, final int type) {
        thrd = new Thread(new Runnable() {
            public void run() {
                InetAddress result = checkRouteToHost(conManager, type);
                if (null != result) {
                    int oft = Ntp.sync(result);
                    Log.d(TAG, "onReceive: synchronization result, the utc time offset: [" + oft + "].");
                    DrmManagerClient.updateClock(oft);
                }
            }
        });
        thrd.start();
    }

    // modify these SNTP host servers, for different countries.
    private static String[] hostList = new String[] {
        "hshh.org",
        "t1.hshh.org",
        "t2.hshh.org",
        "t3.hshh.org",
        "clock.via.net"
    };

    private InetAddress checkRouteToHost(ConnectivityManager conManager, int type) {
        Log.v(TAG, "==== check if there's available route to SNTP servers ====");

        InetAddress result = null;
        if (conManager != null) {
            int size = hostList.length;
            for (int i = 0; i < size; i++) {
                int address = 0;
                InetAddress addr = null;
                try {
                    Log.d(TAG, "get host address by name: [" + hostList[i] + "].");
                    addr = InetAddress.getByName(hostList[i]);
                    address = ipToInt(addr.getHostAddress());
                } catch (UnknownHostException e) {
                    Log.e(TAG, "caught UnknownHostException");
                    continue;
                }

                Log.d(TAG, "request route for host: [" + hostList[i] + "].");
                if (conManager.requestRouteToHost(type, address)) {
                    Log.d(TAG, "request route for host success.");
                    result = addr;
                    break;
                }
                Log.d(TAG, "request route for host failed.");
            }
        }
        return result;
    }

    private int ipToInt(String ipAddress) {
        if (ipAddress == null) {
            return -1;
        }

        String[] addrArray = ipAddress.split("\\.");
        int size = addrArray.length;
        if (size != 4) {
            return -1;
        }

        int[] addrBytes = new int[size];
        try {
            for (int i = 0; i < size; i++) {
                addrBytes[i] = Integer.parseInt(addrArray[i]);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }

        Log.v(TAG, "ipToInt: a[0] = " + addrBytes[0] + ", a[1] = " + addrBytes[1] + ", a[2] = " + addrBytes[2] + ", a[3] = " + addrBytes[3]);
        return ((addrBytes[3] & 0xff) << 24)
               | ((addrBytes[2] & 0xff) << 16)
               | ((addrBytes[1] & 0xff) << 8)
               | (addrBytes[0] & 0xff);
    }
}
