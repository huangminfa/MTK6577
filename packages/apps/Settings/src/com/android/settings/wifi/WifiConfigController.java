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

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import android.content.Context;
import android.content.res.Resources;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.net.ProxyProperties;
import android.net.RouteInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.IpAssignment;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.ProxySettings;
import android.net.wifi.WifiConfiguration.Status;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.net.wifi.WifiConfiguration.Protocol;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.security.Credentials;
import android.security.KeyStore;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.mediatek.featureoption.FeatureOption;
import com.android.settings.ProxySelector;
import com.android.settings.R;
import com.android.settings.Utils;
import com.mediatek.xlog.Xlog;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;

/**
 * The class for allowing UIs like {@link WifiDialog} and {@link WifiConfigUiBase} to
 * share the logic for controlling buttons, text fields, etc.
 */
public class WifiConfigController implements TextWatcher,
        View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String KEYSTORE_SPACE = "keystore://";

    private final WifiConfigUiBase mConfigUi;
    private final View mView;
    private final AccessPoint mAccessPoint;

    private boolean mEdit;

    private TextView mSsidView;

    // e.g. AccessPoint.SECURITY_NONE
    private int mAccessPointSecurity;
    private TextView mPasswordView;

    private Spinner mSecuritySpinner;
    private Spinner mEapMethodSpinner;
    private Spinner mEapCaCertSpinner;
    private Spinner mPhase2Spinner;
    private Spinner mEapUserCertSpinner;
    private TextView mEapIdentityView;
    private TextView mEapAnonymousView;

    /// M: add transmit key spinner @{
    private Spinner mWEPKeyIndex;
    private Spinner mWEPKeyType;
    ///@}

    /* This value comes from "wifi_ip_settings" resource array */
    private static final int DHCP = 0;
    private static final int STATIC_IP = 1;

    /* These values come from "wifi_network_setup" resource array */
    public static final int MANUAL = 0;
    public static final int WPS_PBC = 1;
