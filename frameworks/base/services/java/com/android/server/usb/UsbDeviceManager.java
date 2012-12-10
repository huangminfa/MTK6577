/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * See the License for the specific language governing permissions an
 * limitations under the License.
 */

package com.android.server.usb;

import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.provider.Settings;
import android.util.Pair;
import android.util.Slog;
//Added for USB Develpment debug, more log for more debuging help
import android.util.Log;
//Added for USB Develpment debug, more log for more debuging help

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.mediatek.xlog.SXlog;
import com.mediatek.featureoption.FeatureOption;

/**
 * UsbDeviceManager manages USB state in device mode.
 */
public class UsbDeviceManager {

//Added for USB Develpment debug, more log for more debuging help
    //private static final String TAG = UsbDeviceManager.class.getSimpleName();
    //private static final boolean DEBUG = false;
    private static final String TAG = "UsbDeviceManager";
    private static final boolean DEBUG = true;

    private static final String USB_STATE_MATCH =
            "DEVPATH=/devices/virtual/android_usb/android0";
    private static final String ACCESSORY_START_MATCH =
            "DEVPATH=/devices/virtual/misc/usb_accessory";
    private static final String FUNCTIONS_PATH =
            "/sys/class/android_usb/android0/functions";
    private static final String STATE_PATH =
            "/sys/class/android_usb/android0/state";
    private static final String MASS_STORAGE_FILE_PATH =
            "/sys/class/android_usb/android0/f_mass_storage/lun/file";
    private static final String RNDIS_ETH_ADDR_PATH =
            "/sys/class/android_usb/android0/f_rndis/ethaddr";

    private static final String IPO_POWER_ON  = "android.intent.action.ACTION_BOOT_IPO";
    private static final String IPO_POWER_OFF = "android.intent.action.ACTION_SHUTDOWN_IPO";

    private static final int MSG_UPDATE_STATE = 0;
    private static final int MSG_ENABLE_ADB = 1;
    private static final int MSG_SET_CURRENT_FUNCTION = 2;
    private static final int MSG_SYSTEM_READY = 3;
    private static final int MSG_BOOT_COMPLETED = 4;

    //VIA-START VIA USB
    private static final int MSG_SET_BYPASS_MODE = 5;
    private static final int MSG_HANDLE_CTCLINET = 6;
    private static final int MSG_VIA_CDROM_EDJECT = 7;
    private static final int MSG_SET_VIA_CDROM = 8;
    //VIA-END VIA USB

    private static final int MSG_UPDATE_DISCONNECT_STATE = 9;

    // Delay for debouncing USB disconnects.
    // We often get rapid connect/disconnect events when enabling USB functions,
    // which need debouncing.
    //Extended to 4000 for disconnect debouncing. Enable Tethering takes time and it will be no interrupt within 4000
    private static final int UPDATE_DELAY = 1000;

    //Extended to 45000 for waiting the behavior of XP MTP transfer canceling timeout.
    private static final int RNDIS_UPDATE_DELAY = 45000;

    private static final String BOOT_MODE_PROPERTY = "ro.bootmode";

    private UsbHandler mHandler;
    private boolean mBootCompleted;

    private final Context mContext;
    private final ContentResolver mContentResolver;
    private final UsbSettingsManager mSettingsManager;
    private NotificationManager mNotificationManager;
    private final boolean mHasUsbAccessory;
    private boolean mUseUsbNotification;
    private boolean mAdbEnabled;

    private Map<String, List<Pair<String, String>>> mOemModeMap;
    private boolean mAcmEnabled;
    private boolean mSettingUsbCharging;
    private boolean mUmsAlwaysEnabled;
    private boolean mHwDisconnected;
    private boolean mHwReconnected;
    private boolean mBatteryChargingUnPlug;
    private String mSettingFunction;
    private String mUsbStorageType;
    private final ReentrantLock mAdbUpdateLock = new ReentrantLock();

    //VIA-START VIA USB
    private final boolean mPCModeEnable =!SystemProperties.getBoolean("sys.usb.pcmodem.disable",false);
    private final boolean mAutoCdromEnable = SystemProperties.getBoolean("sys.usb.autocdrom.enable",false);
    //VIA-END VIA USB

    private class AdbSettingsObserver extends ContentObserver {
        public AdbSettingsObserver() {
            super(null);
        }
        @Override
        public void onChange(boolean selfChange) {
            boolean enable = (Settings.Secure.getInt(mContentResolver,
                    Settings.Secure.ADB_ENABLED, 0) > 0);
            mHandler.sendMessage(MSG_ENABLE_ADB, enable);
        }
    }

    /*
     * Listens for uevent messages from the kernel to monitor the USB state
     */
    private final UEventObserver mUEventObserver = new UEventObserver() {
        @Override
        public void onUEvent(UEventObserver.UEvent event) {
            if (DEBUG) Slog.v(TAG, "USB UEVENT: " + event.toString());

            String state = event.get("USB_STATE");
            String accessory = event.get("ACCESSORY");
//Added for USB Develpment debug, more log for more debuging help
            if(DEBUG) Log.w(TAG, "mUEventObserver: onUEvent: state = " + state);
//Added for USB Develpment debug, more log for more debuging help

            if (state != null) {
                mHandler.updateState(state);
            } else if ("START".equals(accessory)) {
                if (DEBUG) Slog.d(TAG, "got accessory start");
                setCurrentFunction(UsbManager.USB_FUNCTION_ACCESSORY, false);
            }
            //VIA-START VIA USB
            if(FeatureOption.EVDO_DT_VIA_SUPPORT == true){
                if(mPCModeEnable){
                    String eject = event.get("VIACDROM");
                    if("EJECT".equals(eject)){
                        mHandler.sendMessage(MSG_VIA_CDROM_EDJECT,true);
                    }
                 }
            }
            //VIA-END VIA USB
        }
    };

    public UsbDeviceManager(Context context, UsbSettingsManager settingsManager) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        mSettingsManager = settingsManager;
        PackageManager pm = mContext.getPackageManager();
        mHwDisconnected = true;
        mHwReconnected = false;
        mSettingUsbCharging = false;
        mBatteryChargingUnPlug = false;
        mHasUsbAccessory = pm.hasSystemFeature(PackageManager.FEATURE_USB_ACCESSORY);
        initRndisAddress();

        readOemUsbOverrideConfig();

        // create a thread for our Handler
        HandlerThread thread = new HandlerThread("UsbDeviceManager",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        if(nativeInitUMSproperty())
            SystemProperties.set("sys.usb.mtk_bicr_support","yes");

        String temp = SystemProperties.get("sys.usb.mtk_bicr_support");

        if(temp != null){
            Slog.i(TAG, "sys.usb.mtk_bicr_support=" + temp);
        } else {
            Slog.i(TAG, "failed to get sys.usb.mtk_bicr_support");
        }

        mHandler = new UsbHandler(thread.getLooper());

        if (nativeIsStartRequested()) {
            if (DEBUG) Slog.d(TAG, "accessory attached at boot");
            setCurrentFunction(UsbManager.USB_FUNCTION_ACCESSORY, false);
        }
    }

