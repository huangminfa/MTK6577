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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.security.Credentials;
import android.security.KeyStore;
import android.telephony.TelephonyManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.util.AsyncChannel;
import com.android.settings.ProgressCategoryForWifi;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.mediatek.xlog.Xlog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mediatek.featureoption.FeatureOption;
import com.android.settings.Utils;

/**
 * This currently provides three types of UI.
 *
 * Two are for phones with relatively small screens: "for SetupWizard" and "for usual Settings".
 * Users just need to launch WifiSettings Activity as usual. The request will be appropriately
 * handled by ActivityManager, and they will have appropriate look-and-feel with this fragment.
 *
 * Third type is for Setup Wizard with X-Large, landscape UI. Users need to launch
 * {@link WifiSettingsForSetupWizardXL} Activity, which contains this fragment but also has
 * other decorations specific to that screen.
 */
public class WifiSettings extends SettingsPreferenceFragment
        implements DialogInterface.OnClickListener  {
    private static final String TAG = "WifiSettings";
    private static final int MENU_ID_SCAN = Menu.FIRST;
    private static final int MENU_ID_ADD_NETWORK = Menu.FIRST + 1;
    private static final int MENU_ID_ADVANCED = Menu.FIRST + 2;
    private static final int MENU_ID_CONNECT = Menu.FIRST + 3;
    private static final int MENU_ID_FORGET = Menu.FIRST + 4;
    private static final int MENU_ID_MODIFY = Menu.FIRST + 5;
    private static final int MENU_ID_DISCONNECT = Menu.FIRST + 6;

    private static final int WIFI_DIALOG_ID = 1;

    // Combo scans can take 5-6s to complete - set to 10s.
    private static final int WIFI_RESCAN_INTERVAL_MS = 6 * 1000;

    // Instance state keys
    private static final String SAVE_DIALOG_EDIT_MODE = "edit_mode";
    private static final String SAVE_DIALOG_ACCESS_POINT_STATE = "wifi_ap_state";

    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver;
    private final Scanner mScanner;
    private TelephonyManager mTm;
    private WifiManager mWifiManager;
    private WifiEnabler mWifiEnabler;
    // An access point being editted is stored here.
    private AccessPoint mSelectedAccessPoint;

    private DetailedState mLastState;
    private WifiInfo mLastInfo;

    private AtomicBoolean mConnected = new AtomicBoolean(false);

    private int mKeyStoreNetworkId = INVALID_NETWORK_ID;

    private WifiDialog mDialog;

    private TextView mEmptyView;

    /* Used in Wifi Setup context */

    // this boolean extra specifies whether to disable the Next button when not connected
    private static final String EXTRA_ENABLE_NEXT_ON_CONNECT = "wifi_enable_next_on_connect";

    // should Next button only be enabled when we have a connection?
    private boolean mEnableNextOnConnection;
    private boolean mInXlSetupWizard;

    // Save the dialog details
    private boolean mDlgEdit;
    private AccessPoint mDlgAccessPoint;
    private Bundle mAccessPointSavedState;


    private int mConfiguredApCount;
    private long mLastAPUpdateTimeMs;
    //all configured ap list
    private List<WifiConfiguration> mConfigs;
    //Array to store the right order of each AP
    private int[] mPriorityOrder;
    //AP priority before user modification
    private int mOldPriorityOrder;
    private int mNewPriorityOrder;
    private boolean mIsAutoPriority;
    private WifiConfiguration mLastConnectedConfig;
//    private int mConnectType;
    private boolean mIsPriorityNeedRefresh=false;
    private static final int WIFI_AP_MAX_ALLOWED_PRIORITY = 1000000;
    //to connect to an AP, we will set its priority to a high value temporarily, 
    //after receiver a Connected or two Connecting event, restore its priority
    private int mReceiveredConnetingTime = 0;
    private int mLastPriority;

    private PreferenceCategory mCmccTrustAP;
    private PreferenceCategory mCmccConfigedAP;
    private PreferenceCategory mCmccNewAP;

    /* End of "used in Wifi Setup context" */

    public WifiSettings() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mFilter.addAction(WifiManager.ERROR_ACTION);
        mFilter.addAction(WifiManager.NO_CERTIFICATION_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };

        mScanner = new Scanner();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mInXlSetupWizard = (activity instanceof WifiSettingsForSetupWizardXL);
        /// M: set title
        getActivity().setTitle(R.string.wifi_settings_title);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // We don't call super.onActivityCreated() here, since it assumes we already set up
        // Preference (probably in onCreate()), while WifiSettings exceptionally set it up in
        // this method.
        if(FeatureOption.MTK_EAP_SIM_AKA==true){
        mTm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        }

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiManager.asyncConnect(getActivity(), new WifiServiceHandler());
        if (savedInstanceState != null
                && savedInstanceState.containsKey(SAVE_DIALOG_ACCESS_POINT_STATE)) {
            mDlgEdit = savedInstanceState.getBoolean(SAVE_DIALOG_EDIT_MODE);
            mAccessPointSavedState = savedInstanceState.getBundle(SAVE_DIALOG_ACCESS_POINT_STATE);
        }

        final Activity activity = getActivity();
        final Intent intent = activity.getIntent();

        // if we're supposed to enable/disable the Next button based on our current connection
        // state, start it off in the right state
        mEnableNextOnConnection = intent.getBooleanExtra(EXTRA_ENABLE_NEXT_ON_CONNECT, false);

        if (mEnableNextOnConnection) {
            if (hasNextButton()) {
                final ConnectivityManager connectivity = (ConnectivityManager)
                        getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivity != null) {
                    NetworkInfo info = connectivity.getNetworkInfo(
                            ConnectivityManager.TYPE_WIFI);
                    changeNextButtonState(info.isConnected());
                }
            }
        }

        if (mInXlSetupWizard) {
            addPreferencesFromResource(R.xml.wifi_access_points_for_wifi_setup_xl);
        } else {
//MTK_OP01_PROTECT_START        
            if(Utils.isCmccLoad()){
                if(getActivity().getIntent().getBooleanExtra("access_points_and_gprs", false)){
                    addPreferencesFromResource(R.xml.wifi_access_points_and_gprs);
                } else {
                    addPreferencesFromResource(R.xml.wifi_settings);
                }
                mCmccTrustAP = (PreferenceCategory)findPreference("trust_access_points");
                mCmccConfigedAP = (PreferenceCategory)findPreference("configed_access_points");
                mCmccNewAP = (PreferenceCategory)findPreference("new_access_points");
            } else
//MTK_OP01_PROTECT_END
            {
                addPreferencesFromResource(R.xml.wifi_settings);
            }
            Switch actionBarSwitch = new Switch(activity);
            if (activity instanceof PreferenceActivity) {
                PreferenceActivity preferenceActivity = (PreferenceActivity) activity;
                if (preferenceActivity.onIsHidingHeaders() || !preferenceActivity.onIsMultiPane()) {
                    final int padding = activity.getResources().getDimensionPixelSize(
                            R.dimen.action_bar_switch_padding);
                    actionBarSwitch.setPadding(0, 0, padding, 0);
                    activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                            ActionBar.DISPLAY_SHOW_CUSTOM);
                    activity.getActionBar().setCustomView(actionBarSwitch, new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.WRAP_CONTENT,
                            ActionBar.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER_VERTICAL | Gravity.RIGHT));
                }
            }
            mWifiEnabler = new WifiEnabler(activity, actionBarSwitch);

