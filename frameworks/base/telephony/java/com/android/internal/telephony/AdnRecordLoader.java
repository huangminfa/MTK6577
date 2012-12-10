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

import java.util.ArrayList;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.telephony.PhoneNumberUtils;

import java.io.UnsupportedEncodingException;



public class AdnRecordLoader extends Handler {
    static String LOG_TAG;

    //***** Instance Variables

    PhoneBase phone;
    int ef;
    int extensionEF;
    int pendingExtLoads;
    Message userResponse;
    String pin2;

    // For "load one"
    int recordNumber;

    // for "load all"
    ArrayList<AdnRecord> adns; // only valid after EVENT_ADN_LOAD_ALL_DONE
    int current_read;
    int used;
    int total;

    // Either an AdnRecord or a reference to adns depending
    // if this is a load one or load all operation
    Object result;

    //***** Event Constants

    static final int EVENT_ADN_LOAD_DONE = 1;
    static final int EVENT_EXT_RECORD_LOAD_DONE = 2;
    static final int EVENT_ADN_LOAD_ALL_DONE = 3;
    static final int EVENT_EF_LINEAR_RECORD_SIZE_DONE = 4;
    static final int EVENT_UPDATE_RECORD_DONE = 5;
    static final int EVENT_UPDATE_PHB_RECORD_DONE = 101;
    static final int EVENT_VERIFY_PIN2 = 102;
    static final int EVENT_PHB_LOAD_DONE = 103;
    static final int EVENT_PHB_LOAD_ALL_DONE = 104;
    static final int EVENT_PHB_QUERY_STAUTS = 105;

    //***** Constructor

    public AdnRecordLoader(PhoneBase phone) {
        // The telephony unit-test cases may create AdnRecords
        // in secondary threads
        super(phone.getHandler().getLooper());

        this.phone = phone;
        LOG_TAG = phone.getPhoneName();
    }

    /**
     * Resulting AdnRecord is placed in response.obj.result
     * or response.obj.exception is set
     */
    public void
    loadFromEF(int ef, int extensionEF, int recordNumber,
                Message response) {
        this.ef = ef;
        this.extensionEF = extensionEF;
        this.recordNumber = recordNumber;
        this.userResponse = response;

        int type = getPhbStorageType(ef);
        if (type != -1) {
            
            phone.mCM.ReadPhbEntry(
                    type, recordNumber, recordNumber, 
                    obtainMessage(EVENT_PHB_LOAD_DONE));
        }
        else {

        phone.mIccFileHandler.loadEFLinearFixed(
                    ef, recordNumber,
                    obtainMessage(EVENT_ADN_LOAD_DONE));
        }
    }


    /**
     * Resulting ArrayList&lt;adnRecord> is placed in response.obj.result
     * or response.obj.exception is set
     */
    public void
    loadAllFromEF(int ef, int extensionEF,
                Message response) {
        this.ef = ef;
        this.extensionEF = extensionEF;
        this.userResponse = response;

        int type = getPhbStorageType(ef);
        if (type != -1) {
            phone.mCM.queryPhbStorageInfo(
                    type,
                    obtainMessage(EVENT_PHB_QUERY_STAUTS));
        }
        else {
        phone.mIccFileHandler.loadEFLinearFixedAll(
                    ef,
                    obtainMessage(EVENT_ADN_LOAD_ALL_DONE));
        }
    }

    /**
     * Write adn to a EF SIM record
     * It will get the record size of EF record and compose hex adn array
     * then write the hex array to EF record
     *
     * @param adn is set with alphaTag and phone number
     * @param ef EF fileid
     * @param extensionEF extension EF fileid
     * @param recordNumber 1-based record index
     * @param pin2 for CHV2 operations, must be null if pin2 is not needed
     * @param response will be sent to its handler when completed
     */
    public void
    updateEF(AdnRecord adn, int ef, int extensionEF, int recordNumber,
            String pin2, Message response) {
        this.ef = ef;
        this.extensionEF = extensionEF;
        this.recordNumber = recordNumber;
        this.userResponse = response;
        this.pin2 = pin2;

        int type = getPhbStorageType(ef);
        if (type != -1) {

            updatePhb(adn, type);
        }
        else {
            
        phone.mIccFileHandler.getEFLinearRecordSize( ef,
            obtainMessage(EVENT_EF_LINEAR_RECORD_SIZE_DONE, adn));
    }
    }

    //***** Overridden from Handler

