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

package com.android.internal.telephony;

import com.android.internal.telephony.sip.SipPhone;

import android.content.Context;
import android.media.AudioManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.RegistrantList;
import android.os.Registrant;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.os.SystemProperties;

//MTK-START [mtk04070][111121][ALPS00093395]MTK added
import com.android.internal.telephony.gemini.*;
import com.android.internal.telephony.gsm.GsmConnection;
import com.android.internal.telephony.gsm.GSMPhone;
import com.android.internal.telephony.PhoneFactory;
import com.mediatek.featureoption.FeatureOption;
/* 3G switch start */
import com.android.internal.telephony.ITelephony;
import android.os.ServiceManager;
import android.os.RemoteException;
/* 3G switch end */
//MTK-END [mtk04070][111121][ALPS00093395]MTK added

/**
 * @hide
 *
 * CallManager class provides an abstract layer for PhoneApp to access
 * and control calls. It implements Phone interface.
 *
 * CallManager provides call and connection control as well as
 * channel capability.
 *
 * There are three categories of APIs CallManager provided
 *
 *  1. Call control and operation, such as dial() and hangup()
 *  2. Channel capabilities, such as CanConference()
 *  3. Register notification
 *
 *
 */
public final class CallManager {

    private static final String LOG_TAG ="CallManager";
    private static final boolean DBG = true;
    //MTK-START [mtk04070][111121][ALPS00093395]Set VDBG to true
    private static final boolean VDBG = true;
    //MTK-END [mtk04070][111121][ALPS00093395]Set VDBG to true

    private static final int EVENT_DISCONNECT = 100;
    private static final int EVENT_PRECISE_CALL_STATE_CHANGED = 101;
    private static final int EVENT_NEW_RINGING_CONNECTION = 102;
    private static final int EVENT_UNKNOWN_CONNECTION = 103;
    private static final int EVENT_INCOMING_RING = 104;
    private static final int EVENT_RINGBACK_TONE = 105;
    private static final int EVENT_IN_CALL_VOICE_PRIVACY_ON = 106;
    private static final int EVENT_IN_CALL_VOICE_PRIVACY_OFF = 107;
    private static final int EVENT_CALL_WAITING = 108;
    private static final int EVENT_DISPLAY_INFO = 109;
    private static final int EVENT_SIGNAL_INFO = 110;
    private static final int EVENT_CDMA_OTA_STATUS_CHANGE = 111;
    private static final int EVENT_RESEND_INCALL_MUTE = 112;
    private static final int EVENT_MMI_INITIATE = 113;
    private static final int EVENT_MMI_COMPLETE = 114;
    private static final int EVENT_ECM_TIMER_RESET = 115;
    private static final int EVENT_SUBSCRIPTION_INFO_READY = 116;
    private static final int EVENT_SUPP_SERVICE_FAILED = 117;
    private static final int EVENT_SERVICE_STATE_CHANGED = 118;
    private static final int EVENT_POST_DIAL_CHARACTER = 119;

    //MTK-START [mtk04070][111121][ALPS00093395]MTK added
    private static final int EVENT_SPEECH_INFO = 120;
    private static final int EVENT_VT_STATUS_INFO = 121;
    private static final int EVENT_VT_RING_INFO = 122;
    private static final int EVENT_CRSS_SUPP_SERVICE_NOTIFICATION = 123;
    private static final int EVENT_SUPP_SERVICE_NOTIFICATION = 124;
    private static final int EVENT_VT_REPLACE_DISCONNECT = 125;
    private static final int EVENT_DISCONNECT2 = 200;
    private static final int EVENT_PRECISE_CALL_STATE_CHANGED2 = 201;
    private static final int EVENT_NEW_RINGING_CONNECTION2 = 202;
    private static final int EVENT_UNKNOWN_CONNECTION2 = 203;
    private static final int EVENT_INCOMING_RING2 = 204;
    private static final int EVENT_RINGBACK_TONE2 = 205;
    private static final int EVENT_IN_CALL_VOICE_PRIVACY_ON2 = 206;
    private static final int EVENT_IN_CALL_VOICE_PRIVACY_OFF2 = 207;
    private static final int EVENT_CALL_WAITING2 = 208;
    private static final int EVENT_DISPLAY_INFO2 = 209;
    private static final int EVENT_SIGNAL_INFO2 = 210;
    private static final int EVENT_CDMA_OTA_STATUS_CHANGE2 = 211;
    private static final int EVENT_RESEND_INCALL_MUTE2 = 212;
    private static final int EVENT_MMI_INITIATE2 = 213;
    private static final int EVENT_MMI_COMPLETE2 = 214;
    private static final int EVENT_ECM_TIMER_RESET2 = 215;
    private static final int EVENT_SUBSCRIPTION_INFO_READY2 = 216;
    private static final int EVENT_SUPP_SERVICE_FAILED2 = 217;
    private static final int EVENT_SERVICE_STATE_CHANGED2 = 218;
    private static final int EVENT_POST_DIAL_CHARACTER2 = 219;
    private static final int EVENT_SPEECH_INFO2 = 220;
    private static final int EVENT_VT_STATUS_INFO2 = 221;
    private static final int EVENT_VT_RING_INFO2 = 222;
    private static final int EVENT_CRSS_SUPP_SERVICE_NOTIFICATION2 = 223;
    private static final int EVENT_SUPP_SERVICE_NOTIFICATION2 = 224;
    private static final int EVENT_VT_REPLACE_DISCONNECT2 = 225;
    //MTK-END [mtk04070][111121][ALPS00093395]MTK added

    // Singleton instance
    private static final CallManager INSTANCE = new CallManager();

    // list of registered phones, which are PhoneBase objs
    private final ArrayList<Phone> mPhones;

    // list of supported ringing calls
    private final ArrayList<Call> mRingingCalls;

    // list of supported background calls
    private final ArrayList<Call> mBackgroundCalls;

    // list of supported foreground calls
    private final ArrayList<Call> mForegroundCalls;

    // empty connection list
    private final ArrayList<Connection> emptyConnections = new ArrayList<Connection>();

    // default phone as the first phone registered, which is PhoneBase obj
    private Phone mDefaultPhone;

    // state registrants
    protected final RegistrantList mPreciseCallStateRegistrants
    = new RegistrantList();

    protected final RegistrantList mNewRingingConnectionRegistrants
    = new RegistrantList();

    protected final RegistrantList mIncomingRingRegistrants
    = new RegistrantList();

    protected final RegistrantList mDisconnectRegistrants
    = new RegistrantList();

    protected final RegistrantList mMmiRegistrants
    = new RegistrantList();

    protected final RegistrantList mUnknownConnectionRegistrants
    = new RegistrantList();

    protected final RegistrantList mRingbackToneRegistrants
    = new RegistrantList();

    protected final RegistrantList mInCallVoicePrivacyOnRegistrants
    = new RegistrantList();

    protected final RegistrantList mInCallVoicePrivacyOffRegistrants
    = new RegistrantList();

    protected final RegistrantList mCallWaitingRegistrants
    = new RegistrantList();

    protected final RegistrantList mDisplayInfoRegistrants
    = new RegistrantList();

    protected final RegistrantList mSignalInfoRegistrants
    = new RegistrantList();

    protected final RegistrantList mCdmaOtaStatusChangeRegistrants
    = new RegistrantList();

    protected final RegistrantList mResendIncallMuteRegistrants
    = new RegistrantList();

    protected final RegistrantList mMmiInitiateRegistrants
    = new RegistrantList();

    protected final RegistrantList mMmiCompleteRegistrants
    = new RegistrantList();

    protected final RegistrantList mEcmTimerResetRegistrants
    = new RegistrantList();

    protected final RegistrantList mSubscriptionInfoReadyRegistrants
    = new RegistrantList();

    protected final RegistrantList mSuppServiceFailedRegistrants
    = new RegistrantList();

    protected final RegistrantList mServiceStateChangedRegistrants
    = new RegistrantList();

    protected final RegistrantList mPostDialCharacterRegistrants
    = new RegistrantList();

    //MTK-START [mtk04070][111121][ALPS00093395]MTK added
    /* MTK proprietary start */
    protected final RegistrantList mSpeechInfoRegistrants
    = new RegistrantList();

    protected final RegistrantList mVtStatusInfoRegistrants
    = new RegistrantList();

    protected final RegistrantList mVtRingInfoRegistrants
    = new RegistrantList();

    protected final RegistrantList mCrssSuppServiceNotificationRegistrants
    = new RegistrantList();

    protected final RegistrantList mSuppServiceNotificationRegistrants
    = new RegistrantList();

    protected final RegistrantList mVtReplaceDisconnectRegistrants
    = new RegistrantList();

    protected final RegistrantList mPreciseCallStateRegistrants2
    = new RegistrantList();

    protected final RegistrantList mNewRingingConnectionRegistrants2
    = new RegistrantList();

    protected final RegistrantList mIncomingRingRegistrants2
    = new RegistrantList();

    protected final RegistrantList mDisconnectRegistrants2
    = new RegistrantList();

    protected final RegistrantList mMmiRegistrants2
    = new RegistrantList();

    protected final RegistrantList mUnknownConnectionRegistrants2
    = new RegistrantList();

    protected final RegistrantList mRingbackToneRegistrants2
    = new RegistrantList();

    protected final RegistrantList mInCallVoicePrivacyOnRegistrants2
    = new RegistrantList();

    protected final RegistrantList mInCallVoicePrivacyOffRegistrants2
    = new RegistrantList();

    protected final RegistrantList mCallWaitingRegistrants2
    = new RegistrantList();

    protected final RegistrantList mDisplayInfoRegistrants2
    = new RegistrantList();

    protected final RegistrantList mSignalInfoRegistrants2
    = new RegistrantList();

    protected final RegistrantList mCdmaOtaStatusChangeRegistrants2
    = new RegistrantList();

    protected final RegistrantList mResendIncallMuteRegistrants2
    = new RegistrantList();

    protected final RegistrantList mMmiInitiateRegistrants2
    = new RegistrantList();

    protected final RegistrantList mMmiCompleteRegistrants2
    = new RegistrantList();

    protected final RegistrantList mEcmTimerResetRegistrants2
    = new RegistrantList();

    protected final RegistrantList mSubscriptionInfoReadyRegistrants2
    = new RegistrantList();

    protected final RegistrantList mSuppServiceFailedRegistrants2
    = new RegistrantList();

    protected final RegistrantList mServiceStateChangedRegistrants2
    = new RegistrantList();

    protected final RegistrantList mPostDialCharacterRegistrants2
    = new RegistrantList();

    protected final RegistrantList mSpeechInfoRegistrants2
    = new RegistrantList();

    protected final RegistrantList mVtStatusInfoRegistrants2
    = new RegistrantList();

    protected final RegistrantList mVtRingInfoRegistrants2
    = new RegistrantList();

    protected final RegistrantList mCrssSuppServiceNotificationRegistrants2
    = new RegistrantList();

    protected final RegistrantList mSuppServiceNotificationRegistrants2
    = new RegistrantList();

    protected final RegistrantList mVtReplaceDisconnectRegistrants2
    = new RegistrantList();

    private boolean hasSetVtPara = false;

    //Merge DualTalk code
    private int mDualModemCall = 0; /* dual modem call */

    /* 3G Switch start */
    private int m3GSwitchLockForPhoneCall;
    /* 3G Switch end */
    
    /* Solve ALPS00275770, to prevent set audio mode to IN_CALL when ESPEECH info is not received yet */
    private int espeech_info = 0;
    private int espeech_info2 = 0;

    /* Solve ALPS00281513, to avoid DTMF start request is sent, but stop request is ignore due to active is held, mtk04070, 20120515 */
    private boolean dtmfRequestIsStarted = false;

    private GeminiPhone mGeminiPhone;


    //MTK-END [mtk04070][111121][ALPS00093395]MTK added

    private CallManager() {
        mPhones = new ArrayList<Phone>();
        mRingingCalls = new ArrayList<Call>();
        mBackgroundCalls = new ArrayList<Call>();
        mForegroundCalls = new ArrayList<Call>();
        mDefaultPhone = null;
    }

    /**
     * get singleton instance of CallManager
     * @return CallManager
     */
    public static CallManager getInstance() {
        return INSTANCE;
    }

    /**
     * Get the corresponding PhoneBase obj
     *
     * @param phone a Phone object
     * @return the corresponding PhoneBase obj in Phone if Phone
     * is a PhoneProxy obj
     * or the Phone itself if Phone is not a PhoneProxy obj
     */
    private static Phone getPhoneBase(Phone phone) {
        if (phone instanceof PhoneProxy) {
            return phone.getForegroundCall().getPhone();
        }
        return phone;
    }

    /**
     * Check if two phones refer to the same PhoneBase obj
     *
     * Note: PhoneBase, not PhoneProxy, is to be used inside of CallManager
     *
     * Both PhoneBase and PhoneProxy implement Phone interface, so
     * they have same phone APIs, such as dial(). The real implementation, for
     * example in GSM,  is in GSMPhone as extend from PhoneBase, so that
     * foregroundCall.getPhone() returns GSMPhone obj. On the other hand,
     * PhoneFactory.getDefaultPhone() returns PhoneProxy obj, which has a class
     * member of GSMPhone.
     *
     * So for phone returned by PhoneFacotry, which is used by PhoneApp,
     *        phone.getForegroundCall().getPhone() != phone
     * but
     *        isSamePhone(phone, phone.getForegroundCall().getPhone()) == true
     *
     * @param p1 is the first Phone obj
     * @param p2 is the second Phone obj
     * @return true if p1 and p2 refer to the same phone
     */
    public static boolean isSamePhone(Phone p1, Phone p2) {
        return (getPhoneBase(p1) == getPhoneBase(p2));
    }

    /**
     * Returns all the registered phone objects.
     * @return all the registered phone objects.
     */
    public List<Phone> getAllPhones() {
        return Collections.unmodifiableList(mPhones);
    }