//    public static final int WPS_KEYPAD = 2;
//    public static final int WPS_DISPLAY = 3;
    public static final int WPS_KEYPAD = 3;
    public static final int WPS_DISPLAY = 2;

    /* These values come from "wifi_proxy_settings" resource array */
    public static final int PROXY_NONE = 0;
    public static final int PROXY_STATIC = 1;

    private static int mConfiguredApCount;

    private static final String TAG = "WifiConfigController";

    private Spinner mNetworkSetupSpinner;
    private Spinner mIpSettingsSpinner;
    private TextView mIpAddressView;
    private TextView mGatewayView;
    private TextView mNetworkPrefixLengthView;
    private TextView mDns1View;
    private TextView mDns2View;

    private Spinner mProxySettingsSpinner;
    private TextView mProxyHostView;
    private TextView mProxyPortView;
    private TextView mProxyExclusionListView;

    //add for EAP_SIM/AKA
    private Spinner mSimSlot;
    private TelephonyManager mTm;

    /* WAPI */
    private Spinner mWapiAsCert;
    private Spinner mWapiClientCert;
    private boolean mHex;
    private static final String WLAN_PROP_KEY = "persist.sys.wlan";
    private static final String WIFI = "wifi";
    private static final String WAPI = "wapi";
    private static final String WIFI_WAPI = "wifi-wapi";
    private static final String DEFAULT_WLAN_PROP = WIFI_WAPI;
    private static final int SSID_MAX_LEN = 32;

    private IpAssignment mIpAssignment = IpAssignment.UNASSIGNED;
    private ProxySettings mProxySettings = ProxySettings.UNASSIGNED;
    private LinkProperties mLinkProperties = new LinkProperties();

    /* add for CMCC ap*/
    private boolean mIsCmccAp;
    private Spinner mPrioritySpinner;
    //here priority means order of its priority, the smaller value, the higher priority
    private int mPriority = -1;
    private String[] mPriorityArray;
    private TextView mNetworkNetmaskView;

    // True when this instance is used in SetupWizard XL context.
    private final boolean mInXlSetupWizard;

    static boolean requireKeyStore(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        String values[] = {config.ca_cert.value(), config.client_cert.value(),
                config.private_key.value(), config.ca_cert2.value()};
        for (String value : values) {
            if (value != null && value.startsWith(KEYSTORE_SPACE)) {
                return true;
            }
        }
        return false;
    }

    public WifiConfigController(
            WifiConfigUiBase parent, View view, AccessPoint accessPoint, boolean edit, TelephonyManager tm) {
        mConfigUi = parent;
        mInXlSetupWizard = (parent instanceof WifiConfigUiForSetupWizardXL);

        mView = view;
        mAccessPoint = accessPoint;
        mAccessPointSecurity = (accessPoint == null) ? AccessPoint.SECURITY_NONE :
                accessPoint.security;
        mEdit = edit;
        mTm = tm;
        mIsCmccAp = AccessPoint.isCmccAp(accessPoint);

        final Context context = mConfigUi.getContext();
        final Resources resources = context.getResources();

        //whether to show access point priority select spinner
//MTK_OP01_PROTECT_START
        int priorityType = Settings.System.getInt(context.getContentResolver(),
                Settings.System.WIFI_PRIORITY_TYPE, Settings.System.WIFI_PRIORITY_TYPE_DEFAULT);
        if(priorityType == Settings.System.WIFI_PRIORITY_TYPE_DEFAULT || !Utils.isCmccLoad()){
//MTK_OP01_PROTECT_END           
            mView.findViewById(R.id.cmcc_wlan_priority).setVisibility(View.GONE);
//MTK_OP01_PROTECT_START            
        }else{
            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            List<WifiConfiguration> mConfigs = mWifiManager.getConfiguredNetworks();
            mConfiguredApCount = mConfigs==null?0:mConfigs.size();

            mView.findViewById(R.id.cmcc_wlan_priority).setVisibility(View.VISIBLE);
            mPrioritySpinner = (Spinner)mView.findViewById(R.id.cmcc_priority_setter);
            if(mPrioritySpinner!=null){
                mPrioritySpinner.setOnItemSelectedListener(this);
                if(mAccessPoint != null && mAccessPoint.networkId != -1){
                    mPriorityArray = new String[mConfiguredApCount];
                }else{
                    //new configured AP, have highest priority by default
                    mPriorityArray = new String[mConfiguredApCount+1];
                }
                for(int i=0;i<mPriorityArray.length;i++){
                    mPriorityArray[i]=String.valueOf(i+1);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        context, android.R.layout.simple_spinner_item, mPriorityArray);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mPrioritySpinner.setAdapter(adapter);
                int priorityCount = mPrioritySpinner.getCount();
                int priorityOrder;
                if(mAccessPoint != null && mAccessPoint.networkId != -1){
                    priorityOrder = priorityCount - mAccessPoint.getConfig().priority+1;
                    mPriority = priorityOrder;
                }else{
                   // priorityOrder = priorityCount;
                   //  mPriority = priorityCount;
                  //new configured AP will have highest priority by default
                    priorityOrder = 1;
                    mPriority = 1;
                }
                Xlog.d(TAG, "onCreate(), priorityOrder="+priorityOrder+", mPriority="+mPriority);
                mPrioritySpinner.setSelection(priorityCount<priorityOrder?(priorityCount-1):(priorityOrder-1));
            }
        }
//MTK_OP01_PROTECT_END

        if (mAccessPoint == null) { // new network
            int viewId = R.id.security;
            mConfigUi.setTitle(R.string.wifi_add_network);

            mSsidView = (TextView) mView.findViewById(R.id.ssid);
            mSsidView.addTextChangedListener(this);
            mSsidView.setFilters(new InputFilter[] {new Utf8ByteLengthFilter(SSID_MAX_LEN)});
//MTK_OP01_PROTECT_START
            if(Utils.isCmccLoad()){
                TextView securityText = (TextView) mView.findViewById(R.id.security_text);
                securityText.setText(R.string.wifi_security_cmcc);
            }
//MTK_OP01_PROTECT_END
            mSecuritySpinner = ((Spinner) mView.findViewById(R.id.security));
            mSecuritySpinner.setOnItemSelectedListener(this);
            if (mInXlSetupWizard) {
                mView.findViewById(R.id.type_ssid).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.type_security).setVisibility(View.VISIBLE);
                // We want custom layout. The content must be same as the other cases.

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                        R.layout.wifi_setup_custom_list_item_1, android.R.id.text1,
                        context.getResources().getStringArray(R.array.wifi_security_no_eap));
                mSecuritySpinner.setAdapter(adapter);
            } else {
                mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
            }
            if (FeatureOption.MTK_WAPI_SUPPORT) {
                String type = SystemProperties.get(WLAN_PROP_KEY, DEFAULT_WLAN_PROP);
                if (type.equals(WIFI_WAPI)) {
                    if(AccessPoint.isWFATestSupported()){
                        viewId = R.id.security_wfa; // WIFI + WAPI, support separate WPA2 PSK security
                    }else{
                        viewId = R.id.security; // WIFI + WAPI 
                    }
                } else if (type.equals(WIFI)) {
                  if(AccessPoint.isWFATestSupported()){
                        viewId = R.id.wpa_security_wfa; // WIFI only, support separate WPA2 PSK security 
                    }else{
                        viewId = R.id.wpa_security; // WIFI only 
                    }
                } else if (type.equals(WAPI)) {
                    viewId = R.id.wapi_security; // WAPI only 
                }
            } else {
              if(AccessPoint.isWFATestSupported()){
                    viewId = R.id.wpa_security_wfa; // WIFI only, support separate WPA and WPA2 PSK security
                }else {
                    viewId = R.id.wpa_security; // WIFI only 
                }
            }

            switchWlanSecuritySpinner((Spinner) mView.findViewById(viewId));
            mConfigUi.setSubmitButton(context.getString(R.string.wifi_save));
        } else {
            mConfigUi.setTitle(mAccessPoint.ssid);

            mIpSettingsSpinner = (Spinner) mView.findViewById(R.id.ip_settings);
            mIpSettingsSpinner.setOnItemSelectedListener(this);
            mProxySettingsSpinner = (Spinner) mView.findViewById(R.id.proxy_settings);
            mProxySettingsSpinner.setOnItemSelectedListener(this);

            ViewGroup group = (ViewGroup) mView.findViewById(R.id.info);

            DetailedState state = mAccessPoint.getState();
            if (state != null) {
                addRow(group, R.string.wifi_status, Summary.get(mConfigUi.getContext(), state));
            }

            int level = mAccessPoint.getLevel();
            if (level != -1) {
                String[] signal = resources.getStringArray(R.array.wifi_signal);
                addRow(group, R.string.wifi_signal, signal[level]);
            }

            WifiInfo info = mAccessPoint.getInfo();
            if (info != null && info.getLinkSpeed() != -1) {
                addRow(group, R.string.wifi_speed, info.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS);
            }
//MTK_OP01_PROTECT_START
            if(Utils.isCmccLoad()){
                addRow(group, R.string.wifi_security_cmcc, mAccessPoint.getSecurityString(false));
            } else 
//MTK_OP01_PROTECT_END
            {
                addRow(group, R.string.wifi_security, mAccessPoint.getSecurityString(false));
            }
            boolean showAdvancedFields = false;
            if (mAccessPoint.networkId != INVALID_NETWORK_ID) {
                WifiConfiguration config = mAccessPoint.getConfig();
                if (config.ipAssignment == IpAssignment.STATIC) {
                    mIpSettingsSpinner.setSelection(STATIC_IP);
                    showAdvancedFields = true;
                } else {
                    mIpSettingsSpinner.setSelection(DHCP);
                }
                //Display IP addresses
                for(InetAddress a : config.linkProperties.getAddresses()) {
                    addRow(group, R.string.wifi_ip_address, a.getHostAddress());
                }


                if (config.proxySettings == ProxySettings.STATIC) {
                    mProxySettingsSpinner.setSelection(PROXY_STATIC);
                    showAdvancedFields = true;
                } else {
                    mProxySettingsSpinner.setSelection(PROXY_NONE);
                }

                if (config.status == Status.DISABLED &&
                        config.disableReason == WifiConfiguration.DISABLED_DNS_FAILURE) {
                    addRow(group, R.string.wifi_disabled_heading,
                            context.getString(R.string.wifi_disabled_help));
                }

            }

            /* Show network setup options only for a new network */
            if (mAccessPoint.networkId == INVALID_NETWORK_ID && mAccessPoint.wpsAvailable) {
                showNetworkSetupFields();
            }

            if (mAccessPoint.networkId == INVALID_NETWORK_ID || mEdit) {
                showSecurityFields();
                showIpConfigFields();
                showProxyFields();
                mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.wifi_advanced_togglebox).setOnClickListener(this);
                if (showAdvancedFields) {
                    ((CheckBox) mView.findViewById(R.id.wifi_advanced_togglebox)).setChecked(true);
                    mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
                }
            }
//MTK_OP01_PROTECT_START
             else{
                mView.findViewById(R.id.cmcc_wlan_priority).setVisibility(View.GONE);
            }