    public void
    handleMessage(Message msg) {
        AsyncResult ar;
        byte data[];
        AdnRecord adn;
        PhbEntry[] entries;
        int[] read_info;
        int type;

        try {
            switch (msg.what) {
                case EVENT_EF_LINEAR_RECORD_SIZE_DONE:
                    ar = (AsyncResult)(msg.obj);
                    adn = (AdnRecord)(ar.userObj);

                    if (ar.exception != null) {
                        throw new RuntimeException("get EF record size failed",
                                ar.exception);
                    }

                    int[] recordSize = (int[])ar.result;
                    // recordSize is int[3] array
                    // int[0]  is the record length
                    // int[1]  is the total length of the EF file
                    // int[2]  is the number of records in the EF file
                    // So int[0] * int[2] = int[1]
                   if (recordSize.length != 3 || recordNumber > recordSize[2]) {
                        throw new RuntimeException("get wrong EF record size format",
                                ar.exception);
                    }

                    data = adn.buildAdnString(recordSize[0]);

                    if(data == null) {
                        throw new RuntimeException("wrong ADN format",
                                ar.exception);
                    }

                    phone.mIccFileHandler.updateEFLinearFixed(ef, recordNumber,
                            data, pin2, obtainMessage(EVENT_UPDATE_RECORD_DONE));

                    pendingExtLoads = 1;

                    break;
                case EVENT_UPDATE_RECORD_DONE:
                    ar = (AsyncResult)(msg.obj);
                    if (ar.exception != null) {
                        throw new RuntimeException("update EF adn record failed",
                                ar.exception);
                    }
                    pendingExtLoads = 0;
                    result = null;
                    break;
                case EVENT_ADN_LOAD_DONE:
                    ar = (AsyncResult)(msg.obj);
                    data = (byte[])(ar.result);

                    if (ar.exception != null) {
                        throw new RuntimeException("load failed", ar.exception);
                    }

                    if (false) {
                        Log.d(LOG_TAG,"ADN EF: 0x"
                            + Integer.toHexString(ef)
                            + ":" + recordNumber
                            + "\n" + IccUtils.bytesToHexString(data));
                    }

                    adn = new AdnRecord(ef, recordNumber, data);
                    result = adn;

                    if (adn.hasExtendedRecord()) {
                        // If we have a valid value in the ext record field,
                        // we're not done yet: we need to read the corresponding
                        // ext record and append it

                        pendingExtLoads = 1;

                        phone.mIccFileHandler.loadEFLinearFixed(
                            extensionEF, adn.extRecord,
                            obtainMessage(EVENT_EXT_RECORD_LOAD_DONE, adn));
                    }
                break;

                case EVENT_EXT_RECORD_LOAD_DONE:
                    ar = (AsyncResult)(msg.obj);
                    data = (byte[])(ar.result);
                    adn = (AdnRecord)(ar.userObj);

                    if (ar.exception != null) {
                        throw new RuntimeException("load failed", ar.exception);
                    }

                    Log.d(LOG_TAG,"ADN extension EF: 0x"
                        + Integer.toHexString(extensionEF)
                        + ":" + adn.extRecord
                        + "\n" + IccUtils.bytesToHexString(data));

                    adn.appendExtRecord(data);

                    pendingExtLoads--;
                    // result should have been set in
                    // EVENT_ADN_LOAD_DONE or EVENT_ADN_LOAD_ALL_DONE
                break;

                case EVENT_ADN_LOAD_ALL_DONE:
                    ar = (AsyncResult)(msg.obj);
                    ArrayList<byte[]> datas = (ArrayList<byte[]>)(ar.result);

                    if (ar.exception != null) {
                        throw new RuntimeException("load failed", ar.exception);
                    }

                    adns = new ArrayList<AdnRecord>(datas.size());
                    result = adns;
                    pendingExtLoads = 0;

                    for(int i = 0, s = datas.size() ; i < s ; i++) {
                        adn = new AdnRecord(ef, 1 + i, datas.get(i));
                        adns.add(adn);

                        if (adn.hasExtendedRecord()) {
                            // If we have a valid value in the ext record field,
                            // we're not done yet: we need to read the corresponding
                            // ext record and append it

                            pendingExtLoads++;

                            phone.mIccFileHandler.loadEFLinearFixed(
                                extensionEF, adn.extRecord,
                                obtainMessage(EVENT_EXT_RECORD_LOAD_DONE, adn));
                        }
                    }
                break;
//MTK-START [mtk80601][111215][ALPS00093395]
                case EVENT_UPDATE_PHB_RECORD_DONE:
                    ar = (AsyncResult)(msg.obj);
                    if (ar.exception != null) {
                        throw new RuntimeException("update PHB EF record failed",
                                ar.exception);
                    }
                    pendingExtLoads = 0;
                    result = null;
                    break;
                    
                case EVENT_VERIFY_PIN2:
                    ar = (AsyncResult)(msg.obj);
                    adn = (AdnRecord)(ar.userObj);

                    if (ar.exception != null) {
                        throw new RuntimeException("PHB Verify PIN2 error",
                                ar.exception);
                    }
                    
                    writeEntryToModem(adn, getPhbStorageType(ef));
                    pendingExtLoads = 1;
                    break;

                case EVENT_PHB_LOAD_DONE:
                    ar = (AsyncResult)(msg.obj);
                    entries = (PhbEntry[]) (ar.result);

                    if (ar.exception != null) {
                        throw new RuntimeException("PHB Read an entry Error",
                                ar.exception);
                    }

                    adn = getAdnRecordFromPhbEntry(entries[0]);
                    result = adn;
                    pendingExtLoads = 0;
                    
                    break;

                case EVENT_PHB_QUERY_STAUTS:
                    /* response.obj.result[0] is number of current used entries
                     * response.obj.result[1] is number of total entries in the storage
                     */

                    ar = (AsyncResult)(msg.obj);
                    int[] info = (int[]) (ar.result);

                    if (ar.exception != null) {
                        throw new RuntimeException("PHB Query Info Error",
                                ar.exception);
                    }

                    type = getPhbStorageType(ef);
                    read_info = new int[3];
                    read_info[0] = 1; //current_index;
                    read_info[1] = info[0]; // # of remaining entries
                    read_info[2] = info[1]; // # of total entries

                    adns = new ArrayList<AdnRecord>(read_info[2]);
                    for (int i=0; i<read_info[2]; i++) {
                        // fillin empty entries to adns
                        adn = new AdnRecord(ef, i+1, "", "");
                        adns.add(i, adn);
                    }
                    
                    readEntryFromModem(type, read_info);
                    pendingExtLoads = 1;
                    break;

                case EVENT_PHB_LOAD_ALL_DONE:
                    ar = (AsyncResult)(msg.obj);
                    read_info = (int[]) (ar.userObj);
                    entries = (PhbEntry[])(ar.result);

                    if (ar.exception != null) {
                        throw new RuntimeException("PHB Read Entries Error",
                                ar.exception);
                    }

                    for (int i=0; i<entries.length; i++) {						
                        adn = getAdnRecordFromPhbEntry(entries[i]);
                        if(adn != null)
                        { 
                            adns.set(adn.recordNumber-1, adn);   
                            read_info[1]--;   
                            Log.d(LOG_TAG, "Read entries: " + adn);
 
                        }
                        else
                        {
                            Log.e(LOG_TAG, "getAdnRecordFromPhbEntry return null" );
                            throw new RuntimeException(
                                "getAdnRecordFromPhbEntry return null" ,
                                CommandException.fromRilErrno(
                                        RILConstants.GENERIC_FAILURE));  
                        }                     
                    }
                    read_info[0] += RILConstants.PHB_MAX_ENTRY;

                    if (read_info[1] < 0) {
                        Log.e(LOG_TAG, "the read entries is not sync with query status: " + read_info[1] );
                        throw new RuntimeException(
                                "the read entries is not sync with query status: " + read_info[1],
                                CommandException.fromRilErrno(
                                        RILConstants.GENERIC_FAILURE));
                    }

                    
                    if (read_info[1] == 0 || read_info[0] >= read_info[2]) {
                        
                        result = adns;
                        pendingExtLoads = 0;
                    }
                    else {
                        type = getPhbStorageType(ef);
                        readEntryFromModem(type, read_info);
                    }
                    break;
//MTK-END [mtk80601][111215][ALPS00093395]
            }
        } catch (RuntimeException exc) {
            if (userResponse != null) {
                AsyncResult.forMessage(userResponse).exception = exc.getCause();
                userResponse.sendToTarget();
                // Loading is all or nothing--either every load succeeds
                // or we fail the whole thing.
                userResponse = null;
            }
            return;
        }

        if (userResponse != null && pendingExtLoads == 0) {
            AsyncResult.forMessage(userResponse).result
                = result;

            userResponse.sendToTarget();
            userResponse = null;
        }
    }
//MTK-START [mtk80601][111215][ALPS00093395]
    private void updatePhb(AdnRecord adn, int type)
    {

        if (pin2 != null) {
            phone.mCM.supplyIccPin2(pin2, obtainMessage(EVENT_VERIFY_PIN2, adn));
        }
        else {
            writeEntryToModem(adn, type);
        }
        
    }

