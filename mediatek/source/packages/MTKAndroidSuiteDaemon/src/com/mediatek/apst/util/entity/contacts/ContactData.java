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

import com.mediatek.apst.util.entity.DatabaseRecordEntity;

/**
 * Class Name: ContactData
 * <p>Package: com.mediatek.apst.util.entity.contacts
 * <p>Created on: 2010-6-17
 * <p>
 * <p>Description: 
 * <p>Represents all contact data field. Classes for all these 
 * fields, phone, structured name, etc. should be derived from it.
 * 
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class ContactData extends DatabaseRecordEntity implements Cloneable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    /*private long packageId;*/
    
    /**
     * The MIME type of data.
     */
    private int mimeType;
    
    private long rawContactId;
    
    private boolean primary;
    
    private boolean superPrimary;
    
    /*private int dataVersion;*/
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    
    public ContactData(long id, int mimeType, long rawContactId, 
            boolean primary, boolean superPrimary, int dataVersion) {
        super(id);
        /*this.packageId = ID_NULL;*/
        this.mimeType = mimeType;
        this.rawContactId = rawContactId;
        this.primary = primary;
        this.superPrimary = superPrimary;
        /*this.dataVersion = dataVersion;*/
    }
    
    public ContactData(long id, int mimeType, long rawContactId){
        this(id, mimeType, rawContactId, false, false, 0);
    }

    public ContactData(long id, int mimeType){
        this(id, mimeType, ID_NULL, false, false, 0);
    }
    
    public ContactData(int mimeType){
        this(ID_NULL, mimeType, ID_NULL, false, false, 0);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    /*
    public long getPackageId() {
        return packageId;
    }
    */
    
    public long getRawContactId() {
        return rawContactId;
    }
    
    
    public boolean isPrimary() {
        return primary;
    }
    
    
    public boolean isSuperPrimary() {
        return superPrimary;
    }
    
    /*
    public int getDataVersion() {
        return dataVersion;
    }
    */
    /**
     * Get the MIME type of the data. This specifies what kind of data it is, 
     * like phone, structured name, email, etc.
     * @return The MIME type of the data.
     */
    public int getMimeType() {
        return mimeType;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    /*
    public void setPackageId(long packageId) {
        this.packageId = packageId;
    }
    */
    
    public void setRawContactId(long rawContactId) {
        this.rawContactId = rawContactId;
    }
    
    
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
    
    
    public void setSuperPrimary(boolean superPrimary) {
        this.superPrimary = superPrimary;
    }
    
    /*
    public void setDataVersion(int dataVersion) {
        this.dataVersion = dataVersion;
    }
    */
    
    public void setMimeType(int mimeType){
        this.mimeType = mimeType;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    /**
     * Override it. Return the String representing the MIME type.
     * @return String representing the MIME type. 
     */
    public String getMimeTypeString(){
        return "";
    }
    
    //@Override
    public ContactData clone() throws CloneNotSupportedException {
        ContactData copy = (ContactData)(super.clone());
        
        return copy;
    }
    /*
    //@Override
    public int getRawSize(){
        int size = super.getRawSize();
        // rawContactId
        size += RawTransUtil.LONG;
        // mimeType
        size += RawTransUtil.INT;
        return size;
    }
    */
    //@Override
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        super.writeRaw(buffer);
        // rawContactId
        buffer.putLong(this.rawContactId);
        // mimeType
        buffer.putInt(this.mimeType);
    }
    
    //@Override
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        super.readRaw(buffer);
        // rawContactId
        this.rawContactId = buffer.getLong();
        // mimeType
        this.mimeType = buffer.getInt();
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