    public void systemReady() {
        if (DEBUG) Slog.d(TAG, "systemReady");

        mNotificationManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // We do not show the USB notification if the primary volume supports mass storage.
        // The legacy mass storage UI will be used instead.
        String config = SystemProperties.get("persist.sys.usb.config", UsbManager.USB_FUNCTION_MTP);
        config = removeFunction(config, UsbManager.USB_FUNCTION_ADB);
        mUsbStorageType = SystemProperties.get("ro.sys.usb.storage.type", UsbManager.USB_FUNCTION_MTP);
        SXlog.d(TAG, "systemReady - mUsbStorageType: " + mUsbStorageType + ", config: " + config);
        if (!containsFunction(mUsbStorageType, config)) {
            mUsbStorageType = config;
            SXlog.d(TAG, "systemReady - mUsbStorageType = config");
        }
        if (mUsbStorageType.equals(UsbManager.USB_FUNCTION_MASS_STORAGE)) {
            SXlog.d(TAG, "systemReady - UMS only");
            mUseUsbNotification = false;
			boolean massStorageSupported = false;
			StorageManager storageManager = (StorageManager)
					mContext.getSystemService(Context.STORAGE_SERVICE);
			StorageVolume[] volumes = storageManager.getVolumeList();

			for (int i=0; i<volumes.length; i++) {
				if (volumes[i].allowMassStorage()) {
					SXlog.d(TAG, "systemReady - massStorageSupported: " + massStorageSupported);
					massStorageSupported = true;
					break;
				}
			}

			mUseUsbNotification = !massStorageSupported;

        }
        else {
            SXlog.d(TAG, "systemReady - MTP(+UMS)");
            mUseUsbNotification = true;
        }

        // make sure the ADB_ENABLED setting value matches the current state
        if (FeatureOption.EVDO_DT_VIA_SUPPORT != true) {
            Settings.Secure.putInt(mContentResolver, Settings.Secure.ADB_ENABLED, mAdbEnabled ? 1 : 0);
        }

        mHandler.sendEmptyMessage(MSG_SYSTEM_READY);
    }
    private static void initRndisAddress() {
        // configure RNDIS ethernet address based on our serial number using the same algorithm
        // we had been previously using in kernel board files
        final int ETH_ALEN = 6;
        int address[] = new int[ETH_ALEN];
        // first byte is 0x02 to signify a locally administered address
        address[0] = 0x02;

        String serial = SystemProperties.get("ro.serialno", "1234567890ABCDEF");
        int serialLength = serial.length();
        // XOR the USB serial across the remaining 5 bytes
        for (int i = 0; i < serialLength; i++) {
            address[i % (ETH_ALEN - 1) + 1] ^= (int)serial.charAt(i);
        }
        String addrString = String.format("%02X:%02X:%02X:%02X:%02X:%02X",
            address[0], address[1], address[2], address[3], address[4], address[5]);
        try {
            FileUtils.stringToFile(RNDIS_ETH_ADDR_PATH, addrString);
        } catch (IOException e) {
           Slog.e(TAG, "failed to write to " + RNDIS_ETH_ADDR_PATH);
        }
    }

     private static String addFunction(String functions, String function) {
        if (!containsFunction(functions, function)) {

//Added for USB Develpment debug, more log for more debuging help
            if(DEBUG) {
                Log.w(TAG, "addFunction, functions: " + functions);
                Log.w(TAG, "addFunction, function: " + function);
            }
//Added for USB Develpment debug, more log for more debuging help

            if ((function.equals(UsbManager.USB_FUNCTION_ADB) || function.equals(UsbManager.USB_FUNCTION_RNDIS))&& containsFunction(functions,UsbManager.USB_FUNCTION_ACM)) {
    	        functions = removeFunction(functions,UsbManager.USB_FUNCTION_ACM);
            }

            if (functions.length() > 0) {
                functions += ",";
            }
            functions += function;

            if ((function.equals(UsbManager.USB_FUNCTION_ADB) || function.equals(UsbManager.USB_FUNCTION_RNDIS)) && containsFunction(functions,UsbManager.USB_FUNCTION_ACM)) {
    	        functions = addFunction(functions,UsbManager.USB_FUNCTION_ACM);
            }
        }
        return functions;
    }

