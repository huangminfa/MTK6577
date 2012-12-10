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

import android.app.Activity;
import android.app.PendingIntent;
import android.app.AlertDialog;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Telephony;
import android.provider.Telephony.Sms.Intents;
import android.provider.Settings;
import android.telephony.SmsMessage;
import android.telephony.ServiceState;
import android.util.Log;
import android.view.WindowManager;

import com.android.internal.telephony.SmsMessageBase.TextEncodingDetails;
import com.android.internal.util.HexDump;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import com.android.internal.R;

import static android.telephony.SmsManager.RESULT_ERROR_GENERIC_FAILURE;
import static android.telephony.SmsManager.RESULT_ERROR_NO_SERVICE;
import static android.telephony.SmsManager.RESULT_ERROR_NULL_PDU;
import static android.telephony.SmsManager.RESULT_ERROR_RADIO_OFF;
import static android.telephony.SmsManager.RESULT_ERROR_LIMIT_EXCEEDED;
import static android.telephony.SmsManager.RESULT_ERROR_FDN_CHECK_FAILURE;

// MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
import android.app.NotificationManager;
import android.app.Notification;

import android.os.Environment;
import android.os.Bundle;
import android.os.StatFs;

import android.util.Config;

import java.util.List;

import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsResponse;
import com.android.internal.telephony.WapPushOverSms;
import com.android.internal.telephony.WspTypeDecoder;
import com.android.internal.telephony.ITelephony;

import com.mediatek.featureoption.FeatureOption;

// MTK_OPTR_PROTECT_START
import com.android.internal.telephony.DMOperatorFile;
import com.mediatek.dmagent.DMAgent;
// MTK_OPTR_PROTECT_END

// DM-Agent
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.os.SystemProperties;

// for Netqin lib
import com.netqin.NqSmsFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ApplicationInfo;
// MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16

import android.content.ComponentName;

public abstract class SMSDispatcher extends Handler {
    static final String TAG = "SMS";    // accessed from inner class
    private static final String SEND_NEXT_MSG_EXTRA = "SendNextMsg";

    /** Default timeout for SMS sent query */
    private static final int DEFAULT_SMS_TIMEOUT = 6000;

    /** Permission required to receive SMS and SMS-CB messages. */
    public static final String RECEIVE_SMS_PERMISSION = "android.permission.RECEIVE_SMS";

    /** Permission required to receive ETWS and CMAS emergency broadcasts. */
    public static final String RECEIVE_EMERGENCY_BROADCAST_PERMISSION =
            "android.permission.RECEIVE_EMERGENCY_BROADCAST";

    /** Query projection for checking for duplicate message segments. */
    private static final String[] PDU_PROJECTION = new String[] {
            "pdu"
    };

    /** Query projection for combining concatenated message segments. */
    private static final String[] PDU_SEQUENCE_PORT_PROJECTION = new String[] {
            "pdu",
            "sequence",
            "destination_port"
    };
    
    // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    protected static final String[] RAW_PROJECTION = new String[] {
        "pdu",
        "sequence",
        "destination_port",
    };
    
    protected static final String[] CB_RAW_PROJECTION = new String[] {
        "pdu",
        "sequence",
    };
    // MTK-END   [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16

    private static final int PDU_COLUMN = 0;
    private static final int SEQUENCE_COLUMN = 1;
    private static final int DESTINATION_PORT_COLUMN = 2;

    /** New SMS received. */
    protected static final int EVENT_NEW_SMS = 1;

    /** SMS send complete. */
    protected static final int EVENT_SEND_SMS_COMPLETE = 2;

    /** Retry sending a previously failed SMS message */
    private static final int EVENT_SEND_RETRY = 3;

    /** SMS confirm required */
    private static final int EVENT_POST_ALERT = 4;

    /** Send the user confirmed SMS */
    static final int EVENT_SEND_CONFIRMED_SMS = 5;  // accessed from inner class

    /** Alert is timeout */
    private static final int EVENT_ALERT_TIMEOUT = 6;

    /** Stop the sending */
    static final int EVENT_STOP_SENDING = 7;        // accessed from inner class
    
    // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    /** Status report received */
    // static final protected int EVENT_NEW_SMS_STATUS_REPORT = 8;

    /** SIM/RUIM storage is full */
    static final public int EVENT_ICC_FULL = 9;

    /** Memory status reporting is acknowledged by RIL */
    static final protected int EVENT_REPORT_MEMORY_STATUS_DONE = 11;

    /** Radio is ON */
    static final protected int EVENT_RADIO_ON = 12;
    
    /** New broadcast SMS */
    static final protected int EVENT_NEW_BROADCAST_SMS = 13;

    /** Activate/Inactivate Cell Broadcast complete */
    static final protected int EVENT_ACTIVATE_CB_COMPLETE = 100;

    /** Get Cell Broadcast Configuration complete */
    static final protected int EVENT_GET_CB_CONFIG_COMPLETE = 101;

    /** Set Cell Broadcast Configuration complete */
    static final protected int EVENT_SET_CB_CONFIG_COMPLETE = 102;

    /** Receive new CB message */
    static final protected int EVENT_NEW_CB_SMS = 103;

    /** Get Cell Broadcast Configuration complete */
    static final protected int EVENT_QUERY_CB_ACTIVATION_COMPLETE = 104;

    /** ME storage is full */
    static final protected int EVENT_ME_FULL = 105;

    /** SMS subsystem in the modem is ready */
    static final protected int EVENT_SMS_READY = 106;

    /** reducted message handling */
    static final protected int EVENT_HANDLE_REDUCTED_MESSAGE = 107;
    static final protected int EVENT_REDUCTED_MESSAGE_TIMEOUT = 108;

    /** copy text message to the ICC card */
    static final protected int EVENT_COPY_TEXT_MESSAGE_DONE = 109;
    // MTK-END   [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    
    protected static final int EVENT_DELAY_WAP_PUSH_SETTING_NOTI = 110;

    protected final Phone mPhone;
    protected final Context mContext;
    protected final ContentResolver mResolver;
    protected final CommandsInterface mCm;
    protected final SmsStorageMonitor mStorageMonitor;

    protected final WapPushOverSms mWapPush;

    protected static final Uri mRawUri = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, "raw");
    
    // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    protected static final Uri mCbRawUri = Uri.withAppendedPath(Uri.parse("content://cb"), "cbraw");
    // MTK-END   [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16

    /** Maximum number of times to retry sending a failed SMS. */
    private static final int MAX_SEND_RETRIES = 3;
    /** Delay before next send attempt on a failed SMS, in milliseconds. */
    private static final int SEND_RETRY_DELAY = 2000;
    /** single part SMS */
    private static final int SINGLE_PART_SMS = 1;
    /** Message sending queue limit */
    private static final int MO_MSG_QUEUE_LIMIT = 5;

    /**
     * Message reference for a CONCATENATED_8_BIT_REFERENCE or
     * CONCATENATED_16_BIT_REFERENCE message set.  Should be
     * incremented for each set of concatenated messages.
     * Static field shared by all dispatcher objects.
     */
    private static int sConcatenatedRef = new Random().nextInt(256);

    /** Outgoing message counter. Shared by all dispatchers. */
    private final SmsUsageMonitor mUsageMonitor;

    private final ArrayList<SmsTracker> mSTrackers = new ArrayList<SmsTracker>(MO_MSG_QUEUE_LIMIT);
    
    /**
     * This list is used to maintain the unsent Sms Tracker
     * we have this queue list to avoid we send a lot of SEND_SMS request to RIL
     * and block other commands.
     * So we only send the next SEND_SMS request after the previously request has been completed
     */
    protected ArrayList<SmsTracker> mSTrackersQueue = new ArrayList<SmsTracker>(MO_MSG_QUEUE_LIMIT);

    /** Wake lock to ensure device stays awake while dispatching the SMS intent. */
    private PowerManager.WakeLock mWakeLock;

    /**
     * Hold the wake lock for 5 seconds, which should be enough time for
     * any receiver(s) to grab its own wake lock.
     */
    private static final int WAKE_LOCK_TIMEOUT = 5000;

    /* Flags indicating whether the current device allows sms service */
    protected boolean mSmsCapable = true;
    protected boolean mSmsReceiveDisabled;
    protected boolean mSmsSendDisabled;

    protected int mRemainingMessages = -1;

    protected static int getNextConcatenatedRef() {
        sConcatenatedRef += 1;
        return sConcatenatedRef;
    }
    
    // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    // MTK_OPTR_PROTECT_START
    static final protected String DM_OA = "10654040";
    static final protected int DM_PORT = 16998;
    
    protected DMOperatorFile mDMOperatorFile = null;
    
    protected static boolean isDmLock = false;
    // MTK_OPTR_PROTECT_END

    /** Default checking period for SMS sent without user permit */
    private static final int DEFAULT_SMS_CHECK_PERIOD = 3600000;

    /** Default number of SMS sent in checking period without user permit */
    private static final int DEFAULT_SMS_MAX_COUNT = 100;
    
    // flag of storage status
    protected boolean mStorageAvailable = true;
    protected boolean mReportMemoryStatusPending = false;

    protected int mSimId = Phone.GEMINI_SIM_1;

    protected boolean mSmsReady = false;

    // for copying text message to ICC card
    protected int messageCountNeedCopy = 0;
    protected Object mLock = new Object();
    protected boolean mSuccess = true;

    // for Netqin SMS checking
    private static SmsHeader.ConcatRef sConcatRef = null;
    private static boolean sRefuseSent = true;
    private static int sConcatMsgCount = 0;

    // for auto push service
    private static final int WAP_PUSH_NOTI_ID = 4999;
    private static final String ACTION_WAP_PUSH_NOTI_CANCEL = "com.mediatek.cu_wap_push_permission_cancel";
    private static final int DELAY_NOTI_TIME = 15 * 1000;

    protected static String PDU_SIZE = "pdu_size";
    // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    
    IConcatenatedSmsFwkExt mConcatenatedSmsFwkExt = null;

    /**
     * Create a new SMS dispatcher.
     * @param phone the Phone to use
     * @param storageMonitor the SmsStorageMonitor to use
     * @param usageMonitor the SmsUsageMonitor to use
     */
    protected SMSDispatcher(PhoneBase phone, SmsStorageMonitor storageMonitor,
            SmsUsageMonitor usageMonitor) {
        mPhone = phone;
        mWapPush = new WapPushOverSms(phone, this);
        mContext = phone.getContext();
        mResolver = mContext.getContentResolver();
        mCm = phone.mCM;
        mStorageMonitor = storageMonitor;
        mUsageMonitor = usageMonitor;

        createWakelock();

        mSmsCapable = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_sms_capable);
        mSmsReceiveDisabled = !SystemProperties.getBoolean(
                                TelephonyProperties.PROPERTY_SMS_RECEIVE, mSmsCapable);
        mSmsSendDisabled = !SystemProperties.getBoolean(
                                TelephonyProperties.PROPERTY_SMS_SEND, mSmsCapable);
        Log.d(TAG, "SMSDispatcher: ctor mSmsCapable=" + mSmsCapable + " format=" + getFormat()
                + " mSmsReceiveDisabled=" + mSmsReceiveDisabled
                + " mSmsSendDisabled=" + mSmsSendDisabled);
        
        // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
        mCm.setOnNewSMS(this, EVENT_NEW_SMS, null);
        mCm.setOnIccSmsFull(this, EVENT_ICC_FULL, null);
        mCm.setOnMeSmsFull(this, EVENT_ME_FULL, null);
        mCm.registerForOn(this, EVENT_RADIO_ON, null);
        mCm.registerForSmsReady(this, EVENT_SMS_READY, null);

        mSimId = mPhone.getMySimId();

        // Register for device storage intents.  Use these to notify the RIL
        // that storage for SMS is or is not available.
        // TODO: Revisit this for a later release.  Storage reporting should
        // rely more on application indication.
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DEVICE_STORAGE_FULL);
        filter.addAction(Intent.ACTION_DEVICE_STORAGE_NOT_FULL);
        filter.addAction(ACTION_WAP_PUSH_NOTI_CANCEL);
        filter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
        mContext.registerReceiver(mResultReceiver, filter);
        
        // MTK_OPTR_PROTECT_START
        // initialize DM operator info
        Log.d(TAG, "[DM initialize DM xml");
        mDMOperatorFile = DMOperatorFile.getInstance();
        mDMOperatorFile.initFromRes(mContext);
        mDMOperatorFile.dump();
        
        // register DM broadcast receiver
        IntentFilter dmFilter = new IntentFilter();
        dmFilter.addAction("com.mediatek.dm.LAWMO_LOCK");
        dmFilter.addAction("com.mediatek.dm.LAWMO_UNLOCK");
        mContext.registerReceiver(mDMLockReceiver, dmFilter);
        
        try {
            IBinder binder = ServiceManager.getService("DMAgent");
            DMAgent dmAgent = DMAgent.Stub.asInterface (binder);
            if(dmAgent != null) {
                  isDmLock = dmAgent.isLockFlagSet();
              }
              Log.d(TAG, "DM is lock: " + isDmLock);
        } catch (RemoteException ex) {
              Log.d(TAG, "Fail to obtain DMAgent");
              ex.printStackTrace();
        }
        // MTK_OPTR_PROTECT_END
        // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
        mConcatenatedSmsFwkExt = new ConcatenatedSmsFwkExt(mContext, mSimId);
    }
    
    // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    /** Unregister for incoming SMS events. */
    public void dispose() {
        mCm.unSetOnNewSMS(this);
        mCm.unSetOnSmsStatus(this);
        mCm.unSetOnIccSmsFull(this);
        mCm.unregisterForOn(this);
        mCm.unregisterForSmsReady(this);
    }
    // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    
    /**
     * The format of the message PDU in the associated broadcast intent.
     * This will be either "3gpp" for GSM/UMTS/LTE messages in 3GPP format
     * or "3gpp2" for CDMA/LTE messages in 3GPP2 format.
     *
     * Note: All applications which handle incoming SMS messages by processing the
     * SMS_RECEIVED_ACTION broadcast intent MUST pass the "format" extra from the intent
     * into the new methods in {@link android.telephony.SmsMessage} which take an
     * extra format parameter. This is required in order to correctly decode the PDU on
     * devices which require support for both 3GPP and 3GPP2 formats at the same time,
     * such as CDMA/LTE devices and GSM/CDMA world phones.
     *
     * @return the format of the message PDU
     */
    protected abstract String getFormat();

    @Override
    protected void finalize() throws Throwable {
        // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
        super.finalize();
        // MTK-END   [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
        Log.d(TAG, "SMSDispatcher finalized");
    }


    /* TODO: Need to figure out how to keep track of status report routing in a
     *       persistent manner. If the phone process restarts (reboot or crash),
     *       we will lose this list and any status reports that come in after
     *       will be dropped.
     */
    /** Sent messages awaiting a delivery status report. */
    protected final ArrayList<SmsTracker> deliveryPendingList = new ArrayList<SmsTracker>();

    /**
     * Handles events coming from the phone stack. Overridden from handler.
     *
     * @param msg the message to handle
     */
    @Override
    public void handleMessage(Message msg) {
        AsyncResult ar;

        switch (msg.what) {
        case EVENT_NEW_SMS:
            // A new SMS has been received by the device
            if (false) {
                Log.d(TAG, "New SMS Message Received");
            }

            SmsMessage sms;

            ar = (AsyncResult) msg.obj;

            if (ar.exception != null) {
                Log.e(TAG, "Exception processing incoming SMS. Exception:" + ar.exception);
                return;
            }

            sms = (SmsMessage) ar.result;
            try {
                int result = dispatchMessage(sms.mWrappedSmsMessage);
                if (result != Activity.RESULT_OK) {
                    // RESULT_OK means that message was broadcast for app(s) to handle.
                    // Any other result, we should ack here.
                    boolean handled = (result == Intents.RESULT_SMS_HANDLED);
                    notifyAndAcknowledgeLastIncomingSms(handled, result, null);
                }
            } catch (RuntimeException ex) {
                Log.e(TAG, "Exception dispatching message", ex);
                notifyAndAcknowledgeLastIncomingSms(false, Intents.RESULT_SMS_GENERIC_ERROR, null);
            }

            break;

        case EVENT_SEND_SMS_COMPLETE:
            // An outbound SMS has been successfully transferred, or failed.
            handleSendComplete((AsyncResult) msg.obj);
            break;

        case EVENT_SEND_RETRY:
            sendSms((SmsTracker) msg.obj);
            break;

        case EVENT_POST_ALERT:
            handleReachSentLimit((SmsTracker)(msg.obj));
            break;

        case EVENT_ALERT_TIMEOUT:
            ((AlertDialog)(msg.obj)).dismiss();
            msg.obj = null;
            if (mSTrackers.isEmpty() == false) {
                try {
                    SmsTracker sTracker = mSTrackers.remove(0);
                    sTracker.mSentIntent.send(RESULT_ERROR_LIMIT_EXCEEDED);
                } catch (CanceledException ex) {
                    Log.e(TAG, "failed to send back RESULT_ERROR_LIMIT_EXCEEDED");
                }
            }
            if (false) {
                Log.d(TAG, "EVENT_ALERT_TIMEOUT, message stop sending");
            }
            break;

        case EVENT_SEND_CONFIRMED_SMS:
            if (mSTrackers.isEmpty() == false) {
                SmsTracker sTracker = mSTrackers.remove(mSTrackers.size() - 1);
                if (sTracker.isMultipart()) {
                    sendMultipartSms(sTracker);
                } else {
                    sendSms(sTracker);
                }
                removeMessages(EVENT_ALERT_TIMEOUT, msg.obj);
                removeMessages(EVENT_REDUCTED_MESSAGE_TIMEOUT, msg.obj);
            }
            break;

        case EVENT_STOP_SENDING:
            if (mSTrackers.isEmpty() == false) {
                // Remove the latest one.
                try {
                    SmsTracker sTracker = mSTrackers.remove(mSTrackers.size() - 1);
                    sTracker.mSentIntent.send(RESULT_ERROR_LIMIT_EXCEEDED);
                } catch (CanceledException ex) {
                    Log.e(TAG, "failed to send back RESULT_ERROR_LIMIT_EXCEEDED");
                }
                removeMessages(EVENT_ALERT_TIMEOUT, msg.obj);
            }
            break;
        // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
        case EVENT_ICC_FULL:
            handleIccFull();
            break;

        case EVENT_ACTIVATE_CB_COMPLETE:
        case EVENT_GET_CB_CONFIG_COMPLETE:
        case EVENT_SET_CB_CONFIG_COMPLETE:
            ar = (AsyncResult) msg.obj;
            AsyncResult.forMessage((Message) ar.userObj, ar.result, ar.exception);
            ((Message) ar.userObj).sendToTarget();            
            break;
            
        case EVENT_QUERY_CB_ACTIVATION_COMPLETE:
            handleQueryCbActivation((AsyncResult) msg.obj);
            break;
            
        case EVENT_NEW_CB_SMS:
            // A new SMS has been received by the device
            if (Config.LOGD) {
                Log.d(TAG, "New Cell Broadcast Message Received");
            }

            ar = (AsyncResult) msg.obj;

            if (ar.exception != null) {
                Log.e(TAG, "Exception processing incoming CBSMS. Exception:" + ar.exception);
                return;
            }
                
            // dispatchCbMessage((String)ar.result);
            try {
                dispatchCbMessage((String)ar.result);
            } catch(RuntimeException e) {
                Log.e(TAG, "Invalid cb pdu string");
                return;
            }
            
            break;

        case EVENT_REPORT_MEMORY_STATUS_DONE:
            ar = (AsyncResult)msg.obj;
            if (ar.exception != null) {
                mReportMemoryStatusPending = true;
                Log.v(TAG, "Memory status report to modem pending : mStorageAvailable = "
                        + mStorageAvailable);
            } else {
                mReportMemoryStatusPending = false;
            }
            break;

        case EVENT_RADIO_ON:
            if (mReportMemoryStatusPending) {
                Log.v(TAG, "Sending pending memory status report : mStorageAvailable = "
                        + mStorageAvailable);
                mCm.reportSmsMemoryStatus(mStorageAvailable,
                        obtainMessage(EVENT_REPORT_MEMORY_STATUS_DONE));
            }
            break;
            
        case EVENT_ME_FULL:
            /* ME FULL, and there is a new SMS */
            notifyLastIncomingSms(Intents.RESULT_SMS_OUT_OF_MEMORY);
            break;

        case EVENT_SMS_READY:
            Log.d(TAG, "SMS is ready, SIM: " +mSimId);
            mSmsReady = true;

            notifySmsReady(mSmsReady);
            break;
            
        case EVENT_COPY_TEXT_MESSAGE_DONE:
            ar = (AsyncResult)msg.obj;
            synchronized (mLock) {
                mSuccess = (ar.exception == null);

                if(mSuccess == true) {
                    Log.d(TAG, "[copyText success to copy one");
                    messageCountNeedCopy -= 1;
                } else {
                    Log.d(TAG, "[copyText fail to copy one");
                    messageCountNeedCopy = 0;
                }
                
                mLock.notifyAll();
            }
            break;
            
        case EVENT_HANDLE_REDUCTED_MESSAGE:
            handleDeductedMessage((SmsTracker)(msg.obj));
            break;
            
        case EVENT_REDUCTED_MESSAGE_TIMEOUT:
            ((AlertDialog)(msg.obj)).dismiss();
            msg.obj = null;
            
            if (mSTrackers.isEmpty() == false) {
                try {
                    SmsTracker sTracker = mSTrackers.remove(mSTrackers.size() - 1);
                    if(sTracker.mSentIntent != null) {
                        sTracker.mSentIntent.send(RESULT_ERROR_LIMIT_EXCEEDED);
                    }
                } catch (CanceledException ex) {
                        Log.e(TAG, "failed to send back RESULT_ERROR_LIMIT_EXCEEDED");
                }
            }
            
            while(sConcatMsgCount > 0 && mSTrackers.size() > 0) {
                mSTrackers.remove(mSTrackers.size() - 1);
                sConcatMsgCount -= 1;
            }
            break;
        // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
        case EVENT_DELAY_WAP_PUSH_SETTING_NOTI:
            notifyForUnsetSim();
            break;
            
            case IConcatenatedSmsFwkExt.EVENT_DISPATCH_CONCATE_SMS_SEGMENTS:
                Log.d(TAG, "ConcatenatedSmsFwkExt: receive timeout message");
                if (msg.obj == null) {
                    Log.d(TAG, "ConcatenatedSmsFwkExt: null TimerRecord in msg");
                    return;
                }

                TimerRecord record = (TimerRecord) msg.obj;
                if (record == null) {
                    Log.d(TAG, "ConcatenatedSmsFwkExt: null TimerRecord in msg 2");
                    return;
                }
                Log.d(TAG,
                        "ConcatenatedSmsFwkExt: timer is expired, dispatch existed segments. refNumber = "
                                + record.refNumber);
                byte[][] pdus = mConcatenatedSmsFwkExt.queryExistedSegments(record);
                if (pdus != null && pdus.length > 0) {
                    dispatchPdus(pdus);
                } else {
                    Log.d(TAG, "ConcatenatedSmsFwkExt: no pdus to be dispatched");
                }
                Log.d(TAG, "ConcatenatedSmsFwkExt: delete segment(s), ref = "
                        + record.refNumber);
                mConcatenatedSmsFwkExt.deleteExistedSegments(record);
                break;
        }
    }

    private void createWakelock() {
        PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SMSDispatcher");
        mWakeLock.setReferenceCounted(true);
    }

    /**
     * Grabs a wake lock and sends intent as an ordered broadcast.
     * The resultReceiver will check for errors and ACK/NACK back
     * to the RIL.
     *
     * @param intent intent to broadcast
     * @param permission Receivers are required to have this permission
     */
    protected void dispatch(Intent intent, String permission) {
        // Hold a wake lock for WAKE_LOCK_TIMEOUT seconds, enough to give any
        // receivers time to take their own wake locks.
        mWakeLock.acquire(WAKE_LOCK_TIMEOUT);
        // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
        intent.putExtra("rTime", System.currentTimeMillis());
        intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);
        // MTK-END   [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
        mContext.sendOrderedBroadcast(intent, permission, mResultReceiver,
                this, Activity.RESULT_OK, null, null);
    }

    /**
     * Called when SMS send completes. Broadcasts a sentIntent on success.
     * On failure, either sets up retries or broadcasts a sentIntent with
     * the failure in the result code.
     *
     * @param ar AsyncResult passed into the message handler.  ar.result should
     *           an SmsResponse instance if send was successful.  ar.userObj
     *           should be an SmsTracker instance.
     */
    protected void handleSendComplete(AsyncResult ar) {
        SmsTracker tracker = (SmsTracker) ar.userObj;
        PendingIntent sentIntent = tracker.mSentIntent;

        // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
        int szPdu = 0;
        if(tracker != null) {
            HashMap map = tracker.mData;
            if(map != null) {
                int smscLength = (map.get("smsc") == null) ? 0 : (((byte[])map.get("smsc")).length);
                int pduLength = (map.get("pdu") == null) ? 0 : (((byte[])map.get("pdu")).length);
                szPdu = smscLength + pduLength;
            }
        }
        synchronized (mSTrackersQueue) {
            // remove the first tracker and send the next one if any
            Log.d(TAG, "Remove Tracker");
            SmsTracker tempTracker = (!mSTrackersQueue.isEmpty()) ? mSTrackersQueue.remove(0) : null;
            if(tempTracker != null && tempTracker.equals(tracker)) {
                Log.d(TAG, "[pdu size: " + szPdu);
            }
            
            if (!mSTrackersQueue.isEmpty()) {
                SmsTracker sendtracker = mSTrackersQueue.get(0);

                sendSms(sendtracker);
            }
        }
        // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16

        if (ar.exception == null) {
            if (Config.DEBUG) {
                Log.d(TAG, "SMS send complete. Broadcasting "
                        + "intent: " + sentIntent);
            }

            if (tracker.mDeliveryIntent != null) {
                // Expecting a status report.  Add it to the list.
                int messageRef = ((SmsResponse)ar.result).messageRef;
                tracker.mMessageRef = messageRef;
                deliveryPendingList.add(tracker);
            }

            if (sentIntent != null) {
                try {
                    if (mRemainingMessages > -1) {
                        mRemainingMessages--;
                    }

                    if (mRemainingMessages == 0) {
                        Intent sendNext = new Intent();
                        sendNext.putExtra(SEND_NEXT_MSG_EXTRA, true);
                        sendNext.putExtra(PDU_SIZE, szPdu);
                        sentIntent.send(mContext, Activity.RESULT_OK, sendNext);
                    } else {
                        // sentIntent.send(Activity.RESULT_OK);
                        Intent fillIn = new Intent();
                        fillIn.putExtra(PDU_SIZE, szPdu);
                        sentIntent.send(mContext, Activity.RESULT_OK, fillIn);
                    }
                } catch (CanceledException ex) {}
            }
        } else {
            Log.d(TAG, "SMS send failed");
            
            // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
            // for ALPS00044719
            boolean isTestIccCard = false;
            try{
                ITelephony telephony = ITelephony.Stub.asInterface(
                                ServiceManager.getService(Context.TELEPHONY_SERVICE));
                if (telephony != null) {
                    isTestIccCard = telephony.isTestIccCard();
                } 
            } catch(RemoteException ex) {
                // This shouldn't happen in the normal case
                Log.d(TAG, "SD-handleSendComplete: RemoteException: " + ex.getMessage());
            } catch (NullPointerException ex) {
                // This could happen before phone restarts due to crashing
                Log.d(TAG, "SD-handleSendComplete: NullPointerException: " + ex.getMessage());
            }
                
            Log.d(TAG, "SD-handleSendComplete: SIM" + mSimId + " isTestIccCard " + isTestIccCard);
            // for ALPS00044719
            // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16

            int ss = mPhone.getServiceState().getState();

            if (ss != ServiceState.STATE_IN_SERVICE) {
                Log.d(TAG, "handleSendComplete: No service");
                handleNotInService(ss, tracker);
            } else if (tracker.mSentIntent != null) {
                int error = RESULT_ERROR_GENERIC_FAILURE;

                if (((CommandException)(ar.exception)).getCommandError()
                        == CommandException.Error.FDN_CHECK_FAILURE) {
                    error = RESULT_ERROR_FDN_CHECK_FAILURE;
                }
                // Done retrying; return an error to the app.
                try {
                    Intent fillIn = new Intent();
                    // add pdu size 
                    fillIn.putExtra(PDU_SIZE, szPdu);
                    if (ar.result != null) {
                        fillIn.putExtra("errorCode", ((SmsResponse)ar.result).errorCode);
                    }
                    if (mRemainingMessages > -1) {
                        mRemainingMessages--;
                    }

                    if (mRemainingMessages == 0) {
                        fillIn.putExtra(SEND_NEXT_MSG_EXTRA, true);
                    }

                    tracker.mSentIntent.send(mContext, error, fillIn);
                } catch (CanceledException ex) {}
            }
        }
    }

    /**
     * Handles outbound message when the phone is not in service.
     *
     * @param ss     Current service state.  Valid values are:
     *                  OUT_OF_SERVICE
     *                  EMERGENCY_ONLY
     *                  POWER_OFF
     * @param tracker   An SmsTracker for the current message.
     */
    protected static void handleNotInService(int ss, SmsTracker tracker) {
        if (tracker.mSentIntent != null) {
            try {
                if (ss == ServiceState.STATE_POWER_OFF) {
                    tracker.mSentIntent.send(RESULT_ERROR_RADIO_OFF);
                } else {
                    tracker.mSentIntent.send(RESULT_ERROR_NO_SERVICE);
                }
            } catch (CanceledException ex) {}
        }
    }

    /**
     * Dispatches an incoming SMS messages.
     *
     * @param sms the incoming message from the phone
     * @return a result code from {@link Telephony.Sms.Intents}, or
     *         {@link Activity#RESULT_OK} if the message has been broadcast
     *         to applications
     */
    public abstract int dispatchMessage(SmsMessageBase sms);

    /**
     * Dispatch a normal incoming SMS. This is called from the format-specific
     * {@link #dispatchMessage(SmsMessageBase)} if no format-specific handling is required.
     *
     * @param sms
     * @return
     */
    protected int dispatchNormalMessage(SmsMessageBase sms) {
        SmsHeader smsHeader = sms.getUserDataHeader();

        // See if message is partial or port addressed.
        if ((smsHeader == null) || (smsHeader.concatRef == null)) {
            // Message is not partial (not part of concatenated sequence).
            byte[][] pdus = new byte[1][];
            pdus[0] = sms.getPdu();

            if (smsHeader != null && smsHeader.portAddrs != null) {
                if(isCuVersion() == true &&
                        (allowDispatchWapPush(mSimId) == false) &&
                        (isMmsWapPush(sms.getUserData()) == false)) {
                    // impl
                    Log.d(TAG, "don't dispatch push message");
                    return Intents.RESULT_SMS_HANDLED;
                }
                if (smsHeader.portAddrs.destPort == SmsHeader.PORT_WAP_PUSH) {
                    // GSM-style WAP indication
                    if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                        Log.d(TAG, "dispatch wap push pdu with addr & sc addr");
                        Bundle mBundle = new Bundle();
                        mBundle.putString(Telephony.WapPush.ADDR, sms.getOriginatingAddress());
                        mBundle.putString(Telephony.WapPush.SERVICE_ADDR, sms.getServiceCenterAddress());

                        return mWapPush.dispatchWapPdu(sms.getUserData(),mBundle);
                    } else {
                        Log.d(TAG, "dispatch wap push pdu");
                        return mWapPush.dispatchWapPdu(sms.getUserData());
                    }
                } else {
                    // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
                    // MTK_OPTR_PROTECT_START
                    if(mDMOperatorFile.searchMatchOp(sms.getOriginatingAddress(),smsHeader.portAddrs.destPort)) {
                          Log.d(TAG, "we receive a DM register SMS");
                        dispatchDmRegisterPdus(pdus);
                    }
                    else {
                    // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
                    // MTK_OPTR_PROTECT_END
                        // The message was sent to a port, so concoct a URI for it.
                        dispatchPortAddressedPdus(pdus, smsHeader.portAddrs.destPort);
                    // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
                    // MTK_OPTR_PROTECT_START
                    }
                    // MTK_OPTR_PROTECT_END
                    // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
                }
            } else {
                // Normal short and non-port-addressed message, dispatch it.
                dispatchPdus(pdus);
            }
            return Activity.RESULT_OK;
        } else {
            // Process the message part.
            SmsHeader.ConcatRef concatRef = smsHeader.concatRef;
            SmsHeader.PortAddrs portAddrs = smsHeader.portAddrs;
            return processMessagePart(sms.getPdu(), sms.getOriginatingAddress(),
                    concatRef.refNumber, concatRef.seqNumber, concatRef.msgCount,
                    sms.getTimestampMillis(), (portAddrs != null ? portAddrs.destPort : -1), false);
        }
    }

    /**
     * If this is the last part send the parts out to the application, otherwise
     * the part is stored for later processing. Handles both 3GPP concatenated messages
     * as well as 3GPP2 format WAP push messages processed by
     * {@link com.android.internal.telephony.cdma.CdmaSMSDispatcher#processCdmaWapPdu}.
     *
     * @param pdu the message PDU, or the datagram portion of a CDMA WDP datagram segment
     * @param address the originating address
     * @param referenceNumber distinguishes concatenated messages from the same sender
     * @param sequenceNumber the order of this segment in the message
     *          (starting at 0 for CDMA WDP datagrams and 1 for concatenated messages).
     * @param messageCount the number of segments in the message
     * @param timestamp the service center timestamp in millis
     * @param destPort the destination port for the message, or -1 for no destination port
     * @param isCdmaWapPush true if pdu is a CDMA WDP datagram segment and not an SM PDU
     *
     * @return a result code from {@link Telephony.Sms.Intents}, or
     *         {@link Activity#RESULT_OK} if the message has been broadcast
     *         to applications
     */
    protected int processMessagePart(byte[] pdu, String address, int referenceNumber,
            int sequenceNumber, int messageCount, long timestamp, int destPort,
            boolean isCdmaWapPush) {
        byte[][] pdus = null;
        Cursor cursor = null;
        try {
            // used by several query selection arguments
            String refNumber = Integer.toString(referenceNumber);
            String seqNumber = Integer.toString(sequenceNumber);
            String simId = Integer.toString(mSimId);

            // Check for duplicate message segment
            cursor = mResolver.query(mRawUri, PDU_PROJECTION,
                    "address=? AND reference_number=? AND sequence=? AND sim_id=?",
                    new String[] {
                    address, refNumber, seqNumber, simId
            }, null);

            // moveToNext() returns false if no duplicates were found
            if (cursor.moveToNext()) {
                Log.w(TAG, "Discarding duplicate message segment from address=" + address
                        + " refNumber=" + refNumber + " seqNumber=" + seqNumber);
                String oldPduString = cursor.getString(PDU_COLUMN);
                byte[] oldPdu = HexDump.hexStringToByteArray(oldPduString);
                if (!Arrays.equals(oldPdu, pdu)) {
                    Log.e(TAG, "Warning: dup message segment PDU of length " + pdu.length
                            + " is different from existing PDU of length " + oldPdu.length);
                }
                return Intents.RESULT_SMS_HANDLED;
            }
            cursor.close();

            // check whether the message is the first segment of one
            // concatenated sms
            boolean isFirstSegment = mConcatenatedSmsFwkExt.isFirstConcatenatedSegment(address,
                    referenceNumber);
            if (isCdmaWapPush == false && isFirstSegment == true) {
                Log.d(TAG, "ConcatenatedSmsFwkExt: the first segment, ref = " + referenceNumber);
                Log.d(TAG, "ConcatenatedSmsFwkExt: start a new timer");
                TimerRecord record = new TimerRecord(address, referenceNumber, messageCount);
                if (record == null) {
                    Log.d(TAG, "ConcatenatedSmsFwkExt: fail to new TimerRecord to start timer");
                }
                mConcatenatedSmsFwkExt.startTimer(this, record);
            }

            // not a dup, query for all other segments of this concatenated
            // message
            String where = "address=? AND reference_number=?";
            String[] whereArgs = new String[] {
                    address, refNumber
            };
            if (isCdmaWapPush) {
                // cdma wap push has diffrence reference_number for multi-segment push
                where = "address=?";
                whereArgs = new String[] {address};
            }
            cursor = mResolver.query(mRawUri, PDU_SEQUENCE_PORT_PROJECTION, where, whereArgs, null);

            int cursorCount = cursor.getCount();
            if (cursorCount != messageCount - 1) {
                Log.d(TAG, "ConcatenatedSmsFwkExt: refresh timer, ref = " + referenceNumber);
                TimerRecord record = mConcatenatedSmsFwkExt.queryTimerRecord(address,
                        referenceNumber);
                if (record == null) {
                    Log.d(TAG, "ConcatenatedSmsFwkExt: fail to get TimerRecord to refresh timer");
                }
                mConcatenatedSmsFwkExt.refreshTimer(this, record);

                // We don't have all the parts yet, store this one away
                ContentValues values = new ContentValues();
                values.put("date", timestamp);
                values.put("pdu", HexDump.toHexString(pdu));
                values.put("address", address);
                values.put("reference_number", referenceNumber);
                values.put("count", messageCount);
                values.put("sequence", sequenceNumber);
                values.put("sim_id", mSimId);
                if (destPort != -1) {
                    values.put("destination_port", destPort);
                }
                mResolver.insert(mRawUri, values);
                return Intents.RESULT_SMS_HANDLED;
            }

            // cancel the timer, because all segments are in place
            Log.d(TAG, "ConcatenatedSmsFwkExt: cancel timer, ref = " + referenceNumber);
            TimerRecord record = mConcatenatedSmsFwkExt.queryTimerRecord(address, referenceNumber);
            if (record == null) {
                Log.d(TAG, "ConcatenatedSmsFwkExt: fail to get TimerRecord to cancel timer");
            }
            mConcatenatedSmsFwkExt.cancelTimer(this, record);

            // All the parts are in place, deal with them
            pdus = new byte[messageCount][];
            for (int i = 0; i < cursorCount; i++) {
                cursor.moveToNext();
                int cursorSequence = cursor.getInt(SEQUENCE_COLUMN);
                // GSM sequence numbers start at 1; CDMA WDP datagram sequence numbers start at 0
                if (!isCdmaWapPush) {
                    cursorSequence--;
                }
                pdus[cursorSequence] = HexDump.hexStringToByteArray(
                        cursor.getString(PDU_COLUMN));

                // Read the destination port from the first segment (needed for CDMA WAP PDU).
                // It's not a bad idea to prefer the port from the first segment for 3GPP as well.
                if (cursorSequence == 0 && !cursor.isNull(DESTINATION_PORT_COLUMN)) {
                    destPort = cursor.getInt(DESTINATION_PORT_COLUMN);
                }
            }
            // This one isn't in the DB, so add it
            // GSM sequence numbers start at 1; CDMA WDP datagram sequence numbers start at 0
            if (isCdmaWapPush) {
                pdus[sequenceNumber] = pdu;
            } else {
                pdus[sequenceNumber - 1] = pdu;
            }

            // Remove the parts from the database
            mResolver.delete(mRawUri, where, whereArgs);
        } catch (SQLException e) {
            Log.e(TAG, "Can't access multipart SMS database", e);
            return Intents.RESULT_SMS_GENERIC_ERROR;
        } finally {
            if (cursor != null) cursor.close();
        }

        // Special handling for CDMA WDP datagrams
        if (isCdmaWapPush) {
            // Build up the data stream
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (int i = 0; i < messageCount; i++) {
                // reassemble the (WSP-)pdu
                output.write(pdus[i], 0, pdus[i].length);
            }
            byte[] datagram = output.toByteArray();

            // Dispatch the PDU to applications
            if (destPort == SmsHeader.PORT_WAP_PUSH) {
                // Handle the PUSH
                return mWapPush.dispatchWapPdu(datagram);
            } else {
                pdus = new byte[1][];
                pdus[0] = datagram;
                // The messages were sent to any other WAP port
                dispatchPortAddressedPdus(pdus, destPort);
                return Activity.RESULT_OK;
            }
        }

        // Dispatch the PDUs to applications
        if (destPort != -1) {
            if (destPort == SmsHeader.PORT_WAP_PUSH) {
                // Build up the data stream
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                for (int i = 0; i < messageCount; i++) {
                    SmsMessage msg = SmsMessage.createFromPdu(pdus[i], getFormat());
                    if(msg != null) {
                        byte[] data = msg.getUserData();
                        output.write(data, 0, data.length);
                    }
                }
                
                if(isCuVersion() == true &&
                        (allowDispatchWapPush(mSimId) == false) &&
                        (isMmsWapPush(output.toByteArray()) == false)) {
                    // impl
                    Log.d(TAG, "don't dispatch push message");
                    return Intents.RESULT_SMS_HANDLED;
                }
                
                // Handle the PUSH
                // return mWapPush.dispatchWapPdu(output.toByteArray());
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    Log.d(TAG, "2 - dispatch wap push pdu with addr & sc addr");
                    SmsMessage sms = SmsMessage.createFromPdu(pdus[0], getFormat());
                    Bundle mBundle = new Bundle();
                    if(sms != null) {
                        mBundle.putString(Telephony.WapPush.ADDR, sms.getOriginatingAddress());
                        mBundle.putString(Telephony.WapPush.SERVICE_ADDR, sms.getServiceCenterAddress());
                    }

                    return mWapPush.dispatchWapPdu(output.toByteArray(), mBundle);
                } else {
                    Log.d(TAG, "2 - dispatch wap push pdu");
                    return mWapPush.dispatchWapPdu(output.toByteArray());
                }
            } else {
                // The messages were sent to a port, so concoct a URI for it
                dispatchPortAddressedPdus(pdus, destPort);
            }
        } else {
            // The messages were not sent to a port
            dispatchPdus(pdus);
        }
        return Activity.RESULT_OK;
    }

    /**
     * Dispatches standard PDUs to interested applications
     *
     * @param pdus The raw PDUs making up the message
     */
    protected void dispatchPdus(byte[][] pdus) {
        Intent intent = new Intent(Intents.SMS_RECEIVED_ACTION);
        intent.putExtra("pdus", pdus);
        intent.putExtra("format", getFormat());
        dispatch(intent, RECEIVE_SMS_PERMISSION);
    }

    /**
     * Dispatches port addressed PDUs to interested applications
     *
     * @param pdus The raw PDUs making up the message
     * @param port The destination port of the messages
     */
    protected void dispatchPortAddressedPdus(byte[][] pdus, int port) {
        Uri uri = Uri.parse("sms://localhost:" + port);
        Intent intent = new Intent(Intents.DATA_SMS_RECEIVED_ACTION, uri);
        intent.putExtra("pdus", pdus);
        intent.putExtra("format", getFormat());
        
        if (port == 8025 || port == 7275 || port == 7276){
            dispatch(intent, null);   //for AGPS only
            Log.d("MtkAgps","=========== SMSDispatcher: Send SMS For A-GPS SUPL NI ========");
        } else {
            dispatch(intent, "android.permission.RECEIVE_SMS");
        }
    }

    /**
     * Send a data based SMS to a specific application port.
     *
     * @param destAddr the address to send the message to
     * @param scAddr is the service center address or null to use
     *  the current default SMSC
     * @param destPort the port to deliver the message to
     * @param data the body of the message to send
     * @param sentIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is successfully sent, or failed.
     *  The result code will be <code>Activity.RESULT_OK<code> for success,
     *  or one of these errors:<br>
     *  <code>RESULT_ERROR_GENERIC_FAILURE</code><br>
     *  <code>RESULT_ERROR_RADIO_OFF</code><br>
     *  <code>RESULT_ERROR_NULL_PDU</code><br>
     *  <code>RESULT_ERROR_NO_SERVICE</code><br>.
     *  For <code>RESULT_ERROR_GENERIC_FAILURE</code> the sentIntent may include
     *  the extra "errorCode" containing a radio technology specific value,
     *  generally only useful for troubleshooting.<br>
     *  The per-application based SMS control checks sentIntent. If sentIntent
     *  is NULL the caller will be checked against all unknown applications,
     *  which cause smaller number of SMS to be sent in checking period.
     * @param deliveryIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is delivered to the recipient.  The
     *  raw pdu of the status report is in the extended data ("pdu").
     */
    protected abstract void sendData(String destAddr, String scAddr, int destPort,
            byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent);

    /**
     * Send a text based SMS.
     *
     * @param destAddr the address to send the message to
     * @param scAddr is the service center address or null to use
     *  the current default SMSC
     * @param text the body of the message to send
     * @param sentIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is successfully sent, or failed.
     *  The result code will be <code>Activity.RESULT_OK<code> for success,
     *  or one of these errors:<br>
     *  <code>RESULT_ERROR_GENERIC_FAILURE</code><br>
     *  <code>RESULT_ERROR_RADIO_OFF</code><br>
     *  <code>RESULT_ERROR_NULL_PDU</code><br>
     *  <code>RESULT_ERROR_NO_SERVICE</code><br>.
     *  For <code>RESULT_ERROR_GENERIC_FAILURE</code> the sentIntent may include
     *  the extra "errorCode" containing a radio technology specific value,
     *  generally only useful for troubleshooting.<br>
     *  The per-application based SMS control checks sentIntent. If sentIntent
     *  is NULL the caller will be checked against all unknown applications,
     *  which cause smaller number of SMS to be sent in checking period.
     * @param deliveryIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is delivered to the recipient.  The
     *  raw pdu of the status report is in the extended data ("pdu").
     */
    protected abstract void sendText(String destAddr, String scAddr,
            String text, PendingIntent sentIntent, PendingIntent deliveryIntent);

    /**
     * Calculate the number of septets needed to encode the message.
     *
     * @param messageBody the message to encode
     * @param use7bitOnly ignore (but still count) illegal characters if true
     * @return TextEncodingDetails
     */
    protected abstract TextEncodingDetails calculateLength(CharSequence messageBody,
            boolean use7bitOnly);

    /**
     * Send a multi-part text based SMS.
     *
     * @param destAddr the address to send the message to
     * @param scAddr is the service center address or null to use
     *   the current default SMSC
     * @param parts an <code>ArrayList</code> of strings that, in order,
     *   comprise the original message
     * @param sentIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been sent.
     *   The result code will be <code>Activity.RESULT_OK<code> for success,
     *   or one of these errors:
     *   <code>RESULT_ERROR_GENERIC_FAILURE</code>
     *   <code>RESULT_ERROR_RADIO_OFF</code>
     *   <code>RESULT_ERROR_NULL_PDU</code>
     *   <code>RESULT_ERROR_NO_SERVICE</code>.
     *  The per-application based SMS control checks sentIntent. If sentIntent
     *  is NULL the caller will be checked against all unknown applications,
     *  which cause smaller number of SMS to be sent in checking period.
     * @param deliveryIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been delivered
     *   to the recipient.  The raw pdu of the status report is in the
     *   extended data ("pdu").
     */
    protected void sendMultipartText(String destAddr, String scAddr,
            ArrayList<String> parts, ArrayList<PendingIntent> sentIntents,
            ArrayList<PendingIntent> deliveryIntents) {

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

            sendNewSubmitPdu(destAddr, scAddr, parts.get(i), smsHeader, encoding,
                    sentIntent, deliveryIntent, (i == (msgCount - 1)));
        }

    }

    /**
     * Create a new SubmitPdu and send it.
     */
    protected abstract void sendNewSubmitPdu(String destinationAddress, String scAddress,
            String message, SmsHeader smsHeader, int encoding,
            PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart);

    /**
     * Send a SMS
     *
     * @param smsc the SMSC to send the message through, or NULL for the
     *  default SMSC
     * @param pdu the raw PDU to send
     * @param sentIntent if not NULL this <code>Intent</code> is
     *  broadcast when the message is successfully sent, or failed.
     *  The result code will be <code>Activity.RESULT_OK<code> for success,
     *  or one of these errors:
     *  <code>RESULT_ERROR_GENERIC_FAILURE</code>
     *  <code>RESULT_ERROR_RADIO_OFF</code>
     *  <code>RESULT_ERROR_NULL_PDU</code>
     *  <code>RESULT_ERROR_NO_SERVICE</code>.
     *  The per-application based SMS control checks sentIntent. If sentIntent
     *  is NULL the caller will be checked against all unknown applications,
     *  which cause smaller number of SMS to be sent in checking period.
     * @param deliveryIntent if not NULL this <code>Intent</code> is
     *  broadcast when the message is delivered to the recipient.  The
     *  raw pdu of the status report is in the extended data ("pdu").
     */
    protected void sendRawPdu(byte[] smsc, byte[] pdu, PendingIntent sentIntent,
            PendingIntent deliveryIntent) {
        if (mSmsSendDisabled) {
            if (sentIntent != null) {
                try {
                    sentIntent.send(RESULT_ERROR_NO_SERVICE);
                } catch (CanceledException ex) {}
            }
            Log.d(TAG, "Device does not support sending sms.");
            return;
        }

        if (pdu == null) {
            if (sentIntent != null) {
                try {
                    sentIntent.send(RESULT_ERROR_NULL_PDU);
                } catch (CanceledException ex) {}
            }
            return;
        }

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("smsc", smsc);
        map.put("pdu", pdu);

        SmsTracker tracker = new SmsTracker(map, sentIntent,
                deliveryIntent);
        int ss = mPhone.getServiceState().getState();

        if (ss != ServiceState.STATE_IN_SERVICE) {
            handleNotInService(ss, tracker);
        } else {
            String appName = getAppNameByIntent(sentIntent);
            // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
            if(FeatureOption.MTK_SMS_FILTER_SUPPORT == true) {
                SmsMessage msg = createMessageFromSubmitPdu(smsc, pdu);
                if(msg != null) {
                    boolean ret = checkSmsWithNqFilter(msg.getDestinationAddress(), msg.getMessageBody(), sentIntent);
                    if(ret == false) {
                        Log.d(TAG, "[NQ this message is safe");
                        if (mUsageMonitor.check(appName, SINGLE_PART_SMS)) {
                            sendSms(tracker);
                        } else {
                            sendMessage(obtainMessage(EVENT_POST_ALERT, tracker));
                        }
                    } else {
                        Log.d(TAG, "[NQ this message may deduct fees");
                        
                        SmsHeader.ConcatRef newConcatRef = null;
                        if(msg.getUserDataHeader() != null) {
                            newConcatRef = msg.getUserDataHeader().concatRef;
                        }
                        
                        if(newConcatRef != null) {
                            if(sConcatRef == null || sConcatRef.refNumber != newConcatRef.refNumber) {
                                Log.d(TAG, "[NQ this is a new concatenated message, just update");
                                sConcatRef = newConcatRef;
                                //sConcatMsgCount = 1;
                                sendMessage(obtainMessage(EVENT_HANDLE_REDUCTED_MESSAGE, tracker));
                            } else {
                                Log.d(TAG, "[NQ this is the same concatenated message, keep previous operation");
                                mSTrackers.add(tracker);
                                sConcatMsgCount += 1;
                            }
                        } else {
                            Log.d(TAG, "[NQ this is a non-concatenated message");
                            //sConcatMsgCount = 0;
                            sendMessage(obtainMessage(EVENT_HANDLE_REDUCTED_MESSAGE, tracker));
                        }
                    }
                } else {
                    Log.d(TAG, "[NQ fail to create message from pdu");
                    if (mUsageMonitor.check(appName, SINGLE_PART_SMS)) {
                        sendSms(tracker);
                    } else {
                        sendMessage(obtainMessage(EVENT_POST_ALERT, tracker));
                    }
                }
            } else {
            // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
                if (mUsageMonitor.check(appName, SINGLE_PART_SMS)) {
                    sendSms(tracker);
                } else {
                    sendMessage(obtainMessage(EVENT_POST_ALERT, tracker));
                }
            // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
            }
            // MTK-END   [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
        }
    }

    /**
     * Post an alert while SMS needs user confirm.
     *
     * An SmsTracker for the current message.
     */
    protected void handleReachSentLimit(SmsTracker tracker) {
        if (mSTrackers.size() >= MO_MSG_QUEUE_LIMIT) {
            // Deny the sending when the queue limit is reached.
            try {
                tracker.mSentIntent.send(RESULT_ERROR_LIMIT_EXCEEDED);
            } catch (CanceledException ex) {
                Log.e(TAG, "failed to send back RESULT_ERROR_LIMIT_EXCEEDED");
            }
            return;
        }

        Resources r = Resources.getSystem();

        String appName = getAppNameByIntent(tracker.mSentIntent);

        AlertDialog d = new AlertDialog.Builder(mContext)
                .setTitle(r.getString(R.string.sms_control_title))
                .setMessage(appName + " " + r.getString(R.string.sms_control_message))
                .setPositiveButton(r.getString(R.string.sms_control_yes), mListener)
                .setNegativeButton(r.getString(R.string.sms_control_no), mListener)
                .create();

        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        d.show();

        mSTrackers.add(tracker);
        sendMessageDelayed ( obtainMessage(EVENT_ALERT_TIMEOUT, d),
                DEFAULT_SMS_TIMEOUT);
    }

    protected static String getAppNameByIntent(PendingIntent intent) {
        Resources r = Resources.getSystem();
        return (intent != null) ? intent.getTargetPackage()
            : r.getString(R.string.sms_control_default_app_name);
    }

    /**
     * Send the message along to the radio.
     *
     * @param tracker holds the SMS message to send
     */
    protected abstract void sendSms(SmsTracker tracker);

    /**
     * Send the multi-part SMS based on multipart Sms tracker
     *
     * @param tracker holds the multipart Sms tracker ready to be sent
     */
    private void sendMultipartSms(SmsTracker tracker) {
        ArrayList<String> parts;
        ArrayList<PendingIntent> sentIntents;
        ArrayList<PendingIntent> deliveryIntents;

        HashMap<String, Object> map = tracker.mData;

        String destinationAddress = (String) map.get("destination");
        String scAddress = (String) map.get("scaddress");

        parts = (ArrayList<String>) map.get("parts");
        sentIntents = (ArrayList<PendingIntent>) map.get("sentIntents");
        deliveryIntents = (ArrayList<PendingIntent>) map.get("deliveryIntents");

        // check if in service
        int ss = mPhone.getServiceState().getState();
        if (ss != ServiceState.STATE_IN_SERVICE) {
            for (int i = 0, count = parts.size(); i < count; i++) {
                PendingIntent sentIntent = null;
                if (sentIntents != null && sentIntents.size() > i) {
                    sentIntent = sentIntents.get(i);
                }
                handleNotInService(ss, new SmsTracker(null, sentIntent, null));
            }
            return;
        }

        sendMultipartText(destinationAddress, scAddress, parts, sentIntents, deliveryIntents);
    }

    /**
     * Send an acknowledge message.
     * @param success indicates that last message was successfully received.
     * @param result result code indicating any error
     * @param response callback message sent when operation completes.
     */
    protected abstract void acknowledgeLastIncomingSms(boolean success,
            int result, Message response);

    /**
     * Notify interested apps if the framework has rejected an incoming SMS,
     * and send an acknowledge message to the network.
     * @param success indicates that last message was successfully received.
     * @param result result code indicating any error
     * @param response callback message sent when operation completes.
     */
    private void notifyAndAcknowledgeLastIncomingSms(boolean success,
            int result, Message response) {
        if (!success) {
            // broadcast SMS_REJECTED_ACTION intent
            Intent intent = new Intent(Intents.SMS_REJECTED_ACTION);
            intent.putExtra("result", result);
            mWakeLock.acquire(WAKE_LOCK_TIMEOUT);
            mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
        }
        acknowledgeLastIncomingSms(success, result, response);
    }

    /**
     * Keeps track of an SMS that has been sent to the RIL, until it has
     * successfully been sent, or we're done trying.
     *
     */
    protected static final class SmsTracker {
        // fields need to be public for derived SmsDispatchers
        public final HashMap<String, Object> mData;
        public int mRetryCount;
        public int mMessageRef;

        public final PendingIntent mSentIntent;
        public final PendingIntent mDeliveryIntent;

        public SmsTracker(HashMap<String, Object> data, PendingIntent sentIntent,
                PendingIntent deliveryIntent) {
            mData = data;
            mSentIntent = sentIntent;
            mDeliveryIntent = deliveryIntent;
            mRetryCount = 0;
        }

        /**
         * Returns whether this tracker holds a multi-part SMS.
         * @return true if the tracker holds a multi-part SMS; false otherwise
         */
        protected boolean isMultipart() {
            HashMap map = mData;
            return map.containsKey("parts");
        }
    }

    private final DialogInterface.OnClickListener mListener =
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    Log.d(TAG, "click YES to send out sms");
                    sendMessage(obtainMessage(EVENT_SEND_CONFIRMED_SMS));
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    Log.d(TAG, "click NO to stop sending");
                    sendMessage(obtainMessage(EVENT_STOP_SENDING));
                }
                
                // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
                if(FeatureOption.MTK_SMS_FILTER_SUPPORT == true) {
                    while(sConcatMsgCount > 0 && mSTrackers.size() > 0) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            Log.d(TAG, "[NQ continue sending " + sConcatMsgCount);
                            sendMessage(obtainMessage(EVENT_SEND_CONFIRMED_SMS));
                        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                            Log.d(TAG, "[NQ stop sending " + sConcatMsgCount);
                            // sendMessage(obtainMessage(EVENT_STOP_SENDING));
                            mSTrackers.remove(mSTrackers.size() - 1);
                        }
                        
                        sConcatMsgCount -= 1;
                    } // end while(sConcatMsgCount > 0 && mSTrackers.size() > 0)
                }
                // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
            }
        };

    private final BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        boolean hasNotifiedForWapPushSetting = false;
        @Override
        public void onReceive(Context context, Intent intent) {
            // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
            if (intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_FULL/*Intent.ACTION_DEVICE_STORAGE_LOW*/)) {
                mStorageAvailable = false;
                mCm.reportSmsMemoryStatus(false, obtainMessage(EVENT_REPORT_MEMORY_STATUS_DONE));
            } else if (intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_NOT_FULL/*Intent.ACTION_DEVICE_STORAGE_OK*/)) {
                mStorageAvailable = true;
                mCm.reportSmsMemoryStatus(true, obtainMessage(EVENT_REPORT_MEMORY_STATUS_DONE));
            } else if (intent.getAction().equals(android.provider.Telephony.CbSms.Intents.CB_SMS_RECEIVED_ACTION)) {
                    // do nothing
            } else if(intent.getAction().equals(ACTION_WAP_PUSH_NOTI_CANCEL)) {
               Log.d(TAG, "receive cancel intent");
               NotificationManager notiMgr = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
               if(notiMgr != null) {
                   Log.d(TAG, "cancel wap push setting notification");
                   notiMgr.cancel(WAP_PUSH_NOTI_ID);
               } else {
                   Log.d(TAG, "fail to create notiMgr by mContext");
               }
            } else if(intent.getAction().equals(TelephonyIntents.ACTION_SIM_INFO_UPDATE)) {
                Log.d(TAG, "siminfo has been updated");
                if(hasNotifiedForWapPushSetting == true) {
                    Log.d(TAG, "wap push setting notification has done");
                    return;
                }
                hasNotifiedForWapPushSetting = true;
                // check wap push setting from SIMInfo
                // we just check this in SIM 1 because only notify once,
                // even two SIM are all new
                // the checking action should be executed after siminfo
                // db has been updated
                if((isCuVersion() == true) && (mSimId == Phone.GEMINI_SIM_1)) {
                    boolean hasUnsetSim = (checkWapPushSettingStatus() == false);
                    if(hasUnsetSim) {
                        // notifyForUnsetSim();
                        Log.d(TAG, "send delayed message for wap push noti");
                        sendMessageDelayed(obtainMessage(EVENT_DELAY_WAP_PUSH_SETTING_NOTI), DELAY_NOTI_TIME);
                    }
                }
            } else {
            // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
                // Assume the intent is one of the SMS receive intents that
                // was sent as an ordered broadcast.  Check result and ACK.
                int rc = getResultCode();
                boolean success = (rc == Activity.RESULT_OK)
                                    || (rc == Intents.RESULT_SMS_HANDLED);

                long rTime = intent.getLongExtra("rTime", -1);
                if (rTime != -1) {
                    long curTime = System.currentTimeMillis();
                    Log.d(TAG, "CNMA elplased time: " + (curTime - rTime));
                    if ((curTime - rTime) / 1000 > 8) {
                        Log.d(TAG, "APP process too long");                
                    } else { 
                        // For a multi-part message, this only ACKs the last part.
                        // Previous parts were ACK'd as they were received.
                        acknowledgeLastIncomingSms(success, rc, null);
                    }
                }   
            // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
            }
            // MTK-END   [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
        }
    };

    protected void dispatchBroadcastPdus(byte[][] pdus, boolean isEmergencyMessage) {
        if (isEmergencyMessage) {
            Intent intent = new Intent(Intents.SMS_EMERGENCY_CB_RECEIVED_ACTION);
            intent.putExtra("pdus", pdus);
            Log.d(TAG, "Dispatching " + pdus.length + " emergency SMS CB pdus");
            dispatch(intent, RECEIVE_EMERGENCY_BROADCAST_PERMISSION);
        } else {
            Intent intent = new Intent(Intents.SMS_CB_RECEIVED_ACTION);
            intent.putExtra("pdus", pdus);
            Log.d(TAG, "Dispatching " + pdus.length + " SMS CB pdus");
            dispatch(intent, RECEIVE_SMS_PERMISSION);
        }
    }
    
    // MTK-START [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    /**
     * Called when SIM_FULL message is received from the RIL.  Notifies interested
     * parties that SIM storage for SMS messages is full.
     */
    private void handleIccFull(){
        // broadcast SIM_FULL intent
        Intent intent = new Intent(Intents.SIM_FULL_ACTION);
        intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);
        mWakeLock.acquire(WAKE_LOCK_TIMEOUT);
        mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
    }

    /**
     * Called when a CB activation result is received.  
     *
     * @param ar AsyncResult passed into the message handler. 
     */
    protected void handleQueryCbActivation(AsyncResult ar) {
        Log.e(TAG, "didn't support cellBoradcast in the CDMA phone");
    }

    /**
     * Dispatches an incoming Cb SMS messages.
     *
     * @param smsPdu the pdu string of the incoming message from the phone
     */
    protected void dispatchCbMessage(String smsPdu)
    {
        Log.e(TAG, "didn't support cellBoradcast in the CDMA phone");
    }

    /**
     * If this is the last part send the parts out to the application, otherwise
     * the part is stored for later processing.
     *
     * NOTE: concatRef (naturally) needs to be non-null, but portAddrs can be null.
     * @return a result code from {@link Telephony.Sms.Intents}, or
     *         {@link Activity#RESULT_OK} if the message has been broadcast
     *         to applications
     */
    protected int processMessagePart(SmsMessageBase sms,
            SmsHeader.ConcatRef concatRef, SmsHeader.PortAddrs portAddrs) {
        // Lookup all other related parts
        StringBuilder where = new StringBuilder("reference_number =");
        where.append(concatRef.refNumber);
        where.append(" AND address = ?");
        where.append(" AND sim_id = " + mSimId);
        String[] whereArgs = new String[] {sms.getOriginatingAddress()};

        byte[][] pdus = null;
        Cursor cursor = null;
        try {
            cursor = mResolver.query(mRawUri, RAW_PROJECTION, where.toString(), whereArgs, null);
            if (cursor == null) {
                return Intents.RESULT_SMS_GENERIC_ERROR;
            }
            int cursorCount = cursor.getCount();

            // All the parts are in place, deal with them
            int pduColumn = cursor.getColumnIndex("pdu");
            int sequenceColumn = cursor.getColumnIndex("sequence");

            // for ALPS00007326
            // judeg if we have already received the same segment
            for (int i = 0; i < cursorCount; i++) {
                cursor.moveToNext();
                int cursorSequence = (int)cursor.getLong(sequenceColumn);
                if (cursorSequence == concatRef.seqNumber) {
                    Log.w(TAG, "Received Duplicate segment: " + cursorSequence);
                    return Intents.RESULT_SMS_HANDLED;
                }
            }
            cursor.moveToFirst();

            if (cursorCount != concatRef.msgCount - 1) {
                // We don't have all the parts yet, store this one away
                ContentValues values = new ContentValues();
                values.put("date", new Long(sms.getTimestampMillis()));
                values.put("pdu", HexDump.toHexString(sms.getPdu()));
                values.put("address", sms.getOriginatingAddress());
                values.put("reference_number", concatRef.refNumber);
                values.put("count", concatRef.msgCount);
                values.put("sequence", concatRef.seqNumber);
                values.put("sim_id", mSimId);
                if (portAddrs != null) {
                    values.put("destination_port", portAddrs.destPort);
                }
                mResolver.insert(mRawUri, values);
                return Intents.RESULT_SMS_HANDLED;
            }

            pdus = new byte[concatRef.msgCount][];
            for (int i = 0; i < cursorCount; i++) {
                
                int cursorSequence = (int)cursor.getLong(sequenceColumn);
                pdus[cursorSequence - 1] = HexDump.hexStringToByteArray(
                        cursor.getString(pduColumn));
                cursor.moveToNext();
            }
            // This one isn't in the DB, so add it
            pdus[concatRef.seqNumber - 1] = sms.getPdu();

            // Remove the parts from the database
            mResolver.delete(mRawUri, where.toString(), whereArgs);
        } catch (SQLException e) {
            Log.e(TAG, "Can't access multipart SMS database", e);
            // TODO:  Would OUT_OF_MEMORY be more appropriate?
            return Intents.RESULT_SMS_GENERIC_ERROR;
        } finally {
            if (cursor != null) cursor.close();
        }

        /**
         * TODO(cleanup): The following code has duplicated logic with
         * the radio-specific dispatchMessage code, which is fragile,
         * in addition to being redundant.  Instead, if this method
         * maybe returned the reassembled message (or just contents),
         * the following code (which is not really related to
         * reconstruction) could be better consolidated.
         */

        // Dispatch the PDUs to applications
        if (portAddrs != null) {
            if((isCuVersion() == true) && (allowDispatchWapPush(mSimId) == false)) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                for (int i = 0; i < concatRef.msgCount; i++) {
                    SmsMessage msg = SmsMessage.createFromPdu(pdus[i]);
                    if(msg != null) {
                        byte[] data = msg.getUserData();
                        output.write(data, 0, data.length);
                    }
                }
                boolean isMms = isMmsWapPush(output.toByteArray());
                if(isMms == false) {
                    Log.d(TAG, "don't dispatch push message");
                    return Intents.RESULT_SMS_HANDLED;
                }
            }
            if (portAddrs.destPort == SmsHeader.PORT_WAP_PUSH) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                //modified by mtk80611
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    Bundle mBundle = new Bundle();
                    SmsMessage msg = SmsMessage.createFromPdu(pdus[0]);
                    if(msg != null) {
                        byte[] data = msg.getUserData();
                        output.write(data,0,data.length);
    
                        mBundle.putString(Telephony.WapPush.ADDR, sms.getOriginatingAddress());
                        mBundle.putString(Telephony.WapPush.SERVICE_ADDR, sms.getServiceCenterAddress());
    
                        for(int i = 1;i<concatRef.msgCount;i++){
                            msg = SmsMessage.createFromPdu(pdus[i]);
                            if(msg != null) {
                                data = msg.getUserData();
                                output.write(data,0,data.length);
                            }
                        }
                        // Handle the PUSH
                        return mWapPush.dispatchWapPdu(output.toByteArray(),mBundle);
                    }
                }
                else
                {
                    for (int i = 0; i < concatRef.msgCount; i++) {
                        SmsMessage msg = SmsMessage.createFromPdu(pdus[i]);
                        if(msg != null) {
                            byte[] data = msg.getUserData();
                            output.write(data, 0, data.length);
                        }
                    }
                    return mWapPush.dispatchWapPdu(output.toByteArray());
                }
                //modified by mtk80611
            } else {
                // The messages were sent to a port, so concoct a URI for it
                SmsMessage msg = SmsMessage.createFromPdu(pdus[0]);
                // MTK_OPTR_PROTECT_START
                if ( true && /*need a feature option*/
                    portAddrs.destPort == DM_PORT &&
                    msg != null &&
                    msg.getOriginatingAddress().equals(DM_OA) ) {

                    dispatchDmRegisterPdus(pdus);
                }
                else {
                // MTK_OPTR_PROTECT_END
                    dispatchPortAddressedPdus(pdus, portAddrs.destPort);
                // MTK_OPTR_PROTECT_START
                }
                // MTK_OPTR_PROTECT_END
            }
        } else {
            // The messages were not sent to a port
            SmsMessage msg = SmsMessage.createFromPdu(pdus[0]);
            if (msg != null && msg.getMessageBody() == null)
            {
                Log.d(TAG," We discard SMS with dcs 8 bit");
                return Intents.RESULT_SMS_GENERIC_ERROR;
            }
            dispatchPdus(pdus);
        }
        return Activity.RESULT_OK;
    }

    // MTK_OPTR_PROTECT_START
    private BroadcastReceiver mDMLockReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "[DM-Lock receive lock/unlock intent");
                if(intent.getAction().equals("com.mediatek.dm.LAWMO_LOCK")) {
                    Log.d(TAG, "[DM-Lock DM is locked now");
                    isDmLock = true;
                } else if(intent.getAction().equals("com.mediatek.dm.LAWMO_UNLOCK")) {
                    Log.d(TAG, "[DM-Lock DM is unlocked now");
                    isDmLock = false;
                }
            }
    };
    // MTK_OPTR_PROTECT_END

    protected boolean checkSmsWithNqFilter(String address, String text, PendingIntent sentIntent) {
        String pkgName = getAppNameByIntent(sentIntent);
        //String appName = mContext.getPackageManager().getApplicationLabel(mContext.getApplicationInfo()).toString();
        String appName = null;
        try {
            ApplicationInfo appInfo = mContext.getPackageManager().getApplicationInfo(pkgName, 0);
            appName = mContext.getPackageManager().getApplicationLabel(appInfo).toString();
        } catch(NameNotFoundException e) {
            appName = Resources.getSystem().getString(R.string.sms_control_default_app_name);
        }

        Log.d(TAG, "[NQ address = " + address + ", text = " + text
            + ", pkgName = " + pkgName + ", appName = " + appName);

        boolean isDeductedMessage = false;
        try {
            isDeductedMessage = NqSmsFilter.getInstance(mContext).nqSmsFilter(address, text, pkgName, appName);
        } catch(Exception e) {
            Log.d(TAG, "[Nq Exception is thrown when call NqSmsFilter");
        }
        
        return isDeductedMessage;
    }
    
    private void handleDeductedMessage(SmsTracker tracker) {
        if (mSTrackers.size() >= MO_MSG_QUEUE_LIMIT) {
            // Deny the sending when the queue limit is reached.
            try {
                tracker.mSentIntent.send(RESULT_ERROR_LIMIT_EXCEEDED);
            } catch (CanceledException ex) {
                Log.e(TAG, "failed to send back RESULT_ERROR_LIMIT_EXCEEDED");
            }
            return;
        }
        
        Resources r = Resources.getSystem();
        
        AlertDialog dlg = new AlertDialog.Builder(mContext)
            .setTitle(r.getString(com.mediatek.internal.R.string.nq_sms_filter_title))
            .setMessage(r.getString(com.mediatek.internal.R.string.nq_sms_filter_message))
            .setPositiveButton(r.getString(com.mediatek.internal.R.string.nq_sms_filter_yes), mListener)
            .setNegativeButton(r.getString(com.mediatek.internal.R.string.nq_sms_filter_no), mListener)
            .create();

        dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        dlg.setCancelable(false);
        dlg.show();
        
        mSTrackers.add(tracker);
        sendMessageDelayed ( obtainMessage(EVENT_REDUCTED_MESSAGE_TIMEOUT, dlg),
                DEFAULT_SMS_TIMEOUT);
    }
    
    private SmsMessage createMessageFromPdu(byte[] smsc, byte[] tpdu) {
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
        
        return SmsMessage.createFromPdu(msgPdu, getFormat());
    }

    /**
     * Dispatches standard CB PDUs to interested applications
     *
     * @param pdus The raw PDUs making up the message
     */
    protected void dispatchCbPdus(byte[][] pdus) {
        Intent intent = new Intent(
            android.provider.Telephony.CbSms.Intents.CB_SMS_RECEIVED_ACTION);
        intent.putExtra("pdus", pdus);
        dispatch(intent, "android.permission.RECEIVE_CB_SMS");
    }
    
    // MTK_OPTR_PROTECT_START
    /**
     * Dispatches DM Register PDUs to DM APP
     *
     * @param pdus The raw PDUs making up the message
     */
    protected void dispatchDmRegisterPdus(byte[][] pdus) {
        Intent intent = new Intent(Intents.DM_REGISTER_SMS_RECEIVED_ACTION);
        intent.putExtra("pdus", pdus);
        dispatch(intent, "android.permission.RECEIVE_DM_REGISTER_SMS");
    }
    // MTK_OPTR_PROTECT_END
    
