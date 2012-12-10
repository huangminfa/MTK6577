/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.internal.telephony.cdma;
import static android.Manifest.permission.READ_PHONE_STATE;
import static com.android.internal.telephony.TelephonyProperties.PROPERTY_ICC_OPERATOR_ISO_COUNTRY;
import static com.android.internal.telephony.TelephonyProperties.PROPERTY_ICC_OPERATOR_ISO_COUNTRY_2;
import static com.android.internal.telephony.TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC;
import static com.android.internal.telephony.TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC_2; 
import static com.android.internal.telephony.TelephonyProperties.PROPERTY_ICC_OPERATOR_DEFAULT_NAME;
import static com.android.internal.telephony.TelephonyProperties.PROPERTY_ICC_OPERATOR_DEFAULT_NAME_2;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.SystemProperties;
import android.util.Log;

import com.android.internal.telephony.AdnRecord;
import com.android.internal.telephony.AdnRecordCache;
import com.android.internal.telephony.AdnRecordLoader;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.telephony.cdma.RuimCard;
import com.android.internal.telephony.MccTable;

// can't be used since VoiceMailConstants is not public
//import com.android.internal.telephony.gsm.VoiceMailConstants;
import com.android.internal.telephony.IccException;
import com.android.internal.telephony.IccRecords;
import com.android.internal.telephony.IccUtils;
import com.android.internal.telephony.PhoneProxy;

import com.mediatek.featureoption.FeatureOption;
import android.app.ActivityManagerNative;
import com.android.internal.telephony.RIL;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Telephony.SIMInfo;
import android.provider.Telephony.SimInfo;
import android.content.ContentUris;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.InternationalCardUtil;

//via support start
import android.text.TextUtils;
import android.os.SystemProperties;
import com.android.internal.telephony.IccCard;
//via support end
/**
 * {@hide}
 */
public final class RuimRecords extends IccRecords {
    static final String LOG_TAG = "CDMA";

    private static final boolean DBG = true;
    private boolean  m_ota_commited=false;

    // ***** Instance Variables

    private String mImsi;
    private String mMyMobileNumber;
    private String mMin2Min1;

    private String mPrlVersion;

    public String mGSM_Imsi;
	
    boolean bEccRequired = false;
    // ***** Event Constants

    private static final int EVENT_RUIM_READY = 1;
    private static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE = 2;
    private static final int EVENT_GET_IMSI_DONE = 3;
    private static final int EVENT_GET_DEVICE_IDENTITY_DONE = 4;
    private static final int EVENT_GET_ICCID_DONE = 5;
    private static final int EVENT_GET_IMSI_RETRY = 6;
    private static final int EVENT_GET_CDMA_SUBSCRIPTION_DONE = 10;
    private static final int EVENT_UPDATE_DONE = 14;
    private static final int EVENT_GET_SST_DONE = 17;
    private static final int EVENT_GET_ALL_SMS_DONE = 18;
    private static final int EVENT_MARK_SMS_READ_DONE = 19;

    private static final int EVENT_SMS_ON_RUIM = 21;
    private static final int EVENT_GET_SMS_DONE = 22;

    private static final int EVENT_RUIM_REFRESH = 31;

    //via support start
    private static final int EVENT_RADIO_STATE_CHANGED = 101;
    private static final String PROPERTY_ICC_CDMA_OPERATOR_MCC = "cdma.icc.operator.mcc";
    //via support end	

//MTK-START [mtk04258][20120806][International card support]
    private static final int EVENT_GET_GSM_IMSI_DONE = 41;
//MTK-END [mtk04258][20120806][International card support]

    RuimRecords(CDMAPhone p) {
        super(p);

        adnCache = new AdnRecordCache(phone);

        recordsRequested = false;  // No load request is made till SIM ready

        // recordsToLoad is set to 0 because no requests are made yet
        recordsToLoad = 0;


        p.mCM.registerForRUIMReady(this, EVENT_RUIM_READY, null);
        p.mCM.registerForOffOrNotAvailable(this, EVENT_RADIO_OFF_OR_NOT_AVAILABLE, null);
        // NOTE the EVENT_SMS_ON_RUIM is not registered
        p.mCM.registerForIccRefresh(this, EVENT_RUIM_REFRESH, null);

	//via support start
	p.mCM.registerForRadioStateChanged(this, EVENT_RADIO_STATE_CHANGED, null);
	//via support end

        // Start off by setting empty state
        onRadioOffOrNotAvailable();

    }

