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

package com.android.DataServiceTestCases;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.FunctionTest.CommandResult;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;

public class DataServiceUtil {
    static final String TAG = "DataServiceUtil";

	static private Object mLock = new Object();
	
    static private Context AppContext = com.android.FuncitonTester.FunctionTesterActivity
            .getCurrentContext();
	static private TelephonyManager mTelephonyManager = (TelephonyManager) AppContext.getSystemService(Context.TELEPHONY_SERVICE);	
	static private WifiManager mWifiManager = (WifiManager) AppContext.getSystemService(Context.WIFI_SERVICE);
	static private ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
	static private ConnectivityManager mConnectivityManager = (ConnectivityManager) AppContext.getSystemService(Context.CONNECTIVITY_SERVICE);  
	
    private static final String[] APN_PROJECTION = {
        Telephony.Carriers.TYPE,            // 0
        Telephony.Carriers.MMSC,            // 1
        Telephony.Carriers.MMSPROXY,        // 2
        Telephony.Carriers.MMSPORT          // 3
    };
    
    private static final int COLUMN_TYPE         = 0;
    private static final int COLUMN_MMSC         = 1;
    private static final int COLUMN_MMSPROXY     = 2;
    private static final int COLUMN_MMSPORT      = 3;
   
    static private int mServiceState;
	public DataServiceUtil() {
	}
	
