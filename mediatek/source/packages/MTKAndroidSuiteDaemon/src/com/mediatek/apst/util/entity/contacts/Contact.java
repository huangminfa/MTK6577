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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.mediatek.apst.util.entity.DatabaseRecordEntity;

/**
 * Class Name: Contact
 * <p>Package: com.mediatek.apst.util.entity.contacts
 * <p>Created on: 2010-6-11
 * <p>
 * <p>Description: 
 * <p>Contact after aggregation.
 *
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 * @deprecated In current version of Android PC Sync Tool, contacts aggregation 
 * is not a implemented feature, and this class is reserved. Use RawContact 
 * instead.
 * @see RawContact
 */
public class Contact extends DatabaseRecordEntity implements Cloneable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    private String displayName;
    
    private long photoId;
    
    private String customRingtone;
    
    private boolean sendToVoicemail;
    
    private int timesContacted;
    
    private long lastTimeContacted;
    
    private boolean starred;
    
    private boolean inVisibleGroup;
    
    private boolean hasPhoneNumber;
    
    private String lookup;
    
    /*private long statusUpdateId;*/
    
    /*private boolean singleIsRestricted;*/
    
    private List<RawContact> rawContacts;
    
    
    private boolean threadSafe;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    
    public Contact(long id, boolean shouldThreadSafe) {
        super(id);
        this.sendToVoicemail = false;
        this.timesContacted = 0;
        this.starred = false;
        this.inVisibleGroup = true;
        this.hasPhoneNumber = false;
        /*this.singleIsRestricted = false;*/
        
        if(shouldThreadSafe){
            this.rawContacts = new Vector<RawContact>();
            threadSafe = true;
        }else{
            this.rawContacts = new ArrayList<RawContact>();
            threadSafe = false;
        }
    }
    
    public Contact(long id){
        this(id, false);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    
    public String getDisplayName() {
        return displayName;
    }

    public long getPhotoId() {
        return photoId;
    }

    public String getCustomRingtone() {
        return customRingtone;
    }

    public boolean isSendToVoicemail() {
        return sendToVoicemail;
    }

    public int getTimesContacted() {
        return timesContacted;
    }

    public long getLastTimeContacted() {
        return lastTimeContacted;
    }

    public boolean isStarred() {
        return starred;
    }

    public boolean isInVisibleGroup() {
        return inVisibleGroup;
    }

    public boolean isHasPhoneNumber() {
        return hasPhoneNumber;
    }

    public String getLookup() {
        return lookup;
    }
    
    /*
    public long getStatusUpdateId() {
        return statusUpdateId;
    }
    */
    
    /*
    public boolean isSingleIsRestricted() {
        return singleIsRestricted;
    }
    */
    /**
     * @deprecated Should use appropriate encapsulated methods instead, e.g. 
     * addRawContact(rawContact) instead of getRawContacts.add(rawContact)
     * @return All raw contacts belongs to this contact in a list.
     */
    public List<RawContact> getRawContacts(){
        return rawContacts;
    }
    
    public boolean isThreadSafe(){
        return threadSafe;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setPhotoId(long photoId) {
        this.photoId = photoId;
    }

    public void setCustomRingtone(String customRingtone) {
        this.customRingtone = customRingtone;
    }

    public void setSendToVoicemail(boolean sendToVoicemail) {
        this.sendToVoicemail = sendToVoicemail;
    }

    public void setTimesContacted(int timesContacted) {
        this.timesContacted = timesContacted;
    }

    public void setLastTimeContacted(long lastTimeContacted) {
        this.lastTimeContacted = lastTimeContacted;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public void setInVisibleGroup(boolean inVisibleGroup) {
        this.inVisibleGroup = inVisibleGroup;
    }

    public void setHasPhoneNumber(boolean hasPhoneNumber) {
        this.hasPhoneNumber = hasPhoneNumber;
    }

    public void setLookup(String lookup) {
        this.lookup = lookup;
    }
    /*
    public void setStatusUpdateId(long statusUpdateId) {
        this.statusUpdateId = statusUpdateId;
    }
    */
    /*
    public void setSingleIsRestricted(boolean singleIsRestricted) {
        this.singleIsRestricted = singleIsRestricted;
    }
    */
    
    public boolean setRawContacts(ArrayList<RawContact> rawContacts){
        if (rawContacts == null){
            return false;
        }
        
        if (!threadSafe){
            this.rawContacts = rawContacts;
            return true;
        }else{
            return false;
        }
    }
    
    public boolean setRawContacts(Vector<RawContact> rawContacts){
        if (rawContacts == null){
            return false;
        }
        
        if (threadSafe){
            this.rawContacts = rawContacts;
            return true;
        }else{
            return false;
        }
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    //@Override
    public Contact clone() throws CloneNotSupportedException{
        Contact copy = (Contact)(super.clone());

        if (copy.threadSafe){
            copy.rawContacts = new Vector<RawContact>();
        }else{
            copy.rawContacts = new ArrayList<RawContact>();
        }
        // Also deep copy all raw contacts belong to it
        for (RawContact rawContact : this.rawContacts){
            copy.rawContacts.add(rawContact.clone());
        }
        
        return copy;
    }
    
    public Contact shallowClone() throws CloneNotSupportedException{
        Contact copy = (Contact)(super.clone());

        if (copy.threadSafe){
            copy.rawContacts = new Vector<RawContact>();
        }else{
            copy.rawContacts = new ArrayList<RawContact>();
        }
        // Just pass all the raw contacts' references
        for (RawContact rawContact : this.rawContacts){
            copy.rawContacts.add(rawContact);
        }
        
        return copy;
    }
    
    public int getRawContactsCount(){
        return rawContacts.size();
    }
    
    public boolean addRawContact(RawContact rawContact){
        return rawContacts.add(rawContact);
    }
    
    public boolean addAll(List<RawContact> rawContacts){
        return rawContacts.addAll(rawContacts);
    }
    
    public boolean removeRawContact(RawContact rawContact){
        return rawContacts.remove(rawContact);
    }
    
    public RawContact removeRawContact(int location){
        return rawContacts.remove(location);
    }
    
    public boolean removeAll(List<RawContact> rawContacts){
        return rawContacts.removeAll(rawContacts);
    }
    
    public void clear(){
        rawContacts.clear();
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
