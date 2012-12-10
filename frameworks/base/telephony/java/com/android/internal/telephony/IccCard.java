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

package com.android.internal.telephony;

import static android.Manifest.permission.READ_PHONE_STATE;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Power;
import android.os.PowerManager;
import android.os.Registrant;
import android.os.RegistrantList;
import android.util.Log;
import android.view.WindowManager;

import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.gsm.SIMRecords;
//MTK-START [mtk80601][111215][ALPS00093395]
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.*;
import android.os.SystemProperties;
import android.provider.Telephony.SIMInfo;
import com.android.internal.R;
import com.mediatek.featureoption.FeatureOption;
import android.content.SharedPreferences;
import android.content.Context;
import android.provider.Settings;
import com.android.internal.telephony.gsm.UsimServiceTable;
//MTK-END

/**
 * {@hide}
 */
public abstract class IccCard {
    protected String mLogTag;
    protected boolean mDbg;

//MTK-START [mtk80601][111215][ALPS00093395]
    static final String PROPERTY_RIL_UICC_TYPE  = "gsm.ril.uicctype";
    static final String PROPERTY_RIL_UICC2_TYPE = "gsm.ril.uicctype.2";
    static final String PROPERTY_RIL_PHB_READY = "gsm.sim.ril.phbready";
    static final String PROPERTY_RIL_PHB_READY_2 = "gsm.sim.ril.phbready.2";	
    protected String mIccType = null; /* Add for USIM detect */
//MTK-END [mtk80601][111215][ALPS00093395]
    private IccCardStatus mIccCardStatus = null;
    protected State mState = null;
    protected PhoneBase mPhone;
    private RegistrantList mAbsentRegistrants = new RegistrantList();
    private RegistrantList mRecoveryRegistrants = new RegistrantList();
    private RegistrantList mPinLockedRegistrants = new RegistrantList();
    private RegistrantList mNetworkLockedRegistrants = new RegistrantList();

    private boolean mDesiredPinLocked;
    private boolean mDesiredFdnEnabled;
    private boolean mIccPinLocked = true; // Default to locked
    private boolean mIccFdnEnabled = false; // Default to disabled.
                                            // Will be updated when SIM_READY.
//MTK-START [mtk80601][111215][ALPS00093395]
    private boolean mPhbReady = false;
    private String mIccId = null;
    //static final int NOTIFY_SIM_INSERT_STATE = 10000;  // Id to update notification about no_sim_inserted
//MTK-END [mtk80601][111215][ALPS00093395]


    /* The extra data for broacasting intent INTENT_ICC_STATE_CHANGE */
    static public final String INTENT_KEY_ICC_STATE = "ss";
    /* NOT_READY means the ICC interface is not ready (eg, radio is off or powering on) */
    static public final String INTENT_VALUE_ICC_NOT_READY = "NOT_READY";
    /* ABSENT means ICC is missing */
    static public final String INTENT_VALUE_ICC_ABSENT = "ABSENT";
    /* LOCKED means ICC is locked by pin or by network */
    static public final String INTENT_VALUE_ICC_LOCKED = "LOCKED";
    /* READY means ICC is ready to access */
    static public final String INTENT_VALUE_ICC_READY = "READY";
    /* IMSI means ICC IMSI is ready in property */
    static public final String INTENT_VALUE_ICC_IMSI = "IMSI";
    /* LOADED means all ICC records, including IMSI, are loaded */
    static public final String INTENT_VALUE_ICC_LOADED = "LOADED";
    /* The extra data for broacasting intent INTENT_ICC_STATE_CHANGE */
    static public final String INTENT_KEY_LOCKED_REASON = "reason";
    /* PIN means ICC is locked on PIN1 */
    static public final String INTENT_VALUE_LOCKED_ON_PIN = "PIN";
    /* PUK means ICC is locked on PUK1 */
    static public final String INTENT_VALUE_LOCKED_ON_PUK = "PUK";
    /* NETWORK means ICC is locked on NETWORK PERSONALIZATION */
    static public final String INTENT_VALUE_LOCKED_NETWORK = "NETWORK";
    /* PERM_DISABLED means ICC is permanently disabled due to puk fails */
    static public final String INTENT_VALUE_ABSENT_ON_PERM_DISABLED = "PERM_DISABLED";


    protected static final int EVENT_ICC_LOCKED_OR_ABSENT = 1;
    private static final int EVENT_GET_ICC_STATUS_DONE = 2;
    protected static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE = 3;
    private static final int EVENT_PINPUK_DONE = 4;
    private static final int EVENT_REPOLL_STATUS_DONE = 5;
    protected static final int EVENT_ICC_READY = 6;
    private static final int EVENT_QUERY_FACILITY_LOCK_DONE = 7;
    private static final int EVENT_CHANGE_FACILITY_LOCK_DONE = 8;
    private static final int EVENT_CHANGE_ICC_PASSWORD_DONE = 9;
    private static final int EVENT_QUERY_FACILITY_FDN_DONE = 10;
    private static final int EVENT_CHANGE_FACILITY_FDN_DONE = 11;
    private static final int EVENT_ICC_STATUS_CHANGED = 12;
    private static final int EVENT_CARD_REMOVED = 13;
    private static final int EVENT_CARD_ADDED = 14;
    // NFC SEEK start
    private static final int EVENT_EXCHANGE_APDU_DONE = 15;
    private static final int EVENT_OPEN_CHANNEL_DONE = 16;
    private static final int EVENT_CLOSE_CHANNEL_DONE = 17;
    private static final int EVENT_SIM_IO_DONE = 18;
    private static final int EVENT_GET_ATR_DONE = 19;
    // NFC SEEK end
//MTK-START [mtk80601][111215][ALPS00093395]
    private static final int EVENT_QUERY_NETWORK_LOCK_DONE = 101;
    private static final int EVENT_CHANGE_NETWORK_LOCK_DONE = 102;
    private static final int EVENT_QUERY_ICCID_DONE = 103;
    protected static final int EVENT_PHB_READY = 104;
    protected static final int EVENT_SIM_MISSING = 105;
    protected static final int EVENT_SIM_RECOVERY = 106;
    protected static final int EVENT_VIRTUAL_SIM_ON = 107;
    protected static final int EVENT_VIRTUAL_SIM_OFF = 108;
    
    private boolean mSIMInfoReady = false;
    
//MTK-END [mtk80601][111215][ALPS00093395]

    //VIA support started
    protected static final int EVENT_UIM_INSERT_STATUS = 201;
   //VIA support ended

    /*
      UNKNOWN is a transient state, for example, after uesr inputs ICC pin under
      PIN_REQUIRED state, the query for ICC status returns UNKNOWN before it
      turns to READY
     */
    public enum State {
        UNKNOWN,
        ABSENT,
        PIN_REQUIRED,
        PUK_REQUIRED,
        NETWORK_LOCKED,
        READY,
        NOT_READY,
        PERM_DISABLED;

        public boolean isPinLocked() {
            return ((this == PIN_REQUIRED) || (this == PUK_REQUIRED));
        }
//MTK-START [mtk80601][111215][ALPS00093395]
        public boolean isLocked() {
            return (this.isPinLocked() || (this == NETWORK_LOCKED));
        }
//MTK-END [mtk80601][111215][ALPS00093395]

        public boolean iccCardExist() {
            return ((this == PIN_REQUIRED) || (this == PUK_REQUIRED)
                    || (this == NETWORK_LOCKED) || (this == READY)
                    || (this == PERM_DISABLED));
        }
    }

