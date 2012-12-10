/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

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

package com.mediatek.telephony.gemini;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.telephony.SmsMessage;
//import android.telephony.SmsManager;
import android.telephony.PhoneNumberUtils;

import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.ISms;
import com.android.internal.telephony.IccConstants;
import com.android.internal.telephony.SmsRawData;
import com.android.internal.telephony.Phone;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages SMS operations such as sending data, text, and pdu SMS messages.
 *
 */
public final class SmsManager {

    private static final String TAG = "SMS";

    private SmsManager() {
        
    }

    /**
     * Divide a message text into several fragments, none bigger than
     * the maximum SMS message size.
     *
     * @param text the original message.  Must not be null.
     * @return an <code>ArrayList</code> of strings that, in order,
     *   comprise the original message 
     */
    public static ArrayList<String> divideMessage(String text) {
        return android.telephony.SmsMessage.fragmentText(text);
    }

    /**
     * Send a text based SMS.
     *
     * @param destinationAddress the address to send the message to
     * @param scAddress is the service center address or null to use
     *  the current default SMSC
     * @param text the body of the message to send
     * @param simId the sim card that user wants to access
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
     *
     * @throws IllegalArgumentException if destinationAddress or text are empty
     */
    public static void sendTextMessage(
            String destinationAddress, String scAddress, String text, int simId,
            PendingIntent sentIntent, PendingIntent deliveryIntent) {

        if (!isValidParameters(destinationAddress, text, sentIntent)) {

            return;
        }

        String isms = getSmsServiceName(simId);
        try {
            ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService(isms));
            if (iccISms != null) {
                iccISms.sendText(destinationAddress, scAddress, text, sentIntent, deliveryIntent);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

    }

    /**
     * Send a text based SMS to a specified application port.
     *
     * @param destinationAddress the address to send the message to
     * @param scAddress is the service center address or null to use
     *  the current default SMSC
     * @param text the body of the message to send
     * @param destinationPort the port to deliver the message to
     * @param simId the sim card that user wants to access
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
     *
     * @throws IllegalArgumentException if destinationAddress or text are empty
     */
    public static void sendTextMessage( 
            String destinationAddress, String scAddress, String text,
            short destinationPort, int simId, PendingIntent sentIntent, 
            PendingIntent deliveryIntent) {

        if (!isValidParameters(destinationAddress, text, sentIntent)) {

            return;
        }

        String isms = getSmsServiceName(simId);
        try {
            ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService(isms));
            if (iccISms != null) {
                iccISms.sendTextWithPort(destinationAddress, scAddress, text, 
                    destinationPort & 0xFFFF, sentIntent, deliveryIntent);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

    }
   
    /**
     * Send a multi-part text based SMS.  The callee should have already
     * divided the message into correctly sized parts by calling
     * <code>divideMessage</code>.
     *
     * @param destinationAddress the address to send the message to
     * @param scAddress is the service center address or null to use
     *   the current default SMSC
     * @param parts an <code>ArrayList</code> of strings that, in order,
     *   comprise the original message
     * @param simId the sim card that user wants to access
     * @param sentIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been sent.
     *   The result code will be <code>Activity.RESULT_OK<code> for success,
     *   or one of these errors:<br>
     *   <code>RESULT_ERROR_GENERIC_FAILURE</code><br>
     *   <code>RESULT_ERROR_RADIO_OFF</code><br>
     *   <code>RESULT_ERROR_NULL_PDU</code><br>
     *   For <code>RESULT_ERROR_GENERIC_FAILURE</code> each sentIntent may include
     *   the extra "errorCode" containing a radio technology specific value,
     *   generally only useful for troubleshooting.<br>
     *   The per-application based SMS control checks sentIntent. If sentIntent
     *   is NULL the caller will be checked against all unknown applicaitons,
     *   which cause smaller number of SMS to be sent in checking period.
     * @param deliveryIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been delivered
     *   to the recipient.  The raw pdu of the status report is in the
     *   extended data ("pdu").
     *
     * @throws IllegalArgumentException if destinationAddress or data are empty
     */
    public static void sendMultipartTextMessage(
            String destinationAddress, String scAddress, ArrayList<String> parts, int simId,
            ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {

        if (!isValidParameters(destinationAddress, parts, sentIntents)) {

            return;
        }

        String isms = getSmsServiceName(simId);
        if (parts.size() > 1) {
            try {
                ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService(isms));
                if (iccISms != null) {
                    iccISms.sendMultipartText(destinationAddress, scAddress, parts,
                            sentIntents, deliveryIntents);
                }
            } catch (RemoteException ex) {
                // ignore it
            }
        } else {
            PendingIntent sentIntent = null;
            PendingIntent deliveryIntent = null;
            if (sentIntents != null && sentIntents.size() > 0) {
                sentIntent = sentIntents.get(0);
            }
            if (deliveryIntents != null && deliveryIntents.size() > 0) {
                deliveryIntent = deliveryIntents.get(0);
            }
            sendTextMessage(destinationAddress, scAddress, parts.get(0), simId,
                    sentIntent, deliveryIntent);
        }

    }

     /**
     * Send a multi-part text based SMS.  The callee should have already
     * divided the message into correctly sized parts by calling
     * <code>divideMessage</code>.
     *
     * @param destinationAddress the address to send the message to
     * @param scAddress is the service center address or null to use
     *   the current default SMSC
     * @param parts an <code>ArrayList</code> of strings that, in order,
     *   comprise the original message
     * @param destinationPort the port to deliver the message to
     * @param simId the sim card that user wants to access
     * @param sentIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been sent.
     *   The result code will be <code>Activity.RESULT_OK<code> for success,
     *   or one of these errors:<br>
     *   <code>RESULT_ERROR_GENERIC_FAILURE</code><br>
     *   <code>RESULT_ERROR_RADIO_OFF</code><br>
     *   <code>RESULT_ERROR_NULL_PDU</code><br>
     *   For <code>RESULT_ERROR_GENERIC_FAILURE</code> each sentIntent may include
     *   the extra "errorCode" containing a radio technology specific value,
     *   generally only useful for troubleshooting.<br>
     *   The per-application based SMS control checks sentIntent. If sentIntent
     *   is NULL the caller will be checked against all unknown applicaitons,
     *   which cause smaller number of SMS to be sent in checking period.
     * @param deliveryIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been delivered
     *   to the recipient.  The raw pdu of the status report is in the
     *   extended data ("pdu").
     *
     * @throws IllegalArgumentException if destinationAddress or data are empty
     */
    public static void sendMultipartTextMessage(
            String destinationAddress, String scAddress, 
            ArrayList<String> parts, short destinationPort, int simId,
            ArrayList<PendingIntent> sentIntents, 
            ArrayList<PendingIntent> deliveryIntents) {



        if (!isValidParameters(destinationAddress, parts, sentIntents)) {

            return;
        }

        String isms = getSmsServiceName(simId);
        if (parts.size() > 1) {
            try {
                ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService(isms));
                if (iccISms != null) {
                    iccISms.sendMultipartTextWithPort(destinationAddress, scAddress, parts,
                            destinationPort, sentIntents, deliveryIntents);
                }
            } catch (RemoteException ex) {
                // ignore it
            }
        } else {
            PendingIntent sentIntent = null;
            PendingIntent deliveryIntent = null;
            if (sentIntents != null && sentIntents.size() > 0) {
                sentIntent = sentIntents.get(0);
            }
            if (deliveryIntents != null && deliveryIntents.size() > 0) {
                deliveryIntent = deliveryIntents.get(0);
            }
            sendTextMessage(destinationAddress, scAddress, parts.get(0),
                    destinationPort, simId, sentIntent, deliveryIntent);
        }

    }

    /**
     * Send a data based SMS to a specific application port.
     *
     * @param destinationAddress the address to send the message to
     * @param scAddress is the service center address or null to use
     *  the current default SMSC
     * @param destinationPort the port to deliver the message to
     * @param data the body of the message to send
     * @param simId the sim card that user wants to access
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
     *  is NULL the caller will be checked against all unknown applicaitons,
     *  which cause smaller number of SMS to be sent in checking period.
     * @param deliveryIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is delivered to the recipient.  The
     *  raw pdu of the status report is in the extended data ("pdu").
     *
     * @throws IllegalArgumentException if destinationAddress or data are empty
     */
    public static void sendDataMessage(
            String destinationAddress, String scAddress, short destinationPort,
            byte[] data, int simId, PendingIntent sentIntent, PendingIntent deliveryIntent) {

        if (!isValidParameters(destinationAddress, "send_data" , sentIntent)) {

            return;
        }

        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Invalid message data");
        }

        String isms = getSmsServiceName(simId);
        try {
            ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService(isms));
            if (iccISms != null) {
                iccISms.sendData(destinationAddress, scAddress, destinationPort & 0xFFFF,
                        data, sentIntent, deliveryIntent);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

    }

    /**
     * Send a multi-part data based SMS.  The callee should have already
     * divided the message into correctly sized parts
     *
     * @param destinationAddress the address to send the message to
     * @param scAddress is the service center address or null to use
     *  the current default SMSC
     * @param destinationPort the port to deliver the message to
     * @param data the array of data messages body to send
     * @param simId the sim card that user wants to access
     * @param sentIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been sent.
     *   The result code will be <code>Activity.RESULT_OK<code> for success,
     *   or one of these errors:<br>
     *   <code>RESULT_ERROR_GENERIC_FAILURE</code><br>
     *   <code>RESULT_ERROR_RADIO_OFF</code><br>
     *   <code>RESULT_ERROR_NULL_PDU</code><br>
     *   For <code>RESULT_ERROR_GENERIC_FAILURE</code> each sentIntent may include
     *   the extra "errorCode" containing a radio technology specific value,
     *   generally only useful for troubleshooting.<br>
     *   The per-application based SMS control checks sentIntent. If sentIntent
     *   is NULL the caller will be checked against all unknown applicaitons,
     *   which cause smaller number of SMS to be sent in checking period.
     * @param deliveryIntents if not null, an <code>ArrayList</code> of
     *   <code>PendingIntent</code>s (one for each message part) that is
     *   broadcast when the corresponding message part has been delivered
     *   to the recipient.  The raw pdu of the status report is in the
     *   extended data ("pdu").
     *
     * @throws IllegalArgumentException if destinationAddress or data are empty
     */
    public static void sendMultipartDataMessage(
            String destinationAddress, String scAddress, short destinationPort,
            byte[][] data, int simId, ArrayList<PendingIntent> sentIntents, 
            ArrayList<PendingIntent> deliveryIntents) {

        ArrayList<String> fake_text = new ArrayList<String>();
        fake_text.add("send_data1");
        if (!isValidParameters(destinationAddress, fake_text , sentIntents)) {

            return;
        }

        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Invalid message data");
        }

        String isms = getSmsServiceName(simId);
        if (data.length > 1) {
            ArrayList<SmsRawData> list = new ArrayList<SmsRawData>(data.length);
            for (int i=0; i < data.length ;i++)
            {
                list.add(new SmsRawData(data[i]));
            }
            
            try {
                ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService("isms"));
                if (iccISms != null) {
                    iccISms.sendMultipartData(destinationAddress, scAddress, destinationPort, 
                        list, sentIntents, deliveryIntents);
                }
            } catch (RemoteException ex) {
                // ignore it
            }
        }
        else{
            PendingIntent sentIntent = null;
            PendingIntent deliveryIntent = null;
            if (sentIntents != null && sentIntents.size() > 0) {
                sentIntent = sentIntents.get(0);
            }
            if (deliveryIntents != null && deliveryIntents.size() > 0) {
                deliveryIntent = deliveryIntents.get(0);
            }
            sendDataMessage(destinationAddress, scAddress, destinationPort, 
                    data[0], simId, sentIntent, deliveryIntent);        
        }

    }

    /**
     * Copy a raw SMS PDU to the ICC.
     * ICC (Integrated Circuit Card) is the card of the device.
     * For example, this can be the SIM or USIM for GSM.
     *
     * @param smsc the SMSC for this message, or NULL for the default SMSC
     * @param pdu the raw PDU to store
     * @param status message status (STATUS_ON_ICC_READ, STATUS_ON_ICC_UNREAD,
     *               STATUS_ON_ICC_SENT, STATUS_ON_ICC_UNSENT)
     * @param simId the sim card that user wants to access
     * @return true for success
     *
     */
    public static boolean copyMessageToIcc(
            byte[] smsc, byte[] pdu, int status, int simId) {

        boolean success = false;
        
        String isms = getSmsServiceName(simId);
        
        try {
            ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService(isms));
            if (iccISms != null) {
                success = iccISms.copyMessageToIccEf(status, pdu, smsc);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return success;
    }

    /**
     * Delete the specified message from the ICC.
     * ICC (Integrated Circuit Card) is the card of the device.
     * For example, this can be the SIM or USIM for GSM.
     *
     * @param messageIndex is the record index of the message on ICC
     * @param simId the sim card that user wants to access
     * @return true for success
     *
     */
    public static boolean
    deleteMessageFromIcc(int messageIndex, int simId) {

        boolean success = false;
        String isms = getSmsServiceName(simId);

        byte[] pdu = new byte[IccConstants.SMS_RECORD_LENGTH-1];
        Arrays.fill(pdu, (byte)0xff);

        try {
            ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService(isms));
            if (iccISms != null) {
                success = iccISms.updateMessageOnIccEf(messageIndex, 
                		android.telephony.SmsManager.STATUS_ON_ICC_FREE, pdu);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return success;
    }

    /**
     * Update the specified message on the ICC.
     * ICC (Integrated Circuit Card) is the card of the device.
     * For example, this can be the SIM or USIM for GSM.
     *
     * @param messageIndex record index of message to update
     * @param newStatus new message status (STATUS_ON_ICC_READ,
     *                  STATUS_ON_ICC_UNREAD, STATUS_ON_ICC_SENT,
     *                  STATUS_ON_ICC_UNSENT, STATUS_ON_ICC_FREE)
     * @param pdu the raw PDU to store
     * @param simId the sim card that user wants to access
     * @return true for success
     *
     */
    public static boolean updateMessageOnIcc(int messageIndex,
            int newStatus, byte[] pdu, int simId) {

        boolean success = false;
        String isms = getSmsServiceName(simId);

        try {
            ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService(isms));
            if (iccISms != null) {
                success = iccISms.updateMessageOnIccEf(messageIndex, newStatus, pdu);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return success;
    }

    /**
     * Retrieves all messages currently stored on ICC.
     * ICC (Integrated Circuit Card) is the card of the device.
     * For example, this can be the SIM or USIM for GSM.
     *
     * @param simId the sim card that user wants to access
     * @return <code>ArrayList</code> of <code>SmsMessage</code> objects
     *
     */
    public static ArrayList<android.telephony.SmsMessage> getAllMessagesFromIcc(int simId) {

        String isms = getSmsServiceName(simId);
        List<SmsRawData> records = null;

        try {
            ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService(isms));
            if (iccISms != null) {
                records = iccISms.getAllMessagesFromIccEf();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return createMessageListFromRawRecords(records, simId);

    }
    
    /**
     * Set the memory storage status of the SMS
     * This function is used for FTA test only
     * 
     * @param status false for storage full, true for storage available
     * @param simId the sim card that user wants to access     
     *
     */
    public static void setSmsMemoryStatus(boolean status, int simId) {

        String isms = getSmsServiceName(simId);
        
        try {
            ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService(isms));
            if (iccISms != null) {
                iccISms.setSmsMemoryStatus(status);
            }
        } catch (RemoteException ex) {
            // ignore it    
        }

    }

    /**
     * Judge if SMS subsystem is ready or not
     *
     * @param simId the sim card ID
     *
     * @return true for success
     *
     */
    public static boolean isSmsReady(int simId) {

        boolean isReady = false;
        String isms = getSmsServiceName(simId);
        
        try {
            ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService(isms));
            if (iccISms != null) {
                isReady = iccISms.isSmsReady();
            }
        } catch (RemoteException ex) {
            // ignore it    
        }

        return isReady;
    }

    /**
     * Create a list of <code>SmsMessage</code>s from a list of RawSmsData
     * records returned by <code>getAllMessagesFromIcc()</code>
     *
     *
     * @param records SMS EF records, returned by
     *   <code>getAllMessagesFromIcc</code>
     * @return <code>ArrayList</code> of <code>SmsMessage</code> objects.
     *
     */
    private static ArrayList<android.telephony.SmsMessage> 
    createMessageListFromRawRecords(List<SmsRawData> records, int simId) {
    
        ArrayList<android.telephony.SmsMessage> geminiMessages = null;
        if (records != null) {
            
            int count = records.size();
            geminiMessages =  new ArrayList<android.telephony.SmsMessage>();
            
            for (int i = 0; i < count; i++) {
                SmsRawData data = records.get(i);
                
                if (data != null) {
                    android.telephony.gemini.GeminiSmsMessage geminiSms = 
                            android.telephony.gemini.GeminiSmsMessage.createFromEfRecord(i+1, data.getBytes(), simId);
                    if (geminiSms != null) {
                        geminiMessages.add(geminiSms);
                    }
                }
            }
        }
        return geminiMessages;
    }

    /**
     * Get the SMS service name by specific SIM ID
     * @simId SIM ID
     * @return the SMS service name
     */
    private static String getSmsServiceName(int simId) {

        if (simId == Phone.GEMINI_SIM_1) {
            return "isms";
        } else if (simId == Phone.GEMINI_SIM_2) {
            return "isms2";
        } else {
            return null;
        }
    }

    /**
     * Judge if the destination address is a valid SMS address or not, and if
     * the text is null or not
     * @destinationAddress the destination address to which the message be sent
     * @text the content of shorm message
     * @sentIntent will be broadcast if the address or the text is invalid
     * @return true for valid parameters
     */
    private static boolean isValidParameters(
            String destinationAddress, String text, PendingIntent sentIntent) {

        ArrayList<PendingIntent> sentIntents = 
                new ArrayList<PendingIntent>();
        ArrayList<String> parts = 
                new ArrayList<String>();

        sentIntents.add(sentIntent);
        parts.add(text);

        if (TextUtils.isEmpty(text)) {
            throw new IllegalArgumentException("Invalid message body");
        }

        return isValidParameters(destinationAddress, parts, sentIntents);
    }
    /**
     * Judge if the destination address is a valid SMS address or not, and if
     * the text is null or not
     * @destinationAddress the destination address to which the message be sent
     * @parts the content of shorm message
     * @sentIntent will be broadcast if the address or the text is invalid
     * @return true for valid parameters
     */
    private static boolean isValidParameters(
            String destinationAddress, ArrayList<String> parts,
            ArrayList<PendingIntent> sentIntents) {

        if (!isValidSmsDestinationAddress(destinationAddress)) {

            for (int i=0; i<sentIntents.size(); i++)
            {
                PendingIntent sentIntent = sentIntents.get(i);
                if (sentIntent != null) {
                    try {
                        sentIntent.send(android.telephony.SmsManager.RESULT_ERROR_GENERIC_FAILURE);
                    } catch (CanceledException ex) {}
                }
            }

            Log.d(TAG, "Invalid destinationAddress: " + destinationAddress);
            return false;
        }

        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        }
        if (parts == null || parts.size() < 1) {
            throw new IllegalArgumentException("Invalid message body");
        }

        return true;
    }

    /**
     * judge if the input destination address is a valid SMS address or not
     *
     * @param da the input destination address
     * @return true for success
     *
     */
    private static boolean isValidSmsDestinationAddress(String da) {

        String encodeAddress = PhoneNumberUtils.extractNetworkPortion(da);
        if(encodeAddress == null)
            return true;

        return encodeAddress.length() == da.length();
    }

}

