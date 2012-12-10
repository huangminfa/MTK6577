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


import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.provider.Telephony.Sms.Intents;
import android.telephony.SmsManager;
import android.telephony.SmsMessage.MessageClass;
import android.util.Log;

import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsMessageBase.TextEncodingDetails;
import com.android.internal.telephony.SmsStorageMonitor;
import com.android.internal.telephony.SmsUsageMonitor;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.telephony.WspTypeDecoder;
import com.android.internal.telephony.cdma.sms.SmsEnvelope;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.IccUtils;
import com.android.internal.util.HexDump;
import com.android.internal.util.BitwiseInputStream;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;

import android.content.res.Resources;

// MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
import android.os.AsyncResult;
import android.util.Config;
import com.android.internal.telephony.SmsRawData;
import java.util.ArrayList;
import java.util.List;

import static android.telephony.SmsManager.STATUS_ON_ICC_READ;
import static android.telephony.SmsManager.STATUS_ON_ICC_UNREAD;
import static android.telephony.SmsManager.STATUS_ON_ICC_SENT;
import static android.telephony.SmsManager.STATUS_ON_ICC_UNSENT;

import static android.telephony.SmsManager.RESULT_ERROR_SUCCESS;
import static android.telephony.SmsManager.RESULT_ERROR_SIM_MEM_FULL;
import static android.telephony.SmsManager.RESULT_ERROR_GENERIC_FAILURE;
import static android.telephony.SmsManager.RESULT_ERROR_NULL_PDU;
import static android.telephony.SmsManager.RESULT_ERROR_INVALID_ADDRESS;
// MTK-END   [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16

import android.os.Bundle;

final class CdmaSMSDispatcher extends SMSDispatcher {
    private static final String TAG = "CDMA";
    
    // VIA add for query sms registe feasibility begin
    private static final int EVENT_RUIM_READY = 2000;
    private static final int EVENT_QUERY_CDMA_NETWORK_REGISTER_STATE = 2001;
    private static final int EVENT_QUERY_CDMA_NETWORK_REGISTER_STATE_DONE = 2002;
    private static final int EVENT_QUERY_CDMA_MODEM_SMS_INIT_STATE = 2003;
    private static final int EVENT_QUERY_CDMA_MODEM_SMS_INIT_STATE_DONE = 2004;

    /* retry max times */
    private static final int QUERY_SMS_REGISTER_FEASIBILITY_MAX_RETRY_TIMES = 100;

    private int mQueryCDMASmsRegisterFeasibilityTimes = 0;
    private boolean mCdmaNetworkRegistered = false;
    private boolean mCdmaModemSmsInitDone = false;
    // VIA add end

    private byte[] mLastDispatchedSmsFingerprint;
    private byte[] mLastAcknowledgedSmsFingerprint;

    private final boolean mCheckForDuplicatePortsInOmadmWapPush = Resources.getSystem().getBoolean(
            com.android.internal.R.bool.config_duplicate_port_omadm_wappush);

    CdmaSMSDispatcher(CDMAPhone phone, SmsStorageMonitor storageMonitor,
            SmsUsageMonitor usageMonitor) {
        super(phone, storageMonitor, usageMonitor);
        mCm.setOnNewCdmaSms(this, EVENT_NEW_SMS, null);
        // VIA add begin
        mCm.registerForRUIMReady(this, EVENT_RUIM_READY, null);
        // VIA add end
    }
    
