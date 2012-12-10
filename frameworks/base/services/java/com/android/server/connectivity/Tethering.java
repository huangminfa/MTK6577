/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.server.connectivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.INetworkManagementEventObserver;
import android.net.INetworkStatsService;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.wifi.IWifiManager;
import android.net.wifi.WifiConfiguration;
import android.os.Binder;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

import com.android.internal.telephony.Phone;
import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.google.android.collect.Lists;

import android.telephony.TelephonyManager;
import com.mediatek.featureoption.FeatureOption;
import android.provider.Telephony.SIMInfo;
import static android.provider.Settings.System.GPRS_CONNECTION_SETTING;
import static android.provider.Settings.System.GPRS_CONNECTION_SETTING_DEFAULT;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.mediatek.xlog.Xlog;

/**
 * @hide
 *
 * Timeout
 *
 * TODO - look for parent classes and code sharing
 */
public class Tethering extends INetworkManagementEventObserver.Stub {

    private Context mContext;
    private final static String TAG = "Tethering";
    private final static boolean DBG = true;
    private final static boolean VDBG = true;

    // TODO - remove both of these - should be part of interface inspection/selection stuff
    private String[] mTetherableUsbRegexs;
    private String[] mTetherableWifiRegexs;
    private String[] mTetherableBluetoothRegexs;
    private Collection<Integer> mUpstreamIfaceTypes;

    // used to synchronize public access to members
    private Object mPublicSync;

    private static final Integer MOBILE_TYPE = new Integer(ConnectivityManager.TYPE_MOBILE);
    private static final Integer HIPRI_TYPE = new Integer(ConnectivityManager.TYPE_MOBILE_HIPRI);
    private static final Integer DUN_TYPE = new Integer(ConnectivityManager.TYPE_MOBILE_DUN);

    // if we have to connect to mobile, what APN type should we use?  Calculated by examining the
    // upstream type list and the DUN_REQUIRED secure-setting
    private int mPreferredUpstreamMobileApn = ConnectivityManager.TYPE_NONE;

    private final INetworkManagementService mNMService;
    private final INetworkStatsService mStatsService;
    private final IConnectivityManager mConnService;
    private Looper mLooper;
    private HandlerThread mThread;

    private HashMap<String, TetherInterfaceSM> mIfaces; // all tethered/tetherable ifaces

    private BroadcastReceiver mStateReceiver;

    private static final String USB_NEAR_IFACE_ADDR      = "192.168.42.129";
    private static final int USB_PREFIX_LENGTH        = 24;

    // USB is  192.168.42.1 and 255.255.255.0
    // Wifi is 192.168.43.1 and 255.255.255.0
    // BT is limited to max default of 5 connections. 192.168.44.1 to 192.168.48.1
    // with 255.255.255.0

    private String[] mDhcpRange;
    private static final String[] DHCP_DEFAULT_RANGE = {
        "192.168.42.2", "192.168.42.254", "192.168.43.2", "192.168.43.254",
        "192.168.44.2", "192.168.44.254", "192.168.45.2", "192.168.45.254",
        "192.168.46.2", "192.168.46.254", "192.168.47.2", "192.168.47.254",
        "192.168.48.2", "192.168.48.254",
    };

    private String[] mDefaultDnsServers;
    private static final String DNS_DEFAULT_SERVER1 = "8.8.8.8";
    private static final String DNS_DEFAULT_SERVER2 = "8.8.4.4";

    private StateMachine mTetherMasterSM;

    private Notification mTetheredNotification;

    private boolean mRndisEnabled;       // track the RNDIS function enabled state
    private boolean mUsbTetherRequested; // true if USB tethering should be started
                                         // when RNDIS is enabled
    //ALPS00233672
	private boolean mUsbTetherEnabled = false; 	  // track the UI USB Tethering State (record)
    //ALPS00233672
                                         
    private String mWifiIface;
    
    //MTK_DEDICATEDAPN_SUPPORT
    private boolean mIsTetheringChangeDone = true;
    //MTK_DEDICATEDAPN_SUPPORT.END