    public State getState() {
        if (mState == null) {
            switch(mPhone.mCM.getRadioState()) {
                /* This switch block must not return anything in
                 * State.isLocked() or State.ABSENT.
                 * If it does, handleSimStatus() may break
                 */
                case RADIO_OFF:
                case RADIO_UNAVAILABLE:
                case SIM_NOT_READY:
                case RUIM_NOT_READY:
                    if (mDbg) log("getState(): radio state is off or unavailable or not ready");
                    return State.UNKNOWN;
                case SIM_LOCKED_OR_ABSENT:
                case RUIM_LOCKED_OR_ABSENT:
                    //this should be transient-only
                    if (mDbg) log("getState():radio state is locked or absent");
                    return State.UNKNOWN;
                case SIM_READY:
                case RUIM_READY:
                case NV_READY:
                    if (mDbg) log("getState():radio state is ready");
                    return State.READY;
                case NV_NOT_READY:
                    if (mDbg) log("getState(): radio state is not ready");
                    return State.ABSENT;
            }
        } else {
            if (mDbg) log("getState(): mState = " + mState);
            return mState;
        }

        Log.e(mLogTag, "IccCard.getState(): case should never be reached");
        return State.UNKNOWN;
    }

    public IccCard(PhoneBase phone, String logTag, Boolean dbg) {
        mPhone = phone;
        mPhone.mCM.registerForIccStatusChanged(mHandler, EVENT_ICC_STATUS_CHANGED, null);
        mLogTag = logTag;
        mDbg = dbg;
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
        phone.getContext().registerReceiver(mReceiver, filter);
    }

    public void dispose() {
        mPhone.mCM.unregisterForIccStatusChanged(mHandler);
    }

    protected void finalize() {
        if(mDbg) Log.d(mLogTag, "IccCard finalized");
    }

