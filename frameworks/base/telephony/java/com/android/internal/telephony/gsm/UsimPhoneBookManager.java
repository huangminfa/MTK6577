/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import com.android.internal.telephony.AdnRecord;
import com.android.internal.telephony.AdnRecordCache;
import com.android.internal.telephony.IccConstants;
import com.android.internal.telephony.IccUtils;
import com.android.internal.telephony.PhoneBase;
//MTK-START [mtk80601][111215][ALPS00093395]
import android.text.TextUtils;
import android.telephony.PhoneNumberUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.IccProvider;
import com.android.internal.telephony.UsimGroup;
import com.android.internal.telephony.AlphaTag;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.IccFileHandler;
import com.android.internal.telephony.IccIoResult;
import com.android.internal.telephony.IccException;
import org.apache.harmony.luni.lang.reflect.ListOfTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import android.content.Intent;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;
//MTK-END [mtk80601][111215][ALPS00093395]

/**
 * This class implements reading and parsing USIM records.
 * Refer to Spec 3GPP TS 31.102 for more details.
 *
 * {@hide}
 */
public class UsimPhoneBookManager extends Handler implements IccConstants {
    private static final String LOG_TAG = "UPBM";
    private static final boolean DBG = true;
    private PbrFile mPbrFile;
    private Boolean mIsPbrPresent;
    private PhoneBase mPhone;
    private AdnRecordCache mAdnCache;
    private Object mLock = new Object();
    private Object mGasLock = new Object();
    private Object mAasLock = new Object();
    private ArrayList<AdnRecord> mPhoneBookRecords;
    private boolean mEmailPresentInIap = false;
    private int mEmailTagNumberInIap = 0;
    private int mEmailRecordSize = -1; 	
    private int mEmailFileSize = 100;
    private int mAdnFileSize = 250;
    private ArrayList<byte[]> mIapFileRecords;
    private ArrayList<byte[]> mEmailFileRecord;
    private ArrayList<byte[]> mPbrFileRecords; 
    private ArrayList<byte[]> mAnrFileRecords;
    private ArrayList<byte[]> mAasFileRecords;
    private Map<Integer, ArrayList<String>> mEmailsForAdnRec;
    private Map<Integer, String> mAnrForAdnRec;	
    private boolean mRefreshCache = false;
    private ArrayList<UsimGroup> mGasForGrp;
    private HashMap<Integer,ArrayList<String>> mAasForAnrRec;	
    private ArrayList<ArrayList<byte[]>> mIapFileList = null;
    private ArrayList<ArrayList<byte[]>> mAnrFileList = null;
    private ArrayList<ArrayList<byte[]>> mEmailFileList = null;
    private int[] mEmailRecTable = new int[400];	
    //private int mReadingAnrNum = 0;	
    //private int mReadingEmailNum = 0;
    //private int mReadingGrpNum = 0;
    private int mAnrRecordSize = 0;
    private int mAnrFileSize = 0;
    private int[] mUpbCap = new int[8];
    private int mResult = -1;

    private static final int EVENT_PBR_LOAD_DONE = 1;
    private static final int EVENT_USIM_ADN_LOAD_DONE = 2;
    private static final int EVENT_IAP_LOAD_DONE = 3;
    private static final int EVENT_EMAIL_LOAD_DONE = 4;
    private static final int EVENT_ANR_LOAD_DONE = 5;	
    private static final int EVENT_GRP_LOAD_DONE = 6;
    private static final int EVENT_GAS_LOAD_DONE = 7;	
    private static final int EVENT_AAS_LOAD_DONE = 8;	
    private static final int EVENT_ANR_UPDATE_DONE = 9;
    private static final int EVENT_GRP_UPDATE_DONE = 10;	
    private static final int EVENT_EMAIL_UPDATE_DONE = 11;
    private static final int EVENT_IAP_UPDATE_DONE = 12;	
    private static final int EVENT_ANR_RECORD_LOAD_DONE = 13;
    private static final int EVENT_GET_ANR_RECORD_SIZE_DONE = 14;
    private static final int EVENT_GRP_RECORD_LOAD_DONE = 15;
    private static final int EVENT_UPB_CAPABILITY_QUERY_DONE = 16;  
    private static final int EVENT_GAS_UPDATE_DONE = 17;
	private static final int EVENT_EMAIL_LENGTH_READ_DONE = 18;
	private static final int EVENT_EMAIL_RECORD_LOAD_DONE = 19;
    private static final int EVENT_GET_AAS_RECORD_SIZE_DONE = 20;
    private static final int EVENT_AAS_UPDATE_DONE = 21;
    private static final int USIM_TYPE1_TAG   = 0xA8;
    private static final int USIM_TYPE2_TAG   = 0xA9;
    private static final int USIM_TYPE3_TAG   = 0xAA;
    private static final int USIM_EFADN_TAG   = 0xC0;
    private static final int USIM_EFIAP_TAG   = 0xC1;
    private static final int USIM_EFEXT1_TAG  = 0xC2;
    private static final int USIM_EFSNE_TAG   = 0xC3;
    private static final int USIM_EFANR_TAG   = 0xC4;
    private static final int USIM_EFPBC_TAG   = 0xC5;
    private static final int USIM_EFGRP_TAG   = 0xC6;
    private static final int USIM_EFAAS_TAG   = 0xC7;
    private static final int USIM_EFGSD_TAG   = 0xC8;
    private static final int USIM_EFUID_TAG   = 0xC9;
    private static final int USIM_EFEMAIL_TAG = 0xCA;
    private static final int USIM_EFCCP1_TAG  = 0xCB;
	
    //Error code for USIM Group
    static public final int USIM_ERROR_NAME_LEN = -10;      //the input group name is too long!
    static public final int USIM_ERROR_GROUP_COUNT = -20;   //outnumber the  max count of groups 
    

    private static final int UPB_EF_ANR = 0;
    private static final int UPB_EF_EMAIL = 1;
    private static final int UPB_EF_SNE = 2;
    private static final int UPB_EF_AAS = 3;
    private static final int UPB_EF_GAS = 4;
    private static final int UPB_EF_GRP = 5;
	private Object mReadLock = new Object();
	private AtomicInteger mReadingAnrNum = new AtomicInteger(0);
    private AtomicInteger mReadingEmailNum = new AtomicInteger(0);
    private AtomicInteger mReadingGrpNum = new AtomicInteger(0);

    public UsimPhoneBookManager(PhoneBase phone, AdnRecordCache cache) {
        mPhone = phone;
        mPhoneBookRecords = new ArrayList<AdnRecord>();		
        mGasForGrp = new ArrayList<UsimGroup>();
        mIapFileList = new ArrayList<ArrayList<byte[]>>();
        mPbrFile = null;
        // We assume its present, after the first read this is updated.
        // So we don't have to read from UICC if its not present on subsequent reads.
        mIsPbrPresent = true;
        mAdnCache = cache;
	Intent intent = new Intent();
	intent.setAction("android.intent.action.ACTION_PHONE_RESTART");
	intent.putExtra("SimId", mPhone.getMySimId());
	mPhone.getContext().sendBroadcast(intent);
        Log.d(LOG_TAG, "UsimPhoneBookManager constructor finished. ");
    }

    public void reset() {
        mPhoneBookRecords.clear();		
        mGasForGrp.clear();
        mIapFileRecords = null;
        mIapFileList = null;
        mEmailFileRecord = null;
        mPbrFile = null;
        mIsPbrPresent = true;
        mRefreshCache = false;
	mPbrFileRecords = null;
        Log.d(LOG_TAG, "UsimPhoneBookManager reset finished. ");

    }

    public ArrayList<AdnRecord> loadEfFilesFromUsim() {
        synchronized (mLock) {
            if (!mPhoneBookRecords.isEmpty()) {
                Log.d(LOG_TAG, "mPhoneBookRecords.size " + mPhoneBookRecords.size() + ",mRefreshCache " + mRefreshCache);
                if (mRefreshCache) {
                    mRefreshCache = false;
                    refreshCache();
                }
                return mPhoneBookRecords;
            }

            if (!mIsPbrPresent) return null;

            // Check if the PBR file is present in the cache, if not read it
            // from the USIM.
            if (mPbrFile == null) {
                readPbrFileAndWait(false);
            }

            if (mPbrFile == null) {
                readPbrFileAndWait(true);
            }

            if (mPbrFile == null) {
                readAdnFileAndWait(0);
                return null;
            }
            if (null != mPbrFile.mFileIds.get(0).get(USIM_EFEMAIL_TAG))
                readRecordSize(mPbrFile.mFileIds.get(0).get(USIM_EFEMAIL_TAG));
            int numRecs = mPbrFile.mFileIds.size();
            // read adn by CPBR, not by read record from EF , So we needn't read
            // for every pbr record.
            readAdnFileAndWait(0);
            if (mAasForAnrRec == null) {
                mAasForAnrRec = new HashMap<Integer, ArrayList<String>>();
            }
            mAasForAnrRec.clear();
            String optr = SystemProperties.get("ro.operator.optr");
            Log.d(LOG_TAG, "optr: " + optr);
            for (int i = 0; i < numRecs; i++) {
                if (optr != null && optr.equals("OP03")) {
                    readAASFileAndWait(i);
                }
                readAnrFileAndWait(i);
                readEmailFileAndWait(i);
            }
            readGrpIdsAndWait();
            // All EF files are loaded, post the response.
        }
        return mPhoneBookRecords;
    }

    private void refreshCache() {
        if (mPbrFile == null) return;
        mPhoneBookRecords.clear();

        int numRecs = mPbrFile.mFileIds.size();
        for (int i = 0; i < numRecs; i++) {
            readAdnFileAndWait(i);
        }
    }

    public void invalidateCache() {
        mRefreshCache = true;
    }

    private void readPbrFileAndWait(boolean is7FFF) {
        mPhone.getIccFileHandler().loadEFLinearFixedAll(EF_PBR, obtainMessage(EVENT_PBR_LOAD_DONE), is7FFF);
        try {
            mLock.wait();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Interrupted Exception in readPbrFileAndWait");
        }
    }

