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

package com.mediatek.apst.util.entity.message;

import java.io.Serializable;
import java.nio.ByteBuffer;

import com.mediatek.apst.util.entity.DatabaseRecordEntity;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: TargetAddress
 * <p>Package: com.mediatek.apst.util.entity.message
 * <p>Created on: 2010-8-12
 * <p>
 * <p>Description: 
 * <p>Target address of a message. The 'target' is equivalent to 'recipient' if 
 * the message is one to send, or 'sender' if the message is one received. Its 
 * address is typically its phone number(may contain other data like +86).
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class TargetAddress implements Cloneable, Serializable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    /**
     * Store the contact id if the address is found belong to a contact
     */
    private long contactId;
    
    private String address;
    
    private String name;
    
    private long mmsId;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    public TargetAddress(long id, String address, String name){
        this.contactId = id;
        this.address = address;
        this.name = name;
    }
    
    public TargetAddress(String address){
        this(DatabaseRecordEntity.ID_NULL, address, null);
    }
    
    public TargetAddress(){
        this(DatabaseRecordEntity.ID_NULL, null, null);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    public long getContactId(){
        return contactId;
    }
    
    public long getMmsId(){
        return mmsId;
    }
    
    public String getAddress(){
        return address;
    }
    
    public String getName(){
        return name;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    public void setContactId(long contactId){
        this.contactId = contactId;
    }
    
    public void setAddress(String address){
        this.address = address;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public void setMmsId(long mmsId){
    	this.mmsId=mmsId;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    /**
     * Deep copy. 
     * @return A deep copy.
     * @throws CloneNotSupportedException
     */
    //@Override
    public TargetAddress clone() throws CloneNotSupportedException{
        TargetAddress copy = (TargetAddress)(super.clone());
        
        return copy;
    }
    
    //@Override
    public String toString(){
        if (null != name){
            return name;
        } else {
            return address;
        }
    }
    
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        // contactId
        buffer.putLong(this.contactId);
        // address
        RawTransUtil.putString(buffer, this.address);
        // name
        RawTransUtil.putString(buffer, this.name);
    }
    
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        // contactId
        this.contactId = buffer.getLong();
        // address
        this.address = RawTransUtil.getString(buffer);
        // name
        this.name = RawTransUtil.getString(buffer);
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
