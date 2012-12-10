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

package com.android.internal.telephony.sip;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.android.internal.telephony.BaseCommands;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.PhbEntry;

/**
 * SIP doesn't need CommandsInterface. The class does nothing but made to work
 * with PhoneBase's constructor.
 */
class SipCommandInterface extends BaseCommands implements CommandsInterface {
    SipCommandInterface(Context context) {
        super(context);
    }

    @Override public void setOnNITZTime(Handler h, int what, Object obj) {
    }

    public void getIccCardStatus(Message result) {
    }

    public void supplyIccPin(String pin, Message result) {
    }

    public void supplyIccPuk(String puk, String newPin, Message result) {
    }

    public void supplyIccPin2(String pin, Message result) {
    }

    public void supplyIccPuk2(String puk, String newPin2, Message result) {
    }

    public void changeIccPin(String oldPin, String newPin, Message result) {
    }

    public void changeIccPin2(String oldPin2, String newPin2, Message result) {
    }

    public void changeBarringPassword(String facility, String oldPwd,
            String newPwd, Message result) {
    }

    public void supplyNetworkDepersonalization(String netpin, Message result) {
    }

    public void getCurrentCalls(Message result) {
    }

    @Deprecated public void getPDPContextList(Message result) {
    }

    public void getDataCallList(Message result) {
    }

    public void dial(String address, int clirMode, Message result) {
    }

    public void dial(String address, int clirMode, UUSInfo uusInfo,
            Message result) {
    }

    public void getIMSI(Message result) {
    }

    public void getIMEI(Message result) {
    }

    public void getIMEISV(Message result) {
    }


    public void hangupConnection (int gsmIndex, Message result) {
    }

    public void hangupWaitingOrBackground (Message result) {
    }

    public void hangupForegroundResumeBackground (Message result) {
    }

    public void switchWaitingOrHoldingAndActive (Message result) {
    }

    public void conference (Message result) {
    }


    public void setPreferredVoicePrivacy(boolean enable, Message result) {
    }

    public void getPreferredVoicePrivacy(Message result) {
    }

    public void separateConnection (int gsmIndex, Message result) {
    }

    public void acceptCall (Message result) {
    }

    public void rejectCall (Message result) {
    }

    public void explicitCallTransfer (Message result) {
    }

    public void getLastCallFailCause (Message result) {
    }

    /** @deprecated */
    public void getLastPdpFailCause (Message result) {
    }

    public void getLastDataCallFailCause (Message result) {
    }

    public void setMute (boolean enableMute, Message response) {
    }

    public void getMute (Message response) {
    }

    public void getSignalStrength (Message result) {
    }

    public void getVoiceRegistrationState (Message result) {
    }

    public void getDataRegistrationState (Message result) {
    }

    public void getOperator(Message result) {
    }

    public void sendDtmf(char c, Message result) {
    }

    public void startDtmf(char c, Message result) {
    }

    public void stopDtmf(Message result) {
    }

    public void sendBurstDtmf(String dtmfString, int on, int off,
            Message result) {
    }

    public void sendSMS (String smscPDU, String pdu, Message result) {
    }

    public void sendCdmaSms(byte[] pdu, Message result) {
    }

    public void deleteSmsOnSim(int index, Message response) {
    }

    public void deleteSmsOnRuim(int index, Message response) {
    }

    public void writeSmsToSim(int status, String smsc, String pdu, Message response) {
    }

    public void writeSmsToRuim(int status, String pdu, Message response) {
    }

    public void setupDataCall(String radioTechnology, String profile,
            String apn, String user, String password, String authType,
            String protocol, Message result) {
    }

    public void deactivateDataCall(int cid, int reason, Message result) {
    }

    public void setRadioPower(boolean on, Message result) {
    }

    public void setSuppServiceNotifications(boolean enable, Message result) {
    }

    public void acknowledgeLastIncomingGsmSms(boolean success, int cause,
            Message result) {
    }

    public void acknowledgeLastIncomingCdmaSms(boolean success, int cause,
            Message result) {
    }

    //MTK-START [mtk04070][111223][ALPS00106134]Merge to ICS 4.0.3
    public void acknowledgeIncomingGsmSmsWithPdu(boolean success, String ackPdu,
            Message result) {
    }
    //MTK-END [mtk04070][111223][ALPS00106134]Merge to ICS 4.0.3

    public void iccIO (int command, int fileid, String path, int p1, int p2,
            int p3, String data, String pin2, Message result) {
    }

    public void getCLIR(Message result) {
    }

    public void setCLIR(int clirMode, Message result) {
    }

    public void queryCallWaiting(int serviceClass, Message response) {
    }

    public void setCallWaiting(boolean enable, int serviceClass,
            Message response) {
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
    }

    public void setNetworkSelectionModeManual(
            String operatorNumeric, Message response) {
    }

