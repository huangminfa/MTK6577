/**
 * 
 */
package com.android.settings.lbs;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageParser.NewPermissionInfo;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.util.Log;

import com.android.settings.R;
import com.android.internal.telephony.Phone;
import com.android.settings.SettingsPreferenceFragment;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.agps.MtkAgpsManager;
import com.mediatek.agps.MtkAgpsProfile;
import com.mediatek.agps.MtkAgpsProfileManager;
import com.mediatek.xlog.Xlog;

import dalvik.system.TemporaryDirectory;

public class AgpsSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    
    public static final String KEY_AGPS_SHARE = "agps_share";
    public static final String SIM_STATUS     = "sim_status";

    private static final String KEY_SELECT_PROFILE = "select_profile";
    
    private static final String KEY_SLP_ADDRESS     = "slp_address";
    private static final String KEY_PORT            = "port";
    private static final String KEY_TLS             = "tls";
    private static final String KEY_MOBILE_DATACONN = "mobile_dataConn";
    private static final String KEY_ABOUT_AGPS      = "about_agps";
    private static final String DISABLE_ON_REBOOT   = "disable_agps_on_reboot";
    private static final String NETWORK_INITIATE    = "Network_Initiate";
    //only local  or local + Roaming
    private static final String NETWORK_USED        = "Network_Used";

    private CheckBoxPreference mDisableOnRebootCB;
    private CheckBoxPreference mNetworkInitiateCB;
    private ListPreference mNetworkUsedListPref;
    
    private ListPreference mSelectProfileListPref;
    private Preference  mNetworkPref;
    private Preference  mAboutPref;
    
    private EditTextPreference mSLPAddressET;
    private EditTextPreference mPortET;
    private CheckBoxPreference mTLSCB;

    private String mOperatorCode;
    //used to describe current data connection status
    private String mDataConnItemTitle, mDataConnItemSummary;
    private boolean mIsGeminiPhone = FeatureOption.MTK_GEMINI_SUPPORT;
    
    private static final int ABOUT_AGPS_DIALOG_ID = 0;
    private static final int ROAMING_ALERT_DIALOG_ID = 1;
    
    private MtkAgpsManager      mAgpsMgr;
    private ConnectivityManager mConnMgr;
    private WifiManager         mWifiMgr;
    private TelephonyManager    mTelephonyMgr;

    private MtkAgpsProfileManager  mAgpsProfileManager = new MtkAgpsProfileManager();
    private MtkAgpsProfile      mDefaultProfile;
    
    //phone data connection state change listener
    private BroadcastReceiver mDataConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            
        	//update data connection title and summary
        	updateDataConnStatus();
            
            //update profile list
            initSlpProfileList();
            updateSlpProfile(mAgpsMgr.getProfile());

        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        log("onCreate");
        mAgpsProfileManager.updateAgpsProfile("/etc/agps_profiles_conf.xml");
        mDefaultProfile = mAgpsProfileManager.getDefaultProfile();
        
        mAgpsMgr = (MtkAgpsManager) getSystemService(Context.MTK_AGPS_SERVICE);
        mConnMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiMgr = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if(mAgpsMgr == null || mConnMgr == null || mWifiMgr == null || mTelephonyMgr == null) {
            log("ERR: getSystemService failed mAgpsMgr=" + mAgpsMgr + " mConnMgr=" + mConnMgr + 
                " mWifiMgr=" + mWifiMgr + " mTelephonyMgr=" + mTelephonyMgr);
            return;
        }
        
        addPreferencesFromResource(R.xml.agps_settings);
        initPreference();
        
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(mDataConnReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        log("onResume");
        updateDataConnStatus();
        initSlpProfileList();
        updatePage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mDataConnReceiver);
    }
    
    private void updatePage() {
        if(mAgpsMgr.getRoamingStatus()) {
            mNetworkUsedListPref.setSummary(R.string.Network_Local_and_Roaming_Summary);
            mNetworkUsedListPref.setValueIndex(1);
        } else {
            mNetworkUsedListPref.setSummary(R.string.Network_Only_Local_Summary);
            mNetworkUsedListPref.setValueIndex(0);
        }
        updateSlpProfile(mAgpsMgr.getProfile());
        mNetworkInitiateCB.setChecked(mAgpsMgr.getNiStatus());
    }
    
     /**
     * initiate profile list according to current SIM status
     */
    private void updateDataConnStatus() {        
        int sim_status_1 = -1;
        int sim_status_2 = -1; 
        
        mDataConnItemTitle = getString(R.string.MobileNetwork_DataConn_off);
        mDataConnItemSummary = getString(R.string.MobileNetwork_off_Summary);
        
        int networkType = -1;
        NetworkInfo networkInfo = mConnMgr.getActiveNetworkInfo();
        if(networkInfo != null){
            networkType = networkInfo.getType();
        } else {
            log("WARNING: no active network");
        }

        mOperatorCode = null;
        if(networkType == ConnectivityManager.TYPE_MOBILE) {
            if(mIsGeminiPhone) {
                
                sim_status_1 = mTelephonyMgr.getSimStateGemini(Phone.GEMINI_SIM_1);
                sim_status_2 = mTelephonyMgr.getSimStateGemini(Phone.GEMINI_SIM_2);

                log("sim1 status=" + sim_status_1);
                log("sim2 status=" + sim_status_2);

                if (TelephonyManager.SIM_STATE_READY == sim_status_1 
                        &&TelephonyManager.SIM_STATE_READY != sim_status_2 ) {
                    getMobileConnectionInfo(true, Phone.GEMINI_SIM_1);
                } else if ( TelephonyManager.SIM_STATE_READY != sim_status_1 
                        && TelephonyManager.SIM_STATE_READY == sim_status_2 ) {
                    getMobileConnectionInfo(true, Phone.GEMINI_SIM_2);
                } else if(TelephonyManager.SIM_STATE_READY == sim_status_1 
                        && TelephonyManager.SIM_STATE_READY == sim_status_2 ) {
                    if(!getMobileConnectionInfo(true, Phone.GEMINI_SIM_1)) {
                        getMobileConnectionInfo(true, Phone.GEMINI_SIM_2);
                    }
                }
            } else {
                sim_status_1 = mTelephonyMgr.getSimState();
                if(TelephonyManager.SIM_STATE_READY == sim_status_1){
                    //In this case ,it is the single card platform and the sim id is nonsense.
                    getMobileConnectionInfo(false, 0);
                }
            }         
        }else if(networkType == ConnectivityManager.TYPE_WIFI && mWifiMgr != null && networkInfo != null 
            && networkInfo.isConnected() && networkInfo.isAvailable()){
                mDataConnItemTitle = getString(R.string.WiFiNetwork_on_title);
                mDataConnItemSummary = getString(R.string.MobileNetwork_on_Summary);
        }

        mNetworkPref.setTitle(mDataConnItemTitle);
        mNetworkPref.setSummary(mDataConnItemSummary);
    }
    
    //when mobile connection is on ,get the profile list,data connection title and summary
    private boolean getMobileConnectionInfo(boolean IsGemini, int simid){
        if(IsGemini) {
            mOperatorCode = mTelephonyMgr.getSimOperatorGemini(simid);
            if(mTelephonyMgr.getDataStateGemini(simid) == TelephonyManager.DATA_CONNECTED) {
                mDataConnItemTitle = getString(R.string.MobileNetwork_SIM_Active, simid + 1);
                mDataConnItemSummary = getString(R.string.MobileNetwork_on_Summary);
                return true;
            }
        } else {
            mOperatorCode = mTelephonyMgr.getSimOperator();
            if(mTelephonyMgr.getDataState() == TelephonyManager.DATA_CONNECTED) {
                mDataConnItemTitle = getString(R.string.MobileNetwork_SIM_Active, "");
                mDataConnItemSummary = getString(R.string.MobileNetwork_on_Summary);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);
        
        if ((preference.getKey()).equals(NETWORK_INITIATE)) {
            CheckBoxPreference niCheckBox= (CheckBoxPreference)preference;
            mAgpsMgr.setNiEnable(niCheckBox.isChecked());
        } else if((preference.getKey()).equals(DISABLE_ON_REBOOT)) {
            Intent intent = new Intent(MtkAgpsManager.AGPS_DISABLE_UPDATE);
            intent.putExtra("status", mDisableOnRebootCB.isChecked());
            getActivity().sendBroadcast(intent);
        } else if (mAboutPref != null && mAboutPref.getKey().equals(preference.getKey())){
            showDialog(ABOUT_AGPS_DIALOG_ID);
        }
        return false;
    }

    private void initPreference() {
        
        mDisableOnRebootCB = (CheckBoxPreference)findPreference(DISABLE_ON_REBOOT);
        SharedPreferences prefs = getActivity().getSharedPreferences("agps_disable", 0);
        boolean disableAfterReboot = false;
        if(prefs.getBoolean("changed", false)== true) {
            disableAfterReboot = prefs.getBoolean("status", false);
        }
        mDisableOnRebootCB.setChecked(disableAfterReboot);

        mNetworkInitiateCB = (CheckBoxPreference)findPreference(NETWORK_INITIATE);
        
        mNetworkUsedListPref = (ListPreference)findPreference(NETWORK_USED);
        mNetworkUsedListPref.setOnPreferenceChangeListener(this);

        /*Address*/        
        mSLPAddressET = (EditTextPreference)findPreference(KEY_SLP_ADDRESS);
        mSLPAddressET.setEnabled(false);

        /*Port*/
        mPortET = (EditTextPreference)findPreference(KEY_PORT);
        mPortET.setEnabled(false);

        /*TLS*/
        mTLSCB = (CheckBoxPreference)findPreference(KEY_TLS);
        mTLSCB.setEnabled(false);

        //MobieNetwork data connection  
        mNetworkPref = (Preference)findPreference(KEY_MOBILE_DATACONN);
        
        //About A-GPS
        mAboutPref = (Preference)findPreference(KEY_ABOUT_AGPS);
        
        //Agps Profile list
        mSelectProfileListPref = (ListPreference)findPreference(KEY_SELECT_PROFILE);
        mSelectProfileListPref.setOnPreferenceChangeListener(this);
    }

    private void initSlpProfileList() {
        SharedPreferences prefs = getActivity().getSharedPreferences("omacp_profile", 0);
        if(prefs.getBoolean("changed", false) == true) {
            MtkAgpsProfile profile = new MtkAgpsProfile();
            profile.name                = prefs.getString("name", null);
            profile.addr                = prefs.getString("addr", null);
            profile.backupSlpNameVar    = prefs.getString("backupSlpNameVar", null);
            profile.port                = prefs.getInt("port", 0);
            profile.tls                 = prefs.getInt("tls", 0);
            profile.showType            = prefs.getInt("showType", 0);
            profile.code                = prefs.getString("code", null);
            profile.addrType            = prefs.getString("addrType", null);
            profile.defaultApn          = prefs.getString("defaultApn", null);
            profile.providerId          = prefs.getString("providerId", null);
            mAgpsProfileManager.insertProfile(profile);
        }

        

        log("opeator code " + mOperatorCode);
        List<MtkAgpsProfile> AvailableProfiles = new ArrayList<MtkAgpsProfile> ();
        
        List<MtkAgpsProfile> profiles = mAgpsProfileManager.getAllProfile();
        for(MtkAgpsProfile profile : profiles) {
        	if(profile.code.equals(mAgpsProfileManager.getDefaultProfile().code)) {
        		log("default profile code" + profile.code);
        		AvailableProfiles.add(profile);
        	} else if(profile.showType == 0) {
        		log("showType == 0 profile code" + profile.code);
        		AvailableProfiles.add(profile);
        	} else if(profile.showType == 2 && profile.code.equals(mOperatorCode)) {
        		log("showType == 2 profile code" + profile.code);
        		AvailableProfiles.add(profile);
        	}
        	
        }
        
        String entries[] = new String[AvailableProfiles.size()];
        String values[] = new String[AvailableProfiles.size()];
        int num = 0;
        for(MtkAgpsProfile profile : AvailableProfiles) {
        	entries[num] = profile.name;
        	values[num] = profile.code;
        	num++;
        }
        mSelectProfileListPref.setEntries(entries);
        mSelectProfileListPref.setEntryValues(values); 
        
        boolean flag = false;
        MtkAgpsProfile selectProfile = mAgpsMgr.getProfile();
        log("select profile code" + selectProfile.code);
        for(MtkAgpsProfile profile : AvailableProfiles) {
        	if(selectProfile.code.equals(profile.code)) {
        		flag = true;
        		break;
        	}
        }
        
        if(!flag) {
        	log("set current profile code" + mDefaultProfile.code);
        	mAgpsMgr.setProfile(mDefaultProfile);
        }
        
    }

    private void updateSlpProfile(MtkAgpsProfile selectProfile) {
        mSelectProfileListPref.setValue(selectProfile.code);
        mSelectProfileListPref.setSummary(selectProfile.name);
        
        mSLPAddressET.setText(selectProfile.addr);
        mSLPAddressET.setSummary(selectProfile.addr);
        
        mPortET.setText(String.valueOf(selectProfile.port));
        mPortET.setSummary(String.valueOf(selectProfile.port));
        
        mTLSCB.setChecked(1 == selectProfile.tls);
    }
    
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        
        final String key = preference.getKey();
        
        if (KEY_SELECT_PROFILE.equals(key)) {
            String code = objValue.toString();
            MtkAgpsProfile selectProfile = new MtkAgpsProfile();
            for(MtkAgpsProfile profile : mAgpsProfileManager.getAllProfile()) {
                if(profile.code.equals(code)) {
                    selectProfile = profile;
                    break;
                }
            }
            updateSlpProfile(selectProfile);
            mAgpsMgr.setProfile(selectProfile);
        }
        else if (mNetworkUsedListPref.getKey().equals(key)) {
            int index = mNetworkUsedListPref.findIndexOfValue(objValue.toString());
            if(index == 0) {
                mAgpsMgr.setRoamingEnable(false);
                updatePage();
            } else if(index == 1) {
                if(mAgpsMgr.getRoamingStatus() == false) {
                    showDialog(ROAMING_ALERT_DIALOG_ID);
                }
            }
        }
        
        return true;
    }
        
    public Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        if(id == ABOUT_AGPS_DIALOG_ID) {
            dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.about_agps_title)
                .setIcon(com.android.internal.R.drawable.ic_dialog_info)
                .setMessage(R.string.about_agps_message)
                .setPositiveButton(R.string.agps_OK, null)
                .create();
        }else if(id == ROAMING_ALERT_DIALOG_ID) {
            dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.Network_Roaming_dialog_title)
                .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                .setMessage(R.string.Network_Roaming_dialog_content)
                .setPositiveButton(R.string.agps_OK,
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        mAgpsMgr.setRoamingEnable(true);
                        updatePage();
                    }
                }).setNegativeButton(R.string.agps_enable_confirm_deny,
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        updatePage();
                    }
                })
                .create();
            updatePage();
        } else {
            log("WARNING: onCreateDialog unknown id recv");
        }
        return dialog;
    }
    
    private void log(String msg) {
        Log.d("hugo_app", "[AGPS Setting] " + msg);
    }
    
}

