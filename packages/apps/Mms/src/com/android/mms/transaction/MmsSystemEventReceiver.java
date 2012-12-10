/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

package com.android.mms.transaction;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.SIMInfo;
import android.util.Log;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.android.mms.LogTag;
import com.google.android.mms.util.PduCache;
// add for gemini
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.database.sqlite.SQLiteDiskIOException;
import android.provider.Telephony.Mms;
import android.telephony.SmsManager;
import android.telephony.gemini.GeminiSmsManager;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.MessageUtils;
//import com.android.mms.ui.MultiDeleteActivity;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.Recycler;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPersister;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;
import com.mediatek.xlog.SXlog;

import android.provider.Telephony.Sms;
import android.content.ContentResolver;
import android.content.ContentValues;

/**
 * MmsSystemEventReceiver receives the
 * {@link android.content.intent.ACTION_BOOT_COMPLETED},
 * {@link com.android.internal.telephony.TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED}
 * and performs a series of operations which may include:
 * <ul>
 * <li>Show/hide the icon in notification area which is used to indicate
 * whether there is new incoming message.</li>
 * <li>Resend the MM's in the outbox.</li>
 * </ul>
 */
public class MmsSystemEventReceiver extends BroadcastReceiver {
    private static final String TAG = "MmsSystemEventReceiver";
    private static MmsSystemEventReceiver sMmsSystemEventReceiver;
    private OnShutDownListener saveDraft;
    private OnSimInforChangedListener mSimInforChangedListener;