    private boolean canUseGsm7Bit(String alphaId)
    {
        //try{
        //    GsmAlphabet.countGsmSeptets(alphaId, true);
        //} catch(EncodeException ex)
        //{
        //    return false;
        //}
        //return true;
        return (GsmAlphabet.countGsmSeptets(alphaId, true)) != null;
    }

    private String EncodeATUCS2(String input)
    {
        byte[] textPart;
        StringBuilder output;

        output = new StringBuilder();

        for (int i=0; i<input.length() ; i++)
        {
            String hexInt = Integer.toHexString( input.charAt(i) );
            for (int j=0; j<(4 - hexInt.length()); j++)
                output.append("0");
            output.append(hexInt);
        }

        return output.toString();
    }

    private int getPhbStorageType(int ef) {
        int type = -1;
        switch (ef) {
            case IccConstants.EF_ADN:
                type = RILConstants.PHB_ADN;
                break;
            case IccConstants.EF_FDN:
                type = RILConstants.PHB_FDN;
                break;
            case IccConstants.EF_MSISDN:
                type = RILConstants.PHB_MSISDN;
                break;
            default:
                break;
        }
        return type;
    }

    private void writeEntryToModem(AdnRecord adn, int type) {
        int ton = 0x81;
        String number = adn.getNumber();
        String alphaId = adn.getAlphaTag();

        // eliminate '+' from number
        if (number.indexOf('+') != -1)
        {
            if (number.indexOf('+') != number.lastIndexOf('+'))
            {
                // there are multiple '+' in the String
                Log.d(LOG_TAG, "There are multiple '+' in the number: " + number);
            }
            ton = 0x91;

            number = number.replace("+", "");
        }
        // replace N with ?
        number = number.replace(PhoneNumberUtils.WILD, '?');
        // replace , with p
        number = number.replace(PhoneNumberUtils.PAUSE, 'p');
		// replace , with w
        number = number.replace(PhoneNumberUtils.WAIT, 'w');

        //Add by mtk80995  replace \ to \5c  and replace " to \22  for MTK modem
        //the order is very important! for "\\" is substring of "\\22"
        alphaId = alphaId.replace("\\", "\\5c");
        alphaId = alphaId.replace("\"", "\\22");
        //end Add by mtk80995 	

        // encode Alpha ID
        alphaId = EncodeATUCS2(alphaId);

        PhbEntry entry = new PhbEntry();
        if (!(number.equals("") && alphaId.equals("") && ton == 0x81)) {

            entry.type = type;
            entry.index = recordNumber;
            entry.number = number;
            entry.ton = ton;
            entry.alphaId = alphaId;
        }
        else {
            entry.type = type;
            entry.index = recordNumber;
            entry.number = null;
            entry.ton = ton;
            entry.alphaId = null;
        }

        //Log.d(LOG_TAG,"Update Entry: " + entry);

        phone.mCM.writePhbEntry(entry, 
                obtainMessage(EVENT_UPDATE_PHB_RECORD_DONE));

    }

