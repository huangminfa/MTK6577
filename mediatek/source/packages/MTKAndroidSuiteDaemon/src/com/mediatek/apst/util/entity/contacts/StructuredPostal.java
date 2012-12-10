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
 * Class Name: StructuredPostal
 * <p>Package: com.mediatek.apst.util.entity.contacts
 * <p>Created on: 2010-6-17
 * <p>
 * <p>Description: 
 * <p>Structured postal data of a contact. It divides a contact's postal in 
 * several fields and stores them individually, like street, city, post code, 
 * etc.
 *
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class StructuredPostal extends ContactData implements BaseTypes, 
Cloneable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    public static final int MIME_TYPE = 3;
    
    public static final String MIME_TYPE_STRING = 
        "vnd.android.cursor.item/postal-address_v2";

    // Type define constants
    public static final int TYPE_HOME   = 1;
    public static final int TYPE_WORK   = 2;
    public static final int TYPE_OTHER  = 3;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    private String formattedAddress;
    
    private int type;
    
    private String label;
    
    private String street;
    
    private String pobox;

    private String neighborhood;

    private String city;

    private String region;

    private String postcode;

    private String country;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================

    public StructuredPostal(long id){
        super(id, MIME_TYPE);
        type = TYPE_NONE;
    }
    
    public StructuredPostal(){
        this(ID_NULL);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    
    public String getFormattedAddress() {
        return formattedAddress;
    }

    public int getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getStreet() {
        return street;
    }

    public String getPobox() {
        return pobox;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getCountry() {
        return country;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    
    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setPobox(String pobox) {
        this.pobox = pobox;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public void setCountry(String country) {
        this.country = country;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    public String getMimeTypeString(){
        return MIME_TYPE_STRING;
    }
    
    //@Override
    public StructuredPostal clone() throws CloneNotSupportedException{
        StructuredPostal copy = (StructuredPostal)(super.clone());
        
        return copy;
    }
    /*
    //@Override
    public int getRawSize(){
        int size = super.getRawSize();
        // formattedAddress
        size += RawTransUtil.sizeOfString(this.formattedAddress);
        // type
        size += RawTransUtil.INT;
        // label
        size += RawTransUtil.sizeOfString(this.label);
        // street
        size += RawTransUtil.sizeOfString(this.street);
        // pobox
        size += RawTransUtil.sizeOfString(this.pobox);
        // neighborhood
        size += RawTransUtil.sizeOfString(this.neighborhood);
        // city
        size += RawTransUtil.sizeOfString(this.city);
        // region
        size += RawTransUtil.sizeOfString(this.region);
        // postcode
        size += RawTransUtil.sizeOfString(this.postcode);
        // country
        size += RawTransUtil.sizeOfString(this.country);
        return size;
    }
    */
    //@Override
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        super.writeRaw(buffer);
        // formattedAddress
        RawTransUtil.putString(buffer, this.formattedAddress);
        // type
        buffer.putInt(this.type);
        // label
        RawTransUtil.putString(buffer, this.label);
        // street
        RawTransUtil.putString(buffer, this.street);
        // pobox
        RawTransUtil.putString(buffer, this.pobox);
        // neighborhood
        RawTransUtil.putString(buffer, this.neighborhood);
        // city
        RawTransUtil.putString(buffer, this.city);
        // region
        RawTransUtil.putString(buffer, this.region);
        // postcode
        RawTransUtil.putString(buffer, this.postcode);
        // country
        RawTransUtil.putString(buffer, this.country);
    }
    
    //@Override
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        super.readRaw(buffer);
        // formattedAddress
        this.formattedAddress = RawTransUtil.getString(buffer);
        // type
        this.type = buffer.getInt();
        // label
        this.label = RawTransUtil.getString(buffer);
        // street
        this.street = RawTransUtil.getString(buffer);
        // pobox
        this.pobox = RawTransUtil.getString(buffer);
        // neighborhood
        this.neighborhood = RawTransUtil.getString(buffer);
        // city
        this.city = RawTransUtil.getString(buffer);
        // region
        this.region = RawTransUtil.getString(buffer);
        // postcode
        this.postcode = RawTransUtil.getString(buffer);
        // country
        this.country = RawTransUtil.getString(buffer);
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