    /**
     * Notifies handler of any transition into State.ABSENT
     */
    public void registerForAbsent(Handler h, int what, Object obj) {
        Registrant r = new Registrant (h, what, obj);

        mAbsentRegistrants.add(r);

        if (getState() == State.ABSENT) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForAbsent(Handler h) {
        mAbsentRegistrants.remove(h);
    }

    public void registerForRecovery(Handler h, int what, Object obj) {
        Registrant r = new Registrant (h, what, obj);

        mRecoveryRegistrants.add(r);

        if (getState() == State.READY) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForRecovery(Handler h) {
        mRecoveryRegistrants.remove(h);
    }
    /**
     * Notifies handler of any transition into State.NETWORK_LOCKED
     */
    public void registerForNetworkLocked(Handler h, int what, Object obj) {
        Registrant r = new Registrant (h, what, obj);

        mNetworkLockedRegistrants.add(r);

        if (getState() == State.NETWORK_LOCKED) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForNetworkLocked(Handler h) {
        mNetworkLockedRegistrants.remove(h);
    }

    /**
     * Notifies handler of any transition into State.isPinLocked()
     */
    public void registerForLocked(Handler h, int what, Object obj) {
        Registrant r = new Registrant (h, what, obj);

        mPinLockedRegistrants.add(r);

        if (getState().isPinLocked()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForLocked(Handler h) {
        mPinLockedRegistrants.remove(h);
    }


    /**
     * Supply the ICC PIN to the ICC
     *
     * When the operation is complete, onComplete will be sent to its
     * Handler.
     *
     * onComplete.obj will be an AsyncResult
     *
     * ((AsyncResult)onComplete.obj).exception == null on success
     * ((AsyncResult)onComplete.obj).exception != null on fail
     *
     * If the supplied PIN is incorrect:
     * ((AsyncResult)onComplete.obj).exception != null
     * && ((AsyncResult)onComplete.obj).exception
     *       instanceof com.android.internal.telephony.gsm.CommandException)
     * && ((CommandException)(((AsyncResult)onComplete.obj).exception))
     *          .getCommandError() == CommandException.Error.PASSWORD_INCORRECT
     *
     *
     */

    public void supplyPin (String pin, Message onComplete) {
        mPhone.mCM.supplyIccPin(pin, mHandler.obtainMessage(EVENT_PINPUK_DONE, onComplete));
    }

    public void supplyPuk (String puk, String newPin, Message onComplete) {
        mPhone.mCM.supplyIccPuk(puk, newPin,
                mHandler.obtainMessage(EVENT_PINPUK_DONE, onComplete));
    }

    public void supplyPin2 (String pin2, Message onComplete) {
        mPhone.mCM.supplyIccPin2(pin2,
                mHandler.obtainMessage(EVENT_PINPUK_DONE, onComplete));
    }

    public void supplyPuk2 (String puk2, String newPin2, Message onComplete) {
        mPhone.mCM.supplyIccPuk2(puk2, newPin2,
                mHandler.obtainMessage(EVENT_PINPUK_DONE, onComplete));
    }

    public void supplyNetworkDepersonalization (String pin, Message onComplete) {
        if(mDbg) log("Network Despersonalization: " + pin);
        mPhone.mCM.supplyNetworkDepersonalization(pin,
                mHandler.obtainMessage(EVENT_PINPUK_DONE, onComplete));
    }

    /**
     * Check whether ICC pin lock is enabled
     * This is a sync call which returns the cached pin enabled state
     *
     * @return true for ICC locked enabled
     *         false for ICC locked disabled
     */
    public boolean getIccLockEnabled() {
        return mIccPinLocked;
     }

    /**
     * Check whether ICC fdn (fixed dialing number) is enabled
     * This is a sync call which returns the cached pin enabled state
     *
     * @return true for ICC fdn enabled
     *         false for ICC fdn disabled
     */
     public boolean getIccFdnEnabled() {
        return mIccFdnEnabled;
     }

     /**
      * Set the ICC pin lock enabled or disabled
      * When the operation is complete, onComplete will be sent to its handler
      *
      * @param enabled "true" for locked "false" for unlocked.
      * @param password needed to change the ICC pin state, aka. Pin1
      * @param onComplete
      *        onComplete.obj will be an AsyncResult
      *        ((AsyncResult)onComplete.obj).exception == null on success
      *        ((AsyncResult)onComplete.obj).exception != null on fail
      */
     public void setIccLockEnabled (boolean enabled,
             String password, Message onComplete) {
         int serviceClassX;
         serviceClassX = CommandsInterface.SERVICE_CLASS_VOICE +
                 CommandsInterface.SERVICE_CLASS_DATA +
                 CommandsInterface.SERVICE_CLASS_FAX;

         mDesiredPinLocked = enabled;

         mPhone.mCM.setFacilityLock(CommandsInterface.CB_FACILITY_BA_SIM,
                 enabled, password, serviceClassX,
                 mHandler.obtainMessage(EVENT_CHANGE_FACILITY_LOCK_DONE, onComplete));
     }

     /**
      * Set the ICC fdn enabled or disabled
      * When the operation is complete, onComplete will be sent to its handler
      *
      * @param enabled "true" for locked "false" for unlocked.
      * @param password needed to change the ICC fdn enable, aka Pin2
      * @param onComplete
      *        onComplete.obj will be an AsyncResult
      *        ((AsyncResult)onComplete.obj).exception == null on success
      *        ((AsyncResult)onComplete.obj).exception != null on fail
      */
     public void setIccFdnEnabled (boolean enabled,
             String password, Message onComplete) {
         int serviceClassX;
         serviceClassX = CommandsInterface.SERVICE_CLASS_VOICE +
                 CommandsInterface.SERVICE_CLASS_DATA +
                 CommandsInterface.SERVICE_CLASS_FAX +
                 CommandsInterface.SERVICE_CLASS_SMS;

         mDesiredFdnEnabled = enabled;

         mPhone.mCM.setFacilityLock(CommandsInterface.CB_FACILITY_BA_FD,
                 enabled, password, serviceClassX,
                 mHandler.obtainMessage(EVENT_CHANGE_FACILITY_FDN_DONE, onComplete));
     }

     /**
      * Change the ICC password used in ICC pin lock
      * When the operation is complete, onComplete will be sent to its handler
      *
      * @param oldPassword is the old password
      * @param newPassword is the new password
      * @param onComplete
      *        onComplete.obj will be an AsyncResult
      *        ((AsyncResult)onComplete.obj).exception == null on success
      *        ((AsyncResult)onComplete.obj).exception != null on fail
      */
     public void changeIccLockPassword(String oldPassword, String newPassword,
             Message onComplete) {
         if(mDbg) log("Change Pin1 old: " + oldPassword + " new: " + newPassword);
         mPhone.mCM.changeIccPin(oldPassword, newPassword,
                 mHandler.obtainMessage(EVENT_CHANGE_ICC_PASSWORD_DONE, onComplete));

     }

     /**
      * Change the ICC password used in ICC fdn enable
      * When the operation is complete, onComplete will be sent to its handler
      *
      * @param oldPassword is the old password
      * @param newPassword is the new password
      * @param onComplete
      *        onComplete.obj will be an AsyncResult
      *        ((AsyncResult)onComplete.obj).exception == null on success
      *        ((AsyncResult)onComplete.obj).exception != null on fail
      */
     public void changeIccFdnPassword(String oldPassword, String newPassword,
             Message onComplete) {
         if(mDbg) log("Change Pin2 old: " + oldPassword + " new: " + newPassword);
         mPhone.mCM.changeIccPin2(oldPassword, newPassword,
                 mHandler.obtainMessage(EVENT_CHANGE_ICC_PASSWORD_DONE, onComplete));

     }
//MTK-START [mtk80601][111215][ALPS00093395]
    /**
     * Check whether ICC network lock is enabled
     * This is an async call which returns lock state to applications directly
     */
    public void QueryIccNetworkLock (int category,
            int lockop, String password, String data_imsi, String gid1, String gid2, Message onComplete) {

        if (mDbg) log("QueryIccNetworkLock(): category =  " + category);

        switch(category){
            case CommandsInterface.CAT_NETWOEK:
            case CommandsInterface.CAT_NETOWRK_SUBSET:
            case CommandsInterface.CAT_CORPORATE:
            case CommandsInterface.CAT_SERVICE_PROVIDER:
            case CommandsInterface.CAT_SIM:
                mPhone.mCM.queryNetworkLock(category, mHandler.obtainMessage(EVENT_QUERY_NETWORK_LOCK_DONE, onComplete));
                break;
            default:
                Log.e(mLogTag, "QueryIccNetworkLock unknown category = " + category);
                break;
        }
    }

    /**
     * Set the ICC network lock enabled or disabled
     * When the operation is complete, onComplete will be sent to its handler
     */
    public void setIccNetworkLockEnabled (int category,
            int lockop, String password, String data_imsi, String gid1, String gid2, Message onComplete) {

        if (mDbg) log("SetIccNetworkEnabled(): category = " + category
            + " lockop = " + lockop + " password = " + password
            + " data_imsi = " + data_imsi + " gid1 = " + gid1 + " gid2 = " + gid2);

        switch(lockop) {
            case CommandsInterface.OP_REMOVE:
            case CommandsInterface.OP_ADD:
            case CommandsInterface.OP_LOCK:
            case CommandsInterface.OP_PERMANENT_UNLOCK:
            case CommandsInterface.OP_UNLOCK:
                mPhone.mCM.setNetworkLock(category, lockop, password, data_imsi, gid1, gid2, mHandler.obtainMessage(EVENT_CHANGE_NETWORK_LOCK_DONE, onComplete));
                break;
            default:
                Log.e(mLogTag, "SetIccNetworkEnabled unknown operation" + lockop);
                break;    
        }
    }

    /**
     * get IccId 
     * When the operation is complete, onComplete will be sent to its handler
     *
     * @param onComplete
     *        onComplete.obj will be an AsyncResult
     *        ((AsyncResult)onComplete.obj).exception == null on success
     *        ((AsyncResult)onComplete.obj).exception != null on fail
     */
    public void getIccId(Message onComplete) {
        if (mDbg) log("getIccId()");
        mPhone.mCM.queryIccId(mHandler.obtainMessage(EVENT_QUERY_ICCID_DONE, onComplete));	
    }
//MTK-END [mtk80601][111215][ALPS00093395]

    /**
     * Returns service provider name stored in ICC card.
     * If there is no service provider name associated or the record is not
     * yet available, null will be returned <p>
     *
     * Please use this value when display Service Provider Name in idle mode <p>
     *
     * Usage of this provider name in the UI is a common carrier requirement.
     *
     * Also available via Android property "gsm.sim.operator.alpha"
     *
     * @return Service Provider Name stored in ICC card
     *         null if no service provider name associated or the record is not
     *         yet available
     *
     */
    public abstract String getServiceProviderName();

    protected void updateStateProperty() {
//MTK-START [mtk80601][111215][ALPS00093395]
        if (mDbg) log("updateStateProperty(): state = " + getState().toString());
        if (Phone.GEMINI_SIM_2 != mPhone.getMySimId()) {
            SystemProperties.set(TelephonyProperties.PROPERTY_SIM_STATE, getState().toString());
        } else {
            SystemProperties.set(TelephonyProperties.PROPERTY_SIM_STATE_2, getState().toString());
        }
//MTK-END [mtk80601][111215][ALPS00093395]
    }
//MTK-START [mtk80601][111215][ALPS00093395]
    private void updatePhbStateProperty() {
        if (mDbg) log("updatePhbStateProperty(): mPhbReady = " + (mPhbReady ? "true" : "false"));
        if (Phone.GEMINI_SIM_2 != mPhone.getMySimId()) {
            SystemProperties.set(PROPERTY_RIL_PHB_READY, mPhbReady ? "true" : "false");
        } else {
            SystemProperties.set(PROPERTY_RIL_PHB_READY_2, mPhbReady ? "true" : "false");
        }        
    }
//MTK-END [mtk80601][111215][ALPS00093395]

    private void getIccCardStatusDone(AsyncResult ar) {
        if (ar.exception != null) {
            Log.e(mLogTag,"Error getting ICC status. "
                    + "RIL_REQUEST_GET_ICC_STATUS should "
                    + "never return an error", ar.exception);
            return;
        }
        handleIccCardStatus((IccCardStatus) ar.result);
    }

    private void handleIccCardStatus(IccCardStatus newCardStatus) {
        boolean transitionedIntoPinLocked;
        boolean transitionedIntoAbsent;
        boolean transitionedIntoNetworkLocked;
        boolean transitionedIntoPermBlocked;
        boolean isIccCardRemoved;
        boolean isIccCardAdded;

        State oldState, newState;

        oldState = mState;
        mIccCardStatus = newCardStatus;
        newState = getIccCardState();
        mState = newState;

        updateStateProperty();

        transitionedIntoPinLocked = (
                 (oldState != State.PIN_REQUIRED && newState == State.PIN_REQUIRED)
              || (oldState != State.PUK_REQUIRED && newState == State.PUK_REQUIRED));
        transitionedIntoAbsent = (oldState != State.ABSENT && newState == State.ABSENT);
        transitionedIntoNetworkLocked = (oldState != State.NETWORK_LOCKED
                && newState == State.NETWORK_LOCKED);
        transitionedIntoPermBlocked = (oldState != State.PERM_DISABLED
                && newState == State.PERM_DISABLED);
        isIccCardRemoved = (oldState != null &&
                        oldState.iccCardExist() && newState == State.ABSENT);
        isIccCardAdded = (oldState == State.ABSENT &&
                        newState != null && newState.iccCardExist());

        if (transitionedIntoPinLocked) {
            if (mDbg) log("Notify SIM pin or puk locked.");
            mPinLockedRegistrants.notifyRegistrants();
            broadcastIccStateChangedIntent(INTENT_VALUE_ICC_LOCKED,
                    (newState == State.PIN_REQUIRED) ?
                       INTENT_VALUE_LOCKED_ON_PIN : INTENT_VALUE_LOCKED_ON_PUK);
//MTK-START [mtk80601][111215][ALPS00093395]
            broadcastIccStateChangedExtendIntent(INTENT_VALUE_ICC_LOCKED,
                    (newState == State.PIN_REQUIRED) ?
                       INTENT_VALUE_LOCKED_ON_PIN : INTENT_VALUE_LOCKED_ON_PUK);
            mPhone.updateSimIndicateState();
//MTK-END [mtk80601][111215][ALPS00093395]
        } else if (transitionedIntoAbsent) {
            if (mDbg) log("Notify SIM missing.");
            mAbsentRegistrants.notifyRegistrants();
            broadcastIccStateChangedIntent(INTENT_VALUE_ICC_ABSENT, null);
            broadcastIccStateChangedExtendIntent(INTENT_VALUE_ICC_ABSENT, null);
        } else if (transitionedIntoNetworkLocked) {
            if (mDbg) log("Notify SIM network locked.");
            mNetworkLockedRegistrants.notifyRegistrants();
            broadcastIccStateChangedIntent(INTENT_VALUE_LOCKED_NETWORK,
                  INTENT_VALUE_LOCKED_NETWORK);
			broadcastIccStateChangedExtendIntent(INTENT_VALUE_LOCKED_NETWORK,
				  INTENT_VALUE_LOCKED_NETWORK);
            mPhone.updateSimIndicateState();

        } else if (transitionedIntoPermBlocked) {
            if (mDbg) log("Notify SIM permanently disabled.");
            broadcastIccStateChangedIntent(INTENT_VALUE_ICC_ABSENT,
                    INTENT_VALUE_ABSENT_ON_PERM_DISABLED);
        }

        if (isIccCardRemoved) {
            mHandler.sendMessage(mHandler.obtainMessage(EVENT_CARD_REMOVED, null));
        } else if (isIccCardAdded) {
            //mHandler.sendMessage(mHandler.obtainMessage(EVENT_CARD_ADDED, null));
            if (newState==State.READY) {
                broadcastIccStateChangedIntent(INTENT_VALUE_ICC_READY, INTENT_VALUE_ICC_READY);
                mPhone.updateSimIndicateState();
            } else if (newState==State.NOT_READY) {
                broadcastIccStateChangedIntent(INTENT_VALUE_ICC_NOT_READY,INTENT_VALUE_ICC_NOT_READY);
                mPhone.updateSimIndicateState();
            }
        }
    }

    private void onIccSwap(boolean isAdded) {
        // TODO: Here we assume the device can't handle SIM hot-swap
        //      and has to reboot. We may want to add a property,
        //      e.g. REBOOT_ON_SIM_SWAP, to indicate if modem support
        //      hot-swap.
        DialogInterface.OnClickListener listener = null;


        // TODO: SimRecords is not reset while SIM ABSENT (only reset while
        //       Radio_off_or_not_available). Have to reset in both both
        //       added or removed situation.
        listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (mDbg) log("Reboot due to SIM swap");
                    PowerManager pm = (PowerManager) mPhone.getContext()
                    .getSystemService(Context.POWER_SERVICE);
                    pm.reboot("SIM is added.");
                }
            }

        };

        Resources r = Resources.getSystem();

        String title = (isAdded) ? r.getString(R.string.sim_added_title) :
            r.getString(R.string.sim_removed_title);
        String message = (isAdded) ? r.getString(R.string.sim_added_message) :
            r.getString(R.string.sim_removed_message);
        String buttonTxt = r.getString(R.string.sim_restart_button);

        AlertDialog dialog = new AlertDialog.Builder(mPhone.getContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonTxt, listener)
            .create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    /**
     * Interperate EVENT_QUERY_FACILITY_LOCK_DONE
     * @param ar is asyncResult of Query_Facility_Locked
     */
    private void onQueryFdnEnabled(AsyncResult ar) {
        if(ar.exception != null) {
            if(mDbg) log("Error in querying facility lock:" + ar.exception);
            return;
        }

        int[] ints = (int[])ar.result;
        if(ints.length != 0) {
            mIccFdnEnabled = (0!=ints[0]);
            if(mDbg) log("Query facility lock : "  + mIccFdnEnabled);
        } else {
            Log.e(mLogTag, "[IccCard] Bogus facility lock response");
        }
    }

    /**
     * Interperate EVENT_QUERY_FACILITY_LOCK_DONE
     * @param ar is asyncResult of Query_Facility_Locked
     */
    private void onQueryFacilityLock(AsyncResult ar) {
        if(ar.exception != null) {
            if (mDbg) log("Error in querying facility lock:" + ar.exception);
            return;
        }

        int[] ints = (int[])ar.result;
        if(ints.length != 0) {
            mIccPinLocked = (0!=ints[0]);
            if(mDbg) log("Query facility lock : "  + mIccPinLocked);
        } else {
            Log.e(mLogTag, "[IccCard] Bogus facility lock response");
        }
    }

    public void broadcastIccStateChangedIntent(String value, String reason) {
        Intent intent = new Intent(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        // intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra(Phone.PHONE_NAME_KEY, mPhone.getPhoneName());
        intent.putExtra(INTENT_KEY_ICC_STATE, value);
        intent.putExtra(INTENT_KEY_LOCKED_REASON, reason);
        intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mPhone.getMySimId());
        if(mDbg) log("Broadcasting intent ACTION_SIM_STATE_CHANGED " +  value
                + " reason " + reason + " sim id " + mPhone.getMySimId());
        ActivityManagerNative.broadcastStickyIntent(intent, READ_PHONE_STATE);
    }
//MTK-START [mtk80601][111215][ALPS00093395]
    public void broadcastIccStateChangedExtendIntent(String value, String reason) {
        Intent intent = new Intent(TelephonyIntents.ACTION_SIM_STATE_CHANGED_EXTEND);
        // intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra(Phone.PHONE_NAME_KEY, mPhone.getPhoneName());
        intent.putExtra(INTENT_KEY_ICC_STATE, value);
        intent.putExtra(INTENT_KEY_LOCKED_REASON, reason);
        intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mPhone.getMySimId());
        if(mDbg) log("Broadcasting intent ACTION_SIM_STATE_CHANGED_EXTEND " +  value
                + " reason " + reason + " sim id " + mPhone.getMySimId());
        ActivityManagerNative.broadcastStickyIntent(intent, READ_PHONE_STATE);
    }


    public void broadcastRadioOffIntent() {
        Intent intent = new Intent(TelephonyIntents.ACTION_RADIO_OFF);
        intent.putExtra(Phone.PHONE_NAME_KEY, mPhone.getPhoneName());
        intent.putExtra(TelephonyIntents.INTENT_KEY_ICC_SLOT, mPhone.getMySimId());
        if(mDbg) log("Broadcasting intent ACTION_RADIO_OFF "  
                + " sim id " + mPhone.getMySimId());
        mPhone.getContext().sendBroadcast(intent);
    }

    public void broadcastPhbStateChangedIntent(boolean isReady) {
        log("broadcastPhbStateChangedIntent, mPhbReady " + mPhbReady + ", mSIMInfoReady " + mSIMInfoReady);
        if (mPhbReady && mSIMInfoReady) {
            Intent intent = new Intent(TelephonyIntents.ACTION_PHB_STATE_CHANGED);
            intent.putExtra("ready", isReady);
            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mPhone.getMySimId());
            if (mDbg) log("Broadcasting intent ACTION_PHB_STATE_CHANGED " + isReady
                        + " sim id " + mPhone.getMySimId());
            mPhone.getContext().sendBroadcast(intent);
        }
    }
    private void setNotificationVirtual(int notifyType){
        if(mDbg) log("setNotification(): notifyType = "+notifyType);
        Notification notification = new Notification();
        notification.when = System.currentTimeMillis();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.icon = com.android.internal.R.drawable.stat_sys_warning;
        Intent intent = new Intent();
        notification.contentIntent = PendingIntent.getActivity(mPhone.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String title = null;
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            if(mPhone.GEMINI_SIM_1 == mPhone.getMySimId() ){
                title = "Virtual SIM 1 ON";
            }else{
                title = "Virtual SIM 2 ON";
            }
        }else{
            title = "Virtual SIM ON";
        }
        CharSequence detail = "Virtual SIM ON";
        notification.tickerText = "Virtual SIM ON";
        notification.setLatestEventInfo(mPhone.getContext(), title, detail,notification.contentIntent);
        NotificationManager notificationManager = (NotificationManager)mPhone.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifyType + mPhone.getMySimId(), notification);
    }

