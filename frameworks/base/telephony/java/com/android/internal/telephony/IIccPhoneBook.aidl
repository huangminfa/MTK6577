/*
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package com.android.internal.telephony;

import com.android.internal.telephony.AdnRecord;
import com.android.internal.telephony.UsimGroup;
import com.android.internal.telephony.AlphaTag;




/** Interface for applications to access the ICC phone book.
 *
 * <p>The following code snippet demonstrates a static method to
 * retrieve the IIccPhoneBook interface from Android:</p>
 * <pre>private static IIccPhoneBook getSimPhoneBookInterface()
            throws DeadObjectException {
    IServiceManager sm = ServiceManagerNative.getDefault();
    IIccPhoneBook spb;
    spb = IIccPhoneBook.Stub.asInterface(sm.getService("iccphonebook"));
    return spb;
}
 * </pre>
 */

interface IIccPhoneBook {

    /**
     * Loads the AdnRecords in efid and returns them as a
     * List of AdnRecords
     *
     * @param efid the EF id of a ADN-like SIM
     * @return List of AdnRecord
     */
    List<AdnRecord> getAdnRecordsInEf(int efid);

    /**
     * Replace oldAdn with newAdn in ADN-like record in EF
     *
     * getAdnRecordsInEf must be called at least once before this function,
     * otherwise an error will be returned
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
    boolean updateAdnRecordsInEfBySearch(int efid,
            String oldTag, String oldPhoneNumber,
            String newTag, String newPhoneNumber,
            String pin2);

    /**
     * Replace oldAdn with newAdn in ADN-like record in EF
     *
     * getAdnRecordsInEf must be called at least once before this function,
     * otherwise an error will be returned.
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
     *
     * {@hide}
     */
    int
    updateAdnRecordsInEfBySearchWithError (int efid,
            String oldTag, String oldPhoneNumber,
            String newTag, String newPhoneNumber, String pin2);


    /**
     * Replace old USIM phonebook contacts with new USIM phonebook contacts 
     *
     * getAdnRecordsInEf must be called at least once before this function,
     * otherwise an error will be returned.
     *
     * This method will return why the error occurs.
     *
     * @param efid must be EF_ADN
     * @param oldTag adn tag to be replaced
     * @param oldPhoneNumber adn number to be replaced
     * @param oldAnr additional number string for the adn record.
     * @param oldGrpIds group id list for the adn record.
     * @param oldEmails Emails for the adn record.     
     *        Set both oldTag and oldPhoneNubmer to "" means to replace an
     *        empty record, aka, insert new record
     * @param newTag adn tag to be stored
     * @param newPhoneNumber adn number ot be stored
     * @param newAnr  new additional number string to be stored 
     * @param newGrpIds group id list for the adn record.
     * @param newEmails Emails for the adn record.  
     *        Set both newTag and newPhoneNubmer to "" means to replace the old
     *        record with empty one, aka, delete old record
     * @return ERROR_ICC_PROVIDER_* defined in the IccProvider
     *
     * {@hide}
     */
    int
    updateUsimPBRecordsInEfBySearchWithError (int efid,
            String oldTag, String oldPhoneNumber,String oldAnr, in String oldGrpIds, in String[] oldEmails,
            String newTag, String newPhoneNumber, String newAnr, in String newGrpIds, in String[] newEmails);	
    /**
     * Update an ADN-like EF record by record index
     *
     * This is useful for iteration the whole ADN file, such as write the whole
     * phone book or erase/format the whole phonebook
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
    boolean updateAdnRecordsInEfByIndex(int efid, String newTag,
            String newPhoneNumber, int index,
            String pin2);

    /**
     * Update an ADN-like EF record by record index
     *
     * This is useful for iteration the whole ADN file, such as write the whole
     * phone book or erase/format the whole phonebook.
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
     *
     * {@hide}
     */
    int
    updateAdnRecordsInEfByIndexWithError(int efid, String newTag,
            String newPhoneNumber, int index, String pin2);

    /**
     * Update an USIM phonebook contacts by record index
     *
     * This is useful for iteration the whole ADN file, such as write the whole
     * phone book or erase/format the whole phonebook.
     *
     * This method will return why the error occurs
     *
     * @param efid must be one among EF_ADN, EF_FDN, and EF_SDN
     * @param newTag adn tag to be stored
     * @param newPhoneNumber adn number to be stored
     * @param newAnr  new additional number string to be stored 
     * @param newGrpIds group id list for the adn record.
     * @param newEmails Emails for the adn record.     
     *        Set both newTag and newPhoneNubmer to "" means to replace the old
     *        record with empty one, aka, delete old record
     * @param index is 1-based adn record index to be updated
     * @return ERROR_ICC_PROVIDER_* defined in the IccProvider
     *
     * {@hide}
     */
    int
    updateUsimPBRecordsInEfByIndexWithError(int efid, String newTag,
            String newPhoneNumber, String newAnr,  in String newGrpIds, in String[] newEmails, int index);

    /**
    *{@hide}
    */
    int updateUsimPBRecordsByIndexWithError(int efid, in AdnRecord record, int index);
    
    /**
    *{@hide}
    */
    int updateUsimPBRecordsBySearchWithError(int efid, in AdnRecord oldAdn, in AdnRecord newAdn);
    
    /**
     * Get the max munber of records in efid
     *
     * @param efid the EF id of a ADN-like SIM
     * @return  int[3] array
     *            recordSizes[0]  is the single record length
     *            recordSizes[1]  is the total length of the EF file
     *            recordSizes[2]  is the number of records in the EF file
     */
    int[] getAdnRecordsSize(int efid);

    /**
     * Judge if the PHB subsystem is ready or not
     *
     * @return  true for ready
     * {@hide}
     */
    boolean isPhbReady();

    List<UsimGroup> getUsimGroups();    

    String getUSIMGroupById(int nGasId);

    boolean removeUSIMGroupById(int nGasId);

    int insertUSIMGroup(String grpName);

    int updateUSIMGroup(int nGasId, String grpName);
    
    boolean addContactToGroup(int adnIndex, int grpIndex);

    boolean removeContactFromGroup(int adnIndex, int grpIndex);

    int hasExistGroup(String grpName);
    
    int getUSIMGrpMaxNameLen();
    
    int getUSIMGrpMaxCount();
    /**
    *{@hide}
    */
    List<AlphaTag> getUSIMAASList();
    /**
    *{@hide}
    */
    String getUSIMAASById(int index);
    /**
    *{@hide}
    */
    int insertUSIMAAS(String aasName);
    /**
    *{@hide}
    */
    int getAnrCount();
    /**
    *{@hide}
    */
    int getUSIMAASMaxCount();
    /**
    *{@hide}
    */
    int getUSIMAASMaxNameLen();
    /**
    *{@hide}
    */
    boolean updateUSIMAAS(int index, int pbrIndex, String aasName);
    /**
    *{@hide}
    */
    boolean removeUSIMAASById(int index, int pbrIndex);
}
