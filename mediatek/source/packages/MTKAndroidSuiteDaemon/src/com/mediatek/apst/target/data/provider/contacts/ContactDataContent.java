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

package com.mediatek.apst.target.data.provider.contacts;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;

import com.mediatek.android.content.MeasuredContentValues;
import com.mediatek.apst.target.data.proxy.IRawBufferWritable;
import com.mediatek.apst.target.util.Debugger;
import com.mediatek.apst.util.entity.DatabaseRecordEntity;
import com.mediatek.apst.util.entity.RawTransUtil;
import com.mediatek.apst.util.entity.contacts.ContactData;
import com.mediatek.apst.util.entity.contacts.Email;
import com.mediatek.apst.util.entity.contacts.GroupMembership;
import com.mediatek.apst.util.entity.contacts.Im;
import com.mediatek.apst.util.entity.contacts.Nickname;
import com.mediatek.apst.util.entity.contacts.Note;
import com.mediatek.apst.util.entity.contacts.Organization;
import com.mediatek.apst.util.entity.contacts.Phone;
import com.mediatek.apst.util.entity.contacts.Photo;
import com.mediatek.apst.util.entity.contacts.RawContact;
import com.mediatek.apst.util.entity.contacts.StructuredName;
import com.mediatek.apst.util.entity.contacts.StructuredPostal;
import com.mediatek.apst.util.entity.contacts.Website;

public abstract class ContactDataContent {
    //==============================================================
    // Constants                                                    
    //==============================================================
    
