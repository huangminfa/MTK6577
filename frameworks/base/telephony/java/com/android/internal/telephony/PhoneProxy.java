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

package com.android.internal.telephony;


import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.net.LinkCapabilities;
import android.net.LinkProperties;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.util.Log;

import com.android.internal.telephony.cdma.CDMAPhone;
import com.android.internal.telephony.gsm.GSMPhone;
import com.android.internal.telephony.ims.IsimRecords;
import com.android.internal.telephony.test.SimulatedRadioControl;

//MTK-START [mtk04070][111223][ALPS00106134]Merge to ICS 4.0.3
import com.android.internal.telephony.gsm.UsimServiceTable;
//MTK-END [mtk04070][111223][ALPS00106134]Merge to ICS 4.0.3


//MTK-START [mtk04070][111117][ALPS00093395]MTK added
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.gsm.NetworkInfoWithAcT;
//MTK-END [mtk04070][111117][ALPS00093395]MTK added

//MTK-START [mtk04258][20120806][International card support]
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.gemini.MTKCallManager;
import com.android.internal.telephony.gemini.MTKPhoneFactory;
//MTK-END [mtk04258][20120806][International card support]

import java.util.List;

public class PhoneProxy extends Handler implements Phone {
    public final static Object lockForRadioTechnologyChange = new Object();

    private Phone mActivePhone;
    private String mOutgoingPhone;
    private CommandsInterface mCommandsInterface;
    private IccSmsInterfaceManagerProxy mIccSmsInterfaceManagerProxy;
    private IccPhoneBookInterfaceManagerProxy mIccPhoneBookInterfaceManagerProxy;
    private PhoneSubInfoProxy mPhoneSubInfoProxy;

    private boolean mResetModemOnRadioTechnologyChange = false;

    private static final int EVENT_RADIO_TECHNOLOGY_CHANGED = 1;
//MTK-START [mtk04258][20120806][International card support]
    public static final int EVENT_RADIO_DUAL_MODE_SIM_TECH_CHANGED = 2;
//MTK-END [mtk04258][20120806][International card support]
    private static final String LOG_TAG = "PHONE";

    //MTK-START [mtk04070][111117][ALPS00093395]MTK added
    private boolean mGPRSOn = false;
    {
        if ("OP02".equals(SystemProperties.get("ro.operator.optr")))
            mGPRSOn = true;
    }
    //MTK-START [mtk04070][111117][ALPS00093395]MTK added

    //***** Class Methods
    public PhoneProxy(Phone phone) {
        mActivePhone = phone;
        mResetModemOnRadioTechnologyChange = SystemProperties.getBoolean(
                TelephonyProperties.PROPERTY_RESET_ON_RADIO_TECH_CHANGE, false);
        //MTK-START [mtk04070][111117][ALPS00093395]Modified for Gemini
        if (Phone.PHONE_TYPE_GSM == phone.getPhoneType()) {
            int simId = ((GSMPhone) phone).getMySimId();
            mIccSmsInterfaceManagerProxy = new IccSmsInterfaceManagerProxy(
                    phone.getIccSmsInterfaceManager(), simId);
            mIccPhoneBookInterfaceManagerProxy = new IccPhoneBookInterfaceManagerProxy(
                    phone.getIccPhoneBookInterfaceManager(), simId);            
            mPhoneSubInfoProxy = new PhoneSubInfoProxy(phone.getPhoneSubInfo(), simId);
        } else if(Phone.PHONE_TYPE_CDMA == phone.getPhoneType()) {
            int simId = phone.getMySimId();
            mIccSmsInterfaceManagerProxy = new IccSmsInterfaceManagerProxy(
                    phone.getIccSmsInterfaceManager(), simId);
            mIccPhoneBookInterfaceManagerProxy = new IccPhoneBookInterfaceManagerProxy(
                    phone.getIccPhoneBookInterfaceManager(), simId);            
            mPhoneSubInfoProxy = new PhoneSubInfoProxy(phone.getPhoneSubInfo(), simId);
        } else {
            mIccSmsInterfaceManagerProxy = new IccSmsInterfaceManagerProxy(
                    phone.getIccSmsInterfaceManager());
            mIccPhoneBookInterfaceManagerProxy = new IccPhoneBookInterfaceManagerProxy(
                    phone.getIccPhoneBookInterfaceManager());
            mPhoneSubInfoProxy = new PhoneSubInfoProxy(phone.getPhoneSubInfo());
        }
        //MTK-END [mtk04070][111117][ALPS00093395]Modified for Gemini
        mCommandsInterface = ((PhoneBase)mActivePhone).mCM;
        mCommandsInterface.registerForRadioTechnologyChanged(
                this, EVENT_RADIO_TECHNOLOGY_CHANGED, null);

        logd("setPhoneComponent()");
        mCommandsInterface.setPhoneComponent(phone); // ALPS00296353 MVNO
    }