    public Tethering(Context context, INetworkManagementService nmService,
            INetworkStatsService statsService, IConnectivityManager connService, Looper looper) {
        mContext = context;
        mNMService = nmService;
        mStatsService = statsService;
        mConnService = connService;
        mLooper = looper;

        mPublicSync = new Object();

        mIfaces = new HashMap<String, TetherInterfaceSM>();

        // make our own thread so we don't anr the system
        mThread = new HandlerThread("Tethering");
        mThread.start();
        mLooper = mThread.getLooper();
        mTetherMasterSM = new TetherMasterSM("TetherMaster", mLooper);
        mTetherMasterSM.start();

        mStateReceiver = new StateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_STATE);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mStateReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_UNSHARED);
        filter.addDataScheme("file");
        mContext.registerReceiver(mStateReceiver, filter);

        mDhcpRange = context.getResources().getStringArray(
                com.android.internal.R.array.config_tether_dhcp_range);
        if ((mDhcpRange.length == 0) || (mDhcpRange.length % 2 ==1)) {
            mDhcpRange = DHCP_DEFAULT_RANGE;
        }

        // load device config info
        updateConfiguration();

        // TODO - remove and rely on real notifications of the current iface
        mDefaultDnsServers = new String[2];
        mDefaultDnsServers[0] = DNS_DEFAULT_SERVER1;
        mDefaultDnsServers[1] = DNS_DEFAULT_SERVER2;

        mWifiIface = SystemProperties.get("wifi.interface", "wlan0");
    }

    void updateConfiguration() {
        String[] tetherableUsbRegexs = mContext.getResources().getStringArray(
                com.android.internal.R.array.config_tether_usb_regexs);
        String[] tetherableWifiRegexs = mContext.getResources().getStringArray(
                com.android.internal.R.array.config_tether_wifi_regexs);
        String[] tetherableBluetoothRegexs = mContext.getResources().getStringArray(
                com.android.internal.R.array.config_tether_bluetooth_regexs);

        int ifaceTypes[] = mContext.getResources().getIntArray(
                com.android.internal.R.array.config_tether_upstream_types);
        Collection<Integer> upstreamIfaceTypes = new ArrayList();
        for (int i : ifaceTypes) {
            upstreamIfaceTypes.add(new Integer(i));
        }

        synchronized (mPublicSync) {
            mTetherableUsbRegexs = tetherableUsbRegexs;
            mTetherableWifiRegexs = tetherableWifiRegexs;
            mTetherableBluetoothRegexs = tetherableBluetoothRegexs;
            mUpstreamIfaceTypes = upstreamIfaceTypes;
        }

        // check if the upstream type list needs to be modified due to secure-settings
        checkDunRequired();
    }

    public void interfaceStatusChanged(String iface, boolean up) {
        if (VDBG) Log.d(TAG, "interfaceStatusChanged " + iface + ", " + up);
        boolean found = false;
        boolean usb = false;
        synchronized (mPublicSync) {
            if (isWifi(iface)) {
                found = true;
            } else if (isUsb(iface)) {
                found = true;
                usb = true;
            } else if (isBluetooth(iface)) {
                found = true;
            }
            if (found == false) return;

            TetherInterfaceSM sm = mIfaces.get(iface);
            if (up) {
                if (sm == null) {
                    sm = new TetherInterfaceSM(iface, mLooper, usb);
                    mIfaces.put(iface, sm);
                    sm.start();
                }
            } else {
                if (isUsb(iface) || isBluetooth(iface)) {
                    // ignore usb0 down after enabling RNDIS
                    // we will handle disconnect in interfaceRemoved instead
                    if (VDBG) Log.d(TAG, "ignore interface down for " + iface);
                } else if (sm != null) {
                    Xlog.d(TAG,"interfaceLinkStatusChanged, sm!=null, sendMessage:CMD_INTERFACE_DOWN");
                    sm.sendMessage(TetherInterfaceSM.CMD_INTERFACE_DOWN);
                    mIfaces.remove(iface);
                }
            }
        }
    }

    public void interfaceLinkStateChanged(String iface, boolean up) {
        if (VDBG) Log.d(TAG, "interfaceLinkStateChanged " + iface + ", " + up);
        interfaceStatusChanged(iface, up);
    }

    private boolean isUsb(String iface) {
        synchronized (mPublicSync) {
            for (String regex : mTetherableUsbRegexs) {
                if (iface.matches(regex)) return true;
            }
            return false;
        }
    }

    public boolean isWifi(String iface) {
        synchronized (mPublicSync) {
            for (String regex : mTetherableWifiRegexs) {
                if (iface.matches(regex)) return true;
            }
            return false;
        }
    }

    public boolean isBluetooth(String iface) {
        synchronized (mPublicSync) {
            for (String regex : mTetherableBluetoothRegexs) {
                if (iface.matches(regex)) return true;
            }
            return false;
        }
    }

    public void interfaceAdded(String iface) {
        if (VDBG) Log.d(TAG, "interfaceAdded " + iface);
        boolean found = false;
        boolean usb = false;
        synchronized (mPublicSync) {
            if (isWifi(iface)) {
                found = true;
            }
            if (isUsb(iface)) {
                found = true;
                usb = true;
            }
            if (isBluetooth(iface)) {
                found = true;
            }
            if (found == false) {
                if (VDBG) Log.d(TAG, iface + " is not a tetherable iface, ignoring");
                return;
            }

            TetherInterfaceSM sm = mIfaces.get(iface);
            if (sm != null) {
                if (VDBG) Log.d(TAG, "active iface (" + iface + ") reported as added, ignoring");
                return;
            }
            
            if (isWifi(iface)) {
                IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
                final INetworkManagementService service = INetworkManagementService.Stub.asInterface(b);
                IBinder binder = ServiceManager.getService(Context.WIFI_SERVICE);
                final IWifiManager wifiService = IWifiManager.Stub.asInterface(binder);
                if (service != null && wifiService != null) {
                    new Thread() {
                        public void run() {
                            try {
                                if (!service.getStartRequest()) {
                                    WifiConfiguration config = wifiService.getWifiApConfiguration();
                                    service.restartAccessPoint(config, SystemProperties.get("wifi.interface", "wlan0"), "ap0");                   
                                }
                            } catch (Exception e) {
                                Xlog.e(TAG, "restartAccessPoint failed!");
                            }
                        }
                    }.start();
                } else {
                    Xlog.e(TAG, "Failed to get the NetworkManagementService or WifiService!");
                }
            }
            
            sm = new TetherInterfaceSM(iface, mLooper, usb);
            mIfaces.put(iface, sm);
            sm.start();
        }
        Xlog.d(TAG, "interfaceAdded :" + iface);
    }

    public void interfaceRemoved(String iface) {
        if (VDBG) Log.d(TAG, "interfaceRemoved " + iface);
        synchronized (mPublicSync) {
            TetherInterfaceSM sm = mIfaces.get(iface);
            if (sm == null) {
                if (VDBG) {
                    Log.e(TAG, "attempting to remove unknown iface (" + iface + "), ignoring");
                }
                return;
            }
            Xlog.d(TAG, "interfaceRemoved, iface=" + iface + ", sendMessage:CMD_INTERFACE_DOWN");
            sm.sendMessage(TetherInterfaceSM.CMD_INTERFACE_DOWN);
            mIfaces.remove(iface);
            if (isWifi(iface)) {
                IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
                INetworkManagementService service = INetworkManagementService.Stub.asInterface(b);
                if (null == service) {
                    Xlog.e(TAG, "Failed to get the NetworkManagementService!");
                    return;
                }
                try {
                    service.setStartRequest(false);
                } catch (Exception e) {
            	    Xlog.e(TAG, "setStartRequest failed!");
            	}  
            }  
        }
    }

    public void limitReached(String limitName, String iface) {}

    public int tether(String iface) {
        if (DBG) Log.d(TAG, "Tethering " + iface);
        TetherInterfaceSM sm = null;
        synchronized (mPublicSync) {
            sm = mIfaces.get(iface);
        }
        if (sm == null) {
            Log.e(TAG, "Tried to Tether an unknown iface :" + iface + ", ignoring");
            return ConnectivityManager.TETHER_ERROR_UNKNOWN_IFACE;
        }
        if (!sm.isAvailable() && !sm.isErrored()) {
            Log.e(TAG, "Tried to Tether an unavailable iface :" + iface + ", ignoring");
            return ConnectivityManager.TETHER_ERROR_UNAVAIL_IFACE;
        }
        sm.sendMessage(TetherInterfaceSM.CMD_TETHER_REQUESTED);
        return ConnectivityManager.TETHER_ERROR_NO_ERROR;
    }

    public int untether(String iface) {
        if (DBG) Log.d(TAG, "Untethering " + iface);
        TetherInterfaceSM sm = null;
        synchronized (mPublicSync) {
            sm = mIfaces.get(iface);
        }
        if (sm == null) {
            Log.e(TAG, "Tried to Untether an unknown iface :" + iface + ", ignoring");
            return ConnectivityManager.TETHER_ERROR_UNKNOWN_IFACE;
        }
        if (sm.isErrored()) {
            Log.e(TAG, "Tried to Untethered an errored iface :" + iface + ", ignoring");
            return ConnectivityManager.TETHER_ERROR_UNAVAIL_IFACE;
        }
        sm.sendMessage(TetherInterfaceSM.CMD_TETHER_UNREQUESTED);
        return ConnectivityManager.TETHER_ERROR_NO_ERROR;
    }

    public int getLastTetherError(String iface) {
        TetherInterfaceSM sm = null;
        synchronized (mPublicSync) {
            sm = mIfaces.get(iface);
            if (sm == null) {
                Log.e(TAG, "Tried to getLastTetherError on an unknown iface :" + iface +
                        ", ignoring");
                return ConnectivityManager.TETHER_ERROR_UNKNOWN_IFACE;
            }
            return sm.getLastError();
        }
    }

    // TODO - move all private methods used only by the state machine into the state machine
    // to clarify what needs synchronized protection.
    private void sendTetherStateChangedBroadcast() {
        try {
            if (!mConnService.isTetheringSupported()) return;
        } catch (RemoteException e) {
            return;
        }

        ArrayList<String> availableList = new ArrayList<String>();
        ArrayList<String> activeList = new ArrayList<String>();
        ArrayList<String> erroredList = new ArrayList<String>();

        boolean wifiTethered = false;
        boolean usbTethered = false;
        boolean bluetoothTethered = false;

        synchronized (mPublicSync) {
            Set ifaces = mIfaces.keySet();
            for (Object iface : ifaces) {
                TetherInterfaceSM sm = mIfaces.get(iface);
                if (sm != null) {
                    if (sm.isErrored()) {
                        erroredList.add((String)iface);
                    } else if (sm.isAvailable()) {
                        availableList.add((String)iface);
                    } else if (sm.isTethered()) {
                        if (isUsb((String)iface)) {
                            usbTethered = true;
                        } else if (isWifi((String)iface)) {
                            wifiTethered = true;
                      } else if (isBluetooth((String)iface)) {
                            bluetoothTethered = true;
                        }
                        activeList.add((String)iface);
                    }
                }
            }
        }
        Intent broadcast = new Intent(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        broadcast.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING |
                Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_AVAILABLE_TETHER,
                availableList);
        broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_ACTIVE_TETHER, activeList);
        broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_ERRORED_TETHER,
                erroredList);
        mContext.sendStickyBroadcast(broadcast);
        if (DBG) {
            Log.d(TAG, "sendTetherStateChangedBroadcast " + availableList.size() + ", " +
                    activeList.size() + ", " + erroredList.size());
        }

        if (usbTethered) {
            if (wifiTethered || bluetoothTethered) {
                showTetheredNotification(com.android.internal.R.drawable.stat_sys_tether_general);
            } else {
                showTetheredNotification(com.android.internal.R.drawable.stat_sys_tether_usb);
            }
        } else if (wifiTethered) {
            if (bluetoothTethered) {
                showTetheredNotification(com.android.internal.R.drawable.stat_sys_tether_general);
            } else {
                showTetheredNotification(com.android.internal.R.drawable.stat_sys_tether_wifi);
            }
        } else if (bluetoothTethered) {
            showTetheredNotification(com.android.internal.R.drawable.stat_sys_tether_bluetooth);
        } else {
            clearTetheredNotification();
        }
    }

    private void showTetheredNotification(int icon) {
        NotificationManager notificationManager =
                (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        if (mTetheredNotification != null) {
            if (mTetheredNotification.icon == icon) {
                return;
            }
            notificationManager.cancel(mTetheredNotification.icon);
        }

        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.TetherSettings");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);

        Resources r = Resources.getSystem();
        CharSequence title = r.getText(com.android.internal.R.string.tethered_notification_title);
        CharSequence message = r.getText(com.android.internal.R.string.
                tethered_notification_message);

        if (mTetheredNotification == null) {
            mTetheredNotification = new Notification();
            mTetheredNotification.when = 0;
        }
        mTetheredNotification.icon = icon;
        mTetheredNotification.defaults &= ~Notification.DEFAULT_SOUND;
        mTetheredNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mTetheredNotification.tickerText = title;
        mTetheredNotification.setLatestEventInfo(mContext, title, message, pi);

        notificationManager.notify(mTetheredNotification.icon, mTetheredNotification);
    }

    private void clearTetheredNotification() {
        NotificationManager notificationManager =
            (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null && mTetheredNotification != null) {
            notificationManager.cancel(mTetheredNotification.icon);
            mTetheredNotification = null;
        }
    }

    private class StateReceiver extends BroadcastReceiver {
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            Xlog.d(TAG, "StateReceiver onReceive action:" + action);
            if (action.equals(UsbManager.ACTION_USB_STATE)) {
                synchronized (Tethering.this.mPublicSync) {
                    boolean usbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
					//ALPS00233672
                    boolean oriRndisEnabled = mRndisEnabled;
					//ALPS00233672
                    mRndisEnabled = intent.getBooleanExtra(UsbManager.USB_FUNCTION_RNDIS, false);

					//ALPS00233672					
					Xlog.d(TAG, "StateReceiver onReceive action synchronized: usbConnected = " + usbConnected + ", mRndisEnabled = " + mRndisEnabled + ", mUsbTetherRequested = " + mUsbTetherRequested); 
					
					Xlog.d(TAG, "StateReceiver onReceive action synchronized: mUsbTetherEnabled = " + mUsbTetherEnabled);
					//check that if the UI state is sync with mRndisEnabled state
					if(!mUsbTetherEnabled)
					{
						if(mRndisEnabled && (mRndisEnabled!=oriRndisEnabled))
						{
							//The state of UI and USB is not synced
							//The USB tethering is enabled without UI checked
							//disable the rndis function
							Xlog.d(TAG, "StateReceiver onReceive action synchronized: mUsbTetherEnabled = " + mUsbTetherEnabled + ", mRndisEnabled = " + mRndisEnabled + ", oriRndisEnabled = " + oriRndisEnabled);
							tetherUsb(false);
							UsbManager usbManager = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
							usbManager.setCurrentFunction(null, false);
							
							mUsbTetherRequested = false;
						}
					}
					//ALPS00233672
                    // start tethering if we have a request pending
                    if (usbConnected && mRndisEnabled && mUsbTetherRequested) {
						//ALPS00233672 				
						Xlog.d(TAG, "StateReceiver onReceive action synchronized: usbConnected && mRndisEnabled && mUsbTetherRequested, tetherUsb!! ");
						//ALPS00233672 				
                        tetherUsb(true);

						//ALPS00233672 Added
						mUsbTetherRequested = false;
						//ALPS00233672 Added
                    }
					//ALPS00233672 marked
                    //mUsbTetherRequested = false;
					//ALPS00233672 marked
                }
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (VDBG) Log.d(TAG, "Tethering got CONNECTIVITY_ACTION, networkInfo:" + intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO));
                mTetherMasterSM.sendMessage(TetherMasterSM.CMD_UPSTREAM_CHANGED);
            }
        }
    }

    private void tetherUsb(boolean enable) {
        if (VDBG) Log.d(TAG, "tetherUsb " + enable);

        String[] ifaces = new String[0];
        try {
            ifaces = mNMService.listInterfaces();
        } catch (Exception e) {
            Log.e(TAG, "Error listing Interfaces", e);
            return;
        }
        for (String iface : ifaces) {
            if (isUsb(iface)) {
                int result = (enable ? tether(iface) : untether(iface));
                if (result == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                    return;
                }
            }
        }
        Log.e(TAG, "unable start or stop USB tethering");
    }

    // configured when we start tethering and unconfig'd on error or conclusion
    private boolean configureUsbIface(boolean enabled) {
        if (VDBG) Log.d(TAG, "configureUsbIface(" + enabled + ")");

        // toggle the USB interfaces
        String[] ifaces = new String[0];
        try {
            ifaces = mNMService.listInterfaces();
        } catch (Exception e) {
            Log.e(TAG, "Error listing Interfaces", e);
            return false;
        }
        for (String iface : ifaces) {
            if (isUsb(iface)) {
                InterfaceConfiguration ifcg = null;
                try {
                    ifcg = mNMService.getInterfaceConfig(iface);
                    if (ifcg != null) {
                        InetAddress addr = NetworkUtils.numericToInetAddress(USB_NEAR_IFACE_ADDR);
                        ifcg.addr = new LinkAddress(addr, USB_PREFIX_LENGTH);
                        if (enabled) {
                            ifcg.interfaceFlags = ifcg.interfaceFlags.replace("down", "up");
                        } else {
                            ifcg.interfaceFlags = ifcg.interfaceFlags.replace("up", "down");
                        }
                        ifcg.interfaceFlags = ifcg.interfaceFlags.replace("running", "");
                        ifcg.interfaceFlags = ifcg.interfaceFlags.replace("  "," ");
                        mNMService.setInterfaceConfig(iface, ifcg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error configuring interface " + iface, e);
                    return false;
                }
            }
         }

        return true;
    }

    // TODO - return copies so people can't tamper
    public String[] getTetherableUsbRegexs() {
        return mTetherableUsbRegexs;
    }

    public String[] getTetherableWifiRegexs() {
        return mTetherableWifiRegexs;
    }

    public String[] getTetherableBluetoothRegexs() {
        return mTetherableBluetoothRegexs;
    }

    public int setUsbTethering(boolean enable) {
        if (VDBG) Log.d(TAG, "setUsbTethering(" + enable + ")");
        UsbManager usbManager = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);

		//ALPS00233672
		mUsbTetherEnabled = enable;	  // track the UI USB Tethering State (record)
		//ALPS00233672

        synchronized (mPublicSync) {
            if (enable) {
                if (mRndisEnabled) {
                    tetherUsb(true);
                } else {
                    mUsbTetherRequested = true;
                    usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_RNDIS, false);
                }
            } else {
                tetherUsb(false);
                if (mRndisEnabled) {
                    usbManager.setCurrentFunction(null, false);
                }
                mUsbTetherRequested = false;
            }
        }
        return ConnectivityManager.TETHER_ERROR_NO_ERROR;
    }

    public int[] getUpstreamIfaceTypes() {
        int values[];
        synchronized (mPublicSync) {
            updateConfiguration();
            values = new int[mUpstreamIfaceTypes.size()];
            Iterator<Integer> iterator = mUpstreamIfaceTypes.iterator();
            for (int i=0; i < mUpstreamIfaceTypes.size(); i++) {
                values[i] = iterator.next();
            }
        }
        return values;
    }

    public void checkDunRequired() {
        int secureSetting = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.TETHER_DUN_REQUIRED, 2);
        synchronized (mPublicSync) {
            // 2 = not set, 0 = DUN not required, 1 = DUN required
            if (secureSetting != 2) {
                int requiredApn = (secureSetting == 1 ?
                        ConnectivityManager.TYPE_MOBILE_DUN :
                        ConnectivityManager.TYPE_MOBILE_HIPRI);
                if (requiredApn == ConnectivityManager.TYPE_MOBILE_DUN) {
                    while (mUpstreamIfaceTypes.contains(MOBILE_TYPE)) {
                        mUpstreamIfaceTypes.remove(MOBILE_TYPE);
                    }
                    while (mUpstreamIfaceTypes.contains(HIPRI_TYPE)) {
                        mUpstreamIfaceTypes.remove(HIPRI_TYPE);
                    }
                    if (mUpstreamIfaceTypes.contains(DUN_TYPE) == false) {
                        mUpstreamIfaceTypes.add(DUN_TYPE);
                    }
                } else {
                    while (mUpstreamIfaceTypes.contains(DUN_TYPE)) {
                        mUpstreamIfaceTypes.remove(DUN_TYPE);
                    }
                    if (mUpstreamIfaceTypes.contains(MOBILE_TYPE) == false) {
                        mUpstreamIfaceTypes.add(MOBILE_TYPE);
                    }
                    if (mUpstreamIfaceTypes.contains(HIPRI_TYPE) == false) {
                        mUpstreamIfaceTypes.add(HIPRI_TYPE);
                    }
                }
            }
            if (mUpstreamIfaceTypes.contains(DUN_TYPE)) {
                mPreferredUpstreamMobileApn = ConnectivityManager.TYPE_MOBILE_DUN;
            } else {
                mPreferredUpstreamMobileApn = ConnectivityManager.TYPE_MOBILE_HIPRI;
            }
        }
    }

    // TODO review API - maybe return ArrayList<String> here and below?
    public String[] getTetheredIfaces() {
        ArrayList<String> list = new ArrayList<String>();
        synchronized (mPublicSync) {
            Set keys = mIfaces.keySet();
            for (Object key : keys) {
                TetherInterfaceSM sm = mIfaces.get(key);
                if (sm.isTethered()) {
                    list.add((String)key);
                }
            }
        }
        String[] retVal = new String[list.size()];
        for (int i=0; i < list.size(); i++) {
            retVal[i] = list.get(i);
        }
        return retVal;
    }

    public String[] getTetheredIfacePairs() {
        final ArrayList<String> list = Lists.newArrayList();
        synchronized (mPublicSync) {
            for (TetherInterfaceSM sm : mIfaces.values()) {
                if (sm.isTethered()) {
                    list.add(sm.mMyUpstreamIfaceName);
                    list.add(sm.mIfaceName);
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public String[] getTetherableIfaces() {
        ArrayList<String> list = new ArrayList<String>();
        synchronized (mPublicSync) {
            Set keys = mIfaces.keySet();
            for (Object key : keys) {
                TetherInterfaceSM sm = mIfaces.get(key);
                if (sm.isAvailable()) {
                    list.add((String)key);
                }
            }
        }
        String[] retVal = new String[list.size()];
        for (int i=0; i < list.size(); i++) {
            retVal[i] = list.get(i);
        }
        return retVal;
    }

    public String[] getErroredIfaces() {
        ArrayList<String> list = new ArrayList<String>();
        synchronized (mPublicSync) {
            Set keys = mIfaces.keySet();
            for (Object key : keys) {
                TetherInterfaceSM sm = mIfaces.get(key);
                if (sm.isErrored()) {
                    list.add((String)key);
                }
            }
        }
        String[] retVal = new String[list.size()];
        for (int i= 0; i< list.size(); i++) {
            retVal[i] = list.get(i);
        }
        return retVal;
    }

    //TODO: Temporary handling upstream change triggered without
    //      CONNECTIVITY_ACTION. Only to accomodate interface
    //      switch during HO.
    //      @see bug/4455071
    public void handleTetherIfaceChange() {
        mTetherMasterSM.sendMessage(TetherMasterSM.CMD_UPSTREAM_CHANGED);
    }

    //MTK_DEDICATEDAPN_SUPPORT
    /**
     * @hide
     */
    public boolean isTetheringChangeDone() {
        return mIsTetheringChangeDone;
    }
    //MTK_DEDICATEDAPN_SUPPORT.END
    
    private void updateDns() {
        String newdns = SystemProperties.get("net.dns1", DNS_DEFAULT_SERVER2);
        Xlog.d(TAG, "updateDns net.dns1:" + newdns);
        if (null != newdns && !newdns.isEmpty()) {
            try {
                InetAddress address = InetAddress.getByName(newdns);
                if (address instanceof Inet4Address) {
                    mDefaultDnsServers[1] = newdns;
                } else {
                    newdns = SystemProperties.get("net.dns2", DNS_DEFAULT_SERVER2);
                    Xlog.d(TAG, "net.dns2:" + newdns);
                    if (null != newdns && !newdns.isEmpty()) {
                        address = InetAddress.getByName(newdns);
                        if (address instanceof Inet4Address) {
                            mDefaultDnsServers[1] = newdns;
                        } else {
                            Xlog.d(TAG, "Not support IPv6 address or else!");
                        }
                    }                    
                }
            } catch (Exception e) {
                Xlog.e(TAG, "Exception:" + e.toString());
            }
        }
    }
    
    class TetherInterfaceSM extends StateMachine {
        // notification from the master SM that it's not in tether mode
        static final int CMD_TETHER_MODE_DEAD            =  1;
        // request from the user that it wants to tether
        static final int CMD_TETHER_REQUESTED            =  2;
        // request from the user that it wants to untether
        static final int CMD_TETHER_UNREQUESTED          =  3;
        // notification that this interface is down
        static final int CMD_INTERFACE_DOWN              =  4;
        // notification that this interface is up
        static final int CMD_INTERFACE_UP                =  5;
        // notification from the master SM that it had an error turning on cellular dun
        static final int CMD_CELL_DUN_ERROR              =  6;
        // notification from the master SM that it had trouble enabling IP Forwarding
        static final int CMD_IP_FORWARDING_ENABLE_ERROR  =  7;
        // notification from the master SM that it had trouble disabling IP Forwarding
        static final int CMD_IP_FORWARDING_DISABLE_ERROR =  8;
        // notification from the master SM that it had trouble staring tethering
        static final int CMD_START_TETHERING_ERROR       =  9;
        // notification from the master SM that it had trouble stopping tethering
        static final int CMD_STOP_TETHERING_ERROR        = 10;
        // notification from the master SM that it had trouble setting the DNS forwarders
        static final int CMD_SET_DNS_FORWARDERS_ERROR    = 11;
        // the upstream connection has changed
        static final int CMD_TETHER_CONNECTION_CHANGED   = 12;

        private State mDefaultState;

        private State mInitialState;
        private State mStartingState;
        private State mTetheredState;

        private State mUnavailableState;

        private boolean mAvailable;
        private boolean mTethered;
        int mLastError;

        String mIfaceName;
        String mMyUpstreamIfaceName;  // may change over time

        boolean mUsb;

        TetherInterfaceSM(String name, Looper looper, boolean usb) {
            super(name, looper);
            mIfaceName = name;
            mUsb = usb;
            setLastError(ConnectivityManager.TETHER_ERROR_NO_ERROR);

            mInitialState = new InitialState();
            addState(mInitialState);
            mStartingState = new StartingState();
            addState(mStartingState);
            mTetheredState = new TetheredState();
            addState(mTetheredState);
            mUnavailableState = new UnavailableState();
            addState(mUnavailableState);

            setInitialState(mInitialState);
        }

        public String toString() {
            String res = new String();
            res += mIfaceName + " - ";
            IState current = getCurrentState();
            if (current == mInitialState) res += "InitialState";
            if (current == mStartingState) res += "StartingState";
            if (current == mTetheredState) res += "TetheredState";
            if (current == mUnavailableState) res += "UnavailableState";
            if (mAvailable) res += " - Available";
            if (mTethered) res += " - Tethered";
            res += " - lastError =" + mLastError;
            return res;
        }

        public int getLastError() {
            synchronized (Tethering.this.mPublicSync) {
                return mLastError;
            }
        }

        private void setLastError(int error) {
            synchronized (Tethering.this.mPublicSync) {
                mLastError = error;

                if (isErrored()) {
                    if (mUsb) {
                        // note everything's been unwound by this point so nothing to do on
                        // further error..
                        Tethering.this.configureUsbIface(false);
                    }
                }
            }
        }

        public boolean isAvailable() {
            synchronized (Tethering.this.mPublicSync) {
                return mAvailable;
            }
        }

        private void setAvailable(boolean available) {
            synchronized (Tethering.this.mPublicSync) {
                mAvailable = available;
            }
        }

        public boolean isTethered() {
            synchronized (Tethering.this.mPublicSync) {
                return mTethered;
            }
        }

        private void setTethered(boolean tethered) {
            synchronized (Tethering.this.mPublicSync) {
                mTethered = tethered;
            }
        }

        public boolean isErrored() {
            synchronized (Tethering.this.mPublicSync) {
                return (mLastError != ConnectivityManager.TETHER_ERROR_NO_ERROR);
            }
        }

        class InitialState extends State {
            @Override
            public void enter() {
                Xlog.d(TAG, "[ISM_Initial] enter, sendTetherStateChangedBroadcast");
                setAvailable(true);
                setTethered(false);
                sendTetherStateChangedBroadcast();
            }

            @Override
            public boolean processMessage(Message message) {
                if (DBG) Log.d(TAG, "[ISM_Initial] processMessage what=" + message.what);
                boolean retValue = true;
                switch (message.what) {
                    case CMD_TETHER_REQUESTED:
                        setLastError(ConnectivityManager.TETHER_ERROR_NO_ERROR);
                        mTetherMasterSM.sendMessage(TetherMasterSM.CMD_TETHER_MODE_REQUESTED,
                                TetherInterfaceSM.this);
                        transitionTo(mStartingState);
                        break;
                    case CMD_INTERFACE_DOWN:
                        transitionTo(mUnavailableState);
                        break;
                    default:
                        retValue = false;
                        break;
                }
                return retValue;
            }
        }

        class StartingState extends State {
            @Override
            public void enter() {
                Xlog.d(TAG, "[ISM_Starting] enter");
                setAvailable(false);
                if (mUsb) {
                    if (!Tethering.this.configureUsbIface(true)) {
                        mTetherMasterSM.sendMessage(TetherMasterSM.CMD_TETHER_MODE_UNREQUESTED,
                                TetherInterfaceSM.this);
                        setLastError(ConnectivityManager.TETHER_ERROR_IFACE_CFG_ERROR);

                        transitionTo(mInitialState);
                        return;
                    }
                }
                Xlog.d(TAG, "[ISM_Starting] sendTetherStateChangedBroadcast");
                sendTetherStateChangedBroadcast();

                // Skipping StartingState
                transitionTo(mTetheredState);
            }
            @Override
            public boolean processMessage(Message message) {
                if (DBG) Log.d(TAG, "[ISM_Starting] processMessage what=" + message.what);
                boolean retValue = true;
                switch (message.what) {
                    // maybe a parent class?
                    case CMD_TETHER_UNREQUESTED:
                        mTetherMasterSM.sendMessage(TetherMasterSM.CMD_TETHER_MODE_UNREQUESTED,
                                TetherInterfaceSM.this);
                        if (mUsb) {
                            if (!Tethering.this.configureUsbIface(false)) {
                                setLastErrorAndTransitionToInitialState(
                                    ConnectivityManager.TETHER_ERROR_IFACE_CFG_ERROR);
                                break;
                            }
                        }
                        transitionTo(mInitialState);
                        break;
                    case CMD_CELL_DUN_ERROR:
                    case CMD_IP_FORWARDING_ENABLE_ERROR:
                    case CMD_IP_FORWARDING_DISABLE_ERROR:
                    case CMD_START_TETHERING_ERROR:
                    case CMD_STOP_TETHERING_ERROR:
                    case CMD_SET_DNS_FORWARDERS_ERROR:
                        setLastErrorAndTransitionToInitialState(
                                ConnectivityManager.TETHER_ERROR_MASTER_ERROR);
                        break;
                    case CMD_INTERFACE_DOWN:
                        mTetherMasterSM.sendMessage(TetherMasterSM.CMD_TETHER_MODE_UNREQUESTED,
                                TetherInterfaceSM.this);
                        transitionTo(mUnavailableState);
                        break;
                    default:
                        retValue = false;
                }
                return retValue;
            }
        }

        class TetheredState extends State {
            @Override
            public void enter() {
                Xlog.d(TAG, "[ISM_Tethered] enter");
                try {
                    mNMService.tetherInterface(mIfaceName);
                } catch (Exception e) {
                    Log.e(TAG, "[ISM_Tethered] Error Tethering: " + e.toString());
                    setLastError(ConnectivityManager.TETHER_ERROR_TETHER_IFACE_ERROR);

                    transitionTo(mInitialState);
                    return;
                }
                if (DBG) Log.d(TAG, "[ISM_Tethered] Tethered " + mIfaceName);
                setAvailable(false);
                setTethered(true);
                Xlog.d(TAG,"[ISM_Tethered] sendTetherStateChangedBroadcast");
                sendTetherStateChangedBroadcast();
            }

            private void cleanupUpstream() {
                if (mMyUpstreamIfaceName != null) {
                    // note that we don't care about errors here.
                    // sometimes interfaces are gone before we get
                    // to remove their rules, which generates errors.
                    // just do the best we can.
                    try {
                        // about to tear down NAT; gather remaining statistics
                        mStatsService.forceUpdate();
                    } catch (Exception e) {
                        if (VDBG) Log.e(TAG, "[ISM_Tethered] Exception in forceUpdate: " + e.toString());
                    }
                    try {
                        mNMService.disableNat(mIfaceName, mMyUpstreamIfaceName);
                        Xlog.d(TAG, "[ISM_Tethered] cleanupUpstream disableNat(" + mIfaceName + ", " + mMyUpstreamIfaceName + ")");
                        //MTK_DEDICATEDAPN_SUPPORT
                        if (FeatureOption.MTK_DEDICATEDAPN_SUPPORT) {
                            if (!mMyUpstreamIfaceName.equals(mWifiIface)) {
                                //mNMService.disableMultiRouter(mIfaceName, mMyUpstreamIfaceName);
                                //Xlog.d(TAG, "[ISM_Tethered] cleanupUpstream disableMultiRouter(" + mIfaceName + ", " + mMyUpstreamIfaceName + ")");
                            }
                        }
                        //MTK_DEDICATEDAPN_SUPPORT.END
                    } catch (Exception e) {
                        if (VDBG) Log.e(TAG, "[ISM_Tethered] Exception in disableNat: " + e.toString());
                    }
                    mMyUpstreamIfaceName = null;
                }
                return;
            }

            @Override
            public boolean processMessage(Message message) {
                if (DBG) Log.d(TAG, "[ISM_Tethered] processMessage what=" + message.what);
                boolean retValue = true;
                boolean error = false;
                switch (message.what) {
                    case CMD_TETHER_UNREQUESTED:
                    case CMD_INTERFACE_DOWN:
                        Xlog.d(TAG, "[ISM_Tethered] mMyUpstreamIfaceName: " + mMyUpstreamIfaceName);
                        cleanupUpstream();
                        try {
                            mNMService.untetherInterface(mIfaceName);
                        } catch (Exception e) {
                            setLastErrorAndTransitionToInitialState(
                                    ConnectivityManager.TETHER_ERROR_UNTETHER_IFACE_ERROR);
                            break;
                        }
                        mTetherMasterSM.sendMessage(TetherMasterSM.CMD_TETHER_MODE_UNREQUESTED,
                                TetherInterfaceSM.this);
                        if (message.what == CMD_TETHER_UNREQUESTED) {
                            if (mUsb) {
                                if (!Tethering.this.configureUsbIface(false)) {
                                    setLastError(
                                            ConnectivityManager.TETHER_ERROR_IFACE_CFG_ERROR);
                                }
                            }
                            transitionTo(mInitialState);
                        } else if (message.what == CMD_INTERFACE_DOWN) {
                            transitionTo(mUnavailableState);
                        }
                        if (DBG) Log.d(TAG, "[ISM_Tethered] Untethered " + mIfaceName);
                        break;
                    case CMD_TETHER_CONNECTION_CHANGED:
                        String newUpstreamIfaceName = (String)(message.obj);
                        Xlog.d(TAG, "[ISM_Tethered] CMD_TETHER_CONNECTION_CHANGED mMyUpstreamIfaceName: " + mMyUpstreamIfaceName + ", newUpstreamIfaceName: " + newUpstreamIfaceName);
                        if ((mMyUpstreamIfaceName == null && newUpstreamIfaceName == null) ||
                                (mMyUpstreamIfaceName != null &&
                                mMyUpstreamIfaceName.equals(newUpstreamIfaceName))) {
                            if (VDBG) Log.d(TAG, "[ISM_Tethered] Connection changed noop - dropping");
                            break;
                        }                        
                        //MTK_DEDICATEDAPN_SUPPORT
                        mIsTetheringChangeDone = false;
                        //MTK_DEDICATEDAPN_SUPPORT.END
                        cleanupUpstream();
                        if (newUpstreamIfaceName != null) {
                            try {
                                mNMService.enableNat(mIfaceName, newUpstreamIfaceName);
                                Xlog.d(TAG, "[ISM_Tethered] CMD_TETHER_CONNECTION_CHANGED enableNat(" + mIfaceName + ", " + newUpstreamIfaceName + ")");
                                //MTK_DEDICATEDAPN_SUPPORT
                                if (FeatureOption.MTK_DEDICATEDAPN_SUPPORT) {
                                    InterfaceConfiguration ifcg = mNMService.getInterfaceConfig(newUpstreamIfaceName);
                                    if (ifcg != null && ifcg.isActive()) {
                                        Xlog.d(TAG, "[ISM_Tethered] " + newUpstreamIfaceName + " is up!");
                                        if (!newUpstreamIfaceName.equals(mWifiIface)) {
                                            //mNMService.enableMultiRouter(mIfaceName, newUpstreamIfaceName);
                                            //Xlog.d(TAG, "[ISM_Tethered] CMD_TETHER_CONNECTION_CHANGED enableMultiRouter(" + mIfaceName + ", " + newUpstreamIfaceName + ")");
                                        }
                                    } else {
                                        Xlog.d(TAG, "[ISM_Tethered] " + newUpstreamIfaceName + " is down!");
                                        mNMService.disableNat(mIfaceName, newUpstreamIfaceName);
                                        Xlog.d(TAG, "[ISM_Tethered] CMD_TETHER_CONNECTION_CHANGED disableNat(" + mIfaceName + ", " + newUpstreamIfaceName + ")");
                                        newUpstreamIfaceName = null;                               
                                        if (FeatureOption.MTK_DEDICATEDAPN_SUPPORT) {
                                            mContext.sendBroadcast(new Intent(ConnectivityManager.TETHER_CHANGED_DONE_ACTION));
                                        }
                                        mIsTetheringChangeDone = true;
                                        break;
                                    }                                    								
                                }
                                //MTK_DEDICATEDAPN_SUPPORT.END
                                Xlog.d(TAG, "[ISM_Tethered] CMD_TETHER_CONNECTION_CHANGED updateDns");
                                updateDns();
                                mNMService.setDnsForwarders(mDefaultDnsServers);
                            } catch (Exception e) {
                                Log.e(TAG, "[ISM_Tethered] Exception enabling Nat: " + e.toString());
                                try {
                                    mNMService.untetherInterface(mIfaceName);
                                } catch (Exception ee) {
                                    Xlog.e(TAG, "[ISM_Tethered] untetherInterface failed, exception: " + ee);
                                }

                                setLastError(ConnectivityManager.TETHER_ERROR_ENABLE_NAT_ERROR);
                                transitionTo(mInitialState);
                                //MTK_DEDICATEDAPN_SUPPORT
                                if (FeatureOption.MTK_DEDICATEDAPN_SUPPORT) {
                                    mContext.sendBroadcast(new Intent(ConnectivityManager.TETHER_CHANGED_DONE_ACTION));
                                }
                                mIsTetheringChangeDone = true;
                                //MTK_DEDICATEDAPN_SUPPORT.END
                                return true;
                            }
                        }
                        mMyUpstreamIfaceName = newUpstreamIfaceName;
                        Xlog.d(TAG, "[ISM_Tethered] CMD_TETHER_CONNECTION_CHANGED finished");
                        //MTK_DEDICATEDAPN_SUPPORT
                        if (FeatureOption.MTK_DEDICATEDAPN_SUPPORT) {
                            mContext.sendBroadcast(new Intent(ConnectivityManager.TETHER_CHANGED_DONE_ACTION));
                        }
                        mIsTetheringChangeDone = true;
                        //MTK_DEDICATEDAPN_SUPPORT.END
                        break;
                    case CMD_CELL_DUN_ERROR:
                    case CMD_IP_FORWARDING_ENABLE_ERROR:
                    case CMD_IP_FORWARDING_DISABLE_ERROR:
                    case CMD_START_TETHERING_ERROR:
                    case CMD_STOP_TETHERING_ERROR:
                    case CMD_SET_DNS_FORWARDERS_ERROR:
                        error = true;
                        // fall through
                    case CMD_TETHER_MODE_DEAD:
                        Xlog.d(TAG, "[ISM_Tethered] CMD_TETHER_MODE_DEAD, mMyUpstreamIfaceName: " + mMyUpstreamIfaceName);
                        cleanupUpstream();
                        try {
                            mNMService.untetherInterface(mIfaceName);
                        } catch (Exception e) {
                            setLastErrorAndTransitionToInitialState(
                                    ConnectivityManager.TETHER_ERROR_UNTETHER_IFACE_ERROR);
                            break;
                        }
                        if (error) {
                            setLastErrorAndTransitionToInitialState(
                                    ConnectivityManager.TETHER_ERROR_MASTER_ERROR);
                            break;
                        }
                        if (DBG) Log.d(TAG, "[ISM_Tethered] Tether lost upstream connection " + mIfaceName);
                        Xlog.d(TAG, "[ISM_Tethered] sendTetherStateChangedBroadcast in CMD_TETHER_MODE_DEAD of TetheredState");
                        sendTetherStateChangedBroadcast();
                        if (mUsb) {
                            if (!Tethering.this.configureUsbIface(false)) {
                                setLastError(ConnectivityManager.TETHER_ERROR_IFACE_CFG_ERROR);
                            }
                        }
                        transitionTo(mInitialState);
                        break;
                    default:
                        retValue = false;
                        break;
                }
                return retValue;
            }
        }

        class UnavailableState extends State {
            @Override
            public void enter() {
                Xlog.d(TAG, "[ISM_Unavailable] enter, sendTetherStateChangedBroadcast");
                setAvailable(false);
                setLastError(ConnectivityManager.TETHER_ERROR_NO_ERROR);
                setTethered(false);
                sendTetherStateChangedBroadcast();
            }
            @Override
            public boolean processMessage(Message message) {
                Xlog.d(TAG, "[ISM_Unavailable] processMessage what=" + message.what);
                boolean retValue = true;
                switch (message.what) {
                    case CMD_INTERFACE_UP:
                        transitionTo(mInitialState);
                        break;
                    default:
                        retValue = false;
                        break;
                }
                return retValue;
            }
        }

        void setLastErrorAndTransitionToInitialState(int error) {
            setLastError(error);
            transitionTo(mInitialState);
        }

    }

    class TetherMasterSM extends StateMachine {
        // an interface SM has requested Tethering
        static final int CMD_TETHER_MODE_REQUESTED   = 1;
        // an interface SM has unrequested Tethering
        static final int CMD_TETHER_MODE_UNREQUESTED = 2;
        // upstream connection change - do the right thing
        static final int CMD_UPSTREAM_CHANGED        = 3;
        // we received notice that the cellular DUN connection is up
        static final int CMD_CELL_CONNECTION_RENEW   = 4;
        // we don't have a valid upstream conn, check again after a delay
        static final int CMD_RETRY_UPSTREAM          = 5;

        // This indicates what a timeout event relates to.  A state that
        // sends itself a delayed timeout event and handles incoming timeout events
        // should inc this when it is entered and whenever it sends a new timeout event.
        // We do not flush the old ones.
        private int mSequenceNumber;

        private State mInitialState;
        private State mTetherModeAliveState;

        private State mSetIpForwardingEnabledErrorState;
        private State mSetIpForwardingDisabledErrorState;
        private State mStartTetheringErrorState;
        private State mStopTetheringErrorState;
        private State mSetDnsForwardersErrorState;

        private ArrayList mNotifyList;

        private int mCurrentConnectionSequence;
        private int mMobileApnReserved = ConnectivityManager.TYPE_NONE;

        private String mUpstreamIfaceName = null;

        private static final int UPSTREAM_SETTLE_TIME_MS     = 10000;
        private static final int CELL_CONNECTION_RENEW_MS    = 40000;

        TetherMasterSM(String name, Looper looper) {
            super(name, looper);

            //Add states
            mInitialState = new InitialState();
            addState(mInitialState);
            mTetherModeAliveState = new TetherModeAliveState();
            addState(mTetherModeAliveState);

            mSetIpForwardingEnabledErrorState = new SetIpForwardingEnabledErrorState();
            addState(mSetIpForwardingEnabledErrorState);
            mSetIpForwardingDisabledErrorState = new SetIpForwardingDisabledErrorState();
            addState(mSetIpForwardingDisabledErrorState);
            mStartTetheringErrorState = new StartTetheringErrorState();
            addState(mStartTetheringErrorState);
            mStopTetheringErrorState = new StopTetheringErrorState();
            addState(mStopTetheringErrorState);
            mSetDnsForwardersErrorState = new SetDnsForwardersErrorState();
            addState(mSetDnsForwardersErrorState);

            mNotifyList = new ArrayList();
            setInitialState(mInitialState);
        }

        class TetherMasterUtilState extends State {
            protected final static boolean TRY_TO_SETUP_MOBILE_CONNECTION = true;
            protected final static boolean WAIT_FOR_NETWORK_TO_SETTLE     = false;

            @Override
            public boolean processMessage(Message m) {
                return false;
            }
            protected String enableString(int apnType) {
                switch (apnType) {
                case ConnectivityManager.TYPE_MOBILE_DUN:
                    return Phone.FEATURE_ENABLE_DUN_ALWAYS;
                case ConnectivityManager.TYPE_MOBILE:
                case ConnectivityManager.TYPE_MOBILE_HIPRI:
                    return Phone.FEATURE_ENABLE_HIPRI;
                }
                return null;
            }
            protected boolean turnOnUpstreamMobileConnection(int apnType) {
                boolean retValue = true;
                if (apnType == ConnectivityManager.TYPE_NONE) return false;
                if (apnType != mMobileApnReserved) turnOffUpstreamMobileConnection();
                int result = Phone.APN_REQUEST_FAILED;
                String enableString = enableString(apnType);
                if (enableString == null) return false;
                try {
                    Xlog.d(TAG, "[MSM_TetherModeAlive] Try to startUsingNetworkFeature" );
                    result = mConnService.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE,
                            enableString, new Binder());
                } catch (Exception e) {
                    Xlog.e(TAG, "[MSM_TetherModeAlive] Failed to startUsingNetworkFeature: " + e.toString());
                }
                Xlog.d(TAG, "[MSM_TetherModeAlive] startUsingNetworkFeature result=" + result);
                switch (result) {
                case Phone.APN_ALREADY_ACTIVE:
                case Phone.APN_REQUEST_STARTED:
                    mMobileApnReserved = apnType;
                    //MTK: no need to renew connection due to feature expire
                    //only effect mms connection by MTK design.
                    /*Message m = obtainMessage(CMD_CELL_CONNECTION_RENEW);
                    if (m != null) {
                        m.arg1 = ++mCurrentConnectionSequence;
                        sendMessageDelayed(m, CELL_CONNECTION_RENEW_MS);
                    } else {Xlog.d(TAG, "[MSM_TetherModeAlive] obtainMsg err");}
                    */
                    break;
                case Phone.APN_REQUEST_FAILED:
                default:
                    retValue = false;
                    break;
                }

                return retValue;
            }
            protected boolean turnOffUpstreamMobileConnection() {
                if (mMobileApnReserved != ConnectivityManager.TYPE_NONE) {
                    try {
                        Xlog.d(TAG, "[MSM_TetherModeAlive] Try to stopUsingNetworkFeature" );
                        mConnService.stopUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE,
                                enableString(mMobileApnReserved));
                    } catch (Exception e) {
                        return false;
                    }
                    mMobileApnReserved = ConnectivityManager.TYPE_NONE;
                }
                return true;
            }
            protected boolean turnOnMasterTetherSettings() {
                try {
                    mNMService.setIpForwardingEnabled(true);
                } catch (Exception e) {
                    transitionTo(mSetIpForwardingEnabledErrorState);
                    return false;
                }
                try {
                    mNMService.startTethering(mDhcpRange);
                } catch (Exception e) {
                    try {
                        mNMService.stopTethering();
                        mNMService.startTethering(mDhcpRange);
                    } catch (Exception ee) {
                        transitionTo(mStartTetheringErrorState);
                        return false;
                    }
                }
                try {
                    Xlog.d(TAG, "[MSM_TetherModeAlive] turnOnMasterTetherSettings updateDns");
                    updateDns();
                    mNMService.setDnsForwarders(mDefaultDnsServers);
                } catch (Exception e) {
                    transitionTo(mSetDnsForwardersErrorState);
                    return false;
                }
                return true;
            }
            protected boolean turnOffMasterTetherSettings() {
                try {
                    mNMService.stopTethering();
                } catch (Exception e) {
                    transitionTo(mStopTetheringErrorState);
                    return false;
                }
                try {
                    mNMService.setIpForwardingEnabled(false);
                } catch (Exception e) {
                    transitionTo(mSetIpForwardingDisabledErrorState);
                    return false;
                }
                // MTK: add for ATT throughput test
                if (mUpstreamIfaceName != null && !mUpstreamIfaceName.equals(mWifiIface)) {
                    int mtu = mContext.getResources().getInteger(com.android.internal.R.integer.config_mobile_mtu_default_size);
                    Xlog.d(TAG, "[MSM_TetherUtil] setMtuByInterface mtu:" + mtu);
                    NetworkUtils.setMtuByInterface(mUpstreamIfaceName, mtu);
                }
                transitionTo(mInitialState);
                return true;
            }
            private int chooseDedicatedType() {
                TelephonyManager tm = TelephonyManager.getDefault();
                boolean isSimAbsent = false;
                boolean isGPRSDisabled = false;
                boolean dataEnabled = false;

                try {
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        final long simId = Settings.System.getLong(mContext.getContentResolver(), 
                                       Settings.System.GPRS_CONNECTION_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET);
                        dataEnabled = !(Settings.System.DEFAULT_SIM_NOT_SET == simId || 0 == simId);
                        Xlog.d(TAG, "SettingsGemini:" + dataEnabled);
                    } else {
                        dataEnabled = mConnService.getMobileDataEnabled();
                        Xlog.d(TAG, "Settings:" + dataEnabled);
                    }

                    final NetworkInfo wifiInfo = mConnService.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (wifiInfo != null && wifiInfo.isConnected()) {
                        Xlog.d(TAG, "trun off dedicated pdp due to wifi connected");
                        turnOffUpstreamMobileConnection();
                        return ConnectivityManager.TYPE_WIFI;
                    }

                    final NetworkInfo hipriInfo = mConnService.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_DUN);
                    if (hipriInfo != null && dataEnabled) {
                        return ConnectivityManager.TYPE_MOBILE_DUN;
                    }
                    
                    if (!dataEnabled) {
                        Xlog.d(TAG, "trunOffUpstreamMobileConnection due to data disabled");
                        turnOffUpstreamMobileConnection();
                    }
                } catch (RemoteException e) {
                    Xlog.e(TAG, "RemoteException " + e);
                }
                
                return ConnectivityManager.TYPE_NONE;
            }
            
            protected void chooseUpstreamType(boolean tryCell) {
                int upType = ConnectivityManager.TYPE_NONE;
                String iface = null;

                updateConfiguration();

                synchronized (mPublicSync) {
                    if (VDBG) {
                        Log.d(TAG, "chooseUpstreamType has upstream iface types:");
                        for (Integer netType : mUpstreamIfaceTypes) {
                            Log.d(TAG, " " + netType);
                        }
                    }
                    if (FeatureOption.MTK_DEDICATEDAPN_SUPPORT) {
                        upType = chooseDedicatedType();
                        Xlog.d(TAG, "chooseDedicatedType:" + upType);
                    } else {
                        for (Integer netType : mUpstreamIfaceTypes) {
                            NetworkInfo info = null;
                            try {
                                info = mConnService.getNetworkInfo(netType.intValue());
                            } catch (RemoteException e) { }
                            if ((info != null) && info.isConnected()) {
                                upType = netType.intValue();
                                break;
                            }
                        }
                    }
                }

                if (DBG) {
                    Log.d(TAG, "chooseUpstreamType(" + tryCell + "), preferredApn ="
                            + mPreferredUpstreamMobileApn + ", got type=" + upType);
                }

                // if we're on DUN, put our own grab on it
                if (upType == ConnectivityManager.TYPE_MOBILE_DUN ||
                        upType == ConnectivityManager.TYPE_MOBILE_HIPRI) {
                    turnOnUpstreamMobileConnection(upType);
                }

                if (upType == ConnectivityManager.TYPE_NONE) {
                    /* MTK Data Policy
                     * Don't enable data connection if no data connection is exist.
                     */
                    /*boolean tryAgainLater = true;
                    if ((tryCell == TRY_TO_SETUP_MOBILE_CONNECTION) &&
                            (turnOnUpstreamMobileConnection(mPreferredUpstreamMobileApn) == true)) {
                        // we think mobile should be coming up - don't set a retry
                        tryAgainLater = false;
                    }
                    if (tryAgainLater) {
                        sendMessageDelayed(CMD_RETRY_UPSTREAM, UPSTREAM_SETTLE_TIME_MS);
                    }*/
                } else {
                    LinkProperties linkProperties = null;
                    try {
                        linkProperties = mConnService.getLinkProperties(upType);
                    } catch (RemoteException e) { }
                    if (linkProperties != null) {
                        iface = linkProperties.getInterfaceName();
                        String[] dnsServers = mDefaultDnsServers;
                        Collection<InetAddress> dnses = linkProperties.getDnses();
                        if (dnses != null) {
                            // we currently only handle IPv4
                            ArrayList<InetAddress> v4Dnses =
                                    new ArrayList<InetAddress>(dnses.size());
                            for (InetAddress dnsAddress : dnses) {
                                if (dnsAddress instanceof Inet4Address) {
                                    v4Dnses.add(dnsAddress);
                                }
                            }
                            if (v4Dnses.size() > 0) {
                                dnsServers = NetworkUtils.makeStrings(v4Dnses);
                            }
                        }
                        try {
                            mNMService.setDnsForwarders(dnsServers);
                        } catch (Exception e) {
                            transitionTo(mSetDnsForwardersErrorState);
                        }
                    }
                }
                notifyTetheredOfNewUpstreamIface(iface);
            }

            protected void notifyTetheredOfNewUpstreamIface(String ifaceName) {
                if (DBG) Log.d(TAG, "[MSM_TetherModeAlive] notifying tethered with iface =" + ifaceName);
                mUpstreamIfaceName = ifaceName;
                // MTK: add for ATT throughput test
                if (ifaceName != null && !ifaceName.equals(mWifiIface)) {
                    Xlog.d(TAG, "[MSM_TetherUtil] setMtuByInterface 1500:" + mUpstreamIfaceName);
                    NetworkUtils.setMtuByInterface(mUpstreamIfaceName, 1500);
                }
                for (Object o : mNotifyList) {
                    TetherInterfaceSM sm = (TetherInterfaceSM)o;
                    if (ifaceName != null && sm.mMyUpstreamIfaceName != null && !ifaceName.equals(sm.mMyUpstreamIfaceName)) {
                        sm.sendMessage(TetherInterfaceSM.CMD_TETHER_CONNECTION_CHANGED, null);
                    }
                }
                for (Object o : mNotifyList) {
                    TetherInterfaceSM sm = (TetherInterfaceSM)o;
                    sm.sendMessage(TetherInterfaceSM.CMD_TETHER_CONNECTION_CHANGED,
                            ifaceName);
                }
            }
        }

        class InitialState extends TetherMasterUtilState {
            @Override
            public void enter() {
                Xlog.d(TAG, "[MSM_Initial] enter");
            }
            @Override
            public boolean processMessage(Message message) {
                if (DBG) Log.d(TAG, "[MSM_Initial] processMessage what=" + message.what);
                boolean retValue = true;
                switch (message.what) {
                    case CMD_TETHER_MODE_REQUESTED:
                        TetherInterfaceSM who = (TetherInterfaceSM)message.obj;
                        if (VDBG) Log.d(TAG, "[MSM_Initial] Tether Mode requested by " + who.toString());
                        mNotifyList.add(who);
                        transitionTo(mTetherModeAliveState);
                        break;
                    case CMD_TETHER_MODE_UNREQUESTED:
                        who = (TetherInterfaceSM)message.obj;
                        if (VDBG) Log.d(TAG, "[MSM_Initial] Tether Mode unrequested by " + who.toString());
                        int index = mNotifyList.indexOf(who);
                        if (index != -1) {
                            mNotifyList.remove(who);
                        }
                        break;
                    default:
                        retValue = false;
                        break;
                }
                return retValue;
            }
        }

        class TetherModeAliveState extends TetherMasterUtilState {
            boolean mTryCell = !WAIT_FOR_NETWORK_TO_SETTLE;
            @Override
            public void enter() {
                Xlog.d(TAG, "[MSM_TetherModeAlive] enter");
                turnOnMasterTetherSettings(); // may transition us out

                mTryCell = !WAIT_FOR_NETWORK_TO_SETTLE; // better try something first pass
                                                        // or crazy tests cases will fail
                chooseUpstreamType(mTryCell);
                mTryCell = !mTryCell;
            }
            @Override
            public void exit() {
                Xlog.d(TAG, "[MSM_TetherModeAlive] exit");
                turnOffUpstreamMobileConnection();
                notifyTetheredOfNewUpstreamIface(null);
            }
            @Override
            public boolean processMessage(Message message) {
                if (DBG) Log.d(TAG, "[MSM_TetherModeAlive] processMessage what=" + message.what);
                boolean retValue = true;
                switch (message.what) {
                    case CMD_TETHER_MODE_REQUESTED:
                        TetherInterfaceSM who = (TetherInterfaceSM)message.obj;
                        mNotifyList.add(who);
                        who.sendMessage(TetherInterfaceSM.CMD_TETHER_CONNECTION_CHANGED,
                                mUpstreamIfaceName);
                        break;
                    case CMD_TETHER_MODE_UNREQUESTED:
                        who = (TetherInterfaceSM)message.obj;
                        int index = mNotifyList.indexOf(who);
                        if (index != -1) {
                            mNotifyList.remove(index);
                            if (mNotifyList.isEmpty()) {
                                turnOffMasterTetherSettings(); // transitions appropriately
                            }
                        }
                        break;
                    case CMD_UPSTREAM_CHANGED:
                        // need to try DUN immediately if Wifi goes down
                        mTryCell = !WAIT_FOR_NETWORK_TO_SETTLE;
                        chooseUpstreamType(mTryCell);
                        mTryCell = !mTryCell;
                        break;
                    case CMD_CELL_CONNECTION_RENEW:
                        // make sure we're still using a requested connection - may have found
                        // wifi or something since then.
                        if (mCurrentConnectionSequence == message.arg1) {
                            if (VDBG) {
                                Log.d(TAG, "[MSM_TetherModeAlive] renewing mobile connection - requeuing for another " +
                                        CELL_CONNECTION_RENEW_MS + "ms");
                            }
                            turnOnUpstreamMobileConnection(mMobileApnReserved);
                        }
                        break;
                    case CMD_RETRY_UPSTREAM:
                        chooseUpstreamType(mTryCell);
                        mTryCell = !mTryCell;
                        break;
                    default:
                        retValue = false;
                        break;
                }
                return retValue;
            }
        }

        class ErrorState extends State {
            int mErrorNotification;
            @Override
            public boolean processMessage(Message message) {
                Xlog.d(TAG, "[MSM_Error] processMessage what=" + message.what);
                boolean retValue = true;
                switch (message.what) {
                    case CMD_TETHER_MODE_REQUESTED:
                        TetherInterfaceSM who = (TetherInterfaceSM)message.obj;
                        who.sendMessage(mErrorNotification);
                        break;
                    default:
                       retValue = false;
                }
                return retValue;
            }
            void notify(int msgType) {
                mErrorNotification = msgType;
                for (Object o : mNotifyList) {
                    TetherInterfaceSM sm = (TetherInterfaceSM)o;
                    sm.sendMessage(msgType);
                }
            }

        }
        class SetIpForwardingEnabledErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "[MSM_Error] setIpForwardingEnabled");
                notify(TetherInterfaceSM.CMD_IP_FORWARDING_ENABLE_ERROR);
                transitionTo(mInitialState);
            }
        }

        class SetIpForwardingDisabledErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "[MSM_Error] setIpForwardingDisabled");
                notify(TetherInterfaceSM.CMD_IP_FORWARDING_DISABLE_ERROR);
                transitionTo(mInitialState);
            }
        }

        class StartTetheringErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "[MSM_Error] startTethering");
                notify(TetherInterfaceSM.CMD_START_TETHERING_ERROR);
                try {
                    mNMService.setIpForwardingEnabled(false);
                } catch (Exception e) {}
                transitionTo(mInitialState);
            }
        }

        class StopTetheringErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "[MSM_Error] stopTethering");
                notify(TetherInterfaceSM.CMD_STOP_TETHERING_ERROR);
                try {
                    mNMService.setIpForwardingEnabled(false);
                } catch (Exception e) {}
                transitionTo(mInitialState);
            }
        }

        class SetDnsForwardersErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "[MSM_Error] setDnsForwarders");
                notify(TetherInterfaceSM.CMD_SET_DNS_FORWARDERS_ERROR);
                try {
                    mNMService.stopTethering();
                } catch (Exception e) {}
                try {
                    mNMService.setIpForwardingEnabled(false);
                } catch (Exception e) {}
                transitionTo(mInitialState);
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission(
                android.Manifest.permission.DUMP) != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump ConnectivityService.Tether " +
                    "from from pid=" + Binder.getCallingPid() + ", uid=" +
                    Binder.getCallingUid());
                    return;
        }

        synchronized (mPublicSync) {
            pw.println("mUpstreamIfaceTypes: ");
            for (Integer netType : mUpstreamIfaceTypes) {
                pw.println(" " + netType);
            }

            pw.println();
            pw.println("Tether state:");
            for (Object o : mIfaces.values()) {
                pw.println(" "+o.toString());
            }
        }
        pw.println();
        return;
    }
}