/**
     * Send a data based SMS to a specific application port.
     *
     * @param destAddr the address to send the message to
     * @param scAddr is the service center address or null to use
     *  the current default SMSC
     * @param destPort the port to deliver the message to
     * @param originalPort the port to deliver the message from
     * @param data the body of the message to send
     * @param sentIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is successfully sent, or failed.
     *  The result code will be <code>Activity.RESULT_OK<code> for success,
     *  or one of these errors:<br>
     *  <code>RESULT_ERROR_GENERIC_FAILURE</code><br>
     *  <code>RESULT_ERROR_RADIO_OFF</code><br>
     *  <code>RESULT_ERROR_NULL_PDU</code><br>
     *  For <code>RESULT_ERROR_GENERIC_FAILURE</code> the sentIntent may include
     *  the extra "errorCode" containing a radio technology specific value,
     *  generally only useful for troubleshooting.<br>
     *  The per-application based SMS control checks sentIntent. If sentIntent
     *  is NULL the caller will be checked against all unknown applications,
     *  which cause smaller number of SMS to be sent in checking period.
     * @param deliveryIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is delivered to the recipient.  The
     *  raw pdu of the status report is in the extended data ("pdu").
     */
    protected abstract void sendData(String destAddr, String scAddr, int destPort, int originalPort,
            byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent);

