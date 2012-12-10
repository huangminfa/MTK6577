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

import android.content.pm.PackageManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
//MTK-START [mtk80601][111215][ALPS00093395]
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import java.util.ArrayList;
//MTK-END [mtk80601][111215][ALPS00093395]

/**
 * SimPhoneBookInterfaceManager to provide an inter-process communication to
 * access ADN-like SIM records.
 */
public abstract class IccPhoneBookInterfaceManager extends IIccPhoneBook.Stub {
    protected static final boolean DBG = true;

    protected PhoneBase phone;
    protected AdnRecordCache adnCache;
    protected final Object mLock = new Object();
    protected int recordSize[];
    protected boolean success;
    protected List<AdnRecord> records;
    protected int errorCause;

    protected static final boolean ALLOW_SIM_OP_IN_UI_THREAD = false;

    protected static final int EVENT_GET_SIZE_DONE = 1;
    protected static final int EVENT_LOAD_DONE = 2;
    protected static final int EVENT_UPDATE_DONE = 3;

	static private int times = 1;

    protected Handler mBaseHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AsyncResult ar;

            switch (msg.what) {
                case EVENT_GET_SIZE_DONE:
                    ar = (AsyncResult) msg.obj;
                    synchronized (mLock) {
                        if (ar.exception == null) {
                            recordSize = (int[])ar.result;
                            // recordSize[0]  is the record length
                            // recordSize[1]  is the total length of the EF file
                            // recordSize[2]  is the number of records in the EF file
                            logd("GET_RECORD_SIZE Size " + recordSize[0] +
                                    " total " + recordSize[1] +
                                    " #record " + recordSize[2]);
                        }
                        notifyPending(ar);
                    }
                    break;
                case EVENT_UPDATE_DONE:
                    ar = (AsyncResult) msg.obj;
                    synchronized (mLock) {
                        success = (ar.exception == null);
//MTK-START [mtk80601][111215][ALPS00093395]
                        if (!success) {
                            errorCause = getErrorCauseFromException( 
                                (CommandException)ar.exception );
                        }
                        else {
                            errorCause = IccProvider.ERROR_ICC_PROVIDER_NO_ERROR;
                        }
                        logd("update done result: " + errorCause);
//MTK-END [mtk80601][111215][ALPS00093395]
                        notifyPending(ar);
                    }
                    break;
                case EVENT_LOAD_DONE:
                    ar = (AsyncResult)msg.obj;
                    synchronized (mLock) {
                        if (ar.exception == null) {
                            records = (List<AdnRecord>) ar.result;
                        } else {
                            if(DBG) logd("Cannot load ADN records");
                            records = null;
                        }
                        notifyPending(ar);
                    }
                    break;
            }
        }

        private void notifyPending(AsyncResult ar) {
            if (ar.userObj == null) {
                return;
            }
            AtomicBoolean status = (AtomicBoolean) ar.userObj;
            status.set(true);
            mLock.notifyAll();
        }
    };

    public IccPhoneBookInterfaceManager(PhoneBase phone) {
        this.phone = phone;
    }

    public void dispose() {
    }

    protected void publish() {
        //NOTE service "simphonebook" added by IccSmsInterfaceManagerProxy
        ServiceManager.addService("simphonebook", this);
    }

    protected abstract void logd(String msg);

    protected abstract void loge(String msg);

    /**
     * Replace oldAdn with newAdn in ADN-like record in EF
     *
     * getAdnRecordsInEf must be called at least once before this function,
     * otherwise an error will be returned. Currently the email field
     * if set in the ADN record is ignored.
     * throws SecurityException if no WRITE_CONTACTS permission
     *
     * @param efid must be one among EF_ADN, EF_FDN, and EF_SDN
     * @param oldTag adn tag to be replaced
     * @param oldPhoneNumber adn number to be replaced
     *        Set both oldTag and oldPhoneNubmer to "" means to replace an
     *        empty record, aka, insert new record
     * @param newTag adn tag to be stored
     * @param newPhoneNumber adn number ot be stored
     *        Set both newTag and newPhoneNubmer to "" means to replace the old
     *        record with empty one, aka, delete old record
     * @param pin2 required to update EF_FDN, otherwise must be null
     * @return true for success
     */
    public boolean
    updateAdnRecordsInEfBySearch (int efid,
            String oldTag, String oldPhoneNumber,
            String newTag, String newPhoneNumber, String pin2) {
//MTK-START [mtk80601][111215][ALPS00093395]
        int result;

        result = updateAdnRecordsInEfBySearchWithError( 
                efid, oldTag, oldPhoneNumber, 
                newTag, newPhoneNumber, pin2);

        return result == IccProvider.ERROR_ICC_PROVIDER_NO_ERROR;
    }

    /**
     * Replace oldAdn with newAdn in ADN-like record in EF
     *
     * getAdnRecordsInEf must be called at least once before this function,
     * otherwise an error will be returned. Currently the email field
     * if set in the ADN record is ignored.
     * throws SecurityException if no WRITE_CONTACTS permission
     *
     * This method will return why the error occurs.
     *
     * @param efid must be one among EF_ADN, EF_FDN, and EF_SDN
     * @param oldTag adn tag to be replaced
     * @param oldPhoneNumber adn number to be replaced
     *        Set both oldTag and oldPhoneNubmer to "" means to replace an
     *        empty record, aka, insert new record
     * @param newTag adn tag to be stored
     * @param newPhoneNumber adn number ot be stored
     *        Set both newTag and newPhoneNubmer to "" means to replace the old
     *        record with empty one, aka, delete old record
     * @param pin2 required to update EF_FDN, otherwise must be null
     * @return ERROR_ICC_PROVIDER_* defined in the IccProvider
     */
    synchronized public int
    updateAdnRecordsInEfBySearchWithError (int efid,
            String oldTag, String oldPhoneNumber,
            String newTag, String newPhoneNumber, String pin2) {

        int index = -1;
        if (phone.getContext().checkCallingOrSelfPermission(
                android.Manifest.permission.WRITE_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException(
                    "Requires android.permission.WRITE_CONTACTS permission");
        }


        if (DBG) logd("updateAdnRecordsInEfBySearch: efid=" + efid +
                " ("+ oldTag + "," + oldPhoneNumber + ")"+ "==>" +
                " ("+ newTag + " (" + newTag.length() + ")," + newPhoneNumber + ")"+ " pin2=" + pin2);

        efid = updateEfForIccType(efid);

        synchronized(mLock) {
            checkThread();
            success = false;
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = mBaseHandler.obtainMessage(EVENT_UPDATE_DONE, status);
			times = (times + 2) % 20000;
			response.arg1 = times;
            AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber);
			if(null == newPhoneNumber) newPhoneNumber = "";
            AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber);
            index = adnCache.updateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
            waitForResult(status);
        }
        if(errorCause == IccProvider.ERROR_ICC_PROVIDER_NO_ERROR){
            logd("updateAdnRecordsInEfBySearchWithError success index is " + index);
            return index;
        }
        return errorCause;
    }

    /**
     * Replace oldAdn with newAdn in ADN-like record in USIM EF
     *
     * getAdnRecordsInEf must be called at least once before this function,
     * otherwise an error will be returned.
     * throws SecurityException if no WRITE_CONTACTS permission
     *
     * This method will return why the error occurs.
     *
     * @param efid must be one among EF_ADN, EF_FDN, and EF_SDN
     * @param oldTag adn tag to be replaced
     * @param oldPhoneNumber adn number to be replaced
     *        Set both oldTag and oldPhoneNubmer to "" means to replace an
     *        empty record, aka, insert new record
     * @param newTag adn tag to be stored
     * @param newPhoneNumber adn number ot be stored
     *        Set both newTag and newPhoneNubmer to "" means to replace the old
     *        record with empty one, aka, delete old record
     * @param oldAnr adn ANR to be replaced
     * @param oldGrpIds adn GROUP to be replaced
     * @param oldEmails adn EMAIL to be replaced
     * @param newAnr adn ANR to be stored
     * @param newGrpIds adn GROUP to be replaced
     * @param newEmails adn EMAIL to be replaced
     * @return ERROR_ICC_PROVIDER_* defined in the IccProvider
     */
    synchronized public int
    updateUsimPBRecordsInEfBySearchWithError (int efid,
            String oldTag, String oldPhoneNumber,String oldAnr, String oldGrpIds, String[] oldEmails,
            String newTag, String newPhoneNumber, String newAnr, String newGrpIds, String[] newEmails) {

        int index = -1;
	AtomicBoolean status = new AtomicBoolean(false);
        if (phone.getContext().checkCallingOrSelfPermission(
                android.Manifest.permission.WRITE_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException(
                    "Requires android.permission.WRITE_CONTACTS permission");
        }

	
        if (DBG) logd("updateUsimPBRecordsInEfBySearchWithError: efid=" + efid +
                " ("+ oldTag + "," + oldPhoneNumber + "oldAnr" + oldAnr + " oldGrpIds " + oldGrpIds + ")"+ "==>" +
                "("+ newTag + "," + newPhoneNumber + ")"+ " newAnr= " + newAnr + " newGrpIds = " + newGrpIds + " newEmails = " + newEmails);

        
        synchronized(mLock) {
            checkThread();
            success = false;
            Message response = mBaseHandler.obtainMessage(EVENT_UPDATE_DONE, status);
			times = (times + 2) % 20000;
			response.arg1 = times;
            AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber);
			if(null == newPhoneNumber) newPhoneNumber = "";
            AdnRecord newAdn = new AdnRecord(0, 0, newTag, newPhoneNumber, newAnr, newEmails, newGrpIds);
            index = adnCache.updateAdnBySearch(efid, oldAdn, newAdn, null, response);
            waitForResult(status);
        }
        if(errorCause == IccProvider.ERROR_ICC_PROVIDER_NO_ERROR){
            logd("updateUsimPBRecordsInEfBySearchWithError success index is " + index);
            return index;
        }       
        return errorCause;
    }
    
    synchronized public int updateUsimPBRecordsBySearchWithError(int efid, AdnRecord oldAdn, AdnRecord newAdn) {
        int index = -1;
        AtomicBoolean status = new AtomicBoolean(false);
        if (phone.getContext().checkCallingOrSelfPermission(
                android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException(
                    "Requires android.permission.WRITE_CONTACTS permission");
        }

        if (DBG)
            logd("updateUsimPBRecordsBySearchWithError: efid=" + efid +
                    " (" + oldAdn + ")" + "==>" + "(" + newAdn + ")");

        synchronized (mLock) {
            checkThread();
            success = false;
            Message response = mBaseHandler.obtainMessage(EVENT_UPDATE_DONE, status);
            times = (times + 2) % 20000;
            response.arg1 = times;
            if (newAdn.number == null) {
                newAdn.number = "";
            }
            index = adnCache.updateAdnBySearch(efid, oldAdn, newAdn, null, response);
            waitForResult(status);
        }
        if (errorCause == IccProvider.ERROR_ICC_PROVIDER_NO_ERROR) {
            logd("updateUsimPBRecordsBySearchWithError success index is " + index);
            return index;
        }
        return errorCause;
    }
//MTK-END [mtk80601][111215][ALPS00093395]

    /**
     * Update an ADN-like EF record by record index
     *
     * This is useful for iteration the whole ADN file, such as write the whole
     * phone book or erase/format the whole phonebook. Currently the email field
     * if set in the ADN record is ignored.
     * throws SecurityException if no WRITE_CONTACTS permission
     *
     * @param efid must be one among EF_ADN, EF_FDN, and EF_SDN
     * @param newTag adn tag to be stored
     * @param newPhoneNumber adn number to be stored
     *        Set both newTag and newPhoneNubmer to "" means to replace the old
     *        record with empty one, aka, delete old record
     * @param index is 1-based adn record index to be updated
     * @param pin2 required to update EF_FDN, otherwise must be null
     * @return true for success
     */
    public boolean
    updateAdnRecordsInEfByIndex(int efid, String newTag,
            String newPhoneNumber, int index, String pin2) {

        int result;

        result = updateAdnRecordsInEfByIndexWithError(
                efid, newTag, 
                newPhoneNumber, index, pin2);

        return result == IccProvider.ERROR_ICC_PROVIDER_NO_ERROR;
    }

    /**
     * Update an ADN-like EF record by record index
     *
     * This is useful for iteration the whole ADN file, such as write the whole
     * phone book or erase/format the whole phonebook. Currently the email field
     * if set in the ADN record is ignored.
     * throws SecurityException if no WRITE_CONTACTS permission
     *
     * This method will return why the error occurs
     *
     * @param efid must be one among EF_ADN, EF_FDN, and EF_SDN
     * @param newTag adn tag to be stored
     * @param newPhoneNumber adn number to be stored
     *        Set both newTag and newPhoneNubmer to "" means to replace the old
     *        record with empty one, aka, delete old record
     * @param index is 1-based adn record index to be updated
     * @param pin2 required to update EF_FDN, otherwise must be null
     * @return ERROR_ICC_PROVIDER_* defined in the IccProvider
     */
//MTK-START [mtk80601][111215][ALPS00093395]
    synchronized public int
    updateAdnRecordsInEfByIndexWithError(int efid, String newTag,
            String newPhoneNumber, int index, String pin2) {

        if (phone.getContext().checkCallingOrSelfPermission(
                android.Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException(
                    "Requires android.permission.WRITE_CONTACTS permission");
        }

        if (DBG) logd("updateAdnRecordsInEfByIndex: efid=" + efid +
                " Index=" + index + " ==> " +
                "("+ newTag + "," + newPhoneNumber + ")"+ " pin2=" + pin2);

        
        synchronized(mLock) {
            checkThread();
            success = false;
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = mBaseHandler.obtainMessage(EVENT_UPDATE_DONE, status);
			if(null == newPhoneNumber) newPhoneNumber = "";
            AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber);
            adnCache.updateAdnByIndex(efid, newAdn, index, pin2, response);
            waitForResult(status);
        }
        return errorCause;
    }

    /**
     * Update an ADN-like USIM EF record by record index
     *
     * This is useful for iteration the whole ADN file, such as write the whole
     * phone book or erase/format the whole phonebook. 
     * throws SecurityException if no WRITE_CONTACTS permission
     *
     * This method will return why the error occurs
     *
     * @param efid must be one among EF_ADN, EF_FDN, and EF_SDN
     * @param newTag adn tag to be stored
     * @param newPhoneNumber adn number to be stored
     *        Set both newTag and newPhoneNubmer to "" means to replace the old
     *        record with empty one, aka, delete old record
     * @param index is 1-based adn record index to be updated
     * @param newAnr adn ANR to be stored
     * @param newGrpIds adn GROUP to be stored
     * @param newEmails adn EMAIL to be stored
     * @return ERROR_ICC_PROVIDER_* defined in the IccProvider
     */
    synchronized public int
    updateUsimPBRecordsInEfByIndexWithError(int efid, String newTag,
            String newPhoneNumber, String newAnr,  String newGrpIds, String[] newEmails, int index) {
            
         if (phone.getContext().checkCallingOrSelfPermission(
                android.Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException(
                    "Requires android.permission.WRITE_CONTACTS permission");
        }

        if (DBG) logd("updateUsimPBRecordsInEfByIndexWithError: efid=" + efid +
                " Index=" + index + " ==> " +
                "("+ newTag + "," + newPhoneNumber + ")"+ " newAnr= " + newAnr + " newGrpIds = " + newGrpIds + " newEmails = " + newEmails);

        
        synchronized(mLock) {
            checkThread();
            success = false;
	    AtomicBoolean status = new AtomicBoolean(false);
            Message response = mBaseHandler.obtainMessage(EVENT_UPDATE_DONE, status);
			if(null == newPhoneNumber) newPhoneNumber = "";
            AdnRecord newAdn = new AdnRecord(efid, index, newTag, newPhoneNumber, newAnr, newEmails, newGrpIds);
            adnCache.updateAdnByIndex(efid, newAdn, index, null, response);
            waitForResult(status);
    }
        return errorCause;

    }
    
    synchronized public int
    updateUsimPBRecordsByIndexWithError(int efid, AdnRecord record, int index) {
            
         if (phone.getContext().checkCallingOrSelfPermission(
                android.Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException(
                    "Requires android.permission.WRITE_CONTACTS permission");
        }

        if (DBG) logd("updateUsimPBRecordsByIndexWithError: efid=" + efid +
                " Index=" + index + " ==> " + record);
        
        synchronized(mLock) {
            checkThread();
            success = false;
        AtomicBoolean status = new AtomicBoolean(false);
            Message response = mBaseHandler.obtainMessage(EVENT_UPDATE_DONE, status);
            adnCache.updateAdnByIndex(efid, record, index, null, response);
            waitForResult(status);
    }
        return errorCause;

    }
//MTK-END [mtk80601][111215][ALPS00093395]

    /**
     * Get the capacity of records in efid
     *
     * @param efid the EF id of a ADN-like ICC
     * @return  int[3] array
     *            recordSizes[0]  is the single record length
     *            recordSizes[1]  is the total length of the EF file
     *            recordSizes[2]  is the number of records in the EF file
     */
    public abstract int[] getAdnRecordsSize(int efid);

    /**
     * Loads the AdnRecords in efid and returns them as a
     * List of AdnRecords
     *
     * throws SecurityException if no READ_CONTACTS permission
     *
     * @param efid the EF id of a ADN-like ICC
     * @return List of AdnRecord
     */
    synchronized public List<AdnRecord> getAdnRecordsInEf(int efid) {

        if (phone.getContext().checkCallingOrSelfPermission(
                android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException(
                    "Requires android.permission.READ_CONTACTS permission");
        }

        efid = updateEfForIccType(efid);
        if (DBG) logd("getAdnRecordsInEF: efid=" + efid);

        synchronized(mLock) {
            checkThread();
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = mBaseHandler.obtainMessage(EVENT_LOAD_DONE, status);
            adnCache.requestLoadAllAdnLike(efid, adnCache.extensionEfForEf(efid), response);
            waitForResult(status);
        }
        return records;
    }

    protected void checkThread() {
        if (!ALLOW_SIM_OP_IN_UI_THREAD) {
            // Make sure this isn't the UI thread, since it will block
            if (mBaseHandler.getLooper().equals(Looper.myLooper())) {
                loge("query() called on the main UI thread!");
                throw new IllegalStateException(
                        "You cannot call query on this provder from the main UI thread.");
            }
        }
    }

    protected void waitForResult(AtomicBoolean status) {
        while (!status.get()) {
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                logd("interrupted while trying to update by search");
            }
        }
    }

    /**
     * Change the request icc type to PBR when the sim is a USIM Card
     * 
     * @param efid request EF
     * @return the request icc file id
     */
    private int updateEfForIccType(int efid) {
        // Check if we are trying to read ADN records
        if (efid == IccConstants.EF_ADN) {
            if (phone.getIccCard().isApplicationOnIcc(IccCardApplication.AppType.APPTYPE_USIM)) {
                return IccConstants.EF_PBR;
            }
        }
        return efid;
    }
//MTK-START [mtk80601][111215][ALPS00093395]
    /**
     * Change the error code to IccProvider error code
     * @return the error code defined in IccProvider
     */
    private int getErrorCauseFromException(CommandException e) {

        int ret;

        if (e == null) {
            return IccProvider.ERROR_ICC_PROVIDER_NO_ERROR;
        }

        switch(e.getCommandError()) {
            case GENERIC_FAILURE: /* occurs when Extension file is full(?) */
                ret = IccProvider.ERROR_ICC_PROVIDER_GENERIC_FAILURE;
                break;
            case DIAL_STRING_TOO_LONG:
                ret = IccProvider.ERROR_ICC_PROVIDER_NUMBER_TOO_LONG;
                break;            
            case SIM_PUK2:
            case PASSWORD_INCORRECT:
                ret = IccProvider.ERROR_ICC_PROVIDER_PASSWORD_ERROR;
                break;
            case TEXT_STRING_TOO_LONG:
                ret = IccProvider.ERROR_ICC_PROVIDER_TEXT_TOO_LONG;
                break;
            case SIM_MEM_FULL:
                ret = IccProvider.ERROR_ICC_PROVIDER_STORAGE_FULL;
                break;
            case NOT_READY:
                ret = IccProvider.ERROR_ICC_PROVIDER_NOT_READY;
                break;
            case ADDITIONAL_NUMBER_STRING_TOO_LONG:
                ret = IccProvider.ERROR_ICC_PROVIDER_ANR_TOO_LONG;
                break;				
            case ADN_LIST_NOT_EXIST:
                ret = IccProvider.ERROR_ICC_PROVIDER_ADN_LIST_NOT_EXIST;
                break;				
	    case EMAIL_SIZE_LIMIT:
		ret = IccProvider.ERROR_ICC_PROVIDER_EMAIL_FULL;
		break;
	    case EMAIL_NAME_TOOLONG:
		ret = IccProvider.ERROR_ICC_PROVIDER_EMAIL_TOOLONG;
		break;
            default:
                ret = IccProvider.ERROR_ICC_PROVIDER_UNKNOWN;
                break;
        }

        return ret;
    }

    /**
     * Load all adn-liked ef when phb ready
     */
    public void onPhbReady() {

        adnCache.requestLoadAllAdnLike(IccConstants.EF_ADN, 
                adnCache.extensionEfForEf(IccConstants.EF_ADN), null);
    }

    public boolean isPhbReady() {
        
        return phone.getIccCard().isPhbReady();
    }

    /**
     * get the USIM group list
     * @return the group list
     */
    public List<UsimGroup> getUsimGroups() {
        return adnCache.getUsimGroups();
    }
    
    /**
     * get the group name by id
     * 
     * @param nGasId group index
     * @retrun the requested group name
     */
    public String getUSIMGroupById(int nGasId){
        return adnCache.getUSIMGroupById(nGasId);
    }

    /**
     * remove the group name by id
     * 
     * @param nGasId group index
     * @retrun true if sucessed
     */
    public boolean removeUSIMGroupById(int nGasId){
        return adnCache.removeUSIMGroupById(nGasId);
    }
    
    /**
     * insert a new group
     * 
     * @param grpName group name
     * @retrun 0 if successed, -1 generic error, -10 name too long, -20 sim full
     */
    public int insertUSIMGroup(String grpName){
        return adnCache.insertUSIMGroup(grpName);
    }

    /**
     * update group
     * 
     * @param grpName group name to be stored
     * @param nGasId group id
     * @retrun 0 if successed, -1 generic error, -10 name too long, -20 sim full
     */
    public int updateUSIMGroup(int nGasId, String grpName){
        return adnCache.updateUSIMGroup(nGasId, grpName);
    }

    /**
     * add contact into group
     * 
     * @param adnIndex the adn index
     * @param grpIndex the group index to be added
     * @retrun true if successed
     */
    public boolean addContactToGroup(int adnIndex, int grpIndex){
        return adnCache.addContactToGroup(adnIndex, grpIndex);
    }

    /**
     * remove contact out of group
     * 
     * @param adnIndex the adn index
     * @param grpIndex the group index to be added
     * @retrun true if successed
     */
    public boolean removeContactFromGroup(int adnIndex, int grpIndex){
        return adnCache.removeContactFromGroup(adnIndex, grpIndex);
    }

    /**
     * if the group is already exist
     * 
     * @grpName the requested group name
     * @retrun -1 if not exist or the group index if exist
     * 
     */
    public int hasExistGroup(String grpName){
        return adnCache.hasExistGroup(grpName);
    }

    /**
     * the max name length
     * 
     * @return the max name length, -1 if some error happened
     */
    public int getUSIMGrpMaxNameLen(){
    	return adnCache.getUSIMGrpMaxNameLen();
    }

    /**
     * the max group count
     * 
     * @return the max group count, -1 if some error happened
     */
    public int getUSIMGrpMaxCount(){
    	return adnCache.getUSIMGrpMaxCount();
    }
    
    public List<AlphaTag> getUSIMAASList() {
        return adnCache.getUSIMAASList();
    }
    public String getUSIMAASById(int index) {
        return adnCache.getUSIMAASById(index);
    }
    
    public boolean removeUSIMAASById(int index, int pbrIndex) {
        return adnCache.removeUSIMAASById(index, pbrIndex);
    }
    
    public int insertUSIMAAS(String aasName) {
        return adnCache.insertUSIMAAS(aasName);
    }
    
    public boolean updateUSIMAAS(int index, int pbrIndex, String aasName) {
        return adnCache.updateUSIMAAS(index, pbrIndex, aasName);
    }
    /**
     * 
     * @param adnIndex: ADN index
     * @param aasIndex: change AAS to the value refered by aasIndex, -1 means remove
     * @return
     */
    public boolean updateADNAAS(int adnIndex, int aasIndex) {
        return adnCache.updateADNAAS(adnIndex, aasIndex);
    }
    public int getAnrCount() {
        return adnCache.getAnrCount();
    }
    public int getUSIMAASMaxCount() {
        return adnCache.getUSIMAASMaxCount();
    }
    
    public int getUSIMAASMaxNameLen() {
        return adnCache.getUSIMAASMaxNameLen();
    }
//MTK-END [mtk80601][111215][ALPS00093395]

}
