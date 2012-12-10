
package com.mediatek.nfc.tag.record.param;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.record.ParamRecord;
import com.mediatek.nfc.tag.utils.Utils;

public class WifiEnablerParam extends SpinnerParamItem {
    private static final String TAG = Utils.TAG + "/WifiEnablerParam";

    public static WifiEnablerParam sInstance;

    private static Activity sActivity;

    private static final int ENABLE_WIFI_TIMEOUT = 10000;

    private WifiStateListener mWifiStateListener = null;

    public static WifiEnablerParam getInstance(Activity activity) {
        sActivity = activity;
        if (sInstance == null) {
            sInstance = new WifiEnablerParam();
        }
        return sInstance;
    }

    public WifiEnablerParam() {
        mStatusArray = sActivity.getResources().getStringArray(R.array.param_2_status_value);
        mParamPrefix = "wifi_enable_status=";
    }

    @Override
    public boolean enableParam(Handler handler, String newStatus) {
        Utils.logv(TAG, "-->enableParam(), newStatus=" + newStatus);
        boolean result = false;
        WifiManager wifiManager = (WifiManager) sActivity.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Utils.loge(TAG, "Fail to get Wifi Manager service");
            return false;
        }

        int currentWifiState = wifiManager.getWifiState();

        if ("0".equals(newStatus)) { // disable Wifi
            if (currentWifiState == WifiManager.WIFI_STATE_DISABLED
                    || currentWifiState == WifiManager.WIFI_STATE_DISABLING) {
                Utils.logw(TAG, "Wifi is already/being disabled");
                return true;
            }
            result = wifiManager.setWifiEnabled(false);
        } else {
            if (currentWifiState == WifiManager.WIFI_STATE_ENABLED
                    || currentWifiState == WifiManager.WIFI_STATE_ENABLING) {
                Utils.logw(TAG, "Wifi is already/being enabled");
                return true;
            }
            result = wifiManager.setWifiEnabled(true);
        }
        mWifiStateListener = new WifiStateListener(handler);
        sActivity.registerReceiver(mWifiStateListener, new IntentFilter(
                WifiManager.WIFI_STATE_CHANGED_ACTION));

        handler.obtainMessage(ParamRecord.MSG_BEGIN_WAIT).sendToTarget();
        // Transfer the Broadcast receiver object. If timeout, unregister this
        // receiver before activity is finished.
        handler.sendMessageDelayed(Message.obtain(handler, ParamRecord.MSG_WAIT_WIFI_TIMEOUT,
                mWifiStateListener), ENABLE_WIFI_TIMEOUT);

        return result;
    }

    class WifiStateListener extends BroadcastReceiver {
        private Handler mHandler = null;

        public WifiStateListener(Handler handler) {
            this.mHandler = handler;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN);
                Utils.logd(TAG, "Wifi new state = " + state);
                if (state == WifiManager.WIFI_STATE_DISABLED
                        || state == WifiManager.WIFI_STATE_ENABLED) {
                    Utils.logi(TAG, "Wifi state changed successfully.");
                    mHandler.removeMessages(ParamRecord.MSG_WAIT_WIFI_TIMEOUT);
                    mHandler.obtainMessage(ParamRecord.MSG_END_WAIT).sendToTarget();
                    sActivity.unregisterReceiver(mWifiStateListener);
                }
            }
        }
    }

    @Override
    public String getLabel() {
        return sActivity.getResources().getString(R.string.param_wifienabler_title);
    }

    @Override
    public View initLayoutView() {
        Utils.logv(TAG, "-->initLayoutView()");
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mLayoutView = inflater.inflate(R.xml.param_item_2_state_enabler, null);
        if (mLayoutView == null) {
            Utils.loge(TAG, "Fail to get layout view");
        } else { // Set WiFi special spinner label
            TextView spinnerLabelView = (TextView) mLayoutView
                    .findViewById(R.id.param_spinner_label);
            if (spinnerLabelView != null) {
                spinnerLabelView.setText(R.string.param_wifienabler_title);
            }
        }
        return mLayoutView;
    }

}