//MTK_OP01_PROTECT_END
            if (mEdit) {
                mConfigUi.setSubmitButton(context.getString(R.string.wifi_save));
            } else {
                if (state == null && level != -1) {
                    mConfigUi.setSubmitButton(context.getString(R.string.wifi_connect));
                } 
//MTK_OP01_PROTECT_START              
                else if(state != null && Utils.isCmccLoad()){//disconnect button just take effect for CMCC load
                    mConfigUi.setSubmitButton(context.getString(R.string.wifi_disconnect));
                }
//MTK_OP01_PROTECT_END        
                else {
                    mView.findViewById(R.id.ip_fields).setVisibility(View.GONE);
                }
                if (mAccessPoint.networkId != INVALID_NETWORK_ID && !mIsCmccAp) {
                    mConfigUi.setForgetButton(context.getString(R.string.wifi_forget));
                }
            }
        }


        mConfigUi.setCancelButton(context.getString(R.string.wifi_cancel));
        if (mConfigUi.getSubmitButton() != null) {
            enableSubmitIfAppropriate();
        }
    }
    public void verifyPassword(){
        if(mPasswordView==null){
            Xlog.d(TAG, "mPasswordView==null");
            return;
        }
        enableSubmitIfAppropriate();
    }
    public static String makeNAI(String imsi, String eapMethod) {
	
        //airplane mode & select wrong sim slot
        if(imsi==null){
            return addQuote("error");
        }

        StringBuffer NAI = new StringBuffer(40);
        // s = sb.append("a = ").append(a).append("!").toString();
        System.out.println("".length());

        
        if (eapMethod.equals("SIM")) 
        NAI.append("1"); 
        else if(eapMethod.equals("AKA")) 
        NAI.append("0");
        
        // add imsi
        NAI.append(imsi);
        NAI.append("@wlan.mnc");
        // add mnc
        NAI.append("0");
        NAI.append(imsi.substring(3, 5));
        NAI.append(".mcc");
        // add mcc

        NAI.append(imsi.substring(0, 3));

        // NAI.append(imsi.substring(5));
        NAI.append(".3gppnetwork.org");
        Xlog.d(TAG, NAI.toString());
        Xlog.d(TAG, "\"" + NAI.toString() + "\"");
        return addQuote(NAI.toString());
    }

    public static String  addQuote(String s) {
        return "\"" + s + "\"";
    }

    private void addRow(ViewGroup group, int name, String value) {
        View row = mConfigUi.getLayoutInflater().inflate(R.layout.wifi_dialog_row, group, false);
        ((TextView) row.findViewById(R.id.name)).setText(name);
        ((TextView) row.findViewById(R.id.value)).setText(value);
        group.addView(row);
    }

    /* show submit button if password, ip and proxy settings are valid */
    private void enableSubmitIfAppropriate() {
        Button submit = mConfigUi.getSubmitButton();
        if (submit == null) return;
        boolean enabled = false;

        boolean passwordInvalid = false;

        /* Check password invalidity for manual network set up alone */
        if (chosenNetworkSetupMethod() == MANUAL &&
            ((mAccessPointSecurity == AccessPoint.SECURITY_WEP && !isWEPKeyValid(mPasswordView.getText().toString())) ||
            ((mAccessPointSecurity == AccessPoint.SECURITY_PSK || mAccessPointSecurity == AccessPoint.SECURITY_WPA_PSK ||       
                mAccessPointSecurity == AccessPoint.SECURITY_WPA2_PSK) && mPasswordView.length() < 8)||
            (mAccessPointSecurity == AccessPoint.SECURITY_WAPI_PSK &&(mPasswordView.length() < 8 || 64 < mPasswordView.length() || 
                (mHex && !mPasswordView.getText().toString().matches("[0-9A-Fa-f]*")))) ||
            (mAccessPointSecurity == AccessPoint.SECURITY_WAPI_CERT &&(mWapiAsCert.getSelectedItemPosition() == 0 ||
                mWapiClientCert.getSelectedItemPosition() == 0)))) {
            passwordInvalid = true;
        }

        if ((mSsidView != null && mSsidView.length() == 0) ||
            ((mAccessPoint == null || mAccessPoint.networkId == INVALID_NETWORK_ID) &&
            passwordInvalid)) {
            enabled = false;
        } else {
            if (ipAndProxyFieldsAreValid()) {
                enabled = true;
            } else {
                enabled = false;
            }
        }
        submit.setEnabled(enabled);
    }

    /**
     * M: verify password
     * check whether we have got a valid WEP key
     * @param password
     * @return
     */
    private boolean isWEPKeyValid(String password){
        if(password == null || password.length() == 0){
            return false;
        }
        int keyType = 0;//password: auto, ASCII or Hex
        if(mWEPKeyType != null && mWEPKeyType.getSelectedItemPosition() != AdapterView.INVALID_POSITION){
            keyType = mWEPKeyType.getSelectedItemPosition();
        }
        int keyLength = password.length();
        if((keyLength == 10 || keyLength == 26 || keyLength == 32) &&
                password.matches("[0-9A-Fa-f]*") && (keyType == 0 || keyType == 2)){
            return true;
        }else if((keyLength == 5 || keyLength == 13 || keyLength == 16) &&
                (keyType == 0 || keyType == 1)){
            return true;
        }
        return false;
    }

    /* package */ WifiConfiguration getConfig() {
    	Context context = mConfigUi.getContext();
        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID && !mEdit) {
            return null;
        }

        WifiConfiguration config = new WifiConfiguration();

        
        if(FeatureOption.MTK_EAP_SIM_AKA==true){
        config.IMSI = addQuote("none");
        config.SIMSLOT = addQuote("-1");

        config.PCSC = addQuote("none");
        }
        if (mAccessPoint == null) {
            config.SSID = AccessPoint.convertToQuotedString(
                    mSsidView.getText().toString());
            // If the user adds a network manually, assume that it is hidden.
            config.hiddenSSID = true;
        } else if (mAccessPoint.networkId == INVALID_NETWORK_ID) {
            config.SSID = AccessPoint.convertToQuotedString(
                    mAccessPoint.ssid);
            config.BSSID = mAccessPoint.bssid;
        } else {
            config.networkId = mAccessPoint.networkId;
        }
//MTK_OP01_PROTECT_START        
        //set priority manually
        if(mPriority>=0){
            config.priority = mPriority;
        }
