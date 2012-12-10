package com.android.mms.transaction;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.telephony.PhoneNumberUtils;
import android.provider.Telephony.SIMInfo;
import android.provider.Telephony.Sms;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import com.android.mms.data.Conversation;
import com.android.mms.LogTag;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;
// add for gemini
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.telephony.gemini.GeminiSmsManager;
import com.android.internal.telephony.Phone;
import com.android.mms.MmsApp;
import com.android.mms.ui.MessagingPreferenceActivity;
import com.google.android.mms.MmsException;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;
import com.mediatek.xlog.SXlog;


public class SmsSingleRecipientSender extends SmsMessageSender {

    private final boolean mRequestDeliveryReport;
    private String mDest;
    private Uri mUri;
    private static final String TAG = "SmsSingleRecipientSender";

    public SmsSingleRecipientSender(Context context, String dest, String msgText, long threadId,
            boolean requestDeliveryReport, Uri uri) {
        super(context, null, msgText, threadId);
        mRequestDeliveryReport = requestDeliveryReport;
        mDest = dest;
        mUri = uri;
    }

    // add for gemini
    public SmsSingleRecipientSender(Context context, String dest, String msgText, long threadId,
            boolean requestDeliveryReport, Uri uri, int simId) {
        super(context, null, msgText, threadId, simId);
        mRequestDeliveryReport = requestDeliveryReport;
        mDest = dest;
        mUri = uri;
    }