    // VIA add for query sms registe feasibility begin
    private void notifyCdmaSmsAutoRegisterBeFeasible() {
        if (!mCdmaNetworkRegistered || !mCdmaModemSmsInitDone) {
               Log.e(TAG, "notifyCdmaSmsAutoRegisterBeFeasible failed for not both true");
               Log.e(TAG, "mCdmaNetworkRegistered = " + mCdmaNetworkRegistered + " , mCdmaModemSmsInitDone = " + mCdmaModemSmsInitDone);
               return;
        }

        Intent intent = new Intent(Intents.CDMA_AUTO_SMS_REGISTER_FEASIBLE_ACTION);
        dispatch(intent, "android.permission.SEND_SMS");
        Log.d(TAG, "viacode, app can do auto sms register work now");
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case EVENT_RUIM_READY:
            Log.d(TAG, "viacode, received EVENT_RUIM_READY in CDMASmsDispather");
            mQueryCDMASmsRegisterFeasibilityTimes = 0;
            mQueryCDMASmsRegisterFeasibilityTimes++;
            mCm.queryCDMANetWorkRegistrationState(obtainMessage(EVENT_QUERY_CDMA_NETWORK_REGISTER_STATE_DONE));
            break;
        case EVENT_QUERY_CDMA_NETWORK_REGISTER_STATE:
            mQueryCDMASmsRegisterFeasibilityTimes++;
            Log.d(TAG, "viacode, do network register state query again, retry times = " + mQueryCDMASmsRegisterFeasibilityTimes);
            if (mQueryCDMASmsRegisterFeasibilityTimes > QUERY_SMS_REGISTER_FEASIBILITY_MAX_RETRY_TIMES) {
               Log.e(TAG, "over retry limits(" + QUERY_SMS_REGISTER_FEASIBILITY_MAX_RETRY_TIMES
                               + "), query cdma sms register feasibility failed");
                 // over the retry times limits, give up querying
            } else {
                mCm.queryCDMANetWorkRegistrationState(obtainMessage(EVENT_QUERY_CDMA_NETWORK_REGISTER_STATE_DONE));
            }
            break;

        case EVENT_QUERY_CDMA_NETWORK_REGISTER_STATE_DONE:
            AsyncResult aresult = (AsyncResult) msg.obj;
            if (aresult != null) {
                int res[] = (int[])aresult.result;
                if (res != null && res.length == 1 && res[0] == 1) {
                    // network register done! to query modem sms init state
                    // reset times
                    mQueryCDMASmsRegisterFeasibilityTimes = 0;
                    mCdmaNetworkRegistered = true;
                    sendEmptyMessage(EVENT_QUERY_CDMA_MODEM_SMS_INIT_STATE);
                } else {
                    // not ok , query again after 2s
                    sendEmptyMessageDelayed(EVENT_QUERY_CDMA_NETWORK_REGISTER_STATE, 2000);
                }
            } else {
                    // not ok , query again after 2s
                    sendEmptyMessageDelayed(EVENT_QUERY_CDMA_NETWORK_REGISTER_STATE, 2000);
            }
            break;

        case EVENT_QUERY_CDMA_MODEM_SMS_INIT_STATE:
            mQueryCDMASmsRegisterFeasibilityTimes++;
            Log.d(TAG, "viacode, do sms init state query again, retry times = " + mQueryCDMASmsRegisterFeasibilityTimes);
            if (mQueryCDMASmsRegisterFeasibilityTimes > QUERY_SMS_REGISTER_FEASIBILITY_MAX_RETRY_TIMES) {
               Log.e(TAG, "over retry limits(" + QUERY_SMS_REGISTER_FEASIBILITY_MAX_RETRY_TIMES
                               + "), query cdma modem sms init state failed");
                 // over the retry times limits, give up querying
            } else {
                mCm.queryCDMASmsAndPBStatus(obtainMessage(EVENT_QUERY_CDMA_MODEM_SMS_INIT_STATE_DONE));
            }
            break;

        case EVENT_QUERY_CDMA_MODEM_SMS_INIT_STATE_DONE:
            AsyncResult asyncresult = (AsyncResult) msg.obj;
            if (asyncresult != null) {
                int queryres[] = (int[])asyncresult.result;
                if (queryres != null && queryres.length == 2 && queryres[0] == 1) {
                    // modem sms init done! to broadcast
                    // reset times
                    mQueryCDMASmsRegisterFeasibilityTimes = 0;
                    mCdmaModemSmsInitDone = true;
                    notifyCdmaSmsAutoRegisterBeFeasible();
                } else {
                    // not ok , query again after 2s
                    sendEmptyMessageDelayed(EVENT_QUERY_CDMA_MODEM_SMS_INIT_STATE, 2000);
                }
            } else {
                    // not ok , query again after 2s
                    sendEmptyMessageDelayed(EVENT_QUERY_CDMA_MODEM_SMS_INIT_STATE, 2000);
            }
            break;

        default:
            super.handleMessage(msg);
        }
    }
    // VIA add end

    @Override
    public void dispose() {
        mCm.unSetOnNewCdmaSms(this);
        //via add for China Telecom auto-register sms begin
        mCm.unregisterForRUIMReady(this);
        //via add for China Telecom auto-register sms end
    }

    @Override
    protected String getFormat() {
        return android.telephony.SmsMessage.FORMAT_3GPP2;
    }

    private void handleCdmaStatusReport(SmsMessage sms) {
        for (int i = 0, count = deliveryPendingList.size(); i < count; i++) {
            SmsTracker tracker = deliveryPendingList.get(i);
            if (tracker.mMessageRef == sms.messageRef) {
                // Found it.  Remove from list and broadcast.
                deliveryPendingList.remove(i);
                PendingIntent intent = tracker.mDeliveryIntent;
                Intent fillIn = new Intent();
                fillIn.putExtra("pdu", sms.getPdu());
                fillIn.putExtra("format", android.telephony.SmsMessage.FORMAT_3GPP2);
                try {
                    intent.send(mContext, Activity.RESULT_OK, fillIn);
                } catch (CanceledException ex) {}
                break;  // Only expect to see one tracker matching this message.
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public int dispatchMessage(SmsMessageBase smsb) {

        // If sms is null, means there was a parsing error.
        if (smsb == null) {
            Log.e(TAG, "dispatchMessage: message is null");
            return Intents.RESULT_SMS_GENERIC_ERROR;
        }

        String inEcm=SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE, "false");
        if (inEcm.equals("true")) {
            return Activity.RESULT_OK;
        }

        if (mSmsReceiveDisabled) {
            // Device doesn't support receiving SMS,
            Log.d(TAG, "Received short message on device which doesn't support "
                    + "receiving SMS. Ignored.");
            return Intents.RESULT_SMS_HANDLED;
        }

        // See if we have a network duplicate SMS.
        SmsMessage sms = (SmsMessage) smsb;
        mLastDispatchedSmsFingerprint = sms.getIncomingSmsFingerprint();
        if (mLastAcknowledgedSmsFingerprint != null &&
                Arrays.equals(mLastDispatchedSmsFingerprint, mLastAcknowledgedSmsFingerprint)) {
            return Intents.RESULT_SMS_HANDLED;
        }
        // Decode BD stream and set sms variables.
        sms.parseSms();
        int teleService = sms.getTeleService();
        boolean handled = false;

        if ((SmsEnvelope.TELESERVICE_VMN == teleService) ||
                (SmsEnvelope.TELESERVICE_MWI == teleService)) {
            // handling Voicemail
            int voicemailCount = sms.getNumOfVoicemails();
            Log.d(TAG, "Voicemail count=" + voicemailCount);
            // Store the voicemail count in preferences.
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
                    mContext);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(CDMAPhone.VM_COUNT_CDMA, voicemailCount);
            editor.apply();
            mPhone.setVoiceMessageWaiting(1, voicemailCount);
            handled = true;
        } else if (((SmsEnvelope.TELESERVICE_WMT == teleService) ||
                (SmsEnvelope.TELESERVICE_WEMT == teleService)) &&
                sms.isStatusReportMessage()) {
            handleCdmaStatusReport(sms);
            handled = true;
        } else if ((sms.getUserData() == null)) {
            if (false) {
                Log.d(TAG, "Received SMS without user data");
            }
            handled = true;
        }

        if (handled) {
            return Intents.RESULT_SMS_HANDLED;
        }

        if (!mStorageMonitor.isStorageAvailable() &&
                sms.getMessageClass() != MessageClass.CLASS_0) {
            // It's a storable message and there's no storage available.  Bail.
            // (See C.S0015-B v2.0 for a description of "Immediate Display"
            // messages, which we represent as CLASS_0.)
            return Intents.RESULT_SMS_OUT_OF_MEMORY;
        }

        if (SmsEnvelope.TELESERVICE_WAP == teleService || SmsEnvelope.TELESERVICE_WAP_CT == teleService) {
            byte[] userData = null;
            try {
                BitwiseInputStream inStream = new BitwiseInputStream(sms.getUserData());
                inStream.skip(8 * 8 + 5);

                int len = inStream.available() / 8;
                userData = new byte[len];
                for (int i = 0; i < len; i++ ){
                    userData[i] = (byte)inStream.read(8);
                }
            }
            catch(BitwiseInputStream.AccessException ex){
                Log.e(TAG, "process wap pdu fail");
            }

            return processCdmaWapPdu(userData, sms.messageRef,
                    sms.getOriginatingAddress());
        }

        // Reject (NAK) any messages with teleservice ids that have
        // not yet been handled and also do not correspond to the two
        // kinds that are processed below.
        if ((SmsEnvelope.TELESERVICE_WMT != teleService) &&
                (SmsEnvelope.TELESERVICE_WEMT != teleService) &&
                (SmsEnvelope.MESSAGE_TYPE_BROADCAST != sms.getMessageType())) {
            return Intents.RESULT_SMS_UNSUPPORTED;
        }

        return dispatchNormalMessage(smsb);
    }

    /**
     * Processes inbound messages that are in the WAP-WDP PDU format. See
     * wap-259-wdp-20010614-a section 6.5 for details on the WAP-WDP PDU format.
     * WDP segments are gathered until a datagram completes and gets dispatched.
     *
     * @param pdu The WAP-WDP PDU segment
     * @return a result code from {@link Telephony.Sms.Intents}, or
     *         {@link Activity#RESULT_OK} if the message has been broadcast
     *         to applications
     */
    protected int processCdmaWapPdu(byte[] pdu, int referenceNumber, String address) {
        int index = 0;

        int msgType = (0xFF & pdu[index++]);
        if (msgType != 0) {
            Log.w(TAG, "Received a WAP SMS which is not WDP. Discard.");
            return Intents.RESULT_SMS_HANDLED;
        }
        int totalSegments = (0xFF & pdu[index++]);   // >= 1
        int segment = (0xFF & pdu[index++]);         // >= 0

        if (segment >= totalSegments) {
            Log.e(TAG, "WDP bad segment #" + segment + " expecting 0-" + (totalSegments - 1));
            return Intents.RESULT_SMS_HANDLED;
        }

        // Only the first segment contains sourcePort and destination Port
        int sourcePort = 0;
        int destinationPort = 0;
        if (segment == 0) {
            //process WDP segment
            sourcePort = (0xFF & pdu[index++]) << 8;
            sourcePort |= 0xFF & pdu[index++];
            destinationPort = (0xFF & pdu[index++]) << 8;
            destinationPort |= 0xFF & pdu[index++];
            // Some carriers incorrectly send duplicate port fields in omadm wap pushes.
            // If configured, check for that here
            if (mCheckForDuplicatePortsInOmadmWapPush) {
                if (checkDuplicatePortOmadmWappush(pdu,index)) {
                    index = index + 4; // skip duplicate port fields
                }
            }
        }

        // Lookup all other related parts
        Log.i(TAG, "Received WAP PDU. Type = " + msgType + ", originator = " + address
                + ", src-port = " + sourcePort + ", dst-port = " + destinationPort
                + ", ID = " + referenceNumber + ", segment# = " + segment + '/' + totalSegments);

        // pass the user data portion of the PDU to the shared handler in SMSDispatcher
        byte[] userData = new byte[pdu.length - index];
        System.arraycopy(pdu, index, userData, 0, pdu.length - index);

        return processMessagePart(userData, address, referenceNumber, segment, totalSegments,
                0L, destinationPort, true);
    }

    /** {@inheritDoc} */
    @Override
    protected void sendData(String destAddr, String scAddr, int destPort,
            byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        SmsMessage.SubmitPdu pdu = SmsMessage.getSubmitPdu(
                scAddr, destAddr, destPort, data, (deliveryIntent != null));
        sendSubmitPdu(pdu, sentIntent, deliveryIntent);
    }

    /** {@inheritDoc} */
    @Override
    protected void sendText(String destAddr, String scAddr, String text,
            PendingIntent sentIntent, PendingIntent deliveryIntent) {
        SmsMessage.SubmitPdu pdu = SmsMessage.getSubmitPdu(
                scAddr, destAddr, text, (deliveryIntent != null), null);
        sendSubmitPdu(pdu, sentIntent, deliveryIntent);
    }

    /** {@inheritDoc} */
    @Override
    protected TextEncodingDetails calculateLength(CharSequence messageBody,
            boolean use7bitOnly) {
        return SmsMessage.calculateLength(messageBody, use7bitOnly);
    }

    /** {@inheritDoc} */
    @Override
    protected void sendNewSubmitPdu(String destinationAddress, String scAddress,
            String message, SmsHeader smsHeader, int encoding,
            PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart) {
        UserData uData = new UserData();
        uData.payloadStr = message;
        uData.userDataHeader = smsHeader;
        if (encoding == android.telephony.SmsMessage.ENCODING_7BIT) {
            uData.msgEncoding = UserData.ENCODING_7BIT_ASCII;
        } else { // assume UTF-16
            uData.msgEncoding = UserData.ENCODING_UNICODE_16;
        }
        uData.msgEncodingSet = true;

        /* By setting the statusReportRequested bit only for the
         * last message fragment, this will result in only one
         * callback to the sender when that last fragment delivery
         * has been acknowledged. */
        SmsMessage.SubmitPdu submitPdu = SmsMessage.getSubmitPdu(destinationAddress,
                uData, (deliveryIntent != null) && lastPart);

        sendSubmitPdu(submitPdu, sentIntent, deliveryIntent);
    }

    protected void sendSubmitPdu(SmsMessage.SubmitPdu pdu,
            PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (SystemProperties.getBoolean(TelephonyProperties.PROPERTY_INECM_MODE, false)) {
            if (sentIntent != null) {
                try {
                    sentIntent.send(SmsManager.RESULT_ERROR_NO_SERVICE);
                } catch (CanceledException ex) {}
            }
            if (false) {
                Log.d(TAG, "Block SMS in Emergency Callback mode");
            }
            return;
        }
        sendRawPdu(pdu.encodedScAddress, pdu.encodedMessage, sentIntent, deliveryIntent);
    }

    /** {@inheritDoc} */
    @Override
    protected void sendSms(SmsTracker tracker) {
        HashMap<String, Object> map = tracker.mData;

        // byte smsc[] = (byte[]) map.get("smsc");  // unused for CDMA
        byte pdu[] = (byte[]) map.get("pdu");

        Message reply = obtainMessage(EVENT_SEND_SMS_COMPLETE, tracker);
        mCm.sendCdmaSms(pdu, reply);
    }

    /** {@inheritDoc} */
    @Override
    protected void acknowledgeLastIncomingSms(boolean success, int result, Message response) {
        String inEcm=SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE, "false");
        if (inEcm.equals("true")) {
            return;
        }

        int causeCode = resultToCause(result);
        mCm.acknowledgeLastIncomingCdmaSms(success, causeCode, response);

        if (causeCode == 0) {
            mLastAcknowledgedSmsFingerprint = mLastDispatchedSmsFingerprint;
        }
        mLastDispatchedSmsFingerprint = null;
    }

    private static int resultToCause(int rc) {
        switch (rc) {
        case Activity.RESULT_OK:
        case Intents.RESULT_SMS_HANDLED:
            // Cause code is ignored on success.
            return 0;
        case Intents.RESULT_SMS_OUT_OF_MEMORY:
            return CommandsInterface.CDMA_SMS_FAIL_CAUSE_RESOURCE_SHORTAGE;
        case Intents.RESULT_SMS_UNSUPPORTED:
            return CommandsInterface.CDMA_SMS_FAIL_CAUSE_INVALID_TELESERVICE_ID;
        case Intents.RESULT_SMS_GENERIC_ERROR:
        default:
            return CommandsInterface.CDMA_SMS_FAIL_CAUSE_ENCODING_PROBLEM;
        }
    }

    /**
     * Optional check to see if the received WapPush is an OMADM notification with erroneous
     * extra port fields.
     * - Some carriers make this mistake.
     * ex: MSGTYPE-TotalSegments-CurrentSegment
     *       -SourcePortDestPort-SourcePortDestPort-OMADM PDU
     * @param origPdu The WAP-WDP PDU segment
     * @param index Current Index while parsing the PDU.
     * @return True if OrigPdu is OmaDM Push Message which has duplicate ports.
     *         False if OrigPdu is NOT OmaDM Push Message which has duplicate ports.
     */
    private static boolean checkDuplicatePortOmadmWappush(byte[] origPdu, int index) {
        index += 4;
        byte[] omaPdu = new byte[origPdu.length - index];
        System.arraycopy(origPdu, index, omaPdu, 0, omaPdu.length);

        WspTypeDecoder pduDecoder = new WspTypeDecoder(omaPdu);
        int wspIndex = 2;

        // Process header length field
        if (pduDecoder.decodeUintvarInteger(wspIndex) == false) {
            return false;
        }

        wspIndex += pduDecoder.getDecodedDataLength(); // advance to next field

        // Process content type field
        if (pduDecoder.decodeContentType(wspIndex) == false) {
            return false;
        }

        String mimeType = pduDecoder.getValueString();
        if (mimeType != null && mimeType.equals(WspTypeDecoder.CONTENT_TYPE_B_PUSH_SYNCML_NOTI)) {
            return true;
        }
        return false;
    }

    // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    /** {@inheritDoc} */
    protected void sendData(String destAddr, String scAddr, int destPort, int originalPort,
            byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        // impl
        Log.d(TAG, "No action in cdma phone");
    }

    /** {@inheritDoc} */
    protected void sendMultipartData(
            String destAddr, String scAddr, int destPort,
            ArrayList<SmsRawData> data, ArrayList<PendingIntent> sentIntents, 
            ArrayList<PendingIntent> deliveryIntents) {
        // impl
        Log.e(TAG, "Error! The functionality sendMultipartData is not implemented for CDMA.");
    }

    /** {@inheritDoc} */
    protected void sendText(String destAddr, String scAddr, String text, 
            int destPort,PendingIntent sentIntent, PendingIntent deliveryIntent) {

        Log.e(TAG, "Error! The functionality sendText with port is not implemented for CDMA.");
    }

    /** {@inheritDoc} */
    protected void sendMultipartText(String destAddr, String scAddr,
            ArrayList<String> parts, int destPort, ArrayList<PendingIntent> sentIntents,
            ArrayList<PendingIntent> deliveryIntents) {

        Log.e(TAG, "Error! The functionality sendMultipartText with port is not implemented for CDMA.");
    }

    /** {@inheritDoc} */
    protected void activateCellBroadcastSms(int activate, Message response) {
        mCm.setCdmaBroadcastActivation((activate == 0), response);
    }

    /** {@inheritDoc} */
    protected void getCellBroadcastSmsConfig(Message response) {
        mCm.getCdmaBroadcastConfig(response);
    }

    /** {@inheritDoc} */
    protected void setCellBroadcastConfig(int[] configValuesArray, Message response) {
        mCm.setCdmaBroadcastConfig(configValuesArray, response);
    }

    public int copyTextMessageToIccCard(String scAddress, String address, List<String> text,
                    int status, long timestamp) {
        Log.d(TAG, "CDMASMSDispatcher: copy text message to icc card");
        /*
        if(checkPhoneNumber(scAddress)) {
            Log.d(TAG, "[copyText invalid sc address");
            scAddress = null;
        }
        
        if(checkPhoneNumber(address) == false) {
            Log.d(TAG, "[copyText invalid dest address");
            return RESULT_ERROR_INVALID_ADDRESS;
        }*/

        mSuccess = true;

        int msgCount = text.size();
        // we should check the available storage of SIM here,
        // but now we suppose it always be true
        if(true) {
            Log.d(TAG, "[copyText storage available");
        } else {
            Log.d(TAG, "[copyText storage unavailable");
            return RESULT_ERROR_SIM_MEM_FULL;
        }

        if(status == STATUS_ON_ICC_READ || status == STATUS_ON_ICC_UNREAD) {
            Log.d(TAG, "[copyText to encode deliver pdu");
        } else if(status == STATUS_ON_ICC_SENT || status == STATUS_ON_ICC_UNSENT) {
            Log.d(TAG, "[copyText to encode submit pdu");
        } else {
            Log.d(TAG, "[copyText invalid status, default is deliver pdu");
            return RESULT_ERROR_GENERIC_FAILURE;
        }

        Log.d(TAG, "[copyText msgCount " + msgCount);
        if(msgCount > 1) {
            Log.d(TAG, "[copyText multi-part message");
        } else if(msgCount == 1) {
            Log.d(TAG, "[copyText single-part message");
        } else {
            Log.d(TAG, "[copyText invalid message count");
            return RESULT_ERROR_GENERIC_FAILURE;
        }
    
        for(int i = 0; i < msgCount; ++i) {
            if(mSuccess == false) {
                Log.d(TAG, "[copyText Exception happened when copy message");
                return RESULT_ERROR_GENERIC_FAILURE;
            }

            SmsMessage.SubmitPdu pdu = SmsMessage.createEfPdu(address, text.get(i), timestamp);
      
            if(pdu != null) {
                Log.d(TAG, "[copyText write submit pdu into UIM");
                mCm.writeSmsToRuim(status, IccUtils.bytesToHexString(pdu.encodedMessage), obtainMessage(EVENT_COPY_TEXT_MESSAGE_DONE));
            }
  
            synchronized (mLock) {
                try {
                    Log.d(TAG, "[copyText wait until the message be wrote in UIM");
                    mLock.wait();
                } catch(InterruptedException e) {
                    Log.d(TAG, "[copyText interrupted while trying to copy text message into UIM");
                    return RESULT_ERROR_GENERIC_FAILURE;
                }
            }
            Log.d(TAG, "[copyText thread is waked up");
        }

        if(mSuccess == true) {
            Log.d(TAG, "[copyText all messages have been copied into UIM");
            return RESULT_ERROR_SUCCESS;
        }

        Log.d(TAG, "[copyText copy failed");
        return RESULT_ERROR_GENERIC_FAILURE;
    }
    // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    
    // MTK-START [ALPS00094531] Orange feature SMS Encoding Type Setting by mtk80589 in 2011.11.22
    /** {@inheritDoc} */
    protected void sendTextWithEncodingType(
            String destAddr,
            String scAddr,
            String text,
            int encodingType,
            PendingIntent sentIntent, 
            PendingIntent deliveryIntent) {
        // impl
        Log.d(TAG, "CdmaSMSDispatcher: don't support this function on cdma phone");
    }
    
    /** {@inheritDoc} */
    protected void sendMultipartTextWithEncodingType(
            String destAddr,
            String scAddr,
            ArrayList<String> parts,
            int encodingType,
            ArrayList<PendingIntent> sentIntents,
            ArrayList<PendingIntent> deliveryIntents) {
        // impl
        Log.d(TAG, "CdmaSMSDispatcher: don't support this function on cdma phone");
    }
    // MTK-END [ALPS00094531] Orange feature SMS Encoding Type Setting by mtk80589 in 2011.11.22
    
    /** {@inheritDoc} */
    public void sendTextWithExtraParams(
            String destAddr,
            String scAddr,
            String text,
            Bundle extraParams,
            PendingIntent sentIntent,
            PendingIntent deliveryIntent) {
        // impl
        Log.d(TAG, "CdmaSMSDispatcher: don't support this method on cdma phone");
    }
    
    /** {@inheritDoc} */
    public void sendMultipartTextWithExtraParams(
            String destAddr,
            String scAddr,
            ArrayList<String> parts,
            Bundle extraParams,
            ArrayList<PendingIntent> sentIntents,
            ArrayList<PendingIntent> deliveryIntents) {
       // impl
       Log.d(TAG, "CdmaSMSDispatcher: don't support this method on cdma phone");
    }
    
    protected android.telephony.SmsMessage createMessageFromSubmitPdu(byte[] smsc, byte[] tpdu) {
        return android.telephony.SmsMessage.createFromPdu(RuimSmsInterfaces.convertSubmitpduToPdu(tpdu), getFormat());
    }
}