    private void readEmailFileAndWait(int recNum) {
        Map <Integer,Integer> fileIds;
        if(mPbrFile == null || mPbrFile.mFileIds == null) return;
        fileIds = mPbrFile.mFileIds.get(recNum);
        if (fileIds == null) return;

        if (fileIds.containsKey(USIM_EFEMAIL_TAG)) {
            int efid = fileIds.get(USIM_EFEMAIL_TAG);
            log("readEmailFileAndWait: efid=" + efid);
            EfRecord rec = null;
            for (EfRecord record : mPbrFile.mEmailFileids) {
                log("readEmailFileAndWait mEmailFile record " + record);
                if (record.mEfTag == efid && record.mPbrRecord == recNum) {
                    rec = record;
                    if (record.mType == USIM_TYPE1_TAG) {
                        readType1Ef(record);
                        return;
                    } else if (record.mType == USIM_TYPE2_TAG) {
                        log("readEmailFileAndWait type2 email " + record);
                        //readType2Ef(record);
                        //return;
                    }
                    break;
                }
            }
            // Check if the EFEmail is a Type 1 file or a type 2 file.
            // If mEmailPresentInIap is true, its a type 2 file.
            // So we read the IAP file and then read the email records.
            // instead of reading directly.
            if (mEmailPresentInIap) {
                readIapFileAndWait(recNum, fileIds.get(USIM_EFIAP_TAG));
                if (mIapFileRecords == null) {
                    Log.e(LOG_TAG, "Error: IAP file is empty");
                    return;
                }
            }
            // Read the EFEmail file.
            Message msg = obtainMessage(EVENT_EMAIL_LOAD_DONE);
            msg.arg1 = recNum;
            mPhone.getIccFileHandler().loadEFLinearFixedAll(fileIds.get(USIM_EFEMAIL_TAG), msg);
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Interrupted Exception in readEmailFileAndWait");
            }

            if (mEmailFileRecord == null) {
                Log.e(LOG_TAG, "Error: Email file is empty");
                return;
            }

           updatePhoneAdnRecordByEmailFile(recNum);
        }

    }

    private void readIapFileAndWait(int pbrIndex, int efid) {
        log("readIapFileAndWait pbrIndex :" + pbrIndex + ",efid:" + efid);
//        if (mIapFileList != null) {
//            ArrayList<byte[]> iap = mIapFileList.get(pbrIndex);
//            if (iap != null) {
//                mIapFileRecords = iap;
//                return;
//            }
//        }
        Message msg = obtainMessage(EVENT_IAP_LOAD_DONE);
        msg.arg1 = pbrIndex;
        mPhone.getIccFileHandler().loadEFLinearFixedAll(efid, msg);
        try {
            mLock.wait();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Interrupted Exception in readIapFileAndWait");
        }
    }

    private void readAASFileAndWait(int recNum) {
        Log.d(LOG_TAG, "readAASFileAndWait " + recNum);
        if (mPbrFile == null || mPbrFile.mFileIds == null) {
            return;
        }
        Map <Integer, Integer> fileIds = mPbrFile.mFileIds.get(recNum);
        if (!fileIds.containsKey(USIM_EFAAS_TAG)) {
            Log.e(LOG_TAG, "readAASFileAndWait-PBR have no AAS EF record");
            return;
        }
        int efid = fileIds.get(USIM_EFAAS_TAG);
        Log.d(LOG_TAG, "readAASFileAndWait-get AAS EFID " + efid);
        if (mAasForAnrRec != null) {
            if (mAasForAnrRec.containsKey(recNum)) {
                log("AAS has been loaded for Pbr number " + recNum);
                return;
            }
            Set<Integer> set = mAasForAnrRec.keySet();
            if (!set.isEmpty()) {
                Iterator<Integer> iter = set.iterator();
                while (iter.hasNext()) {
                    int pbr = iter.next();
                    Map <Integer, Integer> fileid = mPbrFile.mFileIds.get(recNum);
                    int ef = fileIds.get(USIM_EFAAS_TAG);
                    if (efid == ef) {
                        log("AAS has been loaded for ef " + efid);
                        return;
                    }
                }
            }
        }
        IccFileHandler iccFh = mPhone.getIccFileHandler();
        if (iccFh != null){
            Message msg = obtainMessage(EVENT_AAS_LOAD_DONE);
            msg.arg1 = recNum;
            iccFh.loadEFLinearFixedAll(efid, msg);
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Interrupted Exception in readAASFileAndWait");
            }
        } else {
            Log.e(LOG_TAG, "readAASFileAndWait-IccFileHandler is null");
            return;
        }
    }
    
    private void readAnrFileAndWait(int recNum) {
	 Map <Integer,Integer> fileIds;
	if(mPbrFile == null || mPbrFile.mFileIds == null) return;
        fileIds = mPbrFile.mFileIds.get(recNum);
        if (fileIds == null || fileIds.isEmpty()) return;
        Log.d(LOG_TAG, "UsimPhoneBookManager readAnrFileAndWait: recNum is " + recNum );    
   
        if (!fileIds.containsKey(USIM_EFANR_TAG)) {
            Log.d(LOG_TAG, "UsimPhoneBookManager readAnrFileAndWait: No anr tag in pbr record " + recNum );
            return;
        }

        int efid = fileIds.get(USIM_EFANR_TAG);
        Log.d(LOG_TAG, "UsimPhoneBookManager readAnrFileAndWait: recNum is " + recNum
                + " EFANR id is " + efid);
        
        for (EfRecord record : mPbrFile.mAnrFileids) {
            if (record.mEfTag == efid && record.mPbrRecord == recNum) {
                if (record.mType == USIM_TYPE2_TAG ) {
                    readType2Ef(record);
                    return;
                } else if (record.mType == USIM_TYPE1_TAG){
                    //readType1Ef(record);
                    //return;
                }
                break;
            }
        }
        int numAdnRecs = mPhoneBookRecords.size();
        int nOffset = recNum * mAdnFileSize;
        int nMax = nOffset + mAdnFileSize;
        if (numAdnRecs < nMax) {
            nMax = numAdnRecs;
        }
   
        IccFileHandler iccFh = mPhone.getIccFileHandler();
        if (iccFh != null){
            iccFh.getEFLinearRecordSize(efid,obtainMessage(EVENT_GET_ANR_RECORD_SIZE_DONE));
        }else{
            return;
        }
   
        try {
            mLock.wait();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Interrupted Exception in readAnrFileAndWait");
        }

        if (mAnrRecordSize == 0 ){
            Log.d(LOG_TAG, "UsimPhoneBookManager readAnrFileAndWait: AnrRecordSize is 0. " ); 
            return;
        }
        for (int i = nOffset; i < nMax; i++) {
            AdnRecord rec;
            try {
                rec = mPhoneBookRecords.get(i); 
            } catch (IndexOutOfBoundsException e){
                Log.d(LOG_TAG, "UsimPhoneBookManager readAnrFileAndWait: mPhoneBookRecords IndexOutOfBoundsException numAdnRecs is " + numAdnRecs + "index is " + i ); 
                break;
            }
            if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0 ) {
                int[] data = new int[2];
                data[0] = recNum;
                data[1] = i;
                iccFh.readEFLinearFixed(efid, i+1-nOffset, mAnrRecordSize, obtainMessage(EVENT_ANR_RECORD_LOAD_DONE, data));
   
                mReadingAnrNum.addAndGet(1);
                Log.d(LOG_TAG, "UsimPhoneBookManager readAnrFileAndWait: read anr for  " + i + " adn " + "( " + rec.getAlphaTag() + ", " + rec.getNumber() + " )  mReadingAnrNum is " + mReadingAnrNum.get());  
            }      
        }
   
        if(mReadingAnrNum.get() == 0) {
                return;
        }
        try {
            mLock.wait();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Interrupted Exception in readAnrFileAndWait");
        }
    }

    private void readGrpIdsAndWait(){
        //todo: judge if grp is supported
        Log.d(LOG_TAG, "UsimPhoneBookManager readGrpIdsAndWait begin" );    

        int numAdnRecs = mPhoneBookRecords.size();
   
        for (int i = 0; i < numAdnRecs; i++) {
            AdnRecord rec;
            try {
                rec = mPhoneBookRecords.get(i); 
            } catch (IndexOutOfBoundsException e){
                Log.d(LOG_TAG, "UsimPhoneBookManager readGrpIdsAndWait: mPhoneBookRecords IndexOutOfBoundsException numAdnRecs is " + numAdnRecs + "index is " + i ); 
                break;
            }
            if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0 ) {
                int adnIndex = rec.getRecordIndex();
                int[] data = new int[2];
                data[0] = i;
                data[1] = adnIndex;
                mPhone.mCM.readUPBGrpEntry(adnIndex,obtainMessage(EVENT_GRP_RECORD_LOAD_DONE, data));
   
                mReadingGrpNum.incrementAndGet();
                Log.d(LOG_TAG, "UsimPhoneBookManager readGrpIdsAndWait: read grp for  " + i + " adn " + "( " + rec.getAlphaTag() + ", " + rec.getNumber() + " )  mReadingGrpNum is " + mReadingGrpNum.get());
            }      
        }
   
        if(mReadingGrpNum.get() == 0) {
            return;
        }
        try {
            mLock.wait();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Interrupted Exception in readAnrFileAndWait");
        }
    }

    private void updatePhoneAdnRecord() {
        if (mEmailFileRecord == null) return;
        int numAdnRecs = mPhoneBookRecords.size();
        if (mIapFileRecords != null) {
            // The number of records in the IAP file is same as the number of records in ADN file.
            // The order of the pointers in an EFIAP shall be the same as the order of file IDs
            // that appear in the TLV object indicated by Tag 'A9' in the reference file record.
            // i.e value of mEmailTagNumberInIap

            for (int i = 0; i < numAdnRecs; i++) {
                byte[] record = null;
                try {
                    record = mIapFileRecords.get(i);
                } catch (IndexOutOfBoundsException e) {
                    Log.e(LOG_TAG, "Error: Improper ICC card: No IAP record for ADN, continuing");
                    break;
                }
                int recNum = record[mEmailTagNumberInIap];

                if (recNum != -1) {
                    String[] emails = new String[1];
                    // SIM record numbers are 1 based
                    emails[0] = readEmailRecord(recNum - 1);
                    AdnRecord rec = mPhoneBookRecords.get(i);
                    if (rec != null) {
                        rec.setEmails(emails);
                    } else {
                        // might be a record with only email
                        rec = new AdnRecord("", "", emails);
                    }
                    mPhoneBookRecords.set(i, rec);
                }
            }
        }

        // ICC cards can be made such that they have an IAP file but all
        // records are empty. So we read both type 1 and type 2 file
        // email records, just to be sure.

        int len = mPhoneBookRecords.size();
        // Type 1 file, the number of records is the same as the number of
        // records in the ADN file.
        if (mEmailsForAdnRec == null) {
            parseType1EmailFile(len);
        }
        for (int i = 0; i < numAdnRecs; i++) {
            ArrayList<String> emailList = null;
            try {
                emailList = mEmailsForAdnRec.get(i);
            } catch (IndexOutOfBoundsException e) {
                break;
            }
            if (emailList == null) continue;

            AdnRecord rec = mPhoneBookRecords.get(i);

            String[] emails = new String[emailList.size()];
            System.arraycopy(emailList.toArray(), 0, emails, 0, emailList.size());
            rec.setEmails(emails);
            mPhoneBookRecords.set(i, rec);
        }
    }

    void parseType1EmailFile(int numRecs) {
        mEmailsForAdnRec = new HashMap<Integer, ArrayList<String>>();
        byte[] emailRec = null;
        for (int i = 0; i < numRecs; i++) {
            try {
                emailRec = mEmailFileRecord.get(i);
            } catch (IndexOutOfBoundsException e) {
                Log.e(LOG_TAG, "Error: Improper ICC card: No email record for ADN, continuing");
                break;
            }
            int adnRecNum = emailRec[emailRec.length - 1];

            if (adnRecNum == -1) {
                continue;
            }

            String email = readEmailRecord(i);

            if (email == null || email.equals("")) {
                continue;
            }

            // SIM record numbers are 1 based.
            ArrayList<String> val = mEmailsForAdnRec.get(adnRecNum - 1);
            if (val == null) {
                val = new ArrayList<String>();
            }
            val.add(email);
            // SIM record numbers are 1 based.
            mEmailsForAdnRec.put(adnRecNum - 1, val);
        }
    }

    private String readEmailRecord(int recNum) {
        byte[] emailRec = null;
        try {
            emailRec = mEmailFileRecord.get(recNum);
        } catch (IndexOutOfBoundsException e) {
            Log.d(LOG_TAG, "UsimPhoneBookManager readEmailRecord: recNum is " + recNum);
            return null;
        }
        mEmailRecordSize = emailRec.length;
        Log.d(LOG_TAG, "UsimPhoneBookManager readEmailRecord: emailRec.length is " + emailRec.length);
        String email = IccUtils.adnStringFieldToString(emailRec, 0, emailRec.length);
        return email;
    }
   void readRecordSize(int fileId) {
   		synchronized (mReadLock) {
			mPhone.getIccFileHandler().getEFLinearRecordSize(fileId, obtainMessage(EVENT_EMAIL_LENGTH_READ_DONE));
			try {
	            mReadLock.wait();
	        } catch (InterruptedException e) {
	            Log.e(LOG_TAG, "Interrupted Exception in readRecordSize");
	        }
   		}
   }

    private void readAdnFileAndWait(int recNum) {
        //Map <Integer,Integer> fileIds;
       // fileIds = mPbrFile.mFileIds.get(recNum);
       // if (fileIds == null || fileIds.isEmpty()) return;
       //int extEf = 0;
       // Only call fileIds.get while EFEXT1_TAG is available
       //  if (fileIds.containsKey(USIM_EFEXT1_TAG)) {
       //   extEf = fileIds.get(USIM_EFEXT1_TAG);
       //     }

        Log.d(LOG_TAG, "UsimPhoneBookManager readAdnFileAndWait: recNum is " + recNum + "");	
        mAdnCache.requestLoadAllAdnLike(IccConstants.EF_ADN,
 
        mAdnCache.extensionEfForEf(IccConstants.EF_ADN), obtainMessage(EVENT_USIM_ADN_LOAD_DONE)); 

//        int extEf = 0;
//        // Only call fileIds.get while EFEXT1_TAG is available
//        if (fileIds.containsKey(USIM_EFEXT1_TAG)) {
//            extEf = fileIds.get(USIM_EFEXT1_TAG);
//        }

/*       mAdnCache.requestLoadAllAdnLike(fileIds.get(USIM_EFADN_TAG),
            extEf, obtainMessage(EVENT_USIM_ADN_LOAD_DONE));
 
           //read file records by sim_IO  

           */
        try {
            mLock.wait();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
        }
    }

    private void createPbrFile(ArrayList<byte[]> records) {
        if (records == null) {
            mPbrFile = null;
            mIsPbrPresent = false;
            return;
        }
        mPbrFile = new PbrFile(records);
    }

    @Override
    public void handleMessage(Message msg) {
        AsyncResult ar;
        int[] userData = null ;
        switch(msg.what) {
        case EVENT_PBR_LOAD_DONE:
            Log.d(LOG_TAG, "UsimPhoneBookManager handleMessage: EVENT_PBR_LOAD_DONE");		
            ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                createPbrFile((ArrayList<byte[]>)ar.result);
            }
            synchronized (mLock) {
                mLock.notify();
            }
            break;
        case EVENT_USIM_ADN_LOAD_DONE:
            log("Loading USIM ADN records done");
            ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                mPhoneBookRecords.addAll((ArrayList<AdnRecord>)ar.result);
            }
            synchronized (mLock) {
                mLock.notify();
            }
            break;
        case EVENT_GET_ANR_RECORD_SIZE_DONE:
            ar = (AsyncResult)(msg.obj);
            if (ar.exception == null) {
                int[] recordSize = (int[])ar.result;
                // recordSize is int[3] array
                // int[0]  is the record length
                // int[1]  is the total length of the EF file
                // int[2]  is the number of records in the EF file
                // So int[0] * int[2] = int[1]
                if (recordSize.length == 3 ) {
                    mAnrFileSize = recordSize[2];
                    mAnrRecordSize = recordSize[0];
                }else {
                    Log.d(LOG_TAG, "get wrong EF record size format" + ar.exception);
                }

            }else {
                Log.d(LOG_TAG, "get EF record size failed" + ar.exception);
            }
            log("Loading USIM ANR records size done mAnrFileSize:" + mAnrFileSize + ", mAnrRecordSize:" + mAnrRecordSize);
            synchronized (mLock) {
               mLock.notify();
           }
           break;
            case EVENT_ANR_LOAD_DONE:
                int pbrAnr = msg.arg1;
                log("Loading USIM ANR records done " + pbrAnr);
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    mAnrFileRecords = ((ArrayList<byte[]>) ar.result);
                    EfRecord record = (EfRecord) ar.userObj;
                    updatePhoneAdnRecord(mAnrFileRecords, record);
                    if (mAnrFileList == null) {
                        log("mAnrFileList is null !!!! recreat it !");
                        mAnrFileList = new ArrayList<ArrayList<byte[]>>();
                    }
                    try {
                        mAnrFileList.add(pbrAnr, mAnrFileRecords);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                synchronized (mLock) {
                    mLock.notify();
                }
                break;
            case EVENT_ANR_RECORD_LOAD_DONE:
                
                log("Loading USIM ANR record done");
                ar = (AsyncResult) msg.obj;
                userData = (int[]) (ar.userObj);
                IccIoResult result = (IccIoResult) ar.result;

                if (result != null) {
                    IccException iccException = result.getException();

                    if (iccException == null) {
                        log("Loading USIM ANR record done result is "
                                + IccUtils.bytesToHexString(result.payload));
                        updatePhoneAdnRecordWithAnrByIndex(userData[0], userData[1], result.payload);
                    }
                }

                /*
                 * if (ar.exception == null) {
                 * updatePhoneAdnRecordWithAnrByIndex(userData[0], userData[1],
                 * (byte[])ar.result); }
                 */
                mReadingAnrNum.decrementAndGet();
                log("haman, mReadingAnrNum when load done after minus: " + mReadingAnrNum.get());
                if (mReadingAnrNum.get() == 0) {
                    synchronized (mLock) {
                        mLock.notify();
                    }
                }
                break;
            case EVENT_IAP_LOAD_DONE:
                ar = (AsyncResult) msg.obj;
                int pbrIndex = msg.arg1;
                log("Loading USIM IAP records done " + pbrIndex);
                if (ar.exception == null) {
                    mIapFileRecords = ((ArrayList<byte[]>) ar.result);
                    if (mIapFileList == null) {
                        log("IapFileList is null !!!! recreat it !");
                        mIapFileList = new ArrayList<ArrayList<byte[]>>();
                    }
                    try {
                        mIapFileList.add(pbrIndex, mIapFileRecords);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                synchronized (mLock) {
                    mLock.notify();
                }
                break;
            case EVENT_EMAIL_LOAD_DONE:
                int pbr = msg.arg1;
                log("Loading USIM Email records done " + pbr);
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    mEmailFileRecord = ((ArrayList<byte[]>) ar.result);
                    EfRecord record = (EfRecord) ar.userObj;
                    updatePhoneAdnRecord(mEmailFileRecord, record);
                }
                if (mEmailFileList == null) {
                    log("mEmailFileList is null !!!! recreat it !");
                    mEmailFileList = new ArrayList<ArrayList<byte[]>>();
                }
                try {
                    mEmailFileList.add(pbr, mEmailFileRecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                synchronized (mLock) {
                    mLock.notify();
                }
                break;
	 case EVENT_EMAIL_LENGTH_READ_DONE:
			log("Loading USIM Email length done");
            ar = (AsyncResult)(msg.obj);
            if (ar.exception == null) {
                int[] recordSize = (int[])ar.result;
                if (recordSize.length == 3 ) {
		    mEmailFileSize = recordSize[2];
                    mEmailRecordSize = recordSize[0];
                }else {
                    Log.d(LOG_TAG, "get wrong EFEMAIL record size format" + ar.exception);
                }
            }else {
                Log.d(LOG_TAG, "get EF record size failed" + ar.exception);
            }

            synchronized (mReadLock) {
               mReadLock.notify();
           }
			break;
            case EVENT_EMAIL_RECORD_LOAD_DONE:
                log("Loading USIM email record done");
                ar = (AsyncResult) msg.obj;
                userData = (int[]) (ar.userObj);
                IccIoResult em = (IccIoResult) ar.result;

                if (em != null) {
                    IccException iccException = em.getException();

                    if (iccException == null) {
                        log("Loading USIM Email record done result is "
                                + IccUtils.bytesToHexString(em.payload));
                        updatePhoneAdnRecordWithEmailByIndex(userData[0], userData[1], em.payload);
                    }
                }

                mReadingEmailNum.decrementAndGet();
                if (mReadingEmailNum.get() == 0) {
                    synchronized (mLock) {
                        mLock.notify();
                    }
                }
                break;
	 case EVENT_EMAIL_UPDATE_DONE:
            log("Updating USIM Email records done");
            ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                log("Updating USIM Email records successfully!");
            }
            break;		
	 case EVENT_IAP_UPDATE_DONE:
            log("Updating USIM IAP records done");
            ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                log("Updating USIM IAP records successfully!");
            }
            break;
            case EVENT_ANR_UPDATE_DONE:
                log("Updating USIM ANR records done");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    log("Updating USIM ANR records successfully!");
                }
                break;
        case EVENT_GRP_RECORD_LOAD_DONE:
            log("Load USIM GRP record done");
            ar = (AsyncResult) msg.obj;
            userData = (int[])(ar.userObj);            
            
            if(ar.result != null){
                int[] grpIds = (int[]) ar.result;
    
                if (grpIds.length > 0) {
                    log("Load USIM GRP record done result is " );
                    for (int i=0; i<grpIds.length; i++) {
                        log(" " + grpIds[i] + ",");
                    }
                    log("Load USIM GRP record done result is " + grpIds);
                    updatePhoneAdnRecordWithGrpByIndex(userData[0], userData[1], grpIds);
                }   
            }

            mReadingGrpNum.decrementAndGet();
            if(mReadingGrpNum.get() == 0) {   
               synchronized (mLock) {
                   mLock.notify();
               }
            }       
            break;
        case EVENT_UPB_CAPABILITY_QUERY_DONE:
            log("Query UPB capability done");
            ar = (AsyncResult) msg.obj;                       
            if (ar.exception == null) {
                mUpbCap = ((int[])ar.result);
            }           

            synchronized (mLock) {
                mLock.notify();
            }               
            break;
        case EVENT_GAS_LOAD_DONE:
            log("Load UPB GAS done");
            ar = (AsyncResult) msg.obj;                       
            if (ar.exception == null) {
                String[] gasList = ((String[])ar.result);
                if (gasList != null && gasList.length > 0){
                    mGasForGrp = new ArrayList<UsimGroup>();
                    for (int i=0; i<gasList.length; i++){
                        
                    	String gas = decodeGas(gasList[i]);
                        UsimGroup uGasEntry = new UsimGroup(i+1, gas);
                        mGasForGrp.add(uGasEntry);
                        log("Load UPB GAS done i is " + i + ", gas is " + gas);
                    }
                }
            } 			
            synchronized (mGasLock) {
                mGasLock.notify();
            }               
            break;  
        case EVENT_GAS_UPDATE_DONE:
        	log("update UPB GAS done");
        	ar = (AsyncResult) msg.obj;                       
        	if (ar.exception == null) {
        		mResult = 0;
        	}else {               
        		CommandException e = (CommandException)ar.exception;

        		if(e.getCommandError() == CommandException.Error.TEXT_STRING_TOO_LONG) {
        			mResult =  USIM_ERROR_NAME_LEN;
        		}else if(e.getCommandError() == CommandException.Error.SIM_MEM_FULL) {
        			mResult =  USIM_ERROR_GROUP_COUNT;
        		}else{
        			mResult = -1;
        		}
        	}
        	log("update UPB GAS done mResult is " + mResult);
        	synchronized (mGasLock) {
        		mGasLock.notify();
        	}
        	break;
            case EVENT_GRP_UPDATE_DONE:
                log("update UPB GRP done");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    mResult = 0;
                } else {
                    mResult = -1; // todo: set the error code
                }
                synchronized (mLock) {
                    mLock.notify();
                }
                break;
        case EVENT_AAS_LOAD_DONE:
            ar = (AsyncResult) msg.obj;
            int pbrIndexAAS = msg.arg1;
            log("EVENT_AAS_LOAD_DONE done pbr " + pbrIndexAAS);
            if (ar.exception == null) {
                mAasFileRecords = ((ArrayList<byte[]>)ar.result);
            }
            if (mAasFileRecords != null) {
                int size = mAasFileRecords.size();
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < size; i++) {
                    byte[] aas = mAasFileRecords.get(i);
                    if (aas == null) {
                        list.add(null);
                        continue;
                    }
                    String aasAlphaTag = IccUtils.adnStringFieldToString(aas, 0, aas.length);
                    log("AAS[" + i + "]=" + aasAlphaTag + ",byte=" + IccUtils.bytesToHexString(aas));
                    list.add(aasAlphaTag);
                }
                mAasForAnrRec.put(pbrIndexAAS, list);
            }
            
            synchronized (mLock) {
                mLock.notify();
            }
            break;
        case EVENT_AAS_UPDATE_DONE:
            log("EVENT_AAS_UPDATE_DONE done.");
            synchronized (mLock) {
                mLock.notify();
            }
            break;   
        }
    }

    private class PbrFile {
        // RecNum <EF Tag, efid>
        HashMap<Integer,Map<Integer,Integer>> mFileIds;
        ArrayList<EfRecord> mAnrFileids;
        ArrayList<EfRecord> mEmailFileids;
        PbrFile(ArrayList<byte[]> records) {
            mFileIds = new HashMap<Integer, Map<Integer, Integer>>();
            mAnrFileids = new ArrayList<EfRecord>();
            mEmailFileids = new ArrayList<EfRecord>();
            SimTlv recTlv;
            int recNum = 0;
            for (byte[] record: records) {
                recTlv = new SimTlv(record, 0, record.length);
                parseTag(recTlv, recNum);
                recNum ++;
            }
        }

        void parseTag(SimTlv tlv, int recNum) {
            SimTlv tlvEf;
            int tag;
            byte[] data;
            Map<Integer, Integer> val = new HashMap<Integer, Integer>();
            do {
                tag = tlv.getTag();
                switch(tag) {
                case USIM_TYPE1_TAG: // A8
                case USIM_TYPE3_TAG: // AA
                case USIM_TYPE2_TAG: // A9
                    data = tlv.getData();
                    tlvEf = new SimTlv(data, 0, data.length);
                    parseEf(tlvEf, val, tag, recNum);
                    break;
                }
            } while (tlv.nextObject());
            mFileIds.put(recNum, val);
        }

        void parseEf(SimTlv tlv, Map<Integer, Integer> val, int parentTag, int recNum) {
            int tag;
            byte[] data;
            int tagNumberWithinParentTag = 0;
            do {
                tag = tlv.getTag();
                if (parentTag == USIM_TYPE2_TAG && tag == USIM_EFEMAIL_TAG) {
                    mEmailPresentInIap = true;
                    mEmailTagNumberInIap = tagNumberWithinParentTag;
                }
		Log.d(LOG_TAG, "UsimPhoneBookManager parseEf tag is " + tag);	
		  
                switch(tag) {
                    case USIM_EFEMAIL_TAG:
                    case USIM_EFADN_TAG:
                    case USIM_EFEXT1_TAG:
                    case USIM_EFANR_TAG:
                    case USIM_EFPBC_TAG:
                    case USIM_EFGRP_TAG:
                    case USIM_EFAAS_TAG:
                    case USIM_EFGSD_TAG:
                    case USIM_EFUID_TAG:
                    case USIM_EFCCP1_TAG:
                    case USIM_EFIAP_TAG:
                    case USIM_EFSNE_TAG:
                        data = tlv.getData();
                        int efid = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
                        val.put(tag, efid);
                        if (tag == USIM_EFANR_TAG) {
                            EfRecord object = new EfRecord();
                            object.mTag = tag;
                            object.mPbrRecord = recNum;
                            object.mEfTag = efid;
                            object.mType = parentTag;
                            if (parentTag == USIM_TYPE2_TAG) {
                                object.mType2Record = tagNumberWithinParentTag;
                            }
                            mAnrFileids.add(object);
                        }
                        if (tag == USIM_EFEMAIL_TAG) {
                            EfRecord object = new EfRecord();
                            object.mTag = tag;
                            object.mPbrRecord = recNum;
                            object.mEfTag = efid;
                            object.mType = parentTag;
                            if (parentTag == USIM_TYPE2_TAG) {
                                object.mType2Record = tagNumberWithinParentTag;
                            }
                            mEmailFileids.add(object);
                        }
                        break;
                }
                tagNumberWithinParentTag ++;
            } while(tlv.nextObject());
        }
    }