    private static void wakeUpService(Context context) {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "wakeUpService: start transaction service ...");
        }

        context.startService(new Intent(context, TransactionService.class));
    }

    // add for gemini
    private static void wakeUpServiceGemini(Context context, int simId) {
        Xlog.v(MmsApp.TXN_TAG, "wakeUpServiceGemini: start transaction service ... simId=" + simId);
        
        Intent it = new Intent(context, TransactionService.class);
        it.putExtra(Phone.GEMINI_SIM_ID_KEY, simId);

        context.startService(it);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "Intent received: " + intent);
        }
        Xlog.d(MmsApp.LOG_TAG, "onReceive(): intent=" + intent.toString());
        String action = intent.getAction();
        if (action.equals(Mms.Intents.CONTENT_CHANGED_ACTION)) {
            final Intent mIntent = intent;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Uri changed = (Uri) mIntent.getParcelableExtra(Mms.Intents.DELETED_CONTENTS);
                    PduCache.getInstance().purge(changed);
                    Xlog.d(MmsApp.TXN_TAG, "Mms.Intents.CONTENT_CHANGED_ACTION: " + changed);
                }
            }).start();
        } else if (action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
            String state = intent.getStringExtra(Phone.STATE_KEY);

            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "ANY_DATA_STATE event received: " + state);
            }

            String apnType = intent.getStringExtra(Phone.DATA_APN_TYPE_KEY);

            //if (state.equals("CONNECTED")) {
            if (Phone.APN_TYPE_MMS.equals(apnType)) {
                Xlog.d(MmsApp.TXN_TAG, "TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED, type is mms.");
                // if the network is not available for mms, keep listening
                ConnectivityManager ConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = ConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
                if (ni != null && !ni.isAvailable()) {
                    Xlog.d(MmsApp.TXN_TAG, "network is not available for mms, keep listening.");
                    return;
                }
                
                unRegisterForConnectionStateChanges(context);
                // add for gemini
                if(FeatureOption.MTK_GEMINI_SUPPORT == true){
                    // conver slot id to sim id
                    SIMInfo si = SIMInfo.getSIMInfoBySlot(context, intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY,Phone.GEMINI_SIM_1));
                    if (null == si) {
                        Xlog.e(MmsApp.TXN_TAG, "System event receiver: SIMInfo is null for slot " + intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1));
                        return;
                    }
                    int simId = (int)si.mSimId;
                    wakeUpServiceGemini(context, simId/*intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY,Phone.GEMINI_SIM_1)*/);
                } else{
                    wakeUpService(context);
                }
            }
        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Xlog.d(MmsApp.TXN_TAG, "Intent.ACTION_BOOT_COMPLETED");
            final Context contxt = context;
            new Thread(new Runnable() {
                public void run() {
                    setPendingMmsFailed(contxt);
                    setPendingSmsFailed(contxt);
                    setNotificationIndUnstarted(contxt);
                }
            }).start();
            // We should check whether there are unread incoming
            // messages in the Inbox and then update the notification icon.
            // Called on the UI thread so don't block.
            MessagingNotification.nonBlockingUpdateNewMessageIndicator(context, false, false);
        } else if (action.equals(Intent.SIM_SETTINGS_INFO_CHANGED)) {
            int simId = (int)intent.getLongExtra("simid", -1);
            MessageUtils.simInfoMap.remove(simId);
            MessageUtils.getSimInfo(context, simId);
        } else if (action.equals(Intent.ACTION_SHUTDOWN)) {
            saveDraft = (OnShutDownListener) ComposeMessageActivity.getComposeContext();
            if (saveDraft != null) {
                saveDraft.onShutDown();
            }
        } else if (action.equals(Intent.ACTION_SMS_DEFAULT_SIM_CHANGED)) {
            Xlog.d(MmsApp.LOG_TAG, "SMS default SIM changed.");
            mSimInforChangedListener = (OnSimInforChangedListener) ComposeMessageActivity.getComposeContext();
            if (mSimInforChangedListener != null) {
                mSimInforChangedListener.OnSimInforChanged();
            }
            mSimInforChangedListener = (OnSimInforChangedListener) ConversationList.getContext();
            if (mSimInforChangedListener != null) {
                mSimInforChangedListener.OnSimInforChanged();
            }
        } else if (action.equals(Intent.ACTION_DEVICE_STORAGE_FULL)) {
            MmsConfig.setDeviceStorageFullStatus(true);
        } else if (action.equals(Intent.ACTION_DEVICE_STORAGE_NOT_FULL)) {
            MmsConfig.setDeviceStorageFullStatus(false);
            MessagingNotification.cancelNotification(context,
                    SmsRejectedReceiver.SMS_REJECTED_NOTIFICATION_ID);
        }
    }
     
    
    private boolean isIncomingMessage(int  messageStatus) {
        return (messageStatus == SmsManager.STATUS_ON_ICC_READ) ||
               (messageStatus == SmsManager.STATUS_ON_ICC_UNREAD);
    }

    public interface OnShutDownListener {
        void onShutDown();
    }
    
    public static void setPendingMmsFailed(final Context context) {
        Xlog.d(MmsApp.TXN_TAG, "setPendingMmsFailed");
        Cursor cursor = PduPersister.getPduPersister(context).getPendingMessages(
                Long.MAX_VALUE/*System.currentTimeMillis()*/);
        if (cursor != null) {
            try {
                int count = cursor.getCount();
                Xlog.d(MmsApp.TXN_TAG, "setPendingMmsFailed: Pending Message Size=" + count);

                if (count == 0 ) {
                    return;
                }
                DefaultRetryScheme scheme = new DefaultRetryScheme(context, 100);
                ContentValues values = null;
                int columnIndex = 0;
                int columnType = 0;
                int id = 0;
                int type = 0;
                while (cursor.moveToNext()) {
                    columnIndex = cursor.getColumnIndexOrThrow(PendingMessages._ID);
                    id = cursor.getInt(columnIndex);

                    columnType = cursor.getColumnIndexOrThrow(PendingMessages.MSG_TYPE);
                    type = cursor.getInt(columnType);

                    Xlog.d(MmsApp.TXN_TAG, "setPendingMmsFailed: type=" + type + "; MsgId=" + id);

                    if (type == PduHeaders.MESSAGE_TYPE_SEND_REQ) {
                        values = new ContentValues(2);
                        values.put(PendingMessages.ERROR_TYPE,  MmsSms.ERR_TYPE_GENERIC_PERMANENT);
                        values.put(PendingMessages.RETRY_INDEX, scheme.getRetryLimit());
                        SqliteWrapper.update(context, 
                                context.getContentResolver(),
                                PendingMessages.CONTENT_URI,
                                values, PendingMessages._ID + "=" + id, null);
                    }
                }
            } catch (SQLiteDiskIOException e) {
                // Ignore
                Xlog.e(MmsApp.TXN_TAG, "SQLiteDiskIOException caught while set pending message failed", e);
            } finally {
                cursor.close();
            }
        } else {
            Xlog.d(MmsApp.TXN_TAG, "setPendingMmsFailed: no pending MMS.");
        }
    }

    public static void setPendingSmsFailed(final Context context) {
        Xlog.d(MmsApp.TXN_TAG, "setPendingSmsFailed");
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), 
                Sms.CONTENT_URI,
                new String[] {Sms._ID}, 
                Sms.TYPE + "=" + Sms.MESSAGE_TYPE_OUTBOX + " OR " + Sms.TYPE + "=" + Sms.MESSAGE_TYPE_QUEUED, 
                null, null);
        if (cursor != null) {
            try {
                int count = cursor.getCount();
                Xlog.d(MmsApp.TXN_TAG, "setPendingSmsFailed: Message Size=" + count);

                if (count == 0 ) {
                    return;
                }

                ContentValues values = null;
                int id = 0;
                while (cursor.moveToNext()) {
                    id = cursor.getInt(0);
                    Xlog.d(MmsApp.TXN_TAG, "setPendingSmsFailed: MsgId=" + id);
                    values = new ContentValues(1);
                        values.put(Sms.TYPE, Sms.MESSAGE_TYPE_FAILED);
                        SqliteWrapper.update(context, 
                                context.getContentResolver(),
                                Sms.CONTENT_URI,
                                values, Sms._ID + "=" + id, null);
                }
            } catch (SQLiteDiskIOException e) {
                Xlog.e(MmsApp.TXN_TAG, "SQLiteDiskIOException caught while set sms failed", e);
            } finally {
                cursor.close();
            }
        } else {
            Xlog.d(MmsApp.TXN_TAG, "setPendingSmsFailed: no pending messages.");
        }
    }

    public static void setNotificationIndUnstarted(final Context context) {
        Xlog.d(MmsApp.TXN_TAG, "setNotificationIndUnstarted");
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),Mms.CONTENT_URI,
                new String[] {Mms._ID,Mms.STATUS}, Mms.MESSAGE_TYPE + "=" + PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND, null, null);
        if (cursor != null) {
            try {
                int count = cursor.getCount();
                Xlog.d(MmsApp.TXN_TAG, "setNotificationIndUnstarted: Message Size=" + count);

                if (count == 0 ) {
                    return;
                }

                ContentValues values = null;
                int id = 0;
                int status = 0;
                while (cursor.moveToNext()) {
                    id = cursor.getInt(0);
                    status = cursor.getInt(1);
                    Xlog.d(MmsApp.TXN_TAG, "setNotificationIndUnstarted: MsgId=" + id + "; status=" + status);

                    if (DownloadManager.STATE_DOWNLOADING == (status &~ DownloadManager.DEFERRED_MASK)) {
                        values = new ContentValues(1);
                        values.put(Mms.STATUS,  PduHeaders.STATUS_UNRECOGNIZED);
                        SqliteWrapper.update(context, 
                                context.getContentResolver(),
                                Mms.CONTENT_URI,
                                values, Mms._ID + "=" + id, null);
                    }
                }
            } catch (SQLiteDiskIOException e) {
                // Ignore
                Xlog.e(MmsApp.TXN_TAG, "SQLiteDiskIOException caught while set notification ind unstart", e);
            } finally {
                cursor.close();
            }
        } else {
            Xlog.d(MmsApp.TXN_TAG, "setNotificationIndUnstarted: no pending messages.");
        }
    }

    public static void registerForConnectionStateChanges(Context context) {
        Xlog.d(MmsApp.TXN_TAG, "registerForConnectionStateChanges");
        unRegisterForConnectionStateChanges(context);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "registerForConnectionStateChanges");
        }
        if (sMmsSystemEventReceiver == null) {
            sMmsSystemEventReceiver = new MmsSystemEventReceiver();
        }

        context.registerReceiver(sMmsSystemEventReceiver, intentFilter);
    }

    public static void unRegisterForConnectionStateChanges(Context context) {
        Xlog.d(MmsApp.TXN_TAG, "unRegisterForConnectionStateChanges");
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "unRegisterForConnectionStateChanges");
        }
        if (sMmsSystemEventReceiver != null) {
            try {
                context.unregisterReceiver(sMmsSystemEventReceiver);
            } catch (IllegalArgumentException e) {
                // Allow un-matched register-unregister calls
            }
        }
    }

    public interface OnSimInforChangedListener {
        void OnSimInforChanged();
    }
}
