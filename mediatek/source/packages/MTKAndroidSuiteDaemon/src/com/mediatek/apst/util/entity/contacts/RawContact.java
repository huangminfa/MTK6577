/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.apst.util.entity.contacts;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: RawContact
 * <p>Package: com.mediatek.apst.util.entity.contacts
 * <p>Created on: 2010-6-17
 * <p>
 * <p>Description: 
 * <p>Raw contact entity before aggregation.
 *
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class RawContact extends BaseContact implements Cloneable{
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    // Aggregation mode definition constants
    /*public static final int AGGREGATION_MODE_DEFAULT    = 0;
    public static final int AGGREGATION_MODE_IMMEDIATE  = 1;
    public static final int AGGREGATION_MODE_SUSPENDED  = 2;
    public static final int AGGREGATION_MODE_DISABLED   = 3;*/
    
    /* For MTK SIM Contacts feature. 
     * Currently, contacts stored in contacts' database may be 2 kinds:
     * 1. Originally from phone storage, typically they are added by user via 
     * the application "Contacts". In this case, we say it's 'sourced' from 
     * phone storage.
     * 2. Originally from SIM card. MTK's "Contacts" application will 
     * automatically load contacts stored on SIM card and then add them into 
     * database. In that case, we say it's 'sourced' from SIM card. For 
     * single-SIM, there is only one source 'SIM'. For dual-SIM, there is 2 
     * source, 'SIM1' and 'SIM2'.
     */
    /** Source location is unknown. */
    public static final int SOURCE_NONE = -255;
    /** Source location is phone. */
    public static final int SOURCE_PHONE = -1;
    /** Source location is SIM card(single-SIM). */
    public static final int SOURCE_SIM = 0;
    /** Source location is SIM card 1(dual-SIM). */
    public static final int SOURCE_SIM1 = 1;
    /** Source location is SIM card 2(dual-SIM). */
    public static final int SOURCE_SIM2 = 2;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    private long contactId;
    
    /*private int aggregationMode;*/
    
    /*private boolean deleted;*/
    
    private int timesContacted;
    
    private long lastTimeContacted;
    
    private boolean starred;
    
    private String customRingtone;
    
    private boolean sendToVoicemail; 
    
    /*private String accountName;*/
    
    /*private String accountType;*/
    
    /*private String sourceId;*/
    
    private int version;
    
    private boolean dirty;
    
    private List<StructuredName> names;
    
    private List<Phone> phones;
    
    private List<Photo> photos;
    
    private List<Email> emails;
    
    private List<Im> ims;
    
    private List<StructuredPostal> postals;
    
    private List<Organization> organizations;
    
    private List<Note> notes;
    
    private List<Nickname> nicknames;
    
    private List<Website> websites;
    
    // For MTK SIM Contacts feature.
    private int sourceLocation;
    // Added by Shaoying Han 2011.04.06
    private int simId;
    
//    private String iccid;
    private int simIndex;
    
    
    


    //==============================================================
    // Constructors                                                 
    //==============================================================
    /**
     * Constructor.
     * @param id The database id of the raw contact.
     */
    public RawContact(long id) {
        super(id);
        this.contactId = ID_NULL;
        /*this.aggregationMode = AGGREGATION_MODE_DEFAULT;
        this.deleted = false;*/
        this.timesContacted = 0;
        this.starred = false;
        this.sendToVoicemail = false;
        this.version = -1;
        /*this.accountName = null;
        this.accountType = null;
        this.dirty = false;*/
        
        this.names = new ArrayList<StructuredName>();
        this.phones = new ArrayList<Phone>();
        this.photos = new ArrayList<Photo>();
        this.emails = new ArrayList<Email>();
        this.ims = new ArrayList<Im>();
        this.postals = new ArrayList<StructuredPostal>();
        this.organizations = new ArrayList<Organization>();
        this.notes = new ArrayList<Note>();
        this.nicknames = new ArrayList<Nickname>();
        this.websites = new ArrayList<Website>();
        
        // For MTK SIM Contacts feature.
        this.sourceLocation = SOURCE_PHONE;
    }
    
    public RawContact(){
        this(ID_NULL);
    }
    
    /**
     * Constructor. This will construct a new <b>RawContact</b> instance by 
     * extending the given <b>BaseContact</b>, that is, keep the fields of 
     * the BaseContact part in the new <b>RawContact</b> instance.
     * @param baseContact The <b>BaseContact</b> object to extend.
     */
    public RawContact(BaseContact baseContact){
        this(baseContact.getId());
        super.setDisplayName(baseContact.getDisplayName());
        super.setPrimaryNumber(baseContact.getPrimaryNumber());
        super.setStoreLocation(baseContact.getStoreLocation());
        super.getGroupMemberships().addAll(baseContact.getGroupMemberships());
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    
    public long getContactId() {
        return contactId;
    }
    
    /*
    public int getAggregationMode() {
        return aggregationMode;
    }
    */
    
    /*
    public boolean isDeleted() {
        return deleted;
    }
    */
    
    public int getTimesContacted() {
        return timesContacted;
    }
    
    public long getLastTimeContacted() {
        return lastTimeContacted;
    }
    
    public boolean isStarred() {
        return starred;
    }
    
    public String getCustomRingtone() {
        return customRingtone;
    }
    
    public boolean isSendToVoicemail() {
        return sendToVoicemail;
    }
    
    /*
    public String getAccountName() {
        return accountName;
    }
    */
    
    /*
    public String getAccountType() {
        return accountType;
    }
    */
    
    /*
    public String getSourceId() {
        return sourceId;
    }
    */
    
    public int getVersion() {
        return version;
    }
    
    public boolean isDirty() {
        return dirty;
    }
    
    public List<StructuredName> getNames() {
        return names;
    }
    
    public List<Phone> getPhones() {
        return phones;
    }
    
    public List<Photo> getPhotos() {
        return photos;
    }
    
    public List<Email> getEmails() {
        return emails;
    }
    
    public List<Im> getIms() {
        return ims;
    }
    
    public List<StructuredPostal> getPostals() {
        return postals;
    }
    
    public List<Organization> getOrganizations() {
        return organizations;
    }
    
    public List<Note> getNotes() {
        return notes;
    }
    
    public List<Nickname> getNicknames() {
        return nicknames;
    }
    
    public List<Website> getWebsites() {
        return websites;
    }
    
    // For MTK SIM Contacts feature.
    public int getSourceLocation() {
        return sourceLocation;
    }

    public int getSimId() {
		return simId;
	}
    
    public int getSimIndex(){
    	return simIndex;
    }
    
    
//    public String getIccid() {
//        return iccid;
//    }



    //==============================================================
    // Setters                                                      
    //==============================================================
    
    public void setContactId(long contactId) {
        this.contactId = contactId;
    }
    
    /*
    public void setAggregationMode(int aggregationMode) {
        this.aggregationMode = aggregationMode;
    }
    */
    
    /*
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    */
    
    public void setTimesContacted(int timesContacted) {
        this.timesContacted = timesContacted;
    }
    
    public void setLastTimeContacted(long lastTimeContacted) {
        this.lastTimeContacted = lastTimeContacted;
    }
    
    public void setStarred(boolean starred) {
        this.starred = starred;
    }
    
    public void setCustomRingtone(String customRingtone) {
        this.customRingtone = customRingtone;
    }
    
    public void setSendToVoicemail(boolean sendToVoicemail) {
        this.sendToVoicemail = sendToVoicemail;
    }
    
    /*
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    */
    
    /*
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    */
    
    /*
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    */
    
    public void setVersion(int version) {
        this.version = version;
    }
    
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
    
    public boolean setNames(ArrayList<StructuredName> names) {
        if (names == null){
            return false;
        } else {
            this.names = names;
            return true;
        }
    }
    
    public boolean setPhones(ArrayList<Phone> phones) {
        if (phones == null){
            return false;
        } else {
            this.phones = phones;
            return true;
        }
    }
    
    public boolean setPhotos(ArrayList<Photo> photos) {
        if (photos == null){
            return false;
        } else {
            this.photos = photos;
            return true;
        }
    }
    
    public boolean setEmails(ArrayList<Email> emails) {
        if (emails == null){
            return false;
        } else {
            this.emails = emails;
            return true;
        }
    }
    
    public boolean setIms(ArrayList<Im> ims) {
        if (ims == null){
            return false;
        } else {
            this.ims = ims;
            return true;
        }
    }
    
    public boolean setPostals(ArrayList<StructuredPostal> postals) {
        if (postals == null){
            return false;
        } else {
            this.postals = postals;
            return true;
        }
    }
    
    public boolean setOrganizations(ArrayList<Organization> organizations) {
        if (organizations == null){
            return false;
        } else {
            this.organizations = organizations;
            return true;
        }
    }
    
    public boolean setNotes(ArrayList<Note> notes) {
        if (notes == null){
            return false;
        } else {
            this.notes = notes;
            return true;
        } 
    }
    
    public boolean setNicknames(ArrayList<Nickname> nicknames) {
        if (nicknames == null){
            return false;
        } else {
            this.nicknames = nicknames;
            return true;
        }
    }
    
    public boolean setWebsites(ArrayList<Website> websites) {
        if (websites == null){
            return false;
        } else {
            this.websites = websites;
            return true;
        }
    }
    
    public boolean setNames(Vector<StructuredName> names) {
        if (names == null){
            return false;
        } else {
            this.names = names;
            return true;
        }
    }
    
    public boolean setPhones(Vector<Phone> phones) {
        if (phones == null){
            return false;
        } else {
            this.phones = phones;
            return true;
        }
    }
    
    public boolean setPhotos(Vector<Photo> photos) {
        if (photos == null){
            return false;
        } else {
            this.photos = photos;
            return true;
        }
    }
    
    public boolean setEmails(Vector<Email> emails) {
        if (emails == null){
            return false;
        } else {
            this.emails = emails;
            return true;
        }
    }
    
    public boolean setIms(Vector<Im> ims) {
        if (ims == null){
            return false;
        } else {
            this.ims = ims;
            return true;
        }
    }
    
    public boolean setPostals(Vector<StructuredPostal> postals) {
        if (postals == null){
            return false;
        } else {
            this.postals = postals;
            return true;
        }
    }
    
    public boolean setOrganizations(Vector<Organization> organizations) {
        if (organizations == null){
            return false;
        } else {
            this.organizations = organizations;
            return true;
        }
    }
    
    public boolean setNotes(Vector<Note> notes) {
        if (notes == null){
            return false;
        } else {
            this.notes = notes;
            return true;
        } 
    }
    
    public boolean setNicknames(Vector<Nickname> nicknames) {
        if (nicknames == null){
            return false;
        } else {
            this.nicknames = nicknames;
            return true;
        }
    }
    
    public boolean setWebsites(Vector<Website> websites) {
        if (websites == null){
            return false;
        } else {
            this.websites = websites;
            return true;
        }
    }
    
    // For MTK SIM Contacts feature.
    public void setSourceLocation(int sourceLocation){
        this.sourceLocation = sourceLocation;
    }
    
	public void setSimId(int simId) {
		this.simId = simId;
	}
	
//    public void setIccid(String iccid) {
//        this.iccid = iccid;
//    }
	
	public void setSimIndex(int simIndex){
		this.simIndex = simIndex;
	}
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    /**
     * Deep copy. This means all data fields objects(including names, phones) 
     * will also be copied, not just the references.
     * @return A deep copy.
     * @throws CloneNotSupportedException
     */
    //@Override
    public RawContact clone() throws CloneNotSupportedException{
        RawContact copy = (RawContact)(super.clone());
        
        copy.names = new ArrayList<StructuredName>();
        copy.phones = new ArrayList<Phone>();
        copy.photos = new ArrayList<Photo>();
        copy.emails = new ArrayList<Email>();
        copy.ims = new ArrayList<Im>();
        copy.postals = new ArrayList<StructuredPostal>();
        copy.organizations = new ArrayList<Organization>();
        copy.notes = new ArrayList<Note>();
        copy.nicknames = new ArrayList<Nickname>();
        copy.websites = new ArrayList<Website>();
        
        for (StructuredName name : this.names){
            copy.names.add(name.clone());
        }
        
        for (Phone phone : this.phones){
            copy.phones.add(phone.clone());
        }
        
        for (Photo photo : this.photos){
            copy.photos.add(photo.clone());
        }
        
        for (Email email : this.emails){
            copy.emails.add(email.clone());
        }
        
        for (Im im : this.ims){
            copy.ims.add(im.clone());
        }
        
        for (StructuredPostal postal : this.postals){
            copy.postals.add(postal.clone());
        }
        
        for (Organization organization : this.organizations){
            copy.organizations.add(organization.clone());
        }
        
        for (Note note : this.notes){
            copy.notes.add(note.clone());
        }
        
        for (Nickname nickname : this.nicknames){
            copy.nicknames.add(nickname.clone());
        }
        
        for (Website website : this.websites){
            copy.websites.add(website.clone());
        }
        
        return copy;
    }
    
    /**
     * Shallow copy. This means all the copy's data fields are the same objects 
     * in the original raw contact. What is copied is references, but not 
     * objects.
     * @return A shallow copy.
     * @throws CloneNotSupportedException
     */
    public RawContact shallowClone() throws CloneNotSupportedException{
        RawContact copy = (RawContact)(super.clone());
        
        copy.names = new ArrayList<StructuredName>();
        copy.phones = new ArrayList<Phone>();
        copy.photos = new ArrayList<Photo>();
        copy.emails = new ArrayList<Email>();
        copy.ims = new ArrayList<Im>();
        copy.postals = new ArrayList<StructuredPostal>();
        copy.organizations = new ArrayList<Organization>();
        copy.notes = new ArrayList<Note>();
        copy.nicknames = new ArrayList<Nickname>();
        copy.websites = new ArrayList<Website>();
        
        for (StructuredName name : this.names){
            copy.names.add(name);
        }
        
        for (Phone phone : this.phones){
            copy.phones.add(phone);
        }
        
        for (Photo photo : this.photos){
            copy.photos.add(photo);
        }
        
        for (Email email : this.emails){
            copy.emails.add(email);
        }
        
        for (Im im : this.ims){
            copy.ims.add(im);
        }
        
        for (StructuredPostal postal : this.postals){
            copy.postals.add(postal);
        }
        
        for (Organization organization : this.organizations){
            copy.organizations.add(organization);
        }
        
        for (Note note : this.notes){
            copy.notes.add(note);
        }
        
        for (Nickname nickname : this.nicknames){
            copy.nicknames.add(nickname);
        }
        
        for (Website website : this.websites){
            copy.websites.add(website);
        }
        
        return copy;
    }
    
    /**
     * Get a list of all contact data. Data will be filled and returned in the 
     * following sequence: 
     * <p>Names
     * <p>Phones
     * <p>Photos
     * <p>E-mails
     * <p>IMs
     * <p>Postals
     * <p>Organizations
     * <p>Notes
     * <p>Nicknames
     * <p>Websites
     * <p>Group memberships
     * @return The list containing all data.
     */
    public List<ContactData> getAllContactData(){
        List<ContactData> data = new ArrayList<ContactData>();
        data.addAll(this.names);
        data.addAll(this.phones);
        data.addAll(this.photos);
        data.addAll(this.emails);
        data.addAll(this.ims);
        data.addAll(this.postals);
        data.addAll(this.organizations);
        data.addAll(this.notes);
        data.addAll(this.nicknames);
        data.addAll(this.websites);
        data.addAll(this.getGroupMemberships());
        
        return data;
    }
    
    /**
     * Get a count of contact data.
     * @return The count of contact data.
     */
    public int getContactDataCount(){
        return this.names.size() 
            + this.phones.size()
            + this.photos.size()
            + this.emails.size()
            + this.ims.size()
            + this.postals.size()
            + this.organizations.size()
            + this.notes.size()
            + this.nicknames.size()
            + this.websites.size()
            + this.getGroupMemberships().size();
    }
    
    /**
     * Convenient method for adding a contact data.
     * @param data A strip of contact data.
     */
    public void addContactData(ContactData data) throws 
    UnknownContactDataTypeException{
        if (null == data){
            return;
        }
        if (data instanceof StructuredName){
            getNames().add((StructuredName)data);
        } else if (data instanceof Phone){
            getPhones().add((Phone)data);
        } else if (data instanceof Photo){
            getPhotos().add((Photo)data);
        } else if (data instanceof Im){
            getIms().add((Im)data);
        } else if (data instanceof Email){
            getEmails().add((Email)data);
        } else if (data instanceof StructuredPostal){
            getPostals().add((StructuredPostal)data);
        } else if (data instanceof Organization){
            getOrganizations().add((Organization)data);
        } else if (data instanceof Nickname){
            getNicknames().add((Nickname)data);
        } else if (data instanceof Website){
            getWebsites().add((Website)data);
        } else if (data instanceof Note){
            getNotes().add((Note)data);
        } else if (data instanceof GroupMembership){
            getGroupMemberships().add((GroupMembership)data);
        } else {
            throw new UnknownContactDataTypeException(data.getClass().getName(), 
                    data.getMimeType());
        }
    }
    
    /**
     * Clear all contact data.
     */
    public void clearAllData(){
        getNames().clear();
        getPhones().clear();
        getPhotos().clear();
        getIms().clear();
        getEmails().clear();
        getPostals().clear();
        getOrganizations().clear();
        getNicknames().clear();
        getWebsites().clear();
        getNotes().clear();
        getGroupMemberships().clear();
    }
    
    //@Override
    /**
     * @deprecated
     */
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        super.writeRaw(buffer);
        // starred
        RawTransUtil.putBoolean(buffer, this.starred);
        // sendToVoicemail
        RawTransUtil.putBoolean(buffer, this.sendToVoicemail);
        // version
        buffer.putInt(this.version);
        // names
        if (null != names){
            buffer.putInt(names.size());
            for (StructuredName name : names){
                name.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // phones
        if (null != phones){
            buffer.putInt(phones.size());
            for (Phone phone : phones){
                phone.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // photos
        if (null != photos){
            buffer.putInt(photos.size());
            for (Photo photo : photos){
                photo.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // emails
        if (null != emails){
            buffer.putInt(emails.size());
            for (Email email : emails){
                email.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // ims
        if (null != ims){
            buffer.putInt(ims.size());
            for (Im im : ims){
                im.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // postals
        if (null != postals){
            buffer.putInt(postals.size());
            for (StructuredPostal postal : postals){
                postal.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // organizations
        if (null != organizations){
            buffer.putInt(organizations.size());
            for (Organization organization : organizations){
                organization.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // notes
        if (null != notes){
            buffer.putInt(notes.size());
            for (Note note : notes){
                note.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // nicknames
        if (null != nicknames){
            buffer.putInt(nicknames.size());
            for (Nickname nickname : nicknames){
                nickname.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // websites
        if (null != websites){
            buffer.putInt(websites.size());
            for (Website website : websites){
                website.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        
        // For MTK SIM Contacts feature.
        // sourceLocation
        buffer.putInt(this.sourceLocation);
    }
    
    //@Override
    /**
     * @deprecated
     */
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        super.readRaw(buffer);
        // starred
        this.starred = RawTransUtil.getBoolean(buffer);
        // sendToVoicemail
        this.sendToVoicemail = RawTransUtil.getBoolean(buffer);
        // version
        this.version = buffer.getInt();
        // names
        int size = buffer.getInt();
        if (size >= 0){
            this.names = new ArrayList<StructuredName>(size);
            for (int i = 0; i < size; i++){
                StructuredName name = new StructuredName();
                name.readRaw(buffer);
                names.add(name);
            }
        } else {
            this.names = null;
        }
        // phones
        size = buffer.getInt();
        if (size >= 0){
            this.phones = new ArrayList<Phone>(size);
            for (int i = 0; i < size; i++){
                Phone phone = new Phone();
                phone.readRaw(buffer);
                phones.add(phone);
            }
        } else {
            this.phones = null;
        }
        // photos
        size = buffer.getInt();
        if (size >= 0){
            this.photos = new ArrayList<Photo>(size);
            for (int i = 0; i < size; i++){
                Photo photo = new Photo();
                photo.readRaw(buffer);
                photos.add(photo);
            }
        } else {
            this.photos = null;
        }
        // emails
        size = buffer.getInt();
        if (size >= 0){
            this.emails = new ArrayList<Email>(size);
            for (int i = 0; i < size; i++){
                Email email = new Email();
                email.readRaw(buffer);
                emails.add(email);
            }
        } else {
            this.emails = null;
        }
        // ims
        size = buffer.getInt();
        if (size >= 0){
            this.ims = new ArrayList<Im>(size);
            for (int i = 0; i < size; i++){
                Im im = new Im();
                im.readRaw(buffer);
                ims.add(im);
            }
        } else {
            this.ims = null;
        }
        // postals
        size = buffer.getInt();
        if (size >= 0){
            this.postals = new ArrayList<StructuredPostal>(size);
            for (int i = 0; i < size; i++){
                StructuredPostal postal = new StructuredPostal();
                postal.readRaw(buffer);
                postals.add(postal);
            }
        } else {
            this.postals = null;
        }
        // organizations
        size = buffer.getInt();
        if (size >= 0){
            this.organizations = new ArrayList<Organization>(size);
            for (int i = 0; i < size; i++){
                Organization organization = new Organization();
                organization.readRaw(buffer);
                organizations.add(organization);
            }
        } else {
            this.organizations = null;
        }
        // notes
        size = buffer.getInt();
        if (size >= 0){
            this.notes = new ArrayList<Note>(size);
            for (int i = 0; i < size; i++){
                Note note = new Note();
                note.readRaw(buffer);
                notes.add(note);
            }
        } else {
            this.notes = null;
        }
        // nicknames
        size = buffer.getInt();
        if (size >= 0){
            this.nicknames = new ArrayList<Nickname>(size);
            for (int i = 0; i < size; i++){
                Nickname nickname = new Nickname();
                nickname.readRaw(buffer);
                nicknames.add(nickname);
            }
        } else {
            this.nicknames = null;
        }
        // websites
        size = buffer.getInt();
        if (size >= 0){
            this.websites = new ArrayList<Website>(size);
            for (int i = 0; i < size; i++){
                Website website = new Website();
                website.readRaw(buffer);
                websites.add(website);
            }
        } else {
            this.websites = null;
        }
        
        // For MTK SIM Contacts feature.
        // sourceLocation
        this.sourceLocation = buffer.getInt();
    }
    
    @Override
	public void writeRawWithVersion(ByteBuffer buffer, int versionCode)
			throws NullPointerException, BufferOverflowException {
        super.writeRawWithVersion(buffer, versionCode);
        // starred
        RawTransUtil.putBoolean(buffer, this.starred);
        
        // sendToVoicemail
        RawTransUtil.putBoolean(buffer, this.sendToVoicemail);
        
        // version
        buffer.putInt(this.version);
        // names
        if (null != names){
            buffer.putInt(names.size());
            for (StructuredName name : names){
                name.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // phones
        if (null != phones){
            buffer.putInt(phones.size());
            for (Phone phone : phones){
                // phone.writeRaw(buffer);
            	// Modified by Shaoying Han
            	phone.writeRawWithVersion(buffer, versionCode);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // photos
        if (null != photos){
            buffer.putInt(photos.size());
            for (Photo photo : photos){
                photo.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // emails
        if (null != emails){
            buffer.putInt(emails.size());
            for (Email email : emails){
                email.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // ims
        if (null != ims){
            buffer.putInt(ims.size());
            for (Im im : ims){
                im.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // postals
        if (null != postals){
            buffer.putInt(postals.size());
            for (StructuredPostal postal : postals){
                postal.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // organizations
        if (null != organizations){
            buffer.putInt(organizations.size());
            for (Organization organization : organizations){
                organization.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // notes
        if (null != notes){
            buffer.putInt(notes.size());
            for (Note note : notes){
                note.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // nicknames
        if (null != nicknames){
            buffer.putInt(nicknames.size());
            for (Nickname nickname : nicknames){
                nickname.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        // websites
        if (null != websites){
            buffer.putInt(websites.size());
            for (Website website : websites){
                website.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
        
        // For MTK SIM Contacts feature.
        // sourceLocation
        buffer.putInt(this.sourceLocation);
        
        if (versionCode >= 0x00000002) {
        	// simId. Added by Shaoying Han
            buffer.putInt(this.simId);
//            RawTransUtil.putString(buffer, this.iccid);           
        }
        buffer.putInt(this.simIndex);
    }

	@Override
	public void readRawWithVersion(ByteBuffer buffer, int versionCode)
			throws NullPointerException, BufferUnderflowException {
        super.readRawWithVersion(buffer, versionCode);
        // starred
        this.starred = RawTransUtil.getBoolean(buffer);
        

        // sendToVoicemail
        this.sendToVoicemail = RawTransUtil.getBoolean(buffer);
        
        // version
        this.version = buffer.getInt();
        // names
        int size = buffer.getInt();
        if (size >= 0){
            this.names = new ArrayList<StructuredName>(size);
            for (int i = 0; i < size; i++){
                StructuredName name = new StructuredName();
                name.readRaw(buffer);
                names.add(name);
            }
        } else {
            this.names = null;
        }
        // phones
        size = buffer.getInt();
        if (size >= 0){
            this.phones = new ArrayList<Phone>(size);
            for (int i = 0; i < size; i++){
                Phone phone = new Phone();
                // phone.readRaw(buffer);
                // Modified by Shaoying Han
                phone.readRawWithVersion(buffer, versionCode);
                phones.add(phone);
            }
        } else {
            this.phones = null;
        }
        // photos
        size = buffer.getInt();
        if (size >= 0){
            this.photos = new ArrayList<Photo>(size);
            for (int i = 0; i < size; i++){
                Photo photo = new Photo();
                photo.readRaw(buffer);
                photos.add(photo);
            }
        } else {
            this.photos = null;
        }
        // emails
        size = buffer.getInt();
        if (size >= 0){
            this.emails = new ArrayList<Email>(size);
            for (int i = 0; i < size; i++){
                Email email = new Email();
                email.readRaw(buffer);
                emails.add(email);
            }
        } else {
            this.emails = null;
        }
        // ims
        size = buffer.getInt();
        if (size >= 0){
            this.ims = new ArrayList<Im>(size);
            for (int i = 0; i < size; i++){
                Im im = new Im();
                im.readRaw(buffer);
                ims.add(im);
            }
        } else {
            this.ims = null;
        }
        // postals
        size = buffer.getInt();
        if (size >= 0){
            this.postals = new ArrayList<StructuredPostal>(size);
            for (int i = 0; i < size; i++){
                StructuredPostal postal = new StructuredPostal();
                postal.readRaw(buffer);
                postals.add(postal);
            }
        } else {
            this.postals = null;
        }
        // organizations
        size = buffer.getInt();
        if (size >= 0){
            this.organizations = new ArrayList<Organization>(size);
            for (int i = 0; i < size; i++){
                Organization organization = new Organization();
                organization.readRaw(buffer);
                organizations.add(organization);
            }
        } else {
            this.organizations = null;
        }
        // notes
        size = buffer.getInt();
        if (size >= 0){
            this.notes = new ArrayList<Note>(size);
            for (int i = 0; i < size; i++){
                Note note = new Note();
                note.readRaw(buffer);
                notes.add(note);
            }
        } else {
            this.notes = null;
        }
        // nicknames
        size = buffer.getInt();
        if (size >= 0){
            this.nicknames = new ArrayList<Nickname>(size);
            for (int i = 0; i < size; i++){
                Nickname nickname = new Nickname();
                nickname.readRaw(buffer);
                nicknames.add(nickname);
            }
        } else {
            this.nicknames = null;
        }
        // websites
        size = buffer.getInt();
        if (size >= 0){
            this.websites = new ArrayList<Website>(size);
            for (int i = 0; i < size; i++){
                Website website = new Website();
                website.readRaw(buffer);
                websites.add(website);
            }
        } else {
            this.websites = null;
        }
        
        // For MTK SIM Contacts feature.
        // sourceLocation
        this.sourceLocation = buffer.getInt();
        if (versionCode >= 0x00000002) {
        	// simId. Added by Shaoying Han
            this.simId = buffer.getInt();
//            this.iccid=RawTransUtil.getString(buffer);
        }
        this.simIndex = buffer.getInt();
    }

	//==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    public class UnknownContactDataTypeException extends Exception{
        private static final long serialVersionUID = 1L;
        
        private String mClassName;
        
        private int mMimeType;
        
        public UnknownContactDataTypeException(String className, 
                int mimeType){
            mClassName = className;
            mMimeType = mimeType;
        }
        
        //@Override
        public String toString(){
            return "Unknown type of contact data, its class name is " + 
            mClassName + ", MIME type is " + mMimeType;
        }
    }
}
