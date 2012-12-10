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

package com.android.server;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.media.RingtoneManager;
import android.net.wifi.IWifiManager;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiStateMachine;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiWatchdogStateMachine;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WpsInfo;
import android.net.wifi.WpsResult;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.NetworkInfo.DetailedState;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Handler;
import android.os.Messenger;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.FileDescriptor;
import java.io.PrintWriter;

import com.android.internal.app.IBatteryStats;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.util.AsyncChannel;
import com.android.server.am.BatteryStatsService;
import com.android.internal.R;

import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.SXlog;

/**
 * WifiService handles remote WiFi operation requests by implementing
 * the IWifiManager interface.
 *
 * @hide
 */
//TODO: Clean up multiple locks and implement WifiService
// as a SM to track soft AP/client/adhoc bring up based
// on device idle state, airplane mode and boot.

public class WifiService extends IWifiManager.Stub {
    private static final String TAG = "WifiService";
    private static final boolean DBG = true;

    private final WifiStateMachine mWifiStateMachine;

    private Context mContext;

    private AlarmManager mAlarmManager;
    private PendingIntent mIdleIntent;
    private static final int IDLE_REQUEST = 0;
    private boolean mScreenOff;
    private boolean mDeviceIdle;
    private boolean mEmergencyCallbackMode = false;
    private int mPluggedType;

    /* Chipset supports background scan */
    private final boolean mBackgroundScanSupported;

    private final LockList mLocks = new LockList();
    // some wifi lock statistics
    private int mFullHighPerfLocksAcquired;
    private int mFullHighPerfLocksReleased;
    private int mFullLocksAcquired;
    private int mFullLocksReleased;
    private int mScanLocksAcquired;
    private int mScanLocksReleased;

    private final List<Multicaster> mMulticasters =
            new ArrayList<Multicaster>();
    private int mMulticastEnabled;
    private int mMulticastDisabled;

    private final IBatteryStats mBatteryStats;

    private boolean mEnableTrafficStatsPoll = false;
    private int mTrafficStatsPollToken = 0;
    private long mTxPkts;
    private long mRxPkts;
    /* Tracks last reported data activity */
    private int mDataActivity;
    private String mInterfaceName;

    /**
     * Interval in milliseconds between polling for traffic
     * statistics
     */
    private static final int POLL_TRAFFIC_STATS_INTERVAL_MSECS = 1000;

    /**
     * See {@link Settings.Secure#WIFI_IDLE_MS}. This is the default value if a
     * Settings.Secure value is not present. This timeout value is chosen as
     * the approximate point at which the battery drain caused by Wi-Fi
     * being enabled but not active exceeds the battery drain caused by
     * re-establishing a connection to the mobile data network.
     */
    private static final long DEFAULT_IDLE_MS = 15 * 60 * 1000; /* 15 minutes */

    private static final String ACTION_DEVICE_IDLE =
            "com.android.server.WifiManager.action.DEVICE_IDLE";

    private static final int WIFI_DISABLED                  = 0;
    private static final int WIFI_ENABLED                   = 1;
    /* Wifi enabled while in airplane mode */
    private static final int WIFI_ENABLED_AIRPLANE_OVERRIDE = 2;
    /* Wifi disabled due to airplane mode on */
    private static final int WIFI_DISABLED_AIRPLANE_ON      = 3;

    /* Persisted state that tracks the wifi & airplane interaction from settings */
    private AtomicInteger mPersistWifiState = new AtomicInteger(WIFI_DISABLED);
    /* Tracks current airplane mode state */
    private AtomicBoolean mAirplaneModeOn = new AtomicBoolean(false);
    /* Tracks whether wifi is enabled from WifiStateMachine's perspective */
    private boolean mWifiEnabled;

    private boolean mIsReceiverRegistered = false;


    NetworkInfo mNetworkInfo = new NetworkInfo(ConnectivityManager.TYPE_WIFI, 0, "WIFI", "");

    // Variables relating to the 'available networks' notification
    /**
     * The icon to show in the 'available networks' notification. This will also
     * be the ID of the Notification given to the NotificationManager.
     */
    private static final int ICON_NETWORKS_AVAILABLE =
            com.android.internal.R.drawable.stat_notify_wifi_in_range;
    /**
     * When a notification is shown, we wait this amount before possibly showing it again.
     */
    private final long NOTIFICATION_REPEAT_DELAY_MS;
    /**
     * Whether the user has set the setting to show the 'available networks' notification.
     */
    private boolean mNotificationEnabled;
    /**
     * Observes the user setting to keep {@link #mNotificationEnabled} in sync.
     */
    private NotificationEnabledSettingObserver mNotificationEnabledSettingObserver;
    /**
     * The {@link System#currentTimeMillis()} must be at least this value for us
     * to show the notification again.
     */
    private long mNotificationRepeatTime;
    /**
     * The Notification object given to the NotificationManager.
     */
    private Notification mNotification;
    /**
     * Whether the notification is being shown, as set by us. That is, if the
     * user cancels the notification, we will not receive the callback so this
     * will still be true. We only guarantee if this is false, then the
     * notification is not showing.
     */
    private boolean mNotificationShown;
    /**
     * The number of continuous scans that must occur before consider the
     * supplicant in a scanning state. This allows supplicant to associate with
     * remembered networks that are in the scan results.
     */
    private static final int NUM_SCANS_BEFORE_ACTUALLY_SCANNING = 0;
    /**
     * The number of scans since the last network state change. When this
     * exceeds {@link #NUM_SCANS_BEFORE_ACTUALLY_SCANNING}, we consider the
     * supplicant to actually be scanning. When the network state changes to
     * something other than scanning, we reset this to 0.
     */
    private int mNumScansSinceNetworkStateChange;

    /**
     * Asynchronous channel to WifiStateMachine
     */
    private AsyncChannel mWifiStateMachineChannel;

    /**
     * Clients receiving asynchronous messages
     */
    private List<AsyncChannel> mClients = new ArrayList<AsyncChannel>();

    /**
     * Handles client connections
     */
    private class AsyncServiceHandler extends Handler {

