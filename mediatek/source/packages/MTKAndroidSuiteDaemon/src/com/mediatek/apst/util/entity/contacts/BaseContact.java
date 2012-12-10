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

import com.mediatek.apst.util.entity.DataStoreLocations;
import com.mediatek.apst.util.entity.DatabaseRecordEntity;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: BaseContact
 * <p>Package: com.mediatek.apst.util.entity.contacts
 * <p>Created on: 2010-6-24
 * <p>
 * <p>Description: 
 * <p>Base contact entity. Only basic informations are encapsulated.
 *
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class BaseContact extends DatabaseRecordEntity implements 
DataStoreLocations, Cloneable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    /**
     * Indicates where the contact is stored(phone storage, SIM card, etc).
     */
    private int storeLocation;
	/**
     * Displaying name of the contact.
     */
    private String displayName;
    /**
     * The primary phone number of the contact.
     */
    private String primaryNumber;
    /**
     * The group membership list.
     */
    private List<GroupMembership> groupMemberships;
    /**
     * The name of the sim card.
     */
    private String simName;
    /**
     * The modify time of the contact. Added by Shaoying Han
     */
    private long modifyTime;
    
	//==============================================================
    // Constructors                                                 
    //==============================================================
    /**
     * Constructor.
     * @param id The database id of the contact.
     */
    public BaseContact(long id) {
        super(id);
        // Store on phone storage by default
        this.storeLocation = PHONE;
        this.displayName = null;
        this.primaryNumber = null;
        this.groupMemberships = new ArrayList<GroupMembership>();
        this.simName = null;
    }
    
    public BaseContact(){
        this(ID_NULL);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================

    public int getStoreLocation() {
        return storeLocation;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getPrimaryNumber(){
        return primaryNumber;
    }

    public List<GroupMembership> getGroupMemberships() {
        return groupMemberships;
    }
    
    public String getSimName() {
		return simName;
	}
    
    public long getModifyTime() {
		return modifyTime;
	}
    //==============================================================
    // Setters                                                      
    //==============================================================

    public void setStoreLocation(int storeLocation) {
        this.storeLocation = storeLocation;
    }
    
    public void setDisplayName(String displayName){
        this.displayName = displayName;
    }
    
    public void setPrimaryNumber(String primaryNumber){
        this.primaryNumber = primaryNumber;
    }
    
    public boolean setGroupMemberships(ArrayList<GroupMembership> 
    groupMemberships) {
        if (groupMemberships == null){
            return false;
        } else {
            this.groupMemberships = groupMemberships;
            return true;
        }
    }

    public void setSimName(String simName) {
		this.simName = simName;
	}
    
    public void setModifyTime(long modifyTime) {
		this.modifyTime = modifyTime;
	}
    
    public boolean setGroupMemberships(Vector<GroupMembership> 
    groupMemberships) {
        if (groupMemberships == null){
            return false;
        } else {
            this.groupMemberships = groupMemberships;
            return true;
        }
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    /**
     * Deep copy. This means all group membership objects 
     * will also be copied, not just the references.
     * @return A deep copy.
     * @throws CloneNotSupportedException
     */
    //@Override
    public BaseContact clone() throws CloneNotSupportedException{
        BaseContact copy = (BaseContact)(super.clone());
        copy.groupMemberships = new ArrayList<GroupMembership>();
        for (GroupMembership groupMembership : this.groupMemberships){
            copy.groupMemberships.add(groupMembership.clone());
        }
        
        return copy;
    }
    
    /**
     * Shallow copy. This means all the copy's group memberships are the same 
     * objects in the original BaseContact object. What is copied is references, 
     * but not objects.
     * @return A shallow copy.
     * @throws CloneNotSupportedException
     */
    public BaseContact shallowClone() throws CloneNotSupportedException{
        BaseContact copy = (BaseContact)(super.clone());
        copy.groupMemberships = new ArrayList<GroupMembership>();
        for (GroupMembership groupMembership : this.groupMemberships){
            copy.groupMemberships.add(groupMembership);
        }
        
        return copy;
    }
    
    //@Override
    public String toString(){
        return displayName;
    }
    /*
    //@Override
    public int getRawSize(){
        int size = super.getRawSize();
        // storeLocation
        size += RawTransUtil.INT;
        // displayName
        size += RawTransUtil.sizeOfString(this.displayName);
        // primaryNumber
        size += RawTransUtil.sizeOfString(this.primaryNumber);
        // groupMemberships length : int
        size += 4;
        // groupMemberships
        if (null != groupMemberships){
            for (GroupMembership groupMembership : groupMemberships){
                size += groupMembership.getRawSize();
            }
        }
        return size;
    }
    */
    //@Override
    /**
     * @deprecated
     */
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        super.writeRaw(buffer);
        // storeLocation
        buffer.putInt(this.storeLocation);
        // displayName
        RawTransUtil.putString(buffer, this.displayName);
        // primaryNumber
        RawTransUtil.putString(buffer, this.primaryNumber);
        // groupMemberships
        if (null != groupMemberships){
            buffer.putInt(groupMemberships.size());
            for (GroupMembership groupMembership : groupMemberships){
                groupMembership.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
    }
    
    //@Override
    /**
     * @deprecated
     */
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        super.readRaw(buffer);
        // storeLocation
        this.storeLocation = buffer.getInt();
        // displayName
        this.displayName = RawTransUtil.getString(buffer);
        // primaryNumber
        this.primaryNumber = RawTransUtil.getString(buffer);
        // groupMemberships
        int size = buffer.getInt();
        if (size >= 0){
            this.groupMemberships = new ArrayList<GroupMembership>(size);
            for (int i = 0; i < size; i++){
                GroupMembership groupMembership = new GroupMembership();
                groupMembership.readRaw(buffer);
                groupMemberships.add(groupMembership);
            }
        } else {
            this.groupMemberships = null;
        }
    }

    @Override
	public void writeRawWithVersion(ByteBuffer buffer, int versionCode)
			throws NullPointerException, BufferOverflowException {
		super.writeRawWithVersion(buffer, versionCode);
		
		// storeLocation
        buffer.putInt(this.storeLocation);
        // displayName
        RawTransUtil.putString(buffer, this.displayName);
        // primaryNumber
        RawTransUtil.putString(buffer, this.primaryNumber);
        // groupMemberships
        if (null != groupMemberships){
            buffer.putInt(groupMemberships.size());
            for (GroupMembership groupMembership : groupMemberships){
                groupMembership.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
     
        if (versionCode >= 0x00000002) {
        	// sim name
        	RawTransUtil.putString(buffer, this.simName);
        	// Modify time
        	buffer.putLong(this.modifyTime);
        }
	}
    
	@Override
	public void readRawWithVersion(ByteBuffer buffer, int versionCode)
			throws NullPointerException, BufferUnderflowException {
		super.readRawWithVersion(buffer, versionCode);
		
		// storeLocation
        this.storeLocation = buffer.getInt();
        // displayName
        this.displayName = RawTransUtil.getString(buffer);
        // primaryNumber
        this.primaryNumber = RawTransUtil.getString(buffer);
        // groupMemberships
        int size = buffer.getInt();
        if (size >= 0){
            this.groupMemberships = new ArrayList<GroupMembership>(size);
            for (int i = 0; i < size; i++){
                GroupMembership groupMembership = new GroupMembership();
                groupMembership.readRaw(buffer);
                groupMemberships.add(groupMembership);
            }
        } else {
            this.groupMemberships = null;
        }
        
        if (versionCode >= 0x00000002) {
        	// sim name
        	this.simName = RawTransUtil.getString(buffer);
        	// Modify time
        	this.modifyTime = buffer.getLong();
        }
	}

	
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
