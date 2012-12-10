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

import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: StructuredName
 * <p>Package: com.mediatek.apst.util.entity.contacts
 * <p>Created on: 2010-6-17
 * <p>
 * <p>Description: 
 * <p>Structured name data of a contact. It divides a contact's name in several 
 * fields and stores them individually, like given name, family name, prefix, 
 * etc.
 *
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class StructuredName extends ContactData implements Cloneable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    public static final int MIME_TYPE = 9;
    
    public static final String MIME_TYPE_STRING = 
        "vnd.android.cursor.item/name";
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    private String displayName;
    
    private String givenName;
    
    private String middleName;
    
    private String familyName;
    
    private String prefix;
    
    private String suffix;
    
    private String phoneticGivenName;
    
    private String phoneticMiddleName;
    
    private String phoneticFamilyName;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    
    public StructuredName(long id){
        super(id, MIME_TYPE);
    }
    
    public StructuredName(){
        this(ID_NULL);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================

    public String getDisplayName() {
        return displayName;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getPhoneticGivenName() {
        return phoneticGivenName;
    }

    public String getPhoneticMiddleName() {
        return phoneticMiddleName;
    }

    public String getPhoneticFamilyName() {
        return phoneticFamilyName;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setPhoneticGivenName(String phoneticGivenName) {
        this.phoneticGivenName = phoneticGivenName;
    }

    public void setPhoneticMiddleName(String phoneticMiddleName) {
        this.phoneticMiddleName = phoneticMiddleName;
    }

    public void setPhoneticFamilyName(String phoneticFamilyName) {
        this.phoneticFamilyName = phoneticFamilyName;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    public String getMimeTypeString(){
        return MIME_TYPE_STRING;
    }
    
    //@Override
    public StructuredName clone() throws CloneNotSupportedException {
        StructuredName copy = (StructuredName)(super.clone());
        
        return copy;
    }
    /*
    //@Override
    public int getRawSize(){
        int size = super.getRawSize();
        // displayName
        size += RawTransUtil.sizeOfString(this.displayName);
        // givenName
        size += RawTransUtil.sizeOfString(this.givenName);
        // middleName
        size += RawTransUtil.sizeOfString(this.middleName);
        // familyName
        size += RawTransUtil.sizeOfString(this.familyName);
        // prefix
        size += RawTransUtil.sizeOfString(this.prefix);
        // suffix
        size += RawTransUtil.sizeOfString(this.suffix);
        // phoneticGivenName
        size += RawTransUtil.sizeOfString(this.phoneticGivenName);
        // phoneticMiddleName
        size += RawTransUtil.sizeOfString(this.phoneticMiddleName);
        // phoneticFamilyName
        size += RawTransUtil.sizeOfString(this.phoneticFamilyName);
        return size;
    }
    */
    //@Override
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        super.writeRaw(buffer);
        // displayName
        RawTransUtil.putString(buffer, this.displayName);
        // givenName
        RawTransUtil.putString(buffer, this.givenName);
        // middleName
        RawTransUtil.putString(buffer, this.middleName);
        // familyName
        RawTransUtil.putString(buffer, this.familyName);
        // prefix
        RawTransUtil.putString(buffer, this.prefix);
        // suffix
        RawTransUtil.putString(buffer, this.suffix);
        // phoneticGivenName
        RawTransUtil.putString(buffer, this.phoneticGivenName);
        // phoneticMiddleName
        RawTransUtil.putString(buffer, this.phoneticMiddleName);
        // phoneticFamilyName
        RawTransUtil.putString(buffer, this.phoneticFamilyName);
    }
    
    //@Override
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        super.readRaw(buffer);
        // displayName
        this.displayName = RawTransUtil.getString(buffer);
        // givenName
        this.givenName = RawTransUtil.getString(buffer);
        // middleName
        this.middleName = RawTransUtil.getString(buffer);
        // familyName
        this.familyName = RawTransUtil.getString(buffer);
        // prefix
        this.prefix = RawTransUtil.getString(buffer);
        // suffix
        this.suffix = RawTransUtil.getString(buffer);
        // phoneticGivenName
        this.phoneticGivenName = RawTransUtil.getString(buffer);
        // phoneticMiddleName
        this.phoneticMiddleName = RawTransUtil.getString(buffer);
        // phoneticFamilyName
        this.phoneticFamilyName = RawTransUtil.getString(buffer);
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