    @Override
    public void dispose() {
        //Unregister for all events
        phone.mCM.unregisterForRUIMReady(this);
        phone.mCM.unregisterForOffOrNotAvailable( this);
        phone.mCM.unregisterForIccRefresh(this);
        phone.mCM.unregisterForRadioStateChanged(this);
    }

    @Override
    protected void finalize() {
        if(DBG) Log.d(LOG_TAG, "RuimRecords finalized");
    }

    @Override
    protected void onRadioOffOrNotAvailable() {
        countVoiceMessages = 0;
        mncLength = UNINITIALIZED;
        iccid = null;

        adnCache.reset();

        // Don't clean up PROPERTY_ICC_OPERATOR_ISO_COUNTRY and
        // PROPERTY_ICC_OPERATOR_NUMERIC here. Since not all CDMA
        // devices have RUIM, these properties should keep the original
        // values, e.g. build time settings, when there is no RUIM but
        // set new values when RUIM is available and loaded.

        // recordsRequested is set to false indicating that the SIM
        // read requests made so far are not valid. This is set to
        // true only when fresh set of read requests are made.
        recordsRequested = false;
    }

    public String getMdnNumber() {
        return mMyMobileNumber;
    }

    public String getCdmaMin() {
         return mMin2Min1;
    }

    /** Returns null if RUIM is not yet ready */
    public String getPrlVersion() {
        return mPrlVersion;
    }