//MTK_OP01_PROTECT_START
        }
//MTK_OP01_PROTECT_END
        mEmptyView = (TextView) getView().findViewById(android.R.id.empty);
        getListView().setEmptyView(mEmptyView);

        registerForContextMenu(getListView());
        setHasOptionsMenu(true);

//MTK_OP01_PROTECT_START        
        Xlog.d(TAG, "onActivityCreated(), Priority type is "+(Settings.System.getInt(getContentResolver(), 
                Settings.System.WIFI_PRIORITY_TYPE,Settings.System.WIFI_PRIORITY_TYPE_DEFAULT)));
        if(Utils.isCmccLoad()){
            getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.WIFI_PRIORITY_TYPE), 
                    false, priorityObserver);
//            getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.WIFI_CONNECT_TYPE), 
//                    false, connectTypeObserver);
        }
//MTK_OP01_PROTECT_END        

        // After confirming PreferenceScreen is available, we call super.
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWifiEnabler != null) {
            mWifiEnabler.resume();
        }

        getActivity().registerReceiver(mReceiver, mFilter);
        if (mKeyStoreNetworkId != INVALID_NETWORK_ID &&
                KeyStore.getInstance().state() == KeyStore.State.UNLOCKED) {
            mWifiManager.connectNetwork(mKeyStoreNetworkId);
        }
        mKeyStoreNetworkId = INVALID_NETWORK_ID;

        updateAccessPoints();
	    AccessPoint.resetWFAFlag();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWifiEnabler != null) {
            mWifiEnabler.pause();
        }
        getActivity().unregisterReceiver(mReceiver);
        mScanner.pause();
    }
    @Override
    public void onDestroy(){
//MTK_OP01_PROTECT_START
        Xlog.i(TAG, "onDestroy(), unregister priority observer");
        if(Utils.isCmccLoad()){
            if(priorityObserver!=null){
                getContentResolver().unregisterContentObserver(priorityObserver);
            }
/*            if(connectTypeObserver!=null){
                getContentResolver().unregisterContentObserver(connectTypeObserver);
            }
*/
        }
//MTK_OP01_PROTECT_END       
        super.onDestroy();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // We don't want menus in Setup Wizard XL.
        if (!mInXlSetupWizard) {
            final boolean wifiIsEnabled = mWifiManager.isWifiEnabled();
            menu.add(Menu.NONE, MENU_ID_SCAN, 0, R.string.wifi_menu_scan)
                    //.setIcon(R.drawable.ic_menu_scan_network)
                    .setEnabled(wifiIsEnabled)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add(Menu.NONE, MENU_ID_ADD_NETWORK, 0, R.string.wifi_add_network)
                    .setEnabled(wifiIsEnabled)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add(Menu.NONE, MENU_ID_ADVANCED, 0, R.string.wifi_menu_advanced)
                    //.setIcon(android.R.drawable.ic_menu_manage)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If the dialog is showing, save its state.
        if (mDialog != null && mDialog.isShowing()) {
            outState.putBoolean(SAVE_DIALOG_EDIT_MODE, mDlgEdit);
            if (mDlgAccessPoint != null) {
                mAccessPointSavedState = new Bundle();
                mDlgAccessPoint.saveWifiState(mAccessPointSavedState);
                outState.putBundle(SAVE_DIALOG_ACCESS_POINT_STATE, mAccessPointSavedState);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_SCAN:
                if (mWifiManager.isWifiEnabled()) {
                    mScanner.forceScan();
                }
                return true;
            case MENU_ID_ADD_NETWORK:
                if (mWifiManager.isWifiEnabled()) {
                    onAddNetworkPressed();
                }
                return true;
            case MENU_ID_ADVANCED:
                if (getActivity() instanceof PreferenceActivity) {
                    ((PreferenceActivity) getActivity()).startPreferencePanel(
                            AdvancedWifiSettings.class.getCanonicalName(),
                            null,
                            R.string.wifi_advanced_titlebar, null,
                            this, 0);
                } else {
                    startFragment(this, AdvancedWifiSettings.class.getCanonicalName(), -1, null);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
        if (mInXlSetupWizard) {
            ((WifiSettingsForSetupWizardXL)getActivity()).onCreateContextMenu(menu, view, info);
        } else if (info instanceof AdapterContextMenuInfo) {
            Preference preference = (Preference) getListView().getItemAtPosition(
                    ((AdapterContextMenuInfo) info).position);

            if (preference instanceof AccessPoint) {
                mSelectedAccessPoint = (AccessPoint) preference;
                menu.setHeaderTitle(mSelectedAccessPoint.ssid);
                if (mSelectedAccessPoint.getLevel() != -1
                        && mSelectedAccessPoint.getState() == null) {
                    menu.add(Menu.NONE, MENU_ID_CONNECT, 0, R.string.wifi_menu_connect);
                }
//MTK_OP01_PROTECT_START                
                //current connected AP, add a disconnect option to it
                if(mSelectedAccessPoint.getState() !=null ){
                    if(Utils.isCmccLoad()){
                        menu.add(Menu.NONE, MENU_ID_DISCONNECT, 0, R.string.wifi_menu_disconnect);
                    }
                }
//MTK_OP01_PROTECT_END    
                if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
                    if(!AccessPoint.isCmccAp(mSelectedAccessPoint)){
                        menu.add(Menu.NONE, MENU_ID_FORGET, 0, R.string.wifi_menu_forget);
                    }
                    menu.add(Menu.NONE, MENU_ID_MODIFY, 0, R.string.wifi_menu_modify);
                }
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mSelectedAccessPoint == null) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case MENU_ID_CONNECT: {
                if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
                    if (!requireKeyStore(mSelectedAccessPoint.getConfig())) {
                        mWifiManager.connectNetwork(mSelectedAccessPoint.networkId);
                    }
                } else if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE) {
                    /** Bypass dialog for unsecured networks */
                    mSelectedAccessPoint.generateOpenNetworkConfig();
                    mWifiManager.connectNetwork(mSelectedAccessPoint.getConfig());
                } else {
                    showConfigUi(mSelectedAccessPoint, false);
                }
                return true;
            }
            case MENU_ID_FORGET: {
                mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);
                return true;
            }
            case MENU_ID_MODIFY: {
                showConfigUi(mSelectedAccessPoint, true);
                return true;
            }
//MTK_OP01_PROTECT_START
            case MENU_ID_DISCONNECT:{
                disconnect(mSelectedAccessPoint.networkId);
                return true;
            }
//MTK_OP01_PROTECT_END
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        if (preference instanceof AccessPoint) {
            mSelectedAccessPoint = (AccessPoint) preference;
            /** Bypass dialog for unsecured, unsaved networks */
            if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE &&
                    mSelectedAccessPoint.networkId == INVALID_NETWORK_ID) {
                if(mSelectedAccessPoint.isOpenApWPSSupported()){
                    showConfigUi(mSelectedAccessPoint, false);
                }else{
                    mSelectedAccessPoint.generateOpenNetworkConfig();
                    mWifiManager.connectNetwork(mSelectedAccessPoint.getConfig());
                }
            } else {
                showConfigUi(mSelectedAccessPoint, false);
            }
        } else {
            return super.onPreferenceTreeClick(screen, preference);
        }
        return true;
    }

    /**
     * Shows an appropriate Wifi configuration component.
     * Called when a user clicks "Add network" preference or one of available networks is selected.
     */
    private void showConfigUi(AccessPoint accessPoint, boolean edit) {
        if (mInXlSetupWizard) {
            ((WifiSettingsForSetupWizardXL)getActivity()).showConfigUi(accessPoint, edit);
        } else {
            showDialog(accessPoint, edit);
        }
    }

    private void showDialog(AccessPoint accessPoint, boolean edit) {
        if (mDialog != null) {
            removeDialog(WIFI_DIALOG_ID);
            mDialog = null;
            mAccessPointSavedState = null;
        }

        // Save the access point and edit mode
        mDlgAccessPoint = accessPoint;
        mDlgEdit = edit;

        showDialog(WIFI_DIALOG_ID);
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        AccessPoint ap = mDlgAccessPoint; // For manual launch
        if (ap == null) { // For re-launch from saved state
            if (mAccessPointSavedState != null) {
                ap = new AccessPoint(getActivity(), mAccessPointSavedState);
                // For repeated orientation changes
                mDlgAccessPoint = ap;
            }
        }
        // If it's still null, fine, it's for Add Network
        mSelectedAccessPoint = ap;
        mDialog = new WifiDialog(getActivity(), this, ap, mDlgEdit,mTm);
        return mDialog;
    }

    private boolean requireKeyStore(WifiConfiguration config) {
        if (WifiConfigController.requireKeyStore(config) &&
                KeyStore.getInstance().state() != KeyStore.State.UNLOCKED) {
            mKeyStoreNetworkId = config.networkId;
            Credentials.getInstance().unlock(getActivity());
            return true;
        }
        return false;
    }

    /**
     * Shows the latest access points available with supplimental information like
     * the strength of network and the security for it.
     */
    private void updateAccessPoints() {
        final int wifiState = mWifiManager.getWifiState();

        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                // AccessPoints are automatically sorted with TreeSet.
                final Collection<AccessPoint> accessPoints = constructAccessPoints();
                if(!Utils.isCmccLoad() ){
                    getPreferenceScreen().removeAll();
                }
                if (mInXlSetupWizard) {
                    ((WifiSettingsForSetupWizardXL)getActivity()).onAccessPointsUpdated(
                            getPreferenceScreen(), accessPoints);
                } else {
                    if(!Utils.isCmccLoad()){
                        for (AccessPoint accessPoint : accessPoints) {
                            getPreferenceScreen().addPreference(accessPoint);
                        }
                    }
                }
                break;

            case WifiManager.WIFI_STATE_ENABLING:
//MTK_OP01_PROTECT_START
                if(Utils.isCmccLoad()){
                    emptyCategory();
                }else
//MTK_OP01_PROTECT_END
                {
                    getPreferenceScreen().removeAll();
                }
                break;

            case WifiManager.WIFI_STATE_DISABLING:
                addMessagePreference(R.string.wifi_stopping);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                addMessagePreference(R.string.wifi_empty_list_wifi_off);
                break;
            default:
                addMessagePreference(R.string.wifi_empty_list_wifi_off);
                break;
        }
    }

    private void addMessagePreference(int messageId) {
        if (mEmptyView != null) mEmptyView.setText(messageId);
//MTK_OP01_PROTECT_START
        if(Utils.isCmccLoad()){
            getPreferenceScreen().removePreference(mCmccTrustAP);
            getPreferenceScreen().removePreference(mCmccConfigedAP);
            getPreferenceScreen().removePreference(mCmccNewAP);
        }else
//MTK_OP01_PROTECT_END
        {
            getPreferenceScreen().removeAll();
        }
    }

    /** Returns sorted list of access points */
    private List<AccessPoint> constructAccessPoints() {
        ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        /** Lookup table to more quickly update AccessPoints by only considering objects with the
         * correct SSID.  Maps SSID -> List of AccessPoints with the given SSID.  */
        Multimap<String, AccessPoint> apMap = new Multimap<String, AccessPoint>();

        if(Utils.isCmccLoad()){
            emptyCategory();
        }

        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                // Add for EAP-SIM begin

                if(FeatureOption.MTK_EAP_SIM_AKA==true && config.IMSI!=null && !config.IMSI.equals("\"none\"")){

                    Xlog.d(TAG,"dbg eap-sim/aka");
                    Xlog.d(TAG,"config.SSID "+config.SSID);
                    Xlog.d(TAG,"config.imsi "+config.IMSI);
                    Xlog.d(TAG,"config.SIMSLOT "+config.SIMSLOT);
                    Xlog.d(TAG,"config.networkId "+config.networkId);
                    //networkId
                    int slot=0;
                    //if(config.SIMSLOT.equals("\"\"0\"\""))
					if(config.SIMSLOT.equals("\"0\""))
                        slot=0;
                    //else if(config.SIMSLOT.equals("\"\"1\"\""))
					else if(config.SIMSLOT.equals("\"1\""))
                        slot=1;
                    
                    //in simulator mode, skip
                    if((config.IMSI).equals("\"1232010000000000@wlan.mnc001.mcc232.3gppnetwork.org\"")||(config.IMSI).equals("\"0232010000000000@wlan.mnc001.mcc232.3gppnetwork.org\"")){
                       Xlog.d(TAG,"in simulator mode, skip");
                    }
                        
                    else if(config.toString().contains("eap: SIM") ){
                    //if(config.toString().contains("eap: SIM") ){
			Xlog.d(TAG,"mTm.getSubscriberIdGemini() "+mTm.getSubscriberIdGemini(slot));
                        Xlog.d(TAG,"makeNAI(mTm.getSubscriberIdGemini(), SIM); "+WifiDialog.makeNAI(mTm.getSubscriberIdGemini(slot), "SIM"));
                        //Xlog.d(TAG,"makeNAI(mTm.getSubscriberIdGemini(), AKA); "+WifiDialog.makeNAI(mTm.getSubscriberIdGemini(slot), "AKA"));
                        if(config.IMSI.equals(WifiDialog.makeNAI(mTm.getSubscriberIdGemini(slot), "SIM")))
                            Xlog.d(TAG,"user doesn't change or remove sim card");
                        else{
                            Xlog.d(TAG,"user change or remove sim card");
                           
                            Xlog.d(TAG," >>mWifiManager.removeNetwork(config.networkId);");
                            boolean s=mWifiManager.removeNetwork(config.networkId);
                            Xlog.d(TAG," <<mWifiManager.removeNetwork(config.networkId); s: "+s);
                            Xlog.d(TAG,"   >>saveNetworks();");
                            s=mWifiManager.saveConfiguration();
                            Xlog.d(TAG,"saveNetworks(): "+s);
                            continue;
                        }  
                    }else if(config.toString().contains("eap: AKA")){
                       
                        //Xlog.d(TAG,"makeNAI(mTm.getSubscriberIdGemini(), SIM); "+WifiDialog.makeNAI(mTm.getSubscriberIdGemini(slot), "SIM"));
			Xlog.d(TAG,"mTm.getSubscriberIdGemini() "+mTm.getSubscriberIdGemini(slot));
                        Xlog.d(TAG,"makeNAI(mTm.getSubscriberIdGemini(), AKA); "+WifiDialog.makeNAI(mTm.getSubscriberIdGemini(slot), "AKA"));
                        if(WifiDialog.makeNAI(mTm.getSubscriberIdGemini(slot), "AKA").equals(config.IMSI))
                            Xlog.d(TAG,"user doesn't change or remove usim card");
                        else{
                            Xlog.d(TAG,"user change or remove usim card");
                            Xlog.d(TAG," >> mWifiManager.removeNetwork(config.networkId);");
                            boolean s=mWifiManager.removeNetwork(config.networkId);
                            Xlog.d(TAG," << mWifiManager.removeNetwork(config.networkId); s: "+s);
                            Xlog.d(TAG,"   >>saveNetworks();");
                            s=mWifiManager.saveConfiguration();
                            Xlog.d(TAG,"saveNetworks(): "+s);
                            Xlog.d(TAG,"   <<saveNetworks();");
                            continue;
                        }
                    }
                }
    // Add for EAP-SIM end

                AccessPoint accessPoint = new AccessPoint(getActivity(), config);
                accessPoint.update(mLastInfo, mLastState);
                accessPoints.add(accessPoint);
                apMap.put(accessPoint.ssid, accessPoint);
                if(Utils.isCmccLoad()){
                    if(AccessPoint.isCmccAp(accessPoint)){
                        mCmccTrustAP.addPreference(accessPoint);
                    } else {
                        mCmccConfigedAP.addPreference(accessPoint);
                    }
                }
            }
            if(Utils.isCmccLoad()){
                if(mCmccConfigedAP !=null && mCmccConfigedAP.getPreferenceCount()==0){
                    getPreferenceScreen().removePreference(mCmccConfigedAP);
                }
            }
        }

        final List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0 ||
                        result.capabilities.contains("[IBSS]")) {
                    continue;
                }

                boolean found = false;
                for (AccessPoint accessPoint : apMap.getAll(result.SSID)) {
                    if (accessPoint.update(result)){
                        found = true;
                    }
                }
                if (!found) {
                    AccessPoint accessPoint = new AccessPoint(getActivity(), result);
                    accessPoints.add(accessPoint);
                    apMap.put(accessPoint.ssid, accessPoint);
                    if(Utils.isCmccLoad()){
                        if(AccessPoint.isCmccAp(accessPoint)){
                            mCmccTrustAP.addPreference(accessPoint);
                        } else {
                            mCmccNewAP.addPreference(accessPoint);
                        }
                    }
                }
            }
            if(Utils.isCmccLoad()){
                if(mCmccNewAP !=null && mCmccNewAP.getPreferenceCount() == 0){
                    getPreferenceScreen().removePreference(mCmccNewAP);
                }
            }
        }

        // Pre-sort accessPoints to speed preference insertion

        ArrayList<AccessPoint> origAccessPoints = new ArrayList<AccessPoint>(accessPoints.size());
        origAccessPoints.addAll(accessPoints);
        try{
            Collections.sort(accessPoints);
        } catch(Exception e){
            Xlog.d(TAG,"collection.sort exception;origAccessPoints="+origAccessPoints);
            return origAccessPoints;
        }
        return accessPoints;
    }

    /** A restricted multimap for use in constructAccessPoints */
    private class Multimap<K,V> {
        private HashMap<K,List<V>> store = new HashMap<K,List<V>>();
        /** retrieve a non-null list of values with key K */
        List<V> getAll(K key) {
            List<V> values = store.get(key);
            return values != null ? values : Collections.<V>emptyList();
        }

        void put(K key, V val) {
            List<V> curVals = store.get(key);
            if (curVals == null) {
                curVals = new ArrayList<V>(3);
                store.put(key, curVals);
            }
            curVals.add(val);
        }
    }

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action) ||
                WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action) ||
                WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)) {
                updateAccessPoints();
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            //Ignore supplicant state changes when network is connected
            //TODO: we should deprecate SUPPLICANT_STATE_CHANGED_ACTION and
            //introduce a broadcast that combines the supplicant and network
            //network state change events so the apps dont have to worry about
            //ignoring supplicant state change when network is connected
            //to get more fine grained information.
            if (!mConnected.get()) {
                updateConnectionState(WifiInfo.getDetailedStateOf((SupplicantState)
                        intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
            }

            if (mInXlSetupWizard) {
                ((WifiSettingsForSetupWizardXL)getActivity()).onSupplicantStateChanged(intent);
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO);
            mConnected.set(info.isConnected());
            changeNextButtonState(info.isConnected());
            updateAccessPoints();
            updateConnectionState(info.getDetailedState());
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        } else if (WifiManager.ERROR_ACTION.equals(action)) {
            int errorCode = intent.getIntExtra(WifiManager.EXTRA_ERROR_CODE, 0);
            switch (errorCode) {
                case WifiManager.WPS_OVERLAP_ERROR:
                    Toast.makeText(context, R.string.wifi_wps_overlap_error,
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }else if(WifiManager.NO_CERTIFICATION_ACTION.equals(action)){ 
	        String apSSID = "";
	        if(mSelectedAccessPoint!=null){
	            apSSID = "["+mSelectedAccessPoint.ssid+"] ";
	        }
	        Xlog.i(TAG, "Receive  no certification broadcast for AP "+apSSID);
	        String message = getResources().getString(R.string.wifi_no_cert_for_wapi)+apSSID;
	        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
	    }
    }

    private void updateConnectionState(DetailedState state) {
        /* sticky broadcasts can call this when wifi is disabled */
        if (!mWifiManager.isWifiEnabled()) {
            mScanner.pause();
            return;
        }
        if(state==DetailedState.CONNECTED){
            long endTime = System.currentTimeMillis();
            Xlog.i(TAG, "[Performance test][Settings][wifi] wifi connect end ["+ endTime +"]");
        } else if (state==DetailedState.DISCONNECTED){
            long endTime = System.currentTimeMillis();
            Xlog.i(TAG, "[Performance test][Settings][wifi] wifi forget end ["+ endTime +"]");
        }

        if (state == DetailedState.OBTAINING_IPADDR) {
            mScanner.pause();
        } else {
            mScanner.resume();
        }

        mLastInfo = mWifiManager.getConnectionInfo();
        if (state != null) {
            mLastState = state;
        }
//MTK_OP01_PROTECT_START
        if(Utils.isCmccLoad()){
            updateAP(mCmccTrustAP);
            updateAP(mCmccConfigedAP);
            updateAP(mCmccNewAP);
        }else
//MTK_OP01_PROTECT_END
        {
            for (int i = getPreferenceScreen().getPreferenceCount() - 1; i >= 0; --i) {
                // Maybe there's a WifiConfigPreference
                Preference preference = getPreferenceScreen().getPreference(i);
                if (preference instanceof AccessPoint) {
                    final AccessPoint accessPoint = (AccessPoint) preference;
                    accessPoint.update(mLastInfo, mLastState);
                }
            }
        }
//MTK_OP01_PROTECT_START        
        //assume already connect to the selected AP, refresh APs's priority
        if(mIsPriorityNeedRefresh && (state==DetailedState.CONNECTING || state==DetailedState.CONNECTED)){
            if(state==DetailedState.CONNECTING){
                mReceiveredConnetingTime++ ;
            }
            if(state==DetailedState.CONNECTED || mReceiveredConnetingTime>=2){
                mIsPriorityNeedRefresh=false;
                mReceiveredConnetingTime = 0;
                updatePriority();
            }
        }
//MTK_OP01_PROTECT_END    
        if (mInXlSetupWizard) {
            ((WifiSettingsForSetupWizardXL)getActivity()).updateConnectionState(mLastState);
        }
    }

    private void updateWifiState(int state) {
        getActivity().invalidateOptionsMenu();

        switch (state) {
            case WifiManager.WIFI_STATE_ENABLED:
                mScanner.resume();
//MTK_OP01_PROTECT_START             
                updatePriority();
//MTK_OP01_PROTECT_END   
                return; // not break, to avoid the call to pause() below

            case WifiManager.WIFI_STATE_ENABLING:
                addMessagePreference(R.string.wifi_starting);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                addMessagePreference(R.string.wifi_empty_list_wifi_off);
                break;
        }

        mLastInfo = null;
        mLastState = null;
        mScanner.pause();
    }

    private class Scanner extends Handler {
        private int mRetry = 0;

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        void forceScan() {
            removeMessages(0);
            sendEmptyMessage(0);
        }

        void pause() {
            mRetry = 0;
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
            if (mWifiManager.startScanActive()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                Toast.makeText(getActivity(), R.string.wifi_fail_to_scan,
                        Toast.LENGTH_LONG).show();
                return;
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
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
                case WifiManager.CMD_WPS_COMPLETED:
                    WpsResult result = (WpsResult) msg.obj;
                    if (result == null) break;
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.wifi_wps_setup_title)
                        .setPositiveButton(android.R.string.ok, null);
                    switch (result.status) {
                        case FAILURE:
                            dialog.setMessage(R.string.wifi_wps_failed);
                            dialog.show();
                            break;
                        case IN_PROGRESS:
                            dialog.setMessage(R.string.wifi_wps_in_progress);
                            dialog.show();
                            break;
                        default:
                            if (result.pin != null) {
                                dialog.setMessage(getResources().getString(
                                        R.string.wifi_wps_pin_output, result.pin));
                                dialog.show();
                            }
                            break;
                    }
                    break;
                //TODO: more connectivity feedback
                default:
                    //Ignore
                    break;
            }
        }
    }

    /**
     * Renames/replaces "Next" button when appropriate. "Next" button usually exists in
     * Wifi setup screens, not in usual wifi settings screen.
     *
     * @param connected true when the device is connected to a wifi network.
     */
    private void changeNextButtonState(boolean connected) {
        if (mInXlSetupWizard) {
            ((WifiSettingsForSetupWizardXL)getActivity()).changeNextButtonState(connected);
        } else if (mEnableNextOnConnection && hasNextButton()) {
            getNextButton().setEnabled(connected);
        }
    }

    public void onClick(DialogInterface dialogInterface, int button) {
        if (mInXlSetupWizard) {
            if (button == WifiDialog.BUTTON_FORGET && mSelectedAccessPoint != null) {
                forget();
            } else if (button == WifiDialog.BUTTON_SUBMIT) {
                ((WifiSettingsForSetupWizardXL)getActivity()).onConnectButtonPressed();
            }
        } else {
            if (button == WifiDialog.BUTTON_FORGET && mSelectedAccessPoint != null) {
                forget();
            } else if (button == WifiDialog.BUTTON_SUBMIT) {
                submit(mDialog.getController());
            }
        }

    }

    /* package */ void submit(WifiConfigController configController) {
        int networkSetup = configController.chosenNetworkSetupMethod();
        switch(networkSetup) {
            case WifiConfigController.WPS_PBC:
            case WifiConfigController.WPS_DISPLAY:
            case WifiConfigController.WPS_KEYPAD:
                mWifiManager.startWps(configController.getWpsConfig());
                break;
            case WifiConfigController.MANUAL:
                final WifiConfiguration config = configController.getConfig();
                // add for EAP_SIM/AKA start,remind user when he use eap-sim/aka in a wrong way
                try{
                    if (config!=null && FeatureOption.MTK_EAP_SIM_AKA == true && config.IMSI!=null) {
                        if (config.toString().contains("eap: SIM")
                                || config.toString().contains("eap: AKA")) {
                            // cannot use eap-sim/aka under airplane mode
                            if (Settings.System.getInt(this.getContentResolver(),
                                    Settings.System.AIRPLANE_MODE_ON, 0) == 1) {
                                Toast.makeText(getActivity(), R.string.eap_sim_aka_airplanemode, Toast.LENGTH_LONG)
                                        .show();
                                return;
                            }

                            // cannot use eap-sim/aka without a sim/usimcard
                            if (config.IMSI.equals("\"error\"")) {
                                Toast.makeText(getActivity(), R.string.eap_sim_aka_no_sim_error, Toast.LENGTH_LONG)
                                        .show();
                                return;
                            }

                            // cannot use eap-sim/aka if user doesn't select a sim slot
                            if ((FeatureOption.MTK_GEMINI_SUPPORT == true)
                                    && (config.IMSI.equals("\"none\""))) {
                                Toast.makeText(getActivity(), R.string.eap_sim_aka_no_sim_slot_selected,
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                        }

                    }
                }catch (Exception e){
                    Xlog.d(TAG,"submit exception() "+e.toString());
                }

                if (config == null) {
                    //cannot use eap-sim/aka under airplane mode
                    if (FeatureOption.MTK_EAP_SIM_AKA == true) {
                                     
                        Xlog.d(TAG,"mSelectedAccessPoint "+mSelectedAccessPoint);
                                       
                        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
                        
                        if (configs != null) {
                            
                            for (WifiConfiguration mConfig : configs) {
                                Xlog.d(TAG,"onClick() >>if((mConfig.SSID).equals(mSelectedAccessPoint.ssid)){");
                                
                                Xlog.d(TAG,"onClick()" +mConfig.SSID);
                                Xlog.d(TAG,"onClick() "+mSelectedAccessPoint.ssid);
                                if((mConfig.SSID).equals(WifiDialog.addQuote( mSelectedAccessPoint.ssid)) &&
                                    (Settings.System.getInt(this.getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0) == 1)&&
                                    (mConfig.toString().contains("eap: SIM")|| mConfig.toString().contains("eap: AKA"))){
                                    Xlog.d(TAG, "remind user: cannot user eap-sim/aka under airplane mode");
                                    Toast.makeText(getActivity(), R.string.eap_sim_aka_airplanemode, Toast.LENGTH_LONG)
                                    .show();
                                    return;
                                }
                            }
                        }
                        /*
                        if (config.toString().contains("eap: SIM")
                                || config.toString().contains("eap: AKA")) {

                           XLog.d(TAG, "remind user: "+config.toString());
                            // cannot use eap-sim/aka under airplanemode
                            if (Settings.System.getInt(getActivity().getContentResolver(),
                                    Settings.System.AIRPLANE_MODE_ON, 0) == 1) {
                                Toast.makeText(getActivity(), R.string.eap_sim_aka_airplanemode, Toast.LENGTH_LONG)
                                        .show();
                                return;

                            }
                        }
                        */
                    }
                    if (mSelectedAccessPoint != null
                            && !requireKeyStore(mSelectedAccessPoint.getConfig())
                            && mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
                        DetailedState state = mSelectedAccessPoint.getState();
                        if(state == null){
                            mWifiManager.connectNetwork(mSelectedAccessPoint.networkId);
                        } 
//MTK_OP01_PROTECT_START                    
                        else if(Utils.isCmccLoad()){
                           disconnect(mSelectedAccessPoint.networkId);
                        }
//MTK_OP01_PROTECT_END  
                    }
                } else if (config.networkId != INVALID_NETWORK_ID) {
                    if (mSelectedAccessPoint != null) {
//MTK_OP01_PROTECT_START                  
                    //modify a configured access point, get its new priority order, transfer to its priority value
                    if(Utils.isCmccLoad()&& !mIsAutoPriority){
                        mNewPriorityOrder = config.priority;
                        config.priority = mConfiguredApCount-mNewPriorityOrder+1;
                        adjustPriority();
                    }
//MTK_OP01_PROTECT_END                  
                        saveNetwork(config);
                    }
                } else {
//MTK_OP01_PROTECT_START  
                    int networkId;
                    if ((networkId = lookupConfiguredNetwork(config)) != -1) {
                        config.networkId = networkId;
                        Xlog.d(TAG, "update existing network: " + config.networkId);
                        mWifiManager.updateNetwork(config);
                    } else {
                        //add a configured AP, need to adjust already-exist AP's priority
                        if(Utils.isCmccLoad() && !mIsAutoPriority){
                            mNewPriorityOrder = config.priority;
                            config.priority = mConfiguredApCount+1-mNewPriorityOrder+1;
                        }
                        if(networkId!=-1){
                            mConfigs = mWifiManager.getConfiguredNetworks();
                            mConfiguredApCount = mConfigs==null?0:mConfigs.size();
                            adjustPriority();
                        }
                    }
//MTK_OP01_PROTECT_END  
                    if (configController.isEdit() || requireKeyStore(config)) {
                        saveNetwork(config);
                    } else {
                        mWifiManager.connectNetwork(config);
                    }
                }
                break;
        }

        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
        updateAccessPoints();
    }

    private void saveNetwork(WifiConfiguration config) {
        if (mInXlSetupWizard) {
            ((WifiSettingsForSetupWizardXL)getActivity()).onSaveNetwork(config);
        } else {
            mWifiManager.saveNetwork(config);
        }
    }

    /* package */ void forget() {
        mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);

        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
        updateAccessPoints();

        // We need to rename/replace "Next" button in wifi setup context.
        changeNextButtonState(false);
//MTK_OP01_PROTECT_START        
        //since we lost a configured AP, left ones priority need to be refreshed
        mConfigs = mWifiManager.getConfiguredNetworks();
        mConfiguredApCount = mConfigs==null?0:mConfigs.size();
        updatePriority();
 //MTK_OP01_PROTECT_END
    }

    /**
     * Refreshes acccess points and ask Wifi module to scan networks again.
     */
    /* package */ void refreshAccessPoints() {
        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
//MTK_OP01_PROTECT_START
        if(Utils.isCmccLoad()){
            emptyCategory();
        }else
//MTK_OP01_PROTECT_END
        {
            getPreferenceScreen().removeAll();
        }
    }

    /**
     * Called when "add network" button is pressed.
     */
    /* package */ void onAddNetworkPressed() {
        // No exact access point is selected.
        mSelectedAccessPoint = null;
        showConfigUi(null, true);
    }
    public void onAddNetworkPressedForCmcc(){
//MTK_OP01_PROTECT_START
        if(Utils.isCmccLoad()){
            mSelectedAccessPoint = null;
            showConfigUi(null, true);
        }
//MTK_OP01_PROTECT_END
    }
    /* package */ int getAccessPointsCount() {
        final boolean wifiIsEnabled = mWifiManager.isWifiEnabled();
        if (wifiIsEnabled) {
//MTK_OP01_PROTECT_START
            if(Utils.isCmccLoad()){
                return mCmccTrustAP.getPreferenceCount()+mCmccConfigedAP.getPreferenceCount()
                    +mCmccNewAP.getPreferenceCount();
            }else
//MTK_OP01_PROTECT_END
            {
                return getPreferenceScreen().getPreferenceCount();
            }
        } else {
            return 0;
        }
    }

    /**
     * Requests wifi module to pause wifi scan. May be ignored when the module is disabled.
     */
    /* package */ void pauseWifiScan() {
        if (mWifiManager.isWifiEnabled()) {
            mScanner.pause();
        }
    }

    /**
     * Requests wifi module to resume wifi scan. May be ignored when the module is disabled.
     */
    /* package */ void resumeWifiScan() {
        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
    }
//MTK_OP01_PROTECT_START
    /**
     * disconnect from current connected AP
     */
    private void disconnect(int networkId){
        Xlog.d(TAG, "disconnect() from current active AP");
        //if user adjust AP's priority manually, we will not reset each AP's priority
        if(mIsAutoPriority){
//MTK_OP01_PROTECT_START
            if(Utils.isCmccLoad()){
                setDisconnectAPPriority(mCmccTrustAP);
                setDisconnectAPPriority(mCmccConfigedAP);
                setDisconnectAPPriority(mCmccNewAP);
            }else
//MTK_OP01_PROTECT_END
            {
                //priority=0 means being disconnect right now, then former disconnected AP 
                //should have higher priority to be re-connected
                for (int i = getPreferenceScreen().getPreferenceCount() - 1; i >= 0; --i) {
                    AccessPoint accessPoint = (AccessPoint) getPreferenceScreen().getPreference(i);
                    if (accessPoint!=null && accessPoint.networkId != -1 && accessPoint.getConfig().priority<
                            WIFI_AP_MAX_ALLOWED_PRIORITY) {
                        Xlog.d(TAG, "disconnect(), network id="+accessPoint.networkId+", priority="+accessPoint.getConfig().priority);
                        WifiConfiguration config = new WifiConfiguration();
                        config.networkId = accessPoint.networkId;
                        config.priority=accessPoint.getConfig().priority+1;
                        mWifiManager.updateNetwork(config);
                    }
                }
            }
            //set current connected AP's priority to 0 but not disable it, just take effect for auto connect
            WifiConfiguration config = new WifiConfiguration();
            config.networkId = networkId;
            config.priority = 0;
            mWifiManager.updateNetwork(config);
        }
        //user will connect AP manually, so disable this AP
/*        if(mConnectType != Settings.System.WIFI_CONNECT_TYPE_AUTO){
            mWifiManager.disableNetwork(networkId);
        }
*/
        saveNetworks();
        
        mWifiManager.disconnect();
    }
  
    ContentObserver priorityObserver = new ContentObserver(new Handler()){
        @Override
        public void onChange(boolean newValue){
            mLastConnectedConfig=null;
            mIsAutoPriority = Settings.System.getInt(getContentResolver(),Settings.System.WIFI_PRIORITY_TYPE,
                    Settings.System.WIFI_PRIORITY_TYPE_DEFAULT)==Settings.System.WIFI_PRIORITY_TYPE_DEFAULT;
            //if change from manually priority to auto priority, current connected AP will have highest priority(100010)
            if(mIsAutoPriority){
                WifiInfo currentConnInfo = mWifiManager.getConnectionInfo();
                if(currentConnInfo!=null){
                    int curNetworkId = currentConnInfo.getNetworkId();
                    for(int i=0;i<mConfiguredApCount;i++){
                        WifiConfiguration config = mConfigs.get(i);
                        if(config!=null && config.networkId==curNetworkId){
                            config.priority=WIFI_AP_MAX_ALLOWED_PRIORITY+10;
                            updateConfig(config);
                        }
                    }
                }
            }
            updatePriority();
        }
    };
/*    
    ContentObserver connectTypeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean newValue){
            Xlog.d(TAG, "### Auto connect type have changed");
            mConnectType = Settings.System.getInt(getContentResolver(), Settings.System.WIFI_CONNECT_TYPE, 
                    Settings.System.WIFI_CONNECT_TYPE_AUTO );
            //connect type transfer to manually, disable all configured network
            if(mConnectType!=Settings.System.WIFI_CONNECT_TYPE_AUTO){
                mConfigs = mWifiManager.getConfiguredNetworks();
                mConfiguredApCount = mConfigs==null?0:mConfigs.size();
                WifiInfo activeInfo = mWifiManager.getConnectionInfo();
                String activeSSID = null;
                if(activeInfo!=null){
                    activeSSID = activeInfo.getSSID();
                }
                if(mConfigs!=null){
                    for(int i=mConfigs.size()-1;i>=0;i--){
                        WifiConfiguration config = mConfigs.get(i);
                        if(activeSSID!=null && config.SSID!=null 
                                && activeSSID.equals(AccessPoint.removeDoubleQuotes(config.SSID))){
                            Xlog.d(TAG, "["+config.SSID+"] is current connnected AP, don't disable it");
                        }else{
                            mWifiManager.disableNetwork(config.networkId);
                        }
                    }
                }
            }
            saveNetworks();
            if(mConnectType==Settings.System.WIFI_CONNECT_TYPE_AUTO){
                mWifiManager.reconnect();
            }
        }
    };
*/    
    /**
     * give each access point its right priority value
     */
    private void updatePriority(){
        Xlog.d(TAG, "updatePriority(), configured AP count="+(mConfigs==null?0:mConfigs.size())+"  or "+mConfiguredApCount);
        mIsAutoPriority = Settings.System.getInt(getContentResolver(),Settings.System.WIFI_PRIORITY_TYPE,
                Settings.System.WIFI_PRIORITY_TYPE_DEFAULT)==Settings.System.WIFI_PRIORITY_TYPE_DEFAULT;
        if(Utils.isCmccLoad() && mConfigs != null){
            if(!mIsAutoPriority){ //set priority manually
                //To manually set priority, access point's priority order should not change
                if(mLastConnectedConfig!=null){
                    for(int i=0;i<mConfiguredApCount;i++){
                        WifiConfiguration config = mConfigs.get(i);
                        if(config!=null && config.SSID != null && config.SSID.equals(mLastConnectedConfig.SSID)){
                            config.priority=mLastConnectedConfig.priority;
                            break;
                        }
                    }
                    mLastConnectedConfig=null;//just take effect once
                }
                mPriorityOrder = WifiPrioritySettings.calculateInitPriority(mConfigs);
                //adjust priority order of each AP
                for(int i=0;i<mConfiguredApCount;i++){
                    WifiConfiguration config = mConfigs.get(i);
                    if(config.priority!=mConfiguredApCount-mPriorityOrder[i]+1){
                        config.priority=mConfiguredApCount-mPriorityOrder[i]+1;
                        updateConfig(config);
                    }
                }
            }else{
                for(int i=0;i<mConfiguredApCount;i++){
                    WifiConfiguration config = mConfigs.get(i);
                    if(config==null){
                        continue;
                    }
                    if(mLastConnectedConfig==null){
                        Xlog.d(TAG, "updatePriority(), mLastConnectedConfig==null");
                    }else{
                        Xlog.d(TAG, "updatePriority(), mLastConnectedConfig.networkId="+mLastConnectedConfig.SSID+" -- "+mLastConnectedConfig.networkId);
                        Xlog.d(TAG, "config.networkId="+config.SSID+"--"+config.networkId);
                    }
                    if(config.priority==WIFI_AP_MAX_ALLOWED_PRIORITY+10 
                            && (mLastConnectedConfig!=null && mLastConnectedConfig.networkId!=config.networkId)){
                        //This is the former connected AP, should set its priority to mLastPriority
                        config.priority = ++mLastPriority;
                        //judge whether if it's CMCC AP
                        String ssidStr = (config.SSID == null ? "" : AccessPoint.removeDoubleQuotes(config.SSID));
                        if(ssidStr.equals(AccessPoint.CMCC_SSID)){
                            config.priority = WIFI_AP_MAX_ALLOWED_PRIORITY+2;
                        }else if(ssidStr.equals(AccessPoint.CMCC_EDU_SSID)){
                            config.priority = WIFI_AP_MAX_ALLOWED_PRIORITY+1;
                        }
                    }else if(config.priority==WIFI_AP_MAX_ALLOWED_PRIORITY+20){
                        //This is the AP which is just connected, set its priority to WIFI_AP_MAX_ALLOWED_PRIORITY+10
                        config.priority = WIFI_AP_MAX_ALLOWED_PRIORITY+10;
                    }else{
                        //judge whether if it's CMCC AP
                        String ssidStr = (config.SSID == null ? "" : AccessPoint.removeDoubleQuotes(config.SSID));
                        if(ssidStr.equals(AccessPoint.CMCC_SSID)){
                            if(config.priority<WIFI_AP_MAX_ALLOWED_PRIORITY)config.priority = WIFI_AP_MAX_ALLOWED_PRIORITY+2;
                        }else if(ssidStr.equals(AccessPoint.CMCC_EDU_SSID)){
                            if(config.priority<WIFI_AP_MAX_ALLOWED_PRIORITY)config.priority = WIFI_AP_MAX_ALLOWED_PRIORITY+1;
                        }
                    }
                    updateConfig(config);
                }
            }
            mWifiManager.saveAPPriority();
        }
    }
    
    /**
     * Adjust other access point's priority if one of them changed
     */
    public void adjustPriority(){
        Xlog.d(TAG, "adjustPriority(), mOldPriorityOrder="+mOldPriorityOrder+", mNewPriorityOrder="+mNewPriorityOrder);
        if(!Utils.isCmccLoad() || mIsAutoPriority){
            Xlog.d(TAG, "For non-CMCC project or set priority automatically, priority can not be set manually");
            return;
        }
        if(mOldPriorityOrder == mNewPriorityOrder){
            Xlog.d(TAG, "AP priority does not change, keep ["+mOldPriorityOrder+"]");
            return;
        }
        if(mConfigs!=null && mPriorityOrder!=null){
            if(mOldPriorityOrder>mNewPriorityOrder){
                //selected AP will have a higher priority, but smaller order
                for(int i=0;i<mPriorityOrder.length;i++){
                    WifiConfiguration config = mConfigs.get(i);
                    if(mPriorityOrder[i]>=mNewPriorityOrder && mPriorityOrder[i]<mOldPriorityOrder){
                        mPriorityOrder[i]++;
                        config.priority = mConfiguredApCount-mPriorityOrder[i]+1;
                        updateConfig(config);
                    }else if(mPriorityOrder[i]==mOldPriorityOrder){
                        mPriorityOrder[i]=mNewPriorityOrder;
                        config.priority = mConfiguredApCount-mNewPriorityOrder+1;
                        updateConfig(config);
                    }
                }
            }else{
                //selected AP will have a lower priority, but bigger order
                for(int i=0;i<mPriorityOrder.length;i++){
                    WifiConfiguration config = mConfigs.get(i);
                    if(mPriorityOrder[i]<=mNewPriorityOrder && mPriorityOrder[i]>mOldPriorityOrder){
                        mPriorityOrder[i]--;
                        config.priority = mConfiguredApCount-mPriorityOrder[i]+1;
                        updateConfig(config);
                    }else if(mPriorityOrder[i]==mOldPriorityOrder){
                        mPriorityOrder[i]=mNewPriorityOrder;
                        config.priority = mConfiguredApCount-mNewPriorityOrder+1; 
                        updateConfig(config);
                    }
                }
            }
        }
    }

    private void updateConfig(WifiConfiguration config){
        if(config==null){
            return;
        }
        WifiConfiguration newConfig = new WifiConfiguration();
        newConfig.networkId = config.networkId;
        newConfig.priority = config.priority;
        mWifiManager.updateNetwork(newConfig);
    }
    private void saveNetworks() {
        // Always save the configuration with all networks enabled.
        mWifiManager.saveConfiguration();
        updateAccessPoints();
    }
    /**
     * Check the new profile against the configured networks. If none existing
     * is matched, return -1.
     * */
    private int lookupConfiguredNetwork(WifiConfiguration newProfile) {
        mConfigs = mWifiManager.getConfiguredNetworks();
        mConfiguredApCount = mConfigs==null?0:mConfigs.size();
        if (mConfigs != null) {
	    //add null judgement to avoid NullPointerException
            for (WifiConfiguration config : mConfigs) {
                if (config!=null && config.SSID!= null && config.SSID.equals(newProfile.SSID) &&
                    config.allowedAuthAlgorithms!=null && config.allowedAuthAlgorithms.equals(newProfile.allowedAuthAlgorithms) &&
                    config.allowedKeyManagement!=null && config.allowedKeyManagement.equals(newProfile.allowedKeyManagement)) {
                    if(AccessPoint.isWFATestSupported()){
                        if(config.allowedPairwiseCiphers!=null && !config.allowedPairwiseCiphers.equals(newProfile.allowedPairwiseCiphers)){
                            return -1;
                        }
                    }
                    return config.networkId;
                }
            }
        }
        return -1;
    }
//MTK_OP01_PROTECT_END   
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        if(isResumed() && mDialog!=null){
            mDialog.setDialogWidth();
        }
    }
//MTK_OP01_PROTECT_START
    public void emptyCategory(){
        getPreferenceScreen().addPreference(mCmccTrustAP);
        getPreferenceScreen().addPreference(mCmccConfigedAP);
        getPreferenceScreen().addPreference(mCmccNewAP);
        mCmccTrustAP.removeAll();
        mCmccConfigedAP.removeAll();
        mCmccNewAP.removeAll();
    }
    public void updateAP(PreferenceCategory screen){
        for (int i = screen.getPreferenceCount() - 1; i >= 0; --i) {
            // Maybe there's a WifiConfigPreference
            Preference preference = screen.getPreference(i);
            if (preference instanceof AccessPoint) {
                final AccessPoint accessPoint = (AccessPoint) preference;
                accessPoint.update(mLastInfo, mLastState);
            }
        }
    }
    public void setDisconnectAPPriority(PreferenceCategory screen){
        //priority=0 means being disconnect right now, then former disconnected AP 
        //should have higher priority to be re-connected
        for (int i = screen.getPreferenceCount() - 1; i >= 0; --i) {
            AccessPoint accessPoint = (AccessPoint)screen.getPreference(i);
            if (accessPoint!=null && accessPoint.networkId != -1 && accessPoint.getConfig().priority < 
                    WIFI_AP_MAX_ALLOWED_PRIORITY) {
                Xlog.d(TAG, "disconnect(), network id="+accessPoint.networkId+", priority="+accessPoint.getConfig().priority);
                WifiConfiguration config = new WifiConfiguration();
                config.networkId = accessPoint.networkId;
                config.priority=accessPoint.getConfig().priority+1;
                mWifiManager.updateNetwork(config);
            }
        }
    }
//MTK_OP01_PROTECT_END  
}
