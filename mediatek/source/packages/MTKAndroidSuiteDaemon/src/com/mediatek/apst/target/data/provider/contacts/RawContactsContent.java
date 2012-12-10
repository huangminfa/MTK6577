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

import android.database.Cursor;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;

import com.mediatek.apst.target.data.proxy.IRawBufferWritable;
import com.mediatek.apst.target.util.Debugger;
import com.mediatek.apst.target.util.Global;
import com.mediatek.apst.util.entity.DataStoreLocations;
import com.mediatek.apst.util.entity.DatabaseRecordEntity;
import com.mediatek.apst.util.entity.RawTransUtil;
import com.mediatek.apst.util.entity.contacts.RawContact;

public abstract class RawContactsContent {
    //==============================================================
    // Constants                                                    
    //==============================================================
    // For MTK SIM Contacts feature.
    /**
     * Column name of indicate_phone_or_sim_contact.
     */
    public static final String COLUMN_SOURCE_LOCATION = 
        "indicate_phone_or_sim_contact";
    /**
     * Column name of timestamp. Added by Shaoying Han
     */
    public static final String COLUMN_MODIFY_TIME = 
        "timestamp";
    
    public static final String COLUMN_INDEX_IN_SIM = "index_in_sim";
    
    public static final int INSERT_FAIL = -1001;
    
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
    public static RawContact cursorToRawContact(Cursor c){
        if (null == c || c.getPosition() == -1 || 
                c.getPosition() == c.getCount()){
            Debugger.logW(new Object[]{c}, "Cursor is null.");
            return null;
        }
        
        // Create a new raw contact object
        RawContact contact = new RawContact();
        contact.setStoreLocation(DataStoreLocations.PHONE);

        try {
            // Read basic info fields of the raw contact -----------------------
            int colId;
            // id
            colId = c.getColumnIndex(RawContacts._ID);
            if (-1 != colId){
                contact.setId(c.getLong(colId));
            }
            // display name
            colId = c.getColumnIndex(Contacts.DISPLAY_NAME);
            if (-1 != colId){
                contact.setDisplayName(c.getString(colId));
            }
            // starred
            colId = c.getColumnIndex(RawContacts.STARRED);
            if (-1 != colId){
                contact.setStarred(c.getInt(colId) == 
                    DatabaseRecordEntity.TRUE);
            }
            // send to voicemail
            colId = c.getColumnIndex(RawContacts.SEND_TO_VOICEMAIL);
            if (-1 != colId){
                contact.setSendToVoicemail(c.getInt(colId) == 
                    DatabaseRecordEntity.TRUE);
            }
            // times contacted
            /*colId = c.getColumnIndex(RawContacts.TIMES_CONTACTED);
            if (-1 != colId){
                contact.setTimesContacted(c.getInt(colId));
            }*/
            // last time contacted
            /*colId = c.getColumnIndex(RawContacts.LAST_TIME_CONTACTED);
            if (-1 != colId){
                contact.setLastTimeContacted(c.getLong(colId));
            }*/
            // custom ringtone
            /*colId = c.getColumnIndex(RawContacts.CUSTOM_RINGTONE);
            if (-1 != colId){
                contact.setCustomRingtone(c.getString(colId));
            }*/
            // version
            colId = c.getColumnIndex(RawContacts.VERSION);
            if (-1 != colId){
                contact.setVersion(c.getInt(colId));
            }
            // dirty
            colId = c.getColumnIndex(RawContacts.DIRTY);
            if (-1 != colId){
                contact.setDirty(c.getInt(colId) == DatabaseRecordEntity.TRUE);
            }
            
            //For MTK SIM Contacts feature.
            // indicate_phone_or_sim_contact
            colId = c.getColumnIndex(COLUMN_SOURCE_LOCATION);
            if (-1 != colId){
            	int indicateSimOrPhone = c.getInt(colId);
            	// For MTK SIM Contacts feature.
                // sourceLocation
                contact.setSourceLocation(Global.getSourceLocationById(indicateSimOrPhone));
                //Sim Id. Added by Shaoying Han
                contact.setSimId(indicateSimOrPhone);
                // Sim Name. Added by Shaoying Han
                contact.setSimName(Global.getSimName(c.getInt(colId)));
            }
            // Modify time. Added by Shaoying Han
            colId = c.getColumnIndex(COLUMN_MODIFY_TIME);
            if (-1 != colId){
                contact.setModifyTime(c.getLong(colId));
            } 
            
            colId = c.getColumnIndex(COLUMN_INDEX_IN_SIM);
            if (-1 != colId){
            	contact.setSimIndex(c.getInt(colId));
            	Debugger.logI(new Object[]{c}, "contact.getSimIndex()" + contact.getSimIndex());
            } else {
            	contact.setSimIndex(-1);
            }
//           contact.setIccid(Global.getIccid(contact.getSourceLocation()));
//           Debugger.logE(new Object[]{},"SourceLocation="+contact.getSourceLocation());
//           Debugger.logE(new Object[]{},"Iccid="+contact.getIccid());
            // -----------------------------------------------------------------
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c}, null, e);
        }
        
        return contact;
    }
    
    /**
     * 
     * @param c
     * @param buffer
     * @return 
     */
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
        try {
            int colId;
            // id
            colId = c.getColumnIndex(RawContacts._ID);
            if (-1 != colId){
                buffer.putLong(c.getLong(colId));
            } else {
                buffer.putLong(DatabaseRecordEntity.ID_NULL);
            }
            // store location
            buffer.putInt(DataStoreLocations.PHONE);
            // display name
            colId = c.getColumnIndex(Contacts.DISPLAY_NAME);
            if (-1 != colId){
                RawTransUtil.putString(buffer, c.getString(colId));
            } else {
                RawTransUtil.putString(buffer, null);
            }
            // primary number
            RawTransUtil.putString(buffer, null);
            // group memberships
            buffer.putInt(0); // Only put a int to tell that list size is 0
            // sim name Changed by Shaoying Han
        	colId = c.getColumnIndex(COLUMN_SOURCE_LOCATION);
        	if (-1 != colId) {
        		RawTransUtil.putString(buffer, Global.getSimName(c.getInt(colId)));
        	} else {
        		RawTransUtil.putString(buffer, null);
        	}
        	// Modify time. Added by Shaoying Han
        	colId = c.getColumnIndex(COLUMN_MODIFY_TIME);
        	if (-1 != colId) {
        		buffer.putLong(c.getLong(colId));
        	} else {
        		buffer.putLong(DatabaseRecordEntity.ID_NULL);
        	}
            	
            // starred
            colId = c.getColumnIndex(RawContacts.STARRED);
            if (-1 != colId){
                RawTransUtil.putBoolean(buffer, (c.getInt(colId) == 
                    DatabaseRecordEntity.TRUE));
            } else {
                RawTransUtil.putBoolean(buffer, false);
            }
            // send to voicemail
            colId = c.getColumnIndex(RawContacts.SEND_TO_VOICEMAIL);
            if (-1 != colId){
                RawTransUtil.putBoolean(buffer, (c.getInt(colId) == 
                    DatabaseRecordEntity.TRUE));
            } else {
                RawTransUtil.putBoolean(buffer, false);
            }
            // times contacted
            // last time contacted
            // custom ringtone
            // version
            colId = c.getColumnIndex(RawContacts.VERSION);
            if (-1 != colId){
                buffer.putInt(c.getInt(colId));
            } else {
                buffer.putInt(-1);
            }
            // dirty
            // names
            buffer.putInt(0); // Only put a int to tell that list size is 0
            // phones
            buffer.putInt(0); // Only put a int to tell that list size is 0
            // photos
            buffer.putInt(0); // Only put a int to tell that list size is 0
            // emails
            buffer.putInt(0); // Only put a int to tell that list size is 0
            // ims
            buffer.putInt(0); // Only put a int to tell that list size is 0
            // postals
            buffer.putInt(0); // Only put a int to tell that list size is 0
            // organizations
            buffer.putInt(0); // Only put a int to tell that list size is 0
            // notes
            buffer.putInt(0); // Only put a int to tell that list size is 0
            // nicknames
            buffer.putInt(0); // Only put a int to tell that list size is 0
            // websites
            buffer.putInt(0); // Only put a int to tell that list size is 0
            
            
            colId = c.getColumnIndex(COLUMN_SOURCE_LOCATION);
            if (-1 != colId) {
            	int indicateSimOrPhone = c.getInt(colId);
            	// For MTK SIM Contacts feature.
                // indicate_phone_or_sim_contact
                buffer.putInt(Global.getSourceLocationById(indicateSimOrPhone));
                // simId
                buffer.putInt(indicateSimOrPhone);
//                RawTransUtil.putString(buffer, Global.getIccid(Global
//                        .getSourceLocationById(indicateSimOrPhone)));
//                Debugger.logE(new Object[]{},"SourceLocation="+Global.getSourceLocationById(indicateSimOrPhone));
//                Debugger.logE(new Object[]{},"Iccid="+Global.getIccid(Global
//                        .getSourceLocationById(indicateSimOrPhone)));
                
            } else {
                buffer.putInt(RawContact.SOURCE_PHONE);
                // simId. -1 means can not get sim id.
                buffer.putInt(-1);
                
//                RawTransUtil.putString(buffer, null);
            }
            
            colId = c.getColumnIndex(COLUMN_INDEX_IN_SIM);
            if (-1 != colId){
                buffer.putInt(c.getInt(colId));
                Debugger.logI(new Object[]{c}, "c.getColumnIndex(COLUMN_INDEX_IN_SIM): " + c.getInt(colId));
            } else {
                buffer.putInt(-1);
            }
            
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[]{c, buffer}, null, e);
            buffer.reset();
            return IRawBufferWritable.RESULT_FAIL;
        } catch (BufferOverflowException e) {
            /*DebugHelper.logW("[RawContactsContent] " +
                    "cursorToRaw(" + c + "): Not enough space left in " +
                    "buffer. ", e);*/
            buffer.reset();
            return IRawBufferWritable.RESULT_NOT_ENOUGH_BUFFER;
        }
        return IRawBufferWritable.RESULT_SUCCESS;
    }
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