    private void removeNotificationVirtual(int notifyType) {
        NotificationManager notificationManager = (NotificationManager)mPhone.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notifyType + mPhone.getMySimId());
    }

    private void setNotification(int notifyType){
        if(mDbg) log("setNotification(): notifyType = "+notifyType);
        Notification notification = new Notification();
        notification.when = System.currentTimeMillis();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.icon = com.android.internal.R.drawable.stat_sys_warning;
        Intent intent = new Intent();
        notification.contentIntent = PendingIntent.getActivity(mPhone.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String title = null;
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            if(mPhone.GEMINI_SIM_1 == mPhone.getMySimId() ){
                title = Resources.getSystem().getText(com.mediatek.internal.R.string.sim_missing_slot1).toString();
            }else{
                title = Resources.getSystem().getText(com.mediatek.internal.R.string.sim_missing_slot2).toString();
            }
        }else{
            title = Resources.getSystem().getText(com.mediatek.internal.R.string.sim_missing).toString();
        }
        CharSequence detail = mPhone.getContext().getText(com.mediatek.internal.R.string.sim_missing_detail);
        notification.tickerText = title;
        notification.setLatestEventInfo(mPhone.getContext(), title, detail,notification.contentIntent);
        NotificationManager notificationManager = (NotificationManager)mPhone.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifyType + mPhone.getMySimId(), notification);
    }

    // ALPS00294581
    public void disableSimMissingNotification() {
        NotificationManager notificationManager = (NotificationManager)mPhone.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(EVENT_SIM_MISSING + mPhone.getMySimId());
    }

