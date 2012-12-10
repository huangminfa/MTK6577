
package com.mediatek.nfc.tag.record.param;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DataConnEnablerParam extends SpinnerParamItem {
    private static final String TAG = Utils.TAG + "/DataConnEnablerParam";

    public static DataConnEnablerParam sInstance;

    private static Activity sActivity;

    // private WifiStateListener mWifiStateListener = null;

    public static DataConnEnablerParam getInstance(Activity activity) {
        sActivity = activity;
        if (sInstance == null) {
            sInstance = new DataConnEnablerParam();
        }
        return sInstance;
    }

    public DataConnEnablerParam() {
        mStatusArray = sActivity.getResources().getStringArray(R.array.param_2_status_value);
        mParamPrefix = "dataconn_enable_status=";
    }

    @Override
    public boolean enableParam(Handler handler, String newStatus) {
        Utils.logv(TAG, "-->enableParam(), newStatus=" + newStatus);
        ConnectivityManager cm = (ConnectivityManager) sActivity
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            Method getMobileDataEnabled = ConnectivityManager.class
                    .getDeclaredMethod("getMobileDataEnabled");
            Method setMobileDataEnabled = ConnectivityManager.class.getDeclaredMethod(
                    "setMobileDataEnabled", boolean.class);
            boolean isCurrentEnabled = (Boolean) getMobileDataEnabled.invoke(cm);
            Utils.logd(TAG, "Current mobile data enabled?" + isCurrentEnabled);
            // Need to disable data
            if (isCurrentEnabled && "0".equals(newStatus)) {
                setMobileDataEnabled.invoke(cm, false);
                // TODO waiting command response
                Utils.logi(TAG, "Disable mobile data success.");
            } else if (!isCurrentEnabled && "1".equals(newStatus)) {
                setMobileDataEnabled.invoke(cm, true);
                // TODO waiting command response
                Utils.logi(TAG, "Enable mobile data success.");
            } else {
                Utils.logi(TAG, "Mobile data is already in the status what we want."
                        + "  isCurrentEnabled?" + isCurrentEnabled);
            }
            return true;
        } catch (NoSuchMethodException e) {
            Utils.loge(TAG, "NoSuchMethodException", e);
        } catch (IllegalArgumentException e) {
            Utils.loge(TAG, "IllegalArgumentException", e);
        } catch (IllegalAccessException e) {
            Utils.loge(TAG, "IllegalAccessException", e);
        } catch (InvocationTargetException e) {
            Utils.loge(TAG, "InvocationTargetException", e);
        }

        return false;
    }

    @Override
    public String getLabel() {
        return sActivity.getResources().getString(R.string.param_dataconn_title);
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
                spinnerLabelView.setText(R.string.param_dataconn_title);
            }
        }
        return mLayoutView;
    }

}
