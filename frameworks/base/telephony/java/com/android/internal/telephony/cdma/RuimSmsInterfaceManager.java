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

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.internal.telephony.IccConstants;
import com.android.internal.telephony.IccSmsInterfaceManager;
import com.android.internal.telephony.IccUtils;
import com.android.internal.telephony.PhoneProxy;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SmsRawData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.telephony.SmsManager.STATUS_ON_ICC_FREE;
import static android.telephony.SmsManager.STATUS_ON_ICC_READ;
import static android.telephony.SmsManager.STATUS_ON_ICC_UNREAD;
import static android.telephony.SmsManager.STATUS_ON_ICC_SENT;
import static android.telephony.SmsManager.STATUS_ON_ICC_UNSENT;
import com.mediatek.featureoption.FeatureOption;

import android.telephony.SmsMemoryStatus;
import android.telephony.SimSmsInsertStatus;
import static android.telephony.SmsManager.RESULT_ERROR_SUCCESS;
import static android.telephony.SmsManager.RESULT_ERROR_SIM_MEM_FULL;
import static android.telephony.SmsManager.RESULT_ERROR_GENERIC_FAILURE;

/**
 * RuimSmsInterfaceManager to provide an inter-process communication to
 * access Sms in Ruim.
 */
public class RuimSmsInterfaceManager extends IccSmsInterfaceManager {
    static final String LOG_TAG = "CDMA";
    static final boolean DBG = true;

    private final Object mLock = new Object();
    private boolean mSuccess;
    private int mUpdateIndex = -1;
    private List<SmsRawData> mSms;

    private static final int EVENT_LOAD_DONE = 1;
    private static final int EVENT_UPDATE_DONE = 2;
    
    // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    private SmsMemoryStatus mSimMemStatus;
    private static final int EVENT_GET_SMS_SIM_MEM_STATUS_DONE = 3;
    // MTK-END   [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AsyncResult ar;