    static public void Init() {
		mTelephonyManager.listen(mPhoneStateListener, 
				PhoneStateListener.LISTEN_DATA_CONNECTION_STATE | 
				PhoneStateListener.LISTEN_SERVICE_STATE );
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION_IMMEDIATE);
		AppContext.registerReceiver(mConnectionStateReceiver, intentFilter);
	}
	
	static public void Deinit() {
		AppContext.unregisterReceiver(mConnectionStateReceiver);
	}
	
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
	
    static public NetworkInfo getMobileConnInfo() {
        return mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    }

    static public NetworkInfo getMmsConnInfo() {
        return mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
    }

    static public NetworkInfo getWifiConnInfo() {
        return mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    }

    static public long getCurrentGprsSetting() {
        long newGprsValue = Settings.System.getLong(AppContext.getContentResolver(),
                "gprs_connection_sim_setting",
                -5);
        return newGprsValue;
    }

	static public CommandResult switchOffOnData() {
        CommandResult result = new CommandResult("switchOffOnData");
        long gprsValue = getCurrentGprsSetting();
		
		if (!switchOffData().getResult()) {
		    result.setResult(false);
		    return result;
		}
		if (!switchOnData(gprsValue).getResult()) {
            result.setResult(false);
            return result;
        }
		result.setResult(true);
		return result;
	}

	static public CommandResult switchOnData(long gprsValue) {
        NetworkInfo info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        CommandResult result = new CommandResult("switchOnData");
        if (!info.isConnected() && info.isAvailable() &&
                info.getReason().equals("dataDisabled")) {
            Intent intent = new Intent("android.intent.action.DATA_DEFAULT_SIM");
            intent.putExtra("simid", gprsValue);

            AppContext.sendBroadcast(intent);
            while (waitLock(mLock, result.getStartTime(), 180000)) {
                info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                long newGprsValue = getCurrentGprsSetting();
                if (newGprsValue == gprsValue &&
                        info.isConnected()) {
                    result.setResult(true);
                    return result;
                }
            }
            // Timeout
            result.setResult(false, true);
        } else {
            result.setResult(false);
        }
		return result;
	}

	static public CommandResult switchOffData() {
        NetworkInfo info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        CommandResult result = new CommandResult("switchOffData");
        if (info != null && TelephonyManager.DATA_CONNECTED == mTelephonyManager.getDataState()) {
            Intent intent = new Intent("android.intent.action.DATA_DEFAULT_SIM");
            intent.putExtra("simid", -5);
            AppContext.sendBroadcast(intent);
            while (waitLock(mLock, result.getStartTime(), 180000)) {
                info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (info.isConnected() == false) {
                    result.setResult(true);
                    return result;
                }
            }
            // Timeout
            result.setResult(false, true);
        } else {
            result.setResult(false);
        }
		return result;
	}

    static public CommandResult switchSimData(long gprsValue) {
        NetworkInfo info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        CommandResult result = new CommandResult("switchSimData");
        if (info.isAvailable()) {
            Intent intent = new Intent("android.intent.action.DATA_DEFAULT_SIM");
            intent.putExtra("simid", gprsValue);

            AppContext.sendBroadcast(intent);
            while (waitLock(mLock, result.getStartTime(), 180000)) {
                info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                Log.d(TAG, "switchSimData1 info=" + info.toString());
                long newGprsValue = getCurrentGprsSetting();
                if (newGprsValue == gprsValue &&
                        info.isConnected()) {
                    result.setResult(true);
                    return result;
                }
            }
            // Timeout
            result.setResult(false, true);
        } else {
            result.setResult(false);
        }
        return result;
	}
	
    static public CommandResult switchNetworkMode(int oldNetworkModeValue, int networkModeValue) {
        NetworkInfo info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        CommandResult result = new CommandResult("switchNetworkMode");
        if (info.isConnected()) {
            Intent intent = new Intent("com.android.phone.NETWORK_MODE_CHANGE", null);
            intent.putExtra("com.android.phone.OLD_NETWORK_MODE", oldNetworkModeValue);
            intent.putExtra("com.android.phone.NETWORK_MODE_CHANGE", networkModeValue);
            intent.putExtra("simId", info.getSimId());
            AppContext.sendBroadcast(intent);
            // Data drop
            while (waitLock(mLock, result.getStartTime(), 3000)) {
                info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (!info.isConnected()) {
                    break;
                }
            } // Data reconnect
            while (waitLock(mLock, result.getStartTime(), 180000)) {
                info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (info.isConnected()) {
                    result.setResult(true);
                    return result;
                }
            }
            // Timeout
            result.setResult(false, true);
        } else {
            result.setResult(false);
        }
        return result;
	}
	
	
    static public CommandResult set3GCapabilitySIM(int simId) {
        NetworkInfo info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        CommandResult result = new CommandResult("set3GCapabilitySIM");
        if (info.isConnected() && get3GCapabilitySIM() != simId) {
            try {
                iTelephony.set3GCapabilitySIM(simId);
            } catch (RemoteException e) {
                e.printStackTrace();
                result.setResult(false);
                return result;
            }
            // Data drop
            while (waitLock(mLock, result.getStartTime(), 3000)) {
                info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (!info.isConnected()) {
                    break;
                }
            } // Data reconnect
            while (waitLock(mLock, result.getStartTime(), 180000)) {
                info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (get3GCapabilitySIM() == simId &&
                        info.isConnected()) {
                    result.setResult(true);
                    return result;
                }
            }
            // Timeout
            result.setResult(false, true);
        } else {
            result.setResult(false);
        }
        return result;
    }
	
	static public int get3GCapabilitySIM() {
	    int simId = -1;
	    try {
	        simId = iTelephony.get3GCapabilitySIM();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
	    return simId;
	}
	
	
    static public CommandResult switchOnWifi() {
        NetworkInfo info = null;
        CommandResult result = new CommandResult("switchOnWifi");
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
            while (waitLock(mLock, result.getStartTime(), 180000)) {
                info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (mWifiManager.isWifiEnabled() && info.isConnected()) {
                    result.setResult(true);
                    return result;
                }
            }
            // Timeout
            result.setResult(false, true);
        } else {
            result.setResult(false);
        }
        return result;
	}
	
    static public CommandResult switchOffWifi() {
        NetworkInfo mobileInfo = mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        long gprsValue = getCurrentGprsSetting();
        boolean mobileDataActivited = (mobileInfo.isAvailable() && gprsValue > -1);
        NetworkInfo wifiInfo = null;
        CommandResult result = new CommandResult("switchOffWifi");
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
            while (waitLock(mLock, result.getStartTime(), 180000)) {
                wifiInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (mobileDataActivited) {
                    mobileInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    if (mobileInfo.isConnected()) {
                        result.setResult(true);
                        return result;
                    }
                } else if (!mWifiManager.isWifiEnabled() && !wifiInfo.isConnected()) {
                    result.setResult(true);
                    return result;
                }
            }
            // Timeout
            result.setResult(false, true);
        } else {
            result.setResult(false);
        }
        return result;
	}
	
    static public boolean isWifiOn() {
        boolean isWifiOn = mWifiManager.isWifiEnabled();
        return isWifiOn;
	}

    static public boolean isAnyConnectionAvailable() {
        Log.d(TAG, "isAnyConnectionAvailable");
        NetworkInfo[] infos = new NetworkInfo[2];
        infos[0] = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        infos[1] = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        for (NetworkInfo info : infos) {
            Log.d(TAG, "info=" + infos.toString());
            if (info != null && info.isConnected()) {
                return true;
            }
        }
        return false;
    }
	
    static public CommandResult enableMms(int radioNum) {
        NetworkInfo info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
        CommandResult result = new CommandResult("enableMms");
        if (!info.isConnected()) {
            int startResult = mConnectivityManager.startUsingNetworkFeatureGemini(
                    ConnectivityManager.TYPE_MOBILE, Phone.FEATURE_ENABLE_MMS, radioNum);
            switch (startResult) {
                case Phone.APN_ALREADY_ACTIVE:
                    break;
                case Phone.APN_REQUEST_STARTED:
                    while (waitLock(mLock, result.getStartTime(), 180000)) {
                        info = mConnectivityManager
                                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
                        if (info.isConnected()) {
                            result.setResult(true);
                            return result;
                        }
                    }
                    // Timeout
                    result.setResult(false, true);
                    break;
                case Phone.APN_TYPE_NOT_AVAILABLE:
                case Phone.APN_REQUEST_FAILED:
                default:
                    break;
            }
            result.setResult(false);
        } else {
            result.setResult(false);
        }
        return result;
    }
    
    static public CommandResult disableMms(int radioNum) {
        NetworkInfo info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
        CommandResult result = new CommandResult("disableMms");
        if (info.isConnected()) {
            int startResult = mConnectivityManager.stopUsingNetworkFeatureGemini(
                    ConnectivityManager.TYPE_MOBILE, Phone.FEATURE_ENABLE_MMS, radioNum);
            switch (startResult) {
                case Phone.APN_ALREADY_ACTIVE:
                    break;
                case Phone.APN_REQUEST_STARTED:
                    while (waitLock(mLock, result.getStartTime(), 180000)) {
                        info = mConnectivityManager
                                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
                        if (!info.isConnected()) {
                            result.setResult(true);
                            return result;
                        }
                    }
                    // Timeout
                    result.setResult(false, true);
                    break;
                case Phone.APN_TYPE_NOT_AVAILABLE:
                case Phone.APN_REQUEST_FAILED:
                default:
                    break;
            }
            result.setResult(false);
        } else {
            result.setResult(false);
        }
        return result;
    }
    
    static public boolean requestRouteToHost(int networkType, String addr) {
        boolean ret = false;
        ret = mConnectivityManager.requestRouteToHost(networkType, lookupHost(addr));
        return ret;
    }

    public static int lookupHost(String hostname) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            return -1;
        }
        byte[] addrBytes;
        int addr;
        addrBytes = inetAddress.getAddress();
        addr = ((addrBytes[3] & 0xff) << 24)
                | ((addrBytes[2] & 0xff) << 16)
                | ((addrBytes[1] & 0xff) << 8)
                | (addrBytes[0] & 0xff);
        return addr;
    }

    public static int ipToInt(String addr) {
        String[] addrArray = addr.split("\\.");
        int num = 0;
        for (int i = 0; i < addrArray.length; i++) {
            int power = 3 - i;
            num += ((Integer.parseInt(addrArray[i]) % 256 * Math.pow(256, power)));
        }
        return num;
    }

    static private boolean isValidApnType(String types, String requestType) {
        // If APN type is unspecified, assume APN_TYPE_ALL.
        if (TextUtils.isEmpty(types)) {
            return true;
        }

        for (String t : types.split(",")) {
            if (t.equals(requestType) || t.equals(Phone.APN_TYPE_ALL)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isProxySet(String proxyAddress) {
        return (proxyAddress != null) && (proxyAddress.trim().length() != 0);
    }
    
    public static String getConnectedMmsProxyHost() {
        NetworkInfo info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
        if (info == null) {
            return null;
        }
        
        String apnName = info.getExtraInfo();
        int slotId = info.getSimId();
        
        String serviceCenter = null;
        String proxyAddress = null;
        int proxyPort;
        String proxyHostAddress = null;
        
        String selection = (apnName != null)?
                Telephony.Carriers.APN + "='" + apnName.trim() + "'": null;
        Cursor cursor = null;
        if (Phone.GEMINI_SIM_1 == slotId) {
            cursor = SqliteWrapper.query(AppContext, AppContext.getContentResolver(),
                                Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "current"),
                                APN_PROJECTION, selection, null, null);
        } else if (Phone.GEMINI_SIM_2 == slotId){
            cursor = SqliteWrapper.query(AppContext, AppContext.getContentResolver(),
                                Uri.withAppendedPath(Telephony.Carriers.GeminiCarriers.CONTENT_URI, "current"),
                                APN_PROJECTION, selection, null, null);
        } else {
            Log.v("DataServiceTester", "getConnectedMmsProxyHost Invalide slot id:" + slotId);
        }

        if (cursor == null) {
            Log.v("DataServiceTester", "Apn is not found in Database!");
            return null;
        }

        boolean sawValidApn = false;
        try {
            while (cursor.moveToNext() && TextUtils.isEmpty(serviceCenter)) {
                // Read values from APN settings
                if (isValidApnType(cursor.getString(COLUMN_TYPE), Phone.APN_TYPE_MMS)) {
                    sawValidApn = true;
                    serviceCenter = cursor.getString(COLUMN_MMSC) != null ? cursor.getString(COLUMN_MMSC).trim():null;
                    proxyAddress = cursor.getString(COLUMN_MMSPROXY);
                    Log.v("DataServiceTester", "Proxy=" + proxyAddress);
                    proxyHostAddress = proxyAddress;
                    if (isProxySet(proxyAddress)) {
                        String portString = cursor.getString(COLUMN_MMSPORT);
                        Log.v("DataServiceTester", "Port=" + portString);
                        try {
                            proxyPort = Integer.parseInt(portString);
                            proxyHostAddress = proxyHostAddress + ":" + proxyPort;
                        } catch (NumberFormatException e) {
                            if (TextUtils.isEmpty(portString)) {
                                Log.w("DataServiceTester", "mms port not set!");
                            } else {
                                Log.e("DataServiceTester", "Bad port number format: " + portString, e);
                            }
                        }
                    }
                }
            }
        } finally {
            cursor.close();
        }

        Log.v("DataServiceTester", "APN setting: MMSC: " + serviceCenter + " looked for: " + selection);

        if (sawValidApn && TextUtils.isEmpty(serviceCenter)) {
            Log.e("DataServiceTester", "Invalid APN setting: MMSC is empty");
            return null;
        }
        return proxyAddress;
    }

	static public PhoneStateListener mPhoneStateListener = new PhoneStateListener(){
		@Override
  	  	public void onServiceStateChanged (ServiceState serviceState) {
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
            if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                if(wifiState == WifiManager.WIFI_STATE_ENABLING) {  
                } else if(wifiState == WifiManager.WIFI_STATE_ENABLED) {  
                    releaseLock(mLock);
                } else if(wifiState == WifiManager.WIFI_STATE_DISABLED) {  
                    releaseLock(mLock);
                } 
            } else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {         
            	if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                    releaseLock(mLock);
                } else {
                    releaseLock(mLock);
            	} 
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION_IMMEDIATE)) {         
                NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if ((networkInfo != null) && (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE_MMS)) {
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        releaseLock(mLock);

                    } else {
                        releaseLock(mLock);
                    }
                } else if ((networkInfo != null) && (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        releaseLock(mLock);
                    } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                        releaseLock(mLock);
                    }
                }
            }
		}
    };
}