    private void readEntryFromModem(int type, int[] read_info) {

        if (read_info.length != 3) {
            Log.e(LOG_TAG, "readEntryToModem, invalid paramters:" + read_info.length);
            return;
        }

        // read_info[0]   : current_index;
        // read_info[1]   : # of remaining entries
        // read_info[2]   : # of total entries

        int eIndex;
        int count;

        eIndex = read_info[0] + RILConstants.PHB_MAX_ENTRY -1;
        if (eIndex > read_info[2]) {
            eIndex = read_info[2];
        }

        phone.mCM.ReadPhbEntry(type, read_info[0], eIndex, 
            obtainMessage(EVENT_PHB_LOAD_ALL_DONE, read_info));
    }

    private AdnRecord getAdnRecordFromPhbEntry(PhbEntry entry) {

        Log.d(LOG_TAG, "Parse Adn entry :" + entry);

        String alphaId;
        byte[] ba = IccUtils.hexStringToBytes(entry.alphaId);	
        if(ba == null)
        {
            Log.e(LOG_TAG, "entry.alphaId is null");
            return null;
        }
	 
        try {
            alphaId = new String(ba, 0, entry.alphaId.length()/2, "utf-16be");
        } catch (UnsupportedEncodingException ex) {
            Log.e(LOG_TAG, "implausible UnsupportedEncodingException",
                    ex);
            return null;
        }
        //Log.d(LOG_TAG, "Decode ADN alphaId: " + alphaId);

        String number;
        if (entry.ton == PhoneNumberUtils.TOA_International) {
            number = PhoneNumberUtils.prependPlusToNumber(entry.number);
        }
        else {
            number = entry.number;
        }

        // replace ? with N
        number = number.replace('?', PhoneNumberUtils.WILD);
        // replace P with ,
        number = number.replace('p', PhoneNumberUtils.PAUSE);

        //Log.d(LOG_TAG, "Decode ADN number: " + number);

        return new AdnRecord(ef, entry.index, alphaId, number);

    }
//MTK-END [mtk80601][111215][ALPS00093395]
}