    public void getNetworkSelectionMode(Message response) {
    }

    public void getAvailableNetworks(Message response) {
    }

    public void setCallForward(int action, int cfReason, int serviceClass,
                String number, int timeSeconds, Message response) {
    }

    public void queryCallForwardStatus(int cfReason, int serviceClass,
            String number, Message response) {
    }

    public void queryCLIP(Message response) {
    }

    public void getBasebandVersion (Message response) {
    }

    @Override
    public void queryFacilityLock(String facility, String password,
            int serviceClass, Message response) {
    }

    @Override
    public void queryFacilityLockForApp(String facility, String password,
            int serviceClass, String appId, Message response) {
    }

    @Override
    public void setFacilityLock(String facility, boolean lockState,
            String password, int serviceClass, Message response) {
    }

    @Override
    public void setFacilityLockForApp(String facility, boolean lockState,
            String password, int serviceClass, String appId, Message response) {
    }

    public void sendUSSD (String ussdString, Message response) {
    }

    public void cancelPendingUssd (Message response) {
    }

    public void resetRadio(Message result) {
    }

    public void invokeOemRilRequestRaw(byte[] data, Message response) {
    }

    public void invokeOemRilRequestStrings(String[] strings, Message response) {
    }

    public void setBandMode (int bandMode, Message response) {
    }

    public void queryAvailableBandMode (Message response) {
    }

    public void sendTerminalResponse(String contents, Message response) {
    }

    public void sendEnvelope(String contents, Message response) {
    }

    //MTK-START [mtk04070][111223][ALPS00106134]Merge to ICS 4.0.3
    public void sendEnvelopeWithStatus(String contents, Message response) {
    }
    //MTK-EMD [mtk04070][111223][ALPS00106134]Merge to ICS 4.0.3

    public void handleCallSetupRequestFromSim(
            boolean accept, Message response) {
    }

    public void setPreferredNetworkType(int networkType , Message response) {
    }

    public void getPreferredNetworkType(Message response) {
    }

    public void getNeighboringCids(Message response) {
    }

    public void setLocationUpdates(boolean enable, Message response) {
    }

    public void getSmscAddress(Message result) {
    }

    public void setSmscAddress(String address, Message result) {
    }

    public void reportSmsMemoryStatus(boolean available, Message result) {
    }

    @Override
    public void getCdmaSubscriptionSource(Message response) {
    }

    public void getGsmBroadcastConfig(Message response) {
    }

    public void setGsmBroadcastConfig(SmsBroadcastConfigInfo[] config, Message response) {
    }

    public void setGsmBroadcastActivation(boolean activate, Message response) {
    }

    // ***** Methods for CDMA support
    public void getDeviceIdentity(Message response) {
    }

    public void getCDMASubscription(Message response) {
    }

    public void setPhoneType(int phoneType) { //Set by CDMAPhone and GSMPhone constructor
    }

    public void queryCdmaRoamingPreference(Message response) {
    }

    public void setCdmaRoamingPreference(int cdmaRoamingType, Message response) {
    }

    public void setCdmaSubscriptionSource(int cdmaSubscription , Message response) {
    }

    public void queryTTYMode(Message response) {
    }

    public void setTTYMode(int ttyMode, Message response) {
    }

    public void sendCDMAFeatureCode(String FeatureCode, Message response) {
    }

    public void getCdmaBroadcastConfig(Message response) {
    }

    public void setCdmaBroadcastConfig(int[] configValuesArray, Message response) {
    }

    public void setCdmaBroadcastActivation(boolean activate, Message response) {
    }

    public void exitEmergencyCallbackMode(Message response) {
    }

    @Override
    public void supplyIccPinForApp(String pin, String aid, Message response) {
    }

    @Override
    public void supplyIccPukForApp(String puk, String newPin, String aid, Message response) {
    }

    @Override
    public void supplyIccPin2ForApp(String pin2, String aid, Message response) {
    }

    @Override
    public void supplyIccPuk2ForApp(String puk2, String newPin2, String aid, Message response) {
    }

    @Override
    public void changeIccPinForApp(String oldPin, String newPin, String aidPtr, Message response) {
    }

    @Override
    public void changeIccPin2ForApp(String oldPin2, String newPin2, String aidPtr,
            Message response) {
    }

    public void requestIsimAuthentication(String nonce, Message response) {
    }

    // NFC SEEK start
    public void iccExchangeAPDU(int cla, int command, int channel, int p1,
            int p2, int p3, String data, Message response) {
    }

    public void iccOpenChannel(String AID, Message response) {
    }

    public void iccCloseChannel(int channel, Message response) {
    }

    public void iccGetATR(Message result) {
    }
    // NFC SEEK end
	
    //Add functions by MTK
    public void getSmsSimMemoryStatus(Message result) {
    }