//MTK-END [mtk80601][111215][ALPS00093395]
    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            AsyncResult ar;
            int serviceClassX;
            Log.d("IccCard", "receive message " + msg.what );
            serviceClassX = CommandsInterface.SERVICE_CLASS_VOICE +
                            CommandsInterface.SERVICE_CLASS_DATA +
                            CommandsInterface.SERVICE_CLASS_FAX;

            if (!mPhone.mIsTheCurrentActivePhone) {
                Log.e(mLogTag, "Received message " + msg + "[" + msg.what
                        + "] while being destroyed. Ignoring.");
                return;
            }

            switch (msg.what) {
                case EVENT_RADIO_OFF_OR_NOT_AVAILABLE:
                    if (mState != null && mState != State.UNKNOWN && mState != State.NOT_READY){
                        broadcastRadioOffIntent();
                    }
                    mState = null;
                    mIccType = null;
                    updateStateProperty();
                    broadcastIccStateChangedIntent(INTENT_VALUE_ICC_NOT_READY, null);
                    break;
                case EVENT_ICC_READY:
                    // set the state to Ready. SIM_STATE_CHANGED intent has been broadcast in SIMRecords.onSIMReady()
                    if (mDbg) log("handleMessage (EVENT_ICC_READY)");
                    mState = State.READY;
                    updateStateProperty();
                    mPhone.updateSimIndicateState();

                    //TODO: put facility read in SIM_READY now, maybe in REG_NW
                    mPhone.mCM.getIccCardStatus(obtainMessage(EVENT_GET_ICC_STATUS_DONE));
                    mPhone.mCM.queryFacilityLock (
                            CommandsInterface.CB_FACILITY_BA_SIM, "", serviceClassX,
                            obtainMessage(EVENT_QUERY_FACILITY_LOCK_DONE));
                    mPhone.mCM.queryFacilityLock (
                            CommandsInterface.CB_FACILITY_BA_FD, "", serviceClassX,
                            obtainMessage(EVENT_QUERY_FACILITY_FDN_DONE));
                    break;
                case EVENT_ICC_LOCKED_OR_ABSENT:
                    mPhone.mCM.getIccCardStatus(obtainMessage(EVENT_GET_ICC_STATUS_DONE));
                    mPhone.mCM.queryFacilityLock (
                            CommandsInterface.CB_FACILITY_BA_SIM, "", serviceClassX,
                            obtainMessage(EVENT_QUERY_FACILITY_LOCK_DONE));
                    break;
                case EVENT_GET_ICC_STATUS_DONE:
                    ar = (AsyncResult)msg.obj;

                    getIccCardStatusDone(ar);
                    break;
                case EVENT_PINPUK_DONE:
                    // a PIN/PUK/PIN2/PUK2/Network Personalization
                    // request has completed. ar.userObj is the response Message
                    // Repoll before returning
                    ar = (AsyncResult)msg.obj;
                    // TODO should abstract these exceptions
                    AsyncResult.forMessage(((Message)ar.userObj)).exception
                                                        = ar.exception;
                    mPhone.mCM.queryFacilityLock (
                            CommandsInterface.CB_FACILITY_BA_SIM, "", serviceClassX,
                            obtainMessage(EVENT_QUERY_FACILITY_LOCK_DONE));
                    mPhone.mCM.getIccCardStatus(
                        obtainMessage(EVENT_REPOLL_STATUS_DONE, ar.userObj));
                    break;
                case EVENT_REPOLL_STATUS_DONE:
                    // Finished repolling status after PIN operation
                    // ar.userObj is the response messaeg
                    // ar.userObj.obj is already an AsyncResult with an
                    // appropriate exception filled in if applicable

                    ar = (AsyncResult)msg.obj;
                    getIccCardStatusDone(ar);
                    ((Message)ar.userObj).sendToTarget();
                    break;
                case EVENT_QUERY_FACILITY_LOCK_DONE:
                    ar = (AsyncResult)msg.obj;
                    onQueryFacilityLock(ar);
                    break;
                case EVENT_QUERY_FACILITY_FDN_DONE:
                    ar = (AsyncResult)msg.obj;
                    onQueryFdnEnabled(ar);
                    break;
                case EVENT_CHANGE_FACILITY_LOCK_DONE:
                    ar = (AsyncResult)msg.obj;
                    if (ar.exception == null) {
                        mIccPinLocked = mDesiredPinLocked;
                        if (mDbg) log( "EVENT_CHANGE_FACILITY_LOCK_DONE: " +
                                "mIccPinLocked= " + mIccPinLocked);
                    } else {
                        Log.e(mLogTag, "Error change facility lock with exception "
                            + ar.exception);
                    }
                    AsyncResult.forMessage(((Message)ar.userObj)).exception
                                                        = ar.exception;
                    ((Message)ar.userObj).sendToTarget();
                    break;
                case EVENT_CHANGE_FACILITY_FDN_DONE:
                    ar = (AsyncResult)msg.obj;

                    if (ar.exception == null) {
                        mIccFdnEnabled = mDesiredFdnEnabled;
                        mPhbReady = false;
                        updatePhbStateProperty();
                        if (mDbg) log("EVENT_CHANGE_FACILITY_FDN_DONE: " +
                                "mIccFdnEnabled=" + mIccFdnEnabled);
                    } else {
                        Log.e(mLogTag, "Error change facility fdn with exception "
                                + ar.exception);
                    }
                    AsyncResult.forMessage(((Message)ar.userObj)).exception
                                                        = ar.exception;
                    ((Message)ar.userObj).sendToTarget();
                    break;
                case EVENT_CHANGE_ICC_PASSWORD_DONE:
                    ar = (AsyncResult)msg.obj;
                    if(ar.exception != null) {
                        Log.e(mLogTag, "Error in change sim password with exception"
                            + ar.exception);
                    }
                    AsyncResult.forMessage(((Message)ar.userObj)).exception
                                                        = ar.exception;
//MTK-START [mtk80601][111215][ALPS00093395]
                    /* Query Facility lock status again as EVENT_PINPUK_DONE */
                    mPhone.mCM.queryFacilityLock (
                            CommandsInterface.CB_FACILITY_BA_SIM, "", serviceClassX,
                            obtainMessage(EVENT_QUERY_FACILITY_LOCK_DONE));
                    mPhone.mCM.getIccCardStatus(
                            obtainMessage(EVENT_REPOLL_STATUS_DONE, ar.userObj));
                    //((Message)ar.userObj).sendToTarget();
                    break;
                case EVENT_QUERY_NETWORK_LOCK_DONE:
                    if (mDbg) log("handleMessage (EVENT_QUERY_NETWORK_LOCK)");
                    ar = (AsyncResult)msg.obj;
                    
                    if (ar.exception != null) {
                        Log.e(mLogTag, "Error query network lock with exception "
                            + ar.exception );
                    }

                    // ALPS00312695
                    AsyncResult.forMessage((Message) ar.userObj, ar.result, ar.exception);
                    ((Message) ar.userObj).sendToTarget();    

                    break;
                case EVENT_CHANGE_NETWORK_LOCK_DONE:
                    if (mDbg) log("handleMessage (EVENT_CHANGE_NETWORK_LOCK)");
                    ar = (AsyncResult)msg.obj;
                    if (ar.exception != null) {
                        Log.e(mLogTag, "Error change network lock with exception "
                            + ar.exception );
                    }
                    AsyncResult.forMessage(((Message)ar.userObj)).exception
                                                        = ar.exception;
                    ((Message)ar.userObj).sendToTarget();
                    break;
                case EVENT_QUERY_ICCID_DONE:
                    if (mDbg) log("handleMessage (EVENT_QUERY_ICCID_DONE)");
                    ar = (AsyncResult)msg.obj;
                    if (ar.exception != null) {
                        Log.e(mLogTag, "Error query iccid with exception");
                        AsyncResult.forMessage(((Message)ar.userObj)).exception
                                                        = ar.exception;
                    } else {
                        mIccId = (String)ar.result;
                        if (mDbg) log("Query iccid done. mIccId = " + mIccId);
                        AsyncResult.forMessage(((Message)ar.userObj)).result
                                                        = ar.result;
                    }   
                    ((Message)ar.userObj).sendToTarget();
                    break;
                case EVENT_PHB_READY:
                    if (mDbg) log("handleMessage (EVENT_PHB_READY)");
                    mPhbReady = true;
                    //No need to update system property because it has been updated in rill.
                    mPhone.mCM.queryFacilityLock (
                            CommandsInterface.CB_FACILITY_BA_FD, "", serviceClassX,
                            obtainMessage(EVENT_QUERY_FACILITY_FDN_DONE));
                    broadcastPhbStateChangedIntent(mPhbReady);
                    break;
                case EVENT_SIM_MISSING:
                    if (mDbg) log("handleMessage (EVENT_SIM_MISSING)");
                    //MTK-START [mtkXXXXX][120208][APLS00109092] Replace "RIL_UNSOL_SIM_MISSING in RIL.java" with "acively query SIM missing status"
                    mIccCardStatus = null;
                    //MTK-END [mtkXXXXX][120208][APLS00109092] Replace "RIL_UNSOL_SIM_MISSING in RIL.java" with "acively query SIM missing status"
                    mState = State.ABSENT;
                    mAbsentRegistrants.notifyRegistrants();
                    updateStateProperty();
                    broadcastIccStateChangedIntent(INTENT_VALUE_ICC_ABSENT, null);
                    broadcastIccStateChangedExtendIntent(INTENT_VALUE_ICC_ABSENT, null);
                    setNotification(EVENT_SIM_MISSING);
                    break;
                    //MTK-END [mtk80601][111215][ALPS00093395]
                case EVENT_SIM_RECOVERY:
                    if (mDbg) log("handleMessage (EVENT_SIM_RECOVERY)");
                    mPhone.mCM.getIccCardStatus(obtainMessage(EVENT_GET_ICC_STATUS_DONE));
                    mRecoveryRegistrants.notifyRegistrants();
                    updateStateProperty();
                    break;
               case EVENT_VIRTUAL_SIM_ON:
			   	          if (FeatureOption.MTK_GEMINI_SUPPORT) 
			   	          {
			   	          	  if (mDbg) log("handleMessage (EVENT_VIRTUAL_SIM_ON),MTK_GEMINI_SUPPORT on");
                        int simId = mPhone.getMySimId();
                        int dualSimMode = Settings.System.getInt(mPhone.getContext().getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 0);
                        mPhone.mCM.setRadioMode((dualSimMode & (simId+1)), obtainMessage(EVENT_CARD_ADDED, dualSimMode, 0));
                    }
                    else 
                    {
                    	  if (mDbg) log("handleMessage (EVENT_VIRTUAL_SIM_ON),MTK_GEMINI_SUPPORT off");
											  mPhone.mCM.setRadioPower(true,null);
                    }
                    mPhone.mCM.getIccCardStatus(obtainMessage(EVENT_GET_ICC_STATUS_DONE));
                    setNotificationVirtual(EVENT_VIRTUAL_SIM_ON);
                    SharedPreferences shOn = mPhone.getContext().getSharedPreferences("AutoAnswer", 1);
                    SharedPreferences.Editor editorOn = shOn.edit();
                    editorOn.putBoolean("flag", true);
                    editorOn.commit();
                    break;
               case EVENT_VIRTUAL_SIM_OFF:
			   	          if (mDbg) log("handleMessage (EVENT_VIRTUAL_SIM_OFF)");
                    //mPhone.mCM.getIccCardStatus(obtainMessage(EVENT_GET_ICC_STATUS_DONE));
					          mIccCardStatus = null;
                    //MTK-END [mtkXXXXX][120208][APLS00109092] Replace "RIL_UNSOL_SIM_MISSING in RIL.java" with "acively query SIM missing status"
                    mState = State.ABSENT;
                    mAbsentRegistrants.notifyRegistrants();
                    updateStateProperty();
                    broadcastIccStateChangedIntent(INTENT_VALUE_ICC_ABSENT, null);
                    broadcastIccStateChangedExtendIntent(INTENT_VALUE_ICC_ABSENT, null);
                    removeNotificationVirtual(EVENT_VIRTUAL_SIM_ON);
                    setNotification(EVENT_SIM_MISSING);
                    SharedPreferences shOff = mPhone.getContext().getSharedPreferences("AutoAnswer", 1);
                    SharedPreferences.Editor editorOff = shOff.edit();
                    editorOff.putBoolean("flag", false);
                    editorOff.commit();
                    break;
                case EVENT_ICC_STATUS_CHANGED:
                    Log.d(mLogTag, "Received Event EVENT_ICC_STATUS_CHANGED");
                    mPhone.mCM.getIccCardStatus(obtainMessage(EVENT_GET_ICC_STATUS_DONE));
                    break;
                case EVENT_CARD_REMOVED:
                    onIccSwap(false);
                    break;
                case EVENT_CARD_ADDED:
                    //onIccSwap(true);
                    break;

		case EVENT_UIM_INSERT_STATUS: 
		    log("handleMessage (EVENT_SIM_INSERT_STATUS)");
		    ar = (AsyncResult)msg.obj;
		    int uimInsertedStatus = ((int[])ar.result)[0];
		    Log.d("IccCard ", " uimInsertedStatus = "  + uimInsertedStatus);
		    if(uimInsertedStatus == Phone.UIM_STATUS_NO_CARD_INSERTED) {
                        mIccCardStatus = null;
                        mState = State.ABSENT;
                        mAbsentRegistrants.notifyRegistrants();
                        updateStateProperty();
                        broadcastIccStateChangedIntent(INTENT_VALUE_ICC_ABSENT, null);
                        broadcastIccStateChangedExtendIntent(INTENT_VALUE_ICC_ABSENT, null);
                        setNotification(EVENT_UIM_INSERT_STATUS);
		    }
	            break;

                // NFC SEEK start
                case EVENT_EXCHANGE_APDU_DONE:
                case EVENT_OPEN_CHANNEL_DONE:
                case EVENT_CLOSE_CHANNEL_DONE:
		case EVENT_SIM_IO_DONE:
                case EVENT_GET_ATR_DONE:
                    ar = (AsyncResult)msg.obj;
                    if(ar.exception != null) {
                        Log.e(mLogTag, "Error in SIM access with exception"
                            + ar.exception);
                    }
                    AsyncResult.forMessage(((Message)ar.userObj),
                            ar.result, ar.exception);
                    ((Message)ar.userObj).sendToTarget();
                    break;
                // NFC SEEK end
                default:
                    Log.e(mLogTag, "[IccCard] Unknown Event " + msg.what);
            }
        }
    };

    public State getIccCardState() {
        if (mIccCardStatus == null) {
            Log.e(mLogTag, "[IccCard] IccCardStatus is null");
            return IccCard.State.ABSENT;
        }

        // this is common for all radio technologies
        if (!mIccCardStatus.getCardState().isCardPresent()) {
            return IccCard.State.ABSENT;
        }

        RadioState currentRadioState = mPhone.mCM.getRadioState();
        // check radio technology
        if( currentRadioState == RadioState.RADIO_OFF         ||
            currentRadioState == RadioState.RADIO_UNAVAILABLE ||
            currentRadioState == RadioState.SIM_NOT_READY     ||
            currentRadioState == RadioState.RUIM_NOT_READY    ||
            currentRadioState == RadioState.NV_NOT_READY      ||
            currentRadioState == RadioState.NV_READY) {
            return IccCard.State.NOT_READY;
        }

        if( currentRadioState == RadioState.SIM_LOCKED_OR_ABSENT  ||
            currentRadioState == RadioState.SIM_READY             ||
            currentRadioState == RadioState.RUIM_LOCKED_OR_ABSENT ||
            currentRadioState == RadioState.RUIM_READY) {
            State csimState =
                getAppState(mIccCardStatus.getCdmaSubscriptionAppIndex());
            State usimState =
                getAppState(mIccCardStatus.getGsmUmtsSubscriptionAppIndex());

            if(mDbg) log("USIM=" + usimState + " CSIM=" + csimState);

            if (mPhone.getLteOnCdmaMode() == Phone.LTE_ON_CDMA_TRUE) {
                // UICC card contains both USIM and CSIM
                // Return consolidated status
                return getConsolidatedState(csimState, usimState, csimState);
            }

            // check for CDMA radio technology
            if (currentRadioState == RadioState.RUIM_LOCKED_OR_ABSENT ||
                currentRadioState == RadioState.RUIM_READY) {
                return csimState;
            }
            return usimState;
        }

        return IccCard.State.ABSENT;
    }

    private State getAppState(int appIndex) {
        IccCardApplication app;
        if (appIndex >= 0 && appIndex < IccCardStatus.CARD_MAX_APPS) {
            app = mIccCardStatus.getApplication(appIndex);
        } else {
            Log.e(mLogTag, "[IccCard] Invalid Subscription Application index:" + appIndex);
            return IccCard.State.ABSENT;
        }

        if (app == null) {
            Log.e(mLogTag, "[IccCard] Subscription Application in not present");
            return IccCard.State.ABSENT;
        }

        // check if PIN required
        if (app.pin1.isPermBlocked()) {
            return IccCard.State.PERM_DISABLED;
        }
        if (app.app_state.isPinRequired()) {
            return IccCard.State.PIN_REQUIRED;
        }
        if (app.app_state.isPukRequired()) {
            return IccCard.State.PUK_REQUIRED;
        }
        if (app.app_state.isSubscriptionPersoEnabled()) {
            return IccCard.State.NETWORK_LOCKED;
        }
        if (app.app_state.isAppReady()) {
            return IccCard.State.READY;
        }
        if (app.app_state.isAppNotReady()) {
            return IccCard.State.NOT_READY;
        }
        return IccCard.State.NOT_READY;
    }

    private State getConsolidatedState(State left, State right, State preferredState) {
        // Check if either is absent.
        if (right == IccCard.State.ABSENT) return left;
        if (left == IccCard.State.ABSENT) return right;

        // Only if both are ready, return ready
        if ((left == IccCard.State.READY) && (right == IccCard.State.READY)) {
            return State.READY;
        }

        // Case one is ready, but the other is not.
        if (((right == IccCard.State.NOT_READY) && (left == IccCard.State.READY)) ||
            ((left == IccCard.State.NOT_READY) && (right == IccCard.State.READY))) {
            return IccCard.State.NOT_READY;
        }

        // At this point, the other state is assumed to be one of locked state
        if (right == IccCard.State.NOT_READY) return left;
        if (left == IccCard.State.NOT_READY) return right;

        // At this point, FW currently just assumes the status will be
        // consistent across the applications...
        return preferredState;
    }

    public boolean isApplicationOnIcc(IccCardApplication.AppType type) {
        if (mIccCardStatus == null){ 
//MTK-START [mtk80601][111215][ALPS00093395]
			if (mIccType == null) {
                if (Phone.GEMINI_SIM_2 == mPhone.getMySimId()) {
                    mIccType = SystemProperties.get(PROPERTY_RIL_UICC2_TYPE);
                } else {
                    mIccType = SystemProperties.get(PROPERTY_RIL_UICC_TYPE);
                }
			}
            if (mDbg) log("isApplicationOnIcc(): mIccCardStatus is null. mIccType = " + mIccType);

            if ((mIccType != null) && (mIccType.equals("USIM"))) {
                return true;
            } else {
                return false;
            }
//MTK-END [mtk80601][111215][ALPS00093395]
        }

        for (int i = 0 ; i < mIccCardStatus.getNumApplications(); i++) {
            IccCardApplication app = mIccCardStatus.getApplication(i);
            if (app != null && app.app_type == type) {
                return true;
            }
        }

        if (mDbg) log("isApplicationOnIcc(): UICC SIM detected!!!");
        return false;
    }

    /**
     * @return true if a ICC card is present
     */
    public boolean hasIccCard() {
        if (mIccCardStatus == null) {
            return false;
        } else if (mPhone.getPhoneName().equals("GSM")) {
            return mIccCardStatus.getCardState().isCardPresent();
        } else if (mPhone.getPhoneName().equals("CDMA")) {
            return mIccCardStatus.getCardState().isCardPresent();
        } else {
            // TODO: Make work with a CDMA device with a RUIM card.
            return false;
        }
    }
