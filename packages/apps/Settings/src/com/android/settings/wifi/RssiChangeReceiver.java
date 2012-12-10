package com.android.settings.wifi;

import com.android.settings.R;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.provider.Settings;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import com.mediatek.xlog.Xlog;

import java.util.ArrayList;
import java.util.List;
import com.android.internal.util.AsyncChannel;

public class RssiChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "RssiChangeReceiver";
    private static final int KEY_SELECT_SSID_AUTO = 0;
    private static final int KEY_SELECT_SSID_MANUL = 1;
    private static final int KEY_SELECT_SSID_ASK = 2;
    static final String OPTR = SystemProperties.get("ro.operator.optr", "");
    static boolean lastConnected = false;
    static boolean nowConnected = false;
    static String lastSsid;
	private WifiManager mWifiManager;
	private int mRssi;
	private List<WifiConfiguration> mConfigs;

    @Override
    public void onReceive(Context context, Intent intent) {
        Xlog.d(TAG,"onReceive");
	    if("OP01".equals(OPTR)){
            String action = intent.getAction();
            mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            mWifiManager.asyncConnect(context,new WifiServiceHandler());
            if(!mWifiManager.isWifiEnabled()){
                return;
            }
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO);
                if(info != null && nowConnected != info.isConnected()){
                    lastConnected = nowConnected;
                    nowConnected = info.isConnected();
                }
                if(lastConnected && !nowConnected){             
                    mWifiManager.startScanActive();
                }
                Xlog.d(TAG,"NETWORK_STATE_CHANGED_ACTION,lastConnected="+lastConnected+",nowConnected="+nowConnected); 
                return;
            } else if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)){
                if(!lastConnected || nowConnected || lastSsid == null) return;
                int currentRssi = getNetworkRssi(lastSsid);
                lastConnected = nowConnected;
                nowConnected = false;
                if(currentRssi!= Integer.MAX_VALUE && WifiManager.compareSignalLevel(currentRssi, -79) > 0){
                    Xlog.d(TAG,"SCAN_RESULTS_AVAILABLE_ACTION,rssi is better than -79, return");
                    return;
                } 
                Xlog.d(TAG,"SCAN_RESULTS_AVAILABLE_ACTION,lastConnected="+lastConnected+",nowConnected="+nowConnected);   
            }

            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if(wifiInfo == null){
                Xlog.d(TAG,"wifiInfo == null");
                return;
            }
            mRssi = wifiInfo.getRssi();
            mConfigs = mWifiManager.getConfiguredNetworks();
            Xlog.d(TAG,"wifiInfo.getSsid()="+wifiInfo.getSSID());
            if(wifiInfo.getSSID()!=null){
                lastSsid = wifiInfo.getSSID();
            }
            int mConfiguredApCount = mConfigs==null?0:mConfigs.size();
            if(mConfiguredApCount < 2 ){
                Xlog.d(TAG,"mConfiguredApCount<2");
                return;
            }

            int bestConfigSignal = getBestSignalId();
            if (WifiManager.compareSignalLevel(mRssi, -85) > 0 || bestConfigSignal==-1) {
                Xlog.d(TAG, "RSSI > -85,finish");
                return;
            }else{
                Xlog.d(TAG, "RSSI < -85");
            }
            
            int value = Settings.System.getInt(context.getContentResolver(),Settings.System. WIFI_SELECT_SSID_TYPE, 
                        Settings.System.WIFI_SELECT_SSID_ASK);
            Xlog.d(TAG, "reselect type is:"+value);
            switch(value){
                case KEY_SELECT_SSID_AUTO:
                    //connect to which AP
                    if(bestConfigSignal!=-1){
//                        mWifiManager.enableNetwork(bestConfigSignal,true);
//                        mWifiManager.reconnect();     
                        mWifiManager.connectNetwork(bestConfigSignal);
                        Xlog.d(TAG, "auto connect");
                    }
                    return;
                case KEY_SELECT_SSID_MANUL:
                    //do nothing
                    Xlog.d(TAG, "manul connect");
                    return;
                case KEY_SELECT_SSID_ASK:
                    Xlog.d(TAG, "always ask,create dialog");
                    Intent aIntent = new Intent();
                    aIntent.setAction("android.net.wifi.WIFI_RESELECTION_AP");
                    aIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(aIntent);
                    break;
            }
        }
    }
    private int getBestSignalId(){
        int networkId = -1;
        int rssi = -200;
        mConfigs = mWifiManager.getConfiguredNetworks();
        List<ScanResult> results = mWifiManager.getScanResults();
        if (mConfigs != null && results != null) {
            for (WifiConfiguration config : mConfigs) {
                for (ScanResult result : results) {
                    if(config!=null && config.SSID!=null && AccessPoint.removeDoubleQuotes(config.SSID).equals(result.SSID) 
                            && AccessPoint.getSecurity(config)==AccessPoint.getSecurity(result)){
                        if(WifiManager.compareSignalLevel(result.level, rssi) > 0){
                            networkId=config.networkId;
                            rssi=result.level;
                            Xlog.d(TAG, "getBestSignalId,config.SSID:"+config.SSID+"networkId:"+networkId);
                        }
                    }
                }
            }
        }
        if(WifiManager.compareSignalLevel(rssi, -79) > 0){
            Xlog.d(TAG, "there is ap's signal is better than -79.networkId="+networkId);
            return networkId;
        } else{
            Xlog.d(TAG, "there is no ap's signal is better than -79.");
            return -1;
        }
    }
    private int getNetworkRssi(String SSID){
        int rssi = Integer.MAX_VALUE;
        List<ScanResult> results = mWifiManager.getScanResults();
        if (SSID != null && results != null) {
            for (ScanResult result : results) {
                if(SSID.equals(result.SSID)){
                    rssi = result.level;
                    Xlog.d(TAG, "getNetworkRssi,SSID:"+result.SSID+",rssi:"+rssi);
                    break;
                }
            }
        }
        return rssi;
    }
    private class WifiServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED:
                    if (msg.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
                        //AsyncChannel in msg.obj
                    } else {
                        //AsyncChannel set up failure, ignore
                        Xlog.e(TAG, "Failed to establish AsyncChannel connection");
                    }
                    break;
                default:
                    //Ignore
                    break;
            }
        }
    }
}