	/**
     * Column name of indicate_phone_or_sim_contact.
     */
    public static final String COLUMN_BINDING_SIM_ID = 
        "sim_id";
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    public static ContactData cursorToContactData(Cursor c){
        if (null == c || c.getPosition() == -1 || 
                c.getPosition() == c.getCount()){
            return null;
        }
        
        int colId;
        ContactData data = null;
        long id = -1;
        long rawContactId = -1;
        boolean primary = false;
        boolean superPrimary = false;
        String strMimeType = null;
        
        try {
            // id
            colId = c.getColumnIndex(Data._ID);
            if (-1 != colId){
                id = c.getLong(colId);
            }
            // raw contact id
            colId = c.getColumnIndex(Data.RAW_CONTACT_ID);
            if (-1 != colId){
                rawContactId = c.getLong(colId);
            }
            // is primary
            colId = c.getColumnIndex(Data.IS_PRIMARY);
            if (-1 != colId){
                primary = (c.getInt(colId) == DatabaseRecordEntity.TRUE);
            }
            // is super primary
            colId = c.getColumnIndex(Data.IS_SUPER_PRIMARY);
            if (-1 != colId){
                superPrimary = (c.getInt(colId) == DatabaseRecordEntity.TRUE);
            }
            // MIME type string
            colId = c.getColumnIndex(Data.MIMETYPE);
            if (-1 != colId){
                strMimeType = c.getString(colId);
            }
            
            // Create a new contact data object according to its MIME type
            if (null == strMimeType){
                Debugger.logW(new Object[]{c}, "mimeType is absent in cursor.");
                return null;
            } else if (strMimeType.equals(StructuredName.MIME_TYPE)){
                // Name --------------------------------------------
                data = cursorToStructuredName(c);
            } else if (strMimeType.equals(Phone.MIME_TYPE)){
                // Phone -------------------------------------------
                data = cursorToPhone(c);
            } else if (strMimeType.equals(Photo.MIME_TYPE)){
                // Photo -------------------------------------------
                data = cursorToPhoto(c);
            } else if (strMimeType.equals(Im.MIME_TYPE)){
                // IM ----------------------------------------------
                data = cursorToIm(c);
            } else if (strMimeType.equals(Email.MIME_TYPE)){
                // Email -------------------------------------------
                data = cursorToEmail(c);
            } else if (strMimeType.equals(StructuredPostal.MIME_TYPE)){
                // Postal ------------------------------------------
                data = cursorToStructuredPostal(c);
            } else if (strMimeType.equals(Organization.MIME_TYPE)){
                // Organization ------------------------------------
                data = cursorToOrganization(c);
            } else if (strMimeType.equals(Nickname.MIME_TYPE)){
                // Nickname ----------------------------------------
                data = cursorToNickname(c);
            } else if (strMimeType.equals(Website.MIME_TYPE)){
                // Website -----------------------------------------
                data = cursorToWebsite(c);
            } else if (strMimeType.equals(Note.MIME_TYPE)){
                // Note --------------------------------------------
                data = cursorToNote(c);
            } else if (strMimeType.equals(GroupMembership.MIME_TYPE)){
                // Group membership --------------------------------
                data = cursorToGroupMembership(c);
            } else {
                Debugger.logW(new Object[]{c}, 
                        "Ignored unknown mimeType: " + strMimeType);
                return null;
            }
            
            // At last, set common fields
            data.setId(id);
            data.setRawContactId(rawContactId);
            data.setPrimary(primary);
            data.setSuperPrimary(superPrimary);
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c}, null, e);
            return null;
        }
        
        return data;
    }
    
    private static StructuredName cursorToStructuredName(Cursor c){
        StructuredName data = new StructuredName();
        
        try {
            int colId;
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredName.DISPLAY_NAME);
            if (-1 != colId){
                data.setDisplayName(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredName.GIVEN_NAME);
            if (-1 != colId){
                data.setGivenName(c.getString(colId));
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredName.FAMILY_NAME);
            if (-1 != colId){
                data.setFamilyName(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredName.PREFIX);
            if (-1 != colId){
                data.setPrefix(c.getString(colId));
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredName.MIDDLE_NAME);
            if (-1 != colId){
                data.setMiddleName(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredName.SUFFIX);
            if (-1 != colId){
                data.setSuffix(c.getString(colId));
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME);
            if (-1 != colId){
                data.setPhoneticGivenName(c.getString(colId));
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME);
            if (-1 != colId){
                data.setPhoneticMiddleName(c.getString(colId));
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME);
            if (-1 != colId){
                data.setPhoneticFamilyName(c.getString(colId));
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c}, null, e);
            return null;
        }
        return data;
    }
    
    private static Phone cursorToPhone(Cursor c){
        Phone data = new Phone();
        
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Phone.NUMBER);
            if (-1 != colId){
                data.setNumber(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Phone.TYPE);
            if (-1 != colId){
                data.setType(c.getInt(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Phone.LABEL);
            if (-1 != colId){
                data.setLabel(c.getString(colId));
            }
            // Added by Shaoying Han
            colId = c.getColumnIndex(COLUMN_BINDING_SIM_ID); 
            if (-1 != colId){
                data.setBindingSimId(c.getInt(colId));
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c}, null, e);
            return null;
        }
        return data;
    }
    
    private static Photo cursorToPhoto(Cursor c){
        Photo data = new Photo();
        
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Photo.PHOTO);
            if (-1 != colId){
                data.setPhotoBytes(c.getBlob(colId));
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c}, null, e);
            return null;
        }
        return data;
    }
    
    private static Im cursorToIm(Cursor c){
        Im data = new Im();
        
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Im.DATA);
            if (-1 != colId){
                data.setData(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Im.TYPE);
            if (-1 != colId){
                data.setType(c.getInt(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Im.LABEL);
            if (-1 != colId){
                data.setLabel(c.getString(colId));
            }
            // Note that DATA4 is not used in Im data row
            colId = c.getColumnIndex(CommonDataKinds.Im.PROTOCOL);
            if (-1 != colId){
                data.setProtocol(c.getInt(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Im.CUSTOM_PROTOCOL);
            if (-1 != colId){
                data.setCustomProtocol(c.getString(colId));
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c}, null, e);
            return null;
        }
        return data;
    }
    
    private static Email cursorToEmail(Cursor c){
        Email data = new Email();
        
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Email.DATA);
            if (-1 != colId){
                data.setData(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Email.TYPE);
            if (-1 != colId){
                data.setType(c.getInt(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Email.LABEL);
            if (-1 != colId){
                data.setLabel(c.getString(colId));
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c}, null, e);
            return null;
        }
        return data;
    }
    
    private static StructuredPostal cursorToStructuredPostal(Cursor c){
        StructuredPostal data = new StructuredPostal();
        
        try {
            int colId;
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
            if (-1 != colId){
                data.setFormattedAddress(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.TYPE);
            if (-1 != colId){
                data.setType(c.getInt(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.LABEL);
            if (-1 != colId){
                data.setLabel(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.STREET);
            if (-1 != colId){
                data.setStreet(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.POBOX);
            if (-1 != colId){
                data.setPobox(c.getString(colId));
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredPostal.NEIGHBORHOOD);
            if (-1 != colId){
                data.setNeighborhood(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.CITY);
            if (-1 != colId){
                data.setCity(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.REGION);
            if (-1 != colId){
                data.setRegion(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.POSTCODE);
            if (-1 != colId){
                data.setPostcode(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.COUNTRY);
            if (-1 != colId){
                data.setCountry(c.getString(colId));
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c}, null, e);
            return null;
        }
        return data;
    }
    
    private static Organization cursorToOrganization(Cursor c){
        Organization data = new Organization();
        
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Organization.COMPANY);
            if (-1 != colId){
                data.setCompany(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Organization.TYPE);
            if (-1 != colId){
                data.setType(c.getInt(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Organization.LABEL);
            if (-1 != colId){
                data.setLabel(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Organization.TITLE);
            if (-1 != colId){
                data.setTitle(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Organization.DEPARTMENT);
            if (-1 != colId){
                data.setDepartment(c.getString(colId));
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.Organization.JOB_DESCRIPTION);
            if (-1 != colId){
                data.setJobDescription(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Organization.SYMBOL);
            if (-1 != colId){
                data.setSymbol(c.getString(colId));
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.Organization.PHONETIC_NAME);
            if (-1 != colId){
                data.setPhoneticName(c.getString(colId));
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.Organization.OFFICE_LOCATION);
            if (-1 != colId){
                data.setOfficeLocation(c.getString(colId));
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c}, null, e);
            return null;
        }
        return data;
    }
    
    private static Nickname cursorToNickname(Cursor c){
        Nickname data = new Nickname();
        
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Nickname.NAME);
            if (-1 != colId){
                data.setName(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Nickname.TYPE);
            if (-1 != colId){
                data.setType(c.getInt(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Nickname.LABEL);
            if (-1 != colId){
                data.setLabel(c.getString(colId));
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c}, null, e);
            return null;
        }
        return data;
    }
    
    private static Website cursorToWebsite(Cursor c){
        Website data = new Website();
        
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Website.URL);
            if (-1 != colId){
                data.setUrl(c.getString(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Website.TYPE);
            if (-1 != colId){
                data.setType(c.getInt(colId));
            }
            colId = c.getColumnIndex(CommonDataKinds.Website.LABEL);
            if (-1 != colId){
                data.setLabel(c.getString(colId));
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c}, null, e);
            return null;
        }
        return data;
    }
    
    private static Note cursorToNote(Cursor c){
        Note data = new Note();
        
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Note.NOTE);
            if (-1 != colId){
                data.setNote(c.getString(colId));
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c}, null, e);
            return null;
        }
        return data;
    }
    
    private static GroupMembership cursorToGroupMembership(Cursor c){
        GroupMembership data = new GroupMembership();
        
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.GroupMembership.GROUP_ROW_ID);
            if (-1 != colId){
                data.setGroupId(c.getLong(colId));
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c}, null, e);
            return null;
        }
        return data;
    }
    
    public static int cursorToRaw(Cursor c, ByteBuffer buffer){
        if (null == c){
            Debugger.logW(new Object[]{c, buffer}, "Cursor is null.");
            return IRawBufferWritable.RESULT_FAIL;
        } else if (c.getPosition() == -1 || c.getPosition() == c.getCount()){
            Debugger.logW(new Object[]{c, buffer}, 
                    "Cursor has moved to the end.");
            return IRawBufferWritable.RESULT_FAIL;
        } else if (null == buffer) {
            Debugger.logW(new Object[]{c, buffer}, "Buffer is null.");
            return IRawBufferWritable.RESULT_FAIL;
        }
        // Mark the current start position of byte buffer in order to reset 
        // later when there is not enough space left in buffer
        buffer.mark();
        buffer.position();// for test
        int result = IRawBufferWritable.RESULT_FAIL;
        
        try {
            int colId;
            String strMimeType = null;
            // Set common fields of ContactData --------------------------------
            // id
            colId = c.getColumnIndex(Data._ID);
            if (-1 != colId){
                buffer.putLong(c.getLong(colId));
            } else {
                buffer.putLong(DatabaseRecordEntity.ID_NULL);
            }
            // raw contact id
            colId = c.getColumnIndex(Data.RAW_CONTACT_ID);
            if (-1 != colId){
                buffer.putLong(c.getLong(colId));
            } else {
                buffer.putLong(DatabaseRecordEntity.ID_NULL);
            }
            // is primary
            // is super primary
            // MIME type
            colId = c.getColumnIndex(Data.MIMETYPE);
            if (-1 != colId){
                strMimeType = c.getString(colId);
            }
            
            // Read specific fields according to its MIME type -----------------
            if (null == strMimeType){
                Debugger.logW(new Object[]{c, buffer}, "mimeType is absent in cursor.");
                result = IRawBufferWritable.RESULT_FAIL;
            } else if (strMimeType.equals(StructuredName.MIME_TYPE_STRING)){
                // Name --------------------------------------------
                buffer.putInt(StructuredName.MIME_TYPE);
                result = cursorToStructuredNameRaw(c, buffer);
            } else if (strMimeType.equals(Phone.MIME_TYPE_STRING)){
                // Phone -------------------------------------------
                buffer.putInt(Phone.MIME_TYPE);
                result = cursorToPhoneRaw(c, buffer);
            } else if (strMimeType.equals(Photo.MIME_TYPE_STRING)){
                // Photo -------------------------------------------
                buffer.putInt(Photo.MIME_TYPE);
                result = cursorToPhotoRaw(c, buffer);
            } else if (strMimeType.equals(Im.MIME_TYPE_STRING)){
                // IM ----------------------------------------------
                buffer.putInt(Im.MIME_TYPE);
                result = cursorToImRaw(c, buffer);
            } else if (strMimeType.equals(Email.MIME_TYPE_STRING)){
                // Email -------------------------------------------
                buffer.putInt(Email.MIME_TYPE);
                result = cursorToEmailRaw(c, buffer);
            } else if (strMimeType.equals(StructuredPostal.MIME_TYPE_STRING)){
                // Postal ------------------------------------------
                buffer.putInt(StructuredPostal.MIME_TYPE);
                result = cursorToStructuredPostalRaw(c, buffer);
            } else if (strMimeType.equals(Organization.MIME_TYPE_STRING)){
                // Organization ------------------------------------
                buffer.putInt(Organization.MIME_TYPE);
                result = cursorToOrganizationRaw(c, buffer);
            } else if (strMimeType.equals(Nickname.MIME_TYPE_STRING)){
                // Nickname ----------------------------------------
                buffer.putInt(Nickname.MIME_TYPE);
                result = cursorToNicknameRaw(c, buffer);
            } else if (strMimeType.equals(Website.MIME_TYPE_STRING)){
                // Website -----------------------------------------
                buffer.putInt(Website.MIME_TYPE);
                result = cursorToWebsiteRaw(c, buffer);
            } else if (strMimeType.equals(Note.MIME_TYPE_STRING)){
                // Note --------------------------------------------
                buffer.putInt(Note.MIME_TYPE);
                result = cursorToNoteRaw(c, buffer);
            } else if (strMimeType.equals(GroupMembership.MIME_TYPE_STRING)){
                // Group membership --------------------------------
                buffer.putInt(GroupMembership.MIME_TYPE);
                result = cursorToGroupMembershipRaw(c, buffer);
            } else {
                Debugger.logW(new Object[]{c, buffer}, 
                        "Ignored unknown mimeType: " + strMimeType);
                result = IRawBufferWritable.RESULT_FAIL;
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c, buffer}, null, e);
            result = IRawBufferWritable.RESULT_FAIL;
        } catch (BufferOverflowException e) {
            /*DebugHelper.logW("[ContactDataContent] " +
                    "cursorToRaw(" + c + "): Not enough space left in " +
                    "buffer. ", e);*/
            result = IRawBufferWritable.RESULT_NOT_ENOUGH_BUFFER;
        }
        
        if (result == IRawBufferWritable.RESULT_NOT_ENOUGH_BUFFER){
            buffer.reset();
        } else if (result == IRawBufferWritable.RESULT_FAIL){
            buffer.reset();
        }
        
        return result;
    }
    
    private static int cursorToStructuredNameRaw(Cursor c, ByteBuffer buffer) 
    throws BufferOverflowException {
        try {
            int colId;
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredName.DISPLAY_NAME);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredName.GIVEN_NAME);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredName.MIDDLE_NAME);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredName.FAMILY_NAME);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredName.PREFIX);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredName.SUFFIX);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c, buffer}, null, e);
            return IRawBufferWritable.RESULT_FAIL;
        }
        return IRawBufferWritable.RESULT_SUCCESS;
    }
    
    private static int cursorToPhoneRaw(Cursor c, ByteBuffer buffer) 
    throws BufferOverflowException {
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Phone.NUMBER);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.Phone.TYPE);
            if (-1 != colId){
                buffer.putInt(c.getInt(colId));
            } else {
                buffer.putInt(Phone.TYPE_HOME);
            }
            colId = c.getColumnIndex(CommonDataKinds.Phone.LABEL);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            // Added by Shaoying Han
            colId = c.getColumnIndex(COLUMN_BINDING_SIM_ID);
            if (-1 != colId){
            	buffer.putInt(c.getInt(colId));
            } else {
            	buffer.putInt(Phone.DEFAULT_BINDING_SIM_ID);
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c, buffer}, null, e);
            return IRawBufferWritable.RESULT_FAIL;
        }
        return IRawBufferWritable.RESULT_SUCCESS;
    }
    
    private static int cursorToPhotoRaw(Cursor c, ByteBuffer buffer) 
    throws BufferOverflowException {
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Photo.PHOTO);
            if (-1 != colId){
                RawTransUtil.putBytes(buffer, c.getBlob(colId));
            } else {
                RawTransUtil.putBytes(buffer, null);
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c, buffer}, null, e);
            return IRawBufferWritable.RESULT_FAIL;
        }
        return IRawBufferWritable.RESULT_SUCCESS;
    }
    
    private static int cursorToImRaw(Cursor c, ByteBuffer buffer) 
    throws BufferOverflowException {
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Im.DATA);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.Im.TYPE);
            if (-1 != colId){
                buffer.putInt(c.getInt(colId));
            } else {
                buffer.putInt(Im.TYPE_HOME);
            }
            colId = c.getColumnIndex(CommonDataKinds.Im.LABEL);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            // Note that DATA4 is not used in Im data row
            colId = c.getColumnIndex(CommonDataKinds.Im.PROTOCOL);
            if (-1 != colId){
                buffer.putInt(c.getInt(colId));
            } else {
                buffer.putInt(Im.PROTOCOL_AIM);
            }
            colId = c.getColumnIndex(CommonDataKinds.Im.CUSTOM_PROTOCOL);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c, buffer}, null, e);
            return IRawBufferWritable.RESULT_FAIL;
        }
        return IRawBufferWritable.RESULT_SUCCESS;
    }
    
    private static int cursorToEmailRaw(Cursor c, ByteBuffer buffer) 
    throws BufferOverflowException {
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Email.DATA);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.Email.TYPE);
            if (-1 != colId){
                buffer.putInt(c.getInt(colId));
            } else {
                buffer.putInt(Email.TYPE_HOME);
            }
            colId = c.getColumnIndex(CommonDataKinds.Email.LABEL);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c, buffer}, null, e);
            return IRawBufferWritable.RESULT_FAIL;
        }
        return IRawBufferWritable.RESULT_SUCCESS;
    }
    
    private static int cursorToStructuredPostalRaw(Cursor c, ByteBuffer buffer) 
    throws BufferOverflowException {
        try {
            int colId;
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.TYPE);
            if (-1 != colId){
                buffer.putInt(c.getInt(colId));
            } else {
                buffer.putInt(StructuredPostal.TYPE_HOME);
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.LABEL);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.STREET);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.POBOX);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.StructuredPostal.NEIGHBORHOOD);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.CITY);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.REGION);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.POSTCODE);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.StructuredPostal.COUNTRY);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c, buffer}, null, e);
            return IRawBufferWritable.RESULT_FAIL;
        }
        return IRawBufferWritable.RESULT_SUCCESS;
    }
    
    private static int cursorToOrganizationRaw(Cursor c, ByteBuffer buffer) 
    throws BufferOverflowException {
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Organization.COMPANY);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.Organization.TYPE);
            if (-1 != colId){
                buffer.putInt(c.getInt(colId));
            } else {
                buffer.putInt(Organization.TYPE_WORK);
            }
            colId = c.getColumnIndex(CommonDataKinds.Organization.LABEL);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.Organization.TITLE);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.Organization.DEPARTMENT);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.Organization.JOB_DESCRIPTION);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.Organization.SYMBOL);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.Organization.PHONETIC_NAME);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(
                    CommonDataKinds.Organization.OFFICE_LOCATION);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c, buffer}, null, e);
            return IRawBufferWritable.RESULT_FAIL;
        }
        return IRawBufferWritable.RESULT_SUCCESS;
    }
    
    private static int cursorToNicknameRaw(Cursor c, ByteBuffer buffer) 
    throws BufferOverflowException {
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Nickname.NAME);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.Nickname.TYPE);
            if (-1 != colId){
                buffer.putInt(c.getInt(colId));
            } else {
                buffer.putInt(Nickname.TYPE_DEFAULT);
            }
            colId = c.getColumnIndex(CommonDataKinds.Nickname.LABEL);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c, buffer}, null, e);
            return IRawBufferWritable.RESULT_FAIL;
        }
        return IRawBufferWritable.RESULT_SUCCESS;
    }
    
    private static int cursorToWebsiteRaw(Cursor c, ByteBuffer buffer) 
    throws BufferOverflowException {
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Website.URL);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            colId = c.getColumnIndex(CommonDataKinds.Website.TYPE);
            if (-1 != colId){
                buffer.putInt(c.getInt(colId));
            } else {
                buffer.putInt(Website.TYPE_HOMEPAGE);
            }
            colId = c.getColumnIndex(CommonDataKinds.Website.LABEL);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c, buffer}, null, e);
            return IRawBufferWritable.RESULT_FAIL;
        }
        return IRawBufferWritable.RESULT_SUCCESS;
    }
    
    private static int cursorToNoteRaw(Cursor c, ByteBuffer buffer) 
    throws BufferOverflowException {
        try {
            int colId;
            colId = c.getColumnIndex(CommonDataKinds.Note.NOTE);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c, buffer}, null, e);
            return IRawBufferWritable.RESULT_FAIL;
        }
        return IRawBufferWritable.RESULT_SUCCESS;
    }
    
    private static int cursorToGroupMembershipRaw(Cursor c, ByteBuffer buffer) 
    throws BufferOverflowException {
        try {
            int colId;
            colId = c.getColumnIndex(
                    CommonDataKinds.GroupMembership.GROUP_ROW_ID);
            if (-1 != colId){
                buffer.putLong(c.getLong(colId));
            } else {
                buffer.putLong(DatabaseRecordEntity.ID_NULL);
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c, buffer}, null, e);
            return IRawBufferWritable.RESULT_FAIL;
        }
        return IRawBufferWritable.RESULT_SUCCESS;
    }
    
    public static ContentValues createContentValues(ContactData data, 
            boolean setId){
        if (null == data){
            return null;
        }
        ContentValues values;

        int commonFieldsCount;
        if (setId){
            commonFieldsCount = 2;
        } else {
            commonFieldsCount = 1;
        }
        int mimeType = data.getMimeType();
        if (data instanceof StructuredName){
            // Name --------------------------------------------------------
            StructuredName name = (StructuredName)data;
            values = new ContentValues(commonFieldsCount + 9);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            /*values.put(CommonDataKinds.StructuredName.DISPLAY_NAME, 
                    name.getDisplayName());*/
            values.put(CommonDataKinds.StructuredName.GIVEN_NAME, 
                    name.getGivenName());
            values.put(CommonDataKinds.StructuredName.FAMILY_NAME, 
                    name.getFamilyName());
            values.put(CommonDataKinds.StructuredName.MIDDLE_NAME, 
                    name.getMiddleName());
            values.put(CommonDataKinds.StructuredName.PREFIX, 
                    name.getPrefix());
            values.put(CommonDataKinds.StructuredName.SUFFIX, 
                    name.getSuffix());
            values.put(CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME, 
                    name.getPhoneticFamilyName());
            values.put(CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME, 
                    name.getPhoneticGivenName());
            values.put(CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME, 
                    name.getPhoneticMiddleName());
        }else if (data instanceof Phone){
            // Phone -------------------------------------------------------
            Phone phone = (Phone)data;
            values = new ContentValues(commonFieldsCount + 4);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Phone.NUMBER, phone.getNumber());
            values.put(CommonDataKinds.Phone.TYPE, phone.getType());
            values.put(CommonDataKinds.Phone.LABEL, phone.getLabel());
            // Added by Shaoying Han
            values.put(COLUMN_BINDING_SIM_ID, phone.getBindingSimId());
        }else if (data instanceof Photo){
            // Photo -------------------------------------------------------
            Photo photo = (Photo)data;
            values = new ContentValues(commonFieldsCount + 2);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Photo.PHOTO, photo.getPhotoBytes());
        }else if (data instanceof Im){
            // IM ----------------------------------------------------------
            Im im = (Im)data;
            values = new ContentValues(commonFieldsCount + 6);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Im.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Im.DATA, im.getData());
            values.put(CommonDataKinds.Im.TYPE, im.getType());
            values.put(CommonDataKinds.Im.LABEL, im.getLabel());
            values.put(CommonDataKinds.Im.PROTOCOL, im.getProtocol());
            values.put(CommonDataKinds.Im.CUSTOM_PROTOCOL, 
                    im.getCustomProtocol());
        }else if (data instanceof Email){
            // Email -------------------------------------------------------
            Email email = (Email)data;
            values = new ContentValues(commonFieldsCount + 4);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Email.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Email.DATA, email.getData());
            values.put(CommonDataKinds.Email.TYPE, email.getType());
            values.put(CommonDataKinds.Email.LABEL, email.getLabel());
        }else if (data instanceof StructuredPostal){
            // Postal ------------------------------------------------------
            StructuredPostal postal = (StructuredPostal)data;
            values = new ContentValues(commonFieldsCount + 11);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, 
                    postal.getFormattedAddress());
            values.put(CommonDataKinds.StructuredPostal.TYPE, 
                    postal.getType());
            values.put(CommonDataKinds.StructuredPostal.LABEL, 
                    postal.getLabel());
            values.put(CommonDataKinds.StructuredPostal.STREET, 
                    postal.getStreet());
            values.put(CommonDataKinds.StructuredPostal.POBOX, 
                    postal.getPobox());
            values.put(CommonDataKinds.StructuredPostal.NEIGHBORHOOD, 
                    postal.getNeighborhood());
            values.put(CommonDataKinds.StructuredPostal.CITY, 
                    postal.getCity());
            values.put(CommonDataKinds.StructuredPostal.REGION, 
                    postal.getRegion());
            values.put(CommonDataKinds.StructuredPostal.POSTCODE, 
                    postal.getPostcode());
            values.put(CommonDataKinds.StructuredPostal.COUNTRY, 
                    postal.getCountry());
        }else if (data instanceof Organization){
            // Organization ------------------------------------------------
            Organization organization = (Organization)data;
            values = new ContentValues(commonFieldsCount + 10);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Organization.COMPANY, 
                    organization.getCompany());
            values.put(CommonDataKinds.Organization.TYPE, 
                    organization.getType());
            values.put(CommonDataKinds.Organization.LABEL, 
                    organization.getLabel());
            values.put(CommonDataKinds.Organization.TITLE, 
                    organization.getTitle());
            values.put(CommonDataKinds.Organization.DEPARTMENT, 
                    organization.getTitle());
            values.put(CommonDataKinds.Organization.JOB_DESCRIPTION, 
                    organization.getJobDescription());
            values.put(CommonDataKinds.Organization.SYMBOL, 
                    organization.getSymbol());
            values.put(CommonDataKinds.Organization.PHONETIC_NAME, 
                    organization.getPhoneticName());
            values.put(CommonDataKinds.Organization.OFFICE_LOCATION, 
                    organization.getOfficeLocation());
        }else if (data instanceof Nickname){
            // Nickname ----------------------------------------------------
            Nickname nickname = (Nickname)data;
            values = new ContentValues(commonFieldsCount + 4);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Nickname.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Nickname.NAME, nickname.getName());
            values.put(CommonDataKinds.Nickname.TYPE, nickname.getType());
            values.put(CommonDataKinds.Nickname.LABEL, nickname.getLabel());
        }else if (data instanceof Website){
            // Website -----------------------------------------------------
            Website website = (Website)data;
            values = new ContentValues(commonFieldsCount + 4);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Website.URL, website.getUrl());
            values.put(CommonDataKinds.Website.TYPE, website.getType());
            values.put(CommonDataKinds.Website.LABEL, website.getLabel());
        }else if (data instanceof Note){
            // Note --------------------------------------------------------
            Note note = (Note)data;
            values = new ContentValues(commonFieldsCount + 2);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Note.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Note.NOTE, note.getNote());
        }else if (data instanceof GroupMembership){
            // Group membership --------------------------------------------
            GroupMembership groupMembership = (GroupMembership)data;
            values = new ContentValues(commonFieldsCount + 2);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.GroupMembership.GROUP_ROW_ID, 
                    groupMembership.getGroupId());
        }else{
            Debugger.logE(new Object[]{data, setId}, 
                    "Illegal mime type: " + mimeType);
            return null;
        }
        if (setId){
            values.put(Data._ID, data.getId());
        }
        values.put(Data.RAW_CONTACT_ID, data.getRawContactId());
        
        return values;
    }
    
    public static ContentValues[] createContentValuesArray(RawContact contact, 
            boolean setId){
        if (null == contact){
            return null;
        }
        ContentValues values;
        ContentValues[] valuesArray;
        List<ContentValues> valuesBuffer = new ArrayList<ContentValues>();

        int commonFieldsCount;
        if (setId){
            commonFieldsCount = 2;
        } else {
            commonFieldsCount = 1;
        }
        // Name --------------------------------------------------------
        for (StructuredName name : contact.getNames()){
            values = new ContentValues(commonFieldsCount + 9);
            if (setId){
                values.put(Data._ID, name.getId());
            }
            values.put(Data.RAW_CONTACT_ID, contact.getId());
            
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            /*values.put(CommonDataKinds.StructuredName.DISPLAY_NAME, 
                    name.getDisplayName());*/
            values.put(CommonDataKinds.StructuredName.GIVEN_NAME, 
                    name.getGivenName());
            values.put(CommonDataKinds.StructuredName.FAMILY_NAME, 
                    name.getFamilyName());
            values.put(CommonDataKinds.StructuredName.MIDDLE_NAME, 
                    name.getMiddleName());
            values.put(CommonDataKinds.StructuredName.PREFIX, 
                    name.getPrefix());
            values.put(CommonDataKinds.StructuredName.SUFFIX, 
                    name.getSuffix());
            values.put(CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME, 
                    name.getPhoneticFamilyName());
            values.put(CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME, 
                    name.getPhoneticGivenName());
            values.put(CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME, 
                    name.getPhoneticMiddleName());
            valuesBuffer.add(values);
        }
        // Phone -------------------------------------------------------
        for (Phone phone : contact.getPhones()){
            values = new ContentValues(commonFieldsCount + 4);
            if (setId){
                values.put(Data._ID, phone.getId());
            }
            values.put(Data.RAW_CONTACT_ID, contact.getId());
            
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Phone.NUMBER, phone.getNumber());
            values.put(CommonDataKinds.Phone.TYPE, phone.getType());
            values.put(CommonDataKinds.Phone.LABEL, phone.getLabel());
            //Added by Shaoying Han
            values.put(COLUMN_BINDING_SIM_ID, phone.getBindingSimId());
            valuesBuffer.add(values);
        }
        // Photo -------------------------------------------------------
        for (Photo photo : contact.getPhotos()){
            values = new ContentValues(commonFieldsCount + 2);
            if (setId){
                values.put(Data._ID, photo.getId());
            }
            values.put(Data.RAW_CONTACT_ID, contact.getId());

            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Photo.PHOTO, photo.getPhotoBytes());
            valuesBuffer.add(values);
        }
        // IM -------------------------------------------------------
        for (Im im : contact.getIms()){
            values = new ContentValues(commonFieldsCount + 6);
            if (setId){
                values.put(Data._ID, im.getId());
            }
            values.put(Data.RAW_CONTACT_ID, contact.getId());
            
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Im.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Im.DATA, im.getData());
            values.put(CommonDataKinds.Im.TYPE, im.getType());
            values.put(CommonDataKinds.Im.LABEL, im.getLabel());
            values.put(CommonDataKinds.Im.PROTOCOL, im.getProtocol());
            values.put(CommonDataKinds.Im.CUSTOM_PROTOCOL, 
                    im.getCustomProtocol());
            valuesBuffer.add(values);
        }
        // Email -------------------------------------------------------
        for (Email email : contact.getEmails()){
            values = new ContentValues(commonFieldsCount + 4);
            if (setId){
                values.put(Data._ID, email.getId());
            }
            values.put(Data.RAW_CONTACT_ID, contact.getId());

            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Email.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Email.DATA, email.getData());
            values.put(CommonDataKinds.Email.TYPE, email.getType());
            values.put(CommonDataKinds.Email.LABEL, email.getLabel());
            valuesBuffer.add(values);
        }
        // Postal ------------------------------------------------------
        for (StructuredPostal postal : contact.getPostals()){
            values = new ContentValues(commonFieldsCount + 11);
            if (setId){
                values.put(Data._ID, postal.getId());
            }
            values.put(Data.RAW_CONTACT_ID, contact.getId());

            values.put(Data.MIMETYPE, 
                    CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, 
                    postal.getFormattedAddress());
            values.put(CommonDataKinds.StructuredPostal.TYPE, 
                    postal.getType());
            values.put(CommonDataKinds.StructuredPostal.LABEL, 
                    postal.getLabel());
            values.put(CommonDataKinds.StructuredPostal.STREET, 
                    postal.getStreet());
            values.put(CommonDataKinds.StructuredPostal.POBOX, 
                    postal.getPobox());
            values.put(CommonDataKinds.StructuredPostal.NEIGHBORHOOD, 
                    postal.getNeighborhood());
            values.put(CommonDataKinds.StructuredPostal.CITY, 
                    postal.getCity());
            values.put(CommonDataKinds.StructuredPostal.REGION, 
                    postal.getRegion());
            values.put(CommonDataKinds.StructuredPostal.POSTCODE, 
                    postal.getPostcode());
            values.put(CommonDataKinds.StructuredPostal.COUNTRY, 
                    postal.getCountry());
            valuesBuffer.add(values);
        }
        // Organization ------------------------------------------------
        for (Organization organization : contact.getOrganizations()){
            values = new ContentValues(commonFieldsCount + 10);
            if (setId){
                values.put(Data._ID, organization.getId());
            }
            values.put(Data.RAW_CONTACT_ID, contact.getId());

            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Organization.COMPANY, 
                    organization.getCompany());
            values.put(CommonDataKinds.Organization.TYPE, 
                    organization.getType());
            values.put(CommonDataKinds.Organization.LABEL, 
                    organization.getLabel());
            values.put(CommonDataKinds.Organization.TITLE, 
                    organization.getTitle());
            values.put(CommonDataKinds.Organization.DEPARTMENT, 
                    organization.getTitle());
            values.put(CommonDataKinds.Organization.JOB_DESCRIPTION, 
                    organization.getJobDescription());
            values.put(CommonDataKinds.Organization.SYMBOL, 
                    organization.getSymbol());
            values.put(CommonDataKinds.Organization.PHONETIC_NAME, 
                    organization.getPhoneticName());
            values.put(CommonDataKinds.Organization.OFFICE_LOCATION, 
                    organization.getOfficeLocation());
            valuesBuffer.add(values);
        }
        // Nickname ----------------------------------------------------
        for (Nickname nickname : contact.getNicknames()){
            values = new ContentValues(commonFieldsCount + 4);
            if (setId){
                values.put(Data._ID, nickname.getId());
            }
            values.put(Data.RAW_CONTACT_ID, contact.getId());

            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Nickname.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Nickname.NAME, nickname.getName());
            values.put(CommonDataKinds.Nickname.TYPE, nickname.getType());
            values.put(CommonDataKinds.Nickname.LABEL, nickname.getLabel());
            valuesBuffer.add(values);
        }
        // Website -----------------------------------------------------
        for (Website website : contact.getWebsites()){
            values = new ContentValues(commonFieldsCount + 4);
            if (setId){
                values.put(Data._ID, website.getId());
            }
            values.put(Data.RAW_CONTACT_ID, contact.getId());

            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Website.URL, website.getUrl());
            values.put(CommonDataKinds.Website.TYPE, website.getType());
            values.put(CommonDataKinds.Website.LABEL, website.getLabel());
            valuesBuffer.add(values);
        }
        // Note --------------------------------------------------------
        for (Note note : contact.getNotes()){
            values = new ContentValues(commonFieldsCount + 2);
            if (setId){
                values.put(Data._ID, note.getId());
            }
            values.put(Data.RAW_CONTACT_ID, contact.getId());

            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Note.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Note.NOTE, note.getNote());
            valuesBuffer.add(values);
        }
        // Group membership --------------------------------------------
        for (GroupMembership groupMembership : contact.getGroupMemberships()){
            values = new ContentValues(commonFieldsCount + 2);
            if (setId){
                values.put(Data._ID, groupMembership.getId());
            }
            values.put(Data.RAW_CONTACT_ID, contact.getId());

            values.put(Data.MIMETYPE, 
                    CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.GroupMembership.GROUP_ROW_ID, 
                    groupMembership.getGroupId());
            valuesBuffer.add(values);
        }
        
        valuesArray = new ContentValues[valuesBuffer.size()];
        valuesBuffer.toArray(valuesArray);
        
        return valuesArray;
    }

    public static MeasuredContentValues createMeasuredContentValues(
            ContactData data, boolean setId){
        if (null == data){
            return null;
        }
        MeasuredContentValues values;
        
        int commonFieldsCount;
        if (setId){
            commonFieldsCount = 2;
        } else {
            commonFieldsCount = 1;
        }
        int mimeType = data.getMimeType();
        if (data instanceof StructuredName){
            // Name --------------------------------------------------------
            StructuredName name = (StructuredName)data;
            values = new MeasuredContentValues(commonFieldsCount + 9);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            /*values.put(CommonDataKinds.StructuredName.DISPLAY_NAME, 
                    name.getDisplayName());*/
            values.put(CommonDataKinds.StructuredName.GIVEN_NAME, 
                    name.getGivenName());
            values.put(CommonDataKinds.StructuredName.FAMILY_NAME, 
                    name.getFamilyName());
            values.put(CommonDataKinds.StructuredName.MIDDLE_NAME, 
                    name.getMiddleName());
            values.put(CommonDataKinds.StructuredName.PREFIX, 
                    name.getPrefix());
            values.put(CommonDataKinds.StructuredName.SUFFIX, 
                    name.getSuffix());
            values.put(CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME, 
                    name.getPhoneticFamilyName());
            values.put(CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME, 
                    name.getPhoneticGivenName());
            values.put(CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME, 
                    name.getPhoneticMiddleName());
        }else if (data instanceof Phone){
            // Phone -------------------------------------------------------
            Phone phone = (Phone)data;
            values = new MeasuredContentValues(commonFieldsCount + 4);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Phone.NUMBER, phone.getNumber());
            values.put(CommonDataKinds.Phone.TYPE, phone.getType());
            values.put(CommonDataKinds.Phone.LABEL, phone.getLabel());
            // Added by Shaoying Han
            values.put(COLUMN_BINDING_SIM_ID, phone.getBindingSimId());
        }else if (data instanceof Photo){
            // Photo -------------------------------------------------------
            Photo photo = (Photo)data;
            values = new MeasuredContentValues(commonFieldsCount + 2);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Photo.PHOTO, photo.getPhotoBytes());
        }else if (data instanceof Im){
            // IM ----------------------------------------------------------
            Im im = (Im)data;
            values = new MeasuredContentValues(commonFieldsCount + 6);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Im.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Im.DATA, im.getData());
            values.put(CommonDataKinds.Im.TYPE, im.getType());
            values.put(CommonDataKinds.Im.LABEL, im.getLabel());
            values.put(CommonDataKinds.Im.PROTOCOL, im.getProtocol());
            values.put(CommonDataKinds.Im.CUSTOM_PROTOCOL, 
                    im.getCustomProtocol());
        }else if (data instanceof Email){
            // Email -------------------------------------------------------
            Email email = (Email)data;
            values = new MeasuredContentValues(commonFieldsCount + 4);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Email.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Email.DATA, email.getData());
            values.put(CommonDataKinds.Email.TYPE, email.getType());
            values.put(CommonDataKinds.Email.LABEL, email.getLabel());
        }else if (data instanceof StructuredPostal){
            // Postal ------------------------------------------------------
            StructuredPostal postal = (StructuredPostal)data;
            values = new MeasuredContentValues(commonFieldsCount + 11);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, 
                    postal.getFormattedAddress());
            values.put(CommonDataKinds.StructuredPostal.TYPE, 
                    postal.getType());
            values.put(CommonDataKinds.StructuredPostal.LABEL, 
                    postal.getLabel());
            values.put(CommonDataKinds.StructuredPostal.STREET, 
                    postal.getStreet());
            values.put(CommonDataKinds.StructuredPostal.POBOX, 
                    postal.getPobox());
            values.put(CommonDataKinds.StructuredPostal.NEIGHBORHOOD, 
                    postal.getNeighborhood());
            values.put(CommonDataKinds.StructuredPostal.CITY, 
                    postal.getCity());
            values.put(CommonDataKinds.StructuredPostal.REGION, 
                    postal.getRegion());
            values.put(CommonDataKinds.StructuredPostal.POSTCODE, 
                    postal.getPostcode());
            values.put(CommonDataKinds.StructuredPostal.COUNTRY, 
                    postal.getCountry());
        }else if (data instanceof Organization){
            // Organization ------------------------------------------------
            Organization organization = (Organization)data;
            values = new MeasuredContentValues(commonFieldsCount + 10);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Organization.COMPANY, 
                    organization.getCompany());
            values.put(CommonDataKinds.Organization.TYPE, 
                    organization.getType());
            values.put(CommonDataKinds.Organization.LABEL, 
                    organization.getLabel());
            values.put(CommonDataKinds.Organization.TITLE, 
                    organization.getTitle());
            values.put(CommonDataKinds.Organization.DEPARTMENT, 
                    organization.getTitle());
            values.put(CommonDataKinds.Organization.JOB_DESCRIPTION, 
                    organization.getJobDescription());
            values.put(CommonDataKinds.Organization.SYMBOL, 
                    organization.getSymbol());
            values.put(CommonDataKinds.Organization.PHONETIC_NAME, 
                    organization.getPhoneticName());
            values.put(CommonDataKinds.Organization.OFFICE_LOCATION, 
                    organization.getOfficeLocation());
        }else if (data instanceof Nickname){
            // Nickname ----------------------------------------------------
            Nickname nickname = (Nickname)data;
            values = new MeasuredContentValues(commonFieldsCount + 4);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Nickname.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Nickname.NAME, nickname.getName());
            values.put(CommonDataKinds.Nickname.TYPE, nickname.getType());
            values.put(CommonDataKinds.Nickname.LABEL, nickname.getLabel());
        }else if (data instanceof Website){
            // Website -----------------------------------------------------
            Website website = (Website)data;
            values = new MeasuredContentValues(commonFieldsCount + 4);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Website.URL, website.getUrl());
            values.put(CommonDataKinds.Website.TYPE, website.getType());
            values.put(CommonDataKinds.Website.LABEL, website.getLabel());
        }else if (data instanceof Note){
            // Note --------------------------------------------------------
            Note note = (Note)data;
            values = new MeasuredContentValues(commonFieldsCount + 2);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.Note.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.Note.NOTE, note.getNote());
        }else if (data instanceof GroupMembership){
            // Group membership --------------------------------------------
            GroupMembership groupMembership = (GroupMembership)data;
            values = new MeasuredContentValues(commonFieldsCount + 2);
            values.put(Data.MIMETYPE, 
                    CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);
            values.put(CommonDataKinds.GroupMembership.GROUP_ROW_ID, 
                    groupMembership.getGroupId());
        }else{
            Debugger.logE(new Object[]{data, setId}, 
                    "Illegal mime type: " + mimeType);
            return null;
        }
        if (setId){
            values.put(Data._ID, data.getId());
        }
        values.put(Data.RAW_CONTACT_ID, data.getRawContactId());
        
        return values;
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
