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

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Intents;
import android.telephony.ServiceState;
import android.telephony.SmsCbMessage;
import android.telephony.SmsManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IccUtils;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsMessageBase.TextEncodingDetails;
import com.android.internal.telephony.SmsStorageMonitor;
import com.android.internal.telephony.SmsUsageMonitor;
import com.android.internal.telephony.TelephonyProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static android.telephony.SmsMessage.MessageClass;

// MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.util.Config;
import android.database.Cursor;
import android.database.SQLException;
import android.content.ContentValues;
import com.android.internal.util.HexDump;

import com.android.internal.telephony.IccUtils;
import com.android.internal.telephony.SmsMessageBase.TextEncodingDetails;
import com.android.internal.telephony.gsm.SmsMessage;
import com.android.internal.telephony.BaseCommands;
import com.android.internal.telephony.SmsRawData;
import com.android.internal.telephony.BaseCommands;
import com.mediatek.featureoption.FeatureOption;
import java.util.List;

import static android.telephony.SmsManager.STATUS_ON_ICC_READ;
import static android.telephony.SmsManager.STATUS_ON_ICC_UNREAD;
import static android.telephony.SmsManager.STATUS_ON_ICC_SENT;
import static android.telephony.SmsManager.STATUS_ON_ICC_UNSENT;

import static android.telephony.SmsManager.RESULT_ERROR_SUCCESS;
import static android.telephony.SmsManager.RESULT_ERROR_SIM_MEM_FULL;
import static android.telephony.SmsManager.RESULT_ERROR_GENERIC_FAILURE;
import static android.telephony.SmsManager.RESULT_ERROR_NULL_PDU;

import static android.telephony.SmsMessage.ENCODING_UNKNOWN;
import static android.telephony.SmsMessage.ENCODING_7BIT;
import static android.telephony.SmsMessage.ENCODING_8BIT;
import static android.telephony.SmsMessage.ENCODING_16BIT;

// MTK-START [ALPS00094284] Filter hyphen of phone number by mtk80589 in 2011.11.23
import static android.telephony.SmsManager.RESULT_ERROR_INVALID_ADDRESS;
// MTK-END   [ALPS00094284] Filter hyphen of phone number by mtk80589 in 2011.11.23
// MTK-END   [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16

import static android.telephony.SmsManager.EXTRA_PARAMS_VALIDITY_PERIOD;
import static android.telephony.SmsManager.EXTRA_PARAMS_ENCODING_TYPE;


public final class GsmSMSDispatcher extends SMSDispatcher {
    private static final String TAG = "GSM";

    /** Status report received */
    private static final int EVENT_NEW_SMS_STATUS_REPORT = 1000;

    /** New broadcast SMS */
    private static final int EVENT_NEW_BROADCAST_SMS = 1001;

    // MTK-START [ALPS00106465] ICS 4.0.3 SMS-PP Data Download
    /** Result of writing SM to UICC (when SMS-PP service is not available). */
    private static final int EVENT_WRITE_SMS_COMPLETE = 1002;

    /** Handler for SMS-PP data download messages to UICC. */
    private final UsimDataDownloadHandler mDataDownloadHandler;
    // MTK-END   [ALPS00106465] ICS 4.0.3 SMS-PP Data Download

    public GsmSMSDispatcher(PhoneBase phone, SmsStorageMonitor storageMonitor,
            SmsUsageMonitor usageMonitor) {
        super(phone, storageMonitor, usageMonitor);
        // MTK-START [ALPS00106465] ICS 4.0.3 SMS-PP Data Download
        mDataDownloadHandler = new UsimDataDownloadHandler(mCm);
        // MTK-END   [ALPS00106465] ICS 4.0.3 SMS-PP Data Download
        mCm.setOnNewGsmSms(this, EVENT_NEW_SMS, null);
        mCm.setOnSmsStatus(this, EVENT_NEW_SMS_STATUS_REPORT, null);
        mCm.setOnNewGsmBroadcastSms(this, EVENT_NEW_BROADCAST_SMS, null);
    }

    @Override
    public void dispose() {
        mCm.unSetOnNewGsmSms(this);
        mCm.unSetOnSmsStatus(this);
        mCm.unSetOnNewGsmBroadcastSms(this);
    }

    @Override
    protected String getFormat() {
        return android.telephony.SmsMessage.FORMAT_3GPP;
    }

    /**
     * Handles 3GPP format-specific events coming from the phone stack.
     * Other events are handled by {@link SMSDispatcher#handleMessage}.
     *
     * @param msg the message to handle
     */
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case EVENT_NEW_SMS_STATUS_REPORT:
            handleStatusReport((AsyncResult) msg.obj);
            break;

        case EVENT_NEW_BROADCAST_SMS:
            handleBroadcastSms((AsyncResult)msg.obj);
            break;