    /**
     * Get current coarse-grained voice call state.
     * If the Call Manager has an active call and call waiting occurs,
     * then the phone state is RINGING not OFFHOOK
     *
     */
    public Phone.State getState() {
        Phone.State s = Phone.State.IDLE;

        for (Phone phone : mPhones) {
            if (phone.getState() == Phone.State.RINGING) {
                s = Phone.State.RINGING;
            } else if (phone.getState() == Phone.State.OFFHOOK) {
                if (s == Phone.State.IDLE) s = Phone.State.OFFHOOK;
            }
        }
        return s;
    }

    /**
     * @return the service state of CallManager, which represents the
     * highest priority state of all the service states of phones
     *
     * The priority is defined as
     *
     * STATE_IN_SERIVCE > STATE_OUT_OF_SERIVCE > STATE_EMERGENCY > STATE_POWER_OFF
     *
     */

    public int getServiceState() {
        int resultState = ServiceState.STATE_OUT_OF_SERVICE;

        for (Phone phone : mPhones) {
            int serviceState = phone.getServiceState().getState();
            if (serviceState == ServiceState.STATE_IN_SERVICE) {
                // IN_SERVICE has the highest priority
                resultState = serviceState;
                break;
            } else if (serviceState == ServiceState.STATE_OUT_OF_SERVICE) {
                // OUT_OF_SERVICE replaces EMERGENCY_ONLY and POWER_OFF
                // Note: EMERGENCY_ONLY is not in use at this moment
                if ( resultState == ServiceState.STATE_EMERGENCY_ONLY ||
                        resultState == ServiceState.STATE_POWER_OFF) {
                    resultState = serviceState;
                }
            } else if (serviceState == ServiceState.STATE_EMERGENCY_ONLY) {
                if (resultState == ServiceState.STATE_POWER_OFF) {
                    resultState = serviceState;
                }
            }
        }
        return resultState;
    }

    private boolean registerOnePhone(Phone phone) {
    	  boolean result = false;
        Phone basePhone = getPhoneBase(phone);
        if (basePhone != null && !mPhones.contains(basePhone)) {
            if (DBG) {
                Log.d(LOG_TAG, "[BSPPackage]registerPhone(" + phone.getPhoneName() + " " + phone + ")");
            }

            mPhones.add(basePhone);
            mRingingCalls.add(basePhone.getRingingCall());
            mBackgroundCalls.add(basePhone.getBackgroundCall());
            mForegroundCalls.add(basePhone.getForegroundCall()); 
            result = true;   
        }
        return result;
    }


    /**
     * Register phone to CallManager
     * @param phone to be registered
     * @return true if register successfully
     */
    public boolean registerPhone(Phone phone) {
       if ((FeatureOption.MTK_BSP_PACKAGE == true) && 
	   	   (FeatureOption.MTK_GEMINI_SUPPORT == true) && 
	   	   (!(phone instanceof SipPhone))) {
    	   /* registerPhone is called by Google default PhoneAPP */
           Phone p = ((GeminiPhone)phone).getPhonebyId(Phone.GEMINI_SIM_1);
           registerOnePhone(p);
           p = ((GeminiPhone)phone).getPhonebyId(Phone.GEMINI_SIM_2);
           registerOnePhone(p);
           
           int default_sim = SystemProperties.getInt(Phone.GEMINI_DEFAULT_SIM_PROP, Phone.GEMINI_SIM_1);
           mDefaultPhone = getPhoneBase(((GeminiPhone)phone).getPhonebyId(default_sim));
           Log.d(LOG_TAG, "[BSPPackage]default_sim = " + default_sim);
           Log.d(LOG_TAG, "[BSPPackage]mDefaultPhone = " + mDefaultPhone);
           registerForPhoneStates(getPhoneBase(phone));
           return true;
        }
        else {
        Phone basePhone = getPhoneBase(phone);

        if (basePhone != null && !mPhones.contains(basePhone)) {

            if (DBG) {
                Log.d(LOG_TAG, "registerPhone(" +
                        phone.getPhoneName() + " " + phone + ")");
            }

            if (mPhones.isEmpty()) {
                mDefaultPhone = basePhone;
            }
            mPhones.add(basePhone);
            mRingingCalls.add(basePhone.getRingingCall());
            mBackgroundCalls.add(basePhone.getBackgroundCall());
            mForegroundCalls.add(basePhone.getForegroundCall());
            //MTK-START [mtk04070][111121][ALPS00093395]Refined for supporting Gemini
            if (FeatureOption.MTK_GEMINI_SUPPORT == false ||
                (FeatureOption.MTK_GEMINI_SUPPORT == true && phone instanceof SipPhone)) {
            registerForPhoneStates(basePhone);
            }
            //MTK-END [mtk04070][111121][ALPS00093395]Refined for supporting Gemini
            return true;
        }
        return false;
    }
    }

    /**
     * unregister phone from CallManager
     * @param phone to be unregistered
     */
    public void unregisterPhone(Phone phone) {
        Phone basePhone = getPhoneBase(phone);

        if (basePhone != null && mPhones.contains(basePhone)) {

            if (DBG) {
                Log.d(LOG_TAG, "unregisterPhone(" +
                        phone.getPhoneName() + " " + phone + ")");
            }

            mPhones.remove(basePhone);
            mRingingCalls.remove(basePhone.getRingingCall());
            mBackgroundCalls.remove(basePhone.getBackgroundCall());
            mForegroundCalls.remove(basePhone.getForegroundCall());
            //MTK-START [mtk04070][111121][ALPS00093395]Refined for supporting Gemini
            if (FeatureOption.MTK_GEMINI_SUPPORT == false ||
                (FeatureOption.MTK_GEMINI_SUPPORT == true && phone instanceof SipPhone)) {
            unregisterForPhoneStates(basePhone);
            }
            //MTK-END [mtk04070][111121][ALPS00093395]Refined for supporting Gemini
            if (basePhone == mDefaultPhone) {
                if (mPhones.isEmpty()) {
                    mDefaultPhone = null;
                } else {
                    mDefaultPhone = mPhones.get(0);
                }
            }
        }
    }

    /**
     * return the default phone or null if no phone available
     */
    public Phone getDefaultPhone() {
        return mDefaultPhone;
    }

    /**
     * @return the phone associated with the foreground call
     */
    public Phone getFgPhone() {
        return getActiveFgCall().getPhone();
    }

    /**
     * @return the phone associated with the background call
     */
    public Phone getBgPhone() {
        return getFirstActiveBgCall().getPhone();
    }

    /**
     * @return the phone associated with the ringing call
     */
    public Phone getRingingPhone() {
        return getFirstActiveRingingCall().getPhone();
    }

    public void setAudioMode() {
        int mode = AudioManager.MODE_NORMAL;
        switch (getState()) {
            case RINGING:
                mode = AudioManager.MODE_RINGTONE;
                break;
            case OFFHOOK:
                //MTK-START [mtk04070][111223][ALPS00106134]Merge to ICS 4.0.3
                Phone offhookPhone = getFgPhone();
                if (getActiveFgCallState() == Call.State.IDLE) {
                    // There is no active Fg calls, the OFFHOOK state
                    // is set by the Bg call. So set the phone to bgPhone.
                    offhookPhone = getBgPhone();
                }

                if (offhookPhone instanceof SipPhone) {
                    // enable IN_COMMUNICATION audio mode for sipPhone
                    mode = AudioManager.MODE_IN_COMMUNICATION;
                } else {
                    // enable IN_CALL audio mode for telephony
                    mode = AudioManager.MODE_IN_CALL;
                }
                //MTK-END [mtk04070][111223][ALPS00106134]Merge to ICS 4.0.3
                break;
        }

        //Merge DualTalk code.
        //if ((mode == AudioManager.MODE_IN_CALL) && (FeatureOption.MTK_DT_SUPPORT == true)) {
        if ((mode == AudioManager.MODE_IN_CALL) && PhoneFactory.isDualTalkMode()) {
            int newDualModemCall;
            if (getFgPhone().getMySimId() == Phone.GEMINI_SIM_2)
                newDualModemCall = 1;
            else
                newDualModemCall = 0;
            if (newDualModemCall != mDualModemCall) {
                setAudioModeDualModem(mDualModemCall, AudioManager.MODE_NORMAL);
                mDualModemCall = newDualModemCall;
                Log.d(LOG_TAG, "set mDualModemCall = " + mDualModemCall);
            }
            setAudioModeDualModem(mDualModemCall, AudioManager.MODE_IN_CALL);
        } else {		
            //MTK-START [mtk04070][111121][ALPS00093395]MTK modified
            setAudioMode(mode);
            //MTK-END [mtk04070][111121][ALPS00093395]MTK modified
        }
        //Merge DualTalk code.
    }

    private Context getContext() {
        Phone defaultPhone = getDefaultPhone();
        return ((defaultPhone == null) ? null : defaultPhone.getContext());
    }

