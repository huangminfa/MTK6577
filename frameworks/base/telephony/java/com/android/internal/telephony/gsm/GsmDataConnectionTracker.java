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

package com.android.internal.telephony.gsm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkCapabilities;
import android.net.LinkProperties;
import android.net.LinkProperties.CompareResult;
import android.net.wifi.WifiManager;
import android.net.NetworkConfig;
import android.net.NetworkUtils;
import android.net.ProxyProperties;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;

import com.android.internal.telephony.ApnContext;
import com.android.internal.telephony.ApnSetting;
import com.android.internal.telephony.DataCallState;
import com.android.internal.telephony.DataConnection;
import com.android.internal.telephony.DataConnection.FailCause;
import com.android.internal.telephony.DataConnection.UpdateLinkPropertyResult;
import com.android.internal.telephony.DataConnectionAc;
import com.android.internal.telephony.DataConnectionTracker;
import com.android.internal.telephony.EventLogTags;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.RetryManager;
import com.android.internal.util.AsyncChannel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

//MTK-START [mtk04070][111205][ALPS00093395]MTK added
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.gemini.GeminiNetworkSubUtil;
import com.android.internal.telephony.gemini.GeminiPhone;
import android.content.BroadcastReceiver;
import com.mediatek.featureoption.FeatureOption;
import static android.provider.Settings.System.GPRS_CONNECTION_SETTING;
import static android.provider.Settings.System.GPRS_CONNECTION_SETTING_DEFAULT;
import android.provider.Telephony.SIMInfo;
import com.android.internal.telephony.PhoneFactory;
//MTK-END [mtk04070][111205][ALPS00093395]MTK added


/**
 * {@hide}
 */
public final class GsmDataConnectionTracker extends DataConnectionTracker {
    protected final String LOG_TAG = "GSM";
    private static final boolean RADIO_TESTS = false;

    private static final int MSG_RESTART_RADIO_OFF_DONE = 999;
    private static final int MSG_RESTART_RADIO_ON_DONE = 998;
    /**
     * Handles changes to the APN db.
     */
    private class ApnChangeObserver extends ContentObserver {
        public ApnChangeObserver () {
            super(mDataConnectionTracker);
        }

        @Override
        public void onChange(boolean selfChange) {
            sendMessage(obtainMessage(EVENT_APN_CHANGED));
        }
    }

    //***** Instance Variables

    private boolean mReregisterOnReconnectFailure = false;
    private ContentResolver mResolver;

    // Recovery action taken in case of data stall
    private static class RecoveryAction {
        public static final int GET_DATA_CALL_LIST      = 0;
        public static final int CLEANUP                 = 1;
        public static final int REREGISTER              = 2;
        public static final int RADIO_RESTART           = 3;
        public static final int RADIO_RESTART_WITH_PROP = 4;

        private static boolean isAggressiveRecovery(int value) {
            return ((value == RecoveryAction.CLEANUP) ||
                    (value == RecoveryAction.REREGISTER) ||
                    (value == RecoveryAction.RADIO_RESTART) ||
                    (value == RecoveryAction.RADIO_RESTART_WITH_PROP));
        }
    }

    public int getRecoveryAction() {
        int action = Settings.System.getInt(mPhone.getContext().getContentResolver(),
                "radio.data.stall.recovery.action", RecoveryAction.GET_DATA_CALL_LIST);
        if (VDBG) log("getRecoveryAction: " + action);
        return action;
    }
    public void putRecoveryAction(int action) {
        Settings.System.putInt(mPhone.getContext().getContentResolver(),
                "radio.data.stall.recovery.action", action);
        if (VDBG) log("putRecoveryAction: " + action);
    }

    //***** Constants

    private static final int POLL_PDP_MILLIS = 5 * 1000;

    private static final String INTENT_RECONNECT_ALARM =
        "com.android.internal.telephony.gprs-reconnect";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_TYPE = "type";

    private static final String INTENT_DATA_STALL_ALARM =
        "com.android.internal.telephony.gprs-data-stall";

    static final Uri PREFERAPN_NO_UPDATE_URI =
                        Uri.parse("content://telephony/carriers/preferapn_no_update");
    static final Uri PREFERAPN_NO_UPDATE_SIM2_URI = 
                        Uri.parse("content://telephony/carriers_gemini/preferapn_no_update");
    static final String APN_ID = "apn_id";
    private boolean canSetPreferApn = false;
    
    //MTK-BEGIN
    static final Uri PREFER_TETHERING_APN_URI = Uri.parse("content://telephony/carriers/prefertetheringapn");
    static final Uri PREFER_TETHERING_APN_SIM2_URI = Uri.parse("content://telephony/carriers_gemini/prefertetheringapn");
    private ApnSetting mPreferredTetheringApn = null;
    private boolean canSetPreferTetheringApn = false;
    private ArrayList<String> mWaitingApnList = new ArrayList<String>();
    static final Uri PREFERAPN_NO_UPDATE_URI_SIM1 =
                        Uri.parse("content://telephony/carriers_sim1/preferapn_no_update");
    static final Uri PREFERAPN_NO_UPDATE_URI_SIM2 = 
                        Uri.parse("content://telephony/carriers_sim2/preferapn_no_update");
    //MTK-END

    private static final boolean DATA_STALL_SUSPECTED = true;
    private static final boolean DATA_STALL_NOT_SUSPECTED = false;