    @Override
    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete){
        // In CDMA this is Operator/OEM dependent
        AsyncResult.forMessage((onComplete)).exception =
                new IccException("setVoiceMailNumber not implemented");
        onComplete.sendToTarget();
        Log.e(LOG_TAG, "method setVoiceMailNumber is not implemented");
    }

    /**
     * Called by CCAT Service when REFRESH is received.
     * @param fileChanged indicates whether any files changed
     * @param fileList if non-null, a list of EF files that changed
     */
    @Override
    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            // A future optimization would be to inspect fileList and
            // only reload those files that we care about.  For now,
            // just re-fetch all RUIM records that we cache.
            fetchRuimRecords();
        }
    }

    /**
     * Returns the 5 or 6 digit MCC/MNC of the operator that
     *  provided the RUIM card. Returns null of RUIM is not yet ready
     */
    @Override
    public String getOperatorNumeric() {
        if (mImsi == null) {
            return null;
        }

        if (mncLength != UNINITIALIZED && mncLength != UNKNOWN) {
            // Length = length of MCC + length of MNC
            // length of mcc = 3 (3GPP2 C.S0005 - Section 2.3)
            return mImsi.substring(0, 3 + mncLength);
        }

        // Guess the MNC length based on the MCC if we don't
        // have a valid value in ef[ad]

        int mcc = Integer.parseInt(mImsi.substring(0,3));
        return mImsi.substring(0, 3 + MccTable.smallestDigitsMccForMnc(mcc));
    }

    @Override
    public void handleMessage(Message msg) {
        AsyncResult ar;

        byte data[];

        boolean isRecordLoadResponse = false;

        try { switch (msg.what) {
            case EVENT_RUIM_READY:
                onRuimReady();
	        if (DBG) log("handleMessage (EVENT_RUIM_READY)");
		if(bEccRequired == false) {
                    fetchEccList();
                }

            //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
                // add utk from 2.3
                if(FeatureOption.EVDO_DT_VIA_SUPPORT) {
                    Log.d(LOG_TAG, "phone.mCM.profileDownload");
                    phone.mCM.profileDownload("", null);         
                }
            //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

            break;

            case EVENT_GET_IMSI_RETRY:
                Log.d(LOG_TAG, "Event EVENT_GET_IMSI_RETRY Received, to get through ril");
                phone.mCM.getIMSI(obtainMessage(EVENT_GET_IMSI_DONE));
            break;
            case EVENT_RADIO_OFF_OR_NOT_AVAILABLE:
                onRadioOffOrNotAvailable();
            break;
		
            case EVENT_RADIO_STATE_CHANGED:
                if (DBG) log("handleMessage (EVENT_RADIO_STATE_CHANGED)");  
                if(bEccRequired == false) {
                    Log.d(LOG_TAG, "yangli Event EVENT_RADIO_STATE_CHANGED.getIccCard().getIccCardState() = " + phone.getIccCard().getIccCardState());
                    if (phone.getIccCard().getIccCardState() != IccCard.State.ABSENT
                        && phone.getIccCard().getIccCardState() != IccCard.State.UNKNOWN
                        && phone.getIccCard().getIccCardState() != IccCard.State.NOT_READY) {
                        fetchEccList();
                    }
                }
                break;

            case EVENT_GET_DEVICE_IDENTITY_DONE:
                Log.d(LOG_TAG, "Event EVENT_GET_DEVICE_IDENTITY_DONE Received");
            break;

            /* IO events */
            case EVENT_GET_IMSI_DONE:
                isRecordLoadResponse = true;

                ar = (AsyncResult)msg.obj;
                if (ar.exception != null) {
                    Log.e(LOG_TAG, "Exception querying IMSI, Exception:" + ar.exception);
                    break;
                }

                mImsi = (String) ar.result;

                // IMSI (MCC+MNC+MSIN) is at least 6 digits, but not more
                // than 15 (and usually 15).
                if (mImsi != null && (mImsi.length() < 6 || mImsi.length() > 15)) {
                    Log.e(LOG_TAG, "invalid IMSI " + mImsi);
                    mImsi = null;
                }

                Log.d(LOG_TAG, "IMSI: " + mImsi.substring(0, 6) + "xxxxxxxxx");

                if (mImsi != null && mImsi.equals("111111110000000")) {
                    Log.d(LOG_TAG, "it is a default IMSI from via modemm, must to retry");
                    Log.d(LOG_TAG, "IMSI: " + mImsi);
                    mImsi = null;
                }
                String operatorNumeric = getOperatorNumeric();
                if (operatorNumeric != null) {
                    if(operatorNumeric.length() <= 6){
                        MccTable.updateMccMncConfiguration(phone, operatorNumeric);
                    }
                }
                if (mImsi == null) {
                    isRecordLoadResponse = false;
                    getIMSIDelay(3000);
                }
            break;

            case EVENT_GET_CDMA_SUBSCRIPTION_DONE:
                ar = (AsyncResult)msg.obj;
                String localTemp[] = (String[])ar.result;
                if (ar.exception != null) {
                    break;
                }

                mMyMobileNumber = localTemp[0];
                mMin2Min1 = localTemp[3];
                mPrlVersion = localTemp[4];

                Log.d(LOG_TAG, "MDN: " + mMyMobileNumber + " MIN: " + mMin2Min1);

            break;

            case EVENT_GET_ICCID_DONE:
                isRecordLoadResponse = true;

                ar = (AsyncResult)msg.obj;
                data = (byte[])ar.result;

                if (ar.exception != null) {
                    break;
                }

                iccid = IccUtils.bcdToString(data, 0, data.length);

                Log.d(LOG_TAG, "iccid: " + iccid);

            break;

            case EVENT_UPDATE_DONE:
                ar = (AsyncResult)msg.obj;
                if (ar.exception != null) {
                    Log.i(LOG_TAG, "RuimRecords update failed", ar.exception);
                }
            break;

            case EVENT_GET_ALL_SMS_DONE:
            case EVENT_MARK_SMS_READ_DONE:
            case EVENT_SMS_ON_RUIM:
            case EVENT_GET_SMS_DONE:
                Log.w(LOG_TAG, "Event not supported: " + msg.what);
                break;

            // TODO: probably EF_CST should be read instead
            case EVENT_GET_SST_DONE:
                Log.d(LOG_TAG, "Event EVENT_GET_SST_DONE Received");
            break;

            case EVENT_RUIM_REFRESH:
                isRecordLoadResponse = false;
                ar = (AsyncResult)msg.obj;
                if (ar.exception == null) {
                    //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
                    // add utk from 2.3
                    if(FeatureOption.EVDO_DT_VIA_SUPPORT) {
                        //do nothing
                    } else{
                    handleRuimRefresh((int[])(ar.result));
                }
                }
                //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK 
                break;
//MTK-START [mtk04258][20120806][International card support]
            case EVENT_GET_GSM_IMSI_DONE:
				Log.i(LOG_TAG, "EVENT_GET_GSM_IMSI_DONE");
                isRecordLoadResponse = true;

                ar = (AsyncResult)msg.obj;
                data = (byte[])ar.result;

                if (ar.exception != null) {
                    break;
                }

                mGSM_Imsi = IccUtils.bcdToString(data, 1, data[0]);
                // Align to last number with langth 15
                mGSM_Imsi = mGSM_Imsi.substring(mGSM_Imsi.length() - 15,
                mGSM_Imsi.length());
                InternationalCardUtil.updateCardType(phone.getContext(), 
                                                     phone.getMySimId(), 
                                                     mImsi, mGSM_Imsi);
                Log.d(LOG_TAG, "mGSM_Imsi: " + mGSM_Imsi);

            break;
//MTK-END [mtk04258][20120806][International card support]

        }}catch (RuntimeException exc) {
            // I don't want these exceptions to be fatal
            Log.w(LOG_TAG, "Exception parsing RUIM record", exc);
        } finally {
            // Count up record load responses even if they are fails
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        }
    }

    @Override
    protected void onRecordLoaded() {
        // One record loaded successfully or failed, In either case
        // we need to update the recordsToLoad count
        recordsToLoad -= 1;

        if (recordsToLoad == 0 && recordsRequested == true) {
            onAllRecordsLoaded();
        } else if (recordsToLoad < 0) {
            Log.e(LOG_TAG, "RuimRecords: recordsToLoad <0, programmer error suspected");
            recordsToLoad = 0;
        }
    }

    //via support start
    private void fetchEccList() {
        if (DBG) log("fetchEccList()"); 
        bEccRequired = true;
        SystemProperties.set("ro.ril.ecclist", "112,911");
        String numbers = SystemProperties.get("ro.ril.ecclist.cdma");
        if (DBG) log("fetchEccList from ro.ril.cdma.ecclist" + numbers); 
        if (!TextUtils.isEmpty(numbers)) { 
            if (Phone.GEMINI_SIM_2 == phone.getMySimId()) {
                SystemProperties.set("ril.ecclist2", numbers);
            } else {
                SystemProperties.set("ril.ecclist", numbers);
            }
	}

	numbers = SystemProperties.get("ril.ecclist2");
	if (DBG) log("fetchEccList from ro.ril.cdma.ecclist after write " + numbers); 
    }
    //via support end

    @Override
    protected void onAllRecordsLoaded() {
        Log.d(LOG_TAG, "RuimRecords: record load complete");

        // Further records that can be inserted are Operator/OEM dependent

        String operator = getOperatorNumeric();
        Log.d(LOG_TAG, "RUIMRecords: getOperatorNumeric operator = " + operator);

        if(FeatureOption.EVDO_DT_VIA_SUPPORT) {
        if (phone.getMySimId()  == Phone.GEMINI_SIM_1) {
               phone.setSystemProperty(PROPERTY_ICC_OPERATOR_NUMERIC, operator);
           } else {
               phone.setSystemProperty(PROPERTY_ICC_OPERATOR_NUMERIC_2, operator);
           }
        } else {
            SystemProperties.set(PROPERTY_ICC_OPERATOR_NUMERIC, operator);
        }

        if (mImsi != null) {
            String countryCode;
            try {
                //via support start, set cdma operator MCC value
                SystemProperties.set(PROPERTY_ICC_CDMA_OPERATOR_MCC, mImsi.substring(0,3));
                //via support end

                countryCode = 
                    MccTable.countryCodeForMcc(Integer.parseInt(mImsi.substring(0,3)));
            } catch(NumberFormatException e) {
                countryCode = null;
                Log.e(LOG_TAG, "RUIMRecords: Corrupt IMSI!");
        }
     
            if(FeatureOption.EVDO_DT_VIA_SUPPORT) {
                if (phone.getMySimId() == Phone.GEMINI_SIM_1) {            
                    phone.setSystemProperty(PROPERTY_ICC_OPERATOR_ISO_COUNTRY, countryCode);
                } else {
                    phone.setSystemProperty(PROPERTY_ICC_OPERATOR_ISO_COUNTRY_2, countryCode);
                }
            } else {
                 SystemProperties.set(PROPERTY_ICC_OPERATOR_ISO_COUNTRY, countryCode);
            }

            Log.d(LOG_TAG, "RUIMRecords: countryCode = " + countryCode);
        }
        else {
            Log.e("SIM", "[RUIMRecords] onAllRecordsLoaded: mImsi is NULL!");
        }

        recordsLoadedRegistrants.notifyRegistrants(
            new AsyncResult(null, null, null));
        if (phone.mIccCard != null)
        phone.mIccCard.broadcastIccStateChangedIntent(
                RuimCard.INTENT_VALUE_ICC_LOADED, null);

        Log.d("RUIM", "[RUIMRecords] sim id = " + phone.getMySimId() + " mImsi = " + mImsi + " operator = " + operator);

        if(operator != null) {
            String name = null; 
            name = ((RIL)(phone.mCM)).lookupOperatorName(operator, true);
            if(FeatureOption.EVDO_DT_VIA_SUPPORT) {
                if (phone.getMySimId() == Phone.GEMINI_SIM_1) {
                    phone.setSystemProperty(PROPERTY_ICC_OPERATOR_DEFAULT_NAME, name);
                } else {
                    phone.setSystemProperty(PROPERTY_ICC_OPERATOR_DEFAULT_NAME_2, name);
                }
            } else {
                phone.setSystemProperty(PROPERTY_ICC_OPERATOR_DEFAULT_NAME, name);
            }
            Log.d(LOG_TAG, "RUIMRecords: setDefaultNameForNewSIM name = " + name);
            setDefaultNameForNewSIM(name);
        } else {
            setDefaultNameForNewSIM(null);
        }
    }

    public void setDefaultNameForNewSIM(String strName){
        SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(phone.getContext(), phone.getMySimId());
        if (simInfo!= null) {
            String simDisplayName = simInfo.mDisplayName;
            Log.d(LOG_TAG, "[RUIMRECORDS]setDefaultNameForNewSIM SimInfo simDisplayName is " + simDisplayName);
            if (simDisplayName == null) {
                SIMInfo.setDefaultName(phone.getContext(),simInfo.mSimId, strName);
                Log.d(LOG_TAG, "[RUIMRECORDS]setDefaultNameForNewSIM SimInfo simId is " + simInfo.mSimId + " newName is " + strName);
                broadCastSetDefaultNameDone();
            }
        }
    }

    public void broadCastSetDefaultNameDone(){
        Intent intent = new Intent("android.intent.action.SIM_NAME_UPDATE");
        intent.putExtra(Phone.GEMINI_SIM_ID_KEY, phone.getMySimId());        
        ActivityManagerNative.broadcastStickyIntent(intent, READ_PHONE_STATE);
        Log.d(LOG_TAG,"[RUIMRECORDS]broadCast intent ACTION_SIM_NAME_UPDATE for sim " + phone.getMySimId());
    }

    private void onRuimReady() {
        /* broadcast intent ICC_READY here so that we can make sure
          READY is sent before IMSI ready
        */

        phone.mIccCard.broadcastIccStateChangedIntent(
                RuimCard.INTENT_VALUE_ICC_READY, null);

        fetchRuimRecords();

        phone.mCM.getCDMASubscription(obtainMessage(EVENT_GET_CDMA_SUBSCRIPTION_DONE));

    }

    private void getIMSIDelay(long delayMillis) {
        Log.d(LOG_TAG, "to getIMSIDelay, delayMillis = " + delayMillis);
        sendEmptyMessageDelayed(EVENT_GET_IMSI_RETRY, delayMillis);
    }

    private void fetchRuimRecords() {
        recordsRequested = true;

        Log.v(LOG_TAG, "RuimRecords:fetchRuimRecords " + recordsToLoad);

        phone.mCM.getIMSI(obtainMessage(EVENT_GET_IMSI_DONE));
        recordsToLoad++;

        phone.getIccFileHandler().loadEFTransparent(EF_ICCID,
                obtainMessage(EVENT_GET_ICCID_DONE));
        recordsToLoad++;

//MTK-START [mtk04258][20120806][International card support]
        if (FeatureOption.EVDO_DT_SUPPORT) {
            phone.getIccFileHandler().loadEFTransparent(EF_IMSI,
                    obtainMessage(EVENT_GET_GSM_IMSI_DONE));
            recordsToLoad++;
        }
//MTK-END [mtk04258][20120806][International card support]

        // Further records that can be inserted are Operator/OEM dependent
    }

    /**
     * {@inheritDoc}
     *
     * No Display rule for RUIMs yet.
     */
    @Override
    public int getDisplayRule(String plmn) {
        // TODO together with spn
        return 0;
    }

    @Override
    public void setVoiceMessageWaiting(int line, int countWaiting) {
        if (line != 1) {
            // only profile 1 is supported
            return;
        }

        // range check
        if (countWaiting < 0) {
            countWaiting = -1;
        } else if (countWaiting > 0xff) {
            // C.S0015-B v2, 4.5.12
            // range: 0-99
            countWaiting = 0xff;
        }
        countVoiceMessages = countWaiting;

        ((CDMAPhone) phone).notifyMessageWaitingIndicator();
    }

    //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
    // add utk from 2.3
    public void handleRuimRefresh(int type) {
        log("handleRuimRefresh type:"+type);
        switch(type){
            case CommandsInterface.REFRESH_NAA_INIT_AND_FULL_FILE_CHANGE:
            case CommandsInterface.REFRESH_NAA_FILE_CHANGE:
            case CommandsInterface.REFRESH_NAA_INIT_AND_FILE_CHANGE:
            case CommandsInterface.REFRESH_NAA_INIT:
                log("Refresh with NAA file change");
                adnCache.reset();
                break;
            case CommandsInterface.REFRESH_UICC_RESET:
                log("Refresh with UICC reset 0x04");               
                break;
            default:
                log("Refresh with unknown operation");
                break;
        }
    }
    //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

    private void handleRuimRefresh(int[] result) {
        if (result == null || result.length == 0) {
            if (DBG) log("handleRuimRefresh without input");
            return;
        }

        switch ((result[0])) {
            case CommandsInterface.SIM_REFRESH_FILE_UPDATED:
                if (DBG) log("handleRuimRefresh with SIM_REFRESH_FILE_UPDATED");
                adnCache.reset();
                fetchRuimRecords();
                break;
            case CommandsInterface.SIM_REFRESH_INIT:
                if (DBG) log("handleRuimRefresh with SIM_REFRESH_INIT");
                // need to reload all files (that we care about)
                fetchRuimRecords();
                break;
            case CommandsInterface.SIM_REFRESH_RESET:
                if (DBG) log("handleRuimRefresh with SIM_REFRESH_RESET");
                phone.mCM.setRadioPower(false, null);
                /* Note: no need to call setRadioPower(true).  Assuming the desired
                * radio power state is still ON (as tracked by ServiceStateTracker),
                * ServiceStateTracker will call setRadioPower when it receives the
                * RADIO_STATE_CHANGED notification for the power off.  And if the
                * desired power state has changed in the interim, we don't want to
                * override it with an unconditional power on.
                */
                break;
            default:
                // unknown refresh operation
                if (DBG) log("handleRuimRefresh with unknown operation");
                break;
        }
    }

    @Override
    protected void log(String s) {
        Log.d(LOG_TAG, "[RuimRecords] " + s);
    }

    @Override
    protected void loge(String s) {
        Log.e(LOG_TAG, "[RuimRecords] " + s);
    }
}