    @Override
    public void handleMessage(Message msg) {
        switch(msg.what) {
        case EVENT_RADIO_TECHNOLOGY_CHANGED:
        case EVENT_RADIO_DUAL_MODE_SIM_TECH_CHANGED:
            //switch Phone from CDMA to GSM or vice versa
            mOutgoingPhone = mActivePhone.getPhoneName();
            logd("Switching phone from " + mOutgoingPhone + "Phone to " +
                    (mOutgoingPhone.equals("GSM") ? "CDMAPhone" : "GSMPhone") );
            boolean oldPowerState = false; // old power state to off
//MTK-START [mtk04258][20120806][International card support]
            MTKCallManager cm = MTKCallManager.getInstance();
            GeminiPhone phoneInst = null;;
            int simId = Phone.GEMINI_SIM_1;
            if (FeatureOption.EVDO_DT_SUPPORT &&
                msg.what == EVENT_RADIO_DUAL_MODE_SIM_TECH_CHANGED) {
                simId = msg.arg2;
                logd("[SimSw] Switching phone simId= " + simId);
                phoneInst = (GeminiPhone)MTKPhoneFactory.getDefaultPhone();
                // Avoid the query from CallManager during tech change
                if (phoneInst instanceof GeminiPhone)
                    cm.unregisterPhoneGemini(phoneInst); 
            }
//MTK-END [mtk04258][20120806][International card support]			
            if (mResetModemOnRadioTechnologyChange &&
                msg.what == EVENT_RADIO_TECHNOLOGY_CHANGED) {
                if (mCommandsInterface.getRadioState().isOn()) {
                    oldPowerState = true;
                    logd("Setting Radio Power to Off");
                    mCommandsInterface.setRadioPower(false, null);
                }
            }

            if(mOutgoingPhone.equals("GSM")) {
                logd("Make a new CDMAPhone and destroy the old GSMPhone.");

                ((GSMPhone)mActivePhone).dispose();
                Phone oldPhone = mActivePhone;

                //Give the garbage collector a hint to start the garbage collection asap
                // NOTE this has been disabled since radio technology change could happen during
                //   e.g. a multimedia playing and could slow the system. Tests needs to be done
                //   to see the effects of the GC call here when system is busy.
                //System.gc();

                mActivePhone = PhoneFactory.getCdmaPhone(simId);
                //MTK-START [mtk04070][111117][ALPS00093395]Reset Radio
                if (msg.what == EVENT_RADIO_TECHNOLOGY_CHANGED) {
                logd("Resetting Radio");
                mCommandsInterface.setRadioPower(oldPowerState, null);
                }
                //MTK-END [mtk04070][111117][ALPS00093395]Reset Radio
                ((GSMPhone)oldPhone).removeReferences();
                oldPhone = null;
            } else {
                logd("Make a new GSMPhone and destroy the old CDMAPhone.");

                ((CDMAPhone)mActivePhone).dispose();
                //mActivePhone = null;
                Phone oldPhone = mActivePhone;

                // Give the GC a hint to start the garbage collection asap
                // NOTE this has been disabled since radio technology change could happen during
                //   e.g. a multimedia playing and could slow the system. Tests needs to be done
                //   to see the effects of the GC call here when system is busy.
                //System.gc();
                mActivePhone = PhoneFactory.getGsmPhone(simId);
                ((CDMAPhone)oldPhone).removeReferences();
                oldPhone = null;
            }

            if (mResetModemOnRadioTechnologyChange &&
                msg.what == EVENT_RADIO_TECHNOLOGY_CHANGED) {
                logd("Resetting Radio");
                mCommandsInterface.setRadioPower(oldPowerState, null);
            }

            //Set the new interfaces in the proxy's
            mIccSmsInterfaceManagerProxy.setmIccSmsInterfaceManager(
                    mActivePhone.getIccSmsInterfaceManager());
            mIccPhoneBookInterfaceManagerProxy.setmIccPhoneBookInterfaceManager(
                    mActivePhone.getIccPhoneBookInterfaceManager());
            mPhoneSubInfoProxy.setmPhoneSubInfo(this.mActivePhone.getPhoneSubInfo());
            mCommandsInterface = ((PhoneBase)mActivePhone).mCM;

//MTK-START [mtk04258][20120806][International card support]
            if (FeatureOption.EVDO_DT_SUPPORT &&
                msg.what == EVENT_RADIO_DUAL_MODE_SIM_TECH_CHANGED &&
                msg.obj != null) {
                // Notify the switch manager to power on modems
                Handler simSwitchHandler = (Handler)msg.obj;
                simSwitchHandler.obtainMessage(msg.arg1).sendToTarget();
                // Avoid the query from CallManager during tech change
                if (phoneInst instanceof GeminiPhone)
                     cm.registerPhoneGemini(phoneInst); 
            }
//MTK-END [mtk04258][20120806][International card support]
            //Send an Intent to the PhoneApp that we had a radio technology change
            Intent intent = new Intent(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED);
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            intent.putExtra(Phone.PHONE_NAME_KEY, mActivePhone.getPhoneName());
//MTK-START [mtk04258][20120806][International card support]
            if (FeatureOption.EVDO_DT_SUPPORT) {
                intent.putExtra(Phone.PHONE_TYPE_KEY, mActivePhone.getPhoneType());
                intent.putExtra(Phone.GEMINI_SIM_ID_KEY, simId);
            }
//MTK-END [mtk04258][20120806][International card support]
            ActivityManagerNative.broadcastStickyIntent(intent, null);
            break;
        default:
            Log.e(LOG_TAG,"Error! This handler was not registered for this message type. Message: "
                    + msg.what);
        break;
        }
        super.handleMessage(msg);
    }

    private static void logd(String msg) {
        Log.d(LOG_TAG, "[PhoneProxy] " + msg);
    }

    public ServiceState getServiceState() {
        return mActivePhone.getServiceState();
    }

    public CellLocation getCellLocation() {
        return mActivePhone.getCellLocation();
    }

    public DataState getDataConnectionState() {
        return mActivePhone.getDataConnectionState(Phone.APN_TYPE_DEFAULT);
    }

    public DataState getDataConnectionState(String apnType) {
        return mActivePhone.getDataConnectionState(apnType);
    }

    public DataActivityState getDataActivityState() {
        return mActivePhone.getDataActivityState();
    }

    public Context getContext() {
        return mActivePhone.getContext();
    }

    public void disableDnsCheck(boolean b) {
        mActivePhone.disableDnsCheck(b);
    }

    public boolean isDnsCheckDisabled() {
        return mActivePhone.isDnsCheckDisabled();
    }

    public State getState() {
        return mActivePhone.getState();
    }

    public String getPhoneName() {
        return mActivePhone.getPhoneName();
    }

    public int getPhoneType() {
        return mActivePhone.getPhoneType();
    }

    public String[] getActiveApnTypes() {
        return mActivePhone.getActiveApnTypes();
    }

    public String getActiveApnHost(String apnType) {
        return mActivePhone.getActiveApnHost(apnType);
    }

    public LinkProperties getLinkProperties(String apnType) {
        return mActivePhone.getLinkProperties(apnType);
    }

