
package com.mediatek.nfc.tag.record.param;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.utils.Utils;

public class AirplaneModeEnablerParam extends SpinnerParamItem {
    private static final String TAG = Utils.TAG + "/AirplaneModeEnablerParam";

    public static AirplaneModeEnablerParam sInstance;

    private static Activity sActivity;

    public static AirplaneModeEnablerParam getInstance(Activity activity) {
        sActivity = activity;
        if (sInstance == null) {
            sInstance = new AirplaneModeEnablerParam();
        }
        return sInstance;
    }

    public AirplaneModeEnablerParam() {
        mStatusArray = sActivity.getResources().getStringArray(R.array.param_2_status_value);
        mParamPrefix = "airplanemode_enable_status=";
    }

    public boolean enableParam(Handler handler, String newStatus) {
        Utils.logv(TAG, "-->enableParam(), newStatus=" + newStatus);
        boolean targetModeOn = (!"0".equals(newStatus));
        boolean currentModeOn = isAirplaneModeOn(sActivity);
        if (targetModeOn == currentModeOn) {
            Utils.logw(TAG, "Current airplane mode is already what we want: on?" + currentModeOn);
            return true;
        } else {
            setAirplaneModeOn(targetModeOn);
        }
        return true;
    };

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    private void setAirplaneModeOn(boolean enabling) {
        Utils.logd(TAG, "-->setAirplaneModeOn(), enabling?" + enabling);
        // Change the system setting
        Settings.System.putInt(sActivity.getContentResolver(), Settings.System.AIRPLANE_MODE_ON,
                enabling ? 1 : 0);

        // Post the intent
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enabling);
        sActivity.sendBroadcast(intent);
    }

    @Override
    public View initLayoutView() {
        Utils.logv(TAG, "-->getLayoutView()");
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mLayoutView = inflater.inflate(R.xml.param_item_2_state_enabler, null);
        if (mLayoutView == null) {
            Utils.loge(TAG, "Fail to get layout view");
        } else { // Set AirplaneMode special spinner label
            TextView spinnerLabelView = (TextView) mLayoutView
                    .findViewById(R.id.param_spinner_label);
            if (spinnerLabelView != null) {
                spinnerLabelView.setText(R.string.param_airplanemode_title);
            }
        }
        return mLayoutView;
    }

    @Override
    public String getLabel() {
        return sActivity.getResources().getString(R.string.param_airplanemode_title);
    }
}
