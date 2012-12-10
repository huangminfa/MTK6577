/*
 * Copyright (C) 2007 Esmertec AG.
 * Copyright (C) 2007 The Android Open Source Project
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

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Sms;
import android.telephony.SmsMessage;
import android.util.Log;

import android.database.sqlite.SqliteWrapper;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.mediatek.xlog.Xlog;
import com.mediatek.xlog.SXlog;

public class MessageStatusReceiver extends BroadcastReceiver {
    public static final String MESSAGE_STATUS_RECEIVED_ACTION =
            "com.android.mms.transaction.MessageStatusReceiver.MESSAGE_STATUS_RECEIVED";
    private static final String[] ID_PROJECTION = new String[] { Sms._ID, Sms.STATUS };
    private static final String LOG_TAG = "MessageStatusReceiver";
    private static final Uri STATUS_URI =
            Uri.parse("content://sms/status");
    private static final String MMS_READ_STATE_CHANGE = "MMS_READ_STATE_CHANGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (MESSAGE_STATUS_RECEIVED_ACTION.equals(intent.getAction())) {
            SmsMessage message = updateMessageStatus(context, intent);
            // Called on the UI thread so don't block.
            if (message != null && message.getStatus() <= Sms.STATUS_PENDING){
                MessagingNotification.nonBlockingUpdateNewMessageIndicator(context,
                    true, message.isStatusReportMessage());
            }
        } else if (MMS_READ_STATE_CHANGE.equals(intent.getAction())) {
            // update notification when read state change 
            MessagingNotification.nonBlockingUpdateNewMessageIndicator(context, false, false);
       }
    }

    private SmsMessage updateMessageStatus(Context context, Intent intent) {
        byte[] pdu = (byte[]) intent.getExtra("pdu");
        String format = intent.getStringExtra("format");
        SmsMessage message = SmsMessage.createFromPdu(pdu, format);
        if (message == null) {
            return null;
        }
        Uri messageUri = intent.getData();
        // Create a "status/#" URL and use it to update the
        // message's status in the database.
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                            messageUri, ID_PROJECTION, null, null, null);

        try {
            if (cursor.moveToFirst()) {
                int messageId = cursor.getInt(0);
                int oldStatus = cursor.getInt(1);
                Uri updateUri = ContentUris.withAppendedId(STATUS_URI, messageId);
                int status = message.getStatus();
                boolean isStatusReport = message.isStatusReportMessage();
                ContentValues contentValues = new ContentValues(1);

                if (Log.isLoggable(LogTag.TAG, Log.DEBUG)) {
                    log("updateMessageStatus: msgUrl=" + messageUri + ", status=" + status +
                            ", isStatusReport=" + isStatusReport);
                }
                Xlog.d(MmsApp.TXN_TAG, "updateMessageStatus: msgUrl=" + messageUri 
                    + ", status=" + status + ", isStatusReport=" + isStatusReport);

                if (oldStatus == Sms.STATUS_FAILED) {
                    //if the status is failed already, this means this is a long sms, and 
                    //at least one part of it is sent failed. so the status report of this long sms is failed overall.
                    //don't record a part's status.
                    // but this part success status is will toasted.
                    Xlog.d(MmsApp.TXN_TAG,"the original status is:"+oldStatus);
                } else {
                    contentValues.put(Sms.STATUS, status);
                    SqliteWrapper.update(context, context.getContentResolver(),
                                        updateUri, contentValues, null, null);
                }
                MessagingNotification.sMessageUri = updateUri;
            } else {
                error("Can't find message for status update: " + messageUri);
                Xlog.w(MmsApp.TXN_TAG, "Can't find message for status update: " + messageUri);

            }
        } finally {
            cursor.close();
        }
        return message;
    }

    private void error(String message) {
        Log.e(LOG_TAG, "[MessageStatusReceiver] " + message);
    }

    private void log(String message) {
        Log.d(LOG_TAG, "[MessageStatusReceiver] " + message);
    }
}