    public LinkCapabilities getLinkCapabilities(String apnType) {
        return mActivePhone.getLinkCapabilities(apnType);
    }

    public SignalStrength getSignalStrength() {
        return mActivePhone.getSignalStrength();
    }

    public void registerForUnknownConnection(Handler h, int what, Object obj) {
        mActivePhone.registerForUnknownConnection(h, what, obj);
    }

    public void unregisterForUnknownConnection(Handler h) {
        mActivePhone.unregisterForUnknownConnection(h);
    }

    public void registerForPreciseCallStateChanged(Handler h, int what, Object obj) {
        mActivePhone.registerForPreciseCallStateChanged(h, what, obj);
    }

    public void unregisterForPreciseCallStateChanged(Handler h) {
        mActivePhone.unregisterForPreciseCallStateChanged(h);
    }

    public void registerForNewRingingConnection(Handler h, int what, Object obj) {
        mActivePhone.registerForNewRingingConnection(h, what, obj);
    }

    public void unregisterForNewRingingConnection(Handler h) {
        mActivePhone.unregisterForNewRingingConnection(h);
    }

    public void registerForIncomingRing(Handler h, int what, Object obj) {
        mActivePhone.registerForIncomingRing(h, what, obj);
    }

    public void unregisterForIncomingRing(Handler h) {
        mActivePhone.unregisterForIncomingRing(h);
    }

    public void registerForDisconnect(Handler h, int what, Object obj) {
        mActivePhone.registerForDisconnect(h, what, obj);
    }

    public void unregisterForDisconnect(Handler h) {
        mActivePhone.unregisterForDisconnect(h);
    }

    public void registerForMmiInitiate(Handler h, int what, Object obj) {
        mActivePhone.registerForMmiInitiate(h, what, obj);
    }

    public void unregisterForMmiInitiate(Handler h) {
        mActivePhone.unregisterForMmiInitiate(h);
    }

    public void registerForMmiComplete(Handler h, int what, Object obj) {
        mActivePhone.registerForMmiComplete(h, what, obj);
    }

    public void unregisterForMmiComplete(Handler h) {
        mActivePhone.unregisterForMmiComplete(h);
    }

    public List<? extends MmiCode> getPendingMmiCodes() {
        return mActivePhone.getPendingMmiCodes();
    }

    public void sendUssdResponse(String ussdMessge) {
        mActivePhone.sendUssdResponse(ussdMessge);
    }

    public void registerForServiceStateChanged(Handler h, int what, Object obj) {
        mActivePhone.registerForServiceStateChanged(h, what, obj);
    }

    public void unregisterForServiceStateChanged(Handler h) {
        mActivePhone.unregisterForServiceStateChanged(h);
    }

    public void registerForSuppServiceNotification(Handler h, int what, Object obj) {
        mActivePhone.registerForSuppServiceNotification(h, what, obj);
    }

    public void unregisterForSuppServiceNotification(Handler h) {
        mActivePhone.unregisterForSuppServiceNotification(h);
    }

    public void registerForSuppServiceFailed(Handler h, int what, Object obj) {
        mActivePhone.registerForSuppServiceFailed(h, what, obj);
    }

    public void unregisterForSuppServiceFailed(Handler h) {
        mActivePhone.unregisterForSuppServiceFailed(h);
    }

    public void registerForInCallVoicePrivacyOn(Handler h, int what, Object obj){
        mActivePhone.registerForInCallVoicePrivacyOn(h,what,obj);
    }

    public void unregisterForInCallVoicePrivacyOn(Handler h){
        mActivePhone.unregisterForInCallVoicePrivacyOn(h);
    }

    public void registerForInCallVoicePrivacyOff(Handler h, int what, Object obj){
        mActivePhone.registerForInCallVoicePrivacyOff(h,what,obj);
    }

    public void unregisterForInCallVoicePrivacyOff(Handler h){
        mActivePhone.unregisterForInCallVoicePrivacyOff(h);
    }

    public void registerForCdmaOtaStatusChange(Handler h, int what, Object obj) {
        mActivePhone.registerForCdmaOtaStatusChange(h,what,obj);
    }

    public void unregisterForCdmaOtaStatusChange(Handler h) {
         mActivePhone.unregisterForCdmaOtaStatusChange(h);
    }

    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
        mActivePhone.registerForSubscriptionInfoReady(h, what, obj);
    }

    public void unregisterForSubscriptionInfoReady(Handler h) {
        mActivePhone.unregisterForSubscriptionInfoReady(h);
    }

    public void registerForEcmTimerReset(Handler h, int what, Object obj) {
        mActivePhone.registerForEcmTimerReset(h,what,obj);
    }

    public void unregisterForEcmTimerReset(Handler h) {
        mActivePhone.unregisterForEcmTimerReset(h);
    }

    public void registerForRingbackTone(Handler h, int what, Object obj) {
        mActivePhone.registerForRingbackTone(h,what,obj);
    }

    public void unregisterForRingbackTone(Handler h) {
        mActivePhone.unregisterForRingbackTone(h);
    }

    public void registerForResendIncallMute(Handler h, int what, Object obj) {
        mActivePhone.registerForResendIncallMute(h,what,obj);
    }

    public void unregisterForResendIncallMute(Handler h) {
        mActivePhone.unregisterForResendIncallMute(h);
    }

    public boolean getIccRecordsLoaded() {
        return mActivePhone.getIccRecordsLoaded();
    }

    public IccCard getIccCard() {
        return mActivePhone.getIccCard();
    }

    public void acceptCall() throws CallStateException {
        mActivePhone.acceptCall();
    }

    public void rejectCall() throws CallStateException {
        mActivePhone.rejectCall();
    }

    public void switchHoldingAndActive() throws CallStateException {
        mActivePhone.switchHoldingAndActive();
    }

    public boolean canConference() {
        return mActivePhone.canConference();
    }

    public void conference() throws CallStateException {
        mActivePhone.conference();
    }

    public void enableEnhancedVoicePrivacy(boolean enable, Message onComplete) {
        mActivePhone.enableEnhancedVoicePrivacy(enable, onComplete);
    }

    public void getEnhancedVoicePrivacy(Message onComplete) {
        mActivePhone.getEnhancedVoicePrivacy(onComplete);
    }

    public boolean canTransfer() {
        return mActivePhone.canTransfer();
    }

    public void explicitCallTransfer() throws CallStateException {
        mActivePhone.explicitCallTransfer();
    }

    public void clearDisconnected() {
        mActivePhone.clearDisconnected();
    }

    public Call getForegroundCall() {
        return mActivePhone.getForegroundCall();
    }

    public Call getBackgroundCall() {
        return mActivePhone.getBackgroundCall();
    }

    public Call getRingingCall() {
        return mActivePhone.getRingingCall();
    }

    public Connection dial(String dialString) throws CallStateException {
        return mActivePhone.dial(dialString);
    }

    public Connection dial(String dialString, UUSInfo uusInfo) throws CallStateException {
        return mActivePhone.dial(dialString, uusInfo);
    }

    public boolean handlePinMmi(String dialString) {
        return mActivePhone.handlePinMmi(dialString);
    }

    public boolean handleInCallMmiCommands(String command) throws CallStateException {
        return mActivePhone.handleInCallMmiCommands(command);
    }

    public void sendDtmf(char c) {
        mActivePhone.sendDtmf(c);
    }

    public void startDtmf(char c) {
        mActivePhone.startDtmf(c);
    }

    public void stopDtmf() {
        mActivePhone.stopDtmf();
    }

    public void setRadioPower(boolean power) {
        mActivePhone.setRadioPower(power);
    }

