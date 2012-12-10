/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.phone;

import java.util.ArrayList;
import java.util.List;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ThrottleManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings.System;
import android.provider.Telephony.SimInfo;
import android.provider.Telephony.SIMInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.provider.Telephony.SIMInfo;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.mediatek.settings.SimItem;
import com.mediatek.featureoption.FeatureOption;

import android.content.BroadcastReceiver;
import android.telephony.TelephonyManager;
import android.content.Context;

import android.database.ContentObserver;
import com.mediatek.settings.DefaultSimPreference;
import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.CellConnService.CellConnMgr;
/**
 * "Mobile network settings" screen.  This preference screen lets you
 * enable/disable mobile data, and control data roaming and other
 * network-specific mobile data features.  It's used on non-voice-capable
 * tablets as well as regular phone devices.
 *
 * Note that this PreferenceActivity is part of the phone app, even though
 * you reach it from the "Wireless & Networks" section of the main
 * Settings app.  It's not part of the "Call settings" hierarchy that's
 * available from the Phone app (see CallFeaturesSetting for that.)
 *
 * TODO: Rename this to be "NetworkSettings.java" to be more clear.
 * (But be careful in case the Settings app has any hardwired references
 * to this class name...)
 */
public class Settings extends PreferenceActivity implements DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener, Preference.OnPreferenceChangeListener{

    public static final int WCDMA_CARD_SLOT = 0;
    
    // debug data
    private static final String LOG_TAG = "NetworkSettings";
    private static final boolean DBG = true;
    public static final int REQUEST_CODE_EXIT_ECM = 17;

    //String keys for preference lookup
    private static final String BUTTON_DATA_ENABLED_KEY = "button_data_enabled_key";
    private static final String BUTTON_DATA_USAGE_KEY = "button_data_usage_key";
    private static final String BUTTON_PREFERED_NETWORK_MODE = "preferred_network_mode_key";
    private static final String BUTTON_ROAMING_KEY = "button_roaming_key";
    private static final String BUTTON_CDMA_LTE_DATA_SERVICE_KEY = "cdma_lte_data_service_key";

    private static final String BUTTON_GSM_UMTS_OPTIONS = "gsm_umts_options_key";
    private static final String BUTTON_CDMA_OPTIONS = "cdma_options_key";
    private static final String BUTTON_APN = "button_apn_key";
    private static final String BUTTON_CARRIER_SEL = "button_carrier_sel_key";
    
    private static final String BUTTON_3G_SERVICE = "button_3g_service_key";
    private static final String BUTTON_PLMN_LIST = "button_plmn_key";
    private static final String KEY_DATA_CONN = "data_connection_setting";

    /* Orange customization begin */
    private static final String BUTTON_2G_ONLY = "button_prefer_2g_key";
    /* Orange customization end */

    static final int preferredNetworkMode = Phone.PREFERRED_NT_MODE;

    //Information about logical "up" Activity
    private static final String UP_ACTIVITY_PACKAGE = "com.android.settings";
    private static final String UP_ACTIVITY_CLASS =
            "com.android.settings.Settings$WirelessSettingsActivity";

    //UI objects
    //For current platform,mButtonPreferredNetworkMode is used for RAT selection
    private ListPreference mButtonPreferredNetworkMode;
    private Preference mPreferredNetworkMode;
    private CheckBoxPreference mButtonDataRoam;
    private CheckBoxPreference mButtonDataEnabled;
    private Preference mLteDataServicePref;
    private Preference mPreference3GSwitch = null;
    private Preference mPLMNPreference = null;
    ///M: add for data conn feature @{
    private DefaultSimPreference mDataConnPref = null;
    private static String OPERATOR = SystemProperties.get("ro.operator.optr");
    private CellConnMgr mCellConnMgr;
    ///@}
    
    /* Orange customization begin */
    private CheckBoxPreference mButtonPreferredGSMOnly = null;
    private AlertDialog mAlertDlg = null;
    /* Orange customization end */

    private Preference mButtonDataUsage;
    private DataUsageListener mDataUsageListener;
    private static final String iface = "rmnet0"; //TODO: this will go away
    private GeminiPhone mGeminiPhone;
    private PhoneInterfaceManager phoneMgr = null;
    private Phone mPhone;
    private MyHandler mHandler;
    private boolean mOkClicked;
    private static final int SIM_CARD_1 = 0;
    private static final int SIM_CARD_2 = 1;
    private static final int SIM_CARD_SIGNAL = 2;
        
    private int mSimId;
    private PreferenceScreen mApnPref;
    private PreferenceScreen mCarrierSelPref;
    
    private boolean isOnlyOneSim = false;
    private PreCheckForRunning preCfr = null;
    private ProgressDialog pd = null;

    long simIds[] = new long[1];
    //GsmUmts options and Cdma options
    GsmUmtsOptions mGsmUmtsOptions;
    CdmaOptions mCdmaOptions;

    public static final int MODEM_MASK_GPRS = 0x01;
    public static final int MODEM_MASK_EDGE = 0x02;
    public static final int MODEM_MASK_WCDMA = 0x04;
    public static final int MODEM_MASK_TDSCDMA = 0x08;
    public static final int MODEM_MASK_HSDPA = 0x10;
    public static final int MODEM_MASK_HSUPA = 0x20;

    private TelephonyManager mTelephonyManager;

