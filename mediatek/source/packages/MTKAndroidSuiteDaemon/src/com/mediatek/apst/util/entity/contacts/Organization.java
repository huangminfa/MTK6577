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

import com.mediatek.apst.util.entity.BaseTypes;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: Organization
 * <p>Package: com.mediatek.apst.util.entity.contacts
 * <p>Created on: 2010-6-17
 * <p>
 * <p>Description: 
 * <p>Organization data of a contact.
 *
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class Organization extends ContactData implements BaseTypes, Cloneable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    public static final int MIME_TYPE = 8;
    
    public static final String MIME_TYPE_STRING = 
        "vnd.android.cursor.item/organization";
    
    // Type define constants
    public static final int TYPE_WORK   = 1;
    public static final int TYPE_OTHER  = 2;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    private String company;
    
    private int type;
    
    private String label;
    
    private String title;
    
    private String department;
    
    private String jobDescription;
    
    private String symbol;
    
    private String phoneticName;
    
    private String officeLocation;
    
    private String phoneticNameStyle;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================

    public Organization(long id){
        super(id, MIME_TYPE);
        type = TYPE_NONE;
    }
    
    public Organization(){
        this(ID_NULL);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    public String getCompany() {
        return company;
    }

    public int getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getTitle() {
        return title;
    }

    public String getDepartment() {
        return department;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getPhoneticName() {
        return phoneticName;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public String getPhoneticNameStyle() {
        return phoneticNameStyle;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    public void setCompany(String company) {
        this.company = company;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setPhoneticName(String phoneticName) {
        this.phoneticName = phoneticName;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public void setPhoneticNameStyle(String phoneticNameStyle) {
        this.phoneticNameStyle = phoneticNameStyle;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    public String getMimeTypeString(){
        return MIME_TYPE_STRING;
    }
    
    //@Override
    public Organization clone() throws CloneNotSupportedException{
        Organization copy = (Organization)(super.clone());
        
        return copy;
    }
    /*
    //@Override
    public int getRawSize(){
        int size = super.getRawSize();
        // company
        size += RawTransUtil.sizeOfString(this.company);
        // type
        size += RawTransUtil.INT;
        // label
        size += RawTransUtil.sizeOfString(this.label);
        // title
        size += RawTransUtil.sizeOfString(this.title);
        // department
        size += RawTransUtil.sizeOfString(this.department);
        // jobDescription
        size += RawTransUtil.sizeOfString(this.jobDescription);
        // symbol
        size += RawTransUtil.sizeOfString(this.symbol);
        // phoneticName
        size += RawTransUtil.sizeOfString(this.phoneticName);
        // officeLocation
        size += RawTransUtil.sizeOfString(this.officeLocation);
        // phoneticNameStyle
        //size += RawTransUtil.sizeOfString(this.phoneticNameStyle);
        return size;
    }
    
    //@Override*/
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        super.writeRaw(buffer);
        // company
        RawTransUtil.putString(buffer, this.company);
        // type
        buffer.putInt(this.type);
        // label
        RawTransUtil.putString(buffer, this.label);
        // title
        RawTransUtil.putString(buffer, this.title);
        // department
        RawTransUtil.putString(buffer, this.department);
        // jobDescription
        RawTransUtil.putString(buffer, this.jobDescription);
        // symbol
        RawTransUtil.putString(buffer, this.symbol);
        // phoneticName
        RawTransUtil.putString(buffer, this.phoneticName);
        // officeLocation
        RawTransUtil.putString(buffer, this.officeLocation);
        // phoneticNameStyle
        //RawTransUtil.putString(buffer, this.phoneticNameStyle);
    }
    
    //@Override
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        super.readRaw(buffer);
        // company
        this.company = RawTransUtil.getString(buffer);
        // type
        this.type = buffer.getInt();
        // label
        this.label = RawTransUtil.getString(buffer);
        // title
        this.title = RawTransUtil.getString(buffer);
        // department
        this.department = RawTransUtil.getString(buffer);
        // jobDescription
        this.jobDescription = RawTransUtil.getString(buffer);
        // symbol
        this.symbol = RawTransUtil.getString(buffer);
        // phoneticName
        this.phoneticName = RawTransUtil.getString(buffer);
        // officeLocation
        this.officeLocation = RawTransUtil.getString(buffer);
        // phoneticNameStyle
        //this.phoneticNameStyle = RawTransUtil.getString(buffer);
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