    @Override
    protected void onActionIntentReconnectAlarm(Intent intent) {
        if (DBG) log("GPRS reconnect alarm. Previous state was " + mState);

        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            int reconnect_for_simId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);
            logd("GPRS reconnect alarm triggered by simId=" + reconnect_for_simId + ". Previous state was " + getState());  
            if (reconnect_for_simId != mGsmPhone.getMySimId()) {
                return;
            }
        } else {
            logd("GPRS reconnect alarm. Previous state was " + getState());
        }
        String reason = intent.getStringExtra(INTENT_RECONNECT_ALARM_EXTRA_REASON);
        int connectionId = intent.getIntExtra(INTENT_RECONNECT_ALARM_EXTRA_TYPE, -1);

        DataConnectionAc dcac= mDataConnectionAsyncChannels.get(connectionId);

        if (dcac != null) {
            for (ApnContext apnContext : dcac.getApnListSync()) {
                apnContext.setReason(reason);
                if (apnContext.getState() == State.FAILED) {
                    apnContext.setState(State.IDLE);
                }
                sendMessage(obtainMessage(EVENT_TRY_SETUP_DATA, apnContext));
            }
            // Alram had expired. Clear pending intent recorded on the DataConnection.
            dcac.setReconnectIntentSync(null);
        }
    }

    /** Watches for changes to the APN db. */
    private ApnChangeObserver mApnObserver;

    //MTK-START [mtk04070][111205][ALPS00093395]MTK added
    private GSMPhone mGsmPhone;

    //Add for SCRI, Fast Dormancy
    private ScriManager mScriManager;
    protected boolean scriPollEnabled = false;
    protected long scriTxPkts=0, scriRxPkts=0;

    private boolean mIsUmtsMode = false;
    private boolean mIsCallPrefer = false;

    //[ALPS00098656][mtk04070]Disable Fast Dormancy when in Tethered mode
    private boolean mIsTetheredMode = false;
    //MTK-END [mtk04070][111205][ALPS00093395]MTK added

    private static final int PDP_CONNECTION_POOL_SIZE = 3;

    //***** Constructor

    public GsmDataConnectionTracker(PhoneBase p) {
        super(p);

        //MTK-START [mtk04070][111205][ALPS00093395]MTK added
        mGsmPhone = (GSMPhone)p;
        //MTK-END [mtk04070][111205][ALPS00093395]MTK added

        p.mCM.registerForAvailable (this, EVENT_RADIO_AVAILABLE, null);
        p.mCM.registerForOffOrNotAvailable(this, EVENT_RADIO_OFF_OR_NOT_AVAILABLE, null);
        p.mIccRecords.registerForRecordsLoaded(this, EVENT_RECORDS_LOADED, null);
        p.mCM.registerForDataNetworkStateChanged (this, EVENT_DATA_STATE_CHANGED, null);
        p.getCallTracker().registerForVoiceCallEnded (this, EVENT_VOICE_CALL_ENDED, null);
        p.getCallTracker().registerForVoiceCallStarted (this, EVENT_VOICE_CALL_STARTED, null);
        p.getServiceStateTracker().registerForDataConnectionAttached(this,
                EVENT_DATA_CONNECTION_ATTACHED, null);
        p.getServiceStateTracker().registerForDataConnectionDetached(this,
                EVENT_DATA_CONNECTION_DETACHED, null);
        p.getServiceStateTracker().registerForRoamingOn(this, EVENT_ROAMING_ON, null);
        p.getServiceStateTracker().registerForRoamingOff(this, EVENT_ROAMING_OFF, null);
        p.getServiceStateTracker().registerForPsRestrictedEnabled(this,
                EVENT_PS_RESTRICT_ENABLED, null);
        p.getServiceStateTracker().registerForPsRestrictedDisabled(this,
                EVENT_PS_RESTRICT_DISABLED, null);
        mGsmPhone.mSST.registerForPsRegistrants(this, 
                EVENT_PS_RAT_CHANGED, null);

        // install reconnect intent filter for this data connection.
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_DATA_STALL_ALARM);
        //MTK START
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        //MTK END
        p.getContext().registerReceiver(mIntentReceiver, filter, null, p);

        mDataConnectionTracker = this;
        mResolver = mPhone.getContext().getContentResolver();

        mApnObserver = new ApnChangeObserver();
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            Uri geminiUri;
            if ("OP01".equals(SystemProperties.get("ro.operator.optr"))) {
                geminiUri = (Phone.GEMINI_SIM_2 == mPhone.getMySimId())? 
                        Telephony.Carriers.SIM2Carriers.CONTENT_URI : Telephony.Carriers.SIM1Carriers.CONTENT_URI;
            } else {
                geminiUri = (Phone.GEMINI_SIM_2 == mPhone.getMySimId())? 
                        Telephony.Carriers.GeminiCarriers.CONTENT_URI : Telephony.Carriers.CONTENT_URI;
            }
            p.getContext().getContentResolver().registerContentObserver(geminiUri, true, mApnObserver);
        } else {
            p.getContext().getContentResolver().registerContentObserver(
                    Telephony.Carriers.CONTENT_URI, true, mApnObserver);
        }

        mApnContexts = new ConcurrentHashMap<String, ApnContext>();
        initApnContextsAndDataConnection();
        broadcastMessenger();



        //MTK-START [mtk04070][111205][ALPS00093395]MTK added
        //Register for handling SCRI events
        registerSCRIEvent(mGsmPhone);
        //MTK-END [mtk04070][111205][ALPS00093395]MTK added
    }

    @Override
    public void dispose() {
        cleanUpAllConnections(false, null);

        super.dispose();

        //Unregister for all events
        mPhone.mCM.unregisterForAvailable(this);
        mPhone.mCM.unregisterForOffOrNotAvailable(this);
        mPhone.mIccRecords.unregisterForRecordsLoaded(this);
        mPhone.mCM.unregisterForDataNetworkStateChanged(this);
        mPhone.getCallTracker().unregisterForVoiceCallEnded(this);
        mPhone.getCallTracker().unregisterForVoiceCallStarted(this);
        mPhone.getServiceStateTracker().unregisterForDataConnectionAttached(this);
        mPhone.getServiceStateTracker().unregisterForDataConnectionDetached(this);
        mPhone.getServiceStateTracker().unregisterForRoamingOn(this);
        mPhone.getServiceStateTracker().unregisterForRoamingOff(this);
        mPhone.getServiceStateTracker().unregisterForPsRestrictedEnabled(this);
        mPhone.getServiceStateTracker().unregisterForPsRestrictedDisabled(this);
        mGsmPhone.mSST.unregisterForPsRegistrants(this);

        mPhone.getContext().getContentResolver().unregisterContentObserver(this.mApnObserver);
        mApnContexts.clear();
        //MTK
        mPhone.mCM.unSetGprsDetach(this);

        destroyDataConnections();
    }

    @Override
    public boolean isApnTypeActive(String type) {
        ApnContext apnContext = mApnContexts.get(type);
        if (apnContext == null) return false;

        return (apnContext.getDataConnection() != null);
    }

    @Override
    protected boolean isDataPossible(String apnType) {
        log("apnType = " + apnType);
        ApnContext apnContext = mApnContexts.get(apnType);
        if (apnContext == null) {
            return false;
        }
        boolean apnContextIsEnabled = apnContext.isEnabled();
        State apnContextState = apnContext.getState();
        boolean apnTypePossible = !(apnContextIsEnabled &&
                (apnContextState == State.FAILED));
        boolean dataAllowed = isDataAllowed();
        boolean possible = dataAllowed && apnTypePossible;

        if (DBG) {
            log(String.format("isDataPossible(%s): possible=%b isDataAllowed=%b " +
                    "apnTypePossible=%b apnContextisEnabled=%b apnContextState()=%s",
                    apnType, possible, dataAllowed, apnTypePossible,
                    apnContextIsEnabled, apnContextState));
        }
        return possible;
    }

    @Override
    protected void finalize() {
        if(DBG) log("finalize");
    }

    @Override
    protected String getActionIntentReconnectAlarm() {
        return INTENT_RECONNECT_ALARM;
    }

    @Override
    protected String getActionIntentDataStallAlarm() {
        return INTENT_DATA_STALL_ALARM;
    }

    private ApnContext addApnContext(String type) {
        ApnContext apnContext = new ApnContext(type, LOG_TAG);
        apnContext.setDependencyMet(false);
        mApnContexts.put(type, apnContext);
        return apnContext;
    }

    protected void initApnContextsAndDataConnection() {
        boolean defaultEnabled = SystemProperties.getBoolean(DEFALUT_DATA_ON_BOOT_PROP, true);
        // Load device network attributes from resources
        String[] networkConfigStrings = mPhone.getContext().getResources().getStringArray(
                com.android.internal.R.array.networkAttributes);
        for (String networkConfigString : networkConfigStrings) {
            NetworkConfig networkConfig = new NetworkConfig(networkConfigString);
            ApnContext apnContext = null;

            switch (networkConfig.type) {
            case ConnectivityManager.TYPE_MOBILE:
                apnContext = addApnContext(Phone.APN_TYPE_DEFAULT);
                apnContext.setEnabled(defaultEnabled);
                break;
            case ConnectivityManager.TYPE_MOBILE_MMS:
                apnContext = addApnContext(Phone.APN_TYPE_MMS);
                break;
            case ConnectivityManager.TYPE_MOBILE_SUPL:
                apnContext = addApnContext(Phone.APN_TYPE_SUPL);
                break;
            case ConnectivityManager.TYPE_MOBILE_DUN:
                apnContext = addApnContext(Phone.APN_TYPE_DUN);
                break;
            case ConnectivityManager.TYPE_MOBILE_HIPRI:
                apnContext = addApnContext(Phone.APN_TYPE_HIPRI);
                ApnContext defaultContext = mApnContexts.get(Phone.APN_TYPE_DEFAULT);
                if (defaultContext != null) {
                    applyNewState(apnContext, apnContext.isEnabled(),
                            defaultContext.getDependencyMet());
                } else {
                    // the default will set the hipri dep-met when it is created
                }
                continue;
            case ConnectivityManager.TYPE_MOBILE_FOTA:
                apnContext = addApnContext(Phone.APN_TYPE_FOTA);
                break;
            case ConnectivityManager.TYPE_MOBILE_IMS:
                apnContext = addApnContext(Phone.APN_TYPE_IMS);
                break;
            case ConnectivityManager.TYPE_MOBILE_CBS:
                apnContext = addApnContext(Phone.APN_TYPE_CBS);
                break;
            case ConnectivityManager.TYPE_MOBILE_DM:
                apnContext = addApnContext(Phone.APN_TYPE_DM);
                break;
            case ConnectivityManager.TYPE_MOBILE_NET:
                apnContext = addApnContext(Phone.APN_TYPE_NET);
                break;
            case ConnectivityManager.TYPE_MOBILE_WAP:
                apnContext = addApnContext(Phone.APN_TYPE_WAP);
                break;
            case ConnectivityManager.TYPE_MOBILE_CMMAIL:
                apnContext = addApnContext(Phone.APN_TYPE_CMMAIL);
                break;
            default:
                // skip unknown types
                continue;
            }
            if (apnContext != null) {
                // set the prop, but also apply the newly set enabled and dependency values
                onSetDependencyMet(apnContext.getApnType(), networkConfig.dependencyMet);
            }
        }
    }

    @Override
    protected LinkProperties getLinkProperties(String apnType) {
        ApnContext apnContext = mApnContexts.get(apnType);
        if (apnContext != null) {
            DataConnectionAc dcac = apnContext.getDataConnectionAc();
            if (dcac != null) {
                if (DBG) log("return link properites for " + apnType);
                return dcac.getLinkPropertiesSync();
            }
        }
        if (DBG) log("return new LinkProperties");
        return new LinkProperties();
    }

    @Override
    protected LinkCapabilities getLinkCapabilities(String apnType) {
        ApnContext apnContext = mApnContexts.get(apnType);
        if (apnContext!=null) {
            DataConnectionAc dataConnectionAc = apnContext.getDataConnectionAc();
            if (dataConnectionAc != null) {
                if (DBG) log("get active pdp is not null, return link Capabilities for " + apnType);
                return dataConnectionAc.getLinkCapabilitiesSync();
            }
        }
        if (DBG) log("return new LinkCapabilities");
        return new LinkCapabilities();
    }

    @Override
    // Return all active apn types
    public String[] getActiveApnTypes() {
        if (DBG) log("get all active apn types");
        ArrayList<String> result = new ArrayList<String>();

        for (ApnContext apnContext : mApnContexts.values()) {
            if (apnContext.isReady()) {
                result.add(apnContext.getApnType());
            }
        }

        return (String[])result.toArray(new String[0]);
    }

    @Override
    // Return active apn of specific apn type
    public String getActiveApnString(String apnType) {
        if (DBG) log( "get active apn string for type:" + apnType);
        ApnContext apnContext = mApnContexts.get(apnType);
        if (apnContext != null) {
            ApnSetting apnSetting = apnContext.getApnSetting();
            if (apnSetting != null) {
                return apnSetting.apn;
            }
        }
        return null;
    }

    @Override
    public boolean isApnTypeEnabled(String apnType) {
        ApnContext apnContext = mApnContexts.get(apnType);
        if (apnContext == null) {
            return false;
        }
        return apnContext.isEnabled();
    }

    @Override
    protected void setState(State s) {
        if (DBG) log("setState should not be used in GSM" + s);
    }

    // Return state of specific apn type
    @Override
    public State getState(String apnType) {
        ApnContext apnContext = mApnContexts.get(apnType);
        if (apnContext != null) {
            return apnContext.getState();
        }
        return State.FAILED;
    }

    // Return state of overall
    public State getOverallState() {
        boolean isConnecting = false;
        boolean isFailed = true; // All enabled Apns should be FAILED.
        boolean isAnyEnabled = false;
        StringBuilder builder = new StringBuilder();
        for (ApnContext apnContext : mApnContexts.values()) {
        	if (apnContext != null) {
        		builder.append(apnContext.toString() + ", ");
        	}
        }
        if (DBG) log( "overall state is " + builder);
        for (ApnContext apnContext : mApnContexts.values()) {
            if (apnContext.isEnabled()) {
                isAnyEnabled = true;
                switch (apnContext.getState()) {
                case CONNECTED:
                case DISCONNECTING:
                    if (DBG) log("overall state is CONNECTED");
                    return State.CONNECTED;
                case CONNECTING:
                case INITING:
                    isConnecting = true;
                    isFailed = false;
                    break;
                case IDLE:
                case SCANNING:
                    isFailed = false;
                    break;
                }
            }
        }

        if (!isAnyEnabled) { // Nothing enabled. return IDLE.
            if (DBG) log( "overall state is IDLE");
            return State.IDLE;
        }

        if (isConnecting) {
            if (DBG) log( "overall state is CONNECTING");
            return State.CONNECTING;
        } else if (!isFailed) {
            if (DBG) log( "overall state is IDLE");
            return State.IDLE;
        } else {
            if (DBG) log( "overall state is FAILED");
            return State.FAILED;
        }
    }

    /**
     * Ensure that we are connected to an APN of the specified type.
     *
     * @param type the APN type
     * @return Success is indicated by {@code Phone.APN_ALREADY_ACTIVE} or
     *         {@code Phone.APN_REQUEST_STARTED}. In the latter case, a
     *         broadcast will be sent by the ConnectivityManager when a
     *         connection to the APN has been established.
     */
    @Override
    public synchronized int enableApnType(String apnType) {
        ApnContext apnContext = mApnContexts.get(apnType);
        if (apnContext == null || !isApnTypeAvailable(apnType)) {
            if (DBG) log("enableApnType: " + apnType + " is type not available");
            return Phone.APN_TYPE_NOT_AVAILABLE;
        }

        // If already active, return
        if (DBG) log("enableApnType: " + apnType + " mState(" + apnContext.getState() + ")");

        if (apnContext.getState() == State.CONNECTED) {
            if (DBG) log("enableApnType: return APN_ALREADY_ACTIVE");
            return Phone.APN_ALREADY_ACTIVE;
        }
        setEnabled(apnTypeToId(apnType), true);
        if (DBG) {
            log("enableApnType: new apn request for type " + apnType +
                    " return APN_REQUEST_STARTED");
        }
        return Phone.APN_REQUEST_STARTED;
    }

    // A new APN has gone active and needs to send events to catch up with the
    // current condition
    private void notifyApnIdUpToCurrent(String reason, ApnContext apnContext, String type) {
        switch (apnContext.getState()) {
            case IDLE:
            case INITING:
                break;
            case CONNECTING:
            case SCANNING:
                mPhone.notifyDataConnection(reason, type, Phone.DataState.CONNECTING);
                break;
            case CONNECTED:
            case DISCONNECTING:
                mPhone.notifyDataConnection(reason, type, Phone.DataState.CONNECTING);
                mPhone.notifyDataConnection(reason, type, Phone.DataState.CONNECTED);
                break;
        }
    }

    @Override
    public synchronized int disableApnType(String type) {
        if (DBG) log("disableApnType:" + type);
        ApnContext apnContext = mApnContexts.get(type);

        if (apnContext != null) {
            setEnabled(apnTypeToId(type), false);
            if (apnContext.getState() != State.IDLE && apnContext.getState() != State.FAILED) {
                if (DBG) log("diableApnType: return APN_REQUEST_STARTED");
                return Phone.APN_REQUEST_STARTED;
            } else {
                if (DBG) log("disableApnType: return APN_ALREADY_INACTIVE");
                return Phone.APN_ALREADY_INACTIVE;
            }

        } else {
            if (DBG) {
                log("disableApnType: no apn context was found, return APN_REQUEST_FAILED");
            }
            return Phone.APN_REQUEST_FAILED;
        }
    }

    @Override
    protected boolean isApnTypeAvailable(String type) {
        if (type.equals(Phone.APN_TYPE_DUN) && fetchDunApn() != null) {
            return true;
        }

        if (mAllApns != null) {
            for (ApnSetting apn : mAllApns) {
                if (apn.canHandleType(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Report on whether data connectivity is enabled for any APN.
     * @return {@code false} if data connectivity has been explicitly disabled,
     * {@code true} otherwise.
     */
    @Override
    public boolean getAnyDataEnabled() {
        synchronized (mDataEnabledLock) {
            mUserDataEnabled = Settings.Secure.getInt(
                mPhone.getContext().getContentResolver(), Settings.Secure.MOBILE_DATA, 1) == 1;

            if (!(mInternalDataEnabled && mUserDataEnabled && sPolicyDataEnabled)) return false;
            for (ApnContext apnContext : mApnContexts.values()) {
                // Make sure we dont have a context that going down
                // and is explicitly disabled.
                if (isDataAllowed(apnContext)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isNotDefaultTypeDataEnabled() {
        synchronized (mDataEnabledLock) {
            if (!(mInternalDataEnabled && sPolicyDataEnabled))
                return false;
            for (ApnContext apnContext : mApnContexts.values()) {
                // Make sure we dont have a context that going down
                // and is explicitly disabled.
                if (!Phone.APN_TYPE_DEFAULT.equals(apnContext.getApnType()) && isDataAllowed(apnContext)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isDataAllowed(ApnContext apnContext) {
        return apnContext.isReady() && isDataAllowed();
    }

    //****** Called from ServiceStateTracker
    /**
     * Invoked when ServiceStateTracker observes a transition from GPRS
     * attach to detach.
     */
    protected void onDataConnectionDetached() {
        /*
         * We presently believe it is unnecessary to tear down the PDP context
         * when GPRS detaches, but we should stop the network polling.
         */
        if (DBG) log ("onDataConnectionDetached: stop polling and notify detached");
        stopNetStatPoll();
        stopDataStallAlarm();

        notifyDataConnection(Phone.REASON_DATA_DETACHED);
        mGsmPhone.updateSimIndicateState();
        //MTK-START [mtk04070][111205][ALPS00093395]Stop SCRI polling
        /* Add by MTK03594 */
        if (DBG) log ("onDataConnectionDetached: stopScriPoll()");
        stopScriPoll();
        //MTK-END [mtk04070][111205][ALPS00093395]Stop SCRI polling
    }

    private void onDataConnectionAttached() {
        if (DBG) log("onDataConnectionAttached");
        if (getOverallState() == State.CONNECTED) {
            if (DBG) log("onDataConnectionAttached: start polling notify attached");
            startNetStatPoll();
            startDataStallAlarm(DATA_STALL_NOT_SUSPECTED);
            notifyDataConnection(Phone.REASON_DATA_ATTACHED);
        } else {
            // update APN availability so that APN can be enabled.
            notifyOffApnsOfAvailability(Phone.REASON_DATA_ATTACHED);
        }

        setupDataOnReadyApns(Phone.REASON_DATA_ATTACHED);
    }

    @Override
    protected boolean isDataAllowed() {
        final boolean internalDataEnabled;
        synchronized (mDataEnabledLock) {
            internalDataEnabled = mInternalDataEnabled;
        }

        int gprsState = mPhone.getServiceStateTracker().getCurrentDataConnectionState();
        boolean desiredPowerState = mPhone.getServiceStateTracker().getDesiredPowerState();

        boolean allowed =
                    (gprsState == ServiceState.STATE_IN_SERVICE || mAutoAttachOnCreation) &&
                    mPhone.mIccRecords.getRecordsLoaded() &&
                    (mPhone.getState() == Phone.State.IDLE ||
                     mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) &&
                    internalDataEnabled &&
                    (!mPhone.getServiceState().getRoaming() || getDataOnRoamingEnabled()) &&
                    !mIsPsRestricted &&
                    desiredPowerState;
        if (!allowed && DBG) {
            String reason = "";
            if (!((gprsState == ServiceState.STATE_IN_SERVICE) || mAutoAttachOnCreation)) {
                reason += " - gprs= " + gprsState;
            }
            if (!mPhone.mIccRecords.getRecordsLoaded()) reason += " - SIM not loaded";
            if (mPhone.getState() != Phone.State.IDLE &&
                    !mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                reason += " - PhoneState= " + mPhone.getState();
                reason += " - Concurrent voice and data not allowed";
            }
            if (!internalDataEnabled) reason += " - mInternalDataEnabled= false";
            if (mPhone.getServiceState().getRoaming() && !getDataOnRoamingEnabled()) {
                reason += " - Roaming and data roaming not enabled";
            }
            if (mIsPsRestricted) reason += " - mIsPsRestricted= true";
            if (!desiredPowerState) reason += " - desiredPowerState= false";
            if (DBG) log("isDataAllowed: not allowed due to" + reason);
        }
        return allowed;
    }

    private void setupDataOnReadyApns(String reason) {
        // Stop reconnect alarms on all data connections pending
        // retry. Reset ApnContext state to IDLE.
        for (DataConnectionAc dcac : mDataConnectionAsyncChannels.values()) {
            if (dcac.getReconnectIntentSync() != null) {
                cancelReconnectAlarm(dcac);
            }
            // update retry config for existing calls to match up
            // ones for the new RAT.
            if (dcac.dataConnection != null) {
                Collection<ApnContext> apns = dcac.getApnListSync();

                boolean hasDefault = false;
                for (ApnContext apnContext : apns) {
                    if (apnContext.getApnType().equals(Phone.APN_TYPE_DEFAULT)) {
                        hasDefault = true;
                        break;
                    }
                }
                configureRetry(dcac.dataConnection, hasDefault);
            }
        }

        // Only check for default APN state
        for (ApnContext apnContext : mApnContexts.values()) {
            if (apnContext.getState() == State.FAILED) {
                // By this time, alarms for all failed Apns
                // should be stopped if any.
                // Make sure to set the state back to IDLE
                // so that setup data can happen.
                apnContext.setState(State.IDLE);
            }
            if (apnContext.isReady()) {
                if (apnContext.getState() == State.IDLE) {
                    apnContext.setReason(reason);
                    trySetupData(apnContext);
                }
            }
        }
    }

    private boolean trySetupData(String reason, String type) {
        if (DBG) {
            log("trySetupData: " + type + " due to " + (reason == null ? "(unspecified)" : reason)
                    + " isPsRestricted=" + mIsPsRestricted);
        }

        if (type == null) {
            type = Phone.APN_TYPE_DEFAULT;
        }

        ApnContext apnContext = mApnContexts.get(type);

        if (apnContext == null ){
            if (DBG) log("trySetupData new apn context for type:" + type);
            apnContext = new ApnContext(type, LOG_TAG);
            mApnContexts.put(type, apnContext);
        }
        apnContext.setReason(reason);

        return trySetupData(apnContext);
    }

    private boolean trySetupData(ApnContext apnContext) {
        String apnType = apnContext.getApnType();
        if (DBG) {
            log("trySetupData for type:" + apnType +
                    " due to " + apnContext.getReason());
            log("trySetupData with mIsPsRestricted=" + mIsPsRestricted);
        }

        if (mPhone.getSimulatedRadioControl() != null) {
            // Assume data is connected on the simulator
            // FIXME  this can be improved
            apnContext.setState(State.CONNECTED);
            mPhone.notifyDataConnection(apnContext.getReason(), apnType);

            log("trySetupData: (fix?) We're on the simulator; assuming data is connected");
            return true;
        }
        //MTK begin
        if(FeatureOption.MTK_GEMINI_SUPPORT && Phone.APN_TYPE_DEFAULT.equals(apnType)){
            int gprsDefaultSIM = getDataConnectionFromSetting();
            logd("gprsDefaultSIM:" + gprsDefaultSIM);
            if(gprsDefaultSIM != mGsmPhone.getMySimId()){
                  logd("The setting is off(1)");
                  return false;
            }else if(gprsDefaultSIM < 0){
               logd("The setting is off(2)");
               return false;
            }
         }
        //MTK end

        if (apnContext.getState() == State.DISCONNECTING) {
            if (DBG) logd("trySetupData:" + apnContext.getApnType() + " is DISCONNECTING, trun on reactive flag.");
            apnContext.setReactive(true);
        }
        
        String optr = SystemProperties.get("ro.operator.optr");
        boolean desiredPowerState = mPhone.getServiceStateTracker().getDesiredPowerState();
        boolean anyDataEnabled = (FeatureOption.MTK_BSP_PACKAGE || 
                (optr.equals("OP01") && !Phone.APN_TYPE_MMS.equals(apnType)) ||
                Phone.APN_TYPE_DEFAULT.equals(apnType))? getAnyDataEnabled() : isNotDefaultTypeDataEnabled();

        if ((apnContext.getState() == State.IDLE || apnContext.getState() == State.SCANNING) &&
                isDataAllowed(apnContext) && anyDataEnabled && !isEmergency()) {

            if (apnContext.getState() == State.IDLE) {
                ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnType);
                if (waitingApns.isEmpty()) {
                    if (DBG) log("trySetupData: No APN found");
                    notifyNoData(GsmDataConnection.FailCause.MISSING_UNKNOWN_APN, apnContext);
                    notifyOffApnsOfAvailability(apnContext.getReason());
                    return false;
                } else {
                    apnContext.setWaitingApns(waitingApns);
                    if (DBG) {
                        log ("trySetupData: Create from mAllApns : " + apnListToString(mAllApns));
                    }
                }
            }

            if (DBG) {
                log ("Setup watingApns : " + apnListToString(apnContext.getWaitingApns()));
            }
            // apnContext.setReason(apnContext.getReason());
            boolean retValue = setupData(apnContext);
            notifyOffApnsOfAvailability(apnContext.getReason());
            return retValue;
        } else {
            // TODO: check the condition.
            if (DBG) log ("try setup data but not executed [" + mInternalDataEnabled + "," + mUserDataEnabled + "," + sPolicyDataEnabled + "]");
            if (!apnContext.getApnType().equals(Phone.APN_TYPE_DEFAULT)
                && (apnContext.getState() == State.IDLE
                    || apnContext.getState() == State.SCANNING))
                mPhone.notifyDataConnectionFailed(apnContext.getReason(), apnType);
            notifyOffApnsOfAvailability(apnContext.getReason());
            return false;
        }
    }

    @Override
    // Disabled apn's still need avail/unavail notificiations - send them out
    protected void notifyOffApnsOfAvailability(String reason) {
        for (ApnContext apnContext : mApnContexts.values()) {
            if (!apnContext.isReady()) {
                if (DBG) log("notifyOffApnOfAvailability type:" + apnContext.getApnType());
                mPhone.notifyDataConnection(reason != null ? reason : apnContext.getReason(),
                                            apnContext.getApnType(),
                                            Phone.DataState.DISCONNECTED);
            } else {
                if (DBG) {
                    log("notifyOffApnsOfAvailability skipped apn due to isReady==false: " +
                            apnContext.toString());
                }
            }
        }
    }

    /**
     * If tearDown is true, this only tears down a CONNECTED session. Presently,
     * there is no mechanism for abandoning an INITING/CONNECTING session,
     * but would likely involve cancelling pending async requests or
     * setting a flag or new state to ignore them when they came in
     * @param tearDown true if the underlying GsmDataConnection should be
     * disconnected.
     * @param reason reason for the clean up.
     */
    protected void cleanUpAllConnections(boolean tearDown, String reason) {
        if (DBG) log("cleanUpAllConnections: tearDown=" + tearDown + " reason=" + reason);

        for (ApnContext apnContext : mApnContexts.values()) {
            apnContext.setReason(reason);
            cleanUpConnection(tearDown, apnContext);
        }

        stopNetStatPoll();
        stopDataStallAlarm();

        // TODO: Do we need mRequestedApnType?
        mRequestedApnType = Phone.APN_TYPE_DEFAULT;
    }

    /**
     * Cleanup all connections.
     *
     * TODO: Cleanup only a specified connection passed as a parameter.
     *       Also, make sure when you clean up a conn, if it is last apply
     *       logic as though it is cleanupAllConnections
     *
     * @param tearDown true if the underlying DataConnection should be disconnected.
     * @param reason for the clean up.
     */

    @Override
    protected void onCleanUpAllConnections(String cause) {
        cleanUpAllConnections(true, cause);
    }

    private void cleanUpConnection(boolean tearDown, ApnContext apnContext) {

        if (apnContext == null) {
            if (DBG) log("cleanUpConnection: apn context is null");
            return;
        }

        if (DBG) {
            log("cleanUpConnection: tearDown=" + tearDown + " reason=" + apnContext.getReason());
        }
        DataConnectionAc dcac = apnContext.getDataConnectionAc();
        if (tearDown) {
            if (apnContext.isDisconnected()) {
                // The request is tearDown and but ApnContext is not connected.
                // If apnContext is not enabled anymore, break the linkage to the DCAC/DC.
                apnContext.setState(State.IDLE);
                if (!apnContext.isReady()) {
                    apnContext.setDataConnection(null);
                    apnContext.setDataConnectionAc(null);
                }
                // If original state is FAILED, we should notify data possible again since data is disabled.
                mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            } else {
                // Connection is still there. Try to clean up.
                if (dcac != null) {
                    if (apnContext.getState() != State.DISCONNECTING) {
                        boolean disconnectAll = false;
                        if (Phone.APN_TYPE_DUN.equals(apnContext.getApnType())) {
                            ApnSetting dunSetting = fetchDunApn();
                            if (dunSetting != null &&
                                    dunSetting.equals(apnContext.getApnSetting())) {
                                if (DBG) log("tearing down dedicated DUN connection");
                                // we need to tear it down - we brought it up just for dun and
                                // other people are camped on it and now dun is done.  We need
                                // to stop using it and let the normal apn list get used to find
                                // connections for the remaining desired connections
                                disconnectAll = true;
                            }
                        }
                        if (DBG) {
                            log("cleanUpConnection: tearing down" + (disconnectAll ? " all" :""));
                        }
                        Message msg = obtainMessage(EVENT_DISCONNECT_DONE, apnContext);
                        if (disconnectAll) {
                            apnContext.getDataConnection().tearDownAll(apnContext.getReason(), msg);
                        } else {
                            apnContext.getDataConnection().tearDown(apnContext.getReason(), msg);
                        }
                        apnContext.setState(State.DISCONNECTING);
                    }
                } else {
                    // apn is connected but no reference to dcac.
                    // Should not be happen, but reset the state in case.
                    apnContext.setState(State.IDLE);
                    mPhone.notifyDataConnection(apnContext.getReason(),
                                                apnContext.getApnType());
                }
            }
        } else {
            // force clean up the data connection.
            if (dcac != null) dcac.resetSync();
            apnContext.setState(State.IDLE);
            mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            apnContext.setDataConnection(null);
            apnContext.setDataConnectionAc(null);
        }

        // make sure reconnection alarm is cleaned up if there is no ApnContext
        // associated to the connection.
        if (dcac != null) {
            Collection<ApnContext> apnList = dcac.getApnListSync();
            if (apnList.isEmpty()) {
                cancelReconnectAlarm(dcac);
            }
        }
    }

    /**
     * Cancels the alarm associated with DCAC.
     *
     * @param DataConnectionAc on which the alarm should be stopped.
     */
    private void cancelReconnectAlarm(DataConnectionAc dcac) {
        if (dcac == null) return;

        PendingIntent intent = dcac.getReconnectIntentSync();

        if (intent != null) {
                AlarmManager am =
                    (AlarmManager) mPhone.getContext().getSystemService(Context.ALARM_SERVICE);
                am.cancel(intent);
                dcac.setReconnectIntentSync(null);
        }
    }

    /**
     * @param types comma delimited list of APN types
     * @return array of APN types
     */
    private String[] parseTypes(String types) {
        String[] result;
        // If unset, set to DEFAULT.
        if (types == null || types.equals("")) {
            result = new String[1];
            result[0] = Phone.APN_TYPE_ALL;
        } else {
            result = types.split(",");
        }
        return result;
    }

    private ArrayList<ApnSetting> createApnList(Cursor cursor) {
        if (FeatureOption.MTK_MVNO_SUPPORT) {
            String spnString = mGsmPhone.getSpNameInEfSpn();
            logd("createApnList spnString=" + spnString);
            if (spnString != null && !spnString.equals("")) {
                ArrayList<ApnSetting> spnResult = createApnListWithSPN(cursor, spnString);
                if (spnResult.size() > 0) {
                    logd("Has spnResult.");
                    return spnResult;
                }
            }
            String imsiString = mGsmPhone.isOperatorMvnoForImsi();
            if (imsiString != null && !imsiString.equals("")) {
                ArrayList<ApnSetting> imsiResult = createApnListWithIMSI(cursor, imsiString);
                if (imsiResult.size() > 0) {
                    logd("Has imsiResult.");
                    return imsiResult;
                }
            }
        }

        ArrayList<ApnSetting> result = new ArrayList<ApnSetting>();
        if (cursor.moveToFirst()) {
            do {
                String[] types = parseTypes(
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.TYPE)));
                ApnSetting apn = new ApnSetting(
                        cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.NUMERIC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.APN)),
                        NetworkUtils.trimV4AddrZeros(
                                cursor.getString(
                                cursor.getColumnIndexOrThrow(Telephony.Carriers.PROXY))),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.PORT)),
                        NetworkUtils.trimV4AddrZeros(
                                cursor.getString(
                                cursor.getColumnIndexOrThrow(Telephony.Carriers.MMSC))),
                        NetworkUtils.trimV4AddrZeros(
                                cursor.getString(
                                cursor.getColumnIndexOrThrow(Telephony.Carriers.MMSPROXY))),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.MMSPORT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.USER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.PASSWORD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers.AUTH_TYPE)),
                        types,
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.PROTOCOL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(
                                Telephony.Carriers.ROAMING_PROTOCOL)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(
                                Telephony.Carriers.CARRIER_ENABLED)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers.BEARER)));
                result.add(apn);
            } while (cursor.moveToNext());
        }
        if (DBG) log("createApnList: X result=" + result);
        return result;
    }

    private ArrayList<ApnSetting> createApnListWithSPN(Cursor cursor, String spn) {
        ArrayList<ApnSetting> result = new ArrayList<ApnSetting>();
        if (cursor.moveToFirst()) {
            do {
                if (spn != null && !spn.equals(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.SPN)))) {
                    continue;
                }
                String[] types = parseTypes(
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.TYPE)));
                ApnSetting apn = new ApnSetting(
                        cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.NUMERIC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.APN)),
                        NetworkUtils.trimV4AddrZeros(
                                cursor.getString(
                                cursor.getColumnIndexOrThrow(Telephony.Carriers.PROXY))),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.PORT)),
                        NetworkUtils.trimV4AddrZeros(
                                cursor.getString(
                                cursor.getColumnIndexOrThrow(Telephony.Carriers.MMSC))),
                        NetworkUtils.trimV4AddrZeros(
                                cursor.getString(
                                cursor.getColumnIndexOrThrow(Telephony.Carriers.MMSPROXY))),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.MMSPORT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.USER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.PASSWORD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers.AUTH_TYPE)),
                        types,
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.PROTOCOL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(
                                Telephony.Carriers.ROAMING_PROTOCOL)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(
                                Telephony.Carriers.CARRIER_ENABLED)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers.BEARER)));
                result.add(apn);
            } while (cursor.moveToNext());
        }
        if (DBG) log("createApnList: X result=" + result);
        return result;
    }

    private ArrayList<ApnSetting> createApnListWithIMSI(Cursor cursor, String imsi) {
        ArrayList<ApnSetting> result = new ArrayList<ApnSetting>();
        if (cursor.moveToFirst()) {
            do {
                if (imsi != null && !imsi.equals(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.IMSI)))) {
                    continue;
                }
                String[] types = parseTypes(
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.TYPE)));
                ApnSetting apn = new ApnSetting(
                        cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.NUMERIC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.APN)),
                        NetworkUtils.trimV4AddrZeros(
                                cursor.getString(
                                cursor.getColumnIndexOrThrow(Telephony.Carriers.PROXY))),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.PORT)),
                        NetworkUtils.trimV4AddrZeros(
                                cursor.getString(
                                cursor.getColumnIndexOrThrow(Telephony.Carriers.MMSC))),
                        NetworkUtils.trimV4AddrZeros(
                                cursor.getString(
                                cursor.getColumnIndexOrThrow(Telephony.Carriers.MMSPROXY))),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.MMSPORT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.USER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.PASSWORD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers.AUTH_TYPE)),
                        types,
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.PROTOCOL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(
                                Telephony.Carriers.ROAMING_PROTOCOL)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(
                                Telephony.Carriers.CARRIER_ENABLED)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers.BEARER)));
                result.add(apn);
            } while (cursor.moveToNext());
        }
        if (DBG) log("createApnList: X result=" + result);
        return result;
    }

    private boolean dataConnectionNotInUse(DataConnectionAc dcac) {
        for (ApnContext apnContext : mApnContexts.values()) {
            if (apnContext.getDataConnectionAc() == dcac) return false;
        }
        return true;
    }

    private GsmDataConnection findFreeDataConnection() {
        for (DataConnectionAc dcac : mDataConnectionAsyncChannels.values()) {
            if (dcac.isInactiveSync() && dataConnectionNotInUse(dcac)) {
                log("findFreeDataConnection: found free GsmDataConnection");
                return (GsmDataConnection) dcac.dataConnection;
            }
        }
        log("findFreeDataConnection: NO free GsmDataConnection");
        return null;
    }

    protected GsmDataConnection findReadyDataConnection(ApnSetting apn) {
        if (DBG)
            log("findReadyDataConnection: apn string <" +
                (apn!=null?(apn.toString()):"null") +">");
        if (apn == null) {
            return null;
        }
        for (DataConnectionAc dcac : mDataConnectionAsyncChannels.values()) {
            ApnSetting apnSetting = dcac.getApnSettingSync();
            if (DBG) {
                log("findReadyDataConnection: dc apn string <" +
                         (apnSetting != null ? (apnSetting.toString()) : "null") + ">");
            }
            if ((apnSetting != null) && TextUtils.equals(apnSetting.toString(), apn.toString())) {
                return (GsmDataConnection) dcac.dataConnection;
            }
        }
        return null;
    }


    private boolean setupData(ApnContext apnContext) {
        if (DBG) log("setupData: apnContext=" + apnContext);
        ApnSetting apn;
        GsmDataConnection dc;

        int profileId = getApnProfileID(apnContext.getApnType());
        apn = apnContext.getNextWaitingApn();
        if (apn == null) {
            if (DBG) log("setupData: return for no apn found!");
            return false;
        }

        // First, check to see if ApnContext already has DC.
        // This could happen if the retries are currently  engaged.
        dc = (GsmDataConnection)apnContext.getDataConnection();

        if (dc != null) {
            ApnSetting apnSetting = dc.getApnSetting();
            if (apnSetting != null && !apnSetting.canHandleType(apnContext.getApnType())) {
                //if the we share APN setting with other connection and APN is changed
                //since the APN settings we got from the dc may be different
                //we have to double check if current APN setting can handle the connection type
                if (DBG) log("APN settings stored in DC cannot handle this type, clean up DC and create a new one");
                apnContext.setDataConnection(null);
                dc = null;
            }
        }

        if (dc == null) {

            dc = (GsmDataConnection) checkForConnectionForApnContext(apnContext);

            // check if the dc's APN setting is prefered APN for default connection.
            if (dc != null && Phone.APN_TYPE_DEFAULT.equals(apnContext.getApnType())) {
                ApnSetting dcApnSetting = dc.getApnSetting();
                if (dcApnSetting != null && !dcApnSetting.apn.equals(apn.apn)) {
                    if (DBG) log("The existing DC is not using prefered APN.");
                    dc = null;
                }
            }

            if (dc == null) {
                dc = findReadyDataConnection(apn);
            }

            // In checkForConnectionForApnContext() and  findReadyDataConnection() case 
            // APN setting should be the same as shared DC.
            if (dc != null && dc.getApnSetting() != null) {
                apn = dc.getApnSetting();
            }

            if (dc == null) {
                if (DBG) log("setupData: No ready GsmDataConnection found!");
                // TODO: When allocating you are mapping type to id. If more than 1 free,
                // then could findFreeDataConnection get the wrong one??
                dc = findFreeDataConnection();
            }

            if (dc == null) {
                dc = createDataConnection();
            }

            if (dc == null) {
                if(PhoneFactory.isDualTalkMode())
                {
                    if (apnContext.getApnType() == Phone.APN_TYPE_DEFAULT)
                    {
                if (DBG) log("setupData: No free GsmDataConnection found!");
                return false;
            }
                    else
                    {
                        ApnContext DisableapnContext = mApnContexts.get(Phone.APN_TYPE_DEFAULT);
                        clearWaitingApn();
                        cleanUpConnection(true, DisableapnContext);
                        //disableApnType(Phone.APN_TYPE_DEFAULT);
                        mWaitingApnList.add(apnContext.getApnType());
                        return true;
                    }
                }
                else
                {
                    if (DBG) log("setupData: No free GsmDataConnection found!");
                    return false;
                }
            }

            DataConnectionAc dcac = mDataConnectionAsyncChannels.get(dc.getDataConnectionId());
            dc.setProfileId( profileId );
            dc.setActiveApnType(apnContext.getApnType());
            int refCount = dcac.getRefCountSync();
            if (DBG) log("setupData: init dc and apnContext refCount=" + refCount);

            // configure retry count if no other Apn is using the same connection.
            if (refCount == 0) {
                configureRetry(dc, apn.canHandleType(Phone.APN_TYPE_DEFAULT));
            }
			
            if (apnContext.getDataConnectionAc() != null && apnContext.getDataConnectionAc() != dcac) {
                if (DBG) log("setupData: dcac not null and not equal to assigned dcac.");
                apnContext.setDataConnectionAc(null);
            }
			
            apnContext.setDataConnectionAc(dcac);
            apnContext.setDataConnection(dc);
        }

        apnContext.setApnSetting(apn);
        apnContext.setState(State.INITING);
        mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        // If reconnect alarm is active on this DataConnection, wait for the alarm being
        // fired so that we don't disruppt data retry pattern engaged.
        if (apnContext.getDataConnectionAc().getReconnectIntentSync() != null) {
            if (DBG) log("setupData: data reconnection pending");
            apnContext.setState(State.FAILED);
            if (Phone.APN_TYPE_MMS.equals(apnContext.getApnType())) {
                mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType(), Phone.DataState.CONNECTING);
            } else {
                mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            }
            return true;
        }

        if (apnContext.getApnType() == Phone.APN_TYPE_MMS) {
            mWaitingApnList.clear();

            for (ApnContext currApnCtx : mApnContexts.values()) {
                ApnSetting apnSetting = currApnCtx.getApnSetting();

                if (currApnCtx == apnContext)
                    continue;            
                if ((apnSetting != null) && !currApnCtx.isDisconnected() && 
                            !apnSetting.equals(apn) && (isSameProxy(apnSetting, apn) && !apnSetting.apn.equals(apn.apn))) {
                    if (DBG) logd("setupData: disable conflict APN " + currApnCtx.getApnType());
                    disableApnType(currApnCtx.getApnType());
                    mWaitingApnList.add(currApnCtx.getApnType());
                }
            }
        }

        Message msg = obtainMessage();
        msg.what = EVENT_DATA_SETUP_COMPLETE;
        msg.obj = apnContext;
        dc.bringUp(msg, apn);

        if (DBG) log("setupData: initing!");
        return true;
    }

    /**
     * Handles changes to the APN database.
     */
    private void onApnChanged() {
        State overallState = getOverallState();
        boolean isDisconnected = (overallState == State.IDLE || overallState == State.FAILED);

        if (mPhone instanceof GSMPhone) {
            // The "current" may no longer be valid.  MMS depends on this to send properly. TBD
            ((GSMPhone)mPhone).updateCurrentCarrierInProvider();
        }

        // TODO: It'd be nice to only do this if the changed entrie(s)
        // match the current operator.
        if (DBG) log("onApnChanged: createAllApnList and cleanUpAllConnections");
        ArrayList<ApnSetting> previous_allApns = mAllApns;
        ApnSetting previous_preferredApn = mPreferredApn;
        createAllApnList();
        boolean isSameApnSetting = false;
        if ((previous_allApns == null && mAllApns == null)){
            if (previous_preferredApn == null && mPreferredApn == null) {
                isSameApnSetting = true;
            } else if (previous_preferredApn != null && previous_preferredApn.equals(mPreferredApn)) {
                isSameApnSetting = true;
            }
        } else if (previous_allApns != null && mAllApns != null) {
            String pre_all_str = "";
            String all_str = "";
            for (ApnSetting s : previous_allApns) {
                pre_all_str += s.toString();
            }
            for (ApnSetting t : mAllApns) {
                all_str += t.toString();
            }
            if (pre_all_str.equals(all_str)) {
                if (previous_preferredApn == null && mPreferredApn == null) {
                    isSameApnSetting = true;
                } else if (previous_preferredApn != null && previous_preferredApn.equals(mPreferredApn)) {
                    isSameApnSetting = true;
                    //TODO MTK remove
                }
            }
        }
        
        if (isSameApnSetting) {
            if (DBG) log("onApnChanged: not changed.");
            return;
        }
        if (DBG) log("onApnChanged: previous_preferredApn " + previous_preferredApn);
        if (DBG) log("onApnChanged: mPreferredApn " + mPreferredApn);
        cleanUpAllConnections(!isDisconnected, Phone.REASON_APN_CHANGED);
        if (isDisconnected) {
            setupDataOnReadyApns(Phone.REASON_APN_CHANGED);
        }
    }

    /**
     * @param cid Connection id provided from RIL.
     * @return DataConnectionAc associated with specified cid.
     */
    private DataConnectionAc findDataConnectionAcByCid(int cid) {
        for (DataConnectionAc dcac : mDataConnectionAsyncChannels.values()) {
            if (dcac.getCidSync() == cid) {
                return dcac;
            }
        }
        return null;
    }

    /**
     * @param dcacs Collection of DataConnectionAc reported from RIL.
     * @return List of ApnContext which is connected, but is not present in
     *         data connection list reported from RIL.
     */
    private List<ApnContext> findApnContextToClean(Collection<DataConnectionAc> dcacs) {
        if (dcacs == null) return null;

        ArrayList<ApnContext> list = new ArrayList<ApnContext>();
        for (ApnContext apnContext : mApnContexts.values()) {
            if (apnContext.getState() == State.CONNECTED) {
                boolean found = false;
                for (DataConnectionAc dcac : dcacs) {
                    if (dcac == apnContext.getDataConnectionAc()) {
                        // ApnContext holds the ref to dcac present in data call list.
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // ApnContext does not have dcac reported in data call list.
                    // Fetch all the ApnContexts that map to this dcac which are in
                    // INITING state too.
                    if (DBG) log("onDataStateChanged(ar): Connected apn not found in the list (" +
                                 apnContext.toString() + ")");
                    if (apnContext.getDataConnectionAc() != null) {
                        list.addAll(apnContext.getDataConnectionAc().getApnListSync());
                    } else {
                        list.add(apnContext);
                    }
                }
            }
        }
        return list;
    }

    /**
     * @param ar is the result of RIL_REQUEST_DATA_CALL_LIST
     * or RIL_UNSOL_DATA_CALL_LIST_CHANGED
     */
    private void onDataStateChanged (AsyncResult ar) {
        ArrayList<DataCallState> dataCallStates;

        if (DBG) log("onDataStateChanged(ar): E");
        dataCallStates = (ArrayList<DataCallState>)(ar.result);

        if (ar.exception != null) {
            // This is probably "radio not available" or something
            // of that sort. If so, the whole connection is going
            // to come down soon anyway
            if (DBG) log("onDataStateChanged(ar): exception; likely radio not available, clean up pdp");
            //mtk03851: since we have exception, we should assune there is no any pdp context
            dataCallStates = new ArrayList<DataCallState>(0);
        }

        int size = dataCallStates.size();
        if (DBG) log("onDataStateChanged(ar): DataCallState size=" + size);
        if (size == 0) {
            Collection<DataConnection> collection = mDataConnections.values();
            Iterator<DataConnection> iterator = collection.iterator();
            while (iterator.hasNext()) {
                DataConnection dataConnection = iterator.next();
                DataConnectionAc dataConnectionAc = mDataConnectionAsyncChannels.get(dataConnection.getDataConnectionId());
                if (dataConnectionAc != null &&
                    dataConnectionAc.getRefCountSync() > 0)
                {
                    logw("found unlinked DataConnection, to tear down it");
                    dataConnection.tearDownAll(Phone.REASON_DATA_DETACHED, null);
                }
            }
        }

        // Create a hash map to store the dataCallState of each DataConnectionAc
        HashMap<DataCallState, DataConnectionAc> dataCallStateToDcac;
        dataCallStateToDcac = new HashMap<DataCallState, DataConnectionAc>();
        for (DataCallState dataCallState : dataCallStates) {
            DataConnectionAc dcac = findDataConnectionAcByCid(dataCallState.cid);

            if (dcac != null) dataCallStateToDcac.put(dataCallState, dcac);
        }

        // A list of apns to cleanup, those that aren't in the list we know we have to cleanup
        List<ApnContext> apnsToCleanup = findApnContextToClean(dataCallStateToDcac.values());

        // Find which connections have changed state and send a notification or cleanup
        for (DataCallState newState : dataCallStates) {
            DataConnectionAc dcac = dataCallStateToDcac.get(newState);

            if (dcac == null) {
                loge("onDataStateChanged(ar): No associated DataConnection ignore");
                continue;
            }

            // The list of apn's associated with this DataConnection
            Collection<ApnContext> apns = dcac.getApnListSync();

            // Find which ApnContexts of this DC are in the "Connected/Connecting" state.
            ArrayList<ApnContext> connectedApns = new ArrayList<ApnContext>();
            for (ApnContext apnContext : apns) {
                if (apnContext.getState() == State.CONNECTED ||
                       apnContext.getState() == State.CONNECTING ||
                       apnContext.getState() == State.INITING) {
                    connectedApns.add(apnContext);
                }
            }
            if (connectedApns.size() == 0) {
                if (DBG) log("onDataStateChanged(ar): no connected apns");
            } else {
                // Determine if the connection/apnContext should be cleaned up
                // or just a notification should be sent out.
                if (DBG) log("onDataStateChanged(ar): Found ConnId=" + newState.cid
                        + " newState=" + newState.toString());
                if (newState.active == 0) {
                    if (DBG) {
                        log("onDataStateChanged(ar): inactive, cleanup apns=" + connectedApns);
                    }
                    apnsToCleanup.addAll(connectedApns);
                } else {
                    // Its active so update the DataConnections link properties
                    UpdateLinkPropertyResult result =
                        dcac.updateLinkPropertiesDataCallStateSync(newState);
                    if (result.oldLp.equals(result.newLp)) {
                        if (DBG) log("onDataStateChanged(ar): no change");
                    } else {
                        if (result.oldLp.isIdenticalInterfaceName(result.newLp)) {
                            if (! result.oldLp.isIdenticalDnses(result.newLp) ||
                                    ! result.oldLp.isIdenticalRoutes(result.newLp) ||
                                    ! result.oldLp.isIdenticalHttpProxy(result.newLp) ||
                                    ! result.oldLp.isIdenticalAddresses(result.newLp)) {
                                // If the same address type was removed and added we need to cleanup
                                CompareResult<LinkAddress> car =
                                    result.oldLp.compareAddresses(result.newLp);
                                boolean needToClean = false;
                                for (LinkAddress added : car.added) {
                                    for (LinkAddress removed : car.removed) {
                                        if (NetworkUtils.addressTypeMatches(removed.getAddress(),
                                                added.getAddress())) {
                                            needToClean = true;
                                            break;
                                        }
                                    }
                                }
                                if (needToClean) {
                                    if (DBG) {
                                        log("onDataStateChanged(ar): addr change, cleanup apns=" +
                                                connectedApns);
                                    }
                                    apnsToCleanup.addAll(connectedApns);
                                } else {
                                    if (DBG) log("onDataStateChanged(ar): simple change");
                                    for (ApnContext apnContext : connectedApns) {
                                         mPhone.notifyDataConnection(
                                                 Phone.REASON_LINK_PROPERTIES_CHANGED,
                                                 apnContext.getApnType());
                                    }
                                }
                            } else {
                                if (DBG) {
                                    log("onDataStateChanged(ar): no changes");
                                }
                            }
                        } else {
                            //the first time we setup data call, we encounter that the interface is changed
                            //but the old interface is null (not setup yet)
                            //we should ignore cleaning up apn in this case
                            if (result.oldLp.getInterfaceName() != null) {
                                if (DBG) {
                                    log("onDataStateChanged(ar): interface change, cleanup apns="
                                            + connectedApns);
                                }
                                apnsToCleanup.addAll(connectedApns);
                            } else {
                                if (DBG) {
                                    log("onDataStateChanged(ar): interface change but no old interface, not to cleanup apns"
                                            + connectedApns);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (apnsToCleanup.size() != 0) {
            // Add an event log when the network drops PDP
            int cid = getCellLocationId();
            EventLog.writeEvent(EventLogTags.PDP_NETWORK_DROP, cid,
                                TelephonyManager.getDefault().getNetworkType());
        }

        // Cleanup those dropped connections
        for (ApnContext apnContext : apnsToCleanup) {
            cleanUpConnection(true, apnContext);
        }

        if (DBG) log("onDataStateChanged(ar): X");
    }

    private void notifyDefaultData(ApnContext apnContext) {
        if (DBG) {
            log("notifyDefaultData: type=" + apnContext.getApnType()
                + ", reason:" + apnContext.getReason());
        }
        apnContext.setState(State.CONNECTED);
        // setState(State.CONNECTED);
        mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        startNetStatPoll();
        startDataStallAlarm(DATA_STALL_NOT_SUSPECTED);
        // reset reconnect timer
        apnContext.getDataConnection().resetRetryCount();
    }

    // TODO: For multiple Active APNs not exactly sure how to do this.
    protected void gotoIdleAndNotifyDataConnection(String reason) {
        if (DBG) log("gotoIdleAndNotifyDataConnection: reason=" + reason);
        notifyDataConnection(reason);
        mActiveApn = null;
    }

    private void resetPollStats() {
        mTxPkts = -1;
        mRxPkts = -1;
        mNetStatPollPeriod = POLL_NETSTAT_MILLIS;
    }

    private void doRecovery() {
        if (getOverallState() == State.CONNECTED) {
            // Go through a series of recovery steps, each action transitions to the next action
            int recoveryAction = getRecoveryAction();
            switch (recoveryAction) {
            case RecoveryAction.GET_DATA_CALL_LIST:
                //Temporary mark
                //EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_GET_DATA_CALL_LIST,
                //        mSentSinceLastRecv);
                //Temporary mark
                if (DBG) log("doRecovery() get data call list");
                mPhone.mCM.getDataCallList(obtainMessage(EVENT_DATA_STATE_CHANGED));
                putRecoveryAction(RecoveryAction.CLEANUP);
                break;
            case RecoveryAction.CLEANUP:
                //Temporary mark
                //EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_CLEANUP, mSentSinceLastRecv);
                //Temporary mark
                if (DBG) log("doRecovery() cleanup all connections");
                cleanUpAllConnections(true, Phone.REASON_PDP_RESET);
                putRecoveryAction(RecoveryAction.REREGISTER);
                break;
            case RecoveryAction.REREGISTER:
                //Temporary mark
                //EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_REREGISTER,
                //        mSentSinceLastRecv);
                //Temporary mark
                if (DBG) log("doRecovery() re-register");
                mPhone.getServiceStateTracker().reRegisterNetwork(null);
                putRecoveryAction(RecoveryAction.RADIO_RESTART);
                break;
            case RecoveryAction.RADIO_RESTART:
                //Temporary mark
                //EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_RADIO_RESTART,
                //        mSentSinceLastRecv);
                //Temporary mark
                if (DBG) log("restarting radio");
                putRecoveryAction(RecoveryAction.RADIO_RESTART_WITH_PROP);
                restartRadio();
                break;
            case RecoveryAction.RADIO_RESTART_WITH_PROP:
                // This is in case radio restart has not recovered the data.
                // It will set an additional "gsm.radioreset" property to tell
                // RIL or system to take further action.
                // The implementation of hard reset recovery action is up to OEM product.
                // Once gsm.radioreset property is consumed, it is expected to set back
                // to false by RIL.
                //Temporary mark
                //EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_RADIO_RESTART_WITH_PROP, -1);
                //Temporary mark
                if (DBG) log("restarting radio with gsm.radioreset to true");
                SystemProperties.set("gsm.radioreset", "true");
                // give 1 sec so property change can be notified.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
                restartRadio();
                putRecoveryAction(RecoveryAction.GET_DATA_CALL_LIST);
                break;
            default:
                throw new RuntimeException("doRecovery: Invalid recoveryAction=" +
                    recoveryAction);
            }
        }
    }

    @Override
    protected void startNetStatPoll() {
        if (getOverallState() == State.CONNECTED && mNetStatPollEnabled == false) {
            if (DBG) log("startNetStatPoll");
            resetPollStats();
            mNetStatPollEnabled = true;
            mPollNetStat.run();
        }
    }

    @Override
    protected void stopNetStatPoll() {
        mNetStatPollEnabled = false;
        removeCallbacks(mPollNetStat);
        if (DBG) log("stopNetStatPoll");
    }

    @Override
    protected void restartRadio() {
        if (DBG) log("restartRadio: ************TURN OFF RADIO**************");
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            int simId = mPhone.getMySimId();
            int dualSimMode = Settings.System.getInt(mPhone.getContext().getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 0);
            if (DBG) log("restartRadio: dual sim mode: " + dualSimMode);
            cleanUpAllConnections(true, Phone.REASON_RADIO_TURNED_OFF);
            mPhone.getServiceStateTracker().powerOffRadioSafely(this);
            //for dual sim we need restart radio power manually
            log("Start to radio off [" + dualSimMode + ", " + (dualSimMode & ~(simId+1)) + "]");

            //we should always set radio power through 3G protocol
            int sim3G = SystemProperties.getInt("gsm.3gswitch", 1) == 2 ? Phone.GEMINI_SIM_2 : Phone.GEMINI_SIM_1;
            if (sim3G == simId) {
                mPhone.mCM.setRadioMode((dualSimMode & ~(simId+1)), obtainMessage(MSG_RESTART_RADIO_OFF_DONE, dualSimMode, 0));
            } else {
                log("set radio off through peer phone since current phone is 2G protocol");
                if (mPhone instanceof GSMPhone) {
                    GSMPhone peerPhone = ((GSMPhone)mPhone).getPeerPhone();
                    ((PhoneBase)peerPhone).mCM.setRadioMode((dualSimMode & ~(simId+1)), obtainMessage(MSG_RESTART_RADIO_OFF_DONE, dualSimMode, 0));
                }
            }
        } else {
            cleanUpAllConnections(true, Phone.REASON_RADIO_TURNED_OFF);
            mPhone.getServiceStateTracker().powerOffRadioSafely(this);
        /* Note: no need to call setRadioPower(true).  Assuming the desired
         * radio power state is still ON (as tracked by ServiceStateTracker),
         * ServiceStateTracker will call setRadioPower when it receives the
         * RADIO_STATE_CHANGED notification for the power off.  And if the
         * desired power state has changed in the interim, we don't want to
         * override it with an unconditional power on.
         */
        }

        int reset = Integer.parseInt(SystemProperties.get("net.ppp.reset-by-timeout", "0"));
        SystemProperties.set("net.ppp.reset-by-timeout", String.valueOf(reset+1));
    }


    private void updateDataStallInfo() {
        long sent, received;

        TxRxSum preTxRxSum = new TxRxSum(mDataStallTxRxSum);
        mDataStallTxRxSum.updateTxRxSum();

        if (VDBG) {
            log("updateDataStallInfo: mDataStallTxRxSum=" + mDataStallTxRxSum +
                    " preTxRxSum=" + preTxRxSum);
        }

        sent = mDataStallTxRxSum.txPkts - preTxRxSum.txPkts;
        received = mDataStallTxRxSum.rxPkts - preTxRxSum.rxPkts;

        if (RADIO_TESTS) {
            if (SystemProperties.getBoolean("radio.test.data.stall", false)) {
                log("updateDataStallInfo: radio.test.data.stall true received = 0;");
                received = 0;
            }
        }
        if ( sent > 0 && received > 0 ) {
            if (VDBG) log("updateDataStallInfo: IN/OUT");
            mSentSinceLastRecv = 0;
            putRecoveryAction(RecoveryAction.GET_DATA_CALL_LIST);
        } else if (sent > 0 && received == 0) {
            if (mPhone.getState() == Phone.State.IDLE) {
                mSentSinceLastRecv += sent;
            } else {
                mSentSinceLastRecv = 0;
            }
            if (DBG) {
                log("updateDataStallInfo: OUT sent=" + sent +
                        " mSentSinceLastRecv=" + mSentSinceLastRecv);
            }
        } else if (sent == 0 && received > 0) {
            if (VDBG) log("updateDataStallInfo: IN");
            mSentSinceLastRecv = 0;
            putRecoveryAction(RecoveryAction.GET_DATA_CALL_LIST);
        } else {
            if (VDBG) log("updateDataStallInfo: NONE");
        }
    }

    @Override
    protected void onDataStallAlarm(int tag) {
        if (mDataStallAlarmTag != tag) {
            if (DBG) {
                log("onDataStallAlarm: ignore, tag=" + tag + " expecting " + mDataStallAlarmTag);
            }
            return;
        }
        updateDataStallInfo();

        int hangWatchdogTrigger = Settings.Secure.getInt(mResolver,
                Settings.Secure.PDP_WATCHDOG_TRIGGER_PACKET_COUNT,
                NUMBER_SENT_PACKETS_OF_HANG);

        boolean suspectedStall = DATA_STALL_NOT_SUSPECTED;
        if (mSentSinceLastRecv >= hangWatchdogTrigger) {
            if (DBG) {
                log("onDataStallAlarm: tag=" + tag + " do recovery action=" + getRecoveryAction());
            }
            suspectedStall = DATA_STALL_SUSPECTED;
            sendMessage(obtainMessage(EVENT_DO_RECOVERY));
            // reset mSentSinceLastRecv after doing recovery.
            mSentSinceLastRecv = 0;
        } else {
            if (VDBG) {
                log("onDataStallAlarm: tag=" + tag + " Sent " + String.valueOf(mSentSinceLastRecv) +
                    " pkts since last received, < watchdogTrigger=" + hangWatchdogTrigger);
            }
        }
        startDataStallAlarm(suspectedStall);
    }


    private void updateDataActivity() {
        long sent, received;

        Activity newActivity;

        TxRxSum preTxRxSum = new TxRxSum(mTxPkts, mRxPkts);
        TxRxSum curTxRxSum = new TxRxSum();
        curTxRxSum.updateTxRxSum();
        mTxPkts = curTxRxSum.txPkts;
        mRxPkts = curTxRxSum.rxPkts;

        if (VDBG) {
            log("updateDataActivity: curTxRxSum=" + curTxRxSum + " preTxRxSum=" + preTxRxSum);
        }

        if (mNetStatPollEnabled && (preTxRxSum.txPkts > 0 || preTxRxSum.rxPkts > 0)) {
            sent = mTxPkts - preTxRxSum.txPkts;
            received = mRxPkts - preTxRxSum.rxPkts;

            if (VDBG) log("updateDataActivity: sent=" + sent + " received=" + received);
            if ( sent > 0 && received > 0 ) {
                newActivity = Activity.DATAINANDOUT;
            } else if (sent > 0 && received == 0) {
                newActivity = Activity.DATAOUT;
            } else if (sent == 0 && received > 0) {
                newActivity = Activity.DATAIN;
            } else {
                newActivity = Activity.NONE;
            }

            if (mActivity != newActivity && mIsScreenOn) {
                if (VDBG) log("updateDataActivity: newActivity=" + newActivity);
                mActivity = newActivity;
                mPhone.notifyDataActivity();
            }
        }
    }

    private Runnable mPollNetStat = new Runnable()
    {
        @Override
        public void run() {
            updateDataActivity();

            if (mIsScreenOn) {
                mNetStatPollPeriod = Settings.Secure.getInt(mResolver,
                        Settings.Secure.PDP_WATCHDOG_POLL_INTERVAL_MS, POLL_NETSTAT_MILLIS);
            } else {
                mNetStatPollPeriod = Settings.Secure.getInt(mResolver,
                        Settings.Secure.PDP_WATCHDOG_LONG_POLL_INTERVAL_MS,
                        POLL_NETSTAT_SCREEN_OFF_MILLIS);
            }

            if (mNetStatPollEnabled) {
                mDataConnectionTracker.postDelayed(this, mNetStatPollPeriod);
            }
        }
    };

    /**
     * Returns true if the last fail cause is something that
     * seems like it deserves an error notification.
     * Transient errors are ignored
     */
    private boolean shouldPostNotification(GsmDataConnection.FailCause  cause) {
        return (cause != GsmDataConnection.FailCause.UNKNOWN);
    }

    /**
     * Return true if data connection need to be setup after disconnected due to
     * reason.
     *
     * @param reason the reason why data is disconnected
     * @return true if try setup data connection is need for this reason
     */
    private boolean retryAfterDisconnected(String reason) {
        boolean retry = true;

        if ( Phone.REASON_RADIO_TURNED_OFF.equals(reason) ) {
            retry = false;
        }
        return retry;
    }

    private void reconnectAfterFail(FailCause lastFailCauseCode,
                                    ApnContext apnContext, int retryOverride) {
        if (apnContext == null) {
            loge("reconnectAfterFail: apnContext == null, impossible");
            return;
        }
        if ((apnContext.getState() == State.FAILED) &&
            (apnContext.getDataConnection() != null)) {
            if (!apnContext.getDataConnection().isRetryNeeded()) {
                if (!apnContext.getApnType().equals(Phone.APN_TYPE_DEFAULT)) {
                    mPhone.notifyDataConnection(Phone.REASON_APN_FAILED, apnContext.getApnType());
                    return;
                }
                if (mReregisterOnReconnectFailure) {
                    // We've re-registerd once now just retry forever.
                    apnContext.getDataConnection().retryForeverUsingLastTimeout();
                } else {
                    // Try to Re-register to the network.
                    if (DBG) log("reconnectAfterFail: activate failed, Reregistering to network");
                    mReregisterOnReconnectFailure = true;
                    mPhone.getServiceStateTracker().reRegisterNetwork(null);
                    apnContext.getDataConnection().resetRetryCount();
                    return;
                }
            }

            // If retry needs to be backed off for specific case (determined by RIL/Modem)
            // use the specified timer instead of pre-configured retry pattern.
            int nextReconnectDelay = retryOverride;
            if (nextReconnectDelay < 0) {
                nextReconnectDelay = apnContext.getDataConnection().getRetryTimer();
                apnContext.getDataConnection().increaseRetryCount();
            }
            startAlarmForReconnect(nextReconnectDelay, apnContext);

            if (!shouldPostNotification(lastFailCauseCode)) {
                if (DBG) {
                    log("reconnectAfterFail: NOT Posting GPRS Unavailable notification "
                                + "-- likely transient error");
                }
            } else {
                notifyNoData(lastFailCauseCode, apnContext);
            }
        }
    }

    private void startAlarmForReconnect(int delay, ApnContext apnContext) {

        if (DBG) {
            log("Schedule alarm for reconnect: activate failed. Scheduling next attempt for "
                + (delay / 1000) + "s");
        }

        DataConnectionAc dcac = apnContext.getDataConnectionAc();

        if ((dcac == null) || (dcac.dataConnection == null)) {
            // should not happen, but just in case.
            loge("null dcac or dc.");
            return;
        }

        AlarmManager am =
            (AlarmManager) mPhone.getContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(INTENT_RECONNECT_ALARM + '.' +
                                   dcac.dataConnection.getDataConnectionId());
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_REASON, apnContext.getReason());
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_TYPE,
                        dcac.dataConnection.getDataConnectionId());
        String apnType = apnContext.getApnType();
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mGsmPhone.getMySimId());
        }

        PendingIntent alarmIntent = PendingIntent.getBroadcast (mPhone.getContext(), 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

        dcac.setReconnectIntentSync(alarmIntent);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delay, alarmIntent);

    }

    private void startDataStallAlarm(boolean suspectedStall) {
        int nextAction = getRecoveryAction();
        int delayInMs;

        // If screen is on or data stall is currently suspected, set the alarm
        // with an aggresive timeout.
        if (mIsScreenOn || suspectedStall || RecoveryAction.isAggressiveRecovery(nextAction)) {
            delayInMs = Settings.Secure.getInt(mResolver,
                                       Settings.Secure.DATA_STALL_ALARM_AGGRESSIVE_DELAY_IN_MS,
                                       DATA_STALL_ALARM_AGGRESSIVE_DELAY_IN_MS_DEFAULT);
        } else {
            delayInMs = Settings.Secure.getInt(mResolver,
                                       Settings.Secure.DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS,
                                       DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT);
        }

        mDataStallAlarmTag += 1;
        if (VDBG) {
            log("startDataStallAlarm: tag=" + mDataStallAlarmTag +
                    " delay=" + (delayInMs / 1000) + "s");
        }
        AlarmManager am =
            (AlarmManager) mPhone.getContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(INTENT_DATA_STALL_ALARM);
        intent.putExtra(DATA_STALL_ALARM_TAG_EXTRA, mDataStallAlarmTag);
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mPhone.getMySimId());
        }
        mDataStallAlarmIntent = PendingIntent.getBroadcast(mPhone.getContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delayInMs, mDataStallAlarmIntent);
    }

    private void stopDataStallAlarm() {
        AlarmManager am =
            (AlarmManager) mPhone.getContext().getSystemService(Context.ALARM_SERVICE);

        if (VDBG) {
            log("stopDataStallAlarm: current tag=" + mDataStallAlarmTag +
                    " mDataStallAlarmIntent=" + mDataStallAlarmIntent);
        }
        mDataStallAlarmTag += 1;
        if (mDataStallAlarmIntent != null) {
            am.cancel(mDataStallAlarmIntent);
            mDataStallAlarmIntent = null;
        }
    }

    @Override
    protected void restartDataStallAlarm() {
        if (isConnected() == false) return;
        // To be called on screen status change.
        // Do not cancel the alarm if it is set with aggressive timeout.
        int nextAction = getRecoveryAction();

        if (RecoveryAction.isAggressiveRecovery(nextAction)) {
            if (DBG) log("data stall recovery action is pending. not resetting the alarm.");
            return;
        }
        stopDataStallAlarm();
        startDataStallAlarm(DATA_STALL_NOT_SUSPECTED);
    }

    private void notifyNoData(GsmDataConnection.FailCause lastFailCauseCode,
                              ApnContext apnContext) {
        if (DBG) log( "notifyNoData: type=" + apnContext.getApnType());
        apnContext.setState(State.FAILED);
        if (lastFailCauseCode.isPermanentFail()
            && (!apnContext.getApnType().equals(Phone.APN_TYPE_DEFAULT))) {
            mPhone.notifyDataConnectionFailed(apnContext.getReason(), apnContext.getApnType());
        }
    }

    private void onRecordsLoaded() {
        if (DBG) log("onRecordsLoaded: createAllApnList");
        int gprsDefaultSIM = getDataConnectionFromSetting();
        logd("onRecordsLoaded gprsDefaultSIM:" + gprsDefaultSIM);
     
        if(gprsDefaultSIM == mGsmPhone.getMySimId()){
           mGsmPhone.setGprsConnType(GeminiNetworkSubUtil.CONN_TYPE_ALWAYS);
        }
        // MTK Put the query to threads
        new Thread(new Runnable() {
            public void run() {
                createAllApnList();
                if (mPhone.mCM.getRadioState().isOn()) {
                    if (DBG) log("onRecordsLoaded: notifying data availability");
                    notifyOffApnsOfAvailability(Phone.REASON_SIM_LOADED);
                }
                // Need to re-schedule setup data request by sending message to prevent synchronization problem,
                // since we spawn thread here to process createAllApnList(). (ALPS00294899)
                sendMessage(obtainMessage(EVENT_TRY_SETUP_DATA, Phone.REASON_SIM_LOADED));                
            }
        }).start();

    }

    @Override
    protected void onSetDependencyMet(String apnType, boolean met) {
        // don't allow users to tweak hipri to work around default dependency not met
        if (Phone.APN_TYPE_HIPRI.equals(apnType)) return;

        ApnContext apnContext = mApnContexts.get(apnType);
        if (apnContext == null) {
            loge("onSetDependencyMet: ApnContext not found in onSetDependencyMet(" +
                    apnType + ", " + met + ")");
            return;
        }
        applyNewState(apnContext, apnContext.isEnabled(), met);
        if (Phone.APN_TYPE_DEFAULT.equals(apnType)) {
            // tie actions on default to similar actions on HIPRI regarding dependencyMet
            apnContext = mApnContexts.get(Phone.APN_TYPE_HIPRI);
            if (apnContext != null) applyNewState(apnContext, apnContext.isEnabled(), met);
        }
    }

    private void applyNewState(ApnContext apnContext, boolean enabled, boolean met) {
        boolean cleanup = false;
        boolean trySetup = false;
        if (DBG) {
            log("applyNewState(" + apnContext.getApnType() + ", " + enabled +
                    "(" + apnContext.isEnabled() + "), " + met + "(" +
                    apnContext.getDependencyMet() +"))");
        }
        if (apnContext.isReady()) {
            if (enabled && met) {
                State state = apnContext.getState();
                if (state == State.CONNECTED || state == State.CONNECTING) {
                    return;
                } else {
                    apnContext.setReason(Phone.REASON_DATA_ENABLED);
                    trySetup = true;
                }
            } else if (!enabled) {
                cleanup = true;
                apnContext.setReason(Phone.REASON_DATA_DISABLED);
            } else {
                cleanup = true;
                apnContext.setReason(Phone.REASON_DATA_DEPENDENCY_UNMET);
            }
        } else {
            if (enabled && met) {
                if (apnContext.isEnabled()) {
                    apnContext.setReason(Phone.REASON_DATA_DEPENDENCY_MET);
                } else {
                    apnContext.setReason(Phone.REASON_DATA_ENABLED);
                }
                if (apnContext.getState() == State.FAILED) {
                    apnContext.setState(State.IDLE);
                }
                trySetup = true;
            }
        }
        apnContext.setEnabled(enabled);
        apnContext.setDependencyMet(met);
        if (cleanup) cleanUpConnection(true, apnContext);
        if (trySetup) trySetupData(apnContext);
    }

    private DataConnection checkForConnectionForApnContext(ApnContext apnContext) {
        // Loop through all apnContexts looking for one with a conn that satisfies this apnType
        String apnType = apnContext.getApnType();
        ApnSetting dunSetting = null;

        if (Phone.APN_TYPE_DUN.equals(apnType)) {
            dunSetting = fetchDunApn();
        }

        for (ApnContext c : mApnContexts.values()) {
            DataConnection conn = c.getDataConnection();
            if (conn != null) {
                ApnSetting apnSetting = c.getApnSetting();
                if (dunSetting != null) {
                    if (dunSetting.equals(apnSetting)) {
                        if (DBG) {
                            log("checkForConnectionForApnContext: apnContext=" + apnContext +
                                    " found conn=" + conn);
                        }
                        return conn;
                    }
                } else if (apnSetting != null && apnSetting.canHandleType(apnType)) {
                    if (DBG) {
                        log("checkForConnectionForApnContext: apnContext=" + apnContext +
                                " found conn=" + conn);
                    }
                    return conn;
                }
            }
        }
        if (DBG) log("checkForConnectionForApnContext: apnContext=" + apnContext + " NO conn");
        return null;
    }

    @Override
    protected void onEnableApn(int apnId, int enabled) {
        ApnContext apnContext = mApnContexts.get(apnIdToType(apnId));
        if (apnContext == null) {
            loge("onEnableApn(" + apnId + ", " + enabled + "): NO ApnContext");
            return;
        }
        // TODO change our retry manager to use the appropriate numbers for the new APN
        if (DBG) log("onEnableApn: apnContext=" + apnContext + " call applyNewState");
        applyNewState(apnContext, enabled == ENABLED, apnContext.getDependencyMet());
    }

    @Override
    // TODO: We shouldnt need this.
    protected boolean onTrySetupData(String reason) {
        if (DBG) log("onTrySetupData: reason=" + reason);
        setupDataOnReadyApns(reason);
        return true;
    }

    protected boolean onTrySetupData(ApnContext apnContext) {
        if (DBG) log("onTrySetupData: apnContext=" + apnContext);
        return trySetupData(apnContext);
    }

    @Override
    protected void onRoamingOff() {
        if (DBG) log("onRoamingOff");

        if (getDataOnRoamingEnabled() == false) {
            notifyOffApnsOfAvailability(Phone.REASON_ROAMING_OFF);
            setupDataOnReadyApns(Phone.REASON_ROAMING_OFF);
        } else {
            notifyDataConnection(Phone.REASON_ROAMING_OFF);
        }
    }

    @Override
    protected void onRoamingOn() {
        if (getDataOnRoamingEnabled()) {
            if (DBG) log("onRoamingOn: setup data on roaming");
            setupDataOnReadyApns(Phone.REASON_ROAMING_ON);
            notifyDataConnection(Phone.REASON_ROAMING_ON);
        } else {
            if (DBG) log("onRoamingOn: Tear down data connection on roaming.");
            cleanUpAllConnections(true, Phone.REASON_ROAMING_ON);
            notifyOffApnsOfAvailability(Phone.REASON_ROAMING_ON);
        }
    }

    @Override
    protected void onRadioAvailable() {
        if (DBG) log("onRadioAvailable");
        if (mPhone.getSimulatedRadioControl() != null) {
            // Assume data is connected on the simulator
            // FIXME  this can be improved
            // setState(State.CONNECTED);
            notifyDataConnection(null);

            log("onRadioAvailable: We're on the simulator; assuming data is connected");
        }

        if (mPhone.mIccRecords.getRecordsLoaded()) {
            notifyOffApnsOfAvailability(null);
        }

        if (getOverallState() != State.IDLE) {
            cleanUpConnection(true, null);
        }
    }

    @Override
    protected void onRadioOffOrNotAvailable() {
        // Make sure our reconnect delay starts at the initial value
        // next time the radio comes on

        for (DataConnection dc : mDataConnections.values()) {
            dc.resetRetryCount();
        }
        mReregisterOnReconnectFailure = false;

        if (mPhone.getSimulatedRadioControl() != null) {
            // Assume data is connected on the simulator
            // FIXME  this can be improved
            log("We're on the simulator; assuming radio off is meaningless");
        } else {
            if (DBG) log("onRadioOffOrNotAvailable: is off and clean up all connections");
            cleanUpAllConnections(true, Phone.REASON_RADIO_TURNED_OFF);
        }
        notifyOffApnsOfAvailability(null);
    }

    @Override
    protected void onDataSetupComplete(AsyncResult ar) {

        DataConnection.FailCause cause = DataConnection.FailCause.UNKNOWN;
        boolean handleError = false;
        ApnContext apnContext = null;

        if(ar.userObj instanceof ApnContext){
            apnContext = (ApnContext)ar.userObj;
        } else {
            throw new RuntimeException("onDataSetupComplete: No apnContext");
        }

        if (isDataSetupCompleteOk(ar)) {
            DataConnectionAc dcac = apnContext.getDataConnectionAc();

            if (RADIO_TESTS) {
                // Note: To change radio.test.onDSC.null.dcac from command line you need to
                // adb root and adb remount and from the command line you can only change the
                // value to 1 once. To change it a second time you can reboot or execute
                // adb shell stop and then adb shell start. The command line to set the value is:
                //   adb shell sqlite3 /data/data/com.android.providers.settings/databases/settings.db "insert into system (name,value) values ('radio.test.onDSC.null.dcac', '1');"
                ContentResolver cr = mPhone.getContext().getContentResolver();
                String radioTestProperty = "radio.test.onDSC.null.dcac";
                if (Settings.System.getInt(cr, radioTestProperty, 0) == 1) {
                    log("onDataSetupComplete: " + radioTestProperty +
                            " is true, set dcac to null and reset property to false");
                    dcac = null;
                    Settings.System.putInt(cr, radioTestProperty, 0);
                    log("onDataSetupComplete: " + radioTestProperty + "=" +
                            Settings.System.getInt(mPhone.getContext().getContentResolver(),
                                    radioTestProperty, -1));
                }
            }
            if (dcac == null) {
                log("onDataSetupComplete: no connection to DC, handle as error");
                cause = DataConnection.FailCause.CONNECTION_TO_DATACONNECTIONAC_BROKEN;
                handleError = true;
            } else {
                DataConnection dc = apnContext.getDataConnection();

                if (DBG) {
                    // TODO We may use apnContext.getApnSetting() directly
                    // instead of getWaitingApns().get(0)
                    String apnStr = "<unknown>";
                    if (apnContext.getWaitingApns() != null
                            && !apnContext.getWaitingApns().isEmpty()){
                        apnStr = apnContext.getWaitingApns().get(0).apn;
                    }
                    log("onDataSetupComplete: success apn=" + apnStr);
                }
                ApnSetting apn = apnContext.getApnSetting();
                if (apn.proxy != null && apn.proxy.length() != 0) {
                    try {
                        String port = apn.port;
                        if (TextUtils.isEmpty(port)) port = "8080";
                        ProxyProperties proxy = new ProxyProperties(apn.proxy,
                                Integer.parseInt(port), null);
                        dcac.setLinkPropertiesHttpProxySync(proxy);
                    } catch (NumberFormatException e) {
                        loge("onDataSetupComplete: NumberFormatException making ProxyProperties (" +
                                apn.port + "): " + e);
                    }
                }

                // everything is setup
                if(TextUtils.equals(apnContext.getApnType(),Phone.APN_TYPE_DEFAULT)) {
                    SystemProperties.set("gsm.defaultpdpcontext.active", "true");
                    if (canSetPreferApn && mPreferredApn == null) {
                        if (DBG) log("onDataSetupComplete: PREFERED APN is null");
                        mPreferredApn = apnContext.getApnSetting();
                        if (mPreferredApn != null) {
                            setPreferredApn(mPreferredApn.id);
                        }
                    }
                } else {
                    SystemProperties.set("gsm.defaultpdpcontext.active", "false");
                }

                //MTK_OP03_PROTECT_START 
                if (TextUtils.equals(apnContext.getApnType(),Phone.APN_TYPE_HIPRI)&& "OP03".equals(SystemProperties.get("ro.operator.optr"))) {
                    if (canSetPreferTetheringApn && mPreferredTetheringApn == null) {
                        mPreferredTetheringApn = apnContext.getApnSetting();
                        if (mPreferredTetheringApn != null) {
                            setPreferredTetheringApn(mPreferredTetheringApn.id);
                        }
                    }
                }
                //MTK_OP03_PROTECT_END

                // Notify call start again if call is not IDLE and not concurrent
                if (((GsmCallTracker)mGsmPhone.getCallTracker()).state != Phone.State.IDLE &&
                        !mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                    if (DBG) log("onDataSetupComplete: In 2G phone call, notify data REASON_VOICE_CALL_STARTED");
                    notifyDataConnection(Phone.REASON_VOICE_CALL_STARTED);
                }
                notifyDefaultData(apnContext);	
                mGsmPhone.updateSimIndicateState();

                //MTK-START [mtk04070][111205][ALPS00093395]Add for SCRI 
                /* Add by MTK03594 for SCRI feature */                                
                startScriPoll();                
                //MTK-END [mtk04070][111205][ALPS00093395]Add for SCRI 
            }
        } else {
            String apnString;

            cause = (DataConnection.FailCause) (ar.result);
            if (DBG) {
                try {
                    apnString = apnContext.getWaitingApns().get(0).apn;
                } catch (Exception e) {
                    apnString = "<unknown>";
                }
                log(String.format("onDataSetupComplete: error apn=%s cause=%s", apnString, cause));
            }
            if (cause.isEventLoggable()) {
                // Log this failure to the Event Logs.
                int cid = getCellLocationId();
                EventLog.writeEvent(EventLogTags.PDP_SETUP_FAIL,
                        cause.ordinal(), cid, TelephonyManager.getDefault().getNetworkType());
            }

            // Count permanent failures and remove the APN we just tried
            if (cause.isPermanentFail()) apnContext.decWaitingApnsPermFailCount();

            apnContext.removeNextWaitingApn();
            if (DBG) {
                log(String.format("onDataSetupComplete: WaitingApns.size=%d" +
                        " WaitingApnsPermFailureCountDown=%d",
                        apnContext.getWaitingApns().size(),
                        apnContext.getWaitingApnsPermFailCount()));
            }
            handleError = true;
        }

        if (handleError) {
            // See if there are more APN's to try
            if (apnContext.getWaitingApns().isEmpty()) {
                if (apnContext.getWaitingApnsPermFailCount() == 0) {
                    if (DBG) {
                        log("onDataSetupComplete: All APN's had permanent failures, stop retrying");
                    }
                    apnContext.setState(State.FAILED);
                    mPhone.notifyDataConnection(Phone.REASON_APN_FAILED, apnContext.getApnType());

                    apnContext.setDataConnection(null);
                    apnContext.setDataConnectionAc(null);
                    if (apnContext.getApnType() == Phone.APN_TYPE_MMS) {
                        enableWaitingApn();
                    }

                    if (PhoneFactory.isDualTalkMode()) {
                        if (apnContext.getApnType() != Phone.APN_TYPE_DEFAULT && mWaitingApnList.isEmpty()) {
                            // try to restore default
                            trySetupData(Phone.REASON_DATA_ENABLED, Phone.APN_TYPE_DEFAULT);
                        }
                    }
					
                    if (DBG) {
                        // log("onDataSetupComplete: permanent error apn=%s" + apnString );
                    }
                } else {
                    if (DBG) log("onDataSetupComplete: Not all permanent failures, retry");
                    // check to see if retry should be overridden for this failure.
                    int retryOverride = -1;
                    if (ar.exception instanceof DataConnection.CallSetupException) {
                        retryOverride =
                            ((DataConnection.CallSetupException)ar.exception).getRetryOverride();
                    }
                    if (retryOverride == RILConstants.MAX_INT) {
                        if (DBG) log("No retry is suggested.");
                    } else {
                        startDelayedRetry(cause, apnContext, retryOverride);
                    }
                }
            } else {
                if (DBG) log("onDataSetupComplete: Try next APN");
                apnContext.setState(State.SCANNING);
                // Wait a bit before trying the next APN, so that
                // we're not tying up the RIL command channel
                startAlarmForReconnect(APN_DELAY_MILLIS, apnContext);
            }
        }
    }

    /**
     * Called when EVENT_DISCONNECT_DONE is received.
     */
    @Override
    protected void onDisconnectDone(int connId, AsyncResult ar) {
        boolean enableApnRet = false;
        ApnContext apnContext = null;

        if(DBG) log("onDisconnectDone: EVENT_DISCONNECT_DONE connId=" + connId);
        if (ar.userObj instanceof ApnContext) {
            apnContext = (ApnContext) ar.userObj;
        } else {
            loge("Invalid ar in onDisconnectDone");
            return;
        }

        if(PhoneFactory.isDualTalkMode())
        {
            enableApnRet = enableWaitingApn();
            if (enableApnRet) {
                if (apnContext.getApnType() == Phone.APN_TYPE_DEFAULT) {
                    // avoid default retry, ban retry
                    apnContext.setReason(Phone.REASON_RADIO_TURNED_OFF);
                    logd("onDisconnectoinDone: set reason to radio turned off to avoid retry.");
                }
            }
        }
        else
        {
        if (apnContext.getApnType() == Phone.APN_TYPE_MMS) {
            enableWaitingApn();
        }
        }

        apnContext.setState(State.IDLE);

        if (apnContext.isReactive()) {
            if(DBG) log("onDisconnectDone(): isReactive() == true, notify " + apnContext.getApnType() +" APN with state CONNECTING");
            mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType(), Phone.DataState.CONNECTING);
            apnContext.setReactive(false);
        } else {
            mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        }

        // if all data connection are gone, check whether Airplane mode request was
        // pending.
        if (isDisconnected()) {
            //Modify by mtk01411: Only all data connections are terminated, it is necessary to invoke the stopScriPoll()
            if(DBG) log("All data connections are terminated:stopScriPoll()");                    	
            stopScriPoll();        	
            if (mPhone.getServiceStateTracker().processPendingRadioPowerOffAfterDataOff()) {
                // Radio will be turned off. No need to retry data setup
                apnContext.setApnSetting(null);
                apnContext.setDataConnection(null);
                apnContext.setDataConnectionAc(null);
                return;
            }
        }
        mGsmPhone.updateSimIndicateState();
        //MTK-START [mtk04070][111205][ALPS00093395]Stop SCRI polling
        //Modify by mtk01411: Only all data connections are terminated, it is necessary to invoke the stopScriPoll()
        //stopScriPoll();
        //MTK-END [mtk04070][111205][ALPS00093395]Stop SCRI polling

        // If APN is still enabled, try to bring it back up automatically
        if (apnContext.isReady() && retryAfterDisconnected(apnContext.getReason())) {
            SystemProperties.set("gsm.defaultpdpcontext.active", "false");  // TODO - what the heck?  This shoudld go
            // Wait a bit before trying the next APN, so that
            // we're not tying up the RIL command channel.
            // This also helps in any external dependency to turn off the context.
            startAlarmForReconnect(APN_DELAY_MILLIS, apnContext);
        } else {
            apnContext.setApnSetting(null);
            apnContext.setDataConnection(null);
            apnContext.setDataConnectionAc(null);
        }

        if (PhoneFactory.isDualTalkMode()) {
            if (enableApnRet == false) {
                if (apnContext.getApnType() != Phone.APN_TYPE_DEFAULT) {
                    // try to restore default
                    trySetupData(Phone.REASON_DATA_ENABLED, Phone.APN_TYPE_DEFAULT);
                }
            }
    }
    }

    protected void onPollPdp() {
        if (getOverallState() == State.CONNECTED) {
            // only poll when connected
            mPhone.mCM.getDataCallList(this.obtainMessage(EVENT_DATA_STATE_CHANGED));
            sendMessageDelayed(obtainMessage(EVENT_POLL_PDP), POLL_PDP_MILLIS);
        }
    }

    @Override
    protected void onVoiceCallStarted() {
        if (DBG) log("onVoiceCallStarted");
        if (isConnected() && ! mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
            if (DBG) log("onVoiceCallStarted stop polling");
            stopNetStatPoll();
            stopDataStallAlarm();
            notifyDataConnection(Phone.REASON_VOICE_CALL_STARTED);
            mGsmPhone.updateSimIndicateState();
        } else if(FeatureOption.MTK_GEMINI_SUPPORT){
            GeminiPhone mGeminiPhone = (GeminiPhone)PhoneFactory.getDefaultPhone();
            int peerSimId = getPeerSimId();
            logd("update sim indicator due to call start");
            if(mGeminiPhone.getDataConnectionStateGemini(peerSimId) == Phone.DataState.CONNECTED 
                    || mGeminiPhone.getDataConnectionStateGemini(peerSimId) == Phone.DataState.SUSPENDED){
                mGeminiPhone.updateSimIndicateStateGemini(peerSimId);
            }
        }
    }

    @Override
    protected void onVoiceCallEnded() {
        if (DBG) log("onVoiceCallEnded");
        if (isConnected()) {
            if (!mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                startNetStatPoll();
                startDataStallAlarm(DATA_STALL_NOT_SUSPECTED);
                notifyDataConnection(Phone.REASON_VOICE_CALL_ENDED);
                mGsmPhone.updateSimIndicateState();
            } else {
                // clean slate after call end.
                resetPollStats();
            }
        } else {
            // reset reconnect timer
            setupDataOnReadyApns(Phone.REASON_VOICE_CALL_ENDED);
            if(FeatureOption.MTK_GEMINI_SUPPORT){
                GeminiPhone mGeminiPhone = (GeminiPhone)PhoneFactory.getDefaultPhone();
                logd("update sim state due to call end");
                int peerSimId = getPeerSimId();
                if(mGeminiPhone.getDataConnectionStateGemini(peerSimId) == Phone.DataState.CONNECTED || mGeminiPhone.getDataConnectionStateGemini(peerSimId) == Phone.DataState.SUSPENDED){
                   mGeminiPhone.updateSimIndicateStateGemini(peerSimId);
                }
            }
        }

        // Notify ConnectivityService to restore default
        if (FeatureOption.MTK_GEMINI_SUPPORT &&
                getPeerSimId() == getDataConnectionFromSetting()) {
            notifyDataConnection(Phone.REASON_VOICE_CALL_ENDED);            
        }
    }

    @Override
    protected void onCleanUpConnection(boolean tearDown, int apnId, String reason) {
        if (DBG) log("onCleanUpConnection");
        ApnContext apnContext = mApnContexts.get(apnIdToType(apnId));
        if (apnContext != null) {
            apnContext.setReason(reason);
            cleanUpConnection(tearDown, apnContext);
        }
    }

    protected boolean isConnected() {
        for (ApnContext apnContext : mApnContexts.values()) {
            if (apnContext.getState() == State.CONNECTED) {
                // At least one context is connected, return true
                return true;
            }
        }
        // There are not any contexts connected, return false
        return false;
    }

    @Override
    public boolean isDisconnected() {
        for (ApnContext apnContext : mApnContexts.values()) {
            if (!apnContext.isDisconnected()) {
                // At least one context was not disconnected return false
                return false;
            }
        }
        // All contexts were disconnected so return true
        return true;
    }

    @Override
    protected void notifyDataConnection(String reason) {
        if (DBG) log("notifyDataConnection: reason=" + reason);
        for (ApnContext apnContext : mApnContexts.values()) {
            if (apnContext.isReady()) {
                if (DBG) log("notifyDataConnection: type:"+apnContext.getApnType());
                mPhone.notifyDataConnection(reason != null ? reason : apnContext.getReason(),
                        apnContext.getApnType());
            }
        }
        notifyOffApnsOfAvailability(reason);
    }

    /**
     * Based on the sim operator numeric, create a list for all possible
     * Data Connections and setup the preferredApn.
     */
    private void createAllApnList() {
        mAllApns = new ArrayList<ApnSetting>();
        String operator = mPhone.mIccRecords.getOperatorNumeric();
        if (operator != null) {
            String selection = "numeric = '" + operator + "'";
            // query only enabled apn.
            // carrier_enabled : 1 means enabled apn, 0 disabled apn.
            selection += " and carrier_enabled = 1";
            if (DBG) log("createAllApnList: selection=" + selection);
            Cursor cursor = null;
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                Uri geminiUri;
                if ("OP01".equals(SystemProperties.get("ro.operator.optr"))) {
                    geminiUri = (Phone.GEMINI_SIM_2 == mPhone.getMySimId())? 
                            Telephony.Carriers.SIM2Carriers.CONTENT_URI : Telephony.Carriers.SIM1Carriers.CONTENT_URI;
                } else {
                    geminiUri = (Phone.GEMINI_SIM_2 == mPhone.getMySimId())? 
                            Telephony.Carriers.GeminiCarriers.CONTENT_URI : Telephony.Carriers.CONTENT_URI;
                }
                cursor = mPhone.getContext().getContentResolver().query(geminiUri, null, selection, null, null);
            } else {
                cursor = mPhone.getContext().getContentResolver().query(
                        Telephony.Carriers.CONTENT_URI, null, selection, null, null);
            }

            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    mAllApns = createApnList(cursor);
                }
                cursor.close();
            }
        }

        if (mAllApns.isEmpty()) {
            if (DBG) log("createAllApnList: No APN found for carrier: " + operator);
            mPreferredApn = null;
            // TODO: What is the right behaviour?
            //notifyNoData(GsmDataConnection.FailCause.MISSING_UNKNOWN_APN);
        } else {
            mPreferredApn = getPreferredApn();
            if (DBG) log("createAllApnList: mPreferredApn_XXX=" + mPreferredApn);
            if (mPreferredApn == null || !mPreferredApn.numeric.equals(operator)) {
                logd("ro.operator.optr:" + SystemProperties.get("ro.operator.optr"));
                if ("OP02".equals(SystemProperties.get("ro.operator.optr"))) {
                    mPreferredApn = setPreferdApnForCU();
                }
            }
            if (mPreferredApn != null && !mPreferredApn.numeric.equals(operator)) {
                mPreferredApn = null;
                setPreferredApn(-1);
            }
            if (DBG) log("createAllApnList: mPreferredApn=" + mPreferredApn);
        }
        //MTK_OP03_PROTECT_START 
        if ("OP03".equals(SystemProperties.get("ro.operator.optr"))) {
            mPreferredTetheringApn = getPreferredTetheringApn();
            logd("Get preferredTetheringApn:" + mPreferredTetheringApn);
            if (mPreferredTetheringApn != null && !mPreferredTetheringApn.numeric.equals(operator)) {
                mPreferredTetheringApn = null;
                setPreferredTetheringApn(-1);
            }
        }
        //MTK_OP03_PROTECT_END
        if (DBG) log("createAllApnList: X mAllApns=" + mAllApns);
    }

    /** Return the id for a new data connection */
    private GsmDataConnection createDataConnection() {
        if (DBG) log("createDataConnection E");

        RetryManager rm = new RetryManager();
        int id = mUniqueIdGenerator.getAndIncrement();
      
        if (id >= TelephonyManager.getMaxPdpNum(mPhone.getMySimId())) {
            loge("Max PDP count is "+ TelephonyManager.getMaxPdpNum(mPhone.getMySimId())+",but request " + (id + 1));
            return null;
        }
        GsmDataConnection conn = GsmDataConnection.makeDataConnection(mPhone, id, rm, this);
        mDataConnections.put(id, conn);
        DataConnectionAc dcac = new DataConnectionAc(conn, LOG_TAG);
        int status = dcac.fullyConnectSync(mPhone.getContext(), this, conn.getHandler());
        if (status == AsyncChannel.STATUS_SUCCESSFUL) {
            mDataConnectionAsyncChannels.put(dcac.dataConnection.getDataConnectionId(), dcac);
        } else {
            loge("createDataConnection: Could not connect to dcac.mDc=" + dcac.dataConnection +
                    " status=" + status);
        }

        // install reconnect intent filter for this data connection.
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_RECONNECT_ALARM + '.' + id);
        mPhone.getContext().registerReceiver(mIntentReceiver, filter, null, mPhone);

        if (DBG) log("createDataConnection() X id=" + id);
        return conn;
    }

    private void configureRetry(DataConnection dc, boolean forDefault) {
        if (dc == null) return;

        if (!dc.configureRetry(getReryConfig(forDefault))) {
            if (forDefault) {
                if (!dc.configureRetry(DEFAULT_DATA_RETRY_CONFIG)) {
                    // Should never happen, log an error and default to a simple linear sequence.
                    loge("configureRetry: Could not configure using " +
                            "DEFAULT_DATA_RETRY_CONFIG=" + DEFAULT_DATA_RETRY_CONFIG);
                    dc.configureRetry(20, 2000, 1000);
                }
            } else {
                if (!dc.configureRetry(SECONDARY_DATA_RETRY_CONFIG)) {
                    // Should never happen, log an error and default to a simple sequence.
                    loge("configureRetry: Could note configure using " +
                            "SECONDARY_DATA_RETRY_CONFIG=" + SECONDARY_DATA_RETRY_CONFIG);
                    dc.configureRetry("max_retries=3, 333, 333, 333");
                }
            }
        }
    }

    private void destroyDataConnections() {
        if(mDataConnections != null) {
            if (DBG) log("destroyDataConnections: clear mDataConnectionList");
            mDataConnections.clear();
        } else {
            if (DBG) log("destroyDataConnections: mDataConnecitonList is empty, ignore");
        }
    }

    /**
     * Build a list of APNs to be used to create PDP's.
     *
     * @param requestedApnType
     * @return waitingApns list to be used to create PDP
     *          error when waitingApns.isEmpty()
     */
    private ArrayList<ApnSetting> buildWaitingApns(String requestedApnType) {
        ArrayList<ApnSetting> apnList = new ArrayList<ApnSetting>();

        if (requestedApnType.equals(Phone.APN_TYPE_DUN)) {
            ApnSetting dun = fetchDunApn();
            if (dun != null) {
                apnList.add(dun);
                if (DBG) log("buildWaitingApns: X added APN_TYPE_DUN apnList=" + apnList);
                return apnList;
            }
        }

        if (requestedApnType.equals(Phone.APN_TYPE_DM)) {
            ArrayList<ApnSetting> dm = fetchDMApn();
            return dm;
        }
        String operator = mPhone.mIccRecords.getOperatorNumeric();
        //MTK_OP03_PROTECT_START 
        String optr = SystemProperties.get("ro.operator.optr");
        logd("buildWaitingApns optr " + optr);
        if ("OP03".equals(optr) && Phone.APN_TYPE_HIPRI.equals(requestedApnType)) {
            if (canSetPreferTetheringApn && mPreferredTetheringApn != null) {
                if (mPreferredTetheringApn.numeric.equals(operator)) {
                    logi("Waiting APN set to preferred  Tethering APN");
                    apnList.add(mPreferredTetheringApn);
                    return apnList;
                } else {
                    setPreferredTetheringApn(-1);
                    mPreferredTetheringApn = null;
                }
            }
        }
        //MTK_OP03_PROTECT_END
        int radioTech = mPhone.getServiceState().getRadioTechnology();

        if (requestedApnType.equals(Phone.APN_TYPE_DEFAULT)) {
            if (canSetPreferApn && mPreferredApn != null) {
                if (DBG) {
                    log("buildWaitingApns: Preferred APN:" + operator + ":"
                        + mPreferredApn.numeric + ":" + mPreferredApn);
                }
                if (mPreferredApn.numeric.equals(operator)) {
                    if (mPreferredApn.bearer == 0 || mPreferredApn.bearer == radioTech) {
                        apnList.add(mPreferredApn);
                        if (DBG) log("buildWaitingApns: X added preferred apnList=" + apnList);
                        return apnList;
                    } else {
                        if (DBG) log("buildWaitingApns: no preferred APN");
                        setPreferredApn(-1);
                        mPreferredApn = null;
                    }
                } else {
                    if (DBG) log("buildWaitingApns: no preferred APN");
                    setPreferredApn(-1);
                    mPreferredApn = null;
                }
            }
        }
        if (mAllApns != null) {
            for (ApnSetting apn : mAllApns) {
                if (apn.canHandleType(requestedApnType)) {
                    if (apn.bearer == 0 || apn.bearer == radioTech) {
                        if (DBG) log("apn info : " +apn.toString());
                        apnList.add(apn);
                    }
                }
            }
        } else {
            loge("mAllApns is empty!");
        }
        if (DBG) log("buildWaitingApns: X apnList=" + apnList);
        return apnList;
    }

    private String apnListToString (ArrayList<ApnSetting> apns) {
        StringBuilder result = new StringBuilder();
        for (int i = 0, size = apns.size(); i < size; i++) {
            result.append('[')
                  .append(apns.get(i).toString())
                  .append(']');
        }
        return result.toString();
    }

    private void startDelayedRetry(GsmDataConnection.FailCause cause,
                                   ApnContext apnContext, int retryOverride) {
        notifyNoData(cause, apnContext);
        reconnectAfterFail(cause, apnContext, retryOverride);
    }

    private void setPreferredApn(int pos) {
        if (!canSetPreferApn) {
            log("setPreferredApn: X !canSEtPreferApn");
            return;
        }

        log("setPreferredApn: delete");
        ContentResolver resolver = mPhone.getContext().getContentResolver();
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            Uri geminiUri;
            if ("OP01".equals(SystemProperties.get("ro.operator.optr"))) {
                geminiUri = (Phone.GEMINI_SIM_2 == mPhone.getMySimId())? PREFERAPN_NO_UPDATE_URI_SIM2 : PREFERAPN_NO_UPDATE_URI_SIM1;
            } else {
                geminiUri = (Phone.GEMINI_SIM_2 == mPhone.getMySimId())? PREFERAPN_NO_UPDATE_SIM2_URI : PREFERAPN_NO_UPDATE_URI;
            }
            resolver.delete(geminiUri, null, null);
        } else {
            resolver.delete(PREFERAPN_NO_UPDATE_URI, null, null);
        }

        if (pos >= 0) {
            log("setPreferredApn: insert");
            ContentValues values = new ContentValues();
            values.put(APN_ID, pos);
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                Uri geminiUri;
                if ("OP01".equals(SystemProperties.get("ro.operator.optr"))) {
                    geminiUri = (Phone.GEMINI_SIM_2 == mPhone.getMySimId())? PREFERAPN_NO_UPDATE_URI_SIM2 : PREFERAPN_NO_UPDATE_URI_SIM1;
                } else {
                    geminiUri = (Phone.GEMINI_SIM_2 == mPhone.getMySimId())? PREFERAPN_NO_UPDATE_SIM2_URI : PREFERAPN_NO_UPDATE_URI;
                }
                resolver.insert(geminiUri, values);
            } else {
                resolver.insert(PREFERAPN_NO_UPDATE_URI, values);
            }
        }
    }

    private ApnSetting getPreferredApn() {
        if (mAllApns.isEmpty()) {
            log("getPreferredApn: X not found mAllApns.isEmpty");
            return null;
        }

        Uri queryPreferApnUri = PREFERAPN_NO_UPDATE_URI;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            if ("OP01".equals(SystemProperties.get("ro.operator.optr"))) {
                queryPreferApnUri = (Phone.GEMINI_SIM_2 == mPhone.getMySimId())? PREFERAPN_NO_UPDATE_URI_SIM2 : PREFERAPN_NO_UPDATE_URI_SIM1;
            } else {
                queryPreferApnUri = (Phone.GEMINI_SIM_2 == mPhone.getMySimId())? PREFERAPN_NO_UPDATE_SIM2_URI : PREFERAPN_NO_UPDATE_URI;
            }
        }
        Cursor cursor = mPhone.getContext().getContentResolver().query(
                queryPreferApnUri, new String[] { "_id", "name", "apn" },
                null, null, Telephony.Carriers.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            canSetPreferApn = true;
            if (DBG) log("getPreferredApn: canSetPreferApn= " + canSetPreferApn + ",count " + cursor.getCount());
        } else {
            canSetPreferApn = false;
            if (DBG) log("getPreferredApn: canSetPreferApn= " + canSetPreferApn);
        }

        if (canSetPreferApn && cursor.getCount() > 0) {
            int pos;
            cursor.moveToFirst();
            pos = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers._ID));
            for(ApnSetting p:mAllApns) {
                if (p.id == pos && p.canHandleType(mRequestedApnType)) {
                    log("getPreferredApn: X found apnSetting" + p);
                    cursor.close();
                    return p;
                }
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        log("getPreferredApn: X not found");
        return null;
    }

    @Override
    public void handleMessage (Message msg) {
        if (DBG) log("handleMessage msg=" + msg);

        if (!mPhone.mIsTheCurrentActivePhone || mIsDisposed) {
            loge("handleMessage: Ignore GSM msgs since GSM phone is inactive");
            return;
        }

        switch (msg.what) {
            case EVENT_RECORDS_LOADED:
                onRecordsLoaded();
                break;

            case EVENT_DATA_CONNECTION_DETACHED:
                onDataConnectionDetached();
                break;

            case EVENT_DATA_CONNECTION_ATTACHED:
                onDataConnectionAttached();
                break;

            case EVENT_DATA_STATE_CHANGED:
                onDataStateChanged((AsyncResult) msg.obj);
                break;

            case EVENT_POLL_PDP:
                onPollPdp();
                break;

            case EVENT_DO_RECOVERY:
                doRecovery();
                break;

            case EVENT_APN_CHANGED:
                onApnChanged();
                break;

            case EVENT_PS_RESTRICT_ENABLED:
                /**
                 * We don't need to explicitly to tear down the PDP context
                 * when PS restricted is enabled. The base band will deactive
                 * PDP context and notify us with PDP_CONTEXT_CHANGED.
                 * But we should stop the network polling and prevent reset PDP.
                 */
                if (DBG) log("EVENT_PS_RESTRICT_ENABLED " + mIsPsRestricted);
                stopNetStatPoll();
                stopDataStallAlarm();
                mIsPsRestricted = true;
                break;

            case EVENT_PS_RESTRICT_DISABLED:
                /**
                 * When PS restrict is removed, we need setup PDP connection if
                 * PDP connection is down.
                 */
                if (DBG) log("EVENT_PS_RESTRICT_DISABLED " + mIsPsRestricted);
                mIsPsRestricted  = false;

                //MTK-START [mtk04070][111205][ALPS00093395]Add for SCRI                
                startScriPoll();
                //MTK-END [mtk04070][111205][ALPS00093395]Add for SCRI                
                
                if (isConnected()) {
                    startNetStatPoll();
                    startDataStallAlarm(DATA_STALL_NOT_SUSPECTED);
                } else {
                    // TODO: Should all PDN states be checked to fail?
                    if (mState == State.FAILED) {
                        cleanUpAllConnections(false, Phone.REASON_PS_RESTRICT_ENABLED);
                        resetAllRetryCounts();
                        mReregisterOnReconnectFailure = false;
                    }
                    trySetupData(Phone.REASON_PS_RESTRICT_ENABLED, Phone.APN_TYPE_DEFAULT);
                }
                break;
            case EVENT_TRY_SETUP_DATA:
                if (msg.obj instanceof ApnContext) {
                    onTrySetupData((ApnContext)msg.obj);
                } else if (msg.obj instanceof String) {
                    onTrySetupData((String)msg.obj);
                } else {
                    loge("EVENT_TRY_SETUP request w/o apnContext or String");
                }
                break;

            case EVENT_CLEAN_UP_CONNECTION:
                boolean tearDown = (msg.arg1 == 0) ? false : true;
                if (DBG) log("EVENT_CLEAN_UP_CONNECTION tearDown=" + tearDown);
                if (msg.obj instanceof ApnContext) {
                    cleanUpConnection(tearDown, (ApnContext)msg.obj);
                } else {
                    loge("EVENT_CLEAN_UP_CONNECTION request w/o apn context");
                }
                break;
                
            //MTK-START [mtk04070][111205][ALPS00093395]Add for SCRI                
            case EVENT_SCRI_RESULT:
                logd("[SCRI]EVENT_SCRI_RESULT");
                handleScriResult((AsyncResult) msg.obj);
                break;
                
            case EVENT_SCRI_RETRY_TIMER:
                logd("[SCRI]EVENT_SCRI_RETRY_TIMER");
                //[Begin]Solve [ALPS00239224]Failed to send MMS, mtk04070, 20120227.
                if (mScriManager.isDataTransmitting()) {
                    logd("[SCRI]Data is transmitting, cancel retry mechanism.");
                    mScriManager.mScriRetryCounter = 0;
                    mScriManager.setScriState(ScriManager.STATE_ACTIVATED);
                    mScriManager.setScriDataCount(true);
                    mScriManager.setPsSessionStatus(true);
                }
                else {
                    sendScriCmd(true);
                }	
                //[End]Solve [ALPS00239224]Failed to send MMS, mtk04070, 20120227.
                break;

            case EVENT_SCRI_CMD_RESULT:
                logd("[SCRI]EVENT_SCRI_CMD_RESULT");
                AsyncResult ar= (AsyncResult) msg.obj;
                if(ar.exception != null) {
                   logd("command error in +ESCRI");
                   mScriManager.setScriState(ScriManager.STATE_ACTIVATED);
                }
                break;
                
            case EVENT_NW_RAT_CHANGED:
                logd("[SCRI]EVENT_NW_RAT_CHANGED");
                Integer rat = (Integer) ((AsyncResult) msg.obj).result;                
                int result = mScriManager.getScriNwType(rat.intValue());
                
                switch(result){
                    case ScriManager.SCRI_3G:
                        /// M: Fast Dormancy:Fix InterRAT problem ALPS00364331 @{
                        logd("[SCRI] InterRAT to 3G, Set mIsUmtsMode as true before startScriPoll()");
                        mIsUmtsMode = true;
                        startScriPoll();
                        //mIsUmtsMode = true;
                        /// @} 
                        break;
                    case ScriManager.SCRI_2G:
                        stopScriPoll();
                        mIsUmtsMode = false;
                        break;
                    case ScriManager.SCRI_NONE:
                        break;                    
                    }
                break;
            //MTK-END [mtk04070][111205][ALPS00093395]Add for SCRI                
            case MSG_RESTART_RADIO_OFF_DONE:
                logi("MSG_RESTART_RADIO_OFF_DONE");
                int simId = mPhone.getMySimId();
                int sim3G = SystemProperties.getInt("gsm.3gswitch", 1) == 2 ? Phone.GEMINI_SIM_2 : Phone.GEMINI_SIM_1;
                if (sim3G == simId) {
                    mPhone.mCM.setRadioMode(msg.arg1, obtainMessage(MSG_RESTART_RADIO_ON_DONE));
                } else {
                    log("set radio on through peer phone since current phone is 2G protocol");
                    if (mPhone instanceof GSMPhone) {
                        GSMPhone peerPhone = ((GSMPhone)mPhone).getPeerPhone();
                        ((PhoneBase)peerPhone).mCM.setRadioMode(msg.arg1, obtainMessage(MSG_RESTART_RADIO_ON_DONE));
                    }
                }
                break;
            case MSG_RESTART_RADIO_ON_DONE:
                logi("MSG_RESTART_RADIO_ON_DONE");
                break;
            case EVENT_PS_RAT_CHANGED:
                // RAT change is only nofity active APNs in GsmServiceStateTracker
                // Here notify "off" APNs for RAT change.
                logd("EVENT_PS_RAT_CHANGED");
                notifyOffApnsOfAvailability(Phone.REASON_NW_TYPE_CHANGED);
                break;
            default:
                // handle the message in the super class DataConnectionTracker
                super.handleMessage(msg);
                break;
        }
    }

    protected int getApnProfileID(String apnType) {
        if (TextUtils.equals(apnType, Phone.APN_TYPE_IMS)) {
            return RILConstants.DATA_PROFILE_IMS;
        } else if (TextUtils.equals(apnType, Phone.APN_TYPE_FOTA)) {
            return RILConstants.DATA_PROFILE_FOTA;
        } else if (TextUtils.equals(apnType, Phone.APN_TYPE_CBS)) {
            return RILConstants.DATA_PROFILE_CBS;
        } else if (TextUtils.equals(apnType, Phone.APN_TYPE_MMS)) {
            return RILConstants.DATA_PROFILE_MTK_MMS;
        } else {
            return RILConstants.DATA_PROFILE_DEFAULT;
        }
    }

    private int getCellLocationId() {
        int cid = -1;
        CellLocation loc = mPhone.getCellLocation();

        if (loc != null) {
            if (loc instanceof GsmCellLocation) {
                cid = ((GsmCellLocation)loc).getCid();
            } else if (loc instanceof CdmaCellLocation) {
                cid = ((CdmaCellLocation)loc).getBaseStationId();
            }
        }
        return cid;
    }

    @Override
    protected void log(String s) {
        //MTK-START [mtk04070][111205][ALPS00093395]Use logd
        logd(s);
        //MTK-END [mtk04070][111205][ALPS00093395]Use logd
    }

    //MTK-START [mtk04070][111205][ALPS00093395]MTK proprietary methods/classes/receivers 
    void registerSCRIEvent(GSMPhone p) {
        if (FeatureOption.MTK_FD_SUPPORT) {
            mScriManager = new ScriManager();
            
            if(Settings.System.getInt(mGsmPhone.getContext().getContentResolver(), Settings.System.GPRS_TRANSFER_SETTING, 0) == 1){
                mIsCallPrefer = true;
            }else{
                mIsCallPrefer = false;
            }
                                    
            mScriManager.reset();
            p.mCM.setScriResult(this, EVENT_SCRI_RESULT, null);  //Register with unsolicated result
            p.mSST.registerForRatRegistrants(this, EVENT_NW_RAT_CHANGED, null);

            IntentFilter filter = new IntentFilter();
            //Add for SCRI by MTK03594
            filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
            filter.addAction(TelephonyIntents.ACTION_GPRS_TRANSFER_TYPE);

            //[ALPS00098656][mtk04070]Disable Fast Dormancy when in Tethered mode
            filter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);

            // TODO: Why is this registering the phone as the receiver of the intent
            //       and not its own handler?
            p.getContext().registerReceiver(mIntentReceiverScri, filter, null, p);
               
            //[ALPS00098656][mtk04070]Disable Fast Dormancy when in Tethered mode
            /* Get current tethered mode */
            ConnectivityManager connMgr = (ConnectivityManager) mPhone.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if ((connMgr != null) && (connMgr.getTetheredIfaces() != null))
            { 
               mIsTetheredMode = (connMgr.getTetheredIfaces().length > 0);
               logd("[GsmDataConnectionTracker Constructor]mIsTetheredMode = " + mIsTetheredMode);    
            }
        }
    }
    
    //Add for SCRI design
    //Function to start SCRI polling service
    protected void startScriPoll(){        
        if(FeatureOption.MTK_FD_SUPPORT) {
            if(DBG) logd("[SCRI] startScriPoll (" + scriPollEnabled + "," + mScriManager.getScriState() + "," + mIsUmtsMode + ")");
            if(scriPollEnabled == false && mIsUmtsMode && isConnected()) {
                if(mScriManager.getScriState() == ScriManager.STATE_NONE) {
                     scriPollEnabled = true;                     
                     mPollScriStat.run();
                     mScriManager.setPsSessionStatus(true);
                     mScriManager.setScriState(ScriManager.STATE_ACTIVATED);
               }
            }
       }
        
    }

    //Function to stop SCRI polling service
    protected void stopScriPoll()
    {                            
        if(FeatureOption.MTK_FD_SUPPORT && (mScriManager.getScriState() != ScriManager.STATE_NONE)) {
            if(DBG) logd("[SCRI]stopScriPoll");
            mScriManager.reset();
            scriPollEnabled = false;
            mScriManager.setScriState(ScriManager.STATE_NONE);
            mScriManager.setPsSessionStatus(false);
            mDataConnectionTracker.removeMessages(EVENT_SCRI_RETRY_TIMER);
            removeCallbacks(mPollScriStat);
        }
    }
    
    protected void handleScriResult(AsyncResult ar){
        Integer scriResult = (Integer)(ar.result);
        if(DBG) logd("[SCRI] handleScriResult :" + scriResult);
        
        if (ar.exception == null) {
            if (scriResult == ScriManager.SCRI_RESULT_REQ_SENT || 
                scriResult == ScriManager.SCRI_NO_PS_DATA_SESSION ||
                scriResult == ScriManager.SCRI_NOT_ALLOWED) {      //[ALPS00097617][mtk04070]Handle SCRI_NOT_ALLOWED event
                mScriManager.setScriState(ScriManager.STATE_ACTIVATED);
                mScriManager.setPsSessionStatus(false);
            }
            //Add by mtk01411 to handle RAU with FollowOnProceed and RRC connected scenario
            else if(scriResult == ScriManager.SCRI_RAU_ENABLED) {
                if(DBG) logd("[SCRI] RAU with FollowOnProceed: RRC in connected state,scriState=" + mScriManager.getScriState());	
            	  mScriManager.mPeriodicRAUFollowOnProceedEnable = true; 
            
            } else {
                if (DBG) logd("[SCRI] mScriManager.retryCounter :" + mScriManager.mScriRetryCounter);
                mScriManager.setPsSessionStatus(false);
                if (mScriManager.mScriRetryCounter < ScriManager.SCRI_MAX_RETRY_COUNTER) {
                    mScriManager.setScriState(ScriManager.STATE_RETRY);

                    if(mIsScreenOn) {
                        mScriManager.mScriRetryTimer = mScriManager.mScriTriggerDataCounter * 1000;
                    } else {
                        mScriManager.mScriRetryTimer = mScriManager.mScriTriggerDataOffCounter * 1000;
                    }

                    if(mScriManager.mScriRetryTimer > ScriManager.SCRI_MAX_RETRY_TIMERS) {
                        mScriManager.mScriRetryTimer = ScriManager.SCRI_MAX_RETRY_TIMERS;
                    }

                    mScriManager.mScriRetryCounter++;
                    Message msg = mDataConnectionTracker.obtainMessage(EVENT_SCRI_RETRY_TIMER, null);
                    mDataConnectionTracker.sendMessageDelayed(msg, mScriManager.mScriRetryTimer);
                    logd("[SCRI] Retry counter = " + mScriManager.mScriRetryCounter + ", timeout = " + mScriManager.mScriRetryTimer);
                } else {
                    //No retry
                    mScriManager.mScriRetryCounter = 0;
                    mScriManager.setScriState(ScriManager.STATE_ACTIVATED);
                }
            }
        } else {
            mScriManager.setScriState(ScriManager.STATE_RETRY);
            Message msg = mDataConnectionTracker.obtainMessage(EVENT_SCRI_RETRY_TIMER, null);
            mDataConnectionTracker.sendMessageDelayed(msg, ScriManager.SCRI_MAX_RETRY_TIMERS);
        }
    }
    
    protected void sendScriCmd(boolean retry) {
        try{
            if((mScriManager.getPsSessionStatus() || retry) && 
               (mPhone.getState() == Phone.State.IDLE) && 
               mIsUmtsMode && 
               !mIsTetheredMode)   //[ALPS00098656][mtk04070]Disable Fast Dormancy when in Tethered mode
            {
                logd("[SCRI] Send SCRI command:" + mIsCallPrefer + ":" + retry);
                if(!mIsScreenOn) {
                    mPhone.mCM.setScri(true, obtainMessage(EVENT_SCRI_CMD_RESULT));
                    mScriManager.setScriState(ScriManager.STATE_ACTIVIATING);
                }else{
                    boolean forceFlag = false;
                    
                    //Send SCRI with force flag when the data prefer is on and both sims are on
                    if(FeatureOption.MTK_GEMINI_SUPPORT){
                       if(!mIsCallPrefer) {
                            int peerSimId = getPeerSimId();
                            GeminiPhone mGeminiPhone = (GeminiPhone)PhoneFactory.getDefaultPhone();
                            if(mGeminiPhone.isRadioOnGemini(peerSimId)){
                              forceFlag = true;
                            }
                        }
                    }
                    
                    //Force to send SCRI msg to NW if MTK_FD_FORCE_REL_SUPPORT is true
                    //ALPS00071650 for FET NW issue
                    if(FeatureOption.MTK_FD_FORCE_REL_SUPPORT){
                       forceFlag = true;
                    }

                    mPhone.mCM.setScri(forceFlag, obtainMessage(EVENT_SCRI_CMD_RESULT));
                    mScriManager.setScriState(ScriManager.STATE_ACTIVIATING);
                                        
                }
            } else {
                logd("[SCRI] Ingore SCRI command due to (" + mScriManager.getPsSessionStatus() + ";" + mPhone.getState() + ";" + ")");
                logd("[SCRI] mIsUmtsMode = " + mIsUmtsMode);
                logd("[SCRI] mIsTetheredMode = " + mIsTetheredMode);
                    mScriManager.setScriState(ScriManager.STATE_ACTIVATED);                
                }            
        }catch(Exception e){
           e.printStackTrace();
        }
    }
    //ADD_END for SCRI
    
    BroadcastReceiver mIntentReceiverScri = new BroadcastReceiver ()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            /*
               Some actions are handled in DataConnectionTracker.java
            */
            if(action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)){  //add by MTK03594 for SCRI
                if (FeatureOption.MTK_GEMINI_SUPPORT && FeatureOption.MTK_FD_SUPPORT) {
                    //Check SIM2 state during data prefer condition
                }
            } else if(action.equals(TelephonyIntents.ACTION_GPRS_TRANSFER_TYPE)) {
                int gprsTransferType = intent.getIntExtra(Phone.GEMINI_GPRS_TRANSFER_TYPE, 0);
                logd("GPRS Transfer type:" + gprsTransferType);
                if(gprsTransferType == 1) {
                    mIsCallPrefer = true;
                } else {
                    mIsCallPrefer = false;
                }
            } else if(action.equals(ConnectivityManager.ACTION_TETHER_STATE_CHANGED)) {
            	  //[ALPS00098656][mtk04070]Disable Fast Dormancy when in Tethered mode
            	  logd("Received ConnectivityManager.ACTION_TETHER_STATE_CHANGED");
                ArrayList<String> active = intent.getStringArrayListExtra(ConnectivityManager.EXTRA_ACTIVE_TETHER);            	
                mIsTetheredMode = ((active != null) && (active.size() > 0));
                logd("[TETHER_STATE_CHANGED]mIsTetheredMode = " + mIsTetheredMode);
            }
        }/* End of onReceive */
    };

    private class ScriManager{
        protected static final boolean DBG = true;
        
        static public final int STATE_NONE=0;
        static public final int STATE_ACTIVIATING=1;
        static public final int STATE_ACTIVATED=2;
        static public final int STATE_RETRY=3;

        static public final int SCRI_NONE = 0;
        static public final int SCRI_3G = 1;
        static public final int SCRI_2G = 2;

        static public final int SCRI_RESULT_REQ_SENT = 0;
        static public final int SCRI_CS_SESSION_ONGOING = 1;
        static public final int SCRI_PS_SIGNALLING_ONGOING = 2;
        static public final int SCRI_NO_PS_DATA_SESSION = 3;
        static public final int SCRI_REQ_NOT_SENT = 4; 
        //[ALPS00097617][mtk04070]Refine handleScriResult function to handle SCRI_NOT_ALLOWED 
        static public final int SCRI_NOT_ALLOWED = 5;
        //Add by mtk01411 to handle RAU with FollowOnProceed and RRC connected scenario
        static public final int SCRI_RAU_ENABLED = 6; 
        
        static public final int SCRI_MAX_RETRY_COUNTER = 3;
        static public final int SCRI_MAX_RETRY_TIMERS = 30 * 1000;
        
        public int mScriGuardTimer;
        public int mScriPollTimer;
        public int mScriTriggerDataCounter;
        public int mScriTriggerDataOffCounter;
        public int mScriRetryTimer;
        public int mScriRetryCounter;

        //Add by mtk01411 to handle RAU with FollowOnProceed and RRC connected scenario
        public boolean mPeriodicRAUFollowOnProceedEnable = false;
        private boolean mScriNeeded;
        private boolean mPsSession;
        private boolean mGuardTimerExpired;
        private int mScriState;
        private int mScriDataCounter;
        private int mScriAddCounter;
        private int mNwType;
        
        protected final String LOG_TAG = "GSM";
            
        public ScriManager(){
            mScriGuardTimer = 0;
            mScriPollTimer = 0;
            mScriDataCounter = 0;
            mScriRetryTimer = 0;            
            mScriAddCounter = 0;
            mScriTriggerDataCounter = 0;
            mScriTriggerDataOffCounter = 0;
            mScriRetryCounter = 0;
            mPsSession = false;
            
            mScriNeeded = false;
            mGuardTimerExpired = false;
            mScriState = STATE_NONE;

            mNwType = SCRI_NONE;
        }

        public void setScriTimer()
        {
            String  str = null;
	          Integer val = 0;  

            try {
                //Get scri guard timer
                str = SystemProperties.get("persist.radio.fd.guard.timer", "60");
				        val = Integer.parseInt(str);
                if(val < 5 || val > 3600) val = 60;
                mScriGuardTimer = val * 1000;            

                //Get scri poll timer
                str = SystemProperties.get("persist.radio.fd.poll.timer", "5");
				        val = Integer.parseInt(str);
                if(val <= 0 || val > 600) val = 5;
                mScriAddCounter = val;
                mScriPollTimer = val * 1000;

                //Get scri data counter for screen on
                str = SystemProperties.get("persist.radio.fd.counter", "20");
                val = Integer.parseInt(str);
                if(val < 5 || val > 3600) val = 20;
                mScriTriggerDataCounter = val;
            
                //Get scri data counter for screen off
                str = SystemProperties.get("persist.radio.fd.off.counter", "20");
                val = Integer.parseInt(str);
                if(val < 5 || val > 3600) val = 20;
                mScriTriggerDataOffCounter = val;

                //Get scri retry timer
                str = SystemProperties.get("persist.radio.fd.retry.timer", "20");
				        val = Integer.parseInt(str);
                if(val < 5 || val > 600) val = 20;
                mScriRetryTimer = val * 1000;
            
                if (DBG) Log.d(LOG_TAG, "[SCRI] init value (" + mScriGuardTimer + "," + mScriPollTimer + ","+ mScriTriggerDataCounter + "," + mScriTriggerDataOffCounter + "," + mScriRetryTimer + ")");
            } catch (Exception e) {
                        e.printStackTrace();
                        mScriGuardTimer = 60 * 1000;
                        mScriPollTimer = 5 * 1000;
                        mScriTriggerDataCounter = 20;
                        mScriTriggerDataOffCounter = 20;
                        mScriRetryTimer = 20 * 1000;
                        mScriAddCounter = 5;
            }/* End of try-catch */
        }

        public void reset(){
            mScriNeeded = false;
            mGuardTimerExpired = false;
            mPsSession = false;
            mScriRetryCounter = 0;
            mScriState = STATE_NONE;
            mScriDataCounter = 0;
            mScriAddCounter = mScriPollTimer/1000;
            setScriTimer();
        }
            
        public void setScriState(int scriState){
            mScriState = scriState;
        }

        public int getScriState(){
            return mScriState;
        }

        public void setPsSessionStatus(boolean hasPsSession) {
            if(hasPsSession) {
                mScriRetryCounter = 0;
            }
           mPsSession = hasPsSession;
        }

        public boolean getPsSessionStatus() {
           return mPsSession;
        }
        
        public void setScriDataCount(boolean reset){
            if(reset == false){
                mScriDataCounter+=mScriAddCounter;
            }else{
                mScriDataCounter = 0;
            }
            if(DBG) Log.d(LOG_TAG, "[SCRI]setScriDataCount:" + mScriDataCounter);
        }

        public boolean isPollTimerTrigger(boolean isScreenOn) {
            if (isScreenOn) {
               return mScriDataCounter >= mScriTriggerDataCounter;
            } else {
               return mScriDataCounter >= mScriTriggerDataOffCounter;
            }
        }

        public int getScriNwType(int networktype){
            if(DBG) Log.d(LOG_TAG, "[SCRI]getScriNwType:" + networktype);
            int nwType = 0;
            
            if(networktype >= TelephonyManager.NETWORK_TYPE_UMTS){
                nwType = SCRI_3G;
            }else if(networktype == TelephonyManager.NETWORK_TYPE_GPRS || networktype == TelephonyManager.NETWORK_TYPE_EDGE){
                nwType = SCRI_2G;
            }else{
                nwType = SCRI_NONE;
            }

            //Only consider 2G -> 3G & 3G -> 2G
            if(nwType != SCRI_NONE && mNwType != nwType)
            {
               mNwType = nwType;
            }else{
               nwType = SCRI_NONE;
            }
            
            return nwType;
        }

        public boolean isDataTransmitting() {
            long deltaTx, deltaRx;
            long preTxPkts = scriTxPkts, preRxPkts = scriRxPkts;

           if(PhoneFactory.isDualTalkMode())
           {
               TxRxSum curTxRxSum = new TxRxSum();

               curTxRxSum.updateTxRxSum();
               scriTxPkts = curTxRxSum.txPkts;
               scriRxPkts = curTxRxSum.rxPkts;
           }
           else
           {
            scriTxPkts = TrafficStats.getMobileTxPackets();
            scriRxPkts = TrafficStats.getMobileRxPackets();
           }

            Log.d(LOG_TAG, "[SCRI]tx: " + preTxPkts + " ==> " + scriTxPkts);
            Log.d(LOG_TAG, "[SCRI]rx  " + preRxPkts + " ==> " + scriRxPkts);

            deltaTx = scriTxPkts - preTxPkts;
            deltaRx = scriRxPkts - preRxPkts;
            Log.d(LOG_TAG, "[SCRI]delta rx " + deltaRx + " tx " + deltaTx);

            return (deltaTx > 0 || deltaRx > 0);
        }
    }

    private Runnable mPollScriStat = new Runnable(){
        public void run() {
            boolean resetFlag = false;

            resetFlag = mScriManager.isDataTransmitting();
            
            //Add by mtk01411 to handle RAU with FollowOnProceed and RRC connected scenario
            if (mScriManager.mPeriodicRAUFollowOnProceedEnable) {
                logd("[SCRI] Detect RAU FollowOnProceed:Force to let resetFlag as true (regard PS session exist)");
                resetFlag = true;
                mScriManager.mPeriodicRAUFollowOnProceedEnable = false;	
            }

            if (mScriManager.getScriState() == ScriManager.STATE_ACTIVATED || mScriManager.getScriState() == ScriManager.STATE_RETRY) {
                logd("[SCRI]act:" + resetFlag);
            
                if (resetFlag){
                    mScriManager.setPsSessionStatus(true);
                    //Disable retry command due to data transfer
                    if (mScriManager.getScriState() == ScriManager.STATE_RETRY) {
                        mDataConnectionTracker.removeMessages(EVENT_SCRI_RETRY_TIMER);
                        mScriManager.setScriState(ScriManager.STATE_ACTIVATED);
                    }
                }
                
                mScriManager.setScriDataCount(resetFlag);
                if (mScriManager.isPollTimerTrigger(mIsScreenOn))
                {
                    mScriManager.setScriDataCount(true);      
                    sendScriCmd(false);
                }
            }

            logd("mPollScriStat");
            if (scriPollEnabled) {
               mDataConnectionTracker.postDelayed(this, mScriManager.mScriPollTimer);
           }
        }/* End of run() */

    };
    
    /* Add by vendor for Multiple PDP Context used in notify overall data state scenario */
    @Override
    public String getActiveApnType() {
        // TODO Auto-generated method stub
        /* Note by mtk01411: Currently, this API is invoked by DefaultPhoneNotifier::notifyDataConnection(sender, reason) */
        /* => Without specifying the apnType: In this case, it means that report the overall data state */
        /* Return the null for apnType to query overall data state */
        return null;
    }

    private int getPeerSimId(){
        return (mGsmPhone.getMySimId() == Phone.GEMINI_SIM_1 ? Phone.GEMINI_SIM_2 : Phone.GEMINI_SIM_1);
    }

    private boolean isCuImsi(String imsi){

        if(imsi != null){
           int mcc = Integer.parseInt(imsi.substring(0,3));
           int mnc = Integer.parseInt(imsi.substring(3,5));
                  
           logd("mcc mnc:" + mcc +":"+ mnc);

            if(mcc == 460 && mnc == 01){
               return true;
            }
            
            if (mcc == 001) {
                return true;
            }
       }

       return false;
  }
    
    private ApnSetting setPreferdApnForCU() {
        String imsi = mGsmPhone.mIccRecords.getIMSI();
        String operator = mPhone.mIccRecords.getOperatorNumeric();
        logd("setPreferdApnForCU imsi:" + imsi + ", operator: " + operator);
        if (!isCuImsi(imsi)) {
            return mPreferredApn;
        }
        Uri baseUri = Telephony.Carriers.CONTENT_URI;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            if ("OP01".equals(SystemProperties.get("ro.operator.optr"))) {
                baseUri = (Phone.GEMINI_SIM_2 == mPhone.getMySimId())? 
                        Telephony.Carriers.SIM2Carriers.CONTENT_URI : Telephony.Carriers.SIM1Carriers.CONTENT_URI;
            } else {
                baseUri = (Phone.GEMINI_SIM_2 == mPhone.getMySimId())? 
                        Telephony.Carriers.GeminiCarriers.CONTENT_URI : Telephony.Carriers.CONTENT_URI;
            }
        }
        ContentResolver resolver = mPhone.getContext().getContentResolver();
        String selection = Telephony.Carriers.APN + "=" + "'3gnet'" + " and " +
            Telephony.Carriers.TYPE + "!='mms'" +" and " + Telephony.Carriers.NUMERIC + " = '"+ operator + "'" + " and " +
            Telephony.Carriers.CARRIER_ENABLED + " = 1";
        Cursor cursor = resolver.query(baseUri, null, selection, null, null);
        if (cursor == null || !cursor.moveToFirst()) {
            if (cursor != null) cursor.close();
            logd("no prefer apn for cu");
            return mPreferredApn;
        }
        Uri uri = Uri.withAppendedPath(baseUri, "preferapn");
        ContentValues values = new ContentValues();
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Carriers._ID));
        logd("set prefer apn for cu " + id);
        values.put("apn_id", id);
        resolver.insert(uri, values);
        ArrayList<ApnSetting> result = createApnList(cursor);
        cursor.close();
        if (result != null && result.size() > 0) {
            mPreferredApn = result.get(0);
        }
        return mPreferredApn;
    }
    
    private void setPreferredTetheringApn(int pos) {
        if (!canSetPreferTetheringApn) {
            return;
        }
        logd("setPreferredTetheringApn " + pos);
        ContentResolver resolver = mPhone.getContext().getContentResolver();
        /* Modify for Gemini by mtk01411 */
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            if (mPhone.getMySimId() == Phone.GEMINI_SIM_1) {
                resolver.delete(PREFER_TETHERING_APN_URI, null, null);
            } else {
                resolver.delete(PREFER_TETHERING_APN_SIM2_URI, null, null);
            }
        } else {
            resolver.delete(PREFER_TETHERING_APN_URI, null, null);
        }

        if (pos >= 0) {
            ContentValues values = new ContentValues();
            values.put(APN_ID, pos);
            /* Modify for Gemini by mtk01411 */
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (mPhone.getMySimId() == Phone.GEMINI_SIM_1) {
                    resolver.insert(PREFER_TETHERING_APN_URI, values);
                } else {
                    resolver.insert(PREFER_TETHERING_APN_SIM2_URI, values);
                }
            } else {
                resolver.insert(PREFER_TETHERING_APN_URI, values);
            }
        }
    }
    
    private ApnSetting getPreferredTetheringApn() {
        if (mAllApns.isEmpty()) {
            return null;
        }

        /* Add for Gemini by mtk01411 */
        Uri queryPreferApnUri = null;
        if (FeatureOption.MTK_GEMINI_SUPPORT && mPhone.getMySimId() == Phone.GEMINI_SIM_2) {
            queryPreferApnUri = PREFER_TETHERING_APN_SIM2_URI;
        } else {
            queryPreferApnUri = PREFER_TETHERING_APN_URI;
        }

        /* Modify for Gemini by mtk01411 */
        Cursor cursor = mPhone.getContext().getContentResolver().query(
                queryPreferApnUri, new String[] { "_id", "name", "apn" },
                null, null, Telephony.Carriers.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            try{
                ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                canSetPreferTetheringApn = true;
                if (telephony != null) {
                    if(telephony.isTestIccCard()){ 
                        logd("isTestIccCard");
                        canSetPreferTetheringApn = false;
                    }
                }                                
            }catch(Exception e){
                e.printStackTrace();    
            }
        } else {
            canSetPreferTetheringApn = false;
        }

        if (canSetPreferTetheringApn && cursor.getCount() > 0) {
            int pos;
            cursor.moveToFirst();
            pos = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers._ID));
            for(ApnSetting p:mAllApns) {
                if (p.id == pos) {
                    cursor.close();
                    return p;
                }
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return null;
    }
    
    private ArrayList<ApnSetting> fetchDMApn() {
        String operator = mGsmPhone.mIccRecords.getOperatorNumeric();
        /* Add by mtk01411 */
        logd("fetchDMApn():operator=" + operator);
        if (operator != null) {
            String selection = "numeric = '" + operator + "'";
            Cursor dmCursor = null;
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (mPhone.getMySimId() == Phone.GEMINI_SIM_1) {
                    dmCursor = mPhone.getContext().getContentResolver().query(
                                   Telephony.Carriers.CONTENT_URI_DM, null, selection, null, Telephony.Carriers.DEFAULT_SORT_ORDER);
                } else {
                    dmCursor = mPhone.getContext().getContentResolver().query(
                                   Telephony.Carriers.GeminiCarriers.CONTENT_URI_DM, null, selection, null, Telephony.Carriers.DEFAULT_SORT_ORDER);
                }

            } else {
                dmCursor = mPhone.getContext().getContentResolver().query(
                               Telephony.Carriers.CONTENT_URI_DM, null, selection, null, Telephony.Carriers.DEFAULT_SORT_ORDER);
            }

            if (dmCursor != null) {
                try {
                    /* Add by mtk01411 */
                    logd("fetchDMApn(): dmCursor_count=" + Integer.toString(dmCursor.getCount()));
                    if (dmCursor.getCount() > 0) {
                        return createApnList(dmCursor);
                    }
                } finally {
                    if (dmCursor != null) {
                        dmCursor.close();
                    }
                }
            }
        }
        return new ArrayList<ApnSetting>();
    }
    
    protected void logd(String s) {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            Log.d(LOG_TAG, "[GDCT][simId" + mGsmPhone.getMySimId()+ "]"+ s);
        } else {
            Log.d(LOG_TAG, "[GDCT] " + s);
        }
    }

    protected void logi(String s) {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            Log.i(LOG_TAG, "[GDCT][simId" + mGsmPhone.getMySimId()+ "]"+ s);
        } else {
            Log.i(LOG_TAG, "[GDCT] " + s);
        }
    }

    protected void logw(String s) {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            Log.w(LOG_TAG, "[GDCT][simId" + mGsmPhone.getMySimId()+ "]"+ s);
        } else {
            Log.w(LOG_TAG, "[GDCT] " + s);
        }
    }

    protected void loge(String s) {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            Log.e(LOG_TAG, "[GDCT][simId" + mGsmPhone.getMySimId()+ "]"+ s);
        } else {
            Log.e(LOG_TAG, "[GDCT] " + s);
        }
    }

    /* Add by vendor for Multiple PDP Context */
    private boolean isSameProxy(ApnSetting apn1, ApnSetting apn2){
        if (apn1 == null || apn2 == null){
            return false;
        }
        String proxy1;
        if (apn1.canHandleType(Phone.APN_TYPE_MMS)){
            proxy1 = apn1.mmsProxy;
        }else{
            proxy1 = apn1.proxy;
        }
        String proxy2;
        if (apn2.canHandleType(Phone.APN_TYPE_MMS)){
            proxy2 = apn2.mmsProxy;
        }else{
            proxy2 = apn2.proxy;
        }
        /* Fix NULL Pointer Exception problem: proxy1 may be null */ 
        if (proxy1 != null && proxy2 != null && !proxy1.equals("") && !proxy2.equals(""))
            return proxy1.equalsIgnoreCase(proxy2);
        else {
            logd("isSameProxy():proxy1=" + proxy1 + ",proxy2=" + proxy2);
            return false;
        }
    }	

    private boolean enableWaitingApn() {
        boolean ret = false;
        Iterator<String> iterWaitingApn = mWaitingApnList.iterator();

        if (DBG) logd("Reconnect waiting APNs if have.");
        while (iterWaitingApn.hasNext()) {
             enableApnType(iterWaitingApn.next());
             ret = true;
        }
        mWaitingApnList.clear();            
        return ret;
    }

    private void clearWaitingApn() {
        Iterator<String> iterWaitingApn = mWaitingApnList.iterator();

        if (DBG) logd("Reconnect waiting APNs if have.");
        while (iterWaitingApn.hasNext()) {
             mPhone.notifyDataConnection(Phone.REASON_APN_FAILED, iterWaitingApn.next());
        }
        mWaitingApnList.clear();            
    }

    public void gprsDetachResetAPN() {
        // To ensure all context are reset since GPRS detached.
        for (ApnContext apnContext : mApnContexts.values()) {
            if (DBG) logd("Reset APN since GPRS detached [" + apnContext.getApnType() + "]");
            DataConnection dataConnection = apnContext.getDataConnection();
            if (dataConnection != null) {
                State state = apnContext.getState();
                if (state == State.CONNECTED || state == State.CONNECTING) {
                    Message msg = obtainMessage(EVENT_DISCONNECT_DONE, apnContext);
                    dataConnection.tearDown(Phone.REASON_DATA_DETACHED, msg);
                }
            }
            apnContext.setState(State.IDLE);
            apnContext.setApnSetting(null);
            apnContext.setDataConnection(null);
            apnContext.setDataConnectionAc(null);
        }
    }
    //MTK-END [mtk04070][111205][ALPS00093395]MTK proprietary methods/classes/receivers

}