        AsyncServiceHandler(android.os.Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED: {
                    if (msg.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
                        Slog.d(TAG, "New client listening to asynchronous messages");
                        mClients.add((AsyncChannel) msg.obj);
                    } else {
                        Slog.e(TAG, "Client connection failure, error=" + msg.arg1);
                    }
                    break;
                }
                case AsyncChannel.CMD_CHANNEL_DISCONNECTED: {
                    if (msg.arg1 == AsyncChannel.STATUS_SEND_UNSUCCESSFUL) {
                        Slog.d(TAG, "Send failed, client connection lost");
                    } else {
                        Slog.d(TAG, "Client connection lost with reason: " + msg.arg1);
                    }
                    mClients.remove((AsyncChannel) msg.obj);
                    break;
                }
                case AsyncChannel.CMD_CHANNEL_FULL_CONNECTION: {
                    AsyncChannel ac = new AsyncChannel();
                    ac.connect(mContext, this, msg.replyTo);
                    break;
                }
                case WifiManager.CMD_ENABLE_TRAFFIC_STATS_POLL: {
                    mEnableTrafficStatsPoll = (msg.arg1 == 1);
                    mTrafficStatsPollToken++;
                    if (mEnableTrafficStatsPoll) {
                        notifyOnDataActivity();
                        sendMessageDelayed(Message.obtain(this, WifiManager.CMD_TRAFFIC_STATS_POLL,
                                mTrafficStatsPollToken, 0), POLL_TRAFFIC_STATS_INTERVAL_MSECS);
                    }
                    break;
                }
                case WifiManager.CMD_TRAFFIC_STATS_POLL: {
                    if (msg.arg1 == mTrafficStatsPollToken) {
                        notifyOnDataActivity();
                        sendMessageDelayed(Message.obtain(this, WifiManager.CMD_TRAFFIC_STATS_POLL,
                                mTrafficStatsPollToken, 0), POLL_TRAFFIC_STATS_INTERVAL_MSECS);
                    }
                    break;
                }
                case WifiManager.CMD_CONNECT_NETWORK: {
                    if (msg.obj != null) {
                        mWifiStateMachine.connectNetwork((WifiConfiguration)msg.obj);
                    } else {
                        mWifiStateMachine.connectNetwork(msg.arg1);
                    }
                    break;
                }
                case WifiManager.CMD_SAVE_NETWORK: {
                    mWifiStateMachine.saveNetwork((WifiConfiguration)msg.obj);
                    break;
                }
                case WifiManager.CMD_FORGET_NETWORK: {
                    mWifiStateMachine.forgetNetwork(msg.arg1);
                    break;
                }
                case WifiManager.CMD_START_WPS: {
                    //replyTo has the original source
                    mWifiStateMachine.startWps(msg.replyTo, (WpsInfo)msg.obj);
                    break;
                }
                case WifiManager.CMD_DISABLE_NETWORK: {
                    mWifiStateMachine.disableNetwork(msg.replyTo, msg.arg1, msg.arg2);
                    break;
                }
                default: {
                    Slog.d(TAG, "WifiServicehandler.handleMessage ignoring msg=" + msg);
                    break;
                }
            }
        }
    }
    private AsyncServiceHandler mAsyncServiceHandler;

    /**
     * Handles interaction with WifiStateMachine
     */
    private class WifiStateMachineHandler extends Handler {
        private AsyncChannel mWsmChannel;

        WifiStateMachineHandler(android.os.Looper looper) {
            super(looper);
            mWsmChannel = new AsyncChannel();
            mWsmChannel.connect(mContext, this, mWifiStateMachine.getHandler());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED: {
                    if (msg.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
                        mWifiStateMachineChannel = mWsmChannel;
                    } else {
                        Slog.e(TAG, "WifiStateMachine connection failure, error=" + msg.arg1);
                        mWifiStateMachineChannel = null;
                    }
                    break;
                }
                case AsyncChannel.CMD_CHANNEL_DISCONNECTED: {
                    Slog.e(TAG, "WifiStateMachine channel lost, msg.arg1 =" + msg.arg1);
                    mWifiStateMachineChannel = null;
                    //Re-establish connection to state machine
                    mWsmChannel.connect(mContext, this, mWifiStateMachine.getHandler());
                    break;
                }
                default: {
                    Slog.d(TAG, "WifiStateMachineHandler.handleMessage ignoring msg=" + msg);
                    break;
                }
            }
        }
    }
    WifiStateMachineHandler mWifiStateMachineHandler;

    /**
     * Temporary for computing UIDS that are responsible for starting WIFI.
     * Protected by mWifiStateTracker lock.
     */
    private final WorkSource mTmpWorkSource = new WorkSource();
    private WifiWatchdogStateMachine mWifiWatchdogStateMachine;
    
    //MTK_OP01_PROTECT_START
    static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_EAP = 3;
    static final int SECURITY_WAPI_PSK = 4;
    static final int SECURITY_WAPI_CERT = 5;
    static final int SECURITY_WPA2_PSK = 6;
    
    private static final long sSuspendNotificationDuration = 60 * 60 * 1000;
    private static final String mWifiSettingsClassName = "com.android.settings.Settings$WifiSettingsActivity";
    private String OPTR;
    private int mConnectPolicy = Settings.System.WIFI_CONNECT_AP_TYPE_AUTO;
    private int mCellToWiFiPolicy = Settings.System.WIFI_CONNECT_TYPE_AUTO;
    private long mSuspendNotificationTime = 0;
    private ConnectTypeObserver mConnectTypeObserver;
    private boolean mAutoConnect = false;
    private boolean mWaitForScanResult = false;
    private PendingIntent mCheckConnectionIntent;
    private static final int CHECK_CONNECTION_REQUEST = 1;
    private static final String ACTION_CHECK_CONNECTION =
            "com.android.server.WifiManager.action.CHECK_CONNECTION";
    //MTK_OP01_PROTECT_END

    WifiService(Context context) {
        mContext = context;

        mInterfaceName =  SystemProperties.get("wifi.interface", "wlan0");

        mWifiStateMachine = new WifiStateMachine(mContext, mInterfaceName);
        mWifiStateMachine.enableRssiPolling(true);
        mBatteryStats = BatteryStatsService.getService();

        mAlarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        Intent idleIntent = new Intent(ACTION_DEVICE_IDLE, null);
        mIdleIntent = PendingIntent.getBroadcast(mContext, IDLE_REQUEST, idleIntent, 0);

        mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        boolean isAirplaneModeOn = isAirplaneModeOn();
                        SXlog.d(TAG, "Received ACTION_AIRPLANE_MODE_CHANGED, isAirplaneModeOn:" + isAirplaneModeOn);
                        mAirplaneModeOn.set(isAirplaneModeOn);
                        /* On airplane mode disable, restore wifi state if necessary */
                        if (!mAirplaneModeOn.get() && (testAndClearWifiSavedState() ||
                            mPersistWifiState.get() == WIFI_ENABLED_AIRPLANE_OVERRIDE)) {
                                persistWifiState(true);
                        }
                        updateWifiState();
                    }
                },
                new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                                    WifiManager.WIFI_STATE_DISABLED);

                            mWifiEnabled = (wifiState == WifiManager.WIFI_STATE_ENABLED);

                            // reset & clear notification on any wifi state change
                            resetNotification();
                            mWaitForScanResult = false;
                        } else if (intent.getAction().equals(
                                WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                            mNetworkInfo = (NetworkInfo) intent.getParcelableExtra(
                                    WifiManager.EXTRA_NETWORK_INFO);
                            // reset & clear notification on a network connect & disconnect
                            switch(mNetworkInfo.getDetailedState()) {
                                case CONNECTED:
                                case DISCONNECTED:
                                    evaluateTrafficStatsPolling();
                                    resetNotification();
                                    break;
                            }
                        } else if (intent.getAction().equals(
                                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                            checkAndSetNotification();
                        }
                    }
                }, filter);

        HandlerThread wifiThread = new HandlerThread("WifiService");
        wifiThread.start();
        mAsyncServiceHandler = new AsyncServiceHandler(wifiThread.getLooper());
        mWifiStateMachineHandler = new WifiStateMachineHandler(wifiThread.getLooper());

        // Setting is in seconds
        NOTIFICATION_REPEAT_DELAY_MS = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY, 900) * 1000l;
        mNotificationEnabledSettingObserver = new NotificationEnabledSettingObserver(new Handler());
        mNotificationEnabledSettingObserver.register();

        mBackgroundScanSupported = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_wifi_background_scan_support);
                
        //MTK_OP01_PROTECT_START
        mConnectPolicy = Settings.System.getInt(context.getContentResolver(), Settings.System.WIFI_CONNECT_AP_TYPE, Settings.System.WIFI_CONNECT_AP_TYPE_AUTO);
        mCellToWiFiPolicy = Settings.System.getInt(context.getContentResolver(), Settings.System.WIFI_CONNECT_TYPE, Settings.System.WIFI_CONNECT_TYPE_AUTO);
        OPTR = SystemProperties.get("ro.operator.optr", "");
        mConnectTypeObserver = new ConnectTypeObserver(new Handler());
        Intent checkIntent = new Intent(ACTION_CHECK_CONNECTION, null);
        mCheckConnectionIntent = PendingIntent.getBroadcast(mContext, CHECK_CONNECTION_REQUEST, 
                                    checkIntent, 0);
        //MTK_OP01_PROTECT_END
    }

    /**
     * Check if Wi-Fi needs to be enabled and start
     * if needed
     *
     * This function is used only at boot time
     */
    public void checkAndStartWifi() {
        boolean isAirplaneModeOn = isAirplaneModeOn();
        int persistedWifiState = getPersistedWifiState();
        SXlog.d(TAG, "isAirplaneModeOn:" + isAirplaneModeOn + ", getPersistedWifiState:" + persistedWifiState);
        mAirplaneModeOn.set(isAirplaneModeOn);
        mPersistWifiState.set(persistedWifiState);
        /* Start if Wi-Fi should be enabled or the saved state indicates Wi-Fi was on */
        boolean wifiEnabled = shouldWifiBeEnabled() || testAndClearWifiSavedState();

        //MTK_OP01_PROTECT_START
        if (OPTR.equals("OP01") && mAirplaneModeOn.get()) {
            SXlog.i(TAG, "Don't enable wifi when airplane mode is on for CMCC");
            setWifiEnabled(false);
        } else 
        //MTK_OP01_PROTECT_END
        {
            Slog.i(TAG, "WifiService starting up with Wi-Fi " +
                    (wifiEnabled ? "enabled" : "disabled"));
            setWifiEnabled(wifiEnabled);
        }

        mWifiWatchdogStateMachine = WifiWatchdogStateMachine.
               makeWifiWatchdogStateMachine(mContext);

    }

    private boolean testAndClearWifiSavedState() {
        final ContentResolver cr = mContext.getContentResolver();
        int wifiSavedState = 0;
        try {
            wifiSavedState = Settings.Secure.getInt(cr, Settings.Secure.WIFI_SAVED_STATE);
            if(wifiSavedState == 1)
                Settings.Secure.putInt(cr, Settings.Secure.WIFI_SAVED_STATE, 0);
        } catch (Settings.SettingNotFoundException e) {
            ;
        }
        return (wifiSavedState == 1);
    }

    private int getPersistedWifiState() {
        final ContentResolver cr = mContext.getContentResolver();
        try {
            return Settings.Secure.getInt(cr, Settings.Secure.WIFI_ON);
        } catch (Settings.SettingNotFoundException e) {
            Settings.Secure.putInt(cr, Settings.Secure.WIFI_ON, WIFI_DISABLED);
            return WIFI_DISABLED;
        }
    }

    private boolean shouldWifiBeEnabled() {
        if (mAirplaneModeOn.get()) {
            return mPersistWifiState.get() == WIFI_ENABLED_AIRPLANE_OVERRIDE;
        } else {
            return mPersistWifiState.get() != WIFI_DISABLED;
        }
    }

    private void persistWifiState(boolean enabled) {
        final ContentResolver cr = mContext.getContentResolver();
        boolean airplane = mAirplaneModeOn.get() && isAirplaneToggleable();
        if (enabled) {
            if (airplane) {
                mPersistWifiState.set(WIFI_ENABLED_AIRPLANE_OVERRIDE);
            } else {
                mPersistWifiState.set(WIFI_ENABLED);
            }
        } else {
            if (airplane) {
                mPersistWifiState.set(WIFI_DISABLED_AIRPLANE_ON);
            } else {
                mPersistWifiState.set(WIFI_DISABLED);
            }
        }

        Settings.Secure.putInt(cr, Settings.Secure.WIFI_ON, mPersistWifiState.get());
    }


    /**
     * see {@link android.net.wifi.WifiManager#pingSupplicant()}
     * @return {@code true} if the operation succeeds, {@code false} otherwise
     */
    public boolean pingSupplicant() {
        enforceAccessPermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncPingSupplicant(mWifiStateMachineChannel);
        } else {
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return false;
        }
    }

    /**
     * see {@link android.net.wifi.WifiManager#startScan()}
     */
    public void startScan(boolean forceActive) {
        enforceChangePermission();
        mWifiStateMachine.startScan(forceActive);
    }

    private void enforceAccessPermission() {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.ACCESS_WIFI_STATE,
                                                "WifiService");
    }

    private void enforceChangePermission() {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.CHANGE_WIFI_STATE,
                                                "WifiService");

    }

    private void enforceMulticastChangePermission() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                "WifiService");
    }

    /**
     * see {@link android.net.wifi.WifiManager#setWifiEnabled(boolean)}
     * @param enable {@code true} to enable, {@code false} to disable.
     * @return {@code true} if the enable/disable operation was
     *         started or is already in the queue.
     */
    public synchronized boolean setWifiEnabled(boolean enable) {
        enforceChangePermission();
        SXlog.d(TAG, "setWifiEnabled: " + enable + " pid=" + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
        if (DBG) {
            Slog.e(TAG, "Invoking mWifiStateMachine.setWifiEnabled\n");
        }

        if (enable) {
            reportStartWorkSource();
        }
        
        mWifiStateMachine.setWifiEnabled(enable);

        /*
         * Caller might not have WRITE_SECURE_SETTINGS,
         * only CHANGE_WIFI_STATE is enforced
         */

        /* Avoids overriding of airplane state when wifi is already in the expected state */
        if (enable != mWifiEnabled) {
            long ident = Binder.clearCallingIdentity();
            persistWifiState(enable);
            Binder.restoreCallingIdentity(ident);
        }

        if (enable) {
            if (!mIsReceiverRegistered) {
                registerForBroadcasts();
                mIsReceiverRegistered = true;
            }
        } else if (mIsReceiverRegistered) {
            mContext.unregisterReceiver(mReceiver);
            mIsReceiverRegistered = false;
        }

        return true;
    }

    public synchronized boolean setWifiEnabledForIPO(boolean enable) {
        enforceChangePermission();
        SXlog.d(TAG, "setWifiEnabledForIPO: " + enable + " pid=" + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
        if (DBG) {
            Slog.e(TAG, "Invoking mWifiStateMachine.setWifiEnabled\n");
        }

        if (enable) {
            reportStartWorkSource();
        }
        
        mWifiStateMachine.setWifiEnabled(enable);

        /*
         * Caller might not have WRITE_SECURE_SETTINGS,
         * only CHANGE_WIFI_STATE is enforced
         */

        /* Avoids overriding of airplane state when wifi is already in the expected state */
        if (enable != mWifiEnabled) {
            long ident = Binder.clearCallingIdentity();
            //persistWifiState(enable);
            Binder.restoreCallingIdentity(ident);
        }

        if (enable) {
            if (!mIsReceiverRegistered) {
                registerForBroadcasts();
                mIsReceiverRegistered = true;
            }
        } else if (mIsReceiverRegistered) {
            mContext.unregisterReceiver(mReceiver);
            mIsReceiverRegistered = false;
        }

        return true;
    }

    /**
     * see {@link WifiManager#getWifiState()}
     * @return One of {@link WifiManager#WIFI_STATE_DISABLED},
     *         {@link WifiManager#WIFI_STATE_DISABLING},
     *         {@link WifiManager#WIFI_STATE_ENABLED},
     *         {@link WifiManager#WIFI_STATE_ENABLING},
     *         {@link WifiManager#WIFI_STATE_UNKNOWN}
     */
    public int getWifiEnabledState() {
        enforceAccessPermission();
        return mWifiStateMachine.syncGetWifiState();
    }

    /**
     * see {@link android.net.wifi.WifiManager#setWifiApEnabled(WifiConfiguration, boolean)}
     * @param wifiConfig SSID, security and channel details as
     *        part of WifiConfiguration
     * @param enabled true to enable and false to disable
     */
    public void setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        enforceChangePermission();
        mWifiStateMachine.setWifiApEnabled(wifiConfig, enabled);
    }

    /**
     * see {@link WifiManager#getWifiApState()}
     * @return One of {@link WifiManager#WIFI_AP_STATE_DISABLED},
     *         {@link WifiManager#WIFI_AP_STATE_DISABLING},
     *         {@link WifiManager#WIFI_AP_STATE_ENABLED},
     *         {@link WifiManager#WIFI_AP_STATE_ENABLING},
     *         {@link WifiManager#WIFI_AP_STATE_FAILED}
     */
    public int getWifiApEnabledState() {
        enforceAccessPermission();
        return mWifiStateMachine.syncGetWifiApState();
    }

    /**
     * see {@link WifiManager#getWifiApConfiguration()}
     * @return soft access point configuration
     */
    public WifiConfiguration getWifiApConfiguration() {
        enforceAccessPermission();
        return mWifiStateMachine.syncGetWifiApConfiguration();
    }

    /**
     * see {@link WifiManager#setWifiApConfiguration(WifiConfiguration)}
     * @param wifiConfig WifiConfiguration details for soft access point
     */
    public void setWifiApConfiguration(WifiConfiguration wifiConfig) {
        enforceChangePermission();
        if (wifiConfig == null)
            return;
        mWifiStateMachine.setWifiApConfiguration(wifiConfig);
    }

    /**
     * see {@link android.net.wifi.WifiManager#disconnect()}
     */
    public void disconnect() {
        enforceChangePermission();
        mWifiStateMachine.disconnectCommand();
    }

    /**
     * see {@link android.net.wifi.WifiManager#reconnect()}
     */
    public void reconnect() {
        enforceChangePermission();
        mWifiStateMachine.reconnectCommand();
    }

    /**
     * see {@link android.net.wifi.WifiManager#reassociate()}
     */
    public void reassociate() {
        enforceChangePermission();
        mWifiStateMachine.reassociateCommand();
    }

    /**
     * see {@link android.net.wifi.WifiManager#getConfiguredNetworks()}
     * @return the list of configured networks
     */
    public List<WifiConfiguration> getConfiguredNetworks() {
        enforceAccessPermission();
        return mWifiStateMachine.syncGetConfiguredNetworks();
    }

    /**
     * see {@link android.net.wifi.WifiManager#addOrUpdateNetwork(WifiConfiguration)}
     * @return the supplicant-assigned identifier for the new or updated
     * network if the operation succeeds, or {@code -1} if it fails
     */
    public int addOrUpdateNetwork(WifiConfiguration config) {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncAddOrUpdateNetwork(mWifiStateMachineChannel, config);
        } else {
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return -1;
        }
    }

     /**
     * See {@link android.net.wifi.WifiManager#removeNetwork(int)}
     * @param netId the integer that identifies the network configuration
     * to the supplicant
     * @return {@code true} if the operation succeeded
     */
    public boolean removeNetwork(int netId) {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncRemoveNetwork(mWifiStateMachineChannel, netId);
        } else {
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return false;
        }
    }

    /**
     * See {@link android.net.wifi.WifiManager#enableNetwork(int, boolean)}
     * @param netId the integer that identifies the network configuration
     * to the supplicant
     * @param disableOthers if true, disable all other networks.
     * @return {@code true} if the operation succeeded
     */
    public boolean enableNetwork(int netId, boolean disableOthers) {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncEnableNetwork(mWifiStateMachineChannel, netId,
                    disableOthers);
        } else {
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return false;
        }
    }

    /**
     * See {@link android.net.wifi.WifiManager#disableNetwork(int)}
     * @param netId the integer that identifies the network configuration
     * to the supplicant
     * @return {@code true} if the operation succeeded
     */
    public boolean disableNetwork(int netId) {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncDisableNetwork(mWifiStateMachineChannel, netId);
        } else {
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return false;
        }
    }

    /**
     * See {@link android.net.wifi.WifiManager#getConnectionInfo()}
     * @return the Wi-Fi information, contained in {@link WifiInfo}.
     */
    public WifiInfo getConnectionInfo() {
        enforceAccessPermission();
        /*
         * Make sure we have the latest information, by sending
         * a status request to the supplicant.
         */
        if (mWifiStateMachineChannel != null) {
            mWifiStateMachine.syncUpdateRssi(mWifiStateMachineChannel);
        } else {
            SXlog.e(TAG, "mWifiStateMachineChannel is not initialized");
        }
        return mWifiStateMachine.syncRequestConnectionInfo();
    }

    /**
     * Return the results of the most recent access point scan, in the form of
     * a list of {@link ScanResult} objects.
     * @return the list of results
     */
    public List<ScanResult> getScanResults() {
        enforceAccessPermission();
        return mWifiStateMachine.syncGetScanResultsList();
    }

    /**
     * Tell the supplicant to persist the current list of configured networks.
     * @return {@code true} if the operation succeeded
     *
     * TODO: deprecate this
     */
    public boolean saveConfiguration() {
        boolean result = true;
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncSaveConfig(mWifiStateMachineChannel);
        } else {
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return false;
        }
    }

    /**
     * Set the country code
     * @param countryCode ISO 3166 country code.
     * @param persist {@code true} if the setting should be remembered.
     *
     * The persist behavior exists so that wifi can fall back to the last
     * persisted country code on a restart, when the locale information is
     * not available from telephony.
     */
    public void setCountryCode(String countryCode, boolean persist) {
        Slog.i(TAG, "WifiService trying to set country code to " + countryCode +
                " with persist set to " + persist);
        enforceChangePermission();
        mWifiStateMachine.setCountryCode(countryCode, persist);
    }

    /**
     * Set the operational frequency band
     * @param band One of
     *     {@link WifiManager#WIFI_FREQUENCY_BAND_AUTO},
     *     {@link WifiManager#WIFI_FREQUENCY_BAND_5GHZ},
     *     {@link WifiManager#WIFI_FREQUENCY_BAND_2GHZ},
     * @param persist {@code true} if the setting should be remembered.
     *
     */
    public void setFrequencyBand(int band, boolean persist) {
        enforceChangePermission();
        if (!isDualBandSupported()) return;
        Slog.i(TAG, "WifiService trying to set frequency band to " + band +
                " with persist set to " + persist);
        mWifiStateMachine.setFrequencyBand(band, persist);
    }


    /**
     * Get the operational frequency band
     */
    public int getFrequencyBand() {
        enforceAccessPermission();
        return mWifiStateMachine.getFrequencyBand();
    }

    public boolean isDualBandSupported() {
        //TODO: Should move towards adding a driver API that checks at runtime
        return mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_wifi_dual_band_support);
    }


    /**
     * Return the list of channels that are allowed
     * to be used in the current regulatory domain.
     * @return the list of allowed channels, or null if an error occurs
     */
    public String[] getAccessPointPreferredChannels() {
        enforceAccessPermission();
        String[] preferredChannels = null;
        IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
        INetworkManagementService service = INetworkManagementService.Stub.asInterface(b);
        if (service != null) {
            try {
                preferredChannels = service.getSoftApPreferredChannel();
            } catch (Exception e) {
                SXlog.e(TAG, "Error get allowed channel list : " + e);
            }
        }
        return preferredChannels;
    }


    /**
     * Return the DHCP-assigned addresses from the last successful DHCP request,
     * if any.
     * @return the DHCP information
     */
    public DhcpInfo getDhcpInfo() {
        enforceAccessPermission();
        return mWifiStateMachine.syncGetDhcpInfo();
    }

    /**
     * see {@link android.net.wifi.WifiManager#startWifi}
     *
     */
    public void startWifi() {
        enforceChangePermission();
        /* TODO: may be add permissions for access only to connectivity service
         * TODO: if a start issued, keep wifi alive until a stop issued irrespective
         * of WifiLock & device idle status unless wifi enabled status is toggled
         */
        if (mWifiStateMachine.shouldStartWifi()) {
            mWifiStateMachine.setDriverStart(true, mEmergencyCallbackMode);
            mWifiStateMachine.reconnectCommand();
        }
    }

    /**
     * see {@link android.net.wifi.WifiManager#stopWifi}
     *
     */
    public void stopWifi() {
        enforceChangePermission();
        /* TODO: may be add permissions for access only to connectivity service
         * TODO: if a stop is issued, wifi is brought up only by startWifi
         * unless wifi enabled status is toggled
         */
        mWifiStateMachine.setDriverStart(false, mEmergencyCallbackMode);
    }


    /**
     * see {@link android.net.wifi.WifiManager#addToBlacklist}
     *
     */
    public void addToBlacklist(String bssid) {
        enforceChangePermission();

        mWifiStateMachine.addToBlacklist(bssid);
    }

    /**
     * see {@link android.net.wifi.WifiManager#clearBlacklist}
     *
     */
    public void clearBlacklist() {
        enforceChangePermission();

        mWifiStateMachine.clearBlacklist();
    }

    /**
     * Get a reference to handler. This is used by a client to establish
     * an AsyncChannel communication with WifiService
     */
    public Messenger getMessenger() {
        /* Enforce the highest permissions
           TODO: when we consider exposing the asynchronous API, think about
                 how to provide both access and change permissions seperately
         */
        enforceAccessPermission();
        enforceChangePermission();
        return new Messenger(mAsyncServiceHandler);
    }

    /**
     * Get the IP and proxy configuration file
     */
    public String getConfigFile() {
        enforceAccessPermission();
        return mWifiStateMachine.getConfigFile();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            SXlog.d(TAG, "onReceive, action=" + action);
            long idleMillis =
                Settings.Secure.getLong(mContext.getContentResolver(),
                                        Settings.Secure.WIFI_IDLE_MS, DEFAULT_IDLE_MS);
            int stayAwakeConditions =
                Settings.System.getInt(mContext.getContentResolver(),
                                       Settings.System.STAY_ON_WHILE_PLUGGED_IN, 0);
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                mAlarmManager.cancel(mIdleIntent);
                mScreenOff = false;
                evaluateTrafficStatsPolling();
                mWifiStateMachine.enableRssiPolling(true);
                if (mBackgroundScanSupported) {
                    mWifiStateMachine.enableBackgroundScanCommand(false);
                }
                //MTK_OP01_PROTECT_START
                if (!OPTR.equals("OP01"))
                //MTK_OP01_PROTECT_END
                mWifiStateMachine.enableAllNetworks();
                setDeviceIdleAndUpdateWifi(false);
                //MTK_OP01_PROTECT_START
                if (OPTR.equals("OP01")) {
                    mWifiStateMachine.updateScanInterval(true);
                }
                //MTK_OP01_PROTECT_END
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                mScreenOff = true;
                evaluateTrafficStatsPolling();
                mWifiStateMachine.enableRssiPolling(false);
                //MTK_OP01_PROTECT_START
                if (OPTR.equals("OP01")) {
                    mWifiStateMachine.updateScanInterval(false);
                }
                //MTK_OP01_PROTECT_END
                if (mBackgroundScanSupported) {
                    mWifiStateMachine.enableBackgroundScanCommand(true);
                }
                /*
                 * Set a timer to put Wi-Fi to sleep, but only if the screen is off
                 * AND the "stay on while plugged in" setting doesn't match the
                 * current power conditions (i.e, not plugged in, plugged in to USB,
                 * or plugged in to AC).
                 */
                if (!shouldWifiStayAwake(stayAwakeConditions, mPluggedType)) {
                    //Delayed shutdown if wifi is connected
                    if (mNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {
                        if (DBG) Slog.d(TAG, "setting ACTION_DEVICE_IDLE: " + idleMillis + " ms");
                        mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                                + idleMillis, mIdleIntent);
                    } else {
                        setDeviceIdleAndUpdateWifi(true);
                    }
                }
            } else if (action.equals(ACTION_DEVICE_IDLE)) {
                setDeviceIdleAndUpdateWifi(true);
            } else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                /*
                 * Set a timer to put Wi-Fi to sleep, but only if the screen is off
                 * AND we are transitioning from a state in which the device was supposed
                 * to stay awake to a state in which it is not supposed to stay awake.
                 * If "stay awake" state is not changing, we do nothing, to avoid resetting
                 * the already-set timer.
                 */
                int pluggedType = intent.getIntExtra("plugged", 0);
                if (DBG) {
                    Slog.d(TAG, "ACTION_BATTERY_CHANGED pluggedType: " + pluggedType);
                }
                if (mScreenOff && shouldWifiStayAwake(stayAwakeConditions, mPluggedType) &&
                        !shouldWifiStayAwake(stayAwakeConditions, pluggedType)) {
                    long triggerTime = System.currentTimeMillis() + idleMillis;
                    if (DBG) {
                        Slog.d(TAG, "setting ACTION_DEVICE_IDLE timer for " + idleMillis + "ms");
                    }
                    mAlarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, mIdleIntent);
                }
                mPluggedType = pluggedType;
            } else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE,
                        BluetoothAdapter.STATE_DISCONNECTED);
                mWifiStateMachine.sendBluetoothAdapterStateChange(state);
            } else if (action.equals(TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED)) {
                mEmergencyCallbackMode = intent.getBooleanExtra("phoneinECMState", false);
                updateWifiState();
            } else if (action.equals(WifiManager.WIFI_CLEAR_NOTIFICATION_SHOW_FLAG_ACTION)) {
                mNotificationShown = false;
            } else if (action.equals(ACTION_CHECK_CONNECTION)) {
            	SupplicantState supplicantState = mWifiStateMachine.syncRequestConnectionInfo().getSupplicantState();
            	SXlog.d(TAG, "supplicantState=" + supplicantState);
            	if (!(SupplicantState.AUTHENTICATING.ordinal() <= supplicantState.ordinal()
                    && supplicantState.ordinal() <= SupplicantState.COMPLETED.ordinal())) {
                    showGPRSDialog();
            	}
            }
        }

        /**
         * Determines whether the Wi-Fi chipset should stay awake or be put to
         * sleep. Looks at the setting for the sleep policy and the current
         * conditions.
         *
         * @see #shouldDeviceStayAwake(int, int)
         */
        private boolean shouldWifiStayAwake(int stayAwakeConditions, int pluggedType) {
            //Never sleep as long as the user has not changed the settings
            int defaultValue = Settings.System.WIFI_SLEEP_POLICY_NEVER;
            //MTK_OP01_PROTECT_START
            if (OPTR.equals("OP01")) {
                defaultValue = Settings.System.WIFI_SLEEP_POLICY_DEFAULT;
            }
            //MTK_OP01_PROTECT_END
            int wifiSleepPolicy = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.WIFI_SLEEP_POLICY,
                    defaultValue);
            SXlog.d(TAG, "wifiSleepPolicy:" + wifiSleepPolicy);
            if (wifiSleepPolicy == Settings.System.WIFI_SLEEP_POLICY_NEVER) {
                // Never sleep
                return true;
            } else if ((wifiSleepPolicy == Settings.System.WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED) &&
                    (pluggedType != 0)) {
                // Never sleep while plugged, and we're plugged
                return true;
            } else {
                // Default
                return shouldDeviceStayAwake(stayAwakeConditions, pluggedType);
            }
        }

        /**
         * Determine whether the bit value corresponding to {@code pluggedType} is set in
         * the bit string {@code stayAwakeConditions}. Because a {@code pluggedType} value
         * of {@code 0} isn't really a plugged type, but rather an indication that the
         * device isn't plugged in at all, there is no bit value corresponding to a
         * {@code pluggedType} value of {@code 0}. That is why we shift by
         * {@code pluggedType - 1} instead of by {@code pluggedType}.
         * @param stayAwakeConditions a bit string specifying which "plugged types" should
         * keep the device (and hence Wi-Fi) awake.
         * @param pluggedType the type of plug (USB, AC, or none) for which the check is
         * being made
         * @return {@code true} if {@code pluggedType} indicates that the device is
         * supposed to stay awake, {@code false} otherwise.
         */
        private boolean shouldDeviceStayAwake(int stayAwakeConditions, int pluggedType) {
            return (stayAwakeConditions & pluggedType) != 0;
        }
    };

    private void setDeviceIdleAndUpdateWifi(boolean deviceIdle) {
        mWifiStateMachine.setDeviceIdle(deviceIdle);
        mDeviceIdle = deviceIdle;
        reportStartWorkSource();
        updateWifiState();
    }

    private synchronized void reportStartWorkSource() {
        mTmpWorkSource.clear();
        if (mDeviceIdle) {
            for (int i=0; i<mLocks.mList.size(); i++) {
                mTmpWorkSource.add(mLocks.mList.get(i).mWorkSource);
            }
        }
        mWifiStateMachine.updateBatteryWorkSource(mTmpWorkSource);
    }

    private void updateWifiState() {
        boolean lockHeld = mLocks.hasLocks();
        int strongestLockMode = WifiManager.WIFI_MODE_FULL;
        boolean wifiShouldBeStarted;
        boolean wifiShouldBeEnabled;

        if (mEmergencyCallbackMode) {
            wifiShouldBeStarted = false;
        } else {
            wifiShouldBeStarted = !mDeviceIdle || lockHeld;
        }

        if (lockHeld) {
            strongestLockMode = mLocks.getStrongestLockMode();
        }
        /* If device is not idle, lockmode cannot be scan only */
        if (!mDeviceIdle && strongestLockMode == WifiManager.WIFI_MODE_SCAN_ONLY) {
            strongestLockMode = WifiManager.WIFI_MODE_FULL;
        }

        mAirplaneModeOn.set(isAirplaneModeOn());
        
        /* Disable tethering when airplane mode is enabled */
        if (mAirplaneModeOn.get()) {
            mWifiStateMachine.setWifiApEnabled(null, false);
        }

        wifiShouldBeEnabled = shouldWifiBeEnabled();
        SXlog.d(TAG, "updateWifiState, wifiShouldBeEnabled:" + wifiShouldBeEnabled + ", wifiShouldBeStarted:" + wifiShouldBeStarted);
        if (wifiShouldBeEnabled) {
            if (wifiShouldBeStarted) {
                reportStartWorkSource();
                mWifiStateMachine.setWifiEnabled(true);
                mWifiStateMachine.setScanOnlyMode(
                        strongestLockMode == WifiManager.WIFI_MODE_SCAN_ONLY);
                mWifiStateMachine.setDriverStart(true, mEmergencyCallbackMode);
                mWifiStateMachine.setHighPerfModeEnabled(strongestLockMode
                        == WifiManager.WIFI_MODE_FULL_HIGH_PERF);
                mWifiStateMachine.startScan(true);
            } else {
                mWifiStateMachine.setDriverStart(false, mEmergencyCallbackMode);
            }
        } else {
            mWifiStateMachine.setWifiEnabled(false);
        }
    }

    private void registerForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(ACTION_DEVICE_IDLE);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED);
        intentFilter.addAction(WifiManager.WIFI_CLEAR_NOTIFICATION_SHOW_FLAG_ACTION);
        intentFilter.addAction(ACTION_CHECK_CONNECTION);
        mContext.registerReceiver(mReceiver, intentFilter);
    }

    private boolean isAirplaneSensitive() {
        String airplaneModeRadios = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_RADIOS);
        return airplaneModeRadios == null
            || airplaneModeRadios.contains(Settings.System.RADIO_WIFI);
    }

    private boolean isAirplaneToggleable() {
        String toggleableRadios = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        return toggleableRadios != null
            && toggleableRadios.contains(Settings.System.RADIO_WIFI);
    }

    /**
     * Returns true if Wi-Fi is sensitive to airplane mode, and airplane mode is
     * currently on.
     * @return {@code true} if airplane mode is on.
     */
    private boolean isAirplaneModeOn() {
        return isAirplaneSensitive() && Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) == 1;
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump WifiService from from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return;
        }
        pw.println("Wi-Fi is " + mWifiStateMachine.syncGetWifiStateByName());
        pw.println("Stay-awake conditions: " +
                Settings.System.getInt(mContext.getContentResolver(),
                                       Settings.System.STAY_ON_WHILE_PLUGGED_IN, 0));
        pw.println();

        pw.println("Internal state:");
        pw.println(mWifiStateMachine);
        pw.println();
        pw.println("Latest scan results:");
        List<ScanResult> scanResults = mWifiStateMachine.syncGetScanResultsList();
        if (scanResults != null && scanResults.size() != 0) {
            pw.println("  BSSID              Frequency   RSSI  Flags             SSID");
            for (ScanResult r : scanResults) {
                pw.printf("  %17s  %9d  %5d  %-16s  %s%n",
                                         r.BSSID,
                                         r.frequency,
                                         r.level,
                                         r.capabilities,
                                         r.SSID == null ? "" : r.SSID);
            }
        }
        pw.println();
        pw.println("Locks acquired: " + mFullLocksAcquired + " full, " +
                mFullHighPerfLocksAcquired + " full high perf, " +
                mScanLocksAcquired + " scan");
        pw.println("Locks released: " + mFullLocksReleased + " full, " +
                mFullHighPerfLocksReleased + " full high perf, " +
                mScanLocksReleased + " scan");
        pw.println();
        pw.println("Locks held:");
        mLocks.dump(pw);

        pw.println();
        pw.println("WifiWatchdogStateMachine dump");
        mWifiWatchdogStateMachine.dump(pw);
    }

    private class WifiLock extends DeathRecipient {
        WifiLock(int lockMode, String tag, IBinder binder, WorkSource ws) {
            super(lockMode, tag, binder, ws);
        }

        public void binderDied() {
            synchronized (mLocks) {
                releaseWifiLockLocked(mBinder);
            }
        }

        public String toString() {
            return "WifiLock{" + mTag + " type=" + mMode + " binder=" + mBinder + "}";
        }
    }

    private class LockList {
        private List<WifiLock> mList;

        private LockList() {
            mList = new ArrayList<WifiLock>();
        }

        private synchronized boolean hasLocks() {
            return !mList.isEmpty();
        }

        private synchronized int getStrongestLockMode() {
            if (mList.isEmpty()) {
                return WifiManager.WIFI_MODE_FULL;
            }

            if (mFullHighPerfLocksAcquired > mFullHighPerfLocksReleased) {
                return WifiManager.WIFI_MODE_FULL_HIGH_PERF;
            }

            if (mFullLocksAcquired > mFullLocksReleased) {
                return WifiManager.WIFI_MODE_FULL;
            }

            return WifiManager.WIFI_MODE_SCAN_ONLY;
        }

        private void addLock(WifiLock lock) {
            if (findLockByBinder(lock.mBinder) < 0) {
                mList.add(lock);
            }
        }

        private WifiLock removeLock(IBinder binder) {
            int index = findLockByBinder(binder);
            if (index >= 0) {
                WifiLock ret = mList.remove(index);
                ret.unlinkDeathRecipient();
                return ret;
            } else {
                return null;
            }
        }

        private int findLockByBinder(IBinder binder) {
            int size = mList.size();
            for (int i = size - 1; i >= 0; i--)
                if (mList.get(i).mBinder == binder)
                    return i;
            return -1;
        }

        private void dump(PrintWriter pw) {
            for (WifiLock l : mList) {
                pw.print("    ");
                pw.println(l);
            }
        }
    }

    void enforceWakeSourcePermission(int uid, int pid) {
        if (uid == android.os.Process.myUid()) {
            return;
        }
        mContext.enforcePermission(android.Manifest.permission.UPDATE_DEVICE_STATS,
                pid, uid, null);
    }

    public boolean acquireWifiLock(IBinder binder, int lockMode, String tag, WorkSource ws) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.WAKE_LOCK, null);
        if (lockMode != WifiManager.WIFI_MODE_FULL &&
                lockMode != WifiManager.WIFI_MODE_SCAN_ONLY &&
                lockMode != WifiManager.WIFI_MODE_FULL_HIGH_PERF) {
            Slog.e(TAG, "Illegal argument, lockMode= " + lockMode);
            if (DBG) throw new IllegalArgumentException("lockMode=" + lockMode);
            return false;
        }
        if (ws != null && ws.size() == 0) {
            ws = null;
        }
        if (ws != null) {
            enforceWakeSourcePermission(Binder.getCallingUid(), Binder.getCallingPid());
        }
        if (ws == null) {
            ws = new WorkSource(Binder.getCallingUid());
        }
        WifiLock wifiLock = new WifiLock(lockMode, tag, binder, ws);
        synchronized (mLocks) {
            return acquireWifiLockLocked(wifiLock);
        }
    }

    private void noteAcquireWifiLock(WifiLock wifiLock) throws RemoteException {
        switch(wifiLock.mMode) {
            case WifiManager.WIFI_MODE_FULL:
            case WifiManager.WIFI_MODE_FULL_HIGH_PERF:
                mBatteryStats.noteFullWifiLockAcquiredFromSource(wifiLock.mWorkSource);
                break;
            case WifiManager.WIFI_MODE_SCAN_ONLY:
                mBatteryStats.noteScanWifiLockAcquiredFromSource(wifiLock.mWorkSource);
                break;
        }
    }

    private void noteReleaseWifiLock(WifiLock wifiLock) throws RemoteException {
        switch(wifiLock.mMode) {
            case WifiManager.WIFI_MODE_FULL:
            case WifiManager.WIFI_MODE_FULL_HIGH_PERF:
                mBatteryStats.noteFullWifiLockReleasedFromSource(wifiLock.mWorkSource);
                break;
            case WifiManager.WIFI_MODE_SCAN_ONLY:
                mBatteryStats.noteScanWifiLockReleasedFromSource(wifiLock.mWorkSource);
                break;
        }
    }

    private boolean acquireWifiLockLocked(WifiLock wifiLock) {
        if (DBG) Slog.d(TAG, "acquireWifiLockLocked: " + wifiLock);

        mLocks.addLock(wifiLock);

        long ident = Binder.clearCallingIdentity();
        try {
            noteAcquireWifiLock(wifiLock);
            switch(wifiLock.mMode) {
            case WifiManager.WIFI_MODE_FULL:
                ++mFullLocksAcquired;
                break;
            case WifiManager.WIFI_MODE_FULL_HIGH_PERF:
                ++mFullHighPerfLocksAcquired;
                break;

            case WifiManager.WIFI_MODE_SCAN_ONLY:
                ++mScanLocksAcquired;
                break;
            }

            // Be aggressive about adding new locks into the accounted state...
            // we want to over-report rather than under-report.
            reportStartWorkSource();

            updateWifiState();
            return true;
        } catch (RemoteException e) {
            return false;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void updateWifiLockWorkSource(IBinder lock, WorkSource ws) {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        if (ws != null && ws.size() == 0) {
            ws = null;
        }
        if (ws != null) {
            enforceWakeSourcePermission(uid, pid);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (mLocks) {
                int index = mLocks.findLockByBinder(lock);
                if (index < 0) {
                    throw new IllegalArgumentException("Wifi lock not active");
                }
                WifiLock wl = mLocks.mList.get(index);
                noteReleaseWifiLock(wl);
                wl.mWorkSource = ws != null ? new WorkSource(ws) : new WorkSource(uid);
                noteAcquireWifiLock(wl);
            }
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean releaseWifiLock(IBinder lock) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.WAKE_LOCK, null);
        synchronized (mLocks) {
            return releaseWifiLockLocked(lock);
        }
    }

    private boolean releaseWifiLockLocked(IBinder lock) {
        boolean hadLock;

        WifiLock wifiLock = mLocks.removeLock(lock);

        if (DBG) Slog.d(TAG, "releaseWifiLockLocked: " + wifiLock);

        hadLock = (wifiLock != null);

        long ident = Binder.clearCallingIdentity();
        try {
            if (hadLock) {
                noteReleaseWifiLock(wifiLock);
                switch(wifiLock.mMode) {
                    case WifiManager.WIFI_MODE_FULL:
                        ++mFullLocksReleased;
                        break;
                    case WifiManager.WIFI_MODE_FULL_HIGH_PERF:
                        ++mFullHighPerfLocksReleased;
                        break;
                    case WifiManager.WIFI_MODE_SCAN_ONLY:
                        ++mScanLocksReleased;
                        break;
                }
            }

            // TODO - should this only happen if you hadLock?
            updateWifiState();

        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }

        return hadLock;
    }

    private abstract class DeathRecipient
            implements IBinder.DeathRecipient {
        String mTag;
        int mMode;
        IBinder mBinder;
        WorkSource mWorkSource;

        DeathRecipient(int mode, String tag, IBinder binder, WorkSource ws) {
            super();
            mTag = tag;
            mMode = mode;
            mBinder = binder;
            mWorkSource = ws;
            try {
                mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        void unlinkDeathRecipient() {
            mBinder.unlinkToDeath(this, 0);
        }
    }

    private class Multicaster extends DeathRecipient {
        Multicaster(String tag, IBinder binder) {
            super(Binder.getCallingUid(), tag, binder, null);
        }

        public void binderDied() {
            Slog.e(TAG, "Multicaster binderDied");
            synchronized (mMulticasters) {
                int i = mMulticasters.indexOf(this);
                if (i != -1) {
                    removeMulticasterLocked(i, mMode);
                }
            }
        }

        public String toString() {
            return "Multicaster{" + mTag + " binder=" + mBinder + "}";
        }

        public int getUid() {
            return mMode;
        }
    }

    public void initializeMulticastFiltering() {
        enforceMulticastChangePermission();

        synchronized (mMulticasters) {
            // if anybody had requested filters be off, leave off
            if (mMulticasters.size() != 0) {
                return;
            } else {
                mWifiStateMachine.startFilteringMulticastV4Packets();
            }
        }
    }

    public void acquireMulticastLock(IBinder binder, String tag) {
        enforceMulticastChangePermission();

        synchronized (mMulticasters) {
            mMulticastEnabled++;
            mMulticasters.add(new Multicaster(tag, binder));
            // Note that we could call stopFilteringMulticastV4Packets only when
            // our new size == 1 (first call), but this function won't
            // be called often and by making the stopPacket call each
            // time we're less fragile and self-healing.
            mWifiStateMachine.stopFilteringMulticastV4Packets();
        }

        int uid = Binder.getCallingUid();
        Long ident = Binder.clearCallingIdentity();
        try {
            mBatteryStats.noteWifiMulticastEnabled(uid);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void releaseMulticastLock() {
        enforceMulticastChangePermission();

        int uid = Binder.getCallingUid();
        synchronized (mMulticasters) {
            mMulticastDisabled++;
            int size = mMulticasters.size();
            for (int i = size - 1; i >= 0; i--) {
                Multicaster m = mMulticasters.get(i);
                if ((m != null) && (m.getUid() == uid)) {
                    removeMulticasterLocked(i, uid);
                }
            }
        }
    }

    private void removeMulticasterLocked(int i, int uid)
    {
        Multicaster removed = mMulticasters.remove(i);

        if (removed != null) {
            removed.unlinkDeathRecipient();
        }
        if (mMulticasters.size() == 0) {
            mWifiStateMachine.startFilteringMulticastV4Packets();
        }

        Long ident = Binder.clearCallingIdentity();
        try {
            mBatteryStats.noteWifiMulticastDisabled(uid);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean isMulticastEnabled() {
        enforceAccessPermission();

        synchronized (mMulticasters) {
            return (mMulticasters.size() > 0);
        }
    }

    /**
     * Evaluate if traffic stats polling is needed based on
     * connection and screen on status
     */
    private void evaluateTrafficStatsPolling() {
        Message msg;
        if (mNetworkInfo.getDetailedState() == DetailedState.CONNECTED && !mScreenOff) {
            msg = Message.obtain(mAsyncServiceHandler,
                    WifiManager.CMD_ENABLE_TRAFFIC_STATS_POLL, 1, 0);
        } else {
            msg = Message.obtain(mAsyncServiceHandler,
                    WifiManager.CMD_ENABLE_TRAFFIC_STATS_POLL, 0, 0);
        }
        msg.sendToTarget();
    }

    private void notifyOnDataActivity() {
        long sent, received;
        long preTxPkts = mTxPkts, preRxPkts = mRxPkts;
        int dataActivity = WifiManager.DATA_ACTIVITY_NONE;

        mTxPkts = TrafficStats.getTxPackets(mInterfaceName);
        mRxPkts = TrafficStats.getRxPackets(mInterfaceName);

        if (preTxPkts > 0 || preRxPkts > 0) {
            sent = mTxPkts - preTxPkts;
            received = mRxPkts - preRxPkts;
            if (sent > 0) {
                dataActivity |= WifiManager.DATA_ACTIVITY_OUT;
            }
            if (received > 0) {
                dataActivity |= WifiManager.DATA_ACTIVITY_IN;
            }

            if (dataActivity != mDataActivity && !mScreenOff) {
                mDataActivity = dataActivity;
                for (AsyncChannel client : mClients) {
                    client.sendMessage(WifiManager.DATA_ACTIVITY_NOTIFICATION, mDataActivity);
                }
            }
        }
    }


    private void checkAndSetNotification() {
        // If we shouldn't place a notification on available networks, then
        // don't bother doing any of the following
        //if (!mNotificationEnabled) return;

        State state = mNetworkInfo.getState();
        List<ScanResult> scanResults = mWifiStateMachine.syncGetScanResultsList();
        List<WifiConfiguration> networks = getConfiguredNetworks();
        
        //MTK_OP01_PROTECT_START
        if (mWaitForScanResult && scanResults == null) {
            showGPRSDialog();
        }
        //MTK_OP01_PROTECT_END
            
        if (scanResults != null) {           
            //MTK_OP01_PROTECT_START
            if (!OPTR.equals("OP01")) {
            //MTK_OP01_PROTECT_END
                List<WifiConfiguration> rssiWeakNetworks = mWifiStateMachine.syncGetRssiWeakNetworks();
                if (networks != null && networks.size() > 0 && rssiWeakNetworks.size() > 0) {
                    List<WifiConfiguration> enableNetworks = new ArrayList<WifiConfiguration>();
                    for (WifiConfiguration network : networks) {
                        for (ScanResult scanresult : scanResults) {
                        //SXlog.d(TAG, "network.SSID=" + network.SSID + ", scanresult.SSID=" + scanresult.SSID);
                        //SXlog.d(TAG, "network.security=" + getSecurity(network) + ", scanresult.security=" + getSecurity(scanresult));
                        if ((network.SSID != null) && (scanresult.SSID != null) 
                            && network.SSID.equals("\"" + scanresult.SSID + "\"") 
                            && (getSecurity(network) == getSecurity(scanresult))) {
                                if (scanresult.level > WifiStateMachine.RSSI_THRESHOLD) {
                                    for (WifiConfiguration weakNetwork : rssiWeakNetworks) {
                                        if (network.SSID.equals(weakNetwork.SSID) && getSecurity(network) == getSecurity(weakNetwork)) {                                               
                                            SXlog.i(TAG, "Rssi > " + WifiStateMachine.RSSI_THRESHOLD + ", enabling network " + network.networkId);
                                            enableNetwork(network.networkId, false);
                                            enableNetworks.add(weakNetwork);
                                        }
                                    }
                                } else {
                                    SXlog.d(TAG, "Rssi of network " + network.SSID + " is smaller than " + WifiStateMachine.RSSI_THRESHOLD);
                                }
                            }
                        }
                    }
                    if (enableNetworks.size() > 0) {
                        mWifiStateMachine.startScan(true);
                    }
                    for (WifiConfiguration enableNetwork : enableNetworks) {
                        rssiWeakNetworks.remove(enableNetwork);
                    }
                    mWifiStateMachine.syncSetRssiWeakNetwork(rssiWeakNetworks);
                }
            //MTK_OP01_PROTECT_START
            }
            //MTK_OP01_PROTECT_END
        }
        
        if ((state == NetworkInfo.State.DISCONNECTED)
                || (state == NetworkInfo.State.UNKNOWN)) {
            // Look for an open network
            if (scanResults != null) {
                int numOpenNetworks = 0;
                for (int i = scanResults.size() - 1; i >= 0; i--) {
                    ScanResult scanResult = scanResults.get(i);

                    //A capability of [ESS] represents an open access point
                    //that is available for an STA to connect
                    if (scanResult.capabilities != null &&
                            scanResult.capabilities.equals("[ESS]")) {
                        numOpenNetworks++;
                    }
                }
                
                boolean isConnecting = false;
                int networkId = mWifiStateMachine.syncGetConnectingNetworkId(mWifiStateMachineChannel);
                
                //MTK_OP01_PROTECT_START
                SXlog.d(TAG, "OPTR=" + OPTR + ", mConnectPolicy=" + mConnectPolicy + ", mCellToWiFiPolicy=" + mCellToWiFiPolicy);
                if (OPTR.equals("OP01")) {
                    mAutoConnect = false;
                    ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo info = cm.getActiveNetworkInfo();
                    if (null == info) {
                        SXlog.d(TAG, "No active network");
                    } else {
                        SXlog.d(TAG, "Active network type:" + info.getTypeName());
                    }
                  
                    String highestPriorityNetworkSSID = null;
                    int highestPriority = -1;
                    int highestPriorityNetworkId = -1;
                    
                    if (null != networks) {
                        for (WifiConfiguration network : networks) {
                            for (ScanResult scanresult : scanResults) {
                                //SXlog.d(TAG, "network.SSID=" + network.SSID + ", scanresult.SSID=" + scanresult.SSID);
                                //SXlog.d(TAG, "network.security=" + getSecurity(network) + ", scanresult.security=" + getSecurity(scanresult));
                                if ((network.SSID != null) && (scanresult.SSID != null) 
                                    && network.SSID.equals("\"" + scanresult.SSID + "\"") 
                                    && (getSecurity(network) == getSecurity(scanresult))) {
                                    
                                    if (network.priority > highestPriority) {
                                        highestPriority = network.priority;
                                        highestPriorityNetworkId = network.networkId;
                                        highestPriorityNetworkSSID = network.SSID;
                                    }
                                    if (network.networkId == networkId) {
                                        isConnecting = true;
                                    }
                                }
                            }
                        }
                    }
                    if (!isConnecting) {
                        if (null != info && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                            SXlog.d(TAG, "highestPriorityNetworkId=" + highestPriorityNetworkId + ", highestPriorityNetworkSSID=" + highestPriorityNetworkSSID);
                            SXlog.d(TAG, "currentTimeMillis=" + System.currentTimeMillis() + ", mSuspendNotificationTime=" + mSuspendNotificationTime);
                            if (mConnectPolicy == Settings.System.WIFI_CONNECT_AP_TYPE_AUTO) {
                                if (mCellToWiFiPolicy == Settings.System.WIFI_CONNECT_TYPE_ASK) {
                                    if (highestPriorityNetworkId != -1 && !TextUtils.isEmpty(highestPriorityNetworkSSID)
                                        && (System.currentTimeMillis() - mSuspendNotificationTime > sSuspendNotificationDuration)) {
                                        Intent intent = new Intent(WifiManager.WIFI_NOTIFICATION_ACTION);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra(WifiManager.EXTRA_NOTIFICATION_SSID, highestPriorityNetworkSSID);
                                        intent.putExtra(WifiManager.EXTRA_NOTIFICATION_NETWORKID, highestPriorityNetworkId);
                                        mContext.startActivity(intent);
                                    }
                                } else if (mCellToWiFiPolicy == Settings.System.WIFI_CONNECT_TYPE_AUTO) {
                                    if (highestPriorityNetworkId != -1 && !TextUtils.isEmpty(highestPriorityNetworkSSID)) {
                                        enableAllNetworks();
                                    }
                                }
                            } else {
                                ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);   
                                ComponentName cn = null;
                                String classname = null;
                                if (am.getRunningTasks(1) != null && am.getRunningTasks(1).get(0) != null) {
                                    cn = am.getRunningTasks(1).get(0).topActivity;
                                }
                                if (cn != null) {
                                    classname = cn.getClassName();
                                    SXlog.d(TAG, "Class Name:" + classname); 
                                } else {
                                    SXlog.e(TAG, "ComponentName is null");
                                }
                                if (mCellToWiFiPolicy == Settings.System.WIFI_CONNECT_TYPE_ASK &&
                                    !(classname != null && classname.equals(mWifiSettingsClassName))) {
                                    if (highestPriorityNetworkId != -1 && !TextUtils.isEmpty(highestPriorityNetworkSSID)
                                        && (System.currentTimeMillis() - mSuspendNotificationTime > sSuspendNotificationDuration)) {
                                        Intent intent = new Intent(WifiManager.WIFI_NOTIFICATION_ACTION);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra(WifiManager.EXTRA_NOTIFICATION_SSID, highestPriorityNetworkSSID);
                                        intent.putExtra(WifiManager.EXTRA_NOTIFICATION_NETWORKID, highestPriorityNetworkId);
                                        mContext.startActivity(intent);
                                    }
                                }
                            }
                        } else {
                            if (mConnectPolicy == Settings.System.WIFI_CONNECT_AP_TYPE_AUTO) {
                                if (highestPriorityNetworkId != -1 && !TextUtils.isEmpty(highestPriorityNetworkSSID)) {
                                    enableAllNetworks();
                                }
                            }
                        }
                    }
                    SXlog.d(TAG, "mAutoConnect:" + mAutoConnect + ", isConnecting:" + isConnecting + ", mWaitForScanResult:" + mWaitForScanResult);
                    if (mAutoConnect || isConnecting) {
                        mWaitForScanResult = false;
                        return;
                    } else {
                        if (mWaitForScanResult) {
                            showGPRSDialog();
                        }
                    }
                } else 
                //MTK_OP01_PROTECT_END
                {
                    if (null != networks) {
                    LOOP:
                        for (WifiConfiguration network : networks) {
                            for (ScanResult scanresult : scanResults) {
                                //SXlog.d(TAG, "network.SSID=" + network.SSID + ", scanresult.SSID=" + scanresult.SSID);
                                //SXlog.d(TAG, "network.security=" + getSecurity(network) + ", scanresult.security=" + getSecurity(scanresult));
                                if ((network.SSID != null) && (scanresult.SSID != null) 
                                    && network.SSID.equals("\"" + scanresult.SSID + "\"") 
                                    && (getSecurity(network) == getSecurity(scanresult))
                                    && network.networkId == networkId) {
                                    isConnecting = true;
                                    break LOOP;
                                }
                            }
                        }
                    }
                }
                
                SXlog.i(TAG, "Open network num:" + numOpenNetworks);
                if (numOpenNetworks > 0) {
                    if (++mNumScansSinceNetworkStateChange >= NUM_SCANS_BEFORE_ACTUALLY_SCANNING) {
                        /*
                         * We've scanned continuously at least
                         * NUM_SCANS_BEFORE_NOTIFICATION times. The user
                         * probably does not have a remembered network in range,
                         * since otherwise supplicant would have tried to
                         * associate and thus resetting this counter.
                         */
                        
                        SupplicantState supplicantState = mWifiStateMachine.syncRequestConnectionInfo().getSupplicantState();
                        SXlog.d(TAG, "Supplicant state is " + supplicantState + " when interpret scan results, isConnecting=" + isConnecting);
                        if (!(SupplicantState.AUTHENTICATING.ordinal() <= supplicantState.ordinal() && supplicantState.ordinal() <= SupplicantState.COMPLETED.ordinal())) {
                            if (!isConnecting) {
                                setNotificationVisible(true, numOpenNetworks, false, 0);
                            }
                        }
                    }
                    return;
                }
            }
        }

        // No open networks in range, remove the notification
        setNotificationVisible(false, 0, false, 0);
    }

    /**
     * Clears variables related to tracking whether a notification has been
     * shown recently and clears the current notification.
     */
    private void resetNotification() {
        mNotificationRepeatTime = 0;
        mNumScansSinceNetworkStateChange = 0;
        setNotificationVisible(false, 0, false, 0);
    }

    /**
     * Display or don't display a notification that there are open Wi-Fi networks.
     * @param visible {@code true} if notification should be visible, {@code false} otherwise
     * @param numNetworks the number networks seen
     * @param force {@code true} to force notification to be shown/not-shown,
     * even if it is already shown/not-shown.
     * @param delay time in milliseconds after which the notification should be made
     * visible or invisible.
     */
    private void setNotificationVisible(boolean visible, int numNetworks, boolean force,
            int delay) {

        // Since we use auto cancel on the notification, when the
        // mNetworksAvailableNotificationShown is true, the notification may
        // have actually been canceled.  However, when it is false we know
        // for sure that it is not being shown (it will not be shown any other
        // place than here)

        if (visible && !mNotificationEnabled) return;
        
        // If it should be hidden and it is already hidden, then noop
        if (!visible && !mNotificationShown && !force) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Message message;
        if (visible) {

            // Not enough time has passed to show the notification again
            if (System.currentTimeMillis() < mNotificationRepeatTime) {
                SXlog.d(TAG, "Not enough time has passed to show the notification again");
                return;
            }

            if (mNotification == null) {
                // Cache the Notification object.
                mNotification = new Notification();
                mNotification.when = 0;
                mNotification.icon = ICON_NETWORKS_AVAILABLE;
                mNotification.flags = Notification.FLAG_AUTO_CANCEL;
                Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                intent.putExtra(WifiManager.EXTRA_TRIGGERED_BY_NOTIFICATION, true);
                mNotification.contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            }

            CharSequence title = mContext.getResources().getQuantityText(
                    com.android.internal.R.plurals.wifi_available, numNetworks);
            CharSequence details = mContext.getResources().getQuantityText(
                    com.android.internal.R.plurals.wifi_available_detailed, numNetworks);
            mNotification.tickerText = title;
            mNotification.setLatestEventInfo(mContext, title, details, mNotification.contentIntent);

            mNotificationRepeatTime = System.currentTimeMillis() + NOTIFICATION_REPEAT_DELAY_MS;

            if (!mNotificationShown) {
                mNotification.sound = RingtoneManager.getActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_NOTIFICATION);
            } else {
                mNotification.sound = null;
            }
            SXlog.d(TAG, "Pop up notification, mNotification.sound=" + mNotification.sound);            
            notificationManager.notify(ICON_NETWORKS_AVAILABLE, mNotification);
        } else {
            notificationManager.cancel(ICON_NETWORKS_AVAILABLE);
        }

        mNotificationShown = visible;
    }

    private class NotificationEnabledSettingObserver extends ContentObserver {

        public NotificationEnabledSettingObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            ContentResolver cr = mContext.getContentResolver();
            cr.registerContentObserver(Settings.Secure.getUriFor(
                Settings.Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON), true, this);
            mNotificationEnabled = getValue();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            mNotificationEnabled = getValue();
            resetNotification();
        }

        private boolean getValue() {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 1) == 1;
        }
    }
    
    private class ConnectTypeObserver extends ContentObserver {

        public ConnectTypeObserver(Handler handler) {
            super(handler);
            ContentResolver cr = mContext.getContentResolver();
            cr.registerContentObserver(Settings.System.getUriFor(
                Settings.System.WIFI_CONNECT_AP_TYPE), false, this);
            cr.registerContentObserver(Settings.System.getUriFor(
                Settings.System.WIFI_CONNECT_TYPE), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            int connectType = Settings.System.getInt(mContext.getContentResolver(), 
                Settings.System.WIFI_CONNECT_AP_TYPE, Settings.System.WIFI_CONNECT_AP_TYPE_AUTO);
            int cellToWlan = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.WIFI_CONNECT_TYPE, Settings.System.WIFI_CONNECT_TYPE_AUTO);
            if (mCellToWiFiPolicy != cellToWlan || (mCellToWiFiPolicy == Settings.System.WIFI_CONNECT_TYPE_ASK && mConnectPolicy != connectType)) {
                mSuspendNotificationTime = 0;
            }
            mConnectPolicy = connectType;
            mCellToWiFiPolicy = cellToWlan;
            if (mWifiStateMachineChannel != null) {
                mWifiStateMachine.syncSetConnectPolicy(mWifiStateMachineChannel, mConnectPolicy, mCellToWiFiPolicy);
            } else {
                SXlog.e(TAG, "mWifiStateMachineChannel is not initialized");
            }
        }
    }
        
    public void suspendNotification() {
        enforceChangePermission();
        //MTK_OP01_PROTECT_START       
        if (OPTR.equals("OP01")) {
            mSuspendNotificationTime = System.currentTimeMillis();
            SXlog.d(TAG, "suspendNotification, mSuspendNotificationTime=" + mSuspendNotificationTime);
        }
        //MTK_OP01_PROTECT_END
    }

    public boolean saveAPPriority() {
        enforceChangePermission();
        boolean result = false;
        //MTK_OP01_PROTECT_START        
        if (mWifiStateMachineChannel != null) {
            result = mWifiStateMachine.syncSaveAPPriority(mWifiStateMachineChannel);
        } else {
            SXlog.e(TAG, "mWifiStateMachineChannel is not initialized");
        }
        //MTK_OP01_PROTECT_END
        return result;
    }

    public boolean hasConnectableAP() {
        enforceAccessPermission();
        boolean result = false;
        //MTK_OP01_PROTECT_START        
        if (mPersistWifiState.get() == WIFI_ENABLED || mPersistWifiState.get() == WIFI_ENABLED_AIRPLANE_OVERRIDE) {
            mWifiStateMachine.startScan(true);
            mWaitForScanResult = true;
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + 4000, mCheckConnectionIntent);
            result = true;
        }
        //MTK_OP01_PROTECT_END
        return result;
    }
        
    //MTK_OP01_PROTECT_START    
    private int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
            config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WAPI_PSK)) {
            return SECURITY_WAPI_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WAPI_CERT)) {
            return SECURITY_WAPI_CERT;
        }       
        if (config.wepTxKeyIndex>=0 && config.wepTxKeyIndex<config.wepKeys.length && config.wepKeys[config.wepTxKeyIndex]!=null) {
            return SECURITY_WEP;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA2_PSK)) {
            return SECURITY_WPA2_PSK;
        } 
        return SECURITY_NONE;
    }

    private int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WAPI-PSK")) {
            return SECURITY_WAPI_PSK;
        } else if (result.capabilities.contains("WAPI-CERT")) {
            return SECURITY_WAPI_CERT;
        } else if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        } else if (result.capabilities.contains("WPA2-PSK")) {
            return SECURITY_WPA2_PSK;
        }
        return SECURITY_NONE;
    }
    
    private void enableAllNetworks() {
        if (OPTR.equals("OP01")) {
            boolean isConnecting = mWifiStateMachine.isNetworksDisabledDuringConnect();
            SXlog.d(TAG, "enableAllNetworks, isConnecting=" + isConnecting);
            if (!isConnecting) {
                List<WifiConfiguration> networks = getConfiguredNetworks();
                WifiInfo wifiInfo = mWifiStateMachine.syncRequestConnectionInfo();
                if (null != networks) {
                    for (WifiConfiguration network : networks) {
                        if (wifiInfo != null && network.networkId != wifiInfo.getNetworkId() 
                            && network.disableReason == WifiConfiguration.DISABLED_UNKNOWN_REASON) {
                            enableNetwork(network.networkId, false);
                            mAutoConnect = true;
                        }
                    }
                    if (mAutoConnect) {
                        reconnect();
                        saveConfiguration();
                    }
                }
            }
        }
    }
    
    private boolean isPsDataAvailable() {       
        try {
            //Check radio is on
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
            if (phone == null || !phone.isRadioOn()) {
                return false;
            }
            boolean isSIM1Insert = phone.isSimInsert(Phone.GEMINI_SIM_1);
            boolean isSIM2Insert = false;
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
               isSIM2Insert = phone.isSimInsert(Phone.GEMINI_SIM_2);
            }
            if (!isSIM1Insert && !isSIM2Insert) {
               return false;
            }            
        } catch (RemoteException e) {
            SXlog.e(TAG, "Connect to phone service error!");
            return false;
        }
        int airplaneMode = Settings.System.getInt(mContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
        SXlog.d(TAG, "airplaneMode:" + airplaneMode);
        if (airplaneMode == 1) { return false; }
        return true;
    }
    
    private void showGPRSDialog() {
        mAlarmManager.cancel(mCheckConnectionIntent);
        mWaitForScanResult = false;
        boolean isPsDataAvailable = isPsDataAvailable();
        SXlog.d(TAG, "showGPRSDialog, isPsDataAvailable:" + isPsDataAvailable);
        if (isPsDataAvailable) {
            Intent failoverIntent = new Intent(TelephonyIntents.ACTION_WIFI_FAILOVER_GPRS_DIALOG);
            mContext.sendBroadcast(failoverIntent);
        }
    }
    //MTK_OP01_PROTECT_END

    public boolean doWpsPbc(String bssid) {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncDoWpsPbc(mWifiStateMachineChannel, bssid);
        } else {
            SXlog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return false;
        }
    }

    public String doWpsPin(String bssid) {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncDoWpsPin(mWifiStateMachineChannel, bssid);
        } else {
            SXlog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return null;
        }
    }

    public boolean doWpsAbort() {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncDoWpsAbort(mWifiStateMachineChannel);
        } else {
            SXlog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return false;
        }
    }

    public boolean doCTIATestOn() {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncDoCTIATestOn(mWifiStateMachineChannel);
        } else {
            SXlog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return false;
        }
    }

    public boolean doCTIATestOff() {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncDoCTIATestOff(mWifiStateMachineChannel);
        } else {
            SXlog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return false;
        }
    }

    public boolean doCTIATestRate(int rate) {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncDoCTIATestRate(mWifiStateMachineChannel, rate);
        } else {
            SXlog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return false;
        }
    }

    public boolean doCTIATestPower(int powerMode) {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncDoCTIATestPower(mWifiStateMachineChannel, powerMode);
        } else {
            SXlog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return false;
        }
    }
    
    public boolean doCTIATestSet(int id, int value) {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncDoCTIATestSet(mWifiStateMachineChannel, id, value);
        } else {
            SXlog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return false;
        }
    }
    
    public String doCTIATestGet(int id) {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncDoCTIATestGet(mWifiStateMachineChannel, id);
        } else {
            SXlog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return null;
        }
    }
    
    public boolean setTxPowerEnabled(boolean enable) {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncSetTxPowerEnabled(mWifiStateMachineChannel, enable);
        } else {
            SXlog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return false;
        }
    }
    
    public boolean setTxPower(int offset) {
        enforceChangePermission();
        if (mWifiStateMachineChannel != null) {
            return mWifiStateMachine.syncSetTxPower(mWifiStateMachineChannel, offset);
        } else {
            SXlog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return false;
        }
    }

}
