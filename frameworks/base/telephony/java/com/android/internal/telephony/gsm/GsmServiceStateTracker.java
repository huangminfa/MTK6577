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

import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.DataConnectionTracker;
import com.android.internal.telephony.EventLogTags;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.RestrictedState;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Telephony.Intents;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.util.TimeUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

//MTK-START [mtk03851][111124]MTK added
import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.RIL;
import android.provider.Telephony.SIMInfo;
import android.telephony.TelephonyManager;
//MTK-END [mtk03851][111124]MTK added

/**
 * {@hide}
 */
final class GsmServiceStateTracker extends ServiceStateTracker {
    static final String LOG_TAG = "GSM";
    static final boolean DBG = true;

    GSMPhone phone;
    GsmCellLocation cellLoc;
    GsmCellLocation newCellLoc;
    int mPreferredNetworkType;

    private int gprsState = ServiceState.STATE_OUT_OF_SERVICE;
    private int newGPRSState = ServiceState.STATE_OUT_OF_SERVICE;
    private int mMaxDataCalls = 1;
    private int mNewMaxDataCalls = 1;
    private int mReasonDataDenied = -1;
    private int mNewReasonDataDenied = -1;

    protected static final int EVENT_SET_GPRS_CONN_TYPE_DONE = 51;
    protected static final int EVENT_SET_GPRS_CONN_RETRY = 52;

    private int gprsConnType = 0;
    /**
     * GSM roaming status solely based on TS 27.007 7.2 CREG. Only used by
     * handlePollStateResult to store CREG roaming result.
     */
    private boolean mGsmRoaming = false;

    /**
     * Data roaming status solely based on TS 27.007 10.1.19 CGREG. Only used by
     * handlePollStateResult to store CGREG roaming result.
     */
    private boolean mDataRoaming = false;

    /**
     * Mark when service state is in emergency call only mode
     */
    private boolean mEmergencyOnly = false;

    /**
     * Sometimes we get the NITZ time before we know what country we
     * are in. Keep the time zone information from the NITZ string so
     * we can fix the time zone once know the country.
     */
    private boolean mNeedFixZone = false;
    private int mZoneOffset;
    private boolean mZoneDst;
    private long mZoneTime;
    private boolean mGotCountryCode = false;
    private ContentResolver cr;

    String mSavedTimeZone;
    long mSavedTime;
    long mSavedAtTime;

    /**
     * We can't register for SIM_RECORDS_LOADED immediately because the
     * SIMRecords object may not be instantiated yet.
     */
    private boolean mNeedToRegForSimLoaded;

    /** Started the recheck process after finding gprs should registered but not. */
    private boolean mStartedGprsRegCheck = false;

    /** Already sent the event-log for no gprs register. */
    private boolean mReportedGprsNoReg = false;

    /**
     * The Notification object given to the NotificationManager.
     */
    private Notification mNotification;

    /** Wake lock used while setting time of day. */
    private PowerManager.WakeLock mWakeLock;
    private static final String WAKELOCK_TAG = "ServiceStateTracker";

    /** Keep track of SPN display rules, so we only broadcast intent if something changes. */
    private String curSpn = null;
    private String curPlmn = null;
    private int curSpnRule = 0;

    /** waiting period before recheck gprs and voice registration. */
    static final int DEFAULT_GPRS_CHECK_PERIOD_MILLIS = 60 * 1000;

    /* mtk01616 ALPS00236452: manufacturer maintained table for specific operator with multiple PLMN id */
    private String[][] customEhplmn = {{"46000","46002","46007"},
                                       {"45400","45402","45418"},
                                       {"45403","45404"},
    	                               {"45416","45419"},
                                       {"45501","45504"},
                                       {"45503","45505"},
                                       {"45002","45008"},
                                       {"52501","52502"},
                                       {"43602","43612"},                                                                                                                                                            
                                       {"52010","52099"},                                      
                                       {"26207","26208"},
                                       {"23430","23431","23432"},
                                       {"72402","72403","72404"},
                                       {"72406","72410","72411","72423"},
                                       {"72432","72433","72434"},
                                       {"31026","31031","310160","310200","310210","310220","310230","310240","310250","310260","310270","310660"},
                                       {"310150","310170","310380","310410"}};
	
    /** Notification type. */
    static final int PS_ENABLED = 1001;            // Access Control blocks data service
    static final int PS_DISABLED = 1002;           // Access Control enables data service
    static final int CS_ENABLED = 1003;            // Access Control blocks all voice/sms service
    static final int CS_DISABLED = 1004;           // Access Control enables all voice/sms service
    static final int CS_NORMAL_ENABLED = 1005;     // Access Control blocks normal voice/sms service
    static final int CS_EMERGENCY_ENABLED = 1006;  // Access Control blocks emergency call service

    /** Notification id. */
    static final int PS_NOTIFICATION = 888;  // Id to update and cancel PS restricted
    static final int CS_NOTIFICATION = 999;  // Id to update and cancel CS restricted

//MTK-START [mtk03851][111124]MTK added
    protected static final int EVENT_SET_AUTO_SELECT_NETWORK_DONE = 50;
    /** Indicate the first radio state changed **/
    private boolean mFirstRadioChange = true;
    private boolean mIs3GTo2G = false;

    /** Auto attach PS service when SIM Ready **/
    private int mAutoGprsAttach = 1;
    private int mSimId;
    /**
     *  Values correspond to ServiceStateTracker.DATA_ACCESS_ definitions.
     */
    private int ps_networkType = 0;
    private int newps_networkType = 0;
    private int DEFAULT_GPRS_RETRY_PERIOD_MILLIS = 30 * 1000;

    
    private String mLastRegisteredPLMN = null;
    private String mLastPSRegisteredPLMN = null;

    private boolean mEverIVSR = false;	/* ALPS00324111: at least one chance to do IVSR  */

    private RegistrantList ratPsChangedRegistrants = new RegistrantList();
    private RegistrantList ratCsChangedRegistrants = new RegistrantList();