/* Dual Talk */
    public void setRadioPower(boolean power, Message what) {
        mCommandsInterface.setRadioPower(power, what);
    }
/* End of Dual Talk */


    public boolean getMessageWaitingIndicator() {
        return mActivePhone.getMessageWaitingIndicator();
    }

    public boolean getCallForwardingIndicator() {
        return mActivePhone.getCallForwardingIndicator();
    }

    public String getLine1Number() {
        return mActivePhone.getLine1Number();
    }

    public String getCdmaMin() {
        return mActivePhone.getCdmaMin();
    }

    public boolean isMinInfoReady() {
        return mActivePhone.isMinInfoReady();
    }

    public String getCdmaPrlVersion() {
        return mActivePhone.getCdmaPrlVersion();
    }

    public String getLine1AlphaTag() {
        return mActivePhone.getLine1AlphaTag();
    }

    public void setLine1Number(String alphaTag, String number, Message onComplete) {
        mActivePhone.setLine1Number(alphaTag, number, onComplete);
    }

    public String getVoiceMailNumber() {
        return mActivePhone.getVoiceMailNumber();
    }

     /** @hide */
    public int getVoiceMessageCount(){
        return mActivePhone.getVoiceMessageCount();
    }

    public String getVoiceMailAlphaTag() {
        return mActivePhone.getVoiceMailAlphaTag();
    }

    public void setVoiceMailNumber(String alphaTag,String voiceMailNumber,
            Message onComplete) {
        mActivePhone.setVoiceMailNumber(alphaTag, voiceMailNumber, onComplete);
    }

    public void getCallForwardingOption(int commandInterfaceCFReason,
            Message onComplete) {
        mActivePhone.getCallForwardingOption(commandInterfaceCFReason,
                onComplete);
    }

    public void setCallForwardingOption(int commandInterfaceCFReason,
            int commandInterfaceCFAction, String dialingNumber,
            int timerSeconds, Message onComplete) {
        mActivePhone.setCallForwardingOption(commandInterfaceCFReason,
            commandInterfaceCFAction, dialingNumber, timerSeconds, onComplete);
    }

    public void getOutgoingCallerIdDisplay(Message onComplete) {
        mActivePhone.getOutgoingCallerIdDisplay(onComplete);
    }

    public void setOutgoingCallerIdDisplay(int commandInterfaceCLIRMode,
            Message onComplete) {
        mActivePhone.setOutgoingCallerIdDisplay(commandInterfaceCLIRMode,
                onComplete);
    }

    public void getCallWaiting(Message onComplete) {
        mActivePhone.getCallWaiting(onComplete);
    }

    public void setCallWaiting(boolean enable, Message onComplete) {
        mActivePhone.setCallWaiting(enable, onComplete);
    }

    public void getAvailableNetworks(Message response) {
        mActivePhone.getAvailableNetworks(response);
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
        mActivePhone.setNetworkSelectionModeAutomatic(response);
    }

    public void selectNetworkManually(OperatorInfo network, Message response) {
        mActivePhone.selectNetworkManually(network, response);
    }

    public void setPreferredNetworkType(int networkType, Message response) {
        mActivePhone.setPreferredNetworkType(networkType, response);
    }

    public void getPreferredNetworkType(Message response) {
        mActivePhone.getPreferredNetworkType(response);
    }

    public void getNeighboringCids(Message response) {
        mActivePhone.getNeighboringCids(response);
    }

    public void setOnPostDialCharacter(Handler h, int what, Object obj) {
        mActivePhone.setOnPostDialCharacter(h, what, obj);
    }

    public void setMute(boolean muted) {
        mActivePhone.setMute(muted);
    }

    public boolean getMute() {
        return mActivePhone.getMute();
    }

    public void setEchoSuppressionEnabled(boolean enabled) {
        mActivePhone.setEchoSuppressionEnabled(enabled);
    }

    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        mActivePhone.invokeOemRilRequestRaw(data, response);
    }

    public void invokeOemRilRequestStrings(String[] strings, Message response) {
        mActivePhone.invokeOemRilRequestStrings(strings, response);
    }

    public void getDataCallList(Message response) {
        mActivePhone.getDataCallList(response);
    }

    public void updateServiceLocation() {
        mActivePhone.updateServiceLocation();
    }

    public void enableLocationUpdates() {
        mActivePhone.enableLocationUpdates();
    }

    public void disableLocationUpdates() {
        mActivePhone.disableLocationUpdates();
    }

    public void setUnitTestMode(boolean f) {
        mActivePhone.setUnitTestMode(f);
    }

    public boolean getUnitTestMode() {
        return mActivePhone.getUnitTestMode();
    }

    public void setBandMode(int bandMode, Message response) {
        mActivePhone.setBandMode(bandMode, response);
    }

    public void queryAvailableBandMode(Message response) {
        mActivePhone.queryAvailableBandMode(response);
    }

    public boolean getDataRoamingEnabled() {
        return mActivePhone.getDataRoamingEnabled();
    }

    public void setDataRoamingEnabled(boolean enable) {
        mActivePhone.setDataRoamingEnabled(enable);
    }

    public void queryCdmaRoamingPreference(Message response) {
        mActivePhone.queryCdmaRoamingPreference(response);
    }

    public void setCdmaRoamingPreference(int cdmaRoamingType, Message response) {
        mActivePhone.setCdmaRoamingPreference(cdmaRoamingType, response);
    }

    public void setCdmaSubscription(int cdmaSubscriptionType, Message response) {
        mActivePhone.setCdmaSubscription(cdmaSubscriptionType, response);
    }

    public SimulatedRadioControl getSimulatedRadioControl() {
        return mActivePhone.getSimulatedRadioControl();
    }

    public int enableApnType(String type) {
        return mActivePhone.enableApnType(type);
    }

    public int disableApnType(String type) {
        return mActivePhone.disableApnType(type);
    }

    public boolean isDataConnectivityPossible() {
        return mActivePhone.isDataConnectivityPossible(Phone.APN_TYPE_DEFAULT);
    }

    public String getDeviceId() {
        return mActivePhone.getDeviceId();
    }

    public String getDeviceSvn() {
        return mActivePhone.getDeviceSvn();
    }

    public String getSubscriberId() {
        return mActivePhone.getSubscriberId();
    }

    public String getIccSerialNumber() {
        return mActivePhone.getIccSerialNumber();
    }

    public String getEsn() {
        return mActivePhone.getEsn();
    }

    public String getMeid() {
        return mActivePhone.getMeid();
    }

    public String getMsisdn() {
        return mActivePhone.getMsisdn();
    }

    public String getImei() {
        return mActivePhone.getImei();
    }

    public PhoneSubInfo getPhoneSubInfo(){
        return mActivePhone.getPhoneSubInfo();
    }

    public IccSmsInterfaceManager getIccSmsInterfaceManager(){
        return mActivePhone.getIccSmsInterfaceManager();
    }

    public IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager(){
        return mActivePhone.getIccPhoneBookInterfaceManager();
    }

    public void setTTYMode(int ttyMode, Message onComplete) {
        mActivePhone.setTTYMode(ttyMode, onComplete);
    }

    public void queryTTYMode(Message onComplete) {
        mActivePhone.queryTTYMode(onComplete);
    }

    public void activateCellBroadcastSms(int activate, Message response) {
        mActivePhone.activateCellBroadcastSms(activate, response);
    }

    public void getCellBroadcastSmsConfig(Message response) {
        mActivePhone.getCellBroadcastSmsConfig(response);
    }

    public void setCellBroadcastSmsConfig(int[] configValuesArray, Message response) {
        mActivePhone.setCellBroadcastSmsConfig(configValuesArray, response);
    }

    public void notifyDataActivity() {
         mActivePhone.notifyDataActivity();
    }

    public void getSmscAddress(Message result) {
        mActivePhone.getSmscAddress(result);
    }

    public void setSmscAddress(String address, Message result) {
        mActivePhone.setSmscAddress(address, result);
    }

    public int getCdmaEriIconIndex() {
        return mActivePhone.getCdmaEriIconIndex();
    }

    public String getCdmaEriText() {
        return mActivePhone.getCdmaEriText();
    }

    public int getCdmaEriIconMode() {
        return mActivePhone.getCdmaEriIconMode();
    }

    public Phone getActivePhone() {
        return mActivePhone;
    }

    public void sendBurstDtmf(String dtmfString, int on, int off, Message onComplete){
        mActivePhone.sendBurstDtmf(dtmfString, on, off, onComplete);
    }

    public void exitEmergencyCallbackMode(){
        mActivePhone.exitEmergencyCallbackMode();
    }

    public boolean needsOtaServiceProvisioning(){
        return mActivePhone.needsOtaServiceProvisioning();
    }

    public boolean isOtaSpNumber(String dialStr){
        return mActivePhone.isOtaSpNumber(dialStr);
    }

    public void registerForCallWaiting(Handler h, int what, Object obj){
        mActivePhone.registerForCallWaiting(h,what,obj);
    }

    public void unregisterForCallWaiting(Handler h){
        mActivePhone.unregisterForCallWaiting(h);
    }

    public void registerForSignalInfo(Handler h, int what, Object obj) {
        mActivePhone.registerForSignalInfo(h,what,obj);
    }

    public void unregisterForSignalInfo(Handler h) {
        mActivePhone.unregisterForSignalInfo(h);
    }

    public void registerForDisplayInfo(Handler h, int what, Object obj) {
        mActivePhone.registerForDisplayInfo(h,what,obj);
    }

    public void unregisterForDisplayInfo(Handler h) {
        mActivePhone.unregisterForDisplayInfo(h);
    }

    public void registerForNumberInfo(Handler h, int what, Object obj) {
        mActivePhone.registerForNumberInfo(h, what, obj);
    }

    public void unregisterForNumberInfo(Handler h) {
        mActivePhone.unregisterForNumberInfo(h);
    }

    public void registerForRedirectedNumberInfo(Handler h, int what, Object obj) {
        mActivePhone.registerForRedirectedNumberInfo(h, what, obj);
    }

    public void unregisterForRedirectedNumberInfo(Handler h) {
        mActivePhone.unregisterForRedirectedNumberInfo(h);
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        mActivePhone.registerForLineControlInfo( h, what, obj);
    }

    public void unregisterForLineControlInfo(Handler h) {
        mActivePhone.unregisterForLineControlInfo(h);
    }

    public void registerFoT53ClirlInfo(Handler h, int what, Object obj) {
        mActivePhone.registerFoT53ClirlInfo(h, what, obj);
    }

    public void unregisterForT53ClirInfo(Handler h) {
        mActivePhone.unregisterForT53ClirInfo(h);
    }

    public void registerForT53AudioControlInfo(Handler h, int what, Object obj) {
        mActivePhone.registerForT53AudioControlInfo( h, what, obj);
    }

    public void unregisterForT53AudioControlInfo(Handler h) {
        mActivePhone.unregisterForT53AudioControlInfo(h);
    }

    public void setOnEcbModeExitResponse(Handler h, int what, Object obj){
        mActivePhone.setOnEcbModeExitResponse(h,what,obj);
    }

    public void unsetOnEcbModeExitResponse(Handler h){
        mActivePhone.unsetOnEcbModeExitResponse(h);
    }

    public boolean isCspPlmnEnabled() {
        return mActivePhone.isCspPlmnEnabled();
    }

    public IsimRecords getIsimRecords() {
        return mActivePhone.getIsimRecords();
    }

    public void requestIsimAuthentication(String nonce, Message response) {
        mActivePhone.requestIsimAuthentication(nonce, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLteOnCdmaMode() {
        return mActivePhone.getLteOnCdmaMode();
    }

    @Override
    public void setVoiceMessageWaiting(int line, int countWaiting) {
        mActivePhone.setVoiceMessageWaiting(line, countWaiting);
    }

    //MTK-START [mtk04070][111223][ALPS00106134]Merge to ICS 4.0.3
    @Override
    public UsimServiceTable getUsimServiceTable() {
        return mActivePhone.getUsimServiceTable();
    }
    //MTK-END [mtk04070][111223][ALPS00106134]Merge to ICS 4.0.3

    //MTK-START [mtk04070][111117][ALPS00093395]MTK proprietary methods
    /* Add by vendor for Multiple PDP Context */
    public String getActiveApnType() {
        // TODO Auto-generated method stub
        return mActivePhone.getActiveApnType();
    }

    /* Add by vendor for Multiple PDP Context */
    public String getApnForType(String type) {
        return mActivePhone.getApnForType(type);
    }

    public void registerForCrssSuppServiceNotification(Handler h, int what, Object obj) {
        mActivePhone.registerForCrssSuppServiceNotification(h, what, obj);
    }

    public void unregisterForCrssSuppServiceNotification(Handler h) {
        mActivePhone.unregisterForCrssSuppServiceNotification(h);
    }
    
    public int getLastCallFailCause() {
        return mActivePhone.getLastCallFailCause();
    }

    /* vt start */
    public Connection vtDial(String dialString) throws CallStateException {
        return mActivePhone.vtDial(dialString);
    }

    public Connection vtDial(String dialString, UUSInfo uusInfo) throws CallStateException {
        return mActivePhone.vtDial(dialString, uusInfo);
    }

    public void voiceAccept() throws CallStateException {
        mActivePhone.voiceAccept();
    }
    /* vt end */

    public void setRadioPower(boolean power, boolean shutdown) {
        mActivePhone.setRadioPower(power, shutdown);
    }

    public void setRadioMode(int mode, Message what) {
        mCommandsInterface.setRadioMode(mode, what);
    }

    public void setAutoGprsAttach(int auto) {
        if(mActivePhone.getPhoneType() == Phone.PHONE_TYPE_GSM)
        { 
        ((GSMPhone)mActivePhone).setAutoGprsAttach(auto);
    }
    }

    public void setGprsConnType(int type) {
        if(mActivePhone.getPhoneType() == Phone.PHONE_TYPE_GSM)
        { 
            ((GSMPhone)mActivePhone).setGprsConnType(type);
        } else if (mActivePhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            ((CDMAPhone)mActivePhone).setCdmaConnType(type);
        }
    }

    public void getPdpContextList(Message result) {
        getDataCallList(result);
    }

    /* Add by vendor for Multiple PDP Context */
    public boolean isDataConnectivityPossible(String apnType) {
        // TODO Auto-generated method stub
        return mActivePhone.isDataConnectivityPossible(apnType);
    }

    /* Add by mtk01411: For GeminiDataSubUtil to notify data connection when returned value of enableApnType() is error */
    public void notifyDataConnection(String reason, String apnType){
        ((PhoneBase)mActivePhone).notifyDataConnection(reason, apnType);
    }

    public String getInterfaceName(String apnType) {
        return mActivePhone.getInterfaceName(apnType);
    }

    public String getIpAddress(String apnType) {
        return mActivePhone.getIpAddress(apnType);
    }

    public String getGateway(String apnType) {
        return mActivePhone.getGateway(apnType);
    }

    public String[] getDnsServers(String apnType) {
        return mActivePhone.getDnsServers(apnType);
    }

    //Add by mtk80372 for Barcode Number
    public String getSN() {
        return mActivePhone.getSN();
    }

    public void setCellBroadcastSmsConfig(SmsBroadcastConfigInfo[] chIdList, 
            SmsBroadcastConfigInfo[] langList, Message response) {
        mActivePhone.setCellBroadcastSmsConfig(chIdList, langList, response);    
    }

    public void queryCellBroadcastSmsActivation(Message response)
    {
        mActivePhone.queryCellBroadcastSmsActivation(response);    
    }

    /*Add by mtk80372 for Barcode number*/
    public void getMobileRevisionAndIMEI(int type,Message result){
        mActivePhone.getMobileRevisionAndIMEI(type,result);
    }

    public void getFacilityLock(String facility, String password, Message onComplete) {
        mActivePhone.getFacilityLock(facility, password, onComplete);
    }

    public void setFacilityLock(String facility, boolean enable, String password, Message onComplete) {
        mActivePhone.setFacilityLock(facility, enable, password, onComplete);
    }

    public void changeBarringPassword(String facility, String oldPwd, String newPwd, Message onComplete) {
        mActivePhone.changeBarringPassword(facility, oldPwd, newPwd, onComplete);
    }

    public void changeBarringPassword(String facility, String oldPwd, String newPwd, String newCfm, Message onComplete) {
        mActivePhone.changeBarringPassword(facility, oldPwd, newPwd, newCfm, onComplete);
    }

    public void hangupAll() throws CallStateException {
    	mActivePhone.hangupAll();
    }

    public void hangupAllEx() throws CallStateException {
    	mActivePhone.hangupAllEx();
    }

    public void hangupActiveCall() throws CallStateException {
        mActivePhone.hangupActiveCall();
    }
    public void getCurrentCallMeter(Message result) {
    	mActivePhone.getCurrentCallMeter(result);
    }
    	
    public void getAccumulatedCallMeter(Message result) {
    	mActivePhone.getAccumulatedCallMeter(result);
    }
    	
    public void getAccumulatedCallMeterMaximum(Message result) {
    	mActivePhone.getAccumulatedCallMeterMaximum(result);
    }
    	
    public void getPpuAndCurrency(Message result) {
    	mActivePhone.getPpuAndCurrency(result);
    }	
    	
    public void setAccumulatedCallMeterMaximum(String acmmax, String pin2, Message result) {
    	mActivePhone.setAccumulatedCallMeterMaximum(acmmax, pin2, result);
    }
    	
    public void resetAccumulatedCallMeter(String pin2, Message result) {
    	mActivePhone.resetAccumulatedCallMeter(pin2, result);
    }
    	
    public void setPpuAndCurrency(String currency, String ppu, String pin2, Message result) {
    	mActivePhone.setPpuAndCurrency(currency, ppu, pin2, result);
    }

    public void registerForNeighboringInfo(Handler h, int what, Object obj) {
        mActivePhone.registerForNeighboringInfo(h, what, obj);
    }

    public void unregisterForNeighboringInfo(Handler h) {
        mActivePhone.unregisterForNeighboringInfo(h);
    }	

    public void registerForNetworkInfo(Handler h, int what, Object obj) {
        mActivePhone.registerForNetworkInfo(h, what, obj);
    }

    public void unregisterForNetworkInfo(Handler h) {
        mActivePhone.unregisterForNetworkInfo(h);	
    } 

    public void refreshSpnDisplay() {
        mActivePhone.refreshSpnDisplay();
    }

    public void registerForSimInsertedStatus(Handler h, int what, Object obj) {
        mCommandsInterface.registerForSimInsertedStatus(h, what, obj);
    }

    public void unregisterForSimInsertedStatus(Handler h) {
        mCommandsInterface.unregisterForSimInsertedStatus(h);
    }
    
    public void registerForSimMissing(Handler h, int what, Object obj) {
        mCommandsInterface.registerForSimInsertedStatus(h, what, obj);
    }

    public void unregisterForSimMissing(Handler h) {
        mCommandsInterface.unregisterForSimInsertedStatus(h);
    }
    public int getSimInsertedStatus() {
        return ((RIL)mCommandsInterface).getSimInsertedStatus();
    }

    public int getMySimId() {
        return mActivePhone.getMySimId();
    }

    public void registerForSpeechInfo(Handler h, int what, Object obj) {
        mActivePhone.registerForSpeechInfo(h, what, obj);
    }

    public void unregisterForSpeechInfo(Handler h) {
        mActivePhone.unregisterForSpeechInfo(h);
    } 

    /* vt start */
    public void registerForVtStatusInfo(Handler h, int what, Object obj) {
        mActivePhone.registerForVtStatusInfo(h, what, obj);
    }

    public void unregisterForVtStatusInfo(Handler h) {
        mActivePhone.unregisterForVtStatusInfo(h);
    } 

    public void registerForVtRingInfo(Handler h, int what, Object obj) {
        mActivePhone.registerForVtRingInfo(h, what, obj);
    }

    public void unregisterForVtRingInfo(Handler h) {
        mActivePhone.unregisterForVtRingInfo(h);
    }

    public void registerForVtReplaceDisconnect(Handler h, int what, Object obj) {
        mActivePhone.registerForVtReplaceDisconnect(h, what, obj);
    }

    public void unregisterForVtReplaceDisconnect(Handler h) {
        mActivePhone.unregisterForVtReplaceDisconnect(h);
    }

    public void getVtCallForwardingOption(int commandInterfaceCFReason,
                                          Message onComplete) {
        mActivePhone.getVtCallForwardingOption(commandInterfaceCFReason, onComplete);
    }

    public void setVtCallForwardingOption(int commandInterfaceCFReason,
                                          int commandInterfaceCFAction,
                                          String dialingNumber,
                                          int timerSeconds,
                                          Message onComplete) {
        mActivePhone.setVtCallForwardingOption(commandInterfaceCFReason,
                                               commandInterfaceCFAction,
                                               dialingNumber,
                                               timerSeconds,
                                               onComplete);
    }

    public void getVtCallWaiting(Message onComplete) {
        mActivePhone.getVtCallWaiting(onComplete);
    }

    public void setVtCallWaiting(boolean enable, Message onComplete) {
        mActivePhone.setVtCallWaiting(enable, onComplete);
    }

    public void getVtFacilityLock(String facility, String password, Message onComplete) {
        mActivePhone.getVtFacilityLock(facility, password, onComplete);
    }

    public void setVtFacilityLock(String facility, boolean enable, String password, Message onComplete) {
        mActivePhone.setVtFacilityLock(facility, enable, password, onComplete);
    }
    /* vt end */

    public void setGprsTransferType(int type, Message response) {
        mActivePhone.setGprsTransferType(type, response);
    }
    public IccFileHandler getIccFileHandler(){
	if(mActivePhone.getPhoneType() == Phone.PHONE_TYPE_GSM)
	{ 
	    return ((GSMPhone)mActivePhone).getIccFileHandler();
	}
	else
	{
	    return ((CDMAPhone)mActivePhone).getIccFileHandler();
	}
    }

    public IccServiceStatus getIccServiceStatus(IccService enService){
	if(mActivePhone.getPhoneType() == Phone.PHONE_TYPE_GSM)
	{ 	    
	    return ((GSMPhone)mActivePhone).getIccServiceStatus(enService);
	}
	else
	{
	    return ((CDMAPhone)mActivePhone).getIccServiceStatus(enService);
	}
    }

    public void  sendBTSIMProfile(int nAction, int nType, String strData, Message response) {
         mActivePhone.sendBTSIMProfile(nAction, nType, strData, response);
    }

    public void updateSimIndicateState(){
        mActivePhone.updateSimIndicateState();
    }

    public int getSimIndicateState(){
        return mActivePhone.getSimIndicateState();
    }

    public boolean isSimInsert(){
        return mActivePhone.isSimInsert();
    }

    public void doSimAuthentication (String strRand, Message result) {
        mActivePhone.doSimAuthentication(strRand,  result);
    }

    public void doUSimAuthentication (String strRand, String strAutn, Message result) {
	 mActivePhone.doUSimAuthentication(strRand, strAutn, result);
    }
//MTK-START [mtk80601][111212][ALPS00093395]IPO feature
    public void setRadioPowerOn(){
        mActivePhone.setRadioPowerOn();
    }
//MTK-END [mtk80601][111212][ALPS00093395]IPO feature
    public void updateMobileData() {
        ((PhoneBase)mActivePhone).updateMobileData();
    }

/* 3G switch start */
    public int get3GCapabilitySIM() {
        return mActivePhone.get3GCapabilitySIM();
    }

    public boolean set3GCapabilitySIM(int simId) {
        return mActivePhone.set3GCapabilitySIM(simId);
    }

    public boolean isGSMRadioAvailable() {
        if (mActivePhone instanceof GSMPhone)
            return ((GSMPhone)mActivePhone).isRadioAvailable();
        else
            return false;
    }
    public boolean is3GSwitchEnable() {
        return FeatureOption.MTK_GEMINI_3G_SWITCH;
    }
/* 3G switch end */

    public boolean isWCDMAPrefered() {
        return FeatureOption.MTK_RAT_WCDMA_PREFERRED;
    }

    public void getPOLCapability(Message onComplete) {
        mActivePhone.getPOLCapability(onComplete);
    }

    public void getPreferedOperatorList(Message onComplete) {
        mActivePhone.getPreferedOperatorList(onComplete);
    }
    
    public void setPOLEntry(NetworkInfoWithAcT networkWithAct, Message onComplete) {
        mActivePhone.setPOLEntry(networkWithAct, onComplete);
    }

    public boolean isGPRSDefaultOn() {
        return mGPRSOn;
    }

    public void forceNotifyServiceStateChange() {
        if (mActivePhone instanceof GSMPhone)
            ((GSMPhone)mActivePhone).forceNotifyServiceStateChange();
    }

    public void setPreferredNetworkTypeRIL(int NetworkType) {
        logd("PhoneProxy,setPreferredNetworkTypeRIL,Type="+ NetworkType);
        if (mActivePhone instanceof GSMPhone)
           ((GSMPhone)mActivePhone).setPreferredNetworkTypeRIL(NetworkType);
        else
           logd("mActivePhone is not GSMPhone");

    }

    public void setCurrentPreferredNetworkType() {
        logd("PhoneProxy,setCurrentPreferredNetworkType");
        if (mActivePhone instanceof GSMPhone)
            ((GSMPhone)mActivePhone).setCurrentPreferredNetworkType();
        else
            logd("mActivePhone is not GSMPhone");
    }
	
    //ALPS00279048
    public void setCRO(int mode, Message onComplete) {
        mActivePhone.setCRO(mode, onComplete);
    }
	
    // ALPS00294581
    public void notifySimMissingStatus(boolean isSimInsert) {
        mActivePhone.notifySimMissingStatus(isSimInsert);
    }
    //MTK-END [mtk04070][111117][ALPS00093395]MTK proprietary methods

    //via support start
    public void requestSwitchHPF (boolean enableHPF, Message response) {
        mActivePhone.requestSwitchHPF(enableHPF, response);
    }

    public void setAvoidSYS (boolean avoidSYS, Message response) {
        mActivePhone.setAvoidSYS(avoidSYS, response);
    }

    public void getAvoidSYSList (Message response) {
        mActivePhone.getAvoidSYSList(response);
    }

    public void queryCDMANetworkInfo (Message response){
        mActivePhone.queryCDMANetworkInfo(response);
    }
    //via support end

    // ALPS00296353 MVNO START
    public String getSpNameInEfSpn() {
        return mActivePhone.getSpNameInEfSpn();
    }

    public String getSpNameInEfSpn(int simId) {
        if(simId == this.getMySimId())
            return mActivePhone.getSpNameInEfSpn();
        logd("Can't use getSpNameInEfSpn(int simId) in PhoneProxy, because " + simId + " != " + this.getMySimId());
        return null;
    }

    public String isOperatorMvnoForImsi() {
        return mActivePhone.isOperatorMvnoForImsi();
    }

    public String isOperatorMvnoForImsi(int simId) {
        if(simId == this.getMySimId())
            return mActivePhone.isOperatorMvnoForImsi();
        logd("Can't use isOperatorMvnoForImsi(int simId) in PhoneProxy, because " + simId + " != " + this.getMySimId());
        return null;
    }

    public boolean isIccCardProviderAsMvno() {
        return mActivePhone.isIccCardProviderAsMvno();
    }

    public boolean isIccCardProviderAsMvno(int simId) {
        if(simId == this.getMySimId())
            return mActivePhone.isIccCardProviderAsMvno();
        logd("Can't use isIccCardProviderAsMvno(int simId) in PhoneProxy, because " + simId + " != " + this.getMySimId());
        return false;
    }
    // ALPS00296353 MVNO END
}
