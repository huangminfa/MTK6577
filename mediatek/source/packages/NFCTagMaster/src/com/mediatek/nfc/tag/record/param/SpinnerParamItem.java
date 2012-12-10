
package com.mediatek.nfc.tag.record.param;

import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.utils.Utils;

/**
 * Class that be able to adapter parameter items which can set its value by
 * spinner, like WiFi, BT, audio profile ...
 * 
 * @author MTK80906
 */
public abstract class SpinnerParamItem extends ParamItem {
    private static final String TAG = Utils.TAG + "/SpinnerParamItem";

    /**
     * Must be initialized in sub class
     */
    protected String mParamPrefix;

    protected View mLayoutView = null;

    protected Spinner mSpinner = null;

    protected CheckBox mCheckBox = null;

    /**
     * Must be initialized in sub class
     */
    protected String[] mStatusArray = null;

    @Override
    public boolean enableParam(Handler handler) {
        Utils.logv(TAG, "-->enableParam()");
        boolean result = false;

        if (mSpinner == null || mCheckBox == null) {
            Utils.loge(TAG, "Some item in this view is empty");
            return result;
        }
        if (!mCheckBox.isChecked()) {
            Utils.logi(TAG, "User did not select this parameter item");
            result = true;
            return result;
        }

        int selectedIndex = mSpinner.getSelectedItemPosition();
        if (selectedIndex < 0 || selectedIndex >= mStatusArray.length) {
            Utils.loge(TAG, "Invalid selected index: " + selectedIndex + ", status array size="
                    + mStatusArray.length);
        } else {
            String statusValue = mStatusArray[selectedIndex];
            result = enableParam(handler, statusValue);
        }
        return result;
    }

    @Override
    public View getEditItemView() {
        Utils.logd(TAG, "-->getEditItemView()");
        getParamItemView();
        if (mSpinner != null) {
            mSpinner.setEnabled(true);
            mSpinner.setSelection(0);
        }
        if (mCheckBox != null) {
            mCheckBox.setChecked(true);
        }
        return mLayoutView;
    }

    @Override
    public View getHistoryItemView(String paramStr) {
        Utils.logd(TAG, "-->getHistoryItemView()");
        return getReadItemView(paramStr);
    }

    @Override
    public String getParamStr() {
        Utils.logd(TAG, "-->getParamStr()");
        String result = "";
        if (mSpinner == null || mCheckBox == null) {
            Utils.loge(TAG, "Some item in this view is empty");
            return result;
        }
        if (!mCheckBox.isChecked()) {
            Utils.logi(TAG, "User did not select this parameter item");
            return result;
        }
        int selectedIndex = mSpinner.getSelectedItemPosition();
        if (selectedIndex < 0 || selectedIndex >= mStatusArray.length) {
            Utils.loge(TAG, "Invalid selected index: " + selectedIndex + ", status array size="
                    + mStatusArray.length);
        }
        result = mParamPrefix + mStatusArray[selectedIndex];
        Utils.logd(TAG, "<--getParamStr(), result=" + result);
        return result;
    }

    @Override
    public View getReadItemView(String paramStr) {
        Utils.logd(TAG, "-->getReadItemView(), paramStr=" + paramStr);
        if (TextUtils.isEmpty(paramStr) || !paramStr.startsWith(mParamPrefix)) {
            Utils.loge(TAG, "Unable to parse empty or invalid parameter string");
            return null;
        }
        getParamItemView();
        if (mLayoutView != null) {
            String value = paramStr.substring(mParamPrefix.length());
            int valueIndex = -1;
            for (int i = 0; i < mStatusArray.length; i++) {
                if (mStatusArray[i].equals(value)) {
                    valueIndex = i;
                    break;
                }
            }
            if (valueIndex < 0) {
                Utils.loge(TAG, "Invalid parameter value");
                return null;
            }

            if (mSpinner != null) {
                mSpinner.setEnabled(false);
                mSpinner.setSelection(valueIndex);
            }
            if (mCheckBox != null) {
                mCheckBox.setChecked(true);
            }
        } else {
            Utils.loge(TAG, "Fail to get layout view");
        }
        return mLayoutView;
    }

    @Override
    public boolean match(String paramStr) {
        return !TextUtils.isEmpty(paramStr) && paramStr.startsWith(mParamPrefix);
    }

    /**
     * Common method for each kind of item view
     * 
     * @return
     */
    private View getParamItemView() {
        initLayoutView();
        if (mLayoutView != null) {
            mSpinner = (Spinner) mLayoutView.findViewById(R.id.param_spinner_status_selector);
            if (mSpinner != null) {
                mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View view, int pos, long row) {
                        Utils.logi(TAG, "Spinner.onItemSelected(), pos=" + pos + ", row=" + row);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        Utils.logw(TAG, "Spinner.onNothingSelected()");
                    }
                });
            }
            mCheckBox = (CheckBox) mLayoutView.findViewById(R.id.param_spinner_chbox);
        }
        return mLayoutView;
    }
}