        case EVENT_WRITE_SMS_COMPLETE:
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                Log.d(TAG, "Successfully wrote SMS-PP message to UICC");
                mCm.acknowledgeLastIncomingGsmSms(true, 0, null);
            } else {
                Log.d(TAG, "Failed to write SMS-PP message to UICC", ar.exception);
                mCm.acknowledgeLastIncomingGsmSms(false,
                        CommandsInterface.GSM_SMS_FAIL_CAUSE_UNSPECIFIED_ERROR, null);
            }
            break;

        default:
            super.handleMessage(msg);
        }
    }

    /**
     * Called when a status report is received.  This should correspond to
     * a previously successful SEND.
     *
     * @param ar AsyncResult passed into the message handler.  ar.result should
     *           be a String representing the status report PDU, as ASCII hex.
     */
    private void handleStatusReport(AsyncResult ar) {
        String pduString = (String) ar.result;
        SmsMessage sms = SmsMessage.newFromCDS(pduString);

        if (sms != null) {
            int tpStatus = sms.getStatus();
            int messageRef = sms.messageRef;
            for (int i = 0, count = deliveryPendingList.size(); i < count; i++) {
                SmsTracker tracker = deliveryPendingList.get(i);
                if (tracker.mMessageRef == messageRef) {
                    // Found it.  Remove from list and broadcast.
                    if(tpStatus >= Sms.STATUS_FAILED || tpStatus < Sms.STATUS_PENDING ) {
                       deliveryPendingList.remove(i);
                    }
                    PendingIntent intent = tracker.mDeliveryIntent;
                    Intent fillIn = new Intent();
                    fillIn.putExtra("pdu", IccUtils.hexStringToBytes(pduString));
                    fillIn.putExtra("format", android.telephony.SmsMessage.FORMAT_3GPP);
                    try {
                        intent.send(mContext, Activity.RESULT_OK, fillIn);
                    } catch (CanceledException ex) {}

                    // Only expect to see one tracker matching this messageref
                    break;
                }
            }
        }
        acknowledgeLastIncomingSms(true, Intents.RESULT_SMS_HANDLED, null);
    }

    /** {@inheritDoc} */
    @Override
    public int dispatchMessage(SmsMessageBase smsb) {

        // If sms is null, means there was a parsing error.
        if (smsb == null) {
            Log.e(TAG, "dispatchMessage: message is null");
            return Intents.RESULT_SMS_GENERIC_ERROR;
        }

        SmsMessage sms = (SmsMessage) smsb;

        if (sms.isTypeZero()) {
            // As per 3GPP TS 23.040 9.2.3.9, Type Zero messages should not be
            // Displayed/Stored/Notified. They should only be acknowledged.
            Log.d(TAG, "Received short message type 0, Don't display or store it. Send Ack");
            return Intents.RESULT_SMS_HANDLED;
        }

        // Send SMS-PP data download messages to UICC. See 3GPP TS 31.111 section 7.1.1.
        if (sms.isUsimDataDownload()) {
            UsimServiceTable ust = mPhone.getUsimServiceTable();
            // If we receive an SMS-PP message before the UsimServiceTable has been loaded,
            // assume that the data download service is not present. This is very unlikely to
            // happen because the IMS connection will not be established until after the ISIM
            // records have been loaded, after the USIM service table has been loaded.
            if (ust != null && ust.isAvailable(
                    UsimServiceTable.UsimService.DATA_DL_VIA_SMS_PP)) {
                Log.d(TAG, "Received SMS-PP data download, sending to UICC.");
                // MTK-START [ALPS00106465] ICS 4.0.3 SMS-PP Data Download
                return mDataDownloadHandler.startDataDownload(sms);
                // MTK-END   [ALPS00106465] ICS 4.0.3 SMS-PP Data Download
            } else {
                Log.d(TAG, "DATA_DL_VIA_SMS_PP service not available, storing message to UICC.");
                String smsc = IccUtils.bytesToHexString(
                        PhoneNumberUtils.networkPortionToCalledPartyBCDWithLength(
                                sms.getServiceCenterAddress()));
                mCm.writeSmsToSim(SmsManager.STATUS_ON_ICC_UNREAD, smsc,
                        IccUtils.bytesToHexString(sms.getPdu()),
                        obtainMessage(EVENT_WRITE_SMS_COMPLETE));
                return Activity.RESULT_OK;  // acknowledge after response from write to USIM
            }
        }

        if (mSmsReceiveDisabled) {
            // Device doesn't support SMS service,
            Log.d(TAG, "Received short message on device which doesn't support "
                    + "SMS service. Ignored.");
            return Intents.RESULT_SMS_HANDLED;
        }

        // Special case the message waiting indicator messages
        boolean handled = false;
        if (sms.isMWISetMessage()) {
            mPhone.setVoiceMessageWaiting(1, -1);  // line 1: unknown number of msgs waiting
            handled = sms.isMwiDontStore();
            if (Config.DEBUG) {
                Log.d(TAG, "Received voice mail indicator set SMS shouldStore=" + !handled);
            }
        } else if (sms.isMWIClearMessage()) {
            mPhone.setVoiceMessageWaiting(1, 0);   // line 1: no msgs waiting
            handled = sms.isMwiDontStore();
            if (Config.DEBUG) {
                Log.d(TAG, "Received voice mail indicator clear SMS shouldStore=" + !handled);
            }
        }

        if (handled) {
            return Intents.RESULT_SMS_HANDLED;
        }

        if (!mStorageMonitor.isStorageAvailable() &&
                sms.getMessageClass() != MessageClass.CLASS_0) {
            // It's a storable message and there's no storage available.  Bail.
            // (See TS 23.038 for a description of class 0 messages.)
            return Intents.RESULT_SMS_OUT_OF_MEMORY;
        }

        return dispatchNormalMessage(smsb);
    }

    /** {@inheritDoc} */
    @Override
    protected void sendData(String destAddr, String scAddr, int destPort,
            byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        // MTK_OPTR_PROTECT_START
        if(isDmLock == true) {
        	  Log.d(TAG, "DM status: lock-on");
        	  return;
        }
        // MTK_OPTR_PROTECT_END

        SmsMessage.SubmitPdu pdu = SmsMessage.getSubmitPdu(
                scAddr, destAddr, destPort, data, (deliveryIntent != null));
        if (pdu != null) {
        sendRawPdu(pdu.encodedScAddress, pdu.encodedMessage, sentIntent, deliveryIntent);
        } else {
            Log.e(TAG, "GsmSMSDispatcher.sendData(): getSubmitPdu() returned null");
    }
    }

    /** {@inheritDoc} */
    @Override
    protected void sendText(String destAddr, String scAddr, String text,
            PendingIntent sentIntent, PendingIntent deliveryIntent) {
        // MTK_OPTR_PROTECT_START
        if(isDmLock == true) {
        	  Log.d(TAG, "DM status: lock-on");
        	  return;
        }
        // MTK_OPTR_PROTECT_END

        SmsMessage.SubmitPdu pdu = SmsMessage.getSubmitPdu(
                scAddr, destAddr, text, (deliveryIntent != null));
        if (pdu != null) {
        sendRawPdu(pdu.encodedScAddress, pdu.encodedMessage, sentIntent, deliveryIntent);
        } else {
            Log.e(TAG, "GsmSMSDispatcher.sendText(): getSubmitPdu() returned null");
        }
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
        SmsMessage.SubmitPdu pdu = SmsMessage.getSubmitPdu(scAddress, destinationAddress,
                message, deliveryIntent != null, SmsHeader.toByteArray(smsHeader),
                encoding, smsHeader.languageTable, smsHeader.languageShiftTable);
        if (pdu != null) {
        sendRawPdu(pdu.encodedScAddress, pdu.encodedMessage, sentIntent, deliveryIntent);
        } else {
            Log.e(TAG, "GsmSMSDispatcher.sendNewSubmitPdu(): getSubmitPdu() returned null");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void sendSms(SmsTracker tracker) {
        HashMap<String, Object> map = tracker.mData;

        byte smsc[] = (byte[]) map.get("smsc");
        byte pdu[] = (byte[]) map.get("pdu");

        // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
        synchronized (mSTrackersQueue) {
            if (mSTrackersQueue.isEmpty() || mSTrackersQueue.get(0) == tracker) {
        // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
                Message reply = obtainMessage(EVENT_SEND_SMS_COMPLETE, tracker);
                mCm.sendSMS(IccUtils.bytesToHexString(smsc),
                        IccUtils.bytesToHexString(pdu), reply);
        // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
            }

            if (mSTrackersQueue.isEmpty() || mSTrackersQueue.get(0) != tracker) {
                Log.d(TAG, "Add tracker into the list: " + tracker);
                mSTrackersQueue.add(tracker);
            }
        }
        // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    }

    /** {@inheritDoc} */
    @Override
    protected void acknowledgeLastIncomingSms(boolean success, int result, Message response) {
        mCm.acknowledgeLastIncomingGsmSms(success, resultToCause(result), response);
    }

    private static int resultToCause(int rc) {
        switch (rc) {
            case Activity.RESULT_OK:
            case Intents.RESULT_SMS_HANDLED:
                // Cause code is ignored on success.
                return 0;
            case Intents.RESULT_SMS_OUT_OF_MEMORY:
                return CommandsInterface.GSM_SMS_FAIL_CAUSE_MEMORY_CAPACITY_EXCEEDED;
            case Intents.RESULT_SMS_GENERIC_ERROR:
            default:
                return CommandsInterface.GSM_SMS_FAIL_CAUSE_UNSPECIFIED_ERROR;
        }
    }

    /**
     * Holds all info about a message page needed to assemble a complete
     * concatenated message
     */
    private static final class SmsCbConcatInfo {
        private final SmsCbHeader mHeader;

        private final String mPlmn;

        private final int mLac;

        private final int mCid;

        public SmsCbConcatInfo(SmsCbHeader header, String plmn, int lac, int cid) {
            mHeader = header;
            mPlmn = plmn;
            mLac = lac;
            mCid = cid;
        }

        @Override
        public int hashCode() {
            return mHeader.messageIdentifier * 31 + mHeader.updateNumber;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SmsCbConcatInfo) {
                SmsCbConcatInfo other = (SmsCbConcatInfo)obj;

                // Two pages match if all header attributes (except the page
                // index) are identical, and both pages belong to the same
                // location (which is also determined by the scope parameter)
                if (mHeader.geographicalScope == other.mHeader.geographicalScope
                        && mHeader.messageCode == other.mHeader.messageCode
                        && mHeader.updateNumber == other.mHeader.updateNumber
                        && mHeader.messageIdentifier == other.mHeader.messageIdentifier
                        && mHeader.dataCodingScheme == other.mHeader.dataCodingScheme
                        && mHeader.nrOfPages == other.mHeader.nrOfPages) {
                    return matchesLocation(other.mPlmn, other.mLac, other.mCid);
                }
            }

            return false;
        }

        /**
         * Checks if this concatenation info matches the given location. The
         * granularity of the match depends on the geographical scope.
         *
         * @param plmn PLMN
         * @param lac Location area code
         * @param cid Cell ID
         * @return true if matching, false otherwise
         */
        public boolean matchesLocation(String plmn, int lac, int cid) {
            switch (mHeader.geographicalScope) {
                case SmsCbMessage.GEOGRAPHICAL_SCOPE_CELL_WIDE:
                case SmsCbMessage.GEOGRAPHICAL_SCOPE_CELL_WIDE_IMMEDIATE:
                    if (mCid != cid) {
                        return false;
                    }
                    // deliberate fall-through
                case SmsCbMessage.GEOGRAPHICAL_SCOPE_LA_WIDE:
                    if (mLac != lac) {
                        return false;
                    }
                    // deliberate fall-through
                case SmsCbMessage.GEOGRAPHICAL_SCOPE_PLMN_WIDE:
                    return mPlmn != null && mPlmn.equals(plmn);
            }

            return false;
        }
    }

    // This map holds incomplete concatenated messages waiting for assembly
    private final HashMap<SmsCbConcatInfo, byte[][]> mSmsCbPageMap =
            new HashMap<SmsCbConcatInfo, byte[][]>();

    /**
     * Handle 3GPP format SMS-CB message.
     * @param ar the AsyncResult containing the received PDUs
     */
    private void handleBroadcastSms(AsyncResult ar) {
        try {
            byte[] receivedPdu = IccUtils.hexStringToBytes((String)ar.result);

            if (false) {
                for (int i = 0; i < receivedPdu.length; i += 8) {
                    StringBuilder sb = new StringBuilder("SMS CB pdu data: ");
                    for (int j = i; j < i + 8 && j < receivedPdu.length; j++) {
                        int b = receivedPdu[j] & 0xff;
                        if (b < 0x10) {
                            sb.append('0');
                        }
                        sb.append(Integer.toHexString(b)).append(' ');
                    }
                    Log.d(TAG, sb.toString());
                }
            }

            SmsCbHeader header = new SmsCbHeader(receivedPdu);
            String plmn = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_NUMERIC);
            GsmCellLocation cellLocation = (GsmCellLocation) mPhone.getCellLocation();
            int lac = cellLocation.getLac();
            int cid = cellLocation.getCid();

            byte[][] pdus;
            if (header.nrOfPages > 1) {
                // Multi-page message
                SmsCbConcatInfo concatInfo = new SmsCbConcatInfo(header, plmn, lac, cid);

                // Try to find other pages of the same message
                pdus = mSmsCbPageMap.get(concatInfo);

                if (pdus == null) {
                    // This it the first page of this message, make room for all
                    // pages and keep until complete
                    pdus = new byte[header.nrOfPages][];

                    mSmsCbPageMap.put(concatInfo, pdus);
                }

                // Page parameter is one-based
                pdus[header.pageIndex - 1] = receivedPdu;

                for (int i = 0; i < pdus.length; i++) {
                    if (pdus[i] == null) {
                        // Still missing pages, exit
                        return;
                    }
                }

                // Message complete, remove and dispatch
                mSmsCbPageMap.remove(concatInfo);
            } else {
                // Single page message
                pdus = new byte[1][];
                pdus[0] = receivedPdu;
            }

            boolean isEmergencyMessage = SmsCbHeader.isEmergencyMessage(header.messageIdentifier);
            dispatchBroadcastPdus(pdus, isEmergencyMessage);

            // Remove messages that are out of scope to prevent the map from
            // growing indefinitely, containing incomplete messages that were
            // never assembled
            Iterator<SmsCbConcatInfo> iter = mSmsCbPageMap.keySet().iterator();

            while (iter.hasNext()) {
                SmsCbConcatInfo info = iter.next();

                if (!info.matchesLocation(plmn, lac, cid)) {
                    iter.remove();
                }
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Error in decoding SMS CB pdu", e);
        }
    }
    
    // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    /** {@inheritDoc} */
    protected void dispatchCbMessage(String smsPdu) {
        
        if (smsPdu == null) {
            return;
        }
        
        GsmCbSmsMessage cbSms = GsmCbSmsMessage.newFromCBM(smsPdu);
        if (cbSms ==null || cbSms.getMessageBody() == null) {
            return;
        }
       
        
        byte[][] pdus = null;
        int totalPage = cbSms.totalPage;
        
        if( totalPage > 1) {
            
            // Lookup all other related parts
            StringBuilder where = new StringBuilder("msgID =");
            where.append(cbSms.messageID);
            where.append(" AND serialNum = ");      
            where.append(cbSms.serialNumber);      
            where.append(" AND count = ");      
            where.append(cbSms.totalPage);
            where.append(" AND sim_id = ");      
            where.append(mSimId);

            Cursor cursor = null;
            try {
                cursor = mResolver.query(mCbRawUri, CB_RAW_PROJECTION, where.toString(), null, null);
                if (cursor == null)
                    return ;
                int cursorCount = cursor.getCount();

                // All the parts are in place, deal with them
                int pduColumn = cursor.getColumnIndex("pdu");
                int sequenceColumn = cursor.getColumnIndex("sequence");

                // judeg if we have already received the same segment
                for (int i = 0; i < cursorCount; i++) {
                    cursor.moveToNext();
                    int cursorSequence = (int)cursor.getLong(sequenceColumn);
                    if (cursorSequence == cbSms.pageIndex) {
                        Log.w(TAG, "Received Duplicate CB segment: " + cursorSequence);
                        return;
                    }
                }
                cursor.moveToFirst();

                if (cursorCount != totalPage - 1) {
                    
                    // We don't have all the parts yet, store this one away
                    ContentValues values = new ContentValues();
                    values.put("msgID", cbSms.messageID);
                    values.put("serialNum", cbSms.serialNumber);
                    values.put("sequence", cbSms.pageIndex);
                    values.put("count", cbSms.totalPage);
                    values.put("pdu", smsPdu);
                    values.put("sim_id", mSimId);
                    
                    mResolver.insert(mCbRawUri, values);
                    return;
                }
                

                pdus = new byte[totalPage][];
                for (int i = 0; i < cursorCount; i++) {
                    
                    int cursorSequence = (int)cursor.getLong(sequenceColumn);
                    pdus[cursorSequence - 1] = HexDump.hexStringToByteArray(
                            cursor.getString(pduColumn));
                    cursor.moveToNext();
                }
                // This one isn't in the DB, so add it
                pdus[cbSms.pageIndex - 1] = cbSms.mPdu;;

                // Remove the parts from the database
                mResolver.delete(mCbRawUri, where.toString(), null);
            } catch (SQLException e) {
                Log.e(TAG, "Can't access multipart CB SMS database", e);
                // TODO:  Would OUT_OF_MEMORY be more appropriate?
                return;
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        else {
            pdus = new byte[1][];
            pdus[0] = cbSms.mPdu;;
        }

        // Dispatch the CB PDUs to applications
        dispatchCbPdus(pdus);        
        
        return;
    }
    
    /** {@inheritDoc} */
    protected void sendData(String destAddr, String scAddr, int destPort, int originalPort,
            byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        Log.d(TAG, "[xj GsmSmsDispatcher.sendData: enter");
        SmsMessage.SubmitPdu pdu = SmsMessage.getSubmitPdu(
                scAddr, destAddr, destPort, originalPort, data, (deliveryIntent != null));
        if (pdu == null) {
            Log.d(TAG, "sendData error: invalid paramters");
            return;
        }
        sendRawPdu(pdu.encodedScAddress, pdu.encodedMessage, sentIntent, deliveryIntent);
        Log.d(TAG, "[xj GsmSmsDispatcher.sendData: exit");
    }

    /** {@inheritDoc} */
    protected void sendMultipartData(
            String destAddr, String scAddr, int destPort,
            ArrayList<SmsRawData> data, ArrayList<PendingIntent> sentIntents, 
            ArrayList<PendingIntent> deliveryIntents) {
        // MTK_OPTR_PROTECT_START
        if(isDmLock == true) {
        	  Log.d(TAG, "DM status: lock-on");
        	  return;
        }
        // MTK_OPTR_PROTECT_END

        int refNumber = getNextConcatenatedRef() & 0x00FF;
        int msgCount = data.size();

        for (int i = 0; i < msgCount; i++) {
            byte[] smsHeader = SmsHeader.getSubmitPduHeader(
                    destPort, refNumber, i+1, msgCount);   // 1-based sequence

            PendingIntent sentIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                sentIntent = sentIntents.get(i);
            }

            PendingIntent deliveryIntent = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                deliveryIntent = deliveryIntents.get(i);
            }

            SmsMessage.SubmitPdu pdus = SmsMessage.getSubmitPdu(scAddr, destAddr,
                    data.get(i).getBytes() , smsHeader, deliveryIntent != null);

            sendRawPdu(pdus.encodedScAddress, pdus.encodedMessage, sentIntent, deliveryIntent);
        }
    }

    /** {@inheritDoc} */
    protected void sendText(String destAddr, String scAddr, String text, 
            int destPort, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        // MTK_OPTR_PROTECT_START
        if(isDmLock == true) {
        	  Log.d(TAG, "DM status: lock-on");
        	  return;
        }
        // MTK_OPTR_PROTECT_END
        
        SmsMessage.SubmitPdu pdu = SmsMessage.getSubmitPdu(
                scAddr, destAddr, text, destPort, (deliveryIntent != null));
        if(pdu != null) {
            sendRawPdu(pdu.encodedScAddress, pdu.encodedMessage, sentIntent, deliveryIntent);
        } else {
            Log.d(TAG, "sendText: pdu is null");
            if(sentIntent != null) {
                try {
                    sentIntent.send(RESULT_ERROR_NULL_PDU);
                } catch (CanceledException ex) {
                    Log.e(TAG, "failed to send back RESULT_ERROR_NULL_PDU");
                }
            }
        }
    }

    /** {@inheritDoc} */
    protected void sendMultipartText(String destinationAddress, String scAddress,
            ArrayList<String> parts, int destPort, ArrayList<PendingIntent> sentIntents,
            ArrayList<PendingIntent> deliveryIntents) {
        // MTK_OPTR_PROTECT_START
        if(isDmLock == true) {
        	  Log.d(TAG, "DM status: lock-on");
        	  return;
        }
        // MTK_OPTR_PROTECT_END
        
        int refNumber = getNextConcatenatedRef() & 0x00FF;
        int msgCount = parts.size();
        int encoding = android.telephony.SmsMessage.ENCODING_UNKNOWN;

        TextEncodingDetails details[] = new TextEncodingDetails[msgCount];
        for (int i = 0; i < msgCount; i++) {
            details[i] = SmsMessage.calculateLength(parts.get(i), false);
            if (encoding != details[i].codeUnitSize
                    && (encoding == android.telephony.SmsMessage.ENCODING_UNKNOWN
                            || encoding == android.telephony.SmsMessage.ENCODING_7BIT)) {
                encoding = details[i].codeUnitSize;
            }
        }

        for (int i = 0; i < msgCount; i++) {
            int singleShiftId = -1;
            int lockingShiftId = -1;
            int language = details[i].shiftLangId;
            int encoding_method = encoding;

            
            if (encoding == android.telephony.SmsMessage.ENCODING_7BIT) {
                Log.d(TAG, "Detail: " + i + " ted"+ details[i]);
                if (details[i].useLockingShift && details[i].useSingleShift ) {
                    singleShiftId = language;
                    lockingShiftId = language;
                    encoding_method = SmsMessage.ENCODING_7BIT_LOCKING_SINGLE;
                } else if (details[i].useLockingShift) {
                    lockingShiftId = language;
                    encoding_method = SmsMessage.ENCODING_7BIT_LOCKING;
                } else if (details[i].useSingleShift) {
                    singleShiftId = language;
                    encoding_method = SmsMessage.ENCODING_7BIT_SINGLE;
                }
            }
            
            byte[] smsHeader = SmsHeader.getSubmitPduHeaderWithLang(
                    destPort, refNumber, i+1, msgCount, singleShiftId, lockingShiftId);   // 1-based sequence

            PendingIntent sentIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                sentIntent = sentIntents.get(i);
            }

            PendingIntent deliveryIntent = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                deliveryIntent = deliveryIntents.get(i);
            }

            SmsMessage.SubmitPdu pdus = SmsMessage.getSubmitPduWithLang(scAddress, destinationAddress,
                    parts.get(i), deliveryIntent != null, smsHeader, encoding_method, language);

            if (pdus != null) {
                sendRawPdu(pdus.encodedScAddress, pdus.encodedMessage, sentIntent, deliveryIntent);
            } else {
                Log.d(TAG, "sendMultipartText: pdu is null");
                if(sentIntent != null) {
                    try {
                        sentIntent.send(RESULT_ERROR_NULL_PDU);
                    } catch (CanceledException ex) {
                        Log.e(TAG, "failed to send back RESULT_ERROR_NULL_PDU");
                    }
                }
            }
        }
    }

    /** {@inheritDoc} */
    protected void activateCellBroadcastSms(int activate, Message response) {
        Message reply = obtainMessage(EVENT_ACTIVATE_CB_COMPLETE, response);
        mCm.setGsmBroadcastActivation(activate==0, reply);
    }

    /** {@inheritDoc} */
    protected void getCellBroadcastSmsConfig(Message response){

        Message reply = obtainMessage(EVENT_GET_CB_CONFIG_COMPLETE, response);
        mCm.getGsmBroadcastConfig(reply);
    }

    /** {@inheritDoc} */
    protected  void setCellBroadcastConfig(int[] configValuesArray, Message response) {
        // Unless CBS is implemented for GSM, this point should be unreachable.
        Log.e(TAG, "Error! The functionality cell broadcast sms is not implemented for GSM.");
        response.recycle();
    }

    /**
     * Configure cell broadcast SMS.
     * @param chIdList
     *            Channel ID list, fill in the fromServiceId, toServiceId, and selected
     *            in the SmsBroadcastConfigInfo only
     * @param langList
     *            Channel ID list, fill in the fromCodeScheme, toCodeScheme, and selected
     *            in the SmsBroadcastConfigInfo only     
     * @param response     
     *            Callback message is empty on completion
     */           
    protected void setCellBroadcastConfig(ArrayList<SmsBroadcastConfigInfo> chIdList, 
            ArrayList<SmsBroadcastConfigInfo> langList, Message response)
    {
        Message reply = obtainMessage(EVENT_SET_CB_CONFIG_COMPLETE, response);

        chIdList.addAll(langList);
        mCm.setGsmBroadcastConfig(
                chIdList.toArray(new SmsBroadcastConfigInfo[1]), reply);
    }

    /**
     * Query if the Cell Broadcast is activated or not
     *
     * @param response
     *            Callback message contains the activated status
     */
    protected void queryCellBroadcastActivation(Message response)
    {
        Message reply = obtainMessage(EVENT_QUERY_CB_ACTIVATION_COMPLETE, response);
        mCm.getGsmBroadcastConfig(reply);
    }

    public int copyTextMessageToIccCard(String scAddress, String address, List<String> text,
            int status, long timestamp) {
        Log.d(TAG, "GsmSMSDispatcher: copy text message to icc card");
        
        if(checkPhoneNumber(scAddress)) {
            Log.d(TAG, "[copyText invalid sc address");
            scAddress = null;
        }
        
        if(checkPhoneNumber(address) == false) {
            Log.d(TAG, "[copyText invalid dest address");
            return RESULT_ERROR_INVALID_ADDRESS;
        }

        mSuccess = true;

        boolean isDeliverPdu = true;

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
            isDeliverPdu = true;
        } else if(status == STATUS_ON_ICC_SENT || status == STATUS_ON_ICC_UNSENT) {
            isDeliverPdu = false;
            Log.d(TAG, "[copyText to encode submit pdu");
        } else {
            Log.d(TAG, "[copyText invalid status, default is deliver pdu");
            // isDeliverPdu = true;
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

        int refNumber = getNextConcatenatedRef() & 0x00FF;
        int encoding = android.telephony.SmsMessage.ENCODING_UNKNOWN;
        TextEncodingDetails details[] = new TextEncodingDetails[msgCount];
        for (int i = 0; i < msgCount; i++) {
            details[i] = SmsMessage.calculateLength(text.get(i), false);
            if (encoding != details[i].codeUnitSize
                  && (encoding == android.telephony.SmsMessage.ENCODING_UNKNOWN
                          || encoding == android.telephony.SmsMessage.ENCODING_7BIT)) {
                encoding = details[i].codeUnitSize;
            }
        }
    
        for(int i = 0; i < msgCount; ++i) {
            if(mSuccess == false) {
                Log.d(TAG, "[copyText Exception happened when copy message");
                return RESULT_ERROR_GENERIC_FAILURE;
            }
            int singleShiftId = -1;
            int lockingShiftId = -1;
            int language = details[i].shiftLangId;
            int encoding_method = encoding;
  
            if (encoding == android.telephony.SmsMessage.ENCODING_7BIT) {
                Log.d(TAG, "Detail: " + i + " ted"+ details[i]);
                if (details[i].useLockingShift && details[i].useSingleShift ) {
                    singleShiftId = language;
                    lockingShiftId = language;
                    encoding_method = SmsMessage.ENCODING_7BIT_LOCKING_SINGLE;
                } else if (details[i].useLockingShift) {
                    lockingShiftId = language;
                    encoding_method = SmsMessage.ENCODING_7BIT_LOCKING;
                } else if (details[i].useSingleShift) {
                    singleShiftId = language;
                    encoding_method = SmsMessage.ENCODING_7BIT_SINGLE;
                }
            }
  
            byte[] smsHeader = null;
            if(msgCount > 1) {
                Log.d(TAG, "[copyText get pdu header for multi-part message");
                smsHeader = SmsHeader.getSubmitPduHeaderWithLang(
                        -1, refNumber, i+1, msgCount, singleShiftId, lockingShiftId);   // 1-based sequence
            }
  
            if(isDeliverPdu) {
                SmsMessage.DeliverPdu pdu = SmsMessage.getDeliverPduWithLang(scAddress, address,
                    text.get(i), smsHeader, timestamp, encoding, language);
    
                if(pdu != null) {
                    Log.d(TAG, "[copyText write deliver pdu into SIM");
                    mCm.writeSmsToSim(status, IccUtils.bytesToHexString(pdu.encodedScAddress),
                        IccUtils.bytesToHexString(pdu.encodedMessage), obtainMessage(EVENT_COPY_TEXT_MESSAGE_DONE));
                }
            } else {
                SmsMessage.SubmitPdu pdu = SmsMessage.getSubmitPduWithLang(scAddress, address,
                          text.get(i), false, smsHeader, encoding_method, language);
      
                if(pdu != null) {
                    Log.d(TAG, "[copyText write submit pdu into SIM");
                    mCm.writeSmsToSim(status, IccUtils.bytesToHexString(pdu.encodedScAddress),
                        IccUtils.bytesToHexString(pdu.encodedMessage), obtainMessage(EVENT_COPY_TEXT_MESSAGE_DONE));
                }
            }
  
            synchronized (mLock) {
                try {
                    Log.d(TAG, "[copyText wait until the message be wrote in SIM");
                    mLock.wait();
                } catch(InterruptedException e) {
                    Log.d(TAG, "[copyText interrupted while trying to copy text message into SIM");
                    return RESULT_ERROR_GENERIC_FAILURE;
                }
            }
            Log.d(TAG, "[copyText thread is waked up");
        }

        if(mSuccess == true) {
            Log.d(TAG, "[copyText all messages have been copied into SIM");
            return RESULT_ERROR_SUCCESS;
        }

        Log.d(TAG, "[copyText copy failed");
        return RESULT_ERROR_GENERIC_FAILURE;
    }

    private boolean isValidSmsAddress(String address) {
        String encodedAddress = PhoneNumberUtils.extractNetworkPortion(address);
        
        return (encodedAddress == null) ||
            (encodedAddress.length() == address.length());
    }

    // MTK-START [ALPS00094284] Filter hyphen of phone number by mtk80589 in 2011.11.23
    private boolean checkPhoneNumber(final char c) {
        return (c >= '0' && c <= '9') || (c == '*') || (c == '+')
            || (c == '#') || (c == 'N') || (c == ' ') || (c == '-');
    }

    private boolean checkPhoneNumber(final String address) {
        if(address == null) {
            return true;
        }

        Log.d(TAG, "checkPhoneNumber: " + address);
        for(int i = 0, n = address.length(); i < n; ++i) {
            if(checkPhoneNumber(address.charAt(i))) {
                continue;
            } else {
                return false;
            }
        }

        return true;
    }
    // MTK-END [ALPS00094284] Filter hyphen of phone number by mtk80589 in 2011.11.23
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
        // MTK_OPTR_PROTECT_START
        if(isDmLock == true) {
        	  Log.d(TAG, "DM status: lock-on");
        	  return;
        }
        // MTK_OPTR_PROTECT_END
        
        int encoding = encodingType;
        TextEncodingDetails details = SmsMessage.calculateLength(text, false);
        if(encoding != details.codeUnitSize
                && (encoding == android.telephony.SmsMessage.ENCODING_UNKNOWN
                    || encoding == android.telephony.SmsMessage.ENCODING_7BIT)) {
                Log.d(TAG, "[enc conflict between details[" + details.codeUnitSize 
                    + "] and encoding " + encoding);
                details.codeUnitSize = encoding;
            }
        
        SmsMessage.SubmitPdu pdu = SmsMessage.getSubmitPdu(
            scAddr, destAddr, text, (deliveryIntent != null),
            null, encoding, details.languageTable, details.languageShiftTable);
            
        if(pdu != null) {
            sendRawPdu(pdu.encodedScAddress, pdu.encodedMessage, sentIntent, deliveryIntent);
        } else {
            Log.d(TAG, "sendText: pdu is null");
            if(sentIntent != null) {
                try {
                    sentIntent.send(RESULT_ERROR_NULL_PDU);
                } catch (CanceledException ex) {
                    Log.e(TAG, "failed to send back RESULT_ERROR_NULL_PDU");
                }
            }
        }
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
        // MTK_OPTR_PROTECT_START
        if(isDmLock == true) {
        	  Log.d(TAG, "DM status: lock-on");
        	  return;
        }
        // MTK_OPTR_PROTECT_END
        
        int refNumber = getNextConcatenatedRef() & 0xff;
        int msgCount = parts.size();
        int encoding = encodingType;
        
        mRemainingMessages = msgCount;
        TextEncodingDetails[] encodingForParts = new TextEncodingDetails[msgCount];
        for(int i = 0; i < msgCount; ++i) {
            TextEncodingDetails details = SmsMessage.calculateLength(parts.get(i), false);
            if(encoding != details.codeUnitSize
                && (encoding == android.telephony.SmsMessage.ENCODING_UNKNOWN
                    || encoding == android.telephony.SmsMessage.ENCODING_7BIT)) {
                Log.d(TAG, "[enc conflict between details[" + details.codeUnitSize 
                    + "] and encoding " + encoding);
                details.codeUnitSize = encoding;
            }
            encodingForParts[i] = details;
        }
        
        for(int i = 0; i < msgCount; ++i) {
            SmsHeader.ConcatRef concatRef = new SmsHeader.ConcatRef();
            concatRef.refNumber = refNumber;
            concatRef.seqNumber = i + 1;  // 1-based sequence
            concatRef.msgCount = msgCount;
            // TODO: We currently set this to true since our messaging app will never
            // send more than 255 parts (it converts the message to MMS well before that).
            // However, we should support 3rd party messaging apps that might need 16-bit
            // references
            // Note:  It's not sufficient to just flip this bit to true; it will have
            // ripple effects (several calculations assume 8-bit ref).
            concatRef.isEightBits = true;
            SmsHeader smsHeader = new SmsHeader();
            smsHeader.concatRef = concatRef;
            if (encoding == android.telephony.SmsMessage.ENCODING_7BIT) {
                smsHeader.languageTable = encodingForParts[i].languageTable;
                smsHeader.languageShiftTable = encodingForParts[i].languageShiftTable;
            }

            PendingIntent sentIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                sentIntent = sentIntents.get(i);
            }

            PendingIntent deliveryIntent = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                deliveryIntent = deliveryIntents.get(i);
            }
            
            SmsMessage.SubmitPdu pdu = SmsMessage.getSubmitPdu(
                scAddr, destAddr, parts.get(i), (deliveryIntent != null),
                SmsHeader.toByteArray(smsHeader), encoding,
                smsHeader.languageTable, smsHeader.languageShiftTable);
            
            if(pdu != null) {
	            sendRawPdu(pdu.encodedScAddress, pdu.encodedMessage, sentIntent, deliveryIntent);
	        } else {
	            Log.d(TAG, "sendText: pdu is null");
	            if(sentIntent != null) {
	                try {
	                    sentIntent.send(RESULT_ERROR_NULL_PDU);
	                } catch (CanceledException ex) {
	                    Log.e(TAG, "failed to send back RESULT_ERROR_NULL_PDU");
	                }
	            }
	        }
        }
    }
    // MTK-END [ALPS00094531] Orange feature SMS Encoding Type Setting by mtk80589 in 2011.11.22
    
    /**
     * Called when a CB activation result is received.  
     *
     * @param ar AsyncResult passed into the message handler. 
     */
    protected void handleQueryCbActivation(AsyncResult ar) {

        Boolean result = null;
            
        if (ar.exception == null) {
            ArrayList<SmsBroadcastConfigInfo> list = 
                    (ArrayList<SmsBroadcastConfigInfo>) ar.result;

            if (list.size()==0) {
                result = new Boolean(false);
            }
            else {
                SmsBroadcastConfigInfo cbConfig= list.get(0);
                Log.d(TAG, "cbConfig: " + cbConfig.toString());

                if (cbConfig.getFromCodeScheme()== -1 && 
                        cbConfig.getToCodeScheme()== -1 && 
                        cbConfig.getFromServiceId()== -1 && 
                        cbConfig.getToServiceId()== -1 &&
                        cbConfig.isSelected()== false) {
                        
                    result = new Boolean(false);
                }
                else {
                    result = new Boolean(true);
                }
            }
        }  

        Log.d(TAG, "queryCbActivation: " + result);
        AsyncResult.forMessage((Message) ar.userObj, result, ar.exception);
        ((Message) ar.userObj).sendToTarget();        
    }
    
    /** {@inheritDoc} */
    public void sendTextWithExtraParams(
            String destAddr,
            String scAddr,
            String text,
            Bundle extraParams,
            PendingIntent sentIntent,
            PendingIntent deliveryIntent) {
        // impl
        int validityPeriod = extraParams.getInt(EXTRA_PARAMS_VALIDITY_PERIOD, -1);
        Log.d(TAG, "sendTextWithExtraParams: validityPeriod is " + validityPeriod);
        
        SmsMessage.SubmitPdu pdu = SmsMessage.getSubmitPdu(scAddr, destAddr, text,
                (deliveryIntent != null), null, ENCODING_UNKNOWN, 0, 0, validityPeriod);
                
        if(pdu != null) {
            sendRawPdu(pdu.encodedScAddress, pdu.encodedMessage, sentIntent, deliveryIntent);
        } else {
            Log.d(TAG, "sendTextWithExtraParams: pdu is null");
            if(sentIntent != null) {
                try {
                    sentIntent.send(RESULT_ERROR_NULL_PDU);
                } catch (CanceledException ex) {
                    Log.e(TAG, "failed to send back RESULT_ERROR_NULL_PDU");
                }
            }
        }
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
        int validityPeriod = extraParams.getInt(EXTRA_PARAMS_VALIDITY_PERIOD, -1);
        Log.d(TAG, "sendTextWithExtraParams: validityPeriod is " + validityPeriod);
       
        int refNumber = getNextConcatenatedRef() & 0x00FF;
        int msgCount = parts.size();
        int encoding = android.telephony.SmsMessage.ENCODING_UNKNOWN;
        
        mRemainingMessages = msgCount;
        
        TextEncodingDetails[] encodingForParts = new TextEncodingDetails[msgCount];
        for (int i = 0; i < msgCount; i++) {
            TextEncodingDetails details = calculateLength(parts.get(i), false);
            if (encoding != details.codeUnitSize
                    && (encoding == android.telephony.SmsMessage.ENCODING_UNKNOWN
                            || encoding == android.telephony.SmsMessage.ENCODING_7BIT)) {
                encoding = details.codeUnitSize;
            }
            encodingForParts[i] = details;
        }
        
        for (int i = 0; i < msgCount; i++) {
            SmsHeader.ConcatRef concatRef = new SmsHeader.ConcatRef();
            concatRef.refNumber = refNumber;
            concatRef.seqNumber = i + 1;  // 1-based sequence
            concatRef.msgCount = msgCount;
            // TODO: We currently set this to true since our messaging app will never
            // send more than 255 parts (it converts the message to MMS well before that).
            // However, we should support 3rd party messaging apps that might need 16-bit
            // references
            // Note:  It's not sufficient to just flip this bit to true; it will have
            // ripple effects (several calculations assume 8-bit ref).
            concatRef.isEightBits = true;
            SmsHeader smsHeader = new SmsHeader();
            smsHeader.concatRef = concatRef;

            // Set the national language tables for 3GPP 7-bit encoding, if enabled.
            if (encoding == android.telephony.SmsMessage.ENCODING_7BIT) {
                smsHeader.languageTable = encodingForParts[i].languageTable;
                smsHeader.languageShiftTable = encodingForParts[i].languageShiftTable;
            }

            PendingIntent sentIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                sentIntent = sentIntents.get(i);
            }

            PendingIntent deliveryIntent = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                deliveryIntent = deliveryIntents.get(i);
            }

            SmsMessage.SubmitPdu pdu = SmsMessage.getSubmitPdu(scAddr, destAddr,
                parts.get(i), (deliveryIntent != null), SmsHeader.toByteArray(smsHeader),
                encoding, smsHeader.languageTable, smsHeader.languageShiftTable,
                validityPeriod);
            if (pdu != null) {
                sendRawPdu(pdu.encodedScAddress, pdu.encodedMessage, sentIntent, deliveryIntent);
            } else {
                Log.d(TAG, "sendMultipartTextWithExtraParams: pdu is null");
                if(sentIntent != null) {
                    try {
                        sentIntent.send(RESULT_ERROR_NULL_PDU);
                    } catch (CanceledException ex) {
                        Log.e(TAG, "failed to send back RESULT_ERROR_NULL_PDU");
                    }
                }
            }
        }
    }
    
    protected android.telephony.SmsMessage createMessageFromSubmitPdu(byte[] smsc, byte[] tpdu) {
    	// smsc + tpdu
    	Log.d(TAG, "[NQ tpdu first byte is " + tpdu[0]);
        int tpduLen = tpdu.length;
        int smscLen = 1;
        if(smsc != null) {
            smscLen = smsc.length;
        } else {
            Log.d(TAG, "[NQ smsc is null");
        }
        byte[] msgPdu = new byte[smscLen + tpduLen];
        int curIndex = 0;
        try {
            if(smsc != null) {
                System.arraycopy(smsc, 0, msgPdu, curIndex, smscLen);
            } else {
                msgPdu[0] = 0;
            }
            curIndex += smscLen;
            System.arraycopy(tpdu, 0, msgPdu, curIndex, tpduLen);
            Log.d(TAG, "[NQ mti byte in msgPdu is " + msgPdu[1]);
        } catch(IndexOutOfBoundsException e) {
            Log.d(TAG, "[NQ out of bounds error when copy pdu data");
        }
        
        return android.telephony.SmsMessage.createFromPdu(msgPdu, getFormat());
    }
}