            switch (msg.what) {
                case EVENT_UPDATE_DONE:
                    ar = (AsyncResult) msg.obj;
                    int index[] = (int []) ar.result;
                    synchronized (mLock) {
                        if(index != null){
                           mUpdateIndex = index[0];
                        }
                        mSuccess = (ar.exception == null);
                        mLock.notifyAll();
                    }
                    break;
                case EVENT_LOAD_DONE:
                    ar = (AsyncResult)msg.obj;
                    synchronized (mLock) {
                        if (ar.exception == null) {
                            mSms = buildValidRawData((ArrayList<byte[]>) ar.result);
                        } else {
                            if(DBG) log("Cannot load Sms records");
                            if (mSms != null)
                                mSms.clear();
                        }
                        mLock.notifyAll();
                    }
                    break;
                
                // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
                case EVENT_GET_SMS_SIM_MEM_STATUS_DONE:
                    ar = (AsyncResult)msg.obj;

                    synchronized (mLock) {
                        if (ar.exception == null) {
                            mSuccess = true;
              
                            if (mSimMemStatus == null) {
                                mSimMemStatus = new SmsMemoryStatus();
                            }
              
                            SmsMemoryStatus tmpStatus = (SmsMemoryStatus)ar.result;
                            mSimMemStatus.mUsed = tmpStatus.mUsed;
                            mSimMemStatus.mTotal = tmpStatus.mTotal;
                        } else {
                            if(DBG)
                                log("Cannot Get Sms SIM Memory Status from RUIM");
                        }
                        mLock.notifyAll();
                    }
                    break;
                    // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
            }
        }
    };

    public RuimSmsInterfaceManager(CDMAPhone phone, SMSDispatcher dispatcher) {
        super(phone);
        mDispatcher = dispatcher;
    }

    public void dispose() {
    }

    protected void finalize() {
        try {
            super.finalize();
        } catch (Throwable throwable) {
            Log.e(LOG_TAG, "Error while finalizing:", throwable);
        }
        if(DBG) Log.d(LOG_TAG, "RuimSmsInterfaceManager finalized");
    }

    /**
     * Update the specified message on the RUIM.
     *
     * @param index record index of message to update
     * @param status new message status (STATUS_ON_ICC_READ,
     *                  STATUS_ON_ICC_UNREAD, STATUS_ON_ICC_SENT,
     *                  STATUS_ON_ICC_UNSENT, STATUS_ON_ICC_FREE)
     * @param pdu the raw PDU to store
     * @return success or not
     *
     */
    public boolean
    updateMessageOnIccEf(int index, int status, byte[] pdu) {
        if (DBG) log("updateMessageOnIccEf: index=" + index +
                " status=" + status + " ==> " +
                "("+ pdu + ")");
        enforceReceiveAndSend("Updating message on RUIM");
        synchronized(mLock) {
            mSuccess = false;
            Message response = mHandler.obtainMessage(EVENT_UPDATE_DONE);

            if (status == STATUS_ON_ICC_FREE) {
                // Special case FREE: call deleteSmsOnRuim instead of
                // manipulating the RUIM record
                mPhone.mCM.deleteSmsOnRuim(index, response);
            } else {
                byte[] record = makeSmsRecordData(status, pdu);
                mPhone.getIccFileHandler().updateEFLinearFixed(
                        IccConstants.EF_SMS, index, record, null, response);
            }
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to update by index");
            }
            if(mSuccess) {
                if(mSms != null) {
                    if(index > 0 && index <= mSms.size()) {
                        if (status == STATUS_ON_ICC_FREE) {
                            mSms.set(index - 1, null);
                        } else {
                            byte[] record = RuimSmsInterfaces.makeCDMASmsRecordData(status, pdu);
                            mSms.set(index - 1, new SmsRawData(record));
                        }
                    }
                }
            }
        }
        return mSuccess;
    }

    /**
     * Copy a raw SMS PDU to the RUIM.
     *
     * @param pdu the raw PDU to store
     * @param status message status (STATUS_ON_ICC_READ, STATUS_ON_ICC_UNREAD,
     *               STATUS_ON_ICC_SENT, STATUS_ON_ICC_UNSENT)
     * @return success or not
     *
     */
    public boolean copyMessageToIccEf(int status, byte[] pdu, byte[] smsc) {
        //NOTE smsc not used in RUIM
        if (DBG) log("copyMessageToIccEf: status=" + status + " ==> " +
                "pdu=("+ Arrays.toString(pdu) + ")");
        if (pdu == null) {
            log("viaCode, pdu == null, return false");
            return false;
        }
        if (status != STATUS_ON_ICC_FREE && status != STATUS_ON_ICC_READ
            && status != STATUS_ON_ICC_UNREAD && status != STATUS_ON_ICC_SENT
            && status != STATUS_ON_ICC_UNSENT) {
            log("viaCode, invalide status: status = " + status + " return false");
            return false;
        }
        log("to do copy action !");
        enforceReceiveAndSend("Copying message to RUIM");
        synchronized(mLock) {
            mSuccess = false;
            Message response = mHandler.obtainMessage(EVENT_UPDATE_DONE);
            if(FeatureOption.EVDO_DT_VIA_SUPPORT == true) {
                // to make sure the pdu from APP can be processed by writeSmsToRuim
                if (status == STATUS_ON_ICC_READ || status == STATUS_ON_ICC_UNREAD) {
                    // it is a deliver pdu ?
                    android.telephony.SmsMessage msg
                        = android.telephony.SmsMessage.createFromPdu(pdu, android.telephony.SmsMessage.FORMAT_3GPP2);
                    if (msg != null) {
                        log("getDisplayOriginatingAddress: " + msg.getDisplayOriginatingAddress());
                        log("getMessageBody: " + msg.getMessageBody());
                        log("getTimestampMillis: " + msg.getTimestampMillis());
                    } else {
                        log("msg == null");
                    }
                    if (msg != null) {
                        SmsMessage.SubmitPdu mpdu = SmsMessage.createEfPdu(msg.getDisplayOriginatingAddress(),
                                            msg.getMessageBody(), msg.getTimestampMillis());
                        if(mpdu != null) {
                            mPhone.mCM.writeSmsToRuim(status, IccUtils.bytesToHexString(mpdu.encodedMessage), response);
                        } else {
                    	    log("mpdu == null");
                        }
                    }

                } else if (status == STATUS_ON_ICC_SENT || status == STATUS_ON_ICC_UNSENT) {
                    // it is a submit pdu, can loop directly
                    mPhone.mCM.writeSmsToRuim(status, IccUtils.bytesToHexString(pdu),
                            response);
                } else if (status == STATUS_ON_ICC_FREE) {
                    log("error sms status for write sms to uim");
                }
            } else {
                mPhone.mCM.writeSmsToRuim(status, IccUtils.bytesToHexString(pdu),
                        response);
            }

            try {
                mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to update by index");
            }

            if(mSuccess) {
                if(mSms != null) {
                    if(mUpdateIndex >= 0 && mUpdateIndex < mSms.size()) {
                        if (status == STATUS_ON_ICC_READ || status == STATUS_ON_ICC_UNREAD) {
                            mSms.set(mUpdateIndex, new SmsRawData(pdu));
                        } else if (status == STATUS_ON_ICC_SENT || status == STATUS_ON_ICC_UNSENT) {
                            byte[] record = RuimSmsInterfaces.makeCDMASmsRecordData(status, pdu);
                            mSms.set(mUpdateIndex, new SmsRawData(record));
                        }
                    }
                }
            }
            mUpdateIndex = -1;
        }
        return mSuccess;
    }

    /**
     * Retrieves all messages currently stored on RUIM.
     */
    public List<SmsRawData> getAllMessagesFromIccEf() {
        if (DBG) log("getAllMessagesFromEF");

        Context context = mPhone.getContext();

        context.enforceCallingPermission(
                "android.permission.RECEIVE_SMS",
                "Reading messages from RUIM");
        if(mSms == null) {
            synchronized(mLock) {
                Message response = mHandler.obtainMessage(EVENT_LOAD_DONE);
                mPhone.getIccFileHandler().loadEFLinearFixedAll(IccConstants.EF_SMS, response);

                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to load from the RUIM");
                }
            }
        }
        return mSms;
    }

    public boolean enableCellBroadcast(int messageIdentifier) {
        // Not implemented
        Log.e(LOG_TAG, "Error! Not implemented for CDMA.");
        return false;
    }

    public boolean disableCellBroadcast(int messageIdentifier) {
        // Not implemented
        Log.e(LOG_TAG, "Error! Not implemented for CDMA.");
        return false;
    }

    public boolean enableCellBroadcastRange(int startMessageId, int endMessageId) {
        // Not implemented
        Log.e(LOG_TAG, "Error! Not implemented for CDMA.");
        return false;
    }

    public boolean disableCellBroadcastRange(int startMessageId, int endMessageId) {
        // Not implemented
        Log.e(LOG_TAG, "Error! Not implemented for CDMA.");
        return false;
    }

    protected void log(String msg) {
        Log.d(LOG_TAG, "[RuimSmsInterfaceManager] " + msg);
    }
    
    // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
      /**
       * Copy a text SMS to the ICC.
       *
       * @param scAddress Service center address
       * @param address   Destination address or original address
       * @param text      List of message text
       * @param status    message status (STATUS_ON_ICC_READ, STATUS_ON_ICC_UNREAD,
       *                  STATUS_ON_ICC_SENT, STATUS_ON_ICC_UNSENT)
       * @param timestamp Timestamp when service center receive the message
       * @return success or not
       *
       */
    public int copyTextMessageToIccCard(String scAddress, String address, List<String> text,
            int status, long timestamp) {
        if(DBG) {
            log("sc address: " + scAddress + " address: " + address + " message count: " + text.size() +
              " status: " + status + " timestamp: " + timestamp);
        }
        enforceReceiveAndSend("Copying message to UIM");

        SmsMemoryStatus memStatus;

        memStatus = getSmsSimMemoryStatusEx();

        if (memStatus == null)
        {
            log("Fail to get UIM memory status");
            return RESULT_ERROR_GENERIC_FAILURE;
        }
        else
        {
            if(memStatus.getUnused() < text.size()) {
                log("UIM memory is not enough");
                return RESULT_ERROR_SIM_MEM_FULL;
            }
        }

        mSuccess = true;

        for(int i = 0; i < text.size(); ++i) {
            if(mSuccess == false) {
                Log.d("RuimSmsInterfaceManager via code", "[copyText Exception happened when copy message");
                return RESULT_ERROR_GENERIC_FAILURE;
            }

            SmsMessage.SubmitPdu pdu = SmsMessage.createEfPdu(address, text.get(i), timestamp);

            if(pdu != null) {
                Log.d("RuimSmsInterfaceManager via code", "[copyText write submit pdu into UIM");
                Message response = mHandler.obtainMessage(EVENT_UPDATE_DONE);
                mPhone.mCM.writeSmsToRuim(status, IccUtils.bytesToHexString(pdu.encodedMessage), response);
            }

            synchronized (mLock) {
                try {
                    mLock.wait();
                } catch(InterruptedException e) {
                    return RESULT_ERROR_GENERIC_FAILURE;
                }
            }

            if (mSuccess && mSms != null) {
                if(mUpdateIndex >= 0 && mUpdateIndex < mSms.size()) {
                    byte[] record = RuimSmsInterfaces.makeCDMASmsRecordData(status, pdu.encodedMessage);
                    mSms.set(mUpdateIndex, new SmsRawData(record));
                }
            }

        }

        return RESULT_ERROR_SUCCESS;
    }

    /**
     * Get SMS SIM Card memory's total and used number
     *
     * @return <code>SmsMemoryStatus</code> object, if false, return NULL.
     */
    public SmsMemoryStatus getSmsSimMemoryStatus() {
        if (DBG)
            log("getSmsSimMemoryStatus");

        enforceReceiveAndSend("Get SMS SIM Card Memory Status from RUIM");

        return getSmsSimMemoryStatusEx();
    }

    private SmsMemoryStatus getSmsSimMemoryStatusEx() {
        if (DBG)
            log("getSmsSimMemoryStatusEx");

        synchronized(mLock) {
            mSuccess = false;

            Message response = mHandler.obtainMessage(EVENT_GET_SMS_SIM_MEM_STATUS_DONE);

            mPhone.mCM.getSmsSimMemoryStatus(response);

            try {
                mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get SMS SIM Card Memory Status from RUIM");
            }
        }

        if (mSuccess) {
            return mSimMemStatus;
        }
        else {
            return null;
        }
    }
    // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    
    public SimSmsInsertStatus insertTextMessageToIccCard(String scAddress, String address,
            List<String> text, int status, long timestamp) {
        //impl
        log("don't support this method for cdma");
        return null;
    }
    
    public SimSmsInsertStatus insertRawMessageToIccCard(int status, byte[] pdu, byte[] smsc) {
        //impl
        log("don't support this method for cdma");
        return null;
    }
}
