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

package com.android.settings.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.LinearLayout;
import java.util.ArrayList;

import com.android.settings.R;
import com.android.settings.Utils;
import com.mediatek.xlog.Xlog;
/**
 * Dialog to configure the SSID and security settings
 * for Access Point operation
 */
public class WifiApDialog extends AlertDialog implements View.OnClickListener,
        TextWatcher, AdapterView.OnItemSelectedListener {
    private static final String TAG = "WifiApDialog";
    static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;

    private final DialogInterface.OnClickListener mListener;

    public static final int OPEN_INDEX = 0;
    public static final int WPA_INDEX = 1;
    public static final int WPA2_INDEX = 2;
    private static final int AP_SSID_MAX_LENGTH_BYTES = 32;

    private View mView;
    private TextView mSsid;
    private int mSecurityTypeIndex = OPEN_INDEX;
    private int mChannel = 0;
    private int mChannelWidth = 0;
    private EditText mPassword;
    private WifiManager mWifiManager;
    private Context mContext;
    private String[] mChannelList;
    private LinearLayout    mLinearLayout;
    private Spinner mSecurity;
    WifiConfiguration mWifiConfig;

    public WifiApDialog(Context context, DialogInterface.OnClickListener listener,
            WifiConfiguration wifiConfig) {
        super(context);
        mContext = context;
        mListener = listener;
        mWifiConfig = wifiConfig;
        if (wifiConfig != null) {
            mSecurityTypeIndex = getSecurityTypeIndex(wifiConfig);
            Xlog.d(TAG,"getconfig index:"+mSecurityTypeIndex);
        }
    }

    public static int getSecurityTypeIndex(WifiConfiguration wifiConfig) {
        if (wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return WPA_INDEX;
        } else if (wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA2_PSK)) {
            return WPA2_INDEX;
        }
        return OPEN_INDEX;
    }

    public WifiConfiguration getConfig() {

        WifiConfiguration config = new WifiConfiguration();

        /**
         * TODO: SSID in WifiConfiguration for soft ap
         * is being stored as a raw string without quotes.
         * This is not the case on the client side. We need to
         * make things consistent and clean it up
         */
        config.SSID = mSsid.getText().toString();
        config.channel = mChannel;
        config.channelWidth = mChannelWidth;

        switch (mSecurityTypeIndex) {
            case OPEN_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                return config;

            case WPA_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mPassword.length() != 0) {
                    String password = mPassword.getText().toString();
                    config.preSharedKey = password;
                }
                return config;

            case WPA2_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mPassword.length() != 0) {
                    String password = mPassword.getText().toString();
                    config.preSharedKey = password;
                }
                return config;
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Context context = getContext();

        mView = getLayoutInflater().inflate(R.layout.wifi_ap_dialog, null);
        mSecurity = ((Spinner) mView.findViewById(R.id.security));
        if(Utils.isOrangeLoad()){
            String[] setUpArray = context.getResources().getStringArray(R.array.wifi_ap_security);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for(int i = 1; i < 3;i++){
                adapter.add(setUpArray[i]);
            }
            mSecurity.setAdapter(adapter);
        }

        Spinner mChannelSpinner = ((Spinner) mView.findViewById(R.id.channel));
        Spinner mChannelWidthSpinner = ((Spinner) mView.findViewById(R.id.channel_width));

        setView(mView);
        setInverseBackgroundForced(true);

        setTitle(R.string.wifi_tether_configure_ap_text);
        mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
        mSsid = (TextView) mView.findViewById(R.id.ssid);
        mPassword = (EditText) mView.findViewById(R.id.password);

        setButton(BUTTON_SUBMIT, context.getString(R.string.wifi_save), mListener);
        setButton(DialogInterface.BUTTON_NEGATIVE,
        context.getString(R.string.wifi_cancel), mListener);

        if (mWifiConfig != null) {
            mSsid.setText(mWifiConfig.SSID);
            if(Utils.isOrangeLoad()){
                Xlog.d(TAG,"init setSelection:" + (mSecurityTypeIndex-1));
                mSecurity.setSelection(mSecurityTypeIndex - 1);
            } else {
                mSecurity.setSelection(mSecurityTypeIndex);
            }
            if (mSecurityTypeIndex == WPA_INDEX ||
                    mSecurityTypeIndex == WPA2_INDEX) {
                  mPassword.setText(mWifiConfig.preSharedKey);
            }
            mChannel = mWifiConfig.channel;
            mChannelWidth = mWifiConfig.channelWidth;
        }


        // disabled channel and bandwidth setting when hotspot is not enabled 
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        if (SystemProperties.getInt("mediatek.wlan.channelselect", 0) == 0 || mWifiManager.getWifiApState() != WifiManager.WIFI_AP_STATE_ENABLED) {
            mLinearLayout = (LinearLayout) mView.findViewById(R.id.type);
            mLinearLayout.removeView(mView.findViewById(R.id.channel_text));
            mLinearLayout.removeView(mView.findViewById(R.id.width_text));
            mLinearLayout.removeView(mView.findViewById(R.id.channel));
            mLinearLayout.removeView(mView.findViewById(R.id.channel_width));
        } else {

            // temporarily remove channel bandwidth which is not supported
            mLinearLayout = (LinearLayout) mView.findViewById(R.id.type);
            mLinearLayout.removeView(mView.findViewById(R.id.width_text));
            mLinearLayout.removeView(mView.findViewById(R.id.channel_width));
            // end

            ArrayList<String> mTmpChannelList = new ArrayList<String>();
            mTmpChannelList.add(context.getString(R.string.wifi_tether_auto_channel_text));
            for (String s : mWifiManager.getAccessPointPreferredChannels()) {
                mTmpChannelList.add(s);
            } 

            mChannelList = (String[]) mTmpChannelList.toArray(new String[mTmpChannelList.size()]);

            if (mChannelList != null) {
                int i = 0;
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, mChannelList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                mChannelSpinner.setAdapter(adapter);
                if (mChannel != 0) {
                    for (i = 1; i < mChannelList.length; i++) {
                        if (mChannelList[i].equals(mChannel+"")) {
                            break;
                        }
                    }
                    if (i == mChannelList.length) {
                        i = 0;
                    }
                }

                mChannelSpinner.setSelection(i);
                mChannelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                        public void onItemSelected(AdapterView parent, View view, int position, long id) {
                            try {
                                if (position == 0) {
                                    mChannel = 0;
                                } else {
                                    mChannel = Integer.parseInt(mChannelList[position]);
                            }
                            } catch (Exception e) {
                        // channel error
                            }
                        }
                        public void onNothingSelected(AdapterView parent) {
                            }
                });    

            }

            mChannelWidthSpinner.setSelection(mChannelWidth);

            if (mChannelWidthSpinner != null) {
                mChannelWidthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                        public void onItemSelected(AdapterView parent, View view, int position, long id) {
                            try {
                                if (position == 0) {
                                    mChannelWidth = 0;
                                } else {
                                    mChannelWidth = 1;
                                }
                            } catch (Exception e) {
                                // channel width error
                            }
                        }
                        public void onNothingSelected(AdapterView parent) {
                        }
               });
            }


        }

        //SSID max length must shorter than 32 bytes
        mSsid.setFilters(new InputFilter[] {
                    new Utf8ByteLengthFilter(AP_SSID_MAX_LENGTH_BYTES)});
        mSsid.addTextChangedListener(this);
        mPassword.addTextChangedListener(this);
        ((CheckBox) mView.findViewById(R.id.show_password)).setOnClickListener(this);
        mSecurity.setOnItemSelectedListener(this);

        super.onCreate(savedInstanceState);

        showSecurityFields();
        validate();
    }

    private void validate() {
        if ((mSsid != null && mSsid.length() == 0) ||
                   (((mSecurityTypeIndex == WPA_INDEX) || (mSecurityTypeIndex == WPA2_INDEX))&&
                        mPassword.length() < 8)) {
            getButton(BUTTON_SUBMIT).setEnabled(false);
        } else {
            getButton(BUTTON_SUBMIT).setEnabled(true);
        }
    }

    public void onClick(View view) {
        mPassword.setInputType(
                InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_TEXT_VARIATION_PASSWORD));
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void afterTextChanged(Editable editable) {
        validate();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mSecurityTypeIndex = position;
        if(Utils.isOrangeLoad()){
            mSecurityTypeIndex = position + 1;
            Xlog.d(TAG,"Selection index:" + mSecurityTypeIndex);
        }
        showSecurityFields();
        validate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void showSecurityFields() {
        if (mSecurityTypeIndex == OPEN_INDEX) {
            mView.findViewById(R.id.fields).setVisibility(View.GONE);
            return;
        }
        mView.findViewById(R.id.fields).setVisibility(View.VISIBLE);
    }
    public void closeSpinnerDialog(){
        if(mSecurity!=null && mSecurity.isPopupShowing()){
            mSecurity.dismissPopup();
        }
    }
}