//MTK_OP01_PROTECT_END  
        switch (mAccessPointSecurity) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                break;

            case AccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (mPasswordView.length() != 0) {
                    int length = mPasswordView.length();
                    String password = mPasswordView.getText().toString();
                    /// M: get selected WEP key index @{
                    int keyIndex = 0;//selected password index, 0~3
                    if(mWEPKeyIndex != null && mWEPKeyIndex.getSelectedItemPosition() != AdapterView.INVALID_POSITION){
                        keyIndex = mWEPKeyIndex.getSelectedItemPosition();
                    }
                    /// @}
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 32) &&
                            password.matches("[0-9A-Fa-f]*")) {
                        /// M: hex password
                        config.wepKeys[keyIndex] = password;
                    } else {
                        /// M: ASCII password
                        config.wepKeys[keyIndex] = '"' + password + '"';
                    }
                    /// M: set wep index to configuration
                    config.wepTxKeyIndex = keyIndex;
                }
                break;
            case AccessPoint.SECURITY_WPA_PSK:
            case AccessPoint.SECURITY_WPA2_PSK:
            case AccessPoint.SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (mPasswordView.length() != 0) {
                    String password = mPasswordView.getText().toString();
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_EAP:
                config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
                config.eap.setValue((String) mEapMethodSpinner.getSelectedItem());
                if (!"AKA".equals((String) mEapMethodSpinner.getSelectedItem()) &&
                        !"SIM".equals((String) mEapMethodSpinner.getSelectedItem())) {
                    config.phase2.setValue((mPhase2Spinner.getSelectedItemPosition() == 0) ? "" :
                            "auth=" + mPhase2Spinner.getSelectedItem());
                    config.ca_cert.setValue((mEapCaCertSpinner.getSelectedItemPosition() == 0) ? "" :
                            KEYSTORE_SPACE + Credentials.CA_CERTIFICATE +
                            (String) mEapCaCertSpinner.getSelectedItem());
                    config.client_cert.setValue((mEapUserCertSpinner.getSelectedItemPosition() == 0) ?
                            "" : KEYSTORE_SPACE + Credentials.USER_CERTIFICATE +
                            (String) mEapUserCertSpinner.getSelectedItem());
                    config.private_key.setValue((mEapUserCertSpinner.getSelectedItemPosition() == 0) ?
                            "" : KEYSTORE_SPACE + Credentials.USER_PRIVATE_KEY +
                            (String) mEapUserCertSpinner.getSelectedItem());
                    config.identity.setValue((mEapIdentityView.length() == 0) ? "" :
                            mEapIdentityView.getText().toString());
                    config.anonymous_identity.setValue((mEapAnonymousView.length() == 0) ? "" :
                            mEapAnonymousView.getText().toString());
                    if (mPasswordView.length() != 0) {
                        Xlog.d("myaka","password length is not 0");
                        config.password.setValue(mPasswordView.getText().toString());
                    }else{
                        Xlog.d("myaka","password length is 0");
                    }
                }

                if(FeatureOption.MTK_EAP_SIM_AKA==true){
                // add support for EAP-SIM
                if ("SIM".equals((String) mEapMethodSpinner.getSelectedItem())) {

                    if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                        // for test
                        // config.IMSI = "460010452947852";
                        //R.string.eap_sim_slot_0
                        Xlog.d(TAG,"((String) mSimSlot.getSelectedItem()) "+((String) mSimSlot.getSelectedItem()));
                         Xlog.d(TAG,"R.string.eap_sim_slot_0 "+context.getString(R.string.eap_sim_slot_0));
                        if (context.getString(R.string.eap_sim_slot_0).equals((String) mSimSlot.getSelectedItem())) {
                            config.IMSI = makeNAI(mTm.getSubscriberIdGemini(0), "SIM");
                            Xlog.d(TAG, "config.IMSI: " + config.IMSI);
                            config.SIMSLOT = addQuote("0");
                            Xlog.d(TAG, "config.SIMSLOT " + addQuote("0"));
                            config.PCSC = addQuote("rild");
                            Xlog.d(TAG, "config.PCSC: " + addQuote("rild"));

                        } else if (context.getString(R.string.eap_sim_slot_1).equals((String) mSimSlot.getSelectedItem())) {
                            config.IMSI = makeNAI(mTm.getSubscriberIdGemini(1), "SIM");
                            Xlog.d(TAG, "config.IMSI: " + config.IMSI);
                            config.SIMSLOT = addQuote("1");
                            Xlog.d(TAG, "config.SIMSLOT " + addQuote("0"));
                            config.PCSC = addQuote("rild");
                            Xlog.d(TAG, "config.PCSC: " + addQuote("rild"));
                        } else{
						;
						}
					Xlog.d(TAG, "eap-sim, choose sim_slot" + (String) mSimSlot.getSelectedItem());
                    } else  {
                        config.IMSI = makeNAI(mTm.getSubscriberId(), "SIM");
                        Xlog.d(TAG, "config.IMSI: " + config.IMSI);
                        // config.IMSI = "460010452947852";
                        config.SIMSLOT = addQuote("0");
                        config.PCSC = addQuote("rild");
                    } 
                    
                    Xlog.d(TAG, "eap-sim, config.IMSI: " + config.IMSI);
                    Xlog.d(TAG, "eap-sim, config.SIMSLOT: " + config.SIMSLOT);

                }
                // add support for EAP-AKA
                else if ("AKA".equals((String) mEapMethodSpinner.getSelectedItem())) {

                    if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                        // for test
                        // config.IMSI = "5a01d5224f14222c5554102a10df5896";

                        if (context.getString(R.string.eap_sim_slot_0).equals((String) mSimSlot.getSelectedItem())) {
                            config.IMSI = makeNAI(mTm.getSubscriberIdGemini(0), "AKA");
                            Xlog.d(TAG, "config.IMSI: " + config.IMSI);
                            config.SIMSLOT = addQuote("0");
                            config.PCSC = addQuote("rild");

                        } else if (context.getString(R.string.eap_sim_slot_1).equals((String) mSimSlot.getSelectedItem())) {
                            config.IMSI = makeNAI(mTm.getSubscriberIdGemini(1), "AKA");
                            Xlog.d(TAG, "config.IMSI: " + config.IMSI);
                            config.SIMSLOT = addQuote("1");
                            config.PCSC = addQuote("rild");
                        } /*else {
                            config.IMSI = addQuote("error");
                            config.SIMSLOT = addQuote("-1");
                            config.PCSC = addQuote("error");
                        }*/
                        Xlog.d(TAG, "eap-aka, choose sim_slot" + (String) mSimSlot.getSelectedItem());
                    } else if ((FeatureOption.MTK_GEMINI_SUPPORT == false)) {
                        config.IMSI = makeNAI(mTm.getSubscriberId(), "AKA");
                        Xlog.d(TAG, "config.IMSI: " + config.IMSI);
                        // config.IMSI = "5a01d5224f14222c5554102a10df5896";
                        config.SIMSLOT = addQuote("0");
                        config.PCSC = addQuote("rild");
                    } else {
                        ;

                    }
                    
                    Xlog.d(TAG, "eap-aka, config.IMSI: " + config.IMSI);
                    Xlog.d(TAG, "eap-aka, config.SIMSLOT: " + config.SIMSLOT);

                }

                Xlog.d(TAG, "eap-sim/aka, config.toString(): " + config.toString());
                // add for eap-sim
                }
                break;
            case AccessPoint.SECURITY_WAPI_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WAPI_PSK);
                config.allowedProtocols.set(Protocol.WAPI);
                config.allowedPairwiseCiphers.set(PairwiseCipher.SMS4);
                config.allowedGroupCiphers.set(GroupCipher.SMS4);
                if (mPasswordView.length() != 0) {
                    String password = mPasswordView.getText().toString();
                    Xlog.v(TAG, "getConfig(), mHex=" + mHex);
                    if (mHex) { /* Hexadecimal */
                        config.preSharedKey = password;
                    } else { /* ASCII */
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_WAPI_CERT:
                config.allowedKeyManagement.set(KeyMgmt.WAPI_CERT);
                config.allowedProtocols.set(Protocol.WAPI);
                config.allowedPairwiseCiphers.set(PairwiseCipher.SMS4);
                config.allowedGroupCiphers.set(GroupCipher.SMS4);
                config.ca_cert2.setValue((mWapiAsCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.WAPI_AS_CERTIFICATE +
                        (String) mWapiAsCert.getSelectedItem());
                config.client_cert.setValue((mWapiClientCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.WAPI_USER_CERTIFICATE +
                        (String) mWapiClientCert.getSelectedItem());
                config.private_key.setValue((mWapiClientCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.WAPI_USER_CERTIFICATE +
                        (String) mWapiClientCert.getSelectedItem());
                break;
            default:
                    return null;
        }

        config.proxySettings = mProxySettings;
        config.ipAssignment = mIpAssignment;
        config.linkProperties = new LinkProperties(mLinkProperties);

        return config;
    }

    private boolean ipAndProxyFieldsAreValid() {
        mLinkProperties.clear();
        mIpAssignment = (mIpSettingsSpinner != null &&
                mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) ?
                IpAssignment.STATIC : IpAssignment.DHCP;

        if (mIpAssignment == IpAssignment.STATIC) {
            int result = validateIpConfigFields(mLinkProperties);
            if (result != 0) {
                return false;
            }
        }

        mProxySettings = (mProxySettingsSpinner != null &&
                mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) ?
                ProxySettings.STATIC : ProxySettings.NONE;

        if (mProxySettings == ProxySettings.STATIC) {
            String host = mProxyHostView.getText().toString();
            String portStr = mProxyPortView.getText().toString();
            String exclusionList = mProxyExclusionListView.getText().toString();
            int port = 0;
            int result = 0;
            try {
                port = Integer.parseInt(portStr);
                result = ProxySelector.validate(host, portStr, exclusionList);
            } catch (NumberFormatException e) {
                result = R.string.proxy_error_invalid_port;
            }
            if (result == 0) {
                ProxyProperties proxyProperties= new ProxyProperties(host, port, exclusionList);
                mLinkProperties.setHttpProxy(proxyProperties);
            } else {
                return false;
            }
        }
        return true;
    }

    private int validateIpConfigFields(LinkProperties linkProperties) {
        String ipAddr = mIpAddressView.getText().toString();
        InetAddress inetAddr = null;
        try {
            if(!isIpAddress(ipAddr)){
                return R.string.wifi_ip_settings_invalid_ip_address;
            }
            inetAddr = NetworkUtils.numericToInetAddress(ipAddr);
        } catch (IllegalArgumentException e) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }

        int networkPrefixLength = -1;
        try {
//MTK_OP01_PROTECT_START 
            if(Utils.isCmccLoad()){
                String netmask = mNetworkNetmaskView.getText().toString();
                if(isIpAddress(netmask)){
                    networkPrefixLength = getPrefixLength(netmask);
                }
            }else
//MTK_OP01_PROTECT_END
            {
                networkPrefixLength = Integer.parseInt(mNetworkPrefixLengthView.getText().toString());
            }
        } catch (NumberFormatException e) {
            // Use -1
            Xlog.d(TAG,"validateIpConfigFields NumberFormatException"+e+";");
        }
        if (networkPrefixLength < 0 || networkPrefixLength > 32) {
            Xlog.d(TAG,"validateIpConfigFields indalide prefix");
            return R.string.wifi_ip_settings_invalid_network_prefix_length;
        }
        linkProperties.addLinkAddress(new LinkAddress(inetAddr, networkPrefixLength));

        String gateway = mGatewayView.getText().toString();
        InetAddress gatewayAddr = null;
        try {
            gatewayAddr = NetworkUtils.numericToInetAddress(gateway);
        } catch (IllegalArgumentException e) {
            Xlog.d(TAG,"validateIpConfigFields indalide gateway");
            return R.string.wifi_ip_settings_invalid_gateway;
        }
        linkProperties.addRoute(new RouteInfo(gatewayAddr));

        String dns = mDns1View.getText().toString();
        InetAddress dnsAddr = null;
        try {
            dnsAddr = NetworkUtils.numericToInetAddress(dns);
        } catch (IllegalArgumentException e) {
            Xlog.d(TAG,"validateIpConfigFields indalide dns");
            return R.string.wifi_ip_settings_invalid_dns;
        }
        linkProperties.addDns(dnsAddr);
        if (mDns2View.length() > 0) {
            dns = mDns2View.getText().toString();
            try {
                dnsAddr = NetworkUtils.numericToInetAddress(dns);
            } catch (IllegalArgumentException e) {
                return R.string.wifi_ip_settings_invalid_dns;
            }
            linkProperties.addDns(dnsAddr);
        }
        return 0;
    }

    int chosenNetworkSetupMethod() {
        if (mNetworkSetupSpinner != null) {
            return mNetworkSetupSpinner.getSelectedItemPosition();
        }
        return MANUAL;
    }

    WpsInfo getWpsConfig() {
        WpsInfo config = new WpsInfo();
        switch (mNetworkSetupSpinner.getSelectedItemPosition()) {
            case WPS_PBC:
                config.setup = WpsInfo.PBC;
                break;
            case WPS_KEYPAD:
                config.setup = WpsInfo.KEYPAD;
                break;
            case WPS_DISPLAY:
                config.setup = WpsInfo.DISPLAY;
                break;
            default:
                config.setup = WpsInfo.INVALID;
                Xlog.e(TAG, "WPS not selected type");
                return config;
        }
        config.pin = ((TextView) mView.findViewById(R.id.wps_pin)).getText().toString();
        config.BSSID = (mAccessPoint != null) ? mAccessPoint.bssid : null;

        config.proxySettings = mProxySettings;
        config.ipAssignment = mIpAssignment;
        config.linkProperties = new LinkProperties(mLinkProperties);
        return config;
    }

    private void showSecurityFields() {
        if (mInXlSetupWizard) {
            // Note: XL SetupWizard won't hide "EAP" settings here.
            if (!((WifiSettingsForSetupWizardXL)mConfigUi.getContext()).initSecurityFields(mView,
                        mAccessPointSecurity)) {
                return;
            }
        }
        if(mAccessPoint == null && mAccessPointSecurity != AccessPoint.SECURITY_EAP){
            mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(View.GONE);
            mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.GONE);
            Xlog.d(TAG,"add network,mAccessPointSecurity != AccessPoint.SECURITY_EAP");
        }
        if (mAccessPointSecurity == AccessPoint.SECURITY_NONE) {
            mView.findViewById(R.id.security_fields).setVisibility(View.GONE);
            mView.findViewById(R.id.wapi_cert_fields).setVisibility(View.GONE);
            return;
        }
        mView.findViewById(R.id.security_fields).setVisibility(View.VISIBLE);
        /* Hexadecimal checkbox only for WAPI_PSK */
        mView.findViewById(R.id.hex_password).setVisibility(View.GONE);
        if (mAccessPointSecurity == AccessPoint.SECURITY_WAPI_PSK) {
            mView.findViewById(R.id.hex_password).setVisibility(View.VISIBLE);
            ((CheckBox) mView.findViewById(R.id.hex_password)).setChecked(mHex);
        }

        /// M: WEP transmit key & keytype @{
        if(mAccessPointSecurity == AccessPoint.SECURITY_WEP  && FeatureOption.WIFI_WEP_KEY_ID_SET){
            mView.findViewById(R.id.wep).setVisibility(View.VISIBLE);
            mWEPKeyType = (Spinner)mView.findViewById(R.id.wep_key_type);
            mWEPKeyIndex = (Spinner)mView.findViewById(R.id.wep_key_index);
            if(mWEPKeyType != null){
                mWEPKeyType.setOnItemSelectedListener(this);
            }
        }
        /// @}

        /* WAPI CERT */

        if (mAccessPointSecurity == AccessPoint.SECURITY_WAPI_CERT) {
            mView.findViewById(R.id.security_fields).setVisibility(View.GONE);
            mView.findViewById(R.id.wapi_cert_fields).setVisibility(View.VISIBLE);
            mWapiAsCert = (Spinner) mView.findViewById(R.id.wapi_as_cert);
            mWapiClientCert = (Spinner) mView.findViewById(R.id.wapi_user_cert);
            mWapiAsCert.setOnItemSelectedListener(this);
            mWapiClientCert.setOnItemSelectedListener(this);
            loadCertificates(mWapiAsCert, Credentials.WAPI_AS_CERTIFICATE);
            loadCertificates(mWapiClientCert, Credentials.WAPI_USER_CERTIFICATE);

            if (mAccessPoint != null && mAccessPoint.networkId != -1) {
                WifiConfiguration config = mAccessPoint.getConfig();
                setCertificate(mWapiAsCert, Credentials.WAPI_AS_CERTIFICATE,
                        config.ca_cert2.value());
                setCertificate(mWapiClientCert, Credentials.WAPI_USER_CERTIFICATE,
                        config.client_cert.value());
            }
            return;
        } else {
            mView.findViewById(R.id.wapi_cert_fields).setVisibility(View.GONE);
        }

        if (mPasswordView == null) {
            mPasswordView = (TextView) mView.findViewById(R.id.password);
            mPasswordView.addTextChangedListener(this);
            ((CheckBox) mView.findViewById(R.id.show_password)).setOnClickListener(this);
            ((CheckBox) mView.findViewById(R.id.hex_password)).setOnClickListener(this);
            if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
                mPasswordView.setHint(R.string.wifi_unchanged);
            }
        }

        if (mAccessPointSecurity != AccessPoint.SECURITY_EAP) {
            mView.findViewById(R.id.eap).setVisibility(View.GONE);
            mView.findViewById(R.id.eap_identity).setVisibility(View.GONE);
            return;
        }
        mView.findViewById(R.id.eap).setVisibility(View.VISIBLE);
        mView.findViewById(R.id.eap_identity).setVisibility(View.VISIBLE);
        if(mAccessPoint == null){
            Xlog.d(TAG,"add network,Security is AccessPoint.SECURITY_EAP");
            mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.wifi_advanced_togglebox).setOnClickListener(this);
            ((CheckBox) mView.findViewById(R.id.wifi_advanced_togglebox)).setChecked(false);
            mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.GONE);
        }
        if (mEapMethodSpinner == null) {
            
            if(FeatureOption.MTK_EAP_SIM_AKA==true){
            // add for eap-sim
                mEapMethodSpinner = (Spinner) mView.findViewById(R.id.method_sim_aka);
                mSimSlot = (Spinner) mView.findViewById(R.id.sim_slot);
            }else{
                mEapMethodSpinner = (Spinner) mView.findViewById(R.id.method);
            }
            mEapMethodSpinner.setOnItemSelectedListener(this);
            mPhase2Spinner = (Spinner) mView.findViewById(R.id.phase2);
            mEapCaCertSpinner = (Spinner) mView.findViewById(R.id.ca_cert);
            mEapUserCertSpinner = (Spinner) mView.findViewById(R.id.user_cert);
            mEapIdentityView = (TextView) mView.findViewById(R.id.identity);
            mEapAnonymousView = (TextView) mView.findViewById(R.id.anonymous);

            loadCertificates(mEapCaCertSpinner, Credentials.CA_CERTIFICATE);
            loadCertificates(mEapUserCertSpinner, Credentials.USER_PRIVATE_KEY);

            if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
                WifiConfiguration config = mAccessPoint.getConfig();
                setSelection(mEapMethodSpinner, config.eap.value());
                setSelection(mPhase2Spinner, config.phase2.value());
                setCertificate(mEapCaCertSpinner, Credentials.CA_CERTIFICATE,
                        config.ca_cert.value());
                setCertificate(mEapUserCertSpinner, Credentials.USER_PRIVATE_KEY,
                        config.private_key.value());
                mEapIdentityView.setText(config.identity.value());
                mEapAnonymousView.setText(config.anonymous_identity.value());
            }
        }
    }
    
    private void showNetworkSetupFields() {
        mView.findViewById(R.id.setup_fields).setVisibility(View.VISIBLE);

        if (mNetworkSetupSpinner == null) {
            mNetworkSetupSpinner = (Spinner) mView.findViewById(R.id.network_setup);
            //mNetworkSetupSpinner.setOnItemSelectedListener(this);
            //add to remove "PIN from access point"
            Context context = mConfigUi.getContext();
            String[] setUpArray = context.getResources().getStringArray(R.array.wifi_network_setup);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for(int i = 0; i < 4;i++){
                if(i != 2)adapter.add(setUpArray[i]);
            }
            mNetworkSetupSpinner.setAdapter(adapter);
            mNetworkSetupSpinner.setOnItemSelectedListener(this);
        }

        int pos = mNetworkSetupSpinner.getSelectedItemPosition();

        /* Show pin text input if needed */
        if (pos == WPS_KEYPAD) {
            mView.findViewById(R.id.wps_fields).setVisibility(View.VISIBLE);
        } else {
            mView.findViewById(R.id.wps_fields).setVisibility(View.GONE);
        }

        /* show/hide manual security fields appropriately */
        if ((pos == WPS_DISPLAY) || (pos == WPS_KEYPAD)
                || (pos == WPS_PBC) || (mAccessPointSecurity == AccessPoint.SECURITY_NONE && mAccessPoint.isOpenApWPSSupported())) {
            mView.findViewById(R.id.security_fields).setVisibility(View.GONE);
            Xlog.d(TAG,"showNetworkSetupFields security_fields set gone");
        } else {
            mView.findViewById(R.id.security_fields).setVisibility(View.VISIBLE);
        }

    }

    private void showIpConfigFields() {
        WifiConfiguration config = null;

        mView.findViewById(R.id.ip_fields).setVisibility(View.VISIBLE);

        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
            config = mAccessPoint.getConfig();
        }

        if (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) {
            mView.findViewById(R.id.staticip).setVisibility(View.VISIBLE);
            if (mIpAddressView == null) {
                mIpAddressView = (TextView) mView.findViewById(R.id.ipaddress);
                mIpAddressView.addTextChangedListener(this);
                mGatewayView = (TextView) mView.findViewById(R.id.gateway);
                mGatewayView.addTextChangedListener(this);
//MTK_OP01_PROTECT_START 
                if(Utils.isCmccLoad()){
                    mView.findViewById(R.id.prefix).setVisibility(View.GONE);
                    mView.findViewById(R.id.netmask).setVisibility(View.VISIBLE);
                    mNetworkNetmaskView = (TextView) mView.findViewById(
                            R.id.network_netmask);
                    mNetworkNetmaskView.addTextChangedListener(this);
                }else
//MTK_OP01_PROTECT_END
                {
                    mNetworkPrefixLengthView = (TextView) mView.findViewById(
                        R.id.network_prefix_length);
                    mNetworkPrefixLengthView.addTextChangedListener(this);
                }
                mDns1View = (TextView) mView.findViewById(R.id.dns1);
                mDns1View.addTextChangedListener(this);
                mDns2View = (TextView) mView.findViewById(R.id.dns2);
                mDns2View.addTextChangedListener(this);
            }
            if (config != null) {
                LinkProperties linkProperties = config.linkProperties;
                Iterator<LinkAddress> iterator = linkProperties.getLinkAddresses().iterator();
                if (iterator.hasNext()) {
                    LinkAddress linkAddress = iterator.next();
                    mIpAddressView.setText(linkAddress.getAddress().getHostAddress());
//MTK_OP01_PROTECT_START 
                    if(Utils.isCmccLoad()){
                        int len = linkAddress.getNetworkPrefixLength();
                        mNetworkNetmaskView.setText(getNetmask(len));
                    }else
//MTK_OP01_PROTECT_END
                    {
                        mNetworkPrefixLengthView.setText(Integer.toString(linkAddress
                            .getNetworkPrefixLength()));
                    }
                }

                for (RouteInfo route : linkProperties.getRoutes()) {
                    if (route.isDefaultRoute()) {
                        mGatewayView.setText(route.getGateway().getHostAddress());
                        break;
                    }
                }

                Iterator<InetAddress> dnsIterator = linkProperties.getDnses().iterator();
                if (dnsIterator.hasNext()) {
                    mDns1View.setText(dnsIterator.next().getHostAddress());
                }
                if (dnsIterator.hasNext()) {
                    mDns2View.setText(dnsIterator.next().getHostAddress());
                }
            }
        } else {
            mView.findViewById(R.id.staticip).setVisibility(View.GONE);
        }
    }

    private void showProxyFields() {
        WifiConfiguration config = null;

        mView.findViewById(R.id.proxy_settings_fields).setVisibility(View.VISIBLE);

        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
            config = mAccessPoint.getConfig();
        }

        if (mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) {
            mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.proxy_fields).setVisibility(View.VISIBLE);
            if (mProxyHostView == null) {
                mProxyHostView = (TextView) mView.findViewById(R.id.proxy_hostname);
                mProxyHostView.addTextChangedListener(this);
                mProxyPortView = (TextView) mView.findViewById(R.id.proxy_port);
                mProxyPortView.addTextChangedListener(this);
                mProxyExclusionListView = (TextView) mView.findViewById(R.id.proxy_exclusionlist);
                mProxyExclusionListView.addTextChangedListener(this);
//MTK_OP01_PROTECT_START 
                if(Utils.isCmccLoad()){
                    TextView proxyText = (TextView) mView.findViewById(R.id.proxy_exclusionlist_text);
                    proxyText.setText(R.string.proxy_exclusionlist_label_cmcc);
                }
//MTK_OP01_PROTECT_END
            }
            if (config != null) {
                ProxyProperties proxyProperties = config.linkProperties.getHttpProxy();
                if (proxyProperties != null) {
                    mProxyHostView.setText(proxyProperties.getHost());
                    mProxyPortView.setText(Integer.toString(proxyProperties.getPort()));
                    mProxyExclusionListView.setText(proxyProperties.getExclusionList());
                }
            }
        } else {
            mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.GONE);
            mView.findViewById(R.id.proxy_fields).setVisibility(View.GONE);
        }
    }



    private void loadCertificates(Spinner spinner, String prefix) {
        final Context context = mConfigUi.getContext();
        final String unspecified = context.getString(R.string.wifi_unspecified);

        String[] certs = KeyStore.getInstance().saw(prefix);
        if (certs == null || certs.length == 0) {
            certs = new String[] {unspecified};
        } else {
            final String[] array = new String[certs.length + 1];
            array[0] = unspecified;
            System.arraycopy(certs, 0, array, 1, certs.length);
            certs = array;
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, certs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setCertificate(Spinner spinner, String prefix, String cert) {
        prefix = KEYSTORE_SPACE + prefix;
        if (cert != null && cert.startsWith(prefix)) {
            setSelection(spinner, cert.substring(prefix.length()));
        }
    }

    private void setSelection(Spinner spinner, String value) {
        if (value != null) {
            @SuppressWarnings("unchecked")
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
            for (int i = adapter.getCount() - 1; i >= 0; --i) {
                if (value.equals(adapter.getItem(i))) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    public boolean isEdit() {
        return mEdit;
    }

    @Override
    public void afterTextChanged(Editable s) {
        enableSubmitIfAppropriate();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // work done in afterTextChanged
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // work done in afterTextChanged
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.show_password) {
            mPasswordView.setInputType(
                    InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                                InputType.TYPE_TEXT_VARIATION_PASSWORD));
        } else if (view.getId() == R.id.wifi_advanced_togglebox) {
            if (((CheckBox) view).isChecked()) {
                mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
            } else {
                mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.GONE);
            }
        }else if(view.getId() == R.id.hex_password){
            mHex = ((CheckBox) view).isChecked();
            enableSubmitIfAppropriate();
            Xlog.d(TAG,"onClick mHex is=" + mHex +",enableSubmitIfAppropriate");
        } else if(view.getId() == R.id.wep_key_type){
            /// M: verify password if wep key type is clicked
            enableSubmitIfAppropriate();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mSecuritySpinner) {
            mAccessPointSecurity = position;
            if (FeatureOption.MTK_WAPI_SUPPORT 
                    && (SystemProperties.get(WLAN_PROP_KEY, DEFAULT_WLAN_PROP).equals(WAPI))) {//only WPAI supported
                 /* Need to shift only when persist.sys.wlan=="wapi".
                  * Only need to shift if WAPI_SUPPORT=yes && persist.sys.wlan=="wapi"*/
               if(0 < mAccessPointSecurity) mAccessPointSecurity += AccessPoint.SECURITY_WAPI_PSK - AccessPoint.SECURITY_WEP;
            }else if(!AccessPoint.isWFATestSupported()){
                if(mAccessPointSecurity > AccessPoint.SECURITY_PSK) mAccessPointSecurity += 2;
            }else {
                if(mAccessPointSecurity > AccessPoint.SECURITY_WEP) mAccessPointSecurity += 1;
            }
            showSecurityFields();
        } else if (parent == mNetworkSetupSpinner) {
            showNetworkSetupFields();
        } else if (parent == mProxySettingsSpinner) {
            showProxyFields();
        }
//MTK_OP01_PROTECT_START 
         else if(parent.equals(mPrioritySpinner)){
            mPriority = position+1;
            Xlog.d(TAG, "change AP priority manually");
        } 
//MTK_OP01_PROTECT_END
         else if (parent == mIpSettingsSpinner) {
            showIpConfigFields();
        } else if (parent == mEapMethodSpinner && mPasswordView != null && mEapIdentityView !=null && mEapAnonymousView!=null){
            Xlog.d(TAG,"mEapMethodSpinner:"+mEapMethodSpinner.getSelectedItem());
            if ("AKA".equals((String) mEapMethodSpinner.getSelectedItem()) ||
                "SIM".equals((String) mEapMethodSpinner.getSelectedItem())) {
                Xlog.d(TAG,"select aka, empty identity & password");
                mEapIdentityView.setEnabled(false);
                mPasswordView.setEnabled(false);
                mEapAnonymousView.setEnabled(false);

                if(mPhase2Spinner != null){
                    mPhase2Spinner.setEnabled(false);
                }
                if(mEapCaCertSpinner != null){
                    mEapCaCertSpinner.setEnabled(false);
                }
                if(mEapUserCertSpinner != null){
                    mEapUserCertSpinner.setEnabled(false);
                }
            } else {
                Xlog.d(TAG,"not select aka");
                mEapIdentityView.setEnabled(true);
                mPasswordView.setEnabled(true);
                mEapAnonymousView.setEnabled(true);
                if(mPhase2Spinner != null){
                    mPhase2Spinner.setEnabled(true);
                }
                if(mEapCaCertSpinner != null){
                    mEapCaCertSpinner.setEnabled(true);
                }
                if(mEapUserCertSpinner != null){
                    mEapUserCertSpinner.setEnabled(true);
                }
            }
        }
        enableSubmitIfAppropriate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //
    }
    private void switchWlanSecuritySpinner(Spinner securitySpinner) {
        mSecuritySpinner = securitySpinner;
        ((Spinner) mView.findViewById(R.id.security)).setVisibility(View.GONE);
        ((Spinner) mView.findViewById(R.id.wapi_security)).setVisibility(View.GONE);
        ((Spinner) mView.findViewById(R.id.wpa_security)).setVisibility(View.GONE);
        ((Spinner) mView.findViewById(R.id.security_wfa)).setVisibility(View.GONE);
        ((Spinner) mView.findViewById(R.id.wpa_security_wfa)).setVisibility(View.GONE);
        
        securitySpinner.setVisibility(View.VISIBLE);
        securitySpinner.setOnItemSelectedListener(this);
    }
    private int getPrefixLength(String netmask){
        Xlog.d(TAG,"netmask is:"+netmask+";");
        if(netmask.length()==0||!isIpAddress(netmask)){
            return 24;
        }
        int start=0;
        int end=0;
        int len=0;
        try {
            for(int i=0;i<4;i++){
                end = netmask.indexOf(".",start+1);
                if(i==3){
                    end=netmask.length();
                }
                String value = netmask.substring(start,end);
                int intValue = Integer.parseInt(value);
                start=end+1;
                String binaryValue = Integer.toBinaryString(intValue);
                for(int j=0;j<binaryValue.length();j++){
                    if(binaryValue.charAt(j)=='1'){
                        len++;
                    }
                }
            }
        } catch (NumberFormatException e) {
            Xlog.d(TAG,"NumberFormatException"+e+";");
            return 24;
        }
        Xlog.d(TAG,"len is:"+len+";");
        return len;
    }
    private String getNetmask(int prefix){
        String netmask="";
        for(int i=0;i<4;i++){
            if(prefix>8){
                netmask+="255";
                prefix-=8;
            }else{
                int value = (255<<(8-prefix)) & 0xFF;
                prefix=0;
                netmask+=String.valueOf(value);
            }
            if(i<3){
                netmask+=".";
            }
        }
        Xlog.d(TAG,"netmask = "+netmask+";");
        return netmask;
    }

    private boolean isIpAddress(String value) {
        if(value==null){
            return false;
        }
        int start = 0;
        int end = value.indexOf('.');
        int numBlocks = 0;
	
        //IP ends with '.' is invalid
        if(value.startsWith(".") || value.endsWith(".")){
        	return false;
        }
        
        while (start < value.length()) {
            
            if (end == -1) {
                end = value.length();
            }

            try {
                int block = Integer.parseInt(value.substring(start, end));
                if ((block > 255) || (block < 0)) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
            
            numBlocks++;
            
            start = end + 1;
            end = value.indexOf('.', start);
        }
        
        return numBlocks == 4;
    }
    public void closeSpinnerDialog(){
        if(mSecuritySpinner!=null && mSecuritySpinner.isPopupShowing()){
            mSecuritySpinner.dismissPopup();
        }else if(mEapMethodSpinner!=null && mEapMethodSpinner.isPopupShowing()){
            mEapMethodSpinner.dismissPopup();
        }else if(mEapCaCertSpinner!=null && mEapCaCertSpinner.isPopupShowing()){
            mEapCaCertSpinner.dismissPopup();
        }else if(mPhase2Spinner!=null && mPhase2Spinner.isPopupShowing()){
            mPhase2Spinner.dismissPopup();
        }else if(mEapUserCertSpinner!=null && mEapUserCertSpinner.isPopupShowing()){
            mEapUserCertSpinner.dismissPopup();
        }else if(mNetworkSetupSpinner!=null && mNetworkSetupSpinner.isPopupShowing()){
            mNetworkSetupSpinner.dismissPopup();
        }else if(mIpSettingsSpinner!=null && mIpSettingsSpinner.isPopupShowing()){
            mIpSettingsSpinner.dismissPopup();
        }else if(mProxySettingsSpinner!=null && mProxySettingsSpinner.isPopupShowing()){
            mProxySettingsSpinner.dismissPopup();
        }else if(mSimSlot!=null && mSimSlot.isPopupShowing()){
            mSimSlot.dismissPopup();
        }else if(mWapiAsCert!=null && mWapiAsCert.isPopupShowing()){
            mWapiAsCert.dismissPopup();
        }else if(mWapiClientCert!=null && mWapiClientCert.isPopupShowing()){
            mWapiClientCert.dismissPopup();
        }else if(mPrioritySpinner!=null && mPrioritySpinner.isPopupShowing()){
            mPrioritySpinner.dismissPopup();
        }
    }
}
