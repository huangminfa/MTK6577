package com.mediatek.nfc.tag.record.param;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AutoRotateParam extends SpinnerParamItem {
    private static final String TAG = Utils.TAG + "/AutoRotateParam";

    public static AutoRotateParam sInstance;

    private static Activity sActivity;

    public static AutoRotateParam getInstance(Activity activity) {
        sActivity = activity;
        if (sInstance == null) {
            sInstance = new AutoRotateParam();
        }
        return sInstance;
    }

    public AutoRotateParam() {
        mStatusArray = sActivity.getResources().getStringArray(R.array.param_2_status_value);
        mParamPrefix = "autorotate_enable_status=";
    }

    @Override
    public boolean enableParam(Handler handler, String newStatus) {
        int currentRotateState = getCurrentAutoRotateState();
        Utils.logv(TAG, "-->enableParam(), newStatus=" + newStatus + ", currentRotateState="
                + currentRotateState);

        try {
            Class serviceManager = Class.forName("android.os.ServiceManager");
            Method getService = serviceManager.getDeclaredMethod("getService", String.class);

            IBinder binder = (IBinder) getService.invoke(null, Context.WINDOW_SERVICE);
            Utils.logi(TAG, "Get ServiceManager and WindowService Binder successfully.");
            // IWindowManager wm = IWindowManager.Stub.asInterface(binder);
            Utils.logi(TAG, "Get hidden WindowService successfully.");

            Utils.logi(TAG, "Process.myPid()=" + Process.myPid());
            Utils.logi(TAG, "Process.myUid()=" + Process.myUid());

            // wm.thawRotation();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        // catch (RemoteException e) {
        // e.printStackTrace();
        // }

        // TODO Should call IWindowManager.thawRotation() and
        // IWindowManager.freezeRotation(int rotation) for ICS and later version

        // need to turn off
        if ("0".equals(newStatus) && currentRotateState != 0) { 
            setAutoRotateState(0);
        } else if ("1".equals(newStatus) && currentRotateState == 0) {
            setAutoRotateState(1);
        }
        return true;
    }

    private int getCurrentAutoRotateState() {
        return Settings.System.getInt(sActivity.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0);
    }

    private void setAutoRotateState(int newValue) {
        Settings.System.putInt(sActivity.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, newValue);
    }

    @Override
    public String getLabel() {
        return sActivity.getResources().getString(R.string.param_autorotate_title);
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
                spinnerLabelView.setText(R.string.param_autorotate_title);
            }
        }
        return mLayoutView;
    }

}