    public void reportStkServiceIsRunning(Message result) {
    }

    public void queryPhbStorageInfo(int type, Message response) {
    }
        
    public void ReadPhbEntry(int type, int bIndex, int eIndex, Message response) {        
    }
    
    public void writePhbEntry(PhbEntry entry, Message result) {
    }
    
    public void setScri(boolean forceRelease, Message response) {
    }
    
    public void setupDataCall(String radioTechnology, String profile, String apn, String user,
            String password, String authType, String protocol, String requestCid, Message result) {
    }

    public void handleCallSetupRequestFromSim(
            boolean accept, int resCode ,Message response) {
    }

    public void queryNetworkLock (int category, Message result){
    }
    
    public void setNetworkLock (int catagory, int lockop, String password,
        String data_imsi, String gid1, String gid2, Message result) {
    }
    
    public void getCOLP(Message response) {}
    
    public void setCOLP(boolean enable, Message response) {}
    
    public void getCOLR(Message response) {}

    public void setRadioPowerOff(Message result) {}

    public void setRadioMode(int mode, Message result) {}

    public void setGprsConnType(int mode, Message response) {}

    public void setGprsTransferType(int mode, Message response) {}
    
    public void getMobileRevisionAndIMEI(int type,Message result) {}
    
    public void setPpuAndCurrency(String currency, String ppu, String pin2, Message result) {}	
    
    public void setAccumulatedCallMeterMaximum(String acmmax, String pin2, Message result) {}
    
    public void resetAccumulatedCallMeter(String pin2, Message result) {}
    
    public void hangupAll(Message result) {}
    
    public void getCurrentCallMeter(Message result){}
    
    public void getAccumulatedCallMeter(Message result) {}
    
    public void getAccumulatedCallMeterMaximum(Message result) {}
    
    public void getPpuAndCurrency(Message result){}

    public void changeBarringPassword(String facility, String oldPwd, String newPwd, String newCfm, Message result) {}  

//MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
    // add utk from 2.3
    public void getUtkLocalInfo(Message response) {
    }
    
    public void requestUtkRefresh(int type, Message response) {
    }
    
    public void reportUtkServiceIsRunning(Message result) {
    }
    
    public void profileDownload(String profile, Message response) {
    }    

    public void handleCallSetupRequestFromUim(boolean accept, Message response) {
    }
//MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

    /* vt start  */
    public void vtDial (String address, int clirMode, Message result) {}

    public void vtDial (String address, int clirMode, UUSInfo uusInfo, Message result) {}

    public void voiceAccept(int callId, Message result) {}
    /* vt end */

    public void emergencyDial(String address, int clirMode, UUSInfo uusInfo, Message result) {
    }

    public void forceReleaseCall(int index, Message result) { }

    public void setCallIndication(int mode, int callId, int seqNumber, Message result) { }

    public void replaceVtCall(int index, Message result) {}

    public void get3GCapabilitySIM(Message response) {}

    public void set3GCapabilitySIM(int simId, Message response) {}

    public String lookupOperatorName(String numeric, boolean desireLongName) {
        return null;
    }

    // ADD BY via start
    public void getNitzTime(Message result) {}
    public void requestSwitchHPF (boolean enableHPF, Message response){}
    public void setAvoidSYS (boolean avoidSYS, Message response) {}
    public void getAvoidSYSList (Message response) {}
    public void queryCDMANetworkInfo (Message response){}
    //VIA-START VIA AGPS
    public void requestAGPSTcpConnected(int connected, Message result){    }
    public void requestAGPSSetMpcIpPort(String ip, String port, Message result){    }
    public void requestAGPSGetMpcIpPort(Message result){    }
    //VIA-END VIA AGPS
    // ADD BY via end

    //MTK-START [mtk80950][120410][ALPS00266631] check whether download calibration data or not
    public void getCalibrationData(Message result) {}
    //MTK-END [mtk80950][120410][ALPS00266631] check whether download calibration data or not

    //MTK-START [mt04258][121002] OEM unsol support
    public void setUnsolOemHookRowPrefix (String[] urcPrefixes, Message result) {}
    //MTK-END [mt04258][121002] OEM unsol support
    
    // VIA begin for China Telecom auto-register sms
    public void queryCDMASmsAndPBStatus (Message response) {}
    public void queryCDMANetWorkRegistrationState (Message response) {}
    // VIA end for China Telecom auto-register sms

//MTK-START [mtk04258][20120806][International card support]
    public void setRadioPowerCardSwitch(int powerOn, Message response) {}
    public void setSimInterfaceSwitch(int switchMode, Message response) {}
    public void continueMdInit(Message response) { }
    public void switchModem(int preferredNetworkType, int simId) {}
    public void getLimitServiceOperator(Message response) {}
//MTK-END [mtk04258][20120806][International card support]
}
