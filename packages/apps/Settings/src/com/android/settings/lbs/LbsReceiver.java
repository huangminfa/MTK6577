package com.android.settings.lbs;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.android.internal.location.GpsNetInitiatedHandler;
import com.android.internal.telephony.Phone;
import com.android.settings.lbs.AgpsNotifyDialog;
import com.android.settings.R;
import com.mediatek.agps.MtkAgpsConfig;
import com.mediatek.agps.MtkAgpsManager;
import com.mediatek.agps.MtkAgpsProfile;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.mediatek.epo.MtkEpoClientManager;


public class LbsReceiver extends BroadcastReceiver {

    private static final String TAG = "Settings/LbsReceiver";
    private static final String XLOGTAG = "AgpsReceiver";
    private static final String PREFERENCE_FILE = "com.android.settings_preferences";
    
    public static final String ACTION_OMA_CP = "com.mediatek.omacp.settings";//add for omacp
    public static final String ACTION_OMA_CP_FEEDBACK = "com.mediatek.omacp.settings.result";
    public static final String ACTION_OMA_CP_CAPABILITY = "com.mediatek.omacp.capability";
    public static final String ACTION_OMA_CP_CAPABILITY_FEEDBACK = "com.mediatek.omacp.capability.result";
    public static final String APP_ID = "ap0004";
    
    public static final String EXTRA_APP_ID = "appId";
    private static final String EXTRA_SUPL = "supl";
    private static final String EXTRA_SUPL_PROVIDER_ID = "supl_provider_id";
    private static final String EXTRA_SUPL_SEVER_NAME = "supl_server_name";
    private static final String EXTRA_SUPL_SEVER_ADDRESS = "supl_server_addr";
    private static final String EXTRA_SUPL_SEVER_ADDRESS_TYPE = "supl_addr_type";
    private static final String EXTRA_SUPL_TO_NAPID = "supl_to_napid";

    private static final String EM_ENABLE_KEY = "EM_Indication";
    private static final String UNKNOWN_VALUE="UNKNOWN_VALUE";
    
    //add for OMACP
    public static final String ACTION_OMA_UP_FEEDBACK = "com.mediatek.omacp.settings.result";
    
    private static final int SLP_PORT = 7275;
    private static final int SLP_TTL = 1;
    private static final int SLP_SHOW_TYPE = 2;

    private static final int NO_SIM = 0;
    private static final int SINGLE_SIM_SIM1 = 1;
    private static final int SINGLE_SIM_SIM2 = 2;
    private static final int DUAL_SIM = 3;

    private MtkAgpsManager        mAgpsMgr = null;
    private MtkEpoClientManager   mEpoMgr = null;
    private SharedPreferences     mSharedPref;
    
    private String mCurOperatorCode_1, mCurOperatorCode_2;
	private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
    
    	mContext = context;
    	
        String action = intent.getAction();
        log("onReceive action=" + action);
        
        if(FeatureOption.MTK_AGPS_APP && FeatureOption.MTK_GPS_SUPPORT) {
            
            mSharedPref = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);