/**
     * Send a multi-part data based SMS.
     *
     * @param destinationAddress the address to send the message to
     * @param scAddress is the service center address or null to use
     *   the current default SMSC
     * @param data an <code>ArrayList</code> of strings that, in order,
     *   comprise the original message
     * @param destPort the port to deliver the message to
     * @param data an array of data messages in order,
     *   comprise the original message     
     * @param sentIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been sent.
     *   The result code will be <code>Activity.RESULT_OK<code> for success,
     *   or one of these errors:
     *   <code>RESULT_ERROR_GENERIC_FAILURE</code>
     *   <code>RESULT_ERROR_RADIO_OFF</code>
     *   <code>RESULT_ERROR_NULL_PDU</code>.
     * @param deliveryIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been delivered
     *   to the recipient.  The raw pdu of the status report is in the
     *   extended data ("pdu").
     */
    protected abstract void sendMultipartData(
            String destAddr, String scAddr, int destPort,
            ArrayList<SmsRawData> data, ArrayList<PendingIntent> sentIntents, 
            ArrayList<PendingIntent> deliveryIntents);

    /**
     * Send a text based SMS to a specified application port.
     *
     * @param destAddr the address to send the message to
     * @param scAddr is the service center address or null to use
     *  the current default SMSC
     * @param text the body of the message to send
     * @param destPort the port to deliver the message to
     * @param sentIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is sucessfully sent, or failed.
     *  The result code will be <code>Activity.RESULT_OK<code> for success,
     *  or one of these errors:<br>
     *  <code>RESULT_ERROR_GENERIC_FAILURE</code><br>
     *  <code>RESULT_ERROR_RADIO_OFF</code><br>
     *  <code>RESULT_ERROR_NULL_PDU</code><br>
     *  For <code>RESULT_ERROR_GENERIC_FAILURE</code> the sentIntent may include
     *  the extra "errorCode" containing a radio technology specific value,
     *  generally only useful for troubleshooting.<br>
     *  The per-application based SMS control checks sentIntent. If sentIntent
     *  is NULL the caller will be checked against all unknown applications,
     *  which cause smaller number of SMS to be sent in checking period.
     * @param deliveryIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is delivered to the recipient.  The
     *  raw pdu of the status report is in the extended data ("pdu").
     */
    protected abstract void sendText(String destAddr, String scAddr, String text, 
            int destPort,PendingIntent sentIntent, PendingIntent deliveryIntent);

    /**
     * Send a multi-part text based SMS to a specified application port.
     *
     * @param destAddr the address to send the message to
     * @param scAddr is the service center address or null to use
     *   the current default SMSC
     * @param parts an <code>ArrayList</code> of strings that, in order,
     *   comprise the original message
     * @param destPort the port to deliver the message to
     * @param sentIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been sent.
     *   The result code will be <code>Activity.RESULT_OK<code> for success,
     *   or one of these errors:
     *   <code>RESULT_ERROR_GENERIC_FAILURE</code>
     *   <code>RESULT_ERROR_RADIO_OFF</code>
     *   <code>RESULT_ERROR_NULL_PDU</code>.
     *  The per-application based SMS control checks sentIntent. If sentIntent
     *  is NULL the caller will be checked against all unknown applicaitons,
     *  which cause smaller number of SMS to be sent in checking period.
     * @param deliveryIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been delivered
     *   to the recipient.  The raw pdu of the status report is in the
     *   extended data ("pdu").
     */
    protected abstract void sendMultipartText(String destAddr, String scAddr,
            ArrayList<String> parts, int destPort, ArrayList<PendingIntent> sentIntents,
            ArrayList<PendingIntent> deliveryIntents);

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
    abstract public int copyTextMessageToIccCard(
            String scAddress, String address, List<String> text,
            int status, long timestamp);

    public void notifyLastIncomingSms(int result) {
        // broadcast SMS_REJECTED_ACTION intent
        Intent intent = new Intent(Intents.SMS_REJECTED_ACTION);
        intent.putExtra("result", result);
        intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);
        mWakeLock.acquire(WAKE_LOCK_TIMEOUT);
        mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
    }

    private void notifySmsReady(boolean isReady) {
        // broadcast SMS_STATE_CHANGED_ACTION intent
        Intent intent = new Intent(Intents.SMS_STATE_CHANGED_ACTION);
        intent.putExtra("ready", isReady);
        intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);
        mWakeLock.acquire(WAKE_LOCK_TIMEOUT);
        mContext.sendBroadcast(intent);
    }