    //MTK-START [mtk04070][111121][ALPS00093395]MTK modified
    public void registerForPhoneStates(Phone phone) {
        // for common events supported by all phones
        if (FeatureOption.MTK_GEMINI_SUPPORT == true && !(phone instanceof SipPhone)) {
            ((GeminiPhone)phone).registerForPreciseCallStateChangedGemini(mHandler, EVENT_PRECISE_CALL_STATE_CHANGED, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForPreciseCallStateChangedGemini(mHandler, EVENT_PRECISE_CALL_STATE_CHANGED2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForDisconnectGemini(mHandler, EVENT_DISCONNECT, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForDisconnectGemini(mHandler, EVENT_DISCONNECT2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForNewRingingConnectionGemini(mHandler, EVENT_NEW_RINGING_CONNECTION, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForNewRingingConnectionGemini(mHandler, EVENT_NEW_RINGING_CONNECTION2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForUnknownConnectionGemini(mHandler, EVENT_UNKNOWN_CONNECTION, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForUnknownConnectionGemini(mHandler, EVENT_UNKNOWN_CONNECTION2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForIncomingRingGemini(mHandler, EVENT_INCOMING_RING, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForIncomingRingGemini(mHandler, EVENT_INCOMING_RING2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForRingbackToneGemini(mHandler, EVENT_RINGBACK_TONE, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForRingbackToneGemini(mHandler, EVENT_RINGBACK_TONE2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForInCallVoicePrivacyOnGemini(mHandler, EVENT_IN_CALL_VOICE_PRIVACY_ON, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForInCallVoicePrivacyOnGemini(mHandler, EVENT_IN_CALL_VOICE_PRIVACY_ON2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForInCallVoicePrivacyOffGemini(mHandler, EVENT_IN_CALL_VOICE_PRIVACY_OFF, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForInCallVoicePrivacyOffGemini(mHandler, EVENT_IN_CALL_VOICE_PRIVACY_OFF2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForDisplayInfoGemini(mHandler, EVENT_DISPLAY_INFO, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForDisplayInfoGemini(mHandler, EVENT_DISPLAY_INFO2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForSignalInfoGemini(mHandler, EVENT_SIGNAL_INFO, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForSignalInfoGemini(mHandler, EVENT_SIGNAL_INFO2, null, Phone.GEMINI_SIM_2);
            //((GeminiPhone)phone).registerForResendIncallMuteGeminim(Handler, EVENT_RESEND_INCALL_MUTE, null, Phone.GEMINI_SIM_1);
            //((GeminiPhone)phone).registerForResendIncallMuteGeminim(Handler, EVENT_RESEND_INCALL_MUTE2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForMmiInitiateGemini(mHandler, EVENT_MMI_INITIATE, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForMmiInitiateGemini(mHandler, EVENT_MMI_INITIATE2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForMmiCompleteGemini(mHandler, EVENT_MMI_COMPLETE, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForMmiCompleteGemini(mHandler, EVENT_MMI_COMPLETE2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForSuppServiceFailedGemini(mHandler, EVENT_SUPP_SERVICE_FAILED, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForSuppServiceFailedGemini(mHandler, EVENT_SUPP_SERVICE_FAILED2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForServiceStateChangedGemini(mHandler, EVENT_SERVICE_STATE_CHANGED, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForServiceStateChangedGemini(mHandler, EVENT_SERVICE_STATE_CHANGED2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).setOnPostDialCharacterGemini(mHandler, EVENT_POST_DIAL_CHARACTER, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).setOnPostDialCharacterGemini(mHandler, EVENT_POST_DIAL_CHARACTER2, null, Phone.GEMINI_SIM_2);

            /* MTK proprietary start */
            ((GeminiPhone)phone).registerForSpeechInfoGemini(mHandler, EVENT_SPEECH_INFO, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForSpeechInfoGemini(mHandler, EVENT_SPEECH_INFO2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForVtStatusInfoGemini(mHandler, EVENT_VT_STATUS_INFO, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForVtStatusInfoGemini(mHandler, EVENT_VT_STATUS_INFO2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForVtRingInfoGemini(mHandler, EVENT_VT_RING_INFO, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForVtRingInfoGemini(mHandler, EVENT_VT_RING_INFO2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForCrssSuppServiceNotificationGemini(mHandler, EVENT_CRSS_SUPP_SERVICE_NOTIFICATION, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForCrssSuppServiceNotificationGemini(mHandler, EVENT_CRSS_SUPP_SERVICE_NOTIFICATION2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForSuppServiceNotificationGemini(mHandler, EVENT_SUPP_SERVICE_NOTIFICATION, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForSuppServiceNotificationGemini(mHandler, EVENT_SUPP_SERVICE_NOTIFICATION2, null, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).registerForVtReplaceDisconnectGemini(mHandler, EVENT_VT_REPLACE_DISCONNECT, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).registerForVtReplaceDisconnectGemini(mHandler, EVENT_VT_REPLACE_DISCONNECT2, null, Phone.GEMINI_SIM_2);
            /* MTK proprietary end */

            /* 
               InCallScreen only register notification for one phone, so we need to register notifications of phone 1 for phone 2 
            */
            if (FeatureOption.MTK_BSP_PACKAGE == true) {
            	 Log.d(LOG_TAG, "[BSPPackage]Register notification for Phone 2");
               ((GeminiPhone)phone).registerForPreciseCallStateChangedGemini(mHandler, EVENT_PRECISE_CALL_STATE_CHANGED, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForDisconnectGemini(mHandler, EVENT_DISCONNECT, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForNewRingingConnectionGemini(mHandler, EVENT_NEW_RINGING_CONNECTION, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForUnknownConnectionGemini(mHandler, EVENT_UNKNOWN_CONNECTION, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForIncomingRingGemini(mHandler, EVENT_INCOMING_RING, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForRingbackToneGemini(mHandler, EVENT_RINGBACK_TONE, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForInCallVoicePrivacyOnGemini(mHandler, EVENT_IN_CALL_VOICE_PRIVACY_ON, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForInCallVoicePrivacyOffGemini(mHandler, EVENT_IN_CALL_VOICE_PRIVACY_OFF, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForDisplayInfoGemini(mHandler, EVENT_DISPLAY_INFO, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForSignalInfoGemini(mHandler, EVENT_SIGNAL_INFO, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForMmiInitiateGemini(mHandler, EVENT_MMI_INITIATE, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForMmiCompleteGemini(mHandler, EVENT_MMI_COMPLETE, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForSuppServiceFailedGemini(mHandler, EVENT_SUPP_SERVICE_FAILED, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForServiceStateChangedGemini(mHandler, EVENT_SERVICE_STATE_CHANGED, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).setOnPostDialCharacterGemini(mHandler, EVENT_POST_DIAL_CHARACTER, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForSpeechInfoGemini(mHandler, EVENT_SPEECH_INFO, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForVtStatusInfoGemini(mHandler, EVENT_VT_STATUS_INFO, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForVtRingInfoGemini(mHandler, EVENT_VT_RING_INFO, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForCrssSuppServiceNotificationGemini(mHandler, EVENT_CRSS_SUPP_SERVICE_NOTIFICATION, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForSuppServiceNotificationGemini(mHandler, EVENT_SUPP_SERVICE_NOTIFICATION, null, Phone.GEMINI_SIM_2);
               ((GeminiPhone)phone).registerForVtReplaceDisconnectGemini(mHandler, EVENT_VT_REPLACE_DISCONNECT, null, Phone.GEMINI_SIM_2);
            }

            // for events supported only by CDMA phone
            if (FeatureOption.EVDO_DT_SUPPORT) {
                if (PhoneFactory.isDualTalkMode()) { 
                    int simId = PhoneFactory.getExternalModemSlot();
                    ((GeminiPhone)phone).registerForCallWaitingGemini(mHandler, EVENT_CALL_WAITING2, null, simId); 
                    ((GeminiPhone)phone).registerForCdmaOtaStatusChangeGemini(mHandler, EVENT_CDMA_OTA_STATUS_CHANGE2, null, simId); 
                }    
            }
            
            mGeminiPhone = (GeminiPhone)phone;
        } else {
        phone.registerForPreciseCallStateChanged(mHandler, EVENT_PRECISE_CALL_STATE_CHANGED, null);
        phone.registerForDisconnect(mHandler, EVENT_DISCONNECT, null);
        phone.registerForNewRingingConnection(mHandler, EVENT_NEW_RINGING_CONNECTION, null);
        phone.registerForUnknownConnection(mHandler, EVENT_UNKNOWN_CONNECTION, null);
        phone.registerForIncomingRing(mHandler, EVENT_INCOMING_RING, null);
        phone.registerForRingbackTone(mHandler, EVENT_RINGBACK_TONE, null);
        phone.registerForInCallVoicePrivacyOn(mHandler, EVENT_IN_CALL_VOICE_PRIVACY_ON, null);
        phone.registerForInCallVoicePrivacyOff(mHandler, EVENT_IN_CALL_VOICE_PRIVACY_OFF, null);
        phone.registerForDisplayInfo(mHandler, EVENT_DISPLAY_INFO, null);
        phone.registerForSignalInfo(mHandler, EVENT_SIGNAL_INFO, null);
        phone.registerForResendIncallMute(mHandler, EVENT_RESEND_INCALL_MUTE, null);
        phone.registerForMmiInitiate(mHandler, EVENT_MMI_INITIATE, null);
        phone.registerForMmiComplete(mHandler, EVENT_MMI_COMPLETE, null);
        phone.registerForSuppServiceFailed(mHandler, EVENT_SUPP_SERVICE_FAILED, null);
        phone.registerForServiceStateChanged(mHandler, EVENT_SERVICE_STATE_CHANGED, null);
            /* MTK proprietary start */
            phone.registerForSpeechInfo(mHandler, EVENT_SPEECH_INFO, null);
            phone.registerForVtStatusInfo(mHandler, EVENT_VT_STATUS_INFO, null);
            phone.registerForVtRingInfo(mHandler, EVENT_VT_RING_INFO, null);
            phone.registerForCrssSuppServiceNotification(mHandler, EVENT_CRSS_SUPP_SERVICE_NOTIFICATION, null);
            phone.registerForSuppServiceNotification(mHandler, EVENT_SUPP_SERVICE_NOTIFICATION, null);
            phone.registerForVtReplaceDisconnect(mHandler, EVENT_VT_REPLACE_DISCONNECT, null);
            /* MTK proprietary end */

        // for events supported only by GSM and CDMA phone
        if (phone.getPhoneType() == Phone.PHONE_TYPE_GSM ||
                phone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            phone.setOnPostDialCharacter(mHandler, EVENT_POST_DIAL_CHARACTER, null);
        }

        // for events supported only by CDMA phone
        if (phone.getPhoneType() == Phone.PHONE_TYPE_CDMA ){
            phone.registerForCdmaOtaStatusChange(mHandler, EVENT_CDMA_OTA_STATUS_CHANGE, null);
            phone.registerForSubscriptionInfoReady(mHandler, EVENT_SUBSCRIPTION_INFO_READY, null);
            phone.registerForCallWaiting(mHandler, EVENT_CALL_WAITING, null);
            phone.registerForEcmTimerReset(mHandler, EVENT_ECM_TIMER_RESET, null);
        }
      }
    }

    public void unregisterForPhoneStates(Phone phone) {
        //  for common events supported by all phones
        if (FeatureOption.MTK_GEMINI_SUPPORT == true && !(phone instanceof SipPhone)) {
            ((GeminiPhone)phone).unregisterForPreciseCallStateChangedGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForPreciseCallStateChangedGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForDisconnectGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForDisconnectGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForNewRingingConnectionGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForNewRingingConnectionGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForUnknownConnectionGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForUnknownConnectionGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForIncomingRingGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForIncomingRingGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForRingbackToneGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForRingbackToneGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForInCallVoicePrivacyOnGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForInCallVoicePrivacyOnGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForInCallVoicePrivacyOffGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForInCallVoicePrivacyOffGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForDisplayInfoGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForDisplayInfoGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForSignalInfoGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForSignalInfoGemini(mHandler, Phone.GEMINI_SIM_2);
            //((GeminiPhone)phone).unregisterForResendIncallMuteGemini(mHandler, Phone.GEMINI_SIM_1);
            //((GeminiPhone)phone).unregisterForResendIncallMuteGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForMmiInitiateGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForMmiInitiateGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForMmiCompleteGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForMmiCompleteGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForSuppServiceFailedGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForSuppServiceFailedGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForServiceStateChangedGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForServiceStateChangedGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).setOnPostDialCharacterGemini(null, EVENT_POST_DIAL_CHARACTER, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).setOnPostDialCharacterGemini(null, EVENT_POST_DIAL_CHARACTER2, null, Phone.GEMINI_SIM_2);

            /* MTK proprietary start */
            ((GeminiPhone)phone).unregisterForSpeechInfoGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForSpeechInfoGemini(mHandler,Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForVtStatusInfoGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForVtStatusInfoGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForVtRingInfoGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForVtRingInfoGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForCrssSuppServiceNotificationGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForCrssSuppServiceNotificationGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForSuppServiceNotificationGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForSuppServiceNotificationGemini(mHandler, Phone.GEMINI_SIM_2);
            ((GeminiPhone)phone).unregisterForVtReplaceDisconnectGemini(mHandler, Phone.GEMINI_SIM_1);
            ((GeminiPhone)phone).unregisterForVtReplaceDisconnectGemini(mHandler, Phone.GEMINI_SIM_2);
            /* MTK proprietary end */

            // for events supported only by CDMA phone
            if (FeatureOption.EVDO_DT_SUPPORT) {
            	  if (PhoneFactory.isDualTalkMode()) { 
                    int simId = PhoneFactory.getExternalModemSlot();
                    ((GeminiPhone)phone).unregisterForCallWaitingGemini(mHandler, simId); 
                    ((GeminiPhone)phone).unregisterForCdmaOtaStatusChangeGemini(mHandler, simId); 
                }  
            }
        } else {
        phone.unregisterForPreciseCallStateChanged(mHandler);
        phone.unregisterForDisconnect(mHandler);
        phone.unregisterForNewRingingConnection(mHandler);
        phone.unregisterForUnknownConnection(mHandler);
        phone.unregisterForIncomingRing(mHandler);
        phone.unregisterForRingbackTone(mHandler);
        phone.unregisterForInCallVoicePrivacyOn(mHandler);
        phone.unregisterForInCallVoicePrivacyOff(mHandler);
        phone.unregisterForDisplayInfo(mHandler);
        phone.unregisterForSignalInfo(mHandler);
        phone.unregisterForResendIncallMute(mHandler);
        phone.unregisterForMmiInitiate(mHandler);
        phone.unregisterForMmiComplete(mHandler);
        phone.unregisterForSuppServiceFailed(mHandler);
        phone.unregisterForServiceStateChanged(mHandler);
            /* MTK proprietary start */
            phone.unregisterForSpeechInfo(mHandler);
            phone.unregisterForVtStatusInfo(mHandler);
            phone.unregisterForVtRingInfo(mHandler);
            phone.unregisterForCrssSuppServiceNotification(mHandler);
            phone.unregisterForSuppServiceNotification(mHandler);
            phone.unregisterForVtReplaceDisconnect(mHandler);
            /* MTK proprietary end */

        // for events supported only by GSM and CDMA phone
        if (phone.getPhoneType() == Phone.PHONE_TYPE_GSM ||
                phone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            phone.setOnPostDialCharacter(null, EVENT_POST_DIAL_CHARACTER, null);
        }

        // for events supported only by CDMA phone
        if (phone.getPhoneType() == Phone.PHONE_TYPE_CDMA ){
            phone.unregisterForCdmaOtaStatusChange(mHandler);
            phone.unregisterForSubscriptionInfoReady(mHandler);
            phone.unregisterForCallWaiting(mHandler);
            phone.unregisterForEcmTimerReset(mHandler);
        }
      }
    }
    //MTK-END [mtk04070][111121][ALPS00093395]MTK modified


    /**
     * Answers a ringing or waiting call.
     *
     * Active call, if any, go on hold.
     * If active call can't be held, i.e., a background call of the same channel exists,
     * the active call will be hang up.
     *
     * Answering occurs asynchronously, and final notification occurs via
     * {@link #registerForPreciseCallStateChanged(android.os.Handler, int,
     * java.lang.Object) registerForPreciseCallStateChanged()}.
     *
     * @exception CallStateException when call is not ringing or waiting
     */
    public void acceptCall(Call ringingCall) throws CallStateException {
        Phone ringingPhone = ringingCall.getPhone();

        if (VDBG) {
            Log.d(LOG_TAG, "acceptCall(" +ringingCall + " from " + ringingCall.getPhone() + ")");
            Log.d(LOG_TAG, this.toString());
        }

        if ( hasActiveFgCall() ) {
            Phone activePhone = getActiveFgCall().getPhone();
            boolean hasBgCall = ! (activePhone.getBackgroundCall().isIdle());
            boolean sameChannel = (activePhone == ringingPhone);

            if (VDBG) {
                Log.d(LOG_TAG, "hasBgCall: "+ hasBgCall + "sameChannel:" + sameChannel);
            }

            if (sameChannel && hasBgCall) {
                getActiveFgCall().hangup();
            } else if (!sameChannel && !hasBgCall) {
                //Merge DualTalk code
                if (getActiveFgCallState().isDialing()) {
                    getActiveFgCall().hangup();
                }
                else {
                    /* Hang up the ringing call of active phone */
                    if (activePhone.getRingingCall().isRinging()) {
                       activePhone.getRingingCall().hangup();
                    }   

                    /* Switch active phone to held state */
                    activePhone.switchHoldingAndActive();
                }
            } else if (!sameChannel && hasBgCall) {
                getActiveFgCall().hangup();
            }
        }

        ringingPhone.acceptCall();

        if (VDBG) {
            Log.d(LOG_TAG, "End acceptCall(" +ringingCall + ")");
            Log.d(LOG_TAG, this.toString());
        }
    }

    /**
     * Reject (ignore) a ringing call. In GSM, this means UDUB
     * (User Determined User Busy). Reject occurs asynchronously,
     * and final notification occurs via
     * {@link #registerForPreciseCallStateChanged(android.os.Handler, int,
     * java.lang.Object) registerForPreciseCallStateChanged()}.
     *
     * @exception CallStateException when no call is ringing or waiting
     */
    public void rejectCall(Call ringingCall) throws CallStateException {
        if (VDBG) {
            Log.d(LOG_TAG, "rejectCall(" +ringingCall + ")");
            Log.d(LOG_TAG, this.toString());
        }

        Phone ringingPhone = ringingCall.getPhone();

        ringingPhone.rejectCall();

        if (VDBG) {
            Log.d(LOG_TAG, "End rejectCall(" +ringingCall + ")");
            Log.d(LOG_TAG, this.toString());
        }
    }

    /**
     * Places active call on hold, and makes held call active.
     * Switch occurs asynchronously and may fail.
     *
     * There are 4 scenarios
     * 1. only active call but no held call, aka, hold
     * 2. no active call but only held call, aka, unhold
     * 3. both active and held calls from same phone, aka, swap
     * 4. active and held calls from different phones, aka, phone swap
     *
     * Final notification occurs via
     * {@link #registerForPreciseCallStateChanged(android.os.Handler, int,
     * java.lang.Object) registerForPreciseCallStateChanged()}.
     *
     * @exception CallStateException if active call is ringing, waiting, or
     * dialing/alerting, or heldCall can't be active.
     * In these cases, this operation may not be performed.
     */
    public void switchHoldingAndActive(Call heldCall) throws CallStateException {
        Phone activePhone = null;
        Phone heldPhone = null;

        if (VDBG) {
            Log.d(LOG_TAG, "switchHoldingAndActive(" +heldCall + ")");
            Log.d(LOG_TAG, this.toString());
        }

        if (hasActiveFgCall()) {
            activePhone = getActiveFgCall().getPhone();
        }

        if (heldCall != null) {
            heldPhone = heldCall.getPhone();
        }

        if (activePhone != null) {
            activePhone.switchHoldingAndActive();
        }

        if (heldPhone != null && heldPhone != activePhone) {
            heldPhone.switchHoldingAndActive();
        }

        if (VDBG) {
            Log.d(LOG_TAG, "End switchHoldingAndActive(" +heldCall + ")");
            Log.d(LOG_TAG, this.toString());
        }
    }

    /**
     * Hangup foreground call and resume the specific background call
     *
     * Note: this is noop if there is no foreground call or the heldCall is null
     *
     * @param heldCall to become foreground
     * @throws CallStateException
     */
    public void hangupForegroundResumeBackground(Call heldCall) throws CallStateException {
        Phone foregroundPhone = null;
        Phone backgroundPhone = null;

        if (VDBG) {
            Log.d(LOG_TAG, "hangupForegroundResumeBackground(" +heldCall + ")");
            Log.d(LOG_TAG, this.toString());
        }

        if (hasActiveFgCall()) {
            foregroundPhone = getFgPhone();
            if (heldCall != null) {
                backgroundPhone = heldCall.getPhone();
                if (foregroundPhone == backgroundPhone) {
                    getActiveFgCall().hangup();
                } else {
                // the call to be hangup and resumed belongs to different phones
                    getActiveFgCall().hangup();
                    switchHoldingAndActive(heldCall);
                }
            }
        }

        if (VDBG) {
            Log.d(LOG_TAG, "End hangupForegroundResumeBackground(" +heldCall + ")");
            Log.d(LOG_TAG, this.toString());
        }
    }

    /**
     * Whether or not the phone can conference in the current phone
     * state--that is, one call holding and one call active.
     * @return true if the phone can conference; false otherwise.
     */
    public boolean canConference(Call heldCall) {
        Phone activePhone = null;
        Phone heldPhone = null;

        if (hasActiveFgCall()) {
            activePhone = getActiveFgCall().getPhone();
        }

        if (heldCall != null) {
            heldPhone = heldCall.getPhone();
        }

        //MTK-START [mtk04070][111121][ALPS00093395]MTK modified
        if (heldPhone != null && activePhone != null) {
        return heldPhone.getClass().equals(activePhone.getClass());
        } else {
            return false;
        }
        //MTK-END [mtk04070][111121][ALPS00093395]MTK modified
    }

    /**
     * Conferences holding and active. Conference occurs asynchronously
     * and may fail. Final notification occurs via
     * {@link #registerForPreciseCallStateChanged(android.os.Handler, int,
     * java.lang.Object) registerForPreciseCallStateChanged()}.
     *
     * @exception CallStateException if canConference() would return false.
     * In these cases, this operation may not be performed.
     */
    public void conference(Call heldCall) throws CallStateException {

        if (VDBG) {
            Log.d(LOG_TAG, "conference(" +heldCall + ")");
            Log.d(LOG_TAG, this.toString());
        }


        Phone fgPhone = getFgPhone();
        if (fgPhone instanceof SipPhone) {
            ((SipPhone) fgPhone).conference(heldCall);
        } else if (canConference(heldCall)) {
            fgPhone.conference();
        } else {
            throw(new CallStateException("Can't conference foreground and selected background call"));
        }

        if (VDBG) {
            Log.d(LOG_TAG, "End conference(" +heldCall + ")");
            Log.d(LOG_TAG, this.toString());
        }

    }

    /**
     * Initiate a new voice connection. This happens asynchronously, so you
     * cannot assume the audio path is connected (or a call index has been
     * assigned) until PhoneStateChanged notification has occurred.
     *
     * @exception CallStateException if a new outgoing call is not currently
     * possible because no more call slots exist or a call exists that is
     * dialing, alerting, ringing, or waiting.  Other errors are
     * handled asynchronously.
     */
    public Connection dial(Phone phone, String dialString) throws CallStateException {
        Phone basePhone = getPhoneBase(phone);
        Connection result;

        if (VDBG) {
            Log.d(LOG_TAG, " dial(" + basePhone + ", "+ dialString + ")");
            Log.d(LOG_TAG, this.toString());
        }

        //MTK-START [mtk04070][111121][ALPS00093395]Add a parameter - dialString
        if (!canDial(phone, dialString)) {
            throw new CallStateException("cannot dial in current state");
        }
        //MTK-END [mtk04070][111121][ALPS00093395]Add a parameter - dialString

        if ( hasActiveFgCall() ) {
            Phone activePhone = getActiveFgCall().getPhone();
            boolean hasBgCall = !(activePhone.getBackgroundCall().isIdle());

            if (DBG) {
                Log.d(LOG_TAG, "hasBgCall: "+ hasBgCall + " sameChannel:" + (activePhone == basePhone));
            }

            if (activePhone != basePhone) {
                if (hasBgCall) {
                    Log.d(LOG_TAG, "Hangup");
                    getActiveFgCall().hangup();
                } else {
                    Log.d(LOG_TAG, "Switch");
                    activePhone.switchHoldingAndActive();
                }
            }
        }

        result = basePhone.dial(dialString);

        if (VDBG) {
            Log.d(LOG_TAG, "End dial(" + basePhone + ", "+ dialString + ")");
            Log.d(LOG_TAG, this.toString());
        }

        return result;
    }

    /**
     * Initiate a new voice connection. This happens asynchronously, so you
     * cannot assume the audio path is connected (or a call index has been
     * assigned) until PhoneStateChanged notification has occurred.
     *
     * @exception CallStateException if a new outgoing call is not currently
     * possible because no more call slots exist or a call exists that is
     * dialing, alerting, ringing, or waiting.  Other errors are
     * handled asynchronously.
     */
    public Connection dial(Phone phone, String dialString, UUSInfo uusInfo) throws CallStateException {
        return phone.dial(dialString, uusInfo);
    }

    /**
     * clear disconnect connection for each phone
     */
    public void clearDisconnected() {
        for(Phone phone : mPhones) {
            phone.clearDisconnected();
        }
    }

    //MTK-START [mtk04070][111121][ALPS00093395]MTK modified
    /**
     * Phone can make a call only if ALL of the following are true:
     *        - Phone is not powered off
     *        - There's no incoming or waiting call
     *        - There's available call slot in either foreground or background
     *        - The foreground call is ACTIVE or IDLE or DISCONNECTED.
     *          (We mainly need to make sure it *isn't* DIALING or ALERTING.)
     * @param phone
     * @return true if the phone can make a new call
     */
    private boolean canDial(Phone phone, String dialString) {
        int serviceState = phone.getServiceState().getState();
        boolean hasRingingCall = hasActiveRingingCall();
        boolean hasActiveCall = hasActiveFgCall();
        boolean hasHoldingCall = hasActiveBgCall();
        boolean allLinesTaken = hasActiveCall && hasHoldingCall;
        Call.State fgCallState = getActiveFgCallState();

        boolean result = (serviceState != ServiceState.STATE_POWER_OFF
                && !(hasRingingCall && !isInCallMmiCommands(dialString))
                && !(allLinesTaken && !isInCallMmiCommands(dialString))
                && ((fgCallState == Call.State.ACTIVE)
                    || (fgCallState == Call.State.IDLE)
                    || (fgCallState == Call.State.DISCONNECTED)
                    || (fgCallState == Call.State.ALERTING && isInCallMmiCommands(dialString))));

        if (result == false) {
            Log.d(LOG_TAG, "canDial serviceState=" + serviceState
                            + " hasRingingCall=" + hasRingingCall
                            + " hasActiveCall=" + hasActiveCall
                            + " hasHoldingCall=" + hasHoldingCall
                            + " allLinesTaken=" + allLinesTaken
                            + " fgCallState=" + fgCallState);
        }
        return result;
    }
    //MTK-END [mtk04070][111121][ALPS00093395]MTK modified

    /**
     * Whether or not the phone can do explicit call transfer in the current
     * phone state--that is, one call holding and one call active.
     * @return true if the phone can do explicit call transfer; false otherwise.
     */
    public boolean canTransfer(Call heldCall) {
        Phone activePhone = null;
        Phone heldPhone = null;

        if (hasActiveFgCall()) {
            activePhone = getActiveFgCall().getPhone();
        }

        if (heldCall != null) {
            heldPhone = heldCall.getPhone();
        }

        return (heldPhone == activePhone && activePhone.canTransfer());
    }

    /**
     * Connects the held call and active call
     * Disconnects the subscriber from both calls
     *
     * Explicit Call Transfer occurs asynchronously
     * and may fail. Final notification occurs via
     * {@link #registerForPreciseCallStateChanged(android.os.Handler, int,
     * java.lang.Object) registerForPreciseCallStateChanged()}.
     *
     * @exception CallStateException if canTransfer() would return false.
     * In these cases, this operation may not be performed.
     */
    public void explicitCallTransfer(Call heldCall) throws CallStateException {
        if (VDBG) {
            Log.d(LOG_TAG, " explicitCallTransfer(" + heldCall + ")");
            Log.d(LOG_TAG, this.toString());
        }

        if (canTransfer(heldCall)) {
            heldCall.getPhone().explicitCallTransfer();
        }

        if (VDBG) {
            Log.d(LOG_TAG, "End explicitCallTransfer(" + heldCall + ")");
            Log.d(LOG_TAG, this.toString());
        }

    }

    /**
     * Returns a list of MMI codes that are pending for a phone. (They have initiated
     * but have not yet completed).
     * Presently there is only ever one.
     *
     * Use <code>registerForMmiInitiate</code>
     * and <code>registerForMmiComplete</code> for change notification.
     * @return null if phone doesn't have or support mmi code
     */
    public List<? extends MmiCode> getPendingMmiCodes(Phone phone) {
        Log.e(LOG_TAG, "getPendingMmiCodes not implemented");
        return null;
    }

    /**
     * Sends user response to a USSD REQUEST message.  An MmiCode instance
     * representing this response is sent to handlers registered with
     * registerForMmiInitiate.
     *
     * @param ussdMessge    Message to send in the response.
     * @return false if phone doesn't support ussd service
     */
    public boolean sendUssdResponse(Phone phone, String ussdMessge) {
        Log.e(LOG_TAG, "sendUssdResponse not implemented");
        return false;
    }

    /**
     * Mutes or unmutes the microphone for the active call. The microphone
     * is automatically unmuted if a call is answered, dialed, or resumed
     * from a holding state.
     *
     * @param muted true to mute the microphone,
     * false to activate the microphone.
     */

    public void setMute(boolean muted) {
        if (VDBG) {
            Log.d(LOG_TAG, " setMute(" + muted + ")");
            Log.d(LOG_TAG, this.toString());
        }

        if (hasActiveFgCall()) {
            getActiveFgCall().getPhone().setMute(muted);
        }

        if (VDBG) {
            Log.d(LOG_TAG, "End setMute(" + muted + ")");
            Log.d(LOG_TAG, this.toString());
        }
    }

    /**
     * Gets current mute status. Use
     * {@link #registerForPreciseCallStateChanged(android.os.Handler, int,
     * java.lang.Object) registerForPreciseCallStateChanged()}
     * as a change notifcation, although presently phone state changed is not
     * fired when setMute() is called.
     *
     * @return true is muting, false is unmuting
     */
    public boolean getMute() {
        if (hasActiveFgCall()) {
            return getActiveFgCall().getPhone().getMute();
        } else if (hasActiveBgCall()) {
            return getFirstActiveBgCall().getPhone().getMute();
        }
        return false;
    }

    /**
     * Enables or disables echo suppression.
     */
    public void setEchoSuppressionEnabled(boolean enabled) {
        if (VDBG) {
            Log.d(LOG_TAG, " setEchoSuppression(" + enabled + ")");
            //Solve [ALPS00336628]JE issue.
            //Log.d(LOG_TAG, this.toString());
        }

        if (hasActiveFgCall()) {
            getActiveFgCall().getPhone().setEchoSuppressionEnabled(enabled);
        }

        if (VDBG) {
            Log.d(LOG_TAG, "End setEchoSuppression(" + enabled + ")");
            //Solve [ALPS00336628]JE issue.
            //Log.d(LOG_TAG, this.toString());
        }
    }

    /**
     * Play a DTMF tone on the active call.
     *
     * @param c should be one of 0-9, '*' or '#'. Other values will be
     * silently ignored.
     * @return false if no active call or the active call doesn't support
     *         dtmf tone
     */
    public boolean sendDtmf(char c) {
        boolean result = false;

        if (VDBG) {
            Log.d(LOG_TAG, " sendDtmf(" + c + ")");
            Log.d(LOG_TAG, this.toString());
        }

        if (hasActiveFgCall()) {
            getActiveFgCall().getPhone().sendDtmf(c);
            result = true;
        }

        if (VDBG) {
            Log.d(LOG_TAG, "End sendDtmf(" + c + ")");
            Log.d(LOG_TAG, this.toString());
        }
        return result;
    }

    /**
     * Start to paly a DTMF tone on the active call.
     * or there is a playing DTMF tone.
     * @param c should be one of 0-9, '*' or '#'. Other values will be
     * silently ignored.
     *
     * @return false if no active call or the active call doesn't support
     *         dtmf tone
     */
    public boolean startDtmf(char c) {
        boolean result = false;

        if (VDBG) {
            Log.d(LOG_TAG, " startDtmf(" + c + ")");
            Log.d(LOG_TAG, this.toString());
        }

        if (hasActiveFgCall()) {
            getActiveFgCall().getPhone().startDtmf(c);
            result = true;
            /* Solve ALPS00281513, to avoid DTMF start request is sent, but stop request is ignore due to active is held, mtk04070, 20120515
               The following scenario may be happened, all hold requests will be cancelled, so need this flag to send stop request.
                 Hold active call(s) request
                 DTMF start request is sent(DTMF flag in ril is set to 1)
                 Hold command return OK
                 DTMF stop request is ignore due to no active call(DTMF flag is still 1)
            */
            Log.d(LOG_TAG, "dtmfRequestIsStarted = true");
            dtmfRequestIsStarted = true;
        }

        if (VDBG) {
            Log.d(LOG_TAG, "End startDtmf(" + c + ")");
            Log.d(LOG_TAG, this.toString());
        }

        return result;
    }

    /**
     * Stop the playing DTMF tone. Ignored if there is no playing DTMF
     * tone or no active call.
     */
    public void stopDtmf() {
        if (VDBG) {
            Log.d(LOG_TAG, " stopDtmf()" );
            Log.d(LOG_TAG, this.toString());
        }

        /* Solve ALPS00281513, to avoid DTMF start request is sent, but stop request is ignore due to active is held, mtk04070, 20120515 */
        if (hasActiveFgCall() || dtmfRequestIsStarted) {
            getFgPhone().stopDtmf();
            dtmfRequestIsStarted = false;
            Log.d(LOG_TAG, "dtmfRequestIsStarted = false");
        }

        if (VDBG) {
            Log.d(LOG_TAG, "End stopDtmf()");
            Log.d(LOG_TAG, this.toString());
        }
    }

    /**
     * send burst DTMF tone, it can send the string as single character or multiple character
     * ignore if there is no active call or not valid digits string.
     * Valid digit means only includes characters ISO-LATIN characters 0-9, *, #
     * The difference between sendDtmf and sendBurstDtmf is sendDtmf only sends one character,
     * this api can send single character and multiple character, also, this api has response
     * back to caller.
     *
     * @param dtmfString is string representing the dialing digit(s) in the active call
     * @param on the DTMF ON length in milliseconds, or 0 for default
     * @param off the DTMF OFF length in milliseconds, or 0 for default
     * @param onComplete is the callback message when the action is processed by BP
     *
     */
    public boolean sendBurstDtmf(String dtmfString, int on, int off, Message onComplete) {
        if (hasActiveFgCall()) {
            getActiveFgCall().getPhone().sendBurstDtmf(dtmfString, on, off, onComplete);
            return true;
        }
        return false;
    }

    /**
     * Notifies when a voice connection has disconnected, either due to local
     * or remote hangup or error.
     *
     *  Messages received from this will have the following members:<p>
     *  <ul><li>Message.obj will be an AsyncResult</li>
     *  <li>AsyncResult.userObj = obj</li>
     *  <li>AsyncResult.result = a Connection object that is
     *  no longer connected.</li></ul>
     */
    public void registerForDisconnect(Handler h, int what, Object obj) {
        mDisconnectRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregisters for voice disconnection notification.
     * Extraneous calls are tolerated silently
     */
    public void unregisterForDisconnect(Handler h){
        mDisconnectRegistrants.remove(h);
    }

    /**
     * Register for getting notifications for change in the Call State {@link Call.State}
     * This is called PreciseCallState because the call state is more precise than the
     * {@link Phone.State} which can be obtained using the {@link PhoneStateListener}
     *
     * Resulting events will have an AsyncResult in <code>Message.obj</code>.
     * AsyncResult.userData will be set to the obj argument here.
     * The <em>h</em> parameter is held only by a weak reference.
     */
    public void registerForPreciseCallStateChanged(Handler h, int what, Object obj){
        mPreciseCallStateRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregisters for voice call state change notifications.
     * Extraneous calls are tolerated silently.
     */
    public void unregisterForPreciseCallStateChanged(Handler h){
        mPreciseCallStateRegistrants.remove(h);
    }

    /**
     * Notifies when a previously untracked non-ringing/waiting connection has appeared.
     * This is likely due to some other entity (eg, SIM card application) initiating a call.
     */
    public void registerForUnknownConnection(Handler h, int what, Object obj){
        mUnknownConnectionRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregisters for unknown connection notifications.
     */
    public void unregisterForUnknownConnection(Handler h){
        mUnknownConnectionRegistrants.remove(h);
    }


    /**
     * Notifies when a new ringing or waiting connection has appeared.<p>
     *
     *  Messages received from this:
     *  Message.obj will be an AsyncResult
     *  AsyncResult.userObj = obj
     *  AsyncResult.result = a Connection. <p>
     *  Please check Connection.isRinging() to make sure the Connection
     *  has not dropped since this message was posted.
     *  If Connection.isRinging() is true, then
     *   Connection.getCall() == Phone.getRingingCall()
     */
    public void registerForNewRingingConnection(Handler h, int what, Object obj){
        mNewRingingConnectionRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregisters for new ringing connection notification.
     * Extraneous calls are tolerated silently
     */

    public void unregisterForNewRingingConnection(Handler h){
        mNewRingingConnectionRegistrants.remove(h);
    }

    /**
     * Notifies when an incoming call rings.<p>
     *
     *  Messages received from this:
     *  Message.obj will be an AsyncResult
     *  AsyncResult.userObj = obj
     *  AsyncResult.result = a Connection. <p>
     */
    public void registerForIncomingRing(Handler h, int what, Object obj){
        mIncomingRingRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregisters for ring notification.
     * Extraneous calls are tolerated silently
     */

    public void unregisterForIncomingRing(Handler h){
        mIncomingRingRegistrants.remove(h);
    }

    /**
     * Notifies when out-band ringback tone is needed.<p>
     *
     *  Messages received from this:
     *  Message.obj will be an AsyncResult
     *  AsyncResult.userObj = obj
     *  AsyncResult.result = boolean, true to start play ringback tone
     *                       and false to stop. <p>
     */
    public void registerForRingbackTone(Handler h, int what, Object obj){
        mRingbackToneRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregisters for ringback tone notification.
     */

    public void unregisterForRingbackTone(Handler h){
        mRingbackToneRegistrants.remove(h);
    }

    /**
     * Registers the handler to reset the uplink mute state to get
     * uplink audio.
     */
    public void registerForResendIncallMute(Handler h, int what, Object obj){
        mResendIncallMuteRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregisters for resend incall mute notifications.
     */
    public void unregisterForResendIncallMute(Handler h){
        mResendIncallMuteRegistrants.remove(h);
    }

    /**
     * Register for notifications of initiation of a new MMI code request.
     * MMI codes for GSM are discussed in 3GPP TS 22.030.<p>
     *
     * Example: If Phone.dial is called with "*#31#", then the app will
     * be notified here.<p>
     *
     * The returned <code>Message.obj</code> will contain an AsyncResult.
     *
     * <code>obj.result</code> will be an "MmiCode" object.
     */
    public void registerForMmiInitiate(Handler h, int what, Object obj){
        mMmiInitiateRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregisters for new MMI initiate notification.
     * Extraneous calls are tolerated silently
     */
    public void unregisterForMmiInitiate(Handler h){
        mMmiInitiateRegistrants.remove(h);
    }

    /**
     * Register for notifications that an MMI request has completed
     * its network activity and is in its final state. This may mean a state
     * of COMPLETE, FAILED, or CANCELLED.
     *
     * <code>Message.obj</code> will contain an AsyncResult.
     * <code>obj.result</code> will be an "MmiCode" object
     */
    public void registerForMmiComplete(Handler h, int what, Object obj){
        mMmiCompleteRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregisters for MMI complete notification.
     * Extraneous calls are tolerated silently
     */
    public void unregisterForMmiComplete(Handler h){
        mMmiCompleteRegistrants.remove(h);
    }

    /**
     * Registration point for Ecm timer reset
     * @param h handler to notify
     * @param what user-defined message code
     * @param obj placed in Message.obj
     */
    public void registerForEcmTimerReset(Handler h, int what, Object obj){
        mEcmTimerResetRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregister for notification for Ecm timer reset
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForEcmTimerReset(Handler h){
        mEcmTimerResetRegistrants.remove(h);
    }

    /**
     * Register for ServiceState changed.
     * Message.obj will contain an AsyncResult.
     * AsyncResult.result will be a ServiceState instance
     */
    public void registerForServiceStateChanged(Handler h, int what, Object obj){
        mServiceStateChangedRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregisters for ServiceStateChange notification.
     * Extraneous calls are tolerated silently
     */
    public void unregisterForServiceStateChanged(Handler h){
        mServiceStateChangedRegistrants.remove(h);
    }

    /**
     * Register for notifications when a supplementary service attempt fails.
     * Message.obj will contain an AsyncResult.
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */
    public void registerForSuppServiceFailed(Handler h, int what, Object obj){
        mSuppServiceFailedRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregister for notifications when a supplementary service attempt fails.
     * Extraneous calls are tolerated silently
     *
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForSuppServiceFailed(Handler h){
        mSuppServiceFailedRegistrants.remove(h);
    }

    /**
     * Register for notifications when a sInCall VoicePrivacy is enabled
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */
    public void registerForInCallVoicePrivacyOn(Handler h, int what, Object obj){
        mInCallVoicePrivacyOnRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregister for notifications when a sInCall VoicePrivacy is enabled
     *
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForInCallVoicePrivacyOn(Handler h){
        mInCallVoicePrivacyOnRegistrants.remove(h);
    }

    /**
     * Register for notifications when a sInCall VoicePrivacy is disabled
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */
    public void registerForInCallVoicePrivacyOff(Handler h, int what, Object obj){
        mInCallVoicePrivacyOffRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregister for notifications when a sInCall VoicePrivacy is disabled
     *
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForInCallVoicePrivacyOff(Handler h){
        mInCallVoicePrivacyOffRegistrants.remove(h);
    }

    /**
     * Register for notifications when CDMA call waiting comes
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */
    public void registerForCallWaiting(Handler h, int what, Object obj){
        mCallWaitingRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregister for notifications when CDMA Call waiting comes
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForCallWaiting(Handler h){
        mCallWaitingRegistrants.remove(h);
    }


    /**
     * Register for signal information notifications from the network.
     * Message.obj will contain an AsyncResult.
     * AsyncResult.result will be a SuppServiceNotification instance.
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */

    public void registerForSignalInfo(Handler h, int what, Object obj){
        mSignalInfoRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregisters for signal information notifications.
     * Extraneous calls are tolerated silently
     *
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForSignalInfo(Handler h){
        mSignalInfoRegistrants.remove(h);
    }

    /**
     * Register for display information notifications from the network.
     * Message.obj will contain an AsyncResult.
     * AsyncResult.result will be a SuppServiceNotification instance.
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */
    public void registerForDisplayInfo(Handler h, int what, Object obj){
        mDisplayInfoRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregisters for display information notifications.
     * Extraneous calls are tolerated silently
     *
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForDisplayInfo(Handler h) {
        mDisplayInfoRegistrants.remove(h);
    }

    /**
     * Register for notifications when CDMA OTA Provision status change
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */
    public void registerForCdmaOtaStatusChange(Handler h, int what, Object obj){
        mCdmaOtaStatusChangeRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregister for notifications when CDMA OTA Provision status change
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForCdmaOtaStatusChange(Handler h){
        mCdmaOtaStatusChangeRegistrants.remove(h);
    }

    /**
     * Registration point for subscription info ready
     * @param h handler to notify
     * @param what what code of message when delivered
     * @param obj placed in Message.obj
     */
    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj){
        mSubscriptionInfoReadyRegistrants.addUnique(h, what, obj);
    }

    /**
     * Unregister for notifications for subscription info
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForSubscriptionInfoReady(Handler h){
        mSubscriptionInfoReadyRegistrants.remove(h);
    }

    /**
     * Sets an event to be fired when the telephony system processes
     * a post-dial character on an outgoing call.<p>
     *
     * Messages of type <code>what</code> will be sent to <code>h</code>.
     * The <code>obj</code> field of these Message's will be instances of
     * <code>AsyncResult</code>. <code>Message.obj.result</code> will be
     * a Connection object.<p>
     *
     * Message.arg1 will be the post dial character being processed,
     * or 0 ('\0') if end of string.<p>
     *
     * If Connection.getPostDialState() == WAIT,
     * the application must call
     * {@link com.android.internal.telephony.Connection#proceedAfterWaitChar()
     * Connection.proceedAfterWaitChar()} or
     * {@link com.android.internal.telephony.Connection#cancelPostDial()
     * Connection.cancelPostDial()}
     * for the telephony system to continue playing the post-dial
     * DTMF sequence.<p>
     *
     * If Connection.getPostDialState() == WILD,
     * the application must call
     * {@link com.android.internal.telephony.Connection#proceedAfterWildChar
     * Connection.proceedAfterWildChar()}
     * or
     * {@link com.android.internal.telephony.Connection#cancelPostDial()
     * Connection.cancelPostDial()}
     * for the telephony system to continue playing the
     * post-dial DTMF sequence.<p>
     *
     */
    public void registerForPostDialCharacter(Handler h, int what, Object obj){
        mPostDialCharacterRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForPostDialCharacter(Handler h){
        mPostDialCharacterRegistrants.remove(h);
    }

    /* APIs to access foregroudCalls, backgroudCalls, and ringingCalls
     * 1. APIs to access list of calls
     * 2. APIs to check if any active call, which has connection other than
     * disconnected ones, pleaser refer to Call.isIdle()
     * 3. APIs to return first active call
     * 4. APIs to return the connections of first active call
     * 5. APIs to return other property of first active call
     */

    /**
     * @return list of all ringing calls
     */
    public List<Call> getRingingCalls() {
        return Collections.unmodifiableList(mRingingCalls);
    }

    /**
     * @return list of all foreground calls
     */
    public List<Call> getForegroundCalls() {
        return Collections.unmodifiableList(mForegroundCalls);
    }

    /**
     * @return list of all background calls
     */
    public List<Call> getBackgroundCalls() {
        return Collections.unmodifiableList(mBackgroundCalls);
    }

    /**
     * Return true if there is at least one active foreground call
     */
    public boolean hasActiveFgCall() {
        return (getFirstActiveCall(mForegroundCalls) != null);
    }

    /**
     * Return true if there is at least one active background call
     */
    public boolean hasActiveBgCall() {
        // TODO since hasActiveBgCall may get called often
        // better to cache it to improve performance
        return (getFirstActiveCall(mBackgroundCalls) != null);
    }

    /**
     * Return true if there is at least one active ringing call
     *
     */
    public boolean hasActiveRingingCall() {
        return (getFirstActiveCall(mRingingCalls) != null);
    }

    /**
     * return the active foreground call from foreground calls
     *
     * Active call means the call is NOT in Call.State.IDLE
     *
     * 1. If there is active foreground call, return it
     * 2. If there is no active foreground call, return the
     *    foreground call associated with default phone, which state is IDLE.
     * 3. If there is no phone registered at all, return null.
     *
     */
    public Call getActiveFgCall() {
        Call call = getFirstNonIdleCall(mForegroundCalls);
        if (call == null) {
            call = (mDefaultPhone == null)
                    ? null
                    : mDefaultPhone.getForegroundCall();
        }
        return call;
    }

    // Returns the first call that is not in IDLE state. If both active calls
    // and disconnecting/disconnected calls exist, return the first active call.
    private Call getFirstNonIdleCall(List<Call> calls) {
        Call result = null;
        for (Call call : calls) {
            if (!call.isIdle()) {
                return call;
            } else if (call.getState() != Call.State.IDLE) {
                if (result == null) result = call;
            }
        }
        return result;
    }

    /**
     * return one active background call from background calls
     *
     * Active call means the call is NOT idle defined by Call.isIdle()
     *
     * 1. If there is only one active background call, return it
     * 2. If there is more than one active background call, return the first one
     * 3. If there is no active background call, return the background call
     *    associated with default phone, which state is IDLE.
     * 4. If there is no background call at all, return null.
     *
     * Complete background calls list can be get by getBackgroundCalls()
     */
    public Call getFirstActiveBgCall() {
        Call call = getFirstNonIdleCall(mBackgroundCalls);
        if (call == null) {
            call = (mDefaultPhone == null)
                    ? null
                    : mDefaultPhone.getBackgroundCall();
        }
        return call;
    }

    /**
     * return one active ringing call from ringing calls
     *
     * Active call means the call is NOT idle defined by Call.isIdle()
     *
     * 1. If there is only one active ringing call, return it
     * 2. If there is more than one active ringing call, return the first one
     * 3. If there is no active ringing call, return the ringing call
     *    associated with default phone, which state is IDLE.
     * 4. If there is no ringing call at all, return null.
     *
     * Complete ringing calls list can be get by getRingingCalls()
     */
    public Call getFirstActiveRingingCall() {
        Call call = getFirstNonIdleCall(mRingingCalls);
        if (call == null) {
            call = (mDefaultPhone == null)
                    ? null
                    : mDefaultPhone.getRingingCall();
        }
        return call;
    }

    /**
     * @return the state of active foreground call
     * return IDLE if there is no active foreground call
     */
    public Call.State getActiveFgCallState() {
        Call fgCall = getActiveFgCall();

        if (fgCall != null) {
            return fgCall.getState();
        }

        return Call.State.IDLE;
    }

    /**
     * @return the connections of active foreground call
     * return empty list if there is no active foreground call
     */
    public List<Connection> getFgCallConnections() {
        Call fgCall = getActiveFgCall();
        if ( fgCall != null) {
            return fgCall.getConnections();
        }
        return emptyConnections;
    }

    /**
     * @return the connections of active background call
     * return empty list if there is no active background call
     */
    public List<Connection> getBgCallConnections() {
        Call bgCall = getFirstActiveBgCall();
        if ( bgCall != null) {
            return bgCall.getConnections();
        }
        return emptyConnections;
    }

    /**
     * @return the latest connection of active foreground call
     * return null if there is no active foreground call
     */
    public Connection getFgCallLatestConnection() {
        Call fgCall = getActiveFgCall();
        if ( fgCall != null) {
            return fgCall.getLatestConnection();
        }
        return null;
    }

    /**
     * @return true if there is at least one Foreground call in disconnected state
     */
    public boolean hasDisconnectedFgCall() {
        return (getFirstCallOfState(mForegroundCalls, Call.State.DISCONNECTED) != null);
    }

    /**
     * @return true if there is at least one background call in disconnected state
     */
    public boolean hasDisconnectedBgCall() {
        return (getFirstCallOfState(mBackgroundCalls, Call.State.DISCONNECTED) != null);
    }

    /**
     * @return the first active call from a call list
     */
    private  Call getFirstActiveCall(ArrayList<Call> calls) {
        for (Call call : calls) {
            if (!call.isIdle()) {
                return call;
            }
        }
        return null;
    }

    /**
     * @return the first call in a the Call.state from a call list
     */
    private Call getFirstCallOfState(ArrayList<Call> calls, Call.State state) {
        for (Call call : calls) {
            if (call.getState() == state) {
                return call;
            }
        }
        return null;
    }


    private boolean hasMoreThanOneRingingCall() {
        int count = 0;
        for (Call call : mRingingCalls) {
            if (call.getState().isRinging()) {
                if (++count > 1) return true;
            }
        }
        return false;
    }

    private Handler mHandler = new Handler() {

        //MTK-START [mtk04070][111121][ALPS00093395]MTK modified
        @Override
        public void handleMessage(Message msg) {

            if (VDBG) Log.d(LOG_TAG, " handleMessage msgid:" + msg.what);
            
            switch (msg.what) {
                case EVENT_DISCONNECT:
                case EVENT_DISCONNECT2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_DISCONNECT)");
                    if(EVENT_DISCONNECT == msg.what) {
                    mDisconnectRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mDisconnectRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    handle3GSwitchLock();
                    break;
                case EVENT_PRECISE_CALL_STATE_CHANGED:
                case EVENT_PRECISE_CALL_STATE_CHANGED2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_PRECISE_CALL_STATE_CHANGED)");
                    if(EVENT_PRECISE_CALL_STATE_CHANGED == msg.what) {
                    mPreciseCallStateRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mPreciseCallStateRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    handle3GSwitchLock();
                    break;
                case EVENT_NEW_RINGING_CONNECTION:
                case EVENT_NEW_RINGING_CONNECTION2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_NEW_RINGING_CONNECTION)");
                    if ((FeatureOption.MTK_DT_SUPPORT == false) && 
                        (getActiveFgCallState().isDialing() || hasMoreThanOneRingingCall()) ) {
                        Connection c = (Connection) ((AsyncResult) msg.obj).result;
                        try {
                             Log.d(LOG_TAG, "silently drop incoming call: " + c.getCall());
                             c.getCall().hangup();
                        } catch (CallStateException e) {
                             Log.w(LOG_TAG, "new ringing connection", e);
                        }
                    } else {
                        if(EVENT_NEW_RINGING_CONNECTION == msg.what) {
                        mNewRingingConnectionRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        } else {
                            mNewRingingConnectionRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                        }
                    }
                    break;
                case EVENT_UNKNOWN_CONNECTION:
                case EVENT_UNKNOWN_CONNECTION2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_UNKNOWN_CONNECTION)");
                    if(EVENT_UNKNOWN_CONNECTION == msg.what) {
                    mUnknownConnectionRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mUnknownConnectionRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_INCOMING_RING:
                case EVENT_INCOMING_RING2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_INCOMING_RING)");
                    // The event may come from RIL who's not aware of an ongoing fg call
                    if (!hasActiveFgCall()) {
                        if(EVENT_INCOMING_RING == msg.what) {
                        mIncomingRingRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        } else {
                            mIncomingRingRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                        }
                    }
                    break;
                case EVENT_RINGBACK_TONE:
                case EVENT_RINGBACK_TONE2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_RINGBACK_TONE)");
                    if(EVENT_RINGBACK_TONE == msg.what) {
                    mRingbackToneRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mRingbackToneRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_IN_CALL_VOICE_PRIVACY_ON:
                case EVENT_IN_CALL_VOICE_PRIVACY_ON2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_IN_CALL_VOICE_PRIVACY_ON)");
                    if(EVENT_IN_CALL_VOICE_PRIVACY_ON == msg.what) {
                    mInCallVoicePrivacyOnRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mInCallVoicePrivacyOnRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_IN_CALL_VOICE_PRIVACY_OFF:
                case EVENT_IN_CALL_VOICE_PRIVACY_OFF2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_IN_CALL_VOICE_PRIVACY_OFF)");
                    if(EVENT_IN_CALL_VOICE_PRIVACY_OFF == msg.what) {
                    mInCallVoicePrivacyOffRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mInCallVoicePrivacyOffRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_CALL_WAITING:
                case EVENT_CALL_WAITING2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_CALL_WAITING)");
                    if(EVENT_CALL_WAITING == msg.what) {
                    mCallWaitingRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mCallWaitingRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_DISPLAY_INFO:
                case EVENT_DISPLAY_INFO2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_DISPLAY_INFO)");
                    if(EVENT_DISPLAY_INFO == msg.what) {
                    mDisplayInfoRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mDisplayInfoRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_SIGNAL_INFO:
                case EVENT_SIGNAL_INFO2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_SIGNAL_INFO)");
                    if(EVENT_SIGNAL_INFO == msg.what) {
                    mSignalInfoRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mSignalInfoRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_CDMA_OTA_STATUS_CHANGE:
                case EVENT_CDMA_OTA_STATUS_CHANGE2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_CDMA_OTA_STATUS_CHANGE)");
                    if(EVENT_CDMA_OTA_STATUS_CHANGE == msg.what) {
                    mCdmaOtaStatusChangeRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mCdmaOtaStatusChangeRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_RESEND_INCALL_MUTE:
                case EVENT_RESEND_INCALL_MUTE2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_RESEND_INCALL_MUTE)");
                    if(EVENT_RESEND_INCALL_MUTE == msg.what) {
                    mResendIncallMuteRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mResendIncallMuteRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_MMI_INITIATE:
                case EVENT_MMI_INITIATE2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_MMI_INITIATE)");
                    if(EVENT_MMI_INITIATE == msg.what) {
                    mMmiInitiateRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mMmiInitiateRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_MMI_COMPLETE:
                case EVENT_MMI_COMPLETE2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_MMI_COMPLETE)");
                    if(EVENT_MMI_COMPLETE == msg.what) {
                    mMmiCompleteRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mMmiCompleteRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_ECM_TIMER_RESET:
                case EVENT_ECM_TIMER_RESET2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_ECM_TIMER_RESET)");
                    if(EVENT_ECM_TIMER_RESET == msg.what) {
                    mEcmTimerResetRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mEcmTimerResetRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_SUBSCRIPTION_INFO_READY:
                case EVENT_SUBSCRIPTION_INFO_READY2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_SUBSCRIPTION_INFO_READY)");
                    if(EVENT_SUBSCRIPTION_INFO_READY == msg.what) {
                    mSubscriptionInfoReadyRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mSubscriptionInfoReadyRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_SUPP_SERVICE_FAILED:
                case EVENT_SUPP_SERVICE_FAILED2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_SUPP_SERVICE_FAILED)");
                    if(EVENT_SUPP_SERVICE_FAILED == msg.what) {
                    mSuppServiceFailedRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mSuppServiceFailedRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_SERVICE_STATE_CHANGED:
                case EVENT_SERVICE_STATE_CHANGED2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_SERVICE_STATE_CHANGED)");
                    if(EVENT_SERVICE_STATE_CHANGED == msg.what) {
                    mServiceStateChangedRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mServiceStateChangedRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_POST_DIAL_CHARACTER:
                case EVENT_POST_DIAL_CHARACTER2:
                    // we need send the character that is being processed in msg.arg1
                    // so can't use notifyRegistrants()
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_POST_DIAL_CHARACTER)");
                    if(EVENT_POST_DIAL_CHARACTER == msg.what) {
                    for(int i=0; i < mPostDialCharacterRegistrants.size(); i++) {
                        Message notifyMsg;
                        notifyMsg = ((Registrant)mPostDialCharacterRegistrants.get(i)).messageForRegistrant();
                        notifyMsg.obj = msg.obj;
                        notifyMsg.arg1 = msg.arg1;
                        notifyMsg.sendToTarget();
                    }
                    } else {
                        for(int i=0; i < mPostDialCharacterRegistrants2.size(); i++) {
                            Message notifyMsg;
                            notifyMsg = ((Registrant)mPostDialCharacterRegistrants2.get(i)).messageForRegistrant();
                            notifyMsg.obj = msg.obj;
                            notifyMsg.arg1 = msg.arg1;
                            notifyMsg.sendToTarget();
                        }                        
                    }
                    break;
                /* MTK proprietary start */
                case EVENT_SPEECH_INFO:
                case EVENT_SPEECH_INFO2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_SPEECH_INFO)");
                    AsyncResult ar = (AsyncResult) msg.obj;
                    //Merge DualTalk code
                    if (((int[])ar.result)[0] == 1) {
                        int newDualModemCall;
                        if (EVENT_SPEECH_INFO2 == msg.what)
                        {
                            newDualModemCall = 1;
                            /* Solve ALPS00275770, only set audio mode to IN_CALL when in espeech = 1 */
                            espeech_info2 = 1;
                        }
                        else  
                        {
                            newDualModemCall = 0;
                            /* Solve ALPS00275770, only set audio mode to IN_CALL when in espeech = 1 */
                            espeech_info = 1;
                        }
                        //if ((newDualModemCall != mDualModemCall) && (FeatureOption.MTK_DT_SUPPORT == true)) {
                        if ((newDualModemCall != mDualModemCall) && PhoneFactory.isDualTalkMode()) {
                            setAudioModeDualModem(mDualModemCall, AudioManager.MODE_NORMAL);
                            mDualModemCall = newDualModemCall;
                            Log.d(LOG_TAG, "set mDualModemCall = " + mDualModemCall);
                        }
                        setAudioModeDualModem(mDualModemCall, AudioManager.MODE_IN_CALL);						
                    } else {
                        if (EVENT_SPEECH_INFO2 == msg.what)
                        {
                            /* Solve ALPS00275770, only set audio mode to IN_CALL when in espeech = 1 */
                            espeech_info2 = 0;
                        }
                        else
                        {
                            /* Solve ALPS00275770, only set audio mode to IN_CALL when in espeech = 1 */
                            espeech_info = 0;
                        }
                        //if (FeatureOption.MTK_DT_SUPPORT == true) {
                        if (PhoneFactory.isDualTalkMode()) {
                           setAudioMode(AudioManager.MODE_NORMAL);
                        }
                    }
                    if(EVENT_SPEECH_INFO == msg.what){
                        mSpeechInfoRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mSpeechInfoRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
					
                case EVENT_VT_RING_INFO:
                case EVENT_VT_RING_INFO2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_VT_RING_INFO)");
                    if(EVENT_VT_RING_INFO == msg.what){
                        mVtRingInfoRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mVtRingInfoRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_VT_STATUS_INFO:
                case EVENT_VT_STATUS_INFO2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_VT_STATUS_INFO)");
                    if(EVENT_VT_STATUS_INFO == msg.what){
                        mVtStatusInfoRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mVtStatusInfoRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_CRSS_SUPP_SERVICE_NOTIFICATION:
                case EVENT_CRSS_SUPP_SERVICE_NOTIFICATION2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_CRSS_SUPP_SERVICE_NOTIFICATION)");
                    if(EVENT_CRSS_SUPP_SERVICE_NOTIFICATION == msg.what){
                        mCrssSuppServiceNotificationRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mCrssSuppServiceNotificationRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_SUPP_SERVICE_NOTIFICATION:
                case EVENT_SUPP_SERVICE_NOTIFICATION2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_SUPP_SERVICE_NOTIFICATION)");
                    if(EVENT_SUPP_SERVICE_NOTIFICATION == msg.what){
                        mSuppServiceNotificationRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mSuppServiceNotificationRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                case EVENT_VT_REPLACE_DISCONNECT:
                case EVENT_VT_REPLACE_DISCONNECT2:
                    if (VDBG) Log.d(LOG_TAG, " handleMessage (EVENT_VT_REPLACE_DISCONNECT)");
                    if(EVENT_VT_REPLACE_DISCONNECT == msg.what){
                        mVtReplaceDisconnectRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    } else {
                        mVtReplaceDisconnectRegistrants2.notifyRegistrants((AsyncResult) msg.obj);
                    }
                    break;
                /* MTK proprietary end */
            }
        }
        //MTK-END [mtk04070][111121][ALPS00093395]MTK modified
    };

    @Override
    public String toString() {
        Call call;
        StringBuilder b = new StringBuilder();

        b.append("CallManager {");
        b.append("\nstate = " + getState());
        call = getActiveFgCall();
        b.append("\n- Foreground: " + getActiveFgCallState());
        b.append(" from " + call.getPhone());
        b.append("\n  Conn: ").append(getFgCallConnections());
        call = getFirstActiveBgCall();
        b.append("\n- Background: " + call.getState());
        b.append(" from " + call.getPhone());
        b.append("\n  Conn: ").append(getBgCallConnections());
        call = getFirstActiveRingingCall();
        b.append("\n- Ringing: " +call.getState());
        b.append(" from " + call.getPhone());

        for (Phone phone : getAllPhones()) {
            if (phone != null) {
                b.append("\nPhone: " + phone + ", name = " + phone.getPhoneName()
                        + ", state = " + phone.getState());
                call = phone.getForegroundCall();
                b.append("\n- Foreground: ").append(call);
                call = phone.getBackgroundCall();
                b.append(" Background: ").append(call);
                call = phone.getRingingCall();
                b.append(" Ringing: ").append(call);
            }
        }
        b.append("\n}");
        return b.toString();
    }

    //MTK-START [mtk04070][111121][ALPS00093395]MTK proprietary methods
    private void setAudioMode(int mode) {
        Log.d(LOG_TAG, "setAudioMode: mode = " + mode);
        Context context = getContext();
        boolean isVTCall = false;
        
        if (context == null) return;
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        String headsetState = "";
        int value = 0;	/* Normal mode */
        if (AudioManager.MODE_RINGTONE == mode) {
           value = 1;
        } else if ((AudioManager.MODE_IN_CALL == mode) || (AudioManager.MODE_IN_COMMUNICATION == mode)) {
           value = 2;
        }
        headsetState = "RILSetHeadsetState=" + value;
        audioManager.setParameters(headsetState);
		    Log.d(LOG_TAG, headsetState);

        // calling audioManager.setMode() multiple times in a short period of
        // time seems to break the audio recorder in in-call mode
        if (audioManager.getMode() != mode) {
            if (FeatureOption.MTK_VT3G324M_SUPPORT == true) {
                int count = getFgCallConnections().size();
                if (AudioManager.MODE_IN_CALL == mode) {
                    for(int i = 0; i < count; i++) {
                        Connection cn = getFgCallConnections().get(i);
                        if (cn.isVideo()) {
                            Log.d(LOG_TAG, "SetVTSpeechCall=1");
                            audioManager.setParameters("SetVTSpeechCall=1");
                            hasSetVtPara = true;
                            isVTCall = true;
                            break;
                        }
                    }
                } else if (AudioManager.MODE_NORMAL == mode) {
                    if (hasSetVtPara) {
                        Log.d(LOG_TAG, "SetVTSpeechCall=0");
                        audioManager.setParameters("SetVTSpeechCall=0");
                    }
                }
            }

            /* Solve CR - ALPS00264465. Check if the audio mode set to normal is due to call disconnected */
            boolean fgIsAlive = getFgPhone().getForegroundCall().getState().isAlive();
            boolean bgIsAlive = getBgPhone().getBackgroundCall().getState().isAlive();
            //Log.d(LOG_TAG, "getFgPhone().getForegroundCall().getState() = " + getFgPhone().getForegroundCall().getState());
            //Log.d(LOG_TAG, "getBgPhone().getBackgroundCall().getState() = " + getBgPhone().getBackgroundCall().getState());
            if ((AudioManager.MODE_NORMAL == mode) && (fgIsAlive || bgIsAlive)) {
                Log.d(LOG_TAG, "Should not delay 800 msec in audio driver since at least one call is not in idle state.");
                audioManager.setParameters("SkipEndCallDelay=1");
                audioManager.setParameters("RILSetHeadsetState=2");
            }
            
            /* Solve ALPS00275770, ignore setting audio mode to IN_CALL if does not receive "ESPEECH: 1" */
            if (isVTCall == false) {
               if ((mode == AudioManager.MODE_IN_CALL) && (espeech_info == 0) && (espeech_info2 == 0)) {
        	        return;
        	     }   
            }
            
            Log.d(LOG_TAG, "set AudioManager mode " + mode);
            audioManager.setMode(mode);
        }
    }
            
    //Merge DualTalk code
    /**
     * Set audio mode with device ID. In dual modem atchitecture, shall set devId in the handling of speech_info
     * If devId is not assigned, it will set according to the call status which may not sync to the speech_info
     * ex. speech_info(sim1) on before sim1 call is set to active
     * @param devId audio device id (0 or 1 for dual modem platforms)
     * @param mode audio mode
     */
    private void setAudioModeDualModem(int devId, int mode) {
        Context context = getContext();
        if (context == null) return;
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        
        /* If second modem is CDMA and it is set as phone 1, switch the devId */
        if (PhoneFactory.isDualTalkMode()) { 
           if (PhoneFactory.getExternalModemSlot() == Phone.GEMINI_SIM_1) {
              devId = (devId == 1) ? 0 : 1;
              Log.d(LOG_TAG, "Change modem id since second modem is set to Phone 1");
           }
        }
        
        //if (FeatureOption.MTK_DT_SUPPORT == true) {   
        if (PhoneFactory.isDualTalkMode()) {
           if (devId == 1)
           {
               Log.d(LOG_TAG, "SecondModemPhoneCall=1");
               audioManager.setParameters("SecondModemPhoneCall=1");
           }
           else
           {
               Log.d(LOG_TAG, "SecondModemPhoneCall=0");
               audioManager.setParameters("SecondModemPhoneCall=0");
           }
        }

        setAudioMode(mode);
    }	

    /* MTK proprietary start */
    public void registerForSpeechInfo(Handler h, int what, Object obj) {
        mSpeechInfoRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSpeechInfo(Handler h) {
        mSpeechInfoRegistrants.remove(h);
    }

    public void registerForVtStatusInfo(Handler h, int what, Object obj)  {
        mVtStatusInfoRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForVtStatusInfo(Handler h)  {
        mVtStatusInfoRegistrants.remove(h);
    }

    public void registerForVtRingInfo(Handler h, int what, Object obj) {
        mVtRingInfoRegistrants.addUnique(h, what, obj);
    }
    
    public void unregisterForVtRingInfo(Handler h) {
        mVtRingInfoRegistrants.remove(h);
    }

    public void registerForCrssSuppServiceNotification(Handler h, int what, Object obj) {
        mCrssSuppServiceNotificationRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForCrssSuppServiceNotification(Handler h) {
        mCrssSuppServiceNotificationRegistrants.remove(h);
    }

    public void registerForSuppServiceNotification(Handler h, int what, Object obj) {
        mSuppServiceNotificationRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSuppServiceNotification(Handler h){
        mSuppServiceNotificationRegistrants.remove(h);
    }

    public void  registerForVtReplaceDisconnect(Handler h, int what, Object obj) {
        mVtReplaceDisconnectRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForVtReplaceDisconnect(Handler h){
        mVtReplaceDisconnectRegistrants.remove(h);
    }
    /* MTK proprietary end */

    /**
     * Notifies when a voice connection has disconnected, either due to local
     * or remote hangup or error.
     *
     *  Messages received from this will have the following members:<p>
     *  <ul><li>Message.obj will be an AsyncResult</li>
     *  <li>AsyncResult.userObj = obj</li>
     *  <li>AsyncResult.result = a Connection object that is
     *  no longer connected.</li></ul>
     */
    public void registerForDisconnect2(Handler h, int what, Object obj) {
        mDisconnectRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregisters for voice disconnection notification.
     * Extraneous calls are tolerated silently
     */
    public void unregisterForDisconnect2(Handler h){
        mDisconnectRegistrants2.remove(h);
    }

    /**
     * Register for getting notifications for change in the Call State {@link Call.State}
     * This is called PreciseCallState because the call state is more precise than the
     * {@link Phone.State} which can be obtained using the {@link PhoneStateListener}
     *
     * Resulting events will have an AsyncResult in <code>Message.obj</code>.
     * AsyncResult.userData will be set to the obj argument here.
     * The <em>h</em> parameter is held only by a weak reference.
     */
    public void registerForPreciseCallStateChanged2(Handler h, int what, Object obj){
        mPreciseCallStateRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregisters for voice call state change notifications.
     * Extraneous calls are tolerated silently.
     */
    public void unregisterForPreciseCallStateChanged2(Handler h){
        mPreciseCallStateRegistrants2.remove(h);
    }

    /**
     * Notifies when a previously untracked non-ringing/waiting connection has appeared.
     * This is likely due to some other entity (eg, SIM card application) initiating a call.
     */
    public void registerForUnknownConnection2(Handler h, int what, Object obj){
        mUnknownConnectionRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregisters for unknown connection notifications.
     */
    public void unregisterForUnknownConnection2(Handler h){
        mUnknownConnectionRegistrants2.remove(h);
    }
    
    /**
     * Notifies when a new ringing or waiting connection has appeared.<p>
     *
     *  Messages received from this:
     *  Message.obj will be an AsyncResult
     *  AsyncResult.userObj = obj
     *  AsyncResult.result = a Connection. <p>
     *  Please check Connection.isRinging() to make sure the Connection
     *  has not dropped since this message was posted.
     *  If Connection.isRinging() is true, then
     *   Connection.getCall() == Phone.getRingingCall()
     */
    public void registerForNewRingingConnection2(Handler h, int what, Object obj){
        mNewRingingConnectionRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregisters for new ringing connection notification.
     * Extraneous calls are tolerated silently
     */

    public void unregisterForNewRingingConnection2(Handler h){
        mNewRingingConnectionRegistrants2.remove(h);
    }

    /**
     * Notifies when an incoming call rings.<p>
     *
     *  Messages received from this:
     *  Message.obj will be an AsyncResult
     *  AsyncResult.userObj = obj
     *  AsyncResult.result = a Connection. <p>
     */
    public void registerForIncomingRing2(Handler h, int what, Object obj){
        mIncomingRingRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregisters for ring notification.
     * Extraneous calls are tolerated silently
     */

    public void unregisterForIncomingRing2(Handler h){
        mIncomingRingRegistrants2.remove(h);
    }

    /**
     * Notifies when out-band ringback tone is needed.<p>
     *
     *  Messages received from this:
     *  Message.obj will be an AsyncResult
     *  AsyncResult.userObj = obj
     *  AsyncResult.result = boolean, true to start play ringback tone
     *                       and false to stop. <p>
     */
    public void registerForRingbackTone2(Handler h, int what, Object obj){
        mRingbackToneRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregisters for ringback tone notification.
     */

    public void unregisterForRingbackTone2(Handler h){
        mRingbackToneRegistrants2.remove(h);
    }

    /**
     * Registers the handler to reset the uplink mute state to get
     * uplink audio.
     */
    public void registerForResendIncallMute2(Handler h, int what, Object obj){
        mResendIncallMuteRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregisters for resend incall mute notifications.
     */
    public void unregisterForResendIncallMute2(Handler h){
        mResendIncallMuteRegistrants2.remove(h);
    }

    /**
     * Register for notifications of initiation of a new MMI code request.
     * MMI codes for GSM are discussed in 3GPP TS 22.030.<p>
     *
     * Example: If Phone.dial is called with "*#31#", then the app will
     * be notified here.<p>
     *
     * The returned <code>Message.obj</code> will contain an AsyncResult.
     *
     * <code>obj.result</code> will be an "MmiCode" object.
     */
    public void registerForMmiInitiate2(Handler h, int what, Object obj){
        mMmiInitiateRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregisters for new MMI initiate notification.
     * Extraneous calls are tolerated silently
     */
    public void unregisterForMmiInitiate2(Handler h){
        mMmiInitiateRegistrants2.remove(h);
    }

    /**
     * Register for notifications that an MMI request has completed
     * its network activity and is in its final state. This may mean a state
     * of COMPLETE, FAILED, or CANCELLED.
     *
     * <code>Message.obj</code> will contain an AsyncResult.
     * <code>obj.result</code> will be an "MmiCode" object
     */
    public void registerForMmiComplete2(Handler h, int what, Object obj){
        mMmiCompleteRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregisters for MMI complete notification.
     * Extraneous calls are tolerated silently
     */
    public void unregisterForMmiComplete2(Handler h){
        mMmiCompleteRegistrants2.remove(h);
    }

    /**
     * Registration point for Ecm timer reset
     * @param h handler to notify
     * @param what user-defined message code
     * @param obj placed in Message.obj
     */
    public void registerForEcmTimerReset2(Handler h, int what, Object obj){
        mEcmTimerResetRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregister for notification for Ecm timer reset
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForEcmTimerReset2(Handler h){
        mEcmTimerResetRegistrants2.remove(h);
    }

    /**
     * Register for ServiceState changed.
     * Message.obj will contain an AsyncResult.
     * AsyncResult.result will be a ServiceState instance
     */
    public void registerForServiceStateChanged2(Handler h, int what, Object obj){
        mServiceStateChangedRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregisters for ServiceStateChange notification.
     * Extraneous calls are tolerated silently
     */
    public void unregisterForServiceStateChanged2(Handler h){
        mServiceStateChangedRegistrants2.remove(h);
    }

    /**
     * Register for notifications when a supplementary service attempt fails.
     * Message.obj will contain an AsyncResult.
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */
    public void registerForSuppServiceFailed2(Handler h, int what, Object obj){
        mSuppServiceFailedRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregister for notifications when a supplementary service attempt fails.
     * Extraneous calls are tolerated silently
     *
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForSuppServiceFailed2(Handler h){
        mSuppServiceFailedRegistrants2.remove(h);
    }

    /**
     * Register for notifications when a sInCall VoicePrivacy is enabled
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */
    public void registerForInCallVoicePrivacyOn2(Handler h, int what, Object obj){
        mInCallVoicePrivacyOnRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregister for notifications when a sInCall VoicePrivacy is enabled
     *
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForInCallVoicePrivacyOn2(Handler h){
        mInCallVoicePrivacyOnRegistrants2.remove(h);
    }

    /**
     * Register for notifications when a sInCall VoicePrivacy is disabled
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */
    public void registerForInCallVoicePrivacyOff2(Handler h, int what, Object obj){
        mInCallVoicePrivacyOffRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregister for notifications when a sInCall VoicePrivacy is disabled
     *
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForInCallVoicePrivacyOff2(Handler h){
        mInCallVoicePrivacyOffRegistrants2.remove(h);
    }

    /**
     * Register for notifications when CDMA call waiting comes
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */
    public void registerForCallWaiting2(Handler h, int what, Object obj){
        mCallWaitingRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregister for notifications when CDMA Call waiting comes
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForCallWaiting2(Handler h){
        mCallWaitingRegistrants2.remove(h);
    }


    /**
     * Register for signal information notifications from the network.
     * Message.obj will contain an AsyncResult.
     * AsyncResult.result will be a SuppServiceNotification instance.
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */

    public void registerForSignalInfo2(Handler h, int what, Object obj){
        mSignalInfoRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregisters for signal information notifications.
     * Extraneous calls are tolerated silently
     *
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForSignalInfo2(Handler h){
        mSignalInfoRegistrants2.remove(h);
    }

    /**
     * Register for display information notifications from the network.
     * Message.obj will contain an AsyncResult.
     * AsyncResult.result will be a SuppServiceNotification instance.
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */
    public void registerForDisplayInfo2(Handler h, int what, Object obj){
        mDisplayInfoRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregisters for display information notifications.
     * Extraneous calls are tolerated silently
     *
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForDisplayInfo2(Handler h) {
        mDisplayInfoRegistrants2.remove(h);
    }

    /**
     * Register for notifications when CDMA OTA Provision status change
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */
    public void registerForCdmaOtaStatusChange2(Handler h, int what, Object obj){
        mCdmaOtaStatusChangeRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregister for notifications when CDMA OTA Provision status change
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForCdmaOtaStatusChange2(Handler h){
        mCdmaOtaStatusChangeRegistrants2.remove(h);
    }

    /**
     * Registration point for subscription info ready
     * @param h handler to notify
     * @param what what code of message when delivered
     * @param obj placed in Message.obj
     */
    public void registerForSubscriptionInfoReady2(Handler h, int what, Object obj){
        mSubscriptionInfoReadyRegistrants2.addUnique(h, what, obj);
    }

    /**
     * Unregister for notifications for subscription info
     * @param h Handler to be removed from the registrant list.
     */
    public void unregisterForSubscriptionInfoReady2(Handler h){
        mSubscriptionInfoReadyRegistrants2.remove(h);
    }

    /**
     * Sets an event to be fired when the telephony system processes
     * a post-dial character on an outgoing call.<p>
     *
     * Messages of type <code>what</code> will be sent to <code>h</code>.
     * The <code>obj</code> field of these Message's will be instances of
     * <code>AsyncResult</code>. <code>Message.obj.result</code> will be
     * a Connection object.<p>
     *
     * Message.arg1 will be the post dial character being processed,
     * or 0 ('\0') if end of string.<p>
     *
     * If Connection.getPostDialState() == WAIT,
     * the application must call
     * {@link com.android.internal.telephony.Connection#proceedAfterWaitChar()
     * Connection.proceedAfterWaitChar()} or
     * {@link com.android.internal.telephony.Connection#cancelPostDial()
     * Connection.cancelPostDial()}
     * for the telephony system to continue playing the post-dial
     * DTMF sequence.<p>
     *
     * If Connection.getPostDialState() == WILD,
     * the application must call
     * {@link com.android.internal.telephony.Connection#proceedAfterWildChar
     * Connection.proceedAfterWildChar()}
     * or
     * {@link com.android.internal.telephony.Connection#cancelPostDial()
     * Connection.cancelPostDial()}
     * for the telephony system to continue playing the
     * post-dial DTMF sequence.<p>
     *
     */
    public void registerForPostDialCharacter2(Handler h, int what, Object obj){
        mPostDialCharacterRegistrants2.addUnique(h, what, obj);
    }

    public void unregisterForPostDialCharacter2(Handler h){
        mPostDialCharacterRegistrants2.remove(h);
    }

    public void registerForSpeechInfo2(Handler h, int what, Object obj) {
        mSpeechInfoRegistrants2.addUnique(h, what, obj);
    }

    public void unregisterForSpeechInfo2(Handler h) {
        mSpeechInfoRegistrants2.remove(h);
    }
    
    public void registerForVtStatusInfo2(Handler h, int what, Object obj)  {
        mVtStatusInfoRegistrants2.addUnique(h, what, obj);
    }

    public void unregisterForVtStatusInfo2(Handler h)  {
        mVtStatusInfoRegistrants2.remove(h);
    }

    public void registerForVtRingInfo2(Handler h, int what, Object obj) {
        mVtRingInfoRegistrants2.addUnique(h, what, obj);
    }
    
    public void unregisterForVtRingInfo2(Handler h) {
        mVtRingInfoRegistrants2.remove(h);
    }

    public void registerForCrssSuppServiceNotification2(Handler h, int what, Object obj) {
        mCrssSuppServiceNotificationRegistrants2.addUnique(h, what, obj);
    }

    public void unregisterForCrssSuppServiceNotification2(Handler h) {
        mCrssSuppServiceNotificationRegistrants2.remove(h);
    }

    public void registerForSuppServiceNotification2(Handler h, int what, Object obj) {
        mSuppServiceNotificationRegistrants2.addUnique(h, what, obj);
    }

    public void unregisterForSuppServiceNotification2(Handler h){
        mSuppServiceNotificationRegistrants2.remove(h);
    }

    public void  registerForVtReplaceDisconnect2(Handler h, int what, Object obj) {
        mVtReplaceDisconnectRegistrants2.addUnique(h, what, obj);
    }

    public void unregisterForVtReplaceDisconnect2(Handler h){
        mVtReplaceDisconnectRegistrants2.remove(h);
    }

    /* proprietary API start */
    public void hangupAll() throws CallStateException {
        if (VDBG) {
            Log.d(LOG_TAG, " hangupAll() ");
            Log.d(LOG_TAG, this.toString());
        }

        for(Phone phone : mPhones) {
             if (Phone.State.IDLE != phone.getState()) {
                 phone.hangupAll();
             }
        }
    }

    public void hangupActiveCall(Call activeCall) throws CallStateException {
        if (VDBG) {
            Log.d(LOG_TAG, " hangupActiveCall(" + activeCall + ")");
            Log.d(LOG_TAG, this.toString());
        }

        if ( hasActiveFgCall() ) {
            Phone activePhone = activeCall.getPhone();
            activePhone.hangupActiveCall();
        }
    }

    public Connection vtDial(Phone phone, String dialString) throws CallStateException {
        Phone basePhone = getPhoneBase(phone);
        Connection result;

        if (VDBG) {
            Log.d(LOG_TAG, " vtDial(" + basePhone + ", "+ dialString + ")");
            Log.d(LOG_TAG, this.toString());
        }

        if ( hasActiveFgCall() ) {
            Phone activePhone = getActiveFgCall().getPhone();

            if (activePhone instanceof SipPhone) {
                boolean hasBgCall = !(activePhone.getBackgroundCall().isIdle());

                if (DBG) {
                    Log.d(LOG_TAG, "hasBgCall: "+ hasBgCall + " sameChannel:" + (activePhone == basePhone));
                }

                if (hasBgCall) {
                    Log.d(LOG_TAG, "Hangup");
                    getActiveFgCall().hangup();
                } else {
                    Log.d(LOG_TAG, "Switch");
                    activePhone.switchHoldingAndActive();
                }
            } else {
                activePhone.hangupAll();
            }
        }

        result = basePhone.vtDial(dialString);

        if (VDBG) {
            Log.d(LOG_TAG, "End vtDial(" + basePhone + ", "+ dialString + ")");
            Log.d(LOG_TAG, this.toString());
        }

        return result;
    }

    public void voiceAccept(Call ringingCall) throws CallStateException {
        if (VDBG) {
            Log.d(LOG_TAG, "voiceAccept(" +ringingCall + ")");
            Log.d(LOG_TAG, this.toString());
        }

        Phone ringingPhone = ringingCall.getPhone();

        ringingPhone.voiceAccept();

        if (VDBG) {
            Log.d(LOG_TAG, "End voiceAccept(" +ringingCall + ")");
            Log.d(LOG_TAG, this.toString());
        }

    }

    /**
    * check if the dial string is CRSS string.
    */
    private boolean isInCallMmiCommands(String dialString) {
        boolean result = false;
        char ch = dialString.charAt(0);

        switch (ch) {
            case '0':
            case '3':
            case '4':
            case '5':
                if (dialString.length() == 1) {
                    result = true;
                }
                break;

            case '1':
            case '2':
                if (dialString.length() == 1 || dialString.length() == 2) {
                    result = true;
                }
                break;

            default:
                break;
        }

        return result;
    }
    /* proprietary API end */

    private void handle3GSwitchLock() {
        /* 3G switch start */
        if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
            ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            Phone.State state = getState();
            try {
                if (state == Phone.State.IDLE && iTelephony.is3GSwitchLocked()) {
                    if (VDBG) Log.d(LOG_TAG, "Phone call IDLE, release 3G switch lock [" + m3GSwitchLockForPhoneCall + "]");
                    iTelephony.release3GSwitchLock(m3GSwitchLockForPhoneCall);
                    m3GSwitchLockForPhoneCall = -1;
                } else if (state != Phone.State.IDLE && !iTelephony.is3GSwitchLocked()) {
                    m3GSwitchLockForPhoneCall = iTelephony.aquire3GSwitchLock();
                    if (VDBG) Log.d(LOG_TAG, "Phone call not IDLE, acquire 3G switch lock [" + m3GSwitchLockForPhoneCall + "]");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        /* 3G switch end */
    }
    //MTK-END [mtk04070][111121][ALPS00093395]MTK proprietary methods
}
