package com.android.settings.deviceinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Telephony.SIMInfo;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.gemini.GeminiNetworkSubUtil;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.R;

import com.mediatek.featureoption.FeatureOption;
import com.mediatek.CellConnService.CellConnMgr;
import com.mediatek.xlog.Xlog;

public class SimStatusGemini extends PreferenceActivity{

    private static final String KEY_SIGNAL_STRENGTH = "signal_strength";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_NETWORK_TYPE = "network_type";
    private static final String KEY_DATA_STATE = "data_state";
    private static final String KEY_SERVICE_STATE = "service_state";
    private static final String KEY_ROAMING_STATE = "roaming_state";
    private static final String KEY_OPERATOR_NAME = "operator_name";

    private static final int EVENT_SIGNAL_STRENGTH_CHANGED = 200;
    private static final int EVENT_SERVICE_STATE_CHANGED = 300;
    private GeminiPhone mGeminiPhone = null;
    private static Resources mRes;
    private Preference mSignalStrengthPreference;
    
    private SignalStrength mSignalStrength;
    private SignalStrength mSignalStrengthGemini;

    private static String sUnknown;
    
    private TelephonyManager mTelephonyManager;
    
    //SimId, get from the intent extra
    private static int mSimId = 0;
    private boolean mIsUnlockFollow= false;
    private boolean mIsShouldBeFinished= false;
    
    private static final String TAG = "Gemini_SimStatus";

    private int mServiceState;
    private Handler mHandler = new Handler();