//MTK-START [mtk80601][111215][ALPS00093395]
    private void queryUpbCapablityAndWait(){
        Log.d(LOG_TAG, "UsimPhoneBookManager queryUpbCapablityAndWait begin" ); 
        synchronized(mLock){    
            for (int i=0; i<8; i++){
                mUpbCap[i] = 0;
            }
            mPhone.mCM.queryUPBCapability(obtainMessage(EVENT_UPB_CAPABILITY_QUERY_DONE));
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Interrupted Exception in queryUpbCapablityAndWait");
            }
        }
        Log.d(LOG_TAG, "UsimPhoneBookManager queryUpbCapablityAndWait done:" + 
            "N_Anr is " + mUpbCap[0] + ", N_Email is " + mUpbCap[1] + ",N_Sne is " + mUpbCap[2] +
            ",N_Aas is " + mUpbCap[3] + ", L_Aas is " + mUpbCap[4] + ",N_Gas is " + mUpbCap[5] +
            ",L_Gas is " + mUpbCap[6] + ", N_Grp is " + mUpbCap[7]); 
    }
    private void readGasListAndWait() {
        Log.d(LOG_TAG, "UsimPhoneBookManager readGasListAndWait begin" ); 
        synchronized(mGasLock){
            if(mUpbCap[5] <= 0) {
                Log.d(LOG_TAG, "UsimPhoneBookManager readGasListAndWait no need to read. return" ); 
                return;
            }
            mPhone.mCM.readUPBGasList(1,mUpbCap[5],obtainMessage(EVENT_GAS_LOAD_DONE));
            try {
                mGasLock.wait();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Interrupted Exception in readGasListAndWait");
            }
        }
    }

	private void updatePhoneAdnRecordWithAnrByIndex(int recNum, int index, byte[] anrRecData) {
		Log.d(LOG_TAG, "updatePhoneAdnRecordWithAnrByIndex the " + index + "th anr record is " + IccUtils.bytesToHexString(anrRecData)); 
			/* mantdatory if and only if the file is not type 1*/
			//int adnRecNum = anrRec[anrRec.length - 1];
//f***
		int anrRecLength = anrRecData[1];
        int anrAas = anrRecData[0];
		if (anrRecLength > 0 && anrRecLength <= 11) {  
			String anr = PhoneNumberUtils.calledPartyBCDToString(anrRecData,2, anrRecData[1]);
	   /*		  String anr = IccUtils.bcdToString(anrRecData, 3, anrRecData[1] - 1);
		 if (anrRecData[2] == 0x91){
				anr = "+" + anr;
			}*/
			if (anr != null && !anr.equals("")) { 
                String aas = null;
                if (anrAas > 0 && anrAas != 0xFF) {
                    if (mAasForAnrRec != null) {
                        ArrayList<String> aasList = mAasForAnrRec.get(recNum);
                        if (aasList != null && anrAas <= aasList.size()) {
                            aas = aasList.get(anrAas - 1);
                        }
                    }
                }
			 Log.d(LOG_TAG, " updatePhoneAdnRecordWithAnrByIndex " + index + " th anr is " + anr);		
				   // SIM record numbers are 1 based. 
				AdnRecord rec = mPhoneBookRecords.get(index);
				rec.setAnr(anr);
                if (aas != null && aas.length() > 0) {
                    rec.setAasIndex(anrAas);
                }
				mPhoneBookRecords.set(index, rec);	 
			}
		}
	}

    public ArrayList<UsimGroup> getUsimGroups() {
    	Log.d(LOG_TAG, "UsimPhoneBookManager getUsimGroups " ); 
    	synchronized (mGasLock) {
    		if (!mGasForGrp.isEmpty()) return mGasForGrp;
    	}

    	queryUpbCapablityAndWait(); 
    	readGasListAndWait();

    	return mGasForGrp;
    }

    public String getUSIMGroupById(int nGasId){
        String grpName = null;
        Log.d(LOG_TAG, "UsimPhoneBookManager getUSIMGroupById nGasId is " + nGasId);
        if (mGasForGrp != null && nGasId < mGasForGrp.size()) {
            UsimGroup uGas = mGasForGrp.get(nGasId - 1);
            if (uGas != null){
                grpName = uGas.getAlphaTag();
                Log.d(LOG_TAG, "getUSIMGroupById index is " + uGas.getRecordIndex() + 
                ", name is " + grpName);    
            }
        }
        Log.d(LOG_TAG, "UsimPhoneBookManager getUSIMGroupById grpName is " + grpName);  
        return grpName;
    }

    public synchronized boolean removeUSIMGroupById(int nGasId){
        boolean ret = false;
        Log.d(LOG_TAG, "UsimPhoneBookManager removeUSIMGroupById nGasId is " + nGasId);
        synchronized (mGasLock){ 

            if (mGasForGrp == null || nGasId > mGasForGrp.size()) {
                Log.d(LOG_TAG, "UsimPhoneBookManager removeUSIMGroupById fail ");   
            } else {
                UsimGroup uGas = mGasForGrp.get(nGasId - 1);
                Log.d(LOG_TAG, " removeUSIMGroupById index is " + uGas.getRecordIndex());
                if (uGas != null && uGas.getAlphaTag() != null) {
                    mPhone.mCM.deleteUPBEntry(UPB_EF_GAS , 0, nGasId,obtainMessage(EVENT_GAS_UPDATE_DONE));
                    try {
                        mGasLock.wait();
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, "Interrupted Exception in removeUSIMGroupById");
                    }
                }else {
                    Log.d(LOG_TAG, "UsimPhoneBookManager removeUSIMGroupById fail: this gas doesn't exist ");   
                }
                if (mResult == 0){
                    ret = true;
                    uGas.setAlphaTag(null);
                    mGasForGrp.set(nGasId - 1, uGas);
                }
            }
        }
        Log.d(LOG_TAG, "UsimPhoneBookManager removeUSIMGroupById result is " + ret);   
        return ret;
    }   
    private String decodeGas(String srcGas){
    	if(srcGas==null)
    		return null;
    	
    	String retGas = null;
    	byte[] ba = IccUtils.hexStringToBytes(srcGas);   
    	if(ba == null){
    		Log.e(LOG_TAG, "gas string is null");
    		return retGas;
    	}
    	
    	try {
    		retGas = new String(ba, 0, srcGas.length()/2, "utf-16be");
    	} catch (UnsupportedEncodingException ex) {
    		Log.e(LOG_TAG, "implausible UnsupportedEncodingException",ex);
    	}
    	return retGas;
    }	
	
    private String encodeATUCS2(String input)
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
    public synchronized int insertUSIMGroup(String grpName){
        int index = -1;
        Log.d(LOG_TAG, "UsimPhoneBookManager insertUSIMGroup grpName is " + grpName);
        synchronized (mGasLock){  
            if (mGasForGrp == null ) {
                Log.d(LOG_TAG, "UsimPhoneBookManager insertUSIMGroup fail ");   
            } else {
                UsimGroup gasEntry = null;
                int i = 0;
                for (i = 0; i < mGasForGrp.size(); i++){
                    gasEntry = mGasForGrp.get(i);
                    if (gasEntry != null && gasEntry.getAlphaTag() == null) {
                        index = gasEntry.getRecordIndex();
                        Log.d(LOG_TAG, "UsimPhoneBookManager insertUSIMGroup index is " + index);
                        break;
                    }
                }
                if (index < 0){
                    Log.d(LOG_TAG, "UsimPhoneBookManager insertUSIMGroup fail: gas file is full."); 
    				index = USIM_ERROR_GROUP_COUNT;        //too many groups
                    return index;
                }
                String temp = encodeATUCS2(grpName);
                mPhone.mCM.editUPBEntry(UPB_EF_GAS , 0, index,temp,null, obtainMessage(EVENT_GAS_UPDATE_DONE));
                try {
                    mGasLock.wait();
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG, "Interrupted Exception in insertUSIMGroup");
                }   
    
                if (mResult < 0){
                    Log.e(LOG_TAG, "result is negative. insertUSIMGroup");
                    return mResult;
                } else {
                    gasEntry.setAlphaTag(grpName);
                    mGasForGrp.set(i, gasEntry);
                }
                
            }
        }
        return index;
    }

    public synchronized int updateUSIMGroup(int nGasId, String grpName){
    	int ret = -1;
        Log.d(LOG_TAG, "UsimPhoneBookManager updateUSIMGroup nGasId is " + nGasId);

        synchronized (mGasLock){
            if (mGasForGrp == null || nGasId > mGasForGrp.size()) {
                Log.d(LOG_TAG, "UsimPhoneBookManager updateUSIMGroup fail ");   
            } else if (grpName != null) {
                String temp = encodeATUCS2(grpName);
                mPhone.mCM.editUPBEntry(UPB_EF_GAS , 0, nGasId, temp,null,obtainMessage(EVENT_GAS_UPDATE_DONE));
                try {
                    mGasLock.wait();
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG, "Interrupted Exception in updateUSIMGroup");
                }           
            }
            if (mResult == 0){
    			ret = nGasId;
                UsimGroup uGasEntry = mGasForGrp.get(nGasId - 1);
                if(uGasEntry != null){
                    Log.d(LOG_TAG, "updateUSIMGroup index is " + uGasEntry.getRecordIndex());
                    uGasEntry.setAlphaTag(grpName);
                }else {
                    Log.d(LOG_TAG, "updateUSIMGroup the entry doesn't exist ");
                }
    		}
    		else{
    			ret = mResult;
    		}
        }
        return ret;

    }
    public boolean addContactToGroup(int adnIndex, int grpIndex){
        boolean ret = false;
        Log.d(LOG_TAG, "UsimPhoneBookManager addContactToGroup adnIndex is " +
                adnIndex + " to grp " + grpIndex);
        if (mPhoneBookRecords == null || adnIndex <= 0 || adnIndex > mPhoneBookRecords.size()) {
                Log.e(LOG_TAG, "UsimPhoneBookManager addContactToGroup no records or invalid index.");
                return false;
        }
        synchronized (mLock){

            AdnRecord rec = mPhoneBookRecords.get(adnIndex - 1);

            if (rec != null) {
                Log.d(LOG_TAG, " addContactToGroup the adn index is " + rec.getRecordIndex()
                             + " old grpList is " + rec.getGrpIds());
                String grpList = rec.getGrpIds();
                boolean bExist = false;
                int nOrder = -1;
                int grpCount = mUpbCap[7];              
                int[] grpIdArray = new int[grpCount];
                for (int i = 0; i < grpCount; i++){
                    grpIdArray[i] = 0;
                }
                if (grpList != null) {
                    String[] grpIds = rec.getGrpIds().split(",");                   
                    for (int i = 0; i < grpIds.length; i++){
                        grpIdArray[i] = Integer.parseInt(grpIds[i]);
                        if (grpIndex == grpIdArray[i]){
                            bExist = true;
                            Log.d(LOG_TAG, " addContactToGroup the adn is already in the group. i is " + i);
                            break;  
                        }
        
                        if(grpIdArray[i] == 0 || grpIdArray[i] == 255) {
                            nOrder = i;
                            Log.d(LOG_TAG, 
                            " addContactToGroup found an unsed position in the group list. i is " + i);
                        }
                    }

                }else {
                    nOrder = 0;
                }
                
                if (!bExist && nOrder >= 0) {
                    grpIdArray[nOrder] = grpIndex;
                    mPhone.mCM.writeUPBGrpEntry(adnIndex, grpIdArray,obtainMessage(EVENT_GRP_UPDATE_DONE));
                    try {
                        mLock.wait();
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, "Interrupted Exception in addContactToGroup");
                    }
                    if (mResult == 0){
                        ret = true;
                        updatePhoneAdnRecordWithGrpByIndex(adnIndex - 1, adnIndex, grpIdArray);
                        Log.d(LOG_TAG, " addContactToGroup the adn index is " + rec.getRecordIndex());
                        mResult = -1;
                    }
                }
            }
        }
        return ret;
    }

    public synchronized boolean removeContactFromGroup(int adnIndex, int grpIndex){
        boolean ret = false;
        Log.d(LOG_TAG, "UsimPhoneBookManager removeContactFromGroup adnIndex is " +
                adnIndex + " to grp " + grpIndex);
        
        synchronized (mLock){
            if (mPhoneBookRecords == null){
                return false;
            }
            AdnRecord rec = mPhoneBookRecords.get(adnIndex -1);
            if (rec != null) {
                String grpList = rec.getGrpIds();
                if (grpList == null) {
                    Log.d(LOG_TAG, " the adn is not in any group. ");
                    return false;
                }
                String[] grpIds = grpList.split(",");
                boolean bExist = false;
                int nOrder = -1;
                int[] grpIdArray = new int[grpIds.length];
                for (int i = 0; i < grpIds.length; i++){
                    grpIdArray[i] = Integer.parseInt(grpIds[i]);
                    if (grpIndex == grpIdArray[i]){
                        bExist = true;
                        nOrder = i;
                        Log.d(LOG_TAG, " removeContactFromGroup the adn is in the group. i is " + i);                   
                    }
                }
    
                if (bExist && nOrder >= 0) {
                    grpIdArray[nOrder] = 0;
                    mPhone.mCM.writeUPBGrpEntry(adnIndex, grpIdArray,obtainMessage(EVENT_GRP_UPDATE_DONE));
                    try {
                        mLock.wait();
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, "Interrupted Exception in removeContactFromGroup");
                    }
                    if (mResult == 0){
                        ret = true;
                        updatePhoneAdnRecordWithGrpByIndex(adnIndex - 1, adnIndex, grpIdArray);
                        mResult = -1;
                    }
                }else {
                    Log.d(LOG_TAG, " removeContactFromGroup the adn is not in the group. ");                    
                }           
            }
        }
        return ret;
    }

    public int hasExistGroup(String grpName){
        int grpId = -1;
        Log.d(LOG_TAG, "UsimPhoneBookManager hasExistGroup grpName is " + grpName);
        if (grpName == null) {
            return grpId;
        }
        if (mGasForGrp != null && mGasForGrp.size() > 0) {
            for (int i=0; i < mGasForGrp.size(); i++){              
                UsimGroup uGas = mGasForGrp.get(i);
                if (uGas != null && grpName.equals(uGas.getAlphaTag())){                
                    Log.d(LOG_TAG, "getUSIMGroupById index is " + uGas.getRecordIndex() + 
                    ", name is " + grpName);  
                    grpId = uGas.getRecordIndex();
                    break;
                }       
            }

        }
        Log.d(LOG_TAG, "UsimPhoneBookManager hasExistGroup grpId is " + grpId);  
        return grpId;       
    }

    public int getUSIMGrpMaxNameLen(){
    	int ret = -1;

    	Log.d(LOG_TAG, "UsimPhoneBookManager getUSIMGrpMaxNameLen begin" ); 
    	synchronized(mLock){    
    		for (int i=0; i<8; i++){
    			mUpbCap[i] = 0;
    		}
    		mPhone.mCM.queryUPBCapability(obtainMessage(EVENT_UPB_CAPABILITY_QUERY_DONE));
    		try {
    			mLock.wait();
    		} catch (InterruptedException e) {
    			Log.e(LOG_TAG, "Interrupted Exception in getUSIMGrpMaxNameLen");
    		}
    	}
    	ret = mUpbCap[6];
    	Log.d(LOG_TAG, "UsimPhoneBookManager getUSIMGrpMaxNameLen done: " + "L_Gas is " + mUpbCap[6]); 

    	return ret;
    }

    public int getUSIMGrpMaxCount(){
    	int ret = -1;

    	Log.d(LOG_TAG, "UsimPhoneBookManager getUSIMGrpMaxCount begin" ); 
    	synchronized(mLock){    
    		for (int i=0; i<8; i++){
    			mUpbCap[i] = 0;
    		}
    		mPhone.mCM.queryUPBCapability(obtainMessage(EVENT_UPB_CAPABILITY_QUERY_DONE));
    		try {
    			mLock.wait();
    		} catch (InterruptedException e) {
    			Log.e(LOG_TAG, "Interrupted Exception in getUSIMGrpMaxCount");
    		}
    	}

    	ret = mUpbCap[5];
    	Log.d(LOG_TAG, "UsimPhoneBookManager getUSIMGrpMaxCount done: " + "N_Gas is " + mUpbCap[5]); 
    	return ret;
    }