//MTK-START [mtk80601][111215][ALPS00093395]
    public boolean isPhbReady() {
        if (mDbg) log("isPhbReady(): cached mPhbReady = " + (mPhbReady ? "true" : "false"));
        String strPhbReady = null;
        if (Phone.GEMINI_SIM_2 == mPhone.getMySimId()) {
            strPhbReady = SystemProperties.get(PROPERTY_RIL_PHB_READY_2, "false");
        } else {
            strPhbReady = SystemProperties.get(PROPERTY_RIL_PHB_READY, "false");
        }   
        
        if (strPhbReady.equals("true")){
            mPhbReady = true;
        } else {
            mPhbReady = false;
        }
        if (mDbg) log("isPhbReady(): mPhbReady = " + (mPhbReady ? "true" : "false"));
        return mPhbReady;
    }

    public String getIccCardType() {
		 if (mIccType == null || mIccType.equals("")) {
            if (Phone.GEMINI_SIM_2 == mPhone.getMySimId()) {
                mIccType = SystemProperties.get(PROPERTY_RIL_UICC2_TYPE);
            } else {
                mIccType = SystemProperties.get(PROPERTY_RIL_UICC_TYPE);
            }
		 }

        if (mDbg) log("getIccCardType(): mIccType = " + mIccType);
        return mIccType;
    }

    //return the SIM ME Lock type required to unlock
    public int getNetworkPersoType(){
        if (mIccCardStatus == null) {
            Log.e(mLogTag, "[IccCard] getNetworkPersoType IccCardStatus is null");
            return -1;
        }

        // this is common for all radio technologies
        if (!mIccCardStatus.getCardState().isCardPresent()) {
            return -1;
        }

        RadioState currentRadioState = mPhone.mCM.getRadioState();
        // check radio technology
        if( currentRadioState == RadioState.SIM_LOCKED_OR_ABSENT  ||            
            currentRadioState == RadioState.RUIM_LOCKED_OR_ABSENT ) {

            int index;

            // check for CDMA radio technology
            if (currentRadioState == RadioState.RUIM_LOCKED_OR_ABSENT) {
                index = mIccCardStatus.getCdmaSubscriptionAppIndex();
            }
            else {
                index = mIccCardStatus.getGsmUmtsSubscriptionAppIndex();
            }

            IccCardApplication app = mIccCardStatus.getApplication(index);

            if (app == null) {
                Log.e(mLogTag, "[IccCard] Subscription Application in not present");
                return -1;
            }

            if (app.app_state.isSubscriptionPersoEnabled()) {
                switch (app.perso_substate){
                    case PERSOSUBSTATE_SIM_NETWORK:
                        return 0;
                    case PERSOSUBSTATE_SIM_NETWORK_SUBSET:
                        return 1;
                    case PERSOSUBSTATE_SIM_SERVICE_PROVIDER:
                        return 2;
                    case PERSOSUBSTATE_SIM_CORPORATE:
                        return 3;
                    case PERSOSUBSTATE_SIM_SIM:
                        return 4;
                }
             }
            return -1;
        }

        return -1;
    }
	public boolean isFDNExist(){
        UsimServiceTable ust= mPhone.getUsimServiceTable();
        if(ust!=null && ust.isAvailable(UsimServiceTable.UsimService.FDN))
        {
            if (mDbg) log("isFDNExist return true");
		    return true;
        }
        else
	    {		
            if (mDbg) log("isFDNExist return false");
		    return false;
	    }
	}
