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

import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.RawContactsEntity;

import com.mediatek.apst.target.util.Debugger;
import com.mediatek.apst.util.entity.DatabaseRecordEntity;
import com.mediatek.apst.util.entity.contacts.ContactData;
import com.mediatek.apst.util.entity.contacts.Email;
import com.mediatek.apst.util.entity.contacts.GroupMembership;
import com.mediatek.apst.util.entity.contacts.Im;
import com.mediatek.apst.util.entity.contacts.Nickname;
import com.mediatek.apst.util.entity.contacts.Note;
import com.mediatek.apst.util.entity.contacts.Organization;
import com.mediatek.apst.util.entity.contacts.Phone;
import com.mediatek.apst.util.entity.contacts.Photo;
import com.mediatek.apst.util.entity.contacts.StructuredName;
import com.mediatek.apst.util.entity.contacts.StructuredPostal;
import com.mediatek.apst.util.entity.contacts.Website;

public abstract class RawContactsEntityContent {
    //==============================================================
    // Constants                                                    
    //==============================================================
    
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
        long id = DatabaseRecordEntity.ID_NULL;
        long rawContactId = DatabaseRecordEntity.ID_NULL;
        boolean primary = false;
        boolean superPrimary = false;
        String strMimeType = null;
        
        try {
            // raw contact id
            colId = c.getColumnIndex(RawContactsEntity._ID);
            if (-1 != colId){
                rawContactId = c.getLong(colId);
            }
            // data id
            colId = c.getColumnIndex(RawContactsEntity.DATA_ID);
            if (-1 != colId){
                if (c.isNull(colId)){
                    // Data row ID will be null only if the raw contact has no 
                    // data rows, thus, we can return by here
                    data = new ContactData(DatabaseRecordEntity.ID_NULL, -1);
                    // Set the raw contact id before return, for this could tell  
                    // the caller that this raw contact has no data
                    data.setRawContactId(rawContactId);
                    return data;
                } else {
                    id = c.getLong(colId);
                }
            }
            // is primary
            colId = c.getColumnIndex(RawContactsEntity.IS_PRIMARY);
            if (-1 != colId){
                primary = (c.getInt(colId) == DatabaseRecordEntity.TRUE);
            }
            // is super primary
            colId = c.getColumnIndex(RawContactsEntity.IS_SUPER_PRIMARY);
            if (-1 != colId){
                superPrimary = (c.getInt(colId) == DatabaseRecordEntity.TRUE);
            }
            // MIME type
            colId = c.getColumnIndex(RawContactsEntity.MIMETYPE);
            if (-1 != colId){
                strMimeType = c.getString(colId);
            }
            
            // Create a new contact data object according to its MIME type
            if (null == strMimeType){
            	Debugger.logW(new Object[]{c}, "mimeType is absent in cursor.");
                return null;
            } else if (strMimeType.equals(StructuredName.MIME_TYPE_STRING)){
                // Name --------------------------------------------
                data = cursorToStructuredName(c);
            } else if (strMimeType.equals(Phone.MIME_TYPE_STRING)){
                // Phone -------------------------------------------
                data = cursorToPhone(c);
            } else if (strMimeType.equals(Photo.MIME_TYPE_STRING)){
                // Photo -------------------------------------------
                data = cursorToPhoto(c);
            } else if (strMimeType.equals(Im.MIME_TYPE_STRING)){
                // IM ----------------------------------------------
                data = cursorToIm(c);
            } else if (strMimeType.equals(Email.MIME_TYPE_STRING)){
                // Email -------------------------------------------
                data = cursorToEmail(c);
            } else if (strMimeType.equals(StructuredPostal.MIME_TYPE_STRING)){
                // Postal ------------------------------------------
                data = cursorToStructuredPostal(c);
            } else if (strMimeType.equals(Organization.MIME_TYPE_STRING)){
                // Organization ------------------------------------
                data = cursorToOrganization(c);
            } else if (strMimeType.equals(Nickname.MIME_TYPE_STRING)){
                // Nickname ----------------------------------------
                data = cursorToNickname(c);
            } else if (strMimeType.equals(Website.MIME_TYPE_STRING)){
                // Website -----------------------------------------
                data = cursorToWebsite(c);
            } else if (strMimeType.equals(Note.MIME_TYPE_STRING)){
                // Note --------------------------------------------
                data = cursorToNote(c);
            } else if (strMimeType.equals(GroupMembership.MIME_TYPE_STRING)){
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
            colId = c.getColumnIndex(ContactDataContent.COLUMN_BINDING_SIM_ID);
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
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