//MTK-END [mtk80601][111215][ALPS00093395]
    private void log(String msg) {
        if(DBG) Log.d(LOG_TAG, msg);
    }
	
    public void updateAnrByAdnIndex(String anr, int adnIndex) {
        Map<Integer, Integer> fileIds;

        int pbrRecNum = (adnIndex - 1) / mAdnFileSize;
        int anrRecNum = (adnIndex - 1) % mAdnFileSize;
        if (mPbrFile == null) return;
        fileIds = mPbrFile.mFileIds.get(pbrRecNum); // support 1 pbr records in first phase
        if (fileIds == null || fileIds.isEmpty())
            return;

        if (!fileIds.containsKey(USIM_EFANR_TAG)) {
            Log.d(LOG_TAG, "UsimPhoneBookManager updateAnrByAdnIndex: No anr tag in pbr record 0");
            return;
        }

        int efid = fileIds.get(USIM_EFANR_TAG);
        Log.d(LOG_TAG, "UsimPhoneBookManager updateAnrByAdnIndex: recNum is 0 " + " EFANR id is " + efid);

        EfRecord efrecord = null;
        for (EfRecord record : mPbrFile.mAnrFileids) {
            if (record.mEfTag == efid && record.mPbrRecord == pbrRecNum) {
                efrecord = record;
                break;
            }
        }
        if (efrecord == null) return;
        log("updateAnrByAdnIndex efrecord " + efrecord);
        if (efrecord.mType == USIM_TYPE2_TAG) {
            updateType2Anr(anr, adnIndex, efrecord);
            return;
        }
        // to do build anr record data from anr string
		 AdnRecord rec = mPhoneBookRecords.get(adnIndex - 1);
		 int aas = rec.getAasIndex();
        byte[] data = buildAnrRecord(anr, mAnrRecordSize, aas);
        if (data != null) {
            mPhone.getIccFileHandler().updateEFLinearFixed(efid, anrRecNum + 1, data, null,
                    obtainMessage(EVENT_ANR_UPDATE_DONE));
        }
    }
    
   private int getRecNum(String[] emails, int pbrRecNum, int nIapRecNum, byte[] iapRec, int tagNum) {
		boolean hasEmail = false;
		if(null == emails){
			
			if(iapRec[mEmailTagNumberInIap] != 255 && iapRec[mEmailTagNumberInIap] > 0)
				mEmailRecTable[iapRec[mEmailTagNumberInIap]-1] = 0;
			return -1;
		}
		for(int i = 0; i < emails.length; i++) {
			if(null != emails[i] && !emails[i].equals("")) {
				hasEmail = true;
				break;
			}
		}
		if(!hasEmail) {
			if(iapRec[mEmailTagNumberInIap] != 255 && iapRec[mEmailTagNumberInIap] > 0)
				mEmailRecTable[iapRec[mEmailTagNumberInIap]-1] = 0;
			return -1;
		}
		int recNum = iapRec[tagNum];
		if (recNum > mEmailFileSize || recNum == 255 || recNum == -1) {  //no email record before  find 
          //find a index to save the email and update iap record. 
           int nOffset = pbrRecNum * mEmailFileSize;
           for (int i = nOffset; i < nOffset + mEmailFileSize; i++) {
               Log.d(LOG_TAG, "updateEmailsByAdnIndex: mEmailRecTable[" + i +"] is " + mEmailRecTable[i]);
               if ( mEmailRecTable[i] == 0) {
                   recNum = i + 1 - nOffset;
                   mEmailRecTable[i] = nIapRecNum;
                   break;    
               }
           }
       }
		if(recNum > mEmailFileSize) recNum = 255;	
		if(recNum == -1) return -2;
		return recNum;
   }	

   public boolean checkEmailCapacityFree(int adnIndex, String[] emails) {
   	int pbrRecNum = (adnIndex-1)/mAdnFileSize;
   	int nOffset = pbrRecNum * mEmailFileSize;
	boolean hasEmail = false;
	if(null == emails){
		return true;
	}
	for(int i = 0; i < emails.length; i++) {
		if(null != emails[i] && !emails[i].equals("")) {
			hasEmail = true;
			break;
		}
	}
	if(!hasEmail) {
		return true;
	}
		
	for (int i = nOffset; i < nOffset + mEmailFileSize; i++) {
		if ( mEmailRecTable[i] == 0) {
			return true;
		}
	}
	return false;
   }
	public boolean checkEmailLength(String[] emails){
	    if(emails != null && emails[0] != null){
	    	byte[] eMailData = GsmAlphabet.stringToGsm8BitPacked(emails[0]);
			if((mEmailRecordSize != -1) &&  (eMailData.length > mEmailRecordSize)) return false;
	    }	
	    return true; 
   }
    public int updateEmailsByAdnIndex(String[] emails, int adnIndex) {
        // to do : Check the type of Emails file in pbr. and get the index in IAP if it is type2.
        // if(emails == null) return;
        int pbrRecNum = (adnIndex - 1) / mAdnFileSize;
        int nIapRecNum = (adnIndex - 1) % mAdnFileSize;
        Map<Integer, Integer> fileIds;
        if (mPbrFile == null)
            return 0;
        fileIds = mPbrFile.mFileIds.get(pbrRecNum);
        if (fileIds == null || fileIds.isEmpty())
            return 0;

        if (!fileIds.containsKey(USIM_EFEMAIL_TAG)) {
            Log.d(LOG_TAG, "UsimPhoneBookManager updateEmailsByAdnIndex: No email tag in pbr record 0");
            return 0;
        }

        int efid = fileIds.get(USIM_EFEMAIL_TAG);
        Log.d(LOG_TAG, "UsimPhoneBookManager updateEmailsByAdnIndex: pbrrecNum is " + pbrRecNum + " EFEMAIL id is " + efid);

        if (mEmailPresentInIap) {
//            EfRecord efrecord = null;
//            for (EfRecord record : mPbrFile.mEmailFileids) {
//                if (record.mEfTag == efid) {
//                    efrecord = record;
//                    break;
//                }
//            }
//            String email = null;
//            if (emails != null && emails.length > 0) {
//                email = emails[0];
//            }
//            updateType2Anr(email, adnIndex, efrecord);
            
            // to do get iap record by index
            // if the index is valid, update the email record

            byte[] iapRec = null;
            try {
                ArrayList<byte[]> iapFile = mIapFileList.get(pbrRecNum);
                iapRec = iapFile.get(nIapRecNum);
            } catch (Exception e) {
                e.printStackTrace();
                // Log.e(LOG_TAG, "exception occured when update anr and email " + e);
                return 0;
            }

            int recNum = getRecNum(emails, pbrRecNum, nIapRecNum + 1, iapRec, mEmailTagNumberInIap);
            Log.d(LOG_TAG, "UsimPhoneBookManager updateEmailsByAdnIndex: Email recNum is " + recNum);
            if (-2 == recNum) {
                return -1;
            }

            Log.d(LOG_TAG, "updateEmailsByAdnIndex: found Email recNum is " + recNum);
            iapRec[mEmailTagNumberInIap] = (byte) recNum;
            efid = fileIds.get(USIM_EFIAP_TAG);
            mPhone.getIccFileHandler().updateEFLinearFixed(efid, nIapRecNum + 1, iapRec, null,
                    obtainMessage(EVENT_IAP_UPDATE_DONE));

            // ???
            if ((recNum != 255) && (recNum != -1)) {
                String eMailAd = null;
                if (emails != null) {
                    try {
                        eMailAd = emails[0];
                    } catch (IndexOutOfBoundsException e) {
                        Log.e(LOG_TAG, "Error: updateEmailsByAdnIndex no email address, continuing");
                    }
                    if (-1 == mEmailRecordSize)
                        return 0;
                    byte[] eMailRecData = buildEmailRecord(eMailAd, nIapRecNum, mEmailRecordSize); 
                    // to be replaced by the record size
                    efid = fileIds.get(USIM_EFEMAIL_TAG);
                    mPhone.getIccFileHandler().updateEFLinearFixed(efid, recNum, eMailRecData,
                            null, obtainMessage(EVENT_EMAIL_UPDATE_DONE));
                }
            }
            
        } else {
            EfRecord efrecord = null;
            for (EfRecord record : mPbrFile.mEmailFileids) {
                if (record.mEfTag == efid && record.mPbrRecord == pbrRecNum) {
                    efrecord = record; 
                    break;
                }
            }
            log("updateEmailsByAdnIndex record: " + efrecord);
            if (efrecord == null) return 0;
            if (efrecord.mType == USIM_TYPE2_TAG) return 0;
            //handle type1 email
            //if (emails == null || emails.length <= 0) return 0;
            String email = (emails == null || emails.length <= 0)? null : emails[0];
            byte[] data = buildEmailRecord(email, nIapRecNum, mEmailRecordSize);
            log("updateEmailsByAdnIndex build type1 email record:" + IccUtils.bytesToHexString(data));
            if (data != null) {
                mPhone.getIccFileHandler().updateEFLinearFixed(efid, nIapRecNum+1, data, null, obtainMessage(EVENT_EMAIL_UPDATE_DONE));
            }
        }
        return 0;
    }
	
    private byte[] buildAnrRecord(String anr, int recordSize, int aas) {
        log("buildAnrRecord anr:" + anr + ",recordSize:"+ recordSize + ",aas:" + aas);
        if (recordSize <= 0) {
            readAnrRecordSize();
        }
        recordSize = mAnrRecordSize;
        log("buildAnrRecord recordSize:"+ recordSize);
        byte[] bcdNumber;
        byte[] byteTag;
        byte[] anrString;
        anrString = new byte[recordSize];
        for (int i = 0; i < recordSize; i++) {
            anrString[i] = (byte) 0xFF;
        }

        if (TextUtils.isEmpty(anr)) {
            Log.w(LOG_TAG, "[buildAdnString] Empty dialing number");
            return anrString; // return the empty record (for delete)
        } else if (anr.length() > (13 - 4 + 1) * 2) {
            Log.w(LOG_TAG,
                    "[buildAnrRecord] Max length of dialing number is 20");
            return null;
        } else {
            bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(anr);
            if (bcdNumber != null) {
                anrString[0] = (byte) aas;
                System.arraycopy(bcdNumber, 0, anrString,
                        2, bcdNumber.length);
                anrString[1] = (byte) (bcdNumber.length);
                // anrString[2] = (byte) 0x81;
            }
            return anrString;
        }

    }
    private byte[] buildEmailRecord(String strEmail, int adnIndex,  int recordSize) {

        byte[] eMailRecData = new byte[recordSize];  // to be replaced by the record size    
        for (int i = 0; i < recordSize; i++) {
               eMailRecData[i] = (byte) 0xFF;
        }
        if(strEmail != null && !strEmail.equals("")) {
            byte[] eMailData = GsmAlphabet.stringToGsm8BitPacked(strEmail);
	    if(eMailData.length > eMailRecData.length) return null;
            System.arraycopy(eMailData, 0, eMailRecData, 0, eMailData.length);
            //eMailRecData[eMailData.length] = 0x02;   // adn file SFI
            //eMailRecData[eMailData.length + 1] = (byte)adnIndex;  // to be mod by 250
        }
        return eMailRecData;
    }	
    public void updateUsimPhonebookRecordsList(int index, AdnRecord newAdn){
        Log.d(LOG_TAG, "updateUsimPhonebookRecordsList update the " + index + "th record.");
        if (index < mPhoneBookRecords.size()){
            mPhoneBookRecords.set(index, newAdn); 
        }
    }
	
    private void updatePhoneAdnRecordWithGrpByIndex(int recIndex, int adnIndex, int[] grpIds){
         Log.d(LOG_TAG, "updatePhoneAdnRecordWithGrpByIndex the " + recIndex + "th grp " ); 
		 if(recIndex > mPhoneBookRecords.size())
		 	return;

         int grpSize = grpIds.length;
              
         if (grpSize > 0) { 
             AdnRecord rec = mPhoneBookRecords.get(recIndex);
             Log.d(LOG_TAG, "updatePhoneAdnRecordWithGrpByIndex the adnIndex is " + adnIndex + "; the original index is " + rec.getRecordIndex()); 
             StringBuilder grpIdsSb = new StringBuilder();

             for (int i = 0; i < grpSize -1; i++) {
                 grpIdsSb.append(grpIds[i]);
                 grpIdsSb.append(",");
             }
             grpIdsSb.append(grpIds[grpSize-1]);
      
             rec.setGrpIds(grpIdsSb.toString());
             
             Log.d(LOG_TAG, "updatePhoneAdnRecordWithGrpByIndex grpIds is " + grpIdsSb.toString()); 
             mPhoneBookRecords.set(recIndex, rec);   

         }

    }
	    private void updatePhoneAdnRecordByEmailFile(int nPbrRecNum) {
        if (mEmailFileRecord == null) return;
		int nOffset = nPbrRecNum * mEmailFileSize;
		
        for (int i = nOffset; i < nOffset + mEmailFileSize; i++ ) {
            try {
                mEmailRecTable[i] = 0; 
            } catch (ArrayIndexOutOfBoundsException e){
                Log.d(LOG_TAG, "UsimPhoneBookManager updatePhoneAdnRecordByEmailFile: mEmailRecTable ArrayIndexOutOfBoundsException, nPbrRecNum is " + nPbrRecNum); 
                break;
            }
         }
	    
        if (mIapFileRecords != null) {
            // The number of records in the IAP file is same as the number of records in ADN file.
            // The order of the pointers in an EFIAP shall be the same as the order of file IDs
            // that appear in the TLV object indicated by Tag 'A9' in the reference file record.
            // i.e value of mEmailTagNumberInIap
            int numAdnRecs = mPhoneBookRecords.size();
            int nAdnOffset = nPbrRecNum * mAdnFileSize;
            int nMax = nOffset + mAdnFileSize;
            if (numAdnRecs < nMax) {
                nMax = numAdnRecs;
            }
            Log.d(LOG_TAG, "UsimPhoneBookManager updatePhoneAdnRecordByEmailFile:  nAdnOffset is " + numAdnRecs + "nMax is " + nMax );  
            for (int i = nAdnOffset; i < nMax; i++) {
                AdnRecord rec;
                try {
                    rec = mPhoneBookRecords.get(i); 
                } catch (IndexOutOfBoundsException e){
                    Log.d(LOG_TAG, "UsimPhoneBookManager updatePhoneAdnRecordByEmailFile: mPhoneBookRecords IndexOutOfBoundsException numAdnRecs is " + numAdnRecs + "index is " + i ); 
                    return;
                }
                if (rec != null && (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0 )){
				
                    byte[] record = null;				
                    try {
                        record = mIapFileRecords.get(i - nAdnOffset);
                    } catch (IndexOutOfBoundsException e) {
                        Log.e(LOG_TAG, "Error: updatePhoneAdnRecord : No IAP record for ADN " + i +" , continuing");
                        break;
                    }
                    int recNum = record[mEmailTagNumberInIap];
                    log("updatePhoneAdnRecordByEmailFile " + rec.getAlphaTag()  +",recNum[" + i + "]=" + recNum);
                    if (recNum > 0 && recNum <= mEmailFileSize) {
                        String[] emails = new String[1];
                        // SIM record numbers are 1 based
                        emails[0] = readEmailRecord(recNum - 1);
                        try {
							mEmailRecTable[nPbrRecNum * mEmailFileSize + recNum - 1] = i+1;
                        } catch (IndexOutOfBoundsException e) {
                            Log.e(LOG_TAG, "Error: updatePhoneAdnRecord : Email record index out of table storage " + recNum +" , continuing");
                            continue;
                        }

                        rec.setEmails(emails);
                        mPhoneBookRecords.set(i, rec); 
                    }
                }
            }
        }

        // ICC cards can be made such that they have an IAP file but all
        // records are empty. So we read both type 1 and type 2 file
        // email records, just to be sure.
/*
        int len = mPhoneBookRecords.size();
        // Type 1 file, the number of records is the same as the number of
        // records in the ADN file.
        if (mEmailsForAdnRec == null) {
            parseType1EmailFile(len);
        }
        for (int i = 0; i < numAdnRecs; i++) {
            ArrayList<String> emailList = null;
            try {
                emailList = mEmailsForAdnRec.get(i);
            } catch (IndexOutOfBoundsException e) {
                break;
            }
            if (emailList == null) continue;

            AdnRecord rec = mPhoneBookRecords.get(i);

            String[] emails = new String[emailList.size()];
            System.arraycopy(emailList.toArray(), 0, emails, 0, emailList.size());
            rec.setEmails(emails);
            mPhoneBookRecords.set(i, rec);
        }  */
    }
        
        
    private void readType1Ef(EfRecord record) {
        log("readType1Ef:" + record);
        if (record.mType != USIM_TYPE1_TAG) {
            return;
        }
        int pbrIndex = record.mPbrRecord;
        int numAdnRecs = mPhoneBookRecords.size();
        int nOffset = pbrIndex * mAdnFileSize;
        int nMax = nOffset + mAdnFileSize;
        if (numAdnRecs < nMax) {
            nMax = numAdnRecs;
        }
        IccFileHandler iccFh = mPhone.getIccFileHandler();
        int what = 0;
        switch (record.mTag) {
            case USIM_EFANR_TAG:
                what = EVENT_GET_ANR_RECORD_SIZE_DONE;
                Message msg = obtainMessage(what);
                if (iccFh != null) {
                    iccFh.getEFLinearRecordSize(record.mEfTag, msg);
                } else {
                    return;
                }
                synchronized (mLock) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, "Interrupted Exception in readType1Ef");
                    }
                }
                break;
            case USIM_EFEMAIL_TAG:
                what = EVENT_EMAIL_LENGTH_READ_DONE;
                readRecordSize(record.mEfTag);
                break;
        }
        
        int recordSize = 0;
        switch (record.mTag) {
            case USIM_EFANR_TAG:
                recordSize = mAnrRecordSize;
                break;
            case USIM_EFEMAIL_TAG:
                recordSize = mEmailRecordSize;
                break;
        }
        if (recordSize == 0) {
            Log.d(LOG_TAG, "UsimPhoneBookManager readType1Ef: recordSize is 0. ");
            return;
        }
        for (int i = nOffset; i < nMax; i++) {
            AdnRecord rec;
            try {
                rec = mPhoneBookRecords.get(i);
            } catch (IndexOutOfBoundsException e) {
                Log.d(LOG_TAG, "UsimPhoneBookManager readType1Ef: mPhoneBookRecords IndexOutOfBoundsException numAdnRecs is "
                                        + numAdnRecs + "index is " + i);
                break;
            }
            if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0) {
                int[] data = new int[2];
                data[0] = record.mPbrRecord;
                data[1] = i;
                int loadWhat = 0;
                switch(record.mTag) {
                    case USIM_EFANR_TAG:
                        loadWhat = EVENT_ANR_RECORD_LOAD_DONE;
                        break;
                    case USIM_EFEMAIL_TAG:
                        loadWhat = EVENT_EMAIL_RECORD_LOAD_DONE;
                        break;
                        default:
                            Log.e(LOG_TAG, "not support tag " + record.mTag);
                            break;
                }
                iccFh.readEFLinearFixed(record.mEfTag, i + 1 - nOffset, recordSize, obtainMessage(
                        loadWhat, data));
                int simid = mPhone.getMySimId();
                switch (record.mTag) {
                    case USIM_EFANR_TAG:
                        mReadingAnrNum.addAndGet(1);
                        Log.d(LOG_TAG, "[" + simid + "]" + " UsimPhoneBookManager readType1Ef: read for  " + i
                                + " adn " + "( " + rec.getAlphaTag() + ", " + rec.getNumber() + " )  mReadingAnrNum is " + mReadingAnrNum.get());
                        break;
                    case USIM_EFEMAIL_TAG:
                        mReadingEmailNum.incrementAndGet();
                        Log.d(LOG_TAG, "[" + simid + "]" + " UsimPhoneBookManager readType1Ef: read for  " + i
                                + " adn " + "( " + rec.getAlphaTag() + ", " + rec.getNumber() + " )  mReadingEmailNum is " + mReadingEmailNum.get());
                        break;
                    default:
                        Log.e(LOG_TAG, "not support tag " + record.mTag);
                        break;
                }
            }
        }
        switch (record.mTag) {
            case USIM_EFANR_TAG:
                if (mReadingAnrNum.get() == 0) {
                    return;
                }
                break;
            case USIM_EFEMAIL_TAG:
                if (mReadingEmailNum.get() == 0) {
                    return;
                }
                break;
            default:
                Log.e(LOG_TAG, "not support tag " + record.mTag);
                break;
        }
        synchronized (mLock) {
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Interrupted Exception in readType1Ef");
            }
        }
    }
    
    private void readType2Ef(EfRecord record) {
        log("readType2Ef:" + record);
        if (record.mType != USIM_TYPE2_TAG) {
            return;
        }
        
        Map <Integer,Integer> fileIds = mPbrFile.mFileIds.get(record.mPbrRecord);
        readIapFileAndWait(record.mPbrRecord, fileIds.get(USIM_EFIAP_TAG));
        if (mIapFileRecords == null) {
            Log.e(LOG_TAG, "Error: IAP file is empty");
            return;
        }
        int what = 0;
        switch (record.mTag) {
            case USIM_EFANR_TAG:
                what = EVENT_ANR_LOAD_DONE;
                break;
            case USIM_EFEMAIL_TAG:
                what = EVENT_EMAIL_LOAD_DONE;
                break;
                default:
                    //TODO handle other TAG
                    log("no implement type2 EF " + record.mTag);
                    return;
        }
        Message msg = obtainMessage(what, record);
        msg.arg1 = record.mPbrRecord;
        mPhone.getIccFileHandler().loadEFLinearFixedAll(record.mEfTag, msg);
        synchronized (mLock) {
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Interrupted Exception in readAnrFileAndWait");
            }
        }
    }
    
    private void updatePhoneAdnRecord(ArrayList<byte[]> fileRecords, EfRecord efrecord) {
        log("updatePhoneAdnRecord " + efrecord);
        if (efrecord == null) return;
        int numAdnRecs = mPhoneBookRecords.size();
        int pbrIndex = efrecord.mPbrRecord;
        int nOffset = pbrIndex * mAdnFileSize;
        int nMax = nOffset + mAdnFileSize;
        if (numAdnRecs < nMax) {
            nMax = numAdnRecs;
        }
        int simid = mPhone.getMySimId();
        log("updatePhoneAdnRecord offset:" + nOffset + ",nMax:" + nMax + ", mAdnFileSize:" + mAdnFileSize);
        //TODO for email we should update mEmailRecTable
        if (efrecord.mType == USIM_TYPE2_TAG) {
            for (int i = nOffset; i < nMax; i++) {
                AdnRecord rec = mPhoneBookRecords.get(i);
                if ((rec.getAlphaTag() == null || rec.getAlphaTag().length() == 0) 
                        && (rec.getNumber() == null || rec.getNumber().length() == 0)) {
                    continue;
                }
                Log.d(LOG_TAG, " updatePhoneAdnRecord " + i + "th " + rec.getAlphaTag() + ", " + rec.getNumber());
                byte[] record = null;
                try {
                    if (mIapFileList == null){
                        Log.e(LOG_TAG, "updatePhoneAdnRecord mIapFileList = null");
                        return;
                    }
                    ArrayList<byte[]> iaplist = mIapFileList.get(pbrIndex);
                    if (iaplist == null) {
                        Log.e(LOG_TAG, "updatePhoneAdnRecord iaplist = null");
                        return;
                    }
                    record = iaplist.get(i - nOffset);
                } catch (IndexOutOfBoundsException e) {
                    Log.e(LOG_TAG, "Error: Improper ICC card: No IAP record for ADN, continuing");
                    continue;
                }
                if (record == null) {
                    Log.e(LOG_TAG, "Error, No Iap for ADN " + i+1);
                    continue;
                }
                int recNum = record[efrecord.mType2Record] & 0xFF;
                
                log("[" + simid + "]" + "updatePhoneAdnRecord recNum[" + i + "]=" + recNum);
                if (recNum > 0 && recNum < 255) {
                    // SIM record numbers are 1 based
                    byte[] data = null;
                    try {
                        data = fileRecords.get(recNum -1);
                    } catch (IndexOutOfBoundsException e) {
                        Log.e(LOG_TAG, "updatePhoneAdnRecord mIapFileList = null", e);	                        continue;
                    }
                    if (data == null) {
                        Log.e(LOG_TAG, "Error record,data is null;" + i);
                        continue;
                    }
                    if (rec != null) {
                        switch (efrecord.mTag) {
                            case USIM_EFANR_TAG:
                                int anrRecLength = data[1];
                                if (anrRecLength > 0 && anrRecLength <= 11) {
                                    String anr = PhoneNumberUtils.calledPartyBCDToString(data, 2, data[1]);
                                    Log.d(LOG_TAG, "[" + simid + "]" + " updatePhoneAdnRecord " + i + " th anr is " + anr);
                                    if (anr != null && !anr.equals("")) {
                                        rec.setAnr(anr);
                                    }
                                }
                            break;
                            case USIM_EFEMAIL_TAG:

                                String val = IccUtils.adnStringFieldToString(data, 0, data.length);
                                Log.d(LOG_TAG, "[" + simid + "]" + " updatePhoneAdnRecord " + i + " th email is " + val);
                                rec.setEmails(new String[] {
                                    val
                                });
                                break;
                            default:
                                Log.e(LOG_TAG, "not supported tag " + efrecord.mTag);
                                break;
                        }
                        
                    }
                }
            }
        } else if (efrecord.mType == USIM_TYPE1_TAG) {
            //TODO
        }

    }
    
    private class EfRecord {
        public EfRecord() {
        }

        public int mTag;
        public int mPbrRecord;
        public int mEfTag;
        public int mType;
        public int mType2Record = -1;

        public String toString() {
            return "mTAG:" + mTag + "mPbrRecord:" + mPbrRecord + ",mEfTag:" + mEfTag + ",mType:"
                    + mType + ",mType2Record:" + mType2Record;
        }
    }
    
    private void updatePhoneAdnRecordWithEmailByIndex(int recNum, int index, byte[] emailRecData) {
        if (emailRecData == null) return;
        String email = IccUtils.adnStringFieldToString(emailRecData, 0, emailRecData.length);
        Log.d(LOG_TAG, "updatePhoneAdnRecordWithEmailByIndex index " + index
                + " emailRecData record is " + email);
        if (email != null && !email.equals("")) {
            AdnRecord rec = mPhoneBookRecords.get(index);
            rec.setEmails(new String[] {
                email
            });
        }

    }
    
    
    private void updateType2Anr(String anr, int adnIndex, EfRecord record) {
        log("updateType2Anr anr:" + anr + ",adnIndex:" + adnIndex + ",record:" + record);
        int pbrRecNum = (adnIndex - 1) / mAdnFileSize;
        int iapRecNum = (adnIndex - 1) % mAdnFileSize;
        log("updateType2Anr pbrRecNum:" + pbrRecNum + ",iapRecNum:" + iapRecNum);

        if (mIapFileList == null) return;
        if (record == null) return;
        
        Map<Integer, Integer> fileIds;
        if (mPbrFile == null) return;
        fileIds = mPbrFile.mFileIds.get(record.mPbrRecord);
        ArrayList<byte[]> list = mIapFileList.get(record.mPbrRecord);
        if (list == null) return;
        byte[] iap = list.get(iapRecNum);
        if (iap == null) return;
        int index = iap[record.mType2Record] & 0xFF;
        log("updateType2Anr orignal index :" + index);
        if (anr == null || anr.length() == 0) {
            if (index > 0) {
                iap[record.mType2Record] = (byte) 255;
                mPhone.getIccFileHandler().updateEFLinearFixed(fileIds.get(USIM_EFIAP_TAG),
                        iapRecNum + 1, iap, null, obtainMessage(EVENT_IAP_UPDATE_DONE));
            }
            return;
        }
        
        // found the index
        ArrayList<ArrayList<byte[]>>  arrayFileList = null;
        switch (record.mTag) {
            case USIM_EFANR_TAG:
                arrayFileList = mAnrFileList;
                break;
            case USIM_EFEMAIL_TAG:
                arrayFileList = mEmailFileList;
                break;
            default:
                break;
        } 
        if (arrayFileList == null) return;
        int recNum = 0;
        ArrayList<byte[]> dataList = arrayFileList.get(record.mPbrRecord);
        if (dataList == null) return;
        int size = dataList.size();
        log("updateType2Anr size :" + size);
        if (index > 0 && index <= size) {
            recNum = index;
        } else {
            //insert 
            int[] indexArray = new int[size + 1];
            for (int i = 1; i <= size; i++)
                indexArray[i] = 0;
            for (int i = 0; i < list.size(); i++) {
                byte[] value = list.get(i);
                if (value != null) {
                    int tem = value[record.mType2Record] & 0xFF;
                    if (tem > 0 && tem < 255 && tem <= size) {
                        indexArray[tem] = 1;
                    }
                }
            }
            
            for (int i = 1; i <= size; i++) {
                if (indexArray[i] == 0) {
                    recNum = i;
                    break;
                }
            }
        }
        log("updateType2Anr final index :" + recNum);
        if (recNum == 0) return;
        byte[] data = null;
        int what = 0;
        switch (record.mTag) {
            case USIM_EFANR_TAG:
		 AdnRecord rec = mPhoneBookRecords.get(adnIndex - 1);
		 int aas = rec.getAasIndex();
                data = buildAnrRecord(anr, mAnrRecordSize, aas);
                what = EVENT_ANR_UPDATE_DONE;
                break;
            case USIM_EFEMAIL_TAG:
                data = buildEmailRecord(anr, iapRecNum + 1, mEmailRecordSize);
                what = EVENT_EMAIL_UPDATE_DONE;
                break;
                default:
                    break;
        }
        if (data != null) {
            mPhone.getIccFileHandler().updateEFLinearFixed(fileIds.get(record.mTag), recNum, data, null,
                    obtainMessage(what));
            if (recNum != index) {
                iap[record.mType2Record] = (byte) recNum;
                mPhone.getIccFileHandler().updateEFLinearFixed(fileIds.get(USIM_EFIAP_TAG),
                        iapRecNum + 1, iap, null, obtainMessage(EVENT_IAP_UPDATE_DONE));
            }
        }
    }
    
    private void readAnrRecordSize() {
        Log.d(LOG_TAG, "UsimPhoneBookManager readAnrRecordSize");
        if (mPbrFile == null || mPbrFile.mFileIds == null) {
            return;
        }
        Map<Integer, Integer> fileIds = mPbrFile.mFileIds.get(0);
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }

        if (!fileIds.containsKey(USIM_EFANR_TAG)) {
            Log.d(LOG_TAG, "UsimPhoneBookManager readAnrRecordSize: No anr tag in pbr record ");
            return;
        }

        int efid = fileIds.get(USIM_EFANR_TAG);
        IccFileHandler iccFh = mPhone.getIccFileHandler();
        if (iccFh != null) {
            iccFh.getEFLinearRecordSize(efid, obtainMessage(EVENT_GET_ANR_RECORD_SIZE_DONE));
        } else {
            return;
        }
        synchronized (mLock) {
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Interrupted Exception in readAnrFileAndWait");
            }
        }
    }

 
    public ArrayList<AlphaTag> getUSIMAASList() {
        Throwable er = new Throwable();
        er.printStackTrace();
        log("getUSIM AAS List");
        String optr = SystemProperties.get("ro.operator.optr");
        log("getUSIMAASList optr:" + optr);
        if (optr == null || !optr.equals("OP03")) {
            return null;
        }
        ArrayList<AlphaTag> results = new ArrayList<AlphaTag>();
        if (mAasForAnrRec == null || mAasForAnrRec.size() == 0) {
            if (!mIsPbrPresent) {
                Log.e(LOG_TAG, "No PBR files");
                return results;
            }
            synchronized (mLock) {
                loadPBRFiles();
                if (mPbrFile == null) {
                    return results;
                }
                int numRecs = mPbrFile.mFileIds.size();
                if (mAasForAnrRec == null) {
                    mAasForAnrRec = new HashMap<Integer, ArrayList<String>>();
                }
                mAasForAnrRec.clear();
                for (int i = 0; i < numRecs; i++) {
                    readAASFileAndWait(i);
                }
            }
        }
        
        Iterator<Entry<Integer, ArrayList<String>>> entrySet =mAasForAnrRec.entrySet().iterator();
        while (entrySet.hasNext()) {
            Entry<Integer, ArrayList<String>> entry = entrySet.next();
            ArrayList<String> list = entry.getValue();
            int pbrIndex = entry.getKey();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                String value = list.get(i);
                log("aasIndex:" + (i + 1) + ",pbrIndex:" + pbrIndex + ",value:" + value);
                AlphaTag tag = new AlphaTag(i + 1, value, pbrIndex);
                results.add(tag);
            }
        }
        return results;
    }

    public String getUSIMAASById(int index, int pbrIndex) {
        log("remove  usim aas by index " + index + ",pbrIndex " + pbrIndex);
        if (mAasForAnrRec == null) {
            if (!mIsPbrPresent) {
                Log.e(LOG_TAG, "No PBR files");
                return null;
            }
            synchronized (mLock) {
                loadPBRFiles();
                if (mPbrFile == null) {
                    return null;
                }
                int numRecs = mPbrFile.mFileIds.size();

                mAasForAnrRec = new HashMap<Integer, ArrayList<String>>();
                // mAasForAnrRec.clear();
                for (int i = 0; i < numRecs; i++) {
                    readAASFileAndWait(i);
                }
            }
        }
        if (mAasForAnrRec.containsKey(pbrIndex)) {
            ArrayList<String> map = mAasForAnrRec.get(pbrIndex);
            return map.get(index - 1);
        }
        return null;
    }
    
    public boolean removeUSIMAASById(int index, int pbrIndex) {
        log("remove usim aas by id " + index + ",pbrIndex " + pbrIndex);
        if (mAasForAnrRec == null) {
            if (!mIsPbrPresent) {
                Log.e(LOG_TAG, "No PBR files");
                return true;
            }
            synchronized (mLock) {
                loadPBRFiles();
                if (mPbrFile == null) {
                    return true;
                }
                int numRecs = mPbrFile.mFileIds.size();

                mAasForAnrRec = new HashMap<Integer, ArrayList<String>>();
                for (int i = 0; i < numRecs; i++) {
                    readAASFileAndWait(i);
                }
            }
        }
        int aasIndex = index;
        Map <Integer, Integer> fileIds = mPbrFile.mFileIds.get(pbrIndex);
        if (!fileIds.containsKey(USIM_EFAAS_TAG)) {
            Log.e(LOG_TAG, "removeUSIMAASById-PBR have no AAS EF record");
            return false;
        }
        int efid = fileIds.get(USIM_EFAAS_TAG);
        log("removeUSIMAASById result,efid:" + efid);
        
        IccFileHandler iccFh = mPhone.getIccFileHandler();
        if (iccFh != null) {

            Message msg = obtainMessage(EVENT_AAS_UPDATE_DONE);
            int len = getUSIMAASMaxNameLen();
            byte[] aasString = new byte[len];
            for (int i = 0; i < len; i++) {
                aasString[i] = (byte) 0xFF;
            }
            synchronized (mLock) {
                iccFh.updateEFLinearFixed(efid, aasIndex, aasString, null, msg);

                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG, "Interrupted Exception in readAASFileAndWait");
                }
            }
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar == null || ar.exception == null) {
                ArrayList<String> list = mAasForAnrRec.get(pbrIndex);
                if (list != null) {
                    log("remove aas done " + list.get(aasIndex - 1));
                    list.set(aasIndex - 1, null);
                }
                return true;
            } else {
                Log.e(LOG_TAG, "removeUSIMAASById exception " + ar.exception);
                return false;
            }
        } else {
            Log.e(LOG_TAG, "removeUSIMAASById-IccFileHandler is null");
            return false;
        }
    }
    
    public int insertUSIMAAS(String aasName) {
        log("insertUSIMAAS " + aasName);
        if (aasName == null || aasName.length() ==0) {
            return 0;
        }
        int limit = getUSIMAASMaxNameLen();
        int len = aasName.length();
        if (len > limit) {
            return 0;
        }
        int index = -1;
        synchronized (mLock) {
            if (mAasForAnrRec == null) {
                if (!mIsPbrPresent) {
                    Log.e(LOG_TAG, "insertUSIMAAS No PBR ");
                    return -1;
                }
                loadPBRFiles();
                if (mPbrFile == null) {
                    Log.e(LOG_TAG, "insertUSIMAAS No PBR files");
                    return -1;
                }
                int numRecs = mPbrFile.mFileIds.size();
                mAasForAnrRec = new HashMap<Integer, ArrayList<String>>();
                for (int i = 0; i < numRecs; i++) {
                    readAASFileAndWait(i);
                }
            }

            int pbrIndex = -1;
            int aasIndex = 0;
            boolean found = false;
            Iterator<Entry<Integer, ArrayList<String>>> entrySet = mAasForAnrRec.entrySet()
                    .iterator();
            while (entrySet.hasNext() && !found) {
                Entry<Integer, ArrayList<String>> entry = entrySet.next();
                ArrayList<String> map = entry.getValue();
                int size = map.size();
                
                for (int i = 0; i < size; i++) {
                    String value = map.get(i);
                    if (value == null || value.length() == 0) {
                        found = true;
                        pbrIndex = entry.getKey();
                        aasIndex = i + 1;
                        break;
                    }
                }
            }
            log("insertUSIMAAS pbrIndex:" + pbrIndex + ",aasIndex:" + aasIndex + ",found:" + found);
            if (!found) {
                // TODO full 
                return -2;
            }
            String temp = encodeATUCS2(aasName);
            Message msg = obtainMessage(EVENT_AAS_UPDATE_DONE);
            mPhone.mCM.editUPBEntry(UPB_EF_AAS, 0, aasIndex, temp, null, msg);
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Interrupted Exception in insertUSIMAAS");
            }
            AsyncResult ar = (AsyncResult) msg.obj;
            log("insertUSIMAAS UPB_EF_AAS: ar " + ar);
            if (ar == null || ar.exception == null) {
                ArrayList<String> list =mAasForAnrRec.get(pbrIndex);
                if (list != null) {
                    list.set(aasIndex - 1 , aasName);
                    log("insertUSIMAAS update mAasForAnrRec done");
                }
                return aasIndex;
            } else {
                Log.e(LOG_TAG, "insertUSIMAAS exception " + ar.exception);
                return -1;
            }

        }
    }
    
    public boolean updateUSIMAAS(int index, int pbrIndex, String aasName) {
        log("updateUSIMAAS index " + index + ",pbrIndex " + pbrIndex + ",aasName " + aasName);
        if (mAasForAnrRec == null) {
            if (!mIsPbrPresent) {
                Log.e(LOG_TAG, "updateUSIMAAS No PBR ");
                return false;
            }
            synchronized (mLock) {
                loadPBRFiles();
                if (mPbrFile == null) {
                    Log.e(LOG_TAG, "updateUSIMAAS No PBR files");
                    return false;
                }
                int numRecs = mPbrFile.mFileIds.size();
                mAasForAnrRec = new HashMap<Integer, ArrayList<String>>();
                for (int i = 0; i < numRecs; i++) {
                    readAASFileAndWait(i);
                }
            }
        }
        if (!mAasForAnrRec.containsKey(pbrIndex)) {
            log("updateUSIMAAS not found pbr index " + pbrIndex);
            return false;
        }
        ArrayList<String> map = mAasForAnrRec.get(pbrIndex);
        if (map == null) {
            Log.e(LOG_TAG, "no aas for pbr " + pbrIndex);
            return false;
        }
        if (index <= 0 || index > map.size()) {
            Log.e(LOG_TAG, "updateUSIMAAS not found aas index " + index);
            return false;
        }
        String aas = map.get(index - 1);
        log("updateUSIMAAS old aas " + aas);
//        if (aas == null || aas.length() == 0) {
//            if (aasName == null || aasName.length() == 0) return true;
//        }
        if (aasName == null || aasName.length() == 0) {
            // delete
            return removeUSIMAASById(index, pbrIndex);
        } else {
            // update
            int limit = getUSIMAASMaxNameLen();
            int len = aasName.length();
            log("updateUSIMAAS aas limit " + limit);
            if (len > limit) {
                return false;
            }
            int offset = 0;
            for (int i = 0; i < pbrIndex; i++) {
                offset += mAasForAnrRec.get(i).size();
            }
            log("updateUSIMAAS offset " + offset);
            int aasIndex = index + offset;
            String temp = encodeATUCS2(aasName);
            Message msg = obtainMessage(EVENT_AAS_UPDATE_DONE);
            synchronized (mLock) {
                mPhone.mCM.editUPBEntry(UPB_EF_AAS, 0, aasIndex, temp, null, msg);
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG, "Interrupted Exception in updateUSIMAAS");
                }
            }
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar == null || ar.exception == null) {
                ArrayList<String> list =mAasForAnrRec.get(pbrIndex);
                if (list != null) {
                    list.set(index - 1 , aasName);
                    log("updateUSIMAAS update mAasForAnrRec done");
                }
                return true;
            } else {
                Log.e(LOG_TAG, "updateUSIMAAS exception " + ar.exception);
                return false;
            }
        }
    }
    /**
     * @param adnIndex: ADN index
     * @param aasIndex: change AAS to the value refered by aasIndex, -1 means remove
     * @return
     */
    public boolean updateADNAAS(int adnIndex, int aasIndex) {
        int pbrRecNum = (adnIndex-1)/mAdnFileSize;
        int index = (adnIndex-1)%mAdnFileSize;
        //? from 0?
        AdnRecord rec = mPhoneBookRecords.get(adnIndex - 1);
        rec.setAasIndex(aasIndex);
        String anr = rec.getAdditionalNumber();
        //TODO update aas
        updateAnrByAdnIndex(anr, adnIndex);
        return true;
    }
    
    public int getUSIMAASMaxNameLen() {
        Log.d(LOG_TAG, "UsimPhoneBookManager getUSIMAASNameLen begin");
        synchronized (mLock) {
            if (mUpbCap[4] <= 0) {
                mPhone.mCM.queryUPBCapability(obtainMessage(EVENT_UPB_CAPABILITY_QUERY_DONE));
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG, "Interrupted Exception in getUSIMAASNameLen");
                }
            }
        }

        Log.d(LOG_TAG, "UsimPhoneBookManager getUSIMAASNameLen done: " + "L_AAS is " + mUpbCap[4]);

        return mUpbCap[4];
    }

    public int getUSIMAASMaxCount() {
        Log.d(LOG_TAG, "UsimPhoneBookManager getUSIMAASMaxCount begin");
        synchronized (mLock) {
            if (mUpbCap[3] <= 0) {
                mPhone.mCM.queryUPBCapability(obtainMessage(EVENT_UPB_CAPABILITY_QUERY_DONE));
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG, "Interrupted Exception in getUSIMAASMaxCount");
                }
            }
        }

        Log.d(LOG_TAG, "UsimPhoneBookManager getUSIMAASMaxCount done: " + "N_AAS is " + mUpbCap[3]);

        return mUpbCap[3];
    }
    
    public void loadPBRFiles() {
        if (!mIsPbrPresent)
            return;
        synchronized (mLock) {
            // Check if the PBR file is present in the cache, if not read it
            // from the USIM.
            if (mPbrFile == null) {
                readPbrFileAndWait(false);
            }

            if (mPbrFile == null) {
                readPbrFileAndWait(true);
            }
        }
    }
    
    public int getAnrCount() {
        /**
        Log.d(LOG_TAG, "UsimPhoneBookManager getAnrCount begin");
        synchronized (mLock) {
            if (mUpbCap[0] <= 0) {
                mPhone.mCM.queryUPBCapability(obtainMessage(EVENT_UPB_CAPABILITY_QUERY_DONE));
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Interrupted Exception in getAnrCount");
            }
            }
        }

        Log.d(LOG_TAG, "UsimPhoneBookManager getAnrCount done: " + "N_ANR is " + mUpbCap[0]);

        return mUpbCap[0];
        */
        //TODO Support all ANRs
        return 1;
    }
}
