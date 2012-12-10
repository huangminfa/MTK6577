package com.mediatek.nfc.tag.record.param;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.record.ParamRecord;
import com.mediatek.nfc.tag.utils.Utils;

public class BTEnablerParam extends SpinnerParamItem {
    private static final String TAG = Utils.TAG + "/BTEnablerParam";

    public static BTEnablerParam sInstance;

    private static Activity sActivity;

    private static final int ENABLE_BT_TIMEOUT = 10000;

    private BTStateListener mBTStateListener = null;

    public static BTEnablerParam getInstance(Activity activity) {
        sActivity = activity;
        if (sInstance == null) {
            sInstance = new BTEnablerParam();
        }
        return sInstance;
    }

    public BTEnablerParam() {
        mStatusArray = sActivity.getResources().getStringArray(R.array.param_2_status_value);
        mParamPrefix = "bt_enable_status=";
    }

    @Override
    public boolean enableParam(Handler handler, String newStatus) {
        Utils.logv(TAG, "-->enableParam(), newStatus=" + newStatus);
        boolean result = false;

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Utils.loge(TAG, "Fail to get loacal bluetooth adapter");
            return result;
        }

        int currentBTState = adapter.getState();
        if ("0".equals(newStatus)) { // disable BT
            if (currentBTState == BluetoothAdapter.STATE_OFF
                    || currentBTState == BluetoothAdapter.STATE_TURNING_OFF) {
                Utils.logw(TAG, "BT is already/being disabled");
                return true;
            }
            result = adapter.disable();
        } else {
            if (currentBTState == BluetoothAdapter.STATE_ON
                    || currentBTState == BluetoothAdapter.STATE_TURNING_ON) {
                Utils.logw(TAG, "BT is already/being enabled");
                return true;
            }
            result = adapter.enable();
        }

        mBTStateListener = new BTStateListener(handler);
        sActivity.registerReceiver(mBTStateListener, new IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED));

        handler.obtainMessage(ParamRecord.MSG_BEGIN_WAIT).sendToTarget();
        handler.sendMessageDelayed(
                Message.obtain(handler, ParamRecord.MSG_WAIT_BT_TIMEOUT, mBTStateListener),
                ENABLE_BT_TIMEOUT);

        return result;
    }

    class BTStateListener extends BroadcastReceiver {
        private Handler mHandler = null;

        public BTStateListener(Handler handler) {
            this.mHandler = handler;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent
                        .getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                Utils.logd(TAG, "Bluetooth new state = " + state);
                if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_ON) {
                    Utils.logi(TAG, "Bluetooth state changed successfully.");
                    mHandler.removeMessages(ParamRecord.MSG_WAIT_BT_TIMEOUT);
                    mHandler.obtainMessage(ParamRecord.MSG_END_WAIT).sendToTarget();
                    sActivity.unregisterReceiver(mBTStateListener);
                }
            }
        }
    }

    @Override
    public String getLabel() {
        return sActivity.getResources().getString(R.string.param_btenabler_title);
    }

    @Override
    public View initLayoutView() {
        Utils.logv(TAG, "-->getLayoutView()");
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mLayoutView = inflater.inflate(R.xml.param_item_2_state_enabler, null);
        if (mLayoutView == null) {
            Utils.loge(TAG, "Fail to get layout view");
        } else { // Set Bluetooth special spinner label
            TextView spinnerLabelView = (TextView) mLayoutView
                    .findViewById(R.id.param_spinner_label);
            if (spinnerLabelView != null) {
                spinnerLabelView.setText(R.string.param_btenabler_title);
            }
        }
        return mLayoutView;
    }

}