            //BroadcastReceiver will reset all of member after onReceive
            mAgpsMgr = (MtkAgpsManager)context.getSystemService(Context.MTK_AGPS_SERVICE);
            mEpoMgr = (MtkEpoClientManager)context.getSystemService(Context.MTK_EPO_CLIENT_SERVICE);

            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                handleBootCompleted(context, intent);
            } else if(action.equals(MtkAgpsManager.AGPS_PROFILE_UPDATE)) {
                handleAgpsProfileUpdate(context, intent);
            } else if(action.equals(MtkAgpsManager.AGPS_STATUS_UPDATE)) {
                handleAgpsStatusUpdate(context, intent);
            } else if(action.equals(MtkAgpsManager.AGPS_DISABLE_UPDATE)) {
                handleAgpsDisableUpdate(context, intent);
            } else if(action.equals(MtkAgpsManager.AGPS_OMACP_PROFILE_UPDATE)) {
                handleAgpsOmaProfileUpdate(context, intent);
            } else if(action.equals(MtkEpoClientManager.EPO_STATUS_UPDATE)) {
                handleEpoStatusUpdate(context, intent);
            } else if(action.equals(ACTION_OMA_CP)) {
                handleOmaCpSetting(context, intent);
            } else if(action.equals(ACTION_OMA_CP_CAPABILITY)) {
                handleOmaCpCapability(context, intent);
            }
        }
    }



    //TODO need refine the OMACP setting and capability
    private void handleBootCompleted(Context context, Intent intent) {
        SharedPreferences prefs;

        //============ A-GPS ============
        prefs = context.getSharedPreferences("agps_disable", 0);
        boolean disableAfterReboot = false;
        if(prefs.getBoolean("changed", false) == true) {
            disableAfterReboot = prefs.getBoolean("status", false);
        }
        log("disableAfterReboot=" + disableAfterReboot);
        
        prefs = context.getSharedPreferences("agps_profile", 0);
        if(prefs.getBoolean("changed", false) == true) {
            MtkAgpsProfile profile = new MtkAgpsProfile();
            profile.name = prefs.getString("name", null);
            profile.addr                = prefs.getString("addr", null);
            profile.backupSlpNameVar    = prefs.getString("backupSlpNameVar", null);
            profile.port                = prefs.getInt("port", 0);
            profile.tls                 = prefs.getInt("tls", 0);
            profile.showType            = prefs.getInt("showType", 0);
            profile.code                = prefs.getString("code", null);
            profile.addrType            = prefs.getString("addrType", null);
            profile.providerId          = prefs.getString("providerId", null);
            profile.defaultApn          = prefs.getString("defaultApn", null);
            profile.optionApn           = prefs.getString("optionApn", null);
            profile.optionApn2          = prefs.getString("optionApn2", null);
            profile.appId               = prefs.getString("appId", null);

            mAgpsMgr.setProfile(profile);
        }
        
        prefs = context.getSharedPreferences("agps_status", 0);
        if(prefs.getBoolean("changed", false) == true) {
            boolean status = prefs.getBoolean("status", false);
            if(status == true && disableAfterReboot == false) {
                mAgpsMgr.enable();
            } else {
                mAgpsMgr.disable();
            }

            int roaming     = prefs.getInt("roaming", 0);
            int molr        = prefs.getInt("molrPositionType", 0);
            int niEnable    = prefs.getInt("niEnable", 0);

            mAgpsMgr.setRoamingEnable((roaming==0)?false:true);
            mAgpsMgr.setUpEnable((molr==0)?true:false);
            mAgpsMgr.setNiEnable((niEnable==0)?false:true);

        } else {
            mAgpsMgr.extraCommand("USING_XML", null);
        }

        
        //============ EPO ============
        prefs = context.getSharedPreferences("epo_status", 0);
        if(prefs.getBoolean("changed", false) == true) {
            boolean status = prefs.getBoolean("status", false);
            boolean auto   = prefs.getBoolean("auto", false);

            log("status=" + status + " auto=" + auto);
            if(status) {
                mEpoMgr.enable();
            }
            if(auto) {
                mEpoMgr.enableAutoDownload(true);
            }
        } else {
            mEpoMgr.extraCommand("USING_XML", null);
        }
        
    }

    private void handleAgpsProfileUpdate(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String name         = bundle.getString("name");
        String addr         = bundle.getString("addr");
        String backup       = bundle.getString("backupSlpNameVar");
        int port            = bundle.getInt("port");
        int tls             = bundle.getInt("tls");
        int showType        = bundle.getInt("showType");
        String code         = bundle.getString("code");
        String addrType     = bundle.getString("addrType");
        String providerId   = bundle.getString("providerId");
        String defaultApn   = bundle.getString("defaultApn");
        String optionApn    = bundle.getString("optionApn");
        String optionApn2   = bundle.getString("optionApn2");
        String appId        = bundle.getString("appId");
        
        SharedPreferences prefs = context.getSharedPreferences("agps_profile", 0);
        prefs.edit()
            .putString("name", name)
            .putString("addr", addr)
            .putString("backupSlpNameVar", backup)
            .putInt("port", port)
            .putInt("tls", tls)
            .putInt("showType", showType)
            .putString("code", code)
            .putString("addrType", addrType)
            .putString("providerId", providerId)
            .putString("defaultApn", defaultApn)
            .putString("optionApn", optionApn)
            .putString("optionApn2", optionApn2)
            .putString("appId", appId)
            .putBoolean("changed", true)
            .commit();
    }

    private void handleAgpsStatusUpdate(Context context, Intent intent) {
        Bundle bundle   = intent.getExtras();
        boolean status  = bundle.getBoolean("status", false);
        int roaming     = bundle.getInt("roaming", 0);
        int molr        = bundle.getInt("molrPositionType", 0);
        int niEnable    = bundle.getInt("niEnable", 1);
        
        SharedPreferences prefs = context.getSharedPreferences("agps_status", 0);
        prefs.edit()
            .putBoolean("status", status)
            .putInt("roaming", roaming)
            .putInt("molrPositionType", molr)
            .putInt("niEnable", niEnable)
            .putBoolean("changed", true)
            .commit();
    }

    private void handleAgpsDisableUpdate(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        boolean status = bundle.getBoolean("status", false);
        
        SharedPreferences prefs = context.getSharedPreferences("agps_disable", 0);
        prefs.edit()
            .putBoolean("status", status)
            .putBoolean("changed", true)
            .commit();
    }

    private void handleAgpsOmaProfileUpdate(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String name = bundle.getString("name");
        String addr = bundle.getString("addr");
        String backup = bundle.getString("backupSlpNameVar");
        int port = bundle.getInt("port");
        int tls = bundle.getInt("tls");
        int showType = bundle.getInt("showType");
        String code = bundle.getString("code");
        String addrType = bundle.getString("addrType");
        String providerId = bundle.getString("providerId");
        String defaultApn = bundle.getString("defaultApn");
        
        SharedPreferences prefs = context.getSharedPreferences("omacp_profile", 0);
        prefs.edit()
            .putString("name", name)
            .putString("addr", addr)
            .putString("backupSlpNameVar", backup)
            .putInt("port", port)
            .putInt("tls", tls)
            .putInt("showType", showType)
            .putString("code", code)
            .putString("addrType", addrType)
            .putString("providerId", providerId)
            .putString("defaultApn", defaultApn)
            .putBoolean("changed", true)
            .commit();
    }

    private void handleEpoStatusUpdate(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        boolean status = bundle.getBoolean("status", false);
        boolean auto   = bundle.getBoolean("auto", false);
        
        SharedPreferences prefs = context.getSharedPreferences("epo_status", 0);
        prefs.edit()
            .putBoolean("status", status)
            .putBoolean("auto", auto)
            .putBoolean("changed", true)
            .commit();
    }

    private void handleOmaCpSetting(Context context, Intent intent) {
        
        if(!FeatureOption.MTK_OMACP_SUPPORT) {
            Xlog.i(TAG, XLOGTAG + "OMA CP in not supported by feature option, return");
            return;
        }
        Xlog.i(TAG, XLOGTAG + "get the OMA CP broadcast");
        String appId = intent.getStringExtra(EXTRA_APP_ID);
        if(appId == null || !appId.equals(APP_ID)){
            Xlog.i(TAG, XLOGTAG + "get the OMA CP broadcast, but it's not for AGPS");
            return;
        }

        int simId = intent.getIntExtra("simId", Phone.GEMINI_SIM_1);
        String providerId = intent.getStringExtra("PROVIDER-ID");
        String slpName = intent.getStringExtra("NAME");
        String defaultApn = "";
        String address = "";
        String addressType = "";
        String port = "";

        //try{
            Bundle bundle = intent.getExtras();
            ArrayList<HashMap<String, String>> appAddrMapList = (ArrayList<HashMap<String, String>>)bundle.get("APPADDR");
            if(appAddrMapList != null && appAddrMapList.size() > 0){
                HashMap<String, String> addrMap = appAddrMapList.get(0);
                if(addrMap != null){
                    address = addrMap.get("ADDR");
                    addressType = addrMap.get("ADDRTYPE");
                }
            }
            if(address == null || address.equals("")){
                Xlog.w(TAG, XLOGTAG + "invalid oma cp pushed supl address");
                //(Juan)
                dealWithOmaUpdataResult(false, "invalide oma cp pushed supl address");
                return;
            }
            //provider ID
            ArrayList<String> defaultApnList = (ArrayList<String>)bundle.get("TO-NAPID");
            if(defaultApnList != null && defaultApnList.size() > 0){
                defaultApn = defaultApnList.get(0);
            }

        Xlog.d(TAG, XLOGTAG + "current received omacp-pushed supl configuretion is");
        Xlog.d(TAG, XLOGTAG + "simId=" + simId + "providerId=" + providerId + "slpName=" + slpName + "defaultApn=" + defaultApn);
        Xlog.d(TAG, XLOGTAG + "address=" + address + "addre type=" + addressType);

        // initialize sim status.(Juan)
        initSIMStatus(FeatureOption.MTK_GEMINI_SUPPORT);

        ContentValues values = new ContentValues();
        //update value if exist.(Juan)
        String profileCode = "";
        if(simId == Phone.GEMINI_SIM_1){
            profileCode = mCurOperatorCode_1;
        }else if(simId == Phone.GEMINI_SIM_2){
            profileCode = mCurOperatorCode_2;
        }
        if(profileCode == null || "".equals(profileCode)){
        	dealWithOmaUpdataResult(false, "invalide profile code:" + profileCode);
            return;
        }
        Intent mIntent = new Intent(MtkAgpsManager.AGPS_OMACP_PROFILE_UPDATE);
        mIntent.putExtra("code", profileCode);
        mIntent.putExtra("addr", address);
        
        MtkAgpsProfile profile = new MtkAgpsProfile();
        profile.code = profileCode;
        profile.addr = address;

        if(providerId != null && !"".equals(providerId)){
            mIntent.putExtra("providerId", providerId);
            profile.providerId = providerId;
        }
        if(slpName != null && !"".equals(slpName)){
            mIntent.putExtra("name", slpName);
            profile.name = slpName;
            
            //use operator pushed name, cancel MUI name
            mIntent.putExtra("backupSlpNameVar", "");
            profile.backupSlpNameVar = "";
        }
        if(defaultApn != null && !"".equals(defaultApn)){
            mIntent.putExtra("defaultApn", defaultApn);
            profile.defaultApn = defaultApn;
        }
        if(addressType != null && !"".equals(addressType)){
            mIntent.putExtra("addrType", addressType);
            profile.addrType = addressType;
        }

        //because the TTL port is Fixed and the message doesn't include the information about port number, we fix it.
        mIntent.putExtra("port", SLP_PORT);
        profile.port = SLP_PORT;

        mIntent.putExtra("tls", SLP_TTL);
        profile.tls = SLP_TTL;

        mIntent.putExtra("showType", SLP_SHOW_TYPE);
        profile.showType = SLP_SHOW_TYPE;
        
        mContext.sendBroadcast(mIntent);
        mAgpsMgr.setProfile(profile);
        dealWithOmaUpdataResult(true, "OMA CP update successfully finished");
       
    }

    private void handleOmaCpCapability(Context context, Intent intent) {
        if(!FeatureOption.MTK_OMACP_SUPPORT){
            Xlog.d(TAG, XLOGTAG + "OMA CP in not supported by feature option-");
            return;
        }
        Xlog.i(TAG, XLOGTAG + "get OMA CP capability broadcast result");
        Intent it = new Intent();
        it.setAction(ACTION_OMA_CP_CAPABILITY_FEEDBACK);
        it.putExtra(EXTRA_APP_ID, APP_ID);
        it.putExtra(EXTRA_SUPL, true);
        it.putExtra(EXTRA_SUPL_PROVIDER_ID, false);
        it.putExtra(EXTRA_SUPL_SEVER_NAME, true);
        it.putExtra(EXTRA_SUPL_TO_NAPID, false);
        it.putExtra(EXTRA_SUPL_SEVER_ADDRESS, true);
        it.putExtra(EXTRA_SUPL_SEVER_ADDRESS_TYPE, false);

        Xlog.d(TAG, XLOGTAG + "feedback OMA CP capability information");
        context.sendBroadcast(it);
    }

    
    private void log(String info) {
        Xlog.d(TAG, info + " ");
    }
    
    private void loge(String info) {
        Xlog.e(TAG, info + " ");
    }
    
    /*Get current mobile network status*/
    private void initSIMStatus(boolean isGemini) {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        int sim_status_1 = -1;
        int sim_status_2 = -1;
        int mCurSIMStatus = NO_SIM;
        //add for OMA_CP
        mCurOperatorCode_1 = "";
        mCurOperatorCode_2 = "";

        if(isGemini){
            sim_status_1 = telephonyManager.getSimStateGemini(Phone.GEMINI_SIM_1);
            sim_status_2 = telephonyManager.getSimStateGemini(Phone.GEMINI_SIM_2);

            if(TelephonyManager.SIM_STATE_READY == sim_status_1
                    && TelephonyManager.SIM_STATE_READY == sim_status_2 )
            {
                mCurSIMStatus = DUAL_SIM;
                mCurOperatorCode_1 = telephonyManager.getSimOperatorGemini(Phone.GEMINI_SIM_1);
                mCurOperatorCode_2 = telephonyManager.getSimOperatorGemini(Phone.GEMINI_SIM_2);
            }else if (TelephonyManager.SIM_STATE_READY == sim_status_1
                    && TelephonyManager.SIM_STATE_READY != sim_status_2 ){
                mCurSIMStatus = SINGLE_SIM_SIM1;
                mCurOperatorCode_1 = telephonyManager.getSimOperatorGemini(Phone.GEMINI_SIM_1);
            }else if (TelephonyManager.SIM_STATE_READY != sim_status_1
                    && TelephonyManager.SIM_STATE_READY == sim_status_2 ){
                mCurSIMStatus = SINGLE_SIM_SIM2;
                mCurOperatorCode_2 = telephonyManager.getSimOperatorGemini(Phone.GEMINI_SIM_2);
            }
        }else{
            sim_status_1 = telephonyManager.getSimState();
            if(TelephonyManager.SIM_STATE_READY == sim_status_1){
                mCurSIMStatus = SINGLE_SIM_SIM1;
                mCurOperatorCode_1 = telephonyManager.getSimOperator();
            }
        }
		Xlog.d(XLOGTAG, TAG + "mCurSIMStatus is: " + mCurSIMStatus);
		Xlog.d(XLOGTAG, TAG + "sim1 card status is: " + sim_status_1);
		Xlog.d(XLOGTAG, TAG + "sim2 card status is: " + sim_status_2);   
		Xlog.d(XLOGTAG, TAG + "sim1 operator code is: "+ mCurOperatorCode_1);
		Xlog.d(XLOGTAG, TAG + "sim2 operator code is: "+ mCurOperatorCode_2);
    }
    
    /**
     * notify the result of dealing with OMA CP broadcast
     * @param success
     * @param message
     */

    private void dealWithOmaUpdataResult(boolean success, String message){
        Toast.makeText(mContext, "Deal with OMA CP operation: "+message, Toast.LENGTH_LONG).show();
		Xlog.d(XLOGTAG, TAG + "Deal with OMA UP operation: "+message);
        Intent it = new Intent();
        it.setAction(ACTION_OMA_UP_FEEDBACK);
        it.putExtra(EXTRA_APP_ID, APP_ID);
        it.putExtra("result", success);

        mContext.sendBroadcast(it);
    }
}