//MTK-END [mtk80601][111215][ALPS00093395]
    private void log(String msg) {
        Log.d(mLogTag, "[IccCard][SIM" + (mPhone.getMySimId() == 0?"1":"2") + "] "+ msg);
    }

    // NFC SEEK start
    public void exchangeAPDU(int cla, int command, int channel, int p1, int p2,
            int p3, String data, Message onComplete) {
        mPhone.mCM.iccExchangeAPDU(cla, command, channel, p1, p2, p3, data,
                mHandler.obtainMessage(EVENT_EXCHANGE_APDU_DONE, onComplete));
    }

    public void openLogicalChannel(String AID, Message onComplete) {
        mPhone.mCM.iccOpenChannel(AID,
                mHandler.obtainMessage(EVENT_OPEN_CHANNEL_DONE, onComplete));
    }

    public void closeLogicalChannel(int channel, Message onComplete) {
        mPhone.mCM.iccCloseChannel(channel,
                mHandler.obtainMessage(EVENT_CLOSE_CHANNEL_DONE, onComplete));
    }
	
    public void exchangeSimIO(int fileID, int command,
                                           int p1, int p2, int p3, String pathID, Message onComplete) {
        mPhone.mCM.iccIO(command,fileID,pathID,p1,p2,p3,null,null,
               mHandler.obtainMessage(EVENT_SIM_IO_DONE, onComplete));
    }	

    public void iccGetATR(Message onComplete) {
        mPhone.mCM.iccGetATR(mHandler.obtainMessage(EVENT_GET_ATR_DONE, onComplete));
    }
    // NFC SEEK end
	
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            log("Receive action " + action);
            if (TelephonyIntents.ACTION_SIM_INFO_UPDATE.equals(action)) {
                mPhone.getContext().unregisterReceiver(mReceiver);
                mSIMInfoReady = true;
                broadcastPhbStateChangedIntent(true);
            }
        }
        
    };
}