    private final BroadcastReceiver mReceiver = new AirplaneModeBroadcastReceiver();
// unlock sim pin/ me lock
    private Runnable serviceComplete = new Runnable(){
        public void run(){
            int nRet = mCellMgr.getResult();
             if (mCellMgr.RESULT_OK != nRet && mCellMgr.RESULT_STATE_NORMAL != nRet){
                 Xlog.d(TAG, "mCell Mgr Result is not OK");
                 mIsShouldBeFinished = true;
                 SimStatusGemini.this.finish();
                 return;
             }

             mIsUnlockFollow = false;
        }
    };
    // create unlock object
    private CellConnMgr mCellMgr;// = new CellConnMgr(serviceComplete);
    //related to mobile network type and mobile network state
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            updateDataState();
            updateNetworkType();
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength){
            if(signalStrength.getMySimId() == mSimId){
                mSignalStrength = signalStrength;
                updateSignalStrength(mSimId);
                Xlog.d(TAG, "message mGeminiPhone number is : " + signalStrength.getGsmSignalStrength() + " MySimId is" + signalStrength.getMySimId());
            }
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState){
            if(serviceState.getMySimId() == mSimId){
                mServiceState = serviceState.getState();
                updateServiceState(serviceState);
                updateSignalStrength(mSimId);
            }
        }
    };
    
    private PhoneStateListener mPhoneStateListenerGemini = new PhoneStateListener() {

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            updateDataState();
            updateNetworkType();
        }
        
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength){
            if(signalStrength.getMySimId() == mSimId){
                mSignalStrengthGemini = signalStrength;
                updateSignalStrength(mSimId);
                Xlog.d(TAG, "message mGeminiPhone number is : " + signalStrength.getGsmSignalStrength() + " MySimId is" + signalStrength.getMySimId());
            }
        }
        @Override
        public void onServiceStateChanged(ServiceState serviceState){
            if(serviceState.getMySimId() == mSimId){
                mServiceState = serviceState.getState();
                updateServiceState(serviceState);
                updateSignalStrength(mSimId);
            }
        }
    };
    

    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mCellMgr = new CellConnMgr(serviceComplete);
        mCellMgr.register(this);
        addPreferencesFromResource(R.xml.device_info_sim_status_gemini);
        
        //get the correct simId according to the intent extra
        Intent it = getIntent();
        mSimId = it.getIntExtra("slotid", -1);
        Xlog.d(TAG, "mSimId is : " + mSimId);
        
        SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(this, mSimId);
        int simCount = SIMInfo.getInsertedSIMCount(this);
        String simDisplayName = null;
        if(simCount > 1 && simInfo != null){
            simDisplayName = simInfo.mDisplayName;
        }
        if(simDisplayName != null && !simDisplayName.equals("")){
            setTitle(simDisplayName);
        }

        mRes = getResources();
        sUnknown = mRes.getString(R.string.device_info_default);
        mTelephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        
        mGeminiPhone = (GeminiPhone)PhoneFactory.getDefaultPhone();        
        
        // Note - missing in zaku build, be careful later...
        mSignalStrengthPreference = findPreference(KEY_SIGNAL_STRENGTH);
    }
    @Override
    protected void onDestroy(){
        mCellMgr.unregister();
        super.onDestroy();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if(mIsShouldBeFinished) {
            finish();
            return;
        }
        if(!mIsUnlockFollow) {
            mIsUnlockFollow = true;
            mHandler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    mCellMgr.handleCellConn(SimStatusGemini.this.mSimId, CellConnMgr.REQUEST_TYPE_SIMLOCK);
                }
            },500);
        }
        IntentFilter intentFilter =
            new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
        registerReceiver(mReceiver, intentFilter);
        //related to my phone number
        String rawNumber = mGeminiPhone.getLine1NumberGemini(mSimId);  // may be null or empty
        String formattedNumber = null;
        if (!TextUtils.isEmpty(rawNumber)) {
            formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);
        }
        // If formattedNumber is null or empty, it'll display as "Unknown".
        setSummaryText(KEY_NUMBER, formattedNumber);

        //after registerIntent, it will receive the message, so do not need to update signalStrength and service state
        updateDataState();
        ServiceState serviceState = mGeminiPhone.getServiceStateGemini(mSimId);
        updateServiceState(serviceState);
        mServiceState = serviceState.getState();
        SignalStrength signalStrength = mGeminiPhone.getSignalStrengthGemini(mSimId);
        if (mSimId == Phone.GEMINI_SIM_1) {
            mSignalStrength = signalStrength;
        } else if (mSimId == Phone.GEMINI_SIM_2) {
            mSignalStrengthGemini = signalStrength;
        }
        updateSignalStrength(mSimId);
        if(true == FeatureOption.MTK_GEMINI_SUPPORT){
            mTelephonyManager.listenGemini(mPhoneStateListener, 
                  PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                   | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                   | PhoneStateListener.LISTEN_SERVICE_STATE, Phone.GEMINI_SIM_1);
            mTelephonyManager.listenGemini(mPhoneStateListenerGemini, 
                    PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                     | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                     | PhoneStateListener.LISTEN_SERVICE_STATE, Phone.GEMINI_SIM_2);
        }
        else{
            mTelephonyManager.listen(mPhoneStateListener,
                      PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                      | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                      | PhoneStateListener.LISTEN_SERVICE_STATE);
        }

    }
    
    @Override
    public void onPause() {
        super.onPause();
        if(mIsShouldBeFinished) {
            // this is add for CR 64523 by mtk80800
            finish();
            return;
        }
        unregisterReceiver(mReceiver);
        if(true == FeatureOption.MTK_GEMINI_SUPPORT){
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            mTelephonyManager.listen(mPhoneStateListenerGemini, PhoneStateListener.LISTEN_NONE);
            
        }
        else{
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
 
    /**
     * @param preference The key for the Preference item
     * @param property The system property to fetch
     * @param alt The default value, if the property doesn't exist
     */
    private void setSummary(String preference, String property, String alt) {
        try {
            String strSummary = SystemProperties.get(property, alt);

            //replace the "unknown" result with the string resource for MUI
            Preference p = findPreference(preference);
            if ( p != null) {
                p.setSummary(
                        (strSummary.equals("unknown") == true)?sUnknown:strSummary);
            }

        } catch (RuntimeException e) {
            
        }
    }
    
    private void setSummaryText(String preference, String text) {
        if (TextUtils.isEmpty(text)) {
           text = this.getResources().getString(R.string.device_info_default);
         }
         // some preferences may be missing
        Preference p = findPreference(preference);
         if ( p != null) {
             p.setSummary(text);
         }
    }

    private void updateNetworkType() {
        // Whether EDGE, UMTS, etc...
        if(mSimId == Phone.GEMINI_SIM_1)
        {
            setSummary(KEY_NETWORK_TYPE, TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE, sUnknown);
        }
        else if(mSimId == Phone.GEMINI_SIM_2)
        {
            setSummary(KEY_NETWORK_TYPE, TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE_2, sUnknown);
        }
        else
        {
            setSummaryText(KEY_NETWORK_TYPE, sUnknown);
        }
    }

    private void updateDataState() {
        int state = TelephonyManager.DATA_DISCONNECTED;
        state = mTelephonyManager.getDataStateGemini(mSimId);
        
        String display = mRes.getString(R.string.radioInfo_unknown);
    
        switch (state) {
            case TelephonyManager.DATA_CONNECTED:
                display = mRes.getString(R.string.radioInfo_data_connected);
                break;
            case TelephonyManager.DATA_SUSPENDED:
                display = mRes.getString(R.string.radioInfo_data_suspended);
                break;
            case TelephonyManager.DATA_CONNECTING:
                display = mRes.getString(R.string.radioInfo_data_connecting);
                break;
            case TelephonyManager.DATA_DISCONNECTED:
                display = mRes.getString(R.string.radioInfo_data_disconnected);
                break;
        }
        
        setSummaryText(KEY_DATA_STATE, display);
    }

    private void updateServiceState(ServiceState serviceState) {
        
        int state = serviceState.getState();
        String display = mRes.getString(R.string.radioInfo_unknown);
        
        switch (state) {
            case ServiceState.STATE_IN_SERVICE:
                display = mRes.getString(R.string.radioInfo_service_in);
                break;
            case ServiceState.STATE_OUT_OF_SERVICE:
                display = mRes.getString(R.string.radioInfo_service_out);
            case ServiceState.STATE_EMERGENCY_ONLY:
                display = mRes.getString(R.string.radioInfo_service_emergency);
                break;
            case ServiceState.STATE_POWER_OFF:
                display = mRes.getString(R.string.radioInfo_service_off);
                break;
        }
        
        setSummaryText(KEY_SERVICE_STATE, display);
        
        if (serviceState.getRoaming()) {
            setSummaryText(KEY_ROAMING_STATE, mRes.getString(R.string.radioInfo_roaming_in));
        } else {
            setSummaryText(KEY_ROAMING_STATE, mRes.getString(R.string.radioInfo_roaming_not));
        }
        setSummaryText(KEY_OPERATOR_NAME, serviceState.getOperatorAlphaLong());
    }

    void updateSignalStrength(int simId) {
        // TODO PhoneStateIntentReceiver is deprecated and PhoneStateListener
        // should probably used instead.
        
        Xlog.d(TAG, "Enter updateSignalStrength function. simId is " + simId );
        SignalStrength signalStrength = null;

        if (simId == Phone.GEMINI_SIM_1) {
            signalStrength = mSignalStrength;
        } else if (simId == Phone.GEMINI_SIM_2) {
            signalStrength = mSignalStrengthGemini;
        }
        // not loaded in some versions of the code (e.g., zaku)
        if (mSignalStrengthPreference != null) {
            Resources r = getResources();
    
            int signalDbm = 0;
            int signalAsu = 0;
            /** M: CDMA phone signal strength,CR ALPS00361901 @{*/
            int signalDbmEvdo = 0;
            boolean isGsmSignal = true;
            /** @} */
            boolean isNeedUpdate = true;
            Xlog.d(TAG, "ServiceState in  updateSignalStrength function." + mServiceState);
            if ((ServiceState.STATE_OUT_OF_SERVICE ==  mServiceState) ||
                    (ServiceState.STATE_POWER_OFF ==  mServiceState)) {

                isNeedUpdate = true;
            } else {
               if (signalStrength != null) {
                    /** M: CDMA phone signal strength,CR ALPS00361901 @{*/
                    isGsmSignal = signalStrength.isGsm();
                    if (isGsmSignal) {
                     /** @} */
                        signalDbm = signalStrength.getGsmSignalStrengthDbm();
                        signalAsu = signalStrength.getGsmSignalStrength();
                    /** M: CDMA phone signal strength,CR ALPS00361901 @{*/
                    } else {
                        signalDbm = signalStrength.getCdmaDbm();
                        signalDbmEvdo = signalStrength.getEvdoDbm();
                    }
                   /** @} */
               }
               if (-1 == signalDbm || -1 == signalDbmEvdo || 99 == signalAsu) {
                    isNeedUpdate = false;
               }
                    
            }
            Xlog.d(TAG, "SignalStrength is:" + signalDbm + "dbm," + signalAsu + "asu");
            if (isNeedUpdate) {
                /** M: CDMA phone signal strength,CR ALPS00361901 @{*/
                if (isGsmSignal) {
                /** @} */
                    mSignalStrengthPreference.setSummary(String.valueOf(signalDbm) + " "
                            + r.getString(R.string.radioInfo_display_dbm) + "   "
                            + String.valueOf(signalAsu) + " "
                            + r.getString(R.string.radioInfo_display_asu));
                /** M: CDMA phone signal strength,CR ALPS00361901 @{*/
                } else {
                    mSignalStrengthPreference.setSummary(String.valueOf(signalDbm) + " "
                            + r.getString(R.string.radioInfo_display_dbm) + "   "
                            + String.valueOf(signalDbmEvdo) + " " 
                            + r.getString(R.string.evdo_signal_strength_info) + " "
                            + r.getString(R.string.radioInfo_display_dbm));
                }
                /** @} */
            }
        }
    }

    private class AirplaneModeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                boolean airplaneMode = intent.getBooleanExtra("state", false);
                if(airplaneMode)
                    mCellMgr.handleCellConn(mSimId,CellConnMgr.REQUEST_TYPE_SIMLOCK);
                } else if (Intent.ACTION_DUAL_SIM_MODE_CHANGED.equals(action)){
                int dualMode = intent.getIntExtra(Intent.EXTRA_DUAL_SIM_MODE,-1);
                if(dualMode == GeminiNetworkSubUtil.MODE_FLIGHT_MODE) {
                    mCellMgr.handleCellConn(mSimId,CellConnMgr.REQUEST_TYPE_SIMLOCK);
                } else if(dualMode != GeminiNetworkSubUtil.MODE_DUAL_SIM && dualMode != mSimId) {
                    mCellMgr.handleCellConn(mSimId,CellConnMgr.REQUEST_TYPE_SIMLOCK);
                }

            }
        }
    }
}