    private static String removeFunction(String functions, String function) {
        String[] split = functions.split(",");
        for (int i = 0; i < split.length; i++) {
            if (function.equals(split[i])) {
                split[i] = null;
            }
        }
        StringBuilder builder = new StringBuilder();
         for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (s != null) {
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(s);
            }
        }
        return builder.toString();
    }

    private static boolean containsFunction(String functions, String function) {
        int index = functions.indexOf(function);

//Added for USB Develpment debug, more log for more debuging help
        if(DEBUG) {
            Log.w(TAG, "containsFunction, functions: " + functions);
            Log.w(TAG, "containsFunction, function: " + function);
            Log.w(TAG, "containsFunction index: " + index);
        }
//Added for USB Develpment debug, more log for more debuging help

        if (index < 0) return false;
        if (index > 0 && functions.charAt(index - 1) != ',') return false;
        int charAfter = index + function.length();
        if (charAfter < functions.length() && functions.charAt(charAfter) != ',') return false;
        return true;
    }

    private final class UsbHandler extends Handler {

        // current USB state
        private boolean mConnected;
        private boolean mConfigured;
        //VIA-START VIA USB
        private boolean mViaCdromEjected;
        //VIA-END VIA USB
        private String mCurrentFunctions;
        private String mDefaultFunctions;
        private UsbAccessory mCurrentAccessory;
        private int mUsbNotificationId;
        private boolean mAdbNotificationShown;
        private Bypass mBypass;

        private Runnable mShowBuiltinInstallerRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                Slog.i(TAG, "Delay show");
                showBuiltinInstallerUI(mConnected);
            }
        };

        private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                if (DEBUG) Slog.d(TAG, "boot completed");
                mHandler.sendEmptyMessage(MSG_BOOT_COMPLETED);
            }

                if(action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                    Slog.i(TAG, "BOOT_COMPLETED");
                    Handler showBuiltinInstallerHandler = new Handler();
                    showBuiltinInstallerHandler.postDelayed(mShowBuiltinInstallerRunnable, 14000);
                } else if(action.equals(IPO_POWER_ON)) {
                    SXlog.d(TAG, "onReceive - [IPO_POWER_ON] mDefaultFunctions: " + mDefaultFunctions + ", mSettingUsbCharging: " + mSettingUsbCharging);
                    if (mSettingUsbCharging) {
                        mSettingUsbCharging = false;
                        setCurrentFunction(mDefaultFunctions, false);
                    }
                    showBuiltinInstallerUI(mConnected);
                } else if(action.equals(IPO_POWER_OFF)) {
                    Slog.i(TAG, "IPO_POWER_OFF");
                    if("yes_hide".equals(SystemProperties.get("sys.usb.mtk_bicr_support")))
                        SystemProperties.set("sys.usb.mtk_bicr_support","yes");
                }

                if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                    int plugType = intent.getIntExtra("plugged", 0);
                    SXlog.d(TAG, "onReceive - ACTION_BATTERY_CHANGED - plugType: " + plugType + ", mSettingUsbCharging: " + mSettingUsbCharging + ", mConnected: " + mConnected);
                    if (plugType == 0) {
                        if (mSettingUsbCharging) {
                            SXlog.d(TAG, "onReceive - ACTION_BATTERY_CHANGED - [USB unplugged in USB Charging Mode]");
                            mSettingUsbCharging = false;
                            setCurrentFunction(mDefaultFunctions, false);
                        } else {
                            if (mConnected) {
                                mBatteryChargingUnPlug = true;
                                removeMessages(MSG_UPDATE_STATE);
                                removeMessages(MSG_UPDATE_DISCONNECT_STATE);
                                Message msg = Message.obtain();
                                msg.what = MSG_UPDATE_DISCONNECT_STATE;
                                msg.arg1 = 0;
                                msg.arg2 = 0;
                                SXlog.d(TAG, "onReceive - ACTION_BATTERY_CHANGED - [HW USB Disconnected] mHwDisconnected: " + mHwDisconnected + ", mConnected: " + mConnected + ", mConfigured: " + mConfigured);
                                sendMessageDelayed(msg, 1000);
                            }
                        }
                    } else {
                        if (mBatteryChargingUnPlug) {
                            SXlog.d(TAG, "onReceive - ACTION_BATTERY_CHANGED - [IGNORE] mHwDisconnected: " + mHwDisconnected + ", mConnected: " + mConnected + ", mConfigured: " + mConfigured + ", mBatteryChargingUnPlug: " + mBatteryChargingUnPlug);
                            removeMessages(MSG_UPDATE_DISCONNECT_STATE);
                            mBatteryChargingUnPlug = false;
                        }
                    }
                }
            }
        };
        //VIA-START VIA USB
        private final class Bypass{
            private static final String ACTION_USB_BYPASS_SETFUNCTION =
                    "com.via.bypass.action.setfunction";
            private static final String VALUE_ENABLE_BYPASS =
                    "com.via.bypass.enable_bypass";
            private static final String ACTION_USB_BYPASS_SETBYPASS =
                    "com.via.bypass.action.setbypass";
            private static final String ACTION_USB_BYPASS_SETBYPASS_RESULT =
                    "com.via.bypass.action.setbypass_result";
            private static final String VALUE_ISSET_BYPASS =
                    "com.via.bypass.isset_bypass";
            private static final String ACTION_USB_BYPASS_GETBYPASS =
                    "com.via.bypass.action.getbypass";
            private static final String ACTION_USB_BYPASS_GETBYPASS_RESULT =
                    "com.via.bypass.action.getbypass_result";
            private static final String VALUE_BYPASS_CODE =
                    "com.via.bypass.bypass_code";

            private static final String USB_FUNCTION_BYPASS = "via_bypass";
            public static final String USB_FUNCTION_USERMODE = UsbManager.USB_FUNCTION_MTP;

            /*Bypass function values*/
            private File[] mBypassFiles;
            private final int[] mBypassCodes = new int[]{1,2,4,8,16};
            private final String[] mBypassName = new String[]{"gps","pcv","atc","ets","data"};
            private int mBypassAll = 0;

            private final BroadcastReceiver mBypassReceiver = new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    if (DEBUG) Slog.i(TAG,"onReceive="+intent.getAction());
                    if(intent.getAction().equals(ACTION_USB_BYPASS_SETFUNCTION)){
                        Boolean enablebypass = intent.getBooleanExtra(VALUE_ENABLE_BYPASS, false);
                        if(enablebypass){
                            setCurrentFunction(USB_FUNCTION_BYPASS,false);
                        }else{
                            if(mCurrentFunctions.equals(USB_FUNCTION_BYPASS)){
                                closeBypassFunction();
                            }
                        }
                    }else if(intent.getAction().equals(ACTION_USB_BYPASS_SETBYPASS)){
                        int bypasscode = intent.getIntExtra(VALUE_BYPASS_CODE,-1);
                        if(bypasscode>=0 && bypasscode<=mBypassAll){
                            setBypassMode(bypasscode);
                        }else{
                            notifySetBypassResult(false,getCurrentBypassMode());
                        }
                    }else if(intent.getAction().equals(ACTION_USB_BYPASS_GETBYPASS)){
                        Intent reintent = new Intent(ACTION_USB_BYPASS_GETBYPASS_RESULT);
                        reintent.putExtra(VALUE_BYPASS_CODE, getCurrentBypassMode());
                        mContext.sendBroadcast(reintent);
                    }
                }
            };

            public Bypass() {

                mBypassFiles = new File[mBypassName.length];
                for(int i=0;i<mBypassName.length;i++){
                    final String path = "/sys/class/usb_rawbulk/"+mBypassName[i]+"/enable";
                    //if (DEBUG) Slog.d(TAG, "bypass mode file path="+path);
                    mBypassFiles[i] = new File(path);
                    mBypassAll += mBypassCodes[i];
                }
                if(FeatureOption.EVDO_DT_VIA_SUPPORT == true) {
                    //register bypass receiver
                    IntentFilter intent = new IntentFilter(ACTION_USB_BYPASS_SETFUNCTION);
                    intent.addAction(ACTION_USB_BYPASS_SETBYPASS);
                    intent.addAction(ACTION_USB_BYPASS_GETBYPASS);
                    mContext.registerReceiver(mBypassReceiver,intent);
                }
            }
            private int getCurrentBypassMode(){
                int bypassmode = 0;
                try {
                    for(int i=0;i<mBypassCodes.length;i++){
                        String code;
                        if(i==2){
                           code = SystemProperties.get("sys.cp.bypass.at","0");
                        }else{
                           code = FileUtils.readTextFile(mBypassFiles[i],0,null);
                        }
                        //if (DEBUG) Slog.d(TAG, "'"+mBypassFiles[i].getAbsolutePath()+"' value is "+code);
                        if(code!=null && code.trim().equals("1")){
                            bypassmode |= mBypassCodes[i];
                        }
                    }
                    if (DEBUG) Slog.d(TAG, "getCurrentBypassMode()="+bypassmode);
                } catch (IOException e) {
                   Slog.e(TAG, "failed to read bypass mode code!");
                }
                return bypassmode;
            }

            void updateBypassMode(int bypassmode){
                int bypassResult = getCurrentBypassMode();
                if(bypassmode == bypassResult){
                    notifySetBypassResult(true,bypassResult);
                    return;
                }
                //set bypass code
                try {
                    for(int i=0;i<mBypassCodes.length;i++){
                        if((bypassmode & mBypassCodes[i]) != 0){
                            if(i==2){
                                SystemProperties.set("sys.cp.bypass.at", "1");
                            }else{
                                FileUtils.stringToFile(mBypassFiles[i].getAbsolutePath(), "1");
                            }
                            bypassResult |= mBypassCodes[i];
                        }else{
                            if(i==2){
                                SystemProperties.set("sys.cp.bypass.at", "0");
                            }else{
                                FileUtils.stringToFile(mBypassFiles[i].getAbsolutePath(), "0");
                            }
                            if((bypassResult & mBypassCodes[i]) != 0)
                                bypassResult ^= mBypassCodes[i];
                        }
                        if (DEBUG)Slog.d(TAG, "Write '"+mBypassFiles[i].getAbsolutePath()+"' successsfully!");
                    }
                    notifySetBypassResult(true,bypassResult);
                } catch (IOException e) {
                   Slog.e(TAG, "failed to operate bypass!");
                   notifySetBypassResult(false,bypassResult);
                }
            }
            /*Set bypass mode*/
            private void setBypassMode(int bypassmode){
                if (DEBUG) Slog.d(TAG, "setBypassMode()="+bypassmode);
                Message m = Message.obtain(mHandler, MSG_SET_BYPASS_MODE);
                m.arg1 = bypassmode;
                sendMessage(m);
            }
            private void notifySetBypassResult(Boolean isset,int bypassCode){
                if (mBootCompleted) {
                    Intent intent = new Intent(ACTION_USB_BYPASS_SETBYPASS_RESULT);
                    intent.putExtra(VALUE_ISSET_BYPASS, isset);
                    intent.putExtra(VALUE_BYPASS_CODE, bypassCode);
                    mContext.sendBroadcast(intent);
                }
            }
            void handleCTClinet(){
                String ctclientStatus = SystemProperties.get("net.ctclientd","disable");
                if (DEBUG)Slog.d(TAG,"handleCTClinet() mConnected="+mConnected
                    +",mCurrentFunctions="+mCurrentFunctions
                    +",ctclientStatus="+ctclientStatus);
                //when usb connnected and not bypass+at
                if(mConnected){
                    if(ctclientStatus.equals("disable")){
                        if (DEBUG)Slog.d(TAG,"enable ctclientd");
                        SystemProperties.set("net.ctclientd","enable");
                    }
                } else {
                    if(ctclientStatus.equals("enable")){
                        SystemProperties.set("net.ctclientd","disable");
                        if (DEBUG)Slog.d(TAG,"disable ctclientd");
                    }
                }
            }

            void closeBypassFunction(){
                if (DEBUG) Slog.d(TAG, "closeBypassFunction() CurrentFunctions = " +
                                   mCurrentFunctions+",DefaultFunctions="+mDefaultFunctions);
                updateBypassMode(0);
                if(mCurrentFunctions.contains(USB_FUNCTION_BYPASS)){
                    setEnabledFunctions(mDefaultFunctions, false);
                }
            }
        }
        //VIA-END VIA USB
        public UsbHandler(Looper looper) {
            super(looper);
            try {
                //VIA-START VIA USB
                if (FeatureOption.EVDO_DT_VIA_SUPPORT == true) {
                    mBypass = new Bypass();
                }
                //VIA-END VIA USB

                // persist.sys.usb.config should never be unset.  But if it is, set it to "adb"
                // so we have a chance of debugging what happened.
                mDefaultFunctions = SystemProperties.get("persist.sys.usb.config", "adb");

                // Check if USB mode needs to be overridden depending on OEM specific bootmode.
                mDefaultFunctions = processOemUsbOverride(mDefaultFunctions);

                // sanity check the sys.usb.config system property
                // this may be necessary if we crashed while switching USB configurations
                String config = SystemProperties.get("sys.usb.config", "none");
                if (!config.equals(mDefaultFunctions)) {
                    Slog.w(TAG, "resetting config to persistent property: " + mDefaultFunctions);
                    SystemProperties.set("sys.usb.config", mDefaultFunctions);
                }

                mCurrentFunctions = mDefaultFunctions;
                mSettingFunction = mCurrentFunctions;
                String state = FileUtils.readTextFile(new File(STATE_PATH), 0, null).trim();
                updateState(state);
                mAdbEnabled = containsFunction(mCurrentFunctions, UsbManager.USB_FUNCTION_ADB);
                mAcmEnabled = containsFunction(mCurrentFunctions, UsbManager.USB_FUNCTION_ACM);

                // Upgrade step for previous versions that used persist.service.adb.enable
                String value = SystemProperties.get("persist.service.adb.enable", "");
                if (value.length() > 0) {
                    char enable = value.charAt(0);
                    if (enable == '1') {
                        setAdbEnabled(true);
                    } else if (enable == '0') {
                        setAdbEnabled(false);
                    }
                    SystemProperties.set("persist.service.adb.enable", "");
                }

                if (FeatureOption.EVDO_DT_VIA_SUPPORT == true) {
                    // make sure the ADB_ENABLED setting value matches the current state(move from systemready())
                    Settings.Secure.putInt(mContentResolver, Settings.Secure.ADB_ENABLED, mAdbEnabled ? 1 : 0);
                }

                value = SystemProperties.get("persist.service.acm.enable", "");
                if (value.length() > 0) {
                    char enable = value.charAt(0);
                    if (enable == '1') {
                        setAcmEnabled(true);
                    } else if (enable == '0') {
                        setAcmEnabled(false);
                    }
                    SystemProperties.set("persist.service.acm.enable", "");
                }

                if ("yes".equals(SystemProperties.get("sys.usb.mtk_bicr_support"))) {
                    mUmsAlwaysEnabled = true;
                } else {
                    mUmsAlwaysEnabled = false;
                }

                // register observer to listen for settings changes
                mContentResolver.registerContentObserver(
                        Settings.Secure.getUriFor(Settings.Secure.ADB_ENABLED),
                                false, new AdbSettingsObserver());

                // Watch for USB configuration changes
                mUEventObserver.startObserving(USB_STATE_MATCH);
                mUEventObserver.startObserving(ACCESSORY_START_MATCH);

                IntentFilter filter = new IntentFilter();

                if (FeatureOption.MTK_IPO_SUPPORT == true) {
                    filter.addAction(IPO_POWER_ON);
                    filter.addAction(IPO_POWER_OFF);
                }

                filter.addAction(Intent.ACTION_BOOT_COMPLETED);
                filter.addAction(Intent.ACTION_BATTERY_CHANGED);

                mContext.registerReceiver(mIntentReceiver, filter);

                //VIA-START VIA USB
                if(FeatureOption.EVDO_DT_VIA_SUPPORT == true
                               && mPCModeEnable && mAutoCdromEnable){
                    sendEmptyMessage(MSG_SET_VIA_CDROM);
                }
                //VIA-END VIA USB
            } catch (Exception e) {
                Slog.e(TAG, "Error initializing UsbHandler", e);
            }
        }

        public void sendMessage(int what, boolean arg) {
            removeMessages(what);
            Message m = Message.obtain(this, what);
            m.arg1 = (arg ? 1 : 0);
            sendMessage(m);
        }

        public void sendMessage(int what, Object arg) {
            removeMessages(what);
            Message m = Message.obtain(this, what);
            m.obj = arg;
            sendMessage(m);
        }

        public void sendMessage(int what, Object arg0, boolean arg1) {
            removeMessages(what);
            Message m = Message.obtain(this, what);
            m.obj = arg0;
            m.arg1 = (arg1 ? 1 : 0);
            sendMessage(m);
        }

        public void updateState(String state) {
            int connected, configured;

//Added for USB Develpment debug, more log for more debuging help
            if(DEBUG) Log.w(TAG, "updateState: " + state);
//Added for USB Develpment debug, more log for more debuging help
            Message msg;

            if ("HWDISCONNECTED".equals(state)) {
                connected = 0;
                configured = 0;
                mHwDisconnected = true;
            	msg = Message.obtain(this, MSG_UPDATE_DISCONNECT_STATE);
            } else if ("DISCONNECTED".equals(state)) {
                connected = 0;
                configured = 0;
                if ( mHwDisconnected == true)
                    mHwReconnected = true;
                mHwDisconnected = false;
            	msg = Message.obtain(this, MSG_UPDATE_DISCONNECT_STATE);
            } else if ("CONNECTED".equals(state)) {
                connected = 1;
                configured = 0;
                if ( mHwDisconnected == true)
                    mHwReconnected = true;
                mHwDisconnected = false;
            	msg = Message.obtain(this, MSG_UPDATE_STATE);
            } else if ("CONFIGURED".equals(state)) {
                connected = 1;
                configured = 1;
                if ( mHwDisconnected == true)
                    mHwReconnected = true;
                mHwDisconnected = false;
            	msg = Message.obtain(this, MSG_UPDATE_STATE);
            } else {
                Slog.e(TAG, "unknown state " + state);
                return;
            }
            removeMessages(MSG_UPDATE_STATE);
			removeMessages(MSG_UPDATE_DISCONNECT_STATE);

            msg.arg1 = connected;
            msg.arg2 = configured;
            // debounce disconnects to avoid problems bringing up USB tethering
            if (mHwDisconnected || mSettingUsbCharging) {
                SXlog.d(TAG, "updateState - UPDATE_DELAY  " + state + " mSettingFunction: " + mSettingFunction);
                sendMessageDelayed(msg, (connected == 0) ? UPDATE_DELAY : 0);
            }
            else {
                SXlog.d(TAG, "updateState - RNDIS_UPDATE_DELAY  " + state + " mSettingFunction: " + mSettingFunction);
                sendMessageDelayed(msg, (connected == 0) ? RNDIS_UPDATE_DELAY : 0);
            }
        }

        private boolean waitForState(String state) {
            // wait for the transition to complete.
            // give up after 1 second.
            for (int i = 0; i < 20; i++) {
                // State transition is done when sys.usb.state is set to the new configuration
                if (state.equals(SystemProperties.get("sys.usb.state"))) return true;
                try {
                    // try again in 50ms
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
            Slog.e(TAG, "waitForState(" + state + ") FAILED");
            return false;
        }

        private boolean setUsbConfig(String config) {
            if (DEBUG) Slog.d(TAG, "setUsbConfig(" + config + ")");
            // set the new configuration
//Added for USB Develpment debug, more log for more debuging help
            if(DEBUG) Log.w(TAG, "setUsbConfig, config: " + config);
//Added for USB Develpment debug, more log for more debuging help
            SystemProperties.set("sys.usb.config", config);
            return waitForState(config);
        }

        private void setAdbEnabled(boolean enable) {
            if (DEBUG) Slog.d(TAG, "setAdbEnabled: " + enable);
            if (enable != mAdbEnabled) {
                mAdbEnabled = enable;
                // Due to the persist.sys.usb.config property trigger, changing adb state requires
                // switching to default function
                if (!mCurrentFunctions.equals(UsbManager.USB_FUNCTION_CHARGING_ONLY)) {
                setEnabledFunctions(mDefaultFunctions, true);
                updateAdbNotification();
            }
        }
        }

        private void setAcmEnabled(boolean enable) {
            if (DEBUG) Slog.d(TAG, "setAcmEnabled: " + enable);
            if (enable != mAcmEnabled) {
                mAcmEnabled = enable;
                // Due to the persist.sys.usb.config property trigger, changing adb state requires
                // switching to default function
                setEnabledFunctions(mDefaultFunctions, true);
            }
        }

        private void setEnabledFunctions(String functions, boolean makeDefault) {
//Added for USB Develpment debug, more log for more debuging help
            if (DEBUG) {
                Log.w(TAG, "setEnabledFunctions: functions = " + functions);
                Log.w(TAG, "setEnabledFunctions, mDefaultFunctions: " + mDefaultFunctions);
                Log.w(TAG, "setEnabledFunctions, mCurrentFunctions: " + mCurrentFunctions);
                SXlog.d(TAG, "setEnabledFunctions, mSettingFunction: " + mSettingFunction);
            }
//Added for USB Develpment debug, more log for more debuging help
            if (mCurrentFunctions.equals(UsbManager.USB_FUNCTION_CHARGING_ONLY)) {
                SXlog.d(TAG, "setEnabledFunctions - [Disable USB Charging]");
                SystemProperties.set("sys.usb.charging","no");
            }

            // Do not update persystent.sys.usb.config if the device is booted up
            // with OEM specific mode.
            if (functions != null && makeDefault && !needsOemUsbOverride()) {
                if(FeatureOption.EVDO_DT_VIA_SUPPORT == true) {
                    //VIA-START VIA USB
                    //adb can't be open with bypass and via cdrom
                    if(!functions.contains(Bypass.USB_FUNCTION_BYPASS)){
                        if(Settings.Secure.getInt(mContentResolver,
                            Settings.Secure.ADB_ENABLED, 0) > 0){
                            mAdbEnabled = true;
                            updateAdbNotification();
                        }
                    }else if(Settings.Secure.getInt(mContentResolver,
                        Settings.Secure.ADB_ENABLED, 0) > 0){
                        mAdbEnabled = false;
                        updateAdbNotification();
                    }
                    //VIA-END VIA USB
                }

                mSettingFunction = functions;
                if (mUmsAlwaysEnabled && containsFunction(mSettingFunction, UsbManager.USB_FUNCTION_MTP)) {
                    functions = addFunction(functions, UsbManager.USB_FUNCTION_MASS_STORAGE);
                }
                if (mAdbEnabled && !mSettingUsbCharging) {
                    functions = addFunction(functions, UsbManager.USB_FUNCTION_ADB);
                } else {
                    functions = removeFunction(functions, UsbManager.USB_FUNCTION_ADB);
                }
                if (mAcmEnabled && !mSettingUsbCharging) {
                    functions = addFunction(functions, UsbManager.USB_FUNCTION_ACM);
                } else {
                    functions = removeFunction(functions, UsbManager.USB_FUNCTION_ACM);
                }
                if (!mDefaultFunctions.equals(functions) || mCurrentFunctions.equals(UsbManager.USB_FUNCTION_CHARGING_ONLY)) {
                    if (!setUsbConfig("none")) {
                        Slog.e(TAG, "Failed to disable USB");
                        // revert to previous configuration if we fail
                        setUsbConfig(mCurrentFunctions);
                        return;
                    }
                    // setting this property will also change the current USB state
                    // via a property trigger
                    SystemProperties.set("persist.sys.usb.config", functions);
                    if (waitForState(functions)) {
                        mCurrentFunctions = functions;
                        mDefaultFunctions = functions;
                    } else {
                        Slog.e(TAG, "Failed to switch persistent USB config to " + functions);
                        // revert to previous configuration if we fail
                        SystemProperties.set("persist.sys.usb.config", mDefaultFunctions);
                    }
                }else{
                    //VIA-START VIA USB
                    if(FeatureOption.EVDO_DT_VIA_SUPPORT == true) {
                        if(mDefaultFunctions.equals(functions) &&
                        (mCurrentFunctions.contains(UsbManager.USB_FUNCTION_VIA_CDROM)
                        ||mCurrentFunctions.contains(Bypass.USB_FUNCTION_BYPASS))){
                            Slog.i(TAG, "reset DefaultFunctions!");
                            if (!setUsbConfig("none")) {
                                Slog.e(TAG, "Failed to disable USB");
                                // revert to previous configuration if we fail
                                setUsbConfig(mCurrentFunctions);
                                return;
                            }
                            if (setUsbConfig(functions)) {
                                mCurrentFunctions = functions;
                            } else {
                                Slog.e(TAG, "Failed to switch USB config to " + functions);
                                // revert to previous configuration if we fail
                                setUsbConfig(mCurrentFunctions);
                            }
                        }
                    }
                    //VIA-END VIA USB
               }
            } else {
                if (functions == null) {
                    functions = mDefaultFunctions;
                }
                //VIA-START VIA USB
                if(FeatureOption.EVDO_DT_VIA_SUPPORT == true) {
                    //adb can't be open with bypass and via cdrom
                    if(!functions.contains(Bypass.USB_FUNCTION_BYPASS)){
                        if(Settings.Secure.getInt(mContentResolver,
                            Settings.Secure.ADB_ENABLED, 0) > 0){
                             mAdbEnabled = true;
                             updateAdbNotification();
                        }
                    }else if(Settings.Secure.getInt(mContentResolver,
                        Settings.Secure.ADB_ENABLED, 0) > 0){
                        mAdbEnabled = false;
                        updateAdbNotification();
                    }
                }
                //VIA-END VIA USB
                // Override with bootmode specific usb mode if needed
                functions = processOemUsbOverride(functions);
                    mSettingFunction = functions;

                if (mUmsAlwaysEnabled && containsFunction(mSettingFunction, UsbManager.USB_FUNCTION_MTP)) {
                    functions = addFunction(functions, UsbManager.USB_FUNCTION_MASS_STORAGE);
                }
                if (mAdbEnabled && !mSettingUsbCharging) {
                    functions = addFunction(functions, UsbManager.USB_FUNCTION_ADB);
                } else {
                    functions = removeFunction(functions, UsbManager.USB_FUNCTION_ADB);
                }
                if (mAcmEnabled && !mSettingUsbCharging && !functions.contains(UsbManager.USB_FUNCTION_VIA_CDROM)) {
                    functions = addFunction(functions, UsbManager.USB_FUNCTION_ACM);
                } else {
                    functions = removeFunction(functions, UsbManager.USB_FUNCTION_ACM);
                }
                if (!mCurrentFunctions.equals(functions)) {
                    if (!setUsbConfig("none")) {
                        Slog.e(TAG, "Failed to disable USB");
                        // revert to previous configuration if we fail
                        setUsbConfig(mCurrentFunctions);
                        return;
                    }
                    if (setUsbConfig(functions)) {
                        mCurrentFunctions = functions;
                    } else {
                        Slog.e(TAG, "Failed to switch USB config to " + functions);
                        // revert to previous configuration if we fail
                        setUsbConfig(mCurrentFunctions);
                    }
                }
            }
        }

        private void updateCurrentAccessory() {
            if (!mHasUsbAccessory) return;

            if (mConfigured) {
                String[] strings = nativeGetAccessoryStrings();
                if (strings != null) {
                    mCurrentAccessory = new UsbAccessory(strings);
                    Slog.d(TAG, "entering USB accessory mode: " + mCurrentAccessory);
                    // defer accessoryAttached if system is not ready
                    if (mBootCompleted) {
                        mSettingsManager.accessoryAttached(mCurrentAccessory);
                    } // else handle in mBootCompletedReceiver
                } else {
                    Slog.e(TAG, "nativeGetAccessoryStrings failed");
                }
            } else if (!mConnected) {
                // make sure accessory mode is off
                // and restore default functions
                Slog.d(TAG, "exited USB accessory mode");
                setEnabledFunctions(mDefaultFunctions, false);

                if (mCurrentAccessory != null) {
                    if (mBootCompleted) {
                        mSettingsManager.accessoryDetached(mCurrentAccessory);
                    }
                    mCurrentAccessory = null;
                }
            }
        }

        private void updateUsbState() {
            // send a sticky broadcast containing current USB state
            Intent intent = new Intent(UsbManager.ACTION_USB_STATE);
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            intent.putExtra(UsbManager.USB_CONNECTED, mConnected);
            intent.putExtra(UsbManager.USB_CONFIGURED, mConfigured);
            intent.putExtra("USB_HW_DISCONNECTED", mHwDisconnected);
            if (FeatureOption.EVDO_DT_VIA_SUPPORT == true) {
                intent.putExtra("USB_VIA_CDROM_EJECTED", mViaCdromEjected);
            }

            if (mCurrentFunctions != null) {
                String[] functions = mCurrentFunctions.split(",");
                for (int i = 0; i < functions.length; i++) {
                    intent.putExtra(functions[i], true);
                }
            }

            mContext.sendStickyBroadcast(intent);
        }

        @Override
        public void handleMessage(Message msg) {

//Added for USB Develpment debug, more log for more debuging help
            if(DEBUG) Log.w(TAG, "handleMessage: " + msg.what);
//Added for USB Develpment debug, more log for more debuging help

            switch (msg.what) {
                case MSG_UPDATE_STATE:
                case MSG_UPDATE_DISCONNECT_STATE:
                    mConnected = (msg.arg1 == 1);
                    mConfigured = (msg.arg2 == 1);
                    updateUsbNotification();
                    updateAdbNotification();
                    if (containsFunction(mCurrentFunctions,
                            UsbManager.USB_FUNCTION_ACCESSORY)) {
                        updateCurrentAccessory();
                    }
                    if(FeatureOption.EVDO_DT_VIA_SUPPORT == true){
                    //VIA-START VIA USB
                        removeMessages(MSG_SET_VIA_CDROM);
                    //VIA-END VIA USB
                    }
                    if (!mConnected && !mSettingUsbCharging) {
                        // restore defaults when USB is disconnected
                        SXlog.d(TAG, "handleMessage - MSG_UPDATE_STATE - mConnected: " + mConnected + ", mSettingUsbCharging: " + mSettingUsbCharging);
                        //VIA-START VIA USB
                        if(FeatureOption.EVDO_DT_VIA_SUPPORT == true){
                            //set bypass mode to 0
                            mBypass.updateBypassMode(0);
                            if(mPCModeEnable && mAutoCdromEnable){
                                sendEmptyMessage(MSG_SET_VIA_CDROM);
                            } else {
                                setEnabledFunctions(mDefaultFunctions, false);
                            }
                        //VIA-END VIA USB
                        } else {
                            setEnabledFunctions(mDefaultFunctions, false);
                        }
                    }
                    if (mBootCompleted) {
                        updateUsbState();

                        Log.w(TAG, "handleMessage mConnected:" + mConnected + ",mConfigured:" + mConfigured +
							", mHwDisconnected:" + mHwDisconnected + ", mHwReconnected:" + mHwReconnected);

                        if(mHwReconnected == true && mConnected == true) {
                            showBuiltinInstallerUI(true);
                            mHwReconnected = false;
                        }
                    }
                    //VIA-START VIA USB
                    if(FeatureOption.EVDO_DT_VIA_SUPPORT == true) {
                        sendEmptyMessage(MSG_HANDLE_CTCLINET);
                    }
                    //VIA-END VIA USB
                    break;
                case MSG_ENABLE_ADB:
                    if(FeatureOption.EVDO_DT_VIA_SUPPORT == true) {
                        //VIA-START VIA USB
                        if(!mCurrentFunctions.contains(Bypass.USB_FUNCTION_BYPASS))
                            setAdbEnabled(msg.arg1 == 1);
                        //VIA-END VIA USB
                    }else{
                        setAdbEnabled(msg.arg1 == 1);
                    }
                    break;
                case MSG_SET_CURRENT_FUNCTION:
                    String function = (String)msg.obj;
                    boolean makeDefault = (msg.arg1 == 1);
                    if (function != null && function.equals(UsbManager.USB_FUNCTION_CHARGING_ONLY)) {
                        mSettingUsbCharging = true;
                        SXlog.d(TAG, "handleMessage - MSG_SET_CURRENT_FUNCTION - USB_FUNCTION_CHARGING_ONLY - makeDefault: " + makeDefault);
                    } else {
                        mSettingUsbCharging = false;
                    }
                    setEnabledFunctions(function, makeDefault);
                    SXlog.d(TAG, "handleMessage - MSG_SET_CURRENT_FUNCTION - function: " + function);
                    break;
                case MSG_SYSTEM_READY:
                    updateUsbNotification();
                    updateAdbNotification();
                    updateUsbState();
                    break;
                case MSG_BOOT_COMPLETED:
                    mBootCompleted = true;
                    //ALPS00112030 modification
                    //Modified by Ainge for the "notification updated" should be after boot complete.
                    //update while System ready is too early that the "SystemUIService(  314): loading: class com.android.systemui.statusbar.phone.PhoneStatusBar" is not ready
                    updateUsbNotification();
                    updateAdbNotification();
                    //ALPS00112030 modification
                    if (mCurrentAccessory != null) {
                        mSettingsManager.accessoryAttached(mCurrentAccessory);
                    }
                    //ALPS00112030 modification
                    if(mBootCompleted)
                        updateUsbState();
                    //ALPS00112030 modification
                    break;
                //VIA-START VIA USB
                case MSG_SET_BYPASS_MODE:
                    if(FeatureOption.EVDO_DT_VIA_SUPPORT == true) {
                        mBypass.updateBypassMode(msg.arg1);
                    }
                    break;
                 case MSG_HANDLE_CTCLINET:
                    if(FeatureOption.EVDO_DT_VIA_SUPPORT == true) {
                        mBypass.handleCTClinet();
                    }
                    break;
                 case MSG_VIA_CDROM_EDJECT:
                    if(FeatureOption.EVDO_DT_VIA_SUPPORT == true) {
                        mViaCdromEjected = true;
                        setEnabledFunctions(mDefaultFunctions, msg.arg1 == 1);
                        updateUsbState();
                    }
                    break;
                case MSG_SET_VIA_CDROM:
                	  if(FeatureOption.EVDO_DT_VIA_SUPPORT == true) {
                       if(mPCModeEnable && !mCurrentFunctions.contains(UsbManager.USB_FUNCTION_VIA_CDROM)){
                            mViaCdromEjected = false;
            								setEnabledFunctions(UsbManager.USB_FUNCTION_VIA_CDROM,false);
            					 }
                    }
                    break;
                //VIA-END VIA USB
            }
        }

        public UsbAccessory getCurrentAccessory() {
            return mCurrentAccessory;
        }
        public void setViaCdromFunction(){

        }
        private void updateUsbNotification() {
//Added for USB Develpment debug, more log for more debuging help
            if(DEBUG) {
                Log.w(TAG, "updateUsbNotification, mNotificationManager: " + mNotificationManager);
                Log.w(TAG, "updateUsbNotification, mUseUsbNotification: " + mUseUsbNotification);
            }
//Added for USB Develpment debug, more log for more debuging help
            if (mNotificationManager == null || !mUseUsbNotification) return;
            int id = 0;
            Resources r = mContext.getResources();

//Added for USB Develpment debug, more log for more debuging help
            if(DEBUG) {
                Log.w(TAG, "updateUsbNotification, mConnected: " + mConnected);
                Log.w(TAG, "updateUsbNotification, mCurrentFunctions: " + mCurrentFunctions);
            }
//Added for USB Develpment debug, more log for more debuging help

            if (mConnected || mSettingUsbCharging) {
                if (containsFunction(mCurrentFunctions, UsbManager.USB_FUNCTION_MTP)) {
                    id = com.android.internal.R.string.usb_mtp_notification_title;
                    SXlog.d(TAG, "updateUsbNotification - containsFunction:  USB_FUNCTION_MTP");
                } else if (containsFunction(mCurrentFunctions, UsbManager.USB_FUNCTION_PTP)) {
                    id = com.android.internal.R.string.usb_ptp_notification_title;
                    SXlog.d(TAG, "updateUsbNotification - containsFunction:  USB_FUNCTION_PTP");
                } else if (containsFunction(mCurrentFunctions, UsbManager.USB_FUNCTION_MASS_STORAGE)) {
                    SXlog.d(TAG, "updateUsbNotification - containsFunction:  USB_FUNCTION_MASS_STORAGE - mUsbStorageType: " + mUsbStorageType);
                    if (mUsbStorageType.equals(UsbManager.USB_FUNCTION_MTP)) {
                    id = com.android.internal.R.string.usb_cd_installer_notification_title;
                    } else {
                        id = com.android.internal.R.string.usb_ums_notification_title;
                    }
                } else if (containsFunction(mCurrentFunctions, UsbManager.USB_FUNCTION_ACCESSORY)) {
                    id = com.android.internal.R.string.usb_accessory_notification_title;
                } else if (containsFunction(mCurrentFunctions, UsbManager.USB_FUNCTION_CHARGING_ONLY)) {
                    id = com.android.internal.R.string.usb_charging_notification_title;
                    SXlog.d(TAG, "updateUsbNotification - containsFunction:  USB_FUNCTION_CHARGING_ONLY");
                } else {
                    // There is a different notification for USB tethering so we don't need one here
                    if (!containsFunction(mCurrentFunctions, UsbManager.USB_FUNCTION_RNDIS)) {
                        Slog.e(TAG, "No known USB function in updateUsbNotification");
                    }
                    if(FeatureOption.EVDO_DT_VIA_SUPPORT == true){
                        if(mPCModeEnable &&
                            containsFunction(mCurrentFunctions,UsbManager.USB_FUNCTION_VIA_CDROM)){
                            id = com.android.internal.R.string.usb_autorun_cdrom_notification_title;
                            SXlog.d(TAG, "updateUsbNotification - containsFunction:  USB_FUNCTION_VIA_CDROM");
                        }
                    }
                }
            }
            //ALPS00112030 modification
            //Modified by Ainge for the "notification updated" should be after boot complete.
            //update while System ready is too early that the "SystemUIService(  314): loading: class com.android.systemui.statusbar.phone.PhoneStatusBar" is not ready
            if (id != mUsbNotificationId && mBootCompleted) {
            //if (id != mUsbNotificationId) {
            //ALPS00112030 modification
                // clear notification if title needs changing
                if (mUsbNotificationId != 0) {
                    mNotificationManager.cancel(mUsbNotificationId);
                    mUsbNotificationId = 0;
                }
                if (id != 0) {
                    CharSequence message = r.getText(
                            com.android.internal.R.string.usb_notification_message);
                    CharSequence title = r.getText(id);

                    Notification notification = new Notification();
                    notification.icon = com.android.internal.R.drawable.stat_sys_data_usb;
                    notification.when = 0;
                    notification.flags = Notification.FLAG_ONGOING_EVENT;
                    notification.tickerText = title;
                    notification.defaults = 0; // please be quiet
                    notification.sound = null;
                    notification.vibrate = null;

                    Intent intent = Intent.makeRestartActivityTask(
                            new ComponentName("com.android.settings",
                                    "com.android.settings.UsbSettings"));
                    PendingIntent pi = PendingIntent.getActivity(mContext, 0,
                            intent, 0);
                    notification.setLatestEventInfo(mContext, title, message, pi);
                    mNotificationManager.notify(id, notification);
                    mUsbNotificationId = id;
                }
            }
        }

        private void updateAdbNotification() {
            if (mNotificationManager == null) return;
            final int id = com.android.internal.R.string.adb_active_notification_title;

            mAdbUpdateLock.lock();

            if (mAdbEnabled && mConnected) {
                if ("0".equals(SystemProperties.get("persist.adb.notify"))) return;

                //ALPS00112030 modification
                //Modified by Ainge for the "notification updated" should be after boot complete.
                //update while System ready is too early that the "SystemUIService(  314): loading: class com.android.systemui.statusbar.phone.PhoneStatusBar" is not ready
                //if (!mAdbNotificationShown) {
                if (mBootCompleted && !mAdbNotificationShown) {
                //ALPS00112030 modification
                    Resources r = mContext.getResources();
                    CharSequence title = r.getText(id);
                    CharSequence message = r.getText(
                            com.android.internal.R.string.adb_active_notification_message);

                    Notification notification = new Notification();
                    notification.icon = com.android.internal.R.drawable.stat_sys_adb;
                    notification.when = 0;
                    notification.flags = Notification.FLAG_ONGOING_EVENT;
                    notification.tickerText = title;
                    notification.defaults = 0; // please be quiet
                    notification.sound = null;
                    notification.vibrate = null;

                    Intent intent = Intent.makeRestartActivityTask(
                            new ComponentName("com.android.settings",
                                    "com.android.settings.DevelopmentSettings"));
                    PendingIntent pi = PendingIntent.getActivity(mContext, 0,
                            intent, 0);
                    notification.setLatestEventInfo(mContext, title, message, pi);
                    mAdbNotificationShown = true;
                    mNotificationManager.notify(id, notification);
                }
            } else if (mAdbNotificationShown) {
                mAdbNotificationShown = false;
                mNotificationManager.cancel(id);
            }
            mAdbUpdateLock.unlock();
        }

        /*
             * If the device supports built-in installer(sys.usb.mtk_bicr_support=yes) and USB has been connected, USB service will
             * ask activity manager to show Built-in install Activity UI to ask user if turn on usb cd-rom or not.
             */
        private final void showBuiltinInstallerUI(boolean enable) {
            if(enable == true && "yes".equals(SystemProperties.get("sys.usb.mtk_bicr_support"))) {
                Intent dialogIntent = new Intent();
                dialogIntent.setClassName("com.android.systemui","com.android.systemui.usb.BuiltinInstallerActivity");
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    mContext.startActivity(dialogIntent);
                } catch (ActivityNotFoundException e) {
                    Slog.e(TAG, "unable to start BuiltinInstallerActivity");
                }
                Slog.v(TAG, "Show UI");
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw) {
            pw.println("  USB Device State:");
            pw.println("    Current Functions: " + mCurrentFunctions);
            pw.println("    Default Functions: " + mDefaultFunctions);
            pw.println("    mConnected: " + mConnected);
            pw.println("    mConfigured: " + mConfigured);
            pw.println("    mCurrentAccessory: " + mCurrentAccessory);
            try {
                pw.println("    Kernel state: "
                        + FileUtils.readTextFile(new File(STATE_PATH), 0, null).trim());
                pw.println("    Kernel function list: "
                        + FileUtils.readTextFile(new File(FUNCTIONS_PATH), 0, null).trim());
                pw.println("    Mass storage backing file: "
                        + FileUtils.readTextFile(new File(MASS_STORAGE_FILE_PATH), 0, null).trim());
            } catch (IOException e) {
                pw.println("IOException: " + e);
            }
        }
    }

    /* returns the currently attached USB accessory */
    public UsbAccessory getCurrentAccessory() {
        return mHandler.getCurrentAccessory();
    }

    /* opens the currently attached USB accessory */
    public ParcelFileDescriptor openAccessory(UsbAccessory accessory) {
        UsbAccessory currentAccessory = mHandler.getCurrentAccessory();
        if (currentAccessory == null) {
            throw new IllegalArgumentException("no accessory attached");
        }
        if (!currentAccessory.equals(accessory)) {
            String error = accessory.toString()
                    + " does not match current accessory "
                    + currentAccessory;
            throw new IllegalArgumentException(error);
        }
        mSettingsManager.checkPermission(accessory);
        return nativeOpenAccessory();
    }

    public void setCurrentFunction(String function, boolean makeDefault) {
        if (DEBUG) Slog.d(TAG, "setCurrentFunction(" + function + ") default: " + makeDefault);
        mHandler.sendMessage(MSG_SET_CURRENT_FUNCTION, function, makeDefault);
    }

    public void setMassStorageBackingFile(String path) {
        if (path == null) path = "";
        try {
            FileUtils.stringToFile(MASS_STORAGE_FILE_PATH, path);
        } catch (IOException e) {
           Slog.e(TAG, "failed to write to " + MASS_STORAGE_FILE_PATH);
        }
    }

    private void readOemUsbOverrideConfig() {
        String[] configList = mContext.getResources().getStringArray(
            com.android.internal.R.array.config_oemUsbModeOverride);

        if (configList != null) {
            for (String config: configList) {
                String[] items = config.split(":");
                if (items.length == 3) {
                    if (mOemModeMap == null) {
                        mOemModeMap = new HashMap<String, List<Pair<String, String>>>();
                    }
                    List overrideList = mOemModeMap.get(items[0]);
                    if (overrideList == null) {
                        overrideList = new LinkedList<Pair<String, String>>();
                        mOemModeMap.put(items[0], overrideList);
                    }
                    overrideList.add(new Pair<String, String>(items[1], items[2]));
                }
            }
        }
    }

    private boolean needsOemUsbOverride() {
        if (mOemModeMap == null) return false;

        String bootMode = SystemProperties.get(BOOT_MODE_PROPERTY, "unknown");
        return (mOemModeMap.get(bootMode) != null) ? true : false;
    }

    private String processOemUsbOverride(String usbFunctions) {
        if ((usbFunctions == null) || (mOemModeMap == null)) return usbFunctions;

        String bootMode = SystemProperties.get(BOOT_MODE_PROPERTY, "unknown");

        List<Pair<String, String>> overrides = mOemModeMap.get(bootMode);
        if (overrides != null) {
            for (Pair<String, String> pair: overrides) {
                if (pair.first.equals(usbFunctions)) {
                    Slog.d(TAG, "OEM USB override: " + pair.first + " ==> " + pair.second);
                    return pair.second;
                }
            }
        }
        // return passed in functions as is.
        return usbFunctions;
    }

    public void dump(FileDescriptor fd, PrintWriter pw) {
        if (mHandler != null) {
            mHandler.dump(fd, pw);
        }
    }

    private native String[] nativeGetAccessoryStrings();
    private native ParcelFileDescriptor nativeOpenAccessory();
    private native boolean nativeIsStartRequested();
    private native boolean nativeInitUMSproperty();
}
