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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.mediatek.apst.util.entity.DatabaseRecordEntity;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: Group
 * <p>Package: com.mediatek.apst.util.entity.contacts
 * <p>Created on: 2010-6-11
 * <p>
 * <p>Description: 
 * <p>Represents a typical contacts group. One contact can belong to multiple 
 * groups at the same time.
 *
 * @author mtk80734 Siyang.Miao
 * @version V1.1
 */
public class Group extends DatabaseRecordEntity implements Cloneable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    private String title;
    
    private String notes;
    
    private String systemId;
    
    /*
     * added by Yu ,for restore GROUP
     */
    
    private String deleted;
    
    private String account_name;
    
    private String account_type;
    
    private String version;
    
    private String dirty;
    
    private String group_visible;
    
    private String should_sync;


    /**
     * Read-only in ContentProvider.
     */
    /*private boolean groupVisible;*/
    
    /*private boolean shouldSync;*/
    
    private List<BaseContact> members;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    
    public Group(long id){
        super(id);
        /*this.deleted = false;
        this.groupVisible = false;
        this.shouldSync = true;*/
        this.members = new ArrayList<BaseContact>();
    }
    
    public Group(){
        this(ID_NULL);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================

    public String getTitle() {
        return title;
    }

    public String getNotes() {
        return notes;
    }

    public String getSystemId() {
        return systemId;
    }

    public String getDeleted() {
        return deleted;
    }

    public String getAccount_name() {
        return account_name;
    }

    public String getAccount_type() {
        return account_type;
    }

    public String getVersion() {
        return version;
    }

    public String getDirty() {
        return dirty;
    }

    public String getGroup_visible() {
        return group_visible;
    }

    public String getShould_sync() {
        return should_sync;
    }
    
    /**
     * @deprecated Should use appropriate encapsulated methods instead, e.g. 
     * addMember(member) instead of getMembers.add(member)
     * @return Group members in a list of base contacts.
     */
    public List<BaseContact> getMembers() {
        return members;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }

    public void setAccount_type(String account_type) {
        this.account_type = account_type;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDirty(String dirty) {
        this.dirty = dirty;
    }

    public void setGroup_visible(String group_visible) {
        this.group_visible = group_visible;
    }

    public void setShould_sync(String should_sync) {
        this.should_sync = should_sync;
    }

    public void setMembers(List<BaseContact> members) {
        this.members = members;
    }

    
    /**
     * Won't check whether the members has a group membership with the 
     * current group instance. Also has no effect on the members' group 
     * membership. Works on thread unsafe mode.
     * @param members Members to set. Should not pass null value.
     * @return True if set successfully, otherwise false(typically caused by 
     * passing null as 'members' parameter, or thread safe mode dismatch).
     */
    public boolean setMembers(ArrayList<BaseContact> members) {
        if (members == null){
            return false;
        } else {
            this.members = members;
            return true;
        }
    }
    
    /**
     * Won't check whether the members has a group membership with the 
     * current group instance. Also has no effect on the members' group 
     * membership. Works on thread safe mode.
     * @param members Members to set. Should not pass null value.
     * @return True if set successfully, otherwise false(typically caused by 
     * passing null as 'members' parameter, or thread safe mode dismatch).
     */
    public boolean setMembers(Vector<BaseContact> members) {
        if (members == null){
            return false;
        } else {
            this.members = members;
            return true;
        }
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    /**
     * Deep copy. This means all group member objects will also be 
     * copied, not just the references.
     * @return A deep copy.
     * @throws CloneNotSupportedException
     */
    //@Override
    public Group clone() throws CloneNotSupportedException{
        Group copy = (Group)(super.clone());
        copy.members = new ArrayList<BaseContact>();
        // Also deep copy all the group members
        for (BaseContact member : this.members){
            copy.members.add(member.clone());
        }
        
        return copy;
    }
    
    /**
     * Shallow copy. This means all the copy's group members are the same 
     * objects in the original group. What is copied is references, but not 
     * objects.
     * @return A shallow copy.
     * @throws CloneNotSupportedException
     */
    public Group shallowClone() throws CloneNotSupportedException{
        Group copy = (Group)(super.clone());
        copy.members = new ArrayList<BaseContact>();
        // Just pass all the group members' references
        for (BaseContact member : this.members){
            copy.members.add(member);
        }
        
        return copy;
    }
    
    /**
     * Get group size(members count in total).
     * @return The group size.
     */
    public int getSize(){
        return members.size();
    }
    
    /**
     * Won't check whether the member to add has a group membership with current 
     * group instance. Also has no effect on the member's group membership.
     * @param member Contact to add as new member.
     * @return True if succeed, otherwise false.
     */
    public boolean addMember(BaseContact member){
        return members.add(member);
    }
    
    /**
     * Won't check whether the members to add has a group membership with 
     * current group instance. Also has no effect on the members' group 
     * membership.
     * @param members Contacts to add as new members.
     * @return True if succeed, otherwise false.
     */
    public boolean addAll(List<BaseContact> members){
        return members.addAll(members);
    }
    
    /**
     * Won't check whether the member to remove has a group membership with 
     * current group instance. Also has no effect on the member's group 
     * membership.
     * @param member Contact member to remove from current group.
     * @return True if succeed, otherwise false.
     */
    public boolean removeMember(BaseContact member){
        return members.remove(member);
    }
    
    /**
     * Won't check whether the member to remove has a group membership with 
     * current group instance. Also has no effect on the member's group 
     * membership.
     * @param location The location of contact member to remove in the group.
     * @return True if succeed, otherwise false.
     */
    public BaseContact removeMember(int location){
        return members.remove(location);
    }
    
    /**
     * Won't check whether the members to remove has a group membership with 
     * current group instance. Also has no effect on the members' group 
     * membership.
     * @param members Contact members to remove from current group.
     * @return True if succeed, otherwise false.
     */
    public boolean removeAll(List<BaseContact> members){
        return members.removeAll(members);
    }
    
    /**
     * Won't check whether the members to remove has a group membership with 
     * current group instance. Also has no effect on the members' group 
     * membership.
     */
    public void clear(){
        members.clear();
    }
    
    //@Override
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        super.writeRaw(buffer);
        // title
        RawTransUtil.putString(buffer, this.title);
        // notes
        RawTransUtil.putString(buffer, this.notes);
        // system id
        RawTransUtil.putString(buffer, this.systemId);
        // deleted
        RawTransUtil.putString(buffer, this.deleted);
        // account_name
        RawTransUtil.putString(buffer, this.account_name);
        // account_type
        RawTransUtil.putString(buffer, this.account_type);
        // version
        RawTransUtil.putString(buffer, this.version);
        // dirty
        RawTransUtil.putString(buffer, this.dirty);
        // group_visible
        RawTransUtil.putString(buffer, this.group_visible);
        // should_sync
        RawTransUtil.putString(buffer, this.should_sync);
    }
    
    //@Override
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        super.readRaw(buffer);
        // title
        this.title = RawTransUtil.getString(buffer);
        // notes
        this.notes = RawTransUtil.getString(buffer);
        // system id
        this.systemId = RawTransUtil.getString(buffer);
        // deleted
        this.deleted = RawTransUtil.getString(buffer);
        // account_name
        this.account_name = RawTransUtil.getString(buffer);
        // account_type
        this.account_type = RawTransUtil.getString(buffer);
        // version
        this.version = RawTransUtil.getString(buffer);
        // dirty
        this.dirty = RawTransUtil.getString(buffer);
        // group_visible
        this.group_visible = RawTransUtil.getString(buffer);
        // should_sync
        this.should_sync = RawTransUtil.getString(buffer);
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