    /** Notification id. */
    static final int PS_NOTIFICATION_2 = 8882;  // Id to update and cancel PS restricted
    static final int CS_NOTIFICATION_2 = 9992;  // Id to update and cancel CS restricted
    private String mOptr = SystemProperties.get("ro.operator.optr");
//MTK-END [mtk03851][111124]MTK added
    
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            log("BroadcastReceiver: " + intent.getAction());
            if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                // update emergency string whenever locale changed
                updateSpnDisplay();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            	log("ACTION_SCREEN_ON");
                pollState();
            } else if (intent.getAction().equals("com.mtk.TEST_TRM")){ //ALPS00242220
                int mode = intent.getIntExtra("mode", 2); 
                int slot = intent.getIntExtra("slot", 0);//RFU

                log("TEST_TRM mode"+mode+" slot="+slot);
				
                if((mode ==2)&&(mSimId == Phone.GEMINI_SIM_1))
                    phone.setTRM(2,null);
            }
        }
    };

    private ContentObserver mAutoTimeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.i("GsmServiceStateTracker", "Auto time state changed");
            revertToNitzTime();
        }
    };

    private ContentObserver mAutoTimeZoneObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.i("GsmServiceStateTracker", "Auto time zone state changed");
            revertToNitzTimeZone();
        }
    };

    public GsmServiceStateTracker(GSMPhone phone) {
        super();

        this.phone = phone;

//MTK-START [mtk03851][111124]MTK added
        mSimId = phone.getMySimId();
//MTK-START [mtk03851][111124]MTK added

        cm = phone.mCM;
        ss = new ServiceState(mSimId);
        newSS = new ServiceState(mSimId);
        cellLoc = new GsmCellLocation();
        newCellLoc = new GsmCellLocation();
        mSignalStrength = new SignalStrength(mSimId);

        PowerManager powerManager =
                (PowerManager)phone.getContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);

        cm.registerForAvailable(this, EVENT_RADIO_AVAILABLE, null);
        cm.registerForRadioStateChanged(this, EVENT_RADIO_STATE_CHANGED, null);

        cm.registerForVoiceNetworkStateChanged(this, EVENT_NETWORK_STATE_CHANGED, null);
        cm.registerForPsNetworkStateChanged(this, EVENT_PS_NETWORK_STATE_CHANGED, null);
        cm.setOnNITZTime(this, EVENT_NITZ_TIME, null);
        cm.setOnSignalStrengthUpdate(this, EVENT_SIGNAL_STRENGTH_UPDATE, null);
        cm.setOnRestrictedStateChanged(this, EVENT_RESTRICTED_STATE_CHANGED, null);
        cm.registerForSIMReady(this, EVENT_SIM_READY, null);
        cm.setGprsDetach(this, EVENT_DATA_CONNECTION_DETACHED, null);
        cm.setInvalidSimInfo(this, EVENT_INVALID_SIM_INFO, null);//ALPS00248788

        // system setting property AIRPLANE_MODE_ON is set in Settings.
        int airplaneMode = Settings.System.getInt(
                phone.getContext().getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0);
        mDesiredPowerState = ! (airplaneMode > 0);

        cr = phone.getContext().getContentResolver();
        cr.registerContentObserver(
                Settings.System.getUriFor(Settings.System.AUTO_TIME), true,
                mAutoTimeObserver);
        cr.registerContentObserver(
                Settings.System.getUriFor(Settings.System.AUTO_TIME_ZONE), true,
                mAutoTimeZoneObserver);

        setSignalStrengthDefaultValues();
        mNeedToRegForSimLoaded = true;

        // Monitor locale change
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction("com.mtk.TEST_TRM");//ALPS00242220
		
        phone.getContext().registerReceiver(mIntentReceiver, filter);
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mAutoGprsAttach = 0;
        }
        // Gsm doesn't support OTASP so its not needed
        phone.notifyOtaspChanged(OTASP_NOT_NEEDED);

        SystemProperties.set(TelephonyProperties.PROPERTY_ROAMING_INDICATOR_NEEDED, "false");
        SystemProperties.set(TelephonyProperties.PROPERTY_ROAMING_INDICATOR_NEEDED_2, "false");
    }

    public void dispose() {
        // Unregister for all events.
        cm.unregisterForAvailable(this);
        cm.unregisterForRadioStateChanged(this);
        cm.unregisterForVoiceNetworkStateChanged(this);
        cm.unregisterForPsNetworkStateChanged(this);
        cm.unregisterForSIMReady(this);

        phone.mIccRecords.unregisterForRecordsLoaded(this);
        cm.unSetOnSignalStrengthUpdate(this);
        cm.unSetOnRestrictedStateChanged(this);
        cm.unSetOnNITZTime(this);
        cr.unregisterContentObserver(this.mAutoTimeObserver);
        cr.unregisterContentObserver(this.mAutoTimeZoneObserver);
        phone.getContext().unregisterReceiver(mIntentReceiver);
        removeCallbacksAndMessages(null);
    }

    protected void finalize() {
        if(DBG) log("finalize");
    }

    @Override
    protected Phone getPhone() {
        return phone;
    }

    public void handleMessage (Message msg) {
        AsyncResult ar;
        int[] ints;
        String[] strings;
        Message message;
        int testMode = 0, attachType = 0;

        switch (msg.what) {
            case EVENT_GET_SIM_RECOVERY_ON:
                break;
            case EVENT_SET_SIM_RECOVERY_ON:
                break;
            case EVENT_RADIO_AVAILABLE:
                //this is unnecessary
                //setPowerStateToDesired();
                break;

            case EVENT_SIM_READY:
                // Set the network type, in case the radio does not restore it.
                cm.setCurrentPreferredNetworkType();

                //ALPS00279048 START
                long cro_setting = Settings.System.getLong(phone.getContext().getContentResolver(),
		                                        Settings.System.CRO_SETTING,Settings.System.CRO_SETTING_DISABLE);
                log("set CRO setting="+(int)cro_setting);        		
                phone.setCRO((int)cro_setting,null);
                //ALPS00279048 END

                // ALPS00310187 START 
                long hoo_setting = Settings.System.getLong(phone.getContext().getContentResolver(),
		                                        Settings.System.HOO_SETTING,Settings.System.HOO_SETTING_DISABLE);
                log("set HOO setting="+(int)hoo_setting);   
                if(hoo_setting == 0)					
                    phone.setCRO(2,null);
                else if(hoo_setting == 1)					
                    phone.setCRO(3,null);
                // ALPS00310187 END

                // The SIM is now ready i.e if it was locked
                // it has been unlocked. At this stage, the radio is already
                // powered on.
                if (mNeedToRegForSimLoaded) {
                    phone.mIccRecords.registerForRecordsLoaded(this,
                            EVENT_SIM_RECORDS_LOADED, null);
                    mNeedToRegForSimLoaded = false;
                }

                // restore the previous network selection.
                // [ALPS00224837], do not restore network selection, modem will decide selection mode
                //phone.restoreSavedNetworkSelection(null);

                // Set GPRS transfer type: 0:data prefer, 1:call prefer
                int transferType = Settings.System.getInt(phone.getContext().getContentResolver(), 
                                                                                Settings.System.GPRS_TRANSFER_SETTING, 
                                                                                Settings.System.GPRS_TRANSFER_SETTING_DEFAULT);
                cm.setGprsTransferType(transferType, null);
                log("transferType:" + transferType);

                // In non-Gemini project, always set GPRS connection type to ALWAYS
                if(mSimId == Phone.GEMINI_SIM_1){
                    testMode = SystemProperties.getInt("gsm.gcf.testmode", 0);
                }else{
                    testMode = SystemProperties.getInt("gsm.gcf.testmode2", 0);
                }
                
                //Check UE is set to test mode or not
                log("testMode:" + testMode);
                if(testMode == 0){
                    if (mAutoGprsAttach == 1) {
                        attachType = SystemProperties.getInt("persist.radio.gprs.attach.type", 1);
                        log("attachType:" + attachType);
                        ////if(attachType == 1){
                            /* ALPS00300484 : Remove set gprs connection type here. it's too late */			
                            ////  setGprsConnType(1); 
                        ////}
                    } else if (mAutoGprsAttach == 2) {
                        if (FeatureOption.MTK_GEMINI_SUPPORT) {
                            //Disable for Gemini Enhancment by MTK03594
                            if(!FeatureOption.MTK_GEMINI_ENHANCEMENT){
                                Intent intent = new Intent(Intents.ACTION_GPRS_CONNECTION_TYPE_SELECT);
                                intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);
                                phone.getContext().sendStickyBroadcast(intent);
                                log("Broadcast: ACTION_GPRS_CONNECTION_TYPE_SELECT");
                            }                        
                            mAutoGprsAttach = 0;
                        }
                    }
                }
                pollState();
                // Signal strength polling stops when radio is off
                queueNextSignalStrengthPoll();

                //phone.getSimRecoveryOn(obtainMessage(EVENT_GET_SIM_RECOVERY_ON));
                break;

            case EVENT_RADIO_STATE_CHANGED:
                // This will do nothing in the radio not
                // available case
                setPowerStateToDesired();
                pollState();
                break;

            case EVENT_NETWORK_STATE_CHANGED:
                pollState();
                break;
                
            case EVENT_PS_NETWORK_STATE_CHANGED:
                mIs3GTo2G = false;
                pollState();
                break;

            case EVENT_GET_SIGNAL_STRENGTH:
                // This callback is called when signal strength is polled
                // all by itself

                if (!(cm.getRadioState().isOn()) || (cm.getRadioState().isCdma())) {
                    // Polling will continue when radio turns back on and not CDMA
                    return;
                }
                ar = (AsyncResult) msg.obj;
                onSignalStrengthResult(ar);
                queueNextSignalStrengthPoll();

                break;

            case EVENT_GET_LOC_DONE:
                ar = (AsyncResult) msg.obj;

                if (ar.exception == null) {
                    String states[] = (String[])ar.result;
                    int lac = -1;
                    int cid = -1;
                    if (states.length >= 3) {
                        try {
                            if (states[1] != null && states[1].length() > 0) {
                                lac = Integer.parseInt(states[1], 16);
                            }
                            if (states[2] != null && states[2].length() > 0) {
                                cid = Integer.parseInt(states[2], 16);
                            }
                        } catch (NumberFormatException ex) {
                            Log.w(LOG_TAG, "error parsing location: " + ex);
                        }
                        /* ALPS00314520: ignore unknown lac or cid value */						
                        if(lac==0xfffe || cid==0x0fffffff)
                        {
                            log("EVENT_GET_LOC_DONE : unknown lac:"+lac+"or cid:"+cid);
                        }
                        else
                        {                        
                            cellLoc.setLacAndCid(lac, cid);
                        }                            
                        phone.notifyLocationChanged();
                    }
                }

                // Release any temporary cell lock, which could have been
                // acquired to allow a single-shot location update.
                disableSingleLocationUpdate();
                break;

            case EVENT_POLL_STATE_REGISTRATION:
            case EVENT_POLL_STATE_GPRS:
            case EVENT_POLL_STATE_OPERATOR:
            case EVENT_POLL_STATE_NETWORK_SELECTION_MODE:
                ar = (AsyncResult) msg.obj;

                handlePollStateResult(msg.what, ar);
                break;

            case EVENT_POLL_SIGNAL_STRENGTH:
                // Just poll signal strength...not part of pollState()

                cm.getSignalStrength(obtainMessage(EVENT_GET_SIGNAL_STRENGTH));
                break;

            case EVENT_NITZ_TIME:
                ar = (AsyncResult) msg.obj;

                String nitzString = (String)((Object[])ar.result)[0];
                long nitzReceiveTime = ((Long)((Object[])ar.result)[1]).longValue();

                setTimeFromNITZString(nitzString, nitzReceiveTime);
                break;

            case EVENT_SIGNAL_STRENGTH_UPDATE:
                // This is a notification from
                // CommandsInterface.setOnSignalStrengthUpdate

                ar = (AsyncResult) msg.obj;
                int rssi = onSignalStrengthResult(ar);

                // [ALPS00127981]
                // If rssi=99, poll again
                if (rssi == 99) {
                    if (dontPollSignalStrength == true) {
                        dontPollSignalStrength = false;
                        queueNextSignalStrengthPoll();
                    }
                } else {
                dontPollSignalStrength = true;
                }
                break;

            case EVENT_SIM_RECORDS_LOADED:
                // ALPS00296353 MVNO
                if(FeatureOption.MTK_MVNO_SUPPORT) {
                    log("MTK_MVNO_SUPPORT refreshSpnDisplay()");
                    // pollState() result may be faster than load EF complete, so update ss.alphaLongShortName
                    refreshSpnDisplay();
                } else
                    updateSpnDisplay();

                String newImsi = phone.getSubscriberId();
                boolean bImsiChanged = false;	 
                if (mSimId == Phone.GEMINI_SIM_1) { 
                    String oldImsi = Settings.System.getString(phone.getContext().getContentResolver(), "gsm.sim.imsi");
                    if(oldImsi == null || !oldImsi.equals(newImsi)) {	  
                        Log.d(LOG_TAG, "GSST: Sim1 Card changed  lastImsi is " + oldImsi + " newImsi is " + newImsi); 
                        bImsiChanged = true;
                        Settings.System.putString(phone.getContext().getContentResolver(), "gsm.sim.imsi", newImsi);
                    }
                } else {
                    String oldImsi2 = Settings.System.getString(phone.getContext().getContentResolver(), "gsm.sim.imsi.2");
                    if(oldImsi2 == null || !oldImsi2.equals(newImsi)) {	 
                        Log.d(LOG_TAG, "GSST: Sim2 Card changed  lastImsi is "+ oldImsi2 + " newImsi is " + newImsi); 
                        bImsiChanged = true;
                        Settings.System.putString(phone.getContext().getContentResolver(), "gsm.sim.imsi.2", newImsi);	
                    }
                } 
		        // if(bImsiChanged && (ss.getState() != ServiceState.STATE_IN_SERVICE) 	&& ss.getIsManualSelection()) {
                if(bImsiChanged && ss.getIsManualSelection()) {
                    Log.d(LOG_TAG, "GSST: service state is out of service with manual network selection mode,  setNetworkSelectionModeAutomatic " ); 
                    phone.setNetworkSelectionModeAutomatic(obtainMessage(EVENT_SET_AUTO_SELECT_NETWORK_DONE));
                }		  
                break;

            case EVENT_LOCATION_UPDATES_ENABLED:
                ar = (AsyncResult) msg.obj;

                if (ar.exception == null) {
                    cm.getVoiceRegistrationState(obtainMessage(EVENT_GET_LOC_DONE, null));
                }
                break;

            case EVENT_SET_PREFERRED_NETWORK_TYPE:
                ar = (AsyncResult) msg.obj;
                // Don't care the result, only use for dereg network (COPS=2)
                message = obtainMessage(EVENT_RESET_PREFERRED_NETWORK_TYPE, ar.userObj);
                cm.setPreferredNetworkType(mPreferredNetworkType, message);
                break;

            case EVENT_RESET_PREFERRED_NETWORK_TYPE:
                ar = (AsyncResult) msg.obj;
                if (ar.userObj != null) {
                    AsyncResult.forMessage(((Message) ar.userObj)).exception
                            = ar.exception;
                    ((Message) ar.userObj).sendToTarget();
                }
                break;

            case EVENT_GET_PREFERRED_NETWORK_TYPE:
                ar = (AsyncResult) msg.obj;

                if (ar.exception == null) {
                    mPreferredNetworkType = ((int[])ar.result)[0];
                } else {
                    mPreferredNetworkType = RILConstants.NETWORK_MODE_GLOBAL;
                }

                message = obtainMessage(EVENT_SET_PREFERRED_NETWORK_TYPE, ar.userObj);
                int toggledNetworkType = RILConstants.NETWORK_MODE_GLOBAL;

                cm.setPreferredNetworkType(toggledNetworkType, message);
                break;

            case EVENT_CHECK_REPORT_GPRS:
                if (ss != null && !isGprsConsistent(gprsState, ss.getState())) {

                    // Can't register data service while voice service is ok
                    // i.e. CREG is ok while CGREG is not
                    // possible a network or baseband side error
                    GsmCellLocation loc = ((GsmCellLocation)phone.getCellLocation());
                    EventLog.writeEvent(EventLogTags.DATA_NETWORK_REGISTRATION_FAIL,
                            ss.getOperatorNumeric(), loc != null ? loc.getCid() : -1);
                    mReportedGprsNoReg = true;
                }
                mStartedGprsRegCheck = false;
                break;

            case EVENT_RESTRICTED_STATE_CHANGED:
                // This is a notification from
                // CommandsInterface.setOnRestrictedStateChanged

                if (DBG) log("EVENT_RESTRICTED_STATE_CHANGED");

                ar = (AsyncResult) msg.obj;

                onRestrictedStateChanged(ar);
                break;
            case EVENT_SET_AUTO_SELECT_NETWORK_DONE:
                log("GSST EVENT_SET_AUTO_SELECT_NETWORK_DONE");
                break;
            case EVENT_SET_GPRS_CONN_TYPE_DONE:
                Log.d(LOG_TAG, "GSST EVENT_SET_GPRS_CONN_TYPE_DONE");
                ar = (AsyncResult) msg.obj;
                if(ar.exception != null){
                   sendMessageDelayed(obtainMessage(EVENT_SET_GPRS_CONN_RETRY, null), DEFAULT_GPRS_RETRY_PERIOD_MILLIS);
                }
                break;
            case EVENT_SET_GPRS_CONN_RETRY:
                Log.d(LOG_TAG, "EVENT_SET_GPRS_CONN_RETRY");
                ServiceState ss = phone.getServiceState();
                if (ss == null) break;
                Log.d(LOG_TAG, "GSST EVENT_SET_GPRS_CONN_RETRY ServiceState " + ss.getState());
                if (ss.getState() == ServiceState.STATE_POWER_OFF) {
                    break;
                }
                int airplanMode = Settings.System.getInt(phone.getContext().getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
                Log.d(LOG_TAG, "GSST EVENT_SET_GPRS_CONN_RETRY airplanMode " + airplanMode);
                if (airplanMode > 0) {
                    break;
                }
                setGprsConnType(gprsConnType);
                break;
            case EVENT_DATA_CONNECTION_DETACHED:
                Log.d(LOG_TAG, "EVENT_DATA_CONNECTION_DETACHED: set gprsState=STATE_OUT_OF_SERVICE");
                gprsState = ServiceState.STATE_OUT_OF_SERVICE;
                ps_networkType = DATA_ACCESS_UNKNOWN;
                if (mSimId == Phone.GEMINI_SIM_1) {
                    phone.setSystemProperty(TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE, networkTypeToString(ps_networkType));
                } else {
                    phone.setSystemProperty(TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE_2, networkTypeToString(ps_networkType));
                }
                mDetachedRegistrants.notifyRegistrants();
                break;
            case EVENT_INVALID_SIM_INFO: //ALPS00248788
                if (DBG) log("EVENT_INVALID_SIM_INFO");
                ar = (AsyncResult) msg.obj;
                onInvalidSimInfoReceived(ar);
                break;				
            default:
                super.handleMessage(msg);
            break;
        }
    }

    protected void setPowerStateToDesired() {
        log("setPowerStateToDesired mDesiredPowerState:" + mDesiredPowerState +
                " current radio state:" + cm.getRadioState() +
    			" mFirstRadioChange:" + mFirstRadioChange);
        // If we want it on and it's off, turn it on
        if (mDesiredPowerState
            && cm.getRadioState() == CommandsInterface.RadioState.RADIO_OFF) {
            if (!FeatureOption.MTK_GEMINI_SUPPORT) {
                setGprsConnType(2);
                cm.setRadioPower(true, null);
            }
        } else if (!mDesiredPowerState && cm.getRadioState().isOn()) {
            // If it's on and available and we want it off gracefully
            DataConnectionTracker dcTracker = phone.mDataConnectionTracker;
            powerOffRadioSafely(dcTracker);
        } else if (!mDesiredPowerState && !cm.getRadioState().isOn() && mFirstRadioChange) { //mtk added
        	// For boot up in Airplane mode, we would like to startup modem in cfun_state=4
            if (!FeatureOption.MTK_GEMINI_SUPPORT) {  
                cm.setRadioPower(false, null);
            }            
        }// Otherwise, we're in the desired state

        if (mFirstRadioChange) {
            if (cm.getRadioState() == CommandsInterface.RadioState.RADIO_UNAVAILABLE) {
                log("First radio changed but radio unavailable, not to set first radio change off");
            } else {
                log("First radio changed and radio available, set first radio change off");
                mFirstRadioChange = false;
            }
        }
    }

    @Override
    protected void hangupAndPowerOff() {
        // hang up all active voice calls
        if (phone.isInCall()) {
            log("Hangup call ...");
            phone.mCT.ringingCall.hangupIfAlive();
            phone.mCT.backgroundCall.hangupIfAlive();
            phone.mCT.foregroundCall.hangupIfAlive();
        }

        if (!FeatureOption.MTK_GEMINI_SUPPORT) {  
            cm.setRadioPower(false, null);
        }
    }

    /**
     * Handle the result of one of the pollState()-related requests
     */
        protected void handlePollStateResult (int what, AsyncResult ar) {
        int ints[];
        String states[];

        // Ignore stale requests from last poll
        if (ar.userObj != pollingContext) return;

        if (ar.exception != null) {
            CommandException.Error err=null;

            if (ar.exception instanceof CommandException) {
                err = ((CommandException)(ar.exception)).getCommandError();
            }

            if (err == CommandException.Error.RADIO_NOT_AVAILABLE) {
                // Radio has crashed or turned off
                cancelPollState();
                return;
            }

            if (!cm.getRadioState().isOn()) {
                // Radio has crashed or turned off
                cancelPollState();
                return;
            }

            if (err != CommandException.Error.OP_NOT_ALLOWED_BEFORE_REG_NW &&
                    err != CommandException.Error.OP_NOT_ALLOWED_BEFORE_REG_NW) {
                log("RIL implementation has returned an error where it must succeed" + ar.exception);
            }
        } else try {
            switch (what) {
                case EVENT_POLL_STATE_REGISTRATION:
                    states = (String[])ar.result;
                    int lac = -1;
                    int cid = -1;
                    int regState = -1;
                    int reasonRegStateDenied = -1;
                    int psc = -1;
                    if (states.length > 0) {
                        try {
                            regState = Integer.parseInt(states[0]);
                            if (states.length > 3) {
                                if (states[1] != null && states[1].length() > 0) {
                                    lac = Integer.parseInt(states[1], 16);
                                }
                                if (states[2] != null && states[2].length() > 0) {
                                    cid = Integer.parseInt(states[2], 16);
                                }
                                if (states[3] != null && states[3].length() > 0) {                                    
                                    mNewRadioTechnology = Integer.parseInt(states[3]);
                                    newSS.setRadioTechnology(mNewRadioTechnology);
                                }
                            }
                            log("EVENT_POLL_STATE_REGISTRATION cs_networkTyp:" + mRadioTechnology +
                                    ",regState:" + regState + 
                                    ",mNewRadioTechnology:" + mNewRadioTechnology +
                                    ",lac:" + lac + 
                                    ",cid:" + cid); 
                        } catch (NumberFormatException ex) {
                            loge("error parsing RegistrationState: " + ex);
                        }
                    }

                    mGsmRoaming = regCodeIsRoaming(regState);
                    newSS.setState (regCodeToServiceState(regState));
                    newSS.setRegState(regState);
                    // [ALPS00225065] For Gemini special handle,
                    // When SIM blocked, treat as out of service
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        if (cm.getRadioState() == CommandsInterface.RadioState.SIM_LOCKED_OR_ABSENT) {
                            newSS.setState(ServiceState.STATE_OUT_OF_SERVICE);
                        }
                    }

                    if (regState == 10 || regState == 12 || regState == 13 || regState == 14) {
                        mEmergencyOnly = true;
                    } else {
                        mEmergencyOnly = false;
                    }

                    // LAC and CID are -1 if not avail. LAC and CID are -1 in OUT_SERVICE
                    if (states.length > 3 || (regState != 1 && regState != 5)) {
                    	log("states.length > 3");

                        /* ALPS00291583: ignore unknown lac or cid value */						
                        if(lac==0xfffe || cid==0x0fffffff)
                        {
                            log("unknown lac:"+lac+"or cid:"+cid);
                        }
                        else
                        {
                            newCellLoc.setLacAndCid(lac, cid);
                        }						   
                    	//if (mSimId == Phone.GEMINI_SIM_1) {
                    	//	SystemProperties.set(TelephonyProperties.PROPERTY_CS_NETWORK_TYPE, Integer.toString(mNewRadioTechnology));
                    	//	log("PROPERTY_CS_NETWORK_TYPE" + SystemProperties.get(TelephonyProperties.PROPERTY_CS_NETWORK_TYPE));
                    	//} else {
                     //	SystemProperties.set(TelephonyProperties.PROPERTY_CS_NETWORK_TYPE_2, Integer.toString(mNewRadioTechnology));
                    	//	log("PROPERTY_CS_NETWORK_TYPE_2" + SystemProperties.get(TelephonyProperties.PROPERTY_CS_NETWORK_TYPE_2));
                    	//}
                    }
                    newCellLoc.setPsc(psc);
                break;

                case EVENT_POLL_STATE_GPRS:
                    states = (String[])ar.result;

                    regState = -1;
                    mNewReasonDataDenied = -1;
                    mNewMaxDataCalls = 1;
                    if (states.length > 0) {
                        try {
                            regState = Integer.parseInt(states[0]);

                            // states[3] (if present) is the current radio technology
                            if (states.length >= 4 && states[3] != null) {
                                newps_networkType = Integer.parseInt(states[3]);
                            }
                            if ((states.length >= 5 ) && (regState == 3)) {
                                mNewReasonDataDenied = Integer.parseInt(states[4]);
                            }
                            if (states.length >= 6) {
                                mNewMaxDataCalls = Integer.parseInt(states[5]);
                            }
                        } catch (NumberFormatException ex) {
                            loge("error parsing GprsRegistrationState: " + ex);
                        }
                    }
                    newGPRSState = regCodeToServiceState(regState);
                    mDataRoaming = regCodeIsRoaming(regState);
                break;

                case EVENT_POLL_STATE_OPERATOR:
                    String opNames[] = (String[])ar.result;

                    if (opNames != null && opNames.length >= 3) {
                        log("long:" +opNames[0] + " short:" + opNames[1] + " numeric:" + opNames[2]);                        
                        newSS.setOperatorName (opNames[0], opNames[1], opNames[2]);
                    }
                break;

                case EVENT_POLL_STATE_NETWORK_SELECTION_MODE:
                    ints = (int[])ar.result;
                    newSS.setIsManualSelection(ints[0] == 1);
                break;
            }

        } catch (RuntimeException ex) {
            Log.e(LOG_TAG, "Exception while polling service state. "
                            + "Probably malformed RIL response.", ex);
        }

        pollingContext[0]--;

        if (pollingContext[0] == 0) {
            /**
             * [ALPS00006527]
             * Only when CS in service, treat PS as in service
             */            
            if (newSS.getState() != ServiceState.STATE_IN_SERVICE) {
                newGPRSState = regCodeToServiceState(0);
                mDataRoaming = regCodeIsRoaming(0);
            }
            
            /**
             *  Since the roaming states of gsm service (from +CREG) and
             *  data service (from +CGREG) could be different, the new SS
             *  is set roaming while either one is roaming.
             *
             *  There is an exception for the above rule. The new SS is not set
             *  as roaming while gsm service reports roaming but indeed it is
             *  not roaming between operators.
             */
            //BEGIN mtk03923[20120206][ALPS00117799][ALPS00230295]
            //Only check roaming indication from CREG (CS domain)
            //boolean roaming = (mGsmRoaming || mDataRoaming);
            boolean roaming = mGsmRoaming;
            //END   mtk03923[20120206][ALPS00117799][ALPS00230295]
            // [ALPS00220720] remove this particular check.
            // Still display roaming even in the same operator 
            /*
            if (mGsmRoaming && !isRoamingBetweenOperators(mGsmRoaming, newSS)) {
                roaming = false;
            }
            */
            newSS.setRoaming(roaming);
            newSS.setEmergencyOnly(mEmergencyOnly);
            pollStateDone();
        }
    }

    private void setSignalStrengthDefaultValues() {
        mSignalStrength = new SignalStrength(mSimId, 99, -1, -1, -1, -1, -1, -1, -1, -1, -1, SignalStrength.INVALID_SNR, -1, true);
    }

    /**
     * A complete "service state" from our perspective is
     * composed of a handful of separate requests to the radio.
     *
     * We make all of these requests at once, but then abandon them
     * and start over again if the radio notifies us that some
     * event has changed
     */
    private void pollState() {
        pollingContext = new int[1];
        pollingContext[0] = 0;
        log("cm.getRadioState() is " + cm.getRadioState());

        switch (cm.getRadioState()) {
            case RADIO_UNAVAILABLE:
                newSS.setStateOutOfService();
                newCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                mGotCountryCode = false;
                pollStateDone();
            break;

            case RADIO_OFF:
                newSS.setStateOff();
                newCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                mGotCountryCode = false;
                pollStateDone();
            break;

            case RUIM_NOT_READY:
            case RUIM_READY:
            case RUIM_LOCKED_OR_ABSENT:
            case NV_NOT_READY:
            case NV_READY:
                if (DBG) log("Radio Technology Change ongoing, setting SS to off");
                newSS.setStateOff();
                newCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                mGotCountryCode = false;

                //NOTE: pollStateDone() is not needed in this case
                break;

            default:
                // Issue all poll-related commands at once
                // then count down the responses, which
                // are allowed to arrive out-of-order

                pollingContext[0]++;
                cm.getOperator(
                    obtainMessage(
                        EVENT_POLL_STATE_OPERATOR, pollingContext));

                pollingContext[0]++;
                cm.getDataRegistrationState(
                    obtainMessage(
                        EVENT_POLL_STATE_GPRS, pollingContext));

                pollingContext[0]++;
                cm.getVoiceRegistrationState(
                    obtainMessage(
                        EVENT_POLL_STATE_REGISTRATION, pollingContext));

                pollingContext[0]++;
                cm.getNetworkSelectionMode(
                    obtainMessage(
                        EVENT_POLL_STATE_NETWORK_SELECTION_MODE, pollingContext));
            break;
        }
    }

    private static String networkTypeToString(int type) {
        //Network Type from GPRS_REGISTRATION_STATE
        String ret = "unknown";

        switch (type) {
            case DATA_ACCESS_GPRS:
                ret = "GPRS";
                break;
            case DATA_ACCESS_EDGE:
                ret = "EDGE";
                break;
            case DATA_ACCESS_UMTS:
                ret = "UMTS";
                break;
            case DATA_ACCESS_HSDPA:
                ret = "HSDPA";
                break;
            case DATA_ACCESS_HSUPA:
                ret = "HSUPA";
                break;
            case DATA_ACCESS_HSPA:
                ret = "HSPA";
                break;
            default:
                break;
        }
        Log.e(LOG_TAG, "networkTypeToString: " + ret);
        return ret;
    }

    private void pollStateDone() {
        // PS & CS network type summarize -->
        // From 3G to 2G, CS NW type is ensured responding firstly. Before receiving 
        // PS NW type change URC, PS NW type should always take CS NW type.
        if ((mNewRadioTechnology > ServiceState.RADIO_TECHNOLOGY_UNKNOWN &&
                mNewRadioTechnology <= ServiceState.RADIO_TECHNOLOGY_EDGE) &&
                mRadioTechnology >= ServiceState.RADIO_TECHNOLOGY_UMTS) {
            mIs3GTo2G = true;
            log("pollStateDone(): mIs3GTo2G = true");
        }
        if (mIs3GTo2G == true) {
            newps_networkType = mNewRadioTechnology;
        } else if (newps_networkType > mNewRadioTechnology) {
            mNewRadioTechnology = newps_networkType;
            newSS.setRadioTechnology(newps_networkType);                    
        }
        // <-- end of  PS & CS network type summarize
        
        if (DBG) {
            log("Poll ServiceState done: " +
                " oldSS=[" + ss + "] newSS=[" + newSS +
                "] oldGprs=" + gprsState + " newData=" + newGPRSState +
                " oldMaxDataCalls=" + mMaxDataCalls +
                " mNewMaxDataCalls=" + mNewMaxDataCalls +
                " oldReasonDataDenied=" + mReasonDataDenied +
                " mNewReasonDataDenied=" + mNewReasonDataDenied +
                " oldType=" + ServiceState.radioTechnologyToString(mRadioTechnology) +
                " newType=" + ServiceState.radioTechnologyToString(mNewRadioTechnology) +
                " oldGprsType=" + ps_networkType +
                " newGprsType=" + newps_networkType);
        }

        boolean hasRegistered =
            ss.getState() != ServiceState.STATE_IN_SERVICE
            && newSS.getState() == ServiceState.STATE_IN_SERVICE;

        boolean hasDeregistered =
            ss.getState() == ServiceState.STATE_IN_SERVICE
            && newSS.getState() != ServiceState.STATE_IN_SERVICE;

        boolean hasGprsAttached =
                gprsState != ServiceState.STATE_IN_SERVICE
                && newGPRSState == ServiceState.STATE_IN_SERVICE;

        boolean hasPSNetworkTypeChanged = ps_networkType != newps_networkType;

        boolean hasRadioTechnologyChanged = mRadioTechnology != mNewRadioTechnology;

        boolean hasChanged = !newSS.equals(ss);

        boolean hasRoamingOn = !ss.getRoaming() && newSS.getRoaming();

        boolean hasRoamingOff = ss.getRoaming() && !newSS.getRoaming();

        boolean hasLocationChanged = !newCellLoc.equals(cellLoc);

        boolean hasRegStateChanged = ss.getRegState() != newSS.getRegState();

        log("pollStateDone,hasRegistered:"+hasRegistered+",hasDeregistered:"+hasDeregistered+
        		",hasGprsAttached:"+hasGprsAttached+
        		",hasPSNetworkTypeChanged:"+hasPSNetworkTypeChanged+",hasRadioTechnologyChanged:"+hasRadioTechnologyChanged+
        		",hasChanged:"+hasChanged+",hasRoamingOn:"+hasRoamingOn+",hasRoamingOff:"+hasRoamingOff+
        		",hasLocationChanged:"+hasLocationChanged+",hasRegStateChanged:"+hasRegStateChanged);
        // Add an event log when connection state changes
        if (ss.getState() != newSS.getState() || gprsState != newGPRSState) {
            EventLog.writeEvent(EventLogTags.GSM_SERVICE_STATE_CHANGE,
                ss.getState(), gprsState, newSS.getState(), newGPRSState);
        }

        ServiceState tss;
        tss = ss;
        ss = newSS;
        newSS = tss;
        // clean slate for next time
        // newSS.setStateOutOfService();

        // ALPS00277176 
        GsmCellLocation tcl = cellLoc;
        cellLoc = newCellLoc;
        newCellLoc = tcl;

        gprsState = newGPRSState;

        //MTK_OP01_PROTECT_START
        if ("OP01".equals(mOptr)) {
            if ((newGPRSState == ServiceState.STATE_IN_SERVICE && gprsState == ServiceState.STATE_OUT_OF_SERVICE) ||
                (ps_networkType >= TelephonyManager.NETWORK_TYPE_UMTS &&
                 newps_networkType > TelephonyManager.NETWORK_TYPE_UNKNOWN &&
                 newps_networkType <= TelephonyManager.NETWORK_TYPE_EDGE) ||
                 (ps_networkType <= TelephonyManager.NETWORK_TYPE_EDGE &&
                 ps_networkType > TelephonyManager.NETWORK_TYPE_UNKNOWN &&
                 newps_networkType >= TelephonyManager.NETWORK_TYPE_UMTS)) {
                //this is a workaround for MM. when 3G->2G or 2G->3G RAU, PS temporary unavailable
                //it will trigger TCP delay retry and make MM is resumed slowly
                //PS status is recovered from unknown to in service
                //we could trigger MM retry mechanism by socket timeout
                Intent intent = new Intent("com.mtk.ACTION_PS_STATE_RESUMED");
                phone.getContext().sendBroadcast(intent);
            }
        }
        //MTK_OP01_PROTECT_END

        ps_networkType = newps_networkType;
        
        if (hasPSNetworkTypeChanged) {
            if (mSimId == Phone.GEMINI_SIM_1) {
                phone.setSystemProperty(TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE, networkTypeToString(ps_networkType));
            } else {
                phone.setSystemProperty(TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE_2, networkTypeToString(ps_networkType));
            }
            ratPsChangedRegistrants.notifyRegistrants(new AsyncResult(null, ps_networkType, null));
        }

        // Add an event log when network type switched
        // TODO: we may add filtering to reduce the event logged,
        // i.e. check preferred network setting, only switch to 2G, etc
        if (hasRadioTechnologyChanged) {
            int cid = -1;
            GsmCellLocation loc = ((GsmCellLocation)phone.getCellLocation());
            if (loc != null) cid = loc.getCid();
            EventLog.writeEvent(EventLogTags.GSM_RAT_SWITCHED, cid, mRadioTechnology,
                    mNewRadioTechnology);
            if (DBG) {
                log("RAT switched " + ServiceState.radioTechnologyToString(mRadioTechnology) +
                        " -> " + ServiceState.radioTechnologyToString(mNewRadioTechnology) +
                        " at cell " + cid);
            }
            if (mSimId == Phone.GEMINI_SIM_1) {            
                SystemProperties.set(TelephonyProperties.PROPERTY_CS_NETWORK_TYPE, Integer.toString(mNewRadioTechnology));
            } else {
                SystemProperties.set(TelephonyProperties.PROPERTY_CS_NETWORK_TYPE_2, Integer.toString(mNewRadioTechnology));
            }
            updateSpnDisplay(false);
            ratCsChangedRegistrants.notifyRegistrants(new AsyncResult(null, mNewRadioTechnology, null));
        }

        gprsState = newGPRSState;
        mReasonDataDenied = mNewReasonDataDenied;
        mMaxDataCalls = mNewMaxDataCalls;
        mRadioTechnology = mNewRadioTechnology;

        //newSS.setStateOutOfService(); // clean slate for next time

        if (hasRegistered) {
            mNetworkAttachedRegistrants.notifyRegistrants();
            mLastRegisteredPLMN = ss.getOperatorNumeric() ;		
            log("mLastRegisteredPLMN= "+mLastRegisteredPLMN);			
        }

        if (hasChanged) {
            updateSpnDisplay();
            String operatorNumeric = ss.getOperatorNumeric();
            if (Phone.GEMINI_SIM_1 == mSimId) {
                phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ALPHA, ss.getOperatorAlphaLong());
                phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_NUMERIC, ss.getOperatorNumeric());
                phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ISROAMING, ss.getRoaming() ? "true" : "false");
            } else {
                phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ALPHA_2, ss.getOperatorAlphaLong());                
                phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_NUMERIC_2, ss.getOperatorNumeric());
                phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ISROAMING_2, ss.getRoaming() ? "true" : "false");
            }

            if (operatorNumeric == null) {
                if (Phone.GEMINI_SIM_1 == mSimId) {
                	phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY, "");
                } else {
                    phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY_2, "");
                }
                mGotCountryCode = false;
            } else {
                String iso = "";
                try{
                    iso = MccTable.countryCodeForMcc(Integer.parseInt(
                            operatorNumeric.substring(0,3)));
                } catch ( NumberFormatException ex){
                    Log.w(LOG_TAG, "countryCodeForMcc error" + ex);
                } catch ( StringIndexOutOfBoundsException ex) {
                    Log.w(LOG_TAG, "countryCodeForMcc error" + ex);
                }
                if (Phone.GEMINI_SIM_1 == mSimId) {
                	phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY, iso);
                } else {
                    phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY_2, iso);
                }
                mGotCountryCode = true;

                log("[NITZ],mNeedFixZone:" + mNeedFixZone);
                if (mNeedFixZone) {
                    TimeZone zone = null;
                    // If the offset is (0, false) and the timezone property
                    // is set, use the timezone property rather than
                    // GMT.
                    String zoneName = SystemProperties.get(TIMEZONE_PROPERTY);
                    log("[NITZ],zoneName:" + zoneName+",mZoneOffset"+mZoneOffset+",mZoneDst"+mZoneDst);
                    if (iso.equals("")){
                        // Country code not found.  This is likely a test network.
                        // Get a TimeZone based only on the NITZ parameters (best guess).
                        zone = getNitzTimeZone(mZoneOffset, mZoneDst, mZoneTime);
                        log("[NITZ],iso is null.zone:"+zone);
                    } else if ((mZoneOffset == 0) && (mZoneDst == false) &&
                        (zoneName != null) && (zoneName.length() > 0) &&
                        (Arrays.binarySearch(GMT_COUNTRY_CODES, iso) < 0)) {
                        zone = TimeZone.getDefault();
                        // For NITZ string without timezone,
                        // need adjust time to reflect default timezone setting
                        long tzOffset;
                        tzOffset = zone.getOffset(System.currentTimeMillis());
                        log("[NITZ],tzOffset:" + tzOffset);
                        if (getAutoTime()) {
                            setAndBroadcastNetworkSetTime(System.currentTimeMillis() - tzOffset);
                        } else {
                            // Adjust the saved NITZ time to account for tzOffset.
                        	log("[NITZ],mSavedTime:"+mSavedTime);
                            mSavedTime = mSavedTime - tzOffset;
                        }
                    } else {
                        zone = TimeUtils.getTimeZone(mZoneOffset,
                            mZoneDst, mZoneTime, iso);
                        log("[NITZ],zone:"+zone);
                    }

                    mNeedFixZone = false;

                    if (zone != null) {
                        if (getAutoTimeZone()) {
                            setAndBroadcastNetworkSetTimeZone(zone.getID());
                        }
                        saveNitzTimeZone(zone.getID());
                    }
                }
            }

            if (hasRegStateChanged) {
            	if (ss.getRegState() == ServiceState.REGISTRATION_STATE_UNKNOWN
                && (1 == Settings.System.getInt(phone.getContext().getContentResolver(), Settings.System.AIRPLANE_MODE_ON, -1))) {
            		int serviceState = phone.getServiceState().getState();
            		if (serviceState != ServiceState.STATE_POWER_OFF) {
            			ss.setStateOff();
            		}
            	}
            	phone.updateSimIndicateState();
            }
            phone.notifyServiceStateChanged(ss);

            if (hasRegistered) {
                /* ALPS00296741: to handle searching state to registered scenario,we force status bar to refresh signal icon */			
                log("force update signal strength after notifyServiceStateChanged");						
                phone.notifySignalStrength();			
            }			
        }

        if (hasGprsAttached) {
            mAttachedRegistrants.notifyRegistrants();
            mLastPSRegisteredPLMN = ss.getOperatorNumeric() ;		
            log("mLastPSRegisteredPLMN= "+mLastPSRegisteredPLMN);			
        }

        if (hasRadioTechnologyChanged || hasPSNetworkTypeChanged) {
            phone.notifyDataConnection(Phone.REASON_NW_TYPE_CHANGED);
        }

        if (hasLocationChanged) {
        	phone.notifyLocationChanged();
        }

        if (hasRoamingOn) {
            Settings.System.putInt(phone.getContext().getContentResolver(), 
                                            Settings.System.ROAMING_INDICATION_NEEDED, 
                                            1);
            if (mSimId == Phone.GEMINI_SIM_1) {            
                SystemProperties.set(TelephonyProperties.PROPERTY_ROAMING_INDICATOR_NEEDED, "true");
            } else {
                SystemProperties.set(TelephonyProperties.PROPERTY_ROAMING_INDICATOR_NEEDED_2, "true");
            } 
            mRoamingOnRegistrants.notifyRegistrants();
        }

        if (hasRoamingOff) {
            Settings.System.putInt(phone.getContext().getContentResolver(), 
                                            Settings.System.ROAMING_INDICATION_NEEDED, 
                                            0);			
            if (mSimId == Phone.GEMINI_SIM_1) {            
                SystemProperties.set(TelephonyProperties.PROPERTY_ROAMING_INDICATOR_NEEDED, "false");
            } else {
                SystemProperties.set(TelephonyProperties.PROPERTY_ROAMING_INDICATOR_NEEDED_2, "false");
            } 
            mRoamingOffRegistrants.notifyRegistrants();
        }

        if (hasLocationChanged) {
            phone.notifyLocationChanged();
        }

        if (! isGprsConsistent(gprsState, ss.getState())) {
            if (!mStartedGprsRegCheck && !mReportedGprsNoReg) {
                mStartedGprsRegCheck = true;

                int check_period = Settings.Secure.getInt(
                        phone.getContext().getContentResolver(),
                        Settings.Secure.GPRS_REGISTER_CHECK_PERIOD_MS,
                        DEFAULT_GPRS_CHECK_PERIOD_MILLIS);
                sendMessageDelayed(obtainMessage(EVENT_CHECK_REPORT_GPRS),
                        check_period);
            }
        } else {
            mReportedGprsNoReg = false;
        }
    }

    /**
     * Check if GPRS got registered while voice is registered.
     *
     * @param gprsState for GPRS registration state, i.e. CGREG in GSM
     * @param serviceState for voice registration state, i.e. CREG in GSM
     * @return false if device only register to voice but not gprs
     */
    private boolean isGprsConsistent(int gprsState, int serviceState) {
        return !((serviceState == ServiceState.STATE_IN_SERVICE) &&
                (gprsState != ServiceState.STATE_IN_SERVICE));
    }

    /**
     * Returns a TimeZone object based only on parameters from the NITZ string.
     */
    private TimeZone getNitzTimeZone(int offset, boolean dst, long when) {
        TimeZone guess = findTimeZone(offset, dst, when);
        if (guess == null) {
            // Couldn't find a proper timezone.  Perhaps the DST data is wrong.
            guess = findTimeZone(offset, !dst, when);
        }
        if (DBG) log("getNitzTimeZone returning " + (guess == null ? guess : guess.getID()));
        return guess;
    }

    private TimeZone findTimeZone(int offset, boolean dst, long when) {
    	log("[NITZ],findTimeZone,offset:"+offset+",dst:"+dst+",when:"+when);
        int rawOffset = offset;
        if (dst) {
            rawOffset -= 3600000;
        }
        String[] zones = TimeZone.getAvailableIDs(rawOffset);
        TimeZone guess = null;
        Date d = new Date(when);
        for (String zone : zones) {
            TimeZone tz = TimeZone.getTimeZone(zone);
            if (tz.getOffset(when) == offset &&
                tz.inDaylightTime(d) == dst) {
                guess = tz;
                log("[NITZ],find time zone.");
                break;
            }
        }

        return guess;
    }

    private void queueNextSignalStrengthPoll() {
        if (dontPollSignalStrength || (cm.getRadioState().isCdma())) {
            // The radio is telling us about signal strength changes
            // we don't have to ask it
            return;
        }

        Message msg;

        msg = obtainMessage();
        msg.what = EVENT_POLL_SIGNAL_STRENGTH;

        long nextTime;

        // TODO Don't poll signal strength if screen is off
        sendMessageDelayed(msg, POLL_PERIOD_MILLIS);
    }

    /**
     *  Send signal-strength-changed notification if changed.
     *  Called both for solicited and unsolicited signal strength updates.
     */
    private int onSignalStrengthResult(AsyncResult ar) {
        SignalStrength oldSignalStrength = mSignalStrength;
        int rssi = 99;
        int lteSignalStrength = -1;
        int lteRsrp = -1;
        int lteRsrq = -1;
        int lteRssnr = SignalStrength.INVALID_SNR;
        int lteCqi = -1;
        int rscpQdbm = 0;
        
        if (ar.exception != null) {
            // -1 = unknown
            // most likely radio is resetting/disconnected
            setSignalStrengthDefaultValues();
        } else {
            int[] ints = (int[])ar.result;

            // bug 658816 seems to be a case where the result is 0-length
            if (ints.length != 0) {
                rssi = ints[0];
                /* ALPS00296741: Fix potential issue, only 3G signal strength result length is larger than 3 */
                if(ints.length > 3)				
                {				
                    rscpQdbm = ints[3];
                    lteSignalStrength = ints[7];
                    lteRsrp = ints[8];
                    lteRsrq = ints[9];
                    lteRssnr = ints[10];
                    lteCqi = ints[11];
                }				
            } else {
                loge("Bogus signal strength response");
                rssi = 99;
            }
        }

        //BEGIN mtk03923 [20120115][ALPS00113979]
        //mSignalStrength = new SignalStrength(rssi, -1, -1, -1, -1, -1, -1, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, true);
        //MTK-START [mtk04258][120308][ALPS00237725]For CMCC
        mSignalStrength = new SignalStrength(rssi, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, true, rscpQdbm);
        //MTK-END [mtk04258][120308][ALPS00237725]For CMCC
        //BEGIN mtk03923 [20120115][ALPS00113979]

        if (!mSignalStrength.equals(oldSignalStrength)) {
            try { // This takes care of delayed EVENT_POLL_SIGNAL_STRENGTH (scheduled after
                  // POLL_PERIOD_MILLIS) during Radio Technology Change)
                //20120317 ALPS_00253948	 ignore unknown RSSI state (99)			  
                if((rssi == 99)&&(ss.getState() == ServiceState.STATE_IN_SERVICE)){   
                    log("Ignore rssi 99(unknown)");
                }	
                else{    
                    phone.notifySignalStrength();
                }
           } catch (NullPointerException ex) {
                log("onSignalStrengthResult() Phone already destroyed: " + ex
                        + "SignalStrength not notified");
           }
        }
        return rssi;
    }

    /**
     * Set restricted state based on the OnRestrictedStateChanged notification
     * If any voice or packet restricted state changes, trigger a UI
     * notification and notify registrants when sim is ready.
     *
     * @param ar an int value of RIL_RESTRICTED_STATE_*
     */
    private void onRestrictedStateChanged(AsyncResult ar) {
        RestrictedState newRs = new RestrictedState();

        if (DBG) log("onRestrictedStateChanged: E rs "+ mRestrictedState);

        if (ar.exception == null) {
            int[] ints = (int[])ar.result;
            int state = ints[0];

            newRs.setCsEmergencyRestricted(
                    ((state & RILConstants.RIL_RESTRICTED_STATE_CS_EMERGENCY) != 0) ||
                    ((state & RILConstants.RIL_RESTRICTED_STATE_CS_ALL) != 0) );
            //ignore the normal call and data restricted state before SIM READY
            if (phone.getIccCard().getState() == IccCard.State.READY) {
                newRs.setCsNormalRestricted(
                        ((state & RILConstants.RIL_RESTRICTED_STATE_CS_NORMAL) != 0) ||
                        ((state & RILConstants.RIL_RESTRICTED_STATE_CS_ALL) != 0) );
                newRs.setPsRestricted(
                        (state & RILConstants.RIL_RESTRICTED_STATE_PS_ALL)!= 0);
            } else {
                log("[DSAC DEB] IccCard state Not ready ");
                if (mRestrictedState.isCsNormalRestricted() && 
                	((state & RILConstants.RIL_RESTRICTED_STATE_CS_NORMAL) == 0 &&
                	(state & RILConstants.RIL_RESTRICTED_STATE_CS_ALL) == 0)) {
                        newRs.setCsNormalRestricted(false);
                }

                if(mRestrictedState.isPsRestricted() && ((state & RILConstants.RIL_RESTRICTED_STATE_PS_ALL) == 0)) {
                    newRs.setPsRestricted(false);
                }
    	    }

            log("[DSAC DEB] new rs "+ newRs);

            if (!mRestrictedState.isPsRestricted() && newRs.isPsRestricted()) {
                mPsRestrictEnabledRegistrants.notifyRegistrants();
                setNotification(PS_ENABLED);
            } else if (mRestrictedState.isPsRestricted() && !newRs.isPsRestricted()) {
                mPsRestrictDisabledRegistrants.notifyRegistrants();
                setNotification(PS_DISABLED);
            }

            /**
             * There are two kind of cs restriction, normal and emergency. So
             * there are 4 x 4 combinations in current and new restricted states
             * and we only need to notify when state is changed.
             */
            if (mRestrictedState.isCsRestricted()) {
                if (!newRs.isCsRestricted()) {
                    // remove all restriction
                    setNotification(CS_DISABLED);
                } else if (!newRs.isCsNormalRestricted()) {
                    // remove normal restriction
                    setNotification(CS_EMERGENCY_ENABLED);
                } else if (!newRs.isCsEmergencyRestricted()) {
                    // remove emergency restriction
                    setNotification(CS_NORMAL_ENABLED);
                }
            } else if (mRestrictedState.isCsEmergencyRestricted() &&
                    !mRestrictedState.isCsNormalRestricted()) {
                if (!newRs.isCsRestricted()) {
                    // remove all restriction
                    setNotification(CS_DISABLED);
                } else if (newRs.isCsRestricted()) {
                    // enable all restriction
                    setNotification(CS_ENABLED);
                } else if (newRs.isCsNormalRestricted()) {
                    // remove emergency restriction and enable normal restriction
                    setNotification(CS_NORMAL_ENABLED);
                }
            } else if (!mRestrictedState.isCsEmergencyRestricted() &&
                    mRestrictedState.isCsNormalRestricted()) {
                if (!newRs.isCsRestricted()) {
                    // remove all restriction
                    setNotification(CS_DISABLED);
                } else if (newRs.isCsRestricted()) {
                    // enable all restriction
                    setNotification(CS_ENABLED);
                } else if (newRs.isCsEmergencyRestricted()) {
                    // remove normal restriction and enable emergency restriction
                    setNotification(CS_EMERGENCY_ENABLED);
                }
            } else {
                if (newRs.isCsRestricted()) {
                    // enable all restriction
                    setNotification(CS_ENABLED);
                } else if (newRs.isCsEmergencyRestricted()) {
                    // enable emergency restriction
                    setNotification(CS_EMERGENCY_ENABLED);
                } else if (newRs.isCsNormalRestricted()) {
                    // enable normal restriction
                    setNotification(CS_NORMAL_ENABLED);
                }
            }

            mRestrictedState = newRs;
        }
        log("onRestrictedStateChanged: X rs "+ mRestrictedState);
    }

    /** code is registration state 0-5 from TS 27.007 7.2 */
    private int regCodeToServiceState(int code) {
        switch (code) {
            case 0:
            case 2: // 2 is "searching"
            case 3: // 3 is "registration denied"
            case 4: // 4 is "unknown" no vaild in current baseband
            case 10:// same as 0, but indicates that emergency call is possible.
            case 12:// same as 2, but indicates that emergency call is possible.
            case 13:// same as 3, but indicates that emergency call is possible.
            case 14:// same as 4, but indicates that emergency call is possible.
                return ServiceState.STATE_OUT_OF_SERVICE;

            case 1:
                return ServiceState.STATE_IN_SERVICE;

            case 5:
                // in service, roam
                return ServiceState.STATE_IN_SERVICE;

            default:
                loge("regCodeToServiceState: unexpected service state " + code);
                return ServiceState.STATE_OUT_OF_SERVICE;
        }
    }


    /**
     * code is registration state 0-5 from TS 27.007 7.2
     * returns true if registered roam, false otherwise
     */
    private boolean regCodeIsRoaming (int code) {
        Log.d(LOG_TAG, "regCodeIsRoaming mSimId=" +mSimId);
        boolean isRoaming = false;
        SIMRecords simRecords = (SIMRecords)(phone.mIccRecords);			
//MTK-START [mtk04258][20120806][International card support]
        Log.d(LOG_TAG, "regCodeIsRoaming phone=" +phone);
        // fix me, dynamic switch maybe leads null pointer exception
        if (simRecords == null) { 
            Log.e(LOG_TAG, "regCodeIsRoaming simRecords=null");
            //return false;
        }
//MTK-END [mtk04258][20120806][International card support]
        String strHomePlmn = simRecords.getSIMOperatorNumeric();
        String strServingPlmn = newSS.getOperatorNumeric();    		
        boolean isServingPlmnInGroup = false;
        boolean isHomePlmnInGroup = false;		

        if(5 == code){
            isRoaming = true;
        }

    	//MTK_OP03_PROTECT_START        
        String optr = SystemProperties.get("ro.operator.optr");
        if((optr != null)&&(optr.equals("OP03"))) {
            int mccmnc = 0;
            if (phone.getMySimId() == Phone.GEMINI_SIM_1) {
                mccmnc = SystemProperties.getInt(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, 0);
            } else {
                mccmnc = SystemProperties.getInt(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC_2, 0);
            }
            String numeric = newSS.getOperatorNumeric();
            Log.d(LOG_TAG,"numeric:"+numeric+"mccmnc:"+mccmnc);
            if( numeric != null && numeric != ""){
                if(MccTable.isInOrange(mccmnc) 
                    && MccTable.isInOrange(Integer.parseInt(numeric))){
                    isRoaming = false;
                }
            }
        }
        //MTK_OP03_PROTECT_END

        /* mtk01616 ALPS00236452: check manufacturer maintained table for specific operator with multiple home PLMN id */
        if((isRoaming == true) && (strServingPlmn != null) &&(strHomePlmn != null)){
            log("strServingPlmn = "+strServingPlmn+"strHomePlmn"+strHomePlmn);

            for(int i=0; i <customEhplmn.length; i++){
                //reset flag
                isServingPlmnInGroup = false;
                isHomePlmnInGroup = false;

                //check if serving plmn or home plmn in this group
                for(int j=0; j<	customEhplmn[i].length;j++){			
                    if(strServingPlmn.equals(customEhplmn[i][j])){
                        isServingPlmnInGroup = true;		
                    }	
                    if(strHomePlmn.equals(customEhplmn[i][j])){
                        isHomePlmnInGroup = true;	
                    }						
                }					

                //if serving plmn and home plmn both in the same group , do NOT treat it as roaming
                if((isServingPlmnInGroup == true) && (isHomePlmnInGroup == true)){
                    isRoaming = false;
                    log("Ignore roaming");					
                    break;
                }
            }								
        }

        return isRoaming;    

////        // 5 is  "in service -- roam"
////        return 5 == code;
    }

    /**
     * Set roaming state when gsmRoaming is true and, if operator mcc is the
     * same as sim mcc, ons is different from spn
     * @param gsmRoaming TS 27.007 7.2 CREG registered roaming
     * @param s ServiceState hold current ons
     * @return true for roaming state set
     */
    private boolean isRoamingBetweenOperators(boolean gsmRoaming, ServiceState s) {
        String spn = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA, "empty");

        String onsl = s.getOperatorAlphaLong();
        String onss = s.getOperatorAlphaShort();
        String simNumeric;
        String  operatorNumeric = s.getOperatorNumeric();

        if (mSimId == Phone.GEMINI_SIM_1) {
            spn = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA, "empty");
            simNumeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, "");
        } else {
            spn = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA_2, "empty");
            simNumeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC_2, "");
        }

        boolean equalsOnsl = onsl != null && !onsl.equals("") && spn.equals(onsl);
        boolean equalsOnss = onss != null && !onss.equals("") && spn.equals(onss);

        boolean equalsMcc = true;
        try {
            equalsMcc = simNumeric.substring(0, 3).
                    equals(operatorNumeric.substring(0, 3));
        } catch (Exception e){
            Log.w(LOG_TAG, "simNumeric parsing error: " + simNumeric);
            e.printStackTrace();
        }

        return gsmRoaming && !(equalsMcc && (equalsOnsl || equalsOnss));
    }

    private static int twoDigitsAt(String s, int offset) {
        int a, b;

        a = Character.digit(s.charAt(offset), 10);
        b = Character.digit(s.charAt(offset+1), 10);

        if (a < 0 || b < 0) {

            throw new RuntimeException("invalid format");
        }

        return a*10 + b;
    }

    /**
     * @return The current GPRS state. IN_SERVICE is the same as "attached"
     * and OUT_OF_SERVICE is the same as detached.
     */
    int getCurrentGprsState() {
        return gprsState;
    }

    public int getCurrentDataConnectionState() {
        return gprsState;
    }

    /**
     * @return true if phone is camping on a technology (eg UMTS)
     * that could support voice and data simultaneously.
     */
    public boolean isConcurrentVoiceAndDataAllowed() {
        return (mRadioTechnology >= ServiceState.RADIO_TECHNOLOGY_UMTS);
    }

    /**
     * Provides the name of the algorithmic time zone for the specified
     * offset.  Taken from TimeZone.java.
     */
    private static String displayNameFor(int off) {
        off = off / 1000 / 60;

        char[] buf = new char[9];
        buf[0] = 'G';
        buf[1] = 'M';
        buf[2] = 'T';

        if (off < 0) {
            buf[3] = '-';
            off = -off;
        } else {
            buf[3] = '+';
        }

        int hours = off / 60;
        int minutes = off % 60;

        buf[4] = (char) ('0' + hours / 10);
        buf[5] = (char) ('0' + hours % 10);

        buf[6] = ':';

        buf[7] = (char) ('0' + minutes / 10);
        buf[8] = (char) ('0' + minutes % 10);

        return new String(buf);
    }

    /**
     * nitzReceiveTime is time_t that the NITZ time was posted
     */
    private void setTimeFromNITZString (String nitz, long nitzReceiveTime) {
        // "yy/mm/dd,hh:mm:ss(+/-)tz"
        // tz is in number of quarter-hours

        long start = SystemClock.elapsedRealtime();
        if (DBG) {log("NITZ: " + nitz + "," + nitzReceiveTime +
                        " start=" + start + " delay=" + (start - nitzReceiveTime));
        }

        try {
            /* NITZ time (hour:min:sec) will be in UTC but it supplies the timezone
             * offset as well (which we won't worry about until later) */
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

            c.clear();
            c.set(Calendar.DST_OFFSET, 0);

            String[] nitzSubs = nitz.split("[/:,+-]");

            int year = 2000 + Integer.parseInt(nitzSubs[0]);
            c.set(Calendar.YEAR, year);

            // month is 0 based!
            int month = Integer.parseInt(nitzSubs[1]) - 1;
            c.set(Calendar.MONTH, month);

            int date = Integer.parseInt(nitzSubs[2]);
            c.set(Calendar.DATE, date);

            int hour = Integer.parseInt(nitzSubs[3]);
            c.set(Calendar.HOUR, hour);

            int minute = Integer.parseInt(nitzSubs[4]);
            c.set(Calendar.MINUTE, minute);

            int second = Integer.parseInt(nitzSubs[5]);
            c.set(Calendar.SECOND, second);

            boolean sign = (nitz.indexOf('-') == -1);

            int tzOffset = Integer.parseInt(nitzSubs[6]);

            int dst = (nitzSubs.length >= 8 ) ? Integer.parseInt(nitzSubs[7])
                                              : 0;

            // The zone offset received from NITZ is for current local time,
            // so DST correction is already applied.  Don't add it again.
            //
            // tzOffset += dst * 4;
            //
            // We could unapply it if we wanted the raw offset.

            tzOffset = (sign ? 1 : -1) * tzOffset * 15 * 60 * 1000;

            TimeZone    zone = null;

            // As a special extension, the Android emulator appends the name of
            // the host computer's timezone to the nitz string. this is zoneinfo
            // timezone name of the form Area!Location or Area!Location!SubLocation
            // so we need to convert the ! into /
            if (nitzSubs.length >= 9) {
                String  tzname = nitzSubs[8].replace('!','/');
                zone = TimeZone.getTimeZone( tzname );
                log("[NITZ] setTimeFromNITZString,tzname:"+tzname+"zone:"+zone);
            }

            String iso;
            if (mSimId == Phone.GEMINI_SIM_1) {
                iso = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY);
            } else {
                iso = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY_2);
            }
            log("[NITZ] setTimeFromNITZString,mGotCountryCode:"+mGotCountryCode);

            if (zone == null) {

                if (mGotCountryCode) {
                    if (iso != null && iso.length() > 0) {
                        zone = TimeUtils.getTimeZone(tzOffset, dst != 0,
                                c.getTimeInMillis(),
                                iso);
                    } else {
                        // We don't have a valid iso country code.  This is
                        // most likely because we're on a test network that's
                        // using a bogus MCC (eg, "001"), so get a TimeZone
                        // based only on the NITZ parameters.
                        zone = getNitzTimeZone(tzOffset, (dst != 0), c.getTimeInMillis());
                    }
                }
            }

            if (zone == null) {
                // We got the time before the country, so we don't know
                // how to identify the DST rules yet.  Save the information
                // and hope to fix it up later.

                mNeedFixZone = true;
                mZoneOffset  = tzOffset;
                mZoneDst     = dst != 0;
                mZoneTime    = c.getTimeInMillis();
            }

            if (zone != null) {
                if (getAutoTimeZone()) {
                    setAndBroadcastNetworkSetTimeZone(zone.getID());
                }
                saveNitzTimeZone(zone.getID());
            }

            String ignore = SystemProperties.get("gsm.ignore-nitz");
            if (ignore != null && ignore.equals("yes")) {
                log("NITZ: Not setting clock because gsm.ignore-nitz is set");
                return;
            }

            try {
                mWakeLock.acquire();

                if (getAutoTime()) {
                    long millisSinceNitzReceived
                            = SystemClock.elapsedRealtime() - nitzReceiveTime;

                    if (millisSinceNitzReceived < 0) {
                        // Sanity check: something is wrong
                        if (DBG) {
                            log("NITZ: not setting time, clock has rolled "
                                            + "backwards since NITZ time was received, "
                                            + nitz);
                        }
                        return;
                    }

                    if (millisSinceNitzReceived > Integer.MAX_VALUE) {
                        // If the time is this far off, something is wrong > 24 days!
                        if (DBG) {
                            log("NITZ: not setting time, processing has taken "
                                        + (millisSinceNitzReceived / (1000 * 60 * 60 * 24))
                                        + " days");
                        }
                        return;
                    }

                    // Note: with range checks above, cast to int is safe
                    c.add(Calendar.MILLISECOND, (int)millisSinceNitzReceived);

                    if (DBG) {
                        log("NITZ: Setting time of day to " + c.getTime()
                            + " NITZ receive delay(ms): " + millisSinceNitzReceived
                            + " gained(ms): "
                            + (c.getTimeInMillis() - System.currentTimeMillis())
                            + " from " + nitz);
                    }

                    setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                    Log.i(LOG_TAG, "NITZ: after Setting time of day");
                }
                SystemProperties.set("gsm.nitz.time", String.valueOf(c.getTimeInMillis()));
                saveNitzTime(c.getTimeInMillis());
                if (DBG) {
                    long end = SystemClock.elapsedRealtime();
                    log("NITZ: end=" + end + " dur=" + (end - start));
                }
            } finally {
                mWakeLock.release();
            }
        } catch (RuntimeException ex) {
            loge("NITZ: Parsing NITZ time " + nitz + " ex=" + ex);
        }
    }

    private boolean getAutoTime() {
        try {
            return Settings.System.getInt(phone.getContext().getContentResolver(),
                    Settings.System.AUTO_TIME) > 0;
        } catch (SettingNotFoundException snfe) {
            return true;
        }
    }

    private boolean getAutoTimeZone() {
        try {
            return Settings.System.getInt(phone.getContext().getContentResolver(),
                    Settings.System.AUTO_TIME_ZONE) > 0;
        } catch (SettingNotFoundException snfe) {
            return true;
        }
    }

    private void saveNitzTimeZone(String zoneId) {
        mSavedTimeZone = zoneId;
    }

    private void saveNitzTime(long time) {
        mSavedTime = time;
        mSavedAtTime = SystemClock.elapsedRealtime();
    }

    /**
     * Set the timezone and send out a sticky broadcast so the system can
     * determine if the timezone was set by the carrier.
     *
     * @param zoneId timezone set by carrier
     */
    private void setAndBroadcastNetworkSetTimeZone(String zoneId) {
        AlarmManager alarm =
            (AlarmManager) phone.getContext().getSystemService(Context.ALARM_SERVICE);
        alarm.setTimeZone(zoneId);
        Intent intent = new Intent(TelephonyIntents.ACTION_NETWORK_SET_TIMEZONE);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra("time-zone", zoneId);
        phone.getContext().sendStickyBroadcast(intent);
    }

    /**
     * Set the time and Send out a sticky broadcast so the system can determine
     * if the time was set by the carrier.
     *
     * @param time time set by network
     */
    private void setAndBroadcastNetworkSetTime(long time) {
        SystemClock.setCurrentTimeMillis(time);
        Intent intent = new Intent(TelephonyIntents.ACTION_NETWORK_SET_TIME);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra("time", time);
        phone.getContext().sendStickyBroadcast(intent);
    }

    private void revertToNitzTime() {
        if (Settings.System.getInt(phone.getContext().getContentResolver(),
                Settings.System.AUTO_TIME, 0) == 0) {
        	log("[NITZ]:revertToNitz,AUTO_TIME is 0");
            return;
        }
       log("[NITZ]:Reverting to NITZ: tz='" + mSavedTimeZone
                + "' mSavedTime=" + mSavedTime
                + " mSavedAtTime=" + mSavedAtTime);
        if (mSavedTime != 0 && mSavedAtTime != 0) {
            setAndBroadcastNetworkSetTimeZone(mSavedTimeZone);
            setAndBroadcastNetworkSetTime(mSavedTime
                    + (SystemClock.elapsedRealtime() - mSavedAtTime));
        }
    }

    private void revertToNitzTimeZone() {
        if (Settings.System.getInt(phone.getContext().getContentResolver(),
                Settings.System.AUTO_TIME_ZONE, 0) == 0) {
            return;
        }
        if (DBG) log("Reverting to NITZ TimeZone: tz='" + mSavedTimeZone);
        if (mSavedTimeZone != null) {
            setAndBroadcastNetworkSetTimeZone(mSavedTimeZone);
        }
    }

    /**
     * Post a notification to NotificationManager for restricted state
     *
     * @param notifyType is one state of PS/CS_*_ENABLE/DISABLE
     */
    private void setNotification(int notifyType) {

        if (DBG) log("setNotification: create notification " + notifyType);
        Context context = phone.getContext();

        mNotification = new Notification();
        mNotification.when = System.currentTimeMillis();
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
        mNotification.icon = com.android.internal.R.drawable.stat_sys_warning;
        Intent intent = new Intent();
        mNotification.contentIntent = PendingIntent
        .getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        CharSequence details = "";
        CharSequence title = context.getText(com.android.internal.R.string.RestrictedChangedTitle);
        int notificationId = CS_NOTIFICATION;

		if (FeatureOption.MTK_GEMINI_SUPPORT) {
		log("show name log");
        	if (FeatureOption.MTK_GEMINI_ENHANCEMENT == true) {
        		SIMInfo siminfo = SIMInfo.getSIMInfoBySlot(phone.getContext(),mSimId);
        		if (siminfo != null){
        			mNotification.simId = siminfo.mSimId;
        			mNotification.simInfoType = 3;  
				if (mSimId != phone.GEMINI_SIM_1){
					notificationId = CS_NOTIFICATION_2;				
				}        			
        		}
        	}else {
			log("show sim1 log");
        		if (mSimId == phone.GEMINI_SIM_1) {
        			//title = context.getText(com.mediatek.R.string.RestrictedChangedTitle_SIM1);
        			title = "SIM1-" + context.getText(com.android.internal.R.string.RestrictedChangedTitle);
        		} else {
        			notificationId = CS_NOTIFICATION_2;
        			//title = context.getText(com.mediatek.R.string.RestrictedChangedTitle_SIM2);
        			title = "SIM2-" + context.getText(com.android.internal.R.string.RestrictedChangedTitle);
        		}
            }
        }

        switch (notifyType) {
        case PS_ENABLED:
            if (FeatureOption.MTK_GEMINI_SUPPORT && mSimId == phone.GEMINI_SIM_2) {
                notificationId = PS_NOTIFICATION_2;
            } else {
            	notificationId = PS_NOTIFICATION;
            }
            details = context.getText(com.android.internal.R.string.RestrictedOnData);;
            break;
        case PS_DISABLED:
            if (FeatureOption.MTK_GEMINI_SUPPORT && mSimId == phone.GEMINI_SIM_2) {
                notificationId = PS_NOTIFICATION_2;
            } else {
            	notificationId = PS_NOTIFICATION;
            }
            break;
        case CS_ENABLED:
            details = context.getText(com.android.internal.R.string.RestrictedOnAllVoice);;
            break;
        case CS_NORMAL_ENABLED:
            details = context.getText(com.android.internal.R.string.RestrictedOnNormal);;
            break;
        case CS_EMERGENCY_ENABLED:
            details = context.getText(com.android.internal.R.string.RestrictedOnEmergency);;
            break;
        case CS_DISABLED:
            // do nothing and cancel the notification later
            break;
        }

        if (DBG) log("setNotification: put notification " + title + " / " +details);
        mNotification.tickerText = title;
        mNotification.setLatestEventInfo(context, title, details,
                mNotification.contentIntent);

        NotificationManager notificationManager = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);

        //if (notifyType == PS_DISABLED || notifyType == CS_DISABLED) {
        //this is a temp solution from GB for resolving restricted mode notification problem (not to notify PS restricted)
        if (notifyType == PS_DISABLED || notifyType == CS_DISABLED || notifyType == PS_ENABLED) {
            // cancel previous post notification
            notificationManager.cancel(notificationId);
        } else {
            // update restricted state notification
            if (FeatureOption.MTK_GEMINI_SUPPORT && notifyType == PS_ENABLED) {
                //since we do not have to notice user that PS restricted
                //if default data SIM is not set to current PS restricted SIM
                //or it is in air plane mode or radio power is off
                int airplaneMode = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
                int dualSimMode = Settings.System.getInt(context.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 0);
                long dataSimID = Settings.System.getLong(context.getContentResolver(), Settings.System.GPRS_CONNECTION_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET);
                int dataSimSlot = SIMInfo.getSlotById(context, dataSimID);
                if (dataSimSlot == mSimId) {
                    if (airplaneMode != 0)
                        log("set notification but air plane mode, skip");
                    else if (phone.isSimInsert() && !((dualSimMode & (mSimId + 1)) == 0))
                        notificationManager.notify(notificationId, mNotification);
                    else
                        log("set notification but sim radio power off, skip");
                } else {
                    log("set notification but not data enabled SIM, skip");
                }
            } else {
                notificationManager.notify(notificationId, mNotification);
            }
        }
    }

    // ALPS00297554
    public void resetNotification() {
        int notificationId = CS_NOTIFICATION;
        if (mSimId == phone.GEMINI_SIM_2)
            notificationId = CS_NOTIFICATION_2;

        Context context = phone.getContext();
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    @Override
    protected void log(String s) {
        Log.d(LOG_TAG, "[GsmSST" + mSimId + "] " + s);
    }

    @Override
    protected void loge(String s) {
        Log.e(LOG_TAG, "[GsmSST" + mSimId + "] " + s);
    }

//MTK-START [mtk03851][111124]
    public void setRadioPowerOn() {
        // system setting property AIRPLANE_MODE_ON is set in Settings.
        int airplaneMode = Settings.System.getInt(
                phone.getContext().getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0);
        mDesiredPowerState = ! (airplaneMode > 0);

        //since this will trigger radio power on
        //we should reset first radio change here
        mFirstRadioChange = true;

        log("setRadioPowerOn mDesiredPowerState " + mDesiredPowerState);
        cm.setRadioPowerOn(null);
    }

    public void setEverIVSR(boolean value)
    {
        log("setEverIVSR:" + value);    
        mEverIVSR = value;
    }	

    public void setAutoGprsAttach(int auto) {
        mAutoGprsAttach = auto;
    }

    public void setGprsConnType(int type) {
        log("setGprsConnType:" + type);
        removeGprsConnTypeRetry();
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            DataConnectionTracker dcTracker = phone.mDataConnectionTracker;
            if (type == 0) {
                // Not Gprs Attach (set mMasterDataEnabled as false)
                dcTracker.setDataEnabled(false);
            } else {
                // Auto Gprs Attach then activate the default apn type's pdp context (set mMasterDataEnabled as true)
                dcTracker.setDataEnabled(true);
            }
        }
        
        gprsConnType = type;
        cm.setGprsConnType(type, obtainMessage(EVENT_SET_GPRS_CONN_TYPE_DONE, null));
    }

    private int updateAllOpertorInfo(String plmn){
        if(plmn!=null){			
            ss.setOperatorAlphaLong(plmn);
            if (mSimId == Phone.GEMINI_SIM_1) {
                Log.d(LOG_TAG, "setOperatorAlphaLong and update PROPERTY_OPERATOR_ALPHA to"+ss.getOperatorAlphaLong());				
                phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ALPHA, ss.getOperatorAlphaLong());
            } else {
                Log.d(LOG_TAG, "setOperatorAlphaLong and update PROPERTY_OPERATOR_ALPHA_2 to"+ss.getOperatorAlphaLong());				
                phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ALPHA_2, ss.getOperatorAlphaLong());                
            }	
        }   
        return 1;
    }		

    public void refreshSpnDisplay() {
        String numeric = ss.getOperatorNumeric();
        String newAlphaLong = null;
        String newAlphaShort = null;
        boolean force = false;
        
        if (numeric != null) {
            newAlphaLong = cm.lookupOperatorName(numeric, true);
            newAlphaShort = cm.lookupOperatorName(numeric, false);
	     if (mSimId == Phone.GEMINI_SIM_1) {
                phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ALPHA, newAlphaLong);
            } else {
                phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ALPHA_2, newAlphaLong);                
            }		
        } else {
            force = true;
        }

        Log.d(LOG_TAG, "refreshSpnDisplay set mSimId=" +mSimId+","+newAlphaLong +","+newAlphaShort+","+numeric);

        ss.setOperatorName(newAlphaLong, newAlphaShort, numeric);
        updateSpnDisplay(force);
    }

    protected void updateSpnDisplay() {
        updateSpnDisplay(false);
    }

    protected void updateSpnDisplay(boolean forceUpdate) {
        Log.d(LOG_TAG, "updateSpnDisplay mSimId=" +mSimId);
        SIMRecords simRecords = (SIMRecords)(phone.mIccRecords);
//MTK-START [mtk04258][20120806][International card support]
        Log.d(LOG_TAG, "updateSpnDisplay phone=" +phone);
        // fix me
        if (simRecords == null) { 
            Log.e(LOG_TAG, "updateSpnDisplay simRecords=null");
            //return;
        }
//MTK-END [mtk04258][20120806][International card support]
        int rule = simRecords.getDisplayRule(ss.getOperatorNumeric());
        //int rule = SIMRecords.SPN_RULE_SHOW_PLMN;
        String strNumPlmn = ss.getOperatorNumeric();
        String spn = simRecords.getServiceProviderName();
        //String plmn = ss.getOperatorAlphaLong();
        String sEons = null;
        try {	
            sEons = simRecords.getEonsIfExist(ss.getOperatorNumeric(), cellLoc.getLac(), true);
        } catch (RuntimeException ex) {
            Log.e(LOG_TAG, "Exception while getEonsIfExist. ", ex);
        }

        String plmn = null;
        if(sEons != null) {
            plmn = sEons;
        }
        else if (strNumPlmn != null && strNumPlmn.equals(simRecords.getSIMOperatorNumeric())){
	     Log.d(LOG_TAG, "Home PLMN, get CPHS ons");		
	     plmn = simRecords.getSIMCPHSOns();
        }	

        if (plmn == null || plmn.equals("")) {
	     Log.d(LOG_TAG, "No matched EONS and No CPHS ONS"); 		
            plmn = ss.getOperatorAlphaLong();
	     if (plmn == null || plmn.equals(ss.getOperatorNumeric())) {
	         plmn = ss.getOperatorAlphaShort();
	     }	  
	 }
		
        /* ALPS00357573 to make operator name display consistent */				
        updateAllOpertorInfo(plmn); 
        
        // For emergency calls only, pass the EmergencyCallsOnly string via EXTRA_PLMN
        if (mEmergencyOnly && cm.getRadioState().isOn()) {
            plmn = Resources.getSystem().
                getText(com.android.internal.R.string.emergency_calls_only).toString();
        }

        // Do not display SPN before get normal service
        if (ss.getState() != ServiceState.STATE_IN_SERVICE) {
            rule = SIMRecords.SPN_RULE_SHOW_PLMN;
            plmn = null;
        }

        /**
        * mImeiAbnormal=0, Valid IMEI
        * mImeiAbnormal=1, IMEI is null or not valid format
        * mImeiAbnormal=2, Phone1/Phone2 have same IMEI
        */
        if (phone.isDeviceIdAbnormal() == 1) {
            plmn = Resources.getSystem().getText(com.mediatek.R.string.invalid_imei).toString();
            rule = SIMRecords.SPN_RULE_SHOW_PLMN;
        } else if (phone.isDeviceIdAbnormal() == 2) {
            plmn = Resources.getSystem().getText(com.mediatek.R.string.same_imei).toString();
            rule = SIMRecords.SPN_RULE_SHOW_PLMN;
        }

      //MTK_OP01_PROTECT_START  
		/**
        * networkType = 1,2 2G
        * others is 3G
        */
        if ((("OP01".equals(mOptr))||(FeatureOption.MTK_NETWORK_TYPE_ALWAYS_ON == true)) && 0 == phone.isDeviceIdAbnormal() ){
            //BEGIN mtk03923[20120119][ALPS00114079]
            //if (mNewRadioTechnology > 2 && plmn != null ) {
            if (mRadioTechnology > 2 && plmn != null ) {
            //END   mtk03923[20120119][ALPS00114079]
                plmn = plmn + " 3G"; 
            }
        }
      //MTK_OP01_PROTECT_END

        if (rule != curSpnRule
                || !TextUtils.equals(spn, curSpn)
                || !TextUtils.equals(plmn, curPlmn)
                || forceUpdate) {
            boolean showSpn =
                (rule & SIMRecords.SPN_RULE_SHOW_SPN) == SIMRecords.SPN_RULE_SHOW_SPN;
            boolean showPlmn =
                (rule & SIMRecords.SPN_RULE_SHOW_PLMN) == SIMRecords.SPN_RULE_SHOW_PLMN;

            Intent intent = new Intent(Intents.SPN_STRINGS_UPDATED_ACTION);
            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);

            // [ALPS00125833]
            // For Gemini, share the same intent, do not replace the other one
            if (!FeatureOption.MTK_GEMINI_SUPPORT) {
                intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            }

            intent.putExtra(Intents.EXTRA_SHOW_SPN, showSpn);
            intent.putExtra(Intents.EXTRA_SPN, spn);
            intent.putExtra(Intents.EXTRA_SHOW_PLMN, showPlmn);
            intent.putExtra(Intents.EXTRA_PLMN, plmn);
            phone.getContext().sendStickyBroadcast(intent);

            log(" showSpn:" + showSpn + 
                    " spn:" + spn + 
                    " showPlmn:" + showPlmn + 
                    " plmn:" + plmn +
                    " rule:" + rule);
        }

        curSpnRule = rule;
        curSpn = spn;
        curPlmn = plmn;
    }

    void registerForPsRegistrants(Handler h, int what, Object obj) {
        Log.d(LOG_TAG, "[DSAC DEB] " + "registerForCsRegistrants");
        Registrant r = new Registrant(h, what, obj);
        ratPsChangedRegistrants.add(r);
    }

    void unregisterForPsRegistrants(Handler h) {
    	ratPsChangedRegistrants.remove(h);
    }

    void registerForRatRegistrants(Handler h, int what, Object obj) {
        Log.d(LOG_TAG, "[DSAC DEB] " + "registerForRatRegistrants");
        Registrant r = new Registrant(h, what, obj);
        ratCsChangedRegistrants.add(r);
    }

    void unregisterForRatRegistrants(Handler h) {
    	ratCsChangedRegistrants.remove(h);
    }

    //ALPS00248788
    private void onInvalidSimInfoReceived(AsyncResult ar) {
        String[] InvalidSimInfo = (String[]) ar.result;
        String plmn = InvalidSimInfo[0];
        int cs_invalid = Integer.parseInt(InvalidSimInfo[1]);		
        int ps_invalid = Integer.parseInt(InvalidSimInfo[2]);	
        int cause = Integer.parseInt(InvalidSimInfo[3]);				
        int testMode = -1;
        long ivsr_setting = Settings.System.getLong(phone.getContext().getContentResolver(),
			                                        Settings.System.IVSR_SETTING,Settings.System.IVSR_SETTING_DISABLE);

        log("InvalidSimInfo received during ivsr_setting: "+ ivsr_setting);			            

        // do NOT apply IVSR when EM IVSR setting is disabled		
        if(ivsr_setting == Settings.System.IVSR_SETTING_DISABLE)
        {
            return;
        }
		
        // do NOT apply IVSR when in TEST mode
        if(phone.getMySimId() == Phone.GEMINI_SIM_1){
            testMode = SystemProperties.getInt("gsm.gcf.testmode", 0);
        }else{
            testMode = SystemProperties.getInt("gsm.gcf.testmode2", 0);
        }
                
        log("onInvalidSimInfoReceived testMode:" + testMode+" cause:"+cause+" cs_invalid:"+cs_invalid+" ps_invalid:"+ps_invalid+" plmn:"+plmn+"mEverIVSR"+mEverIVSR);

        //Check UE is set to test mode or not	(CTA =1,FTA =2 , IOT=3 ...)	
        if(testMode != 0){
            log("InvalidSimInfo received during test mode: "+ testMode);			
            return;
        }			

        /* check if CS domain ever sucessfully registered to the invalid SIM PLMN */
        if((cs_invalid == 1)&& (mLastRegisteredPLMN != null) && (plmn.equals(mLastRegisteredPLMN)))
        {
            log("InvalidSimInfo set TRM due to CS invalid");	        
            setEverIVSR(true);			
            mLastRegisteredPLMN = null;			
            mLastPSRegisteredPLMN = null;						
            phone.setTRM(3, null);
            return;
        }    

        /* check if PS domain ever sucessfully registered to the invalid SIM PLMN */
        if((ps_invalid == 1)&& (mLastPSRegisteredPLMN != null) && (plmn.equals(mLastPSRegisteredPLMN)))
        {
            log("InvalidSimInfo set TRM due to PS invalid ");	        
            setEverIVSR(true);			
            mLastRegisteredPLMN = null;						
            mLastPSRegisteredPLMN = null;			
            phone.setTRM(3, null);
            return;
        }		

        /* ALPS00324111: to force trigger IVSR */
        if ((mEverIVSR == false) && (gprsState != ServiceState.STATE_IN_SERVICE) &&(ss.getState() != ServiceState.STATE_IN_SERVICE))
        {
            log("InvalidSimInfo set TRM due to never set IVSR");	                
            setEverIVSR(true);
            mLastRegisteredPLMN = null;			
            mLastPSRegisteredPLMN = null;						
            phone.setTRM(3, null);			
            return;			
        }	

    }	

    public void removeGprsConnTypeRetry() {
        removeMessages(EVENT_SET_GPRS_CONN_RETRY);
    }
//MTK-END [mtk03851][111124]
}