    private static final int DIALOG_GPRS_SWITCH_CONFIRM = 1;
    private int mDataSwitchMsgIndex = -1;
    private long mSelectGprsIndex = -1;
    private ITelephony mTelephony;
    private int[] mDataSwitchMsgStr = {
            R.string.gemini_3g_disable_warning_case0,
            R.string.gemini_3g_disable_warning_case1,
            R.string.gemini_3g_disable_warning_case2
            };

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            log("onCallStateChanged ans state is "+state);
            switch(state){
            case TelephonyManager.CALL_STATE_IDLE:
                setScreenEnabled();
                break;
            default:
                break;
            }
        }
    };

    private boolean mAirplaneModeEnabled = false;
    private int mDualSimMode = -1;
    private IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); //Added by vend_am00015 2010-06-07
            if(action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                mAirplaneModeEnabled = intent.getBooleanExtra("state", false);
                setScreenEnabled();
            } else if (action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED) && isChangeData) {
                Log.d(LOG_TAG, "catch data change!");
                Phone.DataState state = getMobileDataState(intent);
                String apnTypeList = intent.getStringExtra(Phone.DATA_APN_TYPE_KEY);
                if ((Phone.APN_TYPE_DEFAULT.equals(apnTypeList) && state == Phone.DataState.CONNECTED) 
                    || (state == Phone.DataState.DISCONNECTED)) {
                    mH.removeMessages(DATA_STATE_CHANGE_TIMEOUT);
                    if (pd != null && pd.isShowing()) {
                        try {
                            pd.dismiss();
                        } catch (Exception e) {
                            Log.d(LOG_TAG, e.toString());
                        }
                        pd = null;
                    }
                    isChangeData = false;
                    setDataConnPref();
                }
            }else if(action.equals(Intent.ACTION_DUAL_SIM_MODE_CHANGED)){
                mDualSimMode = intent.getIntExtra(Intent.EXTRA_DUAL_SIM_MODE, -1);
                setScreenEnabled();
            }else if(action.equals(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)){
                Log.d(LOG_TAG,"indicator state changed");
                setDataConnPref();
            }
        }
    };
    private Preference mClickedPreference;

    private ContentObserver mContentObserver;

    //This is a method implemented for DialogInterface.OnClickListener.
    //  Used to dismiss the dialogs when they come up.
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (CallSettings.isMultipleSim()) {
            if (DBG) log("getDataRoamingEnabledGemini" + " do nothing");
                //mGeminiPhone.setDataRoamingEnabledGemini(true, mSimId);
            } else {
            mPhone.setDataRoamingEnabled(true);
              }
            mOkClicked = true;
            mButtonDataRoam.setChecked(true);
        } else {
            // Reset the toggle
            mButtonDataRoam.setChecked(false);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        // Assuming that onClick gets called first
        if (!mOkClicked) {
            mButtonDataRoam.setChecked(false);
        }
    }

    /**
     * Invoked on each preference click in this hierarchy, overrides
     * PreferenceActivity's implementation.  Used to make sure we track the
     * preference click events.
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        /** TODO: Refactor and get rid of the if's using subclasses */
        
        if (preference == mButtonDataUsage)
        {
            if (CallSettings.isMultipleSim())
            {
                Intent intent = new Intent(this, MultipleSimActivity.class);
                //intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);
                intent.putExtra(MultipleSimActivity.initTitleName, preference.getTitle());
                  intent.putExtra(MultipleSimActivity.intentKey, "PreferenceScreen");
                  intent.putExtra(MultipleSimActivity.targetClassKey, "com.android.phone.DataUsage");
                //this.startActivity(intent);
                  preCfr.checkToRun(intent, this.mSimId, 302);
                return true;
            }else {
                return false;
            }
        }
        
        if (preference == this.mPLMNPreference) {
            if (CallSettings.isMultipleSim()) {
                Intent intent = new Intent(this, MultipleSimActivity.class);
                //intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);
                intent.putExtra(MultipleSimActivity.initTitleName, preference.getTitle());
                intent.putExtra(MultipleSimActivity.intentKey, "PreferenceScreen");
                intent.putExtra(MultipleSimActivity.targetClassKey, "com.android.phone.PLMNListPreference");
                //this.startActivity(intent);
                preCfr.checkToRun(intent, this.mSimId, 302);
                return true;
            }else {
                return false;
            }
        }
        
        if (preference == mPreferredNetworkMode)
        {
            CharSequence[] entries;
            CharSequence[] entriesValue; 
            Intent intent = new Intent(this, MultipleSimActivity.class);
            intent.putExtra(MultipleSimActivity.intentKey, "ListPreference");
            if((getBaseBand(WCDMA_CARD_SLOT) & MODEM_MASK_TDSCDMA) != 0){
                entries = getResources().getStringArray(R.array.gsm_umts_network_preferences_choices_cmcc);
                entriesValue = getResources().getStringArray(R.array.gsm_umts_network_preferences_values_cmcc);
            }else{
                entries = getResources().getStringArray(R.array.gsm_umts_network_preferences_choices);
                entriesValue = getResources().getStringArray(R.array.gsm_umts_network_preferences_values);
            }

            intent.putExtra(MultipleSimActivity.initArray, entries);
            intent.putExtra(MultipleSimActivity.initTitleName, preference.getTitle());
            intent.putExtra(MultipleSimActivity.LIST_TITLE, getResources().getString(R.string.gsm_umts_network_preferences_title));
            intent.putExtra(MultipleSimActivity.initFeatureName, "NETWORK_MODE");
            intent.putExtra(MultipleSimActivity.initSimId, simIds);
            intent.putExtra(MultipleSimActivity.initBaseKey, "preferred_network_mode_key@");
            intent.putExtra(MultipleSimActivity.initArrayValue, entriesValue);
            //this.startActivity(intent);
            preCfr.checkToRun(intent, this.mSimId, 302);
            return true;
        }
        
        if (mGsmUmtsOptions != null &&
                mGsmUmtsOptions.preferenceTreeClick(preference) == true) {
            return true;
        } else if (mCdmaOptions != null &&
                   mCdmaOptions.preferenceTreeClick(preference) == true) {
            if (Boolean.parseBoolean(
                    SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {

                mClickedPreference = preference;

                // In ECM mode launch ECM app dialog
                startActivityForResult(
                    new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null),
                    REQUEST_CODE_EXIT_ECM);
            }
            return true;
        } else if (preference == mButtonPreferredNetworkMode) {
            //displays the value taken from the Settings.System
            int settingsNetworkMode = android.provider.Settings.Secure.getInt(mPhone.getContext().
                    getContentResolver(), android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                    preferredNetworkMode);
            mButtonPreferredNetworkMode.setValue(Integer.toString(settingsNetworkMode));
            return true;
        } else if (preference == mButtonDataRoam) {
            if (DBG) log("onPreferenceTreeClick: preference == mButtonDataRoam.");

            //normally called on the toggle click
            if (mButtonDataRoam.isChecked()) {
                // First confirm with a warning dialog about charges
                mOkClicked = false;
                new AlertDialog.Builder(this).setMessage(
                        getResources().getString(R.string.roaming_warning))
                        .setTitle(android.R.string.dialog_alert_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this)
                        .show()
                        .setOnDismissListener(this);
            }
            else {
        if (CallSettings.isMultipleSim()) {
            //mGeminiPhone.setDataRoamingEnabledGemini(false, mSimId);
        } else {
            mPhone.setDataRoamingEnabled(false);
        }
            }
            return true;
        } else if (preference == mButtonDataEnabled) {
		/* Orange customization begin */
	    if (isOrangeSupport()) {
		    int resId = R.string.networksettings_tips_data_enabled;
		    //Data enable button old status, keep its status until user give a choice
		    boolean isCheckedBefore = !mButtonDataEnabled.isChecked();
		    if (DBG)log("Data enable button old status is "+isCheckedBefore);
		    if (isCheckedBefore) {
			    resId = R.string.networksettings_tips_data_disabled;
		    }
		    mButtonDataEnabled.setChecked(isCheckedBefore);
		    mAlertDlg = new AlertDialog.Builder(this).setMessage(
			getResources().getString(resId)).setTitle(
			android.R.string.dialog_alert_title).setIcon(
			android.R.drawable.ic_dialog_alert).setPositiveButton(android.R.string.yes, 
			new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					isChangeData = true;
					if (DBG)log("onPreferenceTreeClick: preference == mButtonDataEnabled.");
					ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					showProgressDialog();
					boolean tempCheckedStatus = mButtonDataEnabled.isChecked();
					cm.setMobileDataEnabled(!tempCheckedStatus);
					mButtonDataEnabled.setChecked(!tempCheckedStatus);
					mH.sendMessageDelayed(mH.obtainMessage(DATA_STATE_CHANGE_TIMEOUT), 30000);
				}
			}).setNegativeButton(android.R.string.no,null).show();
	    } else {
		    this.isChangeData = true;
		    if (DBG)log("onPreferenceTreeClick: preference == mButtonDataEnabled.");
		    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()){
            }else{
                this.showProgressDialog();
            }
		    cm.setMobileDataEnabled(mButtonDataEnabled.isChecked());
		    mH.sendMessageDelayed(mH.obtainMessage(DATA_STATE_CHANGE_TIMEOUT), 30000);
	    }
	    /* Orange customization end */
	    return true;
        } else if (preference == mLteDataServicePref) {
            String tmpl = android.provider.Settings.Secure.getString(getContentResolver(),
                        android.provider.Settings.Secure.SETUP_PREPAID_DATA_SERVICE_URL);
            if (!TextUtils.isEmpty(tmpl)) {
                String imsi = mTelephonyManager.getSubscriberId();
                if (imsi == null) {
                    imsi = "";
                }
                final String url = TextUtils.isEmpty(tmpl) ? null
                        : TextUtils.expandTemplate(tmpl, imsi).toString();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } else {
                android.util.Log.e(LOG_TAG, "Missing SETUP_PREPAID_DATA_SERVICE_URL");
            }
            return true;
        }
        if (CallSettings.isMultipleSim()) {
            Intent it = new Intent();
            it.setAction("android.intent.action.MAIN");
                if (preference == mApnPref) {
                        it.setClassName("com.android.phone",
                                "com.android.phone.MultipleSimActivity");
                        it.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);
                        it.putExtra(MultipleSimActivity.initTitleName, preference.getTitle());
                        it.putExtra(MultipleSimActivity.intentKey, "PreferenceScreen");
                        it.putExtra(MultipleSimActivity.targetClassKey, "com.android.settings.ApnSettings");
                        //startActivity(it);
                        preCfr.checkToRun(it, this.mSimId, 302);
                        return true;
                    } else if (preference == mCarrierSelPref) {
                        it.setClassName("com.android.phone",
                                "com.android.phone.MultipleSimActivity");
                        //it.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);
                        it.putExtra(MultipleSimActivity.initTitleName, preference.getTitle());
                        it.putExtra(MultipleSimActivity.intentKey, "PreferenceScreen");
                        it.putExtra(MultipleSimActivity.targetClassKey, "com.android.phone.NetworkSetting");
                        //startActivity(it);
                        preCfr.checkToRun(it, this.mSimId, 302);
                        return true;
                    }
        } else {
            // if the button is anything but the simple toggle preference,
            // we'll need to disable all preferences to reject all click
            // events until the sub-activity's UI comes up.
            preferenceScreen.setEnabled(false);
            // Let the intents be launched by the Preference manager
            return false;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.network_setting);
        SIMInfo info = SIMInfo.getSIMInfoBySlot(this, WCDMA_CARD_SLOT);
        simIds[0] = info != null ? info.mSimId : 0;

        mPhone = PhoneApp.getPhone();
        if (CallSettings.isMultipleSim())
        {
            mGeminiPhone = (GeminiPhone)mPhone;
        }
        mHandler = new MyHandler();
        phoneMgr = PhoneApp.getInstance().phoneMgr;
        mIntentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED); 
        mIntentFilter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
        if(FeatureOption.MTK_GEMINI_SUPPORT){
            mIntentFilter.addAction(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
        }
        ///M: add to receiver indicator intents@{
        if (isDataConnAvailable()){
            mIntentFilter.addAction(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
            mCellConnMgr = new CellConnMgr();
            mCellConnMgr.register(this);
        }
        ///@}
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        preCfr = new PreCheckForRunning(this);
        List<SIMInfo> list = SIMInfo.getInsertedSIMList(this);
        if (list.size() == 1) {
            this.isOnlyOneSim = true;
            this.mSimId = list.get(0).mSlot;
        }
        preCfr.byPass = !isOnlyOneSim;
        //get UI object references
        PreferenceScreen prefSet = getPreferenceScreen();
        //M: add data connection for gemini sim project
        mDataConnPref = (DefaultSimPreference) prefSet.findPreference(KEY_DATA_CONN);
        mDataConnPref.setOnPreferenceChangeListener(this);
        if (!isDataConnAvailable()){
            prefSet.removePreference(mDataConnPref);
        }
        //
        mButtonDataEnabled = (CheckBoxPreference) prefSet.findPreference(BUTTON_DATA_ENABLED_KEY);
        mButtonDataRoam = (CheckBoxPreference) prefSet.findPreference(BUTTON_ROAMING_KEY);
         if (CallSettings.isMultipleSim()) {
            prefSet.removePreference(mButtonDataEnabled);
            prefSet.removePreference(mButtonDataRoam);
        }
        mButtonPreferredNetworkMode = (ListPreference) prefSet.findPreference(
                BUTTON_PREFERED_NETWORK_MODE);
        mButtonDataUsage = prefSet.findPreference(BUTTON_DATA_USAGE_KEY);
        
        mPreference3GSwitch = prefSet.findPreference(BUTTON_3G_SERVICE);
        mPLMNPreference = prefSet.findPreference(BUTTON_PLMN_LIST);

        if(!FeatureOption.MTK_PLMN_PREFER_SUPPORT){
            prefSet.removePreference(mPLMNPreference);
        }

        mLteDataServicePref = prefSet.findPreference(BUTTON_CDMA_LTE_DATA_SERVICE_KEY);
        mPreferredNetworkMode = prefSet.findPreference("button_network_mode_ex_key");

        boolean isLteOnCdma = mPhone.getLteOnCdmaMode() == Phone.LTE_ON_CDMA_TRUE;
        if (getResources().getBoolean(R.bool.world_phone)) {
            // set the listener for the mButtonPreferredNetworkMode list preference so we can issue
            // change Preferred Network Mode.
            mButtonPreferredNetworkMode.setOnPreferenceChangeListener(this);

            //Get the networkMode from Settings.System and displays it
            int settingsNetworkMode = android.provider.Settings.Secure.getInt(mPhone.getContext().
                    getContentResolver(),android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                    preferredNetworkMode);
            //mButtonPreferredNetworkMode.setValue(Integer.toString(settingsNetworkMode));
            mCdmaOptions = new CdmaOptions(this, prefSet, mPhone);
            mGsmUmtsOptions = new GsmUmtsOptions(this, prefSet);
        } else {
            if (!isLteOnCdma) {
                prefSet.removePreference(mButtonPreferredNetworkMode);
            }
            int phoneType = mPhone.getPhoneType();
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                prefSet.removePreference(mPreferredNetworkMode);
                mCdmaOptions = new CdmaOptions(this, prefSet, mPhone);
                mApnPref = (PreferenceScreen) prefSet.findPreference(BUTTON_APN);
                if (isLteOnCdma) {
                    mButtonPreferredNetworkMode.setOnPreferenceChangeListener(this);
                    mButtonPreferredNetworkMode.setEntries(
                            R.array.preferred_network_mode_choices_lte);
                    mButtonPreferredNetworkMode.setEntryValues(
                            R.array.preferred_network_mode_values_lte);
                    int settingsNetworkMode = android.provider.Settings.Secure.getInt(
                            mPhone.getContext().getContentResolver(),
                            android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                            preferredNetworkMode);
                    mButtonPreferredNetworkMode.setValue(
                            Integer.toString(settingsNetworkMode));
                }
                if(!PhoneUtils.isSupportFeature("3G_SWITCH")){
                    if(mPreference3GSwitch != null){
                        prefSet.removePreference(mPreference3GSwitch);
                        mPreference3GSwitch = null;
                    }
                }
                mCarrierSelPref = (PreferenceScreen) prefSet.findPreference(BUTTON_CARRIER_SEL);
            } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            	mGsmUmtsOptions = new GsmUmtsOptions(this, prefSet);
				mApnPref = (PreferenceScreen) prefSet.findPreference(BUTTON_APN);
                
				/* Orange customization begin */
				mButtonPreferredGSMOnly = (CheckBoxPreference) prefSet.findPreference(BUTTON_2G_ONLY);
				/* Orange customization end */

                mButtonPreferredNetworkMode = (ListPreference)prefSet.findPreference("gsm_umts_preferred_network_mode_key");
                
              	//Get the networkMode from Settings.System and displays it
                int settingsNetworkMode = android.provider.Settings.Secure.getInt(mPhone.getContext().
                        getContentResolver(),android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                        preferredNetworkMode);
                if (settingsNetworkMode > 2) {
                    settingsNetworkMode = preferredNetworkMode;
                    android.provider.Settings.Secure.putInt(mPhone.getContext().getContentResolver(),
                            android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                            settingsNetworkMode);
                }
                mButtonPreferredNetworkMode.setValue(Integer.toString(settingsNetworkMode));
   
                if("OP01".equals(PhoneUtils.getOptrProperties())
                    || "OP03".equals(PhoneUtils.getOptrProperties())
                    || !isSupport3G(Settings.WCDMA_CARD_SLOT)
                    || PhoneUtils.isSupportFeature("3G_SWITCH")){
                    prefSet.removePreference(mPreferredNetworkMode);
                    prefSet.removePreference(mButtonPreferredNetworkMode);
                }else if("OP02".equals(PhoneUtils.getOptrProperties())){
                    prefSet.removePreference(mPreferredNetworkMode);
                }else{
                    if(isUsedGeneralPreference()){
                        prefSet.removePreference(mButtonPreferredNetworkMode);
                    }else{
                        prefSet.removePreference(mPreferredNetworkMode);
                    }
                }
				
                if("OP02".equals(PhoneUtils.getOptrProperties())
                    || !PhoneUtils.isSupportFeature("3G_SWITCH")
                    || !CallSettings.isMultipleSim()){
                    prefSet.removePreference(mPreference3GSwitch);
                    mPreference3GSwitch = null;
                }

                if (!"OP03".equals(PhoneUtils.getOptrProperties())){
                    prefSet.removePreference(mButtonPreferredGSMOnly);
                    mButtonPreferredGSMOnly = null;
                }

                if (mButtonPreferredNetworkMode != null)
                {
                    mButtonPreferredNetworkMode.setOnPreferenceChangeListener(this);
                    if((getBaseBand(WCDMA_CARD_SLOT) & MODEM_MASK_TDSCDMA) != 0){
                        mButtonPreferredNetworkMode.setEntries(
                            getResources().getStringArray(R.array.gsm_umts_network_preferences_choices_cmcc));	
                        mButtonPreferredNetworkMode.setEntryValues(
                            getResources().getStringArray(R.array.gsm_umts_network_preferences_values_cmcc));	
                    }
                }
                mCarrierSelPref = (PreferenceScreen) prefSet.findPreference(BUTTON_CARRIER_SEL);
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
        }

        final boolean missingDataServiceUrl = TextUtils.isEmpty(
        	android.provider.Settings.Secure.getString(getContentResolver(),
        	android.provider.Settings.Secure.SETUP_PREPAID_DATA_SERVICE_URL));

        if (!isLteOnCdma || missingDataServiceUrl) {
            prefSet.removePreference(mLteDataServicePref);
        } else {
            android.util.Log.d(LOG_TAG, "keep ltePref");
        }


        ThrottleManager tm = (ThrottleManager) getSystemService(Context.THROTTLE_SERVICE);
        mDataUsageListener = new DataUsageListener(this, mButtonDataUsage, prefSet);

        if (!CallSettings.isMultipleSim()) {
		mContentObserver = new ContentObserver(mHandler){
		    @Override
		    public void onChange(boolean selfChange) {
		        super.onChange(selfChange);
		        int state = android.provider.Settings.Secure.getInt(mPhone.getContext().getContentResolver(),
		                    android.provider.Settings.Secure.MOBILE_DATA,
		                    0);                
		        mButtonDataEnabled.setChecked(state != 0);
		    }
		};
		
		this.getContentResolver().registerContentObserver(
		        android.provider.Settings.Secure.getUriFor(android.provider.Settings.Secure.MOBILE_DATA),
		        false, mContentObserver);
        }
        registerReceiver(mReceiver, mIntentFilter);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // upon resumption from the sub-activity, make sure we re-enable the
        // preferences.
        mAirplaneModeEnabled = android.provider.Settings.System.getInt(getContentResolver(),
            android.provider.Settings.System.AIRPLANE_MODE_ON, -1)==1;
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        mButtonDataEnabled.setChecked(cm.getMobileDataEnabled());

        // Set UI state in onResume because a user could go home, launch some
        // app to change this setting's backend, and re-launch this settings app
        // and the UI state would be inconsistent with actual state
    if (CallSettings.isMultipleSim()) {
    } else {
        mButtonDataRoam.setChecked(mPhone.getDataRoamingEnabled());

        if (getPreferenceScreen().findPreference(BUTTON_PREFERED_NETWORK_MODE) != null)  {
            mPhone.getPreferredNetworkType(mHandler.obtainMessage(
                    MyHandler.MESSAGE_GET_PREFERRED_NETWORK_TYPE));
        }
    }
        mDataUsageListener.resume();
       
        //if the phone not idle state or airplane mode, then disable the preferenceScreen
        
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
        mDualSimMode = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.DUAL_SIM_MODE_SETTING, -1);
        Log.d(LOG_TAG, "Settings.onResume(), mDualSimMode="+mDualSimMode);
        }
        
        if (mButtonPreferredNetworkMode != null)
        {
            int settingsNetworkMode = android.provider.Settings.Secure.getInt(
                    mPhone.getContext().getContentResolver(),
                    android.provider.Settings.Secure.PREFERRED_NETWORK_MODE, preferredNetworkMode);
            
            Log.d(LOG_TAG, "mButtonPreferredNetworkMode != null and the settingsNetworkMode = " + settingsNetworkMode);
            UpdatePreferredNetworkModeSummary(settingsNetworkMode);
            
          //There is only one sim inserted
            SIMInfo info = SIMInfo.getSIMInfoBySlot(this, WCDMA_CARD_SLOT);
            if (info == null)
            {
                mButtonPreferredNetworkMode.setEnabled(false);
            }
        }
        
        if (mPreferredNetworkMode != null)
        {
            int settingsNetworkMode = android.provider.Settings.Secure.getInt(
                    mPhone.getContext().getContentResolver(),
                    android.provider.Settings.Secure.PREFERRED_NETWORK_MODE, preferredNetworkMode);
            Log.d(LOG_TAG, "mPreferredNetworkMode != null and the settingsNetworkMode = " + settingsNetworkMode);
            
            if (settingsNetworkMode < 0 || settingsNetworkMode > 2) {
                settingsNetworkMode = 0;
                UpdateGeneralPreferredNetworkModeSummary(settingsNetworkMode);
            }
            
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            //int settingsNetworkMode = Integer.valueOf(sp.getString("preferred_network_mode_key", "-1"));
            SharedPreferences.Editor edit = sp.edit();
            edit.putString("preferred_network_mode_key", String.valueOf(settingsNetworkMode));
            edit.commit();
            //UpdateGeneralPreferredNetworkModeSummary(settingsNetworkMode);
        }
        ///M: add for data connection gemini and op01 only
        setDataConnPref();
        //Please make sure this is the last line!!
        setScreenEnabled();
    }
    private void setDataConnPref(){
        Log.d(LOG_TAG,"setDataConnPref");
        if (isDataConnAvailable() &&
            mDataConnPref != null){
            mDataConnPref.SetCellConnMgr(mCellConnMgr);
            Log.d(LOG_TAG,"setDataConnPref---2");
            long dataconnectionID = android.provider.Settings.System.getLong(getContentResolver(), 
                                    android.provider.Settings.System.GPRS_CONNECTION_SIM_SETTING,
                                    android.provider.Settings.System.DEFAULT_SIM_NOT_SET);
            List<SimItem> mSimItemListGprs = new ArrayList<SimItem>();
            List<SIMInfo> simList = SIMInfo.getInsertedSIMList(this);
            if (simList.size() > 1){
                //check whether need to order the sim card list
                SIMInfo siminfo1 = simList.get(Phone.GEMINI_SIM_1);
                SIMInfo siminfo2 = simList.get(Phone.GEMINI_SIM_2);
                if(siminfo1.mSlot > siminfo2.mSlot){
                 simList.clear();
                 simList.add(siminfo2);
                 simList.add(siminfo1);
                }
            }
            mSimItemListGprs.clear();
            SimItem simitem;
            int state;
            int k = 0;
            TelephonyManagerEx mTelephonyManagerEx = TelephonyManagerEx.getDefault();
            for (SIMInfo siminfo: simList) {
                if (siminfo != null) {
                    simitem = new SimItem(siminfo);
                    state = mTelephonyManagerEx.getSimIndicatorStateGemini(siminfo.mSlot);
                    simitem.mState = state;
                    Log.d(LOG_TAG, "state="+simitem.mState);
                    if (siminfo.mSimId == dataconnectionID) {
                        mDataConnPref.setInitValue(k);
                        mDataConnPref.setSummary(siminfo.mDisplayName);
                    }
                    mSimItemListGprs.add(simitem);
                }
                k++;
            }
            if(dataconnectionID == android.provider.Settings.System.GPRS_CONNECTION_SIM_SETTING_NEVER) {
                mDataConnPref.setInitValue(simList.size());
                mDataConnPref.setSummary(R.string.service_3g_off);
            }
            simitem = new SimItem (null);
            mSimItemListGprs.add(simitem);  
            Log.d(LOG_TAG,"mSimItemListGprs="+mSimItemListGprs.size());
            mDataConnPref.setInitData(mSimItemListGprs);   
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        mDataUsageListener.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isDataConnAvailable() && 
            mCellConnMgr != null){
            mCellConnMgr.unregister();
        }
        unregisterReceiver(mReceiver);
        if (preCfr != null) {
            preCfr.deRegister();
        }
        
        if ((pd != null) && (pd.isShowing())) {
            try {
                pd.dismiss();
            } catch (Exception e) {
                Log.d(LOG_TAG, e.toString());
            }
        }
        
        if (pd != null) {
            pd = null;
        }
	 /* Orange customization begin */
	if (null != this.mAlertDlg) {
		if (this.mAlertDlg.isShowing()) {
			this.mAlertDlg.dismiss();
		}
		this.mAlertDlg = null;
	}
	/* Orange customization begin */
    }
    

    private void showProgressDialog() {
        // TODO Auto-generated method stub
        pd = new ProgressDialog(this);
        pd.setMessage(getText(R.string.updating_settings));
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.show();
    }

    /**
     * Implemented to support onPreferenceChangeListener to look for preference
     * changes specifically on CLIR.
     *
     * @param preference is the preference to be changed, should be mButtonCLIR.
     * @param objValue should be the value of the selection, NOT its localized
     * display value.
     */
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mButtonPreferredNetworkMode) {
            //NOTE onPreferenceChange seems to be called even if there is no change
            //Check if the button value is changed from the System.Setting
            mButtonPreferredNetworkMode.setValue((String) objValue);
            int buttonNetworkMode;
            buttonNetworkMode = Integer.valueOf((String) objValue).intValue();
            int settingsNetworkMode = android.provider.Settings.Secure.getInt(
                    mPhone.getContext().getContentResolver(),
                    android.provider.Settings.Secure.PREFERRED_NETWORK_MODE, preferredNetworkMode);
            if (buttonNetworkMode != settingsNetworkMode) {
                showProgressDialog();
                int modemNetworkMode;
                switch(buttonNetworkMode) {
                    case Phone.NT_MODE_GLOBAL:
                        modemNetworkMode = Phone.NT_MODE_GLOBAL;
                        break;
                    case Phone.NT_MODE_EVDO_NO_CDMA:
                        modemNetworkMode = Phone.NT_MODE_EVDO_NO_CDMA;
                        break;
                    case Phone.NT_MODE_CDMA_NO_EVDO:
                        modemNetworkMode = Phone.NT_MODE_CDMA_NO_EVDO;
                        break;
                    case Phone.NT_MODE_CDMA:
                        modemNetworkMode = Phone.NT_MODE_CDMA;
                        break;
                    case Phone.NT_MODE_GSM_UMTS:
                        modemNetworkMode = Phone.NT_MODE_GSM_UMTS;
                        break;
                    case Phone.NT_MODE_WCDMA_ONLY:
                        modemNetworkMode = Phone.NT_MODE_WCDMA_ONLY;
                        break;
                    case Phone.NT_MODE_GSM_ONLY:
                        modemNetworkMode = Phone.NT_MODE_GSM_ONLY;
                        break;
                    case Phone.NT_MODE_WCDMA_PREF:
                        modemNetworkMode = Phone.NT_MODE_WCDMA_PREF;
                        break;
                    default:
			modemNetworkMode = Phone.NT_MODE_GSM_UMTS;
			break;
                }

                // If button has no valid selection && setting is LTE ONLY
                // mode, let the setting stay in LTE ONLY mode. UI is not
                // supported but LTE ONLY mode could be used in testing.
                if ((modemNetworkMode == Phone.PREFERRED_NT_MODE) &&
                    (settingsNetworkMode == Phone.NT_MODE_LTE_ONLY)) {
                    return true;
                }

                UpdatePreferredNetworkModeSummary(buttonNetworkMode);

                android.provider.Settings.Secure.putInt(mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                        buttonNetworkMode );
                //Set the modem network mode
        if (CallSettings.isMultipleSim()) {
                    mGeminiPhone.setPreferredNetworkTypeGemini(modemNetworkMode, mHandler
                            .obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE), mSimId);
        } else {
                mPhone.setPreferredNetworkType(modemNetworkMode, mHandler
                        .obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
                }
            }
        }else if (preference == mDataConnPref){
            long simId = ((Long) objValue).longValue();
            Log.d(LOG_TAG,"under click simId=" + simId);

            if (simId == 0) {
                switchGprsDefautlSIM(0);
                return true;
            }

            SIMInfo simInfo = getSimInfo(simId);
            if (simInfo == null) {
                return false;
            }
            boolean isInRoaming = mTelephonyManager.isNetworkRoamingGemini(simInfo.mSlot);
            mDataSwitchMsgIndex = -1;

            int slot3G = phoneMgr.get3GCapabilitySIM();
            boolean is3gOff = slot3G == -1;
            if(isInRoaming) {
                boolean isRoamingDataAllowed = simInfo.mDataRoaming == SimInfo.DATA_ROAMING_ENABLE;
                if(isRoamingDataAllowed) {
                    if (simInfo.mSlot != slot3G && FeatureOption.MTK_GEMINI_3G_SWITCH) {
                        mDataSwitchMsgIndex = 1;
                    }
                } else {
                    if (is3gOff || !is3gOff && simInfo.mSlot == slot3G || 
                        !FeatureOption.MTK_GEMINI_3G_SWITCH) {
                        mDataSwitchMsgIndex = 0;
                    } else if(simInfo.mSlot != slot3G && FeatureOption.MTK_GEMINI_3G_SWITCH)
                        mDataSwitchMsgIndex = 2;
                }
            } else {
                if (simInfo.mSlot != slot3G && FeatureOption.MTK_GEMINI_3G_SWITCH) {
                    mDataSwitchMsgIndex = 1;
                }
            }
            log("slot3G=" + slot3G);
            log("simInfo.mSlot=" + simInfo.mSlot);
            if(mDataSwitchMsgIndex == -1) {
                switchGprsDefautlSIM(simId);
            } else {
                mSelectGprsIndex = simId;
                log("mSelectGprsIndex=" + mSelectGprsIndex);
                showDialog(DIALOG_GPRS_SWITCH_CONFIRM);
            }
        }
        // always let the preference setting proceed.
        return true;
    }
    /**
     * switch data connection default SIM
     * @param value: sim id of the new default SIM
     */
    private void switchGprsDefautlSIM(long simid) {
        if(simid <0) {
            Log.d(LOG_TAG,"value="+simid+" is an exceptions");
            return;
        }
        long GprsValue = android.provider.Settings.System.getLong(getContentResolver(),
                         android.provider.Settings.System.GPRS_CONNECTION_SIM_SETTING,
                         android.provider.Settings.System.DEFAULT_SIM_NOT_SET);
        Log.d(LOG_TAG,"Current GprsValue="+GprsValue+" and target value="+simid);
        if(simid == GprsValue) {
            return;
        }        
        Intent intent = new Intent(Intent.ACTION_DATA_DEFAULT_SIM_CHANGED);
        intent.putExtra("simid", simid);
        sendBroadcast(intent);
        showProgressDialog();
        mH.sendMessageDelayed(mH.obtainMessage(DATA_STATE_CHANGE_TIMEOUT), 30000);
        isChangeData = true;
    }
    private class MyHandler extends Handler {

        private static final int MESSAGE_GET_PREFERRED_NETWORK_TYPE = 0;
        private static final int MESSAGE_SET_PREFERRED_NETWORK_TYPE = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_GET_PREFERRED_NETWORK_TYPE:
                    handleGetPreferredNetworkTypeResponse(msg);
                    break;

                case MESSAGE_SET_PREFERRED_NETWORK_TYPE:
                    handleSetPreferredNetworkTypeResponse(msg);
                    break;
            }
        }

        private void handleGetPreferredNetworkTypeResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;

            if (ar.exception == null) {
                int modemNetworkMode = ((int[])ar.result)[0];

                if (DBG) {
                    log ("handleGetPreferredNetworkTypeResponse: modemNetworkMode = " +
                            modemNetworkMode);
                }

                int settingsNetworkMode = android.provider.Settings.Secure.getInt(
                        mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                        preferredNetworkMode);

                if (DBG) {
                    log("handleGetPreferredNetworkTypeReponse: settingsNetworkMode = " +
                            settingsNetworkMode);
                }

                //check that modemNetworkMode is from an accepted value
                if (modemNetworkMode == Phone.NT_MODE_WCDMA_PREF ||
                        modemNetworkMode == Phone.NT_MODE_GSM_ONLY ||
                        modemNetworkMode == Phone.NT_MODE_WCDMA_ONLY ||
                        modemNetworkMode == Phone.NT_MODE_GSM_UMTS ||
                        modemNetworkMode == Phone.NT_MODE_CDMA ||
                        modemNetworkMode == Phone.NT_MODE_CDMA_NO_EVDO ||
                        modemNetworkMode == Phone.NT_MODE_EVDO_NO_CDMA ||
                        modemNetworkMode == Phone.NT_MODE_GLOBAL ) {
                    if (DBG) {
                        log("handleGetPreferredNetworkTypeResponse: if 1: modemNetworkMode = " +
                                modemNetworkMode);
                    }

                    //check changes in modemNetworkMode and updates settingsNetworkMode
                    if (modemNetworkMode != settingsNetworkMode) {
                        if (DBG) {
                            log("handleGetPreferredNetworkTypeResponse: if 2: " +
                                    "modemNetworkMode != settingsNetworkMode");
                        }

                        settingsNetworkMode = modemNetworkMode;

                        if (DBG) { log("handleGetPreferredNetworkTypeResponse: if 2: " +
                                "settingsNetworkMode = " + settingsNetworkMode);
                        }

                        //changes the Settings.System accordingly to modemNetworkMode
                        android.provider.Settings.Secure.putInt(
                                mPhone.getContext().getContentResolver(),
                                android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                                settingsNetworkMode );
                    }

					if(modemNetworkMode == Phone.NT_MODE_GSM_UMTS){
		        		modemNetworkMode = Phone.NT_MODE_WCDMA_PREF;
		        		settingsNetworkMode = Phone.NT_MODE_WCDMA_PREF;
		    		}

                    UpdatePreferredNetworkModeSummary(modemNetworkMode);
                    // changes the mButtonPreferredNetworkMode accordingly to modemNetworkMode
                    mButtonPreferredNetworkMode.setValue(Integer.toString(modemNetworkMode));
                } else if (modemNetworkMode == Phone.NT_MODE_LTE_ONLY) {
                    // LTE Only mode not yet supported on UI, but could be used for testing
                    if (DBG) log("handleGetPreferredNetworkTypeResponse: lte only: no action");
                } else {
                    if (DBG) log("handleGetPreferredNetworkTypeResponse: else: reset to default");
                    resetNetworkModeToDefault();
                }
            }
        }

        private void handleSetPreferredNetworkTypeResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            if (ar.exception == null) {
                int networkMode = Integer.valueOf(
                        mButtonPreferredNetworkMode.getValue()).intValue();
		
                android.provider.Settings.Secure.putInt(mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                        networkMode );
            } else {
            if (CallSettings.isMultipleSim()) {
            mGeminiPhone.getPreferredNetworkTypeGemini(obtainMessage(MESSAGE_GET_PREFERRED_NETWORK_TYPE), mSimId);
            } else {
                mPhone.getPreferredNetworkType(obtainMessage(MESSAGE_GET_PREFERRED_NETWORK_TYPE));
            }
        }
        }

        private void resetNetworkModeToDefault() {
            //set the mButtonPreferredNetworkMode
            mButtonPreferredNetworkMode.setValue(Integer.toString(preferredNetworkMode));
            //set the Settings.System
            android.provider.Settings.Secure.putInt(mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                        preferredNetworkMode );
            //Set the Modem
        if (CallSettings.isMultipleSim()) {
            mGeminiPhone.setPreferredNetworkTypeGemini(preferredNetworkMode,
                        this.obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE), mSimId);
        } else {
            mPhone.setPreferredNetworkType(preferredNetworkMode,
                    this.obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
        }
    }
    }
    
    
    private void UpdateGeneralPreferredNetworkModeSummary(int NetworkMode) {
        android.provider.Settings.Secure.putInt(mPhone.getContext().getContentResolver(),
                android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                NetworkMode );
    }

    private void UpdatePreferredNetworkModeSummary(int NetworkMode) {
        switch(NetworkMode) {
            case Phone.NT_MODE_WCDMA_PREF:
                if((getBaseBand(WCDMA_CARD_SLOT) & MODEM_MASK_TDSCDMA) != 0){
                    mButtonPreferredNetworkMode.setSummary(
                        R.string.preferred_network_mode_scdma_perf_summary);
                }else{
                    mButtonPreferredNetworkMode.setSummary(
                        R.string.preferred_network_mode_wcdma_perf_summary);
                }
                break;
            case Phone.NT_MODE_GSM_ONLY:
                mButtonPreferredNetworkMode.setSummary(
                        R.string.preferred_network_mode_gsm_only_summary);
                break;
            case Phone.NT_MODE_WCDMA_ONLY:
                if((getBaseBand(WCDMA_CARD_SLOT) & MODEM_MASK_TDSCDMA) != 0){
                    mButtonPreferredNetworkMode.setSummary(
                        R.string.preferred_network_mode_scdma_only_summary);
                }else{
                    mButtonPreferredNetworkMode.setSummary(
                        R.string.preferred_network_mode_wcdma_only_summary);
                }
                break;
            case Phone.NT_MODE_GSM_UMTS:
				mButtonPreferredNetworkMode.setSummary(
		        R.string.preferred_network_mode_gsm_wcdma_summary);
                break;
            case Phone.NT_MODE_CDMA:
                switch (mPhone.getLteOnCdmaMode()) {
                    case Phone.LTE_ON_CDMA_TRUE:
                        mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_cdma_summary);
                    break;
                    case Phone.LTE_ON_CDMA_FALSE:
                    default:
                        mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_cdma_evdo_summary);
                        break;
                }
                break;
            case Phone.NT_MODE_CDMA_NO_EVDO:
                mButtonPreferredNetworkMode.setSummary(
                        R.string.preferred_network_mode_cdma_only_summary);
                break;
            case Phone.NT_MODE_EVDO_NO_CDMA:
                mButtonPreferredNetworkMode.setSummary(
                        R.string.preferred_network_mode_evdo_only_summary);
                break;
            case Phone.NT_MODE_GLOBAL:
            default:
                mButtonPreferredNetworkMode.setSummary(
                        R.string.preferred_network_mode_lte_cdma_summary);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
        case REQUEST_CODE_EXIT_ECM:
            Boolean isChoiceYes =
                data.getBooleanExtra(EmergencyCallbackModeExitDialog.EXTRA_EXIT_ECM_RESULT, false);
            if (isChoiceYes) {
                // If the phone exits from ECM mode, show the CDMA Options
                mCdmaOptions.showDialog(mClickedPreference);
            } else {
                // do nothing
            }
            break;

        default:
            break;
        }
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {  // See ActionBar#setDisplayHomeAsUpEnabled()
            // Commenting out "logical up" capability. This is a workaround for issue 5278083.
            //
            // Settings app may not launch this activity via UP_ACTIVITY_CLASS but the other
            // Activity that looks exactly same as UP_ACTIVITY_CLASS ("SubSettings" Activity).
            // At that moment, this Activity launches UP_ACTIVITY_CLASS on top of the Activity.
            // which confuses users.
            // TODO: introduce better mechanism for "up" capability here.
            /*Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(UP_ACTIVITY_PACKAGE, UP_ACTIVITY_CLASS);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);*/
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private boolean isUsedGeneralPreference()
    {
        if (!CallSettings.isMultipleSim())
        {
            return false;
        }
        List<SIMInfo> simList = SIMInfo.getInsertedSIMList(this);
        boolean found3g = false;
        for (SIMInfo info: simList) {
            if (isSupport3G(info.mSlot)) {
                found3g = true;
                break;
            }
        }
        return (simList.size() > 1) && found3g;
    }
    
    public static boolean isSupport3G(int slot)
    {
        //For current, we suppose only two slot support
        if (slot < 0 || slot > 1) {
            return false;
        }
        String propertyKey = "gsm.baseband.capability";
        String capability = null;
        if (slot == 1) {
            propertyKey += "2";
        }
        capability = SystemProperties.get(propertyKey);
        if (capability == null || "".equals(capability)) {
            return false;
        }
        
        int value = 0;
        try {
            value = Integer.valueOf(capability, 16);
        }catch (NumberFormatException ne) {
            return false;
        }
        
        // GPRS: 0x01
        // EDGE: 0x02
        // WCDMA: 0x04
        // TD-SCDMA: 0x08
        // HSDPA: 0x10
        // HSUPA: 0x20
        // HSPA+: 0x40   // Reserve 
        // LTE: 0x80 // Reserve 
        if (value <= 0x3) {
            return false;
        }
        
        return true;
    }

    private static int getBaseBand(int slot)
    {
        int value = 0;
        String propertyKey = "gsm.baseband.capability";
        String capability = null;
        if (slot == 1) {
            propertyKey += "2";
        }
        capability = SystemProperties.get(propertyKey);
        if (capability == null || "".equals(capability)) {
            return value;
        }
        
        try {
            value = Integer.valueOf(capability, 16);
        }catch (NumberFormatException ne) {
            log("parse value of basband error");
        }
        return value;        
    }

    //handle data state changed
    public static int DATA_STATE_CHANGE_TIMEOUT = 2001;
    private boolean isChangeData = false;
    Handler mH = new Handler() {
        
        public void handleMessage(Message msg) {
            if (msg.what == DATA_STATE_CHANGE_TIMEOUT) {
                if (pd != null && pd.isShowing() && isChangeData) {
                    try {
                        pd.dismiss();
                    } catch (Exception e) {
                        Log.d(LOG_TAG, e.toString());
                    }
                    pd = null;
                    isChangeData = false;
                    setDataConnPref();
                }
            }
        }
    };
    
    private static Phone.DataState getMobileDataState(Intent intent) {
        String str = intent.getStringExtra(Phone.STATE_KEY);
        if (str != null) {
            return Enum.valueOf(Phone.DataState.class, str);
        } else {
            return Phone.DataState.DISCONNECTED;
        }
    }

    /* Orange customization begin */
    private boolean isOrangeSupport() {
	    String optr = SystemProperties.get("ro.operator.optr");
	    if (optr != null && "OP03".equals(optr)) {
		    return true;
	    }
	    return false;
    }
    /* Orange customization end */
    // MTK_OP01_PROTECT_START 
    private boolean isDataConnAvailable() {
        if (OPERATOR != null && 
            "OP01".equals(OPERATOR) &&
            CallSettings.isMultipleSim()) {
            return true;
        }
        return false;
    }
    //MTK_OP01_PROTECT_END

    private void setScreenEnabled(){
        boolean isShouldEnabled = false;
        boolean isIdle = (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE);

        isShouldEnabled = isIdle && (!mAirplaneModeEnabled) && (mDualSimMode!=0);
        getPreferenceScreen().setEnabled(isShouldEnabled);

        boolean isGeminiMode = CallSettings.isMultipleSim();
        boolean isSupport3GSwitch = PhoneUtils.isSupportFeature("3G_SWITCH");
        List<SIMInfo> sims = SIMInfo.getInsertedSIMList(this);
        boolean isHasSimCard = ((sims != null) && (sims.size() > 0));
        if (mPreference3GSwitch != null) {
            mPreference3GSwitch.setEnabled(isHasSimCard && isShouldEnabled);
        }

        if(mButtonPreferredNetworkMode != null){
            boolean isNWModeEnabled = isShouldEnabled && CallSettings.isRadioOn(WCDMA_CARD_SLOT);
            mButtonPreferredNetworkMode.setEnabled(isNWModeEnabled);
            if(!isNWModeEnabled){
                Dialog dialog = mButtonPreferredNetworkMode.getDialog();
                if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
            }
        }
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_GPRS_SWITCH_CONFIRM) {
            AlertDialog dialog;
            String message = "";
            if((mDataSwitchMsgIndex >= 0) && (mDataSwitchMsgIndex <= 2)) {
                message = getResources().getString(mDataSwitchMsgStr[mDataSwitchMsgIndex]);
            } 
            dialog = new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(message)
            .setPositiveButton(com.android.internal.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(mSelectGprsIndex != -1){
                            if ((mDataSwitchMsgIndex == 0) | (mDataSwitchMsgIndex == 2)) {
                                enableDataRoaming(mSelectGprsIndex);
                            }
                            switchGprsDefautlSIM(mSelectGprsIndex);
                            mSelectGprsIndex = -1;
                        }
                    }
                })
            .setNegativeButton(com.android.internal.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setDataConnPref();
                    }
                })
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    setDataConnPref();
                }
            })
            .create();
            return dialog;
        }
        return null;
    }
    private SIMInfo getSimInfo(long index) {
        List<SIMInfo> simList = SIMInfo.getInsertedSIMList(this);
        for (SIMInfo simInfo : simList) {
            if (index == simInfo.mSimId) {
                return simInfo;
            }
        }
        return null;
    }

    private void enableDataRoaming(long value){
        log("enableDataRoaming with SimId="+value);
        try {
            if(mTelephony != null) {
                mTelephony.setDataRoamingEnabledGemini(true, 
                           SIMInfo.getSlotById(this, value));
            }
        } catch (RemoteException e){
            log( "mTelephony exception");
            return;
        }
        SIMInfo.setDataRoaming(this,SimInfo.DATA_ROAMING_ENABLE, value);
    }

}