/**
     * Check if a SmsTracker holds multi-part Sms
     *
     * @param tracker a SmsTracker could hold a multi-part Sms
     * @return true for tracker holds Multi-parts Sms
     */
    private boolean isMultipartTracker (SmsTracker tracker) {
        HashMap map = tracker.mData;
        return ( map.get("parts") != null);
    }

    protected boolean allowDispatchWapPush(final int slotId) {
        Telephony.SIMInfo simInfo = null;
        if(slotId == Phone.GEMINI_SIM_2) {
            simInfo = Telephony.SIMInfo.getSIMInfoBySlot(mContext, Phone.GEMINI_SIM_2);
            if(simInfo == null) {
                Log.d(TAG, "null SIMInfo for SIM 2");
                return false;
            }
            Log.d(TAG, "SIM_2 wap push status: " + (simInfo.mWapPush == Telephony.SimInfo.WAP_PUSH_ENABLE));
            return (simInfo.mWapPush == Telephony.SimInfo.WAP_PUSH_ENABLE);
        } else {
            simInfo = Telephony.SIMInfo.getSIMInfoBySlot(mContext, Phone.GEMINI_SIM_1);
            if(simInfo == null) {
                Log.d(TAG, "null SIMInfo for SIM 1");
                return false;
            }
            Log.d(TAG, "SIM_1 wap push status: " + (simInfo.mWapPush == Telephony.SimInfo.WAP_PUSH_ENABLE));
            return (simInfo.mWapPush == Telephony.SimInfo.WAP_PUSH_ENABLE);
        }
    }
    
    /*
    *  need notify user to set auto push setting
    *  if return false, or don't do notification
    */
    protected boolean checkWapPushSettingStatus() {
        boolean isSim1Inserted = false;
        boolean isSim2Inserted = false;
        boolean isSim1BeSetDone = false;
        boolean isSim2BeSetDone = false;
        Telephony.SIMInfo simInfo = null;
        // check SIM 1
        simInfo = Telephony.SIMInfo.getSIMInfoBySlot(mContext, Phone.GEMINI_SIM_1);
        if(simInfo == null) {
            Log.d(TAG, "null SIMInfo for SIM 1");
        } else {
            isSim1Inserted = (simInfo.mSlot != Telephony.SimInfo.SLOT_NONE);
            if(isSim1Inserted == true) {
                Log.d(TAG, "SIM 1 wap push setting status " + simInfo.mWapPush);
                isSim1BeSetDone = (simInfo.mWapPush != Telephony.SimInfo.WAP_PUSH_DEFAULT);
                Log.d(TAG, "SIM 1 is set done " + isSim1BeSetDone);
            }
        }
        // check SIM 2
        simInfo = Telephony.SIMInfo.getSIMInfoBySlot(mContext, Phone.GEMINI_SIM_2);
        if(simInfo == null) {
            Log.d(TAG, "null SIMInfo for SIM 2");
        } else {
            isSim2Inserted = (simInfo.mSlot != Telephony.SimInfo.SLOT_NONE);
            if(isSim2Inserted == true) {
                Log.d(TAG, "SIM 2 wap push setting status " + simInfo.mWapPush);
                isSim2BeSetDone = (simInfo.mWapPush != Telephony.SimInfo.WAP_PUSH_DEFAULT);
                Log.d(TAG, "SIM 2 is set done " + isSim2BeSetDone);
            }
        }
        
        if(isSim1Inserted == false && isSim2Inserted == false) {
            return true;
        } else if(isSim1Inserted == false && isSim2Inserted == true) {
            return isSim2BeSetDone;
        } else if(isSim1Inserted == true && isSim2Inserted == false) {
            return isSim1BeSetDone;
        } else {
            return isSim1BeSetDone && isSim2BeSetDone;
        }
    }
    
    /*
    *  show a notification to notify user.
    *  the phone will start a activity WapPushSettings
    *  if the user click the notification
    */
    protected void notifyForUnsetSim() {
        NotificationManager notiMgr = (NotificationManager)mContext.
                getSystemService(Context.NOTIFICATION_SERVICE);
        if(notiMgr == null) {
            return;
        }
        
        Resources r = Resources.getSystem();
        int iconId = android.R.drawable.stat_sys_warning;
        String tickerText = r.getString(com.mediatek.internal.R.string.wap_push_setting_noti_ticker);
        String contentTitle = r.getString(com.mediatek.internal.R.string.wap_push_setting_noti_title);
        String contentText = r.getString(com.mediatek.internal.R.string.wap_push_setting_noti_text);
        long when = System.currentTimeMillis();
        //Intent notiIntent = new Intent();
        //notiIntent.setClassName("com.android.settings", "com.android.settings.gemini.WapPushSettings");
        // to make sure only one activity will be launched
        Intent notiIntent = Intent.makeRestartActivityTask(new ComponentName("com.android.settings", 
                "com.android.settings.gemini.WapPushSettings"));
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notiIntent, 0);
        
        Notification noti = new Notification(iconId, tickerText, when);
        noti.defaults = Notification.DEFAULT_SOUND;
        noti.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);
        
        Log.d(TAG, "show wap push noti");
        notiMgr.notify(WAP_PUSH_NOTI_ID, noti);
    }
    
    protected boolean isMmsWapPush(byte[] pdu) {
        int index = 0;
        // skip transaction id
        index++;
        int pduType = pdu[index++] & 0xff;
        
        if((pduType != WspTypeDecoder.PDU_TYPE_PUSH) &&
            (pduType != WspTypeDecoder.PDU_TYPE_CONFIRMED_PUSH)) {
            Log.d(TAG, "isMmsWapPush: non wap push pdu. Type = " + pduType);
            return false;
        }
        
        WspTypeDecoder pduDecoder = new WspTypeDecoder(pdu);
        
        if(pduDecoder.decodeUintvarInteger(index) == false) {
            Log.d(TAG, "isMmsWapPush: header length error");
            return false;
        }
        int headerLength = (int)pduDecoder.getValue32();
        index += pduDecoder.getDecodedDataLength();
        
        if(pduDecoder.decodeContentType(index) == false) {
            Log.d(TAG, "isMmsWapPush: header content type error");
            return false;
        } else {
            String mimeType = pduDecoder.getValueString();
            Log.d(TAG, "isMmsWapPush: mimeType = " + mimeType);
            boolean isMms = false;
            if(mimeType != null) {
                isMms = mimeType.equals(WspTypeDecoder.CONTENT_MIME_TYPE_B_MMS);
                Log.d(TAG, "isMmsWapPush: is MMS? " + isMms);
                return isMms;
            } else {
                int binaryContentType = (int)pduDecoder.getValue32();
                isMms = (binaryContentType == WspTypeDecoder.CONTENT_TYPE_B_MMS);
                Log.d(TAG, "isMmsWapPush: is MMS? " + isMms);
                return isMms;
            }
        }
    }
    
    protected boolean isCuVersion() {
        String opName = SystemProperties.get("ro.operator.optr");
        Log.d(TAG, "operator name is " + opName);
        return opName.equals("OP02");
    }

    /**
     * Set the memory storage status of the SMS
     * This function is used for FTA test only
     * 
     * @param status false for storage full, true for storage available
     *
     */
    protected void setSmsMemoryStatus(boolean status) {
        if (status != mStorageAvailable)
        {
            mStorageAvailable = status;
            mCm.reportSmsMemoryStatus(status, null);
        }
    }

    protected boolean isSmsReady() {
        return mSmsReady;
    }
    // MTK-END [ALPS000xxxxx] MTK code port to ICS added by mtk80589 in 2011.11.16
    
    // MTK-START [ALPS00094531] Orange feature SMS Encoding Type Setting by mtk80589 in 2011.11.22
    /**
     * Send an SMS with specified encoding type.
     *
     * @param destAddr the address to send the message to
     * @param scAddr the SMSC to send the message through, or NULL for the
     *  default SMSC
     * @param text the body of the message to send
     * @param encodingType the encoding type of content of message(GSM 7-bit, Unicode or Automatic)
     * @param sentIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is sucessfully sent, or failed.
     *  The result code will be <code>Activity.RESULT_OK<code> for success,
     *  or one of these errors:<br>
     *  <code>RESULT_ERROR_GENERIC_FAILURE</code><br>
     *  <code>RESULT_ERROR_RADIO_OFF</code><br>
     *  <code>RESULT_ERROR_NULL_PDU</code><br>
     *  For <code>RESULT_ERROR_GENERIC_FAILURE</code> the sentIntent may include
     *  the extra "errorCode" containing a radio technology specific value,
     *  generally only useful for troubleshooting.<br>
     *  The per-application based SMS control checks sentIntent. If sentIntent
     *  is NULL the caller will be checked against all unknown applications,
     *  which cause smaller number of SMS to be sent in checking period.
     * @param deliveryIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is delivered to the recipient.  The
     *  raw pdu of the status report is in the extended data ("pdu").
     */
    abstract protected void sendTextWithEncodingType(
            String destAddr,
            String scAddr,
            String text,
            int encodingType,
            PendingIntent sentIntent, 
            PendingIntent deliveryIntent);

    /**
     * Send a multi-part text based SMS with specified encoding type.
     *
     * @param destAddr the address to send the message to
     * @param scAddr is the service center address or null to use
     *   the current default SMSC
     * @param parts an <code>ArrayList</code> of strings that, in order,
     *   comprise the original message
     * @param encodingType the encoding type of content of message(GSM 7-bit, Unicode or Automatic)
     * @param sentIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been sent.
     *   The result code will be <code>Activity.RESULT_OK<code> for success,
     *   or one of these errors:
     *   <code>RESULT_ERROR_GENERIC_FAILURE</code>
     *   <code>RESULT_ERROR_RADIO_OFF</code>
     *   <code>RESULT_ERROR_NULL_PDU</code>.
     * @param deliveryIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been delivered
     *   to the recipient.  The raw pdu of the status report is in the
     *   extended data ("pdu").
     */
    abstract protected void sendMultipartTextWithEncodingType(
            String destAddr,
            String scAddr,
            ArrayList<String> parts,
            int encodingType,
            ArrayList<PendingIntent> sentIntents,
            ArrayList<PendingIntent> deliveryIntents);
    // MTK-END [ALPS00094531] Orange feature SMS Encoding Type Setting by mtk80589 in 2011.11.22
    
    /**
     * Send an SMS with specified encoding type.
     *
     * @param destAddr the address to send the message to
     * @param scAddr the SMSC to send the message through, or NULL for the
     *  default SMSC
     * @param text the body of the message to send
     * @param extraParams extra parameters, such as validity period, encoding type
     * @param sentIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is sucessfully sent, or failed.
     * @param deliveryIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is delivered to the recipient.  The
     *  raw pdu of the status report is in the extended data ("pdu").
     */
    abstract public void sendTextWithExtraParams(
            String destAddr,
            String scAddr,
            String text,
            Bundle extraParams,
            PendingIntent sentIntent,
            PendingIntent deliveryIntent);

	/**
     * Send a multi-part text based SMS with specified encoding type.
     *
     * @param destAddr the address to send the message to
     * @param scAddr is the service center address or null to use
     *   the current default SMSC
     * @param parts an <code>ArrayList</code> of strings that, in order,
     *   comprise the original message
     * @param extraParams extra parameters, such as validity period, encoding type
     * @param sentIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been sent.
     * @param deliveryIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been delivered
     *   to the recipient.  The raw pdu of the status report is in the
     *   extended data ("pdu").
     */
    abstract public void sendMultipartTextWithExtraParams(
            String destAddr,
            String scAddr,
            ArrayList<String> parts,
            Bundle extraParams,
            ArrayList<PendingIntent> sentIntents,
            ArrayList<PendingIntent> deliveryIntents);

    protected void dispatchMwiMessage(SmsMessageBase sms) {
        Log.d(TAG, "broadcast intent for MWI message");
        byte[][] pdus = new byte[1][];
        pdus[0] = sms.getPdu();
        
        Intent intent = new Intent(Intents.MWI_SMS_RECEIVED_ACTION);
        intent.putExtra("pdus", pdus);
        intent.putExtra("format", getFormat());
        dispatch(intent, RECEIVE_SMS_PERMISSION);
    }

    abstract protected SmsMessage createMessageFromSubmitPdu(byte[] smsc, byte[] tpdu);
}