    public boolean sendMessage(long token) throws MmsException {
        if (LogTag.DEBUG_SEND) {
            Log.v(TAG, "sendMessage token: " + token);
        }
        Xlog.d(MmsApp.TXN_TAG, "SmsSingleRecipientSender: sendMessage()");
        if (mMessageText == null) {
            // Don't try to send an empty message, and destination should be just
            // one.
            throw new MmsException("Null message body or have multiple destinations.");
        }

        int codingType = SmsMessage.ENCODING_UNKNOWN;
        String optr = SystemProperties.get("ro.operator.optr");
        //MTK_OP03_PROTECT_START
        if ("OP03".equals(optr)) {
            codingType = MessageUtils.getSmsEncodingType(mContext);
        }
        //MTK_OP03_PROTECT_END

        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> messages = null;
        if ((MmsConfig.getEmailGateway() != null) &&
                (Mms.isEmailAddress(mDest) || MessageUtils.isAlias(mDest))) {
            String msgText;
            msgText = mDest + " " + mMessageText;
            mDest = MmsConfig.getEmailGateway();
            //MTK_OP03_PROTECT_START
            if ("OP03".equals(optr)) {
                messages = smsManager.divideMessage(msgText, codingType);
            } else {
            //MTK_OP03_PROTECT_END
                messages = smsManager.divideMessage(msgText);
            //MTK_OP03_PROTECT_START
            }
            //MTK_OP03_PROTECT_END
        } else {
            //MTK_OP03_PROTECT_START
            if ("OP03".equals(optr)) {
                messages = smsManager.divideMessage(mMessageText, codingType);
            } else {
            //MTK_OP03_PROTECT_END
                messages = smsManager.divideMessage(mMessageText);
            //MTK_OP03_PROTECT_START
            }
            //MTK_OP03_PROTECT_END
            

            // remove spaces and dashes from destination number
            // (e.g. "801 555 1212" -> "8015551212")
            // (e.g. "+8211-123-4567" -> "+82111234567")
            //mDest = PhoneNumberUtils.stripSeparators(mDest);
            // remove spaces from destination number (e.g. "801 555 1212" -> "8015551212")
            mDest = mDest.replaceAll(" ", "");
            mDest = mDest.replaceAll("-", "");
            mDest = Conversation.verifySingleRecipient(mContext, mThreadId, mDest);
        }
        int messageCount = messages.size();
        Xlog.d(MmsApp.TXN_TAG, "SmsSingleRecipientSender: sendMessage(), Message Count=" + messageCount);

        if (messageCount == 0) {
            // Don't try to send an empty message.
            throw new MmsException("SmsSingleRecipientSender.sendMessage: divideMessage returned " +
                    "empty messages. Original message is \"" + mMessageText + "\"");
        }

        boolean moved = Sms.moveMessageToFolder(mContext, mUri, Sms.MESSAGE_TYPE_OUTBOX, 0);
        if (!moved) {
            throw new MmsException("SmsSingleRecipientSender.sendMessage: couldn't move message " +
                    "to outbox: " + mUri);
        }
        if (LogTag.DEBUG_SEND) {
            Log.v(TAG, "sendMessage mDest: " + mDest + " mRequestDeliveryReport: " +
                    mRequestDeliveryReport);
        }

        ArrayList<PendingIntent> deliveryIntents =  new ArrayList<PendingIntent>(messageCount);
        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(messageCount);
        for (int i = 0; i < messageCount; i++) {
            if (mRequestDeliveryReport && (i == (messageCount - 1))) {
                // TODO: Fix: It should not be necessary to
                // specify the class in this intent.  Doing that
                // unnecessarily limits customizability.
                Intent intent = new Intent(
                                MessageStatusReceiver.MESSAGE_STATUS_RECEIVED_ACTION,
                                mUri,
                                mContext,
                                MessageStatusReceiver.class);

                //the parameter is used now! not as the google doc says "currently not used"
                deliveryIntents.add(PendingIntent.getBroadcast(mContext, i, intent, 0));
            } else {
                deliveryIntents.add(null);
            }
            Intent intent  = new Intent(SmsReceiverService.MESSAGE_SENT_ACTION,
                    mUri,
                    mContext,
                    SmsReceiver.class);
            if (i == messageCount -1) {
                intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_SENT_SEND_NEXT, true);
            }

            // add for concatenation msg
            if (messageCount > 1) {
                intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_CONCATENATION, true);
            }
            if (LogTag.DEBUG_SEND) {
                Log.v(TAG, "SmsSingleRecipientSender sendIntent: " + intent);
            }
            sentIntents.add(PendingIntent.getBroadcast(mContext, i, intent, 0));
        }
        try {
            Xlog.d(MmsApp.TXN_TAG, "\t Destination\t= " + mDest);
            Xlog.d(MmsApp.TXN_TAG, "\t ServiceCenter\t= " + mServiceCenter);
            Xlog.d(MmsApp.TXN_TAG, "\t Message\t= " + messages);
            Xlog.d(MmsApp.TXN_TAG, "\t uri\t= " + mUri);
            Xlog.d(MmsApp.TXN_TAG, "\t CodingType\t= " + codingType);
            //MTK_OP03_PROTECT_START
            if ("OP03".equals(optr)) {
                smsManager.sendMultipartTextMessageWithEncodingType(mDest, mServiceCenter, messages, 
                    codingType, sentIntents, deliveryIntents);
            } else {
            //MTK_OP03_PROTECT_END
                smsManager.sendMultipartTextMessage(mDest, mServiceCenter, messages, sentIntents, deliveryIntents);
            //MTK_OP03_PROTECT_START
            }
            //MTK_OP03_PROTECT_END
            
        } catch (Exception ex) {
            Log.e(TAG, "SmsSingleRecipientSender.sendMessage: caught", ex);
            throw new MmsException("SmsSingleRecipientSender.sendMessage: caught " + ex +
                    " from SmsManager.sendTextMessage()");
        }
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE) || LogTag.DEBUG_SEND) {
            log("SmsSingleRecipientSender: address=" + mDest + ", threadId=" + mThreadId +
                    ", uri=" + mUri + ", msgs.count=" + messageCount);
        }
        return false;
    }

    // add for gemini
    public boolean sendMessageGemini(long token, int simId) throws MmsException {
        // convert sim id to slot id
        int slotId = SIMInfo.getSlotById(mContext, mSimId);
        Xlog.d(MmsApp.TXN_TAG, "SmsSingleRecipientSender: sendMessageGemini() simId=" + simId +"slotId=" + slotId);
        if (mMessageText == null) {
            // Don't try to send an empty message, and destination should be just
            // one.
            throw new MmsException("Null message body or have multiple destinations.");
        }
        
        int codingType = SmsMessage.ENCODING_UNKNOWN;
        String optr = SystemProperties.get("ro.operator.optr");
        //MTK_OP03_PROTECT_START
        if ("OP03".equals(optr)) {
            codingType = MessageUtils.getSmsEncodingType(mContext);
        }
        //MTK_OP03_PROTECT_END
        
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> messages = null;
        if ((MmsConfig.getEmailGateway() != null) &&
                (Mms.isEmailAddress(mDest) || MessageUtils.isAlias(mDest))) {
            String msgText;
            msgText = mDest + " " + mMessageText;
            mDest = MmsConfig.getEmailGateway();
            //MTK_OP03_PROTECT_START
            if ("OP03".equals(optr)) {
                messages = smsManager.divideMessage(msgText, codingType);
            } else {
            //MTK_OP03_PROTECT_END
                messages = smsManager.divideMessage(msgText);
            //MTK_OP03_PROTECT_START
            }
            //MTK_OP03_PROTECT_END
        } else {
            //MTK_OP03_PROTECT_START
            if ("OP03".equals(optr)) {
                messages = smsManager.divideMessage(mMessageText, codingType);
            } else {
            //MTK_OP03_PROTECT_END
                messages = smsManager.divideMessage(mMessageText);
            //MTK_OP03_PROTECT_START
            }
            //MTK_OP03_PROTECT_END
           
            // remove spaces from destination number (e.g. "801 555 1212" -> "8015551212")
            mDest = mDest.replaceAll(" ", "");
            mDest = mDest.replaceAll("-", "");
            mDest = Conversation.verifySingleRecipient(mContext, mThreadId, mDest);
        }
        int messageCount = messages.size();
        Xlog.d(MmsApp.TXN_TAG, "SmsSingleRecipientSender: sendMessageGemini(), Message Count=" + messageCount);

        if (messageCount == 0) {
            // Don't try to send an empty message.
            throw new MmsException("SmsSingleRecipientSender.sendMessageGemini: divideMessage returned " +
                    "empty messages. Original message is \"" + mMessageText + "\"");
        }

        boolean moved = Sms.moveMessageToFolder(mContext, mUri, Sms.MESSAGE_TYPE_OUTBOX, 0);
        if (!moved) {
            throw new MmsException("SmsSingleRecipientSender.sendMessageGemini: couldn't move message " +
                    "to outbox: " + mUri);
        }

        ArrayList<PendingIntent> deliveryIntents =  new ArrayList<PendingIntent>(messageCount);
        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(messageCount);
        for (int i = 0; i < messageCount; i++) {
            if (mRequestDeliveryReport && (i == (messageCount - 1))) {
                // TODO: Fix: It should not be necessary to
                // specify the class in this intent.  Doing that
                // unnecessarily limits customizability.
                Intent drIt = new Intent(
                                MessageStatusReceiver.MESSAGE_STATUS_RECEIVED_ACTION,
                                mUri,
                                mContext,
                                MessageStatusReceiver.class);
                drIt.putExtra(Phone.GEMINI_SIM_ID_KEY, slotId/*mSimId*/);
                //the parameter is used now! not as the google doc says "currently not used"                
                deliveryIntents.add(PendingIntent.getBroadcast(mContext, i, drIt, 0));
            } else {
                deliveryIntents.add(null);
            }
            Intent intent  = new Intent(SmsReceiverService.MESSAGE_SENT_ACTION,
                    mUri,
                    mContext,
                    SmsReceiver.class);
            if (i == messageCount -1) {
                intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_SENT_SEND_NEXT, true);
            }
            
            // add for concatenation msg
            if (messageCount > 1) {
                intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_CONCATENATION, true);
            }
            
            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, slotId/*mSimId*/);
            sentIntents.add(PendingIntent.getBroadcast(mContext, i, intent, 0));
        }
        try {
            Xlog.d(MmsApp.TXN_TAG, "\t Destination\t= " + mDest);
            Xlog.d(MmsApp.TXN_TAG, "\t ServiceCenter\t= " + mServiceCenter);
            Xlog.d(MmsApp.TXN_TAG, "\t Message\t= " + messages);
            Xlog.d(MmsApp.TXN_TAG, "\t uri\t= " + mUri);
            Xlog.d(MmsApp.TXN_TAG, "\t slotId\t= "+ slotId/*mSimId*/);
            Xlog.d(MmsApp.TXN_TAG, "\t CodingType\t= " + codingType);
            //MTK_OP03_PROTECT_START
            if ("OP03".equals(optr)) {
                GeminiSmsManager.sendMultipartTextMessageWithEncodingTypeGemini(mDest, mServiceCenter, messages, 
                    codingType, slotId/*mSimId*/, sentIntents, deliveryIntents);
            } else {
            //MTK_OP03_PROTECT_END
                //MTK_OP01_PROTECT_START
                if ("OP01".equals(optr)) {
                    SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(mContext);
                    final String validityKey = Long.toString(slotId) + "_" + MessagingPreferenceActivity.SMS_VALIDITY_PERIOD;
                    int vailidity = spref.getInt(validityKey, SmsManager.VALIDITY_PERIOD_NO_DURATION);
                    Bundle extra = new Bundle();
                    extra.putInt(SmsManager.EXTRA_PARAMS_VALIDITY_PERIOD, vailidity);
                    GeminiSmsManager.sendMultipartTextMessageWithExtraParamsGemini(mDest, mServiceCenter, messages, extra, slotId, sentIntents, deliveryIntents);
                }
                else
                //MTK_OP01_PROTECT_END
                {
                GeminiSmsManager.sendMultipartTextMessageGemini(mDest, mServiceCenter, messages, slotId/*mSimId*/, sentIntents, deliveryIntents);
                    }
            //MTK_OP03_PROTECT_START
            }
            //MTK_OP03_PROTECT_END
            Xlog.d(MmsApp.TXN_TAG, "\t after sendMultipartTextMessageGemini");
        } catch (Exception ex) {
            throw new MmsException("SmsSingleRecipientSender.sendMessageGemini: caught " + ex +
                    " from SmsManager.sendTextMessage()");
        }
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            log("SmsSingleRecipientSender:sendMessageGemini: address=" + mDest + ", threadId=" + mThreadId +
                    ", uri=" + mUri + ", msgs.count=" + messageCount);
        }
        return false;
    }


    private void log(String msg) {
        Log.d(LogTag.TAG, "[SmsSingleRecipientSender] " + msg);
    }
}
